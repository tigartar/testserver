/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.KarmaSpell;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class Disease
extends KarmaSpell {
    public static final int RANGE = 24;

    public Disease() {
        super("Disease", 547, 10, 1000, 10, 10, 300000L);
        this.targetCreature = true;
        this.offensive = true;
        this.description = "diseases creatures and players";
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isLibila() && performer.faithful && !performer.isDuelOrSpar(target)) {
            performer.getCommunicator().sendNormalServerMessage(performer.getDeity().getName() + " would never accept your attack on " + target.getNameWithGenus() + ".", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        int baseDamage = 12000;
        int damage = (int)(12000.0 + 8000.0 * (power / 100.0));
        int diseaseGained = (int)Math.max(power / 2.0, 10.0);
        if (!target.isUnique() || power > 99.0) {
            VolaTile targetVolaTile;
            if ((target.isHuman() || target.isDominated()) && target.getAttitude(performer) != 2 && !performer.getDeity().isLibila() && !performer.isDuelOrSpar(target)) {
                performer.modifyFaith(-(100.0f - performer.getFaith()) / 50.0f);
            }
            target.setDisease((byte)Math.min(90, target.getDisease() + diseaseGained));
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " diseases you.", (byte)4);
            if (target.isPlayer()) {
                double defensiveRoll = target.getSoulStrength().skillCheck(power, 0.0, false, 10.0f);
                if (defensiveRoll <= 50.0) {
                    if (defensiveRoll > 0.0) {
                        damage = (int)((float)damage * 0.5f);
                    }
                    if (performer.getPower() > 1 && Servers.isThisATestServer()) {
                        performer.getCommunicator().sendNormalServerMessage("Damage done: " + damage, (byte)2);
                    }
                    target.addAttacker(performer);
                    target.addWoundOfType(performer, (byte)6, 1, true, 1.0f, false, damage, (float)diseaseGained / 2.5f, 0.0f, false, true);
                } else {
                    String tMessage = "You resisted the '" + this.name + "' spell.";
                    String pMessage = target.getNameWithGenus() + " resisted your '" + this.name + "' spell.";
                    target.getCommunicator().sendCombatNormalMessage(tMessage, (byte)4);
                    performer.getCommunicator().sendCombatNormalMessage(pMessage, (byte)4);
                }
            }
            if ((targetVolaTile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface())) != null) {
                targetVolaTile.sendAttachCreatureEffect(target, (byte)12, (byte)0, (byte)0, (byte)0, (byte)0);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You try to disease " + target.getNameWithGenus() + " but fail.", (byte)3);
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to disease you but fails.", (byte)4);
        }
    }

    @Override
    void doNegativeEffect(Skill castSkill, double power, Creature performer, Creature target) {
        performer.getCommunicator().sendNormalServerMessage("You try to cast karma disease on " + target.getNameWithGenus() + " but fail.", (byte)3);
        target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " tries to cast karma disease on you but fails.", (byte)4);
    }
}

