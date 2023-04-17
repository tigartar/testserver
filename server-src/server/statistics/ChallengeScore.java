/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.statistics;

public class ChallengeScore {
    private final int type;
    private float points;
    private float lastPoints;
    private long lastUpdated;

    public ChallengeScore(int scoreType, float numPoints, long aLastUpdated, float aLastPoints) {
        this.type = scoreType;
        this.setPoints(numPoints);
        this.setLastPoints(aLastPoints);
        this.setLastUpdated(aLastUpdated);
    }

    public int getType() {
        return this.type;
    }

    public float getPoints() {
        return this.points;
    }

    public void setPoints(float aPoints) {
        this.points = aPoints;
    }

    public long getLastUpdated() {
        return this.lastUpdated;
    }

    public void setLastUpdated(long aLastUpdated) {
        this.lastUpdated = aLastUpdated;
    }

    public float getLastPoints() {
        return this.lastPoints;
    }

    public void setLastPoints(float aLastPoints) {
        this.lastPoints = aLastPoints;
    }
}

