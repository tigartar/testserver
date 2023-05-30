package com.wurmonline.server.spells;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.skills.Skill;

public final class BreakAltar extends ReligiousSpell {
   public static final int RANGE = 10;
   private static final float MAX_DAM = 99.9F;

   BreakAltar() {
      super("Break Altar", 258, 30, 20, 50, 40, 300000L);
      this.targetItem = true;
      this.description = "damages altars and huge altars for an increased favor cost";
      this.type = 0;
      this.hasDynamicCost = true;
   }

   @Override
   public int getCost(Item target) {
      return target.isHugeAltar() ? this.getCost() * 4 : this.getCost();
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      if (!target.isDomainItem()) {
         performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
         return false;
      } else if (target.isHugeAltar() && !Deities.mayDestroyAltars()) {
         performer.getCommunicator()
            .sendNormalServerMessage(
               "The time is not right. The moons make the "
                  + target.getName()
                  + " vulnerable on Wrath day and the day of Awakening in the first and third week of a Starfall."
            );
         return false;
      } else if (target.isHugeAltar() && performer.getStrengthSkill() < 23.0) {
         performer.getCommunicator()
            .sendNormalServerMessage(
               "The altar resists your attempt to break it due to your physical weakness. You need at least 23 body strength in order to affect it."
            );
         return false;
      } else {
         return true;
      }
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      try {
         if (target.getOwner() != -10L) {
            Creature owner = Server.getInstance().getCreature(target.getOwner());
            if (!owner.equals(performer)) {
               owner.getCommunicator().sendNormalServerMessage(performer.getName() + " damages the " + target.getName() + ".", (byte)4);
            }
         }
      } catch (NoSuchPlayerException | NotOwnedException | NoSuchCreatureException var7) {
      }

      if (target.getDamage() >= 99.9F) {
         performer.getCommunicator().sendSafeServerMessage("You destroy the " + target.getName() + "!", (byte)3);
         EndGameItems.destroyHugeAltar(target, performer);
      } else {
         target.setDamage(Math.min(99.9F, target.getDamage() + 1.0F));
      }

      if (target.getDamage() < 10.0F) {
         performer.getCommunicator().sendNormalServerMessage("You feel the power of the altar wane for a second.", (byte)2);
      } else if (target.getDamage() < 30.0F) {
         performer.getCommunicator().sendNormalServerMessage("The spell has good effect on the altar.", (byte)2);
      } else if (target.getDamage() < 50.0F) {
         performer.getCommunicator().sendNormalServerMessage("The altar takes good damage from the spell.", (byte)2);
      } else if (target.getDamage() < 70.0F) {
         performer.getCommunicator().sendNormalServerMessage("The altar is starting to look pretty bad.", (byte)2);
      } else if (target.getDamage() < 95.0F) {
         performer.getCommunicator().sendNormalServerMessage("Not much is left of the altar's power now.", (byte)2);
      } else {
         performer.getCommunicator().sendNormalServerMessage("The altar is shuddering from your spell, and your inner eye can see it collapse.", (byte)2);
      }
   }
}
