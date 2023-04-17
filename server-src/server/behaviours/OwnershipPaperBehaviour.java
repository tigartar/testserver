/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

final class OwnershipPaperBehaviour
extends ItemBehaviour {
    private static final Logger logger = Logger.getLogger(OwnershipPaperBehaviour.class.getName());

    OwnershipPaperBehaviour() {
        super((short)52);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
        toReturn.addAll(this.getBehavioursForPaper());
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
        List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
        toReturn.addAll(this.getBehavioursForPaper());
        return toReturn;
    }

    List<ActionEntry> getBehavioursForPaper() {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.add(new ActionEntry(17, "Read paper", "Reading"));
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item target, short action, float counter) {
        boolean done = true;
        if (action == 1 && target.getTemplateId() == 1000) {
            performer.getCommunicator().sendNormalServerMessage("This is the writ of ownership. It can be traded with another player to transfer ownership.");
        } else if (action != 17) {
            done = super.action(act, performer, target, action, counter);
        }
        return done;
    }
}

