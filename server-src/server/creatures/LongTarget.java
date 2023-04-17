/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.items.Item;

public class LongTarget
extends PathTile {
    private Creature ctarget;
    private Item itemTarget;
    private long target = -10L;
    private int epicMission = -1;
    private int missionTrigger = -1;
    private final int startx;
    private final int starty;
    private final long startTime;

    public LongTarget(int tx, int ty, int t, boolean surf, int aFloorLevel, Creature starter) {
        super(tx, ty, t, surf, aFloorLevel);
        this.startx = starter.getTileX();
        this.starty = starter.getTileY();
        this.startTime = System.currentTimeMillis();
    }

    public Creature getCreatureTarget() {
        return this.ctarget;
    }

    public void setCreaturetarget(Creature ctarget) {
        this.ctarget = ctarget;
    }

    public Item getItemTarget() {
        return this.itemTarget;
    }

    public void setItemTarget(Item itemTarget) {
        this.itemTarget = itemTarget;
    }

    public void setTileX(int tileX) {
        this.tilex = tileX;
    }

    public void setTileY(int tileY) {
        this.tiley = tileY;
    }

    public long getMissionTarget() {
        return this.target;
    }

    public void setMissionTarget(long target) {
        this.target = target;
    }

    public int getEpicMission() {
        return this.epicMission;
    }

    public void setEpicMission(int epicMission) {
        this.epicMission = epicMission;
    }

    public int getMissionTrigger() {
        return this.missionTrigger;
    }

    public void setMissionTrigger(int missionTrigger) {
        this.missionTrigger = missionTrigger;
    }

    public int getStartx() {
        return this.startx;
    }

    public int getStarty() {
        return this.starty;
    }

    public long getStartTime() {
        return this.startTime;
    }
}

