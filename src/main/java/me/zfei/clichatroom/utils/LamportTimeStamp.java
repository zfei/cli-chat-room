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

    public int getTsValue() {
        return tsValue;
    }

    @Override
    public void increment() {
        this.tsValue ++;
    }

    @Override
    public void increment(TimeStamp ts) {
        this.tsValue = Math.max(this.tsValue, ((LamportTimeStamp) ts).getTsValue()) + 1;
    }
}
