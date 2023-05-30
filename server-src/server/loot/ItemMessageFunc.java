package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;

public interface ItemMessageFunc {
   void message(Creature var1, Creature var2, Item var3);
}
