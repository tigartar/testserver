package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.AttitudeConstants;

public class RottingGut extends DamageSpell implements AttitudeConstants {
   public static final int RANGE = 50;
   public static final double BASE_DAMAGE = 7000.0;
   public static final double DAMAGE_PER_POWER = 100.0;

   public RottingGut() {
      super("Rotting Gut", 428, 7, 10, 10, 35, 30000L);
      this.targetCreature = true;
      this.offensive = true;
      this.description = "damages the targets stomach with rotting acid";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && performer.faithful && !performer.isDuelOrSpar(target)) {
         performer.getCommunicator()
            .sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getName() + ".", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.isDuelOrSpar(target)) {
         performer.modifyFaith(-5.0F);
      }

      double damage = this.calculateDamage(target, power, 7000.0, 100.0);
      target.addWoundOfType(performer, (byte)10, 23, false, 1.0F, false, damage, (float)power, 0.0F, false, true);
   }
}
