package me.zfei.clichatroom.models;

import org.json.JSONException;
import org.json.JSONObject;

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

    public Message(String serializedMessage) {
        this.serializedMessage = serializedMessage;

        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(serializedMessage);

            this.order = false;
            if (jsonObj.has("order")) {
                this.order = true;
                this.digest = jsonObj.getString("digest");
                this.sequence = jsonObj.getInt("sequence");
            } else {
                this.message = jsonObj.getString("message");
                this.tsString = jsonObj.getString("timestamp");
                this.senderId = jsonObj.getInt("sender");
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

    public int getSequence() {
        return sequence;
    }
}
