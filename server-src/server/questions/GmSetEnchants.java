/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmSetEnchants
extends Question {
    private static final Logger logger = Logger.getLogger(GmSetEnchants.class.getName());
    private final Item item;
    private final Spell[] spells;

    public GmSetEnchants(Creature aResponder, Item aTarget) {
        super(aResponder, "Item Enchants", GmSetEnchants.itemNameWithDescription(aTarget), 104, aTarget.getWurmId());
        this.item = aTarget;
        this.spells = Spells.getSpellsEnchantingItems();
        Arrays.sort(this.spells);
    }

    private static String itemNameWithDescription(Item litem) {
        StringBuilder sb = new StringBuilder();
        String name = litem.getActualName().length() == 0 ? litem.getTemplate().getName() : litem.getActualName();
        MaterialUtilities.appendNameWithMaterialSuffix(sb, name, litem.getMaterial());
        if (litem.getDescription().length() > 0) {
            sb.append(" (" + litem.getDescription() + ")");
        }
        return "Enchants of " + sb.toString();
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 104 && this.getResponder().getPower() >= 4) {
            boolean somethingChanged = false;
            byte itemEnch = this.item.enchantment;
            for (int x = 0; x < this.spells.length; ++x) {
                boolean newsel = Boolean.parseBoolean(aAnswer.getProperty("newsel" + x));
                int newpow = 50;
                try {
                    newpow = Math.min(Integer.parseInt(aAnswer.getProperty("newpow" + x)), this.spells[x].getEnchantment() == 45 ? 10000 : 104);
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
                byte ench = this.spells[x].getEnchantment();
                SpellEffect eff = this.item.getSpellEffect(ench);
                boolean oldsel = false;
                int oldpow = 50;
                if (eff != null) {
                    oldsel = true;
                    oldpow = (int)eff.power;
                } else if (ench == itemEnch) {
                    oldsel = true;
                }
                if (newsel == oldsel && (!oldsel || newpow == oldpow)) continue;
                somethingChanged = true;
                if (oldsel) {
                    if (this.spells[x].singleItemEnchant) {
                        this.item.enchant((byte)0);
                    } else if (eff != null) {
                        this.item.getSpellEffects().removeSpellEffect(eff.type);
                    }
                }
                if (!newsel) continue;
                this.spells[x].castSpell((double)newpow, this.getResponder(), this.item);
                logger.log(Level.INFO, this.getResponder().getName() + " enchanting " + this.spells[x].getName() + " " + this.item.getName() + ", " + this.item.getWurmId() + ", " + newpow);
                this.getResponder().getLogger().log(Level.INFO, " enchanting " + this.spells[x].getName() + " " + this.item.getName() + ", " + this.item.getWurmId() + ", " + newpow);
            }
            if (somethingChanged) {
                GmSetEnchants gt = new GmSetEnchants(this.getResponder(), this.item);
                gt.sendQuestion();
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() >= 4) {
            byte itemEnch = this.item.enchantment;
            buf.append("table{rows=\"" + this.spells.length + "\";cols=\"4\";label{text=\"\"};text{type=\"bold\";text=\"Power\"};text{type=\"bold\";text=\"Name\"};text{type=\"bold\";text=\"Description\"};");
            for (int x = 0; x < this.spells.length; ++x) {
                byte ench = this.spells[x].getEnchantment();
                SpellEffect eff = this.item.getSpellEffect(ench);
                boolean sel = false;
                String pow = "";
                if (eff != null) {
                    sel = true;
                    pow = String.valueOf((int)eff.power);
                } else if (ench == itemEnch) {
                    sel = true;
                }
                int maxChars = ench == 45 ? 5 : 3;
                buf.append("checkbox{id=\"newsel" + x + "\";selected=\"" + sel + "\"};" + (this.spells[x].singleItemEnchant ? "text{type=\"italic\";text=\"(none)\"};" : "input{id=\"newpow" + x + "\";maxchars=\"" + maxChars + "\";text=\"" + pow + "\"};") + "label{text=\"" + this.spells[x].getName() + "\"};label{text=\"" + this.spells[x].getDescription() + "\"};");
            }
            buf.append("}");
            buf.append("label{text=\"\"};");
            buf.append("text{type=\"bold\";text=\"--------------- Help -------------------\"}");
            buf.append("text{text=\"Can add or change or remove enchants to specific powers, it maybe necessary to remove an enchant before modifying its power. If the enchant requires a power, then if none is specified it will default to 50, also \"}");
            buf.append("text{text=\"Note: Checks to see if the item can have the enchantment are not performed.\"}");
            buf.append("text{text=\"If anything is changed, then once the change is applied it will show this screen again.\"}");
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(500, 500, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

