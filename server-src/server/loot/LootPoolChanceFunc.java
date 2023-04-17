/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.LootPool;

public interface LootPoolChanceFunc {
    public boolean chance(Creature var1, Creature var2, LootPool var3);
}

