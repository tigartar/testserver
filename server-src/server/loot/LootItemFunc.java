package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.Optional;

public interface LootItemFunc {
   Optional<LootItem> item(Creature var1, Creature var2, LootPool var3);
}
