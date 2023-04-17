/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.players.Player;

public abstract class JournalReward {
    private final String rewardDescription;

    public abstract void runReward(Player var1);

    public JournalReward(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public String getRewardDesc() {
        return this.rewardDescription;
    }
}

