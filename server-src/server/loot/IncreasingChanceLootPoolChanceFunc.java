/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootPool;
import com.wurmonline.server.loot.LootPoolChanceFunc;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class IncreasingChanceLootPoolChanceFunc
implements LootPoolChanceFunc {
    protected static final Logger logger = Logger.getLogger(IncreasingChanceLootPoolChanceFunc.class.getName());
    private ConcurrentHashMap<Long, Double> progressiveChances = new ConcurrentHashMap();
    private double percentIncrease = 0.001f;

    public IncreasingChanceLootPoolChanceFunc setPercentIncrease(double increase) {
        this.percentIncrease = increase;
        return this;
    }

    @Override
    public boolean chance(Creature victim, Creature receiver, LootPool pool) {
        boolean success;
        double r = pool.getRandom().nextDouble();
        double bonus = Optional.ofNullable(this.progressiveChances.get(receiver.getWurmId())).orElse(0.0);
        boolean bl = success = r < pool.getLootPoolChance() + bonus;
        if (!success) {
            this.progressiveChances.put(receiver.getWurmId(), bonus + this.percentIncrease);
            logger.info(receiver.getName() + " failed loot pool chance for " + pool.getName() + ". Increasing chance by " + this.percentIncrease + " to " + (pool.getLootPoolChance() + this.progressiveChances.get(receiver.getWurmId())));
        } else {
            this.progressiveChances.remove(receiver.getWurmId());
            logger.info(receiver.getName() + " succeeded loot pool chance for " + pool.getName() + ". Clearing increases.");
        }
        return success;
    }
}

