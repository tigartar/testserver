/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.StructureSupport;
import com.wurmonline.server.zones.VolaTile;

public interface IFloor
extends StructureSupport {
    public float getDamageModifierForItem(Item var1);

    public long getStructureId();

    public VolaTile getTile();

    public boolean isOnPvPServer();

    public int getTileX();

    public int getTileY();

    public float getCurrentQualityLevel();

    public float getDamage();

    public boolean setDamage(float var1);

    public float getQualityLevel();

    public void destroyOrRevertToPlan();

    public boolean isAPlan();

    public boolean isThatch();

    public boolean isStone();

    public boolean isSandstone();

    public boolean isSlate();

    public boolean isMarble();

    public boolean isMetal();

    public boolean isWood();

    public boolean isFinished();

    public int getRepairItemTemplate();

    public boolean setQualityLevel(float var1);

    public void setLastUsed(long var1);

    public boolean isOnSurface();

    @Override
    public boolean equals(StructureSupport var1);
}

