package me.zfei.clichatroom.models;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;

/**
 * Created by zfei on 4/2/14.
 */
public class Message {

    private String message;
    private String tsString;
    private int senderId;
    private String serializedMessage;

    private String digest;
    private boolean order;
    private int sequence;

    private boolean ack;

    public Message(DatagramPacket packet) {
        initFromString(decodePacket(packet));
    }

    public Message(String serializedMessage) {
        initFromString(serializedMessage);
    }

    public static String decodePacket(DatagramPacket packet) {
        String serializedMessage = "";
        try {
            serializedMessage = new String(packet.getData(), "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serializedMessage;
    }

    public static String genDigest(String str) {
        return Hashing.sha256().hashString(str, Charsets.UTF_8).toString();
    }

    private void initFromString(String serializedMessage) {
        this.serializedMessage = serializedMessage;

        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(serializedMessage);

            this.order = false;
            this.ack = false;
            this.senderId = jsonObj.getInt("sender");
            if (jsonObj.has("ack")) {
                this.ack = true;
                this.message = jsonObj.getString("message");
            } else if (jsonObj.has("order")) {
                this.order = true;
                this.digest = jsonObj.getString("digest");
                this.sequence = jsonObj.getInt("sequence");
            } else {
                this.message = jsonObj.getString("message");
                this.tsString = jsonObj.getString("timestamp");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public String getTsString() {
        return tsString;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getSerializedMessage() {
        return serializedMessage;
    }

    public String getDigest() {
        return digest;
    }

    public boolean isOrder() {
        return order;
    }

    public boolean isAck() {
        return ack;
    }

    public int getSequence() {
        return sequence;
    }
}
