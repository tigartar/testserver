package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.LinkedList;
import java.util.List;

final class ToyBehaviour extends ItemBehaviour {
   ToyBehaviour() {
      super((short)26);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item object) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (object.getTemplateId() == 271) {
         toReturn.add(Actions.actionEntrys[190]);
      }

      toReturn.addAll(super.getBehavioursFor(performer, object));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (object.getTemplateId() == 271) {
         toReturn.add(Actions.actionEntrys[190]);
      }

      toReturn.addAll(super.getBehavioursFor(performer, source, object));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean toReturn = true;
      if (action == 190) {
         if (target.getTemplateId() == 271) {
            toReturn = MethodsItems.yoyo(performer, target, counter, act);
         }
      } else {
         toReturn = super.action(act, performer, target, action, counter);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean toReturn = false;
      if (target.getTemplateId() == 271 && action == 190) {
         toReturn = this.action(act, performer, target, action, counter);
      } else {
         toReturn = super.action(act, performer, source, target, action, counter);
      }

      return toReturn;
   }
}
