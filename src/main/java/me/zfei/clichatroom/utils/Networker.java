package me.zfei.clichatroom.utils;

import me.zfei.clichatroom.ChatRoom;
import me.zfei.clichatroom.models.Member;
import me.zfei.clichatroom.models.Sequencer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zfei on 4/2/14.
 */
public class Networker {

    private int port;
    private TimeStamp timestamp;

    public Networker() {

    }

    public Networker(int port, TimeStamp timestamp) {
        this.port = port;
        this.timestamp = timestamp;
    }

    public int getPort() {
        return port;
    }

    public TimeStamp getTimestamp() {
        return timestamp;
    }

    private boolean packetWillDrop() {
        Random rand = new Random();
        return rand.nextDouble() < ChatRoom.DROP_RATE;
    }

    public void unicastSend(int senderId, int receiverPort, int receiverId, String msg) throws IOException {
        if (packetWillDrop()) {
            System.out.format("Oops, MEMBER %d DROPPED A PACKET SENT TO %d\n", senderId, receiverId);
            return;
        }

        DatagramSocket outgoingSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData;
        sendData = msg.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
        outgoingSocket.send(sendPacket);
        outgoingSocket.close();
    }

    public void multicast(ArrayList<Member> members, Member sender, String message) {
        int senderId = sender.getIdentifier();
        String serializedMessage;
        switch (ChatRoom.MULTICAST_TYPE) {
            case BASIC_MULTICAST:
                synchronized (this.timestamp) {
                    // increment timestamp
                    this.timestamp.increment();

                    serializedMessage = serializeToJson(message, senderId, this.timestamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, false);
                break;
            case RELIABLE_MULTICAST:
                synchronized (this.timestamp) {
                    // increment timestamp
                    this.timestamp.increment();

                    serializedMessage = serializeToJson(message, senderId, this.timestamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, true);
                break;
            case RELIABLE_CAUSAL_ORDERING:
                synchronized (this.timestamp) {
                    VectorTimeStamp incrementedStamp = new VectorTimeStamp((VectorTimeStamp) this.timestamp);
                    incrementedStamp.increment();

                    serializedMessage = serializeToJson(message, senderId, incrementedStamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, true);
                break;
            case RELIABLE_TOTAL_ORDERING:
                serializedMessage = serializeToJson(message, senderId, "");
                basicMulticast(members, sender, serializedMessage, true);
                break;
            default:
                break;
        }
    }

    private void initiateUnicastSend(final int senderId, final int receiverPort, final int receiverId, final String msg) {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(new Random().nextInt(1000));
                    unicastSend(senderId, receiverPort, receiverId, msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };

        t.start();
    }

    public void basicMulticast(ArrayList<Member> members, Member sender, String serializedMessage, boolean includeMyself) {
        if (sender instanceof Sequencer) {
            System.out.format("SEQUENCER SENDS ORDER\n");
        } else {
            System.out.format("MEMBER %d SENDS MESSAGE AT %s\n", sender.getIdentifier(), this.timestamp.toString());
        }

        for (Member m : members) {
            if (!includeMyself && m == sender) {
                continue;
            }

            initiateUnicastSend(sender.getIdentifier(), m.getPort(), m.getIdentifier(), serializedMessage);
        }
    }

    private String serializeToJson(String message, int senderId, String tsString) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("message", message);
            jsonObj.put("timestamp", tsString);
            jsonObj.put("sender", senderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj.toString();
    }

    public DatagramPacket unicastReceive(DatagramSocket serverSocket) {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            serverSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receivePacket;
    }



}
