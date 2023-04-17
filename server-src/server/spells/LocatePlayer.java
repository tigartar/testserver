/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.LocatePlayerQuestion;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;

public class LocatePlayer
extends ReligiousSpell {
    public static final int RANGE = 40;

    public LocatePlayer() {
        super("Locate Soul", 419, 10, 20, 10, 20, 120000L);
        this.targetCreature = true;
        this.targetItem = true;
        this.targetTile = true;
        this.description = "locates a player and corpses";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        LocatePlayerQuestion lpq = new LocatePlayerQuestion(performer, "Locate a soul", "Which soul do you wish to locate?", performer.getWurmId(), false, power);
        lpq.sendQuestion();
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        LocatePlayerQuestion lpq = new LocatePlayerQuestion(performer, "Locate a soul", "Which soul do you wish to locate?", performer.getWurmId(), false, power);
        lpq.sendQuestion();
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        LocatePlayerQuestion lpq = new LocatePlayerQuestion(performer, "Locate a soul", "Which soul do you wish to locate?", performer.getWurmId(), false, power);
        lpq.sendQuestion();
    }
}

