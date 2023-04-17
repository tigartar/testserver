/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.TakeResultEnum;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Zones;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AutoEquipMethods {
    private static final Logger logger = Logger.getLogger(AutoEquipMethods.class.getName());
    private static List<Short> nextAutoEquipAction = new ArrayList<Short>();

    private AutoEquipMethods() {
    }

    public static List<ActionEntry> getBehaviours(Item item, Creature player) {
        LinkedList<ActionEntry> entries = new LinkedList<ActionEntry>();
        if (item.isCreatureWearableOnly()) {
            return entries;
        }
        if (player.hasFlag(7)) {
            return entries;
        }
        Item parent = item.getParentOrNull();
        if (parent != null && parent.isBodyPart() && parent.getOwnerId() == player.getWurmId()) {
            entries.add(Actions.actionEntrys[585]);
            return entries;
        }
        byte[] slots = AutoEquipMethods.getValidEquipmentSlots(item);
        if (slots != null && parent != null) {
            if (AutoEquipMethods.containsLeftOrRightSlots(slots, item) && slots.length > 1) {
                entries.add(Actions.actionEntrys[582]);
                entries.add(new ActionEntry(-2, "Equip specific", "equipping"));
                entries.add(Actions.actionEntrys[584]);
                entries.add(Actions.actionEntrys[583]);
            } else if (AutoEquipMethods.isMultiSlot(slots, item) && slots.length > 1) {
                Item part1 = AutoEquipMethods.getBodySlot(slots[0], player);
                Item part2 = AutoEquipMethods.getBodySlot(slots[1], player);
                entries.add(Actions.actionEntrys[582]);
                short num = 0;
                if (part1 != null) {
                    num = (short)(num - 1);
                }
                if (part2 != null) {
                    num = (short)(num - 1);
                }
                if (num < 0) {
                    entries.add(new ActionEntry(num, "Equip specific", "equipping"));
                }
                if (part1 != null) {
                    entries.add(new ActionEntry(584, "equip " + part1.getName(), "equipping"));
                }
                if (part2 != null) {
                    entries.add(new ActionEntry(583, "equip " + part2.getName(), "equipping"));
                }
            } else if (slots.length > 0) {
                entries.add(Actions.actionEntrys[582]);
            }
        }
        return entries;
    }

    private static final boolean dropFromParent(Item item, Item parent) {
        try {
            if (parent != null) {
                parent.dropItem(item.getWurmId(), parent.getWurmId(), false);
                if (parent.getTemplateId() == 177) {
                    parent.removeFromPile(item);
                }
                return true;
            }
            return false;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Failed to drop item: " + item.getName() + " id: " + item.getWurmId() + " from parent: " + parent.getName() + " id: " + parent.getWurmId(), nsi);
            return false;
        }
    }

    private static final boolean dropToInventory(Item item, Creature player) {
        Item parent = item.getParentOrNull();
        AutoEquipMethods.dropFromParent(item, parent);
        if (player.getInventory().testInsertItem(item)) {
            player.getInventory().insertItem(item);
            return true;
        }
        parent.insertItem(item);
        return false;
    }

    private static final boolean canCarry(Item item, Creature player) {
        return item.getTopParent() == player.getInventory().getWurmId() || item.getTopParent() == player.getBody().getId() ? true : player.canCarry(item.getFullWeight());
    }

    private static final boolean autoEquipWeapon(Item item, Creature player, byte slot, boolean isLeft) {
        Item part = AutoEquipMethods.getBodySlot(slot, player);
        if (part == null) {
            logger.log(Level.WARNING, "(autoEquipWeapon) Player: " + player.getName() + " is unable to find body part for slot: " + slot);
            return false;
        }
        Item leftWeapon = player.getLefthandItem();
        Item rightWeapon = player.getRighthandItem();
        Item shield = player.getShield();
        Item parent = item.getParentOrNull();
        boolean hasLeft = leftWeapon != null;
        boolean hasRight = rightWeapon != null;
        boolean hasShield = shield != null;
        boolean hasTwoHander = hasRight && rightWeapon.isTwoHanded() || hasLeft && leftWeapon.isTwoHanded();
        boolean failedReplace = false;
        if (hasLeft && isLeft || hasLeft && hasTwoHander || hasLeft && item.isTwoHanded()) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(leftWeapon, player);
        }
        if ((hasRight && !isLeft || hasRight && hasTwoHander || hasRight && item.isTwoHanded()) && !failedReplace) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(rightWeapon, player);
        }
        if (hasShield && isLeft || hasShield && item.isTwoHanded() && !failedReplace) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(shield, player);
        }
        if (failedReplace) {
            return false;
        }
        boolean canCarry = AutoEquipMethods.canCarry(item, player);
        if (part.testInsertItem(item) && canCarry) {
            if (!AutoEquipMethods.dropFromParent(item, parent)) {
                return false;
            }
            part.insertItem(item);
            return true;
        }
        if (parent != null) {
            parent.insertItem(item);
        }
        if (!canCarry) {
            player.getCommunicator().sendNormalServerMessage("You are carrying too much.");
        }
        return false;
    }

    private static final Item getCurrentlyEquippedItem(Item part) {
        if (part != null) {
            for (Item item : part.getItems()) {
                if (item.isBodyPart()) continue;
                return item;
            }
        }
        return null;
    }

    public static final void addNextAutoEquipAction(short action) {
        nextAutoEquipAction.add(action);
    }

    public static final boolean timedAutoEquip(Creature performer, long itemId, short action, Action act, float counter) {
        boolean done = false;
        Item armour = null;
        try {
            armour = Items.getItem(itemId);
        }
        catch (NoSuchItemException e) {
            performer.getCommunicator().sendNormalServerMessage("You cannot equip that.");
            return true;
        }
        if (counter == 1.0f) {
            act.setTimeLeft((int)Math.max(25.0, 50.0 - performer.getBodyControlSkill().getKnowledge() * (double)0.4f));
            performer.getCommunicator().sendNormalServerMessage("You try to quickly equip the " + armour.getName() + ".");
            performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, act.getTimeLeft());
        }
        if (act.currentSecond() % 2 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter > (float)(act.getTimeLeft() / 10)) {
            short actualAction = action;
            if (!nextAutoEquipAction.isEmpty()) {
                actualAction = nextAutoEquipAction.remove(0);
            }
            AutoEquipMethods.autoEquip(itemId, performer, actualAction, act);
            done = true;
        }
        return done;
    }

    public static final boolean timedDragEquip(Creature performer, long itemId, long targetId, Action act, float counter) {
        boolean done = false;
        Item armour = null;
        try {
            armour = Items.getItem(itemId);
        }
        catch (NoSuchItemException e) {
            performer.getCommunicator().sendNormalServerMessage("You cannot equip that.");
            return true;
        }
        if (armour == null) {
            return true;
        }
        if (counter == 1.0f) {
            act.setTimeLeft((int)Math.max(25.0, 50.0 - performer.getBodyControlSkill().getKnowledge() * (double)0.4f));
            performer.getCommunicator().sendNormalServerMessage("You try to quickly equip the " + armour.getName() + ".");
            performer.sendActionControl(Actions.actionEntrys[723].getVerbString(), true, act.getTimeLeft());
        }
        if (act.currentSecond() % 2 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter > (float)(act.getTimeLeft() / 10)) {
            Item topParent = armour.getTopParentOrNull();
            try {
                if (armour.moveToItem(performer, targetId, true)) {
                    performer.getCommunicator().sendUpdateInventoryItem(armour);
                    if (topParent != null && topParent.getTemplateId() != 177) {
                        topParent.updateModelNameOnGroundItem();
                    }
                }
            }
            catch (NoSuchItemException | NoSuchPlayerException | NoSuchCreatureException e) {
                performer.getCommunicator().sendNormalServerMessage("You cannot equip that.");
            }
            done = true;
        }
        return done;
    }

    public static final boolean autoEquip(long itemId, Creature player, short action, Action act) {
        try {
            Item item = Items.getItem(itemId);
            if (!item.isBulkContainer() && !item.isNoTake()) {
                return AutoEquipMethods.autoEquip(item, player, action, act, true);
            }
            return false;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Unable to find item to equip.", nsi);
            return false;
        }
    }

    public static final boolean autoEquip(Item item, Creature player, short action, Action act, boolean performGuardCheck) {
        if (item.isCreatureWearableOnly()) {
            return false;
        }
        if (item.getTopParent() == player.getBody().getId() && item.getParentOrNull() != null && !item.getParentOrNull().isHollow()) {
            player.getCommunicator().sendNormalServerMessage("You already have this item equipped.");
            return false;
        }
        if (item.getParentOrNull() == null) {
            return false;
        }
        if (item.isTraded()) {
            player.getCommunicator().sendNormalServerMessage("You are not allowed to do that, the item: " + item.getName() + " is being traded.");
            return false;
        }
        if (!MethodsItems.isLootableBy(player, item) || item.isNoTake()) {
            player.getCommunicator().sendNormalServerMessage("You are not allowed to loot that.");
            return false;
        }
        TakeResultEnum result = MethodsItems.take(act, player, item);
        switch (result) {
            case SUCCESS: 
            case TARGET_HAS_NO_OWNER: 
            case PERFORMER_IS_OWNER: {
                break;
            }
            default: {
                result.sendToPerformer(player);
                return false;
            }
        }
        if (MethodsItems.checkIfStealing(item, player, null)) {
            int tilex = (int)item.getPosX() >> 2;
            int tiley = (int)item.getPosY() >> 2;
            Village vil = Zones.getVillage(tilex, tiley, player.isOnSurface());
            if (player.isLegal() && vil != null) {
                player.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
                return false;
            }
            if (player.getDeity() != null && !player.getDeity().isLibila() && player.faithful) {
                player.getCommunicator().sendNormalServerMessage("Your deity would never allow stealing.");
                return false;
            }
            if (!Servers.localServer.PVPSERVER) {
                player.getCommunicator().sendNormalServerMessage("That would be very bad for your karma and is disallowed on this server.");
                return false;
            }
            if (!player.maySteal()) {
                player.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
                return false;
            }
            if (performGuardCheck && MethodsItems.setTheftEffects(player, act, item)) {
                return false;
            }
        }
        if (action == 582 || action == 724 || action == 723) {
            byte[] spaces = AutoEquipMethods.getValidEquipmentSlots(item);
            if (item.isShield()) {
                return AutoEquipMethods.equipShield(item, player, spaces[0]);
            }
            if (AutoEquipMethods.containsLeftOrRightSlots(spaces, item)) {
                byte leftSlot = AutoEquipMethods.getLeftSlot(spaces);
                byte rightSlot = AutoEquipMethods.getRightSlot(spaces);
                return AutoEquipMethods.equipLeftRight(player, item, leftSlot, rightSlot, act);
            }
            if (AutoEquipMethods.isMultiSlot(spaces, item)) {
                byte rightSlot = spaces[0];
                byte leftSlot = spaces[1];
                return AutoEquipMethods.equipLeftRight(player, item, leftSlot, rightSlot, act);
            }
            for (int i = 0; i < spaces.length; ++i) {
                if (!AutoEquipMethods.tryEquipInSlot(spaces[i], item, player)) continue;
                return true;
            }
            return false;
        }
        if (action == 583) {
            return AutoEquipMethods.tryEquipLeft(item, player, act);
        }
        if (action == 584) {
            return AutoEquipMethods.tryEquipRight(item, player, act);
        }
        return false;
    }

    private static final boolean equipShield(Item item, Creature player, byte slot) {
        Item part = AutoEquipMethods.getBodySlot(slot, player);
        if (part == null) {
            return false;
        }
        Item leftWeapon = player.getLefthandItem();
        Item shield = player.getShield();
        Item rightWeapon = player.getRighthandItem();
        Item parent = item.getParentOrNull();
        boolean hasShield = shield != null;
        boolean hasLeftWeapon = leftWeapon != null;
        boolean hasRightWeapon = rightWeapon != null;
        boolean isTwoHanded = hasRightWeapon && rightWeapon.isTwoHanded() || hasLeftWeapon && leftWeapon.isTwoHanded();
        boolean failedReplace = false;
        if (hasRightWeapon && isTwoHanded) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(rightWeapon, player);
        }
        if (hasLeftWeapon && !failedReplace) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(leftWeapon, player);
        }
        if (hasShield && !failedReplace) {
            boolean bl = failedReplace = !AutoEquipMethods.dropToInventory(shield, player);
        }
        if (failedReplace) {
            return false;
        }
        AutoEquipMethods.dropFromParent(item, parent);
        if (part.testInsertItem(item) && player.canCarry(item.getFullWeight())) {
            part.insertItem(item);
            return true;
        }
        if (parent != null) {
            parent.insertItem(item);
        }
        return false;
    }

    private static final boolean equipLeftRight(Creature player, Item item, byte leftSlot, byte rightSlot, Action act) {
        Item leftPart = AutoEquipMethods.getBodySlot(leftSlot, player);
        Item rightPart = AutoEquipMethods.getBodySlot(rightSlot, player);
        Item equippedRight = AutoEquipMethods.getCurrentlyEquippedItem(rightPart);
        if (rightSlot != -1 && (equippedRight == null || item.isWeapon() || item.isWeaponBow() || equippedRight.isTwoHanded())) {
            return AutoEquipMethods.autoEquip(item, player, (short)584, act, false);
        }
        if (leftSlot != -1 && AutoEquipMethods.isSlotEmpty(leftPart)) {
            return AutoEquipMethods.autoEquip(item, player, (short)583, act, false);
        }
        Item equippedLeft = AutoEquipMethods.getCurrentlyEquippedItem(leftPart);
        if (rightSlot != -1 && equippedRight.getTemplateId() != item.getTemplateId()) {
            return AutoEquipMethods.autoEquip(item, player, (short)584, act, false);
        }
        if (leftSlot != -1 && equippedLeft.getTemplateId() != item.getTemplateId()) {
            return AutoEquipMethods.autoEquip(item, player, (short)583, act, false);
        }
        return AutoEquipMethods.autoEquip(item, player, (short)584, act, false);
    }

    private static final Item getBodySlot(byte slot, Creature player) {
        try {
            return player.getBody().getBodyPart(slot);
        }
        catch (NoSpaceException ns) {
            return null;
        }
    }

    private static final byte[] getValidEquipmentSlots(Item item) {
        int templateId = item.getTemplateId();
        if (item.isShield()) {
            return new byte[]{44};
        }
        if (item.isWeapon()) {
            return new byte[]{37, 38};
        }
        if (item.isWeaponBow()) {
            return new byte[]{38};
        }
        if (item.isArmour() && !item.isShield()) {
            byte[] spaces = item.getBodySpaces();
            boolean containsSecondHead = false;
            for (int i = 0; i < spaces.length; ++i) {
                if (spaces[i] != 28) continue;
                containsSecondHead = true;
            }
            if (containsSecondHead) {
                return new byte[]{1};
            }
            return spaces;
        }
        if (item.isBelt()) {
            return new byte[]{43};
        }
        if (templateId == 297) {
            return new byte[]{40, 39};
        }
        if (templateId == 231) {
            return new byte[]{3, 4};
        }
        if (templateId == 230 || templateId == 740 || templateId == 985) {
            return new byte[]{36};
        }
        if (templateId == 443) {
            return new byte[]{41};
        }
        if (item.isBag()) {
            return new byte[]{42};
        }
        if (item.isQuiver()) {
            return new byte[]{42, 41};
        }
        return item.getBodySpaces();
    }

    public static final boolean unequip(long target, Creature player) {
        try {
            Item item = Items.getItem(target);
            return AutoEquipMethods.unequip(item, player);
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Unable to find item to unequip.", nsi);
            return false;
        }
    }

    public static final boolean unequip(Item item, Creature player) {
        if (item.getTopParent() != player.getBody().getId()) {
            player.getCommunicator().sendNormalServerMessage("You don't have this item equipped.", (byte)3);
            return false;
        }
        Item parent = item.getParentOrNull();
        if (parent == null) {
            player.getCommunicator().sendNormalServerMessage("No parent for item, not equipped?", (byte)3);
            return false;
        }
        if (parent.getOwnerId() != player.getWurmId()) {
            player.getCommunicator().sendNormalServerMessage("You don't have this item equipped.", (byte)3);
            return false;
        }
        boolean canInsert = player.getInventory().testInsertItem(item);
        boolean mayInsert = player.getInventory().mayCreatureInsertItem();
        if (canInsert && mayInsert) {
            try {
                parent.dropItem(item.getWurmId(), player.getInventory().getWurmId(), false);
                if (!player.getInventory().insertItem(item)) {
                    player.getCommunicator().sendNormalServerMessage("Failed to insert item in inventory.", (byte)3);
                    parent.insertItem(item);
                    return false;
                }
                return true;
            }
            catch (NoSuchItemException nsi) {
                return false;
            }
        }
        if (!canInsert) {
            player.getCommunicator().sendNormalServerMessage("Unable to add the item to the inventory.", (byte)3);
        }
        if (!mayInsert) {
            player.getCommunicator().sendNormalServerMessage("The inventory contains too many items.", (byte)3);
        }
        return false;
    }

    private static final boolean isSlotEmpty(Item part) {
        for (Item item : part.getItems()) {
            if (item.isBodyPart()) continue;
            return false;
        }
        return true;
    }

    private static final boolean containsLeftOrRightSlots(byte[] slots, Item item) {
        if (item.isShield()) {
            return false;
        }
        for (int i = 0; i < slots.length; ++i) {
            switch (slots[i]) {
                case 3: 
                case 4: 
                case 13: 
                case 14: 
                case 15: 
                case 16: 
                case 37: 
                case 38: 
                case 39: 
                case 40: 
                case 46: 
                case 47: {
                    return true;
                }
            }
        }
        return false;
    }

    private static final byte getRightSlot(byte[] slots) {
        for (int i = 0; i < slots.length; ++i) {
            switch (slots[i]) {
                case 4: 
                case 14: 
                case 16: 
                case 38: 
                case 40: 
                case 47: {
                    return slots[i];
                }
            }
        }
        return -1;
    }

    private static final byte getLeftSlot(byte[] slots) {
        for (int i = 0; i < slots.length; ++i) {
            switch (slots[i]) {
                case 3: 
                case 13: 
                case 15: 
                case 37: 
                case 39: 
                case 46: {
                    return slots[i];
                }
            }
        }
        return -1;
    }

    private static final boolean isMultiSlot(byte[] slots, Item item) {
        return !AutoEquipMethods.containsLeftOrRightSlots(slots, item) && slots.length > 1;
    }

    private static final boolean tryEquipLeft(Item item, Creature player, Action act) {
        byte[] slots = AutoEquipMethods.getValidEquipmentSlots(item);
        if (item.isWeapon() || item.isWeaponBow()) {
            byte slot = AutoEquipMethods.getLeftSlot(slots);
            return AutoEquipMethods.autoEquipWeapon(item, player, slot, true);
        }
        if (AutoEquipMethods.containsLeftOrRightSlots(slots, item)) {
            byte slot = AutoEquipMethods.getLeftSlot(slots);
            return AutoEquipMethods.tryEquipInSlot(slot, item, player);
        }
        if (AutoEquipMethods.isMultiSlot(slots, item)) {
            byte slot = slots[1];
            return AutoEquipMethods.tryEquipInSlot(slot, item, player);
        }
        return AutoEquipMethods.autoEquip(item, player, (short)582, act, false);
    }

    private static final boolean tryEquipRight(Item item, Creature player, Action act) {
        byte[] slots = AutoEquipMethods.getValidEquipmentSlots(item);
        if (item.isWeapon() || item.isWeaponBow()) {
            byte slot = AutoEquipMethods.getRightSlot(slots);
            return AutoEquipMethods.autoEquipWeapon(item, player, slot, false);
        }
        if (AutoEquipMethods.containsLeftOrRightSlots(slots, item)) {
            byte slot = AutoEquipMethods.getRightSlot(slots);
            return AutoEquipMethods.tryEquipInSlot(slot, item, player);
        }
        if (AutoEquipMethods.isMultiSlot(slots, item)) {
            byte slot = slots[0];
            return AutoEquipMethods.tryEquipInSlot(slot, item, player);
        }
        return AutoEquipMethods.autoEquip(item, player, (short)582, act, false);
    }

    private static final boolean tryEquipInSlot(byte slot, Item item, Creature player) {
        Item part = AutoEquipMethods.getBodySlot(slot, player);
        if (part == null) {
            logger.log(Level.WARNING, "Player: " + player.getName() + " Unable to find body slot for slot id: " + slot);
            return false;
        }
        Item oldItem = AutoEquipMethods.getCurrentlyEquippedItem(part);
        boolean failedDrop = false;
        Item oldParent = null;
        if (oldItem != null) {
            oldParent = oldItem.getParentOrNull();
            boolean bl = failedDrop = !AutoEquipMethods.dropToInventory(oldItem, player);
        }
        if (failedDrop) {
            return false;
        }
        boolean canCarry = AutoEquipMethods.canCarry(item, player);
        if (part.testInsertItem(item) && canCarry) {
            Item parent = item.getParentOrNull();
            if (parent != null && !AutoEquipMethods.dropFromParent(item, parent)) {
                if (oldParent != null && !failedDrop) {
                    oldParent.insertItem(oldItem);
                }
                return false;
            }
            if (part.insertItem(item)) {
                if (item.isBelt()) {
                    player.setBestToolbelt(null);
                    if (item.getTemplateId() == 516) {
                        player.setBestToolbelt(item);
                    }
                    player.pollToolbelt();
                }
                return true;
            }
            if (parent != null) {
                parent.insertItem(item);
            }
            if (oldParent != null && !failedDrop) {
                oldParent.insertItem(oldItem);
            }
            return false;
        }
        if (oldItem != null && oldParent != null) {
            AutoEquipMethods.dropFromParent(oldItem, oldParent);
            oldParent.insertItem(oldItem);
        }
        if (!canCarry) {
            player.getCommunicator().sendNormalServerMessage("You are carrying too much.", (byte)3);
        }
        return false;
    }
}

