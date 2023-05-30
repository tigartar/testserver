package com.wurmonline.server.spells;

public class Nolocate extends ItemEnchantment {
   public static final int RANGE = 4;

   public Nolocate() {
      super("Nolocate", 451, 15, 60, 10, 22, 0L);
      this.targetJewelry = true;
      this.enchantment = 29;
      this.effectdesc = "protects from being located.";
      this.description = "hides you from locate spells";
      this.type = 0;
   }
}
