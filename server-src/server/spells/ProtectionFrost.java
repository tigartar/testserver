package com.wurmonline.server.spells;

public final class ProtectionFrost extends ItemEnchantment {
   public static final int RANGE = 4;

   ProtectionFrost() {
      super("Frost Protection", 264, 30, 30, 30, 30, 0L);
      this.targetJewelry = true;
      this.enchantment = 6;
      this.effectdesc = "will reduce any frost damage you take.";
      this.description = "reduces any frost damage you take";
      this.type = 1;
   }
}
