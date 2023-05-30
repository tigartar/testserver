package com.wurmonline.server.spells;

public final class Toxin extends ItemEnchantment {
   public static final int RANGE = 4;

   Toxin() {
      super("Toxin", 259, 30, 40, 20, 45, 0L);
      this.targetJewelry = true;
      this.enchantment = 1;
      this.effectdesc = "will increase any poison damage you cause.";
      this.description = "increases any poison damage you cause";
      this.type = 2;
   }
}
