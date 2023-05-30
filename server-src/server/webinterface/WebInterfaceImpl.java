package com.wurmonline.server.webinterface;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.MountTransfer;
import com.wurmonline.server.intra.PasswordTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMetaData;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.PendingAccount;
import com.wurmonline.server.players.PendingAward;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Reimbursement;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.questions.NewsInfo;
import com.wurmonline.server.questions.WurmInfo;
import com.wurmonline.server.questions.WurmInfo2;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillStat;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebInterfaceImpl
   extends UnicastRemoteObject
   implements WebInterface,
   Serializable,
   MiscConstants,
   TimeConstants,
   CounterTypes,
   MonetaryConstants {
   public static final String VERSION = "$Revision: 1.54 $";
   public static String mailAccount = "mail@mydomain.com";
   public static final Pattern VALID_EMAIL_PATTERN = Pattern.compile("^[\\w\\.\\+-=]+@[\\w\\.-]+\\.[\\w-]+$");
   private static final String PASSWORD_CHARS = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789";
   private static final long serialVersionUID = -2682536434841429586L;
   private final boolean isRunning = true;
   private final Random faceRandom = new Random();
   private static final long faceRandomSeed = 8263186381637L;
   private static final DecimalFormat twoDecimals = new DecimalFormat("##0.00");
   private static final Set<String> moneyDetails = new HashSet<>();
   private static final Set<String> timeDetails = new HashSet<>();
   private static final Logger logger = Logger.getLogger(WebInterfaceImpl.class.getName());
   private static final long[] noInfoLong = new long[]{-1L, -1L};
   private static final String BAD_PASSWORD = "Access denied.";
   private final SimpleDateFormat alloformatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
   private String hostname = "localhost";
   private static final Map<String, Long> ipAttempts = new HashMap<>();
   private String[] bannedMailHosts = new String[]{"sharklasers", "spam4", "grr.la", "guerrillamail"};
   static final int[] emptyIntZero = new int[]{0, 0};

   public WebInterfaceImpl(int port) throws RemoteException {
      super(port);

      try {
         InetAddress localMachine = InetAddress.getLocalHost();
         this.hostname = localMachine.getHostName();
         logger.info("Hostname of local machine used to send registration emails: " + this.hostname);
      } catch (UnknownHostException var3) {
         throw new RemoteException("Could not find localhost for WebInterface", var3);
      }
   }

   public WebInterfaceImpl() throws RemoteException {
   }

   private String getRemoteClientDetails() {
      try {
         return getClientHost();
      } catch (ServerNotActiveException var2) {
         logger.log(Level.WARNING, "Could not get ClientHost details due to " + var2.getMessage(), (Throwable)var2);
         return "Unknown Remote Client";
      }
   }

   @Override
   public int getPower(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPower for playerID: " + aPlayerID);
      }

      try {
         PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
         p.load();
         return p.getPower();
      } catch (IOException var5) {
         logger.log(Level.WARNING, aPlayerID + ": " + var5.getMessage(), (Throwable)var5);
         return 0;
      } catch (NoSuchPlayerException var6) {
         return 0;
      }
   }

   @Override
   public boolean isRunning(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " isRunning");
      }

      return true;
   }

   @Override
   public int getPlayerCount(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayerCount");
      }

      return Players.getInstance().numberOfPlayers();
   }

   @Override
   public int getPremiumPlayerCount(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPremiumPlayerCount");
      }

      return Players.getInstance().numberOfPremiumPlayers();
   }

   @Override
   public String getTestMessage(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getTestMessage");
      }

      synchronized(Server.SYNC_LOCK) {
         return "HEj! " + System.currentTimeMillis();
      }
   }

   @Override
   public void broadcastMessage(String intraServerPassword, String message) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " broadcastMessage: " + message);
      }

      synchronized(Server.SYNC_LOCK) {
         Server.getInstance().broadCastAlert(message);
      }
   }

   @Override
   public long getAccountStatusForPlayer(String intraServerPassword, String playerName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getAccountStatusForPlayer for player: " + playerName);
      }

      synchronized(Server.SYNC_LOCK) {
         if (Servers.localServer.id != Servers.loginServer.id) {
            throw new RemoteException("Not a valid request for this server. Ask the login server instead.");
         } else {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

            long var10000;
            try {
               p.load();
               var10000 = p.money;
            } catch (IOException var7) {
               logger.log(Level.WARNING, playerName + ": " + var7.getMessage(), (Throwable)var7);
               return 0L;
            }

            return var10000;
         }
      }
   }

   @Override
   public Map<String, Integer> getBattleRanks(String intraServerPassword, int numberOfRanksToGet) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getBattleRanks number of Ranks: " + numberOfRanksToGet);
      }

      synchronized(Server.SYNC_LOCK) {
         return Players.getBattleRanks(numberOfRanksToGet);
      }
   }

   @Override
   public String getServerStatus(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getServerStatus");
      }

      synchronized(Server.SYNC_LOCK) {
         String toReturn = "Up and running.";
         if (Server.getMillisToShutDown() > -1000L) {
            toReturn = "Shutting down in " + Server.getMillisToShutDown() / 1000L + " seconds: " + Server.getShutdownReason();
         } else if (Constants.maintaining) {
            toReturn = "The server is in maintenance mode and not open for connections.";
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, Long> getFriends(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getFriends for playerid: " + aPlayerID);
      }

      synchronized(Server.SYNC_LOCK) {
         return Players.getFriends(aPlayerID);
      }
   }

   @Override
   public Map<String, String> getInventory(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getInventory for playerid: " + aPlayerID);
      }

      synchronized(Server.SYNC_LOCK) {
         Map<String, String> toReturn = new HashMap<>();

         try {
            Player p = Players.getInstance().getPlayer(aPlayerID);
            Item inventory = p.getInventory();
            Item[] items = inventory.getAllItems(false);

            for(int x = 0; x < items.length; ++x) {
               toReturn.put(
                  String.valueOf(items[x].getWurmId()), items[x].getName() + ", QL: " + items[x].getQualityLevel() + ", DAM: " + items[x].getDamage()
               );
            }
         } catch (NoSuchPlayerException var11) {
         }

         return toReturn;
      }
   }

   @Override
   public Map<Long, Long> getBodyItems(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getBodyItems for playerid: " + aPlayerID);
      }

      synchronized(Server.SYNC_LOCK) {
         Map<Long, Long> toReturn = new HashMap<>();

         try {
            Player p = Players.getInstance().getPlayer(aPlayerID);
            Body lBody = p.getBody();
            if (lBody != null) {
               Item[] items = lBody.getAllItems();

               for(int x = 0; x < items.length; ++x) {
                  toReturn.put(items[x].getWurmId(), items[x].getParentId());
               }
            }
         } catch (NoSuchPlayerException var11) {
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, Float> getSkills(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getSkills for playerid: " + aPlayerID);
      }

      synchronized(Server.SYNC_LOCK) {
         Map<String, Float> toReturn = new HashMap<>();
         Skills skills = SkillsFactory.createSkills(aPlayerID);

         try {
            skills.load();
            Skill[] skillarr = skills.getSkills();

            for(int x = 0; x < skillarr.length; ++x) {
               toReturn.put(skillarr[x].getName(), new Float(skillarr[x].getKnowledge(0.0)));
            }
         } catch (Exception var10) {
            logger.log(Level.WARNING, aPlayerID + ": " + var10.getMessage(), (Throwable)var10);
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, ?> getPlayerSummary(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayerSummary for playerid: " + aPlayerID);
      }

      synchronized(Server.SYNC_LOCK) {
         Map<String, Object> toReturn = new HashMap<>();
         if (WurmId.getType(aPlayerID) == 0) {
            try {
               Player p = Players.getInstance().getPlayer(aPlayerID);
               toReturn.put("Name", p.getName());
               if (p.citizenVillage != null) {
                  Citizen citiz = p.citizenVillage.getCitizen(aPlayerID);
                  toReturn.put("CitizenVillage", p.citizenVillage.getName());
                  toReturn.put("CitizenRole", citiz.getRole().getName());
               }

               String location = "unknown";
               if (p.currentVillage != null) {
                  location = p.currentVillage.getName() + ", in " + Kingdoms.getNameFor(p.currentVillage.kingdom);
               } else if (p.currentKingdom != 0) {
                  location = Kingdoms.getNameFor(p.currentKingdom);
               }

               toReturn.put("Location", location);
               if (p.getDeity() != null) {
                  toReturn.put("Deity", p.getDeity().name);
               }

               toReturn.put("Faith", new Float(p.getFaith()));
               toReturn.put("Favor", new Float(p.getFavor()));
               toReturn.put("Gender", p.getSex());
               toReturn.put("Alignment", new Float(p.getAlignment()));
               toReturn.put("Kingdom", p.getKingdomId());
               toReturn.put("Battle rank", p.getRank());
               toReturn.put("WurmId", new Long(aPlayerID));
               toReturn.put("Banned", p.getSaveFile().isBanned());
               toReturn.put("Money in bank", p.getMoney());
               toReturn.put("Payment", new Date(p.getPaymentExpire()));
               toReturn.put("Email", p.getSaveFile().emailAddress);
               toReturn.put("Current server", Servers.localServer.id);
               toReturn.put("Last login", new Date(p.getLastLogin()));
               toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
               if (p.getSaveFile().isBanned()) {
                  toReturn.put("IPBan reason", p.getSaveFile().banreason);
                  toReturn.put("IPBan expires in", Server.getTimeFor(p.getSaveFile().banexpiry - System.currentTimeMillis()));
               }

               toReturn.put("Warnings", String.valueOf(p.getSaveFile().getWarnings()));
               if (p.isMute()) {
                  toReturn.put("Muted", Boolean.TRUE);
                  toReturn.put("Mute reason", p.getSaveFile().mutereason);
                  toReturn.put("Mute expires in", Server.getTimeFor(p.getSaveFile().muteexpiry - System.currentTimeMillis()));
               }

               toReturn.put("PlayingTime", Server.getTimeFor(p.getSaveFile().playingTime));
               toReturn.put("Reputation", p.getReputation());
               if (p.getTitle() != null || Features.Feature.COMPOUND_TITLES.isEnabled() && p.getSecondTitle() != null) {
                  toReturn.put("Title", p.getTitleString());
               }

               toReturn.put("Coord x", (int)p.getStatus().getPositionX() >> 2);
               toReturn.put("Coord y", (int)p.getStatus().getPositionY() >> 2);
               if (p.isPriest()) {
                  toReturn.put("Priest", Boolean.TRUE);
               }

               toReturn.put("LoggedOut", p.loggedout);
            } catch (NoSuchPlayerException var11) {
               try {
                  PlayerInfo p = PlayerInfoFactory.createPlayerInfo(Players.getInstance().getNameFor(aPlayerID));
                  p.load();
                  toReturn.put("Name", p.getName());
                  if (p.getDeity() != null) {
                     toReturn.put("Deity", p.getDeity().name);
                  }

                  toReturn.put("Faith", new Float(p.getFaith()));
                  toReturn.put("Favor", new Float(p.getFavor()));
                  toReturn.put("Current server", p.currentServer);
                  toReturn.put("Alignment", new Float(p.getAlignment()));
                  toReturn.put("Battle rank", p.getRank());
                  toReturn.put("WurmId", new Long(aPlayerID));
                  toReturn.put("Banned", p.isBanned());
                  toReturn.put("Money in bank", new Long(p.money));
                  toReturn.put("Payment", new Date(p.getPaymentExpire()));
                  toReturn.put("Email", p.emailAddress);
                  toReturn.put("Last login", new Date(p.getLastLogin()));
                  toReturn.put("Last logout", new Date(Players.getInstance().getLastLogoutForPlayer(aPlayerID)));
                  if (p.isBanned()) {
                     toReturn.put("IPBan reason", p.banreason);
                     toReturn.put("IPBan expires in", Server.getTimeFor(p.banexpiry - System.currentTimeMillis()));
                  }

                  toReturn.put("Warnings", String.valueOf(p.getWarnings()));
                  if (p.isMute()) {
                     toReturn.put("Muted", Boolean.TRUE);
                     toReturn.put("Mute reason", p.mutereason);
                     toReturn.put("Mute expires in", Server.getTimeFor(p.muteexpiry - System.currentTimeMillis()));
                  }

                  toReturn.put("PlayingTime", Server.getTimeFor(p.playingTime));
                  toReturn.put("Reputation", p.getReputation());
                  if (p.title != null && p.title.getName(true) != null) {
                     toReturn.put("Title", p.title.getName(true));
                  }

                  if (p.isPriest) {
                     toReturn.put("Priest", Boolean.TRUE);
                  }
               } catch (IOException var9) {
                  logger.log(Level.WARNING, aPlayerID + ":" + var9.getMessage(), (Throwable)var9);
               } catch (NoSuchPlayerException var10) {
                  logger.log(Level.WARNING, aPlayerID + ":" + var10.getMessage(), (Throwable)var10);
               }
            }
         } else {
            toReturn.put("Not a player", String.valueOf(aPlayerID));
         }

         return toReturn;
      }
   }

   @Override
   public long getLocalCreationTime(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getLocalCreationTime");
      }

      return Server.getStartTime();
   }

   @Override
   public Map<Integer, String> getKingdoms(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getKingdoms");
      }

      synchronized(Server.SYNC_LOCK) {
         Map<Integer, String> toReturn = new HashMap<>();
         if (Servers.localServer.HOMESERVER) {
            toReturn.put(Integer.valueOf(Servers.localServer.KINGDOM), Kingdoms.getNameFor(Servers.localServer.KINGDOM));
         } else {
            toReturn.put(1, Kingdoms.getNameFor((byte)1));
            toReturn.put(3, Kingdoms.getNameFor((byte)3));
            toReturn.put(2, Kingdoms.getNameFor((byte)2));
         }

         return toReturn;
      }
   }

   @Override
   public Map<Long, String> getPlayersForKingdom(String intraServerPassword, int aKingdom) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayersForKingdom: " + aKingdom);
      }

      synchronized(Server.SYNC_LOCK) {
         Map<Long, String> toReturn = new HashMap<>();
         Player[] players = Players.getInstance().getPlayers();

         for(int x = 0; x < players.length; ++x) {
            if (players[x].getKingdomId() == aKingdom) {
               toReturn.put(new Long(players[x].getWurmId()), players[x].getName());
            }
         }

         return toReturn;
      }
   }

   @Override
   public long getPlayerId(String intraServerPassword, String name) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayerId for player name: " + name);
      }

      synchronized(Server.SYNC_LOCK) {
         return Players.getInstance().getWurmIdByPlayerName(LoginHandler.raiseFirstLetter(name));
      }
   }

   @Override
   public Map<String, ?> createPlayer(
      String intraServerPassword,
      String name,
      String password,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " createPlayer for player name: " + name);
      }

      appearance = (long)Server.rand.nextInt(5);
      this.faceRandom.setSeed(8263186381637L + appearance);
      appearance = this.faceRandom.nextLong();
      Map<String, Object> toReturn = new HashMap<>();
      logger.log(Level.INFO, "Trying to create player " + name);
      synchronized(Server.SYNC_LOCK) {
         if (isEmailValid(emailAddress)) {
            try {
               toReturn.put(
                  "PlayerId",
                  new Long(LoginHandler.createPlayer(name, password, challengePhrase, challengeAnswer, emailAddress, kingdom, power, appearance, gender))
               );
            } catch (Exception var16) {
               toReturn.put("PlayerId", -1L);
               toReturn.put("error", var16.getMessage());
               logger.log(Level.WARNING, name + ":" + var16.getMessage(), (Throwable)var16);
            }
         } else {
            toReturn.put("error", "The email address " + emailAddress + " is not valid.");
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, String> getPendingAccounts(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPendingAccounts");
      }

      Map<String, String> toReturn = new HashMap<>();

      for(Entry<String, PendingAccount> entry : PendingAccount.accounts.entrySet()) {
         toReturn.put(entry.getKey(), entry.getValue().emailAddress + ", " + GeneralUtilities.toGMTString(entry.getValue().expiration));
      }

      return toReturn;
   }

   @Override
   public Map<String, String> createPlayerPhaseOne(String intraServerPassword, String aPlayerName, String aEmailAddress) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("error", "The server is currently in maintenance mode.");
         return toReturn;
      } else {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase one " + aPlayerName);
         synchronized(Server.SYNC_LOCK) {
            aPlayerName = LoginHandler.raiseFirstLetter(aPlayerName);
            String errstat = LoginHandler.checkName2(aPlayerName);
            if (errstat.length() == 0) {
               if (PlayerInfoFactory.doesPlayerExist(aPlayerName)) {
                  toReturn.put("error", "The name " + aPlayerName + " is taken.");
                  return toReturn;
               }

               if (PendingAccount.doesPlayerExist(aPlayerName)) {
                  toReturn.put("error", "The name " + aPlayerName + " is reserved for up to two days.");
                  return toReturn;
               }

               if (!isEmailValid(aEmailAddress)) {
                  toReturn.put("error", "The email " + aEmailAddress + " is invalid.");
                  return toReturn;
               }

               String[] numAccounts = PlayerInfoFactory.getAccountsForEmail(aEmailAddress);
               if (numAccounts.length >= 5) {
                  String accnames = "";

                  for(int x = 0; x < numAccounts.length; ++x) {
                     accnames = accnames + " " + numAccounts[x];
                  }

                  toReturn.put("error", "You may only have 5 accounts. Please play Wurm with any of the following:" + accnames + ".");
                  return toReturn;
               }

               String[] numAccounts2 = PendingAccount.getAccountsForEmail(aEmailAddress);
               if (numAccounts2.length >= 5) {
                  String accnames = "";

                  for(int x = 0; x < numAccounts2.length; ++x) {
                     accnames = accnames + " " + numAccounts2[x];
                  }

                  toReturn.put(
                     "error",
                     "You may only have 5 accounts. The following accounts are awaiting confirmation by following the link in the verification email:"
                        + accnames
                        + "."
                  );
                  return toReturn;
               }

               for(String blocked : this.bannedMailHosts) {
                  if (aEmailAddress.toLowerCase().contains(blocked)) {
                     String domain = aEmailAddress.substring(aEmailAddress.indexOf("@"), aEmailAddress.length());
                     toReturn.put("error", "We do not accept email addresses from :" + domain + ".");
                     return toReturn;
                  }
               }

               if (numAccounts.length + numAccounts2.length >= 5) {
                  String accnames = "";

                  for(int x = 0; x < numAccounts.length; ++x) {
                     accnames = accnames + " " + numAccounts[x];
                  }

                  for(int x = 0; x < numAccounts2.length; ++x) {
                     accnames = accnames + " " + numAccounts2[x];
                  }

                  toReturn.put(
                     "error",
                     "You may only have 5 accounts. The following accounts are already registered or awaiting confirmation by following the link in the verification email:"
                        + accnames
                        + "."
                  );
                  return toReturn;
               }

               String password = generateRandomPassword();
               long expireTime = System.currentTimeMillis() + 172800000L;
               PendingAccount pedd = new PendingAccount();
               pedd.accountName = aPlayerName;
               pedd.emailAddress = aEmailAddress;
               pedd.expiration = expireTime;
               pedd.password = password;
               if (pedd.create()) {
                  try {
                     if (!Constants.devmode) {
                        String email = Mailer.getPhaseOneMail();
                        email = email.replace("@pname", aPlayerName);
                        email = email.replace("@email", URLEncoder.encode(aEmailAddress, "UTF-8"));
                        email = email.replace("@expiration", GeneralUtilities.toGMTString(expireTime));
                        email = email.replace("@password", password);
                        Mailer.sendMail(mailAccount, aEmailAddress, "Wurm Online character creation request", email);
                     } else {
                        toReturn.put("Hash", password);
                        logger.log(Level.WARNING, "NO MAIL SENT: DEVMODE ACTIVE");
                     }

                     toReturn.put(
                        "ok", "An email has been sent to " + aEmailAddress + ". You will have to click a link in order to proceed with the registration."
                     );
                  } catch (Exception var15) {
                     toReturn.put("error", "An error occured when sending the mail: " + var15.getMessage() + ". No account was reserved.");
                     pedd.delete();
                     logger.log(Level.WARNING, aEmailAddress + ":" + var15.getMessage(), (Throwable)var15);
                  }
               } else {
                  toReturn.put("error", "The account could not be created. Please try later.");
                  logger.warning(aEmailAddress + " The account could not be created. Please try later.");
               }
            } else {
               toReturn.put("error", errstat);
            }

            return toReturn;
         }
      }
   }

   @Override
   public Map<String, ?> createPlayerPhaseTwo(
      String intraServerPassword,
      String playerName,
      String hashedIngamePassword,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender,
      String phaseOneHash
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " createPlayerPhaseTwo for player name: " + playerName);
      }

      appearance = (long)Server.rand.nextInt(5);
      this.faceRandom.setSeed(8263186381637L + appearance);
      appearance = this.faceRandom.nextLong();
      return this.createPlayerPhaseTwo(
         intraServerPassword,
         playerName,
         hashedIngamePassword,
         challengePhrase,
         challengeAnswer,
         emailAddress,
         kingdom,
         power,
         appearance,
         gender,
         phaseOneHash,
         1
      );
   }

   @Override
   public Map<String, ?> createPlayerPhaseTwo(
      String intraServerPassword,
      String playerName,
      String hashedIngamePassword,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender,
      String phaseOneHash,
      int serverId
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      appearance = (long)Server.rand.nextInt(5);
      this.faceRandom.setSeed(8263186381637L + appearance);
      appearance = this.faceRandom.nextLong();
      return this.createPlayerPhaseTwo(
         intraServerPassword,
         playerName,
         hashedIngamePassword,
         challengePhrase,
         challengeAnswer,
         emailAddress,
         kingdom,
         power,
         appearance,
         gender,
         phaseOneHash,
         serverId,
         true
      );
   }

   @Override
   public Map<String, ?> createPlayerPhaseTwo(
      String intraServerPassword,
      String playerName,
      String hashedIngamePassword,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender,
      String phaseOneHash,
      int serverId,
      boolean optInEmail
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      int var35 = 1;
      appearance = (long)Server.rand.nextInt(5);
      this.faceRandom.setSeed(8263186381637L + appearance);
      appearance = this.faceRandom.nextLong();
      kingdom = 4;
      if (kingdom == 3) {
         var35 = 3;
      }

      Map<String, Object> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("error", "The server is currently in maintenance mode.");
         return toReturn;
      } else {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " Trying to create player phase two " + playerName);
         synchronized(Server.SYNC_LOCK) {
            if (playerName != null
               && hashedIngamePassword != null
               && challengePhrase != null
               && challengeAnswer != null
               && emailAddress != null
               && phaseOneHash != null) {
               if (challengePhrase.equals(challengeAnswer)) {
                  toReturn.put("error", "We don't allow the password retrieval question and answer to be the same.");
                  return toReturn;
               } else {
                  playerName = LoginHandler.raiseFirstLetter(playerName);
                  String errstat = LoginHandler.checkName2(playerName);
                  if (errstat.length() > 0) {
                     toReturn.put("error", errstat);
                     return toReturn;
                  } else if (PlayerInfoFactory.doesPlayerExist(playerName)) {
                     toReturn.put("error", "The name " + playerName + " is taken. Your reservation must have expired.");
                     return toReturn;
                  } else if (hashedIngamePassword.length() < 6 || hashedIngamePassword.length() > 40) {
                     toReturn.put("error", "The hashed password must contain at least 6 characters and maximum 40 characters.");
                     return toReturn;
                  } else if (challengePhrase.length() < 4 || challengePhrase.length() > 120) {
                     toReturn.put("error", "The challenge phrase must contain at least 4 characters and max 120 characters.");
                     return toReturn;
                  } else if (challengeAnswer.length() < 1 || challengeAnswer.length() > 20) {
                     toReturn.put("error", "The challenge answer must contain at least 1 character and max 20 characters.");
                     return toReturn;
                  } else if (emailAddress.length() > 125) {
                     toReturn.put("error", "The email address consists of too many characters.");
                     return toReturn;
                  } else {
                     if (isEmailValid(emailAddress)) {
                        try {
                           PendingAccount pacc = PendingAccount.getAccount(playerName);
                           if (pacc == null) {
                              toReturn.put("PlayerId", -1L);
                              toReturn.put(
                                 "error",
                                 "The verification is done too late or the name was never reserved. The name reservation expires after two days. Please try to create the player again."
                              );
                              return toReturn;
                           }

                           if (pacc.password.equals(phaseOneHash)) {
                              if (!pacc.emailAddress.toLowerCase().equals(emailAddress.toLowerCase())) {
                                 toReturn.put("PlayerId", -1L);
                                 toReturn.put("error", "The email supplied does not match with the one that was registered with the name.");
                                 return toReturn;
                              }

                              try {
                                 if (var35 == Servers.localServer.id) {
                                    toReturn.put(
                                       "PlayerId",
                                       new Long(
                                          LoginHandler.createPlayer(
                                             playerName,
                                             hashedIngamePassword,
                                             challengePhrase,
                                             challengeAnswer,
                                             emailAddress,
                                             kingdom,
                                             power,
                                             appearance,
                                             gender
                                          )
                                       )
                                    );
                                 } else if (Servers.localServer.LOGINSERVER) {
                                    ServerEntry toCreateOn = Servers.getServerWithId(var35);
                                    if (toCreateOn != null) {
                                       int tilex = toCreateOn.SPAWNPOINTJENNX;
                                       int tiley = toCreateOn.SPAWNPOINTJENNY;
                                       if (kingdom == 2) {
                                          tilex = toCreateOn.SPAWNPOINTMOLX;
                                          tiley = toCreateOn.SPAWNPOINTMOLY;
                                       }

                                       if (kingdom == 3) {
                                          tilex = toCreateOn.SPAWNPOINTLIBX;
                                          tiley = toCreateOn.SPAWNPOINTLIBY;
                                       }

                                       if (var35 == 5) {
                                          tilex = 2884;
                                          tiley = 3004;
                                       }

                                       LoginServerWebConnection lsw = new LoginServerWebConnection(var35);
                                       byte[] playerData = lsw.createAndReturnPlayer(
                                          playerName,
                                          hashedIngamePassword,
                                          challengePhrase,
                                          challengeAnswer,
                                          emailAddress,
                                          kingdom,
                                          power,
                                          appearance,
                                          gender,
                                          false,
                                          false,
                                          false
                                       );
                                       long wurmId = IntraServerConnection.savePlayerToDisk(playerData, tilex, tiley, true, true);
                                       toReturn.put("PlayerId", wurmId);
                                    } else {
                                       toReturn.put("PlayerId", -1L);
                                       toReturn.put("error", "Failed to create player " + playerName + ": The desired server does not exist.");
                                    }
                                 } else {
                                    toReturn.put("PlayerId", -1L);
                                    toReturn.put("error", "Failed to create player " + playerName + ": This is not a login server.");
                                 }
                              } catch (Exception var28) {
                                 logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + var28.getMessage(), (Throwable)var28);
                                 toReturn.put("PlayerId", -1L);
                                 toReturn.put("error", "Failed to create player " + playerName + ":" + var28.getMessage());
                                 return toReturn;
                              }

                              pacc.delete();

                              try {
                                 if (!Constants.devmode) {
                                    String mail = Mailer.getPhaseTwoMail();
                                    mail = mail.replace("@pname", playerName);
                                    Mailer.sendMail(mailAccount, emailAddress, "Wurm Online character creation success", mail);
                                 }
                              } catch (Exception var27) {
                                 logger.log(
                                    Level.WARNING,
                                    "Failed to send email to " + emailAddress + " for player " + playerName + ":" + var27.getMessage(),
                                    (Throwable)var27
                                 );
                                 toReturn.put("error", "Failed to send email to " + emailAddress + " for player " + playerName + ":" + var27.getMessage());
                              }
                           } else {
                              toReturn.put("PlayerId", -1L);
                              toReturn.put("error", "The verification hash does not match.");
                           }
                        } catch (Exception var29) {
                           logger.log(Level.WARNING, "Failed to create player " + playerName + "!" + var29.getMessage(), (Throwable)var29);
                           toReturn.put("PlayerId", -1L);
                           toReturn.put("error", var29.getMessage());
                        }
                     } else {
                        toReturn.put("error", "The email address " + emailAddress + " is not valid.");
                     }

                     return toReturn;
                  }
               }
            } else {
               if (playerName == null) {
                  toReturn.put("error", "PlayerName is null.");
               }

               if (hashedIngamePassword == null) {
                  toReturn.put("error", "hashedIngamePassword is null.");
               }

               if (challengePhrase == null) {
                  toReturn.put("error", "ChallengePhrase is null.");
               }

               if (challengeAnswer == null) {
                  toReturn.put("error", "ChallengeAnswer is null.");
               }

               if (emailAddress == null) {
                  toReturn.put("error", "EmailAddress is null.");
               }

               if (phaseOneHash == null) {
                  toReturn.put("error", "phaseOneHash is null.");
               }

               return toReturn;
            }
         }
      }
   }

   @Override
   public byte[] createAndReturnPlayer(
      String intraServerPassword,
      String playerName,
      String hashedIngamePassword,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender,
      boolean titleKeeper,
      boolean addPremium,
      boolean passwordIsHashed
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (Constants.maintaining) {
         throw new RemoteException("The server is currently in maintenance mode.");
      } else {
         try {
            appearance = (long)Server.rand.nextInt(5);
            this.faceRandom.setSeed(8263186381637L + appearance);
            appearance = this.faceRandom.nextLong();
            logger.log(Level.INFO, getClientHost() + " Received create attempt for " + playerName);
            return LoginHandler.createAndReturnPlayer(
               playerName,
               hashedIngamePassword,
               challengePhrase,
               challengeAnswer,
               emailAddress,
               kingdom,
               power,
               appearance,
               gender,
               titleKeeper,
               addPremium,
               passwordIsHashed
            );
         } catch (Exception var16) {
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
            throw new RemoteException(var16.getMessage());
         }
      }
   }

   @Override
   public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      byte executor = 6;
      boolean ok = true;
      String campaignId = "";
      name = LoginHandler.raiseFirstLetter(name);
      Map<String, String> toReturn = new HashMap<>();
      if (name == null || name.length() == 0) {
         toReturn.put("error", "Illegal name.");
         return toReturn;
      } else if (moneyToAdd <= 0L) {
         toReturn.put("error", "Invalid amount; must be greater than zero");
         return toReturn;
      } else {
         synchronized(Server.SYNC_LOCK) {
            try {
               Player p = Players.getInstance().getPlayer(name);
               p.addMoney(moneyToAdd);
               long money = p.getMoney();
               new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, executor, campaignId);
               Change change = new Change(moneyToAdd);
               Change current = new Change(money);
               p.save();
               toReturn.put(
                  "ok", "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + "."
               );
            } catch (NoSuchPlayerException var18) {
               try {
                  PlayerInfo px = PlayerInfoFactory.createPlayerInfo(name);
                  px.load();
                  if (px.wurmId > 0L) {
                     px.setMoney(px.money + moneyToAdd);
                     Change changex = new Change(moneyToAdd);
                     Change currentx = new Change(px.money);
                     px.save();
                     toReturn.put(
                        "ok",
                        "An amount of "
                           + changex.getChangeString()
                           + " has been added to the account. Current balance is "
                           + currentx.getChangeString()
                           + ". It may take a while to reach your server."
                     );
                     if (Servers.localServer.id != px.currentServer) {
                        new MoneyTransfer(name, px.wurmId, px.money, moneyToAdd, transactionDetail, executor, campaignId, false);
                     } else {
                        new MoneyTransfer(px.getName(), px.wurmId, px.money, moneyToAdd, transactionDetail, executor, campaignId);
                     }
                  } else {
                     toReturn.put("error", "No player found with the name " + name + ".");
                  }
               } catch (IOException var17) {
                  logger.log(Level.WARNING, name + ":" + var17.getMessage(), (Throwable)var17);
                  throw new RemoteException("An error occured. Please contact customer support.");
               }
            } catch (IOException var19) {
               logger.log(Level.WARNING, name + ":" + var19.getMessage(), (Throwable)var19);
               throw new RemoteException("An error occured. Please contact customer support.");
            } catch (Exception var20) {
               logger.log(Level.WARNING, name + ":" + var20.getMessage(), (Throwable)var20);
               throw new RemoteException("An error occured. Please contact customer support.");
            }

            return toReturn;
         }
      }
   }

   @Override
   public long getMoney(String intraServerPassword, long playerId, String playerName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(playerId);
      if (p == null) {
         p = PlayerInfoFactory.createPlayerInfo(playerName);

         try {
            p.load();
         } catch (IOException var7) {
            logger.log(Level.WARNING, "Failed to load pinfo for " + playerName);
         }

         if (p.wurmId <= 0L) {
            return 0L;
         }
      }

      return p != null ? p.money : 0L;
   }

   @Override
   public Map<String, String> reversePayment(
      String intraServerPassword,
      long moneyToRemove,
      int monthsToRemove,
      int daysToRemove,
      String reversalTransactionID,
      String originalTransactionID,
      String playerName
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();
      logger.log(
         Level.INFO,
         this.getRemoteClientDetails()
            + " Reverse payment for player name: "
            + playerName
            + ", reversalTransactionID: "
            + reversalTransactionID
            + ", originalTransactionID: "
            + originalTransactionID
      );

      try {
         PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);
         p.load();
         if (p.wurmId > 0L) {
            if (moneyToRemove > 0L) {
               if (p.money < moneyToRemove) {
                  Change lack = new Change(moneyToRemove - p.money);
                  toReturn.put("moneylack", "An amount of " + lack.getChangeString() + " was lacking from the account. Removing what we can.");
               }

               p.setMoney(Math.max(0L, p.money - moneyToRemove));
               Change change = new Change(moneyToRemove);
               Change current = new Change(p.money);
               p.save();
               toReturn.put(
                  "moneyok",
                  "An amount of " + change.getChangeString() + " has been removed from the account. Current balance is " + current.getChangeString() + "."
               );
               if (Servers.localServer.id != p.currentServer) {
                  new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "", false);
               } else {
                  new MoneyTransfer(playerName, p.wurmId, p.money, moneyToRemove, originalTransactionID, (byte)4, "");
               }
            }

            if (daysToRemove > 0 || monthsToRemove > 0) {
               long timeToRemove = 0L;
               if (daysToRemove > 0) {
                  timeToRemove = (long)daysToRemove * 86400000L;
               }

               if (monthsToRemove > 0) {
                  timeToRemove += (long)monthsToRemove * 86400000L * 30L;
               }

               long currTime = p.getPaymentExpire();
               currTime = Math.max(currTime, System.currentTimeMillis());
               currTime = Math.max(currTime - timeToRemove, System.currentTimeMillis());

               try {
                  p.setPaymentExpire(currTime);
                  String expireString = "The premier playing time has expired now.";
                  if (System.currentTimeMillis() < currTime) {
                     expireString = "The player now has premier playing time until "
                        + GeneralUtilities.toGMTString(currTime)
                        + ". Your in game player account will be updated shortly.";
                  }

                  p.save();
                  toReturn.put("timeok", expireString);
                  if (p.currentServer != Servers.localServer.id) {
                     new TimeTransfer(playerName, p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID, false);
                  } else {
                     new TimeTransfer(p.getName(), p.wurmId, -monthsToRemove, false, -daysToRemove, originalTransactionID);
                  }
               } catch (IOException var16) {
                  toReturn.put("timeerror", p.getName() + ": failed to set expire to " + currTime + ", " + var16.getMessage());
                  logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + var16.getMessage(), (Throwable)var16);
               }
            }
         } else {
            toReturn.put("error", "No player found with the name " + playerName + ".");
         }

         return toReturn;
      } catch (IOException var17) {
         logger.log(Level.WARNING, playerName + ":" + var17.getMessage(), (Throwable)var17);
         throw new RemoteException("An error occured. Please contact customer support.");
      }
   }

   @Override
   public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long moneyToAdd, String transactionDetail, boolean ingame) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " addMoneyToBank for player name: " + name);
      }

      return this.addMoneyToBank(intraServerPassword, name, -1L, moneyToAdd, transactionDetail, ingame);
   }

   public static String encryptMD5(String plaintext) throws Exception {
      MessageDigest md = null;

      try {
         md = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException var6) {
         throw new WurmServerException("No such algorithm 'MD5'", var6);
      }

      try {
         md.update(plaintext.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var5) {
         throw new WurmServerException("No such encoding: UTF-8", var5);
      }

      byte[] raw = md.digest();
      BigInteger bi = new BigInteger(1, raw);
      return bi.toString(16);
   }

   @Override
   public Map<String, String> addMoneyToBank(String intraServerPassword, String name, long wurmId, long moneyToAdd, String transactionDetail, boolean ingame) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      synchronized(Server.SYNC_LOCK) {
         Map<String, String> toReturn = new HashMap<>();
         if ((name == null || name.length() == 0) && wurmId <= 0L) {
            toReturn.put("error", "Illegal name.");
            return toReturn;
         } else if (moneyToAdd <= 0L) {
            toReturn.put("error", "Invalid amount; must be greater than zero");
            return toReturn;
         } else {
            if (name != null) {
               name = LoginHandler.raiseFirstLetter(name);
            }

            byte executor = 6;
            String campaignId = "";
            logger.log(Level.INFO, this.getRemoteClientDetails() + " Add money to bank 2 , " + moneyToAdd + " for player name: " + name + ", wid " + wurmId);
            if (name != null && name.length() > 0 || wurmId > 0L) {
               try {
                  Player p = null;
                  if (wurmId <= 0L) {
                     p = Players.getInstance().getPlayer(name);
                  } else {
                     p = Players.getInstance().getPlayer(wurmId);
                  }

                  p.addMoney(moneyToAdd);
                  long money = p.getMoney();
                  if (!ingame) {
                     new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, (byte)6, "");
                  }

                  Change change = new Change(moneyToAdd);
                  Change current = new Change(money);
                  p.save();
                  toReturn.put(
                     "ok",
                     "An amount of " + change.getChangeString() + " has been added to the account. Current balance is " + current.getChangeString() + "."
                  );
               } catch (NoSuchPlayerException var20) {
                  try {
                     PlayerInfo p = null;
                     if (name != null && name.length() > 0) {
                        p = PlayerInfoFactory.createPlayerInfo(name);
                     } else {
                        p = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
                     }

                     if (p != null) {
                        p.load();
                        if (p.wurmId > 0L) {
                           p.setMoney(p.money + moneyToAdd);
                           Change change = new Change(moneyToAdd);
                           Change current = new Change(p.money);
                           p.save();
                           toReturn.put(
                              "ok",
                              "An amount of "
                                 + change.getChangeString()
                                 + " has been added to the account. Current balance is "
                                 + current.getChangeString()
                                 + ". It may take a while to reach your server."
                           );
                           if (!ingame) {
                              if (Servers.localServer.id != p.currentServer) {
                                 new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)6, "", false);
                              } else {
                                 new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)6, "");
                              }
                           }
                        } else {
                           toReturn.put("error", "No player found with the wurmid " + p.wurmId + ".");
                        }
                     } else {
                        toReturn.put("error", "No player found with the name " + name + ".");
                     }
                  } catch (IOException var19) {
                     logger.log(Level.WARNING, name + ": " + wurmId + "," + var19.getMessage(), (Throwable)var19);
                     throw new RemoteException("An error occured. Please contact customer support.");
                  }
               } catch (IOException var21) {
                  logger.log(Level.WARNING, name + ":" + wurmId + "," + var21.getMessage(), (Throwable)var21);
                  throw new RemoteException("An error occured. Please contact customer support.");
               } catch (Exception var22) {
                  logger.log(Level.WARNING, name + ":" + wurmId + "," + var22.getMessage(), (Throwable)var22);
                  throw new RemoteException("An error occured. Please contact customer support.");
               }
            }

            return toReturn;
         }
      }
   }

   @Override
   public long chargeMoney(String intraServerPassword, String playerName, long moneyToCharge) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.log(Level.INFO, this.getRemoteClientDetails() + " ChargeMoney for player name: " + playerName + ", money: " + moneyToCharge);
      if (Servers.localServer.id == Servers.loginServer.id) {
         PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

         try {
            p.load();
            if (p.money > 0L) {
               if (p.money - moneyToCharge < 0L) {
                  return -10L;
               } else {
                  p.setMoney(p.money - moneyToCharge);
                  logger.info(playerName + " was charged " + moneyToCharge + " and now has " + p.money);
                  return p.money;
               }
            } else {
               return -10L;
            }
         } catch (IOException var7) {
            logger.log(Level.WARNING, playerName + ": " + var7.getMessage(), (Throwable)var7);
            return -10L;
         }
      } else {
         logger.warning(playerName + " cannot charge " + moneyToCharge + " as this server is not the login server");
         return -10L;
      }
   }

   @Override
   public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      return this.addPlayingTime(intraServerPassword, name, months, days, transactionDetail, true);
   }

   @Override
   public Map<String, String> addPlayingTime(String intraServerPassword, String name, int months, int days, String transactionDetail, boolean addSleepPowder) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      synchronized(Server.SYNC_LOCK) {
         Map<String, String> toReturn = new HashMap<>();
         if (name == null || name.length() == 0 || transactionDetail == null || transactionDetail.length() == 0) {
            toReturn.put("error", "Illegal arguments. Check if name or transaction detail is null or empty strings.");
            return toReturn;
         } else if (months >= 0 && days >= 0) {
            boolean ok = true;
            logger.log(
               Level.INFO,
               this.getRemoteClientDetails()
                  + " Addplayingtime for player name: "
                  + name
                  + ", months: "
                  + months
                  + ", days: "
                  + days
                  + ", transactionDetail: "
                  + transactionDetail
            );
            SimpleDateFormat formatter = new SimpleDateFormat("yy.MM.dd'-'hh:mm:ss");
            Object var45;
            synchronized(Server.SYNC_LOCK) {
               long timeToAdd = 0L;
               if (days != 0) {
                  timeToAdd = (long)days * 86400000L;
               }

               if (months != 0) {
                  timeToAdd += (long)months * 86400000L * 30L;
               }

               try {
                  Player p = Players.getInstance().getPlayer(name);
                  long currTime = p.getPaymentExpire();
                  if (timeToAdd > 0L) {
                     if (currTime <= 0L) {
                        Server.addNewPlayer(p.getName());
                     } else {
                        Server.incrementOldPremiums(p.getName());
                     }
                  }

                  currTime = Math.max(currTime, System.currentTimeMillis());
                  currTime += timeToAdd;

                  try {
                     p.getSaveFile().setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
                     new TimeTransfer(p.getName(), p.getWurmId(), months, addSleepPowder, days, transactionDetail);
                  } catch (IOException var27) {
                     logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + var27.getMessage(), (Throwable)var27);
                  }

                  String expireString = "You now have premier playing time until " + formatter.format(new Date(currTime)) + ".";
                  p.save();
                  toReturn.put("ok", expireString);
                  Message mess = new Message(null, (byte)3, ":Event", expireString);
                  mess.setReceiver(p.getWurmId());
                  Server.getInstance().addMessage(mess);
                  logger.info(p.getName() + ' ' + expireString);
                  if (addSleepPowder) {
                     try {
                        Item inventory = p.getInventory();

                        for(int x = 0; x < months; ++x) {
                           Item i = ItemFactory.createItem(666, 99.0F, "");
                           inventory.insertItem(i, true);
                        }

                        logger.log(Level.INFO, "Inserted " + months + " sleep powder in " + p.getName() + " inventory " + inventory.getWurmId());
                        Message rmess = new Message(null, (byte)3, ":Event", "You have received " + months + " sleeping powders in your inventory.");
                        rmess.setReceiver(p.getWurmId());
                        Server.getInstance().addMessage(rmess);
                     } catch (Exception var28) {
                        logger.log(Level.INFO, var28.getMessage(), (Throwable)var28);
                     }
                  }

                  return toReturn;
               } catch (NoSuchPlayerException var29) {
               } catch (IOException var30) {
                  logger.log(Level.WARNING, name + ":" + var30.getMessage(), (Throwable)var30);
                  throw new RemoteException("An error occured. Please contact customer support.");
               } catch (Exception var31) {
                  logger.log(Level.WARNING, name + ":" + var31.getMessage(), (Throwable)var31);
                  throw new RemoteException("An error occured. Please contact customer support.");
               }

               try {
                  PlayerInfo p = PlayerInfoFactory.createPlayerInfo(name);
                  p.load();
                  if (p.wurmId <= 0L) {
                     toReturn.put("error", "No player found with the name " + name + ".");
                     var45 = toReturn;
                  } else {
                     long currTime = p.getPaymentExpire();
                     if (timeToAdd > 0L) {
                        if (currTime <= 0L) {
                           Server.addNewPlayer(p.getName());
                        } else {
                           Server.incrementOldPremiums(p.getName());
                        }
                     }

                     currTime = Math.max(currTime, System.currentTimeMillis());
                     currTime += timeToAdd;

                     try {
                        p.setPaymentExpire(currTime, !transactionDetail.startsWith("firstBuy"));
                     } catch (IOException var26) {
                        logger.log(Level.WARNING, p.getName() + ": failed to set expire to " + currTime + ", " + var26.getMessage(), (Throwable)var26);
                     }

                     ServerEntry entry = Servers.getServerWithId(p.currentServer);
                     String expireString = "Your premier playing time has expired now.";
                     if (System.currentTimeMillis() < currTime) {
                        if (entry.entryServer) {
                           expireString = "You now have premier playing time until "
                              + formatter.format(new Date(currTime))
                              + ". Your in game player account will be updated shortly. NOTE that you will have to use a portal to get to the premium servers in order to benefit from it.";
                        } else {
                           expireString = "You now have premier playing time until "
                              + formatter.format(new Date(currTime))
                              + ". Your in game player account will be updated shortly.";
                        }
                     }

                     p.save();
                     toReturn.put("ok", expireString);
                     logger.info(p.getName() + ' ' + expireString);
                     if (p.currentServer != Servers.localServer.id) {
                        new TimeTransfer(name, p.wurmId, months, addSleepPowder, days, transactionDetail, false);
                     } else {
                        new TimeTransfer(p.getName(), p.wurmId, months, addSleepPowder, days, transactionDetail);
                        if (addSleepPowder) {
                           try {
                              long inventoryId = DbCreatureStatus.getInventoryIdFor(p.wurmId);

                              for(int x = 0; x < months; ++x) {
                                 Item i = ItemFactory.createItem(666, 99.0F, "");
                                 i.setParentId(inventoryId, true);
                                 i.setOwnerId(p.wurmId);
                              }

                              logger.log(Level.INFO, "Inserted " + months + " sleep powder in offline " + p.getName() + " inventory " + inventoryId);
                           } catch (Exception var32) {
                              logger.log(Level.INFO, var32.getMessage(), (Throwable)var32);
                           }
                        }
                     }

                     return toReturn;
                  }
               } catch (IOException var33) {
                  logger.log(Level.WARNING, name + ":" + var33.getMessage(), (Throwable)var33);
                  throw new RemoteException("An error occured. Please contact customer support.");
               }
            }

            return (Map<String, String>)var45;
         } else {
            toReturn.put("error", "Illegal arguments. Make sure that the values for days and months are not negative.");
            return toReturn;
         }
      }
   }

   @Override
   public Map<Integer, String> getDeeds(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getDeeds");
      }

      Map<Integer, String> toReturn = new HashMap<>();
      Village[] vills = Villages.getVillages();

      for(int x = 0; x < vills.length; ++x) {
         toReturn.put(vills[x].id, vills[x].getName());
      }

      return toReturn;
   }

   @Override
   public Map<String, ?> getDeedSummary(String intraServerPassword, int aVillageID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getDeedSummary for villageID: " + aVillageID);
      }

      try {
         Village village = Villages.getVillage(aVillageID);
         Map<String, Object> toReturn = new HashMap<>();
         toReturn.put("Villageid", village.getId());
         toReturn.put("Deedid", village.getDeedId());
         toReturn.put("Name", village.getName());
         toReturn.put("Motto", village.getMotto());
         toReturn.put("Location", Kingdoms.getNameFor(village.kingdom));
         toReturn.put("Size", (village.getEndX() - village.getStartX()) / 2);
         toReturn.put("Founder", village.getFounderName());
         toReturn.put("Mayor", village.mayorName);
         if (village.disband > 0L) {
            toReturn.put("Disbanding in", Server.getTimeFor(village.disband - System.currentTimeMillis()));
            toReturn.put("Disbander", Players.getInstance().getNameFor(village.disbander));
         }

         toReturn.put("Citizens", village.citizens.size());
         toReturn.put("Allies", village.getAllies().length);
         if (village.guards != null) {
            toReturn.put("guards", village.guards.size());
         }

         try {
            short[] sp = village.getTokenCoords();
            toReturn.put("Token Coord x", Integer.valueOf(sp[0]));
            toReturn.put("Token Coord y", Integer.valueOf(sp[1]));
         } catch (NoSuchItemException var6) {
         }

         return toReturn;
      } catch (Exception var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         throw new RemoteException(var7.getMessage());
      }
   }

   @Override
   public Map<String, Long> getPlayersForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayersForDeed for villageID: " + aVillageID);
      }

      Map<String, Long> toReturn = new HashMap<>();

      try {
         Village village = Villages.getVillage(aVillageID);
         Citizen[] citizens = village.getCitizens();

         for(int x = 0; x < citizens.length; ++x) {
            if (WurmId.getType(citizens[x].getId()) == 0) {
               try {
                  toReturn.put(Players.getInstance().getNameFor(citizens[x].getId()), new Long(citizens[x].getId()));
               } catch (NoSuchPlayerException var8) {
               }
            }
         }

         return toReturn;
      } catch (Exception var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         throw new RemoteException(var9.getMessage());
      }
   }

   @Override
   public Map<String, Integer> getAlliesForDeed(String intraServerPassword, int aVillageID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getAlliesForDeed for villageID: " + aVillageID);
      }

      Map<String, Integer> toReturn = new HashMap<>();

      try {
         Village village = Villages.getVillage(aVillageID);
         Village[] allies = village.getAllies();

         for(int x = 0; x < allies.length; ++x) {
            toReturn.put(allies[x].getName(), allies[x].getId());
         }

         return toReturn;
      } catch (Exception var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         throw new RemoteException(var7.getMessage());
      }
   }

   @Override
   public String[] getHistoryForDeed(String intraServerPassword, int villageID, int maxLength) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getHistoryForDeed for villageID: " + villageID + ", maxLength: " + maxLength);
      }

      try {
         Village village = Villages.getVillage(villageID);
         return village.getHistoryAsStrings(maxLength);
      } catch (Exception var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         throw new RemoteException(var5.getMessage());
      }
   }

   @Override
   public String[] getAreaHistory(String intraServerPassword, int maxLength) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getAreaHistory maxLength: " + maxLength);
      }

      return HistoryManager.getHistory(maxLength);
   }

   @Override
   public Map<String, ?> getItemSummary(String intraServerPassword, long aWurmID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getItemSummary for WurmId: " + aWurmID);
      }

      Map<String, Object> toReturn = new HashMap<>();

      try {
         Item item = Items.getItem(aWurmID);
         toReturn.put("WurmId", new Long(aWurmID));
         toReturn.put("Name", item.getName());
         toReturn.put("QL", String.valueOf(item.getQualityLevel()));
         toReturn.put("DMG", String.valueOf(item.getDamage()));
         toReturn.put("SizeX", String.valueOf(item.getSizeX()));
         toReturn.put("SizeY", String.valueOf(item.getSizeY()));
         toReturn.put("SizeZ", String.valueOf(item.getSizeZ()));
         if (item.getOwnerId() != -10L) {
            toReturn.put("Owner", new Long(item.getOwnerId()));
         } else {
            toReturn.put("Last owner", new Long(item.lastOwner));
         }

         toReturn.put("Coord x", (int)item.getPosX() >> 2);
         toReturn.put("Coord y", (int)item.getPosY() >> 2);
         toReturn.put("Creator", item.creator);
         toReturn.put("Creationdate", WurmCalendar.getTimeFor(item.creationDate));
         toReturn.put("Description", item.getDescription());
         toReturn.put("Material", Item.getMaterialString(item.getMaterial()));
         return toReturn;
      } catch (Exception var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
         throw new RemoteException(var6.getMessage());
      }
   }

   @Override
   public Map<String, String> getPlayerIPAddresses(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayerIPAddresses");
      }

      Map<String, String> toReturn = new HashMap<>();
      Player[] playerArr = Players.getInstance().getPlayersByIp();

      for(int x = 0; x < playerArr.length; ++x) {
         if (playerArr[x].getSaveFile().getPower() == 0) {
            toReturn.put(playerArr[x].getName(), playerArr[x].getSaveFile().getIpaddress());
         }
      }

      return toReturn;
   }

   @Override
   public Map<String, String> getNameBans(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getNameBans");
      }

      Map<String, String> toReturn = new HashMap<>();
      Ban[] bips = Players.getInstance().getPlayersBanned();
      if (bips.length > 0) {
         for(int x = 0; x < bips.length; ++x) {
            long daytime = bips[x].getExpiry() - System.currentTimeMillis();
            toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
         }
      }

      return toReturn;
   }

   @Override
   public Map<String, String> getIPBans(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getIPBans");
      }

      Map<String, String> toReturn = new HashMap<>();
      Ban[] bips = Players.getInstance().getBans();
      if (bips.length > 0) {
         for(int x = 0; x < bips.length; ++x) {
            long daytime = bips[x].getExpiry() - System.currentTimeMillis();
            toReturn.put(bips[x].getIdentifier(), Server.getTimeFor(daytime) + ", " + bips[x].getReason());
         }
      }

      return toReturn;
   }

   @Override
   public Map<String, String> getWarnings(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getWarnings");
      }

      Map<String, String> toReturn = new HashMap<>();
      toReturn.put("Not implemented", "Need a name to check.");
      return toReturn;
   }

   @Override
   public String getWurmTime(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getWurmTime");
      }

      return WurmCalendar.getTime();
   }

   @Override
   public String getUptime(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getUptime");
      }

      return Server.getTimeFor(System.currentTimeMillis() - Server.getStartTime());
   }

   @Override
   public String getNews(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getNews");
      }

      return NewsInfo.getInfo();
   }

   @Override
   public String getGameInfo(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getGameInfo");
      }

      return WurmInfo.getInfo() + WurmInfo2.getInfo();
   }

   @Override
   public Map<String, String> getKingdomInfluence(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getKingdomInfluence");
      }

      Map<String, String> toReturn = new HashMap<>();
      Zones.calculateZones(false);
      Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

      for(int x = 0; x < kingdoms.length; ++x) {
         toReturn.put("Percent controlled by " + kingdoms[x].getName(), twoDecimals.format((double)Zones.getPercentLandForKingdom(kingdoms[x].getId())));
      }

      return toReturn;
   }

   @Override
   public Map<String, ?> getMerchantSummary(String intraServerPassword, long aWurmID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getMerchantSummary for WurmID: " + aWurmID);
      }

      Map<String, Object> toReturn = new HashMap<>();
      toReturn.put("Not implemented", "not yet");
      return toReturn;
   }

   @Override
   public Map<String, ?> getBankAccount(String intraServerPassword, long aPlayerID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getBankAccount for playerid: " + aPlayerID);
      }

      Map<String, Object> toReturn = new HashMap<>();
      logger.log(Level.INFO, "GetBankAccount " + aPlayerID);

      try {
         Bank lBank = Banks.getBank(aPlayerID);
         if (lBank != null) {
            toReturn.put("BankID", lBank.id);
            toReturn.put("Owner", lBank.owner);
            toReturn.put("StartedMoving", lBank.startedMoving);
            toReturn.put("Open", lBank.open);
            toReturn.put("Size", lBank.size);

            try {
               Village lCurrentVillage = lBank.getCurrentVillage();
               if (lCurrentVillage != null) {
                  toReturn.put("CurrentVillageID", lCurrentVillage.getId());
                  toReturn.put("CurrentVillageName", lCurrentVillage.getName());
               }
            } catch (BankUnavailableException var11) {
            }

            int lTargetVillageID = lBank.targetVillage;
            if (lTargetVillageID > 0) {
               toReturn.put("TargetVillageID", lTargetVillageID);
            }

            BankSlot[] lSlots = lBank.slots;
            if (lSlots != null && lSlots.length > 0) {
               Map<Long, String> lItemsMap = new HashMap<>(lSlots.length + 1);

               for(int i = 0; i < lSlots.length; ++i) {
                  if (lSlots[i] == null) {
                     logger.log(Level.INFO, "Weird. Bank Slot " + i + " is null for " + aPlayerID);
                  } else {
                     Item lItem = lSlots[i].item;
                     if (lItem != null) {
                        lItemsMap.put(lItem.getWurmId(), lItem.getName() + ", Inserted: " + lSlots[i].inserted + ", Stasis: " + lSlots[i].stasis);
                     }
                  }
               }

               if (lItemsMap != null && lItemsMap.size() > 0) {
                  toReturn.put("Items", lItemsMap);
               }
            }
         } else {
            toReturn.put("Error", "Cannot find bank for player ID " + aPlayerID);
         }
      } catch (RuntimeException var12) {
         logger.log(Level.WARNING, "Error: " + var12.getMessage(), (Throwable)var12);
         toReturn.put("Error", "Problem getting bank account for player ID " + aPlayerID + ", " + var12);
      }

      return toReturn;
   }

   @Override
   public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword, Map params) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
      }

      Map<String, Object> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("ResponseCode0", "NOTOK");
         toReturn.put("ErrorMessage0", "The server is currently unavailable.");
         toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
         return toReturn;
      } else {
         try {
            boolean ver = false;
            Object answer = params.get("VerifiedPayPalAccount");
            if (answer != null && answer instanceof Boolean) {
               ver = (Boolean)answer;
            }

            boolean rev = false;
            answer = params.get("ChargebackOrReversal");
            if (answer != null && answer instanceof Boolean) {
               rev = (Boolean)answer;
            }

            Date lastReversal = (Date)params.get("LastChargebackOrReversal");
            Date first = (Date)params.get("FirstTransactionDate");
            Date last = (Date)params.get("LastTransactionDate");
            int total = 0;
            answer = params.get("TotalEurosSuccessful");
            if (answer != null && answer instanceof Integer) {
               total = (Integer)answer;
               if (total < 0) {
                  int var26 = false;
               }
            }

            int lastMonthEuros = 0;
            answer = params.get("LastMonthEurosSuccessful");
            if (answer != null && answer instanceof Integer) {
               lastMonthEuros = (Integer)answer;
               if (lastMonthEuros < 0) {
                  lastMonthEuros = 0;
               }
            }

            String ipAddress = (String)params.get("IP");
            if (ipAddress != null) {
               logger.log(Level.INFO, "IP:" + ipAddress);
               Long lastAttempt = ipAttempts.get(ipAddress);
               if (lastAttempt != null && System.currentTimeMillis() - lastAttempt < 5000L) {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "Too many logon attempts. Please try again in a few seconds.");
                  toReturn.put("display_text0", "Too many logon attempts. Please try again in a few seconds.");
                  return toReturn;
               }

               ipAttempts.put(ipAddress, System.currentTimeMillis());
            }

            PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            if (file.undeadType != 0) {
               toReturn.put("ResponseCode0", "NOTOK");
               toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
               toReturn.put("display_text0", "Undeads not allowed in here!");
               return toReturn;
            } else {
               try {
                  file.load();
                  if (file.undeadType != 0) {
                     toReturn.put("ResponseCode0", "NOTOK");
                     toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                     toReturn.put("display_text0", "Undeads not allowed in here!");
                     return toReturn;
                  }
               } catch (IOException var19) {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "An error occurred when loading your account.");
                  toReturn.put("display_text0", "An error occurred when loading your account.");
                  logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
                  return toReturn;
               }

               if (file.overRideShop || !rev || lastReversal != null && last != null && !lastReversal.after(last)) {
                  toReturn = this.authenticateUser(intraServerPassword, playerName, emailAddress, hashedIngamePassword);
                  Integer max = (Integer)toReturn.get("maximum_silver0");
                  if (max != null) {
                     int maxval = max;
                     if (file.overRideShop) {
                        maxval = 50 + Math.min(50, (int)(file.playingTime / 3600000L * 3L));
                        toReturn.put("maximum_silver0", maxval);
                     } else if (lastMonthEuros >= 400) {
                        int var29 = 0;
                        toReturn.put("maximum_silver0", Integer.valueOf(var29));
                        toReturn.put("display_text0", "You may only purchase 400 silver via PayPal per month");
                     }
                  }

                  return toReturn;
               } else {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "This paypal account has reversed transactions registered.");
                  toReturn.put("display_text0", "This paypal account has reversed transactions registered.");
                  return toReturn;
               }
            }
         } catch (Exception var20) {
            logger.log(Level.WARNING, "Error: " + var20.getMessage(), (Throwable)var20);
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "An error occured.");
            return toReturn;
         }
      }
   }

   @Override
   public Map<String, String> doesPlayerExist(String intraServerPassword, String playerName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " doesPlayerExist for player name: " + playerName);
      }

      Map<String, String> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("ResponseCode", "NOTOK");
         toReturn.put("ErrorMessage", "The server is currently unavailable.");
         toReturn.put("display_text", "The server is currently unavailable.");
         return toReturn;
      } else {
         toReturn.put("ResponseCode", "OK");
         if (playerName != null) {
            PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);

            try {
               file.load();
               if (file.wurmId <= 0L) {
                  toReturn.clear();
                  toReturn.put("ResponseCode", "NOTOK");
                  toReturn.put(
                     "ErrorMessage", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity."
                  );
                  toReturn.put(
                     "display_text", "No such player on the " + Servers.localServer.name + " game server. Maybe it has been deleted due to inactivity."
                  );
               }
            } catch (Exception var6) {
               toReturn.clear();
               toReturn.put("ResponseCode", "NOTOK");
               toReturn.put("ErrorMessage", var6.getMessage());
               toReturn.put("display_text", "An error occurred on the " + Servers.localServer.name + " game server: " + var6.getMessage());
            }
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, ?> authenticateUser(String intraServerPassword, String playerName, String emailAddress, String hashedIngamePassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " authenticateUser for player name: " + playerName);
      }

      Map<String, Object> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("ResponseCode0", "NOTOK");
         toReturn.put("ErrorMessage0", "The server is currently unavailable.");
         toReturn.put("display_text0", "The server is in maintenance mode. Please try later.");
         return toReturn;
      } else {
         if (playerName != null) {
            PlayerInfo file = PlayerInfoFactory.createPlayerInfo(playerName);
            if (file.undeadType != 0) {
               toReturn.put("ResponseCode0", "NOTOK");
               toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
               toReturn.put("display_text0", "Undeads not allowed in here!");
               return toReturn;
            }

            try {
               file.load();
               if (file.undeadType != 0) {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "Undeads not allowed in here!");
                  toReturn.put("display_text0", "Undeads not allowed in here!");
                  return toReturn;
               }

               if (file.wurmId <= 0L) {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "No such player.");
               } else if (hashedIngamePassword.equals(file.getPassword())) {
                  if (Servers.isThisLoginServer()) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection(file.currentServer);
                     Map<String, String> m = lsw.doesPlayerExist(playerName);
                     String resp = m.get("ResponseCode");
                     if (resp != null && resp.equals("NOTOK")) {
                        toReturn.put("ResponseCode0", "NOTOK");
                        toReturn.put("ErrorMessage0", m.get("ErrorMessage"));
                        toReturn.put("display_text0", m.get("display_text"));
                        return toReturn;
                     }
                  }

                  toReturn.put("ErrorMessage0", "");
                  if (file.getPaymentExpire() < 0L) {
                     toReturn.put(
                        "display_text0",
                        "You are new to the game and may give away an in-game referral to the person who introduced you to Wurm Online using the chat command '/refer' if you purchase premium game time."
                     );
                  } else {
                     toReturn.put("display_text0", "Don't forget to use the in-game '/refer' chat command to refer the one who introduced you to Wurm Online.");
                  }

                  if (file.getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                     toReturn.put(
                        "display_text0", "You have less than a week left of premium game time so the amount of coins you can purchase is somewhat limited."
                     );
                     toReturn.put("maximum_silver0", 10);
                  } else {
                     toReturn.put("maximum_silver0", 20 + Math.min(100, (int)(file.playingTime / 3600000L * 3L)));
                  }

                  if (!file.overRideShop && file.isBanned()) {
                     toReturn.put("PurchaseOk0", "NOTOK");
                     toReturn.put("maximum_silver0", 0);
                     toReturn.put("display_text0", "You have been banned. Reason: " + file.banreason);
                     toReturn.put("ErrorMessage0", "The player has been banned. Reason: " + file.banreason);
                  } else {
                     toReturn.put("PurchaseOk0", "OK");
                  }

                  int maxMonths = 0;
                  if (file.getPaymentExpire() > System.currentTimeMillis()) {
                     long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - file.getPaymentExpire();
                     maxMonths = (int)(maxMonthsMillis / 2419200000L);
                     if (maxMonths < 0) {
                        maxMonths = 0;
                     }
                  } else {
                     maxMonths = 12;
                  }

                  toReturn.put("maximum_months0", maxMonths);
                  toReturn.put("new_customer0", file.getPaymentExpire() <= 0L);
                  toReturn.put("ResponseCode0", "OK");
                  toReturn.put("PlayerID0", new Long(file.wurmId));
                  toReturn.put("ingameBankBalance0", new Long(file.money));
                  toReturn.put("PlayingTimeExpire0", new Long(file.getPaymentExpire()));
               } else {
                  toReturn.put("ResponseCode0", "NOTOK");
                  toReturn.put("ErrorMessage0", "Password does not match.");
               }
            } catch (Exception var11) {
               toReturn.put("ResponseCode0", "NOTOK");
               toReturn.put("ErrorMessage0", var11.getMessage());
               logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
            }
         } else if (isEmailValid(emailAddress)) {
            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);

            for(int x = 0; x < infos.length; ++x) {
               if (infos[x].getPassword().equals(hashedIngamePassword)) {
                  toReturn.put("ErrorMessage" + x, "");
                  if (infos[x].getPaymentExpire() < System.currentTimeMillis() + 604800000L) {
                     toReturn.put("maximum_silver" + x, 10);
                  } else {
                     toReturn.put("maximum_silver" + x, 10 + Math.min(100, (int)(infos[x].playingTime / 86400000L)));
                  }

                  if (!infos[x].overRideShop && infos[x].isBanned()) {
                     toReturn.put("PurchaseOk" + x, "NOTOK");
                     toReturn.put("maximum_silver" + x, 0);
                     toReturn.put("display_text" + x, "You have been banned. Reason: " + infos[x].banreason);
                     toReturn.put("ErrorMessage" + x, "The player has been banned. Reason: " + infos[x].banreason);
                  } else {
                     toReturn.put("PurchaseOk" + x, "OK");
                  }

                  int maxMonths = 0;
                  if (infos[x].getPaymentExpire() > System.currentTimeMillis()) {
                     long maxMonthsMillis = System.currentTimeMillis() + 36288000000L - infos[x].getPaymentExpire();
                     maxMonths = (int)(maxMonthsMillis / 2419200000L);
                     if (maxMonths < 0) {
                        maxMonths = 0;
                     }
                  } else {
                     maxMonths = 12;
                  }

                  toReturn.put("maximum_months" + x, maxMonths);
                  toReturn.put("new_customer" + x, infos[x].getPaymentExpire() <= 0L);
                  toReturn.put("ResponseCode" + x, "OK");
                  toReturn.put("PlayerID" + x, new Long(infos[x].wurmId));
                  toReturn.put("ingameBankBalance" + x, new Long(infos[x].money));
                  toReturn.put("PlayingTimeExpire" + x, new Long(infos[x].getPaymentExpire()));
               } else {
                  toReturn.put("ResponseCode" + x, "NOTOK");
                  toReturn.put("ErrorMessage" + x, "Password does not match.");
               }
            }
         } else {
            toReturn.put("ResponseCode0", "NOTOK");
            toReturn.put("ErrorMessage0", "Invalid email: " + emailAddress);
         }

         return toReturn;
      }
   }

   @Override
   public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String newPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();

      try {
         toReturn.put("Result", "Unknown email.");
         logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword Name: " + playerName + ", email: " + emailAddress);
         if (emailAddress != null) {
            if (isEmailValid(emailAddress)) {
               PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
               int nums = 0;

               for(int x = 0; x < infos.length; ++x) {
                  if (infos[x].getPower() == 0) {
                     try {
                        infos[x].updatePassword(newPassword);
                        if (infos[x].currentServer != Servers.localServer.id) {
                           new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                        }

                        toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                     } catch (IOException var17) {
                        logger.log(Level.WARNING, "Failed to update password for " + infos[x].getName(), (Throwable)var17);
                        toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
                     }
                  } else {
                     toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
                     logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x].getPower());
                  }
               }

               if (nums > 0) {
                  toReturn.put("Result", nums + " player accounts were affected.");
               } else {
                  toReturn.put("Error", nums + " player accounts were affected.");
               }

               return toReturn;
            }

            toReturn.put("Error", emailAddress + " is an invalid email.");
         } else if (playerName != null) {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

            try {
               p.load();
               if (isEmailValid(p.emailAddress)) {
                  emailAddress = p.emailAddress;
                  PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
                  int nums = 0;
                  boolean failed = false;

                  for(int x = 0; x < infos.length; ++x) {
                     if (infos[x].getPower() == 0) {
                        try {
                           infos[x].updatePassword(newPassword);
                           if (infos[x].currentServer != Servers.localServer.id) {
                              new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                           }

                           toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                        } catch (IOException var18) {
                           failed = true;
                           toReturn.put("Error" + nums, "Failed to update password for a player.");
                        }
                     } else {
                        failed = true;
                        logger.warning("Failed to update password for " + infos[x].getName() + " as power is " + infos[x].getPower());
                     }
                  }

                  if (nums > 0) {
                     toReturn.put("Result", nums + " player accounts were affected.");
                  } else {
                     toReturn.put("Error", nums + " player accounts were affected.");
                  }

                  if (failed) {
                     logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                  }

                  return toReturn;
               }

               toReturn.put("Error", emailAddress + " is an invalid email.");
            } catch (IOException var19) {
               toReturn.put("Error", "Failed to load player data. Password not changed.");
               logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
            }
         }

         return toReturn;
      } finally {
         logger.info("Changepassword Name: " + playerName + ", email: " + emailAddress + ", exit: " + toReturn);
      }
   }

   @Override
   public Map<String, String> changePassword(String intraServerPassword, String playerName, String emailAddress, String hashedOldPassword, String newPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();
      toReturn.put("Result", "Unknown email.");
      logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword 2 for player name: " + playerName);
      if (emailAddress != null) {
         if (isEmailValid(emailAddress)) {
            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);
            boolean ok = false;
            int nums = 0;

            for(int x = 0; x < infos.length; ++x) {
               if (infos[x].getPassword().equals(hashedOldPassword)) {
                  ok = true;
               }
            }

            if (ok) {
               boolean failed = false;

               for(int x = 0; x < infos.length; ++x) {
                  if (infos[x].getPower() == 0) {
                     try {
                        infos[x].updatePassword(newPassword);
                        if (infos[x].currentServer != Servers.localServer.id) {
                           new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                        }

                        toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                     } catch (IOException var14) {
                        failed = true;
                        toReturn.put("Error" + nums, "Failed to update password for " + infos[x].getName());
                     }
                  } else {
                     failed = true;
                     toReturn.put("Error" + nums, infos[x].getName() + " password was _not_ updated.");
                  }
               }

               if (failed) {
                  logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
               }
            }

            if (nums > 0) {
               toReturn.put("Result", nums + " player accounts were affected.");
            } else {
               toReturn.put("Error", nums + " player accounts were affected.");
            }

            return toReturn;
         }

         toReturn.put("Result", emailAddress + " is an invalid email.");
      } else if (playerName != null) {
         PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

         try {
            p.load();
            boolean ok = false;
            if (isEmailValid(p.emailAddress)) {
               emailAddress = p.emailAddress;
               PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(emailAddress);

               for(int x = 0; x < infos.length; ++x) {
                  if (infos[x].getPassword().equals(hashedOldPassword)) {
                     ok = true;
                  }
               }

               int nums = 0;
               if (ok) {
                  boolean failed = false;

                  for(int x = 0; x < infos.length; ++x) {
                     if (infos[x].getPower() == 0) {
                        try {
                           infos[x].updatePassword(newPassword);
                           if (infos[x].currentServer != Servers.localServer.id) {
                              new PasswordTransfer(infos[x].getName(), infos[x].wurmId, infos[x].getPassword(), System.currentTimeMillis(), false);
                           }

                           toReturn.put("Account" + ++nums, infos[x].getName() + " password was updated.");
                        } catch (IOException var15) {
                           failed = true;
                           toReturn.put("Error" + x, "Failed to update password for " + infos[x].getName());
                        }
                     } else {
                        failed = true;
                     }
                  }

                  if (failed) {
                     logger.log(Level.WARNING, "Failed to update password for one or more accounts.");
                  }
               }

               if (nums > 0) {
                  toReturn.put("Result", nums + " player accounts were affected.");
               } else {
                  toReturn.put("Error", nums + " player accounts were affected.");
               }

               return toReturn;
            }

            toReturn.put("Error", emailAddress + " is an invalid email.");
         } catch (IOException var16) {
            toReturn.put("Error", "Failed to load player data. Password not changed.");
            logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
         }
      }

      return toReturn;
   }

   @Override
   public Map<String, String> changeEmail(String intraServerPassword, String playerName, String oldEmailAddress, String newEmailAddress) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();
      toReturn.put("Result", "Unknown email.");
      logger.log(Level.INFO, this.getRemoteClientDetails() + " Change Email for player name: " + playerName);
      if (Constants.maintaining) {
         toReturn.put("Error", "The server is currently unavailable.");
         toReturn.put("Result", "The server is in maintenance mode. Please try later.");
         return toReturn;
      } else if (oldEmailAddress == null) {
         if (playerName != null) {
            PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

            try {
               p.load();
               if (isEmailValid(newEmailAddress)) {
                  oldEmailAddress = p.emailAddress;
                  PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
                  int nums = 0;

                  for(int x = 0; x < infos.length; ++x) {
                     if (infos[x].getPower() == 0) {
                        infos[x].setEmailAddress(newEmailAddress);
                        toReturn.put("Account" + ++nums, infos[x].getName() + " account was affected.");
                     } else {
                        toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
                     }
                  }

                  if (nums > 0) {
                     toReturn.put("Result", nums + " player accounts were affected.");
                  } else {
                     toReturn.put("Error", nums + " player accounts were affected.");
                  }

                  return toReturn;
               }

               toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
            } catch (IOException var10) {
               toReturn.put("Error", "Failed to load player data. Email not changed.");
               logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
            }
         }

         return toReturn;
      } else {
         if (!isEmailValid(oldEmailAddress)) {
            toReturn.put("Error", "The old email address, " + oldEmailAddress + " is an invalid email.");
         } else if (!isEmailValid(newEmailAddress)) {
            toReturn.put("Error", "The new email address, " + newEmailAddress + " is an invalid email.");
         } else {
            PlayerInfo[] infos = PlayerInfoFactory.getPlayerInfosWithEmail(oldEmailAddress);
            int nums = 0;

            for(int x = 0; x < infos.length; ++x) {
               if (infos[x].getPower() == 0) {
                  infos[x].setEmailAddress(newEmailAddress);
                  toReturn.put("Account" + ++nums, infos[x].getName() + " account was affected.");
               } else {
                  toReturn.put("Account" + nums, infos[x].getName() + " account was _not_ affected.");
               }
            }

            if (nums > 0) {
               toReturn.put("Result", nums + " player accounts were affected.");
            } else {
               toReturn.put("Error", nums + " player accounts were affected.");
            }
         }

         return toReturn;
      }
   }

   @Override
   public String getChallengePhrase(String intraServerPassword, String playerName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (playerName.contains("@")) {
         PlayerInfo[] pinfos = PlayerInfoFactory.getPlayerInfosForEmail(playerName);
         return pinfos.length > 0 ? pinfos[0].pwQuestion : "Incorrect email.";
      } else {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer(this.getRemoteClientDetails() + " getChallengePhrase for player name: " + playerName);
         }

         PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

         try {
            p.load();
            return p.pwQuestion;
         } catch (IOException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            return "Error";
         }
      }
   }

   @Override
   public String[] getPlayerNamesForEmail(String intraServerPassword, String emailAddress) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getPlayerNamesForEmail: " + emailAddress);
      }

      return PlayerInfoFactory.getAccountsForEmail(emailAddress);
   }

   @Override
   public String getEmailAddress(String intraServerPassword, String playerName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getEmailAddress for player name: " + playerName);
      }

      PlayerInfo p = PlayerInfoFactory.createPlayerInfo(playerName);

      try {
         p.load();
         return p.emailAddress;
      } catch (IOException var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         return "Error";
      }
   }

   public static String generateRandomPassword() {
      Random rand = new Random();
      int length = rand.nextInt(3) + 6;
      char[] password = new char[length];

      for(int x = 0; x < length; ++x) {
         int randDecimalAsciiVal = rand.nextInt("abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".length());
         password[x] = "abcdefgijkmnopqrstwxyzABCDEFGHJKLMNPQRSTWXYZ23456789".charAt(randDecimalAsciiVal);
      }

      return String.valueOf(password);
   }

   public static final boolean isEmailValid(String emailAddress) {
      if (emailAddress == null) {
         return false;
      } else {
         Matcher m = VALID_EMAIL_PATTERN.matcher(emailAddress);
         return m.matches();
      }
   }

   @Override
   public Map<String, String> requestPasswordReset(String intraServerPassword, String email, String challengePhraseAnswer) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<String, String> toReturn = new HashMap<>();
      if (Constants.maintaining) {
         toReturn.put("Error0", "The server is currently in maintenance mode.");
         return toReturn;
      } else {
         boolean ok = false;
         String password = generateRandomPassword();
         String playernames = "";
         logger.log(Level.INFO, this.getRemoteClientDetails() + " Password reset for email/name: " + email);
         if (challengePhraseAnswer != null && challengePhraseAnswer.length() >= 1) {
            if (!email.contains("@")) {
               PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(email);
               if (!pinf.loaded) {
                  try {
                     pinf.load();
                     logger.log(Level.INFO, email + " " + challengePhraseAnswer + " compares to " + pinf.pwAnswer);
                     if (System.currentTimeMillis() - pinf.lastRequestedPassword <= 60000L) {
                        toReturn.put("Error", "Please try again in a minute.");
                        return toReturn;
                     }

                     logger.log(Level.INFO, email + " time ok. comparing.");
                     if (pinf.pwAnswer.equalsIgnoreCase(challengePhraseAnswer)) {
                        logger.log(Level.INFO, email + " challenge answer correct.");
                        ok = true;
                        playernames = pinf.getName();
                        pinf.updatePassword(password);
                        if (pinf.currentServer != Servers.localServer.id) {
                           new PasswordTransfer(pinf.getName(), pinf.wurmId, pinf.getPassword(), System.currentTimeMillis(), false);
                        }
                     }

                     pinf.lastRequestedPassword = System.currentTimeMillis();
                  } catch (IOException var12) {
                     logger.log(Level.WARNING, email + ":" + var12.getMessage(), (Throwable)var12);
                     toReturn.put("Error", "An error occured. Please try later.");
                     return toReturn;
                  }
               }
            } else {
               PlayerInfo[] p = PlayerInfoFactory.getPlayerInfosWithEmail(email);

               for(int x = 0; x < p.length; ++x) {
                  try {
                     p[x].load();
                     if (p[x].pwAnswer.toLowerCase().equals(challengePhraseAnswer.toLowerCase())
                        || p[x].pwAnswer.length() == 0 && p[x].pwQuestion.length() == 0) {
                        if (System.currentTimeMillis() - p[x].lastRequestedPassword > 60000L) {
                           ok = true;
                           if (playernames.length() > 0) {
                              playernames = playernames + ", " + p[x].getName();
                           } else {
                              playernames = p[x].getName();
                           }

                           p[x].updatePassword(password);
                           if (p[x].currentServer != Servers.localServer.id) {
                              new PasswordTransfer(p[x].getName(), p[x].wurmId, p[x].getPassword(), System.currentTimeMillis(), false);
                           }
                        } else if (!ok) {
                           toReturn.put("Error", "Please try again in a minute.");
                           return toReturn;
                        }
                     }

                     p[x].lastRequestedPassword = System.currentTimeMillis();
                  } catch (IOException var13) {
                     logger.log(Level.WARNING, email + ":" + var13.getMessage(), (Throwable)var13);
                     toReturn.put("Error", "An error occured. Please try later.");
                     return toReturn;
                  }
               }
            }

            if (ok) {
               toReturn.put("Result", "Password was changed.");
            } else {
               toReturn.put("Error", "Password was not changed.");
            }

            if (playernames.length() > 0) {
               try {
                  String mail = Mailer.getPasswordMail();
                  mail = mail.replace("@pname", playernames);
                  mail = mail.replace("@password", password);
                  Mailer.sendMail(mailAccount, email, "Wurm Online password request", mail);
                  toReturn.put("MailResult", "A mail was sent to the mail adress: " + email + " for " + playernames + ".");
               } catch (Exception var11) {
                  logger.log(Level.WARNING, email + ":" + var11.getMessage(), (Throwable)var11);
                  toReturn.put("MailError", "An error occured - " + var11.getMessage() + ". Please try later.");
               }

               return toReturn;
            } else {
               toReturn.put("Error", "Wrong answer.");
               return toReturn;
            }
         } else {
            toReturn.put("Error0", "The answer is too short.");
            return toReturn;
         }
      }
   }

   @Override
   public Map<Integer, String> getAllServers(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      return this.getAllServerInternalAddresses(intraServerPassword);
   }

   @Override
   public Map<Integer, String> getAllServerInternalAddresses(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Map<Integer, String> toReturn = new HashMap<>();
      ServerEntry[] entries = Servers.getAllServers();

      for(int x = 0; x < entries.length; ++x) {
         toReturn.put(entries[x].id, entries[x].INTRASERVERADDRESS);
      }

      return toReturn;
   }

   @Override
   public boolean sendMail(String intraServerPassword, String sender, String receiver, String subject, String text) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (!isEmailValid(sender)) {
         return false;
      } else if (!isEmailValid(receiver)) {
         return false;
      } else {
         try {
            Mailer.sendMail(sender, receiver, subject, text);
            return true;
         } catch (Exception var7) {
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            return false;
         }
      }
   }

   @Override
   public void shutDown(String intraServerPassword, String playerName, String password, String reason, int seconds) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINE)) {
         logger.fine(this.getRemoteClientDetails() + " shutDown by player name: " + playerName);
      }

      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(playerName));

      try {
         pinf.load();
         if (pinf.getPower() >= 4) {
            try {
               String pw = LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.getName())));
               if (pw.equals(pinf.getPassword())) {
                  logger.log(
                     Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + " initiated shutdown in " + seconds + " seconds: " + reason
                  );
                  if (seconds <= 0) {
                     Server.getInstance().shutDown();
                  } else {
                     Server.getInstance().startShutdown(seconds, reason);
                  }
               } else {
                  logger.log(Level.WARNING, this.getRemoteClientDetails() + " player: " + playerName + " denied shutdown due to wrong password.");
               }
            } catch (Exception var8) {
               logger.log(Level.INFO, "Failed to encrypt password for player " + playerName, (Throwable)var8);
            }
         } else {
            logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + " DENIED shutdown in " + seconds + " seconds: " + reason);
         }
      } catch (IOException var9) {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " player: " + playerName + ": " + var9.getMessage(), (Throwable)var9);
      }
   }

   @Override
   public Map<String, Byte> getReferrers(String intraServerPassword, long wurmid) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getReferrers for WurmID: " + wurmid);
      }

      return PlayerInfoFactory.getReferrers(wurmid);
   }

   @Override
   public String addReferrer(String intraServerPassword, String receiver, long referrer) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.info(this.getRemoteClientDetails() + " addReferrer for Receiver player name: " + receiver + ", referrerID: " + referrer);
      synchronized(Server.SYNC_LOCK) {
         String var12;
         try {
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(receiver);

            try {
               pinf.load();
            } catch (IOException var9) {
               return receiver + " - no such player exists. Please check the spelling.";
            }

            if (pinf.wurmId == referrer) {
               return "You may not refer yourself.";
            }

            if (pinf.getPaymentExpire() <= 0L) {
               return pinf.getName() + " has never had a premium account and may not receive referrals.";
            }

            if (PlayerInfoFactory.addReferrer(pinf.wurmId, referrer)) {
               return String.valueOf(pinf.wurmId);
            }

            var12 = "You have already awarded referral to that player.";
         } catch (Exception var10) {
            logger.log(Level.WARNING, var10.getMessage() + " " + receiver + " from " + referrer, (Throwable)var10);
            return "An error occurred. Please write a bug report about this.";
         }

         return var12;
      }
   }

   @Override
   public String acceptReferrer(String intraServerPassword, long wurmid, String awarderName, boolean money) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINE)) {
         logger.fine(this.getRemoteClientDetails() + " acceptReferrer for player wurmid: " + wurmid + ", awarderName: " + awarderName + ", money: " + money);
      }

      PlayerInfo pinf = null;

      try {
         long l = Long.parseLong(awarderName);
         pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(l);
      } catch (NumberFormatException var13) {
         pinf = PlayerInfoFactory.createPlayerInfo(awarderName);

         try {
            pinf.load();
         } catch (IOException var12) {
            logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
            return "Failed to locate the player " + awarderName + " in the database.";
         }
      }

      if (pinf != null) {
         try {
            synchronized(Server.SYNC_LOCK) {
               if (!PlayerInfoFactory.acceptReferer(wurmid, pinf.wurmId, money)) {
                  return "Failed to match " + awarderName + " to any existing referral.";
               }

               try {
                  if (money) {
                     PlayerInfoFactory.addMoneyToBank(wurmid, 30000L, "Referred by " + pinf.getName());
                  } else {
                     PlayerInfoFactory.addPlayingTime(wurmid, 0, 20, "Referred by " + pinf.getName());
                  }
               } catch (Exception var11) {
                  logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
                  PlayerInfoFactory.revertReferer(wurmid, pinf.wurmId);
                  return "An error occured. Please try later or post a bug report.";
               }
            }
         } catch (Exception var15) {
            logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
            return "An error occured. Please try later or post a bug report.";
         }

         return "Okay, accepted the referral from " + awarderName + ". The reward will arrive soon if it has not already.";
      } else {
         return "Failed to locate " + awarderName + " in the database.";
      }
   }

   @Override
   public Map<String, Double> getSkillStats(String intraServerPassword, int skillid) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getSkillStats for skillid: " + skillid);
      }

      Map<String, Double> toReturn = new HashMap<>();

      try {
         SkillStat sk = SkillStat.getSkillStatForSkill(skillid);

         for(Entry<Long, Double> entry : sk.stats.entrySet()) {
            Long lid = entry.getKey();
            long pid = lid;
            PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
            if (p != null && entry.getValue() > 1.0) {
               toReturn.put(p.getName(), entry.getValue());
            }
         }
      } catch (Exception var11) {
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
         toReturn.put("ERROR: " + var11.getMessage(), 0.0);
      }

      return toReturn;
   }

   @Override
   public Map<Integer, String> getSkills(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      return SkillSystem.skillNames;
   }

   @Override
   public Map<String, ?> getStructureSummary(String intraServerPassword, long aStructureID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getStructureSummary for StructureID: " + aStructureID);
      }

      Map<String, Object> lToReturn = new HashMap<>(10);

      try {
         Structure lStructure = Structures.getStructure(aStructureID);
         if (lStructure != null) {
            lToReturn.put("CenterX", lStructure.getCenterX());
            lToReturn.put("CenterY", lStructure.getCenterY());
            lToReturn.put("CreationDate", lStructure.getCreationDate());
            lToReturn.put("Door Count", lStructure.getDoors());
            lToReturn.put("FinalFinished", lStructure.isFinalFinished());
            lToReturn.put("Finalized", lStructure.isFinalized());
            lToReturn.put("Finished", lStructure.isFinished());
            lToReturn.put("Guest Count", lStructure.getPermissionsPlayerList().size());
            lToReturn.put("Limit", lStructure.getLimit());
            lToReturn.put("Lockable", lStructure.isLockable());
            lToReturn.put("Locked", lStructure.isLocked());
            lToReturn.put("MaxX", lStructure.getMaxX());
            lToReturn.put("MaxY", lStructure.getMaxY());
            lToReturn.put("MinX", lStructure.getMinX());
            lToReturn.put("MinY", lStructure.getMinY());
            lToReturn.put("Name", lStructure.getName());
            lToReturn.put("OwnerID", lStructure.getOwnerId());
            lToReturn.put("Roof", lStructure.getRoof());
            lToReturn.put("Size", lStructure.getSize());
            lToReturn.put("HasWalls", lStructure.hasWalls());
            Wall[] lWalls = lStructure.getWalls();
            if (lWalls != null) {
               lToReturn.put("Wall Count", lWalls.length);
            } else {
               lToReturn.put("Wall Count", 0);
            }

            lToReturn.put("WritID", lStructure.getWritId());
            lToReturn.put("WurmID", lStructure.getWurmId());
         } else {
            lToReturn.put("Error", "No such Structure");
         }
      } catch (NoSuchStructureException var7) {
         logger.log(Level.WARNING, "Structure with id " + aStructureID + " not found.", (Throwable)var7);
         lToReturn.put("Error", "No such Structure");
         lToReturn.put("Exception", var7.getMessage());
      } catch (RuntimeException var8) {
         logger.log(Level.WARNING, "Error: " + var8.getMessage(), (Throwable)var8);
         lToReturn.put("Exception", var8);
      }

      return lToReturn;
   }

   @Override
   public long getStructureIdFromWrit(String intraServerPassword, long aWritID) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getStructureIdFromWrit for WritID: " + aWritID);
      }

      try {
         Structure struct = Structures.getStructureForWrit(aWritID);
         if (struct != null) {
            return struct.getWurmId();
         }
      } catch (NoSuchStructureException var5) {
      }

      return -1L;
   }

   @Override
   public Map<String, ?> getTileSummary(String intraServerPassword, int tilex, int tiley, boolean surfaced) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getTileSummary for tile (x,y): " + tilex + ", " + tiley);
      }

      Map<String, Object> lToReturn = new HashMap<>(10);

      try {
         Zone zone = Zones.getZone(tilex, tiley, surfaced);
         VolaTile tile = zone.getTileOrNull(tilex, tiley);
         if (tile != null) {
            Structure lStructure = tile.getStructure();
            if (lStructure != null) {
               lToReturn.put("StructureID", lStructure.getWurmId());
               lToReturn.put("StructureName", lStructure.getName());
            }

            lToReturn.put("Kingdom", tile.getKingdom());
            Village lVillage = tile.getVillage();
            if (lVillage != null) {
               lToReturn.put("VillageID", lVillage.getId());
               lToReturn.put("VillageName", lVillage.getName());
            }

            lToReturn.put("Coord x", tile.getTileX());
            lToReturn.put("Coord y", tile.getTileY());
         } else {
            lToReturn.put("Error", "No such tile");
         }
      } catch (NoSuchZoneException var10) {
         lToReturn.put("Error", "No such zone");
         lToReturn.put("Exception", var10.getMessage());
      } catch (RuntimeException var11) {
         logger.log(Level.WARNING, "Error: " + var11.getMessage(), (Throwable)var11);
         lToReturn.put("Exception", var11);
      }

      return lToReturn;
   }

   @Override
   public String getReimbursementInfo(String intraServerPassword, String email) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getReimbursementInfo for email: " + email);
      }

      return Reimbursement.getReimbursementInfo(email);
   }

   @Override
   public boolean withDraw(
      String intraServerPassword, String retriever, String name, String _email, int _months, int _silvers, boolean titlebok, int _daysLeft
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.info(
         this.getRemoteClientDetails()
            + " withDraw for retriever: "
            + retriever
            + ", name: "
            + name
            + ", email: "
            + _email
            + ", months: "
            + _months
            + ", silvers: "
            + _silvers
      );
      return Reimbursement.withDraw(retriever, name, _email, _months, _silvers, titlebok, _daysLeft);
   }

   @Override
   public boolean transferPlayer(String intraServerPassword, String playerName, int posx, int posy, boolean surfaced, int power, byte[] data) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (Constants.maintaining && power <= 0) {
         return false;
      } else {
         logger.log(
            Level.INFO,
            this.getRemoteClientDetails() + " Transferplayer name: " + playerName + ", position (x,y): " + posx + ", " + posy + ", surfaced: " + surfaced
         );
         if (IntraServerConnection.savePlayerToDisk(data, posx, posy, surfaced, false) > 0L) {
            if (!Servers.isThisLoginServer()) {
               return new LoginServerWebConnection().setCurrentServer(playerName, Servers.localServer.id);
            } else {
               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean changePassword(String intraServerPassword, long wurmId, String newPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.log(Level.INFO, this.getRemoteClientDetails() + " Changepassword name: " + wurmId);
      return IntraServerConnection.setNewPassword(wurmId, newPassword);
   }

   @Override
   public boolean setCurrentServer(String intraServerPassword, String name, int currentServer) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " setCurrentServer to " + currentServer + " for player name: " + name);
      }

      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
      if (pinf == null) {
         return false;
      } else {
         pinf.setCurrentServer(currentServer);
         return true;
      }
   }

   @Override
   public boolean addDraggedItem(String intraServerPassword, long itemId, byte[] itemdata, long draggerId, int posx, int posy) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
      logger.log(Level.INFO, this.getRemoteClientDetails() + " Adddraggeditem itemID: " + itemId + ", draggerId: " + draggerId);

      try {
         Set<ItemMetaData> idset = new HashSet<>();
         int nums = iis.readInt();

         for(int x = 0; x < nums; ++x) {
            IntraServerConnection.createItem(iis, 0.0F, 0.0F, 0.0F, idset, false);
         }

         Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
      } catch (IOException var15) {
         logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
         return false;
      }

      try {
         Item i = Items.getItem(itemId);
         Zone z = Zones.getZone(posx, posy, true);
         z.addItem(i);
         return true;
      } catch (NoSuchItemException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
         return false;
      } catch (NoSuchZoneException var14) {
         logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
         return false;
      }
   }

   @Override
   public String rename(String intraServerPassword, String oldName, String newName, String newPass, int power) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " rename oldName: " + oldName + ", newName: " + newName + ", power: " + power);
      }

      String toReturn = "";
      newName = LoginHandler.raiseFirstLetter(newName);
      if (Servers.localServer.LOGINSERVER && Players.getInstance().doesPlayerNameExist(newName)) {
         return "The name " + newName + " already exists. This is an Error.";
      } else {
         if (Servers.localServer.LOGINSERVER) {
            toReturn = toReturn + Servers.rename(oldName, newName, newPass, power);
         }

         if (!toReturn.contains("Error.")) {
            try {
               toReturn = PlayerInfoFactory.rename(oldName, newName, newPass, power);
            } catch (IOException var8) {
               toReturn = toReturn + Servers.localServer.name + " " + var8.getMessage() + ". This is an Error.\n";
               logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            }
         }

         return toReturn;
      }
   }

   @Override
   public String changePassword(String intraServerPassword, String changerName, String name, String newPass, int power) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
      }

      String toReturn = "";
      changerName = LoginHandler.raiseFirstLetter(changerName);
      name = LoginHandler.raiseFirstLetter(name);

      try {
         toReturn = PlayerInfoFactory.changePassword(changerName, name, newPass, power);
      } catch (IOException var9) {
         toReturn = toReturn + Servers.localServer.name + " " + var9.getMessage() + "\n";
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
      }

      logger.log(Level.INFO, this.getRemoteClientDetails() + " changePassword, changerName: " + changerName + ", for player name: " + name);
      if (Servers.localServer.LOGINSERVER) {
         if (changerName.equals(name)) {
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
            if (pinf != null && Servers.localServer.id != pinf.currentServer) {
               LoginServerWebConnection lsw = new LoginServerWebConnection(pinf.currentServer);
               toReturn = toReturn + lsw.changePassword(changerName, name, newPass, power);
            }
         } else {
            toReturn = toReturn + Servers.sendChangePass(changerName, name, newPass, power);
         }
      }

      return toReturn;
   }

   @Override
   public String changeEmail(
      String intraServerPassword, String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name + ", power: " + power);
      }

      changerName = LoginHandler.raiseFirstLetter(changerName);
      name = LoginHandler.raiseFirstLetter(name);
      String toReturn = "";
      logger.log(Level.INFO, this.getRemoteClientDetails() + " changeEmail, changerName: " + changerName + ", for player name: " + name);

      try {
         toReturn = PlayerInfoFactory.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
         if (toReturn.equals("NO") || toReturn.equals("NO Retrieval info updated.")) {
            return "You may only have 5 accounts with the same email. Also you need to provide the correct password for a character with that email address in order to change to it.";
         }
      } catch (IOException var11) {
         toReturn = toReturn + Servers.localServer.name + " " + var11.getMessage() + "\n";
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
      }

      if (Servers.localServer.LOGINSERVER) {
         toReturn = toReturn + Servers.changeEmail(changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
      }

      return toReturn;
   }

   @Override
   public String addReimb(String intraServerPassword, String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINE)) {
         logger.fine(
            this.getRemoteClientDetails()
               + " addReimb, changerName: "
               + changerName
               + ", for player name: "
               + name
               + ", numMonths: "
               + numMonths
               + ", silver: "
               + _silver
               + ", daysLeft: "
               + _daysLeft
               + ", setbok: "
               + setbok
         );
      }

      changerName = LoginHandler.raiseFirstLetter(changerName);
      name = LoginHandler.raiseFirstLetter(name);
      return Servers.localServer.LOGINSERVER
         ? Reimbursement.addReimb(changerName, name, numMonths, _silver, _daysLeft, setbok)
         : Servers.localServer.name + " - failed to add reimbursement. This is not the login server.";
   }

   @Override
   public long[] getCurrentServerAndWurmid(String intraServerPassword, String name, long wurmid) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " getCurrentServerAndWurmid for player name: " + name + ", wurmid: " + wurmid);
      }

      PlayerInfo pinf = null;
      if (name != null && name.length() > 2) {
         name = LoginHandler.raiseFirstLetter(name);
         pinf = PlayerInfoFactory.createPlayerInfo(name);
      } else if (wurmid > 0L) {
         pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
      }

      if (pinf != null) {
         try {
            pinf.load();
            return new long[]{(long)pinf.currentServer, pinf.wurmId};
         } catch (IOException var7) {
         }
      }

      return noInfoLong;
   }

   @Override
   public Map<Long, byte[]> getPlayerStates(String intraServerPassword, long[] wurmids) throws RemoteException, WurmServerException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         if (wurmids.length == 0) {
            logger.finer(this.getRemoteClientDetails() + " getPlayersSubInfo for ALL players.");
         } else {
            StringBuilder buf = new StringBuilder();

            for(int x = 0; x < wurmids.length; ++x) {
               if (x > 0) {
                  buf.append(",");
               }

               buf.append(wurmids[x]);
            }

            logger.finer(this.getRemoteClientDetails() + " getPlayersSubInfo for player wurmids: " + buf.toString());
         }
      }

      return PlayerInfoFactory.getPlayerStates(wurmids);
   }

   @Override
   public void manageFeature(
      String intraServerPassword, int serverId, final int featureId, final boolean aOverridden, final boolean aEnabled, final boolean global
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " manageFeature " + featureId);
      }

      Thread t = new Thread("manageFeature-Thread-" + featureId) {
         @Override
         public void run() {
            Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, aOverridden, aEnabled, global);
         }
      };
      t.setPriority(4);
      t.start();
   }

   @Override
   public void startShutdown(String intraServerPassword, String instigator, int seconds, String reason) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (Servers.isThisLoginServer()) {
         Servers.startShutdown(instigator, seconds, reason);
      }

      logger.log(Level.INFO, instigator + " shutting down server in " + seconds + " seconds, reason: " + reason);
      Server.getInstance().startShutdown(seconds, reason);
   }

   @Override
   public String sendMail(String intraServerPassword, byte[] maildata, byte[] itemdata, long sender, long wurmid, int targetServer) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.log(Level.INFO, this.getRemoteClientDetails() + " sendMail " + sender + " to server " + targetServer + ", receiver ID: " + wurmid);
      if (targetServer == Servers.localServer.id) {
         DataInputStream dis = new DataInputStream(new ByteArrayInputStream(maildata));

         try {
            int nums = dis.readInt();

            for(int x = 0; x < nums; ++x) {
               WurmMail m = new WurmMail(
                  dis.readByte(),
                  dis.readLong(),
                  dis.readLong(),
                  dis.readLong(),
                  dis.readLong(),
                  dis.readLong(),
                  dis.readLong(),
                  dis.readInt(),
                  dis.readBoolean(),
                  false
               );
               WurmMail.addWurmMail(m);
               m.createInDatabase();
            }
         } catch (IOException var15) {
            logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
            return "A database error occurred. Please report this to a GM.";
         }

         DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));

         try {
            Set<ItemMetaData> idset = new HashSet<>();
            int nums = iis.readInt();

            for(int x = 0; x < nums; ++x) {
               IntraServerConnection.createItem(iis, 0.0F, 0.0F, 0.0F, idset, false);
            }

            Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
            return "";
         } catch (IOException var14) {
            logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
            return "A database error occurred when inserting an item. Please report this to a GM.";
         }
      } else {
         ServerEntry entry = Servers.getServerWithId(targetServer);
         if (entry != null) {
            if (entry.isAvailable(5, true)) {
               LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
               return lsw.sendMail(maildata, itemdata, sender, wurmid, targetServer);
            } else {
               return "The target server is not available right now.";
            }
         } else {
            return "Failed to locate target server.";
         }
      }
   }

   @Override
   public String pardonban(String intraServerPassword, String name) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " pardonban for player name: " + name);
      }

      if (Servers.localServer.LOGINSERVER) {
         PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
         if (info != null) {
            try {
               info.load();
            } catch (IOException var6) {
               logger.log(
                  Level.WARNING,
                  this.getRemoteClientDetails() + " Failed to load the player information. Not pardoned - " + var6.getMessage(),
                  (Throwable)var6
               );
               return "Failed to load the player information. Not pardoned.";
            }

            try {
               info.setBanned(false, "", 0L);
            } catch (IOException var5) {
               logger.log(
                  Level.WARNING,
                  this.getRemoteClientDetails() + " Failed to save the player information. Not pardoned - " + var5.getMessage(),
                  (Throwable)var5
               );
               return "Failed to save the player information. Not pardoned.";
            }

            logger.info(this.getRemoteClientDetails() + " Login server pardoned " + name);
            return "Login server pardoned " + name + ".";
         } else {
            logger.warning("Failed to locate the player " + name + ".");
            return "Failed to locate the player " + name + ".";
         }
      } else {
         logger.warning(Servers.localServer.name + " not login server. Pardon failed.");
         return Servers.localServer.name + " not login server. Pardon failed.";
      }
   }

   @Override
   public String ban(String intraServerPassword, String name, String reason, int days) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " ban for player name: " + name + ", reason: " + reason + ", for " + days + " days");
      }

      if (Servers.localServer.LOGINSERVER) {
         PlayerInfo info = PlayerInfoFactory.createPlayerInfo(name);
         if (info != null) {
            long expiry = System.currentTimeMillis() + (long)days * 86400000L;

            try {
               info.load();
            } catch (IOException var10) {
               logger.log(Level.WARNING, "Failed to load the player information. Not banned - " + var10.getMessage(), (Throwable)var10);
               return "Failed to load the player information. Not banned.";
            }

            try {
               info.setBanned(true, reason, expiry);
            } catch (IOException var9) {
               logger.log(Level.WARNING, "Failed to save the player information. Not banned - " + var9.getMessage(), (Throwable)var9);
               return "Failed to save the player information. Not banned.";
            }

            logger.info(this.getRemoteClientDetails() + " Login server banned " + name + ": " + reason + " for " + days + " days.");
            return "Login server banned " + name + ": " + reason + " for " + days + " days.";
         } else {
            logger.warning("Failed to locate the player " + name + ".");
            return "Failed to locate the player " + name + ".";
         }
      } else {
         logger.warning(Servers.localServer.name + " not login server. IPBan failed.");
         return Servers.localServer.name + " not login server. IPBan failed.";
      }
   }

   @Override
   public String addBannedIp(String intraServerPassword, String ip, String reason, int days) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      long expiry = System.currentTimeMillis() + (long)days * 86400000L;
      Players.getInstance().addBannedIp(ip, reason, expiry);
      logger.info(this.getRemoteClientDetails() + " RMI client requested " + ip + " banned for " + days + " days - " + reason);
      return ip + " banned for " + days + " days - " + reason;
   }

   @Override
   public Ban[] getPlayersBanned(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      return Players.getInstance().getPlayersBanned();
   }

   @Override
   public Ban[] getIpsBanned(String intraServerPassword) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      return Players.getInstance().getBans();
   }

   @Override
   public String removeBannedIp(String intraServerPassword, String ip) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (Players.getInstance().removeBan(ip)) {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client requested " + ip + " was pardoned.");
         return "Okay, " + ip + " was pardoned.";
      } else {
         logger.info(this.getRemoteClientDetails() + " RMI client requested pardon but the ip " + ip + " was not previously banned.");
         return "The ip " + ip + " was not previously banned.";
      }
   }

   @Override
   public String setPlayerMoney(String intraServerPassword, long wurmid, long currentMoney, long moneyAdded, String detail) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (moneyDetails.contains(detail)) {
         logger.warning(
            this.getRemoteClientDetails()
               + " RMI client The money transaction has already been performed, wurmid: "
               + wurmid
               + ", currentMoney: "
               + currentMoney
               + ", moneyAdded: "
               + moneyAdded
               + ", detail: "
               + detail
         );
         return "The money transaction has already been performed";
      } else {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set player money for " + wurmid);
         PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
         if (info != null) {
            try {
               info.load();
            } catch (IOException var13) {
               logger.log(Level.WARNING, "Failed to load player info for " + wurmid + ", detail: " + detail + ": " + var13.getMessage(), (Throwable)var13);
               return "Failed to load the player from database. Transaction failed.";
            }

            if (info.wurmId <= 0L) {
               logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
               return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
            } else if (info.currentServer != Servers.localServer.id) {
               logger.warning(
                  "Received a CMD_SET_PLAYER_MONEY for player "
                     + info.getName()
                     + " (id: "
                     + wurmid
                     + ") but their currentserver (id: "
                     + info.getCurrentServer()
                     + ") is not this server (id: "
                     + Servers.localServer.id
                     + "), detail: "
                     + detail
               );
               return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
            } else {
               try {
                  info.setMoney(currentMoney);
                  new MoneyTransfer(info.getName(), wurmid, currentMoney, moneyAdded, detail, (byte)6, "");
                  Change c = new Change(currentMoney);
                  moneyDetails.add(detail);

                  try {
                     logger.info(
                        this.getRemoteClientDetails()
                           + " RMI client Added "
                           + moneyAdded
                           + " to player ID: "
                           + wurmid
                           + ", currentMoney: "
                           + currentMoney
                           + ", detail: "
                           + detail
                     );
                     Player p = Players.getInstance().getPlayer(wurmid);
                     Message mess = new Message(null, (byte)3, ":Event", "Your available money in the bank is now " + c.getChangeString() + ".");
                     mess.setReceiver(p.getWurmId());
                     Server.getInstance().addMessage(mess);
                  } catch (NoSuchPlayerException var14) {
                     if (logger.isLoggable(Level.FINER)) {
                        logger.finer(
                           "player ID: " + wurmid + " is not online, currentMoney: " + currentMoney + ", moneyAdded: " + moneyAdded + ", detail: " + detail
                        );
                     }
                  }

                  return "Okay. The player now has " + c.getChangeString() + " in the bank.";
               } catch (IOException var15) {
                  logger.log(Level.WARNING, wurmid + ", failed to set money to " + currentMoney + ", detail: " + detail + ".", (Throwable)var15);
                  return "Money transaction failed. Error reported was " + var15.getMessage() + ".";
               }
            }
         } else {
            logger.log(Level.WARNING, wurmid + ", failed to locate player info and set money to " + currentMoney + ", detail: " + detail + "!");
            return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
         }
      }
   }

   @Override
   public String setPlayerPremiumTime(String intraServerPassword, long wurmid, long currentExpire, int days, int months, String detail) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (timeDetails.contains(detail)) {
         logger.warning(
            this.getRemoteClientDetails()
               + " RMI client The time transaction has already been performed, wurmid: "
               + wurmid
               + ", currentExpire: "
               + currentExpire
               + ", days: "
               + days
               + ", months: "
               + months
               + ", detail: "
               + detail
         );
         return "The time transaction has already been performed";
      } else {
         logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client set premium time for " + wurmid);
         PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
         if (info != null) {
            try {
               info.load();
            } catch (IOException var15) {
               logger.log(
                  Level.WARNING,
                  "Failed to load the player from database. Transaction failed, wurmid: "
                     + wurmid
                     + ", currentExpire: "
                     + currentExpire
                     + ", days: "
                     + days
                     + ", months: "
                     + months
                     + ", detail: "
                     + detail,
                  (Throwable)var15
               );
               return "Failed to load the player from database. Transaction failed.";
            }

            if (info.currentServer != Servers.localServer.id) {
               logger.warning(
                  "Received a CMD_SET_PLAYER_PAYMENTEXPIRE for player "
                     + info.getName()
                     + " (id: "
                     + wurmid
                     + ") but their currentserver (id: "
                     + info.getCurrentServer()
                     + ") is not this server (id: "
                     + Servers.localServer.id
                     + "), detail: "
                     + detail
               );
               return "There is inconsistency with regards to which server the player account is active on. Please email contact@wurmonline.com with this message. Transaction failed.";
            } else {
               try {
                  info.setPaymentExpire(currentExpire);
                  new TimeTransfer(info.getName(), wurmid, months, false, days, detail);
                  timeDetails.add(detail);

                  try {
                     Player p = Players.getInstance().getPlayer(wurmid);
                     String expireString = "You now have premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
                     Message mess = new Message(null, (byte)3, ":Event", expireString);
                     mess.setReceiver(p.getWurmId());
                     Server.getInstance().addMessage(mess);
                  } catch (NoSuchPlayerException var13) {
                  }

                  logger.info(
                     this.getRemoteClientDetails()
                        + " RMI client "
                        + info.getName()
                        + " now has premier playing time until "
                        + WurmCalendar.formatGmt(currentExpire)
                        + ", wurmid: "
                        + wurmid
                        + ", currentExpire: "
                        + currentExpire
                        + ", days: "
                        + days
                        + ", months: "
                        + months
                        + ", detail: "
                        + detail
                        + '.'
                  );
                  return "Okay. " + info.getName() + " now has premier playing time until " + WurmCalendar.formatGmt(currentExpire) + ".";
               } catch (IOException var14) {
                  logger.log(
                     Level.WARNING,
                     "Transaction failed, wurmid: "
                        + wurmid
                        + ", currentExpire: "
                        + currentExpire
                        + ", days: "
                        + days
                        + ", months: "
                        + months
                        + ", detail: "
                        + detail
                        + ", "
                        + var14.getMessage(),
                     (Throwable)var14
                  );
                  return "Time transaction failed. Error reported was " + var14.getMessage() + ".";
               }
            }
         } else {
            logger.log(Level.WARNING, wurmid + ", failed to locate player info and set expire time to " + currentExpire + "!, detail: " + detail);
            return "Failed to locate the player in the database. The player account probably has been deleted. Transaction failed.";
         }
      }
   }

   @Override
   public void setWeather(String intraServerPassword, float windRotation, float windpower, float windDir) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Server.getWeather().setWindOnly(windRotation, windpower, windDir);
      logger.log(Level.INFO, this.getRemoteClientDetails() + " RMI client. Received weather data from login server. Propagating windrot=" + windRotation);
      Players.getInstance().setShouldSendWeather(true);
   }

   @Override
   public String sendVehicle(
      String intraServerPassword,
      byte[] passengerdata,
      byte[] itemdata,
      long pilotId,
      long vehicleId,
      int targetServer,
      int tilex,
      int tiley,
      int layer,
      float rot
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.log(
         Level.INFO,
         this.getRemoteClientDetails()
            + " RMI client send vehicle for pilot "
            + pilotId
            + " vehicle "
            + vehicleId
            + " itemdata bytes="
            + itemdata.length
            + " passenger data bytes="
            + passengerdata.length
      );
      if (targetServer == Servers.localServer.id) {
         long start = System.nanoTime();
         DataInputStream iis = new DataInputStream(new ByteArrayInputStream(itemdata));
         Set<ItemMetaData> idset = new HashSet<>();

         try {
            int nums = iis.readInt();
            logger.log(Level.INFO, "Trying to create " + nums + " items for vehicle: " + vehicleId);
            float posx = (float)(tilex * 4 + 2);
            float posy = (float)(tiley * 4 + 2);
            IntraServerConnection.resetTransferVariables(String.valueOf(pilotId));

            for(int x = 0; x < nums; ++x) {
               IntraServerConnection.createItem(iis, posx, posy, 0.0F, idset, false);
            }

            Items.convertItemMetaData(idset.toArray(new ItemMetaData[idset.size()]));
         } catch (IOException var25) {
            logger.log(
               Level.WARNING,
               var25.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId,
               (Throwable)var25
            );

            for(ItemMetaData md : idset) {
               logger.log(Level.INFO, md.itname + ", " + md.itemId);
            }

            return "A database error occurred when inserting an item. Please report this to a GM.";
         } catch (Exception var26) {
            logger.log(
               Level.WARNING,
               var26.getMessage() + " Last item=" + IntraServerConnection.lastItemName + ", " + IntraServerConnection.lastItemId,
               (Throwable)var26
            );
            return "A database error occurred when inserting an item. Please report this to a GM.";
         }

         DataInputStream dis = new DataInputStream(new ByteArrayInputStream(passengerdata));

         try {
            Item i = Items.getItem(vehicleId);
            i.setPosXYZ((float)(tilex * 4 + 2), (float)(tiley * 4 + 2), 0.0F);
            i.setRotation(rot);
            logger.log(Level.INFO, "Trying to put " + i.getName() + ", " + i.getDescription() + " at " + i.getTileX() + "," + i.getTileY());
            Zones.getZone(i.getTileX(), i.getTileY(), layer == 0).addItem(i);
            Vehicles.createVehicle(i);
            MountTransfer mt = new MountTransfer(vehicleId, pilotId);
            int nums = dis.readInt();

            for(int x = 0; x < nums; ++x) {
               mt.addToSeat(dis.readLong(), dis.readInt());
            }
         } catch (NoSuchItemException var22) {
            logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var22.getMessage(), (Throwable)var22);
         } catch (NoSuchZoneException var23) {
            logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var23.getMessage(), (Throwable)var23);
         } catch (IOException var24) {
            logger.log(Level.WARNING, "Transferring vehicle " + vehicleId + ' ' + var24.getMessage(), (Throwable)var24);
            return "A database error occurred. Please report this to a GM.";
         }

         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.log(Level.INFO, "Transferring vehicle " + vehicleId + " took " + lElapsedTime + " ms.");
         return "";
      } else {
         ServerEntry entry = Servers.getServerWithId(targetServer);
         if (entry != null) {
            if (entry.isAvailable(5, true)) {
               LoginServerWebConnection lsw = new LoginServerWebConnection(targetServer);
               return lsw.sendVehicle(passengerdata, itemdata, pilotId, vehicleId, targetServer, tilex, tiley, layer, rot);
            } else {
               return "The target server is not available right now.";
            }
         } else {
            return "Failed to locate target server.";
         }
      }
   }

   @Override
   public void genericWebCommand(String intraServerPassword, short wctype, long id, byte[] data) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      WebCommand wc = WebCommand.createWebCommand(wctype, id, data);
      if (wc != null) {
         if (Servers.localServer.LOGINSERVER && wc.autoForward()) {
            Servers.sendWebCommandToAllServers(wctype, wc, wc.isEpicOnly());
         }

         if (WurmId.getOrigin(id) != Servers.localServer.id) {
            Server.getInstance().addWebCommand(wc);
         }
      }
   }

   @Override
   public void setKingdomInfo(
      String intraServerPassword,
      int serverId,
      byte kingdomId,
      byte templateKingdom,
      String _name,
      String _password,
      String _chatName,
      String _suffix,
      String mottoOne,
      String mottoTwo,
      boolean acceptsPortals
   ) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Kingdom newInfo = new Kingdom(kingdomId, templateKingdom, _name, _password, _chatName, _suffix, mottoOne, mottoTwo, acceptsPortals);
      if (serverId != Servers.localServer.id) {
         Kingdoms.addKingdom(newInfo);
      }

      WcKingdomInfo wck = new WcKingdomInfo(WurmId.getNextWCCommandId(), true, kingdomId);
      wck.encode();
      Servers.sendWebCommandToAllServers((short)7, wck, wck.isEpicOnly());
   }

   @Override
   public boolean kingdomExists(String intraServerPassword, int serverId, byte kingdomId, boolean exists) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      logger.log(Level.INFO, "serverId:" + serverId + " kingdom id " + kingdomId + " exists=" + exists);
      boolean result = Servers.kingdomExists(serverId, kingdomId, exists);
      if (Servers.getServerWithId(serverId) != null && Servers.getServerWithId(serverId).name != null) {
         logger.log(Level.INFO, Servers.getServerWithId(serverId).name + " kingdom id " + kingdomId + " exists=" + exists);
      } else if (Servers.getServerWithId(serverId) == null) {
         logger.log(Level.INFO, serverId + " server is null " + kingdomId + " exists=" + exists);
      } else {
         logger.log(Level.INFO, "Name for " + Servers.getServerWithId(serverId) + " server is null " + kingdomId + " exists=" + exists);
      }

      if (Servers.localServer.LOGINSERVER) {
         if (!exists) {
            if (!result) {
               Kingdom k = Kingdoms.getKingdomOrNull(kingdomId);
               boolean sendDelete = false;
               if (k != null && k.isCustomKingdom()) {
                  k.delete();
                  Kingdoms.removeKingdom(kingdomId);
                  sendDelete = true;
               }
            } else {
               Servers.sendKingdomExistsToAllServers(serverId, kingdomId, false);
            }
         } else {
            Servers.sendKingdomExistsToAllServers(serverId, kingdomId, true);
         }
      }

      return result;
   }

   public static void main(String[] args) {
      if (args.length == 2 && args[0].compareTo("ShutdownLive") == 0) {
         try {
            WebInterfaceTest wit = new WebInterfaceTest();
            System.out.println("Shutting down ALL live servers!");
            wit.shutdownAll("Maintenance restart. Up to thirty minutes downtime.", Integer.parseInt(args[1]));
            System.out.println("I do hope this is what you wanted. All servers will be down in approximately " + args[1] + " seconds.");
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      } else if (args.length == 3) {
         try {
            WebInterfaceTest wit = new WebInterfaceTest();
            System.out.println("Attempting to shutdown server at " + args[0] + ", port " + args[1]);
            String[] userInfo = args[2].split(":");
            wit.shutDown(args[0], args[1], userInfo[0], userInfo[1]);
         } catch (Exception var3) {
            logger.log(Level.INFO, "failed to shut down localhost", (Throwable)var3);
            var3.printStackTrace();
         }
      } else {
         System.out
            .println(
               "Usage:\nNo arguments - This message.\nShutdownLive <delay> - Shutsdown ALL LIVE SERVERS using the seconds provided as a delay\n<host> <port> <user>:<password> - Shutdown the specified server using your GM credentials."
            );
      }
   }

   private boolean validateAccount(String user, String password, byte power) throws IOException, Exception {
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(LoginHandler.raiseFirstLetter(user));
      if (pinf == null) {
         return false;
      } else {
         pinf.load();
         if (pinf.getPower() <= power) {
            return false;
         } else {
            String pw = LoginHandler.encrypt(pinf.getName() + password);
            return pw.equals(pinf.getPassword());
         }
      }
   }

   private void interactiveShutdown() {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      int state = 0;
      boolean interactive = true;
      String user = "";
      String message = "Maintenance shutdown. Up to thirty minutes downtime. See the forums for more information: http://forum.wurmonline.com/";
      int delay = 1800;
      System.out.println("[Shutdown Servers]\n(Type 'quit' at any time to abort)");

      while(interactive) {
         try {
            switch(state) {
               case 0:
                  System.out.print("GM Name: ");
                  user = br.readLine().trim();
                  state = 1;
                  break;
               case 1:
                  System.out.print("GM password: ");
                  String password = br.readLine().trim();
                  if (!this.validateAccount(user, password, (byte)4)) {
                     interactive = false;
                     System.out.println("Invalid password or power level insufficient.");
                     return;
                  }

                  state = 2;
                  break;
               case 2:
                  System.out.print("Message: [default '" + message + "'] ");
                  String in = br.readLine().trim();
                  if (!in.isEmpty()) {
                     message = in;
                  }

                  state = 3;
                  in = "";
                  break;
               case 3:
                  System.out.print("Delay: [default '" + delay + "']");
                  String in = br.readLine().trim();
                  if (!in.isEmpty()) {
                     delay = Integer.valueOf(in);
                  }

                  state = 4;
               case 4:
            }

            String s = br.readLine();
            System.out.print("Enter Integer:");
            int var10 = Integer.parseInt(br.readLine());
         } catch (NumberFormatException var11) {
            System.err.println("Invalid Format!");
         } catch (Exception var12) {
         }
      }
   }

   @Override
   public void requestDemigod(String intraServerPassword, byte existingDeity, String existingDeityName) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Player[] players = Players.getInstance().getPlayers();

      for(int x = 0; x < players.length; ++x) {
         if (players[x].getKingdomTemplateId() == Deities.getFavoredKingdom(existingDeity) && (players[x].getPower() == 0 || Servers.localServer.testServer)) {
            MissionPerformer mp = MissionPerformed.getMissionPerformer(players[x].getWurmId());
            if (mp != null) {
               MissionPerformed[] perfs = mp.getAllMissionsPerformed();
               int numsForDeity = 0;
               logger.log(Level.INFO, "Checking if " + players[x].getName() + " can be elevated.");

               for(MissionPerformed mpf : perfs) {
                  Mission m = mpf.getMission();
                  if (m != null) {
                     logger.log(Level.INFO, "Found a mission for " + existingDeityName);
                     if (m.getCreatorType() == 2 && m.getOwnerId() == (long)existingDeity) {
                        ++numsForDeity;
                     }
                  }
               }

               logger.log(Level.INFO, "Found " + numsForDeity + " missions for " + players[x].getName());
               if (Server.rand.nextInt(numsForDeity) > 2) {
                  logger.log(Level.INFO, "Sending ascension to " + players[x].getName());
                  AscensionQuestion asc = new AscensionQuestion(players[x], (long)existingDeity, existingDeityName);
                  asc.sendQuestion();
               }
            }
         }
      }
   }

   @Override
   public String ascend(
      String intraServerPassword,
      int newId,
      String deityName,
      long wurmid,
      byte existingDeity,
      byte gender,
      byte newPower,
      float initialBStr,
      float initialBSta,
      float initialBCon,
      float initialML,
      float initialMS,
      float initialSS,
      float initialSD
   ) {
      try {
         this.validateIntraServerPassword(intraServerPassword);
      } catch (AccessException var20) {
         var20.printStackTrace();
      }

      String toReturn = "";
      if (Servers.localServer.LOGINSERVER) {
         Deity deity = null;
         if (newPower == 2) {
            deity = Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0F, -1.0F);
            if (deity == null) {
               return "Ouch, failed to save your demigod on the login server. Please contact administration";
            }

            StringBuilder builder = new StringBuilder("You have now ascended! ");
            if (initialBStr < 30.0F) {
               builder.append("The other immortals will not fear your strength initially. ");
            } else if (initialBStr < 45.0F) {
               builder.append("You have acceptable strength as a demigod. ");
            } else if (initialBStr < 60.0F) {
               builder.append("Your strength and skills will impress other immortals. ");
            } else {
               builder.append("Your enormous strength will strike fear in other immortals. ");
            }

            if (initialBSta < 30.0F) {
               builder.append("You are not the most vital demigod around so you will have to watch your back in the beginning. ");
            } else if (initialBSta < 45.0F) {
               builder.append("Your vitality is acceptable and will earn respect. ");
            } else if (initialBSta < 60.0F) {
               builder.append("You have good vitality and can expect a bright future as immortal. ");
            } else {
               builder.append("Other immortals will envy your fantastic vitality and avoid confrontations with you. ");
            }

            if (deity.isHealer()) {
               builder.append("Your love and kindness will be a beacon for everyone to follow. ");
            } else if (deity.isHateGod()) {
               builder.append("Your true nature turns out to be based on rage and hate. ");
            }

            if (deity.isForestGod()) {
               builder.append("Love for trees and living things will bind your followers together. ");
            }

            if (deity.isMountainGod()) {
               builder.append("Your followers will look for you in high places and fear and adore you as they do the dragon. ");
            }

            if (deity.isWaterGod()) {
               builder.append("You will be considered the pathfinder and explorer of your kin. ");
            }

            HexMap.VALREI
               .addDemigod(
                  deityName, (long)deity.number, (long)existingDeity, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD
               );
            toReturn = builder.toString();
         } else if (newPower > 2) {
            String sgender = "He";
            String sposs = "his";
            if (gender == 1) {
               sgender = "She";
               sposs = "her";
            }

            Servers.ascend(
               newId, deityName, wurmid, existingDeity, gender, newPower, initialBStr, initialBSta, initialBCon, initialML, initialMS, initialSS, initialSD
            );
            HistoryManager.addHistory(
               deityName,
               "has joined the ranks of true deities. "
                  + sgender
                  + " invites you to join "
                  + sposs
                  + " religion, as "
                  + sgender.toLowerCase()
                  + " will now forever partake in the hunts on Valrei!"
            );
            Server.getInstance()
               .broadCastSafe(
                  deityName
                     + " has joined the ranks of true deities. "
                     + sgender
                     + " invites you to join "
                     + sposs
                     + " religion, as "
                     + sgender.toLowerCase()
                     + " will now forever partake in the hunts on Valrei!"
               );
         }
      } else if (newPower > 2) {
         Deities.ascend(newId, deityName, wurmid, gender, newPower, -1.0F, -1.0F);
         String sgender = "He";
         String sposs = "his";
         if (gender == 1) {
            sgender = "She";
            sposs = "her";
         }

         HistoryManager.addHistory(
            deityName,
            "has joined the ranks of true deities. "
               + sgender
               + " invites you to join "
               + sposs
               + " religion, as "
               + sgender.toLowerCase()
               + " will now forever partake in the hunts on Valrei!"
         );
         Server.getInstance()
            .broadCastSafe(
               deityName
                  + " has joined the ranks of true deities. "
                  + sgender
                  + " invites you to join "
                  + sposs
                  + " religion, as "
                  + sgender.toLowerCase()
                  + " will now forever partake in the hunts on Valrei!"
            );
      }

      return toReturn;
   }

   @Override
   public final int[] getPremTimeSilvers(String intraServerPassword, long wurmId) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
      if (info != null) {
         try {
            if (!info.loaded) {
               info.load();
            }

            if (info.getPaymentExpire() > 0L && info.awards != null) {
               return new int[]{info.awards.getMonthsPaidEver(), info.awards.getSilversPaidEver()};
            }
         } catch (IOException var6) {
         }
      }

      return emptyIntZero;
   }

   @Override
   public void awardPlayer(String intraServerPassword, long wurmid, String name, int days, int months) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      Server.addPendingAward(new PendingAward(wurmid, name, days, months));
   }

   @Override
   public boolean requestDeityMove(String intraServerPassword, int deityNum, int desiredHex, String guide) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (Servers.localServer.LOGINSERVER) {
         EpicEntity entity = HexMap.VALREI.getEntity((long)deityNum);
         if (entity != null) {
            logger.log(Level.INFO, "Requesting move for " + entity);
            MapHex mh = HexMap.VALREI.getMapHex(desiredHex);
            if (mh != null) {
               entity.setNextTargetHex(desiredHex);
               entity.broadCastWithName(" was guided by " + guide + " towards " + mh.getName() + ".");
               entity.sendEntityData();
               return true;
            }

            logger.log(Level.INFO, "No hex for " + desiredHex);
         } else {
            logger.log(Level.INFO, "Requesting move for nonexistant " + deityNum);
         }
      }

      return false;
   }

   private void validateIntraServerPassword(String intraServerPassword) throws AccessException {
      if (!Servers.localServer.INTRASERVERPASSWORD.equals(intraServerPassword)) {
         throw new AccessException("Access denied.");
      }
   }

   @Override
   public boolean isFeatureEnabled(String intraServerPassword, int aFeatureId) throws RemoteException {
      this.validateIntraServerPassword(intraServerPassword);
      if (logger.isLoggable(Level.FINER)) {
         logger.finer(this.getRemoteClientDetails() + " isFeatureEnabled " + aFeatureId);
      }

      return Features.Feature.isFeatureEnabled(aFeatureId);
   }

   @Override
   public boolean setPlayerFlag(String intraServerPassword, long wurmid, int flag, boolean set) throws RemoteException {
      return false;
   }

   public boolean setPlayerFlag(long wurmid, int flag, boolean set) {
      PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
      if (pinf != null) {
         pinf.setFlag(flag, set);
         return true;
      } else {
         return false;
      }
   }
}
