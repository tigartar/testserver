/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

public enum SpellEffectsEnum {
    NONE(0L, "", -1, 0, 0),
    CR_BONUS(-12335499L, "Potion", 0, 9, 0),
    FARWALKER(-12335498L, "Farwalker", 1, 9, 0),
    DISEASE(-12335497L, "Disease", 2, 1, 1),
    CHAMP_POINTS(-12335496L, "Champion points", 3, false, 1, 0),
    ENEMY(-12335495L, "Enemy", 4, 5, 1, false),
    LINKS(-12335494L, "Links", 5, 5, 0, false),
    FAITHBONUS(-12335493L, "Faith bonus", 6, 5, 0, false),
    WOUNDMOVE(-12335492L, "Hurting", 7, 1, 1, false),
    DEITY_FAVORGAIN(-12335491L, "Spirit Favor", 8, 6, 0, false),
    DEITY_STAMINAGAIN(-12335490L, "Spirit Stamina", 9, 6, 0, false),
    DEITY_CRBONUS(-12335489L, "Spirit Fervor", 10, 6, 0, false),
    DEITY_MOVEBONUS(-12335488L, "Spirit Speed", 11, 6, 0, false),
    LOVE_EFFECT(-12335487L, "Love effect", 12, 1, 0),
    ROD_BEGUILING_EFFECT(-12335486L, "Beguiling", 13, 6, 0, false),
    FINGER_FO_EFFECT(-12335485L, "Finger of Fo", 14, 6, 0, false),
    CROWN_MAGRANON_EFFECT(-12335484L, "Crown of Might", 15, 6, 0, false),
    ORB_DOOM_EFFECT(-12335483L, "Orb Of Doom", 16, 6, 1),
    KARMA(-12335481L, "Karma", 18, false, 1, 0),
    SCENARIOKARMA(-12335480L, "Scenario Points", 19, false, 1, 0),
    FIRE_RESIST(-12335479L, "Fire Toughness", 20, 1, 0, false),
    COLD_RESIST(-12335478L, "Cold Toughness", 21, 1, 0, false),
    DISEASE_RESIST(-12335477L, "Disease Toughness", 22, 1, 0, false),
    PHYSICAL_RESIST(-12335476L, "Physical Damage Toughness", 23, 1, 0, false),
    PIERCE_RESIST(-12335475L, "Pierce Insensitive Skin", 24, 1, 0, false),
    SLASH_RESIST(-12335474L, "Slash Damage Toughness", 25, 1, 0, false),
    CRUSH_RESIST(-12335473L, "Crush Damage Toughness", 26, 1, 0, false),
    BITE_RESIST(-12335472L, "Bite Damage Toughness", 27, 1, 0, false),
    POISON_RESIST(-12335471L, "Poison Resistance", 28, 1, 0, false),
    WATER_RESIST(-12335470L, "Water Damage Resistance", 29, 1, 0, false),
    ACID_RESIST(-12335469L, "Acid Resistant Skin", 30, 1, 0, false),
    INTERNAL_RESIST(-12335468L, "Internal Damage Toughness", 31, 1, 0, false),
    FIRE_VULNERABILITY(-12335467L, "Fire Vulnerability", 32, 1, 1, false),
    COLD_VULNERABILITY(-12335466L, "Cold Vulnerability", 33, 1, 1, false),
    DISEASE_VULNERABILITY(-12335465L, "Disease Vulnerability", 34, 1, 1, false),
    PHYSICAL_VULNERABILITY(-12335464L, "Physical Damage Sensitivity", 35, 1, 1, false),
    PIERCE_VULNERABILITY(-12335463L, "Pierce Damage Sensitivity", 36, 1, 1, false),
    SLASH_VULNERABILITY(-12335462L, "Slash Damage Sensitivity", 37, 1, 1, false),
    CRUSH_VULNERABILITY(-12335461L, "Crush Damage Sensitivity", 38, 1, 1, false),
    BITE_VULNERABILITY(-12335460L, "Bite Damage Sensitivity", 39, 1, 1, false),
    POISON_VULNERABILITY(-12335459L, "Poison Sensitivity", 40, 1, 1, false),
    WATER_VULNERABILITY(-12335458L, "Water Vulnerability", 41, 1, 1, false),
    ACID_VULNERABILITY(-12335457L, "Acid Sensitivity", 42, 1, 1, false),
    INTERNAL_VULNERABILITY(-12335456L, "Internal Damage Sensitivity", 43, 1, 1, false),
    ILLUSION(0L, "Illusion", 44, 9, 0),
    FAVOR_OVERHEATED(52L, "Overheated", 44, 52, 1),
    POISON(0L, "Poison", 45, 0, 1),
    DETECT_INVIS(-12335454L, "Detect Invisible", 46, 1, 0),
    STUNNED(-12335453L, "Stunned", 47, 1, 1),
    HUNTED(0L, "Hunted", 48, 1, 1),
    NEWBIE_HEALTH(0L, "Newbie healing buff", 49, 1, 0),
    NEWBIE_AGGRO_RANGE(0L, "Newbie agg range buff", 50, 1, 0),
    NEWBIE_FOOD(0L, "Newbie food and drink buff", 51, 1, 0),
    HATE_FEAR_EFFECT(-12335452L, "Fear effect", 52, 1, 0),
    HATE_DOUBLE_WAR(-12335451L, "Rage", 53, 1, 0),
    HATE_DOUBLE_STRUCT(-12335450L, "Double structure damage", 54, 1, 0),
    POWER_NO_ELEMENTAL(-12335449L, "Elemental immunity", 55, 1, 0),
    POWER_IGNORE_TRAPS(-12335448L, "Trap immunity", 56, 1, 0),
    LOVE_HEALING_HANDS(-12335447L, "Healing hands", 57, 1, 0, false),
    HATE_SPELL_IMMUNITY(-12335446L, "Spell immunity", 58, 1, 0),
    POWER_USES_LESS_STAMINA(-12335445L, "Stamina of the Vibrant Light", 59, 1, 0),
    KNOWLEDGE_NO_DECAY(-12335444L, "No skill loss", 60, 1, 0, false),
    KNOWLEDGE_INCREASED_SKILL_GAIN(-12335443L, "Increased skill gain", 61, 1, 0),
    INSANITY_SHIELD_GONE(-12335442L, "Shield of the Gone", 62, 1, 0),
    SPELL_FOREST_GIANT(0L, "Forest Giant Strength", 63, 1, 0),
    SPELL_BEARPAWS(0L, "Bearpaws", 64, 1, 0),
    SPELL_MORNING_FOG(0L, "Morning Fog", 65, 1, 0),
    SPELL_NOLOCATE(0L, "Nolocate", 66, 1, 0),
    SPELL_OAKSHELL(0L, "Oakshell", 67, 1, 0),
    SPELL_WILLOWSPINE(0L, "Willowspine", 68, 1, 0),
    SPELL_SIXTH_SENSE(0L, "Sixth sense", 69, 1, 0),
    SPELL_HELL_STRENGTH(0L, "Hell strength", 70, 1, 0),
    SPELL_FRANTIC_CHARGE(0L, "Frantic charge", 71, 1, 0),
    SPELL_GOAT_SHAPE(0L, "Goat shape", 72, 1, 0),
    SPELL_TRUE_HIT(0L, "Truehit", 73, 1, 0),
    SPELL_THORNSHELL(0L, "Thornshell", 74, 1, 0),
    SPELL_KARMA_STONESKIN(0L, "Stoneskin", 75, 1, 0),
    SPELL_KARMA_RUST_MONSTER(0L, "Rust Monster", 76, 1, 0),
    SPELL_KARMA_RIVER(0L, "Karma Drain", 77, 1, 1),
    SPELL_KARMA_CONTINUUM(0L, "Continuum", 78, 1, 0),
    SPELL_EXCEL(0L, "Excel", 79, 1, 0),
    SPELL_TRUE_STRIKE(0L, "True Strike", 80, 1, 0),
    RES_TENTACLES(-12335441L, "Res Tentacles", 81, 1, 0),
    RES_DISPEL(-12335440L, "Res Dispel", 82, 1, 0),
    RES_DRAINHEALTH(-12335439L, "Res Drain Health", 83, 1, 0),
    RES_DRAINSTAMINA(-12335438L, "Res Drain Stamina", 84, 1, 0),
    RES_FIREHEART(-12335437L, "Res Fireheart", 85, 1, 0),
    RES_FIREPILLAR(-12335436L, "Res Firepillar", 86, 1, 0),
    RES_FUNGUSTRAP(-12335435L, "Res Fungus Trap", 87, 1, 0),
    RES_HEAL(-12335434L, "Res Heal", 88, 1, 0),
    RES_ICEPILLAR(-12335433L, "Res Icepillar", 89, 1, 0),
    RES_LIGHTOFFO(-12335432L, "Res Light Of Fo", 90, 1, 0),
    RES_PAINRAIN(-12335431L, "Res Pain Rain", 91, 1, 0),
    RES_ROTTINGGUT(-12335430L, "Res Rotting Gut", 92, 1, 0),
    RES_SCORNLIBILA(-12335429L, "Res Scorn Of Libila", 93, 1, 0),
    RES_SHARDOFICE(-12335428L, "Res Shard Of Ice", 94, 1, 1),
    RES_SMITE(-12335427L, "Res Smite", 95, 1, 0),
    RES_WEAKNESS(-12335426L, "Res Weakness", 96, 1, 0),
    RES_GENERIC(-12335425L, "Res Generic", 97, 1, 0),
    ARMOUR_LIMIT_HEAVY(-12335424L, "Armour Limit -30.000002%", 98, 1, 1),
    ARMOUR_LIMIT_MEDIUM(-12335230L, "Armour Limit -15.000001%", 99, 1, 0),
    ARMOUR_LIMIT_LIGHT(-12335422L, "Armour Bonus 0.0%", 100, 1, 0),
    ARMOUR_LIMIT_NONE(-12335421L, "Armour Bonus 30.000002%", 101, 1, 0),
    SPELL_TANGLEWEAVE(-12335420L, "Tangle Weave", 102, 1, 1),
    SPELL_KARMA_INCINERATION(-12335419L, "Incineration", 103, 1, 1),
    ITEM_COTTON_CRUSHING(-12335418L, "Cloth armour pieces glance bonus versus crushing", 104, 10, 0, false),
    ITEM_COTTON_SLASHING(-12335417L, "Cloth armour pieces glance bonus versus slashing", 105, 10, 0, false),
    ITEM_NONE_BASHING(-12335416L, "Dodge bonus vs bashing wearing no armour", 106, 10, 0, false),
    ITEM_LEATHER_TWOHANDED(-12335415L, "Leather armour glance bonus vs twohanded.", 107, 10, 0, false),
    ITEM_STUDDED_TWOHANDED(-12335414L, "Studded leather armour glance bonus vs twohanded.", 108, 10, 0, false),
    ITEM_LIGHT_BASHING(-12335413L, "Dodge bonus vs bashing wearing light armour", 109, 10, 0, false),
    ITEM_MEDIUM_BASHING(-12335412L, "Dodge bonus vs bashing wearing medium armour", 110, 10, 0, false),
    ITEM_COTTON_SLASHDAM(-12335411L, "Cloth armour piece damage reduction vs slash combat damage", 111, 10, 0, false),
    ITEM_FACEDAM(-12335410L, "Face damage protection", 112, 10, 0, false),
    ITEM_LEATHER_CRUSHDAM(-12335409L, "Leather armour combat damage reduction vs crush damage", 113, 10, 0, false),
    ITEM_CHAIN_SLASHDAM(-12335408L, "Chain armour combat damage reduction vs slash damage", 114, 10, 0, false),
    ITEM_CHAIN_PIERCEDAM(-12335407L, "Chain armour combat damage reduction vs pierce damage", 115, 10, 0, false),
    ITEM_AREA_SPELL(-12335406L, "Area spell damage increase", 116, 10, 0, false),
    ITEM_ENCHANT_DAMREDUCT(-12335405L, "Enchant damage combat damage reduction reduction", 117, 10, 0, false),
    ITEM_AREASPELL_DAMREDUCT(-12335404L, "Area spell damage reduction", 118, 10, 0, false),
    ITEM_MEDLIGHT_DAMINCREASE(-12335403L, "Weapon damage increase while wearing medium armour or lighter.", 119, 10, 0, false),
    ITEM_HEAVY_ARCHERY(-12335402L, "Archery fail reduction when wearing heavy or medium armour.", 120, 10, 0, false),
    ITEM_RING_STAMINA(-12335401L, "Stamina reduction bonus", 121, 10, 0, false),
    ITEM_RING_DODGE(-12335400L, "Dodge bonus", 122, 10, 0, false),
    ITEM_RING_CR(-12335399L, "Combat rating bonus", 123, 10, 0, false),
    ITEM_RING_SPELLRESIST(-12335398L, "Spell resist bonus", 124, 10, 0, false),
    ITEM_RING_HEALING(-12335397L, "Healing bonus", 125, 10, 0, false),
    ITEM_RING_SKILLGAIN(-12335396L, "Skillgain bonus", 126, 10, 0, false),
    ITEM_RING_SWIMMING(-12335395L, "Drown damage reduction.", 127, 10, 0, false),
    ITEM_RING_STEALTH(-12335394L, "Stealth bonus", 128, 10, 0, false),
    ITEM_RING_DETECTION(-12335393L, "Stealth detection", 129, 10, 0, false),
    ITEM_BRACELET_CRUSH(-12335392L, "Parry wielding crushing", 130, 10, 0, false),
    ITEM_BRACELET_TWOHANDED(-12335391L, "Parry wielding twohanded", 131, 10, 0, false),
    ITEM_BRACELET_PIERCEDAM(-12335390L, "Pierce damage", 132, 10, 0, false),
    ITEM_BRACELET_POLEARMDAM(-12335389L, "Polearm damage", 133, 10, 0, false),
    ITEM_BRACELET_ENCHANTDAM(-12335388L, "Enchant damage", 134, 10, 0, false),
    ITEM_NECKLACE_SKILLEFF(-12335387L, "Skill efficiency", 135, 10, 0, false),
    ITEM_NECKLACE_SKILLMAX(-12335386L, "Item improvement skill max", 136, 10, 0, false),
    ITEM_NECKLACE_HURTING(-12335385L, "Hurting time reduction", 137, 10, 0, false),
    ITEM_NECKLACE_FOCUS(-12335384L, "Focus chance", 138, 10, 0, false),
    ITEM_NECKLACE_REPLENISH(-12335383L, "Replenishment", 139, 10, 0, false),
    ITEM_DEBUFF_EXHAUSTION(-12335382L, "Exhaustion", 140, 10, 1),
    ITEM_DEBUFF_CLUMSINESS(-12335381L, "Clumsiness", 141, 10, 1),
    ITEM_DEBUFF_VULNERABILITY(-12335380L, "Vulnerability", 142, 10, 1),
    DEATH_PROTECTION(-12335454L, "Death protection", 143, 1, 0),
    SKILL_TIMED_AFFINITY(0L, "Timed Affinity", 144, 1, 0),
    RES_STUNNED(-12335379L, "Res Stunned", 145, 1, 0),
    RES_TORNADO(-12335378L, "Res Tornado", 146, 1, 0),
    RES_WORMBRAINS(-12335377L, "Res Worm Brains", 147, 1, 0),
    RES_SHARED(-12335376L, "Res Shared", 148, 1, 0),
    RES_WRATH_OF_MAGRANON(-12335375L, "Res Wrath of Magranon", 149, 1, 0);

