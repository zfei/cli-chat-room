package me.zfei.clichatroom.models;

import me.zfei.clichatroom.ChatRoom;
import me.zfei.clichatroom.utils.MulticastType;
import me.zfei.clichatroom.utils.Networker;
import me.zfei.clichatroom.utils.VectorTimeStamp;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zfei on 3/31/14.
 */
public class MemberListener extends Thread {

    private Member owner;
    private HashSet<String> receivedMessages;
    private Queue<String> holdBackQueue;

    private Networker networker;

    public MemberListener(Member owner) {
        this.owner = owner;
        this.receivedMessages = new HashSet<String>();
        this.holdBackQueue = new LinkedList<String>();

        this.networker = new Networker();

        if (ChatRoom.MULTICAST_TYPE == MulticastType.RELIABLE_CAUSAL_ORDERING) {
            // start daemon thread that processes the hold back queue
            Thread t = new Thread() {

                @Override
                public void run() {
                    while (true) {
                        Iterator<String> it = holdBackQueue.iterator();
                        while (it.hasNext()) {
                            if (causalDeliver(new Message(it.next()), false)) {
                                it.remove();
                            }
                        }

                        // wait some time and try again
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };

            t.start();
        }
    }

    private void deliver(String message, String tsString) {
        System.out.format("MEMBER %d DELIVERED %s AT %s\n", this.owner.getIdentifier(), message, tsString);
    }

    private boolean causalDeliver(Message messageObj, boolean holdBack) {
        synchronized (this.owner.getTimestamp()) {
            VectorTimeStamp messageVts = new VectorTimeStamp(messageObj.getTsString());

            if (canCausalDeliver(messageVts, messageObj.getSenderId())) {
                // increment sender entry in timestamp
                ((VectorTimeStamp) this.owner.getTimestamp()).incrementAt(messageObj.getSenderId());

                deliver(messageObj.getMessage(), this.owner.getTimestamp().toString());

                return true;
            } else if (holdBack) {
                this.holdBackQueue.offer(messageObj.getSerializedMessage());
            }
        }

        return false;
    }

    private boolean canCausalDeliver(VectorTimeStamp vts, int senderId) {
        int[] tsArr1 = ((VectorTimeStamp) this.owner.getTimestamp()).getTsArray();
        int[] tsArr2 = vts.getTsArray();

        for (int i = 0; i < tsArr1.length; i++) {
            if (i == senderId) {
                if (tsArr2[i] != tsArr1[i] + 1) {
                    System.out.format("MEMBER %d FAILS TO DELIVER MESSAGE SENT FROM %d AT %s, ITS TIMESTAMP IS %s\n", this.owner.getIdentifier(), senderId, vts.toString(), this.owner.getTimestamp().toString());
                    return false;
                }
            } else if (tsArr2[i] > tsArr1[i]) {
                System.out.format("MEMBER %d FAILS TO DELIVER MESSAGE SENT FROM %d AT %s, ITS TIMESTAMP IS %s\n", this.owner.getIdentifier(), senderId, vts.toString(), this.owner.getTimestamp().toString());
                return false;
            }
        }

        return true;
    }

    private synchronized void onReceivePacket(DatagramPacket receivedPacket) {
        // unpack received packet
        String serializedMessage = "";
        try {
            serializedMessage = new String(receivedPacket.getData(), "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Message messageObj = new Message(serializedMessage);

        String message = messageObj.getMessage();
        String tsString = messageObj.getTsString();
        int senderId = messageObj.getSenderId();

        // try to deliver message
        switch (ChatRoom.MULTICAST_TYPE) {
            case BASIC_MULTICAST:
                synchronized (this.owner.getTimestamp()) {
                    // increment timestamp
                    this.owner.getTimestamp().increment(tsString);

                    deliver(message, this.owner.getTimestamp().toString());
                }
                break;
            case RELIABLE_MULTICAST:
                if (!this.receivedMessages.contains(serializedMessage)) {
                    this.receivedMessages.add(serializedMessage);

                    if (this.owner.getIdentifier() != senderId) {
                        this.owner.getNetworker().basicMulticast(this.owner.getMembers(), this.owner, serializedMessage, true);
                    }

                    synchronized (this.owner.getTimestamp()) {
                        // increment timestamp
                        this.owner.getTimestamp().combine(tsString);

                        deliver(message, this.owner.getTimestamp().toString());
                    }
                }
                break;
            case RELIABLE_CAUSAL_ORDERING:
                causalDeliver(messageObj, true);
                break;
            case RELIABLE_TOTAL_ORDERING:
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket(this.owner.getPort());
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            final DatagramPacket receivedPacket = networker.unicastReceive(serverSocket);

            // deliver packet
            Thread t = new Thread() {

                @Override
                public void run() {
                    onReceivePacket(receivedPacket);
                }

            };

            t.start();
        }
    }
}
