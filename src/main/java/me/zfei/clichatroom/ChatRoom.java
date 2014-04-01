package me.zfei.clichatroom;

import me.zfei.clichatroom.utils.LamportTimeStamp;

import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by zfei on 3/31/14.
 */
public class ChatRoom {
    public static void main(String[] args) throws SocketException {
        ArrayList<Member> members = new ArrayList<Member>();
        members.add(new Member(0, new LamportTimeStamp()));
        members.add(new Member(1, new LamportTimeStamp()));
        members.add(new Member(2, new LamportTimeStamp()));

        for (Member m : members) {
            m.setMembers(members);
            m.start();
        }
    }
}
