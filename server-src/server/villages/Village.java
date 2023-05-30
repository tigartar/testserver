package com.wurmonline.server.villages;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Group;
import com.wurmonline.server.Groups;
import com.wurmonline.server.HistoryEvent;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.Twit;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.VillageTeleportQuestion;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Village implements MiscConstants, VillageStatus, TimeConstants, CounterTypes, MonetaryConstants, Comparable<Village> {
   public static final int MINIMUM_PERIMETER = 5;
   public static final int ATTACK_PERIMETER = 2;
   public static final byte SPAWN_VILLAGE_ALLIES = 0;
   public static final byte SPAWN_KINGDOM = 1;
   public static final int BADREPUTATION = 50;
   public static final int MAXBADREPUTATION = 150;
   private final Set<Item> oilBarrels = new HashSet<>();
   byte spawnSituation = 0;
   private boolean alerted = false;
   public int startx;
   public int endx;
   public int starty;
   public int endy;
   private static final Logger logger = Logger.getLogger(Village.class.getName());
   String name;
   final String founderName;
   int perimeterTiles = 0;
   public String mayorName;
   public int id = -10;
   final long creationDate;
   public final Map<Long, Citizen> citizens;
   public long deedid;
   long upkeep;
   final boolean surfaced;
   final Map<Integer, VillageRole> roles;
   boolean democracy = true;
   String motto = "A settlement just like any other!";
   protected final Group group;
   private final Set<FenceGate> gates;
   private final Set<MineDoorPermission> mineDoors;
   long tokenId = -10L;
   public final Map<Long, Guard> guards = new HashMap<>();
   private static final int maxGuardsOnThisServer = Servers.localServer.isChallengeOrEpicServer() ? 4 : 4;
   public long disband = 0L;
   public long disbander = -10L;
   private static final long disbandTime = 86400000L;
   final Map<Long, Reputation> reputations = new HashMap<>();
   public Set<Long> targets = new HashSet<>();
   private Set<MapAnnotation> villageMapAnnotations = new HashSet<>();
   private Set<VillageRecruitee> recruitees = new HashSet<>();
   public static final int REPUTATION_CRIMINAL = -30;
   long lastLogin = 0L;
   private Map<Village, VillageWar> wars;
   public Map<Village, WarDeclaration> warDeclarations;
   private long lastPolledReps = System.currentTimeMillis();
   public byte kingdom;
   public GuardPlan plan;
   Permissions settings = new Permissions();
   public boolean unlimitedCitizens = false;
   public long lastChangedName = 0L;
   boolean acceptsMerchants = false;
   LinkedList<HistoryEvent> history = new LinkedList<>();
   int maxCitizens = 0;
   public final boolean isPermanent;
   final byte spawnKingdom;
   private static boolean freeDisbands = false;
   private static final String upkeepString = "upkeep";
   boolean allowsAggCreatures = false;
   String consumerKeyToUse = "";
   String consumerSecretToUse = "";
   String applicationToken = "";
   String applicationSecret = "";
   boolean twitChat = false;
   private boolean canTwit = false;
   boolean twitEnabled = true;
   float faithWar = 0.0F;
   float faithHeal = 0.0F;
   float faithCreate = 0.0F;
   float faithDivideVal = 1.0F;
   int allianceNumber = 0;
   short hotaWins = 0;
   protected String motd = "";
   static final Village[] emptyVillages = new Village[0];
   int villageReputation = 0;
   VillageRole everybody = null;
   public long pmkKickDate = 0L;
   private short[] outsideSpawn;
   public final Map<Long, Wagoner> wagoners = new ConcurrentHashMap<>();
   long lastSentPmkWarning = 0L;
   boolean detectedBunny = false;
   public static final float OPTIMUMCRETRATIO = Servers.localServer.PVPSERVER ? 5.0F : 15.0F;
   public static final float OFFDEEDCRETRATIO = 10.0F;

   Village(
      int aStartX,
      int aEndX,
      int aStartY,
      int aEndY,
      String aName,
      Creature aFounder,
      long aDeedId,
      boolean aSurfaced,
      boolean aDemocracy,
      String aMotto,
      boolean aPermanent,
      byte aSpawnKingdom,
      int initialPerimeter
   ) throws NoSuchCreatureException, NoSuchPlayerException, IOException {
      this.citizens = new HashMap<>();
      this.group = new Group(aName);
      Groups.addGroup(this.group);
      this.roles = new HashMap<>();
      this.startx = aStartX;
      this.endx = aEndX;
      this.starty = aStartY;
      this.endy = aEndY;
      this.name = aName;
      this.founderName = aFounder.getName();
      this.kingdom = aFounder.getKingdomId();
      Kingdom k = Kingdoms.getKingdom(this.kingdom);
      if (k != null) {
         k.setExistsHere(true);
      }

      this.mayorName = this.founderName;
      this.creationDate = System.currentTimeMillis();
      this.lastLogin = this.creationDate;
      this.deedid = aDeedId;
      this.surfaced = aSurfaced;
      this.democracy = aDemocracy;
      this.motto = aMotto;
      this.isPermanent = aPermanent;
      this.spawnKingdom = aSpawnKingdom;
      this.perimeterTiles = initialPerimeter;
      this.id = this.create();
      this.gates = new HashSet<>();
      this.mineDoors = new HashSet<>();
      this.createRoles();
   }

   Village(
      int aId,
      int aStartX,
      int aEndX,
      int aStartY,
      int aEndY,
      String aName,
      String aFounderName,
      String aMayor,
      long aDeedId,
      boolean aSurfaced,
      boolean aDemocracy,
      String aDevise,
      long _creationDate,
      boolean aHomestead,
      long aTokenid,
      long aDisbandTime,
      long aDisbId,
      long aLast,
      byte aKingdom,
      long aUpkeep,
      byte aSettings,
      boolean aAcceptsHomes,
      boolean aAcceptsMerchants,
      int aMaxCitizens,
      boolean aPermanent,
      byte aSpawnkingdom,
      int perimetert,
      boolean allowsAggro,
      String _consumerKeyToUse,
      String _consumerSecretToUse,
      String _applicationToken,
      String _applicationSecret,
      boolean _twitChat,
      boolean _twitEnabled,
      float _faithWar,
      float _faithHeal,
      float _faithCreate,
      byte _spawnSituation,
      int _allianceNumber,
      short _hotaWins,
      long lastChangeName,
      String _motd
   ) {
      this.citizens = new HashMap<>();
      this.group = new Group(aName);
      Groups.addGroup(this.group);
      this.roles = new HashMap<>();
      this.startx = aStartX;
      this.endx = aEndX;
      this.starty = aStartY;
      this.endy = aEndY;
      this.name = aName;
      this.founderName = aFounderName;
      this.mayorName = aMayor;
      this.deedid = aDeedId;
      this.surfaced = aSurfaced;
      this.id = aId;
      this.democracy = aDemocracy;
      this.motto = aDevise;
      this.tokenId = aTokenid;
      this.kingdom = aKingdom;
      Kingdom k = Kingdoms.getKingdom(this.kingdom);
      if (k != null) {
         k.setExistsHere(true);
      }

      this.gates = new HashSet<>();
      this.mineDoors = new HashSet<>();
      this.disband = aDisbandTime;
      this.disbander = aDisbId;
      this.lastLogin = aLast;
      this.upkeep = aUpkeep;
      this.settings.setPermissionBits(aSettings & 255);
      this.unlimitedCitizens = aAcceptsHomes;
      this.acceptsMerchants = aAcceptsMerchants;
      this.maxCitizens = aMaxCitizens;
      this.isPermanent = aPermanent;
      this.spawnKingdom = aSpawnkingdom;
      this.creationDate = _creationDate;
      this.perimeterTiles = perimetert;
      this.allowsAggCreatures = allowsAggro;
      this.consumerKeyToUse = _consumerKeyToUse;
      this.consumerSecretToUse = _consumerSecretToUse;
      this.applicationToken = _applicationToken;
      this.applicationSecret = _applicationSecret;
      this.twitChat = _twitChat;
      this.twitEnabled = _twitEnabled;
      this.faithWar = _faithWar;
      this.faithHeal = _faithHeal;
      this.faithCreate = _faithCreate;
      this.spawnSituation = _spawnSituation;
      this.allianceNumber = _allianceNumber;
      this.hotaWins = _hotaWins;
      this.lastChangedName = lastChangeName;
      this.motd = _motd;
      this.canTwit();
      if (!Features.Feature.HIGHWAYS.isEnabled()) {
         try {
            if (this.settings.getPermissions() != 0) {
               this.settings.setPermissionBits(0);
               this.saveSettings();
            }
         } catch (IOException var53) {
            logger.log(Level.WARNING, var53.getMessage(), (Throwable)var53);
         }
      }
   }

   public boolean canTwit() {
      this.canTwit = false;
      if (this.consumerKeyToUse != null
         && this.consumerKeyToUse.length() > 5
         && this.consumerSecretToUse != null
         && this.consumerSecretToUse.length() > 5
         && this.applicationToken != null
         && this.applicationToken.length() > 5
         && this.applicationSecret != null
         && this.applicationSecret.length() > 5) {
         this.canTwit = true;
      }

      return this.canTwit;
   }

   final void createInitialUpkeepPlan() {
      this.plan = new DbGuardPlan(0, this.id);
   }

   final void initialize() {
      Zone[] coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);

      for(int x = 0; x < coveredZones.length; ++x) {
         coveredZones[x].addVillage(this);
      }

      coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);

      for(int x = 0; x < coveredZones.length; ++x) {
         coveredZones[x].addVillage(this);
      }

      this.setKingdomInfluence();
      if (Features.Feature.TOWER_CHAINING.isEnabled()) {
         try {
            InfluenceChain.addTokenToChain(this.kingdom, this.getToken());
         } catch (NoSuchItemException var3) {
            logger.warning(String.format("Village Initialize Error: No token found for village %s.", this.getName()));
         }
      }

      this.outsideSpawn = this.calcOutsideSpawn();
   }

   public final void setKingdomInfluence() {
      for(int x = this.startx - 5 - this.perimeterTiles; x < this.endx + 5 + this.perimeterTiles; ++x) {
         for(int y = this.starty - 5 - this.perimeterTiles; y < this.endy + 5 + this.perimeterTiles; ++y) {
            Zones.setKingdom(x, y, this.kingdom);
         }
      }
   }

   public final double getSkillModifier() {
      long timeSinceCreated = System.currentTimeMillis() - this.creationDate;
      if (timeSinceCreated > 174182400000L) {
         return 4.0;
      } else if (timeSinceCreated > 116121600000L) {
         return 3.0;
      } else if ((double)timeSinceCreated > 1.016064E11) {
         return 2.75;
      } else if (timeSinceCreated > 87091200000L) {
         return 2.5;
      } else if ((double)timeSinceCreated > 7.2576E1) {
         return 2.25;
      } else if (timeSinceCreated > 58060800000L) {
         return 2.0;
      } else if ((double)timeSinceCreated > 4.35456E1) {
         return 1.75;
      } else if (timeSinceCreated > 29030400000L) {
         return 1.5;
      } else if (timeSinceCreated > 21772800000L) {
         return 1.0;
      } else if (timeSinceCreated > 14515200000L) {
         return 0.5;
      } else {
         return timeSinceCreated > 7257600000L ? 0.25 : 0.1;
      }
   }

   private void createRoles() {
      this.createRoleEverybody();
      this.createRoleCitizen();
      this.createRoleMayor();
   }

   public final void checkIfRaiseAlert(Creature creature) {
      if (creature.getPower() <= 0 && this.isEnemy(creature)) {
         this.addTarget(creature);
      }
   }

   public final boolean acceptsNewCitizens() {
      if (this.unlimitedCitizens) {
         return true;
      } else {
         int g = 0;
         if (this.guards != null) {
            g = this.guards.size();
         }

         return this.getMaxCitizens() > this.citizens.size() - g;
      }
   }

   public boolean hasToomanyCitizens() {
      int g = 0;
      if (this.guards != null) {
         g = this.guards.size();
      }

      return this.getMaxCitizens() < this.citizens.size() - g;
   }

   final void checkForEnemies() {
      if (this.guards.size() > 0) {
         for(int x = this.startx; x <= this.endx; ++x) {
            for(int y = this.starty; y <= this.endy; ++y) {
               this.checkForEnemiesOn(x, y, true);
               this.checkForEnemiesOn(x, y, false);
            }
         }
      }
   }

   private void checkForEnemiesOn(int x, int y, boolean onSurface) {
      VolaTile tile = Zones.getTileOrNull(x, y, onSurface);
      if (tile != null) {
         Creature[] creatures = tile.getCreatures();

         for(int c = 0; c < creatures.length; ++c) {
            if (this.isEnemy(creatures[c])) {
               this.addTarget(creatures[c]);
            }
         }
      }
   }

   public final boolean isEnemy(Creature creature) {
      return this.isEnemy(creature, false);
   }

   public final boolean isEnemy(Creature creature, boolean ignoreInvulnerable) {
      if ((!creature.isInvulnerable() || ignoreInvulnerable) && !creature.isUnique()) {
         if (creature.getKingdomId() != 0 && !creature.isFriendlyKingdom(this.kingdom)) {
            return true;
         } else {
            if (creature.isDominated() && creature.getDominator() != null) {
               if (this.isEnemy(creature.getDominator().citizenVillage)) {
                  return true;
               }

               Reputation rep = this.reputations.get(new Long(creature.dominator));
               if (rep != null && rep.getValue() <= -30 && creature.getCurrentTile() != null && creature.getCurrentTile().getVillage() == this) {
                  return true;
               }
            }

            if (!creature.isPlayer()) {
               if (creature.isAggHuman()) {
                  if (!creature.isFriendlyKingdom(this.kingdom)) {
                     return !this.allowsAggCreatures();
                  }

                  return false;
               }

               if (creature.getTemplate().isFromValrei && creature.getKingdomId() == 0) {
                  return !this.allowsAggCreatures();
               }
            }

            if (this.isEnemy(creature.getCitizenVillage())) {
               return true;
            } else {
               return this.getReputation(creature) <= -30 && this.isWithinMinimumPerimeter(creature.getTileX(), creature.getTileY());
            }
         }
      } else {
         return false;
      }
   }

   public final void addTarget(Creature creature) {
      if (!creature.isInvulnerable() && !creature.isUnique()) {
         if (creature.getCultist() == null || !creature.getCultist().hasFearEffect()) {
            if (!creature.isTransferring()) {
               if (this.guards.size() > 0) {
                  if (!this.isAlerted()) {
                     this.setAlerted(true);
                     this.broadCastAlert(creature.getName() + " raises the settlement alarm!", (byte)4);

                     try {
                        if (this.gates != null && this.gates.size() > 0) {
                           Server.getInstance()
                              .broadCastMessage(
                                 "A horn sounds and the gates are locked. " + this.getName() + " is put on alert!",
                                 this.getToken().getTileX(),
                                 this.getToken().getTileY(),
                                 this.isOnSurface(),
                                 this.endx - this.startx
                              );
                        } else {
                           Server.getInstance()
                              .broadCastMessage(
                                 "A horn sounds. " + this.getName() + " is put on alert!",
                                 this.getToken().getTileX(),
                                 this.getToken().getTileY(),
                                 this.isOnSurface(),
                                 this.endx - this.startx
                              );
                        }
                     } catch (NoSuchItemException var3) {
                        logger.log(Level.WARNING, "No settlement token for " + this.getName() + ": " + this.tokenId, (Throwable)var3);
                     }
                  }

                  if (!this.targets.contains(new Long(creature.getWurmId()))) {
                     this.targets.add(new Long(creature.getWurmId()));
                  }

                  this.assignTargets();
               }
            }
         }
      }
   }

   public final void assignTargets() {
      if (this.guards.size() > 0 && this.targets.size() > 0) {
         LinkedList<Guard> g = new LinkedList<>();
         g.addAll(this.guards.values());
         Long[] targs = this.getTargets();

         for(int x = 0; x < targs.length; ++x) {
            int guardsAssigned = 0;
            long targid = targs[x];
            Guard best = null;
            int bestdist = Integer.MAX_VALUE;
            Guard nextBest = null;
            int nextBestdist = Integer.MAX_VALUE;
            Guard thirdBest = null;
            int thirdBestdist = Integer.MAX_VALUE;
            if (!g.isEmpty()) {
               try {
                  Creature target = Server.getInstance().getCreature(targid);
                  if (target.isDead()) {
                     this.targets.remove(targid);
                  } else if (target.getCurrentTile().getTileX() >= this.getStartX() - 5
                     && target.getCurrentTile().getTileX() <= this.getEndX() + 5
                     && target.getCurrentTile().getTileY() >= this.getStartY() - 5
                     && target.getCurrentTile().getTileY() <= this.getEndY() + 5) {
                     ListIterator<Guard> it2 = g.listIterator();

                     while(it2.hasNext()) {
                        Guard guard = it2.next();
                        if (guard.creature.target == targid) {
                           ++guardsAssigned;
                           it2.remove();
                           if (guardsAssigned >= 3) {
                              break;
                           }
                        } else if (guard.creature.target == -10L) {
                           int diffx = (int)Math.abs(guard.creature.getPosX() - target.getPosX());
                           int diffy = (int)Math.abs(guard.creature.getPosY() - target.getPosY());
                           int dist = Math.max(diffx, diffy);
                           if (dist < bestdist) {
                              best = guard;
                              bestdist = dist;
                           } else if (dist < nextBestdist) {
                              nextBest = guard;
                              nextBestdist = dist;
                           } else if (dist < thirdBestdist) {
                              thirdBest = guard;
                              thirdBestdist = dist;
                           }
                        }
                     }

                     if (guardsAssigned < 3 && best != null) {
                        best.creature.setTarget(targid, false);
                        best.creature.say("I'll take care of " + target.getName() + "!");
                        g.remove(best);
                        if (++guardsAssigned < 3 && nextBest != null) {
                           nextBest.creature.setTarget(targid, false);
                           nextBest.creature.say("I'll help you with " + target.getName() + "!");
                           g.remove(nextBest);
                           ++guardsAssigned;
                        }

                        if (guardsAssigned < 3 && thirdBest != null) {
                           thirdBest.creature.setTarget(targid, false);
                           thirdBest.creature.say("I'll help you with " + target.getName() + "!");
                           g.remove(thirdBest);
                           ++guardsAssigned;
                        }
                     }
                  } else {
                     this.removeTarget(target.getWurmId(), false);
                  }
               } catch (NoSuchCreatureException var19) {
               } catch (NoSuchPlayerException var20) {
               }
            }
         }
      }
   }

   public final boolean isAlerted() {
      return this.alerted;
   }

   public final boolean isCapital() {
      King k = King.getKing(this.kingdom);
      return k != null ? k.capital.equalsIgnoreCase(this.getName()) : false;
   }

   public final void addBarrel(Item barrel) {
      this.oilBarrels.add(barrel);
   }

   public final void removeBarrel(Item barrel) {
      this.oilBarrels.remove(barrel);
   }

   public final int getOilAmount(int amount, boolean onDeed) {
      if (amount <= 0) {
         return 0;
      } else if (this.guards.size() == 0 && !onDeed) {
         return 0;
      } else if (this.isPermanent) {
         return 100;
      } else {
         for(Item i : this.oilBarrels) {
            if (!i.isEmpty(false)) {
               Item[] contained = i.getAllItems(false);

               for(Item liquid : contained) {
                  if (liquid.isLiquidInflammable()) {
                     if (amount >= liquid.getWeightGrams()) {
                        Items.destroyItem(liquid.getWurmId());
                        return liquid.getWeightGrams();
                     }

                     liquid.setWeight(liquid.getWeightGrams() - amount, true);
                     return amount;
                  }
               }
            }
         }

         return 0;
      }
   }

   private Long[] getTargets() {
      return this.targets.toArray(new Long[this.targets.size()]);
   }

   public final boolean containsTarget(Creature creature) {
      return this.targets.contains(new Long(creature.getWurmId()));
   }

   public final boolean containsItem(Item item) {
      return item.getZoneId() > 0
         && this.getStartX() <= item.getTileX()
         && this.getEndX() >= item.getTileX()
         && this.getStartY() <= item.getTileY()
         && this.getEndY() >= item.getTileY();
   }

   public final boolean isWithinMinimumPerimeter(int tilex, int tiley) {
      return this.getStartX() - 5 <= tilex && this.getEndX() + 5 >= tilex && this.getStartY() - 5 <= tiley && this.getEndY() + 5 >= tiley;
   }

   public final boolean isWithinAttackPerimeter(int tilex, int tiley) {
      return this.getStartX() - 2 <= tilex && this.getEndX() + 2 >= tilex && this.getStartY() - 2 <= tiley && this.getEndY() + 2 >= tiley;
   }

   public final boolean lessThanWeekLeft() {
      if (this.plan != null) {
         return this.plan.getTimeLeft() < 604800000L;
      } else {
         return true;
      }
   }

   public final boolean moreThanMonthLeft() {
      if (!this.isChained()) {
         return false;
      } else if (this.plan != null) {
         return this.plan.getTimeLeft() > 2419200000L;
      } else {
         return true;
      }
   }

   final void poll(long now, boolean reduceFaith) {
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Polling settlement: " + this);
      }

      boolean disb = this.plan.poll();
      if (disb) {
         this.disband = now - 1L;
         this.disband("upkeep");
      } else if (this.checkDisband(now)) {
         disb = true;
         String pname = "Unknown Player";

         try {
            pname = Players.getInstance().getNameFor(this.disbander);
         } catch (NoSuchPlayerException var15) {
            logger.log(Level.WARNING, "No name for " + this.disbander, (Throwable)var15);
         } catch (IOException var16) {
            logger.log(Level.WARNING, "No name for " + this.disbander, (Throwable)var16);
         }

         if (disb) {
            this.disband(pname);
         }
      } else {
         this.faithDivideVal = Math.max(1.0F, Math.min(3.0F, (float)(this.getCitizens().length - this.plan.getNumHiredGuards()) / 2.0F));
         if (reduceFaith) {
            this.setFaithCreate(Math.max(0.0F, this.faithCreate - Math.max(0.01F, this.faithCreate / 15.0F)));
            this.setFaithWar(Math.max(0.0F, this.faithWar - Math.max(0.01F, this.faithWar / 15.0F)));
            this.setFaithHeal(Math.max(0.0F, this.faithHeal - Math.max(0.01F, this.faithHeal / 15.0F)));
         }

         if (WurmCalendar.isEaster() && this.isPermanent && (!Servers.localServer.entryServer || Server.getInstance().isPS()) && !this.detectedBunny) {
            for(Citizen citiz : this.getCitizens()) {
               try {
                  Creature bunny = Creatures.getInstance().getCreature(citiz.getId());
                  if (bunny.getTemplate().getTemplateId() == 53) {
                     this.detectedBunny = true;
                  }
               } catch (NoSuchCreatureException var14) {
               }
            }

            if (!this.detectedBunny) {
               int tilex = this.getCenterX();
               int tiley = this.getCenterY();
               boolean ok = false;
               int tries = 0;

               while(!ok && tries++ < 100) {
                  switch(Server.rand.nextInt(4)) {
                     case 0:
                        tilex = this.getStartX() - (20 + Server.rand.nextInt(40));
                        break;
                     case 1:
                        tilex = this.getEndX() + 20 + Server.rand.nextInt(40);
                        break;
                     case 2:
                        tiley = this.getEndY() + 20 + Server.rand.nextInt(40);
                        break;
                     case 3:
                        tiley = this.getStartY() - (20 + Server.rand.nextInt(40));
                  }

                  VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
                  if ((t == null || t.getFences().length == 0 && t.getStructure() == null) && Tiles.decodeHeight(Zones.getTileIntForTile(tilex, tiley, 0)) > 0
                     )
                   {
                     ok = true;
                  }
               }

               try {
                  byte sex = 0;
                  if (Server.rand.nextBoolean()) {
                     sex = 1;
                  }

                  Creature bunny = Creature.doNew(
                     53,
                     true,
                     (float)(tilex * 4 + 2),
                     (float)(tiley * 4 + 2),
                     Server.rand.nextFloat() * 360.0F,
                     0,
                     "Easter Bunny",
                     sex,
                     (byte)0,
                     (byte)0,
                     false,
                     (byte)1
                  );
                  this.addCitizen(bunny, this.getRole(3));
                  logger.log(Level.INFO, "Created easter bunny for " + this.getName());
                  this.detectedBunny = true;
               } catch (Exception var13) {
                  logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
               }
            }
         }
      }

      if (this.isLeavingPmk() && System.currentTimeMillis() - this.lastSentPmkWarning > 1800000L) {
         Kingdom pmk = Kingdoms.getKingdom(this.kingdom);
         Kingdom template = Kingdoms.getKingdom(Servers.isThisAChaosServer() ? 4 : pmk.getTemplate());
         if (pmk == null || !pmk.isCustomKingdom()) {
            this.pmkKickDate = 0L;
         } else if (this.checkLeavePmk(System.currentTimeMillis())) {
            this.lastSentPmkWarning = 0L;
            this.addHistory(this.getName(), "converts to " + template.getName() + " from " + pmk.getName() + ".");
            this.broadCastAlert(this.getName() + " leaves " + pmk.getName() + " for " + template.getName() + ".", (byte)4);
            this.convertToKingdom(Servers.isThisAChaosServer() ? 4 : pmk.getTemplate(), false, false);
         } else {
            this.lastSentPmkWarning = System.currentTimeMillis();
            this.broadCastAlert(
               this.getName()
                  + " is leaving "
                  + pmk.getName()
                  + " for "
                  + template.getName()
                  + " in "
                  + Server.getTimeFor(this.pmkKickDate - System.currentTimeMillis())
                  + ".",
               (byte)4
            );
         }
      }

      if (this.targets.size() > 0) {
         Long[] targArr = this.getTargets();

         for(int x = 0; x < targArr.length; ++x) {
            try {
               Creature c = Server.getInstance().getCreature(targArr[x]);
               VolaTile t = c.getCurrentTile();
               if (t != null && t.getVillage() != this) {
                  this.targets.remove(targArr[x]);
               }
            } catch (NoSuchPlayerException var11) {
               this.targets.remove(targArr[x]);
            } catch (NoSuchCreatureException var12) {
               this.targets.remove(targArr[x]);
            }
         }

         if (this.targets.size() == 0) {
            this.setAlerted(false);
            this.broadCastSafe("The danger is over for now.");
         }
      }

      if (now - this.lastPolledReps > 7200000L) {
         if (this.getVillageReputation() > 0) {
            this.setVillageRep(this.getVillageReputation() - 1);
         }

         Long[] keys = this.reputations.keySet().toArray(new Long[this.reputations.keySet().size()]);

         for(int x = 0; x < keys.length; ++x) {
            Reputation r = this.reputations.get(keys[x]);
            int old = r.getValue();
            if (old < 0) {
               r.modify(1);
               int newr = r.getValue();
               if (newr >= 0) {
                  this.reputations.remove(keys[x]);
               }
            }
         }

         this.lastPolledReps = System.currentTimeMillis();
      }

      if (this.warDeclarations != null) {
         WarDeclaration[] declArr = this.warDeclarations.values().toArray(new WarDeclaration[this.warDeclarations.size()]);

         for(int x = 0; x < declArr.length; ++x) {
            if (now - declArr[x].time > 86400000L) {
               declArr[x].accept();
            }
         }
      }
   }

   public final void removeTarget(long target, boolean ignoreFighting) {
      this.targets.remove(new Long(target));
      Guard[] _guards = this.getGuards();
      boolean fighting = false;

      for(int x = 0; x < _guards.length; ++x) {
         if (_guards[x].creature.target == target) {
            if (_guards[x].creature.isFighting() && !ignoreFighting) {
               fighting = true;
            } else {
               if (_guards[x].creature.opponent != null && _guards[x].creature.opponent.getWurmId() == target) {
                  _guards[x].creature.opponent.setTarget(-10L, true);
                  _guards[x].creature.opponent.setOpponent(null);
                  _guards[x].creature.setOpponent(null);
               }

               _guards[x].creature.setTarget(-10L, true);
            }
         }
      }

      if (this.targets.size() == 0 && !fighting) {
         this.setAlerted(false);
      }
   }

   public final void removeTarget(Creature target) {
      this.targets.remove(new Long(target.getWurmId()));
      Guard[] _guards = this.getGuards();
      boolean fighting = false;

      for(int x = 0; x < _guards.length; ++x) {
         if (_guards[x].creature.target == target.getWurmId()) {
            if (!_guards[x].creature.isFighting()) {
               _guards[x].creature.setTarget(-10L, true);
            } else {
               fighting = true;
            }
         }
      }

      if (this.targets.size() == 0 && !fighting) {
         this.setAlerted(false);
      }
   }

   private void setAlerted(boolean alert) {
      int n = 0;
      if (alert) {
         for(FenceGate g : this.gates) {
            if (g.startAlert(n == 0)) {
               ++n;
            }
         }

         this.alerted = true;
         if (this.plan != null) {
            this.plan.startSiege();
         }
      } else if (this.alerted) {
         for(FenceGate g : this.gates) {
            if (g.endAlert(n == 0)) {
               ++n;
            }
         }

         this.alerted = false;
      }
   }

   protected VillageRole createRoleEverybody() {
      if (this.everybody == null) {
         try {
            Permissions settings = new Permissions();
            Permissions moreSettings = new Permissions();
            Permissions extraSettings = new Permissions();
            boolean atknon = Servers.localServer.PVPSERVER;
            if (atknon) {
               settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), true);
            }

            this.everybody = new DbVillageRole(
               this.id,
               "non-citizens",
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               atknon,
               false,
               false,
               (byte)1,
               0,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               false,
               -10L,
               settings.getPermissions(),
               moreSettings.getPermissions(),
               extraSettings.getPermissions()
            );
            this.roles.put(this.everybody.getId(), this.everybody);
         } catch (IOException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         }
      }

      return this.everybody;
   }

   public int getPerimeterSize() {
      return this.perimeterTiles;
   }

   public int getTotalPerimeterSize() {
      return this.perimeterTiles + 5;
   }

   private VillageRole createRoleGuard() {
      VillageRole role = null;

      try {
         Permissions settings = new Permissions();
         Permissions moreSettings = new Permissions();
         Permissions extraSettings = new Permissions();
         settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_CITIZENS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), true);
         role = new DbVillageRole(
            this.id,
            "guard",
            false,
            true,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            false,
            true,
            true,
            true,
            true,
            (byte)4,
            0,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            -10L,
            settings.getPermissions(),
            moreSettings.getPermissions(),
            extraSettings.getPermissions()
         );
         this.roles.put(role.getId(), role);
      } catch (IOException var5) {
         logger.log(Level.WARNING, "Failed to create role guard for settlement " + this.getName() + " " + var5.getMessage(), (Throwable)var5);
      }

      return role;
   }

   private VillageRole createRoleWagoner() {
      VillageRole role = null;

      try {
         Permissions settings = new Permissions();
         Permissions moreSettings = new Permissions();
         Permissions extraSettings = new Permissions();
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PICKUP.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.LOAD.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.UNLOAD.getBit(), true);
         role = new DbVillageRole(
            this.id,
            "wagoner",
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            (byte)6,
            0,
            false,
            false,
            false,
            true,
            false,
            true,
            false,
            false,
            false,
            -10L,
            settings.getPermissions(),
            moreSettings.getPermissions(),
            extraSettings.getPermissions()
         );
         this.roles.put(role.getId(), role);
      } catch (IOException var5) {
         logger.log(Level.WARNING, "Failed to create role wagoner for settlement " + this.getName() + " " + var5.getMessage(), (Throwable)var5);
      }

      return role;
   }

   private VillageRole createRoleCitizen() {
      VillageRole role = null;

      try {
         Permissions settings = new Permissions();
         Permissions moreSettings = new Permissions();
         Permissions extraSettings = new Permissions();
         settings.setPermissionBit(VillageRole.RolePermissions.BUTCHER.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.GROOM.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.LEAD.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.MILK_SHEAR.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.TAME.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.DIG_RESOURCE.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.SOW_FIELDS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.TEND_FIELDS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.CHOP_DOWN_OLD_TREES.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.CUT_GRASS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.HARVEST_FRUIT.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.MAKE_LAWN.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PICK_SPROUTS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PLANT_FLOWERS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PLANT_SPROUTS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PRUNE.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.FORAGE.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MEDITATION_ABILITY.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.IMPROVE_REPAIR.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PICKUP.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PULL_PUSH.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_IRON.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_OTHER.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_ROCK.getBit(), true);
         role = new DbVillageRole(
            this.id,
            "citizen",
            false,
            true,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            true,
            true,
            false,
            false,
            false,
            true,
            true,
            true,
            (byte)3,
            0,
            true,
            false,
            true,
            true,
            true,
            true,
            true,
            false,
            false,
            -10L,
            settings.getPermissions(),
            moreSettings.getPermissions(),
            extraSettings.getPermissions()
         );
         this.roles.put(role.getId(), role);
      } catch (IOException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return role;
   }

   private VillageRole createRoleMayor() {
      String title = "mayor";
      boolean mayinvite = true;
      VillageRole role = null;

      try {
         int permissions = -1;
         int morePermissions = -1;
         int extraPermissions = -1;
         role = new DbVillageRole(
            this.id,
            "mayor",
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            (byte)2,
            0,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            -10L,
            -1,
            -1,
            -1
         );
         this.roles.put(role.getId(), role);
      } catch (IOException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      }

      return role;
   }

   public final int getReputation(Creature creature) {
      if (creature.getKingdomId() != 0 && !creature.isFriendlyKingdom(this.kingdom)) {
         return -100;
      } else {
         long wid = creature.getWurmId();
         if (creature.getCitizenVillage() != null && this.isEnemy(creature.getCitizenVillage())) {
            return -100;
         } else {
            Reputation rep = this.reputations.get(new Long(wid));
            return rep != null ? rep.getValue() : 0;
         }
      }
   }

   public final int getReputation(long wid) {
      Village vill = Villages.getVillageForCreature(wid);
      if (vill != null && this.isEnemy(vill)) {
         return -100;
      } else {
         Reputation rep = this.reputations.get(new Long(wid));
         return rep != null ? rep.getValue() : 0;
      }
   }

   public final Reputation[] getReputations() {
      return this.reputations.values().toArray(new Reputation[this.reputations.values().size()]);
   }

   protected VillageRole createRoleAlly() {
      VillageRole role = null;

      try {
         Permissions settings = new Permissions();
         Permissions moreSettings = new Permissions();
         Permissions extraSettings = new Permissions();
         settings.setPermissionBit(VillageRole.RolePermissions.BUTCHER.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.GROOM.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.LEAD.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.MILK_SHEAR.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.TAME.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.DIG_RESOURCE.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.SOW_FIELDS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.TEND_FIELDS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.CHOP_DOWN_OLD_TREES.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.CUT_GRASS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.HARVEST_FRUIT.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.MAKE_LAWN.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PICK_SPROUTS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PLANT_FLOWERS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PLANT_SPROUTS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.PRUNE.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), true);
         settings.setPermissionBit(VillageRole.RolePermissions.FORAGE.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MEDITATION_ABILITY.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.IMPROVE_REPAIR.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PULL_PUSH.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_IRON.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_OTHER.getBit(), true);
         moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_ROCK.getBit(), true);
         role = new DbVillageRole(
            this.id,
            "ally",
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            true,
            false,
            (byte)5,
            0,
            false,
            false,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            -10L,
            settings.getPermissions(),
            moreSettings.getPermissions(),
            extraSettings.getPermissions()
         );
         this.roles.put(role.getId(), role);
      } catch (IOException var5) {
         logger.log(Level.WARNING, "Failed to create role allied for settlement " + this.getName() + " " + var5.getMessage(), (Throwable)var5);
      }

      return role;
   }

   public final void resetRoles() {
      Citizen mayor = this.getMayor();
      VillageRole[] roleArr = this.getRoles();
      Set<Citizen> guardSet = new HashSet<>();
      Set<Citizen> wagonerSet = new HashSet<>();
      Citizen[] citiz = this.getCitizens();

      for(int y = 0; y < citiz.length; ++y) {
         if (citiz[y].getRole().getStatus() == 4) {
            guardSet.add(citiz[y]);
         }

         if (citiz[y].getRole().getStatus() == 6) {
            wagonerSet.add(citiz[y]);
         }
      }

      for(int x = 0; x < roleArr.length; ++x) {
         try {
            roleArr[x].delete();
         } catch (IOException var17) {
            logger.log(Level.WARNING, this.getName() + " role: " + roleArr[x].getName() + " " + var17.getMessage(), (Throwable)var17);
         }
      }

      this.roles.clear();
      if (this.allianceNumber > 0) {
         this.createRoleAlly();
      }

      VillageRole citizRole = this.createRoleCitizen();
      this.everybody = null;
      this.createRoleEverybody();
      VillageRole mayorRole = this.createRoleMayor();
      VillageRole guardRole = null;
      VillageRole wagonerRole = null;
      if (guardSet.size() > 0) {
         guardRole = this.createRoleGuard();

         for(Citizen g : guardSet) {
            try {
               g.setRole(guardRole);
            } catch (IOException var16) {
               logger.log(Level.WARNING, this.getName(), (Throwable)var16);
            }
         }
      }

      if (wagonerSet.size() > 0) {
         wagonerRole = this.createRoleWagoner();

         for(Citizen g : wagonerSet) {
            try {
               g.setRole(wagonerRole);
            } catch (IOException var15) {
               logger.log(Level.WARNING, this.getName(), (Throwable)var15);
            }
         }
      }

      for(int y = 0; y < citiz.length; ++y) {
         if (citiz[y] != mayor) {
            try {
               boolean addRole = true;
               if (guardRole != null && citiz[y].getRole() == guardRole) {
                  addRole = false;
               }

               if (wagonerRole != null && citiz[y].getRole() == wagonerRole) {
                  addRole = false;
               }

               if (addRole) {
                  citiz[y].setRole(citizRole);
               }
            } catch (IOException var13) {
               logger.log(Level.WARNING, this.getName(), (Throwable)var13);
            }
         } else {
            try {
               citiz[y].setRole(mayorRole);
            } catch (IOException var14) {
               logger.log(Level.WARNING, this.getName(), (Throwable)var14);
            }
         }
      }
   }

   public final short[] getSpawnPoint() {
      int x = this.getCenterX();
      int y = this.getCenterY();
      int tries = 0;

      while(tries < 10) {
         ++tries;
         x = Server.rand.nextInt(this.endx - this.startx) + this.startx;
         y = Server.rand.nextInt(this.endy - this.starty) + this.starty;
         VolaTile tile = Zones.getTileOrNull(x, y, this.isOnSurface());
         if (tile == null) {
            return new short[]{(short)x, (short)y};
         }
      }

      x = this.getCenterX();
      y = this.getCenterY();

      try {
         Item token = this.getToken();
         x = (int)token.getPosX() >> 2;
         y = (int)token.getPosY() >> 2;
      } catch (NoSuchItemException var5) {
         logger.log(Level.WARNING, "No token found for settlement " + this.getName(), (Throwable)var5);
      }

      return new short[]{(short)x, (short)y};
   }

   public final short[] getTokenCoords() throws NoSuchItemException {
      Item token = this.getToken();
      int x = (int)token.getPosX() >> 2;
      int y = (int)token.getPosY() >> 2;
      return new short[]{(short)x, (short)y};
   }

   public final int getStartX() {
      return this.startx;
   }

   public final int getEndX() {
      return this.endx;
   }

   public final int getStartY() {
      return this.starty;
   }

   public final int getEndY() {
      return this.endy;
   }

   final int getCenterX() {
      return this.startx + (this.endx - this.startx) / 2;
   }

   final int getCenterY() {
      return this.starty + (this.endy - this.starty) / 2;
   }

   public final int getTokenX() {
      try {
         Item token = this.getToken();
         return (int)token.getPosX() >> 2;
      } catch (NoSuchItemException var2) {
         return this.startx + (this.endx - this.startx) / 2;
      }
   }

   public final int getTokenY() {
      try {
         Item token = this.getToken();
         return (int)token.getPosY() >> 2;
      } catch (NoSuchItemException var2) {
         return this.starty + (this.endy - this.starty) / 2;
      }
   }

   public final Set<FenceGate> getGates() {
      return this.gates;
   }

   public final void addGate(FenceGate gate) {
      if (!this.gates.contains(gate)) {
         this.gates.add(gate);
         gate.setVillage(this);

         try {
            Item lock = gate.getLock();
            lock.addKey(this.deedid);
         } catch (NoSuchLockException var3) {
         }
      }
   }

   public final Set<MineDoorPermission> getMineDoors() {
      return this.mineDoors;
   }

   public final void addMineDoor(MineDoorPermission mineDoor) {
      if (!this.mineDoors.contains(mineDoor)) {
         this.mineDoors.add(mineDoor);
         mineDoor.setVillage(this);
      }
   }

   public final boolean hasAllies() {
      return this.allianceNumber > 0;
   }

   public final boolean isEnemy(Village village) {
      if (village == null) {
         return false;
      } else if (village.kingdom != this.kingdom && !Kingdoms.getKingdom(this.kingdom).isAllied(village.kingdom)) {
         return true;
      } else if (this.wars != null) {
         return this.wars.get(village) != null;
      } else {
         return false;
      }
   }

   final void addWar(VillageWar war) {
      if (this.wars == null) {
         this.wars = new HashMap<>();
      }

      Village opponent = war.getVilltwo();
      if (opponent == this) {
         opponent = war.getVillone();
      }

      if (opponent != this && !this.isEnemy(opponent)) {
         this.wars.put(opponent, war);
         if (this.isAlly(opponent)) {
            if (this.allianceNumber == this.getId()) {
               opponent.setAllianceNumber(0);
            } else if (opponent.getId() == this.allianceNumber) {
               this.setAllianceNumber(0);
            }
         }
      }
   }

   public final boolean mayDeclareWarOn(Village village) {
      if (village == this) {
         return false;
      } else if (!Servers.localServer.PVPSERVER) {
         return false;
      } else if (village.kingdom != this.kingdom) {
         return false;
      } else if (village.isPermanent) {
         return false;
      } else {
         if (village.getVillageReputation() < 50 && !Servers.isThisAChaosServer()) {
            Kingdom kingd = Kingdoms.getKingdom(this.kingdom);
            if (!kingd.isCustomKingdom() || this.isCapital()) {
               return false;
            }
         }

         if (this.warDeclarations != null && this.warDeclarations.containsKey(village)) {
            return false;
         } else {
            return this.wars == null || !this.wars.containsKey(village);
         }
      }
   }

   public final boolean isAtPeaceWith(Village village) {
      if (!Servers.localServer.PVPSERVER) {
         return true;
      } else if (village.kingdom != this.kingdom) {
         return false;
      } else if (village.isPermanent) {
         return true;
      } else if (this.warDeclarations != null && this.warDeclarations.containsKey(village)) {
         return false;
      } else {
         return this.wars == null || !this.wars.containsKey(village);
      }
   }

   final void addWarDeclaration(WarDeclaration declaration) {
      if (this.warDeclarations == null) {
         this.warDeclarations = new HashMap<>();
      }

      boolean wedeclare = false;
      Village opponent = declaration.declarer;
      if (opponent == this) {
         wedeclare = true;
         opponent = declaration.receiver;
      }

      if (opponent != this) {
         if (this.warDeclarations.containsKey(opponent)) {
            return;
         }

         this.warDeclarations.put(opponent, declaration);
         if (this.isAlly(opponent)) {
            if (this.allianceNumber == this.getId()) {
               opponent.setAllianceNumber(0);
            } else if (opponent.getId() == this.allianceNumber) {
               this.setAllianceNumber(0);
            }

            if (wedeclare) {
               this.broadCastNormal(
                  "Under the rule of "
                     + this.getMayor().getName()
                     + ", "
                     + this.getName()
                     + " has declared war with the treacherous "
                     + opponent.getName()
                     + ". Citizens, be strong and brave!"
               );
            } else {
               this.broadCastNormal(
                  "Under the rule of "
                     + this.getMayor().getName()
                     + ", "
                     + this.getName()
                     + " has been challenged by the treacherous "
                     + opponent.getName()
                     + " and will be forced into war. Citizens, be strong and brave!"
               );
            }
         } else if (wedeclare) {
            this.broadCastNormal(
               "Under the rule of "
                  + this.getMayor().getName()
                  + ", "
                  + this.getName()
                  + " has declared war with the cowardly "
                  + opponent.getName()
                  + ". Citizens, be strong and brave!"
            );
         } else {
            this.broadCastNormal(
               "Under the rule of "
                  + this.getMayor().getName()
                  + ", "
                  + this.getName()
                  + " has been challenged by the cowardly "
                  + opponent.getName()
                  + ". Citizens, be strong and brave - war is coming our way!"
            );
         }

         this.addHistory(opponent.getName(), "is now under war declaration");
      } else {
         logger.log(
            Level.WARNING,
            "Added declaration to "
               + this.getName()
               + " but the war is for "
               + declaration.declarer.getName()
               + " and "
               + declaration.receiver.getName()
               + ". Deleting."
         );
         declaration.delete();
      }
   }

   final void startWar(VillageWar war, boolean wedeclare) {
      if (this.wars == null) {
         this.wars = new HashMap<>();
      }

      Village opponent = war.getVilltwo();
      if (opponent == this) {
         opponent = war.getVillone();
      }

      if (opponent != this) {
         if (!this.isEnemy(opponent)) {
            this.wars.put(opponent, war);
            if (this.isAlly(opponent)) {
               if (this.allianceNumber == this.getId()) {
                  opponent.setAllianceNumber(0);
               } else if (opponent.getId() == this.allianceNumber) {
                  this.setAllianceNumber(0);
               }

               if (wedeclare) {
                  this.broadCastNormal(
                     "Under the rule of "
                        + this.getMayor().getName()
                        + ", "
                        + this.getName()
                        + " has decided to go to war with the treacherous "
                        + opponent.getName()
                        + ". Citizens, be strong and brave!"
                  );
               } else {
                  this.broadCastNormal(
                     "Under the rule of "
                        + this.getMayor().getName()
                        + ", "
                        + this.getName()
                        + " was betrayed by the treacherous "
                        + opponent.getName()
                        + " and forced into war. Citizens, be strong and brave!"
                  );
               }
            } else if (wedeclare) {
               this.broadCastNormal(
                  "Under the rule of "
                     + this.getMayor().getName()
                     + ", "
                     + this.getName()
                     + " has decided to go to war with the cowardly "
                     + opponent.getName()
                     + ". Citizens, be strong and brave!"
               );
            } else {
               this.broadCastNormal(
                  "Under the rule of "
                     + this.getMayor().getName()
                     + ", "
                     + this.getName()
                     + " has been attacked by the cowardly "
                     + opponent.getName()
                     + ". Citizens, be strong and brave - we go to war!"
               );
            }

            this.addHistory(opponent.getName(), "is now a deadly enemy");
         }
      } else {
         logger.log(
            Level.WARNING,
            "Added war to " + this.getName() + " but the war is for " + war.getVilltwo().getName() + " and " + war.getVillone().getName() + ". Deleting."
         );
         war.delete();
      }
   }

   public final boolean isAlly(Village village) {
      if (village == null) {
         return false;
      } else {
         return this.allianceNumber > 0 && village.getAllianceNumber() == this.allianceNumber;
      }
   }

   public final Village[] getEnemies() {
      return this.wars != null && this.wars.size() > 0 ? this.wars.keySet().toArray(new Village[this.wars.size()]) : new Village[0];
   }

   final void declarePeace(Creature breaker, Creature accepter, Village village, boolean webreak) {
      if (this.wars != null || this.warDeclarations != null) {
         if (webreak) {
            this.broadCastNormal(
               "The wise "
                  + breaker.getName()
                  + " has ended the war with "
                  + village.getName()
                  + " through their intermediary "
                  + accepter.getName()
                  + ". Amnesty for all perpetrators is declared."
            );
            this.addHistory(breaker.getName(), "ends the war with " + village.getName());
         } else {
            this.broadCastNormal(
               breaker.getName()
                  + " of "
                  + village.getName()
                  + " has been given the grace of peace with "
                  + this.getName()
                  + " through the wise "
                  + accepter.getName()
                  + "! Amnesty for all perpetrators is declared."
            );
            this.addHistory(accepter.getName(), "accepts peace with " + village.getName());
         }

         this.declarePeace(village);
      }
   }

   final void removeWarDeclaration(Village village) {
      this.declarePeace(village);
      this.addHistory("someone", "removes the war declaration from " + village.getName());
   }

   public final void declarePeace(Village village) {
      if (this.wars != null) {
         Citizen[] vcitiz = village.getCitizens();

         for(int x = 0; x < vcitiz.length; ++x) {
            this.removeReputation(vcitiz[x].getId());
         }

         this.wars.remove(village);
      }

      if (this.warDeclarations != null) {
         WarDeclaration decl = this.warDeclarations.remove(village);
         if (decl != null) {
            decl.delete();
         }
      }
   }

   public final Village[] getAllies() {
      if (this.allianceNumber > 0) {
         PvPAlliance pvpall = PvPAlliance.getPvPAlliance(this.allianceNumber);
         if (pvpall != null) {
            return pvpall.getVillages();
         }

         logger.log(Level.WARNING, this.getName() + " has allianceNumber " + this.allianceNumber + " which doesn't exist.");
      }

      return emptyVillages;
   }

   public final String getAllianceName() {
      if (this.allianceNumber > 0) {
         PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.allianceNumber);
         if (alliance != null) {
            return alliance.getName();
         }
      }

      return "";
   }

   public final boolean isAlly(Creature creature) {
      Village village = creature.getCitizenVillage();
      if (village == null) {
         return false;
      } else {
         if (this.allianceNumber > 0 && village.getAllianceNumber() == this.allianceNumber) {
            Citizen cit = village.getCitizen(creature.getWurmId());
            if (cit != null) {
               VillageRole vr = cit.getRole();
               return vr != null && vr.mayPerformActionsOnAlliedDeeds();
            }
         }

         return false;
      }
   }

   public final void replaceDeed(Creature performer, Item oldDeed) {
      long oldDeedid = this.deedid;
      long newDeedid = -10L;
      if (oldDeedid != oldDeed.getWurmId()) {
         performer.getCommunicator().sendNormalServerMessage("This deed is not registered for this settlement called " + this.getName() + ".");
         logger.log(Level.WARNING, this.deedid + " does not match " + oldDeed.getWurmId() + " for " + performer.getName() + " in settlement " + this.getName());
      } else {
         long deedVal = (long)oldDeed.getValue();

         try {
            try {
               ItemTemplate newdeedtype = ItemTemplateFactory.getInstance().getTemplate(663);
               long toreimb = deedVal - (long)newdeedtype.getValue();
               if (toreimb > 0L) {
                  LoginServerWebConnection lsw = new LoginServerWebConnection();
                  if (!lsw.addMoney(performer.getWurmId(), performer.getName(), toreimb, "Replace" + oldDeedid)) {
                     performer.getCommunicator().sendSafeServerMessage("Failed to contact your bank. Please try later.");
                     return;
                  }

                  Items.destroyItem(oldDeedid);
               } else {
                  Items.destroyItem(oldDeedid);
               }
            } catch (NoSuchTemplateException var15) {
               logger.log(Level.WARNING, "No template for new deeds.");
               performer.getCommunicator().sendSafeServerMessage("An error occurred.");
               return;
            }

            Item newDeed = ItemFactory.createItem(663, 50.0F + Server.rand.nextFloat() * 50.0F, performer.getName());
            newDeed.setName("Settlement deed");
            performer.getInventory().insertItem(newDeed, true);

            try {
               newDeed.setDescription(this.getName());
               newDeed.setData2(this.id);
               this.setDeedId(newDeed.getWurmId());
               if (this.gates != null) {
                  for(FenceGate gate : this.gates) {
                     try {
                        Item lock = gate.getLock();
                        lock.addKey(-10L);
                        lock.removeKey(oldDeedid);
                     } catch (NoSuchLockException var14) {
                     }
                  }
               }

               performer.addKey(newDeed, false);
               this.plan.hiredGuardNumber = 50;
               int newNum = 0;
               this.plan.changePlan(0, newNum);
               int var22 = 3;
               this.plan.changePlan(0, var22);

               try {
                  if (!this.getRoleForStatus((byte)2).mayInviteCitizens()) {
                     logger.log(Level.INFO, "Set mayor to be able to invite for " + this.getName());
                     this.getRoleForStatus((byte)2).setMayInvite(true);
                  }
               } catch (NoSuchRoleException var13) {
                  logger.log(Level.INFO, "Failed to find mayo role to invite for " + performer.getName());
               }

               performer.getCommunicator()
                  .sendAlertServerMessage(
                     "You will be set to " + var22 + " heavy guards. You need to manage guards in order to make sure you have the desired amount."
                  );
            } catch (IOException var16) {
               performer.getCommunicator().sendNormalServerMessage("A server error occured while saving the new deed id.");
               logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
            }
         } catch (FailedException var17) {
            logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
            performer.getCommunicator().sendSafeServerMessage("An error occurred when creating the new deed.");
         } catch (NoSuchTemplateException var18) {
            logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
            performer.getCommunicator().sendSafeServerMessage("An error occurred when creating the new deed.");
         }
      }
   }

   public final void removeReputation(long wid) {
      Reputation reput = this.reputations.remove(new Long(wid));
      if (reput != null) {
         reput.delete();
      }
   }

   final void addGates() {
      Zone[] zones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, this.isOnSurface());

      for(int x = 0; x < zones.length; ++x) {
         zones[x].addGates(this);
      }
   }

   public final void removeGate(FenceGate gate) {
      if (this.gates.contains(gate)) {
         this.gates.remove(gate);
         if (gate.getVillageId() == this.getId()) {
            gate.setIsManaged(false, null);
         }

         gate.setVillage(null);

         try {
            Item lock = gate.getLock();
            lock.removeKey(this.deedid);
         } catch (NoSuchLockException var3) {
         }
      }
   }

   final void addMineDoors() {
      Zone[] zones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, this.isOnSurface());

      for(int x = 0; x < zones.length; ++x) {
         zones[x].addMineDoors(this);
      }
   }

   public final void removeMineDoor(MineDoorPermission mineDoor) {
      if (this.mineDoors.contains(mineDoor)) {
         this.mineDoors.remove(mineDoor);
         if (mineDoor.getVillageId() == this.getId()) {
            mineDoor.setIsManaged(false, null);
         }

         mineDoor.setVillage(null);
      }
   }

   public final long getDeedId() {
      return this.deedid;
   }

   public final VillageRole[] getRoles() {
      return this.roles.values().toArray(new VillageRole[this.roles.size()]);
   }

   public final String getMotto() {
      return this.motto;
   }

   public final String getMotd() {
      return this.motd;
   }

   protected final Message getDisbandMessage() {
      String left = "Less than a months upkeep left.";
      if (!this.isChained()) {
         left = "Not connected to kingdom influence.";
      }

      if (this.lessThanWeekLeft()) {
         left = "Under a weeks upkeep left.";
      }

      return new Message(null, (byte)3, "Village", "Village:" + left, 250, 150, 250);
   }

   protected final Message getMotdMessage() {
      return new Message(null, (byte)3, "Village", "MOTD:" + this.motd, 250, 150, 250);
   }

   protected final Message getRepMessage(String toSend) {
      return new Message(null, (byte)3, "Village", toSend);
   }

   public boolean isChained() {
      if (!Features.Feature.TOWER_CHAINING.isEnabled()) {
         return true;
      } else {
         try {
            Item token = this.getToken();
            return token.isChained();
         } catch (NoSuchItemException var2) {
            logger.warning(String.format("Village Error: No token found for village %s.", this.getName()));
            return true;
         }
      }
   }

   public final boolean isDemocracy() {
      return this.democracy;
   }

   public final boolean isOnSurface() {
      return this.surfaced;
   }

   final void createGuard(Creature creature, long expireDate) {
      VillageRole role = null;

      try {
         role = this.getRoleForStatus((byte)4);
      } catch (NoSuchRoleException var7) {
         role = this.createRoleGuard();
      }

      try {
         this.addCitizen(creature, role);
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Failed to add guard as citizen for settlement " + this.getName() + " " + var6.getMessage(), (Throwable)var6);
      }

      Guard guard = new DbGuard(this.id, creature, expireDate);
      guard.save();
      this.guards.put(new Long(creature.getWurmId()), guard);
   }

   public final void deleteGuard(Creature creature, boolean deleteCreature) {
      this.removeCitizen(creature);
      Guard guard = this.guards.get(new Long(creature.getWurmId()));
      this.guards.remove(new Long(creature.getWurmId()));
      if (guard != null) {
         guard.delete();
         if (deleteCreature) {
            if (this.plan != null) {
               this.plan.destroyGuard(creature);
            }

            guard.getCreature().destroy();
         }
      }

      this.assignTargets();
   }

   public final Guard[] getGuards() {
      return this.guards.values().toArray(new Guard[this.guards.size()]);
   }

   public final void createWagoner(Creature creature) {
      VillageRole role = null;

      try {
         role = this.getRoleForStatus((byte)6);
      } catch (NoSuchRoleException var5) {
         role = this.createRoleWagoner();
      }

      try {
         this.addCitizen(creature, role);
      } catch (IOException var4) {
         logger.log(Level.WARNING, "Failed to add wagoner as citizen for settlement " + this.getName() + " " + var4.getMessage(), (Throwable)var4);
      }

      Wagoner wagoner = creature.getWagoner();
      if (wagoner == null) {
         logger.log(Level.WARNING, "Wagoner not found!");
      } else {
         this.wagoners.put(new Long(creature.getWurmId()), wagoner);
      }
   }

   public final void deleteWagoner(Creature creature) {
      this.removeCitizen(creature);
      this.wagoners.remove(new Long(creature.getWurmId()));
      Wagoner wagoner = creature.getWagoner();
      if (wagoner != null) {
         wagoner.clrVillage();
      }

      if (this.wagoners.isEmpty()) {
         try {
            this.removeRole(this.getRoleForStatus((byte)6));
         } catch (NoSuchRoleException var4) {
            logger.log(
               Level.WARNING, "Cannot find role for wagoner so cannot remove it for settlement " + this.getName() + " " + var4.getMessage(), (Throwable)var4
            );
         }
      }
   }

   public final Wagoner[] getWagoners() {
      return this.wagoners.values().toArray(new Wagoner[this.wagoners.size()]);
   }

   public final VillageRole getRole(int aId) throws NoSuchRoleException {
      VillageRole toReturn = this.roles.get(aId);
      if (toReturn == null) {
         throw new NoSuchRoleException("No role with id " + aId);
      } else {
         return toReturn;
      }
   }

   public final VillageRole getRoleForStatus(byte status) throws NoSuchRoleException {
      for(VillageRole role : this.roles.values()) {
         if (role.getStatus() == status) {
            return role;
         }
      }

      throw new NoSuchRoleException("No role with status " + status);
   }

   public final VillageRole getRoleForVillage(int villageId) {
      if (villageId > 0) {
         for(VillageRole role : this.roles.values()) {
            if (role.getVillageAppliedTo() == villageId) {
               return role;
            }
         }
      }

      return null;
   }

   public final VillageRole getRoleForPlayer(long playerId) {
      Citizen citiz = this.citizens.get(new Long(playerId));
      if (citiz != null) {
         return citiz.getRole();
      } else {
         if (playerId > 0L) {
            for(VillageRole role : this.roles.values()) {
               if (role.getPlayerAppliedTo() == playerId) {
                  return role;
               }
            }
         }

         return null;
      }
   }

   public final void addRole(VillageRole role) {
      this.roles.put(role.getId(), role);
   }

   public final void removeRole(VillageRole role) {
      for(Citizen citiz : this.citizens.values()) {
         if (citiz.getRole() == role) {
            try {
               citiz.setRole(this.getRoleForStatus((byte)3));
            } catch (IOException var5) {
               logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            } catch (NoSuchRoleException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         }
      }

      this.roles.remove(role.getId());
   }

   public final boolean covers(int x, int y) {
      return x >= this.startx && x <= this.endx && y >= this.starty && y <= this.endy;
   }

   public final boolean coversPlus(int x, int y, int extra) {
      return x >= this.startx - extra && x <= this.endx + extra && y >= this.starty - extra && y <= this.endy + extra;
   }

   public final boolean coversWithPerimeter(int x, int y) {
      return x >= this.startx - 5 - this.perimeterTiles
         && x <= this.endx + 5 + this.perimeterTiles
         && y >= this.starty - 5 - this.perimeterTiles
         && y <= this.endy + 5 + this.perimeterTiles;
   }

   public final boolean coversWithPerimeterAndBuffer(int x, int y, int bufferTiles) {
      return x >= this.startx - 5 - this.perimeterTiles - bufferTiles
         && x <= this.endx + 5 + this.perimeterTiles + bufferTiles
         && y >= this.starty - 5 - this.perimeterTiles - bufferTiles
         && y <= this.endy + 5 + this.perimeterTiles + bufferTiles;
   }

   public final void modifyReputations(int num, Creature perpetrator) {
      if (!this.isEnemy(perpetrator.getCitizenVillage())) {
         this.modifyReputation(perpetrator.getWurmId(), num, perpetrator.isGuest());
      }
   }

   public final void modifyReputations(Action action, Creature perpetrator) {
      if (!this.isEnemy(perpetrator.getCitizenVillage())) {
         if (perpetrator.isFriendlyKingdom(this.kingdom)) {
            perpetrator.setUnmotivatedAttacker();
         }

         if (action.isOffensive()) {
            this.setReputation(perpetrator.getWurmId(), -100, perpetrator.isGuest(), false);
         } else if (Actions.isActionDestroy(action.getNumber())) {
            this.modifyReputation(perpetrator.getWurmId(), -10, perpetrator.isGuest());
         } else if (action.getNumber() != 74 && action.getNumber() != 6 && action.getNumber() != 100 && action.getNumber() != 101 && action.getNumber() != 465
            )
          {
            this.modifyReputation(perpetrator.getWurmId(), -5, perpetrator.isGuest());
         } else {
            this.modifyReputation(perpetrator.getWurmId(), -20, perpetrator.isGuest());
         }
      }
   }

   public final boolean checkGuards(Action action, Creature perpetrator) {
      float mod = 1.0F;
      if (Servers.localServer.HOMESERVER) {
         mod = 1.5F;
      }

      perpetrator.setSecondsToLogout(300);
      float dist = (float)Math.max(Math.abs(this.getCenterX() - action.getTileX()), Math.abs(this.getCenterY() - action.getTileY()));
      Guard[] g = this.getGuards();
      if (g.length == 0) {
         return false;
      } else {
         boolean noticed = false;
         boolean dryrun = false;
         Reputation rep = this.getReputationObject(perpetrator.getWurmId());
         if (rep != null && rep.getValue() >= 0 && rep.isPermanent()) {
            dryrun = true;
         }

         if (dist <= 5.0F) {
            if (action.getNumber() != 100 && !action.isEquipAction() && action.getNumber() != 101) {
               return true;
            }

            if (perpetrator.getStealSkill().getKnowledge(0.0) < 50.0) {
               perpetrator.getStealSkill().skillCheck(50.0, 0.0, dryrun, 10.0F);
               return true;
            }

            float diff = 75.0F - dist;
            if (Servers.localServer.HOMESERVER) {
               diff = 80.0F - dist;
            }

            noticed = perpetrator.getStealSkill().skillCheck((double)diff, 0.0, dryrun, 10.0F) < 0.0;
         }

         if (!noticed) {
            float factor = dist * dist / 5.0F;
            float guardfactor = (float)this.guards.size() / factor;
            if (!(Server.rand.nextFloat() < guardfactor)) {
               for(int x = 0; x < g.length; ++x) {
                  int tx = g[x].creature.getTileX();
                  int ty = g[x].creature.getTileY();
                  int d = Math.max(Math.abs(tx - action.getTileX()), Math.abs(ty - action.getTileY()));
                  if (d <= 5 && Server.rand.nextFloat() * mod < g[x].creature.getNoticeChance()) {
                     if (action.getNumber() != 100 && !action.isEquipAction() && action.getNumber() != 101) {
                        return true;
                     }

                     if (perpetrator.getStealSkill().skillCheck((double)(g[x].creature.getNoticeChance() * 100.0F), 0.0, dryrun, (float)x) < 0.0) {
                        return true;
                     }
                  }
               }

               return false;
            } else {
               if (action.getNumber() == 100 || action.isEquipAction() || action.getNumber() == 101) {
                  perpetrator.getStealSkill().skillCheck(20.0, 0.0, dryrun, 10.0F);
               }

               return true;
            }
         } else {
            return noticed;
         }
      }
   }

   public final boolean checkGuards(int tilex, int tiley, Creature perpetrator) {
      return this.checkGuards(tilex, tiley, perpetrator, 5.0F);
   }

   public final boolean checkGuards(int tilex, int tiley, Creature perpetrator, float maxdist) {
      perpetrator.setSecondsToLogout(300);
      float dist = (float)Math.max(Math.abs(this.getCenterX() - tilex), Math.abs(this.getCenterY() - tiley));
      if (dist <= 5.0F) {
         return true;
      } else {
         float factor = dist * dist / 5.0F;
         float guardfactor = (float)this.guards.size() / factor;
         if (Server.rand.nextFloat() < guardfactor) {
            return true;
         } else {
            Guard[] g = this.getGuards();

            for(int x = 0; x < g.length; ++x) {
               int tx = g[x].creature.getTileX();
               int ty = g[x].creature.getTileY();
               int d = Math.max(Math.abs(tx - tilex), Math.abs(ty - tiley));
               if ((float)d <= maxdist && Server.rand.nextFloat() < g[x].creature.getNoticeChance()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public int getMaxGuardsAttacking() {
      return Math.max(maxGuardsOnThisServer, this.guards.size() / 20);
   }

   public final void cryForHelp(Creature needhelp, boolean cry) {
      int guardsAssigned = 0;
      Guard best = null;
      int bestdist = Integer.MAX_VALUE;
      if (this.guards.size() > 1) {
         Creature target = needhelp.getTarget();
         if (target != null) {
            for(Guard guard : this.guards.values()) {
               if (guard.creature.target == target.getWurmId()) {
                  if (++guardsAssigned >= this.getMaxGuardsAttacking()) {
                     break;
                  }
               } else if (guard.creature.target == -10L) {
                  int diffx = (int)Math.abs(guard.creature.getPosX() - target.getPosX());
                  int diffy = (int)Math.abs(guard.creature.getPosY() - target.getPosY());
                  int dist = Math.max(diffx, diffy);
                  if (dist < bestdist) {
                     best = guard;
                     bestdist = dist;
                  }
               }
            }

            if (guardsAssigned < this.getMaxGuardsAttacking() && best != null) {
               if (cry) {
                  best.creature.say("I'll help you with " + target.getName() + "!");
               }

               boolean attackTarget = true;
               if (target.getVehicle() != -10L && Server.rand.nextInt(3) == 0) {
                  Vehicle vehic = Vehicles.getVehicleForId(target.getVehicle());
                  if (vehic != null && vehic.creature) {
                     best.creature.setTarget(target.getVehicle(), false);
                     attackTarget = false;
                  }
               }

               if (attackTarget) {
                  best.creature.setTarget(target.getWurmId(), false);
               }
            }
         }
      }
   }

   public final void resolveDispute(Creature performer, Creature defender) {
      if (this.guards.size() > 0) {
         if (this.mayAttack(performer, defender) && !this.mayAttack(defender, performer)) {
            this.setReputation(defender.getWurmId(), -100, defender.isGuest(), false);
         }

         if (this.mayAttack(defender, performer) && !this.mayAttack(performer, defender)) {
            this.setReputation(performer.getWurmId(), -100, performer.isGuest(), false);
         }
      }
   }

   public final boolean isActionAllowed(short action, Creature creature) {
      return this.isActionAllowed(action, creature, false, 0, 0);
   }

   public final boolean isActionAllowed(short action, Creature creature, boolean setHunted, int encodedTile, int dir) {
      boolean ok = this.isActionAllowed(setHunted, action, creature, encodedTile, dir);
      if (!ok && Servers.isThisAPvpServer()) {
         if (creature.isFriendlyKingdom(this.kingdom) && setHunted && (creature.getCitizenVillage() == null || !creature.getCitizenVillage().isEnemy(this))) {
            creature.setUnmotivatedAttacker();
         }

         if (this.isEnemy(creature)) {
            if (Actions.actionEntrys[action].isEnemyAllowedWhenNoGuards() && this.guards.size() == 0) {
               return true;
            }

            if (Actions.actionEntrys[action].isEnemyNeverAllowed()) {
               return false;
            }

            if (Actions.actionEntrys[action].isEnemyAlwaysAllowed()) {
               return true;
            }
         }
      }

      return ok;
   }

   private final boolean isActionAllowed(boolean ignoreGuardCount, short action, Creature creature, int encodedTile, int dir) {
      if (creature.getPower() >= 2) {
         return true;
      } else if (System.currentTimeMillis() - this.creationDate < 120000L) {
         return true;
      } else {
         VillageRole role = this.getRoleFor(creature);
         if (role == null) {
            return false;
         } else if (action != 100 && action != 350 && action != 537) {
            boolean onSurface = creature.getLayer() >= 0;
            byte tileType = Tiles.decodeType(encodedTile);
            Tiles.Tile t = Tiles.getTile(tileType);
            if (t == null) {
               logger.log(Level.SEVERE, "Unknown tile type " + tileType + " for " + creature.getName() + " at " + creature.getTilePos());
               return false;
            } else if (Actions.isActionBrand(action)) {
               return role.mayBrand() || this.everybody.mayBrand();
            } else if (Actions.isActionBreed(action)) {
               return role.mayBreed() || this.everybody.mayBreed();
            } else if (Actions.isActionButcher(action)) {
               return role.mayButcher() || this.everybody.mayButcher();
            } else if (Actions.isActionGroom(action)) {
               return role.mayGroom() || this.everybody.mayGroom();
            } else if (Actions.isActionLead(action)) {
               return role.mayLead() || this.everybody.mayLead();
            } else if (Actions.isActionMilkOrShear(action)) {
               return role.mayMilkAndShear() || this.everybody.mayMilkAndShear();
            } else if (Actions.isActionSacrifice(action)) {
               return role.maySacrifice() || this.everybody.maySacrifice();
            } else if (Actions.isActionTame(action)) {
               return role.mayTame() || this.everybody.mayTame();
            } else if (Actions.isActionBuild(action) || Actions.isActionChangeBuilding(action)) {
               return role.mayBuild() || this.everybody.mayBuild();
            } else if (Actions.isActionDestroyFence(action)) {
               return role.mayDestroyFences() || this.everybody.mayDestroyFences();
            } else if (Actions.isActionDestroyItem(action)) {
               return role.mayDestroyItems() || this.everybody.mayDestroyItems();
            } else if (Actions.isActionLockPick(action)) {
               return role.mayPickLocks();
            } else if (Actions.isActionPlanBuilding(action)) {
               return role.mayPlanBuildings() || this.everybody.mayPlanBuildings();
            } else if (Actions.isActionCultivate(action)) {
               return role.mayCultivate() || this.everybody.mayCultivate();
            } else if (!Actions.isActionDig(action)
               || tileType != Tiles.Tile.TILE_CLAY.id
                  && tileType != Tiles.Tile.TILE_MOSS.id
                  && tileType != Tiles.Tile.TILE_PEAT.id
                  && tileType != Tiles.Tile.TILE_TAR.id) {
               if (Actions.isActionPack(action)) {
                  return role.mayPack() || this.everybody.mayPack();
               } else if (Actions.isActionTerraform(action, onSurface)) {
                  return role.mayTerraform() || this.everybody.mayTerraform();
               } else if (!Actions.isActionHarvest(action) || tileType != Tiles.Tile.TILE_FIELD.id && tileType != Tiles.Tile.TILE_FIELD2.id) {
                  if (Actions.isActionSow(action)) {
                     return role.maySowFields() || this.everybody.maySowFields();
                  } else if (Actions.isActionFarm(action)) {
                     return role.mayTendFields() || this.everybody.mayTendFields();
                  } else if (encodedTile == 0 && Actions.isActionChop(action)) {
                     return role.mayDestroyFences() || this.everybody.mayDestroyFences();
                  } else {
                     Tiles.Tile theTile = Tiles.getTile(tileType);
                     if (!Actions.isActionChop(action) || !theTile.isTree() && !theTile.isBush()) {
                        if (Actions.isActionGather(action)) {
                           return role.mayCutGrass() || this.everybody.mayCutGrass();
                        } else if (Actions.isActionPick(action) || Actions.isActionHarvest(action) && (t.isTree() || t.isBush() || encodedTile == 0)) {
                           return role.mayHarvestFruit() || this.everybody.mayHarvestFruit();
                        } else if (Actions.isActionTrim(action)) {
                           return role.mayMakeLawn() || this.everybody.mayMakeLawn();
                        } else if (Actions.isActionPickSprout(action)) {
                           return role.mayPickSprouts() || this.everybody.mayPickSprouts();
                        } else if (Actions.isActionPlant(action)) {
                           return role.mayPlantFlowers() || this.everybody.mayPlantFlowers();
                        } else if (Actions.isActionPlantCenter(action)) {
                           return role.mayPlantSprouts() || this.everybody.mayPlantSprouts();
                        } else if (Actions.isActionPrune(action)) {
                           return role.mayPrune() || this.everybody.mayPrune();
                        } else if (Actions.isActionDietySpell(action)) {
                           return role.mayCastDeitySpells() || this.everybody.mayCastDeitySpells();
                        } else if (Actions.isActionSorcerySpell(action)) {
                           return role.mayCastSorcerySpells() || this.everybody.mayCastSorcerySpells();
                        } else if (Actions.isActionForageBotanizeInvestigate(action)) {
                           return role.mayForageAndBotanize() || this.everybody.mayForageAndBotanize();
                        } else if (Actions.isActionPlaceNPCs(action)) {
                           return role.mayPlaceMerchants() || this.everybody.mayPlaceMerchants();
                        } else if (Actions.isActionPave(action)) {
                           return role.mayPave() || this.everybody.mayPave();
                        } else if (Actions.isActionMeditate(action)) {
                           return role.mayUseMeditationAbilities() || this.everybody.mayUseMeditationAbilities();
                        } else if (Actions.isActionAttachLock(action)) {
                           return role.mayAttachLock() || this.everybody.mayAttachLock();
                        } else if (Actions.isActionDrop(action)) {
                           return role.mayDrop() || this.everybody.mayDrop();
                        } else if (Actions.isActionImproveOrRepair(action)) {
                           return role.mayImproveAndRepair() || this.everybody.mayImproveAndRepair();
                        } else if (Actions.isActionLoad(action)) {
                           return role.mayLoad() || this.everybody.mayLoad();
                        } else if (Actions.isActionTake(action)) {
                           return role.mayPickup() || this.everybody.mayPickup();
                        } else if (Actions.isActionPickupPlanted(action)) {
                           return role.mayPickupPlanted() || this.everybody.mayPickupPlanted();
                        } else if (Actions.isActionPlantItem(action)) {
                           return role.mayPlantItem() || this.everybody.mayPlantItem();
                        } else if (Actions.isActionPullPushTurn(action)) {
                           return role.mayPushPullTurn() || this.everybody.mayPushPullTurn();
                        } else if (Actions.isActionUnload(action)) {
                           return role.mayUnload() || this.everybody.mayUnload();
                        } else if (!onSurface && (dir == 0 || dir == 1) && Actions.isActionMineFloor(action)) {
                           return role.mayMineFloor() || this.everybody.mayMineFloor();
                        } else {
                           boolean isCaveWall = !onSurface && dir != 0 && dir != 1;
                           if (isCaveWall && tileType == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id && Actions.isActionMine(action)) {
                              return role.mayMineIronVeins() || this.everybody.mayMineIronVeins();
                           } else if (isCaveWall && tileType != Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id && t.isOreCave() && Actions.isActionMine(action)) {
                              return role.mayMineOtherVeins() || this.everybody.mayMineOtherVeins();
                           } else if (!isCaveWall
                              || !Actions.isActionMine(action)
                              || tileType != Tiles.Tile.TILE_CAVE_WALL.id && !Tiles.isReinforcedCave(tileType)) {
                              if (onSurface && Actions.isActionMineSurface(action)) {
                                 return role.mayMineSurface() || this.everybody.mayMineSurface();
                              } else if (Actions.isActionTunnel(action)) {
                                 return role.mayTunnel() || this.everybody.mayTunnel();
                              } else if (!onSurface && Actions.isActionReinforce(action)) {
                                 return role.mayReinforce() || this.everybody.mayReinforce();
                              } else if (Actions.isActionDestroy(action)) {
                                 return role.mayDestroyAnyBuilding();
                              } else if (action == 73) {
                                 return role.mayInviteCitizens() || creature.getCitizenVillage() != this;
                              } else if (Actions.isActionManage(action)) {
                                 return role.mayManageAllowedObjects();
                              } else if (action == 66) {
                                 return role.mayManageCitizenRoles();
                              } else if (action == 67) {
                                 return role.mayManageGuards();
                              } else if (action == 69) {
                                 return role.mayManageReputations();
                              } else if (action == 540) {
                                 return role.mayManageRoles();
                              } else if (action == 68) {
                                 return role.mayManageSettings();
                              } else if (action == 481) {
                                 return role.mayConfigureTwitter();
                              } else if (action == 348) {
                                 return role.mayDisbandSettlement();
                              } else {
                                 return action == 76 ? role.mayResizeSettlement() : true;
                              }
                           } else {
                              return role.mayMineRock() || this.everybody.mayMineRock();
                           }
                        }
                     } else {
                        byte tileData = Tiles.decodeData(encodedTile);
                        int treeAge = tileData >> 4 & 15;
                        if (treeAge > 11 && treeAge < 15 && theTile.isTree()) {
                           return role.mayChopDownOldTrees() || this.everybody.mayChopDownOldTrees();
                        } else {
                           return role.mayChopDownAllTrees() || this.everybody.mayChopDownAllTrees();
                        }
                     }
                  }
               } else {
                  return role.mayHarvestFields() || this.everybody.mayHarvestFields();
               }
            } else {
               return role.mayDigResources() || this.everybody.mayDigResources();
            }
         } else {
            return false;
         }
      }
   }

   public final void updateGatesForRole(VillageRole role) {
      if (this.citizens != null && this.gates != null) {
         for(FenceGate gate : this.gates) {
            for(Citizen citiz : this.citizens.values()) {
               if (citiz.getRole() == role) {
                  try {
                     Creature creat = Server.getInstance().getCreature(citiz.getId());
                     if (gate.containsCreature(creat)) {
                        creat.updateGates();
                     }
                  } catch (NoSuchCreatureException var7) {
                     logger.log(Level.WARNING, citiz.getName() + " - creature not found:", (Throwable)var7);
                  } catch (NoSuchPlayerException var8) {
                  }
               }
            }
         }
      }
   }

   public final boolean mayAttack(Creature attacker, Creature defender) {
      if (Servers.localServer.PVPSERVER) {
         if (attacker.isFriendlyKingdom(this.kingdom) && !defender.isFriendlyKingdom(this.kingdom)) {
            return true;
         }

         if (!attacker.isFriendlyKingdom(this.kingdom)) {
            return true;
         }

         if (attacker.isEnemyOnChaos(defender)) {
            return true;
         }
      }

      if (this.guards.size() >= 1 || (!attacker.isOnPvPServer() || !defender.isOnPvPServer()) && !this.isEnemy(defender)) {
         VillageRole attackerRole = this.getRoleFor(attacker);
         if (attackerRole == null) {
            return false;
         }

         Citizen def = this.citizens.get(new Long(defender.getWurmId()));
         if (!Servers.isThisAPvpServer() && def == null && defender.isBrandedBy(this.getId())) {
            return attackerRole.mayAttackCitizens() || this.everybody.mayAttackCitizens();
         }

         if (def != null) {
            return attackerRole.mayAttackCitizens() || this.everybody.mayAttackCitizens();
         }

         if (this.isAlly(defender)) {
            return attackerRole.mayAttackCitizens() || this.everybody.mayAttackCitizens();
         }

         if (!defender.isAtWarWith(attacker)) {
            return attackerRole.mayAttackNonCitizens() || this.everybody.mayAttackNonCitizens();
         }

         if (Kingdoms.getKingdomTemplateFor(this.kingdom) != 3 && attacker.getReputation() < 0 && defender.getReputation() >= 0) {
            return false;
         }
      }

      return true;
   }

   public final boolean mayDoDiplomacy(Creature creature) {
      Citizen citiz = this.citizens.get(new Long(creature.getWurmId()));
      VillageRole role = null;
      if (citiz != null) {
         role = citiz.getRole();
         return role.isDiplomat();
      } else {
         return false;
      }
   }

   public final int getId() {
      return this.id;
   }

   public final String getName() {
      return this.name;
   }

   public final String getFounderName() {
      return this.founderName;
   }

   public final boolean addCitizen(Creature creature, VillageRole role) throws IOException {
      long wurmid = creature.getWurmId();
      boolean first = true;
      if (creature.getCitizenVillage() != null) {
         creature.getCitizenVillage().removeCitizen(creature);
         first = false;
      }

      if (this.citizens.keySet().contains(new Long(wurmid))) {
         return false;
      } else {
         Citizen citizen = null;
         Citizen var13 = new DbCitizen(wurmid, creature.getName(), role, -10L, -10L);
         var13.create(creature, this.id);
         boolean ok = false;
         if (var13 != null) {
            this.broadCastSafe(creature.getName() + " is now a citizen of " + this.name + "!", (byte)2);
            this.citizens.put(new Long(var13.getId()), var13);
            creature.getCommunicator().sendSafeServerMessage("Congratulations! You are now the proud citizen of " + this.name + ".", (byte)2);
            this.group.addMember(creature.getName(), creature);
            MapAnnotation[] annotations = this.getVillageMapAnnotationsArray();
            if (annotations != null && creature.isPlayer()) {
               creature.getCommunicator().sendMapAnnotations(annotations);
            }

            creature.setCitizenVillage(this);
            if (creature.isPlayer()) {
               this.sendCitizensToPlayer((Player)creature);
            }

            if (this.getAllianceNumber() > 0) {
               PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(this.getAllianceNumber());
               if (pvpAll != null && pvpAll.getMotd().length() > 0) {
                  Message mess = pvpAll.getMotdMessage();
                  creature.getCommunicator().sendMessage(mess);
               } else {
                  Message mess = new Message(creature, (byte)15, "Alliance", "");
                  creature.getCommunicator().sendMessage(mess);
               }

               if (pvpAll != null) {
                  creature.getCommunicator().sendMapAnnotations(pvpAll.getAllianceMapAnnotationsArray());
               }
            }

            this.setReputation(creature.getWurmId(), 0, false, true);
            ok = true;
         }

         if (ok && creature.isPlayer()) {
            if (first) {
               creature.achievement(171);
            }

            this.addHistory(creature.getName(), "becomes a citizen");
            Citizen[] lCitizens = this.getCitizens();
            int plays = 0;

            for(int x = 0; x < lCitizens.length; ++x) {
               if (lCitizens[x].isPlayer()) {
                  try {
                     Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
                     if (lCitizens[x].getId() != wurmid) {
                        p.getCommunicator().sendAddVillager(creature.getName(), lCitizens[x].getId());
                        ++plays;
                     } else {
                        p.setLastChangedVillage(System.currentTimeMillis());
                     }
                  } catch (NoSuchPlayerException var12) {
                  }
               }
            }

            Players.getInstance().sendAddToAlliance(creature, this);
            if (plays > this.maxCitizens) {
               if (this.maxCitizens < 1000 && plays >= 1000) {
                  this.addHistory(creature.getName(), "breaks the thousand citizen count");
                  HistoryManager.addHistory(creature.getName(), "breaks the thousand citizen count of " + this.getName());
               }

               if (this.maxCitizens < 200 && plays >= 200) {
                  this.addHistory(creature.getName(), "breaks the twohundred citizen count");
                  HistoryManager.addHistory(creature.getName(), "breaks the twohundred citizen count of " + this.getName());
               } else if (this.maxCitizens < 100 && plays >= 100) {
                  this.addHistory(creature.getName(), "breaks the hundred citizen count");
                  HistoryManager.addHistory(creature.getName(), "breaks the hundred citizen count of " + this.getName());
               } else if (this.maxCitizens < 50 && plays >= 50) {
                  this.addHistory(creature.getName(), "breaks the fifty citizen count");
                  HistoryManager.addHistory(creature.getName(), "breaks the fifty citizen count of " + this.getName());
               } else if (this.maxCitizens < 20 && plays >= 20) {
                  this.addHistory(creature.getName(), "breaks the twenty citizen count");
                  HistoryManager.addHistory(creature.getName(), "breaks the twenty citizen count of " + this.getName());
               } else if (this.maxCitizens < 5 && plays >= 5) {
                  this.addHistory(creature.getName(), "breaks the five citizen count");
               }

               this.setMaxcitizens(plays);
            }
         }

         return ok;
      }
   }

   public final void removeCitizen(Creature creature) {
      if (creature.isPlayer()) {
         Players.getInstance().sendRemoveFromAlliance(creature, this);
      }

      this.citizens.remove(new Long(creature.getWurmId()));
      this.group.dropMember(creature.getName());
      creature.setCitizenVillage(null);
      if (creature.isPlayer() || creature.isWagoner()) {
         this.broadCastSafe(creature.getName() + " is no longer a citizen of " + this.name + ".");
      }

      creature.getCommunicator().sendSafeServerMessage("You are no longer citizen of " + this.name + ".", (byte)2);
      if (creature.isPlayer() && creature instanceof Player) {
         ((Player)creature).sendClearVillageMapAnnotations();
         ((Player)creature).sendClearAllianceMapAnnotations();
      }

      try {
         Citizen.delete(creature.getWurmId());
      } catch (IOException var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
      }

      if (WurmId.getType(creature.getWurmId()) == 0) {
         this.addHistory(creature.getName(), "is no longer a citizen");
         Citizen[] lCitizens = this.getCitizens();

         for(int x = 0; x < lCitizens.length; ++x) {
            if (WurmId.getType(lCitizens[x].getId()) == 0) {
               try {
                  Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
                  p.getCommunicator().sendRemoveVillager(creature.getName());
               } catch (NoSuchPlayerException var5) {
               }
            }
         }

         VillageMessages.delete(this.getId(), creature.getWurmId());
      }

      if (creature.isWagoner()) {
         this.addHistory(creature.getName(), "is no longer a citizen");
      }
   }

   public final void sendCitizensToPlayer(Player player) {
      if (this.motd != null && this.motd.length() > 0) {
         player.getCommunicator().sendMessage(this.getMotdMessage());
      }

      if (!this.moreThanMonthLeft()) {
         player.getCommunicator().sendMessage(this.getDisbandMessage());
      }

      Citizen[] lCitizens = this.getCitizens();

      for(int x = 0; x < lCitizens.length; ++x) {
         if (WurmId.getType(lCitizens[x].getId()) == 0 && lCitizens[x].getId() != player.getWurmId()) {
            try {
               Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
               player.getCommunicator().sendAddVillager(p.getName(), lCitizens[x].getId());
            } catch (NoSuchPlayerException var5) {
            }
         }
      }
   }

   public final void removeCitizen(Citizen citizen) {
      Creature creature = null;

      try {
         creature = Server.getInstance().getCreature(citizen.getId());
      } catch (NoSuchCreatureException var5) {
         logger.log(Level.WARNING, "No creature exists with wurmid " + citizen.getId() + " any longer?", (Throwable)var5);
      } catch (NoSuchPlayerException var6) {
      }

      if (creature != null) {
         this.removeCitizen(creature);
         if (citizen.getRole().getStatus() == 4) {
            this.deleteGuard(creature, false);
         }

         if (citizen.getRole().getStatus() == 6) {
            this.deleteWagoner(creature);
         }
      } else {
         this.citizens.remove(new Long(citizen.getId()));
         this.broadCastSafe(citizen.getName() + " is no longer a citizen of " + this.name + ".");
         this.addHistory(citizen.getName(), "is no longer a citizen");

         try {
            Citizen.delete(citizen.getId());
         } catch (IOException var4) {
            logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         }
      }
   }

   public final void broadCastSafe(String message) {
      this.broadCastSafe(message, (byte)0);
   }

   public final void broadCastSafe(String message, byte messageType) {
      this.group.broadCastSafe(message, messageType);
      this.twit(message);
   }

   public final void broadCastAlert(String message) {
      this.broadCastAlert(message, (byte)0);
   }

   public final void broadCastAlert(String message, byte messageType) {
      this.group.broadCastAlert(message, messageType);
      this.twit(message);
   }

   public final void broadCastNormal(String message) {
      this.group.broadCastNormal(message);
      this.twit(message);
   }

   public final void broadCastMessage(Message message) {
      this.broadCastMessage(message, true);
   }

   public final void broadCastMessage(Message message, boolean twit) {
      this.group.sendMessage(message);
      if (twit) {
         this.twit(message.getMessage());
      }
   }

   public final VillageRole getRoleFor(Creature creature) {
      if (this.everybody == null) {
         this.everybody = this.createRoleEverybody();
      }

      VillageRole role = this.getRoleForPlayer(creature.getWurmId());
      if (role == null) {
         if (creature.getCitizenVillage() != null) {
            role = this.getRoleForVillage(creature.getCitizenVillage().getId());
         }

         if (role == null) {
            try {
               if (this.isAlly(creature)) {
                  role = this.getRoleForStatus((byte)5);
               } else {
                  role = this.getRoleForStatus((byte)1);
               }
            } catch (NoSuchRoleException var4) {
               logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
            }
         }
      }

      return role;
   }

   public final VillageRole getRoleFor(long creatureId) {
      if (this.everybody == null) {
         this.everybody = this.createRoleEverybody();
      }

      VillageRole role = this.getRoleForPlayer(creatureId);
      if (role == null) {
         Village citvill = Villages.getVillageForCreature(creatureId);
         if (citvill != null) {
            role = this.getRoleForVillage(citvill.getId());
            if (role == null && this.allianceNumber > 0 && citvill.getAllianceNumber() == this.allianceNumber) {
               Citizen cit = citvill.getCitizen(creatureId);
               if (cit != null) {
                  VillageRole vr = cit.getRole();
                  if (vr != null && vr.mayPerformActionsOnAlliedDeeds()) {
                     try {
                        return this.getRoleForStatus((byte)5);
                     } catch (NoSuchRoleException var8) {
                        logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
                     }
                  }
               }
            }
         }

         role = this.everybody;
      }

      return role;
   }

   public final Citizen getCitizen(long wurmId) {
      return this.citizens.get(new Long(wurmId));
   }

   public final Citizen[] getCitizens() {
      Citizen[] toReturn = new Citizen[0];
      if (this.citizens.size() > 0) {
         toReturn = this.citizens.values().toArray(new Citizen[this.citizens.size()]);
      }

      return toReturn;
   }

   public final void replaceNoDeed(Creature mayor) {
      try {
         Item newDeed = ItemFactory.createItem(663, 50.0F + Server.rand.nextFloat() * 50.0F, mayor.getName());
         logger.log(Level.INFO, mayor.getName() + " replacing deed for " + this.getName() + " with id " + newDeed.getWurmId() + " from " + this.deedid);
         newDeed.setName("Settlement deed");
         newDeed.setDescription(this.getName());
         newDeed.setData2(this.id);
         mayor.getInventory().insertItem(newDeed, true);
         logger.log(Level.INFO, "Inserted " + newDeed + " into inventory of " + mayor.getName());
         long oldDeed = this.deedid;
         if (this.gates != null) {
            for(FenceGate gate : this.gates) {
               try {
                  Item lock = gate.getLock();
                  lock.addKey(newDeed.getWurmId());
                  lock.removeKey(this.deedid);
               } catch (NoSuchLockException var10) {
               }
            }
         }

         logger.log(Level.INFO, "Fixed gates. Now destroying " + this.deedid);
         Items.destroyItem(this.deedid);
         this.setDeedId(newDeed.getWurmId());
         logger.log(Level.INFO, "Setting deedid to " + newDeed.getWurmId());
         mayor.addKey(newDeed, false);

         try {
            logger.log(Level.INFO, "Verifying existance of deed " + newDeed.getWurmId());
            Item i = Items.getItem(newDeed.getWurmId());
            logger.log(Level.INFO, "Item " + i.getWurmId() + " was properly found in database! Data 2 is " + i.getData2());
         } catch (NoSuchItemException var9) {
            logger.log(Level.INFO, "Item " + newDeed.getWurmId() + " not found in database!");
         }

         try {
            logger.log(Level.INFO, "Verifying removal of deed " + oldDeed);
            Item i = Items.getItem(oldDeed);
            logger.log(Level.INFO, "Deed " + oldDeed + " was erroneously found in database! Data is " + i.getData2());
         } catch (NoSuchItemException var8) {
            logger.log(Level.INFO, "Item " + oldDeed + " properly not found in database!");
         }
      } catch (NoSuchTemplateException var11) {
         logger.log(Level.WARNING, "No deed template for settlement " + this.name, (Throwable)var11);
      } catch (FailedException var12) {
         logger.log(Level.WARNING, "Failed to create deed for settlement " + this.name, (Throwable)var12);
      } catch (IOException var13) {
         logger.log(Level.WARNING, "failed to set new deed id for the settlement of " + this.name, (Throwable)var13);
      }
   }

   public final void setNewBounds(int newsx, int newsy, int newex, int newey) {
      Zone[] coveredOldSurfaceZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
      Zone[] coveredOldCaveZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);
      int oldStartPerimeterX = this.startx - 5 - this.perimeterTiles;
      int oldStartPerimeterY = this.starty - 5 - this.perimeterTiles;
      int oldEndPerimeterX = this.endx + 5 + this.perimeterTiles;
      int oldEndPerimeterY = this.endy + 5 + this.perimeterTiles;
      Rectangle oldPerimeter = new Rectangle(
         oldStartPerimeterX, oldStartPerimeterY, oldEndPerimeterX - oldStartPerimeterX, oldEndPerimeterY - oldStartPerimeterY
      );

      try {
         this.setStartX(newsx);
         this.setStartY(newsy);
         this.setEndX(newex);
         this.setEndY(newey);
      } catch (IOException var27) {
         logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
      }

      int newStartPerimeterX = this.startx - 5 - this.perimeterTiles;
      int newStartPerimeterY = this.starty - 5 - this.perimeterTiles;
      int newEndPerimeterX = this.endx + 5 + this.perimeterTiles;
      int newEndPerimeterY = this.endy + 5 + this.perimeterTiles;

      for(int x = newStartPerimeterX; x <= newEndPerimeterX; ++x) {
         for(int y = newStartPerimeterY; y <= newEndPerimeterY; ++y) {
            if (!oldPerimeter.contains(x, y)) {
               Zones.setKingdom(x, y, this.kingdom);
            }
         }
      }

      Rectangle newPerimeter = new Rectangle(
         newStartPerimeterX, newStartPerimeterY, newEndPerimeterX - newStartPerimeterX, newEndPerimeterY - newStartPerimeterY
      );

      for(int x = oldStartPerimeterX; x <= oldEndPerimeterX; ++x) {
         for(int y = oldStartPerimeterY; y <= oldEndPerimeterY; ++y) {
            if (!newPerimeter.contains(x, y)) {
               Zones.setKingdom(x, y, (byte)0);
            }
         }
      }

      GuardTower nw = Kingdoms.getClosestTower(Math.min(oldStartPerimeterX, newStartPerimeterX), Math.min(oldStartPerimeterY, newStartPerimeterY), true);
      if (nw != null) {
         Kingdoms.addTowerKingdom(nw.getTower());
      }

      GuardTower ne = Kingdoms.getClosestTower(Math.max(oldEndPerimeterX, newEndPerimeterX), Math.min(oldStartPerimeterY, newStartPerimeterY), true);
      if (ne != null && ne != nw) {
         Kingdoms.addTowerKingdom(ne.getTower());
      }

      GuardTower se = Kingdoms.getClosestTower(Math.min(oldStartPerimeterX, newStartPerimeterX), Math.max(oldEndPerimeterY, newEndPerimeterY), true);
      if (se != null && se != nw && se != ne) {
         Kingdoms.addTowerKingdom(se.getTower());
      }

      GuardTower sw = Kingdoms.getClosestTower(Math.max(oldEndPerimeterX, newEndPerimeterX), Math.max(oldEndPerimeterY, newEndPerimeterY), true);
      if (sw != null && sw != nw && sw != ne && sw != nw) {
         Kingdoms.addTowerKingdom(sw.getTower());
      }

      Zone[] coveredNewSurfaceZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
      Set<Zone> notfound = new HashSet<>();

      for(int y = 0; y < coveredOldSurfaceZones.length; ++y) {
         notfound.add(coveredOldSurfaceZones[y]);
      }

      boolean found = false;

      for(int x = 0; x < coveredNewSurfaceZones.length; ++x) {
         found = false;

         for(int y = 0; y < coveredOldSurfaceZones.length; ++y) {
            if (coveredNewSurfaceZones[x].getId() == coveredOldSurfaceZones[y].getId()) {
               coveredNewSurfaceZones[x].updateVillage(this, true);
               notfound.remove(coveredOldSurfaceZones[y]);
               found = true;
               break;
            }
         }

         if (!found) {
            coveredNewSurfaceZones[x].updateVillage(this, true);
         }
      }

      for(Zone z : notfound) {
         z.updateVillage(this, false);
      }

      notfound.clear();

      for(int y = 0; y < coveredOldCaveZones.length; ++y) {
         notfound.add(coveredOldCaveZones[y]);
      }

      Zone[] coveredNewCaveZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);

      for(int x = 0; x < coveredNewCaveZones.length; ++x) {
         found = false;

         for(int y = 0; y < coveredOldCaveZones.length; ++y) {
            if (coveredNewCaveZones[x].getId() == coveredOldCaveZones[y].getId()) {
               coveredOldCaveZones[y].updateVillage(this, true);
               notfound.remove(coveredOldCaveZones[y]);
               found = true;
               break;
            }
         }

         if (!found) {
            coveredNewCaveZones[x].updateVillage(this, true);
         }
      }

      for(Zone z : notfound) {
         z.updateVillage(this, false);
      }
   }

   public final boolean isCitizen(Creature creature) {
      long wid = creature.getWurmId();
      return this.citizens.keySet().contains(new Long(wid));
   }

   public final boolean isCitizen(long wid) {
      return this.citizens.keySet().contains(new Long(wid));
   }

   public final boolean isMayor(Creature creature) {
      return this.isMayor(creature.getWurmId());
   }

   public final boolean isMayor(long playerId) {
      Citizen c = this.getCitizen(playerId);
      return c != null && c.getRole().getStatus() == 2;
   }

   private void checkLeadership() {
      Citizen[] citizarr = this.getCitizens();
      Citizen leader = null;
      Citizen currMayor = null;
      Map<Long, Integer> votees = new HashMap<>();
      int votesCast = 0;

      for(int x = 0; x < citizarr.length; ++x) {
         if (citizarr[x].hasVoted()) {
            ++votesCast;
            long votedFor = citizarr[x].getVotedFor();
            Long vote = new Long(votedFor);
            Integer votei = votees.get(vote);
            if (votei == null) {
               votei = 0;
            }

            int votes = votei + 1;
            votei = votes;
            votees.put(vote, votei);
         }

         if (citizarr[x].getRole().getStatus() == 2) {
            currMayor = citizarr[x];
         }

         try {
            citizarr[x].setVoteDate(-10L);
            citizarr[x].setVotedFor(-10L);
         } catch (IOException var14) {
            logger.log(Level.WARNING, "Failed to clear votes for " + citizarr[x].getName() + ": " + var14.getMessage(), (Throwable)var14);
         }
      }

      long leaderlong = -10L;
      int maxvotes = 0;

      for(Long target : votees.keySet()) {
         Integer votes = votees.get(target);
         if (votes > maxvotes) {
            leaderlong = target;
            maxvotes = votes;
         }
      }

      leader = this.citizens.get(new Long(leaderlong));
      logger.log(Level.INFO, this.getName() + " Checking if " + leader + " will become mayor with " + maxvotes + " out of " + votesCast + ".");
      if (leader != null && this.changeRule(maxvotes, votesCast)) {
         logger.log(Level.INFO, this.getName() + " swapping owners - old: " + currMayor + ", new: " + leader);
         this.swapDeedOwners(currMayor, leader);

         try {
            this.group.broadCastSafe(this.name + " has a new " + this.getRoleForStatus((byte)2).getName() + "! Hail " + leader.getName() + "!", (byte)2);
            this.addHistory(
               leader.getName(), "new " + this.getRoleForStatus((byte)2).getName() + " by a vote of " + maxvotes + " out of " + votesCast + " cast"
            );
         } catch (NoSuchRoleException var13) {
            logger.log(Level.WARNING, this.name + " has no ROLE_MAYOR!");
         }
      } else {
         try {
            if (currMayor != null) {
               this.group
                  .broadCastSafe(
                     currMayor.getName()
                        + " will be your "
                        + this.getRoleForStatus((byte)2).getName()
                        + " for another period! Hail "
                        + currMayor.getName()
                        + "!",
                     (byte)2
                  );
               this.addHistory(currMayor.getName(), "stays " + this.getRoleForStatus((byte)2).getName() + ". Number of votes cast: " + votesCast);
            } else {
               this.group.broadCastSafe("You will have no " + this.getRoleForStatus((byte)2).getName() + " for another voting period.", (byte)2);
            }
         } catch (NoSuchRoleException var12) {
            logger.log(Level.WARNING, this.name + " has no ROLE_MAYOR!");
         }
      }
   }

   public final Citizen getMayor() {
      Citizen[] citizarr = this.getCitizens();

      for(int x = 0; x < citizarr.length; ++x) {
         VillageRole role = citizarr[x].getRole();
         if (role.getStatus() == 2) {
            return citizarr[x];
         }
      }

      return null;
   }

   private void swapDeedOwners(Citizen mayor, Citizen newMayor) {
      try {
         if (newMayor != null) {
            try {
               Item deed = Items.getItem(this.deedid);
               Creature mayorCreature = null;
               if (mayor != null) {
                  try {
                     mayorCreature = Server.getInstance().getCreature(mayor.getId());
                  } catch (NoSuchCreatureException var9) {
                     logger.log(Level.WARNING, "The mayor for " + this.name + " is a creature?", (Throwable)var9);
                  } catch (NoSuchPlayerException var10) {
                     logger.log(Level.INFO, mayor.getName() + " is offline loosing mayorship.");
                  }
               }

               Creature newMayorCreature = null;

               try {
                  newMayorCreature = Server.getInstance().getCreature(newMayor.getId());
               } catch (NoSuchCreatureException var7) {
                  logger.log(Level.WARNING, "The mayor for " + this.name + " is a creature?", (Throwable)var7);
               } catch (NoSuchPlayerException var8) {
                  logger.log(Level.INFO, newMayor.getName() + " is offline becoming mayor.");
               }

               try {
                  if (mayor != null) {
                     mayor.setRole(this.getRoleForStatus((byte)3));
                  }

                  newMayor.setRole(this.getRoleForStatus((byte)2));
                  if (mayorCreature != null && newMayorCreature != null) {
                     this.swapDeedOwners(mayorCreature, newMayorCreature, deed);
                  } else if (mayorCreature == null && newMayorCreature != null) {
                     this.swapDeedOwners(mayor, newMayorCreature, deed);
                  } else if (newMayorCreature == null && mayorCreature != null) {
                     this.swapDeedOwners(mayorCreature, newMayor, deed);
                  } else {
                     Items.returnItemFromFreezer(this.deedid);
                     deed.setParentId(DbCreatureStatus.getInventoryIdFor(newMayor.getId()), true);
                     deed.setOwnerId(newMayor.getId());
                  }

                  if (mayorCreature != null) {
                     mayorCreature.getCommunicator().sendSafeServerMessage("You are no longer the mayor of " + this.name + ".");
                  }

                  if (newMayorCreature != null) {
                     newMayorCreature.getCommunicator().sendSafeServerMessage("You are now the new mayor of " + this.name + ". Serve it well.");
                  }

                  this.setMayor(newMayor.getName());
               } catch (IOException var11) {
                  logger.log(Level.WARNING, this.getName() + " failed to set mayor status: " + var11.getMessage(), (Throwable)var11);
               } catch (NoSuchRoleException var12) {
                  logger.log(Level.WARNING, this.getName() + " this settlement doesn't have the correct roles: " + var12.getMessage(), (Throwable)var12);
               }
            } catch (NoSuchItemException var13) {
               logger.log(
                  Level.WARNING, "Deed with id " + this.deedid + " for settlement " + this.getName() + ", " + this.id + " not found!", (Throwable)var13
               );
            }
         } else if (newMayor == null) {
            logger.log(Level.INFO, "Error, new mayor is null: " + this.name + ".", (Throwable)(new Exception()));
         }
      } catch (NullPointerException var14) {
         logger.log(Level.INFO, var14.getMessage(), (Throwable)var14);
      }
   }

   private void swapDeedOwners(Creature owner, Creature receiver, Item deed) throws NoSuchItemException {
      owner.getInventory().dropItem(deed.getWurmId(), false);
      receiver.getInventory().insertItem(deed);
   }

   private void swapDeedOwners(Creature owner, Citizen receiver, Item deed) throws NoSuchItemException {
      owner.getInventory().dropItem(deed.getWurmId(), false);
      deed.setOwnerId(receiver.getId());
      deed.setParentId(DbCreatureStatus.getInventoryIdFor(receiver.getId()), owner.isOnSurface());
   }

   private boolean enoughVotes() {
      if (!this.isDemocracy()) {
         return false;
      } else {
         Citizen[] citizarr = this.getCitizens();
         int votes = 0;

         for(int x = 0; x < citizarr.length; ++x) {
            if (citizarr[x].hasVoted()) {
               ++votes;
            }
         }

         int activeCitizens = 0;

         for(Long it : this.citizens.keySet()) {
            long wurmid = it;
            if (WurmId.getType(wurmid) == 0) {
               long lastLogout = Players.getInstance().getLastLogoutForPlayer(wurmid);
               if (System.currentTimeMillis() - lastLogout < 1209600000L) {
                  ++activeCitizens;
               }
            }
         }

         logger.log(Level.INFO, this.getName() + " votes is " + votes + " for the last week, active citizens are " + activeCitizens);
         return this.changeRule(votes, activeCitizens);
      }
   }

   public final void vote(Creature voter, String targname) throws IOException, NoSuchPlayerException {
      if (!this.isDemocracy()) {
         voter.getCommunicator().sendNormalServerMessage("You vote for " + targname + " is noted, but ignored.", (byte)3);
      } else {
         if (!voter.getName().equals(targname)) {
            Citizen votercit = this.citizens.get(new Long(voter.getWurmId()));
            if (votercit != null) {
               if (!votercit.hasVoted()) {
                  long vid = Players.getInstance().getWurmIdFor(targname);
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(vid);
                  if (pinf != null) {
                     if (pinf.isPaying()) {
                        Citizen targcit = this.citizens.get(new Long(vid));
                        if (targcit != null) {
                           votercit.setVotedFor(vid);
                           votercit.setVoteDate(System.currentTimeMillis());
                           voter.getCommunicator().sendNormalServerMessage("You vote for " + targname + " as mayor this week.");
                           if (this.enoughVotes()) {
                              this.checkLeadership();
                           }
                        } else {
                           voter.getCommunicator().sendNormalServerMessage(targname + " is not a citizen of " + this.name + ".", (byte)3);
                        }
                     } else {
                        voter.getCommunicator().sendNormalServerMessage("You may only vote for premium players as mayor.", (byte)3);
                     }
                  } else {
                     voter.getCommunicator().sendNormalServerMessage(targname + " is not a citizen of " + this.name + ".", (byte)3);
                  }
               } else {
                  voter.getCommunicator().sendNormalServerMessage("You have already voted in the election this week.", (byte)3);
               }
            } else {
               logger.log(Level.WARNING, voter.getName() + " tried to vote in a settlement he wasn't citizen of!");
            }
         } else {
            voter.getCommunicator().sendNormalServerMessage("You cannot vote for yourself in the mayor elections.", (byte)3);
         }
      }
   }

   private void swapDeedOwners(Citizen owner, Creature receiver, Item deed) throws NoSuchItemException {
      receiver.getInventory().insertItem(deed, true);
   }

   private boolean changeRule(int votes, int totalVotes) {
      logger.log(
         Level.INFO,
         this.getName()
            + " total votes is "
            + totalVotes
            + ". Votes is "
            + votes
            + " so fraction is "
            + (float)votes / (float)totalVotes
            + ". This is a democracy="
            + this.democracy
            + ": 0.51*="
            + 0.51 * (double)totalVotes
            + ", 0.81*="
            + 0.81 * (double)totalVotes
      );
      if (this.democracy) {
         return (double)votes >= 0.51 * (double)totalVotes;
      } else {
         return false;
      }
   }

   public final Item getToken() throws NoSuchItemException {
      return Items.getItem(this.tokenId);
   }

   public final String getTag() {
      return this.getName().substring(0, 3);
   }

   public final long getDisbanding() {
      return this.disband;
   }

   public final boolean isDisbanding() {
      return this.disband != 0L;
   }

   public final boolean checkDisband(long now) {
      return this.disband != 0L && now > this.disband;
   }

   public final boolean isLeavingPmk() {
      return this.pmkKickDate != 0L;
   }

   public final boolean checkLeavePmk(long now) {
      return this.pmkKickDate > 0L && now > this.pmkKickDate;
   }

   public final void startDisbanding(Creature performer, String aName, long disbid) {
      this.addHistory(aName, "starts disbanding");
      if (performer == null || this.getMayor().getId() == disbid && this.getDiameterX() < 30 && this.getDiameterY() < 30) {
         try {
            this.setDisbandTime(System.currentTimeMillis() + 3600000L);
            this.setDisbander(disbid);
         } catch (IOException var7) {
            this.disband = System.currentTimeMillis() + 3600000L;
            logger.log(Level.WARNING, "Failed to set disband time for settlement with id " + this.getId() + ".", (Throwable)var7);
         }
      } else {
         try {
            this.setDisbandTime(System.currentTimeMillis() + 86400000L);
            this.setDisbander(disbid);
         } catch (IOException var6) {
            this.disband = System.currentTimeMillis() + 86400000L;
            logger.log(Level.WARNING, "Failed to set disband time for settlement with id " + this.getId() + ".", (Throwable)var6);
         }
      }
   }

   public final long getDisbander() {
      return this.disbander;
   }

   final void stopDisbanding() {
      if (this.disband != 0L) {
         try {
            try {
               Player player = Players.getInstance().getPlayer(this.getDisbander());
               player.getCommunicator().sendAlertServerMessage("The settlement of " + this.getName() + " has been salvaged!", (byte)2);
               this.addHistory(player.getName(), "salvages the settlement from disbanding");
            } catch (NoSuchPlayerException var3) {
               this.addHistory("", "the settlement has been salvaged from disbanding");
            }

            Village[] allies = this.getAllies();

            for(int x = 0; x < allies.length; ++x) {
               allies[x].broadCastSafe("The settlement of " + this.getName() + " has been salvaged.", (byte)2);
            }

            this.setDisbandTime(0L);
            this.setDisbander(-10L);
         } catch (IOException var4) {
            this.disband = 0L;
            this.addHistory("", "the settlement has been salvaged from disbanding");
            logger.log(Level.WARNING, "Failed to set disband time to 0 for settlement with id " + this.getId() + ".", (Throwable)var4);
         }
      }
   }

   private long getFoundingCost() {
      int tiles = this.getDiameterX() * this.getDiameterY();
      long moneyNeeded = (long)tiles * Villages.TILE_COST;
      return moneyNeeded + (long)this.perimeterTiles * Villages.PERIMETER_COST;
   }

   public final boolean givesTheftBonus() {
      return this.plan.isUnderSiege() && this.plan.hiredGuardNumber > 9;
   }

   public final void disband(String disbanderName) {
      long moneyToReimburse = 0L;
      if (!disbanderName.equals("upkeep")) {
         Citizen mayor = this.getMayor();
         if (mayor != null) {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(mayor.wurmId);
            if (pinf != null) {
               if (freeDisbands) {
                  long left = Servers.localServer.isFreeDeeds() ? 0L : this.plan.moneyLeft;
                  left -= 10000L;

                  try {
                     Item deed = Items.getItem(this.deedid);
                     if (deed.isNewDeed() && !Servers.localServer.isFreeDeeds()) {
                        logger.log(Level.INFO, "DISBANDING " + this.getName() + " left=" + left + ". Found cost=" + this.getFoundingCost());
                        left += this.getFoundingCost();
                     }
                  } catch (NoSuchItemException var24) {
                     logger.log(Level.WARNING, this.getName() + " No deed id with id=" + this.deedid, (Throwable)var24);
                  }

                  Citizen[] citizarr = this.getCitizens();

                  for(int x = 0; x < citizarr.length; ++x) {
                     if (WurmId.getType(citizarr[x].wurmId) == 1) {
                        try {
                           Creature c = Creatures.getInstance().getCreature(citizarr[x].wurmId);
                           if (c.isNpcTrader()) {
                              Shop shop = Economy.getEconomy().getShop(c);
                              if (shop != null && !shop.isPersonal()) {
                                 logger.log(Level.INFO, "Adding 20 silver to " + pinf.getName() + " for trader in settlement " + this.getName());
                                 left += 200000L;
                              }
                           } else if (c.isSpiritGuard() && !Servers.localServer.isFreeDeeds()) {
                              logger.log(Level.INFO, "Adding guard cost to " + pinf.getName() + " for guard in settlement " + this.getName());
                              left += Villages.GUARD_COST;
                           }
                        } catch (NoSuchCreatureException var23) {
                        }
                     }
                  }

                  if (left > 0L) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     if (!lsw.addMoney(mayor.wurmId, pinf.getName(), left, "Disb " + this.getName())) {
                        logger.log(Level.INFO, "Postponing disbanding " + this.getName() + ".");
                        return;
                     }
                  }
               } else {
                  long left = 0L;
                  if (!Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep()
                     || Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep() && this.creationDate > System.currentTimeMillis() + 2419200000L) {
                     left = this.plan.getDisbandMoneyLeft();
                  }

                  moneyToReimburse += Math.max(left, 0L);
                  if (moneyToReimburse > 0L) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     if (!lsw.addMoney(mayor.wurmId, pinf.getName(), moneyToReimburse, "Disb " + this.getName())) {
                        logger.log(Level.INFO, "Postponing disbanding " + this.getName() + ".");
                        return;
                     }
                  }
               }
            }
         } else {
            logger.log(Level.INFO, "NO mayor found for " + this.getName() + " when disbanding.");
         }
      }

      if (this.gates != null) {
         for(FenceGate gate : this.gates) {
            gate.setOpenTime(0);
            gate.setCloseTime(0);
         }
      }

      FenceGate.unManageGatesFor(this.getId());
      MineDoorPermission.unManageMineDoorsFor(this.getId());
      Creatures.getInstance().removeBrandingFor(this.getId());
      VillageMessages.delete(this.getId());
      Zone[] coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);

      for(int x = 0; x < coveredZones.length; ++x) {
         coveredZones[x].removeVillage(this);
      }

      coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);

      for(int x = 0; x < coveredZones.length; ++x) {
         coveredZones[x].removeVillage(this);
      }

      Zones.setKingdom(
         this.startx - 5 - this.getPerimeterSize(),
         this.starty - 5 - this.getPerimeterSize(),
         this.getPerimeterDiameterX(),
         this.getPerimeterDiameterY(),
         (byte)0
      );
      Kingdoms.reAddKingdomInfluences(
         -5 - this.getPerimeterSize() * 2,
         this.starty - 5 - this.getPerimeterSize() * 2,
         this.endx + 5 + this.getPerimeterSize() * 2,
         this.endy + 5 + this.getPerimeterSize() * 2
      );

      try {
         Item token = this.getToken();
         Items.destroyItem(token.getWurmId());
      } catch (NoSuchItemException var22) {
         logger.log(
            Level.WARNING,
            "No token for settlement " + this.getName() + " when destroying it at " + this.getStartX() + ", " + this.getStartY() + ".",
            (Throwable)var22
         );
      }

      Guard[] guardarr = this.getGuards();

      for(int x = 0; x < guardarr.length; ++x) {
         Creature c = guardarr[x].getCreature();
         this.deleteGuard(c, true);
      }

      Wagoner[] wagonerarr = this.getWagoners();

      for(int x = 0; x < wagonerarr.length; ++x) {
         Creature c = wagonerarr[x].getCreature();
         if (c != null) {
            this.deleteWagoner(c);
         }
      }

      Citizen[] citizarr = this.getCitizens();

      for(int x = 0; x < citizarr.length; ++x) {
         if (WurmId.getType(citizarr[x].wurmId) == 1) {
            try {
               Creature c = Creatures.getInstance().getCreature(citizarr[x].wurmId);
               if (c.isNpcTrader()) {
                  c.destroy();
               }
            } catch (NoSuchCreatureException var21) {
            }
         }

         try {
            Creature c = Server.getInstance().getCreature(citizarr[x].wurmId);
            if (c.getMusicPlayer() != null) {
               c.getMusicPlayer().checkMUSIC_DISBAND_SND();
            }
         } catch (NoSuchPlayerException var19) {
         } catch (NoSuchCreatureException var20) {
         }

         this.removeCitizen(citizarr[x]);
      }

      if (this.citizens != null) {
         this.citizens.clear();
      }

      VillageRole[] rolearr = this.getRoles();

      for(int x = 0; x < rolearr.length; ++x) {
         try {
            rolearr[x].delete();
         } catch (IOException var18) {
            logger.log(
               Level.WARNING,
               "Failed to delete role with id "
                  + rolearr[x].getId()
                  + " for settlement "
                  + this.getName()
                  + " with id "
                  + this.id
                  + " from db: "
                  + var18.getMessage(),
               (Throwable)var18
            );
         }
      }

      if (this.roles != null) {
         this.roles.clear();
      }

      try {
         RecruitmentAds.deleteVillageAd(this);
         this.delete();
         this.deleteVillageMapAnnotations();
      } catch (IOException var17) {
         logger.log(Level.WARNING, "Failed to delete settlement " + this.getName() + " from db: " + var17.getMessage(), (Throwable)var17);
      }

      if (this.wars != null) {
         for(Village opponent : this.wars.keySet()) {
            opponent.broadCastSafe(this.getName() + " has just been disbanded!", (byte)2);
            if (opponent.wars != null) {
               opponent.wars.remove(this);
            }

            VillageWar war = this.wars.get(opponent);
            war.delete();
         }

         this.wars.clear();
      }

      if (this.warDeclarations != null) {
         for(Village opponent : this.warDeclarations.keySet()) {
            opponent.broadCastSafe(this.getName() + " has just been disbanded!", (byte)2);
            if (opponent.warDeclarations != null) {
               opponent.warDeclarations.remove(this);
            }

            WarDeclaration war = this.warDeclarations.get(opponent);
            war.delete();
         }

         this.warDeclarations.clear();
      }

      if (this.reputations != null) {
         Reputation[] reps = this.getReputations();

         for(int x = 0; x < reps.length; ++x) {
            reps[x].delete();
         }

         this.reputations.clear();
      }

      this.plan.delete();
      this.plan = null;
      Villages.removeVillage(this.id);
      Server.getInstance().broadCastSafe(WurmCalendar.getTime(), false);
      String vil = "settlement";
      if (disbanderName.equals("upkeep")) {
         Server.getInstance().broadCastSafe("The settlement of " + this.getName() + " has just been disbanded.", true, (byte)2);
      } else {
         Server.getInstance().broadCastSafe("The settlement of " + this.getName() + " has just been disbanded by " + disbanderName + ".", true, (byte)2);
      }

      this.addHistory(disbanderName, "disbanded");
      HistoryManager.addHistory(disbanderName, "disbanded " + this.getName(), false);
      long check = System.currentTimeMillis();
      if (Villages.wasLastVillage(this)) {
         Kingdom k = Kingdoms.getKingdom(this.kingdom);
         if (k != null) {
            k.disband();
         }
      }

      this.leavePvPAlliance();
      if (freeDisbands) {
         if (disbanderName.equals("upkeep")) {
            Items.destroyItem(this.getDeedId());
         } else {
            try {
               Item deed = Items.getItem(this.deedid);
               if (!deed.isNewDeed()) {
                  deed.setName(deed.getTemplate().getName());
                  deed.setDescription("");
                  deed.setData(-1, -1);
                  deed.setAuxData((byte)0);
               } else {
                  Items.destroyItem(this.deedid);
               }
            } catch (NoSuchItemException var16) {
            }
         }
      } else {
         Items.destroyItem(this.getDeedId());
      }

      logger.info("The settlement of " + this.getName() + ", " + this.id + " has just been disbanded by " + disbanderName + ".");
      if (System.currentTimeMillis() - check > 1000L) {
         logger.log(Level.INFO, "Lag detected when destroying deed at 7.11: " + (int)((System.currentTimeMillis() - check) / 1000L));
      }

      GuardTower nw = Kingdoms.getClosestTower(this.startx, this.starty, true);
      if (nw != null) {
         Kingdoms.addTowerKingdom(nw.getTower());
      }

      GuardTower ne = Kingdoms.getClosestTower(this.endx, this.starty, true);
      if (ne != null && ne != nw) {
         Kingdoms.addTowerKingdom(ne.getTower());
      }

      GuardTower se = Kingdoms.getClosestTower(this.startx, this.endy, true);
      if (se != null && se != ne && se != nw) {
         Kingdoms.addTowerKingdom(se.getTower());
      }

      GuardTower sw = Kingdoms.getClosestTower(this.endx, this.endy, true);
      if (sw != null && sw != nw && sw != ne && sw != nw) {
         Kingdoms.addTowerKingdom(sw.getTower());
      }
   }

   private void leavePvPAlliance() {
      PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(this.getAllianceNumber());
      if (pvpAll != null) {
         if (this.getId() == this.getAllianceNumber()) {
            Village newCap = null;
            Village[] allyArr = this.getAllies();
            this.setAllianceNumber(0);
            boolean alldisb = false;
            if (!pvpAll.exists()) {
               alldisb = true;
               pvpAll.delete();
               pvpAll.sendClearAllianceAnnotations();
               pvpAll.deleteAllianceMapAnnotations();
            }

            for(Village v : allyArr) {
               if (v.getId() != this.getId()) {
                  if (alldisb) {
                     v.broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                     v.setAllianceNumber(0);
                  } else if (newCap == null) {
                     newCap = v;
                     v.setAllianceNumber(v.getId());
                     pvpAll.setIdNumber(v.getId());
                     v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + " and " + v.getName() + " is the new main settlement.");
                     v.addHistory(this.getName(), "left the " + pvpAll.getName());
                  } else {
                     v.setAllianceNumber(newCap.getId());
                     v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + " and " + newCap.getName() + " is the new capital.");
                     v.addHistory(this.getName(), "left the " + pvpAll.getName() + ", making " + newCap.getName() + " the new capital.");
                  }
               }
            }
         } else {
            Village[] allyArr = this.getAllies();
            boolean alldisb = false;
            this.setAllianceNumber(0);
            if (!pvpAll.exists()) {
               alldisb = true;
               pvpAll.delete();
            }

            for(Village v : allyArr) {
               if (v.getId() != this.getId()) {
                  if (alldisb) {
                     v.broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                     v.setAllianceNumber(0);
                  } else {
                     v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + ".");
                     v.addHistory(this.getName(), "left the " + pvpAll.getName() + ".");
                  }
               }
            }
         }
      }
   }

   public final Reputation setReputation(long wurmid, int val, boolean guest, boolean override) {
      if (WurmId.getType(wurmid) == 0) {
         Long key = new Long(wurmid);
         Reputation r = this.reputations.get(key);
         if (r != null) {
            r.setValue(val, override);
            if (r.getValue() == 0) {
               this.reputations.remove(key);
               r = null;
            }
         } else if (val != 0) {
            r = new Reputation(wurmid, this.id, false, val, guest, false);
            this.reputations.put(key, r);
         }

         if (val <= -30) {
            try {
               Creature cret = Server.getInstance().getCreature(wurmid);
               this.checkIfRaiseAlert(cret);
            } catch (NoSuchPlayerException var9) {
            } catch (NoSuchCreatureException var10) {
            }
         } else {
            this.removeTarget(wurmid, true);
         }

         return r;
      } else {
         return null;
      }
   }

   public final void modifyReputation(long wurmid, int val, boolean guest) {
      if (WurmId.getType(wurmid) == 0) {
         Long key = new Long(wurmid);
         Reputation r = this.reputations.get(key);
         if (r != null) {
            r.modify(val);
            if (r.getValue() == 0) {
               this.reputations.remove(key);
               r = null;
            }
         } else if (val != 0) {
            r = new Reputation(wurmid, this.id, false, val, guest, false);
            this.reputations.put(key, r);
         }

         if (r != null && r.getValue() <= -30) {
            try {
               Creature cret = Server.getInstance().getCreature(wurmid);
               this.checkIfRaiseAlert(cret);
            } catch (NoSuchPlayerException var8) {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            } catch (NoSuchCreatureException var9) {
               logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
            }
         } else {
            this.removeTarget(wurmid, false);
         }
      }
   }

   public final Reputation getReputationObject(long creatureId) {
      return this.reputations.get(new Long(creatureId));
   }

   public final void modifyUpkeep(long upkeepMod) throws IOException {
      this.setUpkeep(upkeepMod + this.upkeep);
   }

   public final boolean isHighwayFound() {
      return this.settings.hasPermission(Village.VillagePermissions.HIGHWAY_OPT_IN.getBit());
   }

   public final boolean isKosAllowed() {
      return this.settings.hasPermission(Village.VillagePermissions.ALLOW_KOS.getBit());
   }

   public final boolean isHighwayAllowed() {
      return this.settings.hasPermission(Village.VillagePermissions.ALLOW_HIGHWAYS.getBit());
   }

   public final void setIsHighwayFound(boolean highwayFound) {
      this.settings.setPermissionBit(Village.VillagePermissions.HIGHWAY_OPT_IN.getBit(), highwayFound);
   }

   public final void setIsKosAllowed(boolean kosAlloed) {
      this.settings.setPermissionBit(Village.VillagePermissions.ALLOW_KOS.getBit(), kosAlloed);
   }

   public final void setIsHighwayAllowed(boolean highwayAllowed) {
      this.settings.setPermissionBit(Village.VillagePermissions.ALLOW_HIGHWAYS.getBit(), highwayAllowed);
   }

   public final boolean acceptsMerchants() {
      return this.acceptsMerchants;
   }

   public final HistoryEvent[] getHistoryEvents() {
      return this.history.toArray(new HistoryEvent[this.history.size()]);
   }

   public final boolean twitChat() {
      return this.twitChat;
   }

   public final int getHistorySize() {
      return this.history.size();
   }

   public final String[] getHistoryAsStrings(int numevents) {
      String[] hist = new String[0];
      if (this.history.size() > 0) {
         int numbersToFetch = Math.min(numevents, this.history.size());
         hist = new String[numbersToFetch];
         HistoryEvent[] events = this.getHistoryEvents();

         for(int x = 0; x < numbersToFetch; ++x) {
            hist[x] = events[x].getLongDesc();
         }
      }

      return hist;
   }

   abstract int create() throws IOException;

   abstract void delete() throws IOException;

   abstract void save();

   abstract void loadCitizens();

   abstract void loadVillageMapAnnotations();

   abstract void loadVillageRecruitees();

   abstract void deleteVillageMapAnnotations();

   public abstract void setMayor(String var1) throws IOException;

   public abstract void setDisbandTime(long var1) throws IOException;

   public abstract void setLogin();

   public abstract void setDisbander(long var1) throws IOException;

   public abstract void setName(String var1) throws IOException;

   abstract void setStartX(int var1) throws IOException;

   abstract void setEndX(int var1) throws IOException;

   abstract void setStartY(int var1) throws IOException;

   abstract void setEndY(int var1) throws IOException;

   public abstract void setDemocracy(boolean var1) throws IOException;

   abstract void setDeedId(long var1) throws IOException;

   public abstract void setTokenId(long var1) throws IOException;

   abstract void loadRoles();

   abstract void loadGuards();

   abstract void loadReputations();

   public abstract void setMotto(String var1) throws IOException;

   abstract void setUpkeep(long var1) throws IOException;

   public abstract void setUnlimitedCitizens(boolean var1) throws IOException;

   public abstract void setMotd(String var1) throws IOException;

   public abstract void saveSettings() throws IOException;

   abstract void loadHistory();

   public abstract void addHistory(String var1, String var2);

   abstract void saveRecruitee(VillageRecruitee var1);

   abstract void setMaxcitizens(int var1);

   @Override
   public final String toString() {
      return "Village [ID: "
         + this.id
         + ", Name: "
         + this.name
         + ", DeedId: "
         + this.deedid
         + ", Kingdom: "
         + Kingdoms.getNameFor(this.kingdom)
         + ", Size: "
         + (this.endx - this.startx) / 2
         + ']';
   }

   public final void putGuardsAtToken() {
      Guard[] guardarr = this.getGuards();

      try {
         for(int x = 0; x < guardarr.length; ++x) {
            guardarr[x].getCreature().blinkTo(this.getToken().getTileX(), this.getToken().getTileY(), this.getToken().isOnSurface() ? 0 : -1, 0);
         }
      } catch (Exception var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
      }
   }

   public final boolean allowsAggCreatures() {
      return this.allowsAggCreatures;
   }

   public abstract void setAcceptsMerchants(boolean var1) throws IOException;

   public abstract void setAllowsAggroCreatures(boolean var1) throws IOException;

   public abstract void setPerimeter(int var1) throws IOException;

   public abstract void setKingdom(byte var1) throws IOException;

   public abstract void setKingdom(byte var1, boolean var2) throws IOException;

   public int getDiameterX() {
      return this.endx - this.startx + 1;
   }

   public int getDiameterY() {
      return this.endy - this.starty + 1;
   }

   public int getMaxGuards() {
      return GuardPlan.getMaxGuards(this.getDiameterX(), this.getDiameterY());
   }

   public int getNumTiles() {
      return this.getDiameterX() * this.getDiameterY();
   }

   public final float getNumCreaturesNotHuman() {
      float found = 0.0F;

      for(int x = this.getStartX(); x <= this.getEndX(); ++x) {
         for(int y = this.getStartY(); y <= this.getEndY(); ++y) {
            found += this.getNumCreaturesNotHumanOn(x, y, true, false);
            found += this.getNumCreaturesNotHumanOn(x, y, false, false);
         }
      }

      return found;
   }

   public final float getNumBrandedCreaturesNotHuman() {
      float found = 0.0F;

      for(int x = this.getStartX(); x <= this.getEndX(); ++x) {
         for(int y = this.getStartY(); y <= this.getEndY(); ++y) {
            found += this.getNumCreaturesNotHumanOn(x, y, true, true);
            found += this.getNumCreaturesNotHumanOn(x, y, false, true);
         }
      }

      return found;
   }

   private float getNumCreaturesNotHumanOn(int x, int y, boolean onSurface, boolean findBranded) {
      float found = 0.0F;
      VolaTile t = Zones.getTileOrNull(x, y, onSurface);
      if (t != null && t.getVillage() == this) {
         Creature[] crets = t.getCreatures();

         for(Creature c : crets) {
            if (!c.isHuman() && (c.isAnimal() || c.isMonster())) {
               if (findBranded && c.isBrandedBy(this.getId())) {
                  ++found;
               } else if (!findBranded) {
                  ++found;
               }
            }
         }

         Item[] items = t.getItems();

         for(Item i : items) {
            if (i.getTemplateId() == 1311 && !i.isEmpty(true)) {
               ++found;
            }

            if (i.isVehicle()) {
               for(Item v : i.getAllItems(true)) {
                  if (v.getTemplateId() == 1311 && !v.isEmpty(true)) {
                     ++found;
                  }
               }
            }

            if (i.getTemplateId() == 1432) {
               for(Item item : i.getAllItems(true)) {
                  if (item.getTemplateId() == 1436 && !item.isEmpty(true)) {
                     Item[] chickens = item.getAllItems(true);

                     for(int z = 0; z < chickens.length; ++z) {
                        ++found;
                     }
                  }
               }
            }
         }
      }

      return found;
   }

   public final float getCreatureRatio() {
      return (float)this.getNumTiles() / this.getNumCreaturesNotHuman();
   }

   public int getPerimeterDiameterX() {
      return this.getDiameterX() + 5 + 5 + this.perimeterTiles * 2;
   }

   public int getPerimeterDiameterY() {
      return this.getDiameterY() + 5 + 5 + this.perimeterTiles * 2;
   }

   public int getMaxCitizens() {
      return this.getNumTiles() / 11;
   }

   public final String getConsumerKey() {
      return this.consumerKeyToUse;
   }

   public final String getConsumerSecret() {
      return this.consumerSecretToUse;
   }

   public final String getApplicationToken() {
      return this.applicationToken;
   }

   public final String getApplicationSecret() {
      return this.applicationSecret;
   }

   public int getPerimeterNonFreeTiles() {
      return this.getPerimeterDiameterX() * this.getPerimeterDiameterY() - (this.getDiameterX() + 5 + 5) * (this.getDiameterY() + 5 + 5);
   }

   public int compareTo(Village aVillage) {
      return this.getName().compareTo(aVillage.getName());
   }

   public void convertOfflineCitizensToKingdom(byte newKingdom, boolean updateTimeStamp) {
      Citizen[] citiz = this.getCitizens();

      for(Citizen c : citiz) {
         if (WurmId.getType(c.getId()) == 0) {
            try {
               Players.getInstance().getPlayer(c.getId());
            } catch (NoSuchPlayerException var10) {
               PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(c.getId());
               if (updateTimeStamp) {
                  pinf.setChangedKingdom();
               }

               Players.convertPlayerToKingdom(c.getId(), newKingdom);
            }
         }
      }
   }

   public void convertTowersWithinDistance(int distance) {
      int sx = Zones.safeTileX(this.getStartX() - this.getPerimeterSize() - 5 - distance);
      int ex = Zones.safeTileX(this.getEndX() + this.getPerimeterSize() + 5 + distance);
      int sy = Zones.safeTileY(this.getStartY() - this.getPerimeterSize() - 5 - distance);
      int ey = Zones.safeTileY(this.getEndY() + this.getPerimeterSize() + 5 + distance);
      Kingdoms.convertTowersWithin(sx, sy, ex, ey, this.kingdom);
   }

   public void convertTowersWithinPerimeter() {
      int sx = Zones.safeTileX(this.getStartX() - this.getPerimeterSize() - 5);
      int ex = Zones.safeTileX(this.getEndX() + this.getPerimeterSize() + 5);
      int sy = Zones.safeTileY(this.getStartY() - this.getPerimeterSize() - 5);
      int ey = Zones.safeTileY(this.getEndY() + this.getPerimeterSize() + 5);
      Kingdoms.convertTowersWithin(sx, sy, ex, ey, this.kingdom);
   }

   public void convertToKingdom(byte newKingdom, boolean convertOnlyCitizens, boolean setTimeStamp) {
      if (newKingdom != this.kingdom) {
         try {
            this.leavePvPAlliance();
            byte oldKingdom = this.kingdom;
            this.setKingdom(newKingdom, setTimeStamp);
            int sx = Zones.safeTileX(this.getStartX() - this.getPerimeterSize() - 5);
            int ex = Zones.safeTileX(this.getEndX() + this.getPerimeterSize() + 5);
            int sy = Zones.safeTileY(this.getStartY() - this.getPerimeterSize() - 5);
            int ey = Zones.safeTileY(this.getEndY() + this.getPerimeterSize() + 5);
            Kingdoms.convertTowersWithin(sx, sy, ex, ey, newKingdom);

            for(int x = sx; x < ex; ++x) {
               for(int y = sy; y < ey; ++y) {
                  this.convertCreatures(oldKingdom, newKingdom, x, y, true, convertOnlyCitizens, setTimeStamp);
                  this.convertCreatures(oldKingdom, newKingdom, x, y, false, convertOnlyCitizens, setTimeStamp);
               }
            }
         } catch (IOException var11) {
            logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         }
      }
   }

   public boolean convertCreatures(byte oldkingdom, byte newkingdom, int x, int y, boolean tsurfaced, boolean convertOnlyCitizens, boolean setTimeStamp) {
      VolaTile t = Zones.getTileOrNull(x, y, tsurfaced);
      if (t != null) {
         Creature[] crets = t.getCreatures();
         if (crets.length > 0) {
            for(int c = 0; c < crets.length; ++c) {
               if (crets[c].getKingdomId() == oldkingdom) {
                  try {
                     boolean convertedMayor = false;
                     Citizen mayor = this.getMayor();
                     if (mayor != null && crets[c].getWurmId() == mayor.getId()) {
                        try {
                           mayor.role = this.getRoleForStatus((byte)3);
                        } catch (NoSuchRoleException var17) {
                           logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
                        }

                        convertedMayor = true;
                     }

                     if (!crets[c].isPlayer() || !convertOnlyCitizens || this.isCitizen(crets[c])) {
                        crets[c].setKingdomId(newkingdom, true, setTimeStamp);
                     }

                     if (crets[c].isKingdomGuard()) {
                        GuardTower tower = Kingdoms.getTower(crets[c]);
                        if (tower != null && tower.getTower().getAuxData() != newkingdom) {
                           Kingdoms.removeInfluenceForTower(tower.getTower());
                           tower.getTower().setAuxData(newkingdom);
                           Kingdom k = Kingdoms.getKingdom(newkingdom);
                           if (k != null) {
                              String aName = k.getName() + " guard tower";
                              tower.getTower().setName(aName);
                           }

                           Kingdoms.addTowerKingdom(tower.getTower());
                           tower.getTower().updateIfGroundItem();
                        }
                     }

                     if (convertedMayor) {
                        if (mayor != null) {
                           try {
                              mayor.role = this.getRoleForStatus((byte)2);
                           } catch (NoSuchRoleException var16) {
                              logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                           }
                        } else {
                           logger.log(Level.WARNING, "Mayor role became null while converting.");
                        }
                     }
                  } catch (IOException var18) {
                     logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
                  }
               }
            }
         }
      }

      return true;
   }

   private final Twit createTwit(String message) {
      return this.canTwit
         ? new Twit(this.name, message, this.consumerKeyToUse, this.consumerSecretToUse, this.applicationToken, this.applicationSecret, true)
         : null;
   }

   public final void twit(String message) {
      if (this.isTwitEnabled()) {
         Twit t = this.createTwit(message);
         if (t != null) {
            Twit.twit(t);
         }
      }
   }

   public final boolean isTwitEnabled() {
      return this.twitEnabled;
   }

   public float getFaithWarValue() {
      return this.faithWar;
   }

   public float getFaithHealValue() {
      return this.faithHeal;
   }

   public float getFaithCreateValue() {
      return this.faithCreate;
   }

   public float getFaithWarBonus() {
      return Math.min(30.0F, this.faithWar / this.faithDivideVal);
   }

   public float getFaithHealBonus() {
      return Math.min(30.0F, this.faithHeal / this.faithDivideVal);
   }

   public float getFaithCreateBonus() {
      return Math.min(30.0F, this.faithCreate / this.faithDivideVal);
   }

   public byte getSpawnSituation() {
      return !this.isCapital() && !this.isPermanent ? this.spawnSituation : 1;
   }

   public int getAllianceNumber() {
      return this.allianceNumber;
   }

   public void addHotaWin() {
      for(Citizen citizen : this.citizens.values()) {
         if (citizen.isPlayer()) {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(citizen.getId());
            if (pinf != null) {
               pinf.setHotaWins((short)(pinf.getHotaWins() + 1));
            }
         }
      }

      this.setHotaWins((short)(this.hotaWins + 1));
   }

   public final void createHotaPrize(int winStreak) {
      try {
         Item statue = ItemFactory.createItem(742, 99.0F, null);
         byte material = 7;
         if (winStreak > 50) {
            material = 56;
         } else if (winStreak > 40) {
            material = 57;
         } else if (winStreak > 30) {
            material = 54;
         } else if (winStreak > 15) {
            material = 52;
         }

         statue.setMaterial(material);
         float posX = this.getToken().getPosX() - 2.0F + Server.rand.nextFloat() * 4.0F;
         float posY = this.getToken().getPosY() - 2.0F + Server.rand.nextFloat() * 4.0F;
         statue.setPosXYZRotation(posX, posY, Zones.calculateHeight(posX, posY, true), (float)Server.rand.nextInt(350));

         for(int i = 0; i < winStreak; ++i) {
            if (i / 11 == winStreak % 11) {
               statue.setAuxData((byte)0);
               statue.setData1(1);
            } else {
               statue.setAuxData((byte)winStreak);
            }
         }

         int r = winStreak * 50 & 0xFF;
         int g = 0;
         int b = 0;
         if (winStreak > 5 && winStreak < 16) {
            r = 0;
         }

         if (winStreak > 5 && winStreak < 20) {
            g = winStreak * 50 & 0xFF;
         }

         if (winStreak > 5 && winStreak < 30) {
            b = winStreak * 50 & 0xFF;
         }

         if (winStreak >= 30) {
            g = winStreak * 80 & 0xFF;
            b = winStreak * 120 & 0xFF;
         }

         statue.setColor(WurmColor.createColor(r, g, b));
         statue.getColor();
         Zone z = Zones.getZone(statue.getTileX(), statue.getTileY(), true);
         int numHelpers = 0;

         for(Citizen c : this.citizens.values()) {
            if (Hota.getHelpValue(c.getId()) > 0) {
               ++numHelpers;
            }
         }

         numHelpers = Math.min(20, numHelpers);

         for(int x = 0; x < numHelpers; ++x) {
            Item medallion = ItemFactory.createItem(740, (float)Math.min(99, 80 + winStreak), null);
            medallion.setAuxData((byte)winStreak);
            if (winStreak > 40) {
               medallion.setMaterial((byte)57);
            } else if (winStreak > 30) {
               medallion.setMaterial((byte)56);
            } else if (winStreak > 20) {
               medallion.setMaterial((byte)54);
            } else if (winStreak > 10) {
               medallion.setMaterial((byte)52);
            }

            statue.insertItem(medallion);
         }

         for(int x = 0; x < 5; ++x) {
            Item lump = ItemFactory.createItem(694, (float)Math.min(99, 50 + winStreak), null);
            statue.insertItem(lump);
         }

         for(int x = 0; x < 5; ++x) {
            Item lump = ItemFactory.createItem(698, (float)Math.min(99, 50 + winStreak), null);
            statue.insertItem(lump);
         }

         z.addItem(statue);
      } catch (Exception var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      }
   }

   public final int getHotaWins() {
      return this.hotaWins;
   }

   public final boolean mayChangeName() {
      return System.currentTimeMillis() - this.lastChangedName > (Servers.localServer.testServer ? 60000L : 14515200000L);
   }

   public abstract void setTwitCredentials(String var1, String var2, String var3, String var4, boolean var5, boolean var6);

   public abstract void setFaithCreate(float var1);

   public abstract void setFaithWar(float var1);

   public abstract void setFaithHeal(float var1);

   public abstract void setSpawnSituation(byte var1);

   public abstract void setAllianceNumber(int var1);

   public abstract void setHotaWins(short var1);

   public abstract void setLastChangedName(long var1);

   public abstract void setVillageRep(int var1);

   public final long getAvailablePlanMoney() {
      return this.plan.moneyLeft < 30000L ? 0L : this.plan.moneyLeft - 30000L;
   }

   public final int getSettings() {
      return this.settings.getPermissions();
   }

   public final int getVillageReputation() {
      return this.villageReputation;
   }

   public final boolean hasBadReputation() {
      return this.villageReputation >= 50;
   }

   public final List<Citizen> getTraders() {
      List<Citizen> toReturn = new ArrayList<>();

      for(Citizen citizen : this.citizens.values()) {
         if (WurmId.getType(citizen.wurmId) == 1) {
            try {
               Creature c = Creatures.getInstance().getCreature(citizen.wurmId);
               if (c.isNpcTrader()) {
                  toReturn.add(citizen);
               }
            } catch (NoSuchCreatureException var5) {
            }
         }
      }

      return toReturn;
   }

   public final boolean addVillageRecruitee(String pName, long pId) {
      VillageRecruitee newRecruit = new VillageRecruitee(this.getId(), pId, pName);
      if (this.addVillageRecruitee(newRecruit)) {
         this.saveRecruitee(newRecruit);
         return true;
      } else {
         return false;
      }
   }

   public final boolean removeRecruitee(long wid) {
      for(VillageRecruitee vr : this.recruitees) {
         if (vr.getRecruiteeId() == wid) {
            this.deleteRecruitee(vr);
            return this.recruitees.remove(vr);
         }
      }

      return false;
   }

   abstract void deleteRecruitee(VillageRecruitee var1);

   public final VillageRecruitee[] getRecruitees() {
      VillageRecruitee[] array = new VillageRecruitee[this.recruitees.size()];
      return this.recruitees.toArray(array);
   }

   public final boolean joinVillage(Player player) {
      VillageRecruitee vr = this.getRecruiteeById(player.getWurmId());
      if (vr == null) {
         player.getCommunicator().sendNormalServerMessage("You are not on the village recruitment list.");
         return false;
      } else if (player.getCitizenVillage() != null && player.getCitizenVillage().isMayor(player.getWurmId())) {
         player.getCommunicator().sendNormalServerMessage("You may not join a village while being the mayor of another village.");
         return false;
      } else if (player.isPlayer() && player.mayChangeVillageInMillis() > 0L) {
         player.getCommunicator()
            .sendNormalServerMessage("You may not change village until " + Server.getTimeFor(player.mayChangeVillageInMillis()) + " has elapsed.");
         return false;
      } else if (this.kingdom != player.getKingdomId()) {
         player.getCommunicator().sendNormalServerMessage("You must be of the same kingdom as the village you are trying to join.");
         return false;
      } else {
         try {
            this.addCitizen(player, this.getRoleForStatus((byte)3));
            if (player.canUseFreeVillageTeleport()) {
               VillageTeleportQuestion vtq = new VillageTeleportQuestion(player);
               vtq.sendQuestion();
            }

            this.removeRecruitee(player.getWurmId());
            return true;
         } catch (IOException var4) {
            logger.log(Level.INFO, "Failed to add " + player.getName() + " to settlement " + this.getName() + "." + var4.getMessage(), (Throwable)var4);
            player.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
         } catch (NoSuchRoleException var5) {
            logger.log(Level.INFO, "Failed to add " + player.getName() + " to settlement " + this.getName() + "." + var5.getMessage(), (Throwable)var5);
            player.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
         }

         return false;
      }
   }

   protected final boolean addVillageRecruitee(VillageRecruitee recruitee) {
      return this.recruiteeExists(recruitee) ? false : this.recruitees.add(recruitee);
   }

   private final VillageRecruitee getRecruiteeById(long wid) {
      for(VillageRecruitee vr : this.recruitees) {
         if (vr.getRecruiteeId() == wid) {
            return vr;
         }
      }

      return null;
   }

   private final boolean recruiteeExists(VillageRecruitee recruitee) {
      for(VillageRecruitee vr : this.recruitees) {
         if (vr.getRecruiteeId() == recruitee.getRecruiteeId()) {
            return true;
         }
      }

      return false;
   }

   public final boolean addVillageMapAnnotation(MapAnnotation annotation, boolean send) {
      if (this.villageMapAnnotations.size() < 500) {
         this.villageMapAnnotations.add(annotation);
         if (send) {
            this.sendMapAnnotationsToVillagers(new MapAnnotation[]{annotation});
         }

         return true;
      } else {
         return false;
      }
   }

   public void removeVillageMapAnnotation(MapAnnotation annotation) {
      if (this.villageMapAnnotations.contains(annotation)) {
         this.villageMapAnnotations.remove(annotation);

         try {
            MapAnnotation.deleteAnnotation(annotation.getId());
            this.sendRemoveMapAnnotationToVillagers(annotation);
         } catch (IOException var3) {
            logger.log(Level.WARNING, "Error when deleting annotation: " + annotation.getId() + " : " + var3.getMessage(), (Throwable)var3);
         }
      }
   }

   public final Set<MapAnnotation> getVillageMapAnnotations() {
      return this.villageMapAnnotations;
   }

   public final MapAnnotation[] getVillageMapAnnotationsArray() {
      if (this.villageMapAnnotations != null && this.villageMapAnnotations.size() != 0) {
         MapAnnotation[] annotations = new MapAnnotation[this.villageMapAnnotations.size()];
         this.villageMapAnnotations.toArray(annotations);
         return annotations;
      } else {
         return null;
      }
   }

   public void sendMapAnnotationsToVillagers(MapAnnotation[] annotations) {
      if (this.group != null && annotations != null) {
         this.group.sendMapAnnotation(annotations);
      }
   }

   public void sendRemoveMapAnnotationToVillagers(MapAnnotation annotation) {
      if (this.group != null) {
         this.group.sendRemoveMapAnnotation(annotation);
      }
   }

   public void sendClearMapAnnotationsOfType(byte type) {
      if (this.group != null) {
         this.group.sendClearMapAnnotationsOfType(type);
      }
   }

   public final long getCreationDate() {
      return this.creationDate;
   }

   private short[] calcOutsideSpawn() {
      logger.info("Calculating outside spawn for " + this.getName());
      boolean surfaced = this.isOnSurface();
      if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
         return new short[]{(short)(this.getStartX() - 5), (short)(this.getStartY() - 5)};
      } else if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
         return new short[]{(short)(this.getEndX() + 5), (short)(this.getStartY() - 5)};
      } else if (Zones.isGoodTileForSpawn(this.getEndX() + 5, this.getStartY() - 5, surfaced)) {
         return new short[]{(short)(this.getEndX() + 5), (short)(this.getEndY() + 5)};
      } else if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
         return new short[]{(short)(this.getStartX() - 5), (short)(this.getEndY() + 5)};
      } else {
         int tilex = this.getStartX() - 5;
         int tiley = this.getStartY() - 5;

         for(int x = 1; x < 20; ++x) {
            if (Zones.isGoodTileForSpawn(tilex - x, tiley, surfaced)) {
               return new short[]{(short)(tilex - x), (short)tiley};
            }
         }

         for(int y = 1; y < 20; ++y) {
            if (Zones.isGoodTileForSpawn(tilex, tiley - y, surfaced)) {
               return new short[]{(short)tilex, (short)(tiley - y)};
            }
         }

         tilex = this.getEndX() + 5;
         tiley = this.getEndY() + 5;

         for(int x = 1; x < 20; ++x) {
            if (Zones.isGoodTileForSpawn(tilex + x, tiley, surfaced)) {
               return new short[]{(short)(tilex + x), (short)tiley};
            }
         }

         for(int y = 1; y < 20; ++y) {
            if (Zones.isGoodTileForSpawn(tilex, tiley + y, surfaced)) {
               return new short[]{(short)tilex, (short)(tiley + y)};
            }
         }

         tilex = this.getEndX() + 5;
         tiley = this.getStartY() - 5;

         for(int x = 1; x < 20; ++x) {
            if (Zones.isGoodTileForSpawn(tilex + x, tiley, surfaced)) {
               return new short[]{(short)(tilex + x), (short)tiley};
            }
         }

         for(int y = 1; y < 20; ++y) {
            if (Zones.isGoodTileForSpawn(tilex, tiley - y, surfaced)) {
               return new short[]{(short)tilex, (short)(tiley - y)};
            }
         }

         tilex = this.getStartX() - 5;
         tiley = this.getEndY() + 5;

         for(int x = 1; x < 20; ++x) {
            if (Zones.isGoodTileForSpawn(tilex - x, tiley, surfaced)) {
               return new short[]{(short)(tilex - x), (short)tiley};
            }
         }

         for(int y = 1; y < 20; ++y) {
            if (Zones.isGoodTileForSpawn(tilex, tiley + y, surfaced)) {
               return new short[]{(short)tilex, (short)(tiley + y)};
            }
         }

         return new short[]{-1, -1};
      }
   }

   public short[] getOutsideSpawn() {
      if (this.outsideSpawn == null || !Zones.isGoodTileForSpawn(this.outsideSpawn[0], this.outsideSpawn[1], this.isOnSurface())) {
         this.outsideSpawn = this.calcOutsideSpawn();
         if (!Zones.isGoodTileForSpawn(this.outsideSpawn[0], this.outsideSpawn[1], this.isOnSurface())) {
            logger.warning("Could not find outside spawn point for " + this.getName());
         }
      }

      return this.outsideSpawn;
   }

   public boolean hasHighway() {
      for(Item marker : Items.getMarkers()) {
         if (this.coversPlus(marker.getTileX(), marker.getTileY(), 2)) {
            return true;
         }
      }

      return false;
   }

   public static enum VillagePermissions implements Permissions.IPermission {
      HIGHWAY_OPT_IN(0, "Village", "Highway Opt-in"),
      ALLOW_KOS(1, "Village", "Allow KOS"),
      ALLOW_HIGHWAYS(2, "Village", "Allow Highways"),
      SPARE03(3, "Unknown", "Spare"),
      SPARE04(4, "Unknown", "Spare"),
      SPARE05(5, "Unknown", "Spare"),
      SPARE06(6, "Unknown", "Spare"),
      SPARE07(7, "Unknown", "Spare");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      private static final Permissions.Allow[] types = Permissions.Allow.values();

      private VillagePermissions(int aBit, String category, String aDescription) {
         this.bit = (byte)aBit;
         this.description = aDescription;
         this.header1 = category;
         this.header2 = "";
      }

      @Override
      public byte getBit() {
         return this.bit;
      }

      @Override
      public int getValue() {
         return 1 << this.bit;
      }

      @Override
      public String getDescription() {
         return this.description;
      }

      @Override
      public String getHeader1() {
         return this.header1;
      }

      @Override
      public String getHeader2() {
         return this.header2;
      }

      @Override
      public String getHover() {
         return "";
      }

      public static Permissions.IPermission[] getPermissions() {
         return types;
      }
   }
}
