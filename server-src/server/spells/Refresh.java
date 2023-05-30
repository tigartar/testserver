package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.constants.AttitudeConstants;

public final class Refresh extends ReligiousSpell implements AttitudeConstants {
   public static final int RANGE = 12;

   Refresh() {
      super("Refresh", 250, 9, 15, 20, 20, 180000L);
      this.targetCreature = true;
      this.description = "refreshes stamina of a single creature";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if (target.getAttitude(performer) != 2) {
         return true;
      } else {
         performer.getCommunicator()
            .sendNormalServerMessage(performer.getDeity().getName() + " would never help the infidel " + target.getName() + ".", (byte)3);
         return false;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      target.getStatus().modifyStamina2(100.0F);
      if (performer != target) {
         performer.getCommunicator().sendNormalServerMessage("You refresh " + target.getName() + ".", (byte)2);
         if (performer != target) {
            target.getCommunicator().sendNormalServerMessage(performer.getName() + " refreshens you.", (byte)2);
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You refresh yourself.", (byte)2);
      }
   }
}
