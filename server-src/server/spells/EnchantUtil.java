/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.shared.constants.Enchants;
import java.util.ArrayList;
import java.util.logging.Logger;

public class EnchantUtil
implements Enchants {
    protected static final Logger logger = Logger.getLogger(EnchantUtil.class.getName());
    public static ArrayList<ArrayList<Byte>> enchantGroups = new ArrayList();

    public static float getDemiseBonus(Item item, Creature defender) {
        if (item.enchantment != 0 && (item.enchantment == 11 ? defender.isAnimal() && !defender.isUnique() : (item.enchantment == 9 ? defender.isPlayer() || defender.isHuman() : (item.enchantment == 10 ? defender.isMonster() && !defender.isUnique() : item.enchantment == 12 && defender.isUnique())))) {
            return 0.03f;
        }
        return 0.0f;
    }

    public static float getJewelryDamageIncrease(Creature attacker, byte woundType) {
        byte jewelryEnchant = 0;
        switch (woundType) {
            case 4: {
                jewelryEnchant = 2;
                break;
            }
            case 5: {
                jewelryEnchant = 1;
                break;
            }
            case 8: {
                jewelryEnchant = 3;
                break;
            }
            case 10: {
                jewelryEnchant = 4;
            }
        }
        if (jewelryEnchant == 0) {
            return 1.0f;
        }
        Item[] bodyItems = attacker.getBody().getContainersAndWornItems();
        float damageMultiplier = 1.0f;
        float totalPower = 0.0f;
        int activeJewelry = 0;
        for (Item bodyItem : bodyItems) {
            if (!bodyItem.isEnchantableJewelry() || !(bodyItem.getBonusForSpellEffect(jewelryEnchant) > 0.0f)) continue;
            ++activeJewelry;
            totalPower += (bodyItem.getCurrentQualityLevel() + bodyItem.getBonusForSpellEffect(jewelryEnchant)) / 2.0f;
        }
        if (totalPower > 0.0f) {
            float increase = 0.025f * (float)activeJewelry + 0.025f * (totalPower / 100.0f);
            damageMultiplier *= 1.0f + (increase *= 2.0f / (float)(activeJewelry + 1));
        }
        return damageMultiplier;
    }

    public static float getJewelryResistModifier(Creature defender, byte woundType) {
        byte jewelryEnchant = 0;
        switch (woundType) {
            case 4: {
                jewelryEnchant = 7;
                break;
            }
            case 5: {
                jewelryEnchant = 8;
                break;
            }
            case 8: {
                jewelryEnchant = 6;
                break;
            }
            case 10: {
                jewelryEnchant = 5;
            }
        }
        if (jewelryEnchant == 0) {
            return 1.0f;
        }
        Item[] bodyItems = defender.getBody().getContainersAndWornItems();
        float damageMultiplier = 1.0f;
        float totalPower = 0.0f;
        int activeJewelry = 0;
        for (Item bodyItem : bodyItems) {
            if (!bodyItem.isEnchantableJewelry() || !(bodyItem.getBonusForSpellEffect(jewelryEnchant) > 0.0f)) continue;
            ++activeJewelry;
            totalPower += (bodyItem.getCurrentQualityLevel() + bodyItem.getBonusForSpellEffect(jewelryEnchant)) / 2.0f;
        }
        if (totalPower > 0.0f) {
            float reduction = 0.025f * (float)activeJewelry + 0.05f * (totalPower / 100.0f);
            damageMultiplier *= 1.0f - (reduction *= 2.0f / (float)(activeJewelry + 1));
        }
        return damageMultiplier;
    }

    public static SpellEffect hasNegatingEffect(Item target, byte enchantment) {
        if (target.getSpellEffects() != null) {
            for (ArrayList<Byte> group2 : enchantGroups) {
                if (!group2.contains(enchantment)) continue;
                for (byte ench : group2) {
                    if (ench == enchantment || !(target.getBonusForSpellEffect(ench) > 0.0f)) continue;
                    return target.getSpellEffect(ench);
                }
            }
        }
        return null;
    }

    public static boolean canEnchantDemise(Creature performer, Item target) {
        if (!Spell.mayBeEnchanted(target)) {
            performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
            return false;
        }
        if (target.enchantment != 0) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already enchanted.", (byte)3);
            return false;
        }
        if (target.getCurrentQualityLevel() < 70.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is of too low quality for this enchantment.", (byte)3);
            return false;
        }
        return true;
    }

    public static void sendInvalidTargetMessage(Creature performer, Spell spell) {
        StringBuilder str = new StringBuilder();
        str.append("You can only target ");
        ArrayList<String> targets = new ArrayList<String>();
        if (spell.isTargetArmour()) {
            targets.add("armour");
        }
        if (spell.isTargetWeapon()) {
            targets.add("weapons");
        }
        if (spell.isTargetJewelry()) {
            targets.add("jewelry");
        }
        if (spell.isTargetPendulum()) {
            targets.add("pendulums");
        }
        if (targets.size() == 0) {
            logger.warning("Spell " + spell.getName() + " has no valid targets.");
        } else if (targets.size() == 1) {
            str.append((String)targets.get(0));
        } else if (targets.size() == 2) {
            str.append((String)targets.get(0)).append(" or ").append((String)targets.get(1));
        } else {
            StringBuilder allTargets = new StringBuilder();
            for (String target : targets) {
                if (allTargets.length() > 0) {
                    allTargets.append(", ");
                }
                allTargets.append(target);
            }
            str.append((CharSequence)allTargets);
        }
        str.append(".");
        performer.getCommunicator().sendNormalServerMessage(str.toString());
    }

    public static void sendCannotBeEnchantedMessage(Creature performer) {
        performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.", (byte)3);
    }

    public static void sendNegatingEffectMessage(String name, Creature performer, Item target, SpellEffect negatingEffect) {
        performer.getCommunicator().sendNormalServerMessage(String.format("The %s is already enchanted with %s, which would negate the effect of %s.", target.getName(), negatingEffect.getName(), name), (byte)3);
    }

    public static void initializeEnchantGroups() {
        ArrayList<Byte> speedEffectGroup = new ArrayList<Byte>();
        speedEffectGroup.add((byte)47);
        speedEffectGroup.add((byte)16);
        speedEffectGroup.add((byte)32);
        enchantGroups.add(speedEffectGroup);
        ArrayList<Byte> skillgainEffectGroup = new ArrayList<Byte>();
        skillgainEffectGroup.add((byte)47);
        skillgainEffectGroup.add((byte)13);
        enchantGroups.add(skillgainEffectGroup);
        ArrayList<Byte> weaponDamageEffectGroup = new ArrayList<Byte>();
        weaponDamageEffectGroup.add((byte)45);
        weaponDamageEffectGroup.add((byte)63);
        weaponDamageEffectGroup.add((byte)14);
        weaponDamageEffectGroup.add((byte)33);
        weaponDamageEffectGroup.add((byte)26);
        weaponDamageEffectGroup.add((byte)18);
        weaponDamageEffectGroup.add((byte)27);
        enchantGroups.add(weaponDamageEffectGroup);
        ArrayList<Byte> armourEffectGroup = new ArrayList<Byte>();
        armourEffectGroup.add((byte)17);
        armourEffectGroup.add((byte)46);
        enchantGroups.add(armourEffectGroup);
        ArrayList<Byte> mailboxEffectGroup = new ArrayList<Byte>();
        mailboxEffectGroup.add((byte)20);
        mailboxEffectGroup.add((byte)44);
        enchantGroups.add(mailboxEffectGroup);
        ArrayList<Byte> jewelryEffectGroup = new ArrayList<Byte>();
        jewelryEffectGroup.add((byte)29);
        jewelryEffectGroup.add((byte)1);
        jewelryEffectGroup.add((byte)5);
        jewelryEffectGroup.add((byte)4);
        jewelryEffectGroup.add((byte)8);
        jewelryEffectGroup.add((byte)2);
        jewelryEffectGroup.add((byte)6);
        jewelryEffectGroup.add((byte)3);
        jewelryEffectGroup.add((byte)7);
        enchantGroups.add(jewelryEffectGroup);
    }
}

