/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemRestrictionManagement
extends Question {
    private static final Logger logger = Logger.getLogger(ItemRestrictionManagement.class.getName());
    private Permissions.IAllow obj;

    public ItemRestrictionManagement(Creature aResponder, Permissions.IAllow aObj, long aTargetId) {
        super(aResponder, "Manage Item Restrictions", ItemRestrictionManagement.makeQuestion(aResponder, aObj, aTargetId), 122, aTargetId);
        this.obj = aObj;
    }

    private static String makeQuestion(Creature aResponder, Permissions.IAllow aObj, long aTargetId) {
        String named = "";
        switch (WurmId.getType(aTargetId)) {
            case 2: {
                named = "Item: " + aObj.getName().replace("\"", "'");
                break;
            }
            case 28: {
                named = "Bridge Part: " + aObj.getName().replace("\"", "'");
                break;
            }
            case 7: {
                named = "Fence";
                break;
            }
            case 23: {
                named = "Floor";
                break;
            }
            case 5: {
                named = "Wall";
                break;
            }
            default: {
                named = "Unknown";
            }
        }
        return "Manage " + named + " Restrictions (id= " + aTargetId + ")";
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 122 && this.getResponder().getPower() >= 2) {
            String val;
            boolean plant;
            boolean noPut;
            boolean noDrop;
            boolean noDrag;
            boolean noRepair;
            boolean noImprove;
            boolean noRunes;
            boolean noPaint;
            boolean ownerTurn;
            boolean ownerMove;
            boolean noTurn;
            boolean noMove;
            boolean noLockpick;
            boolean noLock;
            boolean noDestroy;
            boolean noSpells;
            boolean noTake;
            boolean noEatOrDrink;
            boolean noDecay;
            boolean alwaysLit;
            boolean autoLight;
            boolean autoFill;
            boolean sealed;
            String itemName = "The " + this.obj.getName() + " ";
            boolean change = false;
            boolean spellremoved = false;
            boolean spelladded = true;
            if (this.obj.canHaveCourier()) {
                boolean courier = Boolean.parseBoolean(aAnswer.getProperty("courier"));
                boolean darkmessenger = Boolean.parseBoolean(aAnswer.getProperty("darkmessenger"));
                String spow = aAnswer.getProperty("pow");
                if (courier && darkmessenger) {
                    this.getResponder().getCommunicator().sendNormalServerMessage(itemName + "cannot support both courier and darkmessenger at the same time. So they have been ignored.");
                } else if ((courier || darkmessenger) && (spow == null || spow.length() == 0)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Need a power for the courier / darkmessenger. So as not given they have been ignored.");
                } else {
                    byte ench;
                    SpellEffect eff;
                    int pow = Integer.parseInt(spow);
                    if (pow < 0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Need a power for the courier / darkmessenger. Power must be positive, defaulting it to 50.");
                        pow = 50;
                    }
                    Spell spell = null;
                    if (this.obj.hasCourier()) {
                        spell = Spells.getSpell(338);
                    } else if (this.obj.hasDarkMessenger()) {
                        spell = Spells.getSpell(339);
                    }
                    if (courier != this.obj.hasCourier()) {
                        this.obj.setHasCourier(courier);
                        change = true;
                        if (courier) {
                            this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "now has Courier.");
                        } else {
                            this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "no longer has Courier.");
                        }
                    }
                    if (darkmessenger != this.obj.hasDarkMessenger()) {
                        this.obj.setHasDarkMessenger(darkmessenger);
                        change = true;
                        if (darkmessenger) {
                            this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "now has Dark Messenger.");
                        } else {
                            this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "no longer has Dark Messenger.");
                        }
                    }
                    Item item = (Item)this.obj;
                    if (spell != null && (eff = item.getSpellEffect(ench = spell.getEnchantment())) != null) {
                        item.getSpellEffects().removeSpellEffect(eff.type);
                        spellremoved = true;
                    }
                    spell = null;
                    if (courier) {
                        spell = Spells.getSpell(338);
                    } else if (darkmessenger) {
                        spell = Spells.getSpell(339);
                    }
                    if (spell != null) {
                        spell.castSpell((double)pow, this.getResponder(), item);
                        spelladded = true;
                    }
                }
            }
            if ((this.obj.canBeSealedByPlayer() || this.obj.canBePeggedByPlayer() || this.canGMSeal()) && (sealed = Boolean.parseBoolean(aAnswer.getProperty("sealed"))) != this.obj.isSealedByPlayer()) {
                this.obj.setIsSealedByPlayer(sealed);
                change = true;
                if (sealed) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is now sealed.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is no longer sealed.");
                }
            }
            if (this.obj.canBeAutoFilled() && (autoFill = Boolean.parseBoolean(aAnswer.getProperty("autofill"))) != this.obj.isAutoFilled()) {
                this.obj.setIsAutoFilled(autoFill);
                change = true;
                if (autoFill) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will Auto-Fill.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will no longer Auto-Fill.");
                }
            }
            if (this.obj.canBeAutoLit() && (autoLight = Boolean.parseBoolean(aAnswer.getProperty("autolight"))) != this.obj.isAutoLit()) {
                this.obj.setIsAutoLit(autoLight);
                change = true;
                if (autoLight) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will Auto-Light.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will no longer Auto-Light.");
                }
            }
            if (this.obj.canBeAlwaysLit() && (alwaysLit = Boolean.parseBoolean(aAnswer.getProperty("alwaysLit"))) != this.obj.isAlwaysLit()) {
                this.obj.setIsAlwaysLit(alwaysLit);
                change = true;
                if (alwaysLit) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will be Always Lit.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will no longer be Always Lit.");
                }
            }
            if (this.obj.canDisableDecay() && (noDecay = Boolean.parseBoolean(aAnswer.getProperty("noDecay"))) != this.obj.hasNoDecay()) {
                this.obj.setHasNoDecay(noDecay);
                change = true;
                if (noDecay) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will not decay further.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "will decay at default rate.");
                }
            }
            if (this.obj.canDisableEatAndDrink() && (noEatOrDrink = Boolean.parseBoolean(aAnswer.getProperty("noEatOrDrink"))) != this.obj.isNoEatOrDrink()) {
                this.obj.setIsNoEatOrDrink(noEatOrDrink);
                change = true;
                if (noEatOrDrink) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be eaten or drunk.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can be eaten or drunk.");
                }
            }
            if (this.obj.canDisableTake() && (noTake = Boolean.parseBoolean(aAnswer.getProperty("noTake"))) != this.obj.isNoTake()) {
                this.obj.setIsNoTake(noTake);
                change = true;
                if (noTake) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be taken.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can be taken.");
                }
            }
            if (this.obj.canDisableSpellTarget() && (noSpells = Boolean.parseBoolean(aAnswer.getProperty("noSpells"))) != this.obj.isNotSpellTarget()) {
                this.obj.setIsNotSpellTarget(noSpells);
                change = true;
                if (noSpells) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be target for spells.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can be target for spells.");
                }
            }
            if (this.obj.canDisableDestroy() && (noDestroy = Boolean.parseBoolean(aAnswer.getProperty("noDestroy"))) != this.obj.isIndestructible()) {
                this.obj.setIsIndestructible(noDestroy);
                change = true;
                if (noDestroy) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is now indestructible.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be bashed/destroyed.");
                }
            }
            if (this.obj.canDisableLocking() && (noLock = Boolean.parseBoolean(aAnswer.getProperty("noLock"))) != this.obj.isNotLockable()) {
                this.obj.setIsNotLockable(noLock);
                change = true;
                if (noLock) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is now not lockable.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is now lockable.");
                }
            }
            if (this.obj.canDisableLockpicking() && (noLockpick = Boolean.parseBoolean(aAnswer.getProperty("noLockpick"))) != this.obj.isNotLockpickable()) {
                this.obj.setIsNotLockpickable(noLockpick);
                change = true;
                if (noLockpick) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be lockpicked.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be lockpicked.");
                }
            }
            if (this.obj.canDisableMoveable() && (noMove = Boolean.parseBoolean(aAnswer.getProperty("noMove"))) != this.obj.isNoMove()) {
                this.obj.setIsNoMove(noMove);
                change = true;
                if (noMove) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be moved.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be moved.");
                }
            }
            if (this.obj.canDisableTurning() && (noTurn = Boolean.parseBoolean(aAnswer.getProperty("noTurn"))) != this.obj.isNotTurnable()) {
                this.obj.setIsNotTurnable(noTurn);
                change = true;
                if (noTurn) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be turned.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be turned.");
                }
            }
            if (this.obj.canDisableOwnerMoveing() && (ownerMove = Boolean.parseBoolean(aAnswer.getProperty("ownerMove"))) != this.obj.isOwnerMoveable()) {
                this.obj.setIsOwnerMoveable(ownerMove);
                change = true;
                if (ownerMove) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be moved by owner.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be moved by owner.");
                }
            }
            if (this.obj.canDisableOwnerTurning() && (ownerTurn = Boolean.parseBoolean(aAnswer.getProperty("ownerTurn"))) != this.obj.isOwnerTurnable()) {
                this.obj.setIsOwnerTurnable(ownerTurn);
                change = true;
                if (ownerTurn) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be turned by owner.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be turned by owner.");
                }
            }
            if (this.obj.canDisablePainting() && (noPaint = Boolean.parseBoolean(aAnswer.getProperty("noPaint"))) != this.obj.isNotPaintable()) {
                this.obj.setIsNotPaintable(noPaint);
                change = true;
                if (noPaint) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be painted.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be painted.");
                }
            }
            if (this.obj.canDisableRuneing() && (noRunes = Boolean.parseBoolean(aAnswer.getProperty("noRunes"))) != this.obj.isNotRuneable()) {
                this.obj.setIsNotRuneable(noRunes);
                change = true;
                if (noRunes) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be runed.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be runed.");
                }
            }
            if (this.obj.canDisableImprove() && (noImprove = Boolean.parseBoolean(aAnswer.getProperty("noImprove"))) != this.obj.isNoImprove()) {
                this.obj.setIsNoImprove(noImprove);
                change = true;
                if (noImprove) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be improved.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be improved.");
                }
            }
            if (this.obj.canDisableRepair() && (noRepair = Boolean.parseBoolean(aAnswer.getProperty("noRepair"))) != this.obj.isNoRepair()) {
                this.obj.setIsNoRepair(noRepair);
                change = true;
                if (noRepair) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be repaired.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be repaired.");
                }
            }
            if (this.obj.canDisableDrag() && (noDrag = Boolean.parseBoolean(aAnswer.getProperty("noDrag"))) != this.obj.isNoDrag()) {
                this.obj.setIsNoDrag(noDrag);
                change = true;
                if (noDrag) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be dragged.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be dragged.");
                }
            }
            if (this.obj.canDisableDrop() && (noDrop = Boolean.parseBoolean(aAnswer.getProperty("noDrop"))) != this.obj.isNoDrop()) {
                this.obj.setIsNoDrop(noDrop);
                change = true;
                if (noDrop) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot be dropped.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now be dropped.");
                }
            }
            if (this.obj.canDisableRepair() && (noPut = Boolean.parseBoolean(aAnswer.getProperty("noPut"))) != this.obj.isNoPut()) {
                this.obj.setIsNoPut(noPut);
                change = true;
                if (noPut) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "cannot have items put in it.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "can now have items put in it.");
                }
            }
            if (this.obj.canBePlanted() && (plant = Boolean.parseBoolean(aAnswer.getProperty("planted"))) != this.obj.isPlanted()) {
                Item theTarget;
                if (this.obj instanceof Item && !plant && ((Item)this.obj).isRoadMarker() && Routes.isMarkerUsed(theTarget = (Item)this.obj)) {
                    theTarget.setWhatHappened("unplanted by " + this.getResponder().getName());
                }
                this.obj.setIsPlanted(plant);
                change = true;
                if (plant) {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is now planted.");
                } else {
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "is no longer planted.");
                }
            }
            if (change) {
                this.obj.savePermissions();
            }
            if ((val = aAnswer.getProperty("ql")) != null && val.length() > 0) {
                try {
                    double newQL = Double.parseDouble(val);
                    if (newQL < 1.0 || newQL > 100.0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("QL " + newQL + " Out of range (1-100).");
                    } else {
                        this.obj.setQualityLevel((float)newQL);
                        this.obj.setOriginalQualityLevel((float)newQL);
                        change = true;
                        this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "now has QL of " + newQL + ".");
                    }
                }
                catch (NumberFormatException nfe) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Failed to interpret " + val + " as a number for QL.");
                }
            }
            if ((val = aAnswer.getProperty("dmg")) != null && val.length() > 0) {
                try {
                    double newDMG = Double.parseDouble(val);
                    if (newDMG < 0.0 || newDMG >= 100.0) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("DMG " + newDMG + " Out of range (0 upto 100).");
                    } else {
                        this.obj.setDamage((float)newDMG);
                        change = true;
                        this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "now has Damage of " + newDMG + ".");
                    }
                }
                catch (NumberFormatException nfe) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Failed to interpret " + val + " as a number for damage.");
                }
            }
            if (this.obj.canChangeCreator() && (val = aAnswer.getProperty("creator")) != null && val.length() > 0) {
                if (LoginHandler.containsIllegalCharacters(val)) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("New creator (" + val + ") contains illegial characters.");
                } else if ((val = LoginHandler.raiseFirstLetter(val)).length() < 3) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("New creator (" + val + ") is too short (min 3 chars).");
                } else if (!val.equalsIgnoreCase(this.obj.getCreatorName())) {
                    this.obj.setCreator(val);
                    change = true;
                    this.getResponder().getCommunicator().sendSafeServerMessage(itemName + "now has creator of " + val + ".");
                }
            }
            if (!change) {
                if (spellremoved && !spelladded) {
                    this.getResponder().getCommunicator().sendNormalServerMessage(itemName + "has been disenchanted");
                } else if (!spellremoved) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("nothing changed");
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        int pow = 50;
        Spell spell = null;
        if (this.obj.canHaveCourier()) {
            byte ench;
            SpellEffect eff;
            if (this.obj.hasCourier()) {
                spell = Spells.getSpell(338);
            } else if (this.obj.hasDarkMessenger()) {
                spell = Spells.getSpell(339);
            }
            if (spell != null && (eff = ((Item)this.obj).getSpellEffect(ench = spell.getEnchantment())) != null) {
                pow = (int)eff.getPower();
            }
        }
        String blank = "image{src=\"img.gui.bridge.blank\";size=\"160,2\";text=\"\"}";
        buf.append("text{text=\"\"}");
        buf.append("label{type=\"bold\";text=\"Attributes / Restrictions\"}");
        buf.append("table{rows=\"5\";cols=\"4\";");
        buf.append(ItemRestrictionManagement.permission("alwaysLit", this.obj.isAlwaysLit(), "Always Lit", this.obj.canBeAlwaysLit(), "Ticked if item will always be lit.", "Item is always lit.", "Item cannot be always lit."));
        buf.append(ItemRestrictionManagement.permission("autofill", this.obj.isAutoFilled(), "Auto-Fill", this.obj.canBeAutoFilled(), "Ticked if item will auto-fill. Wells or Fountains (only).", "Item is always Auto-Filled.", "Item cannot be Auto-Filled."));
        buf.append(ItemRestrictionManagement.permission("autolight", this.obj.isAutoLit(), "Auto-Lights", this.obj.canBeAutoLit(), "Ticked if item will auto-light.", "Item is always Auto-Lit.", "Item cannot be Auto-Lit."));
        buf.append(ItemRestrictionManagement.permission("noDecay", this.obj.hasNoDecay(), "Decay Disabled", this.obj.canDisableDecay(), "Tick to disable decay on this item.", "Item always has decay disabled.", "Item cannot have decay disabled."));
        buf.append(ItemRestrictionManagement.permission("noDestroy", this.obj.isIndestructible(), "No Destroy / Bash", this.obj.canDisableDestroy(), "Tick to disallow bashing/destroying of this item.", "Item is always Indestructable.", "Item cannot be made Industrictable."));
        buf.append(ItemRestrictionManagement.permission("noDrag", this.obj.isNoDrag(), "No Drag", this.obj.canDisableDrag(), "Tick to disallow dragging of this item.", "Item is always not draggable.", "Item cannot be made not draggable."));
        buf.append(ItemRestrictionManagement.permission("noDrop", this.obj.isNoDrop(), "No Drop", this.obj.canDisableDrop(), "Tick to disallow dropping of this item.", "Item is always not droppable.", "Item cannot be made not droppable."));
        buf.append(ItemRestrictionManagement.permission("noEatOrDrink", this.obj.isNoEatOrDrink(), "No Eat Or Drink", this.obj.canDisableEatAndDrink(), "Tick to disallow eating and drinking of this item.", "Item is always not eatable or drinkable.", "Item cannot be made not eatable or drinkable."));
        buf.append(ItemRestrictionManagement.permission("noPut", this.obj.isNoPut(), "No Put", this.obj.canDisablePut(), "Tick to disallow putting items in this item.", "Item is always noPut.", "Item cannot be made noPut."));
        buf.append(ItemRestrictionManagement.permission("noSpells", this.obj.isNotSpellTarget(), "No Spells", this.obj.canDisableSpellTarget(), "Tick to disallow spells to be cast on this item.", "Item cannot accept spells anyway.", "Item cannot be made to not be a spell target."));
        buf.append(ItemRestrictionManagement.permission("noTake", this.obj.isNoTake(), "No Take", this.obj.canDisableTake(), "Tick to override the No Take.", "Item is always No Take.", "Item cannot be made No Take."));
        buf.append(ItemRestrictionManagement.permission("noImprove", this.obj.isNoImprove(), "Not Improvable", this.obj.canDisableImprove(), "Tick to disallow improving of this item.", "Item is always not improvable.", "Item cannot be made not improvable."));
        buf.append(ItemRestrictionManagement.permission("noLock", this.obj.isNotLockable(), "Not Lockable", this.obj.canDisableLocking(), "Tick to disallow locking of this item.", "Item is always not lockable.", "Item cannot be made not lockable."));
        buf.append(ItemRestrictionManagement.permission("noLockpick", this.obj.isNotLockpickable(), "Not Lockpickable", this.obj.canDisableLockpicking(), "Tick to disallow lockpicking of this item.", "Item is always not lockpickable.", "Item cannot be made not-lockpickable (cannot have a lock?)"));
        buf.append(ItemRestrictionManagement.permission("noMove", this.obj.isNoMove(), "Not Moveable", this.obj.canDisableMoveable(), "Tick to disallow movement of this item.", "Item is always not moveable.", "Item cannot be made not moveable."));
        buf.append(ItemRestrictionManagement.permission("noPaint", this.obj.isNotPaintable(), "Not Paintable", this.obj.canDisablePainting(), "Tick to disallow painting of this item.", "Item is always not paintable.", "Item cannot be made not paintable."));
        buf.append(ItemRestrictionManagement.permission("noRepair", this.obj.isNoRepair(), "Not Repairable", this.obj.canDisableRepair(), "Tick to disallow repairing of this item.", "Item is always not repairable.", "Item cannot be made not repairable."));
        buf.append(ItemRestrictionManagement.permission("noRunes", this.obj.isNotRuneable(), "Not Runeable", this.obj.canDisableRuneing(), "Tick to disallow runeing of this item.", "Item is always not runeable.", "Item cannot be made not runeable."));
        buf.append(ItemRestrictionManagement.permission("noTurn", this.obj.isNotTurnable(), "Not Turnable", this.obj.canDisableTurning(), "Tick to disallow turning of this item.", "Item is always not turnable.", "Item cannot be made not turnable."));
        buf.append(ItemRestrictionManagement.permission("ownerTurn", this.obj.isOwnerTurnable(), "Owner Turnable", this.obj.canDisableOwnerTurning(), "Tick to disallow owner turning of this item.", "Item is always not turnable by owner.", "Item cannot be made not turnable by owner."));
        buf.append(ItemRestrictionManagement.permission("ownerMove", this.obj.isOwnerMoveable(), "Owner Moveable", this.obj.canDisableOwnerMoveing(), "Tick to disallow owner moveing of this item.", "Item is always not moveable by owner.", "Item cannot be made not moveable by owner."));
        buf.append(ItemRestrictionManagement.permission("planted", this.obj.isPlanted(), "Planted", this.obj.canBePlanted(), "Ticked if item is planted.", "Item is always planted.", "Item cannot be planted."));
        buf.append(ItemRestrictionManagement.permission("sealed", this.obj.isSealedByPlayer(), "Sealed", this.obj.canBeSealedByPlayer() || this.obj.canBePeggedByPlayer() || this.canGMSeal(), "Ticked if item is sealed by player.", "Item is always sealed.", "Item cannot be sealed by player."));
        buf.append("label{text=\"\"}");
        buf.append(ItemRestrictionManagement.permission("courier", this.obj.hasCourier(), "Has Courier", this.obj.canHaveCourier(), "Ticked it Courier has been cast on it.", "Item always has courier.", "Item cannot have courier."));
        buf.append(ItemRestrictionManagement.permission("darkmessenger", this.obj.hasDarkMessenger(), "Has Dark Messenger", this.obj.canHaveDakrMessenger(), "Ticked if Dark Messenger has been cast on it.", "Item always has dark messeneger.", "Item cannot have dark messenger."));
        buf.append("harray{label{text=\"Power=\"};");
        if (this.obj.canHaveCourier()) {
            buf.append("input{id=\"pow\";maxchars=\"3\";text=\"" + pow + "\"}");
        } else {
            buf.append("label{text=\"na\"}");
        }
        buf.append("}");
        buf.append("label{text=\"\"}");
        buf.append("image{src=\"img.gui.bridge.blank\";size=\"160,2\";text=\"\"}image{src=\"img.gui.bridge.blank\";size=\"160,2\";text=\"\"}image{src=\"img.gui.bridge.blank\";size=\"160,2\";text=\"\"}image{src=\"img.gui.bridge.blank\";size=\"160,2\";text=\"\"}");
        buf.append("}");
        buf.append("label{text=\"Update QL (range 1-100) and/or damage (range 0-99).\"};");
        buf.append("label{type=\"talics\";text=\"leave blank for no change.\"};");
        buf.append("table{rows=\"2\";cols=\"4\";");
        buf.append("label{text=\"QL:\"};label{text=\"" + this.obj.getQualityLevel() + "\"};input{id=\"ql\";maxchars=\"10\"};label{text=\"(1-100)\"}");
        buf.append("label{text=\"DMG:\"};label{text=\"" + this.obj.getDamage() + "\"};input{id=\"dmg\";maxchars=\"10\"};label{text=\"(0 upto 100)\"}");
        if (this.obj.canChangeCreator()) {
            buf.append("label{text=\"Creator:\"};label{text=\"" + this.obj.getCreatorName() + "\"};input{id=\"creator\";maxchars=\"40\"};label{text=\"(3-40 chars)\"}");
        }
        buf.append("}");
        buf.append("label{text=\"\"};");
        buf.append("harray{button{text=\"Update Restrictions\";id=\"update\"}};");
        buf.append("label{text=\"\"};");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 380, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private static String permission(String sid, boolean selected, String text, boolean enabled, String hover, String always, String never) {
        if (enabled) {
            return "checkbox{id=\"" + sid + "\";selected=\"" + selected + "\";text=\"" + text + "\";hover=\"" + hover + "\"};";
        }
        return "harray{image{src=\"img.gui." + (selected ? "vsmall" : "xsmall") + "\";size=\"16,16\";text=\"" + hover + "\"}label{text=\"" + text + "\";hover=\"" + (selected ? always : never) + "\"};};";
    }

    private boolean canGMSeal() {
        if (WurmId.getType(this.target) == 2) {
            Item[] contains;
            Item item = (Item)this.obj;
            if (item.getTemplateId() == 768 && (contains = item.getItemsAsArray()).length == 2) {
                return contains[0].isLiquid() && contains[0].isFermenting() || contains[1].isLiquid() && contains[1].isFermenting();
            }
            if (item.getTemplateId() == 1309) {
                return true;
            }
        }
        return false;
    }
}

