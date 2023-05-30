package com.wurmonline.server.spells;

public final class RottingTouch extends ItemEnchantment {
   public static final int RANGE = 4;

   RottingTouch() {
      super("Rotting Touch", 281, 20, 40, 60, 33, 0L);
      this.targetWeapon = true;
      this.enchantment = 18;
      this.effectdesc = "will hurt more, but be more brittle.";
      this.description = "causes extra damage on wounds, but takes more damage";
      this.type = 2;
   }
}
