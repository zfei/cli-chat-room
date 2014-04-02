package me.zfei.clichatroom.utils;

/**
 * Created by zfei on 3/31/14.
 */
public class LamportTimeStamp implements TimeStamp {

    private int tsValue;

    public LamportTimeStamp() {
        this.tsValue = 0;
    }

    public LamportTimeStamp(int tsValue) {
        this.tsValue = tsValue;
    }

    public LamportTimeStamp(String tsString) {
        this.tsValue = Integer.valueOf(tsValue);
    }

    public int getTsValue() {
        return this.tsValue;
    }

    @Override
    public String toString() {
        return String.valueOf(this.tsValue);
    }

    @Override
    public synchronized TimeStamp increment() {
        this.tsValue++;
        return this;
    }

    @Override
    public synchronized TimeStamp increment(TimeStamp ts) {
        combine(ts);
        this.tsValue++;
        return this;
    }

    @Override
    public synchronized TimeStamp increment(String tsString) {
        increment(new LamportTimeStamp(tsString));
        return this;
    }

    @Override
    public synchronized TimeStamp combine(TimeStamp ts) {
        this.tsValue = Math.max(this.tsValue, ((LamportTimeStamp) ts).getTsValue());
        return this;
    }

    @Override
    public synchronized TimeStamp combine(String tsString) {
        combine(new LamportTimeStamp(tsString));
        return this;
    }
}
