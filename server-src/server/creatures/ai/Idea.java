/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

public abstract class Idea {
    private int priority = 0;
    static final int MOVEMENTPRIO = 0;
    static final int PURCHASEPRIO = 1;
    static final int BUILDPRIO = 0;
    static final int ATTACKPRIO = 5;
    static final int DEFENDPRIO = 7;
    static final int RETREATPRIO = 8;
    static final int SCOUTPRIO = 3;

    public abstract boolean resolve();

    public final int getPriority() {
        return this.priority;
    }

    public final void setPriority(int aPriority) {
        this.priority = aPriority;
    }
}

