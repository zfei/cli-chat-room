package me.zfei.clichatroom;

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

    private void deliver(DatagramPacket receivedPacket) {
        String sentence = null;
        try {
            sentence = new String(receivedPacket.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.format("MEMBER %d RECEIVED %s\n", this.owner.getIdentifier(), sentence.trim());
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
