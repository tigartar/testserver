package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;

public class DamageSpell extends ReligiousSpell {
   DamageSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long aCooldown) {
      super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, aCooldown);
   }

   public double calculateDamage(Creature target, double power, double baseDamage, double damagePerPower) {
      double damage = power * damagePerPower;
      damage += baseDamage;
      double resistance = SpellResist.getSpellResistance(target, this.getNumber());
      damage *= resistance;
      SpellResist.addSpellResistance(target, this.getNumber(), damage);
      return Spell.modifyDamage(target, damage);
   }
}
