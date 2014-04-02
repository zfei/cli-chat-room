package me.zfei.clichatroom.models;

import me.zfei.clichatroom.ChatRoom;
import me.zfei.clichatroom.utils.Networker;
import me.zfei.clichatroom.utils.TimeStamp;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zfei on 3/31/14.
 */
public class Member extends Thread {

    protected int identifier;
    protected int port;

    protected Networker networker;

    protected ArrayList<Member> members;
    private MemberListener listener;

    public Member(int identifier, TimeStamp initialTimestamp) {
        this.identifier = identifier;
        this.port = ChatRoom.PORT_BASE + identifier;

        this.networker = new Networker(this.port, initialTimestamp);
    }

    public void startListener() {
        // start listening to incoming connections
        this.listener = new MemberListener(this);
        this.listener.start();
    }


    public int getIdentifier() {
        return identifier;
    }

    public int getPort() {
        return this.port;
    }

    public TimeStamp getTimestamp() {
        return this.networker.getTimestamp();
    }

    public Networker getNetworker() {
        return networker;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void addMember(Member newMember) {
        this.members.add(newMember);
    }


    private void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

            this.networker.multicast(this.members, this, message);

            sleep(rand.nextInt(1500) + 1000);
        }
    }
}
