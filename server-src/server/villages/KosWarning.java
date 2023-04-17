/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.villages.Village;

public class KosWarning {
    public final long playerId;
    public final int newReputation;
    private int ticks = 0;
    public final Village village;
    public final boolean permanent;

    public KosWarning(long pid, int newRep, Village vill, boolean perma) {
        this.playerId = pid;
        this.newReputation = newRep;
        this.village = vill;
        this.permanent = perma;
    }

    public final int getTick() {
        return this.ticks;
    }

    public final int tick() {
        return ++this.ticks;
    }
}

