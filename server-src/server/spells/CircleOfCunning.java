package com.wurmonline.server.spells;

public final class CircleOfCunning extends ItemEnchantment {
   public static final int RANGE = 4;

   CircleOfCunning() {
      super("Circle of Cunning", 276, 20, 50, 60, 51, 0L);
      this.targetItem = true;
      this.enchantment = 13;
      this.effectdesc = "will increase skill gained with it when used.";
      this.description = "increases skill gain";
      this.type = 1;
   }
}
