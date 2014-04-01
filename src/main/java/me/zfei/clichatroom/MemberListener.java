package me.zfei.clichatroom;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by zfei on 3/31/14.
 */
public class MemberListener extends Thread {

    private Member owner;

    public MemberListener(Member owner) {
        this.owner = owner;
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

    private synchronized void deliver(DatagramPacket receivedPacket) {
        String serializedMessage = null;
        try {
            serializedMessage = new String(receivedPacket.getData(), "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(serializedMessage);
            String message = jsonObj.getString("message");
            String tsString = jsonObj.getString("timestamp");

            // increment timestamp
            this.owner.getTimestamp().increment(tsString);

            System.out.format("MEMBER %d RECEIVED %s AT %s\n", this.owner.getIdentifier(), message, tsString);
        } catch (JSONException e) {
            e.printStackTrace();
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

        while(true)
        {
            final DatagramPacket receivedPacket = unicastReceive(serverSocket);

            // deliver packet
            Thread t = new Thread() {

                @Override
                public void run() {
                    deliver(receivedPacket);
                }

            };

            t.start();
        }
    }
}
