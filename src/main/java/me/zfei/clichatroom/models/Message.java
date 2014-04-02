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

    public Message(String serializedMessage) {
        this.serializedMessage = serializedMessage;

        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(serializedMessage);
            message = jsonObj.getString("message");
            tsString = jsonObj.getString("timestamp");
            senderId = jsonObj.getInt("sender");
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
}
