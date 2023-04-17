/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import java.util.Date;
import java.util.logging.Logger;

public class Rift
implements TimeConstants,
MiscConstants {
    private static final Logger logger = Logger.getLogger(Rift.class.getName());

    public Rift(TilePos center, float size) {
    }

    public TilePos getCenterPos() {
        return new TilePos();
    }

    public float getSize() {
        return 0.0f;
    }

    public void setSize(float size) {
    }

    public int getWave() {
        return 0;
    }

    public void setWave(int wave) {
    }

    public float getPercentWaveCompletion() {
        return 100.0f;
    }

    public void setPercentWaveCompletion(float percent, boolean save) {
    }

    private static final void createTable() {
    }

    public final void save(boolean create) {
    }

    public final void saveActivated() {
    }

    public final void saveEnded() {
    }

    public static final void loadRifts() {
    }

    public int getNumber() {
        return 0;
    }

    public void setNumber(int number) {
    }

    public int getState() {
        return 0;
    }

    public void setState(int state) {
    }

    public boolean isActive() {
        return false;
    }

    public void setActive(boolean active) {
    }

    public Date getInitiated() {
        return new Date(System.currentTimeMillis());
    }

    public void setInitiated(Date initiated, boolean initiate) {
    }

    public Date getActivated() {
        return this.getInitiated();
    }

    public void setActivated(Date activated, boolean saveNow) {
    }

    public Date getEnded() {
        return this.getInitiated();
    }

    public void setEnded(Date ended, boolean saveNow) {
    }

    public final void poll() {
    }

    public final void activateWave() {
    }

    private final void spawnRiftItems() {
    }

    private final void spawnTraps() {
    }

    private static final int getRandomTrapType() {
        return 0;
    }

    private final void spawnCreatures() {
    }

    public static final boolean waterFound(int tilex, int tiley) {
        return false;
    }

    public String getDescription() {
        return "";
    }

    public void setDescription(String description) {
    }

    public static Rift getActiveRift() {
        return null;
    }

    public static void setActiveRift(Rift activeRift) {
    }

    public byte getType() {
        return 0;
    }

    public void setType(byte type) {
    }

    public static Rift getLastRift() {
        return null;
    }

    public static void setLastRift(Rift lastRift) {
    }
}

