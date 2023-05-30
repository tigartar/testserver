package com.wurmonline.server.spells;

public final class ProtectionFire extends ItemEnchantment {
   public static final int RANGE = 4;

   ProtectionFire() {
      super("Fire Protection", 265, 30, 30, 30, 28, 0L);
      this.targetJewelry = true;
      this.enchantment = 7;
      this.effectdesc = "will reduce any fire damage you take.";
      this.description = "reduces any fire damage you take";
      this.type = 2;
   }
}
