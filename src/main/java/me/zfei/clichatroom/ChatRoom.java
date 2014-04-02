package me.zfei.clichatroom;

import me.zfei.clichatroom.models.Member;
import me.zfei.clichatroom.utils.LamportTimeStamp;
import me.zfei.clichatroom.utils.MulticastType;
import me.zfei.clichatroom.utils.VectorTimeStamp;

import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by zfei on 3/31/14.
 */
public class ChatRoom {

    public static int PORT_BASE = 60000;
    public static int NUM_MEMBERS = 3;
    public static boolean USE_VECTOR_TIMESTAMP = true;
    public static MulticastType MULTICAST_TYPE = MulticastType.RELIABLE_CAUSAL_ORDERING;
    public static double DROP_RATE = 0;

    public static void main(String[] args) throws SocketException {
        ArrayList<Member> members = new ArrayList<Member>();
        for (int i = 0; i < NUM_MEMBERS; i++) {
            members.add(new Member(i, USE_VECTOR_TIMESTAMP? new VectorTimeStamp(i, NUM_MEMBERS): new LamportTimeStamp()));
        }

        for (Member m : members) {
            m.setMembers(members);
            m.start();
        }
    }
}

