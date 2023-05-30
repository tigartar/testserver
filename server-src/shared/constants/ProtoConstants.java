package com.wurmonline.shared.constants;

public interface ProtoConstants {
   int PROTOCOL_VERSION = 250990585;
   String PROTOCOL_VERSION_STR = "0xEF5CFF9s";
   int TOGGLE_ARCHERY = 100;
   int CLIENT_FEATURE_COMPASS = 0;
   int CLIENT_FEATURE_USING_BINOCULARS = 1;
   int CLIENT_FEATURE_TOOLBELT = 2;
   byte ATTACH_EFFECT_LIGHT = 0;
   byte ATTACH_EFFECT_FIRE = 1;
   byte ATTACH_EFFECT_TRANSPARENT = 2;
   byte ATTACH_EFFECT_GLOW = 3;
   byte ATTACH_EFFECT_FLAME = 4;
   byte ATTACH_EFFECT_FIRE_FLAME = 5;
   byte ATTACH_EFFECT_ICE_RING = 6;
   byte ATTACH_EFFECT_MUSHROOM_RING = 7;
   byte ATTACH_EFFECT_SCORN_OF_LIBILA_DAMAGE = 8;
   byte ATTACH_EFFECT_SCORN_OF_LIBILA_HEAL = 9;
   byte ATTACH_EFFECT_SMITE = 10;
   byte ATTACH_EFFECT_HEAL = 11;
   byte ATTACH_EFFECT_DISEASE = 12;
   byte EQUIPMENT_SLOT_LEFT_HAND = 0;
   byte EQUIPMENT_SLOT_RIGHT_HAND = 1;
   byte EQUIPMENT_SLOT_ARMOR_HEAD = 2;
   byte EQUIPMENT_SLOT_ARMOR_TORSO = 3;
   byte EQUIPMENT_SLOT_ARMOR_LEGS = 4;
   byte EQUIPMENT_SLOT_ARMOR_LEFT_ARM = 5;
   byte EQUIPMENT_SLOT_ARMOR_RIGHT_ARM = 6;
   byte EQUIPMENT_SLOT_ARMOR_LEFT_HAND = 7;
   byte EQUIPMENT_SLOT_ARMOR_RIGHT_HAND = 8;
   byte EQUIPMENT_SLOT_ARMOR_LEFT_FOOT = 9;
   byte EQUIPMENT_SLOT_ARMOR_RIGHT_FOOT = 10;
   byte EQUIPMENT_SLOT_SHIELD = 11;
   byte EQUIPMENT_SLOT_MISC_TORSO = 12;
   byte EQUIPMENT_SLOT_MISC_LEGS = 13;
   byte EQUIPMENT_SLOT_CAPE = 14;
   byte EQUIPMENT_SLOT_TABARD = 15;
   byte EQUIPMENT_SLOT_RIGHT_RING = 16;
   byte EQUIPMENT_SLOT_LEFT_RING = 17;
   byte EQUIPMENT_SLOT_ARMOR_LEFT_SHOULDER = 18;
   byte EQUIPMENT_SLOT_ARMOR_RIGHT_SHOULDER = 19;
   byte EQUIPMENT_SLOT_BACK = 20;
   byte EQUIPMENT_SLOT_NECK = 21;
   byte EQUIPMENT_SLOT_BELT = 22;
   byte EQUIPMENT_SLOT_QUIVER = 23;
   byte EQUIPMENT_SLOT_ARMOR_BODY = 24;
   byte EQUIPMENT_SLOT_ARMOR_FACE = 25;
   byte EQUIPMENT_SLOT_LEFT_ARM = 26;
   byte EQUIPMENT_SLOT_RIGHT_ARM = 27;
   int EQUIPMENT_SLOTS = 28;
   byte EVENT_FIGHT = 0;
   byte EVENT_WORK = 1;
   byte EVENT_ERROR = 2;
   byte EVENT_OTHER = 3;
   byte north = 0;
   byte northeast = 1;
   byte east = 2;
   byte southeast = 3;
   byte south = 4;
   byte southwest = 5;
   byte west = 6;
   byte northwest = 7;
   int TOGGLE_CLIMBING = 0;
   int TOGGLE_FAITHFUL = 1;
   int TOGGLE_LAWFUL = 2;
   int TOGGLE_STEALTH = 3;
   int TOGGLE_AUTOFIGHT = 4;
   int TOGGLE_HEALTHY = 5;
   int TOGGLE_SLEEP = 6;
   int LOGIN_NO_SUCH_ACCOUNT = -1;
   int LOGIN_WRONG_PASSWORD = -2;
   byte CMD_SLEEP_BONUS_INFO = 1;
   byte CMD_CUSTOMIZE_FACE = 2;
   byte CMD_ACK = 3;
   byte CMD_CLIENT_QUIT = 4;
   byte CMD_STATUS_WEIGHT = 5;
   byte CMD_SET_CREATURE_ATTITUDE = 6;
   byte CMD_ADD_SPELLEFFECT = 7;
   byte CMD_LOGOUT = 8;
   byte CMD_NOT_MOVE_CREATURE = 9;
   byte CMD_REMOVE_ITEM = 10;
   byte CMD_SET_CREATUREDAMAGE = 11;
   byte CMD_ADD_FENCE = 12;
   byte CMD_REMOVE_FENCE = 13;
   byte CMD_DELETE_CREATURE = 14;
   byte CMD_FORM_RESPONSE = 15;
   byte CMD_FATAL_ERROR = 16;
   byte CMD_REMOVE_SPELLEFFECT = 17;
   byte CMD_UNATTACH_EFFECT = 18;
   byte CMD_SET_ITEM_STATE = 19;
   byte CMD_AVAILABLE_ACTIONS = 20;
   byte CMD_ADD_CLOTHING = 21;
   byte CMD_STATE_DAMAGE = 22;
   byte CMD_RECONNECT = 23;
   byte CMD_PLAY_ANIMATION = 24;
   byte CMD_SET_TARGET = 25;
   byte CMD_SET_FIGHTSTYLE = 26;
   byte CMD_REMOVE_CLOTHING = 27;
   byte CMD_STUNNED = 28;
   byte CMD_MORE_ITEMS = 29;
   byte CMD_CREATURE_LAYER = 30;
   byte CMD_STATE = 31;
   byte CMD_SPEEDMODIFIER = 32;
   byte CMD_MISSION_STATE = 33;
   byte CMD_STATE_HOUSEWALL_DAMAGE = 34;
   byte CMD_PROJECTILE = 35;
   byte CMD_MOVE_CREATURE = 36;
   byte CMD_REMOVE_EFFECT = 37;
   byte CMD_NEW_ACHIEVEMENT = 38;
   byte CMD_SEND_NEW_KINGODM = 39;
   byte CMD_SEND_ALL_KINGODM = 40;
   byte CMD_ERROR = 41;
   byte CMD_SET_TRADE_AGREE = 42;
   byte CMD_MOVE_INVENTORY = 43;
   byte CMD_RENAME_ITEM = 44;
   byte CMD_MESSAGE_MULTICOLORED = 45;
   byte CMD_WEATHER_UPDATE = 46;
   byte CMD_RENAME = 47;
   byte CMD_REMOVE_STRUCTURE = 48;
   byte CMD_ADD_WALL = 49;
   byte CMD_FOCUS_LEVEL_CHANGED = 50;
   byte CMD_TELEPORT = 51;
   byte CMD_NEW_FACE = 52;
   byte CMD_HASTARGET = 53;
   byte CMD_REMOVE_WALL = 54;
   byte CMD_REMOVE_HORSE_ITEM = 55;
   byte CMD_DRUMROLL_STARTED = 56;
   byte CMD_TAB_CLOSED = 57;
   byte CMD_OPEN_INVENTORY_CONTAINER = 58;
   byte CMD_AVAILABLE_SERVER = 59;
   byte CMD_MOUNTSPEED = 60;
   byte CMD_STATUS_HUNGER = 61;
   byte CMD_TOGGLE_SWITCH = 62;
   byte CMD_SET_VEHICLE_CONTROLLER = 63;
   byte CMD_ADD_EFFECT = 64;
   byte CMD_DEAD = 65;
   byte CMD_UPDATE_SKILL = 66;
   byte CMD_ROTATE = 67;
   byte CMD_UPDATE_INVENTORY = 68;
   byte CMD_RECEIVED = 69;
   byte CMD_TILESTRIP_NEAR = 70;
   byte CMD_STOP_USE_ITEM = 71;
   byte CMD_MOVE_CREATURE_AND_SET_Z = 72;
   byte CMD_TILESTRIP = 73;
   byte CMD_RESIZE = 74;
   byte CMD_DEATH_ANIMATION_AND_CORPSE = 75;
   byte CMD_ADD_TO_INVENTORY = 76;
   byte CMD_REMOVE_FLOOR = 77;
   byte CMD_ITEM_MODELNAME = 78;
   byte CMD_CLIMB = 79;
   byte CMD_WOUND = 80;
   byte CMD_WOUND_ACTION_ADD = 81;
   byte CMD_ADD_FLOOR = 82;
   byte CMD_OPEN_FENCE = 83;
   byte CMD_WOUND_ACTION_REMOVE = 84;
   byte CMD_SET_STANCE = 85;
   byte CMD_PLAYSOUND = 86;
   byte CMD_TIMELEFT = 87;
   byte CMD_ANIMATION_WITH_TARGET = 88;
   byte CMD_UPDATE_FRIENDLIST = 89;
   byte CMD_STATUS_STAMINA = 90;
   byte CMD_TRADE_CHANGED = 91;
   byte CMD_REPAINT = 92;
   byte CMD_MESSAGE_TYPED = 93;
   byte CMD_SETGROUNDOFFSET = 94;
   byte CMD_ADVANCED_EFFECT = 95;
   byte CMD_BUILD_MARK = 96;
   byte CMD_ACTION = 97;
   byte CMD_FIGHT_MOVE_OPTIONS = 98;
   byte CMD_MESSAGE = 99;
   byte CMD_ACHIEVEMENT_LIST = 100;
   byte CMD_SET_EQUIPMENT = 101;
   byte CMD_TILESTRIP_CAVE = 102;
   byte CMD_TILESTRIP_FAR = 103;
   byte CMD_TEAM_INVITE = 104;
   byte CMD_STATUS_THIRST = 105;
   byte CMD_BML_FORM = 106;
   byte CMD_SERVER_TIME = 107;
   byte CMD_ADD_CREATURE = 108;
   byte CMD_ATTACH_EFFECT = 109;
   byte CMD_USE_ITEM = 110;
   byte CMD_ATTACH_CREATURE = 111;
   byte CMD_ADD_STRUCTURE = 112;
   byte REMOVE_MISSION_STATE = 113;
   byte CMD_PART_GROUP = 114;
   byte CMD_PLAYMUSIC = 115;
   byte CMD_OPEN_INVENTORY_WINDOW = 116;
   byte CMD_WINDIMPACT = 117;
   byte CMD_LOGIN_FAILURE = 118;
   byte CMD_OPEN_TRADE_WINDOW = 119;
   byte CMD_CLOSE_INVENTORY_WINDOW = 120;
   byte CMD_CLOSE_TRADE_WINDOW = 121;
   byte CMD_OPEN_WALL = 122;
   byte CMD_TEAM_MUTE = 123;
   byte CMD_SET_SKILL = 124;
   byte CMD_SET_PASSABLE = 125;
   byte CMD_REQUEST_ACTIONS = 126;
   byte CMD_CLOSE_WALL = 127;
   byte CMD_GET_EIGC_NAME = -1;
   byte CMD_GET_GAME_NAME = -2;
   byte CMD_EIGC_LOGIN = -3;
   byte CMD_ADD_TILE_EFFECT = -4;
   byte CMD_REMOVE_TILE_EFFECT = -5;
   byte CMD_VERIFY_CLIENT_VERSION = -6;
   byte CMD_UPDATE_INVENTORY_ATTRIBUTE = -7;
   byte CMD_SET_EIGC_SERVICE_STATE = -8;
   byte CMD_ADD_ITEM = -9;
   byte CMD_REMOVE_FROM_INVENTORY = -10;
   byte CMD_SHOW_HTML = -11;
   byte CMD_ACTION_STRING = -12;
   byte CMD_JOIN_GROUP = -13;
   byte CMD_FIGHT_STATUS = -14;
   byte CMD_LOGIN = -15;
   byte CMD_EMPTY = -16;
   byte CMD_SPECIALMOVE = -17;
   byte CMD_STATUS_STRING = -18;
   byte CMD_SET_WATER = -19;
   byte CMD_SERVERPORTAL = -20;
   byte CMD_SET_BRIDGE = -21;
   byte CMD_TAB_SELECTED = -22;
   byte CMD_REQUEST_SELECT = -23;
   byte SEND_SELECT_LIST = 0;
   byte UPDATE_SELECTION = 1;
   byte KEEP_SELECTION = 2;
   byte CMD_SET_FLYING = -24;
   byte CMD_BRIDGE = -25;
   byte CMD_PERMISSIONS = -26;
   byte CMD_TARGETSTATUS = -27;
   byte CMD_STARTMOVING = -28;
   byte CMD_MOVE_CREATURE_MOD = -29;
   byte CMD_TOGGLE_CLIENT_FEATURE = -30;
   byte CMD_ADD_HORSE_ITEM = -31;
   byte CMD_EQUIP_ITEM = -32;
   byte CMD_UPDATE_PLAYER_TITLE = -33;
   byte CMD_TICKET_ADD = -34;
   byte CMD_CREATION_WINDOW = -35;
   byte CMD_TICKET_REMOVE = -36;
   byte CMD_UPDATE_PLAYER_KINGDOM = -37;
   byte CMD_MOVE = -38;
   byte CMD_PERSONAL_GOAL_LIST = -39;
   byte CMD_UPDATE_PERSONAL_GOAL = -40;
   byte CMD_SHOW_PERSONAL_GOALS = -41;
   byte CREATION_WINDOW_OPEN = 1;
   byte CREATION_WINDOW_CLOSE = 2;
   byte CREATION_WINDOW_UPDATE = 3;
   byte CMD_SEND_WINDOW_TYPE_DATA = -42;
   byte CMD_MAP_ANNOTATIONS = -43;
   byte CMD_OPEN_WINDOW_TYPE = -44;
   byte CMD_SEND_MAP_INFO = -45;
   byte CMD_ITEM_CREATION_LIST = -46;
   byte CMD_STATUS_EFFECT_BAR = -47;
   byte CMD_CHANGE_MODELNAME = -48;
   byte CMD_SEND_PLONK = -49;
   byte CMD_SEND_VALREI_MAP_INFO = -50;
   byte CMD_SHOW_DEED_PLAN = -51;
   byte CMD_STEAM_AUTHENTICATION = -52;
   byte CMD_UPDATE_DECORATIONS = -53;
   byte CMD_UPDATE_CREATURE_RARITY = -54;
   byte CMD_COOKBOOK = -55;
   byte CMD_WAYSTONE = -56;
   byte CMD_SHOW_LINKS = -57;
   byte CMD_ADD_MINEDOOR = -58;
   byte CMD_REMOVE_MINEDOOR = -59;
   byte CMD_OPEN_MINEDOOR = -60;
   byte CMD_CLOSE_MINEDOOR = -61;
   byte CMD_VALREIFIGHT = -62;
   byte CMD_PLACE_ITEM = -63;
   byte CMD_FISH = -64;
   byte CMD_CLEAR_WINDOW = -65;
   byte CMD_OPENCLOSE_WINDOW = -66;
   byte ADD_EFFECT_TO_BAR = 0;
   byte REMOVE_EFFECT_FROM_BAR = 1;
   byte OPEN_WINDOW = 0;
   byte CLOSE_WINDOW = 1;
   byte DISABLE_QUICKBUTTON = 3;
   byte ENABLE_QUICKBUTTON = 4;
   byte DISABLE_QUICKBUTTON_ALL = 5;
   byte ENABLE_QUICKBUTTON_ALL = 6;
   byte REQUEST_PARTIAL_LIST = 0;
   byte ADD_TO_CREATION_WINDOW = 1;
   byte REMOVE_CREATION_GROUND_ITEM = 2;
   byte FULL_CREATIONS_LIST = 3;
   byte FULL_CATEGORY_LIST = 4;
   byte ACTION_RESULT = 5;
   byte UPDATE_GROUND_ITEM = 6;
   byte ADD_TILE_BORDER = 7;
   byte ADD_FENCE_TO_WINDOW = 8;
   byte FINISHED_STRUCTURE_ACTION_IN_CREATION_WINDOW = 9;
   byte ADD_WALL_TO_CREATION_WINDOW = 10;
   byte ADD_FLOOR_OR_ROOF_TO_CREATION_WINDOW = 11;
   byte ADD_BRIDGEPART_TO_CREATION_WINDOW = 12;
   byte ADD_ANNOTATION = 0;
   byte REMOVE_ANNOTATION = 1;
   byte REQUEST_MAP_PERMISSIONS = 2;
   byte CLEAR_ANNOTATION = 3;
   byte VALREI_DIETY = 0;
   byte VALREI_ITEM = 1;
   byte VALREI_DEMIGOD = 2;
   byte VALREI_ALLY = 3;
   byte TIME_UPDATE = 4;
   byte MANAGE_RECRUITMENT_WINDOW = 0;
   byte LOOK_FOR_VILLAGE_WINDOW = 1;
   byte ITEM_NEW_DATA = 1;
   byte SHOW_PLAN = 1;
   byte HIDE_PLAN = 2;
   byte ADD_PART = 3;
   byte REMOVE_PART = 4;
   int SET_FLOOR_OVERRIDE = -9999;
   byte PERMISSIONS_SHOW = 0;
   byte PERMISSIONS_ADDED_MANUALLY = 1;
   byte PERMISSIONS_APPLY_CHANGES = 2;
   byte PERMISSIONS_BACK = 3;
   byte PERMISSIONS_HIDE = 4;
   byte DEED_PLAN_SHOW = 0;
   byte DEED_PLAN_EXPORT = 1;
   byte DECORATION_ADD = 0;
   byte DECORATION_REMOVE = 1;
   byte COOKBOOK_RECIPE_LIST = 0;
   byte COOKBOOK_RECIPE = 1;
   byte COOKBOOK_FAVOURITE = 2;
   byte COOKBOOK_NOTES = 3;
   byte COOKBOOK_MARK = 4;
   byte COOKBOOK_REMOVE = 5;
   byte LINKS_SHOW = 0;
   byte LINKS_HIDE = 1;
   byte LINKS_PROTECTION = 2;
   byte FIGHTS_LIST = 0;
   byte FIGHTS_DETAIL = 1;
   byte PLACEABLE_NOTHING = 0;
   byte PLACEABLE_PARENT = 1;
   byte PLACEABLE_INSIDE = 2;
   byte FISH_START = 0;
   byte FISH_BITE = 1;
   byte FISH_MOVED_ON = 2;
   byte FISH_HOOKED = 3;
   byte FISH_MISSED = 4;
   byte FISH_NO_FISH = 5;
   byte FISH_LINE_SNAPPED = 6;
   byte FISH_ROD_BROKE = 7;
   byte FISH_TIME_OUT = 8;
   byte FISH_CASTED = 9;
   byte FISH_CANCEL = 10;
   byte FISH_STRIKE = 11;
   byte FISH_CAUGHT = 12;
   byte FISH_GOT_AWAY = 13;
   byte FISH_SWAM_AWAY = 14;
   byte FISH_STOP = 15;
   byte FISH_MOVE = 16;
   byte FISH_PULL = 17;
   byte FISH_PAUSE = 18;
   byte FISH_MOVING_ON = 19;
   byte SPEAR_START = 20;
   byte SPEAR_MOVE = 21;
   byte SPEAR_HIT = 22;
   byte SPEAR_MISSED = 23;
   byte SPEAR_NO_FISH = 24;
   byte SPEAR_TIME_OUT = 25;
   byte SPEAR_STRIKE = 26;
   byte SPEAR_CANCEL = 27;
   byte SPEAR_SWAM_AWAY = 28;
   byte SPEAR_STOP = 29;
   byte NET_START = 40;
   byte SHOW_FISH_SPOTS = 45;
   byte PERSONAL_GOALS_OLD = 0;
   byte PERSONAL_GOALS_J_TIERADD = 1;
   byte PERSONAL_GOALS_J_TIERUPDATE = 2;
   byte PERSONAL_GOALS_J_ACHVUPDATE = 3;
   byte PERSONAL_GOALS_J_TUTADD = 4;
   byte PERSONAL_GOALS_J_TUTOPEN = 5;
   byte ITEM_NEW_PARENT = 2;
   byte ITEM_NEW_CUSTOM_NAME = 3;
   byte ITEM_NEW_COLOR = 4;
   byte ITEM_NEW_TYPE = 5;
   byte ITEM_NEW_PRICE = 6;
   byte ITEM_NEW_TEMPERATURE = 7;
   byte ITEM_NEW_INSCRIPTION = 8;
   byte ITEM_TEMPERATURE_FROZEN = -1;
   byte ITEM_TEMPERATURE_NEUTRAL = 0;
   byte ITEM_TEMPERATURE_WARM = 1;
   byte ITEM_TEMPERATURE_HOT = 2;
   byte ITEM_TEMPERATURE_BOILING = 3;
   byte ITEM_TEMPERATURE_SEARING = 4;
   byte ITEM_TEMPERATURE_GLOWING = 5;
   byte TELE_STOP_COMMANDING = 0;
   byte TELE_START_COMMAND_BOAT = 1;
   byte TELE_START_COMMAND_CART = 2;
   byte TELE_START_COMMAND_CREATURE = 3;
   byte PROJECTILE_NEW_TYPE = -1;
   byte PROJECTILE_ARROW = 1;
   byte PROJECTILE_CATAPULT = 2;
   byte PROJECTILE_LAVA_BOULDER = 3;
   byte PROJECTILE_SPELL_SHARD_OF_ICE = 4;
   byte PROJECTILE_PEW_ICE = 5;
   byte PROJECTILE_PEW_ACID = 6;
   byte PROJECTILE_PEW_FIRE = 7;
   byte PROJECTILE_PEW_LIGHTNING = 8;
   byte PROJECTILE_BALLISTA = 9;
   int MAGIC_OBJECT_ON_GROUND = -3000;
   String MAGIC_CLIENT_CHECK = "PERFORM CLIENT VERSION TEST HMD";
   long PLAYER_INVENTORY_ID = -1L;
   long PLAYER_EQUIPMENT_ID = -2L;
   long TRADE_OFFER_PASSIVE = 1L;
   long TRADE_OFFER_ACTIVE = 2L;
   long TRADE_SELECTED_FROM_PASSIVE = 3L;
   long TRADE_SELECTED_FROM_ACTIVE = 4L;
   byte EIGC_SERVICE_NONE = 0;
   byte EIGC_SERVICE_PROXIMITY = 1;
   byte EIGC_SERVICE_TEAM = 2;
   byte EIGC_SERVICE_LECTURE = 4;
   byte EIGC_SERVICE_P2P = 8;
   byte EIGC_SERVICE_HIFI = 16;
   byte PROTOCOL_KINGDOM_UNKNOWN = -1;
   byte PROTOCOL_KINGDOM_NONE = 0;
   byte PROTOCOL_KINGDOM_JENN = 1;
   byte PROTOCOL_KINGDOM_MOLREHAN = 2;
   byte PROTOCOL_KINGDOM_HOTS = 3;
   byte PROTOCOL_KINGDOM_FREEDOM = 4;
   byte LOGIN_NORMAL = 0;
   byte LOGIN_DEV = 1;
   byte LOGIN_FLIGHT = 2;
   byte CAT_NONE = 0;
   byte CAT_ACCOUNT = 1;
   byte CAT_BOAT = 2;
   byte CAT_BUG = 3;
   byte CAT_FORUM = 4;
   byte CAT_GRIEF = 5;
   byte CAT_HORSE = 6;
   byte CAT_PASSWORD = 7;
   byte CAT_PAYMENT = 8;
   byte CAT_STUCK = 9;
   byte CAT_OTHER = 10;
   byte CAT_WATCH = 11;
   byte MUTE_NONE = 0;
   byte MUTE_WARN = 1;
   byte MUTE_MUTE = 2;
   long ALLOW_ALLIES = -20L;
   long ALLOW_CITIZENS = -30L;
   long ALLOW_KINGDOM = -40L;
   long ALLOW_EVERYONE = -50L;
   long ALLOW_ROLE_PERMISSION = -60L;
   byte M_NONE = 0;
   byte M_SYSTEM = 1;
   byte M_INFO = 2;
   byte M_FAIL = 3;
   byte M_HOSTILE = 4;
   float ITEM_PLACING_DISTANCE = 4.0F;

   public static enum InfectionSeverity {
      verylight((byte)1),
      light((byte)2),
      medium((byte)3),
      bad((byte)4),
      severe((byte)5);

      private byte infectionSeverity;

      private InfectionSeverity(byte protocolValue) {
         this.infectionSeverity = protocolValue;
      }

      public byte getProtocolValue() {
         return this.infectionSeverity;
      }
   }

   public static enum WoundSeverity {
      verylight((byte)1),
      light((byte)1),
      medium((byte)3),
      bad((byte)4),
      severe((byte)5);

      private byte woundSeverity;

      private WoundSeverity(byte protocolValue) {
         this.woundSeverity = protocolValue;
      }

      public byte getProtocolValue() {
         return this.woundSeverity;
      }
   }

   public static enum WoundType {
      crush((byte)0),
      slash((byte)1),
      pierce((byte)2),
      bite((byte)3),
      burn((byte)4),
      poison((byte)5),
      infection((byte)6),
      water((byte)7),
      cold((byte)8),
      internal((byte)9),
      acid((byte)10);

      private byte woundType = 0;

      private WoundType(byte protocolValue) {
         this.woundType = protocolValue;
      }

      public byte getProtocolValue() {
         return this.woundType;
      }
   }
}
