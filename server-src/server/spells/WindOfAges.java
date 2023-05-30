package com.wurmonline.server.spells;

public final class WindOfAges extends ItemEnchantment {
   public static final int RANGE = 4;

   WindOfAges() {
      super("Wind of Ages", 279, 20, 50, 60, 50, 0L);
      this.targetItem = true;
      this.enchantment = 16;
      this.effectdesc = "will be quicker to use.";
      this.description = "increases usage speed";
      this.type = 1;
   }
}
