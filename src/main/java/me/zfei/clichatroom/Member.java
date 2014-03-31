package me.zfei.clichatroom;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zfei on 3/31/14.
 */
public class Member implements Runnable {

    public static int PORT_BASE = 5000;

    private int identifer;
    private int port;

    private ArrayList<Member> members;
    private MemberListener listener;

    private Socket[] sockets;

    public Member(int identifer) {
        this.identifer = identifer;
        this.port = PORT_BASE + identifer;

        // start listening to incoming connections
        this.listener = new MemberListener(this);
        this.listener.run();
    }

    public int getIdentifer() {
        return identifer;
    }

    public int getPort() {
        return port;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
        this.sockets = new Socket[members.size()];

        // setup connections with all other members
        for (int i = 0; i < this.members.size(); i++)
            if (i != this.identifer)
                while (!initSocketWith(members.get(i)))
                    sleep(500);
    }

    public void addMember(Member newMember) {
        this.members.add(newMember);
        while (!initSocketWith(newMember))
            sleep(500);
    }

    private void multicast(String msg) {
        Random rand = new Random();
        for (Member m : this.members) {
            sleep(rand.nextInt(1000));
            sendTo(m, msg);
        }
    }

    private void sendTo(Member other, String msg) {

    }

    private boolean initSocketWith(Member other) {
        try {
            this.sockets[other.getIdentifer()] = new Socket("localhost", other.getPort());
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void sleep(int milliseconds) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        // randomly send messages
        while (true) {
            sleep(10000);
            multicast("hello");
        }
    }
}
