package me.zfei.clichatroom.utils;

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

    public int[] getTsArray() {
        return this.tsArray;
    }

    @Override
    public void increment() {
        this.tsArray[this.identifier] ++;
    }

    @Override
    public void increment(TimeStamp ts) {
        for (int i = 0; i < this.tsArray.length; i++) {
            if (i == this.identifier) {
                this.tsArray[i] ++;
            } else {
                this.tsArray[i] = Math.max(this.tsArray[i], ((VectorTimeStamp) ts).getTsArray()[i]);
            }
        }
    }
}
