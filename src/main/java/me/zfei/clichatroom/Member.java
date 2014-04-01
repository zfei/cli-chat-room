package me.zfei.clichatroom;

import me.zfei.clichatroom.utils.TimeStamp;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zfei on 3/31/14.
 */
public class Member extends Thread {

    private int identifier;
    private int port;

    private TimeStamp timestamp;

    private ArrayList<Member> members;
    private MemberListener listener;

    public Member(int identifier, TimeStamp initialTimestamp) throws SocketException {
        this.identifier = identifier;
        this.port = ChatRoom.PORT_BASE + identifier;

        this.timestamp = initialTimestamp;

        // start listening to incoming connections
        this.listener = new MemberListener(this);
        this.listener.start();
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getPort() {
        return port;
    }

    public TimeStamp getTimestamp() {
        return timestamp;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public void addMember(Member newMember) {
        this.members.add(newMember);
    }

    public void multicast(String serializedMessage) {
        System.out.format("MEMBER %d SENDS MESSAGE AT %s\n", this.identifier, this.timestamp.toString());

        switch (ChatRoom.MULTICAST_TYPE) {
            case BASIC_MULTICAST:
//                this.timestamp.increment();
                basicMulticast(serializedMessage, false);
                break;
            case RELIABLE_MULTICAST:
//                this.timestamp.increment();
                basicMulticast(serializedMessage, true);
                break;
            case RELIABLE_CAUSAL_ORDERING:
                break;
            case RELIABLE_TOTAL_ORDERING:
                break;
            default:
                break;
        }
    }

    public void basicMulticast(String serializedMessage, boolean includeMyself) {
        for (Member m : this.members) {
            if (!includeMyself && m == this) {
                continue;
            }

            initiateUnicastSend(m, serializedMessage);
        }
    }

    private boolean packetWillDrop() {
        Random rand = new Random();
        return rand.nextDouble() < ChatRoom.DROP_RATE;
    }

    public void unicastSend(Member other, String msg) throws IOException {
        if (packetWillDrop()) {
            System.out.format("Oops, MEMBER %d DROPPED A PACKET SENT TO %d\n", this.identifier, other.getIdentifier());
            return;
        }

        DatagramSocket outgoingSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData;
        sendData = msg.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, other.getPort());
        outgoingSocket.send(sendPacket);
        outgoingSocket.close();
    }

    private void initiateUnicastSend(final Member other, final String msg) {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(new Random().nextInt(1000));
                    unicastSend(other, msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };

        t.start();
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String serializeToJson(String message) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("message", message);
            jsonObj.put("timestamp", this.timestamp);
            jsonObj.put("sender", this.identifier);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj.toString();
    }

    @Override
    public void run() {
        String[] messages = {"Hello", "How are you", "Have a good day", "Nice talking to you"};
        for (int i = 0; i < messages.length; i++) {
            messages[i] += ", I'm member " + this.identifier;
        }

        // send messages
        Random rand = new Random();
        while (true) {
            String message = messages[rand.nextInt(messages.length)];

            synchronized (this.timestamp) {
                // increment timestamp
                this.timestamp.increment();
            }

            String serializedMessage = serializeToJson(message);
            multicast(serializedMessage);

            sleep(rand.nextInt(1500) + 1000);
        }
    }
}
