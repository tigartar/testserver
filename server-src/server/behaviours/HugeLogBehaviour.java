/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import java.util.LinkedList;
import java.util.List;

final class HugeLogBehaviour
extends ItemBehaviour {
    HugeLogBehaviour() {
        super((short)37);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, source, target));
        boolean reachable = false;
        if (target.getOwnerId() == -10L) {
            BlockingResult result;
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f) && (result = Blocking.getBlockerBetween(performer, target, 4)) == null) {
                reachable = true;
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
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0f)) {
                reachable = true;
            }
        } else if (target.getOwnerId() == performer.getWurmId()) {
            reachable = true;
        }
        done = reachable ? (action == 97 ? MethodsItems.chop(act, performer, source, target, counter) : super.action(act, performer, source, target, action, counter)) : super.action(act, performer, source, target, action, counter);
        return done;
    }
}

