/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Vessle
extends ReligiousSpell {
    private static final Logger logger = Logger.getLogger(Vessle.class.getName());
    public static final int RANGE = 4;

    Vessle() {
        super("Vessel", 272, 30, 5, 70, 31, 0L);
        this.targetItem = true;
        this.description = "stores favor in a gem";
        this.type = 0;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Item target) {
        if (target.isGem()) {
            if (target.isSource()) {
                performer.getCommunicator().sendNormalServerMessage("This gem can hold no more power.", (byte)3);
                return false;
            }
            if (target.getData1() > 0) {
                performer.getCommunicator().sendNormalServerMessage("The gem can hold no more power right now.", (byte)3);
                return false;
            }
            if (performer.isRoyalPriest()) {
                if (performer.getFavor() - (float)this.cost / 2.0f < 5.0f) {
                    performer.getCommunicator().sendNormalServerMessage("You have too little favor left to store.", (byte)3);
                    return false;
                }
            } else if ((performer.getFavor() - (float)this.cost) * 2.0f < 5.0f) {
                performer.getCommunicator().sendNormalServerMessage("You have too little favor left to store.", (byte)3);
                return false;
            }
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("You need something pure and beautiful.", (byte)3);
        return false;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, Creature target) {
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        if (target.isGem()) {
            if (target.getQualityLevel() > 10.0f) {
                int favorStored = (int)Math.min(performer.getFavor() * 2.0f, target.getQualityLevel() * 2.0f);
                if (favorStored > 5) {
                    float qlMod = (float)target.getRarity() + 1.0f;
                    target.setData1((int)((float)favorStored * qlMod));
                    try {
                        performer.setFavor(performer.getFavor() - (float)favorStored / 2.0f);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, performer.getName() + ":" + iox.getMessage(), iox);
                    }
                    if (performer.getDeity().getNumber() == 4) {
                        performer.getCommunicator().sendNormalServerMessage("You fill the gem with the power of your hate.", (byte)2);
                    } else if (performer.getDeity().getNumber() == 3) {
                        performer.getCommunicator().sendNormalServerMessage("You fill the gem with the power of your determination.", (byte)2);
                    } else if (performer.getDeity().getNumber() == 2) {
                        performer.getCommunicator().sendNormalServerMessage("You fill the gem with the power of your rage.", (byte)2);
                    } else if (performer.getDeity().getNumber() == 1) {
                        performer.getCommunicator().sendNormalServerMessage("You fill the gem with the power of your love.", (byte)2);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You fill the gem with the power of your devotion.", (byte)2);
                    }
                    performer.achievement(614);
                } else {
                    target.setQualityLevel(target.getQualityLevel() - 3.0f + (float)target.getRarity());
                    performer.getCommunicator().sendNormalServerMessage("You fail to store any favor but the " + target.getName() + " is damaged in the process.", (byte)3);
                }
            } else {
                target.setQualityLevel(target.getQualityLevel() - 3.0f);
                performer.getCommunicator().sendNormalServerMessage("The gem is of too low quality to store any power and is damaged a bit.", (byte)3);
            }
        }
    }
}

