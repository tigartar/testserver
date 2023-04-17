/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.villages.Village;

public abstract class Alliance {
    final Village villone;
    final Village villtwo;

    Alliance(Village vone, Village vtwo) {
        this.villone = vone;
        this.villtwo = vtwo;
    }

    final Village getVillone() {
        return this.villone;
    }

    final Village getVilltwo() {
        return this.villtwo;
    }

    abstract void save();

    abstract void delete();

    public final String toString() {
        return "Alliance [" + this.villone + " and " + this.villtwo + ']';
    }
}

