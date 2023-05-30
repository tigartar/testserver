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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
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
   protected HashMap<Integer, SpellResist> spellResistances = new HashMap<>();
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
   private static final DoubleValueModifier willowMod = new DoubleValueModifier(-0.15F);
   public boolean shouldStandStill = false;
   public byte opportunityAttackCounter = 0;
   private long lastSentToolbelt = 0L;
   protected static final float submergedMinDepth = -5.0F;
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
   private float teleportX = -1.0F;
   private float teleportY = -1.0F;
   protected int teleportLayer = 0;
   protected int teleportFloorLevel = 0;
   protected boolean justSpawned = false;
   public String spawnWeapon = "";
   public String spawnArmour = "";
   private LinkedList<int[]> openedTiles;
   private int carriedWeight = 0;
   private static final float DEGS_TO_RADS = (float) (Math.PI / 180.0);
   private TradeHandler tradeHandler;
   public Village citizenVillage;
   public Village currentVillage;
   private Set<Item> itemsTaken = null;
   private Set<Item> itemsDropped = null;
   protected MovementScheme movementScheme;
   protected Battle battle = null;
   private Set<Long> stealthBreakers = null;
   private Set<DoubleValueModifier> visionModifiers;
   private final ConcurrentHashMap<Item, Float> weaponsUsed = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<AttackAction, UsedAttackData> attackUsed = new ConcurrentHashMap<>();
   public long lastSavedPos = System.currentTimeMillis() - (long)Server.rand.nextInt(1800000);
   protected byte guardSecondsLeft = 0;
   private byte fightStyle = 2;
   private boolean milked = false;
   private boolean sheared = false;
   private boolean isRiftSummoned = false;
   public long target = -10L;
   public Creature leader = null;
   public long dominator = -10L;
   public float zoneBonus = 0.0F;
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
   private static final float HUNGER_RANGE = 20535.0F;
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
   private static final Set<Long> pantLess = new HashSet<>();
   private static final Map<Long, Set<MovementEntity>> illusions = new ConcurrentHashMap<>();
   protected boolean isInOwnBattleCamp = false;
   private boolean doLavaDamage = false;
   private boolean doAreaDamage = false;
   protected float webArmourModTime = 0.0F;
   private ArrayList<Effect> effects;
   private ServerEntry destination;
   private static CreaturePathFinder pathFinder = new CreaturePathFinder();
   private static CreaturePathFinderAgg pathFinderAgg = new CreaturePathFinderAgg();
   private static CreaturePathFinderNPC pathFinderNPC = new CreaturePathFinderNPC();
   public long vehicle = -10L;
   protected byte seatType = -1;
   protected int teleports = 0;
   private long lastWaystoneChecked = -10L;
   private boolean checkedHotItemsAfterLogin = false;
   private boolean ignoreSaddleDamage = false;
   private boolean isPlacingItem = false;
   private Item placementItem = null;
   private float[] pendingPlacement = null;
   private GuardTower guardTower = null;
   private int lastSecond = 1;
   static long firstCreature = -10L;
   static int pollChecksPer = 301;
   static final int breedPollCounter = 201;
   int breedTick = 0;
   private int lastPolled = Server.rand.nextInt(pollChecksPer);
   private CreatureAIData aiData = null;
   private boolean isPathing = false;
   private boolean setTargetNOID = false;
   private Creature creatureToBlinkTo = null;
   public boolean receivedPath = false;
   private PathTile targetPathTile = null;
   static int totx = 0;
   static int toty = 0;
   static int movesx = 0;
   static int movesy = 0;
   protected static final String NOPATH = "No pathing now";
   float creatureFavor = 100.0F;
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

   public void checkTrap() {
      if (!this.isDead()) {
         Trap trap = Trap.getTrap(this.currentTile.tilex, this.currentTile.tiley, this.getLayer());
         if (this.getPower() >= 3) {
            if (trap != null) {
               this.getCommunicator().sendNormalServerMessage("A " + trap.getName() + " is here.");
            }
         } else if (trap != null) {
            boolean trigger = false;
            if (trap.getKingdom() != this.getKingdomId()) {
               if (this.getKingdomId() == 0 && !this.isAggHuman()) {
                  trigger = false;
                  if (this.riders != null && this.riders.size() > 0) {
                     for(Long rider : this.riders) {
                        try {
                           Creature rr = Server.getInstance().getCreature(rider);
                           if (rr.getKingdomId() != trap.getKingdom()) {
                              trigger = true;
                           }
                        } catch (NoSuchCreatureException var7) {
                        } catch (NoSuchPlayerException var8) {
                        }
                     }
                  }
               } else {
                  trigger = true;
               }
            } else if (trap.getVillage() > 0) {
               try {
                  Village vill = Villages.getVillage(trap.getVillage());
                  if (vill.isEnemy(this)) {
                     trigger = true;
                  }
               } catch (NoSuchVillageException var6) {
               }
            }

            if (trigger) {
               trap.doEffect(this, this.currentTile.tilex, this.currentTile.tiley, this.getLayer());
            }
         }
      }
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
         if (tile.getStructure() != null && tile.getStructure().isTypeHouse()) {
            if (this.getFloorLevel() != 0 || wasOnBridge) {
               int targetFloorLevel = tile.getDropFloorLevel(this.getFloorLevel());
               if (targetFloorLevel != this.getFloorLevel()) {
                  if (!this.isPlayer()) {
                     this.pushToFloorLevel(targetFloorLevel);
                  }
               } else if (forceAddFloorLayer && !this.isPlayer()) {
                  float oldposz = this.getPositionZ();
                  float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface())
                     + (tile.getFloors(-10, 10).length == 0 ? 0.0F : 0.25F);
                  float diffz = newPosz - oldposz;
                  this.setPositionZ(newPosz);
                  if (this.currentTile != null && this.getVisionArea() != null) {
                     this.moved(0.0F, 0.0F, diffz, 0, 0);
                  }
               }
            } else if (!this.isPlayer()) {
               float oldposz = this.getPositionZ();
               if ((double)oldposz >= -1.25) {
                  float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface())
                     + (tile.getFloors(-10, 10).length == 0 ? 0.0F : 0.25F);
                  float diffz = newPosz - oldposz;
                  this.setPositionZ(newPosz);
                  if (this.currentTile != null && this.getVisionArea() != null) {
                     this.moved(0.0F, 0.0F, diffz, 0, 0);
                  }
               }
            }
         } else if ((tile.getStructure() == null || !tile.getStructure().isTypeBridge()) && this.getFloorLevel() >= 0 && !this.isPlayer()) {
            float oldposz = this.getPositionZ();
            if (oldposz >= 0.0F) {
               float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface());
               float diffz = newPosz - oldposz;
               this.setPositionZ(newPosz);
               if (this.currentTile != null && this.getVisionArea() != null) {
                  this.moved(0.0F, 0.0F, diffz, 0, 0);
               }
            }
         }
      } catch (NoSuchZoneException var8) {
      }
   }

   public int compareTo(Creature otherCreature) {
      return this.getName().compareTo(otherCreature.getName());
   }

   public boolean setNewTile(@Nullable VolaTile newtile, float diffZ, boolean ignoreBridge) {
      if (newtile == null || this.getTileX() == newtile.tilex && this.getTileY() == newtile.tiley) {
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
               ListIterator<int[]> openedIterator = this.openedTiles.listIterator();

               while(openedIterator.hasNext()) {
                  int[] opened = (int[])openedIterator.next();
                  if (newtile == null || opened[0] != newtile.getTileX() || opened[1] != newtile.getTileY()) {
                     try {
                        this.getCommunicator().sendTileDoor((short)opened[0], (short)opened[1], false);
                        openedIterator.remove();
                        MineDoorPermission md = MineDoorPermission.getPermission((short)opened[0], (short)opened[1]);
                        if (md != null) {
                           md.close(this);
                        }
                     } catch (IOException var16) {
                     }
                  }
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

               for(FocusZone fz : newFocusZones) {
                  if (fz.isPvP()) {
                     this.isInPvPZone = true;
                     break;
                  }

                  if (fz.isNonPvP()) {
                     this.isInNonPvPZone = true;
                     break;
                  }
               }

               ++this.tilesMoved;
               if (this.tilesMoved >= 10) {
                  if (this.isDominated() || this.isHorse()) {
                     try {
                        this.savePosition(this.currentTile.getZone().getId());
                     } catch (IOException var15) {
                     }
                  }

                  this.tilesMoved = 0;
               }
            }

            if (this.isPlayer()) {
               try {
                  this.savePosition(this.currentTile.getZone().getId());
               } catch (IOException var14) {
               }

               for(FocusZone fz : newFocusZones) {
                  if (fz.isFog() && !this.isInFogZone) {
                     this.isInFogZone = true;
                     this.getCommunicator().sendSpecificWeather(0.85F);
                  }

                  if (fz.isPvP()) {
                     if (!this.isInPvPZone) {
                        if (!this.isOnPvPServer()) {
                           this.achievement(4);
                           this.getCommunicator()
                              .sendAlertServerMessage("You enter the " + fz.getName() + " PvP area. Other players may attack you here.", (byte)4);
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
                           this.getCommunicator()
                              .sendSafeServerMessage("You enter the " + fz.getName() + " No-PvP area. Other players may no longer attack you here.", (byte)2);
                        } else {
                           this.getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " No-PvP area.", (byte)2);
                        }

                        this.sendAttitudeChange();
                     }

                     this.isInNonPvPZone = true;
                     break;
                  }

                  if ((fz.isName() || fz.isNamePopup() || fz.isNoBuild() || fz.isPremSpawnOnly()) && (oldFocusZones == null || !oldFocusZones.contains(fz))) {
                     if (!fz.isName() && !fz.isNoBuild() && !fz.isPremSpawnOnly()) {
                        SimplePopup sp = new SimplePopup(this, "Entering " + fz.getName(), "You enter the " + fz.getName() + " area.", fz.getDescription());
                        sp.sendQuestion();
                     } else {
                        this.getCommunicator().sendSafeServerMessage("You enter the " + fz.getName() + " area.", (byte)2);
                     }
                  }
               }

               if (oldFocusZones != null) {
                  for(FocusZone fz : oldFocusZones) {
                     if (fz.isFog() && (newFocusZones == null || !newFocusZones.contains(fz))) {
                        this.isInFogZone = false;
                        this.getCommunicator().checkSendWeather();
                     }

                     if (fz.isPvP()) {
                        if (newFocusZones == null || !newFocusZones.contains(fz)) {
                           this.isInPvPZone = false;
                           if (this.isOnPvPServer()) {
                              this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                           } else {
                              this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " PvP area.", (byte)2);
                           }

                           this.sendAttitudeChange();
                        }
                     } else if (fz.isNonPvP()) {
                        if (newFocusZones == null || !newFocusZones.contains(fz)) {
                           this.isInNonPvPZone = false;
                           this.sendAttitudeChange();
                           if (this.isOnPvPServer()) {
                              this.getCommunicator()
                                 .sendAlertServerMessage("You leave the " + fz.getName() + " No-PvP area. Other players may attack you here.", (byte)2);
                           } else {
                              this.getCommunicator().sendAlertServerMessage("You leave the " + fz.getName() + " No-PvP area.", (byte)2);
                           }
                        }
                     } else if ((fz.isName() || fz.isNamePopup() || fz.isNoBuild() || fz.isPremSpawnOnly())
                        && (newFocusZones == null || !newFocusZones.contains(fz))) {
                        if (!fz.isName() && !fz.isNoBuild() && !fz.isPremSpawnOnly()) {
                           SimplePopup sp = new SimplePopup(this, "Leaving " + fz.getName(), "You leave the " + fz.getName() + " area.");
                           sp.sendQuestion();
                        } else {
                           this.getCommunicator().sendSafeServerMessage("You leave the " + fz.getName() + " area.", (byte)2);
                        }
                     }
                  }
               }

               if (!WurmCalendar.isSeasonWinter()) {
                  HiveZone newHiveZone = Zones.getHiveZoneAt(this.currentTile.tilex, this.currentTile.tiley, this.isOnSurface());
                  boolean newHiveClose = newHiveZone == null ? false : newHiveZone.isClose(this.currentTile.tilex, this.currentTile.tiley);
                  boolean domestic = newHiveZone == null ? false : newHiveZone.getCurrentHive().getTemplateId() == 1175;
                  if (oldHiveClose && !newHiveClose) {
                     this.getCommunicator()
                        .sendSafeServerMessage("The sounds of bees decreases as you move further away from the hive.", (byte)(domestic ? 0 : 2));
                  }

                  if (oldHiveZone == null && newHiveZone != null) {
                     this.getCommunicator().sendSafeServerMessage("You hear bees, maybe you are getting close to a hive.", (byte)(domestic ? 0 : 2));
                  } else if (oldHiveZone != null && newHiveZone == null) {
                     this.getCommunicator()
                        .sendSafeServerMessage(
                           "The sounds of bees disappears in the distance.", (byte)(oldHiveZone.getCurrentHive().getTemplateId() == 1175 ? 0 : 2)
                        );
                  }

                  if (!oldHiveClose && newHiveClose) {
                     if (newHiveZone.getCurrentHive().hasTwoQueens()) {
                        this.getCommunicator()
                           .sendSafeServerMessage(
                              "The bees noise is getting louder, sounds like there is unusual activity in the hive.", (byte)(domestic ? 0 : 2)
                           );
                     } else {
                        this.getCommunicator()
                           .sendSafeServerMessage("The bees noise is getting louder, maybe you are getting closer to their hive.", (byte)(domestic ? 0 : 2));
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

               if (!Servers.localServer.HOMESERVER
                  && this.isOnSurface()
                  && this.getFaith() > 0.0F
                  && (float)Server.rand.nextInt(100) < this.getFaith()
                  && EndGameItems.getArtifactAtTile(this.currentTile.tilex, this.currentTile.tiley) != null
                  && this.getDeity() != null) {
                  this.getCommunicator().sendSafeServerMessage(this.getDeity().name + " urges you to deeply investigate the area!");
               }
            }

            if (this.isPlayer() && !this.currentTile.isTransition && this.getVisionArea() != null && this.getVisionArea().isInitialized()) {
               this.checkOpenMineDoor();
            }
         }

         if (this.currentTile != null) {
            this.checkInvisDetection();
            boolean hostilePerimeter = false;
            if (this.isPlayer()) {
               Village lVill = Villages.getVillageWithPerimeterAt(this.getTileX(), this.getTileY(), true);
               if (lVill != null && lVill.kingdom == this.getKingdomId() && lVill.isEnemy(this)) {
                  if (!this.inHostilePerimeter) {
                     this.getCommunicator()
                        .sendAlertServerMessage(
                           "You are now within the hostile perimeter of " + lVill.getName() + " and will be attacked by kingdom guards.", (byte)4
                        );
                  }

                  hostilePerimeter = true;
               }
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
      } else {
         logger.log(
            Level.WARNING,
            this.getName() + " set to " + newtile.tilex + "," + newtile.tiley + " but at " + this.getTileX() + "," + this.getTileY(),
            (Throwable)(new Exception())
         );
         if (this.currentTile != null) {
            logger.log(
               Level.WARNING,
               "old is "
                  + this.currentTile.tilex
                  + "("
                  + this.getPosX()
                  + "), "
                  + this.currentTile.tiley
                  + "("
                  + this.getPosY()
                  + "), vehic="
                  + this.getVehicle()
            );
            if (this.isPlayer()) {
               ((Player)this)
                  .intraTeleport(
                     (float)((this.currentTile.tilex << 2) + 2),
                     (float)((this.currentTile.tiley << 2) + 2),
                     this.getPositionZ(),
                     this.getStatus().getRotation(),
                     this.getLayer(),
                     "on wrong tile"
                  );
            }
         }

         return false;
      }
   }

   public final boolean isInOwnDuelRing() {
      return this.isInDuelRing;
   }

   public final boolean hasOpenedMineDoor(int tilex, int tiley) {
      if (this.openedTiles == null) {
         return false;
      } else {
         for(int[] openedTile : this.openedTiles) {
            if (openedTile[0] == tilex && openedTile[1] == tiley) {
               return true;
            }
         }

         return false;
      }
   }

   public void checkOpenMineDoor() {
      if (this.currentTile != null) {
         Set<int[]> oldM = Terraforming.getAllMineDoors(this.currentTile.tilex, this.currentTile.tiley);
         if (oldM != null) {
            for(int[] checkedTile : oldM) {
               if (!this.hasOpenedMineDoor(checkedTile[0], checkedTile[1])) {
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

                     if (ok) {
                        if (this.openedTiles == null) {
                           this.openedTiles = new LinkedList<>();
                        }

                        this.openedTiles.add(checkedTile);
                        this.getMovementScheme().touchFreeMoveCounter();
                        this.getVisionArea().checkCaves(false);
                        this.getCommunicator().sendTileDoor((short)checkedTile[0], (short)checkedTile[1], true);
                        md.open(this);
                     }
                  } catch (IOException var7) {
                  }
               }
            }
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
      if (this.bestLightsource != null && (!this.bestLightsource.isOnFire() || this.bestLightsource.getOwnerId() != this.getWurmId())) {
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

      if (_opponent == null || _opponent.getAttackers() < _opponent.getMaxGroupAttackSize() || _opponent.isPlayer() && !this.isPlayer()) {
         if (this.opponent != _opponent && _opponent != null && this.isPlayer() && _opponent.isPlayer()) {
            this.battle = Battles.getBattleFor(this, _opponent);
            this.battle.addEvent(new BattleEvent((short)-1, this.getName(), _opponent.getName()));
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
            } catch (IOException var3) {
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
      if (style == 2) {
         mess = "You will now fight defensively.";
      } else if (style == 1) {
         mess = "You will now fight aggressively.";
      } else {
         mess = "You will now fight normally.";
      }

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
      } else {
         return this.getLoyalty() > 0.0F
            ? (this.isReborn() ? 0.7F : 0.5F) * this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier()
            : this.template.getBaseCombatRating() * this.status.getBattleRatingTypeModifier();
      }
   }

   public float getBonusCombatRating() {
      return this.template.getBonusCombatRating();
   }

   public final boolean isOkToKillBy(Creature attacker) {
      if (!Servers.localServer.HOMESERVER && !Servers.localServer.isChallengeServer()) {
         return true;
      } else if (!attacker.isFriendlyKingdom(this.getKingdomId())) {
         return true;
      } else if (Servers.isThisAChaosServer()) {
         return true;
      } else if (this.getKingdomTemplateId() == 3) {
         return true;
      } else if (this.hasAttackedUnmotivated()) {
         return true;
      } else if (attacker.isDuelOrSpar(this)) {
         return true;
      } else if (this.getReputation() < 0) {
         return true;
      } else if (this.isInOwnDuelRing()) {
         return true;
      } else if (Zones.isWithinDuelRing(this.getTileX(), this.getTileY(), true) != null) {
         return true;
      } else {
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
   }

   public final boolean isEnemyOnChaos(Creature creature) {
      return Servers.isThisAChaosServer() && this.isInSameAlliance(creature) ? false : false;
   }

   public final boolean isInSameAlliance(Creature creature) {
      if (this.getCitizenVillage() == null) {
         return false;
      } else if (creature.getCitizenVillage() == null) {
         return false;
      } else {
         return this.getCitizenVillage().getAllianceNumber() == creature.getCitizenVillage().getAllianceNumber();
      }
   }

   public boolean hasAttackedUnmotivated() {
      if (this.isDominated() && this.getDominator() != null) {
         return this.getDominator().hasAttackedUnmotivated();
      } else {
         SpellEffects effs = this.getSpellEffects();
         if (effs == null) {
            return false;
         } else {
            SpellEffect eff = effs.getSpellEffect((byte)64);
            return eff != null;
         }
      }
   }

   public void setUnmotivatedAttacker() {
      if (!this.isNpc()) {
         if (Servers.isThisAPvpServer() && Servers.localServer.HOMESERVER) {
            if (this.getKingdomTemplateId() != 3) {
               SpellEffects effs = this.getSpellEffects();
               if (effs == null) {
                  effs = this.createSpellEffects();
               }

               SpellEffect eff = effs.getSpellEffect((byte)64);
               if (eff == null) {
                  this.setVisible(false);
                  logger.log(Level.INFO, this.getName() + " set unmotivated attacker at ", (Throwable)(new Exception()));
                  eff = new SpellEffect(this.getWurmId(), (byte)64, 100.0F, (int)(Servers.isThisATestServer() ? 120L : 1800L), (byte)1, (byte)1, true);
                  effs.addSpellEffect(eff);
                  this.setVisible(true);
                  this.getCommunicator().sendAlertServerMessage("You have received the hunted status and may be attacked without penalty for half an hour.");
                  if (this.getCitizenVillage() != null) {
                     this.getCitizenVillage().setVillageRep(this.getCitizenVillage().getVillageReputation() + 10);
                  }

                  Achievements ach = Achievements.getAchievementObject(this.getWurmId());
                  if (ach != null && ach.getAchievement(369) != null) {
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
      }
   }

   public void addAttacker(Creature creature) {
      if (!this.isDuelOrSpar(creature)) {
         if (this.isSpiritGuard() && this.getCitizenVillage() != null && !this.getCitizenVillage().containsTarget(creature)) {
            this.getCitizenVillage().addTarget(creature);
         }

         if (this.attackers == null) {
            this.attackers = new HashMap<>();
         }

         if (creature.isPlayer()) {
            if (!this.isInvulnerable()) {
               this.setSecondsToLogout(this.getSecondsToLogout());
            }

            if (this.isPlayer()) {
               if (!this.isOkToKillBy(creature) && !creature.hasBeenAttackedBy(this.getWurmId())) {
                  creature.setUnmotivatedAttacker();
               }
            } else if (this.isRidden()) {
               if (creature.getCitizenVillage() == null || this.getCurrentVillage() != creature.getCitizenVillage()) {
                  for(Long riderLong : this.getRiders()) {
                     try {
                        Creature rider = Server.getInstance().getCreature(riderLong);
                        if (rider != creature) {
                           if (!creature.hasBeenAttackedBy(rider.getWurmId())
                              && !creature.hasBeenAttackedBy(this.getWurmId())
                              && !rider.isOkToKillBy(creature)) {
                              creature.setUnmotivatedAttacker();
                           }

                           rider.addAttacker(creature);
                        }
                     } catch (NoSuchCreatureException var9) {
                     } catch (NoSuchPlayerException var10) {
                     }
                  }
               }
            } else if (this.getHitched() != null) {
               if (Servers.localServer.HOMESERVER
                  && (creature.getCitizenVillage() == null || this.getCurrentVillage() != creature.getCitizenVillage())
                  && !this.getHitched().isCreature()) {
                  try {
                     Item i = Items.getItem(this.getHitched().wurmid);
                     long ownid = i.getLastOwnerId();

                     try {
                        if (ownid != creature.getWurmId()) {
                           byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                           if (creature.isFriendlyKingdom(kingd) && !creature.hasBeenAttackedBy(ownid)) {
                              creature.setUnmotivatedAttacker();
                           }
                        }
                     } catch (Exception var7) {
                     }
                  } catch (NoSuchItemException var8) {
                     logger.log(Level.INFO, this.getHitched().wurmid + " no such item:", (Throwable)var8);
                  }
               }
            } else if (this.isDominated()) {
               if (Servers.localServer.HOMESERVER) {
                  this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
                  if (creature.isFriendlyKingdom(this.getKingdomId())
                     && !creature.hasBeenAttackedBy(this.dominator)
                     && !creature.hasBeenAttackedBy(this.getWurmId())
                     && creature != this.getDominator()) {
                     creature.setUnmotivatedAttacker();
                  }
               }
            } else if (this.getCurrentVillage() != null && Servers.localServer.HOMESERVER) {
               Brand brand = Creatures.getInstance().getBrand(this.getWurmId());
               if (brand != null) {
                  try {
                     Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                     if (this.getCurrentVillage() == villageBrand
                        && creature.getCitizenVillage() != villageBrand
                        && !villageBrand.isEnemy(creature.getCitizenVillage())) {
                        creature.setUnmotivatedAttacker();
                     }
                  } catch (NoSuchVillageException var6) {
                     brand.deleteBrand();
                  }
               }
            }

            if (!creature.hasAddedToAttack) {
               this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
            }
         } else if (!creature.hasAddedToAttack) {
            this.attackers.put(creature.getWurmId(), System.currentTimeMillis());
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
      } else if (this.attackers == null) {
         return false;
      } else {
         Long l = _id;
         return this.attackers.keySet().contains(l);
      }
   }

   public long[] getLatestAttackers() {
      if (this.attackers != null && this.attackers.size() > 0) {
         Long[] lKeys = this.attackers.keySet().toArray(new Long[this.attackers.size()]);
         long[] toReturn = new long[lKeys.length];

         for(int x = 0; x < toReturn.length; ++x) {
            toReturn[x] = lKeys[x];
         }

         return toReturn;
      } else {
         return EMPTY_LONG_PRIMITIVE_ARRAY;
      }
   }

   protected long[] getAttackerIds() {
      if (this.attackers == null) {
         return EMPTY_LONG_PRIMITIVE_ARRAY;
      } else {
         Long[] longs = this.attackers.keySet().toArray(new Long[this.attackers.size()]);
         long[] ll = new long[longs.length];

         for(int x = 0; x < longs.length; ++x) {
            ll[x] = longs[x];
         }

         return ll;
      }
   }

   public void trimAttackers(boolean delete) {
      if (delete) {
         this.attackers = null;
      } else if (this.attackers != null && this.attackers.size() > 0) {
         Long[] lKeys = this.attackers.keySet().toArray(new Long[this.attackers.size()]);

         for(Long lLKey : lKeys) {
            Long time = this.attackers.get(lLKey);
            if (WurmId.getType(lLKey) == 1) {
               if (System.currentTimeMillis() - time > 180000L) {
                  this.attackers.remove(lLKey);
               }
            } else if (System.currentTimeMillis() - time > 300000L) {
               this.attackers.remove(lLKey);
            }
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
         for(Long l : this.attackers.values()) {
            if (System.currentTimeMillis() - l < (long)(seconds * 1000)) {
               return true;
            }
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
                  for(Item waystone : Items.getWaystones()) {
                     VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
                     if (vt != null) {
                        for(VirtualZone vz : vt.getWatchers()) {
                           try {
                              if (vz.getWatcher().getWurmId() == this.getWurmId()) {
                                 this.getCommunicator().sendWaystoneData(waystone);
                                 break;
                              }
                           } catch (Exception var12) {
                              logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                           }
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
      if (this.isVisible()) {
         this.setVisible(false);
         this.setVisible(true);
      }
   }

   public void setVisible(boolean visible) {
      this.status.visible = visible;
      if (this.getStatus().offline) {
         this.status.visible = false;
      } else {
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
               } catch (NoSuchCreatureException var7) {
                  logger.log(Level.INFO, var7.getMessage() + " " + this.id + ", " + this.name, (Throwable)var7);
               } catch (NoSuchPlayerException var8) {
                  logger.log(Level.INFO, var8.getMessage() + " " + this.id + ", " + this.name, (Throwable)var8);
               }
            } else {
               tile.makeInvisible(this);
            }
         } catch (NoSuchZoneException var9) {
            logger.log(Level.INFO, this.getName() + " outside of bounds when going invis.");
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

         this.zoneBonus = 0.0F;
         Deity deity = this.getDeity();
         if (deity != null) {
            FaithZone z = Zones.getFaithZone(tilex, tiley, surfaced);
            if (z != null) {
               if (z.getCurrentRuler() == deity) {
                  if (this.getFaith() > 30.0F) {
                     this.zoneBonus += 10.0F;
                  }

                  if (this.getFaith() > 90.0F) {
                     this.zoneBonus += this.getFaith() - 90.0F;
                  }

                  if (Features.Feature.NEWDOMAINS.isEnabled()) {
                     this.zoneBonus += (float)z.getStrengthForTile(tilex, tiley, surfaced) / 2.0F;
                  } else {
                     this.zoneBonus += (float)z.getStrength() / 2.0F;
                  }
               } else if ((Features.Feature.NEWDOMAINS.isEnabled() ? z.getStrengthForTile(tilex, tiley, surfaced) : z.getStrength()) == 0
                  && this.getFaith() >= 90.0F) {
                  this.zoneBonus = 5.0F + this.getFaith() - 90.0F;
               }
            } else if (this.getFaith() >= 90.0F) {
               this.zoneBonus = 5.0F + this.getFaith() - 90.0F;
            }
         }
      } catch (NoSuchZoneException var6) {
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
                  this.getCommunicator()
                     .sendSafeServerMessage(
                        "You feel an energy boost, as if " + this.getDeity().getName() + " turns " + this.getDeity().getHisHerItsString() + " eyes at you."
                     );
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
                  this.achievement(374);
                  if (newKingdom == 3 && this.getKingdomId() != 3) {
                     this.musicPlayer.checkMUSIC_TERRITORYHOTS_SND();
                  } else if (this.getKingdomId() == 3) {
                     this.musicPlayer.checkMUSIC_TERRITORYWL_SND();
                  }

                  Appointments p = King.getCurrentAppointments(newKingdom);
                  if (p != null) {
                     long secret = p.getOfficialForId(1500);
                     if (secret > 0L) {
                        try {
                           Creature c = Server.getInstance().getCreature(secret);
                           if (c.getMindLogical().skillCheck(40.0, 0.0, false, 1.0F) > 0.0) {
                              c.getCommunicator()
                                 .sendNormalServerMessage("Your informers relay information that " + this.getName() + " has entered your territory.");
                           }
                        } catch (Exception var6) {
                        }
                     }
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
         this.getCommunicator().sendNormalServerMessage("You no longer feel the presence of " + Deities.getDeity(this.currentDeity).name + ".");
         this.currentDeity = 0;
      }
   }

   public Creature(long aId) throws Exception {
      this();
      this.setWurmId(aId, 0.0F, 0.0F, 0.0F, 0);
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
         this.itemsTaken = new HashSet<>();
      }

      this.itemsTaken.add(item);
   }

   public void addItemDropped(Item item) {
      this.checkTheftWarnQuestion();
      if (this.itemsDropped == null) {
         this.itemsDropped = new HashSet<>();
      }

      this.itemsDropped.add(item);
   }

   public void addChallengeScore(int type, float scoreAdded) {
   }

   protected void sendItemsTaken() {
      if (this.itemsTaken != null) {
         PlayerTutorial.firePlayerTrigger(this.getWurmId(), PlayerTutorial.PlayerTrigger.TAKEN_ITEM);
         Map<Integer, Integer> diffItems = new HashMap<>();
         Map<String, Integer> foodItems = new HashMap<>();

         for(Item item : this.itemsTaken) {
            if (item.isFood()) {
               String name = item.getName();
               if (foodItems.containsKey(name)) {
                  Integer num = foodItems.get(name);
                  int nums = num;
                  foodItems.put(name, ++nums);
               } else {
                  foodItems.put(name, 1);
               }
            } else {
               Integer templateId = item.getTemplateId();
               if (diffItems.containsKey(templateId)) {
                  Integer num = diffItems.get(templateId);
                  int nums = num;
                  diffItems.put(templateId, ++nums);
               } else {
                  diffItems.put(templateId, 1);
               }
            }
         }

         for(Integer key : diffItems.keySet()) {
            try {
               ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate(key);
               Integer num = diffItems.get(key);
               int number = num;
               if (number == 1) {
                  this.getCommunicator().sendNormalServerMessage("You get " + lTemplate.getNameWithGenus() + ".");
                  if (this.isVisible()) {
                     Server.getInstance().broadCastAction(this.name + " gets " + lTemplate.getNameWithGenus() + ".", this, 5);
                  }
               } else {
                  this.getCommunicator()
                     .sendNormalServerMessage("You get " + StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate.getPlural() + ".");
                  if (this.isVisible()) {
                     Server.getInstance()
                        .broadCastAction(
                           this.name + " gets " + StringUtilities.getWordForNumber(number) + " " + lTemplate.sizeString + lTemplate.getPlural() + ".", this, 5
                        );
                  }
               }
            } catch (NoSuchTemplateException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            }
         }

         for(String key : foodItems.keySet()) {
            Integer num = foodItems.get(key);
            int number = num;
            if (number == 1) {
               this.getCommunicator().sendNormalServerMessage("You get " + StringUtilities.addGenus(key) + ".");
               if (this.isVisible()) {
                  Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.addGenus(key) + ".", this, 5);
               }
            } else {
               this.getCommunicator().sendNormalServerMessage("You get " + StringUtilities.getWordForNumber(number) + " " + key + ".");
               if (this.isVisible()) {
                  Server.getInstance().broadCastAction(this.name + " gets " + StringUtilities.getWordForNumber(number) + " " + key + ".", this, 5);
               }
            }
         }

         this.itemsTaken = null;
      }
   }

   public boolean isIgnored(long playerId) {
      return false;
   }

   public void sendItemsDropped() {
      if (this.itemsDropped != null) {
         Map<Integer, Integer> diffItems = new HashMap<>();
         Map<String, Integer> foodItems = new HashMap<>();

         for(Item item : this.itemsDropped) {
            if (item.isFood()) {
               String name = item.getName();
               if (foodItems.containsKey(name)) {
                  Integer num = foodItems.get(name);
                  int nums = num;
                  foodItems.put(name, ++nums);
               } else {
                  foodItems.put(name, 1);
               }
            } else {
               Integer templateId = item.getTemplateId();
               if (diffItems.containsKey(templateId)) {
                  Integer num = diffItems.get(templateId);
                  int nums = num;
                  diffItems.put(templateId, ++nums);
               } else {
                  diffItems.put(templateId, 1);
               }
            }
         }

         for(Integer key : diffItems.keySet()) {
            try {
               ItemTemplate lTemplate = ItemTemplateFactory.getInstance().getTemplate(key);
               Integer num = diffItems.get(key);
               int number = num;
               if (number == 1) {
                  this.getCommunicator().sendNormalServerMessage("You drop " + lTemplate.getNameWithGenus() + ".");
                  Server.getInstance()
                     .broadCastAction(this.name + " drops " + lTemplate.getNameWithGenus() + ".", this, Math.max(3, lTemplate.getSizeZ() / 10));
               } else {
                  this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.getWordForNumber(number) + " " + lTemplate.getPlural() + ".");
                  Server.getInstance()
                     .broadCastAction(this.name + " drops " + StringUtilities.getWordForNumber(number) + " " + lTemplate.getPlural() + ".", this, 5);
               }
            } catch (NoSuchTemplateException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            }
         }

         for(String key : foodItems.keySet()) {
            Integer num = foodItems.get(key);
            int number = num;
            if (number == 1) {
               this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.addGenus(key) + ".");
               Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.addGenus(key) + ".", this, 5);
            } else {
               this.getCommunicator().sendNormalServerMessage("You drop " + StringUtilities.getWordForNumber(number) + " " + key + ".");
               Server.getInstance().broadCastAction(this.name + " drops " + StringUtilities.getWordForNumber(number) + " " + key + ".", this, 5);
            }
         }

         this.itemsDropped = null;
      }
   }

   public String getHoverText(@Nonnull Creature watcher) {
      String hoverText = "";
      if ((!watcher.isPlayer() || !watcher.hasFlag(57)) && (!this.isPlayer() || !this.hasFlag(58)) && this.getCitizenVillage() != null && this.isPlayer()) {
         hoverText = hoverText + this.getCitizenVillage().getCitizen(this.getWurmId()).getRole().getName() + " of " + this.getCitizenVillage().getName();
      }

      return hoverText;
   }

   public String getNameWithGenus() {
      if (!this.isUnique() && !this.isPlayer()) {
         if (this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) != 0) {
            return "the " + this.getName();
         } else {
            return this.template.isVowel(this.getName().substring(0, 1)) ? "an " + this.getName() : "a " + this.getName();
         }
      } else {
         return this.getName();
      }
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
      } else if (this.riders != null && this.riders.size() > 0) {
         return false;
      } else if (this.isDominated()) {
         return this.getDominator() != null ? this.getDominator().equals(potentialLeader) : false;
      } else {
         return this.template.isLeadable();
      }
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
      this.damageCounter = (short)((int)(30.0F * ItemBonus.getHurtingReductionBonus(this)));
      this.setStealth(false);
      this.getStatus().sendStateString();
   }

   private void addWoundMod() {
      this.getMovementScheme().addModifier(this.woundMoveMod);
      if (this.isPlayer()) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.WOUNDMOVE, 100000, 100.0F);
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
      } else if (this.isEncumbered()) {
         return 10;
      } else {
         return this.isCantMove() ? 20 : 0;
      }
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
         } catch (NoSuchSkillException var2) {
            logger.log(Level.WARNING, "No strength skill for " + this, (Throwable)var2);
         }
      }
   }

   public void calcBaseMoveMod() {
      if (this.carriedWeight < this.moveslow) {
         this.movementScheme.setEncumbered(false);
         this.movementScheme.setBaseModifier(1.0F);
      } else if (this.carriedWeight >= this.cantmove) {
         this.movementScheme.setEncumbered(true);
         this.movementScheme.setBaseModifier(0.05F);
         this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
      } else if (this.carriedWeight >= this.encumbered) {
         this.movementScheme.setEncumbered(false);
         this.movementScheme.setBaseModifier(0.25F);
      } else if (this.carriedWeight >= this.moveslow) {
         this.movementScheme.setEncumbered(false);
         this.movementScheme.setBaseModifier(0.75F);
      }
   }

   public void addCarriedWeight(int weight) {
      boolean canTriggerPlonk = false;
      if (this.isPlayer()) {
         if (this.carriedWeight < this.moveslow) {
            if (this.carriedWeight + weight >= this.cantmove) {
               this.movementScheme.setEncumbered(true);
               this.movementScheme.setBaseModifier(0.05F);
               this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
               canTriggerPlonk = true;
            } else if (this.carriedWeight + weight >= this.encumbered) {
               this.movementScheme.setBaseModifier(0.25F);
               canTriggerPlonk = true;
            } else if (this.carriedWeight + weight >= this.moveslow) {
               this.movementScheme.setBaseModifier(0.75F);
               canTriggerPlonk = true;
            }
         } else if (this.carriedWeight < this.encumbered) {
            if (this.carriedWeight + weight >= this.cantmove) {
               this.movementScheme.setEncumbered(true);
               this.movementScheme.setBaseModifier(0.05F);
               this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
               canTriggerPlonk = true;
            } else if (this.carriedWeight + weight >= this.encumbered) {
               this.movementScheme.setBaseModifier(0.25F);
               canTriggerPlonk = true;
            }
         } else if (this.carriedWeight < this.cantmove && this.carriedWeight + weight >= this.cantmove) {
            this.movementScheme.setEncumbered(true);
            this.movementScheme.setBaseModifier(0.05F);
            this.getCommunicator().sendAlertServerMessage("You are encumbered and move extremely slow.");
            canTriggerPlonk = true;
         }

         if (canTriggerPlonk && !PlonkData.ENCUMBERED.hasSeenThis(this)) {
            PlonkData.ENCUMBERED.trigger(this);
         }

         this.carriedWeight += weight;
         if (this.getVehicle() != -10L) {
            Creature c = Creatures.getInstance().getCreatureOrNull(this.getVehicle());
            if (c != null) {
               c.ignoreSaddleDamage = true;
               c.getMovementScheme().update();
            }
         }
      } else {
         this.carriedWeight += weight;
         this.ignoreSaddleDamage = true;
         this.movementScheme.update();
      }
   }

   public boolean removeCarriedWeight(int weight) {
      if (this.isPlayer()) {
         if (this.carriedWeight >= this.cantmove) {
            if (this.carriedWeight - weight < this.moveslow) {
               this.movementScheme.setEncumbered(false);
               this.movementScheme.setBaseModifier(1.0F);
               this.getCommunicator().sendAlertServerMessage("You can now move again.");
            } else if (this.carriedWeight - weight < this.encumbered) {
               this.movementScheme.setEncumbered(false);
               this.movementScheme.setBaseModifier(0.75F);
               this.getCommunicator().sendAlertServerMessage("You can now move again.");
            } else if (this.carriedWeight - weight < this.cantmove) {
               this.movementScheme.setEncumbered(false);
               this.movementScheme.setBaseModifier(0.25F);
               this.getCommunicator().sendAlertServerMessage("You can now move again.");
            }
         } else if (this.carriedWeight >= this.encumbered) {
            if (this.carriedWeight - weight < this.moveslow) {
               this.movementScheme.setEncumbered(false);
               this.movementScheme.setBaseModifier(1.0F);
            } else if (this.carriedWeight - weight < this.encumbered) {
               this.movementScheme.setEncumbered(false);
               this.movementScheme.setBaseModifier(0.75F);
            }
         } else if (this.carriedWeight >= this.moveslow && this.carriedWeight - weight < this.moveslow) {
            this.movementScheme.setEncumbered(false);
            this.movementScheme.setBaseModifier(1.0F);
         }

         this.carriedWeight -= weight;
         if (this.getVehicle() != -10L) {
            Creature c = Creatures.getInstance().getCreatureOrNull(this.getVehicle());
            if (c != null) {
               c.ignoreSaddleDamage = true;
               c.getMovementScheme().update();
            }
         }
      } else {
         this.carriedWeight -= weight;
         this.ignoreSaddleDamage = true;
         this.movementScheme.update();
      }

      if (this.carriedWeight < 0) {
         logger.log(Level.WARNING, "Carried weight is less than 0 for " + this);
         if (this instanceof Player) {
            logger.log(
               Level.INFO,
               this.name
                  + " now carries "
                  + this.carriedWeight
                  + " AFTER removing "
                  + weight
                  + " gs. Modifier is:"
                  + this.movementScheme.getSpeedModifier()
                  + "."
            );
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean canCarry(int weight) {
      try {
         if (this.getPower() > 1) {
            return true;
         } else {
            Skill strength = this.skills.getSkill(102);
            return strength.getKnowledge(0.0) * 7000.0 > (double)(weight + this.carriedWeight);
         }
      } catch (NoSuchSkillException var3) {
         logger.log(Level.WARNING, "No strength skill for " + this);
         return false;
      }
   }

   public int getCarryCapacityFor(int weight) {
      try {
         Skill strength = this.skills.getSkill(102);
         return (int)(strength.getKnowledge(0.0) * 7000.0 - (double)this.carriedWeight) / weight;
      } catch (NoSuchSkillException var3) {
         logger.log(Level.WARNING, "No strength skill for " + this);
         return 0;
      }
   }

   public int getCarriedWeight() {
      return this.carriedWeight;
   }

   public int getSaddleBagsCarriedWeight() {
      for(Item i : this.getBody().getAllItems()) {
         if (i.isSaddleBags()) {
            float mod = 0.5F;
            if (i.getTemplateId() == 1334) {
               mod = 0.6F;
            }

            return (int)((float)i.getFullWeight() * mod);
         }
      }

      return 0;
   }

   public int getCarryingCapacityLeft() {
      try {
         Skill strength = this.skills.getSkill(102);
         return (int)(strength.getKnowledge(0.0) * 7000.0) - this.carriedWeight;
      } catch (NoSuchSkillException var2) {
         logger.log(Level.WARNING, "No strength skill for " + this);
         return 0;
      }
   }

   public void setTeleportPoints(short x, short y, int layer, int floorLevel) {
      this.setTeleportPoints((float)(x << 2) + 2.0F, (float)(y << 2) + 2.0F, layer, floorLevel);
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
      } else {
         if (this.status != null) {
            int tilex = this.getTileX();
            int tiley = this.getTileY();

            try {
               Zone zone = Zones.getZone(tilex, tiley, this.isOnSurface());
               this.currentTile = zone.getOrCreateTile(tilex, tiley);
               return this.currentTile;
            } catch (NoSuchZoneException var4) {
            }
         }

         return null;
      }
   }

   public int getCurrentTileNum() {
      int tilex = this.getTileX();
      int tiley = this.getTileY();
      return this.isOnSurface() ? Server.surfaceMesh.getTile(tilex, tiley) : Server.caveMesh.getTile(tilex, tiley);
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
            this.getTrade()
               .creatureOne
               .getCommunicator()
               .sendAlertServerMessage("You took too long to trade and " + this.getName() + " takes care of another customer.");
            this.getTrade().end(this, false);
            return true;
         }

         if (this.getTrade().creatureTwo != null && this.getTrade().creatureTwo.isPlayer() && this.getTrade().creatureTwo.shouldStopTrading(false)) {
            this.getTrade()
               .creatureTwo
               .getCommunicator()
               .sendAlertServerMessage("You took too long to trade and " + this.getName() + " takes care of another customer.");
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
      } else {
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
         } catch (NoSuchZoneException var11) {
            logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + tileX + ", " + tileY);
         } catch (NoSuchCreatureException var12) {
            logger.log(Level.WARNING, this + " creature doesn't exist?", (Throwable)var12);
         } catch (NoSuchPlayerException var13) {
            logger.log(Level.WARNING, this + " player doesn't exist?", (Throwable)var13);
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
               this.status
                  .setPositionZ(
                     Math.max(
                        Zones.calculateHeight(this.teleportX, this.teleportY, this.isOnSurface()) + this.mountAction.getOffZ(), this.mountAction.getOffZ()
                     )
                  );
               this.getMovementScheme().offZ = this.mountAction.getOffZ();
            } else {
               VolaTile targetTile = Zones.getTileOrNull((int)(this.teleportX / 4.0F), (int)(this.teleportY / 4.0F), this.teleportLayer >= 0);
               float height = this.teleportFloorLevel > 0 ? (float)(this.teleportFloorLevel * 3) : 0.0F;
               if (targetTile != null) {
                  this.getMovementScheme().setGroundOffset((int)(height * 10.0F), true);
                  this.calculateFloorLevel(targetTile, true);
               }

               this.status.setPositionZ(Zones.calculateHeight(this.teleportX, this.teleportY, this.isOnSurface()) + height);
            }
         } catch (NoSuchZoneException var10) {
            logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + this.teleportX + ", " + this.teleportY);
         }

         this.getMovementScheme().setPosition(this.teleportX, this.teleportY, this.status.getPositionZ(), this.status.getRotation(), this.getLayer());
         this.getMovementScheme().haltSpeedModifier();
         boolean zoneExists = true;

         try {
            this.status
               .savePosition(
                  this.getWurmId(), this.isPlayer(), Zones.getZoneIdFor((int)this.teleportX >> 2, (int)this.teleportY >> 2, this.isOnSurface()), true
               );
         } catch (IOException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         } catch (NoSuchZoneException var9) {
            logger.log(
               Level.INFO, this.getName() + " no zone at " + ((int)this.teleportX >> 2) + ", " + ((int)this.teleportY >> 2) + ", surf=" + this.isOnSurface()
            );
            zoneExists = false;
         }

         try {
            if (zoneExists) {
               Zones.getZone((int)this.teleportX >> 2, (int)this.teleportY >> 2, this.isOnSurface()).addCreature(this.id);
            }

            Server.getInstance().addCreatureToPort(this);
         } catch (Exception var7) {
            logger.log(Level.WARNING, this.getName() + " failed to recreate vision area after teleporting: " + var7.getMessage());
         }

         return true;
      }
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
            } catch (Exception var4) {
               logger.log(Level.WARNING, "Failed to create visionArea:" + var4.getMessage(), (Throwable)var4);
            }

            Server.getInstance().addCreatureToPort(this);
         } catch (NoSuchZoneException var5) {
            logger.log(Level.WARNING, this.getName() + " tried to teleport to nonexistant zone at " + this.getTileX() + ", " + this.getTileY());
         } catch (NoSuchCreatureException var6) {
            logger.log(Level.WARNING, "This creature doesn't exist?", (Throwable)var6);
         } catch (NoSuchPlayerException var7) {
            logger.log(Level.WARNING, "This player doesn't exist?", (Throwable)var7);
         }
      }

      this.addingAfterTeleport = false;
      this.stopTeleporting();
   }

   public void cancelTeleport() {
      this.teleportX = -1.0F;
      this.teleportY = -1.0F;
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
         this.teleportX = -1.0F;
         this.teleportY = -1.0F;
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
               } catch (Exception var2) {
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
      return this.status.getSex() == 127 ? this.template.getSex() : this.status.getSex();
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
         if (this.spawnWeapon != null && this.spawnWeapon.length() > 0) {
            TestQuestion.createAndInsertItems(this, 319, 319, 40.0F, true, (byte)-1);

            try {
               int w = Integer.parseInt(this.spawnWeapon);
               int lTemplate = 0;
               boolean shield = false;
               switch(w) {
                  case 1:
                     lTemplate = 21;
                     shield = true;
                     break;
                  case 2:
                     lTemplate = 81;
                     break;
                  case 3:
                     lTemplate = 90;
                     shield = true;
                     break;
                  case 4:
                     lTemplate = 87;
                     break;
                  case 5:
                     lTemplate = 292;
                     shield = true;
                     break;
                  case 6:
                     lTemplate = 290;
                     break;
                  case 7:
                     lTemplate = 706;
                     break;
                  case 8:
                     lTemplate = 705;
               }

               if (lTemplate > 0) {
                  try {
                     TestQuestion.createAndInsertItems(this, lTemplate, lTemplate, 40.0F, true, (byte)-1);
                     if (shield) {
                        TestQuestion.createAndInsertItems(this, 84, 84, 40.0F, true, (byte)-1);
                     }
                  } catch (Exception var6) {
                     logger.log(Level.INFO, "Failed to create item for spawning.", (Throwable)var6);
                     this.getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
                  }
               }
            } catch (Exception var7) {
               this.getCommunicator().sendAlertServerMessage("Failed to spawn weapon.");
            }
         }

         this.spawnWeapon = null;
         if (this.spawnArmour != null && this.spawnArmour.length() > 0) {
            try {
               int arm = Integer.parseInt(this.spawnArmour);
               float ql = 20.0F;
               byte matType = -1;
               switch(arm) {
                  case 1:
                     ql = 40.0F;
                     TestQuestion.createAndInsertItems(this, 274, 279, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 278, 278, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 274, 274, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 277, 277, ql, true, (byte)-1);
                     break;
                  case 2:
                     ql = 60.0F;
                     TestQuestion.createAndInsertItems(this, 103, 108, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 103, 103, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 105, 105, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 106, 106, ql, true, (byte)-1);
                     break;
                  case 3:
                     ql = 20.0F;
                     TestQuestion.createAndInsertItems(this, 280, 287, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 284, 284, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 280, 280, ql, true, (byte)-1);
                     TestQuestion.createAndInsertItems(this, 283, 283, ql, true, (byte)-1);
               }
            } catch (Exception var5) {
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
         this.keys = new HashSet<>();
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
            for(Door lDoor : doors) {
               lDoor.updateDoor(this, key, removedKey);
            }
         }
      } else {
         logger.log(Level.WARNING, this.getName() + " was on null tile.", (Throwable)(new Exception()));
      }
   }

   public void updateGates() {
      VolaTile t = this.getCurrentTile();
      if (t != null) {
         Door[] doors = t.getDoors();
         if (doors != null) {
            for(Door lDoor : doors) {
               lDoor.removeCreature(this);
               if (lDoor.covers(this.getPosX(), this.getPosY(), this.getPositionZ(), this.getFloorLevel(), this.followsGround())) {
                  lDoor.addCreature(this);
               }
            }
         }
      } else {
         logger.log(Level.WARNING, this.getName() + " was on null tile.", (Throwable)(new Exception()));
      }
   }

   public boolean unlockItems(Item key, Item[] items) {
      for(Item lItem : items) {
         if (lItem.isLockable() && lItem.getLockId() != -10L) {
            try {
               Item lock = Items.getItem(lItem.getLockId());
               long[] keyarr = lock.getKeyIds();

               for(long lElement : keyarr) {
                  if (lElement == key.getWurmId()) {
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
            } catch (NoSuchItemException var14) {
               logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
            }
         }
      }

      return false;
   }

   public boolean lockItems(Item key, Item[] items) {
      boolean stillUnlocked = false;

      for(Item lItem : items) {
         if (lItem.isLockable() && lItem.getLockId() != -10L) {
            try {
               Item lock = Items.getItem(lItem.getLockId());
               long[] keyarr = lock.getKeyIds();
               boolean thisLock = false;

               for(long lElement : keyarr) {
                  for(Item key2 : this.keys) {
                     if (lElement == key2.getWurmId()) {
                        stillUnlocked = true;
                     }
                  }

                  if (lElement == key.getWurmId()) {
                     thisLock = true;
                  }
               }

               if (thisLock && !stillUnlocked) {
                  for(Item item : lItem.getItems()) {
                     item.removeWatcher(this, true);
                  }

                  return true;
               }

               if (thisLock) {
                  return true;
               }
            } catch (NoSuchItemException var18) {
               logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
            }
         }
      }

      return false;
   }

   public boolean hasKeyForLock(Item lock) {
      if (lock.getWurmId() == this.getWurmId()) {
         return true;
      } else if (this.keys != null && !this.keys.isEmpty()) {
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

         long[] keyarr = lock.getKeyIds();

         for(long lElement : keyarr) {
            for(Item key : this.keys) {
               if (lElement == key.getWurmId()) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean hasAllKeysForLock(Item lock) {
      for(long aKey : lock.getKeyIds()) {
         boolean foundit = false;

         for(Item key : this.getKeys()) {
            if (aKey == key.getWurmId()) {
               foundit = true;
               break;
            }
         }

         if (!foundit) {
            return false;
         }
      }

      return true;
   }

   public Item[] getKeys() {
      Item[] toReturn = new Item[0];
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
         if (!this.isPlayer() && !removeFromTile) {
            this.getStatus().setLayer(layer);
            this.getCurrentTile().newLayer(this);
         } else {
            if (this.currentTile != null) {
               if (!(this instanceof Player)) {
                  this.setPositionZ(
                     Zones.calculatePosZ(
                        this.getPosX(),
                        this.getPosY(),
                        this.getCurrentTile(),
                        this.isOnSurface(),
                        this.isFloating(),
                        this.getPositionZ(),
                        this,
                        this.getBridgeId()
                     )
                  );
               }

               this.getStatus().setLayer(layer);
               if (this.getVehicle() != -10L && this.isVehicleCommander()) {
                  Vehicle vehic = Vehicles.getVehicleForId(this.getVehicle());
                  if (vehic != null) {
                     boolean ok = true;
                     if (vehic.creature) {
                        try {
                           Creature cretVehicle = Server.getInstance().getCreature(vehic.wurmid);
                           if (layer < 0) {
                              int tile = Server.caveMesh.getTile(cretVehicle.getTileX(), cretVehicle.getTileY());
                              if (!Tiles.isSolidCave(Tiles.decodeType(tile))) {
                                 cretVehicle.setLayer(layer, false);
                              }
                           } else {
                              cretVehicle.setLayer(layer, false);
                           }
                        } catch (NoSuchCreatureException var16) {
                           logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + var16.getMessage(), (Throwable)var16);
                        } catch (NoSuchPlayerException var17) {
                           logger.log(Level.WARNING, this + ", cannot get creature for vehicle: " + vehic + " due to " + var17.getMessage(), (Throwable)var17);
                        }
                     } else {
                        try {
                           Item itemVehicle = Items.getItem(vehic.wurmid);
                           if (layer < 0) {
                              int caveTile = Server.caveMesh.getTile((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2);
                              if (Tiles.isSolidCave(Tiles.decodeType(caveTile))) {
                                 ok = false;
                              }
                           }

                           if (ok) {
                              itemVehicle.newLayer = (byte)layer;
                              Zone zone = null;

                              try {
                                 zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, itemVehicle.isOnSurface());
                                 zone.removeItem(itemVehicle, true, true);
                              } catch (NoSuchZoneException var15) {
                                 logger.log(
                                    Level.WARNING,
                                    itemVehicle.getName()
                                       + " this shouldn't happen: "
                                       + var15.getMessage()
                                       + " at "
                                       + ((int)itemVehicle.getPosX() >> 2)
                                       + ", "
                                       + ((int)itemVehicle.getPosY() >> 2),
                                    (Throwable)var15
                                 );
                              }

                              try {
                                 zone = Zones.getZone((int)itemVehicle.getPosX() >> 2, (int)itemVehicle.getPosY() >> 2, layer >= 0);
                                 zone.addItem(itemVehicle, false, false, false);
                              } catch (NoSuchZoneException var14) {
                                 logger.log(
                                    Level.WARNING,
                                    itemVehicle.getName()
                                       + " this shouldn't happen: "
                                       + var14.getMessage()
                                       + " at "
                                       + ((int)itemVehicle.getPosX() >> 2)
                                       + ", "
                                       + ((int)itemVehicle.getPosY() >> 2),
                                    (Throwable)var14
                                 );
                              }

                              itemVehicle.newLayer = -128;
                              Seat[] seats = vehic.hitched;
                              if (seats != null) {
                                 for(int x = 0; x < seats.length; ++x) {
                                    if (seats[x] != null) {
                                       if (seats[x].occupant != -10L) {
                                          try {
                                             Creature c = Server.getInstance().getCreature(seats[x].occupant);
                                             c.getStatus().setLayer(layer);
                                             c.getCurrentTile().newLayer(c);
                                          } catch (NoSuchPlayerException var12) {
                                             logger.log(Level.WARNING, this.getName() + " " + var12.getMessage(), (Throwable)var12);
                                          } catch (NoSuchCreatureException var13) {
                                             logger.log(Level.WARNING, this.getName() + " " + var13.getMessage(), (Throwable)var13);
                                          }
                                       }
                                    } else {
                                       logger.log(Level.WARNING, this.getName() + " " + vehic.name + ": lacking seat " + x, (Throwable)(new Exception()));
                                    }
                                 }
                              }
                           }
                        } catch (NoSuchItemException var18) {
                           logger.log(Level.WARNING, this.getName() + " " + var18.getMessage(), (Throwable)var18);
                        }
                     }

                     if (ok) {
                        Seat[] seats = vehic.seats;
                        if (seats != null) {
                           for(int x = 0; x < seats.length; ++x) {
                              if (x > 0) {
                                 if (seats[x] != null) {
                                    if (seats[x].occupant != -10L) {
                                       try {
                                          Creature c = Server.getInstance().getCreature(seats[x].occupant);
                                          c.getStatus().setLayer(layer);
                                          c.getCurrentTile().newLayer(c);
                                          if (c.isPlayer()) {
                                             if (c.isOnSurface()) {
                                                c.getCommunicator().sendNormalServerMessage("You leave the cave.");
                                             } else {
                                                c.getCommunicator().sendNormalServerMessage("You enter the cave.");
                                                if (c.getVisionArea() != null) {
                                                   c.getVisionArea().initializeCaves();
                                                }
                                             }
                                          }
                                       } catch (NoSuchPlayerException var10) {
                                          logger.log(Level.WARNING, this.getName() + " " + var10.getMessage(), (Throwable)var10);
                                       } catch (NoSuchCreatureException var11) {
                                          logger.log(Level.WARNING, this.getName() + " " + var11.getMessage(), (Throwable)var11);
                                       }
                                    }
                                 } else {
                                    logger.log(Level.WARNING, this.getName() + " " + vehic.name + ": lacking seat " + x, (Throwable)(new Exception()));
                                 }
                              }
                           }
                        }
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

                  for(int gx = 0; gx < guards.length; ++gx) {
                     if (guards[gx].getCreature().isWithinDistanceTo(this, 20.0F) && this.visibilityCheck(guards[gx].getCreature(), 0.0F)) {
                        v.checkIfRaiseAlert(this);
                        break;
                     }
                  }
               }
            }
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
      this.setRotation(normalizeAngle(newRot));
      this.moved(0.0F, 0.0F, 0.0F, 0, 0);
   }

   public void turnBy(float turnAmount) {
      this.setRotation(normalizeAngle(this.status.getRotation() + turnAmount));
      this.moved(0.0F, 0.0F, 0.0F, 0, 0);
   }

   public void submerge() {
      try {
         float lOldPosZ = this.getPositionZ();
         float lNewPosZ = this.isFloating() ? this.template.offZ : Zones.calculateHeight(this.getPosX(), this.getPosY(), true) / 2.0F;
         this.moved(0.0F, 0.0F, lNewPosZ - lOldPosZ, 0, 0);
      } catch (NoSuchZoneException var3) {
      }
   }

   public void surface() {
      float lOldPosZ = this.getPositionZ();
      float lNewPosZ = this.isFloating() ? this.template.offZ : -1.25F;
      this.setPositionZ(lNewPosZ);
      this.moved(0.0F, 0.0F, lNewPosZ - lOldPosZ, 0, 0);
   }

   public void almostSurface() {
      float _oldPosZ = this.getPositionZ();
      float _newPosZ = -2.0F;
      this.setPositionZ(-2.0F);
      this.moved(0.0F, 0.0F, -2.0F - _oldPosZ, 0, 0);
   }

   public void setCommunicator(Communicator comm) {
      this.communicator = comm;
   }

   public void loadPossessions(long inventoryId) throws Exception {
      try {
         this.possessions = new Possessions(this, inventoryId);
      } catch (Exception var4) {
         logger.log(Level.INFO, var4.getMessage(), (Throwable)var4);
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
      if (Math.abs(this.getStatus().getPositionX() - _target.getStatus().getPositionX())
         > Math.abs(this.getStatus().getPositionY() - _target.getStatus().getPositionY())) {
         return Math.abs(this.getStatus().getPositionX() - _target.getStatus().getPositionX()) < 8.0F;
      } else {
         return Math.abs(this.getStatus().getPositionY() - _target.getStatus().getPositionY()) < 8.0F;
      }
   }

   public static final int rangeTo(Creature performer, Creature target) {
      return Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX())
            > Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY())
         ? (int)Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX())
         : (int)Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY());
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
      float min = 10000.0F;
      int index = -1;

      for(int i = 0; i < points.length; ++i) {
         if (canIgnore) {
            boolean doIgnore = false;

            for(int x = 0; x < ignore.length; ++x) {
               if (points[i] == ignore[x]) {
                  doIgnore = true;
               }
            }

            if (doIgnore) {
               continue;
            }
         }

         float len = pos.subtract(points[i]).length();
         if (len < min) {
            index = i;
            min = len;
         }
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
         Vector3f cpos = new Vector3f(center.x, center.y, 1.0F);
         float rotation = target.getStatus().getRotation();
         Vector3f mp1 = new Vector3f(minX, minY, 0.0F);
         Vector3f mp2 = new Vector3f(maxX, maxY, 0.0F);
         BoxMatrix M = new BoxMatrix(true);
         BoundBox box = new BoundBox(M, mp1, mp2);
         box.M.translate(cpos);
         box.M.rotate(rotation + 180.0F, false, false, true);
         Vector3f ppos = new Vector3f(PX, PY, 0.5F);
         return box.isPointInBox(ppos) ? box.distOutside(ppos, cpos) * 10.0F : box.distOutside(ppos, cpos) * 10.0F;
      } else {
         return Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX())
               > Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY())
            ? Math.abs(performer.getStatus().getPositionX() - target.getStatus().getPositionX()) * 10.0F
            : Math.abs(performer.getStatus().getPositionY() - target.getStatus().getPositionY()) * 10.0F;
      }
   }

   public static int rangeTo(Creature performer, Item aTarget) {
      return Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX()) > Math.abs(performer.getStatus().getPositionY() - aTarget.getPosY())
         ? (int)Math.abs(performer.getStatus().getPositionX() - aTarget.getPosX())
         : (int)Math.abs(performer.getStatus().getPositionY() - aTarget.getPosY());
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
         this.followers = new HashMap<>();
      }

      this.followers.put(follower, leadingItem);
   }

   public Creature[] getFollowers() {
      return this.followers != null && this.followers.size() != 0 ? this.followers.keySet().toArray(new Creature[this.followers.size()]) : emptyCreatures;
   }

   public final int getNumberOfFollowers() {
      return this.followers == null ? 0 : this.followers.size();
   }

   public void stopLeading() {
      if (this.followers != null) {
         Creature[] followArr = this.followers.keySet().toArray(new Creature[this.followers.size()]);

         for(Creature lElement : followArr) {
            lElement.setLeader(null);
         }

         this.followers = null;
      }
   }

   public boolean mayLeadMoreCreatures() {
      return this.followers == null || this.followers.size() < 10;
   }

   public final boolean isLeading(Creature checked) {
      for(Creature c : this.getFollowers()) {
         for(Creature c2 : this.getFollowers()) {
            for(Creature c3 : this.getFollowers()) {
               for(Creature c4 : this.getFollowers()) {
                  for(Creature c5 : this.getFollowers()) {
                     for(Creature c6 : this.getFollowers()) {
                        for(Creature c7 : this.getFollowers()) {
                           if (c7.getWurmId() == checked.getWurmId()) {
                              return true;
                           }
                        }

                        if (c6.getWurmId() == checked.getWurmId()) {
                           return true;
                        }
                     }

                     if (c5.getWurmId() == checked.getWurmId()) {
                        return true;
                     }
                  }

                  if (c4.getWurmId() == checked.getWurmId()) {
                     return true;
                  }
               }

               if (c3.getWurmId() == checked.getWurmId()) {
                  return true;
               }
            }

            if (c2.getWurmId() == checked.getWurmId()) {
               return true;
            }
         }

         if (c.getWurmId() == checked.getWurmId()) {
            return true;
         }
      }

      return false;
   }

   public void setLeader(@Nullable Creature leadingCreature) {
      if (leadingCreature == this) {
         logger.log(Level.WARNING, this.getName() + " tries to lead itself at ", (Throwable)(new Exception()));
      } else {
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
      } catch (NoSuchZoneException var2) {
         logger.log(Level.WARNING, this.getName() + " " + var2.getMessage(), (Throwable)var2);
      } catch (NoSuchPlayerException var3) {
         logger.log(Level.WARNING, this.getName() + " " + var3.getMessage(), (Throwable)var3);
      } catch (NoSuchCreatureException var4) {
         logger.log(Level.WARNING, this.getName() + " " + var4.getMessage(), (Throwable)var4);
      }
   }

   public static final double getRange(Creature performer, double targetX, double targetY) {
      double diffx = Math.abs((double)performer.getPosX() - targetX);
      double diffy = Math.abs((double)performer.getPosY() - targetY);
      return Math.sqrt(diffx * diffx + diffy * diffy);
   }

   public static final double getTileRange(Creature performer, int targetX, int targetY) {
      double diffx = (double)Math.abs(performer.getTileX() - targetX);
      double diffy = (double)Math.abs(performer.getTileY() - targetY);
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
      return Math.abs(this.getStatus().getPositionX() - targetCret.getPosX()) <= maxDist
         && Math.abs(this.getStatus().getPositionY() - targetCret.getPosY()) <= maxDist;
   }

   public boolean isWithinDistanceTo(float aPosX, float aPosY, float aPosZ, float maxDist, float modifier) {
      return Math.abs(this.getStatus().getPositionX() - (aPosX + modifier)) < maxDist
         && Math.abs(this.getStatus().getPositionY() - (aPosY + modifier)) < maxDist;
   }

   public boolean isWithinDistanceToZ(float aPosZ, float maxDist, boolean addHalfHeight) {
      return Math.abs(this.getStatus().getPositionZ() + (addHalfHeight ? (float)this.getHalfHeightDecimeters() / 10.0F : 0.0F) - aPosZ) < maxDist;
   }

   public boolean isWithinDistanceTo(int aPosX, int aPosY, int maxDistance) {
      return Math.abs(this.getTileX() - aPosX) <= maxDistance && Math.abs(this.getTileY() - aPosY) <= maxDistance;
   }

   public void creatureMoved(Creature creature, float diffX, float diffY, float diffZ) {
      if (this.leader != null && this.leader.equals(creature) && !this.isRidden() && (diffX != 0.0F || diffY != 0.0F)) {
         this.followLeader();
      }

      if (this.isTypeFleeing()) {
         if (creature.isPlayer() && this.isBred()) {
            return;
         }

         if (creature.isPlayer() || creature.isAggHuman() || creature.isHuman() || creature.isCarnivore() || creature.isMonster()) {
            Vector2f mypos = new Vector2f(this.getPosX(), this.getPosY());
            float oldDistance = new Vector2f(creature.getPosX() - diffX, creature.getPosY() - diffY).distance(mypos);
            float newDistance = new Vector2f(creature.getPosX(), creature.getPosY()).distance(mypos);
            if (oldDistance > newDistance) {
               if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                  int baseCounter = (int)(Math.max(1.0F, creature.getBaseCombatRating() - this.getBaseCombatRating()) * 5.0F);
                  if ((float)baseCounter - newDistance > 0.0F) {
                     this.setFleeCounter((int)Math.min(60.0F, Math.max(3.0F, (float)baseCounter - newDistance)));
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
      } else {
         return this.status.getSex() == 1 ? "her" : "its";
      }
   }

   public String getHimHerItString() {
      if (this.status.getSex() == 0) {
         return "him";
      } else {
         return this.status.getSex() == 1 ? "her" : "it";
      }
   }

   public boolean mayAttack(@Nullable Creature cret) {
      return this.status.getStunned() <= 0.0F && !this.status.isUnconscious();
   }

   public boolean isStunned() {
      return this.status.getStunned() > 0.0F;
   }

   public boolean isUnconscious() {
      return this.status.isUnconscious();
   }

   public String getHeSheItString() {
      if (this.status.getSex() == 0) {
         return "he";
      } else {
         return this.status.getSex() == 1 ? "she" : "it";
      }
   }

   public void stopCurrentAction() {
      try {
         String toSend = this.actions.stopCurrentAction(false);
         if (toSend.length() > 0) {
            this.communicator.sendNormalServerMessage(toSend);
         }

         this.sendActionControl("", false, 0);
      } catch (NoSuchActionException var2) {
      }
   }

   public void maybeInterruptAction(int damage) {
      try {
         Action act = this.actions.getCurrentAction();
         if (act.isVulnerable()
            && act.getNumber() != Spells.SPELL_CHARM_ANIMAL.number
            && act.getNumber() != Spells.SPELL_DOMINATE.number
            && this.getBodyControlSkill().skillCheck((double)((float)damage / 100.0F), (double)this.zoneBonus, false, 1.0F) < 0.0) {
            String toSend = this.actions.stopCurrentAction(false);
            if (toSend.length() > 0) {
               this.communicator.sendNormalServerMessage(toSend);
            }

            this.sendActionControl("", false, 0);
         }
      } catch (NoSuchActionException var4) {
      }
   }

   public float getCombatDamage(Item bodyPart) {
      short pos = bodyPart.getPlace();
      if (pos == 13 || pos == 14) {
         return this.getHandDamage();
      } else if (pos == 34) {
         return this.getKickDamage();
      } else if (pos == 1) {
         return this.getHeadButtDamage();
      } else if (pos == 29) {
         return this.getBiteDamage();
      } else {
         return pos == 2 ? this.getBreathDamage() : 0.0F;
      }
   }

   public String getAttackStringForBodyPart(Item bodypart) {
      if (bodypart.getPlace() == 13 || bodypart.getPlace() == 14) {
         return this.template.getHandDamString();
      } else if (bodypart.getPlace() == 34) {
         return this.template.getKickDamString();
      } else if (bodypart.getPlace() == 29) {
         return this.template.getBiteDamString();
      } else if (bodypart.getPlace() == 1) {
         return this.template.getHeadButtDamString();
      } else {
         return bodypart.getPlace() == 2 ? this.template.getBreathDamString() : this.template.getHandDamString();
      }
   }

   public float getBodyWeaponSpeed(Item bodypart) {
      float size = (float)this.template.getSize();
      if (bodypart.getPlace() == 13 || bodypart.getPlace() == 14) {
         return size + 1.0F;
      } else if (bodypart.getPlace() == 34) {
         return size + 2.0F;
      } else if (bodypart.getPlace() == 29) {
         return size + 2.5F;
      } else if (bodypart.getPlace() == 1) {
         return size + 3.0F;
      } else {
         return bodypart.getPlace() == 2 ? size + 3.5F : 4.0F;
      }
   }

   public Item getArmour(byte location) throws NoArmourException, NoSpaceException {
      Item bodyPart = null;

      try {
         boolean barding = this.isHorse();
         if (barding) {
            bodyPart = this.status.getBody().getBodyPart(2);
         } else {
            bodyPart = this.status.getBody().getBodyPart(location);
         }

         if (location == 29) {
            return this.getArmour((byte)1);
         }

         for(Item item : bodyPart.getItems()) {
            if (item.isArmour()) {
               byte[] spaces = item.getBodySpaces();

               for(byte lSpace : spaces) {
                  if (lSpace == location || barding) {
                     return item;
                  }
               }
            }
         }
      } catch (NoArmourException var12) {
         throw var12;
      } catch (Exception var13) {
         throw new NoSpaceException(var13);
      }

      throw new NoArmourException("No armour worn on bodypart " + location);
   }

   public Item getCarriedItem(int itemTemplateId) {
      Item inventory = this.getInventory();
      Item[] items = inventory.getAllItems(false);

      for(Item lItem : items) {
         if (lItem.getTemplateId() == itemTemplateId) {
            return lItem;
         }
      }

      Item body = this.getBody().getBodyItem();
      items = body.getAllItems(false);

      for(Item lItem : items) {
         if (lItem.getTemplateId() == itemTemplateId) {
            return lItem;
         }
      }

      return null;
   }

   public Item getEquippedItem(byte location) throws NoSpaceException {
      try {
         for(Item item : this.status.getBody().getBodyPart(location).getItems()) {
            if (!item.isArmour() && !item.isBodyPartAttached()) {
               return item;
            }
         }
      } catch (NullPointerException var5) {
         if (this.status == null) {
            logger.log(Level.WARNING, "status is null for creature" + this.getName(), (Throwable)var5);
         } else if (this.status.getBody() == null) {
            logger.log(Level.WARNING, "body is null for creature" + this.getName(), (Throwable)var5);
         } else if (this.status.getBody().getBodyPart(location) == null) {
            logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + this.getName(), (Throwable)var5);
         } else {
            logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + this.getName(), (Throwable)var5);
         }
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
         } else if ((bodyPart.getPlace() == 37 || bodyPart.getPlace() == 38 || bodyPart.getPlace() == 13 || bodyPart.getPlace() == 14)
            && (this.isPlayer() || !fetchBodypart)) {
            for(Item item : bodyPart.getItems()) {
               if (!item.isArmour() && !item.isBodyPartAttached() && (Weapon.getBaseDamageForWeapon(item) > 0.0F || item.isWeaponBow() && allowBow)) {
                  return item;
               }
            }

            if (bodyPart.getPlace() == 37 || bodyPart.getPlace() == 38) {
               int handSlot = bodyPart.getPlace() == 37 ? 13 : 14;
               bodyPart = this.status.getBody().getBodyPart(handSlot);
            }

            return bodyPart;
         } else {
            return bodyPart;
         }
      } catch (NullPointerException var8) {
         if (this.status == null) {
            logger.log(Level.WARNING, "status is null for creature" + this.getName(), (Throwable)var8);
         } else if (this.status.getBody() == null) {
            logger.log(Level.WARNING, "body is null for creature" + this.getName(), (Throwable)var8);
         } else if (this.status.getBody().getBodyPart(location) == null) {
            logger.log(Level.WARNING, "body inventoryspace(" + location + ") is null for creature" + this.getName(), (Throwable)var8);
         } else {
            logger.log(Level.WARNING, "seems wornItems for inventoryspace was null for creature" + this.getName(), (Throwable)var8);
         }

         throw new NoSpaceException("No  bodypart " + location, var8);
      }
   }

   public int getTotalInventoryWeightGrams() {
      Body body = this.status.getBody();
      int weight = 0;
      Item[] items = body.getAllItems();

      for(Item lItem : items) {
         weight += lItem.getFullWeight();
      }

      Item[] inventoryItems = this.possessions.getInventory().getAllItems(true);

      for(int x = 0; x < items.length; ++x) {
         weight += inventoryItems[x].getFullWeight();
      }

      return weight;
   }

   public void startPersonalAction(short action, long subject, long _target) {
      try {
         BehaviourDispatcher.action(this, this.communicator, subject, _target, action);
      } catch (FailedException var7) {
      } catch (NoSuchBehaviourException var8) {
      } catch (NoSuchCreatureException var9) {
      } catch (NoSuchItemException var10) {
      } catch (NoSuchPlayerException var11) {
      } catch (NoSuchWallException var12) {
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
            } catch (NoSuchActionException var3) {
            }

            if (lCurrentAction == null || lCurrentAction.getNumber() != 114) {
               BehaviourDispatcher.action(this, this.communicator, -1L, this.opponent.getWurmId(), (short)114);
            } else if (lCurrentAction != null) {
               this.sendToLoggers(
                  "busy "
                     + lCurrentAction.getActionString()
                     + " seconds "
                     + lCurrentAction.getCounterAsFloat()
                     + " "
                     + lCurrentAction.getTarget()
                     + ", path is null:"
                     + (this.status.getPath() == null),
                  (byte)4
               );
            }

            this.status.setPath(null);
         } catch (FailedException var4) {
            this.setOpponent(null);
         } catch (NoSuchBehaviourException var5) {
            this.setTarget(-10L, true);
            this.setOpponent(null);
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         } catch (NoSuchCreatureException var6) {
            this.setTarget(-10L, true);
            this.setOpponent(null);
         } catch (NoSuchItemException var7) {
            this.setTarget(-10L, true);
            this.setOpponent(null);
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         } catch (NoSuchPlayerException var8) {
            this.setTarget(-10L, true);
            this.setOpponent(null);
         } catch (NoSuchWallException var9) {
            this.setOpponent(null);
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      }
   }

   public void attackTarget() {
      if (this.target != -10L && (this.opponent == null || this.opponent.getWurmId() != this.target)) {
         long start = System.nanoTime();
         Creature tg = this.getTarget();
         if (tg == null || !tg.isDead() && !tg.isOffline()) {
            if (this.isDominated() && tg != null && tg.isDominated() && this.getDominator() == tg.getDominator()) {
               this.setTarget(-10L, true);
               this.setOpponent(null);
            } else if (tg != null) {
               if (rangeTo(this, tg) < Actions.actionEntrys[114].getRange()) {
                  if (!this.isPlayer() && tg.getFloorLevel() != this.getFloorLevel()) {
                     if (this.isSpiritGuard()) {
                        this.pushToFloorLevel(this.getTarget().getFloorLevel());
                     } else if (tg.getFloorLevel() != this.getFloorLevel()) {
                        Floor[] floors = this.getCurrentTile()
                           .getFloors(Math.min(this.getFloorLevel(), tg.getFloorLevel()) * 30, Math.max(this.getFloorLevel(), tg.getFloorLevel()) * 30);

                        for(Floor f : floors) {
                           if (tg.getFloorLevel() > this.getFloorLevel()) {
                              if (f.getFloorLevel() == this.getFloorLevel() + 1 && (f.isOpening() && this.canOpenDoors() || f.isStair())) {
                                 this.pushToFloorLevel(f.getFloorLevel());
                                 break;
                              }
                           } else if (f.getFloorLevel() == this.getFloorLevel() && (f.isOpening() && this.canOpenDoors() || f.isStair())) {
                              this.pushToFloorLevel(f.getFloorLevel() - 1);
                              break;
                           }
                        }
                     }
                  }

                  if (tg.getLayer() != this.getLayer() && (!tg.getCurrentTile().isTransition || !this.getCurrentTile().isTransition)) {
                     return;
                  }

                  if (tg != this.opponent && tg.getAttackers() >= tg.getMaxGroupAttackSize()) {
                     ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                     segments.add(new CreatureLineSegment(tg));
                     segments.add(new MulticolorLineSegment(" is too crowded with attackers. You find no space.", (byte)0));
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

                        ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                        segments.add(new CreatureLineSegment(this));
                        segments.add(new MulticolorLineSegment(" try to " + CombatEngine.getAttackString(this, this.getPrimWeapon()) + " ", (byte)0));
                        segments.add(new CreatureLineSegment(tg));
                        segments.add(new MulticolorLineSegment(".", (byte)0));
                        this.getCommunicator().sendColoredMessageCombat(segments);
                        if (!this.isPlayer() && !this.isDominated()) {
                           segments.get(1).setText(" moves in to attack ");
                           tg.getCommunicator().sendColoredMessageCombat(segments);
                        } else {
                           segments.get(1).setText(" tries to " + CombatEngine.getAttackString(this, this.getPrimWeapon()) + " ");
                           tg.getCommunicator().sendColoredMessageCombat(segments);
                           if (this.isDominated() && this.getDominator() != null && this.getDominator().isPlayer()) {
                              this.getDominator().getCommunicator().sendColoredMessageCombat(segments);
                           }
                        }
                     }
                  } else if (!this.isPlayer() && Server.rand.nextInt(50) == 0) {
                     this.setTarget(-10L, true);
                  }
               } else if (this.isSpellCaster()
                  && rangeTo(this, tg) < 24
                  && !this.isPlayer()
                  && tg.getFloorLevel() == this.getFloorLevel()
                  && this.getLayer() == tg.getLayer()
                  && this.getFavor() >= 100.0F
                  && Server.rand.nextInt(10) == 0) {
                  this.setOpponent(tg);
                  short spellAction = 420;
                  switch(this.template.getTemplateId()) {
                     case 110:
                        if (Server.rand.nextInt(3) == 0) {
                           spellAction = 485;
                        }

                        if (Server.rand.nextBoolean()) {
                           spellAction = 414;
                        }
                        break;
                     case 111:
                        if (Server.rand.nextInt(3) == 0) {
                           spellAction = 550;
                        }

                        if (Server.rand.nextBoolean()) {
                           spellAction = 549;
                        }
                        break;
                     default:
                        spellAction = 420;
                  }

                  if (this.opponent != null) {
                     try {
                        long itemId = -10L;

                        try {
                           Item bodyHand = this.getBody().getBodyPart(14);
                           itemId = bodyHand.getWurmId();
                        } catch (Exception var9) {
                           logger.log(Level.INFO, this.getName() + ": No hand.");
                        }

                        if (spellAction != 420 && spellAction != 414) {
                           BehaviourDispatcher.action(this, this.communicator, itemId, this.opponent.getWurmId(), spellAction);
                        } else {
                           BehaviourDispatcher.action(
                              this, this.communicator, itemId, Tiles.getTileId(this.opponent.getTileX(), this.opponent.getTileY(), 0), spellAction
                           );
                        }
                     } catch (Exception var10) {
                        logger.log(Level.INFO, this.getName() + " casting " + spellAction + ":" + var10.getMessage(), (Throwable)var10);
                     }
                  }
               }
            } else {
               this.setTarget(-10L, true);
            }
         } else {
            this.setTarget(-10L, true);
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
      if (this.getBody().getWounds() != null) {
         Wound[] wounds = this.getBody().getWounds().getWounds();
         if (wounds.length > 0) {
            int num = Server.rand.nextInt(wounds.length);
            if (wounds[num].getSeverity() / 1000.0F < (float)power) {
               wounds[num].heal();
               return true;
            }

            wounds[num].modifySeverity(-power * 1000);
            return true;
         }
      }

      return false;
   }

   protected void decreaseOpportunityCounter() {
      if (this.opportunityAttackCounter > 0) {
         --this.opportunityAttackCounter;
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
      } else {
         return null;
      }
   }

   public boolean poll() throws Exception {
      if (this.template.getCreatureAI() != null) {
         boolean toDestroy = this.template.getCreatureAI().pollCreature(this, System.currentTimeMillis() - this.getCreatureAIData().getLastPollTime());
         this.getCreatureAIData().setLastPollTime(System.currentTimeMillis());
         return toDestroy;
      } else {
         if (this.breedTick++ >= 201) {
            this.checkBreedCounter();
            this.breedTick = 0;
         }

         if (this.isNpcTrader() && this.heatCheckTick++ >= 600) {
            this.getInventory().pollCoolingItems(this, 600000L);
            this.heatCheckTick = 0;
         }

         if (!this.isVisibleToPlayers()
            && !this.isTrader()
            && this.lastPolled != 0
            && this.status.getPath() == null
            && this.target == -10L
            && !this.isUnique()
            && !this.isNpc()) {
            --this.lastPolled;
            return false;
         } else {
            if (firstCreature == -10L) {
               firstCreature = this.getWurmId();
            }

            this.lastPolled = pollChecksPer - 1;
            long start = System.nanoTime();

            boolean disease;
            try {
               if (this.fleeCounter > 0) {
                  --this.fleeCounter;
               }

               this.setHugeMoveCounter(this.getHugeMoveCounter() - 1);
               this.decreaseOpportunityCounter();
               if (this.guardSecondsLeft > 0) {
                  --this.guardSecondsLeft;
               }

               if (this.getPathfindCounter() > 100) {
                  if (this.isSpiritGuard()) {
                     logger.log(
                        Level.WARNING,
                        this.getName()
                           + " "
                           + this.getWurmId()
                           + " pathfind "
                           + this.getPathfindCounter()
                           + ". Target was "
                           + this.target
                           + ". Surfaced="
                           + this.isOnSurface()
                     );
                  }

                  this.setPathfindcounter(0);
                  this.setTarget(-10L, true);
                  if (this.isDominated()) {
                     logger.log(Level.WARNING, this.getName() + " was dominated and failed to find path.");
                     if (this.getDominator() != null) {
                        this.getDominator().getCommunicator().sendNormalServerMessage("The " + this.getName() + " fails to follow your orders.");
                     }

                     if (this.decisions != null) {
                        this.decisions.clearOrders();
                     }
                  }
               }

               if (this.getTemplate().getTemplateId() == 88) {
                  if (!WurmCalendar.isNight() && this.getLayer() >= 0) {
                     this.die(false, "Wraith in Daylight");
                     return true;
                  }
               } else if (this.isOutOfBounds()) {
                  this.handleCreatureOutOfBounds();
                  return true;
               }

               if (this.opponentCounter > 0 && this.opponent == null && --this.opponentCounter == 0) {
                  this.lastOpponent = null;
                  this.getCombatHandler().setCurrentStance(-1, (byte)15);
                  this.combatRound = 0;
               }

               this.status.pollDetectInvis();
               if (this.isStunned()) {
                  this.getStatus().setStunned((float)((byte)((int)(this.getStatus().getStunned() - 1.0F))), false);
               }

               if (!this.isDead()) {
                  if (this.getSpellEffects() != null) {
                     this.getSpellEffects().poll();
                  }

                  this.pollNPCChat();
                  if (this.actions.poll(this)) {
                     this.attackTarget();
                     if (this.isFighting()) {
                        this.setFighting();
                     } else if (!this.isDead()) {
                        if (Server.getSecondsUptime() != this.lastSecond) {
                           this.lastSecond = Server.getSecondsUptime();
                           if (!this.isRidden() && this.isNeedFood() && this.canEat() && Server.rand.nextInt(60) == 0) {
                              this.findFood();
                              if (this.hasTrait(7) && Zone.hasSpring(this.getTileX(), this.getTileY()) && Server.rand.nextInt(5) == 0) {
                                 this.frolic();
                              }

                              if (!this.isRidden() && this.hasTrait(12) && Server.rand.nextInt(10) == 0 && this.getLeader() != null) {
                                 Server.getInstance().broadCastAction(this.getName() + " refuses to move on.", this, 5);
                                 this.setLeader(null);
                              }
                           }

                           this.checkStealthing();
                           this.pollNPC();
                           this.checkEggLaying();
                           if (!this.isRidden() && !this.pollAge()) {
                              this.checkMove();
                              this.startUsingPath();
                           }

                           if (this.getStatus().pollFat()) {
                              disease = this.getStatus().disease >= 100;
                              String deathCause = "starvation";
                              if (disease) {
                                 deathCause = "disease";
                              }

                              Server.getInstance()
                                 .broadCastAction(
                                    this.getNameWithGenus()
                                       + " rolls with the eyes, ejects "
                                       + this.getHisHerItsString()
                                       + " tongue and dies from "
                                       + deathCause
                                       + ".",
                                    this,
                                    5
                                 );
                              logger.log(Level.INFO, this.getName() + " dies from " + deathCause + ".");
                              this.die(false, deathCause);
                           } else {
                              this.checkForEnemies();
                           }
                        }
                     } else {
                        logger.log(Level.INFO, this.getName() + " died when attacking?");
                     }
                  }
               }

               if (this.webArmourModTime > 0.0F && this.webArmourModTime-- <= 1.0F) {
                  this.webArmourModTime = 0.0F;
                  if (this.getMovementScheme().setWebArmourMod(false, 0.0F)) {
                     this.getMovementScheme().setWebArmourMod(false, 0.0F);
                  }

                  if (!this.isFighting() && this.fightlevel > 0) {
                     this.fightlevel = (byte)Math.max(0, this.fightlevel - 1);
                     if (this.isPlayer()) {
                        this.getCommunicator().sendFocusLevel(this.getWurmId());
                     }
                  }
               }

               if (System.currentTimeMillis() - this.lastSavedPos > 3600000L) {
                  this.lastSavedPos = System.currentTimeMillis() + (long)(Server.rand.nextInt(3600) * 1000);
                  this.savePosition(this.status.getZoneId());
                  this.getStatus().save();
                  if ((
                        this.getTemplateId() == 78
                           || this.getTemplateId() == 79
                           || this.getTemplateId() == 80
                           || this.getTemplateId() == 81
                           || this.getTemplateId() == 68
                     )
                     && !EpicServerStatus.doesGiveItemMissionExist(this.getWurmId())) {
                     return true;
                  }
               }

               if (this.status.dead) {
                  if (this.respawnCounter > 0) {
                     --this.respawnCounter;
                     if (this.respawnCounter == 0) {
                        float[] xy = Player.findRandomSpawnX(true, true);

                        try {
                           this.setLayer(0, true);
                           this.setPositionX(xy[0]);
                           this.setPositionY(xy[1]);
                           this.setPositionZ(this.calculatePosZ());
                           this.respawn();
                           Zone zone = Zones.getZone(this.getTileX(), this.getTileY(), this.isOnSurface());
                           zone.addCreature(this.getWurmId());
                           this.savePosition(zone.getId());
                           return false;
                        } catch (NoSuchZoneException var14) {
                        } catch (NoSuchCreatureException var15) {
                        } catch (NoSuchPlayerException var16) {
                        } catch (Exception var17) {
                        }
                     }
                  }

                  return true;
               }

               if (this.damageCounter > 0) {
                  --this.damageCounter;
                  if (this.damageCounter <= 0) {
                     this.removeWoundMod();
                     this.getStatus().sendStateString();
                  }
               }

               this.breakout();
               this.pollItems();
               if (this.tradeHandler != null) {
                  this.tradeHandler.balance();
               }

               this.sendItemsTaken();
               this.sendItemsDropped();
               if (this.isVehicle()) {
                  this.pollMount();
               }

               if (this.getBody() != null) {
                  this.getBody().poll();
               } else {
                  logger.log(Level.WARNING, this.getName() + "'s body is null.");
               }

               if (this.template.isMilkable() && !this.canEat() && Server.rand.nextInt(7200) == 0) {
                  this.setMilked(false);
               }

               if (this.template.isWoolProducer()) {
                  if (!this.canEat() && Server.rand.nextInt(14400) == 0) {
                     this.setSheared(false);
                  }
               } else {
                  this.removeRandomItems();
               }

               this.pollStamina();
               this.pollFavor();
               this.pollLoyalty();
               this.trimAttackers(false);
               this.numattackers = 0;
               this.hasAddedToAttack = false;
               if (this.isSpiritGuard() && this.citizenVillage != null && this.target == -10L && this.citizenVillage.targets.size() > 0) {
                  this.citizenVillage.assignTargets();
               }

               if (this.hitchedTo != null || this.isRidden()) {
                  this.goOffline = false;
               }

               if (this.isUnique()
                  || !this.goOffline
                  || this.isFighting()
                  || !this.isDominated()
                  || Players.getInstance().getPlayerOrNull(this.dominator) != null) {
                  return this.isTransferring() || !this.isOnCurrentServer();
               }

               logger.log(Level.INFO, this.getName() + " going offline.");
               Creatures.getInstance().setCreatureOffline(this);
               this.goOffline = false;
               disease = true;
            } finally {
               this.shouldStandStill = false;
               float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
               if (lElapsedTime > (float)LOG_ELAPSED_TIME_THRESHOLD) {
                  logger.info("Polled Creature id, " + this.getWurmId() + ", which took " + lElapsedTime + " millis.");
               }
            }

            return disease;
         }
      }
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
         if (this.leader == null && !this.isDominated() && this.isInTheMoodToBreed(false)) {
            this.checkBreedingPossibility();
         }

         float mod = (float)Servers.localServer.getBreedingTimer();
         if (mod <= 0.0F) {
            mod = 1.0F;
         }

         int base = (int)(84000.0F / mod);
         if (this.checkPregnancy(false)) {
            base = (int)(Servers.isThisAPvpServer() ? 2000.0F / mod : 84000.0F / mod);
            this.forcedBreed = true;
         } else {
            base = (int)(Servers.isThisAPvpServer() ? 900.0F / mod : 2000.0F / mod);
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
         Item[] items = this.currentTile.getItems();

         for(Item lItem : items) {
            if (lItem.isEdibleBy(this)) {
               if (lItem.getTemplateId() != 272) {
                  this.eat(lItem);
                  return;
               }

               if (lItem.isCorpseLootable()) {
                  this.eat(lItem);
                  return;
               }
            }
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
                     str.setKnowledge(str.getKnowledge() - 0.003F, false);
                  }
               } catch (NoSuchSkillException var4) {
                  this.skills.learn(102, 20.0F);
               }

               return false;
            }
         } else if (Server.rand.nextBoolean()) {
            try {
               Skill str = this.skills.getSkill(102);
               double templateStr = this.getTemplate().getSkills().getSkill(102).getKnowledge();
               if (str.getKnowledge() < templateStr) {
                  str.setKnowledge(str.getKnowledge() + 0.03F, false);
               }
            } catch (NoSuchSkillException var5) {
               this.skills.learn(102, 20.0F);
            } catch (Exception var6) {
            }
         }

         int tile = Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley);
         byte type = Tiles.decodeType(tile);
         Village v = Villages.getVillage(this.currentTile.tilex, this.currentTile.tiley, this.currentTile.isOnSurface());
         return !this.hasTrait(22) ? this.grazeNonCorrupt(tile, type, v) : this.grazeCorrupt(tile, type);
      } else {
         return false;
      }
   }

   private boolean grazeCorrupt(int tile, byte type) {
      if (type != Tiles.Tile.TILE_MYCELIUM.id && type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id) {
         return false;
      } else {
         this.getStatus().modifyHunger(-10000, 0.9F);
         if (Server.rand.nextInt(20) == 0) {
            if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
               TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
            } else if (type == Tiles.Tile.TILE_MYCELIUM.id) {
               GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
               if (growthStage == GrassData.GrowthStage.SHORT) {
                  Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
               } else {
                  growthStage = growthStage.getPreviousStage();
                  Server.setSurfaceTile(
                     this.currentTile.tilex,
                     this.currentTile.tiley,
                     Tiles.decodeHeight(tile),
                     Tiles.Tile.TILE_MYCELIUM.id,
                     GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE)
                  );
               }

               Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
            }
         }

         Server.getInstance().broadCastAction(this.getNameWithGenus() + " grazes.", this, 5);
         return true;
      }
   }

   private boolean grazeNonCorrupt(int tile, byte type, Village v) {
      if (type != Tiles.Tile.TILE_GRASS.id
         && type != Tiles.Tile.TILE_FIELD.id
         && type != Tiles.Tile.TILE_FIELD2.id
         && type != Tiles.Tile.TILE_STEPPE.id
         && type != Tiles.Tile.TILE_ENCHANTED_GRASS.id) {
         return false;
      } else {
         this.getStatus().modifyHunger(-10000, type == Tiles.Tile.TILE_STEPPE.id ? 0.5F : 0.9F);
         if (Server.rand.nextInt(20) == 0) {
            int enchGrassPackChance = 120;
            if (v == null) {
               enchGrassPackChance = 80;
            } else if (v.getCreatureRatio() > Village.OPTIMUMCRETRATIO) {
               enchGrassPackChance = 240;
            }

            if (type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id) {
               if (type == Tiles.Tile.TILE_GRASS.id
                  || type == Tiles.Tile.TILE_STEPPE.id
                  || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id && Server.rand.nextInt(enchGrassPackChance) == 0) {
                  GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
                  if (growthStage == GrassData.GrowthStage.SHORT) {
                     Server.setSurfaceTile(this.currentTile.tilex, this.currentTile.tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
                  } else {
                     growthStage = growthStage.getPreviousStage();
                     Server.setSurfaceTile(
                        this.currentTile.tilex,
                        this.currentTile.tiley,
                        Tiles.decodeHeight(tile),
                        Tiles.Tile.TILE_GRASS.id,
                        GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE)
                     );
                  }

                  Players.getInstance().sendChangedTile(this.currentTile.tilex, this.currentTile.tiley, true, true);
               }
            } else {
               TileFieldBehaviour.graze(this.currentTile.tilex, this.currentTile.tiley, tile);
            }
         }

         Server.getInstance().broadCastAction(this.getNameWithGenus() + " grazes.", this, 5);
         return true;
      }
   }

   public boolean pollAge() {
      long start = System.nanoTime();

      try {
         int maxAge = this.template.getMaxAge();
         if (this.isReborn()) {
            maxAge = 14;
         }

         if (this.getStatus().pollAge(maxAge)) {
            this.sendDeathString();
            this.die(true, "Old Age");
            return true;
         } else {
            return false;
         }
      } finally {
         ;
      }
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
            this.setFavor(this.getFavor() + 10.0F);
         } catch (Exception var2) {
         }
      }
   }

   public boolean isSalesman() {
      return this.template.getTemplateId() == 9;
   }

   public boolean isAvatar() {
      return this.template.getTemplateId() == 78
         || this.template.getTemplateId() == 79
         || this.template.getTemplateId() == 80
         || this.template.getTemplateId() == 81
         || this.template.getTemplateId() == 68;
   }

   public void removeRandomItems() {
      if (!this.isTrading() && this.isNpcTrader() && Server.rand.nextInt(86400) == 0) {
         try {
            this.actions.getCurrentAction();
         } catch (NoSuchActionException var6) {
            Shop myshop = Economy.getEconomy().getShop(this);
            if (myshop.getOwnerId() == -10L) {
               Shop kingsMoney = Economy.getEconomy().getKingsShop();
               if (kingsMoney.getMoney() > 0L) {
                  int value = 0;
                  value = (int)(kingsMoney.getMoney() / (long)Shop.getNumTraders());
                  if (!Servers.localServer.HOMESERVER) {
                     value = (int)((float)value * (1.0F + Zones.getPercentLandForKingdom(this.getKingdomId()) / 100.0F));
                     value = (int)((float)value * (1.0F + (float)Items.getBattleCampControl(this.getKingdomId()) / 10.0F));
                  }

                  if (value > 0
                     && myshop != null
                     && myshop.getMoney() < (long)Servers.localServer.getTraderMaxIrons()
                     && (myshop.getSellRatio() > 0.1F || Server.getInstance().isPS())
                     && (Server.getInstance().isPS() || Servers.localServer.id != 15 || kingsMoney.getMoney() > 2000000L)) {
                     myshop.setMoney(myshop.getMoney() + (long)value);
                     kingsMoney.setMoney(kingsMoney.getMoney() - (long)value);
                  }
               }
            } else if (this.canAutoDismissMerchant(myshop)) {
               try {
                  Item sign = ItemFactory.createItem(209, 10.0F + Server.rand.nextFloat() * 10.0F, this.getName());
                  sign.setDescription("Due to poor business I have moved on. Thank you for your time. " + this.getName());
                  sign.setLastOwnerId(myshop.getOwnerId());
                  sign.putItemInfrontof(this);
                  sign.setIsPlanted(true);
               } catch (Exception var5) {
                  logger.log(Level.WARNING, var5.getMessage() + " " + this.getName() + " at " + this.getTileX() + ", " + this.getTileY(), (Throwable)var5);
               }

               TraderManagementQuestion.dismissMerchant(this, this.getWurmId());
            }
         }
      }
   }

   private boolean canAutoDismissMerchant(Shop myshop) {
      if (myshop.howLongEmpty() > 7257600000L) {
         return true;
      } else {
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
            } catch (IOException var4) {
            }
         }

         return true;
      }
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
      return (int)(this.getPositionZ() * 10.0F);
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
      if (Servers.localServer.HOMESERVER && (!this.isPlayer() && this.isDominated() || this.isRidden() || this.getHitched() != null) && this.attackers != null
         )
       {
         for(Long attl : this.attackers.keySet()) {
            try {
               Creature attacker = Server.getInstance().getCreature(attl);
               if (attacker.isPlayer() || attacker.isDominated()) {
                  if (this.isRidden()) {
                     if (attacker.getCitizenVillage() == null || this.getCurrentVillage() != attacker.getCitizenVillage()) {
                        for(Long riderLong : this.getRiders()) {
                           try {
                              Creature rider = Server.getInstance().getCreature(riderLong);
                              if (rider != attacker && !rider.isOkToKillBy(attacker)) {
                                 attacker.setUnmotivatedAttacker();
                                 attacker.setReputation(attacker.getReputation() - 10);
                              }
                           } catch (NoSuchPlayerException var14) {
                           }
                        }
                     }
                  } else if (this.getHitched() != null) {
                     if ((attacker.getCitizenVillage() == null || this.getCurrentVillage() != attacker.getCitizenVillage()) && !this.getHitched().isCreature()
                        )
                      {
                        try {
                           Item i = Items.getItem(this.getHitched().wurmid);
                           long ownid = i.getLastOwnerId();
                           if (ownid != attacker.getWurmId()) {
                              try {
                                 byte kingd = Players.getInstance().getKingdomForPlayer(ownid);
                                 if (attacker.isFriendlyKingdom(kingd) && !attacker.hasBeenAttackedBy(ownid)) {
                                    boolean ok = false;

                                    try {
                                       Creature owner = Server.getInstance().getCreature(ownid);
                                       if (owner.isOkToKillBy(attacker)) {
                                          ok = true;
                                       }
                                    } catch (NoSuchCreatureException var11) {
                                    }

                                    if (!ok) {
                                       attacker.setUnmotivatedAttacker();
                                       attacker.setReputation(attacker.getReputation() - 10);
                                    }
                                 }
                              } catch (Exception var12) {
                              }
                           }
                        } catch (NoSuchItemException var13) {
                           logger.log(Level.INFO, this.getHitched().wurmid + " no such item:", (Throwable)var13);
                        }
                     }
                  } else if (this.isDominated()) {
                     if (attacker.isFriendlyKingdom(this.getKingdomId())) {
                        boolean ok = false;

                        try {
                           Creature owner = Server.getInstance().getCreature(this.dominator);
                           if (attacker == owner || owner.isOkToKillBy(attacker)) {
                              ok = true;
                           }
                        } catch (NoSuchCreatureException var15) {
                        }

                        if (!ok) {
                           attacker.setUnmotivatedAttacker();
                           attacker.setReputation(attacker.getReputation() - 10);
                        }
                     }
                  } else if (this.getCurrentVillage() != null) {
                     Brand brand = Creatures.getInstance().getBrand(this.getWurmId());
                     if (brand != null) {
                        try {
                           Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                           if (this.getCurrentVillage() == villageBrand && attacker.getCitizenVillage() != villageBrand) {
                              attacker.setUnmotivatedAttacker();
                              attacker.setReputation(attacker.getReputation() - 10);
                           }
                        } catch (NoSuchVillageException var10) {
                           brand.deleteBrand();
                        }
                     }
                  }
               }
            } catch (Exception var16) {
            }
         }
      }
   }

   public void die(boolean freeDeath, String reasonOfDeath) {
      this.die(freeDeath, reasonOfDeath, false);
   }

   public void die(boolean freeDeath, String reasonOfDeath, boolean noCorpse) {
      WcKillCommand wkc = new WcKillCommand(WurmId.getNextWCCommandId(), this.getWurmId());
      if (Servers.isThisLoginServer()) {
         wkc.sendFromLoginServer();
      } else {
         wkc.sendToLoginServer();
      }

      if (this.isPregnant()) {
         Offspring.deleteSettings(this.getWurmId());
      }

      if (this.getTemplate().getCreatureAI() != null) {
         boolean fullOverride = this.getTemplate().getCreatureAI().creatureDied(this);
         if (fullOverride) {
            return;
         }
      }

      String corpseDescription = "";
      if (this.template.isHorse) {
         String col = this.template.getColourName(this.status);
         corpseDescription = col;
      } else if (this.template.isBlackOrWhite) {
         if (!this.hasTrait(15)
            && !this.hasTrait(16)
            && !this.hasTrait(18)
            && !this.hasTrait(24)
            && !this.hasTrait(25)
            && !this.hasTrait(23)
            && !this.hasTrait(30)
            && !this.hasTrait(31)
            && !this.hasTrait(32)
            && !this.hasTrait(33)
            && !this.hasTrait(34)
            && this.hasTrait(17)) {
            corpseDescription = "black";
         }
      } else if (this.template.isColoured) {
         corpseDescription = this.template.getColourName(this.getStatus());
      }

      if (this.isCaredFor()) {
         if (corpseDescription.equals("")) {
            corpseDescription = corpseDescription + reasonOfDeath.toLowerCase();
         } else {
            corpseDescription = corpseDescription + " [" + reasonOfDeath.toLowerCase() + "]";
         }
      }

      if (this.getTemplate().getTemplateId() == 105) {
         try {
            Item water = ItemFactory.createItem(128, 100.0F, "");
            this.getInventory().insertItem(water);
         } catch (NoSuchTemplateException var46) {
            logger.log(Level.WARNING, this.getName() + " No template for item id " + 128);
         } catch (FailedException var47) {
            logger.log(Level.WARNING, this.getName() + " failed for item id " + 128);
         }

         Weather weather = Server.getWeather();
         if (weather != null) {
            weather.modifyFogTarget(-0.025F);
         }
      }

      if (this.isUnique() && !this.isReborn()) {
         Player[] ps = Players.getInstance().getPlayers();
         HashSet<Player> lootReceivers = new HashSet<>();

         for(Player p : ps) {
            if (p != null && p.getInventory() != null && p.isWithinDistanceTo(this, 300.0F) && p.isPaying()) {
               if (!p.isDead()) {
                  try {
                     Item blood = ItemFactory.createItem(866, 100.0F, "");
                     blood.setData2(this.template.getTemplateId());
                     p.getInventory().insertItem(blood);
                     lootReceivers.add(p);
                  } catch (NoSuchTemplateException var44) {
                     logger.log(Level.WARNING, p.getName() + " No template for item id " + 866);
                  } catch (FailedException var45) {
                     logger.log(Level.WARNING, p.getName() + " " + var45.getMessage() + ":" + 866);
                  }
               } else {
                  logger.log(Level.INFO, "Player " + p.getName() + " is dead, and therefor received no loot from " + this.getNameWithGenus() + ".");
               }
            }
         }

         this.setPathing(false, true);
         if (this.isDragon()) {
            Set<Player> primeLooters = new HashSet<>();
            Set<Player> leecher = new HashSet<>();

            for(Player looter : lootReceivers) {
               Skill bStrength = looter.getBodyStrength();
               Skill bControl = looter.getBodyControlSkill();
               Skill fighting = looter.getFightingSkill();
               if ((bStrength == null || !(bStrength.getRealKnowledge() >= 30.0))
                  && (bControl == null || !(bControl.getRealKnowledge() >= 30.0))
                  && (fighting == null || !(fighting.getRealKnowledge() >= 65.0))
                  && !looter.isPriest()) {
                  leecher.add(looter);
               } else {
                  primeLooters.add(looter);
               }
            }

            int lootTemplate = 371;
            if (this.getTemplate().getTemplateId() == 16
               || this.getTemplate().getTemplateId() == 89
               || this.getTemplate().getTemplateId() == 91
               || this.getTemplate().getTemplateId() == 90
               || this.getTemplate().getTemplateId() == 92) {
               lootTemplate = 372;
            }

            try {
               this.distributeDragonScaleOrHide(primeLooters, leecher, lootTemplate);
            } catch (NoSuchTemplateException var54) {
               logger.log(Level.WARNING, "No template for " + lootTemplate + "! Players to receive were:");

               for(Player p : lootReceivers) {
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
         for(int x = 0; x < 5; ++x) {
            this.getStatus().decreaseFat();
         }
      }

      this.combatRound = 0;
      Item corpse = null;
      int tilex = this.getTileX();
      int tiley = this.getTileY();

      try {
         boolean wasHunted = this.hasAttackedUnmotivated();
         if (this.isPlayer()) {
            Item i = this.getDraggedItem();
            if (i != null && (i.getTemplateId() == 539 || i.getTemplateId() == 186 || i.getTemplateId() == 445 || i.getTemplateId() == 1125)) {
               this.achievement(72);
            }

            if (this.getVehicle() != -10L) {
               Vehicle vehic = Vehicles.getVehicleForId(this.getVehicle());
               if (vehic != null && vehic.getPilotId() == this.getWurmId()) {
                  try {
                     Item c = Items.getItem(this.getVehicle());
                     if (c.getTemplateId() == 539) {
                        this.achievement(71);
                     }
                  } catch (NoSuchItemException var43) {
                  }
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
            SoundPlayer.playSound(this.getDeathSound(), this, 1.6F);
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
         } catch (NoSuchActionException var42) {
         }

         this.actions.clear();
         if (this.isKing()) {
            King king = King.getKing(this.getKingdomId());
            if (king != null) {
               if (king.getChallengeAcceptedDate() > 0L && System.currentTimeMillis() > king.getChallengeAcceptedDate()) {
                  king.setFailedChallenge();
               }

               if (this.isInOwnDuelRing() && !king.hasFailedAllChallenges()) {
                  king.setFailedChallenge();
               }
            }
         }

         this.getCommunicator().sendSafeServerMessage("You are dead.");
         this.getCommunicator().sendCombatSafeMessage("You are dead.");
         Server.getInstance().broadCastAction(this.getNameWithGenus() + " is dead. R.I.P.", this, 5);
         if (!this.isPlayer()
            && (this.isTrader() || this.isSalesman() || this.isBartender() || this.template != null && (this.template.id == 63 || this.template.id == 62))) {
            String message = "("
               + this.getWurmId()
               + ") died at ["
               + this.getTileX()
               + ", "
               + this.getTileY()
               + "] surf="
               + this.isOnSurface()
               + " with the reason of death being "
               + reasonOfDeath;
            if (this.attackers != null && this.attackers.size() > 0) {
               message = message + ". numAttackers=" + this.attackers.size() + " :";
               int counter = 0;

               for(long playerID : this.attackers.keySet()) {
                  ++counter;
                  message = message + " " + PlayerInfoFactory.getPlayerName(playerID) + (counter == this.attackers.size() ? "," : ".");
               }
            }

            Players.getInstance().sendGmMessage(null, this.getName(), message, false);
            String templateAndName = (this.getTemplate() != null ? this.getTemplate().getName() : "Important creature") + " " + this.getName() + " died";
            logger.warning(templateAndName + " " + message);
            WcTrelloDeaths wtd = new WcTrelloDeaths(templateAndName, message);
            wtd.sendToLoginServer();
         }

         if (!this.isGhost()
            && !this.template.isNoCorpse()
            && !noCorpse
            && (this.getCreatureAIData() == null || this.getCreatureAIData() != null && this.getCreatureAIData().doesDropCorpse())) {
            corpse = ItemFactory.createItem(272, 100.0F, null);
            corpse.setPosXY(this.getStatus().getPositionX(), this.getStatus().getPositionY());
            corpse.setPosZ(this.calculatePosZ());
            corpse.onBridge = this.getBridgeId();
            if (this.hasCustomSize()) {
               corpse.setSizes(
                  (int)((float)(corpse.getSizeX() * (this.getSizeModX() & 255)) / 64.0F),
                  (int)((float)(corpse.getSizeY() * (this.getSizeModY() & 255)) / 64.0F),
                  (int)((float)(corpse.getSizeZ() * (this.getSizeModZ() & 255)) / 64.0F)
               );
            }

            corpse.setRotation(normalizeAngle(this.getStatus().getRotation() - 180.0F));
            int nameLength = 10 + this.name.length() + this.getStatus().getAgeString().length() + 1 + this.getStatus().getTypeString().length();
            int nameLengthNoType = 10 + this.name.length() + this.getStatus().getAgeString().length();
            int nameLengthNoAge = 10 + this.name.length() + 1 + this.getStatus().getTypeString().length();
            if (this.isPlayer()) {
               corpse.setName("corpse of " + this.name);
            } else if (nameLength < 40) {
               corpse.setName(
                  "corpse of " + this.getStatus().getAgeString() + " " + (nameLength < 40 ? this.getStatus().getTypeString() : "") + this.name.toLowerCase()
               );
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

               while(strt.hasMoreTokens()) {
                  ++number;
                  String next = strt.nextToken();
                  if (maxNumber < 4 || maxNumber > 4 && number > 4) {
                     if ((coname + " " + next).length() >= 40) {
                        break;
                     }

                     coname = coname + " ";
                     coname = coname + next;
                  }
               }

               corpse.setName(coname);
            }

            byte extra1 = -1;
            byte extra2 = this.status.modtype;
            if (this.template.isHorse || this.template.isBlackOrWhite) {
               extra1 = this.template.getColourCode(this.status);
            }

            if (this.isReborn()) {
               corpse.setDamage(20.0F);
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
                     if (this.isDragon()) {
                        drop = Server.rand.nextInt(10) == 0;
                     } else {
                        drop = Server.rand.nextBoolean();
                     }

                     if (drop) {
                        int item = 795 + Server.rand.nextInt(16);
                        if (item == 1009) {
                           item = 807;
                        } else if (item == 805) {
                           item = 808;
                        }

                        Item epicItem = ItemFactory.createItem(item, (float)(60 + Server.rand.nextInt(20)), "");
                        epicItem.setOwnerId(corpse.getWurmId());
                        epicItem.setLastOwnerId(corpse.getWurmId());
                        if (this.isDragon()) {
                           epicItem.setAuxData((byte)2);
                        }

                        logger.info("Dropping a " + epicItem.getName() + " (" + epicItem.getWurmId() + ")  for the slaying of " + corpse.getName());
                        corpse.insertItem(epicItem);
                     }
                  } catch (NoSuchTemplateException var40) {
                     logger.log(Level.WARNING, "No template for item id 866");
                  } catch (FailedException var41) {
                     logger.log(Level.WARNING, var41.getMessage() + ":" + 866);
                  }
               } else if (Servers.localServer.EPIC && !Servers.localServer.HOMESERVER && this.isDragon()) {
                  try {
                     boolean dropLoot = Server.rand.nextBoolean();
                     if (dropLoot) {
                        int lootId = CreatureTemplateCreator.getDragonLoot(this.template.getTemplateId());
                        if (lootId > 0) {
                           Item loot = ItemFactory.createItem(lootId, (float)(60 + Server.rand.nextInt(20)), "");
                           logger.info("Dropping a " + loot.getName() + " (" + loot.getWurmId() + ") for the slaying of " + corpse.getName());
                           corpse.insertItem(loot);
                           loot.setOwnerId(corpse.getWurmId());
                        }
                     }
                  } catch (Exception var39) {
                  }
               }
            }

            if (this.isPlayer()
               && !wasHunted
               && this.getReputation() >= 0
               && !this.isInPvPZone()
               && Servers.localServer.KINGDOM != 0
               && !this.isOnHostileHomeServer()) {
               boolean killedInVillageWar = false;
               if (this.attackers != null) {
                  for(Long l : this.attackers.keySet()) {
                     try {
                        Creature c = Creatures.getInstance().getCreature(l);
                        if (c.getCitizenVillage() != null && c.getCitizenVillage().isEnemy(this) && Servers.isThisAPvpServer()) {
                           logger.log(Level.INFO, this.getName() + " was killed by " + c.getName() + " during village war. May be looted.");
                           killedInVillageWar = true;
                        }
                     } catch (Exception var38) {
                     }
                  }
               }

               if (!killedInVillageWar) {
                  corpse.setProtected(true);
               }
            }

            corpse.setAuxData(this.getKingdomId());
            corpse.setWeight((int)Math.min(50000.0F, this.status.body.getWeight(this.status.fat)), false);
            corpse.setLastOwnerId(this.getWurmId());
            if (this.isKingdomGuard()) {
               corpse.setDamage(50.0F);
            }

            if (this.getSex() == 1) {
               corpse.setFemale(true);
            }

            corpse.setDescription(corpseDescription);
            if (!this.isPlayer() && !Servers.isThisAPvpServer()) {
               Brand brand = Creatures.getInstance().getBrand(this.getWurmId());
               if (brand != null) {
                  try {
                     corpse.setWasBrandedTo(brand.getBrandId());
                     PermissionsPlayerList allowedList = this.getPermissionsPlayerList();
                     PermissionsByPlayer[] pbpList = allowedList.getPermissionsByPlayer();
                     byte bito = ItemSettings.CorpsePermissions.COMMANDER.getBit();
                     int valueo = ItemSettings.CorpsePermissions.COMMANDER.getValue();
                     byte bitx = ItemSettings.CorpsePermissions.EXCLUDE.getBit();
                     int valuex = ItemSettings.CorpsePermissions.EXCLUDE.getValue();
                     Village bVill = null;

                     for(PermissionsByPlayer pbp : pbpList) {
                        if (pbp.getPlayerId() == -60L) {
                           if (bVill == null) {
                              bVill = Villages.getVillage((int)brand.getBrandId());
                           }

                           int value = 0;
                           if (pbp.hasPermission(bito)) {
                              value += valueo;
                           }

                           if (pbp.hasPermission(bitx)) {
                              value += valuex;
                           }

                           if (value != 0) {
                              for(Citizen citz : bVill.getCitizens()) {
                                 if (citz.isPlayer() && citz.getRole().mayBrand()) {
                                    ItemSettings.addPlayer(corpse.getWurmId(), citz.wurmId, value);
                                 }
                              }
                           }
                        }
                     }

                     for(PermissionsByPlayer pbp : pbpList) {
                        if (pbp.getPlayerId() != -60L) {
                           int value = 0;
                           if (pbp.hasPermission(bito)) {
                              value += valueo;
                           }

                           if (pbp.hasPermission(bitx)) {
                              value += valuex;
                           }

                           if (value != 0) {
                              ItemSettings.addPlayer(corpse.getWurmId(), pbp.getPlayerId(), value);
                           }
                        }
                     }
                  } catch (NoSuchVillageException var51) {
                     Creatures.getInstance().setBrand(this.getWurmId(), -10L);
                  }
               }
            }

            VolaTile vvtile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
            vvtile.addItem(corpse, false, this.getWurmId(), false);
         } else if (this.isGhost() || this.template.isNoCorpse()) {
            int[] butcheredItems = this.getTemplate().getItemsButchered();

            for(int x = 0; x < butcheredItems.length; ++x) {
               try {
                  ItemFactory.createItem(
                     butcheredItems[x],
                     20.0F + Server.rand.nextFloat() * 80.0F,
                     this.getPosX(),
                     this.getPosY(),
                     (float)(Server.rand.nextInt() * 360),
                     this.isOnSurface(),
                     (byte)0,
                     this.getStatus().getBridgeId(),
                     this.getName()
                  );
               } catch (FailedException var36) {
                  logger.log(Level.WARNING, var36.getMessage());
               } catch (NoSuchTemplateException var37) {
                  logger.log(Level.WARNING, var37.getMessage());
               }
            }
         }

         VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, this.isOnSurface());
         boolean keepItems = this.isTransferring();
         if (!this.isOnCurrentServer()) {
            keepItems = true;
         }

         if (this.getDeity() != null && this.getDeity().isDeathItemProtector() && this.getFaith() >= 70.0F && this.getFavor() >= 35.0F) {
            float chance = 0.35F;
            String successMessage = this.getDeity().getName() + " is with you and keeps your items safe.";
            String failMessage = this.getDeity().getName() + " could not keep your items safe this time.";
            float rand = Server.rand.nextFloat();
            if (this.isDeathProtected()) {
               chance = 0.5F;
               if (rand > 0.35F && rand <= chance) {
                  successMessage = this.getDeity().getName()
                     + " could not keep your items safe this time, but ethereal strands of web attach to your items and keep them safe, close to your spirit!";
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
                  for(Item w : worn) {
                     if (w.isArmour()) {
                        found = true;
                        break;
                     }
                  }
               }

               if (!found) {
                  pantLess.add(this.getWurmId());
               }
            } catch (NoSpaceException var50) {
            }
         }

         boolean insertItem = true;
         boolean dropNewbieItems = false;
         if (this.attackers != null) {
            for(Long cid : this.attackers.keySet()) {
               if (WurmId.getType(cid) == 0 && (!Servers.localServer.isChallengeServer() || this.getPlayingTime() > 86400000L)) {
                  dropNewbieItems = true;
                  break;
               }
            }
         }

         Item inventory = this.getInventory();
         Item[] invarr = inventory.getAllItems(true);

         for(int x = 0; x < invarr.length; ++x) {
            if (invarr[x].isTraded() && this.getTrade() != null) {
               invarr[x].getTradeWindow().removeItem(invarr[x]);
            }

            boolean destroyChall = false;
            if (Features.Feature.FREE_ITEMS.isEnabled()
               && invarr[x].isChallengeNewbieItem()
               && (invarr[x].isArmour() || invarr[x].isWeapon() || invarr[x].isShield())) {
               destroyChall = true;
            }

            if (destroyChall) {
               Items.destroyItem(invarr[x].getWurmId());
            } else if (invarr[x].isArtifact()
               || !keepItems && !invarr[x].isNoDrop() && (!invarr[x].isNewbieItem() || dropNewbieItems || invarr[x].isHollow() && !invarr[x].isTent())) {
               try {
                  Item parent = invarr[x].getParent();
                  if (inventory.equals(parent) || parent.getTemplateId() == 824) {
                     parent.dropItem(invarr[x].getWurmId(), true);
                     invarr[x].setBusy(false);
                     if (corpse == null || !corpse.insertItem(invarr[x], true)) {
                        if (invarr[x].isTent() && invarr[x].isNewbieItem()) {
                           Items.destroyItem(invarr[x].getWurmId());
                        } else {
                           vtile.addItem(invarr[x], false, false);
                        }
                     }
                  }
               } catch (NoSuchItemException var49) {
                  logger.log(Level.WARNING, this.getName() + " " + invarr[x].getName() + ":" + var49.getMessage(), (Throwable)var49);
               }
            } else if (!invarr[x].isArtifact() && !keepItems) {
               try {
                  Item parent = invarr[x].getParent();
                  invarr[x].setBusy(false);
                  insertItem = !parent.isNoDrop();
                  if (invarr[x].getTemplateId() == 443 && !(this.getStrengthSkill() > 21.0) && !(this.getFaith() > 35.0F)) {
                     insertItem = false;
                     if (!invarr[x].setDamage(invarr[x].getDamage() + 0.3F, true)) {
                        insertItem = true;
                     }
                  }

                  if (insertItem) {
                     parent.dropItem(invarr[x].getWurmId(), false);
                     inventory.insertItem(invarr[x], true);
                  }
               } catch (NoSuchItemException var35) {
                  logger.log(Level.WARNING, this.getName() + " " + invarr[x].getName() + ":" + var35.getMessage(), (Throwable)var35);
               }
            }
         }

         Item[] boditems = this.getBody().getContainersAndWornItems();

         for(int x = 0; x < boditems.length; ++x) {
            if (boditems[x].isTraded() && this.getTrade() != null) {
               boditems[x].getTradeWindow().removeItem(boditems[x]);
            }

            if (boditems[x].isArtifact()
               || !keepItems && !boditems[x].isNoDrop() && (!boditems[x].isNewbieItem() || dropNewbieItems || boditems[x].isHollow() && !boditems[x].isTent())
               )
             {
               if (boditems[x].isHollow()) {
                  Item[] containedItems = boditems[x].getAllItems(false);

                  for(Item lContainedItem : containedItems) {
                     if (lContainedItem.isNoDrop() || lContainedItem.isNewbieItem() && !dropNewbieItems && !lContainedItem.isHollow()) {
                        try {
                           lContainedItem.setBusy(false);
                           Item parent = lContainedItem.getParent();
                           parent.dropItem(lContainedItem.getWurmId(), false);
                           inventory.insertItem(lContainedItem, true);
                        } catch (NoSuchItemException var34) {
                           logger.log(Level.WARNING, this.getName() + ":" + var34.getMessage(), (Throwable)var34);
                        }
                     }
                  }
               }

               try {
                  Item parent = boditems[x].getParent();
                  parent.dropItem(boditems[x].getWurmId(), true);
                  boditems[x].setBusy(false);
                  if (corpse == null || !corpse.insertItem(boditems[x], true)) {
                     if (boditems[x].isTent() && boditems[x].isNewbieItem()) {
                        Items.destroyItem(invarr[x].getWurmId());
                     } else {
                        vtile.addItem(boditems[x], false, false);
                     }
                  }
               } catch (NoSuchItemException var48) {
                  logger.log(Level.WARNING, this.getName() + ":" + var48.getMessage(), (Throwable)var48);
               }
            } else if (!boditems[x].isArtifact() && !keepItems) {
               try {
                  Item parent = boditems[x].getParent();
                  boditems[x].setBusy(false);
                  insertItem = !parent.isNoDrop();
                  if (boditems[x].getTemplateId() == 443 && !(this.getStrengthSkill() > 21.0) && !(this.getFaith() > 35.0F)) {
                     insertItem = false;
                     if (!boditems[x].setDamage(boditems[x].getDamage() + 0.3F, true)) {
                        insertItem = true;
                     }
                  }

                  if (insertItem) {
                     parent.dropItem(boditems[x].getWurmId(), false);
                     inventory.insertItem(boditems[x], true);
                  }
               } catch (NoSuchItemException var33) {
                  logger.log(Level.WARNING, this.getName() + " " + boditems[x].getName() + ":" + var33.getMessage(), (Throwable)var33);
               }
            }
         }
      } catch (FailedException var52) {
         logger.log(Level.WARNING, this.getName() + ":" + var52.getMessage(), (Throwable)var52);
      } catch (NoSuchTemplateException var53) {
         logger.log(Level.WARNING, this.getName() + ":" + var53.getMessage(), (Throwable)var53);
      }

      if (corpse != null) {
         if (this.isSuiciding() && corpse.getAllItems(true).length == 0) {
            Items.destroyItem(corpse.getWurmId());
            Item var61 = null;
         }
      } else {
         this.playAnimation("die", false);
      }

      try {
         this.setBridgeId(-10L);
         this.getBody().healFully();
      } catch (Exception var32) {
         logger.log(Level.WARNING, this.getName() + var32.getMessage(), (Throwable)var32);
      }

      if (!this.isTransferring() && this.isOnCurrentServer()) {
         if (this.getTemplateId() == 78
            || this.getTemplateId() == 79
            || this.getTemplateId() == 80
            || this.getTemplateId() == 81
            || this.getTemplateId() == 68) {
            EpicServerStatus.avatarCreatureKilled(this.getWurmId());
         }

         this.setDeathEffects(freeDeath, tilex, tiley);
         if (EpicServerStatus.doesTraitorMissionExist(this.getWurmId())) {
            EpicServerStatus.traitorCreatureKilled(this.getWurmId());
         }
      }
   }

   private void distributeDragonScaleOrHide(Set<Player> primeLooters, Set<Player> leecher, int lootTemplate) throws NoSuchTemplateException {
      ItemTemplate itemt = ItemTemplateFactory.getInstance().getTemplate(lootTemplate);
      float lootNums = this.calculateDragonLootMultiplier();
      float totalWeightToDistribute = this.calculateDragonLootTotalWeight(itemt, lootNums) * (lootTemplate == 371 ? 3.0F : 1.0F);
      float leecherShare = 0.0F;
      if (leecher.size() > 0) {
         leecherShare = totalWeightToDistribute / 5.0F;
      }

      float primeShare = totalWeightToDistribute - leecherShare;
      if (leecher.size() > 0) {
         float lSplit = leecherShare / (float)leecher.size();
         float pSplit = primeShare / (float)primeLooters.size();
         if (lSplit > pSplit) {
            leecherShare = pSplit * 0.9F * (float)leecher.size();
            primeShare = totalWeightToDistribute - leecherShare;
         }
      }

      this.splitDragonLootTo(primeLooters, itemt, lootTemplate, primeShare);
      this.splitDragonLootTo(leecher, itemt, lootTemplate, leecherShare);
   }

   private final float calculateDragonLootTotalWeight(ItemTemplate template, float lootMult) {
      return 1.0F + (float)template.getWeightGrams() * lootMult;
   }

   private final float calculateDragonLootMultiplier() {
      float lootNums = 1.0F;
      if (!Servers.isThisAnEpicServer()) {
         lootNums = Math.max(1.0F, 1.0F + Server.rand.nextFloat() * 3.0F);
      }

      return lootNums;
   }

   private void splitDragonLootTo(Set<Player> lootReceivers, ItemTemplate itemt, int lootTemplate, float totalWeight) {
      if (lootReceivers.size() != 0) {
         float receivers = (float)lootReceivers.size();
         float weight = totalWeight / receivers;

         for(Player p : lootReceivers) {
            try {
               double power = 0.0;

               try {
                  Skill butchering = p.getSkills().getSkill(10059);
                  power = Math.max(0.0, butchering.skillCheck(10.0, 0.0, false, 10.0F));
               } catch (NoSuchSkillException var13) {
                  Skill butcheringx = p.getSkills().learn(10059, 1.0F);
                  power = Math.max(0.0, butcheringx.skillCheck(10.0, 0.0, false, 10.0F));
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
            } catch (NoSuchTemplateException var14) {
               logger.log(Level.WARNING, p.getName() + " No template for item id " + lootTemplate);
            } catch (FailedException var15) {
               logger.log(Level.WARNING, p.getName() + " " + var15.getMessage() + ":" + lootTemplate);
            }
         }
      }
   }

   public boolean isSuiciding() {
      return false;
   }

   public Item[] getAllItems() {
      Set<Item> allitems = new HashSet<>();
      Item inventory = this.getInventory();
      allitems.add(inventory);
      Item body = this.getBody().getBodyItem();
      allitems.add(body);
      Item[] boditems = body.getAllItems(true);

      for(Item lBoditem : boditems) {
         allitems.add(lBoditem);
      }

      Item[] invitems = inventory.getAllItems(true);

      for(Item lInvitem : invitems) {
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
      if (newCounter > 0 && newCounter >= this.fleeCounter) {
         if ((!this.isPlayer() && !this.isUnique() && (!this.isDominated() || warded) || this.isPrey()) && (warded || this.isPrey())) {
            this.fleeCounter = (byte)newCounter;
            this.sendToLoggers("updated flee counter: " + this.fleeCounter);
         }
      }
   }

   public void setTarget(long targ, boolean switchTarget) {
      if (targ == this.getWurmId()) {
         targ = -10L;
      }

      if (!this.isPrey()) {
         if (targ != -10L && this.getVehicle() != -10L) {
            try {
               Creature cret = Server.getInstance().getCreature(this.target);
               if (cret.getHitched() != null) {
                  Vehicle v = Vehicles.getVehicleForId(this.getVehicle());
                  if (v != null && v == cret.getHitched()) {
                     this.getCommunicator().sendNormalServerMessage("You cannot target " + cret.getName() + " while on the same vehicle.");
                     targ = -10L;
                  }
               }
            } catch (NoSuchCreatureException | NoSuchPlayerException var10) {
            }
         }

         if (this.loggerCreature1 != -10L) {
            logger.log(Level.FINE, this.getName() + " target=" + targ, (Throwable)(new Exception()));
         }

         if (targ == -10L) {
            this.getCommunicator().sendCombatStatus(0.0F, 0.0F, (byte)0);
            if (this.opponent != null && this.opponent.getWurmId() == this.target) {
               this.setOpponent(null);
            }

            if (this.target != targ) {
               try {
                  Creature cret = Server.getInstance().getCreature(this.target);
                  cret.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret));
               } catch (NoSuchCreatureException var8) {
               } catch (NoSuchPlayerException var9) {
               }
            }

            this.target = targ;
            this.getCommunicator().sendTarget(targ);
            VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (t != null) {
               t.sendUpdateTarget(this);
            }

            this.status.sendStateString();
         } else if ((this.target == -10L || switchTarget) && this.target != targ && (this.getBaseCombatRating() > 10.0F || this.fleeCounter <= 0)) {
            if (this.target != -10L) {
               try {
                  Creature cret = Server.getInstance().getCreature(this.target);
                  cret.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret));
               } catch (NoSuchCreatureException var6) {
               } catch (NoSuchPlayerException var7) {
               }
            }

            try {
               Creature cret = Server.getInstance().getCreature(targ);
               if (this.isSpiritGuard() && this.citizenVillage != null) {
                  VolaTile currTile = cret.getCurrentTile();
                  if (currTile.getTileX() >= this.citizenVillage.getStartX() - 5
                     && currTile.getTileX() <= this.citizenVillage.getEndX() + 5
                     && currTile.getTileY() >= this.citizenVillage.getStartY() - 5
                     && currTile.getTileY() <= this.citizenVillage.getEndY() + 5) {
                     this.citizenVillage.cryForHelp(this, false);
                  } else {
                     if (cret.opponent == this) {
                        cret.setOpponent(null);
                        cret.setTarget(-10L, true);
                        cret.getCommunicator().sendNormalServerMessage("The " + this.getName() + " suddenly becomes hazy and hard to target.");
                     }

                     targ = -10L;
                     this.setOpponent(null);
                     if (this.status.getPath() == null) {
                        this.getMoveTarget(0);
                     }
                  }
               }

               if (targ != -10L) {
                  cret.getCommunicator().changeAttitude(this.getWurmId(), this.getAttitude(cret));
               }
            } catch (NoSuchCreatureException var11) {
            } catch (NoSuchPlayerException var12) {
            }

            this.target = targ;
            this.getCommunicator().sendTarget(targ);
            VolaTile t = Zones.getTileOrNull(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (t != null) {
               t.sendUpdateTarget(this);
            }

            this.status.sendStateString();
         }
      }
   }

   public boolean modifyFightSkill(int dtilex, int dtiley) {
      boolean pvp = false;
      Map<Creature, Double> lSkillReceivers = null;
      boolean activatedTrigger = false;
      if (!this.isNoSkillgain()) {
         lSkillReceivers = new HashMap<>();
         long now = System.currentTimeMillis();
         double kskill = 0.0;
         double sumskill = 0.0;
         boolean wasHelped = false;
         if (this.attackers != null && this.attackers.size() > 0) {
            ArrayList<Long> possibleTriggerOwners = new ArrayList<>();

            for(long l : this.attackers.keySet()) {
               if (now - this.attackers.get(l) < 600000L
                  && WurmId.getType(l) == 0
                  && (!this.isPlayer() || !Players.getInstance().isOverKilling(l, this.getWurmId()))) {
                  possibleTriggerOwners.add(l);
               }
            }

            if (!possibleTriggerOwners.isEmpty()) {
               try {
                  Player player = Players.getInstance().getPlayer(possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
                  MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(this.getTemplate().getTemplateId(), 491, this.getWurmId());

                  for(MissionTrigger t : trigs) {
                     EpicMission em = EpicServerStatus.getEpicMissionForMission(t.getMissionRequired());
                     if (em != null) {
                        EpicMissionEnum missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType());
                        if (missionEnum != null && EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)) {
                           float karmaSplit = (float)(missionEnum.getKarmaBonusDiffMult() * em.getDifficulty());
                           float karmaGained = karmaSplit / (float)EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                           karmaGained = (float)Math.ceil((double)(karmaGained / (float)possibleTriggerOwners.size()));

                           for(long id : possibleTriggerOwners) {
                              try {
                                 Player p = Players.getInstance().getPlayer(id);
                                 if (Deities.getFavoredKingdom(em.getEpicEntityId()) == p.getKingdomTemplateId() || !Servers.localServer.EPIC) {
                                    MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                                    if (mp == null) {
                                       mp = MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                                    } else {
                                       MissionPerformed mperf = mp.getMission(t.getMissionRequired());
                                       if (mperf == null) {
                                          MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                                       }
                                    }

                                    p.modifyKarma((int)karmaGained);
                                    if (p.isPaying()) {
                                       p.setScenarioKarma((int)((float)p.getScenarioKarma() + karmaGained));
                                       if (Servers.localServer.EPIC) {
                                          WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(
                                             WurmId.getNextWCCommandId(), new long[]{p.getWurmId()}, new int[]{p.getScenarioKarma()}, em.getEpicEntityId()
                                          );
                                          wcek.sendToLoginServer();
                                       }
                                    }
                                 }
                              } catch (NoSuchPlayerException var42) {
                              }
                           }
                        }
                     }
                  }

                  MissionTriggers.activateTriggers(player, this.getTemplate().getTemplateId(), 491, this.getWurmId(), 1);
                  activatedTrigger = true;
               } catch (NoSuchPlayerException var43) {
               }
            }

            for(Entry<Long, Long> entry : this.attackers.entrySet()) {
               long attackerId = entry.getKey();
               long attackTime = entry.getValue();
               if (now - attackTime < 600000L) {
                  if (WurmId.getType(attackerId) == 0) {
                     pvp = true;
                     if (!this.isPlayer() || !Players.getInstance().isOverKilling(attackerId, this.getWurmId())) {
                        try {
                           Player player = Players.getInstance().getPlayer(attackerId);
                           if (!this.isDuelOrSpar(player)) {
                              kskill = player.getFightingSkill().getRealKnowledge();
                              lSkillReceivers.put(player, new Double(kskill));
                              sumskill += kskill;
                           }

                           if (!this.isPlayer() && !this.isSpiritGuard() && !this.isKingdomGuard() && player.isPlayer() && !player.isDead()) {
                              player.checkCoinAward(this.attackers.size() * (this.isBred() ? 20 : (this.isDomestic() ? 50 : 100)));
                           }

                           if (this.isChampion() && player.isPlayer() && (this.getKingdomId() != player.getKingdomId() || player.isEnemyOnChaos(this))) {
                              player.addTitle(Titles.Title.ChampSlayer);
                              if (player.isChampion()) {
                                 player.modifyChampionPoints(30);
                                 Servers.localServer.createChampTwit(player.getName() + " slays " + this.getName() + " and gains 30 champion points");
                              }
                           }
                        } catch (NoSuchPlayerException var41) {
                        }
                     }
                  } else {
                     try {
                        Creature c = Creatures.getInstance().getCreature(attackerId);
                        if (c.isDominated()) {
                           kskill = c.getFightingSkill().getKnowledge();
                           lSkillReceivers.put(c, new Double(kskill));
                           sumskill += kskill;
                        } else if ((c.isSpiritGuard() || c.isKingdomGuard()) && !this.isPlayer()) {
                           wasHelped = true;
                        }
                     } catch (NoSuchCreatureException var40) {
                     }
                  }
               }
            }
         }

         kskill = this.getFightingSkill().getRealKnowledge();
         this.getFightingSkill().touch();
         if (this.isPlayer() && kskill <= 10.0) {
            kskill = 0.0;
         }

         if (!this.isPlayer()) {
            kskill = (double)this.getBaseCombatRating();
            kskill += (double)this.getBonusCombatRating();
            if (kskill > 2.0) {
               if (!this.isReborn() && !this.isUndead()) {
                  kskill *= 5.0;
               } else if (this.getTemplate().getTemplateId() == 69) {
                  kskill *= 0.2F;
               }
            }
         } else {
            this.getFightingSkill().setKnowledge(Math.max(1.0, this.getFightingSkill().getKnowledge() - 0.25), false);
         }

         if (kskill > 0.0) {
            if (!this.isSpiritGuard() && !this.isKingdomGuard() && !this.isWarGuard()) {
               HashSet<Creature> lootReceivers = new HashSet<>();

               for(Entry<Creature, Double> entry : lSkillReceivers.entrySet()) {
                  Creature p = entry.getKey();
                  Double psk = entry.getValue();
                  double pskill = psk;
                  double percentSkillGained = pskill / sumskill;
                  double diff = kskill - pskill;
                  double lMod = 0.2F;
                  if (diff > 1.0) {
                     lMod = Math.sqrt(diff);
                  } else if (diff < -1.0) {
                     lMod = kskill / pskill;
                  }

                  if (!this.isPlayer()) {
                     lMod /= Servers.localServer.isChallengeServer() ? 2.0 : 7.0;
                     if (pskill > 70.0) {
                        double tomax = 100.0 - pskill;
                        double modifier = tomax / (double)(Servers.localServer.isChallengeServer() ? 30.0F : 500.0F);
                        lMod *= modifier;
                     }

                     if (wasHelped) {
                        lMod *= 0.1F;
                     }
                  } else if (pskill > 50.0 && kskill < 20.0) {
                     lMod = 0.0;
                  } else if (this.getKingdomId() == p.getKingdomId()) {
                     lMod = 0.0;
                  }

                  if (kskill <= 0.0) {
                     lMod = 0.0;
                  }

                  double skillGained = percentSkillGained * lMod * 0.25 * (double)ItemBonus.getKillEfficiencyBonus(p);
                  if (skillGained > 0.0) {
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
                        } catch (Exception var39) {
                           logger.log(Level.WARNING, this.getName() + " and " + p.getName() + ":" + var39.getMessage());
                        }

                        switch(this.status.modtype) {
                           case 1:
                              p.achievement(253);
                              break;
                           case 2:
                              p.achievement(254);
                              break;
                           case 3:
                              p.achievement(255);
                              break;
                           case 4:
                              p.achievement(256);
                              break;
                           case 5:
                              p.achievement(257);
                              break;
                           case 6:
                              p.achievement(258);
                              break;
                           case 7:
                              p.achievement(259);
                              break;
                           case 8:
                              p.achievement(260);
                              break;
                           case 9:
                              p.achievement(261);
                              break;
                           case 10:
                              p.achievement(262);
                              break;
                           case 11:
                              p.achievement(263);
                              break;
                           case 99:
                              p.achievement(264);
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

                     logger.log(
                        Level.INFO,
                        p.getName()
                           + " killed "
                           + this.getName()
                           + " as champ="
                           + p.isChampion()
                           + ". Diff="
                           + diff
                           + " mod="
                           + lMod
                           + " skillGained="
                           + skillGained
                           + " pskill="
                           + pskill
                           + " kskill="
                           + kskill
                     );
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

                        for(Item i : bodyItems) {
                           if (i.isArmour()) {
                              if (i.isCloth()) {
                                 ++clothArmourFound;
                              } else if (i.isDragonArmour() && (i.getTemplateId() == 476 || i.getTemplateId() == 475)) {
                                 ++dragonPiecesFound;
                              }
                           }
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

                  if (this.isPlayer() && kskill > 40.0 && lMod > 0.0 && p.isChampion()) {
                     PlayerKills pk = Players.getInstance().getPlayerKillsFor(p.getWurmId());
                     if (System.currentTimeMillis() - pk.getLastKill(this.getWurmId()) > 86400000L && pk.getNumKills(this.getWurmId()) < 10L) {
                        p.modifyChampionPoints(1);
                        Servers.localServer.createChampTwit(p.getName() + " slays " + this.getName() + " and gains 1 champion point because of difficulty");
                     }
                  }
               }

               this.getTemplate().getLootTable().ifPresent(tx -> tx.awardAll(this, lootReceivers));
            } else {
               for(Entry<Creature, Double> entry : lSkillReceivers.entrySet()) {
                  Creature p = entry.getKey();
                  if (p.isPlayer() && !this.isFriendlyKingdom(p.getKingdomId()) && this.isSpiritGuard()) {
                     p.achievement(267);
                  }
               }
            }
         } else {
            for(Entry<Creature, Double> entry : lSkillReceivers.entrySet()) {
               Creature p = entry.getKey();
               if (p.isPlayer() && !this.isFriendlyKingdom(p.getKingdomId()) && this.isSpiritGuard()) {
                  p.achievement(267);
               }
            }
         }
      } else if (!this.isUndead() && this.attackers != null && this.attackers.size() > 0) {
         ArrayList<Long> possibleTriggerOwners = new ArrayList<>();

         for(long l : this.attackers.keySet()) {
            if (WurmId.getType(l) == 0 && (!this.isPlayer() || !Players.getInstance().isOverKilling(l, this.getWurmId()))) {
               possibleTriggerOwners.add(l);
            }
         }

         if (!possibleTriggerOwners.isEmpty()) {
            try {
               Player player = Players.getInstance().getPlayer(possibleTriggerOwners.get(Server.rand.nextInt(possibleTriggerOwners.size())));
               MissionTriggers.activateTriggers(player, this.getTemplate().getTemplateId(), 491, this.getWurmId(), 1);
               MissionTrigger[] trigs = MissionTriggers.getMissionTriggersWith(this.getTemplate().getTemplateId(), 491, this.getWurmId());

               for(MissionTrigger t : trigs) {
                  EpicMission em = EpicServerStatus.getEpicMissionForMission(t.getMissionRequired());
                  if (em != null) {
                     EpicMissionEnum missionEnum = EpicMissionEnum.getMissionForType(em.getMissionType());
                     if (missionEnum != null && EpicMissionEnum.isMissionKarmaGivenOnKill(missionEnum)) {
                        float karmaSplit = (float)(missionEnum.getKarmaBonusDiffMult() * em.getDifficulty());
                        float karmaGained = karmaSplit / (float)EpicServerStatus.getNumberRequired(em.getDifficulty(), missionEnum);
                        karmaGained = (float)Math.ceil((double)(karmaGained / (float)possibleTriggerOwners.size()));

                        for(long id : possibleTriggerOwners) {
                           try {
                              Player p = Players.getInstance().getPlayer(id);
                              if (Deities.getFavoredKingdom(em.getEpicEntityId()) == p.getKingdomTemplateId() || !Servers.localServer.EPIC) {
                                 MissionPerformer mp = MissionPerformed.getMissionPerformer(id);
                                 if (mp == null) {
                                    mp = MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                                 } else {
                                    MissionPerformed mperf = mp.getMission(t.getMissionRequired());
                                    if (mperf == null) {
                                       MissionPerformed.startNewMission(t.getMissionRequired(), id, 1.0F);
                                    }
                                 }

                                 p.modifyKarma((int)karmaGained);
                                 if (p.isPaying()) {
                                    p.setScenarioKarma((int)((float)p.getScenarioKarma() + karmaGained));
                                    if (Servers.localServer.EPIC) {
                                       WcEpicKarmaCommand wcek = new WcEpicKarmaCommand(
                                          WurmId.getNextWCCommandId(), new long[]{p.getWurmId()}, new int[]{p.getScenarioKarma()}, em.getEpicEntityId()
                                       );
                                       wcek.sendToLoginServer();
                                    }
                                 }
                              }
                           } catch (NoSuchPlayerException var37) {
                           }
                        }
                     }
                  }
               }

               activatedTrigger = true;
            } catch (NoSuchPlayerException var38) {
            }
         }

         for(Entry<Long, Long> entry : this.attackers.entrySet()) {
            long attackerId = entry.getKey();
            if (WurmId.getType(attackerId) == 0) {
               pvp = true;

               try {
                  Player player = Players.getInstance().getPlayer(attackerId);
                  if (!this.isFriendlyKingdom(player.getKingdomId()) && this.isKingdomGuard()) {
                     player.achievement(266);
                  }
               } catch (NoSuchPlayerException var36) {
               }
            }
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
         } catch (NoSuchCreatureException var3) {
            this.setTarget(-10L, true);
         } catch (NoSuchPlayerException var4) {
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
            } catch (Exception var9) {
               logger.log(Level.WARNING, this.getName() + ", " + this.getWurmId() + ": failed to remove traitor name.");
            }
         }

         try {
            this.status.setDead(true);
         } catch (IOException var8) {
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
               } catch (IOException var7) {
                  logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
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

      this.getStatus().setStunned(0.0F, false);
      this.trimAttackers(true);
   }

   public void respawn() {
      if (this.getVisionArea() == null) {
         try {
            if (!this.isNpc()) {
               if (this.skills.getSkill(10052).getKnowledge(0.0) > this.template.getSkills().getSkill(10052).getKnowledge(0.0) * 2.0
                  || 100.0 - this.skills.getSkill(10052).getKnowledge(0.0) < 30.0
                  || this.skills.getSkill(10052).getKnowledge(0.0) < this.template.getSkills().getSkill(10052).getKnowledge(0.0) / 2.0) {
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
            this.getStatus().modifyStamina(65535.0F);
            this.getStatus().refresh(0.5F, false);
            this.createVisionArea();
         } catch (Exception var2) {
            logger.log(Level.WARNING, this.getName() + ":" + var2.getMessage(), (Throwable)var2);
         }
      } else {
         logger.log(Level.WARNING, this.getName() + " already has a visionarea.", (Throwable)(new Exception()));
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
            int hungMod = 4;
            int thirstMod = (int)(5.0F * ItemBonus.getReplenishBonus(this));
            if (this.getSpellEffects() != null && this.getSpellEffects().getSpellEffect((byte)74) != null) {
               hungMod = 2;
               thirstMod = 2;
            }

            hungMod = (int)((float)hungMod * ItemBonus.getReplenishBonus(this));
            boolean reduceHunger = true;
            if (this.getDeity() != null && this.getDeity().number == 4 && this.isOnSurface()) {
               int tile = Server.surfaceMesh.getTile(this.getTileX(), this.getTileY());
               if (Tiles.getTile(Tiles.decodeType(tile)).isMycelium()) {
                  reduceHunger = false;
               }
            }

            int hunger;
            if (reduceHunger) {
               this.status.decreaseCCFPValues();
               hunger = this.status.modifyHunger((int)((float)hungMod * (2.0F - this.status.getNutritionlevel())), 1.0F);
            } else {
               hunger = this.status.modifyHunger(-4, 0.99F);
            }

            int thirst = this.status.modifyThirst((float)thirstMod);
            float hungpercent = 1.0F;
            if (hunger > 45000) {
               hungpercent = Math.max(1.0F, (float)(65535 - hunger)) / 20535.0F;
               hungpercent *= hungpercent;
            }

            float thirstpercent = Math.max((float)(65535 - thirst), 1.0F) / 65535.0F;
            thirstpercent = thirstpercent * thirstpercent * thirstpercent;
            if (this.status.hasNormalRegen() && !this.isFighting()) {
               float toModify = 0.6F;
               if (this.isStealth()) {
                  toModify = 0.06F;
               }

               toModify = toModify * hungpercent * thirstpercent;
               double staminaModifier = this.status.getModifierValuesFor(1);
               if (this.getDeity() != null && this.getDeity().isStaminaBonus() && this.getFaith() >= 20.0F && this.getFavor() >= 10.0F) {
                  staminaModifier += 0.25;
               }

               if (this.hasSpiritStamina) {
                  staminaModifier *= 1.1;
               }

               if (this.hasSleepBonus()) {
                  toModify = Math.max(0.006F, toModify * (float)(1.0 + staminaModifier) * 3.0F);
               } else {
                  toModify = Math.max(0.004F, toModify * (float)(1.0 + staminaModifier));
               }

               if (this.hasSpellEffect((byte)95)) {
                  toModify *= 0.5F;
               }

               if ((this.getPower() != 0 || this.getVehicle() != -10L || !((double)(this.getPositionZ() + this.getAltOffZ()) < -1.45))
                  && !this.isUsingLastGasp()) {
                  this.status.modifyStamina2(toModify);
               } else {
                  toModify = 0.0F;
               }
            }

            this.status.setNormalRegen(true);
         } else {
            if (this.isNeedFood()) {
               if (Server.rand.nextInt(600) == 0) {
                  if (this.hasTrait(14) || this.isPregnant()) {
                     this.status.modifyHunger(1500, 1.0F);
                  } else if (!this.isCarnivore()) {
                     this.status.modifyHunger(700, 1.0F);
                  } else {
                     this.status.modifyHunger(150, 1.0F);
                  }
               }
            } else {
               this.status.modifyHunger(-1, 0.5F);
            }

            if ((this.isRegenerating() || this.isUnique()) && Server.rand.nextInt(10) == 0) {
               this.healTick();
            }

            if (Server.rand.nextInt(100) == 0) {
               if (!this.isFighting() || this.isUnique()) {
                  this.status.resetCreatureStamina();
               }

               if (!this.isSwimming()
                  && !this.isUnique()
                  && !this.isSubmerged()
                  && (double)(this.getPositionZ() + this.getAltOffZ()) <= -1.25
                  && this.getVehicle() == -10L
                  && this.hitchedTo == null
                  && !this.isRidden()
                  && this.getLeader() == null
                  && !Tiles.isSolidCave(Tiles.decodeType(this.getCurrentTileNum()))) {
                  this.addWoundOfType(null, (byte)7, 2, false, 1.0F, false, (double)(4000.0F + Server.rand.nextFloat() * 3000.0F), 0.0F, 0.0F, false, false);
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
      if (offspring != null && (!offspring.isChecked() || insta) && (Server.rand.nextInt(4) == 0 || insta)) {
         float creatureRatio = 10.0F;
         if (this.getCurrentVillage() != null) {
            creatureRatio = this.getCurrentVillage().getCreatureRatio();
         }

         if ((
               this.status.hunger > 60000 && this.status.fat <= 2
                  || creatureRatio < Village.OPTIMUMCRETRATIO && Server.rand.nextInt(Math.max((int)(creatureRatio / 2.0F), 1)) == 0
            )
            && Server.rand.nextInt(3) == 0) {
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
                  int cid = this.template.getChildTemplateId();
                  if (cid <= 0) {
                     cid = this.template.getTemplateId();
                  }

                  CreatureTemplate temp = CreatureTemplateFactory.getInstance().getTemplate(cid);
                  String newname = temp.getName();
                  byte sex = temp.keepSex ? temp.getSex() : (byte)Server.rand.nextInt(2);
                  if (this.isHorse()) {
                     if (Server.rand.nextBoolean()) {
                        newname = Offspring.generateGenericName();
                     } else if (sex == 1) {
                        newname = Offspring.generateFemaleName();
                     } else {
                        newname = Offspring.generateMaleName();
                     }

                     newname = LoginHandler.raiseFirstLetter(newname);
                  }

                  if (this.isUnicorn()) {
                     if (Server.rand.nextBoolean()) {
                        newname = Offspring.generateGenericName();
                     } else if (sex == 1) {
                        newname = Offspring.generateFemaleUnicornName();
                     } else {
                        newname = Offspring.generateMaleUnicornName();
                     }

                     newname = LoginHandler.raiseFirstLetter(newname);
                  }

                  boolean zombie = false;
                  if (cid == 66) {
                     zombie = true;
                     if (sex == 1) {
                        newname = LoginHandler.raiseFirstLetter("Daughter of " + this.name);
                     } else {
                        newname = LoginHandler.raiseFirstLetter("Son of " + this.name);
                     }

                     if (this.getKingdomTemplateId() != 3) {
                        cid = 25;
                        zombie = false;
                     }
                  }

                  Creature newCreature = doNew(
                     cid,
                     true,
                     this.getPosX(),
                     this.getPosY(),
                     Server.rand.nextFloat() * 360.0F,
                     this.getLayer(),
                     newname,
                     sex,
                     this.isAggHuman() ? this.getKingdomId() : 0,
                     Server.rand.nextBoolean() ? this.getStatus().modtype : 0,
                     zombie,
                     (byte)1
                  );
                  this.getCommunicator().sendAlertServerMessage("You give birth to " + newCreature.getName() + "!");
                  newCreature.getStatus().setTraitBits(offspring.getTraits());
                  newCreature.getStatus().setInheritance(offspring.getTraits(), offspring.getMother(), offspring.getFather());
                  newCreature.getStatus().saveCreatureName(newname);
                  if (zombie) {
                     if (this.getPet() != null) {
                        this.getCommunicator().sendNormalServerMessage(this.getPet().getNameWithGenus() + " stops following you.");
                        if (this.getPet().getLeader() == this) {
                           this.getPet().setLeader(null);
                        }

                        this.getPet().setDominator(-10L);
                        this.setPet(-10L);
                     }

                     newCreature.setDominator(this.getWurmId());
                     newCreature.setLoyalty(100.0F);
                     this.setPet(newCreature.getWurmId());
                     newCreature.getSkills().delete();
                     newCreature.getSkills().clone(this.skills.getSkills());
                     Skill[] cskills = newCreature.getSkills().getSkills();

                     for(Skill lCskill : cskills) {
                        lCskill.setKnowledge(Math.min(40.0, lCskill.getKnowledge() * 0.5), false);
                     }

                     newCreature.getSkills().save();
                  }

                  newCreature.refreshVisible();
                  Server.getInstance().broadCastAction(this.getNameWithGenus() + " gives birth to " + newCreature.getNameWithGenus() + "!", this, 5);
                  return true;
               } catch (NoSuchCreatureTemplateException var15) {
                  logger.log(Level.WARNING, this.getName() + " gives birth to nonexistant template:" + this.template.getChildTemplateId());
               }
            } catch (Exception var16) {
               logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
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
      if (father.getFather() == this.getFather()
         || father.getMother() == this.getMother()
         || father.getWurmId() == this.getFather()
         || father.getMother() == this.getWurmId()) {
         inbred = true;
      }

      new Offspring(
         this.getWurmId(),
         father.getWurmId(),
         breeder == null
            ? Traits.calcNewTraits(inbred, this.getTraits(), father.getTraits())
            : Traits.calcNewTraits(breeder.getAnimalHusbandrySkillValue(), inbred, this.getTraits(), father.getTraits()),
         (byte)(this.template.daysOfPregnancy + Server.rand.nextInt(5)),
         false
      );
      logger.log(Level.INFO, this.getName() + " gender=" + this.getSex() + " just got pregnant with " + father.getName() + " gender=" + father.getSex() + ".");
   }

   public boolean isBred() {
      return this.hasTrait(63);
   }

   static boolean isInbred(Creature maleCreature, Creature femaleCreature) {
      return maleCreature.getFather() == femaleCreature.getFather()
         || maleCreature.getMother() == femaleCreature.getMother()
         || maleCreature.getWurmId() == femaleCreature.getFather()
         || maleCreature.getMother() == femaleCreature.getWurmId();
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
         } catch (Exception var2) {
            logger.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
         }
      }
   }

   public void wearItems() {
      Item inventory = this.getInventory();
      Body body = this.getBody();
      Set<Item> invitems = inventory.getItems();
      Item[] invarr = invitems.toArray(new Item[invitems.size()]);

      for(Item lElement : invarr) {
         if (!lElement.isWeapon() || this.isPlayer() && (lElement.getTemplateId() == 7 || lElement.isWeaponKnife())) {
            if (lElement.isShield()) {
               try {
                  Item bodyPart = body.getBodyPart(44);
                  bodyPart.insertItem(lElement);
               } catch (NoSpaceException var16) {
                  var16.printStackTrace();
               }
            } else {
               byte[] places = lElement.getBodySpaces();

               for(byte lPlace : places) {
                  try {
                     Item bodyPart = body.getBodyPart(lPlace);
                     if (bodyPart.testInsertItem(lElement)) {
                        Item parent = lElement.getParent();
                        parent.dropItem(lElement.getWurmId(), false);
                        bodyPart.insertItem(lElement);
                        break;
                     }
                  } catch (NoSpaceException var19) {
                     if (!Servers.localServer.testServer && lPlace != 28) {
                        logger.log(Level.WARNING, this.getName() + ":" + var19.getMessage(), (Throwable)var19);
                     }
                  } catch (NoSuchItemException var20) {
                     logger.log(Level.WARNING, this.getName() + ":" + var20.getMessage(), (Throwable)var20);
                  }
               }
            }
         } else {
            try {
               byte rslot = (byte)(this.isPlayer() ? 38 : 14);
               Item bodyPart = body.getBodyPart(rslot);
               if (bodyPart.testInsertItem(lElement)) {
                  Item parent = lElement.getParent();
                  parent.dropItem(lElement.getWurmId(), false);
                  bodyPart.insertItem(lElement);
               } else {
                  byte lslot = (byte)(this.isPlayer() ? 37 : 13);
                  bodyPart = body.getBodyPart(lslot);
                  if (bodyPart.testInsertItem(lElement)) {
                     Item parent = lElement.getParent();
                     parent.dropItem(lElement.getWurmId(), false);
                     bodyPart.insertItem(lElement);
                  }
               }
            } catch (NoSuchItemException var17) {
               logger.log(Level.WARNING, this.getName() + " " + var17.getMessage(), (Throwable)var17);
            } catch (NoSpaceException var18) {
               logger.log(Level.WARNING, this.getName() + " " + var18.getMessage(), (Throwable)var18);
            }
         }
      }
   }

   public float getStaminaMod() {
      int hunger = this.status.getHunger();
      int thirst = this.status.getThirst();
      float newhungpercent = 1.0F;
      if (hunger > 45000) {
         newhungpercent = Math.max(1.0F, (float)(65535 - hunger)) / 20535.0F;
         newhungpercent *= newhungpercent;
      }

      float thirstpercent = Math.max((float)(65535 - thirst), 1.0F) / 65535.0F;
      thirstpercent = thirstpercent * thirstpercent * thirstpercent;
      return 1.0F - newhungpercent * thirstpercent;
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
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(10073, 1.0F);
      }
   }

   public double getLockPickingSkillVal() {
      try {
         return this.skills.getSkill(10076).getKnowledge(0.0);
      } catch (NoSuchSkillException var2) {
         return 1.0;
      }
   }

   public double getLockSmithingSkill() {
      try {
         return this.skills.getSkill(10034).getKnowledge(0.0);
      } catch (NoSuchSkillException var2) {
         return 1.0;
      }
   }

   public double getStrengthSkill() {
      try {
         return this.isPlayer() ? this.skills.getSkill(102).getKnowledge(0.0) : this.skills.getSkill(102).getKnowledge();
      } catch (NoSuchSkillException var2) {
         return 1.0;
      }
   }

   public Skill getStealSkill() {
      try {
         return this.skills.getSkill(10075);
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(10075, 1.0F);
      }
   }

   public Skill getStaminaSkill() {
      try {
         return this.skills.getSkill(103);
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(103, 1.0F);
      }
   }

   public final double getAnimalHusbandrySkillValue() {
      try {
         return this.skills.getSkill(10085).getKnowledge(0.0);
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(10085, 1.0F).getKnowledge(0.0);
      }
   }

   public double getBodyControl() {
      try {
         return this.skills.getSkill(104).getKnowledge(0.0);
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(104, 1.0F).getKnowledge(0.0);
      }
   }

   public Skill getBodyControlSkill() {
      try {
         return this.skills.getSkill(104);
      } catch (NoSuchSkillException var2) {
         return this.skills.learn(104, 1.0F);
      }
   }

   public Skill getFightingSkill() {
      if (!this.isPlayer()) {
         return this.getWeaponLessFightingSkill();
      } else {
         try {
            return this.skills.getSkill(1023);
         } catch (NoSuchSkillException var2) {
            return this.skills.learn(1023, 1.0F);
         }
      }
   }

   public Skill getWeaponLessFightingSkill() {
      try {
         return this.skills.getSkill(10052);
      } catch (NoSuchSkillException var5) {
         try {
            return this.skills.learn(10052, (float)this.template.getSkills().getSkill(10052).getKnowledge(0.0));
         } catch (NoSuchSkillException var3) {
            logger.log(Level.WARNING, "Template for " + this.getName() + " has no weaponless skill?");
            return this.skills.learn(10052, 20.0F);
         } catch (Exception var4) {
            logger.log(Level.WARNING, var4.getMessage() + " template for " + this.getName() + " has skills?");
            return this.skills.learn(10052, 20.0F);
         }
      }
   }

   public byte getAttitude(Creature aTarget) {
      if (this.opponent == aTarget) {
         return 2;
      } else if (aTarget.isNpc() && this.isNpc() && aTarget.getKingdomId() == this.getKingdomId()) {
         return 1;
      } else {
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

            if (this.getLoyalty() > 0.0F
               && (aTarget.getReputation() >= 0 || aTarget.getKingdomTemplateId() == 3)
               && this.isFriendlyKingdom(aTarget.getKingdomId())) {
               return 0;
            }
         }

         if (aTarget.isDominated()) {
            Creature lDominator = aTarget.getDominator();
            if (lDominator != null) {
               if (lDominator == this) {
                  return 1;
               }

               if (!aTarget.isHorse() || !aTarget.isRidden()) {
                  return this.getAttitude(lDominator);
               }

               if (this.isHungry() && this.isCarnivore()) {
                  if (Server.rand.nextInt(5) == 0) {
                     for(Long riderLong : aTarget.getRiders()) {
                        try {
                           Creature rider = Server.getInstance().getCreature(riderLong);
                           if (this.getAttitude(rider) == 2) {
                              return 2;
                           }
                        } catch (Exception var6) {
                           logger.log(Level.WARNING, var6.getMessage());
                        }
                     }
                  }

                  return 0;
               }
            }

            if (this.isFriendlyKingdom(aTarget.getKingdomId()) && aTarget.getLoyalty() > 0.0F) {
               return 0;
            }
         }

         if (this.getPet() != null && aTarget == this.getPet()) {
            return 1;
         } else if (this.isInvulnerable()) {
            return 0;
         } else if (aTarget.isInvulnerable()) {
            return 0;
         } else {
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
            } else if (this.onlyAttacksPlayers() && !aTarget.isPlayer()) {
               return 0;
            } else if (!this.isPlayer() && aTarget.onlyAttacksPlayers()) {
               return 0;
            } else if (Servers.isThisAChaosServer() && this.getCitizenVillage() != null && this.getCitizenVillage().isEnemy(aTarget)) {
               return 2;
            } else {
               if (this.isAggHuman()) {
                  if (aTarget instanceof Player) {
                     boolean atta = true;
                     if (this.isAnimal()
                        && aTarget.getDeity() != null
                        && aTarget.getDeity().isBefriendCreature()
                        && aTarget.getFaith() > 60.0F
                        && aTarget.getFavor() >= 30.0F) {
                        atta = false;
                     }

                     if (this.isMonster()
                        && !this.isUnique()
                        && aTarget.getDeity() != null
                        && aTarget.getDeity().isBefriendMonster()
                        && aTarget.getFaith() > 60.0F
                        && aTarget.getFavor() >= 30.0F) {
                        atta = false;
                     }

                     if (this.getLoyalty() > 0.0F
                        && (aTarget.getReputation() >= 0 || aTarget.getKingdomTemplateId() == 3)
                        && this.isFriendlyKingdom(aTarget.getKingdomId())) {
                        atta = false;
                     }

                     if (atta) {
                        return 2;
                     }
                  } else if ((!aTarget.isSpiritGuard() || aTarget.getCitizenVillage() != null) && !aTarget.isKingdomGuard()) {
                     if (aTarget.isRidden()) {
                        if (this.isHungry() && this.isCarnivore() && Server.rand.nextInt(5) == 0) {
                           for(Long riderLong : aTarget.getRiders()) {
                              try {
                                 Creature rider = Server.getInstance().getCreature(riderLong);
                                 if (this.getAttitude(rider) == 2) {
                                    return 2;
                                 }
                              } catch (Exception var7) {
                                 logger.log(Level.WARNING, var7.getMessage());
                              }
                           }
                        }

                        return 0;
                     }
                  } else if (this.getLoyalty() <= 0.0F && !this.isUnique() && (!this.isHorse() || !this.isRidden())) {
                     return 2;
                  }
               } else {
                  if (aTarget.getKingdomId() != 0
                     && !this.isFriendlyKingdom(aTarget.getKingdomId())
                     && (this.isDefendKingdom() || this.isAggWhitie() && aTarget.getKingdomTemplateId() != 3)) {
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
                           for(Long riderLong : aTarget.getRiders()) {
                              try {
                                 Creature rider = Server.getInstance().getCreature(riderLong);
                                 if (!this.isFriendlyKingdom(rider.getKingdomId())) {
                                    return 2;
                                 }
                              } catch (Exception var8) {
                                 logger.log(Level.WARNING, var8.getMessage());
                              }
                           }

                           return 0;
                        }
                     }
                  } else if (this.isKingdomGuard()) {
                     if (aTarget.getKingdomId() != 0) {
                        if (!this.isFriendlyKingdom(aTarget.getKingdomId())) {
                           return 2;
                        }

                        if (aTarget.getKingdomTemplateId() != 3 && aTarget.getReputation() <= -100) {
                           return 2;
                        }

                        if (aTarget.isPlayer()) {
                           Village lVill = Villages.getVillageWithPerimeterAt(this.getTileX(), this.getTileY(), true);
                           if (lVill != null && lVill.kingdom == this.getKingdomId() && lVill.isEnemy(aTarget)) {
                              return 2;
                           }
                        }
                     } else if (aTarget.isAggHuman()
                        && !aTarget.isUnique()
                        && aTarget.getCurrentKingdom() == this.getKingdomId()
                        && aTarget.getLoyalty() <= 0.0F
                        && !aTarget.isRidden()) {
                        return 2;
                     }

                     if (aTarget.isRidden()) {
                        for(Long riderLong : aTarget.getRiders()) {
                           try {
                              Creature rider = Server.getInstance().getCreature(riderLong);
                              if (this.getAttitude(rider) == 2) {
                                 return 2;
                              }
                           } catch (Exception var9) {
                              logger.log(Level.WARNING, var9.getMessage());
                           }
                        }
                     }
                  }
               }

               return (byte)(this.isCarnivore()
                     && aTarget.isPrey()
                     && Server.rand.nextInt(10) == 0
                     && this.canEat()
                     && aTarget.getCurrentVillage() == null
                     && aTarget.getHitched() == null
                  ? 2
                  : 0);
            }
         }
      }
   }

   public final byte getCurrentKingdom() {
      return Zones.getKingdom(this.getTileX(), this.getTileY());
   }

   public boolean isFriendlyKingdom(byte targetKingdom) {
      if (this.getKingdomId() != 0 && targetKingdom != 0) {
         if (this.getKingdomId() == targetKingdom) {
            return true;
         } else {
            Kingdom myKingd = Kingdoms.getKingdom(this.getKingdomId());
            return myKingd != null ? myKingd.isAllied(targetKingdom) : false;
         }
      } else {
         return false;
      }
   }

   public Possessions getPossessions() {
      return this.possessions;
   }

   public Item getInventory() {
      if (this.possessions != null) {
         return this.possessions.getInventory();
      } else {
         logger.warning("Posessions was null for " + this.id);
         return null;
      }
   }

   public Optional<Item> getInventoryOptional() {
      if (this.possessions != null) {
         return Optional.ofNullable(this.possessions.getInventory());
      } else {
         logger.warning("Posessions was null for " + this.id);
         return Optional.empty();
      }
   }

   public static final Item createItem(int templateId, float qualityLevel) throws Exception {
      return ItemFactory.createItem(templateId, qualityLevel, (byte)0, (byte)0, null);
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
      return doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, (byte)0, ctype, false);
   }

   public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender) throws Exception {
      return doNew(templateid, aPosX, aPosY, aRot, layer, name, gender, (byte)0);
   }

   public static Creature doNew(int templateid, float aPosX, float aPosY, float aRot, int layer, String name, byte gender, byte kingdom) throws Exception {
      return doNew(templateid, true, aPosX, aPosY, aRot, layer, name, gender, kingdom, (byte)0, false);
   }

   public static Creature doNew(
      int templateid,
      boolean createPossessions,
      float aPosX,
      float aPosY,
      float aRot,
      int layer,
      String name,
      byte gender,
      byte kingdom,
      byte ctype,
      boolean reborn
   ) throws Exception {
      return doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, (byte)0);
   }

   public static Creature doNew(
      int templateid,
      boolean createPossessions,
      float aPosX,
      float aPosY,
      float aRot,
      int layer,
      String name,
      byte gender,
      byte kingdom,
      byte ctype,
      boolean reborn,
      byte age
   ) throws Exception {
      return doNew(templateid, createPossessions, aPosX, aPosY, aRot, layer, name, gender, kingdom, ctype, reborn, age, 0);
   }

   public static Creature doNew(
      int templateid,
      boolean createPossessions,
      float aPosX,
      float aPosY,
      float aRot,
      int layer,
      String name,
      byte gender,
      byte kingdom,
      byte ctype,
      boolean reborn,
      byte age,
      int floorLevel
   ) throws Exception {
      Creature toReturn = (Creature)(reborn || templateid != 1 && templateid != 113
         ? new Creature(CreatureTemplateFactory.getInstance().getTemplate(templateid))
         : new Npc(CreatureTemplateFactory.getInstance().getTemplate(templateid)));
      long wid = WurmId.getNextCreatureId();

      try {
         while(Creatures.getInstance().getCreature(wid) != null) {
            wid = WurmId.getNextCreatureId();
         }
      } catch (Exception var17) {
      }

      toReturn.setWurmId(wid, aPosX, aPosY, normalizeAngle(aRot), layer);
      if (name.length() > 0) {
         toReturn.setName(name);
      }

      if (toReturn.getTemplate().isRoyalAspiration()) {
         if (toReturn.getTemplate().getTemplateId() == 62) {
            kingdom = 1;
         } else if (toReturn.getTemplate().getTemplateId() == 63) {
            kingdom = 3;
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

      if (age <= 0) {
         toReturn.getStatus().age = (int)(1.0F + Server.rand.nextFloat() * (float)Math.min(48, toReturn.getTemplate().getMaxAge()));
      } else {
         toReturn.getStatus().age = age;
      }

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
         toReturn.setAlignment(-50.0F);
         toReturn.setDeity(Deities.getDeity(4));
         toReturn.setFaith(1.0F);
      }

      toReturn.setSex(gender, true);
      Creatures.getInstance().addCreature(toReturn, false, false);
      toReturn.loadSkills();
      toReturn.createPossessions();
      toReturn.getBody().createBodyParts();
      if (!toReturn.isAnimal() && createPossessions) {
         createBasicItems(toReturn);
         toReturn.wearItems();
      }

      if ((toReturn.isHorse() || toReturn.getTemplate().isBlackOrWhite) && Server.rand.nextInt(10) == 0) {
         setRandomColor(toReturn);
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
         toReturn.setAlignment(-50.0F);
         toReturn.setDeity(Deities.getDeity(4));
         toReturn.setFaith(1.0F);
      }

      if (templateid != 119) {
         Server.getInstance().broadCastAction(toReturn.getNameWithGenus() + " has arrived.", toReturn, 10);
      }

      if (toReturn.isUnique()) {
         Server.getInstance().broadCastSafe("Rumours of " + toReturn.getName() + " are starting to spread.");
         Servers.localServer.spawnedUnique();
         logger.log(
            Level.INFO,
            "Unique " + toReturn.getName() + " spawned @ " + toReturn.getTileX() + ", " + toReturn.getTileY() + ", wurmID = " + toReturn.getWurmId()
         );
      }

      if (toReturn.getTemplate().getCreatureAI() != null) {
         toReturn.getTemplate().getCreatureAI().creatureCreated(toReturn);
      }

      return toReturn;
   }

   public float getSecondsPlayed() {
      return 1.0F;
   }

   public static void createBasicItems(Creature toReturn) {
      try {
         Item inventory = toReturn.getInventory();
         if (toReturn.getTemplate().getTemplateId() == 11) {
            Item club = createItem(314, 45.0F);
            inventory.insertItem(club);
            Item paper = getRareRecipe("Da Wife", 1250, 1251, 1252, 1253);
            if (paper != null) {
               inventory.insertItem(paper);
            }
         } else if (toReturn.getTemplate().getTemplateId() == 23) {
            Item paper = getRareRecipe("Granny Gobin", 1255, 1256, 1257, 1258);
            if (paper != null) {
               inventory.insertItem(paper);
            }
         } else if (toReturn.getTemplate().getTemplateId() == 75) {
            Item swo = createItem(81, 85.0F);
            ItemSpellEffects effs = new ItemSpellEffects(swo.getWurmId());
            effs.addSpellEffect(new SpellEffect(swo.getWurmId(), (byte)33, 90.0F, 20000000));
            inventory.insertItem(swo);
            Item helmOne = createItem(285, 75.0F);
            Item helmTwo = createItem(285, 75.0F);
            helmOne.setMaterial((byte)9);
            helmTwo.setMaterial((byte)9);
            inventory.insertItem(helmOne);
            inventory.insertItem(helmTwo);
         } else if (toReturn.isUnique()) {
            if (toReturn.getTemplate().getTemplateId() == 26) {
               Item sword = createItem(80, 45.0F);
               inventory.insertItem(sword);
               Item shield = createItem(4, 45.0F);
               inventory.insertItem(shield);
               Item goboHat = createItem(1014, 55.0F);
               inventory.insertItem(goboHat);
            } else if (toReturn.getTemplate().getTemplateId() == 27) {
               Item club = createItem(314, 65.0F);
               inventory.insertItem(club);
               Item trollCrown = createItem(1015, 70.0F);
               inventory.insertItem(trollCrown);
            } else if (toReturn.getTemplate().getTemplateId() != 22 && toReturn.getTemplate().getTemplateId() != 20) {
               if (!CreatureTemplate.isDragonHatchling(toReturn.getTemplate().getTemplateId())
                  && CreatureTemplate.isFullyGrownDragon(toReturn.getTemplate().getTemplateId())) {
               }
            } else {
               Item club = createItem(314, 65.0F);
               inventory.insertItem(club);
            }
         }
      } catch (Exception var6) {
         logger.log(Level.INFO, "Failed to create items for creature.", (Throwable)var6);
      }
   }

   public Item getPrimWeapon() {
      return this.getPrimWeapon(false);
   }

   public Item getPrimWeapon(boolean onlyBodyPart) {
      Item primWeapon = null;
      if (this.isAnimal()) {
         try {
            if (this.getHandDamage() > 0.0F) {
               return this.getEquippedWeapon((byte)14);
            }

            if (this.getKickDamage() > 0.0F) {
               return this.getEquippedWeapon((byte)34);
            }

            if (this.getHeadButtDamage() > 0.0F) {
               return this.getEquippedWeapon((byte)1);
            }

            if (this.getBiteDamage() > 0.0F) {
               return this.getEquippedWeapon((byte)29);
            }

            if (this.getBreathDamage() > 0.0F) {
               return this.getEquippedWeapon((byte)2);
            }
         } catch (NoSpaceException var6) {
            logger.log(Level.WARNING, this.getName() + var6.getMessage(), (Throwable)var6);
         }
      } else {
         try {
            byte slot = (byte)(this.isPlayer() ? 38 : 14);
            primWeapon = this.getEquippedWeapon(slot, true);
         } catch (NoSpaceException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         }
      }

      if (primWeapon == null) {
         try {
            byte slot = (byte)(this.isPlayer() ? 37 : 13);
            primWeapon = this.getEquippedWeapon(slot, true);
            if (!primWeapon.isTwoHanded()) {
               primWeapon = null;
            } else if (this.getShield() != null) {
               primWeapon = null;
            }
         } catch (NoSpaceException var4) {
            logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         }
      }

      return primWeapon;
   }

   public Item getLefthandWeapon() {
      try {
         byte slot = (byte)(this.isPlayer() ? 37 : 13);
         Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
         if (wornItems != null) {
            for(Item item : wornItems) {
               if (!item.isArmour() && !item.isBodyPartAttached() && item.getDamagePercent() > 0) {
                  return item;
               }
            }
         }
      } catch (NoSpaceException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public Item getLefthandItem() {
      try {
         byte slot = (byte)(this.isPlayer() ? 37 : 13);
         Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
         if (wornItems != null) {
            for(Item item : wornItems) {
               if (!item.isArmour() && !item.isBodyPartAttached()) {
                  return item;
               }
            }
         }
      } catch (NoSpaceException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public Item getRighthandItem() {
      try {
         byte slot = (byte)(this.isPlayer() ? 38 : 14);
         Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
         if (wornItems != null) {
            for(Item item : wornItems) {
               if (!item.isArmour() && !item.isBodyPartAttached()) {
                  return item;
               }
            }
         }
      } catch (NoSpaceException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public Item getRighthandWeapon() {
      try {
         byte slot = (byte)(this.isPlayer() ? 38 : 14);
         Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
         if (wornItems != null) {
            for(Item item : wornItems) {
               if (!item.isArmour() && !item.isBodyPartAttached() && item.getDamagePercent() > 0) {
                  return item;
               }
            }
         }
      } catch (NoSpaceException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public Item getWornBelt() {
      try {
         byte slot = (byte)(this.isPlayer() ? 43 : 34);
         Set<Item> wornItems = this.status.getBody().getBodyPart(slot).getItems();
         if (wornItems != null) {
            for(Item item : wornItems) {
               if (item.isBelt()) {
                  return item;
               }
            }
         }
      } catch (NoSpaceException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return null;
   }

   public Item[] getSecondaryWeapons() {
      Set<Item> toReturn = new HashSet<>();
      if (this.getBiteDamage() > 0.0F) {
         try {
            toReturn.add(this.getEquippedWeapon((byte)29));
         } catch (NoSpaceException var5) {
            logger.log(Level.WARNING, this.getName() + " no face.");
         }
      }

      if (this.getHeadButtDamage() > 0.0F) {
         try {
            toReturn.add(this.getEquippedWeapon((byte)1));
         } catch (NoSpaceException var4) {
            logger.log(Level.WARNING, this.getName() + " no head.");
         }
      }

      if (this.getKickDamage() > 0.0F) {
         try {
            if (!this.isAnimal() && !this.isMonster()) {
               try {
                  this.getArmour((byte)34);
               } catch (NoArmourException var7) {
                  if (this.getCarryingCapacityLeft() > 40000) {
                     toReturn.add(this.getEquippedWeapon((byte)34));
                  }
               }
            } else {
               toReturn.add(this.getEquippedWeapon((byte)34));
            }
         } catch (NoSpaceException var8) {
            logger.log(Level.WARNING, this.getName() + " no legs.");
         }
      }

      if (this.getBreathDamage() > 0.0F) {
         try {
            toReturn.add(this.getEquippedWeapon((byte)2));
         } catch (NoSpaceException var3) {
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
         } catch (NoSpaceException var6) {
            logger.log(Level.WARNING, this.getName() + " - no arm. This may be possible later but not now." + var6.getMessage(), (Throwable)var6);
         }
      }

      return !toReturn.isEmpty() ? toReturn.toArray(new Item[toReturn.size()]) : emptyItems;
   }

   public Item getShield() {
      Item shield = null;

      try {
         byte slot = (byte)(this.isPlayer() ? 44 : 3);
         shield = this.getEquippedItem(slot);
         if (shield != null && !shield.isShield()) {
            shield = null;
         }
      } catch (NoSpaceException var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
      }

      return shield;
   }

   public float getSpeed() {
      return this.getCreatureAIData() != null ? this.getCreatureAIData().getSpeed() : this.template.getSpeed();
   }

   public int calculateSize() {
      int centimetersHigh = this.getBody().getCentimetersHigh();
      int centimetersLong = this.getBody().getCentimetersLong();
      int centimetersWide = this.getBody().getCentimetersWide();
      int size = 3;
      byte var5;
      if (centimetersHigh > 400 || centimetersLong > 400 || centimetersWide > 400) {
         var5 = 5;
      } else if (centimetersHigh > 200 || centimetersLong > 200 || centimetersWide > 200) {
         var5 = 4;
      } else if (centimetersHigh > 100 || centimetersLong > 100 || centimetersWide > 100) {
         var5 = 3;
      } else if (centimetersHigh <= 50 && centimetersLong <= 50 && centimetersWide <= 50) {
         var5 = 1;
      } else {
         var5 = 2;
      }

      return var5;
   }

   public void say(String message) {
      if (this.currentTile != null) {
         this.currentTile.broadCastMessage(new Message(this, (byte)0, ":Local", "<" + this.getName() + "> " + message));
      }
   }

   public void say(String message, boolean emote) {
      if (this.currentTile != null) {
         if (!emote) {
            this.say(message);
         } else {
            this.currentTile.broadCastMessage(new Message(this, (byte)6, ":Local", this.getName() + " " + message));
         }
      }
   }

   public void sendEquipment(Creature receiver) {
      if (receiver.addItemWatched(this.getBody().getBodyItem())) {
         receiver.getCommunicator().sendOpenInventoryWindow(this.getBody().getBodyItem().getWurmId(), this.getName());
         this.getBody().getBodyItem().addWatcher(this.getBody().getBodyItem().getWurmId(), receiver);
         Wounds w = this.getBody().getWounds();
         if (w != null) {
            Wound[] wounds = w.getWounds();

            for(Wound lWound : wounds) {
               try {
                  Item bodypart = this.getBody().getBodyPartForWound(lWound);
                  receiver.getCommunicator().sendAddWound(lWound, bodypart);
               } catch (NoSpaceException var9) {
                  logger.log(Level.INFO, var9.getMessage(), (Throwable)var9);
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
            logger.log(
               Level.INFO,
               this.getName()
                  + " at "
                  + this.getTileX()
                  + ","
                  + this.getTileY()
                  + " "
                  + this.getLayer()
                  + "  blingking to "
                  + this.creatureToBlinkTo.getTileX()
                  + ","
                  + this.creatureToBlinkTo.getTileY()
                  + ","
                  + this.creatureToBlinkTo.getLayer()
            );
            this.blinkTo(
               this.creatureToBlinkTo.getTileX(), this.creatureToBlinkTo.getTileY(), this.creatureToBlinkTo.getLayer(), this.creatureToBlinkTo.getFloorLevel()
            );
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
            this.sendToLoggers(
               "received path to " + this.status.getPath().getTargetTile().getTileX() + "," + this.status.getPath().getTargetTile().getTileY(), (byte)2
            );
            if (this.status.getPath().getSize() >= 4) {
               this.pathRecalcLength = this.status.getPath().getSize() / 2;
            } else {
               this.pathRecalcLength = 0;
            }

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

         if (!this.isAnimal() && !this.isDominated()) {
            findPath = true;
         } else {
            path = this.status.getPath();
            if (path == null) {
               findPath = true;
            }
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
      } else {
         this.guardSecondsLeft = 10;
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
      if (this.creatureToBlinkTo == null) {
         PathTile p = this.getMoveTarget(seed);
         if (p != null) {
            this.startPathingToTile(p);
         }
      }
   }

   public final void checkMove() throws NoSuchCreatureException, NoSuchPlayerException {
      if (this.hitchedTo == null) {
         if (!this.isSentinel()) {
            if (this.isHorse() || this.isUnicorn()) {
               Item torsoItem = this.getWornItem((byte)2);
               if (torsoItem != null && (torsoItem.isSaddleLarge() || torsoItem.isSaddleNormal())) {
                  return;
               }
            }

            if (this.isDominated()) {
               if (this.hasOrders()) {
                  if (this.target == -10L) {
                     if (this.status.getPath() == null) {
                        if (!this.isPathing()) {
                           this.startPathing(0);
                        }
                     } else if (this.moveAlongPath() || this.isTeleporting()) {
                        this.status.setPath(null);
                        this.status.setMoving(false);
                        if (this.isSpy()) {
                           Creature linkedToc = this.getCreatureLinkedTo();
                           if (this.isWithinSpyDist(linkedToc)) {
                              this.turnTowardsCreature(linkedToc);

                              for(Npc npc : Creatures.getInstance().getNpcs()) {
                                 if (!npc.isDead() && this.isSpyFriend(npc) && npc.isWithinDistanceTo(this, 400.0F) && npc.longTarget == null) {
                                    npc.longTarget = new LongTarget(
                                       linkedToc.getTileX(), linkedToc.getTileY(), 0, linkedToc.isOnSurface(), linkedToc.getFloorLevel(), npc
                                    );
                                    if (!npc.isWithinDistanceTo(linkedToc, 100.0F)) {
                                       int seed = Server.rand.nextInt(5);
                                       String mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                                       switch(seed) {
                                          case 0:
                                             mess = linkedToc.getName() + " is in trouble now!";
                                             break;
                                          case 1:
                                             mess = "Going to check out what " + linkedToc.getName() + " is doing.";
                                             break;
                                          case 2:
                                             mess = "Heading to slay " + linkedToc.getName() + ".";
                                             break;
                                          case 3:
                                             mess = "Going to get me the scalp of " + linkedToc.getName() + " today.";
                                             break;
                                          case 4:
                                             mess = "Poor " + linkedToc.getName() + " won't know what hit " + linkedToc.getHimHerItString() + ".";
                                             break;
                                          default:
                                             mess = "Think I'll go hunt for " + linkedToc.getName() + " a bit...";
                                       }

                                       VolaTile tile = npc.getCurrentTile();
                                       if (tile != null) {
                                          Message m = new Message(npc, (byte)0, ":Local", "<" + npc.getName() + "> " + mess);
                                          tile.broadCastMessage(m);
                                       }
                                    }
                                 }
                              }
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
            } else if (this.leader == null && !this.shouldStandStill && !this.status.isUnconscious() && this.status.getStunned() == 0.0F) {
               if (this.isMoveGlobal()) {
                  if (this.status.getPath() != null) {
                     if (this.moveAlongPath() || this.isTeleporting()) {
                        this.status.setPath(null);
                        this.status.setMoving(false);
                     }
                  } else if (this.isHunter() && this.target != -10L && this.fleeCounter <= 0) {
                     this.hunt();
                  } else {
                     if (Server.rand.nextInt(100) == 0) {
                        PathTile targ = this.getPersonalTargetTile();
                        if (targ != null && !this.isPathing) {
                           this.startPathingToTile(targ);
                        }
                     }

                     if (this.status.moving) {
                        if (Server.rand.nextInt(100) < 5) {
                           this.status.setMoving(false);
                        }
                     } else {
                        int mod = 1;
                        int max = 2000;
                        if ((!this.isCareful() || this.getStatus().damage <= 10000) && this.loggerCreature1 <= 0L) {
                           if (this.isBred() || this.isBranded() || this.isCaredFor()) {
                              max = 20000;
                           } else if (this.isNpc() && !this.isAggHuman() && this.getCitizenVillage() != null) {
                              max = 200 + (int)(this.getWurmId() % 100L) * 3;
                           }
                        } else {
                           mod = 19;
                        }

                        if (Server.rand.nextInt(Math.max(1, max - this.template.getMoveRate() * mod)) >= 5 && !this.shouldFlee()) {
                           if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()
                              && (Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5 || this.loggerCreature1 > 0L)) {
                              for(Fence f : this.getCurrentTile().getAllFences()) {
                                 if (f.isHorizontal() && Math.abs(f.getPositionY() - this.getPosY()) < 1.25F) {
                                    this.takeSimpleStep();
                                    break;
                                 }

                                 if (!f.isHorizontal() && Math.abs(f.getPositionX() - this.getPosX()) < 1.25F) {
                                    this.takeSimpleStep();
                                    break;
                                 }
                              }
                           }
                        } else {
                           this.status.setMoving(true);
                        }
                     }

                     if (this.status.moving && !this.isTeleporting()) {
                        this.takeSimpleStep();
                     }
                  }
               } else if (this.status.getPath() == null) {
                  if (!this.isTeleporting()) {
                     if (!this.isPathing()) {
                        if (this.target != -10L && !this.shouldFlee()) {
                           this.hunt();
                        } else {
                           int mod = 1;
                           int max = 2000;
                           if (this.isCareful() && this.getStatus().damage > 10000) {
                              mod = 19;
                           }

                           if (this.loggerCreature1 > 0L) {
                              mod = 19;
                           }

                           int seed = Server.rand.nextInt(Math.max(2, max - this.template.getMoveRate() * mod));
                           if (this.getPositionZ() < 0.0F) {
                              seed -= 100;
                           }

                           if (seed < 8 || this.isSpiritGuard() && this.citizenVillage != this.currentVillage || this.shouldFlee()) {
                              this.startPathing(seed);
                           }
                        }

                        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                           if (Server.rand.nextInt(Math.max(1, 1000 - this.template.getMoveRate())) < 5 || this.loggerCreature1 > 0L) {
                              float xMod = this.getPosX() % 4.0F;
                              float yMod = this.getPosY() % 4.0F;
                              if (xMod > 3.5F || xMod < 0.5F || yMod > 3.5F || yMod < 0.5F) {
                                 this.takeSimpleStep();
                              }
                           }

                           if (this.shouldFlee()
                              && this.getPathfindCounter() > 10
                              && this.targetPathTile != null
                              && (this.getTileX() != this.targetPathTile.getTileX() || this.getTileY() != this.targetPathTile.getTileY())) {
                              if (this.getPathfindCounter() % 50 == 0 && Server.rand.nextFloat() < 0.05F) {
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
      }
   }

   public float getMoveModifier(int tile) {
      short height = Tiles.decodeHeight(tile);
      return height < 2 ? 0.5F * this.status.getMovementTypeModifier() : Tiles.getTile(Tiles.decodeType(tile)).speed * this.status.getMovementTypeModifier();
   }

   public boolean mayManageGuards() {
      return this.citizenVillage != null ? this.citizenVillage.isActionAllowed((short)67, this) : false;
   }

   public boolean isMoving() {
      return this.status.isMoving();
   }

   public static final float normalizeAngle(float angle) {
      return MovementChecker.normalizeAngle(angle);
   }

   public final void checkBridgeMove(VolaTile oldTile, VolaTile newtile, float diffZ) {
      if (this.getBridgeId() == -10L && newtile.getStructure() != null) {
         BridgePart[] bridgeParts = newtile.getBridgeParts();
         if (bridgeParts != null) {
            for(BridgePart bp : bridgeParts) {
               if (bp.isFinished()) {
                  boolean enter = false;
                  float nz = Zones.calculatePosZ(
                     this.getPosX(), this.getPosY(), newtile, this.isOnSurface(), this.isFloating(), this.getPositionZ(), this, bp.getStructureId()
                  );
                  float newDiff = Math.abs(nz - this.getPositionZ());
                  float maxDiff = 1.3F;
                  if (oldTile != null) {
                     if (bp.getDir() != 0 && bp.getDir() != 4) {
                        if (oldTile.getTileX() == newtile.getTileX() && newDiff < 1.3F && bp.hasAnExit()) {
                           enter = true;
                        }
                     } else if (oldTile.getTileY() == newtile.getTileY() && newDiff < 1.3F && bp.hasAnExit()) {
                        enter = true;
                     }
                  } else {
                     enter = newDiff < 1.3F;
                  }

                  if (enter) {
                     this.setBridgeId(bp.getStructureId());
                     float newDiffZ = nz - this.getPositionZ();
                     this.setPositionZ(nz);
                     this.moved(0.0F, 0.0F, newDiffZ, 0, 0);
                     break;
                  }
               }
            }
         }
      } else if (this.getBridgeId() != -10L) {
         boolean leave = true;
         if (oldTile != null) {
            BridgePart[] bridgeParts = oldTile.getBridgeParts();
            if (bridgeParts != null) {
               for(BridgePart bp : bridgeParts) {
                  if (bp.isFinished()) {
                     if (bp.getDir() != 0 && bp.getDir() != 4) {
                        if (oldTile.getTileY() != newtile.getTileY()) {
                           leave = false;
                        }
                     } else if (oldTile.getTileX() != newtile.getTileX()) {
                        leave = false;
                     }
                  }
               }
            }
         }

         if (leave) {
            if (newtile.getStructure() != null && newtile.getStructure().getWurmId() == this.getBridgeId()) {
               BridgePart[] bridgeParts = newtile.getBridgeParts();
               boolean foundBridge = false;

               for(BridgePart bp : bridgeParts) {
                  foundBridge = true;
                  if (!bp.isFinished()) {
                     this.setBridgeId(-10L);
                     return;
                  }
               }

               if (foundBridge) {
                  for(BridgePart bp : bridgeParts) {
                     if (bp.isFinished() && bp.hasAnExit()) {
                        this.setBridgeId(bp.getStructureId());
                        return;
                     }
                  }
               }
            } else {
               this.setBridgeId(-10L);
            }
         }
      }
   }

   public boolean moveAlongPath() {
      long start = System.nanoTime();

      try {
         Path path = null;
         int mvs = 2;
         if (this.target != -10L) {
            mvs = 3;
         }

         if (this.getSize() >= 5) {
            mvs += 3;
         }

         for(int x = 0; x < mvs; ++x) {
            path = this.status.getPath();
            if (path != null && !path.isEmpty()) {
               PathTile next = path.getFirst();
               if (next.getTileX() == this.getCurrentTile().tilex && next.getTileY() == this.getCurrentTile().tiley) {
                  boolean canRemove = true;
                  if (next.hasSpecificPos()) {
                     float diffX = this.status.getPositionX() - next.getPosX();
                     float diffY = this.status.getPositionY() - next.getPosY();
                     double totalDist = Math.sqrt((double)(diffX * diffX + diffY * diffY));
                     float lMod = this.getMoveModifier(
                        (this.isOnSurface() ? Server.surfaceMesh : Server.caveMesh)
                           .getTile((int)this.status.getPositionX() >> 2, (int)this.status.getPositionY() >> 2)
                     );
                     if (totalDist > (double)(this.getSpeed() * lMod)) {
                        canRemove = false;
                     }
                  }

                  if (canRemove) {
                     path.removeFirst();
                     if (this.getTarget() != null
                        && this.getTarget().getTileX() == this.getTileX()
                        && this.getTarget().getTileY() == this.getTileY()
                        && this.getTarget().getFloorLevel() != this.getFloorLevel()) {
                        if (this.isSpiritGuard()) {
                           this.pushToFloorLevel(this.getTarget().getFloorLevel());
                        } else if (this.canOpenDoors()) {
                           Floor[] floors = this.getCurrentTile()
                              .getFloors(
                                 Math.min(this.getFloorLevel(), this.getTarget().getFloorLevel()) * 30,
                                 Math.max(this.getFloorLevel(), this.getTarget().getFloorLevel()) * 30
                              );

                           for(Floor f : floors) {
                              if (this.getTarget().getFloorLevel() > this.getFloorLevel()) {
                                 if (f.getFloorLevel() == this.getFloorLevel() + 1 && (f.isOpening() || f.isAPlan())) {
                                    this.pushToFloorLevel(f.getFloorLevel());
                                    break;
                                 }
                              } else if (f.getFloorLevel() == this.getFloorLevel() && (f.isOpening() || f.isAPlan())) {
                                 this.pushToFloorLevel(f.getFloorLevel() - 1);
                                 break;
                              }
                           }
                        }
                     }

                     if (path.isEmpty()) {
                        return true;
                     }

                     next = path.getFirst();
                  }
               }

               float lPosX = this.status.getPositionX();
               float lPosY = this.status.getPositionY();
               float lPosZ = this.status.getPositionZ();
               float lRotation = this.status.getRotation();
               double lNewRotation = next.hasSpecificPos()
                  ? Math.atan2((double)(next.getPosY() - lPosY), (double)(next.getPosX() - lPosX))
                  : Math.atan2((double)((float)((next.getTileY() << 2) + 2) - lPosY), (double)((float)((next.getTileX() << 2) + 2) - lPosX));
               lRotation = (float)(lNewRotation * (180.0 / Math.PI)) + 90.0F;
               int lOldTileX = (int)lPosX >> 2;
               int lOldTileY = (int)lPosY >> 2;
               MeshIO lMesh;
               if (this.isOnSurface()) {
                  lMesh = Server.surfaceMesh;
               } else {
                  lMesh = Server.caveMesh;
               }

               float lMod = this.getMoveModifier(lMesh.getTile(lOldTileX, lOldTileY));
               float lXPosMod = (float)Math.sin((double)(lRotation * (float) (Math.PI / 180.0))) * this.getSpeed() * lMod;
               float lYPosMod = -((float)Math.cos((double)(lRotation * (float) (Math.PI / 180.0)))) * this.getSpeed() * lMod;
               int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
               int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
               int lDiffTileX = lNewTileX - lOldTileX;
               int lDiffTileY = lNewTileY - lOldTileY;
               if (Math.abs(lDiffTileX) > 1 || Math.abs(lDiffTileY) > 1) {
                  logger.log(Level.WARNING, this.getName() + "," + this.getWurmId() + " diffTileX=" + lDiffTileX + ", y=" + lDiffTileY);
               }

               if (lDiffTileX != 0 || lDiffTileY != 0) {
                  if (!this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(lMesh.getTile(lNewTileX, lNewTileY)))) {
                     this.rotateRandom(lRotation, 45);

                     try {
                        this.takeSimpleStep();
                     } catch (NoSuchPlayerException var34) {
                     } catch (NoSuchCreatureException var35) {
                     }

                     return true;
                  }

                  if (!this.isGhost()) {
                     BlockingResult result = Blocking.getBlockerBetween(
                        this,
                        this.getPosX(),
                        this.getPosY(),
                        lPosX + lXPosMod,
                        lPosY + lYPosMod,
                        this.getPositionZ(),
                        this.getPositionZ(),
                        this.isOnSurface(),
                        this.isOnSurface(),
                        false,
                        6,
                        true,
                        -10L,
                        this.getBridgeId(),
                        this.getBridgeId(),
                        this.followsGround()
                     );
                     if (result != null) {
                        boolean foundDoor = false;

                        for(Blocker blocker : result.getBlockerArray()) {
                           if (blocker.isDoor() && !blocker.canBeOpenedBy(this, false)) {
                              foundDoor = true;
                           }
                        }

                        if (!foundDoor) {
                           path.clear();
                           return true;
                        }
                     }
                  }

                  if (!next.hasSpecificPos() && next.getTileX() == lNewTileX && next.getTileY() == lNewTileY) {
                     path.removeFirst();
                  }

                  movesx += lDiffTileX;
                  movesy += lDiffTileY;
               }

               int oldDeciZ = (int)(lPosZ * 10.0F);
               lPosX += lXPosMod;
               lPosY += lYPosMod;
               if (lPosX >= (float)(Zones.worldTileSizeX - 1 << 2) || lPosX < 0.0F || lPosY < 0.0F || lPosY >= (float)(Zones.worldTileSizeY - 1 << 2)) {
                  this.destroy();
                  return true;
               }

               lPosZ = this.calculatePosZ();
               int newDeciZ = (int)(lPosZ * 10.0F);
               if ((double)lPosZ < -0.5) {
                  if (this.isSubmerged()) {
                     if (this.isFloating() && (float)newDeciZ > this.template.offZ * 10.0F) {
                        this.rotateRandom(lRotation, 100);
                        if (this.target != -10L) {
                           this.setTarget(-10L, true);
                        }

                        return true;
                     }

                     if (!this.isFloating() && lPosZ > -5.0F && oldDeciZ < newDeciZ) {
                        this.rotateRandom(lRotation, 100);
                        if (this.target != -10L) {
                           this.setTarget(-10L, true);
                        }

                        return true;
                     }

                     if (lPosZ < -5.0F) {
                        if (x == 3) {
                           if (this.isFloating()) {
                              lPosZ = this.template.offZ;
                           } else {
                              float newdiff = Math.max(-1.0F, Math.min(1.0F, (float)Server.rand.nextGaussian()));
                              float newPosZ = Math.max(lPosZ, Math.min(-5.0F, this.getPositionZ() + newdiff));
                              lPosZ = newPosZ;
                           }
                        }
                     } else if (x == 3 && this.isFloating()) {
                        lPosZ = this.template.offZ;
                     }
                  } else {
                     lPosZ = Math.max(-1.25F, lPosZ);
                     if (this.isFloating()) {
                        lPosZ = Math.max(this.template.offZ, lPosZ);
                     }
                  }
               }

               this.status.setPositionX(lPosX);
               this.status.setPositionY(lPosY);
               this.status.setPositionZ(lPosZ);
               this.status.setRotation(lRotation);
               this.moved(lPosX - lPosX, lPosY - lPosY, lPosZ - lPosZ, lDiffTileX, lDiffTileY);
            }
         }

         if (path == null) {
            return true;
         } else {
            return this.pathRecalcLength > 0 && path.getSize() <= this.pathRecalcLength ? true : path.isEmpty();
         }
      } finally {
         ;
      }
   }

   protected boolean startDestroyingWall(Wall wall) {
      try {
         BehaviourDispatcher.action(this, this.communicator, this.getEquippedWeapon((byte)14).getWurmId(), wall.getId(), (short)180);
         return false;
      } catch (FailedException var3) {
         return true;
      } catch (NoSuchBehaviourException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         return true;
      } catch (NoSuchCreatureException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         return true;
      } catch (NoSuchItemException var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
         return true;
      } catch (NoSuchPlayerException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         return true;
      } catch (NoSuchWallException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         return true;
      } catch (NoSpaceException var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         return true;
      }
   }

   public void followLeader() {
      float iposx = this.leader.getStatus().getPositionX();
      float iposy = this.leader.getStatus().getPositionY();
      float diffx = iposx - this.status.getPositionX();
      float diffy = iposy - this.status.getPositionY();
      int diff = (int)Math.max(Math.abs(diffx), Math.abs(diffy));
      if (!(diffx < 0.0F) || !(this.status.getPositionX() < 10.0F)) {
         if (!(diffy < 0.0F) || !(this.status.getPositionY() < 10.0F)) {
            if (!(diffy > 0.0F) || !(this.status.getPositionY() > Zones.worldMeterSizeY - 10.0F)) {
               if (!(diffx > 0.0F) || !(this.status.getPositionX() > Zones.worldMeterSizeX - 10.0F)) {
                  if (diff > 35) {
                     logger.log(Level.INFO, this.leader.getName() + " moved " + diff + "diffx=" + diffx + ", diffy=" + diffy);
                     this.setLeader(null);
                  } else if (diffx > 4.0F || diffy > 4.0F || diffx < -4.0F || diffy < -4.0F) {
                     float lPosX = this.status.getPositionX();
                     float lPosY = this.status.getPositionY();
                     float lPosZ = this.status.getPositionZ();
                     int lOldTileX = (int)lPosX >> 2;
                     int lOldTileY = (int)lPosY >> 2;
                     double lNewrot = Math.atan2((double)(iposy - lPosY), (double)(iposx - lPosX));
                     lNewrot = lNewrot * (180.0 / Math.PI) + 90.0;
                     if (lNewrot > 360.0) {
                        lNewrot -= 360.0;
                     }

                     if (lNewrot < 0.0) {
                        lNewrot += 360.0;
                     }

                     float movex = 0.0F;
                     float movey = 0.0F;
                     if (diffx < -4.0F) {
                        movex = diffx + 4.0F;
                     } else if (diffx > 4.0F) {
                        movex = diffx - 4.0F;
                     }

                     if (diffy < -4.0F) {
                        movey = diffy + 4.0F;
                     } else if (diffy > 4.0F) {
                        movey = diffy - 4.0F;
                     }

                     float lXPosMod = (float)Math.sin(lNewrot * (float) (Math.PI / 180.0)) * Math.abs(movex + Server.rand.nextFloat());
                     float lYPosMod = -((float)Math.cos(lNewrot * (float) (Math.PI / 180.0))) * Math.abs(movey + Server.rand.nextFloat());
                     float newPosX = lPosX + lXPosMod;
                     float newPosY = lPosY + lYPosMod;
                     int lNewTileX = (int)newPosX >> 2;
                     int lNewTileY = (int)newPosY >> 2;
                     int lDiffTileX = lNewTileX - lOldTileX;
                     int lDiffTileY = lNewTileY - lOldTileY;
                     if ((lDiffTileX != 0 || lDiffTileY != 0) && !this.isGhost() && this.leader.getBridgeId() < 0L && this.getBridgeId() < 0L) {
                        BlockingResult result = Blocking.getBlockerBetween(
                           this,
                           lPosX,
                           lPosY,
                           newPosX,
                           newPosY,
                           this.getPositionZ(),
                           this.leader.getPositionZ(),
                           this.isOnSurface(),
                           this.isOnSurface(),
                           false,
                           2,
                           -1L,
                           this.getBridgeId(),
                           this.getBridgeId(),
                           this.followsGround()
                        );
                        if (result != null) {
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
                     }

                     if (!this.leader.isOnSurface()
                        && !this.isOnSurface()
                        && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile((int)newPosX >> 2, (int)newPosY >> 2)))) {
                        newPosX = iposx;
                        newPosY = iposy;
                     }

                     float newPosZ = this.calculatePosZ();
                     if (!this.isSwimming() && (double)newPosZ < -0.71 && newPosZ < lPosZ) {
                        this.setLeader(null);
                        this.status.setPositionZ(newPosZ);
                     } else {
                        newPosZ = Math.max(-1.25F, newPosZ);
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
            }
         }
      }
   }

   public void sendAttitudeChange() {
      if (this.currentTile != null) {
         this.currentTile.checkChangedAttitude(this);
      }
   }

   public final void takeSimpleStep() throws NoSuchCreatureException, NoSuchPlayerException {
      long start = 0L;

      try {
         int mvs = 2;
         if (this.target != -10L) {
            mvs = 3;
         }

         if (this.getSize() >= 5) {
            mvs += 3;
         }

         for(int x = 0; x < mvs; ++x) {
            float lPosX = this.status.getPositionX();
            float lPosY = this.status.getPositionY();
            float lPosZ = this.status.getPositionZ();
            float lRotation = this.status.getRotation();
            float oldPosZ = lPosZ;
            int oldDeciZ = (int)(lPosZ * 10.0F);
            int lOldTileX = (int)lPosX >> 2;
            int lOldTileY = (int)lPosY >> 2;
            if (this.target == -10L) {
               if (this.isOnSurface()) {
                  int rand = Server.rand.nextInt(100);
                  if (rand < 10) {
                     float lXPosMod = (float)Math.sin((double)(lRotation * (float) (Math.PI / 180.0))) * 12.0F;
                     float lYPosMod = -((float)Math.cos((double)(lRotation * (float) (Math.PI / 180.0)))) * 12.0F;
                     int lNewTileX = Zones.safeTileX((int)(lPosX + lXPosMod) >> 2);
                     int lNewTileY = Zones.safeTileY((int)(lPosY + lYPosMod) >> 2);
                     int tile = Zones.getTileIntForTile(lNewTileX, lNewTileY, this.getLayer());
                     if (this.isTargetTileTooHigh(lNewTileX, lNewTileY, tile, lPosZ < 0.0F)) {
                        short[] lLowestNode = this.getLowestTileCorner((short)lOldTileX, (short)lOldTileY);
                        this.turnTowardsTile(lLowestNode[0], lLowestNode[1]);
                     }
                  } else if (rand < 12) {
                     this.rotateRandom(lRotation, 100);
                  } else if (rand < 15) {
                     lRotation = normalizeAngle(lRotation + (float)Server.rand.nextInt(100));
                  }
               } else {
                  int rand = Server.rand.nextInt(100);
                  if (rand < 2) {
                     this.rotateRandom(lRotation, 100);
                  } else if (rand < 5) {
                     lRotation = normalizeAngle(lRotation + (float)Server.rand.nextInt(100));
                  }
               }
            } else {
               this.turnTowardsCreature(this.getTarget());
            }

            lRotation = normalizeAngle(lRotation);
            float lMoveModifier;
            if (!this.isOnSurface()) {
               lMoveModifier = this.getMoveModifier(Server.caveMesh.getTile(lOldTileX, lOldTileY));
            } else {
               lMoveModifier = this.getMoveModifier(Server.surfaceMesh.getTile(lOldTileX, lOldTileY));
            }

            float lXPosMod = (float)Math.sin((double)(lRotation * (float) (Math.PI / 180.0))) * this.getSpeed() * lMoveModifier;
            float lYPosMod = -((float)Math.cos((double)(lRotation * (float) (Math.PI / 180.0)))) * this.getSpeed() * lMoveModifier;
            int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
            int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
            int lDiffTileX = lNewTileX - lOldTileX;
            int lDiffTileY = lNewTileY - lOldTileY;
            if ((lDiffTileX != 0 || lDiffTileY != 0) && !this.isGhost()) {
               if (!this.isOnSurface()) {
                  if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lOldTileX, lOldTileY)))) {
                     logger.log(Level.INFO, this.getName() + " is in rock at takesimplestep. Dying.");
                     this.die(false, "Suffocated in Rock");
                     return;
                  }

                  if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lNewTileX, lNewTileY)))) {
                     if (!this.currentTile.isTransition) {
                        this.rotateRandom(lRotation, 45);
                        return;
                     }

                     this.sendToLoggers(lPosZ + " setting to surface then moving.");
                     if (Tiles.isMineDoor(Tiles.decodeType(Server.caveMesh.getTile(this.getTileX(), this.getTileY())))
                        && !MineDoorPermission.getPermission(this.getTileX(), this.getTileY()).mayPass(this)) {
                        this.rotateRandom(lRotation, 45);
                     } else {
                        this.setLayer(0, true);
                     }

                     return;
                  }
               } else if (Tiles.Tile.TILE_LAVA.id == Tiles.decodeType(Server.surfaceMesh.getTile(lNewTileX, lNewTileY))) {
                  this.rotateRandom(lRotation, 45);
                  return;
               }

               BlockingResult result;
               if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                  result = Blocking.getBlockerBetween(
                     this,
                     lPosX,
                     lPosY,
                     lPosX + lXPosMod,
                     lPosY + lYPosMod,
                     this.getPositionZ(),
                     this.getPositionZ(),
                     this.isOnSurface(),
                     this.isOnSurface(),
                     false,
                     6,
                     -1L,
                     this.getBridgeId(),
                     this.getBridgeId(),
                     this.followsGround()
                  );
               } else {
                  result = Blocking.getBlockerBetween(
                     this,
                     lPosX,
                     lPosY,
                     lPosX + lXPosMod,
                     lPosY + lYPosMod,
                     this.getPositionZ(),
                     this.getPositionZ(),
                     this.isOnSurface(),
                     this.isOnSurface(),
                     false,
                     6,
                     -1L,
                     this.getBridgeId(),
                     this.getBridgeId(),
                     this.followsGround()
                  );
               }

               if (result != null) {
                  Blocker first = result.getFirstBlocker();
                  if (!this.isKingdomGuard() && !this.isSpiritGuard()) {
                     if (!Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                        this.rotateRandom(lRotation, 100);
                        return;
                     }

                     this.turnTowardsTile((short)this.getTileX(), (short)this.getTileY());
                     this.rotateRandom(this.status.getRotation(), 45);
                     x = 0;
                     this.getStatus().setMoving(false);
                     continue;
                  }

                  if (!first.isDoor()) {
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

            lPosX += lXPosMod;
            lPosY += lYPosMod;
            if (lPosX >= (float)(Zones.worldTileSizeX - 1 << 2) || lPosX < 0.0F || lPosY < 0.0F || lPosY >= (float)(Zones.worldTileSizeY - 1 << 2)) {
               this.destroy();
               return;
            }

            if (this.getFloorLevel() == 0) {
               try {
                  lPosZ = Zones.calculateHeight(lPosX, lPosY, this.isOnSurface());
               } catch (NoSuchZoneException var28) {
                  logger.log(Level.WARNING, this.name + " moved out of zone.");
               }

               if (this.isFloating()) {
                  lPosZ = Math.max(this.template.offZ, lPosZ);
               }

               int newDeciZ = (int)(lPosZ * 10.0F);
               if ((double)lPosZ < 0.5) {
                  if (this.isSubmerged()) {
                     if (this.isFloating() && (float)newDeciZ > this.template.offZ * 10.0F) {
                        this.rotateRandom(lRotation, 100);
                        if (this.target != -10L) {
                           this.setTarget(-10L, true);
                        }

                        return;
                     }

                     if (!this.isFloating() && lPosZ > -5.0F && oldDeciZ < newDeciZ) {
                        this.rotateRandom(lRotation, 100);
                        if (this.target != -10L) {
                           this.setTarget(-10L, true);
                        }

                        return;
                     }

                     if (lPosZ < -5.0F) {
                        if (x == 3) {
                           if (this.isFloating()) {
                              lPosZ = this.template.offZ;
                           } else {
                              float newdiff = Math.max(-1.0F, Math.min(1.0F, (float)Server.rand.nextGaussian()));
                              float newPosZ = Math.max(lPosZ, Math.min(-5.0F, this.getPositionZ() + newdiff));
                              lPosZ = newPosZ;
                           }
                        }
                     } else if (x == 3 && this.isFloating()) {
                        lPosZ = this.template.offZ;
                     }
                  }

                  if ((lPosZ > -2.0F || oldDeciZ <= -20) && (oldDeciZ < 0 || this.target != -10L) && this.isSwimming()) {
                     lPosZ = Math.max(-1.25F, lPosZ);
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
               this.status.setPositionZ(lPosZ + 0.25F);
            } else {
               this.status.setPositionZ(lPosZ);
            }

            this.status.setRotation(lRotation);
            this.moved(lPosX - lPosX, lPosY - lPosY, lPosZ - oldPosZ, lDiffTileX, lDiffTileY);
         }
      } finally {
         ;
      }
   }

   public void rotateRandom(float aRot, int degrees) {
      aRot -= (float)degrees;
      aRot += (float)Server.rand.nextInt(degrees * 2);
      aRot = normalizeAngle(aRot);
      this.status.setRotation(aRot);
      this.moved(0.0F, 0.0F, 0.0F, 0, 0);
   }

   public int getAttackDistance() {
      return this.template.getSize();
   }

   public void moved(float diffX, float diffY, float diffZ, int aDiffTileX, int aDiffTileY) {
      if (!this.isDead()) {
         try {
            if (!this.isPlayer() && !this.isWagoner()) {
               try {
                  this.getVisionArea().move(aDiffTileX, aDiffTileY);
               } catch (IOException var10) {
                  return;
               }

               try {
                  this.getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
               } catch (NoSuchPlayerException var8) {
               } catch (NoSuchCreatureException var9) {
               }

               this.getVisionArea().linkZones(aDiffTileX, aDiffTileY);
            } else {
               this.movementScheme.move(diffX, diffY, diffZ);
               if ((this.isWagoner() || this.hasLink()) && this.getVisionArea() != null) {
                  try {
                     this.getVisionArea().move(aDiffTileX, aDiffTileY);
                  } catch (IOException var13) {
                     return;
                  }
               }

               try {
                  this.getCurrentTile().creatureMoved(this.id, diffX, diffY, diffZ, aDiffTileX, aDiffTileY);
               } catch (NoSuchPlayerException var11) {
               } catch (NoSuchCreatureException var12) {
               }

               if (this.hasLink() && this.getVisionArea() != null) {
                  this.getVisionArea().linkZones(aDiffTileX, aDiffTileY);
               }
            }
         } catch (NullPointerException var16) {
            try {
               if (!this.isPlayer()) {
                  this.createVisionArea();
               }

               return;
            } catch (Exception var15) {
            }
         }

         if (diffX != 0.0F || diffY != 0.0F) {
            try {
               if (this.isPlayer() && this.actions.getCurrentAction().isInterruptedAtMove()) {
                  boolean stop = true;
                  if (this.actions.getCurrentAction().getNumber() == 136) {
                     this.getCommunicator().sendToggle(3, false);
                  } else if ((
                        this.actions.getCurrentAction().getNumber() == 329
                           || this.actions.getCurrentAction().getNumber() == 162
                           || this.actions.getCurrentAction().getNumber() == 160
                     )
                     && this.getVehicle() != -10L) {
                     stop = false;
                  }

                  if (stop) {
                     this.communicator.sendSafeServerMessage("You must not move while doing that.");
                     this.stopCurrentAction();
                  }
               }
            } catch (NoSuchActionException var14) {
            }

            if ((aDiffTileX != 0 || aDiffTileY != 0) && this.musicPlayer != null) {
               this.musicPlayer.moveTile(this.getCurrentTileNum(), this.getPositionZ() <= 0.0F);
            }
         }

         if (this.status.isTrading()) {
            Trade trade = this.status.getTrade();
            Creature lOpponent = null;
            if (trade.creatureOne == this) {
               lOpponent = trade.creatureTwo;
            } else {
               lOpponent = trade.creatureOne;
            }

            if (rangeTo(this, lOpponent) > 6) {
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
         double lNewrot = Math.atan2(
            (double)(targ.getStatus().getPositionY() - this.getStatus().getPositionY()),
            (double)(targ.getStatus().getPositionX() - this.getStatus().getPositionX())
         );
         this.setRotation((float)(lNewrot * (180.0 / Math.PI)) + 90.0F);
         if (this.isSubmerged()) {
            try {
               float currFloor = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface());
               float maxHeight = this.isFloating() ? this.template.offZ : Math.min(targ.getPositionZ(), Math.max(-5.0F, currFloor));
               float oldHeight = this.getPositionZ();
               int diffCentiZ = (int)((maxHeight - oldHeight) * 100.0F);
               this.moved(0.0F, 0.0F, (float)diffCentiZ, 0, 0);
               return;
            } catch (NoSuchZoneException var8) {
            }
         }

         this.moved(0.0F, 0.0F, 0.0F, 0, 0);
      }
   }

   public void turnTowardsPoint(float posX, float posY) {
      double lNewrot = Math.atan2((double)(posY - this.getStatus().getPositionY()), (double)(posX - this.getStatus().getPositionX()));
      this.setRotation((float)(lNewrot * (180.0 / Math.PI)) + 90.0F);
      this.moved(0.0F, 0.0F, 0.0F, 0, 0);
   }

   public void turnTowardsTile(short tilex, short tiley) {
      double lNewrot = Math.atan2(
         (double)((float)((tiley << 2) + 2) - this.getStatus().getPositionY()), (double)((float)((tilex << 2) + 2) - this.getStatus().getPositionX())
      );
      this.setRotation((float)(lNewrot * (180.0 / Math.PI)) + 90.0F);
      this.moved(0.0F, 0.0F, 0.0F, 0, 0);
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
      return this.getStatus() != null ? this.getStatus().getAgeString() + " " + this.getStatus().getTypeString() + this.name : "Unknown";
   }

   public String getName() {
      String fullName = this.name;
      if (this.isWagoner()) {
         return fullName;
      } else {
         if (this.isAnimal() || this.isMonster()) {
            if (this.name.toLowerCase().compareTo(this.template.getName().toLowerCase()) == 0) {
               fullName = this.getPrefixes() + this.name.toLowerCase();
            } else {
               fullName = this.getPrefixes() + StringUtilities.raiseFirstLetterOnly(this.name);
            }
         }

         return this.petName.length() > 0 ? fullName + " '" + this.petName + "'" : fullName;
      }
   }

   public String getNamePossessive() {
      String toReturn = this.getName();
      return toReturn.endsWith("s") ? toReturn + "'" : toReturn + "'s";
   }

   public String getPrefixes() {
      return this.isUnique()
         ? "The " + this.getStatus().getAgeString() + " " + this.getStatus().getFatString() + this.getStatus().getTypeString()
         : this.getStatus().getAgeString() + " " + this.getStatus().getFatString() + this.getStatus().getTypeString();
   }

   public void setName(String _name) {
      this.name = _name;
   }

   public void setPetName(String aPetName) {
      if (aPetName == null) {
         this.petName = "";
      } else {
         this.petName = aPetName.substring(0, Math.min(19, aPetName.length()));
      }
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
      skill.skillCheck(50.0, 0.0, false, 3600.0F);
      if (skill.getKnowledge(0.0) > knowledge) {
         message = this.getName() + " trains some  " + sname + " and now have skill " + skill.getKnowledge(0.0);
      }

      logger.log(Level.INFO, message);
   }

   public void setSkill(int skill, float val) {
      try {
         Skill sktomod = this.skills.getSkill(skill);
         sktomod.setKnowledge((double)val, false);
      } catch (NoSuchSkillException var4) {
         this.skills.learn(skill, val);
      }
   }

   public void sendSkills() {
      try {
         this.loadAffinities();
         Map<Integer, Skill> skilltree = this.skills.getSkillTree();

         for(Integer number : skilltree.keySet()) {
            try {
               Skill skill = skilltree.get(number);
               int[] needed = skill.getDependencies();
               int parentSkillId = 0;
               if (needed.length > 0) {
                  parentSkillId = needed[0];
               }

               if (parentSkillId != 0) {
                  int parentType = SkillSystem.getTypeFor(parentSkillId);
                  if (parentType == 0) {
                     parentSkillId = Integer.MAX_VALUE;
                  }
               } else if (skill.getType() == 1) {
                  parentSkillId = 2147483646;
               } else {
                  parentSkillId = Integer.MAX_VALUE;
               }

               this.getCommunicator()
                  .sendAddSkill(number, parentSkillId, skill.getName(), (float)skill.getRealKnowledge(), (float)skill.getMinimumValue(), skill.affinity);
            } catch (NullPointerException var8) {
               logger.log(Level.WARNING, "Inconsistency: " + this.getName() + " forgetting skill with number " + number, (Throwable)var8);
            }
         }
      } catch (Exception var9) {
         logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + var9.getMessage(), (Throwable)var9);
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
            this.getCommunicator().sendAddSkill(2147483646, 0, "Characteristics", 0.0F, 0.0F, 0);
            this.getCommunicator().sendAddSkill(2147483643, 0, "Religion", 0.0F, 0.0F, 0);
            this.getCommunicator().sendAddSkill(Integer.MAX_VALUE, 0, "Skills", 0.0F, 0.0F, 0);
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
               this.skills.learn(1023, 30.0F);
               this.skills.learn(10052, 50.0F);
               this.skills.getSkill(102).setKnowledge(25.0, false);
               this.skills.getSkill(103).setKnowledge(25.0, false);
            }

            if (Servers.localServer.testServer && Servers.localServer.entryServer && WurmId.getType(this.id) == 0) {
               int level = 20;
               this.skills.learn(1023, (float)level);
               this.skills.learn(10025, (float)level);
               this.skills.learn(10006, (float)level);
               this.skills.learn(10023, (float)level);
               this.skills.learn(10022, (float)level);
               this.skills.learn(10020, (float)level);
               this.skills.learn(10021, (float)level);
               this.skills.learn(10019, (float)level);
               this.skills.learn(10001, (float)level);
               this.skills.learn(10024, (float)level);
               this.skills.learn(10005, (float)level);
               this.skills.learn(10027, (float)level);
               this.skills.learn(10028, (float)level);
               this.skills.learn(10026, (float)level);
               this.skills.learn(10064, (float)level);
               this.skills.learn(10061, (float)level);
               this.skills.learn(10062, (float)level);
               this.skills.learn(10063, (float)level);
               this.skills.learn(1002, (float)level / 2.0F);
               this.skills.learn(1003, (float)level / 2.0F);
               this.skills.learn(10056, (float)level);
               this.skills.getSkill(104).setKnowledge(23.0, false);
               this.skills.getSkill(1).setKnowledge(3.0, false);
               this.skills.getSkill(102).setKnowledge(23.0, false);
               this.skills.getSkill(103).setKnowledge(23.0, false);
               this.skills.learn(10053, (float)level);
               this.skills.learn(10054, (float)level);
               level = (int)(Server.rand.nextFloat() * 100.0F);
               this.skills.learn(1030, (float)level);
               this.skills.learn(10081, (float)level);
               this.skills.learn(10079, (float)level);
               this.skills.learn(10080, (float)level);
            }
         }

         this.setMoveLimits();
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Failed to load and create skills for creature with name " + this.name + ":" + var3.getMessage(), (Throwable)var3);
      }
   }

   public void addStructureTile(VolaTile toAdd, byte structureType) {
      if (this.structure == null) {
         this.structure = Structures.createStructure(
            structureType, this.name + "'s planned structure", WurmId.getNextPlanId(), toAdd.tilex, toAdd.tiley, this.isOnSurface()
         );
         this.status.setBuildingId(this.structure.getWurmId());
      } else {
         try {
            this.structure.addBuildTile(toAdd, false);
            if (structureType == 0) {
               this.structure.clearAllWallsAndMakeWallsForStructureBorder(toAdd);
            }
         } catch (NoSuchZoneException var4) {
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
         } else {
            return this.status.sex == 1
               ? "model.creature.humanoid.human.player.zombie.female" + WurmCalendar.getSpecialMapping(true)
               : "model.creature.humanoid.human.player.zombie" + WurmCalendar.getSpecialMapping(true);
         }
      } else if (this.getUndeadType() == 2) {
         return "model.creature.humanoid.human.skeleton" + WurmCalendar.getSpecialMapping(true);
      } else {
         return this.getUndeadType() == 3 ? "model.creature.humanoid.human.spirit.shadow" + WurmCalendar.getSpecialMapping(true) : this.getModelName();
      }
   }

   public String getModelName() {
      if (this.isReborn()) {
         if (this.status.sex == 0) {
            return this.template.getModelName() + ".zombie.male" + WurmCalendar.getSpecialMapping(true);
         } else {
            return this.status.sex == 1
               ? this.template.getModelName() + ".zombie.female" + WurmCalendar.getSpecialMapping(true)
               : this.template.getModelName() + ".zombie" + WurmCalendar.getSpecialMapping(true);
         }
      } else if (this.template.isHorse || this.template.isColoured) {
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
      } else if (this.template.isBlackOrWhite) {
         StringBuilder s = new StringBuilder();
         s.append(this.template.getModelName());
         if (this.status.sex == 0) {
            s.append(".male");
         }

         if (this.status.sex == 1) {
            s.append(".female");
         }

         if (!this.hasTrait(15)
            && !this.hasTrait(16)
            && !this.hasTrait(18)
            && !this.hasTrait(24)
            && !this.hasTrait(25)
            && !this.hasTrait(23)
            && !this.hasTrait(30)
            && !this.hasTrait(31)
            && !this.hasTrait(32)
            && !this.hasTrait(33)
            && !this.hasTrait(34)
            && this.hasTrait(17)) {
            s.append(".black");
         }

         if (this.status.disease > 0) {
            s.append(".diseased");
         }

         s.append(WurmCalendar.getSpecialMapping(true));
         return s.toString();
      } else if (this.template.getTemplateId() == 119) {
         StringBuilder s = new StringBuilder();
         FishAI.FishAIData faid = (FishAI.FishAIData)this.getCreatureAIData();
         FishEnums.FishData fd = faid.getFishData();
         s.append(fd.getModelName());
         return s.toString();
      } else {
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
         return 0.2F;
      } else if (this.template.getTemplateId() == 5) {
         return 0.3F;
      } else if (this.template.getTemplateId() == 31 || this.template.getTemplateId() == 30 || this.template.getTemplateId() == 6) {
         return 0.4F;
      } else if (this.template.getTemplateId() == 7) {
         return 0.6F;
      } else {
         return this.template.getTemplateId() != 33 && this.template.getTemplateId() != 32 && this.template.getTemplateId() != 8 ? 1.0F : 0.65F;
      }
   }

   public Structure getStructure() throws NoSuchStructureException {
      if (this.structure == null) {
         throw new NoSuchStructureException("This creature has no structure");
      } else {
         return this.structure;
      }
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
         } catch (Exception var11) {
            logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
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
      } catch (NoSuchZoneException var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
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
      } catch (IOException var2) {
         logger.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
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
      } catch (Exception var9) {
         logger.log(Level.INFO, "Error when deleting creature skills: " + var9.getMessage(), (Throwable)var9);
      }

      try {
         Item[] items = this.possessions.getInventory().getAllItems(true);

         for(int x = 0; x < items.length; ++x) {
            if (!items[x].isUnique()) {
               Items.destroyItem(items[x].getWurmId());
            } else {
               this.dropItem(items[x]);
            }
         }

         Items.destroyItem(this.possessions.getInventory().getWurmId());
      } catch (Exception var11) {
         logger.log(Level.INFO, "Error when decaying items: " + var11.getMessage(), (Throwable)var11);
      }

      try {
         Item[] items = this.getBody().getBodyItem().getAllItems(true);

         for(int x = 0; x < items.length; ++x) {
            if (!items[x].isUnique()) {
               Items.destroyItem(items[x].getWurmId());
            } else {
               this.dropItem(items[x]);
            }
         }

         Items.destroyItem(this.getBody().getBodyItem().getWurmId());
      } catch (Exception var10) {
         logger.log(Level.INFO, "Error when decaying body items: " + var10.getMessage(), (Throwable)var10);
      }

      if (this.citizenVillage != null) {
         Village vill = this.citizenVillage;
         Guard[] guards = this.citizenVillage.getGuards();

         for(Guard lGuard : guards) {
            if (lGuard.getCreature() == this) {
               vill.deleteGuard(this, false);
               if (this.isSpiritGuard()) {
                  vill.plan.destroyGuard(this);
               }
            }
         }

         Wagoner[] wagoners = vill.getWagoners();

         for(Wagoner wagoner : wagoners) {
            if (wagoner.getWurmId() == this.getWurmId()) {
               vill.deleteWagoner(this);
            }
         }
      }

      if (this.isNpcTrader() && Economy.getEconomy().getShop(this, true) != null) {
         if (Economy.getEconomy().getShop(this, true).getMoney() > 0L) {
            Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + Economy.getEconomy().getShop(this, true).getMoney());
         }

         Economy.deleteShop(this.id);
      }

      if (this.isKingdomGuard()) {
         GuardTower tower = Kingdoms.getTower(this);
         if (tower != null) {
            try {
               tower.destroyGuard(this);
            } catch (IOException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            }
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
         for(int x = Zones.safeTileX(this.getTileX() - 3); x < Zones.safeTileX(this.getTileX() + 3); ++x) {
            for(int y = Zones.safeTileY(this.getTileY() - 3); y < Zones.safeTileY(this.getTileY() + 3); ++y) {
               VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
               if (t != null) {
                  Item[] items = t.getItems();

                  for(Item i : items) {
                     if (i.isGuardTower() && !this.isFriendlyKingdom(i.getKingdom())) {
                        GuardTower tower = Kingdoms.getTower(i);
                        if (i.getCurrentQualityLevel() > 50.0F) {
                           if (tower != null) {
                              tower.sendAttackWarning();
                           }

                           this.turnTowardsTile((short)i.getTileX(), (short)i.getTileY());
                           this.playAnimation("fight_strike", false);
                           Server.getInstance().broadCastAction(this.getName() + " attacks the " + i.getName() + ".", this, 5);
                           i.setDamage(i.getDamage() + (float)(this.getStrengthSkill() / 1000.0));
                           if (Server.rand.nextInt(300) == 0) {
                              if (Server.rand.nextBoolean()) {
                                 GuardTower.spawnCommander(i, i.getKingdom());
                              }

                              for(int n = 0; n < 2 + Server.rand.nextInt(4); ++n) {
                                 GuardTower.spawnSoldier(i, i.getKingdom());
                              }
                           }
                        } else if (!Servers.localServer.HOMESERVER && Server.rand.nextInt(300) == 0 && tower != null && !tower.hasLiveGuards()) {
                           Server.getInstance().broadCastAction(this.getName() + " conquers the " + tower.getName() + "!", this, 5);
                           Server.getInstance().broadCastSafe(this.getName() + " conquers " + tower.getName() + ".");
                           Kingdoms.convertTowersWithin(i.getTileX() - 10, i.getTileY() - 10, i.getTileX() + 10, i.getTileY() + 10, this.getKingdomId());
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void breakout() {
      if (!this.isDominated()
         && ((this.isCaveDweller() || this.isBreakFence()) && this.status.hunger >= 60000 || this.isUnique())
         && !this.isSubmerged()
         && Server.rand.nextInt(100) == 0) {
         Village breakoutVillage = Zones.getVillage(this.getTileX(), this.getTileY(), this.isOnSurface());
         if (breakoutVillage != null && breakoutVillage.isPermanent) {
            return;
         }

         if (this.isBreakFence() && this.currentTile != null) {
            if (this.currentTile.getStructure() != null) {
               Wall[] walls = this.currentTile.getWallsForLevel(this.getFloorLevel());
               if (walls.length > 0) {
                  Wall tobreak = walls[Server.rand.nextInt(walls.length)];
                  if (!tobreak.isIndestructible()) {
                     Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                     if (this.isUnique()) {
                        tobreak.setDamage(tobreak.getDamage() + 100.0F);
                     } else {
                        tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0F * tobreak.getDamageModifier());
                     }
                  }
               }
            }

            boolean onSurface = true;
            if ((this.isOnSurface() || this.currentTile.isTransition) && this.isUnique()) {
               VolaTile t = Zones.getTileOrNull(this.getTileX() + 1, this.getTileY(), true);
               if (t != null) {
                  Wall[] walls = t.getWallsForLevel(Math.max(0, this.getFloorLevel()));
                  if (walls.length > 0) {
                     for(Wall tobreak : walls) {
                        if (!tobreak.isIndestructible() && tobreak.getTileX() == this.getTileX() + 1 && !tobreak.isHorizontal()) {
                           Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                           if (this.isUnique()) {
                              tobreak.setDamage(tobreak.getDamage() + 100.0F);
                           } else {
                              tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0F * tobreak.getDamageModifier());
                           }
                        }
                     }
                  }
               }

               t = Zones.getTileOrNull(this.getTileX() - 1, this.getTileY(), true);
               if (t != null) {
                  Wall[] walls = t.getWallsForLevel(Math.max(0, this.getFloorLevel()));
                  if (walls.length > 0) {
                     for(Wall tobreak : walls) {
                        if (!tobreak.isIndestructible() && tobreak.getTileX() == this.getTileX() && !tobreak.isHorizontal()) {
                           Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                           if (this.isUnique()) {
                              tobreak.setDamage(tobreak.getDamage() + 100.0F);
                           } else {
                              tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0F * tobreak.getDamageModifier());
                           }
                        }
                     }
                  }
               }

               t = Zones.getTileOrNull(this.getTileX(), this.getTileY() - 1, true);
               if (t != null) {
                  Wall[] walls = t.getWallsForLevel(Math.max(0, this.getFloorLevel()));
                  if (walls.length > 0) {
                     for(Wall tobreak : walls) {
                        if (!tobreak.isIndestructible() && tobreak.getTileY() == this.getTileY() && tobreak.isHorizontal()) {
                           Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                           if (this.isUnique()) {
                              tobreak.setDamage(tobreak.getDamage() + 100.0F);
                           } else {
                              tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0F * tobreak.getDamageModifier());
                           }
                        }
                     }
                  }
               }

               t = Zones.getTileOrNull(this.getTileX(), this.getTileY() + 1, true);
               if (t != null) {
                  Wall[] walls = t.getWallsForLevel(Math.max(0, this.getFloorLevel()));
                  if (walls.length > 0) {
                     for(Wall tobreak : walls) {
                        if (!tobreak.isIndestructible() && tobreak.getTileY() == this.getTileY() + 1 && tobreak.isHorizontal()) {
                           Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + tobreak.getName() + ".", this, 5);
                           if (this.isUnique()) {
                              tobreak.setDamage(tobreak.getDamage() + 100.0F);
                           } else {
                              tobreak.setDamage(tobreak.getDamage() + (float)this.getStrengthSkill() / 10.0F * tobreak.getDamageModifier());
                           }
                        }
                     }
                  }
               }
            }

            Fence[] fences = this.currentTile.getFencesForLevel(this.currentTile.isTransition ? 0 : this.getFloorLevel());
            boolean onlyHoriz = false;
            boolean onlyVert = false;
            if (fences == null) {
               if (this.isOnSurface()) {
                  if (fences == null) {
                     VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true);
                     if (t != null) {
                        fences = t.getFencesForLevel(this.getFloorLevel());
                        onlyVert = true;
                     }
                  }

                  if (fences == null) {
                     VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true);
                     if (t != null) {
                        fences = t.getFencesForLevel(this.getFloorLevel());
                        onlyHoriz = true;
                     }
                  }
               }

               if (this.currentTile.isTransition) {
                  if (!this.isOnSurface()) {
                     VolaTile t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY(), true);
                     if (t != null) {
                        fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                     }

                     if (fences == null) {
                        t = Zones.getTileOrNull(this.currentTile.getTileX() + 1, this.currentTile.getTileY(), true);
                        if (t != null) {
                           fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                           onlyVert = true;
                        }
                     }

                     if (fences == null) {
                        t = Zones.getTileOrNull(this.currentTile.getTileX(), this.currentTile.getTileY() + 1, true);
                        if (t != null) {
                           fences = t.getFencesForLevel(Math.max(0, this.getFloorLevel()));
                           onlyHoriz = true;
                        }
                     }
                  }

                  if (this.getFloorLevel() <= 0
                     && Tiles.isMineDoor(Tiles.decodeType(Zones.getTileIntForTile(this.currentTile.tilex, this.currentTile.tiley, 0)))) {
                     int currQl = Server.getWorldResource(this.currentTile.tilex, this.currentTile.tiley);
                     int damage = 1000;
                     currQl = Math.max(0, currQl - 1000);
                     Server.setWorldResource(this.currentTile.tilex, this.currentTile.tiley, currQl);

                     try {
                        MethodsStructure.sendDestroySound(
                           this,
                           this.getBody().getBodyPart(13),
                           Tiles.decodeType(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == 25
                        );
                     } catch (Exception var11) {
                        logger.log(Level.INFO, this.getName() + var11.getMessage());
                     }

                     if (currQl == 0) {
                        TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, 0, this.getWurmId(), 174);
                        TileEvent.log(this.currentTile.tilex, this.currentTile.tiley, -1, this.getWurmId(), 174);
                        if (Tiles.decodeType(Server.caveMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                           Server.setSurfaceTile(
                              this.currentTile.tilex,
                              this.currentTile.tiley,
                              Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)),
                              Tiles.Tile.TILE_HOLE.id,
                              (byte)0
                           );
                        } else {
                           Server.setSurfaceTile(
                              this.currentTile.tilex,
                              this.currentTile.tiley,
                              Tiles.decodeHeight(Server.surfaceMesh.getTile(this.currentTile.tilex, this.currentTile.tiley)),
                              Tiles.Tile.TILE_ROCK.id,
                              (byte)0
                           );
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
               for(Fence f : fences) {
                  if (!f.isIndestructible()) {
                     if (f.isHorizontal()) {
                        if (!onlyVert) {
                           Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + f.getName() + ".", this, 5);
                           if (this.isUnique()) {
                              f.setDamage(f.getDamage() + (float)Server.rand.nextInt(100));
                           } else {
                              if (f.getVillage() != null) {
                                 f.getVillage().addTarget(this);
                              }

                              f.setDamage(f.getDamage() + (float)this.getStrengthSkill() / 10.0F * f.getDamageModifier());
                           }
                        }
                     } else if (!onlyHoriz) {
                        Server.getInstance().broadCastAction("The " + this.getName() + " smashes the " + f.getName() + ".", this, 5);
                        if (this.isUnique()) {
                           f.setDamage(f.getDamage() + (float)Server.rand.nextInt(100));
                        } else {
                           if (f.getVillage() != null) {
                              f.getVillage().addTarget(this);
                           }

                           f.setDamage(f.getDamage() + (float)this.getStrengthSkill() / 10.0F * f.getDamageModifier());
                        }
                     }
                  }
               }
            }
         }

         if (this.isUnique() && !this.isOnSurface() && Server.rand.nextInt(500) == 0) {
            boolean breakReinforcement = this.isUnique();
            int tx = Zones.safeTileX(this.getTileX() - 1);
            int ty = Zones.safeTileY(this.getTileY());
            int t = Zones.getTileIntForTile(tx, ty, 0);
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
               } catch (Exception var10) {
                  logger.log(Level.WARNING, var10.getMessage());
               }
            }

            t = Zones.getTileIntForTile(tx, ty, -1);
            if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
               Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
               Players.getInstance().sendChangedTile(tx, ty, false, true);
            }

            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id) {
               Village v = Zones.getVillage(tx, ty, true);
               if (v == null || this.isOnPvPServer() || this.isUnique()) {
                  TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 2, false, null);
                  if (v != null) {
                     v.addTarget(this);
                  }
               }
            }

            tx = Zones.safeTileX(this.getTileX());
            ty = Zones.safeTileY(this.getTileY() - 1);
            t = Zones.getTileIntForTile(tx, ty, -1);
            if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
               Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
               Players.getInstance().sendChangedTile(tx, ty, false, true);
            }

            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id) {
               Village v = Zones.getVillage(tx, ty, true);
               if (v == null || this.isOnPvPServer() || this.isUnique()) {
                  TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 3, false, null);
                  if (v != null) {
                     v.addTarget(this);
                  }
               }
            }

            tx = Zones.safeTileX(this.getTileX() + 1);
            ty = Zones.safeTileY(this.getTileY());
            t = Zones.getTileIntForTile(tx, ty, -1);
            if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
               Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
               Players.getInstance().sendChangedTile(tx, ty, false, true);
            }

            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id) {
               Village v = Zones.getVillage(tx, ty, true);
               if (v == null || this.isOnPvPServer() || this.isUnique()) {
                  TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 4, false, null);
                  if (v != null) {
                     v.addTarget(this);
                  }
               }
            }

            tx = Zones.safeTileX(this.getTileX());
            ty = Zones.safeTileY(this.getTileY() + 1);
            t = Zones.getTileIntForTile(tx, ty, -1);
            if (breakReinforcement && Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
               Server.caveMesh.setTile(tx, ty, Tiles.encode(Tiles.decodeHeight(t), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(t)));
               Players.getInstance().sendChangedTile(tx, ty, false, true);
            }

            if (Tiles.decodeType(t) == Tiles.Tile.TILE_CAVE_WALL.id) {
               Village v = Zones.getVillage(tx, ty, true);
               if (v == null || this.isOnPvPServer() || this.isUnique()) {
                  TileRockBehaviour.createInsideTunnel(tx, ty, t, this, 145 + Server.rand.nextInt(3), 5, false, null);
                  if (v != null) {
                     v.addTarget(this);
                  }
               }
            }
         }
      }
   }

   public int getMaxHuntDistance() {
      return this.isDominated() ? 20 : this.template.getMaxHuntDistance();
   }

   public Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder) throws NoPathException {
      Path path = null;
      PathFinder pf = pathfinder != null ? pathfinder : new PathFinder();
      this.setPathfindcounter(this.getPathfindCounter() + 1);
      if (this.getPathfindCounter() >= 10 && this.target == -10L && this.getPower() <= 0) {
         throw new NoPathException("No pathing now");
      } else {
         if (!this.isSpiritGuard() || this.citizenVillage == null) {
            if (this.isWithinTileDistanceTo(
               targetX, targetY, (int)this.status.getPositionZ() >> 2, Math.max(this.getMaxHuntDistance(), this.template.getVision())
            )) {
               path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 5);
            } else if (this.isUnique() || this.isKingdomGuard() || this.isDominated() || this.template.isTowerBasher()) {
               if (this.target == -10L) {
                  path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 5);
               } else {
                  this.setTargetNOID = true;
               }
            }
         } else if (this.target == -10L) {
            if (this.isWithinTileDistanceTo(targetX, targetY, (int)(this.status.getPositionZ() + this.getAltOffZ()) >> 2, this.getMaxHuntDistance())) {
               path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 10);
            }
         } else {
            try {
               path = pf.findPath(this, this.getTileX(), this.getTileY(), targetX, targetY, this.isOnSurface(), 10);
            } catch (NoPathException var7) {
               if (this.currentVillage == this.citizenVillage) {
                  if (targetX >= this.citizenVillage.getStartX() - 5
                     && targetX <= this.citizenVillage.getEndX() + 5
                     && targetY >= this.citizenVillage.getStartY() - 5
                     && targetY <= this.citizenVillage.getEndY() + 5) {
                     if (this.getTarget() != null) {
                        this.creatureToBlinkTo = this.getTarget();
                        return null;
                     }
                  } else {
                     this.setTargetNOID = true;
                  }
               } else if (this.getTarget() != null) {
                  this.creatureToBlinkTo = this.getTarget();
                  return null;
               }
            }
         }

         if (path != null) {
            this.setPathfindcounter(0);
         }

         return path;
      }
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
      } else {
         return this.template.getTemplateId() == 1 && !this.isPlayer() ? false : this.template.isTrader();
      }
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
      return this.isReborn() ? true : this.template.isAggHuman();
   }

   public boolean isMoveLocal() {
      return this.template.isMoveLocal() && this.status.modtype != 99;
   }

   public boolean isMoveGlobal() {
      boolean shouldMove = false;
      if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()
         && this.getCurrentTile().getVillage() != null
         && (this.isBred() || this.isBranded() || this.isCaredFor())
         && this.target == -10L) {
         shouldMove = true;
      }

      return this.template.isMoveGlobal() || this.status.modtype == 99 || shouldMove;
   }

   public boolean shouldFlee() {
      if (!Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
         if (this.getStatus().isChampion()) {
            return false;
         } else {
            return this.fleeCounter > 0;
         }
      } else if (this.getCurrentTile().getVillage() == null || !this.isBred() && !this.isBranded() && !this.isCaredFor()) {
         if (this.getStatus().isChampion()) {
            return false;
         } else {
            if (this.fleeCounter > 0) {
               Long[] visibleCreatures = this.getVisionArea().getSurface().getCreatures();

               for(Long lCret : visibleCreatures) {
                  try {
                     Creature cret = Server.getInstance().getCreature(lCret);
                     if ((cret.getPower() == 0 || Servers.localServer.testServer)
                        && (cret.isPlayer() || cret.isAggHuman() || cret.isCarnivore() || cret.isMonster() || cret.isHunter())) {
                        float modifier = 1.0F;
                        if (this.getCurrentTile().getVillage() != null && cret.isPlayer()) {
                           modifier = 2.0F;
                        }

                        this.sendToLoggers(
                           "checking if should flee from "
                              + cret.getName()
                              + ": "
                              + (cret.getBaseCombatRating() - Math.abs(cret.getPos2f().distance(this.getPos2f()) / 4.0F))
                              + " vs "
                              + this.getBaseCombatRating() * modifier
                        );
                        if (cret.getBaseCombatRating() - Math.abs(cret.getPos2f().distance(this.getPos2f()) / 2.0F) > this.getBaseCombatRating() * modifier) {
                           return true;
                        }
                     }
                  } catch (NoSuchCreatureException | NoSuchPlayerException var8) {
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
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
      } else {
         return !this.isRidden() && this.hitchedTo == null ? this.template.isDominatable() : false;
      }
   }

   public final int getAggressivity() {
      return this.template.getAggressivity();
   }

   final byte getCombatDamageType() {
      return this.template.getCombatDamageType();
   }

   final float getBreathDamage() {
      if (this.isUndead()) {
         return 10.0F;
      } else {
         return this.isReborn() ? Math.max(3.0F, this.template.getBreathDamage()) : this.template.getBreathDamage();
      }
   }

   public float getHandDamage() {
      if (this.isUndead()) {
         return 5.0F;
      } else {
         return this.isReborn() ? Math.max(3.0F, this.template.getHandDamage()) : this.template.getHandDamage();
      }
   }

   public float getBiteDamage() {
      if (this.isUndead()) {
         return 8.0F;
      } else {
         return this.isReborn() ? Math.max(5.0F, this.template.getBiteDamage()) : this.template.getBiteDamage();
      }
   }

   public float getKickDamage() {
      return this.isReborn() ? Math.max(2.0F, this.template.getKickDamage()) : this.template.getKickDamage();
   }

   public float getHeadButtDamage() {
      return this.isReborn() ? Math.max(4.0F, this.template.getKickDamage()) : this.template.getHeadButtDamage();
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
      } else {
         float mod = this.getStatus().getBattleRatingTypeModifier();
         return (int)Math.max((float)this.template.getMaxGroupAttackSize(), (float)this.template.getMaxGroupAttackSize() * mod);
      }
   }

   public int getGroupSize() {
      int nums = 0;

      for(int x = Math.max(0, this.getCurrentTile().getTileX() - 3); x < Math.min(this.getCurrentTile().getTileX() + 3, Zones.worldTileSizeX - 1); ++x) {
         for(int y = Math.max(0, this.getCurrentTile().getTileY() - 3); y < Math.min(this.getCurrentTile().getTileY() + 3, Zones.worldTileSizeY - 1); ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
            if (t != null && t.getCreatures().length > 0) {
               Creature[] xret = t.getCreatures();

               for(Creature lElement : xret) {
                  if (lElement.getTemplate().getTemplateId() == this.template.getTemplateId()
                     || lElement.getTemplate().getTemplateId() == this.template.getLeaderTemplateId()) {
                     ++nums;
                  }
               }
            }
         }
      }

      return nums;
   }

   public final TilePos getAdjacentTilePos(TilePos pos) {
      switch(Server.rand.nextInt(8)) {
         case 0:
            return pos.East();
         case 1:
            return pos.South();
         case 2:
            return pos.West();
         case 3:
            return pos.North();
         case 4:
            return pos.NorthEast();
         case 5:
            return pos.NorthWest();
         case 6:
            return pos.SouthWest();
         case 7:
            return pos.SouthEast();
         default:
            return pos;
      }
   }

   public void checkEggLaying() {
      if (this.isEggLayer()) {
         if (this.template.getTemplateId() == 53) {
            if (Server.rand.nextInt(7200) == 0) {
               if (WurmCalendar.isAfterEaster()) {
                  this.destroy();
                  Server.getInstance().broadCastAction(this.getNameWithGenus() + " suddenly vanishes down into a hole!", this, 10);
               } else {
                  try {
                     Item egg = ItemFactory.createItem(466, 50.0F, null);
                     egg.putItemInfrontof(this);
                     Server.getInstance().broadCastAction(this.getNameWithGenus() + " throws something in the air!", this, 10);
                  } catch (Exception var4) {
                     logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
                  }
               }
            }
         } else if (this.status.getSex() == 1
            && this.isNeedFood()
            && !this.canEat()
            && (Items.mayLayEggs() || this.isUnique())
            && Server.rand.nextInt(20000 * (this.isUnique() ? 1000 : 1)) == 0
            && this.isOnSurface()) {
            byte type = Tiles.decodeType(Server.surfaceMesh.getTile(this.getCurrentTile().tilex, this.getCurrentTile().tiley));
            if (type == Tiles.Tile.TILE_GRASS.id
               || type == Tiles.Tile.TILE_FIELD.id
               || type == Tiles.Tile.TILE_FIELD2.id
               || type == Tiles.Tile.TILE_DIRT.id
               || type == Tiles.Tile.TILE_DIRT_PACKED.id) {
               int templateId = 464;
               if (this.template.getSize() > 4) {
                  templateId = 465;
               }

               try {
                  Item egg = ItemFactory.createItem(
                     templateId,
                     99.0F,
                     this.getPosX(),
                     this.getPosY(),
                     this.status.getRotation(),
                     this.isOnSurface(),
                     (byte)0,
                     this.getStatus().getBridgeId(),
                     null
                  );
                  if (templateId == 465 || Server.rand.nextInt(5) == 0) {
                     egg.setData1(this.template.getEggTemplateId());
                  }
               } catch (NoSuchTemplateException var5) {
                  logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
               } catch (FailedException var6) {
                  logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
               }

               this.status.hunger = 60000;
            }
         }
      }
   }

   public boolean isNoSkillFor(Creature attacker) {
      if ((this.isKingdomGuard() || this.isSpiritGuard() || this.isZombieSummoned() || this.isPlayer() && attacker.isPlayer() || this.isWarGuard())
         && this.isFriendlyKingdom(attacker.getKingdomId())) {
         return true;
      } else {
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
   }

   public int[] forageForFood(VolaTile currTile) {
      int[] toReturn = new int[]{-1, -1};
      if (this.canEat() && this.isNeedFood()) {
         for(int x = -2; x <= 2; ++x) {
            for(int y = -2; y <= 2; ++y) {
               VolaTile t = Zones.getTileOrNull(Zones.safeTileX(currTile.getTileX() + x), Zones.safeTileY(currTile.getTileY() + y), this.isOnSurface());
               if (t != null) {
                  Item[] its = t.getItems();

                  for(Item lIt : its) {
                     if (lIt.isEdibleBy(this) && Server.rand.nextInt(10) == 0) {
                        this.sendToLoggers("Found " + lIt.getName());
                        toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
                        toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
                        return toReturn;
                     }
                  }
               }

               if (this.isGrazer() && this.canEat() && Server.rand.nextInt(9) == 0) {
                  byte type = Zones.getTextureForTile(Zones.safeTileX(currTile.getTileX() + x), Zones.safeTileY(currTile.getTileY() + y), this.getLayer());
                  if (type == Tiles.Tile.TILE_GRASS.id
                     || type == Tiles.Tile.TILE_FIELD.id
                     || type == Tiles.Tile.TILE_FIELD2.id
                     || type == Tiles.Tile.TILE_STEPPE.id
                     || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id) {
                     this.sendToLoggers("Found grass or field");
                     toReturn[0] = Zones.safeTileX(currTile.getTileX() + x);
                     toReturn[1] = Zones.safeTileY(currTile.getTileY() + y);
                     return toReturn;
                  }
               }
            }
         }
      }

      return toReturn;
   }

   public void blinkTo(int tilex, int tiley, int layer, int floorLevel) {
      this.getCurrentTile().deleteCreatureQuick(this);

      try {
         this.setPositionX((float)((tilex << 2) + 2));
         this.setPositionY((float)((tiley << 2) + 2));
         this.setLayer(Math.min(0, layer), false);
         if (floorLevel > 0) {
            this.pushToFloorLevel(floorLevel);
         } else {
            this.setPositionZ(Zones.calculateHeight(this.getStatus().getPositionX(), this.getStatus().getPositionY(), this.isOnSurface()));
         }

         Zone z = Zones.getZone(tilex, tiley, layer >= 0);
         z.addCreature(this.getWurmId());
      } catch (Exception var6) {
         logger.log(
            Level.WARNING, this.getName() + " - " + tilex + ", " + tiley + ", " + layer + ", " + floorLevel + ": " + var6.getMessage(), (Throwable)var6
         );
      }
   }

   public final boolean isBeachDweller() {
      return this.template.isBeachDweller();
   }

   public final boolean isWoolProducer() {
      return this.template.isWoolProducer();
   }

   public boolean isTargetTileTooHigh(int targetX, int targetY, int currentTileNum, boolean swimming) {
      if (this.getFloorLevel() > 0) {
         return false;
      } else if (this.isFlying()) {
         return false;
      } else {
         short currheight = Tiles.decodeHeight(currentTileNum);
         short[] lSteepness = getTileSteepness(targetX, targetY, this.isOnSurface());
         if (swimming && lSteepness[0] < -200 && currheight > lSteepness[0] && !this.isFloating()) {
            return true;
         } else {
            if (this.isBeachDweller()) {
               if (currheight > 20 && lSteepness[0] > currheight) {
                  return true;
               }

               if (currheight < 0 && lSteepness[0] > 0 && !WurmCalendar.isNight()) {
                  return true;
               }
            }

            if (this.isOnSurface()) {
               VolaTile stile = Zones.getTileOrNull(targetX, targetY, this.isOnSurface());
               if (stile != null && stile.getStructure() != null && stile.getStructure().isTypeBridge()) {
                  if (stile.getStructure().isHorizontal()) {
                     if (stile.getStructure().getMaxX() == stile.getTileX() || stile.getStructure().getMinX() == stile.getTileX()) {
                        return false;
                     }
                  } else if (stile.getStructure().getMaxY() == stile.getTileY() || stile.getStructure().getMinY() == stile.getTileY()) {
                     return false;
                  }
               }
            }

            if (currheight < 500) {
               return false;
            } else if (!swimming && (double)(lSteepness[0] - currheight) > 60.0 * Math.max(1.0, getTileRange(this, targetX, targetY)) && lSteepness[1] > 20) {
               if (Creatures.getInstance().isLog()) {
                  logger.log(
                     Level.INFO,
                     this.getName()
                        + " Skipping moving up since avg steep="
                        + lSteepness[0]
                        + "="
                        + (lSteepness[0] - currheight)
                        + ">"
                        + 60.0 * Math.max(1.0, getTileRange(this, targetX, targetY))
                        + " at "
                        + targetX
                        + ","
                        + targetY
                        + " from "
                        + this.getTileX()
                        + ", "
                        + this.getTileY()
                  );
               }

               return true;
            } else if (!swimming && (double)(currheight - lSteepness[0]) > 60.0 * Math.max(1.0, getTileRange(this, targetX, targetY)) && lSteepness[1] > 20) {
               if (Creatures.getInstance().isLog()) {
                  logger.log(
                     Level.INFO,
                     this.getName()
                        + " Skipping moving down since avg steep="
                        + lSteepness[0]
                        + "="
                        + (lSteepness[0] - currheight)
                        + ">"
                        + 60.0 * Math.max(1.0, getTileRange(this, targetX, targetY))
                        + " at "
                        + targetX
                        + ","
                        + targetY
                        + " from "
                        + this.getTileX()
                        + ", "
                        + this.getTileY()
                  );
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   public final long getBridgeId() {
      return this.getStatus().getPosition() != null ? this.getStatus().getPosition().getBridgeId() : -10L;
   }

   public final boolean isWarGuard() {
      return this.template.isWarGuard();
   }

   public PathTile getMoveTarget(int seed) {
      if (this.getStatus() == null) {
         return null;
      } else {
         long now = System.currentTimeMillis();
         float lPosX = this.status.getPositionX();
         float lPosY = this.status.getPositionY();
         boolean hasTarget = false;
         int tilePosX = (int)lPosX >> 2;
         int tilePosY = (int)lPosY >> 2;
         int tx = tilePosX;
         int ty = tilePosY;

         try {
            if (this.target == -10L || this.fleeCounter > 0 && this.target == -10L) {
               boolean flee = false;
               if (this.isDominated() && this.fleeCounter <= 0) {
                  if (this.hasOrders()) {
                     Order order = this.getFirstOrder();
                     if (order.isTile()) {
                        boolean swimming = false;
                        int ctile = this.isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
                        if (Tiles.decodeHeight(ctile) <= 0) {
                           swimming = true;
                        }

                        int tile = Zones.getTileIntForTile(order.getTileX(), order.getTileY(), this.getLayer());
                        if (!Tiles.isSolidCave(Tiles.decodeType(tile)) && (Tiles.decodeHeight(tile) > 0 || swimming)) {
                           if (this.isOnSurface()) {
                              if (!this.isTargetTileTooHigh(order.getTileX(), order.getTileY(), tile, swimming)) {
                                 hasTarget = true;
                                 tilePosX = order.getTileX();
                                 tilePosY = order.getTileY();
                              }
                           } else {
                              hasTarget = true;
                              tilePosX = order.getTileX();
                              tilePosY = order.getTileY();
                           }
                        }
                     } else if (order.isCreature()) {
                        Creature lTarget = order.getCreature();
                        if (lTarget != null) {
                           if (lTarget.isDead()) {
                              this.removeOrder(order);
                           } else {
                              hasTarget = true;
                              tilePosX = lTarget.getCurrentTile().tilex;
                              tilePosY = lTarget.getCurrentTile().tiley;
                           }
                        }
                     }
                  }
               } else if (this.isTypeFleeing() || this.shouldFlee()) {
                  if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
                     if (this.isOnSurface() && this.getVisionArea() != null && this.getVisionArea().getSurface() != null) {
                        int heatmapSize = this.template.getVision() * 2 + 1;
                        float[][] rangeHeatmap = new float[heatmapSize][heatmapSize];

                        for(int i = 0; i < heatmapSize; ++i) {
                           for(int j = 0; j < heatmapSize; ++j) {
                              rangeHeatmap[i][j] = -100.0F;
                           }
                        }

                        Long[] visibleCreatures = this.getVisionArea().getSurface().getCreatures();

                        for(Long lCret : visibleCreatures) {
                           try {
                              Creature cret = Server.getInstance().getCreature(lCret);
                              float tileModifier = 0.0F;
                              int diffX = (int)(cret.getPosX() - this.getPosX()) >> 2;
                              int diffY = (int)(cret.getPosY() - this.getPosY()) >> 2;

                              for(int i = 0; i < heatmapSize; ++i) {
                                 for(int j = 0; j < heatmapSize; ++j) {
                                    int deltaX = Math.abs(this.template.getVision() + diffX - i);
                                    int deltaY = Math.abs(this.template.getVision() + diffY - j);
                                    if ((cret.getPower() == 0 || Servers.localServer.testServer)
                                       && (cret.isPlayer() || cret.isAggHuman() || cret.isCarnivore() || cret.isMonster() || cret.isHunter())) {
                                       tileModifier = cret.getBaseCombatRating();
                                       if (cret.isBred() || cret.isBranded() || cret.isCaredFor()) {
                                          tileModifier /= 3.0F;
                                       }

                                       if (cret.isDominated()) {
                                          tileModifier /= 3.0F;
                                       }

                                       tileModifier -= (float)Math.max(deltaX, deltaY);
                                    } else {
                                       tileModifier = 1.0F;
                                    }

                                    rangeHeatmap[i][j] += tileModifier;
                                 }
                              }
                           } catch (NoSuchCreatureException | NoSuchPlayerException var42) {
                           }
                        }

                        float currentVal = rangeHeatmap[this.template.getVision()][this.template.getVision()];
                        int currentValCount = 1;
                        int currentTileHeight = Tiles.decodeHeight(
                           Server.surfaceMesh.getTile(Zones.safeTileX(this.getTileX()), Zones.safeTileY(this.getTileY()))
                        );

                        for(int y = 0; y < heatmapSize; ++y) {
                           for(int x = 0; x < heatmapSize; ++x) {
                              int tileHeight = Tiles.decodeHeight(
                                 Server.surfaceMesh
                                    .getTile(
                                       Zones.safeTileX(this.getTileX() + x - this.template.getVision()),
                                       Zones.safeTileY(this.getTileY() + y - this.template.getVision())
                                    )
                              );
                              if (!this.isSubmerged() && tileHeight < 0) {
                                 if (!this.isSwimming()) {
                                    rangeHeatmap[x][y] += (float)(100 + -tileHeight);
                                 } else {
                                    rangeHeatmap[x][y] += (float)(-tileHeight);
                                 }
                              } else if (tileHeight > 0) {
                                 rangeHeatmap[x][y] += (float)(Math.abs(currentTileHeight - tileHeight) / 15);
                              }

                              float testVal = rangeHeatmap[x][y];
                              if (testVal == currentVal) {
                                 ++currentValCount;
                              } else if (testVal < currentVal) {
                                 currentValCount = 1;
                                 currentVal = testVal;
                              }
                           }
                        }

                        for(int y = 0; y < heatmapSize && !flee; ++y) {
                           for(int x = 0; x < heatmapSize && !flee; ++x) {
                              if (currentVal == rangeHeatmap[x][y] && Server.rand.nextInt((int)Math.max(1.0F, (float)currentValCount * 0.75F)) == 0) {
                                 tilePosX = tx + x - this.template.getVision();
                                 tilePosY = ty + y - this.template.getVision();
                                 flee = true;
                              }
                           }
                        }

                        if (!flee) {
                           return null;
                        }
                     }
                  } else if (this.isOnSurface()) {
                     if (Server.rand.nextBoolean()) {
                        if (this.getCurrentTile() != null && this.getCurrentTile().getVillage() != null) {
                           Long[] crets = this.getVisionArea().getSurface().getCreatures();

                           for(Long lCret : crets) {
                              try {
                                 Creature cret = Server.getInstance().getCreature(lCret);
                                 if (cret.getPower() == 0 && (cret.isPlayer() || cret.isAggHuman() || cret.isCarnivore() || cret.isMonster())) {
                                    if (cret.getPosX() > this.getPosX()) {
                                       tilePosX -= Server.rand.nextInt(6);
                                    } else {
                                       tilePosX += Server.rand.nextInt(6);
                                    }

                                    if (cret.getPosY() > this.getPosY()) {
                                       tilePosY -= Server.rand.nextInt(6);
                                    } else {
                                       tilePosY += Server.rand.nextInt(6);
                                    }

                                    flee = true;
                                    break;
                                 }
                              } catch (Exception var43) {
                              }
                           }
                        }
                     } else {
                        for(Player p : Players.getInstance().getPlayers()) {
                           if ((p.getPower() == 0 || Servers.localServer.testServer)
                              && p.getVisionArea() != null
                              && p.getVisionArea().getSurface() != null
                              && p.getVisionArea().getSurface().containsCreature(this)) {
                              if (p.getPosX() > this.getPosX()) {
                                 tilePosX -= Server.rand.nextInt(6);
                              } else {
                                 tilePosX += Server.rand.nextInt(6);
                              }

                              if (p.getPosY() > this.getPosY()) {
                                 tilePosY -= Server.rand.nextInt(6);
                              } else {
                                 tilePosY += Server.rand.nextInt(6);
                              }

                              flee = true;
                              break;
                           }
                        }
                     }
                  }

                  if (this.isSpy()) {
                     int[] empty = new int[]{-1, -1};
                     int[] newarr = this.getSpySpot(empty);
                     if (newarr[0] > 0 && newarr[1] > 0) {
                        flee = true;
                        tilePosX = newarr[0];
                        tilePosY = newarr[1];
                     }
                  }
               }

               if (this.isMoveLocal() && !flee && !hasTarget) {
                  VolaTile currTile = this.getCurrentTile();
                  if (this.isUnique() && Server.rand.nextInt(10) == 0) {
                     Den den = Dens.getDen(this.template.getTemplateId());
                     if (den != null && (den.getTilex() != tx || den.getTiley() != ty)) {
                        tilePosX = den.getTilex();
                        tilePosY = den.getTiley();
                     }
                  } else if (currTile != null) {
                     int rand = Server.rand.nextInt(9);
                     int tpx = currTile.getTileX() + 4 - rand;
                     rand = Server.rand.nextInt(9);
                     int tpy = currTile.getTileY() + 4 - rand;
                     totx += currTile.getTileX() - tpx;
                     toty += currTile.getTileY() - tpy;
                     int[] foodSpot = this.forageForFood(currTile);
                     boolean abort = false;
                     if (Server.rand.nextBoolean()) {
                        if (foodSpot[0] != -1) {
                           tpx = foodSpot[0];
                           tpy = foodSpot[1];
                        } else if (this.template.isTowerBasher() && Servers.localServer.PVPSERVER) {
                           GuardTower closestTower = Kingdoms.getClosestEnemyTower(this.getTileX(), this.getTileY(), true, this);
                           if (closestTower != null) {
                              tilePosX = closestTower.getTower().getTileX();
                              tilePosY = closestTower.getTower().getTileY();
                              abort = true;
                           }
                        } else if (this.isWarGuard()) {
                           tilePosX = Zones.safeTileX(tpx);
                           tilePosY = Zones.safeTileY(tpy);
                           if (!this.isOnSurface()) {
                              int[] tiles = new int[]{tilePosX, tilePosY};
                              if (this.getCurrentTile().isTransition) {
                                 this.setLayer(0, true);
                              } else {
                                 tiles = this.findRandomCaveExit(tiles);
                                 if (tiles[0] != tilePosX && tiles[1] != tilePosY) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    abort = true;
                                 } else {
                                    this.setLayer(0, true);
                                 }
                              }
                           } else {
                              GuardTower gt = this.getGuardTower();
                              if (gt == null) {
                                 gt = Kingdoms.getClosestTower(this.getTileX(), this.getTileY(), true);
                              }

                              boolean towerFound = false;
                              if (gt != null && gt.getKingdom() == this.getKingdomId()) {
                                 towerFound = true;
                              }

                              Item wtarget = Kingdoms.getClosestWarTarget(tx, ty, this);
                              if (wtarget != null
                                 && (
                                    !towerFound
                                       || getTileRange(this, wtarget.getTileX(), wtarget.getTileY())
                                          < getTileRange(this, gt.getTower().getTileX(), gt.getTower().getTileY())
                                 )
                                 && !this.isWithinTileDistanceTo(wtarget.getTileX(), wtarget.getTileY(), wtarget.getFloorLevel(), 15)) {
                                 rand = Server.rand.nextInt(9);
                                 tilePosX = Zones.safeTileX(wtarget.getTileX() + 4 - rand);
                                 rand = Server.rand.nextInt(9);
                                 tilePosY = Zones.safeTileY(wtarget.getTileY() + 4 - rand);
                                 this.setTarget(-10L, true);
                                 this.sendToLoggers("No target. Heading to my camp at " + tilePosX + "," + tilePosY);
                                 abort = true;
                              }

                              if (!abort
                                 && towerFound
                                 && !this.isWithinTileDistanceTo(gt.getTower().getTileX(), gt.getTower().getTileY(), gt.getTower().getFloorLevel(), 15)) {
                                 rand = Server.rand.nextInt(9);
                                 tilePosX = Zones.safeTileX(gt.getTower().getTileX() + 4 - rand);
                                 rand = Server.rand.nextInt(9);
                                 tilePosY = Zones.safeTileY(gt.getTower().getTileY() + 4 - rand);
                                 this.setTarget(-10L, true);
                                 this.sendToLoggers("No target. Heading to my tower at " + tilePosX + "," + tilePosY);
                                 abort = true;
                              }
                           }
                        }
                     }

                     tpx = Zones.safeTileX(tpx);
                     tpy = Zones.safeTileY(tpy);
                     if (!abort) {
                        VolaTile t = Zones.getOrCreateTile(tpx, tpy, this.isOnSurface());
                        VolaTile myt = this.getCurrentTile();
                        if (!t.isGuarded() || myt != null && myt.isGuarded() && !t.hasFire()) {
                           boolean swimming = false;
                           int ctile = this.isOnSurface() ? Server.surfaceMesh.getTile(tx, ty) : Server.caveMesh.getTile(tx, ty);
                           if (Tiles.decodeHeight(ctile) <= 0) {
                              swimming = true;
                           }

                           int tile = Zones.getTileIntForTile(tpx, tpy, this.getLayer());
                           if (!Tiles.isSolidCave(Tiles.decodeType(tile)) && (Tiles.decodeHeight(tile) > 0 || swimming)) {
                              if (!this.isOnSurface()) {
                                 if (t == null || t.getCreatures().length < 3) {
                                    tilePosX = tpx;
                                    tilePosY = tpy;
                                 }
                              } else {
                                 boolean stepOnBridge = false;
                                 if (Server.rand.nextInt(5) == 0) {
                                    for(VolaTile stile : this.currentTile.getThisAndSurroundingTiles(1)) {
                                       if (stile.getStructure() != null && stile.getStructure().isTypeBridge()) {
                                          if (stile.getStructure().isHorizontal()) {
                                             if ((stile.getStructure().getMaxX() == stile.getTileX() || stile.getStructure().getMinX() == stile.getTileX())
                                                && this.getTileY() == stile.getTileY()) {
                                                tilePosX = stile.getTileX();
                                                tilePosY = stile.getTileY();
                                                stepOnBridge = true;
                                                break;
                                             }
                                          } else if ((stile.getStructure().getMaxY() == stile.getTileY() || stile.getStructure().getMinY() == stile.getTileY())
                                             && this.getTileX() == stile.getTileX()) {
                                             tilePosX = stile.getTileX();
                                             tilePosY = stile.getTileY();
                                             stepOnBridge = true;
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 if (!stepOnBridge && !this.isTargetTileTooHigh(tpx, tpy, tile, swimming) && (t == null || t.getCreatures().length < 3)) {
                                    tilePosX = tpx;
                                    tilePosY = tpy;
                                 }
                              }
                           }
                        }
                     }
                  }
               } else if (this.isSpiritGuard() && !hasTarget) {
                  if (this.citizenVillage != null) {
                     int[] tiles = new int[]{tilePosX, tilePosY};
                     if (!this.isOnSurface()) {
                        if (this.getCurrentTile().isTransition) {
                           this.setLayer(0, true);
                        } else {
                           tiles = this.findRandomCaveExit(tiles);
                           tilePosX = tiles[0];
                           tilePosY = tiles[1];
                           if (tilePosX != tx && tilePosY != ty) {
                           }
                        }
                     } else {
                        int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
                        int y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty);
                        VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
                        if (t != null) {
                           if (t.getStructure() == null) {
                              tilePosX = x;
                              tilePosY = y;
                           }
                        } else {
                           tilePosX = x;
                           tilePosY = y;
                        }
                     }
                  } else {
                     VolaTile currTile = this.getCurrentTile();
                     if (currTile != null) {
                        int rand = Server.rand.nextInt(5);
                        int tpx = currTile.getTileX() + 2 - rand;
                        rand = Server.rand.nextInt(5);
                        int tpy = currTile.getTileY() + 2 - rand;
                        VolaTile t = Zones.getTileOrNull(tilePosX, tilePosY, this.isOnSurface());
                        tpx = Zones.safeTileX(tpx);
                        tpy = Zones.safeTileY(tpy);
                        if (t == null) {
                           tilePosX = tpx;
                           tilePosY = tpy;
                        }
                     } else if (!this.isDead()) {
                        currTile = Zones.getOrCreateTile(tilePosX, tilePosY, this.isOnSurface());
                        logger.log(
                           Level.WARNING, this.getName() + " stuck on no tile at " + this.getTileX() + "," + this.getTileY() + "," + this.isOnSurface()
                        );
                     }
                  }
               } else if (this.isKingdomGuard() && !hasTarget) {
                  int[] tiles = new int[]{tilePosX, tilePosY};
                  if (!this.isOnSurface()) {
                     tiles = this.findRandomCaveExit(tiles);
                     tilePosX = tiles[0];
                     tilePosY = tiles[1];
                     if (tilePosX != tx && tilePosY != ty) {
                        hasTarget = true;
                     }
                  }

                  if (!hasTarget && Server.rand.nextInt(40) == 0) {
                     GuardTower gt = Kingdoms.getTower(this);
                     if (gt != null) {
                        int tpx = gt.getTower().getTileX();
                        int tpy = gt.getTower().getTileY();
                        tilePosX = tpx;
                        tilePosY = tpy;
                        hasTarget = true;
                     }
                  }

                  if (!hasTarget) {
                     VolaTile currTile = this.getCurrentTile();
                     int rand = Server.rand.nextInt(5);
                     int tpx = Zones.safeTileX(currTile.getTileX() + 2 - rand);
                     rand = Server.rand.nextInt(5);
                     int tpy = Zones.safeTileY(currTile.getTileY() + 2 - rand);
                     VolaTile t = Zones.getOrCreateTile(tpx, tpy, this.isOnSurface());
                     if ((t.getKingdom() == this.getKingdomId() || currTile.getKingdom() != this.getKingdomId()) && t.getStructure() == null) {
                        tilePosX = tpx;
                        tilePosY = tpy;
                     }
                  }
               }

               if (!this.isCaveDweller()
                  && !this.isOnSurface()
                  && this.getCurrentTile().isTransition
                  && tilePosX == tx
                  && tilePosY == ty
                  && (!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty))) || MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
                  this.setLayer(0, true);
               }
            } else if (this.target != -10L) {
               Creature targ = this.getTarget();
               if (targ != null) {
                  if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                     this.setTarget(-10L, true);
                  }

                  VolaTile currTile = targ.getCurrentTile();
                  if (currTile != null) {
                     tilePosX = currTile.tilex;
                     tilePosY = currTile.tiley;
                     if (seed == 100) {
                        tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                        tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                     }

                     if (this.isSpellCaster() || this.isSummoner()) {
                        tilePosX = Server.rand.nextBoolean()
                           ? currTile.tilex - (Server.rand.nextBoolean() ? 0 : 5)
                           : currTile.tilex + (Server.rand.nextBoolean() ? 0 : 5);
                        tilePosY = Server.rand.nextBoolean()
                           ? currTile.tiley - (Server.rand.nextBoolean() ? 0 : 5)
                           : currTile.tiley + (Server.rand.nextBoolean() ? 0 : 5);
                     }

                     int targGroup = targ.getGroupSize();
                     int myGroup = this.getGroupSize();
                     if (this.isOnSurface() != currTile.isOnSurface()) {
                        boolean changeLayer = false;
                        if (this.getCurrentTile().isTransition) {
                           changeLayer = true;
                        }

                        if (this.isSpiritGuard()) {
                           if (this.currentVillage == this.citizenVillage) {
                              if (this.citizenVillage != null) {
                                 if (currTile.getTileX() >= this.citizenVillage.getStartX() - 5
                                    && currTile.getTileX() <= this.citizenVillage.getEndX() + 5
                                    && currTile.getTileY() >= this.citizenVillage.getStartY() - 5
                                    && currTile.getTileY() <= this.citizenVillage.getEndY() + 5) {
                                    this.blinkTo(tilePosX, tilePosY, targ.getLayer(), targ.getFloorLevel());
                                    return null;
                                 }

                                 if (this.citizenVillage.isOnSurface() == this.isOnSurface()) {
                                    try {
                                       changeLayer = false;
                                       tilePosX = this.citizenVillage.getToken().getTileX();
                                       tilePosY = this.citizenVillage.getToken().getTileY();
                                    } catch (NoSuchItemException var41) {
                                       logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                    }
                                 }

                                 this.setTarget(-10L, true);
                              } else {
                                 this.setTarget(-10L, true);
                              }
                           } else if (this.citizenVillage != null) {
                              if (currTile.getTileX() >= this.citizenVillage.getStartX() - 5
                                 && currTile.getTileX() <= this.citizenVillage.getEndX() + 5
                                 && currTile.getTileY() >= this.citizenVillage.getStartY() - 5
                                 && currTile.getTileY() <= this.citizenVillage.getEndY() + 5) {
                                 this.blinkTo(tilePosX, tilePosY, targ.getLayer(), 0);
                                 return null;
                              }

                              if (this.citizenVillage.isOnSurface() == this.isOnSurface()) {
                                 try {
                                    tilePosX = this.citizenVillage.getToken().getTileX();
                                    tilePosY = this.citizenVillage.getToken().getTileY();
                                    changeLayer = false;
                                 } catch (NoSuchItemException var40) {
                                    logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                 }
                              } else if (!changeLayer) {
                                 int[] tiles = new int[]{tilePosX, tilePosY};
                                 if (this.isOnSurface()) {
                                    tiles = this.findRandomCaveEntrance(tiles);
                                 } else {
                                    tiles = this.findRandomCaveExit(tiles);
                                 }

                                 tilePosX = tiles[0];
                                 tilePosY = tiles[1];
                              }

                              this.setTarget(-10L, true);
                           } else {
                              this.setTarget(-10L, true);
                           }
                        } else if (this.isUnique()) {
                           Den den = Dens.getDen(this.template.getTemplateId());
                           if (den != null) {
                              tilePosX = den.getTilex();
                              tilePosY = den.getTiley();
                              if (!changeLayer) {
                                 int[] tiles = new int[]{tilePosX, tilePosY};
                                 if (!this.isOnSurface()) {
                                    tiles = this.findRandomCaveExit(tiles);
                                 }

                                 tilePosX = tiles[0];
                                 tilePosY = tiles[1];
                              }

                              this.setTarget(-10L, true);
                           } else if (!this.isOnSurface() && !changeLayer) {
                              int[] tiles = new int[]{tilePosX, tilePosY};
                              tiles = this.findRandomCaveExit(tiles);
                              tilePosX = tiles[0];
                              tilePosY = tiles[1];
                           }
                        } else if (this.isKingdomGuard()) {
                           if (this.getCurrentKingdom() == this.getKingdomId()) {
                              if (this.isWithinTileDistanceTo(
                                 currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                              )) {
                                 if (!changeLayer) {
                                    int[] tiles = new int[]{tilePosX, tilePosY};
                                    if (this.isOnSurface()) {
                                       tiles = this.findRandomCaveEntrance(tiles);
                                    } else {
                                       tiles = this.findRandomCaveExit(tiles);
                                    }

                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                 }
                              } else {
                                 this.setTarget(-10L, true);
                              }
                           } else {
                              changeLayer = false;
                              this.setTarget(-10L, true);
                           }
                        } else if (this.getSize() > 3) {
                           changeLayer = false;
                           this.setTarget(-10L, true);
                        } else {
                           VolaTile t = this.getCurrentTile();
                           if ((this.isAggHuman() || this.isHunter() || this.isDominated())
                              && (!currTile.isGuarded() || t != null && t.isGuarded())
                              && this.isWithinTileDistanceTo(
                                 currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                              )) {
                              if (!changeLayer) {
                                 int[] tiles = new int[]{tilePosX, tilePosY};
                                 if (this.isOnSurface()) {
                                    tiles = this.findRandomCaveEntrance(tiles);
                                 } else {
                                    tiles = this.findRandomCaveExit(tiles);
                                 }

                                 tilePosX = tiles[0];
                                 tilePosY = tiles[1];
                              }
                           } else {
                              this.setTarget(-10L, true);
                           }
                        }

                        if (changeLayer
                           && (
                              !Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)))
                                 || MineDoorPermission.getPermission(tx, ty).mayPass(this)
                           )) {
                           this.setLayer(this.isOnSurface() ? -1 : 0, true);
                        }
                     } else if (this.isSpiritGuard()) {
                        if (this.currentVillage == this.citizenVillage) {
                           if (this.citizenVillage != null) {
                              tilePosX = currTile.getTileX();
                              tilePosY = currTile.getTileY();
                              if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                                 tilePosX = this.citizenVillage.getStartX() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterX() + 10);
                                 tilePosY = this.citizenVillage.getStartY() - 5 + Server.rand.nextInt(this.citizenVillage.getDiameterY() + 10);
                              } else if (currTile.getTileX() >= this.citizenVillage.getStartX() - 5
                                 && currTile.getTileX() <= this.citizenVillage.getEndX() + 5
                                 && currTile.getTileY() >= this.citizenVillage.getStartY() - 5
                                 && currTile.getTileY() <= this.citizenVillage.getEndY() + 5) {
                                 this.citizenVillage.cryForHelp(this, false);
                              } else {
                                 try {
                                    tilePosX = this.citizenVillage.getToken().getTileX();
                                    tilePosY = this.citizenVillage.getToken().getTileY();
                                 } catch (NoSuchItemException var39) {
                                    logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                                 }

                                 this.setTarget(-10L, true);
                              }
                           } else if (this.isWithinTileDistanceTo(
                              currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                           )) {
                              logger.log(
                                 Level.WARNING,
                                 "Why does this happen to a " + this.getName() + " at " + this.getCurrentTile().tilex + ", " + this.getCurrentTile().tiley
                              );
                              tilePosX = currTile.getTileX();
                              tilePosY = currTile.getTileY();
                           } else {
                              this.setTarget(-10L, true);
                           }
                        } else if (this.citizenVillage != null) {
                           tilePosX = currTile.getTileX();
                           tilePosY = currTile.getTileY();
                           if (currTile.getTileX() >= this.citizenVillage.getStartX() - 5
                              && currTile.getTileX() <= this.citizenVillage.getEndX() + 5
                              && currTile.getTileY() >= this.citizenVillage.getStartY() - 5
                              && currTile.getTileY() <= this.citizenVillage.getEndY() + 5) {
                              this.citizenVillage.cryForHelp(this, true);
                           } else {
                              try {
                                 tilePosX = this.citizenVillage.getToken().getTileX();
                                 tilePosY = this.citizenVillage.getToken().getTileY();
                              } catch (NoSuchItemException var38) {
                                 logger.log(Level.WARNING, this.getName() + " no token for village " + this.citizenVillage);
                              }

                              this.setTarget(-10L, true);
                           }
                        } else {
                           if (this.isWithinTileDistanceTo(
                              currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                           )) {
                              if (Server.rand.nextInt(100) != 0) {
                                 tilePosX = currTile.getTileX();
                                 tilePosY = currTile.getTileY();
                              } else {
                                 this.setTarget(-10L, true);
                              }
                           } else {
                              this.setTarget(-10L, true);
                           }

                           logger.log(Level.WARNING, this.getName() + " no citizen village.");
                        }
                     } else if (this.isUnique()) {
                        Den den = Dens.getDen(this.template.getTemplateId());
                        if (den != null) {
                           if (Math.abs(currTile.getTileX() - den.getTilex()) > this.template.getVision()
                              || Math.abs(currTile.getTileY() - den.getTiley()) > this.template.getVision()) {
                              if (Server.rand.nextInt(10) == 0) {
                                 if (!this.isFighting()) {
                                    this.setTarget(-10L, true);
                                    tilePosX = den.getTilex();
                                    tilePosY = den.getTiley();
                                 }
                              } else if (this.isWithinTileDistanceTo(
                                 currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                              )) {
                                 tilePosX = currTile.getTileX();
                                 tilePosY = currTile.getTileY();
                                 if (this.getSize() < 5 && targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                    int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                    if (tiles[0] > 0) {
                                       tilePosX = tiles[0];
                                       tilePosY = tiles[1];
                                       if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                          tilePosX = currTile.tilex;
                                          tilePosY = currTile.tiley;
                                       }
                                    }
                                 } else if (this.getBridgeId() != targ.getBridgeId()) {
                                    int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId());
                                    if (tiles[0] > 0) {
                                       tilePosX = tiles[0];
                                       tilePosY = tiles[1];
                                       if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                          tilePosX = currTile.tilex;
                                          tilePosY = currTile.tiley;
                                       }
                                    }
                                 }

                                 if (seed == 100) {
                                    tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                    tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                 }
                              } else if (!this.isFighting()) {
                                 this.setTarget(-10L, true);
                              }
                           }
                        } else if (this.isWithinTileDistanceTo(
                           currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                        )) {
                           if (seed == 100) {
                              tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                              tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                           } else {
                              tilePosX = currTile.getTileX();
                              tilePosY = currTile.getTileY();
                              if (this.getSize() < 5 && targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                 int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                 if (tiles[0] > 0) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                       tilePosX = currTile.tilex;
                                       tilePosY = currTile.tiley;
                                    }
                                 }
                              } else if (this.getBridgeId() != targ.getBridgeId()) {
                                 int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId());
                                 if (tiles[0] > 0) {
                                    tilePosX = tiles[0];
                                    tilePosY = tiles[1];
                                    if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                       tilePosX = currTile.tilex;
                                       tilePosY = currTile.tiley;
                                    }
                                 }
                              }
                           }
                        } else if (!this.isFighting()) {
                           this.setTarget(-10L, true);
                        }
                     } else if (this.isKingdomGuard()) {
                        if (this.getCurrentKingdom() == this.getKingdomId()) {
                           if (this.isWithinTileDistanceTo(
                              currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                           )) {
                              GuardTower gt = Kingdoms.getTower(this);
                              if (gt != null) {
                                 int tpx = gt.getTower().getTileX();
                                 int tpy = gt.getTower().getTileY();
                                 if (targGroup >= myGroup * this.getMaxGroupAttackSize()
                                    || !targ.isWithinTileDistanceTo(tpx, tpy, (int)gt.getTower().getPosZ(), 50)) {
                                    tilePosX = tpx;
                                    tilePosY = tpy;
                                    this.setTarget(-10L, true);
                                 } else if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                                    if (Server.rand.nextBoolean()) {
                                       tilePosX = Math.max(currTile.getTileX() + 10, this.getTileX());
                                    } else {
                                       tilePosX = Math.min(currTile.getTileX() - 10, this.getTileX());
                                    }

                                    if (Server.rand.nextBoolean()) {
                                       tilePosX = Math.max(currTile.getTileY() + 10, this.getTileY());
                                    } else {
                                       tilePosX = Math.min(currTile.getTileY() - 10, this.getTileY());
                                    }
                                 } else if (seed == 100) {
                                    tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                    tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                 } else {
                                    tilePosX = currTile.getTileX();
                                    tilePosY = currTile.getTileY();
                                    if (targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                       int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                       if (tiles[0] > 0) {
                                          tilePosX = tiles[0];
                                          tilePosY = tiles[1];
                                          if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                             tilePosX = currTile.tilex;
                                             tilePosY = currTile.tiley;
                                          }
                                       }
                                    } else if (this.getBridgeId() != targ.getBridgeId()) {
                                       int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId());
                                       if (tiles[0] > 0) {
                                          tilePosX = tiles[0];
                                          tilePosY = tiles[1];
                                          if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                             tilePosX = currTile.tilex;
                                             tilePosY = currTile.tiley;
                                          }
                                       }
                                    }
                                 }
                              } else if (seed == 100) {
                                 tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                 tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                              } else {
                                 tilePosX = currTile.getTileX();
                                 tilePosY = currTile.getTileY();
                              }
                           } else {
                              this.setTarget(-10L, true);
                           }
                        } else {
                           this.setTarget(-10L, true);
                        }
                     } else if (targ.getCultist() != null && targ.getCultist().hasFearEffect()) {
                        if (Server.rand.nextBoolean()) {
                           tilePosX = Math.max(currTile.getTileX() + 10, this.getTileX());
                        } else {
                           tilePosX = Math.min(currTile.getTileX() - 10, this.getTileX());
                        }

                        if (Server.rand.nextBoolean()) {
                           tilePosX = Math.max(currTile.getTileY() + 10, this.getTileY());
                        } else {
                           tilePosX = Math.min(currTile.getTileY() - 10, this.getTileY());
                        }
                     } else {
                        boolean abort = false;
                        boolean towerFound = false;
                        if (this.isWarGuard()) {
                           GuardTower gt = Kingdoms.getClosestTower(this.getTileX(), this.getTileY(), true);
                           if (gt != null && gt.getKingdom() == this.getKingdomId()) {
                              towerFound = true;
                           }

                           Item wtarget = Kingdoms.getClosestWarTarget(tx, ty, this);
                           if (wtarget != null
                              && (
                                 !towerFound
                                    || getTileRange(this, wtarget.getTileX(), wtarget.getTileY())
                                       < getTileRange(this, gt.getTower().getTileX(), gt.getTower().getTileY())
                              )
                              && !this.isWithinTileDistanceTo(wtarget.getTileX(), wtarget.getTileY(), wtarget.getFloorLevel(), 15)) {
                              int rand = Server.rand.nextInt(9);
                              tilePosX = Zones.safeTileX(wtarget.getTileX() + 4 - rand);
                              rand = Server.rand.nextInt(9);
                              tilePosY = Zones.safeTileY(wtarget.getTileY() + 4 - rand);
                              abort = true;
                              this.setTarget(-10L, true);
                              this.sendToLoggers("Heading to my camp at " + tilePosX + "," + tilePosY);
                           }

                           if (!abort
                              && towerFound
                              && !this.isWithinTileDistanceTo(gt.getTower().getTileX(), gt.getTower().getTileY(), gt.getTower().getFloorLevel(), 15)) {
                              int rand = Server.rand.nextInt(9);
                              tilePosX = Zones.safeTileX(gt.getTower().getTileX() + 4 - rand);
                              rand = Server.rand.nextInt(9);
                              tilePosY = Zones.safeTileY(gt.getTower().getTileY() + 4 - rand);
                              abort = true;
                              this.setTarget(-10L, true);
                              this.sendToLoggers("Heading to my tower at " + tilePosX + "," + tilePosY);
                           }
                        }

                        if (!abort) {
                           VolaTile t = this.getCurrentTile();
                           if (targGroup <= myGroup * this.getMaxGroupAttackSize()
                              && (this.isAggHuman() || this.isHunter())
                              && (!currTile.isGuarded() || t != null && t.isGuarded())) {
                              if (this.isWithinTileDistanceTo(
                                 currTile.getTileX(), currTile.getTileY(), (int)targ.getPositionZ(), this.template.getMaxHuntDistance()
                              )) {
                                 if (targ.getKingdomId() == 0
                                    || this.isFriendlyKingdom(targ.getKingdomId())
                                    || !this.isDefendKingdom() && (!this.isAggWhitie() || targ.getKingdomTemplateId() == 3)) {
                                    if (this.isSubmerged()) {
                                       try {
                                          float z = Zones.calculateHeight(targ.getPosX(), targ.getPosY(), targ.isOnSurface());
                                          if (z < -5.0F) {
                                             if (seed == 100) {
                                                tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                                tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                             } else {
                                                tilePosX = currTile.getTileX();
                                                tilePosY = currTile.getTileY();
                                             }
                                          } else {
                                             int[] tiles = new int[]{tilePosX, tilePosY};
                                             if (this.isOnSurface()) {
                                                tiles = this.findRandomDeepSpot(tiles);
                                             }

                                             tilePosX = tiles[0];
                                             tilePosY = tiles[1];
                                             this.setTarget(-10L, true);
                                          }
                                       } catch (NoSuchZoneException var37) {
                                          this.setTarget(-10L, true);
                                       }
                                    } else if (seed == 100) {
                                       tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                       tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                    } else {
                                       tilePosX = currTile.getTileX();
                                       tilePosY = currTile.getTileY();
                                       if (this.getSize() < 5 && targ.getBridgeId() != -10L && this.getBridgeId() < 0L) {
                                          int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), targ.getBridgeId());
                                          if (tiles[0] > 0) {
                                             tilePosX = tiles[0];
                                             tilePosY = tiles[1];
                                             if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                tilePosX = currTile.tilex;
                                                tilePosY = currTile.tiley;
                                             }
                                          }
                                       } else if (this.getBridgeId() != targ.getBridgeId()) {
                                          int[] tiles = this.findBestBridgeEntrance(targ.getTileX(), targ.getTileY(), targ.getLayer(), this.getBridgeId());
                                          if (tiles[0] > 0) {
                                             tilePosX = tiles[0];
                                             tilePosY = tiles[1];
                                             if (this.getTileX() == tilePosX && this.getTileY() == tilePosY) {
                                                tilePosX = currTile.tilex;
                                                tilePosY = currTile.tiley;
                                             }
                                          }
                                       }
                                    }
                                 } else if (!this.isFighting()) {
                                    if (seed == 100) {
                                       tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                                       tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                                    } else {
                                       tilePosX = currTile.getTileX();
                                       tilePosY = currTile.getTileY();
                                       this.setTarget(targ.getWurmId(), false);
                                    }
                                 }
                              } else if (!this.isFighting()) {
                                 this.setTarget(-10L, true);
                              }
                           } else if (!this.isFighting()) {
                              this.setTarget(-10L, true);
                           }
                        }
                     }
                  } else {
                     this.setTarget(-10L, true);
                  }
               } else {
                  this.setTarget(-10L, true);
               }
            }

            if (tilePosX == tx && tilePosY == ty) {
               return null;
            } else {
               tilePosX = Zones.safeTileX(tilePosX);
               tilePosY = Zones.safeTileY(tilePosY);
               if (!this.isOnSurface()) {
                  int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
                  if (!Tiles.isSolidCave(Tiles.decodeType(tile))
                     && (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged())) {
                     return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                  }
               } else {
                  int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
                  if (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged()) {
                     return new PathTile(tilePosX, tilePosY, tile, this.isOnSurface(), this.getFloorLevel());
                  }
               }

               this.setTarget(-10L, true);
               if (this.isDominated() && this.hasOrders()) {
                  this.removeOrder(this.getFirstOrder());
               }

               return null;
            }
         } catch (ArrayIndexOutOfBoundsException var44) {
            logger.log(Level.WARNING, this.getName() + " " + tilePosX + ", " + tilePosY + var44.getMessage(), (Throwable)var44);
            return null;
         } finally {
            ;
         }
      }
   }

   public final boolean isBridgeBlockingAttack(Creature attacker, boolean justChecking) {
      if (!this.isInvulnerable() && !attacker.isInvulnerable()) {
         if (this.getPositionZ() + this.getAltOffZ() < 0.0F && attacker.getBridgeId() > 0L) {
            return true;
         } else if (attacker.getPositionZ() + this.getAltOffZ() < 0.0F && this.getBridgeId() > 0L) {
            return true;
         } else {
            return !justChecking
               && this.getFloorLevel() != attacker.getFloorLevel()
               && this.getBridgeId() != attacker.getBridgeId()
               && this.getSize() < 5
               && attacker.getSize() < 5;
         }
      } else {
         return true;
      }
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
         int[] foodSpot = this.forageForFood(currTile);
         if (foodSpot[0] != -1) {
            tilePosX = foodSpot[0];
            tilePosY = foodSpot[1];
         } else if (this.template.isTowerBasher() && Servers.localServer.PVPSERVER) {
            GuardTower closestTower = Kingdoms.getClosestEnemyTower(this.getTileX(), this.getTileY(), true, this);
            if (closestTower != null) {
               tilePosX = closestTower.getTower().getTileX();
               tilePosY = closestTower.getTower().getTileY();
            }
         }
      }

      if (tilePosX == tx && tilePosY == ty) {
         return null;
      } else {
         tilePosX = Zones.safeTileX(tilePosX);
         tilePosY = Zones.safeTileY(tilePosY);
         if (!this.isOnSurface()) {
            int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
            if (!Tiles.isSolidCave(Tiles.decodeType(tile))
               && (Tiles.decodeHeight(tile) > -this.getHalfHeightDecimeters() || this.isSwimming() || this.isSubmerged())) {
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
         startx = Math.max(0, this.citizenVillage.getStartX() - 5);
         endx = Math.min(Zones.worldTileSizeX - 1, this.citizenVillage.getEndX() + 5);
         starty = Math.max(0, this.citizenVillage.getStartY() - 5);
         endy = Math.min(Zones.worldTileSizeY - 1, this.citizenVillage.getEndY() + 5);
         int x = this.citizenVillage.startx + Server.rand.nextInt(this.citizenVillage.endx - this.citizenVillage.startx);
         int y = this.citizenVillage.starty + Server.rand.nextInt(this.citizenVillage.endy - this.citizenVillage.starty);
         if (!Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y)))) {
            tiles[0] = x;
            tiles[1] = y;
            this.setPathfindcounter(0);
         }
      }

      int rand = Server.rand.nextInt(endx - startx);
      startx += rand;
      rand = Server.rand.nextInt(endy - starty);
      starty += rand;

      for(int x = startx; x < endx; ++x) {
         for(int y = starty; y < endy; ++y) {
            if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
               tiles[0] = x;
               tiles[1] = y;
               this.setPathfindcounter(0);
               return tiles;
            }
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

      for(int x = startx; x < Math.min(endx, startx + 10); ++x) {
         for(int y = starty; y < Math.min(endy, starty + 10); ++y) {
            if ((float)Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) < -50.0F) {
               tiles[0] = x;
               tiles[1] = y;
               return tiles;
            }
         }
      }

      return tiles;
   }

   public final boolean isSpyTarget(Creature c) {
      if (!c.isDead() && c.getPower() <= 0) {
         if (this.getTemplate().getTemplateId() == 84 && c.getKingdomId() != 3) {
            return true;
         } else if (this.getTemplate().getTemplateId() == 12 && c.getKingdomId() != 1) {
            return true;
         } else {
            return this.getTemplate().getTemplateId() == 10 && c.getKingdomId() != 2;
         }
      } else {
         return false;
      }
   }

   public final boolean isSpyFriend(Creature c) {
      if (!c.isAggHuman() && c.getCitizenVillage() != null) {
         if (this.getTemplate().getTemplateId() == 84 && c.getKingdomId() == 3) {
            return true;
         } else if (this.getTemplate().getTemplateId() == 12 && c.getKingdomId() == 1) {
            return true;
         } else {
            return this.getTemplate().getTemplateId() == 10 && c.getKingdomId() == 2;
         }
      } else {
         return false;
      }
   }

   public final boolean isWithinSpyDist(Creature c) {
      return c != null && c.isWithinTileDistanceTo(this.getTileX(), this.getTileY(), 100, 40);
   }

   public int[] getSpySpot(int[] suggested) {
      if (this.isSpy()) {
         Creature linkedToc = this.getCreatureLinkedTo();
         if (linkedToc == null || !linkedToc.isDead() || !this.isWithinSpyDist(linkedToc)) {
            this.linkedTo = -10L;

            for(Player player : Players.getInstance().getPlayers()) {
               if (this.isSpyTarget(player) && !player.isDead() && this.isWithinSpyDist(player)) {
                  linkedToc = player;
                  this.setLinkedTo(player.getWurmId(), false);
                  break;
               }
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

      for(int x = startx; x < Math.min(endx, startx + 10); ++x) {
         for(int y = starty; y < Math.min(endy, starty + 10); ++y) {
            if (Tiles.decodeType(Server.surfaceMesh.getTile(x, y)) == Tiles.Tile.TILE_HOLE.id
               || passMineDoors && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(x, y)))) {
               tiles[0] = x;
               tiles[1] = y;
               return tiles;
            }
         }
      }

      return tiles;
   }

   public int[] findBestBridgeEntrance(int tilex, int tiley, int layer, long bridgeId) {
      VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
      return t != null && t.getStructure() != null && t.getStructure().getWurmId() == bridgeId
         ? t.getStructure().findBestBridgeEntrance(this, tilex, tiley, layer, bridgeId, this.pathfindcounter)
         : Structure.noEntrance;
   }

   public void setAbilityTitle(int newTitle) {
   }

   public int getAbilityTitleVal() {
      return this.template.abilityTitle;
   }

   public String getAbilityTitle() {
      return this.template.abilityTitle > -1 ? Abilities.getAbilityString(this.template.abilityTitle) + " " : "";
   }

   public boolean isLogged() {
      return false;
   }

   public float getFaith() {
      return this.template.getFaith();
   }

   public Skill getChannelingSkill() {
      Skill channeling = null;

      try {
         channeling = this.skills.getSkill(10067);
      } catch (NoSuchSkillException var3) {
         if (this.getFaith() >= 10.0F) {
            channeling = this.skills.learn(10067, 1.0F);
         }
      }

      return channeling;
   }

   public Skill getMindLogical() {
      Skill toReturn = null;

      try {
         toReturn = this.getSkills().getSkill(100);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(100, 1.0F);
      }

      return toReturn;
   }

   public Skill getMindSpeed() {
      Skill toReturn = null;

      try {
         toReturn = this.getSkills().getSkill(101);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(101, 1.0F);
      }

      return toReturn;
   }

   public Skill getSoulDepth() {
      Skill toReturn = null;

      try {
         toReturn = this.getSkills().getSkill(106);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(106, 1.0F);
      }

      return toReturn;
   }

   public Skill getBreedingSkill() {
      Skill toReturn;
      try {
         toReturn = this.getSkills().getSkill(10085);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(10085, 1.0F);
      }

      return toReturn;
   }

   public Skill getSoulStrength() {
      Skill toReturn = null;

      try {
         toReturn = this.getSkills().getSkill(105);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(105, 1.0F);
      }

      return toReturn;
   }

   public Skill getBodyStrength() {
      Skill toReturn = null;

      try {
         toReturn = this.getSkills().getSkill(102);
      } catch (NoSuchSkillException var3) {
         toReturn = this.getSkills().learn(102, 1.0F);
      }

      return toReturn;
   }

   public Deity getDeity() {
      return this.template.getDeity();
   }

   public void modifyFaith(float modifier) {
   }

   public boolean isActionFaithful(Action action) {
      return this.getDeity() != null && this.faithful ? this.getDeity().isActionFaithful(action) : true;
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
      return !this.isSpellCaster() && !this.isSummoner() ? this.template.getFaith() : this.creatureFavor;
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
      return this.citizenVillage != null && creature.citizenVillage != null ? this.citizenVillage.isEnemy(creature.citizenVillage) : false;
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
      if (!Servers.isThisAPvpServer()) {
         Village bVill = this.getBrandVillage();
         if (bVill != null) {
            return bVill.kingdom;
         }
      }

      return this.status.kingdom;
   }

   public byte getKingdomTemplateId() {
      Kingdom k = Kingdoms.getKingdom(this.getKingdomId());
      return k != null ? k.getTemplate() : 0;
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
         if (this.isKing()) {
            this.getCommunicator().sendNormalServerMessage("You are the king, and may not change kingdom!");
            return false;
         }

         Village v = this.getCitizenVillage();
         if (!forced && v != null && v.getMayor().getId() == this.getWurmId()) {
            try {
               this.getCommunicator().sendNormalServerMessage("You are the mayor of " + v.getName() + ", and may not change kingdom!");
               return false;
            } catch (Exception var8) {
               return false;
            }
         }

         if (Kingdoms.getKingdomTemplateFor(this.getKingdomId()) == 3 && Kingdoms.getKingdomTemplateFor(kingdom) != 3) {
            if (this.getDeity() != null && this.getDeity().number == 4) {
               this.setDeity(null);
               this.setFaith(0.0F);
               this.setAlignment(Math.max(1.0F, this.getAlignment()));
            }
         } else if (Kingdoms.getKingdomTemplateFor(kingdom) == 3
            && Kingdoms.getKingdomTemplateFor(this.getKingdomId()) != 3
            && (this.getDeity() == null || this.getDeity().number == 1 || this.getDeity().number == 2 || this.getDeity().number == 3)) {
            this.setDeity(Deities.getDeity(4));
            this.setAlignment(Math.min(this.getAlignment(), -50.0F));
            this.setFaith(1.0F);
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
               KingdomIp kip = KingdomIp.getKIP(this.getCommunicator().getConnection().getIp(), this.getKingdomId());
               if (kip != null) {
                  kip.logon(kingdom);
               }
            } catch (Exception var10) {
               logger.log(Level.INFO, this.getName() + " " + var10.getMessage());
            }
         }

         this.status.setKingdom(kingdom);
         if (this.isPlayer()) {
            if (Servers.localServer.isChallengeOrEpicServer() || Servers.isThisAChaosServer() || Servers.localServer.PVPSERVER) {
               if (this.getCommunicator().getConnection() != null) {
                  try {
                     if (this.getCommunicator().getConnection().getIp() != null) {
                        KingdomIp kip = KingdomIp.getKIP(this.getCommunicator().getConnection().getIp());
                        if (kip != null) {
                           kip.setKingdom(kingdom);
                        }
                     }
                  } catch (NullPointerException var9) {
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
      if ((this.isWarGuard() || this.isKingdomGuard())
         && this.guardTower != null
         && this.guardTower.getKingdom() == this.getKingdomId()
         && System.currentTimeMillis() - this.guardTower.getLastSentWarning() < 180000L) {
         overrideRandomChance = true;
      }

      if ((overrideRandomChance || Server.rand.nextInt(!this.isKingdomGuard() && !this.isWarGuard() ? 100 : 20) == 0) && this.getVisionArea() != null) {
         try {
            if (this.isOnSurface()) {
               this.getVisionArea().getSurface().checkForEnemies();
            } else {
               this.getVisionArea().getUnderGround().checkForEnemies();
            }
         } catch (Exception var3) {
            logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         }
      }
   }

   public boolean sendTransfer(
      Server senderServer,
      String targetIp,
      int targetPort,
      String serverpass,
      int targetServerId,
      int tilex,
      int tiley,
      boolean surfaced,
      boolean toOrFromEpic,
      byte targetKingdomId
   ) {
      logger.log(Level.WARNING, "Sendtransfer called in creature", (Throwable)(new Exception()));
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
      if (this.getCultist() == null || !this.getCultist().isNoDecay()) {
         try {
            Skill bodyStr = this.skills.getSkill(102);
            bodyStr.setKnowledge(bodyStr.getKnowledge() - 0.01F, false);
            Skill body = this.skills.getSkill(1);
            body.setKnowledge(body.getKnowledge() - 0.01F, false);
         } catch (NoSuchSkillException var10) {
            this.skills.learn(102, 1.0F);
            logger.log(Level.WARNING, this.getName() + " learnt body strength.");
         }

         if (!pvp) {
            Skill[] sk = this.skills.getSkills();
            int nums = 0;

            for(Skill lElement : sk) {
               if ((lElement.getType() == 4 || lElement.getType() == 2)
                  && lElement.getNumber() != 1023
                  && Server.rand.nextInt(10) == 0
                  && lElement.getKnowledge(0.0) > 2.0
                  && lElement.getKnowledge(0.0) < 99.0) {
                  lElement.setKnowledge(Math.max(1.0, lElement.getKnowledge() - aMod), false);
                  if (++nums > 4) {
                     break;
                  }
               }
            }
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
      } else if (this.hasCustomKingdom()) {
         return true;
      } else if (this.status.hasCustomColor()) {
         return true;
      } else {
         return this.template.getColorRed() != 255 || this.template.getColorGreen() != 255 || this.template.getColorBlue() != 255;
      }
   }

   public boolean hasCustomKingdom() {
      return this.getKingdomId() > 4 || this.getKingdomId() < 0;
   }

   public byte getColorRed() {
      return this.status.hasCustomColor() ? this.status.getColorRed() : (byte)this.template.getColorRed();
   }

   public byte getColorGreen() {
      return this.status.hasCustomColor() ? this.status.getColorGreen() : (byte)this.template.getColorGreen();
   }

   public byte getColorBlue() {
      return this.status.hasCustomColor() ? this.status.getColorBlue() : (byte)this.template.getColorBlue();
   }

   public boolean hasCustomSize() {
      if (this.status.getSizeMod() != 1.0F) {
         return true;
      } else {
         return this.template.getSizeModX() != 64 || this.template.getSizeModY() != 64 || this.template.getSizeModZ() != 64;
      }
   }

   public byte getSizeModX() {
      return (byte)((int)Math.min(255.0F, (float)this.template.getSizeModX() * this.status.getSizeMod()));
   }

   public byte getSizeModY() {
      return (byte)((int)Math.min(255.0F, (float)this.template.getSizeModY() * this.status.getSizeMod()));
   }

   public byte getSizeModZ() {
      return (byte)((int)Math.min(255.0F, (float)this.template.getSizeModZ() * this.status.getSizeMod()));
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

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            if (tilex + x < Zones.worldTileSizeX && tiley + y < Zones.worldTileSizeY) {
               short height = 0;
               if (surfaced) {
                  height = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
               } else {
                  height = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
               }

               if (height > highest) {
                  highest = height;
               }

               if (height < lowest) {
                  lowest = height;
               }
            }
         }
      }

      int med = (highest + lowest) / 2;
      return new short[]{(short)med, (short)(highest - lowest)};
   }

   public short[] getLowestTileCorner(short tilex, short tiley) {
      short lowestX = tilex;
      short lowestY = tiley;
      short lowest = 32000;

      for(int x = 0; x <= 1; ++x) {
         for(int y = 0; y <= 1; ++y) {
            if (tilex + x < Zones.worldTileSizeX && tiley + y < Zones.worldTileSizeY) {
               short height = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
               if (height < lowest) {
                  lowest = height;
                  lowestX = (short)(tilex + x);
                  lowestY = (short)(tiley + y);
               }
            }
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
      if (this.getKingdomTemplateId() == 3) {
         return 50.0F + ItemBonus.getDetectionBonus(this);
      } else {
         SpellEffects eff = this.getSpellEffects();
         if (eff != null) {
            SpellEffect effbon = eff.getSpellEffect((byte)21);
            if (effbon != null) {
               return effbon.power + ItemBonus.getDetectionBonus(this);
            }
         }

         return ItemBonus.getDetectionBonus(this);
      }
   }

   public float getBonusForSpellEffect(byte enchantment) {
      SpellEffects eff = this.getSpellEffects();
      if (eff != null) {
         SpellEffect skillgain = eff.getSpellEffect(enchantment);
         if (skillgain != null) {
            return skillgain.power;
         }
      }

      return 0.0F;
   }

   public float getNoLocateItemBonus(boolean reducePower) {
      Item[] bodyItems = this.getBody().getContainersAndWornItems();
      float maxBonus = 0.0F;
      Item maxItem = null;

      for(int x = 0; x < bodyItems.length; ++x) {
         if ((bodyItems[x].isEnchantableJewelry() || bodyItems[x].isArtifact()) && bodyItems[x].getNolocateBonus() > maxBonus) {
            maxBonus = bodyItems[x].getNolocateBonus();
            maxItem = bodyItems[x];
         }
      }

      if (maxItem != null) {
         maxBonus = (maxBonus + maxItem.getCurrentQualityLevel()) / 2.0F;
         ItemSpellEffects effs = maxItem.getSpellEffects();
         if (effs == null) {
            effs = new ItemSpellEffects(maxItem.getWurmId());
         }

         SpellEffect eff = effs.getSpellEffect((byte)29);
         if (eff != null && reducePower) {
            eff.setPower(eff.power - 0.2F);
         }
      }

      return maxBonus;
   }

   public int getNumberOfShopItems() {
      Set<Item> ite = this.getInventory().getItems();
      int nums = 0;

      for(Item i : ite) {
         if (!i.isCoin()) {
            ++nums;
         }
      }

      return nums;
   }

   public final void addNewbieBuffs() {
      if (this.getPlayingTime() < 86400000L) {
         SpellEffects effs = this.createSpellEffects();
         SpellEffect eff = effs.getSpellEffect((byte)74);
         if (eff == null) {
            this.getCommunicator().sendSafeServerMessage("You require less food and drink as a new player.");
            eff = new SpellEffect(this.getWurmId(), (byte)74, 100.0F, (int)((86400000L - this.getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
            effs.addSpellEffect(eff);
         }

         SpellEffect range = effs.getSpellEffect((byte)73);
         if (range == null) {
            this.getCommunicator().sendSafeServerMessage("Creatures and monsters are less aggressive to new players.");
            range = new SpellEffect(this.getWurmId(), (byte)73, 100.0F, (int)((86400000L - this.getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
            effs.addSpellEffect(range);
         }

         SpellEffect health = effs.getSpellEffect((byte)75);
         if (health == null) {
            this.getCommunicator().sendSafeServerMessage("You regenerate health faster as a new player.");
            health = new SpellEffect(this.getWurmId(), (byte)75, 100.0F, (int)((86400000L - this.getPlayingTime()) / 1000L), (byte)1, (byte)0, true);
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
         this.getCommunicator()
            .sendAddSpellEffect(
               effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect.getSpellInfluenceType(), effect.timeleft, effect.power
            );
      }
   }

   public void sendAddSpellEffect(SpellEffect effect) {
      SpellEffectsEnum spellEffect = SpellEffectsEnum.getEnumByName(effect.getName());
      if (spellEffect != SpellEffectsEnum.NONE) {
         this.getCommunicator().sendAddSpellEffect(effect.id, spellEffect, effect.timeleft, effect.power);
      } else {
         this.getCommunicator()
            .sendAddSpellEffect(
               effect.id, effect.getName(), effect.type, effect.getSpellEffectType(), effect.getSpellInfluenceType(), effect.timeleft, effect.power
            );
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
      if (this.getSpellEffects() != null) {
         SpellEffect ill = this.getSpellEffects().getSpellEffect((byte)72);
         if (ill != null) {
            this.getSpellEffects().removeSpellEffect(ill);
         }
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
      if (this.getMovementScheme().setWebArmourMod(false, 0.0F)) {
         this.getMovementScheme().setWebArmourMod(false, 0.0F);
         toret = true;
      }

      if (this.getSpellEffects() != null) {
         SpellEffect[] speffs = this.getSpellEffects().getEffects();

         for(int x = 0; x < speffs.length; ++x) {
            if (speffs[x].type != 64
               && speffs[x].type != 74
               && speffs[x].type != 73
               && speffs[x].type != 75
               && (double)Server.rand.nextInt(Math.max(1, (int)speffs[x].power)) < power) {
               this.getSpellEffects().removeSpellEffect(speffs[x]);
               if (speffs[x].type == 22 && this.getCurrentTile() != null) {
                  this.getCurrentTile().setNewRarityShader(this);
               }

               return true;
            }
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
      } else {
         try {
            return Server.getInstance().getCreature(this.dominator);
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public Item getWornItem(byte bodyPart) {
      try {
         return this.getEquippedItem(bodyPart);
      } catch (NoSpaceException var3) {
         return null;
      }
   }

   public boolean hasBridle() {
      if (this.isHorse() || this.isUnicorn()) {
         Item neckItem = this.getWornItem((byte)17);
         if (neckItem != null) {
            return neckItem.isBridle();
         }
      }

      return false;
   }

   private float calcHorseShoeBonus(boolean mounting) {
      float bonus = 0.0F;
      float leftFootB = 0.0F;
      float rightFootB = 0.0F;
      float leftHandB = 0.0F;
      float rightHandB = 0.0F;

      try {
         Item leftFoot = this.getEquippedItem((byte)15);
         if (leftFoot != null) {
            leftFootB += Math.max(10.0F, leftFoot.getCurrentQualityLevel()) / 2000.0F;
            leftFootB += leftFoot.getSpellSpeedBonus() / 2000.0F;
            leftFootB += (float)leftFoot.getRarity() * 0.03F;
            if (!mounting && !this.ignoreSaddleDamage) {
               leftFoot.setDamage(leftFoot.getDamage() + 0.001F);
            }
         }
      } catch (NoSpaceException var11) {
         logger.log(Level.WARNING, this.getName() + " No left foot.");
      }

      try {
         Item rightFoot = this.getEquippedItem((byte)16);
         if (rightFoot != null) {
            rightFootB += Math.max(10.0F, rightFoot.getCurrentQualityLevel()) / 2000.0F;
            rightFootB += rightFoot.getSpellSpeedBonus() / 2000.0F;
            rightFootB += (float)rightFoot.getRarity() * 0.03F;
            if (!mounting && !this.ignoreSaddleDamage) {
               rightFoot.setDamage(rightFoot.getDamage() + 0.001F);
            }
         }
      } catch (NoSpaceException var10) {
         logger.log(Level.WARNING, this.getName() + " No left foot.");
      }

      try {
         Item rightHand = this.getEquippedItem((byte)14);
         if (rightHand != null) {
            rightHandB += Math.max(10.0F, rightHand.getCurrentQualityLevel()) / 2000.0F;
            rightHandB += rightHand.getSpellSpeedBonus() / 2000.0F;
            rightHandB += (float)rightHand.getRarity() * 0.03F;
            if (!mounting && !this.ignoreSaddleDamage) {
               rightHand.setDamage(rightHand.getDamage() + 0.001F);
            }
         }
      } catch (NoSpaceException var9) {
         logger.log(Level.WARNING, this.getName() + " No left foot.");
      }

      try {
         Item leftHand = this.getEquippedItem((byte)13);
         if (leftHand != null) {
            leftHandB += Math.max(10.0F, leftHand.getCurrentQualityLevel()) / 2000.0F;
            leftHandB += leftHand.getSpellSpeedBonus() / 2000.0F;
            leftHandB += (float)leftHand.getRarity() * 0.03F;
            if (!mounting && !this.ignoreSaddleDamage) {
               leftHand.setDamage(leftHand.getDamage() + 0.001F);
            }
         }
      } catch (NoSpaceException var8) {
         logger.log(Level.WARNING, this.getName() + " No left foot.");
      }

      bonus += leftHandB;
      bonus += rightHandB;
      bonus += leftFootB;
      return bonus + rightFootB;
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
         } catch (IOException var4) {
            logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         }

         this.setLoyalty(0.0F);
         this.setLeader(null);
      }

      if (newdominator != this.dominator) {
         this.dominator = newdominator;
         this.getStatus().setDominator(this.dominator);
         this.sendAttitudeChange();
         return true;
      } else {
         return false;
      }
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
      return this.decisions != null ? this.decisions.getFirst() : null;
   }

   public void removeOrder(Order order) {
      if (this.decisions != null) {
         this.decisions.removeOrder(order);
      }
   }

   public boolean hasOrders() {
      return this.decisions != null ? this.decisions.hasOrders() : false;
   }

   public boolean mayReceiveOrder() {
      if (this.decisions != null) {
         return this.decisions.mayReceiveOrders();
      } else if (this.isDominated()) {
         this.decisions = new DecisionStack();
         return true;
      } else {
         return false;
      }
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
      if (_vehicle == -10L) {
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
                  Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
                  lVehicle.removeRider(this.getWurmId());
                  if (teleport) {
                     Structure struct = this.getActualTileVehicle().getStructure();
                     if (struct != null && !struct.mayPass(this)) {
                        try {
                           float newposx = lVehicle.getPosX();
                           float newposy = lVehicle.getPosY();
                           tilex = (int)newposx / 4;
                           tiley = (int)newposy / 4;
                        } catch (Exception var29) {
                           logger.log(Level.WARNING, var29.getMessage(), (Throwable)var29);
                        }
                     }

                     if (!this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) {
                        try {
                           float newposx = lVehicle.getPosX();
                           float newposy = lVehicle.getPosY();
                           tilex = (int)newposx / 4;
                           tiley = (int)newposy / 4;
                        } catch (Exception var28) {
                           logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
                        }
                     }
                  }
               } catch (NoSuchCreatureException var30) {
                  logger.log(Level.WARNING, this.getName() + " " + var30.getMessage(), (Throwable)var30);
               } catch (NoSuchPlayerException var31) {
                  logger.log(Level.WARNING, this.getName() + " " + var31.getMessage(), (Throwable)var31);
               }
            } else {
               try {
                  Item ivehic = Items.getItem(this.vehicle);
                  boolean atTransferBorder = false;
                  if (this.getTileX() < 20
                     || this.getTileX() > Zones.worldTileSizeX - 20
                     || this.getTileY() < 20
                     || this.getTileY() > Zones.worldTileSizeX - 20) {
                     atTransferBorder = true;
                  }

                  if (!ivehic.isBoat() || !this.isTransferring() && !atTransferBorder) {
                     this.setLastVehicle(-10L, (byte)-1);
                     if (teleport) {
                        Structure struct = this.getActualTileVehicle().getStructure();
                        if (struct != null && struct.isTypeHouse() && !struct.mayPass(this)) {
                           try {
                              Creature dragger = Items.getDragger(ivehic);
                              float newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                              float newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                              tilex = (int)newposx / 4;
                              tiley = (int)newposy / 4;
                           } catch (Exception var27) {
                              logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
                           }
                        }

                        if (struct != null && struct.isTypeBridge()) {
                           try {
                              Creature dragger = Items.getDragger(ivehic);
                              float newposx = dragger == null ? ivehic.getPosX() : dragger.getPosX();
                              float newposy = dragger == null ? ivehic.getPosY() : dragger.getPosY();
                              tilex = (int)newposx / 4;
                              tiley = (int)newposy / 4;
                           } catch (Exception var26) {
                              logger.log(Level.WARNING, var26.getMessage(), (Throwable)var26);
                           }
                        }

                        if (!this.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) {
                           try {
                              float newposx = ivehic.getPosX();
                              float newposy = ivehic.getPosY();
                              tilex = (int)newposx / 4;
                              tiley = (int)newposy / 4;
                           } catch (Exception var25) {
                              logger.log(Level.WARNING, var25.getMessage(), (Throwable)var25);
                           }
                        }
                     }
                  }
               } catch (NoSuchItemException var36) {
                  this.setLastVehicle(-10L, (byte)-1);
               }
            }
         }

         this.getMovementScheme().offZ = 0.0F;
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
            this.clearDestination();
            this.setFarwalkerSeconds((byte)0);
            this.getMovementScheme().setFarwalkerMoveMod(false);
            this.movementScheme.setEncumbered(false);
            this.movementScheme.setBaseModifier(1.0F);
            this.setStealth(false);
            float offx = 0.0F;
            float offy = 0.0F;

            for(int x = 0; x < vehic.seats.length; ++x) {
               if (vehic.seats[x].occupant == this.getWurmId()) {
                  offx = vehic.seats[x].offx;
                  offy = vehic.seats[x].offy;
                  break;
               }
            }

            if (vehic.creature) {
               try {
                  Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
                  float r = (-lVehicle.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -offx - c * -offy;
                  float yo = c * -offx + s * -offy;
                  float newposx = lVehicle.getPosX() + xo;
                  float newposy = lVehicle.getPosY() + yo;
                  this.getMovementScheme().setVehicleRotation(lVehicle.getStatus().getRotation());
                  this.getStatus().setRotation(lVehicle.getStatus().getRotation());
                  this.setBridgeId(lVehicle.getBridgeId());
                  this.setTeleportPoints(newposx, newposy, lVehicle.getLayer(), lVehicle.getFloorLevel());
                  if (this.getVisionArea() != null && (int)newposx >> 2 == this.getTileX() && (int)newposy >> 2 == this.getTileY()) {
                     this.embark(
                        newposx,
                        newposy,
                        this.getPositionZ(),
                        this.getStatus().getRotation(),
                        this.teleportLayer,
                        "Embarking " + vehic.name,
                        null,
                        lVehicle,
                        vehic
                     );
                  } else if (!this.getCommunicator().stillLoggingIn()) {
                     int tx = this.getTileX();
                     int ty = this.getTileY();
                     int nx = (int)newposx >> 2;
                     int ny = (int)newposy >> 2;

                     try {
                        if (this.hasLink() && this.getVisionArea() != null) {
                           this.getVisionArea().move(nx - tx, ny - ty);
                           this.embark(
                              newposx,
                              newposy,
                              this.getPositionZ(),
                              this.getStatus().getRotation(),
                              this.teleportLayer,
                              "Embarking " + vehic.name,
                              null,
                              lVehicle,
                              vehic
                           );
                           this.getVisionArea().linkZones(nx - tx, ny - ty);
                        }
                     } catch (IOException var33) {
                        this.startTeleporting(true);
                        lVehicle.setLeader(null);
                        lVehicle.addRider(this.getWurmId());
                        this.sendMountData();
                        if (this.isVehicleCommander()) {
                           this.getCommunicator().sendTeleport(true, false, vehic.commandType);
                        } else {
                           this.getCommunicator().sendTeleport(false, false, (byte)0);
                        }
                     }
                  } else {
                     this.startTeleporting(true);
                     lVehicle.setLeader(null);
                     lVehicle.addRider(this.getWurmId());
                     this.sendMountData();
                     if (this.isVehicleCommander()) {
                        this.getCommunicator().sendTeleport(true, false, vehic.commandType);
                     } else {
                        this.getCommunicator().sendTeleport(false, false, (byte)0);
                     }
                  }
               } catch (NoSuchCreatureException var34) {
                  logger.log(Level.WARNING, this.getName() + " " + var34.getMessage(), (Throwable)var34);
               } catch (NoSuchPlayerException var35) {
                  logger.log(Level.WARNING, this.getName() + " " + var35.getMessage(), (Throwable)var35);
               }
            } else {
               try {
                  Item lVehicle = Items.getItem(vehic.wurmid);
                  float r = (-lVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -offx - c * -offy;
                  float yo = c * -offx + s * -offy;
                  float newposx = lVehicle.getPosX() + xo;
                  float newposy = lVehicle.getPosY() + yo;
                  this.getMovementScheme().setVehicleRotation(lVehicle.getRotation());
                  this.getStatus().setRotation(lVehicle.getRotation());
                  this.setBridgeId(lVehicle.getBridgeId());
                  if (this.getVisionArea() != null && (int)newposx >> 2 == this.getTileX() && (int)newposy >> 2 == this.getTileY()) {
                     this.embark(
                        newposx,
                        newposy,
                        this.getPositionZ(),
                        this.getStatus().getRotation(),
                        this.teleportLayer,
                        "Embarking " + vehic.name,
                        lVehicle,
                        null,
                        vehic
                     );
                  } else {
                     this.setTeleportPoints(newposx, newposy, lVehicle.isOnSurface() ? 0 : -1, lVehicle.getFloorLevel());
                     if (this.isVehicleCommander()) {
                        if (lVehicle.getKingdom() != this.getKingdomId()) {
                           Server.getInstance()
                              .broadCastAction(
                                 LoginHandler.raiseFirstLetter(lVehicle.getName())
                                    + " is now the property of "
                                    + Kingdoms.getNameFor(this.getKingdomId())
                                    + "!",
                                 this,
                                 10
                              );
                           String message = StringUtil.format(
                              "You declare the %s the property of %s.", lVehicle.getName(), Kingdoms.getNameFor(this.getKingdomId())
                           );
                           this.getCommunicator().sendNormalServerMessage(message);
                           lVehicle.setLastOwnerId(this.getWurmId());
                        } else if (Servers.isThisAChaosServer()) {
                           Village v = Villages.getVillageForCreature(lVehicle.getLastOwnerId());
                           if (v == null || v.isEnemy(this.getCitizenVillage())) {
                              String vehname = this.getName();
                              if (this.getCitizenVillage() != null) {
                                 vehname = this.getCitizenVillage().getName();
                              }

                              Server.getInstance()
                                 .broadCastAction(LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + vehname + "!", this, 10);
                              String message = StringUtil.format("You declare the %s the property of %s.", lVehicle.getName(), vehname);
                              this.getCommunicator().sendNormalServerMessage(message);
                              lVehicle.setLastOwnerId(this.getWurmId());
                           }
                        }

                        lVehicle.setAuxData(this.getKingdomId());
                        this.setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle);
                     } else {
                        this.setEmbarkTeleportVehicle(newposx, newposy, vehic, lVehicle);
                     }
                  }
               } catch (NoSuchItemException var32) {
                  logger.log(Level.WARNING, this.getName() + " " + var32.getMessage(), (Throwable)var32);
               }
            }
         }
      } else if (teleport) {
         if (tilex < 0
            && tiley < 0
            && !this.isOnSurface()
            && Tiles.isSolidCave(
               Tiles.decodeType(Server.caveMesh.getTile((int)(this.getStatus().getPositionX() / 4.0F), (int)(this.getStatus().getPositionX() / 4.0F)))
            )) {
            try {
               if (WurmId.getType(this.vehicle) == 1) {
                  Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
                  float newposx = lVehicle.getPosX();
                  float newposy = lVehicle.getPosY();
                  tilex = (int)newposx / 4;
                  tiley = (int)newposy / 4;
               } else {
                  Item ivehic = Items.getItem(this.vehicle);
                  float newposx = ivehic.getPosX();
                  float newposy = ivehic.getPosY();
                  tilex = (int)newposx / 4;
                  tiley = (int)newposy / 4;
               }
            } catch (Exception var24) {
               logger.log(Level.WARNING, var24.getMessage(), (Throwable)var24);
            }
         }

         if (tilex <= -1 && tiley <= -1) {
            Structure struct = this.getCurrentTile() != null
               ? this.getCurrentTile().getStructure()
               : Structures.getStructureForTile(this.getTileX(), this.getTileY(), this.isOnSurface());
            if (struct == null || struct.mayPass(this)) {
               float posz = this.getStatus().getPositionZ();
               posz = Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), false, posz, this, this.getBridgeId());
               this.intraTeleport(
                  this.getStatus().getPositionX(), this.getStatus().getPositionY(), posz, this.getStatus().getRotation(), this.getLayer(), "left vehicle"
               );
            }
         } else {
            int ntx = tilex - this.getTileX();
            int nty = tiley - this.getTileY();
            float posz = this.getStatus().getPositionZ();
            posz = Zones.calculatePosZ(this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), false, posz, this, this.getBridgeId());

            try {
               if (this.hasLink() && this.getVisionArea() != null) {
                  this.getVisionArea().move(ntx, nty);
                  this.intraTeleport((float)(tilex * 4 + 2), (float)(tiley * 4 + 2), posz, this.getStatus().getRotation(), this.getLayer(), "left vehicle");
                  this.getVisionArea().linkZones(ntx, nty);
               }
            } catch (IOException var23) {
               this.setTeleportPoints((short)tilex, (short)tiley, this.getLayer(), 0);
               this.startTeleporting(false);
               this.getCommunicator().sendTeleport(false, true, (byte)0);
            }
         }

         this.getMovementScheme().addWindImpact((byte)0);
         this.calcBaseMoveMod();
         this.getMovementScheme().commandingBoat = false;
         this.getCurrentTile().sendAttachCreature(this.getWurmId(), -1L, 0.0F, 0.0F, 0.0F, 0);
      } else {
         if (!this.getMovementScheme().isIntraTeleporting()) {
            this.getMovementScheme().addWindImpact((byte)0);
            this.calcBaseMoveMod();
            this.getMovementScheme().setMooredMod(false);
            this.getMovementScheme().commandingBoat = false;
         }

         this.getCurrentTile().sendAttachCreature(this.getWurmId(), -1L, 0.0F, 0.0F, 0.0F, 0);
      }
   }

   public void intraTeleport(float posx, float posy, float posz, float aRot, int layer, String reason) {
      if (reason.contains("in rock")) {
         posx = this.getMovementScheme().xOld;
         posy = this.getMovementScheme().yOld;
      }

      ++this.teleports;
      if (!this.isDead()) {
         posx = Math.max(0.0F, Math.min(posx, Zones.worldMeterSizeX - 1.0F));
         posy = Math.max(0.0F, Math.min(posy, Zones.worldMeterSizeY - 1.0F));
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
            logger.log(
               Level.INFO,
               this.getName()
                  + " intrateleport to "
                  + posx
                  + ","
                  + posy
                  + ", "
                  + posz
                  + ", layer "
                  + layer
                  + " currentTile:null="
                  + (t == null)
                  + " reason="
                  + reason
                  + " hasVisionArea="
                  + (this.getVisionArea() != null)
                  + ", initialized="
                  + visionAreaInitialized
                  + " vehicle="
                  + this.vehicle,
               (Throwable)(new Exception())
            );
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
         } catch (NoSuchActionException var10) {
         }

         this.getMovementScheme().commandingBoat = false;
         this.getMovementScheme().addWindImpact((byte)0);
         this.getCommunicator().sendTeleport(true);
         this.disembark(false);
         this.getMovementScheme().addIntraTeleport(this.getTeleportCounter());
      }
   }

   public Vector3f getActualPosVehicle() {
      Vector3f toReturn = new Vector3f(this.getPosX(), this.getPosY(), this.getPositionZ());
      if (this.vehicle != -10L) {
         Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
         if (vehic != null) {
            float offx = 0.0F;
            float offy = 0.0F;

            for(int x = 0; x < vehic.seats.length; ++x) {
               if (vehic.seats[x].occupant == this.getWurmId()) {
                  offx = vehic.seats[x].offx;
                  offy = vehic.seats[x].offy;
                  break;
               }
            }

            if (vehic.creature) {
               try {
                  Creature lVehicle = Server.getInstance().getCreature(this.vehicle);
                  float r = (-lVehicle.getStatus().getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -offx - c * -offy;
                  float yo = c * -offx + s * -offy;
                  float newposx = lVehicle.getPosX() + xo;
                  float newposy = lVehicle.getPosY() + yo;
                  toReturn.setX(newposx);
                  toReturn.setY(newposy);
               } catch (NoSuchPlayerException | NoSuchCreatureException var14) {
               }
            } else {
               try {
                  Item lVehicle = Items.getItem(vehic.wurmid);
                  float r = (-lVehicle.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                  float s = (float)Math.sin((double)r);
                  float c = (float)Math.cos((double)r);
                  float xo = s * -offx - c * -offy;
                  float yo = c * -offx + s * -offy;
                  float newposx = lVehicle.getPosX() + xo;
                  float newposy = lVehicle.getPosY() + yo;
                  toReturn.setX(newposx);
                  toReturn.setY(newposy);
               } catch (NoSuchItemException var13) {
               }
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
               this.embark(
                  newposx, newposy, this.getPositionZ(), this.getStatus().getRotation(), this.teleportLayer, "Embarking " + vehic.name, lVehicle, null, vehic
               );
               this.getVisionArea().linkZones(nx - tx, ny - ty);
            }
         } catch (IOException var10) {
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
      if (!this.isVehicleCommander()) {
         this.stopLeading();
      }

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
      } catch (NoSuchActionException var15) {
      }

      this._enterVehicle = true;
      if (cVehicle != null) {
         cVehicle.setLeader(null);
         cVehicle.addRider(this.getWurmId());
      }

      this.sendMountData();
      if (this.isVehicleCommander()) {
         if (lVehicle != null) {
            if (lVehicle.getKingdom() != this.getKingdomId()) {
               Server.getInstance()
                  .broadCastAction(
                     LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + Kingdoms.getNameFor(this.getKingdomId()) + "!", this, 10
                  );
               String message = StringUtil.format("You declare the %s the property of %s.", lVehicle.getName(), Kingdoms.getNameFor(this.getKingdomId()));
               this.getCommunicator().sendNormalServerMessage(message);
               lVehicle.setLastOwnerId(this.getWurmId());
            } else if (Servers.isThisAChaosServer()) {
               Village v = Villages.getVillageForCreature(lVehicle.getLastOwnerId());
               if (v == null || v.isEnemy(this.getCitizenVillage())) {
                  String vehname = this.getName();
                  if (this.getCitizenVillage() != null) {
                     vehname = this.getCitizenVillage().getName();
                  }

                  Server.getInstance()
                     .broadCastAction(LoginHandler.raiseFirstLetter(lVehicle.getName()) + " is now the property of " + vehname + "!", this, 10);
                  String message = StringUtil.format("You declare the %s the property of %s.", lVehicle.getName(), vehname);
                  this.getCommunicator().sendNormalServerMessage(message);
                  lVehicle.setLastOwnerId(this.getWurmId());
               }
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
            if (vehic.pilotId == this.getWurmId()) {
               this.setVehicleCommander(false);
               vehic.pilotId = -10L;
               this.getCommunicator().setVehicleController(-1L, -1L, 0.0F, 0.0F, 0.0F, -2000.0F, 2000.0F, 2000.0F, 0.0F, 0);

               try {
                  Item item = Items.getItem(this.vehicle);
                  item.savePosition();
               } catch (Exception var8) {
               }
            } else if (vehic.pilotId != -10L) {
               try {
                  Item item = Items.getItem(this.vehicle);
                  item.savePosition();
                  Creature pilot = Server.getInstance().getCreature(vehic.pilotId);
                  if (!vehic.creature && item.isBoat()) {
                     pilot.getMovementScheme().addMountSpeed((short)vehic.calculateNewBoatSpeed(true));
                  } else if (vehic.creature) {
                     vehic.updateDraggedSpeed(true);
                  }
               } catch (WurmServerException var9) {
               } catch (Exception var10) {
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

            for(int x = 0; x < vehic.seats.length; ++x) {
               if (vehic.seats[x].occupant == this.getWurmId()) {
                  vehic.seats[x].occupant = -10L;
                  ++found;
               }
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
      return this.getPower() > 0 ? true : this.template.isGlowing();
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
      } else if (this.opportunityAttackCounter > 0) {
         return false;
      } else {
         return (double)this.getCombatHandler().getOpportunityAttacks() < this.getFightingSkill().getKnowledge(0.0) / 10.0;
      }
   }

   public boolean opportunityAttack(Creature creature) {
      if (creature.isInvulnerable()) {
         return false;
      } else if (!creature.isVisibleTo(this)) {
         return false;
      } else if (this.isPlayer() && creature.isPlayer() && !Servers.isThisAPvpServer() && !this.isDuelOrSpar(creature)) {
         return false;
      } else {
         if ((this.isFighting() || creature.getWurmId() == this.target) && (!this.isPlayer() || !creature.isPlayer())) {
            if (this.isBridgeBlockingAttack(creature, false)) {
               return false;
            }

            if (this.mayOpportunityAttack()
               && this.getLayer() == creature.getLayer()
               && this.getMindSpeed().skillCheck((double)(this.getCombatHandler().getOpportunityAttacks() * 10), 0.0, false, 1.0F) > 0.0) {
               if (this.opponent == null) {
                  this.setOpponent(creature);
               }

               return this.getCombatHandler().attack(creature, 10, true, 2.0F, null);
            }
         }

         return false;
      }
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
            this.stealthBreakers = new HashSet<>();
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
      if (this.isVisible()) {
         if (this.isStealth()) {
            if (this.getPower() > 0 && this.getPower() <= watcher.getPower()) {
               return true;
            } else if (this.getPower() < watcher.getPower()) {
               return true;
            } else if (watcher.isUnique()) {
               return true;
            } else if (this.stealthBreakers != null && this.stealthBreakers.contains(watcher.getWurmId())) {
               return true;
            } else {
               int distModifier = (int)Math.max(Math.abs(watcher.getPosX() - this.getPosX()), Math.abs(watcher.getPosY() - this.getPosY()));
               if (watcher.getCurrentTile() != this.getCurrentTile()
                  && !watcher.isDetectInvis()
                  && !((float)Server.rand.nextInt((int)(100.0F + difficultyModifier + (float)distModifier)) < watcher.getDetectDangerBonus() / 5.0F)
                  && !(watcher.getMindLogical().skillCheck(this.getBodyControl() + (double)difficultyModifier + (double)distModifier, 0.0, true, 1.0F) > 0.0)) {
                  return false;
               } else {
                  if (this.stealthBreakers == null) {
                     this.stealthBreakers = new HashSet<>();
                  }

                  this.stealthBreakers.add(watcher.getWurmId());
                  return true;
               }
            }
         } else {
            return true;
         }
      } else {
         return this.getPower() > 0 && this.getPower() <= watcher.getPower();
      }
   }

   public boolean isDetectInvis() {
      if (this.template.isDetectInvis()) {
         return true;
      } else {
         return this.status.detectInvisCounter > 0;
      }
   }

   public boolean isVisibleTo(Creature watcher) {
      return this.isVisibleTo(watcher, false);
   }

   public boolean isVisibleTo(Creature watcher, boolean ignoreStealth) {
      if (this.isVisible()) {
         if (this.isStealth() && !ignoreStealth) {
            if (this.getPower() > 0 && this.getPower() <= watcher.getPower()) {
               return true;
            } else if (this.getPower() < watcher.getPower()) {
               return true;
            } else if (!watcher.isUnique() && !watcher.isDetectInvis()) {
               return this.stealthBreakers != null && this.stealthBreakers.contains(watcher.getWurmId());
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return this.getPower() > 0 && this.getPower() <= watcher.getPower();
      }
   }

   public void addVisionModifier(DoubleValueModifier modifier) {
      if (this.visionModifiers == null) {
         this.visionModifiers = new HashSet<>();
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
      } else {
         double doubleModifier = 0.0;

         for(DoubleValueModifier lDoubleValueModifier : this.visionModifiers) {
            doubleModifier += lDoubleValueModifier.getModifier();
         }

         return doubleModifier;
      }
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
      double strength;
      if (!spell) {
         strength = this.getStrengthSkill();
      } else {
         strength = this.getSoulStrengthVal();
      }

      float damMod = (float)(120.0 - strength) / 100.0F;
      if (this.isPlayer() && pvp && Servers.localServer.PVPSERVER) {
         damMod = (float)(1.0 - 0.15 * Math.log(Math.max(20.0, strength) * 0.8F - 15.0));
         damMod = Math.max(Math.min(damMod, 1.0F), 0.2F);
      }

      if (this.hasSpellEffect((byte)96)) {
         damMod *= 1.1F;
      }

      if (this.getCultist() != null) {
         float percent = this.getCultist().getHalfDamagePercentage();
         if (percent > 0.0F) {
            if (this.isChampion()) {
               float red = 1.0F - 0.1F * percent;
               damMod *= red;
            } else {
               float red = 1.0F - 0.3F * percent;
               damMod *= red;
            }
         }
      }

      return damMod;
   }

   @Override
   public String toString() {
      return "Creature [id: "
         + this.id
         + ", name: "
         + this.name
         + ", Tile: "
         + this.currentTile
         + ", Template: "
         + this.template
         + ", Status: "
         + this.status
         + ']';
   }

   public void sendToLoggers(String tolog) {
      this.sendToLoggers(tolog, (byte)2);
   }

   public void sendToLoggers(String tolog, byte restrictedToPower) {
      if (this.loggerCreature1 != -10L) {
         try {
            Creature receiver = Server.getInstance().getCreature(this.loggerCreature1);
            receiver.getCommunicator().sendLogMessage(this.getName() + " [" + tolog + "]");
         } catch (Exception var5) {
            this.loggerCreature1 = -10L;
         }
      }

      if (this.loggerCreature2 != -10L) {
         try {
            Creature receiver = Server.getInstance().getCreature(this.loggerCreature2);
            receiver.getCommunicator().sendLogMessage(this.getName() + " [" + tolog + "]");
         } catch (Exception var4) {
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
            return king.currentLand > 2.0F;
         } else {
            return false;
         }
      } else {
         return true;
      }
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

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (int)(this.id ^ this.id >>> 32);
      return 31 * result + (this.isPlayer() ? 1231 : 1237);
   }

   public void setCheated(String reason) {
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof Creature)) {
         return false;
      } else {
         Creature other = (Creature)obj;
         if (this.id != other.id) {
            return false;
         } else {
            return this.isPlayer() == other.isPlayer();
         }
      }
   }

   public boolean seesPlayerAssistantWindow() {
      return false;
   }

   public void setHitched(@Nullable Vehicle _hitched, boolean loading) {
      this.hitchedTo = _hitched;
      if (this.hitchedTo != null) {
         this.clearOrders();
         this.seatType = 2;
         if (!loading) {
            this.getStatus().setVehicle(this.hitchedTo.wurmid, this.seatType);
         }
      } else {
         this.seatType = -1;
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
         this.riders = new HashSet<>();
      }

      this.riders.add(newrider);
   }

   public void removeRider(long lostrider) {
      if (this.riders == null) {
         this.riders = new HashSet<>();
      }

      this.riders.remove(lostrider);
   }

   protected void forceMountSpeedChange() {
      this.mountPollCounter = 0;
      this.pollMount();
   }

   private void pollMount() {
      if (this.isRidden()) {
         if (this.mountPollCounter > 0 && Server.rand.nextInt(100) != 0) {
            --this.mountPollCounter;
         } else {
            Vehicle vehic = Vehicles.getVehicleForId(this.getWurmId());
            if (vehic != null) {
               try {
                  Creature rider = Server.getInstance().getCreature(vehic.getPilotSeat().occupant);
                  byte val = vehic.calculateNewMountSpeed(this, false);
                  if (this.switchv) {
                     --val;
                  }

                  this.switchv = !this.switchv;
                  rider.getMovementScheme().addMountSpeed((short)val);
               } catch (NoSuchCreatureException var4) {
               } catch (NoSuchPlayerException var5) {
               }
            }

            this.mountPollCounter = 20;
         }
      }
   }

   public boolean mayChangeSpeed() {
      return this.mountPollCounter <= 0;
   }

   public float getMountSpeedPercent(boolean mounting) {
      float factor = 0.5F;
      if (this.getStatus().getHunger() < 45000) {
         factor += 0.2F;
      }

      if (this.getStatus().getHunger() < 10000) {
         factor += 0.1F;
      }

      if (this.getStatus().damage < 10000) {
         factor += 0.1F;
      } else if (this.getStatus().damage > 20000) {
         factor -= 0.5F;
      } else if (this.getStatus().damage > 45000) {
         factor -= 0.7F;
      }

      if (this.isHorse() || this.isUnicorn()) {
         float hbonus = this.calcHorseShoeBonus(mounting);
         this.sendToLoggers("Horse shoe bonus " + hbonus + " so factor from " + factor + " to " + (factor + hbonus));
         factor += hbonus;
      }

      float tperc = this.getTraitMovePercent(mounting);
      this.sendToLoggers("Trait move percent= " + tperc + " so factor from " + factor + " to " + (factor + tperc));
      factor += tperc;
      if (this.getBonusForSpellEffect((byte)22) > 0.0F) {
         factor -= 0.2F * (this.getBonusForSpellEffect((byte)22) / 100.0F);
      }

      if (this.isRidden()) {
         Item torsoItem = this.getWornItem((byte)2);
         if (torsoItem != null && (torsoItem.isSaddleLarge() || torsoItem.isSaddleNormal())) {
            factor += Math.max(10.0F, torsoItem.getCurrentQualityLevel()) / 1000.0F;
            factor += (float)torsoItem.getRarity() * 0.03F;
            factor += torsoItem.getSpellSpeedBonus() / 2000.0F;
            if (!mounting && !this.ignoreSaddleDamage) {
               torsoItem.setDamage(torsoItem.getDamage() + 0.001F);
            }

            this.ignoreSaddleDamage = false;
         }

         this.sendToLoggers("After saddle move percent= " + factor);
         factor *= this.getMovementScheme().getSpeedModifier();
         this.sendToLoggers("After speedModifier " + this.getMovementScheme().getSpeedModifier() + " move percent= " + factor);
      }

      return factor;
   }

   private int getCarriedMountWeight() {
      int currWeight = this.getCarriedWeight();
      int bagsWeight = this.getSaddleBagsCarriedWeight();
      currWeight -= bagsWeight;
      if (this.isRidden()) {
         for(Long lLong : this.riders) {
            try {
               Creature _rider = Server.getInstance().getCreature(lLong);
               currWeight += Math.max(30000, _rider.getStatus().fat * 1000);
               currWeight += _rider.getCarriedWeight();
            } catch (NoSuchCreatureException var6) {
            } catch (NoSuchPlayerException var7) {
            }
         }
      }

      return currWeight;
   }

   public boolean hasTraits() {
      return this.status.traits != 0L;
   }

   public boolean hasTrait(int traitbit) {
      return this.status.traits != 0L ? this.status.isTraitBitSet(traitbit) : false;
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
      return this.status.traits != 0L ? this.status.removeRandomNegativeTrait() : false;
   }

   private float getTraitMovePercent(boolean mounting) {
      float traitMod = 0.0F;
      Creature r = null;
      boolean moving = false;
      if (this.isRidden() && this.getMountVehicle() != null) {
         try {
            r = Server.getInstance().getCreature(this.getMountVehicle().pilotId);
            moving = r.isMoving();
         } catch (NoSuchCreatureException var8) {
         } catch (NoSuchPlayerException var9) {
         }
      }

      int cweight = this.getCarriedMountWeight();
      if (!mounting && this.status.traits != 0L) {
         Skill sstrength = this.getSoulStrength();
         if (this.status.isTraitBitSet(1) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            traitMod += 0.1F;
         }

         if (!this.status.isTraitBitSet(15)
            && !this.status.isTraitBitSet(16)
            && !this.status.isTraitBitSet(17)
            && !this.status.isTraitBitSet(18)
            && !this.status.isTraitBitSet(24)
            && !this.status.isTraitBitSet(25)
            && this.status.isTraitBitSet(23)
            && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            traitMod += 0.025F;
         }

         if (this.status.isTraitBitSet(4) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            traitMod += 0.2F;
         }

         if (this.status.isTraitBitSet(8) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) < 0.0)) {
            traitMod -= 0.1F;
         }

         if (this.status.isTraitBitSet(9) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) < 0.0)) {
            traitMod -= 0.3F;
         }

         if (this.status.isTraitBitSet(6) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            traitMod += 0.1F;
         }

         float wmod = 0.0F;
         if (this.status.isTraitBitSet(3) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            wmod += 10000.0F;
         }

         if (this.status.isTraitBitSet(5) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            wmod += 20000.0F;
         }

         if (this.status.isTraitBitSet(11) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) < 0.0)) {
            wmod -= 30000.0F;
         }

         if (this.status.isTraitBitSet(6) && (!this.isHorse() || sstrength.skillCheck(20.0, 0.0, !moving, 1.0F) > 0.0)) {
            wmod += 10000.0F;
         }

         if ((double)cweight > this.getStrengthSkill() * 5000.0 + (double)wmod) {
            traitMod = (float)((double)traitMod - 0.15 * ((double)cweight - this.getStrengthSkill() * 5000.0 - (double)wmod) / 50000.0);
         }
      } else if ((double)cweight > this.getStrengthSkill() * 5000.0) {
         traitMod = (float)((double)traitMod - 0.15 * ((double)cweight - this.getStrengthSkill() * 5000.0) / 50000.0);
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
      if (!this.isDead() && !potentialMate.isDead()) {
         if (potentialMate.getTemplate().getMateTemplateId() == this.template.getTemplateId()
            || this.template.getTemplateId() == 96 && potentialMate.getTemplate().getTemplateId() == 96) {
            if (this.template.getAdultFemaleTemplateId() != -1 || this.template.getAdultMaleTemplateId() != -1) {
               return false;
            }

            if (potentialMate.getSex() != this.getSex() && potentialMate.getWurmId() != this.getWurmId()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean checkBreedingPossibility() {
      Creature[] crets = this.getCurrentTile().getCreatures();
      if (!this.isKingdomGuard()
         && !this.isGhost()
         && !this.isHuman()
         && crets.length > 0
         && this.mayMate(crets[0])
         && !crets[0].isPregnant()
         && !this.isPregnant()) {
         try {
            BehaviourDispatcher.action(this, this.getCommunicator(), -1L, crets[0].getWurmId(), (short)379);
            return true;
         } catch (Exception var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isInTheMoodToBreed(boolean forced) {
      if (this.getStatus().getHunger() > 10000) {
         return false;
      } else if (this.template.getAdultFemaleTemplateId() != -1 || this.template.getAdultMaleTemplateId() != -1) {
         return false;
      } else if (this.getStatus().age <= 3) {
         return false;
      } else {
         return this.breedCounter == 0 || forced && !this.forcedBreed;
      }
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
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DISEASE, 100000, (float)this.getStatus().disease);
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
      } catch (Exception var2) {
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

         for(int a = 0; a < warr.length; ++a) {
            if (warr[a].isPoison()) {
               this.getCommunicator()
                  .sendAddSpellEffect(SpellEffectsEnum.POISON.createId(warr[a].getWurmId()), SpellEffectsEnum.POISON, 100000, warr[a].getPoisonSeverity());
               this.hasSentPoison = true;
            }
         }
      }
   }

   public final boolean isPoisoned() {
      Wounds w = this.getBody().getWounds();
      if (w != null && w.getWounds() != null) {
         Wound[] warr = w.getWounds();

         for(int a = 0; a < warr.length; ++a) {
            if (warr[a].isPoison()) {
               return true;
            }
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
      } else if (this.isInTheNorthEast()) {
         return 1;
      } else if (this.isInTheSouthEast()) {
         return 3;
      } else if (this.isInTheSouthWest()) {
         return 5;
      } else if (this.isInTheNorth()) {
         return 0;
      } else if (this.isInTheEast()) {
         return 2;
      } else if (this.isInTheSouth()) {
         return 4;
      } else {
         return this.isInTheWest() ? 6 : -1;
      }
   }

   public boolean mayDestroy(Item item) {
      if (item.isDestroyable(this.getWurmId())) {
         return true;
      } else {
         if (item.isOwnerDestroyable() && !item.isLocked()) {
            Village village = Zones.getVillage(item.getTilePos(), item.isOnSurface());
            if (village != null) {
               return village.isActionAllowed((short)83, this);
            }

            if (!item.isUnfinished()) {
               return true;
            }

            if (item.getRealTemplate() != null && item.getRealTemplate().isKingdomMarker() && this.getKingdomId() != item.getAuxData()) {
               return true;
            }
         }

         if (item.isEnchantedTurret()) {
            VolaTile t = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface());
            if (t != null && t.getVillage() != null && t.getVillage().isPermanent && t.getVillage().kingdom == item.getKingdom()) {
               return false;
            }
         }

         return false;
      }
   }

   public boolean isCaredFor() {
      return !this.isUnique() && !this.onlyAttacksPlayers() ? Creatures.getInstance().isCreatureProtected(this.getWurmId()) : false;
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
      } else {
         Village pVill = Villages.getVillage(this.getTileX(), this.getTileY(), true);
         return pVill != null && bVill.getId() == pVill.getId();
      }
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
      if (ftime == null) {
         ftime = time;
      } else {
         ftime = ftime + time;
      }

      this.weaponsUsed.put(weapon, ftime);
      return ftime;
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
      for(AttackAction key : this.attackUsed.keySet()) {
         UsedAttackData data = this.attackUsed.get(key);
         if (data != null) {
            data.update(data.getTime() - time);
         }
      }
   }

   public final UsedAttackData getUsedAttackData(AttackAction act) {
      return this.attackUsed.get(act);
   }

   public final float deductFromWeaponUsed(Item weapon, float swingTime) {
      Float ftime = this.weaponsUsed.get(weapon);
      if (ftime == null) {
         ftime = swingTime;
      }

      while(ftime >= swingTime) {
         ftime = ftime - swingTime;
      }

      this.weaponsUsed.put(weapon, ftime);
      return ftime;
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
         this.fightlevel = 5;
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
         this.fightlevel = 5;
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
      return this.currentVillage != null && this.currentVillage.getReputationObject(this.getWurmId()) != null
         ? this.currentVillage.getReputationObject(this.getWurmId()).isPermanent()
         : false;
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
      } else {
         return false;
      }
   }

   public final void reduceStoneSkin() {
      if (this.getSpellEffects() != null) {
         SpellEffect sk = this.getSpellEffects().getSpellEffect((byte)68);
         if (sk != null) {
            if (sk.getPower() > 34.0F) {
               sk.setPower(sk.getPower() - 34.0F);
            } else {
               this.getSpellEffects().removeSpellEffect(sk);
            }
         }
      }
   }

   public final void removeTrueStrike() {
      if (this.getSpellEffects() != null) {
         SpellEffect sk = this.getSpellEffects().getSpellEffect((byte)67);
         if (sk != null) {
            this.getSpellEffects().removeSpellEffect(sk);
         }
      }
   }

   public final boolean addWoundOfType(
      @Nullable Creature attacker,
      byte woundType,
      int pos,
      boolean randomizePos,
      float armourMod,
      boolean calculateArmour,
      double damage,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if ((woundType == 8 || woundType == 4 || woundType == 10) && this.getCultist() != null && this.getCultist().hasNoElementalDamage()) {
         return false;
      } else {
         if (this.hasSpellEffect((byte)69)) {
            damage *= 0.8F;
         }

         try {
            if (randomizePos) {
               pos = this.getBody().getRandomWoundPos();
            }

            if (calculateArmour) {
               armourMod = this.getArmourMod();
               if (armourMod == 1.0F || this.isVehicle() || this.isKingdomGuard()) {
                  try {
                     byte protectionSlot = ArmourTemplate.getArmourPosition((byte)pos);
                     Item armour = this.getArmour(protectionSlot);
                     if (!this.isKingdomGuard()) {
                        armourMod = ArmourTemplate.calculateDR(armour, woundType);
                     } else {
                        armourMod *= ArmourTemplate.calculateDR(armour, woundType);
                     }

                     armour.setDamage(
                        (float)(
                           (double)armour.getDamage()
                              + damage
                                 * (double)armourMod
                                 / 30000.0
                                 * (double)armour.getDamageModifier()
                                 * (double)ArmourTemplate.getArmourDamageModFor(armour, woundType)
                        )
                     );
                     if (this.getBonusForSpellEffect((byte)22) > 0.0F) {
                        if (armourMod >= 1.0F) {
                           armourMod = 0.2F + (1.0F - this.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
                        } else {
                           armourMod = Math.min(armourMod, 0.2F + (1.0F - this.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
                        }
                     }
                  } catch (NoArmourException var15) {
                  }
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
               return CombatEngine.addWound(
                  attacker, this, woundType, pos, damage, armourMod, "poison", null, infection, poison, false, true, noMinimumDamage, spell
               );
            }

            return CombatEngine.addWound(
               attacker, this, woundType, pos, damage, armourMod, "hit", null, infection, poison, false, true, noMinimumDamage, spell
            );
         } catch (NoSpaceException var16) {
            logger.log(Level.WARNING, this.getName() + " no armour space on loc " + pos);
         } catch (Exception var17) {
            logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
         }

         return false;
      }
   }

   public float addSpellResistance(short spellId) {
      return 1.0F;
   }

   public SpellResistance getSpellResistance(short spellId) {
      return null;
   }

   public final boolean isInPvPZone() {
      return this.isInNonPvPZone ? false : this.isInPvPZone;
   }

   public final boolean isOnPvPServer() {
      if (this.isInNonPvPZone) {
         return false;
      } else if (Servers.localServer.PVPSERVER) {
         return true;
      } else if (this.isInPvPZone) {
         return true;
      } else {
         return this.isInDuelRing;
      }
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
      if (this.getBonusForSpellEffect((byte)22) > 70.0F) {
         return 2;
      } else {
         return (byte)(this.getBonusForSpellEffect((byte)22) > 0.0F ? 1 : 0);
      }
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
      return !this.isSpellCaster() && !this.isSummoner() ? 0 : 10000;
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
            float newPosz = Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()) + (float)(floorLevel * 3) + 0.25F;
            float diffz = newPosz - oldposz;
            this.getStatus().setPositionZ(newPosz, true);
            if (this.currentTile != null && this.getVisionArea() != null) {
               this.moved(0.0F, 0.0F, diffz, 0, 0);
            }
         }
      } catch (NoSuchZoneException var5) {
      }
   }

   public final float calculatePosZ() {
      return Zones.calculatePosZ(
         this.getPosX(), this.getPosY(), this.getCurrentTile(), this.isOnSurface(), this.isFloating(), this.getPositionZ(), this, this.getBridgeId()
      );
   }

   public final boolean canOpenDoors() {
      return this.template.canOpenDoors();
   }

   public final int getFloorLevel(boolean ignoreVehicleOffset) {
      try {
         float vehicleOffsetToRemove = 0.0F;
         if (ignoreVehicleOffset) {
            long vehicleId = this.getVehicle();
            if (vehicleId != -10L) {
               Vehicle vehicle = Vehicles.getVehicleForId(vehicleId);
               if (vehicle == null) {
                  logger.log(Level.WARNING, "Unknown vehicle for id: " + vehicleId + " resulting in possinly incorrect floor level!");
               } else {
                  Seat seat = vehicle.getSeatFor(this.id);
                  if (seat == null) {
                     logger.log(
                        Level.WARNING,
                        "Unable to find the seat the player: "
                           + this.id
                           + " supposedly is on, Vehicle id: "
                           + vehicleId
                           + ". Resulting in possibly incorrect floor level calculation."
                     );
                  } else {
                     vehicleOffsetToRemove = Math.max(this.getAltOffZ(), seat.offz);
                  }
               }
            }
         }

         float playerPosZ = this.getPositionZ() + this.getAltOffZ();
         float groundHeight = Math.max(0.0F, Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()));
         float posZ = Math.max(0.0F, (playerPosZ - groundHeight - vehicleOffsetToRemove + 0.5F) * 10.0F);
         return (int)posZ / 30;
      } catch (NoSuchZoneException var7) {
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
      for(Set<MovementEntity> set : illusions.values()) {
         for(MovementEntity entity : set) {
            if (entity.getWurmid() == illusionId) {
               return entity.getCreatorId();
            }
         }
      }

      return -10L;
   }

   public void addIllusion(MovementEntity entity) {
      Set<MovementEntity> entities = illusions.get(this.getWurmId());
      if (entities == null) {
         entities = new HashSet<>();
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

      for(FocusZone fz : FocusZone.getZonesAt(this.getTileX(), this.getTileY())) {
         if (fz.isBattleCamp()) {
            for(Item wartarget : Items.getWarTargets()) {
               if (closest == null
                  || getRange(this, (double)wartarget.getPosX(), (double)wartarget.getPosY())
                     < getRange(this, (double)closest.getPosX(), (double)closest.getPosY())) {
                  closest = wartarget;
               }
            }
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
      if (!this.isInvulnerable()
         && !this.isGhost()
         && !this.isUnique()
         && (this.getDeity() == null || !this.getDeity().isMountainGod() || this.getFaith() < 35.0F)
         && this.getFarwalkerSeconds() <= 0) {
         Wound wound = null;
         boolean dead = false;

         try {
            byte pos = this.getBody().getRandomWoundPos((byte)10);
            if (Server.rand.nextInt(10) <= 6 && this.getBody().getWounds() != null) {
               wound = this.getBody().getWounds().getWoundAtLocation(pos);
               if (wound != null) {
                  dead = wound.modifySeverity((int)(5000.0F + (float)Server.rand.nextInt(5000) * (100.0F - this.getSpellDamageProtectBonus()) / 100.0F));
                  wound.setBandaged(false);
                  this.setWounded();
               }
            }

            if (wound == null && !this.isGhost() && !this.isUnique() && !this.isKingdomGuard()) {
               dead = this.addWoundOfType(
                  null,
                  (byte)4,
                  pos,
                  false,
                  1.0F,
                  true,
                  (double)(5000.0F + (float)Server.rand.nextInt(5000) * (100.0F - this.getSpellDamageProtectBonus()) / 100.0F),
                  0.0F,
                  0.0F,
                  false,
                  false
               );
            }

            this.getCommunicator().sendAlertServerMessage("You are burnt by lava!");
            if (dead) {
               this.achievement(142);
               return true;
            }
         } catch (Exception var4) {
            logger.log(Level.WARNING, this.getName() + " " + var4.getMessage(), (Throwable)var4);
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
      return 0.0F;
   }

   public void recalcLimitingFactor(Item currentItem) {
   }

   public final float getAltOffZ() {
      if (this.getVehicle() != -10L) {
         Vehicle vehic = Vehicles.getVehicleForId(this.getVehicle());
         if (vehic != null) {
            Seat s = vehic.getSeatFor(this.getWurmId());
            if (s != null) {
               return s.getAltOffz();
            }
         }
      }

      return 0.0F;
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
      return this.isWagoner() ? Wagoner.getWagoner(this.id) : null;
   }

   @Override
   public String getTypeName() {
      return this.getTemplate().getName();
   }

   @Override
   public String getObjectName() {
      return this.isWagoner() ? this.getName() : this.petName;
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
         } else {
            return false;
         }
      } else {
         Village bVill = this.getBrandVillage();
         return bVill != null && bVill.isMayor(playerId);
      }
   }

   @Override
   public boolean canChangeOwner(Creature creature) {
      return false;
   }

   @Override
   public boolean canChangeName(Creature creature) {
      if (this.isWagoner()) {
         return false;
      } else if (creature.getPower() > 1) {
         return true;
      } else {
         Village bVill = this.getBrandVillage();
         return bVill == null ? false : bVill.isMayor(creature);
      }
   }

   @Override
   public boolean setNewOwner(long playerId) {
      if (this.isWagoner()) {
         Wagoner wagoner = this.getWagoner();
         if (wagoner != null) {
            wagoner.setOwnerId(playerId);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public String getOwnerName() {
      return "";
   }

   @Override
   public String getWarning() {
      if (this.isWagoner()) {
         return "";
      } else {
         Village bVill = this.getBrandVillage();
         return bVill == null ? "NEEDS TO BE BRANDED FOR PERMISSIONS TO WORK" : "";
      }
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
      } else {
         Village bVill = this.getBrandVillage();
         return bVill != null ? "Settlement \"" + bVill.getName() + "\" may manage" : "";
      }
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
      String sName = "";
      Village bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
      if (bVill != null) {
         sName = bVill.getName();
      }

      return sName.length() > 0 ? "Citizens of \"" + sName + "\"" : "";
   }

   @Override
   public String getAllianceName() {
      String aName = "";
      Village bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
      if (bVill != null) {
         aName = bVill.getAllianceName();
      }

      return aName.length() > 0 ? "Alliance of \"" + aName + "\"" : "";
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
      return bVill != null ? "Brand Permission of \"" + bVill.getName() + "\"" : "";
   }

   @Override
   public boolean isCitizen(Creature creature) {
      Village bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
      return bVill != null ? bVill.isCitizen(creature) : false;
   }

   @Override
   public boolean isAllied(Creature creature) {
      Village bVill = this.isWagoner() ? this.citizenVillage : this.getBrandVillage();
      return bVill != null ? bVill.isAlly(creature) : false;
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
      return AnimalSettings.isGuest(this, playerId);
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
            return Villages.getVillage((int)brand.getBrandId());
         } catch (NoSuchVillageException var3) {
            brand.deleteBrand();
         }
      }

      return null;
   }

   @Override
   public final boolean canHavePermissions() {
      if (this.isWagoner() && Features.Feature.WAGONER.isEnabled()) {
         return true;
      } else {
         return this.getBrandVillage() != null;
      }
   }

   public final boolean mayLead(Creature creature) {
      if (this.mayCommand(creature)) {
         return true;
      } else if (AnimalSettings.isExcluded(this, creature)) {
         return false;
      } else {
         Village bvill = this.getBrandVillage();
         if (bvill != null) {
            VillageRole vr = bvill.getRoleFor(creature);
            return vr.mayLead();
         } else {
            Village cvill = this.getCurrentVillage();
            if (cvill != null) {
               VillageRole vr = cvill.getRoleFor(creature);
               return vr.mayLead();
            } else {
               return true;
            }
         }
      }
   }

   @Override
   public final boolean mayShowPermissions(Creature creature) {
      return this.canHavePermissions() && this.mayManage(creature);
   }

   public final boolean canManage(Creature creature) {
      if (this.isWagoner()) {
         Wagoner wagoner = this.getWagoner();
         if (wagoner != null) {
            if (wagoner.getOwnerId() == creature.getWurmId()) {
               return true;
            }

            if (creature.getCitizenVillage() != null && creature.getCitizenVillage() == this.citizenVillage && creature.getCitizenVillage().isMayor(creature)) {
               return true;
            }
         }
      }

      if (AnimalSettings.isExcluded(this, creature)) {
         return false;
      } else {
         Village vill = this.getBrandVillage();
         if (AnimalSettings.canManage(this, creature, vill)) {
            return true;
         } else if (creature.getCitizenVillage() == null) {
            return false;
         } else if (vill == null) {
            return false;
         } else {
            return !vill.isCitizen(creature) ? false : vill.isActionAllowed((short)663, creature);
         }
      }
   }

   public final boolean mayManage(Creature creature) {
      return creature.getPower() > 1 && !this.isPlayer() ? true : this.canManage(creature);
   }

   public final boolean maySeeHistory(Creature creature) {
      if (this.isWagoner()) {
         Wagoner wagoner = this.getWagoner();
         if (wagoner != null) {
            if (wagoner.getOwnerId() == creature.getWurmId()) {
               return true;
            }

            if (creature.getCitizenVillage() != null && creature.getCitizenVillage() == this.citizenVillage && creature.getCitizenVillage().isMayor(creature)) {
               return true;
            }
         }
      }

      if (creature.getPower() > 1 && !this.isPlayer()) {
         return true;
      } else {
         Village bVill = this.getBrandVillage();
         return bVill != null && bVill.isMayor(creature);
      }
   }

   public final boolean mayCommand(Creature creature) {
      return AnimalSettings.isExcluded(this, creature) ? false : AnimalSettings.mayCommand(this, creature, this.getBrandVillage());
   }

   public final boolean mayPassenger(Creature creature) {
      return AnimalSettings.isExcluded(this, creature) ? false : AnimalSettings.mayPassenger(this, creature, this.getBrandVillage());
   }

   public final boolean mayAccessHold(Creature creature) {
      return AnimalSettings.isExcluded(this, creature) ? false : AnimalSettings.mayAccessHold(this, creature, this.getBrandVillage());
   }

   public final boolean mayUse(Creature creature) {
      return AnimalSettings.isExcluded(this, creature) ? false : AnimalSettings.mayUse(this, creature, this.getBrandVillage());
   }

   public final boolean publicMayUse(Creature creature) {
      return AnimalSettings.isExcluded(this, creature) ? false : AnimalSettings.publicMayUse(this);
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
      return this.getCitizenVillage() != null ? this.getCitizenVillage().getId() : 0;
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
            Item newItem = ItemFactory.createItem(pp, (float)itq, (byte)0, recipe.getLootableRarity(), null);
            newItem.setInscription(recipe, sig, 1550103);
            return newItem;
         } catch (FailedException var11) {
            logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         } catch (NoSuchTemplateException var12) {
            logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
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

            for(int x = 0; x < seats.length; ++x) {
               if (seats[x].getType() == type && !seats[x].isOccupied()) {
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
      } catch (NoSuchItemException var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
      }

      return false;
   }

   public ArrayList<Effect> getEffects() {
      return this.effects;
   }

   public void addEffect(Effect e) {
      if (e != null) {
         if (this.effects == null) {
            this.effects = new ArrayList<>();
         }

         this.effects.add(e);
      }
   }

   public void removeEffect(Effect e) {
      if (this.effects != null && e != null) {
         this.effects.remove(e);
         if (this.effects.isEmpty()) {
            this.effects = null;
         }
      }
   }

   public void updateEffects() {
      if (this.effects != null) {
         for(Effect e : this.effects) {
            e.setPosXYZ(this.getPosX(), this.getPosY(), this.getPositionZ(), false);
         }
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
      if (this.placementItem != null) {
         this.pendingPlacement = new float[]{
            this.placementItem.getPosX(),
            this.placementItem.getPosY(),
            this.placementItem.getPosZ(),
            this.placementItem.getRotation(),
            xPos,
            yPos,
            zPos,
            Math.abs(rot - this.placementItem.getRotation()) > 180.0F ? rot - 360.0F : rot
         };
      } else {
         this.pendingPlacement = null;
      }
   }

   public float[] getPendingPlacement() {
      return this.pendingPlacement;
   }

   public boolean canUseWithEquipment() {
      for(Item subjectItem : this.getBody().getContainersAndWornItems()) {
         if (subjectItem.isCreatureWearableOnly()) {
            if (subjectItem.isSaddleLarge()) {
               if (this.getSize() <= 4) {
                  return false;
               }

               if (this.isKingdomGuard()) {
                  return false;
               }
            } else if (subjectItem.isSaddleNormal()) {
               if (this.getSize() > 4) {
                  return false;
               }

               if (this.isKingdomGuard()) {
                  return false;
               }
            } else if (subjectItem.isHorseShoe()) {
               if (!this.isHorse()
                  && (!this.isUnicorn() || subjectItem.getMaterial() != 7 && subjectItem.getMaterial() != 8 && subjectItem.getMaterial() != 96)) {
                  return false;
               }
            } else if (subjectItem.isBarding() && !this.isHorse()) {
               return false;
            }
         }
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
   }
}
