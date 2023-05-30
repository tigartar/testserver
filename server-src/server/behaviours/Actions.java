package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.villages.Village;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;

public final class Actions implements MiscConstants, ActionTypes {
   private static List<ActionEntry> defaultItemActions = new LinkedList<>();
   private static List<ActionEntry> defaultCreatureActions = new LinkedList<>();
   private static List<ActionEntry> defaultTileActions = new LinkedList<>();
   private static List<ActionEntry> defaultStructureActions = new LinkedList<>();
   private static List<ActionEntry> defaultWoundActions = new LinkedList<>();
   private static List<ActionEntry> defaultTileBorderActions = new LinkedList<>();
   private static List<ActionEntry> defaultPlanetActions = new LinkedList<>();
   public static final int combatRange = 6;
   public static final int combatSpellRange = 50;
   public static final int combatAreaSpellRange = 24;
   public static final int spellRangeMedium = 12;
   public static final short NOACTION = 0;
   public static final short EXAMINE = 1;
   public static final short EQUIPMENT = 2;
   public static final short OPEN = 3;
   public static final short CLOSE = 4;
   public static final short WAVE = 5;
   public static final short TAKE = 6;
   public static final short DROP = 7;
   public static final short CUT = 8;
   public static final short DISENGAGE = 9;
   public static final short ASSIST = 10;
   public static final short RIDE = 11;
   public static final short LIGHT = 12;
   public static final short BUY = 13;
   public static final short EMPTY = 14;
   public static final short CALL = 15;
   public static final short REMOVE = 16;
   public static final short READ = 17;
   public static final short KNOCK = 18;
   public static final short TASTE = 19;
   public static final short QUIT = 20;
   public static final short TELL = 21;
   public static final short QUAFF = 22;
   public static final short DISMOUNT = 23;
   public static final short ABANDON = 24;
   public static final short UNCOVER = 25;
   public static final short COVER = 26;
   public static final short UNSADDLE = 27;
   public static final short LOCK = 28;
   public static final short SADDLE = 29;
   public static final short WAKE = 30;
   public static final short SELL = 31;
   public static final short WEAR = 32;
   public static final short WIZKILL = 33;
   public static final short FINDPATH = 34;
   public static final short CRIMINALMODE = 35;
   public static final short FAITHMODE = 36;
   public static final short DROPDIRT = 37;
   public static final short CLIMB = 38;
   public static final short STOPCLIMB = 39;
   public static final short ORDERPET_MOVE = 40;
   public static final short ORDERPET_CLEAR = 41;
   public static final short ORDERPET_ATTACK = 42;
   public static final short PET_TRANSFER = 43;
   public static final short PET_STAYONLINE = 44;
   public static final short PET_GOOFFLINE = 45;
   public static final short TAME = 46;
   public static final short GIVE = 47;
   public static final short FOLLOW = 48;
   public static final short GROUP = 49;
   public static final short UNGROUP = 50;
   public static final short POUR = 51;
   public static final short SIP = 52;
   public static final short SNUFF = 53;
   public static final short CRUSH = 54;
   public static final short PICKSEED = 55;
   public static final short BUILD_PLAN = 56;
   public static final short BUILD_PLAN_REMOVE = 57;
   public static final short BUILD_PLAN_FINALIZE = 58;
   public static final short RENAME = 59;
   public static final short ADDPERSON = 60;
   public static final short REMOVEPERSON = 61;
   public static final short MANAGE = 62;
   public static final short TRADE = 63;
   public static final short GET_COORDINATES = 64;
   public static final short FOUND_VILLAGE = 65;
   public static final short MANAGE_VILLAGE_CITIZENS = 66;
   public static final short MANAGE_VILLAGE_GUARDS = 67;
   public static final short MANAGE_VILLAGE_SETTINGS = 68;
   public static final short MANAGE_VILLAGE_REPUTATIONS = 69;
   public static final short MANAGE_VILLAGE_GATES = 70;
   public static final short VILLAGE_HISTORY = 71;
   public static final short AREA_HISTORY = 72;
   public static final short VILLAGE_INVITE = 73;
   public static final short DRAG = 74;
   public static final short STOPDRAG = 75;
   public static final short RESIZE_VILLAGE = 76;
   public static final short VILLAGE_INFO = 77;
   public static final short REPLACE = 78;
   public static final short NOSPOON = 79;
   public static final short MANAGE_VILLAGE_ALLIANCES = 80;
   public static final short ALLIANCE_INVITE = 81;
   public static final short DESTROY_STRUCTURE = 82;
   public static final short DESTROY_ITEM = 83;
   public static final short SPAMMODE = 84;
   public static final short MANAGE_TRADERS = 85;
   public static final short SET_PRICE = 86;
   public static final short GET_PRICE = 87;
   public static final short SETDATA = 88;
   public static final short SETKINGDOM = 89;
   public static final short POLL = 90;
   public static final short ASK_REFRESH = 91;
   public static final short LEARNSKILL = 92;
   public static final short COMBINE = 93;
   public static final short TELEPORTSET = 94;
   public static final short TELEPORT = 95;
   public static final short CHOP = 96;
   public static final short CHOP_UP = 97;
   public static final short PROTECT = 98;
   public static final short PUSH = 99;
   public static final short STEAL = 100;
   public static final short LOCKPICK = 101;
   public static final short UNLOCK = 102;
   public static final short TAUNT = 103;
   public static final short LEARN = 104;
   public static final short SHIELDBASH = 105;
   public static final short LEAD = 106;
   public static final short STOPLEADING = 107;
   public static final short BACKSTAB = 108;
   public static final short TRACK = 109;
   public static final short RECITE = 110;
   public static final short TEACH = 111;
   public static final short REST = 112;
   public static final short KICK = 113;
   public static final short ATTACK = 114;
   public static final short LISTEN = 115;
   public static final short BUILD = 116;
   public static final short BURN = 117;
   public static final short USE = 118;
   public static final short BURY = 119;
   public static final short BUTCHER = 120;
   public static final short MEND = 121;
   public static final short CAST = 122;
   public static final short SCALP = 123;
   public static final short SHOOT = 124;
   public static final short QUICK_SHOT = 125;
   public static final short SHOOT_HEAD = 126;
   public static final short SHOOT_FACE = 127;
   public static final short SHOOT_TORSO = 128;
   public static final short SHOOT_LEFTARM = 129;
   public static final short SHOOT_RIGHTARM = 130;
   public static final short SHOOT_LEGS = 131;
   public static final short STRING = 132;
   public static final short BOW_UNSTRING = 133;
   public static final short TARGET_PRACTICE = 134;
   public static final short HIDE = 135;
   public static final short STEALTH = 136;
   public static final short PICK = 137;
   public static final short HANG = 138;
   public static final short SIT = 139;
   public static final short SLEEP = 140;
   public static final short PRAY = 141;
   public static final short SACRIFICE = 142;
   public static final short DESECRATE = 143;
   public static final short DIG = 144;
   public static final short MINE = 145;
   public static final short MINEUPWARDS = 146;
   public static final short MINEDOWNWARDS = 147;
   public static final short CREATE = 148;
   public static final short STOP = 149;
   public static final short FLATTEN = 150;
   public static final short FARM = 151;
   public static final short HARVEST = 152;
   public static final short SOW = 153;
   public static final short ROAD_PACK = 154;
   public static final short ROAD_PAVE = 155;
   public static final short PROSPECT = 156;
   public static final short FIGHT_AGGRESSIVE = 157;
   public static final short FIGHT_NORMAL = 158;
   public static final short FIGHT_DEFEND = 159;
   public static final short FISH = 160;
   public static final short SET_LOCK = 161;
   public static final short REPAIR = 162;
   public static final short BUILD_STONEWALL = 163;
   public static final short BUILD_STONEWALL_HIGH = 164;
   public static final short BUILD_PALISADE = 165;
   public static final short BUILD_WOODEN_FENCE = 166;
   public static final short BUILD_PALISADE_GATE = 167;
   public static final short BUILD_WOODEN_FENCE_GATE = 168;
   public static final short CONTINUE_BUILDING = 169;
   public static final short CONTINUE_BUILDING_FENCE = 170;
   public static final short PLAN_FENCE_DESTROY = 171;
   public static final short FENCE_DESTROY = 172;
   public static final short FENCE_DISASSEMBLE = 173;
   public static final short WALL_DESTROY = 174;
   public static final short WALL_DISASSEMBLE = 175;
   public static final short PLANT_SIGN = 176;
   public static final short TURN_ITEM = 177;
   public static final short TURN_ITEM_BACK = 178;
   public static final short SUMMON = 179;
   public static final short DESTROY = 180;
   public static final short PULL = 181;
   public static final short EAT = 182;
   public static final short DRINK = 183;
   public static final short SHUTDOWN = 184;
   public static final short GETINFO = 185;
   public static final short PLANT = 186;
   public static final short PICKSPROUT = 187;
   public static final short GROW = 188;
   public static final short FILL = 189;
   public static final short SPIN = 190;
   public static final short DESTROY_PAVE = 191;
   public static final short IMPROVE = 192;
   public static final short REPAIR_STRUCT = 193;
   public static final short PAYMENT_MANAGEMENT = 194;
   public static final short POWER_MANAGEMENT = 195;
   public static final short FIRSTAID = 196;
   public static final short SPECMOVE1 = 197;
   public static final short SPECMOVE2 = 198;
   public static final short SPECMOVE3 = 199;
   public static final short SPECMOVE4 = 200;
   public static final short SPECMOVE5 = 201;
   public static final short SPECMOVE6 = 202;
   public static final short SPECMOVE7 = 203;
   public static final short SPECMOVE8 = 204;
   public static final short SPECMOVE9 = 205;
   public static final short SPECMOVE10 = 206;
   public static final short SPECMOVE11 = 207;
   public static final short SPECMOVE12 = 208;
   public static final short DECLARE_WAR = 209;
   public static final short OFFER_PEACE = 210;
   public static final short PRACTICE = 211;
   public static final short FAITH_MANAGEMENT = 212;
   public static final short ASK_CONVERT = 213;
   public static final short ASK_GIFT = 214;
   public static final short CONVERT = 215;
   public static final short PREACH = 216;
   public static final short READ_INSCRIPTION_1 = 217;
   public static final short READ_INSCRIPTION_2 = 218;
   public static final short READ_INSCRIPTION_3 = 219;
   public static final short SET_REALDEATH = 220;
   public static final short DESTROY_ALTAR = 221;
   public static final short MANAGE_BANK = 222;
   public static final short FORAGE = 223;
   public static final short BOTANIZE = 224;
   public static final short FILLET = 225;
   public static final short WITHDRAW_MONEY = 226;
   public static final short TUNNEL = 227;
   public static final short WORK = 228;
   public static final short REINFORCE = 229;
   public static final short FEED = 230;
   public static final short COLOR = 231;
   public static final short COLOR_REMOVE = 232;
   public static final short LOAD = 233;
   public static final short UNLOAD = 234;
   public static final short UNWIND = 235;
   public static final short FIRE = 236;
   public static final short WINCH = 237;
   public static final short WINCH5 = 238;
   public static final short WINCH10 = 239;
   public static final short TRANSFER_NORTH = 240;
   public static final short TRANSFER_EAST = 241;
   public static final short TRANSFER_SOUTH = 242;
   public static final short TRANSFER_WEST = 243;
   public static final short MANAGE_SERVERS = 244;
   public static final short SPELL_BLESS = 245;
   public static final short SPELL_CURELIGHT = 246;
   public static final short SPELL_CUREMEDIUM = 247;
   public static final short SPELL_CURESERIOUS = 248;
   public static final short SPELL_HEAL = 249;
   public static final short SPELL_REFRESH = 250;
   public static final short SPELL_MEND = 251;
   public static final short SPELL_SMITE = 252;
   public static final short SPELL_SUNDERITEM = 253;
   public static final short SPELL_DRAIN_STAMINA = 254;
   public static final short SPELL_DRAIN_HEALTH = 255;
   public static final short SPELL_EARLY_HARVEST = 256;
   public static final short SPELL_DRAIN_SKILL = 257;
   public static final short SPELL_BREAK_ALTAR = 258;
   public static final short SPELL_ENCHANT_TOXIN = 259;
   public static final short SPELL_ENCHANT_BLAZE = 260;
   public static final short SPELL_ENCHANT_GLACIAL = 261;
   public static final short SPELL_ENCHANT_CORROSION = 262;
   public static final short SPELL_ENCHANT_PROT_ACID = 263;
   public static final short SPELL_ENCHANT_PROT_FROST = 264;
   public static final short SPELL_ENCHANT_PROT_FIRE = 265;
   public static final short SPELL_ENCHANT_PROT_POISON = 266;
   public static final short SPELL_ENCHANT_DEMISE_HUMAN = 267;
   public static final short SPELL_ENCHANT_DEMISE_MONSTER = 268;
   public static final short SPELL_ENCHANT_DEMISE_ANIMAL = 269;
   public static final short SPELL_ENCHANT_DEMISE_LEGENDARY = 270;
   public static final short SPELL_LOCATE_ARTIFACT = 271;
   public static final short SPELL_VESSEL = 272;
   public static final short SPELL_REBIRTH = 273;
   public static final short SPELL_DOMINATE = 274;
   public static final short SPELL_CHARM = 275;
   public static final short SPELL_ITEMBUFF_SKILLGAIN = 276;
   public static final short SPELL_ITEMBUFF_WEAPONDAM = 277;
   public static final short SPELL_ITEMBUFF_PAINSHARE = 278;
   public static final short SPELL_ITEMBUFF_QUICKACTIONS = 279;
   public static final short SPELL_ITEMBUFF_GOODFOOD = 280;
   public static final short SPELL_ITEMBUFF_DAMAGE_BRITTLE = 281;
   public static final short SPELL_CREATUREBUFF_PROT_DAMAGE = 282;
   public static final short MIX = 283;
   public static final short TREAT = 284;
   public static final short MIX_INFO = 285;
   public static final short BECOME_PRIEST = 286;
   public static final short ATTACK_UPPER_LEFT_HARD = 287;
   public static final short ATTACK_UPPER_LEFT_NORMAL = 288;
   public static final short ATTACK_UPPER_LEFT_QUICK = 289;
   public static final short ATTACK_MID_LEFT_HARD = 290;
   public static final short ATTACK_MID_LEFT_NORMAL = 291;
   public static final short ATTACK_MID_LEFT_QUICK = 292;
   public static final short ATTACK_LOW_LEFT_HARD = 293;
   public static final short ATTACK_LOW_LEFT_NORMAL = 294;
   public static final short ATTACK_LOW_LEFT_QUICK = 295;
   public static final short ATTACK_LOW_HARD = 296;
   public static final short ATTACK_LOW_NORMAL = 297;
   public static final short ATTACK_LOW_QUICK = 298;
   public static final short ATTACK_HIGH_HARD = 299;
   public static final short ATTACK_HIGH_NORMAL = 300;
   public static final short ATTACK_HIGH_QUICK = 301;
   public static final short ATTACK_CENTER_HARD = 302;
   public static final short ATTACK_CENTER_NORMAL = 303;
   public static final short ATTACK_CENTER_QUICK = 304;
   public static final short ATTACK_UPPER_RIGHT_HARD = 305;
   public static final short ATTACK_UPPER_RIGHT_NORMAL = 306;
   public static final short ATTACK_UPPER_RIGHT_QUICK = 307;
   public static final short ATTACK_MID_RIGHT_HARD = 308;
   public static final short ATTACK_MID_RIGHT_NORMAL = 309;
   public static final short ATTACK_MID_RIGHT_QUICK = 310;
   public static final short ATTACK_LOW_RIGHT_HARD = 311;
   public static final short ATTACK_LOW_RIGHT_NORMAL = 312;
   public static final short ATTACK_LOW_RIGHT_QUICK = 313;
   public static final short DEFEND_HIGH = 314;
   public static final short DEFEND_LEFT = 315;
   public static final short DEFEND_LOW = 316;
   public static final short DEFEND_RIGHT = 317;
   public static final short CULTIVATE = 318;
   public static final short RENT_1_COPPER = 319;
   public static final short RENT_10_COPPER = 320;
   public static final short RENT_1_SILVER = 321;
   public static final short RENT_10_SILVER = 322;
   public static final short RESET_RENT = 323;
   public static final short HIRE = 324;
   public static final short ASKSLEEP = 325;
   public static final short TARGET = 326;
   public static final short FREEZE = 327;
   public static final short SUCK = 328;
   public static final short WATCH = 329;
   public static final short HATCH = 330;
   public static final short EMBARK_DRIVER = 331;
   public static final short EMBARK_PASSENGER = 332;
   public static final short DISEMBARK = 333;
   public static final short CHECK_REIMBURSEMENTS = 334;
   public static final short ASK_CHANGETILE = 335;
   public static final short MAIL_CHECK = 336;
   public static final short MAIL_SEND = 337;
   public static final short SPELL_ENCHANT_COURIER = 338;
   public static final short SPELL_ENCHANT_MESSENGER = 339;
   public static final short FIGHT_FOCUS = 340;
   public static final short CLEARTARGET = 341;
   public static final short THROW = 342;
   public static final short DUEL = 343;
   public static final short SPAR = 344;
   public static final short MILK = 345;
   public static final short HEALFAST = 346;
   public static final short HEAL_ABSORB = 347;
   public static final short DISBAND_VILLAGE = 348;
   public static final short STOP_DISBAND_VILLAGE = 349;
   public static final short DRAIN_COFFERS = 350;
   public static final short ASK_TUTORIAL = 351;
   public static final short SET_LOGGING = 352;
   public static final short ASPIRE_KING = 353;
   public static final short APPOINT = 354;
   public static final short KINGDOM_STATUS = 355;
   public static final short KINGDOM_HISTORY = 356;
   public static final short ANNOUNCE = 357;
   public static final short ABDICATE = 358;
   public static final short MANAGE_VEHICLE = 359;
   public static final short MOOR = 360;
   public static final short RAISEANCHOR = 361;
   public static final short DREDGE = 362;
   public static final short BUILD_MINEDOOR = 363;
   public static final short MANAGE_MINEDOOR = 364;
   public static final short RENT_10_IRON = 365;
   public static final short RENT_25_IRON = 366;
   public static final short RENT_50_IRON = 367;
   public static final short SETNP = 368;
   public static final short SCULPT = 369;
   public static final short RECHARGE = 370;
   public static final short ECONOMIC_INFO = 371;
   public static final short MANAGE_PERIMETER = 372;
   public static final short PRUNE = 373;
   public static final short TRAP = 374;
   public static final short DISARM = 375;
   public static final short SPELL_SIXTH_SENSE = 376;
   public static final short HITCH = 377;
   public static final short UNHITCH = 378;
   public static final short BREED = 379;
   public static final short WISH = 380;
   public static final short PROT_TILE = 381;
   public static final short DEPROT_TILE = 382;
   public static final short COUNT_ITEMS = 383;
   public static final short MEDITATE = 384;
   public static final short LEAVE_PATH = 385;
   public static final short PATH_LEADERS = 386;
   public static final short LIGHT_PATH = 387;
   public static final short ENCHANT = 388;
   public static final short LOVE_EFFECT = 389;
   public static final short DOUBLE_ATTACK_DAM = 390;
   public static final short DOUBLE_STRUCT_DAM = 391;
   public static final short FEAR_EFFECT = 392;
   public static final short NO_ELEMENTAL_DAM = 393;
   public static final short IGNORE_TRAPS = 394;
   public static final short CLEAN_WOUND = 395;
   public static final short FILLUP = 396;
   public static final short PUPPETEER = 397;
   public static final short GROOM = 398;
   public static final short MAGICLINK = 399;
   public static final short SPELL_HOLYCROP = 400;
   public static final short SPELL_RITUAL_SUN = 401;
   public static final short SPELL_RITE_DEATH = 402;
   public static final short SPELL_RITE_SPRING = 403;
   public static final short SPELL_OAKSHELL = 404;
   public static final short SPELL_WILLOWSPINE = 405;
   public static final short SPELL_BEARPAW = 406;
   public static final short SPELL_HUMIDDRIZZLE = 407;
   public static final short SPELL_GENESIS = 408;
   public static final short SPELL_ITEMBUFF_LIFETRANSFER = 409;
   public static final short SPELL_FORESTGIANT = 410;
   public static final short SPELL_FROGLEAP = 411;
   public static final short SPELL_ITEMBUFF_VENOM = 412;
   public static final short SPELL_TORNADO = 413;
   public static final short SPELL_ICEPILLAR = 414;
   public static final short SPELL_ITEMBUFF_MINDSTEALER = 415;
   public static final short SPELL_ITEMBUFF_NIMBLE = 416;
   public static final short SPELL_ITEMBUFF_FROSTBRAND = 417;
   public static final short SPELL_TENTACLESDEEP = 418;
   public static final short SPELL_LOCATE_PLAYER = 419;
   public static final short SPELL_FIREPILLAR = 420;
   public static final short SPELL_LIGHTTOKEN = 421;
   public static final short SPELL_GOATSHAPE = 422;
   public static final short SPELL_FRANTICCHARGE = 423;
   public static final short SPELL_FIREHEART = 424;
   public static final short SPELL_MASS_STAMINA = 425;
   public static final short SPELL_PHANTASMS = 426;
   public static final short SPELL_HELLSTRENGTH = 427;
   public static final short SPELL_ROTTINGGUT = 428;
   public static final short SPELL_WEAKNESS = 429;
   public static final short SPELL_WORMBRAINS = 430;
   public static final short SPELL_ZOMBIEINFESTATION = 431;
   public static final short SPELL_PAINRAIN = 432;
   public static final short SPELL_FUNGUSTRAP = 433;
   public static final short SPELL_OUTOFMIND = 434;
   public static final short SPELL_LANDOFDEAD = 435;
   public static final short SPELL_WILDGROWTH = 436;
   public static final short SPELL_WARD = 437;
   public static final short SPELL_LIGHTOFFO = 438;
   public static final short SPELL_MOLESENSES = 439;
   public static final short SPELL_STRONGWALL = 440;
   public static final short SPELL_WRATH_OF_MAGRANON = 441;
   public static final short SPELL_EXCEL = 442;
   public static final short SPELL_REVEAL_SETTLEMENT = 443;
   public static final short SPELL_REVEAL_CREATURES = 444;
   public static final short SPELL_WISDOM_VYNORA = 445;
   public static final short SPELL_CORRUPT = 446;
   public static final short SPELL_TRUEHIT = 447;
   public static final short SPELL_SCORN_LIBILA = 448;
   public static final short SPELL_DISINTEGRATE = 449;
   public static final short SPELL_DISPEL = 450;
   public static final short SPELL_NOLOCATE = 451;
   public static final short SELECT_SPELL = 452;
   public static final short SPELL_DIRT = 453;
   public static final short SPELL_ITEMBUFF_BLOODTHIRST = 454;
   public static final short SPELL_ITEMBUFF_SLOWDOWN = 455;
   public static final short SPELL_ITEMBUFF_BLESSINGDARK = 456;
   public static final short SPELL_ITEMBUFF_LOCATEFISH = 457;
   public static final short SPELL_ITEMBUFF_LOCATECHAMP = 458;
   public static final short SPELL_ITEMBUFF_LOCATEENEMY = 459;
   public static final short WRAP = 460;
   public static final short TOGGLE_JOAT = 461;
   public static final short TRANSMUTATE = 462;
   public static final short ADD = 463;
   public static final short REMOVE_TEN = 464;
   public static final short REMOVE_FIFTY = 465;
   public static final short REFUND = 466;
   public static final short GM_INTERFACE = 467;
   public static final short CHOP_OLD_TREE = 468;
   public static final short TEAM_INVITE = 469;
   public static final short TEAM_KICK = 470;
   public static final short TEAM_MANAGE = 471;
   public static final short MISSION_MANAGE = 472;
   public static final short TALK = 473;
   public static final short SHOW = 474;
   public static final short STEP_ON = 475;
   public static final short GETLOGEVENTS = 476;
   public static final short BUILD_IRON_FENCE = 477;
   public static final short BUILD_WOVEN_FENCE = 478;
   public static final short BUILD_IRON_FENCE_GATE = 479;
   public static final short FOUND_KINGDOM = 480;
   public static final short CONFIGURE_TWITTER = 481;
   public static final short READ_INSCRIPTION_4 = 482;
   public static final short TEST_TERRAIN = 483;
   public static final short BRAND = 484;
   public static final short SPELL_SHARD_ICE = 485;
   public static final short TESTCASE = 486;
   public static final short VOTE = 487;
   public static final short CHALLENGE = 488;
   public static final short RECALL = 489;
   public static final short FINAL_BREATH = 490;
   public static final short ON_DEATH = 491;
   public static final short ON_CUT_DOWN = 492;
   public static final short SET_PROTECTED = 493;
   public static final short VOICE_CHAT = 494;
   public static final short LAST_GASP = 495;
   public static final short RITUAL_FAITH = 496;
   public static final short RITUAL_RAIN = 497;
   public static final short RITUAL_FOG = 498;
   public static final short RITUAL_SUN = 499;
   public static final short RITUAL_HEALING = 500;
   public static final short RITUAL_DEATH = 501;
   public static final short RITUAL_PLAGUE = 502;
   public static final short CREATE_ZONE = 503;
   public static final short CONQUER = 504;
   public static final short INSCRIBE = 505;
   public static final short INSCRIPTION_READ = 506;
   public static final short BUILD_PLAN_ROOF = 507;
   public static final short BUILD_PLAN_FLOOR_ABOVE = 508;
   public static final short BUILD_PLAN_FLOOR_BELOW = 509;
   public static final short MANAGE_ACHIEVEMENT = 510;
   public static final short KARMA_QUESTION = 511;
   public static final short MAGIC_WALL = 512;
   public static final short SUMMON_GUARDS = 513;
   public static final short BUILD_PLAN_FLOOR_ABOVE_WITH_DOOR = 514;
   public static final short BUILD_PLAN_FLOOR_ABOVE_WITH_OPENING = 515;
   public static final short BUILD_WOODEN_FENCE_PARAPET = 516;
   public static final short BUILD_STONE_FENCE_PARAPET = 517;
   public static final short RAISE_GROUND = 518;
   public static final short SMELT = 519;
   public static final short BUILD_WOODEN_FENCE_CRUDE = 520;
   public static final short BUILD_STONE_IRON_FENCE_PARAPET = 521;
   public static final short CLIMB_UP = 522;
   public static final short CLIMB_DOWN = 523;
   public static final short FLOOR_DESTROY = 524;
   public static final short ROOF_DESTROY = 525;
   public static final short BUILD_WOODEN_FENCE_GARDESGARD_LOW = 526;
   public static final short BUILD_WOODEN_FENCE_GARDESGARD_HIGH = 527;
   public static final short BUILD_WOODEN_FENCE_CRUDE_GATE = 528;
   public static final short BUILD_WOODEN_FENCE_GARDESGARD_GATE = 529;
   public static final short BUILD_EXPAND_TILE = 530;
   public static final short BUILD_REMOVE_TILE = 531;
   public static final short LEVEL = 532;
   public static final short FLATTEN_BORDER = 533;
   public static final short GM_TOOL = 534;
   public static final short FEATURE_MANAGEMENT = 535;
   public static final short ANALYSE = 536;
   public static final short THREATEN = 537;
   public static final short GM_SET_TRAITS = 538;
   public static final short GM_SET_ENCHANTS = 539;
   public static final short MANAGE_VILLAGE_ROLES = 540;
   public static final short BUILD_STONE_FENCE = 541;
   public static final short BUILD_STONE_CURB = 542;
   public static final short BUILD_ROPE_FENCE_LOW = 543;
   public static final short BUILD_ROPE_FENCE_HIGH = 544;
   public static final short BUILD_HIGH_IRON_FENCE = 545;
   public static final short BUILD_HIGH_IRON_FENCE_GATE = 546;
   public static final short SPELL_KARMA_DISEASE = 547;
   public static final short SPELL_KARMA_RUSTMONSTER = 548;
   public static final short SPELL_KARMA_FIREBALL = 549;
   public static final short SPELL_KARMA_BOLT = 550;
   public static final short SPELL_KARMA_MISSILE = 551;
   public static final short SPELL_KARMA_CONTINUUM = 552;
   public static final short SPELL_KARMA_STONESKIN = 553;
   public static final short SPELL_KARMA_SLOW = 554;
   public static final short SPELL_KARMA_TRUESTRIKE = 555;
   public static final short SPELL_KARMA_ICEWALL = 556;
   public static final short SPELL_KARMA_FIREWALL = 557;
   public static final short SPELL_KARMA_STONEWALL = 558;
   public static final short SPELL_KARMA_SUMMON = 559;
   public static final short SPELL_KARMA_FORECAST = 560;
   public static final short SPELL_KARMA_WEATHER = 561;
   public static final short SPELL_KARMA_MIRRORS = 562;
   public static final short PLANT_FLOWERBED = 563;
   public static final short PLANT_FLOWER = 564;
   public static final short WATER_PLANT = 565;
   public static final short MANAGE_PLAYER_PROFILE = 566;
   public static final short ADD_INVENTORY_GROUP = 567;
   public static final short OPEN_INVENTORY_CONTAINER = 568;
   public static final short FORAGE_VEG = 569;
   public static final short FORAGE_RESOURCE = 570;
   public static final short FORAGE_BERRIES = 571;
   public static final short BOTANIZE_SEEDS = 572;
   public static final short BOTANIZE_HERBS = 573;
   public static final short BOTANIZE_PLANTS = 574;
   public static final short BOTANIZE_RESOURCE = 575;
   public static final short ROAD_PAVE_CORNER = 576;
   public static final short SET_INVISIBLE = 577;
   public static final short SET_VISIBLE = 578;
   public static final short SET_INVULNERABLE = 579;
   public static final short SET_VULNERABLE = 580;
   public static final short DECAY = 581;
   public static final short EQUIP = 582;
   public static final short EQUIP_LEFT = 583;
   public static final short EQUIP_RIGHT = 584;
   public static final short UNEQUIP = 585;
   public static final short REMOVE_INVENTORY_GROUP = 586;
   public static final short TICKET_VIEW = 587;
   public static final short TICKET_CANCEL = 588;
   public static final short TICKET_RESPOND = 589;
   public static final short TICKET_RESOLVED = 590;
   public static final short TICKET_FORWARD_GM = 591;
   public static final short TICKET_FORWARD_ARCH = 592;
   public static final short TICKET_FORWARD_DEV = 593;
   public static final short TICKET_ONHOLD = 594;
   public static final short TICKET_TAKE = 595;
   public static final short TICKET_FORWARD_CM = 596;
   public static final short TICKET_FEEDBACK = 597;
   public static final short MANAGE_RECRUITMENT_AD = 598;
   public static final short TICKET_REOPEN = 599;
   public static final short DISCARD = 600;
   public static final short LOOK_FOR_VILLAGE = 601;
   public static final short DELETE_RECRUITMENT_AD = 602;
   public static final short EDIT_RECRUITMENT_AD = 603;
   public static final short PAINT_TERRAIN = 604;
   public static final short LOAD_CARGO = 605;
   public static final short UNLOAD_CARGO = 606;
   public static final short ADD_TO_CREATION_WINDOW = 607;
   public static final short SWITCH_SKILL = 608;
   public static final short INGAME_VOTING_SETUP = 609;
   public static final short COMMUNE = 610;
   public static final short BUILD_MEDIUM_CHAIN_FENCE = 611;
   public static final short BUILD_WOOD_WALL = 612;
   public static final short BUILD_WOOD_WINDOW = 613;
   public static final short BUILD_WOOD_DOOR = 614;
   public static final short BUILD_WOOD_DOUBLE_DOOR = 615;
   public static final short BUILD_WOOD_ARCHED_WALL = 616;
   public static final short BUILD_STONE_WALL = 617;
   public static final short BUILD_STONE_WINDOW = 618;
   public static final short BUILD_STONE_DOOR = 619;
   public static final short BUILD_STONE_DOUBLE_DOOR = 620;
   public static final short BUILD_STONE_ARCHED_WALL = 621;
   public static final short BUILD_TIMBER_FRAMED_WALL = 622;
   public static final short BUILD_TIMBER_FRAMED_WINDOW = 623;
   public static final short BUILD_TIMBER_FRAMED_DOOR = 624;
   public static final short BUILD_TIMBER_FRAMED_DOUBLE_DOOR = 625;
   public static final short BUILD_TIMBER_FRAMED_ARCHED_WALL = 626;
   public static final short ROAD_PAVE_ROUGH = 627;
   public static final short ROAD_PAVE_ROUND = 628;
   public static final short SPELL_KARMA_SUMMON_WORG = 629;
   public static final short SPELL_KARMA_SUMMON_WRAITH = 630;
   public static final short SPELL_KARMA_SUMMON_SKELETONS = 631;
   public static final short TRANSFER_RARITY = 632;
   public static final short IMBUE = 633;
   public static final short SPELL_KARMA_SPROUT_TREES = 634;
   public static final short GROUP_CA_HELP_SETUP = 635;
   public static final short HOLD = 636;
   public static final short BUILD_PLAN_BRIDGE = 637;
   public static final short DROP_AS_PILE = 638;
   public static final short DRAIN = 639;
   public static final short SURVEY = 640;
   public static final short SPELL_TANGLEWEAVE = 641;
   public static final short RUMMAGE = 642;
   public static final short UNBRAND = 643;
   public static final short TRIM = 644;
   public static final short GATHER = 645;
   public static final short SHEAR = 646;
   public static final short MODIFY_WALL = 647;
   public static final short BUILD_PLAIN_STONE_WALL = 648;
   public static final short BUILD_PLAIN_STONE_WINDOW = 649;
   public static final short BUILD_PLAIN_NARROW_STONE_WINDOW = 650;
   public static final short BUILD_PLAIN_STONE_DOOR = 651;
   public static final short BUILD_PLAIN_STONE_DOUBLE_DOOR = 652;
   public static final short BUILD_PLAIN_STONE_ARCHED_WALL = 653;
   public static final short BUILD_FENCE_PORTCULLIS = 654;
   public static final short BUILD_PLAIN_STONE_PORTCULLIS = 655;
   public static final short BUILD_PLAIN_STONE_BARRED_WALL = 656;
   public static final short BUILD_STONE_PORTCULLIS = 657;
   public static final short BUILD_WOOD_PORTCULLIS = 658;
   public static final short BUILD_PLAN_STAIRCASE_WITH_OPENING_ABOVE = 659;
   public static final short PLANT_CENTER = 660;
   public static final short MANAGE_FRIENDS = 661;
   public static final short INSIDEOUT = 662;
   public static final short MANAGE_ANIMAL = 663;
   public static final short MANAGE_BUILDING = 664;
   public static final short MANAGE_CART = 665;
   public static final short MANAGE_DOOR = 666;
   public static final short MANAGE_GATE = 667;
   public static final short MANAGE_SHIP = 668;
   public static final short MANAGE_WAGON = 669;
   public static final short MANAGE_UPKEEP = 670;
   public static final short HAUL_UP = 671;
   public static final short HAUL_DOWN = 672;
   public static final short MANAGE_ALL_DOORS = 673;
   public static final short TAG_ITEM = 674;
   public static final short SUMMON_TAGGED_ITEM = 675;
   public static final short BUILD_TIMBER_FRAMED_BALCONY = 676;
   public static final short BUILD_TIMBER_FRAMED_JETTY = 677;
   public static final short BUILD_DECORATED_STONE_ORIEL = 678;
   public static final short BUILD_WOODEN_CANOPY = 679;
   public static final short BUILD_WOODEN_WIDE_WINDOW = 680;
   public static final short BUILD_PLAIN_STONE_ORIEL = 681;
   public static final short USE_PENDULUM = 682;
   public static final short ROTATE_WALL = 683;
   public static final short MANAGE_ITEM_RESTRICTIONS = 684;
   public static final short PICKUP_PLANTED = 685;
   public static final short SPELL_KARMA_INCINERATE = 686;
   public static final short MANAGE_SMALL_CART = 687;
   public static final short MANAGE_ITEM = 688;
   public static final short SHOW_VILLAGE_PLAN = 689;
   public static final short SEARCH_PERMISSIONS = 690;
   public static final short SHOW_HISTORY_FOR_OBJECT = 691;
   public static final short SHOW_HISTORY_FOR_DOOR = 692;
   public static final short SKIP_TUTORIAL = 693;
   public static final short ROAD_PAVE_CORNER_ROUGH = 694;
   public static final short ROAD_PAVE_CORNER_ROUND = 695;
   public static final short PUSH_GENTLY = 696;
   public static final short PULL_GENTLY = 697;
   public static final short TOGGLE_CA = 698;
   public static final short TOGGLE_CM = 699;
   public static final short GET_LCM_INFO = 700;
   public static final short SIT_ANY = 701;
   public static final short SIT_LEFT = 702;
   public static final short SIT_RIGHT = 703;
   public static final short BUILD_PLAN_WIDE_STAIRCASE = 704;
   public static final short BUILD_PLAN_RIGHT_STAIRCASE = 705;
   public static final short BUILD_PLAN_LEFT_STAIRCASE = 706;
   public static final short BURY_ALL = 707;
   public static final short STAND_UP = 708;
   public static final short BUILD_PLAN_CLOCKWISE_STAIRCASE = 709;
   public static final short BUILD_PLAN_ANTICLOCKWISE_STAIRCASE = 710;
   public static final short BUILD_PLAN_CLOCKWISE_STAIRCASE_WITH_BANISTER = 711;
   public static final short BUILD_PLAN_ANTICLOCKWISE_STAIRCASE_WITH_BANISTER = 712;
   public static final short BUILD_PLAN_WIDE_STAIRCASE_RIGHT_BANISTER = 713;
   public static final short BUILD_PLAN_WIDE_STAIRCASE_LEFT_BANISTER = 714;
   public static final short BUILD_PLAN_WIDE_STAIRCASE_BOTH_BANISTER = 715;
   public static final short TARGET_HOSTILE = 716;
   public static final short PLOT_COURSE = 717;
   public static final short DEFAULT_TERRAFORMING = 718;
   public static final short KINGDOM_MEMBER_LIST = 719;
   public static final short BOTANIZE_SPICES = 720;
   public static final short GM_SET_MEDPATH = 721;
   public static final short CHANGE_PATH = 722;
   public static final short EQUIP_TIMED = 723;
   public static final short EQUIP_TIMED_AUTO = 724;
   public static final short SET_VOLUME_1 = 725;
   public static final short SET_VOLUME_2 = 726;
   public static final short SET_VOLUME_5 = 727;
   public static final short SET_VOLUME_10 = 728;
   public static final short SET_VOLUME_20 = 729;
   public static final short SET_VOLUME_50 = 730;
   public static final short SET_VOLUME_100 = 731;
   public static final short SET_VOLUME_200 = 732;
   public static final short SET_VOLUME_500 = 733;
   public static final short SET_VOLUME_1000 = 734;
   public static final short SET_VOLUME_2000 = 735;
   public static final short SET_VOLUME_5000 = 736;
   public static final short SET_VOLUME_10000 = 737;
   public static final short COOKBOOK = 738;
   public static final short SEAL = 739;
   public static final short UNSEAL = 740;
   public static final short COLLECT = 741;
   public static final short WRITE_RECIPE = 742;
   public static final short READ_RECIPE = 743;
   public static final short ADD_RECIPE = 744;
   public static final short THROW_PLAYFUL = 745;
   public static final short PLANT_LEFT = 746;
   public static final short PLANT_RIGHT = 747;
   public static final short VIEW_LINKS = 748;
   public static final short SET_LINK_NORTH = 749;
   public static final short SET_LINK_NORTH_EAST = 750;
   public static final short SET_LINK_EAST = 751;
   public static final short SET_LINK_SOUTH_EAST = 752;
   public static final short SET_LINK_SOUTH = 753;
   public static final short SET_LINK_SOUTH_WEST = 754;
   public static final short SET_LINK_WEST = 755;
   public static final short SET_LINK_NORTH_WEST = 756;
   public static final short PRY = 757;
   public static final short FIND_ROUTE = 758;
   public static final short VIEW_PROTECTION = 759;
   public static final short BUILD_WOOD_ARCHED_LEFT = 760;
   public static final short BUILD_WOOD_ARCHED_RIGHT = 761;
   public static final short BUILD_WOOD_ARCHED_T = 762;
   public static final short BUILD_STONE_ARCHED_LEFT = 763;
   public static final short BUILD_STONE_ARCHED_RIGHT = 764;
   public static final short BUILD_STONE_ARCHED_T = 765;
   public static final short BUILD_TIMBER_FRAMED_ARCHED_LEFT = 766;
   public static final short BUILD_TIMBER_FRAMED_ARCHED_RIGHT = 767;
   public static final short BUILD_TIMBER_FRAMED_ARCHED_T = 768;
   public static final short BUILD_PLAIN_STONE_ARCHED_LEFT = 769;
   public static final short BUILD_PLAIN_STONE_ARCHED_RIGHT = 770;
   public static final short BUILD_PLAIN_STONE_ARCHED_T = 771;
   public static final short BUILD_SLATE_WALL = 772;
   public static final short BUILD_SLATE_WINDOW = 773;
   public static final short BUILD_SLATE_NARROW_WINDOW = 774;
   public static final short BUILD_SLATE_DOOR = 775;
   public static final short BUILD_SLATE_DOUBLE_DOOR = 776;
   public static final short BUILD_SLATE_ARCHED_WALL = 777;
   public static final short BUILD_SLATE_PORTCULLIS = 778;
   public static final short BUILD_SLATE_BARRED_WALL = 779;
   public static final short BUILD_SLATE_ORIEL = 780;
   public static final short BUILD_SLATE_ARCHED_LEFT = 781;
   public static final short BUILD_SLATE_ARCHED_RIGHT = 782;
   public static final short BUILD_SLATE_ARCHED_T = 783;
   public static final short BUILD_ROUNDED_STONE_WALL = 784;
   public static final short BUILD_ROUNDED_STONE_WINDOW = 785;
   public static final short BUILD_ROUNDED_STONE_NARROW_WINDOW = 786;
   public static final short BUILD_ROUNDED_STONE_DOOR = 787;
   public static final short BUILD_ROUNDED_STONE_DOUBLE_DOOR = 788;
   public static final short BUILD_ROUNDED_STONE_ARCHED_WALL = 789;
   public static final short BUILD_ROUNDED_STONE_PORTCULLIS = 790;
   public static final short BUILD_ROUNDED_STONE_BARRED_WALL = 791;
   public static final short BUILD_ROUNDED_STONE_ORIEL = 792;
   public static final short BUILD_ROUNDED_STONE_ARCHED_LEFT = 793;
   public static final short BUILD_ROUNDED_STONE_ARCHED_RIGHT = 794;
   public static final short BUILD_ROUNDED_STONE_ARCHED_T = 795;
   public static final short BUILD_POTTERY_WALL = 796;
   public static final short BUILD_POTTERY_WINDOW = 797;
   public static final short BUILD_POTTERY_NARROW_WINDOW = 798;
   public static final short BUILD_POTTERY_DOOR = 799;
   public static final short BUILD_POTTERY_DOUBLE_DOOR = 800;
   public static final short BUILD_POTTERY_ARCHED_WALL = 801;
   public static final short BUILD_POTTERY_PORTCULLIS = 802;
   public static final short BUILD_POTTERY_BARRED_WALL = 803;
   public static final short BUILD_POTTERY_ORIEL = 804;
   public static final short BUILD_POTTERY_ARCHED_LEFT = 805;
   public static final short BUILD_POTTERY_ARCHED_RIGHT = 806;
   public static final short BUILD_POTTERY_ARCHED_T = 807;
   public static final short BUILD_SANDSTONE_WALL = 808;
   public static final short BUILD_SANDSTONE_WINDOW = 809;
   public static final short BUILD_SANDSTONE_NARROW_WINDOW = 810;
   public static final short BUILD_SANDSTONE_DOOR = 811;
   public static final short BUILD_SANDSTONE_DOUBLE_DOOR = 812;
   public static final short BUILD_SANDSTONE_ARCHED_WALL = 813;
   public static final short BUILD_SANDSTONE_PORTCULLIS = 814;
   public static final short BUILD_SANDSTONE_BARRED_WALL = 815;
   public static final short BUILD_SANDSTONE_ORIEL = 816;
   public static final short BUILD_SANDSTONE_ARCHED_LEFT = 817;
   public static final short BUILD_SANDSTONE_ARCHED_RIGHT = 818;
   public static final short BUILD_SANDSTONE_ARCHED_T = 819;
   public static final short BUILD_MARBLE_WALL = 820;
   public static final short BUILD_MARBLE_WINDOW = 821;
   public static final short BUILD_MARBLE_NARROW_WINDOW = 822;
   public static final short BUILD_MARBLE_DOOR = 823;
   public static final short BUILD_MARBLE_DOUBLE_DOOR = 824;
   public static final short BUILD_MARBLE_ARCHED_WALL = 825;
   public static final short BUILD_MARBLE_PORTCULLIS = 826;
   public static final short BUILD_MARBLE_BARRED_WALL = 827;
   public static final short BUILD_MARBLE_ORIEL = 828;
   public static final short BUILD_MARBLE_ARCHED_LEFT = 829;
   public static final short BUILD_MARBLE_ARCHED_RIGHT = 830;
   public static final short BUILD_MARBLE_ARCHED_T = 831;
   public static final short BUILD_SLATE_FENCE = 832;
   public static final short BUILD_SLATE_IRON_FENCE = 833;
   public static final short BUILD_SLATE_IRON_FENCE_GATE = 834;
   public static final short BUILD_ROUNDED_STONE_FENCE = 835;
   public static final short BUILD_ROUNDED_STONE_IRON_FENCE = 836;
   public static final short BUILD_ROUNDED_STONE_IRON_FENCE_GATE = 837;
   public static final short BUILD_POTTERY_FENCE = 838;
   public static final short BUILD_POTTERY_IRON_FENCE = 839;
   public static final short BUILD_POTTERY_IRON_FENCE_GATE = 840;
   public static final short BUILD_SANDSTONE_FENCE = 841;
   public static final short BUILD_SANDSTONE_IRON_FENCE = 842;
   public static final short BUILD_SANDSTONE_IRON_FENCE_GATE = 843;
   public static final short BUILD_MARBLE_FENCE = 844;
   public static final short BUILD_MARBLE_IRON_FENCE = 845;
   public static final short BUILD_MARBLE_IRON_FENCE_GATE = 846;
   public static final short TOGGLE_RENDER = 847;
   public static final short TOGGLE_HALF_ARCH = 848;
   public static final short INCREASE_ANGLE = 849;
   public static final short REDUCE_ANGLE = 850;
   public static final short RAM = 851;
   public static final short STUDY = 852;
   public static final short RECORD = 853;
   public static final short ADD_FOLDER = 854;
   public static final short REMOVE_FOLDER = 855;
   public static final short REINFORCE_STONE = 856;
   public static final short REINFORCE_SLATE = 857;
   public static final short REINFORCE_POTTERY = 858;
   public static final short REINFORCE_ROUNDED_STONE = 859;
   public static final short REINFORCE_SANDSTONE = 860;
   public static final short REINFORCE_MARBLE = 861;
   public static final short REINFORCE_WOOD = 862;
   public static final short MANAGE_WAGONER = 863;
   public static final short MOVE_CENTER = 864;
   public static final short LEVEL_BORDER = 865;
   public static final short BUILD_ALL_WALL_PLANS = 866;
   public static final short BUILD_SCAFFOLDING_OPENING = 867;
   public static final short BUILD_SCAFFOLDING_FLOOR = 868;
   public static final short BUILD_SCAFFOLDING_SUPPORT = 869;
   public static final short BUILD_FENCE_SLATE_TALL_STONE_WALL = 870;
   public static final short BUILD_FENCE_SLATE_PORTCULLIS = 871;
   public static final short BUILD_FENCE_SLATE_HIGH_IRON_FENCE = 872;
   public static final short BUILD_FENCE_SLATE_HIGH_IRON_FENCE_GATE = 873;
   public static final short BUILD_FENCE_SLATE_STONE_PARAPET = 874;
   public static final short BUILD_FENCE_SLATE_CHAIN_FENCE = 875;
   public static final short BUILD_FENCE_ROUNDED_STONE_TALL_STONE_WALL = 876;
   public static final short BUILD_FENCE_ROUNDED_STONE_PORTCULLIS = 877;
   public static final short BUILD_FENCE_ROUNDED_STONE_HIGH_IRON_FENCE = 878;
   public static final short BUILD_FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE = 879;
   public static final short BUILD_FENCE_ROUNDED_STONE_STONE_PARAPET = 880;
   public static final short BUILD_FENCE_ROUNDED_STONE_CHAIN_FENCE = 881;
   public static final short BUILD_FENCE_SANDSTONE_TALL_STONE_WALL = 882;
   public static final short BUILD_FENCE_SANDSTONE_PORTCULLIS = 883;
   public static final short BUILD_FENCE_SANDSTONE_HIGH_IRON_FENCE = 884;
   public static final short BUILD_FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE = 885;
   public static final short BUILD_FENCE_SANDSTONE_STONE_PARAPET = 886;
   public static final short BUILD_FENCE_SANDSTONE_CHAIN_FENCE = 887;
   public static final short BUILD_FENCE_RENDERED_TALL_STONE_WALL = 888;
   public static final short BUILD_FENCE_RENDERED_PORTCULLIS = 889;
   public static final short BUILD_FENCE_RENDERED_HIGH_IRON_FENCE = 890;
   public static final short BUILD_FENCE_RENDERED_HIGH_IRON_FENCE_GATE = 891;
   public static final short BUILD_FENCE_RENDERED_STONE_PARAPET = 892;
   public static final short BUILD_FENCE_RENDERED_CHAIN_FENCE = 893;
   public static final short BUILD_FENCE_POTTERY_TALL_STONE_WALL = 894;
   public static final short BUILD_FENCE_POTTERY_PORTCULLIS = 895;
   public static final short BUILD_FENCE_POTTERY_HIGH_IRON_FENCE = 896;
   public static final short BUILD_FENCE_POTTERY_HIGH_IRON_FENCE_GATE = 897;
   public static final short BUILD_FENCE_POTTERY_STONE_PARAPET = 898;
   public static final short BUILD_FENCE_POTTERY_CHAIN_FENCE = 899;
   public static final short BUILD_FENCE_MARBLE_TALL_STONE_WALL = 900;
   public static final short BUILD_FENCE_MARBLE_PORTCULLIS = 901;
   public static final short BUILD_FENCE_MARBLE_HIGH_IRON_FENCE = 902;
   public static final short BUILD_FENCE_MARBLE_HIGH_IRON_FENCE_GATE = 903;
   public static final short BUILD_FENCE_MARBLE_STONE_PARAPET = 904;
   public static final short BUILD_FENCE_MARBLE_CHAIN_FENCE = 905;
   public static final short REMOVE_MINEDOOR = 906;
   public static final short LOAD_CREATURE = 907;
   public static final short UNLOAD_CREATURE = 908;
   public static final short GETSTOREDCREATUREINFO = 909;
   public static final short INVESTIGATE = 910;
   public static final short IDENTIFY_ITEM = 911;
   public static final short COMBINE_FRAGMENT = 912;
   public static final short ON_DESTROY = 913;
   public static final short TRANSFERBULKITEMS = 914;
   public static final short SETUP_DELIVERY = 915;
   public static final short MANAGE_DELIVERIES = 916;
   public static final short CANCEL_DELIVERY = 917;
   public static final short VIEW_DELIVERY = 918;
   public static final short WAGONER_HISTORY = 919;
   public static final short WAGONER_DISMISS = 920;
   public static final short DIG_TO_PILE = 921;
   public static final short REMOVE_ASH = 922;
   public static final short COLOR2 = 923;
   public static final short COLOR2_REMOVE = 924;
   public static final short PLACE_ITEM = 925;
   public static final short PLACE_ITEM_LARGE = 926;
   public static final short SWITCH_TILE_TYPE = 927;
   public static final short GM_CHANGE_AGE = 928;
   public static final short SPELL_FOCUSED_WILL = 929;
   public static final short SPELL_CLEANSE = 930;
   public static final short SPELL_INFERNO = 931;
   public static final short SPELL_HYPOTHERMIA = 932;
   public static final short SPELL_ITEMBUFF_ESSENCEDRAIN = 933;
   public static final short SPELL_SUMMON_SOUL = 934;
   public static final short SEARCH = 935;
   public static final short MAKE_BAIT = 936;
   public static final short MAKE_FLOAT = 937;
   public static final short CATCH_FLYS = 938;
   public static final short UPGRADE = 939;
   public static final short OPEN2 = 940;
   public static final short CLOSE2 = 941;
   public static final short PRY_OPEN = 942;
   public static final short ATTACH = 943;
   public static final short DETACH = 944;
   public static final short USE_RUNE = 945;
   public static final short SPELL_PURGE = 946;
   public static final ActionEntry[] actionEntrys = new ActionEntry[]{
      new ActionEntry((short)0, "None", "none", new int[]{0, 29}),
      new ActionEntry((short)1, "Examine", "examining", new int[]{0, 25, 29, 37}, false),
      new ActionEntry((short)2, "Equipment", "equipment", new int[]{0, 25, 29}),
      new ActionEntry((short)3, "Open", "opening", new int[]{0, 25, 43, 44}, 6, false),
      new ActionEntry((short)4, "Close", "closing", new int[]{0, 25, 43}, 6, false),
      new ActionEntry((short)5, "Wave", "waving", new int[]{0, 33}, false),
      new ActionEntry((short)6, "Take", "taking", new int[]{0, 5, 25, 43, 44, 48}, 6, false),
      new ActionEntry((short)7, "Drop", "dropping", new int[]{0, 25, 48}, false),
      new ActionEntry((short)8, "Cut", "cutting", new int[]{1}),
      new ActionEntry((short)9, "Disengage", "disengaging", new int[]{23, 29}),
      new ActionEntry((short)10, "Assist", "assisting", EMPTY_INT_ARRAY),
      new ActionEntry((short)11, "Ride", "riding", new int[]{0, 43, 44}, 8, false),
      new ActionEntry((short)12, "Light", "lighting", new int[]{25, 43}, false),
      new ActionEntry((short)13, "Buy", "buying", new int[]{0}),
      new ActionEntry((short)14, "Empty", "emptying", new int[]{0}),
      new ActionEntry((short)15, "Call", "calling", new int[]{0}),
      new ActionEntry((short)16, "Remove", "removing", new int[]{5, 48}),
      new ActionEntry((short)17, "Read", "reading", EMPTY_INT_ARRAY, false),
      new ActionEntry((short)18, "Knock", "knocking", new int[]{0}),
      new ActionEntry((short)19, "Taste", "tasting", new int[]{0, 25}, false),
      new ActionEntry((short)20, "Quit", "quitting", new int[]{0, 23}, false),
      new ActionEntry((short)21, "Tell", "telling", new int[]{0, 23}),
      new ActionEntry((short)22, "Quaff", "quaffing", new int[]{0}),
      new ActionEntry((short)23, "Dismount", "dismounting", new int[]{0, 23, 43}, false),
      new ActionEntry((short)24, "Abandon", "abandoning", new int[]{0, 23}),
      new ActionEntry((short)25, "Uncover", "uncovering", new int[]{0}),
      new ActionEntry((short)26, "Cover", "covering", new int[]{0}),
      new ActionEntry((short)27, "Unsaddle", "unsaddling", new int[]{0}),
      new ActionEntry((short)28, "Lock", "locking", new int[]{0, 25, 44}, false),
      new ActionEntry((short)29, "Saddle", "saddling", new int[]{0}),
      new ActionEntry((short)30, "Wake", "waking", new int[]{0}),
      new ActionEntry((short)31, "Sell", "selling", new int[]{25, 4, 44}),
      new ActionEntry((short)32, "Wear", "wearing", new int[]{0}),
      new ActionEntry((short)33, "Wizkill", "wizkilling", new int[]{0, 23}),
      new ActionEntry((short)34, "Find path", "pathfinding", new int[]{0, 23}),
      new ActionEntry((short)35, "Legal mode", "legal mode", new int[]{0, 23}, false),
      new ActionEntry((short)36, "Faith mode", "Faith mode", new int[]{0, 23}, false),
      new ActionEntry((short)37, "Drop dirt", "USE THIS ONE ONLY TO CHECK CRIMINALITY", new int[]{0, 48}),
      new ActionEntry((short)38, "Climb", "climb", new int[]{0, 23}, false),
      new ActionEntry((short)39, "Stop climbing", "climb", new int[]{0, 23}, false),
      new ActionEntry((short)40, "Go here", "ordering", new int[]{22, 33}, 80, false),
      new ActionEntry((short)41, "Clear orders", "ordering", new int[]{22, 23}, false),
      new ActionEntry((short)42, "Attack", "ordering", new int[]{5, 22, 33}, 80, false),
      new ActionEntry((short)43, "Give away pet", "giving", new int[]{22, 23}, 4, false),
      new ActionEntry((short)44, "Stay online", "ordering", new int[]{22, 23}, false),
      new ActionEntry((short)45, "Go offline", "ordering", new int[]{22, 23}, false),
      new ActionEntry((short)46, 8, "Tame", "taming", new int[]{5, 4, 25, 26, 43, 44, 48}, 4, true),
      new ActionEntry((short)47, "Give", "giving", new int[]{25, 33}, false),
      new ActionEntry((short)48, "Follow", "following", new int[]{22}, false),
      new ActionEntry((short)49, "Group", "grouping", new int[]{0, 23}, false),
      new ActionEntry((short)50, "Ungroup", "ungrouping", new int[]{0, 23}, false),
      new ActionEntry((short)51, "Pour", "pouring", new int[]{0, 23}, false),
      new ActionEntry((short)52, "Sip", "sipping", new int[]{0}, false),
      new ActionEntry((short)53, "Snuff", "snuffing", new int[]{0, 25, 43}, false),
      new ActionEntry((short)54, "Crush", "crushing", new int[]{0, 5}, false),
      new ActionEntry((short)55, "Pick seeds", "picking seeds", new int[]{0, 5, 25}, false),
      new ActionEntry((short)56, "Plan building", "planning building", new int[]{4, 25, 36, 45, 49}),
      new ActionEntry((short)57, "Remove plan here", "planning building", new int[]{4, 25, 36, 45, 49}),
      new ActionEntry((short)58, "Finalize build plan", "finalizing build plan", new int[]{4, 25, 36, 45, 49}),
      new ActionEntry((short)59, "Rename", "renaming", new int[]{0, 37, 25}, false),
      new ActionEntry((short)60, "Add", "manipulating list", new int[]{0, 25, 23}, 30),
      new ActionEntry((short)61, "Remove", "manipulating list", new int[]{0, 25, 23}, 30),
      new ActionEntry((short)62, "Manage", "manage", new int[]{0, 23}, false),
      new ActionEntry((short)63, "Trade", "trade", new int[]{0, 25, 33}, false),
      new ActionEntry((short)64, "Get coordinates", "getting coordinates", new int[]{0, 23}),
      new ActionEntry((short)65, "Found settlement", "founding settlement", new int[]{0, 23}),
      new ActionEntry((short)66, "Manage citizens", "managing settlement", new int[]{0, 23}),
      new ActionEntry((short)67, "Manage guards", "managing settlement", new int[]{0, 23}),
      new ActionEntry((short)68, "Manage settings", "managing settlement", new int[]{0, 23}),
      new ActionEntry((short)69, "Manage reputations", "managing reputations", new int[]{0, 23}),
      new ActionEntry((short)70, "Manage gates", "managing gates", new int[]{0, 23}),
      new ActionEntry((short)71, "Settlement history", "checking history", new int[]{0, 23}, false),
      new ActionEntry((short)72, "Area history", "checking history", new int[]{0, 23}, false),
      new ActionEntry((short)73, "Invite to join", "inviting to settlement", new int[]{0, 33}, false),
      new ActionEntry((short)74, "Drag", "dragging", new int[]{0, 25, 43}, false),
      new ActionEntry((short)75, "Stop dragging", "stops dragging", new int[]{0, 25, 29, 43}, false),
      new ActionEntry((short)76, "Resize", "resizing settlement", new int[]{0, 29}, false),
      new ActionEntry((short)77, "Info", "reading settlement information", new int[]{0, 25, 33}, false),
      new ActionEntry((short)78, "Replace", "replacing", new int[0], false),
      new ActionEntry((short)79, "No spoon", "nospooning", new int[]{0}, false),
      new ActionEntry((short)80, "Manage politics", "managing politics", new int[]{0}, false),
      new ActionEntry((short)81, "Form alliance", "forming alliances", new int[]{0, 33}, false),
      new ActionEntry((short)82, "Destroy structure", "destroying", new int[]{0, 33}, false),
      new ActionEntry((short)83, "Destroy", "destroying", new int[]{5, 4, 49}, true),
      new ActionEntry((short)84, "Spam mode", "setting spam mode", new int[]{0, 23}, false),
      new ActionEntry((short)85, "Manage traders", "managing traders", new int[]{47}, false),
      new ActionEntry((short)86, "Set price", "setting price", new int[]{0, 23}, false),
      new ActionEntry((short)87, "Get price", "getting price", new int[]{0}, false),
      new ActionEntry((short)88, "Set data", "setting data", new int[]{22, 23}),
      new ActionEntry((short)89, "Change kingdom", "changing kingdom", new int[]{22, 23}, false),
      new ActionEntry((short)90, "Poll", "polling", new int[]{22, 23}),
      new ActionEntry((short)91, "Ask for refreshment", "asking", new int[]{22, 33}, false),
      new ActionEntry((short)92, "Learn", "learning skill", new int[]{22, 23}),
      new ActionEntry((short)93, "Combine", "combining", new int[]{0, 25}),
      new ActionEntry((short)94, 8, "Set teleport marker", "setting teleport marker", new int[]{22, 23}),
      new ActionEntry((short)95, 8, "Teleport", "teleporting", new int[]{22, 23}),
      new ActionEntry((short)96, "Cut down", "cutting down", new int[]{1, 4, 9, 20, 18, 21, 25, 41, 43, 48}),
      new ActionEntry((short)97, "Chop up", "chopping", new int[]{1, 4, 9, 20, 18, 21, 25, 41, 43, 44, 48}),
      new ActionEntry((short)98, "Protect", "protecting", new int[]{22}, 2),
      new ActionEntry((short)99, "Push", "pushing", new int[]{37, 5, 44, 49}, 6, false),
      new ActionEntry((short)100, "Steal", "stealing", new int[]{5, 6, 8, 44}, false),
      new ActionEntry((short)101, "Pick lock", "picking lock", new int[]{5, 6, 25, 24, 44, 49}),
      new ActionEntry((short)102, "Unlock", "unlocking", new int[]{0, 25, 44}, false),
      new ActionEntry((short)103, 8, "Taunt", "taunting", new int[]{3, 22, 25, 44}, 6, false),
      new ActionEntry((short)104, "Learn", "learning", new int[]{22}),
      new ActionEntry((short)105, 8, "Shieldbash", "bashing", new int[]{3, 22, 24, 25, 44}, 6, false),
      new ActionEntry((short)106, "Lead", "leading", new int[]{36, 43, 44, 48}),
      new ActionEntry((short)107, 8, "Stop leading", "stop leading", new int[]{22, 23, 25, 37, 43}, false),
      new ActionEntry((short)108, "Backstab", "backstabbing", new int[]{3, 44}),
      new ActionEntry((short)109, "Track", "tracking", new int[]{4, 25, 43}, false),
      new ActionEntry((short)110, 8, "Recite", "reciting", new int[]{6}),
      new ActionEntry((short)111, "Teach", "teaching", new int[]{4, 33, 44}),
      new ActionEntry((short)112, "Rest", "resting", EMPTY_INT_ARRAY, false),
      new ActionEntry((short)113, "Kick", "kicking", new int[]{0}, false),
      new ActionEntry((short)114, 7, "Attack", "attacking", new int[]{3, 22, 25, 29, 48}, 6, false),
      new ActionEntry((short)115, "Listen", "listening", new int[]{6}),
      new ActionEntry((short)116, "Build", "building", new int[]{4, 45, 49}),
      new ActionEntry((short)117, "Burn", "burning", new int[]{25, 43}, 4, false),
      new ActionEntry((short)118, 8, "Use", "using", new int[]{43}, false),
      new ActionEntry((short)119, "Bury", "burying", new int[]{43, 44}),
      new ActionEntry((short)120, "Butcher", "butchering", new int[]{25, 43, 44, 48}),
      new ActionEntry((short)121, "Mend", "mending", new int[]{4, 6}),
      new ActionEntry((short)122, "Cast", "casting", EMPTY_INT_ARRAY),
      new ActionEntry((short)123, "Scalp", "scalping", new int[]{8, 44}),
      new ActionEntry((short)124, 8, "Shoot", "shooting", new int[]{3, 6, 24, 25, 43}, 200, true),
      new ActionEntry((short)125, 8, "Shoot quickly", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)126, 8, "Aim at head", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)127, 8, "Aim at face", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)128, 8, "Aim at torso", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)129, 8, "Aim at left arm", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)130, 8, "Aim at right arm", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)131, 8, "Aim at legs", "shooting", new int[]{3, 6, 24}, 200, true),
      new ActionEntry((short)132, "String", "stringing", new int[]{6}, 4),
      new ActionEntry((short)133, "Unstring", "unstringing", new int[]{6}, 4),
      new ActionEntry((short)134, "Shoot Arrow", "shooting", new int[]{6, 25, 43}, 200),
      new ActionEntry((short)135, "Hide", "hiding", EMPTY_INT_ARRAY),
      new ActionEntry((short)136, "Stealth", "sneaking", new int[]{6, 4, 23, 25}),
      new ActionEntry((short)137, "Pick", "picking", new int[]{43, 37}),
      new ActionEntry((short)138, "Hang", "hanging", new int[]{0, 25}),
      new ActionEntry((short)139, "Sit", "sitting", new int[]{0}),
      new ActionEntry((short)140, "Sleep", "sleeping", new int[]{6, 43}),
      new ActionEntry((short)141, "Pray", "praying", new int[]{4, 25, 43}, false),
      new ActionEntry((short)142, 8, "Sacrifice", "sacrificing", new int[]{25, 44, 26, 48}, false),
      new ActionEntry((short)143, "Desecrate", "desecrating", new int[]{4, 25, 43, 44}),
      new ActionEntry((short)144, "Dig", "digging", new int[]{1, 4, 25, 46, 9, 19, 20, 43, 48}),
      new ActionEntry((short)145, "Mine", "mining", new int[]{1, 4, 25, 9, 19, 18, 21, 41, 43, 5, 49, 50}),
      new ActionEntry((short)146, "Mine upwards", "mining up", new int[]{1, 4, 25, 9, 19, 18, 21, 41, 43, 5, 49, 50}),
      new ActionEntry((short)147, "Mine downwards", "mining down", new int[]{1, 4, 25, 9, 19, 18, 21, 41, 43, 5, 49, 50}),
      new ActionEntry((short)148, "Create", "creating", new int[]{4, 25, 6, 23}),
      new ActionEntry((short)149, "Stop", "stopping", new int[]{0, 23}, false),
      new ActionEntry((short)150, "Flatten", "flattening", new int[]{1, 4, 9, 25, 19, 52, 38, 40, 43, 49}),
      new ActionEntry((short)151, "Farm", "farming", new int[]{4, 25, 43, 48}),
      new ActionEntry((short)152, "Harvest", "harvesting", new int[]{4, 25, 43, 48}),
      new ActionEntry((short)153, "Sow", "sowing", new int[]{4, 25, 43, 48}),
      new ActionEntry((short)154, "Pack", "packing", new int[]{1, 4, 9, 25, 20, 43, 49}),
      new ActionEntry((short)155, "Pave", "paving", new int[]{1, 9, 25, 20, 43, 49}),
      new ActionEntry((short)156, "Prospect", "prospecting", new int[]{4, 9, 25, 19, 18, 21, 43, 50}),
      new ActionEntry((short)157, 8, "Fight aggressively", "attacking", new int[]{22, 23}),
      new ActionEntry((short)158, 8, "Fight normally", "attacking", new int[]{22, 23}),
      new ActionEntry((short)159, 8, "Defend", "defending", new int[]{22, 23}),
      new ActionEntry((short)160, "Fish", "fishing", new int[]{4, 5, 25, 33, 36, 43, 6}, 40),
      new ActionEntry((short)161, "Attach lock", "attaching lock", new int[]{4, 25, 49}),
      new ActionEntry((short)162, "Repair", "repairing", new int[]{4, 25, 6, 37, 43, 48}),
      new ActionEntry((short)163, "Build low wall", "planning stone wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)164, "Build tall wall", "planning high stone wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)165, "Build wooden palisade", "planning wooden palisade", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)166, "Build wooden fence", "planning wooden fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)167, "Build wooden palisade gate", "planning palisade gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)168, "Build wooden fence gate", "planning fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)169, "Continue building", "continuing", new int[]{4, 6, 43, 49}, 2),
      new ActionEntry((short)170, "Continue building", "continuing", "buildfence", new int[]{4, 43, 49}, 2),
      new ActionEntry((short)171, "Destroy fence plan", "destroying fence plan", new int[]{49, 6}),
      new ActionEntry((short)172, "Destroy fence", "destroying fence", new int[]{43, 49, 6}),
      new ActionEntry((short)173, "Disassemble fence", "disassembling fence", new int[]{49}),
      new ActionEntry((short)174, "Destroy wall", "destroying wall", new int[]{43, 49, 6}),
      new ActionEntry((short)175, "Disassemble wall", "disassembling wall", new int[]{49}),
      new ActionEntry((short)176, "Plant", "planting", new int[]{48}),
      new ActionEntry((short)177, "Turn item clockwise", "turning item", new int[]{37, 49}, 6, false),
      new ActionEntry((short)178, "Turn item counterclockwise", "turning item", new int[]{37, 49}, 6, false),
      new ActionEntry((short)179, "Summon", "summoning", new int[]{23}, 100),
      new ActionEntry((short)180, 10, "Destroy", "destroying", new int[]{23}, 100, true),
      new ActionEntry((short)181, "Pull", "pulling", new int[]{37, 5, 25, 44, 49}, 6, false),
      new ActionEntry((short)182, "Eat", "eating", new int[]{5, 37, 43}, false),
      new ActionEntry((short)183, "Drink", "drinking", new int[]{5, 25, 37, 43}, false),
      new ActionEntry((short)184, 10, "Shutdown Server", "shutting down", new int[]{22, 23}),
      new ActionEntry((short)185, 10, "Get info", "getting info", new int[]{23}, false),
      new ActionEntry((short)186, "Plant naturally", "planting", new int[]{4, 25, 36, 44, 48}),
      new ActionEntry((short)187, "Pick sprout", "picking", new int[]{4, 25, 36, 43, 48}),
      new ActionEntry((short)188, "Grow", "growing", new int[]{36}),
      new ActionEntry((short)189, 8, "Fill", "filling", new int[]{5, 25, 36, 43}, false),
      new ActionEntry((short)190, "Spin", "spinning", new int[]{4}),
      new ActionEntry((short)191, "Destroy pavement", "destroying pavement", new int[]{1, 4, 36, 43}),
      new ActionEntry((short)192, "Improve", "improving", new int[]{4, 9, 36, 43, 44, 48}),
      new ActionEntry((short)193, "Repair", "repairing", new int[]{4, 25, 6, 43, 48}),
      new ActionEntry((short)194, "Manage player payments", "managing payments", new int[]{23}),
      new ActionEntry((short)195, "Manage player powers", "managing powers", new int[]{23, 36}),
      new ActionEntry((short)196, "Firstaid", "aiding", new int[]{25, 36, 44}, 8),
      new ActionEntry((short)197, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)198, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)199, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)200, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)201, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)202, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)203, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)204, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)205, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)206, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)207, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)208, 8, "move", "aiming", new int[]{3, 22, 24, 37}, 6, false),
      new ActionEntry((short)209, "Declare war", "declaring", new int[]{0}, 8),
      new ActionEntry((short)210, "Offer peace", "offering", new int[]{0}, 8),
      new ActionEntry((short)211, "Practice", "practicing", new int[]{4, 43}, 1),
      new ActionEntry((short)212, "Set faith", "managing faith", new int[]{22, 23, 36}),
      new ActionEntry((short)213, "Convert", "converting", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)214, "Check for gift", "asking", new int[]{0, 37}, 4),
      new ActionEntry((short)215, "Convert", "converting", new int[]{4}, 4),
      new ActionEntry((short)216, "Preach", "preaching", new int[]{4, 25}, 4),
      new ActionEntry((short)217, "Read inscription one", "reading", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)218, "Read inscription two", "reading", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)219, "Read inscription three", "reading", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)220, "Realdeath", "realdeath", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)221, "Destroy", "destroying", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)222, "Manage", "opening", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)223, "Forage", "foraging", new int[]{4, 25, 37, 43, 48}, 4, false),
      new ActionEntry((short)224, "Botanize", "botanizing", new int[]{4, 25, 37, 43, 48}, 4, false),
      new ActionEntry((short)225, "Fillet", "filleting", new int[]{4, 25}, 4),
      new ActionEntry((short)226, "Withdraw money", "withdrawing", EMPTY_INT_ARRAY, 30),
      new ActionEntry((short)227, "Tunnel", "tunneling", new int[]{4, 25, 9, 19, 18, 21, 43, 49}, 4),
      new ActionEntry((short)228, "Work on", "working on", new int[]{4}, 4),
      new ActionEntry((short)229, "Reinforce", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)230, "Feed", "feeding", new int[]{33, 43, 44}),
      new ActionEntry((short)231, "Paint", "painting", new int[]{5, 25, 49}),
      new ActionEntry((short)232, "Remove paint", "removing", new int[]{5, 25, 36, 49}),
      new ActionEntry((short)233, "Load", "loading", new int[]{43, 49}, false),
      new ActionEntry((short)234, "Unload", "unloading", new int[]{43, 49}, false),
      new ActionEntry((short)235, "Unwind", "unwinding", new int[]{43}, false),
      new ActionEntry((short)236, "Fire", "firing", new int[]{9, 18, 21, 43}, false),
      new ActionEntry((short)237, "Winch", "winching", new int[]{9, 18, 21, 43}, false),
      new ActionEntry((short)238, "Winch five laps", "winching", new int[]{9, 18, 21, 43}, false),
      new ActionEntry((short)239, "Winch ten laps", "winching", new int[]{9, 18, 21, 43}, false),
      new ActionEntry((short)240, "Transfer north", "transferring", new int[]{22, 23, 36}),
      new ActionEntry((short)241, "Transfer east", "transferring", new int[]{22, 23, 36}),
      new ActionEntry((short)242, "Transfer south", "transferring", new int[]{22, 23, 36}),
      new ActionEntry((short)243, "Transfer west", "transferring", new int[]{22, 23, 36}),
      new ActionEntry((short)244, 8, "Wurm Servers", "managing", new int[]{22, 23, 36}),
      new ActionEntry((short)245, "Bless", "blessing", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)246, 8, "Cure Light", "curing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)247, 8, "Cure Medium", "curing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)248, 8, "Cure Serious", "curing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)249, 8, "Heal", "healing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)250, 8, "Refresh", "refreshing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)251, "Mend", "mending", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)252, 8, "Smite", "smiting", new int[]{2, 3, 36, 48}, 12, true),
      new ActionEntry((short)253, 8, "Sunder", "sundering", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)254, 8, "Drain Stamina", "draining", new int[]{2, 3, 36, 48}, 12, true),
      new ActionEntry((short)255, 8, "Drain Health", "draining", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)256, "Early Harvest", "healing", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)257, 8, "Drain Skill", "draining", new int[]{2, 3, 36, 48}, 6, true),
      new ActionEntry((short)258, 8, "Break Altar", "breaking", new int[]{2, 36, 48}, 10, true),
      new ActionEntry((short)259, "Toxin", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)260, "Blaze", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)261, "Glacial", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)262, "Corrosion", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)263, "Acid Protection", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)264, "Frost Protection", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)265, "Fire Protection", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)266, "Poison Protection", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)267, "Human Demise", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)268, "Monster Demise", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)269, "Animal Demise", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)270, "Legendary Demise", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)271, "Locate Artifact", "locating", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)272, "Vessel", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)273, "Rebirth", "summoning", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)274, 8, "Dominate", "dominating", new int[]{2, 3, 26, 36, 48}, 6, true),
      new ActionEntry((short)275, 8, "Charm Animal", "charming", new int[]{2, 3, 36, 48}, 6, true),
      new ActionEntry((short)276, "Circle of Cunning", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)277, "Flaming Aura", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)278, "Aura of Shared Pain", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)279, "Wind of Ages", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)280, "Opulence", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)281, "Rotting Touch", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)282, "Morning Fog", "enchanting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)283, "Mix", "mixing", new int[]{5, 36}),
      new ActionEntry((short)284, "Treat", "treating", new int[]{25, 36, 44}),
      new ActionEntry((short)285, "Lore", "thinking", new int[0]),
      new ActionEntry((short)286, "Become a priest", "becoming", new int[0]),
      new ActionEntry((short)287, 8, "Left Hard", "attacking", new int[]{3, 12, 14, 17}, 6, false),
      new ActionEntry((short)288, 8, "Left Normal", "attacking", new int[]{3, 12, 14, 17}, 6, false),
      new ActionEntry((short)289, 8, "Left quick", "attacking", new int[]{3, 12, 14, 17}, 6, false),
      new ActionEntry((short)290, 8, "Left Hard", "attacking", new int[]{3, 14, 17}, 6, false),
      new ActionEntry((short)291, 8, "Left Normal", "attacking", new int[]{3, 14, 17}, 6, false),
      new ActionEntry((short)292, 8, "Left Quick", "attacking", new int[]{3, 14, 17}, 6, false),
      new ActionEntry((short)293, 8, "Left Hard", "attacking", new int[]{3, 13, 14, 17}, 6, false),
      new ActionEntry((short)294, 8, "Left Normal", "attacking", new int[]{3, 13, 14, 17}, 6, false),
      new ActionEntry((short)295, 8, "Left Quick", "attacking", new int[]{3, 13, 14, 17}, 6, false),
      new ActionEntry((short)296, 8, "Hard", "attacking", new int[]{3, 13, 17}, 6, false),
      new ActionEntry((short)297, 8, "Normal", "attacking", new int[]{3, 13, 17}, 6, false),
      new ActionEntry((short)298, 8, "Quick", "attacking", new int[]{3, 13, 17}, 6, false),
      new ActionEntry((short)299, 8, "Hard", "attacking", new int[]{3, 12, 17}, 6, false),
      new ActionEntry((short)300, 8, "Normal", "attacking", new int[]{3, 12, 17}, 6, false),
      new ActionEntry((short)301, 8, "Quick", "attacking", new int[]{3, 12, 17}, 6, false),
      new ActionEntry((short)302, 8, "Hard", "attacking", new int[]{3, 17}, 6, false),
      new ActionEntry((short)303, 8, "Normal", "attacking", new int[]{3, 17}, 6, false),
      new ActionEntry((short)304, 8, "Quick", "attacking", new int[]{3, 17}, 6, false),
      new ActionEntry((short)305, 8, "Right Hard", "attacking", new int[]{3, 12, 15, 17}, 6, false),
      new ActionEntry((short)306, 8, "Right Normal", "attacking", new int[]{3, 12, 15, 17}, 6, false),
      new ActionEntry((short)307, 8, "Right Quick", "attacking", new int[]{3, 12, 15, 17}, 6, false),
      new ActionEntry((short)308, 8, "Right Hard", "attacking", new int[]{3, 15, 17}, 6, false),
      new ActionEntry((short)309, 8, "Right Normal", "attacking", new int[]{3, 15, 17}, 6, false),
      new ActionEntry((short)310, 8, "Right Quick", "attacking", new int[]{3, 15, 17}, 6, false),
      new ActionEntry((short)311, 8, "Right Hard", "attacking", new int[]{3, 13, 15, 17}, 6, false),
      new ActionEntry((short)312, 8, "Right Normal", "attacking", new int[]{3, 13, 15, 17}, 6, false),
      new ActionEntry((short)313, 8, "Right Quick", "attacking", new int[]{3, 13, 15, 17}, 6, false),
      new ActionEntry((short)314, 8, "High", "defending", new int[]{16}, 6, false),
      new ActionEntry((short)315, 8, "Left", "defending", new int[]{16}, 6, false),
      new ActionEntry((short)316, 8, "Low", "defending", new int[]{16}, 6, false),
      new ActionEntry((short)317, 8, "Right", "defending", new int[]{16}, 6, false),
      new ActionEntry((short)318, "Cultivate", "cultivating", new int[]{4, 36, 43, 49}),
      new ActionEntry((short)319, "Rent 1 copper", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)320, "Rent 10 copper", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)321, "Rent 1 silver", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)322, "Rent 10 silver", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)323, "No rent", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)324, "Rent", "renting", new int[]{22, 37}, 8),
      new ActionEntry((short)325, "Sleep", "sleeping", new int[]{6, 22, 37, 43}),
      new ActionEntry((short)326, "Target", "targetting", new int[]{0, 22, 29, 37, 43}, 200),
      new ActionEntry((short)327, "Freeze", "freezing", new int[]{22}, 200),
      new ActionEntry((short)328, "Suck Egg", "sucking", new int[]{0, 37}),
      new ActionEntry((short)329, "Spy", "spying", new int[]{6, 33}, 200),
      new ActionEntry((short)330, "Hatch", "hatching", EMPTY_INT_ARRAY),
      new ActionEntry((short)331, "Embark - commander", "embarking", new int[]{0, 43, 44}, 12, false),
      new ActionEntry((short)332, "Embark - passenger", "embarking", new int[]{0, 43, 44}, 12, false),
      new ActionEntry((short)333, "Disembark", "disembark", new int[]{0, 23, 43}, false),
      new ActionEntry((short)334, "Reimbursements", "reimbursing", new int[]{0}, 30),
      new ActionEntry((short)335, "Change terrain", "terraforming", new int[]{22, 23, 36}),
      new ActionEntry((short)336, "Check", "checking", new int[]{0, 37}),
      new ActionEntry((short)337, "Send", "sending", new int[]{0, 37}),
      new ActionEntry((short)338, "Courier", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)339, "Dark Messenger", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)340, 8, "Focus", "focusing", new int[]{17, 22, 24, 37, 48}, 6, false),
      new ActionEntry((short)341, 1, "No target", "targetting", new int[]{22, 23, 27, 37, 48}, 200, false),
      new ActionEntry((short)342, 8, "Throw", "throwing", new int[]{22, 3, 23}, 200, false),
      new ActionEntry((short)343, "Duel", "duelling", new int[]{0}, 40, false),
      new ActionEntry((short)344, "Spar", "sparring", new int[]{0}, 40, false),
      new ActionEntry((short)345, "Milk", "milking", new int[]{4, 36, 43, 48}),
      new ActionEntry((short)346, 8, "Heal", "healing", new int[]{22}, 16, true),
      new ActionEntry((short)347, 8, "Absorb", "absorbing", new int[]{37, 43}),
      new ActionEntry((short)348, "Disband settlement", "disbanding", EMPTY_INT_ARRAY, 3),
      new ActionEntry((short)349, "Stop disband", "salvaging", new int[]{22}, 3),
      new ActionEntry((short)350, "Drain coffers", "stealing", new int[]{5, 6}, 3),
      new ActionEntry((short)351, "Receive instructions", "asking", new int[]{0}, 12),
      new ActionEntry((short)352, "Log", "loggin", new int[]{0, 23}, 20),
      new ActionEntry((short)353, "Take the test", "testing", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)354, "Appoint", "appointing", EMPTY_INT_ARRAY, 8),
      new ActionEntry((short)355, "Kingdoms", "checking", EMPTY_INT_ARRAY, 8),
      new ActionEntry((short)356, "Kingdom history", "checking", EMPTY_INT_ARRAY, 8),
      new ActionEntry((short)357, "Announce", "announcing", new int[]{0}, 16),
      new ActionEntry((short)358, "Abdicate", "abdicating", new int[]{0}, 4),
      new ActionEntry((short)359, "Manage Vehicle", "managing", EMPTY_INT_ARRAY, 20, false),
      new ActionEntry((short)360, "Moor", "mooring", EMPTY_INT_ARRAY, 20),
      new ActionEntry((short)361, 8, "Raise anchor", "hauling", new int[]{23}, 20, false),
      new ActionEntry((short)362, "Dredge", "dredging", new int[]{1, 4, 9, 19, 20, 36, 43, 46, 48}, 20),
      new ActionEntry((short)363, "Build mine door", "building", new int[]{4, 36, 49}),
      new ActionEntry((short)364, "Manage mine door", "managing", new int[]{0, 33}, 16, false),
      new ActionEntry((short)365, "Rent 10 iron", "renting", new int[]{22, 37}, 8, false),
      new ActionEntry((short)366, "Rent 25 iron", "renting", new int[]{22, 37}, 8, false),
      new ActionEntry((short)367, "Rent 50 iron", "renting", new int[]{22, 37}, 8, false),
      new ActionEntry((short)368, "Setnp", "nping", new int[]{4}, 8),
      new ActionEntry((short)369, "Sculpt", "sculpting", new int[]{5, 36, 49}, 40),
      new ActionEntry((short)370, "Recharge", "recharging", EMPTY_INT_ARRAY),
      new ActionEntry((short)371, "Economics", "calculating", EMPTY_INT_ARRAY),
      new ActionEntry((short)372, "Manage perimeter", "managing", EMPTY_INT_ARRAY),
      new ActionEntry((short)373, "Prune", "pruning", new int[]{4, 25, 5, 36, 43, 48}),
      new ActionEntry((short)374, "Trap", "trapping", new int[]{4, 25}, 6),
      new ActionEntry((short)375, "Disarm", "disarming", new int[]{4, 25}, 6),
      new ActionEntry((short)376, "Sixth Sense", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)377, "Hitch", "hitching", new int[]{43, 44}, false),
      new ActionEntry((short)378, "Unhitch", "unhitching", new int[]{37, 43, 44}, false),
      new ActionEntry((short)379, 8, "Breed", "breeding", new int[]{4, 25, 43, 44, 48}, 2, false),
      new ActionEntry((short)380, "Wish", "wishing", EMPTY_INT_ARRAY),
      new ActionEntry((short)381, "Protect", "protecting", new int[]{23}),
      new ActionEntry((short)382, "Remove protection", "deprotecting", new int[]{23}),
      new ActionEntry((short)383, "Count items", "counting", EMPTY_INT_ARRAY),
      new ActionEntry((short)384, "Meditate", "meditating", new int[]{6, 25, 43, 48}),
      new ActionEntry((short)385, "Leave path", "leaving", new int[]{6}),
      new ActionEntry((short)386, "Path leaders", "status", EMPTY_INT_ARRAY),
      new ActionEntry((short)387, "Light path", "leading", EMPTY_INT_ARRAY),
      new ActionEntry((short)388, "Enchant", "enchanting", EMPTY_INT_ARRAY),
      new ActionEntry((short)389, "Love effect", "loving", new int[]{0}),
      new ActionEntry((short)390, "Increase attack damage", "damaging", new int[]{0}),
      new ActionEntry((short)391, "Increase structure damage", "damaging", new int[]{0}),
      new ActionEntry((short)392, "Fear effect", "scaring", new int[]{0}),
      new ActionEntry((short)393, "Elemental immunity", "protecting", new int[]{0}),
      new ActionEntry((short)394, "Trap immunity", "protecting", new int[]{0}),
      new ActionEntry((short)395, "Clean", "cleaning", new int[]{0}, 4),
      new ActionEntry((short)396, "Fill", "filling", new int[]{0, 37}),
      new ActionEntry((short)397, "Puppeteer", "puppeteering", new int[]{25}),
      new ActionEntry((short)398, "Groom", "grooming", new int[]{25, 36, 43, 44, 48}),
      new ActionEntry((short)399, "Link", "linking", new int[]{25}),
      new ActionEntry((short)400, "Holy Crop", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)401, "Ritual of the Sun", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)402, "Rite of Death", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)403, "Rite of Spring", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)404, 8, "Oakshell", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)405, 8, "Willowspine", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)406, 8, "Bearpaws", "casting", new int[]{2, 36, 48}, 40, true),
      new ActionEntry((short)407, "Humid Drizzle", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)408, "Genesis", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)409, "Life Transfer", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)410, 8, "Forest Giant Strength", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)411, "Frogleap", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)412, "Venom", "casting", new int[]{2, 36, 48}, 40),
      new ActionEntry((short)413, 8, "Tornado", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)414, 8, "Ice Pillar", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)415, "Mind Stealer", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)416, "Nimbleness", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)417, "Frost Brand", "casting", new int[]{2, 36, 48}, 40),
      new ActionEntry((short)418, 8, "Tentacles", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)419, "Locate Soul", "casting", new int[]{2, 36, 48}, 40),
      new ActionEntry((short)420, 8, "Fire Pillar", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)421, 8, "Light Token", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)422, "Goat Shape", "casting", new int[]{2, 36, 48}, 20),
      new ActionEntry((short)423, 8, "Frantic Charge", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)424, 8, "Fireheart", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)425, 8, "Mass Stamina", "casting", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)426, 8, "Phantasms", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)427, 8, "Hell Strength", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)428, 8, "Rotting Gut", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)429, 8, "Weakness", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)430, 8, "Worm Brains", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)431, 8, "Zombie Infestation", "casting", new int[]{2, 36, 48}, 50, true),
      new ActionEntry((short)432, 8, "Pain Rain", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)433, 8, "Fungus Trap", "casting", new int[]{2, 3, 36, 48}, 24, true),
      new ActionEntry((short)434, 8, "Out of Mind", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)435, 8, "Land of the Dead", "casting", new int[]{2, 36, 48}, 50, true),
      new ActionEntry((short)436, "Wild growth", "casting", new int[]{2, 36, 48}, 40),
      new ActionEntry((short)437, 8, "Ward", "casting", new int[]{2, 36, 48}, 40, true),
      new ActionEntry((short)438, 8, "Light of Fo", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)439, "Mole Senses", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)440, 8, "Strongwall", "casting", new int[]{2, 36, 48}, 40, true),
      new ActionEntry((short)441, 8, "Wrath of Magranon", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)442, "Excel", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)443, "Reveal Settlements", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)444, "Reveal Creatures", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)445, "Wisdom of Vynora", "casting", new int[]{2, 36, 48}, 12),
      new ActionEntry((short)446, 8, "Corrupt", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)447, 8, "Truehit", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)448, 8, "Scorn of Libila", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)449, 8, "Disintegrate", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)450, 8, "Dispel", "dispelling", new int[]{2, 36, 48}, 24, true),
      new ActionEntry((short)451, 8, "Nolocate", "hiding", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)452, "Spell list", "listing", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)453, "Dirt", "casting", new int[]{2, 36, 48}, 8),
      new ActionEntry((short)454, "Bloodthirst", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)455, "Web Armour", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)456, "Blessings of the Dark", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)457, "Lurker in the Deep", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)458, "Lurker in the Woods", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)459, "Lurker in the Dark", "casting", new int[]{2, 36, 48}, 4),
      new ActionEntry((short)460, "Wrap", "wrapping", new int[0], 4),
      new ActionEntry((short)461, "Toggle Limit", "toggling", new int[]{0}, 4),
      new ActionEntry((short)462, "Transmutate", "transmutating", new int[]{48, 6}, 4),
      new ActionEntry((short)463, "Insert", "inserting", new int[0], 4),
      new ActionEntry((short)464, "Remove ten", "removing", new int[]{5, 48}, 4),
      new ActionEntry((short)465, "Remove fifty", "removing", new int[]{5, 48}, 4),
      new ActionEntry((short)466, "Refund", "refunding", new int[0], 4),
      new ActionEntry((short)467, "GM interface", "GM interface", new int[]{0}, 4),
      new ActionEntry((short)468, "Cut old trees", "cutting", new int[]{48}, 4),
      new ActionEntry((short)469, "Team up", "inviting", new int[0], 300),
      new ActionEntry((short)470, "Remove from team", "kicking", new int[0], 300),
      new ActionEntry((short)471, "Manage team", "managing", new int[0], 300),
      new ActionEntry((short)472, "Manage missions", "managing", new int[0], 300),
      new ActionEntry((short)473, "Talk", "talking", new int[]{25}),
      new ActionEntry((short)474, "Show", "showing", new int[]{25}),
      new ActionEntry((short)475, "Step on", "stepping", new int[]{25}),
      new ActionEntry((short)476, "Get log events", "checking", new int[]{23, 37}, 300),
      new ActionEntry((short)477, "Build iron fence", "planning fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)478, "Build woven fence", "planning fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)479, "Build iron fence gate", "planning fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)480, "Found kingdom", "founding kingdom", new int[0]),
      new ActionEntry((short)481, "Twitter settings", "configuring", new int[0]),
      new ActionEntry((short)482, "Read random inscription", "reading", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)483, "Test terrain", "testing", EMPTY_INT_ARRAY, 200),
      new ActionEntry((short)484, "Brand", "branding", new int[]{36, 43, 44, 47}),
      new ActionEntry((short)485, 8, "Shard of Ice", "casting", new int[]{2, 3, 36, 48}, 50, true),
      new ActionEntry((short)486, "Test", "testing", new int[]{23, 36}, 200),
      new ActionEntry((short)487, "Vote for ruler removal", "voting", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)488, "Challenge ruler", "challenging", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)489, "Recall home", "recalling", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)490, 8, "Final Breath", "hurting", new int[]{3}, 8, true),
      new ActionEntry((short)491, "Kill", "killing", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)492, "Cut down", "cutting", new int[]{25}, 4),
      new ActionEntry((short)493, "Care for", "caring", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)494, "Voice chat options", "vco", EMPTY_INT_ARRAY, 200),
      new ActionEntry((short)495, "Last gasp", "gasping", EMPTY_INT_ARRAY, 200),
      new ActionEntry((short)496, "Ritual of faith", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)497, "Ritual of rain", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)498, "Ritual of fog", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)499, "Ritual of sun", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)500, "Ritual of healing", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)501, "Ritual of death", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)502, "Ritual of plague", "performing ritual", EMPTY_INT_ARRAY, 10),
      new ActionEntry((short)503, "Create focus zone", "creating zone", new int[]{23}, 10),
      new ActionEntry((short)504, "Conquer", "conquering", EMPTY_INT_ARRAY, 6),
      new ActionEntry((short)505, "Inscribe", "inscribing", EMPTY_INT_ARRAY, 1),
      new ActionEntry((short)506, "Read inscription", "Reading inscription", EMPTY_INT_ARRAY, 1),
      new ActionEntry((short)507, "Roof", "planning", new int[]{4, 36}, 4),
      new ActionEntry((short)508, "Floor above", "planning", new int[]{4, 36}, 4),
      new ActionEntry((short)509, "Floor below", "planning", new int[]{4, 36}, 4),
      new ActionEntry((short)510, "Manage achievements", "creating", EMPTY_INT_ARRAY, 200),
      new ActionEntry((short)511, "Karma", "karma", EMPTY_INT_ARRAY),
      new ActionEntry((short)512, "Weave wall", "walling", EMPTY_INT_ARRAY, 40),
      new ActionEntry((short)513, "Summon guards", "summoning", EMPTY_INT_ARRAY, 40),
      new ActionEntry((short)514, "Build hatch floor above", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)515, "Build ladder", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)516, "Build wooden parapet", "planning wooden parapet", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)517, "Build stone parapet", "planning stone parapet", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)518, "Raise corner", "raising corner", new int[]{4, 5}),
      new ActionEntry((short)519, "Smelt", "smelting", new int[]{4}, false),
      new ActionEntry((short)520, "Build crude wooden fence", "planning wooden fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)521, "Build stone and iron parapet", "planning stone and iron parapet", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)522, "Climb up", "climbing", new int[]{37, 43}),
      new ActionEntry((short)523, "Climb down", "climbing", new int[]{37, 43}),
      new ActionEntry((short)524, "Destroy floor", "destroying floor", new int[]{36}),
      new ActionEntry((short)525, "Destroy roof", "destroying roof", new int[]{36}),
      new ActionEntry((short)526, "Build low roundpole fence", "planning low roundpole fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)527, "Build high roundpole fence", "planning high roundpole fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)528, "Build crude wooden fence gate", "planning wooden fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)529, "Build roundpole fence gate", "planning roundpole fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)530, "Add to building", "planning expansion", new int[]{4, 25, 36}),
      new ActionEntry((short)531, "Remove from building", "planning retraction", new int[]{4, 25, 36}),
      new ActionEntry((short)532, "Level", "leveling", new int[]{1, 4, 9, 25, 19, 52, 38, 40, 36, 43, 49}),
      new ActionEntry((short)533, "Flatten", "flattening", new int[]{1, 4, 9, 25, 19, 20, 36, 43, 49}),
      new ActionEntry((short)534, "GM tool", "GM tool", new int[]{0, 23}, 4),
      new ActionEntry((short)535, 10, "Feature Management", "getting features", new int[]{23, 36}),
      new ActionEntry((short)536, "Analyse", "analysing", new int[]{1, 4, 25, 9, 19, 18, 21, 37}),
      new ActionEntry((short)537, "Threaten", "threatening", new int[]{5}),
      new ActionEntry((short)538, "Traits", "traiting", new int[]{23, 36}),
      new ActionEntry((short)539, "Enchants", "enchanting", new int[]{23, 36}),
      new ActionEntry((short)540, "Manage roles", "managing settlement", new int[]{0, 23}),
      new ActionEntry((short)541, "Build stone fence", "planning stone fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)542, "Build curb", "planning curb", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)543, "Build low rope fence", "planning low rope fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)544, "Build high rope fence", "planning high rope fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)545, "Build high iron fence", "planning high iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)546, "Build high iron fence gate", "planning high iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)547, 8, "Karma Disease", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)548, 8, "Rust Monster", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)549, 8, "Fireball", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)550, 8, "Karma Bolt", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)551, 8, "Karma Missile", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)552, 8, "Karma Continuum", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)553, "Stone skin", "casting", new int[]{2, 48}),
      new ActionEntry((short)554, 8, "Karma Slow", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)555, 8, "True Strike", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)556, 8, "Wall of Ice", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)557, 8, "Wall of Fire", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)558, 8, "Wall of Stone", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)559, "Summon", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)560, "Forecast", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)561, 8, "Lightning", "casting", new int[]{2, 48}, 24, true),
      new ActionEntry((short)562, 8, "Mirrored Self", "casting", new int[]{2, 48}),
      new ActionEntry((short)563, "Plant flowerbed", "planting flowers", new int[]{4, 9, 19, 36}),
      new ActionEntry((short)564, "Plant flower", "planting", new int[]{4, 36, 48}),
      new ActionEntry((short)565, "Water plant", "watering", new int[]{4, 36}),
      new ActionEntry((short)566, "Profile", "profile", new int[]{0, 37}, false),
      new ActionEntry((short)567, "Add group", "add group", new int[]{37}, false),
      new ActionEntry((short)568, "Open", "open", new int[]{0, 37}, false),
      new ActionEntry((short)569, "Vegetables", "foraging", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)570, "Resources", "foraging", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)571, "Berries", "foraging", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)572, "Seeds", "botanizing", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)573, "Herbs", "botanizing", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)574, "Plants", "botanizing", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)575, "Resource", "botanizing", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)576, "Pave nearest corner", "paving", new int[]{1, 9, 20, 36, 43}),
      new ActionEntry((short)577, "Turn invisible", "invisible", new int[]{0, 29, 37}, 300),
      new ActionEntry((short)578, "Turn visible", "visible", new int[]{0, 29, 37}, 300),
      new ActionEntry((short)579, "Invulnerable", "Invulnerable", new int[]{0, 29, 37}, 300),
      new ActionEntry((short)580, "Vulnerable", "Vulnerable", new int[]{0, 29, 37}, 300),
      new ActionEntry((short)581, "Decay", "Decay", new int[]{29, 36}),
      new ActionEntry((short)582, 8, "Equip", "equipping", new int[]{0}, 4, false),
      new ActionEntry((short)583, 8, "Equip left", "equipping left", new int[]{0}, 4, false),
      new ActionEntry((short)584, 8, "Equip right", "equipping right", new int[]{0}, 4, false),
      new ActionEntry((short)585, 8, "Unequip", "unequipping", new int[]{0}, 4, false),
      new ActionEntry((short)586, "Remove", "remove", new int[]{0, 37}, false),
      new ActionEntry((short)587, "View/Update Ticket...", "viewing", new int[]{0, 37}, false),
      new ActionEntry((short)588, "Cancel...", "cancelling", new int[]{0, 37}, false),
      new ActionEntry((short)589, "Respond", "responding", new int[]{0, 37}, false),
      new ActionEntry((short)590, "Resolve...", "resolving", new int[]{0, 37}, false),
      new ActionEntry((short)591, "GM...", "forwarding to GM", new int[]{0, 37}, false),
      new ActionEntry((short)592, "Arch...", "forwarding to Arch", new int[]{0, 37}, false),
      new ActionEntry((short)593, "Dev...", "forwarding to Dev", new int[]{0, 37}, false),
      new ActionEntry((short)594, "On Hold...", "putting on hold", new int[]{0, 37}, false),
      new ActionEntry((short)595, "Take Ticket", "taking ticket", new int[]{0, 37}, false),
      new ActionEntry((short)596, "CM...", "forwarding to CM", new int[]{0, 37}, false),
      new ActionEntry((short)597, "Ticket Survey...", "doing Survey", new int[]{0, 37}, false),
      new ActionEntry((short)598, "Create recruitment ad", "creating recruitment ad", new int[]{0, 23}),
      new ActionEntry((short)599, "ReOpen...", "ReOpening", new int[]{0, 37}, false),
      new ActionEntry((short)600, "Discard", "discarding", new int[]{25, 4}),
      new ActionEntry((short)601, "Look for village", "Look for village", new int[]{0, 23, 43}),
      new ActionEntry((short)602, "Delete recruitment ad", "deleting recruitment ad", new int[]{0, 23}),
      new ActionEntry((short)603, "Edit recruitment ad", "editing recruitment ad", new int[]{0, 23}),
      new ActionEntry((short)604, "Paint terrain", "painting", new int[]{23, 36}, false),
      new ActionEntry((short)605, "Load cargo", "loading cargo", new int[]{37, 5, 43, 44, 5, 49}, 12, false),
      new ActionEntry((short)606, "Unload cargo", "unloading cargo", new int[]{37, 5, 43, 44, 5, 49}, 12, false),
      new ActionEntry((short)607, "Add to crafting window", "adding to window", new int[]{37, 43}, false),
      new ActionEntry((short)608, "Switch deity", "switching", new int[]{0}),
      new ActionEntry((short)609, "Voting Set up", "Settingup", new int[]{37, 0}),
      new ActionEntry((short)610, "Commune", "communing", new int[]{0}),
      new ActionEntry((short)611, "Build chain fence", "planning chain fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)612, "Build wooden wall", "building wooden wall", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)613, "Build wooden window", "building wooden window", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)614, "Build wooden door", "building wooden door", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)615, "Build wooden double door", "building wooden double door", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)616, "Build wooden arched wall", "building wooden arched wall", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)617, "Build stone wall", "building stone wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)618, "Build stone window", "building stone window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)619, "Build stone door", "building stone door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)620, "Build stone double door", "building stone double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)621, "Build stone arched wall", "building stone arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)622, "Build timber framed wall", "building timber framed wall", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)623, "Build timber framed window", "building timber framed window", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)624, "Build timber framed door", "building timber framed door", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)625, "Build timber framed double door", "building timber framed double door", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)626, "Build timber framed arched wall", "building timber framed arched wall", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)627, "Rough cobblestone", "paving", new int[]{1, 9, 25, 20, 43}),
      new ActionEntry((short)628, "Round cobblestone", "paving", new int[]{1, 9, 25, 20, 43}),
      new ActionEntry((short)629, "Summon Worg", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)630, "Summon Wraith", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)631, "Summon Skeleton", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)632, "Transfer rarity", "transferring", new int[0]),
      new ActionEntry((short)633, "Imbue", "imbuing", new int[0]),
      new ActionEntry((short)634, "Sprout trees", "casting", new int[]{2, 48}, 24),
      new ActionEntry((short)635, "Set up CA Help Groups", "Settingup", new int[]{37, 0}),
      new ActionEntry((short)636, "Hold", "holding", new int[]{36, 6}, false),
      new ActionEntry((short)637, "Plan bridge", "planning a bridge", new int[]{36, 6, 4, 23}, false),
      new ActionEntry((short)638, "As Pile", "dropping", new int[]{0, 25, 48}, false),
      new ActionEntry((short)639, "Drain", "draining", new int[]{4, 23, 24}, false),
      new ActionEntry((short)640, "Survey", "surveying", new int[]{36, 6, 4, 23}, false),
      new ActionEntry((short)641, 8, "Tangleweave", "tangles", new int[]{2, 3, 36, 48}, 50, false),
      new ActionEntry((short)642, "Rummage", "rummaging", new int[]{4, 25, 37, 43}, 4, false),
      new ActionEntry((short)643, "Remove Brand", "un-branding", new int[]{36, 43, 44, 47}),
      new ActionEntry((short)644, "Trim", "triming", new int[]{36, 4, 45, 48}),
      new ActionEntry((short)645, "Gather", "gathering", new int[]{36, 4, 25, 48}),
      new ActionEntry((short)646, "Shear", "shearing", new int[]{36, 4, 44, 48, 43}),
      new ActionEntry((short)647, "Modify", "modifying", new int[]{36, 4}, 4),
      new ActionEntry((short)648, "Build plain stone wall", "building plain stone wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)649, "Build plain stone window", "building plain stone window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)650, "Build plain narrow stone window", "building plain narrow stone window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)651, "Build plain stone door", "building plain stone door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)652, "Build plain stone double door", "building plain stone double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)653, "Build plain stone arched wall", "building plain stone arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)654, "Build portcullis", "building portcullis", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)655, "Build plain stone portcullis", "building plain stone portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)656, "Build plain stone barred wall", "building plain stone barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)657, "Build stone portcullis", "building stone portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)658, "Build wood portcullis", "building wood portcullis", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)659, "Build staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)660, "Plant in center", "planting", new int[]{4, 25, 36, 48}),
      new ActionEntry((short)661, "Friends", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)662, "InsideOut", "toggling", new int[]{0, 36}, false),
      new ActionEntry((short)663, "Manage Animal", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)664, "Manage Building", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)665, "Manage Large Cart", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)666, "Manage Door", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)667, "Manage Gate", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)668, "Manage Ship", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)669, "Manage Wagon", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)670, "Add to Upkeep", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)671, "Haul up", "hauling", new int[]{36, 5}),
      new ActionEntry((short)672, "Haul down", "hauling", new int[]{36, 5}),
      new ActionEntry((short)673, "Manage All Doors", "managing all doors", new int[]{0, 37}, false),
      new ActionEntry((short)674, "Tag Item", "tagging", new int[]{36}),
      new ActionEntry((short)675, "Summon Tagged Item", "summoning", new int[]{36}),
      new ActionEntry((short)676, "Build timber framed balcony", "building timber framed balcony", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)677, "Build timber framed jetty", "building timber framed jetty", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)678, "Build decorated stone oriel", "building decorated stone oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)679, "Build wooden canopy", "building wooden canopy", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)680, "Build wooden wide window", "building wooden wide window", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)681, "Build plain stone oriel", "building plain stone oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)682, 1, "Use", "using", new int[0], false),
      new ActionEntry((short)683, "Rotate wall", "rotating wall", new int[]{4, 37}),
      new ActionEntry((short)684, "Manage Restrictions", "managing restrictions", new int[]{0, 36}, false),
      new ActionEntry((short)685, "Pickup planted item", "USE THIS ONE ONLY TO CHECK CRIMINALITY", new int[]{0, 49}),
      new ActionEntry((short)686, 8, "Incinerate", "casting", new int[]{3, 2, 48}, 24, true),
      new ActionEntry((short)687, "Manage Small Cart", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)688, "Manage Item", "managing", new int[]{0, 37}, false),
      new ActionEntry((short)689, "Show Village Plan", "showing", new int[]{0, 37}, false),
      new ActionEntry((short)690, "Search Permissions", "searching", new int[]{0, 37}, false),
      new ActionEntry((short)691, "History of Building", "viewing", new int[]{0, 37}, false),
      new ActionEntry((short)692, "History of Door", "viewing", new int[]{0, 37}, false),
      new ActionEntry((short)693, "Skip Tutorial", "skipping", new int[]{0, 37}, false),
      new ActionEntry((short)694, "Rough in nearest corner", "paving", new int[]{1, 9, 25, 20, 43}),
      new ActionEntry((short)695, "Round in nearest corner", "paving", new int[]{1, 9, 25, 20, 43}),
      new ActionEntry((short)696, "Push gently", "pushing", new int[]{37, 5, 44, 49}, 6, false),
      new ActionEntry((short)697, "Pull gently", "pulling", new int[]{37, 5, 25, 44, 49}, 6, false),
      new ActionEntry((short)698, 8, "Toggle CA Status", "toggling CA status", new int[]{22, 23}, false),
      new ActionEntry((short)699, 8, "Toggle CM status", "toggling CM status", new int[]{22, 23}, false),
      new ActionEntry((short)700, 8, "Player info", "getting player info", new int[]{22, 23}),
      new ActionEntry((short)701, "Sit", "sitting", new int[]{0, 43, 44}, 12, false),
      new ActionEntry((short)702, "Sit on the left", "sitting", new int[]{0, 43, 44}, 12, false),
      new ActionEntry((short)703, "Sit on the right", "sitting", new int[]{0, 43, 44}, 12, false),
      new ActionEntry((short)704, "Build wide staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)705, "Build right staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)706, "Build left staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)707, "Bury all", "burying", new int[]{43, 44}),
      new ActionEntry((short)708, "Stand up", "stand up", new int[]{0, 23, 43}, false),
      new ActionEntry((short)709, "Build clockwise spiral staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)710, "Build counter clockwise spiral staircase", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)711, "Build clockwise spiral staircase with banisters", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)712, "Build counter clockwise spiral staircase with banisters", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)713, "Build wide staircase with right banister", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)714, "Build wide staircase with left banister", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)715, "Build wide staircase with both banisters", "building", new int[]{4, 36}, 4),
      new ActionEntry((short)716, "Target hostile", "targetting", new int[]{0, 22, 29, 37}, 200),
      new ActionEntry((short)717, "Plot course", "plotting", new int[]{0, 23}, false),
      new ActionEntry((short)718, "Default action", "defaulting", new int[]{36}, false),
      new ActionEntry((short)719, "Kingdom members", "viewing member list", new int[]{0, 23}),
      new ActionEntry((short)720, "Spices", "botanizing", new int[]{4, 37, 48}, 4, false),
      new ActionEntry((short)721, "Meditation Path", "setting path", new int[]{23, 36}),
      new ActionEntry((short)722, "Change Path", "changing path", EMPTY_INT_ARRAY),
      new ActionEntry((short)723, 8, "Equip", "equipping", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)724, 8, "Equip", "equipping", EMPTY_INT_ARRAY, 4, false),
      new ActionEntry((short)725, "1g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)726, "2g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)727, "5g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)728, "10g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)729, "20g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)730, "50g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)731, "100g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)732, "200g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)733, "500g", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)734, "1kg", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)735, "2kg", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)736, "5kg", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)737, "10kg", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)738, "Cook book", "showing", new int[]{37}, 4, false),
      new ActionEntry((short)739, "Seal", "sealing", new int[0], 4, false),
      new ActionEntry((short)740, "Unseal", "unsealing", new int[]{37}, 4, false),
      new ActionEntry((short)741, "Collect snow", "collecting", new int[]{37}, 4, false),
      new ActionEntry((short)742, "Write recipe", "writing", new int[]{36}, 4, false),
      new ActionEntry((short)743, "Read recipe", "reading", new int[]{37}, 4, false),
      new ActionEntry((short)744, "Add recipe", "adding", new int[]{36}, 4, false),
      new ActionEntry((short)745, 8, "Throw", "throwing", new int[]{22, 23}, 20, false),
      new ActionEntry((short)746, "On left", "planting", new int[]{48}),
      new ActionEntry((short)747, "On right", "planting", new int[]{48}),
      new ActionEntry((short)748, "View links", "viewing", new int[]{6}, 4, false),
      new ActionEntry((short)749, "North", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)750, "Northeast", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)751, "East", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)752, "Southeast", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)753, "South", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)754, "Southwest", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)755, "West", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)756, "Northhwest", "setting", new int[]{37}, 4, false),
      new ActionEntry((short)757, "Pry", "prying", new int[]{36, 43, 5, 4}, 4, false),
      new ActionEntry((short)758, "Find route", "finding route", new int[]{37}, 4, false),
      new ActionEntry((short)759, "View protection", "viewing", new int[]{6}, 4, false),
      new ActionEntry((short)760, "Build wood left arch", "building wood left arch", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)761, "Build wood right arch", "building wood right arch", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)762, "Build wood T arch", "building wood T arch", "buildwoodenwall", new int[]{4, 36}),
      new ActionEntry((short)763, "Build stone left arch", "building stone left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)764, "Build stone right arch", "building stone right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)765, "Build stone T arch", "building stone T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)766, "Build timber framed left arch", "building timber framed left arch", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)767, "Build timber framed right arch", "building timber framed right arch", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)768, "Build timber framed T arch", "building timber framed T arch", "buildtimberframedwall", new int[]{4, 36}),
      new ActionEntry((short)769, "Build plain stone left arch", "building plain stone arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)770, "Build plain stone right arch", "building plain stone right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)771, "Build plain stone T arch", "building plain stone T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)772, "Build slate brick wall", "building slate brick wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)773, "Build slate brick window", "building slate brick window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)774, "Build slate brick narrow window", "building slate brick narrow window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)775, "Build slate brick door", "building slate brick door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)776, "Build slate brick double door", "building slate brick double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)777, "Build slate brick arched wall", "building slate brick arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)778, "Build slate brick portcullis", "building slate brick portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)779, "Build slate brick barred wall", "building slate brick barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)780, "Build slate brick oriel", "building slate brick oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)781, "Build slate brick left arch", "building slate brick left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)782, "Build slate brick right arch", "building slate brick right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)783, "Build slate brick T arch", "building slate brick T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)784, "Build rounded stone wall", "building rounded stone wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)785, "Build rounded stone window", "building rounded stone window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)786, "Build rounded stone narrow window", "building rounded stone narrow window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)787, "Build rounded stone door", "building rounded stone door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)788, "Build rounded stone double door", "building rounded stone double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)789, "Build rounded stone arched wall", "building rounded stone arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)790, "Build rounded stone portcullis", "building rounded stone portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)791, "Build rounded stone barred wall", "building rounded stone barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)792, "Build rounded stone oriel", "building rounded stone oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)793, "Build rounded stone left arch", "building rounded stone left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)794, "Build rounded stone right arch", "building rounded stone right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)795, "Build rounded stone T arch", "building rounded stone T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)796, "Build pottery brick wall", "building pottery brick wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)797, "Build pottery brick window", "building pottery brick window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)798, "Build pottery brick narrow window", "building pottery brick narrow window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)799, "Build pottery brick door", "building pottery brick door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)800, "Build pottery brick double door", "building pottery brick double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)801, "Build pottery brick arched wall", "building pottery brick arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)802, "Build pottery brick portcullis", "building pottery brick portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)803, "Build pottery brick barred wall", "building pottery brick barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)804, "Build pottery brick oriel", "building pottery brick oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)805, "Build pottery brick left arch", "building pottery brick left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)806, "Build pottery brick right arch", "building pottery brick right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)807, "Build pottery brick T arch", "building pottery brick T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)808, "Build sandstone brick wall", "building sandstone brick wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)809, "Build sandstone brick window", "building sandstone brick window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)810, "Build sandstone brick narrow window", "building sandstone brick narrow window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)811, "Build sandstone brick door", "building sandstone brick door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)812, "Build sandstone brick double door", "building sandstone brick double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)813, "Build sandstone brick arched wall", "building sandstone brick arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)814, "Build sandstone brick portcullis", "building sandstone brick portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)815, "Build sandstone brick barred wall", "building sandstone brick barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)816, "Build sandstone brick oriel", "building sandstone brick oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)817, "Build sandstone brick left arch", "building sandstone brick left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)818, "Build sandstone brick right arch", "building sandstone brick right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)819, "Build sandstone brick T arch", "building sandstone brick T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)820, "Build marble brick wall", "building marble brick wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)821, "Build marble brick window", "building marble brick window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)822, "Build marble brick narrow window", "building marble brick narrow window", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)823, "Build marble brick door", "building marble brick door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)824, "Build marble brick double door", "building marble brick double door", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)825, "Build marble brick arched wall", "building marble brick arched wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)826, "Build marble brick portcullis", "building marble brick portcullis", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)827, "Build marble brick barred wall", "building marble brick barred wall", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)828, "Build marble brick oriel", "building marble brick oriel", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)829, "Build marble brick left arch", "building marble brick left arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)830, "Build marble brick right arch", "building marble brick right arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)831, "Build marble brick T arch", "building marble brick T arch", "buildstonewall", new int[]{4, 36}),
      new ActionEntry((short)832, "Build slate fence", "planning slate fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)833, "Build slate iron fence", "planning slate iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)834, "Build slate iron fence gate", "planning slate iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)835, "Build rounded stone fence", "planning rounded stone fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)836, "Build rounded stone iron fence", "planning rounded stone iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)837, "Build rounded stone iron fence gate", "planning rounded stone iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)838, "Build pottery fence", "planning pottery fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)839, "Build pottery iron fence", "planning pottery iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)840, "Build pottery iron fence gate", "planning pottery iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)841, "Build sandstone fence", "planning sandstone fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)842, "Build sandstone iron fence", "planning sandstone iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)843, "Build sandstone iron fence gate", "planning sandstone iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)844, "Build marble fence", "planning marble fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)845, "Build marble iron fence", "planning marble iron fence", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)846, "Build marble iron fence gate", "planning marble iron fence gate", "buildfence", new int[]{4, 36, 49}),
      new ActionEntry((short)847, "Render wall", "rendering wall", new int[]{4, 36}),
      new ActionEntry((short)848, "Swap arch side", "swaping arch side", new int[]{4, 36}),
      new ActionEntry((short)849, "Increase angle", "increasing angle", new int[]{9, 18, 21}, false),
      new ActionEntry((short)850, "Reduce angle", "reducing angle", new int[]{9, 18, 21}, false),
      new ActionEntry((short)851, "Ram", "ramming", new int[]{9, 18, 21, 43}, false),
      new ActionEntry((short)852, "Study", "studying", new int[]{37}),
      new ActionEntry((short)853, "Record harvest times", "recording", new int[]{36}),
      new ActionEntry((short)854, "Add folder", "adding", new int[]{37}),
      new ActionEntry((short)855, "Remove folder", "removing", new int[]{37}),
      new ActionEntry((short)856, "Clad with stone bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)857, "Clad with slate bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)858, "Clad with pottery bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)859, "Clad with rounded stone bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)860, "Clad with sandstone bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)861, "Clad with marble bricks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)862, "Clad with planks", "reinforcing", new int[]{4, 25, 9, 18, 21, 47}, 4),
      new ActionEntry((short)863, "Wagoners", "managing wagoners", new int[]{47}, 4, true),
      new ActionEntry((short)864, "Move to center", "moving to center", new int[]{37, 5, 44, 49}, 6),
      new ActionEntry((short)865, "Level", "leveling", new int[]{1, 4, 9, 25, 19, 20, 36, 43, 49}),
      new ActionEntry((short)866, "Build all wall plans", "building all wall plans", new int[]{0, 23}),
      new ActionEntry((short)867, "Build scaffolding opening", "building scaffolding opening", "buildwoodenwall", new int[]{4, 9, 36}),
      new ActionEntry((short)868, "Build scaffolding floor", "building scaffolding floor", "buildwoodenwall", new int[]{4, 9, 36}),
      new ActionEntry((short)869, "Build scaffolding support", "building scaffolding support", "buildwoodenwall", new int[]{4, 9, 36}),
      new ActionEntry((short)870, "Build tall slate wall", "planning tall slate wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)871, "Build slate portcullis", "planning slate portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)872, "Build slate high iron fence", "planning slate high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)873, "Build slate high iron fence gate", "planning slate high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)874, "Build slate parapet", "planning slate parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)875, "Build slate chain fence", "planning slate chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)876, "Build tall rounded stone wall", "planning tall rounded stone wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)877, "Build rounded stone portcullis", "planning rounded stone portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)878, "Build rounded stone high iron fence", "planning rounded stone high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)879, "Build rounded stone high iron fence gate", "planning rounded stone high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)880, "Build rounded stone parapet", "planning rounded stone parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)881, "Build rounded stone chain fence", "planning rounded stone chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)882, "Build tall sandstone wall", "planning tall sandstone wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)883, "Build sandstone portcullis", "planning sandstone portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)884, "Build sandstone high iron fence", "planning sandstone high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)885, "Build sandstone high iron fence gate", "planning sandstone high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)886, "Build sandstone parapet", "planning sandstone parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)887, "Build sandstone chain fence", "planning sandstone chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)888, "Build tall rendered wall", "planning tall rendered wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)889, "Build rendered portcullis", "planning rendered portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)890, "Build rendered high iron fence", "planning rendered high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)891, "Build rendered high iron fence gate", "planning rendered high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)892, "Build rendered parapet", "planning rendered parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)893, "Build rendered chain fence", "planning rendered chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)894, "Build tall pottery wall", "planning tall pottery wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)895, "Build pottery portcullis", "planning pottery portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)896, "Build pottery high iron fence", "planning pottery high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)897, "Build pottery high iron fence gate", "planning pottery high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)898, "Build pottery parapet", "planning pottery parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)899, "Build pottery chain fence", "planning pottery chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)900, "Build tall marble wall", "planning tall marble wall", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)901, "Build marble portcullis", "planning marble portcullis", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)902, "Build marble high iron fence", "planning marble high iron fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)903, "Build marble high iron fence gate", "planning marble high iron fence gate", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)904, "Build marble parapet", "planning marble parapet", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)905, "Build marble chain fence", "planning marble chain fence", "buildfence", new int[]{4, 49}),
      new ActionEntry((short)906, "Remove mine door", "removing mine door", new int[]{49}),
      new ActionEntry((short)907, "Load Creature", "loading creature", new int[]{4}),
      new ActionEntry((short)908, "Unload Creaure", "unloading creature", new int[]{4}),
      new ActionEntry((short)909, "Get Creature Info", "getting", new int[]{4}),
      new ActionEntry((short)910, "Investigate", "investigating", new int[]{4, 36, 43}, false),
      new ActionEntry((short)911, "Identify", "identifying", new int[]{4, 36}),
      new ActionEntry((short)912, "Combine fragments", "combining fragments", new int[]{4, 36}),
      new ActionEntry((short)913, "Destroy", "destroying", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)914, "transferring bulk items", "transferring bulk items", new int[]{49, 5, 44, 51}, 4),
      new ActionEntry((short)915, "Set up a delivery", "setting up a delivery", new int[]{47}, 4, true),
      new ActionEntry((short)916, "Deliveries", "managing deliveries", new int[]{47}, 4, true),
      new ActionEntry((short)917, "Cancel delivery", "cancelling a delivery", new int[]{47}, 4, true),
      new ActionEntry((short)918, "View delivery", "viewing delivery", new int[]{47}, 4, true),
      new ActionEntry((short)919, "Wagoner history", "checking history", new int[]{0, 23}, false),
      new ActionEntry((short)920, "Dismiss", "dismissing wagoner", new int[]{0, 23}, false),
      new ActionEntry((short)921, "Dig", "digging", new int[]{1, 4, 25, 46, 9, 19, 20, 43, 48}),
      new ActionEntry((short)922, "Clean", "cleaning", new int[]{4, 36}),
      new ActionEntry((short)923, "Dye sails", "dying", new int[]{5, 25, 49}),
      new ActionEntry((short)924, "Remove dye", "removing dye", new int[]{5, 25, 36, 49}),
      new ActionEntry((short)925, "Place", "placing", new int[]{37}, 6, false),
      new ActionEntry((short)926, "Place", "placing", new int[]{37}, 6, false),
      new ActionEntry((short)927, "Switch tile", "switching", new int[]{1, 4, 9, 19, 48}),
      new ActionEntry((short)928, "Change Age", "changing", new int[]{1, 4, 9, 19, 48}),
      new ActionEntry((short)929, 8, "Focused Will", "healing", new int[]{2, 36, 48}, 12, true),
      new ActionEntry((short)930, 8, "Cleanse", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)931, 8, "Inferno", "casting", new int[]{2, 36, 48}, 50, true),
      new ActionEntry((short)932, 8, "Hypothermia", "casting", new int[]{2, 36, 48}, 50, true),
      new ActionEntry((short)933, 8, "Essence Drain", "casting", new int[]{2, 36, 48}, 4, true),
      new ActionEntry((short)934, 8, "Summon Soul", "casting", new int[]{2, 36, 48}, 40, true),
      new ActionEntry((short)935, "Search", "searching", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)936, "Make fishing bait", "making bait", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)937, "Make fishing float", "making float", EMPTY_INT_ARRAY, 4),
      new ActionEntry((short)938, "Catch flies", "catching flies", new int[]{36}, 4),
      new ActionEntry((short)939, "Upgrade", "upgrading", new int[]{37}, 4),
      new ActionEntry((short)940, 8, "Open keep net", "opening", new int[]{0, 37, 44, 43}, false),
      new ActionEntry((short)941, "Close keep net", "closing", new int[]{0, 37, 43}, false),
      new ActionEntry((short)942, "Pry open", "opening", new int[]{36, 44}, false),
      new ActionEntry((short)943, "Attach", "attaching", new int[]{36, 44}, false),
      new ActionEntry((short)944, "Detach", "detaching", new int[]{37, 44}, false),
      new ActionEntry((short)945, 8, "Use Rune", "using rune", new int[]{36, 6}, 4, false),
      new ActionEntry((short)946, 8, "Purge", "casting", new int[]{2, 36, 48}, 24, true)
   };

   private Actions() {
   }

   public static String getVerbForAction(short action) {
      return actionEntrys[action].getVerbString();
   }

   public static String getActionString(short action) {
      return actionEntrys[action].getActionString();
   }

   public static String createRandomString() {
      Random rand = new Random();
      int length = rand.nextInt(3) + 5;
      char[] password = new char[length];

      for(int x = 0; x < length; ++x) {
         int randDecimalAsciiVal = rand.nextInt("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
         password[x] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(randDecimalAsciiVal);
      }

      return LoginHandler.raiseFirstLetter(String.valueOf(password));
   }

   private static void createDefaultCreatureActions() {
      defaultCreatureActions.add(new ActionEntry((short)-1, "Look", "look", EMPTY_INT_ARRAY));
      defaultCreatureActions.add(actionEntrys[2]);
   }

   private static void createDefaultTileActions() {
      defaultTileActions.add(actionEntrys[109]);
   }

   static List<ActionEntry> getDefaultItemActions() {
      return defaultItemActions;
   }

   static List<ActionEntry> getDefaultCreatureActions() {
      return defaultCreatureActions;
   }

   static List<ActionEntry> getDefaultTileActions() {
      return defaultTileActions;
   }

   static List<ActionEntry> getDefaultTileBorderActions() {
      return defaultTileBorderActions;
   }

   static List<ActionEntry> getDefaultPlanetActions() {
      return defaultPlanetActions;
   }

   static List<ActionEntry> getDefaultStructureActions() {
      return defaultStructureActions;
   }

   static List<ActionEntry> getDefaultWoundActions() {
      return defaultWoundActions;
   }

   static final float getStaminaModiferFor(Creature performer, int staminaNeeded) {
      int currstam = performer.getStatus().getStamina();
      float staminaMod = 1.0F;
      if (currstam > 60000) {
         staminaMod = 0.8F;
      }

      staminaMod += 1.0F - (float)currstam / 65535.0F;
      if (currstam < staminaNeeded) {
         float diff = (float)(staminaNeeded - currstam);
         staminaMod += diff / (float)staminaNeeded * performer.getStaminaMod();
      }

      return staminaMod;
   }

   public static final int getStandardActionTime(Creature performer, Skill skill, @Nullable Item source, double bonus) {
      double knowl = 1.0;
      if (source == null) {
         knowl = skill.getKnowledge(0.0);
      } else {
         knowl = skill.getKnowledge(source, 0.0);
      }

      float multiplier = 1.3F / Servers.localServer.getActionTimer();
      double time = (100.0 - knowl) * (double)multiplier;
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = 30.0 + time * (1.0 - 0.2 * (double)source.getSpellSpeedBonus() / 100.0);
      } else {
         time = 30.0 + time;
      }

      if (source != null) {
         time -= (double)getRarityTimeModifier(source.getRarity());
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      time *= (double)getStaminaModiferFor(performer, 20000);
      time = Math.min(65535.0, time);
      return (int)time;
   }

   public static final int getQuickActionTime(Creature performer, Skill skill, @Nullable Item source, double bonus) {
      double knowl = 1.0;
      if (source == null) {
         knowl = skill.getKnowledge(0.0);
      } else {
         knowl = skill.getKnowledge(source, 0.0);
      }

      double time = (100.0 - knowl) / (double)Servers.localServer.getActionTimer();
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = 30.0 + time * (1.0 - 0.5 * (double)source.getSpellSpeedBonus() / 100.0);
      } else {
         time = 30.0 + time;
      }

      if (source != null) {
         time -= (double)getRarityTimeModifier(source.getRarity());
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      time *= (double)getStaminaModiferFor(performer, 20000);
      time = Math.min(65535.0, time);
      return (int)time;
   }

   public static final int getVariableActionTime(Creature performer, Skill skill, @Nullable Item source, double bonus, int max, int min, int staminaNeeded) {
      double skillVal = source == null ? skill.getKnowledge(bonus) : skill.getKnowledge(source, bonus);
      float multiplier = (float)(max - min) / 100.0F / Servers.localServer.getActionTimer();
      double time = (100.0 - skillVal) * (double)multiplier;
      if (source != null) {
         if (source.getSpellSpeedBonus() > 0.0F) {
            time *= (double)(1.0F - source.getSpellSpeedBonus() / 100.0F * 0.5F);
         }

         if (source.getRarity() > 0) {
            time *= (double)(1.0F - getRarityTimeModifier(source.getRarity()));
         }

         if (source.getSpellEffects() != null) {
            time *= (double)(1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED));
         }
      }

      time *= (double)getStaminaModiferFor(performer, staminaNeeded);
      time += (double)min;
      return Math.min((int)time, 65535);
   }

   public static final int getSlowActionTime(Creature performer, Skill skill, @Nullable Item source, double bonus) {
      double knowl = 1.0;
      if (source == null) {
         knowl = skill.getKnowledge(0.0);
      } else {
         knowl = skill.getKnowledge(source, 0.0);
      }

      float multiplier = 3.0F / Servers.localServer.getActionTimer();
      double time = Math.max(20.0, (100.0 - knowl) * (double)multiplier);
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = 25.0 + time * (1.0 - 0.5 * (double)source.getSpellSpeedBonus() / 100.0);
      } else {
         time = 25.0 + time;
      }

      if (skill.getNumber() == 1008) {
         time = Math.min(250.0, time);
      }

      if (source != null) {
         time -= (double)getRarityTimeModifier(source.getRarity());
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      time *= (double)getStaminaModiferFor(performer, 20000);
      time = Math.min(65535.0, time);
      return (int)time;
   }

   public static final int getPickActionTime(Creature performer, Skill skill, Item source, double bonus) {
      double knowl = 1.0;
      if (source == null) {
         knowl = skill.getKnowledge(0.0);
      } else {
         knowl = skill.getKnowledge(source, 0.0);
      }

      int time = (int)(Math.max(300.0, (100.0 - knowl) * 5.0) / (double)Servers.localServer.getActionTimer());
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = (int)Math.max(200.0, (double)time * (1.0 - 0.5 * (double)source.getSpellSpeedBonus() / 100.0));
      }

      if (source != null) {
         time = (int)((float)time - getRarityTimeModifier(source.getRarity()) * 10.0F);
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time = (int)((float)time * modifier);
         }
      }

      time = (int)((float)time * getStaminaModiferFor(performer, 5000));
      return Math.min(65535, time);
   }

   public static final int getMoveActionTime(Creature performer) {
      int time = (int)Math.max(50.0F, 50.0F * getStaminaModiferFor(performer, 20000));
      time = Math.min(65535, time);
      if (canReceiveDeedSpeedBonus(performer)) {
         time /= 5;
      }

      return time;
   }

   public static final int getLoadUnloadActionTime(Creature performer) {
      int time = 50;
      if (canReceiveDeedSpeedBonus(performer)) {
         time /= 5;
      }

      return time;
   }

   public static final int getPlantActionTime(Creature performer, Item item) {
      int time = 100;
      if (canReceiveDeedSpeedBonus(performer)) {
         time /= 5;
      } else if (item.isRoadMarker()) {
         Skill paving = performer.getSkills().getSkillOrLearn(10031);
         double knowledge = paving.getKnowledge(0.0);
         int newTime = (int)(120.0 - knowledge);
         time = Math.max(20, Math.min(100, newTime));
      } else if (item.isAbility()) {
         time = 600;
      }

      return time;
   }

   public static final boolean canReceiveDeedSpeedBonus(Creature performer) {
      Village currentVillage = performer.currentVillage;
      return currentVillage != null
         && currentVillage.isActionAllowed((short)233, performer)
         && (!currentVillage.isAlerted() || !Servers.isThisAPvpServer())
         && (currentVillage.isCitizen(performer) || currentVillage.isAlly(performer) || !Servers.isThisAPvpServer());
   }

   public static final float getRarityTimeModifier(byte rarity) {
      switch(rarity) {
         case 0:
            return 0.0F;
         case 1:
            return 0.1F;
         case 2:
            return 0.3F;
         case 3:
            return 0.5F;
         default:
            return 0.0F;
      }
   }

   public static final int getImproveActionTime(Creature performer, Item source) {
      int time = (int)(150.0F / Servers.localServer.getActionTimer());
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = (int)Math.max(50.0, (double)time * (1.0 - 0.5 * (double)source.getSpellSpeedBonus() / 100.0));
      }

      if (source != null) {
         time = (int)((float)time - getRarityTimeModifier(source.getRarity()) * 10.0F);
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time = (int)((float)time * modifier);
         }
      }

      time = (int)((float)time * getStaminaModiferFor(performer, 20000));
      time = Math.min(65535, time);
      return Math.max(15, time);
   }

   public static final int getRepairActionTime(Creature performer, Skill skill, double bonus) {
      int time = (int)(80.0 * (1.0 + (100.0 - skill.getKnowledge(bonus)) / 100.0));
      time = (int)((float)time * getStaminaModiferFor(performer, 20000));
      time = (int)((float)time / Servers.localServer.getActionTimer());
      if (!Servers.localServer.PVPSERVER || performer.getEnemyPresense() == 0) {
         time /= 2;
      }

      return Math.min(65535, time);
   }

   public static final int getDestroyActionTime(Creature performer, Skill skill, Item source, double bonus) {
      double knowl = 1.0;
      if (source == null) {
         knowl = skill.getKnowledge(0.0);
      } else {
         knowl = skill.getKnowledge(source, 0.0);
      }

      int time = (int)(Math.max(300.0, (100.0 - knowl) * 5.0) / (double)Servers.localServer.getActionTimer());
      if (source != null && source.getSpellSpeedBonus() > 0.0F) {
         time = (int)(Math.max(50.0, (double)time * (1.0 - 0.5 * (double)source.getSpellSpeedBonus() / 100.0)) / (double)Servers.localServer.getActionTimer());
      }

      if (source != null) {
         time = (int)((float)time - getRarityTimeModifier(source.getRarity()));
         if (source.getSpellEffects() != null) {
            float modifier = 1.0F / source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time = (int)((float)time * modifier);
         }
      }

      time = (int)((float)time * getStaminaModiferFor(performer, 20000));
      return Math.min(65535, time);
   }

   public static final int getItemCreationTime(
      int startTime, Creature performer, Skill primSkill, CreationEntry entry, Item realSource, Item realTarget, boolean isMassProduction
   ) throws NoSuchTemplateException {
      float standardTime = 100.0F / Servers.localServer.getActionTimer();
      float difficultyTimeMod = Math.max(0.9F, ((float)startTime - entry.getDifficultyFor(realTarget, realSource, performer)) / 100.0F);
      float minTime = 60.0F;
      if (isMassProduction) {
         minTime = 50.0F;
      }

      double time = (double)minTime + ((double)standardTime - primSkill.getKnowledge(realSource, 0.0)) * (double)difficultyTimeMod;
      if (realSource != null && realSource.getSpellSpeedBonus() > 0.0F) {
         time *= 1.0 - 0.2 * (double)realSource.getSpellSpeedBonus() / 100.0;
      } else if (realTarget != null && realTarget.getSpellSpeedBonus() > 0.0F) {
         time *= 1.0 - 0.2 * (double)realTarget.getSpellSpeedBonus() / 100.0;
      }

      if (realTarget != null) {
         time -= (double)getRarityTimeModifier(realTarget.getRarity());
         if (realTarget.getSpellEffects() != null) {
            float modifier = 1.0F / realTarget.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      if (realSource != null) {
         time -= (double)getRarityTimeModifier(realSource.getRarity());
         if (realSource.getSpellEffects() != null) {
            float modifier = 1.0F / realSource.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      time *= (double)getStaminaModiferFor(performer, 20000);
      time = Math.min(65535.0, time);
      time = Math.max(15.0, time);
      return (int)time;
   }

   public static final int getRecipeCreationTime(
      int startTime, Creature performer, Skill primSkill, Recipe entry, Item realSource, Item realTarget, boolean isMassProduction
   ) throws NoSuchTemplateException {
      float standardTime = 100.0F / Servers.localServer.getActionTimer();
      float difficultyTimeMod = Math.max(0.9F, ((float)startTime - entry.getChanceFor(realSource, realTarget, performer)) / 100.0F);
      float minTime = 30.0F;
      if (isMassProduction) {
         minTime = 25.0F;
      }

      double time = (double)minTime + ((double)standardTime - primSkill.getKnowledge(realSource, 0.0)) * (double)difficultyTimeMod;
      if (realSource != null && realSource.getSpellSpeedBonus() > 0.0F) {
         time *= 1.0 - 0.2 * (double)realSource.getSpellSpeedBonus() / 100.0;
      } else if (realTarget != null && realTarget.getSpellSpeedBonus() > 0.0F) {
         time *= 1.0 - 0.2 * (double)realTarget.getSpellSpeedBonus() / 100.0;
      }

      if (realTarget != null) {
         time -= (double)getRarityTimeModifier(realTarget.getRarity());
         if (realTarget.getSpellEffects() != null) {
            float modifier = 1.0F / realTarget.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      if (realSource != null) {
         time -= (double)getRarityTimeModifier(realSource.getRarity());
         if (realSource.getSpellEffects() != null) {
            float modifier = 1.0F / realSource.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_USESPEED);
            time *= (double)modifier;
         }
      }

      time *= (double)getStaminaModiferFor(performer, 20000);
      time = Math.min(65535.0, time);
      time = Math.max(15.0, time);
      return (int)time;
   }

   public static final boolean isMultipleRunEpicAction(int action) {
      return action == 491 || action == 492 || action == 148 || action == 142 || action == 47;
   }

   public static final short getCorrectActionByTargetType(short action, long targetType) {
      switch(action) {
         case 162:
         case 193:
            return getCorrectRepairActionByTargetType(targetType);
         default:
            return action;
      }
   }

   public static final short getCorrectRepairActionByTargetType(long targetType) {
      if (targetType == 5L || targetType == 23L || targetType == 7L) {
         return 193;
      } else {
         return (short)(targetType == 2L ? 162 : 162);
      }
   }

   public static final boolean isMultipleItemAction(short action, long[] targets) {
      for(int x = 0; x < targets.length; ++x) {
         if (WurmId.getType(targets[x]) != 2) {
            return false;
         }
      }

      return action == 7 || action == 638 || action == 59 || action == 86;
   }

   public static final boolean isActionBrand(short action) {
      return action == 484 || action == 643;
   }

   public static final boolean isActionBreed(short action) {
      return action == 379;
   }

   public static final boolean isActionGroom(short action) {
      return action == 398;
   }

   public static final boolean isActionBuildFence(short action) {
      return action == 166
         || action == 168
         || action == 520
         || action == 528
         || action == 165
         || action == 167
         || action == 543
         || action == 544
         || action == 526
         || action == 527
         || action == 529
         || action == 478
         || action == 163
         || action == 164
         || action == 541
         || action == 542
         || action == 477
         || action == 479
         || action == 545
         || action == 546
         || action == 611
         || action == 654
         || action == 832
         || action == 833
         || action == 834
         || action == 835
         || action == 836
         || action == 837
         || action == 838
         || action == 839
         || action == 840
         || action == 841
         || action == 842
         || action == 843
         || action == 844
         || action == 845
         || action == 846
         || action == 870
         || action == 871
         || action == 872
         || action == 873
         || action == 874
         || action == 875
         || action == 876
         || action == 877
         || action == 878
         || action == 879
         || action == 880
         || action == 881
         || action == 882
         || action == 883
         || action == 884
         || action == 885
         || action == 886
         || action == 887
         || action == 888
         || action == 889
         || action == 890
         || action == 891
         || action == 892
         || action == 893
         || action == 894
         || action == 895
         || action == 896
         || action == 897
         || action == 898
         || action == 899
         || action == 900
         || action == 901
         || action == 902
         || action == 903
         || action == 904
         || action == 905;
   }

   public static final boolean isActionBuild(short action) {
      return action == 116
         || action == 231
         || action == 232
         || action == 363
         || action == 169
         || action == 170
         || action == 516
         || action == 517
         || action == 521
         || isActionBuildFence(action);
   }

   public static final boolean isActionButcher(short action) {
      return action == 120;
   }

   public static final boolean isActionChop(short action) {
      return action == 96 || action == 468;
   }

   public static final boolean isActionCultivate(short action) {
      return action == 318;
   }

   public static final boolean isActionDestroy(int action) {
      return action == 171
         || action == 524
         || action == 174
         || action == 175
         || action == 172
         || action == 173
         || action == 82
         || action == 57
         || action == 757;
   }

   public static final boolean isActionDestroyFence(short action) {
      return action == 171 || action == 172 || action == 173;
   }

   public static final boolean isActionDestroyItem(short action) {
      return action == 83 || action == 757;
   }

   public static final boolean isActionDietySpell(short action) {
      return action == 245;
   }

   public static final boolean isActionDig(short action) {
      return action == 144 || action == 362 || action == 921 || action == 927;
   }

   public static final boolean isActionDrop(short action) {
      return action == 7;
   }

   public static final boolean isActionPlantItem(short action) {
      return action == 176 || action == 746 || action == 747;
   }

   public static final boolean isActionFarm(short action) {
      return action == 151;
   }

   public static final boolean isActionForageBotanizeInvestigate(short action) {
      return action == 223
         || action == 571
         || action == 570
         || action == 569
         || action == 224
         || action == 573
         || action == 720
         || action == 574
         || action == 575
         || action == 572
         || action == 910
         || action == 935;
   }

   public static final boolean isActionGather(short action) {
      return action == 645;
   }

   public static final boolean isActionPick(short action) {
      return action == 137 || action == 852;
   }

   public static final boolean isActionHarvest(short action) {
      return action == 152;
   }

   public static final boolean isActionImproveOrRepair(short action) {
      return action == 192 || action == 162 || action == 193;
   }

   public static final boolean isActionLead(short action) {
      return action == 106;
   }

   public static final boolean isActionLoad(short action) {
      return action == 605;
   }

   public static final boolean isActionAttachLock(short action) {
      return action == 161;
   }

   public static final boolean isActionChangeBuilding(short action) {
      return action == 683 || action == 847;
   }

   public static final boolean isActionLockPick(short action) {
      return action == 101;
   }

   public static final boolean isActionMeditate(short action) {
      return action == 384;
   }

   public static final boolean isActionMilkOrShear(short action) {
      return action == 345 || action == 646;
   }

   public static final boolean isActionMine(short action) {
      return action == 145 || action == 147 || action == 146;
   }

   public static final boolean isActionMineFloor(short action) {
      return action == 145 || action == 150 || action == 532 || action == 518;
   }

   public static final boolean isActionMineSurface(short action) {
      return action == 145 || action == 518;
   }

   public static final boolean isActionPack(short action) {
      return action == 154;
   }

   public static final boolean isActionPave(short action) {
      return action == 155 || action == 191 || action == 628 || action == 627 || action == 576 || action == 695 || action == 694 || action == 757;
   }

   public static final boolean isActionPickSprout(short action) {
      return action == 187;
   }

   public static final boolean isActionPickupPlanted(short action) {
      return action == 685;
   }

   public static final boolean isActionPlaceMerchants(short action) {
      return action == 85;
   }

   public static final boolean isActionPlaceNPCs(short action) {
      return action == 85 || action == 863 && Features.Feature.WAGONER.isEnabled();
   }

   public static final boolean isActionPlanBuilding(short action) {
      return action == 56 || action == 57 || action == 58;
   }

   public static final boolean isActionPlant(short action) {
      return action == 186;
   }

   public static final boolean isActionPlantCenter(short action) {
      return action == 660;
   }

   public static final boolean isActionPrune(short action) {
      return action == 373;
   }

   public static final boolean isActionPullPushTurn(short action) {
      return action == 99 || action == 181 || action == 696 || action == 697 || action == 177 || action == 178 || action == 926;
   }

   public static final boolean isActionReinforce(short action) {
      return action == 229;
   }

   public static final boolean isActionSacrifice(short action) {
      return action == 142;
   }

   public static final boolean isActionSorcerySpell(short action) {
      return action == 547;
   }

   public static final boolean isActionSow(short action) {
      return action == 153;
   }

   public static final boolean isActionTake(int action) {
      return action == 6 || action == 16 || action == 465 || action == 464 || action == 133;
   }

   public static final boolean isActionTame(short action) {
      return action == 46 || action == 275 || action == 274;
   }

   public static final boolean isActionTerraform(short action, boolean onSurface) {
      if (!onSurface || action != 150 && action != 532) {
         return action == 144 || action == 362 || action == 369 || action == 533 || action == 865 || action == 37 || action == 462 || action == 927;
      } else {
         return true;
      }
   }

   public static final boolean isActionTrim(short action) {
      return action == 644;
   }

   public static final boolean isActionTunnel(short action) {
      return action == 227;
   }

   public static final boolean isActionUnload(short action) {
      return action == 234;
   }

   public static final boolean isActionManage(short action) {
      return action == 664
         || action == 364
         || action == 663
         || action == 668
         || action == 665
         || action == 687
         || action == 669
         || action == 688
         || action == 667
         || action == 863;
   }

   public static final boolean isActionBuildingPermission(short action) {
      return isActionAttachLock(action)
         || isActionBuild(action)
         || isActionDestroy(action)
         || isActionDestroyFence(action)
         || isActionDrop(action)
         || isActionImproveOrRepair(action)
         || isActionLoad(action)
         || isActionTake(action)
         || isActionPickupPlanted(action)
         || isActionPlaceMerchants(action)
         || isActionPlanBuilding(action)
         || isActionUnload(action);
   }

   public static final boolean isActionDisembark(short action) {
      return action == 333 || action == 708;
   }

   public static final boolean isActionAllowedOnVehicle(short action) {
      return action == 118
         || action == 329
         || action == 160
         || action == 362
         || action == 162
         || action == 192
         || action == 148
         || action == 229
         || action == 911
         || action == 912
         || action == 190
         || action == 373
         || action == 187
         || action == 152
         || action == 216
         || action == 285;
   }

   public static final boolean isActionAllowedOnBoat(short action) {
      return action == 532 || action == 150;
   }

   public static final boolean isActionAllowedThroughFloors(short action) {
      return action == 1 || action == 671 || action == 672;
   }

   public static final boolean isDefaultTerraformingAction(short action) {
      return action == 718;
   }

   public static final short getDefaultActionForTarget(long actionItemId, long target) {
      int counterType = WurmId.getType(target);
      switch(counterType) {
         case 2:
            Optional<Item> targetOptional = Items.getItemOptional(target);
            if (targetOptional.isPresent()) {
               Item targetItem = targetOptional.get();
               Optional<Item> actionItem = Items.getItemOptional(actionItemId);
               if (targetItem.getTemplateId() == 385 && actionItem.isPresent()) {
                  return 97;
               }

               if (targetItem.isBoat() && actionItem.isPresent()) {
                  Item tool = actionItem.get();
                  if (tool.getTemplateId() == 581) {
                     return 362;
                  }
               }
            }

            return 0;
         case 3:
            int x = Tiles.decodeTileX(target);
            int y = Tiles.decodeTileY(target);
            int tileId = Server.surfaceMesh.getTile(x, y);
            byte type = Tiles.decodeType(tileId);
            Optional<Item> actionItem = Items.getItemOptional(actionItemId);
            if (type == Tiles.Tile.TILE_ROCK.id) {
               if (actionItem.isPresent()) {
                  Item item = actionItem.get();
                  if (item.getTemplateId() == 20) {
                     return 227;
                  }
               }

               return 0;
            } else {
               if (!Tiles.isTree(type) && !Tiles.isBush(type)) {
                  if (actionItem.isPresent()) {
                     Item actionTool = actionItem.get();
                     if (actionTool.getTemplateId() == 581) {
                        return 362;
                     }

                     if (actionTool.getTemplateId() == 25) {
                        return 144;
                     }
                  }

                  return 0;
               }

               return 96;
            }
         case 17:
            return 145;
         default:
            return 0;
      }
   }

   static {
      createDefaultCreatureActions();
      createDefaultTileActions();
   }
}
