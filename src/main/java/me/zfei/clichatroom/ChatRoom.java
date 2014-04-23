package me.zfei.clichatroom;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import me.zfei.clichatroom.models.Member;
import me.zfei.clichatroom.models.Sequencer;
import me.zfei.clichatroom.utils.LamportTimeStamp;
import me.zfei.clichatroom.utils.MulticastType;
import me.zfei.clichatroom.utils.TimeStamp;
import me.zfei.clichatroom.utils.VectorTimeStamp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zfei on 3/31/14.
 */
public class ChatRoom {

    public static int PORT_BASE = 60000;
    public static int NUM_MEMBERS = 2;
    public static boolean USE_VECTOR_TIMESTAMP = true;
    public static MulticastType MULTICAST_TYPE = MulticastType.RELIABLE_TOTAL_ORDERING;
    public static double DROP_RATE = 0.8;
    public static int MEAN_DELAY = 50;
    public static int DELAY_DEVIATION = 50;
    public static int NON_INT_INTERVAL = 2000;
    public static int NON_INT_DELAY_DEV = 1000;
    public static boolean INTERACTIVE = false;

    public static void main(String[] args) throws IOException, JSONException {
        if (args.length != 1) {
            System.out.println("Usage: java -jar ChatRoom.jar CONFIG_FILE");
            System.exit(1);
        }

        JSONObject configJson = readConfig(args[0]);

        boolean isTo = MULTICAST_TYPE == MulticastType.RELIABLE_TOTAL_ORDERING;
        ArrayList<Member> members = new ArrayList<Member>();
        if (INTERACTIVE) {
            JSONArray membersArr = configJson.getJSONArray("members");
            for (int i = 0; i < membersArr.length(); i++) {
                JSONObject memberObj = (JSONObject) membersArr.get(i);
                TimeStamp ts = USE_VECTOR_TIMESTAMP ? new VectorTimeStamp(i, NUM_MEMBERS) : new LamportTimeStamp();
                Member newMember = new Member(memberObj.getString("ip"), memberObj.getInt("identifier"), ts);
//                newMember.startListener();
                members.add(newMember);
            }
        } else {
            int i;
            for (i = 0; i < NUM_MEMBERS; i++) {
                TimeStamp ts = USE_VECTOR_TIMESTAMP ? new VectorTimeStamp(i, NUM_MEMBERS) : new LamportTimeStamp();
                Member newMember = new Member("localhost", i, ts);
                newMember.startListener();
                members.add(newMember);
            }
        }

        if (isTo) {
            Sequencer sequencer = new Sequencer(-1, new LamportTimeStamp());
            sequencer.startListener();
            members.add(sequencer);
        }

        for (Member m : members) {
            m.setMembers(members);

            if (!INTERACTIVE && !(m instanceof Sequencer)) {
                m.start();
            }
        }
    }

    public static JSONObject readConfig() throws IOException, JSONException {
        return readConfig("config.json");
    }

    public static JSONObject readConfig(String path) throws IOException, JSONException {
//        URL url = Resources.getResource(path);
//        String configString = Resources.toString(url, Charsets.UTF_8);

        String configString = Files.toString(new File(path), Charsets.UTF_8);

        JSONObject configJson = new JSONObject(configString);

        if (configJson.has("vectorStamp")) {
            USE_VECTOR_TIMESTAMP = configJson.getBoolean("vectorStamp");
        }
        if (configJson.has("roomSize")) {
            NUM_MEMBERS = configJson.getInt("roomSize");
        }
        if (configJson.has("portBase")) {
            PORT_BASE = configJson.getInt("portBase");
        }
        if (configJson.has("multicastType")) {
            MULTICAST_TYPE = MulticastType.valueOf(configJson.getString("multicastType"));
        }
        if (configJson.has("meanDelay")) {
            MEAN_DELAY = configJson.getInt("meanDelay");
        }
        if (configJson.has("delayDeviation")) {
            DELAY_DEVIATION = configJson.getInt("delayDeviation");
        }
        if (configJson.has("dropRate")) {
            DROP_RATE = configJson.getDouble("dropRate");
        }
        if (configJson.has("nonInteractiveMsgInterval")) {
            NON_INT_INTERVAL = configJson.getInt("nonInteractiveMsgInterval");
        }
        if (configJson.has("interactive") && configJson.getBoolean("interactive")) {
            INTERACTIVE = true;
        }

        boolean isTo = MULTICAST_TYPE == MulticastType.RELIABLE_TOTAL_ORDERING;
        boolean isCo = MULTICAST_TYPE == MulticastType.RELIABLE_CAUSAL_ORDERING;
        USE_VECTOR_TIMESTAMP &= !isTo;
        USE_VECTOR_TIMESTAMP |= isCo;

        return configJson;
    }
}

