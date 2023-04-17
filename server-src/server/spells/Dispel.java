/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.zones.AreaSpellEffect;

public class Dispel
extends ReligiousSpell {
    public static final int RANGE = 24;

    public Dispel() {
        super("Dispel", 450, 5, 10, 20, 10, 0L);
        this.targetTile = true;
        this.targetItem = true;
        this.targetCreature = true;
        this.description = "dispels an effect on the target";
        this.type = 0;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        SpellEffect[] speffs;
        if (performer != target) {
            target.getCommunicator().sendCombatNormalMessage(performer.getNameWithGenus() + " dispels you!");
        }
        if (target.getSpellEffects() == null) {
            performer.getCommunicator().sendCombatNormalMessage(String.format("%s has no effects to dispel.", target.getName()));
            return;
        }
        SpellEffects effs = target.getSpellEffects();
        for (SpellEffect speff : speffs = effs.getEffects()) {
            if (speff.type == 64 || speff.type == 74 || speff.type == 73 || speff.type == 75) continue;
            double resistance = SpellResist.getSpellResistance(target, this.getNumber());
            double powerToRemove = power * resistance;
            SpellResist.addSpellResistance(target, this.getNumber(), powerToRemove);
            if (powerToRemove < 1.0) {
                performer.getCommunicator().sendCombatNormalMessage(String.format("%s resists your dispel!", target.getName()));
                return;
            }
            if (powerToRemove >= (double)speff.getPower()) {
                effs.removeSpellEffect(speff);
                if (speff.type == 22 && target.getCurrentTile() != null) {
                    target.getCurrentTile().setNewRarityShader(target);
                }
                performer.getCommunicator().sendCombatNormalMessage(String.format("You completely dispel %s from %s!", speff.getName(), target.getName()));
                return;
            }
            speff.setPower((float)((double)speff.getPower() - powerToRemove));
            performer.getCommunicator().sendCombatNormalMessage(String.format("You partially dispel the %s from %s!", speff.getName(), target.getName()));
            return;
        }
        performer.getCommunicator().sendCombatNormalMessage(String.format("%s has no effects to dispel.", target.getName()));
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Item target) {
        SpellEffect[] speffs;
        if (!Dispel.mayBeEnchanted(target)) {
            performer.getCommunicator().sendNormalServerMessage("The protection force is too high. You fail to dispel the " + target.getName() + ".", (byte)3);
            return;
        }
        if (!(target.getOwnerId() == performer.getWurmId() || target.getOwnerId() == -10L && target.getLastOwnerId() == performer.getWurmId() || performer.mayDestroy(target))) {
            performer.getCommunicator().sendNormalServerMessage("The protection force is too high. You fail to dispel the " + target.getName() + ".", (byte)3);
            return;
        }
        ItemSpellEffects effs = target.getSpellEffects();
        if ((effs == null || effs.getEffects().length == 0) && target.enchantment == 0) {
            performer.getCommunicator().sendNormalServerMessage("Nothing seems to happen as you dispel " + target.getName() + ".", (byte)3);
            return;
        }
        if (target.enchantment != 0) {
            String enchName = "";
            int enchDifficulty = 60;
            Spell ench = Spells.getEnchantment(target.enchantment);
            if (ench != null) {
                enchName = ench.getName();
                enchDifficulty = ench.getDifficulty(true);
            } else {
                switch (target.enchantment) {
                    case 90: {
                        enchName = "acid damage";
                        break;
                    }
                    case 91: {
                        enchName = "fire damage";
                        break;
                    }
                    case 92: {
                        enchName = "frost damage";
                    }
                }
            }
            if (enchName != "") {
                if ((double)Server.rand.nextInt(enchDifficulty + 100) < power) {
                    target.enchant((byte)0);
                    performer.getCommunicator().sendNormalServerMessage("You remove the " + enchName + " enchantment.", (byte)2);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Nothing seems to happen as you dispel " + target.getName() + ".", (byte)3);
                }
                return;
            }
        }
        if (effs != null && (speffs = effs.getEffects()).length > 0) {
            Spell ench = Spells.getEnchantment(speffs[0].type);
            if (ench == null && (double)Server.rand.nextInt(20 + (int)speffs[0].getPower()) < power) {
                performer.getCommunicator().sendNormalServerMessage("You remove the " + speffs[0].getName() + " imbuement.", (byte)2);
                effs.removeSpellEffect(speffs[0].type);
            } else if (ench != null && (double)Server.rand.nextInt(ench.getDifficulty(true) + (int)speffs[0].getPower()) < power) {
                performer.getCommunicator().sendNormalServerMessage("You remove the " + ench.getName() + " enchantment.", (byte)2);
                effs.removeSpellEffect(speffs[0].type);
                if (target.isEnchantedTurret() && (speffs[0].type == 20 || speffs[0].type == 44)) {
                    target.setTemplateId(934);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("Nothing seems to happen as you dispel " + target.getName() + ".", (byte)3);
            }
            return;
        }
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        AreaSpellEffect.removeAreaEffect(tilex, tiley, layer);
    }
}

