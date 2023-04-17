/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;

public final class LocateArtifact
extends ReligiousSpell {
    public static final int RANGE = 4;

    LocateArtifact() {
        super("Locate Artifact", 271, 30, 70, 70, 80, 1800000L);
        this.targetTile = true;
        this.description = "locates hidden artifacts";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        if (performer.getPower() > 0 && performer.getPower() < 5) {
            performer.getCommunicator().sendNormalServerMessage("You may not cast this spell.", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage(EndGameItems.locateRandomEndGameItem(performer));
    }
}

