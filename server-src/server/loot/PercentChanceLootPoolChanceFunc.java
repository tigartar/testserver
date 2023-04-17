/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootPool;
import com.wurmonline.server.loot.LootPoolChanceFunc;
import java.util.logging.Logger;

public class PercentChanceLootPoolChanceFunc
implements LootPoolChanceFunc {
    protected static final Logger logger = Logger.getLogger(PercentChanceLootPoolChanceFunc.class.getName());

    @Override
    public boolean chance(Creature victim, Creature receiver, LootPool pool) {
        boolean success;
        double r = pool.getRandom().nextDouble();
        boolean bl = success = r < pool.getLootPoolChance();
        if (!success) {
            logger.info(receiver.getName() + " failed loot pool chance for " + pool.getName() + ": " + r + " not less than " + pool.getLootPoolChance());
        }
        return success;
    }
}

