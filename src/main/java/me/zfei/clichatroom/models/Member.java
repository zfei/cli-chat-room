package me.zfei.clichatroom.models;

import me.zfei.clichatroom.ChatRoom;
import me.zfei.clichatroom.utils.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by zfei on 3/31/14.
 */
public class Member extends Thread {

    protected InetAddress ip;
    protected int identifier;
    protected int port;

    protected Networker networker;

    protected ArrayList<Member> members;
    private MemberListener listener;

    public Member(String ip, int identifier, TimeStamp initialTimestamp) {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.identifier = identifier;
        this.port = ChatRoom.PORT_BASE + identifier;

        this.networker = new Networker(this, initialTimestamp);
    }

    public void startListener() {
        System.out.format("LISTENER STARTED ON %s AT %d\n", this.getIp(), this.getPort());

        // start listening to incoming connections
        this.listener = new MemberListener(this);
        this.listener.start();
    }

    public InetAddress getIp() {
        return ip;
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

    public InetAddress getMemberIp(int memberId) {
        for (Member m : this.getMembers()) {
            if (m.getIdentifier() == memberId) {
                return m.getIp();
            }
        }

        return null;
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

            sleep(rand.nextInt(ChatRoom.NON_INT_DELAY_DEV) + ChatRoom.NON_INT_INTERVAL);
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        if (args.length != 2) {
            System.out.println("Usage: java -jar Member.jar CONFIG_FILE IDENTIFIER");
            System.exit(1);
        }

        String configPath = args[0];
        JSONObject configJson = ChatRoom.readConfig(configPath);
        int id = Integer.parseInt(args[1]);

        ArrayList<Member> members = new ArrayList<Member>();
        JSONArray membersArr = configJson.getJSONArray("members");

        Member me = null;
        for (int i = 0; i < membersArr.length(); i++) {
            JSONObject memberObj = (JSONObject) membersArr.get(i);
            TimeStamp ts = ChatRoom.USE_VECTOR_TIMESTAMP ? new VectorTimeStamp(id, ChatRoom.NUM_MEMBERS) : new LamportTimeStamp();
            Member newMember = new Member(memberObj.getString("ip"), memberObj.getInt("identifier"), ts);
//            newMember.startListener();
            members.add(newMember);

            if (i == id) {
                me = newMember;
            }
        }

        boolean isTo = ChatRoom.MULTICAST_TYPE == MulticastType.RELIABLE_TOTAL_ORDERING;
        if (isTo) {
            Sequencer sequencer = new Sequencer(-1, new LamportTimeStamp());
            members.add(sequencer);
        }

        me.setMembers(members);

        me.startListener();

        Scanner sc = new Scanner(System.in,"UTF-8");
        while(sc.hasNext()) {
            String next = sc.nextLine();
            me.networker.multicast(me.members, me, next);
        }
    }
}
