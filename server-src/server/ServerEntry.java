package com.wurmonline.server;

import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.intra.IntraClient;
import com.wurmonline.server.intra.IntraServerConnectionListener;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import com.wurmonline.server.webinterface.WcKingdomInfo;
import com.wurmonline.server.webinterface.WcPlayerStatus;
import com.wurmonline.server.webinterface.WcSpawnPoints;
import com.wurmonline.server.webinterface.WcTicket;
import com.wurmonline.server.zones.TilePoller;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ServerEntry implements MiscConstants, IntraServerConnectionListener, TimeConstants, Comparable<ServerEntry>, MonetaryConstants {
   public int id;
   public int SPAWNPOINTJENNX;
   public int SPAWNPOINTJENNY;
   public int SPAWNPOINTMOLX;
   public int SPAWNPOINTMOLY;
   public int SPAWNPOINTLIBX;
   public int SPAWNPOINTLIBY;
   public boolean HOMESERVER;
   public boolean PVPSERVER;
   public boolean EPIC = false;
   public String INTRASERVERADDRESS = "";
   public String INTRASERVERPORT = "";
   public int RMI_PORT = 7220;
   public int REGISTRATION_PORT = 7221;
   public ServerEntry serverNorth;
   public ServerEntry serverEast;
   public ServerEntry serverSouth;
   public ServerEntry serverWest;
   public String INTRASERVERPASSWORD = "";
   public boolean testServer = false;
   public String name = "Unknown";
   public String mapname = "";
   public String EXTERNALIP = "";
   public boolean LOGINSERVER = false;
   public String EXTERNALPORT = "";
   public String STEAMQUERYPORT = "";
   private byte[] externalIpBytes = null;
   private byte[] internalIpBytes = null;
   public byte KINGDOM = 0;
   public boolean challengeServer = false;
   public boolean entryServer = false;
   public boolean ISPAYMENT = false;
   protected boolean isAvailable = false;
   private static final Logger logger = Logger.getLogger(ServerEntry.class.getName());
   private boolean done;
   private IntraClient client;
   private long timeOutAt;
   private long timeOutTime = 20000L;
   private long startTime;
   private long lastPing = 0L;
   public long lastDecreasedChampionPoints = 0L;
   private final Set<Byte> existingKingdoms = new HashSet<>();
   private float skillGainRate = 1.0F;
   private float actionTimer = 1.0F;
   private int hotaDelay = 2160;
   private float combatRatingModifier = 1.0F;
   String consumerKeyToUse = "";
   String consumerSecretToUse = "";
   String applicationToken = "";
   String applicationSecret = "";
   String champConsumerKeyToUse = "";
   String champConsumerSecretToUse = "";
   String champApplicationToken = "";
   String champApplicationSecret = "";
   private static final String SET_CHAMP_TWITTER = "UPDATE SERVERS SET CHAMPTWITKEY=?,CHAMPTWITSECRET=?,CHAMPTWITAPP=?,CHAMPTWITAPPSECRET=? WHERE SERVER=?";
   private static final String SET_TWITTER = "UPDATE SERVERS SET TWITKEY=?,TWITSECRET=?,TWITAPP=?,TWITAPPSECRET=? WHERE SERVER=?";
   private static final int PLAYER_LIMIT_MARGIN = 10;
   private boolean canTwit = false;
   public boolean canTwitChamps = false;
   public boolean maintaining = false;
   public int pLimit = 200;
   public boolean playerLimitOverridable = true;
   public int maxCreatures = 1000;
   public int maxTypedCreatures = 250;
   public float percentAggCreatures = 10.0F;
   public int treeGrowth = 20;
   public int currentPlayers = 0;
   public int isShuttingDownIn = 0;
   public boolean loggingIn = false;
   private static final String SET_CHAMPSTAMP = "UPDATE SERVERS SET LASTRESETCHAMPS=? WHERE SERVER=?";
   public boolean isLocal = false;
   public boolean reloading = false;
   public int meshSize;
   public boolean shouldResendKingdoms;
   private long movedArtifacts;
   private static final String SET_MOVEDARTIFACTS = "UPDATE SERVERS SET MOVEDARTIS=? WHERE SERVER=?";
   private static final String SET_SPAWNEDUNIQUE = "UPDATE SERVERS SET SPAWNEDUNIQUE=? WHERE SERVER=?";
   private static final String MOVEPLAYERS = "UPDATE PLAYERS SET CURRENTSERVER=? WHERE CURRENTSERVER=?";
   private static final String UPDATE_CAHELPGROUP = "UPDATE SERVERS SET CAHELPGROUP=? WHERE SERVER=?";
   private static final String UPDATE_CHALLENGETIMES = "UPDATE SERVERS SET CHALLENGESTARTED=?, CHALLENGEEND=? WHERE SERVER=?";
   public static final String SET_TIMERS = "UPDATE SERVERS SET SKILLDAYSWITCH=?,SKILLWEEKSWITCH=?,NEXTEPICPOLL=?,FATIGUESWITCH=?,NEXTHOTA=?,WORLDTIME=?,TILEREST=?,POLLTILE=?,POLLMOD=?,POLLROUND=? WHERE SERVER=?";
   private long skillDaySwitch;
   private long skillWeekSwitch;
   private long nextEpicPoll;
   private long fatigueSwitch;
   private long lastSpawnedUnique;
   private long nextHota;
   private Spawnpoint[] spawns;
   private byte caHelpGroup;
   private long challengeStarted;
   private long challengeEnds;
   public boolean isCreating;
   public boolean randomSpawns;
   public static final String UPDATE_SERVER_NEW = "UPDATE SERVERS SET SERVER=?,NAME=?,MAXCREATURES=?,MAXPLAYERS=?,PERCENT_AGG_CREATURES=?,TREEGROWTH=?,SKILLGAINRATE=?,ACTIONTIMER=?,HOTADELAY=?,PVP=?,          HOMESERVER=?,KINGDOM=?,INTRASERVERPASSWORD=?,EXTERNALIP=?,EXTERNALPORT=?,INTRASERVERADDRESS=?,INTRASERVERPORT=?,ISTEST=?,ISPAYMENT=?,LOGINSERVER=?,           RMIPORT=?,REGISTRATIONPORT=?,LOCAL=?,RANDOMSPAWNS=?,SKILLBASICSTART=?,SKILLMINDLOGICSTART=?,SKILLFIGHTINGSTART=?,SKILLOVERALLSTART=?,EPIC=?,CRMOD=?,            STEAMPW=?,UPKEEP=?,MAXDEED=?,FREEDEEDS=?,TRADERMAX=?,TRADERINIT=?,BREEDING=?,FIELDGROWTH=?,KINGSMONEY=?, MOTD=?,     TUNNELING=?,SKILLBODYCONTROLSTART=? WHERE SERVER=?";
   private float skillbasicval;
   private float skillmindval;
   private float skillfightval;
   private float skilloverallval;
   private float skillbcval;
   private String steamServerPassword;
   private boolean upkeep;
   private int maxDeedSize;
   private boolean freeDeeds;
   private int traderMaxIrons;
   private int initialTraderIrons;
   private int tunnelingHits;
   private long breedingTimer;
   private long fieldGrowthTime;
   private int kingsmoneyAtRestart;
   private String motd;
   public String adminPassword;
   int pingcounter;

   public ServerEntry() {
      this.meshSize = Constants.meshSize;
      this.shouldResendKingdoms = false;
      this.movedArtifacts = System.currentTimeMillis();
      this.skillDaySwitch = 0L;
      this.skillWeekSwitch = 0L;
      this.nextEpicPoll = 0L;
      this.fatigueSwitch = 0L;
      this.lastSpawnedUnique = 0L;
      this.nextHota = 0L;
      this.caHelpGroup = -1;
      this.challengeStarted = 0L;
      this.challengeEnds = 0L;
      this.isCreating = false;
      this.randomSpawns = false;
      this.skillbasicval = 20.0F;
      this.skillmindval = 20.0F;
      this.skillfightval = 1.0F;
      this.skilloverallval = 1.0F;
      this.skillbcval = 20.0F;
      this.steamServerPassword = "";
      this.upkeep = true;
      this.maxDeedSize = 0;
      this.freeDeeds = false;
      this.traderMaxIrons = 500000;
      this.initialTraderIrons = 10000;
      this.tunnelingHits = 51;
      this.breedingTimer = 0L;
      this.fieldGrowthTime = 86400000L;
      this.kingsmoneyAtRestart = 0;
      this.motd = "";
      this.adminPassword = "";
      this.pingcounter = 0;
   }

   ServerEntry(
      int aId,
      String aName,
      boolean aEntryServer,
      boolean aHomeServer,
      boolean aPvpServer,
      boolean aLoginServer,
      boolean aIsPayment,
      byte aKingdom,
      String aExternalIP,
      String aExternalPort,
      String aIntraServerAddress,
      String aIntraServerPort,
      String aIntraServerPassword,
      int aSpawnPointJennX,
      int aSpawnPointJennY,
      int aSpawPpointMolX,
      int aSpawPpointMolY,
      int aSpawnPointLibX,
      int aSpawnPointLibY,
      String _consumerKeyToUse,
      String _consumerSecretToUse,
      String _applicationToken,
      String _applicationSecret,
      boolean isTest,
      long lastDecreasedChamps,
      long movedArtis,
      long spawnedUniques,
      boolean challenge
   ) {
      this.meshSize = Constants.meshSize;
      this.shouldResendKingdoms = false;
      this.movedArtifacts = System.currentTimeMillis();
      this.skillDaySwitch = 0L;
      this.skillWeekSwitch = 0L;
      this.nextEpicPoll = 0L;
      this.fatigueSwitch = 0L;
      this.lastSpawnedUnique = 0L;
      this.nextHota = 0L;
      this.caHelpGroup = -1;
      this.challengeStarted = 0L;
      this.challengeEnds = 0L;
      this.isCreating = false;
      this.randomSpawns = false;
      this.skillbasicval = 20.0F;
      this.skillmindval = 20.0F;
      this.skillfightval = 1.0F;
      this.skilloverallval = 1.0F;
      this.skillbcval = 20.0F;
      this.steamServerPassword = "";
      this.upkeep = true;
      this.maxDeedSize = 0;
      this.freeDeeds = false;
      this.traderMaxIrons = 500000;
      this.initialTraderIrons = 10000;
      this.tunnelingHits = 51;
      this.breedingTimer = 0L;
      this.fieldGrowthTime = 86400000L;
      this.kingsmoneyAtRestart = 0;
      this.motd = "";
      this.adminPassword = "";
      this.pingcounter = 0;
      this.id = aId;
      this.name = aName;
      this.entryServer = aEntryServer;
      this.HOMESERVER = aHomeServer;
      this.PVPSERVER = aPvpServer;
      this.LOGINSERVER = aLoginServer;
      this.ISPAYMENT = aIsPayment;
      this.KINGDOM = aKingdom;
      this.EXTERNALIP = aExternalIP;
      this.EXTERNALPORT = aExternalPort;
      this.INTRASERVERADDRESS = aIntraServerAddress;
      this.INTRASERVERPORT = aIntraServerPort;
      this.INTRASERVERPASSWORD = aIntraServerPassword;
      this.SPAWNPOINTJENNX = aSpawnPointJennX;
      this.SPAWNPOINTJENNY = aSpawnPointJennY;
      this.SPAWNPOINTMOLX = aSpawPpointMolX;
      this.SPAWNPOINTMOLY = aSpawPpointMolY;
      this.SPAWNPOINTLIBX = aSpawnPointLibX;
      this.SPAWNPOINTLIBY = aSpawnPointLibY;
      this.consumerKeyToUse = _consumerKeyToUse;
      this.consumerSecretToUse = _consumerSecretToUse;
      this.applicationToken = _applicationToken;
      this.applicationSecret = _applicationSecret;
      this.lastDecreasedChampionPoints = lastDecreasedChamps;
      this.lastSpawnedUnique = spawnedUniques;
      this.testServer = isTest;
      this.challengeServer = challenge;
      if (movedArtis > 0L) {
         this.setMovedArtifacts(movedArtis);
      } else {
         this.movedArtifacts();
      }

      this.canTwit();
   }

   public boolean canTwit() {
      if (this.consumerKeyToUse != null
         && this.consumerKeyToUse.length() > 5
         && this.consumerSecretToUse != null
         && this.consumerSecretToUse.length() > 5
         && this.applicationToken != null
         && this.applicationToken.length() > 5
         && this.applicationSecret != null
         && this.applicationSecret.length() > 5) {
         this.canTwit = true;
      } else {
         this.canTwit = false;
      }

      if (this.champConsumerKeyToUse != null
         && this.champConsumerKeyToUse.length() > 5
         && this.champConsumerSecretToUse != null
         && this.champConsumerSecretToUse.length() > 5
         && this.champApplicationToken != null
         && this.champApplicationToken.length() > 5
         && this.champApplicationSecret != null
         && this.champApplicationSecret.length() > 5) {
         this.canTwitChamps = true;
      } else {
         this.canTwitChamps = false;
      }

      return this.canTwit;
   }

   public final boolean isChaosServer() {
      return this.id == 3 || this.testServer && this.PVPSERVER && Features.Feature.CHAOS.isEnabled();
   }

   public final boolean isChallengeServer() {
      return this.challengeServer;
   }

   public final boolean isChallengeOrEpicServer() {
      return this.challengeServer || this.EPIC;
   }

   public Twit createTwit(String message) {
      return this.canTwit
         ? new Twit(this.name, message, this.consumerKeyToUse, this.consumerSecretToUse, this.applicationToken, this.applicationSecret, false)
         : null;
   }

   public void createChampTwit(String message) {
      if (this.canTwitChamps) {
         Twit t = new Twit(
            this.name, message, this.champConsumerKeyToUse, this.champConsumerSecretToUse, this.champApplicationToken, this.champApplicationSecret, false
         );
         if (t != null) {
            Twit.twit(t);
         }
      }
   }

   public byte[] getExternalIpAsBytes() {
      if (this.externalIpBytes == null) {
         this.externalIpBytes = new byte[4];
         StringTokenizer tokens = new StringTokenizer(this.EXTERNALIP);

         for(int x = 0; tokens.hasMoreTokens(); ++x) {
            String next = tokens.nextToken();
            this.externalIpBytes[x] = Integer.valueOf(next).byteValue();
         }
      }

      return this.externalIpBytes;
   }

   public void setAvailable(boolean available, boolean maintain, int currentPlayerCount, int plimit, int secsToShutdown, int mSize) {
      this.pLimit = plimit;
      this.currentPlayers = currentPlayerCount;
      this.isShuttingDownIn = secsToShutdown;
      this.meshSize = mSize;
      this.maintaining = maintain;
      if (available != this.isAvailable) {
         this.isAvailable = available;
         if (available) {
            logger.log(Level.INFO, this.name + " is now available.");
            final int serverId = this.getId();
            (new Thread("ServerEntry.setAvailable-Thread") {
               @Override
               public void run() {
                  long now = System.nanoTime();
                  if (ServerEntry.logger.isLoggable(Level.FINE)) {
                     ServerEntry.logger.fine("Starting ServerEntry.setAvailable() thread");
                  }

                  ServerEntry.this.sendKingdomInfo();
                  ServerEntry.this.setupPlayerStates();
                  ServerEntry.this.sendSpawnpoints();
                  if (Servers.localServer.LOGINSERVER) {
                     for(EpicEntity entity : Server.getEpicMap().getAllEntities()) {
                        if (entity.isDeity()) {
                           entity.checkifServerFailed(serverId);
                        }
                     }
                  }

                  if (ServerEntry.logger.isLoggable(Level.FINE)) {
                     float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
                     ServerEntry.logger.fine("Finished ServerEntry.setAvailable() thread. That took " + lElapsedTime + " millis.");
                  }
               }
            }).start();
         } else {
            logger.log(Level.INFO, this.name + " is no longer available.");
            PlayerInfoFactory.setPlayerStatesToOffline(this.id);
         }
      }
   }

   public final void sendSpawnpoints() {
      if (Servers.getLocalServerId() != this.id) {
         Set<Spawnpoint> lSpawns = new HashSet<>();

         for(Kingdom kingdom : Kingdoms.getAllKingdoms()) {
            Village[] villages = Villages.getPermanentVillages(kingdom.getId());
            if (villages.length > 0) {
               for(Village vill : villages) {
                  String toSend = vill.getMotto();
                  if (Servers.localServer.isChallengeServer()) {
                     if (vill.getId() != 1 && vill.getId() != 7 && vill.getId() != 9) {
                        toSend = "Far Base, for new players";
                     } else {
                        toSend = "Forward Base, for experienced players";
                     }
                  }

                  lSpawns.add(new Spawnpoint(vill.getName(), (byte)1, toSend, (short)vill.getTokenX(), (short)vill.getTokenY(), true, vill.kingdom));
               }
            }
         }

         if (lSpawns.size() > 0) {
            WcSpawnPoints wcp = new WcSpawnPoints(WurmId.getNextWCCommandId());
            wcp.setSpawns(lSpawns.toArray(new Spawnpoint[lSpawns.size()]));
            wcp.sendToServer(this.id);
         }
      }
   }

   public boolean isAvailable(int power, boolean isPremium) {
      if (Servers.getLocalServerId() != this.id || this.maintaining && power <= 0) {
         if (!this.isAvailable) {
            return false;
         } else if (this.maintaining && power <= 0) {
            return false;
         } else {
            return !this.isFull() || isPremium || power > 0;
         }
      } else {
         return true;
      }
   }

   public boolean isFull() {
      return this.currentPlayers >= this.pLimit - 10;
   }

   public boolean isConnected() {
      return this.isAvailable;
   }

   public byte[] getInternalIpAsBytes() {
      if (this.internalIpBytes == null) {
         this.internalIpBytes = new byte[4];
         StringTokenizer tokens = new StringTokenizer(this.INTRASERVERADDRESS);

         for(int x = 0; tokens.hasMoreTokens(); ++x) {
            String next = tokens.nextToken();
            this.internalIpBytes[x] = Integer.valueOf(next).byteValue();
         }
      }

      return this.internalIpBytes;
   }

   private final void sendKingdomInfo() {
      LoginServerWebConnection lsw = new LoginServerWebConnection(this.id);
      if (Servers.localServer.LOGINSERVER) {
         lsw.setWeather(Server.getWeather().getWindRotation(), Server.getWeather().getWindPower(), Server.getWeather().getWindDir());
         WcKingdomInfo wc = new WcKingdomInfo(WurmId.getNextWCCommandId(), false, (byte)0);
         wc.encode();
         lsw.sendWebCommand((short)7, wc);
         WcEpicStatusReport report = new WcEpicStatusReport(WurmId.getNextWCCommandId(), false, 0, (byte)-1, -1);
         report.fillStatusReport(Server.getEpicMap());
         report.sendToServer(this.id);
      }

      Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

      for(Kingdom k : kingdoms) {
         if (k.existsHere()) {
            lsw.kingdomExists(Servers.getLocalServerId(), k.getId(), true);
         } else if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, k.getName() + " doesn't exist here");
         }
      }

      this.shouldResendKingdoms = false;
   }

   private final void setupPlayerStates() {
      if (Servers.isThisLoginServer() && this.id != Servers.loginServer.id) {
         WcPlayerStatus wps = new WcPlayerStatus();
         wps.sendToServer(this.id);
         WcTicket wt = new WcTicket(Tickets.getLatestActionDate());
         wt.sendToServer(this.id);
      } else if (!Servers.isThisLoginServer() && this.id == Servers.loginServer.id) {
         Tickets.checkBatchNos();
         PlayerInfoFactory.grabPlayerStates();
         WcTicket wt = new WcTicket(Tickets.getLatestActionDate());
         wt.sendToServer(this.id);
      }
   }

   public boolean poll() {
      if (this.id == Servers.getLocalServerId()) {
         this.isAvailable = true;
         return true;
      } else {
         if (this.client != null) {
            if (this.client.hasFailedConnection) {
               this.setAvailable(false, false, 0, 0, 0, 10);
               if (this.client != null) {
                  this.client.disconnect("Failed.");
               }

               this.client = null;
               this.done = true;
               this.loggingIn = false;
            } else if (!this.client.isConnecting && !this.client.loggedIn && !this.loggingIn) {
               this.loggingIn = true;
               this.client.login(this.INTRASERVERPASSWORD, true);
            }
         }

         if (this.client == null && System.currentTimeMillis() > this.timeOutAt) {
            this.startTime = System.currentTimeMillis();
            this.timeOutAt = this.startTime + this.timeOutTime;
            this.done = false;
            this.client = new IntraClient();
            this.loggingIn = false;
            this.client.reconnectAsynch(this.INTRASERVERADDRESS, Integer.parseInt(this.INTRASERVERPORT), this);
         }

         if (this.client != null && !this.done && !this.client.isConnecting) {
            if (System.currentTimeMillis() > this.timeOutAt && !this.client.loggedIn) {
               this.done = true;
            }

            if (!this.done) {
               try {
                  if (this.client.loggedIn && System.currentTimeMillis() - this.lastPing > 10000L) {
                     if (this.shouldResendKingdoms) {
                        this.sendKingdomInfo();
                     }

                     try {
                        this.client.executePingCommand();
                        this.lastPing = System.currentTimeMillis();
                        this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
                     } catch (Exception var2) {
                        this.done = true;
                        this.client.disconnect(var2.getMessage());
                     }
                  }

                  if (!this.done) {
                     this.client.update();
                  }
               } catch (IOException var3) {
                  logger.log(Level.INFO, "IOException to " + this.name + ". Disc:" + var3.getMessage(), (Throwable)var3);
                  this.done = true;
               }
            }
         }

         if (this.done && this.client != null) {
            this.client.disconnect("done");
            this.client = null;
         }

         return this.done;
      }
   }

   @Override
   public void commandExecuted(IntraClient aClient) {
      this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
   }

   @Override
   public void commandFailed(IntraClient aClient) {
      this.setAvailable(false, false, 0, 0, 0, 10);
      this.done = true;
      if (this.loggingIn) {
         this.loggingIn = false;
      }
   }

   @Override
   public void dataReceived(IntraClient aClient) {
      logger.log(Level.INFO, "Datareceived " + this.name);
   }

   @Override
   public void reschedule(IntraClient aClient) {
      this.setAvailable(false, false, 0, 0, 0, 10);
   }

   @Override
   public void remove(IntraClient aClient) {
      this.done = true;
   }

   @Override
   public void receivingData(ByteBuffer buffer) {
      this.maintaining = (buffer.get() & 1) == 1;
      int numsPlaying = buffer.getInt();
      int maxLimit = buffer.getInt();
      int secsToShutdown = buffer.getInt();
      int mSize = buffer.getInt();
      this.setAvailable(true, this.maintaining, numsPlaying, maxLimit, secsToShutdown, mSize);
      this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
      ++this.pingcounter;
      if (this.pingcounter == 20) {
         this.pingcounter = 0;
      }
   }

   public String getConsumerKey() {
      return this.consumerKeyToUse;
   }

   public void setConsumerKeyToUse(String aConsumerKey) {
      this.consumerKeyToUse = aConsumerKey;
   }

   public String getConsumerSecret() {
      return this.consumerSecretToUse;
   }

   public void setConsumerSecret(String aConsumerSecret) {
      this.consumerSecretToUse = aConsumerSecret;
   }

   public String getApplicationToken() {
      return this.applicationToken;
   }

   public void setApplicationToken(String aApplicationToken) {
      this.applicationToken = aApplicationToken;
   }

   public String getApplicationSecret() {
      return this.applicationSecret;
   }

   public void setApplicationSecret(String aApplicationSecret) {
      this.applicationSecret = aApplicationSecret;
   }

   void addExistingKingdom(byte kingdomId) {
      if (!this.kingdomExists(kingdomId)) {
         this.existingKingdoms.add(kingdomId);
      }
   }

   boolean kingdomExists(byte kingdomId) {
      return this.existingKingdoms.contains(kingdomId);
   }

   boolean removeKingdom(byte kingdomId) {
      return this.existingKingdoms.remove(kingdomId);
   }

   public Set<Byte> getExistingKingdoms() {
      return this.existingKingdoms;
   }

   public byte getKingdom() {
      return this.KINGDOM;
   }

   public String getName() {
      return this.name;
   }

   public int getId() {
      return this.id;
   }

   @Override
   public String toString() {
      return "ServerEntry [id: "
         + this.id
         + ", Name: "
         + this.name
         + ", IntraIP: "
         + this.INTRASERVERADDRESS
         + ':'
         + this.INTRASERVERPORT
         + ", ExternalIP: "
         + this.EXTERNALIP
         + ':'
         + this.EXTERNALPORT
         + ", canTwit: "
         + this.canTwit()
         + ']';
   }

   public final void setChampTwitter(
      String newChampConsumerKeyToUse, String newChampConsumerSecretToUse, String newChampApplicationToken, String newChampApplicationSecret
   ) {
      this.champConsumerKeyToUse = newChampConsumerKeyToUse;
      this.champConsumerSecretToUse = newChampConsumerSecretToUse;
      this.champApplicationToken = newChampApplicationToken;
      this.champApplicationSecret = newChampApplicationSecret;
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET CHAMPTWITKEY=?,CHAMPTWITSECRET=?,CHAMPTWITAPP=?,CHAMPTWITAPPSECRET=? WHERE SERVER=?");
         ps.setString(1, this.champConsumerKeyToUse);
         ps.setString(2, this.champConsumerSecretToUse);
         ps.setString(3, this.champApplicationToken);
         ps.setString(4, this.champApplicationSecret);
         ps.setInt(5, this.id);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to set champ stamp for localserver ", (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      this.canTwit();
   }

   public final void setChampStamp() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.lastDecreasedChampionPoints = System.currentTimeMillis();
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET LASTRESETCHAMPS=? WHERE SERVER=?");
         ps.setLong(1, this.lastDecreasedChampionPoints);
         ps.setInt(2, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to set champ stamp for localserver ", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final boolean saveTwitter() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET TWITKEY=?,TWITSECRET=?,TWITAPP=?,TWITAPPSECRET=? WHERE SERVER=?");
         ps.setString(1, this.consumerKeyToUse);
         ps.setString(2, this.consumerSecretToUse);
         ps.setString(3, this.applicationToken);
         ps.setString(4, this.applicationSecret);
         ps.setInt(5, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save twitter for server " + this.id, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return this.canTwit();
   }

   public final void movedArtifacts() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.setMovedArtifacts(System.currentTimeMillis());
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET MOVEDARTIS=? WHERE SERVER=?");
         ps.setLong(1, this.getMovedArtifacts());
         ps.setInt(2, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to set moved artifacts stamp for localserver ", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void setCAHelpGroup(byte dbCAHelpGroup) {
      this.caHelpGroup = dbCAHelpGroup;
   }

   public final byte getCAHelpGroup() {
      return this.caHelpGroup;
   }

   public final void saveChallengeTimes() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET CHALLENGESTARTED=?, CHALLENGEEND=? WHERE SERVER=?");
         ps.setLong(1, this.challengeStarted);
         ps.setLong(2, this.challengeEnds);
         ps.setLong(3, (long)this.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to update ChallengeTimes for localserver ", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void updateCAHelpGroup(byte newCAHelpGroup) {
      if (this.caHelpGroup != newCAHelpGroup) {
         this.caHelpGroup = newCAHelpGroup;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement("UPDATE SERVERS SET CAHELPGROUP=? WHERE SERVER=?");
            ps.setByte(1, newCAHelpGroup);
            ps.setInt(2, this.id);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update CAHelp Group for localserver ", (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public final void setLastSpawnedUnique(long val) {
      this.lastSpawnedUnique = val;
   }

   public final long getLastSpawnedUnique() {
      return this.lastSpawnedUnique;
   }

   public final void spawnedUnique() {
      this.lastSpawnedUnique = System.currentTimeMillis();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE SERVERS SET SPAWNEDUNIQUE=? WHERE SERVER=?");
         ps.setLong(1, this.lastSpawnedUnique);
         ps.setInt(2, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to set moved artifacts stamp for localserver ", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void movePlayersFromId(int oldId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET CURRENTSERVER=? WHERE CURRENTSERVER=?");
         ps.setInt(1, this.id);
         ps.setInt(2, oldId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to move players from server id " + oldId + " to localserver id " + this.id, (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void saveNewGui(int oldId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE SERVERS SET SERVER=?,NAME=?,MAXCREATURES=?,MAXPLAYERS=?,PERCENT_AGG_CREATURES=?,TREEGROWTH=?,SKILLGAINRATE=?,ACTIONTIMER=?,HOTADELAY=?,PVP=?,          HOMESERVER=?,KINGDOM=?,INTRASERVERPASSWORD=?,EXTERNALIP=?,EXTERNALPORT=?,INTRASERVERADDRESS=?,INTRASERVERPORT=?,ISTEST=?,ISPAYMENT=?,LOGINSERVER=?,           RMIPORT=?,REGISTRATIONPORT=?,LOCAL=?,RANDOMSPAWNS=?,SKILLBASICSTART=?,SKILLMINDLOGICSTART=?,SKILLFIGHTINGSTART=?,SKILLOVERALLSTART=?,EPIC=?,CRMOD=?,            STEAMPW=?,UPKEEP=?,MAXDEED=?,FREEDEEDS=?,TRADERMAX=?,TRADERINIT=?,BREEDING=?,FIELDGROWTH=?,KINGSMONEY=?, MOTD=?,     TUNNELING=?,SKILLBODYCONTROLSTART=? WHERE SERVER=?"
         );
         ps.setInt(1, this.id);
         ps.setString(2, this.name);
         ps.setInt(3, this.maxCreatures);
         ps.setInt(4, this.pLimit);
         ps.setFloat(5, this.percentAggCreatures);
         ps.setInt(6, this.treeGrowth);
         ps.setFloat(7, this.skillGainRate);
         ps.setFloat(8, this.actionTimer);
         ps.setInt(9, this.hotaDelay);
         ps.setBoolean(10, this.PVPSERVER);
         ps.setBoolean(11, this.HOMESERVER);
         ps.setByte(12, this.KINGDOM);
         ps.setString(13, this.INTRASERVERPASSWORD);
         ps.setString(14, this.EXTERNALIP);
         ps.setString(15, this.EXTERNALPORT);
         ps.setString(16, this.INTRASERVERADDRESS);
         ps.setString(17, this.INTRASERVERPORT);
         ps.setBoolean(18, this.testServer);
         ps.setBoolean(19, this.ISPAYMENT);
         ps.setBoolean(20, this.LOGINSERVER);
         ps.setString(21, String.valueOf(this.RMI_PORT));
         ps.setString(22, String.valueOf(this.REGISTRATION_PORT));
         ps.setBoolean(23, this.isLocal);
         ps.setBoolean(24, this.randomSpawns);
         ps.setFloat(25, this.getSkillbasicval());
         ps.setFloat(26, this.getSkillmindval());
         ps.setFloat(27, this.getSkillfightval());
         ps.setFloat(28, this.getSkilloverallval());
         ps.setBoolean(29, this.EPIC);
         ps.setFloat(30, this.getCombatRatingModifier());
         ps.setString(31, this.getSteamServerPassword());
         ps.setBoolean(32, this.isUpkeep());
         ps.setInt(33, this.getMaxDeedSize());
         ps.setBoolean(34, this.isFreeDeeds());
         ps.setInt(35, this.getTraderMaxIrons());
         ps.setInt(36, this.getInitialTraderIrons());
         ps.setLong(37, this.getBreedingTimer());
         ps.setLong(38, this.getFieldGrowthTime());
         ps.setInt(39, this.getKingsmoneyAtRestart());
         ps.setString(40, this.motd);
         ps.setInt(41, this.getTunnelingHits());
         ps.setFloat(42, this.getSkillbcval());
         ps.setInt(43, oldId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to save new stuff from gui or command line", (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void saveTimers() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE SERVERS SET SKILLDAYSWITCH=?,SKILLWEEKSWITCH=?,NEXTEPICPOLL=?,FATIGUESWITCH=?,NEXTHOTA=?,WORLDTIME=?,TILEREST=?,POLLTILE=?,POLLMOD=?,POLLROUND=? WHERE SERVER=?"
         );
         ps.setLong(1, this.getSkillDaySwitch());
         ps.setLong(2, this.getSkillWeekSwitch());
         ps.setLong(3, this.getNextEpicPoll());
         ps.setLong(4, this.getFatigueSwitch());
         ps.setLong(5, this.getNextHota());
         ps.setLong(6, WurmCalendar.getCurrentTime());
         ps.setInt(7, TilePoller.rest);
         ps.setInt(8, TilePoller.currentPollTile);
         ps.setInt(9, TilePoller.pollModifier);
         ps.setInt(10, TilePoller.pollround);
         ps.setInt(11, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to set time stamps for localserver ", (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public int getCurrentPlayersForSort() {
      return this.id == 3 ? 1000 : this.currentPlayers;
   }

   public int compareTo(ServerEntry entry) {
      return this.getCurrentPlayersForSort() - entry.getCurrentPlayersForSort();
   }

   public String getAbbreviation() {
      return this.testServer ? this.name.substring(0, 2) + this.name.substring(this.name.length() - 1) : this.name.substring(0, 3);
   }

   public long getMovedArtifacts() {
      return this.movedArtifacts;
   }

   public void setMovedArtifacts(long aMovedArtifacts) {
      this.movedArtifacts = aMovedArtifacts;
   }

   public long getSkillDaySwitch() {
      return this.skillDaySwitch;
   }

   public void setSkillDaySwitch(long aSkillDaySwitch) {
      this.skillDaySwitch = aSkillDaySwitch;
   }

   public long getSkillWeekSwitch() {
      return this.skillWeekSwitch;
   }

   public void setSkillWeekSwitch(long aSkillWeekSwitch) {
      this.skillWeekSwitch = aSkillWeekSwitch;
   }

   public long getNextEpicPoll() {
      return this.nextEpicPoll;
   }

   public void setNextEpicPoll(long aNextEpicPoll) {
      this.nextEpicPoll = aNextEpicPoll;
   }

   public long getFatigueSwitch() {
      return this.fatigueSwitch;
   }

   public void setFatigueSwitch(long aFatigueSwitch) {
      this.fatigueSwitch = aFatigueSwitch;
   }

   public long getNextHota() {
      return this.nextHota;
   }

   public void setNextHota(long aNextHota) {
      this.nextHota = aNextHota;
   }

   public Spawnpoint[] getSpawns() {
      return this.spawns;
   }

   public void setSpawns(Spawnpoint[] aSpawns) {
      this.spawns = aSpawns;
   }

   public void updateSpawns() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE SERVERS SET SPAWNPOINTJENNX=?,SPAWNPOINTJENNY=?,SPAWNPOINTLIBX=?,SPAWNPOINTLIBY=?,SPAWNPOINTMOLX=?,SPAWNPOINTMOLY=? WHERE SERVER=?"
         );
         ps.setInt(1, this.SPAWNPOINTJENNX);
         ps.setInt(2, this.SPAWNPOINTJENNY);
         ps.setInt(3, this.SPAWNPOINTLIBX);
         ps.setInt(4, this.SPAWNPOINTLIBY);
         ps.setInt(5, this.SPAWNPOINTMOLX);
         ps.setInt(6, this.SPAWNPOINTMOLY);
         ps.setInt(7, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to update spawnpoints." + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public long getChallengeStarted() {
      return this.challengeStarted;
   }

   public void setChallengeStarted(long challengeStarted) {
      this.challengeStarted = challengeStarted;
   }

   public long getChallengeEnds() {
      return this.challengeEnds;
   }

   public void setChallengeEnds(long challengeEnds) {
      this.challengeEnds = challengeEnds;
   }

   public float getSkillGainRate() {
      return this.skillGainRate;
   }

   public void setSkillGainRate(float skillGainRate) {
      this.skillGainRate = skillGainRate;
   }

   public float getActionTimer() {
      return this.actionTimer;
   }

   public void setActionTimer(float actionTimer) {
      this.actionTimer = actionTimer;
   }

   public int getHotaDelay() {
      return this.hotaDelay;
   }

   public void setHotaDelay(int hotaDelay) {
      this.hotaDelay = hotaDelay;
   }

   public float getSkillfightval() {
      return this.skillfightval;
   }

   public void setSkillfightval(float skillfightval) {
      this.skillfightval = skillfightval;
   }

   public float getSkillbasicval() {
      return this.skillbasicval;
   }

   public void setSkillbasicval(float skillbasicval) {
      this.skillbasicval = skillbasicval;
   }

   public float getSkillmindval() {
      return this.skillmindval;
   }

   public void setSkillmindval(float skillmindval) {
      this.skillmindval = skillmindval;
   }

   public float getSkilloverallval() {
      return this.skilloverallval;
   }

   public void setSkilloverallval(float skilloverallval) {
      this.skilloverallval = skilloverallval;
   }

   public float getCombatRatingModifier() {
      return this.combatRatingModifier;
   }

   public void setCombatRatingModifier(float combatRatingModifier) {
      this.combatRatingModifier = combatRatingModifier;
   }

   public String getSteamServerPassword() {
      return this.steamServerPassword;
   }

   public void setSteamServerPassword(String steamServerPassword) {
      this.steamServerPassword = steamServerPassword;
   }

   public boolean isUpkeep() {
      return this.upkeep;
   }

   public void setUpkeep(boolean upkeep) {
      this.upkeep = upkeep;
   }

   public boolean isFreeDeeds() {
      return this.freeDeeds;
   }

   public void setFreeDeeds(boolean freeDeeds) {
      this.freeDeeds = freeDeeds;
   }

   public int getMaxDeedSize() {
      return this.maxDeedSize;
   }

   public void setMaxDeedSize(int maxDeedSize) {
      this.maxDeedSize = maxDeedSize;
   }

   public int getTraderMaxIrons() {
      return this.traderMaxIrons;
   }

   public void setTraderMaxIrons(int traderMaxIrons) {
      this.traderMaxIrons = traderMaxIrons;
   }

   public int getInitialTraderIrons() {
      return this.initialTraderIrons;
   }

   public void setInitialTraderIrons(int initialTraderIrons) {
      this.initialTraderIrons = initialTraderIrons;
   }

   public int getTunnelingHits() {
      return this.tunnelingHits;
   }

   public void setTunnelingHits(int tunnelingHits) {
      this.tunnelingHits = tunnelingHits;
   }

   public long getBreedingTimer() {
      return this.breedingTimer;
   }

   public void setBreedingTimer(long breedingTimer) {
      this.breedingTimer = breedingTimer;
   }

   public long getFieldGrowthTime() {
      return this.fieldGrowthTime;
   }

   public void setFieldGrowthTime(long fieldGrowthTime) {
      this.fieldGrowthTime = fieldGrowthTime;
   }

   public int getKingsmoneyAtRestart() {
      return this.kingsmoneyAtRestart;
   }

   public void setKingsmoneyAtRestart(int kingsmoneyAtRestart) {
      this.kingsmoneyAtRestart = kingsmoneyAtRestart;
   }

   public String getMotd() {
      return this.motd;
   }

   public final boolean hasMotd() {
      return this.getMotd() != null && this.getMotd().length() > 0;
   }

   public void setMotd(String nmotd) {
      this.motd = nmotd;
      if (this.hasMotd()) {
         Constants.motd = this.motd;
      }
   }

   public float getSkillbcval() {
      return this.skillbcval;
   }

   public void setSkillbcval(float skillbcval) {
      this.skillbcval = skillbcval;
   }
}
