package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import java.util.LinkedList;
import java.util.List;

final class HugeLogBehaviour extends ItemBehaviour {
   HugeLogBehaviour() {
      super((short)37);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, source, target));
      boolean reachable = false;
      if (target.getOwnerId() == -10L) {
         if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null) {
               reachable = true;
            }
         }
      } else if (target.getOwnerId() == performer.getWurmId()) {
         reachable = true;
      }

      if (reachable && (source.isWeaponAxe() || source.getTemplateId() == 24)) {
         toReturn.add(Actions.actionEntrys[97]);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = false;
      boolean reachable = false;
      if (target.getOwnerId() == -10L) {
         if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
            reachable = true;
         }
      } else if (target.getOwnerId() == performer.getWurmId()) {
         reachable = true;
      }

      if (reachable) {
         if (action == 97) {
            done = MethodsItems.chop(act, performer, source, target, counter);
         } else {
            done = super.action(act, performer, source, target, action, counter);
         }
      } else {
         done = super.action(act, performer, source, target, action, counter);
      }

      return done;
   }
}
