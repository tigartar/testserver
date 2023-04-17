/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.villages.Village;

public abstract class VillageWar {
    final Village villone;
    public final Village villtwo;

    VillageWar(Village vone, Village vtwo) {
        this.villone = vone;
        this.villtwo = vtwo;
    }

    public final Village getVillone() {
        return this.villone;
    }

    public final Village getVilltwo() {
        return this.villtwo;
    }

    abstract void save();

    abstract void delete();

    public final String toString() {
        return "VillageWar [" + this.villone + " and " + this.villtwo + ']';
    }
}

