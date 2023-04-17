/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import java.util.BitSet;

public interface MiscConstants {
    public static final long NOID = -10L;
    public static final short notInitialized = -1;
    public static final short ACTION_DEFAULT = -1;
    public static final short OWN_INVENTORY = -1;
    public static final double RADS_TO_DEGS = 57.29577951308232;
    public static final float DEGS_TO_RADS = (float)Math.PI / 180;
    public static final int MAX_SHORTINT = 65535;
    public static final int SIXTY_K = 60000;
    public static final int FORTYFIVE_K = 45000;
    public static final int ONE_K = 1000;
    public static final int TWENTY_K = 20000;
    public static final int FIVE_K = 5000;
    public static final int TEN_K = 10000;
    public static final int NORTHEAST = 0;
    public static final int EASTNORTH = 1;
    public static final int EASTSOUTH = 2;
    public static final int SOUTHEAST = 3;
    public static final int SOUTHWEST = 4;
    public static final int WESTSOUTH = 5;
    public static final int WESTNORTH = 6;
    public static final int NORTHWEST = 7;
    public static final byte north = 0;
    public static final byte northeast = 1;
    public static final byte east = 2;
    public static final byte southeast = 3;
    public static final byte south = 4;
    public static final byte southwest = 5;
    public static final byte west = 6;
    public static final byte northwest = 7;
    public static final byte POWER_NONE = 0;
    public static final byte POWER_HERO = 1;
    public static final byte POWER_DEMIGOD = 2;
    public static final byte POWER_HIGH_GOD = 3;
    public static final byte POWER_ARCHANGEL = 4;
    public static final byte POWER_IMPLEMENTOR = 5;
    public static final byte POWER_IMPOSSIBLE = 10;
    public static final int PRIORITY_MAX = 10;
    public static final int PRIORITY_MIN = 1;
    public static final int PRIORITY_NORM = 5;
    public static final int PRIORITY_FIGHT = 7;
    public static final int PRIORITY_HIGH = 8;
    public static final int PRIORITY_SPELL = 8;
    public static final byte SEX_MALE = 0;
    public static final byte SEX_FEMALE = 1;
    public static final byte SEX_NOSEX = 127;
    public static final int CREATURE_SIZE_HUGE = 5;
    public static final int CREATURE_SIZE_LARGE = 4;
    public static final int CREATURE_SIZE_MEDIUM = 3;
    public static final int CREATURE_SIZE_SMALL = 2;
    public static final int CREATURE_SIZE_TINY = 1;
    public static final byte KINGDOM_NONE = 0;
    public static final byte KINGDOM_JENN = 1;
    public static final byte KINGDOM_MOLREHAN = 2;
    public static final byte KINGDOM_HOTS = 3;
    public static final byte KINGDOM_FREEDOM = 4;
    public static final int REPUTATION_MOD_STEAL = 35;
    public static final int REPUTATION_MOD_STEAL_KINGDOM = 10;
    public static final int REPUTATION_MOD_KILL = 20;
    public static final int MAX_ACCOUNTS = 5;
    public static final double ONETHIRD = 0.3333333333333333;
    public static final String EMPTYSTRING = "";
    public static final String ESCAPED_DOUBLE_QUOTE_STRING = "\"";
    public static final char ESCAPED_DOUBLE_QUOTE_CHAR = '\"';
    public static final float GROUND_LEVEL = -3000.0f;
    public static final int AGE_YOUNG = 3;
    public static final int AGE_ADOL = 8;
    public static final int AGE_MATURE = 12;
    public static final int AGE_AGED = 30;
    public static final int AGE_OLD = 40;
    public static final int AGE_VENER = 1000000;
    public static final String WORD_YOUNG = "young";
    public static final String WORD_ADOLESCENT = "adolescent";
    public static final String WORD_MATURE = "mature";
    public static final String WORD_AGED = "aged";
    public static final String WORD_OLD = "old";
    public static final String WORD_VENERABLE = "venerable";
    public static final String WORD_FIERCE = "fierce ";
    public static final String WORD_ANGRY = "angry ";
    public static final String WORD_RAGING = "raging ";
    public static final String WORD_SLOW = "slow ";
    public static final String WORD_ALERT = "alert ";
    public static final String WORD_GREENISH = "greenish ";
    public static final String WORD_LURKING = "lurking ";
    public static final String WORD_SLY = "sly ";
    public static final String WORD_HARDENED = "hardened ";
    public static final String WORD_SCARED = "scared ";
    public static final String WORD_CHAMPION = "champion ";
    public static final String WORD_DISEASED = "diseased ";
    public static final String WORD_STARVING = "starving ";
    public static final String WORD_FAT = "fat ";
    public static final String CHECKWORD_FIERCE = " fierce ";
    public static final String CHECKWORD_ANGRY = " angry ";
    public static final String CHECKWORD_RAGING = " raging ";
    public static final String CHECKWORD_SLOW = " slow ";
    public static final String CHECKWORD_ALERT = " alert ";
    public static final String CHECKWORD_GREENISH = " greenish ";
    public static final String CHECKWORD_LURKING = " lurking ";
    public static final String CHECKWORD_SLY = " sly ";
    public static final String CHECKWORD_HARDENED = " hardened ";
    public static final String CHECKWORD_SCARED = " scared ";
    public static final String CHECKWORD_CHAMPION = " champion ";
    public static final String CHECKWORD_DISEASED = " diseased ";
    public static final String CHECKWORD_STARVING = " starving ";
    public static final String CHECKWORD_FAT = " fat ";
    public static final String HUMAN = "Human";
    public static final String emptyString = "";
    public static final String commaStringNsp = ",";
    public static final String commaString = ", ";
    public static final String andString = " and ";
    public static final String andTheString = " and the ";
    public static final String spaceString = " ";
    public static final String[] emptyStringArray = new String[0];
    public static final String MILLISECONDS_STRING = " millis.";
    public static final String WHICH_TOOK_STRING = ", which took ";
    public static final String GLOBALKCHAT = "GL-";
    public static final String TRADE = "Trade";
    public static final char SPACE_CHAR = ' ';
    public static final String dotString = ".";
    public static final char DOT_CHAR = '.';
    public static final char COLON_CHAR = ':';
    public static final char CLOSE_SQUARE_BRACKET_CHAR = ']';
    public static final char EXCLAMATION_MARK = '!';
    public static final char XY_COORDINATE_SEPARATOR = ',';
    public static final byte[] EMPTY_BYTE_PRIMITIVE_ARRAY = new byte[0];
    public static final short[] EMPTY_SHORT_PRIMITIVE_ARRAY = new short[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final long[] EMPTY_LONG_PRIMITIVE_ARRAY = new long[0];
    public static final float NANOS_IN_A_MILLISECOND = 1000000.0f;
    public static final int MAX_TRAIT_BITS = 64;
    public static final int MAX_ABILITIES_BITS = 64;
    public static final int MAX_FLAGS_BITS = 64;
    public static final int MAX_SESSIONFLAGS_BITS = 64;
    public static final int MAX_SETTINGS_BITS = 32;
    public static final int TRAIT_BRAVE = 0;
    public static final int TRAIT_FAST = 1;
    public static final int TRAIT_TOUGH = 2;
    public static final int TRAIT_STRONG = 3;
    public static final int TRAIT_LIGHTNING = 4;
    public static final int TRAIT_PACK = 5;
    public static final int TRAIT_JUMPER = 6;
    public static final int TRAIT_WATER_DRAWN = 7;
    public static final int TRAIT_SLOW = 8;
    public static final int TRAIT_HALT = 9;
    public static final int TRAIT_BITER = 10;
    public static final int TRAIT_NAG = 11;
    public static final int TRAIT_SURLY = 12;
    public static final int TRAIT_SICK = 13;
    public static final int TRAIT_RAVAGING = 14;
    public static final int TRAIT_COLOR1 = 15;
    public static final int TRAIT_COLOR2 = 16;
    public static final int TRAIT_COLOR3 = 17;
    public static final int TRAIT_COLOR4 = 18;
    public static final int TRAIT_DISEASE_PRONE = 19;
    public static final int TRAIT_DISEASE_AVERTED = 20;
    public static final int TRAIT_LONG_LIVED = 21;
    public static final int TRAIT_CORRUPT = 22;
    public static final int TRAIT_COLOR7 = 23;
    public static final int TRAIT_COLOR5 = 24;
    public static final int TRAIT_COLOR6 = 25;
    public static final int TRAIT_RIFT = 27;
    public static final int TRAIT_TRAITOR = 28;
    public static final int TRAIT_VALREI_MISSION = 29;
    public static final int TRAIT_COLOR8 = 30;
    public static final int TRAIT_COLOR9 = 31;
    public static final int TRAIT_COLOR10 = 32;
    public static final int TRAIT_COLOR11 = 33;
    public static final int TRAIT_COLOR12 = 34;
    public static final int TRAIT_COLOR13 = 35;
    public static final int TRAIT_COLOR14 = 36;
    public static final int TRAIT_COLOR15 = 37;
    public static final int TRAIT_COLOR16 = 38;
    public static final int CURRENT_MAX_TRAIT = 34;
    public static final int TRAIT_BRED = 63;
    public static final int CURRENT_NUM_CREATURE_COLOURS = 13;
    public static final String BROWN = "brown";
    public static final String WHITE = "white";
    public static final String BLACK = "black";
    public static final String GOLD = "gold";
    public static final String GREY = "grey";
    public static final String PIEBALDPINTO = "piebaldPinto";
    public static final String BLOODBAY = "bloodBay";
    public static final String EBONYBLACK = "ebonyBlack";
    public static final String SKEWBALDPINTO = "skewbaldpinto";
    public static final String GOLDBUCKSKIN = "goldbuckskin";
    public static final String BLACKSILVER = "blacksilver";
    public static final String APPALOOSA = "appaloosa";
    public static final String CHESTNUT = "chestnut";
    public static final String MESSAGE_FORMAT_UTF_8 = "UTF-8";
    public static final String vocals = "aeiouyAEIOUY";
    public static final byte COMMON = 0;
    public static final byte RARE = 1;
    public static final byte SUPREME = 2;
    public static final byte FANTASTIC = 3;
    public static final byte USE_ACTIVE_ITEM_MAYBE = 0;
    public static final byte USE_ACTIVE_ITEM_ALWAYS = 1;
    public static final byte USE_ACTIVE_ITEM_NEVER = 2;
    public static final byte A_TYPE_DIAMOND = 5;
    public static final byte A_TYPE_GOLD = 4;
    public static final byte A_TYPE_SILVER = 3;
    public static final byte A_TYPE_STEEL = 2;
    public static final float BLOCKER_GENERAL_HALF_THICKNESS = 0.1f;
    public static final float BLOCKER_GENERAL_PADDING = 0.1f;
    public static final int FLAG_STRUCTUREDOOR = 0;
    public static final int FLAG_NO_PMS = 1;
    public static final int FLAG_CROSS_KINGDOM = 2;
    public static final int FLAG_CROSS_SERVER = 3;
    public static final int FLAG_FRIENDS_OVERRIDE = 4;
    public static final int FLAG_RECEIVED_GOAL_KARMA = 5;
    public static final int FLAG_WON_THE_GAME = 6;
    public static final int FLAG_HIDE_EQUIP_OPTIONS = 7;
    public static final int FLAG_SENT_EXPIRY_WARNING = 8;
    public static final int FLAG_PREMIUM_EXPIRED = 9;
    public static final int FLAG_RECEIVED_PREM_BONUS = 10;
    public static final int FLAG_SWITCHED_SKILLS = 11;
    public static final int FLAG_PLONK_FIRST_DAMAGE = 12;
    public static final int FLAG_PLONK_LOW_STAMINA = 13;
    public static final int FLAG_PLONK_HUNGRY = 14;
    public static final int FLAG_PLONK_THIRSTY = 15;
    public static final int FLAG_PLONK_FALL_DAMAGE = 16;
    public static final int FLAG_PLONK_SWIMMING = 17;
    public static final int FLAG_PLONK_DEATH = 18;
    public static final int FLAG_PLONK_FIRST_TIME_ON_A_BOAT = 19;
    public static final int FLAG_PLONK_ENCUMBERED = 20;
    public static final int FLAG_VILLAGE_TELEPORT = 21;
    public static final int FLAG_PLONK_TREE_ACTIONS = 22;
    public static final int FLAG_PLONK_BOAT_SECURITY = 23;
    public static final int FLAG_HIDE_TITLE_ABILITY = 24;
    public static final int FLAG_HIDE_TITLE_CULTIST = 25;
    public static final int FLAG_IS_QA_ACCOUNT = 26;
    public static final int FLAG_ACK_CHALLENGE = 27;
    public static final int FLAG_CHECKED_AWARDS = 28;
    public static final int FLAG_KINGDOM_CHAT = 29;
    public static final int FLAG_GLOBAL_KINGDOM_CHAT = 30;
    public static final int FLAG_TRADE_CHANNEL = 31;
    public static final int FLAG_LOOT_ALLIANCE = 32;
    public static final int FLAG_LOOT_VILLAGE = 33;
    public static final int FLAG_LOOT_TRUSTED_FRIENDS = 34;
    public static final int FLAG_KINGDOM_MESSAGE = 35;
    public static final int FLAG_GLOBAL_KINGDOM_MESSAGE = 36;
    public static final int FLAG_TRADE_MESSAGE = 37;
    public static final int FLAG_VILLAGE_CHAT = 38;
    public static final int FLAG_VILLAGE_MESSAGE = 39;
    public static final int FLAG_ALLIANCE_CHAT = 40;
    public static final int FLAG_ALLIANCE_MESSAGE = 41;
    public static final int FLAG_DONE_TUTORIAL = 42;
    public static final int FLAG_SB_IDLEOFF = 43;
    public static final int FLAG_PVP_BLOCK = 44;
    public static final int FLAG_HIDE_GV_HELP = 45;
    public static final int FLAG_RECEIVED_GIFTPACK = 46;
    public static final int FLAG_SHOULD_RECEIVE_RETURNERSPACK = 47;
    public static final int FLAG_SHOULD_RECEIVE_GM_PAYMENT = 48;
    public static final int FLAG_CHECKED_FOR_EXTRAGIFT = 49;
    public static final int FLAG_HAS_A_NAMED_RECIPE = 50;
    public static final int FLAG_IS_A_TESTER = 51;
    public static final int FLAG_HIDE_CCFP = 52;
    public static final int FLAG_NEW_AFFINITY_CALC = 53;
    public static final int FLAG_NO_WAGONER_CHAT = 54;
    public static final int FLAG_ANNIVERSARY_GIFT = 55;
    public static final int FLAG_SEE_OTHER_PLAYER_TITLES = 56;
    public static final int FLAG_SEE_OTHER_VILLAGE_TITLES = 57;
    public static final int FLAG_SHOW_OWN_VILLAGE_TITLES = 58;
    public static final int FLAG_HIDE_MY_PVE_DEATHS = 59;
    public static final int FLAG_IGNORE_PVE_DEATHS_TAB = 60;
    public static final int FLAG_FIXED_PREMIUM_ACHIEVEMENTS = 61;
    public static final int FLAG_RECEIVED_XMAS_PRESENT = 62;
    public static final int FLAG_CODE_ONLY_PREM = 63;
    public static final int FLAG_CURRENT_MAX = 63;
    public static final int FLAG_JOURNAL_COMPT0 = 64;
    public static final int FLAG_JOURNAL_COMPT1 = 65;
    public static final int FLAG_JOURNAL_COMPT2 = 66;
    public static final int FLAG_JOURNAL_COMPT3 = 67;
    public static final int FLAG_JOURNAL_COMPT4 = 68;
    public static final int FLAG_JOURNAL_COMPT5 = 69;
    public static final int FLAG_JOURNAL_COMPT6 = 70;
    public static final int FLAG_JOURNAL_COMPT7 = 71;
    public static final int FLAG_JOURNAL_COMPT8 = 72;
    public static final int FLAG_JOURNAL_COMPT9 = 73;
    public static final int FLAG_USED_FAITH_SWAP = 74;
    public static final int FLAG_CHAOS_BORDER_CROSSED = 75;
    public static final int FLAG_CANUSE_TUTORIALPORTAL = 76;
    public static final int FLAG_INC_SB_CAP = 77;
    public static final int FLAG_JOURNAL_COMPP1 = 78;
    public static final int FLAG_JOURNAL_COMPP2 = 79;
    public static final int FLAG_JOURNAL_COMPP3 = 80;
    public static final int FLAG_INC_FAITH_CAP = 81;
    public static final int FLAG_INC_SPELL_POWER = 82;
    public static final int FLAG2_CURRENT_MAX = 82;
    public static final int ABILITY_UNUSED_FOR_NOW = 0;
    public static final int ABILITY_WITCH = 1;
    public static final int ABILITY_HAG = 2;
    public static final int ABILITY_CRONE = 3;
    public static final int ABILITY_NIGHTHAG = 4;
    public static final int ABILITY_ENCHANTRESS = 5;
    public static final int ABILITY_NORN = 6;
    public static final int ABILITY_FORTUNETELLER = 7;
    public static final int ABILITY_MESMERIZER = 8;
    public static final int ABILITY_SOOTHSAYER = 9;
    public static final int ABILITY_MEDIUM = 10;
    public static final int ABILITY_SIREN = 11;
    public static final int ABILITY_DIVINER = 12;
    public static final int ABILITY_INQUISITOR = 13;
    public static final int ABILITY_WITCHDOCTOR = 14;
    public static final int ABILITY_NECROMANCER = 15;
    public static final int ABILITY_OCCULTIST = 16;
    public static final int ABILITY_DEATHKNIGHT = 17;
    public static final int ABILITY_DIABOLIST = 18;
    public static final int ABILITY_HYPNOTIST = 19;
    public static final int ABILITY_EVOCATOR = 20;
    public static final int ABILITY_THAUMATURG = 21;
    public static final int ABILITY_WARLOCK = 22;
    public static final int ABILITY_MAGICIAN = 23;
    public static final int ABILITY_CONJURER = 24;
    public static final int ABILITY_MAGUS = 25;
    public static final int ABILITY_ARCHMAGE = 26;
    public static final int ABILITY_WITCHHUNTER = 27;
    public static final int ABILITY_WIZARD = 28;
    public static final int ABILITY_SUMMONER = 29;
    public static final int ABILITY_SPELLBINDER = 30;
    public static final int ABILITY_ILLUSIONIST = 31;
    public static final int ABILITY_ENCHANTER = 32;
    public static final int ABILITY_DRUID = 33;
    public static final int ABILITY_SORCEROR = 34;
    public static final int ABILITY_SORCERESS = 35;
    public static final int ABILITY_DEMON_QUEEN = 36;
    public static final int ABILITY_MAGE = 37;
    public static final int ABILITY_SHADOWMAGE = 38;
    public static final int ABILITY_ASCENDED = 39;
    public static final int ABILITY_PLANESWALKER = 40;
    public static final int ABILITY_WORGMASTER = 41;
    public static final int ABILITY_VALKYRIE = 42;
    public static final int ABILITY_BERSERKER = 43;
    public static final int ABILITY_INCINERATOR = 44;
    public static final int ABILITY_CURRENT_MAX = 44;
    public static final int SESSIONFLAG_SIGNEDIN = 0;
    public static final int SESSIONFLAG_AFK = 1;
    public static final int SESSIONFLAG_SEND_EXTRA_BYTES = 2;
    public static final int SESSIONFLAG_CREATION_WINDOW_OPEN = 3;
    public static final int SESSIONFLAG_CURRENT_MAX = 3;
    public static final byte TICKET_NOTE = 0;
    public static final byte TICKET_CANCELLED = 1;
    public static final byte TICKET_CM_RESPONDED = 2;
    public static final byte TICKET_GM_RESPONDED = 3;
    public static final byte TICKET_ARCH_RESPONDED = 4;
    public static final byte TICKET_DEV_RESPONDED = 5;
    public static final byte TICKET_FORWARD_GM = 6;
    public static final byte TICKET_FORWARD_ARCH = 7;
    public static final byte TICKET_FORWARD_DEV = 8;
    public static final byte TICKET_RESOLVED = 9;
    public static final byte TICKET_ON_HOLD = 10;
    public static final byte TICKET_TAKEN = 11;
    public static final byte TICKET_FORWARD_CM = 13;
    public static final byte TICKET_FEEDBACK = 14;
    public static final byte TICKET_REOPENED = 15;
    public static final int UNDEAD_NOT = 0;
    public static final int UNDEAD_ZOMBIE = 1;
    public static final int UNDEAD_SKELETON = 2;
    public static final int UNDEAD_GHOST = 3;
    public static final int UNDEAD_SPECTRE = 4;
    public static final int UNDEAD_LICH = 5;
    public static final int UNDEAD_LICH_KING = 6;
    public static final int UNDEAD_GHAST = 7;
    public static final int UNDEAD_GHOUL = 8;
    public static final long NEWBIE_END = 86400000L;
    public static final float LIMIT_HEAVY = -0.3f;
    public static final float LIMIT_MEDIUM = -0.15f;
    public static final float LIMIT_LIGHT = 0.0f;
    public static final float LIMIT_NONE = 0.3f;
    public static final long HAVENS_LANDING_TOKENID = 7689502046815490L;

    public static String getDirectionString(byte direction) {
        switch (direction) {
            case 0: {
                return "north";
            }
            case 1: {
                return "northeast";
            }
            case 2: {
                return "east";
            }
            case 3: {
                return "southeast";
            }
            case 4: {
                return "south";
            }
            case 5: {
                return "southwest";
            }
            case 6: {
                return "west";
            }
            case 7: {
                return "northwest";
            }
        }
        return "unknown";
    }

    public static BitSet createBitSetLong(long fromLong) {
        BitSet bitSet = new BitSet(64);
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((fromLong & 1L) == 1L) {
                    bitSet.set(x, true);
                    continue;
                }
                bitSet.set(x, false);
                continue;
            }
            if ((fromLong >> x & 1L) == 1L) {
                bitSet.set(x, true);
                continue;
            }
            bitSet.set(x, false);
        }
        return bitSet;
    }

    public static long bitSetToLong(BitSet bitSet) {
        long ret = 0L;
        for (int x = 0; x < 64; ++x) {
            if (!bitSet.get(x)) continue;
            ret += 1L << x;
        }
        return ret;
    }
}

