package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;

public interface LootPoolChanceFunc {
   boolean chance(Creature var1, Creature var2, LootPool var3);
}
