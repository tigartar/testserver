package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;

public class DefaultActiveFunc implements ActiveFunc {
   @Override
   public boolean active(Creature victim, Creature receiver) {
      return true;
   }
}
