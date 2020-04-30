package com.uniproject.queuemanageronline.utils;

public class Counter {
    int numShards;

    public Counter() { }

    public Counter(int numShards) {
        this.numShards = numShards;
    }

    public int getNumShards() {
        return numShards;
    }

    public void setNumShards(int numShards) {
        this.numShards = numShards;
    }
}
