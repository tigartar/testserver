/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import java.util.LinkedList;
import java.util.List;

public class SkillBehaviour
extends Behaviour {
    public SkillBehaviour() {
        super((short)42);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Skill skill) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Skill skill) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, Skill skill, short action, float counter) {
        return this.action(act, performer, skill, action, counter);
    }

    @Override
    public boolean action(Action act, Creature performer, Skill skill, short action, float counter) {
        if (action == 1) {
            performer.getCommunicator().sendNormalServerMessage("This is the skill " + skill.getName() + ". Use 'Find on Wurmpedia' to see an explanation.");
        }
        return true;
    }
}

