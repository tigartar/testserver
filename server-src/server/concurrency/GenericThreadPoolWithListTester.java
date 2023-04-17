/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.concurrency;

import com.wurmonline.server.concurrency.GenericThreadPoolWithList;
import com.wurmonline.server.concurrency.Pollable;
import java.util.ArrayList;

public class GenericThreadPoolWithListTester {
    public static void main(String[] args) {
        try {
            Thread.sleep(1000L);
        }
        catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        long lLastID = 10000L;
        ArrayList<1> lInputList = new ArrayList<1>(10010);
        for (long j = 0L; j < 10000L; ++j) {
            lInputList.add(new Pollable(){

                @Override
                public void poll(long now) {
                    Thread.yield();
                }
            });
        }
        for (int lNumberOfTasks = 1; lNumberOfTasks < 50; ++lNumberOfTasks) {
            GenericThreadPoolWithList.multiThreadedPoll(lInputList, lNumberOfTasks);
        }
    }
}

