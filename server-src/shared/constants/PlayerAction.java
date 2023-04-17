/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import com.wurmonline.shared.constants.PlayerActionConstants;
import java.util.HashMap;
import java.util.Map;

public class PlayerAction
implements PlayerActionConstants {
    public static final int SURFACE_TILE = 1;
    public static final int SURFACE_TILE_BORDER = 2;
    public static final int CAVE_TILE = 16;
    public static final int INVENTORY_ITEM = 256;
    public static final int GROUND_ITEM = 512;
    public static final int WALL = 1024;
    public static final int HOUSE = 2048;
    public static final int CREATURE = 4096;
    public static final int PLAYER = 8192;
    public static final int ANY_TILE = 17;
    public static final int STRUCTURE = 3072;
    public static final int MOBILE = 12288;
    public static final int ANY_ITEM = 768;
    public static final int CREATURE_OR_ITEM = 4864;
    public static final int STATIC_OBJECT = 3840;
    public static final int ANYTHING = 65535;
    private final short id;
    private String bind;
    private final String name;
    private final boolean instant;
    private final boolean atomic;
    private final int targetMask;
    private static Map<Short, PlayerAction> actionIds = new HashMap<Short, PlayerAction>();
    public static final PlayerAction[] emotes = new PlayerAction[]{new PlayerAction("", -3, 65535, "Emotes"), new PlayerAction("", -13, 65535, "Nice"), new PlayerAction("", 2000, 65535, "Smile"), new PlayerAction("", 2001, 65535, "Chuckle"), new PlayerAction("", 2002, 65535, "Applaud"), new PlayerAction("", 2003, 65535, "Hug"), new PlayerAction("", 2004, 65535, "Kiss"), new PlayerAction("", 2005, 65535, "Grovel"), new PlayerAction("", 2006, 65535, "Worship"), new PlayerAction("", 2007, 65535, "Comfort"), new PlayerAction("", 2008, 65535, "Dance"), new PlayerAction("", 2009, 65535, "Flirt"), new PlayerAction("", 2010, 65535, "Bow"), new PlayerAction("", 2011, 65535, "Kiss hand"), new PlayerAction("", 2012, 65535, "Tickle"), new PlayerAction("", -16, 65535, "Neutral"), new PlayerAction("", 2013, 65535, "Wave"), new PlayerAction("", 2014, 65535, "Call"), new PlayerAction("", 2015, 65535, "Poke"), new PlayerAction("", 2016, 65535, "Roll with the eyes"), new PlayerAction("", 2017, 65535, "Disbelieve"), new PlayerAction("", 2018, 65535, "Worry"), new PlayerAction("", 2019, 65535, "Disagree"), new PlayerAction("", 2020, 65535, "Tease"), new PlayerAction("", 2021, 65535, "Laugh"), new PlayerAction("", 2022, 65535, "Cry"), new PlayerAction("", 2023, 65535, "Point"), new PlayerAction("", 2030, 65535, "Follow"), new PlayerAction("", 2031, 65535, "Goodbye"), new PlayerAction("", 2032, 65535, "Lead"), new PlayerAction("", 2033, 65535, "That way"), new PlayerAction("", 2034, 65535, "Wrong way"), new PlayerAction("", -6, 65535, "Offensive"), new PlayerAction("", 2024, 65535, "Spit"), new PlayerAction("", 2025, 65535, "Fart"), new PlayerAction("", 2026, 65535, "Insult"), new PlayerAction("", 2027, 65535, "Push"), new PlayerAction("", 2028, 65535, "Curse"), new PlayerAction("", 2029, 65535, "Slap")};
    public static final PlayerAction DEFAULT_ACTION = new PlayerAction("DEFAULT_ACTION", -1, 65535);
    public static final PlayerAction DEFAULT_TERRAFORM_ACTION = new PlayerAction("DEFAULT_TERRAFORM_ACTION", 718, 529);
    public static final PlayerAction EXAMINE = new PlayerAction("EXAMINE", 1, 65535, "Examine");
    public static final PlayerAction OPEN = new PlayerAction("OPEN", 3, 4864);
    public static final PlayerAction CLOSE = new PlayerAction("CLOSE", 4, 768);
    public static final PlayerAction TAKE = new PlayerAction("TAKE", 6, 768);
    public static final PlayerAction TRADE = new PlayerAction("TRADE", 63, 12288);
    public static final PlayerAction DROP = new PlayerAction("DROP", 7, 768);
    public static final PlayerAction COMBINE = new PlayerAction("COMBINE", 93, 256);
    public static final PlayerAction LOCK = new PlayerAction("LOCK", 28, 3840);
    public static final PlayerAction TAME = new PlayerAction("TAME", 46, 4096);
    public static final PlayerAction SELL = new PlayerAction("SELL", 31, 4864);
    public static final PlayerAction PLAN_BUILDING = new PlayerAction("PLAN_BUILDING", 56, 1);
    public static final PlayerAction FINALIZE_BUILDING = new PlayerAction("FINALIZE_BUILDING", 58, 2048);
    public static final PlayerAction ADD_FRIEND = new PlayerAction("ADD_FRIEND", 60, 8192);
    public static final PlayerAction REMOVE_FRIEND = new PlayerAction("REMOVE_FRIEND", 61, 8192);
    public static final PlayerAction DRAG = new PlayerAction("DRAG", 74, 512);
    public static final PlayerAction STOP_DRAGGING = new PlayerAction("STOP_DRAGGING", 75, 512);
    public static final PlayerAction SPAM_MODE = new PlayerAction("SPAM_MODE", 84, 65535, "Spam mode");
    public static final PlayerAction CUT_DOWN = new PlayerAction("CUT_DOWN", 96, 1025);
    public static final PlayerAction CHOP_UP = new PlayerAction("CHOP_UP", 97, 768);
    public static final PlayerAction PUSH = new PlayerAction("PUSH", 99, 512);
    public static final PlayerAction PUSH_GENTLY = new PlayerAction("PUSH_GENTLY", 696, 512);
    public static final PlayerAction MOVE_CENTER = new PlayerAction("MOVE_CENTER", 864, 512);
    public static final PlayerAction UNLOCK = new PlayerAction("UNLOCK", 102, 3840);
    public static final PlayerAction LEAD = new PlayerAction("LEAD", 106, 4096);
    public static final PlayerAction STOP_LEADING = new PlayerAction("STOP_LEADING", 107, 4096);
    public static final PlayerAction TRACK = new PlayerAction("TRACK", 109, 17);
    public static final PlayerAction BURY = new PlayerAction("BURY", 119, 768);
    public static final PlayerAction BURY_ALL = new PlayerAction("BURY_ALL", 707, 768);
    public static final PlayerAction BUTCHER = new PlayerAction("BUTCHER", 120, 768);
    public static final PlayerAction FILET = new PlayerAction("FILET", 225, 768);
    public static final PlayerAction PRAY = new PlayerAction("PRAY", 141, 529);
    public static final PlayerAction PREACH = new PlayerAction("PREACH", 216, 512);
    public static final PlayerAction LISTEN = new PlayerAction("LISTEN", 115, 8192);
    public static final PlayerAction LINK = new PlayerAction("LINK", 399, 8192);
    public static final PlayerAction SACRIFICE = new PlayerAction("SACRIFICE", 142, 4608);
    public static final PlayerAction MEDITATE = new PlayerAction("MEDITATE", 384, 512);
    public static final PlayerAction FIRSTAID = new PlayerAction("FIRSTAID", 196, 256);
    public static final PlayerAction TREAT = new PlayerAction("TREAT", 284, 256);
    public static final PlayerAction LOAD_CARGO = new PlayerAction("LOAD_CARGO", 605, 512);
    public static final PlayerAction UNLOAD_CARGO = new PlayerAction("UNLOAD_CARGO", 606, 256);
    public static final PlayerAction STOP = new PlayerAction("STOP", 149, 65535, "Stop");
    public static final PlayerAction MINE_FORWARD = new PlayerAction("MINE_FORWARD", 145, 19);
    public static final PlayerAction MINE_UP = new PlayerAction("MINE_UP", 146, 16);
    public static final PlayerAction MINE_DOWN = new PlayerAction("MINE_DOWN", 147, 16);
    public static final PlayerAction FARM = new PlayerAction("FARM", 151, 1);
    public static final PlayerAction HARVEST = new PlayerAction("HARVEST", 152, 513);
    public static final PlayerAction SOW = new PlayerAction("SOW", 153, 1);
    public static final PlayerAction PROSPECT = new PlayerAction("PROSPECT", 156, 17);
    public static final PlayerAction FISH = new PlayerAction("FISH", 160, 529);
    public static final PlayerAction REPAIR = new PlayerAction("REPAIR", 162, 3840);
    public static final PlayerAction BUILD_STONE_WALL = new PlayerAction("BUILD_STONE_WALL", 163, 2);
    public static final PlayerAction BUILD_TALL_STONE_WALL = new PlayerAction("BUILD_TALL_STONE_WALL", 164, 2);
    public static final PlayerAction BUILD_PALISADE = new PlayerAction("BUILD_PALISADE", 165, 2);
    public static final PlayerAction BUILD_FENCE = new PlayerAction("BUILD_FENCE", 166, 2);
    public static final PlayerAction BUILD_PALISADE_GATE = new PlayerAction("BUILD_PALISADE_GATE", 167, 2);
    public static final PlayerAction BUILD_FENCE_GATE = new PlayerAction("BUILD_FENCE_GATE", 168, 2);
    public static final PlayerAction CONTINUE = new PlayerAction("CONTINUE", 169, 65535);
    public static final PlayerAction CONTINUE_BUILDING = new PlayerAction("CONTINUE_BUILDING", 170, 3072);
    public static final PlayerAction DESTROY_FENCE_PLAN = new PlayerAction("DESTROY_FENCE_PLAN", 171, 1024);
    public static final PlayerAction DESTROY_FENCE = new PlayerAction("DESTROY_FENCE", 172, 1024);
    public static final PlayerAction TURN_CLOCKWISE = new PlayerAction("TURN_CLOCKWISE", 177, 512);
    public static final PlayerAction TURN_COUNTERCLOCKWISE = new PlayerAction("TURN_COUNTERCLOCKWISE", 178, 512);
    public static final PlayerAction PULL = new PlayerAction("PULL", 181, 512);
    public static final PlayerAction PULL_GENTLY = new PlayerAction("PULL_GENTLY", 697, 512);
    public static final PlayerAction DESTROY_WALL = new PlayerAction("DESTROY_WALL", 174, 2048);
    public static final PlayerAction DESTROY_ITEM = new PlayerAction("DESTROY_ITEM", 83, 512);
    public static final PlayerAction DESTROY_PAVEMENT = new PlayerAction("DESTROY_PAVEMENT", 191, 17);
    public static final PlayerAction EMBARK_DRIVER = new PlayerAction("EMBARK_DRIVER", 331, 512);
    public static final PlayerAction EMBARK_PASSENGER = new PlayerAction("EMBARK_PASSENGER", 332, 512);
    public static final PlayerAction DISEMBARK = new PlayerAction("DISEMBARK", 333, 529);
    public static final PlayerAction PICK_SPROUT = new PlayerAction("PICK_SPROUT", 187, 1);
    public static final PlayerAction IMPROVE = new PlayerAction("IMPROVE", 192, 3840);
    public static final PlayerAction FORAGE = new PlayerAction("FORAGE", 223, 1);
    public static final PlayerAction BOTANIZE = new PlayerAction("BOTANIZE", 224, 1);
    public static final PlayerAction MINE_TUNNEL = new PlayerAction("MINE_TUNNEL", 227, 1);
    public static final PlayerAction FINISH = new PlayerAction("FINISH", 228, 768);
    public static final PlayerAction FEED = new PlayerAction("FEED", 230, 4096);
    public static final PlayerAction CULTIVATE = new PlayerAction("CULTIVATE", 318, 1);
    public static final PlayerAction TARGET = new PlayerAction("TARGET", 326, 12288);
    public static final PlayerAction TARGET_HOSTILE = new PlayerAction("TARGET_HOSTILE", 716, 12288);
    public static final PlayerAction NO_TARGET = new PlayerAction("NO_TARGET", 341, 65535, "No target");
    public static final PlayerAction ABSORB = new PlayerAction("ABSORB", 347, 1);
    public static final PlayerAction BREED = new PlayerAction("BREED", 379, 4096);
    public static final PlayerAction PROTECT = new PlayerAction("PROTECT", 381, 1);
    public static final PlayerAction GROOM = new PlayerAction("GROOM", 398, 4096);
    public static final PlayerAction DIG = new PlayerAction("DIG", 144, 1);
    public static final PlayerAction DIG_TO_PILE = new PlayerAction("DIG_TO_PILE", 921, 1);
    public static final PlayerAction FLATTEN = new PlayerAction("FLATTEN", 150, 19);
    public static final PlayerAction PACK = new PlayerAction("PACK", 154, 1);
    public static final PlayerAction PAVE = new PlayerAction("PAVE", 155, 17);
    public static final PlayerAction PAVE_CORNER = new PlayerAction("PAVE_CORNER", 576, 17);
    public static final PlayerAction DREDGE = new PlayerAction("DREDGE", 362, 529);
    public static final PlayerAction PRUNE = new PlayerAction("PRUNE", 373, 1537);
    public static final PlayerAction MUTETOOL = new PlayerAction("MUTETOOL", 467, 65535);
    public static final PlayerAction GMTOOL = new PlayerAction("GMTOOL", 534, 65535);
    public static final PlayerAction PAINT_TERRAIN = new PlayerAction("PAINT_TERRAIN", 604, 17);
    public static final PlayerAction LEVEL = new PlayerAction("LEVEL", 532, 19);
    public static final PlayerAction FLATTEN_BORDER = new PlayerAction("FLATTEN_BORDER", 533, 2);
    public static final PlayerAction ANALYSE = new PlayerAction("ANALYSE", 536, 256);
    public static final PlayerAction FORAGE_VEG = new PlayerAction("FORAGE_VEG", 569, 1);
    public static final PlayerAction FORAGE_RESOURCE = new PlayerAction("FORAGE_RESOURCE", 570, 1);
    public static final PlayerAction FORAGE_BERRIES = new PlayerAction("FORAGE_BERRIES", 571, 1);
    public static final PlayerAction BOTANIZE_SEEDS = new PlayerAction("BOTANIZE_SEEDS", 572, 1);
    public static final PlayerAction BOTANIZE_HERBS = new PlayerAction("BOTANIZE_HERBS", 573, 1);
    public static final PlayerAction BOTANIZE_PLANTS = new PlayerAction("BOTANIZE_PLANTS", 574, 1);
    public static final PlayerAction BOTANIZE_RESOURCE = new PlayerAction("BOTANIZE_RESOURCE", 575, 1);
    public static final PlayerAction BOTANIZE_SPICES = new PlayerAction("BOTANIZE_SPICES", 720, 1);
    public static final PlayerAction DROP_AS_PILE = new PlayerAction("DROP_AS_PILE", 638, 256);
    public static final PlayerAction SET_PRICE = new PlayerAction("SET_PRICE", 86, 768);
    public static final PlayerAction RENAME = new PlayerAction("RENAME", 59, 768);
    public static final PlayerAction TRIM = new PlayerAction("TRIM", 644, 1);
    public static final PlayerAction SHEAR = new PlayerAction("SHEAR", 646, 4096);
    public static final PlayerAction MILK = new PlayerAction("MILK", 345, 4096);
    public static final PlayerAction GATHER = new PlayerAction("GATHER", 645, 1);
    public static final PlayerAction SIT_ANY = new PlayerAction("SIT_ANY", 701, 512);
    public static final PlayerAction STAND_UP = new PlayerAction("STAND_UP", 708, 512);
    public static final PlayerAction CLIMB_UP = new PlayerAction("CLIMB_UP", 522, 2048);
    public static final PlayerAction CLIMB_DOWN = new PlayerAction("CLIMB_DOWN", 523, 2048);
    public static final PlayerAction BUILD_HOUSE_WALL = new PlayerAction("BUILD_HOUSE_WALL", 20000, 2048);
    public static final PlayerAction BUILD_HOUSE_WINDOW = new PlayerAction("BUILD_HOUSE_WINDOW", 20001, 2048);
    public static final PlayerAction BUILD_HOUSE_DOOR = new PlayerAction("BUILD_HOUSE_DOOR", 20002, 2048);
    public static final PlayerAction EQUIP_ITEM = new PlayerAction("EQUIP", 582, 256);
    public static final PlayerAction EQUIP_ITEM_LEFT = new PlayerAction("EQUIP_LEFT", 583, 256);
    public static final PlayerAction EQUIP_ITEM_RIGHT = new PlayerAction("EQUIP_RIGHT", 584, 256);
    public static final PlayerAction UNEQUIP_ITEM = new PlayerAction("UNEQUIP", 585, 256);
    public static final PlayerAction ADD_TO_CRAFTING_WINDOW = new PlayerAction("ADD_TO_CRAFTING_WINDOW", 607, 514);
    public static final PlayerAction PLANT = new PlayerAction("PLANT", 186, 1);
    public static final PlayerAction PLANT_CENTER = new PlayerAction("PLANT_CENTER", 660, 1);
    public static final PlayerAction PICK = new PlayerAction("PICK", 137, 768);
    public static final PlayerAction COLLECT = new PlayerAction("COLLECT", 741, 1);
    public static final PlayerAction PLANT_SIGN = new PlayerAction("PLANT_SIGN", 176, 65535);
    public static final PlayerAction PLANT_LEFT = new PlayerAction("PLANT_LEFT", 746, 65535);
    public static final PlayerAction PLANT_RIGHT = new PlayerAction("PLANT_RIGHT", 747, 65535);
    public static final PlayerAction WINCH = new PlayerAction("WINCH", 237, 512);
    public static final PlayerAction WINCH5 = new PlayerAction("WINCH5", 238, 512);
    public static final PlayerAction WINCH10 = new PlayerAction("WINCH10", 239, 512);
    public static final PlayerAction UNWIND = new PlayerAction("UNWIND", 235, 512);
    public static final PlayerAction LOAD = new PlayerAction("LOAD", 233, 512);
    public static final PlayerAction UNLOAD = new PlayerAction("UNLOAD", 234, 512);
    public static final PlayerAction FIRE = new PlayerAction("FIRE", 236, 512);
    public static final PlayerAction BLESS = new PlayerAction("BLESS", 245, 13056);
    public static final PlayerAction DISPEL = new PlayerAction("DISPEL", 450, 15121);
    public static final PlayerAction DIRT_SPELL = new PlayerAction("DIRT_SPELL", 453, 769);
    public static final PlayerAction LOCATE_SOUL = new PlayerAction("LOCATE_SOUL", 419, 13073);
    public static final PlayerAction LIGHT_TOKEN = new PlayerAction("LIGHT_TOKEN", 421, 15121);
    public static final PlayerAction NOLOCATE = new PlayerAction("NOLOCATE", 451, 13056);
    public static final PlayerAction CURE_LIGHT = new PlayerAction("CURE_LIGHT", 246, 256);
    public static final PlayerAction CURE_MEDIUM = new PlayerAction("CURE_MEDIUM", 247, 256);
    public static final PlayerAction CURE_SERIOUS = new PlayerAction("CURE_SERIOUS", 248, 256);
    public static final PlayerAction HUMID_DRIZZLE = new PlayerAction("HUMID_DRIZZLE", 407, 2065);
    public static final PlayerAction WARD = new PlayerAction("WARD", 437, 2065);
    public static final PlayerAction WILD_GROWTH = new PlayerAction("WILD_GROWTH", 436, 2065);
    public static final PlayerAction LIGHT_OF_FO = new PlayerAction("LIGHT_OF_FO", 438, 2065);
    public static final PlayerAction WRATH_OF_MAGRANON = new PlayerAction("WRATH_OF_MAGRANON", 441, 2065);
    public static final PlayerAction DISINTEGRATE = new PlayerAction("DISINTEGRATE", 449, 16);
    public static final PlayerAction MASS_STAMINA = new PlayerAction("MASS_STAMINA", 425, 2065);
    public static final PlayerAction FIRE_PILLAR = new PlayerAction("FIRE_PILLAR", 420, 2065);
    public static final PlayerAction MOLE_SENSES = new PlayerAction("MOLE_SENSES", 439, 2065);
    public static final PlayerAction STRONGWALL = new PlayerAction("STRONGWALL", 440, 16);
    public static final PlayerAction LOCATE_ARTIFACT = new PlayerAction("LOCATE_ARTIFACT", 271, 2065);
    public static final PlayerAction TORNADO = new PlayerAction("TORNADO", 413, 2065);
    public static final PlayerAction TENTACLES = new PlayerAction("TENTACLES", 418, 2065);
    public static final PlayerAction REVEAL_CREATURES = new PlayerAction("REVEAL_CREATURES", 444, 2065);
    public static final PlayerAction ICE_PILLAR = new PlayerAction("ICE_PILLAR", 414, 2065);
    public static final PlayerAction REVEAL_SETTLEMENTS = new PlayerAction("REVEAL_SETTLEMENTS", 443, 2065);
    public static final PlayerAction FUNGUS_TRAP = new PlayerAction("FUNGUS_TRAP", 433, 2065);
    public static final PlayerAction PAINRAIN = new PlayerAction("PAINRAIN", 432, 2065);
    public static final PlayerAction FUNGUS = new PlayerAction("FUNGUS", 446, 2065);
    public static final PlayerAction SCORN_OF_LIBILA = new PlayerAction("SCORN_OF_LIBILA", 448, 2065);
    public static final PlayerAction BEARPAWS = new PlayerAction("BEARPAWS", 406, 12544);
    public static final PlayerAction WILLOWSPINE = new PlayerAction("WILLOWSPINE", 405, 12544);
    public static final PlayerAction SIXTH_SENSE = new PlayerAction("SIXTH_SENSE", 376, 12544);
    public static final PlayerAction MORNING_FOG = new PlayerAction("MORNING_FOG", 282, 12544);
    public static final PlayerAction CHARM = new PlayerAction("CHARM", 275, 12288);
    public static final PlayerAction REFRESH_SPELL = new PlayerAction("REFRESH_SPELL", 250, 12544);
    public static final PlayerAction OAKSHELL = new PlayerAction("OAKSHELL", 404, 12544);
    public static final PlayerAction FOREST_GIANT_STRENGTH = new PlayerAction("FOREST_GIANT_STRENGTH", 410, 12544);
    public static final PlayerAction TANGLEWEAVE = new PlayerAction("TANGLEWEAVE", 641, 12288);
    public static final PlayerAction GENESIS = new PlayerAction("GENESIS", 408, 12288);
    public static final PlayerAction HEAL_SPELL = new PlayerAction("HEAL_SPELL", 249, 12544);
    public static final PlayerAction FRANTIC_CHARGE = new PlayerAction("FRANTIC_CHARGE", 423, 12544);
    public static final PlayerAction FIRE_HEART = new PlayerAction("FIRE_HEART", 424, 12288);
    public static final PlayerAction DOMINATE = new PlayerAction("DOMINATE", 274, 12288);
    public static final PlayerAction SMITE = new PlayerAction("SMITE", 252, 12288);
    public static final PlayerAction GOAT_SHAPE = new PlayerAction("GOAT_SHAPE", 422, 12544);
    public static final PlayerAction SHARD_OF_ICE = new PlayerAction("SHARD_OF_ICE", 485, 12288);
    public static final PlayerAction EXCEL = new PlayerAction("EXCEL", 442, 12544);
    public static final PlayerAction WISDOM_OF_VYNORA = new PlayerAction("WISDOM_OF_VYNORA", 445, 12544);
    public static final PlayerAction DRAIN_HEALTH = new PlayerAction("DRAIN_HEALTH", 255, 12288);
    public static final PlayerAction PHANTASMS = new PlayerAction("PHANTASMS", 426, 12288);
    public static final PlayerAction ROTTING_GUT = new PlayerAction("ROTTING_GUT", 428, 12288);
    public static final PlayerAction WEAKNESS = new PlayerAction("WEAKNESS", 429, 12288);
    public static final PlayerAction TRUEHIT = new PlayerAction("TRUEHIT", 447, 12544);
    public static final PlayerAction HELL_STRENGTH = new PlayerAction("HELL_STRENGTH", 427, 12544);
    public static final PlayerAction DRAIN_STAMINA = new PlayerAction("DRAIN_STAMINA", 254, 12288);
    public static final PlayerAction WORM_BRAINS = new PlayerAction("WORM_BRAINS", 430, 12288);
    public static final PlayerAction LIBILAS_DEMISE = new PlayerAction("LIBILAS_DEMISE", 262, 768);
    public static final PlayerAction LURKER_IN_THE_WOODS = new PlayerAction("LURKER_IN_THE_WOODS", 458, 768);
    public static final PlayerAction LIFETRANSFER = new PlayerAction("LIFETRANSFER", 409, 768);
    public static final PlayerAction VESSEL = new PlayerAction("VESSEL", 272, 768);
    public static final PlayerAction DARK_MESSENGER = new PlayerAction("DARK_MESSENGER", 339, 768);
    public static final PlayerAction COURIER = new PlayerAction("COURIER", 338, 768);
    public static final PlayerAction VENOM = new PlayerAction("VENOM", 412, 768);
    public static final PlayerAction BREAK_ALTAR = new PlayerAction("BREAK_ALTAR", 258, 768);
    public static final PlayerAction FOS_TOUCH = new PlayerAction("FOS_TOUCH", 263, 768);
    public static final PlayerAction FOS_DEMISE = new PlayerAction("FOS_DEMISE", 259, 768);
    public static final PlayerAction DRAGONS_DEMISE = new PlayerAction("DRAGONS_DEMISE", 270, 768);
    public static final PlayerAction VYNORAS_DEMISE = new PlayerAction("VYNORAS_DEMISE", 261, 768);
    public static final PlayerAction MAGRANONS_SHIELD = new PlayerAction("MAGRANONS_SHIELD", 264, 768);
    public static final PlayerAction FLAMING_AURA = new PlayerAction("FLAMING_AURA", 277, 768);
    public static final PlayerAction LURKER_IN_THE_DARK = new PlayerAction("LURKER_IN_THE_DARK", 459, 768);
    public static final PlayerAction SELFHEALERS_DEMISE = new PlayerAction("SELFHEALERS_DEMISE", 268, 768);
    public static final PlayerAction AURA_OF_SHARED_PAIN = new PlayerAction("AURA_OF_SHARED_PAIN", 278, 768);
    public static final PlayerAction SUNDER = new PlayerAction("SUNDER", 253, 768);
    public static final PlayerAction ANIMALS_DEMISE = new PlayerAction("ANIMALS_DEMISE", 269, 768);
    public static final PlayerAction CIRCLE_OF_CUNNING = new PlayerAction("CIRCLE_OF_CUNNING", 276, 768);
    public static final PlayerAction OPULENCE = new PlayerAction("OPULENCE", 280, 768);
    public static final PlayerAction FROSTBRAND = new PlayerAction("FROSTBRAND", 417, 768);
    public static final PlayerAction MIND_STEALER = new PlayerAction("MIND_STEALER", 415, 768);
    public static final PlayerAction LURKER_IN_THE_DEEP = new PlayerAction("LURKER_IN_THE_DEEP", 457, 768);
    public static final PlayerAction VYNORAS_HAND = new PlayerAction("VYNORAS_HAND", 265, 768);
    public static final PlayerAction MEND = new PlayerAction("MEND", 251, 768);
    public static final PlayerAction WIND_OF_AGES = new PlayerAction("WIND_OF_AGES", 279, 768);
    public static final PlayerAction NIMBLENESS = new PlayerAction("NIMBLENESS", 416, 768);
    public static final PlayerAction HUMANS_DEMISE = new PlayerAction("HUMANS_DEMISE", 267, 768);
    public static final PlayerAction ROTTING_TOUCH = new PlayerAction("ROTTING_TOUCH", 281, 768);
    public static final PlayerAction MAGRANONS_DEMISE = new PlayerAction("MAGRANONS_DEMISE", 260, 768);
    public static final PlayerAction BLOODTHIRST = new PlayerAction("BLOODTHIRST", 454, 768);
    public static final PlayerAction WEB_ARMOUR = new PlayerAction("WEB_ARMOUR", 455, 768);
    public static final PlayerAction BLESSINGS_OF_THE_DARK = new PlayerAction("BLESSINGS_OF_THE_DARK", 456, 768);
    public static final PlayerAction LIBILAS_SHIELDING = new PlayerAction("LIBILAS_SHIELDING", 266, 768);
    public static final PlayerAction REBIRTH = new PlayerAction("REBIRTH", 273, 768);
    public static final PlayerAction SHOOT = new PlayerAction("SHOOT", 124, 12288);
    public static final PlayerAction QUICK_SHOT = new PlayerAction("QUICK_SHOT", 125, 12288);
    public static final PlayerAction SHOOT_TORSO = new PlayerAction("SHOOT_TORSO", 128, 12288);
    public static final PlayerAction SHOOT_LEFTARM = new PlayerAction("SHOOT_LEFTARM", 129, 12288);
    public static final PlayerAction SHOOT_RIGHTARM = new PlayerAction("SHOOT_RIGHTARM", 130, 12288);
    public static final PlayerAction SHOOT_HEAD = new PlayerAction("SHOOT_HEAD", 126, 12288);
    public static final PlayerAction SHOOT_FACE = new PlayerAction("SHOOT_FACE", 127, 12288);
    public static final PlayerAction SHOOT_LEGS = new PlayerAction("SHOOT_LEGS", 131, 12288);
    public static final PlayerAction INVESTIGATE = new PlayerAction("INVESTIGATE", 910, 1);
    public static final PlayerAction IDENTIFY = new PlayerAction("IDENTIFY", 911, 768);
    public static final PlayerAction COMBINE_FRAGMENT = new PlayerAction("COMBINE_FRAGMENT", 912, 768);

    public PlayerAction(String bind, short aId, int aTargetMask) {
        this(bind, aId, aTargetMask, null);
    }

    public PlayerAction(String bind, short aId, int aTargetMask, String aName) {
        this(aId, aTargetMask, aName, false);
        actionIds.put(aId, this);
        this.bind = bind;
    }

    public PlayerAction(short aId, int aTargetMask, String aName, boolean aInstant) {
        this.id = aId;
        this.name = aName;
        this.instant = aInstant;
        this.targetMask = aTargetMask;
        this.bind = null;
        switch (aId) {
            case 1: 
            case 6: 
            case 7: 
            case 54: 
            case 55: 
            case 59: 
            case 86: 
            case 93: 
            case 638: {
                this.atomic = true;
                break;
            }
            default: {
                this.atomic = false;
            }
        }
    }

    public final short getId() {
        return this.id;
    }

    public final String getName() {
        return this.name;
    }

    public String getBind() {
        return this.bind;
    }

    public boolean isInstant() {
        return this.instant;
    }

    public boolean isAtomic() {
        return this.atomic;
    }

    public int getTargetMask() {
        return this.targetMask;
    }

    public static PlayerAction getByActionId(short id) {
        return actionIds.get(id);
    }
}

