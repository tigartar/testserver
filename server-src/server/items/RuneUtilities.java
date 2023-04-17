/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class RuneUtilities
implements MiscConstants,
Enchants,
ItemMaterials {
    private static final ArrayList<Byte> metalList;
    private static final HashMap<Byte, RuneData> runeDataMap;

    public static final boolean isSingleUseRune(Item rune) {
        if (!runeDataMap.containsKey(RuneUtilities.getEnchantForRune(rune))) {
            return false;
        }
        return runeDataMap.get(RuneUtilities.getEnchantForRune(rune)).isSingleUse();
    }

    public static final boolean isEnchantRune(Item rune) {
        if (!runeDataMap.containsKey(RuneUtilities.getEnchantForRune(rune))) {
            return false;
        }
        return runeDataMap.get(RuneUtilities.getEnchantForRune(rune)).isEnchantment();
    }

    public static final String getAttachmentTargets(Item rune) {
        int riftTemplate = rune.getRealTemplateId();
        RuneData runeData = runeDataMap.get(RuneUtilities.getEnchantForRune(rune));
        if (runeData.isAnyTarget()) {
            return "any item";
        }
        switch (riftTemplate) {
            case 1104: {
                return "wooden items";
            }
            case 1103: {
                return "metal items";
            }
            case 1102: {
                return "stone, leather, cloth and pottery items";
            }
        }
        return "unknown";
    }

    public static final boolean isCorrectTarget(Item rune, Item target) {
        int riftTemplate = rune.getRealTemplateId();
        RuneData runeData = runeDataMap.get(RuneUtilities.getEnchantForRune(rune));
        if (runeData.getModifierPercentage(ModifierEffect.ENCH_GLOW) > 0.0f && (target.getTemplate().isLight() || target.getTemplate().isCooker() || target.isAlwaysPoll())) {
            return false;
        }
        switch (riftTemplate) {
            case 1104: {
                if (!target.isWood() && !runeData.isAnyTarget()) break;
                return true;
            }
            case 1103: {
                if (!target.isMetal() && !runeData.isAnyTarget()) break;
                return true;
            }
            case 1102: {
                if (target.isStone() || runeData.isAnyTarget()) {
                    return true;
                }
                if (target.isLeather() || target.isCloth()) {
                    return true;
                }
                if (!target.isPottery()) break;
                return true;
            }
            default: {
                return Servers.isThisATestServer();
            }
        }
        return false;
    }

    public static final int getNumberOfRuneEffects(Item target) {
        ItemSpellEffects effs = target.getSpellEffects();
        if (effs != null) {
            return effs.getNumberOfRuneEffects();
        }
        return 0;
    }

    public static final boolean canApplyRuneTo(Item rune, Item target) {
        if (RuneUtilities.canApplyRuneTo(RuneUtilities.getEnchantForRune(rune), target)) {
            return RuneUtilities.isCorrectTarget(rune, target);
        }
        return false;
    }

    public static final boolean canApplyRuneTo(byte runeEnchant, Item target) {
        return !target.isNotRuneable();
    }

    public static final byte getEnchantForRune(Item rune) {
        int startId;
        int metalOffset = metalList.indexOf(rune.getMaterial());
        if (rune.getMaterial() == 96) {
            switch (rune.getTemplateId()) {
                case 1290: {
                    return -50;
                }
            }
            return -1;
        }
        switch (rune.getTemplateId()) {
            case 1289: {
                startId = -128;
                break;
            }
            case 1290: {
                startId = -115;
                break;
            }
            case 1291: {
                startId = -102;
                break;
            }
            case 1292: {
                startId = -89;
                break;
            }
            default: {
                startId = -76;
            }
        }
        return (byte)(startId + metalOffset);
    }

    public static Spell getSpellForRune(Item source) {
        if (runeDataMap.get(RuneUtilities.getEnchantForRune(source)) != null) {
            return runeDataMap.get(RuneUtilities.getEnchantForRune(source)).getSingleUseSpell();
        }
        return null;
    }

    public static final byte getMetalForEnchant(byte enchantType) {
        for (enchantType = (byte)(enchantType + 128); enchantType > 12; enchantType = (byte)(enchantType - 13)) {
        }
        return metalList.get(enchantType);
    }

    public static final String getRuneName(byte type) {
        switch (type) {
            case -50: {
                return MaterialUtilities.getMaterialString((byte)96) + " rune of " + Deities.getDeityName(1);
            }
        }
        String toReturn = MaterialUtilities.getMaterialString(RuneUtilities.getMetalForEnchant(type));
        toReturn = toReturn + " rune of ";
        if (type < -115) {
            toReturn = toReturn + Deities.getDeityName(2);
        } else if (type < -102) {
            toReturn = toReturn + Deities.getDeityName(1);
        } else if (type < -89) {
            toReturn = toReturn + Deities.getDeityName(3);
        } else if (type < -76) {
            toReturn = toReturn + Deities.getDeityName(4);
        } else if (type < -63) {
            toReturn = toReturn + Deities.getDeityName(9);
        } else if (type < -50) {
            toReturn = toReturn + Deities.getDeityName(11);
        }
        return toReturn;
    }

    public static final String getRuneLongDesc(byte type) {
        if (!runeDataMap.containsKey(type)) {
            return "";
        }
        return runeDataMap.get(type).getDescription();
    }

    public static final float getModifier(byte type, ModifierEffect e) {
        if (!runeDataMap.containsKey(type)) {
            return 0.0f;
        }
        return runeDataMap.get(type).getModifierPercentage(e);
    }

    private static void createRuneDefinitions() {
        RuneData tempRune = new RuneData(-128, true, "increase quality at a faster rate when being improved (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-127, true, "gather resources at a higher quality level (5%) and increase the time an enchant holds its power on the item (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_RESGATHERED, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_ENCHRETENTION, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-126, false, "activate the mole senses effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(439));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-125, true, "increase vehicle speed (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_VEHCSPEED, 0.1f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-124, true, "increase usage speed (5%) and increase skill level on skill checks (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_SKILLCHECKBONUS, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-123, true, "reduce size (5%) and reduce weight (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_SIZE, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-122, true, "increase volume (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-121, true, "reduce decay taken (5%) and increase the effect on speed by wind (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_WIND, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-120, true, "have a higher chance to be successfully improved (5%) and increase the chance of increasing rarity when improved (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_RARITYIMP, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-119, false, "activate the sunder effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(253));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-118, true, "reduce the quality change when repairing damage (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-117, true, "increase size (5%) and increase vehicle speed (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_SIZE, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_VEHCSPEED, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-116, true, "increase chance to resist shattering when being enchanted (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_SHATTERRES, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-50, false, "decrease the age of a single creature");
        tempRune.addModifier(ModifierEffect.SINGLE_CHANGE_AGE, 1.0f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-115, true, "increase quality at a faster rate when being improved (5%) and gather resources at a higher quality level (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_RESGATHERED, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-114, true, "increase the effect on speed by wind (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_WIND, 0.1f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-113, true, "increase the chance of increasing rarity when improved (5%) and reduce damage taken (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_RARITYIMP, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-112, true, "increase the time an enchant holds its power on the item (5%) and increase the chance of successfully enchanting the item (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHRETENTION, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-111, true, "increase skill level on skill checks (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_SKILLCHECKBONUS, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-110, true, "have a chance to increase the effect of tending a field or harvesting a tree or bush (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_FARMYIELD, 0.1f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-109, true, "have an okay glow and increase skill level on skill checks (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_GLOW, 0.25f);
        tempRune.addModifier(ModifierEffect.ENCH_SKILLCHECKBONUS, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-108, true, "reduce decay taken (5%) and reduce damage taken (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-107, false, "activate the charm animal effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(275));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-106, true, "reduce fuel usage rate (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_FUELUSE, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-105, true, "reduce the quality change when repairing damage (5%) and increase usage speed (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-104, false, "activate the morning fog effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(282));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-103, true, "increase the chance of successfully enchanting the item (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-102, true, "increase quality at a faster rate when being improved (7.5%) and have a slight glow");
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.075f);
        tempRune.addModifier(ModifierEffect.ENCH_GLOW, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-101, true, "increase the effect on speed by wind (7.5%) and increase vehicle speed (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_WIND, 0.075f);
        tempRune.addModifier(ModifierEffect.ENCH_VEHCSPEED, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-100, true, "increase the chance of increasing rarity when improved (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_RARITYIMP, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-99, true, "increase vehicle speed (7.5%) and reduce decay taken (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_VEHCSPEED, 0.075f);
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-98, true, "increase usage speed (5%) and increase quality at a faster rate when being improved (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-97, false, "activate the reveal creatures effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(444));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-96, true, "reduce the decay taken of items inside (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_INTERNAL_DECAY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-95, true, "reduce damage taken (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-94, true, "have a higher chance to be successfully improved (7.5%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.075f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-93, true, "reduce fuel usage rate (5%) and reduce decay taken (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_FUELUSE, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-92, true, "reduce the quality change when repairing damage (5%) and increase volume (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-91, false, "activate the mend effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(251));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-90, true, "increase chance to resist shattering when being enchanted (5%) and increase the chance of successfully enchanting the item (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_SHATTERRES, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-89, true, "have an okay glow and gather resources at a higher quality level (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_GLOW, 0.25f);
        tempRune.addModifier(ModifierEffect.ENCH_RESGATHERED, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-88, true, "gather resources at a higher quality level (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_RESGATHERED, 0.1f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-87, true, "increase the chance of increasing rarity when improved (5%) and have a higher chance to be successfully improved (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_RARITYIMP, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-86, true, "increase the time an enchant holds its power on the item (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHRETENTION, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-85, true, "increase usage speed (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-84, true, "have a chance to increase the effect of tending a field or harvesting a tree or bush (5%) and reduce weight (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_FARMYIELD, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-83, true, "increase volume (5%) and reduce the decay taken of items inside (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_INTERNAL_DECAY, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-82, true, "reduce damage taken (5%) and increase the effect on speed by wind (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_WIND, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-81, false, "activate the locate soul effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(419));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-80, false, "activate the light token effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(421));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-79, true, "reduce volume (5%) and increase usage speed (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-78, true, "reduce weight (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-77, true, "increase chance to resist shattering when being enchanted (5%) and reduce the quality change when repairing damage (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_SHATTERRES, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-76, true, "have a decent glow");
        tempRune.addModifier(ModifierEffect.ENCH_GLOW, 0.4f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-75, false, "activate the goat shape effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(422));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-74, false, "activate the refresh effect one time");
        tempRune.setSingleUseSpell(Spells.getSpell(250));
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-73, false, "give an item a random color one time");
        tempRune.addModifier(ModifierEffect.SINGLE_COLOR, 1.0f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-72, true, "increase skill level bonus on skill checks (5%) and increase quality at a faster rate when being improved (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_SKILLCHECKBONUS, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-71, true, "reduce size (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_SIZE, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-70, true, "have an okay glow and increase volume (7.5%)");
        tempRune.addModifier(ModifierEffect.ENCH_GLOW, 0.25f);
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, 0.075f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-69, true, "reduce decay taken (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-68, true, "have a higher chance to be successfully improved (5%) and reduce the decay taken of items inside (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_INTERNAL_DECAY, 0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-67, true, "have a chance to increase the effect of tending a field or harvesting a tree or bush (5%) and reduce decay taken (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_FARMYIELD, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-66, true, "reduce volume (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-65, true, "reduce weight (5%) and increase vehicle speed (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_VEHCSPEED, 0.05f);
        tempRune.setAnyTarget(true);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-64, true, "increase the chance of successfully enchanting the item (5%) and reduce the quality change when repairing damage (5%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.05f);
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.05f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-63, true, "reduce damage taken (10%) and reduce weight (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-62, true, "increase quality at a faster rate when being improved (10%) and increase the time an enchant holds its power on the item (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPQL, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_ENCHRETENTION, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-61, true, "increase the chance of increasing rarity when improved (10%) and have a higher chance to be successfully improved (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_RARITYIMP, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-60, true, "increase the chance of successfully enchanting the item (10%) and reduce decay taken (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-59, true, "increase usage speed (10%) and increase chance to resist shattering when being enchanted (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_SHATTERRES, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-58, true, "reduce the quality change when repairing damage (10%) and reduce damage taken (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-57, true, "increase volume (10%) and reduce the decay taken of items inside (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_INTERNAL_DECAY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-56, true, "reduce damage taken (10%) and reduce decay taken (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_DAMAGETAKEN, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_DECAY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-55, true, "have a higher chance to be successfully improved (10%) and reduce the quality change when repairing damage (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_IMPPERCENT, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_REPAIRQL, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-54, true, "increase the time an enchant holds its power on the item (10%) and reduce size (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_ENCHRETENTION, 0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_SIZE, -0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-53, true, "reduce volume (10%) and increase usage speed (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_VOLUME, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_USESPEED, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-52, true, "reduce weight (10%) and increase size (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_WEIGHT, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_SIZE, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
        tempRune = new RuneData(-51, true, "increase chance to resist shattering when being enchanted (10%) and increase the chance of successfully enchanting the item (10%)");
        tempRune.addModifier(ModifierEffect.ENCH_SHATTERRES, -0.1f);
        tempRune.addModifier(ModifierEffect.ENCH_ENCHANTABILITY, 0.1f);
        runeDataMap.put(tempRune.getEnchantType(), tempRune);
    }

    static {
        byte[] metalOffset = new byte[]{30, 31, 34, 56, 57, 7, 8, 9, 10, 11, 12, 13, 67};
        metalList = new ArrayList();
        for (byte b : metalOffset) {
            metalList.add(b);
        }
        runeDataMap = new HashMap();
        RuneUtilities.createRuneDefinitions();
    }

    static class RuneData {
        private byte enchantType;
        private boolean isSingleUse;
        private boolean isEnchantment;
        private boolean anyTarget;
        private String description;
        private HashMap<ModifierEffect, Float> modifierMap;
        private Spell singleUse = null;

        RuneData(byte enchantType, boolean isEnchantment, String description) {
            this.enchantType = enchantType;
            this.isEnchantment = isEnchantment;
            this.isSingleUse = !isEnchantment;
            this.description = description;
            this.anyTarget = false;
        }

        public boolean isAnyTarget() {
            return this.anyTarget;
        }

        public void setAnyTarget(boolean anyTarget) {
            this.anyTarget = anyTarget;
        }

        public byte getEnchantType() {
            return this.enchantType;
        }

        public boolean isEnchantment() {
            return this.isEnchantment;
        }

        public void setEnchantment(boolean isEnchantment) {
            this.isEnchantment = isEnchantment;
        }

        public boolean isSingleUse() {
            return this.isSingleUse;
        }

        public void setSingleUse(boolean isSingleUse) {
            this.isSingleUse = isSingleUse;
        }

        public Spell getSingleUseSpell() {
            return this.singleUse;
        }

        public void setSingleUseSpell(Spell singleUseSpell) {
            this.singleUse = singleUseSpell;
        }

        public String getDescription() {
            return this.description;
        }

        void addModifier(ModifierEffect mod, float percentage) {
            if (percentage < -1.0f || percentage > 1.0f) {
                return;
            }
            if (this.modifierMap == null) {
                this.modifierMap = new HashMap();
            }
            if (this.modifierMap.containsKey((Object)mod)) {
                this.modifierMap.replace(mod, Float.valueOf(percentage));
            }
            this.modifierMap.put(mod, Float.valueOf(percentage));
        }

        float getModifierPercentage(ModifierEffect mod) {
            if (this.modifierMap == null) {
                return 0.0f;
            }
            if (this.modifierMap.containsKey((Object)mod)) {
                return this.modifierMap.get((Object)mod).floatValue();
            }
            return 0.0f;
        }
    }

    public static enum ModifierEffect {
        ENCH_WEIGHT,
        ENCH_VOLUME,
        ENCH_DAMAGETAKEN,
        ENCH_USESPEED,
        ENCH_SIZE,
        ENCH_SKILLCHECKBONUS,
        ENCH_SHATTERRES,
        ENCH_DECAY,
        ENCH_INTERNAL_DECAY,
        ENCH_VEHCSPEED,
        ENCH_WIND,
        ENCH_IMPQL,
        ENCH_REPAIRQL,
        ENCH_FUELUSE,
        ENCH_ENCHANTABILITY,
        ENCH_ENCHRETENTION,
        ENCH_IMPPERCENT,
        ENCH_RESGATHERED,
        ENCH_FARMYIELD,
        ENCH_RARITYIMP,
        ENCH_GLOW,
        SINGLE_COLOR,
        SINGLE_REFRESH,
        SINGLE_CHANGE_AGE;

    }
}

