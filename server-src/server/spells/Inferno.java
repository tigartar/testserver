package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.AttitudeConstants;

public class Inferno extends DamageSpell implements AttitudeConstants {
   public static final int RANGE = 50;
   public static final double BASE_DAMAGE = 25000.0;
   public static final double DAMAGE_PER_POWER = 75.0;

   public Inferno() {
      super("Inferno", 931, 20, 40, 50, 65, 60000L);
      this.targetCreature = true;
      this.offensive = true;
      this.description = "damages the target with fire from a volcano";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && performer.faithful) {
         performer.getCommunicator()
            .sendNormalServerMessage(performer.getDeity().getName() + " would never accept your spell on " + target.getName() + ".", (byte)3);
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2) {
         performer.modifyFaith(-5.0F);
      }

      double damage = this.calculateDamage(target, power, 25000.0, 75.0);
      target.addWoundOfType(performer, (byte)4, 1, true, 1.0F, false, damage, 0.0F, 0.0F, false, true);
   }
}
