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
    public synchronized void increment() {
        this.tsValue++;
    }

    @Override
    public synchronized void increment(TimeStamp ts) {
        this.tsValue = Math.max(this.tsValue, ((LamportTimeStamp) ts).getTsValue()) + 1;
    }

    @Override
    public synchronized void increment(String tsString) {
        increment(new LamportTimeStamp(tsString));
    }
}
