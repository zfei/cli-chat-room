package me.zfei.clichatroom.utils;

/**
 * Created by zfei on 3/31/14.
 */
public interface TimeStamp {
    TimeStamp increment();
    TimeStamp increment(TimeStamp ts);
    TimeStamp increment(String tsString);

    TimeStamp combine(TimeStamp ts);
    TimeStamp combine(String tsString);
}
