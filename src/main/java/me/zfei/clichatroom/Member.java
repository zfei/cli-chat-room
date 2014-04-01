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

    public static int PORT_BASE = 60000;

    private int identifier;
    private int port;

    private TimeStamp timestamp;

    private ArrayList<Member> members;
    private MemberListener listener;

    public Member(int identifier, TimeStamp initialTimestamp) throws SocketException {
        this.identifier = identifier;
        this.port = PORT_BASE + identifier;

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

    private void multicast(String msg) {
        this.timestamp.increment();

        for (Member m : this.members) {
            if (m == this) {
                continue;
            }

            sendTo(m, msg);
        }
    }

    public void unicastSend(Member other, String msg) throws IOException {
        DatagramSocket outgoingSocket = null;
        outgoingSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData;
        sendData = msg.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, other.getPort());
        outgoingSocket.send(sendPacket);
        outgoingSocket.close();
    }

    private void sendTo(final Member other, final String msg) {
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
            // increment timestamp
            this.timestamp.increment();
            String serializedMessage = serializeToJson(message);
            multicast(serializedMessage);
            sleep(rand.nextInt(1500) + 1000);
        }
    }
}
