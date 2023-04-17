/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootItem;
import com.wurmonline.server.loot.LootItemFunc;
import com.wurmonline.server.loot.LootPool;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class PercentChanceLootItemFunc
implements LootItemFunc {
    protected static final Logger logger = Logger.getLogger(PercentChanceLootItemFunc.class.getName());

    @Override
    public Optional<LootItem> item(Creature victim, Creature receiver, LootPool pool) {
        List<LootItem> lootItems = pool.getLootItems();
        lootItems.sort(Comparator.comparingDouble(LootItem::getItemChance));
        double r = pool.getRandom().nextDouble();
        return lootItems.stream().filter(i -> {
            boolean success;
            boolean bl = success = r < i.getItemChance();
            if (!success) {
                logger.info(receiver.getName() + " failed loot roll for " + i.getItemName() + ": " + r + " not less than " + i.getItemChance());
            }
            return success;
        }).findFirst();
    }
}

