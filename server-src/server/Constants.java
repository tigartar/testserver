package com.wurmonline.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Constants {
   public static String dbHost = "localhost";
   public static String dbPort = ":3306";
   private static final boolean DEFAULT_DB_AUTO_MIGRATE = true;
   public static boolean dbAutoMigrate = true;
   public static boolean enabledMounts = true;
   public static boolean loadNpcs = true;
   public static boolean loadEndGameItems = true;
   public static boolean enableSpyPrevention = true;
   public static boolean enableAutoNetworking = true;
   public static boolean analyseAllDbTables = false;
   public static boolean checkAllDbTables = false;
   public static boolean optimiseAllDbTables = false;
   public static boolean useSplitCreaturesTable = false;
   public static boolean createTemporaryDatabaseIndicesAtStartup = true;
   public static boolean dropTemporaryDatabaseIndicesAtStartup = true;
   public static boolean usePrepStmts = false;
   public static boolean gatherDbStats = false;
   public static boolean checkWurmLogs = false;
   public static boolean startChallenge = false;
   public static int challengeDays = 30;
   private static final boolean DEFAULT_IS_GAME_SERVER = true;
   public static boolean isGameServer = true;
   public static boolean isEigcEnabled = false;
   public static String dbUser = "";
   public static String dbPass = "";
   public static String dbDriver = "com.mysql.jdbc.Driver";
   public static String webPath = ".";
   public static final boolean useDb = true;
   public static boolean usePooledDb = true;
   public static boolean trackOpenDatabaseResources = false;
   public static boolean enablePnpPortForward = true;
   private static Logger logger = Logger.getLogger(Constants.class.getName());
   private static Properties props = null;
   public static int numberOfDirtyMeshRowsToSaveEachCall = 1;
   public static boolean useDirectByteBuffersForMeshIO = false;
   public static int meshSize;
   public static boolean createSeeds = false;
   public static boolean devmode = false;
   public static String motd = "Wurm has been waiting for you.";
   public static String skillTemplatesDBPath = "templates" + File.separator + "skills" + File.separator;
   public static String zonesDBPath = "zones" + File.separator;
   public static String itemTemplatesDBPath = "templates" + File.separator + "items" + File.separator;
   public static String creatureTemplatesDBPath = "templates" + File.separator + "creatures" + File.separator;
   public static String creatureStatsDBPath = "creatures" + File.separator;
   public static String playerStatsDBPath = "players" + File.separator;
   public static String itemStatsDBPath = "items" + File.separator;
   public static String tileStatsDBPath = "tiles" + File.separator;
   public static String itemOldStatsDBPath = "olditems" + File.separator;
   public static String creatureOldStatsDBPath = "deadCreatures" + File.separator;
   public static boolean RUNBATCH = false;
   public static boolean maintaining = false;
   public static boolean useQueueToSendDataToPlayers = false;
   public static boolean useMultiThreadedBankPolling = false;
   public static boolean useScheduledExecutorToCountEggs = false;
   public static boolean useScheduledExecutorToSaveConstants = false;
   public static boolean useScheduledExecutorToSaveDirtyMeshRows = false;
   public static boolean useScheduledExecutorToSendTimeSync = false;
   public static boolean useScheduledExecutorToSwitchFatigue = false;
   public static boolean useScheduledExecutorToTickCalendar = false;
   public static boolean useScheduledExecutorToWriteLogs = false;
   public static boolean useScheduledExecutorForServer = false;
   public static int scheduledExecutorServiceThreads = 1;
   public static String playerStatLog = "numplayers.log";
   public static String logonStatLog = "numlogons.log";
   public static String ipStatLog = "numips.log";
   public static String tutorialLog = "tutorial.log";
   public static String newbieStatLog = "newbies.log";
   public static String totIpStatLog = "totalips.log";
   public static String payingLog = "paying.log";
   public static String subscriptionLog = "subscriptions.log";
   public static String moneyLog = "mrtgmoney.log";
   public static String economyLog = "economy.log";
   public static String expiryLog = "expiry.log";
   public static String lagLog = "lag.log";
   public static String retentionStatLog = "retention.log";
   public static String retentionPercentStatLog = "retentionpercent.log";
   public static boolean useItemTransferLog = false;
   public static boolean useTileEventLog = false;
   public static boolean useDatabaseForServerStatisticsLog = false;
   public static boolean useScheduledExecutorToUpdateCreaturePositionInDatabase = false;
   public static int numberOfDbCreaturePositionsToUpdateEachTime = 500;
   public static boolean useScheduledExecutorToUpdatePlayerPositionInDatabase = false;
   public static int numberOfDbPlayerPositionsToUpdateEachTime = 500;
   public static boolean useScheduledExecutorToUpdateItemDamageInDatabase = false;
   public static int numberOfDbItemDamagesToUpdateEachTime = 500;
   public static boolean useScheduledExecutorToUpdateItemOwnerInDatabase = true;
   public static boolean useScheduledExecutorToUpdateItemLastOwnerInDatabase = true;
   public static boolean useScheduledExecutorToUpdateItemParentInDatabase = true;
   public static int numberOfDbItemOwnersToUpdateEachTime = 500;
   public static boolean useScheduledExecutorToConnectToTwitter = true;
   public static boolean useScheduledExecutorForTrello = false;
   public static long lagThreshold = 1000L;
   static boolean crashed = true;
   public static boolean respawnUniques = false;
   public static boolean pruneDb = true;
   public static boolean reprospect = false;
   public static boolean caveImg = false;
   public static long minMillisBetweenPlayerConns = 1000L;
   public static String trelloBoardid = "";
   public static String trelloApiKey = "";
   public static String trelloToken = null;
   public static String trelloMVBoardId = "";
   private static final boolean DEFAULT_USE_INCOMING_RMI = false;
   public static boolean useIncomingRMI = false;
   public static boolean isNewbieFriendly = false;

   public static final void load() {
      props = new Properties();
      File file = null;

      try {
         file = new File(ServerDirInfo.getConstantsFileName());
         logger.info("Loading configuration file at " + file.getAbsolutePath());
         FileInputStream fis = new FileInputStream(file);
         props.load(fis);
         fis.close();
      } catch (FileNotFoundException var11) {
         logger.log(Level.SEVERE, "Failed to locate wurm initializer file at " + file.getAbsolutePath());

         try {
            save();
         } catch (Exception var10) {
            logger.log(Level.SEVERE, "Failed to create wurm initializer file at " + file.getAbsolutePath(), (Throwable)var10);
         }
      } catch (IOException var12) {
         logger.log(Level.SEVERE, "Failed to load properties at " + file.getAbsolutePath());
      }

      try {
         motd = props.getProperty("MOTD");
         File dbdir = null;
         dbHost = props.getProperty("DB_HOST");
         dbUser = props.getProperty("DB_USER");
         dbPass = props.getProperty("DB_PASS");
         dbDriver = props.getProperty("DB_DRIVER");
         webPath = props.getProperty("WEB_PATH");
         usePooledDb = getBoolean("USE_POOLED_DB", false);
         trackOpenDatabaseResources = getBoolean("TRACK_OPEN_DATABASE_RESOURCES", false);
         createSeeds = getBoolean("CREATESEEDS", false);
         if (props.getProperty("DB_PORT") != null && props.getProperty("DB_PORT").length() > 0) {
            dbPort = ":" + props.getProperty("DB_PORT");
         }

         dbAutoMigrate = getBoolean("DB_AUTO_MIGRATE", true);

         try {
            numberOfDirtyMeshRowsToSaveEachCall = Integer.parseInt(props.getProperty("NUMBER_OF_DIRTY_MESH_ROWS_TO_SAVE_EACH_CALL"));
            useDirectByteBuffersForMeshIO = getBoolean("USE_DIRECT_BYTE_BUFFERS_FOR_MESHIO", false);
         } catch (Exception var9) {
            numberOfDirtyMeshRowsToSaveEachCall = 10;
            useDirectByteBuffersForMeshIO = false;
         }

         File worldMachineOutput = new File(ServerDirInfo.getFileDBPath() + "top_layer.map");
         long baseFileSize = worldMachineOutput.length();
         int mapDimension = (int)Math.sqrt((double)baseFileSize) / 2;
         meshSize = (int)(Math.log((double)mapDimension) / Math.log(2.0));
         System.out.println("Meshsize=" + meshSize);
         devmode = getBoolean("DEVMODE", false);
         crashed = getBoolean("CRASHED", false);
         RUNBATCH = getBoolean("RUNBATCH", false);
         maintaining = getBoolean("MAINTAINING", false);
         checkWurmLogs = getBoolean("CHECK_WURMLOGS", false);

         try {
            startChallenge = getBoolean("STARTCHALLENGE", false);
            challengeDays = Integer.parseInt(props.getProperty("CHALLENGEDAYS"));
         } catch (Exception var8) {
         }

         isGameServer = getBoolean("IS_GAME_SERVER", true);
         lagThreshold = getLong("LAG_THRESHOLD", 1000L);
         useSplitCreaturesTable = getBoolean("USE_SPLIT_CREATURES_TABLE", false);
         analyseAllDbTables = getBoolean("ANALYSE_ALL_DB_TABLES", false);
         checkAllDbTables = getBoolean("CHECK_ALL_DB_TABLES", false);
         optimiseAllDbTables = getBoolean("OPTIMISE_ALL_DB_TABLES", false);
         usePrepStmts = getBoolean("PREPSTATEMENTS", false);
         gatherDbStats = getBoolean("DBSTATS", false);
         pruneDb = getBoolean("PRUNEDB", false);
         reprospect = getBoolean("PROSPECT", false);
         props.put("PROSPECT", String.valueOf(false));
         caveImg = getBoolean("CAVEIMG", false);
         props.put("CAVEIMG", String.valueOf(false));

         try {
            respawnUniques = getBoolean("RESPAWN", false);
            props.put("RESPAWN", String.valueOf(false));
         } catch (Exception var7) {
            logger.log(Level.WARNING, "Not respawning uniques");
         }

         loadNpcs = getBoolean("NPCS", true);
         minMillisBetweenPlayerConns = getLong("PLAYER_CONN_MILLIS", 1000L);
         useQueueToSendDataToPlayers = getBoolean("USE_QUEUE_TO_SEND_DATA_TO_PLAYERS", false);
         useMultiThreadedBankPolling = getBoolean("USE_MULTI_THREADED_BANK_POLLING", false);
         useScheduledExecutorToCountEggs = getBoolean("USE_SCHEDULED_EXECUTOR_TO_COUNT_EGGS", false);
         useScheduledExecutorToSaveDirtyMeshRows = getBoolean("USE_SCHEDULED_EXECUTOR_TO_SAVE_DIRTY_MESH_ROWS", false);
         useScheduledExecutorToSendTimeSync = getBoolean("USE_SCHEDULED_EXECUTOR_TO_SEND_TIME_SYNC", false);
         useScheduledExecutorToSwitchFatigue = getBoolean("USE_SCHEDULED_EXECUTOR_TO_SWITCH_FATIGUE", false);
         useScheduledExecutorToTickCalendar = getBoolean("USE_SCHEDULED_EXECUTOR_TO_TICK_CALENDAR", false);
         useScheduledExecutorToWriteLogs = getBoolean("USE_SCHEDULED_EXECUTOR", false);
         useScheduledExecutorForServer = getBoolean("USE_SCHEDULED_EXECUTOR_FOR_SERVER", false);
         useScheduledExecutorForTrello = getBoolean("USE_SCHEDULED_EXECUTOR_FOR_TRELLO", false);
         scheduledExecutorServiceThreads = getInt("SCHEDULED_EXECUTOR_SERVICE_NUMBER_OF_THREADS", scheduledExecutorServiceThreads);
         useItemTransferLog = getBoolean("USE_ITEM_TRANSFER_LOG", false);
         useTileEventLog = getBoolean("USE_TILE_LOG", false);
         useDatabaseForServerStatisticsLog = getBoolean("USE_DATABASE_FOR_SERVER_STATISTICS_LOG", false);
         useScheduledExecutorToUpdateCreaturePositionInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_CREATURE_POSITION_IN_DATABASE", false);
         useScheduledExecutorToUpdatePlayerPositionInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_PLAYER_POSITION_IN_DATABASE", false);
         useScheduledExecutorToUpdateItemDamageInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_DAMAGE_IN_DATABASE", false);
         useScheduledExecutorToUpdateItemOwnerInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_OWNER_IN_DATABASE", true);
         useScheduledExecutorToUpdateItemLastOwnerInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_LASTOWNER_IN_DATABASE", true);
         useScheduledExecutorToUpdateItemParentInDatabase = getBoolean("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_PARENT_IN_DATABASE", true);
         numberOfDbCreaturePositionsToUpdateEachTime = getInt(
            "NUMBER_OF_DB_CREATURE_POSITIONS_TO_UPDATE_EACH_TIME", numberOfDbCreaturePositionsToUpdateEachTime
         );
         numberOfDbPlayerPositionsToUpdateEachTime = getInt("NUMBER_OF_DB_PLAYER_POSITIONS_TO_UPDATE_EACH_TIME", numberOfDbPlayerPositionsToUpdateEachTime);
         numberOfDbItemDamagesToUpdateEachTime = getInt("NUMBER_OF_DB_ITEM_DAMAGES_TO_UPDATE_EACH_TIME", numberOfDbItemDamagesToUpdateEachTime);
         numberOfDbItemOwnersToUpdateEachTime = getInt("NUMBER_OF_DB_ITEM_OWNERS_TO_UPDATE_EACH_TIME", numberOfDbItemOwnersToUpdateEachTime);
         trelloBoardid = props.getProperty("TRELLO_BOARD_ID", "");
         trelloMVBoardId = props.getProperty("TRELLO_MUTE_VOTE_BOARD_ID", "");
         trelloApiKey = props.getProperty("TRELLO_APIKEY", "");
         trelloToken = props.getProperty("TRELLO_TOKEN");
         enableAutoNetworking = getBoolean("AUTO_NETWORKING", true);
         enablePnpPortForward = getBoolean("ENABLE_PNP_PORT_FORWARD", true);
         useIncomingRMI = getBoolean("USE_INCOMING_RMI", false);
         String logfile = props.getProperty("PLAYERLOG");
         if (logfile == null || logfile.length() <= 0) {
            logger.log(Level.WARNING, "PLAYERLOG not specified. Using default: " + playerStatLog);
         } else if (logfile.endsWith(".log")) {
            playerStatLog = logfile;
         } else {
            logger.log(Level.WARNING, "PLAYERLOG file does not end with '.log'. Using default: " + playerStatLog);
         }
      } catch (Exception var13) {
         logger.log(Level.WARNING, "Failed to load property.", (Throwable)var13);
      }
   }

   private static boolean getBoolean(String key, boolean defaultValue) {
      String maybeBoolean = props.getProperty(key);
      return maybeBoolean == null ? defaultValue : Boolean.parseBoolean(maybeBoolean);
   }

   private static int getInt(String key, int defaultValue) {
      String maybeInt = props.getProperty(key);
      if (maybeInt == null) {
         System.out.println(key + " - " + maybeInt);
         return defaultValue;
      } else {
         try {
            return Integer.parseInt(maybeInt);
         } catch (NumberFormatException var4) {
            System.out.println(key + " - " + maybeInt);
            return defaultValue;
         }
      }
   }

   private static long getLong(String key, long defaultValue) {
      String maybeLong = props.getProperty(key);
      if (maybeLong == null) {
         return defaultValue;
      } else {
         try {
            return Long.parseLong(maybeLong);
         } catch (NumberFormatException var5) {
            System.out.println(key + " - " + maybeLong);
            return defaultValue;
         }
      }
   }

   private Constants() {
   }

   public static int getMeshSize() {
      return meshSize;
   }

   public static void save() {
      File file = new File(ServerDirInfo.getConstantsFileName());
      if (logger.isLoggable(Level.FINER)) {
         logger.finer("Saving wurm initializer file at " + file.getAbsolutePath());
      }

      try {
         if (!ServerDirInfo.getFileDBPath().endsWith(File.separator)) {
            ServerDirInfo.setFileDBPath(ServerDirInfo.getFileDBPath() + File.separator);
         }

         props.put("DBPATH", ServerDirInfo.getFileDBPath());
         props.put("MOTD", motd);
         props.put("CHECK_WURMLOGS", String.valueOf(checkWurmLogs));
         props.put("LAG_THRESHOLD", String.valueOf(lagThreshold));
         props.put("DB_HOST", dbHost);
         props.put("DB_USER", dbUser);
         props.put("DB_PASS", dbPass);
         props.put("DB_DRIVER", dbDriver);
         props.put("USEDB", String.valueOf(true));
         props.put("USE_POOLED_DB", String.valueOf(usePooledDb));
         props.put("TRACK_OPEN_DATABASE_RESOURCES", String.valueOf(trackOpenDatabaseResources));
         props.put("NUMBER_OF_DIRTY_MESH_ROWS_TO_SAVE_EACH_CALL", Integer.toString(numberOfDirtyMeshRowsToSaveEachCall));
         props.put("USE_DIRECT_BYTE_BUFFERS_FOR_MESHIO", Boolean.toString(useDirectByteBuffersForMeshIO));
         props.put("MAINTAINING", String.valueOf(false));
         props.put("RUNBATCH", String.valueOf(false));
         props.put("PROSPECT", String.valueOf(false));
         props.put("CAVEIMG", String.valueOf(false));
         props.put("USE_QUEUE_TO_SEND_DATA_TO_PLAYERS", Boolean.toString(useQueueToSendDataToPlayers));
         props.put("USE_MULTI_THREADED_BANK_POLLING", Boolean.toString(useMultiThreadedBankPolling));
         props.put("USE_SCHEDULED_EXECUTOR_TO_COUNT_EGGS", Boolean.toString(useScheduledExecutorToCountEggs));
         props.put("USE_SCHEDULED_EXECUTOR_TO_SAVE_DIRTY_MESH_ROWS", Boolean.toString(useScheduledExecutorToSaveDirtyMeshRows));
         props.put("USE_SCHEDULED_EXECUTOR_TO_SEND_TIME_SYNC", Boolean.toString(useScheduledExecutorToSendTimeSync));
         props.put("USE_SCHEDULED_EXECUTOR_TO_SWITCH_FATIGUE", Boolean.toString(useScheduledExecutorToSwitchFatigue));
         props.put("USE_SCHEDULED_EXECUTOR_TO_TICK_CALENDAR", Boolean.toString(useScheduledExecutorToTickCalendar));
         props.put("USE_SCHEDULED_EXECUTOR", Boolean.toString(useScheduledExecutorToWriteLogs));
         props.put("USE_SCHEDULED_EXECUTOR_FOR_SERVER", Boolean.toString(useScheduledExecutorForServer));
         props.put("USE_SCHEDULED_EXECUTOR_FOR_TRELLO", Boolean.toString(useScheduledExecutorForTrello));
         props.put("SCHEDULED_EXECUTOR_SERVICE_NUMBER_OF_THREADS", Integer.toString(scheduledExecutorServiceThreads));
         props.put("PLAYERLOG", playerStatLog);
         props.put("USE_ITEM_TRANSFER_LOG", Boolean.toString(useItemTransferLog));
         props.put("USE_TILE_LOG", Boolean.toString(useTileEventLog));
         props.put("USE_DATABASE_FOR_SERVER_STATISTICS_LOG", Boolean.toString(useDatabaseForServerStatisticsLog));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_CREATURE_POSITION_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdateCreaturePositionInDatabase));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_PLAYER_POSITION_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdatePlayerPositionInDatabase));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_DAMAGE_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdateItemDamageInDatabase));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_OWNER_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdateItemOwnerInDatabase));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_LASTOWNER_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdateItemLastOwnerInDatabase));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_ITEM_Parent_IN_DATABASE", Boolean.toString(useScheduledExecutorToUpdateItemParentInDatabase));
         props.put("NUMBER_OF_DB_CREATURE_POSITIONS_TO_UPDATE_EACH_TIME", Integer.toString(numberOfDbCreaturePositionsToUpdateEachTime));
         props.put("NUMBER_OF_DB_PLAYER_POSITIONS_TO_UPDATE_EACH_TIME", Integer.toString(numberOfDbPlayerPositionsToUpdateEachTime));
         props.put("NUMBER_OF_DB_ITEM_DAMAGES_TO_UPDATE_EACH_TIME", Integer.toString(numberOfDbItemDamagesToUpdateEachTime));
         props.put("NUMBER_OF_DB_ITEM_OWNERS_TO_UPDATE_EACH_TIME", Integer.toString(numberOfDbItemOwnersToUpdateEachTime));
         props.put("USE_SCHEDULED_EXECUTOR_TO_UPDATE_TWITTER", Boolean.toString(useScheduledExecutorToConnectToTwitter));
         props.put("WEB_PATH", webPath);
         props.put("CREATESEEDS", "false");
         props.put("DEVMODE", String.valueOf(devmode));
         props.put("CRASHED", String.valueOf(crashed));
         props.put("PRUNEDB", String.valueOf(pruneDb));
         props.put("PLAYER_CONN_MILLIS", String.valueOf(minMillisBetweenPlayerConns));
         props.put("USE_SPLIT_CREATURES_TABLE", Boolean.toString(useSplitCreaturesTable));
         props.put("ANALYSE_ALL_DB_TABLES", Boolean.toString(analyseAllDbTables));
         props.put("CHECK_ALL_DB_TABLES", Boolean.toString(checkAllDbTables));
         props.put("OPTIMISE_ALL_DB_TABLES", Boolean.toString(optimiseAllDbTables));
         props.put("CREATE_TEMPORARY_DATABASE_INDICES_AT_STARTUP", Boolean.toString(createTemporaryDatabaseIndicesAtStartup));
         props.put("DROP_TEMPORARY_DATABASE_INDICES_AT_STARTUP", Boolean.toString(dropTemporaryDatabaseIndicesAtStartup));
         props.put("PREPSTATEMENTS", String.valueOf(usePrepStmts));
         props.put("DBSTATS", String.valueOf(gatherDbStats));
         props.put("MOUNTS", String.valueOf(enabledMounts));
         props.put("DB_PORT", String.valueOf(dbPort.replace(":", "")));
         props.put("TRELLO_BOARD_ID", trelloBoardid);
         props.put("TRELLO_MUTE_VOTE_BOARD_ID", trelloMVBoardId);
         props.put("TRELLO_APIKEY", trelloApiKey);
         props.put("NPCS", Boolean.toString(loadNpcs));
         if (trelloToken != null) {
            props.put("TRELLO_TOKEN", trelloToken);
         }
      } catch (Exception var2) {
         logger.log(Level.SEVERE, "Failed to create wurm initializer file at " + file.getAbsolutePath(), (Throwable)var2);
      }
   }

   static void logConstantValues(boolean aWithPasswords) {
      logger.info("motd: " + motd);
      logger.info("");
      logger.info("fileName: " + ServerDirInfo.getConstantsFileName());
      logger.info("");
      logger.info("Check WURMLOGS: " + checkWurmLogs);
      logger.info("isGameServer: " + isGameServer);
      logger.info("devmode: " + devmode);
      logger.info("maintaining: " + maintaining);
      logger.info("crashed: " + crashed);
      logger.info("RUNBATCH: " + RUNBATCH);
      logger.info("pruneDb: " + pruneDb);
      logger.info("reprospect: " + reprospect);
      logger.info("caveImg: " + caveImg);
      logger.info("createSeeds: " + createSeeds);
      logger.info("Min millis between player connections: " + minMillisBetweenPlayerConns);
      logger.info("");
      logger.info("fileDBPath: " + ServerDirInfo.getFileDBPath());
      logger.info("dbHost: " + dbHost);
      logger.info("useSplitCreaturesTable: " + useSplitCreaturesTable);
      logger.info("analyseAllDbTables: " + analyseAllDbTables);
      logger.info("checkAllDbTables: " + checkAllDbTables);
      logger.info("optimiseAllDbTables: " + optimiseAllDbTables);
      logger.info("createTemporaryDatabaseIndicesAtStartup: " + createTemporaryDatabaseIndicesAtStartup);
      logger.info("dropTemporaryDatabaseIndicesAtStartup: " + dropTemporaryDatabaseIndicesAtStartup);
      logger.info("usePrepStmts: " + usePrepStmts);
      logger.info("gatherDbStats: " + gatherDbStats);
      logger.info("");
      logger.info("");
      logger.info("");
      logger.info("dbUser: " + dbUser);
      if (aWithPasswords) {
         logger.info("dbPass: " + dbPass);
      }

      logger.info("dbDriver: " + dbDriver);
      logger.info("useDb: true");
      logger.info("usePooledDb: " + usePooledDb);
      logger.info("dbPort: " + dbPort);
      logger.info("trackOpenDatabaseResources: " + trackOpenDatabaseResources);
      logger.info("");
      logger.info("numberOfDirtyMeshRowsToSaveEachCall: " + numberOfDirtyMeshRowsToSaveEachCall);
      logger.info("useDirectByteBuffersForMeshIO: " + useDirectByteBuffersForMeshIO);
      logger.info("");
      logger.info("webPath: " + webPath);
      logger.info("skillTemplatesDBPath: " + skillTemplatesDBPath);
      logger.info("zonesDBPath: " + zonesDBPath);
      logger.info("itemTemplatesDBPath: " + itemTemplatesDBPath);
      logger.info("creatureStatsDBPath: " + creatureStatsDBPath);
      logger.info("playerStatsDBPath: " + playerStatsDBPath);
      logger.info("itemStatsDBPath: " + itemStatsDBPath);
      logger.info("tileStatsDBPath: " + tileStatsDBPath);
      logger.info("itemOldStatsDBPath: " + itemOldStatsDBPath);
      logger.info("creatureOldStatsDBPath: " + creatureOldStatsDBPath);
      logger.info("useQueueToSendDataToPlayers: " + useQueueToSendDataToPlayers);
      logger.info("useMultiThreadedBankPolling: " + useMultiThreadedBankPolling);
      logger.info("useScheduledExecutorToCountEggs: " + useScheduledExecutorToCountEggs);
      logger.info("useScheduledExecutorToSaveConstants: " + useScheduledExecutorToSaveConstants);
      logger.info("useScheduledExecutorToSaveDirtyMeshRows: " + useScheduledExecutorToSaveDirtyMeshRows);
      logger.info("useScheduledExecutorToSendTimeSync: " + useScheduledExecutorToSendTimeSync);
      logger.info("useScheduledExecutorToSwitchFatigue: " + useScheduledExecutorToSwitchFatigue);
      logger.info("useScheduledExecutorToTickCalendar: " + useScheduledExecutorToTickCalendar);
      logger.info("useScheduledExecutorToWriteLogs: " + useScheduledExecutorToWriteLogs);
      logger.info("useScheduledExecutorForServer: " + useScheduledExecutorForServer);
      logger.info("scheduledExecutorServiceThreads: " + scheduledExecutorServiceThreads);
      logger.info("useItemTransferLog: " + useItemTransferLog);
      logger.info("useTileEventLog: " + useTileEventLog);
      logger.info("useDatabaseForServerStatisticsLog: " + useDatabaseForServerStatisticsLog);
      logger.info("useScheduledExecutorToUpdateCreaturePositionInDatabase: " + useScheduledExecutorToUpdateCreaturePositionInDatabase);
      logger.info("useScheduledExecutorToUpdatePlayerPositionInDatabase: " + useScheduledExecutorToUpdatePlayerPositionInDatabase);
      logger.info("useScheduledExecutorToUpdateItemDamageInDatabase: " + useScheduledExecutorToUpdateItemDamageInDatabase);
      logger.info("useScheduledExecutorToUpdateItemOwnerInDatabase: " + useScheduledExecutorToUpdateItemOwnerInDatabase);
      logger.info("useScheduledExecutorToUpdateItemLastOwnerInDatabase: " + useScheduledExecutorToUpdateItemLastOwnerInDatabase);
      logger.info("useScheduledExecutorToUpdateItemParentInDatabase: " + useScheduledExecutorToUpdateItemParentInDatabase);
      logger.info("useScheduledExecutorToConnectToTwitter: " + useScheduledExecutorToConnectToTwitter);
      logger.info("numberOfDbCreaturePositionsToUpdateEachTime: " + numberOfDbCreaturePositionsToUpdateEachTime);
      logger.info("numberOfDbPlayerPositionsToUpdateEachTime: " + numberOfDbPlayerPositionsToUpdateEachTime);
      logger.info("numberOfDbItemDamagesToUpdateEachTime: " + numberOfDbItemDamagesToUpdateEachTime);
      logger.info("numberOfDbItemOwnersToUpdateEachTime: " + numberOfDbItemOwnersToUpdateEachTime);
      logger.info("playerStatLog: " + playerStatLog);
      logger.info("logonStatLog: " + logonStatLog);
      logger.info("ipStatLog: " + ipStatLog);
      logger.info("newbieStatLog: " + newbieStatLog);
      logger.info("totIpStatLog: " + totIpStatLog);
      logger.info("payingLog: " + payingLog);
      logger.info("moneyLog: " + moneyLog);
      logger.info("lagLog: " + lagLog);
      logger.info("lagThreshold: " + lagThreshold);
      logger.info("Eigc enabled " + isEigcEnabled);
      logger.info("useIncomingRMI: " + useIncomingRMI);
   }

   static {
      load();
   }

   static final class ConstantsSaver implements Runnable {
      @Override
      public void run() {
         if (Constants.logger.isLoggable(Level.FINER)) {
            Constants.logger.finer("Running newSingleThreadScheduledExecutor for saving Constants to wurm.ini");
         }

         try {
            long now = System.nanoTime();
            Constants.save();
            float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
            if (lElapsedTime > (float)Constants.lagThreshold) {
               Constants.logger.info("Finished saving Constants to wurm.ini, which took " + lElapsedTime + " millis.");
            }
         } catch (RuntimeException var4) {
            Constants.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Constants.save()", (Throwable)var4);
            throw var4;
         }
      }
   }
}
