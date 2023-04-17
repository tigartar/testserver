/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.DbCitizen;
import com.wurmonline.server.villages.DbGuard;
import com.wurmonline.server.villages.DbGuardPlan;
import com.wurmonline.server.villages.DbVillageRole;
import com.wurmonline.server.villages.Guard;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.RecruitmentAds;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.VillageMessages;
import com.wurmonline.server.villages.VillageRecruitee;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.VillageWar;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.villages.WarDeclaration;
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

public abstract class Village
implements MiscConstants,
VillageStatus,
TimeConstants,
CounterTypes,
MonetaryConstants,
Comparable<Village> {
    public static final int MINIMUM_PERIMETER = 5;
    public static final int ATTACK_PERIMETER = 2;
    public static final byte SPAWN_VILLAGE_ALLIES = 0;
    public static final byte SPAWN_KINGDOM = 1;
    public static final int BADREPUTATION = 50;
    public static final int MAXBADREPUTATION = 150;
    private final Set<Item> oilBarrels = new HashSet<Item>();
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
    public final Map<Long, Guard> guards = new HashMap<Long, Guard>();
    private static final int maxGuardsOnThisServer = Servers.localServer.isChallengeOrEpicServer() ? 4 : 4;
    public long disband = 0L;
    public long disbander = -10L;
    private static final long disbandTime = 86400000L;
    final Map<Long, Reputation> reputations = new HashMap<Long, Reputation>();
    public Set<Long> targets = new HashSet<Long>();
    private Set<MapAnnotation> villageMapAnnotations = new HashSet<MapAnnotation>();
    private Set<VillageRecruitee> recruitees = new HashSet<VillageRecruitee>();
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
    LinkedList<HistoryEvent> history = new LinkedList();
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
    float faithWar = 0.0f;
    float faithHeal = 0.0f;
    float faithCreate = 0.0f;
    float faithDivideVal = 1.0f;
    int allianceNumber = 0;
    short hotaWins = 0;
    protected String motd = "";
    static final Village[] emptyVillages = new Village[0];
    int villageReputation = 0;
    VillageRole everybody = null;
    public long pmkKickDate = 0L;
    private short[] outsideSpawn;
    public final Map<Long, Wagoner> wagoners = new ConcurrentHashMap<Long, Wagoner>();
    long lastSentPmkWarning = 0L;
    boolean detectedBunny = false;
    public static final float OPTIMUMCRETRATIO = Servers.localServer.PVPSERVER ? 5.0f : 15.0f;
    public static final float OFFDEEDCRETRATIO = 10.0f;

    Village(int aStartX, int aEndX, int aStartY, int aEndY, String aName, Creature aFounder, long aDeedId, boolean aSurfaced, boolean aDemocracy, String aMotto, boolean aPermanent, byte aSpawnKingdom, int initialPerimeter) throws NoSuchCreatureException, NoSuchPlayerException, IOException {
        this.citizens = new HashMap<Long, Citizen>();
        this.group = new Group(aName);
        Groups.addGroup(this.group);
        this.roles = new HashMap<Integer, VillageRole>();
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
        this.lastLogin = this.creationDate = System.currentTimeMillis();
        this.deedid = aDeedId;
        this.surfaced = aSurfaced;
        this.democracy = aDemocracy;
        this.motto = aMotto;
        this.isPermanent = aPermanent;
        this.spawnKingdom = aSpawnKingdom;
        this.perimeterTiles = initialPerimeter;
        this.id = this.create();
        this.gates = new HashSet<FenceGate>();
        this.mineDoors = new HashSet<MineDoorPermission>();
        this.createRoles();
    }

    Village(int aId, int aStartX, int aEndX, int aStartY, int aEndY, String aName, String aFounderName, String aMayor, long aDeedId, boolean aSurfaced, boolean aDemocracy, String aDevise, long _creationDate, boolean aHomestead, long aTokenid, long aDisbandTime, long aDisbId, long aLast, byte aKingdom, long aUpkeep, byte aSettings, boolean aAcceptsHomes, boolean aAcceptsMerchants, int aMaxCitizens, boolean aPermanent, byte aSpawnkingdom, int perimetert, boolean allowsAggro, String _consumerKeyToUse, String _consumerSecretToUse, String _applicationToken, String _applicationSecret, boolean _twitChat, boolean _twitEnabled, float _faithWar, float _faithHeal, float _faithCreate, byte _spawnSituation, int _allianceNumber, short _hotaWins, long lastChangeName, String _motd) {
        this.citizens = new HashMap<Long, Citizen>();
        this.group = new Group(aName);
        Groups.addGroup(this.group);
        this.roles = new HashMap<Integer, VillageRole>();
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
        this.gates = new HashSet<FenceGate>();
        this.mineDoors = new HashSet<MineDoorPermission>();
        this.disband = aDisbandTime;
        this.disbander = aDisbId;
        this.lastLogin = aLast;
        this.upkeep = aUpkeep;
        this.settings.setPermissionBits(aSettings & 0xFF);
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
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public boolean canTwit() {
        this.canTwit = false;
        if (this.consumerKeyToUse != null && this.consumerKeyToUse.length() > 5 && this.consumerSecretToUse != null && this.consumerSecretToUse.length() > 5 && this.applicationToken != null && this.applicationToken.length() > 5 && this.applicationSecret != null && this.applicationSecret.length() > 5) {
            this.canTwit = true;
        }
        return this.canTwit;
    }

    final void createInitialUpkeepPlan() {
        this.plan = new DbGuardPlan(0, this.id);
    }

    final void initialize() {
        int x;
        Zone[] coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
        for (x = 0; x < coveredZones.length; ++x) {
            coveredZones[x].addVillage(this);
        }
        coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);
        for (x = 0; x < coveredZones.length; ++x) {
            coveredZones[x].addVillage(this);
        }
        this.setKingdomInfluence();
        if (Features.Feature.TOWER_CHAINING.isEnabled()) {
            try {
                InfluenceChain.addTokenToChain(this.kingdom, this.getToken());
            }
            catch (NoSuchItemException e) {
                logger.warning(String.format("Village Initialize Error: No token found for village %s.", this.getName()));
            }
        }
        this.outsideSpawn = this.calcOutsideSpawn();
    }

    public final void setKingdomInfluence() {
        for (int x = this.startx - 5 - this.perimeterTiles; x < this.endx + 5 + this.perimeterTiles; ++x) {
            for (int y = this.starty - 5 - this.perimeterTiles; y < this.endy + 5 + this.perimeterTiles; ++y) {
                Zones.setKingdom(x, y, this.kingdom);
            }
        }
    }

    public final double getSkillModifier() {
        long timeSinceCreated = System.currentTimeMillis() - this.creationDate;
        if (timeSinceCreated > 174182400000L) {
            return 4.0;
        }
        if (timeSinceCreated > 116121600000L) {
            return 3.0;
        }
        if ((double)timeSinceCreated > 1.016064E11) {
            return 2.75;
        }
        if (timeSinceCreated > 87091200000L) {
            return 2.5;
        }
        if ((double)timeSinceCreated > 7.2576E10) {
            return 2.25;
        }
        if (timeSinceCreated > 58060800000L) {
            return 2.0;
        }
        if ((double)timeSinceCreated > 4.35456E10) {
            return 1.75;
        }
        if (timeSinceCreated > 29030400000L) {
            return 1.5;
        }
        if (timeSinceCreated > 21772800000L) {
            return 1.0;
        }
        if (timeSinceCreated > 14515200000L) {
            return 0.5;
        }
        if (timeSinceCreated > 7257600000L) {
            return 0.25;
        }
        return 0.1;
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
        }
        int g = 0;
        if (this.guards != null) {
            g = this.guards.size();
        }
        return this.getMaxCitizens() > this.citizens.size() - g;
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
            for (int x = this.startx; x <= this.endx; ++x) {
                for (int y = this.starty; y <= this.endy; ++y) {
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
            for (int c = 0; c < creatures.length; ++c) {
                if (!this.isEnemy(creatures[c])) continue;
                this.addTarget(creatures[c]);
            }
        }
    }

    public final boolean isEnemy(Creature creature) {
        return this.isEnemy(creature, false);
    }

    public final boolean isEnemy(Creature creature, boolean ignoreInvulnerable) {
        if (creature.isInvulnerable() && !ignoreInvulnerable || creature.isUnique()) {
            return false;
        }
        if (creature.getKingdomId() != 0 && !creature.isFriendlyKingdom(this.kingdom)) {
            return true;
        }
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
        }
        return this.getReputation(creature) <= -30 && this.isWithinMinimumPerimeter(creature.getTileX(), creature.getTileY());
    }

    public final void addTarget(Creature creature) {
        if (creature.isInvulnerable() || creature.isUnique()) {
            return;
        }
        if (creature.getCultist() != null && creature.getCultist().hasFearEffect()) {
            return;
        }
        if (creature.isTransferring()) {
            return;
        }
        if (this.guards.size() > 0) {
            if (!this.isAlerted()) {
                this.setAlerted(true);
                this.broadCastAlert(creature.getName() + " raises the settlement alarm!", (byte)4);
                try {
                    if (this.gates != null && this.gates.size() > 0) {
                        Server.getInstance().broadCastMessage("A horn sounds and the gates are locked. " + this.getName() + " is put on alert!", this.getToken().getTileX(), this.getToken().getTileY(), this.isOnSurface(), this.endx - this.startx);
                    } else {
                        Server.getInstance().broadCastMessage("A horn sounds. " + this.getName() + " is put on alert!", this.getToken().getTileX(), this.getToken().getTileY(), this.isOnSurface(), this.endx - this.startx);
                    }
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "No settlement token for " + this.getName() + ": " + this.tokenId, nsi);
                }
            }
            if (!this.targets.contains(new Long(creature.getWurmId()))) {
                this.targets.add(new Long(creature.getWurmId()));
            }
            this.assignTargets();
        }
    }

    public final void assignTargets() {
        if (this.guards.size() > 0 && this.targets.size() > 0) {
            LinkedList<Guard> g = new LinkedList<Guard>();
            g.addAll(this.guards.values());
            Long[] targs = this.getTargets();
            for (int x = 0; x < targs.length; ++x) {
                int guardsAssigned = 0;
                long targid = targs[x];
                Guard best = null;
                int bestdist = Integer.MAX_VALUE;
                Guard nextBest = null;
                int nextBestdist = Integer.MAX_VALUE;
                Guard thirdBest = null;
                int thirdBestdist = Integer.MAX_VALUE;
                if (g.isEmpty()) continue;
                try {
                    Creature target = Server.getInstance().getCreature(targid);
                    if (!target.isDead()) {
                        if (target.getCurrentTile().getTileX() < this.getStartX() - 5 || target.getCurrentTile().getTileX() > this.getEndX() + 5 || target.getCurrentTile().getTileY() < this.getStartY() - 5 || target.getCurrentTile().getTileY() > this.getEndY() + 5) {
                            this.removeTarget(target.getWurmId(), false);
                            continue;
                        }
                        ListIterator it2 = g.listIterator();
                        while (it2.hasNext()) {
                            int diffy;
                            Guard guard = (Guard)it2.next();
                            if (guard.creature.target == targid) {
                                it2.remove();
                                if (++guardsAssigned < 3) continue;
                                break;
                            }
                            if (guard.creature.target != -10L) continue;
                            int diffx = (int)Math.abs(guard.creature.getPosX() - target.getPosX());
                            int dist = Math.max(diffx, diffy = (int)Math.abs(guard.creature.getPosY() - target.getPosY()));
                            if (dist < bestdist) {
                                best = guard;
                                bestdist = dist;
                                continue;
                            }
                            if (dist < nextBestdist) {
                                nextBest = guard;
                                nextBestdist = dist;
                                continue;
                            }
                            if (dist >= thirdBestdist) continue;
                            thirdBest = guard;
                            thirdBestdist = dist;
                        }
                        if (guardsAssigned >= 3 || best == null) continue;
                        best.creature.setTarget(targid, false);
                        best.creature.say("I'll take care of " + target.getName() + "!");
                        g.remove(best);
                        if (++guardsAssigned < 3 && nextBest != null) {
                            nextBest.creature.setTarget(targid, false);
                            nextBest.creature.say("I'll help you with " + target.getName() + "!");
                            g.remove(nextBest);
                            ++guardsAssigned;
                        }
                        if (guardsAssigned >= 3 || thirdBest == null) continue;
                        thirdBest.creature.setTarget(targid, false);
                        thirdBest.creature.say("I'll help you with " + target.getName() + "!");
                        g.remove(thirdBest);
                        ++guardsAssigned;
                        continue;
                    }
                    this.targets.remove(targid);
                    continue;
                }
                catch (NoSuchCreatureException noSuchCreatureException) {
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    public final boolean isAlerted() {
        return this.alerted;
    }

    public final boolean isCapital() {
        King k = King.getKing(this.kingdom);
        if (k != null) {
            return k.capital.equalsIgnoreCase(this.getName());
        }
        return false;
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
        }
        if (this.guards.size() == 0 && !onDeed) {
            return 0;
        }
        if (this.isPermanent) {
            return 100;
        }
        for (Item i : this.oilBarrels) {
            Item[] contained;
            if (i.isEmpty(false)) continue;
            for (Item liquid : contained = i.getAllItems(false)) {
                if (!liquid.isLiquidInflammable()) continue;
                if (amount >= liquid.getWeightGrams()) {
                    Items.destroyItem(liquid.getWurmId());
                    return liquid.getWeightGrams();
                }
                liquid.setWeight(liquid.getWeightGrams() - amount, true);
                return amount;
            }
        }
        return 0;
    }

    private Long[] getTargets() {
        return this.targets.toArray(new Long[this.targets.size()]);
    }

    public final boolean containsTarget(Creature creature) {
        return this.targets.contains(new Long(creature.getWurmId()));
    }

    public final boolean containsItem(Item item) {
        return item.getZoneId() > 0 && this.getStartX() <= item.getTileX() && this.getEndX() >= item.getTileX() && this.getStartY() <= item.getTileY() && this.getEndY() >= item.getTileY();
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
        }
        return true;
    }

    public final boolean moreThanMonthLeft() {
        if (!this.isChained()) {
            return false;
        }
        if (this.plan != null) {
            return this.plan.getTimeLeft() > 2419200000L;
        }
        return true;
    }

    final void poll(long now, boolean reduceFaith) {
        boolean disb;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Polling settlement: " + this);
        }
        if (disb = this.plan.poll()) {
            this.disband = now - 1L;
            this.disband(upkeepString);
        } else if (this.checkDisband(now)) {
            disb = true;
            String pname = "Unknown Player";
            try {
                pname = Players.getInstance().getNameFor(this.disbander);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "No name for " + this.disbander, nsp);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "No name for " + this.disbander, iox);
            }
            if (disb) {
                this.disband(pname);
            }
        } else {
            this.faithDivideVal = Math.max(1.0f, Math.min(3.0f, (float)(this.getCitizens().length - this.plan.getNumHiredGuards()) / 2.0f));
            if (reduceFaith) {
                this.setFaithCreate(Math.max(0.0f, this.faithCreate - Math.max(0.01f, this.faithCreate / 15.0f)));
                this.setFaithWar(Math.max(0.0f, this.faithWar - Math.max(0.01f, this.faithWar / 15.0f)));
                this.setFaithHeal(Math.max(0.0f, this.faithHeal - Math.max(0.01f, this.faithHeal / 15.0f)));
            }
            if (WurmCalendar.isEaster() && this.isPermanent && (!Servers.localServer.entryServer || Server.getInstance().isPS()) && !this.detectedBunny) {
                for (Citizen citiz : this.getCitizens()) {
                    try {
                        Creature bunny = Creatures.getInstance().getCreature(citiz.getId());
                        if (bunny.getTemplate().getTemplateId() != 53) continue;
                        this.detectedBunny = true;
                    }
                    catch (NoSuchCreatureException bunny) {
                        // empty catch block
                    }
                }
                if (!this.detectedBunny) {
                    int tilex = this.getCenterX();
                    int tiley = this.getCenterY();
                    boolean ok = false;
                    int tries = 0;
                    while (!ok && tries++ < 100) {
                        VolaTile t;
                        switch (Server.rand.nextInt(4)) {
                            case 0: {
                                tilex = this.getStartX() - (20 + Server.rand.nextInt(40));
                                break;
                            }
                            case 1: {
                                tilex = this.getEndX() + (20 + Server.rand.nextInt(40));
                                break;
                            }
                            case 2: {
                                tiley = this.getEndY() + (20 + Server.rand.nextInt(40));
                                break;
                            }
                            case 3: {
                                tiley = this.getStartY() - (20 + Server.rand.nextInt(40));
                            }
                        }
                        if ((t = Zones.getTileOrNull(tilex, tiley, true)) != null && (t.getFences().length != 0 || t.getStructure() != null) || Tiles.decodeHeight(Zones.getTileIntForTile(tilex, tiley, 0)) <= 0) continue;
                        ok = true;
                    }
                    try {
                        byte sex = 0;
                        if (Server.rand.nextBoolean()) {
                            sex = 1;
                        }
                        Creature bunny = Creature.doNew(53, true, tilex * 4 + 2, tiley * 4 + 2, Server.rand.nextFloat() * 360.0f, 0, "Easter Bunny", sex, (byte)0, (byte)0, false, (byte)1);
                        this.addCitizen(bunny, this.getRole(3));
                        logger.log(Level.INFO, "Created easter bunny for " + this.getName());
                        this.detectedBunny = true;
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, ex.getMessage(), ex);
                    }
                }
            }
        }
        if (this.isLeavingPmk() && System.currentTimeMillis() - this.lastSentPmkWarning > 1800000L) {
            Kingdom pmk = Kingdoms.getKingdom(this.kingdom);
            Kingdom template = Kingdoms.getKingdom(Servers.isThisAChaosServer() ? (byte)4 : (byte)pmk.getTemplate());
            if (pmk != null && pmk.isCustomKingdom()) {
                if (this.checkLeavePmk(System.currentTimeMillis())) {
                    this.lastSentPmkWarning = 0L;
                    this.addHistory(this.getName(), "converts to " + template.getName() + " from " + pmk.getName() + ".");
                    this.broadCastAlert(this.getName() + " leaves " + pmk.getName() + " for " + template.getName() + ".", (byte)4);
                    this.convertToKingdom(Servers.isThisAChaosServer() ? (byte)4 : (byte)pmk.getTemplate(), false, false);
                } else {
                    this.lastSentPmkWarning = System.currentTimeMillis();
                    this.broadCastAlert(this.getName() + " is leaving " + pmk.getName() + " for " + template.getName() + " in " + Server.getTimeFor(this.pmkKickDate - System.currentTimeMillis()) + ".", (byte)4);
                }
            } else {
                this.pmkKickDate = 0L;
            }
        }
        if (this.targets.size() > 0) {
            Long[] targArr = this.getTargets();
            for (int x = 0; x < targArr.length; ++x) {
                try {
                    Creature c = Server.getInstance().getCreature(targArr[x]);
                    VolaTile t = c.getCurrentTile();
                    if (t == null || t.getVillage() == this) continue;
                    this.targets.remove(targArr[x]);
                    continue;
                }
                catch (NoSuchPlayerException nsp) {
                    this.targets.remove(targArr[x]);
                    continue;
                }
                catch (NoSuchCreatureException nsc) {
                    this.targets.remove(targArr[x]);
                }
            }
            if (this.targets.size() == 0) {
                this.setAlerted(false);
                this.broadCastSafe("The danger is over for now.");
            }
        }
        if (now - this.lastPolledReps > 0x6DDD00L) {
            if (this.getVillageReputation() > 0) {
                this.setVillageRep(this.getVillageReputation() - 1);
            }
            Long[] keys = this.reputations.keySet().toArray(new Long[this.reputations.keySet().size()]);
            for (int x = 0; x < keys.length; ++x) {
                Reputation r = this.reputations.get(keys[x]);
                int old = r.getValue();
                if (old >= 0) continue;
                r.modify(1);
                int newr = r.getValue();
                if (newr < 0) continue;
                this.reputations.remove(keys[x]);
            }
            this.lastPolledReps = System.currentTimeMillis();
        }
        if (this.warDeclarations != null) {
            WarDeclaration[] declArr = this.warDeclarations.values().toArray(new WarDeclaration[this.warDeclarations.size()]);
            for (int x = 0; x < declArr.length; ++x) {
                if (now - declArr[x].time <= 86400000L) continue;
                declArr[x].accept();
            }
        }
    }

    public final void removeTarget(long target, boolean ignoreFighting) {
        this.targets.remove(new Long(target));
        Guard[] _guards = this.getGuards();
        boolean fighting = false;
        for (int x = 0; x < _guards.length; ++x) {
            if (_guards[x].creature.target != target) continue;
            if (!_guards[x].creature.isFighting() || ignoreFighting) {
                if (_guards[x].creature.opponent != null && _guards[x].creature.opponent.getWurmId() == target) {
                    _guards[x].creature.opponent.setTarget(-10L, true);
                    _guards[x].creature.opponent.setOpponent(null);
                    _guards[x].creature.setOpponent(null);
                }
                _guards[x].creature.setTarget(-10L, true);
                continue;
            }
            fighting = true;
        }
        if (this.targets.size() == 0 && !fighting) {
            this.setAlerted(false);
        }
    }

    public final void removeTarget(Creature target) {
        this.targets.remove(new Long(target.getWurmId()));
        Guard[] _guards = this.getGuards();
        boolean fighting = false;
        for (int x = 0; x < _guards.length; ++x) {
            if (_guards[x].creature.target != target.getWurmId()) continue;
            if (!_guards[x].creature.isFighting()) {
                _guards[x].creature.setTarget(-10L, true);
                continue;
            }
            fighting = true;
        }
        if (this.targets.size() == 0 && !fighting) {
            this.setAlerted(false);
        }
    }

    private void setAlerted(boolean alert) {
        int n = 0;
        if (alert) {
            for (FenceGate g : this.gates) {
                if (!g.startAlert(n == 0)) continue;
                ++n;
            }
            this.alerted = true;
            if (this.plan != null) {
                this.plan.startSiege();
            }
        } else if (this.alerted) {
            for (FenceGate g : this.gates) {
                if (!g.endAlert(n == 0)) continue;
                ++n;
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
                this.everybody = new DbVillageRole(this.id, "non-citizens", false, false, false, false, false, false, false, false, false, false, false, false, false, false, atknon, false, false, 1, 0, false, false, false, false, false, false, false, false, false, -10L, settings.getPermissions(), moreSettings.getPermissions(), extraSettings.getPermissions());
                this.roles.put(this.everybody.getId(), this.everybody);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
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
        DbVillageRole role = null;
        try {
            Permissions settings = new Permissions();
            Permissions moreSettings = new Permissions();
            Permissions extraSettings = new Permissions();
            settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_CITIZENS.getBit(), true);
            settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), true);
            role = new DbVillageRole(this.id, "guard", false, true, false, true, false, false, false, false, false, true, true, true, false, true, true, true, true, 4, 0, true, false, false, false, false, false, false, false, false, -10L, settings.getPermissions(), moreSettings.getPermissions(), extraSettings.getPermissions());
            this.roles.put(role.getId(), role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to create role guard for settlement " + this.getName() + " " + iox.getMessage(), iox);
        }
        return role;
    }

    private VillageRole createRoleWagoner() {
        DbVillageRole role = null;
        try {
            Permissions settings = new Permissions();
            Permissions moreSettings = new Permissions();
            Permissions extraSettings = new Permissions();
            moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PICKUP.getBit(), true);
            moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.LOAD.getBit(), true);
            moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.UNLOAD.getBit(), true);
            role = new DbVillageRole(this.id, "wagoner", false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false, false, 6, 0, false, false, false, true, false, true, false, false, false, -10L, settings.getPermissions(), moreSettings.getPermissions(), extraSettings.getPermissions());
            this.roles.put(role.getId(), role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to create role wagoner for settlement " + this.getName() + " " + iox.getMessage(), iox);
        }
        return role;
    }

    private VillageRole createRoleCitizen() {
        DbVillageRole role = null;
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
            role = new DbVillageRole(this.id, "citizen", false, true, false, true, false, false, false, false, false, true, true, false, false, false, true, true, true, 3, 0, true, false, true, true, true, true, true, false, false, -10L, settings.getPermissions(), moreSettings.getPermissions(), extraSettings.getPermissions());
            this.roles.put(role.getId(), role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        return role;
    }

    private VillageRole createRoleMayor() {
        String title = "mayor";
        boolean mayinvite = true;
        DbVillageRole role = null;
        try {
            int permissions = -1;
            int morePermissions = -1;
            int extraPermissions = -1;
            role = new DbVillageRole(this.id, "mayor", true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, 2, 0, true, true, true, true, true, true, true, true, true, -10L, -1, -1, -1);
            this.roles.put(role.getId(), role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        return role;
    }

    public final int getReputation(Creature creature) {
        if (creature.getKingdomId() != 0 && !creature.isFriendlyKingdom(this.kingdom)) {
            return -100;
        }
        long wid = creature.getWurmId();
        if (creature.getCitizenVillage() != null && this.isEnemy(creature.getCitizenVillage())) {
            return -100;
        }
        Reputation rep = this.reputations.get(new Long(wid));
        if (rep != null) {
            return rep.getValue();
        }
        return 0;
    }

    public final int getReputation(long wid) {
        Village vill = Villages.getVillageForCreature(wid);
        if (vill != null && this.isEnemy(vill)) {
            return -100;
        }
        Reputation rep = this.reputations.get(new Long(wid));
        if (rep != null) {
            return rep.getValue();
        }
        return 0;
    }

    public final Reputation[] getReputations() {
        return this.reputations.values().toArray(new Reputation[this.reputations.values().size()]);
    }

    protected VillageRole createRoleAlly() {
        DbVillageRole role = null;
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
            role = new DbVillageRole(this.id, "ally", false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, false, 5, 0, false, false, true, false, false, false, false, false, false, -10L, settings.getPermissions(), moreSettings.getPermissions(), extraSettings.getPermissions());
            this.roles.put(role.getId(), role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to create role allied for settlement " + this.getName() + " " + iox.getMessage(), iox);
        }
        return role;
    }

    public final void resetRoles() {
        Citizen mayor = this.getMayor();
        VillageRole[] roleArr = this.getRoles();
        HashSet<Citizen> guardSet = new HashSet<Citizen>();
        HashSet<Citizen> wagonerSet = new HashSet<Citizen>();
        Citizen[] citiz = this.getCitizens();
        for (int y = 0; y < citiz.length; ++y) {
            if (citiz[y].getRole().getStatus() == 4) {
                guardSet.add(citiz[y]);
            }
            if (citiz[y].getRole().getStatus() != 6) continue;
            wagonerSet.add(citiz[y]);
        }
        for (int x = 0; x < roleArr.length; ++x) {
            try {
                roleArr[x].delete();
                continue;
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, this.getName() + " role: " + roleArr[x].getName() + " " + iox.getMessage(), iox);
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
            for (Citizen g : guardSet) {
                try {
                    g.setRole(guardRole);
                }
                catch (IOException iox0) {
                    logger.log(Level.WARNING, this.getName(), iox0);
                }
            }
        }
        if (wagonerSet.size() > 0) {
            wagonerRole = this.createRoleWagoner();
            for (Citizen g : wagonerSet) {
                try {
                    g.setRole(wagonerRole);
                }
                catch (IOException iox0) {
                    logger.log(Level.WARNING, this.getName(), iox0);
                }
            }
        }
        for (int y = 0; y < citiz.length; ++y) {
            if (citiz[y] != mayor) {
                try {
                    boolean addRole = true;
                    if (guardRole != null && citiz[y].getRole() == guardRole) {
                        addRole = false;
                    }
                    if (wagonerRole != null && citiz[y].getRole() == wagonerRole) {
                        addRole = false;
                    }
                    if (!addRole) continue;
                    citiz[y].setRole(citizRole);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, this.getName(), iox);
                }
                continue;
            }
            try {
                citiz[y].setRole(mayorRole);
                continue;
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, this.getName(), iox);
            }
        }
    }

    public final short[] getSpawnPoint() {
        int x = this.getCenterX();
        int y = this.getCenterY();
        for (int tries = 0; tries < 10; ++tries) {
            x = Server.rand.nextInt(this.endx - this.startx) + this.startx;
            VolaTile tile = Zones.getTileOrNull(x, y = Server.rand.nextInt(this.endy - this.starty) + this.starty, this.isOnSurface());
            if (tile != null) continue;
            return new short[]{(short)x, (short)y};
        }
        x = this.getCenterX();
        y = this.getCenterY();
        try {
            Item token = this.getToken();
            x = (int)token.getPosX() >> 2;
            y = (int)token.getPosY() >> 2;
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "No token found for settlement " + this.getName(), nsi);
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
        }
        catch (NoSuchItemException e) {
            return this.startx + (this.endx - this.startx) / 2;
        }
    }

    public final int getTokenY() {
        try {
            Item token = this.getToken();
            return (int)token.getPosY() >> 2;
        }
        catch (NoSuchItemException e) {
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
            }
            catch (NoSuchLockException noSuchLockException) {
                // empty catch block
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
        }
        if (village.kingdom != this.kingdom && !Kingdoms.getKingdom(this.kingdom).isAllied(village.kingdom)) {
            return true;
        }
        if (this.wars != null) {
            return this.wars.get(village) != null;
        }
        return false;
    }

    final void addWar(VillageWar war) {
        Village opponent;
        if (this.wars == null) {
            this.wars = new HashMap<Village, VillageWar>();
        }
        if ((opponent = war.getVilltwo()) == this) {
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
        Kingdom kingd;
        if (village == this) {
            return false;
        }
        if (!Servers.localServer.PVPSERVER) {
            return false;
        }
        if (village.kingdom != this.kingdom) {
            return false;
        }
        if (village.isPermanent) {
            return false;
        }
        if (!(village.getVillageReputation() >= 50 || Servers.isThisAChaosServer() || (kingd = Kingdoms.getKingdom(this.kingdom)).isCustomKingdom() && !this.isCapital())) {
            return false;
        }
        if (this.warDeclarations != null && this.warDeclarations.containsKey(village)) {
            return false;
        }
        return this.wars == null || !this.wars.containsKey(village);
    }

    public final boolean isAtPeaceWith(Village village) {
        if (!Servers.localServer.PVPSERVER) {
            return true;
        }
        if (village.kingdom != this.kingdom) {
            return false;
        }
        if (village.isPermanent) {
            return true;
        }
        if (this.warDeclarations != null && this.warDeclarations.containsKey(village)) {
            return false;
        }
        return this.wars == null || !this.wars.containsKey(village);
    }

    final void addWarDeclaration(WarDeclaration declaration2) {
        if (this.warDeclarations == null) {
            this.warDeclarations = new HashMap<Village, WarDeclaration>();
        }
        boolean wedeclare = false;
        Village opponent = declaration2.declarer;
        if (opponent == this) {
            wedeclare = true;
            opponent = declaration2.receiver;
        }
        if (opponent != this) {
            if (this.warDeclarations.containsKey(opponent)) {
                return;
            }
            this.warDeclarations.put(opponent, declaration2);
            if (this.isAlly(opponent)) {
                if (this.allianceNumber == this.getId()) {
                    opponent.setAllianceNumber(0);
                } else if (opponent.getId() == this.allianceNumber) {
                    this.setAllianceNumber(0);
                }
                if (wedeclare) {
                    this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has declared war with the treacherous " + opponent.getName() + ". Citizens, be strong and brave!");
                } else {
                    this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has been challenged by the treacherous " + opponent.getName() + " and will be forced into war. Citizens, be strong and brave!");
                }
            } else if (wedeclare) {
                this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has declared war with the cowardly " + opponent.getName() + ". Citizens, be strong and brave!");
            } else {
                this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has been challenged by the cowardly " + opponent.getName() + ". Citizens, be strong and brave - war is coming our way!");
            }
            this.addHistory(opponent.getName(), "is now under war declaration");
        } else {
            logger.log(Level.WARNING, "Added declaration to " + this.getName() + " but the war is for " + declaration2.declarer.getName() + " and " + declaration2.receiver.getName() + ". Deleting.");
            declaration2.delete();
        }
    }

    final void startWar(VillageWar war, boolean wedeclare) {
        Village opponent;
        if (this.wars == null) {
            this.wars = new HashMap<Village, VillageWar>();
        }
        if ((opponent = war.getVilltwo()) == this) {
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
                        this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has decided to go to war with the treacherous " + opponent.getName() + ". Citizens, be strong and brave!");
                    } else {
                        this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " was betrayed by the treacherous " + opponent.getName() + " and forced into war. Citizens, be strong and brave!");
                    }
                } else if (wedeclare) {
                    this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has decided to go to war with the cowardly " + opponent.getName() + ". Citizens, be strong and brave!");
                } else {
                    this.broadCastNormal("Under the rule of " + this.getMayor().getName() + ", " + this.getName() + " has been attacked by the cowardly " + opponent.getName() + ". Citizens, be strong and brave - we go to war!");
                }
                this.addHistory(opponent.getName(), "is now a deadly enemy");
            }
        } else {
            logger.log(Level.WARNING, "Added war to " + this.getName() + " but the war is for " + war.getVilltwo().getName() + " and " + war.getVillone().getName() + ". Deleting.");
            war.delete();
        }
    }

    public final boolean isAlly(Village village) {
        if (village != null) {
            return this.allianceNumber > 0 && village.getAllianceNumber() == this.allianceNumber;
        }
        return false;
    }

    public final Village[] getEnemies() {
        if (this.wars != null && this.wars.size() > 0) {
            return this.wars.keySet().toArray(new Village[this.wars.size()]);
        }
        return new Village[0];
    }

    final void declarePeace(Creature breaker, Creature accepter, Village village, boolean webreak) {
        if (this.wars != null || this.warDeclarations != null) {
            if (webreak) {
                this.broadCastNormal("The wise " + breaker.getName() + " has ended the war with " + village.getName() + " through their intermediary " + accepter.getName() + ". Amnesty for all perpetrators is declared.");
                this.addHistory(breaker.getName(), "ends the war with " + village.getName());
            } else {
                this.broadCastNormal(breaker.getName() + " of " + village.getName() + " has been given the grace of peace with " + this.getName() + " through the wise " + accepter.getName() + "! Amnesty for all perpetrators is declared.");
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
        WarDeclaration decl;
        if (this.wars != null) {
            Citizen[] vcitiz = village.getCitizens();
            for (int x = 0; x < vcitiz.length; ++x) {
                this.removeReputation(vcitiz[x].getId());
            }
            this.wars.remove(village);
        }
        if (this.warDeclarations != null && (decl = this.warDeclarations.remove(village)) != null) {
            decl.delete();
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
        PvPAlliance alliance;
        if (this.allianceNumber > 0 && (alliance = PvPAlliance.getPvPAlliance(this.allianceNumber)) != null) {
            return alliance.getName();
        }
        return "";
    }

    public final boolean isAlly(Creature creature) {
        Citizen cit;
        Village village = creature.getCitizenVillage();
        if (village == null) {
            return false;
        }
        if (this.allianceNumber > 0 && village.getAllianceNumber() == this.allianceNumber && (cit = village.getCitizen(creature.getWurmId())) != null) {
            VillageRole vr = cit.getRole();
            return vr != null && vr.mayPerformActionsOnAlliedDeeds();
        }
        return false;
    }

    public final void replaceDeed(Creature performer, Item oldDeed) {
        long oldDeedid = this.deedid;
        long newDeedid = -10L;
        if (oldDeedid != oldDeed.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("This deed is not registered for this settlement called " + this.getName() + ".");
            logger.log(Level.WARNING, this.deedid + " does not match " + oldDeed.getWurmId() + " for " + performer.getName() + " in settlement " + this.getName());
            return;
        }
        long deedVal = oldDeed.getValue();
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
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for new deeds.");
                performer.getCommunicator().sendSafeServerMessage("An error occurred.");
                return;
            }
            Item newDeed = ItemFactory.createItem(663, 50.0f + Server.rand.nextFloat() * 50.0f, performer.getName());
            newDeed.setName("Settlement deed");
            performer.getInventory().insertItem(newDeed, true);
            try {
                newDeed.setDescription(this.getName());
                newDeed.setData2(this.id);
                this.setDeedId(newDeed.getWurmId());
                if (this.gates != null) {
                    for (FenceGate gate : this.gates) {
                        try {
                            Item lock = gate.getLock();
                            lock.addKey(-10L);
                            lock.removeKey(oldDeedid);
                        }
                        catch (NoSuchLockException noSuchLockException) {}
                    }
                }
                performer.addKey(newDeed, false);
                this.plan.hiredGuardNumber = 50;
                int newNum = 0;
                this.plan.changePlan(0, newNum);
                newNum = 3;
                this.plan.changePlan(0, newNum);
                try {
                    if (!this.getRoleForStatus((byte)2).mayInviteCitizens()) {
                        logger.log(Level.INFO, "Set mayor to be able to invite for " + this.getName());
                        this.getRoleForStatus((byte)2).setMayInvite(true);
                    }
                }
                catch (NoSuchRoleException nsr) {
                    logger.log(Level.INFO, "Failed to find mayo role to invite for " + performer.getName());
                }
                performer.getCommunicator().sendAlertServerMessage("You will be set to " + newNum + " heavy guards. You need to manage guards in order to make sure you have the desired amount.");
            }
            catch (IOException iox) {
                performer.getCommunicator().sendNormalServerMessage("A server error occured while saving the new deed id.");
                logger.log(Level.WARNING, iox.getMessage(), iox);
                return;
            }
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
            performer.getCommunicator().sendSafeServerMessage("An error occurred when creating the new deed.");
            return;
        }
        catch (NoSuchTemplateException snt) {
            logger.log(Level.WARNING, snt.getMessage(), snt);
            performer.getCommunicator().sendSafeServerMessage("An error occurred when creating the new deed.");
            return;
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
        for (int x = 0; x < zones.length; ++x) {
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
            }
            catch (NoSuchLockException noSuchLockException) {
                // empty catch block
            }
        }
    }

    final void addMineDoors() {
        Zone[] zones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, this.isOnSurface());
        for (int x = 0; x < zones.length; ++x) {
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
        VillageRole[] toReturn = this.roles.values().toArray(new VillageRole[this.roles.size()]);
        return toReturn;
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
        return new Message(null, 3, "Village", "Village:" + left, 250, 150, 250);
    }

    protected final Message getMotdMessage() {
        return new Message(null, 3, "Village", "MOTD:" + this.motd, 250, 150, 250);
    }

    protected final Message getRepMessage(String toSend) {
        return new Message(null, 3, "Village", toSend);
    }

    public boolean isChained() {
        if (!Features.Feature.TOWER_CHAINING.isEnabled()) {
            return true;
        }
        try {
            Item token = this.getToken();
            return token.isChained();
        }
        catch (NoSuchItemException e) {
            logger.warning(String.format("Village Error: No token found for village %s.", this.getName()));
            return true;
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
        }
        catch (NoSuchRoleException nsr) {
            role = this.createRoleGuard();
        }
        try {
            this.addCitizen(creature, role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to add guard as citizen for settlement " + this.getName() + " " + iox.getMessage(), iox);
        }
        DbGuard guard = new DbGuard(this.id, creature, expireDate);
        ((Guard)guard).save();
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
        }
        catch (NoSuchRoleException nsr) {
            role = this.createRoleWagoner();
        }
        try {
            this.addCitizen(creature, role);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to add wagoner as citizen for settlement " + this.getName() + " " + iox.getMessage(), iox);
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
            }
            catch (NoSuchRoleException nsrx) {
                logger.log(Level.WARNING, "Cannot find role for wagoner so cannot remove it for settlement " + this.getName() + " " + nsrx.getMessage(), nsrx);
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
        }
        return toReturn;
    }

    public final VillageRole getRoleForStatus(byte status) throws NoSuchRoleException {
        for (VillageRole role : this.roles.values()) {
            if (role.getStatus() != status) continue;
            return role;
        }
        throw new NoSuchRoleException("No role with status " + status);
    }

    public final VillageRole getRoleForVillage(int villageId) {
        if (villageId > 0) {
            for (VillageRole role : this.roles.values()) {
                if (role.getVillageAppliedTo() != villageId) continue;
                return role;
            }
        }
        return null;
    }

    public final VillageRole getRoleForPlayer(long playerId) {
        Citizen citiz = this.citizens.get(new Long(playerId));
        if (citiz != null) {
            return citiz.getRole();
        }
        if (playerId > 0L) {
            for (VillageRole role : this.roles.values()) {
                if (role.getPlayerAppliedTo() != playerId) continue;
                return role;
            }
        }
        return null;
    }

    public final void addRole(VillageRole role) {
        this.roles.put(role.getId(), role);
    }

    public final void removeRole(VillageRole role) {
        for (Citizen citiz : this.citizens.values()) {
            if (citiz.getRole() != role) continue;
            try {
                citiz.setRole(this.getRoleForStatus((byte)3));
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.WARNING, nsr.getMessage(), nsr);
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
        return x >= this.startx - 5 - this.perimeterTiles && x <= this.endx + 5 + this.perimeterTiles && y >= this.starty - 5 - this.perimeterTiles && y <= this.endy + 5 + this.perimeterTiles;
    }

    public final boolean coversWithPerimeterAndBuffer(int x, int y, int bufferTiles) {
        return x >= this.startx - 5 - this.perimeterTiles - bufferTiles && x <= this.endx + 5 + this.perimeterTiles + bufferTiles && y >= this.starty - 5 - this.perimeterTiles - bufferTiles && y <= this.endy + 5 + this.perimeterTiles + bufferTiles;
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
            } else if (action.getNumber() == 74 || action.getNumber() == 6 || action.getNumber() == 100 || action.getNumber() == 101 || action.getNumber() == 465) {
                this.modifyReputation(perpetrator.getWurmId(), -20, perpetrator.isGuest());
            } else {
                this.modifyReputation(perpetrator.getWurmId(), -5, perpetrator.isGuest());
            }
        }
    }

    public final boolean checkGuards(Action action, Creature perpetrator) {
        float mod = 1.0f;
        if (Servers.localServer.HOMESERVER) {
            mod = 1.5f;
        }
        perpetrator.setSecondsToLogout(300);
        float dist = Math.max(Math.abs(this.getCenterX() - action.getTileX()), Math.abs(this.getCenterY() - action.getTileY()));
        Guard[] g = this.getGuards();
        if (g.length == 0) {
            return false;
        }
        boolean noticed = false;
        boolean dryrun = false;
        Reputation rep = this.getReputationObject(perpetrator.getWurmId());
        if (rep != null && rep.getValue() >= 0 && rep.isPermanent()) {
            dryrun = true;
        }
        if (dist <= 5.0f) {
            if (action.getNumber() == 100 || action.isEquipAction() || action.getNumber() == 101) {
                if (perpetrator.getStealSkill().getKnowledge(0.0) < 50.0) {
                    perpetrator.getStealSkill().skillCheck(50.0, 0.0, dryrun, 10.0f);
                    return true;
                }
                float diff = 75.0f - dist;
                if (Servers.localServer.HOMESERVER) {
                    diff = 80.0f - dist;
                }
                noticed = perpetrator.getStealSkill().skillCheck(diff, 0.0, dryrun, 10.0f) < 0.0;
            } else {
                return true;
            }
        }
        if (!noticed) {
            float factor = dist * dist / 5.0f;
            float guardfactor = (float)this.guards.size() / factor;
            if (Server.rand.nextFloat() < guardfactor) {
                if (action.getNumber() == 100 || action.isEquipAction() || action.getNumber() == 101) {
                    perpetrator.getStealSkill().skillCheck(20.0, 0.0, dryrun, 10.0f);
                }
                return true;
            }
            for (int x = 0; x < g.length; ++x) {
                int tx = g[x].creature.getTileX();
                int ty = g[x].creature.getTileY();
                int d = Math.max(Math.abs(tx - action.getTileX()), Math.abs(ty - action.getTileY()));
                if (d > 5 || !(Server.rand.nextFloat() * mod < g[x].creature.getNoticeChance())) continue;
                if (action.getNumber() == 100 || action.isEquipAction() || action.getNumber() == 101) {
                    if (!(perpetrator.getStealSkill().skillCheck(g[x].creature.getNoticeChance() * 100.0f, 0.0, dryrun, x) < 0.0)) continue;
                    return true;
                }
                return true;
            }
            return false;
        }
        return noticed;
    }

    public final boolean checkGuards(int tilex, int tiley, Creature perpetrator) {
        return this.checkGuards(tilex, tiley, perpetrator, 5.0f);
    }

    public final boolean checkGuards(int tilex, int tiley, Creature perpetrator, float maxdist) {
        perpetrator.setSecondsToLogout(300);
        float dist = Math.max(Math.abs(this.getCenterX() - tilex), Math.abs(this.getCenterY() - tiley));
        if (dist <= 5.0f) {
            return true;
        }
        float factor = dist * dist / 5.0f;
        float guardfactor = (float)this.guards.size() / factor;
        if (Server.rand.nextFloat() < guardfactor) {
            return true;
        }
        Guard[] g = this.getGuards();
        for (int x = 0; x < g.length; ++x) {
            int tx = g[x].creature.getTileX();
            int ty = g[x].creature.getTileY();
            int d = Math.max(Math.abs(tx - tilex), Math.abs(ty - tiley));
            if (!((float)d <= maxdist) || !(Server.rand.nextFloat() < g[x].creature.getNoticeChance())) continue;
            return true;
        }
        return false;
    }

    public int getMaxGuardsAttacking() {
        return Math.max(maxGuardsOnThisServer, this.guards.size() / 20);
    }

    public final void cryForHelp(Creature needhelp, boolean cry) {
        Creature target;
        int guardsAssigned = 0;
        Guard best = null;
        int bestdist = Integer.MAX_VALUE;
        if (this.guards.size() > 1 && (target = needhelp.getTarget()) != null) {
            for (Guard guard : this.guards.values()) {
                int diffy;
                int diffx;
                int dist;
                if (guard.creature.target == target.getWurmId()) {
                    if (++guardsAssigned < this.getMaxGuardsAttacking()) continue;
                    break;
                }
                if (guard.creature.target != -10L || (dist = Math.max(diffx = (int)Math.abs(guard.creature.getPosX() - target.getPosX()), diffy = (int)Math.abs(guard.creature.getPosY() - target.getPosY()))) >= bestdist) continue;
                best = guard;
                bestdist = dist;
            }
            if (guardsAssigned < this.getMaxGuardsAttacking() && best != null) {
                Vehicle vehic;
                if (cry) {
                    best.creature.say("I'll help you with " + target.getName() + "!");
                }
                boolean attackTarget = true;
                if (target.getVehicle() != -10L && Server.rand.nextInt(3) == 0 && (vehic = Vehicles.getVehicleForId(target.getVehicle())) != null && vehic.creature) {
                    best.creature.setTarget(target.getVehicle(), false);
                    attackTarget = false;
                }
                if (attackTarget) {
                    best.creature.setTarget(target.getWurmId(), false);
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
        boolean isCaveWall;
        if (creature.getPower() >= 2) {
            return true;
        }
        if (System.currentTimeMillis() - this.creationDate < 120000L) {
            return true;
        }
        VillageRole role = this.getRoleFor(creature);
        if (role == null) {
            return false;
        }
        if (action == 100 || action == 350 || action == 537) {
            return false;
        }
        boolean onSurface = creature.getLayer() >= 0;
        byte tileType = Tiles.decodeType(encodedTile);
        Tiles.Tile t = Tiles.getTile(tileType);
        if (t == null) {
            logger.log(Level.SEVERE, "Unknown tile type " + tileType + " for " + creature.getName() + " at " + creature.getTilePos());
            return false;
        }
        if (Actions.isActionBrand(action)) {
            return role.mayBrand() || this.everybody.mayBrand();
        }
        if (Actions.isActionBreed(action)) {
            return role.mayBreed() || this.everybody.mayBreed();
        }
        if (Actions.isActionButcher(action)) {
            return role.mayButcher() || this.everybody.mayButcher();
        }
        if (Actions.isActionGroom(action)) {
            return role.mayGroom() || this.everybody.mayGroom();
        }
        if (Actions.isActionLead(action)) {
            return role.mayLead() || this.everybody.mayLead();
        }
        if (Actions.isActionMilkOrShear(action)) {
            return role.mayMilkAndShear() || this.everybody.mayMilkAndShear();
        }
        if (Actions.isActionSacrifice(action)) {
            return role.maySacrifice() || this.everybody.maySacrifice();
        }
        if (Actions.isActionTame(action)) {
            return role.mayTame() || this.everybody.mayTame();
        }
        if (Actions.isActionBuild(action) || Actions.isActionChangeBuilding(action)) {
            return role.mayBuild() || this.everybody.mayBuild();
        }
        if (Actions.isActionDestroyFence(action)) {
            return role.mayDestroyFences() || this.everybody.mayDestroyFences();
        }
        if (Actions.isActionDestroyItem(action)) {
            return role.mayDestroyItems() || this.everybody.mayDestroyItems();
        }
        if (Actions.isActionLockPick(action)) {
            return role.mayPickLocks();
        }
        if (Actions.isActionPlanBuilding(action)) {
            return role.mayPlanBuildings() || this.everybody.mayPlanBuildings();
        }
        if (Actions.isActionCultivate(action)) {
            return role.mayCultivate() || this.everybody.mayCultivate();
        }
        if (Actions.isActionDig(action) && (tileType == Tiles.Tile.TILE_CLAY.id || tileType == Tiles.Tile.TILE_MOSS.id || tileType == Tiles.Tile.TILE_PEAT.id || tileType == Tiles.Tile.TILE_TAR.id)) {
            return role.mayDigResources() || this.everybody.mayDigResources();
        }
        if (Actions.isActionPack(action)) {
            return role.mayPack() || this.everybody.mayPack();
        }
        if (Actions.isActionTerraform(action, onSurface)) {
            return role.mayTerraform() || this.everybody.mayTerraform();
        }
        if (Actions.isActionHarvest(action) && (tileType == Tiles.Tile.TILE_FIELD.id || tileType == Tiles.Tile.TILE_FIELD2.id)) {
            return role.mayHarvestFields() || this.everybody.mayHarvestFields();
        }
        if (Actions.isActionSow(action)) {
            return role.maySowFields() || this.everybody.maySowFields();
        }
        if (Actions.isActionFarm(action)) {
            return role.mayTendFields() || this.everybody.mayTendFields();
        }
        if (encodedTile == 0 && Actions.isActionChop(action)) {
            return role.mayDestroyFences() || this.everybody.mayDestroyFences();
        }
        Tiles.Tile theTile = Tiles.getTile(tileType);
        if (Actions.isActionChop(action) && (theTile.isTree() || theTile.isBush())) {
            byte tileData = Tiles.decodeData(encodedTile);
            int treeAge = tileData >> 4 & 0xF;
            if (treeAge > 11 && treeAge < 15 && theTile.isTree()) {
                return role.mayChopDownOldTrees() || this.everybody.mayChopDownOldTrees();
            }
            return role.mayChopDownAllTrees() || this.everybody.mayChopDownAllTrees();
        }
        if (Actions.isActionGather(action)) {
            return role.mayCutGrass() || this.everybody.mayCutGrass();
        }
        if (Actions.isActionPick(action) || Actions.isActionHarvest(action) && (t.isTree() || t.isBush() || encodedTile == 0)) {
            return role.mayHarvestFruit() || this.everybody.mayHarvestFruit();
        }
        if (Actions.isActionTrim(action)) {
            return role.mayMakeLawn() || this.everybody.mayMakeLawn();
        }
        if (Actions.isActionPickSprout(action)) {
            return role.mayPickSprouts() || this.everybody.mayPickSprouts();
        }
        if (Actions.isActionPlant(action)) {
            return role.mayPlantFlowers() || this.everybody.mayPlantFlowers();
        }
        if (Actions.isActionPlantCenter(action)) {
            return role.mayPlantSprouts() || this.everybody.mayPlantSprouts();
        }
        if (Actions.isActionPrune(action)) {
            return role.mayPrune() || this.everybody.mayPrune();
        }
        if (Actions.isActionDietySpell(action)) {
            return role.mayCastDeitySpells() || this.everybody.mayCastDeitySpells();
        }
        if (Actions.isActionSorcerySpell(action)) {
            return role.mayCastSorcerySpells() || this.everybody.mayCastSorcerySpells();
        }
        if (Actions.isActionForageBotanizeInvestigate(action)) {
            return role.mayForageAndBotanize() || this.everybody.mayForageAndBotanize();
        }
        if (Actions.isActionPlaceNPCs(action)) {
            return role.mayPlaceMerchants() || this.everybody.mayPlaceMerchants();
        }
        if (Actions.isActionPave(action)) {
            return role.mayPave() || this.everybody.mayPave();
        }
        if (Actions.isActionMeditate(action)) {
            return role.mayUseMeditationAbilities() || this.everybody.mayUseMeditationAbilities();
        }
        if (Actions.isActionAttachLock(action)) {
            return role.mayAttachLock() || this.everybody.mayAttachLock();
        }
        if (Actions.isActionDrop(action)) {
            return role.mayDrop() || this.everybody.mayDrop();
        }
        if (Actions.isActionImproveOrRepair(action)) {
            return role.mayImproveAndRepair() || this.everybody.mayImproveAndRepair();
        }
        if (Actions.isActionLoad(action)) {
            return role.mayLoad() || this.everybody.mayLoad();
        }
        if (Actions.isActionTake(action)) {
            return role.mayPickup() || this.everybody.mayPickup();
        }
        if (Actions.isActionPickupPlanted(action)) {
            return role.mayPickupPlanted() || this.everybody.mayPickupPlanted();
        }
        if (Actions.isActionPlantItem(action)) {
            return role.mayPlantItem() || this.everybody.mayPlantItem();
        }
        if (Actions.isActionPullPushTurn(action)) {
            return role.mayPushPullTurn() || this.everybody.mayPushPullTurn();
        }
        if (Actions.isActionUnload(action)) {
            return role.mayUnload() || this.everybody.mayUnload();
        }
        if (!onSurface && (dir == 0 || dir == 1) && Actions.isActionMineFloor(action)) {
            return role.mayMineFloor() || this.everybody.mayMineFloor();
        }
        boolean bl = isCaveWall = !onSurface && dir != 0 && dir != 1;
        if (isCaveWall && tileType == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id && Actions.isActionMine(action)) {
            return role.mayMineIronVeins() || this.everybody.mayMineIronVeins();
        }
        if (isCaveWall && tileType != Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id && t.isOreCave() && Actions.isActionMine(action)) {
            return role.mayMineOtherVeins() || this.everybody.mayMineOtherVeins();
        }
        if (isCaveWall && Actions.isActionMine(action) && (tileType == Tiles.Tile.TILE_CAVE_WALL.id || Tiles.isReinforcedCave(tileType))) {
            return role.mayMineRock() || this.everybody.mayMineRock();
        }
        if (onSurface && Actions.isActionMineSurface(action)) {
            return role.mayMineSurface() || this.everybody.mayMineSurface();
        }
        if (Actions.isActionTunnel(action)) {
            return role.mayTunnel() || this.everybody.mayTunnel();
        }
        if (!onSurface && Actions.isActionReinforce(action)) {
            return role.mayReinforce() || this.everybody.mayReinforce();
        }
        if (Actions.isActionDestroy(action)) {
            return role.mayDestroyAnyBuilding();
        }
        if (action == 73) {
            return role.mayInviteCitizens() || creature.getCitizenVillage() != this;
        }
        if (Actions.isActionManage(action)) {
            return role.mayManageAllowedObjects();
        }
        if (action == 66) {
            return role.mayManageCitizenRoles();
        }
        if (action == 67) {
            return role.mayManageGuards();
        }
        if (action == 69) {
            return role.mayManageReputations();
        }
        if (action == 540) {
            return role.mayManageRoles();
        }
        if (action == 68) {
            return role.mayManageSettings();
        }
        if (action == 481) {
            return role.mayConfigureTwitter();
        }
        if (action == 348) {
            return role.mayDisbandSettlement();
        }
        if (action == 76) {
            return role.mayResizeSettlement();
        }
        return true;
    }

    public final void updateGatesForRole(VillageRole role) {
        if (this.citizens != null && this.gates != null) {
            for (FenceGate gate : this.gates) {
                for (Citizen citiz : this.citizens.values()) {
                    if (citiz.getRole() != role) continue;
                    try {
                        Creature creat = Server.getInstance().getCreature(citiz.getId());
                        if (!gate.containsCreature(creat)) continue;
                        creat.updateGates();
                    }
                    catch (NoSuchCreatureException nsc) {
                        logger.log(Level.WARNING, citiz.getName() + " - creature not found:", nsc);
                    }
                    catch (NoSuchPlayerException noSuchPlayerException) {}
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
        if (!(this.guards.size() < 1 && (attacker.isOnPvPServer() && defender.isOnPvPServer() || this.isEnemy(defender)))) {
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
        if (citiz == null) {
            return false;
        }
        role = citiz.getRole();
        return role.isDiplomat();
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
        }
        DbCitizen citizen = null;
        citizen = new DbCitizen(wurmid, creature.getName(), role, -10L, -10L);
        ((Citizen)citizen).create(creature, this.id);
        boolean ok = false;
        if (citizen != null) {
            this.broadCastSafe(creature.getName() + " is now a citizen of " + this.name + "!", (byte)2);
            this.citizens.put(new Long(citizen.getId()), citizen);
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
                Message mess;
                PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(this.getAllianceNumber());
                if (pvpAll != null && pvpAll.getMotd().length() > 0) {
                    mess = pvpAll.getMotdMessage();
                    creature.getCommunicator().sendMessage(mess);
                } else {
                    mess = new Message(creature, 15, "Alliance", "");
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
            for (int x = 0; x < lCitizens.length; ++x) {
                if (!lCitizens[x].isPlayer()) continue;
                try {
                    Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
                    if (lCitizens[x].getId() != wurmid) {
                        p.getCommunicator().sendAddVillager(creature.getName(), lCitizens[x].getId());
                        ++plays;
                        continue;
                    }
                    p.setLastChangedVillage(System.currentTimeMillis());
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
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
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        if (WurmId.getType(creature.getWurmId()) == 0) {
            this.addHistory(creature.getName(), "is no longer a citizen");
            Citizen[] lCitizens = this.getCitizens();
            for (int x = 0; x < lCitizens.length; ++x) {
                if (WurmId.getType(lCitizens[x].getId()) != 0) continue;
                try {
                    Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
                    p.getCommunicator().sendRemoveVillager(creature.getName());
                    continue;
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
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
        for (int x = 0; x < lCitizens.length; ++x) {
            if (WurmId.getType(lCitizens[x].getId()) != 0 || lCitizens[x].getId() == player.getWurmId()) continue;
            try {
                Player p = Players.getInstance().getPlayer(lCitizens[x].getId());
                player.getCommunicator().sendAddVillager(p.getName(), lCitizens[x].getId());
                continue;
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public final void removeCitizen(Citizen citizen) {
        Creature creature = null;
        try {
            creature = Server.getInstance().getCreature(citizen.getId());
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, "No creature exists with wurmid " + citizen.getId() + " any longer?", nsc);
        }
        catch (NoSuchPlayerException nsc) {
            // empty catch block
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
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
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
        VillageRole role;
        if (this.everybody == null) {
            this.everybody = this.createRoleEverybody();
        }
        if ((role = this.getRoleForPlayer(creature.getWurmId())) == null) {
            if (creature.getCitizenVillage() != null) {
                role = this.getRoleForVillage(creature.getCitizenVillage().getId());
            }
            if (role == null) {
                try {
                    role = this.isAlly(creature) ? this.getRoleForStatus((byte)5) : this.getRoleForStatus((byte)1);
                }
                catch (NoSuchRoleException nsr) {
                    logger.log(Level.WARNING, nsr.getMessage(), nsr);
                }
            }
        }
        return role;
    }

    public final VillageRole getRoleFor(long creatureId) {
        VillageRole role;
        if (this.everybody == null) {
            this.everybody = this.createRoleEverybody();
        }
        if ((role = this.getRoleForPlayer(creatureId)) == null) {
            VillageRole vr;
            Citizen cit;
            Village citvill = Villages.getVillageForCreature(creatureId);
            if (citvill != null && (role = this.getRoleForVillage(citvill.getId())) == null && this.allianceNumber > 0 && citvill.getAllianceNumber() == this.allianceNumber && (cit = citvill.getCitizen(creatureId)) != null && (vr = cit.getRole()) != null && vr.mayPerformActionsOnAlliedDeeds()) {
                try {
                    return this.getRoleForStatus((byte)5);
                }
                catch (NoSuchRoleException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
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
        Citizen[] toReturn = new Citizen[]{};
        if (this.citizens.size() > 0) {
            toReturn = this.citizens.values().toArray(new Citizen[this.citizens.size()]);
        }
        return toReturn;
    }

    public final void replaceNoDeed(Creature mayor) {
        try {
            Item i;
            Item newDeed = ItemFactory.createItem(663, 50.0f + Server.rand.nextFloat() * 50.0f, mayor.getName());
            logger.log(Level.INFO, mayor.getName() + " replacing deed for " + this.getName() + " with id " + newDeed.getWurmId() + " from " + this.deedid);
            newDeed.setName("Settlement deed");
            newDeed.setDescription(this.getName());
            newDeed.setData2(this.id);
            mayor.getInventory().insertItem(newDeed, true);
            logger.log(Level.INFO, "Inserted " + newDeed + " into inventory of " + mayor.getName());
            long oldDeed = this.deedid;
            if (this.gates != null) {
                for (FenceGate gate : this.gates) {
                    try {
                        Item lock = gate.getLock();
                        lock.addKey(newDeed.getWurmId());
                        lock.removeKey(this.deedid);
                    }
                    catch (NoSuchLockException noSuchLockException) {}
                }
            }
            logger.log(Level.INFO, "Fixed gates. Now destroying " + this.deedid);
            Items.destroyItem(this.deedid);
            this.setDeedId(newDeed.getWurmId());
            logger.log(Level.INFO, "Setting deedid to " + newDeed.getWurmId());
            mayor.addKey(newDeed, false);
            try {
                logger.log(Level.INFO, "Verifying existance of deed " + newDeed.getWurmId());
                i = Items.getItem(newDeed.getWurmId());
                logger.log(Level.INFO, "Item " + i.getWurmId() + " was properly found in database! Data 2 is " + i.getData2());
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.INFO, "Item " + newDeed.getWurmId() + " not found in database!");
            }
            try {
                logger.log(Level.INFO, "Verifying removal of deed " + oldDeed);
                i = Items.getItem(oldDeed);
                logger.log(Level.INFO, "Deed " + oldDeed + " was erroneously found in database! Data is " + i.getData2());
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.INFO, "Item " + oldDeed + " properly not found in database!");
            }
        }
        catch (NoSuchTemplateException nsi) {
            logger.log(Level.WARNING, "No deed template for settlement " + this.name, nsi);
        }
        catch (FailedException nsf) {
            logger.log(Level.WARNING, "Failed to create deed for settlement " + this.name, nsf);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "failed to set new deed id for the settlement of " + this.name, iox);
        }
    }

    public final void setNewBounds(int newsx, int newsy, int newex, int newey) {
        GuardTower sw;
        GuardTower se;
        GuardTower ne;
        Zone[] coveredOldSurfaceZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
        Zone[] coveredOldCaveZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);
        int oldStartPerimeterX = this.startx - 5 - this.perimeterTiles;
        int oldStartPerimeterY = this.starty - 5 - this.perimeterTiles;
        int oldEndPerimeterX = this.endx + 5 + this.perimeterTiles;
        int oldEndPerimeterY = this.endy + 5 + this.perimeterTiles;
        Rectangle oldPerimeter = new Rectangle(oldStartPerimeterX, oldStartPerimeterY, oldEndPerimeterX - oldStartPerimeterX, oldEndPerimeterY - oldStartPerimeterY);
        try {
            this.setStartX(newsx);
            this.setStartY(newsy);
            this.setEndX(newex);
            this.setEndY(newey);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        int newStartPerimeterX = this.startx - 5 - this.perimeterTiles;
        int newStartPerimeterY = this.starty - 5 - this.perimeterTiles;
        int newEndPerimeterX = this.endx + 5 + this.perimeterTiles;
        int newEndPerimeterY = this.endy + 5 + this.perimeterTiles;
        for (int x = newStartPerimeterX; x <= newEndPerimeterX; ++x) {
            for (int y = newStartPerimeterY; y <= newEndPerimeterY; ++y) {
                if (oldPerimeter.contains(x, y)) continue;
                Zones.setKingdom(x, y, this.kingdom);
            }
        }
        Rectangle newPerimeter = new Rectangle(newStartPerimeterX, newStartPerimeterY, newEndPerimeterX - newStartPerimeterX, newEndPerimeterY - newStartPerimeterY);
        for (int x = oldStartPerimeterX; x <= oldEndPerimeterX; ++x) {
            for (int y = oldStartPerimeterY; y <= oldEndPerimeterY; ++y) {
                if (newPerimeter.contains(x, y)) continue;
                Zones.setKingdom(x, y, (byte)0);
            }
        }
        GuardTower nw = Kingdoms.getClosestTower(Math.min(oldStartPerimeterX, newStartPerimeterX), Math.min(oldStartPerimeterY, newStartPerimeterY), true);
        if (nw != null) {
            Kingdoms.addTowerKingdom(nw.getTower());
        }
        if ((ne = Kingdoms.getClosestTower(Math.max(oldEndPerimeterX, newEndPerimeterX), Math.min(oldStartPerimeterY, newStartPerimeterY), true)) != null && ne != nw) {
            Kingdoms.addTowerKingdom(ne.getTower());
        }
        if ((se = Kingdoms.getClosestTower(Math.min(oldStartPerimeterX, newStartPerimeterX), Math.max(oldEndPerimeterY, newEndPerimeterY), true)) != null && se != nw && se != ne) {
            Kingdoms.addTowerKingdom(se.getTower());
        }
        if ((sw = Kingdoms.getClosestTower(Math.max(oldEndPerimeterX, newEndPerimeterX), Math.max(oldEndPerimeterY, newEndPerimeterY), true)) != null && sw != nw && sw != ne && sw != nw) {
            Kingdoms.addTowerKingdom(sw.getTower());
        }
        Zone[] coveredNewSurfaceZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
        HashSet<Zone> notfound = new HashSet<Zone>();
        for (int y = 0; y < coveredOldSurfaceZones.length; ++y) {
            notfound.add(coveredOldSurfaceZones[y]);
        }
        boolean found = false;
        for (int x = 0; x < coveredNewSurfaceZones.length; ++x) {
            found = false;
            for (int y = 0; y < coveredOldSurfaceZones.length; ++y) {
                if (coveredNewSurfaceZones[x].getId() != coveredOldSurfaceZones[y].getId()) continue;
                coveredNewSurfaceZones[x].updateVillage(this, true);
                notfound.remove(coveredOldSurfaceZones[y]);
                found = true;
                break;
            }
            if (found) continue;
            coveredNewSurfaceZones[x].updateVillage(this, true);
        }
        for (Zone z : notfound) {
            z.updateVillage(this, false);
        }
        notfound.clear();
        for (int y = 0; y < coveredOldCaveZones.length; ++y) {
            notfound.add(coveredOldCaveZones[y]);
        }
        Zone[] coveredNewCaveZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);
        for (int x = 0; x < coveredNewCaveZones.length; ++x) {
            found = false;
            for (int y = 0; y < coveredOldCaveZones.length; ++y) {
                if (coveredNewCaveZones[x].getId() != coveredOldCaveZones[y].getId()) continue;
                coveredOldCaveZones[y].updateVillage(this, true);
                notfound.remove(coveredOldCaveZones[y]);
                found = true;
                break;
            }
            if (found) continue;
            coveredNewCaveZones[x].updateVillage(this, true);
        }
        for (Zone z : notfound) {
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
        HashMap<Object, Integer> votees = new HashMap<Object, Integer>();
        int votesCast = 0;
        for (int x = 0; x < citizarr.length; ++x) {
            if (citizarr[x].hasVoted()) {
                ++votesCast;
                long votedFor = citizarr[x].getVotedFor();
                Long vote = new Long(votedFor);
                Integer votei = (Integer)votees.get(vote);
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
                continue;
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to clear votes for " + citizarr[x].getName() + ": " + iox.getMessage(), iox);
            }
        }
        long leaderlong = -10L;
        int maxvotes = 0;
        for (Long target : votees.keySet()) {
            Integer votes = (Integer)votees.get(target);
            if (votes <= maxvotes) continue;
            leaderlong = target;
            maxvotes = votes;
        }
        leader = this.citizens.get(new Long(leaderlong));
        logger.log(Level.INFO, this.getName() + " Checking if " + leader + " will become mayor with " + maxvotes + " out of " + votesCast + ".");
        if (leader != null && this.changeRule(maxvotes, votesCast)) {
            logger.log(Level.INFO, this.getName() + " swapping owners - old: " + currMayor + ", new: " + leader);
            this.swapDeedOwners(currMayor, leader);
            try {
                this.group.broadCastSafe(this.name + " has a new " + this.getRoleForStatus((byte)2).getName() + "! Hail " + leader.getName() + "!", (byte)2);
                this.addHistory(leader.getName(), "new " + this.getRoleForStatus((byte)2).getName() + " by a vote of " + maxvotes + " out of " + votesCast + " cast");
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.WARNING, this.name + " has no ROLE_MAYOR!");
            }
        } else {
            try {
                if (currMayor != null) {
                    this.group.broadCastSafe(currMayor.getName() + " will be your " + this.getRoleForStatus((byte)2).getName() + " for another period! Hail " + currMayor.getName() + "!", (byte)2);
                    this.addHistory(currMayor.getName(), "stays " + this.getRoleForStatus((byte)2).getName() + ". Number of votes cast: " + votesCast);
                } else {
                    this.group.broadCastSafe("You will have no " + this.getRoleForStatus((byte)2).getName() + " for another voting period.", (byte)2);
                }
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.WARNING, this.name + " has no ROLE_MAYOR!");
            }
        }
    }

    public final Citizen getMayor() {
        Citizen[] citizarr = this.getCitizens();
        for (int x = 0; x < citizarr.length; ++x) {
            VillageRole role = citizarr[x].getRole();
            if (role.getStatus() != 2) continue;
            return citizarr[x];
        }
        return null;
    }

    private void swapDeedOwners(Citizen mayor, Citizen newMayor) {
        block25: {
            try {
                if (newMayor != null) {
                    try {
                        Item deed = Items.getItem(this.deedid);
                        Creature mayorCreature = null;
                        if (mayor != null) {
                            try {
                                mayorCreature = Server.getInstance().getCreature(mayor.getId());
                            }
                            catch (NoSuchCreatureException nsc) {
                                logger.log(Level.WARNING, "The mayor for " + this.name + " is a creature?", nsc);
                            }
                            catch (NoSuchPlayerException nsp) {
                                logger.log(Level.INFO, mayor.getName() + " is offline loosing mayorship.");
                            }
                        }
                        Creature newMayorCreature = null;
                        try {
                            newMayorCreature = Server.getInstance().getCreature(newMayor.getId());
                        }
                        catch (NoSuchCreatureException nsc) {
                            logger.log(Level.WARNING, "The mayor for " + this.name + " is a creature?", nsc);
                        }
                        catch (NoSuchPlayerException nsp) {
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
                            break block25;
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, this.getName() + " failed to set mayor status: " + iox.getMessage(), iox);
                            break block25;
                        }
                        catch (NoSuchRoleException nsr) {
                            logger.log(Level.WARNING, this.getName() + " this settlement doesn't have the correct roles: " + nsr.getMessage(), nsr);
                        }
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, "Deed with id " + this.deedid + " for settlement " + this.getName() + ", " + this.id + " not found!", nsi);
                    }
                    break block25;
                }
                if (newMayor == null) {
                    logger.log(Level.INFO, "Error, new mayor is null: " + this.name + ".", new Exception());
                }
            }
            catch (NullPointerException nsp) {
                logger.log(Level.INFO, nsp.getMessage(), nsp);
            }
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
        }
        Citizen[] citizarr = this.getCitizens();
        int votes = 0;
        for (int x = 0; x < citizarr.length; ++x) {
            if (!citizarr[x].hasVoted()) continue;
            ++votes;
        }
        int activeCitizens = 0;
        for (Long it : this.citizens.keySet()) {
            long wurmid = it;
            if (WurmId.getType(wurmid) != 0) continue;
            long lastLogout = Players.getInstance().getLastLogoutForPlayer(wurmid);
            if (System.currentTimeMillis() - lastLogout >= 1209600000L) continue;
            ++activeCitizens;
        }
        logger.log(Level.INFO, this.getName() + " votes is " + votes + " for the last week, active citizens are " + activeCitizens);
        return this.changeRule(votes, activeCitizens);
    }

    public final void vote(Creature voter, String targname) throws IOException, NoSuchPlayerException {
        if (!this.isDemocracy()) {
            voter.getCommunicator().sendNormalServerMessage("You vote for " + targname + " is noted, but ignored.", (byte)3);
            return;
        }
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

    private void swapDeedOwners(Citizen owner, Creature receiver, Item deed) throws NoSuchItemException {
        receiver.getInventory().insertItem(deed, true);
    }

    private boolean changeRule(int votes, int totalVotes) {
        logger.log(Level.INFO, this.getName() + " total votes is " + totalVotes + ". Votes is " + votes + " so fraction is " + (float)votes / (float)totalVotes + ". This is a democracy=" + this.democracy + ": 0.51*=" + 0.51 * (double)totalVotes + ", 0.81*=" + 0.81 * (double)totalVotes);
        if (this.democracy) {
            return (double)votes >= 0.51 * (double)totalVotes;
        }
        return false;
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
            }
            catch (IOException iox) {
                this.disband = System.currentTimeMillis() + 3600000L;
                logger.log(Level.WARNING, "Failed to set disband time for settlement with id " + this.getId() + ".", iox);
            }
        } else {
            try {
                this.setDisbandTime(System.currentTimeMillis() + 86400000L);
                this.setDisbander(disbid);
            }
            catch (IOException iox) {
                this.disband = System.currentTimeMillis() + 86400000L;
                logger.log(Level.WARNING, "Failed to set disband time for settlement with id " + this.getId() + ".", iox);
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
                }
                catch (NoSuchPlayerException nsp) {
                    this.addHistory("", "the settlement has been salvaged from disbanding");
                }
                Village[] allies = this.getAllies();
                for (int x = 0; x < allies.length; ++x) {
                    allies[x].broadCastSafe("The settlement of " + this.getName() + " has been salvaged.", (byte)2);
                }
                this.setDisbandTime(0L);
                this.setDisbander(-10L);
            }
            catch (IOException iox) {
                this.disband = 0L;
                this.addHistory("", "the settlement has been salvaged from disbanding");
                logger.log(Level.WARNING, "Failed to set disband time to 0 for settlement with id " + this.getId() + ".", iox);
            }
        }
    }

    private long getFoundingCost() {
        int tiles = this.getDiameterX() * this.getDiameterY();
        long moneyNeeded = (long)tiles * Villages.TILE_COST;
        return moneyNeeded += (long)this.perimeterTiles * Villages.PERIMETER_COST;
    }

    public final boolean givesTheftBonus() {
        return this.plan.isUnderSiege() && this.plan.hiredGuardNumber > 9;
    }

    public final void disband(String disbanderName) {
        GuardTower sw;
        GuardTower se;
        GuardTower ne;
        GuardTower nw;
        long check;
        block63: {
            Kingdom k;
            Object war;
            int x;
            long moneyToReimburse = 0L;
            if (!disbanderName.equals(upkeepString)) {
                Citizen mayor = this.getMayor();
                if (mayor != null) {
                    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(mayor.wurmId);
                    if (pinf != null) {
                        long left;
                        if (freeDisbands) {
                            LoginServerWebConnection lsw;
                            left = Servers.localServer.isFreeDeeds() ? 0L : this.plan.moneyLeft;
                            left -= 10000L;
                            try {
                                Item deed = Items.getItem(this.deedid);
                                if (deed.isNewDeed() && !Servers.localServer.isFreeDeeds()) {
                                    logger.log(Level.INFO, "DISBANDING " + this.getName() + " left=" + left + ". Found cost=" + this.getFoundingCost());
                                    left += this.getFoundingCost();
                                }
                            }
                            catch (NoSuchItemException nsi) {
                                logger.log(Level.WARNING, this.getName() + " No deed id with id=" + this.deedid, nsi);
                            }
                            Citizen[] citizarr = this.getCitizens();
                            for (int x2 = 0; x2 < citizarr.length; ++x2) {
                                if (WurmId.getType(citizarr[x2].wurmId) != 1) continue;
                                try {
                                    Creature c = Creatures.getInstance().getCreature(citizarr[x2].wurmId);
                                    if (c.isNpcTrader()) {
                                        Shop shop = Economy.getEconomy().getShop(c);
                                        if (shop == null || shop.isPersonal()) continue;
                                        logger.log(Level.INFO, "Adding 20 silver to " + pinf.getName() + " for trader in settlement " + this.getName());
                                        left += 200000L;
                                        continue;
                                    }
                                    if (!c.isSpiritGuard() || Servers.localServer.isFreeDeeds()) continue;
                                    logger.log(Level.INFO, "Adding guard cost to " + pinf.getName() + " for guard in settlement " + this.getName());
                                    left += Villages.GUARD_COST;
                                    continue;
                                }
                                catch (NoSuchCreatureException c) {
                                    // empty catch block
                                }
                            }
                            if (left > 0L && !(lsw = new LoginServerWebConnection()).addMoney(mayor.wurmId, pinf.getName(), left, "Disb " + this.getName())) {
                                logger.log(Level.INFO, "Postponing disbanding " + this.getName() + ".");
                                return;
                            }
                        } else {
                            LoginServerWebConnection lsw;
                            left = 0L;
                            if (!Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep() || Servers.localServer.isFreeDeeds() && Servers.localServer.isUpkeep() && this.creationDate > System.currentTimeMillis() + 2419200000L) {
                                left = this.plan.getDisbandMoneyLeft();
                            }
                            if ((moneyToReimburse += Math.max(left, 0L)) > 0L && !(lsw = new LoginServerWebConnection()).addMoney(mayor.wurmId, pinf.getName(), moneyToReimburse, "Disb " + this.getName())) {
                                logger.log(Level.INFO, "Postponing disbanding " + this.getName() + ".");
                                return;
                            }
                        }
                    }
                } else {
                    logger.log(Level.INFO, "NO mayor found for " + this.getName() + " when disbanding.");
                }
            }
            if (this.gates != null) {
                for (FenceGate gate : this.gates) {
                    gate.setOpenTime(0);
                    gate.setCloseTime(0);
                }
            }
            FenceGate.unManageGatesFor(this.getId());
            MineDoorPermission.unManageMineDoorsFor(this.getId());
            Creatures.getInstance().removeBrandingFor(this.getId());
            VillageMessages.delete(this.getId());
            Zone[] coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, true);
            for (x = 0; x < coveredZones.length; ++x) {
                coveredZones[x].removeVillage(this);
            }
            coveredZones = Zones.getZonesCoveredBy(this.startx, this.starty, this.endx, this.endy, false);
            for (x = 0; x < coveredZones.length; ++x) {
                coveredZones[x].removeVillage(this);
            }
            Zones.setKingdom(this.startx - 5 - this.getPerimeterSize(), this.starty - 5 - this.getPerimeterSize(), this.getPerimeterDiameterX(), this.getPerimeterDiameterY(), (byte)0);
            Kingdoms.reAddKingdomInfluences(-5 - this.getPerimeterSize() * 2, this.starty - 5 - this.getPerimeterSize() * 2, this.endx + 5 + this.getPerimeterSize() * 2, this.endy + 5 + this.getPerimeterSize() * 2);
            try {
                Item token = this.getToken();
                Items.destroyItem(token.getWurmId());
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "No token for settlement " + this.getName() + " when destroying it at " + this.getStartX() + ", " + this.getStartY() + ".", nsi);
            }
            Guard[] guardarr = this.getGuards();
            for (int x3 = 0; x3 < guardarr.length; ++x3) {
                Creature c = guardarr[x3].getCreature();
                this.deleteGuard(c, true);
            }
            Wagoner[] wagonerarr = this.getWagoners();
            for (int x4 = 0; x4 < wagonerarr.length; ++x4) {
                Creature c = wagonerarr[x4].getCreature();
                if (c == null) continue;
                this.deleteWagoner(c);
            }
            Citizen[] citizarr = this.getCitizens();
            for (int x5 = 0; x5 < citizarr.length; ++x5) {
                Creature c2;
                if (WurmId.getType(citizarr[x5].wurmId) == 1) {
                    try {
                        c2 = Creatures.getInstance().getCreature(citizarr[x5].wurmId);
                        if (c2.isNpcTrader()) {
                            c2.destroy();
                        }
                    }
                    catch (NoSuchCreatureException c2) {
                        // empty catch block
                    }
                }
                try {
                    c2 = Server.getInstance().getCreature(citizarr[x5].wurmId);
                    if (c2.getMusicPlayer() != null) {
                        c2.getMusicPlayer().checkMUSIC_DISBAND_SND();
                    }
                }
                catch (NoSuchPlayerException c3) {
                }
                catch (NoSuchCreatureException c3) {
                    // empty catch block
                }
                this.removeCitizen(citizarr[x5]);
            }
            if (this.citizens != null) {
                this.citizens.clear();
            }
            VillageRole[] rolearr = this.getRoles();
            for (int x6 = 0; x6 < rolearr.length; ++x6) {
                try {
                    rolearr[x6].delete();
                    continue;
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to delete role with id " + rolearr[x6].getId() + " for settlement " + this.getName() + " with id " + this.id + " from db: " + iox.getMessage(), iox);
                }
            }
            if (this.roles != null) {
                this.roles.clear();
            }
            try {
                RecruitmentAds.deleteVillageAd(this);
                this.delete();
                this.deleteVillageMapAnnotations();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to delete settlement " + this.getName() + " from db: " + iox.getMessage(), iox);
            }
            if (this.wars != null) {
                for (Village opponent : this.wars.keySet()) {
                    opponent.broadCastSafe(this.getName() + " has just been disbanded!", (byte)2);
                    if (opponent.wars != null) {
                        opponent.wars.remove(this);
                    }
                    war = this.wars.get(opponent);
                    ((VillageWar)war).delete();
                }
                this.wars.clear();
            }
            if (this.warDeclarations != null) {
                for (Village opponent : this.warDeclarations.keySet()) {
                    opponent.broadCastSafe(this.getName() + " has just been disbanded!", (byte)2);
                    if (opponent.warDeclarations != null) {
                        opponent.warDeclarations.remove(this);
                    }
                    war = this.warDeclarations.get(opponent);
                    ((WarDeclaration)war).delete();
                }
                this.warDeclarations.clear();
            }
            if (this.reputations != null) {
                Reputation[] reps = this.getReputations();
                for (int x7 = 0; x7 < reps.length; ++x7) {
                    reps[x7].delete();
                }
                this.reputations.clear();
            }
            this.plan.delete();
            this.plan = null;
            Villages.removeVillage(this.id);
            Server.getInstance().broadCastSafe(WurmCalendar.getTime(), false);
            String vil = "settlement";
            if (disbanderName.equals(upkeepString)) {
                Server.getInstance().broadCastSafe("The settlement of " + this.getName() + " has just been disbanded.", true, (byte)2);
            } else {
                Server.getInstance().broadCastSafe("The settlement of " + this.getName() + " has just been disbanded by " + disbanderName + ".", true, (byte)2);
            }
            this.addHistory(disbanderName, "disbanded");
            HistoryManager.addHistory(disbanderName, "disbanded " + this.getName(), false);
            check = System.currentTimeMillis();
            if (Villages.wasLastVillage(this) && (k = Kingdoms.getKingdom(this.kingdom)) != null) {
                k.disband();
            }
            this.leavePvPAlliance();
            if (freeDisbands) {
                if (disbanderName.equals(upkeepString)) {
                    Items.destroyItem(this.getDeedId());
                } else {
                    try {
                        Item deed = Items.getItem(this.deedid);
                        if (!deed.isNewDeed()) {
                            deed.setName(deed.getTemplate().getName());
                            deed.setDescription("");
                            deed.setData(-1, -1);
                            deed.setAuxData((byte)0);
                            break block63;
                        }
                        Items.destroyItem(this.deedid);
                    }
                    catch (NoSuchItemException deed) {}
                }
            } else {
                Items.destroyItem(this.getDeedId());
            }
        }
        logger.info("The settlement of " + this.getName() + ", " + this.id + " has just been disbanded by " + disbanderName + ".");
        if (System.currentTimeMillis() - check > 1000L) {
            logger.log(Level.INFO, "Lag detected when destroying deed at 7.11: " + (int)((System.currentTimeMillis() - check) / 1000L));
        }
        if ((nw = Kingdoms.getClosestTower(this.startx, this.starty, true)) != null) {
            Kingdoms.addTowerKingdom(nw.getTower());
        }
        if ((ne = Kingdoms.getClosestTower(this.endx, this.starty, true)) != null && ne != nw) {
            Kingdoms.addTowerKingdom(ne.getTower());
        }
        if ((se = Kingdoms.getClosestTower(this.startx, this.endy, true)) != null && se != ne && se != nw) {
            Kingdoms.addTowerKingdom(se.getTower());
        }
        if ((sw = Kingdoms.getClosestTower(this.endx, this.endy, true)) != null && sw != nw && sw != ne && sw != nw) {
            Kingdoms.addTowerKingdom(sw.getTower());
        }
    }

    private void leavePvPAlliance() {
        block9: {
            PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(this.getAllianceNumber());
            if (pvpAll == null) break block9;
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
                for (Village v : allyArr) {
                    if (v.getId() == this.getId()) continue;
                    if (alldisb) {
                        v.broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                        v.setAllianceNumber(0);
                        continue;
                    }
                    if (newCap == null) {
                        newCap = v;
                        v.setAllianceNumber(newCap.getId());
                        pvpAll.setIdNumber(newCap.getId());
                        v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + " and " + v.getName() + " is the new main settlement.");
                        v.addHistory(this.getName(), "left the " + pvpAll.getName());
                        continue;
                    }
                    v.setAllianceNumber(newCap.getId());
                    v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + " and " + newCap.getName() + " is the new capital.");
                    v.addHistory(this.getName(), "left the " + pvpAll.getName() + ", making " + newCap.getName() + " the new capital.");
                }
            } else {
                Village[] allyArr = this.getAllies();
                boolean alldisb = false;
                this.setAllianceNumber(0);
                if (!pvpAll.exists()) {
                    alldisb = true;
                    pvpAll.delete();
                }
                for (Village v : allyArr) {
                    if (v.getId() == this.getId()) continue;
                    if (alldisb) {
                        v.broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                        v.setAllianceNumber(0);
                        continue;
                    }
                    v.broadCastAlert(this.getName() + " has left the " + pvpAll.getName() + ".");
                    v.addHistory(this.getName(), "left the " + pvpAll.getName() + ".");
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
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                }
                catch (NoSuchCreatureException noSuchCreatureException) {}
            } else {
                this.removeTarget(wurmid, true);
            }
            return r;
        }
        return null;
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
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                }
                catch (NoSuchCreatureException nsc) {
                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
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
        return this.settings.hasPermission(VillagePermissions.HIGHWAY_OPT_IN.getBit());
    }

    public final boolean isKosAllowed() {
        return this.settings.hasPermission(VillagePermissions.ALLOW_KOS.getBit());
    }

    public final boolean isHighwayAllowed() {
        return this.settings.hasPermission(VillagePermissions.ALLOW_HIGHWAYS.getBit());
    }

    public final void setIsHighwayFound(boolean highwayFound) {
        this.settings.setPermissionBit(VillagePermissions.HIGHWAY_OPT_IN.getBit(), highwayFound);
    }

    public final void setIsKosAllowed(boolean kosAlloed) {
        this.settings.setPermissionBit(VillagePermissions.ALLOW_KOS.getBit(), kosAlloed);
    }

    public final void setIsHighwayAllowed(boolean highwayAllowed) {
        this.settings.setPermissionBit(VillagePermissions.ALLOW_HIGHWAYS.getBit(), highwayAllowed);
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
        String[] hist = new String[]{};
        if (this.history.size() > 0) {
            int numbersToFetch = Math.min(numevents, this.history.size());
            hist = new String[numbersToFetch];
            HistoryEvent[] events = this.getHistoryEvents();
            for (int x = 0; x < numbersToFetch; ++x) {
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

    public final String toString() {
        return "Village [ID: " + this.id + ", Name: " + this.name + ", DeedId: " + this.deedid + ", Kingdom: " + Kingdoms.getNameFor(this.kingdom) + ", Size: " + (this.endx - this.startx) / 2 + ']';
    }

    public final void putGuardsAtToken() {
        Guard[] guardarr = this.getGuards();
        try {
            for (int x = 0; x < guardarr.length; ++x) {
                guardarr[x].getCreature().blinkTo(this.getToken().getTileX(), this.getToken().getTileY(), this.getToken().isOnSurface() ? 0 : -1, 0);
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
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
        float found = 0.0f;
        for (int x = this.getStartX(); x <= this.getEndX(); ++x) {
            for (int y = this.getStartY(); y <= this.getEndY(); ++y) {
                found += this.getNumCreaturesNotHumanOn(x, y, true, false);
                found += this.getNumCreaturesNotHumanOn(x, y, false, false);
            }
        }
        return found;
    }

    public final float getNumBrandedCreaturesNotHuman() {
        float found = 0.0f;
        for (int x = this.getStartX(); x <= this.getEndX(); ++x) {
            for (int y = this.getStartY(); y <= this.getEndY(); ++y) {
                found += this.getNumCreaturesNotHumanOn(x, y, true, true);
                found += this.getNumCreaturesNotHumanOn(x, y, false, true);
            }
        }
        return found;
    }

    private float getNumCreaturesNotHumanOn(int x, int y, boolean onSurface, boolean findBranded) {
        float found = 0.0f;
        VolaTile t = Zones.getTileOrNull(x, y, onSurface);
        if (t != null && t.getVillage() == this) {
            Item[] items;
            Creature[] crets;
            for (Creature c : crets = t.getCreatures()) {
                if (c.isHuman() || !c.isAnimal() && !c.isMonster()) continue;
                if (findBranded && c.isBrandedBy(this.getId())) {
                    found += 1.0f;
                    continue;
                }
                if (findBranded) continue;
                found += 1.0f;
            }
            for (Item i : items = t.getItems()) {
                if (i.getTemplateId() == 1311 && !i.isEmpty(true)) {
                    found += 1.0f;
                }
                if (i.isVehicle()) {
                    for (Item v : i.getAllItems(true)) {
                        if (v.getTemplateId() != 1311 || v.isEmpty(true)) continue;
                        found += 1.0f;
                    }
                }
                if (i.getTemplateId() != 1432) continue;
                for (Item item : i.getAllItems(true)) {
                    if (item.getTemplateId() != 1436 || item.isEmpty(true)) continue;
                    Item[] chickens = item.getAllItems(true);
                    for (int z = 0; z < chickens.length; ++z) {
                        found += 1.0f;
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

    @Override
    public int compareTo(Village aVillage) {
        return this.getName().compareTo(aVillage.getName());
    }

    public void convertOfflineCitizensToKingdom(byte newKingdom, boolean updateTimeStamp) {
        Citizen[] citiz;
        for (Citizen c : citiz = this.getCitizens()) {
            if (WurmId.getType(c.getId()) != 0) continue;
            try {
                Players.getInstance().getPlayer(c.getId());
            }
            catch (NoSuchPlayerException nsp) {
                PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(c.getId());
                if (updateTimeStamp) {
                    pinf.setChangedKingdom();
                }
                Players.convertPlayerToKingdom(c.getId(), newKingdom);
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
                for (int x = sx; x < ex; ++x) {
                    for (int y = sy; y < ey; ++y) {
                        this.convertCreatures(oldKingdom, newKingdom, x, y, true, convertOnlyCitizens, setTimeStamp);
                        this.convertCreatures(oldKingdom, newKingdom, x, y, false, convertOnlyCitizens, setTimeStamp);
                    }
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
        }
    }

    public boolean convertCreatures(byte oldkingdom, byte newkingdom, int x, int y, boolean tsurfaced, boolean convertOnlyCitizens, boolean setTimeStamp) {
        Creature[] crets;
        VolaTile t = Zones.getTileOrNull(x, y, tsurfaced);
        if (t != null && (crets = t.getCreatures()).length > 0) {
            for (int c = 0; c < crets.length; ++c) {
                if (crets[c].getKingdomId() != oldkingdom) continue;
                try {
                    GuardTower tower;
                    boolean convertedMayor = false;
                    Citizen mayor = this.getMayor();
                    if (mayor != null && crets[c].getWurmId() == mayor.getId()) {
                        try {
                            mayor.role = this.getRoleForStatus((byte)3);
                        }
                        catch (NoSuchRoleException e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                        convertedMayor = true;
                    }
                    if (!crets[c].isPlayer() || !convertOnlyCitizens || this.isCitizen(crets[c])) {
                        crets[c].setKingdomId(newkingdom, true, setTimeStamp);
                    }
                    if (crets[c].isKingdomGuard() && (tower = Kingdoms.getTower(crets[c])) != null && tower.getTower().getAuxData() != newkingdom) {
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
                    if (!convertedMayor) continue;
                    if (mayor != null) {
                        try {
                            mayor.role = this.getRoleForStatus((byte)2);
                        }
                        catch (NoSuchRoleException e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                        continue;
                    }
                    logger.log(Level.WARNING, "Mayor role became null while converting.");
                    continue;
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                }
            }
        }
        return true;
    }

    private final Twit createTwit(String message) {
        if (this.canTwit) {
            return new Twit(this.name, message, this.consumerKeyToUse, this.consumerSecretToUse, this.applicationToken, this.applicationSecret, true);
        }
        return null;
    }

    public final void twit(String message) {
        Twit t;
        if (this.isTwitEnabled() && (t = this.createTwit(message)) != null) {
            Twit.twit(t);
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
        return Math.min(30.0f, this.faithWar / this.faithDivideVal);
    }

    public float getFaithHealBonus() {
        return Math.min(30.0f, this.faithHeal / this.faithDivideVal);
    }

    public float getFaithCreateBonus() {
        return Math.min(30.0f, this.faithCreate / this.faithDivideVal);
    }

    public byte getSpawnSituation() {
        if (this.isCapital() || this.isPermanent) {
            return 1;
        }
        return this.spawnSituation;
    }

    public int getAllianceNumber() {
        return this.allianceNumber;
    }

    public void addHotaWin() {
        for (Citizen citizen : this.citizens.values()) {
            PlayerInfo pinf;
            if (!citizen.isPlayer() || (pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(citizen.getId())) == null) continue;
            pinf.setHotaWins((short)(pinf.getHotaWins() + 1));
        }
        this.setHotaWins((short)(this.hotaWins + 1));
    }

    public final void createHotaPrize(int winStreak) {
        try {
            Item lump;
            int x;
            Item statue = ItemFactory.createItem(742, 99.0f, null);
            int material = 7;
            if (winStreak > 50) {
                material = 56;
            } else if (winStreak > 40) {
                material = 57;
            } else if (winStreak > 30) {
                material = 54;
            } else if (winStreak > 15) {
                material = 52;
            }
            statue.setMaterial((byte)material);
            float posX = this.getToken().getPosX() - 2.0f + Server.rand.nextFloat() * 4.0f;
            float posY = this.getToken().getPosY() - 2.0f + Server.rand.nextFloat() * 4.0f;
            statue.setPosXYZRotation(posX, posY, Zones.calculateHeight(posX, posY, true), Server.rand.nextInt(350));
            for (int i = 0; i < winStreak; ++i) {
                if (i / 11 == winStreak % 11) {
                    statue.setAuxData((byte)0);
                    statue.setData1(1);
                    continue;
                }
                statue.setAuxData((byte)winStreak);
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
            for (Citizen c : this.citizens.values()) {
                if (Hota.getHelpValue(c.getId()) <= 0) continue;
                ++numHelpers;
            }
            numHelpers = Math.min(20, numHelpers);
            for (x = 0; x < numHelpers; ++x) {
                Item medallion = ItemFactory.createItem(740, Math.min(99, 80 + winStreak), null);
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
            for (x = 0; x < 5; ++x) {
                lump = ItemFactory.createItem(694, Math.min(99, 50 + winStreak), null);
                statue.insertItem(lump);
            }
            for (x = 0; x < 5; ++x) {
                lump = ItemFactory.createItem(698, Math.min(99, 50 + winStreak), null);
                statue.insertItem(lump);
            }
            z.addItem(statue);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
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
        if (this.plan.moneyLeft < 30000L) {
            return 0L;
        }
        return this.plan.moneyLeft - 30000L;
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
        ArrayList<Citizen> toReturn = new ArrayList<Citizen>();
        for (Citizen citizen : this.citizens.values()) {
            if (WurmId.getType(citizen.wurmId) != 1) continue;
            try {
                Creature c = Creatures.getInstance().getCreature(citizen.wurmId);
                if (!c.isNpcTrader()) continue;
                toReturn.add(citizen);
            }
            catch (NoSuchCreatureException noSuchCreatureException) {}
        }
        return toReturn;
    }

    public final boolean addVillageRecruitee(String pName, long pId) {
        VillageRecruitee newRecruit = new VillageRecruitee(this.getId(), pId, pName);
        if (this.addVillageRecruitee(newRecruit)) {
            this.saveRecruitee(newRecruit);
            return true;
        }
        return false;
    }

    public final boolean removeRecruitee(long wid) {
        for (VillageRecruitee vr : this.recruitees) {
            if (vr.getRecruiteeId() != wid) continue;
            this.deleteRecruitee(vr);
            return this.recruitees.remove(vr);
        }
        return false;
    }

    abstract void deleteRecruitee(VillageRecruitee var1);

    public final VillageRecruitee[] getRecruitees() {
        VillageRecruitee[] array = new VillageRecruitee[this.recruitees.size()];
        array = this.recruitees.toArray(array);
        return array;
    }

    public final boolean joinVillage(Player player) {
        VillageRecruitee vr = this.getRecruiteeById(player.getWurmId());
        if (vr == null) {
            player.getCommunicator().sendNormalServerMessage("You are not on the village recruitment list.");
            return false;
        }
        if (player.getCitizenVillage() != null && player.getCitizenVillage().isMayor(player.getWurmId())) {
            player.getCommunicator().sendNormalServerMessage("You may not join a village while being the mayor of another village.");
            return false;
        }
        if (player.isPlayer() && player.mayChangeVillageInMillis() > 0L) {
            player.getCommunicator().sendNormalServerMessage("You may not change village until " + Server.getTimeFor(player.mayChangeVillageInMillis()) + " has elapsed.");
            return false;
        }
        if (this.kingdom != player.getKingdomId()) {
            player.getCommunicator().sendNormalServerMessage("You must be of the same kingdom as the village you are trying to join.");
            return false;
        }
        try {
            this.addCitizen(player, this.getRoleForStatus((byte)3));
            if (player.canUseFreeVillageTeleport()) {
                VillageTeleportQuestion vtq = new VillageTeleportQuestion(player);
                vtq.sendQuestion();
            }
            this.removeRecruitee(player.getWurmId());
            return true;
        }
        catch (IOException iox) {
            logger.log(Level.INFO, "Failed to add " + player.getName() + " to settlement " + this.getName() + "." + iox.getMessage(), iox);
            player.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
        }
        catch (NoSuchRoleException nsr) {
            logger.log(Level.INFO, "Failed to add " + player.getName() + " to settlement " + this.getName() + "." + nsr.getMessage(), nsr);
            player.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
        }
        return false;
    }

    protected final boolean addVillageRecruitee(VillageRecruitee recruitee) {
        if (this.recruiteeExists(recruitee)) {
            return false;
        }
        return this.recruitees.add(recruitee);
    }

    private final VillageRecruitee getRecruiteeById(long wid) {
        for (VillageRecruitee vr : this.recruitees) {
            if (vr.getRecruiteeId() != wid) continue;
            return vr;
        }
        return null;
    }

    private final boolean recruiteeExists(VillageRecruitee recruitee) {
        for (VillageRecruitee vr : this.recruitees) {
            if (vr.getRecruiteeId() != recruitee.getRecruiteeId()) continue;
            return true;
        }
        return false;
    }

    public final boolean addVillageMapAnnotation(MapAnnotation annotation2, boolean send) {
        if (this.villageMapAnnotations.size() < 500) {
            this.villageMapAnnotations.add(annotation2);
            if (send) {
                this.sendMapAnnotationsToVillagers(new MapAnnotation[]{annotation2});
            }
            return true;
        }
        return false;
    }

    public void removeVillageMapAnnotation(MapAnnotation annotation2) {
        if (this.villageMapAnnotations.contains(annotation2)) {
            this.villageMapAnnotations.remove(annotation2);
            try {
                MapAnnotation.deleteAnnotation(annotation2.getId());
                this.sendRemoveMapAnnotationToVillagers(annotation2);
            }
            catch (IOException iex) {
                logger.log(Level.WARNING, "Error when deleting annotation: " + annotation2.getId() + " : " + iex.getMessage(), iex);
            }
        }
    }

    public final Set<MapAnnotation> getVillageMapAnnotations() {
        return this.villageMapAnnotations;
    }

    public final MapAnnotation[] getVillageMapAnnotationsArray() {
        if (this.villageMapAnnotations == null || this.villageMapAnnotations.size() == 0) {
            return null;
        }
        MapAnnotation[] annotations = new MapAnnotation[this.villageMapAnnotations.size()];
        this.villageMapAnnotations.toArray(annotations);
        return annotations;
    }

    public void sendMapAnnotationsToVillagers(MapAnnotation[] annotations) {
        if (this.group != null && annotations != null) {
            this.group.sendMapAnnotation(annotations);
        }
    }

    public void sendRemoveMapAnnotationToVillagers(MapAnnotation annotation2) {
        if (this.group != null) {
            this.group.sendRemoveMapAnnotation(annotation2);
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
        int y;
        int x;
        logger.info("Calculating outside spawn for " + this.getName());
        boolean surfaced = this.isOnSurface();
        if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
            return new short[]{(short)(this.getStartX() - 5), (short)(this.getStartY() - 5)};
        }
        if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
            return new short[]{(short)(this.getEndX() + 5), (short)(this.getStartY() - 5)};
        }
        if (Zones.isGoodTileForSpawn(this.getEndX() + 5, this.getStartY() - 5, surfaced)) {
            return new short[]{(short)(this.getEndX() + 5), (short)(this.getEndY() + 5)};
        }
        if (Zones.isGoodTileForSpawn(this.getStartX() - 5, this.getStartY() - 5, surfaced)) {
            return new short[]{(short)(this.getStartX() - 5), (short)(this.getEndY() + 5)};
        }
        int tilex = this.getStartX() - 5;
        int tiley = this.getStartY() - 5;
        for (x = 1; x < 20; ++x) {
            if (!Zones.isGoodTileForSpawn(tilex - x, tiley, surfaced)) continue;
            return new short[]{(short)(tilex - x), (short)tiley};
        }
        for (y = 1; y < 20; ++y) {
            if (!Zones.isGoodTileForSpawn(tilex, tiley - y, surfaced)) continue;
            return new short[]{(short)tilex, (short)(tiley - y)};
        }
        tilex = this.getEndX() + 5;
        tiley = this.getEndY() + 5;
        for (x = 1; x < 20; ++x) {
            if (!Zones.isGoodTileForSpawn(tilex + x, tiley, surfaced)) continue;
            return new short[]{(short)(tilex + x), (short)tiley};
        }
        for (y = 1; y < 20; ++y) {
            if (!Zones.isGoodTileForSpawn(tilex, tiley + y, surfaced)) continue;
            return new short[]{(short)tilex, (short)(tiley + y)};
        }
        tilex = this.getEndX() + 5;
        tiley = this.getStartY() - 5;
        for (x = 1; x < 20; ++x) {
            if (!Zones.isGoodTileForSpawn(tilex + x, tiley, surfaced)) continue;
            return new short[]{(short)(tilex + x), (short)tiley};
        }
        for (y = 1; y < 20; ++y) {
            if (!Zones.isGoodTileForSpawn(tilex, tiley - y, surfaced)) continue;
            return new short[]{(short)tilex, (short)(tiley - y)};
        }
        tilex = this.getStartX() - 5;
        tiley = this.getEndY() + 5;
        for (x = 1; x < 20; ++x) {
            if (!Zones.isGoodTileForSpawn(tilex - x, tiley, surfaced)) continue;
            return new short[]{(short)(tilex - x), (short)tiley};
        }
        for (y = 1; y < 20; ++y) {
            if (!Zones.isGoodTileForSpawn(tilex, tiley + y, surfaced)) continue;
            return new short[]{(short)tilex, (short)(tiley + y)};
        }
        return new short[]{-1, -1};
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
        for (Item marker : Items.getMarkers()) {
            if (!this.coversPlus(marker.getTileX(), marker.getTileY(), 2)) continue;
            return true;
        }
        return false;
    }

    public static enum VillagePermissions implements Permissions.IPermission
    {
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
        private static final Permissions.Allow[] types;

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

        static {
            types = Permissions.Allow.values();
        }
    }
}

