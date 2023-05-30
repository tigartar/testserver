package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.Optional;

public interface ItemCreateFunc {
   Optional<Item> create(Creature var1, Creature var2, LootItem var3);
}
