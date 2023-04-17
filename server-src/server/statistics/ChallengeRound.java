/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.statistics;

import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeScore;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeRound {
    private final int round;
    private final ConcurrentHashMap<Integer, ChallengeScore> privateScores = new ConcurrentHashMap();

    ChallengeRound(int roundval) {
        this.round = roundval;
    }

    protected final void setScore(ChallengeScore score) {
        this.privateScores.put(score.getType(), score);
    }

    protected final ChallengeScore getCurrentScoreForType(int type) {
        return this.privateScores.get(type);
    }

    protected final ChallengeScore[] getScores() {
        return this.privateScores.values().toArray(new ChallengeScore[this.privateScores.size()]);
    }

    public int getRound() {
        return this.round;
    }

    public final String getRoundName() {
        return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getName();
    }

    public final String getRoundDescription() {
        return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getDesc();
    }

    public final String getRoundIcon() {
        return ChallengePointEnum.ChallengeScenario.fromInt(this.round).getUrl();
    }

    public final boolean isCurrent() {
        return this.round == ChallengePointEnum.ChallengeScenario.current.getNum();
    }
}

