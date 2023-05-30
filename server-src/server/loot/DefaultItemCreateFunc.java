package com.wurmonline.server.loot;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import java.util.Optional;

public class DefaultItemCreateFunc implements ItemCreateFunc {
   @Override
   public Optional<Item> create(Creature victim, Creature receiver, LootItem lootItem) {
      return ItemFactory.createItemOptional(lootItem.getItemTemplateId(), 50.0F + Server.rand.nextFloat() * 40.0F, victim.getName());
   }
}
