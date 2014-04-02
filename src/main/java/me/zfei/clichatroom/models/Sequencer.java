package me.zfei.clichatroom.models;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import me.zfei.clichatroom.utils.TimeStamp;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zfei on 4/2/14.
 */
public class Sequencer extends Member{

    private int sequenceNum;

    public Sequencer(int identifier, TimeStamp initialTimestamp) {
        super(identifier, initialTimestamp);

        this.sequenceNum = 0;

        // start daemon thread that listens to incoming messages
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
        String serializedMessage = "";
        try {
            serializedMessage = new String(receivedPacket.getData(), "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject orderObj = new JSONObject();
        try {
            orderObj.put("order", true);
            orderObj.put("digest", Hashing.sha256().hashString(serializedMessage, Charsets.UTF_8).toString());
            orderObj.put("sequence", this.sequenceNum);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.networker.basicMulticast(this.members, this, orderObj.toString(), false);

        this.sequenceNum ++;
    }


}
