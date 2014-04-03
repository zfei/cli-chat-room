package me.zfei.clichatroom.models;

import me.zfei.clichatroom.utils.TimeStamp;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;

/**
 * Created by zfei on 4/2/14.
 */
public class Sequencer extends Member{

    private int sequenceNum;
    private HashSet<String> receivedMessages;

    public Sequencer(int identifier, TimeStamp initialTimestamp) {
        super("localhost", identifier, initialTimestamp);

        this.sequenceNum = 0;
        this.receivedMessages = new HashSet<String>();
    }

    @Override
    public void startListener() {
        Thread t = new Thread() {

            @Override
            public void run() {
                DatagramSocket serverSocket = null;
                try {
                    serverSocket = new DatagramSocket(networker.getPort());
                } catch (SocketException e) {
                    e.printStackTrace();
                    return;
                }

                while (true) {
                    final DatagramPacket receivedPacket = networker.unicastReceive(serverSocket);

                    // deliver packet
                    Thread handler = new Thread() {

                        @Override
                        public void run() {
                            onReceiveBySequencer(receivedPacket);
                        }

                    };

                    handler.start();
                }
            }

        };

        t.start();
    }

    private synchronized void onReceiveBySequencer(DatagramPacket receivedPacket) {
        // unpack received packet
        String serializedMessage = Message.decodePacket(receivedPacket);
        Message msgObj = new Message(serializedMessage);
        if (msgObj.isAck()) {
            System.out.println("SEQUENCER RECEIVED ACK");
            this.networker.processAck(msgObj);
            return;
        }

        if (this.receivedMessages.contains(serializedMessage)) {
            return;
        }

        this.receivedMessages.add(serializedMessage);

        JSONObject orderObj = new JSONObject();
        try {
            orderObj.put("order", true);
            orderObj.put("digest", Message.genDigest(serializedMessage));
            orderObj.put("sequence", this.sequenceNum);
            orderObj.put("sender", this.identifier);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.networker.basicMulticast(this.members, this, orderObj.toString(), false);

        this.sequenceNum ++;
    }


}
