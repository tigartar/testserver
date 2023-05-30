package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import java.util.Optional;

public class RandomIndexLootItemFunc implements LootItemFunc {
   @Override
   public Optional<LootItem> item(Creature victim, Creature receiver, LootPool pool) {
      return Optional.ofNullable(pool.getLootItems().get(pool.getRandom().nextInt(pool.getLootItems().size())));
   }
}
