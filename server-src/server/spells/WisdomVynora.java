package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;

public class WisdomVynora extends ReligiousSpell {
   public static final int RANGE = 12;

   public WisdomVynora() {
      super("Wisdom of Vynora", 445, 30, 30, 50, 30, 1800000L);
      this.targetCreature = true;
      this.description = "transfers fatigue to sleep bonus";
      this.type = 1;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      if (target.isReborn()) {
         return true;
      } else if (target.getFatigueLeft() < 100) {
         performer.getCommunicator().sendNormalServerMessage(target.getName() + " has almost no fatigue left.", (byte)3);
         return false;
      } else if (!target.equals(performer)) {
         if (performer.getDeity() != null) {
            if (target.getDeity() != null) {
               if (target.getDeity().isHateGod()) {
                  if (performer.isFaithful()) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(performer.getDeity().getName() + " would never help the infidel " + target.getName() + "!", (byte)3);
                     return false;
                  } else {
                     return true;
                  }
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      double toconvert = Math.max(20.0, power);
      toconvert = Math.min(99.0, toconvert + (double)(performer.getNumLinks() * 10));
      toconvert /= 100.0;
      int numsecondsToMove = Math.min((int)((double)((float)target.getFatigueLeft() / 12.0F) * toconvert), 3600);
      target.setFatigue(-numsecondsToMove);
      numsecondsToMove = (int)((float)numsecondsToMove * 0.2F);
      if (target.isPlayer()) {
         ((Player)target).getSaveFile().addToSleep(numsecondsToMove);
      }
   }
}
