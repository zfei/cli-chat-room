package me.zfei.clichatroom;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;

/**
 * Created by zfei on 3/31/14.
 */
public class MemberListener extends Thread {

    private Member owner;
    private HashSet<String> receivedMessages;

    public MemberListener(Member owner) {
        this.owner = owner;
        this.receivedMessages = new HashSet<String>();
    }

    private DatagramPacket unicastReceive(DatagramSocket serverSocket) {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            serverSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivePacket;
    }

    private void deliver(String message, String tsString) {
        System.out.format("MEMBER %d DELIVERED %s AT %s\n", this.owner.getIdentifier(), message, tsString);
    }

    private synchronized void onPacketReceive(DatagramPacket receivedPacket) {
        // unpack received packet
        String serializedMessage = null;
        try {
            serializedMessage = new String(receivedPacket.getData(), "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject jsonObj;
        String message = "";
        String tsString = "";
        int senderId = -1;
        try {
            jsonObj = new JSONObject(serializedMessage);
            message = jsonObj.getString("message");
            tsString = jsonObj.getString("timestamp");
            senderId = jsonObj.getInt("sender");
//            System.out.format("MEMBER %d RECEIVED %s AT %s\n", this.owner.getIdentifier(), message, tsString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                        this.owner.basicMulticast(serializedMessage, true);
                    }

                    deliver(message, this.owner.getTimestamp().toString());
                }
                break;
            case RELIABLE_CAUSAL_ORDERING:
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
            final DatagramPacket receivedPacket = unicastReceive(serverSocket);

            // deliver packet
            Thread t = new Thread() {

                @Override
                public void run() {
                    onPacketReceive(receivedPacket);
                }

            };

            t.start();
        }
    }
}
