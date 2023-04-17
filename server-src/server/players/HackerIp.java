/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

public final class HackerIp {
    public int timesFailed = 0;
    public long mayTryAgain = 0L;
    public String name;

    public HackerIp(int _timesFailed, long _mayTryAgain, String _name) {
        this.timesFailed = _timesFailed;
        this.mayTryAgain = _mayTryAgain;
        this.name = _name;
    }

    public int getTimesFailed() {
        return this.timesFailed;
    }

    public void incrementTimesFailed() {
        ++this.timesFailed;
    }

    public long getMayTryAgain() {
        return this.mayTryAgain;
    }

    public void setMayTryAgain(long aMayTryAgain) {
        this.mayTryAgain = aMayTryAgain;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String aName) {
        this.name = aName;
    }
}

