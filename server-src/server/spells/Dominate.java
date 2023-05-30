package com.wurmonline.server.spells;

import com.wurmonline.server.Constants;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.skills.Skill;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dominate extends ReligiousSpell {
   private static final Logger logger = Logger.getLogger(Dominate.class.getName());
   public static final int RANGE = 6;

   Dominate() {
      super("Dominate", 274, 20, 40, 35, 39, 0L);
      this.targetCreature = true;
      this.offensive = true;
      this.dominate = true;
      this.description = "forces a creature or monster to become a loyal companion of yours";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Creature target) {
      return mayDominate(performer, target);
   }

   public static boolean mayDominate(Creature performer, Creature target) {
      if (performer.getDeity() != null) {
         if (target.isDominatable(performer) && target.isMonster()) {
            if (performer.getPet() != null && DbCreatureStatus.getIsLoaded(performer.getPet().getWurmId()) == 1) {
               performer.getCommunicator().sendNormalServerMessage("You have a pet in a cage, remove it first, to dominate this one.", (byte)3);
               return false;
            }

            if (!target.isReborn() && (!target.isDominated() || target.getDominator() == performer)) {
               return true;
            }

            performer.getCommunicator().sendNormalServerMessage("You fail to connect with the " + target.getName() + ".", (byte)3);
            return false;
         }

         performer.getCommunicator().sendNormalServerMessage("You fail to connect with the " + target.getName() + ".", (byte)3);
      }

      return false;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
      if (power > 0.0) {
         dominate(power, performer, target);
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to bind " + target.getNameWithGenus() + ".", (byte)3);
      }
   }

   public static void dominate(double power, Creature performer, Creature target) {
      if (performer.getPet() != null && performer.getPet() != target) {
         performer.getCommunicator().sendNormalServerMessage(performer.getPet().getNameWithGenus() + " stops following you.", (byte)2);
         if (performer.getPet().getLeader() == performer) {
            performer.getPet().setLeader(null);
         }

         performer.getPet().setDominator(-10L);
         performer.setPet(-10L);
      }

      boolean newpet = false;
      if (target.dominator != performer.getWurmId()) {
         newpet = true;
      }

      target.setTarget(-10L, true);
      target.stopFighting();
      if (performer.getTarget() == target) {
         performer.setTarget(-10L, true);
         performer.stopFighting();
      }

      if (target.opponent == performer) {
         target.setOpponent(null);
      }

      if (performer.opponent == target) {
         performer.setOpponent(null);
      }

      try {
         target.setKingdomId(performer.getKingdomId());
      } catch (IOException var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
      }

      target.setDominator(performer.getWurmId());
      if (newpet) {
         target.setLoyalty((float)Math.max(10.0, power));
         performer.setPet(target.getWurmId());
         target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " dominates you!");
         performer.getCommunicator().sendNormalServerMessage("You bind " + target.getNameWithGenus() + " with fear for your power.", (byte)2);
         if (target.isUnique()) {
            HistoryManager.addHistory(performer.getName(), "dominated " + target.getName());
         }
      } else {
         target.setLoyalty((float)Math.min(99.0, (double)target.getLoyalty() + power / 10.0));
         target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " dominates you!");
         performer.getCommunicator().sendNormalServerMessage("You strengthen " + target.getNameWithGenus() + "'s fear for your power.", (byte)2);
      }

      if (Constants.devmode) {
         performer.getCommunicator().sendNormalServerMessage("New loyalty=" + target.getLoyalty());
      }

      target.getStatus().setLastPolledLoyalty();
   }
}
