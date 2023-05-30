package com.wurmonline.server.spells;

public final class ProtectionPoison extends ItemEnchantment {
   public static final int RANGE = 4;

   ProtectionPoison() {
      super("Poison Protection", 266, 30, 30, 30, 27, 0L);
      this.targetJewelry = true;
      this.enchantment = 8;
      this.effectdesc = "will reduce any poison damage you take.";
      this.description = "reduces any poison damage you take";
      this.type = 1;
   }
}