    private final long id;
    private final int typeId;
    private final byte effectType;
    private final byte influence;
    private final String name;
    private final boolean sendToBuffBar;
    private final boolean sendDuration;
    private static SpellEffectsEnum[] effects;

    private SpellEffectsEnum(long id, String name, int typeId, byte effectType, byte influence, boolean sendDuration) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.effectType = effectType;
        this.influence = influence;
        this.sendDuration = sendDuration;
        this.sendToBuffBar = true;
    }

    private SpellEffectsEnum(long id, String name, int typeId, byte effectType, byte influence) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.effectType = effectType;
        this.influence = influence;
        this.sendDuration = true;
        this.sendToBuffBar = true;
    }

    private SpellEffectsEnum(long id, String name, int typeId, boolean sendToBuffBar, byte effectType, byte influence) {
        this.id = id;
        this.name = name;
        this.typeId = typeId;
        this.sendToBuffBar = sendToBuffBar;
        this.effectType = effectType;
        this.influence = influence;
        this.sendDuration = true;
    }

    public static final SpellEffectsEnum getResistanceForSpell(short spellActionId) {
        switch (spellActionId) {
            case 418: {
                return RES_TENTACLES;
            }
            case 450: {
                return RES_DISPEL;
            }
            case 255: {
                return RES_DRAINHEALTH;
            }
            case 254: {
                return RES_DRAINSTAMINA;
            }
            case 424: {
                return RES_FIREHEART;
            }
            case 420: {
                return RES_FIREPILLAR;
            }
            case 433: {
                return RES_FUNGUSTRAP;
            }
            case 249: {
                return RES_HEAL;
            }
            case 414: {
                return RES_ICEPILLAR;
            }
            case 438: {
                return RES_LIGHTOFFO;
            }
            case 432: {
                return RES_PAINRAIN;
            }
            case 428: {
                return RES_ROTTINGGUT;
            }
            case 448: {
                return RES_SCORNLIBILA;
            }
            case 485: {
                return RES_SHARDOFICE;
            }
            case 252: {
                return RES_SMITE;
            }
            case 429: {
                return RES_WEAKNESS;
            }
        }
        if (spellActionId == STUNNED.getTypeId()) {
            return RES_STUNNED;
        }
        return RES_GENERIC;
    }

    public static final byte getDebuffForEnum(SpellEffectsEnum checkedEnum) {
        switch (checkedEnum) {
            case ITEM_DEBUFF_EXHAUSTION: {
                return 95;
            }
            case ITEM_DEBUFF_VULNERABILITY: {
                return 96;
            }
            case ITEM_DEBUFF_CLUMSINESS: {
                return 97;
            }
        }
        return 0;
    }

    public long createId(long modifier) {
        return this.id + modifier;
    }

    public final long getId() {
        return this.id;
    }

    public final int getTypeId() {
        return this.typeId;
    }

    public final String getName() {
        return this.name;
    }

    public final boolean isSendToBuffBar() {
        return this.sendToBuffBar;
    }

    public final byte getEffectType() {
        return this.effectType;
    }

    public final byte getInfluence() {
        return this.influence;
    }

    public final boolean isSendDuration() {
        return this.sendDuration;
    }

    public static final SpellEffectsEnum getEnumByName(String name) {
        for (int i = 0; i < effects.length; ++i) {
            if (!effects[i].getName().equalsIgnoreCase(name)) continue;
            return effects[i];
        }
        return NONE;
    }

    public static final SpellEffectsEnum getEnumById(long id) {
        for (int i = 0; i < effects.length; ++i) {
            if (effects[i].getId() != id) continue;
            return effects[i];
        }
        return NONE;
    }

    public static final SpellEffectsEnum getEnumForItemTemplateId(int templateId, int extraInfo) {
        switch (templateId) {
            case 1049: {
                return ITEM_COTTON_CRUSHING;
            }
            case 1050: {
                return ITEM_COTTON_SLASHING;
            }
            case 1051: {
                return ITEM_NONE_BASHING;
            }
            case 1052: {
                return ITEM_LEATHER_TWOHANDED;
            }
            case 1053: 
            case 1066: {
                return ITEM_STUDDED_TWOHANDED;
            }
            case 1054: {
                return ITEM_LIGHT_BASHING;
            }
            case 1055: 
            case 1092: {
                return ITEM_MEDIUM_BASHING;
            }
            case 1056: 
            case 1093: {
                return ITEM_COTTON_SLASHDAM;
            }
            case 1057: 
            case 1094: {
                return ITEM_FACEDAM;
            }
            case 1058: 
            case 1095: {
                return ITEM_LEATHER_CRUSHDAM;
            }
            case 1059: {
                return ITEM_CHAIN_SLASHDAM;
            }
            case 1060: {
                return ITEM_CHAIN_PIERCEDAM;
            }
            case 1061: {
                return ITEM_AREA_SPELL;
            }
            case 1062: {
                return ITEM_ENCHANT_DAMREDUCT;
            }
            case 1063: {
                return ITEM_AREASPELL_DAMREDUCT;
            }
            case 1064: {
                return ITEM_MEDLIGHT_DAMINCREASE;
            }
            case 1065: {
                return ITEM_HEAVY_ARCHERY;
            }
            case 1076: {
                switch (extraInfo) {
                    case 1: {
                        return ITEM_RING_STAMINA;
                    }
                    case 2: {
                        return ITEM_RING_DODGE;
                    }
                    case 3: {
                        return ITEM_RING_CR;
                    }
                    case 4: {
                        return ITEM_RING_SPELLRESIST;
                    }
                    case 5: {
                        return ITEM_RING_HEALING;
                    }
                }
            }
            case 1077: {
                return ITEM_RING_SKILLGAIN;
            }
            case 1078: {
                return ITEM_RING_SWIMMING;
            }
            case 1079: {
                return ITEM_RING_STEALTH;
            }
            case 1080: {
                return ITEM_RING_DETECTION;
            }
            case 1081: {
                return ITEM_BRACELET_CRUSH;
            }
            case 1082: {
                return ITEM_BRACELET_TWOHANDED;
            }
            case 1083: {
                return ITEM_BRACELET_PIERCEDAM;
            }
            case 1084: {
                return ITEM_BRACELET_POLEARMDAM;
            }
            case 1085: {
                return ITEM_BRACELET_ENCHANTDAM;
            }
            case 1086: {
                return ITEM_NECKLACE_SKILLEFF;
            }
            case 1087: {
                return ITEM_NECKLACE_SKILLMAX;
            }
            case 1088: {
                return ITEM_NECKLACE_HURTING;
            }
            case 1089: {
                return ITEM_NECKLACE_FOCUS;
            }
            case 1090: {
                return ITEM_NECKLACE_REPLENISH;
            }
        }
        return null;
    }

    public static final SpellEffectsEnum getDebuffEnumForItemTemplateId(int templateId, int extraInfo) {
        switch (templateId) {
            case 1076: {
                switch (extraInfo) {
                    case 1: {
                        return ITEM_DEBUFF_VULNERABILITY;
                    }
                    case 2: {
                        return ITEM_DEBUFF_CLUMSINESS;
                    }
                    case 3: {
                        return ITEM_DEBUFF_CLUMSINESS;
                    }
                    case 4: {
                        return ITEM_DEBUFF_EXHAUSTION;
                    }
                    case 5: {
                        return ITEM_DEBUFF_VULNERABILITY;
                    }
                }
            }
            case 1077: {
                return ITEM_DEBUFF_CLUMSINESS;
            }
            case 1078: {
                return ITEM_DEBUFF_EXHAUSTION;
            }
            case 1079: {
                return ITEM_DEBUFF_VULNERABILITY;
            }
            case 1080: {
                return ITEM_DEBUFF_VULNERABILITY;
            }
            case 1081: {
                return ITEM_DEBUFF_EXHAUSTION;
            }
            case 1082: {
                return ITEM_DEBUFF_EXHAUSTION;
            }
            case 1083: {
                return ITEM_DEBUFF_CLUMSINESS;
            }
            case 1084: {
                return ITEM_DEBUFF_VULNERABILITY;
            }
            case 1085: {
                return ITEM_DEBUFF_VULNERABILITY;
            }
            case 1086: {
                return ITEM_DEBUFF_CLUMSINESS;
            }
            case 1087: {
                return ITEM_DEBUFF_CLUMSINESS;
            }
            case 1088: {
                return ITEM_DEBUFF_VULNERABILITY;
            }
            case 1089: {
                return ITEM_DEBUFF_EXHAUSTION;
            }
            case 1090: {
                return ITEM_DEBUFF_EXHAUSTION;
            }
        }
        return null;
    }

    static {
        effects = SpellEffectsEnum.values();
    }
}

