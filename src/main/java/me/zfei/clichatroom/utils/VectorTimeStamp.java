package me.zfei.clichatroom.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by zfei on 3/31/14.
 */
public class VectorTimeStamp implements TimeStamp {

    private int identifier;
    private int[] tsArray;

    public VectorTimeStamp(VectorTimeStamp vts) {
        this(vts.getIdentifier(), vts.getTsArray().length);
        this.initWithString(vts.toString());
    }

    public VectorTimeStamp(int identifier, int numMembers) {
        this.identifier = identifier;
        this.tsArray = new int[numMembers];
    }

    public VectorTimeStamp(String tsString) {
        initWithString(tsString);
    }

    public VectorTimeStamp initWithString(String tsString) {
        try {
            JSONArray jsonArray = new JSONArray(tsString);

            this.tsArray = new int[jsonArray.length()];
            for (int i = 0; i < this.tsArray.length; i++) {
                tsArray[i] = jsonArray.getInt(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public int getIdentifier() {
        return identifier;
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

    public synchronized void incrementAt(int i) {
        this.tsArray[i] ++;
    }

    @Override
    public synchronized TimeStamp increment() {
        incrementAt(this.identifier);
        return this;
    }

    @Override
    public synchronized TimeStamp increment(TimeStamp ts) {
        combine(ts);
        this.tsArray[this.identifier] ++;
        return this;
    }

    @Override
    public synchronized TimeStamp increment(String tsString) {
        increment(new VectorTimeStamp(tsString));
        return this;
    }

    @Override
    public synchronized TimeStamp combine(TimeStamp ts) {
        for (int i = 0; i < this.tsArray.length; i++) {
            if (i != this.identifier) {
                this.tsArray[i] = Math.max(this.tsArray[i], ((VectorTimeStamp) ts).getTsArray()[i]);
            }
        }
        return this;
    }

    @Override
    public synchronized TimeStamp combine(String tsString) {
        combine(new VectorTimeStamp(tsString));
        return this;
    }
}
