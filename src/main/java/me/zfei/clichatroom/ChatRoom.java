package me.zfei.clichatroom;

import me.zfei.clichatroom.models.Member;
import me.zfei.clichatroom.models.Sequencer;
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
    public static int NUM_MEMBERS = 2;
    public static boolean USE_VECTOR_TIMESTAMP = true;
    public static MulticastType MULTICAST_TYPE = MulticastType.RELIABLE_TOTAL_ORDERING;
    public static double DROP_RATE = 0;

    public static void main(String[] args) throws SocketException {
        ArrayList<Member> members = new ArrayList<Member>();

        boolean isTo = MULTICAST_TYPE == MulticastType.RELIABLE_TOTAL_ORDERING;

        int i;
        for (i = 0; i < NUM_MEMBERS; i++) {
            Member newMember = new Member(i, USE_VECTOR_TIMESTAMP && !isTo ? new VectorTimeStamp(i, NUM_MEMBERS) : new LamportTimeStamp());
            newMember.startListener();
            members.add(newMember);
        }

        if (isTo) {
            Sequencer sequencer = new Sequencer(i, new LamportTimeStamp());
            members.add(sequencer);
        }

        for (Member m : members) {
            m.setMembers(members);

            if (!(m instanceof Sequencer)) {
                m.start();
            }
        }
    }
}

