/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.statistics;

import com.wurmonline.server.statistics.ChallengeScore;

public class ScoreNamePair
implements Comparable<ScoreNamePair> {
    public final String name;
    public final ChallengeScore score;

    public ScoreNamePair(String owner, ChallengeScore score) {
        this.name = owner;
        this.score = score;
    }

    @Override
    public int compareTo(ScoreNamePair namePair) {
        if (this.score.getPoints() > namePair.score.getPoints()) {
            return -1;
        }
        if (this.name.toLowerCase().equals(namePair.name.toLowerCase()) && this.score.getPoints() == namePair.score.getPoints()) {
            return 0;
        }
        return 1;
    }
}

