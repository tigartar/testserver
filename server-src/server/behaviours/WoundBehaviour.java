/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

final class WoundBehaviour
extends Behaviour
implements ItemMaterials,
MiscConstants {
    private static final Logger logger = Logger.getLogger(Wound.class.getName());

    WoundBehaviour() {
        super((short)27);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Wound target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, target));
        if (performer.getCultist() != null && performer.getCultist().mayCleanWounds() && !target.isDrownWound()) {
            toReturn.add(Actions.actionEntrys[395]);
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Wound target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, source, target));
        if (!target.isDrownWound()) {
            if (source.getMaterial() == 17 && !target.isInternal()) {
                toReturn.add(Actions.actionEntrys[196]);
            } else if (source.isHealingSalve() && (target.isInternal() || target.isBruise() || target.isPoison())) {
                toReturn.add(Actions.actionEntrys[196]);
            }
            if (source.getTemplateId() == 481) {
                if (target.getType() != 10) {
                    toReturn.add(Actions.actionEntrys[284]);
                }
            } else if (source.getTemplateId() == 128 && target.getType() == 10) {
                toReturn.add(Actions.actionEntrys[284]);
            }
        }
        if (performer.getDeity() != null && source.isHolyItem(performer.getDeity()) && (performer.isPriest() || performer.getPower() > 0)) {
            float faith = performer.getFaith();
            Spell[] spells = performer.getDeity().getSpellsTargettingWounds((int)faith);
            if (spells.length > 0) {
                toReturn.add(new ActionEntry((short)(-spells.length), "Spells", "spells"));
                for (int x = 0; x < spells.length; ++x) {
                    toReturn.add(Actions.actionEntrys[spells[x].number]);
                }
            }
        }
        if (performer.getCultist() != null && performer.getCultist().mayCleanWounds() && !target.isDrownWound()) {
            toReturn.add(Actions.actionEntrys[395]);
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Wound target, short action, float counter) {
        boolean done = true;
        Creature owner = target.getCreature();
        if (owner != null && !performer.isWithinDistanceTo(owner.getPosX(), owner.getPosY(), owner.getPositionZ() + owner.getAltOffZ(), 8.0f)) {
            return true;
        }
        if (action == 1) {
            performer.getCommunicator().sendNormalServerMessage("You see " + target.getWoundString() + " at the " + performer.getBody().getWoundLocationString(target.getLocation()) + ".");
            if (target.getHealEff() > 0) {
                performer.getCommunicator().sendNormalServerMessage("It is covered with some healing plants (" + target.getHealEff() + ").");
            }
        } else if (action == 395) {
            if (target.getInfectionSeverity() <= 0.0f && target.getPoisonSeverity() <= 0.0f) {
                performer.getCommunicator().sendNormalServerMessage("The wound is not dirty or infected.");
            } else if (target.getInfectionSeverity() <= 0.0f && target.isInternal()) {
                performer.getCommunicator().sendNormalServerMessage("That wound is internal.");
            } else if (Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().mayCleanWounds()) {
                target.setInfectionSeverity(0.0f);
                target.setPoisonSeverity(0.0f);
                String targetName = target.getCreature().getNameWithGenus();
                if (target.getCreature().isPlayer()) {
                    targetName = target.getCreature().getName();
                }
                if (target.getCreature() == performer) {
                    Server.getInstance().broadCastAction(performer.getName() + " sucks on " + performer.getHisHerItsString() + " wounds.", performer, 5);
                } else {
                    target.getCreature().getCommunicator().sendNormalServerMessage(performer.getName() + " sucks on your wounds.");
                    Server.getInstance().broadCastAction(performer.getName() + " sucks on " + targetName + "'s wounds.", performer, target.getCreature(), 5);
                }
                performer.getCommunicator().sendNormalServerMessage("You gleefully clean the wound.");
                performer.getCultist().touchCooldown1();
            }
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Wound target, short action, float counter) {
        Spell spell;
        boolean done = true;
        Creature owner = target.getCreature();
        if (owner != null && !performer.isWithinDistanceTo(owner.getPosX(), owner.getPosY(), owner.getPositionZ() + owner.getAltOffZ(), 8.0f)) {
            return true;
        }
        if (action == 1) {
            return this.action(act, performer, target, action, counter);
        }
        if (action == 196) {
            if (!target.isDrownWound()) {
                return MethodsCreatures.firstAid(performer, source, target, counter, act);
            }
        } else if (action == 284) {
            if (!target.isDrownWound()) {
                return MethodsCreatures.treat(performer, source, target, counter, act);
            }
        } else if (action == 395) {
            if (!target.isDrownWound()) {
                return this.action(act, performer, target, action, counter);
            }
        } else if (act.isSpell() && (spell = Spells.getSpell(action)) != null && (spell.religious ? Methods.isActionAllowed(performer, (short)245) : Methods.isActionAllowed(performer, (short)547))) {
            return Methods.castSpell(performer, Spells.getSpell(action), target, act.getCounterAsFloat());
        }
        return true;
    }
}

