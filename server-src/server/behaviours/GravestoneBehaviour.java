/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TextInputQuestion;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.List;

public final class GravestoneBehaviour
extends ItemBehaviour {
    public GravestoneBehaviour() {
        super((short)48);
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        if (action == 506) {
            return GravestoneBehaviour.readInscription(performer, target);
        }
        if (action == 177 || action == 178 || action == 181 || action == 99) {
            if (GravestoneBehaviour.canManipulateGrave(target, performer)) {
                return MethodsItems.moveItem(performer, target, counter, action, act);
            }
            performer.getCommunicator().sendNormalServerMessage("You may not push, pull or turn that item.");
            return true;
        }
        if (action == 1) {
            performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
            target.sendEnchantmentStrings(performer.getCommunicator());
            GravestoneBehaviour.sendInscription(performer, target);
            return true;
        }
        return super.action(act, performer, target, action, counter);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
        if (action == 505) {
            return GravestoneBehaviour.inscribe(performer, source, target);
        }
        if (action == 506) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 192) {
            if (target.creationState == 0) return MethodsItems.improveItem(act, performer, source, target, counter);
            int tid = MethodsItems.getItemForImprovement(target.getMaterial(), target.creationState);
            if (source.getTemplateId() != tid) return true;
            return MethodsItems.polishItem(act, performer, source, target, counter);
        }
        if (action == 83 || action == 180) {
            if (!performer.mayDestroy(target) && performer.getPower() < 2) return true;
            return MethodsItems.destroyItem(action, performer, source, target, false, counter);
        }
        if (action == 177 || action == 178 || action == 181 || action == 99 || action == 1) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 179) {
            GravestoneBehaviour.summon(performer, source, target);
            return true;
        }
        if (action == 91) {
            if (source.getTemplateId() != 176 && source.getTemplateId() != 315 || performer.getPower() < 2) return true;
            float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0f;
            performer.getStatus().refresh(nut, false);
            return true;
        }
        if (action != 503) return super.action(act, performer, source, target, action, counter);
        if (performer.getPower() < 2 || source.getTemplateId() != 176 && source.getTemplateId() != 315) return true;
        Methods.sendCreateZone(performer);
        return true;
    }

    private static final boolean canManipulateGrave(Item grave, Creature performer) {
        if (grave.lastOwner == performer.getWurmId()) {
            return true;
        }
        if (performer.getPower() >= 2) {
            return true;
        }
        VolaTile t = Zones.getTileOrNull(grave.getTileX(), grave.getTileY(), grave.isOnSurface());
        return t != null && t.getVillage() != null && t.getVillage().isCitizen(performer);
    }

    private static final boolean inscribe(Creature performer, Item chisel, Item gravestone) {
        if (chisel == null) {
            performer.getCommunicator().sendNormalServerMessage("You fumble with the " + chisel + " but you cannot figure out how it works.");
            return true;
        }
        InscriptionData inscriptionData = gravestone.getInscription();
        if (!gravestone.canHaveInscription()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot inscribe on that!");
            return true;
        }
        if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
            performer.getCommunicator().sendNormalServerMessage("This " + gravestone.getName() + " has already been inscribed by " + inscriptionData.getInscriber() + ".");
            return true;
        }
        int numberOfChars = (int)(gravestone.getQualityLevel() * 2.0f);
        TextInputQuestion tiq = new TextInputQuestion(performer, "Inscribing a message on " + gravestone.getName() + ".", "Inscribing is an irreversible process. Enter your important message here:", 2, gravestone.getWurmId(), numberOfChars, false);
        Server.getInstance().broadCastAction(performer.getName() + " starts to inscribe with " + chisel.getName() + " on " + gravestone.getNameWithGenus() + ".", performer, 5);
        tiq.sendQuestion();
        return true;
    }

    private static final boolean readInscription(Creature performer, Item gravestone) {
        InscriptionData inscriptionData = gravestone.getInscription();
        if (inscriptionData != null) {
            SimplePopup pp = new SimplePopup(performer, gravestone.getName(), inscriptionData.getInscription());
            performer.getCommunicator().sendNormalServerMessage("You read the " + gravestone.getName() + ".");
            pp.sendQuestion("Close");
        } else {
            performer.getCommunicator().sendNormalServerMessage("There was no inscription to read.");
        }
        return true;
    }

    private static void summon(Creature performer, Item wand, Item target) {
        int stid = wand.getTemplateId();
        if ((stid == 176 || stid == 315) && performer.getPower() >= 2) {
            try {
                Zone currZone = Zones.getZone((int)target.getPosX() >> 2, (int)target.getPosY() >> 2, target.isOnSurface());
                currZone.removeItem(target);
                target.putItemInfrontof(performer);
            }
            catch (NoSuchZoneException nsz) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the zone for that item. Failed to summon.");
            }
            catch (NoSuchCreatureException nsc) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
            }
            catch (NoSuchItemException nsi) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the item for that request! Failed to summon.");
            }
            catch (NoSuchPlayerException nsp) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
            }
        }
    }

    private static void sendInscription(Creature performer, Item gravestone) {
        String inscription;
        InscriptionData inscriptionData = gravestone.getInscription();
        if (inscriptionData != null && (inscription = inscriptionData.getInscription()).length() > 0) {
            performer.getCommunicator().sendNormalServerMessage("There is an inscription carved into the gravestone.");
            performer.getCommunicator().sendNormalServerMessage(inscription);
        }
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        InscriptionData inscriptionData;
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        if (target.getTemplateId() == 822 && (inscriptionData = target.getInscription()) != null && inscriptionData.hasBeenInscribed()) {
            toReturn.add(Actions.actionEntrys[506]);
        }
        if (toReturn.contains(Actions.actionEntrys[59]) && !GravestoneBehaviour.canManipulateGrave(target, performer)) {
            toReturn.remove(Actions.actionEntrys[59]);
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        if (target.getTemplateId() == 822) {
            InscriptionData inscriptionData = target.getInscription();
            if (source.getTemplateId() == 97 && target.canHaveInscription() && (inscriptionData == null || !inscriptionData.hasBeenInscribed() || performer.getPower() >= 2)) {
                toReturn.add(Actions.actionEntrys[505]);
            }
            if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
                toReturn.add(Actions.actionEntrys[506]);
            }
        }
        if (toReturn.contains(Actions.actionEntrys[59]) && !GravestoneBehaviour.canManipulateGrave(target, performer)) {
            toReturn.remove(Actions.actionEntrys[59]);
        }
        return toReturn;
    }
}

