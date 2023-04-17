/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;

public class LightToken
extends ReligiousSpell {
    public static final int RANGE = 4;

    public LightToken() {
        super("Light Token", 421, 10, 5, 10, 20, 0L);
        this.targetItem = true;
        this.targetTile = true;
        this.targetCreature = true;
        this.description = "creates a bright light item";
        this.type = 0;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        this.createToken(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        this.createToken(performer, power);
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        this.createToken(performer, power);
    }

    void createToken(Creature performer, double power) {
        try {
            Item token = ItemFactory.createItem(649, (float)Math.max(50.0, power), performer.getName());
            performer.getInventory().insertItem(token);
            performer.getCommunicator().sendNormalServerMessage("Something starts shining in your pocket.", (byte)2);
        }
        catch (FailedException | NoSuchTemplateException wurmServerException) {
            // empty catch block
        }
    }
}

