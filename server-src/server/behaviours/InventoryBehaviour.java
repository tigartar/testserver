package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.questions.TextInputQuestion;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InventoryBehaviour extends Behaviour {
   private static final Logger logger = Logger.getLogger(MethodsItems.class.getName());

   public InventoryBehaviour() {
      super((short)49);
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      if (action == 1) {
         performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
         target.sendEnchantmentStrings(performer.getCommunicator());
         return true;
      } else if (action == 567) {
         addGroup(target, performer);
         return true;
      } else if (action == 59) {
         if (target.getTemplateId() != 824) {
            return true;
         } else {
            renameGroup(target, performer);
            return true;
         }
      } else if (action == 586) {
         removeGroup(target, performer);
         return true;
      } else if (action == 568 || action == 3) {
         openContainer(target, performer);
         return true;
      } else {
         return ManageMenu.isManageAction(performer, action)
            ? ManageMenu.action(act, performer, action, counter)
            : super.action(act, performer, target, action, counter);
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      if (action == 1) {
         return this.action(act, performer, target, action, counter);
      } else if (action == 567 || action == 59 || action == 586) {
         return this.action(act, performer, target, action, counter);
      } else if (action == 568 || action == 3) {
         return this.action(act, performer, target, action, counter);
      } else if (ManageMenu.isManageAction(performer, action)) {
         return ManageMenu.action(act, performer, action, counter);
      } else {
         return action == 744 && source.canHaveInscription()
            ? PapyrusBehaviour.addToCookbook(act, performer, source, target, action, counter)
            : super.action(act, performer, source, target, action, counter);
      }
   }

   private static void removeGroup(Item group, Creature performer) {
      if (group.getTemplateId() == 824) {
         if (group.getItemsAsArray().length > 0) {
            performer.getCommunicator().sendNormalServerMessage("The group must be empty before you can remove it.");
         } else {
            Items.destroyItem(group.getWurmId());
         }
      }
   }

   private static void addGroup(Item inventory, Creature performer) {
      if ((inventory.isInventory() || inventory.isInventoryGroup()) && inventory.getOwnerId() == performer.getWurmId()) {
         Item[] items = performer.getInventory().getItemsAsArray();
         int groupCount = 0;

         for(int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() == 824) {
               ++groupCount;
            }

            if (groupCount == 20) {
               break;
            }
         }

         if (groupCount >= 20) {
            performer.getCommunicator().sendNormalServerMessage("You can only have 20 groups.");
         } else {
            try {
               Item group = ItemFactory.createItem(824, 100.0F, "");
               group.setName("Group");
               inventory.insertItem(group, true);
               renameGroup(group, performer);
            } catch (NoSuchTemplateException var5) {
               logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            } catch (FailedException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You can only add groups to your inventory or other groups.");
      }
   }

   private static void openContainer(Item group, Creature performer) {
      performer.getCommunicator().sendOpenInventoryContainer(group.getWurmId());
   }

   private static void renameGroup(Item group, Creature performer) {
      TextInputQuestion tiq = new TextInputQuestion(performer, "Setting name for group.", "Set the new name:", 1, group.getWurmId(), 20, false);
      tiq.setOldtext(group.getName());
      tiq.sendQuestion();
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      int tid = target.getTemplateId();
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      if ((target.isInventory() || target.isInventoryGroup()) && target.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[567]);
      }

      if (tid == 824 && target.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[59]);
         toReturn.add(Actions.actionEntrys[586]);
         toReturn.add(Actions.actionEntrys[568]);
      }

      toReturn.addAll(ManageMenu.getBehavioursFor(performer));
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      int tid = target.getTemplateId();
      if ((target.isInventory() || target.isInventoryGroup()) && target.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[567]);
      }

      if (tid == 824 && target.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[59]);
         toReturn.add(Actions.actionEntrys[586]);
         toReturn.add(Actions.actionEntrys[568]);
      }

      toReturn.addAll(ManageMenu.getBehavioursFor(performer));
      if (target.isInventory() && source.canHaveInscription()) {
         toReturn.addAll(PapyrusBehaviour.getPapyrusBehavioursFor(performer, source));
      }

      return toReturn;
   }
}
