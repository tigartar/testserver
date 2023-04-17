/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.Creature;
import java.util.HashMap;

public abstract class CreatureAIData {
    private Creature creature;
    private long lastPollTime = 0L;
    private boolean dropsCorpse = true;
    private float movementSpeedModifier = 1.0f;
    private float sizeModifier = 1.0f;
    private HashMap<Integer, Long> aiTimerMap = new HashMap();

    public void setTimer(int timer, long time) {
        if (!this.aiTimerMap.containsKey(timer)) {
            this.aiTimerMap.put(timer, time);
        } else {
            this.aiTimerMap.replace(timer, time);
        }
    }

    public long getTimer(int timer) {
        if (!this.aiTimerMap.containsKey(timer)) {
            this.setTimer(timer, 0L);
        }
        return this.aiTimerMap.get(timer);
    }

    public void setCreature(Creature c) {
        this.creature = c;
    }

    public Creature getCreature() {
        return this.creature;
    }

    public long getLastPollTime() {
        return this.lastPollTime;
    }

    public void setLastPollTime(long lastPollTime) {
        this.lastPollTime = lastPollTime;
    }

    public boolean doesDropCorpse() {
        return this.dropsCorpse;
    }

    public void setDropsCorpse(boolean dropsCorpse) {
        this.dropsCorpse = dropsCorpse;
    }

    public float getMovementSpeedModifier() {
        return this.movementSpeedModifier;
    }

    public void setMovementSpeedModifier(float movementModifier) {
        this.movementSpeedModifier = movementModifier;
    }

    public float getSpeed() {
        return this.creature.getTemplate().getSpeed();
    }

    public float getSizeModifier() {
        return this.sizeModifier;
    }

    public void setSizeModifier(float sizeModifier) {
        this.sizeModifier = sizeModifier;
    }
}

