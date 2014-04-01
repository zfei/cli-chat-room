package me.zfei.clichatroom.utils;

/**
 * Created by zfei on 3/31/14.
 */
public interface TimeStamp {
    void increment();
    void increment(TimeStamp ts);
    void increment(String tsString);
}
