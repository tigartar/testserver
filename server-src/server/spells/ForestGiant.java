package com.wurmonline.server.spells;

public class ForestGiant extends CreatureEnchantment {
   public static final int RANGE = 4;

   ForestGiant() {
      super("Forest Giant Strength", 410, 10, 50, 49, 55, 0L);
      this.enchantment = 25;
      this.effectdesc = "increased body strength.";
      this.description = "increases body strength";
      this.type = 2;
   }
}
