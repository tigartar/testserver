/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.concurrency;

import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.concurrency.Pollable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericPollerWithList<V>
implements Callable {
    private static Logger logger = Logger.getLogger(GenericPollerWithList.class.getName());
    private int iFirstID;
    private int iLastID;
    private List<? extends Pollable> iTaskList;

    public GenericPollerWithList(int aFirstID, int aLastID, List<? extends Pollable> aTaskList) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.entering(GenericPollerWithList.class.getName(), "GenericPollerWithList()", new Object[]{aFirstID, aLastID, aTaskList});
        }
        if (aTaskList == null) {
            throw new IllegalArgumentException("GenericPollerWithList TaskList argument must not be null");
        }
        this.iTaskList = aTaskList;
        this.iFirstID = aFirstID < 0 ? 0 : aFirstID;
        this.iLastID = aLastID < this.iFirstID ? this.iFirstID : (aLastID > aTaskList.size() ? aTaskList.size() : aLastID);
    }

    public Long call() throws Exception {
        if (logger.isLoggable(Level.FINEST)) {
            logger.entering(GenericPollerWithList.class.getName(), "call()");
        }
        long start = System.nanoTime();
        System.out.println("TASK CALLED");
        for (int i = this.iFirstID; i < this.iLastID; ++i) {
            Pollable lTask = this.iTaskList.get(i);
            if (lTask != null && lTask instanceof Bank) {
                ((Bank)lTask).poll(System.currentTimeMillis());
                continue;
            }
            logger.log(Level.WARNING, "Unsupported Pollable Class: " + lTask);
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Tasks from " + this.iFirstID + " to " + this.iLastID + " took " + (float)(System.nanoTime() - start) / 1000000.0f + "ms");
        }
        return System.nanoTime() - start;
    }

    public String toString() {
        return "GenericPollerWithList + First ID: " + this.iFirstID + ", Last ID: " + this.iLastID + ", Number of Tasks: " + this.iTaskList.size();
    }
}

