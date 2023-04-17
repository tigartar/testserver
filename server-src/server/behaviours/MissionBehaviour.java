/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import java.util.LinkedList;
import java.util.List;

public class MissionBehaviour
extends Behaviour {
    public MissionBehaviour() {
        super((short)43);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int missionId) {
        return this.getBehavioursFor(performer, missionId);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, int missionId) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.add(Actions.actionEntrys[1]);
        toReturn.add(Actions.actionEntrys[16]);
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, int missionId, short action, float counter) {
        if (action == 1) {
            performer.getCommunicator().sendNormalServerMessage("This displays the state of a mission.");
        }
        if (action == 16) {
            MissionPerformer mp = MissionPerformed.getMissionPerformer(performer.getWurmId());
            MissionPerformed mpf = mp.getMission(missionId);
            mpf.setInactive(true);
        }
        return true;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, int missionId, short action, float counter) {
        return this.action(act, performer, missionId, action, counter);
    }
}

