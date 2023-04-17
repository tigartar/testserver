/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootItem;
import com.wurmonline.server.loot.LootPool;
import java.util.Optional;

public interface LootItemFunc {
    public Optional<LootItem> item(Creature var1, Creature var2, LootPool var3);
}

