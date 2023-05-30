package com.wurmonline.server;

import coffee.keenan.network.wrappers.upnp.UPNPService;
import com.wurmonline.communication.ServerListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.communication.SocketServer;
import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.batchjobs.PlayerBatchJob;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.TerraformingTask;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Arrows;
import com.wurmonline.server.combat.Battles;
import com.wurmonline.server.combat.ServerProjectile;
import com.wurmonline.server.combat.WeaponCreator;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureTemplateCreator;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MineDoorSettings;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.deities.DbRitual;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.LocalSupplyDemand;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.EpicMapListener;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.EpicXmlWriter;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.server.highways.HighwayFinder;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.intra.IntraCommand;
import com.wurmonline.server.intra.IntraServer;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.intra.TimeSync;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.BodyDbStrings;
import com.wurmonline.server.items.CoinDbStrings;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemDbStrings;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemRequirement;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemTemplateCreator;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.TradingWindow;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.loot.LootTableCreator;
import com.wurmonline.server.players.AchievementGenerator;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.AwardLadder;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.HackerIp;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerCommunicatorSender;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.players.WurmRecord;
import com.wurmonline.server.questions.Questions;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.SpellGenerator;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.statistics.Statistics;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.Trello;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.utils.DbIndexManager;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.RecruitmentAds;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageMessages;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.weather.Weather;
import com.wurmonline.server.webinterface.WcEpicKarmaCommand;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.CropTilePoller;
import com.wurmonline.server.zones.Dens;
import com.wurmonline.server.zones.ErrorChecks;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.TilePoller;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.Water;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.server.zones.ZonesUtility;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public final class Server
   extends TimerTask
   implements Runnable,
   ServerMonitoring,
   ServerListener,
   CounterTypes,
   MiscConstants,
   CreatureTemplateIds,
   TimeConstants,
   EpicMapListener {
   private SocketServer socketServer;
   private boolean isPS = false;
   private static final Logger logger = Logger.getLogger(Server.class.getName());
   private static Server instance = null;
   private static boolean EpicServer;
   private static boolean ChallengeServer;
   public static final Random rand = new Random();
   public static final Object SYNC_LOCK = new Object();
   public static final long SLEEP_TIME = 25L;
   private static final long LIGHTNING_INTERVAL = 5000L;
   private static final long DIRTY_MESH_ROW_SAVE_INTERVAL = 60000L;
   private static final long SKILL_POLL_INTERVAL = 21600000L;
   private static final long MACROING_RESET_INTERVAL = 14400000L;
   private static final long ARROW_POLL_INTERVAL = 100L;
   private static final long MAIL_POLL_INTERVAL = 364000L;
   private static final long RUBBLE_POLL_INTERVAL = 60000L;
   private static final long WATER_POLL_INTERVAL = 1000L;
   private static final float STORM_RAINY_THRESHOLD = 0.5F;
   private static final float STORM_CLOUDY_THRESHOLD = 0.5F;
   private static final long WEATHER_SET_INTERVAL = 70000L;
   private static short counter = 0;
   private List<Long> playersAtLogin;
   private static final ReentrantReadWriteLock PLAYERS_AT_LOGIN_RW_LOCK = new ReentrantReadWriteLock();
   private static boolean locked = false;
   private static short molRehanX = 438;
   private static short molRehanY = 2142;
   private static int newPremiums = 0;
   private static int expiredPremiums = 0;
   private static long lastResetNewPremiums = 0L;
   private static long lastPolledSupplyDepots = 0L;
   private static long savedChallengePage = System.currentTimeMillis() + 120000L;
   private static int oldPremiums = 0;
   private static long lastResetOldPremiums = 0L;
   public static MeshIO surfaceMesh;
   public static MeshIO caveMesh;
   public static MeshIO resourceMesh;
   public static MeshIO rockMesh;
   public static HexMap epicMap;
   private static MeshIO flagsMesh;
   private static final int bitBonatize = 128;
   private static final int bitForage = 64;
   private static final int bitGather = 32;
   private static final int bitInvestigate = 16;
   private static final int bitGrubs = 2048;
   private static final int bitHiveCheck = 1024;
   private static final int bitBeingTransformed = 512;
   private static final int bitTransformed = 256;
   private boolean needSeeds = false;
   private static List<Creature> creaturesToRemove = new ArrayList<>();
   private static final ReentrantReadWriteLock CREATURES_TO_REMOVE_RW_LOCK = new ReentrantReadWriteLock();
   private static final Set<WebCommand> webcommands = new HashSet<>();
   private static final Set<TerraformingTask> terraformingTasks = new HashSet<>();
   public static final ReentrantReadWriteLock TERRAFORMINGTASKS_RW_LOCK = new ReentrantReadWriteLock();
   public static final ReentrantReadWriteLock WEBCOMMANDS_RW_LOCK = new ReentrantReadWriteLock();
   public static int lagticks = 0;
   public static float lastLagticks = 0.0F;
   public static int lagMoveModifier = 0;
   private static int lastSentWarning = 0;
   private static long lastAwardedBattleCamps = System.currentTimeMillis();
   private static long startTime = System.currentTimeMillis();
   private static long lastSecond = System.currentTimeMillis();
   private static long lastPolledRubble = 0L;
   private static long lastPolledShopCultist = System.currentTimeMillis();
   private static Map<String, Boolean> ips = new ConcurrentHashMap<>();
   private static ConcurrentLinkedQueue<PendingAward> pendingAwards = new ConcurrentLinkedQueue<>();
   private static int numips = 0;
   private static int logons = 0;
   private static int logonsPrem = 0;
   private static int newbies = 0;
   private static volatile long millisToShutDown = Long.MIN_VALUE;
   private static long lastPinged = 0L;
   private static long lastDeletedPlayer = 0L;
   private static long lastLoweredRanks = System.currentTimeMillis() + 600000L;
   private static volatile String shutdownReason = "Reason: unknown";
   private static List<Long> finalLogins = new ArrayList<>();
   private static final ReentrantReadWriteLock FINAL_LOGINS_RW_LOCK = new ReentrantReadWriteLock();
   private static boolean pollCommunicators = false;
   public static final int VILLAGE_POLL_MOD = 4000;
   private long lastTicked = 0L;
   private static long lastWeather = 0L;
   private static long lastArrow = 0L;
   private static long lastMailCheck = System.currentTimeMillis();
   private static long lastFaith = 0L;
   private static long lastRecruitmentPoll = 0L;
   private static long lastAwardedItems = System.currentTimeMillis();
   private static int lostConnections = 0;
   private long nextTerraformPoll = System.currentTimeMillis();
   private static int totalTicks = 0;
   private static int commPollCounter = 0;
   private static int commPollCounterInit = 1;
   private long lastLogged = 0L;
   private static long lastPolledBanks = 0L;
   private static long lastPolledWater = 0L;
   private static long lastPolledHighwayFinder = 0L;
   private byte[] externalIp = new byte[4];
   private byte[] internalIp = new byte[4];
   private static final Weather weather = new Weather();
   private boolean thunderMode = false;
   private long lastFlash = 0L;
   private IntraServer intraServer;
   private final List<IntraCommand> intraCommands = new LinkedList<>();
   private static final ReentrantReadWriteLock INTRA_COMMANDS_RW_LOCK = new ReentrantReadWriteLock();
   private long lastClearedFaithGain = 0L;
   private static int exceptions = 0;
   private static int secondsLag = 0;
   public static String alertMessage1 = "";
   public static long lastAlertMess1 = Long.MAX_VALUE;
   public static String alertMessage2 = "";
   public static long lastAlertMess2 = Long.MAX_VALUE;
   public static String alertMessage3 = "";
   public static long lastAlertMess3 = Long.MAX_VALUE;
   public static String alertMessage4 = "";
   public static long lastAlertMess4 = Long.MAX_VALUE;
   public static long timeBetweenAlertMess1 = Long.MAX_VALUE;
   public static long timeBetweenAlertMess2 = Long.MAX_VALUE;
   public static long timeBetweenAlertMess3 = Long.MAX_VALUE;
   public static long timeBetweenAlertMess4 = Long.MAX_VALUE;
   private static long lastPolledSkills = 0L;
   private static long lastPolledRifts = 0L;
   private static long lastResetAspirations = System.currentTimeMillis();
   private static long lastPolledTileEffects = System.currentTimeMillis();
   private static long lastResetTiles = System.currentTimeMillis();
   private static int combatCounter = 0;
   private static int secondsUptime = 0;
   private ScheduledExecutorService scheduledExecutorService;
   public static boolean allowTradeCheat = true;
   private ExecutorService mainExecutorService;
   private static final int EXECUTOR_SERVICE_NUMBER_OF_THREADS = 20;
   private static PlayerCommunicatorSender playerCommunicatorSender;
   private static boolean appointedSixThousand = false;
   static final double FMOD = 1.3571428F;
   static final double RMOD = 0.16666667F;
   public static int playersThroughTutorial = 0;
   public Water waterThread = null;
   public HighwayFinder highwayFinderThread = null;
   private static Map<Integer, Short> lowDirtHeight = new ConcurrentHashMap<>();
   private static Set<Integer> newYearEffects = new HashSet<>();
   public SteamHandler steamHandler = new SteamHandler();
   private static final ConcurrentHashMap<Long, Long> tempEffects = new ConcurrentHashMap<>();

   public static Server getInstance() {
      while(locked) {
         try {
            Thread.sleep(1000L);
            logger.log(Level.INFO, "Thread sleeping 1 second waiting for server to start.");
         } catch (InterruptedException var1) {
         }
      }

      if (instance == null) {
         try {
            locked = true;
            instance = new Server();
            locked = false;
         } catch (Exception var2) {
            logger.log(Level.SEVERE, "Failed to create server instance... shutting down.", (Throwable)var2);
            System.exit(0);
         }
      }

      return instance;
   }

   public void addCreatureToRemove(Creature creature) {
      CREATURES_TO_REMOVE_RW_LOCK.writeLock().lock();

      try {
         creaturesToRemove.add(creature);
      } finally {
         CREATURES_TO_REMOVE_RW_LOCK.writeLock().unlock();
      }
   }

   public void startShutdown(int seconds, String reason) {
      millisToShutDown = (long)seconds * 1000L;
      shutdownReason = "Reason: " + reason;
      int mins = seconds / 60;
      int secs = seconds - mins * 60;
      StringBuffer buf = new StringBuffer();
      if (mins > 0) {
         buf.append(mins + " minute");
         if (mins > 1) {
            buf.append("s");
         }
      }

      if (secs > 0) {
         if (mins > 0) {
            buf.append(" and ");
         }

         buf.append(secs + " seconds");
      }

      this.broadCastAlert("The server is shutting down in " + buf.toString() + ". " + shutdownReason, true, (byte)0);
   }

   private void removeCreatures() {
      CREATURES_TO_REMOVE_RW_LOCK.writeLock().lock();

      try {
         Creature[] crets = creaturesToRemove.toArray(new Creature[creaturesToRemove.size()]);

         for(Creature lCret : crets) {
            if (lCret instanceof Player) {
               Players.getInstance().logoutPlayer((Player)lCret);
            } else {
               Creatures.getInstance().removeCreature(lCret);
            }

            creaturesToRemove.remove(lCret);
         }

         if (creaturesToRemove.size() > 0) {
            logger.log(Level.WARNING, "Okay something is weird here. Deleting list. Debug more.");
            creaturesToRemove = new ArrayList<>();
         }
      } finally {
         CREATURES_TO_REMOVE_RW_LOCK.writeLock().unlock();
      }
   }

   private Server() throws Exception {
   }

   @Override
   public boolean isLagging() {
      return lagticks >= 2000;
   }

   public void setExternalIp() {
      StringTokenizer tokens = new StringTokenizer(Servers.localServer.EXTERNALIP, ".");

      for(int x = 0; tokens.hasMoreTokens(); ++x) {
         String next = tokens.nextToken();
         this.externalIp[x] = Integer.valueOf(next).byteValue();
      }
   }

   private void setInternalIp() {
      StringTokenizer tokens = new StringTokenizer(Servers.localServer.INTRASERVERADDRESS, ".");

      for(int x = 0; tokens.hasMoreTokens(); ++x) {
         String next = tokens.nextToken();
         this.internalIp[x] = Integer.valueOf(next).byteValue();
      }
   }

   private void initialiseExecutorService(int aNumberOfThreads) {
      logger.info("Initialising ExecutorService with NumberOfThreads: " + aNumberOfThreads);
      this.mainExecutorService = Executors.newFixedThreadPool(aNumberOfThreads);
   }

   public ExecutorService getMainExecutorService() {
      return this.mainExecutorService;
   }

   public void startRunning() throws Exception {
      Constants.logConstantValues(false);
      addShutdownHook();
      this.logCodeVersionInformation();
      DbConnector.initialize();
      if (Constants.dbAutoMigrate) {
         if (DbConnector.hasPendingMigrations() && DbConnector.performMigrations().isError()) {
            throw new WurmServerException("Could not perform migrations successfully, they must either be performed manually or disabled.");
         }
      } else {
         logger.info("Database auto-migration is not enabled - skipping migrations checks");
      }

      if (Constants.checkAllDbTables) {
         DbUtilities.performAdminOnAllTables(DbConnector.getLoginDbCon(), DbUtilities.DbAdminAction.CHECK_MEDIUM);
      } else {
         logger.info("checkAllDbTables is false so not checking database tables for errors.");
      }

      if (Constants.analyseAllDbTables) {
         DbUtilities.performAdminOnAllTables(DbConnector.getLoginDbCon(), DbUtilities.DbAdminAction.ANALYZE);
      } else {
         logger.info("analyseAllDbTables is false so not analysing database tables to update indices.");
      }

      if (Constants.optimiseAllDbTables) {
         DbUtilities.performAdminOnAllTables(DbConnector.getLoginDbCon(), DbUtilities.DbAdminAction.OPTIMIZE);
      } else {
         logger.info("OptimizeAllDbTables is false so not optimising database tables.");
      }

      Servers.loadAllServers(false);
      if (Constants.useDirectByteBuffersForMeshIO) {
         MeshIO.setAllocateDirectBuffers(true);
      }

      if (this.steamHandler.getIsOfflineServer()) {
         Servers.localServer.EXTERNALIP = "0.0.0.0";
         Servers.localServer.INTRASERVERADDRESS = "0.0.0.0";
      }

      this.loadWorldMesh();
      this.loadCaveMesh();
      this.loadResourceMesh();
      this.loadRockMesh();
      this.loadFlagsMesh();
      logger.info("Max height: " + getMaxHeight());

      try {
         boolean start = Features.Feature.SURFACEWATER.isEnabled();
      } catch (Exception var10) {
         throw var10;
      }

      if (Features.Feature.SURFACEWATER.isEnabled()) {
         Water.loadWaterMesh();
      }

      surfaceMesh.calcDistantTerrain();
      Features.loadAllFeatures();
      MessageServer.initialise();
      PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();

      try {
         this.playersAtLogin = new ArrayList<>();
      } finally {
         PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
      }

      Groups.addGroup(new Group("wurm"));
      EpicServer = Servers.localServer.EPIC;
      ChallengeServer = Servers.localServer.isChallengeServer();
      logger.log(Level.INFO, "Protocol: 250990585");
      ItemTemplateCreator.initialiseItemTemplates();
      SpellGenerator.createSpells();
      CreatureTemplateCreator.createCreatureTemplates();
      LootTableCreator.initializeLootTables();
      if (Constants.createTemporaryDatabaseIndicesAtStartup) {
         DbIndexManager.createIndexes();
      } else {
         logger.warning(
            "createTemporaryDatabaseIndicesAtStartup is false so not creating indices. This is only for development and should not happen in production"
         );
      }

      if (Features.Feature.CROP_POLLER.isEnabled()) {
         CropTilePoller.initializeFields();
      }

      if (Constants.RUNBATCH) {
      }

      if (Constants.crashed) {
         PlayerBatchJob.reimburseFatigue();
      } else if (!Servers.localServer.LOGINSERVER) {
         Constants.crashed = true;
      }

      EffectFactory.getInstance().loadEffects();
      AnimalSettings.loadAll();
      ItemSettings.loadAll();
      DoorSettings.loadAll();
      StructureSettings.loadAll();
      MineDoorSettings.loadAll();
      PermissionsHistories.loadAll();
      Items.loadAllItemData();
      Items.loadAllItempInscriptionData();
      Items.loadAllStaticItems();
      Items.loadAllZoneItems(BodyDbStrings.getInstance());
      Items.loadAllZoneItems(ItemDbStrings.getInstance());
      Items.loadAllZoneItems(CoinDbStrings.getInstance());
      ItemRequirement.loadAllItemRequirements();
      ArmourTemplate.initialize();
      WeaponCreator.createWeapons();
      Banks.loadAllBanks();
      Wall.loadAllWalls();
      Floor.loadAllFloors();
      BridgePart.loadAllBridgeParts();
      Kingdom.loadAllKingdoms();
      King.loadAllEra();
      Cooldowns.loadAllCooldowns();
      TilePoller.mask = (1 << Constants.meshSize) * (1 << Constants.meshSize) - 1;
      Zones.getZone(0, 0, true);
      Villages.loadVillages();
      if (Features.Feature.HIGHWAYS.isEnabled()) {
         this.highwayFinderThread = new HighwayFinder();
         this.highwayFinderThread.start();
         Routes.generateAllRoutes();
         if (Features.Feature.WAGONER.isEnabled()) {
            Delivery.dbLoadAllDeliveries();
            Wagoner.dbLoadAllWagoners();
         }
      }

      try {
         CreaturePos.loadAllPositions();
         Creatures.getInstance().loadAllCreatures();
      } catch (NoSuchCreatureException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         System.exit(0);
         return;
      }

      Villages.loadDeadVillages();
      Villages.loadCitizens();
      Villages.loadGuards();
      fixHoles();
      Items.loadAllItemEffects();
      MineDoorPermission.loadAllMineDoors();
      Zones.loadTowers();
      PvPAlliance.loadPvPAlliances();
      Villages.loadWars();
      Villages.loadWarDeclarations();
      RecruitmentAds.loadRecruitmentAds();
      Zones.addWarDomains();
      long start = System.nanoTime();
      Economy.getEconomy();
      if (Servers.localServer.getKingsmoneyAtRestart() > 0) {
         Economy.getEconomy().getKingsShop().setMoney((long)Servers.localServer.getKingsmoneyAtRestart());
      }

      logger.log(Level.INFO, "Loading economy took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
      EndGameItems.loadEndGameItems();
      if (!Servers.localServer.HOMESERVER && Items.getWarTargets().length == 0) {
      }

      if ((!Servers.localServer.EPIC || !Servers.localServer.HOMESERVER) && Items.getSourceSprings().length == 0) {
         Zones.shouldSourceSprings = true;
      }

      if (!Features.Feature.NEWDOMAINS.isEnabled()) {
         Zones.checkAltars();
      }

      PlayerInfoFactory.loadPlayerInfos();
      WurmRecord.loadAllChampRecords();
      Affinities.loadAffinities();
      PlayerInfoFactory.loadReferers();
      Dens.loadDens();
      Reimbursement.loadAll();
      PendingAccount.loadAllPendingAccounts();
      PasswordTransfer.loadAllPasswordTransfers();
      Trap.loadAllTraps();
      this.setExternalIp();
      this.setInternalIp();
      AchievementGenerator.generateAchievements();
      Achievements.loadAllAchievements();
      if (Constants.isGameServer) {
         Zones.writeZones();
      }

      if (Constants.dropTemporaryDatabaseIndicesAtStartup) {
         DbIndexManager.removeIndexes();
      } else {
         logger.warning(
            "dropTemporaryDatabaseIndicesAtStartup is false so not dropping indices. This is only for development and should not happen in production"
         );
      }

      TilePoller.entryServer = Servers.localServer.entryServer;
      WcEpicKarmaCommand.loadAllKarmaHelpers();
      FocusZone.loadAll();
      Hota.loadAllHotaItems();
      Hota.loadAllHelpers();
      if (Constants.createSeeds || this.needSeeds) {
         Zones.createSeeds();
      }

      if (Servers.localServer.testServer) {
         Zones.createInvestigatables();
      }

      this.intraServer = new IntraServer(this);
      Statistics.getInstance().startup(logger);
      WurmHarvestables.setStartTimes();
      WurmMail.loadAllMails();
      HistoryManager.loadHistory();
      Cultist.loadAllCultists();
      Effectuator.loadEffects();
      EpicServerStatus.loadLocalEntries();
      Tickets.loadTickets();
      VoteQuestions.loadVoteQuestions();
      PlayerVotes.loadAllPlayerVotes();
      Recipes.loadAllRecipes();
      ItemMealData.loadAllMealData();
      AffinitiesTimed.loadAllPlayerTimedAffinities();
      VillageMessages.loadVillageMessages();
      if (Constants.RUNBATCH) {
      }

      Constants.RUNBATCH = false;
      if (Constants.useMultiThreadedBankPolling || Constants.useQueueToSendDataToPlayers) {
         this.initialiseExecutorService(20);
         this.initialisePlayerCommunicatorSender();
      }

      this.setupScheduledExecutors();
      Eigc.loadAllAccounts();
      this.socketServer = new SocketServer(
         this.externalIp, Integer.parseInt(Servers.localServer.EXTERNALPORT), Integer.parseInt(Servers.localServer.EXTERNALPORT) + 1, this
      );
      SocketServer.MIN_MILLIS_BETWEEN_CONNECTIONS = Constants.minMillisBetweenPlayerConns;
      logger.log(
         Level.INFO,
         "The Wurm Server is listening on ip "
            + Servers.localServer.EXTERNALIP
            + " and port "
            + Servers.localServer.EXTERNALPORT
            + ". Min time between same ip connections="
            + SocketServer.MIN_MILLIS_BETWEEN_CONNECTIONS
      );
      commPollCounterInit = 1;
      if (!Servers.localServer.PVPSERVER && Zones.worldTileSizeX > 5000) {
         commPollCounterInit = 6;
      }

      logger.log(Level.INFO, "commPollCounterInit=" + commPollCounterInit);
      if (Constants.useScheduledExecutorForServer) {
         ScheduledExecutorService scheduledServerRunExecutor = Executors.newScheduledThreadPool(Constants.scheduledExecutorServiceThreads);

         for(int i = 0; i < Constants.scheduledExecutorServiceThreads; ++i) {
            scheduledServerRunExecutor.scheduleWithFixedDelay(this, (long)(i * 2), 25L, TimeUnit.MILLISECONDS);
         }
      } else {
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(this, 0L, 25L);
         startTime = System.currentTimeMillis();
      }

      Missions.getAllMissions();
      MissionTriggers.getAllTriggers();
      TriggerEffects.getAllEffects();
      if (Servers.localServer.LOGINSERVER) {
         epicMap = EpicServerStatus.getValrei();
         epicMap.loadAllEntities();
         epicMap.addListener(this);
         EpicXmlWriter.dumpEntities(epicMap);
      }

      if (Features.Feature.SURFACEWATER.isEnabled()) {
         this.waterThread = new Water();
         this.waterThread.loadSprings();
         this.waterThread.start();
      }

      if (Constants.startChallenge) {
         Servers.localServer.setChallengeStarted(System.currentTimeMillis());
         Servers.localServer.setChallengeEnds(System.currentTimeMillis() + (long)Constants.challengeDays * 86400000L);
         Servers.localServer.saveChallengeTimes();
         Constants.startChallenge = false;
      }

      ChallengeSummary.loadLocalChallengeScores();
      Creatures.getInstance().startPollTask();
      WaterType.calcWaterTypes();
      this.steamHandler.initializeSteam();
      this.steamHandler.createServer("wurmunlimitedserver", "wurmunlimitedserver", "Wurm Unlimited Server", "1.0.0.0");
      DbRitual.loadRiteEvents();
      DbRitual.loadRiteClaims();
      logger.info("End of game server initialisation");
   }

   private void setupScheduledExecutors() {
      if (Constants.useScheduledExecutorToWriteLogs
         || Constants.useScheduledExecutorToSaveConstants
         || Constants.useScheduledExecutorToTickCalendar
         || Constants.useScheduledExecutorToCountEggs
         || Constants.useScheduledExecutorToSaveDirtyMeshRows
         || Constants.useScheduledExecutorToSendTimeSync
         || Constants.useScheduledExecutorToSwitchFatigue
         || Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase
         || Constants.useScheduledExecutorToUpdateItemDamageInDatabase
         || Constants.useScheduledExecutorToUpdateItemOwnerInDatabase
         || Constants.useScheduledExecutorToUpdateItemLastOwnerInDatabase
         || Constants.useScheduledExecutorToUpdateItemParentInDatabase
         || Constants.useScheduledExecutorToConnectToTwitter
         || Constants.useScheduledExecutorToUpdatePlayerPositionInDatabase
         || Constants.useItemTransferLog
         || Constants.useTileEventLog) {
         this.scheduledExecutorService = Executors.newScheduledThreadPool(15);
      }

      if (Constants.useScheduledExecutorToWriteLogs) {
         logger.info("Going to use a ScheduledExecutorService to write logs");
         long lInitialDelay = 60L;
         long lDelay = 300L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
               if (Server.logger.isLoggable(Level.FINER)) {
                  Server.logger.finer("Running newSingleThreadScheduledExecutor for stat log writing");
               }
            }
         }, 60L, 300L, TimeUnit.SECONDS);
         long lPingDelay = 300L;
         long lInitialDelay2 = 5000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
               if (Server.logger.isLoggable(Level.FINEST)) {
               }

               try {
                  Servers.pingServers();
               } catch (RuntimeException var2) {
                  Server.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorServicePollServers while calling pingServers()", (Throwable)var2);
                  throw var2;
               }
            }
         }, 5000L, 300L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToCountEggs) {
         logger.info("Going to use a ScheduledExecutorService to count eggs");
         long lInitialDelay = 1000L;
         long lDelay = 3600000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new Items.EggCounter(), 1000L, 3600000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToSaveConstants) {
         logger.info("Going to use a ScheduledExecutorService to save Constants to wurm.ini");
         long lInitialDelay = 1000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new Constants.ConstantsSaver(), 1000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToSaveDirtyMeshRows) {
         logger.info("Going to use a ScheduledExecutorService to call MeshIO.saveNextDirtyRow()");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         long delayInterval = 250L;
         this.scheduledExecutorService
            .scheduleWithFixedDelay(
               new MeshSaver(surfaceMesh, "SurfaceMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS
            );
         lInitialDelay += 250L;
         this.scheduledExecutorService
            .scheduleWithFixedDelay(
               new MeshSaver(caveMesh, "CaveMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS
            );
         lInitialDelay += 250L;
         this.scheduledExecutorService
            .scheduleWithFixedDelay(
               new MeshSaver(rockMesh, "RockMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS
            );
         lInitialDelay += 250L;
         this.scheduledExecutorService
            .scheduleWithFixedDelay(
               new MeshSaver(resourceMesh, "ResourceMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS
            );
         lInitialDelay += 250L;
         this.scheduledExecutorService
            .scheduleWithFixedDelay(
               new MeshSaver(flagsMesh, "FlagsMesh", Constants.numberOfDirtyMeshRowsToSaveEachCall), lInitialDelay, 1000L, TimeUnit.MILLISECONDS
            );
      }

      if (Constants.useScheduledExecutorToSendTimeSync) {
         if (Servers.localServer.LOGINSERVER) {
            logger.warning("This is the login server so it will not send TimeSync commands");
         } else {
            logger.info("Going to use a ScheduledExecutorService to send TimeSync commands");
            long lInitialDelay = 1000L;
            long lDelay = 3600000L;
            this.scheduledExecutorService.scheduleWithFixedDelay(new TimeSync.TimeSyncSender(), 1000L, 3600000L, TimeUnit.MILLISECONDS);
         }
      }

      if (Constants.useScheduledExecutorToSwitchFatigue) {
         logger.info("Going to use a ScheduledExecutorService to switch fatigue");
         long lInitialDelay = 60000L;
         long lDelay = 86400000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new PlayerInfoFactory.FatigueSwitcher(), 60000L, 86400000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToTickCalendar) {
         logger.info("Going to use a ScheduledExecutorService to call WurmCalendar.tickSeconds()");
         long lInitialDelay = 125L;
         long lDelay = 125L;
         this.scheduledExecutorService.scheduleWithFixedDelay(new WurmCalendar.Ticker(), 125L, 125L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useItemTransferLog) {
         logger.info("Going to use a ScheduledExecutorService to log Item Transfers");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(Item.getItemlogger(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useTileEventLog) {
         logger.info("Going to use a ScheduledExecutorService to log tile events");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(TileEvent.getTilelogger(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update creature positions in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(CreaturePos.getCreatureDbPosUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdatePlayerPositionInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update player positions in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(CreaturePos.getPlayerDbPosUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdateItemDamageInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update item damage in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemDamageDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdateItemOwnerInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update item owner in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemOwnerDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdateItemLastOwnerInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update item last owner in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemLastOwnerDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToUpdateItemParentInDatabase) {
         logger.info("Going to use a ScheduledExecutorService to update item parent in database");
         long lInitialDelay = 60000L;
         long lDelay = 1000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(DbItem.getItemParentDatabaseUpdater(), 60000L, 1000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorToConnectToTwitter) {
         logger.info("Going to use a ScheduledExecutorService to connect to twitter");
         long lInitialDelay = 60000L;
         long lDelay = 5000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(Twit.getTwitterThread(), 60000L, 5000L, TimeUnit.MILLISECONDS);
      }

      if (Constants.useScheduledExecutorForTrello) {
         logger.info("Going to use a ScheduledExecutorService for maintaining tickets in Trello");
         long lInitialDelay = 5000L;
         long lDelay = 60000L;
         this.scheduledExecutorService.scheduleWithFixedDelay(Trello.getTrelloThread(), 5000L, 60000L, TimeUnit.MILLISECONDS);
      }
   }

   void twitLocalServer(String message) {
      Twit t = Servers.localServer.createTwit(message);
      if (t != null) {
         Twit.twit(t);
      }
   }

   private void logCodeVersionInformation() {
      try {
         Package p = Class.forName("com.wurmonline.server.Server").getPackage();
         if (p == null) {
            logger.warning("Wurm Build Date: UNKNOWN (Package.getPackage() is null!)");
         } else {
            logger.info("Wurm Impl Title: " + p.getImplementationTitle());
            logger.info("Wurm Impl Vendor: " + p.getImplementationVendor());
            logger.info("Wurm Impl Version: " + p.getImplementationVersion());
         }
      } catch (Exception var6) {
         logger.severe("Wurm version: UNKNOWN (Error getting version number from MANIFEST.MF)");
      }

      try {
         Package p = Class.forName("com.wurmonline.shared.constants.ProtoConstants").getPackage();
         if (p == null) {
            logger.warning("Wurm Common: UNKNOWN (Package.getPackage() is null!)");
         } else {
            logger.info("Wurm Common Impl Title: " + p.getImplementationTitle());
            logger.info("Wurm Common Impl Vendor: " + p.getImplementationVendor());
            logger.info("Wurm Common Impl Version: " + p.getImplementationVersion());
         }
      } catch (Exception var5) {
         logger.severe("Wurm Common: UNKNOWN (Error getting version number from MANIFEST.MF)");
      }

      try {
         Package p = Class.forName("com.mysql.jdbc.Driver").getPackage();
         if (p == null) {
            logger.warning("MySQL JDBC: UNKNOWN (Package.getPackage() is null!)");
         } else {
            logger.info("MySQL JDBC Spec Title: " + p.getSpecificationTitle());
            logger.info("MySQL JDBC Spec Vendor: " + p.getSpecificationVendor());
            logger.info("MySQL JDBC Spec Version: " + p.getSpecificationVersion());
            logger.info("MySQL JDBC Impl Title: " + p.getImplementationTitle());
            logger.info("MySQL JDBC Impl Vendor: " + p.getImplementationVendor());
            logger.info("MySQL JDBC Impl Version: " + p.getImplementationVersion());
         }
      } catch (Exception var4) {
         logger.severe("MySQL JDBC: UNKNOWN (Error getting version number from MANIFEST.MF)");
      }

      try {
         Package p = Class.forName("javax.mail.Message").getPackage();
         if (p == null) {
            logger.warning("Javax Mail: UNKNOWN (Package.getPackage() is null!)");
         } else {
            logger.info("Javax Mail Spec Title: " + p.getSpecificationTitle());
            logger.info("Javax Mail Spec Vendor: " + p.getSpecificationVendor());
            logger.info("Javax Mail Spec Version: " + p.getSpecificationVersion());
            logger.info("Javax Mail Impl Title: " + p.getImplementationTitle());
            logger.info("Javax Mail Impl Vendor: " + p.getImplementationVendor());
            logger.info("Javax Mail Impl Version: " + p.getImplementationVersion());
         }
      } catch (Exception var3) {
         logger.severe("Javax Mail: UNKNOWN (Error getting version number from MANIFEST.MF)");
      }

      try {
         Package p = Class.forName("javax.activation.DataSource").getPackage();
         if (p == null) {
            logger.warning("Javax Activation: UNKNOWN (Package.getPackage() is null!)");
         } else {
            logger.info("Javax Activation Spec Title: " + p.getSpecificationTitle());
            logger.info("Javax Activation Spec Vendor: " + p.getSpecificationVendor());
            logger.info("Javax Activation Spec Version: " + p.getSpecificationVersion());
            logger.info("Javax Activation Impl Title: " + p.getImplementationTitle());
            logger.info("Javax Activation Impl Vendor: " + p.getImplementationVendor());
            logger.info("Javax Activation Impl Version: " + p.getImplementationVersion());
         }
      } catch (Exception var2) {
         logger.severe("Javax Activation: UNKNOWN (Error getting version number from MANIFEST.MF)");
      }
   }

   public void initialisePlayerCommunicatorSender() {
      if (Constants.useQueueToSendDataToPlayers) {
         playerCommunicatorSender = new PlayerCommunicatorSender();
         this.getMainExecutorService().execute(playerCommunicatorSender);
      }
   }

   private static void fixHoles() {
      logger.log(Level.INFO, "Fixing cave entrances.");
      long start = System.nanoTime();
      int found = 0;
      int fixed = 0;
      int fixed2 = 0;
      int fixed3 = 0;
      int fixed4 = 0;
      int fixed5 = 0;
      int fixedWalls = 0;
      int min = 0;
      int ms = Constants.meshSize;
      int max = 1 << ms;

      for(int x = 0; x < max; ++x) {
         for(int y = 0; y < max; ++y) {
            int tile = surfaceMesh.getTile(x, y);
            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_HOLE.id) {
               ++found;
               boolean fix = false;
               int t = caveMesh.getTile(x, y);
               if (Tiles.decodeType(t) != Tiles.Tile.TILE_CAVE_EXIT.id) {
                  ++fixed;
                  setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte)0);
               } else {
                  for(int xx = 0; xx <= 1; ++xx) {
                     for(int yy = 0; yy <= 1; ++yy) {
                        int tt = caveMesh.getTile(x + xx, y + yy);
                        if (Tiles.decodeHeight(tt) == -100 && Tiles.decodeData(tt) == 0) {
                           fix = true;
                           break;
                        }
                     }
                  }

                  if (fix) {
                     ++fixed2;

                     for(int xx = 0; xx <= 1; ++xx) {
                        for(int yy = 0; yy <= 1; ++yy) {
                           caveMesh.setTile(x + xx, y + yy, Tiles.encode((short)-100, TileRockBehaviour.prospect(x + xx, y + yy, false), (byte)0));
                        }
                     }

                     setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte)0);
                  }
               }

               if (!fix) {
                  int lowestX = 100000;
                  int lowestY = 100000;
                  int nextLowestX = lowestX;
                  int nextLowestY = lowestY;
                  int lowestHeight = 100000;
                  int nextLowestHeight = lowestHeight;

                  for(int xa = 0; xa <= 1; ++xa) {
                     for(int ya = 0; ya <= 1; ++ya) {
                        if (x + xa < max && y + ya < max) {
                           int rockTile = rockMesh.getTile(x + xa, y + ya);
                           int rockHeight = Tiles.decodeHeight(rockTile);
                           if (rockHeight <= lowestHeight) {
                              if (lowestHeight < nextLowestHeight && TileRockBehaviour.isAdjacent(lowestX, lowestY, x + xa, y + ya)) {
                                 nextLowestHeight = lowestHeight;
                                 nextLowestX = lowestX;
                                 nextLowestY = lowestY;
                              }

                              lowestHeight = rockHeight;
                              lowestX = x + xa;
                              lowestY = y + ya;
                           } else if (rockHeight <= nextLowestHeight
                              && nextLowestHeight > lowestHeight
                              && TileRockBehaviour.isAdjacent(lowestX, lowestY, x + xa, y + ya)) {
                              nextLowestHeight = rockHeight;
                              nextLowestX = x + xa;
                              nextLowestY = y + ya;
                           }
                        }
                     }
                  }

                  if (lowestX != 100000 && lowestY != 100000 && nextLowestX != 100000 && nextLowestY != 100000) {
                     int lowestRock = rockMesh.getTile(lowestX, lowestY);
                     int nextLowestRock = rockMesh.getTile(nextLowestX, nextLowestY);
                     int lowestCave = caveMesh.getTile(lowestX, lowestY);
                     int nextLowestCave = caveMesh.getTile(nextLowestX, nextLowestY);
                     int lowestSurf = surfaceMesh.getTile(lowestX, lowestY);
                     int nextLowestSurf = surfaceMesh.getTile(nextLowestX, nextLowestY);
                     short lrockHeight = Tiles.decodeHeight(lowestRock);
                     short nlrockHeight = Tiles.decodeHeight(nextLowestRock);
                     short lcaveHeight = Tiles.decodeHeight(lowestCave);
                     short nlcaveHeight = Tiles.decodeHeight(nextLowestCave);
                     short lsurfHeight = Tiles.decodeHeight(lowestSurf);
                     short nlsurfHeight = Tiles.decodeHeight(nextLowestSurf);
                     if (lcaveHeight != lrockHeight || Tiles.decodeData(lowestCave) != 0) {
                        ++fixed4;
                        caveMesh.setTile(lowestX, lowestY, Tiles.encode(lrockHeight, Tiles.decodeType(lowestCave), (byte)0));
                     }

                     if (nlcaveHeight != nlrockHeight || Tiles.decodeData(nextLowestCave) != 0) {
                        ++fixed4;
                        caveMesh.setTile(nextLowestX, nextLowestY, Tiles.encode(nlrockHeight, Tiles.decodeType(nextLowestCave), (byte)0));
                     }

                     if (lsurfHeight != lrockHeight) {
                        ++fixed5;
                        setSurfaceTile(lowestX, lowestY, lrockHeight, Tiles.decodeType(lowestSurf), Tiles.decodeData(lowestSurf));
                     }

                     if (nlsurfHeight != nlrockHeight) {
                        ++fixed5;
                        setSurfaceTile(nextLowestX, nextLowestY, nlrockHeight, Tiles.decodeType(nextLowestSurf), Tiles.decodeData(nextLowestSurf));
                     }
                  }
               }
            } else {
               tile = caveMesh.getTile(x, y);
               if (Tiles.decodeType(tile) != Tiles.Tile.TILE_CAVE.id) {
                  if (Tiles.getTile(Tiles.decodeType(tile)) == null) {
                     caveMesh.setTile(
                        x,
                        y,
                        Tiles.encode((short)-100, TileRockBehaviour.prospect(x & (1 << Constants.meshSize) - 1, y >> Constants.meshSize, false), (byte)0)
                     );
                     logger.log(Level.INFO, "Mended a " + Tiles.decodeType(tile) + " cave tile at " + x + "," + y);
                  } else {
                     int cavet = caveMesh.getTile(x, y);
                     if (Tiles.decodeData(cavet) != 0) {
                        byte cceil = Tiles.decodeData(cavet);
                        int caveh = Tiles.decodeHeight(cavet);
                        int rockHeight = Tiles.decodeHeight(rockMesh.getTile(x, y));
                        if (cceil + caveh > rockHeight) {
                           ++fixedWalls;
                           int maxHeight = rockHeight - caveh;
                           caveMesh.setTile(x, y, Tiles.encode((short)caveh, Tiles.decodeType(cavet), (byte)Math.min(maxHeight, cceil)));
                        }
                     }
                  }
               } else {
                  int minheight = -100;
                  boolean fix = false;

                  for(int xx = 0; xx <= 1; ++xx) {
                     for(int yy = 0; yy <= 1; ++yy) {
                        int tt = caveMesh.getTile(x + xx, y + yy);
                        if (Tiles.decodeHeight(tt) == -100 && Tiles.decodeData(tt) == 0) {
                           fix = true;
                           if (Tiles.decodeHeight(tt) > minheight) {
                              minheight = Tiles.decodeHeight(tt);
                           }
                        }
                     }
                  }

                  if (fix) {
                     ++fixed3;

                     for(int xx = 0; xx <= 1; ++xx) {
                        for(int yy = 0; yy <= 1; ++yy) {
                           int tt = caveMesh.getTile(x + xx, y + yy);
                           int rocktile = rockMesh.getTile(x + xx, y + yy);
                           int rockHeight = Tiles.decodeHeight(rocktile);
                           int maxHeight = rockHeight - minheight;
                           if (Tiles.decodeHeight(tt) == -100 && Tiles.decodeData(tt) == 0) {
                              caveMesh.setTile(x + xx, y + yy, Tiles.encode((short)minheight, Tiles.decodeType(tt), (byte)Math.min(maxHeight, 5)));
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      try {
         surfaceMesh.saveAll();
         logger.log(Level.INFO, "Set " + fixed + " cave entrances to rock out of " + found);
      } catch (IOException var36) {
         logger.log(Level.WARNING, "Failed to save surfaceMesh", (Throwable)var36);
      }

      if (fixed2 > 0 || fixed3 > 0 || fixedWalls > 0 || fixed4 > 0 || fixed5 > 0) {
         try {
            caveMesh.saveAll();
            logger.log(
               Level.INFO,
               "Fixed "
                  + fixed2
                  + " crazy cave entrances and "
                  + fixed3
                  + " weird caves as well. Also fixed "
                  + fixedWalls
                  + " walls sticking up. Also fixed "
                  + fixed4
                  + " unleavable exit nodes. Fixed "
                  + fixed5
                  + " misaligned surface tile nodes."
            );
         } catch (IOException var35) {
            logger.log(Level.WARNING, "Failed to save surfaceMesh", (Throwable)var35);
         }
      }

      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Fixing cave entrances took " + lElapsedTime + " ms");
   }

   private void checkShutDown() {
      int secondsToShutDown = (int)(millisToShutDown / 1000L);
      if (secondsToShutDown == 2400) {
         if (lastSentWarning != 2400) {
            lastSentWarning = 2400;
            this.broadCastAlert("40 minutes to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 1200) {
         if (lastSentWarning != 1200) {
            lastSentWarning = 1200;
            this.broadCastAlert("20 minutes to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 600) {
         if (lastSentWarning != 600) {
            lastSentWarning = 600;
            this.broadCastAlert("10 minutes to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 300) {
         if (lastSentWarning != 300) {
            lastSentWarning = 300;
            this.broadCastAlert("5 minutes to shutdown. ", true, (byte)1);
            this.broadCastAlert(shutdownReason, true, (byte)0);
            Players.getInstance().setChallengeStep(2);
         }
      } else if (secondsToShutDown == 180) {
         if (lastSentWarning != 180) {
            lastSentWarning = 180;
            this.broadCastAlert("3 minutes to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
            Players.getInstance().setChallengeStep(3);
            Players.getInstance().setChallengeStep(4);
         }
      } else if (secondsToShutDown == 60) {
         if (lastSentWarning != 60) {
            lastSentWarning = 60;
            this.broadCastAlert("1 minute to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 30) {
         if (lastSentWarning != 30) {
            lastSentWarning = 30;
            this.broadCastAlert("30 seconds to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 20) {
         if (lastSentWarning != 20) {
            lastSentWarning = 20;
            this.broadCastAlert("20 seconds to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 10) {
         if (lastSentWarning != 10) {
            lastSentWarning = 10;
            FocusZone hotaZone = FocusZone.getHotaZone();
            if (hotaZone != null) {
               Hota.forcePillarsToWorld();
            }

            this.broadCastAlert("10 seconds to shutdown. ", false, (byte)1);
            this.broadCastAlert(shutdownReason, false, (byte)0);
         }
      } else if (secondsToShutDown == 3 && lastSentWarning != 1) {
         lastSentWarning = 1;
         this.broadCastAlert("Server shutting down NOW!/%7?o#### NO CARRIER", false);
         Players.getInstance().sendLogoff("The server shut down: " + shutdownReason);
         this.twitLocalServer("The server shut down: " + shutdownReason);
      }

      if (secondsToShutDown < 120) {
         Constants.maintaining = true;
      }
   }

   @Override
   public void run() {
      long now = 0L;
      long check = 0L;

      try {
         now = System.currentTimeMillis();
         if (Constants.isGameServer) {
            TilePoller.pollNext();
         }

         if (!Servers.localServer.testServer && System.currentTimeMillis() - now > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at tilepoller.pollnext (0.1): " + (float)(System.currentTimeMillis() - now) / 1000.0F + " seconds");
         }

         check = System.currentTimeMillis();
         Zones.pollNextZones(25L);
         if (Features.Feature.CROP_POLLER.isEnabled()) {
            CropTilePoller.pollCropTiles();
         }

         Players.getInstance().pollPlayers();
         Delivery.poll();
         if (!Servers.localServer.testServer && System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at Zones.pollnextzones (0.5): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
         }

         if (millisToShutDown > -1000L) {
            if (millisToShutDown < 0L) {
               this.shutDown();
            } else {
               this.checkShutDown();
               millisToShutDown -= 25L;
            }
         }

         if (counter == 2) {
            VoteQuestions.handleVoting();
            VoteQuestions.handleArchiveTickets();
            if (Features.Feature.HIGHWAYS.isEnabled()) {
               Routes.handlePathsToSend();
            }
         }

         if (counter == 3) {
            PlayerInfoFactory.handlePlayerStateList();
            Tickets.handleArchiveTickets();
            Tickets.handleTicketsToSend();
         }

         if (++counter == 5) {
            if (Constants.useScheduledExecutorToTickCalendar) {
               if (logger.isLoggable(Level.FINEST)) {
               }
            } else {
               WurmCalendar.tickSecond();
            }

            ServerProjectile.pollAll();
            if (now - this.lastLogged > 300000L) {
               this.lastLogged = now;
               if (Constants.useScheduledExecutorToWriteLogs && logger.isLoggable(Level.FINER)) {
                  logger.finer("Using a ScheduledExecutorService to write logs so do not call writePlayerLog() from main Server thread");
               }

               if (Constants.isGameServer && System.currentTimeMillis() - Servers.localServer.getFatigueSwitch() > 86400000L) {
                  if (Constants.useScheduledExecutorToSwitchFatigue) {
                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer(
                           "Using a ScheduledExecutorService to switch fatigue so do not call PlayerInfoFactory.switchFatigue() from main Server thread"
                        );
                     }
                  } else {
                     PlayerInfoFactory.switchFatigue();
                  }

                  Offspring.resetOffspringCounters();
                  Servers.localServer.setFatigueSwitch(System.currentTimeMillis());
               }

               King.pollKings();
               Players.getInstance().checkElectors();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at 1: " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (Constants.isGameServer && now - lastArrow > 100L) {
               Arrows.pollAll((float)(now - lastArrow));
               lastArrow = now;
            }

            boolean startHota = Servers.localServer.getNextHota() > 0L && System.currentTimeMillis() > Servers.localServer.getNextHota();
            if (startHota) {
               Hota.poll();
            }

            if (now - lastMailCheck > 364000L) {
               WurmMail.poll();
               lastMailCheck = now;
            }

            if (now - lastPolledRubble > 60000L) {
               lastPolledRubble = System.currentTimeMillis();

               for(Fence fence : Fence.getRubbleFences()) {
                  fence.poll(now);
               }

               for(Wall wall : Wall.getRubbleWalls()) {
                  wall.poll(now, null, null);
               }

               if (ChallengeServer
                  && Servers.localServer.getChallengeEnds() > 0L
                  && System.currentTimeMillis() > Servers.localServer.getChallengeEnds()
                  && millisToShutDown < 0L) {
                  for(Village v : Villages.getVillages()) {
                     v.disband("System");
                  }

                  this.startShutdown(600, "The world is ending.");
                  Players.getInstance().setChallengeStep(1);
               }

               if (tempEffects.size() > 0) {
                  HashSet<Long> toRemove = new HashSet<>();

                  for(Entry<Long, Long> entry : tempEffects.entrySet()) {
                     if (System.currentTimeMillis() > entry.getValue()) {
                        toRemove.add(entry.getKey());
                     }
                  }

                  for(Long val : toRemove) {
                     tempEffects.remove(val);
                     Players.getInstance().removeGlobalEffect(val);
                  }
               }
            }

            if (now - lastPolledWater > 1000L) {
               this.pollSurfaceWater();
               lastPolledWater = System.currentTimeMillis();
            }

            if (now - lastWeather > 70000L) {
               check = System.currentTimeMillis();
               lastWeather = now;
               boolean setw = true;
               if (weather.tick() && Servers.localServer.LOGINSERVER) {
                  startSendWeatherThread();
                  setw = false;
               }

               if (setw) {
                  Players.getInstance().setShouldSendWeather(true);
               }

               this.thunderMode = weather.getRain() > 0.5F && weather.getCloudiness() > 0.5F;
               if (WurmCalendar.isChristmas()) {
                  Zones.loadChristmas();
               } else if (WurmCalendar.wasTestChristmas) {
                  WurmCalendar.wasTestChristmas = false;
                  Zones.deleteChristmas();
               } else if (WurmCalendar.isAfterChristmas()) {
                  Zones.deleteChristmas();
               }

               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Weather (2): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }

               if (!startHota) {
                  Hota.poll();
               }
            }

            if (Constants.isGameServer && this.thunderMode && now - this.lastFlash > 5000L) {
               this.lastFlash = now;
               if (weather.getRain() - 0.5F + (weather.getCloudiness() - 0.5F) > rand.nextFloat()) {
                  Zones.flash();
               }
            }

            if (Constants.isGameServer && now - lastSecond > 60000L) {
               check = System.currentTimeMillis();
               lastSecond = now;
               if (Constants.useScheduledExecutorToSaveDirtyMeshRows) {
                  if (logger.isLoggable(Level.FINER)) {
                     logger.finer("useScheduledExecutorToSaveDirtyMeshRows is true so do not save the meshes from Server.run()");
                  }
               } else {
                  caveMesh.saveNextDirtyRow();
                  surfaceMesh.saveNextDirtyRow();
                  rockMesh.saveNextDirtyRow();
                  resourceMesh.saveNextDirtyRow();
                  flagsMesh.saveNextDirtyRow();
               }

               MountTransfer.pruneTransfers();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Meshes.saveNextDirtyRow (4): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (Constants.isGameServer && now - lastPolledSkills > 21600000L) {
               check = System.currentTimeMillis();
               if (!Features.Feature.SKILLSTAT_DISABLE.isEnabled()) {
                  SkillStat.pollSkills();
               }

               lastPolledSkills = System.currentTimeMillis();
               EndGameItems.pollAll();
               Trap.checkUpdate();
               Items.pollUnstableRifts();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at pollskills (4.5): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }

               if (System.currentTimeMillis() - Servers.localServer.getLastSpawnedUnique() > 1209600000L) {
                  Dens.checkDens(true);
               }
            }

            if (Constants.isGameServer && now - lastResetTiles > 14400000L) {
               Zones.saveProtectedTiles();
               lastResetTiles = System.currentTimeMillis();
            }

            if (Servers.localServer.LOGINSERVER && System.currentTimeMillis() > Servers.localServer.getNextEpicPoll()) {
               epicMap.pollAllEntities(false);
               Servers.localServer.setNextEpicPoll(System.currentTimeMillis() + 1200000L);
            }

            ValreiMapData.pollValreiData();
            SpellResist.onServerPoll();
            if (now - lastRecruitmentPoll > 86400000L) {
               lastRecruitmentPoll = System.currentTimeMillis();
               RecruitmentAds.poll();
            }

            if (now - lastAwardedItems > 2000L) {
               ValreiMapData.pollValreiData();
               pollPendingAwards();
               AwardLadder.clearItemAwards();
               lastAwardedItems = System.currentTimeMillis();
            }

            if (now - lastFaith > 3600000L) {
               check = System.currentTimeMillis();
               lastFaith = System.currentTimeMillis();
               if (Constants.isGameServer) {
                  Deities.calculateFaiths();
                  if (now - this.lastClearedFaithGain > 86400000L) {
                     Players.resetFaithGain();
                     this.lastClearedFaithGain = now;
                  }

                  Creatures.getInstance().pollOfflineCreatures();
               }

               if (!Servers.isThisLoginServer()) {
                  if (Constants.useScheduledExecutorToSendTimeSync) {
                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer("useScheduledExecutorToSendTimeSync is true so do not send TimeSync from Server.run()");
                     }
                  } else {
                     TimeSync synch = new TimeSync();
                     this.addIntraCommand(synch);
                  }
               } else {
                  ErrorChecks.checkItemWatchers();
               }

               if (rand.nextInt(3) == 0) {
                  PendingAccount.poll();
               }

               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at 5: " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (Constants.isGameServer && now - lastPolledBanks > 3601000L) {
               check = System.currentTimeMillis();
               if (Constants.useScheduledExecutorToCountEggs) {
                  if (logger.isLoggable(Level.FINER)) {
                     logger.finer("useScheduledExecutorToCountEggs is true so do not call Items.countEggs() from Server.run()");
                  }
               } else {
                  Items.countEggs();
               }

               lastPolledBanks = now;
               Banks.poll(now);
               Players.getInstance().checkAffinities();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Banks and Eggs (6): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (Constants.isGameServer && WurmCalendar.currentTime % 4000L == 0L) {
               check = System.currentTimeMillis();
               Players.getInstance().calcCRBonus();
               Villages.poll();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Villages.poll (7): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }

               check = System.currentTimeMillis();
               Kingdoms.poll();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Kingdoms.poll (7.1): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }

               check = System.currentTimeMillis();
               Questions.trimQuestions();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(
                     Level.INFO, "Lag detected at Questions.trimQuestions (7.2): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds"
                  );
               }
            }

            if (WurmCalendar.currentTime % 100L == 0L) {
               check = System.currentTimeMillis();
               Skills.switchSkills(check);
               Battles.poll(false);
               Servers.localServer.saveTimers();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at Battles and Constants (9): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            } else if (WurmCalendar.currentTime % 1050L == 0L) {
               Players.getInstance().pollChamps();
               Effectuator.pollEpicEffects();
            }

            if (now - lastDeletedPlayer > 3000L) {
               PlayerInfoFactory.checkIfDeleteOnePlayer();
               lastDeletedPlayer = System.currentTimeMillis();
            }

            if (now - lastLoweredRanks > 600000L) {
               PlayerInfoFactory.pruneRanks(now);
               EpicServerStatus.pollExpiredMissions();
               lastLoweredRanks = System.currentTimeMillis();
            }

            if (now > this.nextTerraformPoll) {
               this.pollTerraformingTasks();
               this.nextTerraformPoll = System.currentTimeMillis() + 1000L;
            }

            if (Servers.localServer.EPIC && !Servers.localServer.HOMESERVER && now > lastPolledSupplyDepots + 60000L) {
               for(Item depot : Items.getSupplyDepots()) {
                  depot.checkItemSpawn();
               }

               lastPolledSupplyDepots = now;
            }

            if (Servers.localServer.isChallengeServer()) {
               if (now - lastAwardedBattleCamps > 600000L) {
                  for(Item i : Items.getWarTargets()) {
                     Kingdom k = Kingdoms.getKingdom(i.getKingdom());
                     if (k != null) {
                        k.addWinpoints(1);
                     }

                     for(PlayerInfo pinf : PlayerInfoFactory.getPlayerInfos()) {
                        if (System.currentTimeMillis() - pinf.lastLogin < 86400000L
                           && Players.getInstance().getKingdomForPlayer(pinf.wurmId) == i.getKingdom()) {
                           ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 1.0F);
                        }
                     }
                  }

                  lastAwardedBattleCamps = System.currentTimeMillis();
               }

               if (now > lastPolledSupplyDepots + 60000L) {
                  for(Item depot : Items.getSupplyDepots()) {
                     depot.checkItemSpawn();
                  }

                  lastPolledSupplyDepots = now;
               }

               if (now - savedChallengePage > 10000L) {
                  ChallengeSummary.saveCurrentGlobalHtmlPage();
                  savedChallengePage = System.currentTimeMillis();
               }
            }

            if (now - lastPinged > 1000L) {
               Trap.checkQuickUpdate();
               Players.getInstance().checkSendWeather();
               check = System.currentTimeMillis();
               if (lostConnections > 20 && lostConnections > Players.getInstance().numberOfPlayers() / 2) {
                  logger.log(Level.INFO, "Trying to forcibly log off linkless players: " + lostConnections);
                  Players.getInstance().logOffLinklessPlayers();
               }

               lostConnections = 0;
               this.checkAlertMessages();
               lastPinged = now;
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at checkAlertMessages (10): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (Constants.isGameServer && now - lastPolledShopCultist > 86400000L) {
               lastPolledShopCultist = System.currentTimeMillis();
               Cultist.resetSkillGain();
               logger.log(Level.INFO, "Polling shop demands");
               check = System.currentTimeMillis();
               this.pollShopDemands();
               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at pollShopDemands (11): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            if (System.currentTimeMillis() - lastPolledTileEffects > 3000L) {
               AreaSpellEffect.pollEffects();
               lastPolledTileEffects = System.currentTimeMillis();
               Players.printStats();
            }

            if (System.currentTimeMillis() - lastResetAspirations > 90000000L) {
               Methods.resetAspirants();
               lastResetAspirations = System.currentTimeMillis();
            }

            if (this.playersAtLogin.size() > 0) {
               check = System.currentTimeMillis();
               Iterator<Long> it = this.playersAtLogin.listIterator();

               while(it.hasNext()) {
                  long pid = it.next();

                  try {
                     Creature player = Players.getInstance().getPlayer(pid);
                     if (player.getVisionArea() == null) {
                        logger.log(Level.INFO, "VisionArea null for " + player.getName() + ", creating one.");
                        player.createVisionArea();
                     }

                     VisionArea area = player.getVisionArea();
                     if (area != null && area.isInitialized()) {
                        it.remove();
                     } else {
                        try {
                           if (area != null && !player.isDead()) {
                              area.sendNextStrip();
                           } else if (area == null && !player.isDead() && !player.isTeleporting()) {
                              logger.log(Level.WARNING, "VisionArea is null for player " + player.getName() + ". Removing from login.");
                              it.remove();
                           }
                        } catch (Exception var23) {
                           logger.log(Level.INFO, var23.getMessage(), (Throwable)var23);
                           it.remove();
                        }
                     }
                  } catch (NoSuchPlayerException var24) {
                     logger.log(Level.INFO, var24.getMessage(), (Throwable)var24);
                     it.remove();
                  }
               }

               if (System.currentTimeMillis() - check > Constants.lagThreshold) {
                  logger.log(Level.INFO, "Lag detected at VisionArea (12): " + (float)(System.currentTimeMillis() - check) / 1000.0F + " seconds");
               }
            }

            check = System.currentTimeMillis();
            this.removeCreatures();
            if (System.currentTimeMillis() - check > Constants.lagThreshold) {
               logger.log(Level.INFO, "Lag detected at removeCreatures (13.5): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
            }

            counter = 0;
            this.pollWebCommands();
         }

         check = System.currentTimeMillis();
         MessageServer.sendMessages();
         if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at sendMessages (14): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
         }

         check = System.currentTimeMillis();
         this.sendFinals();
         if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at sendFinals (15): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
         }

         check = System.currentTimeMillis();
         this.socketServer.tick();
         int realTicks = (int)(now - startTime) / 25;
         totalTicks = realTicks - totalTicks;
         if (--commPollCounter <= 0) {
            this.pollComms(now);
            commPollCounter = commPollCounterInit;
         }

         totalTicks = realTicks;
         if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at socketserver.tick (15.5): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
            logger.log(
               Level.INFO,
               "Numcommands="
                  + Communicator.getNumcommands()
                  + ", last="
                  + Communicator.getLastcommand()
                  + ", prev="
                  + Communicator.getPrevcommand()
                  + " target="
                  + Communicator.getCommandAction()
                  + ", Message="
                  + Communicator.getCommandMessage()
            );
            logger.log(
               Level.INFO,
               "Size of connections="
                  + this.socketServer.getNumberOfConnections()
                  + " logins="
                  + LoginHandler.logins
                  + ", redirs="
                  + LoginHandler.redirects
                  + " exceptions="
                  + exceptions
            );
         }

         LoginHandler.logins = 0;
         LoginHandler.redirects = 0;
         exceptions = 0;
         check = System.currentTimeMillis();
         this.pollIntraCommands();
         if (System.currentTimeMillis() - check > Constants.lagThreshold) {
            logger.log(Level.INFO, "Lag detected at pollintracommands (15.8): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
         }

         try {
            check = System.currentTimeMillis();
            this.intraServer.socketServer.tick();
            if (System.currentTimeMillis() - check > Constants.lagThreshold) {
               logger.log(Level.INFO, "Lag detected at intraServer.tick (16): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
            }
         } catch (IOException var22) {
            logger.log(Level.INFO, "Failed to update intraserver.", (Throwable)var22);
         }

         long runLoopTime = System.currentTimeMillis() - now;
         if (runLoopTime > 1000L) {
            secondsLag = (int)((long)secondsLag + runLoopTime / 1000L);
            logger.info("Elapsed time (" + runLoopTime + "ms) for this loop was more than 1 second so adding it to the lag count, which is now: " + secondsLag);
         }
      } catch (IOException var25) {
         logger.log(Level.INFO, "Failed to update updserver", (Throwable)var25);
      } catch (Throwable var26) {
         logger.log(Level.SEVERE, var26.getMessage(), var26);
         if (var26.getMessage() == null && var26.getCause() == null) {
            logger.log(
               Level.SEVERE, "Server is shutting down but there is no information in the Exception so creating a new one", (Throwable)(new Exception())
            );
         }

         this.shutDown();
      } finally {
         if (logger.isLoggable(Level.FINEST)) {
         }
      }

      this.steamHandler.update();
   }

   private final void pollComms(long now) {
      long check = System.currentTimeMillis();
      Map<String, Player> playerMap = Players.getInstance().getPlayerMap();

      for(Entry<String, Player> mapEntry : playerMap.entrySet()) {
         if (mapEntry.getValue().getCommunicator() != null) {
            for(int xm = 0;
               xm < 10 && mapEntry.getValue().getCommunicator().getMoves() > 0 && mapEntry.getValue().getCommunicator().getAvailableMoves() > 0;
               ++xm
            ) {
               if (mapEntry.getValue().getCommunicator().pollNextMove()) {
                  mapEntry.getValue().getCommunicator().setAvailableMoves(mapEntry.getValue().getCommunicator().getAvailableMoves() - 1);
               }
            }

            if (mapEntry.getValue().moveWarned
               || mapEntry.getValue().getCommunicator().getMoves() <= 240 && mapEntry.getValue().getCommunicator().getMoves() >= -240) {
               if (mapEntry.getValue().moveWarned
                  && mapEntry.getValue().getCommunicator().getMoves() > -24
                  && mapEntry.getValue().getCommunicator().getMoves() < 24) {
                  mapEntry.getValue().getCommunicator().sendSafeServerMessage("Your position on the server is now updated.");
                  long seconds = (System.currentTimeMillis() - mapEntry.getValue().moveWarnedTime) / 1000L;
                  logger.log(
                     Level.INFO,
                     mapEntry.getValue().getName()
                        + " moves down to "
                        + mapEntry.getValue().getCommunicator().getMoves()
                        + ". Was lagging "
                        + seconds
                        + " seconds with a peak of "
                        + mapEntry.getValue().peakMoves
                        + " moves."
                  );
                  mapEntry.getValue().moveWarned = false;
                  mapEntry.getValue().peakMoves = 0L;
                  mapEntry.getValue().moveWarnedTime = 0L;
               } else if (mapEntry.getValue().moveWarned
                  && (mapEntry.getValue().getCommunicator().getMoves() > 1440 || mapEntry.getValue().getCommunicator().getMoves() < -1440)) {
                  mapEntry.getValue().getCommunicator().sendAlertServerMessage("You are out of synch with the server. Please stand still.");
               }
            } else {
               if (mapEntry.getValue().getPower() >= 5) {
                  mapEntry.getValue().getCommunicator().sendAlertServerMessage("Moves at " + mapEntry.getValue().getCommunicator().getMoves());
               } else {
                  mapEntry.getValue().getCommunicator().sendAlertServerMessage("Your position on the server is not updated. Please move slower.");
               }

               mapEntry.getValue().moveWarned = true;
               mapEntry.getValue().moveWarnedTime = System.currentTimeMillis();
            }

            if (mapEntry.getValue().getCommunicator().getMoves() > 240) {
               if (mapEntry.getValue().peakMoves < (long)mapEntry.getValue().getCommunicator().getMoves()) {
                  mapEntry.getValue().peakMoves = (long)mapEntry.getValue().getCommunicator().getMoves();
               }
            } else if (mapEntry.getValue().getCommunicator().getMoves() < -240
               && mapEntry.getValue().peakMoves > (long)mapEntry.getValue().getCommunicator().getMoves()) {
               mapEntry.getValue().peakMoves = (long)mapEntry.getValue().getCommunicator().getMoves();
            }
         }
      }

      long time = System.currentTimeMillis() - this.lastTicked;
      if (time <= 3L) {
         ++lagticks;
      }

      this.lastTicked = System.currentTimeMillis();
      if (System.currentTimeMillis() - check > Constants.lagThreshold) {
         logger.log(Level.INFO, "Lag detected at Player Moves (13): " + (float)(System.currentTimeMillis() - check) / 1000.0F);
      }
   }

   private final void pollSurfaceWater() {
      if (this.waterThread != null) {
         this.waterThread.propagateChanges();
      }
   }

   public void pollShopDemands() {
      Shop[] shops = Economy.getEconomy().getShops();

      for(Shop lShop : shops) {
         lShop.getLocalSupplyDemand().lowerDemands();
      }

      LocalSupplyDemand.increaseAllDemands();
      Economy.getEconomy().pollTraderEarnings();
   }

   public static void addNewPlayer(String name) {
      if (System.currentTimeMillis() - lastResetNewPremiums > 10800000L) {
         newPremiums = 0;
         lastResetNewPremiums = System.currentTimeMillis();
      }

      ++newPremiums;
   }

   public static final void addNewbie() {
      ++newbies;
   }

   public static final void addExpiry() {
      ++expiredPremiums;
   }

   private void sendFinals() {
      if (FINAL_LOGINS_RW_LOCK.writeLock().tryLock()) {
         try {
            ListIterator<Long> it = finalLogins.listIterator();

            while(it.hasNext()) {
               try {
                  long pid = it.next();
                  Player player = Players.getInstance().getPlayer(pid);
                  int step = player.getLoginStep();
                  if (player.isNew()) {
                     if (player.hasLink()) {
                        int result = LoginHandler.createPlayer(player, step);
                        if (result == Integer.MAX_VALUE) {
                           it.remove();
                           if (!this.isPlayerReceivingTiles(player)) {
                              this.playersAtLogin.add(new Long(player.getWurmId()));
                           }

                           player.setLoginHandler(null);
                        } else if (result >= 0) {
                           player.setLoginStep(++result);
                        } else {
                           player.setLoginHandler(null);
                           it.remove();
                        }
                     } else {
                        player.setLoginHandler(null);
                        it.remove();
                     }
                  } else if (player.hasLink()) {
                     LoginHandler handler = player.getLoginhandler();
                     if (handler != null) {
                        int result = handler.loadPlayer(player, step);
                        if (result == Integer.MAX_VALUE) {
                           it.remove();
                           if (!this.isPlayerReceivingTiles(player)) {
                              this.playersAtLogin.add(new Long(player.getWurmId()));
                           }

                           player.setLoginHandler(null);
                        } else if (result >= 0) {
                           player.setLoginStep(++result);
                        } else {
                           player.setLoginHandler(null);
                           it.remove();
                        }
                     } else {
                        it.remove();
                     }
                  } else {
                     player.setLoginHandler(null);
                     it.remove();
                  }

                  player.getStatus().setMoving(false);
                  if (!player.hasLink()) {
                     Players.getInstance().logoutPlayer(player);
                  }
               } catch (NoSuchPlayerException var11) {
                  logger.log(Level.INFO, var11.getMessage(), (Throwable)var11);
                  it.remove();
               }
            }
         } finally {
            FINAL_LOGINS_RW_LOCK.writeLock().unlock();
         }
      }
   }

   public void addCreatureToPort(Creature creature) {
      if (creature.isPlayer()) {
         PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();

         try {
            if (!this.playersAtLogin.contains(new Long(creature.getWurmId()))) {
               this.playersAtLogin.add(new Long(creature.getWurmId()));
            }
         } finally {
            PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
         }
      }
   }

   @Override
   public void clientConnected(SocketConnection serverConnection) {
      HackerIp ip = LoginHandler.failedIps.get(serverConnection.getIp());
      if (ip != null && System.currentTimeMillis() <= ip.mayTryAgain) {
         logger.log(
            Level.INFO,
            ip.name + " Because of the repeated failures the conn may try again in " + getTimeFor(ip.mayTryAgain - System.currentTimeMillis()) + '.'
         );
         serverConnection.disconnect();
      } else {
         try {
            LoginHandler login = new LoginHandler(serverConnection);
            serverConnection.setConnectionListener(login);
         } catch (Exception var4) {
            logger.log(Level.SEVERE, "Failed to create login handler for serverConnection: " + serverConnection + '.', (Throwable)var4);
         }
      }
   }

   public void addToPlayersAtLogin(Player player) {
      if (WurmId.getType(player.getWurmId()) != 0) {
         logger.log(Level.WARNING, "Adding " + player.getName() + " to playersAtLogin.", (Throwable)(new Exception()));
      }

      if (!this.isPlayerReceivingTiles(player)) {
         PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();

         try {
            this.playersAtLogin.add(new Long(player.getWurmId()));
         } finally {
            PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
         }
      }
   }

   public void addPlayer(Player player) {
      Players.getInstance().addPlayer(player);
      if (player.isPaying()) {
         ++logonsPrem;
      }

      ++logons;
   }

   void addIp(String ip) {
      if (!ips.keySet().contains(ip)) {
         ips.put(ip, Boolean.FALSE);
         ++numips;
      } else {
         Boolean newb = ips.get(ip);
         if (!newb) {
            ips.put(ip, Boolean.FALSE);
         }
      }
   }

   private void checkAlertMessages() {
      if (timeBetweenAlertMess1 < Long.MAX_VALUE && alertMessage1.length() > 0 && lastAlertMess1 + timeBetweenAlertMess1 < System.currentTimeMillis()) {
         this.broadCastAlert(alertMessage1);
         lastAlertMess1 = System.currentTimeMillis();
      }

      if (timeBetweenAlertMess2 < Long.MAX_VALUE && alertMessage2.length() > 0 && lastAlertMess2 + timeBetweenAlertMess2 < System.currentTimeMillis()) {
         this.broadCastAlert(alertMessage2);
         lastAlertMess2 = System.currentTimeMillis();
      }

      if (timeBetweenAlertMess3 < Long.MAX_VALUE && alertMessage3.length() > 0 && lastAlertMess3 + timeBetweenAlertMess3 < System.currentTimeMillis()) {
         this.broadCastAlert(alertMessage3);
         lastAlertMess3 = System.currentTimeMillis();
      }

      if (timeBetweenAlertMess4 < Long.MAX_VALUE && alertMessage4.length() > 0 && lastAlertMess4 + timeBetweenAlertMess4 < System.currentTimeMillis()) {
         this.broadCastAlert(alertMessage4);
         lastAlertMess4 = System.currentTimeMillis();
      }
   }

   public void startSendingFinals(Player player) {
      FINAL_LOGINS_RW_LOCK.writeLock().lock();

      try {
         finalLogins.add(new Long(player.getWurmId()));
      } finally {
         FINAL_LOGINS_RW_LOCK.writeLock().unlock();
      }
   }

   private boolean isPlayerReceivingTiles(Player player) {
      PLAYERS_AT_LOGIN_RW_LOCK.readLock().lock();

      boolean var2;
      try {
         var2 = this.playersAtLogin.contains(new Long(player.getWurmId()));
      } finally {
         PLAYERS_AT_LOGIN_RW_LOCK.readLock().unlock();
      }

      return var2;
   }

   @Override
   public void clientException(SocketConnection conn, Exception ex) {
      ++exceptions;

      try {
         Player player = Players.getInstance().getPlayer(conn);
         ++lostConnections;
         if (this.playersAtLogin != null) {
            PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();

            try {
               this.playersAtLogin.remove(new Long(player.getWurmId()));
            } finally {
               PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
            }
         }

         if (finalLogins != null) {
            FINAL_LOGINS_RW_LOCK.writeLock().lock();

            try {
               finalLogins.remove(new Long(player.getWurmId()));
            } finally {
               FINAL_LOGINS_RW_LOCK.writeLock().unlock();
            }
         }

         player.setLink(false);
      } catch (Exception var32) {
         Player player = Players.getInstance().logout(conn);
         if (player != null) {
            if (this.playersAtLogin != null) {
               PLAYERS_AT_LOGIN_RW_LOCK.writeLock().lock();

               try {
                  this.playersAtLogin.remove(new Long(player.getWurmId()));
               } finally {
                  PLAYERS_AT_LOGIN_RW_LOCK.writeLock().unlock();
               }
            }

            if (finalLogins != null) {
               FINAL_LOGINS_RW_LOCK.writeLock().lock();

               try {
                  finalLogins.remove(new Long(player.getWurmId()));
               } finally {
                  FINAL_LOGINS_RW_LOCK.writeLock().unlock();
               }
            }

            logger.log(Level.INFO, player.getName() + " lost link at exception 2");
         }
      }
   }

   public Creature getCreature(long creatureId) throws NoSuchPlayerException, NoSuchCreatureException {
      Creature toReturn = null;
      if (WurmId.getType(creatureId) == 1) {
         toReturn = Creatures.getInstance().getCreature(creatureId);
      } else {
         toReturn = Players.getInstance().getPlayer(creatureId);
      }

      return toReturn;
   }

   public Creature getCreatureOrNull(long creatureId) {
      return (Creature)(WurmId.getType(creatureId) == 1
         ? Creatures.getInstance().getCreatureOrNull(creatureId)
         : Players.getInstance().getPlayerOrNull(creatureId));
   }

   public void addMessage(Message message) {
      MessageServer.addMessage(message);
   }

   public void broadCastNormal(String message) {
      this.broadCastNormal(message, true);
   }

   public void broadCastNormal(String message, boolean twit) {
      MessageServer.broadCastNormal(message);
      if (twit) {
         this.twitLocalServer(message);
      }
   }

   public void broadCastSafe(String message) {
      this.broadCastSafe(message, true);
   }

   public void broadCastSafe(String message, boolean twit) {
      this.broadCastSafe(message, twit, (byte)0);
   }

   public void broadCastSafe(String message, boolean twit, byte messageType) {
      MessageServer.broadCastSafe(message, messageType);
      if (twit) {
         this.twitLocalServer(message);
      }
   }

   public void broadCastAlert(String message) {
      this.broadCastAlert(message, true);
   }

   public void broadCastAlert(String message, boolean twit) {
      this.broadCastAlert(message, twit, (byte)0);
   }

   public void broadCastAlert(String message, boolean twit, byte messageType) {
      MessageServer.broadCastAlert(message, messageType);
      if (twit) {
         this.twitLocalServer(message);
      }
   }

   public void broadCastAction(String message, Creature performer, int tileDist, boolean combat) {
      MessageServer.broadCastAction(message, performer, null, tileDist, combat);
   }

   public void broadCastAction(String message, Creature performer, int tileDist) {
      MessageServer.broadCastAction(message, performer, tileDist);
   }

   public void broadCastAction(String message, Creature performer, Creature receiver, int tileDist) {
      MessageServer.broadCastAction(message, performer, receiver, tileDist);
   }

   public void broadCastAction(String message, Creature performer, Creature receiver, int tileDist, boolean combat) {
      MessageServer.broadCastAction(message, performer, receiver, tileDist, combat);
   }

   public void broadCastMessage(String message, int tilex, int tiley, boolean surfaced, int tiledistance) {
      MessageServer.broadCastMessage(message, tilex, tiley, surfaced, tiledistance);
   }

   private void loadCaveMesh() {
      long start = System.nanoTime();

      try {
         caveMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "map_cave.map");
      } catch (IOException var17) {
         logger.log(Level.SEVERE, "Cavemap doesn't exist... initializing... size will be " + (1 << Constants.meshSize) + "!");

         try {
            Constants.caveImg = true;
            int msize = (1 << Constants.meshSize) * (1 << Constants.meshSize);
            int[] caveArr = new int[msize];

            for(int x = 0; x < msize; ++x) {
               if (x % 100000 == 0) {
                  logger.log(Level.INFO, "Created " + x + " tiles out of " + msize);
               }

               caveArr[x] = Tiles.encode((short)-100, TileRockBehaviour.prospect(x & (1 << Constants.meshSize) - 1, x >> Constants.meshSize, false), (byte)0);
            }

            caveMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "map_cave.map", Constants.meshSize, caveArr);
         } catch (IOException var14) {
            logger.log(Level.INFO, "Failed to initialize caves. Exiting. " + var14.getMessage(), (Throwable)var14);
            System.exit(0);
         } catch (ArrayIndexOutOfBoundsException var15) {
            logger.log(Level.WARNING, "Failed to initialize caves. Exiting. " + var15.getMessage(), (Throwable)var15);
            System.exit(0);
         } catch (Exception var16) {
            logger.log(Level.WARNING, "Failed to initialize caves. Exiting. " + var16.getMessage(), (Throwable)var16);
            System.exit(0);
         }
      } finally {
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loading cave mesh, size: " + caveMesh.getSize() + " took " + lElapsedTime + " ms");
      }

      if (Constants.reprospect) {
         TileRockBehaviour.reProspect();
      }

      if (Constants.caveImg) {
         ZonesUtility.saveAsImg(caveMesh);
         logger.log(Level.INFO, "Saved cave mesh as img");
      }
   }

   private void loadWorldMesh() {
      long start = System.nanoTime();

      try {
         surfaceMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "top_layer.map");
      } catch (IOException var8) {
         logger.log(Level.SEVERE, "Worldmap " + ServerDirInfo.getFileDBPath() + "top_layer.map doesn't exist.. Shutting down..", (Throwable)var8);
         System.exit(0);
      } finally {
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loading world mesh, size: " + surfaceMesh.getSize() + " took " + lElapsedTime + " ms");
      }
   }

   private void loadRockMesh() {
      long start = System.nanoTime();

      try {
         rockMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "rock_layer.map");
      } catch (IOException var8) {
         logger.log(Level.SEVERE, "Worldmap " + ServerDirInfo.getFileDBPath() + "rock_layer.map doesn't exist.. Shutting down..", (Throwable)var8);
         System.exit(0);
      } finally {
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loading rock mesh, size: " + rockMesh.getSize() + " took " + lElapsedTime + " ms");
      }
   }

   public static int getCaveResource(int tilex, int tiley) {
      int value = resourceMesh.getTile(tilex, tiley);
      return value >> 16 & 65535;
   }

   public static void setCaveResource(int tilex, int tiley, int newValue) {
      int value = resourceMesh.getTile(tilex, tiley);
      if ((value >> 16 & 65535) != newValue) {
         resourceMesh.setTile(tilex, tiley, ((newValue & 65535) << 16) + (value & 65535));
      }
   }

   public static int getWorldResource(int tilex, int tiley) {
      int value = resourceMesh.getTile(tilex, tiley);
      return value & 65535;
   }

   public static void setWorldResource(int tilex, int tiley, int newValue) {
      int value = resourceMesh.getTile(tilex, tiley);
      if ((value & 65535) != newValue) {
         resourceMesh.setTile(tilex, tiley, (value & -65536) + (newValue & 65535));
      }
   }

   public static int getDigCount(int tilex, int tiley) {
      int value = resourceMesh.getTile(tilex, tiley);
      return value & 0xFF;
   }

   public static void setDigCount(int tilex, int tiley, int newValue) {
      int value = resourceMesh.getTile(tilex, tiley);
      if ((value & 0xFF) != newValue) {
         resourceMesh.setTile(tilex, tiley, (value & -256) + (newValue & 0xFF));
      }
   }

   public static int getPotionQLCount(int tilex, int tiley) {
      int value = resourceMesh.getTile(tilex, tiley);
      int pQLCount = (value & 0xFF00) >> 8;
      return pQLCount == 255 ? 0 : pQLCount;
   }

   public static void setPotionQLCount(int tilex, int tiley, int newValue) {
      int pQLCount = newValue << 8;
      int value = resourceMesh.getTile(tilex, tiley);
      if ((value & 0xFF00) != pQLCount) {
         resourceMesh.setTile(tilex, tiley, (value & -65281) + (pQLCount & 0xFF00));
      }
   }

   public static boolean isBotanizable(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 128) == 128;
   }

   public static void setBotanizable(int tilex, int tiley, boolean isBotanizable) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isBotanizable ? 128 : 0;
      if ((value & 128) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -129 | newValue);
      }
   }

   public static boolean isForagable(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 64) == 64;
   }

   public static void setForagable(int tilex, int tiley, boolean isForagable) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isForagable ? 64 : 0;
      if ((value & 64) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -65 | newValue);
      }
   }

   public static boolean isGatherable(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 32) == 32;
   }

   public static void setGatherable(int tilex, int tiley, boolean isGather) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isGather ? 32 : 0;
      if ((value & 32) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -33 | newValue);
      }
   }

   public static boolean isInvestigatable(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 16) == 16;
   }

   public static void setInvestigatable(int tilex, int tiley, boolean isInvestigate) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isInvestigate ? 16 : 0;
      if ((value & 16) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -17 | newValue);
      }
   }

   public static boolean isCheckHive(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 1024) == 1024;
   }

   public static void setCheckHive(int tilex, int tiley, boolean isChecked) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isChecked ? 1024 : 0;
      if ((value & 1024) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -1025 | newValue);
      }
   }

   public static boolean wasTransformed(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 256) == 256;
   }

   public static void setTransformed(int tilex, int tiley, boolean isTransformed) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isTransformed ? 256 : 0;
      if ((value & 256) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -257 | newValue);
      }
   }

   public static boolean isBeingTransformed(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 512) == 512;
   }

   public static void setBeingTransformed(int tilex, int tiley, boolean isTransformed) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = isTransformed ? 512 : 0;
      if ((value & 512) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -513 | newValue);
      }
   }

   public static boolean hasGrubs(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (value & 2048) == 2048;
   }

   public static void setGrubs(int tilex, int tiley, boolean grubs) {
      int value = flagsMesh.getTile(tilex, tiley);
      int newValue = grubs ? 2048 : 0;
      if ((value & 2048) != newValue) {
         flagsMesh.setTile(tilex, tiley, value & -2049 | newValue);
      }
   }

   public static byte getClientSurfaceFlags(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (byte)(value & 0xFF);
   }

   public static byte getServerSurfaceFlags(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (byte)(value >>> 8 & 0xFF);
   }

   public static byte getServerCaveFlags(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (byte)(value >>> 24 & 0xFF);
   }

   public static byte getClientCaveFlags(int tilex, int tiley) {
      int value = flagsMesh.getTile(tilex, tiley);
      return (byte)(value >>> 16 & 0xFF);
   }

   public static void setServerCaveFlags(int tilex, int tiley, byte newByte) {
      int value = flagsMesh.getTile(tilex, tiley);
      flagsMesh.setTile(tilex, tiley, value & 16777215 | (newByte & 255) << 24);
   }

   public static void setClientCaveFlags(int tilex, int tiley, byte newByte) {
      int value = flagsMesh.getTile(tilex, tiley);
      flagsMesh.setTile(tilex, tiley, value & -16711681 | (newByte & 255) << 16);
   }

   public static void setSurfaceTile(@Nonnull TilePos tilePos, short newHeight, byte newTileType, byte newTileData) {
      setSurfaceTile(tilePos.x, tilePos.y, newHeight, newTileType, newTileData);
   }

   public static void setSurfaceTile(int tilex, int tiley, short newHeight, byte newTileType, byte newTileData) {
      int oldTile = surfaceMesh.getTile(tilex, tiley);
      byte oldType = Tiles.decodeType(oldTile);
      if (oldType != newTileType) {
         modifyFlagsByTileType(tilex, tiley, newTileType);
      }

      surfaceMesh.setTile(tilex, tiley, Tiles.encode(newHeight, newTileType, newTileData));
   }

   public static void modifyFlagsByTileType(int tilex, int tiley, byte newTileType) {
      Tiles.Tile theNewTile = Tiles.getTile(newTileType);
      if (!theNewTile.canBotanize()) {
         setBotanizable(tilex, tiley, false);
      }

      if (!theNewTile.canForage()) {
         setForagable(tilex, tiley, false);
      }

      setGatherable(tilex, tiley, false);
      setBeingTransformed(tilex, tiley, false);
      setTransformed(tilex, tiley, false);
   }

   public static boolean canBotanize(byte type) {
      return type == Tiles.Tile.TILE_GRASS.id
         || type == Tiles.Tile.TILE_STEPPE.id
         || type == Tiles.Tile.TILE_MARSH.id
         || type == Tiles.Tile.TILE_MOSS.id
         || type == Tiles.Tile.TILE_PEAT.id
         || Tiles.isNormalBush(type)
         || Tiles.isNormalTree(type);
   }

   public static boolean canForage(byte type) {
      return type == Tiles.Tile.TILE_GRASS.id
         || type == Tiles.Tile.TILE_STEPPE.id
         || type == Tiles.Tile.TILE_TUNDRA.id
         || type == Tiles.Tile.TILE_MARSH.id
         || Tiles.isNormalBush(type)
         || Tiles.isNormalTree(type);
   }

   public static boolean canBearFruit(byte type) {
      return Tiles.isTree(type) || Tiles.isBush(type);
   }

   public void shutDown(String aReason, Throwable aCause) {
      try {
         logger.log(Level.INFO, "Shutting down the server - reason: " + aReason);
         logger.log(Level.INFO, "Shutting down the server - cause: ", aCause);
      } finally {
         this.shutDown();
      }
   }

   public void shutDown() {
      if (ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward)) {
         UPNPService.shutdown();
      }

      Creatures.getInstance().shutDownPolltask();
      Creature.shutDownPathFinders();
      logger.log(Level.INFO, "Shutting down at: ", (Throwable)(new Exception()));
      if (this.highwayFinderThread != null) {
         logger.info("Shutting down - Stopping HighwayFinder");
         this.highwayFinderThread.shouldStop();
      }

      ServerProjectile.clear();
      logger.info("Shutting down - Polling Battles");
      if (Constants.isGameServer) {
         Battles.poll(true);
      }

      Zones.saveProtectedTiles();
      logger.info("Shutting down - Saving Players");
      Players.getInstance().savePlayersAtShutdown();
      logger.info("Shutting down - Clearing Item Database Batches");
      DbItem.clearBatches();
      logger.info("Shutting down - Saving Creatures");
      logger.info("Shutting down - Clearing Creature Database Batches");

      for(Creature c : Creatures.getInstance().getCreatures()) {
         if (c.getStatus().getPosition() != null && c.getStatus().getPosition().isChanged()) {
            try {
               c.getStatus().savePosition(c.getWurmId(), false, c.getStatus().getZoneId(), true);
            } catch (IOException var11) {
               logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
            }
         }
      }

      if (Constants.useScheduledExecutorToUpdateCreaturePositionInDatabase) {
         CreaturePos.getCreatureDbPosUpdater().saveImmediately();
      }

      CreaturePos.clearBatches();
      logger.info("Shutting down - Saving all creatures");
      Creatures.getInstance().saveCreatures();
      logger.info("Shutting down - Saving All Zones");
      Zones.saveAllZones();
      if (this.scheduledExecutorService != null && !this.scheduledExecutorService.isShutdown()) {
         this.scheduledExecutorService.shutdown();
      }

      logger.info("Shutting down - Saving Surface Mesh");

      try {
         surfaceMesh.saveAll();
         surfaceMesh.close();
      } catch (IOException var10) {
         logger.log(Level.WARNING, "Failed to save surfacemesh!", (Throwable)var10);
      }

      logger.info("Shutting down - Saving Rock Mesh");

      try {
         rockMesh.saveAll();
         rockMesh.close();
      } catch (IOException var9) {
         logger.log(Level.WARNING, "Failed to save rockmesh!", (Throwable)var9);
      }

      logger.info("Shutting down - Saving Cave Mesh");

      try {
         caveMesh.saveAll();
         caveMesh.close();
      } catch (IOException var8) {
         logger.log(Level.WARNING, "Failed to save cavemesh!", (Throwable)var8);
      }

      logger.info("Shutting down - Saving Resource Mesh");

      try {
         resourceMesh.saveAll();
         resourceMesh.close();
      } catch (IOException var7) {
         logger.log(Level.WARNING, "Failed to save resourcemesh!", (Throwable)var7);
      }

      logger.info("Shutting down - Saving Flags Mesh");

      try {
         flagsMesh.saveAll();
         flagsMesh.close();
      } catch (IOException var6) {
         logger.log(Level.WARNING, "Failed to save flagsmesh!", (Throwable)var6);
      }

      if (this.waterThread != null) {
         logger.info("Shutting down - Saving Water Mesh");
         this.waterThread.shouldStop = true;
      }

      logger.info("Shutting down - Saving Constants");
      Constants.crashed = false;
      Constants.save();
      logger.info("Shutting down - Saving WurmID Numbers");
      WurmId.updateNumbers();
      this.steamHandler.closeServer();
      logger.info("Shutting down - Closing Database Connections");
      DbConnector.closeAll();
      logger.log(Level.INFO, "The server shut down nicely. Wurmcalendar time is " + WurmCalendar.currentTime);
      System.exit(0);
   }

   private void loadResourceMesh() {
      long start = System.nanoTime();

      try {
         resourceMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "resources.map");
      } catch (IOException var12) {
         logger.log(Level.INFO, "resources doesn't exist.. creating..");
         int[] resourceArr = new int[(1 << Constants.meshSize) * (1 << Constants.meshSize)];

         for(int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); ++x) {
            resourceArr[x] = -1;
         }

         try {
            resourceMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "resources.map", Constants.meshSize, resourceArr);
         } catch (IOException var11) {
            logger.log(Level.SEVERE, "Failed to create resources. Exiting.", (Throwable)var11);
            System.exit(0);
         }
      } finally {
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loading resource mesh, size: " + resourceMesh.getSize() + " took " + lElapsedTime + " ms");
      }
   }

   private void loadFlagsMesh() {
      long start = System.nanoTime();

      try {
         flagsMesh = MeshIO.open(ServerDirInfo.getFileDBPath() + "flags.map");
         int first = flagsMesh.getTile(0, 0);
         if ((first & -256) == -256) {
            logger.log(Level.INFO, "converting flags.");

            for(int x = 0; x < 1 << Constants.meshSize; ++x) {
               for(int y = 0; y < 1 << Constants.meshSize; ++y) {
                  int value = flagsMesh.getTile(x, y) & 0xFF;
                  int serverSurfaceFlag = value & 15;
                  value |= serverSurfaceFlag << 8;
                  value &= 65520;
                  flagsMesh.setTile(x, y, value);
               }
            }
         }
      } catch (IOException var14) {
         logger.log(Level.INFO, "flags doesn't exist.. creating..");
         int[] resourceArr = new int[(1 << Constants.meshSize) * (1 << Constants.meshSize)];

         for(int x = 0; x < (1 << Constants.meshSize) * (1 << Constants.meshSize); ++x) {
            resourceArr[x] = 0;
         }

         try {
            flagsMesh = MeshIO.createMap(ServerDirInfo.getFileDBPath() + "flags.map", Constants.meshSize, resourceArr);
            this.needSeeds = true;
         } catch (IOException var13) {
            logger.log(Level.SEVERE, "Failed to create flags. Exiting.", (Throwable)var13);
            System.exit(0);
         }
      } finally {
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loading flags mesh, size: " + flagsMesh.getSize() + " took " + lElapsedTime + " ms");
      }
   }

   public static final void addPendingAward(PendingAward award) {
      pendingAwards.add(award);
   }

   private static final void pollPendingAwards() {
      for(PendingAward award : pendingAwards) {
         award.award();
      }

      pendingAwards.clear();
   }

   public static final String getTimeFor(long aTime) {
      String times = "";
      if (aTime < 60000L) {
         long secs = aTime / 1000L;
         times = times + secs + (secs == 1L ? " second" : " seconds");
      } else {
         long daysleft = aTime / 86400000L;
         long hoursleft = (aTime - daysleft * 86400000L) / 3600000L;
         long minutesleft = (aTime - daysleft * 86400000L - hoursleft * 3600000L) / 60000L;
         if (daysleft > 0L) {
            times = times + daysleft + (daysleft == 1L ? " day" : " days");
         }

         if (hoursleft > 0L) {
            String aft = "";
            if (daysleft > 0L && minutesleft > 0L) {
               times = times + ", ";
               aft = aft + " and ";
            } else if (daysleft > 0L) {
               times = times + " and ";
            } else if (minutesleft > 0L) {
               aft = aft + " and ";
            }

            times = times + hoursleft + (hoursleft == 1L ? " hour" : " hours") + aft;
         }

         if (minutesleft > 0L) {
            String aft = "";
            if (daysleft > 0L && hoursleft == 0L) {
               aft = " and ";
            }

            times = times + aft + minutesleft + (minutesleft == 1L ? " minute" : " minutes");
         }
      }

      if (times.length() == 0) {
         times = "nothing";
      }

      return times;
   }

   public void transaction(long itemId, long oldownerid, long newownerid, String reason, long value) {
      Economy.getEconomy().transaction(itemId, oldownerid, newownerid, reason, value);
   }

   private void pollIntraCommands() {
      try {
         if (INTRA_COMMANDS_RW_LOCK.writeLock().tryLock()) {
            try {
               IntraCommand[] comms = this.intraCommands.toArray(new IntraCommand[this.intraCommands.size()]);

               for(int x = 0; x < comms.length; ++x) {
                  if (x < 40 && comms[x].poll()) {
                     this.intraCommands.remove(comms[x]);
                  }
               }
            } finally {
               INTRA_COMMANDS_RW_LOCK.writeLock().unlock();
            }
         }

         MoneyTransfer[] transfers = MoneyTransfer.transfers.toArray(new MoneyTransfer[MoneyTransfer.transfers.size()]);

         for(int x = 0; x < transfers.length; ++x) {
            if (transfers[x].poll() && (transfers[x].deleted || transfers[x].pollTimes > 500)) {
               logger.log(Level.INFO, "Polling MoneyTransfer " + x + " deleted: " + transfers[x]);
               MoneyTransfer.transfers.remove(transfers[x]);
            }
         }

         TimeTransfer[] ttransfers = TimeTransfer.transfers.toArray(new TimeTransfer[TimeTransfer.transfers.size()]);

         for(TimeTransfer lTtransfer : ttransfers) {
            if (lTtransfer.poll() && lTtransfer.deleted) {
               logger.log(Level.INFO, "Polling tt deleted");
               TimeTransfer.transfers.remove(lTtransfer);
            }
         }

         PasswordTransfer[] ptransfers = PasswordTransfer.transfers.toArray(new PasswordTransfer[PasswordTransfer.transfers.size()]);

         for(PasswordTransfer lPtransfer : ptransfers) {
            if (lPtransfer.poll() && lPtransfer.deleted) {
               PasswordTransfer.transfers.remove(lPtransfer);
            }
         }
      } catch (Exception var11) {
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
      }
   }

   public void addIntraCommand(IntraCommand command) {
      INTRA_COMMANDS_RW_LOCK.writeLock().lock();

      try {
         this.intraCommands.add(command);
      } finally {
         INTRA_COMMANDS_RW_LOCK.writeLock().unlock();
      }
   }

   public void addWebCommand(WebCommand command) {
      try {
         WEBCOMMANDS_RW_LOCK.writeLock().lock();
         webcommands.add(command);
      } finally {
         WEBCOMMANDS_RW_LOCK.writeLock().unlock();
      }
   }

   private void pollWebCommands() {
      try {
         WEBCOMMANDS_RW_LOCK.writeLock().lock();

         for(WebCommand wc : webcommands) {
            wc.execute();
         }

         webcommands.clear();
      } finally {
         WEBCOMMANDS_RW_LOCK.writeLock().unlock();
      }
   }

   public void addTerraformingTask(TerraformingTask task) {
      try {
         TERRAFORMINGTASKS_RW_LOCK.writeLock().lock();
         terraformingTasks.add(task);
      } finally {
         TERRAFORMINGTASKS_RW_LOCK.writeLock().unlock();
      }
   }

   private void pollTerraformingTasks() {
      try {
         TERRAFORMINGTASKS_RW_LOCK.writeLock().lock();
         TerraformingTask[] tasks = terraformingTasks.toArray(new TerraformingTask[terraformingTasks.size()]);

         for(TerraformingTask task : tasks) {
            if (task.poll()) {
               terraformingTasks.remove(task);
            }
         }
      } finally {
         TERRAFORMINGTASKS_RW_LOCK.writeLock().unlock();
      }
   }

   @Override
   public byte[] getExternalIp() {
      return this.externalIp;
   }

   @Override
   public byte[] getInternalIp() {
      return this.internalIp;
   }

   @Override
   public int getIntraServerPort() {
      return Integer.parseInt(Servers.localServer.INTRASERVERPORT);
   }

   public static short getMolRehanX() {
      return molRehanX;
   }

   public static void setMolRehanX(short aMolRehanX) {
      molRehanX = aMolRehanX;
   }

   public static short getMolRehanY() {
      return molRehanY;
   }

   public static void setMolRehanY(short aMolRehanY) {
      molRehanY = aMolRehanY;
   }

   public static void incrementOldPremiums(String name) {
      if (System.currentTimeMillis() - lastResetOldPremiums > 10800000L) {
         oldPremiums = 0;
         lastResetOldPremiums = System.currentTimeMillis();
      }

      ++oldPremiums;
      if (!appointedSixThousand && (PlayerInfoFactory.getNumberOfPayingPlayers() + 1) % 1000 == 0) {
         logger.log(Level.INFO, name + " IS THE NUMBER " + (PlayerInfoFactory.getNumberOfPayingPlayers() + 1) + " PAYING PLAYER");
         appointedSixThousand = true;
      }
   }

   public static long getStartTime() {
      return startTime;
   }

   public static long getMillisToShutDown() {
      return millisToShutDown;
   }

   public static String getShutdownReason() {
      return shutdownReason;
   }

   public static Weather getWeather() {
      return weather;
   }

   public static int getCombatCounter() {
      return combatCounter;
   }

   public static void incrementCombatCounter() {
      ++combatCounter;
   }

   public static int getSecondsUptime() {
      return secondsUptime;
   }

   public static void incrementSecondsUptime() {
      ++secondsUptime;
      Players.getInstance().tickSecond();
      lastLagticks = (float)lagticks;
      if (lastLagticks > 0.0F) {
         lagMoveModifier = (int)Math.max(10.0F, lastLagticks / 30.0F * 24.0F);
      } else {
         lagMoveModifier = 0;
      }

      lagticks = 0;
      if (WurmCalendar.isNewYear1()) {
         logger.log(Level.INFO, "IT's NEW YEAR");
         if (secondsUptime % 20 == 0) {
            if (rand.nextBoolean()) {
               Effect globalEffect = EffectFactory.getInstance()
                  .createSpawnEff(WurmId.getNextTempItemId(), rand.nextFloat() * Zones.worldMeterSizeX, rand.nextFloat() * Zones.worldMeterSizeY, 0.0F, true);
               newYearEffects.add(globalEffect.getId());

               try {
                  ItemFactory.createItem(
                     52,
                     rand.nextFloat() * 90.0F + 1.0F,
                     globalEffect.getPosX(),
                     globalEffect.getPosY(),
                     globalEffect.getPosZ(),
                     true,
                     (byte)8,
                     getRandomRarityNotCommon(),
                     -10L,
                     "",
                     (byte)0
                  );
               } catch (Exception var3) {
               }
            } else {
               Effect globalEffect = EffectFactory.getInstance()
                  .createChristmasEff(
                     WurmId.getNextTempItemId(), rand.nextFloat() * Zones.worldMeterSizeX, rand.nextFloat() * Zones.worldMeterSizeY, 0.0F, true
                  );
               newYearEffects.add(globalEffect.getId());

               try {
                  ItemFactory.createItem(
                     52,
                     rand.nextFloat() * 90.0F + 1.0F,
                     globalEffect.getPosX(),
                     globalEffect.getPosY(),
                     globalEffect.getPosZ(),
                     true,
                     (byte)8,
                     getRandomRarityNotCommon(),
                     -10L,
                     "",
                     (byte)0
                  );
               } catch (Exception var2) {
               }
            }
         }

         if (secondsUptime % 11 == 0) {
            Zones.sendNewYear();
         }
      } else if (WurmCalendar.isAfterNewYear1()) {
         if (newYearEffects != null && !newYearEffects.isEmpty()) {
            for(Integer l : newYearEffects) {
               EffectFactory.getInstance().deleteEffect(l);
            }
         }

         if (newYearEffects != null) {
            newYearEffects.clear();
         }
      }
   }

   public static final byte getRandomRarityNotCommon() {
      if (rand.nextFloat() * 10000.0F <= 1.0F) {
         return 3;
      } else {
         return (byte)(rand.nextInt(100) <= 0 ? 2 : 1);
      }
   }

   private static void startSendWeatherThread() {
      (new Thread() {
         @Override
         public void run() {
            Servers.sendWeather(Server.weather.getWindRotation(), Server.weather.getWindPower(), Server.weather.getWindDir());
         }
      }).start();
      Players.getInstance().sendWeather();
   }

   private static void addShutdownHook() {
      logger.info("Adding Shutdown Hook");
      Runtime.getRuntime().addShutdownHook(new Thread("WurmServerShutdownHook-Thread") {
         @Override
         public void run() {
            Server.logger.info("\nWurm Server Shutdown hook is running\n");
            DbConnector.closeAll();
            ServerLauncher.stopLoggers();
            TradingWindow.stopLoggers();
            Players.stopLoggers();
         }
      });
   }

   public static final double getModifiedFloatEffect(double eff) {
      if (EpicServer) {
         double modEff = 0.0;
         if (eff >= 1.0) {
            if (eff <= 70.0) {
               modEff = 1.3571428F * eff;
            } else {
               modEff = 0.95F + (eff - 70.0) * 0.16666667F;
            }
         } else {
            modEff = 1.0 - (1.0 - eff) * (1.0 - eff);
         }

         return modEff;
      } else {
         return eff;
      }
   }

   public static final double getModifiedPercentageEffect(double eff) {
      if (!EpicServer && !ChallengeServer) {
         return eff;
      } else {
         double modEff = 0.0;
         if (eff >= 100.0) {
            if (eff <= 7000.0) {
               modEff = 1.3571428F * eff;
            } else {
               modEff = 95.0 + (eff - 7000.0) * 0.16666667F;
            }
         } else {
            modEff = (10000.0 - (100.0 - eff) * (100.0 - eff)) / 100.0;
         }

         return modEff;
      }
   }

   public static final double getBuffedQualityEffect(double eff) {
      if (eff < 1.0) {
         return Math.max(0.05, 1.0 - (1.0 - eff) * (1.0 - eff));
      } else {
         double base = 2.0;
         double pow1 = 1.3;
         double pow2 = 3.0;
         return 1.0 + base * (1.0 - Math.pow(2.0, -Math.pow(eff - 1.0, pow1) / pow2));
      }
   }

   public static final HexMap getEpicMap() {
      return epicMap;
   }

   @Override
   public void broadCastEpicEvent(String event) {
      Servers.localServer.createChampTwit(event);
   }

   @Override
   public void broadCastEpicWinCondition(String scenarioname, String scenarioQuest) {
      Servers.localServer.createChampTwit(scenarioname + " has begun. " + scenarioQuest);
   }

   public final boolean hasThunderMode() {
      return this.thunderMode;
   }

   public final short getLowDirtHeight(int x, int y) {
      Integer xy = x | y << Constants.meshSize;
      return lowDirtHeight.containsKey(xy) ? lowDirtHeight.get(xy) : Tiles.decodeHeight(surfaceMesh.getTile(x, y));
   }

   public static final boolean isDirtHeightLower(int x, int y, short ht) {
      Integer xy = x | y << Constants.meshSize;
      short cHt;
      if (lowDirtHeight.containsKey(xy)) {
         cHt = lowDirtHeight.get(xy);
         if (ht < cHt) {
            lowDirtHeight.put(xy, ht);
         }
      } else {
         cHt = Tiles.decodeHeight(surfaceMesh.getTile(x, y));
         lowDirtHeight.put(xy, (short)Math.min(cHt, ht));
      }

      return ht < cHt;
   }

   public boolean isPS() {
      return this.isPS;
   }

   public void setIsPS(boolean ps) {
      this.isPS = ps;
   }

   public final void addGlobalTempEffect(long id, long expiretime) {
      tempEffects.put(id, expiretime);
   }

   public static short getMaxHeight() {
      return surfaceMesh.getMaxHeight();
   }

   public HighwayFinder getHighwayFinderThread() {
      return this.highwayFinderThread;
   }
}
