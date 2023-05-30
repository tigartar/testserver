package com.wurmonline.server.spells;

public class Bearpaw extends CreatureEnchantment {
   public static final int RANGE = 40;

   public Bearpaw() {
      super("Bearpaws", 406, 10, 20, 29, 35, 0L);
      this.enchantment = 24;
      this.effectdesc = "more effective weaponless fighting and increased unarmed damage.";
      this.description = "increases effective weaponless fighting skill and unarmed damage";
      this.type = 2;
   }
}
