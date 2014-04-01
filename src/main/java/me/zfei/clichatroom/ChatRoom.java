package me.zfei.clichatroom;

import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by zfei on 3/31/14.
 */
public class ChatRoom {
    public static void main(String[] args) throws SocketException {
        ArrayList<Member> members = new ArrayList<Member>();
        members.add(new Member(0));
        members.add(new Member(1));
        members.add(new Member(2));

        for (Member m : members) {
            m.setMembers(members);
            m.start();
        }
    }
}
