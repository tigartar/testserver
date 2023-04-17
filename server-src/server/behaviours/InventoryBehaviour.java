/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.ManageMenu;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.PapyrusBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.questions.TextInputQuestion;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class InventoryBehaviour
extends Behaviour {
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
        }
        if (action == 567) {
            InventoryBehaviour.addGroup(target, performer);
            return true;
        }
        if (action == 59) {
            if (target.getTemplateId() != 824) {
                return true;
            }
            InventoryBehaviour.renameGroup(target, performer);
            return true;
        }
        if (action == 586) {
            InventoryBehaviour.removeGroup(target, performer);
            return true;
        }
        if (action == 568 || action == 3) {
            InventoryBehaviour.openContainer(target, performer);
            return true;
        }
        if (ManageMenu.isManageAction(performer, action)) {
            return ManageMenu.action(act, performer, action, counter);
        }
        return super.action(act, performer, target, action, counter);
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        if (action == 1) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 567 || action == 59 || action == 586) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 568 || action == 3) {
            return this.action(act, performer, target, action, counter);
        }
        if (ManageMenu.isManageAction(performer, action)) {
            return ManageMenu.action(act, performer, action, counter);
        }
        if (action == 744 && source.canHaveInscription()) {
            return PapyrusBehaviour.addToCookbook(act, performer, source, target, action, counter);
        }
        return super.action(act, performer, source, target, action, counter);
    }

    private static void removeGroup(Item group2, Creature performer) {
        if (group2.getTemplateId() != 824) {
            return;
        }
        if (group2.getItemsAsArray().length > 0) {
            performer.getCommunicator().sendNormalServerMessage("The group must be empty before you can remove it.");
            return;
        }
        Items.destroyItem(group2.getWurmId());
    }

    private static void addGroup(Item inventory, Creature performer) {
        if (!inventory.isInventory() && !inventory.isInventoryGroup() || inventory.getOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You can only add groups to your inventory or other groups.");
            return;
        }
        Item[] items = performer.getInventory().getItemsAsArray();
        int groupCount = 0;
        for (int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() == 824) {
                ++groupCount;
            }
            if (groupCount == 20) break;
        }
        if (groupCount >= 20) {
            performer.getCommunicator().sendNormalServerMessage("You can only have 20 groups.");
            return;
        }
        try {
            Item group2 = ItemFactory.createItem(824, 100.0f, "");
            group2.setName("Group");
            inventory.insertItem(group2, true);
            InventoryBehaviour.renameGroup(group2, performer);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
    }

    private static void openContainer(Item group2, Creature performer) {
        performer.getCommunicator().sendOpenInventoryContainer(group2.getWurmId());
    }

    private static void renameGroup(Item group2, Creature performer) {
        TextInputQuestion tiq = new TextInputQuestion(performer, "Setting name for group.", "Set the new name:", 1, group2.getWurmId(), 20, false);
        tiq.setOldtext(group2.getName());
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

