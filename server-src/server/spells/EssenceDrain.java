package com.wurmonline.server.spells;

public class EssenceDrain extends ItemEnchantment {
   public static final int RANGE = 4;

   EssenceDrain() {
      super("Essence Drain", 933, 20, 100, 60, 61, 0L);
      this.targetWeapon = true;
      this.enchantment = 63;
      this.effectdesc = "will cause extra internal wounds and heal you.";
      this.description = "causes extra internal wounds and heals the wielder slightly";
      this.type = 1;
   }
}
