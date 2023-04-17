/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootItem;
import com.wurmonline.server.loot.LootItemFunc;
import com.wurmonline.server.loot.LootPool;
import java.util.Optional;

public class RandomIndexLootItemFunc
implements LootItemFunc {
    @Override
    public Optional<LootItem> item(Creature victim, Creature receiver, LootPool pool) {
        return Optional.ofNullable(pool.getLootItems().get(pool.getRandom().nextInt(pool.getLootItems().size())));
    }
}

