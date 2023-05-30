package com.wurmonline.server.players;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.intra.TimeTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcPlayerStatus;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerInfoFactory implements TimeConstants, MiscConstants {
   private static final ConcurrentHashMap<String, PlayerInfo> playerInfos = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Long, PlayerInfo> playerInfosWurmId = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Long, PlayerState> playerStatus = new ConcurrentHashMap<>();
   private static final Set<Long> failedIds = new HashSet<>();
   private static final ConcurrentLinkedDeque<PlayerState> statesToUpdate = new ConcurrentLinkedDeque<>();
   private static final ConcurrentHashMap<Long, PlayerState> friendsToUpdate = new ConcurrentHashMap<>();
   private static final Logger logger = Logger.getLogger(PlayerInfoFactory.class.getName());
   private static final Logger deletelogger = Logger.getLogger("deletions");
   private static final String LOAD_AWARDS = "SELECT * FROM AWARDS";
   private static final String GET_ALL_PLAYERS = "SELECT * FROM PLAYERS";
   private static final long EXPIRATION_TIME = 7257600000L;
   protected static final long NOTICE_TIME = 604800000L;
   private static final Map<Long, Set<Referer>> referrers = new ConcurrentHashMap<>();
   private static final String LOAD_REFERERS = "SELECT * FROM REFERERS";
   private static final String SET_REFERER = "UPDATE REFERERS SET HANDLED=1, MONEY=? WHERE WURMID=? AND REFERER=?";
   private static final String ADD_REFERER = "INSERT INTO REFERERS (WURMID, REFERER,HANDLED, MONEY ) VALUES(?,?,0,0)";
   private static final String REVERT_REFERER = "UPDATE REFERERS SET HANDLED=0, MONEY=0 WHERE WURMID=? AND REFERER=?";
   private static final String RESET_SCENARIOKARMA = "UPDATE PLAYERS SET SCENARIOKARMA=0";
   private static int deletedPlayers = 0;
   public static final String NOPERMISSION = "NO";
   public static final String RETRIEVAL = " Retrieval info updated.";
   private static long OFFLINETIME_UNTIL_FREEZE = 1296000000L;
   private static final LinkedList<WurmRecord> championRecords = new LinkedList<>();

   private PlayerInfoFactory() {
   }

   public static PlayerInfo createPlayerInfo(String name) {
      name = LoginHandler.raiseFirstLetter(name);
      return (PlayerInfo)(playerInfos.containsKey(name) ? playerInfos.get(name) : new DbPlayerInfo(name));
   }

   public static void addPlayerInfo(PlayerInfo info) {
      if (!doesPlayerInfoExist(info.getName())) {
         playerInfos.put(info.name, info);
         playerInfosWurmId.put(info.wurmId, info);
      }
   }

   private static boolean doesPlayerInfoExist(String aName) {
      return playerInfos.containsKey(aName);
   }

   public static long getPlayerMoney() {
      PlayerInfo[] p = getPlayerInfos();
      long toRet = 0L;

      for(PlayerInfo lElement : p) {
         if (lElement.currentServer == Servers.localServer.id || Servers.localServer.LOGINSERVER) {
            toRet += lElement.money;
         }
      }

      return toRet;
   }

   public static final void loadReferers() throws IOException {
      long start = System.nanoTime();
      int loadedReferrers = 0;
      if (Servers.localServer.id == Servers.loginServer.id) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM REFERERS");

            Referer r;
            Set<Referer> s;
            for(rs = ps.executeQuery(); rs.next(); s.add(r)) {
               r = new Referer(rs.getLong("WURMID"), rs.getLong("REFERER"), rs.getBoolean("MONEY"), rs.getBoolean("HANDLED"));
               Long wid = new Long(r.getWurmid());
               s = referrers.get(wid);
               if (s == null) {
                  s = new HashSet<>();
                  referrers.put(wid, s);
                  ++loadedReferrers;
               }
            }
         } catch (SQLException var14) {
            logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
            throw new IOException(var14);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + loadedReferrers + " referrers from the database took " + (float)(end - start) / 1000000.0F + " ms");
         }
      } else {
         logger.info("Not Loading referrers from the database as this is not the login server");
      }
   }

   public static final boolean addReferrer(long wurmid, long referrer) throws IOException {
      Set<Referer> s = referrers.get(new Long(wurmid));
      if (s != null) {
         for(Referer r : s) {
            if (r.getReferer() == referrer) {
               return false;
            }
         }
      } else {
         s = new HashSet<>();
         referrers.put(new Long(wurmid), s);
      }

      Referer r = new Referer(wurmid, referrer);
      s.add(r);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("INSERT INTO REFERERS (WURMID, REFERER,HANDLED, MONEY ) VALUES(?,?,0,0)");
         ps.setLong(1, wurmid);
         ps.setLong(2, referrer);
         ps.executeUpdate();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to add referrer " + referrer + " for " + wurmid);
         throw new IOException(var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      PlayerInfo pinf = getPlayerInfoWithWurmId(referrer);
      if (pinf != null) {
         pinf.setReferedby(wurmid);
      }

      return true;
   }

   public static final boolean acceptReferer(long wurmid, long referrer, boolean money) throws IOException {
      Set<Referer> s = referrers.get(new Long(wurmid));
      if (s != null) {
         boolean found = false;

         for(Referer r : s) {
            if (r.getReferer() == referrer) {
               found = true;
               r.setMoney(money);
               r.setHandled(true);
               break;
            }
         }

         if (found) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            boolean ex;
            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement("UPDATE REFERERS SET HANDLED=1, MONEY=? WHERE WURMID=? AND REFERER=?");
               ps.setBoolean(1, money);
               ps.setLong(2, wurmid);
               ps.setLong(3, referrer);
               ps.executeUpdate();
               ex = true;
            } catch (SQLException var13) {
               logger.log(Level.WARNING, "Failed to set referrer " + referrer + " for " + wurmid + " and money=" + money);
               throw new IOException(var13);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }

            return ex;
         }
      }

      return false;
   }

   public static final void revertReferer(long wurmid, long referrer) throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE REFERERS SET HANDLED=0, MONEY=0 WHERE WURMID=? AND REFERER=?");
         ps.setLong(1, wurmid);
         ps.setLong(2, referrer);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to revert referrer " + referrer + " for " + wurmid);
         throw new IOException(var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final Map<String, Byte> getReferrers(long wurmid) {
      Map<String, Byte> map = new HashMap<>();
      Set<Referer> s = referrers.get(new Long(wurmid));
      if (s != null) {
         for(Referer r : s) {
            byte type = 0;
            if (r.isHandled()) {
               if (r.isMoney()) {
                  type = 1;
               } else {
                  type = 2;
               }
            }

            String name = String.valueOf(r.getReferer());

            try {
               name = Players.getInstance().getNameFor(r.getReferer());
            } catch (Exception var9) {
               logger.log(Level.WARNING, "No name found for " + r.getReferer());
            }

            map.put(name, type);
         }
      }

      return map;
   }

   public static final boolean addMoneyToBank(long wurmid, long moneyToAdd, String transactionDetail) throws Exception {
      try {
         Player p = Players.getInstance().getPlayer(wurmid);
         if (moneyToAdd >= 1000000L) {
            logger.log(Level.INFO, "Adding " + moneyToAdd + " to " + p.getName(), (Throwable)(new Exception()));
         }

         p.addMoney(moneyToAdd);
         long money = p.getMoney();
         new MoneyTransfer(p.getName(), p.getWurmId(), money, moneyToAdd, transactionDetail, (byte)0, "");
         Change change = new Change(moneyToAdd);
         Change current = new Change(money);
         p.save();
         p.getCommunicator()
            .sendSafeServerMessage(
               "An amount of " + change.getChangeString() + " has been added to your bank account. Current balance is " + current.getChangeString() + "."
            );
         if (transactionDetail.startsWith("Referred by ")) {
            p.getSaveFile().addToSleep(3600);
            String sleepString = "You received an hour of sleep bonus which will increase your skill gain speed.";
            p.getCommunicator().sendSafeServerMessage("You received an hour of sleep bonus which will increase your skill gain speed.");
         }
      } catch (NoSuchPlayerException var11) {
         PlayerInfo p = getPlayerInfoWithWurmId(wurmid);
         if (p.wurmId <= 0L) {
            return false;
         }

         if (moneyToAdd >= 1000000L) {
            logger.log(Level.INFO, "Adding " + moneyToAdd + " to " + p.getName(), (Throwable)(new Exception()));
         }

         p.setMoney(p.money + moneyToAdd);
         p.save();
         if (transactionDetail.startsWith("Referred by ")) {
            p.addToSleep(3600);
         }

         if (Servers.localServer.id != p.currentServer) {
            new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)5, "", false);
         } else {
            new MoneyTransfer(p.getName(), p.wurmId, p.money, moneyToAdd, transactionDetail, (byte)5, "");
         }
      }

      return true;
   }

   public static final boolean addPlayingTime(long wurmid, int months, int days, String transactionDetail) throws Exception {
      if (wurmid < 0L || transactionDetail == null || transactionDetail.length() == 0) {
         throw new WurmServerException("Illegal arguments. Check if name or transaction detail is null or empty strings.");
      } else if (months >= 0 && days >= 0) {
         long timeToAdd = 0L;
         if (days != 0) {
            timeToAdd = (long)days * 86400000L;
         }

         if (months != 0) {
            timeToAdd += (long)months * 86400000L * 30L;
         }

         try {
            Player p = Players.getInstance().getPlayer(wurmid);
            long currTime = p.getPaymentExpire();
            currTime = Math.max(currTime, System.currentTimeMillis());
            currTime += timeToAdd;
            if (transactionDetail.startsWith("Referred by ")) {
               p.getSaveFile().addToSleep(3600);
               String sleepString = "You received an hour of sleep bonus which will increase your skill gain speed.";
               p.getCommunicator().sendSafeServerMessage("You received an hour of sleep bonus which will increase your skill gain speed.");
            }

            p.setPaymentExpire(currTime);
            new TimeTransfer(p.getName(), p.getWurmId(), months, false, days, transactionDetail);
            p.save();
            p.getCommunicator().sendNormalServerMessage("You now have premier playing time until " + WurmCalendar.formatGmt(currTime) + ".");
         } catch (NoSuchPlayerException var11) {
            PlayerInfo p = getPlayerInfoWithWurmId(wurmid);
            if (p.wurmId <= 0L) {
               return false;
            }

            long currTime = p.getPaymentExpire();
            currTime = Math.max(currTime, System.currentTimeMillis());
            currTime += timeToAdd;
            p.setPaymentExpire(currTime);
            if (transactionDetail.startsWith("Referred by ")) {
               p.addToSleep(3600);
            }

            if (p.currentServer != Servers.localServer.id) {
               new TimeTransfer(p.getName(), p.wurmId, months, false, days, transactionDetail, false);
            } else {
               new TimeTransfer(p.getName(), p.wurmId, months, false, days, transactionDetail);
            }
         }

         return true;
      } else {
         throw new WurmServerException("Illegal arguments. Make sure that the values for days and months are not negative.");
      }
   }

   public static final void pruneRanks(long now) {
      for(PlayerInfo pinf : getPlayerInfos()) {
         if (pinf.getRank() > 1000 && now - pinf.lastModifiedRank > 864000000L) {
            try {
               pinf.setRank((int)((double)pinf.getRank() * 0.975));
               logger.log(Level.INFO, "Set rank of " + pinf.getName() + " to " + pinf.getRank());
            } catch (IOException var7) {
               logger.log(Level.INFO, pinf.getName() + ": " + var7.getMessage());
            }
         }
      }
   }

   public static final void pollPremiumPlayers() {
      for(PlayerInfo info : getPlayerInfos()) {
         if (info.timeToCheckPrem-- <= 0) {
            info.timeToCheckPrem = (int)((86400000L + System.currentTimeMillis()) / 1000L) + Server.rand.nextInt(200);
            if (info.getPower() <= 0 && info.paymentExpireDate > 0L && !info.isFlagSet(63) && info.awards != null) {
               if (System.currentTimeMillis() - info.awards.getLastTickedDay() > 86400000L) {
                  boolean wasPrem = info.awards.getLastTickedDay() < info.paymentExpireDate;
                  if (info.isQAAccount() || info.paymentExpireDate > System.currentTimeMillis() || wasPrem) {
                     info.awards.setDaysPrem(info.awards.getDaysPrem() + 1);
                     info.timeToCheckPrem = 86400 + Server.rand.nextInt(200);
                     if (info.awards.getDaysPrem() % 28 == 0) {
                        info.awards.setMonthsPaidSinceReset(info.awards.getMonthsPaidSinceReset() + 1);
                        info.awards.setMonthsPaidInARow(info.awards.getMonthsPaidInARow() + 1);
                        AwardLadder.award(info, true);
                     }
                  } else if (info.awards.getMonthsPaidInARow() > 0) {
                     info.awards.setMonthsPaidInARow(0);
                  }

                  info.awards.setLastTickedDay(System.currentTimeMillis());
                  info.awards.update();
               } else {
                  info.timeToCheckPrem = (int)((info.awards.getLastTickedDay() + 86400000L - System.currentTimeMillis()) / 1000L) + 100;
               }
            }
         }
      }
   }

   public static final void checkIfDeleteOnePlayer() {
      long now = System.currentTimeMillis();
      boolean loginServer = Servers.localServer.LOGINSERVER;
      if (Constants.pruneDb && Server.getSecondsUptime() > 30) {
         for(PlayerInfo pinf : getPlayerInfos()) {
            if (pinf.creationDate < now - 604800000L && !pinf.isQAAccount()) {
               if (pinf.power == 0
                  && pinf.playingTime < 86400000L
                  && pinf.lastLogout < now - 7257600000L
                  && (pinf.paymentExpireDate == 0L || pinf.isFlagSet(63))
                  && Servers.localServer.id != 20
                  && pinf.currentServer != 20
                  && pinf.lastServer != 20) {
                  try {
                     if (pinf.money < 50000L) {
                        ++deletedPlayers;
                        Village[] vills = Villages.getVillages();

                        for(Village v : vills) {
                           if (v.getMayor() != null && v.getMayor().getId() == pinf.wurmId) {
                              v.disband(pinf.getName() + " deleted");
                           }
                        }

                        for(Item item : Items.loadAllItemsForCreatureWithId(pinf.wurmId, pinf.hasMovedInventory())) {
                           if (!item.isIndestructible() && !item.isVillageDeed() && !item.isHomesteadDeed() && WurmId.getType(item.getWurmId()) != 19) {
                              IntraServerConnection.deleteItem(item.getWurmId(), pinf.hasMovedInventory());
                              Items.removeItem(item.getWurmId());
                           }
                        }

                        IntraServerConnection.deletePlayer(pinf.wurmId);
                        deletelogger.log(Level.INFO, "Deleted " + pinf.name + ", email[" + pinf.emailAddress + "] " + pinf.wurmId);
                        MissionPerformed.deleteMissionPerformer(pinf.wurmId);
                        playerStatus.remove(pinf.wurmId);
                        playerInfos.remove(pinf.getName());
                        return;
                     }

                     if (loginServer) {
                        sendDeletePreventLetter(pinf);
                     }

                     deletelogger.log(Level.INFO, "Kept and charged 5 silver from " + pinf.name + ", " + pinf);
                     pinf.setMoney(pinf.money - 50000L);
                     pinf.lastLogout = now;
                     pinf.setFlag(8, false);
                     pinf.save();
                     return;
                  } catch (IOException var12) {
                     logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                  }
               } else if (!pinf.isOnlineHere() && now - pinf.lastLogout > OFFLINETIME_UNTIL_FREEZE) {
                  if (!pinf.hasMovedInventory() && !failedIds.contains(pinf.wurmId)) {
                     if (Items.moveItemsToFreezerFor(pinf.wurmId)) {
                        pinf.setMovedInventory(true);
                        deletelogger.log(Level.INFO, "Froze items for " + pinf.getName());
                        return;
                     }

                     failedIds.add(pinf.wurmId);
                  }
               } else if (pinf.power != 0
                  || pinf.playingTime >= 86400000L
                  || pinf.lastLogout >= now - 7257600000L - 604800000L
                  || pinf.paymentExpireDate != 0L && !pinf.isFlagSet(63)) {
                  if (pinf.power == 0 && pinf.paymentExpireDate > now && pinf.paymentExpireDate < now + 604800000L) {
                     if (loginServer && !pinf.isFlagSet(8)) {
                        sendPremiumWarningLetter(pinf);
                     }
                  } else if (pinf.power == 0 && pinf.paymentExpireDate < now && !pinf.isFlagSet(9)) {
                     Server.addExpiry();
                     pinf.setFlag(9, true);
                  }
               } else if (loginServer && !pinf.isFlagSet(8)) {
                  sendDeleteLetter(pinf);
               }
            }
         }
      }
   }

   public static final void sendDeletePreventLetter(PlayerInfo pinf) {
      try {
         String email = Mailer.getAccountDelPreventionMail();
         email = email.replace("@pname", pinf.getName());
         Mailer.sendMail(WebInterfaceImpl.mailAccount, pinf.emailAddress, "Wurm Online deletion protection", email);
      } catch (Exception var2) {
         logger.log(Level.INFO, var2.getMessage(), (Throwable)var2);
      }
   }

   public static final void sendDeleteLetter(PlayerInfo pinf) {
      try {
         String email = Mailer.getAccountDelMail();
         email = email.replace("@pname", pinf.getName());
         Mailer.sendMail(WebInterfaceImpl.mailAccount, pinf.emailAddress, "Wurm Online character deletion", email);
      } catch (Exception var2) {
         logger.log(Level.INFO, var2.getMessage(), (Throwable)var2);
      }

      pinf.setFlag(8, true);
   }

   public static final void sendPremiumWarningLetter(PlayerInfo pinf) {
      if (pinf.awards != null) {
         String rewString = "We have no award specified at this level of total premium time since this program started";
         String reward = "unspecified";
         String nextRewardMonth = "lots more";
         int ql = (int)AwardLadder.consecutiveItemQL(pinf.awards.getMonthsPaidInARow() + 1);
         AwardLadder next = pinf.awards.getNextReward();
         if (next != null) {
            rewString = "Your next award is <i>@award</i> which will occur when you have @nextmonths months of premium time since this program started";
            reward = next.getName();
            nextRewardMonth = next.getMonthsRequiredReset() + "";
            rewString = rewString.replace("@award", reward);
            rewString = rewString.replace("@nextmonths", nextRewardMonth);
         }

         try {
            String email = Mailer.getPremExpiryMail();
            email = email.replace("@pname", pinf.getName());
            email = email.replace("@reward", rewString);
            email = email.replace("@qualityLevel", ql + "");
            email = email.replace("@currmonths", pinf.awards.getMonthsPaidSinceReset() + "");
            Mailer.sendMail(WebInterfaceImpl.mailAccount, pinf.emailAddress, "Wurm Online premium expiry warning", email);
         } catch (Exception var7) {
            logger.log(Level.INFO, var7.getMessage(), (Throwable)var7);
         }

         pinf.setFlag(8, true);
      }
   }

   public static final Logger getDeleteLogger() {
      return deletelogger;
   }

   public static final void loadAwards() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM AWARDS");
         rs = ps.executeQuery();

         while(rs.next()) {
            new Awards(
               rs.getLong("WURMID"),
               rs.getInt("DAYSPREM"),
               rs.getInt("MONTHSEVER"),
               rs.getInt("CONSECMONTHS"),
               rs.getInt("MONTHSPREM"),
               rs.getInt("SILVERSPURCHASED"),
               rs.getLong("LASTTICKEDPREM"),
               rs.getInt("CURRENTLOYALTY"),
               rs.getInt("TOTALLOYALTY"),
               false
            );
         }
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final void loadPlayerInfos() throws IOException {
      Players.loadAllArtists();
      loadAwards();
      long now = System.currentTimeMillis();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (Constants.pruneDb) {
            logger.log(Level.INFO, "Loading player infos. Going to prune DB.");
         }

         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM PLAYERS");
         rs = ps.executeQuery();

         while(rs.next()) {
            String name = rs.getString("NAME");
            name = LoginHandler.raiseFirstLetter(name);
            DbPlayerInfo pinf = new DbPlayerInfo(name);
            pinf.wurmId = rs.getLong("WURMID");
            pinf.password = rs.getString("PASSWORD");
            pinf.playingTime = rs.getLong("PLAYINGTIME");
            pinf.reimbursed = rs.getBoolean("REIMBURSED");
            pinf.plantedSign = rs.getLong("PLANTEDSIGN");
            pinf.ipaddress = rs.getString("IPADDRESS");
            pinf.banned = rs.getBoolean("BANNED");
            pinf.power = rs.getByte("POWER");
            pinf.rank = rs.getInt("RANK");
            pinf.maxRank = rs.getInt("MAXRANK");
            pinf.lastModifiedRank = rs.getLong("LASTMODIFIEDRANK");
            pinf.mayHearDevTalk = rs.getBoolean("DEVTALK");
            pinf.paymentExpireDate = rs.getLong("PAYMENTEXPIRE");
            pinf.lastWarned = rs.getLong("LASTWARNED");
            pinf.warnings = rs.getShort("WARNINGS");
            pinf.lastCheated = rs.getLong("CHEATED");
            pinf.lastFatigue = rs.getLong("LASTFATIGUE");
            pinf.fatigueSecsLeft = rs.getInt("FATIGUE");
            pinf.fatigueSecsToday = rs.getInt("FATIGUETODAY");
            pinf.fatigueSecsYesterday = rs.getInt("FATIGUEYDAY");
            pinf.dead = rs.getBoolean("DEAD");
            pinf.version = rs.getLong("VERSION");
            pinf.money = rs.getLong("MONEY");
            pinf.climbing = rs.getBoolean("CLIMBING");
            pinf.banexpiry = rs.getLong("BANEXPIRY");
            pinf.banreason = rs.getString("BANREASON");
            pinf.emailAddress = rs.getString("EMAIL");
            if (pinf.banreason == null) {
               pinf.banreason = "";
            }

            pinf.logging = rs.getBoolean("LOGGING");
            pinf.referrer = rs.getLong("REFERRER");
            pinf.isPriest = rs.getBoolean("PRIEST");
            pinf.bed = rs.getLong("BED");
            pinf.sleep = rs.getInt("SLEEP");
            pinf.isTheftWarned = rs.getBoolean("THEFTWARNED");
            pinf.noReimbursementLeft = rs.getBoolean("NOREIMB");
            pinf.deathProtected = rs.getBoolean("DEATHPROT");
            pinf.tutorialLevel = rs.getInt("TUTORIALLEVEL");
            pinf.autoFighting = rs.getBoolean("AUTOFIGHT");
            pinf.appointments = rs.getLong("APPOINTMENTS");
            pinf.playerAssistant = rs.getBoolean("PA");
            pinf.mayAppointPlayerAssistant = rs.getBoolean("APPOINTPA");
            pinf.seesPlayerAssistantWindow = rs.getBoolean("PAWINDOW");
            pinf.hasFreeTransfer = rs.getBoolean("FREETRANSFER");
            pinf.votedKing = rs.getBoolean("VOTEDKING");
            byte kingdom = rs.getByte("KINGDOM");
            Players.getInstance().registerNewKingdom(pinf.wurmId, kingdom);
            if (pinf.playingTime < 0L) {
               pinf.playingTime = 0L;
            }

            pinf.alignment = rs.getFloat("ALIGNMENT");
            byte deityNum = rs.getByte("DEITY");
            if (deityNum > 0) {
               Deity d = Deities.getDeity(deityNum);
               pinf.deity = d;
            } else {
               pinf.deity = null;
            }

            pinf.favor = rs.getFloat("FAVOR");
            pinf.faith = rs.getFloat("FAITH");
            byte gid = rs.getByte("GOD");
            if (gid > 0) {
               Deity d = Deities.getDeity(gid);
               pinf.god = d;
            }

            pinf.lastChangedDeity = rs.getLong("LASTCHANGEDDEITY");
            pinf.changedKingdom = rs.getByte("NUMSCHANGEDKINGDOM");
            pinf.realdeath = rs.getByte("REALDEATH");
            pinf.muted = rs.getBoolean("MUTED");
            pinf.muteTimes = rs.getShort("MUTETIMES");
            pinf.lastFaith = rs.getLong("LASTFAITH");
            pinf.numFaith = rs.getByte("NUMFAITH");
            pinf.creationDate = rs.getLong("CREATIONDATE");
            long face = rs.getLong("FACE");
            if (face == 0L) {
               face = Server.rand.nextLong();
            }

            pinf.face = face;
            pinf.reputation = rs.getInt("REPUTATION");
            pinf.lastPolledReputation = rs.getLong("LASTPOLLEDREP");
            if (pinf.lastPolledReputation == 0L) {
               pinf.lastPolledReputation = System.currentTimeMillis();
            }

            int titnum = rs.getInt("TITLE");
            if (titnum > 0) {
               pinf.title = Titles.Title.getTitle(titnum);
            }

            try {
               int secTitleNum = rs.getInt("SECONDTITLE");
               if (secTitleNum > 0) {
                  pinf.secondTitle = Titles.Title.getTitle(secTitleNum);
               }
            } catch (SQLException var20) {
               logger.severe("You may need to run the script addSecondTitle.sql!");
               logger.severe(var20.getMessage());
               pinf.secondTitle = null;
            }

            pinf.pet = rs.getLong("PET");
            pinf.lastLogout = rs.getLong("LASTLOGOUT");
            pinf.nicotine = rs.getFloat("NICOTINE");
            pinf.alcohol = rs.getFloat("ALCOHOL");
            pinf.nicotineAddiction = rs.getLong("NICOTINETIME");
            pinf.alcoholAddiction = rs.getLong("ALCOHOLTIME");
            pinf.mayMute = rs.getBoolean("MAYMUTE");
            pinf.overRideShop = rs.getBoolean("MAYUSESHOP");
            pinf.muteexpiry = rs.getLong("MUTEEXPIRY");
            pinf.mutereason = rs.getString("MUTEREASON");
            pinf.lastServer = rs.getInt("LASTSERVER");
            pinf.currentServer = rs.getInt("CURRENTSERVER");
            pinf.pwQuestion = rs.getString("PWQUESTION");
            pinf.pwAnswer = rs.getString("PWANSWER");
            pinf.lastChangedVillage = rs.getLong("CHANGEDVILLAGE");
            pinf.fightmode = rs.getByte("FIGHTMODE");
            pinf.nextAffinity = rs.getLong("NEXTAFFINITY");
            pinf.lastvehicle = rs.getLong("VEHICLE");
            pinf.lastTaggedKindom = rs.getByte("ENEMYTERR");
            pinf.lastMovedBetweenKingdom = rs.getLong("LASTMOVEDTERR");
            pinf.priestType = rs.getByte("PRIESTTYPE");
            pinf.lastChangedPriestType = rs.getLong("LASTCHANGEDPRIEST");
            pinf.hasMovedInventory = rs.getBoolean("MOVEDINV");
            pinf.hasSkillGain = rs.getBoolean("HASSKILLGAIN");
            pinf.lastTriggerEffect = rs.getInt("LASTTRIGGER");
            pinf.lastChangedKindom = rs.getLong("LASTCHANGEDKINGDOM");
            pinf.championTimeStamp = rs.getLong("LASTLOSTCHAMPION");
            pinf.championPoints = rs.getShort("CHAMPIONPOINTS");
            pinf.champChanneling = rs.getFloat("CHAMPCHANNELING");
            pinf.epicKingdom = rs.getByte("EPICKINGDOM");
            pinf.epicServerId = rs.getInt("EPICSERVER");
            pinf.chaosKingdom = rs.getByte("CHAOSKINGDOM");
            pinf.hotaWins = rs.getShort("HOTA_WINS");
            pinf.spamMode = rs.getBoolean("SPAMMODE");
            pinf.karma = rs.getInt("KARMA");
            pinf.maxKarma = rs.getInt("MAXKARMA");
            pinf.totalKarma = rs.getInt("TOTALKARMA");
            pinf.blood = rs.getByte("BLOOD");
            pinf.flags = rs.getLong("FLAGS");
            pinf.flags2 = rs.getLong("FLAGS2");
            pinf.abilities = rs.getLong("ABILITIES");
            pinf.abilityTitle = rs.getInt("ABILITYTITLE");
            pinf.undeadType = rs.getByte("UNDEADTYPE");
            pinf.undeadKills = rs.getInt("UNDEADKILLS");
            pinf.undeadPlayerKills = rs.getInt("UNDEADPKILLS");
            pinf.undeadPlayerSeconds = rs.getInt("UNDEADPSECS");
            pinf.moneyEarnedBySellingEver = rs.getLong("MONEYSALES");
            pinf.setFlagBits(pinf.flags);
            pinf.setFlag2Bits(pinf.flags2);
            pinf.setAbilityBits(pinf.abilities);
            pinf.scenarioKarma = rs.getInt("SCENARIOKARMA");
            pinf.loaded = true;
            if ((Servers.localServer.id == pinf.currentServer || Servers.localServer.LOGINSERVER) && pinf.paymentExpireDate > 0L) {
               pinf.awards = Awards.getAwards(pinf.wurmId);
            }

            playerInfos.put(name, pinf);
            playerInfosWurmId.put(pinf.wurmId, pinf);
            if (Servers.isThisLoginServer()) {
               playerStatus.put(
                  pinf.wurmId, new PlayerState(pinf.currentServer, pinf.wurmId, pinf.name, pinf.lastLogin, pinf.lastLogout, PlayerOnlineStatus.OFFLINE)
               );
            }
         }
      } catch (SQLException var21) {
         logger.log(Level.WARNING, var21.getMessage(), (Throwable)var21);
         throw new IOException(var21);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.currentTimeMillis();
         logger.info("Loaded " + playerInfos.size() + " PlayerInfos from the database took " + (end - now) + " ms");
      }
   }

   public static final void transferPlayersToWild() {
      logger.log(Level.INFO, "Starting to migrate accounts");
      ServerEntry localServer = Servers.localServer;
      ServerEntry wildServer = Servers.getServerWithId(3);
      String targetIp = wildServer.INTRASERVERADDRESS;
      int targetPort = Integer.parseInt(wildServer.INTRASERVERPORT);
      String serverpass = wildServer.INTRASERVERPASSWORD;
      int tilex = wildServer.SPAWNPOINTJENNX;
      int tiley = wildServer.SPAWNPOINTJENNY;

      for(PlayerInfo p : playerInfos.values()) {
         if ((p.getPaymentExpire() > 0L || p.money >= 50000L) && p.currentServer == localServer.id && !p.banned) {
            try {
               Player player = new Player(p);
               Server.getInstance().addPlayer(player);
               player.checkBodyInventoryConsistency();
               player.loadSkills();
               Items.loadAllItemsForCreature(player, player.getStatus().getInventoryId());
               player.getBody().load();
               PlayerTransfer.willItemsTransfer(player, true, 3);
               tilex = wildServer.SPAWNPOINTJENNX;
               tiley = wildServer.SPAWNPOINTJENNY;
               if (player.getKingdomId() == 3) {
                  tilex = wildServer.SPAWNPOINTLIBX;
                  tiley = wildServer.SPAWNPOINTLIBY;
               } else if (player.getKingdomId() == 2) {
                  tilex = wildServer.SPAWNPOINTMOLX;
                  tiley = wildServer.SPAWNPOINTMOLY;
               }

               PlayerTransfer pt = new PlayerTransfer(
                  Server.getInstance(), player, targetIp, targetPort, serverpass, 3, tilex, tiley, true, false, player.getKingdomId()
               );
               pt.copiedToLoginServer = true;
               Server.getInstance().addIntraCommand(pt);
            } catch (Exception var11) {
               logger.log(Level.INFO, var11.getMessage(), (Throwable)var11);
            }
         }
      }

      logger.log(Level.INFO, "Created intra commands");
   }

   public static final PlayerInfo[] getPlayerInfos() {
      return playerInfos.values().toArray(new PlayerInfo[playerInfos.size()]);
   }

   public static final PlayerInfo[] getPlayerInfosWithEmail(String email) {
      Set<PlayerInfo> infos = new HashSet<>();

      for(PlayerInfo info : playerInfos.values()) {
         if (info == null) {
            logger.log(Level.WARNING, "getPlayerInfosWithEmail() NULL in playerInfos.values()??");
         } else {
            try {
               info.load();
               if (wildCardMatch(info.emailAddress.toLowerCase(), email.toLowerCase())) {
                  infos.add(info);
               }
            } catch (IOException var5) {
               logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            }
         }
      }

      return infos.toArray(new PlayerInfo[infos.size()]);
   }

   public static final PlayerInfo[] getPlayerInfosWithIpAddress(String ipaddress) {
      Set<PlayerInfo> infos = new HashSet<>();

      for(PlayerInfo info : playerInfos.values()) {
         if (info == null) {
            logger.log(Level.WARNING, "getPlayerInfosWithIpAddress() NULL in playerInfos.values()??");
         } else {
            try {
               info.load();
               if (info.ipaddress != null && wildCardMatch(info.ipaddress, ipaddress)) {
                  infos.add(info);
               }
            } catch (IOException var5) {
               logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
            }
         }
      }

      return infos.toArray(new PlayerInfo[infos.size()]);
   }

   public static boolean wildCardMatch(String text, String pattern) {
      String[] cards = pattern.split("\\*");
      int offset = 0;
      boolean first = true;

      for(String card : cards) {
         if (card.length() > 0) {
            int idx = text.indexOf(card, offset);
            if (idx == -1 || first && idx != 0) {
               return false;
            }

            offset = idx + card.length();
         }

         first = false;
      }

      return offset >= text.length() || pattern.endsWith("*");
   }

   public static PlayerInfo getPlayerInfoWithWurmId(long wurmId) {
      return playerInfosWurmId.get(wurmId);
   }

   public static Optional<PlayerInfo> getPlayerInfoOptional(long wurmId) {
      return Optional.ofNullable(playerInfosWurmId.get(wurmId));
   }

   public static PlayerInfo getPlayerInfoWithName(String name) {
      return playerInfos.get(LoginHandler.raiseFirstLetter(name));
   }

   public static Optional<PlayerInfo> getPlayerInfoOptional(String name) {
      return Optional.ofNullable(playerInfos.get(LoginHandler.raiseFirstLetter(name)));
   }

   public static Map<Long, byte[]> getPlayerStates(long[] wurmids) throws RemoteException, WurmServerException {
      if (Servers.localServer.id != Servers.loginServer.id) {
         LoginServerWebConnection lsw = new LoginServerWebConnection();
         return lsw.getPlayerStates(wurmids);
      } else {
         Map<Long, byte[]> toReturn = new HashMap<>();
         if (wurmids.length > 0) {
            for(int x = 0; x < wurmids.length; ++x) {
               toReturn.put(wurmids[x], new PlayerState(wurmids[x]).encode());
            }
         } else {
            for(PlayerState pState : playerStatus.values()) {
               toReturn.put(pState.getPlayerId(), pState.encode());
            }
         }

         return toReturn;
      }
   }

   public static PlayerState getPlayerState(long playerWurmId) {
      return playerStatus.get(playerWurmId);
   }

   private static boolean playerJustTransfered(PlayerState playerState) {
      PlayerInfo pinf = getPlayerInfoWithWurmId(playerState.getPlayerId());
      if (pinf != null) {
         try {
            pinf.load();
            if (pinf.currentServer != Servers.getLocalServerId()) {
               return true;
            }
         } catch (IOException var3) {
            logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         }
      }

      return false;
   }

   public static final void setPlantedSignFalse() {
      for(PlayerInfo info : playerInfos.values()) {
         info.plantedSign = 0L;
      }
   }

   public static boolean doesPlayerExist(String name) {
      name = LoginHandler.raiseFirstLetter(name);
      return doesPlayerInfoExist(name);
   }

   public static String[] getAccountsForEmail(String email) {
      Set<String> set = new HashSet<>();

      for(PlayerInfo info : playerInfos.values()) {
         if (info.emailAddress.toLowerCase().equals(email.toLowerCase())) {
            set.add(info.name);
         }
      }

      return set.toArray(new String[set.size()]);
   }

   public static PlayerInfo[] getPlayerInfosForEmail(String email) {
      Set<PlayerInfo> set = new HashSet<>();

      for(PlayerInfo info : playerInfos.values()) {
         if (info.emailAddress.toLowerCase().equals(email.toLowerCase())) {
            set.add(info);
         }
      }

      return set.toArray(new PlayerInfo[set.size()]);
   }

   public static PlayerInfo getPlayerSleepingInBed(long bedid) {
      for(PlayerInfo info : playerInfos.values()) {
         if (info.bed == bedid) {
            if (info.lastLogin <= 0L && info.lastLogout > System.currentTimeMillis() - 86400000L && info.currentServer == Servers.localServer.id) {
               return info;
            }

            return null;
         }
      }

      return null;
   }

   public static final int getNumberOfPayingPlayers() {
      long now = System.currentTimeMillis();
      int nums = 0;

      try {
         for(PlayerInfo info : playerInfos.values()) {
            if (info.getPower() == 0 && info.paymentExpireDate > now && (info.getCurrentServer() == Servers.localServer.id || Servers.localServer.LOGINSERVER)
               )
             {
               ++nums;
            }
         }
      } catch (Exception var5) {
         logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      return nums;
   }

   public static final int getNumberOfPayingPlayersEver() {
      int nums = 0;

      try {
         for(PlayerInfo info : playerInfos.values()) {
            if (info.getPower() == 0 && info.paymentExpireDate > 0L && (info.getCurrentServer() == Servers.localServer.id || Servers.localServer.LOGINSERVER)) {
               ++nums;
            }
         }
      } catch (Exception var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
      }

      return nums;
   }

   public static String rename(String oldName, String newName, String newPass, int power) throws IOException {
      logger.log(Level.INFO, "Trying to rename " + oldName + " to " + newName);
      if (!playerInfos.containsKey(oldName)) {
         return "";
      } else {
         PlayerInfo pinf = playerInfos.get(oldName);
         logger.log(Level.INFO, "Trying to rename " + oldName + " to " + newName + " power=" + power + ", pinf power=" + pinf.power);
         if (pinf.power < power) {
            pinf.setName(newName);
            pinf.updatePassword(newPass);
            playerInfos.remove(oldName);
            playerInfos.put(newName, pinf);

            try {
               Player p = Players.getInstance().getPlayer(oldName);
               p.refreshVisible();
               p.getCommunicator().sendSelfToLocal();
               p.getCommunicator().sendSafeServerMessage("Your password now is '" + newPass + "'. Please take a note of this.");
            } catch (NoSuchPlayerException var10) {
            }

            try {
               Village[] villages = Villages.getVillages();

               for(Village lVillage : villages) {
                  if (lVillage.mayorName.equals(oldName)) {
                     lVillage.setMayor(newName);
                  }
               }
            } catch (IOException var11) {
               logger.log(Level.WARNING, oldName + " failed to change the mayorname to " + newName, (Throwable)var11);
               return Servers.localServer.name + " failed to change the mayor name from " + oldName + " to " + newName;
            }

            return Servers.localServer.name + " - ok\n";
         } else {
            return Servers.localServer.name + " you do not have the power to do that.";
         }
      }
   }

   public static String changePassword(String changerName, String name, String newPass, int power) throws IOException {
      if (playerInfos.containsKey(name)) {
         PlayerInfo pinf = playerInfos.get(name);
         if (pinf.power >= power && !changerName.equals(name)) {
            return Servers.localServer.name + " you do not have the power to do that.";
         } else {
            pinf.updatePassword(newPass);
            if (!changerName.equals(name)) {
               try {
                  Player p = Players.getInstance().getPlayer(name);
                  p.getCommunicator().sendSafeServerMessage("Your password has been changed by " + changerName + " to " + newPass);
               } catch (NoSuchPlayerException var6) {
               }
            }

            logger.log(Level.INFO, changerName + " changed the password of " + name + ".");
            return Servers.localServer.name + " - ok\n";
         }
      } else {
         return "";
      }
   }

   public static boolean doesEmailExist(String email) {
      PlayerInfo[] accs = getPlayerInfosForEmail(email);
      return accs.length > 0;
   }

   public static boolean verifyPasswordForEmail(String email, String password, int power) {
      PlayerInfo[] accs = getPlayerInfosForEmail(email);
      boolean ok = true;
      if (accs.length > 0) {
         if (accs.length > 4) {
            return false;
         }

         ok = false;
         if (power > 0) {
            ok = true;
         }

         for(PlayerInfo lAcc : accs) {
            if (power == 0 || lAcc.power > power) {
               ok = false;
               if (password != null) {
                  try {
                     if (lAcc.password.equals(LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(lAcc.name))))) {
                        return true;
                     }
                  } catch (Exception var10) {
                  }
               }
            }
         }
      }

      return ok;
   }

   public static String changeEmail(String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer) throws IOException {
      if (playerInfos.containsKey(name)) {
         PlayerInfo pinf = playerInfos.get(name);
         if (pinf.power >= power && !changerName.equals(name)) {
            return Servers.localServer.name + " you do not have the power to do that.";
         } else {
            boolean ok = false;
            String retrievalInfo = "";
            if (pwQuestion != null
               && pwAnswer != null
               && changerName.equals(name)
               && (pwQuestion.length() > 3 && !pwQuestion.equals(pinf.pwQuestion) || pwAnswer.length() > 2 && !pwAnswer.equals(pinf.pwAnswer))) {
               pinf.setPassRetrieval(pwQuestion, pwAnswer);
               retrievalInfo = " Retrieval info updated.";
            }

            if (doesEmailExist(newEmail)) {
               if (verifyPasswordForEmail(newEmail, password, power)) {
                  ok = true;
               }

               logger.log(
                  Level.INFO, "Email exists for " + pinf.name + " " + pinf.password + " " + pinf.emailAddress + " new email:" + newEmail + " verified=" + ok
               );
            } else if (pinf.power >= power) {
               try {
                  if (pinf.password.equals(LoginHandler.hashPassword(password, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(pinf.name))))) {
                     ok = true;
                  }
               } catch (Exception var12) {
                  logger.log(Level.INFO, "Skipped " + pinf.name + " " + pinf.password + " " + pinf.emailAddress);
               }
            } else {
               ok = true;
            }

            if (!ok) {
               return "NO" + retrievalInfo;
            } else {
               pinf.setEmailAddress(newEmail);
               logger.log(Level.INFO, changerName + " changed the email of " + name + " to " + newEmail + "." + retrievalInfo);

               try {
                  Player p = Players.getInstance().getPlayer(name);
                  p.getCommunicator().sendSafeServerMessage("Your email has been changed by " + changerName + " to " + newEmail + "." + retrievalInfo);
               } catch (NoSuchPlayerException var11) {
               }

               return Servers.localServer.name + " - ok " + retrievalInfo + "\n";
            }
         }
      } else {
         return "";
      }
   }

   public static final void switchFatigue() {
      for(PlayerInfo info : playerInfos.values()) {
         info.saveSwitchFatigue();
      }
   }

   public static final void resetScenarioKarma() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("UPDATE PLAYERS SET SCENARIOKARMA=0");
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to reset scenario karma");
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      for(PlayerInfo info : playerInfos.values()) {
         info.scenarioKarma = 0;
      }

      for(Player p : Players.getInstance().getPlayers()) {
         p.sendScenarioKarma();
      }
   }

   public static void resetFaithGain() {
      for(PlayerInfo p : playerInfos.values()) {
         p.numFaith = 0;
         p.lastFaith = 0L;
      }
   }

   public static final int getNumberOfActivePlayersWithDeity(int deityNumber) {
      int nums = 0;
      long breakOff = System.currentTimeMillis() - 604800000L;

      for(PlayerInfo p : playerInfos.values()) {
         if (p.getDeity() != null && p.getDeity().number == deityNumber && p.getLastLogin() > breakOff) {
            ++nums;
         }
      }

      return nums;
   }

   public static final PlayerInfo[] getActivePriestsForDeity(int deityNumber) {
      Set<PlayerInfo> infos = new HashSet<>();
      long breakOff = System.currentTimeMillis() - 604800000L;

      for(PlayerInfo info : playerInfos.values()) {
         if (info == null) {
            logger.log(Level.WARNING, "getPlayerInfosWithEmail() NULL in playerInfos.values()??");
         } else {
            try {
               info.load();
               if (info.getDeity() != null && info.getDeity().number == deityNumber && info.isPriest && info.getLastLogin() > breakOff) {
                  infos.add(info);
               }
            } catch (IOException var7) {
               logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            }
         }
      }

      return infos.toArray(new PlayerInfo[infos.size()]);
   }

   public static final PlayerInfo[] getActiveFollowersForDeity(int deityNumber) {
      Set<PlayerInfo> infos = new HashSet<>();
      long breakOff = System.currentTimeMillis() - 604800000L;

      for(PlayerInfo info : playerInfos.values()) {
         if (info == null) {
            logger.log(Level.WARNING, "getPlayerInfosWithEmail() NULL in playerInfos.values()??");
         } else {
            try {
               info.load();
               if (info.getDeity() != null && info.getDeity().number == deityNumber && info.getLastLogin() > breakOff) {
                  infos.add(info);
               }
            } catch (IOException var7) {
               logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            }
         }
      }

      return infos.toArray(new PlayerInfo[infos.size()]);
   }

   public static final int getVotesForKingdom(byte kingdom) {
      int nums = 0;
      PlayerInfo[] pinfs = getPlayerInfos();

      for(PlayerInfo lPinf : pinfs) {
         if (lPinf.votedKing && Players.getInstance().getKingdomForPlayer(lPinf.wurmId) == kingdom) {
            ++nums;
         }
      }

      return nums;
   }

   public static final void resetVotesForKingdom(byte kingdom) {
      PlayerInfo[] pinfs = getPlayerInfos();

      for(PlayerInfo lPinf : pinfs) {
         if (lPinf.votedKing && Players.getInstance().getKingdomForPlayer(lPinf.wurmId) == kingdom) {
            lPinf.setVotedKing(false);
         }
      }
   }

   public static final int getNumberOfChamps(int deityNum) {
      int nums = 0;
      PlayerInfo[] pinfs = getPlayerInfos();

      for(PlayerInfo lPinf : pinfs) {
         if (lPinf.realdeath > 0
            && lPinf.realdeath < 4
            && System.currentTimeMillis() - lPinf.championTimeStamp < 14515200000L
            && lPinf.getDeity() != null
            && lPinf.getDeity().number == deityNum) {
            ++nums;
         }
      }

      return nums;
   }

   public static final void whosOnline() {
      grabPlayerStates();

      for(PlayerState entry : playerStatus.values()) {
         if (entry.getState() == PlayerOnlineStatus.ONLINE && entry.getServerId() == Servers.getLocalServerId()) {
            WcPlayerStatus wps = new WcPlayerStatus(entry);
            wps.sendToLoginServer();
         }
      }
   }

   public static final void grabPlayerStates() {
      if (playerStatus.size() < 100) {
         try {
            Map<Long, byte[]> statusBytes = getPlayerStates(EMPTY_LONG_PRIMITIVE_ARRAY);

            for(byte[] entry : statusBytes.values()) {
               PlayerState pState = new PlayerState(entry);
               playerStatus.put(pState.getPlayerId(), pState);
            }

            logger.log(Level.INFO, "Got " + playerStatus.size() + " player status");
         } catch (RemoteException var4) {
            logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         } catch (WurmServerException var5) {
            logger.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
         }
      }
   }

   public static final void updatePlayerState(Player player, long whenStateChanged, PlayerOnlineStatus aStatus) {
      PlayerState pState = new PlayerState(player.getWurmId(), player.getName(), whenStateChanged, aStatus);
      if (!playerJustTransfered(pState)) {
         updatePlayerState(pState);
         if (aStatus == PlayerOnlineStatus.ONLINE) {
            for(Friend f : player.getFriends()) {
               PlayerState fState = playerStatus.get(f.getFriendId());
               if (fState != null) {
                  player.getCommunicator().sendFriend(fState, f.getNote());
               }
            }
         }

         WcPlayerStatus wps = new WcPlayerStatus(pState);
         if (Servers.isThisLoginServer()) {
            wps.sendFromLoginServer();
         } else {
            wps.sendToLoginServer();
         }
      }
   }

   public static final void updatePlayerState(PlayerState pState) {
      if (pState.getState() != PlayerOnlineStatus.UNKNOWN) {
         statesToUpdate.add(pState);
      }
   }

   public static final void handlePlayerStateList() {
      for(PlayerState pState = statesToUpdate.pollFirst(); pState != null; pState = statesToUpdate.pollFirst()) {
         boolean tellAll = true;
         PlayerState oldState = playerStatus.get(pState.getPlayerId());
         if (pState.getState() == PlayerOnlineStatus.DELETE_ME) {
            playerStatus.remove(pState.getPlayerId());
         } else if (oldState != null && oldState.getState() == PlayerOnlineStatus.OFFLINE && pState.getState() == PlayerOnlineStatus.LOST_LINK) {
            tellAll = false;
         } else {
            playerStatus.put(pState.getPlayerId(), pState);
         }

         if (tellAll) {
            Players.tellFriends(pState);
            if (oldState == null) {
               Tickets.playerStateChange(pState);
            } else if (pState.getState() != oldState.getState()
               && (pState.getState() == PlayerOnlineStatus.ONLINE || oldState.getState() == PlayerOnlineStatus.ONLINE)) {
               Tickets.playerStateChange(pState);
            }
         }
      }

      for(Entry<Long, PlayerState> entry : friendsToUpdate.entrySet()) {
         try {
            long playerWurmId = entry.getKey();
            Player player = Players.getInstance().getPlayer(playerWurmId);
            player.getCommunicator().sendFriend(entry.getValue());
         } catch (NoSuchPlayerException var6) {
         }
      }

      friendsToUpdate.clear();
   }

   public static final boolean isPlayerOnline(long playerWurmId) {
      PlayerState pState = playerStatus.get(playerWurmId);
      return pState != null && pState.getState() == PlayerOnlineStatus.ONLINE;
   }

   public static final String getPlayerName(long playerWurmId) {
      PlayerState pState = playerStatus.get(playerWurmId);
      return pState != null ? pState.getPlayerName() : "Unknown";
   }

   public static final long getWurmId(String name) {
      for(PlayerState pState : playerStatus.values()) {
         if (pState.getPlayerName().equalsIgnoreCase(name)) {
            return pState.getPlayerId();
         }
      }

      return -10L;
   }

   public static final PlayerState getPlayerState(String name) {
      for(PlayerState pState : playerStatus.values()) {
         if (pState.getPlayerName().equalsIgnoreCase(name)) {
            return pState;
         }
      }

      return null;
   }

   public static final void setPlayerStatesToOffline(int serverId) {
      for(PlayerState pState : playerStatus.values()) {
         if (pState.getServerId() == serverId && pState.getState() != PlayerOnlineStatus.OFFLINE) {
            PlayerState newState = new PlayerState(
               pState.getServerId(), pState.getPlayerId(), pState.getPlayerName(), System.currentTimeMillis(), PlayerOnlineStatus.OFFLINE
            );
            updatePlayerState(newState);
         }
      }
   }

   public static long[] getPlayersOnCurrentServer() {
      Set<Long> pIds = new HashSet<>();

      for(PlayerState pState : playerStatus.values()) {
         if (pState.getServerId() == Servers.getLocalServerId()) {
            pIds.add(pState.getPlayerId());
         }
      }

      long[] ans = new long[pIds.size()];
      int x = 0;

      for(Long pId : pIds) {
         ans[x++] = pId;
      }

      return ans;
   }

   public static long breakFriendship(String playerName, long playerWurmId, String friendName) {
      for(PlayerState fState : playerStatus.values()) {
         if (fState.getPlayerName().equalsIgnoreCase(friendName)) {
            long friendWurmId = fState.getPlayerId();
            breakFriendship(playerName, playerWurmId, friendName, friendWurmId);
            return friendWurmId;
         }
      }

      return -10L;
   }

   public static void breakFriendship(String playerName, long playerWurmId, String friendName, long friendWurmId) {
      breakFriendship(playerWurmId, friendWurmId, friendName);
      breakFriendship(friendWurmId, playerWurmId, playerName);
   }

   private static void breakFriendship(long playerWurmId, long friendWurmId, String friendName) {
      PlayerInfo pInfo = getPlayerInfoWithWurmId(playerWurmId);
      if (pInfo != null) {
         pInfo.removeFriend(friendWurmId);
         PlayerState pState = new PlayerState(friendWurmId, friendName, -1L, PlayerOnlineStatus.DELETE_ME);
         friendsToUpdate.put(playerWurmId, pState);
      }
   }

   public static final WurmRecord getChampionRecord(String name) {
      for(WurmRecord record : championRecords) {
         if (record.getHolder().toLowerCase().equals(name.toLowerCase()) && record.isCurrent()) {
            return record;
         }
      }

      return null;
   }

   public static final void addChampRecord(WurmRecord record) {
      championRecords.add(record);
   }

   public static final WurmRecord[] getChampionRecords() {
      return championRecords.toArray(new WurmRecord[championRecords.size()]);
   }

   public static void expelMember(long playerId, byte fromKingdomId, byte toKingdomId, int originServer) {
      boolean isOnline = true;
      ServerEntry server = Servers.getServerWithId(originServer);
      if (server == null) {
         logger.warning("ExpelMember request from invalid server ID " + originServer + " for playerID " + playerId);
      } else {
         Player p = Players.getInstance().getPlayerOrNull(playerId);
         if (p == null) {
            PlayerInfo pInfo = playerInfosWurmId.get(playerId);
            if (pInfo == null) {
               return;
            }

            isOnline = false;

            try {
               pInfo.load();
               p = new Player(pInfo);
            } catch (Exception var11) {
               logger.log(Level.WARNING, "Unable to complete expel command for: " + playerId, (Throwable)var11);
               return;
            }
         }

         logger.info(
            "Expelling "
               + p.getName()
               + " from "
               + Kingdoms.getNameFor(fromKingdomId)
               + " on "
               + server.getName()
               + ", new kingdom: "
               + Kingdoms.getNameFor(toKingdomId)
         );
         if (server.EPIC && Servers.localServer.EPIC) {
            try {
               if (!p.setKingdomId(toKingdomId, false, false, isOnline)) {
                  logger.log(Level.WARNING, "Unable to complete expel command for: " + p.getName());
                  return;
               }
            } catch (IOException var10) {
               logger.log(Level.WARNING, "Unable to complete expel command for: " + p.getName(), (Throwable)var10);
               return;
            }
         } else if (server.EPIC && !Server.getInstance().isPS()) {
            p.getSaveFile().setEpicLocation(toKingdomId, p.getSaveFile().epicServerId);
         } else if (server.PVPSERVER || server.isChaosServer()) {
            p.getSaveFile().setChaosKingdom(toKingdomId);
         }

         if (isOnline) {
            p.getCommunicator().sendAlertServerMessage("You have been expelled from " + Kingdoms.getNameFor(fromKingdomId) + " on " + server.getName() + "!");
         }
      }
   }

   public static final class FatigueSwitcher implements Runnable {
      @Override
      public void run() {
         if (PlayerInfoFactory.logger.isLoggable(Level.FINER)) {
            PlayerInfoFactory.logger.finer("Running newSingleThreadScheduledExecutor for calling PlayerInfoFactory.switchFatigue()");
         }

         try {
            long now = System.nanoTime();
            PlayerInfoFactory.switchFatigue();
            float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
            if (lElapsedTime > (float)Constants.lagThreshold) {
               PlayerInfoFactory.logger.info("Finished calling PlayerInfoFactory.switchFatigue(), which took " + lElapsedTime + " millis.");
            }
         } catch (RuntimeException var4) {
            PlayerInfoFactory.logger
               .log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling PlayerInfoFactory.switchFatigue()", (Throwable)var4);
            throw var4;
         }
      }
   }
}
