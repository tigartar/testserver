package com.wurmonline.server;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Artist;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.DbSearcher;
import com.wurmonline.server.players.IPBan;
import com.wurmonline.server.players.KingdomIp;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerKills;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.players.SteamIdBan;
import com.wurmonline.server.players.TabData;
import com.wurmonline.server.players.WurmRecord;
import com.wurmonline.server.questions.KosWarningInfo;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.KosWarning;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WCGmMessage;
import com.wurmonline.server.webinterface.WcDemotion;
import com.wurmonline.server.webinterface.WcTabLists;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.EffectConstants;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.website.StatsXMLWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Players implements MiscConstants, CreatureTemplateIds, EffectConstants, MonetaryConstants, TimeConstants {
   private static Map<String, Player> players = new ConcurrentHashMap<>();
   private static Map<Long, Player> playersById = new ConcurrentHashMap<>();
   private final Map<Long, Byte> pkingdoms = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<String, TabData> tabListGM = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<String, TabData> tabListMGMT = new ConcurrentHashMap<>();
   private static Players instance = null;
   private static Logger logger = Logger.getLogger(Players.class.getName());
   private static final String DOES_PLAYER_NAME_EXIST = "SELECT WURMID FROM PLAYERS WHERE NAME=?";
   private static final Map<Long, Artist> artists = new HashMap<>();
   private static final String GET_ARTISTS = "SELECT * FROM ARTISTS";
   private static final String SET_ARTISTS = "INSERT INTO ARTISTS (WURMID,SOUND,GRAPHICS) VALUES(?,?,?)";
   private static final String DELETE_ARTIST = "DELETE FROM ARTISTS WHERE WURMID=?";
   private static final String GET_PLAYERS_BANNED = "SELECT NAME,BANREASON,BANEXPIRY FROM PLAYERS WHERE BANNED=1";
   private static final String SET_NOSTRUCTURE = "update PLAYERS set BUILDINGID=-10 WHERE BUILDINGID=?";
   private static final String GET_LASTLOGOUT = "SELECT LASTLOGOUT FROM PLAYERS WHERE WURMID=?";
   private static final String GET_KINGDOM = "SELECT KINGDOM FROM PLAYERS WHERE WURMID=?";
   private static final String GET_KINGDOM_PLAYERS = "SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND CURRENTSERVER=? AND POWER=0";
   private static final String GET_PREMIUM_KINGDOM_PLAYERS = "SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND PAYMENTEXPIRE>? AND POWER=0";
   private static final String GET_CHAMPION_KINGDOM_PLAYERS = "SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0";
   private static final String RESET_FAITHGAIN = "UPDATE PLAYERS SET LASTFAITH=0,NUMFAITH=0";
   private static final String GM_SALARY = "UPDATE PLAYERS SET MONEY=MONEY+250000 WHERE POWER>1";
   private static final String RESET_PLAYER_SKILLS = "UPDATE SKILLS SET VALUE=20, MINVALUE=20 WHERE VALUE>20 AND OWNER=?";
   private static final String RESET_PLAYER_FAITH = "UPDATE PLAYERS SET FAITH=20 WHERE FAITH>20 AND WURMID=?";
   private static final String ADD_GM_MESSAGE = "INSERT INTO GMMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)";
   private static final String ADD_MGMT_MESSAGE = "INSERT INTO MGMTMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)";
   private static final String GET_GM_MESSAGES = "SELECT TIME,SENDER,MESSAGE FROM GMMESSAGES ORDER BY TIME";
   private static final String GET_MGMT_MESSAGES = "SELECT TIME,SENDER,MESSAGE FROM MGMTMESSAGES ORDER BY TIME";
   private static final String PRUNE_GM_MESSAGES = "DELETE FROM GMMESSAGES WHERE TIME<?";
   private static final String PRUNE_MGMT_MESSAGES = "DELETE FROM MGMTMESSAGES WHERE TIME<?";
   private static final String GET_BATTLE_RANKS = "select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?";
   private static final String GET_MAXBATTLE_RANKS = "select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?";
   private static final String GET_FRIENDS = "select p.NAME,p.WURMID from PLAYERS p INNER JOIN FRIENDS f ON f.FRIEND=p.WURMID WHERE f.WURMID=? ORDER BY NAME";
   private static final String GET_PLAYERID_BY_NAME = "SELECT WURMID FROM PLAYERS WHERE NAME=?";
   private static final String GET_PLAYERS_MUTED = "SELECT NAME,MUTEREASON,MUTEEXPIRY FROM PLAYERS WHERE MUTED=1";
   private static final String GET_MUTERS = "SELECT NAME FROM PLAYERS WHERE MAYMUTE=1";
   private static final String GET_DEVTALKERS = "SELECT NAME FROM PLAYERS WHERE DEVTALK=1";
   private static final String GET_CAS = "SELECT NAME FROM PLAYERS WHERE PA=1";
   private static final String GET_HEROS = "SELECT NAME FROM PLAYERS WHERE POWER=? AND CURRENTSERVER=?";
   private static final String GET_PRIVATE_MAP_POI = "SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=0 AND OWNERID=?";
   private static final String CHANGE_KINGDOM = "UPDATE PLAYERS SET KINGDOM=? WHERE KINGDOM=?";
   private static final String CHANGE_KINGDOM_FOR_PLAYER = "UPDATE PLAYERS SET KINGDOM=? WHERE WURMID=?";
   public static final String CACHAN = "CA HELP";
   public static final String GVCHAN = "GV HELP";
   public static final String JKCHAN = "JK HELP";
   public static final String MRCHAN = "MR HELP";
   public static final String HOTSCHAN = "HOTS HELP";
   private static final String CAPREFIX = " CA ";
   private static final Map<String, Logger> loggers = new HashMap<>();
   private static Set<Ban> bans = new HashSet<>();
   private static final Map<Long, PlayerKills> playerKills = new ConcurrentHashMap<>();
   private final Map<Byte, Float> crBonuses = new HashMap<>();
   private boolean shouldSendWeather = false;
   private final long timeBetweenChampDecreases = 604800000L;
   private static ConcurrentLinkedQueue<KosWarning> kosList = new ConcurrentLinkedQueue<>();
   private static String header = "<HTML> <HEAD><TITLE>Wurm battle ranks</TITLE></HEAD><BODY><BR><BR>";
   private long lastPoll = System.currentTimeMillis();
   private static final float minDelta = 0.095F;
   private static boolean pollCheckClients = false;
   private long lastCheckClients = System.currentTimeMillis();
   private static int challengeStep = 0;
   private static HashMap<Long, Short> deathCount = new HashMap<>();
   private static final Logger caHelpLogger = Logger.getLogger("ca-help");
   private static String header2 = "<HTML>\n\t<HEAD>\n\t<TITLE>Wurm Online battle ranks</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
   private static final String footer2 = "\n</BODY>\n</HTML>";
   private static String headerStats = "<!DOCTYPE html> <HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> <TITLE>Wurm Online Server Stats</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
   private static String headerStats2 = "<!DOCTYPE html> <HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"> <TITLE>Wurm Online Champion Eternal Records</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
   private static final String footerStats = "\n</BODY>\n</HTML>";

   public static Players getInstance() {
      if (instance == null) {
         instance = new Players();
      }

      return instance;
   }

   static synchronized Players getInstanceForUnitTestingWithoutDatabase() {
      if (instance == null) {
         instance = new Players(true);
      }

      return instance;
   }

   public void setCreatureDead(Creature dead) {
      long deadid = dead.getWurmId();
      Player[] plays = getInstance().getPlayers();

      for(Player player : plays) {
         if (player.opponent == dead) {
            player.setOpponent(null);
         }

         if (player.target == deadid) {
            player.setTarget(-10L, true);
         }

         player.removeTarget(deadid);
      }

      Vehicles.removeDragger(dead);
   }

   private Players() {
      this.loadBannedIps();
      this.loadBannedSteamIds();
      header = getBattleRanksHtmlHeader();
   }

   private Players(boolean forUnitTestingWithoutDatabase) {
      if (forUnitTestingWithoutDatabase) {
         logger.warning("Instantiating Players for Unit Test without a database");
      } else {
         this.loadBannedIps();
         this.loadBannedSteamIds();
      }
   }

   static String getBattleRanksHtmlHeader() {
      return "<HTML> <HEAD><TITLE>Wurm battle ranks on " + Servers.getLocalServerName() + "</TITLE></HEAD><BODY><BR><BR>";
   }

   public int numberOfPlayers() {
      return players.size();
   }

   public int numberOfPremiumPlayers() {
      int x = 0;

      for(Player lPlayer : getInstance().getPlayers()) {
         if (lPlayer.isPaying() && lPlayer.getPower() == 0) {
            ++x;
         }
      }

      return x;
   }

   public void weatherFlash(int tilex, int tiley, float height) {
      for(Player p : getInstance().getPlayers()) {
         if (p != null) {
            Communicator lPlayerCommunicator = p.getCommunicator();
            if (lPlayerCommunicator != null) {
               lPlayerCommunicator.sendAddEffect(9223372036854775707L, (short)1, (float)(tilex << 2), (float)(tiley << 2), height, (byte)0);
            }
         }
      }
   }

   public void sendGlobalNonPersistantComplexEffect(
      long target,
      short effect,
      int tilex,
      int tiley,
      float height,
      float radiusMeters,
      float lengthMeters,
      int direction,
      byte kingdomTemplateId,
      byte epicEntityId
   ) {
      long effectId = Long.MAX_VALUE - (long)Server.rand.nextInt(1000);

      for(Player p : getInstance().getPlayers()) {
         if (p != null) {
            Communicator lPlayerCommunicator = p.getCommunicator();
            if (lPlayerCommunicator != null) {
               lPlayerCommunicator.sendAddComplexEffect(
                  effectId,
                  target,
                  effect,
                  (float)(tilex << 2),
                  (float)(tiley << 2),
                  height,
                  (byte)0,
                  radiusMeters,
                  lengthMeters,
                  direction,
                  kingdomTemplateId,
                  epicEntityId
               );
            }
         }
      }
   }

   public void sendGlobalNonPersistantEffect(long id, short effect, int tilex, int tiley, float height) {
      long effectId = Long.MAX_VALUE - (long)Server.rand.nextInt(1000);

      for(Player p : getInstance().getPlayers()) {
         if (p != null) {
            Communicator lPlayerCommunicator = p.getCommunicator();
            if (lPlayerCommunicator != null) {
               lPlayerCommunicator.sendAddEffect(id <= 0L ? effectId : id, effect, (float)(tilex << 2), (float)(tiley << 2), height, (byte)0);
            }
         }
      }
   }

   public void sendGlobalNonPersistantTimedEffect(long id, short effect, int tilex, int tiley, float height, long expireTime) {
      long effectId = id <= 0L ? Long.MAX_VALUE - (long)Server.rand.nextInt(10000) : id;
      Server.getInstance().addGlobalTempEffect(effectId, expireTime);

      for(Player p : getInstance().getPlayers()) {
         if (p != null) {
            Communicator lPlayerCommunicator = p.getCommunicator();
            if (lPlayerCommunicator != null) {
               lPlayerCommunicator.sendAddEffect(effectId, effect, (float)(tilex << 2), (float)(tiley << 2), height, (byte)0);
            }
         }
      }
   }

   public final int getChallengeStep() {
      return challengeStep;
   }

   public final void setChallengeStep(int step) {
      if (Servers.localServer.isChallengeServer() || Servers.localServer.testServer) {
         challengeStep = step;
         byte toSend = 0;
         switch(challengeStep) {
            case 1:
               toSend = 20;
               break;
            case 2:
               toSend = 21;
               break;
            case 3:
               toSend = 22;
               break;
            case 4:
               toSend = 23;
               break;
            default:
               toSend = 0;
         }

         if (toSend > 0) {
            this.sendGlobalNonPersistantEffect(Long.MAX_VALUE - (long)Server.rand.nextInt(100000), (short)toSend, 0, 0, 0.0F);
         }
      }
   }

   public void sendPlayerStatus(Player player) {
      for(Player p : getInstance().getPlayers()) {
         player.getCommunicator().sendNormalServerMessage(p.getName() + ", secstolog=" + p.getSecondsToLogout() + ", logged off=" + p.loggedout);
      }
   }

   public int getOnlinePlayersFromKingdom(byte kingdomId) {
      int nums = 0;

      for(Player lPlayer : getInstance().getPlayers()) {
         if (lPlayer.getKingdomId() == kingdomId) {
            ++nums;
         }
      }

      return nums;
   }

   public Player[] getPlayersByIp() {
      Player[] playerArr = this.getPlayers();
      Arrays.sort(playerArr, new Comparator<Player>() {
         public int compare(Player o1, Player o2) {
            return o1.getSaveFile().getIpaddress().compareTo(o2.getSaveFile().getIpaddress());
         }
      });
      return playerArr;
   }

   public void sendIpsToPlayer(Player player) {
      Player[] playerArr = this.getPlayersByIp();

      for(Player lPlayer : playerArr) {
         if (lPlayer.getPower() <= player.getPower() && player.getPower() > 1) {
            player.getCommunicator().sendNormalServerMessage(lPlayer.getName() + " IP: " + lPlayer.getSaveFile().getIpaddress());
         }
      }

      player.getCommunicator().sendNormalServerMessage(playerArr.length + " players logged on.");
   }

   public void sendIpsToPlayer(Player player, String playername) {
      PlayerInfo pinfo = null;

      try {
         pinfo = this.getPlayer(playername).getSaveFile();
      } catch (NoSuchPlayerException var12) {
         pinfo = PlayerInfoFactory.createPlayerInfo(playername);

         try {
            pinfo.load();
         } catch (IOException var11) {
            logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         }
      }

      if (pinfo != null) {
         if (pinfo.getPower() <= player.getPower() && player.getPower() > 1) {
            Player[] playerArr = this.getPlayersByIp();
            Map<String, String> ps = new HashMap<>();
            boolean error = false;
            ps.put(playername, pinfo.getIpaddress());

            for(Player lPlayer : playerArr) {
               if (lPlayer.getSaveFile().getIpaddress().equals(pinfo.getIpaddress())) {
                  ps.put(lPlayer.getName(), lPlayer.getSaveFile().getIpaddress());
               }
            }

            for(String name : ps.keySet()) {
               String ip = ps.get(name);
               player.getCommunicator().sendNormalServerMessage(name + ", " + ip);
            }
         } else {
            player.getCommunicator().sendNormalServerMessage("You may not check that player's ip.");
         }
      } else {
         player.getCommunicator().sendNormalServerMessage(playername + " - not found!");
      }
   }

   public static void stopLoggers() {
      for(Logger logger : loggers.values()) {
         if (logger != null) {
            for(Handler h : logger.getHandlers()) {
               h.close();
            }
         }
      }
   }

   public static Logger getLogger(Player player) {
      if (player.getPower() <= 0 && !player.isLogged() && !isArtist(player.getWurmId(), false, false)) {
         return null;
      } else {
         String name = player.getName();
         Logger personalLogger = loggers.get(name);
         if (personalLogger == null) {
            personalLogger = Logger.getLogger(name);
            personalLogger.setUseParentHandlers(false);
            Handler[] h = logger.getHandlers();

            for(int i = 0; i != h.length; ++i) {
               personalLogger.removeHandler(h[i]);
            }

            try {
               FileHandler fh = new FileHandler(name + ".log", 0, 1, true);
               fh.setFormatter(new SimpleFormatter());
               personalLogger.addHandler(fh);
            } catch (IOException var5) {
               Logger.getLogger(name).log(Level.WARNING, name + ":no redirection possible!");
            }

            loggers.put(name, personalLogger);
         }

         return personalLogger;
      }
   }

   public Player getPlayer(String name) throws NoSuchPlayerException {
      Player p = this.getPlayerByName(LoginHandler.raiseFirstLetter(name));
      if (p == null) {
         throw new NoSuchPlayerException(name);
      } else {
         return p;
      }
   }

   private Player getPlayerByName(String aName) {
      return players.get(aName);
   }

   public Player getPlayerOrNull(String aName) {
      return this.getPlayerByName(aName);
   }

   public Optional<Player> getPlayerOptional(String aName) {
      return Optional.ofNullable(this.getPlayerByName(aName));
   }

   public Player getPlayer(long id) throws NoSuchPlayerException {
      Player p = this.getPlayerById(id);
      if (p != null) {
         return p;
      } else {
         throw new NoSuchPlayerException("Player with id " + id + " could not be found.");
      }
   }

   public Player getPlayerOrNull(long id) {
      return this.getPlayerById(id);
   }

   public Optional<Player> getPlayerOptional(long id) {
      return Optional.ofNullable(this.getPlayerById(id));
   }

   public Player getPlayer(Long id) throws NoSuchPlayerException {
      Player p = this.getPlayerById(id);
      if (p != null) {
         return p;
      } else {
         throw new NoSuchPlayerException("Player with id " + id + " could not be found.");
      }
   }

   private Player getPlayerById(long aWurmID) {
      return this.getPlayerById(new Long(aWurmID));
   }

   private Player getPlayerById(Long aWurmID) {
      return playersById.get(aWurmID);
   }

   public Player getPlayer(SocketConnection serverConnection) throws NoSuchPlayerException {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         try {
            if (serverConnection == lPlayer.getCommunicator().getConnection()) {
               return lPlayer;
            }
         } catch (NullPointerException var8) {
            if (lPlayer == null) {
               logger.log(Level.WARNING, "A player in the Players list is null. this shouldn't happen.");
            } else if (lPlayer.getCommunicator() == null) {
               logger.log(Level.WARNING, lPlayer + "'s communicator is null.");
            } else {
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            }
         }
      }

      throw new NoSuchPlayerException("Player could not be found.");
   }

   void sendReconnect(Player player) {
      if (!player.isUndead()) {
         player.getCommunicator().sendClearFriendsList();
         this.sendConnectInfo(player, " reconnected.", player.getLastLogin(), PlayerOnlineStatus.ONLINE);
      }
   }

   void sendAddPlayer(Player player) {
      if (!player.isUndead()) {
         this.sendConnectInfo(player, " joined.", player.getLastLogin(), PlayerOnlineStatus.ONLINE);
      }
   }

   public void sendConnectAlert(String message) {
      Player[] playerArr = this.getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.getPower() > 0) {
            lPlayer.getCommunicator().sendAlertServerMessage(message);
         }
      }
   }

   public void sendConnectInfo(Player player, String message, long whenStateChanged, PlayerOnlineStatus loginstatus) {
      this.sendConnectInfo(player, message, whenStateChanged, loginstatus, false);
   }

   public void sendConnectInfo(Player player, String message, long whenStateChanged, PlayerOnlineStatus loginstatus, boolean loggedin) {
      PlayerInfoFactory.updatePlayerState(player, whenStateChanged, loginstatus);
      if (!player.isUndead()) {
         Player[] playerArr = this.getPlayers();
         int tilex = player.getTileX();
         int tiley = player.getTileY();
         int tilez = (int)(player.getPositionZ() + player.getAltOffZ()) >> 2;
         int vision = 80;

         try {
            vision = CreatureTemplateFactory.getInstance().getTemplate(1).getVision();
         } catch (NoSuchCreatureTemplateException var19) {
            logger.log(Level.WARNING, "Failed to find HUMAN_CID. Vision set to " + vision);
         }

         Village village = player.getCitizenVillage();

         for(Player lPlayer : playerArr) {
            if (player != lPlayer) {
               if (lPlayer.getPower() > 1) {
                  if (player.getCommunicator() != null && player.getCommunicator().getConnection() != null && lPlayer.getPower() > player.getPower()) {
                     try {
                        lPlayer.getCommunicator()
                           .sendSystemMessage(player.getName() + "[" + player.getCommunicator().getConnection().getIp() + "] " + message);
                     } catch (Exception var18) {
                        lPlayer.getCommunicator().sendSystemMessage(player.getName() + message);
                     }
                  }
               } else if (player.isVisibleTo(lPlayer) && (!loggedin || player.getPower() <= 1) && !lPlayer.isFriend(player.getWurmId())) {
                  if (village != null && lPlayer.getCitizenVillage() == village) {
                     lPlayer.getCommunicator().sendSafeServerMessage(player.getName() + message);
                  } else if (lPlayer.isOnSurface() == player.isOnSurface() && lPlayer.isWithinTileDistanceTo(tilex, tiley, tilez, vision)) {
                     lPlayer.getCommunicator().sendSafeServerMessage(player.getName() + message);
                  }
               }

               if (lPlayer.seesPlayerAssistantWindow() && player.seesPlayerAssistantWindow()) {
                  if (player.isVisibleTo(lPlayer)) {
                     if (player.isPlayerAssistant()) {
                        lPlayer.getCommunicator().sendAddPa(" CA " + player.getName(), player.getWurmId());
                     } else if (this.shouldReceivePlayerList(lPlayer)) {
                        lPlayer.getCommunicator().sendAddPa(player.getName(), player.getWurmId());
                     }
                  }

                  if (lPlayer.isVisibleTo(player)) {
                     if (lPlayer.isPlayerAssistant()) {
                        player.getCommunicator().sendAddPa(" CA " + lPlayer.getName(), lPlayer.getWurmId());
                     } else if (this.shouldReceivePlayerList(player)) {
                        player.getCommunicator().sendAddPa(lPlayer.getName(), lPlayer.getWurmId());
                     }
                  }
               }
            }
         }
      }
   }

   public void combatRound() {
      for(Player lPlayer : getInstance().getPlayers()) {
         lPlayer.getCombatHandler().clearRound();
      }
   }

   public void pollKosWarnings() {
      for(KosWarning kos : kosList) {
         try {
            Player p = this.getPlayer(kos.playerId);
            if (p.isFullyLoaded() && p.getVisionArea() != null && p.getVisionArea().isInitialized() && p.hasLink()) {
               if (kos.getTick() < 10) {
                  if (kos.tick() == 10) {
                     if (p.acceptsKosPopups(kos.village.getId())) {
                        KosWarningInfo kwi = new KosWarningInfo(p, kos.playerId, kos.village);
                        kwi.sendQuestion();
                     } else {
                        p.getCommunicator().sendAlertServerMessage("You are being put on the KOS list of " + kos.village.getName() + " again.", (byte)4);
                     }
                  }
               } else if (kos.getTick() % 30 == 0 && p.acceptsKosPopups(kos.village.getId())) {
                  if (p.getCurrentVillage() == kos.village) {
                     p.getCommunicator()
                        .sendAlertServerMessage(
                           "You must leave the settlement of " + kos.village.getName() + " immediately or you will be attacked by the guards!", (byte)4
                        );
                  } else {
                     p.getCommunicator()
                        .sendAlertServerMessage(
                           "Make sure to stay out of " + kos.village.getName() + " since you soon will be killed on sight there!", (byte)4
                        );
                  }
               }

               if (kos.getTick() >= 130 && p.acceptsKosPopups(kos.village.getId())) {
                  p.getCommunicator().sendAlertServerMessage("You will now be killed on sight in " + kos.village.getName() + "!", (byte)4);
               }
            }
         } catch (NoSuchPlayerException var5) {
         }

         PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(kos.playerId);
         if (pinf != null) {
            if (kos.getTick() >= 10) {
               kos.tick();
               if (kos.getTick() >= 130) {
                  Reputation r = kos.village.setReputation(kos.playerId, kos.newReputation, false, true);
                  r.setPermanent(kos.permanent);
                  kos.village.addHistory(pinf.getName(), "will now be killed on sight.");
                  kosList.remove(kos);
               }
            }
         } else {
            kosList.remove(kos);
         }
      }
   }

   public final boolean addKosWarning(KosWarning newkos) {
      for(KosWarning kosw : kosList) {
         if (kosw.playerId == newkos.playerId) {
            return false;
         }
      }

      kosList.add(newkos);
      return true;
   }

   public final boolean removeKosFor(long wurmId) {
      for(KosWarning kos : kosList) {
         if (kos.playerId == wurmId) {
            kosList.remove(kos);
            return true;
         }
      }

      return false;
   }

   public void pollDeadPlayers() {
      Player[] playerarr = this.getPlayers();

      for(Player lPlayer : playerarr) {
         if (lPlayer != null && lPlayer.getSaveFile() != null && lPlayer.pollDead()) {
            logger.log(Level.INFO, "Removing from players " + lPlayer.getName() + ".");
            players.remove(lPlayer.getName());
         }
      }
   }

   public void broadCastMissionInfo(String missionInfo, int missionRelated) {
      Player[] playarr = this.getPlayers();

      for(int x = 0; x < playarr.length; ++x) {
         MissionPerformer mp = MissionPerformed.getMissionPerformer(playarr[x].getWurmId());
         if (mp != null) {
            MissionPerformed m = mp.getMission(missionRelated);
            if (m != null) {
               playarr[x].getCommunicator().sendSafeServerMessage(missionInfo);
            }
         }
      }
   }

   public final void broadCastConquerInfo(Creature conquerer, String info) {
      Player[] playarr = this.getPlayers();

      for(int x = 0; x < playarr.length; ++x) {
         int r = 200;
         int g = 200;
         int b = 25;
         if (conquerer.isFriendlyKingdom(playarr[x].getKingdomId())) {
            r = 25;
         } else {
            g = 25;
         }

         playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)25);
      }
   }

   public final void broadCastDestroyInfo(Creature performer, String info) {
      Player[] playarr = getInstance().getPlayers();

      for(int x = 0; x < playarr.length; ++x) {
         int r = 200;
         int g = 200;
         int b = 25;
         if (performer.getKingdomId() == playarr[x].getKingdomId()) {
            r = 25;
         } else {
            g = 25;
         }

         playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)25);
      }
   }

   public final void broadCastBashInfo(Item target, String info) {
      Player[] playarr = getInstance().getPlayers();

      for(int x = 0; x < playarr.length; ++x) {
         if (target.getKingdom() == playarr[x].getKingdomId()) {
            int r = 200;
            int g = 25;
            int b = 25;
            playarr[x].getCommunicator().sendDeathServerMessage(info, (byte)r, (byte)g, (byte)b);
         }
      }
   }

   public final void broadCastDeathInfo(Player player, String slayers) {
      if (Servers.isThisAPvpServer()) {
         String toSend = player.getName() + " slain by " + slayers;
         Player[] playarr = this.getPlayers();

         for(int x = 0; x < playarr.length; ++x) {
            int r = 200;
            int g = 200;
            int b = 25;
            if (player.isFriendlyKingdom(playarr[x].getKingdomId())) {
               r = 25;
            } else {
               g = 25;
            }

            playarr[x].getCommunicator().sendDeathServerMessage(toSend, (byte)r, (byte)g, (byte)25);
         }
      } else if (Features.Feature.PVE_DEATHTABS.isEnabled()) {
         String toSend = player.getName() + " slain by " + slayers;

         for(Player p : getInstance().getPlayers()) {
            if (!p.hasFlag(60)) {
               p.getCommunicator().sendDeathServerMessage(toSend, (byte)25, (byte)-56, (byte)25);
            }
         }
      }
   }

   public final void sendAddToAlliance(Creature player, Village village) {
      if (village != null) {
         Player[] playerArr = this.getPlayers();

         for(Player lPlayer : playerArr) {
            if (player != lPlayer
               && player.isVisibleTo(lPlayer)
               && lPlayer.getCitizenVillage() != null
               && village.getAllianceNumber() > 0
               && village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()) {
               lPlayer.getCommunicator().sendAddAlly(player.getName(), player.getWurmId());
               player.getCommunicator().sendAddAlly(lPlayer.getName(), lPlayer.getWurmId());
            }
         }
      }
   }

   public final void sendRemoveFromAlliance(Creature player, Village village) {
      if (village != null) {
         Player[] playerArr = this.getPlayers();

         for(Player lPlayer : playerArr) {
            if (player != lPlayer
               && lPlayer.getCitizenVillage() != null
               && village.getAllianceNumber() > 0
               && village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()) {
               lPlayer.getCommunicator().sendRemoveAlly(player.getName());
               player.getCommunicator().sendRemoveAlly(lPlayer.getName());
            }
         }
      }
   }

   public void addToGroups(Player player) {
      if (!player.isUndead()) {
         try {
            Groups.getGroup("wurm").addMember(player.getName(), player);
         } catch (NoSuchGroupException var9) {
            logger.log(Level.WARNING, "Could not get group for Group 'wurm', Player: " + player + " due to " + var9.getMessage(), (Throwable)var9);
         }

         Village citvil = Villages.getVillageForCreature(player);
         player.setCitizenVillage(citvil);
         player.sendSkills();
         if (citvil != null) {
            try {
               citvil.setLogin();
               Groups.getGroup(citvil.getName()).addMember(player.getName(), player);
               if (citvil.getAllianceNumber() > 0) {
                  PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(citvil.getAllianceNumber());
                  if (pvpAll != null && !pvpAll.getMotd().isEmpty()) {
                     Message mess = pvpAll.getMotdMessage();
                     player.getCommunicator().sendMessage(mess);
                  } else {
                     Message mess = new Message(player, (byte)15, "Alliance", "");
                     player.getCommunicator().sendMessage(mess);
                  }
               }
            } catch (NoSuchGroupException var10) {
               logger.log(
                  Level.WARNING, "Could not get group for Village: " + citvil + ", Player: " + player + " due to " + var10.getMessage(), (Throwable)var10
               );
            }
         }

         Player[] playerArr = this.getPlayers();
         Village village = player.getCitizenVillage();
         if (village != null) {
            for(Player lPlayer : playerArr) {
               if (player != lPlayer && player.isVisibleTo(lPlayer, true)) {
                  if (lPlayer.getCitizenVillage() == village) {
                     lPlayer.getCommunicator().sendAddVillager(player.getName(), player.getWurmId());
                  }

                  if (lPlayer.getCitizenVillage() != null
                     && village.getAllianceNumber() > 0
                     && village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()) {
                     lPlayer.getCommunicator().sendAddAlly(player.getName(), player.getWurmId());
                     player.getCommunicator().sendAddAlly(lPlayer.getName(), lPlayer.getWurmId());
                  }
               }
            }
         }
      } else {
         player.sendSkills();
      }
   }

   private void removeFromGroups(Player player) {
      try {
         Groups.getGroup("wurm").dropMember(player.getName());
         if (player.getCitizenVillage() != null) {
            Groups.getGroup(player.getCitizenVillage().getName()).dropMember(player.getName());
         }
      } catch (NoSuchGroupException var8) {
         logger.log(
            Level.WARNING,
            "Could not get group for Village: " + player.getCitizenVillage() + ", Player: " + player + " due to " + var8.getMessage(),
            (Throwable)var8
         );
      }

      if (player.mayHearDevTalk() || player.mayHearMgmtTalk()) {
         this.removeFromTabs(player.getWurmId(), player.getName());
         this.sendRemoveFromTabs(player.getWurmId(), player.getName());
      }

      Village village = player.getCitizenVillage();
      Player[] playerArr = this.getPlayers();

      for(Player lPlayer : playerArr) {
         if (player != lPlayer) {
            if (village != null) {
               if (lPlayer.getCitizenVillage() == village) {
                  lPlayer.getCommunicator().sendRemoveVillager(player.getName());
               }

               if (lPlayer.getCitizenVillage() != null
                  && village.getAllianceNumber() > 0
                  && village.getAllianceNumber() == lPlayer.getCitizenVillage().getAllianceNumber()) {
                  lPlayer.getCommunicator().sendRemoveAlly(player.getName());
               }
            }

            if (player.seesPlayerAssistantWindow() && lPlayer.seesPlayerAssistantWindow()) {
               if (player.isPlayerAssistant()) {
                  lPlayer.getCommunicator().sendRemovePa(" CA " + player.getName());
               } else if (this.shouldReceivePlayerList(lPlayer)) {
                  lPlayer.getCommunicator().sendRemovePa(player.getName());
               }
            }
         }
      }
   }

   public void setShouldSendWeather(boolean shouldSend) {
      this.shouldSendWeather = shouldSend;
   }

   private boolean shouldSendWeather() {
      return this.shouldSendWeather;
   }

   public void checkSendWeather() {
      if (this.shouldSendWeather()) {
         this.sendWeather();
         this.setShouldSendWeather(false);
      }
   }

   public void sendWeather() {
      Player[] playerArr = this.getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer != null && lPlayer.getCommunicator() != null) {
            lPlayer.getCommunicator().sendWeather();
         }
      }
   }

   public Player logout(SocketConnection serverConnection) {
      Player player = null;
      String ip = "";
      if (serverConnection != null) {
         try {
            ip = serverConnection.getIp();
         } catch (Exception var9) {
         }

         try {
            serverConnection.disconnect();
         } catch (NullPointerException var8) {
         }
      }

      try {
         player = this.getPlayer(serverConnection);
         this.logoutPlayer(player);
      } catch (NoSuchPlayerException var7) {
         NoSuchPlayerException ex = var7;

         try {
            player = this.getPlayer(serverConnection);
            if (player != null) {
               if (ip.equals("")) {
                  ip = player.getSaveFile().getIpaddress();
               }

               this.removeFromGroups(player);
               players.remove(player.getName());
               playersById.remove(player.getWurmId());
               logger.log(Level.INFO, "Logout - " + ex.getMessage() + " please verify that player " + player.getName() + " is logged out.", (Throwable)ex);
            } else {
               logger.log(Level.INFO, "Logout - " + ex.getMessage(), (Throwable)ex);
            }
         } catch (NoSuchPlayerException var6) {
         }
      }

      if (Servers.localServer.PVPSERVER && !Servers.isThisATestServer() && !ip.isEmpty()) {
         KingdomIp kip = KingdomIp.getKIP(ip, (byte)0);
         if (kip != null) {
            kip.logoff();
         }
      }

      return player;
   }

   public void sendPAWindow(Player player) {
      String chan = getKingdomHelpChannelName(player.getKingdomId());
      if (chan.length() != 0) {
         Message mess = new Message(
            player,
            (byte)12,
            chan,
            "<System> This is the Community Assistance window. Just type your questions here. To stop receiving these messages, manage your profile."
         );
         player.getCommunicator().sendMessage(mess);
         this.joinPAChannel(player);
      }
   }

   public static String getKingdomHelpChannelName(byte kingdomId) {
      String chan = "";
      if (kingdomId == 4) {
         chan = "CA HELP";
      } else if (kingdomId == 1) {
         chan = "JK HELP";
      } else if (kingdomId == 2) {
         chan = "MR HELP";
      } else if (kingdomId == 3) {
         chan = "HOTS HELP";
      }

      return chan;
   }

   public void sendGVHelpWindow(Player player) {
      Message mess = new Message(
         player,
         (byte)12,
         "GV HELP",
         "<System> This is the GV Help window. just reply to questions here. To stop receiving these messages, manage your profile."
      );
      player.getCommunicator().sendMessage(mess);
   }

   void sendGmsToPlayer(Player player) {
      Message mess = new Message(player, (byte)9, "MGMT", "");
      player.getCommunicator().sendMessage(mess);
      if (player.mayHearMgmtTalk() || player.mayHearDevTalk()) {
         this.sendToTabs(player, player.getPower() < 2, player.getPower() >= 2);
      }

      if (player.mayHearMgmtTalk()) {
         for(TabData tabData : tabListMGMT.values()) {
            if (tabData.isVisible() || tabData.getPower() < 2) {
               player.getCommunicator().sendAddMgmt(tabData.getName(), tabData.getWurmId());
            }
         }
      }

      if (player.mayMute()) {
         Message mess2 = new Message(player, (byte)11, "GM", "");
         player.getCommunicator().sendMessage(mess2);
         if (player.mayHearDevTalk()) {
            for(TabData tabData : tabListGM.values()) {
               if (tabData.isVisible() || tabData.getPower() <= player.getPower()) {
                  player.getCommunicator().sendAddGm(tabData.getName(), tabData.getWurmId());
               }
            }
         }
      }
   }

   void sendTicketsToPlayer(Player player) {
      Ticket[] tickets = Tickets.getTickets(player);

      for(Ticket t : tickets) {
         player.getCommunicator().sendTicket(t);
      }
   }

   public final void removeGlobalEffect(long id) {
      for(Player player : this.getPlayers()) {
         player.getCommunicator().sendRemoveEffect(id);
      }
   }

   void sendAltarsToPlayer(Player player) {
      for(EndGameItem eg : EndGameItems.altars.values()) {
         if (eg.isHoly()) {
            player.getCommunicator().sendAddEffect(eg.getWurmid(), (short)2, eg.getItem().getPosX(), eg.getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
            if (WurmCalendar.isChristmas()) {
               if (Zones.santaMolRehan != null) {
                  player.getCommunicator()
                     .sendAddEffect(
                        Zones.santaMolRehan.getWurmId(),
                        (short)4,
                        Zones.santaMolRehan.getPosX(),
                        Zones.santaMolRehan.getPosY(),
                        Zones.santaMolRehan.getPositionZ(),
                        (byte)0
                     );
               }

               if (Zones.santa != null) {
                  player.getCommunicator()
                     .sendAddEffect(Zones.santa.getWurmId(), (short)4, Zones.santa.getPosX(), Zones.santa.getPosY(), Zones.santa.getPositionZ(), (byte)0);
               }

               if (Zones.santas != null && !Zones.santas.isEmpty()) {
                  for(Creature santa : Zones.santas.values()) {
                     player.getCommunicator().sendAddEffect(santa.getWurmId(), (short)4, santa.getPosX(), santa.getPosY(), santa.getPositionZ(), (byte)0);
                  }
               }
            }
         } else {
            player.getCommunicator().sendAddEffect(eg.getWurmid(), (short)3, eg.getItem().getPosX(), eg.getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
            if (WurmCalendar.isChristmas() && Zones.evilsanta != null) {
               player.getCommunicator()
                  .sendAddEffect(
                     Zones.evilsanta.getWurmId(), (short)4, Zones.evilsanta.getPosX(), Zones.evilsanta.getPosY(), Zones.evilsanta.getPositionZ(), (byte)0
                  );
            }
         }
      }

      if ((EndGameItems.altars == null || EndGameItems.altars.isEmpty()) && WurmCalendar.isChristmas()) {
         if (Zones.santa != null) {
            player.getCommunicator()
               .sendAddEffect(Zones.santa.getWurmId(), (short)4, Zones.santa.getPosX(), Zones.santa.getPosY(), Zones.santa.getPositionZ(), (byte)0);
         }

         if (Zones.santaMolRehan != null) {
            player.getCommunicator()
               .sendAddEffect(
                  Zones.santaMolRehan.getWurmId(),
                  (short)4,
                  Zones.santaMolRehan.getPosX(),
                  Zones.santaMolRehan.getPosY(),
                  Zones.santaMolRehan.getPositionZ(),
                  (byte)0
               );
         }

         if (Zones.evilsanta != null) {
            player.getCommunicator()
               .sendAddEffect(
                  Zones.evilsanta.getWurmId(), (short)4, Zones.evilsanta.getPosX(), Zones.evilsanta.getPosY(), Zones.evilsanta.getPositionZ(), (byte)0
               );
         }

         if (Zones.santas != null && !Zones.santas.isEmpty()) {
            for(Creature santa : Zones.santas.values()) {
               player.getCommunicator().sendAddEffect(santa.getWurmId(), (short)4, santa.getPosX(), santa.getPosY(), santa.getPositionZ(), (byte)0);
            }
         }
      }

      if (Servers.localServer.isChallengeServer() && challengeStep > 0) {
         player.getCommunicator().sendAddEffect(Long.MAX_VALUE - (long)Server.rand.nextInt(100000), (short)20, 0.0F, 0.0F, 0.0F, (byte)0);
         if (challengeStep > 1) {
            player.getCommunicator().sendAddEffect(Long.MAX_VALUE - (long)Server.rand.nextInt(100000), (short)21, 0.0F, 0.0F, 0.0F, (byte)0);
         }

         if (challengeStep > 2) {
            player.getCommunicator().sendAddEffect(Long.MAX_VALUE - (long)Server.rand.nextInt(100000), (short)22, 0.0F, 0.0F, 0.0F, (byte)0);
         }

         if (challengeStep > 3) {
            player.getCommunicator().sendAddEffect(Long.MAX_VALUE - (long)Server.rand.nextInt(100000), (short)23, 0.0F, 0.0F, 0.0F, (byte)0);
         }
      }

      Effect[] effs = EffectFactory.getInstance().getAllEffects();

      for(Effect effect : effs) {
         if (effect.isGlobal()) {
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(
                  player.getName()
                     + " Sending effect type "
                     + effect.getType()
                     + " at position (x,y,z) "
                     + effect.getPosX()
                     + ','
                     + effect.getPosY()
                     + ','
                     + effect.getPosZ()
               );
            }

            player.getCommunicator().sendAddEffect(effect.getOwner(), effect.getType(), effect.getPosX(), effect.getPosY(), effect.getPosZ(), (byte)0);
         }
      }
   }

   public void logoutPlayer(Player player) {
      if (player.hasLink()) {
         try {
            player.getCommunicator().sendShutDown("You were logged out by the server.", false);
         } catch (Exception var4) {
            if (logger.isLoggable(Level.FINEST)) {
               logger.log(Level.FINEST, "Could not send shutdown to " + player + " due to " + var4.getMessage(), (Throwable)var4);
            }
         }

         try {
            player.getCommunicator().disconnect();
         } catch (Exception var3) {
            if (logger.isLoggable(Level.FINEST)) {
               logger.log(Level.FINEST, "Could not send disconnect to " + player + " due to " + var3.getMessage(), (Throwable)var3);
            }
         }
      } else {
         player.getCommunicator().disconnect();
      }

      this.sendConnectInfo(player, " left the world.", System.currentTimeMillis(), PlayerOnlineStatus.OFFLINE);
      this.removeFromGroups(player);
      this.setCreatureDead(player);
      Creatures.getInstance().setCreatureDead(player);
      player.logout();
      players.remove(player.getName());
      playersById.remove(player.getWurmId());
      Server.getInstance().steamHandler.EndAuthSession(player.getSteamId().toString());
      if (Servers.localServer.PVPSERVER && !Servers.isThisATestServer() && player.getPower() < 1) {
         KingdomIp kip = KingdomIp.getKIP(player.getSaveFile().getIpaddress(), (byte)0);
         if (kip != null) {
            kip.logoff();
         }
      }
   }

   final void removePlayer(Player player) {
      players.remove(player.getName());
      playersById.remove(player.getWurmId());
   }

   final void addPlayer(Player player) {
      players.put(player.getName(), player);
      playersById.put(player.getWurmId(), player);
   }

   public Player[] getPlayers() {
      return players.values().toArray(new Player[players.size()]);
   }

   public Map<String, Player> getPlayerMap() {
      return players;
   }

   String[] getPlayerNames() {
      return players.keySet().toArray(new String[players.size()]);
   }

   public void sendEffect(short effType, float posx, float posy, float posz, boolean surfaced, float maxDistMeters) {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         try {
            if (lPlayer.getVisionArea() != null && lPlayer.isOnSurface() == surfaced && lPlayer.isWithinDistanceTo(posx, posy, posz, maxDistMeters)) {
               lPlayer.getCommunicator().sendAddEffect(WurmId.getNextTempItemId(), effType, posx, posy, posz, (byte)(surfaced ? 0 : -1));
            }
         } catch (NullPointerException var13) {
            logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
            lPlayer.setLink(false);
         }
      }
   }

   public void sendChangedTile(@Nonnull TilePos tilePos, boolean surfaced, boolean destroyTrap) {
      this.sendChangedTile(tilePos.x, tilePos.y, surfaced, destroyTrap);
   }

   public void sendChangedTile(int tilex, int tiley, boolean surfaced, boolean destroyTrap) {
      Player[] playarr = this.getPlayers();
      if (destroyTrap) {
         Trap t = Trap.getTrap(tilex, tiley, surfaced ? 0 : -1);
         if (t != null) {
            byte tiletype = Tiles.decodeType(Zones.getMesh(surfaced).getTile(tilex, tiley));
            if (!t.mayTrapRemainOnTile(tiletype)) {
               try {
                  t.delete();
               } catch (IOException var14) {
               }
            }
         }
      }

      boolean nearRoad = this.isNearRoad(surfaced, tilex, tiley);
      if (surfaced) {
         for(Player lPlayer : playarr) {
            try {
               if (lPlayer.getVisionArea() != null && lPlayer.getVisionArea().contains(tilex, tiley) && lPlayer.getCommunicator() != null) {
                  try {
                     lPlayer.getMovementScheme().touchFreeMoveCounter();
                     if (nearRoad) {
                        lPlayer.getCommunicator().sendTileStrip((short)(tilex - 1), (short)(tiley - 1), 3, 3);
                     } else {
                        lPlayer.getCommunicator().sendTileStrip((short)tilex, (short)tiley, 1, 1);
                     }
                  } catch (IOException var13) {
                  }
               }
            } catch (NullPointerException var15) {
               if (lPlayer == null) {
                  logger.log(Level.INFO, "Null player detected. Ignoring for now.");
               } else {
                  logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
                  lPlayer.setLink(false);
               }
            }
         }
      } else {
         for(Player lPlayer : playarr) {
            try {
               if (lPlayer.getVisionArea() != null && lPlayer.getVisionArea().containsCave(tilex, tiley)) {
                  lPlayer.getMovementScheme().touchFreeMoveCounter();
                  lPlayer.getCommunicator().sendCaveStrip((short)(tilex - 1), (short)(tiley - 1), 3, 3);
               }
            } catch (NullPointerException var12) {
               logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
               lPlayer.setLink(false);
            }
         }
      }
   }

   public void sendChangedTiles(int startX, int startY, int sizeX, int sizeY, boolean surfaced, boolean destroyTrap) {
      if (destroyTrap) {
         for(int x = 0; x < sizeX; ++x) {
            for(int y = 0; y < sizeY; ++y) {
               int tempTileX = startX + x;
               int tempTileY = startY + y;
               if (GeneralUtilities.isValidTileLocation(tempTileX, tempTileY)) {
                  Trap t = Trap.getTrap(tempTileX, tempTileY, surfaced ? 0 : -1);
                  if (t != null) {
                     byte tiletype = Tiles.decodeType(Zones.getMesh(surfaced).getTile(tempTileX, tempTileY));
                     if (!t.mayTrapRemainOnTile(tiletype)) {
                        try {
                           t.delete();
                        } catch (IOException var17) {
                        }
                     }
                  }
               }
            }
         }
      }

      boolean nearRoad = sizeX == 1 && sizeY == 1 && this.isNearRoad(surfaced, startX, startY);
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         try {
            if (surfaced) {
               if (nearRoad) {
                  if (lPlayer.getVisionArea() != null
                     && (
                        lPlayer.getVisionArea().contains(startX, startY)
                           || lPlayer.getVisionArea().contains(startX, startY + sizeY)
                           || lPlayer.getVisionArea().contains(startX + sizeX, startY + sizeY)
                           || lPlayer.getVisionArea().contains(startX + sizeX, startY)
                     )) {
                     try {
                        lPlayer.getCommunicator().sendTileStrip((short)(startX - 1), (short)(startY - 1), 3, 3);
                     } catch (IOException var16) {
                     }
                  }
               } else if (lPlayer.getVisionArea() != null
                  && (
                     lPlayer.getVisionArea().contains(startX, startY)
                        || lPlayer.getVisionArea().contains(startX, startY + sizeY)
                        || lPlayer.getVisionArea().contains(startX + sizeX, startY + sizeY)
                        || lPlayer.getVisionArea().contains(startX + sizeX, startY)
                  )) {
                  try {
                     lPlayer.getCommunicator().sendTileStrip((short)startX, (short)startY, sizeX, sizeY);
                  } catch (IOException var15) {
                  }
               }
            } else if (lPlayer.isNearCave()) {
               for(int xx = startX; xx < startX + sizeX; ++xx) {
                  for(int yy = startY; yy < startY + sizeY; ++yy) {
                     if (lPlayer.getVisionArea() != null && lPlayer.getVisionArea().containsCave(xx, yy)) {
                        lPlayer.getCommunicator().sendCaveStrip((short)xx, (short)yy, 1, 1);
                     }
                  }
               }
            }
         } catch (NullPointerException var18) {
            logger.log(Level.WARNING, "Null visionArea or communicator for player " + lPlayer.getName() + ", disconnecting.");
            lPlayer.setLink(false);
         }
      }
   }

   private boolean isNearRoad(boolean surfaced, int tilex, int tiley) {
      try {
         if (surfaced) {
            for(int x = -1; x <= 1; ++x) {
               for(int y = -1; y <= 1; ++y) {
                  if (GeneralUtilities.isValidTileLocation(tilex + x, tiley + y) && Tiles.isRoadType(Server.surfaceMesh.getTile(tilex + x, tiley + y))) {
                     return true;
                  }
               }
            }
         }
      } catch (Exception var6) {
         logger.log(Level.WARNING, "****** Oops invalid x,y " + tilex + "," + tiley + ".");
      }

      return false;
   }

   void savePlayersAtShutdown() {
      logger.info("Saving Players");
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         if (lPlayer.getDraggedItem() != null) {
            Items.stopDragging(lPlayer.getDraggedItem());
         }

         try {
            lPlayer.sleep();
         } catch (Exception var7) {
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         }
      }

      logger.info("Finished saving Players");
   }

   public String getNameFor(long playerId) throws NoSuchPlayerException, IOException {
      Long pid = playerId;
      Player p = this.getPlayerById(pid);
      if (p != null) {
         return p.getName();
      } else {
         PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
         if (info != null) {
            return info.getName();
         } else {
            PlayerState pState = PlayerInfoFactory.getPlayerState(playerId);
            return pState != null ? pState.getPlayerName() : DbSearcher.getNameForPlayer(playerId);
         }
      }
   }

   public long getWurmIdFor(String name) throws NoSuchPlayerException, IOException {
      PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
      return info.loaded ? info.wurmId : DbSearcher.getWurmIdForPlayer(name);
   }

   private void loadBannedIps() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(IPBan.getSelectSql());
         rs = ps.executeQuery();

         while(rs.next()) {
            String ip = rs.getString("IPADDRESS");
            String reason = rs.getString("BANREASON");
            long expiry = rs.getLong("BANEXPIRY");
            Ban bip = new IPBan(ip, reason, expiry);
            if (!bip.isExpired()) {
               bans.add(bip);
            } else {
               this.removeBan(bip);
            }
         }

         logger.info("Loaded " + bans.size() + " banned IPs");
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to load banned ips.", (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void loadBannedSteamIds() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(SteamIdBan.getSelectSql());
         rs = ps.executeQuery();

         while(rs.next()) {
            String identifier = rs.getString("STEAM_ID");
            String reason = rs.getString("BANREASON");
            long expiry = rs.getLong("BANEXPIRY");
            Ban bip = new SteamIdBan(SteamId.fromSteamID64(Long.valueOf(identifier)), reason, expiry);
            if (!bip.isExpired()) {
               bans.add(bip);
            } else {
               this.removeBan(bip);
            }
         }

         logger.info("Loaded " + bans.size() + " more bans from steamids");
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to load banned steamids.", (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public int getNumberOfPlayers() {
      return players.size();
   }

   public void addBannedIp(String ip, String reason, long expiry) {
      Ban ban = new IPBan(ip, reason, expiry);
      this.addBan(ban);
   }

   public void addBan(Ban ban) {
      if (ban != null && ban.getIdentifier() != null && !ban.getIdentifier().isEmpty()) {
         Ban bip = this.getBannedIp(ban.getIdentifier());
         if (bip == null) {
            bans.add(ban);
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement(ban.getInsertSql());
               ps.setString(1, ban.getIdentifier());
               ps.setString(2, ban.getReason());
               ps.setLong(3, ban.getExpiry());
               ps.executeUpdate();
            } catch (SQLException var18) {
               logger.log(Level.WARNING, "Failed to add ban " + ban.getIdentifier(), (Throwable)var18);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         } else {
            bip.setReason(ban.getReason());
            bip.setExpiry(ban.getExpiry());
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement(bip.getUpdateSql());
               ps.setString(1, bip.getReason());
               ps.setLong(2, bip.getExpiry());
               ps.setString(3, bip.getIdentifier());
               ps.executeUpdate();
            } catch (SQLException var16) {
               logger.log(Level.WARNING, "Failed to update ban " + bip.getIdentifier(), (Throwable)var16);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      } else {
         logger.warning("Cannot add a null ban");
      }
   }

   public boolean removeBan(String identifier) {
      Ban existing = null;

      for(Ban lBip : bans) {
         if (lBip.getIdentifier().equals(identifier)) {
            existing = lBip;
         } else if (identifier.contains("*") && lBip.getIdentifier().startsWith(identifier)) {
            existing = lBip;
         }
      }

      if (existing == null) {
         existing = Ban.fromString(identifier);
      }

      return this.removeBan(existing);
   }

   public boolean removeBan(Ban ban) {
      bans.remove(ban);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(ban.getDeleteSql());
         ps.setString(1, ban.getIdentifier());
         ps.executeUpdate();
         return true;
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to remove ban " + ban.getIdentifier(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return false;
   }

   public final void tickSecond() {
      for(Player p : players.values()) {
         if (p.getSaveFile() != null && p.getSaveFile().sleep > 0 && !p.getSaveFile().frozenSleep) {
            float chance = p.getStatus().getFats() / 3.0F;
            if (!(Server.rand.nextFloat() < chance)) {
               --p.getSaveFile().sleep;
            }
         }
      }
   }

   public Ban[] getPlayersBanned() {
      Set<Ban> banned = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,BANREASON,BANEXPIRY FROM PLAYERS WHERE BANNED=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            String ip = rs.getString("NAME");
            String reason = rs.getString("BANREASON");
            long expiry = rs.getLong("BANEXPIRY");
            if (expiry > System.currentTimeMillis()) {
               banned.add(new IPBan(ip, reason, expiry));
            }
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to get players banned.", (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return banned.toArray(new Ban[banned.size()]);
   }

   public void sendGmMessage(Creature sender, String playerName, String message, boolean emote, int red, int green, int blue) {
      Message mess = null;
      if (emote) {
         mess = new Message(sender, (byte)6, "GM", message);
      } else {
         mess = new Message(sender, (byte)11, "GM", "<" + playerName + "> " + message, red, green, blue);
      }

      addGmMessage(playerName, message);
      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.mayHearDevTalk()) {
            if (sender == null) {
               mess.setSender(lPlayer);
            }

            lPlayer.getCommunicator().sendMessage(mess);
         }
      }
   }

   public void sendGmMessage(Creature sender, String playerName, String message, boolean emote) {
      this.sendGmMessage(sender, playerName, message, emote, -1, -1, -1);
   }

   public void sendGlobalKingdomMessage(Creature sender, long senderId, String playerName, String message, boolean emote, byte kingdom, int r, int g, int b) {
      Message mess = null;
      mess = new Message(sender, (byte)16, "GL-" + Kingdoms.getChatNameFor(kingdom), "<" + playerName + "> " + message);
      mess.setSenderKingdom(kingdom);
      mess.setSenderId(senderId);
      mess.setColorR(r);
      mess.setColorG(g);
      mess.setColorB(b);
      Server.getInstance().addMessage(mess);
   }

   public void sendGlobalTradeMessage(Creature sender, long senderId, String playerName, String message, byte kingdom, int r, int g, int b) {
      Message mess = null;
      mess = new Message(sender, (byte)18, "Trade", "<" + playerName + "> " + message);
      mess.setSenderKingdom(kingdom);
      mess.setSenderId(senderId);
      mess.setColorR(r);
      mess.setColorG(g);
      mess.setColorB(b);
      Server.getInstance().addMessage(mess);
   }

   public void partPAChannel(Player player) {
      if (!player.seesPlayerAssistantWindow()) {
         Player[] playerArr = getInstance().getPlayers();

         for(Player lPlayer : playerArr) {
            if (lPlayer.getSaveFile() != null && lPlayer.seesPlayerAssistantWindow()) {
               lPlayer.getCommunicator().sendRemovePa(player.getName());
            }
         }
      }
   }

   public void joinPAChannel(Player player) {
      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.getSaveFile() != null && lPlayer.seesPlayerAssistantWindow() && player.isVisibleTo(lPlayer)) {
            if (player.isPlayerAssistant()) {
               lPlayer.getCommunicator().sendAddPa(" CA " + player.getName(), player.getWurmId());
            } else if (this.shouldReceivePlayerList(lPlayer) && player.getPower() < 2) {
               lPlayer.getCommunicator().sendAddPa(player.getName(), player.getWurmId());
            }
         }
      }
   }

   public void partChannels(Player player) {
      boolean mayDev = player.mayHearDevTalk();
      boolean mayMgmt = player.mayHearMgmtTalk();
      boolean mayHelp = player.seesPlayerAssistantWindow();
      if (mayDev || mayMgmt || mayHelp) {
         String playerName = player.getName();
         if (mayDev || mayMgmt) {
            this.removeFromTabs(player.getWurmId(), playerName);
            this.sendRemoveFromTabs(player.getWurmId(), playerName);
         }

         for(Player otherPlayer : getInstance().getPlayers()) {
            if (!player.isVisibleTo(otherPlayer) && mayHelp && otherPlayer.seesPlayerAssistantWindow()) {
               if (player.isPlayerAssistant()) {
                  otherPlayer.getCommunicator().sendRemovePa(" CA " + playerName);
               } else if (this.shouldReceivePlayerList(otherPlayer)) {
                  otherPlayer.getCommunicator().sendRemovePa(playerName);
               }
            }
         }
      }
   }

   public void joinChannels(Player player) {
      boolean mayDev = player.mayHearDevTalk();
      boolean mayMgmt = player.mayHearMgmtTalk();
      boolean mayHelp = player.seesPlayerAssistantWindow();
      if (mayDev || mayMgmt || mayHelp) {
         long playerId = player.getWurmId();
         String playerName = player.getName();
         if (mayDev || mayMgmt) {
            this.sendToTabs(player, player.getPower() < 2, player.getPower() >= 2);
         }

         for(Player otherPlayer : getInstance().getPlayers()) {
            if (player.isVisibleTo(otherPlayer) && player != otherPlayer && mayHelp && otherPlayer.seesPlayerAssistantWindow()) {
               if (player.isPlayerAssistant()) {
                  otherPlayer.getCommunicator().sendAddPa(" CA " + playerName, playerId);
               } else if (this.shouldReceivePlayerList(otherPlayer) && player.getPower() < 2) {
                  otherPlayer.getCommunicator().sendAddPa(playerName, playerId);
               }
            }
         }
      }
   }

   public void sendPaMessage(Message mes) {
      caHelpLogger.info(mes.getMessage());
      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.seesPlayerAssistantWindow()) {
            lPlayer.getCommunicator().sendMessage(mes);
         }
      }
   }

   public void sendGVMessage(Message mes) {
      caHelpLogger.info(mes.getMessage());
      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.seesGVHelpWindow()) {
            lPlayer.getCommunicator().sendMessage(mes);
         }
      }
   }

   public void sendCaMessage(byte kingdom, Message mes) {
      caHelpLogger.info(mes.getMessage());
      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.seesPlayerAssistantWindow() && (lPlayer.getKingdomId() == kingdom || lPlayer.getPower() >= 2)) {
            lPlayer.getCommunicator().sendMessage(mes);
         }
      }
   }

   public String[] getMuters() {
      Set<String> muted = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE MAYMUTE=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            muted.add(name);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to get muters.", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return muted.toArray(new String[muted.size()]);
   }

   public String[] getDevTalkers() {
      Set<String> devTalkers = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE DEVTALK=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            devTalkers.add(name);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to get dev talkers.", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return devTalkers.toArray(new String[devTalkers.size()]);
   }

   public String[] getCAs() {
      Set<String> pas = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE PA=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            pas.add(name);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to get pas.", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return pas.toArray(new String[pas.size()]);
   }

   public String[] getHeros(byte checkPower) {
      Set<String> heros = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME FROM PLAYERS WHERE POWER=? AND CURRENTSERVER=?");
         ps.setByte(1, checkPower);
         ps.setInt(2, Servers.localServer.getId());
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            heros.add(name);
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to get heros.", (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return heros.toArray(new String[heros.size()]);
   }

   public Ban[] getPlayersMuted() {
      Set<Ban> muted = new HashSet<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,MUTEREASON,MUTEEXPIRY FROM PLAYERS WHERE MUTED=1");
         rs = ps.executeQuery();

         while(rs.next()) {
            String ip = rs.getString("NAME");
            String reason = rs.getString("MUTEREASON");
            long expiry = rs.getLong("MUTEEXPIRY");
            muted.add(new IPBan(ip, reason, expiry));
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to get players muted.", (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return muted.toArray(new Ban[muted.size()]);
   }

   public Ban getAnyBan(String ip, Player player, String steamId) {
      Ban ban = player.getBan();
      if (ban == null) {
         ban = this.getBannedIp(ip);
      }

      if (ban == null) {
         ban = this.getBannedSteamId(steamId);
      }

      return ban;
   }

   public Ban getBannedSteamId(String steamId) {
      if (steamId.isEmpty()) {
         return null;
      } else {
         Ban[] banArr = bans.toArray(new Ban[0]);

         for(Ban ban : banArr) {
            if (ban != null && ban.getIdentifier().equals(steamId)) {
               if (!ban.isExpired()) {
                  return ban;
               }

               this.removeBan(ban);
            }
         }

         return null;
      }
   }

   public Ban getBannedIp(String ip) {
      if (ip.isEmpty()) {
         return null;
      } else {
         Ban[] bips = bans.toArray(new Ban[0]);
         int dots = 0;

         for(Ban lBip : bips) {
            if (lBip == null) {
               logger.warning("BannedIPs includes a null");
               return null;
            }

            dots = lBip.getIdentifier().indexOf("*");
            if (dots > 0) {
               if (lBip.isExpired()) {
                  this.removeBan(lBip.getIdentifier());
               } else if (lBip.getIdentifier().substring(0, dots).equals(ip.substring(0, dots))) {
                  return lBip;
               }
            }

            if (lBip.getIdentifier().equals(ip)) {
               if (!lBip.isExpired()) {
                  return lBip;
               }

               this.removeBan(lBip.getIdentifier());
            }
         }

         return null;
      }
   }

   public Ban[] getBans() {
      Ban[] bips = bans.toArray(new Ban[bans.size()]);
      Arrays.sort(bips, new Comparator<Ban>() {
         public int compare(Ban o1, Ban o2) {
            return o1.getIdentifier().compareTo(o2.getIdentifier());
         }
      });
      return bips;
   }

   public void convertFromKingdomToKingdom(byte oldKingdom, byte newKingdom) {
      Player[] playerArr = this.getPlayers();

      for(Player play : playerArr) {
         if (play.getKingdomId() == oldKingdom) {
            try {
               play.setKingdomId(newKingdom, true);
            } catch (IOException var15) {
               logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
            }
         }
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET KINGDOM=? WHERE KINGDOM=?");
         ps.setByte(1, newKingdom);
         ps.setByte(2, oldKingdom);
         ps.executeUpdate();
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Failed to change kingdom to " + newKingdom, (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void convertPlayerToKingdom(long wurmId, byte newKingdom) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET KINGDOM=? WHERE WURMID=?");
         ps.setByte(1, newKingdom);
         ps.setLong(2, wurmId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to change kingdom to " + newKingdom + " for " + wurmId, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public long getLastLogoutForPlayer(long wurmid) {
      long toReturn = 0L;
      if (this.getPlayerById(wurmid) != null) {
         toReturn = System.currentTimeMillis();
      } else {
         PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
         if (pinf != null) {
            return pinf.lastLogout;
         }
      }

      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT LASTLOGOUT FROM PLAYERS WHERE WURMID=?");
         ps.setLong(1, wurmid);
         rs = ps.executeQuery();
         if (rs.next()) {
            toReturn = rs.getLong("LASTLOGOUT");
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to retrieve lastlogout for " + wurmid, (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return toReturn;
   }

   public boolean doesPlayerNameExist(String name) {
      if (this.getPlayerByName(name) != null) {
         return true;
      } else {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         boolean sqex;
         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT WURMID FROM PLAYERS WHERE NAME=?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (!rs.next()) {
               return false;
            }

            sqex = true;
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to check if " + name + " exists:" + var9.getMessage(), (Throwable)var9);
            return false;
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }

         return sqex;
      }
   }

   public long getWurmIdByPlayerName(String name) {
      String lName = LoginHandler.raiseFirstLetter(name);
      if (this.getPlayerByName(lName) != null) {
         return this.getPlayerByName(lName).getWurmId();
      } else {
         PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(lName);
         if (pinf.wurmId > 0L) {
            return pinf.wurmId;
         } else {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            long sqex;
            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement("SELECT WURMID FROM PLAYERS WHERE NAME=?");
               ps.setString(1, lName);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  return -1L;
               }

               sqex = rs.getLong("WURMID");
            } catch (SQLException var11) {
               logger.log(Level.WARNING, "Failed to retrieve wurmid for " + name + " exists:" + var11.getMessage(), (Throwable)var11);
               return -1L;
            } finally {
               DbUtilities.closeDatabaseObjects(ps, rs);
               DbConnector.returnConnection(dbcon);
            }

            return sqex;
         }
      }
   }

   public void registerNewKingdom(Creature registered) {
      this.registerNewKingdom(registered.getWurmId(), registered.getKingdomId());
   }

   public void pollChamps() {
      if (System.currentTimeMillis() - Servers.localServer.lastDecreasedChampionPoints > 604800000L) {
         Servers.localServer.setChampStamp();
         PlayerInfo[] playinfos = PlayerInfoFactory.getPlayerInfos();

         for(int p = 0; p < playinfos.length; ++p) {
            if (playinfos[p].realdeath > 0 && playinfos[p].realdeath < 5) {
               try {
                  Player play = getInstance().getPlayer(playinfos[p].wurmId);
                  play.sendAddChampionPoints();
               } catch (NoSuchPlayerException var4) {
               }
            }
         }
      }

      printChampStats();
   }

   public void registerNewKingdom(long aWurmId, byte aKingdom) {
      this.pkingdoms.put(aWurmId, aKingdom);
   }

   public byte getKingdomForPlayer(long wurmid) {
      Byte b = this.pkingdoms.get(wurmid);
      if (b != null) {
         return b;
      } else {
         Player lPlayerById = this.getPlayerById(wurmid);
         if (lPlayerById != null) {
            this.registerNewKingdom(wurmid, lPlayerById.getKingdomId());
            return lPlayerById.getKingdomId();
         } else {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            byte var9;
            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement("SELECT KINGDOM FROM PLAYERS WHERE WURMID=?");
               ps.setLong(1, wurmid);
               rs = ps.executeQuery();
               if (!rs.next()) {
                  return 0;
               }

               byte toret = rs.getByte("KINGDOM");
               this.pkingdoms.put(wurmid, toret);
               var9 = toret;
            } catch (SQLException var13) {
               logger.log(Level.WARNING, "Failed to retrieve kingdom for " + wurmid, (Throwable)var13);
               return 0;
            } finally {
               DbUtilities.closeDatabaseObjects(ps, rs);
               DbConnector.returnConnection(dbcon);
            }

            return var9;
         }
      }
   }

   public int getPlayersFromKingdom(byte kingdomId) {
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND CURRENTSERVER=? AND POWER=0");
         ps.setByte(1, kingdomId);
         ps.setInt(2, Servers.localServer.id);
         rs = ps.executeQuery();
         rs.last();
         return rs.getRow();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return 0;
   }

   public static int getChampionsFromKingdom(byte kingdomId) {
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0");
         ps.setByte(1, kingdomId);
         rs = ps.executeQuery();

         while(rs.next()) {
            long wid = rs.getLong("WURMID");
            String name = rs.getString("NAME");
            long lastChamped = rs.getLong("LASTLOSTCHAMPION");
            int realDeath = rs.getInt("REALDEATH");
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
            if (pinf.getCurrentServer() == Servers.localServer.id && System.currentTimeMillis() - pinf.championTimeStamp < 14515200000L) {
               ++nums;
            }
         }

         return nums;
      } catch (SQLException var15) {
         logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, (Throwable)var15);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return nums;
   }

   public static int getChampionsFromKingdom(byte kingdomId, int deity) {
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,WURMID,REALDEATH,LASTLOSTCHAMPION FROM PLAYERS WHERE KINGDOM=? AND REALDEATH>0 AND REALDEATH<4 AND POWER=0");
         ps.setByte(1, kingdomId);
         rs = ps.executeQuery();

         while(rs.next()) {
            long wid = rs.getLong("WURMID");
            String name = rs.getString("NAME");
            long lastChamped = rs.getLong("LASTLOSTCHAMPION");
            int realDeath = rs.getInt("REALDEATH");
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
            if (pinf.getCurrentServer() == Servers.localServer.id
               && System.currentTimeMillis() - pinf.championTimeStamp < 14515200000L
               && pinf.getDeity() != null
               && pinf.getDeity().getNumber() == deity) {
               ++nums;
            }
         }

         logger.log(Level.INFO, "Found " + nums + " champs for kingdom =" + kingdomId);
         return nums;
      } catch (SQLException var16) {
         logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, (Throwable)var16);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return nums;
   }

   public static int getPremiumPlayersFromKingdom(byte kingdomId) {
      int nums = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT NAME,WURMID FROM PLAYERS WHERE KINGDOM=? AND PAYMENTEXPIRE>? AND POWER=0");
         ps.setByte(1, kingdomId);
         ps.setLong(2, System.currentTimeMillis());
         rs = ps.executeQuery();

         while(rs.next()) {
            long wid = rs.getLong("WURMID");
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid);
            if (pinf.getCurrentServer() == Servers.localServer.id || System.currentTimeMillis() - pinf.getLastLogout() < 259200000L) {
               ++nums;
            }
         }

         return nums;
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to retrieve nums kingdom for " + kingdomId, (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return nums;
   }

   public void setStructureFinished(long structureid) {
      Player[] playarr = this.getPlayers();
      boolean found = false;

      for(Player lPlayer : playarr) {
         try {
            if (lPlayer.getStructure().getWurmId() == structureid) {
               try {
                  lPlayer.setStructure(null);
                  lPlayer.save();
                  found = true;
                  break;
               } catch (Exception var17) {
                  logger.log(Level.WARNING, "Failed to set structure finished for " + lPlayer, (Throwable)var17);
               }
            }
         } catch (NoSuchStructureException var18) {
         }
      }

      if (!found) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("update PLAYERS set BUILDINGID=-10 WHERE BUILDINGID=?");
            ps.setLong(1, structureid);
            ps.executeUpdate();
         } catch (SQLException var15) {
            logger.log(Level.WARNING, "Failed to set buidlingid to -10 for " + structureid, (Throwable)var15);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static void resetFaithGain() {
      PlayerInfoFactory.resetFaithGain();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET LASTFAITH=0,NUMFAITH=0");
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Problem resetting faith gain - " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void payGms() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET MONEY=MONEY+250000 WHERE POWER>1");
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Problem processing GM Salary - " + var10.getMessage(), (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         if (lPlayer.getPower() > 0) {
            lPlayer.getCommunicator().sendSafeServerMessage("You have now received salary.");
         }
      }
   }

   public void resetPlayer(long wurmid) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE SKILLS SET VALUE=20, MINVALUE=20 WHERE VALUE>20 AND OWNER=?");
         ps.setLong(1, wurmid);
         ps.executeUpdate();
      } catch (SQLException var27) {
         logger.log(Level.WARNING, "Problem resetting player skills - " + var27.getMessage(), (Throwable)var27);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET FAITH=20 WHERE FAITH>20 AND WURMID=?");
         ps.setLong(1, wurmid);
         ps.executeUpdate();
      } catch (SQLException var25) {
         logger.log(Level.WARNING, "Problem resetting player faith - " + var25.getMessage(), (Throwable)var25);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      try {
         Player p = this.getPlayer(wurmid);

         try {
            if (p.isChampion()) {
               p.revertChamp();
               if (p.getFaith() > 20.0F) {
                  p.setFaith(20.0F);
               }
            }
         } catch (IOException var24) {
         }

         Skills sk = p.getSkills();
         Skill[] skills = sk.getSkills();

         for(int x = 0; x < skills.length; ++x) {
            if (skills[x].getKnowledge() > 20.0) {
               skills[x].minimum = 20.0;
               skills[x].setKnowledge(20.0, true);
            }
         }
      } catch (NoSuchPlayerException var29) {
         PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
         if (p != null) {
            try {
               p.setRealDeath((byte)0);
            } catch (IOException var23) {
            }
         }
      }
   }

   public static void sendGmMessages(Player player) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT TIME,SENDER,MESSAGE FROM GMMESSAGES ORDER BY TIME");
         rs = ps.executeQuery();

         while(rs.next()) {
            player.getCommunicator().sendGmMessage(rs.getLong("TIME"), rs.getString("SENDER"), rs.getString("MESSAGE"));
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem getting GM messages - " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      pruneMessages();
   }

   public static void sendMgmtMessages(Player player) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT TIME,SENDER,MESSAGE FROM MGMTMESSAGES ORDER BY TIME");
         rs = ps.executeQuery();

         while(rs.next()) {
            player.getCommunicator().sendMgmtMessage(rs.getLong("TIME"), rs.getString("SENDER"), rs.getString("MESSAGE"));
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem getting management messages - " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      pruneMessages();
   }

   public void sendStartKingdomChat(Player player) {
      if (player.showKingdomStartMessage()) {
         Message mess = new Message(
            player, (byte)10, Kingdoms.getChatNameFor(player.getKingdomId()), "<System> This is the Kingdom Chat for your current server. ", 250, 150, 250
         );
         player.getCommunicator().sendMessage(mess);
         Message mess1 = new Message(
            player,
            (byte)10,
            Kingdoms.getChatNameFor(player.getKingdomId()),
            "<System> You can disable receiving these messages, by a setting in your profile.",
            250,
            150,
            250
         );
         player.getCommunicator().sendMessage(mess1);
      }
   }

   public void sendStartGlobalKingdomChat(Player player) {
      if (player.showGlobalKingdomStartMessage()) {
         Message mess = new Message(
            player, (byte)16, "GL-" + Kingdoms.getChatNameFor(player.getKingdomId()), "<System> This is your Global Kingdom Chat. ", 250, 150, 250
         );
         player.getCommunicator().sendMessage(mess);
         Message mess1 = new Message(
            player,
            (byte)16,
            "GL-" + Kingdoms.getChatNameFor(player.getKingdomId()),
            "<System> You can disable receiving these messages, by a setting in your profile.",
            250,
            150,
            250
         );
         player.getCommunicator().sendMessage(mess1);
      }
   }

   public void sendStartGlobalTradeChannel(Player player) {
      if (player.showTradeStartMessage()) {
         Message mess = new Message(player, (byte)18, "Trade", "<System> This is the Trade channel. ", 250, 150, 250);
         player.getCommunicator().sendMessage(mess);
         Message mess1 = new Message(player, (byte)18, "Trade", "<System> Only messages starting with WTB, WTS, WTT, PC or @ are allowed. ", 250, 150, 250);
         player.getCommunicator().sendMessage(mess1);
         Message mess2 = new Message(player, (byte)18, "Trade", "<System> Please PM the person if you are interested in the Item.", 250, 150, 250);
         player.getCommunicator().sendMessage(mess2);
         Message mess3 = new Message(player, (byte)18, "Trade", "<System> You can also use @<name> to send a reply in this channel to <name>.", 250, 150, 250);
         player.getCommunicator().sendMessage(mess3);
         Message mess4 = new Message(
            player, (byte)18, "Trade", "<System> You can disable receiving these messages, by a setting in your profile.", 250, 150, 250
         );
         player.getCommunicator().sendMessage(mess4);
      }
   }

   public static Map<String, Integer> getBattleRanks(int num) {
      Map<String, Integer> toReturn = new HashMap<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
         ps.setInt(1, num);
         rs = ps.executeQuery();

         while(rs.next()) {
            toReturn.put(rs.getString("NAME"), rs.getInt("RANK"));
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Problem getting battle ranks - " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return toReturn;
   }

   public static Map<String, Integer> getMaxBattleRanks(int num) {
      Map<String, Integer> toReturn = new HashMap<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?");
         ps.setInt(1, num);
         rs = ps.executeQuery();

         while(rs.next()) {
            toReturn.put(rs.getString("NAME"), rs.getInt("MAXRANK"));
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Problem getting Max battle ranks - " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return toReturn;
   }

   public static void printMaxRanks() {
      Writer output = null;

      try {
         String dir = Constants.webPath;
         if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
         }

         File aFile = new File(dir + "maxranks.html");
         output = new BufferedWriter(new FileWriter(aFile));
         String start = "<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>";

         try {
            output.write("<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>");
         } catch (IOException var27) {
            logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
         }

         int nums = 0;
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("select MAXRANK,RANK, NAME from PLAYERS ORDER BY MAXRANK DESC LIMIT ?");
            ps.setInt(1, 30);

            for(rs = ps.executeQuery(); rs.next(); ++nums) {
               if (nums < 10) {
                  output.write(
                     "<TR class=\"gameDataTopTenTR\"><TD class=\"gameDataTopTenTDName\">"
                        + rs.getString("NAME")
                        + "</TD><TD class=\"gameDataTopTenTDValue\">"
                        + rs.getInt("MAXRANK")
                        + "</TD></TR>"
                  );
               } else {
                  output.write(
                     "<TR class=\"gameDataTR\"><TD class=\"gameDataTDName\">"
                        + rs.getString("NAME")
                        + "</TD><TD class=\"gameDataTDValue\">"
                        + rs.getInt("MAXRANK")
                        + "</TD></TR>"
                  );
               }
            }
         } catch (SQLException var28) {
            logger.log(Level.WARNING, "Problem writing maxranks" + var28.getMessage(), (Throwable)var28);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }

         output.write("</TABLE>");
      } catch (IOException var30) {
         logger.log(Level.WARNING, "Failed to save maxranks.html", (Throwable)var30);
      } finally {
         try {
            if (output != null) {
               output.close();
            }
         } catch (IOException var26) {
         }
      }
   }

   public static void printRanks() {
      printMaxRanks();
      Writer output = null;

      try {
         String dir = Constants.webPath;
         if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
         }

         File aFile = new File(dir + "ranks.html");
         output = new BufferedWriter(new FileWriter(aFile));
         output.write(header2);
         String start = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Rank</TH>\n\t\t</TR>\n\t\t";

         try {
            output.write("<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Rank</TH>\n\t\t</TR>\n\t\t");
         } catch (IOException var27) {
            logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
         }

         int nums = 0;
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
            ps.setInt(1, 30);

            for(rs = ps.executeQuery(); rs.next(); ++nums) {
               if (nums < 10) {
                  output.write(
                     "<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDName\">"
                        + rs.getString("NAME")
                        + "</TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">"
                        + rs.getInt("RANK")
                        + "</TD>\n\t\t</TR>\n\t\t"
                  );
               } else {
                  output.write(
                     "<TR class=\"gameDataTR\">\n\t\t\t<TD class=\"gameDataTDName\">"
                        + rs.getString("NAME")
                        + "</TD>\n\t\t\t<TD class=\"gameDataTDValue\">"
                        + rs.getInt("RANK")
                        + "</TD>\n\t\t</TR>\n\n\t"
                  );
               }
            }
         } catch (SQLException var28) {
            logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }

         output.write("</TABLE>\n");
         output.write("\n</BODY>\n</HTML>");
      } catch (IOException var30) {
         logger.log(Level.WARNING, "Failed to close ranks.html", (Throwable)var30);
      } finally {
         try {
            if (output != null) {
               output.close();
            }
         } catch (IOException var26) {
         }
      }
   }

   public static void printChampStats() {
      WurmRecord[] alls = PlayerInfoFactory.getChampionRecords();
      if (alls.length > 0) {
         Writer output = null;

         try {
            String dir = Constants.webPath;
            if (!dir.endsWith(File.separator)) {
               dir = dir + File.separator;
            }

            File aFile = new File(dir + "champs.html");
            output = new BufferedWriter(new FileWriter(aFile));

            try {
               output.write(headerStats2);
               String start = "<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t";
               output.write("<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t");
               int total = 0;
               int totalLimit = 0;

               for(WurmRecord entry : alls) {
                  output.write(
                     "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">"
                        + entry.getHolder()
                        + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                        + entry.getValue()
                        + " current="
                        + entry.isCurrent()
                        + "</TD>\n\t\t</TR>\n\t\t"
                  );
                  total += entry.getValue();
                  ++totalLimit;
               }

               output.write(
                  "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Average points</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                     + total
                     + "/"
                     + totalLimit
                     + "="
                     + total / totalLimit
                     + "</TD>\n\t\t</TR>\n\t\t"
               );
               output.write("</TABLE>\n");
               output.write("\n</BODY>\n</HTML>");
            } catch (IOException var20) {
               logger.log(Level.WARNING, "Problem writing server stats = " + var20.getMessage(), (Throwable)var20);
            }
         } catch (IOException var21) {
            logger.log(Level.WARNING, "Failed to open stats.html", (Throwable)var21);
         } finally {
            try {
               if (output != null) {
                  output.close();
               }
            } catch (IOException var19) {
            }
         }
      }
   }

   public static void printStats() {
      try {
         String dir = Constants.webPath;
         if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
         }

         File aFile = new File(dir + "stats.xml");
         StatsXMLWriter.createXML(aFile);
      } catch (Exception var23) {
         logger.log(Level.WARNING, var23.getMessage(), (Throwable)var23);
      }

      Writer output = null;

      try {
         String dir = Constants.webPath;
         if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
         }

         File aFile = new File(dir + "stats.html");
         output = new BufferedWriter(new FileWriter(aFile));

         try {
            output.write(headerStats);
            String start = "<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t";
            output.write("<TABLE id=\"statsDataTable\">\n\t\t<TR>\n\t\t\t<TH></TH>\n\t\t\t<TH></TH>\n\t\t</TR>\n\t\t");
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Server name</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + Servers.localServer.getName()
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Last updated</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + DateFormat.getDateInstance(2).format(new Timestamp(System.currentTimeMillis()))
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Status</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + (
                     Servers.localServer.maintaining
                        ? "Maintenance"
                        : (Server.getMillisToShutDown() > 0L ? "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds" : "Up and running")
                  )
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Uptime</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + Server.getTimeFor((long)(Server.getSecondsUptime() * 1000))
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Wurm Time</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + WurmCalendar.getTime()
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Weather</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + Server.getWeather().getWeatherString(false)
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            int total = 0;
            int totalLimit = 0;
            int epic = 0;
            int epicMax = 0;
            ServerEntry[] alls = Servers.getAllServers();

            for(ServerEntry entry : alls) {
               if (!entry.EPIC) {
                  if (!entry.isLocal) {
                     output.write(
                        "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">"
                           + entry.getName()
                           + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                           + entry.currentPlayers
                           + "/"
                           + entry.pLimit
                           + "</TD>\n\t\t</TR>\n\t\t"
                     );
                     total += entry.currentPlayers;
                     totalLimit += entry.pLimit;
                  } else {
                     output.write(
                        "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">"
                           + entry.getName()
                           + " players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                           + getInstance().getNumberOfPlayers()
                           + "/"
                           + entry.pLimit
                           + "</TD>\n\t\t</TR>\n\t\t"
                     );
                     total += getInstance().getNumberOfPlayers();
                     totalLimit += entry.pLimit;
                  }
               } else {
                  epic += entry.currentPlayers;
                  epicMax += entry.pLimit;
                  totalLimit += entry.pLimit;
                  total += entry.currentPlayers;
               }
            }

            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Epic cluster players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + epic
                  + "/"
                  + epicMax
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write(
               "<TR class=\"statsTR\">\n\t\t\t<TD class=\"statsDataTDName\">Total players</TD>\n\t\t\t<TD class=\"statsDataTDValue\">"
                  + total
                  + "/"
                  + totalLimit
                  + "</TD>\n\t\t</TR>\n\t\t"
            );
            output.write("</TABLE>\n");
            output.write("\n</BODY>\n</HTML>");
         } catch (IOException var24) {
            logger.log(Level.WARNING, "Problem writing server stats = " + var24.getMessage(), (Throwable)var24);
         }
      } catch (IOException var25) {
         logger.log(Level.WARNING, "Failed to open stats.html", (Throwable)var25);
      } finally {
         try {
            if (output != null) {
               output.close();
            }
         } catch (IOException var22) {
         }
      }
   }

   public static void printRanks2() {
      printMaxRanks();
      Writer output = null;

      try {
         String dir = Constants.webPath;
         if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
         }

         File aFile = new File(dir + "ranks.html");
         output = new BufferedWriter(new FileWriter(aFile));
         String start = "<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>";

         try {
            output.write("<TABLE class=\"gameDataTable\"><TR><TH><Name</TH><TH>Rank</TH></TR>");
         } catch (IOException var27) {
            logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
         }

         int nums = 0;
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("select RANK, NAME from PLAYERS ORDER BY RANK DESC LIMIT ?");
            ps.setInt(1, 30);

            for(rs = ps.executeQuery(); rs.next(); ++nums) {
               if (nums < 10) {
                  output.write(
                     "<TR class=\"gameDataTopTenTR\"><TD class=\"gameDataTopTenTDName\">"
                        + rs.getString("NAME")
                        + "</TD><TD class=\"gameDataTopTenTDValue\">"
                        + rs.getInt("RANK")
                        + "</TD></TR>"
                  );
               } else {
                  output.write(
                     "<TR class=\"gameDataTR\"><TD class=\"gameDataTDName\">"
                        + rs.getString("NAME")
                        + "</TD><TD class=\"gameDataTDValue\">"
                        + rs.getInt("RANK")
                        + "</TD></TR>"
                  );
               }
            }
         } catch (SQLException var28) {
            logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }

         output.write("</TABLE>");
      } catch (IOException var30) {
         logger.log(Level.WARNING, "Failed to close ranks.html", (Throwable)var30);
      } finally {
         try {
            if (output != null) {
               output.close();
            }
         } catch (IOException var26) {
         }
      }
   }

   public static Map<String, Long> getFriends(long wurmid) {
      Map<String, Long> toReturn = new HashMap<>();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("select p.NAME,p.WURMID from PLAYERS p INNER JOIN FRIENDS f ON f.FRIEND=p.WURMID WHERE f.WURMID=? ORDER BY NAME");
         ps.setLong(1, wurmid);
         rs = ps.executeQuery();

         while(rs.next()) {
            toReturn.put(rs.getString("NAME"), rs.getLong("WURMID"));
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return toReturn;
   }

   public static void pruneMessages() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM GMMESSAGES WHERE TIME<? AND MESSAGE NOT LIKE '<Roads> %' AND MESSAGE NOT LIKE '<System> Debug:'");
         ps.setLong(1, System.currentTimeMillis() - 172800000L);
         ps.executeUpdate();
      } catch (SQLException var28) {
         logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM GMMESSAGES WHERE TIME<?");
         ps.setLong(1, System.currentTimeMillis() - 604800000L);
         ps.executeUpdate();
      } catch (SQLException var26) {
         logger.log(Level.WARNING, var26.getMessage(), (Throwable)var26);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MGMTMESSAGES WHERE TIME<?");
         ps.setLong(1, System.currentTimeMillis() - 86400000L);
         ps.executeUpdate();
      } catch (SQLException var24) {
         logger.log(Level.WARNING, var24.getMessage(), (Throwable)var24);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void addMgmtMessage(String sender, String message) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("INSERT INTO MGMTMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)");
         ps.setLong(1, System.currentTimeMillis());
         ps.setString(2, sender);
         ps.setString(3, message.substring(0, Math.min(message.length(), 200)));
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void addGmMessage(String sender, String message) {
      if (!message.contains(" movement too ")) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("INSERT INTO GMMESSAGES(TIME,SENDER,MESSAGE) VALUES(?,?,?)");
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, sender);
            ps.setString(3, message.substring(0, Math.min(message.length(), 200)));
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static void loadAllPrivatePOIForPlayer(Player player) {
      if (player.getPrivateMapAnnotations().isEmpty()) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=0 AND OWNERID=?");
            ps.setLong(1, player.getWurmId());
            rs = ps.executeQuery();

            while(rs.next()) {
               long wid = rs.getLong("ID");
               String name = rs.getString("NAME");
               long position = rs.getLong("POSITION");
               byte type = rs.getByte("POITYPE");
               long ownerId = rs.getLong("OWNERID");
               String server = rs.getString("SERVER");
               byte icon = rs.getByte("ICON");
               player.addMapPOI(new MapAnnotation(wid, name, type, position, ownerId, server, icon), false);
            }
         } catch (SQLException var17) {
            logger.log(Level.WARNING, "Problem loading all private POI's - " + var17.getMessage(), (Throwable)var17);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static void loadAllArtists() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM ARTISTS");
         rs = ps.executeQuery();

         while(rs.next()) {
            long wid = rs.getLong("WURMID");
            artists.put(wid, new Artist(wid, rs.getBoolean("SOUND"), rs.getBoolean("GRAPHICS")));
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem loading all artists - " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static boolean isArtist(long wurmid, boolean soundRequired, boolean graphicsRequired) {
      if (!artists.containsKey(wurmid)) {
         return false;
      } else {
         Artist artist = artists.get(wurmid);
         if (soundRequired) {
            if (artist.isSound()) {
               return graphicsRequired ? artist.isGraphics() : artist.isSound();
            } else {
               return false;
            }
         } else if (graphicsRequired) {
            if (artist.isGraphics()) {
               return soundRequired ? artist.isSound() : artist.isGraphics();
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   public static void addArtist(long wurmid, boolean sound, boolean graphics) {
      if (!artists.containsKey(wurmid)) {
         Artist artist = new Artist(wurmid, sound, graphics);
         artists.put(wurmid, artist);
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("INSERT INTO ARTISTS (WURMID,SOUND,GRAPHICS) VALUES(?,?,?)");
            ps.setLong(1, wurmid);
            ps.setBoolean(2, sound);
            ps.setBoolean(3, graphics);
            ps.executeUpdate();
         } catch (SQLException var11) {
            logger.log(Level.WARNING, "Problem adding artist with id: " + wurmid + " - " + var11.getMessage(), (Throwable)var11);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      } else {
         Artist artist = artists.get(wurmid);
         if (artist.isSound() != sound || artist.isGraphics() != graphics) {
            deleteArtist(wurmid);
            addArtist(wurmid, sound, graphics);
         }
      }
   }

   public static void deleteArtist(long wurmid) {
      artists.remove(wurmid);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM ARTISTS WHERE WURMID=?");
         ps.setLong(1, wurmid);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Problem deleting artist with id: " + wurmid + " - " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public long getNumberOfKills() {
      long totalPlayerKills = 0L;

      for(PlayerKills pk : playerKills.values()) {
         if (pk != null && pk.getNumberOfKills() > 0) {
            totalPlayerKills += (long)pk.getNumberOfKills();
         }
      }

      return totalPlayerKills;
   }

   public PlayerKills getPlayerKillsFor(long wurmId) {
      PlayerKills pk = playerKills.get(wurmId);
      if (pk == null) {
         pk = new PlayerKills(wurmId);
         playerKills.put(wurmId, pk);
      }

      return pk;
   }

   public boolean isOverKilling(long killerid, long victimid) {
      PlayerKills pk = this.getPlayerKillsFor(killerid);
      if (pk.isOverKilling(victimid)) {
         return true;
      } else {
         return deathCount.containsKey(victimid) && deathCount.get(victimid) > 3;
      }
   }

   public void addKill(long killerid, long victimid, String victimName) {
      PlayerKills pk = this.getPlayerKillsFor(killerid);
      pk.addKill(victimid, victimName);
   }

   public void addPvPDeath(long victimId) {
      short currentCount = 0;
      if (deathCount.containsKey(victimId)) {
         currentCount = deathCount.get(victimId);
      }

      deathCount.put(victimId, (short)(currentCount + 1));
   }

   public void removePvPDeath(long victimId) {
      if (deathCount.containsKey(victimId)) {
         short currentCount = deathCount.get(victimId);
         if (currentCount > 1) {
            deathCount.put(victimId, (short)(currentCount - 1));
         } else {
            deathCount.remove(victimId);
         }
      }
   }

   public boolean hasPvpDeaths(long victimId) {
      return deathCount.containsKey(victimId);
   }

   public void sendLogoff(String reason) {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         lPlayer.getCommunicator().sendShutDown(reason, false);
      }
   }

   public void logOffLinklessPlayers() {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         if (!lPlayer.hasLink()) {
            this.logoutPlayer(lPlayer);
         }
      }
   }

   public void checkAffinities() {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         lPlayer.checkAffinity();
      }
   }

   public void checkElectors() {
      Player[] playarr = this.getPlayers();

      for(Player lPlayer : playarr) {
         if (lPlayer.isAspiringKing()) {
            return;
         }
      }

      Methods.resetJennElector();
      Methods.resetHotsElector();
      Methods.resetMolrStone();
   }

   public float getCRBonus(byte kingdomId) {
      Float f = this.crBonuses.get(kingdomId);
      return f != null ? f : 0.0F;
   }

   public void sendUpdateEpicMission(EpicMission mission) {
      for(Player p : this.getPlayers()) {
         if (!Servers.localServer.PVPSERVER) {
            MissionPerformer.sendEpicMission(mission, p.getCommunicator());
         } else {
            MissionPerformer.sendEpicMissionPvPServer(mission, p, p.getCommunicator());
         }
      }
   }

   public void calcCRBonus() {
      if (!Servers.isThisAHomeServer()) {
         Map<Byte, Float> numPs = new HashMap<>();
         float total = 0.0F;

         for(Player lPlayer : this.getPlayers()) {
            if (lPlayer.isPaying()) {
               byte kingdomId = lPlayer.getKingdomId();
               Float f = numPs.get(kingdomId);
               if (f == null) {
                  f = 1.0F;
               } else {
                  f = f + 1.0F;
               }

               numPs.put(kingdomId, f);
               ++total;
            }
         }

         Map<Byte, Float> alliedPs = new HashMap<>();

         for(Byte b : numPs.keySet()) {
            Float f = numPs.get(b);
            alliedPs.put(b, f);
            Kingdom k = Kingdoms.getKingdom(b);
            if (k != null) {
               Map<Byte, Byte> allies = k.getAllianceMap();

               for(Entry<Byte, Byte> entry : allies.entrySet()) {
                  if (entry.getValue() == 1) {
                     Float other = numPs.get(entry.getKey());
                     if (other != null) {
                        f = f + other;
                     }
                  }
               }
            }
         }

         this.crBonuses.clear();
         if (total > 20.0F) {
            for(Entry<Byte, Float> totals : alliedPs.entrySet()) {
               float numbers = totals.getValue();
               if (numbers / total < 0.05F) {
                  this.crBonuses.put(totals.getKey(), 2.0F);
               } else if (numbers / total < 0.1F) {
                  this.crBonuses.put(totals.getKey(), 1.0F);
               }
            }
         }
      }
   }

   public final void updateEigcInfo(EigcClient client) {
      if (!client.getPlayerName().isEmpty()) {
         try {
            Player p = this.getPlayer(client.getPlayerName());
            p.getCommunicator().updateEigcInfo(client);
         } catch (NoSuchPlayerException var3) {
         }
      }
   }

   public static boolean existsPlayerWithIp(String ipAddress) {
      for(Player p : getInstance().getPlayers()) {
         if (p.getSaveFile().getIpaddress().contains(ipAddress)) {
            return true;
         }
      }

      return false;
   }

   public final void sendGlobalGMMessage(Creature sender, String message) {
      Message mess = new Message(sender, (byte)11, "GM", "<" + sender.getName() + "> " + message);
      Server.getInstance().addMessage(mess);
      addGmMessage(sender.getName(), message);
      if (message.trim().length() > 1) {
         WCGmMessage wc = new WCGmMessage(WurmId.getNextWCCommandId(), sender.getName(), "(" + Servers.localServer.getAbbreviation() + ") " + message, false);
         if (Servers.localServer.LOGINSERVER) {
            wc.sendFromLoginServer();
         } else {
            wc.sendToLoginServer();
         }
      }
   }

   public final void pollPlayers() {
      long delta = System.currentTimeMillis() - this.lastPoll;
      if (!((float)delta < 0.095F)) {
         this.lastPoll = System.currentTimeMillis();

         for(Player lPlayer : this.getPlayers()) {
            if (lPlayer != null) {
               lPlayer.pollActions();
            }
         }
      }
   }

   public final void sendKingdomToPlayers(Kingdom kingdom) {
      for(Player lPlayer : this.getPlayers()) {
         if (lPlayer.hasLink()) {
            lPlayer.getCommunicator().sendNewKingdom(kingdom.getId(), kingdom.getName(), kingdom.getSuffix());
         }
      }
   }

   public static void tellFriends(PlayerState pState) {
      for(Player p : getInstance().getPlayers()) {
         if (p.isFriend(pState.getPlayerId())) {
            p.getCommunicator().sendFriend(pState);
         }
      }
   }

   public final void sendTicket(Ticket ticket) {
      for(Player p : getInstance().getPlayers()) {
         if (p.hasLink() && ticket.isTicketShownTo(p)) {
            p.getCommunicator().sendTicket(ticket);
         }
      }
   }

   public final void sendTicket(Ticket ticket, @Nullable TicketAction ticketAction) {
      for(Player p : getInstance().getPlayers()) {
         if (p.hasLink() && ticket.isTicketShownTo(p) && (ticketAction == null || ticketAction.isActionShownTo(p))) {
            p.getCommunicator().sendTicket(ticket, ticketAction);
         }
      }
   }

   public final void removeTicket(Ticket ticket) {
      for(Player p : getInstance().getPlayers()) {
         if (p.hasLink() && ticket.isTicketShownTo(p)) {
            p.getCommunicator().removeTicket(ticket);
         }
      }
   }

   public static final void sendVotingOpen(VoteQuestion vq) {
      for(Player p : getInstance().getPlayers()) {
         sendVotingOpen(p, vq);
      }
   }

   public static void sendVotingOpen(Player p, VoteQuestion vq) {
      if (p.hasLink() && vq.canVote(p)) {
         p.getCommunicator().sendServerMessage("Poll for " + vq.getQuestionTitle() + " is open, use /poll to participate.", 250, 150, 250);
      }
   }

   public void sendMgmtMessage(Creature sender, String playerName, String message, boolean emote, boolean logit, int red, int green, int blue) {
      Message mess = null;
      if (emote) {
         mess = new Message(sender, (byte)6, "MGMT", message);
      } else {
         mess = new Message(sender, (byte)9, "MGMT", "<" + playerName + "> " + message, red, green, blue);
      }

      if (logit) {
         addMgmtMessage(playerName, message);
      }

      Player[] playerArr = getInstance().getPlayers();

      for(Player lPlayer : playerArr) {
         if (lPlayer.mayHearMgmtTalk()) {
            if (sender == null) {
               mess.setSender(lPlayer);
            }

            lPlayer.getCommunicator().sendMessage(mess);
         }
      }
   }

   private boolean shouldReceivePlayerList(Player player) {
      return player.getKingdomId() == 4 && (player.isPlayerAssistant() || player.mayMute() || player.mayHearDevTalk() || player.getPower() > 0);
   }

   public static boolean getPollCheckClients() {
      return pollCheckClients;
   }

   public static void setPollCheckClients(boolean doit) {
      pollCheckClients = doit;
   }

   public static void appointCA(Creature performer, String targetName) {
      Player playerPerformer = null;
      if (performer instanceof Player) {
         playerPerformer = (Player)performer;
         if (playerPerformer.mayAppointPlayerAssistant()) {
            String pname = LoginHandler.raiseFirstLetter(targetName);
            Player p = null;

            try {
               p = getInstance().getPlayer(pname);
            } catch (NoSuchPlayerException var8) {
               playerPerformer.getCommunicator().sendNormalServerMessage("No player online with the name " + pname);
            }

            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(pname);

            try {
               pinf.load();
            } catch (IOException var7) {
               performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
               return;
            }

            if (pinf.wurmId > 0L) {
               if (pinf.isPlayerAssistant()) {
                  pinf.setIsPlayerAssistant(false);
                  if (p != null) {
                     p.getCommunicator().sendAlertServerMessage("You no longer have the duties of a community assistant.", (byte)1);
                  }

                  playerPerformer.getCommunicator().sendSafeServerMessage(pname + " no longer has the duties of being a community assistant.", (byte)1);
                  WcDemotion wc = new WcDemotion(WurmId.getNextWCCommandId(), playerPerformer.getWurmId(), pinf.wurmId, (short)1);
                  wc.sendToLoginServer();
               } else {
                  if (p != null) {
                     p.setPlayerAssistant(true);
                     p.togglePlayerAssistantWindow(true);
                     p.getCommunicator().sendSafeServerMessage("You are now a Community Assistant and receives a CA window.");
                     p.getCommunicator().sendSafeServerMessage("New players will also receive that and may ask you questions.");
                     p.getCommunicator().sendSafeServerMessage("The suggested way to approach new players is not to approach them directly");
                     p.getCommunicator().sendSafeServerMessage("but instead let them ask questions. Otherwise many of them may become deterred");
                     p.getCommunicator().sendSafeServerMessage("since this may be an early online experience or they have poor english knowledge.");
                  } else {
                     pinf.setIsPlayerAssistant(true);
                     pinf.togglePlayerAssistantWindow(true);
                     playerPerformer.getCommunicator().sendAlertServerMessage(pname + " needs to be online in order to receive the title.", (byte)2);
                  }

                  playerPerformer.getCommunicator().sendSafeServerMessage(pname + " is now appointed Community Assistant.", (byte)1);
                  if (playerPerformer.getLogger() != null) {
                     playerPerformer.getLogger().log(Level.INFO, playerPerformer.getName() + " appoints " + pname + " community assistant.");
                  }

                  logger.log(Level.INFO, playerPerformer.getName() + " appoints " + pname + " as community assistant.");
               }
            } else {
               playerPerformer.getCommunicator().sendNormalServerMessage("No player found with the name " + pname);
            }
         }
      }
   }

   public static void appointCM(Creature performer, String targetName) {
      if (performer.getPower() >= 1) {
         String pname = LoginHandler.raiseFirstLetter(targetName);
         Player p = null;

         try {
            p = getInstance().getPlayer(pname);
         } catch (NoSuchPlayerException var7) {
            performer.getCommunicator().sendNormalServerMessage("No player online with the name " + pname);
         }

         PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(pname);

         try {
            pinf.load();
         } catch (IOException var6) {
            performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
            return;
         }

         if (pinf.wurmId > 0L) {
            if (pinf.getPower() == 0) {
               if (pinf.mayMute) {
                  pinf.setMayMute(false);
                  if (p != null) {
                     p.getCommunicator().sendAlertServerMessage("You may no longer mute other players.", (byte)1);
                  }

                  performer.getCommunicator().sendSafeServerMessage(pname + " may no longer mute other players.");
                  WcDemotion wc = new WcDemotion(WurmId.getNextWCCommandId(), performer.getWurmId(), pinf.wurmId, (short)2);
                  wc.sendToLoginServer();
               } else {
                  pinf.setMayMute(true);
                  if (p != null) {
                     p.getCommunicator().sendSafeServerMessage("You may now mute other players. Use this with extreme care and wise judgement.");
                     p.getCommunicator().sendSafeServerMessage("The syntax is #mute <playername> <number of hours> <reason>");
                     p.getCommunicator().sendSafeServerMessage("For example: #mute unforgiven 6 foul language");
                     p.getCommunicator().sendSafeServerMessage("To unmute a player, use #unmute <playername>");
                     p.getCommunicator().sendSafeServerMessage("You may see who are muted with the command #showmuted");
                  }

                  performer.getCommunicator().sendSafeServerMessage(pname + " may now mute other players.", (byte)1);
                  if (performer.getLogger() != null) {
                     performer.getLogger().log(Level.INFO, performer.getName() + " allows " + pname + " to mute other players.");
                  }

                  logger.log(Level.INFO, performer.getName() + " allows " + pname + " to mute other players.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage(pinf.getName() + " may already mute, because he is a Hero or higher.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("No player found with the name " + pname);
         }
      }
   }

   public static void displayLCMInfo(Creature performer, String targetName) {
      if (performer != null && performer.hasLink()) {
         if (performer.getPower() >= 1) {
            try {
               PlayerInfo targetInfo = PlayerInfoFactory.createPlayerInfo(targetName);
               targetInfo.load();
               PlayerState targetState = PlayerInfoFactory.getPlayerState(targetInfo.wurmId);
               Logger logger = performer.getLogger();
               if (logger != null) {
                  logger.log(Level.INFO, performer.getName() + " tried to view the info of " + targetInfo.getName());
               }

               if (performer.getPower() < targetInfo.getPower()) {
                  performer.getCommunicator().sendSafeServerMessage("You can't just look at the information of higher ranking staff members!");
                  return;
               }

               String email = targetInfo.emailAddress;
               String ip = targetInfo.getIpaddress();
               String lastLogout = new Date(targetState.getLastLogout()).toString();
               String timePlayed = Server.getTimeFor(targetInfo.playingTime);
               String CAInfo = targetInfo.getName() + " is " + (targetInfo.isPlayerAssistant() ? "a CA." : "not a CA.");
               String CMInfo = targetInfo.getName() + " is " + (targetInfo.mayMute ? "a CM." : "not a CM.");
               performer.getCommunicator().sendNormalServerMessage("Information about " + targetInfo.getName());
               performer.getCommunicator().sendNormalServerMessage("-----");
               performer.getCommunicator().sendNormalServerMessage("Email address: " + email);
               performer.getCommunicator().sendNormalServerMessage("IP address: " + ip);
               performer.getCommunicator().sendNormalServerMessage("Last logout: " + lastLogout);
               performer.getCommunicator().sendNormalServerMessage("Time played: " + timePlayed);
               performer.getCommunicator().sendNormalServerMessage(CAInfo);
               performer.getCommunicator().sendNormalServerMessage(CMInfo);
               performer.getCommunicator().sendNormalServerMessage("-----");
            } catch (Exception var11) {
               performer.getCommunicator().sendAlertServerMessage("This player does not exist.");
            }
         }
      }
   }

   public void updateTabs(byte tab, TabData tabData) {
      if (tab == 2) {
         this.removeFromTabs(tabData.getWurmId(), tabData.getName());
      } else if (tab == 0) {
         tabListGM.put(tabData.getName(), tabData);

         for(Player player : getInstance().getPlayers()) {
            if (player.mayHearDevTalk()) {
               if (!tabData.isVisible() && tabData.getPower() > player.getPower()) {
                  player.getCommunicator().sendRemoveGm(tabData.getName());
               } else {
                  player.getCommunicator().sendAddGm(tabData.getName(), tabData.getWurmId());
               }
            }
         }
      } else if (tab == 1) {
         tabListMGMT.put(tabData.getName(), tabData);

         for(Player player : getInstance().getPlayers()) {
            if (player.mayHearMgmtTalk()) {
               if (tabData.isVisible()) {
                  player.getCommunicator().sendAddMgmt(tabData.getName(), tabData.getWurmId());
               } else {
                  player.getCommunicator().sendRemoveMgmt(tabData.getName());
               }
            }
         }
      }
   }

   public void sendToTabs(Player player, boolean showMe, boolean justGM) {
      if (player.getPower() >= 2 || player.mayHearDevTalk()) {
         boolean sendGM = false;
         TabData tabData = tabListGM.get(player.getName());
         if (tabData != null) {
            if (tabData.isVisible() != showMe && player.getPower() >= 2) {
               tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe);
               sendGM = true;
            }
         } else {
            tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe || player.getPower() < 2);
            sendGM = true;
         }

         if (sendGM) {
            this.updateTabs((byte)0, tabData);
            WcTabLists wtl = new WcTabLists((byte)0, tabData);
            if (Servers.isThisLoginServer()) {
               wtl.sendFromLoginServer();
            } else {
               wtl.sendToLoginServer();
            }
         }
      }

      if (!justGM) {
         boolean sendMGMT = false;
         TabData tabData = tabListMGMT.get(player.getName());
         if (tabData != null) {
            if (tabData.isVisible() != showMe && player.getPower() >= 2) {
               tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe);
               sendMGMT = true;
            }
         } else {
            tabData = new TabData(player.getWurmId(), player.getName(), (byte)player.getPower(), showMe || player.getPower() < 2);
            sendMGMT = true;
         }

         if (sendMGMT) {
            this.updateTabs((byte)1, tabData);
            WcTabLists wtl = new WcTabLists((byte)1, tabData);
            if (Servers.isThisLoginServer()) {
               wtl.sendFromLoginServer();
            } else {
               wtl.sendToLoginServer();
            }
         }
      }
   }

   public void removeFromTabs(long wurmId, String name) {
      TabData oldGMTabData = tabListGM.remove(name);
      TabData oldMGMTTabData = tabListMGMT.remove(name);
      if (oldGMTabData != null || oldMGMTTabData != null) {
         for(Player player : getInstance().getPlayers()) {
            if (oldGMTabData != null && player.mayHearDevTalk()) {
               player.getCommunicator().sendRemoveGm(name);
            }

            if (oldMGMTTabData != null && player.mayHearMgmtTalk()) {
               player.getCommunicator().sendRemoveMgmt(name);
            }
         }
      }
   }

   public void sendRemoveFromTabs(long wurmId, String name) {
      TabData tabData = new TabData(wurmId, name, (byte)0, false);
      if (tabData != null) {
         WcTabLists wtl = new WcTabLists((byte)2, tabData);
         if (Servers.isThisLoginServer()) {
            wtl.sendFromLoginServer();
         } else {
            wtl.sendToLoginServer();
         }
      }
   }
}
