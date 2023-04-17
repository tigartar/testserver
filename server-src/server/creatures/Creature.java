/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector2f;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Team;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionStack;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.Behaviours;
import com.wurmonline.server.behaviours.FishEnums;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.TileFieldBehaviour;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Attack;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.BattleEvent;
import com.wurmonline.server.combat.Battles;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.SpecialMove;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.AttackAction;
import com.wurmonline.server.creatures.BoundBox;
import com.wurmonline.server.creatures.BoxMatrix;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.CreatureCommunicator;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureStatusFactory;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.LongTarget;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.creatures.UsedAttackData;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.creatures.ai.CreaturePathFinder;
import com.wurmonline.server.creatures.ai.CreaturePathFinderAgg;
import com.wurmonline.server.creatures.ai.CreaturePathFinderNPC;
import com.wurmonline.server.creatures.ai.DecisionStack;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Order;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.creatures.ai.scripts.FishAI;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicMissionEnum;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Possessions;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.MovementEntity;
import com.wurmonline.server.players.MusicPlayer;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerKills;
import com.wurmonline.server.players.SpellResistance;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TestQuestion;
import com.wurmonline.server.questions.TraderManagementQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Guard;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.webinterface.WcEpicKarmaCommand;
import com.wurmonline.server.webinterface.WcKillCommand;
import com.wurmonline.server.webinterface.WcTrelloDeaths;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.HiveZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.MovementChecker;
import com.wurmonline.shared.util.MulticolorLineSegment;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Creature
implements ItemTypes,
CounterTypes,
MiscConstants,
CreatureTypes,
TimeConstants,
ProtoConstants,
CombatConstants,
ModifierTypes,
CreatureTemplateIds,
MonetaryConstants,
AttitudeConstants,
PermissionsPlayerList.ISettings,
Comparable<Creature> {
    protected Skills skills;
    private int respawnCounter = 0;
    private static final int NPCRESPAWN = 600;
    protected CreatureStatus status;
    protected HashMap<Integer, SpellResist> spellResistances = new HashMap();
    private long id;
    private static final double skillLost = 0.25;
    public static final double MAX_LEAD_DEPTH = -0.71;
    public long loggerCreature1 = -10L;
    private long loggerCreature2 = -10L;
    public int combatRound = 0;
    public SpecialMove specialMove = null;
    protected boolean isVehicleCommander = false;
    public Creature lastOpponent;
    public int opponentCounter = 0;
    protected boolean _enterVehicle = false;
    protected MountAction mountAction = null;
    public boolean addingAfterTeleport = false;
    private static final Item[] emptyItems = new Item[0];
    protected long linkedTo = -10L;
    private boolean isInDuelRing = false;
    private static final DoubleValueModifier willowMod = new DoubleValueModifier(-0.15f);
    public boolean shouldStandStill = false;
    public byte opportunityAttackCounter = 0;
    private long lastSentToolbelt = 0L;
    protected static final float submergedMinDepth = -5.0f;
    protected static final Logger logger = Logger.getLogger(Creature.class.getName());
    protected CreatureTemplate template;
    protected Vehicle hitchedTo = null;
    protected MusicPlayer musicPlayer;
    private boolean inHostilePerimeter = false;
    private int hugeMoveCounter = 0;
    protected String name = "Noname";
    protected String petName = "";
    protected Possessions possessions;
    protected Communicator communicator;
    private VisionArea visionArea;
    private final Behaviour behaviour;
    protected ActionStack actions;
    private Structure structure;
    public int numattackers;
    public int numattackerslast;
    protected Map<Long, Long> attackers;
    public Creature opponent = null;
    private Set<Long> riders = null;
    private Set<Item> keys;
    protected byte fightlevel = 0;
    protected boolean guest = false;
    protected boolean isTeleporting = false;
    private long startTeleportTime = Long.MIN_VALUE;
    public boolean faithful = true;
    private Door currentDoor = null;
    private float teleportX = -1.0f;
    private float teleportY = -1.0f;
    protected int teleportLayer = 0;
    protected int teleportFloorLevel = 0;
    protected boolean justSpawned = false;
    public String spawnWeapon = "";
    public String spawnArmour = "";
    private LinkedList<int[]> openedTiles;
    private int carriedWeight = 0;
    private static final float DEGS_TO_RADS = (float)Math.PI / 180;
    private TradeHandler tradeHandler;
    public Village citizenVillage;
    public Village currentVillage;
    private Set<Item> itemsTaken = null;
    private Set<Item> itemsDropped = null;
    protected MovementScheme movementScheme;
    protected Battle battle = null;
    private Set<Long> stealthBreakers = null;
    private Set<DoubleValueModifier> visionModifiers;
    private final ConcurrentHashMap<Item, Float> weaponsUsed = new ConcurrentHashMap();
    private final ConcurrentHashMap<AttackAction, UsedAttackData> attackUsed = new ConcurrentHashMap();
    public long lastSavedPos = System.currentTimeMillis() - (long)Server.rand.nextInt(1800000);
    protected byte guardSecondsLeft = 0;
    private byte fightStyle = (byte)2;
    private boolean milked = false;
    private boolean sheared = false;
    private boolean isRiftSummoned = false;
    public long target = -10L;
    public Creature leader = null;
    public long dominator = -10L;
    public float zoneBonus = 0.0f;
    private byte currentDeity = 0;
    public byte fleeCounter = 0;
    public boolean isLit = false;
    private int encumbered = 70000;
    private int moveslow = 40000;
    private int cantmove = 140000;
    private byte tilesMoved = 0;
    private byte pathfindcounter = 0;
    protected Map<Creature, Item> followers = null;
    protected static final Creature[] emptyCreatures = new Creature[0];
    public byte currentKingdom = 0;
    protected short damageCounter = 0;
    private final DoubleValueModifier woundMoveMod = new DoubleValueModifier(7, -0.25);
    public long lastParry = 0L;
    public VolaTile currentTile;
    public int staminaPollCounter = 0;
    private DecisionStack decisions = null;
    private static final float HUNGER_RANGE = 20535.0f;
    public boolean goOffline = false;
    private Item bestLightsource = null;
    private Item bestCompass = null;
    private Item bestToolbelt = null;
    private Item bestBeeSmoker = null;
    private Item bestTackleBox = null;
    public boolean lightSourceChanged = false;
    public boolean lastSentHasCompass = false;
    private CombatHandler combatHandler = null;
    private int pollCounter = 0;
    private static final int secondsBetweenItemPolls = 10800;
    private static final int secondsBetweenTraderCoolingPolls = 600;
    private int heatCheckTick = 0;
    private int mountPollCounter = 10;
    protected int breedCounter = 0;
    private boolean visibleToPlayers = false;
    private boolean forcedBreed = false;
    private boolean hasSpiritStamina = false;
    protected boolean hasSpiritFavorgain = false;
    public boolean hasAddedToAttack = false;
    private static final long LOG_ELAPSED_TIME_THRESHOLD = Constants.lagThreshold;
    private static final boolean DO_MORE_ELAPSED_TIME_MEASUREMENTS = false;
    protected boolean hasSentPoison = false;
    int pathRecalcLength = 0;
    protected boolean isInPvPZone = false;
    protected boolean isInNonPvPZone = false;
    protected boolean isInFogZone = false;
    private static final Set<Long> pantLess = new HashSet<Long>();
    private static final Map<Long, Set<MovementEntity>> illusions = new ConcurrentHashMap<Long, Set<MovementEntity>>();
    protected boolean isInOwnBattleCamp = false;
    private boolean doLavaDamage = false;
    private boolean doAreaDamage = false;
    protected float webArmourModTime = 0.0f;
    private ArrayList<Effect> effects;
    private ServerEntry destination;
    private static CreaturePathFinder pathFinder = new CreaturePathFinder();
    private static CreaturePathFinderAgg pathFinderAgg = new CreaturePathFinderAgg();
    private static CreaturePathFinderNPC pathFinderNPC = new CreaturePathFinderNPC();
    public long vehicle = -10L;
    protected byte seatType = (byte)-1;
    protected int teleports = 0;
    private long lastWaystoneChecked = -10L;
    private boolean checkedHotItemsAfterLogin = false;
    private boolean ignoreSaddleDamage = false;
    private boolean isPlacingItem = false;
    private Item placementItem = null;
    private float[] pendingPlacement = null;
    private GuardTower guardTower = null;
    private int lastSecond = 1;
    static long firstCreature;
    static int pollChecksPer;
    static final int breedPollCounter = 201;
    int breedTick = 0;
    private int lastPolled = Server.rand.nextInt(pollChecksPer);
    private CreatureAIData aiData = null;
    private boolean isPathing = false;
    private boolean setTargetNOID = false;
    private Creature creatureToBlinkTo = null;
    public boolean receivedPath = false;
    private PathTile targetPathTile = null;
    static int totx;
    static int toty;
    static int movesx;
    static int movesy;
    protected static final String NOPATH = "No pathing now";
    float creatureFavor = 100.0f;
    boolean switchv = true;

    public static void shutDownPathFinders() {
        pathFinder.shutDown();
        pathFinderAgg.shutDown();
        pathFinderNPC.shutDown();
    }

    public static final CreaturePathFinder getPF() {
        return pathFinder;
    }

    public static final CreaturePathFinderAgg getPFA() {
        return pathFinderAgg;
    }

    public static final CreaturePathFinderNPC getPFNPC() {
        return pathFinderNPC;
    }

    protected Creature() throws Exception {
        this.behaviour = Behaviours.getInstance().getBehaviour((short)4);
        this.communicator = new CreatureCommunicator(this);
        this.actions = new ActionStack();
        this.movementScheme = new MovementScheme(this);
        this.pollCounter = Server.rand.nextInt(10800);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void checkTrap() {
        boolean trigger;
        Trap trap;
        block8: {
            Iterator<Long> iterator;
            block11: {
                block9: {
                    block10: {
                        if (this.isDead()) return;
                        trap = Trap.getTrap(this.currentTile.tilex, this.currentTile.tiley, this.getLayer());
                        if (this.getPower() >= 3) {
                            if (trap == null) return;
                            this.getCommunicator().sendNormalServerMessage("A " + trap.getName() + " is here.");
                            return;
                        }
                        if (trap == null) return;
                        trigger = false;
                        if (trap.getKingdom() == this.getKingdomId()) break block9;
                        if (this.getKingdomId() != 0 || this.isAggHuman()) break block10;
                        trigger = false;
                        if (this.riders == null || this.riders.size() <= 0) break block8;
                        iterator = this.riders.iterator();
                        break block11;
                    }
                    trigger = true;
                    break block8;
                }
                if (trap.getVillage() <= 0) break block8;
                try {
                    Village vill = Villages.getVillage(trap.getVillage());
                    if (vill.isEnemy(this)) {
                        trigger = true;
                    }
                    break block8;
                }
                catch (NoSuchVillageException noSuchVillageException) {
                    // empty catch block
                }
                break block8;
            }
            while (iterator.hasNext()) {
                Long rider = iterator.next();
                try {
                    Creature rr = Server.getInstance().getCreature(rider);
                    if (rr.getKingdomId() == trap.getKingdom()) continue;
                    trigger = true;
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
            }
        }
        if (!trigger) return;
        trap.doEffect(this, this.currentTile.tilex, this.currentTile.tiley, this.getLayer());
    }

    public void sendDetectTrap(Trap trap) {
        if (trap != null && (float)Server.rand.nextInt(100) < this.getDetectDangerBonus()) {
            this.getCommunicator().sendAlertServerMessage("TRAP!", (byte)4);
        }
    }

    public final void calculateFloorLevel(VolaTile tile, boolean forceAddFloorLayer) {
        this.calculateFloorLevel(tile, forceAddFloorLayer, false);
    }

    public final void calculateFloorLevel(VolaTile tile, boolean forceAddFloorLayer, boolean wasOnBridge) {
        try {
            float oldposz;
            if (tile.getStructure() != null && tile.getStructure().isTypeHouse()) {
                if (this.getFloorLevel() == 0 && !wasOnBridge) {
                    float oldposz2;
                    if (!this.isPlayer() && (double)(oldposz2 = this.getPositionZ()) >= -1.25) {
                        float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()) + (tile.getFloors(-10, 10).length == 0 ? 0.0f : 0.25f);
                        float diffz = newPosz - oldposz2;
                        this.setPositionZ(newPosz);
                        if (this.currentTile != null && this.getVisionArea() != null) {
                            this.moved(0.0f, 0.0f, diffz, 0, 0);
                        }
                    }
                } else {
                    int targetFloorLevel = tile.getDropFloorLevel(this.getFloorLevel());
                    if (targetFloorLevel != this.getFloorLevel()) {
                        if (!this.isPlayer()) {
                            this.pushToFloorLevel(targetFloorLevel);
                        }
                    } else if (forceAddFloorLayer && !this.isPlayer()) {
                        float oldposz3 = this.getPositionZ();
                        float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()) + (tile.getFloors(-10, 10).length == 0 ? 0.0f : 0.25f);
                        float diffz = newPosz - oldposz3;
                        this.setPositionZ(newPosz);
                        if (this.currentTile != null && this.getVisionArea() != null) {
                            this.moved(0.0f, 0.0f, diffz, 0, 0);
                        }
                    }
                }
            } else if (!(tile.getStructure() != null && tile.getStructure().isTypeBridge() || this.getFloorLevel() < 0 || this.isPlayer() || !((oldposz = this.getPositionZ()) >= 0.0f))) {
                float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface());
                float diffz = newPosz - oldposz;
                this.setPositionZ(newPosz);
                if (this.currentTile != null && this.getVisionArea() != null) {
                    this.moved(0.0f, 0.0f, diffz, 0, 0);
                }
            }
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
    }

    @Override
    public int compareTo(Creature otherCreature) {
        return this.getName().compareTo(otherCreature.getName());
    }

    public boolean setNewTile(@Nullable VolaTile newtile, float diffZ, boolean ignoreBridge) {
        if (newtile != null && (this.getTileX() != newtile.tilex || this.getTileY() != newtile.tiley)) {
            logger.log(Level.WARNING, this.getName() + " set to " + newtile.tilex + "," + newtile.tiley + " but at " + this.getTileX() + "," + this.getTileY(), new Exception());
            if (this.currentTile != null) {
                logger.log(Level.WARNING, "old is " + this.currentTile.tilex + "(" + this.getPosX() + "), " + this.currentTile.tiley + "(" + this.getPosY() + "), vehic=" + this.getVehicle());
                if (this.isPlayer()) {
                    ((Player)this).intraTeleport((this.currentTile.tilex << 2) + 2, (this.currentTile.tiley << 2) + 2, this.getPositionZ(), this.getStatus().getRotation(), this.getLayer(), "on wrong tile");
                }
            }
            return false;
        }
        boolean wasInDuelRing = false;
        Set<FocusZone> oldFocusZones = null;
        HiveZone oldHiveZone = null;
        boolean oldHiveClose = false;
        long oldBridgeId = this.getBridgeId();
        if (this.currentTile != null) {
            if (this.isPlayer()) {
                Item ring = Zones.isWithinDuelRing(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
                if (ring != null) {
                    wasInDuelRing = true;
                }
                oldFocusZones = FocusZone.getZonesAt(this.currentTile.tilex, this.currentTile.tiley);
                oldHiveZone = Zones.getHiveZoneAt(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
                if (oldHiveZone != null) {
                    oldHiveClose = oldHiveZone.isClose(this.currentTile.tilex, this.currentTile.tiley);
                }
            }
            if (newtile != null && !this.isDead()) {
                this.currentTile.checkOpportunityAttacks(this);
                if (this.currentTile != null) {
                    int diffX = newtile.tilex - this.currentTile.tilex;
                    int diffY = newtile.tiley - this.currentTile.tiley;
                    if (diffX != 0) {
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley, this.getLayer()));
                    }
                    if (diffY != 0) {
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex, newtile.tiley + diffY, this.getLayer()));
                    }
                    if (diffY != 0 && diffX != 0) {
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley + diffY, this.getLayer()));
                    } else if (diffX != 0) {
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley - 1, this.getLayer()));
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex + diffX, newtile.tiley + 1, this.getLayer()));
                    } else if (diffY != 0) {
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex + 1, newtile.tiley + diffY, this.getLayer()));
                        this.sendDetectTrap(Trap.getTrap(newtile.tilex - 1, newtile.tiley + diffY, this.getLayer()));
                    }
                    if (this.currentTile != newtile) {
                        this.currentTile.removeCreature(this);
                    }
                }
                if (this.isPlayer()) {
                    this.addTileMoved();
                }
            } else {
                this.currentTile.removeCreature(this);
            }
            if (this.currentTile != null && this.isPlayer() && this.currentTile != newtile && this.openedTiles != null) {
                ListIterator openedIterator = this.openedTiles.listIterator();
                while (openedIterator.hasNext()) {
                    int[] opened = (int[])openedIterator.next();
                    if (newtile != null && opened[0] == newtile.getTileX() && opened[1] == newtile.getTileY()) continue;
                    try {
                        this.getCommunicator().sendTileDoor((short)opened[0], (short)opened[1], false);
                        openedIterator.remove();
                        MineDoorPermission md = MineDoorPermission.getPermission((short)opened[0], (short)opened[1]);
                        if (md == null) continue;
                        md.close(this);
                    }
                    catch (IOException md) {}
                }
                if (this.openedTiles.isEmpty()) {
                    this.openedTiles = null;
                }
            }
            if (this.currentTile != null && newtile != null) {
                this.currentTile = newtile;
                this.checkTrap();
                if (this.isDead()) {
                    return false;
                }
                if (!this.isPlayer() && !ignoreBridge) {
                    this.checkBridgeMove(this.currentTile, newtile, diffZ);
                }
            } else if (newtile != null && !ignoreBridge && !this.isPlayer()) {
                this.checkBridgeMove(null, newtile, diffZ);
            }
        }
        this.currentTile = newtile;
        if (this.currentTile != null) {
            if (!this.isRidden()) {
                boolean wasOnBridge = false;
                if (oldBridgeId != -10L && oldBridgeId != this.getBridgeId()) {
                    wasOnBridge = true;
                }
                this.calculateFloorLevel(this.currentTile, false, wasOnBridge);
            }
            Set<FocusZone> newFocusZones = FocusZone.getZonesAt(this.currentTile.tilex, this.currentTile.tiley);
            if (!this.isPlayer()) {
                this.isInPvPZone = false;
                this.isInNonPvPZone = false;
                for (FocusZone fz : newFocusZones) {
                    if (fz.isPvP()) {
                        this.isInPvPZone = true;
                        break;
                    }
                    if (!fz.isNonPvP()) continue;
                    this.isInNonPvPZone = true;
                    break;
                }
                this.tilesMoved = (byte)(this.tilesMoved + 1);
                if (this.tilesMoved >= 10) {
                    if (this.isDominated() || this.isHorse()) {
                        try {
                            this.savePosition(this.currentTile.getZone().getId());
                        }
                        catch (IOException opened) {
                            // empty catch block
                        }
                    }
                    this.tilesMoved = 0;
                }
            }
            if (this.isPlayer()) {
                SimplePopup sp;
                try {
                    this.savePosition(this.currentTile.getZone().getId());
                }
                catch (IOException opened) {
                    // empty catch block
                }
                for (FocusZone fz : newFocusZones) {
                    if (fz.isFog() && !this.isInFogZone) {
                        this.isInFogZone = true;
                        this.getCommunicator().sendSpecificWeather(0.85f);
                    }
                    if (fz.isPvP()) {
                        if (!this.isInPvPZone) {
                            if (!this.isOnPvPServer()) {
                                this.achievement(4);
                                this.getCommunicator().sendAlertServerMessage("You enter the " + fz.getName() + " PvP area. Other players may attack you here.", (byte)4);
                            } else {
                                this.getCommunicator().sendAlertServerMessage("You enter the " + fz.getName() + " area.", (byte)4);
                            }
                            this.sendAttitudeChange();
                        }
                        this.isInPvPZone = true;
                        break;
                    }
                    if (fz.isNonPvP()) {
                        if (!this.isInNonPvPZone) {
                            if (this.isOnPvPServer()) {
                                this.getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " No-PvP area. Other players may no longer attack you here.", (byte)2);
                            } else {
                                this.getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " No-PvP area.", (byte)2);
                            }
                            this.sendAttitudeChange();
                        }
                        this.isInNonPvPZone = true;
                        break;
                    }
                    if (!fz.isName() && !fz.isNamePopup() && !fz.isNoBuild() && !fz.isPremSpawnOnly() || oldFocusZones != null && oldFocusZones.contains(fz)) continue;
                    if (fz.isName() || fz.isNoBuild() || fz.isPremSpawnOnly()) {
                        this.getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " area.", (byte)2);
                        continue;
                    }
                    sp = new SimplePopup(this, "Entering " + fz.getName(), "You enter the " + fz.getName() + " area.", fz.getDescription());
                    sp.sendQuestion();
                }
                if (oldFocusZones != null) {
                    for (FocusZone fz : oldFocusZones) {
                        if (fz.isFog() && (newFocusZones == null || !newFocusZones.contains(fz))) {
                            this.isInFogZone = false;
                            this.getCommunicator().checkSendWeather();
                        }
                        if (fz.isPvP()) {
                            if (newFocusZones != null && newFocusZones.contains(fz)) continue;
                            this.isInPvPZone = false;
                            if (this.isOnPvPServer()) {
                                this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                            } else {
                                this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " PvP area.", (byte)2);
                            }
                            this.sendAttitudeChange();
                            continue;
                        }
                        if (fz.isNonPvP()) {
                            if (newFocusZones != null && newFocusZones.contains(fz)) continue;
                            this.isInNonPvPZone = false;
                            this.sendAttitudeChange();
                            if (this.isOnPvPServer()) {
                                this.getCommunicator().sendAlertServerMessage("You leave the " + fz.getName() + " No-PvP area. Other players may attack you here.", (byte)2);
                                continue;
                            }
                            this.getCommunicator().sendAlertServerMessage("You leave the " + fz.getName() + " No-PvP area.", (byte)2);
                            continue;
                        }
                        if (!fz.isName() && !fz.isNamePopup() && !fz.isNoBuild() && !fz.isPremSpawnOnly() || newFocusZones != null && newFocusZones.contains(fz)) continue;
                        if (fz.isName() || fz.isNoBuild() || fz.isPremSpawnOnly()) {
                            this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                            continue;
                        }
                        sp = new SimplePopup(this, "Leaving " + fz.getName(), "You leave the " + fz.getName() + " area.");
                        sp.sendQuestion();
                    }
                }
                if (!WurmCalendar.isSeasonWinter()) {
                    boolean domestic;
                    boolean newHiveClose;
                    HiveZone newHiveZone = Zones.getHiveZoneAt(this.currentTile.tilex, this.currentTile.tiley, this.isOnSurface());
                    boolean bl = newHiveClose = newHiveZone == null ? false : newHiveZone.isClose(this.currentTile.tilex, this.currentTile.tiley);
                    boolean bl2 = newHiveZone == null ? false : (domestic = newHiveZone.getCurrentHive().getTemplateId() == 1175);
                    if (oldHiveClose && !newHiveClose) {
                        this.getCommunicator().sendSafeServerMessage("The sounds of bees decreases as you move further away from the hive.", domestic ? (byte)0 : 2);
                    }
                    if (oldHiveZone == null && newHiveZone != null) {
                        this.getCommunicator().sendSafeServerMessage("You hear bees, maybe you are getting close to a hive.", domestic ? (byte)0 : 2);
                    } else if (oldHiveZone != null && newHiveZone == null) {
                        this.getCommunicator().sendSafeServerMessage("The sounds of bees disappears in the distance.", oldHiveZone.getCurrentHive().getTemplateId() == 1175 ? (byte)0 : 2);
                    }
                    if (!oldHiveClose && newHiveClose) {
                        if (newHiveZone.getCurrentHive().hasTwoQueens()) {
                            this.getCommunicator().sendSafeServerMessage("The bees noise is getting louder, sounds like there is unusual activity in the hive.", domestic ? (byte)0 : 2);
                        } else {
                            this.getCommunicator().sendSafeServerMessage("The bees noise is getting louder, maybe you are getting closer to their hive.", domestic ? (byte)0 : 2);
                        }
                    }
                }
                this.isInDuelRing = false;
                Item ring = Zones.isWithinDuelRing(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
                if (ring != null) {
                    Kingdom k = Kingdoms.getKingdom(ring.getAuxData());
                    if (k != null) {
                        if (ring.getAuxData() == this.getKingdomId()) {
                            this.isInDuelRing = true;
                        }
                        if (!wasInDuelRing) {
                            this.getCommunicator().sendAlertServerMessage("You enter the duelling area of " + k.getName() + ".", (byte)4);
                            if (this.isInDuelRing) {
                                this.getCommunicator().sendAlertServerMessage("People from your own kingdom may slay you here without penalty.", (byte)4);
                            }
                        }
                    }
                } else if (wasInDuelRing) {
                    this.getCommunicator().sendSafeServerMessage("You leave the duelling area.", (byte)2);
                }
                if (!Servers.localServer.HOMESERVER && this.isOnSurface() && this.getFaith() > 0.0f && (float)Server.rand.nextInt(100) < this.getFaith() && EndGameItems.getArtifactAtTile(this.currentTile.tilex, this.currentTile.tiley) != null && this.getDeity() != null) {
                    this.getCommunicator().sendSafeServerMessage(this.getDeity().name + " urges you to deeply investigate the area!");
                }
            }
            if (this.isPlayer() && !this.currentTile.isTransition && this.getVisionArea() != null && this.getVisionArea().isInitialized()) {
                this.checkOpenMineDoor();
            }
        }
        if (this.currentTile != null) {
            Village lVill;
            this.checkInvisDetection();
            boolean hostilePerimeter = false;
            if (this.isPlayer() && (lVill = Villages.getVillageWithPerimeterAt(this.getTileX(), this.getTileY(), true)) != null && lVill.kingdom == this.getKingdomId() && lVill.isEnemy(this)) {
                if (!this.inHostilePerimeter) {
                    this.getCommunicator().sendAlertServerMessage("You are now within the hostile perimeter of " + lVill.getName() + " and will be attacked by kingdom guards.", (byte)4);
                }
                hostilePerimeter = true;
            }
            if (!hostilePerimeter && this.inHostilePerimeter) {
                this.getCommunicator().sendSafeServerMessage("You are now outside the hostile perimeters.");
                this.inHostilePerimeter = false;
            }
            if (hostilePerimeter) {
                this.inHostilePerimeter = true;
            }
            if (this.isPlayer()) {
                MissionTriggers.activateTriggerPlate(this, this.currentTile.tilex, this.currentTile.tiley, this.getLayer());
            }
        }
        return true;
    }

    public final boolean isInOwnDuelRing() {
        return this.isInDuelRing;
    }

    public final boolean hasOpenedMineDoor(int tilex, int tiley) {
        if (this.openedTiles == null) {
            return false;
        }
        for (int[] openedTile : this.openedTiles) {
            if (openedTile[0] != tilex || openedTile[1] != tiley) continue;
            return true;
        }
        return false;
    }

    public void checkOpenMineDoor() {
        Set<int[]> oldM;
        if (this.currentTile != null && (oldM = Terraforming.getAllMineDoors(this.currentTile.tilex, this.currentTile.tiley)) != null) {
            for (int[] checkedTile : oldM) {
                if (this.hasOpenedMineDoor(checkedTile[0], checkedTile[1])) continue;
                try {
                    boolean ok = false;
                    MineDoorPermission md = MineDoorPermission.getPermission(checkedTile[0], checkedTile[1]);
                    if (md != null) {
                        if (md.mayPass(this)) {
                            ok = true;
                            if (this.isPlayer()) {
                                VolaTile tile = Zones.getOrCreateTile(checkedTile[0], checkedTile[1], true);
                                if (this.getEnemyPresense() > 0 && (tile == null || tile.getVillage() == null)) {
                                    md.setClosingTime(System.currentTimeMillis() + (Servers.isThisAChaosServer() ? 30000L : 120000L));
                                }
                            }
                        } else if (md.isWideOpen()) {
                            ok = true;
                        }
                    }
                    if (!ok) continue;
                    if (this.openedTiles == null) {
                        this.openedTiles = new LinkedList();
                    }
                    this.openedTiles.add(checkedTile);
                    this.getMovementScheme().touchFreeMoveCounter();
                    this.getVisionArea().checkCaves(false);
                    this.getCommunicator().sendTileDoor((short)checkedTile[0], (short)checkedTile[1], true);
                    md.open(this);
                }
                catch (IOException iOException) {}
            }
        }
    }

    public Creature(CreatureTemplate aTemplate) throws Exception {
        this();
        this.template = aTemplate;
        this.getMovementScheme().initalizeModifiersWithTemplate();
        this.name = aTemplate.getName();
        this.skills = aTemplate.getSkills();
    }

    public Item getBestLightsource() {
        return this.bestLightsource;
    }

    public Item getBestCompass() {
        return this.bestCompass;
    }

    public Item getBestToolbelt() {
        return this.bestToolbelt;
    }

    public Item getBestBeeSmoker() {
        return this.bestBeeSmoker;
    }

    public Item getBestTackleBox() {
        return this.bestTackleBox;
    }

    public void setBestLightsource(@Nullable Item item, boolean override) {
        if (override || this.getVisionArea() != null && this.getVisionArea().isInitialized()) {
            this.bestLightsource = item;
            this.lightSourceChanged = true;
        }
    }

    public void setBestCompass(Item item) {
        this.bestCompass = item;
    }

    public void setBestToolbelt(@Nullable Item item) {
        this.bestToolbelt = item;
    }

    public void setBestBeeSmoker(Item item) {
        this.bestBeeSmoker = item;
    }

    public void setBestTackleBox(Item item) {
        this.bestTackleBox = item;
    }

    public void resetCompassLantern() {
        this.bestCompass = null;
        this.bestToolbelt = null;
        if (!(this.bestLightsource == null || this.bestLightsource.isOnFire() && this.bestLightsource.getOwnerId() == this.getWurmId())) {
            this.bestLightsource = null;
            this.lightSourceChanged = true;
        }
        this.bestBeeSmoker = null;
        this.bestTackleBox = null;
    }

    public void pollToolbelt() {
        if (this.bestToolbelt != null && this.lastSentToolbelt != this.bestToolbelt.getWurmId()) {
            this.getCommunicator().sendToolbelt(this.bestToolbelt);
            this.lastSentToolbelt = this.bestToolbelt.getWurmId();
        } else if (this.bestToolbelt == null && this.lastSentToolbelt != 0L) {
            this.getCommunicator().sendToolbelt(this.bestToolbelt);
            this.lastSentToolbelt = 0L;
        }
    }

    public void resetLastSentToolbelt() {
        this.lastSentToolbelt = 0L;
    }

    public void pollCompassLantern() {
        if (!this.lastSentHasCompass) {
            if (this.bestCompass != null) {
                this.getCommunicator().sendCompass(this.bestCompass);
                this.lastSentHasCompass = true;
            }
        } else if (this.bestCompass == null) {
            this.getCommunicator().sendCompass(this.bestCompass);
            this.lastSentHasCompass = false;
        }
        this.pollToolbelt();
        if (this.lightSourceChanged) {
            if (this.bestLightsource != null) {
                if (this.getCurrentTile() != null) {
                    this.getCurrentTile().setHasLightSource(this, this.bestLightsource);
                }
            } else if (this.bestLightsource == null && this.getCurrentTile() != null) {
                this.getCurrentTile().setHasLightSource(this, null);
                this.isLit = false;
            }
            this.lightSourceChanged = false;
        }
    }

    public void mute(boolean mute, String reason, long expiry) {
    }

    public boolean isMute() {
        return false;
    }

    public boolean hasSleepBonus() {
        return false;
    }

    public void setOpponent(@Nullable Creature _opponent) {
        if (_opponent != null && this.target == -10L && !this.isPrey()) {
            this.setTarget(_opponent.getWurmId(), true);
        }
        if (_opponent != null && _opponent.getAttackers() >= _opponent.getMaxGroupAttackSize() && (!_opponent.isPlayer() || this.isPlayer())) {
            return;
        }
        if (this.opponent != _opponent && _opponent != null && this.isPlayer() && _opponent.isPlayer()) {
            this.battle = Battles.getBattleFor(this, _opponent);
            this.battle.addEvent(new BattleEvent(-1, this.getName(), _opponent.getName()));
        }
        this.opponent = _opponent;
        if (this.opponent != null) {
            this.opponent.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(this.opponent));
            if (!this.opponent.equals(this.lastOpponent)) {
                this.resetWeaponsUsed();
                this.resetAttackUsed();
                this.getCombatHandler().setCurrentStance(-1, (byte)0);
                this.lastOpponent = this.opponent;
                this.combatRound = 0;
                if (this.isPlayer() && this.opponent.isPlayer()) {
                    if (this.opponent.getKingdomId() != this.getKingdomId() && this.getKingdomId() != 0) {
                        Kingdom k = Kingdoms.getKingdom(this.getKingdomId());
                        k.lastConfrontationTileX = this.getTileX();
                        k.lastConfrontationTileY = this.getTileY();
                    }
                    if (this.getDeity() != null) {
                        this.getDeity().lastConfrontationTileX = this.getTileX();
                        this.getDeity().lastConfrontationTileY = this.getTileY();
                    }
                }
            }
        } else {
            this.resetWeaponsUsed();
            this.resetAttackUsed();
        }
        this.status.sendStateString();
        if (this.isPlayer()) {
            if (this.opponent == null) {
                this.getCommunicator().sendSpecialMove((short)-1, "N/A");
                this.getCommunicator().sendCombatOptions(CombatHandler.NO_COMBAT_OPTIONS, (short)-1);
                this.getCombatHandler().setSentAttacks(false);
            } else {
                this.getCombatHandler().setSentAttacks(false);
                this.getCombatHandler().calcAttacks(false);
            }
        }
    }

    public boolean mayRaiseFightLevel() {
        if (this.combatRound > 2 && this.fightlevel < 5) {
            if (this.fightlevel == 0) {
                return true;
            }
            if (this.fightlevel == 1) {
                return this.getFightingSkill().getKnowledge(0.0) > 30.0;
            }
            if (this.fightlevel == 2) {
                return this.getBodyControl() > 25.0;
            }
            if (this.fightlevel == 3) {
                return this.getMindSpeed().getKnowledge(0.0) > 25.0;
            }
            if (this.fightlevel == 4) {
                return this.getSoulDepth().getKnowledge(0.0) > 25.0;
            }
        }
        return false;
    }

    public CombatHandler getCombatHandler() {
        if (this.combatHandler == null) {
            this.combatHandler = new CombatHandler(this);
        }
        return this.combatHandler;
    }

    public void removeTarget(long targetId) {
        this.actions.removeTarget(targetId);
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean isLegal() {
        return true;
    }

    public void setLegal(boolean mode) {
    }

    public void setAutofight(boolean mode) {
    }

    public void setFaithMode(boolean mode) {
    }

    public Item getDraggedItem() {
        return this.movementScheme.getDraggedItem();
    }

    public void setDraggedItem(@Nullable Item dragged) {
        this.movementScheme.setDraggedItem(dragged);
    }

    public Door getCurrentDoor() {
        return this.currentDoor;
    }

    public void setCurrentDoor(@Nullable Door door) {
        this.currentDoor = door;
    }

    public Battle getBattle() {
        return this.battle;
    }

    public void setBattle(@Nullable Battle batle) {
        this.battle = batle;
    }

    public void setCitizenVillage(@Nullable Village newVillage) {
        this.citizenVillage = newVillage;
        if (this.citizenVillage != null) {
            this.setVillageSkillModifier(this.citizenVillage.getSkillModifier());
            if (this.citizenVillage.kingdom != this.getKingdomId()) {
                try {
                    this.setKingdomId(this.citizenVillage.kingdom, true);
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
            if (this.isPlayer()) {
                ((Player)this).maybeTriggerAchievement(576, true);
            }
        } else {
            this.setVillageSkillModifier(0.0);
        }
        this.refreshAttitudes();
    }

    public Village getCitizenVillage() {
        return this.citizenVillage;
    }

    public void setFightingStyle(byte style) {
        this.setFightingStyle(style, false);
    }

    public void setFightingStyle(byte style, boolean loading) {
        String mess = "";
        mess = style == 2 ? "You will now fight defensively." : (style == 1 ? "You will now fight aggressively." : "You will now fight normally.");
        if (this.isFighting()) {
            this.getCommunicator().sendCombatNormalMessage(mess);
        } else {
            this.getCommunicator().sendNormalServerMessage(mess);
        }
        this.getCombatHandler().setFightingStyle(style);
        this.fightStyle = style;
        this.getCommunicator().sendFightStyle(this.fightStyle);
        this.status.sendStateString();
        if (!loading) {
            this.saveFightMode(this.fightStyle);
        }
    }

    public void saveFightMode(byte mode) {
    }

    public byte getFightStyle() {
        return this.fightStyle;
    }

    public float getBaseCombatRating() {
        if (this.isPlayer()) {
            return this.template.getBaseCombatRating();
        }
        if (this.getLoyalty() > 0.0f) {
            return (this.isReborn() ? 0.7f : 0.5f) * this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier();
        }
        return this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier();
    }

    public float getBonusCombatRating() {
        return this.template.getBonusCombatRating();
    }

    public final boolean isOkToKillBy(Creature attacker) {
        if (!Servers.localServer.HOMESERVER && !Servers.localServer.isChallengeServer()) {
            return true;
        }
        if (!attacker.isFriendlyKingdom(this.getKingdomId())) {
            return true;
        }
        if (Servers.isThisAChaosServer()) {
            return true;
        }
        if (this.getKingdomTemplateId() == 3) {
            return true;
        }
        if (this.hasAttackedUnmotivated()) {
            return true;
        }
        if (attacker.isDuelOrSpar(this)) {
            return true;
        }
        if (this.getReputation() < 0) {
            return true;
        }
        if (this.isInOwnDuelRing()) {
            return true;
        }
        if (Zones.isWithinDuelRing(this.getTileX(), this.getTileY(), true) != null) {
            return true;
        }
        if (attacker.getCitizenVillage() != null) {
            if (attacker.getCitizenVillage().isEnemy(this.getCitizenVillage())) {
                return true;
            }
            if (Servers.localServer.PVPSERVER) {
                Village v = Villages.getVillageWithPerimeterAt(attacker.getTileX(), attacker.getTileY(), true);
                if (v == attacker.getCitizenVillage() && this.getCurrentVillage() == v) {
                    return true;
                }
                if (attacker.getCitizenVillage().isEnemy(this)) {
                    return true;
                }
                if (attacker.getCitizenVillage().isAlly(this.getCitizenVillage())) {
                    return false;
                }
            }
        }
        return this.isInPvPZone();
    }

    public final boolean isEnemyOnChaos(Creature creature) {
        if (Servers.isThisAChaosServer() && this.isInSameAlliance(creature)) {
            return false;
        }
        return false;
    }

    public final boolean isInSameAlliance(Creature creature) {
        if (this.getCitizenVillage() == null) {
            return false;
        }
        if (creature.getCitizenVillage() == null) {
            return false;
        }
        return this.getCitizenVillage().getAllianceNumber() == creature.getCitizenVillage().getAllianceNumber();
    }

    public boolean hasAttackedUnmotivated() {
        if (this.isDominated() && this.getDominator() != null) {
            return this.getDominator().hasAttackedUnmotivated();
        }
        SpellEffects effs = this.getSpellEffects();
        if (effs == null) {
            return false;
        }
        SpellEffect eff = effs.getSpellEffect((byte)64);
        return eff != null;
    }

    public void setUnmotivatedAttacker() {
        if (this.isNpc()) {
            return;
        }
        if (!Servers.isThisAPvpServer() || !Servers.localServer.HOMESERVER) {
            return;
        }
        if (this.getKingdomTemplateId() != 3) {
            SpellEffect eff;
            SpellEffects effs = this.getSpellEffects();
            if (effs == null) {
                effs = this.createSpellEffects();
            }
            if ((eff = effs.getSpellEffect((byte)64)) == null) {
                Achievements ach;
                this.setVisible(false);
                logger.log(Level.INFO, this.getName() + " set unmotivated attacker at ", new Exception());
                eff = new SpellEffect(this.getWurmId(), 64, 100.0f, (int)(Servers.isThisATestServer() ? 120L : 1800L), 1, 1, true);
                effs.addSpellEffect(eff);
                this.setVisible(true);
                this.getCommunicator().sendAlertServerMessage("You have received the hunted status and may be attacked without penalty for half an hour.");
                if (this.getCitizenVillage() != null) {
                    this.getCitizenVillage().setVillageRep(this.getCitizenVillage().getVillageReputation() + 10);
                }
                if ((ach = Achievements.getAchievementObject(this.getWurmId())) != null && ach.getAchievement(369) != null) {
                    this.achievement(373);
                    this.removeTitle(Titles.Title.Knigt);
                    this.addTitle(Titles.Title.FallenKnight);
                }
            } else {
                eff.setTimeleft(1800);
                this.sendUpdateSpellEffect(eff);
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void addAttacker(Creature creature) {
        block22: {
            block29: {
                block21: {
                    Iterator<Long> iterator;
                    block26: {
                        block23: {
                            Brand brand;
                            block28: {
                                block27: {
                                    block25: {
                                        block24: {
                                            if (this.isDuelOrSpar(creature)) break block22;
                                            if (this.isSpiritGuard() && this.getCitizenVillage() != null && !this.getCitizenVillage().containsTarget(creature)) {
                                                this.getCitizenVillage().addTarget(creature);
                                            }
                                            if (this.attackers == null) {
                                                this.attackers = new HashMap<Long, Long>();
                                            }
                                            if (!creature.isPlayer()) break block23;
                                            if (!this.isInvulnerable()) {
                                                this.setSecondsToLogout(this.getSecondsToLogout());
                                            }
                                            if (!this.isPlayer()) break block24;
                                            if (!this.isOkToKillBy(creature) && !creature.hasBeenAttackedBy(this.getWurmId())) {
                                                creature.setUnmotivatedAttacker();
                                            }
                                            break block21;
                                        }
                                        if (!this.isRidden()) break block25;
                                        if (creature.getCitizenVillage() != null && this.getCurrentVillage() == creature.getCitizenVillage()) break block21;
                                        iterator = this.getRiders().iterator();
                                        break block26;
                                    }
                                    if (this.getHitched() == null) break block27;
                                    if (Servers.localServer.HOMESERVER && (creature.getCitizenVillage() == null || this.getCurrentVillage() != creature.getCitizenVillage()) && !this.getHitched().isCreature()) {
                                        try {
                                            Item i = Items.getItem(this.getHitched().wurmid);
                                            long ownid = i.getLastOwnerId();
                                            try {
                                                byte kingd;
                                                if (ownid != creature.getWurmId() && creature.isFriendlyKingdom(kingd = Players.getInstance().getKingdomForPlayer(ownid)) && !creature.hasBeenAttackedBy(ownid)) {
                                                    creature.setUnmotivatedAttacker();
                                                }
                                                break block21;
                                            }
                                            catch (Exception exception) {}
                                            break block21;
                                        }
                                        catch (NoSuchItemException nsi) {
                                            logger.log(Level.INFO, this.getHitched().wurmid + " no such item:", nsi);
                                        }
                                    }
                                    break block21;
                                }
                                if (!this.isDominated()) break block28;
                                if (!Servers.localServer.HOMESERVER) break block21;
                                this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
                                if (!creature.isFriendlyKingdom(this.getKingdomId()) || creature.hasBeenAttackedBy(this.dominator) || creature.hasBeenAttackedBy(this.getWurmId()) || creature == this.getDominator()) break block21;
                                creature.setUnmotivatedAttacker();
                                break block21;
                            }
                            if (this.getCurrentVillage() == null || !Servers.localServer.HOMESERVER || (brand = Creatures.getInstance().getBrand(this.getWurmId())) == null) break block21;
                            try {
                                Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                                if (this.getCurrentVillage() == villageBrand && creature.getCitizenVillage() != villageBrand && !villageBrand.isEnemy(creature.getCitizenVillage())) {
                                    creature.setUnmotivatedAttacker();
                                }
                                break block21;
                            }
                            catch (NoSuchVillageException nsv) {
                                brand.deleteBrand();
                            }
                            break block21;
                        }
                        if (!creature.hasAddedToAttack) {
                            this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
                        }
                        break block29;
                    }
                    while (iterator.hasNext()) {
                        Long riderLong = iterator.next();
                        try {
                            Creature rider = Server.getInstance().getCreature(riderLong);
                            if (rider == creature) continue;
                            if (!(creature.hasBeenAttackedBy(rider.getWurmId()) || creature.hasBeenAttackedBy(this.getWurmId()) || rider.isOkToKillBy(creature))) {
                                creature.setUnmotivatedAttacker();
                            }
                            rider.addAttacker(creature);
                        }
                        catch (NoSuchCreatureException noSuchCreatureException) {
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {}
                    }
                }
                if (!creature.hasAddedToAttack) {
                    this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
                }
            }
            if (!creature.hasAddedToAttack) {
                ++this.numattackers;
                creature.hasAddedToAttack = true;
            }
        }
    }

    public int getSecondsToLogout() {
        return 300;
    }

    public boolean hasBeenAttackedBy(long _id) {
        if (!this.isPlayer()) {
            return false;
        }
        if (this.attackers == null) {
            return false;
        }
        Long l = _id;
        return this.attackers.keySet().contains(l);
    }

    public long[] getLatestAttackers() {
        if (this.attackers != null && this.attackers.size() > 0) {
            Long[] lKeys = this.attackers.keySet().toArray(new Long[this.attackers.size()]);
            long[] toReturn = new long[lKeys.length];
            for (int x = 0; x < toReturn.length; ++x) {
                toReturn[x] = lKeys[x];
            }
            return toReturn;
        }
        return EMPTY_LONG_PRIMITIVE_ARRAY;
    }

    protected long[] getAttackerIds() {
        if (this.attackers == null) {
            return EMPTY_LONG_PRIMITIVE_ARRAY;
        }
        Long[] longs = this.attackers.keySet().toArray(new Long[this.attackers.size()]);
        long[] ll = new long[longs.length];
        for (int x = 0; x < longs.length; ++x) {
            ll[x] = longs[x];
        }
        return ll;
    }

    public void trimAttackers(boolean delete) {
        if (delete) {
            this.attackers = null;
        } else if (this.attackers != null && this.attackers.size() > 0) {
            Long[] lKeys;
            for (Long lLKey : lKeys = this.attackers.keySet().toArray(new Long[this.attackers.size()])) {
                Long time = this.attackers.get(lLKey);
                if (WurmId.getType(lLKey) == 1) {
                    if (System.currentTimeMillis() - time <= 180000L) continue;
                    this.attackers.remove(lLKey);
                    continue;
                }
                if (System.currentTimeMillis() - time <= 300000L) continue;
                this.attackers.remove(lLKey);
            }
            if (this.attackers.isEmpty()) {
                this.attackers = null;
            }
        }
    }

    public void setMilked(boolean aMilked) {
        this.milked = aMilked;
    }

    public void setSheared(boolean isSheared) {
        this.sheared = isSheared;
    }

    public boolean isMilked() {
        return this.milked;
    }

    public boolean isSheared() {
        return this.sheared;
    }

    public int getAttackers() {
        return this.numattackers;
    }

    public int getLastAttackers() {
        return this.numattackerslast;
    }

    public final boolean hasBeenAttackedWithin(int seconds) {
        if (this.attackers != null) {
            for (Long l : this.attackers.values()) {
                if (System.currentTimeMillis() - l >= (long)(seconds * 1000)) continue;
                return true;
            }
        }
        return false;
    }

    public void setCurrentVillage(Village newVillage) {
        if (this.currentVillage == null) {
            if (newVillage != null) {
                this.getCommunicator().sendNormalServerMessage("You enter " + newVillage.getName() + ".");
                newVillage.checkIfRaiseAlert(this);
                if (this.isPlayer() && this.getHighwayPathDestination().length() > 0 && this.getHighwayPathDestination().equalsIgnoreCase(newVillage.getName())) {
                    this.getCommunicator().sendNormalServerMessage("You have arrived at your destination.");
                    this.setLastWaystoneChecked(-10L);
                    this.setHighwayPath("", null);
                    if (this.isPlayer()) {
                        block2: for (Item waystone : Items.getWaystones()) {
                            VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
                            if (vt == null) continue;
                            for (VirtualZone vz : vt.getWatchers()) {
                                try {
                                    if (vz.getWatcher().getWurmId() != this.getWurmId()) continue;
                                    this.getCommunicator().sendWaystoneData(waystone);
                                    continue block2;
                                }
                                catch (Exception e) {
                                    logger.log(Level.WARNING, e.getMessage(), e);
                                }
                            }
                        }
                    }
                }
                if (this.getLogger() != null) {
                    this.getLogger().log(Level.INFO, this.getName() + " enters " + newVillage.getName() + ".");
                }
            }
        } else if (!this.currentVillage.equals(newVillage)) {
            if (newVillage == null) {
                this.getCommunicator().sendNormalServerMessage("You leave " + this.currentVillage.getName() + ".");
                if (!this.isFighting()) {
                    this.currentVillage.removeTarget(this);
                }
                if (this.getLogger() != null) {
                    this.getLogger().log(Level.INFO, this.getName() + " leaves " + this.currentVillage.getName() + ".");
                }
            }
            if (newVillage != null) {
                this.getCommunicator().sendNormalServerMessage("You enter " + newVillage.getName() + ".");
                newVillage.checkIfRaiseAlert(this);
                if (this.getLogger() != null) {
                    this.getLogger().log(Level.INFO, this.getName() + " enters " + newVillage.getName() + ".");
                }
            }
        }
        this.currentVillage = newVillage;
    }

    public Village getCurrentVillage() {
        return this.currentVillage;
    }

    public boolean isVisible() {
        return this.status.visible;
    }

    public void refreshVisible() {
        if (!this.isVisible()) {
            return;
        }
        this.setVisible(false);
        this.setVisible(true);
    }

    public void setVisible(boolean visible) {
        this.status.visible = visible;
        if (this.getStatus().offline) {
            this.status.visible = false;
        } else {
            block12: {
                int tilex = this.getTileX();
                int tiley = this.getTileY();
                try {
                    Zone zone = Zones.getZone(tilex, tiley, this.isOnSurface());
                    VolaTile tile = zone.getOrCreateTile(tilex, tiley);
                    if (visible) {
                        try {
                            if (!this.isDead()) {
                                tile.makeVisible(this);
                            }
                            break block12;
                        }
                        catch (NoSuchCreatureException nsc) {
                            logger.log(Level.INFO, nsc.getMessage() + " " + this.id + ", " + this.name, nsc);
                        }
                        catch (NoSuchPlayerException nsp) {
                            logger.log(Level.INFO, nsp.getMessage() + " " + this.id + ", " + this.name, nsp);
                        }
                        break block12;
                    }
                    tile.makeInvisible(this);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.INFO, this.getName() + " outside of bounds when going invis.");
                }
            }
            if (this.isPlayer()) {
                if (!this.status.visible) {
                    Players.getInstance().partChannels((Player)this);
                } else {
                    Players.getInstance().joinChannels((Player)this);
                }
            }
            this.status.sendStateString();
        }
    }

    public void calculateZoneBonus(int tilex, int tiley, boolean surfaced) {
        try {
            if (Servers.localServer.HOMESERVER) {
                if (this.currentKingdom == 0) {
                    this.currentKingdom = Servers.localServer.KINGDOM;
                }
            } else {
                this.setCurrentKingdom(this.getCurrentKingdom());
            }
            this.zoneBonus = 0.0f;
            Deity deity = this.getDeity();
            if (deity != null) {
                FaithZone z = Zones.getFaithZone(tilex, tiley, surfaced);
                if (z != null) {
                    if (z.getCurrentRuler() == deity) {
                        if (this.getFaith() > 30.0f) {
                            this.zoneBonus += 10.0f;
                        }
                        if (this.getFaith() > 90.0f) {
                            this.zoneBonus += this.getFaith() - 90.0f;
                        }
                        this.zoneBonus = Features.Feature.NEWDOMAINS.isEnabled() ? (this.zoneBonus += (float)z.getStrengthForTile(tilex, tiley, surfaced) / 2.0f) : (this.zoneBonus += (float)z.getStrength() / 2.0f);
                    } else if ((Features.Feature.NEWDOMAINS.isEnabled() ? z.getStrengthForTile(tilex, tiley, surfaced) : z.getStrength()) == 0 && this.getFaith() >= 90.0f) {
                        this.zoneBonus = 5.0f + this.getFaith() - 90.0f;
                    }
                } else if (this.getFaith() >= 90.0f) {
                    this.zoneBonus = 5.0f + this.getFaith() - 90.0f;
                }
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "No faith zone at " + tilex + "," + tiley + ", surf=" + surfaced);
        }
    }

    public boolean mustChangeTerritory() {
        return false;
    }

    protected byte getLastTaggedKingdom() {
        return this.currentKingdom;
    }

    public void setLastTaggedTerr(byte newKingdom) {
    }

    public void setCurrentKingdom(byte newKingdom) {
        if (this.currentKingdom == 0) {
            if (newKingdom != 0) {
                this.getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(newKingdom) + ".");
                if (Servers.localServer.isChallengeOrEpicServer() && this.getLastTaggedKingdom() != newKingdom) {
                    if (this.mustChangeTerritory()) {
                        this.getCommunicator().sendSafeServerMessage("You feel an energy boost, as if " + this.getDeity().getName() + " turns " + this.getDeity().getHisHerItsString() + " eyes at you.");
                    }
                    this.setLastTaggedTerr(newKingdom);
                }
                if (newKingdom != this.getKingdomId()) {
                    this.achievement(374);
                }
                if (this.musicPlayer != null && this.musicPlayer.isItOkToPlaySong(true)) {
                    if (newKingdom != this.getKingdomTemplateId()) {
                        if (Kingdoms.getKingdomTemplateFor(newKingdom) == 3 && Kingdoms.getKingdomTemplateFor(this.getKingdomId()) != 3) {
                            this.musicPlayer.checkMUSIC_TERRITORYHOTS_SND();
                        } else if (Kingdoms.getKingdomTemplateFor(this.getKingdomId()) == 3) {
                            this.musicPlayer.checkMUSIC_TERRITORYWL_SND();
                        }
                    } else {
                        this.playAnthem();
                    }
                }
            }
        } else if (newKingdom != this.currentKingdom) {
            if (newKingdom == 0) {
                this.getCommunicator().sendNormalServerMessage("You leave " + Kingdoms.getNameFor(this.currentKingdom) + ".");
            }
            if (newKingdom != 0) {
                this.getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(newKingdom) + ".");
                if (this.getPower() <= 0 && this.musicPlayer != null && this.musicPlayer.isItOkToPlaySong(true)) {
                    if (newKingdom != this.getKingdomId()) {
                        long secret;
                        this.achievement(374);
                        if (newKingdom == 3 && this.getKingdomId() != 3) {
                            this.musicPlayer.checkMUSIC_TERRITORYHOTS_SND();
                        } else if (this.getKingdomId() == 3) {
                            this.musicPlayer.checkMUSIC_TERRITORYWL_SND();
                        }
                        Appointments p = King.getCurrentAppointments(newKingdom);
                        if (p != null && (secret = p.getOfficialForId(1500)) > 0L) {
                            try {
                                Creature c = Server.getInstance().getCreature(secret);
                                if (c.getMindLogical().skillCheck(40.0, 0.0, false, 1.0f) > 0.0) {
                                    c.getCommunicator().sendNormalServerMessage("Your informers relay information that " + this.getName() + " has entered your territory.");
                                }
                            }
                            catch (Exception exception) {}
                        }
                    } else {
                        this.playAnthem();
                    }
                }
            }
        }
        this.currentKingdom = newKingdom;
    }

    public void setCurrentDeity(Deity deity) {
        if (deity != null) {
            if (this.currentDeity != deity.number) {
                this.currentDeity = (byte)deity.number;
                this.getCommunicator().sendNormalServerMessage("You feel the presence of " + deity.name + ".");
            }
        } else if (this.currentDeity != 0) {
            this.getCommunicator().sendNormalServerMessage("You no longer feel the presence of " + Deities.getDeity((int)this.currentDeity).name + ".");
            this.currentDeity = 0;
        }
    }

    public Creature(long aId) throws Exception {
        this();
        this.setWurmId(aId, 0.0f, 0.0f, 0.0f, 0);
        this.skills = SkillsFactory.createSkills(aId);
    }

    public final void loadTemplate() {
        this.template = this.status.getTemplate();
        this.getMovementScheme().initalizeModifiersWithTemplate();
        this.breedCounter = (Servers.isThisAPvpServer() ? 900 : 2000) + Server.rand.nextInt(1000);
    }

    public Creature setWurmId(long aId, float posx, float posy, float aRot, int layer) throws Exception {
        this.id = aId;
        this.status = CreatureStatusFactory.createCreatureStatus(this, posx, posy, aRot, layer);
        this.getMovementScheme().setBridgeId(this.getBridgeId());
        return this;
    }

    public void postLoad() throws Exception {
        this.loadSkills();
        if (!this.isDead() && !this.isOffline()) {
            this.createVisionArea();
        }
        if (this.getTemplate().getCreatureAI() != null) {
            this.getTemplate().getCreatureAI().creatureCreated(this);
        }
    }

    public TradeHandler getTradeHandler() {
        if (this.tradeHandler == null) {
            this.tradeHandler = new TradeHandler(this, this.getStatus().getTrade());
        }
        return this.tradeHandler;
    }

    public void endTrade() {
        this.tradeHandler.end();
        this.tradeHandler = null;
    }

    public void addItemTaken(Item item) {
        if (this.itemsTaken == null) {
            this.itemsTaken = new HashSet<Item>();
        }
        this.itemsTaken.add(item);
    }

    public void addItemDropped(Item item) {
        this.checkTheftWarnQuestion();
        if (this.itemsDropped == null) {
            this.itemsDropped = new HashSet<Item>();
        }
        this.itemsDropped.add(item);
    }

    public void addChallengeScore(int type, float scoreAdded) {
    }

    protected void sendItemsTaken() {
        if (this.itemsTaken != null) {
            Integer num;
            PlayerTutorial.firePlayerTrigger(this.getWurmId(), PlayerTutorial.PlayerTrigger.TAKEN_ITEM);
            HashMap<Integer, Integer> diffItems = new HashMap<Integer, Integer>();
            HashMap<String, Integer> foodItems = new HashMap<String, Integer>();
            for (Item item : this.itemsTaken) {
                int nums;
                if (item.isFood()) {
                    String name = item.getName();
                    if (foodItems.containsKey(name)) {
                        num = (Integer)foodItems.get(name);
                        nums = num;
                        foodItems.put(name, ++nums);
                        continue;
                    }
                    foodItems.put(name, 1);
                    continue;
                }
                Integer templateId = item.getTemplateId();
                if (diffItems.containsKey(templateId)) {
                    num = (Integer)diffItems.get(templateId);
                    nums = num;
                    diffItems.put(templateId, ++nums);
                    continue;
                }
                diffItems.put(templateId, 1);
            }
            for (Object key : diffItems.keySet()) {
                try {
                    ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate((Integer)key);
                    num = (Integer)diffItems.get(key);
                    int number = num;
                    if (number == 1) {
                        this.getCommunicator().sendNormalServerMessage("You get " + lTemplate.getNameWithGenus() + ".");
                        if (!this.isVisible()) continue;
                        Server.getInstance().broadCastAction(this.name + " gets " + lTemplate.getNameWithGenus() + ".", this, 5);
                        continue;
                    }
                    this.getCommunicator().sendNormalServerMessage("You get " + StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate.getPlural() + ".");
                    if (!this.isVisible()) continue;
                    Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate.getPlural() + ".", this, 5);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
            for (Object key : foodItems.keySet()) {
                Integer num2 = (Integer)foodItems.get(key);
                int number = num2;
                if (number == 1) {
                    this.getCommunicator().sendNormalServerMessage("You get " + StringUtilities.addGenus((String)key) + ".");
                    if (!this.isVisible()) continue;
                    Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.addGenus((String)key) + ".", this, 5);
                    continue;
                }
                this.getCommunicator().sendNormalServerMessage("You get " + StringUtilities.getWordForNumber(number) + " " + (String)key + ".");
                if (!this.isVisible()) continue;
                Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.getWordForNumber(number) + " " + (String)key + ".", this, 5);
            }
            this.itemsTaken = null;
        }
    }

    public boolean isIgnored(long playerId) {
        return false;
    }

    public void sendItemsDropped() {
        if (this.itemsDropped != null) {
            Integer num;
            HashMap<Integer, Integer> diffItems = new HashMap<Integer, Integer>();
            HashMap<String, Integer> foodItems = new HashMap<String, Integer>();
            for (Item item : this.itemsDropped) {
                int nums;
                if (item.isFood()) {
                    String name = item.getName();
                    if (foodItems.containsKey(name)) {
                        num = (Integer)foodItems.get(name);
                        nums = num;
                        foodItems.put(name, ++nums);
                        continue;
                    }
                    foodItems.put(name, 1);
                    continue;
                }
                Integer templateId = item.getTemplateId();
                if (diffItems.containsKey(templateId)) {
                    num = (Integer)diffItems.get(templateId);
                    nums = num;
                    diffItems.put(templateId, ++nums);
                    continue;
                }
                diffItems.put(templateId, 1);
            }
            for (Object key : diffItems.keySet()) {
                try {
                    ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate((Integer)key);
                    num = (Integer)diffItems.get(key);
                    int number = num;
                    if (number == 1) {
                        this.getCommunicator().sendNormalServerMessage("You drop " + lTemplate.getNameWithGenus() + ".");
                        Server.getInstance().broadCastAction(this.name + " drops " + lTemplate.getNameWithGenus() + ".", this, Math.max(3, lTemplate.getSizeZ() / 10));
                        continue;
                    }
                    this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.getWordForNumber(number) + " " + lTemplate.getPlural() + ".");
                    Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.getWordForNumber(number) + " " + lTemplate.getPlural() + ".", this, 5);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
            for (Object key : foodItems.keySet()) {
                Integer num2 = (Integer)foodItems.get(key);
                int number = num2;
                if (number == 1) {
                    this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.addGenus((String)key) + ".");
                    Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.addGenus((String)key) + ".", this, 5);
                    continue;
                }
                this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.getWordForNumber(number) + " " + (String)key + ".");
                Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.getWordForNumber(number) + " " + (String)key + ".", this, 5);
            }
            this.itemsDropped = null;
        }
    }

    public String getHoverText(@Nonnull Creature watcher) {
        String hoverText = "";
        if (!(watcher.isPlayer() && watcher.hasFlag(57) || this.isPlayer() && this.hasFlag(58) || this.getCitizenVillage() == null || !this.isPlayer())) {
            hoverText = hoverText + this.getCitizenVillage().getCitizen(this.getWurmId()).getRole().getName() + " of " + this.getCitizenVillage().getName();
        }
        return hoverText;
    }

    public String getNameWithGenus() {
        if (this.isUnique() || this.isPlayer()) {
            return this.getName();
        }
        if (this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) != 0) {
            return "the " + this.getName();
        }
        if (this.template.isVowel(this.getName().substring(0, 1))) {
            return "an " + this.getName();
        }
        return "a " + this.getName();
    }

    public void setTrade(@Nullable Trade trade) {
        this.status.setTrade(trade);
    }

    public Trade getTrade() {
        return this.status.getTrade();
    }

    public boolean isTrading() {
        return this.status.isTrading();
    }

    public boolean isLeadable(Creature potentialLeader) {
        if (this.hitchedTo != null) {
            return false;
        }
        if (this.riders != null && this.riders.size() > 0) {
            return false;
        }
        if (this.isDominated()) {
            if (this.getDominator() != null) {
                return this.getDominator().equals(potentialLeader);
            }
            return false;
        }
        return this.template.isLeadable();
    }

    public boolean isOffline() {
        return this.getStatus().offline;
    }

    public boolean isLoggedOut() {
        return false;
    }

    public boolean isStayonline() {
        return this.getStatus().stayOnline;
    }

    public boolean setStayOnline(boolean stayOnline) {
        return this.getStatus().setStayOnline(stayOnline);
    }

    void setOffline(boolean offline) {
        this.getStatus().setOffline(offline);
    }

    public Creature getLeader() {
        return this.leader;
    }

    public void setWounded() {
        this.removeIllusion();
        if (this.damageCounter == 0) {
            this.addWoundMod();
        }
        this.playAnimation("wounded", false);
        this.damageCounter = (short)(30.0f * ItemBonus.getHurtingReductionBonus(this));
        this.setStealth(false);
        this.getStatus().sendStateString();
    }

    private void addWoundMod() {
        this.getMovementScheme().addModifier(this.woundMoveMod);
        if (this.isPlayer()) {
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.WOUNDMOVE, 100000, 100.0f);
        }
    }

    public void removeWoundMod() {
        this.getMovementScheme().removeModifier(this.woundMoveMod);
        if (this.isPlayer()) {
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.WOUNDMOVE);
        }
    }

    public boolean isEncumbered() {
        return this.carriedWeight >= this.encumbered;
    }

    public boolean isMoveSlow() {
        return this.carriedWeight >= this.moveslow;
    }

    public boolean isCantMove() {
        return this.carriedWeight >= this.cantmove;
    }

    public int getMovePenalty() {
        if (this.isMoveSlow()) {
            return 5;
        }
        if (this.isEncumbered()) {
            return 10;
        }
        if (this.isCantMove()) {
            return 20;
        }
        return 0;
    }

    public final int getMoveSlow() {
        return this.moveslow;
    }

    private void setMoveLimits() {
        if (this.getPower() > 1) {
            this.moveslow = Integer.MAX_VALUE;
            this.encumbered = Integer.MAX_VALUE;
            this.cantmove = Integer.MAX_VALUE;
            if (this.movementScheme.stealthMod == null) {
                this.movementScheme.stealthMod = new DoubleValueModifier(-(80.0 - Math.min(79.0, this.getBodyControl())) / 100.0);
            } else {
                this.movementScheme.stealthMod.setModifier(-(80.0 - Math.min(79.0, this.getBodyControl())) / 100.0);
            }
        } else {
            try {
                Skill strength = this.skills.getSkill(102);
                this.moveslow = (int)strength.getKnowledge(0.0) * 2000;
                this.encumbered = (int)strength.getKnowledge(0.0) * 3500;
                this.cantmove = (int)strength.getKnowledge(0.0) * 7000;
                if (this.movementScheme.stealthMod == null) {
                    this.movementScheme.stealthMod = new DoubleValueModifier(-(80.0 - Math.min(79.0, this.getBodyControl())) / 100.0);
                } else {
                    this.movementScheme.stealthMod.setModifier(-(80.0 - Math.min(79.0, this.getBodyControl())) / 100.0);
                }
            }
            catch (NoSuchSkillException nss) {
                logger.log(Level.WARNING, "No strength skill for " + this, nss);
            }
        }
    }

    public void calcBaseMoveMod() {
        if (this.carriedWeight < this.moveslow) {
            this.movementScheme.setEncumbered(false);
            this.movementScheme.setBaseModifier(1.0f);
        } else if (this.carriedWeight >= this.cantmove) {
            this.movementScheme.setEncumbered(true);
            this.movementScheme.setBaseModifier(0.05f);
            this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
        } else if (this.carriedWeight >= this.encumbered) {
            this.movementScheme.setEncumbered(false);
            this.movementScheme.setBaseModifier(0.25f);
        } else if (this.carriedWeight >= this.moveslow) {
            this.movementScheme.setEncumbered(false);
            this.movementScheme.setBaseModifier(0.75f);
        }
    }

    public void addCarriedWeight(int weight) {
        boolean canTriggerPlonk = false;
        if (this.isPlayer()) {
            Creature c;
            if (this.carriedWeight < this.moveslow) {
                if (this.carriedWeight + weight >= this.cantmove) {
                    this.movementScheme.setEncumbered(true);
                    this.movementScheme.setBaseModifier(0.05f);
                    this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
                    canTriggerPlonk = true;
                } else if (this.carriedWeight + weight >= this.encumbered) {
                    this.movementScheme.setBaseModifier(0.25f);
                    canTriggerPlonk = true;
                } else if (this.carriedWeight + weight >= this.moveslow) {
                    this.movementScheme.setBaseModifier(0.75f);
                    canTriggerPlonk = true;
                }
            } else if (this.carriedWeight < this.encumbered) {
                if (this.carriedWeight + weight >= this.cantmove) {
                    this.movementScheme.setEncumbered(true);
                    this.movementScheme.setBaseModifier(0.05f);
                    this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
                    canTriggerPlonk = true;
                } else if (this.carriedWeight + weight >= this.encumbered) {
                    this.movementScheme.setBaseModifier(0.25f);
                    canTriggerPlonk = true;
                }
            } else if (this.carriedWeight < this.cantmove && this.carriedWeight + weight >= this.cantmove) {
                this.movementScheme.setEncumbered(true);
                this.movementScheme.setBaseModifier(0.05f);
                this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
                canTriggerPlonk = true;
            }
            if (canTriggerPlonk && !PlonkData.ENCUMBERED.hasSeenThis(this)) {
                PlonkData.ENCUMBERED.trigger(this);
            }
            this.carriedWeight += weight;
            if (this.getVehicle() != -10L && (c = Creatures.getInstance().getCreatureOrNull(this.getVehicle())) != null) {
                c.ignoreSaddleDamage = true;
                c.getMovementScheme().update();
            }
        } else {
            this.carriedWeight += weight;
            this.ignoreSaddleDamage = true;
            this.movementScheme.update();
        }
    }

    public boolean removeCarriedWeight(int weight) {
        if (this.isPlayer()) {
            Creature c;
            if (this.carriedWeight >= this.cantmove) {
                if (this.carriedWeight - weight < this.moveslow) {
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(1.0f);
                    this.getCommunicator().sendAlertServerMessage("You can now move again.");
                } else if (this.carriedWeight - weight < this.encumbered) {
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(0.75f);
                    this.getCommunicator().sendAlertServerMessage("You can now move again.");
                } else if (this.carriedWeight - weight < this.cantmove) {
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(0.25f);
                    this.getCommunicator().sendAlertServerMessage("You can now move again.");
                }
            } else if (this.carriedWeight >= this.encumbered) {
                if (this.carriedWeight - weight < this.moveslow) {
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(1.0f);
                } else if (this.carriedWeight - weight < this.encumbered) {
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(0.75f);
                }
            } else if (this.carriedWeight >= this.moveslow && this.carriedWeight - weight < this.moveslow) {
                this.movementScheme.setEncumbered(false);
                this.movementScheme.setBaseModifier(1.0f);
            }
            this.carriedWeight -= weight;
            if (this.getVehicle() != -10L && (c = Creatures.getInstance().getCreatureOrNull(this.getVehicle())) != null) {
                c.ignoreSaddleDamage = true;
                c.getMovementScheme().update();
            }
        } else {
            this.carriedWeight -= weight;
            this.ignoreSaddleDamage = true;
            this.movementScheme.update();
        }
        if (this.carriedWeight < 0) {
            logger.log(Level.WARNING, "Carried weight is less than 0 for " + this);
            if (this instanceof Player) {
                logger.log(Level.INFO, this.name + " now carries " + this.carriedWeight + " AFTER removing " + weight + " gs. Modifier is:" + this.movementScheme.getSpeedModifier() + ".");
            }
            return false;
        }
        return true;
    }

    public boolean canCarry(int weight) {
        try {
            if (this.getPower() > 1) {
                return true;
            }
            Skill strength = this.skills.getSkill(102);
            return strength.getKnowledge(0.0) * 7000.0 > (double)(weight + this.carriedWeight);
        }
        catch (NoSuchSkillException nss) {
            logger.log(Level.WARNING, "No strength skill for " + this);
            return false;
        }
    }

    public int getCarryCapacityFor(int weight) {
        try {
            Skill strength = this.skills.getSkill(102);
            return (int)(strength.getKnowledge(0.0) * 7000.0 - (double)this.carriedWeight) / weight;
        }
        catch (NoSuchSkillException nss) {
            logger.log(Level.WARNING, "No strength skill for " + this);
            return 0;
        }
    }

    public int getCarriedWeight() {
        return this.carriedWeight;
    }

    public int getSaddleBagsCarriedWeight() {
        for (Item i : this.getBody().getAllItems()) {
            if (!i.isSaddleBags()) continue;
            float mod = 0.5f;
            if (i.getTemplateId() == 1334) {
                mod = 0.6f;
            }
            return (int)((float)i.getFullWeight() * mod);
        }
        return 0;
    }

    public int getCarryingCapacityLeft() {
        try {
            Skill strength = this.skills.getSkill(102);
            return (int)(strength.getKnowledge(0.0) * 7000.0) - this.carriedWeight;
        }
        catch (NoSuchSkillException nss) {
            logger.log(Level.WARNING, "No strength skill for " + this);
            return 0;
        }
    }

    public void setTeleportPoints(short x, short y, int layer, int floorLevel) {
        this.setTeleportPoints((float)(x << 2) + 2.0f, (float)(y << 2) + 2.0f, layer, floorLevel);
    }

    public void setTeleportPoints(float x, float y, int layer, int floorLevel) {
        this.teleportX = x;
        this.teleportY = y;
        this.teleportLayer = layer;
        this.teleportFloorLevel = floorLevel;
    }

    public void setTeleportLayer(int layer) {
        this.teleportLayer = layer;
    }

    public void setTeleportFloorLevel(int floorLevel) {
        this.teleportFloorLevel = floorLevel;
    }

    public int getTeleportLayer() {
        return this.teleportLayer;
    }

    public int getTeleportFloorLevel() {
        return this.teleportFloorLevel;
    }

    public VolaTile getCurrentTile() {
        if (this.currentTile != null) {
            return this.currentTile;
        }
        if (this.status != null) {
            int tilex = this.getTileX();
            int tiley = this.getTileY();
            try {
                Zone zone = Zones.getZone(tilex, tiley, this.isOnSurface());
                this.currentTile = zone.getOrCreateTile(tilex, tiley);
                return this.currentTile;
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
        }
        return null;
    }

    public int getCurrentTileNum() {
        int tilex = this.getTileX();
        int tiley = this.getTileY();
        if (this.isOnSurface()) {
            return Server.surfaceMesh.getTile(tilex, tiley);
        }
        return Server.caveMesh.getTile(tilex, tiley);
    }

    public void addItemsToTrade() {
        if (this.isTrader()) {
            this.getTradeHandler().addItemsToTrade();
        }
    }

    public boolean startTeleporting() {
        this.disembark(false);
        return this.startTeleporting(false);
    }

    public float getTeleportX() {
        return this.teleportX;
    }

    public float getTeleportY() {
        return this.teleportY;
    }

    public void startTrading() {
    }

    public boolean shouldStopTrading(boolean firstCall) {
        if (this.isTrading()) {
            if (this.getTrade().creatureOne != null && this.getTrade().creatureOne.isPlayer() && this.getTrade().creatureOne.shouldStopTrading(false)) {
                this.getTrade().creatureOne.getCommunicator().sendAlertServerMessage("You took too long to trade and " + this.getName() + " takes care of another customer.");
                this.getTrade().end(this, false);
                return true;
            }
            if (this.getTrade().creatureTwo != null && this.getTrade().creatureTwo.isPlayer() && this.getTrade().creatureTwo.shouldStopTrading(false)) {
                this.getTrade().creatureTwo.getCommunicator().sendAlertServerMessage("You took too long to trade and " + this.getName() + " takes care of another customer.");
                this.getTrade().end(this, false);
                return true;
            }
        }
        return false;
    }

    public boolean startTeleporting(boolean enterVehicle) {
        if (this.teleportLayer < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)this.teleportX >> 2, (int)this.teleportY >> 2)))) {
            this.getCommunicator().sendAlertServerMessage("The teleportation target is in rock!");
            return false;
        }
        this.stopLeading();
        this._enterVehicle = enterVehicle;
        if (!enterVehicle) {
            Creatures.getInstance().setCreatureDead(this);
            Players.getInstance().setCreatureDead(this);
        }
        this.startTeleportTime = System.currentTimeMillis();
        this.communicator.setReady(false);
        if (this.status.isTrading()) {
            this.status.getTrade().end(this, false);
        }
        if (this.movementScheme.draggedItem != null) {
            MethodsItems.stopDragging(this, this.movementScheme.draggedItem);
        }
        int tileX = this.getTileX();
        int tileY = this.getTileY();
        try {
            this.destroyVisionArea();
            if (!this.isDead()) {
                Zone zone = Zones.getZone(tileX, tileY, this.isOnSurface());
                zone.deleteCreature(this, true);
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + tileX + ", " + tileY);
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, this + " creature doesn't exist?", nsc);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, this + " player doesn't exist?", nsp);
        }
        this.status.setPositionX(this.teleportX);
        this.status.setPositionY(this.teleportY);
        try {
            this.status.setLayer(this.teleportLayer >= 0 ? 0 : -1);
            boolean setOffZ = false;
            if (this.mountAction != null) {
                setOffZ = true;
            }
            if (setOffZ) {
                this.status.setPositionZ(Math.max(Zones.calculateHeight(this.teleportX, this.teleportY, this.isOnSurface()) + this.mountAction.getOffZ(), this.mountAction.getOffZ()));
                this.getMovementScheme().offZ = this.mountAction.getOffZ();
            } else {
                float height;
                VolaTile targetTile = Zones.getTileOrNull((int)(this.teleportX / 4.0f), (int)(this.teleportY / 4.0f), this.teleportLayer >= 0);
                float f = height = this.teleportFloorLevel > 0 ? (float)(this.teleportFloorLevel * 3) : 0.0f;
                if (targetTile != null) {
                    this.getMovementScheme().setGroundOffset((int)(height * 10.0f), true);
                    this.calculateFloorLevel(targetTile, true);
                }
                this.status.setPositionZ(Zones.calculateHeight(this.teleportX, this.teleportY, this.isOnSurface()) + height);
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + this.teleportX + ", " + this.teleportY);
        }
        this.getMovementScheme().setPosition(this.teleportX, this.teleportY, this.status.getPositionZ(), this.status.getRotation(), this.getLayer());
        this.getMovementScheme().haltSpeedModifier();
        boolean zoneExists = true;
        try {
            this.status.savePosition(this.getWurmId(), this.isPlayer(), Zones.getZoneIdFor((int)this.teleportX >> 2, (int)this.teleportY >> 2, this.isOnSurface()), true);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.INFO, this.getName() + " no zone at " + ((int)this.teleportX >> 2) + ", " + ((int)this.teleportY >> 2) + ", surf=" + this.isOnSurface());
            zoneExists = false;
        }
        try {
            if (zoneExists) {
                Zones.getZone((int)this.teleportX >> 2, (int)this.teleportY >> 2, this.isOnSurface()).addCreature(this.id);
            }
            Server.getInstance().addCreatureToPort(this);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.getName() + " failed to recreate vision area after teleporting: " + ex.getMessage());
        }
        return true;
    }

    public long getPlayingTime() {
        return System.currentTimeMillis();
    }

    public void teleport() {
        this.teleport(true);
    }

    public void teleport(boolean destroyVisionArea) {
        this.communicator.setReady(true);
        if (destroyVisionArea) {
            try {
                Zone newzone = Zones.getZone(this.getTileX(), this.getTileY(), this.isOnSurface());
                this.addingAfterTeleport = true;
                newzone.addCreature(this.id);
                this.sendActionControl("", false, 0);
                try {
                    this.createVisionArea();
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to create visionArea:" + ex.getMessage(), ex);
                }
                Server.getInstance().addCreatureToPort(this);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + this.getTileX() + ", " + this.getTileY());
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, "This creature doesn't exist?", nsc);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "This player doesn't exist?", nsp);
            }
        }
        this.addingAfterTeleport = false;
        this.stopTeleporting();
    }

    public void cancelTeleport() {
        this.teleportX = -1.0f;
        this.teleportY = -1.0f;
        this.teleportLayer = 0;
        this.startTeleportTime = Long.MIN_VALUE;
    }

    public void sendMountData() {
        if (this._enterVehicle) {
            if (this.mountAction != null) {
                this.mountAction.sendData(this);
                MountTransfer mt = MountTransfer.getTransferFor(this.getWurmId());
                if (mt != null) {
                    mt.remove(this.getWurmId());
                }
            }
            this.setMountAction(null);
        }
    }

    public void stopTeleporting() {
        if (this.isTeleporting()) {
            this.teleportX = -1.0f;
            this.teleportY = -1.0f;
            this.teleportLayer = 0;
            this.startTeleportTime = Long.MIN_VALUE;
            if (!this._enterVehicle) {
                this.getMovementScheme().setMooredMod(false);
                this.getMovementScheme().addWindImpact((byte)0);
                this.disembark(false);
                this.setMountAction(null);
                this.calcBaseMoveMod();
            }
            if (this.isPlayer()) {
                ((Player)this).sentClimbing = 0L;
                ((Player)this).sentMountSpeed = 0L;
                ((Player)this).sentWind = 0L;
                if (!this._enterVehicle) {
                    try {
                        if (this.getLayer() >= 0) {
                            this.getVisionArea().getSurface().checkIfEnemyIsPresent(false);
                        } else {
                            this.getVisionArea().getUnderGround().checkIfEnemyIsPresent(false);
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            this._enterVehicle = false;
            if (!this.getCommunicator().stillLoggingIn() || !this.isPlayer()) {
                this.setTeleporting(false);
            }
            if (this.justSpawned) {
                this.justSpawned = false;
            }
        }
    }

    public boolean isWithinTeleportTime() {
        return System.currentTimeMillis() - this.startTeleportTime < 30000L;
    }

    public final boolean isTeleporting() {
        return this.isTeleporting;
    }

    public final void setTeleporting(boolean teleporting) {
        this.isTeleporting = teleporting;
    }

    public Body getBody() {
        return this.status.getBody();
    }

    public String examine() {
        return this.template.examine();
    }

    public void setSpam(boolean spam) {
    }

    public boolean spamMode() {
        return false;
    }

    public byte getSex() {
        if (this.status.getSex() == 127) {
            return this.template.getSex();
        }
        return this.status.getSex();
    }

    public boolean setSex(byte sex, boolean creation) {
        this.status.setSex(sex);
        if (!creation && this.currentTile != null) {
            this.refreshVisible();
        }
        return true;
    }

    public final void spawnFreeItems() {
        if (Features.Feature.FREE_ITEMS.isEnabled()) {
            block25: {
                if (this.spawnWeapon != null && this.spawnWeapon.length() > 0) {
                    TestQuestion.createAndInsertItems(this, 319, 319, 40.0f, true, (byte)-1);
                    try {
                        int w = Integer.parseInt(this.spawnWeapon);
                        int lTemplate = 0;
                        boolean shield = false;
                        switch (w) {
                            case 1: {
                                lTemplate = 21;
                                shield = true;
                                break;
                            }
                            case 2: {
                                lTemplate = 81;
                                break;
                            }
                            case 3: {
                                lTemplate = 90;
                                shield = true;
                                break;
                            }
                            case 4: {
                                lTemplate = 87;
                                break;
                            }
                            case 5: {
                                lTemplate = 292;
                                shield = true;
                                break;
                            }
                            case 6: {
                                lTemplate = 290;
                                break;
                            }
                            case 7: {
                                lTemplate = 706;
                                break;
                            }
                            case 8: {
                                lTemplate = 705;
                            }
                        }
                        if (lTemplate <= 0) break block25;
                        try {
                            TestQuestion.createAndInsertItems(this, lTemplate, lTemplate, 40.0f, true, (byte)-1);
                            if (shield) {
                                TestQuestion.createAndInsertItems(this, 84, 84, 40.0f, true, (byte)-1);
                            }
                        }
                        catch (Exception ex) {
                            logger.log(Level.INFO, "Failed to create item for spawning.", ex);
                            this.getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
                        }
                    }
                    catch (Exception ex) {
                        this.getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
                    }
                }
            }
            this.spawnWeapon = null;
            if (this.spawnArmour != null && this.spawnArmour.length() > 0) {
                try {
                    int arm = Integer.parseInt(this.spawnArmour);
                    float ql = 20.0f;
                    int matType = -1;
                    switch (arm) {
                        case 1: {
                            ql = 40.0f;
                            TestQuestion.createAndInsertItems(this, 274, 279, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 278, 278, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 274, 274, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 277, 277, ql, true, (byte)-1);
                            break;
                        }
                        case 2: {
                            ql = 60.0f;
                            TestQuestion.createAndInsertItems(this, 103, 108, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 103, 103, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 105, 105, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 106, 106, ql, true, (byte)-1);
                            break;
                        }
                        case 3: {
                            ql = 20.0f;
                            TestQuestion.createAndInsertItems(this, 280, 287, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 284, 284, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 280, 280, ql, true, (byte)-1);
                            TestQuestion.createAndInsertItems(this, 283, 283, ql, true, (byte)-1);
                        }
                    }
                }
                catch (Exception ex) {
                    this.getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
                }
            }
            this.spawnArmour = null;
        }
    }

    public Communicator getCommunicator() {
        return this.communicator;
    }

    public void setSecondsToLogout(int seconds) {
    }

    public void addKey(Item key, boolean loading) {
        if (this.keys == null) {
            this.keys = new HashSet<Item>();
        }
        if (!this.keys.contains(key)) {
            this.keys.add(key);
            if (!loading) {
                Item[] itemarr = this.getInventory().getAllItems(false);
                if (!this.unlockItems(key, itemarr)) {
                    this.unlockItems(key, this.getBody().getAllItems());
                }
                this.updateGates(key, false);
            }
        }
    }

    public void removeKey(Item key, boolean loading) {
        if (this.keys != null) {
            if (this.keys.remove(key) && !loading) {
                Item[] itemarr = this.getInventory().getAllItems(false);
                if (!this.lockItems(key, itemarr)) {
                    this.lockItems(key, this.getBody().getAllItems());
                }
                this.updateGates(key, true);
            }
            if (this.keys.isEmpty()) {
                this.keys = null;
            }
        }
    }

    public void updateGates(Item key, boolean removedKey) {
        VolaTile t = this.getCurrentTile();
        if (t != null) {
            Door[] doors = t.getDoors();
            if (doors != null) {
                for (Door lDoor : doors) {
                    lDoor.updateDoor(this, key, removedKey);
                }
            }
        } else {
            logger.log(Level.WARNING, this.getName() + " was on null tile.", new Exception());
        }
    }

    public void updateGates() {
        VolaTile t = this.getCurrentTile();
        if (t != null) {
            Door[] doors = t.getDoors();
            if (doors != null) {
                for (Door lDoor : doors) {
                    lDoor.removeCreature(this);
                    if (!lDoor.covers(this.getPosX(), this.getPosY(), this.getPositionZ(), this.getFloorLevel(), this.followsGround())) continue;
                    lDoor.addCreature(this);
                }
            }
        } else {
            logger.log(Level.WARNING, this.getName() + " was on null tile.", new Exception());
        }
    }

    public boolean unlockItems(Item key, Item[] items) {
        for (Item lItem : items) {
            if (!lItem.isLockable() || lItem.getLockId() == -10L) continue;
            try {
                long[] keyarr;
                Item lock = Items.getItem(lItem.getLockId());
                for (long lElement : keyarr = lock.getKeyIds()) {
                    if (lElement != key.getWurmId()) continue;
                    if (!lItem.isEmpty(false)) {
                        if (lItem.getOwnerId() == this.getWurmId()) {
                            this.getCommunicator().sendHasMoreItems(-1L, lItem.getWurmId());
                        } else {
                            this.getCommunicator().sendHasMoreItems(lItem.getTopParent(), lItem.getWurmId());
                        }
                    }
                    return true;
                }
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, nsi.getMessage(), nsi);
            }
        }
        return false;
    }

    public boolean lockItems(Item key, Item[] items) {
        boolean stillUnlocked = false;
        for (Item lItem : items) {
            if (!lItem.isLockable() || lItem.getLockId() == -10L) continue;
            try {
                Item lock = Items.getItem(lItem.getLockId());
                long[] keyarr = lock.getKeyIds();
                boolean thisLock = false;
                for (long lElement : keyarr) {
                    for (Item key2 : this.keys) {
                        if (lElement != key2.getWurmId()) continue;
                        stillUnlocked = true;
                    }
                    if (lElement != key.getWurmId()) continue;
                    thisLock = true;
                }
                if (thisLock && !stillUnlocked) {
                    Set<Item> contItems = lItem.getItems();
                    for (Item item : contItems) {
                        item.removeWatcher(this, true);
                    }
                    return true;
                }
                if (!thisLock) continue;
                return true;
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, nsi.getMessage(), nsi);
            }
        }
        return false;
    }

    public boolean hasKeyForLock(Item lock) {
        long[] keyarr;
        if (lock.getWurmId() == this.getWurmId()) {
            return true;
        }
        if (this.keys == null || this.keys.isEmpty()) {
            return false;
        }
        if (lock.getWurmId() == 5390789413122L && lock.getParentId() == 5390755858690L) {
            boolean ok = true;
            if (!this.hasAbility(Abilities.getAbilityForItem(809, this))) {
                ok = false;
            }
            if (!this.hasAbility(Abilities.getAbilityForItem(808, this))) {
                ok = false;
            }
            if (!this.hasAbility(Abilities.getAbilityForItem(798, this))) {
                ok = false;
            }
            if (!this.hasAbility(Abilities.getAbilityForItem(810, this))) {
                ok = false;
            }
            if (!this.hasAbility(Abilities.getAbilityForItem(807, this))) {
                ok = false;
            }
            if (!ok) {
                this.getCommunicator().sendAlertServerMessage("There is some mysterious enchantment on this lock!");
                return ok;
            }
        }
        for (long lElement : keyarr = lock.getKeyIds()) {
            for (Item key : this.keys) {
                if (lElement != key.getWurmId()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean hasAllKeysForLock(Item lock) {
        for (long aKey : lock.getKeyIds()) {
            boolean foundit = false;
            for (Item key : this.getKeys()) {
                if (aKey != key.getWurmId()) continue;
                foundit = true;
                break;
            }
            if (foundit) continue;
            return false;
        }
        return true;
    }

    public Item[] getKeys() {
        Item[] toReturn = new Item[]{};
        if (this.keys != null) {
            toReturn = this.keys.toArray(new Item[this.keys.size()]);
        }
        return toReturn;
    }

    public boolean isOnSurface() {
        return this.status.isOnSurface();
    }

    public void setLayer(int layer, boolean removeFromTile) {
        if (this.getStatus().getLayer() != layer) {
            if (this.isPlayer() || removeFromTile) {
                if (this.currentTile != null) {
                    Vehicle vehic;
                    if (!(this instanceof Player)) {
                        this.setPositionZ(Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), this.isFloating(), this.getPositionZ(), this, this.getBridgeId()));
                    }
                    this.getStatus().setLayer(layer);
                    if (this.getVehicle() != -10L && this.isVehicleCommander() && (vehic = Vehicles.getVehicleForId(this.getVehicle())) != null) {
                        Seat[] seats;
                        boolean ok;
                        block40: {
                            ok = true;
                            if (vehic.creature) {
                                try {
                                    Creature cretVehicle = Server.getInstance().getCreature(vehic.wurmid);
                                    if (layer < 0) {
                                        int tile = Server.caveMesh.getTile(cretVehicle.getTileX(), cretVehicle.getTileY());
                                        if (!Tiles.isSolidCave(Tiles.decodeType(tile))) {
                                            cretVehicle.setLayer(layer, false);
                                        }
                                        break block40;
                                    }
                                    cretVehicle.setLayer(layer, false);
                                }
                                catch (NoSuchCreatureException nsi) {
                                    logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + nsi.getMessage(), nsi);
                                }
                                catch (NoSuchPlayerException nsp) {
                                    logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + nsp.getMessage(), nsp);
                                }
                            } else {
                                try {
                                    int caveTile;
                                    Item itemVehicle = Items.getItem(vehic.wurmid);
                                    if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(caveTile = Server.caveMesh.getTile((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2)))) {
                                        ok = false;
                                    }
                                    if (!ok) break block40;
                                    itemVehicle.newLayer = (byte)layer;
                                    Zone zone = null;
                                    try {
                                        zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, itemVehicle.isOnSurface());
                                        zone.removeItem(itemVehicle, true, true);
                                    }
                                    catch (NoSuchZoneException nsz) {
                                        logger.log(Level.WARNING, itemVehicle.getName() + " this shouldn't happen: " + nsz.getMessage() + " at " + ((int)itemVehicle.getPosX() >> 2) + ", " + ((int)itemVehicle.getPosY() >> 2), nsz);
                                    }
                                    try {
                                        zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, layer >= 0);
                                        zone.addItem(itemVehicle, false, false, false);
                                    }
                                    catch (NoSuchZoneException nsz) {
                                        logger.log(Level.WARNING, itemVehicle.getName() + " this shouldn't happen: " + nsz.getMessage() + " at " + ((int)itemVehicle.getPosX() >> 2) + ", " + ((int)itemVehicle.getPosY() >> 2), nsz);
                                    }
                                    itemVehicle.newLayer = (byte)-128;
                                    Seat[] seats2 = vehic.hitched;
                                    if (seats2 != null) {
                                        for (int x = 0; x < seats2.length; ++x) {
                                            if (seats2[x] != null) {
                                                if (seats2[x].occupant == -10L) continue;
                                                try {
                                                    Creature c = Server.getInstance().getCreature(seats2[x].occupant);
                                                    c.getStatus().setLayer(layer);
                                                    c.getCurrentTile().newLayer(c);
                                                }
                                                catch (NoSuchPlayerException nsp) {
                                                    logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
                                                }
                                                catch (NoSuchCreatureException nsc) {
                                                    logger.log(Level.WARNING, this.getName() + " " + nsc.getMessage(), nsc);
                                                }
                                                continue;
                                            }
                                            logger.log(Level.WARNING, this.getName() + " " + vehic.name + ": lacking seat " + x, new Exception());
                                        }
                                    }
                                }
                                catch (NoSuchItemException is) {
                                    logger.log(Level.WARNING, this.getName() + " " + is.getMessage(), is);
                                }
                            }
                        }
                        if (ok && (seats = vehic.seats) != null) {
                            for (int x = 0; x < seats.length; ++x) {
                                if (x <= 0) continue;
                                if (seats[x] != null) {
                                    if (seats[x].occupant == -10L) continue;
                                    try {
                                        Creature c = Server.getInstance().getCreature(seats[x].occupant);
                                        c.getStatus().setLayer(layer);
                                        c.getCurrentTile().newLayer(c);
                                        if (!c.isPlayer()) continue;
                                        if (c.isOnSurface()) {
                                            c.getCommunicator().sendNormalServerMessage("You leave the cave.");
                                            continue;
                                        }
                                        c.getCommunicator().sendNormalServerMessage("You enter the cave.");
                                        if (c.getVisionArea() == null) continue;
                                        c.getVisionArea().initializeCaves();
                                    }
                                    catch (NoSuchPlayerException nsp) {
                                        logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
                                    }
                                    catch (NoSuchCreatureException nsc) {
                                        logger.log(Level.WARNING, this.getName() + " " + nsc.getMessage(), nsc);
                                    }
                                    continue;
                                }
                                logger.log(Level.WARNING, this.getName() + " " + vehic.name + ": lacking seat " + x, new Exception());
                            }
                        }
                    }
                    this.currentTile.newLayer(this);
                } else {
                    this.getStatus().setLayer(layer);
                }
                if (this.isPlayer()) {
                    if (layer < 0 && this.getVisionArea() != null) {
                        this.getVisionArea().checkCaves(true);
                    }
                    if (layer < 0) {
                        this.getCommunicator().sendNormalServerMessage("You enter the cave.");
                    } else {
                        this.getCommunicator().sendNormalServerMessage("You leave the cave.");
                    }
                    Village v = Villages.getVillage(this.getTileX(), this.getTileY(), true);
                    if (v != null && v.isEnemy(this)) {
                        Guard[] guards = v.getGuards();
                        for (int gx = 0; gx < guards.length; ++gx) {
                            if (!guards[gx].getCreature().isWithinDistanceTo(this, 20.0f) || !this.visibilityCheck(guards[gx].getCreature(), 0.0f)) continue;
                            v.checkIfRaiseAlert(this);
                            break;
                        }
                    }
                }
            } else {
                this.getStatus().setLayer(layer);
                this.getCurrentTile().newLayer(this);
            }
        }
    }

    public int getLayer() {
        return this.getStatus().getLayer();
    }

    public void setPositionX(float pos) {
        this.status.setPositionX(pos);
    }

    public void setPositionY(float pos) {
        this.status.setPositionY(pos);
    }

    public void setPositionZ(float pos) {
        this.status.setPositionZ(pos);
    }

    public void setRotation(float aRot) {
        this.status.setRotation(aRot);
    }

    public void turnTo(float newRot) {
        this.setRotation(Creature.normalizeAngle(newRot));
        this.moved(0.0f, 0.0f, 0.0f, 0, 0);
    }

    public void turnBy(float turnAmount) {
        this.setRotation(Creature.normalizeAngle(this.status.getRotation() + turnAmount));
        this.moved(0.0f, 0.0f, 0.0f, 0, 0);
    }

    public void submerge() {
        try {
            float lOldPosZ = this.getPositionZ();
            float lNewPosZ = this.isFloating() ? this.template.offZ : Zones.calculateHeight(this.getPosX(), this.getPosY(), true) / 2.0f;
            this.moved(0.0f, 0.0f, lNewPosZ - lOldPosZ, 0, 0);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
    }

    public void surface() {
        float lOldPosZ = this.getPositionZ();
        float lNewPosZ = this.isFloating() ? this.template.offZ : -1.25f;
        this.setPositionZ(lNewPosZ);
        this.moved(0.0f, 0.0f, lNewPosZ - lOldPosZ, 0, 0);
    }

    public void almostSurface() {
        float _oldPosZ = this.getPositionZ();
        float _newPosZ = -2.0f;
        this.setPositionZ(-2.0f);
        this.moved(0.0f, 0.0f, -2.0f - _oldPosZ, 0, 0);
    }

    public void setCommunicator(Communicator comm) {
        this.communicator = comm;
    }

    public void loadPossessions(long inventoryId) throws Exception {
        try {
            this.possessions = new Possessions(this, inventoryId);
        }
        catch (Exception ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
            this.status.createNewPossessions();
        }
    }

    public long createPossessions() throws Exception {
        this.possessions = new Possessions(this);
        return this.possessions.getInventory().getWurmId();
    }

    public Behaviour getBehaviour() {
        return this.behaviour;
    }

    public final boolean hasFightDistanceTo(Creature _target) {
        if (Math.abs(this.getStatus().getPositionX() - _target.getStatus().getPositionX()) > Math.abs(this.getStatus().getPositionY() - _target.getStatus().getPositionY())) {
            return Math.abs(this.getStatus().getPositionX() - _target.getStatus().getPositionX()) < 8.0f;
        }
        return Math.abs(this.getStatus().getPositionY() - _target.getStatus().getPositionY()) < 8.0f;
    }

    public static final int rangeTo(Creature performer, Creature target) {
        if (Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) > Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY())) {
            return (int)Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX());
        }
        return (int)Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY());
    }

    private static final float calcModPosX(double sinRot, double cosRot, float widthCM, float lengthCM) {
        return (float)(cosRot * (double)widthCM - sinRot * (double)lengthCM);
    }

    private static final float calcModPosY(double sinRot, double cosRot, float widthCM, float lengthCM) {
        return (float)((double)widthCM * sinRot + (double)lengthCM * cosRot);
    }

    private static Vector2f rotate(float angle, Vector2f center, Vector2f point) {
        double rads = (double)angle * Math.PI / 180.0;
        Vector2f nPoint = new Vector2f();
        nPoint.x = (float)((double)center.x + (double)(point.x - center.x) * Math.cos(rads) + (double)(point.y - center.y) * Math.sin(rads));
        nPoint.y = (float)((double)center.y - (double)(point.x - center.x) * Math.sin(rads) + (double)(point.y - center.y) * Math.cos(rads));
        return nPoint;
    }

    private static final boolean isLeftOf(Vector2f point, float posX) {
        return posX < point.x;
    }

    private static final boolean isRightOf(Vector2f point, float posX) {
        return posX > point.x;
    }

    private static final boolean isAbove(Vector2f point, float posY) {
        return posY > point.y;
    }

    private static final boolean isBelow(Vector2f point, float posY) {
        return posY < point.y;
    }

    private static final int closestPoint(Vector2f[] points, Vector2f pos, Vector2f[] ignore) {
        boolean canIgnore = ignore != null;
        float min = 10000.0f;
        int index = -1;
        for (int i = 0; i < points.length; ++i) {
            float len;
            if (canIgnore) {
                boolean doIgnore = false;
                for (int x = 0; x < ignore.length; ++x) {
                    if (points[i] != ignore[x]) continue;
                    doIgnore = true;
                }
                if (doIgnore) continue;
            }
            if (!((len = pos.subtract(points[i]).length()) < min)) continue;
            index = i;
            min = len;
        }
        return index;
    }

    public static final float rangeToInDec(Creature performer, Creature target) {
        if (target.getTemplate().hasBoundingBox() && Features.Feature.CREATURE_COMBAT_CHANGES.isEnabled()) {
            float minX = target.getTemplate().getBoundMinX() * target.getStatus().getSizeMod();
            float minY = target.getTemplate().getBoundMinY() * target.getStatus().getSizeMod();
            float maxX = target.getTemplate().getBoundMaxX() * target.getStatus().getSizeMod();
            float maxY = target.getTemplate().getBoundMaxY() * target.getStatus().getSizeMod();
            Vector2f center = new Vector2f(target.getStatus().getPositionX(), target.getStatus().getPositionY());
            float PX = performer.getStatus().getPositionX();
            float PY = performer.getStatus().getPositionY();
            Vector3f cpos = new Vector3f(center.x, center.y, 1.0f);
            float rotation = target.getStatus().getRotation();
            Vector3f mp1 = new Vector3f(minX, minY, 0.0f);
            Vector3f mp2 = new Vector3f(maxX, maxY, 0.0f);
            BoxMatrix M = new BoxMatrix(true);
            BoundBox box = new BoundBox(M, mp1, mp2);
            box.M.translate(cpos);
            box.M.rotate(rotation + 180.0f, false, false, true);
            Vector3f ppos = new Vector3f(PX, PY, 0.5f);
            if (box.isPointInBox(ppos)) {
                return box.distOutside(ppos, cpos) * 10.0f;
            }
            return box.distOutside(ppos, cpos) * 10.0f;
        }
        if (Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) > Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY())) {
            return Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) * 10.0f;
        }
        return Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY()) * 10.0f;
    }

    public static int rangeTo(Creature performer, Item aTarget) {
        if (Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX()) > Math.abs(performer.getStatus().getPositionY() - aTarget.getPosY())) {
            return (int)Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX());
        }
        return (int)Math.abs(performer.getStatus().getPositionY() - aTarget.getPosY());
    }

    public void setAction(Action action) {
        this.actions.addAction(action);
    }

    public ActionStack getActions() {
        return this.actions;
    }

    public Action getCurrentAction() throws NoSuchActionException {
        return this.actions.getCurrentAction();
    }

    public void modifyRanking() {
    }

    public void dropLeadingItem(Item item) {
    }

    public Item getLeadingItem(Creature follower) {
        return null;
    }

    public Creature getFollowedCreature(Item leadingItem) {
        return null;
    }

    public boolean isItemLeading(Item item) {
        return false;
    }

    public void addFollower(Creature follower, @Nullable Item leadingItem) {
        if (this.followers == null) {
            this.followers = new HashMap<Creature, Item>();
        }
        this.followers.put(follower, leadingItem);
    }

    public Creature[] getFollowers() {
        if (this.followers == null || this.followers.size() == 0) {
            return emptyCreatures;
        }
        return this.followers.keySet().toArray(new Creature[this.followers.size()]);
    }

    public final int getNumberOfFollowers() {
        if (this.followers == null) {
            return 0;
        }
        return this.followers.size();
    }

    public void stopLeading() {
        if (this.followers != null) {
            Creature[] followArr;
            for (Creature lElement : followArr = this.followers.keySet().toArray(new Creature[this.followers.size()])) {
                lElement.setLeader(null);
            }
            this.followers = null;
        }
    }

    public boolean mayLeadMoreCreatures() {
        return this.followers == null || this.followers.size() < 10;
    }

    public final boolean isLeading(Creature checked) {
        for (Creature c : this.getFollowers()) {
            for (Creature c2 : this.getFollowers()) {
                for (Creature c3 : this.getFollowers()) {
                    for (Creature c4 : this.getFollowers()) {
                        for (Creature c5 : this.getFollowers()) {
                            for (Creature c6 : this.getFollowers()) {
                                for (Creature c7 : this.getFollowers()) {
                                    if (c7.getWurmId() != checked.getWurmId()) continue;
                                    return true;
                                }
                                if (c6.getWurmId() != checked.getWurmId()) continue;
                                return true;
                            }
                            if (c5.getWurmId() != checked.getWurmId()) continue;
                            return true;
                        }
                        if (c4.getWurmId() != checked.getWurmId()) continue;
                        return true;
                    }
                    if (c3.getWurmId() != checked.getWurmId()) continue;
                    return true;
                }
                if (c2.getWurmId() != checked.getWurmId()) continue;
                return true;
            }
            if (c.getWurmId() != checked.getWurmId()) continue;
            return true;
        }
        return false;
    }

    public void setLeader(@Nullable Creature leadingCreature) {
        if (leadingCreature == this) {
            logger.log(Level.WARNING, this.getName() + " tries to lead itself at ", new Exception());
            return;
        }
        this.clearOrders();
        if (this.leader == null) {
            if (leadingCreature != null) {
                if (this.isLeading(leadingCreature)) {
                    return;
                }
                this.leader = leadingCreature;
                Creatures.getInstance().setLastLed(this.getWurmId(), this.leader.getWurmId());
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " now follows " + this.leader.getNameWithGenus() + ".", this.leader, this, 5);
                this.leader.getCommunicator().sendNormalServerMessage("You start leading " + this.getNameWithGenus() + ".");
                this.getCommunicator().sendNormalServerMessage("You start following " + this.leader.getNameWithGenus() + ".");
            }
        } else if (leadingCreature == null) {
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " stops following " + this.leader.getNameWithGenus() + ".", this.leader, this, 5);
            this.leader.getCommunicator().sendNormalServerMessage("You stop leading " + this.getNameWithGenus() + ".");
            this.getCommunicator().sendNormalServerMessage("You stop following " + this.leader.getNameWithGenus() + ".");
            this.leader.removeFollower(this);
            this.leader = null;
        }
    }

    public void removeFollower(Creature follower) {
        if (this.followers != null) {
            this.followers.remove(follower);
        }
    }

    public void putInWorld() {
        try {
            Zone z = Zones.getZone(this.getTileX(), this.getTileY(), this.getLayer() >= 0);
            z.addCreature(this.getWurmId());
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, this.getName() + " " + nsz.getMessage(), nsz);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, this.getName() + " " + nsc.getMessage(), nsc);
        }
    }

    public static final double getRange(Creature performer, double targetX, double targetY) {
        double diffx = Math.abs((double)performer.getPosX() - targetX);
        double diffy = Math.abs((double)performer.getPosY() - targetY);
        return Math.sqrt(diffx * diffx + diffy * diffy);
    }

    public static final double getTileRange(Creature performer, int targetX, int targetY) {
        double diffx = Math.abs(performer.getTileX() - targetX);
        double diffy = Math.abs(performer.getTileY() - targetY);
        return Math.sqrt(diffx * diffx + diffy * diffy);
    }

    public boolean isWithinTileDistanceTo(int tileX, int tileY, int heigh1tOffset, int maxDist) {
        int ptilex = this.getTileX();
        int ptiley = this.getTileY();
        return ptilex <= tileX + maxDist && ptilex >= tileX - maxDist && ptiley <= tileY + maxDist && ptiley >= tileY - maxDist;
    }

    public boolean isWithinDistanceTo(@Nonnull Item item, float maxDist) {
        return this.isWithinDistanceTo(item.getPos3f(), maxDist);
    }

    public boolean isWithinDistanceTo(@Nonnull Vector3f targetPos, float maxDist) {
        return this.isWithinDistanceTo(targetPos.x, targetPos.y, targetPos.z, maxDist);
    }

    public boolean isWithinDistanceTo(float aPosX, float aPosY, float aPosZ, float maxDist) {
        return Math.abs(this.getStatus().getPositionX() + this.getAltOffZ() - aPosX) <= maxDist && Math.abs(this.getStatus().getPositionY() - aPosY) <= maxDist;
    }

    public boolean isWithinDistanceTo(Creature targetCret, float maxDist) {
        return Math.abs(this.getStatus().getPositionX() - targetCret.getPosX()) <= maxDist && Math.abs(this.getStatus().getPositionY() - targetCret.getPosY()) <= maxDist;
    }

    public boolean isWithinDistanceTo(float aPosX, float aPosY, float aPosZ, float maxDist, float modifier) {
        return Math.abs(this.getStatus().getPositionX() - (aPosX + modifier)) < maxDist && Math.abs(this.getStatus().getPositionY() - (aPosY + modifier)) < maxDist;
    }

    public boolean isWithinDistanceToZ(float aPosZ, float maxDist, boolean addHalfHeight) {
        return Math.abs(this.getStatus().getPositionZ() + (addHalfHeight ? (float)this.getHalfHeightDecimeters() / 10.0f : 0.0f) - aPosZ) < maxDist;
    }

    public boolean isWithinDistanceTo(int aPosX, int aPosY, int maxDistance) {
        return Math.abs(this.getTileX() - aPosX) <= maxDistance && Math.abs(this.getTileY() - aPosY) <= maxDistance;
    }

    public void creatureMoved(Creature creature, float diffX, float diffY, float diffZ) {
        if (this.leader != null && this.leader.equals(creature) && !this.isRidden() && (diffX != 0.0f || diffY != 0.0f)) {
            this.followLeader();
        }
        if (this.isTypeFleeing()) {
            if (creature.isPlayer() && this.isBred()) {
                return;
            }
            if (creature.isPlayer() || creature.isAggHuman() || creature.isHuman() || creature.isCarnivore() || creature.isMonster()) {
                float newDistance;
                Vector2f mypos = new Vector2f(this.getPosX(), this.getPosY());
                float oldDistance = new Vector2f(creature.getPosX() - diffX, creature.getPosY() - diffY).distance(mypos);
                if (oldDistance > (newDistance = new Vector2f(creature.getPosX(), creature.getPosY()).distance(mypos))) {
                    if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                        int baseCounter = (int)(Math.max(1.0f, creature.getBaseCombatRating() - this.getBaseCombatRating()) * 5.0f);
                        if ((float)baseCounter - newDistance > 0.0f) {
                            this.setFleeCounter((int)Math.min(60.0f, Math.max(3.0f, (float)baseCounter - newDistance)));
                        }
                    } else {
                        this.setFleeCounter(60);
                    }
                }
            }
        }
    }

    public final boolean isPrey() {
        return this.template.isPrey();
    }

    public final boolean isSpy() {
        return this.status.modtype == 8 && (this.template.getTemplateId() == 84 || this.template.getTemplateId() == 10 || this.template.getTemplateId() == 12);
    }

    public void delete() {
        Server.getInstance().addCreatureToRemove(this);
    }

    public void destroyVisionArea() {
        if (this.visionArea != null) {
            this.visionArea.destroy();
        }
        this.visionArea = null;
    }

    public void createVisionArea() throws Exception {
        if (this.visionArea != null) {
            this.visionArea.destroy();
        }
        this.visionArea = new VisionArea(this, this.template.getVision());
    }

    public String getHisHerItsString() {
        if (this.status.getSex() == 0) {
            return "his";
        }
        if (this.status.getSex() == 1) {
            return "her";
        }
        return "its";
    }

    public String getHimHerItString() {
        if (this.status.getSex() == 0) {
            return "him";
        }
        if (this.status.getSex() == 1) {
            return "her";
        }
        return "it";
    }

    public boolean mayAttack(@Nullable Creature cret) {
        return this.status.getStunned() <= 0.0f && !this.status.isUnconscious();
    }

    public boolean isStunned() {
        return this.status.getStunned() > 0.0f;
    }

    public boolean isUnconscious() {
        return this.status.isUnconscious();
    }

    public String getHeSheItString() {
        if (this.status.getSex() == 0) {
            return "he";
        }
        if (this.status.getSex() == 1) {
            return "she";
        }
        return "it";
    }

    public void stopCurrentAction() {
        try {
            String toSend = this.actions.stopCurrentAction(false);
            if (toSend.length() > 0) {
                this.communicator.sendNormalServerMessage(toSend);
            }
            this.sendActionControl("", false, 0);
        }
        catch (NoSuchActionException noSuchActionException) {
            // empty catch block
        }
    }

    public void maybeInterruptAction(int damage) {
        try {
            Action act = this.actions.getCurrentAction();
            if (act.isVulnerable() && act.getNumber() != Spells.SPELL_CHARM_ANIMAL.number && act.getNumber() != Spells.SPELL_DOMINATE.number && this.getBodyControlSkill().skillCheck((float)damage / 100.0f, this.zoneBonus, false, 1.0f) < 0.0) {
                String toSend = this.actions.stopCurrentAction(false);
                if (toSend.length() > 0) {
                    this.communicator.sendNormalServerMessage(toSend);
                }
                this.sendActionControl("", false, 0);
            }
        }
        catch (NoSuchActionException noSuchActionException) {
            // empty catch block
        }
    }

    public float getCombatDamage(Item bodyPart) {
        short pos = bodyPart.getPlace();
        if (pos == 13 || pos == 14) {
            return this.getHandDamage();
        }
        if (pos == 34) {
            return this.getKickDamage();
        }
        if (pos == 1) {
            return this.getHeadButtDamage();
        }
        if (pos == 29) {
            return this.getBiteDamage();
        }
        if (pos == 2) {
            return this.getBreathDamage();
        }
        return 0.0f;
    }

    public String getAttackStringForBodyPart(Item bodypart) {
        if (bodypart.getPlace() == 13 || bodypart.getPlace() == 14) {
            return this.template.getHandDamString();
        }
        if (bodypart.getPlace() == 34) {
            return this.template.getKickDamString();
        }
        if (bodypart.getPlace() == 29) {
            return this.template.getBiteDamString();
        }
        if (bodypart.getPlace() == 1) {
            return this.template.getHeadButtDamString();
        }
        if (bodypart.getPlace() == 2) {
            return this.template.getBreathDamString();
        }
        return this.template.getHandDamString();
    }

    public float getBodyWeaponSpeed(Item bodypart) {
        float size = this.template.getSize();
        if (bodypart.getPlace() == 13 || bodypart.getPlace() == 14) {
            return size + 1.0f;
        }
        if (bodypart.getPlace() == 34) {
            return size + 2.0f;
        }
        if (bodypart.getPlace() == 29) {
            return size + 2.5f;
        }
        if (bodypart.getPlace() == 1) {
            return size + 3.0f;
        }
        if (bodypart.getPlace() == 2) {
            return size + 3.5f;
        }
        return 4.0f;
    }

    public Item getArmour(byte location) throws NoArmourException, NoSpaceException {
        Item bodyPart = null;
        try {
            boolean barding = this.isHorse();
            bodyPart = barding ? this.status.getBody().getBodyPart(2) : this.status.getBody().getBodyPart(location);
            if (location == 29) {
                Item helmet = this.getArmour((byte)1);
                return helmet;
            }
            Set<Item> its = bodyPart.getItems();
            for (Item item : its) {
                byte[] spaces;
                if (!item.isArmour()) continue;
                for (byte lSpace : spaces = item.getBodySpaces()) {
                    if (lSpace != location && !barding) continue;
                    return item;
                }
            }
        }
        catch (NoArmourException noa) {
            throw noa;
        }
        catch (Exception ex) {
            throw new NoSpaceException(ex);
        }
        throw new NoArmourException("No armour worn on bodypart " + location);
    }

    public Item getCarriedItem(int itemTemplateId) {
        Item[] items;
        Item inventory = this.getInventory();
        for (Item lItem : items = inventory.getAllItems(false)) {
            if (lItem.getTemplateId() != itemTemplateId) continue;
            return lItem;
        }
        Item body = this.getBody().getBodyItem();
        for (Item lItem : items = body.getAllItems(false)) {
            if (lItem.getTemplateId() != itemTemplateId) continue;
            return lItem;
        }
        return null;
    }

    public Item getEquippedItem(byte location) throws NoSpaceException {
        try {
            Set<Item> wornItems = this.status.getBody().getBodyPart(location).getItems();
            for (Item item : wornItems) {
                if (item.isArmour() || item.isBodyPartAttached()) continue;
                return item;
            }
        }
        catch (NullPointerException npe) {
            if (this.status == null) {
                logger.log(Level.WARNING, "status is null for creature" + this.getName(), npe);
            }
            if (this.status.getBody() == null) {
                logger.log(Level.WARNING, "body is null for creature" + this.getName(), npe);
            }
            if (this.status.getBody().getBodyPart(location) == null) {
                logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + this.getName(), npe);
            }
            logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + this.getName(), npe);
        }
        return null;
    }

    public Item getEquippedWeapon(byte location) throws NoSpaceException {
        return this.getEquippedWeapon(location, true);
    }

    public Item getEquippedWeapon(byte location, boolean allowBow) throws NoSpaceException {
        return this.getEquippedWeapon(location, allowBow, false);
    }

    public Item getEquippedWeapon(byte location, boolean allowBow, boolean fetchBodypart) throws NoSpaceException {
        Item bodyPart = null;
        try {
            bodyPart = this.status.getBody().getBodyPart(location);
            if (this.isAnimal()) {
                return bodyPart;
            }
            if (bodyPart.getPlace() != 37 && bodyPart.getPlace() != 38 && bodyPart.getPlace() != 13 && bodyPart.getPlace() != 14 || !this.isPlayer() && fetchBodypart) {
                return bodyPart;
            }
            Set<Item> wornItems = bodyPart.getItems();
            for (Item item : wornItems) {
                if (item.isArmour() || item.isBodyPartAttached() || !(Weapon.getBaseDamageForWeapon(item) > 0.0f) && (!item.isWeaponBow() || !allowBow)) continue;
                return item;
            }
            if (bodyPart.getPlace() == 37 || bodyPart.getPlace() == 38) {
                int handSlot = bodyPart.getPlace() == 37 ? 13 : 14;
                bodyPart = this.status.getBody().getBodyPart(handSlot);
            }
        }
        catch (NullPointerException npe) {
            if (this.status == null) {
                logger.log(Level.WARNING, "status is null for creature" + this.getName(), npe);
            } else if (this.status.getBody() == null) {
                logger.log(Level.WARNING, "body is null for creature" + this.getName(), npe);
            } else if (this.status.getBody().getBodyPart(location) == null) {
                logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + this.getName(), npe);
            } else {
                logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + this.getName(), npe);
            }
            throw new NoSpaceException("No  bodypart " + location, npe);
        }
        return bodyPart;
    }

    public int getTotalInventoryWeightGrams() {
        Item[] items;
        Body body = this.status.getBody();
        int weight = 0;
        for (Item lItem : items = body.getAllItems()) {
            weight += lItem.getFullWeight();
        }
        Item[] inventoryItems = this.possessions.getInventory().getAllItems(true);
        for (int x = 0; x < items.length; ++x) {
            weight += inventoryItems[x].getFullWeight();
        }
        return weight;
    }

    public void startPersonalAction(short action, long subject, long _target) {
        try {
            BehaviourDispatcher.action(this, this.communicator, subject, _target, action);
        }
        catch (FailedException failedException) {
        }
        catch (NoSuchBehaviourException noSuchBehaviourException) {
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
        }
        catch (NoSuchItemException noSuchItemException) {
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
        }
        catch (NoSuchWallException noSuchWallException) {
            // empty catch block
        }
    }

    public void setFighting() {
        if (this.opponent != null) {
            if (this.getPower() > 0 && !this.isVisible()) {
                this.setOpponent(null);
                return;
            }
            try {
                Action lCurrentAction = null;
                try {
                    lCurrentAction = this.getCurrentAction();
                }
                catch (NoSuchActionException noSuchActionException) {
                    // empty catch block
                }
                if (lCurrentAction == null || lCurrentAction.getNumber() != 114) {
                    BehaviourDispatcher.action(this, this.communicator, -1L, this.opponent.getWurmId(), (short)114);
                } else if (lCurrentAction != null) {
                    this.sendToLoggers("busy " + lCurrentAction.getActionString() + " seconds " + lCurrentAction.getCounterAsFloat() + " " + lCurrentAction.getTarget() + ", path is null:" + (this.status.getPath() == null), (byte)4);
                }
                this.status.setPath(null);
            }
            catch (FailedException fe) {
                this.setOpponent(null);
            }
            catch (NoSuchBehaviourException nsb) {
                this.setTarget(-10L, true);
                this.setOpponent(null);
                logger.log(Level.WARNING, nsb.getMessage(), nsb);
            }
            catch (NoSuchCreatureException nsc) {
                this.setTarget(-10L, true);
                this.setOpponent(null);
            }
            catch (NoSuchItemException nsi) {
                this.setTarget(-10L, true);
                this.setOpponent(null);
                logger.log(Level.WARNING, nsi.getMessage(), nsi);
            }
            catch (NoSuchPlayerException nsp) {
                this.setTarget(-10L, true);
                this.setOpponent(null);
            }
            catch (NoSuchWallException nsw) {
                this.setOpponent(null);
                logger.log(Level.WARNING, nsw.getMessage(), nsw);
            }
        }
    }

    public void attackTarget() {
        block40: {
            if (this.target != -10L && (this.opponent == null || this.opponent.getWurmId() != this.target)) {
                long start = System.nanoTime();
                Creature tg = this.getTarget();
                if (tg != null && (tg.isDead() || tg.isOffline())) {
                    this.setTarget(-10L, true);
                } else if (this.isDominated() && tg != null && tg.isDominated() && this.getDominator() == tg.getDominator()) {
                    this.setTarget(-10L, true);
                    this.setOpponent(null);
                } else if (tg != null) {
                    if (Creature.rangeTo(this, tg) < Actions.actionEntrys[114].getRange()) {
                        ArrayList<MulticolorLineSegment> segments;
                        if (!this.isPlayer() && tg.getFloorLevel() != this.getFloorLevel()) {
                            if (this.isSpiritGuard()) {
                                this.pushToFloorLevel(this.getTarget().getFloorLevel());
                            } else if (tg.getFloorLevel() != this.getFloorLevel()) {
                                Floor[] floors;
                                for (Floor f : floors = this.getCurrentTile().getFloors(Math.min(this.getFloorLevel(), tg.getFloorLevel()) * 30, Math.max(this.getFloorLevel(), tg.getFloorLevel()) * 30)) {
                                    if (tg.getFloorLevel() > this.getFloorLevel()) {
                                        if (f.getFloorLevel() != this.getFloorLevel() + 1 || (!f.isOpening() || !this.canOpenDoors()) && !f.isStair()) continue;
                                        this.pushToFloorLevel(f.getFloorLevel());
                                        break;
                                    }
                                    if (f.getFloorLevel() != this.getFloorLevel() || (!f.isOpening() || !this.canOpenDoors()) && !f.isStair()) continue;
                                    this.pushToFloorLevel(f.getFloorLevel() - 1);
                                    break;
                                }
                            }
                        }
                        if (!(tg.getLayer() == this.getLayer() || tg.getCurrentTile().isTransition && this.getCurrentTile().isTransition)) {
                            return;
                        }
                        if (tg != this.opponent && tg.getAttackers() >= tg.getMaxGroupAttackSize()) {
                            segments = new ArrayList<MulticolorLineSegment>();
                            segments.add(new CreatureLineSegment(tg));
                            segments.add(new MulticolorLineSegment(" is too crowded with attackers. You find no space.", 0));
                            this.getCommunicator().sendColoredMessageCombat(segments);
                            return;
                        }
                        if (!CombatHandler.prerequisitesFail(this, tg, true, this.getPrimWeapon())) {
                            if (!tg.isTeleporting()) {
                                this.setOpponent(tg);
                                if (!tg.isPlayer() && this.fightlevel > 1) {
                                    this.fightlevel = (byte)(this.fightlevel / 2);
                                    if (this.isPlayer()) {
                                        this.getCommunicator().sendFocusLevel(this.getWurmId());
                                    }
                                }
                                if (!this.isPlayer()) {
                                    this.status.setMoving(false);
                                }
                                segments = new ArrayList();
                                segments.add(new CreatureLineSegment(this));
                                segments.add(new MulticolorLineSegment(" try to " + CombatEngine.getAttackString(this, this.getPrimWeapon()) + " ", 0));
                                segments.add(new CreatureLineSegment(tg));
                                segments.add(new MulticolorLineSegment(".", 0));
                                this.getCommunicator().sendColoredMessageCombat(segments);
                                if (this.isPlayer() || this.isDominated()) {
                                    segments.get(1).setText(" tries to " + CombatEngine.getAttackString(this, this.getPrimWeapon()) + " ");
                                    tg.getCommunicator().sendColoredMessageCombat(segments);
                                    if (this.isDominated() && this.getDominator() != null && this.getDominator().isPlayer()) {
                                        this.getDominator().getCommunicator().sendColoredMessageCombat(segments);
                                    }
                                } else {
                                    segments.get(1).setText(" moves in to attack ");
                                    tg.getCommunicator().sendColoredMessageCombat(segments);
                                }
                            }
                        } else if (!this.isPlayer() && Server.rand.nextInt(50) == 0) {
                            this.setTarget(-10L, true);
                        }
                    } else if (this.isSpellCaster() && Creature.rangeTo(this, tg) < 24 && !this.isPlayer() && tg.getFloorLevel() == this.getFloorLevel() && this.getLayer() == tg.getLayer() && this.getFavor() >= 100.0f && Server.rand.nextInt(10) == 0) {
                        this.setOpponent(tg);
                        short spellAction = 420;
                        switch (this.template.getTemplateId()) {
                            case 110: {
                                if (Server.rand.nextInt(3) == 0) {
                                    spellAction = 485;
                                }
                                if (!Server.rand.nextBoolean()) break;
                                spellAction = 414;
                                break;
                            }
                            case 111: {
                                if (Server.rand.nextInt(3) == 0) {
                                    spellAction = 550;
                                }
                                if (!Server.rand.nextBoolean()) break;
                                spellAction = 549;
                                break;
                            }
                            default: {
                                spellAction = 420;
                            }
                        }
                        if (this.opponent != null) {
                            try {
                                long itemId = -10L;
                                try {
                                    Item bodyHand = this.getBody().getBodyPart(14);
                                    itemId = bodyHand.getWurmId();
                                }
                                catch (Exception ex) {
                                    logger.log(Level.INFO, this.getName() + ": No hand.");
                                }
                                if (spellAction == 420 || spellAction == 414) {
                                    BehaviourDispatcher.action(this, this.communicator, itemId, Tiles.getTileId(this.opponent.getTileX(), this.opponent.getTileY(), 0), spellAction);
                                    break block40;
                                }
                                BehaviourDispatcher.action(this, this.communicator, itemId, this.opponent.getWurmId(), spellAction);
                            }
                            catch (Exception ex) {
                                logger.log(Level.INFO, this.getName() + " casting " + spellAction + ":" + ex.getMessage(), ex);
                            }
                        }
                    }
                } else {
                    this.setTarget(-10L, true);
                }
            }
        }
    }

    public void moan() {
        if (this.isDominated()) {
            if (this.getDominator() != null) {
                this.getDominator().getCommunicator().sendNormalServerMessage("You sense a disturbance in " + this.getNameWithGenus() + ".");
            }
            if (this.isAnimal()) {
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " grunts.", this, 5);
            } else {
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " moans.", this, 5);
            }
        }
    }

    private void frolic() {
        if (this.isDominated()) {
            if (this.getDominator() != null) {
                this.getDominator().getCommunicator().sendNormalServerMessage("You sense a sudden calm in " + this.getNameWithGenus() + ".");
            }
            if (this.isAnimal()) {
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " purrs.", this, 5);
            } else {
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " hizzes.", this, 5);
            }
        }
    }

    private boolean isOutOfBounds() {
        return this.getTileX() < 0 || this.getTileX() > Zones.worldTileSizeX - 1 || this.getTileY() < 0 || this.getTileY() > Zones.worldTileSizeY - 1;
    }

    private boolean isFlying() {
        return false;
    }

    public boolean healRandomWound(int power) {
        Wound[] wounds;
        if (this.getBody().getWounds() != null && (wounds = this.getBody().getWounds().getWounds()).length > 0) {
            int num = Server.rand.nextInt(wounds.length);
            if (wounds[num].getSeverity() / 1000.0f < (float)power) {
                wounds[num].heal();
                return true;
            }
            wounds[num].modifySeverity(-power * 1000);
            return true;
        }
        return false;
    }

    protected void decreaseOpportunityCounter() {
        if (this.opportunityAttackCounter > 0) {
            this.opportunityAttackCounter = (byte)(this.opportunityAttackCounter - 1);
        }
    }

    public void pollNPC() {
    }

    public void pollNPCChat() {
    }

    public CreatureAIData getCreatureAIData() {
        if (this.template.getCreatureAI() != null) {
            if (this.aiData == null) {
                this.aiData = this.template.getCreatureAI().createCreatureAIData();
                this.aiData.setCreature(this);
            }
            return this.aiData;
        }
        return null;
    }

    /*
     * Exception decompiling
     */
    public boolean poll() throws Exception {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [8[CATCHBLOCK]], but top level block is 7[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public void setWebArmourModTime(float time) {
        this.webArmourModTime = time;
    }

    private void checkStealthing() {
    }

    public boolean isSpellCaster() {
        return this.template.isCaster();
    }

    public boolean isSummoner() {
        return this.template.isSummoner();
    }

    public boolean isRespawn() {
        return false;
    }

    private void handleCreatureOutOfBounds() {
        logger.log(Level.WARNING, this.getName() + " was out of bounds. Killing.");
        Creatures.getInstance().setCreatureDead(this);
        Players.getInstance().setCreatureDead(this);
        this.destroy();
    }

    protected void checkBreedCounter() {
        if (this.breedCounter > 0) {
            this.breedCounter -= 201;
        }
        if (this.breedCounter < 0) {
            this.breedCounter = 0;
        }
        if (this.breedCounter == 0) {
            float mod;
            if (this.leader == null && !this.isDominated() && this.isInTheMoodToBreed(false)) {
                this.checkBreedingPossibility();
            }
            if ((mod = (float)Servers.localServer.getBreedingTimer()) <= 0.0f) {
                mod = 1.0f;
            }
            int base = (int)(84000.0f / mod);
            if (this.checkPregnancy(false)) {
                base = (int)(Servers.isThisAPvpServer() ? 2000.0f / mod : 84000.0f / mod);
                this.forcedBreed = true;
            } else {
                base = (int)(Servers.isThisAPvpServer() ? 900.0f / mod : 2000.0f / mod);
                this.forcedBreed = false;
            }
            this.breedCounter = base + (int)((float)Server.rand.nextInt(Math.max(1000, 100 * Math.abs(20 - this.getStatus().age))) / mod);
        }
    }

    public void pollLoyalty() {
        if (this.isDominated() && this.getStatus().pollLoyalty()) {
            if (this.getDominator() != null) {
                this.getDominator().getCommunicator().sendAlertServerMessage(this.getNameWithGenus() + " is tame no more.", (byte)2);
                if (this.getDominator().getPet() == this) {
                    this.getDominator().setPet(-10L);
                }
            }
            this.setDominator(-10L);
        }
    }

    public boolean isInRock() {
        return this.getLayer() < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(this.getTileX(), this.getTileY())));
    }

    public void findFood() {
        if (this.currentTile != null && !this.graze()) {
            Item[] items;
            for (Item lItem : items = this.currentTile.getItems()) {
                if (!lItem.isEdibleBy(this)) continue;
                if (lItem.getTemplateId() != 272) {
                    this.eat(lItem);
                    return;
                }
                if (!lItem.isCorpseLootable()) continue;
                this.eat(lItem);
                return;
            }
        }
    }

    public int eat(Item item) {
        int hungerStilled = MethodsItems.eat(this, item);
        if (hungerStilled > 0) {
            this.getStatus().modifyHunger(-hungerStilled, item.getNutritionLevel());
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " eats " + item.getNameWithGenus() + ".", this, 5);
        } else if (item.getTemplateId() != 272) {
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " eats " + item.getNameWithGenus() + ".", this, 5);
        }
        return hungerStilled;
    }

    public boolean graze() {
        if (this.isGrazer() && this.isOnSurface()) {
            if (this.hasTrait(13)) {
                if (Server.rand.nextBoolean()) {
                    try {
                        Skill str = this.skills.getSkill(102);
                        if (str.getKnowledge() > 15.0) {
                            str.setKnowledge(str.getKnowledge() - (double)0.003f, false);
                        }
                    }
                    catch (NoSuchSkillException nss) {
                        this.skills.learn(102, 20.0f);
                    }
                    return false;
                }
            } else if (Server.rand.nextBoolean()) {
                try {
                    Skill str = this.skills.getSkill(102);
                    double templateStr = this.getTemplate().getSkills().getSkill(102).getKnowledge();
                    if (str.getKnowledge() < templateStr) {
                        str.setKnowledge(str.getKnowledge() + (double)0.03f, false);
                    }
                }
                catch (NoSuchSkillException e) {
                    this.skills.learn(102, 20.0f);
                }
                catch (Exception e) {
                    // empty catch block
                }
            }
            int tile = Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley);
            byte type = Tiles.decodeType(tile);
            Village v = Villages.getVillage(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
            if (!this.hasTrait(22)) {
                return this.grazeNonCorrupt(tile, type, v);
            }
            return this.grazeCorrupt(tile, type);
        }
        return false;
    }

    private boolean grazeCorrupt(int tile, byte type) {
        if (type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
            this.getStatus().modifyHunger(-10000, 0.9f);
            if (Server.rand.nextInt(20) == 0) {
                if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
                    TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
                } else if (type == Tiles.Tile.TILE_MYCELIUM.id) {
                    GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
                    if (growthStage == GrassData.GrowthStage.SHORT) {
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
                    } else {
                        growthStage = growthStage.getPreviousStage();
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE));
                    }
                    Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
                }
            }
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " grazes.", this, 5);
            return true;
        }
        return false;
    }

    private boolean grazeNonCorrupt(int tile, byte type, Village v) {
        if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) {
            this.getStatus().modifyHunger(-10000, type == Tiles.Tile.TILE_STEPPE.id ? 0.5f : 0.9f);
            if (Server.rand.nextInt(20) == 0) {
                int enchGrassPackChance = 120;
                if (v == null) {
                    enchGrassPackChance = 80;
                } else if (v.getCreatureRatio() > Village.OPTIMUMCRETRATIO) {
                    enchGrassPackChance = 240;
                }
                if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
                    TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
                } else if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id && Server.rand.nextInt(enchGrassPackChance) == 0) {
                    GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
                    if (growthStage == GrassData.GrowthStage.SHORT) {
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
                    } else {
                        growthStage = growthStage.getPreviousStage();
                        Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE));
                    }
                    Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
                }
            }
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " grazes.", this, 5);
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean pollAge() {
        block4: {
            long start = System.nanoTime();
            int maxAge = this.template.getMaxAge();
            if (this.isReborn()) {
                maxAge = 14;
            }
            if (!this.getStatus().pollAge(maxAge)) break block4;
            this.sendDeathString();
            this.die(true, "Old Age");
            boolean bl = true;
            return bl;
        }
        boolean bl = false;
        return bl;
    }

    public void sendDeathString() {
        if (!this.isOffline()) {
            String act = "hiccups";
            int x = Server.rand.nextInt(6);
            if (x == 0) {
                act = "drools";
            } else if (x == 1) {
                act = "faints";
            } else if (x == 2) {
                act = "makes a weird gurgly sound";
            } else if (x == 3) {
                act = "falls down";
            } else if (x == 4) {
                act = "rolls over";
            }
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " " + act + " and dies.", this, 5);
        }
    }

    public void pollFavor() {
        if ((this.isSpellCaster() || this.isSummoner()) && Server.rand.nextInt(30) == 0) {
            try {
                this.setFavor(this.getFavor() + 10.0f);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public boolean isSalesman() {
        return this.template.getTemplateId() == 9;
    }

    public boolean isAvatar() {
        return this.template.getTemplateId() == 78 || this.template.getTemplateId() == 79 || this.template.getTemplateId() == 80 || this.template.getTemplateId() == 81 || this.template.getTemplateId() == 68;
    }

    public void removeRandomItems() {
        block9: {
            if (!this.isTrading() && this.isNpcTrader() && Server.rand.nextInt(86400) == 0) {
                try {
                    this.actions.getCurrentAction();
                }
                catch (NoSuchActionException nsa) {
                    Shop myshop = Economy.getEconomy().getShop(this);
                    if (myshop.getOwnerId() == -10L) {
                        Shop kingsMoney = Economy.getEconomy().getKingsShop();
                        if (kingsMoney.getMoney() > 0L) {
                            int value = 0;
                            value = (int)(kingsMoney.getMoney() / (long)Shop.getNumTraders());
                            if (!Servers.localServer.HOMESERVER) {
                                value = (int)((float)value * (1.0f + Zones.getPercentLandForKingdom(this.getKingdomId()) / 100.0f));
                                value = (int)((float)value * (1.0f + (float)Items.getBattleCampControl(this.getKingdomId()) / 10.0f));
                            }
                            if (value > 0 && myshop != null && myshop.getMoney() < (long)Servers.localServer.getTraderMaxIrons() && (myshop.getSellRatio() > 0.1f || Server.getInstance().isPS()) && (Server.getInstance().isPS() || Servers.localServer.id != 15 || kingsMoney.getMoney() > 2000000L)) {
                                myshop.setMoney(myshop.getMoney() + (long)value);
                                kingsMoney.setMoney(kingsMoney.getMoney() - (long)value);
                            }
                        }
                    }
                    if (!this.canAutoDismissMerchant(myshop)) break block9;
                    try {
                        Item sign = ItemFactory.createItem(209, 10.0f + Server.rand.nextFloat() * 10.0f, this.getName());
                        sign.setDescription("Due to poor business I have moved on. Thank you for your time. " + this.getName());
                        sign.setLastOwnerId(myshop.getOwnerId());
                        sign.putItemInfrontof(this);
                        sign.setIsPlanted(true);
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage() + " " + this.getName() + " at " + this.getTileX() + ", " + this.getTileY(), e);
                    }
                    TraderManagementQuestion.dismissMerchant(this, this.getWurmId());
                }
            }
        }
    }

    private boolean canAutoDismissMerchant(Shop myshop) {
        if (myshop.howLongEmpty() > 7257600000L) {
            return true;
        }
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(myshop.getOwnerId());
        if (pinf != null) {
            try {
                if (!pinf.loaded) {
                    pinf.load();
                }
                if (pinf.lastLogin == 0L && System.currentTimeMillis() - pinf.lastLogout > 7257600000L) {
                    logger.log(Level.INFO, pinf.getName() + " last login was " + Server.getTimeFor(System.currentTimeMillis() - pinf.lastLogout) + " ago.");
                    return true;
                }
                return false;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return true;
    }

    public float getArmourMod() {
        return this.template.getNaturalArmour();
    }

    public final Vector2f getPos2f() {
        return this.getStatus().getPosition2f();
    }

    public final Vector3f getPos3f() {
        return this.getStatus().getPosition3f();
    }

    public final float getPosX() {
        return this.getStatus().getPositionX();
    }

    public final float getPosY() {
        return this.getStatus().getPositionY();
    }

    public final float getPositionZ() {
        return this.getStatus().getPositionZ();
    }

    @Nonnull
    public final TilePos getTilePos() {
        return TilePos.fromXY(this.getTileX(), this.getTileY());
    }

    public final int getTileX() {
        return (int)this.getPosX() >> 2;
    }

    public final int getTileY() {
        return (int)this.getPosY() >> 2;
    }

    public final int getPosZDirts() {
        return (int)(this.getPositionZ() * 10.0f);
    }

    public final void pollItems() {
        this.resetCompassLantern();
        ++this.pollCounter;
        boolean triggerPoll = false;
        if (this.isHorse() && this.getBody().getAllItems().length > 0) {
            triggerPoll = true;
        }
        if (this.isPlayer() || (this.isReborn() || this.isHuman()) && this.pollCounter > 10800 || triggerPoll && (long)this.pollCounter > 60L) {
            if (!this.checkedHotItemsAfterLogin && this.isPlayer()) {
                this.checkedHotItemsAfterLogin = true;
                long timeSinceLastCoolingCheck = System.currentTimeMillis() - PlayerInfoFactory.createPlayerInfo(this.getName()).getLastLogout();
                this.getInventory().pollCoolingItems(this, timeSinceLastCoolingCheck);
            }
            this.getInventory().pollOwned(this);
            this.getBody().getBodyItem().pollOwned(this);
            if (triggerPoll) {
                this.getInventory().pollCoolingItems(this, (long)(this.pollCounter - 1) * 1000L);
                this.getBody().getBodyItem().pollCoolingItems(this, (long)(this.pollCounter - 1) * 1000L);
            }
        }
        if (this.pollCounter > 10800 || triggerPoll && (long)this.pollCounter > 60L) {
            this.pollCounter = 0;
        }
        this.pollCompassLantern();
    }

    public boolean isLastDeath() {
        return false;
    }

    public boolean isOnHostileHomeServer() {
        return false;
    }

    public void playPersonalSound(String soundName) {
    }

    public final void setReputationEffects() {
        if (Servers.localServer.HOMESERVER && (!this.isPlayer() && this.isDominated() || this.isRidden() || this.getHitched() != null) && this.attackers != null) {
            for (Long attl : this.attackers.keySet()) {
                try {
                    Brand brand;
                    Creature attacker = Server.getInstance().getCreature(attl);
                    if (!attacker.isPlayer() && !attacker.isDominated()) continue;
                    if (this.isRidden()) {
                        if (attacker.getCitizenVillage() != null && this.getCurrentVillage() == attacker.getCitizenVillage()) continue;
                        for (Long riderLong : this.getRiders()) {
                            try {
                                Creature rider = Server.getInstance().getCreature(riderLong);
                                if (rider == attacker || rider.isOkToKillBy(attacker)) continue;
                                attacker.setUnmotivatedAttacker();
                                attacker.setReputation(attacker.getReputation() - 10);
                            }
                            catch (NoSuchPlayerException noSuchPlayerException) {}
                        }
                        continue;
                    }
                    if (this.getHitched() != null) {
                        if (attacker.getCitizenVillage() != null && this.getCurrentVillage() == attacker.getCitizenVillage() || this.getHitched().isCreature()) continue;
                        try {
                            Item i = Items.getItem(this.getHitched().wurmid);
                            long ownid = i.getLastOwnerId();
                            if (ownid == attacker.getWurmId()) continue;
                            try {
                                byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                                if (!attacker.isFriendlyKingdom(kingd) || attacker.hasBeenAttackedBy(ownid)) continue;
                                boolean ok = false;
                                try {
                                    Creature owner = Server.getInstance().getCreature(ownid);
                                    if (owner.isOkToKillBy(attacker)) {
                                        ok = true;
                                    }
                                }
                                catch (NoSuchCreatureException noSuchCreatureException) {
                                    // empty catch block
                                }
                                if (ok) continue;
                                attacker.setUnmotivatedAttacker();
                                attacker.setReputation(attacker.getReputation() - 10);
                            }
                            catch (Exception exception) {
                            }
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.INFO, this.getHitched().wurmid + " no such item:", nsi);
                        }
                        continue;
                    }
                    if (this.isDominated()) {
                        if (!attacker.isFriendlyKingdom(this.getKingdomId())) continue;
                        boolean ok = false;
                        try {
                            Creature owner = Server.getInstance().getCreature(this.dominator);
                            if (attacker == owner || owner.isOkToKillBy(attacker)) {
                                ok = true;
                            }
                        }
                        catch (NoSuchCreatureException owner) {
                            // empty catch block
                        }
                        if (ok) continue;
                        attacker.setUnmotivatedAttacker();
                        attacker.setReputation(attacker.getReputation() - 10);
                        continue;
                    }
                    if (this.getCurrentVillage() == null || (brand = Creatures.getInstance().getBrand(this.getWurmId())) == null) continue;
                    try {
                        Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                        if (this.getCurrentVillage() != villageBrand || attacker.getCitizenVillage() == villageBrand) continue;
                        attacker.setUnmotivatedAttacker();
                        attacker.setReputation(attacker.getReputation() - 10);
                    }
                    catch (NoSuchVillageException nsv) {
                        brand.deleteBrand();
                    }
                }
                catch (Exception exception) {}
            }
        }
    }

    public void die(boolean freeDeath, String reasonOfDeath) {
        this.die(freeDeath, reasonOfDeath, false);
    }

    public void die(boolean freeDeath, String reasonOfDeath, boolean noCorpse) {
        boolean fullOverride;
        WcKillCommand wkc = new WcKillCommand(WurmId.getNextWCCommandId(), this.getWurmId());
        if (Servers.isThisLoginServer()) {
            wkc.sendFromLoginServer();
        } else {
            wkc.sendToLoginServer();
        }
        if (this.isPregnant()) {
            Offspring.deleteSettings(this.getWurmId());
        }
        if (this.getTemplate().getCreatureAI() != null && (fullOverride = this.getTemplate().getCreatureAI().creatureDied(this))) {
            return;
        }
        String corpseDescription = "";
        if (this.template.isHorse) {
            String col;
            corpseDescription = col = this.template.getColourName(this.status);
        } else if (this.template.isBlackOrWhite) {
            if (!(this.hasTrait(15) || this.hasTrait(16) || this.hasTrait(18) || this.hasTrait(24) || this.hasTrait(25) || this.hasTrait(23) || this.hasTrait(30) || this.hasTrait(31) || this.hasTrait(32) || this.hasTrait(33) || this.hasTrait(34) || !this.hasTrait(17))) {
                corpseDescription = "black";
            }
        } else if (this.template.isColoured) {
            corpseDescription = this.template.getColourName(this.getStatus());
        }
        if (this.isCaredFor()) {
            corpseDescription = corpseDescription.equals("") ? corpseDescription + reasonOfDeath.toLowerCase() : corpseDescription + " [" + reasonOfDeath.toLowerCase() + "]";
        }
        if (this.getTemplate().getTemplateId() == 105) {
            try {
                Item water = ItemFactory.createItem(128, 100.0f, "");
                this.getInventory().insertItem(water);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, this.getName() + " No template for item id " + 128);
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, this.getName() + " failed for item id " + 128);
            }
            Weather weather = Server.getWeather();
            if (weather != null) {
                weather.modifyFogTarget(-0.025f);
            }
        }
        if (this.isUnique() && !this.isReborn()) {
            Player[] ps = Players.getInstance().getPlayers();
            HashSet<Player> lootReceivers = new HashSet<Player>();
            for (Player p : ps) {
                if (p == null || p.getInventory() == null || !p.isWithinDistanceTo(this, 300.0f) || !p.isPaying()) continue;
                if (!p.isDead()) {
                    try {
                        Item blood = ItemFactory.createItem(866, 100.0f, "");
                        blood.setData2(this.template.getTemplateId());
                        p.getInventory().insertItem(blood);
                        lootReceivers.add(p);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, p.getName() + " No template for item id " + 866);
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, p.getName() + " " + fe.getMessage() + ":" + 866);
                    }
                    continue;
                }
                logger.log(Level.INFO, "Player " + p.getName() + " is dead, and therefor received no loot from " + this.getNameWithGenus() + ".");
            }
            this.setPathing(false, true);
            if (this.isDragon()) {
                HashSet<Player> primeLooters = new HashSet<Player>();
                HashSet<Player> leecher = new HashSet<Player>();
                for (Player looter : lootReceivers) {
                    Skill bStrength = looter.getBodyStrength();
                    Skill bControl = looter.getBodyControlSkill();
                    Skill fighting = looter.getFightingSkill();
                    if (bStrength != null && bStrength.getRealKnowledge() >= 30.0 || bControl != null && bControl.getRealKnowledge() >= 30.0 || fighting != null && fighting.getRealKnowledge() >= 65.0 || looter.isPriest()) {
                        primeLooters.add(looter);
                        continue;
                    }
                    leecher.add(looter);
                }
                int lootTemplate = 371;
                if (this.getTemplate().getTemplateId() == 16 || this.getTemplate().getTemplateId() == 89 || this.getTemplate().getTemplateId() == 91 || this.getTemplate().getTemplateId() == 90 || this.getTemplate().getTemplateId() == 92) {
                    lootTemplate = 372;
                }
                try {
                    this.distributeDragonScaleOrHide(primeLooters, leecher, lootTemplate);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for " + lootTemplate + "! Players to receive were:");
                    for (Player p : lootReceivers) {
                        logger.log(Level.WARNING, p.getName());
                    }
                }
            }
        }
        this.removeIllusion();
        this.setReputationEffects();
        this.getCombatHandler().clearMoveStack();
        this.getCommunicator().setGroundOffset(0, true);
        this.setDoLavaDamage(false);
        this.setDoAreaEffect(false);
        if (this.isPlayer()) {
            for (int x = 0; x < 5; ++x) {
                this.getStatus().decreaseFat();
            }
        }
        this.combatRound = 0;
        Item corpse = null;
        int tilex = this.getTileX();
        int tiley = this.getTileY();
        try {
            King king;
            Object c2;
            boolean wasHunted = this.hasAttackedUnmotivated();
            if (this.isPlayer()) {
                Vehicle vehic;
                Item i = this.getDraggedItem();
                if (i != null && (i.getTemplateId() == 539 || i.getTemplateId() == 186 || i.getTemplateId() == 445 || i.getTemplateId() == 1125)) {
                    this.achievement(72);
                }
                if (this.getVehicle() != -10L && (vehic = Vehicles.getVehicleForId(this.getVehicle())) != null && vehic.getPilotId() == this.getWurmId()) {
                    try {
                        c2 = Items.getItem(this.getVehicle());
                        if (((Item)c2).getTemplateId() == 539) {
                            this.achievement(71);
                        }
                    }
                    catch (NoSuchItemException c2) {
                        // empty catch block
                    }
                }
                if (!PlonkData.DEATH.hasSeenThis(this)) {
                    PlonkData.DEATH.trigger(this);
                }
            }
            if (this.getDraggedItem() != null) {
                MethodsItems.stopDragging(this, this.getDraggedItem());
            }
            this.stopLeading();
            if (this.leader != null) {
                this.leader.removeFollower(this);
            }
            this.clearLinks();
            this.disableLink();
            this.disembark(false);
            if (!this.hasNoServerSound()) {
                SoundPlayer.playSound(this.getDeathSound(), this, 1.6f);
            }
            if (this.musicPlayer != null) {
                this.musicPlayer.checkMUSIC_DYING1_SND();
            }
            Creatures.getInstance().setCreatureDead(this);
            Players.getInstance().setCreatureDead(this);
            if (this.getSpellEffects() != null) {
                this.getSpellEffects().destroy(true);
            }
            if (this.currentVillage != null) {
                this.currentVillage.removeTarget(this.getWurmId(), true);
            }
            this.setOpponent(null);
            this.target = -10L;
            try {
                this.getCurrentAction().stop(false);
            }
            catch (NoSuchActionException i) {
                // empty catch block
            }
            this.actions.clear();
            if (this.isKing() && (king = King.getKing(this.getKingdomId())) != null) {
                if (king.getChallengeAcceptedDate() > 0L && System.currentTimeMillis() > king.getChallengeAcceptedDate()) {
                    king.setFailedChallenge();
                }
                if (this.isInOwnDuelRing() && !king.hasFailedAllChallenges()) {
                    king.setFailedChallenge();
                }
            }
            this.getCommunicator().sendSafeServerMessage("You are dead.");
            this.getCommunicator().sendCombatSafeMessage("You are dead.");
            Server.getInstance().broadCastAction(this.getNameWithGenus() + " is dead. R.I.P.", this, 5);
            if (!this.isPlayer() && (this.isTrader() || this.isSalesman() || this.isBartender() || this.template != null && (this.template.id == 63 || this.template.id == 62))) {
                String message = "(" + this.getWurmId() + ") died at [" + this.getTileX() + ", " + this.getTileY() + "] surf=" + this.isOnSurface() + " with the reason of death being " + reasonOfDeath;
                if (this.attackers != null && this.attackers.size() > 0) {
                    message = message + ". numAttackers=" + this.attackers.size() + " :";
                    int counter = 0;
                    c2 = this.attackers.keySet().iterator();
                    while (c2.hasNext()) {
                        long playerID = (Long)c2.next();
                        message = message + " " + PlayerInfoFactory.getPlayerName(playerID) + (++counter == this.attackers.size() ? "," : ".");
                    }
                }
                Players.getInstance().sendGmMessage(null, this.getName(), message, false);
                String templateAndName = (this.getTemplate() != null ? this.getTemplate().getName() : "Important creature") + " " + this.getName() + " died";
                logger.warning(templateAndName + " " + message);
                WcTrelloDeaths wtd = new WcTrelloDeaths(templateAndName, message);
                wtd.sendToLoginServer();
            }
            if (!this.isGhost() && !this.template.isNoCorpse() && !noCorpse && (this.getCreatureAIData() == null || this.getCreatureAIData() != null && this.getCreatureAIData().doesDropCorpse())) {
                Brand brand;
                corpse = ItemFactory.createItem(272, 100.0f, null);
                corpse.setPosXY(this.getStatus().getPositionX(), this.getStatus().getPositionY());
                corpse.setPosZ(this.calculatePosZ());
                corpse.onBridge = this.getBridgeId();
                if (this.hasCustomSize()) {
                    corpse.setSizes((int)((float)(corpse.getSizeX() * (this.getSizeModX() & 0xFF)) / 64.0f), (int)((float)(corpse.getSizeY() * (this.getSizeModY() & 0xFF)) / 64.0f), (int)((float)(corpse.getSizeZ() * (this.getSizeModZ() & 0xFF)) / 64.0f));
                }
                corpse.setRotation(Creature.normalizeAngle(this.getStatus().getRotation() - 180.0f));
                int nameLength = 10 + this.name.length() + this.getStatus().getAgeString().length() + 1 + this.getStatus().getTypeString().length();
                int nameLengthNoType = 10 + this.name.length() + this.getStatus().getAgeString().length();
                int nameLengthNoAge = 10 + this.name.length() + 1 + this.getStatus().getTypeString().length();
                if (this.isPlayer()) {
                    corpse.setName("corpse of " + this.name);
                } else if (nameLength < 40) {
                    corpse.setName("corpse of " + this.getStatus().getAgeString() + " " + (nameLength < 40 ? this.getStatus().getTypeString() : "") + this.name.toLowerCase());
                } else if (nameLengthNoAge < 40) {
                    corpse.setName("corpse of " + this.getStatus().getTypeString() + this.name.toLowerCase());
                } else if (nameLengthNoType < 40) {
                    corpse.setName("corpse of " + this.getStatus().getAgeString() + " " + this.name.toLowerCase());
                } else if (("corpse of " + this.name).length() < 40) {
                    corpse.setName("corpse of " + this.name.toLowerCase());
                } else {
                    StringTokenizer strt = new StringTokenizer(this.name.toLowerCase());
                    int maxNumber = strt.countTokens();
                    String coname = "corpse of " + strt.nextToken();
                    int number = 1;
                    while (strt.hasMoreTokens()) {
                        String next = strt.nextToken();
                        if (maxNumber >= 4 && (maxNumber <= 4 || ++number <= 4)) continue;
                        if ((coname + " " + next).length() >= 40) break;
                        coname = coname + " ";
                        coname = coname + next;
                    }
                    corpse.setName(coname);
                }
                int extra1 = -1;
                byte extra2 = this.status.modtype;
                if (this.template.isHorse || this.template.isBlackOrWhite) {
                    extra1 = this.template.getColourCode(this.status);
                }
                if (this.isReborn()) {
                    corpse.setDamage(20.0f);
                    corpse.setButchered();
                    corpse.setAllData(this.template.getTemplateId(), 1, extra1, extra2);
                } else {
                    corpse.setAllData(this.template.getTemplateId(), this.getStatus().fat << 1, extra1, extra2);
                }
                if (this.isUnique()) {
                    Server.getInstance().broadCastNormal(this.getNameWithGenus() + " has been slain.");
                    if (!Servers.localServer.EPIC && !this.isReborn()) {
                        try {
                            boolean drop = false;
                            drop = this.isDragon() ? Server.rand.nextInt(10) == 0 : Server.rand.nextBoolean();
                            if (drop) {
                                int item = 795 + Server.rand.nextInt(16);
                                if (item == 1009) {
                                    item = 807;
                                } else if (item == 805) {
                                    item = 808;
                                }
                                Item epicItem = ItemFactory.createItem(item, 60 + Server.rand.nextInt(20), "");
                                epicItem.setOwnerId(corpse.getWurmId());
                                epicItem.setLastOwnerId(corpse.getWurmId());
                                if (this.isDragon()) {
                                    epicItem.setAuxData((byte)2);
                                }
                                logger.info("Dropping a " + epicItem.getName() + " (" + epicItem.getWurmId() + ")  for the slaying of " + corpse.getName());
                                corpse.insertItem(epicItem);
                            }
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, "No template for item id 866");
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, fe.getMessage() + ":" + 866);
                        }
                    } else if (Servers.localServer.EPIC && !Servers.localServer.HOMESERVER && this.isDragon()) {
                        try {
                            int lootId;
                            boolean dropLoot = Server.rand.nextBoolean();
                            if (dropLoot && (lootId = CreatureTemplateCreator.getDragonLoot(this.template.getTemplateId())) > 0) {
                                Item loot = ItemFactory.createItem(lootId, 60 + Server.rand.nextInt(20), "");
                                logger.info("Dropping a " + loot.getName() + " (" + loot.getWurmId() + ") for the slaying of " + corpse.getName());
                                corpse.insertItem(loot);
                                loot.setOwnerId(corpse.getWurmId());
                            }
                        }
                        catch (Exception dropLoot) {
                            // empty catch block
                        }
                    }
                }
                if (this.isPlayer() && !wasHunted && this.getReputation() >= 0 && !this.isInPvPZone() && Servers.localServer.KINGDOM != 0 && !this.isOnHostileHomeServer()) {
                    boolean killedInVillageWar = false;
                    if (this.attackers != null) {
                        for (Long l : this.attackers.keySet()) {
                            try {
                                Creature c3 = Creatures.getInstance().getCreature(l);
                                if (c3.getCitizenVillage() == null || !c3.getCitizenVillage().isEnemy(this) || !Servers.isThisAPvpServer()) continue;
                                logger.log(Level.INFO, this.getName() + " was killed by " + c3.getName() + " during village war. May be looted.");
                                killedInVillageWar = true;
                            }
                            catch (Exception c3) {}
                        }
                    }
                    if (!killedInVillageWar) {
                        corpse.setProtected(true);
                    }
                }
                corpse.setAuxData(this.getKingdomId());
                corpse.setWeight((int)Math.min(50000.0f, this.status.body.getWeight(this.status.fat)), false);
                corpse.setLastOwnerId(this.getWurmId());
                if (this.isKingdomGuard()) {
                    corpse.setDamage(50.0f);
                }
                if (this.getSex() == 1) {
                    corpse.setFemale(true);
                }
                corpse.setDescription(corpseDescription);
                if (!this.isPlayer() && !Servers.isThisAPvpServer() && (brand = Creatures.getInstance().getBrand(this.getWurmId())) != null) {
                    try {
                        int value;
                        corpse.setWasBrandedTo(brand.getBrandId());
                        PermissionsPlayerList allowedList = this.getPermissionsPlayerList();
                        PermissionsByPlayer[] pbpList = allowedList.getPermissionsByPlayer();
                        byte bito = ItemSettings.CorpsePermissions.COMMANDER.getBit();
                        int valueo = ItemSettings.CorpsePermissions.COMMANDER.getValue();
                        byte bitx = ItemSettings.CorpsePermissions.EXCLUDE.getBit();
                        int valuex = ItemSettings.CorpsePermissions.EXCLUDE.getValue();
                        Village bVill = null;
                        for (PermissionsByPlayer pbp : pbpList) {
                            if (pbp.getPlayerId() != -60L) continue;
                            if (bVill == null) {
                                bVill = Villages.getVillage((int)brand.getBrandId());
                            }
                            value = 0;
                            if (pbp.hasPermission(bito)) {
                                value += valueo;
                            }
                            if (pbp.hasPermission(bitx)) {
                                value += valuex;
                            }
                            if (value == 0) continue;
                            for (Citizen citz : bVill.getCitizens()) {
                                if (!citz.isPlayer() || !citz.getRole().mayBrand()) continue;
                                ItemSettings.addPlayer(corpse.getWurmId(), citz.wurmId, value);
                            }
                        }
                        for (PermissionsByPlayer pbp : pbpList) {
                            if (pbp.getPlayerId() == -60L) continue;
                            value = 0;
                            if (pbp.hasPermission(bito)) {
                                value += valueo;
                            }
                            if (pbp.hasPermission(bitx)) {
                                value += valuex;
                            }
                            if (value == 0) continue;
                            ItemSettings.addPlayer(corpse.getWurmId(), pbp.getPlayerId(), value);
                        }
                    }
                    catch (NoSuchVillageException e) {
                        Creatures.getInstance().setBrand(this.getWurmId(), -10L);
                    }
                }
                VolaTile vvtile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
                vvtile.addItem(corpse, false, this.getWurmId(), false);
            } else if (this.isGhost() || this.template.isNoCorpse()) {
                int[] butcheredItems = this.getTemplate().getItemsButchered();
                for (int x = 0; x < butcheredItems.length; ++x) {
                    try {
                        ItemFactory.createItem(butcheredItems[x], 20.0f + Server.rand.nextFloat() * 80.0f, this.getPosX(), this.getPosY(), Server.rand.nextInt() * 360, this.isOnSurface(), (byte)0, this.getStatus().getBridgeId(), this.getName());
                        continue;
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, fe.getMessage());
                        continue;
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, nst.getMessage());
                    }
                }
            }
            VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
            boolean keepItems = this.isTransferring();
            if (!this.isOnCurrentServer()) {
                keepItems = true;
            }
            if (this.getDeity() != null && this.getDeity().isDeathItemProtector() && this.getFaith() >= 70.0f && this.getFavor() >= 35.0f) {
                float chance = 0.35f;
                String successMessage = this.getDeity().getName() + " is with you and keeps your items safe.";
                String failMessage = this.getDeity().getName() + " could not keep your items safe this time.";
                float rand = Server.rand.nextFloat();
                if (this.isDeathProtected()) {
                    chance = 0.5f;
                    if (rand > 0.35f && rand <= chance) {
                        successMessage = this.getDeity().getName() + " could not keep your items safe this time, but ethereal strands of web attach to your items and keep them safe, close to your spirit!";
                    } else {
                        failMessage = this.getDeity().getName() + " could not keep your items safe this time.";
                    }
                }
                if (rand <= chance) {
                    this.getCommunicator().sendNormalServerMessage(successMessage);
                    keepItems = true;
                } else {
                    this.getCommunicator().sendNormalServerMessage(failMessage);
                }
            } else if (this.isDeathProtected()) {
                if (Server.rand.nextInt(2) > 0) {
                    this.getCommunicator().sendNormalServerMessage("Ethereal strands of web attach to your items and keep them safe, close to your spirit!");
                    keepItems = true;
                } else {
                    this.getCommunicator().sendNormalServerMessage("Your items could not be kept safe this time.");
                }
            }
            if (this.isPlayer()) {
                try {
                    Item legs = this.getBody().getBodyPart(19);
                    boolean found = false;
                    Set<Item> worn = legs.getItems();
                    if (worn != null) {
                        for (Item w : worn) {
                            if (!w.isArmour()) continue;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        pantLess.add(this.getWurmId());
                    }
                }
                catch (NoSpaceException legs) {
                    // empty catch block
                }
            }
            boolean insertItem = true;
            boolean dropNewbieItems = false;
            if (this.attackers != null) {
                for (Long cid : this.attackers.keySet()) {
                    if (WurmId.getType(cid) != 0 || Servers.localServer.isChallengeServer() && this.getPlayingTime() <= 86400000L) continue;
                    dropNewbieItems = true;
                    break;
                }
            }
            Item inventory = this.getInventory();
            Item[] invarr = inventory.getAllItems(true);
            for (int x = 0; x < invarr.length; ++x) {
                if (invarr[x].isTraded() && this.getTrade() != null) {
                    invarr[x].getTradeWindow().removeItem(invarr[x]);
                }
                boolean destroyChall = false;
                if (Features.Feature.FREE_ITEMS.isEnabled() && invarr[x].isChallengeNewbieItem() && (invarr[x].isArmour() || invarr[x].isWeapon() || invarr[x].isShield())) {
                    destroyChall = true;
                }
                if (destroyChall) {
                    Items.destroyItem(invarr[x].getWurmId());
                    continue;
                }
                if (invarr[x].isArtifact() || !keepItems && !invarr[x].isNoDrop() && (!invarr[x].isNewbieItem() || dropNewbieItems || invarr[x].isHollow() && !invarr[x].isTent())) {
                    try {
                        Item parent = invarr[x].getParent();
                        if (!inventory.equals(parent) && parent.getTemplateId() != 824) continue;
                        parent.dropItem(invarr[x].getWurmId(), true);
                        invarr[x].setBusy(false);
                        if (corpse != null && corpse.insertItem(invarr[x], true)) continue;
                        if (invarr[x].isTent() && invarr[x].isNewbieItem()) {
                            Items.destroyItem(invarr[x].getWurmId());
                            continue;
                        }
                        vtile.addItem(invarr[x], false, false);
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, this.getName() + " " + invarr[x].getName() + ":" + nsi.getMessage(), nsi);
                    }
                    continue;
                }
                if (invarr[x].isArtifact() || keepItems) continue;
                try {
                    Item parent = invarr[x].getParent();
                    invarr[x].setBusy(false);
                    boolean bl = insertItem = !parent.isNoDrop();
                    if (invarr[x].getTemplateId() == 443 && !(this.getStrengthSkill() > 21.0) && !(this.getFaith() > 35.0f)) {
                        insertItem = false;
                        if (!invarr[x].setDamage(invarr[x].getDamage() + 0.3f, true)) {
                            insertItem = true;
                        }
                    }
                    if (!insertItem) continue;
                    parent.dropItem(invarr[x].getWurmId(), false);
                    inventory.insertItem(invarr[x], true);
                    continue;
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, this.getName() + " " + invarr[x].getName() + ":" + nsi.getMessage(), nsi);
                }
            }
            Item[] boditems = this.getBody().getContainersAndWornItems();
            for (int x = 0; x < boditems.length; ++x) {
                if (boditems[x].isTraded() && this.getTrade() != null) {
                    boditems[x].getTradeWindow().removeItem(boditems[x]);
                }
                if (boditems[x].isArtifact() || !keepItems && !boditems[x].isNoDrop() && (!boditems[x].isNewbieItem() || dropNewbieItems || boditems[x].isHollow() && !boditems[x].isTent())) {
                    if (boditems[x].isHollow()) {
                        Item[] containedItems;
                        for (Item lContainedItem : containedItems = boditems[x].getAllItems(false)) {
                            if (!lContainedItem.isNoDrop() && (!lContainedItem.isNewbieItem() || dropNewbieItems || lContainedItem.isHollow())) continue;
                            try {
                                lContainedItem.setBusy(false);
                                Item parent = lContainedItem.getParent();
                                parent.dropItem(lContainedItem.getWurmId(), false);
                                inventory.insertItem(lContainedItem, true);
                            }
                            catch (NoSuchItemException nsi) {
                                logger.log(Level.WARNING, this.getName() + ":" + nsi.getMessage(), nsi);
                            }
                        }
                    }
                    try {
                        Item parent = boditems[x].getParent();
                        parent.dropItem(boditems[x].getWurmId(), true);
                        boditems[x].setBusy(false);
                        if (corpse != null && corpse.insertItem(boditems[x], true)) continue;
                        if (boditems[x].isTent() && boditems[x].isNewbieItem()) {
                            Items.destroyItem(invarr[x].getWurmId());
                            continue;
                        }
                        vtile.addItem(boditems[x], false, false);
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, this.getName() + ":" + nsi.getMessage(), nsi);
                    }
                    continue;
                }
                if (boditems[x].isArtifact() || keepItems) continue;
                try {
                    Item parent = boditems[x].getParent();
                    boditems[x].setBusy(false);
                    boolean bl = insertItem = !parent.isNoDrop();
                    if (boditems[x].getTemplateId() == 443 && !(this.getStrengthSkill() > 21.0) && !(this.getFaith() > 35.0f)) {
                        insertItem = false;
                        if (!boditems[x].setDamage(boditems[x].getDamage() + 0.3f, true)) {
                            insertItem = true;
                        }
                    }
                    if (!insertItem) continue;
                    parent.dropItem(boditems[x].getWurmId(), false);
                    inventory.insertItem(boditems[x], true);
                    continue;
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, this.getName() + " " + boditems[x].getName() + ":" + nsi.getMessage(), nsi);
                }
            }
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, this.getName() + ":" + fe.getMessage(), fe);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, this.getName() + ":" + nst.getMessage(), nst);
        }
        if (corpse != null) {
            if (this.isSuiciding() && corpse.getAllItems(true).length == 0) {
                Items.destroyItem(corpse.getWurmId());
                corpse = null;
            }
        } else {
            this.playAnimation("die", false);
        }
        try {
            this.setBridgeId(-10L);
            this.getBody().healFully();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.getName() + ex.getMessage(), ex);
        }
        if (this.isTransferring() || !this.isOnCurrentServer()) {
            return;
        }
        if (this.getTemplateId() == 78 || this.getTemplateId() == 79 || this.getTemplateId() == 80 || this.getTemplateId() == 81 || this.getTemplateId() == 68) {
            EpicServerStatus.avatarCreatureKilled(this.getWurmId());
        }
        this.setDeathEffects(freeDeath, tilex, tiley);
        if (EpicServerStatus.doesTraitorMissionExist(this.getWurmId())) {
            EpicServerStatus.traitorCreatureKilled(this.getWurmId());
        }
    }

    private void distributeDragonScaleOrHide(Set<Player> primeLooters, Set<Player> leecher, int lootTemplate) throws NoSuchTemplateException {
        float pSplit;
        float lSplit;
        ItemTemplate itemt = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
        float lootNums = this.calculateDragonLootMultiplier();
        float totalWeightToDistribute = this.calculateDragonLootTotalWeight(itemt, lootNums) * (lootTemplate == 371 ? 3.0f : 1.0f);
        float leecherShare = 0.0f;
        if (leecher.size() > 0) {
            leecherShare = totalWeightToDistribute / 5.0f;
        }
        float primeShare = totalWeightToDistribute - leecherShare;
        if (leecher.size() > 0 && (lSplit = leecherShare / (float)leecher.size()) > (pSplit = primeShare / (float)primeLooters.size())) {
            leecherShare = pSplit * 0.9f * (float)leecher.size();
            primeShare = totalWeightToDistribute - leecherShare;
        }
        this.splitDragonLootTo(primeLooters, itemt, lootTemplate, primeShare);
        this.splitDragonLootTo(leecher, itemt, lootTemplate, leecherShare);
    }

    private final float calculateDragonLootTotalWeight(ItemTemplate template, float lootMult) {
        return 1.0f + (float)template.getWeightGrams() * lootMult;
    }

    private final float calculateDragonLootMultiplier() {
        float lootNums = 1.0f;
        if (!Servers.isThisAnEpicServer()) {
            lootNums = Math.max(1.0f, 1.0f + Server.rand.nextFloat() * 3.0f);
        }
        return lootNums;
    }

    private void splitDragonLootTo(Set<Player> lootReceivers, ItemTemplate itemt, int lootTemplate, float totalWeight) {
        if (lootReceivers.size() == 0) {
            return;
        }
        float receivers = lootReceivers.size();
        float weight = totalWeight / receivers;
        for (Player p : lootReceivers) {
            try {
                double power = 0.0;
                try {
                    Skill butchering = p.getSkills().getSkill(10059);
                    power = Math.max(0.0, butchering.skillCheck(10.0, 0.0, false, 10.0f));
                }
                catch (NoSuchSkillException nss) {
                    Skill butchering = p.getSkills().learn(10059, 1.0f);
                    power = Math.max(0.0, butchering.skillCheck(10.0, 0.0, false, 10.0f));
                }
                Item loot = ItemFactory.createItem(lootTemplate, (float)(80.0 + power / 5.0), "");
                String creatureName = this.getTemplate().getName().toLowerCase();
                if (!loot.getName().contains(creatureName)) {
                    loot.setName(creatureName.toLowerCase() + " " + itemt.getName());
                }
                loot.setData2(this.template.getTemplateId());
                loot.setWeight((int)weight, true);
                p.getInventory().insertItem(loot);
                lootReceivers.add(p);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, p.getName() + " No template for item id " + lootTemplate);
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, p.getName() + " " + fe.getMessage() + ":" + lootTemplate);
            }
        }
    }

    public boolean isSuiciding() {
        return false;
    }

    public Item[] getAllItems() {
        Item[] invitems;
        Item[] boditems;
        HashSet<Item> allitems = new HashSet<Item>();
        Item inventory = this.getInventory();
        allitems.add(inventory);
        Item body = this.getBody().getBodyItem();
        allitems.add(body);
        for (Item lBoditem : boditems = body.getAllItems(true)) {
            allitems.add(lBoditem);
        }
        for (Item lInvitem : invitems = inventory.getAllItems(true)) {
            allitems.add(lInvitem);
        }
        return allitems.toArray(new Item[allitems.size()]);
    }

    public void checkWorkMusic() {
        if (this.musicPlayer != null) {
            this.musicPlayer.checkMUSIC_VILLAGEWORK_SND();
        }
    }

    public boolean isFightingSpiritGuard() {
        return this.opponent != null && this.opponent.isSpiritGuard();
    }

    public boolean isFighting(long opponentid) {
        return this.opponent != null && this.opponent.getWurmId() == opponentid;
    }

    public void setFleeCounter(int newCounter) {
        this.setFleeCounter(newCounter, false);
    }

    public void setFleeCounter(int newCounter, boolean warded) {
        if (newCounter <= 0 || newCounter < this.fleeCounter) {
            return;
        }
        if (!((this.isPlayer() || this.isUnique() || this.isDominated() && !warded) && !this.isPrey() || !warded && !this.isPrey())) {
            this.fleeCounter = (byte)newCounter;
            this.sendToLoggers("updated flee counter: " + this.fleeCounter);
        }
    }

    public void setTarget(long targ, boolean switchTarget) {
        VolaTile t;
        Creature cret2;
        if (targ == this.getWurmId()) {
            targ = -10L;
        }
        if (this.isPrey()) {
            return;
        }
        if (targ != -10L && this.getVehicle() != -10L) {
            try {
                Vehicle v;
                cret2 = Server.getInstance().getCreature(this.target);
                if (cret2.getHitched() != null && (v = Vehicles.getVehicleForId(this.getVehicle())) != null && v == cret2.getHitched()) {
                    this.getCommunicator().sendNormalServerMessage("You cannot target " + cret2.getName() + " while on the same vehicle.");
                    targ = -10L;
                }
            }
            catch (NoSuchPlayerException | NoSuchCreatureException cret2) {
                // empty catch block
            }
        }
        if (this.loggerCreature1 != -10L) {
            logger.log(Level.FINE, this.getName() + " target=" + targ, new Exception());
        }
        if (targ == -10L) {
            this.getCommunicator().sendCombatStatus(0.0f, 0.0f, (byte)0);
            if (this.opponent != null && this.opponent.getWurmId() == this.target) {
                this.setOpponent(null);
            }
            if (this.target != targ) {
                try {
                    cret2 = Server.getInstance().getCreature(this.target);
                    cret2.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret2));
                }
                catch (NoSuchCreatureException cret3) {
                }
                catch (NoSuchPlayerException cret3) {
                    // empty catch block
                }
            }
            this.target = targ;
            this.getCommunicator().sendTarget(targ);
            t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (t != null) {
                t.sendUpdateTarget(this);
            }
            this.status.sendStateString();
        } else if ((this.target == -10L || switchTarget) && this.target != targ && (this.getBaseCombatRating() > 10.0f || this.fleeCounter <= 0)) {
            if (this.target != -10L) {
                try {
                    cret2 = Server.getInstance().getCreature(this.target);
                    cret2.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret2));
                }
                catch (NoSuchCreatureException cret4) {
                }
                catch (NoSuchPlayerException cret4) {
                    // empty catch block
                }
            }
            try {
                cret2 = Server.getInstance().getCreature(targ);
                if (this.isSpiritGuard() && this.citizenVillage != null) {
                    VolaTile currTile = cret2.getCurrentTile();
                    if (currTile.getTileX() < this.citizenVillage.getStartX() - 5 || currTile.getTileX() > this.citizenVillage.getEndX() + 5 || currTile.getTileY() < this.citizenVillage.getStartY() - 5 || currTile.getTileY() > this.citizenVillage.getEndY() + 5) {
                        if (cret2.opponent == this) {
                            cret2.setOpponent(null);
                            cret2.setTarget(-10L, true);
                            cret2.getCommunicator().sendNormalServerMessage("The " + this.getName() + " suddenly becomes hazy and hard to target.");
                        }
                        targ = -10L;
                        this.setOpponent(null);
                        if (this.status.getPath() == null) {
                            this.getMoveTarget(0);
                        }
                    } else {
                        this.citizenVillage.cryForHelp(this, false);
                    }
                }
                if (targ != -10L) {
                    cret2.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret2));
                }
            }
            catch (NoSuchCreatureException cret5) {
            }
            catch (NoSuchPlayerException cret5) {
                // empty catch block
            }
            this.target = targ;
            this.getCommunicator().sendTarget(targ);
            t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (t != null) {
                t.sendUpdateTarget(this);
            }
            this.status.sendStateString();
        }
    }

    public boolean modifyFightSkill(int dtilex, int dtiley) {
        boolean pvp = false;
        HashMap<Creature, Double> lSkillReceivers = null;
        boolean activatedTrigger = false;
        if (!this.isNoSkillgain()) {
            lSkillReceivers = new HashMap<Creature, Double>();
            long now = System.currentTimeMillis();
            double d = 0.0;
            double sumskill = 0.0;
            boolean wasHelped = false;
            if (this.attackers != null && this.attackers.size() > 0) {
                ArrayList<Long> possibleTriggerOwners = new ArrayList<Long>();
                for (long l : this.attackers.keySet()) {
                    if (now - this.attackers.get(l) >= 600000L || WurmId.getType(l) != 0 || this.isPlayer() && Players.getInstance().isOverKilling(l, this.getWurmId())) continue;
                    possibleTriggerOwners.add(l);
                }
                if (!possibleTriggerOwners.isEmpty()) {
                    try {
                        MissionTrigger[] trigs;
                        Iterator player = Players.getInstance().getPlayer((Long)possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
                        for (MissionTrigger t2 : trigs = MissionTriggers.getMissionTriggersWith(this.getTemplate().getTemplateId(), 491, this.getWurmId())) {
                            EpicMissionEnum missionEnum;
                            EpicMission em = EpicServerStatus.getEpicMissionForMission(t2.getMissionRequired());
                            if (em == null || (missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType())) == null || !EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)) continue;
                            float karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                            float karmaGained = karmaSplit / (float)EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                            karmaGained = (float)Math.ceil(karmaGained / (float)possibleTriggerOwners.size());
                            Iterator iterator = possibleTriggerOwners.iterator();
                            while (iterator.hasNext()) {
                                long id = (Long)iterator.next();
                                try {
                                    Player p = Players.getInstance().getPlayer(id);
                                    if (Deities.getFavoredKingdom(em.getEpicEntityId()) != p.getKingdomTemplateId() && Servers.localServer.EPIC) continue;
                                    MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                                    if (mp == null) {
                                        mp = MissionPerformed.startNewMission(t2.getMissionRequired(), id, 1.0f);
                                    } else {
                                        MissionPerformed mperf = mp.getMission(t2.getMissionRequired());
                                        if (mperf == null) {
                                            MissionPerformed.startNewMission(t2.getMissionRequired(), id, 1.0f);
                                        }
                                    }
                                    p.modifyKarma((int)karmaGained);
                                    if (!p.isPaying()) continue;
                                    p.setScenarioKarma((int)((float)p.getScenarioKarma() + karmaGained));
                                    if (!Servers.localServer.EPIC) continue;
                                    WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(WurmId.getNextWCCommandId(), new long[]{p.getWurmId()}, new int[]{p.getScenarioKarma()}, em.getEpicEntityId());
                                    wcek.sendToLoginServer();
                                }
                                catch (NoSuchPlayerException noSuchPlayerException) {}
                            }
                        }
                        MissionTriggers.activateTriggers(player, this.getTemplate().getTemplateId(), 491, this.getWurmId(), 1);
                        activatedTrigger = true;
                    }
                    catch (NoSuchPlayerException player) {
                        // empty catch block
                    }
                }
                for (Map.Entry<Long, Long> entry : this.attackers.entrySet()) {
                    long attackerId = entry.getKey();
                    long attackTime = entry.getValue();
                    if (now - attackTime >= 600000L) continue;
                    if (WurmId.getType(attackerId) == 0) {
                        pvp = true;
                        if (this.isPlayer() && Players.getInstance().isOverKilling(attackerId, this.getWurmId())) continue;
                        try {
                            Player player = Players.getInstance().getPlayer(attackerId);
                            if (!this.isDuelOrSpar(player)) {
                                d = player.getFightingSkill().getRealKnowledge();
                                lSkillReceivers.put(player, new Double(d));
                                sumskill += d;
                            }
                            if (!(this.isPlayer() || this.isSpiritGuard() || this.isKingdomGuard() || !player.isPlayer() || player.isDead())) {
                                player.checkCoinAward(this.attackers.size() * (this.isBred() ? 20 : (this.isDomestic() ? 50 : 100)));
                            }
                            if (!this.isChampion() || !player.isPlayer() || this.getKingdomId() == player.getKingdomId() && !player.isEnemyOnChaos(this)) continue;
                            player.addTitle(Titles.Title.ChampSlayer);
                            if (!player.isChampion()) continue;
                            player.modifyChampionPoints(30);
                            Servers.localServer.createChampTwit(player.getName() + " slays " + this.getName() + " and gains 30 champion points");
                        }
                        catch (NoSuchPlayerException player) {}
                        continue;
                    }
                    try {
                        Creature c = Creatures.getInstance().getCreature(attackerId);
                        if (c.isDominated()) {
                            d = c.getFightingSkill().getKnowledge();
                            lSkillReceivers.put(c, new Double(d));
                            sumskill += d;
                            continue;
                        }
                        if (!c.isSpiritGuard() && !c.isKingdomGuard() || this.isPlayer()) continue;
                        wasHelped = true;
                    }
                    catch (NoSuchCreatureException c) {}
                }
            }
            d = this.getFightingSkill().getRealKnowledge();
            this.getFightingSkill().touch();
            if (this.isPlayer() && d <= 10.0) {
                d = 0.0;
            }
            if (!this.isPlayer()) {
                d = this.getBaseCombatRating();
                if ((d += (double)this.getBonusCombatRating()) > 2.0) {
                    if (!this.isReborn() && !this.isUndead()) {
                        d *= 5.0;
                    } else if (this.getTemplate().getTemplateId() == 69) {
                        d *= (double)0.2f;
                    }
                }
            } else {
                this.getFightingSkill().setKnowledge(Math.max(1.0, this.getFightingSkill().getKnowledge() - 0.25), false);
            }
            if (d > 0.0) {
                if (!(this.isSpiritGuard() || this.isKingdomGuard() || this.isWarGuard())) {
                    HashSet lootReceivers = new HashSet();
                    for (Map.Entry entry : lSkillReceivers.entrySet()) {
                        double skillGained;
                        Creature p = (Creature)entry.getKey();
                        Double psk = (Double)entry.getValue();
                        double pskill = psk;
                        double percentSkillGained = pskill / sumskill;
                        double diff = d - pskill;
                        double lMod = 0.2f;
                        if (diff > 1.0) {
                            lMod = Math.sqrt(diff);
                        } else if (diff < -1.0) {
                            lMod = d / pskill;
                        }
                        if (!this.isPlayer()) {
                            lMod /= Servers.localServer.isChallengeServer() ? 2.0 : 7.0;
                            if (pskill > 70.0) {
                                double tomax = 100.0 - pskill;
                                double modifier = tomax / (double)(Servers.localServer.isChallengeServer() ? 30.0f : 500.0f);
                                lMod *= modifier;
                            }
                            if (wasHelped) {
                                lMod *= (double)0.1f;
                            }
                        } else if (pskill > 50.0 && d < 20.0) {
                            lMod = 0.0;
                        } else if (this.getKingdomId() == p.getKingdomId()) {
                            lMod = 0.0;
                        }
                        if (d <= 0.0) {
                            lMod = 0.0;
                        }
                        if ((skillGained = percentSkillGained * lMod * 0.25 * (double)ItemBonus.getKillEfficiencyBonus(p)) > 0.0) {
                            p.getFightingSkill().touch();
                            if (p.isPaying() || pskill < 20.0) {
                                if (pskill + skillGained > 100.0) {
                                    p.getFightingSkill().setKnowledge(pskill + (100.0 - pskill) / 100.0, false);
                                } else {
                                    p.getFightingSkill().setKnowledge(pskill + skillGained, false);
                                }
                                p.getFightingSkill().maybeSetMinimum();
                            }
                            p.getFightingSkill().checkInitialTitle();
                        }
                        if (!this.isPlayer()) {
                            if (p.isPlayer()) {
                                p.achievement(522);
                                if (p.isUndead()) {
                                    ++((Player)p).getSaveFile().undeadKills;
                                    ((Player)p).getSaveFile().setUndeadData();
                                    p.achievement(335);
                                }
                                if (this.isUnique()) {
                                    HistoryManager.addHistory(p.getName(), "slayed " + this.getName());
                                }
                                int tid = this.getTemplate().getTemplateId();
                                try {
                                    if (CreatureTemplate.isDragon(tid)) {
                                        ((Player)p).addTitle(Titles.Title.DragonSlayer);
                                    } else if (tid == 11 || tid == 27) {
                                        ((Player)p).addTitle(Titles.Title.TrollSlayer);
                                    } else if (tid == 20 || tid == 22) {
                                        ((Player)p).addTitle(Titles.Title.GiantSlayer);
                                    } else if (this.isUnique()) {
                                        ((Player)p).addTitle(Titles.Title.UniqueSlayer);
                                    }
                                }
                                catch (Exception ex) {
                                    logger.log(Level.WARNING, this.getName() + " and " + p.getName() + ":" + ex.getMessage());
                                }
                                switch (this.status.modtype) {
                                    case 1: {
                                        p.achievement(253);
                                        break;
                                    }
                                    case 2: {
                                        p.achievement(254);
                                        break;
                                    }
                                    case 3: {
                                        p.achievement(255);
                                        break;
                                    }
                                    case 4: {
                                        p.achievement(256);
                                        break;
                                    }
                                    case 5: {
                                        p.achievement(257);
                                        break;
                                    }
                                    case 6: {
                                        p.achievement(258);
                                        break;
                                    }
                                    case 7: {
                                        p.achievement(259);
                                        break;
                                    }
                                    case 8: {
                                        p.achievement(260);
                                        break;
                                    }
                                    case 9: {
                                        p.achievement(261);
                                        break;
                                    }
                                    case 10: {
                                        p.achievement(262);
                                        break;
                                    }
                                    case 11: {
                                        p.achievement(263);
                                        break;
                                    }
                                    case 99: {
                                        p.achievement(264);
                                        break;
                                    }
                                }
                                if (tid == 58) {
                                    p.achievement(225);
                                } else if (tid == 21 || tid == 118) {
                                    p.achievement(228);
                                } else if (tid == 25) {
                                    p.achievement(231);
                                } else if (tid == 11) {
                                    p.achievement(235);
                                } else if (tid == 10) {
                                    p.achievement(237);
                                } else if (tid == 54) {
                                    p.achievement(239);
                                } else if (tid == 56) {
                                    p.achievement(243);
                                } else if (tid == 57) {
                                    p.achievement(244);
                                } else if (tid == 55) {
                                    p.achievement(265);
                                } else if (tid == 43) {
                                    p.achievement(268);
                                } else if (tid == 42 || tid == 12) {
                                    p.achievement(269);
                                } else if (CreatureTemplate.isFullyGrownDragon(tid)) {
                                    p.achievement(270);
                                } else if (CreatureTemplate.isDragonHatchling(tid)) {
                                    p.achievement(271);
                                } else if (tid == 20) {
                                    p.achievement(272);
                                } else if (tid == 23) {
                                    p.achievement(273);
                                } else if (tid == 27) {
                                    p.achievement(274);
                                } else if (tid == 68) {
                                    p.achievement(276);
                                } else if (tid == 70) {
                                    p.achievement(277);
                                } else if (tid == 71) {
                                    p.achievement(278);
                                } else if (tid == 72) {
                                    p.achievement(279);
                                } else if (tid == 73) {
                                    p.achievement(280);
                                } else if (tid == 74) {
                                    p.achievement(281);
                                } else if (tid == 75) {
                                    p.achievement(282);
                                } else if (tid == 76) {
                                    p.achievement(283);
                                } else if (tid == 77) {
                                    p.achievement(284);
                                } else if (tid == 78) {
                                    p.achievement(285);
                                } else if (tid == 79) {
                                    p.achievement(286);
                                } else if (tid == 80) {
                                    p.achievement(287);
                                } else if (tid == 81) {
                                    p.achievement(288);
                                } else if (tid == 82) {
                                    p.achievement(289);
                                } else if (tid == 83 || tid == 117) {
                                    p.achievement(291);
                                } else if (tid == 84) {
                                    p.achievement(290);
                                } else if (tid == 85) {
                                    p.achievement(292);
                                } else if (tid == 59) {
                                    p.achievement(313);
                                } else if (tid == 15) {
                                    p.achievement(314);
                                } else if (tid == 14) {
                                    p.achievement(315);
                                } else if (tid == 13) {
                                    p.achievement(316);
                                } else if (tid == 22) {
                                    p.achievement(307);
                                } else if (tid == 26) {
                                    p.achievement(308);
                                } else if (tid == 64 || tid == 65) {
                                    p.achievement(309);
                                } else if (tid == 49 || tid == 3 || tid == 50) {
                                    p.achievement(310);
                                } else if (tid == 44) {
                                    p.achievement(311);
                                } else if (tid == 51) {
                                    p.achievement(312);
                                } else if (tid == 106) {
                                    p.achievement(378);
                                } else if (tid == 107) {
                                    p.achievement(379);
                                } else if (tid == 108) {
                                    p.achievement(380);
                                } else if (tid == 109) {
                                    p.achievement(381);
                                }
                                if (this.isDefendKingdom() && !this.isFriendlyKingdom(p.getKingdomId())) {
                                    p.achievement(275);
                                }
                                if (this.isReborn()) {
                                    p.achievement(248);
                                }
                                if (this.isUnique()) {
                                    p.achievement(589);
                                }
                            }
                        } else if (this.isKing() && p.isPlayer() && p.getKingdomId() != this.getKingdomId()) {
                            ((Player)p).addTitle(Titles.Title.Kingslayer);
                            HistoryManager.addHistory(p.getName(), "slayed " + this.getName());
                        }
                        if (this.isPlayer() && p.isPlayer() && !this.isUndead()) {
                            if (p.isUndead()) {
                                ++((Player)p).getSaveFile().undeadPlayerKills;
                                ((Player)p).getSaveFile().setUndeadData();
                                p.achievement(339);
                            }
                            logger.log(Level.INFO, p.getName() + " killed " + this.getName() + " as champ=" + p.isChampion() + ". Diff=" + diff + " mod=" + lMod + " skillGained=" + skillGained + " pskill=" + pskill + " kskill=" + d);
                            if (skillGained > 0.0) {
                                p.achievement(8);
                                Item weapon = p.getPrimWeapon();
                                if (weapon != null) {
                                    if (weapon.isWeaponBow()) {
                                        p.achievement(11);
                                    } else if (weapon.isWeaponSword()) {
                                        p.achievement(14);
                                    } else if (weapon.isWeaponCrush()) {
                                        p.achievement(17);
                                    } else if (weapon.isWeaponAxe()) {
                                        p.achievement(20);
                                    } else if (weapon.isWeaponKnife()) {
                                        p.achievement(25);
                                    }
                                    if (weapon.getTemplateId() == 314) {
                                        p.achievement(27);
                                    } else if (weapon.getTemplateId() == 567) {
                                        p.achievement(29);
                                    } else if (weapon.getTemplateId() == 20) {
                                        p.achievement(30);
                                    }
                                }
                                Item[] bodyItems = p.getBody().getAllItems();
                                int clothArmourFound = 0;
                                int dragonPiecesFound = 0;
                                for (Item i : bodyItems) {
                                    if (!i.isArmour()) continue;
                                    if (i.isCloth()) {
                                        ++clothArmourFound;
                                        continue;
                                    }
                                    if (!i.isDragonArmour() || i.getTemplateId() != 476 && i.getTemplateId() != 475) continue;
                                    ++dragonPiecesFound;
                                }
                                if (clothArmourFound >= 8) {
                                    p.achievement(31);
                                }
                                if (dragonPiecesFound >= 2) {
                                    p.achievement(32);
                                }
                                if (pantLess.contains(this.getWurmId())) {
                                    this.achievement(33);
                                }
                            }
                        }
                        if (!this.isPlayer() || !(d > 40.0) || !(lMod > 0.0) || !p.isChampion()) continue;
                        PlayerKills pk = Players.getInstance().getPlayerKillsFor(p.getWurmId());
                        if (System.currentTimeMillis() - pk.getLastKill(this.getWurmId()) <= 86400000L || pk.getNumKills(this.getWurmId()) >= 10L) continue;
                        p.modifyChampionPoints(1);
                        Servers.localServer.createChampTwit(p.getName() + " slays " + this.getName() + " and gains 1 champion point because of difficulty");
                    }
                    this.getTemplate().getLootTable().ifPresent(t -> t.awardAll(this, lootReceivers));
                } else {
                    for (Map.Entry entry : lSkillReceivers.entrySet()) {
                        Creature p = (Creature)entry.getKey();
                        if (!p.isPlayer() || this.isFriendlyKingdom(p.getKingdomId()) || !this.isSpiritGuard()) continue;
                        p.achievement(267);
                    }
                }
            } else {
                for (Map.Entry entry : lSkillReceivers.entrySet()) {
                    Creature p = (Creature)entry.getKey();
                    if (!p.isPlayer() || this.isFriendlyKingdom(p.getKingdomId()) || !this.isSpiritGuard()) continue;
                    p.achievement(267);
                }
            }
        } else if (!this.isUndead() && this.attackers != null && this.attackers.size() > 0) {
            ArrayList<Long> possibleTriggerOwners = new ArrayList<Long>();
            Iterator<Object> iterator = this.attackers.keySet().iterator();
            while (iterator.hasNext()) {
                long l = iterator.next();
                if (WurmId.getType(l) != 0 || this.isPlayer() && Players.getInstance().isOverKilling(l, this.getWurmId())) continue;
                possibleTriggerOwners.add(l);
            }
            if (!possibleTriggerOwners.isEmpty()) {
                try {
                    MissionTrigger[] missionTriggerArray;
                    Player player = Players.getInstance().getPlayer((Long)possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
                    MissionTriggers.activateTriggers((Creature)player, this.getTemplate().getTemplateId(), 491, this.getWurmId(), 1);
                    for (MissionTrigger t3 : missionTriggerArray = MissionTriggers.getMissionTriggersWith(this.getTemplate().getTemplateId(), 491, this.getWurmId())) {
                        EpicMissionEnum missionEnum;
                        EpicMission em = EpicServerStatus.getEpicMissionForMission(t3.getMissionRequired());
                        if (em == null || (missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType())) == null || !EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)) continue;
                        float karmaSplit = missionEnum.getKarmaBonusDiffMult() * em.getDifficulty();
                        float karmaGained = karmaSplit / (float)EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                        karmaGained = (float)Math.ceil(karmaGained / (float)possibleTriggerOwners.size());
                        Iterator iterator2 = possibleTriggerOwners.iterator();
                        while (iterator2.hasNext()) {
                            long id = (Long)iterator2.next();
                            try {
                                Player p = Players.getInstance().getPlayer(id);
                                if (Deities.getFavoredKingdom(em.getEpicEntityId()) != p.getKingdomTemplateId() && Servers.localServer.EPIC) continue;
                                MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                                if (mp == null) {
                                    mp = MissionPerformed.startNewMission(t3.getMissionRequired(), id, 1.0f);
                                } else {
                                    MissionPerformed mperf = mp.getMission(t3.getMissionRequired());
                                    if (mperf == null) {
                                        MissionPerformed.startNewMission(t3.getMissionRequired(), id, 1.0f);
                                    }
                                }
                                p.modifyKarma((int)karmaGained);
                                if (!p.isPaying()) continue;
                                p.setScenarioKarma((int)((float)p.getScenarioKarma() + karmaGained));
                                if (!Servers.localServer.EPIC) continue;
                                WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(WurmId.getNextWCCommandId(), new long[]{p.getWurmId()}, new int[]{p.getScenarioKarma()}, em.getEpicEntityId());
                                wcek.sendToLoginServer();
                            }
                            catch (NoSuchPlayerException noSuchPlayerException) {}
                        }
                    }
                    activatedTrigger = true;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
            for (Map.Entry entry : this.attackers.entrySet()) {
                long attackerId = (Long)entry.getKey();
                if (WurmId.getType(attackerId) != 0) continue;
                pvp = true;
                try {
                    Player player = Players.getInstance().getPlayer(attackerId);
                    if (this.isFriendlyKingdom(player.getKingdomId()) || !this.isKingdomGuard()) continue;
                    player.achievement(266);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
            }
        }
        pantLess.remove(this.getWurmId());
        return pvp || lSkillReceivers != null && lSkillReceivers.size() > 0;
    }

    @Nullable
    public Creature getTarget() {
        Creature toReturn = null;
        if (this.target != -10L) {
            try {
                toReturn = Server.getInstance().getCreature(this.target);
            }
            catch (NoSuchCreatureException nsc) {
                this.setTarget(-10L, true);
            }
            catch (NoSuchPlayerException nsp) {
                this.setTarget(-10L, true);
            }
        }
        return toReturn;
    }

    public void setDeathEffects(boolean freeDeath, int dtilex, int dtiley) {
        boolean respawn = false;
        this.removeWoundMod();
        this.modifyFightSkill(dtilex, dtiley);
        if (this.isSpiritGuard() && this.citizenVillage != null) {
            respawn = true;
        } else if (this.isKingdomGuard() || this.isNpc() && this.isRespawn()) {
            respawn = true;
        }
        if (respawn) {
            this.setDestroyed();
            if (this.name.endsWith("traitor")) {
                try {
                    this.setName(this.getNameWithoutPrefixes());
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, this.getName() + ", " + this.getWurmId() + ": failed to remove traitor name.");
                }
            }
            try {
                this.status.setDead(true);
            }
            catch (IOException ioex) {
                logger.log(Level.WARNING, this.getName() + ", " + this.getWurmId() + ": Set dead manually.");
            }
            if (this.isSpiritGuard()) {
                Village vil = this.citizenVillage;
                if (vil != null) {
                    vil.deleteGuard(this, false);
                    vil.plan.returnGuard(this);
                } else {
                    this.destroy();
                }
            } else if (this.isKingdomGuard()) {
                GuardTower tower = Kingdoms.getTower(this);
                if (tower != null) {
                    try {
                        tower.returnGuard(this);
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, iox.getMessage(), iox);
                    }
                } else {
                    logger.log(Level.INFO, this.getName() + ", " + this.getWurmId() + " without tower, destroying.");
                    this.destroy();
                }
            } else {
                this.respawnCounter = 600;
            }
        } else {
            this.destroy();
        }
        this.getStatus().setStunned(0.0f, false);
        this.trimAttackers(true);
    }

    public void respawn() {
        if (this.getVisionArea() == null) {
            try {
                if (!this.isNpc()) {
                    if (this.skills.getSkill(10052).getKnowledge(0.0) > this.template.getSkills().getSkill(10052).getKnowledge(0.0) * 2.0 || 100.0 - this.skills.getSkill(10052).getKnowledge(0.0) < 30.0 || this.skills.getSkill(10052).getKnowledge(0.0) < this.template.getSkills().getSkill(10052).getKnowledge(0.0) / 2.0) {
                        this.skills.delete();
                        this.skills.clone(this.template.getSkills().getSkills());
                        this.skills.save();
                        this.getStatus().age = 0;
                    } else if (this.getStatus().age >= this.template.getMaxAge() - 1) {
                        this.getStatus().age = 0;
                    }
                }
                this.status.setDead(false);
                this.pollCounter = 0;
                this.lastPolled = 0;
                this.setDisease((byte)0);
                this.getStatus().removeWounds();
                this.getStatus().modifyStamina(65535.0f);
                this.getStatus().refresh(0.5f, false);
                this.createVisionArea();
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, this.getName() + ":" + ex.getMessage(), ex);
            }
        } else {
            logger.log(Level.WARNING, this.getName() + " already has a visionarea.", new Exception());
        }
        Server.getInstance().broadCastAction(this.getNameWithGenus() + " has arrived.", this, 10);
    }

    public boolean hasColoredChat() {
        return false;
    }

    public int getCustomGreenChat() {
        return 140;
    }

    public int getCustomRedChat() {
        return 255;
    }

    public int getCustomBlueChat() {
        return 0;
    }

    public final boolean isFaithful() {
        return this.faithful;
    }

    public boolean isFighting() {
        return this.opponent != null;
    }

    public MovementScheme getMovementScheme() {
        return this.movementScheme;
    }

    public boolean isOnGround() {
        return this.movementScheme.onGround;
    }

    public void pollStamina() {
        this.staminaPollCounter = Math.max(0, --this.staminaPollCounter);
        if (this.staminaPollCounter == 0) {
            if (!this.isUndead() && WurmId.getType(this.id) == 0) {
                int hunger;
                int tile;
                int hungMod = 4;
                int thirstMod = (int)(5.0f * ItemBonus.getReplenishBonus(this));
                if (this.getSpellEffects() != null && this.getSpellEffects().getSpellEffect((byte)74) != null) {
                    hungMod = 2;
                    thirstMod = 2;
                }
                hungMod = (int)((float)hungMod * ItemBonus.getReplenishBonus(this));
                boolean reduceHunger = true;
                if (this.getDeity() != null && this.getDeity().number == 4 && this.isOnSurface() && Tiles.getTile(Tiles.decodeType(tile = Server.surfaceMesh.getTile(this.getTileX(), this.getTileY()))).isMycelium()) {
                    reduceHunger = false;
                }
                if (reduceHunger) {
                    this.status.decreaseCCFPValues();
                    hunger = this.status.modifyHunger((int)((float)hungMod * (2.0f - this.status.getNutritionlevel())), 1.0f);
                } else {
                    hunger = this.status.modifyHunger(-4, 0.99f);
                }
                int thirst = this.status.modifyThirst(thirstMod);
                float hungpercent = 1.0f;
                if (hunger > 45000) {
                    hungpercent = Math.max(1.0f, (float)(65535 - hunger)) / 20535.0f;
                    hungpercent *= hungpercent;
                }
                float thirstpercent = Math.max((float)(65535 - thirst), 1.0f) / 65535.0f;
                thirstpercent = thirstpercent * thirstpercent * thirstpercent;
                if (this.status.hasNormalRegen() && !this.isFighting()) {
                    float toModify = 0.6f;
                    if (this.isStealth()) {
                        toModify = 0.06f;
                    }
                    toModify = toModify * hungpercent * thirstpercent;
                    double staminaModifier = this.status.getModifierValuesFor(1);
                    if (this.getDeity() != null && this.getDeity().isStaminaBonus() && this.getFaith() >= 20.0f && this.getFavor() >= 10.0f) {
                        staminaModifier += 0.25;
                    }
                    if (this.hasSpiritStamina) {
                        staminaModifier *= 1.1;
                    }
                    toModify = this.hasSleepBonus() ? Math.max(0.006f, toModify * (float)(1.0 + staminaModifier) * 3.0f) : Math.max(0.004f, toModify * (float)(1.0 + staminaModifier));
                    if (this.hasSpellEffect((byte)95)) {
                        toModify *= 0.5f;
                    }
                    if (this.getPower() == 0 && this.getVehicle() == -10L && (double)(this.getPositionZ() + this.getAltOffZ()) < -1.45 || this.isUsingLastGasp()) {
                        toModify = 0.0f;
                    } else {
                        this.status.modifyStamina2(toModify);
                    }
                }
                this.status.setNormalRegen(true);
            } else {
                if (this.isNeedFood()) {
                    if (Server.rand.nextInt(600) == 0) {
                        if (this.hasTrait(14) || this.isPregnant()) {
                            this.status.modifyHunger(1500, 1.0f);
                        } else if (!this.isCarnivore()) {
                            this.status.modifyHunger(700, 1.0f);
                        } else {
                            this.status.modifyHunger(150, 1.0f);
                        }
                    }
                } else {
                    this.status.modifyHunger(-1, 0.5f);
                }
                if ((this.isRegenerating() || this.isUnique()) && Server.rand.nextInt(10) == 0) {
                    this.healTick();
                }
                if (Server.rand.nextInt(100) == 0) {
                    if (!this.isFighting() || this.isUnique()) {
                        this.status.resetCreatureStamina();
                    }
                    if (!(this.isSwimming() || this.isUnique() || this.isSubmerged() || !((double)(this.getPositionZ() + this.getAltOffZ()) <= -1.25) || this.getVehicle() != -10L || this.hitchedTo != null || this.isRidden() || this.getLeader() != null || Tiles.isSolidCave(Tiles.decodeType(this.getCurrentTileNum())))) {
                        this.addWoundOfType(null, (byte)7, 2, false, 1.0f, false, 4000.0f + Server.rand.nextFloat() * 3000.0f, 0.0f, 0.0f, false, false);
                    }
                }
                this.status.setNormalRegen(true);
            }
        }
    }

    public void sendDeityEffectBonuses() {
    }

    public void sendRemoveDeityEffectBonus(int effectNumber) {
    }

    public void sendAddDeityEffectBonus(int effectNumber) {
    }

    public final boolean checkPregnancy(boolean insta) {
        Offspring offspring = Offspring.getOffspring(this.getWurmId());
        if (!(offspring == null || offspring.isChecked() && !insta || Server.rand.nextInt(4) != 0 && !insta)) {
            float creatureRatio = 10.0f;
            if (this.getCurrentVillage() != null) {
                creatureRatio = this.getCurrentVillage().getCreatureRatio();
            }
            if ((this.status.hunger > 60000 && this.status.fat <= 2 || creatureRatio < Village.OPTIMUMCRETRATIO && Server.rand.nextInt(Math.max((int)(creatureRatio / 2.0f), 1)) == 0) && Server.rand.nextInt(3) == 0) {
                Offspring.deleteSettings(this.getWurmId());
                this.getCommunicator().sendAlertServerMessage("You suddenly bleed immensely and lose your unborn child due to malnourishment!");
                Server.getInstance().broadCastAction(this.getNameWithGenus() + " bleeds immensely due to miscarriage.", this, 5);
                if (Server.rand.nextInt(5) == 0) {
                    this.die(false, "Miscarriage");
                }
                return false;
            }
            if (offspring.decreaseDaysLeft()) {
                try {
                    try {
                        byte sex;
                        int cid = this.template.getChildTemplateId();
                        if (cid <= 0) {
                            cid = this.template.getTemplateId();
                        }
                        CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(cid);
                        String newname = temp.getName();
                        byte by = sex = temp.keepSex ? temp.getSex() : (byte)Server.rand.nextInt(2);
                        if (this.isHorse()) {
                            newname = Server.rand.nextBoolean() ? Offspring.generateGenericName() : (sex == 1 ? Offspring.generateFemaleName() : Offspring.generateMaleName());
                            newname = LoginHandler.raiseFirstLetter(newname);
                        }
                        if (this.isUnicorn()) {
                            newname = Server.rand.nextBoolean() ? Offspring.generateGenericName() : (sex == 1 ? Offspring.generateFemaleUnicornName() : Offspring.generateMaleUnicornName());
                            newname = LoginHandler.raiseFirstLetter(newname);
                        }
                        boolean zombie = false;
                        if (cid == 66) {
                            zombie = true;
                            newname = sex == 1 ? LoginHandler.raiseFirstLetter("Daughter of " + this.name) : LoginHandler.raiseFirstLetter("Son of " + this.name);
                            if (this.getKingdomTemplateId() != 3) {
                                cid = 25;
                                zombie = false;
                            }
                        }
                        Creature newCreature = Creature.doNew(cid, true, this.getPosX(), this.getPosY(), Server.rand.nextFloat() * 360.0f, this.getLayer(), newname, sex, this.isAggHuman() ? this.getKingdomId() : (byte)0, Server.rand.nextBoolean() ? this.getStatus().modtype : (byte)0, zombie, (byte)1);
                        this.getCommunicator().sendAlertServerMessage("You give birth to " + newCreature.getName() + "!");
                        newCreature.getStatus().setTraitBits(offspring.getTraits());
                        newCreature.getStatus().setInheritance(offspring.getTraits(), offspring.getMother(), offspring.getFather());
                        newCreature.getStatus().saveCreatureName(newname);
                        if (zombie) {
                            Skill[] cskills;
                            if (this.getPet() != null) {
                                this.getCommunicator().sendNormalServerMessage(this.getPet().getNameWithGenus() + " stops following you.");
                                if (this.getPet().getLeader() == this) {
                                    this.getPet().setLeader(null);
                                }
                                this.getPet().setDominator(-10L);
                                this.setPet(-10L);
                            }
                            newCreature.setDominator(this.getWurmId());
                            newCreature.setLoyalty(100.0f);
                            this.setPet(newCreature.getWurmId());
                            newCreature.getSkills().delete();
                            newCreature.getSkills().clone(this.skills.getSkills());
                            for (Skill lCskill : cskills = newCreature.getSkills().getSkills()) {
                                lCskill.setKnowledge(Math.min(40.0, lCskill.getKnowledge() * 0.5), false);
                            }
                            newCreature.getSkills().save();
                        }
                        newCreature.refreshVisible();
                        Server.getInstance().broadCastAction(this.getNameWithGenus() + " gives birth to " + newCreature.getNameWithGenus() + "!", this, 5);
                        return true;
                    }
                    catch (NoSuchCreatureTemplateException nst) {
                        logger.log(Level.WARNING, this.getName() + " gives birth to nonexistant template:" + this.template.getChildTemplateId());
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
        }
        return false;
    }

    private long getTraits() {
        return this.status.traits;
    }

    public void mate(Creature father, @Nullable Creature breeder) {
        boolean inbred = false;
        if (father.getFather() == this.getFather() || father.getMother() == this.getMother() || father.getWurmId() == this.getFather() || father.getMother() == this.getWurmId()) {
            inbred = true;
        }
        new Offspring(this.getWurmId(), father.getWurmId(), breeder == null ? Traits.calcNewTraits(inbred, this.getTraits(), father.getTraits()) : Traits.calcNewTraits(breeder.getAnimalHusbandrySkillValue(), inbred, this.getTraits(), father.getTraits()), (byte)(this.template.daysOfPregnancy + Server.rand.nextInt(5)), false);
        logger.log(Level.INFO, this.getName() + " gender=" + this.getSex() + " just got pregnant with " + father.getName() + " gender=" + father.getSex() + ".");
    }

    public boolean isBred() {
        return this.hasTrait(63);
    }

    static boolean isInbred(Creature maleCreature, Creature femaleCreature) {
        return maleCreature.getFather() == femaleCreature.getFather() || maleCreature.getMother() == femaleCreature.getMother() || maleCreature.getWurmId() == femaleCreature.getFather() || maleCreature.getMother() == femaleCreature.getWurmId();
    }

    public boolean isPregnant() {
        return this.getOffspring() != null;
    }

    public Offspring getOffspring() {
        return Offspring.getOffspring(this.getWurmId());
    }

    private void healTick() {
        if (this.status.damage > 0) {
            try {
                Wound[] w = this.getBody().getWounds().getWounds();
                if (w.length > 0) {
                    w[0].modifySeverity(-300);
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    public void wearItems() {
        Item[] invarr;
        Item inventory = this.getInventory();
        Body body = this.getBody();
        Set<Item> invitems = inventory.getItems();
        block8: for (Item lElement : invarr = invitems.toArray(new Item[invitems.size()])) {
            byte[] places;
            if (lElement.isWeapon() && (!this.isPlayer() || lElement.getTemplateId() != 7 && !lElement.isWeaponKnife())) {
                try {
                    int rslot = this.isPlayer() ? 38 : 14;
                    Item bodyPart = body.getBodyPart(rslot);
                    if (bodyPart.testInsertItem(lElement)) {
                        Item parent = lElement.getParent();
                        parent.dropItem(lElement.getWurmId(), false);
                        bodyPart.insertItem(lElement);
                        continue;
                    }
                    int lslot = this.isPlayer() ? 37 : 13;
                    bodyPart = body.getBodyPart(lslot);
                    if (!bodyPart.testInsertItem(lElement)) continue;
                    Item parent = lElement.getParent();
                    parent.dropItem(lElement.getWurmId(), false);
                    bodyPart.insertItem(lElement);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, this.getName() + " " + nsi.getMessage(), nsi);
                }
                catch (NoSpaceException nsp) {
                    logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
                }
                continue;
            }
            if (lElement.isShield()) {
                try {
                    Item bodyPart = body.getBodyPart(44);
                    bodyPart.insertItem(lElement);
                }
                catch (NoSpaceException e) {
                    e.printStackTrace();
                }
                continue;
            }
            for (byte lPlace : places = lElement.getBodySpaces()) {
                try {
                    Item bodyPart = body.getBodyPart(lPlace);
                    if (!bodyPart.testInsertItem(lElement)) continue;
                    Item parent = lElement.getParent();
                    parent.dropItem(lElement.getWurmId(), false);
                    bodyPart.insertItem(lElement);
                    continue block8;
                }
                catch (NoSpaceException nsp) {
                    if (Servers.localServer.testServer || lPlace == 28) continue;
                    logger.log(Level.WARNING, this.getName() + ":" + nsp.getMessage(), nsp);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, this.getName() + ":" + nsi.getMessage(), nsi);
                }
            }
        }
    }

    public float getStaminaMod() {
        int hunger = this.status.getHunger();
        int thirst = this.status.getThirst();
        float newhungpercent = 1.0f;
        if (hunger > 45000) {
            newhungpercent = Math.max(1.0f, (float)(65535 - hunger)) / 20535.0f;
            newhungpercent *= newhungpercent;
        }
        float thirstpercent = Math.max((float)(65535 - thirst), 1.0f) / 65535.0f;
        thirstpercent = thirstpercent * thirstpercent * thirstpercent;
        return 1.0f - newhungpercent * thirstpercent;
    }

    public Skills getSkills() {
        return this.skills;
    }

    public double getSoulStrengthVal() {
        return this.getSoulStrength().getKnowledge(0.0);
    }

    public Skill getClimbingSkill() {
        try {
            return this.skills.getSkill(10073);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(10073, 1.0f);
        }
    }

    public double getLockPickingSkillVal() {
        try {
            return this.skills.getSkill(10076).getKnowledge(0.0);
        }
        catch (NoSuchSkillException nss) {
            return 1.0;
        }
    }

    public double getLockSmithingSkill() {
        try {
            return this.skills.getSkill(10034).getKnowledge(0.0);
        }
        catch (NoSuchSkillException nss) {
            return 1.0;
        }
    }

    public double getStrengthSkill() {
        try {
            if (this.isPlayer()) {
                return this.skills.getSkill(102).getKnowledge(0.0);
            }
            return this.skills.getSkill(102).getKnowledge();
        }
        catch (NoSuchSkillException nss) {
            return 1.0;
        }
    }

    public Skill getStealSkill() {
        try {
            return this.skills.getSkill(10075);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(10075, 1.0f);
        }
    }

    public Skill getStaminaSkill() {
        try {
            return this.skills.getSkill(103);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(103, 1.0f);
        }
    }

    public final double getAnimalHusbandrySkillValue() {
        try {
            return this.skills.getSkill(10085).getKnowledge(0.0);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(10085, 1.0f).getKnowledge(0.0);
        }
    }

    public double getBodyControl() {
        try {
            return this.skills.getSkill(104).getKnowledge(0.0);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(104, 1.0f).getKnowledge(0.0);
        }
    }

    public Skill getBodyControlSkill() {
        try {
            return this.skills.getSkill(104);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(104, 1.0f);
        }
    }

    public Skill getFightingSkill() {
        if (!this.isPlayer()) {
            return this.getWeaponLessFightingSkill();
        }
        try {
            return this.skills.getSkill(1023);
        }
        catch (NoSuchSkillException nss) {
            return this.skills.learn(1023, 1.0f);
        }
    }

    public Skill getWeaponLessFightingSkill() {
        try {
            return this.skills.getSkill(10052);
        }
        catch (NoSuchSkillException nss) {
            try {
                return this.skills.learn(10052, (float)this.template.getSkills().getSkill(10052).getKnowledge(0.0));
            }
            catch (NoSuchSkillException nss2) {
                logger.log(Level.WARNING, "Template for " + this.getName() + " has no weaponless skill?");
                return this.skills.learn(10052, 20.0f);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage() + " template for " + this.getName() + " has skills?");
                return this.skills.learn(10052, 20.0f);
            }
        }
    }

    public byte getAttitude(Creature aTarget) {
        if (this.opponent == aTarget) {
            return 2;
        }
        if (aTarget.isNpc() && this.isNpc() && aTarget.getKingdomId() == this.getKingdomId()) {
            return 1;
        }
        if (this.isDominated()) {
            if (this.getDominator() != null) {
                if (this.getDominator() == aTarget) {
                    return 1;
                }
                if (this.getDominator() == aTarget.getDominator()) {
                    return 1;
                }
                return aTarget.getAttitude(this.getDominator());
            }
            if (this.getLoyalty() > 0.0f && (aTarget.getReputation() >= 0 || aTarget.getKingdomTemplateId() == 3) && this.isFriendlyKingdom(aTarget.getKingdomId())) {
                return 0;
            }
        }
        if (aTarget.isDominated()) {
            Creature lDominator = aTarget.getDominator();
            if (lDominator != null) {
                if (lDominator == this) {
                    return 1;
                }
                if (aTarget.isHorse() && aTarget.isRidden()) {
                    if (this.isHungry() && this.isCarnivore()) {
                        if (Server.rand.nextInt(5) == 0) {
                            for (Long riderLong : aTarget.getRiders()) {
                                try {
                                    Creature rider = Server.getInstance().getCreature(riderLong);
                                    if (this.getAttitude(rider) != 2) continue;
                                    return 2;
                                }
                                catch (Exception ex) {
                                    logger.log(Level.WARNING, ex.getMessage());
                                }
                            }
                        }
                        return 0;
                    }
                } else {
                    return this.getAttitude(lDominator);
                }
            }
            if (this.isFriendlyKingdom(aTarget.getKingdomId()) && aTarget.getLoyalty() > 0.0f) {
                return 0;
            }
        }
        if (this.getPet() != null && aTarget == this.getPet()) {
            return 1;
        }
        if (this.isInvulnerable()) {
            return 0;
        }
        if (aTarget.isInvulnerable()) {
            return 0;
        }
        if (!this.isPlayer() && aTarget.getCultist() != null) {
            if (aTarget.getCultist().hasFearEffect()) {
                return 0;
            }
            if (aTarget.getCultist().hasLoveEffect()) {
                return 1;
            }
        }
        if (this.isReborn() && !aTarget.equals(this.getTarget()) && !aTarget.equals(this.opponent) && aTarget.getKingdomId() == this.getKingdomId()) {
            return 0;
        }
        if (this.onlyAttacksPlayers() && !aTarget.isPlayer()) {
            return 0;
        }
        if (!this.isPlayer() && aTarget.onlyAttacksPlayers()) {
            return 0;
        }
        if (Servers.isThisAChaosServer() && this.getCitizenVillage() != null && this.getCitizenVillage().isEnemy(aTarget)) {
            return 2;
        }
        if (this.isAggHuman()) {
            if (aTarget instanceof Player) {
                boolean atta = true;
                if (this.isAnimal() && aTarget.getDeity() != null && aTarget.getDeity().isBefriendCreature() && aTarget.getFaith() > 60.0f && aTarget.getFavor() >= 30.0f) {
                    atta = false;
                }
                if (this.isMonster() && !this.isUnique() && aTarget.getDeity() != null && aTarget.getDeity().isBefriendMonster() && aTarget.getFaith() > 60.0f && aTarget.getFavor() >= 30.0f) {
                    atta = false;
                }
                if (this.getLoyalty() > 0.0f && (aTarget.getReputation() >= 0 || aTarget.getKingdomTemplateId() == 3) && this.isFriendlyKingdom(aTarget.getKingdomId())) {
                    atta = false;
                }
                if (atta) {
                    return 2;
                }
            } else if (aTarget.isSpiritGuard() && aTarget.getCitizenVillage() == null || aTarget.isKingdomGuard()) {
                if (!(!(this.getLoyalty() <= 0.0f) || this.isUnique() || this.isHorse() && this.isRidden())) {
                    return 2;
                }
            } else if (aTarget.isRidden()) {
                if (this.isHungry() && this.isCarnivore() && Server.rand.nextInt(5) == 0) {
                    for (Long riderLong : aTarget.getRiders()) {
                        try {
                            Creature rider = Server.getInstance().getCreature(riderLong);
                            if (this.getAttitude(rider) != 2) continue;
                            return 2;
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, ex.getMessage());
                        }
                    }
                }
                return 0;
            }
        } else {
            if (aTarget.getKingdomId() != 0 && !this.isFriendlyKingdom(aTarget.getKingdomId()) && (this.isDefendKingdom() || this.isAggWhitie() && aTarget.getKingdomTemplateId() != 3)) {
                return 2;
            }
            if (this.isSpiritGuard()) {
                if (this.citizenVillage != null) {
                    if (aTarget instanceof Player) {
                        if (this.citizenVillage.isEnemy(aTarget.citizenVillage)) {
                            return 2;
                        }
                        if (this.citizenVillage.getReputation(aTarget) <= -30) {
                            return 2;
                        }
                        if (this.citizenVillage.isEnemy(aTarget)) {
                            return 2;
                        }
                        if (this.citizenVillage.isAlly(aTarget)) {
                            return 1;
                        }
                        if (this.citizenVillage.isCitizen(aTarget)) {
                            return 1;
                        }
                        if (!this.isFriendlyKingdom(aTarget.getKingdomId())) {
                            return 2;
                        }
                        return 0;
                    }
                    if (aTarget.getKingdomId() != 0) {
                        if (!this.isFriendlyKingdom(this.getKingdomId())) {
                            return 2;
                        }
                        return 0;
                    }
                    if (aTarget.isRidden()) {
                        for (Long riderLong : aTarget.getRiders()) {
                            try {
                                Creature rider = Server.getInstance().getCreature(riderLong);
                                if (this.isFriendlyKingdom(rider.getKingdomId())) continue;
                                return 2;
                            }
                            catch (Exception ex) {
                                logger.log(Level.WARNING, ex.getMessage());
                            }
                        }
                        return 0;
                    }
                }
            } else if (this.isKingdomGuard()) {
                if (aTarget.getKingdomId() != 0) {
                    Village lVill;
                    if (!this.isFriendlyKingdom(aTarget.getKingdomId())) {
                        return 2;
                    }
                    if (aTarget.getKingdomTemplateId() != 3 && aTarget.getReputation() <= -100) {
                        return 2;
                    }
                    if (aTarget.isPlayer() && (lVill = Villages.getVillageWithPerimeterAt(this.getTileX(), this.getTileY(), true)) != null && lVill.kingdom == this.getKingdomId() && lVill.isEnemy(aTarget)) {
                        return 2;
                    }
                } else if (aTarget.isAggHuman() && !aTarget.isUnique() && aTarget.getCurrentKingdom() == this.getKingdomId() && aTarget.getLoyalty() <= 0.0f && !aTarget.isRidden()) {
                    return 2;
                }
                if (aTarget.isRidden()) {
                    for (Long riderLong : aTarget.getRiders()) {
                        try {
                            Creature rider = Server.getInstance().getCreature(riderLong);
                            if (this.getAttitude(rider) != 2) continue;
                            return 2;
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, ex.getMessage());
                        }
                    }
                }
            }
        }
        if (this.isCarnivore() && aTarget.isPrey() && Server.rand.nextInt(10) == 0 && this.canEat() && aTarget.getCurrentVillage() == null && aTarget.getHitched() == null) {
            return 2;
        }
        return 0;
    }

    public final byte getCurrentKingdom() {
        return Zones.getKingdom(this.getTileX(), this.getTileY());
    }

    public boolean isFriendlyKingdom(byte targetKingdom) {
        if (this.getKingdomId() == 0 || targetKingdom == 0) {
            return false;
        }
        if (this.getKingdomId() == targetKingdom) {
            return true;
        }
        Kingdom myKingd = Kingdoms.getKingdom(this.getKingdomId());
        if (myKingd != null) {
            return myKingd.isAllied(targetKingdom);
        }
        return false;
    }

    public Possessions getPossessions() {
        return this.possessions;
    }

    public Item getInventory() {
        if (this.possessions != null) {
            return this.possessions.getInventory();
        }
        logger.warning("Posessions was null for " + this.id);
        return null;
    }

    public Optional<Item> getInventoryOptional() {
        if (this.possessions != null) {
            return Optional.ofNullable(this.possessions.getInventory());
        }
        logger.warning("Posessions was null for " + this.id);
        return Optional.empty();
    }

    public static final Item createItem(int templateId, float qualityLevel) throws Exception {
        Item item = ItemFactory.createItem(templateId, qualityLevel, (byte)0, (byte)0, null);
        return item;
    }

    @Override
    public void save() throws IOException {
        this.possessions.save();
        this.status.save();
        this.skills.save();
    }

    public void savePosition(int zoneid) throws IOException {
        this.status.savePosition(this.id, false, zoneid, false);
    }

    public boolean isGuest() {
        return this.guest;
    }

    public void setGuest(boolean g) {
        this.guest = g;
    }

    public CreatureTemplate getTemplate() {
        return this.template;
    }

    public void refreshAttitudes() {
        if (this.visionArea != null) {
            this.visionArea.refreshAttitudes();
        }
        if (this.currentTile != null) {
            this.currentTile.checkChangedAttitude(this);
        }
    }

    public static Creature doNew(int templateid, byte ctype, float aPosX, float aPosY, float aRot, int layer, String name, byte gender) throws Exception {
        return Creature.doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, (byte)0, ctype, false);
    }

    public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender) throws Exception {
        return Creature.doNew(templateid, aPosX, aPosY, aRot, layer, name, gender, (byte)0);
    }

    public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom) throws Exception {
        return Creature.doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, kingdom, (byte)0, false);
    }

    public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn) throws Exception {
        return Creature.doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, (byte)0);
    }

    public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age) throws Exception {
        return Creature.doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, age, 0);
    }

    public static Creature doNew(int templateid, boolean createPossessions, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom, byte ctype, boolean reborn, byte age, int floorLevel) throws Exception {
        Creature toReturn = !reborn && (templateid == 1 || templateid == 113) ? new Npc(CreatureTemplateFactory.getInstance().getTemplate(templateid)) : new Creature(CreatureTemplateFactory.getInstance().getTemplate(templateid));
        long wid = WurmId.getNextCreatureId();
        try {
            while (Creatures.getInstance().getCreature(wid) != null) {
                wid = WurmId.getNextCreatureId();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        toReturn.setWurmId(wid, aPosX, aPosY, Creature.normalizeAngle(aRot), layer);
        if (name.length() > 0) {
            toReturn.setName(name);
        }
        if (toReturn.getTemplate().isRoyalAspiration()) {
            if (toReturn.getTemplate().getTemplateId() == 62) {
                kingdom = 1;
            } else if (toReturn.getTemplate().getTemplateId() == 63) {
                kingdom = (byte)3;
            }
        }
        if (reborn) {
            toReturn.getStatus().reborn = true;
        }
        if (floorLevel > 0) {
            toReturn.pushToFloorLevel(floorLevel);
        } else {
            toReturn.setPositionZ(toReturn.calculatePosZ());
        }
        toReturn.getStatus().age = age <= 0 ? (int)(1.0f + Server.rand.nextFloat() * (float)Math.min(48, toReturn.getTemplate().getMaxAge())) : (int)age;
        if (toReturn.isGhost() || toReturn.isKingdomGuard() || reborn) {
            toReturn.getStatus().age = 12;
        }
        if (ctype != 0) {
            toReturn.getStatus().modtype = ctype;
        }
        if (toReturn.isUnique()) {
            toReturn.getStatus().age = 12 + (int)(Server.rand.nextFloat() * (float)(toReturn.getTemplate().getMaxAge() - 12));
        }
        toReturn.getStatus().kingdom = kingdom;
        if (Kingdoms.getKingdom(kingdom) != null && Kingdoms.getKingdom(kingdom).getTemplate() == 3) {
            toReturn.setAlignment(-50.0f);
            toReturn.setDeity(Deities.getDeity(4));
            toReturn.setFaith(1.0f);
        }
        toReturn.setSex(gender, true);
        Creatures.getInstance().addCreature(toReturn, false, false);
        toReturn.loadSkills();
        toReturn.createPossessions();
        toReturn.getBody().createBodyParts();
        if (!toReturn.isAnimal() && createPossessions) {
            Creature.createBasicItems(toReturn);
            toReturn.wearItems();
        }
        if ((toReturn.isHorse() || toReturn.getTemplate().isBlackOrWhite) && Server.rand.nextInt(10) == 0) {
            Creature.setRandomColor(toReturn);
        }
        Creatures.getInstance().sendToWorld(toReturn);
        toReturn.createVisionArea();
        toReturn.save();
        if (reborn) {
            toReturn.getStatus().setReborn(true);
        }
        if (ctype != 0) {
            toReturn.getStatus().setType(ctype);
        }
        toReturn.getStatus().setKingdom(kingdom);
        if (kingdom == 3) {
            toReturn.setAlignment(-50.0f);
            toReturn.setDeity(Deities.getDeity(4));
            toReturn.setFaith(1.0f);
        }
        if (templateid != 119) {
            Server.getInstance().broadCastAction(toReturn.getNameWithGenus() + " has arrived.", toReturn, 10);
        }
        if (toReturn.isUnique()) {
            Server.getInstance().broadCastSafe("Rumours of " + toReturn.getName() + " are starting to spread.");
            Servers.localServer.spawnedUnique();
            logger.log(Level.INFO, "Unique " + toReturn.getName() + " spawned @ " + toReturn.getTileX() + ", " + toReturn.getTileY() + ", wurmID = " + toReturn.getWurmId());
        }
        if (toReturn.getTemplate().getCreatureAI() != null) {
            toReturn.getTemplate().getCreatureAI().creatureCreated(toReturn);
        }
        return toReturn;
    }

    public float getSecondsPlayed() {
        return 1.0f;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void createBasicItems(Creature toReturn) {
        try {
            Item inventory = toReturn.getInventory();
            if (toReturn.getTemplate().getTemplateId() == 11) {
                Item club = Creature.createItem(314, 45.0f);
                inventory.insertItem(club);
                Item paper = Creature.getRareRecipe("Da Wife", 1250, 1251, 1252, 1253);
                if (paper == null) return;
                inventory.insertItem(paper);
                return;
            }
            if (toReturn.getTemplate().getTemplateId() == 23) {
                Item paper = Creature.getRareRecipe("Granny Gobin", 1255, 1256, 1257, 1258);
                if (paper == null) return;
                inventory.insertItem(paper);
                return;
            }
            if (toReturn.getTemplate().getTemplateId() == 75) {
                Item swo = Creature.createItem(81, 85.0f);
                ItemSpellEffects effs = new ItemSpellEffects(swo.getWurmId());
                effs.addSpellEffect(new SpellEffect(swo.getWurmId(), 33, 90.0f, 20000000));
                inventory.insertItem(swo);
                Item helmOne = Creature.createItem(285, 75.0f);
                Item helmTwo = Creature.createItem(285, 75.0f);
                helmOne.setMaterial((byte)9);
                helmTwo.setMaterial((byte)9);
                inventory.insertItem(helmOne);
                inventory.insertItem(helmTwo);
                return;
            }
            if (!toReturn.isUnique()) return;
            if (toReturn.getTemplate().getTemplateId() == 26) {
                Item sword = Creature.createItem(80, 45.0f);
                inventory.insertItem(sword);
                Item shield = Creature.createItem(4, 45.0f);
                inventory.insertItem(shield);
                Item goboHat = Creature.createItem(1014, 55.0f);
                inventory.insertItem(goboHat);
                return;
            }
            if (toReturn.getTemplate().getTemplateId() == 27) {
                Item club = Creature.createItem(314, 65.0f);
                inventory.insertItem(club);
                Item trollCrown = Creature.createItem(1015, 70.0f);
                inventory.insertItem(trollCrown);
                return;
            }
            if (toReturn.getTemplate().getTemplateId() == 22 || toReturn.getTemplate().getTemplateId() == 20) {
                Item club = Creature.createItem(314, 65.0f);
                inventory.insertItem(club);
                return;
            }
            if (!CreatureTemplate.isDragonHatchling(toReturn.getTemplate().getTemplateId()) && !CreatureTemplate.isFullyGrownDragon(toReturn.getTemplate().getTemplateId())) return;
        }
        catch (Exception ex) {
            logger.log(Level.INFO, "Failed to create items for creature.", ex);
        }
    }

    public Item getPrimWeapon() {
        return this.getPrimWeapon(false);
    }

    public Item getPrimWeapon(boolean onlyBodyPart) {
        Item primWeapon = null;
        if (this.isAnimal()) {
            try {
                if (this.getHandDamage() > 0.0f) {
                    return this.getEquippedWeapon((byte)14);
                }
                if (this.getKickDamage() > 0.0f) {
                    return this.getEquippedWeapon((byte)34);
                }
                if (this.getHeadButtDamage() > 0.0f) {
                    return this.getEquippedWeapon((byte)1);
                }
                if (this.getBiteDamage() > 0.0f) {
                    return this.getEquippedWeapon((byte)29);
                }
                if (this.getBreathDamage() > 0.0f) {
                    return this.getEquippedWeapon((byte)2);
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, this.getName() + nsp.getMessage(), nsp);
            }
        } else {
            try {
                byte slot = this.isPlayer() ? (byte)38 : 14;
                primWeapon = this.getEquippedWeapon(slot, true);
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
        }
        if (primWeapon == null) {
            try {
                byte slot = this.isPlayer() ? (byte)37 : 13;
                primWeapon = this.getEquippedWeapon(slot, true);
                if (!primWeapon.isTwoHanded()) {
                    primWeapon = null;
                } else if (this.getShield() != null) {
                    primWeapon = null;
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, nsp.getMessage(), nsp);
            }
        }
        return primWeapon;
    }

    public Item getLefthandWeapon() {
        try {
            int slot = this.isPlayer() ? 37 : 13;
            Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
            if (wornItems != null) {
                for (Item item : wornItems) {
                    if (item.isArmour() || item.isBodyPartAttached() || item.getDamagePercent() <= 0) continue;
                    return item;
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return null;
    }

    public Item getLefthandItem() {
        try {
            int slot = this.isPlayer() ? 37 : 13;
            Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
            if (wornItems != null) {
                for (Item item : wornItems) {
                    if (item.isArmour() || item.isBodyPartAttached()) continue;
                    return item;
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return null;
    }

    public Item getRighthandItem() {
        try {
            int slot = this.isPlayer() ? 38 : 14;
            Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
            if (wornItems != null) {
                for (Item item : wornItems) {
                    if (item.isArmour() || item.isBodyPartAttached()) continue;
                    return item;
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return null;
    }

    public Item getRighthandWeapon() {
        try {
            int slot = this.isPlayer() ? 38 : 14;
            Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
            if (wornItems != null) {
                for (Item item : wornItems) {
                    if (item.isArmour() || item.isBodyPartAttached() || item.getDamagePercent() <= 0) continue;
                    return item;
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return null;
    }

    public Item getWornBelt() {
        try {
            int slot = this.isPlayer() ? 43 : 34;
            Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
            if (wornItems != null) {
                for (Item item : wornItems) {
                    if (!item.isBelt()) continue;
                    return item;
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return null;
    }

    public Item[] getSecondaryWeapons() {
        HashSet<Item> toReturn;
        block23: {
            toReturn = new HashSet<Item>();
            if (this.getBiteDamage() > 0.0f) {
                try {
                    toReturn.add(this.getEquippedWeapon((byte)29));
                }
                catch (NoSpaceException nsp) {
                    logger.log(Level.WARNING, this.getName() + " no face.");
                }
            }
            if (this.getHeadButtDamage() > 0.0f) {
                try {
                    toReturn.add(this.getEquippedWeapon((byte)1));
                }
                catch (NoSpaceException nsp) {
                    logger.log(Level.WARNING, this.getName() + " no head.");
                }
            }
            if (this.getKickDamage() > 0.0f) {
                try {
                    if (this.isAnimal() || this.isMonster()) {
                        toReturn.add(this.getEquippedWeapon((byte)34));
                        break block23;
                    }
                    try {
                        this.getArmour((byte)34);
                    }
                    catch (NoArmourException nsp) {
                        if (this.getCarryingCapacityLeft() > 40000) {
                            toReturn.add(this.getEquippedWeapon((byte)34));
                        }
                    }
                }
                catch (NoSpaceException nsp) {
                    logger.log(Level.WARNING, this.getName() + " no legs.");
                }
            }
        }
        if (this.getBreathDamage() > 0.0f) {
            try {
                toReturn.add(this.getEquippedWeapon((byte)2));
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, this.getName() + " no torso.");
            }
        }
        if (this.getShield() == null) {
            try {
                if (this.getPrimWeapon() == null || !this.getPrimWeapon().isTwoHanded()) {
                    if (this.isPlayer()) {
                        toReturn.add(this.getEquippedWeapon((byte)37, false));
                    } else {
                        toReturn.add(this.getEquippedWeapon((byte)13, false));
                    }
                }
            }
            catch (NoSpaceException nsp) {
                logger.log(Level.WARNING, this.getName() + " - no arm. This may be possible later but not now." + nsp.getMessage(), nsp);
            }
        }
        if (!toReturn.isEmpty()) {
            return toReturn.toArray(new Item[toReturn.size()]);
        }
        return emptyItems;
    }

    public Item getShield() {
        Item shield = null;
        try {
            byte slot = this.isPlayer() ? (byte)44 : 3;
            shield = this.getEquippedItem(slot);
            if (shield != null && !shield.isShield()) {
                shield = null;
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
        return shield;
    }

    public float getSpeed() {
        if (this.getCreatureAIData() != null) {
            return this.getCreatureAIData().getSpeed();
        }
        return this.template.getSpeed();
    }

    public int calculateSize() {
        short centimetersHigh = this.getBody().getCentimetersHigh();
        short centimetersLong = this.getBody().getCentimetersLong();
        short centimetersWide = this.getBody().getCentimetersWide();
        int size = 3;
        size = centimetersHigh > 400 || centimetersLong > 400 || centimetersWide > 400 ? 5 : (centimetersHigh > 200 || centimetersLong > 200 || centimetersWide > 200 ? 4 : (centimetersHigh > 100 || centimetersLong > 100 || centimetersWide > 100 ? 3 : (centimetersHigh > 50 || centimetersLong > 50 || centimetersWide > 50 ? 2 : 1)));
        return size;
    }

    public void say(String message) {
        if (this.currentTile != null) {
            this.currentTile.broadCastMessage(new Message(this, 0, ":Local", "<" + this.getName() + "> " + message));
        }
    }

    public void say(String message, boolean emote) {
        if (this.currentTile != null) {
            if (!emote) {
                this.say(message);
            } else {
                this.currentTile.broadCastMessage(new Message(this, 6, ":Local", this.getName() + " " + message));
            }
        }
    }

    public void sendEquipment(Creature receiver) {
        if (receiver.addItemWatched(this.getBody().getBodyItem())) {
            receiver.getCommunicator().sendOpenInventoryWindow(this.getBody().getBodyItem().getWurmId(), this.getName());
            this.getBody().getBodyItem().addWatcher(this.getBody().getBodyItem().getWurmId(), receiver);
            Wounds w = this.getBody().getWounds();
            if (w != null) {
                Wound[] wounds;
                for (Wound lWound : wounds = w.getWounds()) {
                    try {
                        Item bodypart = this.getBody().getBodyPartForWound(lWound);
                        receiver.getCommunicator().sendAddWound(lWound, bodypart);
                    }
                    catch (NoSpaceException nsp) {
                        logger.log(Level.INFO, nsp.getMessage(), nsp);
                    }
                }
            }
        }
        if (receiver.getPower() >= 2 && receiver.addItemWatched(this.getInventory())) {
            receiver.getCommunicator().sendOpenInventoryWindow(this.getInventory().getWurmId(), this.getName() + " inventory");
            this.getInventory().addWatcher(this.getInventory().getWurmId(), receiver);
        }
    }

    public final void startUsingPath() {
        if (this.setTargetNOID) {
            this.setTarget(-10L, true);
            this.setTargetNOID = false;
        }
        if (this.creatureToBlinkTo != null) {
            if (!this.creatureToBlinkTo.isDead()) {
                logger.log(Level.INFO, this.getName() + " at " + this.getTileX() + "," + this.getTileY() + " " + this.getLayer() + "  blingking to " + this.creatureToBlinkTo.getTileX() + "," + this.creatureToBlinkTo.getTileY() + "," + this.creatureToBlinkTo.getLayer());
                this.blinkTo(this.creatureToBlinkTo.getTileX(), this.creatureToBlinkTo.getTileY(), this.creatureToBlinkTo.getLayer(), this.creatureToBlinkTo.getFloorLevel());
                this.status.setPath(null);
                this.receivedPath = false;
                this.setPathing(false, true);
            }
            this.creatureToBlinkTo = null;
        }
        if (this.receivedPath) {
            this.receivedPath = false;
            this.setPathing(false, false);
            if (this.status.getPath() != null) {
                this.sendToLoggers("received path to " + this.status.getPath().getTargetTile().getTileX() + "," + this.status.getPath().getTargetTile().getTileY(), (byte)2);
                this.pathRecalcLength = this.status.getPath().getSize() >= 4 ? this.status.getPath().getSize() / 2 : 0;
                this.status.setMoving(true);
                if (this.moveAlongPath() || this.isTeleporting()) {
                    this.status.setPath(null);
                    this.status.setMoving(false);
                }
            }
        }
    }

    protected void hunt() {
        if (!this.isPathing()) {
            Path path = null;
            boolean findPath = false;
            if (Server.rand.nextInt(2 * Math.max(1, this.template.getAggressivity())) == 0) {
                this.setTargetNOID = true;
                return;
            }
            if (this.isAnimal() || this.isDominated()) {
                path = this.status.getPath();
                if (path == null) {
                    findPath = true;
                }
            } else {
                findPath = true;
            }
            if (findPath) {
                this.startPathing(10);
            } else if (path == null) {
                this.startPathing(100);
            }
        }
    }

    public void setAlertSeconds(int seconds) {
        this.guardSecondsLeft = (byte)seconds;
    }

    public byte getAlertSeconds() {
        return this.guardSecondsLeft;
    }

    public void callGuards() {
        if (this.guardSecondsLeft > 0) {
            this.getCommunicator().sendNormalServerMessage("You already called the guards. Wait a few seconds.", (byte)3);
            return;
        }
        this.guardSecondsLeft = (byte)10;
        if (this.getVisionArea() != null) {
            if (this.isOnSurface()) {
                if (this.getVisionArea().getSurface() != null) {
                    this.getVisionArea().getSurface().callGuards();
                }
            } else if (this.getVisionArea().getUnderGround() != null) {
                this.getVisionArea().getUnderGround().callGuards();
            }
        }
    }

    public final boolean isPathing() {
        return this.isPathing;
    }

    public final void setPathing(boolean pathing, boolean removeFromPathing) {
        this.isPathing = pathing;
        if (removeFromPathing) {
            if (this.isHuman() || this.isGhost() || this.isUnique()) {
                pathFinderNPC.removeTarget(this);
            } else if (this.isAggHuman()) {
                pathFinderAgg.removeTarget(this);
            } else {
                pathFinder.removeTarget(this);
            }
        }
    }

    public final void startPathingToTile(PathTile p) {
        if (this.creatureToBlinkTo == null) {
            this.targetPathTile = p;
            if (p != null) {
                this.sendToLoggers("heading to specific " + p.getTileX() + "," + p.getTileY(), (byte)2);
                this.setPathing(true, false);
                if (this.isHuman() || this.isGhost() || this.isUnique()) {
                    pathFinderNPC.addTarget(this, p);
                } else if (this.isAggHuman()) {
                    pathFinderAgg.addTarget(this, p);
                } else {
                    pathFinder.addTarget(this, p);
                }
            }
        }
    }

    public final void startPathing(int seed) {
        PathTile p;
        if (this.creatureToBlinkTo == null && (p = this.getMoveTarget(seed)) != null) {
            this.startPathingToTile(p);
        }
    }

    public final void checkMove() throws NoSuchCreatureException, NoSuchPlayerException {
        block54: {
            block55: {
                block59: {
                    block58: {
                        PathTile targ;
                        block57: {
                            block56: {
                                block53: {
                                    Item torsoItem;
                                    if (this.hitchedTo != null) {
                                        return;
                                    }
                                    if (this.isSentinel()) {
                                        return;
                                    }
                                    if ((this.isHorse() || this.isUnicorn()) && (torsoItem = this.getWornItem((byte)2)) != null && (torsoItem.isSaddleLarge() || torsoItem.isSaddleNormal())) {
                                        return;
                                    }
                                    if (!this.isDominated()) break block53;
                                    if (this.hasOrders()) {
                                        if (this.target == -10L) {
                                            if (this.status.getPath() == null) {
                                                if (!this.isPathing()) {
                                                    this.startPathing(0);
                                                }
                                            } else if (this.moveAlongPath() || this.isTeleporting()) {
                                                Creature linkedToc;
                                                this.status.setPath(null);
                                                this.status.setMoving(false);
                                                if (this.isSpy() && this.isWithinSpyDist(linkedToc = this.getCreatureLinkedTo())) {
                                                    this.turnTowardsCreature(linkedToc);
                                                    for (Npc npc : Creatures.getInstance().getNpcs()) {
                                                        if (npc.isDead() || !this.isSpyFriend(npc) || !npc.isWithinDistanceTo(this, 400.0f) || npc.longTarget != null) continue;
                                                        npc.longTarget = new LongTarget(linkedToc.getTileX(), linkedToc.getTileY(), 0, linkedToc.isOnSurface(), linkedToc.getFloorLevel(), npc);
                                                        if (npc.isWithinDistanceTo(linkedToc, 100.0f)) continue;
                                                        int seed = Server.rand.nextInt(5);
                                                        String mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                                                        switch (seed) {
                                                            case 0: {
                                                                mess = linkedToc.getName() + " is in trouble now!";
                                                                break;
                                                            }
                                                            case 1: {
                                                                mess = "Going to check out what " + linkedToc.getName() + " is doing.";
                                                                break;
                                                            }
                                                            case 2: {
                                                                mess = "Heading to slay " + linkedToc.getName() + ".";
                                                                break;
                                                            }
                                                            case 3: {
                                                                mess = "Going to get me the scalp of " + linkedToc.getName() + " today.";
                                                                break;
                                                            }
                                                            case 4: {
                                                                mess = "Poor " + linkedToc.getName() + " won't know what hit " + linkedToc.getHimHerItString() + ".";
                                                                break;
                                                            }
                                                            default: {
                                                                mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                                                            }
                                                        }
                                                        VolaTile tile = npc.getCurrentTile();
                                                        if (tile == null) continue;
                                                        Message m = new Message(npc, 0, ":Local", "<" + npc.getName() + "> " + mess);
                                                        tile.broadCastMessage(m);
                                                    }
                                                }
                                            }
                                        } else if (this.status.getPath() != null) {
                                            if (this.moveAlongPath() || this.isTeleporting()) {
                                                this.status.setPath(null);
                                                this.status.setMoving(false);
                                            }
                                        } else {
                                            this.hunt();
                                        }
                                    }
                                    break block54;
                                }
                                if (this.leader != null || this.shouldStandStill || this.status.isUnconscious() || this.status.getStunned() != 0.0f) break block54;
                                if (!this.isMoveGlobal()) break block55;
                                if (this.status.getPath() == null) break block56;
                                if (this.moveAlongPath() || this.isTeleporting()) {
                                    this.status.setPath(null);
                                    this.status.setMoving(false);
                                }
                                break block54;
                            }
                            if (!this.isHunter() || this.target == -10L || this.fleeCounter > 0) break block57;
                            this.hunt();
                            break block54;
                        }
                        if (Server.rand.nextInt(100) == 0 && (targ = this.getPersonalTargetTile()) != null && !this.isPathing) {
                            this.startPathingToTile(targ);
                        }
                        if (!this.status.moving) break block58;
                        if (Server.rand.nextInt(100) >= 5) break block59;
                        this.status.setMoving(false);
                        break block59;
                    }
                    int mod = 1;
                    int max = 2000;
                    if (this.isCareful() && this.getStatus().damage > 10000 || this.loggerCreature1 > 0L) {
                        mod = 19;
                    } else if (this.isBred() || this.isBranded() || this.isCaredFor()) {
                        max = 20000;
                    } else if (this.isNpc() && !this.isAggHuman() && this.getCitizenVillage() != null) {
                        max = 200 + (int)(this.getWurmId() % 100L) * 3;
                    }
                    if (Server.rand.nextInt(Math.max(1, max - this.template.getMoveRate() * mod)) < 5 || this.shouldFlee()) {
                        this.status.setMoving(true);
                    } else if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled() && (Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5 || this.loggerCreature1 > 0L)) {
                        for (Fence f : this.getCurrentTile().getAllFences()) {
                            if (f.isHorizontal() && Math.abs(f.getPositionY() - this.getPosY()) < 1.25f) {
                                this.takeSimpleStep();
                                break;
                            }
                            if (f.isHorizontal() || !(Math.abs(f.getPositionX() - this.getPosX()) < 1.25f)) continue;
                            this.takeSimpleStep();
                            break;
                        }
                    }
                }
                if (this.status.moving && !this.isTeleporting()) {
                    this.takeSimpleStep();
                }
                break block54;
            }
            if (this.status.getPath() == null) {
                if (!this.isTeleporting()) {
                    if (!this.isPathing()) {
                        if (this.target == -10L || this.shouldFlee()) {
                            int mod = 1;
                            int max = 2000;
                            if (this.isCareful() && this.getStatus().damage > 10000) {
                                mod = 19;
                            }
                            if (this.loggerCreature1 > 0L) {
                                mod = 19;
                            }
                            int seed = Server.rand.nextInt(Math.max(2, max - this.template.getMoveRate() * mod));
                            if (this.getPositionZ() < 0.0f) {
                                seed -= 100;
                            }
                            if (seed < 8 || this.isSpiritGuard() && this.citizenVillage != this.currentVillage || this.shouldFlee()) {
                                this.startPathing(seed);
                            }
                        } else {
                            this.hunt();
                        }
                        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                            if (Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5 || this.loggerCreature1 > 0L) {
                                float xMod = this.getPosX() % 4.0f;
                                float yMod = this.getPosY() % 4.0f;
                                if (xMod > 3.5f || xMod < 0.5f || yMod > 3.5f || yMod < 0.5f) {
                                    this.takeSimpleStep();
                                }
                            }
                            if (this.shouldFlee() && this.getPathfindCounter() > 10 && this.targetPathTile != null && (this.getTileX() != this.targetPathTile.getTileX() || this.getTileY() != this.targetPathTile.getTileY())) {
                                if (this.getPathfindCounter() % 50 == 0 && Server.rand.nextFloat() < 0.05f) {
                                    this.turnTowardsTile((short)this.targetPathTile.getTileX(), (short)this.targetPathTile.getTileY());
                                }
                                this.takeSimpleStep();
                            }
                        }
                    } else {
                        this.sendToLoggers("still pathing");
                    }
                } else {
                    this.status.setPath(null);
                    this.status.setMoving(false);
                }
            } else if (this.moveAlongPath() || this.isTeleporting()) {
                this.status.setPath(null);
                this.status.setMoving(false);
            }
        }
    }

    public float getMoveModifier(int tile) {
        short height = Tiles.decodeHeight(tile);
        if (height < 2) {
            return 0.5f * this.status.getMovementTypeModifier();
        }
        return Tiles.getTile((byte)Tiles.decodeType((int)tile)).speed * this.status.getMovementTypeModifier();
    }

    public boolean mayManageGuards() {
        if (this.citizenVillage != null) {
            return this.citizenVillage.isActionAllowed((short)67, this);
        }
        return false;
    }

    public boolean isMoving() {
        return this.status.isMoving();
    }

    public static final float normalizeAngle(float angle) {
        return MovementChecker.normalizeAngle(angle);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public final void checkBridgeMove(VolaTile oldTile, VolaTile newtile, float diffZ) {
        BridgePart[] bridgeParts;
        if (this.getBridgeId() == -10L && newtile.getStructure() != null) {
            BridgePart[] bridgeParts2 = newtile.getBridgeParts();
            if (bridgeParts2 == null) return;
            for (BridgePart bp : bridgeParts2) {
                if (!bp.isFinished()) continue;
                boolean enter = false;
                float nz = Zones.calculatePosZ(this.getPosX(), this.getPosY(), newtile, this.isOnSurface(), this.isFloating(), this.getPositionZ(), this, bp.getStructureId());
                float newDiff = Math.abs(nz - this.getPositionZ());
                float maxDiff = 1.3f;
                if (oldTile != null) {
                    if (bp.getDir() == 0 || bp.getDir() == 4) {
                        if (oldTile.getTileY() == newtile.getTileY() && newDiff < 1.3f && bp.hasAnExit()) {
                            enter = true;
                        }
                    } else if (oldTile.getTileX() == newtile.getTileX() && newDiff < 1.3f && bp.hasAnExit()) {
                        enter = true;
                    }
                } else {
                    boolean bl = enter = newDiff < 1.3f;
                }
                if (!enter) continue;
                this.setBridgeId(bp.getStructureId());
                float newDiffZ = nz - this.getPositionZ();
                this.setPositionZ(nz);
                this.moved(0.0f, 0.0f, newDiffZ, 0, 0);
                return;
            }
            return;
        }
        if (this.getBridgeId() == -10L) return;
        boolean leave = true;
        if (oldTile != null && (bridgeParts = oldTile.getBridgeParts()) != null) {
            for (BridgePart bp : bridgeParts) {
                if (!bp.isFinished()) continue;
                if (bp.getDir() == 0 || bp.getDir() == 4) {
                    if (oldTile.getTileX() == newtile.getTileX()) continue;
                    leave = false;
                    continue;
                }
                if (oldTile.getTileY() == newtile.getTileY()) continue;
                leave = false;
            }
        }
        if (!leave) return;
        if (newtile.getStructure() == null || newtile.getStructure().getWurmId() != this.getBridgeId()) {
            this.setBridgeId(-10L);
            return;
        } else {
            bridgeParts = newtile.getBridgeParts();
            boolean foundBridge = false;
            for (BridgePart bp : bridgeParts) {
                foundBridge = true;
                if (bp.isFinished()) continue;
                this.setBridgeId(-10L);
                return;
            }
            if (!foundBridge) return;
            for (BridgePart bp : bridgeParts) {
                if (!bp.isFinished() || !bp.hasAnExit()) continue;
                this.setBridgeId(bp.getStructureId());
                return;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean moveAlongPath() {
        long start = System.nanoTime();
        Path path = null;
        int mvs = 2;
        if (this.target != -10L) {
            mvs = 3;
        }
        if (this.getSize() >= 5) {
            mvs += 3;
        }
        int x = 0;
        while (true) {
            block33: {
                float oldPosZ;
                float oldPosY;
                float oldPosX;
                int lDiffTileY;
                int lDiffTileX;
                float lRotation;
                float lPosZ;
                float lPosY;
                float lPosX;
                block31: {
                    block38: {
                        PathTile next;
                        block34: {
                            block35: {
                                int totalDist2;
                                Floor[] diffY2;
                                block37: {
                                    block32: {
                                        Floor[] floors;
                                        block36: {
                                            if (x >= mvs) break block32;
                                            path = this.status.getPath();
                                            if (path == null || path.isEmpty()) break block33;
                                            next = path.getFirst();
                                            if (next.getTileX() != this.getCurrentTile().tilex || next.getTileY() != this.getCurrentTile().tiley) break block34;
                                            boolean canRemove = true;
                                            if (next.hasSpecificPos()) {
                                                float diffX = this.status.getPositionX() - next.getPosX();
                                                float diffY2 = this.status.getPositionY() - next.getPosY();
                                                double totalDist2 = Math.sqrt(diffX * diffX + diffY2 * diffY2);
                                                float lMod = this.getMoveModifier((this.isOnSurface() ? Server.surfaceMesh : Server.caveMesh).getTile((int)this.status.getPositionX() >> 2, (int)this.status.getPositionY() >> 2));
                                                if (totalDist2 > (double)(this.getSpeed() * lMod)) {
                                                    canRemove = false;
                                                }
                                            }
                                            if (!canRemove) break block34;
                                            path.removeFirst();
                                            if (this.getTarget() == null || this.getTarget().getTileX() != this.getTileX() || this.getTarget().getTileY() != this.getTileY() || this.getTarget().getFloorLevel() == this.getFloorLevel()) break block35;
                                            if (!this.isSpiritGuard()) break block36;
                                            this.pushToFloorLevel(this.getTarget().getFloorLevel());
                                            break block35;
                                        }
                                        if (!this.canOpenDoors()) break block35;
                                        diffY2 = floors = this.getCurrentTile().getFloors(Math.min(this.getFloorLevel(), this.getTarget().getFloorLevel()) * 30, Math.max(this.getFloorLevel(), this.getTarget().getFloorLevel()) * 30);
                                        totalDist2 = diffY2.length;
                                        break block37;
                                    }
                                    if (path == null) {
                                        return true;
                                    }
                                    if (this.pathRecalcLength <= 0) return path.isEmpty();
                                    if (path.getSize() > this.pathRecalcLength) return path.isEmpty();
                                    return true;
                                }
                                for (int i = 0; i < totalDist2; ++i) {
                                    Floor f = diffY2[i];
                                    if (this.getTarget().getFloorLevel() > this.getFloorLevel()) {
                                        if (f.getFloorLevel() != this.getFloorLevel() + 1 || !f.isOpening() && !f.isAPlan()) continue;
                                        this.pushToFloorLevel(f.getFloorLevel());
                                        break;
                                    }
                                    if (f.getFloorLevel() != this.getFloorLevel() || !f.isOpening() && !f.isAPlan()) continue;
                                    this.pushToFloorLevel(f.getFloorLevel() - 1);
                                    break;
                                }
                            }
                            if (path.isEmpty()) {
                                return true;
                            }
                            next = path.getFirst();
                        }
                        lPosX = this.status.getPositionX();
                        lPosY = this.status.getPositionY();
                        lPosZ = this.status.getPositionZ();
                        lRotation = this.status.getRotation();
                        double lNewRotation = next.hasSpecificPos() ? Math.atan2(next.getPosY() - lPosY, next.getPosX() - lPosX) : Math.atan2((float)((next.getTileY() << 2) + 2) - lPosY, (float)((next.getTileX() << 2) + 2) - lPosX);
                        lRotation = (float)(lNewRotation * 57.29577951308232) + 90.0f;
                        int lOldTileX = (int)lPosX >> 2;
                        int lOldTileY = (int)lPosY >> 2;
                        MeshIO lMesh = this.isOnSurface() ? Server.surfaceMesh : Server.caveMesh;
                        float lMod = this.getMoveModifier(lMesh.getTile(lOldTileX, lOldTileY));
                        float lXPosMod = (float)Math.sin(lRotation * ((float)Math.PI / 180)) * this.getSpeed() * lMod;
                        float lYPosMod = -((float)Math.cos(lRotation * ((float)Math.PI / 180))) * this.getSpeed() * lMod;
                        int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
                        int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
                        lDiffTileX = lNewTileX - lOldTileX;
                        lDiffTileY = lNewTileY - lOldTileY;
                        if (Math.abs(lDiffTileX) > 1 || Math.abs(lDiffTileY) > 1) {
                            logger.log(Level.WARNING, this.getName() + "," + this.getWurmId() + " diffTileX=" + lDiffTileX + ", y=" + lDiffTileY);
                        }
                        if (lDiffTileX != 0 || lDiffTileY != 0) {
                            BlockingResult result;
                            if (!this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(lMesh.getTile(lNewTileX, lNewTileY)))) {
                                this.rotateRandom(lRotation, 45);
                                try {
                                    this.takeSimpleStep();
                                    return true;
                                }
                                catch (NoSuchPlayerException noSuchPlayerException) {
                                    return true;
                                }
                                catch (NoSuchCreatureException noSuchCreatureException) {
                                    // empty catch block
                                }
                                return true;
                            }
                            if (!this.isGhost() && (result = Blocking.getBlockerBetween(this, this.getPosX(), this.getPosY(), lPosX + lXPosMod, lPosY + lYPosMod, this.getPositionZ(), this.getPositionZ(), this.isOnSurface(), this.isOnSurface(), false, 6, true, -10L, this.getBridgeId(), this.getBridgeId(), this.followsGround())) != null) {
                                boolean foundDoor = false;
                                for (Blocker blocker : result.getBlockerArray()) {
                                    if (!blocker.isDoor() || blocker.canBeOpenedBy(this, false)) continue;
                                    foundDoor = true;
                                }
                                if (!foundDoor) {
                                    path.clear();
                                    return true;
                                }
                            }
                            if (!next.hasSpecificPos() && next.getTileX() == lNewTileX && next.getTileY() == lNewTileY) {
                                path.removeFirst();
                            }
                            movesx += lDiffTileX;
                            movesy += lDiffTileY;
                        }
                        oldPosX = lPosX;
                        oldPosY = lPosY;
                        oldPosZ = lPosZ;
                        int oldDeciZ = (int)(lPosZ * 10.0f);
                        lPosY += lYPosMod;
                        if ((lPosX += lXPosMod) >= (float)(Zones.worldTileSizeX - 1 << 2) || lPosX < 0.0f || lPosY < 0.0f || lPosY >= (float)(Zones.worldTileSizeY - 1 << 2)) {
                            this.destroy();
                            return 1 != 0;
                        }
                        lPosZ = this.calculatePosZ();
                        int newDeciZ = (int)(lPosZ * 10.0f);
                        if (!((double)lPosZ < -0.5)) break block31;
                        if (!this.isSubmerged()) break block38;
                        if (this.isFloating() && (float)newDeciZ > this.template.offZ * 10.0f) {
                            this.rotateRandom(lRotation, 100);
                            if (this.target == -10L) return true;
                            this.setTarget(-10L, true);
                            return true;
                        }
                        if (!this.isFloating() && lPosZ > -5.0f && oldDeciZ < newDeciZ) {
                            this.rotateRandom(lRotation, 100);
                            if (this.target == -10L) return true;
                            this.setTarget(-10L, true);
                            return true;
                        }
                        if (lPosZ < -5.0f) {
                            if (x == 3) {
                                if (this.isFloating()) {
                                    lPosZ = this.template.offZ;
                                    break block31;
                                } else {
                                    float newPosZ;
                                    float newdiff = Math.max(-1.0f, Math.min(1.0f, (float)Server.rand.nextGaussian()));
                                    lPosZ = newPosZ = Math.max(lPosZ, Math.min(-5.0f, this.getPositionZ() + newdiff));
                                }
                            }
                            break block31;
                        } else if (x == 3 && this.isFloating()) {
                            lPosZ = this.template.offZ;
                        }
                        break block31;
                    }
                    lPosZ = Math.max(-1.25f, lPosZ);
                    if (this.isFloating()) {
                        lPosZ = Math.max(this.template.offZ, lPosZ);
                    }
                }
                this.status.setPositionX(lPosX);
                this.status.setPositionY(lPosY);
                this.status.setPositionZ(lPosZ);
                this.status.setRotation(lRotation);
                this.moved(lPosX - oldPosX, lPosY - oldPosY, lPosZ - oldPosZ, lDiffTileX, lDiffTileY);
            }
            ++x;
        }
    }

    protected boolean startDestroyingWall(Wall wall) {
        try {
            BehaviourDispatcher.action(this, this.communicator, this.getEquippedWeapon((byte)14).getWurmId(), wall.getId(), (short)180);
        }
        catch (FailedException fe) {
            return true;
        }
        catch (NoSuchBehaviourException nsb) {
            logger.log(Level.WARNING, nsb.getMessage(), nsb);
            return true;
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, nsc.getMessage(), nsc);
            return true;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, nsi.getMessage(), nsi);
            return true;
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
            return true;
        }
        catch (NoSuchWallException nsw) {
            logger.log(Level.WARNING, nsw.getMessage(), nsw);
            return true;
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
            return true;
        }
        return false;
    }

    public void followLeader() {
        float iposx = this.leader.getStatus().getPositionX();
        float iposy = this.leader.getStatus().getPositionY();
        float diffx = iposx - this.status.getPositionX();
        float diffy = iposy - this.status.getPositionY();
        int diff = (int)Math.max(Math.abs(diffx), Math.abs(diffy));
        if (diffx < 0.0f && this.status.getPositionX() < 10.0f) {
            return;
        }
        if (diffy < 0.0f && this.status.getPositionY() < 10.0f) {
            return;
        }
        if (diffy > 0.0f && this.status.getPositionY() > Zones.worldMeterSizeY - 10.0f) {
            return;
        }
        if (diffx > 0.0f && this.status.getPositionX() > Zones.worldMeterSizeX - 10.0f) {
            return;
        }
        if (diff > 35) {
            logger.log(Level.INFO, this.leader.getName() + " moved " + diff + "diffx=" + diffx + ", diffy=" + diffy);
            this.setLeader(null);
        } else if (diffx > 4.0f || diffy > 4.0f || diffx < -4.0f || diffy < -4.0f) {
            BlockingResult result;
            float lPosX = this.status.getPositionX();
            float lPosY = this.status.getPositionY();
            float lPosZ = this.status.getPositionZ();
            int lOldTileX = (int)lPosX >> 2;
            int lOldTileY = (int)lPosY >> 2;
            double lNewrot = Math.atan2(iposy - lPosY, iposx - lPosX);
            if ((lNewrot = lNewrot * 57.29577951308232 + 90.0) > 360.0) {
                lNewrot -= 360.0;
            }
            if (lNewrot < 0.0) {
                lNewrot += 360.0;
            }
            float movex = 0.0f;
            float movey = 0.0f;
            if (diffx < -4.0f) {
                movex = diffx + 4.0f;
            } else if (diffx > 4.0f) {
                movex = diffx - 4.0f;
            }
            if (diffy < -4.0f) {
                movey = diffy + 4.0f;
            } else if (diffy > 4.0f) {
                movey = diffy - 4.0f;
            }
            float lXPosMod = (float)Math.sin(lNewrot * 0.01745329238474369) * Math.abs(movex + Server.rand.nextFloat());
            float lYPosMod = -((float)Math.cos(lNewrot * 0.01745329238474369)) * Math.abs(movey + Server.rand.nextFloat());
            float newPosX = lPosX + lXPosMod;
            float newPosY = lPosY + lYPosMod;
            int lNewTileX = (int)newPosX >> 2;
            int lNewTileY = (int)newPosY >> 2;
            int lDiffTileX = lNewTileX - lOldTileX;
            int lDiffTileY = lNewTileY - lOldTileY;
            if (!(lDiffTileX == 0 && lDiffTileY == 0 || this.isGhost() || this.leader.getBridgeId() >= 0L || this.getBridgeId() >= 0L || (result = Blocking.getBlockerBetween(this, lPosX, lPosY, newPosX, newPosY, this.getPositionZ(), this.leader.getPositionZ(), this.isOnSurface(), this.isOnSurface(), false, 2, -1L, this.getBridgeId(), this.getBridgeId(), this.followsGround())) == null)) {
                Blocker first = result.getFirstBlocker();
                if (!first.isDoor()) {
                    this.leader.sendToLoggers("Your floor level " + this.leader.getFloorLevel() + ", creature: " + this.getFloorLevel());
                    this.setLeader(null);
                    return;
                }
                if (!first.canBeOpenedBy(this.leader, false) && !first.canBeOpenedBy(this, false)) {
                    this.leader.sendToLoggers("Your floor level " + this.leader.getFloorLevel() + ", creature: " + this.getFloorLevel());
                    this.setLeader(null);
                    return;
                }
            }
            if (!this.leader.isOnSurface() && !this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))) {
                newPosX = iposx;
                newPosY = iposy;
            }
            float newPosZ = this.calculatePosZ();
            if (!this.isSwimming() && (double)newPosZ < -0.71 && newPosZ < lPosZ) {
                this.setLeader(null);
                this.status.setPositionZ(newPosZ);
            } else {
                newPosZ = Math.max(-1.25f, newPosZ);
                if (this.isFloating()) {
                    newPosZ = Math.max(this.template.offZ, newPosZ);
                }
                this.setRotation((float)lNewrot);
                int tilex = (int)lPosX >> 2;
                int tiley = (int)lPosY >> 2;
                int newtilex = (int)newPosX >> 2;
                int newtiley = (int)newPosY >> 2;
                this.status.setPositionX(newPosX);
                this.status.setPositionY(newPosY);
                this.status.setPositionZ(newPosZ);
                this.moved(newPosX - lPosX, newPosY - lPosY, newPosZ - lPosZ, newtilex - tilex, newtiley - tiley);
            }
        }
    }

    public void sendAttitudeChange() {
        if (this.currentTile != null) {
            this.currentTile.checkChangedAttitude(this);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void takeSimpleStep() throws NoSuchCreatureException, NoSuchPlayerException {
        long start = 0L;
        int mvs = 2;
        if (this.target != -10L) {
            mvs = 3;
        }
        if (this.getSize() >= 5) {
            mvs += 3;
        }
        for (int x = 0; x < mvs; ++x) {
            int lNewTileY;
            int lNewTileX;
            float lYPosMod;
            float lXPosMod;
            float lPosX = this.status.getPositionX();
            float lPosY = this.status.getPositionY();
            float lPosZ = this.status.getPositionZ();
            float lRotation = this.status.getRotation();
            float oldPosX = lPosX;
            float oldPosY = lPosY;
            float oldPosZ = lPosZ;
            int oldDeciZ = (int)(lPosZ * 10.0f);
            int lOldTileX = (int)lPosX >> 2;
            int lOldTileY = (int)lPosY >> 2;
            if (this.target == -10L) {
                if (this.isOnSurface()) {
                    int rand = Server.rand.nextInt(100);
                    if (rand < 10) {
                        int tile;
                        lXPosMod = (float)Math.sin(lRotation * ((float)Math.PI / 180)) * 12.0f;
                        lYPosMod = -((float)Math.cos(lRotation * ((float)Math.PI / 180))) * 12.0f;
                        lNewTileX = Zones.safeTileX((int)(lPosX + lXPosMod) >> 2);
                        if (this.isTargetTileTooHigh(lNewTileX, lNewTileY = Zones.safeTileY((int)(lPosY + lYPosMod) >> 2), tile = Zones.getTileIntForTile(lNewTileX, lNewTileY, this.getLayer()), lPosZ < 0.0f)) {
                            short[] lLowestNode = this.getLowestTileCorner((short)lOldTileX, (short)lOldTileY);
                            this.turnTowardsTile(lLowestNode[0], lLowestNode[1]);
                        }
                    } else if (rand < 12) {
                        this.rotateRandom(lRotation, 100);
                    } else if (rand < 15) {
                        lRotation = Creature.normalizeAngle(lRotation + (float)Server.rand.nextInt(100));
                    }
                } else {
                    int rand = Server.rand.nextInt(100);
                    if (rand < 2) {
                        this.rotateRandom(lRotation, 100);
                    } else if (rand < 5) {
                        lRotation = Creature.normalizeAngle(lRotation + (float)Server.rand.nextInt(100));
                    }
                }
            } else {
                this.turnTowardsCreature(this.getTarget());
            }
            lRotation = Creature.normalizeAngle(lRotation);
            float lMoveModifier = !this.isOnSurface() ? this.getMoveModifier(Server.caveMesh.getTile(lOldTileX, lOldTileY)) : this.getMoveModifier(Server.surfaceMesh.getTile(lOldTileX, lOldTileY));
            lXPosMod = (float)Math.sin(lRotation * ((float)Math.PI / 180)) * this.getSpeed() * lMoveModifier;
            lYPosMod = -((float)Math.cos(lRotation * ((float)Math.PI / 180))) * this.getSpeed() * lMoveModifier;
            lNewTileX = (int)(lPosX + lXPosMod) >> 2;
            lNewTileY = (int)(lPosY + lYPosMod) >> 2;
            int lDiffTileX = lNewTileX - lOldTileX;
            int lDiffTileY = lNewTileY - lOldTileY;
            if (!(lDiffTileX == 0 && lDiffTileY == 0 || this.isGhost())) {
                BlockingResult result;
                if (!this.isOnSurface()) {
                    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lOldTileX, lOldTileY)))) {
                        logger.log(Level.INFO, this.getName() + " is in rock at takesimplestep. Dying.");
                        this.die(false, "Suffocated in Rock");
                        return;
                    }
                    if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lNewTileX, lNewTileY)))) {
                        if (this.currentTile.isTransition) {
                            this.sendToLoggers(lPosZ + " setting to surface then moving.");
                            if (!Tiles.isMineDoor(Tiles.decodeType(Server.caveMesh.getTile(this.getTileX(), this.getTileY()))) || MineDoorPermission.getPermission(this.getTileX(), this.getTileY()).mayPass(this)) {
                                this.setLayer(0, true);
                            } else {
                                this.rotateRandom(lRotation, 45);
                            }
                            return;
                        }
                        this.rotateRandom(lRotation, 45);
                        return;
                    }
                } else if (Tiles.Tile.TILE_LAVA.id == Tiles.decodeType(Server.surfaceMesh.getTile(lNewTileX, lNewTileY))) {
                    this.rotateRandom(lRotation, 45);
                    return;
                }
                if ((result = Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled() ? Blocking.getBlockerBetween(this, lPosX, lPosY, lPosX + lXPosMod, lPosY + lYPosMod, this.getPositionZ(), this.getPositionZ(), this.isOnSurface(), this.isOnSurface(), false, 6, -1L, this.getBridgeId(), this.getBridgeId(), this.followsGround()) : Blocking.getBlockerBetween(this, lPosX, lPosY, lPosX + lXPosMod, lPosY + lYPosMod, this.getPositionZ(), this.getPositionZ(), this.isOnSurface(), this.isOnSurface(), false, 6, -1L, this.getBridgeId(), this.getBridgeId(), this.followsGround())) != null) {
                    Blocker first = result.getFirstBlocker();
                    if (this.isKingdomGuard() || this.isSpiritGuard()) {
                        if (!first.isDoor()) {
                            this.rotateRandom(lRotation, 100);
                            return;
                        }
                    } else {
                        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                            this.turnTowardsTile((short)this.getTileX(), (short)this.getTileY());
                            this.rotateRandom(this.status.getRotation(), 45);
                            x = 0;
                            this.getStatus().setMoving(false);
                            continue;
                        }
                        this.rotateRandom(lRotation, 100);
                        return;
                    }
                }
                VolaTile t = Zones.getOrCreateTile(lNewTileX, lNewTileY, this.isOnSurface());
                VolaTile myt = this.getCurrentTile();
                if (t.isGuarded() && myt != null && !myt.isGuarded() || this.isAnimal() && t.hasFire()) {
                    this.rotateRandom(lRotation, 100);
                    return;
                }
            }
            lPosY += lYPosMod;
            if ((lPosX += lXPosMod) >= (float)(Zones.worldTileSizeX - 1 << 2) || lPosX < 0.0f || lPosY < 0.0f || lPosY >= (float)(Zones.worldTileSizeY - 1 << 2)) {
                this.destroy();
                return;
            }
            if (this.getFloorLevel() == 0) {
                try {
                    lPosZ = Zones.calculateHeight(lPosX, lPosY, this.isOnSurface());
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.WARNING, this.name + " moved out of zone.");
                }
                if (this.isFloating()) {
                    lPosZ = Math.max(this.template.offZ, lPosZ);
                }
                int newDeciZ = (int)(lPosZ * 10.0f);
                if ((double)lPosZ < 0.5) {
                    if (this.isSubmerged()) {
                        if (this.isFloating() && (float)newDeciZ > this.template.offZ * 10.0f) {
                            this.rotateRandom(lRotation, 100);
                            if (this.target != -10L) {
                                this.setTarget(-10L, true);
                            }
                            return;
                        }
                        if (!this.isFloating() && lPosZ > -5.0f && oldDeciZ < newDeciZ) {
                            this.rotateRandom(lRotation, 100);
                            if (this.target != -10L) {
                                this.setTarget(-10L, true);
                            }
                            return;
                        }
                        if (lPosZ < -5.0f) {
                            if (x == 3) {
                                if (this.isFloating()) {
                                    lPosZ = this.template.offZ;
                                } else {
                                    float newPosZ;
                                    float newdiff = Math.max(-1.0f, Math.min(1.0f, (float)Server.rand.nextGaussian()));
                                    lPosZ = newPosZ = Math.max(lPosZ, Math.min(-5.0f, this.getPositionZ() + newdiff));
                                }
                            }
                        } else if (x == 3 && this.isFloating()) {
                            lPosZ = this.template.offZ;
                        }
                    }
                    if ((lPosZ > -2.0f || oldDeciZ <= -20) && (oldDeciZ < 0 || this.target != -10L) && this.isSwimming()) {
                        lPosZ = Math.max(-1.25f, lPosZ);
                        if (this.isFloating()) {
                            lPosZ = Math.max(this.template.offZ, lPosZ);
                        }
                    } else if ((double)lPosZ < -0.5 && !this.isSubmerged()) {
                        this.rotateRandom(lRotation, 100);
                        if (this.target != -10L) {
                            this.setTarget(-10L, true);
                        }
                        return;
                    }
                } else if (this.isSubmerged() && oldDeciZ < newDeciZ) {
                    this.rotateRandom(lRotation, 100);
                    if (this.target != -10L) {
                        this.setTarget(-10L, true);
                    }
                    return;
                }
            }
            this.status.setPositionX(lPosX);
            this.status.setPositionY(lPosY);
            if (Structure.isGroundFloorAtPosition(lPosX, lPosY, this.isOnSurface())) {
                this.status.setPositionZ(lPosZ + 0.25f);
            } else {
                this.status.setPositionZ(lPosZ);
            }
            this.status.setRotation(lRotation);
            this.moved(lPosX - oldPosX, lPosY - oldPosY, lPosZ - oldPosZ, lDiffTileX, lDiffTileY);
        }
    }

    public void rotateRandom(float aRot, int degrees) {
        aRot -= (float)degrees;
        aRot += (float)Server.rand.nextInt(degrees * 2);
        aRot = Creature.normalizeAngle(aRot);
        this.status.setRotation(aRot);
        this.moved(0.0f, 0.0f, 0.0f, 0, 0);
    }

    public int getAttackDistance() {
        return this.template.getSize();
    }

    public void moved(float diffX, float diffY, float diffZ, int aDiffTileX, int aDiffTileY) {
        if (!this.isDead()) {
            block30: {
                try {
                    if (this.isPlayer() || this.isWagoner()) {
                        this.movementScheme.move(diffX, diffY, diffZ);
                        if ((this.isWagoner() || this.hasLink()) && this.getVisionArea() != null) {
                            try {
                                this.getVisionArea().move(aDiffTileX, aDiffTileY);
                            }
                            catch (IOException iox) {
                                return;
                            }
                        }
                        try {
                            this.getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
                        }
                        catch (NoSuchPlayerException iox) {
                        }
                        catch (NoSuchCreatureException iox) {
                            // empty catch block
                        }
                        if (this.hasLink() && this.getVisionArea() != null) {
                            this.getVisionArea().linkZones(aDiffTileX, aDiffTileY);
                        }
                        break block30;
                    }
                    try {
                        this.getVisionArea().move(aDiffTileX, aDiffTileY);
                    }
                    catch (IOException iox) {
                        return;
                    }
                    try {
                        this.getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
                    }
                    catch (NoSuchPlayerException iox) {
                    }
                    catch (NoSuchCreatureException iox) {
                        // empty catch block
                    }
                    this.getVisionArea().linkZones(aDiffTileX, aDiffTileY);
                }
                catch (NullPointerException ex) {
                    try {
                        if (!this.isPlayer()) {
                            this.createVisionArea();
                        }
                        return;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
            if (diffX != 0.0f || diffY != 0.0f) {
                try {
                    if (this.isPlayer() && this.actions.getCurrentAction().isInterruptedAtMove()) {
                        boolean stop = true;
                        if (this.actions.getCurrentAction().getNumber() == 136) {
                            this.getCommunicator().sendToggle(3, false);
                        } else if ((this.actions.getCurrentAction().getNumber() == 329 || this.actions.getCurrentAction().getNumber() == 162 || this.actions.getCurrentAction().getNumber() == 160) && this.getVehicle() != -10L) {
                            stop = false;
                        }
                        if (stop) {
                            this.communicator.sendSafeServerMessage("You must not move while doing that.");
                            this.stopCurrentAction();
                        }
                    }
                }
                catch (NoSuchActionException stop) {
                    // empty catch block
                }
                if ((aDiffTileX != 0 || aDiffTileY != 0) && this.musicPlayer != null) {
                    this.musicPlayer.moveTile(this.getCurrentTileNum(), this.getPositionZ() <= 0.0f);
                }
            }
            if (this.status.isTrading()) {
                Trade trade = this.status.getTrade();
                Creature lOpponent = null;
                lOpponent = trade.creatureOne == this ? trade.creatureTwo : trade.creatureOne;
                if (Creature.rangeTo(this, lOpponent) > 6) {
                    trade.end(this, false);
                }
            }
        }
    }

    public void stopFighting() {
        if (this.actions != null) {
            this.actions.removeAttacks(this);
        }
    }

    public void turnTowardsCreature(Creature targ) {
        if (targ != null) {
            double lNewrot = Math.atan2(targ.getStatus().getPositionY() - this.getStatus().getPositionY(), targ.getStatus().getPositionX() - this.getStatus().getPositionX());
            this.setRotation((float)(lNewrot * 57.29577951308232) + 90.0f);
            if (this.isSubmerged()) {
                try {
                    float currFloor = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface());
                    float maxHeight = this.isFloating() ? this.template.offZ : Math.min(targ.getPositionZ(), Math.max(-5.0f, currFloor));
                    float oldHeight = this.getPositionZ();
                    int diffCentiZ = (int)((maxHeight - oldHeight) * 100.0f);
                    this.moved(0.0f, 0.0f, diffCentiZ, 0, 0);
                    return;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
            this.moved(0.0f, 0.0f, 0.0f, 0, 0);
        }
    }

    public void turnTowardsPoint(float posX, float posY) {
        double lNewrot = Math.atan2(posY - this.getStatus().getPositionY(), posX - this.getStatus().getPositionX());
        this.setRotation((float)(lNewrot * 57.29577951308232) + 90.0f);
        this.moved(0.0f, 0.0f, 0.0f, 0, 0);
    }

    public void turnTowardsTile(short tilex, short tiley) {
        double lNewrot = Math.atan2((float)((tiley << 2) + 2) - this.getStatus().getPositionY(), (float)((tilex << 2) + 2) - this.getStatus().getPositionX());
        this.setRotation((float)(lNewrot * 57.29577951308232) + 90.0f);
        this.moved(0.0f, 0.0f, 0.0f, 0, 0);
    }

    @Override
    public long getWurmId() {
        return this.id;
    }

    @Override
    public int getTemplateId() {
        return this.getTemplate().getTemplateId();
    }

    public String getNameWithoutPrefixes() {
        return this.name;
    }

    public String getNameWithoutFatStatus() {
        if (this.getStatus() != null) {
            return this.getStatus().getAgeString() + " " + this.getStatus().getTypeString() + this.name;
        }
        return "Unknown";
    }

    public String getName() {
        String fullName = this.name;
        if (this.isWagoner()) {
            return fullName;
        }
        if (this.isAnimal() || this.isMonster()) {
            fullName = this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) == 0 ? this.getPrefixes() + this.name.toLowerCase() : this.getPrefixes() + StringUtilities.raiseFirstLetterOnly(this.name);
        }
        if (this.petName.length() > 0) {
            return fullName + " '" + this.petName + "'";
        }
        return fullName;
    }

    public String getNamePossessive() {
        String toReturn = this.getName();
        if (toReturn.endsWith("s")) {
            return toReturn + "'";
        }
        return toReturn + "'s";
    }

    public String getPrefixes() {
        if (this.isUnique()) {
            return "The " + this.getStatus().getAgeString() + " " + this.getStatus().getFatString() + this.getStatus().getTypeString();
        }
        return this.getStatus().getAgeString() + " " + this.getStatus().getFatString() + this.getStatus().getTypeString();
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public void setPetName(String aPetName) {
        this.petName = aPetName == null ? "" : aPetName.substring(0, Math.min(19, aPetName.length()));
    }

    public String getColourName() {
        return this.template.getColourName(this.status);
    }

    public String getColourName(int trait) {
        return this.template.getTemplateColourName(trait);
    }

    public CreatureStatus getStatus() {
        return this.status;
    }

    public VisionArea getVisionArea() {
        return this.visionArea;
    }

    public void trainSkill(String sname) throws Exception {
        Skill skill = this.skills.getSkill(sname);
        String message = this.getName() + " trains some " + sname + ", but learns nothing new.";
        double knowledge = skill.getKnowledge(0.0);
        skill.skillCheck(50.0, 0.0, false, 3600.0f);
        if (skill.getKnowledge(0.0) > knowledge) {
            message = this.getName() + " trains some  " + sname + " and now have skill " + skill.getKnowledge(0.0);
        }
        logger.log(Level.INFO, message);
    }

    public void setSkill(int skill, float val) {
        try {
            Skill sktomod = this.skills.getSkill(skill);
            sktomod.setKnowledge(val, false);
        }
        catch (NoSuchSkillException nss) {
            this.skills.learn(skill, val);
        }
    }

    public void sendSkills() {
        try {
            this.loadAffinities();
            Map<Integer, Skill> skilltree = this.skills.getSkillTree();
            for (Integer number : skilltree.keySet()) {
                try {
                    Skill skill = skilltree.get(number);
                    int[] needed = skill.getDependencies();
                    int parentSkillId = 0;
                    if (needed.length > 0) {
                        parentSkillId = needed[0];
                    }
                    if (parentSkillId != 0) {
                        short parentType = SkillSystem.getTypeFor(parentSkillId);
                        if (parentType == 0) {
                            parentSkillId = Integer.MAX_VALUE;
                        }
                    } else {
                        parentSkillId = skill.getType() == 1 ? 0x7FFFFFFE : Integer.MAX_VALUE;
                    }
                    this.getCommunicator().sendAddSkill(number, parentSkillId, skill.getName(), (float)skill.getRealKnowledge(), (float)skill.getMinimumValue(), skill.affinity);
                }
                catch (NullPointerException np) {
                    logger.log(Level.WARNING, "Inconsistency: " + this.getName() + " forgetting skill with number " + number, np);
                }
            }
        }
        catch (Exception ex2) {
            logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + ex2.getMessage(), ex2);
        }
    }

    public void loadSkills() throws Exception {
        if (this.skills == null) {
            logger.log(Level.WARNING, "Skills object is null in creature " + this.name);
        }
        try {
            if (!this.isPlayer()) {
                if (this.skills.getId() != -10L) {
                    this.skills.initializeSkills();
                }
            } else if (!this.guest) {
                this.getCommunicator().sendAddSkill(0x7FFFFFFE, 0, "Characteristics", 0.0f, 0.0f, 0);
                this.getCommunicator().sendAddSkill(0x7FFFFFFB, 0, "Religion", 0.0f, 0.0f, 0);
                this.getCommunicator().sendAddSkill(Integer.MAX_VALUE, 0, "Skills", 0.0f, 0.0f, 0);
                this.skills.load();
            }
            boolean created = false;
            if (this.skills.isTemplate() || this.skills.getSkills().length == 0) {
                Skills newSkills = SkillsFactory.createSkills(this.id);
                newSkills.clone(this.skills.getSkills());
                this.skills = newSkills;
                created = true;
                if (!this.guest) {
                    this.skills.save();
                }
                this.skills.addTempSkills();
            }
            if (created) {
                if (this.isUndead()) {
                    this.skills.learn(1023, 30.0f);
                    this.skills.learn(10052, 50.0f);
                    this.skills.getSkill(102).setKnowledge(25.0, false);
                    this.skills.getSkill(103).setKnowledge(25.0, false);
                }
                if (Servers.localServer.testServer && Servers.localServer.entryServer && WurmId.getType(this.id) == 0) {
                    int level = 20;
                    this.skills.learn(1023, level);
                    this.skills.learn(10025, level);
                    this.skills.learn(10006, level);
                    this.skills.learn(10023, level);
                    this.skills.learn(10022, level);
                    this.skills.learn(10020, level);
                    this.skills.learn(10021, level);
                    this.skills.learn(10019, level);
                    this.skills.learn(10001, level);
                    this.skills.learn(10024, level);
                    this.skills.learn(10005, level);
                    this.skills.learn(10027, level);
                    this.skills.learn(10028, level);
                    this.skills.learn(10026, level);
                    this.skills.learn(10064, level);
                    this.skills.learn(10061, level);
                    this.skills.learn(10062, level);
                    this.skills.learn(10063, level);
                    this.skills.learn(1002, (float)level / 2.0f);
                    this.skills.learn(1003, (float)level / 2.0f);
                    this.skills.learn(10056, level);
                    this.skills.getSkill(104).setKnowledge(23.0, false);
                    this.skills.getSkill(1).setKnowledge(3.0, false);
                    this.skills.getSkill(102).setKnowledge(23.0, false);
                    this.skills.getSkill(103).setKnowledge(23.0, false);
                    this.skills.learn(10053, level);
                    this.skills.learn(10054, level);
                    level = (int)(Server.rand.nextFloat() * 100.0f);
                    this.skills.learn(1030, level);
                    this.skills.learn(10081, level);
                    this.skills.learn(10079, level);
                    this.skills.learn(10080, level);
                }
            }
            this.setMoveLimits();
        }
        catch (Exception ex2) {
            logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + ex2.getMessage(), ex2);
        }
    }

    public void addStructureTile(VolaTile toAdd, byte structureType) {
        if (this.structure == null) {
            this.structure = Structures.createStructure(structureType, this.name + "'s planned structure", WurmId.getNextPlanId(), toAdd.tilex, toAdd.tiley, this.isOnSurface());
            this.status.setBuildingId(this.structure.getWurmId());
        } else {
            try {
                this.structure.addBuildTile(toAdd, false);
                if (structureType == 0) {
                    this.structure.clearAllWallsAndMakeWallsForStructureBorder(toAdd);
                }
            }
            catch (NoSuchZoneException nsz) {
                this.getCommunicator().sendNormalServerMessage("You can't build there.", (byte)3);
            }
        }
    }

    public long getBuildingId() {
        return this.status.buildingId;
    }

    public String getUndeadModelName() {
        if (this.getUndeadType() == 1) {
            if (this.status.sex == 0) {
                return "model.creature.humanoid.human.player.zombie.male" + WurmCalendar.getSpecialMapping(true);
            }
            if (this.status.sex == 1) {
                return "model.creature.humanoid.human.player.zombie.female" + WurmCalendar.getSpecialMapping(true);
            }
            return "model.creature.humanoid.human.player.zombie" + WurmCalendar.getSpecialMapping(true);
        }
        if (this.getUndeadType() == 2) {
            return "model.creature.humanoid.human.skeleton" + WurmCalendar.getSpecialMapping(true);
        }
        if (this.getUndeadType() == 3) {
            return "model.creature.humanoid.human.spirit.shadow" + WurmCalendar.getSpecialMapping(true);
        }
        return this.getModelName();
    }

    public String getModelName() {
        if (this.isReborn()) {
            if (this.status.sex == 0) {
                return this.template.getModelName() + ".zombie.male" + WurmCalendar.getSpecialMapping(true);
            }
            if (this.status.sex == 1) {
                return this.template.getModelName() + ".zombie.female" + WurmCalendar.getSpecialMapping(true);
            }
            return this.template.getModelName() + ".zombie" + WurmCalendar.getSpecialMapping(true);
        }
        if (this.template.isHorse || this.template.isColoured) {
            String col = this.template.getModelColourName(this.status);
            StringBuilder s = new StringBuilder();
            s.append(this.template.getModelName());
            s.append('.');
            s.append(col.toLowerCase());
            if (this.status.sex == 0) {
                s.append(".male");
            }
            if (this.status.sex == 1) {
                s.append(".female");
            }
            if (this.status.disease > 0) {
                s.append(".diseased");
            }
            s.append(WurmCalendar.getSpecialMapping(true));
            return s.toString();
        }
        if (this.template.isBlackOrWhite) {
            StringBuilder s = new StringBuilder();
            s.append(this.template.getModelName());
            if (this.status.sex == 0) {
                s.append(".male");
            }
            if (this.status.sex == 1) {
                s.append(".female");
            }
            if (!(this.hasTrait(15) || this.hasTrait(16) || this.hasTrait(18) || this.hasTrait(24) || this.hasTrait(25) || this.hasTrait(23) || this.hasTrait(30) || this.hasTrait(31) || this.hasTrait(32) || this.hasTrait(33) || this.hasTrait(34) || !this.hasTrait(17))) {
                s.append(".black");
            }
            if (this.status.disease > 0) {
                s.append(".diseased");
            }
            s.append(WurmCalendar.getSpecialMapping(true));
            return s.toString();
        }
        if (this.template.getTemplateId() == 119) {
            StringBuilder s = new StringBuilder();
            FishAI.FishAIData faid = (FishAI.FishAIData)this.getCreatureAIData();
            FishEnums.FishData fd = faid.getFishData();
            s.append(fd.getModelName());
            return s.toString();
        }
        StringBuilder s = new StringBuilder();
        s.append(this.template.getModelName());
        if (this.status.sex == 0) {
            s.append(".male");
        }
        if (this.status.sex == 1) {
            s.append(".female");
        }
        if (this.getKingdomId() != 0) {
            s.append('.');
            Kingdom kingdomt = Kingdoms.getKingdom(this.getKingdomId());
            if (kingdomt.getTemplate() != this.getKingdomId()) {
                s.append(Kingdoms.getSuffixFor(kingdomt.getTemplate()));
            }
            s.append(Kingdoms.getSuffixFor(this.getKingdomId()));
            if (this.status.disease > 0) {
                s.append("diseased.");
            }
        } else {
            s.append('.');
            if (this.status.disease > 0) {
                s.append("diseased.");
            }
        }
        s.append(WurmCalendar.getSpecialMapping(false));
        return s.toString();
    }

    public String getHitSound() {
        return this.template.getHitSound(this.getSex());
    }

    public String getDeathSound() {
        return this.template.getDeathSound(this.getSex());
    }

    public final boolean hasNoServerSound() {
        return this.template.noServerSounds();
    }

    public void setStructure(@Nullable Structure struct) {
        if (struct == null) {
            this.status.setBuildingId(-10L);
        }
        this.structure = struct;
    }

    public float getNoticeChance() {
        if (this.template.getTemplateId() == 29 || this.template.getTemplateId() == 28 || this.template.getTemplateId() == 4) {
            return 0.2f;
        }
        if (this.template.getTemplateId() == 5) {
            return 0.3f;
        }
        if (this.template.getTemplateId() == 31 || this.template.getTemplateId() == 30 || this.template.getTemplateId() == 6) {
            return 0.4f;
        }
        if (this.template.getTemplateId() == 7) {
            return 0.6f;
        }
        if (this.template.getTemplateId() == 33 || this.template.getTemplateId() == 32 || this.template.getTemplateId() == 8) {
            return 0.65f;
        }
        return 1.0f;
    }

    public Structure getStructure() throws NoSuchStructureException {
        if (this.structure == null) {
            throw new NoSuchStructureException("This creature has no structure");
        }
        return this.structure;
    }

    public boolean hasLink() {
        return false;
    }

    public short getCentimetersLong() {
        return this.status.getBody().getCentimetersLong();
    }

    public short getCentimetersHigh() {
        return this.status.getBody().getCentimetersHigh();
    }

    public short getCentimetersWide() {
        return this.status.getBody().getCentimetersWide();
    }

    public void setCentimetersLong(short centimetersLong) {
        this.status.getBody().setCentimetersLong(centimetersLong);
    }

    public void setCentimetersHigh(short centimetersHigh) {
        this.status.getBody().setCentimetersHigh(centimetersHigh);
    }

    public void setCentimetersWide(short centimetersWide) {
        this.status.getBody().setCentimetersWide(centimetersWide);
    }

    public float getWeight() {
        return this.status.getBody().getWeight(this.getStatus().fat);
    }

    public int getSize() {
        return this.template.getSize();
    }

    public boolean isClimber() {
        return this.template.climber;
    }

    public boolean addItemWatched(Item watched) {
        return true;
    }

    public boolean removeItemWatched(Item watched) {
        return true;
    }

    public boolean isItemWatched(Item watched) {
        return true;
    }

    public boolean isPaying() {
        return true;
    }

    public boolean isReallyPaying() {
        return true;
    }

    public int getPower() {
        return 0;
    }

    public void dropItem(Item item) {
        long parentId = item.getParentId();
        item.setPosXY(this.getPosX(), this.getPosY());
        if (parentId != -10L) {
            try {
                Item parent = Items.getItem(parentId);
                parent.dropItem(item.getWurmId(), false);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        int tilex = this.getTileX();
        int tiley = this.getTileY();
        try {
            Zone zone = Zones.getZone(tilex, tiley, this.isOnSurface());
            VolaTile t = zone.getOrCreateTile(tilex, tiley);
            if (t != null) {
                t.addItem(item, false, false);
            } else {
                int x = Server.rand.nextInt(Zones.worldTileSizeX);
                int y = Server.rand.nextInt(Zones.worldTileSizeY);
                t = Zones.getOrCreateTile(x, y, true);
                t.addItem(item, false, false);
            }
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, nsz.getMessage(), nsz);
        }
    }

    public void setDestroyed() {
        if (this.decisions != null) {
            this.decisions.clearOrders();
            this.decisions = null;
        }
        this.getStatus().setPath(null);
        try {
            this.savePosition(-10);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        this.damageCounter = 0;
        this.status.dead = true;
        this.setLeader(null);
        if (this.followers != null) {
            this.stopLeading();
        }
        if (this.isTrading()) {
            this.getTrade().end(this, true);
        }
        this.setTarget(-10L, true);
        this.destroyVisionArea();
        if (this.isVehicle()) {
            Vehicles.destroyVehicle(this.getWurmId());
        }
    }

    public void destroy() {
        GuardTower tower;
        int x;
        Item[] items;
        if (this.isDominated()) {
            this.setDominator(-10L);
        }
        this.getCurrentTile().deleteCreature(this);
        this.setDestroyed();
        if (this.getSpellEffects() != null) {
            this.getSpellEffects().destroy(false);
        }
        try {
            this.skills.delete();
        }
        catch (Exception ex) {
            logger.log(Level.INFO, "Error when deleting creature skills: " + ex.getMessage(), ex);
        }
        try {
            items = this.possessions.getInventory().getAllItems(true);
            for (x = 0; x < items.length; ++x) {
                if (!items[x].isUnique()) {
                    Items.destroyItem(items[x].getWurmId());
                    continue;
                }
                this.dropItem(items[x]);
            }
            Items.destroyItem(this.possessions.getInventory().getWurmId());
        }
        catch (Exception e) {
            logger.log(Level.INFO, "Error when decaying items: " + e.getMessage(), e);
        }
        try {
            items = this.getBody().getBodyItem().getAllItems(true);
            for (x = 0; x < items.length; ++x) {
                if (!items[x].isUnique()) {
                    Items.destroyItem(items[x].getWurmId());
                    continue;
                }
                this.dropItem(items[x]);
            }
            Items.destroyItem(this.getBody().getBodyItem().getWurmId());
        }
        catch (Exception e) {
            logger.log(Level.INFO, "Error when decaying body items: " + e.getMessage(), e);
        }
        if (this.citizenVillage != null) {
            Wagoner[] wagoners;
            Guard[] guards;
            Village vill = this.citizenVillage;
            for (Guard lGuard : guards = this.citizenVillage.getGuards()) {
                if (lGuard.getCreature() != this) continue;
                vill.deleteGuard(this, false);
                if (!this.isSpiritGuard()) continue;
                vill.plan.destroyGuard(this);
            }
            for (Wagoner wagoner : wagoners = vill.getWagoners()) {
                if (wagoner.getWurmId() != this.getWurmId()) continue;
                vill.deleteWagoner(this);
            }
        }
        if (this.isNpcTrader() && Economy.getEconomy().getShop(this, true) != null) {
            if (Economy.getEconomy().getShop(this, true).getMoney() > 0L) {
                Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + Economy.getEconomy().getShop(this, true).getMoney());
            }
            Economy.deleteShop(this.id);
        }
        if (this.isKingdomGuard() && (tower = Kingdoms.getTower(this)) != null) {
            try {
                tower.destroyGuard(this);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
        Creatures.getInstance().permanentlyDelete(this);
    }

    public boolean isBreakFence() {
        return this.template.isBreakFence();
    }

    public boolean isCareful() {
        return this.template.isCareful();
    }

    public final void attackTower() {
        if (this.isOnSurface() && !this.isFriendlyKingdom(this.getCurrentKingdom())) {
            for (int x = Zones.safeTileX(this.getTileX() - 3); x < Zones.safeTileX(this.getTileX() + 3); ++x) {
                for (int y = Zones.safeTileY(this.getTileY() - 3); y < Zones.safeTileY(this.getTileY() + 3); ++y) {
                    Item[] items;
                    VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
                    if (t == null) continue;
                    for (Item i : items = t.getItems()) {
                        if (!i.isGuardTower() || this.isFriendlyKingdom(i.getKingdom())) continue;
                        GuardTower tower = Kingdoms.getTower(i);
                        if (i.getCurrentQualityLevel() > 50.0f) {
                            if (tower != null) {
                                tower.sendAttackWarning();
                            }
                            this.turnTowardsTile((short)i.getTileX(), (short)i.getTileY());
                            this.playAnimation("fight_strike", false);
                            Server.getInstance().broadCastAction(this.getName() + " attacks the " + i.getName() + ".", this, 5);
                            i.setDamage(i.getDamage() + (float)(this.getStrengthSkill() / 1000.0));
                            if (Server.rand.nextInt(300) != 0) continue;
                            if (Server.rand.nextBoolean()) {
                                GuardTower.spawnCommander(i, i.getKingdom());
                            }
                            for (int n = 0; n < 2 + Server.rand.nextInt(4); ++n) {
                                GuardTower.spawnSoldier(i, i.getKingdom());
                            }
                            continue;
                        }
                        if (Servers.localServer.HOMESERVER || Server.rand.nextInt(300) != 0 || tower == null || tower.hasLiveGuards()) continue;
                        Server.getInstance().broadCastAction(this.getName() + " conquers the " + tower.getName() + "!", this, 5);
                        Server.getInstance().broadCastSafe(this.getName() + " conquers " + tower.getName() + ".");
                        Kingdoms.convertTowersWithin(i.getTileX() - 10, i.getTileY() - 10, i.getTileX() + 10, i.getTileY() + 10, this.getKingdomId());
                    }
                }
            }
        }
    }

    public void breakout() {
        if (!this.isDominated() && ((this.isCaveDweller() || this.isBreakFence()) && this.status.hunger >= 60000 || this.isUnique()) && !this.isSubmerged() && Server.rand.nextInt(100) == 0) {
            Village breakoutVillage = Zones.getVillage(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (breakoutVillage != null && breakoutVillage.isPermanent) {
                return;
            }
            if (this.isBreakFence() && this.currentTile != null) {
                Wall tobreak;
                Wall[] walls;
                if (this.currentTile.getStructure() != null && (walls = this.currentTile.getWallsForLevel(this.getFloorLevel())).length > 0 && !(tobreak = walls[Server.rand.nextInt(walls.length)]).isIndestructible()) {
                    Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                    if (this.isUnique()) {
                        tobreak.setDamage(tobreak.getDamage() + 100.0f);
                    } else {
                        tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0f * tobreak.getDamageModifier());
                    }
                }
                boolean onSurface = true;
                if ((this.isOnSurface() || this.currentTile.isTransition) && this.isUnique()) {
                    Wall[] walls2;
                    VolaTile t = Zones.getTileOrNull(this.getTileX() + 1, this.getTileY(), true);
                    if (t != null && (walls2 = t.getWallsForLevel(Math.max(0, this.getFloorLevel()))).length > 0) {
                        for (Wall tobreak2 : walls2) {
                            if (tobreak2.isIndestructible() || tobreak2.getTileX() != this.getTileX() + 1 || tobreak2.isHorizontal()) continue;
                            Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak2.getName() + ".", this, 5);
                            if (this.isUnique()) {
                                tobreak2.setDamage(tobreak2.getDamage() + 100.0f);
                                continue;
                            }
                            tobreak2.setDamage(tobreak2.getDamage() + (float)this.getStrengthSkill() / 10.0f * tobreak2.getDamageModifier());
                        }
                    }
                    if ((t = Zones.getTileOrNull(this.getTileX() - 1, this.getTileY(), true)) != null && (walls2 = t.getWallsForLevel(Math.max(0, this.getFloorLevel()))).length > 0) {
                        for (Wall tobreak2 : walls2) {
                            if (tobreak2.isIndestructible() || tobreak2.getTileX() != this.getTileX() || tobreak2.isHorizontal()) continue;
                            Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak2.getName() + ".", this, 5);
                            if (this.isUnique()) {
                                tobreak2.setDamage(tobreak2.getDamage() + 100.0f);
                                continue;
                            }
                            tobreak2.setDamage(tobreak2.getDamage() + (float)this.getStrengthSkill() / 10.0f * tobreak2.getDamageModifier());
                        }
                    }
                    if ((t = Zones.getTileOrNull(this.getTileX(), this.getTileY() - 1, true)) != null && (walls2 = t.getWallsForLevel(Math.max(0, this.getFloorLevel()))).length > 0) {
                        for (Wall tobreak2 : walls2) {
                            if (tobreak2.isIndestructible() || tobreak2.getTileY() != this.getTileY() || !tobreak2.isHorizontal()) continue;
                            Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak2.getName() + ".", this, 5);
                            if (this.isUnique()) {
                                tobreak2.setDamage(tobreak2.getDamage() + 100.0f);
                                continue;
                            }
                            tobreak2.setDamage(tobreak2.getDamage() + (float)this.getStrengthSkill() / 10.0f * tobreak2.getDamageModifier());
                        }
                    }
                    if ((t = Zones.getTileOrNull(this.getTileX(), this.getTileY() + 1, true)) != null && (walls2 = t.getWallsForLevel(Math.max(0, this.getFloorLevel()))).length > 0) {
                        for (Wall tobreak2 : walls2) {
                            if (tobreak2.isIndestructible() || tobreak2.getTileY() != this.getTileY() + 1 || !tobreak2.isHorizontal()) continue;
                            Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak2.getName() + ".", this, 5);
                            if (this.isUnique()) {
                                tobreak2.setDamage(tobreak2.getDamage() + 100.0f);
                                continue;
                            }
                            tobreak2.setDamage(tobreak2.getDamage() + (float)this.getStrengthSkill() / 10.0f * tobreak2.getDamageModifier());
                        }
                    }
                }
                Fence[] fences = this.currentTile.getFencesForLevel(this.currentTile.isTransition ? 0 : this.getFloorLevel());
                boolean onlyHoriz = false;
                boolean onlyVert = false;
                if (fences == null) {
                    if (this.isOnSurface()) {
                        VolaTile t;
                        if (fences == null && (t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true)) != null) {
                            fences = t.getFencesForLevel(this.getFloorLevel());
                            onlyVert = true;
                        }
                        if (fences == null && (t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true)) != null) {
                            fences = t.getFencesForLevel(this.getFloorLevel());
                            onlyHoriz = true;
                        }
                    }
                    if (this.currentTile.isTransition) {
                        if (!this.isOnSurface()) {
                            VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY(), true);
                            if (t != null) {
                                fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                            }
                            if (fences == null && (t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true)) != null) {
                                fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                                onlyVert = true;
                            }
                            if (fences == null && (t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true)) != null) {
                                fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                                onlyHoriz = true;
                            }
                        }
                        if (this.getFloorLevel() <= 0 && Tiles.isMineDoor(Tiles.decodeType(Zones.getTileIntForTile(this.currentTile.tilex, this.currentTile.tiley, 0)))) {
                            int currQl = Server.getWorldResource(this.currentTile.tilex, this.currentTile.tiley);
                            int damage = 1000;
                            currQl = Math.max(0, currQl - 1000);
                            Server.setWorldResource(this.currentTile.tilex, this.currentTile.tiley, currQl);
                            try {
                                MethodsStructure.sendDestroySound(this, this.getBody().getBodyPart(13), Tiles.decodeType(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == 25);
                            }
                            catch (Exception ex) {
                                logger.log(Level.INFO, this.getName() + ex.getMessage());
                            }
                            if (currQl == 0) {
                                TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, 0, this.getWurmId(), 174);
                                TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, -1, this.getWurmId(), 174);
                                if (Tiles.decodeType(Server.caveMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                                    Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                                } else {
                                    Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                                }
                                Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
                                MineDoorPermission.deleteMineDoor(this.currentTile.tilex, this.currentTile.tiley);
                                Server.getInstance().broadCastAction(this.getName() + " damages a door and the last parts fall down with a crash.", this, 5);
                            } else {
                                Server.getInstance().broadCastAction(this.getName() + " damages the door.", this, 5);
                            }
                        }
                    }
                }
                if (fences != null) {
                    for (Fence f : fences) {
                        if (f.isIndestructible()) continue;
                        if (f.isHorizontal()) {
                            if (onlyVert) continue;
                            Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + f.getName() + ".", this, 5);
                            if (this.isUnique()) {
                                f.setDamage(f.getDamage() + (float)Server.rand.nextInt(100));
                                continue;
                            }
                            if (f.getVillage() != null) {
                                f.getVillage().addTarget(this);
                            }
                            f.setDamage(f.getDamage() + (float)this.getStrengthSkill() / 10.0f * f.getDamageModifier());
                            continue;
                        }
                        if (onlyHoriz) continue;
                        Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + f.getName() + ".", this, 5);
                        if (this.isUnique()) {
                            f.setDamage(f.getDamage() + (float)Server.rand.nextInt(100));
                            continue;
                        }
                        if (f.getVillage() != null) {
                            f.getVillage().addTarget(this);
                        }
                        f.setDamage(f.getDamage() + (float)this.getStrengthSkill() / 10.0f * f.getDamageModifier());
                    }
                }
            }
            if (this.isUnique() && !this.isOnSurface() && Server.rand.nextInt(500) == 0) {
                Village v;
                int ty;
                boolean breakReinforcement = this.isUnique();
                int tx = Zones.safeTileX(this.getTileX() - 1);
                int t = Zones.getTileIntForTile(tx, ty = Zones.safeTileY(this.getTileY()), 0);
                if (Tiles.isMineDoor(Tiles.decodeType(t))) {
                    int currQl = Server.getWorldResource(tx, ty);
                    try {
                        MethodsStructure.sendDestroySound(this, this.getBody().getBodyPart(13), Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)) == 25);
                        currQl = Math.max(0, currQl - 1000);
                        Server.setWorldResource(tx, ty, currQl);
                        if (currQl == 0) {
                            TileEvent.log(tx, ty, 0, this.getWurmId(), 174);
                            TileEvent.log(tx, ty, -1, this.getWurmId(), 174);
                            if (Tiles.decodeType(Server.caveMesh.getTile(tx, ty)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                                Server.setSurfaceTile(tx, ty, Tiles.decodeHeight(Server.surfaceMesh.getTile(tx, ty)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                            } else {
                                Server.setSurfaceTile(tx, ty, Tiles.decodeHeight(Server.surfaceMesh.getTile(tx, ty)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                            }
                            Players.getInstance().sendChangedTile(tx, ty, true, true);
                            MineDoorPermission.deleteMineDoor(tx, ty);
                            Server.getInstance().broadCastAction(this.getNameWithGenus() + " damages a door and the last parts fall down with a crash.", this, 5);
                        }
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage());
                    }
                }
                t = Zones.getTileIntForTile(tx, ty, -1);
                if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                    Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
                    Players.getInstance().sendChangedTile(tx, ty, false, true);
                }
                if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id && ((v = Zones.getVillage(tx, ty, true)) == null || this.isOnPvPServer() || this.isUnique())) {
                    TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 2, false, null);
                    if (v != null) {
                        v.addTarget(this);
                    }
                }
                tx = Zones.safeTileX(this.getTileX());
                ty = Zones.safeTileY(this.getTileY() - 1);
                t = Zones.getTileIntForTile(tx, ty, -1);
                if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                    Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
                    Players.getInstance().sendChangedTile(tx, ty, false, true);
                }
                if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id && ((v = Zones.getVillage(tx, ty, true)) == null || this.isOnPvPServer() || this.isUnique())) {
                    TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 3, false, null);
                    if (v != null) {
                        v.addTarget(this);
                    }
                }
                tx = Zones.safeTileX(this.getTileX() + 1);
                ty = Zones.safeTileY(this.getTileY());
                t = Zones.getTileIntForTile(tx, ty, -1);
                if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                    Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
                    Players.getInstance().sendChangedTile(tx, ty, false, true);
                }
                if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id && ((v = Zones.getVillage(tx, ty, true)) == null || this.isOnPvPServer() || this.isUnique())) {
                    TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 4, false, null);
                    if (v != null) {
                        v.addTarget(this);
                    }
                }
                tx = Zones.safeTileX(this.getTileX());
                ty = Zones.safeTileY(this.getTileY() + 1);
                t = Zones.getTileIntForTile(tx, ty, -1);
                if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                    Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
                    Players.getInstance().sendChangedTile(tx, ty, false, true);
                }
                if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id && ((v = Zones.getVillage(tx, ty, true)) == null || this.isOnPvPServer() || this.isUnique())) {
                    TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 5, false, null);
                    if (v != null) {
                        v.addTarget(this);
                    }
                }
            }
        }
    }

    public int getMaxHuntDistance() {
        if (this.isDominated()) {
            return 20;
        }
        return this.template.getMaxHuntDistance();
    }

    /*
     * Unable to fully structure code
     */
    public Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder) throws NoPathException {
        path = null;
        pf = pathfinder != null ? pathfinder : new PathFinder();
        this.setPathfindcounter(this.getPathfindCounter() + 1);
        if (this.getPathfindCounter() < 10 || this.target != -10L || this.getPower() > 0) {
            if (this.isSpiritGuard() && this.citizenVillage != null) {
                if (this.target == -10L) {
                    if (this.isWithinTileDistanceTo(targetX, targetY, (int)(this.status.getPositionZ() + this.getAltOffZ()) >> 2, this.getMaxHuntDistance())) {
                        path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 10);
                    }
                } else {
                    try {
                        path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 10);
                    }
                    catch (NoPathException nsp) {
                        if (this.currentVillage == this.citizenVillage) {
                            if (targetX < this.citizenVillage.getStartX() - 5 || targetX > this.citizenVillage.getEndX() + 5 || targetY < this.citizenVillage.getStartY() - 5 || targetY > this.citizenVillage.getEndY() + 5) {
                                this.setTargetNOID = true;
                            }
                            if (this.getTarget() == null) ** GOTO lbl34
                            this.creatureToBlinkTo = this.getTarget();
                            return null;
                        }
                        if (this.getTarget() == null) ** GOTO lbl34
                        this.creatureToBlinkTo = this.getTarget();
                        return null;
                    }
                }
            } else if (this.isWithinTileDistanceTo(targetX, targetY, (int)this.status.getPositionZ() >> 2, Math.max(this.getMaxHuntDistance(), this.template.getVision()))) {
                path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 5);
            } else if (this.isUnique() || this.isKingdomGuard() || this.isDominated() || this.template.isTowerBasher()) {
                if (this.target == -10L) {
                    path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 5);
                } else {
                    this.setTargetNOID = true;
                }
            }
        } else {
            throw new NoPathException("No pathing now");
        }
lbl34:
        // 8 sources

        if (path != null) {
            this.setPathfindcounter(0);
        }
        return path;
    }

    public boolean isSentinel() {
        return this.template.isSentinel();
    }

    public boolean isNpc() {
        return false;
    }

    public boolean isTrader() {
        if (this.isReborn()) {
            return false;
        }
        if (this.template.getTemplateId() == 1 && !this.isPlayer()) {
            return false;
        }
        return this.template.isTrader();
    }

    public boolean canEat() {
        return this.getStatus().canEat();
    }

    public boolean isHungry() {
        return this.getStatus().isHungry();
    }

    public boolean isNeedFood() {
        return this.template.isNeedFood();
    }

    public boolean isMoveRandom() {
        return this.template.isMoveRandom();
    }

    public boolean isSwimming() {
        return this.template.isSwimming();
    }

    public boolean isAnimal() {
        return this.template.isAnimal();
    }

    public boolean isHuman() {
        return this.template.isHuman();
    }

    public boolean isRegenerating() {
        return this.template.isRegenerating() || this.isUndead();
    }

    public boolean isDragon() {
        return this.template.isDragon();
    }

    public boolean isTypeFleeing() {
        return this.isSpy() || this.template.isFleeing();
    }

    public boolean isMonster() {
        return this.template.isMonster();
    }

    public boolean isInvulnerable() {
        return this.template.isInvulnerable();
    }

    public boolean isNpcTrader() {
        return this.template.isNpcTrader();
    }

    public boolean isAggHuman() {
        if (this.isReborn()) {
            return true;
        }
        return this.template.isAggHuman();
    }

    public boolean isMoveLocal() {
        return this.template.isMoveLocal() && this.status.modtype != 99;
    }

    public boolean isMoveGlobal() {
        boolean shouldMove = false;
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled() && this.getCurrentTile().getVillage() != null && (this.isBred() || this.isBranded() || this.isCaredFor()) && this.target == -10L) {
            shouldMove = true;
        }
        return this.template.isMoveGlobal() || this.status.modtype == 99 || shouldMove;
    }

    public boolean shouldFlee() {
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            if (this.getCurrentTile().getVillage() != null && (this.isBred() || this.isBranded() || this.isCaredFor())) {
                return false;
            }
            if (this.getStatus().isChampion()) {
                return false;
            }
            if (this.fleeCounter > 0) {
                Long[] visibleCreatures;
                for (Long lCret : visibleCreatures = this.getVisionArea().getSurface().getCreatures()) {
                    try {
                        Creature cret = Server.getInstance().getCreature(lCret);
                        if (cret.getPower() != 0 && !Servers.localServer.testServer || !cret.isPlayer() && !cret.isAggHuman() && !cret.isCarnivore() && !cret.isMonster() && !cret.isHunter()) continue;
                        float modifier = 1.0f;
                        if (this.getCurrentTile().getVillage() != null && cret.isPlayer()) {
                            modifier = 2.0f;
                        }
                        this.sendToLoggers("checking if should flee from " + cret.getName() + ": " + (cret.getBaseCombatRating() - Math.abs(cret.getPos2f().distance(this.getPos2f()) / 4.0f)) + " vs " + this.getBaseCombatRating() * modifier);
                        if (!(cret.getBaseCombatRating() - Math.abs(cret.getPos2f().distance(this.getPos2f()) / 2.0f) > this.getBaseCombatRating() * modifier)) continue;
                        return true;
                    }
                    catch (NoSuchPlayerException | NoSuchCreatureException wurmServerException) {
                        // empty catch block
                    }
                }
            }
            return false;
        }
        if (this.getStatus().isChampion()) {
            return false;
        }
        return this.fleeCounter > 0;
    }

    public boolean isGrazer() {
        return this.template.isGrazer();
    }

    public boolean isHerd() {
        return this.template.isHerd();
    }

    public boolean isHunter() {
        return this.template.isHunter();
    }

    public boolean isMilkable() {
        return this.template.isMilkable() && this.getSex() == 1 && this.getStatus().age >= 3;
    }

    public boolean isReborn() {
        return this.getStatus().reborn;
    }

    public boolean isDominatable(Creature aDominator) {
        if (this.getLeader() != null && this.getLeader() != aDominator) {
            return false;
        }
        if (this.isRidden() || this.hitchedTo != null) {
            return false;
        }
        return this.template.isDominatable();
    }

    public final int getAggressivity() {
        return this.template.getAggressivity();
    }

    final byte getCombatDamageType() {
        return this.template.getCombatDamageType();
    }

    final float getBreathDamage() {
        if (this.isUndead()) {
            return 10.0f;
        }
        if (this.isReborn()) {
            return Math.max(3.0f, this.template.getBreathDamage());
        }
        return this.template.getBreathDamage();
    }

    public float getHandDamage() {
        if (this.isUndead()) {
            return 5.0f;
        }
        if (this.isReborn()) {
            return Math.max(3.0f, this.template.getHandDamage());
        }
        return this.template.getHandDamage();
    }

    public float getBiteDamage() {
        if (this.isUndead()) {
            return 8.0f;
        }
        if (this.isReborn()) {
            return Math.max(5.0f, this.template.getBiteDamage());
        }
        return this.template.getBiteDamage();
    }

    public float getKickDamage() {
        if (this.isReborn()) {
            return Math.max(2.0f, this.template.getKickDamage());
        }
        return this.template.getKickDamage();
    }

    public float getHeadButtDamage() {
        if (this.isReborn()) {
            return Math.max(4.0f, this.template.getKickDamage());
        }
        return this.template.getHeadButtDamage();
    }

    public Logger getLogger() {
        return null;
    }

    public boolean isUnique() {
        return this.template.isUnique();
    }

    public boolean isKingdomGuard() {
        return this.template.isKingdomGuard();
    }

    public boolean isGuard() {
        return this.isKingdomGuard() || this.isSpiritGuard() || this.isWarGuard();
    }

    public boolean isGhost() {
        return this.template.isGhost();
    }

    public boolean unDead() {
        return this.template.isUndead();
    }

    public final boolean onlyAttacksPlayers() {
        return this.template.onlyAttacksPlayers();
    }

    public boolean isSpiritGuard() {
        return this.template.isSpiritGuard();
    }

    public boolean isZombieSummoned() {
        return this.template.getTemplateId() == 69;
    }

    public boolean isBartender() {
        return this.template.isBartender();
    }

    public boolean isDefendKingdom() {
        return this.template.isDefendKingdom();
    }

    public boolean isNotFemale() {
        return this.getSex() != 1;
    }

    public boolean isAggWhitie() {
        return this.template.isAggWhitie() || this.isReborn();
    }

    public boolean isHerbivore() {
        return this.template.isHerbivore();
    }

    public boolean isCarnivore() {
        return this.template.isCarnivore();
    }

    public boolean isOmnivore() {
        return this.template.isOmnivore();
    }

    public boolean isCaveDweller() {
        return this.template.isCaveDweller();
    }

    public boolean isSubmerged() {
        return this.template.isSubmerged();
    }

    public boolean isEggLayer() {
        return this.template.isEggLayer();
    }

    public int getEggTemplateId() {
        return this.template.getEggTemplateId();
    }

    public int getMaxGroupAttackSize() {
        if (this.isUnique()) {
            return 100;
        }
        float mod = this.getStatus().getBattleRatingTypeModifier();
        return (int)Math.max((float)this.template.getMaxGroupAttackSize(), (float)this.template.getMaxGroupAttackSize() * mod);
    }

    public int getGroupSize() {
        int nums = 0;
        for (int x = Math.max(0, this.getCurrentTile().getTileX() - 3); x < Math.min(this.getCurrentTile().getTileX() + 3, Zones.worldTileSizeX - 1); ++x) {
            for (int y = Math.max(0, this.getCurrentTile().getTileY() - 3); y < Math.min(this.getCurrentTile().getTileY() + 3, Zones.worldTileSizeY - 1); ++y) {
                Creature[] xret;
                VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
                if (t == null || t.getCreatures().length <= 0) continue;
                for (Creature lElement : xret = t.getCreatures()) {
                    if (lElement.getTemplate().getTemplateId() != this.template.getTemplateId() && lElement.getTemplate().getTemplateId() != this.template.getLeaderTemplateId()) continue;
                    ++nums;
                }
            }
        }
        return nums;
    }

    public final TilePos getAdjacentTilePos(TilePos pos) {
        switch (Server.rand.nextInt(8)) {
            case 0: {
                return pos.East();
            }
            case 1: {
                return pos.South();
            }
            case 2: {
                return pos.West();
            }
            case 3: {
                return pos.North();
            }
            case 4: {
                return pos.NorthEast();
            }
            case 5: {
                return pos.NorthWest();
            }
            case 6: {
                return pos.SouthWest();
            }
            case 7: {
                return pos.SouthEast();
            }
        }
        return pos;
    }

    public void checkEggLaying() {
        if (this.isEggLayer()) {
            byte type;
            if (this.template.getTemplateId() == 53) {
                if (Server.rand.nextInt(7200) == 0) {
                    if (WurmCalendar.isAfterEaster()) {
                        this.destroy();
                        Server.getInstance().broadCastAction(this.getNameWithGenus() + " suddenly vanishes down into a hole!", this, 10);
                    } else {
                        try {
                            Item egg = ItemFactory.createItem(466, 50.0f, null);
                            egg.putItemInfrontof(this);
                            Server.getInstance().broadCastAction(this.getNameWithGenus() + " throws something in the air!", this, 10);
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, ex.getMessage(), ex);
                        }
                    }
                }
            } else if (this.status.getSex() == 1 && this.isNeedFood() && !this.canEat() && (Items.mayLayEggs() || this.isUnique()) && Server.rand.nextInt(20000 * (this.isUnique() ? 1000 : 1)) == 0 && this.isOnSurface() && ((type = Tiles.decodeType(Server.surfaceMesh.getTile(this.getCurrentTile().tilex, this.getCurrentTile().tiley))) == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id || type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_DIRT_PACKED.id)) {
                int templateId = 464;
                if (this.template.getSize() > 4) {
                    templateId = 465;
                }
                try {
                    Item egg = ItemFactory.createItem(templateId, 99.0f, this.getPosX(), this.getPosY(), this.status.getRotation(), this.isOnSurface(), (byte)0, this.getStatus().getBridgeId(), null);
                    if (templateId == 465 || Server.rand.nextInt(5) == 0) {
                        egg.setData1(this.template.getEggTemplateId());
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                }
                this.status.hunger = 60000;
            }
        }
    }

    public boolean isNoSkillFor(Creature attacker) {
        if ((this.isKingdomGuard() || this.isSpiritGuard() || this.isZombieSummoned() || this.isPlayer() && attacker.isPlayer() || this.isWarGuard()) && this.isFriendlyKingdom(attacker.getKingdomId())) {
            return true;
        }
        if (this.isPlayer() && attacker.isPlayer()) {
            if (Players.getInstance().isOverKilling(attacker.getWurmId(), this.getWurmId())) {
                return true;
            }
            if (((Player)this).getSaveFile().getIpaddress().equals(((Player)attacker).getSaveFile().getIpaddress())) {
                return true;
            }
        }
        return false;
    }

    public int[] forageForFood(VolaTile currTile) {
        int[] toReturn = new int[]{-1, -1};
        if (this.canEat() && this.isNeedFood()) {
            for (int x = -2; x <= 2; ++x) {
                for (int y = -2; y <= 2; ++y) {
                    byte type;
                    VolaTile t = Zones.getTileOrNull(Zones.safeTileX(currTile.getTileX() + x), Zones.safeTileY(currTile.getTileY() + y), this.isOnSurface());
                    if (t != null) {
                        Item[] its;
                        for (Item lIt : its = t.getItems()) {
                            if (!lIt.isEdibleBy(this) || Server.rand.nextInt(10) != 0) continue;
                            this.sendToLoggers("Found " + lIt.getName());
                            toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
                            toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
                            return toReturn;
                        }
                    }
                    if (!this.isGrazer() || !this.canEat() || Server.rand.nextInt(9) != 0 || (type = Zones.getTextureForTile(Zones.safeTileX(currTile.getTileX() + x), Zones.safeTileY(currTile.getTileY() + y), this.getLayer())) != Tiles.Tile.TILE_GRASS.id && type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id && type != Tiles.Tile.TILE_STEPPE.id && type != Tiles.Tile.TILE_ENCHANTED_GRASS.id) continue;
                    this.sendToLoggers("Found grass or field");
                    toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
                    toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
                    return toReturn;
                }
            }
        }
        return toReturn;
    }

    public void blinkTo(int tilex, int tiley, int layer, int floorLevel) {
        this.getCurrentTile().deleteCreatureQuick(this);
        try {
            this.setPositionX((tilex << 2) + 2);
            this.setPositionY((tiley << 2) + 2);
            this.setLayer(Math.min(0, layer), false);
            if (floorLevel > 0) {
                this.pushToFloorLevel(floorLevel);
            } else {
                this.setPositionZ(Zones.calculateHeight(this.getStatus().getPositionX(), this.getStatus().getPositionY(), this.isOnSurface()));
            }
            Zone z = Zones.getZone(tilex, tiley, layer >= 0);
            z.addCreature(this.getWurmId());
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.getName() + " - " + tilex + ", " + tiley + ", " + layer + ", " + floorLevel + ": " + ex.getMessage(), ex);
        }
    }

    public final boolean isBeachDweller() {
        return this.template.isBeachDweller();
    }

    public final boolean isWoolProducer() {
        return this.template.isWoolProducer();
    }

    public boolean isTargetTileTooHigh(int targetX, int targetY, int currentTileNum, boolean swimming) {
        VolaTile stile;
        if (this.getFloorLevel() > 0) {
            return false;
        }
        if (this.isFlying()) {
            return false;
        }
        short currheight = Tiles.decodeHeight(currentTileNum);
        short[] lSteepness = Creature.getTileSteepness(targetX, targetY, this.isOnSurface());
        if (swimming && lSteepness[0] < -200 && currheight > lSteepness[0] && !this.isFloating()) {
            return true;
        }
        if (this.isBeachDweller()) {
            if (currheight > 20 && lSteepness[0] > currheight) {
                return true;
            }
            if (currheight < 0 && lSteepness[0] > 0 && !WurmCalendar.isNight()) {
                return true;
            }
        }
        if (this.isOnSurface() && (stile = Zones.getTileOrNull(targetX, targetY, this.isOnSurface())) != null && stile.getStructure() != null && stile.getStructure().isTypeBridge() && (stile.getStructure().isHorizontal() ? stile.getStructure().getMaxX() == stile.getTileX() || stile.getStructure().getMinX() == stile.getTileX() : stile.getStructure().getMaxY() == stile.getTileY() || stile.getStructure().getMinY() == stile.getTileY())) {
            return false;
        }
        if (currheight < 500) {
            return false;
        }
        if (!swimming && (double)(lSteepness[0] - currheight) > 60.0 * Math.max(1.0, Creature.getTileRange(this, targetX, targetY)) && lSteepness[1] > 20) {
            if (Creatures.getInstance().isLog()) {
                logger.log(Level.INFO, this.getName() + " Skipping moving up since avg steep=" + lSteepness[0] + "=" + (lSteepness[0] - currheight) + ">" + 60.0 * Math.max(1.0, Creature.getTileRange(this, targetX, targetY)) + " at " + targetX + "," + targetY + " from " + this.getTileX() + ", " + this.getTileY());
            }
            return true;
        }
        if (!swimming && (double)(currheight - lSteepness[0]) > 60.0 * Math.max(1.0, Creature.getTileRange(this, targetX, targetY)) && lSteepness[1] > 20) {
            if (Creatures.getInstance().isLog()) {
                logger.log(Level.INFO, this.getName() + " Skipping moving down since avg steep=" + lSteepness[0] + "=" + (lSteepness[0] - currheight) + ">" + 60.0 * Math.max(1.0, Creature.getTileRange(this, targetX, targetY)) + " at " + targetX + "," + targetY + " from " + this.getTileX() + ", " + this.getTileY());
            }
            return true;
        }
        return false;
    }

    public final long getBridgeId() {
        if (this.getStatus().getPosition() != null) {
            return this.getStatus().getPosition().getBridgeId();
        }
        return -10L;
    }

    public final boolean isWarGuard() {
        return this.template.isWarGuard();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public PathTile getMoveTarget(int seed) {
        if (this.getStatus() == null) {
            return null;
        }
        long now = System.currentTimeMillis();
        float lPosX = this.status.getPositionX();
        float lPosY = this.status.getPositionY();
        boolean hasTarget = false;
        int tilePosX = (int)lPosX >> 2;
        int tilePosY = (int)lPosY >> 2;
        int tx = tilePosX;
        int ty = tilePosY;
        try {
            int tile;
            block162: {
                block167: {
                    boolean stepOnBridge;
                    int tile2;
                    boolean swimming;
                    VolaTile t;
                    int tpy;
                    int tpx;
                    block219: {
                        Iterator<VolaTile> iterator;
                        block220: {
                            GuardTower gt;
                            int[] tiles;
                            block221: {
                                VolaTile currTile;
                                block222: {
                                    int y;
                                    block223: {
                                        block213: {
                                            block218: {
                                                int ctile;
                                                boolean abort;
                                                block166: {
                                                    Item wtarget;
                                                    int rand;
                                                    block217: {
                                                        block216: {
                                                            block215: {
                                                                block214: {
                                                                    boolean flee;
                                                                    block160: {
                                                                        int[] empty;
                                                                        int[] newarr;
                                                                        block165: {
                                                                            int rangeHeatmap22;
                                                                            Player[] crets2;
                                                                            block175: {
                                                                                int visibleCreatures2;
                                                                                Long[] rangeHeatmap22;
                                                                                block174: {
                                                                                    Long[] visibleCreatures2;
                                                                                    float[][] rangeHeatmap22;
                                                                                    int heatmapSize;
                                                                                    block172: {
                                                                                        block176: {
                                                                                            block177: {
                                                                                                block207: {
                                                                                                    block208: {
                                                                                                        int[] tiles2;
                                                                                                        VolaTile currTile2;
                                                                                                        Creature targ;
                                                                                                        block212: {
                                                                                                            block211: {
                                                                                                                block210: {
                                                                                                                    block209: {
                                                                                                                        int myGroup;
                                                                                                                        int targGroup;
                                                                                                                        block206: {
                                                                                                                            block198: {
                                                                                                                                block199: {
                                                                                                                                    block200: {
                                                                                                                                        block201: {
                                                                                                                                            int tpy2;
                                                                                                                                            int tpx2;
                                                                                                                                            block202: {
                                                                                                                                                int[] tiles3;
                                                                                                                                                block205: {
                                                                                                                                                    block204: {
                                                                                                                                                        block203: {
                                                                                                                                                            block189: {
                                                                                                                                                                block195: {
                                                                                                                                                                    int[] tiles4;
                                                                                                                                                                    block197: {
                                                                                                                                                                        block196: {
                                                                                                                                                                            block190: {
                                                                                                                                                                                block192: {
                                                                                                                                                                                    block194: {
                                                                                                                                                                                        int[] tiles5;
                                                                                                                                                                                        block193: {
                                                                                                                                                                                            block191: {
                                                                                                                                                                                                block184: {
                                                                                                                                                                                                    block188: {
                                                                                                                                                                                                        block185: {
                                                                                                                                                                                                            block186: {
                                                                                                                                                                                                                block187: {
                                                                                                                                                                                                                    block178: {
                                                                                                                                                                                                                        boolean changeLayer;
                                                                                                                                                                                                                        block161: {
                                                                                                                                                                                                                            block182: {
                                                                                                                                                                                                                                block183: {
                                                                                                                                                                                                                                    block181: {
                                                                                                                                                                                                                                        block179: {
                                                                                                                                                                                                                                            block180: {
                                                                                                                                                                                                                                                block168: {
                                                                                                                                                                                                                                                    block173: {
                                                                                                                                                                                                                                                        Long[] crets2;
                                                                                                                                                                                                                                                        block171: {
                                                                                                                                                                                                                                                            block169: {
                                                                                                                                                                                                                                                                Creature lTarget;
                                                                                                                                                                                                                                                                Order order;
                                                                                                                                                                                                                                                                block170: {
                                                                                                                                                                                                                                                                    int tile3;
                                                                                                                                                                                                                                                                    int ctile2;
                                                                                                                                                                                                                                                                    if (this.target != -10L && (this.fleeCounter <= 0 || this.target != -10L)) break block168;
                                                                                                                                                                                                                                                                    flee = false;
                                                                                                                                                                                                                                                                    if (!this.isDominated() || this.fleeCounter > 0) break block169;
                                                                                                                                                                                                                                                                    if (!this.hasOrders()) break block160;
                                                                                                                                                                                                                                                                    order = this.getFirstOrder();
                                                                                                                                                                                                                                                                    if (!order.isTile()) break block170;
                                                                                                                                                                                                                                                                    boolean swimming2 = false;
                                                                                                                                                                                                                                                                    int n = ctile2 = this.isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
                                                                                                                                                                                                                                                                    if (Tiles.decodeHeight(ctile2) <= 0) {
                                                                                                                                                                                                                                                                        swimming2 = true;
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                    if (!Tiles.isSolidCave(Tiles.decodeType(tile3 = Zones.getTileIntForTile(order.getTileX(), order.getTileY(), this.getLayer()))) && (Tiles.decodeHeight(tile3) > 0 || swimming2)) {
                                                                                                                                                                                                                                                                        if (this.isOnSurface()) {
                                                                                                                                                                                                                                                                            if (!this.isTargetTileTooHigh(order.getTileX(), order.getTileY(), tile3, swimming2)) {
                                                                                                                                                                                                                                                                                hasTarget = true;
                                                                                                                                                                                                                                                                                tilePosX = order.getTileX();
                                                                                                                                                                                                                                                                                tilePosY = order.getTileY();
                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                            break block160;
                                                                                                                                                                                                                                                                        } else {
                                                                                                                                                                                                                                                                            hasTarget = true;
                                                                                                                                                                                                                                                                            tilePosX = order.getTileX();
                                                                                                                                                                                                                                                                            tilePosY = order.getTileY();
                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                    break block160;
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                if (order.isCreature() && (lTarget = order.getCreature()) != null) {
                                                                                                                                                                                                                                                                    if (lTarget.isDead()) {
                                                                                                                                                                                                                                                                        this.removeOrder(order);
                                                                                                                                                                                                                                                                        break block160;
                                                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                                                        hasTarget = true;
                                                                                                                                                                                                                                                                        tilePosX = lTarget.getCurrentTile().tilex;
                                                                                                                                                                                                                                                                        tilePosY = lTarget.getCurrentTile().tiley;
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                break block160;
                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                            if (!this.isTypeFleeing() && !this.shouldFlee()) break block160;
                                                                                                                                                                                                                                                            if (!Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) break block171;
                                                                                                                                                                                                                                                            if (!this.isOnSurface() || this.getVisionArea() == null || this.getVisionArea().getSurface() == null) break block165;
                                                                                                                                                                                                                                                            heatmapSize = this.template.getVision() * 2 + 1;
                                                                                                                                                                                                                                                            rangeHeatmap22 = new float[heatmapSize][heatmapSize];
                                                                                                                                                                                                                                                            break block172;
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                        if (!this.isOnSurface()) break block165;
                                                                                                                                                                                                                                                        if (!Server.rand.nextBoolean()) break block173;
                                                                                                                                                                                                                                                        if (this.getCurrentTile() == null || this.getCurrentTile().getVillage() == null) break block165;
                                                                                                                                                                                                                                                        rangeHeatmap22 = crets2 = this.getVisionArea().getSurface().getCreatures();
                                                                                                                                                                                                                                                        visibleCreatures2 = rangeHeatmap22.length;
                                                                                                                                                                                                                                                        break block174;
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                    crets2 = Players.getInstance().getPlayers();
                                                                                                                                                                                                                                                    rangeHeatmap22 = crets2.length;
                                                                                                                                                                                                                                                    break block175;
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                if (this.target == -10L) break block162;
                                                                                                                                                                                                                                                targ = this.getTarget();
                                                                                                                                                                                                                                                if (targ == null) break block176;
                                                                                                                                                                                                                                                if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                                                                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                if ((currTile2 = targ.getCurrentTile()) == null) break block177;
                                                                                                                                                                                                                                                tilePosX = currTile2.tilex;
                                                                                                                                                                                                                                                tilePosY = currTile2.tiley;
                                                                                                                                                                                                                                                if (seed == 100) {
                                                                                                                                                                                                                                                    tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                                                                                                                                    tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                if (this.isSpellCaster() || this.isSummoner()) {
                                                                                                                                                                                                                                                    int n = Server.rand.nextBoolean() ? currTile2.tilex - (Server.rand.nextBoolean() ? 0 : 5) : (tilePosX = currTile2.tilex + (Server.rand.nextBoolean() ? 0 : 5));
                                                                                                                                                                                                                                                    tilePosY = Server.rand.nextBoolean() ? currTile2.tiley - (Server.rand.nextBoolean() ? 0 : 5) : currTile2.tiley + (Server.rand.nextBoolean() ? 0 : 5);
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                targGroup = targ.getGroupSize();
                                                                                                                                                                                                                                                myGroup = this.getGroupSize();
                                                                                                                                                                                                                                                if (this.isOnSurface() == currTile2.isOnSurface()) break block178;
                                                                                                                                                                                                                                                changeLayer = false;
                                                                                                                                                                                                                                                if (this.getCurrentTile().isTransition) {
                                                                                                                                                                                                                                                    changeLayer = true;
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                if (!this.isSpiritGuard()) break block179;
                                                                                                                                                                                                                                                if (this.currentVillage != this.citizenVillage) break block180;
                                                                                                                                                                                                                                                if (this.citizenVillage != null) {
                                                                                                                                                                                                                                                    if (currTile2.getTileX() >= this.citizenVillage.getStartX() - 5 && currTile2.getTileX() <= this.citizenVillage.getEndX() + 5 && currTile2.getTileY() >= this.citizenVillage.getStartY() - 5 && currTile2.getTileY() <= this.citizenVillage.getEndY() + 5) {
                                                                                                                                                                                                                                                        this.blinkTo(tilePosX, tilePosY, targ.getLayer(), targ.getFloorLevel());
                                                                                                                                                                                                                                                        return null;
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                    if (this.citizenVillage.isOnSurface() == this.isOnSurface()) {
                                                                                                                                                                                                                                                        try {
                                                                                                                                                                                                                                                            changeLayer = false;
                                                                                                                                                                                                                                                            tilePosX = this.citizenVillage.getToken().getTileX();
                                                                                                                                                                                                                                                            tilePosY = this.citizenVillage.getToken().getTileY();
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                        catch (NoSuchItemException nsi) {
                                                                                                                                                                                                                                                            logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                                                                    break block161;
                                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                break block161;
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                            if (this.citizenVillage != null) {
                                                                                                                                                                                                                                                if (currTile2.getTileX() >= this.citizenVillage.getStartX() - 5 && currTile2.getTileX() <= this.citizenVillage.getEndX() + 5 && currTile2.getTileY() >= this.citizenVillage.getStartY() - 5 && currTile2.getTileY() <= this.citizenVillage.getEndY() + 5) {
                                                                                                                                                                                                                                                    this.blinkTo(tilePosX, tilePosY, targ.getLayer(), 0);
                                                                                                                                                                                                                                                    return null;
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                if (this.citizenVillage.isOnSurface() == this.isOnSurface()) {
                                                                                                                                                                                                                                                    try {
                                                                                                                                                                                                                                                        tilePosX = this.citizenVillage.getToken().getTileX();
                                                                                                                                                                                                                                                        tilePosY = this.citizenVillage.getToken().getTileY();
                                                                                                                                                                                                                                                        changeLayer = false;
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                    catch (NoSuchItemException nsi) {
                                                                                                                                                                                                                                                        logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                } else if (!changeLayer) {
                                                                                                                                                                                                                                                    int[] tiles6 = new int[]{tilePosX, tilePosY};
                                                                                                                                                                                                                                                    tiles6 = this.isOnSurface() ? this.findRandomCaveEntrance(tiles6) : this.findRandomCaveExit(tiles6);
                                                                                                                                                                                                                                                    tilePosX = tiles6[0];
                                                                                                                                                                                                                                                    tilePosY = tiles6[1];
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                                                                                                                                break block161;
                                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                            break block161;
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        if (!this.isUnique()) break block181;
                                                                                                                                                                                                                                        Den den = Dens.getDen(this.template.getTemplateId());
                                                                                                                                                                                                                                        if (den != null) {
                                                                                                                                                                                                                                            tilePosX = den.getTilex();
                                                                                                                                                                                                                                            tilePosY = den.getTiley();
                                                                                                                                                                                                                                            if (!changeLayer) {
                                                                                                                                                                                                                                                int[] tiles7 = new int[]{tilePosX, tilePosY};
                                                                                                                                                                                                                                                if (!this.isOnSurface()) {
                                                                                                                                                                                                                                                    tiles7 = this.findRandomCaveExit(tiles7);
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                tilePosX = tiles7[0];
                                                                                                                                                                                                                                                tilePosY = tiles7[1];
                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                            this.setTarget(-10L, true);
                                                                                                                                                                                                                                            break block161;
                                                                                                                                                                                                                                        } else if (!this.isOnSurface() && !changeLayer) {
                                                                                                                                                                                                                                            int[] tiles8 = new int[]{tilePosX, tilePosY};
                                                                                                                                                                                                                                            tiles8 = this.findRandomCaveExit(tiles8);
                                                                                                                                                                                                                                            tilePosX = tiles8[0];
                                                                                                                                                                                                                                            tilePosY = tiles8[1];
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        break block161;
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                    if (!this.isKingdomGuard()) break block182;
                                                                                                                                                                                                                                    if (this.getCurrentKingdom() != this.getKingdomId()) break block183;
                                                                                                                                                                                                                                    if (this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                                                                                                                                                                                                                                        if (!changeLayer) {
                                                                                                                                                                                                                                            int[] tiles9 = new int[]{tilePosX, tilePosY};
                                                                                                                                                                                                                                            tiles9 = this.isOnSurface() ? this.findRandomCaveEntrance(tiles9) : this.findRandomCaveExit(tiles9);
                                                                                                                                                                                                                                            tilePosX = tiles9[0];
                                                                                                                                                                                                                                            tilePosY = tiles9[1];
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                        break block161;
                                                                                                                                                                                                                                    } else {
                                                                                                                                                                                                                                        this.setTarget(-10L, true);
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                    break block161;
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                changeLayer = false;
                                                                                                                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                                                                                                                break block161;
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                            if (this.getSize() > 3) {
                                                                                                                                                                                                                                changeLayer = false;
                                                                                                                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                                VolaTile t2 = this.getCurrentTile();
                                                                                                                                                                                                                                if ((this.isAggHuman() || this.isHunter() || this.isDominated()) && (!currTile2.isGuarded() || t2 != null && t2.isGuarded()) && this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                                                                                                                                                                                                                                    if (!changeLayer) {
                                                                                                                                                                                                                                        int[] tiles10 = new int[]{tilePosX, tilePosY};
                                                                                                                                                                                                                                        tiles10 = this.isOnSurface() ? this.findRandomCaveEntrance(tiles10) : this.findRandomCaveExit(tiles10);
                                                                                                                                                                                                                                        tilePosX = tiles10[0];
                                                                                                                                                                                                                                        tilePosY = tiles10[1];
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                        if (changeLayer && (!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty))) || MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                                                                                                                                                                                                                            this.setLayer(this.isOnSurface() ? -1 : 0, true);
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                        break block162;
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    if (!this.isSpiritGuard()) break block184;
                                                                                                                                                                                                                    if (this.currentVillage != this.citizenVillage) break block185;
                                                                                                                                                                                                                    if (this.citizenVillage == null) break block186;
                                                                                                                                                                                                                    tilePosX = currTile2.getTileX();
                                                                                                                                                                                                                    tilePosY = currTile2.getTileY();
                                                                                                                                                                                                                    if (targ.getCultist() == null || !targ.getCultist().hasFearEffect()) break block187;
                                                                                                                                                                                                                    tilePosX = this.citizenVillage.getStartX() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterX() + 10);
                                                                                                                                                                                                                    tilePosY = this.citizenVillage.getStartY() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterY() + 10);
                                                                                                                                                                                                                    break block162;
                                                                                                                                                                                                                }
                                                                                                                                                                                                                if (currTile2.getTileX() < this.citizenVillage.getStartX() - 5 || currTile2.getTileX() > this.citizenVillage.getEndX() + 5 || currTile2.getTileY() < this.citizenVillage.getStartY() - 5 || currTile2.getTileY() > this.citizenVillage.getEndY() + 5) {
                                                                                                                                                                                                                    try {
                                                                                                                                                                                                                        tilePosX = this.citizenVillage.getToken().getTileX();
                                                                                                                                                                                                                        tilePosY = this.citizenVillage.getToken().getTileY();
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    catch (NoSuchItemException nsi) {
                                                                                                                                                                                                                        logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                                    break block162;
                                                                                                                                                                                                                } else {
                                                                                                                                                                                                                    this.citizenVillage.cryForHelp(this, false);
                                                                                                                                                                                                                }
                                                                                                                                                                                                                break block162;
                                                                                                                                                                                                            }
                                                                                                                                                                                                            if (this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                                                                                                                                                                                                                logger.log(Level.WARNING, "Why does this happen to a " + this.getName() + " at " + this.getCurrentTile().tilex + ", " + this.getCurrentTile().tiley);
                                                                                                                                                                                                                tilePosX = currTile2.getTileX();
                                                                                                                                                                                                                tilePosY = currTile2.getTileY();
                                                                                                                                                                                                                break block162;
                                                                                                                                                                                                            } else {
                                                                                                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                                                                                            }
                                                                                                                                                                                                            break block162;
                                                                                                                                                                                                        }
                                                                                                                                                                                                        if (this.citizenVillage == null) break block188;
                                                                                                                                                                                                        tilePosX = currTile2.getTileX();
                                                                                                                                                                                                        tilePosY = currTile2.getTileY();
                                                                                                                                                                                                        if (currTile2.getTileX() < this.citizenVillage.getStartX() - 5 || currTile2.getTileX() > this.citizenVillage.getEndX() + 5 || currTile2.getTileY() < this.citizenVillage.getStartY() - 5 || currTile2.getTileY() > this.citizenVillage.getEndY() + 5) {
                                                                                                                                                                                                            try {
                                                                                                                                                                                                                tilePosX = this.citizenVillage.getToken().getTileX();
                                                                                                                                                                                                                tilePosY = this.citizenVillage.getToken().getTileY();
                                                                                                                                                                                                            }
                                                                                                                                                                                                            catch (NoSuchItemException nsi) {
                                                                                                                                                                                                                logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                                                                                                                                                                                            }
                                                                                                                                                                                                            this.setTarget(-10L, true);
                                                                                                                                                                                                            break block162;
                                                                                                                                                                                                        } else {
                                                                                                                                                                                                            this.citizenVillage.cryForHelp(this, true);
                                                                                                                                                                                                        }
                                                                                                                                                                                                        break block162;
                                                                                                                                                                                                    }
                                                                                                                                                                                                    if (this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) {
                                                                                                                                                                                                        if (Server.rand.nextInt(100) != 0) {
                                                                                                                                                                                                            tilePosX = currTile2.getTileX();
                                                                                                                                                                                                            tilePosY = currTile2.getTileY();
                                                                                                                                                                                                        } else {
                                                                                                                                                                                                            this.setTarget(-10L, true);
                                                                                                                                                                                                        }
                                                                                                                                                                                                    } else {
                                                                                                                                                                                                        this.setTarget(-10L, true);
                                                                                                                                                                                                    }
                                                                                                                                                                                                    logger.log(Level.WARNING, this.getName() + " no citizen village.");
                                                                                                                                                                                                    break block162;
                                                                                                                                                                                                }
                                                                                                                                                                                                if (!this.isUnique()) break block189;
                                                                                                                                                                                                Den den = Dens.getDen(this.template.getTemplateId());
                                                                                                                                                                                                if (den == null) break block190;
                                                                                                                                                                                                if (Math.abs(currTile2.getTileX() - den.getTilex()) <= this.template.getVision() && Math.abs(currTile2.getTileY() - den.getTiley()) <= this.template.getVision()) break block162;
                                                                                                                                                                                                if (Server.rand.nextInt(10) != 0) break block191;
                                                                                                                                                                                                if (!this.isFighting()) {
                                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                                    tilePosX = den.getTilex();
                                                                                                                                                                                                    tilePosY = den.getTiley();
                                                                                                                                                                                                }
                                                                                                                                                                                                break block162;
                                                                                                                                                                                            }
                                                                                                                                                                                            if (!this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) break block192;
                                                                                                                                                                                            tilePosX = currTile2.getTileX();
                                                                                                                                                                                            tilePosY = currTile2.getTileY();
                                                                                                                                                                                            if (this.getSize() >= 5 || targ.getBridgeId() == -10L || this.getBridgeId() >= 0L) break block193;
                                                                                                                                                                                            int[] tiles11 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                                                                                                                                                                            if (tiles11[0] > 0) {
                                                                                                                                                                                                tilePosX = tiles11[0];
                                                                                                                                                                                                tilePosY = tiles11[1];
                                                                                                                                                                                                if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                                                                    tilePosX = currTile2.tilex;
                                                                                                                                                                                                    tilePosY = currTile2.tiley;
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                            break block194;
                                                                                                                                                                                        }
                                                                                                                                                                                        if (this.getBridgeId() != targ.getBridgeId() && (tiles5 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId()))[0] > 0) {
                                                                                                                                                                                            tilePosX = tiles5[0];
                                                                                                                                                                                            tilePosY = tiles5[1];
                                                                                                                                                                                            if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                                                                tilePosX = currTile2.tilex;
                                                                                                                                                                                                tilePosY = currTile2.tiley;
                                                                                                                                                                                            }
                                                                                                                                                                                        }
                                                                                                                                                                                    }
                                                                                                                                                                                    if (seed == 100) {
                                                                                                                                                                                        tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                                                                        tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                                                                    }
                                                                                                                                                                                    break block162;
                                                                                                                                                                                }
                                                                                                                                                                                if (!this.isFighting()) {
                                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                                }
                                                                                                                                                                                break block162;
                                                                                                                                                                            }
                                                                                                                                                                            if (!this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) break block195;
                                                                                                                                                                            if (seed != 100) break block196;
                                                                                                                                                                            tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                                                            tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                                                            break block162;
                                                                                                                                                                        }
                                                                                                                                                                        tilePosX = currTile2.getTileX();
                                                                                                                                                                        tilePosY = currTile2.getTileY();
                                                                                                                                                                        if (this.getSize() >= 5 || targ.getBridgeId() == -10L || this.getBridgeId() >= 0L) break block197;
                                                                                                                                                                        int[] tiles12 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                                                                                                                                                        if (tiles12[0] > 0) {
                                                                                                                                                                            tilePosX = tiles12[0];
                                                                                                                                                                            tilePosY = tiles12[1];
                                                                                                                                                                            if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                                                tilePosX = currTile2.tilex;
                                                                                                                                                                                tilePosY = currTile2.tiley;
                                                                                                                                                                            }
                                                                                                                                                                        }
                                                                                                                                                                        break block162;
                                                                                                                                                                    }
                                                                                                                                                                    if (this.getBridgeId() != targ.getBridgeId() && (tiles4 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId()))[0] > 0) {
                                                                                                                                                                        tilePosX = tiles4[0];
                                                                                                                                                                        tilePosY = tiles4[1];
                                                                                                                                                                        if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                                            tilePosX = currTile2.tilex;
                                                                                                                                                                            tilePosY = currTile2.tiley;
                                                                                                                                                                        }
                                                                                                                                                                    }
                                                                                                                                                                    break block162;
                                                                                                                                                                }
                                                                                                                                                                if (!this.isFighting()) {
                                                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                                                }
                                                                                                                                                                break block162;
                                                                                                                                                            }
                                                                                                                                                            if (!this.isKingdomGuard()) break block198;
                                                                                                                                                            if (this.getCurrentKingdom() != this.getKingdomId()) break block199;
                                                                                                                                                            if (!this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) break block200;
                                                                                                                                                            GuardTower gt2 = Kingdoms.getTower(this);
                                                                                                                                                            if (gt2 == null) break block201;
                                                                                                                                                            tpx2 = gt2.getTower().getTileX();
                                                                                                                                                            tpy2 = gt2.getTower().getTileY();
                                                                                                                                                            if (targGroup >= myGroup * this.getMaxGroupAttackSize() || !targ.isWithinTileDistanceTo(tpx2, tpy2, (int)gt2.getTower().getPosZ(), 50)) break block202;
                                                                                                                                                            if (targ.getCultist() == null || !targ.getCultist().hasFearEffect()) break block203;
                                                                                                                                                            tilePosX = Server.rand.nextBoolean() ? Math.max(currTile2.getTileX() + 10, this.getTileX()) : Math.min(currTile2.getTileX() - 10, this.getTileX());
                                                                                                                                                            tilePosX = Server.rand.nextBoolean() ? Math.max(currTile2.getTileY() + 10, this.getTileY()) : Math.min(currTile2.getTileY() - 10, this.getTileY());
                                                                                                                                                            break block162;
                                                                                                                                                        }
                                                                                                                                                        if (seed != 100) break block204;
                                                                                                                                                        tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                                        tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                                        break block162;
                                                                                                                                                    }
                                                                                                                                                    tilePosX = currTile2.getTileX();
                                                                                                                                                    tilePosY = currTile2.getTileY();
                                                                                                                                                    if (targ.getBridgeId() == -10L || this.getBridgeId() >= 0L) break block205;
                                                                                                                                                    int[] tiles13 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                                                                                                                                    if (tiles13[0] > 0) {
                                                                                                                                                        tilePosX = tiles13[0];
                                                                                                                                                        tilePosY = tiles13[1];
                                                                                                                                                        if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                            tilePosX = currTile2.tilex;
                                                                                                                                                            tilePosY = currTile2.tiley;
                                                                                                                                                        }
                                                                                                                                                    }
                                                                                                                                                    break block162;
                                                                                                                                                }
                                                                                                                                                if (this.getBridgeId() != targ.getBridgeId() && (tiles3 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId()))[0] > 0) {
                                                                                                                                                    tilePosX = tiles3[0];
                                                                                                                                                    tilePosY = tiles3[1];
                                                                                                                                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                                                        tilePosX = currTile2.tilex;
                                                                                                                                                        tilePosY = currTile2.tiley;
                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                                break block162;
                                                                                                                                            }
                                                                                                                                            tilePosX = tpx2;
                                                                                                                                            tilePosY = tpy2;
                                                                                                                                            this.setTarget(-10L, true);
                                                                                                                                            break block162;
                                                                                                                                        }
                                                                                                                                        if (seed == 100) {
                                                                                                                                            tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                            tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                            break block162;
                                                                                                                                        } else {
                                                                                                                                            tilePosX = currTile2.getTileX();
                                                                                                                                            tilePosY = currTile2.getTileY();
                                                                                                                                        }
                                                                                                                                        break block162;
                                                                                                                                    }
                                                                                                                                    this.setTarget(-10L, true);
                                                                                                                                    break block162;
                                                                                                                                }
                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                break block162;
                                                                                                                            }
                                                                                                                            if (targ.getCultist() == null || !targ.getCultist().hasFearEffect()) break block206;
                                                                                                                            tilePosX = Server.rand.nextBoolean() ? Math.max(currTile2.getTileX() + 10, this.getTileX()) : Math.min(currTile2.getTileX() - 10, this.getTileX());
                                                                                                                            tilePosX = Server.rand.nextBoolean() ? Math.max(currTile2.getTileY() + 10, this.getTileY()) : Math.min(currTile2.getTileY() - 10, this.getTileY());
                                                                                                                            break block162;
                                                                                                                        }
                                                                                                                        boolean abort2 = false;
                                                                                                                        boolean towerFound = false;
                                                                                                                        if (this.isWarGuard()) {
                                                                                                                            int rand2;
                                                                                                                            Item wtarget2;
                                                                                                                            GuardTower gt3 = Kingdoms.getClosestTower(this.getTileX(), this.getTileY(), true);
                                                                                                                            if (gt3 != null && gt3.getKingdom() == this.getKingdomId()) {
                                                                                                                                towerFound = true;
                                                                                                                            }
                                                                                                                            if (!((wtarget2 = Kingdoms.getClosestWarTarget(tx, ty, this)) == null || towerFound && !(Creature.getTileRange(this, wtarget2.getTileX(), wtarget2.getTileY()) < Creature.getTileRange(this, gt3.getTower().getTileX(), gt3.getTower().getTileY())) || this.isWithinTileDistanceTo(wtarget2.getTileX(), wtarget2.getTileY(), wtarget2.getFloorLevel(), 15))) {
                                                                                                                                rand2 = Server.rand.nextInt(9);
                                                                                                                                tilePosX = Zones.safeTileX(wtarget2.getTileX() + 4 - rand2);
                                                                                                                                rand2 = Server.rand.nextInt(9);
                                                                                                                                tilePosY = Zones.safeTileY(wtarget2.getTileY() + 4 - rand2);
                                                                                                                                abort2 = true;
                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                this.sendToLoggers("Heading to my camp at " + tilePosX + "," + tilePosY);
                                                                                                                            }
                                                                                                                            if (!abort2 && towerFound && !this.isWithinTileDistanceTo(gt3.getTower().getTileX(), gt3.getTower().getTileY(), gt3.getTower().getFloorLevel(), 15)) {
                                                                                                                                rand2 = Server.rand.nextInt(9);
                                                                                                                                tilePosX = Zones.safeTileX(gt3.getTower().getTileX() + 4 - rand2);
                                                                                                                                rand2 = Server.rand.nextInt(9);
                                                                                                                                tilePosY = Zones.safeTileY(gt3.getTower().getTileY() + 4 - rand2);
                                                                                                                                abort2 = true;
                                                                                                                                this.setTarget(-10L, true);
                                                                                                                                this.sendToLoggers("Heading to my tower at " + tilePosX + "," + tilePosY);
                                                                                                                            }
                                                                                                                        }
                                                                                                                        if (abort2) break block162;
                                                                                                                        VolaTile t3 = this.getCurrentTile();
                                                                                                                        if (targGroup > myGroup * this.getMaxGroupAttackSize() || !this.isAggHuman() && !this.isHunter() || currTile2.isGuarded() && (t3 == null || !t3.isGuarded())) break block207;
                                                                                                                        if (!this.isWithinTileDistanceTo(currTile2.getTileX(), currTile2.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance())) break block208;
                                                                                                                        if (targ.getKingdomId() == 0 || this.isFriendlyKingdom(targ.getKingdomId()) || !this.isDefendKingdom() && (!this.isAggWhitie() || targ.getKingdomTemplateId() == 3)) break block209;
                                                                                                                        if (!this.isFighting()) {
                                                                                                                            if (seed == 100) {
                                                                                                                                tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                break block162;
                                                                                                                            } else {
                                                                                                                                tilePosX = currTile2.getTileX();
                                                                                                                                tilePosY = currTile2.getTileY();
                                                                                                                                this.setTarget(targ.getWurmId(), false);
                                                                                                                            }
                                                                                                                        }
                                                                                                                        break block162;
                                                                                                                    }
                                                                                                                    if (!this.isSubmerged()) break block210;
                                                                                                                    try {
                                                                                                                        float z = Zones.calculateHeight(targ.getPosX(), targ.getPosY(), targ.isOnSurface());
                                                                                                                        if (z < -5.0f) {
                                                                                                                            if (seed == 100) {
                                                                                                                                tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                                tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                                break block162;
                                                                                                                            } else {
                                                                                                                                tilePosX = currTile2.getTileX();
                                                                                                                                tilePosY = currTile2.getTileY();
                                                                                                                            }
                                                                                                                            break block162;
                                                                                                                        }
                                                                                                                        int[] tiles14 = new int[]{tilePosX, tilePosY};
                                                                                                                        if (this.isOnSurface()) {
                                                                                                                            tiles14 = this.findRandomDeepSpot(tiles14);
                                                                                                                        }
                                                                                                                        tilePosX = tiles14[0];
                                                                                                                        tilePosY = tiles14[1];
                                                                                                                        this.setTarget(-10L, true);
                                                                                                                    }
                                                                                                                    catch (NoSuchZoneException nsz) {
                                                                                                                        this.setTarget(-10L, true);
                                                                                                                    }
                                                                                                                    break block162;
                                                                                                                }
                                                                                                                if (seed != 100) break block211;
                                                                                                                tilePosX = currTile2.tilex - 1 + Server.rand.nextInt(3);
                                                                                                                tilePosY = currTile2.tiley - 1 + Server.rand.nextInt(3);
                                                                                                                break block162;
                                                                                                            }
                                                                                                            tilePosX = currTile2.getTileX();
                                                                                                            tilePosY = currTile2.getTileY();
                                                                                                            if (this.getSize() >= 5 || targ.getBridgeId() == -10L || this.getBridgeId() >= 0L) break block212;
                                                                                                            tiles2 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                                                                                            if (tiles2[0] > 0) {
                                                                                                                tilePosX = tiles2[0];
                                                                                                                tilePosY = tiles2[1];
                                                                                                                if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                    tilePosX = currTile2.tilex;
                                                                                                                    tilePosY = currTile2.tiley;
                                                                                                                }
                                                                                                            }
                                                                                                            break block162;
                                                                                                        }
                                                                                                        if (this.getBridgeId() != targ.getBridgeId() && (tiles2 = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId()))[0] > 0) {
                                                                                                            tilePosX = tiles2[0];
                                                                                                            tilePosY = tiles2[1];
                                                                                                            if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                                                                                tilePosX = currTile2.tilex;
                                                                                                                tilePosY = currTile2.tiley;
                                                                                                            }
                                                                                                        }
                                                                                                        break block162;
                                                                                                    }
                                                                                                    if (!this.isFighting()) {
                                                                                                        this.setTarget(-10L, true);
                                                                                                    }
                                                                                                    break block162;
                                                                                                }
                                                                                                if (!this.isFighting()) {
                                                                                                    this.setTarget(-10L, true);
                                                                                                }
                                                                                                break block162;
                                                                                            }
                                                                                            this.setTarget(-10L, true);
                                                                                            break block162;
                                                                                        }
                                                                                        this.setTarget(-10L, true);
                                                                                        break block162;
                                                                                    }
                                                                                    for (int i = 0; i < heatmapSize; ++i) {
                                                                                        for (int j = 0; j < heatmapSize; ++j) {
                                                                                            rangeHeatmap22[i][j] = -100.0f;
                                                                                        }
                                                                                    }
                                                                                    for (Long lCret : visibleCreatures2 = this.getVisionArea().getSurface().getCreatures()) {
                                                                                        try {
                                                                                            Creature cret = Server.getInstance().getCreature(lCret);
                                                                                            float tileModifier = 0.0f;
                                                                                            int diffX = (int)(cret.getPosX() - this.getPosX()) >> 2;
                                                                                            int diffY = (int)(cret.getPosY() - this.getPosY()) >> 2;
                                                                                            for (int i = 0; i < heatmapSize; ++i) {
                                                                                                int j = 0;
                                                                                                while (j < heatmapSize) {
                                                                                                    int deltaX = Math.abs(this.template.getVision() + diffX - i);
                                                                                                    int deltaY = Math.abs(this.template.getVision() + diffY - j);
                                                                                                    if ((cret.getPower() == 0 || Servers.localServer.testServer) && (cret.isPlayer() || cret.isAggHuman() || cret.isCarnivore() || cret.isMonster() || cret.isHunter())) {
                                                                                                        tileModifier = cret.getBaseCombatRating();
                                                                                                        if (cret.isBred() || cret.isBranded() || cret.isCaredFor()) {
                                                                                                            tileModifier /= 3.0f;
                                                                                                        }
                                                                                                        if (cret.isDominated()) {
                                                                                                            tileModifier /= 3.0f;
                                                                                                        }
                                                                                                        tileModifier -= (float)Math.max(deltaX, deltaY);
                                                                                                    } else {
                                                                                                        tileModifier = 1.0f;
                                                                                                    }
                                                                                                    float[] fArray = rangeHeatmap22[i];
                                                                                                    int n = j++;
                                                                                                    fArray[n] = fArray[n] + tileModifier;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        catch (NoSuchPlayerException | NoSuchCreatureException cret) {
                                                                                            // empty catch block
                                                                                        }
                                                                                    }
                                                                                    float currentVal = rangeHeatmap22[this.template.getVision()][this.template.getVision()];
                                                                                    int currentValCount = 1;
                                                                                    short currentTileHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(Zones.safeTileX(this.getTileX()), Zones.safeTileY(this.getTileY())));
                                                                                    int y2 = 0;
                                                                                    while (true) {
                                                                                        if (y2 >= heatmapSize) break;
                                                                                        for (int x = 0; x < heatmapSize; ++x) {
                                                                                            short tileHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(Zones.safeTileX(this.getTileX() + x - this.template.getVision()), Zones.safeTileY(this.getTileY() + y2 - this.template.getVision())));
                                                                                            if (!this.isSubmerged() && tileHeight < 0) {
                                                                                                if (!this.isSwimming()) {
                                                                                                    float[] fArray = rangeHeatmap22[x];
                                                                                                    int n = y2;
                                                                                                    fArray[n] = fArray[n] + (float)(100 + -tileHeight);
                                                                                                } else {
                                                                                                    float[] fArray = rangeHeatmap22[x];
                                                                                                    int n = y2;
                                                                                                    fArray[n] = fArray[n] + (float)(-tileHeight);
                                                                                                }
                                                                                            } else if (tileHeight > 0) {
                                                                                                float[] fArray = rangeHeatmap22[x];
                                                                                                int n = y2;
                                                                                                fArray[n] = fArray[n] + (float)(Math.abs(currentTileHeight - tileHeight) / 15);
                                                                                            }
                                                                                            float testVal = rangeHeatmap22[x][y2];
                                                                                            if (testVal == currentVal) {
                                                                                                ++currentValCount;
                                                                                                continue;
                                                                                            }
                                                                                            if (!(testVal < currentVal)) continue;
                                                                                            currentValCount = 1;
                                                                                            currentVal = testVal;
                                                                                        }
                                                                                        ++y2;
                                                                                    }
                                                                                    for (y2 = 0; y2 < heatmapSize && !flee; ++y2) {
                                                                                        for (int x = 0; x < heatmapSize && !flee; ++x) {
                                                                                            if (currentVal != rangeHeatmap22[x][y2] || Server.rand.nextInt((int)Math.max(1.0f, (float)currentValCount * 0.75f)) != 0) continue;
                                                                                            tilePosX = tx + x - this.template.getVision();
                                                                                            tilePosY = ty + y2 - this.template.getVision();
                                                                                            flee = true;
                                                                                        }
                                                                                    }
                                                                                    if (!flee) {
                                                                                        return null;
                                                                                    }
                                                                                    break block165;
                                                                                }
                                                                                for (int currentVal = 0; currentVal < visibleCreatures2; ++currentVal) {
                                                                                    Long lCret = rangeHeatmap22[currentVal];
                                                                                    try {
                                                                                        Creature cret = Server.getInstance().getCreature(lCret);
                                                                                        if (cret.getPower() != 0 || !cret.isPlayer() && !cret.isAggHuman() && !cret.isCarnivore() && !cret.isMonster()) continue;
                                                                                        tilePosX = cret.getPosX() > this.getPosX() ? (tilePosX -= Server.rand.nextInt(6)) : (tilePosX += Server.rand.nextInt(6));
                                                                                        tilePosY = cret.getPosY() > this.getPosY() ? (tilePosY -= Server.rand.nextInt(6)) : (tilePosY += Server.rand.nextInt(6));
                                                                                        flee = true;
                                                                                        break block165;
                                                                                    }
                                                                                    catch (Exception cret) {
                                                                                        // empty catch block
                                                                                    }
                                                                                }
                                                                                break block165;
                                                                            }
                                                                            for (int visibleCreatures2 = 0; visibleCreatures2 < rangeHeatmap22; ++visibleCreatures2) {
                                                                                Player p = crets2[visibleCreatures2];
                                                                                if (p.getPower() != 0 && !Servers.localServer.testServer || p.getVisionArea() == null || p.getVisionArea().getSurface() == null || !p.getVisionArea().getSurface().containsCreature(this)) continue;
                                                                                tilePosX = p.getPosX() > this.getPosX() ? (tilePosX -= Server.rand.nextInt(6)) : (tilePosX += Server.rand.nextInt(6));
                                                                                tilePosY = p.getPosY() > this.getPosY() ? (tilePosY -= Server.rand.nextInt(6)) : (tilePosY += Server.rand.nextInt(6));
                                                                                flee = true;
                                                                                break;
                                                                            }
                                                                        }
                                                                        if (this.isSpy() && (newarr = this.getSpySpot(empty = new int[]{-1, -1}))[0] > 0 && newarr[1] > 0) {
                                                                            flee = true;
                                                                            tilePosX = newarr[0];
                                                                            tilePosY = newarr[1];
                                                                        }
                                                                    }
                                                                    if (!this.isMoveLocal() || flee || hasTarget) break block213;
                                                                    currTile = this.getCurrentTile();
                                                                    if (!this.isUnique() || Server.rand.nextInt(10) != 0) break block214;
                                                                    Den den = Dens.getDen(this.template.getTemplateId());
                                                                    if (den != null && (den.getTilex() != tx || den.getTiley() != ty)) {
                                                                        tilePosX = den.getTilex();
                                                                        tilePosY = den.getTiley();
                                                                    }
                                                                    break block167;
                                                                }
                                                                if (currTile == null) break block167;
                                                                rand = Server.rand.nextInt(9);
                                                                tpx = currTile.getTileX() + 4 - rand;
                                                                rand = Server.rand.nextInt(9);
                                                                tpy = currTile.getTileY() + 4 - rand;
                                                                totx += currTile.getTileX() - tpx;
                                                                toty += currTile.getTileY() - tpy;
                                                                int[] foodSpot = this.forageForFood(currTile);
                                                                abort = false;
                                                                if (!Server.rand.nextBoolean()) break block166;
                                                                if (foodSpot[0] == -1) break block215;
                                                                tpx = foodSpot[0];
                                                                tpy = foodSpot[1];
                                                                break block166;
                                                            }
                                                            if (!this.template.isTowerBasher() || !Servers.localServer.PVPSERVER) break block216;
                                                            GuardTower closestTower = Kingdoms.getClosestEnemyTower(this.getTileX(), this.getTileY(), true, this);
                                                            if (closestTower != null) {
                                                                tilePosX = closestTower.getTower().getTileX();
                                                                tilePosY = closestTower.getTower().getTileY();
                                                                abort = true;
                                                            }
                                                            break block166;
                                                        }
                                                        if (!this.isWarGuard()) break block166;
                                                        tilePosX = Zones.safeTileX(tpx);
                                                        tilePosY = Zones.safeTileY(tpy);
                                                        if (this.isOnSurface()) break block217;
                                                        int[] tiles15 = new int[]{tilePosX, tilePosY};
                                                        if (this.getCurrentTile().isTransition) {
                                                            this.setLayer(0, true);
                                                            break block166;
                                                        } else if ((tiles15 = this.findRandomCaveExit(tiles15))[0] != tilePosX && tiles15[1] != tilePosY) {
                                                            tilePosX = tiles15[0];
                                                            tilePosY = tiles15[1];
                                                            abort = true;
                                                            break block166;
                                                        } else {
                                                            this.setLayer(0, true);
                                                        }
                                                        break block166;
                                                    }
                                                    GuardTower gt4 = this.getGuardTower();
                                                    if (gt4 == null) {
                                                        gt4 = Kingdoms.getClosestTower(this.getTileX(), this.getTileY(), true);
                                                    }
                                                    boolean towerFound = false;
                                                    if (gt4 != null && gt4.getKingdom() == this.getKingdomId()) {
                                                        towerFound = true;
                                                    }
                                                    if (!((wtarget = Kingdoms.getClosestWarTarget(tx, ty, this)) == null || towerFound && !(Creature.getTileRange(this, wtarget.getTileX(), wtarget.getTileY()) < Creature.getTileRange(this, gt4.getTower().getTileX(), gt4.getTower().getTileY())) || this.isWithinTileDistanceTo(wtarget.getTileX(), wtarget.getTileY(), wtarget.getFloorLevel(), 15))) {
                                                        rand = Server.rand.nextInt(9);
                                                        tilePosX = Zones.safeTileX(wtarget.getTileX() + 4 - rand);
                                                        rand = Server.rand.nextInt(9);
                                                        tilePosY = Zones.safeTileY(wtarget.getTileY() + 4 - rand);
                                                        this.setTarget(-10L, true);
                                                        this.sendToLoggers("No target. Heading to my camp at " + tilePosX + "," + tilePosY);
                                                        abort = true;
                                                    }
                                                    if (!abort && towerFound && !this.isWithinTileDistanceTo(gt4.getTower().getTileX(), gt4.getTower().getTileY(), gt4.getTower().getFloorLevel(), 15)) {
                                                        rand = Server.rand.nextInt(9);
                                                        tilePosX = Zones.safeTileX(gt4.getTower().getTileX() + 4 - rand);
                                                        rand = Server.rand.nextInt(9);
                                                        tilePosY = Zones.safeTileY(gt4.getTower().getTileY() + 4 - rand);
                                                        this.setTarget(-10L, true);
                                                        this.sendToLoggers("No target. Heading to my tower at " + tilePosX + "," + tilePosY);
                                                        abort = true;
                                                    }
                                                }
                                                tpx = Zones.safeTileX(tpx);
                                                tpy = Zones.safeTileY(tpy);
                                                if (abort) break block167;
                                                t = Zones.getOrCreateTile(tpx, tpy, this.isOnSurface());
                                                VolaTile myt = this.getCurrentTile();
                                                if (t.isGuarded() && (myt == null || !myt.isGuarded() || t.hasFire())) break block167;
                                                swimming = false;
                                                int n = ctile = this.isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
                                                if (Tiles.decodeHeight(ctile) <= 0) {
                                                    swimming = true;
                                                }
                                                if (Tiles.isSolidCave(Tiles.decodeType(tile2 = Zones.getTileIntForTile(tpx, tpy, this.getLayer()))) || Tiles.decodeHeight(tile2) <= 0 && !swimming) break block167;
                                                if (!this.isOnSurface()) break block218;
                                                stepOnBridge = false;
                                                if (Server.rand.nextInt(5) != 0) break block219;
                                                iterator = this.currentTile.getThisAndSurroundingTiles(1).iterator();
                                                break block220;
                                            }
                                            if (t == null || t.getCreatures().length < 3) {
                                                tilePosX = tpx;
                                                tilePosY = tpy;
                                            }
                                            break block167;
                                        }
                                        if (!this.isSpiritGuard() || hasTarget) break block221;
                                        if (this.citizenVillage == null) break block222;
                                        tiles = new int[]{tilePosX, tilePosY};
                                        if (this.isOnSurface()) break block223;
                                        if (this.getCurrentTile().isTransition) {
                                            this.setLayer(0, true);
                                            break block167;
                                        } else {
                                            tiles = this.findRandomCaveExit(tiles);
                                            tilePosX = tiles[0];
                                            tilePosY = tiles[1];
                                            if (tilePosX != tx && tilePosY != ty) {
                                                // empty if block
                                            }
                                        }
                                        break block167;
                                    }
                                    int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
                                    VolaTile t4 = Zones.getTileOrNull(x, y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty), this.isOnSurface());
                                    if (t4 != null) {
                                        if (t4.getStructure() == null) {
                                            tilePosX = x;
                                            tilePosY = y;
                                        }
                                        break block167;
                                    } else {
                                        tilePosX = x;
                                        tilePosY = y;
                                    }
                                    break block167;
                                }
                                currTile = this.getCurrentTile();
                                if (currTile != null) {
                                    int rand = Server.rand.nextInt(5);
                                    tpx = currTile.getTileX() + 2 - rand;
                                    rand = Server.rand.nextInt(5);
                                    tpy = currTile.getTileY() + 2 - rand;
                                    VolaTile t5 = Zones.getTileOrNull(tilePosX, tilePosY, this.isOnSurface());
                                    tpx = Zones.safeTileX(tpx);
                                    tpy = Zones.safeTileY(tpy);
                                    if (t5 == null) {
                                        tilePosX = tpx;
                                        tilePosY = tpy;
                                    }
                                    break block167;
                                } else if (!this.isDead()) {
                                    currTile = Zones.getOrCreateTile(tilePosX, tilePosY, this.isOnSurface());
                                    logger.log(Level.WARNING, this.getName() + " stuck on no tile at " + this.getTileX() + "," + this.getTileY() + "," + this.isOnSurface());
                                }
                                break block167;
                            }
                            if (!this.isKingdomGuard() || hasTarget) break block167;
                            tiles = new int[]{tilePosX, tilePosY};
                            if (!this.isOnSurface()) {
                                tiles = this.findRandomCaveExit(tiles);
                                tilePosX = tiles[0];
                                tilePosY = tiles[1];
                                if (tilePosX != tx && tilePosY != ty) {
                                    hasTarget = true;
                                }
                            }
                            if (!hasTarget && Server.rand.nextInt(40) == 0 && (gt = Kingdoms.getTower(this)) != null) {
                                tpx = gt.getTower().getTileX();
                                tpy = gt.getTower().getTileY();
                                tilePosX = tpx;
                                tilePosY = tpy;
                                hasTarget = true;
                            }
                            if (!hasTarget) {
                                VolaTile currTile = this.getCurrentTile();
                                int rand = Server.rand.nextInt(5);
                                int tpx3 = Zones.safeTileX(currTile.getTileX() + 2 - rand);
                                rand = Server.rand.nextInt(5);
                                int tpy3 = Zones.safeTileY(currTile.getTileY() + 2 - rand);
                                VolaTile t6 = Zones.getOrCreateTile(tpx3, tpy3, this.isOnSurface());
                                if ((t6.getKingdom() == this.getKingdomId() || currTile.getKingdom() != this.getKingdomId()) && t6.getStructure() == null) {
                                    tilePosX = tpx3;
                                    tilePosY = tpy3;
                                }
                            }
                            break block167;
                        }
                        while (iterator.hasNext()) {
                            VolaTile stile = iterator.next();
                            if (stile.getStructure() == null || !stile.getStructure().isTypeBridge()) continue;
                            if (stile.getStructure().isHorizontal()) {
                                if (stile.getStructure().getMaxX() != stile.getTileX() && stile.getStructure().getMinX() != stile.getTileX() || this.getTileY() != stile.getTileY()) continue;
                                tilePosX = stile.getTileX();
                                tilePosY = stile.getTileY();
                                stepOnBridge = true;
                                break;
                            }
                            if (stile.getStructure().getMaxY() != stile.getTileY() && stile.getStructure().getMinY() != stile.getTileY() || this.getTileX() != stile.getTileX()) continue;
                            tilePosX = stile.getTileX();
                            tilePosY = stile.getTileY();
                            stepOnBridge = true;
                            break;
                        }
                    }
                    if (!(stepOnBridge || this.isTargetTileTooHigh(tpx, tpy, tile2, swimming) || t != null && t.getCreatures().length >= 3)) {
                        tilePosX = tpx;
                        tilePosY = tpy;
                    }
                }
                if (!(this.isCaveDweller() || this.isOnSurface() || !this.getCurrentTile().isTransition || tilePosX != tx || tilePosY != ty || Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty))) && !MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                    this.setLayer(0, true);
                }
            }
            if (tilePosX == tx && tilePosY == ty) {
                return null;
            }
            tilePosX = Zones.safeTileX(tilePosX);
            tilePosY = Zones.safeTileY(tilePosY);
            if (!this.isOnSurface()) {
                tile = Server.caveMesh.getTile(tilePosX, tilePosY);
                if (!Tiles.isSolidCave(Tiles.decodeType(tile))) {
                    if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters()) return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                    if (this.isSwimming()) return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                    if (this.isSubmerged()) {
                        return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                    }
                }
            } else {
                tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
                if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters()) return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                if (this.isSwimming()) return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                if (this.isSubmerged()) {
                    return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                }
            }
            this.setTarget(-10L, true);
            if (!this.isDominated()) return null;
            if (!this.hasOrders()) return null;
            this.removeOrder(this.getFirstOrder());
            return null;
        }
        catch (ArrayIndexOutOfBoundsException iao) {
            logger.log(Level.WARNING, this.getName() + " " + tilePosX + ", " + tilePosY + iao.getMessage(), iao);
            return null;
        }
    }

    public final boolean isBridgeBlockingAttack(Creature attacker, boolean justChecking) {
        if (this.isInvulnerable() || attacker.isInvulnerable()) {
            return true;
        }
        if (this.getPositionZ() + this.getAltOffZ() < 0.0f && attacker.getBridgeId() > 0L) {
            return true;
        }
        if (attacker.getPositionZ() + this.getAltOffZ() < 0.0f && this.getBridgeId() > 0L) {
            return true;
        }
        return !justChecking && this.getFloorLevel() != attacker.getFloorLevel() && this.getBridgeId() != attacker.getBridgeId() && this.getSize() < 5 && attacker.getSize() < 5;
    }

    public final PathTile getPersonalTargetTile() {
        float lPosX = this.status.getPositionX();
        float lPosY = this.status.getPositionY();
        int tilePosX = (int)lPosX >> 2;
        int tilePosY = (int)lPosY >> 2;
        int tx = tilePosX;
        int ty = tilePosY;
        VolaTile currTile = this.getCurrentTile();
        if (currTile != null) {
            GuardTower closestTower;
            int[] foodSpot = this.forageForFood(currTile);
            if (foodSpot[0] != -1) {
                tilePosX = foodSpot[0];
                tilePosY = foodSpot[1];
            } else if (this.template.isTowerBasher() && Servers.localServer.PVPSERVER && (closestTower = Kingdoms.getClosestEnemyTower(this.getTileX(), this.getTileY(), true, this)) != null) {
                tilePosX = closestTower.getTower().getTileX();
                tilePosY = closestTower.getTower().getTileY();
            }
        }
        if (tilePosX == tx && tilePosY == ty) {
            return null;
        }
        tilePosX = Zones.safeTileX(tilePosX);
        tilePosY = Zones.safeTileY(tilePosY);
        if (!this.isOnSurface()) {
            int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
            if (!Tiles.isSolidCave(Tiles.decodeType(tile)) && (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged())) {
                return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), -1);
            }
        } else {
            int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
            if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged()) {
                return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), 0);
            }
        }
        return null;
    }

    public final int getHalfHeightDecimeters() {
        return this.getCentimetersHigh() / 20;
    }

    public int[] findRandomCaveExit(int[] tiles) {
        int startx = Math.max(0, this.currentTile.tilex - 20);
        int endx = Math.min(Zones.worldTileSizeX - 1, this.currentTile.tilex + 20);
        int starty = Math.max(0, this.currentTile.tiley - 20);
        int endy = Math.min(Zones.worldTileSizeY - 1, this.currentTile.tiley + 20);
        if (this.citizenVillage != null && Server.rand.nextInt(2) == 0) {
            int y;
            startx = Math.max(0, this.citizenVillage.getStartX() - 5);
            endx = Math.min(Zones.worldTileSizeX - 1, this.citizenVillage.getEndX() + 5);
            starty = Math.max(0, this.citizenVillage.getStartY() - 5);
            endy = Math.min(Zones.worldTileSizeY - 1, this.citizenVillage.getEndY() + 5);
            int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
            if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty))))) {
                tiles[0] = x;
                tiles[1] = y;
                this.setPathfindcounter(0);
            }
        }
        int rand = Server.rand.nextInt(endx - startx);
        startx += rand;
        rand = Server.rand.nextInt(endy - starty);
        starty += rand;
        for (int x = startx; x < endx; ++x) {
            for (int y = starty; y < endy; ++y) {
                if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) != Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                tiles[0] = x;
                tiles[1] = y;
                this.setPathfindcounter(0);
                return tiles;
            }
        }
        return tiles;
    }

    public int[] findRandomDeepSpot(int[] tiles) {
        int startx = Zones.safeTileX(this.currentTile.tilex - 50);
        int endx = Zones.safeTileX(this.currentTile.tilex + 50);
        int starty = Zones.safeTileY(this.currentTile.tiley - 50);
        int endy = Zones.safeTileY(this.currentTile.tiley + 50);
        int rand = Server.rand.nextInt(endx - startx);
        startx += rand;
        rand = Server.rand.nextInt(endy - starty);
        starty += rand;
        for (int x = startx; x < Math.min(endx, startx + 10); ++x) {
            for (int y = starty; y < Math.min(endy, starty + 10); ++y) {
                if (!((float)Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) < -50.0f)) continue;
                tiles[0] = x;
                tiles[1] = y;
                return tiles;
            }
        }
        return tiles;
    }

    public final boolean isSpyTarget(Creature c) {
        if (c.isDead() || c.getPower() > 0) {
            return false;
        }
        if (this.getTemplate().getTemplateId() == 84 && c.getKingdomId() != 3) {
            return true;
        }
        if (this.getTemplate().getTemplateId() == 12 && c.getKingdomId() != 1) {
            return true;
        }
        return this.getTemplate().getTemplateId() == 10 && c.getKingdomId() != 2;
    }

    public final boolean isSpyFriend(Creature c) {
        if (c.isAggHuman() || c.getCitizenVillage() == null) {
            return false;
        }
        if (this.getTemplate().getTemplateId() == 84 && c.getKingdomId() == 3) {
            return true;
        }
        if (this.getTemplate().getTemplateId() == 12 && c.getKingdomId() == 1) {
            return true;
        }
        return this.getTemplate().getTemplateId() == 10 && c.getKingdomId() == 2;
    }

    public final boolean isWithinSpyDist(Creature c) {
        return c != null && c.isWithinTileDistanceTo(this.getTileX(), this.getTileY(), 100, 40);
    }

    public int[] getSpySpot(int[] suggested) {
        if (this.isSpy()) {
            Creature linkedToc = this.getCreatureLinkedTo();
            if (linkedToc == null || !linkedToc.isDead() || !this.isWithinSpyDist(linkedToc)) {
                this.linkedTo = -10L;
                for (Player player : Players.getInstance().getPlayers()) {
                    if (!this.isSpyTarget(player) || player.isDead() || !this.isWithinSpyDist(player)) continue;
                    linkedToc = player;
                    this.setLinkedTo(player.getWurmId(), false);
                    break;
                }
            }
            if (linkedToc != null) {
                int targX = linkedToc.getTileX() + 15 + Server.rand.nextInt(6);
                if (this.getTileX() < linkedToc.getTileX()) {
                    targX = linkedToc.getTileX() - 15 - Server.rand.nextInt(6);
                }
                int targY = linkedToc.getTileY() + 15 + Server.rand.nextInt(6);
                if (this.getTileY() < linkedToc.getTileY()) {
                    targX = linkedToc.getTileY() - 15 - Server.rand.nextInt(6);
                }
                targX = Zones.safeTileX(targX);
                targY = Zones.safeTileX(targY);
                return new int[]{targX, targY};
            }
        }
        return suggested;
    }

    public int[] findRandomCaveEntrance(int[] tiles) {
        int startx = Math.max(0, this.currentTile.tilex - 20);
        int endx = Math.min(Zones.worldTileSizeX - 1, this.currentTile.tilex + 20);
        int starty = Math.max(0, this.currentTile.tiley - 20);
        int endy = Math.min(Zones.worldTileSizeY - 1, this.currentTile.tiley + 20);
        if (this.citizenVillage != null) {
            startx = Math.max(0, this.citizenVillage.getStartX() - 5);
            endx = Math.min(Zones.worldTileSizeX - 1, this.citizenVillage.getEndX() + 5);
            starty = Math.max(0, this.citizenVillage.getStartY() - 5);
            endy = Math.min(Zones.worldTileSizeY - 1, this.citizenVillage.getEndY() + 5);
        }
        int rand = Server.rand.nextInt(endx - startx);
        startx += rand;
        rand = Server.rand.nextInt(endy - starty);
        starty += rand;
        boolean passMineDoors = this.isKingdomGuard() || this.isGhost() || this.isUnique();
        for (int x = startx; x < Math.min(endx, startx + 10); ++x) {
            for (int y = starty; y < Math.min(endy, starty + 10); ++y) {
                if (Tiles.decodeType(Server.surfaceMesh.getTile(x, y)) != Tiles.Tile.TILE_HOLE.id && (!passMineDoors || !Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))))) continue;
                tiles[0] = x;
                tiles[1] = y;
                return tiles;
            }
        }
        return tiles;
    }

    public int[] findBestBridgeEntrance(int tilex, int tiley, int layer, long bridgeId) {
        VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
        if (t != null && t.getStructure() != null && t.getStructure().getWurmId() == bridgeId) {
            return t.getStructure().findBestBridgeEntrance(this, tilex, tiley, layer, bridgeId, this.pathfindcounter);
        }
        return Structure.noEntrance;
    }

    public void setAbilityTitle(int newTitle) {
    }

    public int getAbilityTitleVal() {
        return this.template.abilityTitle;
    }

    public String getAbilityTitle() {
        if (this.template.abilityTitle > -1) {
            return Abilities.getAbilityString(this.template.abilityTitle) + " ";
        }
        return "";
    }

    public boolean isLogged() {
        return false;
    }

    public float getFaith() {
        return this.template.getFaith();
    }

    public Skill getChannelingSkill() {
        Skill channeling;
        block2: {
            channeling = null;
            try {
                channeling = this.skills.getSkill(10067);
            }
            catch (NoSuchSkillException nss) {
                if (!(this.getFaith() >= 10.0f)) break block2;
                channeling = this.skills.learn(10067, 1.0f);
            }
        }
        return channeling;
    }

    public Skill getMindLogical() {
        Skill toReturn = null;
        try {
            toReturn = this.getSkills().getSkill(100);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(100, 1.0f);
        }
        return toReturn;
    }

    public Skill getMindSpeed() {
        Skill toReturn = null;
        try {
            toReturn = this.getSkills().getSkill(101);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(101, 1.0f);
        }
        return toReturn;
    }

    public Skill getSoulDepth() {
        Skill toReturn = null;
        try {
            toReturn = this.getSkills().getSkill(106);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(106, 1.0f);
        }
        return toReturn;
    }

    public Skill getBreedingSkill() {
        Skill toReturn;
        try {
            toReturn = this.getSkills().getSkill(10085);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(10085, 1.0f);
        }
        return toReturn;
    }

    public Skill getSoulStrength() {
        Skill toReturn = null;
        try {
            toReturn = this.getSkills().getSkill(105);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(105, 1.0f);
        }
        return toReturn;
    }

    public Skill getBodyStrength() {
        Skill toReturn = null;
        try {
            toReturn = this.getSkills().getSkill(102);
        }
        catch (NoSuchSkillException nss) {
            toReturn = this.getSkills().learn(102, 1.0f);
        }
        return toReturn;
    }

    public Deity getDeity() {
        return this.template.getDeity();
    }

    public void modifyFaith(float modifier) {
    }

    public boolean isActionFaithful(Action action) {
        if (this.getDeity() != null && this.faithful) {
            return this.getDeity().isActionFaithful(action);
        }
        return true;
    }

    public void performActionOkey(Action action) {
        if (this.getDeity() != null && !this.getDeity().performActionOkey(this, action)) {
            this.getCommunicator().sendNormalServerMessage(this.getDeity().name + " noticed you!");
        }
    }

    public void setFaith(float faith) throws IOException {
    }

    public void setDeity(@Nullable Deity deity) throws IOException {
    }

    public boolean checkLoyaltyProgram() {
        return false;
    }

    public boolean maybeModifyAlignment(float modification) {
        return false;
    }

    public void setAlignment(float align) throws IOException {
    }

    public void setPriest(boolean priest) {
    }

    public boolean isPriest() {
        return this.isSpellCaster() || this.isSummoner();
    }

    public float getAlignment() {
        return this.template.getAlignment();
    }

    public float getFavor() {
        if (this.isSpellCaster() || this.isSummoner()) {
            return this.creatureFavor;
        }
        return this.template.getFaith();
    }

    public float getFavorLinked() {
        return this.template.getFaith();
    }

    public void setFavor(float favor) throws IOException {
        if (this.isSpellCaster() || this.isSummoner()) {
            this.creatureFavor = favor;
        }
    }

    public void depleteFavor(float favorToRemove, boolean combatSpell) throws IOException {
        if (this.isSpellCaster() || this.isSummoner()) {
            this.setFavor(this.getFavor() - favorToRemove);
        }
    }

    public boolean mayChangeDeity(int targetDeity) {
        return true;
    }

    public void setChangedDeity() throws IOException {
    }

    public boolean isNewbie() {
        return false;
    }

    public boolean maySteal() {
        return true;
    }

    public boolean isAtWarWith(Creature creature) {
        if (this.citizenVillage != null && creature.citizenVillage != null) {
            return this.citizenVillage.isEnemy(creature.citizenVillage);
        }
        return false;
    }

    public boolean isChampion() {
        return false;
    }

    public void setRealDeath(byte realdeathcounter) throws IOException {
    }

    public boolean modifyChampionPoints(int championPointsModifier) {
        return false;
    }

    public int getFatigueLeft() {
        return 20000;
    }

    public void decreaseFatigue() {
    }

    public boolean checkPrayerFaith() {
        return false;
    }

    public boolean isAlive() {
        return !this.status.dead;
    }

    public boolean isDead() {
        return this.status.dead;
    }

    public byte getKingdomId() {
        Village bVill;
        if (!Servers.isThisAPvpServer() && (bVill = this.getBrandVillage()) != null) {
            return bVill.kingdom;
        }
        return this.status.kingdom;
    }

    public byte getKingdomTemplateId() {
        Kingdom k = Kingdoms.getKingdom(this.getKingdomId());
        if (k != null) {
            return k.getTemplate();
        }
        return 0;
    }

    public int getReputation() {
        return this.template.getReputation();
    }

    public void setReputation(int reputation) {
    }

    public void playAnthem() {
        if (this.musicPlayer != null) {
            if (this.getKingdomTemplateId() == 3) {
                this.musicPlayer.checkMUSIC_ANTHEMHOTS_SND();
            }
            if (this.getKingdomId() == 1) {
                this.musicPlayer.checkMUSIC_ANTHEMJENN_SND();
            }
            if (this.getKingdomId() == 2) {
                this.musicPlayer.checkMUSIC_ANTHEMMOLREHAN_SND();
            }
        }
    }

    public boolean isTransferring() {
        return false;
    }

    public boolean isOnCurrentServer() {
        return true;
    }

    public boolean setKingdomId(byte kingdom) throws IOException {
        return this.setKingdomId(kingdom, false, true);
    }

    public boolean setKingdomId(byte kingdom, boolean forced) throws IOException {
        return this.setKingdomId(kingdom, forced, true);
    }

    public boolean setKingdomId(byte kingdom, boolean forced, boolean setTimeStamp) throws IOException {
        return this.setKingdomId(kingdom, forced, setTimeStamp, true);
    }

    public boolean setKingdomId(byte kingdom, boolean forced, boolean setTimeStamp, boolean online) throws IOException {
        boolean sendUpdate = false;
        if (this.getKingdomId() != kingdom) {
            KingdomIp kip;
            if (this.isKing()) {
                this.getCommunicator().sendNormalServerMessage("You are the king, and may not change kingdom!");
                return false;
            }
            Village v = this.getCitizenVillage();
            if (!forced && v != null && v.getMayor().getId() == this.getWurmId()) {
                try {
                    this.getCommunicator().sendNormalServerMessage("You are the mayor of " + v.getName() + ", and may not change kingdom!");
                    return false;
                }
                catch (Exception ex) {
                    return false;
                }
            }
            if (Kingdoms.getKingdomTemplateFor(this.getKingdomId()) == 3 && Kingdoms.getKingdomTemplateFor(kingdom) != 3) {
                if (this.getDeity() != null && this.getDeity().number == 4) {
                    this.setDeity(null);
                    this.setFaith(0.0f);
                    this.setAlignment(Math.max(1.0f, this.getAlignment()));
                }
            } else if (Kingdoms.getKingdomTemplateFor(kingdom) == 3 && Kingdoms.getKingdomTemplateFor(this.getKingdomId()) != 3 && (this.getDeity() == null || this.getDeity().number == 1 || this.getDeity().number == 2 || this.getDeity().number == 3)) {
                this.setDeity(Deities.getDeity(4));
                this.setAlignment(Math.min(this.getAlignment(), -50.0f));
                this.setFaith(1.0f);
            }
            if (this.getKingdomId() != 0 && !forced) {
                if (this.citizenVillage != null) {
                    this.citizenVillage.removeCitizen(this);
                }
                if (kingdom != 0 && Servers.localServer.PVPSERVER) {
                    this.increaseChangedKingdom(setTimeStamp);
                }
                sendUpdate = true;
            }
            this.clearRoyalty();
            this.setTeam(null, true);
            if (this.isPlayer() && this.getCommunicator() != null && this.hasLink() && Servers.localServer.PVPSERVER && !Servers.localServer.testServer) {
                try {
                    kip = KingdomIp.getKIP(this.getCommunicator().getConnection().getIp(), this.getKingdomId());
                    if (kip != null) {
                        kip.logon(kingdom);
                    }
                }
                catch (Exception iox) {
                    logger.log(Level.INFO, this.getName() + " " + iox.getMessage());
                }
            }
            this.status.setKingdom(kingdom);
            if (this.isPlayer()) {
                if (Servers.localServer.isChallengeOrEpicServer() || Servers.isThisAChaosServer() || Servers.localServer.PVPSERVER) {
                    if (this.getCommunicator().getConnection() != null) {
                        try {
                            if (this.getCommunicator().getConnection().getIp() != null && (kip = KingdomIp.getKIP(this.getCommunicator().getConnection().getIp())) != null) {
                                kip.setKingdom(kingdom);
                            }
                        }
                        catch (NullPointerException nullPointerException) {
                            // empty catch block
                        }
                    }
                    if (Server.getInstance().isPS() && Servers.localServer.PVPSERVER || Servers.isThisAChaosServer()) {
                        ((Player)this).getSaveFile().setChaosKingdom(kingdom);
                    }
                }
                Players.getInstance().registerNewKingdom(this);
                this.setVotedKing(false);
            }
            this.playAnthem();
            Creatures.getInstance().setCreatureDead(this);
            this.setTarget(-10L, true);
            if (sendUpdate && online) {
                this.refreshVisible();
            }
            if (this.citizenVillage != null) {
                if (!forced) {
                    this.citizenVillage.removeCitizen(this);
                } else if (this.citizenVillage.getMayor().wurmId == this.getWurmId()) {
                    this.citizenVillage.convertToKingdom(kingdom, true, setTimeStamp);
                }
            }
        }
        return true;
    }

    public void setVotedKing(boolean voted) {
    }

    public boolean hasVotedKing() {
        return true;
    }

    public void clearRoyalty() {
    }

    public void checkForEnemies() {
        this.checkForEnemies(false);
    }

    public void checkForEnemies(boolean overrideRandomChance) {
        if ((this.isWarGuard() || this.isKingdomGuard()) && this.guardTower != null && this.guardTower.getKingdom() == this.getKingdomId() && System.currentTimeMillis() - this.guardTower.getLastSentWarning() < 180000L) {
            overrideRandomChance = true;
        }
        if ((overrideRandomChance || Server.rand.nextInt(this.isKingdomGuard() || this.isWarGuard() ? 20 : 100) == 0) && this.getVisionArea() != null) {
            try {
                if (this.isOnSurface()) {
                    this.getVisionArea().getSurface().checkForEnemies();
                } else {
                    this.getVisionArea().getUnderGround().checkForEnemies();
                }
            }
            catch (Exception ep) {
                logger.log(Level.WARNING, ep.getMessage(), ep);
            }
        }
    }

    public boolean sendTransfer(Server senderServer, String targetIp, int targetPort, String serverpass, int targetServerId, int tilex, int tiley, boolean surfaced, boolean toOrFromEpic, byte targetKingdomId) {
        logger.log(Level.WARNING, "Sendtransfer called in creature", new Exception());
        return false;
    }

    public void increaseChangedKingdom(boolean setTimeStamp) throws IOException {
    }

    public boolean mayChangeKingdom(Creature converter) {
        return false;
    }

    public boolean isOfCustomKingdom() {
        Kingdom k = Kingdoms.getKingdom(this.getKingdomId());
        return k != null && k.isCustomKingdom();
    }

    public void punishSkills(double aMod, boolean pvp) {
        if (this.getCultist() != null && this.getCultist().isNoDecay()) {
            return;
        }
        try {
            Skill bodyStr = this.skills.getSkill(102);
            bodyStr.setKnowledge(bodyStr.getKnowledge() - (double)0.01f, false);
            Skill body = this.skills.getSkill(1);
            body.setKnowledge(body.getKnowledge() - (double)0.01f, false);
        }
        catch (NoSuchSkillException nss) {
            this.skills.learn(102, 1.0f);
            logger.log(Level.WARNING, this.getName() + " learnt body strength.");
        }
        if (!pvp) {
            Skill[] sk = this.skills.getSkills();
            int nums = 0;
            for (Skill lElement : sk) {
                if (lElement.getType() != 4 && lElement.getType() != 2 || lElement.getNumber() == 1023 || Server.rand.nextInt(10) != 0 || !(lElement.getKnowledge(0.0) > 2.0) || !(lElement.getKnowledge(0.0) < 99.0)) continue;
                lElement.setKnowledge(Math.max(1.0, lElement.getKnowledge() - aMod), false);
                if (++nums > 4) break;
            }
        }
    }

    public long getMoney() {
        return 0L;
    }

    public boolean addMoney(long moneyToAdd) throws IOException {
        return false;
    }

    public boolean chargeMoney(long moneyToCharge) throws IOException {
        return false;
    }

    public boolean hasCustomColor() {
        if (this.getPower() > 0) {
            return true;
        }
        if (this.hasCustomKingdom()) {
            return true;
        }
        if (this.status.hasCustomColor()) {
            return true;
        }
        return this.template.getColorRed() != 255 || this.template.getColorGreen() != 255 || this.template.getColorBlue() != 255;
    }

    public boolean hasCustomKingdom() {
        return this.getKingdomId() > 4 || this.getKingdomId() < 0;
    }

    public byte getColorRed() {
        if (this.status.hasCustomColor()) {
            return this.status.getColorRed();
        }
        return (byte)this.template.getColorRed();
    }

    public byte getColorGreen() {
        if (this.status.hasCustomColor()) {
            return this.status.getColorGreen();
        }
        return (byte)this.template.getColorGreen();
    }

    public byte getColorBlue() {
        if (this.status.hasCustomColor()) {
            return this.status.getColorBlue();
        }
        return (byte)this.template.getColorBlue();
    }

    public boolean hasCustomSize() {
        if (this.status.getSizeMod() != 1.0f) {
            return true;
        }
        return this.template.getSizeModX() != 64 || this.template.getSizeModY() != 64 || this.template.getSizeModZ() != 64;
    }

    public byte getSizeModX() {
        return (byte)Math.min(255.0f, (float)this.template.getSizeModX() * this.status.getSizeMod());
    }

    public byte getSizeModY() {
        return (byte)Math.min(255.0f, (float)this.template.getSizeModY() * this.status.getSizeMod());
    }

    public byte getSizeModZ() {
        return (byte)Math.min(255.0f, (float)this.template.getSizeModZ() * this.status.getSizeMod());
    }

    public void setMoney(long newMoney) throws IOException {
    }

    public void setClimbing(boolean climbing) throws IOException {
    }

    public boolean isClimbing() {
        return true;
    }

    public boolean acceptsInvitations() {
        return false;
    }

    public Cultist getCultist() {
        return null;
    }

    public static short[] getTileSteepness(int tilex, int tiley, boolean surfaced) {
        short highest = -100;
        short lowest = 32000;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                if (tilex + x >= Zones.worldTileSizeX || tiley + y >= Zones.worldTileSizeY) continue;
                short height = 0;
                height = surfaced ? Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y)) : Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
                if (height > highest) {
                    highest = height;
                }
                if (height >= lowest) continue;
                lowest = height;
            }
        }
        int med = (highest + lowest) / 2;
        return new short[]{(short)med, (short)(highest - lowest)};
    }

    public short[] getLowestTileCorner(short tilex, short tiley) {
        short lowestX = tilex;
        short lowestY = tiley;
        short lowest = 32000;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                short height;
                if (tilex + x >= Zones.worldTileSizeX || tiley + y >= Zones.worldTileSizeY || (height = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y))) >= lowest) continue;
                lowest = height;
                lowestX = (short)(tilex + x);
                lowestY = (short)(tiley + y);
            }
        }
        return new short[]{lowestX, lowestY};
    }

    public void setSecondTitle(Titles.Title title) {
    }

    public void setTitle(Titles.Title title) {
    }

    public Titles.Title getSecondTitle() {
        return null;
    }

    public Titles.Title getTitle() {
        return null;
    }

    public String getTitleString() {
        String suff = "";
        if (this.getTitle() != null) {
            if (this.getTitle().isRoyalTitle()) {
                if (this.getAppointments() != 0L || this.isAppointed()) {
                    suff = suff + this.getKingdomTitle();
                }
            } else {
                suff = suff + this.getTitle().getName(this.isNotFemale());
            }
        }
        if (Features.Feature.COMPOUND_TITLES.isEnabled() && this.getSecondTitle() != null) {
            if (this.getTitle() != null) {
                suff = suff + " ";
            }
            if (this.getSecondTitle().isRoyalTitle()) {
                if (this.getAppointments() != 0L || this.isAppointed()) {
                    suff = suff + this.getKingdomTitle();
                }
            } else {
                suff = suff + this.getSecondTitle().getName(this.isNotFemale());
            }
        }
        return suff;
    }

    public String getKingdomTitle() {
        return "";
    }

    public void setFinestAppointment() {
    }

    public float getSpellDamageProtectBonus() {
        return this.getBonusForSpellEffect((byte)19);
    }

    public float getDetectDangerBonus() {
        SpellEffect effbon;
        if (this.getKingdomTemplateId() == 3) {
            return 50.0f + ItemBonus.getDetectionBonus(this);
        }
        SpellEffects eff = this.getSpellEffects();
        if (eff != null && (effbon = eff.getSpellEffect((byte)21)) != null) {
            return effbon.power + ItemBonus.getDetectionBonus(this);
        }
        return ItemBonus.getDetectionBonus(this);
    }

    public float getBonusForSpellEffect(byte enchantment) {
        SpellEffect skillgain;
        SpellEffects eff = this.getSpellEffects();
        if (eff != null && (skillgain = eff.getSpellEffect(enchantment)) != null) {
            return skillgain.power;
        }
        return 0.0f;
    }

    public float getNoLocateItemBonus(boolean reducePower) {
        Item[] bodyItems = this.getBody().getContainersAndWornItems();
        float maxBonus = 0.0f;
        Item maxItem = null;
        for (int x = 0; x < bodyItems.length; ++x) {
            if (!bodyItems[x].isEnchantableJewelry() && !bodyItems[x].isArtifact() || !(bodyItems[x].getNolocateBonus() > maxBonus)) continue;
            maxBonus = bodyItems[x].getNolocateBonus();
            maxItem = bodyItems[x];
        }
        if (maxItem != null) {
            SpellEffect eff;
            maxBonus = (maxBonus + maxItem.getCurrentQualityLevel()) / 2.0f;
            ItemSpellEffects effs = maxItem.getSpellEffects();
            if (effs == null) {
                effs = new ItemSpellEffects(maxItem.getWurmId());
            }
            if ((eff = effs.getSpellEffect((byte)29)) != null && reducePower) {
                eff.setPower(eff.power - 0.2f);
            }
        }
        return maxBonus;
    }

    public int getNumberOfShopItems() {
        Set<Item> ite = this.getInventory().getItems();
        int nums = 0;
        for (Item i : ite) {
            if (i.isCoin()) continue;
            ++nums;
        }
        return nums;
    }

    public final void addNewbieBuffs() {
        if (this.getPlayingTime() < 86400000L) {
            SpellEffect health;
            SpellEffect range;
            SpellEffects effs = this.createSpellEffects();
            SpellEffect eff = effs.getSpellEffect((byte)74);
            if (eff == null) {
                this.getCommunicator().sendSafeServerMessage("You require less food and drink as a new player.");
                eff = new SpellEffect(this.getWurmId(), 74, 100.0f, (int)((86400000L - this.getPlayingTime()) / 1000L), 1, 0, true);
                effs.addSpellEffect(eff);
            }
            if ((range = effs.getSpellEffect((byte)73)) == null) {
                this.getCommunicator().sendSafeServerMessage("Creatures and monsters are less aggressive to new players.");
                range = new SpellEffect(this.getWurmId(), 73, 100.0f, (int)((86400000L - this.getPlayingTime()) / 1000L), 1, 0, true);
                effs.addSpellEffect(range);
            }
            if ((health = effs.getSpellEffect((byte)75)) == null) {
                this.getCommunicator().sendSafeServerMessage("You regenerate health faster as a new player.");
                health = new SpellEffect(this.getWurmId(), 75, 100.0f, (int)((86400000L - this.getPlayingTime()) / 1000L), 1, 0, true);
                effs.addSpellEffect(health);
            }
        }
    }

    public SpellEffects getSpellEffects() {
        return this.getStatus().spellEffects;
    }

    public void sendUpdateSpellEffect(SpellEffect effect) {
        SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
        if (spellEffect != SpellEffectsEnum.NONE) {
            this.getCommunicator().sendAddSpellEffect(effect.id, spellEffect, effect.timeleft, effect.power);
        } else {
            this.getCommunicator().sendAddSpellEffect(effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect.getSpellInfluenceType(), effect.timeleft, effect.power);
        }
    }

    public void sendAddSpellEffect(SpellEffect effect) {
        SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
        if (spellEffect != SpellEffectsEnum.NONE) {
            this.getCommunicator().sendAddSpellEffect(effect.id, spellEffect, effect.timeleft, effect.power);
        } else {
            this.getCommunicator().sendAddSpellEffect(effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect.getSpellInfluenceType(), effect.timeleft, effect.power);
        }
        if (effect.type == 23) {
            this.getCombatHandler().addDodgeModifier(willowMod);
        } else if (effect.type == 39) {
            this.getMovementScheme().setChargeMoveMod(true);
        }
    }

    public void removeSpellEffect(SpellEffect effect) {
        SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
        if (spellEffect != SpellEffectsEnum.NONE) {
            this.getCommunicator().sendRemoveSpellEffect(effect.id, spellEffect);
        } else {
            this.getCommunicator().sendRemoveSpellEffect(effect.id, null);
        }
        this.getCommunicator().sendNormalServerMessage("You are no longer affected by " + effect.getName() + ".");
        if (effect.type == 23) {
            this.getCombatHandler().removeDodgeModifier(willowMod);
        } else if (effect.type == 39) {
            this.getMovementScheme().setChargeMoveMod(false);
        } else if (effect.type == 64) {
            this.setVisible(false);
            this.refreshAttitudes();
            this.setVisible(true);
        } else if (effect.type == 72) {
            this.setModelName("Human");
        }
    }

    public final void removeIllusion() {
        SpellEffect ill;
        if (this.getSpellEffects() != null && (ill = this.getSpellEffects().getSpellEffect((byte)72)) != null) {
            this.getSpellEffects().removeSpellEffect(ill);
        }
    }

    public void sendRemovePhantasms() {
    }

    public SpellEffects createSpellEffects() {
        if (this.getStatus().spellEffects == null) {
            this.getStatus().spellEffects = new SpellEffects(this.getWurmId());
        }
        return this.getStatus().spellEffects;
    }

    @Deprecated
    public boolean dispelSpellEffect(double power) {
        boolean toret = false;
        if (this.getMovementScheme().setWebArmourMod(false, 0.0f)) {
            this.getMovementScheme().setWebArmourMod(false, 0.0f);
            toret = true;
        }
        if (this.getSpellEffects() != null) {
            SpellEffect[] speffs = this.getSpellEffects().getEffects();
            for (int x = 0; x < speffs.length; ++x) {
                if (speffs[x].type == 64 || speffs[x].type == 74 || speffs[x].type == 73 || speffs[x].type == 75 || !((double)Server.rand.nextInt(Math.max(1, (int)speffs[x].power)) < power)) continue;
                this.getSpellEffects().removeSpellEffect(speffs[x]);
                if (speffs[x].type == 22 && this.getCurrentTile() != null) {
                    this.getCurrentTile().setNewRarityShader(this);
                }
                return true;
            }
        }
        return toret;
    }

    public byte getFarwalkerSeconds() {
        return 0;
    }

    protected void setFarwalkerSeconds(byte seconds) {
    }

    public void activeFarwalkerAmulet(Item amulet) {
    }

    public Creature getDominator() {
        if (this.dominator == -10L) {
            return null;
        }
        try {
            return Server.getInstance().getCreature(this.dominator);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public Item getWornItem(byte bodyPart) {
        try {
            return this.getEquippedItem(bodyPart);
        }
        catch (NoSpaceException nsp) {
            return null;
        }
    }

    public boolean hasBridle() {
        Item neckItem;
        if ((this.isHorse() || this.isUnicorn()) && (neckItem = this.getWornItem((byte)17)) != null) {
            return neckItem.isBridle();
        }
        return false;
    }

    private float calcHorseShoeBonus(boolean mounting) {
        float bonus = 0.0f;
        float leftFootB = 0.0f;
        float rightFootB = 0.0f;
        float leftHandB = 0.0f;
        float rightHandB = 0.0f;
        try {
            Item leftFoot = this.getEquippedItem((byte)15);
            if (leftFoot != null) {
                leftFootB += Math.max(10.0f, leftFoot.getCurrentQualityLevel()) / 2000.0f;
                leftFootB += leftFoot.getSpellSpeedBonus() / 2000.0f;
                leftFootB += (float)leftFoot.getRarity() * 0.03f;
                if (!mounting && !this.ignoreSaddleDamage) {
                    leftFoot.setDamage(leftFoot.getDamage() + 0.001f);
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, this.getName() + " No left foot.");
        }
        try {
            Item rightFoot = this.getEquippedItem((byte)16);
            if (rightFoot != null) {
                rightFootB += Math.max(10.0f, rightFoot.getCurrentQualityLevel()) / 2000.0f;
                rightFootB += rightFoot.getSpellSpeedBonus() / 2000.0f;
                rightFootB += (float)rightFoot.getRarity() * 0.03f;
                if (!mounting && !this.ignoreSaddleDamage) {
                    rightFoot.setDamage(rightFoot.getDamage() + 0.001f);
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, this.getName() + " No left foot.");
        }
        try {
            Item rightHand = this.getEquippedItem((byte)14);
            if (rightHand != null) {
                rightHandB += Math.max(10.0f, rightHand.getCurrentQualityLevel()) / 2000.0f;
                rightHandB += rightHand.getSpellSpeedBonus() / 2000.0f;
                rightHandB += (float)rightHand.getRarity() * 0.03f;
                if (!mounting && !this.ignoreSaddleDamage) {
                    rightHand.setDamage(rightHand.getDamage() + 0.001f);
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, this.getName() + " No left foot.");
        }
        try {
            Item leftHand = this.getEquippedItem((byte)13);
            if (leftHand != null) {
                leftHandB += Math.max(10.0f, leftHand.getCurrentQualityLevel()) / 2000.0f;
                leftHandB += leftHand.getSpellSpeedBonus() / 2000.0f;
                leftHandB += (float)leftHand.getRarity() * 0.03f;
                if (!mounting && !this.ignoreSaddleDamage) {
                    leftHand.setDamage(leftHand.getDamage() + 0.001f);
                }
            }
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, this.getName() + " No left foot.");
        }
        bonus += leftHandB;
        bonus += rightHandB;
        bonus += leftFootB;
        return bonus += rightFootB;
    }

    public boolean hasHands() {
        return this.template.hasHands;
    }

    public boolean isDominated() {
        return this.dominator > 0L;
    }

    public boolean setDominator(long newdominator) {
        if (newdominator == -10L) {
            if (this.decisions != null) {
                this.decisions.clearOrders();
                this.decisions = null;
            }
            try {
                this.setKingdomId((byte)0);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            this.setLoyalty(0.0f);
            this.setLeader(null);
        }
        if (newdominator != this.dominator) {
            this.dominator = newdominator;
            this.getStatus().setDominator(this.dominator);
            this.sendAttitudeChange();
            return true;
        }
        return false;
    }

    public boolean hasPet() {
        return false;
    }

    public boolean isOnFire() {
        return this.template.isOnFire();
    }

    public byte getFireRadius() {
        return this.template.getFireRadius();
    }

    public int getPaintMode() {
        return this.template.getPaintMode();
    }

    public boolean addOrder(Order order) {
        if (this.decisions == null) {
            this.decisions = new DecisionStack();
        }
        return this.decisions.addOrder(order);
    }

    public void clearOrders() {
        if (this.decisions != null) {
            this.decisions.clearOrders();
        }
        this.getStatus().setPath(null);
        this.getStatus().setMoving(false);
        this.setTarget(-10L, true);
    }

    public Order getFirstOrder() {
        if (this.decisions != null) {
            return this.decisions.getFirst();
        }
        return null;
    }

    public void removeOrder(Order order) {
        if (this.decisions != null) {
            this.decisions.removeOrder(order);
        }
    }

    public boolean hasOrders() {
        if (this.decisions != null) {
            return this.decisions.hasOrders();
        }
        return false;
    }

    public boolean mayReceiveOrder() {
        if (this.decisions != null) {
            return this.decisions.mayReceiveOrders();
        }
        if (this.isDominated()) {
            this.decisions = new DecisionStack();
            return true;
        }
        return false;
    }

    public void setPet(long petId) {
    }

    public Creature getPet() {
        return null;
    }

    public void modifyLoyalty(float modifier) {
        if (this.getStatus().modifyLoyalty(modifier)) {
            if (this.getDominator() != null) {
                this.getDominator().getCommunicator().sendAlertServerMessage(this.getNameWithGenus() + " is tame no more.", (byte)2);
                this.getDominator().setPet(-10L);
            }
            this.setDominator(-10L);
        }
    }

    public void setLoyalty(float loyalty) {
        this.getStatus().setLoyalty(loyalty);
    }

    public float getLoyalty() {
        return this.getStatus().loyalty;
    }

    public ArmourTemplate.ArmourType getArmourType() {
        return this.template.getArmourType();
    }

    public boolean isFrozen() {
        return false;
    }

    public void toggleFrozen(Creature freezer) {
    }

    protected void setLastVehicle(long _lastvehicle, byte _seatType) {
        this.status.setVehicle(_lastvehicle, _seatType);
    }

    public void setVehicle(long vehicle, boolean teleport, byte _seatType) {
        this.setVehicle(vehicle, teleport, _seatType, -1, -1);
    }

    public void setVehicle(long _vehicle, boolean teleport, byte _seatType, int tilex, int tiley) {
        block67: {
            Item ivehic;
            Creature lVehicle;
            if (_vehicle == -10L) {
                block66: {
                    if (this.vehicle != -10L) {
                        this.removeIllusion();
                        if (this.getVisionArea() != null) {
                            if (this.getVisionArea().getSurface() != null) {
                                this.getVisionArea().getSurface().clearMovementForCreature(this.vehicle);
                            }
                            if (this.getVisionArea().getUnderGround() != null) {
                                this.getVisionArea().getUnderGround().clearMovementForCreature(this.vehicle);
                            }
                        }
                        if (WurmId.getType(this.vehicle) == 1) {
                            this.setLastVehicle(-10L, (byte)-1);
                            try {
                                float newposy;
                                float newposx;
                                lVehicle = Server.getInstance().getCreature(this.vehicle);
                                lVehicle.removeRider(this.getWurmId());
                                if (!teleport) break block66;
                                Structure struct = this.getActualTileVehicle().getStructure();
                                if (struct != null && !struct.mayPass(this)) {
                                    try {
                                        newposx = lVehicle.getPosX();
                                        newposy = lVehicle.getPosY();
                                        tilex = (int)newposx / 4;
                                        tiley = (int)newposy / 4;
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage(), ex);
                                    }
                                }
                                if (this.isOnSurface() || !Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) break block66;
                                try {
                                    newposx = lVehicle.getPosX();
                                    newposy = lVehicle.getPosY();
                                    tilex = (int)newposx / 4;
                                    tiley = (int)newposy / 4;
                                }
                                catch (Exception ex) {
                                    logger.log(Level.WARNING, ex.getMessage(), ex);
                                }
                            }
                            catch (NoSuchCreatureException nsi) {
                                logger.log(Level.WARNING, this.getName() + " " + nsi.getMessage(), nsi);
                            }
                            catch (NoSuchPlayerException nsp) {
                                logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
                            }
                        } else {
                            try {
                                float newposy;
                                float newposx;
                                Creature dragger;
                                ivehic = Items.getItem(this.vehicle);
                                boolean atTransferBorder = false;
                                if (this.getTileX() < 20 || this.getTileX() > Zones.worldTileSizeX - 20 || this.getTileY() < 20 || this.getTileY() > Zones.worldTileSizeX - 20) {
                                    atTransferBorder = true;
                                }
                                if (ivehic.isBoat() && (this.isTransferring() || atTransferBorder)) break block66;
                                this.setLastVehicle(-10L, (byte)-1);
                                if (!teleport) break block66;
                                Structure struct = this.getActualTileVehicle().getStructure();
                                if (struct != null && struct.isTypeHouse() && !struct.mayPass(this)) {
                                    try {
                                        dragger = Items.getDragger(ivehic);
                                        newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                                        newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                                        tilex = (int)newposx / 4;
                                        tiley = (int)newposy / 4;
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage(), ex);
                                    }
                                }
                                if (struct != null && struct.isTypeBridge()) {
                                    try {
                                        dragger = Items.getDragger(ivehic);
                                        newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                                        newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                                        tilex = (int)newposx / 4;
                                        tiley = (int)newposy / 4;
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage(), ex);
                                    }
                                }
                                if (!this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) {
                                    try {
                                        float newposx2 = ivehic.getPosX();
                                        float newposy2 = ivehic.getPosY();
                                        tilex = (int)newposx2 / 4;
                                        tiley = (int)newposy2 / 4;
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage(), ex);
                                    }
                                }
                            }
                            catch (NoSuchItemException nsi) {
                                this.setLastVehicle(-10L, (byte)-1);
                            }
                        }
                    }
                }
                this.getMovementScheme().offZ = 0.0f;
            }
            this.vehicle = _vehicle;
            this.seatType = _seatType;
            if (!this.isPlayer()) {
                this.setLastVehicle(_vehicle, _seatType);
            }
            if (this.vehicle != -10L) {
                this.removeIllusion();
                Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
                if (vehic != null) {
                    float s;
                    float r;
                    this.clearDestination();
                    this.setFarwalkerSeconds((byte)0);
                    this.getMovementScheme().setFarwalkerMoveMod(false);
                    this.movementScheme.setEncumbered(false);
                    this.movementScheme.setBaseModifier(1.0f);
                    this.setStealth(false);
                    float offx = 0.0f;
                    float offy = 0.0f;
                    for (int x = 0; x < vehic.seats.length; ++x) {
                        if (vehic.seats[x].occupant != this.getWurmId()) continue;
                        offx = vehic.seats[x].offx;
                        offy = vehic.seats[x].offy;
                        break;
                    }
                    if (vehic.creature) {
                        try {
                            Creature lVehicle2 = Server.getInstance().getCreature(this.vehicle);
                            r = (-lVehicle2.getStatus().getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                            s = (float)Math.sin(r);
                            float c = (float)Math.cos(r);
                            float xo = s * -offx - c * -offy;
                            float yo = c * -offx + s * -offy;
                            float newposx = lVehicle2.getPosX() + xo;
                            float newposy = lVehicle2.getPosY() + yo;
                            this.getMovementScheme().setVehicleRotation(lVehicle2.getStatus().getRotation());
                            this.getStatus().setRotation(lVehicle2.getStatus().getRotation());
                            this.setBridgeId(lVehicle2.getBridgeId());
                            this.setTeleportPoints(newposx, newposy, lVehicle2.getLayer(), lVehicle2.getFloorLevel());
                            if (this.getVisionArea() != null && (int)newposx >> 2 == this.getTileX() && (int)newposy >> 2 == this.getTileY()) {
                                this.embark(newposx, newposy, this.getPositionZ(), this.getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, null, lVehicle2, vehic);
                                break block67;
                            }
                            if (!this.getCommunicator().stillLoggingIn()) {
                                int tx = this.getTileX();
                                int ty = this.getTileY();
                                int nx = (int)newposx >> 2;
                                int ny = (int)newposy >> 2;
                                try {
                                    if (this.hasLink() && this.getVisionArea() != null) {
                                        this.getVisionArea().move(nx - tx, ny - ty);
                                        this.embark(newposx, newposy, this.getPositionZ(), this.getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, null, lVehicle2, vehic);
                                        this.getVisionArea().linkZones(nx - tx, ny - ty);
                                    }
                                    break block67;
                                }
                                catch (IOException ex) {
                                    this.startTeleporting(true);
                                    lVehicle2.setLeader(null);
                                    lVehicle2.addRider(this.getWurmId());
                                    this.sendMountData();
                                    if (this.isVehicleCommander()) {
                                        this.getCommunicator().sendTeleport(true, false, vehic.commandType);
                                        break block67;
                                    }
                                    this.getCommunicator().sendTeleport(false, false, (byte)0);
                                }
                                break block67;
                            }
                            this.startTeleporting(true);
                            lVehicle2.setLeader(null);
                            lVehicle2.addRider(this.getWurmId());
                            this.sendMountData();
                            if (this.isVehicleCommander()) {
                                this.getCommunicator().sendTeleport(true, false, vehic.commandType);
                                break block67;
                            }
                            this.getCommunicator().sendTeleport(false, false, (byte)0);
                        }
                        catch (NoSuchCreatureException nsi) {
                            logger.log(Level.WARNING, this.getName() + " " + nsi.getMessage(), nsi);
                        }
                        catch (NoSuchPlayerException nsp) {
                            logger.log(Level.WARNING, this.getName() + " " + nsp.getMessage(), nsp);
                        }
                    } else {
                        try {
                            Item lVehicle3 = Items.getItem(vehic.wurmid);
                            r = (-lVehicle3.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                            s = (float)Math.sin(r);
                            float c = (float)Math.cos(r);
                            float xo = s * -offx - c * -offy;
                            float yo = c * -offx + s * -offy;
                            float newposx = lVehicle3.getPosX() + xo;
                            float newposy = lVehicle3.getPosY() + yo;
                            this.getMovementScheme().setVehicleRotation(lVehicle3.getRotation());
                            this.getStatus().setRotation(lVehicle3.getRotation());
                            this.setBridgeId(lVehicle3.getBridgeId());
                            if (this.getVisionArea() != null && (int)newposx >> 2 == this.getTileX() && (int)newposy >> 2 == this.getTileY()) {
                                this.embark(newposx, newposy, this.getPositionZ(), this.getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, lVehicle3, null, vehic);
                                break block67;
                            }
                            this.setTeleportPoints(newposx, newposy, lVehicle3.isOnSurface() ? 0 : -1, lVehicle3.getFloorLevel());
                            if (this.isVehicleCommander()) {
                                Village v;
                                if (lVehicle3.getKingdom() != this.getKingdomId()) {
                                    Server.getInstance().broadCastAction(LoginHandler.raiseFirstLetter(lVehicle3.getName()) + " is now the property of " + Kingdoms.getNameFor(this.getKingdomId()) + "!", this, 10);
                                    String message = StringUtil.format("You declare the %s the property of %s.", lVehicle3.getName(), Kingdoms.getNameFor(this.getKingdomId()));
                                    this.getCommunicator().sendNormalServerMessage(message);
                                    lVehicle3.setLastOwnerId(this.getWurmId());
                                } else if (Servers.isThisAChaosServer() && ((v = Villages.getVillageForCreature(lVehicle3.getLastOwnerId())) == null || v.isEnemy(this.getCitizenVillage()))) {
                                    String vehname = this.getName();
                                    if (this.getCitizenVillage() != null) {
                                        vehname = this.getCitizenVillage().getName();
                                    }
                                    Server.getInstance().broadCastAction(LoginHandler.raiseFirstLetter(lVehicle3.getName()) + " is now the property of " + vehname + "!", this, 10);
                                    String message = StringUtil.format("You declare the %s the property of %s.", lVehicle3.getName(), vehname);
                                    this.getCommunicator().sendNormalServerMessage(message);
                                    lVehicle3.setLastOwnerId(this.getWurmId());
                                }
                                lVehicle3.setAuxData(this.getKingdomId());
                                this.setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle3);
                                break block67;
                            }
                            this.setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle3);
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.WARNING, this.getName() + " " + nsi.getMessage(), nsi);
                        }
                    }
                }
            } else if (teleport) {
                if (tilex < 0 && tiley < 0 && !this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)(this.getStatus().getPositionX() / 4.0f), (int)(this.getStatus().getPositionX() / 4.0f))))) {
                    try {
                        float newposy;
                        float newposx;
                        if (WurmId.getType(this.vehicle) == 1) {
                            lVehicle = Server.getInstance().getCreature(this.vehicle);
                            newposx = lVehicle.getPosX();
                            newposy = lVehicle.getPosY();
                            tilex = (int)newposx / 4;
                            tiley = (int)newposy / 4;
                        } else {
                            ivehic = Items.getItem(this.vehicle);
                            newposx = ivehic.getPosX();
                            newposy = ivehic.getPosY();
                            tilex = (int)newposx / 4;
                            tiley = (int)newposy / 4;
                        }
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
                if (tilex > -1 || tiley > -1) {
                    int ntx = tilex - this.getTileX();
                    int nty = tiley - this.getTileY();
                    float posz = this.getStatus().getPositionZ();
                    posz = Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), false, posz, this, this.getBridgeId());
                    try {
                        if (this.hasLink() && this.getVisionArea() != null) {
                            this.getVisionArea().move(ntx, nty);
                            this.intraTeleport(tilex * 4 + 2, tiley * 4 + 2, posz, this.getStatus().getRotation(), this.getLayer(), "left vehicle");
                            this.getVisionArea().linkZones(ntx, nty);
                        }
                    }
                    catch (IOException ex) {
                        this.setTeleportPoints((short)tilex, (short)tiley, this.getLayer(), 0);
                        this.startTeleporting(false);
                        this.getCommunicator().sendTeleport(false, true, (byte)0);
                    }
                } else {
                    Structure struct;
                    Structure structure = struct = this.getCurrentTile() != null ? this.getCurrentTile().getStructure() : Structures.getStructureForTile(this.getTileX(), this.getTileY(), this.isOnSurface());
                    if (struct == null || struct.mayPass(this)) {
                        float posz = this.getStatus().getPositionZ();
                        posz = Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), false, posz, this, this.getBridgeId());
                        this.intraTeleport(this.getStatus().getPositionX(), this.getStatus().getPositionY(), posz, this.getStatus().getRotation(), this.getLayer(), "left vehicle");
                    }
                }
                this.getMovementScheme().addWindImpact((byte)0);
                this.calcBaseMoveMod();
                this.getMovementScheme().commandingBoat = false;
                this.getCurrentTile().sendAttachCreature(this.getWurmId(), -1L, 0.0f, 0.0f, 0.0f, 0);
            } else {
                if (!this.getMovementScheme().isIntraTeleporting()) {
                    this.getMovementScheme().addWindImpact((byte)0);
                    this.calcBaseMoveMod();
                    this.getMovementScheme().setMooredMod(false);
                    this.getMovementScheme().commandingBoat = false;
                }
                this.getCurrentTile().sendAttachCreature(this.getWurmId(), -1L, 0.0f, 0.0f, 0.0f, 0);
            }
        }
    }

    public void intraTeleport(float posx, float posy, float posz, float aRot, int layer, String reason) {
        if (reason.contains("in rock")) {
            posx = this.getMovementScheme().xOld;
            posy = this.getMovementScheme().yOld;
        }
        ++this.teleports;
        if (this.isDead()) {
            return;
        }
        posx = Math.max(0.0f, Math.min(posx, Zones.worldMeterSizeX - 1.0f));
        posy = Math.max(0.0f, Math.min(posy, Zones.worldMeterSizeY - 1.0f));
        VolaTile t = this.getCurrentTile();
        if (t != null) {
            t.deleteCreatureQuick(this);
        } else {
            logger.log(Level.INFO, this.getName() + " no current tile when intrateleporting.");
        }
        this.getStatus().setPositionX(posx);
        this.getStatus().setPositionY(posy);
        this.getStatus().setPositionZ(posz);
        this.getStatus().setRotation(aRot);
        if (layer == 0 && Zones.getTextureForTile((int)posx >> 2, (int)posy >> 2, layer) == Tiles.Tile.TILE_HOLE.id) {
            layer = -1;
        }
        boolean visionAreaInitialized = false;
        if (this.getVisionArea() != null) {
            visionAreaInitialized = this.getVisionArea().isInitialized();
        }
        if (!reason.contains("Embarking") && !reason.contains("left vehicle")) {
            logger.log(Level.INFO, this.getName() + " intrateleport to " + posx + "," + posy + ", " + posz + ", layer " + layer + " currentTile:null=" + (t == null) + " reason=" + reason + " hasVisionArea=" + (this.getVisionArea() != null) + ", initialized=" + visionAreaInitialized + " vehicle=" + this.vehicle, new Exception());
            if (this.getPower() >= 3) {
                this.getCommunicator().sendAlertServerMessage("IntraTeleporting " + reason);
            }
        }
        this.getMovementScheme().setPosition(posx, posy, posz, aRot, layer);
        this.putInWorld();
        this.getMovementScheme().haltSpeedModifier();
        this.getCommunicator().setReady(false);
        this.getMovementScheme().setMooredMod(false);
        this.addCarriedWeight(0);
        try {
            this.sendActionControl("", false, 0);
            this.actions.stopCurrentAction(false);
        }
        catch (NoSuchActionException noSuchActionException) {
            // empty catch block
        }
        this.getMovementScheme().commandingBoat = false;
        this.getMovementScheme().addWindImpact((byte)0);
        this.getCommunicator().sendTeleport(true);
        this.disembark(false);
        this.getMovementScheme().addIntraTeleport(this.getTeleportCounter());
    }

    public Vector3f getActualPosVehicle() {
        Vehicle vehic;
        Vector3f toReturn = new Vector3f(this.getPosX(), this.getPosY(), this.getPositionZ());
        if (this.vehicle != -10L && (vehic = Vehicles.getVehicleForId(this.vehicle)) != null) {
            float offx = 0.0f;
            float offy = 0.0f;
            for (int x = 0; x < vehic.seats.length; ++x) {
                if (vehic.seats[x].occupant != this.getWurmId()) continue;
                offx = vehic.seats[x].offx;
                offy = vehic.seats[x].offy;
                break;
            }
            if (vehic.creature) {
                try {
                    Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
                    float r = (-lVehicle.getStatus().getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float s = (float)Math.sin(r);
                    float c = (float)Math.cos(r);
                    float xo = s * -offx - c * -offy;
                    float yo = c * -offx + s * -offy;
                    float newposx = lVehicle.getPosX() + xo;
                    float newposy = lVehicle.getPosY() + yo;
                    toReturn.setX(newposx);
                    toReturn.setY(newposy);
                }
                catch (NoSuchPlayerException | NoSuchCreatureException lVehicle) {}
            } else {
                try {
                    Item lVehicle = Items.getItem(vehic.wurmid);
                    float r = (-lVehicle.getRotation() + 180.0f) * (float)Math.PI / 180.0f;
                    float s = (float)Math.sin(r);
                    float c = (float)Math.cos(r);
                    float xo = s * -offx - c * -offy;
                    float yo = c * -offx + s * -offy;
                    float newposx = lVehicle.getPosX() + xo;
                    float newposy = lVehicle.getPosY() + yo;
                    toReturn.setX(newposx);
                    toReturn.setY(newposy);
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
            }
        }
        return toReturn;
    }

    protected VolaTile getActualTileVehicle() {
        Vector3f v = this.getActualPosVehicle();
        int nx = (int)v.x >> 2;
        int ny = (int)v.y >> 2;
        return Zones.getOrCreateTile(nx, ny, this.isOnSurface());
    }

    protected void setEmbarkTeleportVehicle(float newposx, float newposy, Vehicle vehic, Item lVehicle) {
        if (!this.getCommunicator().stillLoggingIn()) {
            int tx = this.getTileX();
            int ty = this.getTileY();
            int nx = (int)newposx >> 2;
            int ny = (int)newposy >> 2;
            try {
                if ((this.hasLink() || this.isWagoner()) && this.getVisionArea() != null) {
                    this.getVisionArea().move(nx - tx, ny - ty);
                    this.embark(newposx, newposy, this.getPositionZ(), this.getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, lVehicle, null, vehic);
                    this.getVisionArea().linkZones(nx - tx, ny - ty);
                }
            }
            catch (IOException ex) {
                this.startTeleporting(true);
                this.sendMountData();
                this.getCommunicator().sendTeleport(true, false, vehic.commandType);
            }
        } else {
            this.startTeleporting(true);
            this.sendMountData();
            if (this.isVehicleCommander()) {
                this.getCommunicator().sendTeleport(true, false, vehic.commandType);
            } else {
                this.getCommunicator().sendTeleport(false, false, (byte)0);
            }
        }
    }

    private void embark(float posx, float posy, float posz, float aRot, int layer, String reason, @Nullable Item lVehicle, Creature cVehicle, Vehicle vehic) {
        VolaTile t;
        if (!this.isVehicleCommander()) {
            this.stopLeading();
        }
        if ((t = this.getCurrentTile()) != null) {
            t.deleteCreatureQuick(this);
        } else {
            logger.log(Level.INFO, this.getName() + " no current tile when intrateleporting.");
        }
        this.getStatus().setPositionX(posx);
        this.getStatus().setPositionY(posy);
        this.getStatus().setPositionZ(posz);
        this.getStatus().setRotation(aRot);
        if (layer == 0 && Zones.getTextureForTile((int)posx >> 2, (int)posy >> 2, layer) == Tiles.Tile.TILE_HOLE.id) {
            layer = -1;
        }
        boolean setOffZ = false;
        if (this.mountAction != null) {
            setOffZ = true;
        }
        if (setOffZ) {
            if (lVehicle != null) {
                float targetZ = lVehicle.getPosZ();
                this.status.setPositionZ(targetZ + this.mountAction.getOffZ());
            } else if (cVehicle != null) {
                float cretZ = cVehicle.getStatus().getPositionZ();
                this.status.setPositionZ(cretZ + this.mountAction.getOffZ());
            }
            this.getMovementScheme().offZ = this.mountAction.getOffZ();
        }
        this.getMovementScheme().setPosition(posx, posy, this.status.getPositionZ(), this.status.getRotation(), this.getLayer());
        this.putInWorld();
        this.getMovementScheme().haltSpeedModifier();
        this.getCommunicator().setReady(false);
        if (this.status.isTrading()) {
            this.status.getTrade().end(this, false);
        }
        if (this.movementScheme.draggedItem != null) {
            MethodsItems.stopDragging(this, this.movementScheme.draggedItem);
        }
        try {
            this.sendActionControl("", false, 0);
            this.actions.stopCurrentAction(false);
        }
        catch (NoSuchActionException cretZ) {
            // empty catch block
        }
        this._enterVehicle = true;
        if (cVehicle != null) {
            cVehicle.setLeader(null);
            cVehicle.addRider(this.getWurmId());
        }
        this.sendMountData();
        if (this.isVehicleCommander()) {
            if (lVehicle != null) {
                Village v;
                if (lVehicle.getKingdom() != this.getKingdomId()) {
                    Server.getInstance().broadCastAction(LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + Kingdoms.getNameFor(this.getKingdomId()) + "!", this, 10);
                    String message = StringUtil.format("You declare the %s the property of %s.", lVehicle.getName(), Kingdoms.getNameFor(this.getKingdomId()));
                    this.getCommunicator().sendNormalServerMessage(message);
                    lVehicle.setLastOwnerId(this.getWurmId());
                } else if (Servers.isThisAChaosServer() && ((v = Villages.getVillageForCreature(lVehicle.getLastOwnerId())) == null || v.isEnemy(this.getCitizenVillage()))) {
                    String vehname = this.getName();
                    if (this.getCitizenVillage() != null) {
                        vehname = this.getCitizenVillage().getName();
                    }
                    Server.getInstance().broadCastAction(LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + vehname + "!", this, 10);
                    String message = StringUtil.format("You declare the %s the property of %s.", lVehicle.getName(), vehname);
                    this.getCommunicator().sendNormalServerMessage(message);
                    lVehicle.setLastOwnerId(this.getWurmId());
                }
                lVehicle.setAuxData(this.getKingdomId());
            }
            this.getCommunicator().sendTeleport(true, false, vehic.commandType);
        } else {
            this.getCommunicator().sendTeleport(true, false, (byte)0);
        }
        this.getMovementScheme().addIntraTeleport(this.getTeleportCounter());
    }

    public void disembark(boolean teleport) {
        this.disembark(teleport, -1, -1);
    }

    public void disembark(boolean teleport, int tilex, int tiley) {
        if (this.vehicle > -10L) {
            Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
            if (vehic != null) {
                Item item3;
                if (vehic.pilotId == this.getWurmId()) {
                    this.setVehicleCommander(false);
                    vehic.pilotId = -10L;
                    this.getCommunicator().setVehicleController(-1L, -1L, 0.0f, 0.0f, 0.0f, -2000.0f, 2000.0f, 2000.0f, 0.0f, 0);
                    try {
                        item3 = Items.getItem(this.vehicle);
                        item3.savePosition();
                    }
                    catch (Exception item2) {}
                } else if (vehic.pilotId != -10L) {
                    try {
                        item3 = Items.getItem(this.vehicle);
                        item3.savePosition();
                        Creature pilot = Server.getInstance().getCreature(vehic.pilotId);
                        if (!vehic.creature && item3.isBoat()) {
                            pilot.getMovementScheme().addMountSpeed(vehic.calculateNewBoatSpeed(true));
                        } else if (vehic.creature) {
                            vehic.updateDraggedSpeed(true);
                        }
                    }
                    catch (WurmServerException item3) {
                    }
                    catch (Exception item3) {
                        // empty catch block
                    }
                }
                String vehicName = Vehicle.getVehicleName(vehic);
                if (vehic.isChair()) {
                    this.getCommunicator().sendNormalServerMessage(StringUtil.format("You get up from the %s.", vehicName));
                    Server.getInstance().broadCastAction(StringUtil.format("%s gets up from the %s.", this.getName(), vehicName), this, 5);
                } else {
                    this.getCommunicator().sendNormalServerMessage(StringUtil.format("You leave the %s.", vehicName));
                    Server.getInstance().broadCastAction(StringUtil.format("%s leaves the %s.", this.getName(), vehicName), this, 5);
                }
                this.setVehicle(-10L, teleport, (byte)-1, tilex, tiley);
                int found = 0;
                for (int x = 0; x < vehic.seats.length; ++x) {
                    if (vehic.seats[x].occupant != this.getWurmId()) continue;
                    vehic.seats[x].occupant = -10L;
                    ++found;
                }
                if (found > 1) {
                    logger.log(Level.INFO, StringUtil.format("%s was occupying %d seats on %s.", this.getName(), found, vehicName));
                }
            } else {
                this.setVehicle(-10L, teleport, (byte)-1, tilex, tiley);
            }
        }
    }

    public int getTeleportCounter() {
        return 0;
    }

    public long getVehicle() {
        return this.vehicle;
    }

    public byte getSeatType() {
        return this.seatType;
    }

    public Vehicle getMountVehicle() {
        return Vehicles.getVehicleForId(this.getWurmId());
    }

    public boolean isVehicleCommander() {
        return this.isVehicleCommander;
    }

    public double getVillageSkillModifier() {
        return 0.0;
    }

    public void setVillageSkillModifier(double newModifier) {
    }

    public String getEmotePrefix() {
        return this.template.getName();
    }

    public void playAnimation(String animationName, boolean looping) {
        if (this.currentTile != null) {
            this.currentTile.sendAnimation(this, animationName, looping, -10L);
        }
    }

    public void playAnimation(String animationName, boolean looping, long aTarget) {
        if (this.currentTile != null) {
            this.currentTile.sendAnimation(this, animationName, looping, aTarget);
        }
    }

    public void sendStance(byte stance) {
        if (this.currentTile != null) {
            this.currentTile.sendStance(this, stance);
        }
    }

    public void sendDamage(float damPercent) {
        if (this.currentTile != null) {
            this.currentTile.sendCreatureDamage(this, damPercent);
        }
    }

    public void sendFishingLine(float posX, float posY, byte floatType) {
        if (this.currentTile != null) {
            this.currentTile.sendFishingLine(this, posX, posY, floatType);
        }
    }

    public void sendFishHooked(byte fishType, long fishId) {
        if (this.currentTile != null) {
            this.currentTile.sendFishHooked(this, fishType, fishId);
        }
    }

    public void sendFishingStopped() {
        if (this.currentTile != null) {
            this.currentTile.sendFishingStopped(this);
        }
    }

    public void sendSpearStrike(float posX, float posY) {
        if (this.currentTile != null) {
            this.currentTile.sendSpearStrike(this, posX, posY);
        }
    }

    public void checkTheftWarnQuestion() {
    }

    public void setTheftWarned(boolean warned) {
    }

    public void checkChallengeWarnQuestion() {
    }

    public void setChallengeWarned(boolean warned) {
    }

    public void addEnemyPresense() {
    }

    public void removeEnemyPresense() {
    }

    public int getEnemyPresense() {
        return 0;
    }

    public boolean mayMute() {
        return false;
    }

    public boolean hasNoReimbursement() {
        return true;
    }

    public boolean isDeathProtected() {
        return false;
    }

    public void setDeathProtected(boolean _deathProtected) {
    }

    public long mayChangeVillageInMillis() {
        return 0L;
    }

    public boolean hasGlow() {
        if (this.getPower() > 0) {
            return true;
        }
        return this.template.isGlowing();
    }

    public void loadAffinities() {
    }

    public void increaseAffinity(int skillnumber, int value) {
    }

    public void decreaseAffinity(int skillnumber, int value) {
    }

    public boolean mayOpportunityAttack() {
        if (this.isStunned()) {
            return false;
        }
        if (this.opportunityAttackCounter > 0) {
            return false;
        }
        return (double)this.getCombatHandler().getOpportunityAttacks() < this.getFightingSkill().getKnowledge(0.0) / 10.0;
    }

    public boolean opportunityAttack(Creature creature) {
        if (creature.isInvulnerable()) {
            return false;
        }
        if (!creature.isVisibleTo(this)) {
            return false;
        }
        if (this.isPlayer() && creature.isPlayer() && !Servers.isThisAPvpServer() && !this.isDuelOrSpar(creature)) {
            return false;
        }
        if (!(!this.isFighting() && creature.getWurmId() != this.target || this.isPlayer() && creature.isPlayer())) {
            if (this.isBridgeBlockingAttack(creature, false)) {
                return false;
            }
            if (this.mayOpportunityAttack() && this.getLayer() == creature.getLayer() && this.getMindSpeed().skillCheck(this.getCombatHandler().getOpportunityAttacks() * 10, 0.0, false, 1.0f) > 0.0) {
                if (this.opponent == null) {
                    this.setOpponent(creature);
                }
                return this.getCombatHandler().attack(creature, 10, true, 2.0f, null);
            }
        }
        return false;
    }

    public boolean isSparring(Creature _opponent) {
        return false;
    }

    public boolean isDuelling(Creature _opponent) {
        return false;
    }

    public boolean isDuelOrSpar(Creature _opponent) {
        return false;
    }

    public void setChangedTileCounter() {
    }

    public boolean isStealth() {
        return this.status.stealth;
    }

    public void setStealth(boolean stealth) {
        if (this.status.setStealth(stealth)) {
            if (stealth) {
                this.stealthBreakers = new HashSet<Long>();
                if (this.isPlayer()) {
                    this.getCommunicator().sendNormalServerMessage("You attempt to hide from others.", (byte)4);
                }
                this.movementScheme.setStealthMod(true);
            } else {
                if (this.stealthBreakers != null) {
                    this.stealthBreakers.clear();
                }
                this.getCommunicator().sendNormalServerMessage("You no longer hide.", (byte)4);
                this.movementScheme.setStealthMod(false);
            }
            this.checkInvisDetection();
        }
    }

    public void checkInvisDetection() {
        if (this.getBody().getBodyItem() != null) {
            this.getCurrentTile().checkVisibility(this, !this.isVisible() || this.isStealth());
        }
    }

    public boolean visibilityCheck(Creature watcher, float difficultyModifier) {
        if (!this.isVisible()) {
            return this.getPower() > 0 && this.getPower() <= watcher.getPower();
        }
        if (this.isStealth()) {
            if (this.getPower() > 0 && this.getPower() <= watcher.getPower()) {
                return true;
            }
            if (this.getPower() < watcher.getPower()) {
                return true;
            }
            if (watcher.isUnique()) {
                return true;
            }
            if (this.stealthBreakers != null && this.stealthBreakers.contains(watcher.getWurmId())) {
                return true;
            }
            int distModifier = (int)Math.max(Math.abs(watcher.getPosX() - this.getPosX()), Math.abs(watcher.getPosY() - this.getPosY()));
            if (watcher.getCurrentTile() == this.getCurrentTile() || watcher.isDetectInvis() || (float)Server.rand.nextInt((int)(100.0f + difficultyModifier + (float)distModifier)) < watcher.getDetectDangerBonus() / 5.0f || watcher.getMindLogical().skillCheck(this.getBodyControl() + (double)difficultyModifier + (double)distModifier, 0.0, true, 1.0f) > 0.0) {
                if (this.stealthBreakers == null) {
                    this.stealthBreakers = new HashSet<Long>();
                }
                this.stealthBreakers.add(watcher.getWurmId());
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isDetectInvis() {
        if (this.template.isDetectInvis()) {
            return true;
        }
        return this.status.detectInvisCounter > 0;
    }

    public boolean isVisibleTo(Creature watcher) {
        return this.isVisibleTo(watcher, false);
    }

    public boolean isVisibleTo(Creature watcher, boolean ignoreStealth) {
        if (!this.isVisible()) {
            return this.getPower() > 0 && this.getPower() <= watcher.getPower();
        }
        if (this.isStealth() && !ignoreStealth) {
            if (this.getPower() > 0 && this.getPower() <= watcher.getPower()) {
                return true;
            }
            if (this.getPower() < watcher.getPower()) {
                return true;
            }
            if (watcher.isUnique() || watcher.isDetectInvis()) {
                return true;
            }
            return this.stealthBreakers != null && this.stealthBreakers.contains(watcher.getWurmId());
        }
        return true;
    }

    public void addVisionModifier(DoubleValueModifier modifier) {
        if (this.visionModifiers == null) {
            this.visionModifiers = new HashSet<DoubleValueModifier>();
        }
        this.visionModifiers.add(modifier);
    }

    public void removeVisionModifier(DoubleValueModifier modifier) {
        if (this.visionModifiers != null) {
            this.visionModifiers.remove(modifier);
        }
    }

    public double getVisionMod() {
        if (this.visionModifiers == null) {
            return 0.0;
        }
        double doubleModifier = 0.0;
        for (DoubleValueModifier lDoubleValueModifier : this.visionModifiers) {
            doubleModifier += lDoubleValueModifier.getModifier();
        }
        return doubleModifier;
    }

    public int[] getCombatMoves() {
        return this.template.getCombatMoves();
    }

    public boolean isGuide() {
        return this.template.isTutorial();
    }

    public int getTutorialLevel() {
        return 9999;
    }

    public void setTutorialLevel(int newLevel) {
    }

    public boolean skippedTutorial() {
        return false;
    }

    public String getCurrentMissionInstruction() {
        return "";
    }

    public void missionFinished(boolean reward, boolean sendpopup) {
    }

    public boolean isNoSkillgain() {
        return this.template.isNoSkillgain() || this.isBred() || !this.isPaying();
    }

    public boolean isAutofight() {
        return false;
    }

    public final float getDamageModifier(boolean pvp, boolean spell) {
        float percent;
        double strength = !spell ? this.getStrengthSkill() : this.getSoulStrengthVal();
        float damMod = (float)(120.0 - strength) / 100.0f;
        if (this.isPlayer() && pvp && Servers.localServer.PVPSERVER) {
            damMod = (float)(1.0 - 0.15 * Math.log(Math.max(20.0, strength) * (double)0.8f - 15.0));
            damMod = Math.max(Math.min(damMod, 1.0f), 0.2f);
        }
        if (this.hasSpellEffect((byte)96)) {
            damMod *= 1.1f;
        }
        if (this.getCultist() != null && (percent = this.getCultist().getHalfDamagePercentage()) > 0.0f) {
            if (this.isChampion()) {
                float red = 1.0f - 0.1f * percent;
                damMod *= red;
            } else {
                float red = 1.0f - 0.3f * percent;
                damMod *= red;
            }
        }
        return damMod;
    }

    public String toString() {
        return "Creature [id: " + this.id + ", name: " + this.name + ", Tile: " + this.currentTile + ", Template: " + this.template + ", Status: " + this.status + ']';
    }

    public void sendToLoggers(String tolog) {
        this.sendToLoggers(tolog, (byte)2);
    }

    public void sendToLoggers(String tolog, byte restrictedToPower) {
        Creature receiver;
        if (this.loggerCreature1 != -10L) {
            try {
                receiver = Server.getInstance().getCreature(this.loggerCreature1);
                receiver.getCommunicator().sendLogMessage(this.getName() + " [" + tolog + "]");
            }
            catch (Exception ex) {
                this.loggerCreature1 = -10L;
            }
        }
        if (this.loggerCreature2 != -10L) {
            try {
                receiver = Server.getInstance().getCreature(this.loggerCreature2);
                receiver.getCommunicator().sendLogMessage(this.getName() + " [" + tolog + "]");
            }
            catch (Exception ex) {
                this.loggerCreature2 = -10L;
            }
        }
    }

    public long getAppointments() {
        return 0L;
    }

    public void addAppointment(int aid) {
    }

    public void removeAppointment(int aid) {
    }

    public boolean isFloating() {
        return this.template.isFloating();
    }

    public boolean hasAppointment(int aid) {
        return false;
    }

    public boolean isKing() {
        return false;
    }

    public final boolean isEligibleForKingdomBonus() {
        if (this.hasCustomKingdom()) {
            King king = King.getKing(this.getKingdomId());
            if (king != null) {
                return king.currentLand > 2.0f;
            }
            return false;
        }
        return true;
    }

    public String getAppointmentTitles() {
        return "";
    }

    public boolean isRoyalAnnouncer() {
        return King.isOfficial(1510, this.getWurmId(), this.getKingdomId());
    }

    public boolean isRoyalChef() {
        return King.isOfficial(1509, this.getWurmId(), this.getKingdomId());
    }

    public boolean isRoyalPriest() {
        return King.isOfficial(1506, this.getWurmId(), this.getKingdomId());
    }

    public boolean isRoyalSmith() {
        return King.isOfficial(1503, this.getWurmId(), this.getKingdomId());
    }

    public boolean isRoyalExecutioner() {
        return King.isOfficial(1508, this.getWurmId(), this.getKingdomId());
    }

    public boolean isEconomicAdvisor() {
        return King.isOfficial(1505, this.getWurmId(), this.getKingdomId());
    }

    public boolean isInformationOfficer() {
        return King.isOfficial(1500, this.getWurmId(), this.getKingdomId());
    }

    public String getAnnounceString() {
        return this.getName() + '!';
    }

    public boolean isAppointed() {
        return false;
    }

    public boolean isArcheryMode() {
        return false;
    }

    public MusicPlayer getMusicPlayer() {
        return this.musicPlayer;
    }

    public int getPushCounter() {
        return 200;
    }

    public void setPushCounter(int val) {
    }

    public Seat getSeat() {
        return null;
    }

    public void setMountAction(@Nullable MountAction act) {
        this.mountAction = act;
    }

    public void activePotion(Item potion) {
    }

    public byte getCRCounterBonus() {
        return 0;
    }

    public boolean isNoAttackVehicles() {
        return !this.template.attacksVehicles;
    }

    public int getMaxNumActions() {
        return 10;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (int)(this.id ^ this.id >>> 32);
        result = 31 * result + (this.isPlayer() ? 1231 : 1237);
        return result;
    }

    public void setCheated(String reason) {
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Creature)) {
            return false;
        }
        Creature other = (Creature)obj;
        if (this.id != other.id) {
            return false;
        }
        return this.isPlayer() == other.isPlayer();
    }

    public boolean seesPlayerAssistantWindow() {
        return false;
    }

    public void setHitched(@Nullable Vehicle _hitched, boolean loading) {
        this.hitchedTo = _hitched;
        if (this.hitchedTo != null) {
            this.clearOrders();
            this.seatType = (byte)2;
            if (!loading) {
                this.getStatus().setVehicle(this.hitchedTo.wurmid, this.seatType);
            }
        } else {
            this.seatType = (byte)-1;
            this.getStatus().setVehicle(-10L, this.seatType);
        }
    }

    public Vehicle getHitched() {
        return this.hitchedTo;
    }

    public boolean isPlayerAssistant() {
        return false;
    }

    public boolean isVehicle() {
        return this.template.isVehicle;
    }

    public Set<Long> getRiders() {
        return this.riders;
    }

    public boolean isRidden() {
        return this.riders != null && this.riders.size() > 0;
    }

    public boolean isRiddenBy(long wurmid) {
        return this.riders != null && this.riders.contains(wurmid);
    }

    public void addRider(long newrider) {
        if (this.riders == null) {
            this.riders = new HashSet<Long>();
        }
        this.riders.add(newrider);
    }

    public void removeRider(long lostrider) {
        if (this.riders == null) {
            this.riders = new HashSet<Long>();
        }
        this.riders.remove(lostrider);
    }

    protected void forceMountSpeedChange() {
        this.mountPollCounter = 0;
        this.pollMount();
    }

    private void pollMount() {
        if (this.isRidden()) {
            if (this.mountPollCounter <= 0 || Server.rand.nextInt(100) == 0) {
                Vehicle vehic = Vehicles.getVehicleForId(this.getWurmId());
                if (vehic != null) {
                    try {
                        Creature rider = Server.getInstance().getCreature(vehic.getPilotSeat().occupant);
                        byte val = vehic.calculateNewMountSpeed(this, false);
                        if (this.switchv) {
                            val = (byte)(val - 1);
                        }
                        this.switchv = !this.switchv;
                        rider.getMovementScheme().addMountSpeed(val);
                    }
                    catch (NoSuchCreatureException noSuchCreatureException) {
                    }
                    catch (NoSuchPlayerException noSuchPlayerException) {
                        // empty catch block
                    }
                }
                this.mountPollCounter = 20;
            } else {
                --this.mountPollCounter;
            }
        }
    }

    public boolean mayChangeSpeed() {
        return this.mountPollCounter <= 0;
    }

    public float getMountSpeedPercent(boolean mounting) {
        float factor = 0.5f;
        if (this.getStatus().getHunger() < 45000) {
            factor += 0.2f;
        }
        if (this.getStatus().getHunger() < 10000) {
            factor += 0.1f;
        }
        if (this.getStatus().damage < 10000) {
            factor += 0.1f;
        } else if (this.getStatus().damage > 20000) {
            factor -= 0.5f;
        } else if (this.getStatus().damage > 45000) {
            factor -= 0.7f;
        }
        if (this.isHorse() || this.isUnicorn()) {
            float hbonus = this.calcHorseShoeBonus(mounting);
            this.sendToLoggers("Horse shoe bonus " + hbonus + " so factor from " + factor + " to " + (factor + hbonus));
            factor += hbonus;
        }
        float tperc = this.getTraitMovePercent(mounting);
        this.sendToLoggers("Trait move percent= " + tperc + " so factor from " + factor + " to " + (factor + tperc));
        factor += tperc;
        if (this.getBonusForSpellEffect((byte)22) > 0.0f) {
            factor -= 0.2f * (this.getBonusForSpellEffect((byte)22) / 100.0f);
        }
        if (this.isRidden()) {
            Item torsoItem = this.getWornItem((byte)2);
            if (torsoItem != null && (torsoItem.isSaddleLarge() || torsoItem.isSaddleNormal())) {
                factor += Math.max(10.0f, torsoItem.getCurrentQualityLevel()) / 1000.0f;
                factor += (float)torsoItem.getRarity() * 0.03f;
                factor += torsoItem.getSpellSpeedBonus() / 2000.0f;
                if (!mounting && !this.ignoreSaddleDamage) {
                    torsoItem.setDamage(torsoItem.getDamage() + 0.001f);
                }
                this.ignoreSaddleDamage = false;
            }
            this.sendToLoggers("After saddle move percent= " + factor);
            this.sendToLoggers("After speedModifier " + this.getMovementScheme().getSpeedModifier() + " move percent= " + (factor *= this.getMovementScheme().getSpeedModifier()));
        }
        return factor;
    }

    private int getCarriedMountWeight() {
        int currWeight = this.getCarriedWeight();
        int bagsWeight = this.getSaddleBagsCarriedWeight();
        currWeight -= bagsWeight;
        if (this.isRidden()) {
            for (Long lLong : this.riders) {
                try {
                    Creature _rider = Server.getInstance().getCreature(lLong);
                    currWeight += Math.max(30000, _rider.getStatus().fat * 1000);
                    currWeight += _rider.getCarriedWeight();
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
            }
        }
        return currWeight;
    }

    public boolean hasTraits() {
        return this.status.traits != 0L;
    }

    public boolean hasTrait(int traitbit) {
        if (this.status.traits != 0L) {
            return this.status.isTraitBitSet(traitbit);
        }
        return false;
    }

    public boolean hasAbility(int abilityBit) {
        return false;
    }

    public boolean hasFlag(int flagBit) {
        return false;
    }

    public void setFlag(int number, boolean value) {
    }

    public void setAbility(int number, boolean value) {
    }

    public void setTagItem(long itemId, String itemName) {
    }

    public String getTaggedItemName() {
        return "";
    }

    public long getTaggedItemId() {
        return -10L;
    }

    public boolean removeRandomNegativeTrait() {
        if (this.status.traits != 0L) {
            return this.status.removeRandomNegativeTrait();
        }
        return false;
    }

    private float getTraitMovePercent(boolean mounting) {
        float traitMod;
        block67: {
            int cweight;
            block46: {
                float wmod;
                block65: {
                    block66: {
                        Skill sstrength;
                        boolean moving;
                        block63: {
                            block64: {
                                block61: {
                                    block62: {
                                        block59: {
                                            block60: {
                                                block57: {
                                                    block58: {
                                                        block55: {
                                                            block56: {
                                                                block53: {
                                                                    block54: {
                                                                        block51: {
                                                                            block52: {
                                                                                block49: {
                                                                                    block50: {
                                                                                        block47: {
                                                                                            block48: {
                                                                                                traitMod = 0.0f;
                                                                                                Creature r = null;
                                                                                                moving = false;
                                                                                                if (this.isRidden() && this.getMountVehicle() != null) {
                                                                                                    try {
                                                                                                        r = Server.getInstance().getCreature(this.getMountVehicle().pilotId);
                                                                                                        moving = r.isMoving();
                                                                                                    }
                                                                                                    catch (NoSuchCreatureException noSuchCreatureException) {
                                                                                                    }
                                                                                                    catch (NoSuchPlayerException noSuchPlayerException) {
                                                                                                        // empty catch block
                                                                                                    }
                                                                                                }
                                                                                                cweight = this.getCarriedMountWeight();
                                                                                                if (mounting || this.status.traits == 0L) break block46;
                                                                                                sstrength = this.getSoulStrength();
                                                                                                if (!this.status.isTraitBitSet(1)) break block47;
                                                                                                if (!this.isHorse()) break block48;
                                                                                                boolean bl = !moving;
                                                                                                if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block47;
                                                                                            }
                                                                                            traitMod += 0.1f;
                                                                                        }
                                                                                        if (this.status.isTraitBitSet(15) || this.status.isTraitBitSet(16) || this.status.isTraitBitSet(17) || this.status.isTraitBitSet(18) || this.status.isTraitBitSet(24) || this.status.isTraitBitSet(25) || !this.status.isTraitBitSet(23)) break block49;
                                                                                        if (!this.isHorse()) break block50;
                                                                                        boolean bl = !moving;
                                                                                        if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block49;
                                                                                    }
                                                                                    traitMod += 0.025f;
                                                                                }
                                                                                if (!this.status.isTraitBitSet(4)) break block51;
                                                                                if (!this.isHorse()) break block52;
                                                                                boolean bl = !moving;
                                                                                if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block51;
                                                                            }
                                                                            traitMod += 0.2f;
                                                                        }
                                                                        if (!this.status.isTraitBitSet(8)) break block53;
                                                                        if (!this.isHorse()) break block54;
                                                                        boolean bl = !moving;
                                                                        if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) < 0.0)) break block53;
                                                                    }
                                                                    traitMod -= 0.1f;
                                                                }
                                                                if (!this.status.isTraitBitSet(9)) break block55;
                                                                if (!this.isHorse()) break block56;
                                                                boolean bl = !moving;
                                                                if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) < 0.0)) break block55;
                                                            }
                                                            traitMod -= 0.3f;
                                                        }
                                                        if (!this.status.isTraitBitSet(6)) break block57;
                                                        if (!this.isHorse()) break block58;
                                                        boolean bl = !moving;
                                                        if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block57;
                                                    }
                                                    traitMod += 0.1f;
                                                }
                                                wmod = 0.0f;
                                                if (!this.status.isTraitBitSet(3)) break block59;
                                                if (!this.isHorse()) break block60;
                                                boolean bl = !moving;
                                                if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block59;
                                            }
                                            wmod += 10000.0f;
                                        }
                                        if (!this.status.isTraitBitSet(5)) break block61;
                                        if (!this.isHorse()) break block62;
                                        boolean bl = !moving;
                                        if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block61;
                                    }
                                    wmod += 20000.0f;
                                }
                                if (!this.status.isTraitBitSet(11)) break block63;
                                if (!this.isHorse()) break block64;
                                boolean bl = !moving;
                                if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) < 0.0)) break block63;
                            }
                            wmod -= 30000.0f;
                        }
                        if (!this.status.isTraitBitSet(6)) break block65;
                        if (!this.isHorse()) break block66;
                        boolean bl = !moving;
                        if (!(sstrength.skillCheck(20.0, 0.0, bl, 1.0f) > 0.0)) break block65;
                    }
                    wmod += 10000.0f;
                }
                if ((double)cweight > this.getStrengthSkill() * 5000.0 + (double)wmod) {
                    traitMod = (float)((double)traitMod - 0.15 * ((double)cweight - this.getStrengthSkill() * 5000.0 - (double)wmod) / 50000.0);
                }
                break block67;
            }
            if ((double)cweight > this.getStrengthSkill() * 5000.0) {
                traitMod = (float)((double)traitMod - 0.15 * ((double)cweight - this.getStrengthSkill() * 5000.0) / 50000.0);
            }
        }
        return traitMod;
    }

    public boolean isHorse() {
        return this.template.isHorse;
    }

    public boolean isUnicorn() {
        return this.template.isUnicorn();
    }

    public boolean cantRideUntame() {
        return this.template.cantRideUntamed();
    }

    public static void setRandomColor(Creature creature) {
        if (Server.rand.nextInt(3) == 0) {
            creature.getStatus().setTraitBit(15, true);
        } else if (Server.rand.nextInt(3) == 0) {
            creature.getStatus().setTraitBit(16, true);
        } else if (Server.rand.nextInt(3) == 0) {
            creature.getStatus().setTraitBit(17, true);
        } else if (Server.rand.nextInt(3) == 0) {
            creature.getStatus().setTraitBit(18, true);
        } else if (Server.rand.nextInt(6) == 0) {
            creature.getStatus().setTraitBit(24, true);
        } else if (Server.rand.nextInt(12) == 0) {
            creature.getStatus().setTraitBit(25, true);
        } else if (Server.rand.nextInt(24) == 0) {
            creature.getStatus().setTraitBit(23, true);
        } else if (creature.getTemplate().maxColourCount > 8) {
            if (Server.rand.nextInt(6) == 0) {
                creature.getStatus().setTraitBit(30, true);
            } else if (Server.rand.nextInt(6) == 0) {
                creature.getStatus().setTraitBit(31, true);
            } else if (Server.rand.nextInt(6) == 0) {
                creature.getStatus().setTraitBit(32, true);
            } else if (Server.rand.nextInt(6) == 0) {
                creature.getStatus().setTraitBit(33, true);
            } else if (Server.rand.nextInt(6) == 0) {
                creature.getStatus().setTraitBit(34, true);
            }
        }
    }

    public boolean mayMate(Creature potentialMate) {
        if (this.isDead() || potentialMate.isDead()) {
            return false;
        }
        if (potentialMate.getTemplate().getMateTemplateId() == this.template.getTemplateId() || this.template.getTemplateId() == 96 && potentialMate.getTemplate().getTemplateId() == 96) {
            if (this.template.getAdultFemaleTemplateId() != -1 || this.template.getAdultMaleTemplateId() != -1) {
                return false;
            }
            if (potentialMate.getSex() != this.getSex() && potentialMate.getWurmId() != this.getWurmId()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkBreedingPossibility() {
        Creature[] crets = this.getCurrentTile().getCreatures();
        if (!(this.isKingdomGuard() || this.isGhost() || this.isHuman() || crets.length <= 0 || !this.mayMate(crets[0]) || crets[0].isPregnant() || this.isPregnant())) {
            try {
                BehaviourDispatcher.action(this, this.getCommunicator(), -1L, crets[0].getWurmId(), (short)379);
                return true;
            }
            catch (Exception ex) {
                return false;
            }
        }
        return false;
    }

    public boolean isInTheMoodToBreed(boolean forced) {
        if (this.getStatus().getHunger() > 10000) {
            return false;
        }
        if (this.template.getAdultFemaleTemplateId() != -1 || this.template.getAdultMaleTemplateId() != -1) {
            return false;
        }
        if (this.getStatus().age <= 3) {
            return false;
        }
        return this.breedCounter == 0 || forced && !this.forcedBreed;
    }

    public int getBreedCounter() {
        return this.breedCounter;
    }

    public void resetBreedCounter() {
        this.forcedBreed = true;
        this.breedCounter = (Servers.isThisAPvpServer() ? 900 : 2000) + Server.rand.nextInt(Math.max(1000, 100 * Math.abs(20 - this.getStatus().age)));
    }

    public long getMother() {
        return this.status.mother;
    }

    public long getFather() {
        return this.status.father;
    }

    public int getMeditateX() {
        return 0;
    }

    public int getMeditateY() {
        return 0;
    }

    public void setMeditateX(int tilex) {
    }

    public void setMeditateY(int tiley) {
    }

    public void setDisease(byte newDisease) {
        boolean changed = false;
        if (this.getStatus().disease > 0 && newDisease <= 0) {
            if (this.getPower() < 2) {
                this.setVisible(false);
            }
            changed = true;
            this.getCommunicator().sendSafeServerMessage("You feel a lot better now as your disease is gone.", (byte)2);
            if (this.isPlayer()) {
                this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DISEASE);
            }
        } else if (this.isPlayer() && newDisease > 0) {
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DISEASE, 100000, this.getStatus().disease);
            this.achievement(173);
        }
        if (this.getStatus().disease == 0 && newDisease == 1) {
            if (this.isUnique() || this.isKingdomGuard() || this.isGhost() || this.status.modtype == 11) {
                return;
            }
            if (this.getPower() < 2) {
                this.setVisible(false);
            }
            changed = true;
            this.getCommunicator().sendAlertServerMessage("You scratch yourself. What did you catch now?", (byte)2);
            this.achievement(568);
        }
        this.getStatus().setDisease(newDisease);
        if (changed && this.getPower() < 2) {
            this.setVisible(true);
        }
    }

    public byte getDisease() {
        return this.getStatus().disease;
    }

    public long getLastGroomed() {
        return this.getStatus().lastGroomed;
    }

    public void setLastGroomed(long newLastGroomed) {
        this.getStatus().setLastGroomed(newLastGroomed);
    }

    public boolean canBeGroomed() {
        return System.currentTimeMillis() - this.getLastGroomed() > 3600000L;
    }

    public boolean isDomestic() {
        return this.template.domestic;
    }

    public void setLastKingdom() {
    }

    public void addLink(Creature creature) {
    }

    public boolean isLinked() {
        return this.linkedTo != -10L;
    }

    public Creature getCreatureLinkedTo() {
        try {
            return Server.getInstance().getCreature(this.linkedTo);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public void removeLink(long wurmid) {
    }

    public int getNumLinks() {
        return 0;
    }

    public Creature[] getLinks() {
        return emptyCreatures;
    }

    public void clearLinks() {
    }

    public void setLinkedTo(long wid, boolean linkback) {
        this.linkedTo = wid;
    }

    public void disableLink() {
        this.setLinkedTo(-10L, true);
    }

    public void setFatigue(int fatigueToAdd) {
    }

    public boolean isMissionairy() {
        return true;
    }

    public long getLastChangedPriestType() {
        return 0L;
    }

    public void setPriestType(byte type) {
    }

    public void setPrayerSeconds(int prayerSeconds) {
    }

    public long getLastChangedJoat() {
        return 0L;
    }

    public void resetJoat() {
    }

    public Team getTeam() {
        return null;
    }

    public void setTeam(@Nullable Team newTeam, boolean sendRemove) {
    }

    public boolean isTeamLeader() {
        return false;
    }

    public boolean mayInviteTeam() {
        return false;
    }

    public void setMayInviteTeam(boolean mayInvite) {
    }

    public void sendSystemMessage(String message) {
    }

    public void sendHelpMessage(String message) {
    }

    public void makeEmoteSound() {
    }

    public void poisonChanged(boolean hadPoison, Wound w) {
        if (hadPoison) {
            if (!this.isPoisoned()) {
                this.getCommunicator().sendRemoveSpellEffect(w.getWurmId(), SpellEffectsEnum.POISON);
                this.hasSentPoison = false;
            }
        } else if (!this.hasSentPoison) {
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POISON.createId(w.getWurmId()), SpellEffectsEnum.POISON, 100000, w.getPoisonSeverity());
            this.hasSentPoison = true;
        }
    }

    public final void sendAllPoisonEffect() {
        Wounds w = this.getBody().getWounds();
        if (w != null && w.getWounds() != null) {
            Wound[] warr = w.getWounds();
            for (int a = 0; a < warr.length; ++a) {
                if (!warr[a].isPoison()) continue;
                this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POISON.createId(warr[a].getWurmId()), SpellEffectsEnum.POISON, 100000, warr[a].getPoisonSeverity());
                this.hasSentPoison = true;
            }
        }
    }

    public final boolean isPoisoned() {
        Wounds w = this.getBody().getWounds();
        if (w != null && w.getWounds() != null) {
            Wound[] warr = w.getWounds();
            for (int a = 0; a < warr.length; ++a) {
                if (!warr[a].isPoison()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean mayEmote() {
        return false;
    }

    public boolean hasSkillGain() {
        return true;
    }

    public boolean setHasSkillGain(boolean hasSkillGain) {
        return true;
    }

    public long getChampTimeStamp() {
        return 0L;
    }

    public void becomeChamp() {
    }

    public void revertChamp() {
    }

    public long getLastChangedCluster() {
        return 0L;
    }

    public void setLastChangedCluster() {
    }

    public boolean isInTheNorthWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3 && this.getTileY() < Zones.worldTileSizeY / 3;
    }

    public boolean isInTheNorth() {
        return this.getTileY() < Zones.worldTileSizeX / 3;
    }

    public boolean isInTheNorthEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3 && this.getTileY() < Zones.worldTileSizeY / 3;
    }

    public boolean isInTheEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3;
    }

    public boolean isInTheSouthEast() {
        return this.getTileX() > Zones.worldTileSizeX - Zones.worldTileSizeX / 3 && this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheSouth() {
        return this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheSouthWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3 && this.getTileY() > Zones.worldTileSizeY - Zones.worldTileSizeY / 3;
    }

    public boolean isInTheWest() {
        return this.getTileX() < Zones.worldTileSizeX / 3;
    }

    public int getGlobalMapPlacement() {
        if (this.isInTheNorthWest()) {
            return 7;
        }
        if (this.isInTheNorthEast()) {
            return 1;
        }
        if (this.isInTheSouthEast()) {
            return 3;
        }
        if (this.isInTheSouthWest()) {
            return 5;
        }
        if (this.isInTheNorth()) {
            return 0;
        }
        if (this.isInTheEast()) {
            return 2;
        }
        if (this.isInTheSouth()) {
            return 4;
        }
        if (this.isInTheWest()) {
            return 6;
        }
        return -1;
    }

    public boolean mayDestroy(Item item) {
        VolaTile t;
        if (item.isDestroyable(this.getWurmId())) {
            return true;
        }
        if (item.isOwnerDestroyable() && !item.isLocked()) {
            Village village = Zones.getVillage(item.getTilePos(), item.isOnSurface());
            if (village != null) {
                return village.isActionAllowed((short)83, this);
            }
            if (item.isUnfinished()) {
                if (item.getRealTemplate() != null && item.getRealTemplate().isKingdomMarker() && this.getKingdomId() != item.getAuxData()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        if (item.isEnchantedTurret() && (t = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface())) != null && t.getVillage() != null && t.getVillage().isPermanent && t.getVillage().kingdom == item.getKingdom()) {
            return false;
        }
        return false;
    }

    public boolean isCaredFor() {
        if (this.isUnique() || this.onlyAttacksPlayers()) {
            return false;
        }
        return Creatures.getInstance().isCreatureProtected(this.getWurmId());
    }

    public long getCareTakerId() {
        return Creatures.getInstance().getCreatureProtectorFor(this.getWurmId());
    }

    public boolean isCaredFor(Player player) {
        return this.getCareTakerId() == player.getWurmId();
    }

    public boolean isBrandedBy(int villageId) {
        Village bVill = this.getBrandVillage();
        return bVill != null && bVill.getId() == villageId;
    }

    public boolean isBranded() {
        Village bVill = this.getBrandVillage();
        return bVill != null;
    }

    public boolean isOnDeed() {
        Village bVill = this.getBrandVillage();
        if (bVill == null) {
            return false;
        }
        Village pVill = Villages.getVillage(this.getTileX(), this.getTileY(), true);
        return pVill != null && bVill.getId() == pVill.getId();
    }

    public boolean isHitched() {
        return this.getHitched() != null;
    }

    public int getNumberOfPossibleCreatureTakenCareOf() {
        return 0;
    }

    public final void setHasSpiritStamina(boolean hasStaminaGain) {
        this.hasSpiritStamina = hasStaminaGain;
    }

    public void setHasSpiritFavorgain(boolean hasFavorGain) {
    }

    public void setHasSpiritFervor(boolean hasSpiritFervor) {
    }

    public boolean mayUseLastGasp() {
        return false;
    }

    public void useLastGasp() {
    }

    public boolean isUsingLastGasp() {
        return false;
    }

    public final float addToWeaponUsed(Item weapon, float time) {
        Float ftime = this.weaponsUsed.get(weapon);
        ftime = ftime == null ? Float.valueOf(time) : Float.valueOf(ftime.floatValue() + time);
        this.weaponsUsed.put(weapon, ftime);
        return ftime.floatValue();
    }

    public final UsedAttackData addToAttackUsed(AttackAction act, float time, int rounds) {
        UsedAttackData data = this.attackUsed.get(act);
        if (data == null) {
            data = new UsedAttackData(time, rounds);
        } else {
            data.setTime(data.getTime() + time);
            data.setRounds(data.getRounds() + rounds);
        }
        this.attackUsed.put(act, data);
        return data;
    }

    public final void updateAttacksUsed(float time) {
        for (AttackAction key : this.attackUsed.keySet()) {
            UsedAttackData data = this.attackUsed.get(key);
            if (data == null) continue;
            data.update(data.getTime() - time);
        }
    }

    public final UsedAttackData getUsedAttackData(AttackAction act) {
        return this.attackUsed.get(act);
    }

    public final float deductFromWeaponUsed(Item weapon, float swingTime) {
        Float ftime = this.weaponsUsed.get(weapon);
        if (ftime == null) {
            ftime = Float.valueOf(swingTime);
        }
        while (ftime.floatValue() >= swingTime) {
            ftime = Float.valueOf(ftime.floatValue() - swingTime);
        }
        this.weaponsUsed.put(weapon, ftime);
        return ftime.floatValue();
    }

    public final void resetWeaponsUsed() {
        this.weaponsUsed.clear();
    }

    public final void resetAttackUsed() {
        this.attackUsed.clear();
        if (this.combatHandler != null) {
            this.combatHandler.resetSecAttacks();
        }
    }

    public byte getFightlevel() {
        if (this.fightlevel < 0) {
            this.fightlevel = 0;
        }
        if (this.fightlevel > 5) {
            this.fightlevel = (byte)5;
        }
        return this.fightlevel;
    }

    public String getFightlevelString() {
        int fl = this.getFightlevel();
        if (fl < 0) {
            fl = 0;
        }
        if (fl >= 5) {
            fl = 5;
        }
        return Attack.focusStrings[fl];
    }

    public void increaseFightlevel(int delta) {
        this.fightlevel = (byte)(this.fightlevel + delta);
        if (this.fightlevel > 5) {
            this.fightlevel = (byte)5;
        }
        if (this.fightlevel < 0) {
            this.fightlevel = 0;
        }
    }

    public void setKickedOffBoat(boolean kicked) {
    }

    public boolean wasKickedOffBoat() {
        return false;
    }

    public boolean isOnPermaReputationGrounds() {
        if (this.currentVillage != null && this.currentVillage.getReputationObject(this.getWurmId()) != null) {
            return this.currentVillage.getReputationObject(this.getWurmId()).isPermanent();
        }
        return false;
    }

    public boolean hasFingerEffect() {
        return false;
    }

    public void setHasFingerEffect(boolean eff) {
    }

    public void sendHasFingerEffect() {
    }

    public boolean hasFingerOfFoBonus() {
        return false;
    }

    public void setHasCrownEffect(boolean eff) {
    }

    public void sendHasCrownEffect() {
    }

    public void setCrownInfluence(int influence) {
    }

    public boolean hasCrownInfluence() {
        return false;
    }

    public int getEpicServerId() {
        return -1;
    }

    public byte getEpicServerKingdom() {
        return 0;
    }

    public final boolean attackingIntoIllegalDuellingRing(int targetX, int targetY, boolean surfaced) {
        if (surfaced) {
            Item ring1 = Zones.isWithinDuelRing(this.getCurrentTile().getTileX(), this.getCurrentTile().getTileY(), this.getCurrentTile().isOnSurface());
            Item ring = Zones.isWithinDuelRing(targetX, targetY, surfaced);
            if (ring != ring1) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasSpellEffect(byte spellEffect) {
        if (this.getSpellEffects() != null) {
            return this.getSpellEffects().getSpellEffect(spellEffect) != null;
        }
        return false;
    }

    public final void reduceStoneSkin() {
        SpellEffect sk;
        if (this.getSpellEffects() != null && (sk = this.getSpellEffects().getSpellEffect((byte)68)) != null) {
            if (sk.getPower() > 34.0f) {
                sk.setPower(sk.getPower() - 34.0f);
            } else {
                this.getSpellEffects().removeSpellEffect(sk);
            }
        }
    }

    public final void removeTrueStrike() {
        SpellEffect sk;
        if (this.getSpellEffects() != null && (sk = this.getSpellEffects().getSpellEffect((byte)67)) != null) {
            this.getSpellEffects().removeSpellEffect(sk);
        }
    }

    public final boolean addWoundOfType(@Nullable Creature attacker, byte woundType, int pos, boolean randomizePos, float armourMod, boolean calculateArmour, double damage, float infection, float poison, boolean noMinimumDamage, boolean spell) {
        if ((woundType == 8 || woundType == 4 || woundType == 10) && this.getCultist() != null && this.getCultist().hasNoElementalDamage()) {
            return false;
        }
        if (this.hasSpellEffect((byte)69)) {
            damage *= (double)0.8f;
        }
        try {
            if (randomizePos) {
                pos = this.getBody().getRandomWoundPos();
            }
            if (calculateArmour && ((armourMod = this.getArmourMod()) == 1.0f || this.isVehicle() || this.isKingdomGuard())) {
                try {
                    byte protectionSlot = ArmourTemplate.getArmourPosition((byte)pos);
                    Item armour = this.getArmour(protectionSlot);
                    armourMod = !this.isKingdomGuard() ? ArmourTemplate.calculateDR(armour, woundType) : (armourMod *= ArmourTemplate.calculateDR(armour, woundType));
                    armour.setDamage((float)((double)armour.getDamage() + damage * (double)armourMod / 30000.0 * (double)armour.getDamageModifier() * (double)ArmourTemplate.getArmourDamageModFor(armour, woundType)));
                    if (this.getBonusForSpellEffect((byte)22) > 0.0f) {
                        armourMod = armourMod >= 1.0f ? 0.2f + (1.0f - this.getBonusForSpellEffect((byte)22) / 100.0f) * 0.6f : Math.min(armourMod, 0.2f + (1.0f - this.getBonusForSpellEffect((byte)22) / 100.0f) * 0.6f);
                    }
                }
                catch (NoArmourException protectionSlot) {
                    // empty catch block
                }
            }
            if (pos == 1 || pos == 29) {
                damage *= (double)ItemBonus.getFaceDamReductionBonus(this);
            }
            damage *= (double)Wound.getResistModifier(attacker, this, woundType);
            if (woundType == 8) {
                return CombatEngine.addColdWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 7) {
                return CombatEngine.addDrownWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 9) {
                return CombatEngine.addInternalWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 10) {
                return CombatEngine.addAcidWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 4) {
                return CombatEngine.addFireWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 6) {
                return CombatEngine.addRotWound(attacker, this, pos, damage, armourMod, infection, poison, noMinimumDamage, spell);
            }
            if (woundType == 5) {
                return CombatEngine.addWound(attacker, this, woundType, pos, damage, armourMod, "poison", null, infection, poison, false, true, noMinimumDamage, spell);
            }
            return CombatEngine.addWound(attacker, this, woundType, pos, damage, armourMod, "hit", null, infection, poison, false, true, noMinimumDamage, spell);
        }
        catch (NoSpaceException nsp) {
            logger.log(Level.WARNING, this.getName() + " no armour space on loc " + pos);
        }
        catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return false;
    }

    public float addSpellResistance(short spellId) {
        return 1.0f;
    }

    public SpellResistance getSpellResistance(short spellId) {
        return null;
    }

    public final boolean isInPvPZone() {
        if (this.isInNonPvPZone) {
            return false;
        }
        return this.isInPvPZone;
    }

    public final boolean isOnPvPServer() {
        if (this.isInNonPvPZone) {
            return false;
        }
        if (Servers.localServer.PVPSERVER) {
            return true;
        }
        if (this.isInPvPZone) {
            return true;
        }
        return this.isInDuelRing;
    }

    public short getHotaWins() {
        return 0;
    }

    public void setHotaWins(short wins) {
    }

    public void setVehicleCommander(boolean isCommander) {
        this.isVehicleCommander = isCommander;
    }

    public long getFace() {
        return 0L;
    }

    public byte getRarity() {
        return 0;
    }

    public byte getRarityShader() {
        if (this.getBonusForSpellEffect((byte)22) > 70.0f) {
            return 2;
        }
        if (this.getBonusForSpellEffect((byte)22) > 0.0f) {
            return 1;
        }
        return 0;
    }

    public void achievement(int achievementId) {
    }

    public void addTitle(Titles.Title title) {
    }

    public void removeTitle(Titles.Title title) {
    }

    public void achievement(int achievementId, int counterModifier) {
    }

    protected void addTileMovedDragging() {
    }

    protected void addTileMovedRiding() {
    }

    protected void addTileMoved() {
    }

    protected void addTileMovedDriving() {
    }

    protected void addTileMovedPassenger() {
    }

    public int getKarma() {
        if (this.isSpellCaster() || this.isSummoner()) {
            return 10000;
        }
        return 0;
    }

    public void setKarma(int newKarma) {
    }

    public void modifyKarma(int points) {
    }

    public long getTimeToSummonCorpse() {
        return 0L;
    }

    public boolean maySummonCorpse() {
        return false;
    }

    public final void pushToFloorLevel(int floorLevel) {
        try {
            if (!this.isPlayer()) {
                float oldposz = this.getPositionZ();
                float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()) + (float)(floorLevel * 3) + 0.25f;
                float diffz = newPosz - oldposz;
                this.getStatus().setPositionZ(newPosz, true);
                if (this.currentTile != null && this.getVisionArea() != null) {
                    this.moved(0.0f, 0.0f, diffz, 0, 0);
                }
            }
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
    }

    public final float calculatePosZ() {
        return Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), this.isFloating(), this.getPositionZ(), this, this.getBridgeId());
    }

    public final boolean canOpenDoors() {
        return this.template.canOpenDoors();
    }

    public final int getFloorLevel(boolean ignoreVehicleOffset) {
        try {
            long vehicleId;
            float vehicleOffsetToRemove = 0.0f;
            if (ignoreVehicleOffset && (vehicleId = this.getVehicle()) != -10L) {
                Vehicle vehicle = Vehicles.getVehicleForId(vehicleId);
                if (vehicle == null) {
                    logger.log(Level.WARNING, "Unknown vehicle for id: " + vehicleId + " resulting in possinly incorrect floor level!");
                } else {
                    Seat seat = vehicle.getSeatFor(this.id);
                    if (seat == null) {
                        logger.log(Level.WARNING, "Unable to find the seat the player: " + this.id + " supposedly is on, Vehicle id: " + vehicleId + ". Resulting in possibly incorrect floor level calculation.");
                    } else {
                        vehicleOffsetToRemove = Math.max(this.getAltOffZ(), seat.offz);
                    }
                }
            }
            float playerPosZ = this.getPositionZ() + this.getAltOffZ();
            float groundHeight = Math.max(0.0f, Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()));
            float posZ = Math.max(0.0f, (playerPosZ - groundHeight - vehicleOffsetToRemove + 0.5f) * 10.0f);
            return (int)posZ / 30;
        }
        catch (NoSuchZoneException snz) {
            return 0;
        }
    }

    public final int getFloorLevel() {
        return this.getFloorLevel(false);
    }

    public boolean fireTileLog() {
        return false;
    }

    public void sendActionControl(String actionString, boolean start, int timeLeft) {
    }

    public byte getBlood() {
        return 0;
    }

    public Shop getShop() {
        return Economy.getEconomy().getShop(this);
    }

    public void setScenarioKarma(int newKarma) {
    }

    public int getScenarioKarma() {
        return 0;
    }

    public boolean knowsKarmaSpell(int karmaSpellActionNum) {
        return this.isSpellCaster() || this.isSummoner();
    }

    public float getFireResistance() {
        return this.template.fireResistance;
    }

    public boolean checkCoinAward(int chance) {
        return false;
    }

    public float getColdResistance() {
        return this.template.coldResistance;
    }

    public float getDiseaseResistance() {
        return this.template.diseaseResistance;
    }

    public float getPhysicalResistance() {
        return this.template.physicalResistance;
    }

    public float getPierceResistance() {
        return this.template.pierceResistance;
    }

    public float getSlashResistance() {
        return this.template.slashResistance;
    }

    public float getCrushResistance() {
        return this.template.crushResistance;
    }

    public float getBiteResistance() {
        return this.template.biteResistance;
    }

    public float getPoisonResistance() {
        return this.template.poisonResistance;
    }

    public float getWaterResistance() {
        return this.template.waterResistance;
    }

    public float getAcidResistance() {
        return this.template.acidResistance;
    }

    public float getInternalResistance() {
        return this.template.internalResistance;
    }

    public float getFireVulnerability() {
        return this.template.fireVulnerability;
    }

    public float getColdVulnerability() {
        return this.template.coldVulnerability;
    }

    public float getDiseaseVulnerability() {
        return this.template.diseaseVulnerability;
    }

    public float getPhysicalVulnerability() {
        return this.template.physicalVulnerability;
    }

    public float getPierceVulnerability() {
        return this.template.pierceVulnerability;
    }

    public float getSlashVulnerability() {
        return this.template.slashVulnerability;
    }

    public float getCrushVulnerability() {
        return this.template.crushVulnerability;
    }

    public float getBiteVulnerability() {
        return this.template.biteVulnerability;
    }

    public float getPoisonVulnerability() {
        return this.template.poisonVulnerability;
    }

    public float getWaterVulnerability() {
        return this.template.waterVulnerability;
    }

    public float getAcidVulnerability() {
        return this.template.acidVulnerability;
    }

    public float getInternalVulnerability() {
        return this.template.internalVulnerability;
    }

    public boolean hasAnyAbility() {
        return false;
    }

    public static final Set<MovementEntity> getIllusionsFor(long wurmid) {
        return illusions.get(wurmid);
    }

    public static final long getWurmIdForIllusion(long illusionId) {
        for (Set<MovementEntity> set : illusions.values()) {
            for (MovementEntity entity : set) {
                if (entity.getWurmid() != illusionId) continue;
                return entity.getCreatorId();
            }
        }
        return -10L;
    }

    public void addIllusion(MovementEntity entity) {
        Set<MovementEntity> entities = illusions.get(this.getWurmId());
        if (entities == null) {
            entities = new HashSet<MovementEntity>();
            illusions.put(this.getWurmId(), entities);
        }
        entities.add(entity);
    }

    public boolean isUndead() {
        return false;
    }

    public byte getUndeadType() {
        return 0;
    }

    public String getUndeadTitle() {
        return "";
    }

    public final void setBridgeId(long bid) {
        this.setBridgeId(bid, true);
    }

    public final void setBridgeId(long bid, boolean sendToSelf) {
        this.status.getPosition().setBridgeId(bid);
        if (this.getMovementScheme() != null) {
            this.getMovementScheme().setBridgeId(bid);
        }
        if (this.getCurrentTile() != null) {
            this.getCurrentTile().sendSetBridgeId(this, bid, sendToSelf);
        }
    }

    public long getMoneyEarnedBySellingLastHour() {
        return 0L;
    }

    public void addMoneyEarnedBySellingLastHour(long money) {
    }

    public void setModelName(String newModelName) {
    }

    public final void calcBattleCampBonus() {
        Item closest = null;
        for (FocusZone fz : FocusZone.getZonesAt(this.getTileX(), this.getTileY())) {
            if (!fz.isBattleCamp()) continue;
            for (Item wartarget : Items.getWarTargets()) {
                if (closest != null && !(Creature.getRange(this, wartarget.getPosX(), wartarget.getPosY()) < Creature.getRange(this, closest.getPosX(), closest.getPosY()))) continue;
                closest = wartarget;
            }
        }
        if (closest != null) {
            this.isInOwnBattleCamp = closest.getKingdom() == this.getKingdomId();
        }
        this.isInOwnBattleCamp = false;
        logger.log(Level.INFO, this.getName() + " set battle camp bonus to " + this.isInOwnBattleCamp);
    }

    public final boolean hasBattleCampBonus() {
        return this.isInOwnBattleCamp;
    }

    public boolean isVisibleToPlayers() {
        return this.visibleToPlayers;
    }

    public void setVisibleToPlayers(boolean aVisibleToPlayers) {
        this.visibleToPlayers = aVisibleToPlayers;
    }

    public boolean isDoLavaDamage() {
        return this.doLavaDamage;
    }

    public void setDoLavaDamage(boolean aDoLavaDamage) {
        this.doLavaDamage = aDoLavaDamage;
    }

    public final boolean doLavaDamage() {
        this.setDoLavaDamage(false);
        if (!(this.isInvulnerable() || this.isGhost() || this.isUnique() || this.getDeity() != null && this.getDeity().isMountainGod() && !(this.getFaith() < 35.0f) || this.getFarwalkerSeconds() > 0)) {
            Wound wound = null;
            boolean dead = false;
            try {
                byte pos = this.getBody().getRandomWoundPos((byte)10);
                if (Server.rand.nextInt(10) <= 6 && this.getBody().getWounds() != null && (wound = this.getBody().getWounds().getWoundAtLocation(pos)) != null) {
                    dead = wound.modifySeverity((int)(5000.0f + (float)Server.rand.nextInt(5000) * (100.0f - this.getSpellDamageProtectBonus()) / 100.0f));
                    wound.setBandaged(false);
                    this.setWounded();
                }
                if (!(wound != null || this.isGhost() || this.isUnique() || this.isKingdomGuard())) {
                    dead = this.addWoundOfType(null, (byte)4, pos, false, 1.0f, true, 5000.0f + (float)Server.rand.nextInt(5000) * (100.0f - this.getSpellDamageProtectBonus()) / 100.0f, 0.0f, 0.0f, false, false);
                }
                this.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
                if (dead) {
                    this.achievement(142);
                    return true;
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, this.getName() + " " + ex.getMessage(), ex);
            }
        }
        return false;
    }

    public boolean isDoAreaDamage() {
        return this.doAreaDamage;
    }

    public void setDoAreaEffect(boolean aDoAreaDamage) {
        this.doAreaDamage = aDoAreaDamage;
    }

    public byte getPathfindCounter() {
        return this.pathfindcounter;
    }

    public void setPathfindcounter(int i) {
        this.pathfindcounter = (byte)i;
    }

    public int getHugeMoveCounter() {
        return this.hugeMoveCounter;
    }

    public void setHugeMoveCounter(int aHugeMoveCounter) {
        this.hugeMoveCounter = Math.max(0, aHugeMoveCounter);
    }

    public void setArmourLimitingFactor(float factor, boolean initializing) {
    }

    public float getArmourLimitingFactor() {
        return 0.0f;
    }

    public void recalcLimitingFactor(Item currentItem) {
    }

    public final float getAltOffZ() {
        Seat s;
        Vehicle vehic;
        if (this.getVehicle() != -10L && (vehic = Vehicles.getVehicleForId(this.getVehicle())) != null && (s = vehic.getSeatFor(this.getWurmId())) != null) {
            return s.getAltOffz();
        }
        return 0.0f;
    }

    public final boolean followsGround() {
        return this.getBridgeId() == -10L && (!this.isPlayer() || this.getMovementScheme().onGround) && this.getFloorLevel() == 0;
    }

    public final boolean isWagoner() {
        return this.template.getTemplateId() == 114;
    }

    public final boolean isFish() {
        return this.template.getTemplateId() == 119;
    }

    @Nullable
    public final Wagoner getWagoner() {
        if (this.isWagoner()) {
            return Wagoner.getWagoner(this.id);
        }
        return null;
    }

    @Override
    public String getTypeName() {
        return this.getTemplate().getName();
    }

    @Override
    public String getObjectName() {
        if (this.isWagoner()) {
            return this.getName();
        }
        return this.petName;
    }

    @Override
    public boolean setObjectName(String aNewName, Creature aCreature) {
        this.setVisible(false);
        this.setPetName(aNewName);
        this.setVisible(true);
        this.status.setChanged(true);
        return true;
    }

    @Override
    public boolean isActualOwner(long playerId) {
        return false;
    }

    @Override
    public boolean isOwner(Creature creature) {
        return this.isOwner(creature.getWurmId());
    }

    @Override
    public boolean isOwner(long playerId) {
        if (this.isWagoner()) {
            Wagoner wagoner = this.getWagoner();
            if (wagoner != null) {
                return wagoner.getOwnerId() == playerId;
            }
            return false;
        }
        Village bVill = this.getBrandVillage();
        return bVill != null && bVill.isMayor(playerId);
    }

    @Override
    public boolean canChangeOwner(Creature creature) {
        return false;
    }

    @Override
    public boolean canChangeName(Creature creature) {
        if (this.isWagoner()) {
            return false;
        }
        if (creature.getPower() > 1) {
            return true;
        }
        Village bVill = this.getBrandVillage();
        if (bVill == null) {
            return false;
        }
        return bVill.isMayor(creature);
    }

    @Override
    public boolean setNewOwner(long playerId) {
        if (this.isWagoner()) {
            Wagoner wagoner = this.getWagoner();
            if (wagoner != null) {
                wagoner.setOwnerId(playerId);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public String getOwnerName() {
        return "";
    }

    @Override
    public String getWarning() {
        if (this.isWagoner()) {
            return "";
        }
        Village bVill = this.getBrandVillage();
        if (bVill == null) {
            return "NEEDS TO BE BRANDED FOR PERMISSIONS TO WORK";
        }
        return "";
    }

    @Override
    public PermissionsPlayerList getPermissionsPlayerList() {
        return AnimalSettings.getPermissionsPlayerList(this.getWurmId());
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean isManageEnabled(Player player) {
        return false;
    }

    @Override
    public void setIsManaged(boolean newIsManaged, Player player) {
    }

    @Override
    public String mayManageText(Player player) {
        if (this.isWagoner()) {
            return "";
        }
        Village bVill = this.getBrandVillage();
        if (bVill != null) {
            return "Settlement \"" + bVill.getName() + "\" may manage";
        }
        return "";
    }

    @Override
    public String mayManageHover(Player aPlayer) {
        return "";
    }

    @Override
    public String messageOnTick() {
        return "";
    }

    @Override
    public String questionOnTick() {
        return "";
    }

    @Override
    public String messageUnTick() {
        return "";
    }

    @Override
    public String questionUnTick() {
        return "";
    }

    @Override
    public String getSettlementName() {
        Village bVill;
        String sName = "";
        Village village = bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
        if (bVill != null) {
            sName = bVill.getName();
        }
        if (sName.length() > 0) {
            return "Citizens of \"" + sName + "\"";
        }
        return "";
    }

    @Override
    public String getAllianceName() {
        Village bVill;
        String aName = "";
        Village village = bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
        if (bVill != null) {
            aName = bVill.getAllianceName();
        }
        if (aName.length() > 0) {
            return "Alliance of \"" + aName + "\"";
        }
        return "";
    }

    @Override
    public String getKingdomName() {
        return "";
    }

    @Override
    public boolean canAllowEveryone() {
        return true;
    }

    @Override
    public String getRolePermissionName() {
        Village bVill = this.getBrandVillage();
        if (bVill != null) {
            return "Brand Permission of \"" + bVill.getName() + "\"";
        }
        return "";
    }

    @Override
    public boolean isCitizen(Creature creature) {
        Village bVill;
        Village village = bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
        if (bVill != null) {
            return bVill.isCitizen(creature);
        }
        return false;
    }

    @Override
    public boolean isAllied(Creature creature) {
        Village bVill;
        Village village = bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
        if (bVill != null) {
            return bVill.isAlly(creature);
        }
        return false;
    }

    @Override
    public boolean isSameKingdom(Creature creature) {
        return false;
    }

    @Override
    public void addGuest(long guestId, int aSettings) {
        AnimalSettings.addPlayer(this.getWurmId(), guestId, aSettings);
    }

    @Override
    public void removeGuest(long guestId) {
        AnimalSettings.removePlayer(this.getWurmId(), guestId);
    }

    @Override
    public void addDefaultCitizenPermissions() {
        if (!this.getPermissionsPlayerList().exists(-30L)) {
            int value = AnimalSettings.Animal1Permissions.COMMANDER.getValue();
            this.addNewGuest(-30L, value);
        }
    }

    @Override
    public boolean isGuest(Creature creature) {
        return this.isGuest(creature.getWurmId());
    }

    @Override
    public boolean isGuest(long playerId) {
        return AnimalSettings.isGuest((PermissionsPlayerList.ISettings)this, playerId);
    }

    @Override
    public int getMaxAllowed() {
        return AnimalSettings.getMaxAllowed();
    }

    public void addNewGuest(long guestId, int aSettings) {
        AnimalSettings.addPlayer(this.getWurmId(), guestId, aSettings);
    }

    public Village getBrandVillage() {
        Brand brand = Creatures.getInstance().getBrand(this.getWurmId());
        if (brand != null) {
            try {
                Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                return villageBrand;
            }
            catch (NoSuchVillageException nsv) {
                brand.deleteBrand();
            }
        }
        return null;
    }

    @Override
    public final boolean canHavePermissions() {
        if (this.isWagoner() && Features.Feature.WAGONER.isEnabled()) {
            return true;
        }
        return this.getBrandVillage() != null;
    }

    public final boolean mayLead(Creature creature) {
        if (this.mayCommand(creature)) {
            return true;
        }
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        Village bvill = this.getBrandVillage();
        if (bvill != null) {
            VillageRole vr = bvill.getRoleFor(creature);
            return vr.mayLead();
        }
        Village cvill = this.getCurrentVillage();
        if (cvill != null) {
            VillageRole vr = cvill.getRoleFor(creature);
            return vr.mayLead();
        }
        return true;
    }

    @Override
    public final boolean mayShowPermissions(Creature creature) {
        return this.canHavePermissions() && this.mayManage(creature);
    }

    public final boolean canManage(Creature creature) {
        Wagoner wagoner;
        if (this.isWagoner() && (wagoner = this.getWagoner()) != null) {
            if (wagoner.getOwnerId() == creature.getWurmId()) {
                return true;
            }
            if (creature.getCitizenVillage() != null && creature.getCitizenVillage() == this.citizenVillage && creature.getCitizenVillage().isMayor(creature)) {
                return true;
            }
        }
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        Village vill = this.getBrandVillage();
        if (AnimalSettings.canManage(this, creature, vill)) {
            return true;
        }
        if (creature.getCitizenVillage() == null) {
            return false;
        }
        if (vill == null) {
            return false;
        }
        if (!vill.isCitizen(creature)) {
            return false;
        }
        return vill.isActionAllowed((short)663, creature);
    }

    public final boolean mayManage(Creature creature) {
        if (creature.getPower() > 1 && !this.isPlayer()) {
            return true;
        }
        return this.canManage(creature);
    }

    public final boolean maySeeHistory(Creature creature) {
        Wagoner wagoner;
        if (this.isWagoner() && (wagoner = this.getWagoner()) != null) {
            if (wagoner.getOwnerId() == creature.getWurmId()) {
                return true;
            }
            if (creature.getCitizenVillage() != null && creature.getCitizenVillage() == this.citizenVillage && creature.getCitizenVillage().isMayor(creature)) {
                return true;
            }
        }
        if (creature.getPower() > 1 && !this.isPlayer()) {
            return true;
        }
        Village bVill = this.getBrandVillage();
        return bVill != null && bVill.isMayor(creature);
    }

    public final boolean mayCommand(Creature creature) {
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        return AnimalSettings.mayCommand(this, creature, this.getBrandVillage());
    }

    public final boolean mayPassenger(Creature creature) {
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        return AnimalSettings.mayPassenger(this, creature, this.getBrandVillage());
    }

    public final boolean mayAccessHold(Creature creature) {
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        return AnimalSettings.mayAccessHold(this, creature, this.getBrandVillage());
    }

    public final boolean mayUse(Creature creature) {
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        return AnimalSettings.mayUse(this, creature, this.getBrandVillage());
    }

    public final boolean publicMayUse(Creature creature) {
        if (AnimalSettings.isExcluded(this, creature)) {
            return false;
        }
        return AnimalSettings.publicMayUse(this);
    }

    public ServerEntry getDestination() {
        return this.destination;
    }

    public void setDestination(ServerEntry destination) {
        if (destination != null && !destination.isChallengeOrEpicServer() && !destination.LOGINSERVER && destination != Servers.localServer) {
            this.destination = destination;
        }
    }

    public void clearDestination() {
        this.destination = null;
    }

    public int getVillageId() {
        if (this.getCitizenVillage() != null) {
            return this.getCitizenVillage().getId();
        }
        return 0;
    }

    private static Item getRareRecipe(String sig, int commonRecipeId, int rareRecipeId, int supremeRecipeId, int fantasticRecipeId) {
        int rno = Server.rand.nextInt(Servers.isThisATestServer() ? 100 : 1000);
        if (rno < 100) {
            int recipeId = -10;
            if (rno == 0 && fantasticRecipeId != -10) {
                recipeId = fantasticRecipeId;
            } else if (rno < 6 && supremeRecipeId != -10) {
                recipeId = supremeRecipeId;
            } else if (rno < 31 && rareRecipeId != -10) {
                recipeId = rareRecipeId;
            } else if (rno >= 50 && commonRecipeId != -10) {
                recipeId = commonRecipeId;
            }
            if (recipeId == -10) {
                return null;
            }
            Recipe recipe = Recipes.getRecipeById((short)recipeId);
            if (recipe == null) {
                return null;
            }
            int pp = Server.rand.nextBoolean() ? 1272 : 748;
            int itq = 20 + Server.rand.nextInt(50);
            try {
                Item newItem = ItemFactory.createItem(pp, itq, (byte)0, recipe.getLootableRarity(), null);
                newItem.setInscription(recipe, sig, 0x17A717);
                return newItem;
            }
            catch (FailedException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (NoSuchTemplateException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
        return null;
    }

    public short getDamageCounter() {
        return this.damageCounter;
    }

    public void setDamageCounter(short damageCounter) {
        this.damageCounter = damageCounter;
    }

    public List<Route> getHighwayPath() {
        return null;
    }

    public void setHighwayPath(String newDestination, List<Route> newPath) {
    }

    public String getHighwayPathDestination() {
        return "";
    }

    public long getLastWaystoneChecked() {
        return this.lastWaystoneChecked;
    }

    public void setLastWaystoneChecked(long waystone) {
        this.lastWaystoneChecked = waystone;
        Wagoner wagoner = this.getWagoner();
        if (this.isWagoner() && wagoner != null) {
            wagoner.setLastWaystoneId(waystone);
        }
    }

    public boolean embarkOn(long wurmId, byte type) {
        try {
            Item item = Items.getItem(wurmId);
            Vehicle vehicle = Vehicles.getVehicle(item);
            if (vehicle != null) {
                Seat[] seats = vehicle.getSeats();
                for (int x = 0; x < seats.length; ++x) {
                    if (seats[x].getType() != type || seats[x].isOccupied()) continue;
                    seats[x].occupy(vehicle, this);
                    if (type == 0) {
                        vehicle.pilotId = this.getWurmId();
                    }
                    this.setVehicleCommander(type == 0);
                    MountAction m = new MountAction(null, item, vehicle, x, type == 0, vehicle.seats[x].offz);
                    this.setMountAction(m);
                    this.setVehicle(item.getWurmId(), true, type);
                    return true;
                }
            }
        }
        catch (NoSuchItemException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return false;
    }

    public ArrayList<Effect> getEffects() {
        return this.effects;
    }

    public void addEffect(Effect e) {
        if (e == null) {
            return;
        }
        if (this.effects == null) {
            this.effects = new ArrayList();
        }
        this.effects.add(e);
    }

    public void removeEffect(Effect e) {
        if (this.effects == null || e == null) {
            return;
        }
        this.effects.remove(e);
        if (this.effects.isEmpty()) {
            this.effects = null;
        }
    }

    public void updateEffects() {
        if (this.effects == null) {
            return;
        }
        for (Effect e : this.effects) {
            e.setPosXYZ(this.getPosX(), this.getPosY(), this.getPositionZ(), false);
        }
    }

    public boolean isPlacingItem() {
        return this.isPlacingItem;
    }

    public void setPlacingItem(boolean placingItem) {
        this.isPlacingItem = placingItem;
        if (!placingItem) {
            this.setPlacementItem(null);
        }
    }

    public void setPlacingItem(boolean placingItem, Item placementItem) {
        this.isPlacingItem = placingItem;
        this.setPlacementItem(placementItem);
    }

    public Item getPlacementItem() {
        return this.placementItem;
    }

    public void setPlacementItem(Item placementItem) {
        this.placementItem = placementItem;
        if (placementItem == null) {
            this.pendingPlacement = null;
        }
    }

    public void setPendingPlacement(float xPos, float yPos, float zPos, float rot) {
        this.pendingPlacement = (float[])(this.placementItem != null ? new float[]{this.placementItem.getPosX(), this.placementItem.getPosY(), this.placementItem.getPosZ(), this.placementItem.getRotation(), xPos, yPos, zPos, Math.abs(rot - this.placementItem.getRotation()) > 180.0f ? rot - 360.0f : rot} : null);
    }

    public float[] getPendingPlacement() {
        return this.pendingPlacement;
    }

    public boolean canUseWithEquipment() {
        for (Item subjectItem : this.getBody().getContainersAndWornItems()) {
            if (!subjectItem.isCreatureWearableOnly()) continue;
            if (subjectItem.isSaddleLarge()) {
                if (this.getSize() <= 4) {
                    return false;
                }
                if (!this.isKingdomGuard()) continue;
                return false;
            }
            if (subjectItem.isSaddleNormal()) {
                if (this.getSize() > 4) {
                    return false;
                }
                if (!this.isKingdomGuard()) continue;
                return false;
            }
            if (!(subjectItem.isHorseShoe() ? !this.isHorse() && (!this.isUnicorn() || subjectItem.getMaterial() != 7 && subjectItem.getMaterial() != 8 && subjectItem.getMaterial() != 96) : subjectItem.isBarding() && !this.isHorse())) continue;
            return false;
        }
        return true;
    }

    public HashMap<Integer, SpellResist> getSpellResistances() {
        return this.spellResistances;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    public void setGuardTower(GuardTower guardTower) {
        this.guardTower = guardTower;
    }

    public GuardTower getGuardTower() {
        return this.guardTower;
    }

    static {
        pathFinder.startRunning();
        pathFinderAgg.startRunning();
        pathFinderNPC.startRunning();
        firstCreature = -10L;
        pollChecksPer = 301;
        totx = 0;
        toty = 0;
        movesx = 0;
        movesy = 0;
    }
}

