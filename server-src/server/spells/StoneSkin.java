package com.wurmonline.server.spells;

public class StoneSkin extends KarmaEnchantment {
   public StoneSkin() {
      super("Stoneskin", 553, 20, 500, 20, 1, 240000L);
      this.targetCreature = true;
      this.enchantment = 68;
      this.effectdesc = "3 wounds ignored.";
      this.description = "makes you ignore 3 wounds";
      this.durationModifier = 100000.0F;
   }
}
