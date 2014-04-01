package me.zfei.clichatroom.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by zfei on 3/31/14.
 */
public class VectorTimeStamp implements TimeStamp {

    private int identifier;
    private int[] tsArray;

    public VectorTimeStamp(int identifier, int numMembers) {
        this.identifier = identifier;
        this.tsArray = new int[numMembers];
    }

    public VectorTimeStamp(String tsString) {
        try {
            JSONArray jsonArray = new JSONArray(tsString);

            this.tsArray = new int[jsonArray.length()];
            for (int i = 0; i < this.tsArray.length; i++) {
                tsArray[i] = jsonArray.getInt(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int[] getTsArray() {
        return this.tsArray;
    }

    @Override
    public String toString() {
        try {
            return new JSONArray(this.tsArray).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "[]";
    }

    @Override
    public synchronized void increment() {
        this.tsArray[this.identifier] ++;
    }

    @Override
    public synchronized void increment(TimeStamp ts) {
        for (int i = 0; i < this.tsArray.length; i++) {
            if (i == this.identifier) {
                this.tsArray[i] ++;
            } else {
                this.tsArray[i] = Math.max(this.tsArray[i], ((VectorTimeStamp) ts).getTsArray()[i]);
            }
        }
    }

    @Override
    public synchronized void increment(String tsString) {
        increment(new VectorTimeStamp(tsString));
    }
}
