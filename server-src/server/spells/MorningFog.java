package com.wurmonline.server.spells;

public final class MorningFog extends CreatureEnchantment {
   public static final int RANGE = 4;

   MorningFog() {
      super("Morning Fog", 282, 10, 5, 10, 7, 0L);
      this.targetCreature = true;
      this.enchantment = 19;
      this.effectdesc = "protection from thorns and lava.";
      this.description = "protection from thorns and lava";
      this.type = 2;
   }
}
