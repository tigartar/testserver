/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

public class ClosestVillage {
    private final String name;
    private final short distance;

    ClosestVillage(String name, short distance) {
        this.name = name;
        this.distance = distance;
    }

    public String getName() {
        return this.name;
    }

    public short getDistance() {
        return this.distance;
    }
}

