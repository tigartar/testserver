package com.wurmonline.server.spells;

abstract class ReligiousSpell extends Spell {
   ReligiousSpell(String aName, int aNum, int aCastingTime, int aCost, int aDifficulty, int aLevel, long cooldown) {
      super(aName, aNum, aCastingTime, aCost, aDifficulty, aLevel, cooldown, true);
   }
}
