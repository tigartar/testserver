/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

public interface ProtoConstants {
    public static final int PROTOCOL_VERSION = 250990585;
    public static final String PROTOCOL_VERSION_STR = "0xEF5CFF9s";
    public static final int TOGGLE_ARCHERY = 100;
    public static final int CLIENT_FEATURE_COMPASS = 0;
    public static final int CLIENT_FEATURE_USING_BINOCULARS = 1;
    public static final int CLIENT_FEATURE_TOOLBELT = 2;
    public static final byte ATTACH_EFFECT_LIGHT = 0;
    public static final byte ATTACH_EFFECT_FIRE = 1;
    public static final byte ATTACH_EFFECT_TRANSPARENT = 2;
    public static final byte ATTACH_EFFECT_GLOW = 3;
    public static final byte ATTACH_EFFECT_FLAME = 4;
    public static final byte ATTACH_EFFECT_FIRE_FLAME = 5;
    public static final byte ATTACH_EFFECT_ICE_RING = 6;
    public static final byte ATTACH_EFFECT_MUSHROOM_RING = 7;
    public static final byte ATTACH_EFFECT_SCORN_OF_LIBILA_DAMAGE = 8;
    public static final byte ATTACH_EFFECT_SCORN_OF_LIBILA_HEAL = 9;
    public static final byte ATTACH_EFFECT_SMITE = 10;
    public static final byte ATTACH_EFFECT_HEAL = 11;
    public static final byte ATTACH_EFFECT_DISEASE = 12;
    public static final byte EQUIPMENT_SLOT_LEFT_HAND = 0;
    public static final byte EQUIPMENT_SLOT_RIGHT_HAND = 1;
    public static final byte EQUIPMENT_SLOT_ARMOR_HEAD = 2;
    public static final byte EQUIPMENT_SLOT_ARMOR_TORSO = 3;
    public static final byte EQUIPMENT_SLOT_ARMOR_LEGS = 4;
    public static final byte EQUIPMENT_SLOT_ARMOR_LEFT_ARM = 5;
    public static final byte EQUIPMENT_SLOT_ARMOR_RIGHT_ARM = 6;
    public static final byte EQUIPMENT_SLOT_ARMOR_LEFT_HAND = 7;
    public static final byte EQUIPMENT_SLOT_ARMOR_RIGHT_HAND = 8;
    public static final byte EQUIPMENT_SLOT_ARMOR_LEFT_FOOT = 9;
    public static final byte EQUIPMENT_SLOT_ARMOR_RIGHT_FOOT = 10;
    public static final byte EQUIPMENT_SLOT_SHIELD = 11;
    public static final byte EQUIPMENT_SLOT_MISC_TORSO = 12;
    public static final byte EQUIPMENT_SLOT_MISC_LEGS = 13;
    public static final byte EQUIPMENT_SLOT_CAPE = 14;
    public static final byte EQUIPMENT_SLOT_TABARD = 15;
    public static final byte EQUIPMENT_SLOT_RIGHT_RING = 16;
    public static final byte EQUIPMENT_SLOT_LEFT_RING = 17;
    public static final byte EQUIPMENT_SLOT_ARMOR_LEFT_SHOULDER = 18;
    public static final byte EQUIPMENT_SLOT_ARMOR_RIGHT_SHOULDER = 19;
    public static final byte EQUIPMENT_SLOT_BACK = 20;
    public static final byte EQUIPMENT_SLOT_NECK = 21;
    public static final byte EQUIPMENT_SLOT_BELT = 22;
    public static final byte EQUIPMENT_SLOT_QUIVER = 23;
    public static final byte EQUIPMENT_SLOT_ARMOR_BODY = 24;
    public static final byte EQUIPMENT_SLOT_ARMOR_FACE = 25;
    public static final byte EQUIPMENT_SLOT_LEFT_ARM = 26;
    public static final byte EQUIPMENT_SLOT_RIGHT_ARM = 27;
    public static final int EQUIPMENT_SLOTS = 28;
    public static final byte EVENT_FIGHT = 0;
    public static final byte EVENT_WORK = 1;
    public static final byte EVENT_ERROR = 2;
    public static final byte EVENT_OTHER = 3;
    public static final byte north = 0;
    public static final byte northeast = 1;
    public static final byte east = 2;
    public static final byte southeast = 3;
    public static final byte south = 4;
    public static final byte southwest = 5;
    public static final byte west = 6;
    public static final byte northwest = 7;
    public static final int TOGGLE_CLIMBING = 0;
    public static final int TOGGLE_FAITHFUL = 1;
    public static final int TOGGLE_LAWFUL = 2;
    public static final int TOGGLE_STEALTH = 3;
    public static final int TOGGLE_AUTOFIGHT = 4;
    public static final int TOGGLE_HEALTHY = 5;
    public static final int TOGGLE_SLEEP = 6;
    public static final int LOGIN_NO_SUCH_ACCOUNT = -1;
    public static final int LOGIN_WRONG_PASSWORD = -2;
    public static final byte CMD_SLEEP_BONUS_INFO = 1;
    public static final byte CMD_CUSTOMIZE_FACE = 2;
    public static final byte CMD_ACK = 3;
    public static final byte CMD_CLIENT_QUIT = 4;
    public static final byte CMD_STATUS_WEIGHT = 5;
    public static final byte CMD_SET_CREATURE_ATTITUDE = 6;
    public static final byte CMD_ADD_SPELLEFFECT = 7;
    public static final byte CMD_LOGOUT = 8;
    public static final byte CMD_NOT_MOVE_CREATURE = 9;
    public static final byte CMD_REMOVE_ITEM = 10;
    public static final byte CMD_SET_CREATUREDAMAGE = 11;
    public static final byte CMD_ADD_FENCE = 12;
    public static final byte CMD_REMOVE_FENCE = 13;
    public static final byte CMD_DELETE_CREATURE = 14;
    public static final byte CMD_FORM_RESPONSE = 15;
    public static final byte CMD_FATAL_ERROR = 16;
    public static final byte CMD_REMOVE_SPELLEFFECT = 17;
    public static final byte CMD_UNATTACH_EFFECT = 18;
    public static final byte CMD_SET_ITEM_STATE = 19;
    public static final byte CMD_AVAILABLE_ACTIONS = 20;
    public static final byte CMD_ADD_CLOTHING = 21;
    public static final byte CMD_STATE_DAMAGE = 22;
    public static final byte CMD_RECONNECT = 23;
    public static final byte CMD_PLAY_ANIMATION = 24;
    public static final byte CMD_SET_TARGET = 25;
    public static final byte CMD_SET_FIGHTSTYLE = 26;
    public static final byte CMD_REMOVE_CLOTHING = 27;
    public static final byte CMD_STUNNED = 28;
    public static final byte CMD_MORE_ITEMS = 29;
    public static final byte CMD_CREATURE_LAYER = 30;
    public static final byte CMD_STATE = 31;
    public static final byte CMD_SPEEDMODIFIER = 32;
    public static final byte CMD_MISSION_STATE = 33;
    public static final byte CMD_STATE_HOUSEWALL_DAMAGE = 34;
    public static final byte CMD_PROJECTILE = 35;
    public static final byte CMD_MOVE_CREATURE = 36;
    public static final byte CMD_REMOVE_EFFECT = 37;
    public static final byte CMD_NEW_ACHIEVEMENT = 38;
    public static final byte CMD_SEND_NEW_KINGODM = 39;
    public static final byte CMD_SEND_ALL_KINGODM = 40;
    public static final byte CMD_ERROR = 41;
    public static final byte CMD_SET_TRADE_AGREE = 42;
    public static final byte CMD_MOVE_INVENTORY = 43;
    public static final byte CMD_RENAME_ITEM = 44;
    public static final byte CMD_MESSAGE_MULTICOLORED = 45;
    public static final byte CMD_WEATHER_UPDATE = 46;
    public static final byte CMD_RENAME = 47;
    public static final byte CMD_REMOVE_STRUCTURE = 48;
    public static final byte CMD_ADD_WALL = 49;
    public static final byte CMD_FOCUS_LEVEL_CHANGED = 50;
    public static final byte CMD_TELEPORT = 51;
    public static final byte CMD_NEW_FACE = 52;
    public static final byte CMD_HASTARGET = 53;
    public static final byte CMD_REMOVE_WALL = 54;
    public static final byte CMD_REMOVE_HORSE_ITEM = 55;
    public static final byte CMD_DRUMROLL_STARTED = 56;
    public static final byte CMD_TAB_CLOSED = 57;
    public static final byte CMD_OPEN_INVENTORY_CONTAINER = 58;
    public static final byte CMD_AVAILABLE_SERVER = 59;
    public static final byte CMD_MOUNTSPEED = 60;
    public static final byte CMD_STATUS_HUNGER = 61;
    public static final byte CMD_TOGGLE_SWITCH = 62;
    public static final byte CMD_SET_VEHICLE_CONTROLLER = 63;
    public static final byte CMD_ADD_EFFECT = 64;
    public static final byte CMD_DEAD = 65;
    public static final byte CMD_UPDATE_SKILL = 66;
    public static final byte CMD_ROTATE = 67;
    public static final byte CMD_UPDATE_INVENTORY = 68;
    public static final byte CMD_RECEIVED = 69;
    public static final byte CMD_TILESTRIP_NEAR = 70;
    public static final byte CMD_STOP_USE_ITEM = 71;
    public static final byte CMD_MOVE_CREATURE_AND_SET_Z = 72;
    public static final byte CMD_TILESTRIP = 73;
    public static final byte CMD_RESIZE = 74;
    public static final byte CMD_DEATH_ANIMATION_AND_CORPSE = 75;
    public static final byte CMD_ADD_TO_INVENTORY = 76;
    public static final byte CMD_REMOVE_FLOOR = 77;
    public static final byte CMD_ITEM_MODELNAME = 78;
    public static final byte CMD_CLIMB = 79;
    public static final byte CMD_WOUND = 80;
    public static final byte CMD_WOUND_ACTION_ADD = 81;
    public static final byte CMD_ADD_FLOOR = 82;
    public static final byte CMD_OPEN_FENCE = 83;
    public static final byte CMD_WOUND_ACTION_REMOVE = 84;
    public static final byte CMD_SET_STANCE = 85;
    public static final byte CMD_PLAYSOUND = 86;
    public static final byte CMD_TIMELEFT = 87;
    public static final byte CMD_ANIMATION_WITH_TARGET = 88;
    public static final byte CMD_UPDATE_FRIENDLIST = 89;
    public static final byte CMD_STATUS_STAMINA = 90;
    public static final byte CMD_TRADE_CHANGED = 91;
    public static final byte CMD_REPAINT = 92;
    public static final byte CMD_MESSAGE_TYPED = 93;
    public static final byte CMD_SETGROUNDOFFSET = 94;
    public static final byte CMD_ADVANCED_EFFECT = 95;
    public static final byte CMD_BUILD_MARK = 96;
    public static final byte CMD_ACTION = 97;
    public static final byte CMD_FIGHT_MOVE_OPTIONS = 98;
    public static final byte CMD_MESSAGE = 99;
    public static final byte CMD_ACHIEVEMENT_LIST = 100;
    public static final byte CMD_SET_EQUIPMENT = 101;
    public static final byte CMD_TILESTRIP_CAVE = 102;
    public static final byte CMD_TILESTRIP_FAR = 103;
    public static final byte CMD_TEAM_INVITE = 104;
    public static final byte CMD_STATUS_THIRST = 105;
    public static final byte CMD_BML_FORM = 106;
    public static final byte CMD_SERVER_TIME = 107;
    public static final byte CMD_ADD_CREATURE = 108;
    public static final byte CMD_ATTACH_EFFECT = 109;
    public static final byte CMD_USE_ITEM = 110;
    public static final byte CMD_ATTACH_CREATURE = 111;
    public static final byte CMD_ADD_STRUCTURE = 112;
    public static final byte REMOVE_MISSION_STATE = 113;
    public static final byte CMD_PART_GROUP = 114;
    public static final byte CMD_PLAYMUSIC = 115;
    public static final byte CMD_OPEN_INVENTORY_WINDOW = 116;
    public static final byte CMD_WINDIMPACT = 117;
    public static final byte CMD_LOGIN_FAILURE = 118;
    public static final byte CMD_OPEN_TRADE_WINDOW = 119;
    public static final byte CMD_CLOSE_INVENTORY_WINDOW = 120;
    public static final byte CMD_CLOSE_TRADE_WINDOW = 121;
    public static final byte CMD_OPEN_WALL = 122;
    public static final byte CMD_TEAM_MUTE = 123;
    public static final byte CMD_SET_SKILL = 124;
    public static final byte CMD_SET_PASSABLE = 125;
    public static final byte CMD_REQUEST_ACTIONS = 126;
    public static final byte CMD_CLOSE_WALL = 127;
    public static final byte CMD_GET_EIGC_NAME = -1;
    public static final byte CMD_GET_GAME_NAME = -2;
    public static final byte CMD_EIGC_LOGIN = -3;
    public static final byte CMD_ADD_TILE_EFFECT = -4;
    public static final byte CMD_REMOVE_TILE_EFFECT = -5;
    public static final byte CMD_VERIFY_CLIENT_VERSION = -6;
    public static final byte CMD_UPDATE_INVENTORY_ATTRIBUTE = -7;
    public static final byte CMD_SET_EIGC_SERVICE_STATE = -8;
    public static final byte CMD_ADD_ITEM = -9;
    public static final byte CMD_REMOVE_FROM_INVENTORY = -10;
    public static final byte CMD_SHOW_HTML = -11;
    public static final byte CMD_ACTION_STRING = -12;
    public static final byte CMD_JOIN_GROUP = -13;
    public static final byte CMD_FIGHT_STATUS = -14;
    public static final byte CMD_LOGIN = -15;
    public static final byte CMD_EMPTY = -16;
    public static final byte CMD_SPECIALMOVE = -17;
    public static final byte CMD_STATUS_STRING = -18;
    public static final byte CMD_SET_WATER = -19;
    public static final byte CMD_SERVERPORTAL = -20;
    public static final byte CMD_SET_BRIDGE = -21;
    public static final byte CMD_TAB_SELECTED = -22;
    public static final byte CMD_REQUEST_SELECT = -23;
    public static final byte SEND_SELECT_LIST = 0;
    public static final byte UPDATE_SELECTION = 1;
    public static final byte KEEP_SELECTION = 2;
    public static final byte CMD_SET_FLYING = -24;
    public static final byte CMD_BRIDGE = -25;
    public static final byte CMD_PERMISSIONS = -26;
    public static final byte CMD_TARGETSTATUS = -27;
    public static final byte CMD_STARTMOVING = -28;
    public static final byte CMD_MOVE_CREATURE_MOD = -29;
    public static final byte CMD_TOGGLE_CLIENT_FEATURE = -30;
    public static final byte CMD_ADD_HORSE_ITEM = -31;
    public static final byte CMD_EQUIP_ITEM = -32;
    public static final byte CMD_UPDATE_PLAYER_TITLE = -33;
    public static final byte CMD_TICKET_ADD = -34;
    public static final byte CMD_CREATION_WINDOW = -35;
    public static final byte CMD_TICKET_REMOVE = -36;
    public static final byte CMD_UPDATE_PLAYER_KINGDOM = -37;
    public static final byte CMD_MOVE = -38;
    public static final byte CMD_PERSONAL_GOAL_LIST = -39;
    public static final byte CMD_UPDATE_PERSONAL_GOAL = -40;
    public static final byte CMD_SHOW_PERSONAL_GOALS = -41;
    public static final byte CREATION_WINDOW_OPEN = 1;
    public static final byte CREATION_WINDOW_CLOSE = 2;
    public static final byte CREATION_WINDOW_UPDATE = 3;
    public static final byte CMD_SEND_WINDOW_TYPE_DATA = -42;
    public static final byte CMD_MAP_ANNOTATIONS = -43;
    public static final byte CMD_OPEN_WINDOW_TYPE = -44;
    public static final byte CMD_SEND_MAP_INFO = -45;
    public static final byte CMD_ITEM_CREATION_LIST = -46;
    public static final byte CMD_STATUS_EFFECT_BAR = -47;
    public static final byte CMD_CHANGE_MODELNAME = -48;
    public static final byte CMD_SEND_PLONK = -49;
    public static final byte CMD_SEND_VALREI_MAP_INFO = -50;
    public static final byte CMD_SHOW_DEED_PLAN = -51;
    public static final byte CMD_STEAM_AUTHENTICATION = -52;
    public static final byte CMD_UPDATE_DECORATIONS = -53;
    public static final byte CMD_UPDATE_CREATURE_RARITY = -54;
    public static final byte CMD_COOKBOOK = -55;
    public static final byte CMD_WAYSTONE = -56;
    public static final byte CMD_SHOW_LINKS = -57;
    public static final byte CMD_ADD_MINEDOOR = -58;
    public static final byte CMD_REMOVE_MINEDOOR = -59;
    public static final byte CMD_OPEN_MINEDOOR = -60;
    public static final byte CMD_CLOSE_MINEDOOR = -61;
    public static final byte CMD_VALREIFIGHT = -62;
    public static final byte CMD_PLACE_ITEM = -63;
    public static final byte CMD_FISH = -64;
    public static final byte CMD_CLEAR_WINDOW = -65;
    public static final byte CMD_OPENCLOSE_WINDOW = -66;
    public static final byte ADD_EFFECT_TO_BAR = 0;
    public static final byte REMOVE_EFFECT_FROM_BAR = 1;
    public static final byte OPEN_WINDOW = 0;
    public static final byte CLOSE_WINDOW = 1;
    public static final byte DISABLE_QUICKBUTTON = 3;
    public static final byte ENABLE_QUICKBUTTON = 4;
    public static final byte DISABLE_QUICKBUTTON_ALL = 5;
    public static final byte ENABLE_QUICKBUTTON_ALL = 6;
    public static final byte REQUEST_PARTIAL_LIST = 0;
    public static final byte ADD_TO_CREATION_WINDOW = 1;
    public static final byte REMOVE_CREATION_GROUND_ITEM = 2;
    public static final byte FULL_CREATIONS_LIST = 3;
    public static final byte FULL_CATEGORY_LIST = 4;
    public static final byte ACTION_RESULT = 5;
    public static final byte UPDATE_GROUND_ITEM = 6;
    public static final byte ADD_TILE_BORDER = 7;
    public static final byte ADD_FENCE_TO_WINDOW = 8;
    public static final byte FINISHED_STRUCTURE_ACTION_IN_CREATION_WINDOW = 9;
    public static final byte ADD_WALL_TO_CREATION_WINDOW = 10;
    public static final byte ADD_FLOOR_OR_ROOF_TO_CREATION_WINDOW = 11;
    public static final byte ADD_BRIDGEPART_TO_CREATION_WINDOW = 12;
    public static final byte ADD_ANNOTATION = 0;
    public static final byte REMOVE_ANNOTATION = 1;
    public static final byte REQUEST_MAP_PERMISSIONS = 2;
    public static final byte CLEAR_ANNOTATION = 3;
    public static final byte VALREI_DIETY = 0;
    public static final byte VALREI_ITEM = 1;
    public static final byte VALREI_DEMIGOD = 2;
    public static final byte VALREI_ALLY = 3;
    public static final byte TIME_UPDATE = 4;
    public static final byte MANAGE_RECRUITMENT_WINDOW = 0;
    public static final byte LOOK_FOR_VILLAGE_WINDOW = 1;
    public static final byte ITEM_NEW_DATA = 1;
    public static final byte SHOW_PLAN = 1;
    public static final byte HIDE_PLAN = 2;
    public static final byte ADD_PART = 3;
    public static final byte REMOVE_PART = 4;
    public static final int SET_FLOOR_OVERRIDE = -9999;
    public static final byte PERMISSIONS_SHOW = 0;
    public static final byte PERMISSIONS_ADDED_MANUALLY = 1;
    public static final byte PERMISSIONS_APPLY_CHANGES = 2;
    public static final byte PERMISSIONS_BACK = 3;
    public static final byte PERMISSIONS_HIDE = 4;
    public static final byte DEED_PLAN_SHOW = 0;
    public static final byte DEED_PLAN_EXPORT = 1;
    public static final byte DECORATION_ADD = 0;
    public static final byte DECORATION_REMOVE = 1;
    public static final byte COOKBOOK_RECIPE_LIST = 0;
    public static final byte COOKBOOK_RECIPE = 1;
    public static final byte COOKBOOK_FAVOURITE = 2;
    public static final byte COOKBOOK_NOTES = 3;
    public static final byte COOKBOOK_MARK = 4;
    public static final byte COOKBOOK_REMOVE = 5;
    public static final byte LINKS_SHOW = 0;
    public static final byte LINKS_HIDE = 1;
    public static final byte LINKS_PROTECTION = 2;
    public static final byte FIGHTS_LIST = 0;
    public static final byte FIGHTS_DETAIL = 1;
    public static final byte PLACEABLE_NOTHING = 0;
    public static final byte PLACEABLE_PARENT = 1;
    public static final byte PLACEABLE_INSIDE = 2;
    public static final byte FISH_START = 0;
    public static final byte FISH_BITE = 1;
    public static final byte FISH_MOVED_ON = 2;
    public static final byte FISH_HOOKED = 3;
    public static final byte FISH_MISSED = 4;
    public static final byte FISH_NO_FISH = 5;
    public static final byte FISH_LINE_SNAPPED = 6;
    public static final byte FISH_ROD_BROKE = 7;
    public static final byte FISH_TIME_OUT = 8;
    public static final byte FISH_CASTED = 9;
    public static final byte FISH_CANCEL = 10;
    public static final byte FISH_STRIKE = 11;
    public static final byte FISH_CAUGHT = 12;
    public static final byte FISH_GOT_AWAY = 13;
    public static final byte FISH_SWAM_AWAY = 14;
    public static final byte FISH_STOP = 15;
    public static final byte FISH_MOVE = 16;
    public static final byte FISH_PULL = 17;
    public static final byte FISH_PAUSE = 18;
    public static final byte FISH_MOVING_ON = 19;
    public static final byte SPEAR_START = 20;
    public static final byte SPEAR_MOVE = 21;
    public static final byte SPEAR_HIT = 22;
    public static final byte SPEAR_MISSED = 23;
    public static final byte SPEAR_NO_FISH = 24;
    public static final byte SPEAR_TIME_OUT = 25;
    public static final byte SPEAR_STRIKE = 26;
    public static final byte SPEAR_CANCEL = 27;
    public static final byte SPEAR_SWAM_AWAY = 28;
    public static final byte SPEAR_STOP = 29;
    public static final byte NET_START = 40;
    public static final byte SHOW_FISH_SPOTS = 45;
    public static final byte PERSONAL_GOALS_OLD = 0;
    public static final byte PERSONAL_GOALS_J_TIERADD = 1;
    public static final byte PERSONAL_GOALS_J_TIERUPDATE = 2;
    public static final byte PERSONAL_GOALS_J_ACHVUPDATE = 3;
    public static final byte PERSONAL_GOALS_J_TUTADD = 4;
    public static final byte PERSONAL_GOALS_J_TUTOPEN = 5;
    public static final byte ITEM_NEW_PARENT = 2;
    public static final byte ITEM_NEW_CUSTOM_NAME = 3;
    public static final byte ITEM_NEW_COLOR = 4;
    public static final byte ITEM_NEW_TYPE = 5;
    public static final byte ITEM_NEW_PRICE = 6;
    public static final byte ITEM_NEW_TEMPERATURE = 7;
    public static final byte ITEM_NEW_INSCRIPTION = 8;
    public static final byte ITEM_TEMPERATURE_FROZEN = -1;
    public static final byte ITEM_TEMPERATURE_NEUTRAL = 0;
    public static final byte ITEM_TEMPERATURE_WARM = 1;
    public static final byte ITEM_TEMPERATURE_HOT = 2;
    public static final byte ITEM_TEMPERATURE_BOILING = 3;
    public static final byte ITEM_TEMPERATURE_SEARING = 4;
    public static final byte ITEM_TEMPERATURE_GLOWING = 5;
    public static final byte TELE_STOP_COMMANDING = 0;
    public static final byte TELE_START_COMMAND_BOAT = 1;
    public static final byte TELE_START_COMMAND_CART = 2;
    public static final byte TELE_START_COMMAND_CREATURE = 3;
    public static final byte PROJECTILE_NEW_TYPE = -1;
    public static final byte PROJECTILE_ARROW = 1;
    public static final byte PROJECTILE_CATAPULT = 2;
    public static final byte PROJECTILE_LAVA_BOULDER = 3;
    public static final byte PROJECTILE_SPELL_SHARD_OF_ICE = 4;
    public static final byte PROJECTILE_PEW_ICE = 5;
    public static final byte PROJECTILE_PEW_ACID = 6;
    public static final byte PROJECTILE_PEW_FIRE = 7;
    public static final byte PROJECTILE_PEW_LIGHTNING = 8;
    public static final byte PROJECTILE_BALLISTA = 9;
    public static final int MAGIC_OBJECT_ON_GROUND = -3000;
    public static final String MAGIC_CLIENT_CHECK = "PERFORM CLIENT VERSION TEST HMD";
    public static final long PLAYER_INVENTORY_ID = -1L;
    public static final long PLAYER_EQUIPMENT_ID = -2L;
    public static final long TRADE_OFFER_PASSIVE = 1L;
    public static final long TRADE_OFFER_ACTIVE = 2L;
    public static final long TRADE_SELECTED_FROM_PASSIVE = 3L;
    public static final long TRADE_SELECTED_FROM_ACTIVE = 4L;
    public static final byte EIGC_SERVICE_NONE = 0;
    public static final byte EIGC_SERVICE_PROXIMITY = 1;
    public static final byte EIGC_SERVICE_TEAM = 2;
    public static final byte EIGC_SERVICE_LECTURE = 4;
    public static final byte EIGC_SERVICE_P2P = 8;
    public static final byte EIGC_SERVICE_HIFI = 16;
    public static final byte PROTOCOL_KINGDOM_UNKNOWN = -1;
    public static final byte PROTOCOL_KINGDOM_NONE = 0;
    public static final byte PROTOCOL_KINGDOM_JENN = 1;
    public static final byte PROTOCOL_KINGDOM_MOLREHAN = 2;
    public static final byte PROTOCOL_KINGDOM_HOTS = 3;
    public static final byte PROTOCOL_KINGDOM_FREEDOM = 4;
    public static final byte LOGIN_NORMAL = 0;
    public static final byte LOGIN_DEV = 1;
    public static final byte LOGIN_FLIGHT = 2;
    public static final byte CAT_NONE = 0;
    public static final byte CAT_ACCOUNT = 1;
    public static final byte CAT_BOAT = 2;
    public static final byte CAT_BUG = 3;
    public static final byte CAT_FORUM = 4;
    public static final byte CAT_GRIEF = 5;
    public static final byte CAT_HORSE = 6;
    public static final byte CAT_PASSWORD = 7;
    public static final byte CAT_PAYMENT = 8;
    public static final byte CAT_STUCK = 9;
    public static final byte CAT_OTHER = 10;
    public static final byte CAT_WATCH = 11;
    public static final byte MUTE_NONE = 0;
    public static final byte MUTE_WARN = 1;
    public static final byte MUTE_MUTE = 2;
    public static final long ALLOW_ALLIES = -20L;
    public static final long ALLOW_CITIZENS = -30L;
    public static final long ALLOW_KINGDOM = -40L;
    public static final long ALLOW_EVERYONE = -50L;
    public static final long ALLOW_ROLE_PERMISSION = -60L;
    public static final byte M_NONE = 0;
    public static final byte M_SYSTEM = 1;
    public static final byte M_INFO = 2;
    public static final byte M_FAIL = 3;
    public static final byte M_HOSTILE = 4;
    public static final float ITEM_PLACING_DISTANCE = 4.0f;

    public static enum InfectionSeverity {
        verylight(1),
        light(2),
        medium(3),
        bad(4),
        severe(5);

        private byte infectionSeverity;

        private InfectionSeverity(byte protocolValue) {
            this.infectionSeverity = protocolValue;
        }

        public byte getProtocolValue() {
            return this.infectionSeverity;
        }
    }

    public static enum WoundSeverity {
        verylight(1),
        light(1),
        medium(3),
        bad(4),
        severe(5);

        private byte woundSeverity;

        private WoundSeverity(byte protocolValue) {
            this.woundSeverity = protocolValue;
        }

        public byte getProtocolValue() {
            return this.woundSeverity;
        }
    }

    public static enum WoundType {
        crush(0),
        slash(1),
        pierce(2),
        bite(3),
        burn(4),
        poison(5),
        infection(6),
        water(7),
        cold(8),
        internal(9),
        acid(10);

        private byte woundType = 0;

        private WoundType(byte protocolValue) {
            this.woundType = protocolValue;
        }

        public byte getProtocolValue() {
            return this.woundType;
        }
    }
}

