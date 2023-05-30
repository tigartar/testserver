package com.wurmonline.server.players;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.steam.SteamId;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class PlayerInfo implements MiscConstants, TimeConstants, CombatConstants, Comparable<PlayerInfo> {
   public long lastCreatedHistoryEvent = 0L;
   public int timeToCheckPrem = 61 + Server.rand.nextInt(28000);
   String name = null;
   String password = "EmptyPassword";
   public long wurmId = -10L;
   public long lastToggledFSleep = 0L;
   public long lastLogin = 0L;
   private long lastDeath = 0L;
   public long playingTime = 0L;
   private static Logger logger = Logger.getLogger(PlayerInfo.class.getName());
   private ConcurrentHashMap<Long, Friend> friends;
   private boolean hasLoadedFriends = false;
   private Set<Long> enemies;
   boolean reimbursed = !WurmCalendar.isChristmas() && !WurmCalendar.isEaster();
   public long plantedSign = System.currentTimeMillis() - 604800000L;
   boolean banned = false;
   String ipaddress = "";
   SteamId steamId;
   private Set<Long> ignored = null;
   boolean muted = false;
   byte power = 0;
   long paymentExpireDate;
   public long version = 0L;
   int rank = 1000;
   int maxRank = 1000;
   public boolean mayHearDevTalk = false;
   public long lastWarned = 0L;
   public int warnings = 0;
   public long lastChangedDeity = System.currentTimeMillis() - 604800000L;
   public byte realdeath = 0;
   float alignment = 1.0F;
   Deity deity = null;
   float faith = 0.0F;
   float favor = 0.0F;
   Deity god = null;
   public static final String LOADED_CLASSES_DISCONNECT = "CLASS_CHECK_DISCONNECT";
   public long lastCheated = 0L;
   public int fatigueSecsLeft = 28800;
   public int fatigueSecsToday = 0;
   public int fatigueSecsYesterday = 0;
   public long lastFatigue = System.currentTimeMillis();
   public static final int MAX_FATIGUE_SECONDS = 43200;
   private static final int FATIGUE_INCREASE_TIME = 3600;
   private static final long FATIGUE_INCREASE_DELAY_PREMIUM = 10800000L;
   public static final long MINTIME_BETWEEN_CHAMPION = 14515200000L;
   public boolean dead = false;
   String sessionKey = "";
   public long sessionExpiration = 0L;
   public byte numFaith;
   public long lastFaith;
   protected byte sex;
   public long money = 0L;
   public boolean climbing = false;
   protected boolean spamMode = true;
   private long lastGasp = 0L;
   byte changedKingdom = 0;
   public long MIN_KINGDOM_CHANGE_TIME = 1209600000L;
   public long lastChangedKindom = System.currentTimeMillis() - 1209600000L;
   public long championTimeStamp = 0L;
   public short championPoints = 0;
   public long creationDate = System.currentTimeMillis();
   public String banreason = "";
   public long banexpiry = 0L;
   public long face = 0L;
   protected byte blood = 0;
   public long lastChangedCluster = 0L;
   public short muteTimes = 0;
   public long nextAvailableMute = 0L;
   public long startedReceivingMutes = 0L;
   public short mutesReceived = 0;
   public int reputation = 100;
   public long lastPolledReputation = System.currentTimeMillis();
   Set<Titles.Title> titles = new HashSet<>();
   public Titles.Title title = null;
   public Titles.Title secondTitle = null;
   public String kingdomtitle = "";
   public long pet = -10L;
   public float alcohol = 0.0F;
   public float nicotine = 0.0F;
   public long alcoholAddiction = 0L;
   public long nicotineAddiction = 0L;
   public boolean mayMute = false;
   public String mutereason = "";
   public long muteexpiry = 0L;
   public boolean logging = false;
   public int lastServer;
   public int currentServer;
   public boolean loaded = false;
   public long referrer = 0L;
   public String emailAddress = "";
   public long lastLogout = 0L;
   public String pwQuestion = "";
   public String pwAnswer = "";
   public long lastRequestedPassword = 0L;
   long lastChangedVillage = 0L;
   public boolean isPriest = false;
   public long bed = -10L;
   public int sleep = 0;
   public boolean frozenSleep = true;
   public boolean overRideShop = false;
   public boolean isTheftWarned = false;
   public boolean noReimbursementLeft = false;
   public boolean deathProtected = false;
   public byte fightmode = 2;
   public long nextAffinity = 0L;
   public int tutorialLevel = 0;
   public boolean autoFighting = false;
   public long appointments = 0L;
   public long lastvehicle = -10L;
   boolean playerAssistant = false;
   boolean mayAppointPlayerAssistant = false;
   boolean seesPlayerAssistantWindow = false;
   protected boolean hasMovedInventory = false;
   public byte priestType = 0;
   public long lastChangedPriestType = 0L;
   byte lastTaggedKindom = 0;
   private final Map<Long, Integer> macroAttackers = new HashMap<>();
   private final Map<Long, Integer> macroArchers = new HashMap<>();
   private static final int MAX_MACRO_ATTACKS = 100;
   long lastMovedBetweenKingdom = System.currentTimeMillis();
   public long lastModifiedRank = System.currentTimeMillis();
   public long lastChangedJoat = System.currentTimeMillis();
   public boolean hasFreeTransfer = false;
   public int lastTriggerEffect = 0;
   public boolean hasSkillGain = true;
   public float champChanneling = 0.0F;
   public boolean votedKing = false;
   public int epicServerId = -1;
   public byte epicKingdom = 0;
   public long lastUsedEpicPortal = 0L;
   byte chaosKingdom = 0;
   short hotaWins = 0;
   protected int karma = 0;
   protected int maxKarma = 0;
   protected int totalKarma = 0;
   public long abilities;
   public int abilityTitle = -1;
   public Awards awards = null;
   protected BitSet abilityBits = new BitSet(64);
   public long flags;
   public long flags2;
   protected BitSet flagBits = new BitSet(64);
   protected BitSet flag2Bits = new BitSet(64);
   public int scenarioKarma = 0;
   public byte undeadType = 0;
   public int undeadKills = 0;
   public int undeadPlayerKills = 0;
   public int undeadPlayerSeconds = 0;
   private long moneyToSend = 0L;
   private final ConcurrentHashMap<String, Long> targetPMIds = new ConcurrentHashMap<>();
   private long sessionFlags = 0L;
   protected BitSet sessionFlagBits = new BitSet(64);
   protected String modelName = "Human";
   private long moneyEarnedBySellingLastHour = 0L;
   protected long moneyEarnedBySellingEver = 0L;
   private long lastResetEarningsCounter = 0L;
   public final ConcurrentHashMap<String, Long> historyIPStart = new ConcurrentHashMap<>();
   public final ConcurrentHashMap<String, Long> historyIPLast = new ConcurrentHashMap<>();
   public final ConcurrentHashMap<String, Long> historyEmail = new ConcurrentHashMap<>();
   public final ConcurrentHashMap<SteamId, SteamIdHistory> historySteamId = new ConcurrentHashMap<>();
   private Map<Short, SpellResistance> spellResistances;
   private float limitingArmourFactor = 0.3F;
   private long lastChangedPath = 0L;
   private List<Route> highwayPath = null;
   private List<Float> highwayDistances = null;
   private String highwayPathDestination = "";

   PlayerInfo(String aname) {
      this.name = aname;
   }

   public int compareTo(PlayerInfo otherPlayerInfo) {
      return this.getName().compareTo(otherPlayerInfo.getName());
   }

   public final int getPower() {
      return this.power;
   }

   public final long getPaymentExpire() {
      return System.currentTimeMillis() + 29030400000L;
   }

   public abstract void setPower(byte var1) throws IOException;

   public abstract void setPaymentExpire(long var1) throws IOException;

   public abstract void setPaymentExpire(long var1, boolean var3) throws IOException;

   public final boolean isPaying() {
      return true;
   }

   public final boolean isQAAccount() {
      return this.isFlagSet(26);
   }

   public final boolean isBanned() {
      return this.banned;
   }

   public final int getChangedKingdom() {
      return this.changedKingdom;
   }

   public abstract void setBanned(boolean var1, String var2, long var3) throws IOException;

   public final void setLogin() {
      this.calculateSleep();
      this.lastLogin = System.currentTimeMillis();
   }

   public final long getLastLogin() {
      return this.lastLogin;
   }

   public final long getLastLogout() {
      return this.lastLogout;
   }

   public final boolean mayBecomeChampion() {
      return System.currentTimeMillis() - this.championTimeStamp > 14515200000L;
   }

   public final short getChampionPoints() {
      return this.championPoints;
   }

   public final boolean isReimbursed() {
      return this.reimbursed;
   }

   public final boolean hasLoadedFriends() {
      return this.hasLoadedFriends;
   }

   protected final void setLoadedFriends(boolean hasLoaded) {
      this.hasLoadedFriends = hasLoaded;
   }

   public final long getPlayerId() {
      return this.wurmId;
   }

   public final String getPassword() {
      return this.password;
   }

   final boolean hasPlantedSign() {
      return System.currentTimeMillis() - this.plantedSign < 86400000L;
   }

   public final Titles.Title[] getTitles() {
      return this.titles.toArray(new Titles.Title[this.titles.size()]);
   }

   final boolean mayChangeDeity(int targetDeity) {
      if (targetDeity == 4) {
         return true;
      } else {
         return System.currentTimeMillis() - this.lastChangedDeity > 604800000L;
      }
   }

   public final void setPassword(String pw) {
      this.password = pw;

      try {
         this.save();
      } catch (IOException var3) {
         logger.log(Level.WARNING, "Failed to change password for " + this.name, (Throwable)var3);
      }
   }

   public void initialize(String aName, long aWurmId, String aPassword, String aPwQuestion, String aPwAnswer, long aFace, boolean aGuest) throws IOException {
      this.name = aName;
      this.wurmId = aWurmId;
      this.password = aPassword;
      this.face = aFace;
      this.pwQuestion = aPwQuestion;
      this.pwAnswer = aPwAnswer;
      this.lastLogout = System.currentTimeMillis();
      this.flagBits.set(3, true);
      this.flags = this.getFlagLong();
      if (!aGuest) {
         this.save();
      }

      PlayerInfoFactory.addPlayerInfo(this);
   }

   public final String getName() {
      return this.name;
   }

   public final Friend[] getFriends() {
      if (!this.hasLoadedFriends()) {
         this.loadFriends(this.wurmId);
      }

      return this.friends != null ? this.friends.values().toArray(new Friend[this.friends.size()]) : new Friend[0];
   }

   @Nullable
   public final Friend getFriend(long friendId) {
      if (!this.hasLoadedFriends()) {
         this.loadFriends(friendId);
      }

      return this.friends.get(friendId);
   }

   final void addFriend(long friendId, byte catId, String note, boolean loading) {
      if (this.friends == null) {
         this.friends = new ConcurrentHashMap<>();
      }

      Long fid = new Long(friendId);
      if (!this.friends.containsKey(fid)) {
         this.friends.put(fid, new Friend(friendId, catId, note));
         if (!loading) {
            try {
               this.saveFriend(this.wurmId, friendId, catId, note);
            } catch (IOException var8) {
               if (this.name != null) {
                  logger.log(Level.WARNING, "Failed to save friends for " + this.name, (Throwable)var8);
               } else {
                  logger.log(Level.WARNING, "Failed to save friends for unknown player.", (Throwable)var8);
               }
            }
         }
      }
   }

   final void updateFriendData(long friendId, byte catId, String note) {
      if (this.friends == null) {
         this.friends = new ConcurrentHashMap<>();
      }

      Long fid = new Long(friendId);
      if (this.friends.containsKey(fid)) {
         Friend friend = this.friends.put(fid, new Friend(friendId, catId, note));
         if (friend.getCatId() != catId || !friend.getNote().equals(note)) {
            try {
               this.updateFriend(this.wurmId, friendId, catId, note);
            } catch (IOException var8) {
               if (this.name != null) {
                  logger.log(Level.WARNING, "Failed to update friend (" + friend.getName() + ") for " + this.name, (Throwable)var8);
               } else {
                  logger.log(Level.WARNING, "Failed to update friend (" + friend.getName() + ") for unknown player.", (Throwable)var8);
               }
            }
         }
      }
   }

   public final boolean isFriendsWith(long friendId) {
      if (this.friends == null) {
         this.loadFriends(this.wurmId);
      }

      return this.friends != null ? this.friends.containsKey(friendId) : false;
   }

   public final boolean removeFriend(long friendId) {
      if (this.friends == null) {
         this.loadFriends(this.wurmId);
      }

      if (this.friends != null) {
         Long fid = new Long(friendId);
         if (this.friends.containsKey(fid)) {
            this.friends.remove(fid);

            try {
               this.deleteFriend(this.wurmId, friendId);
            } catch (IOException var5) {
               if (this.name != null) {
                  logger.log(Level.WARNING, "Failed to save friends for " + this.name, (Throwable)var5);
               } else {
                  logger.log(Level.WARNING, "Failed to save friends for unknown player.", (Throwable)var5);
               }
            }

            return true;
         }
      }

      return false;
   }

   final void addEnemy(long enemyId, boolean loading) {
      if (this.enemies == null) {
         this.enemies = new HashSet<>();
      }

      Long fid = new Long(enemyId);
      if (!this.enemies.contains(fid)) {
         this.enemies.add(fid);
         if (!loading) {
            try {
               this.saveEnemy(this.wurmId, enemyId);
            } catch (IOException var6) {
               if (this.name != null) {
                  logger.log(Level.WARNING, "Failed to save friends for " + this.name, (Throwable)var6);
               } else {
                  logger.log(Level.WARNING, "Failed to save friends for unknown player.", (Throwable)var6);
               }
            }
         }
      }
   }

   public final boolean removeIgnored(long ignoredId) {
      if (this.ignored != null) {
         Long fid = new Long(ignoredId);
         if (this.ignored.contains(fid)) {
            this.ignored.remove(fid);

            try {
               this.deleteIgnored(this.wurmId, ignoredId);
               return true;
            } catch (IOException var5) {
               if (this.name != null) {
                  logger.log(Level.WARNING, "Failed to delete ignored for " + this.name, (Throwable)var5);
               } else {
                  logger.log(Level.WARNING, "Failed to delete ignored for unknown player.", (Throwable)var5);
               }
            }
         }
      }

      return false;
   }

   final boolean isIgnored(long playerId) {
      if (this.ignored != null) {
         for(Long id : this.ignored) {
            if (id == playerId) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public final long[] getIgnored() {
      long[] toReturn = EMPTY_LONG_PRIMITIVE_ARRAY;
      if (this.ignored != null && this.ignored.size() > 0) {
         toReturn = new long[this.ignored.size()];
         int x = 0;

         for(Long ig : this.ignored) {
            toReturn[x] = ig;
            ++x;
         }
      }

      return toReturn;
   }

   public final int getWarnings() {
      return this.warnings;
   }

   public final long getLastWarned() {
      return this.lastWarned;
   }

   public final String getWarningStats(long getLastWarned) {
      String warnString = this.name + " has never been warned before.";
      if (getLastWarned > 0L) {
         warnString = "Last warning received was " + Server.getTimeFor(System.currentTimeMillis() - getLastWarned) + " ago.";
      }

      return this.name + " has played " + Server.getTimeFor(this.playingTime) + " and received " + this.warnings + " warnings. " + warnString;
   }

   public abstract void resetWarnings() throws IOException;

   public final int getRank() {
      return this.rank;
   }

   public final int getMaxRank() {
      return this.maxRank;
   }

   public final float getAlignment() {
      return this.alignment;
   }

   public final float getFaith() {
      return this.faith;
   }

   public final Deity getDeity() {
      return this.deity;
   }

   public final Deity getGod() {
      return this.god;
   }

   public boolean spamMode() {
      return this.spamMode;
   }

   public final float getFavor() {
      return this.favor;
   }

   final void sendReligionStatus(int itemNum, float value) {
      try {
         Player p = Players.getInstance().getPlayer(this.wurmId);
         p.getCommunicator().sendUpdateSkill(itemNum, value, 0);
         p.checkFaithTitles();
      } catch (NoSuchPlayerException var4) {
      }
   }

   final void sendAttitudeChange() {
      try {
         Players.getInstance().getPlayer(this.wurmId).sendAttitudeChange();
      } catch (NoSuchPlayerException var2) {
      }
   }

   final boolean checkPrayerFaith() {
      if (this.deity == null) {
         return false;
      } else if (this.numFaith >= (this.isFlagSet(81) ? 6 : 5)) {
         return false;
      } else if (System.currentTimeMillis() - this.lastFaith <= 1200000L) {
         return false;
      } else {
         this.lastFaith = System.currentTimeMillis();
         if (this.getFaith() < 30.0F || this.isPaying()) {
            if (!Servers.localServer.isChallengeServer()) {
               this.modifyFaith(Math.min(1.0F, (100.0F - this.getFaith()) / (10.0F * Math.max(1.0F, this.getFaith()))));
            } else {
               this.modifyFaith(1.0F);
            }

            ++this.numFaith;
         }

         try {
            this.setNumFaith(this.numFaith, this.lastFaith);
         } catch (IOException var2) {
            logger.log(Level.WARNING, this.name + " " + var2.getMessage(), (Throwable)var2);
         }

         return true;
      }
   }

   final void modifyFaith(float mod) {
      if (this.deity != null && mod != 0.0F && (this.getFaith() < 30.0F || mod < 0.0F || this.isPriest && this.isPaying())) {
         try {
            this.setFaith(Math.max(1.0F, this.getFaith() + Math.min(1.0F, mod)));
         } catch (IOException var3) {
            logger.log(Level.WARNING, this.name, (Throwable)var3);
         }
      }
   }

   final void decreaseFatigue() {
      --this.fatigueSecsLeft;
      ++this.fatigueSecsToday;
      if (this.fatigueSecsLeft % 100 == 0) {
         this.setFatigueSecs(this.fatigueSecsLeft, this.lastFatigue);
      }
   }

   final boolean checkFatigue() {
      long times = 0L;
      times = (System.currentTimeMillis() - this.lastFatigue) / 10800000L;
      if (times <= 0L) {
         return false;
      } else {
         for(int x = 0; (long)x < Math.min(times, 8L); ++x) {
            this.fatigueSecsLeft = Math.min(this.fatigueSecsLeft + 3600, 43200);
         }

         this.lastFatigue += times * 10800000L;
         this.setFatigueSecs(this.fatigueSecsLeft, this.lastFatigue);
         return true;
      }
   }

   final int hardSetFatigueSecs(int fatsecsleft) {
      this.fatigueSecsLeft = Math.max(0, Math.min(this.fatigueSecsLeft + fatsecsleft, 43200));
      return this.fatigueSecsLeft;
   }

   public final boolean isMute() {
      return this.muted;
   }

   public final boolean addIgnored(long id, boolean load) throws IOException {
      if (this.ignored == null) {
         this.ignored = new HashSet<>();
      }

      if (!this.ignored.contains(new Long(id))) {
         this.ignored.add(new Long(id));
         if (!load) {
            this.saveIgnored(this.wurmId, id);
         }

         return true;
      } else {
         return false;
      }
   }

   public final int getReputation() {
      return this.reputation;
   }

   final void pollReputation(long now) {
      if (now > this.lastPolledReputation + 3600000L) {
         long nums = (now - this.lastPolledReputation) / 3600000L;
         this.setReputation(this.reputation + (int)nums);
         this.lastPolledReputation = System.currentTimeMillis();
      }
   }

   public final void logout() {
      if (this.lastLogin > 0L) {
         this.playingTime = this.playingTime + System.currentTimeMillis() - this.lastLogin;
      }

      if (this.lastLogin > 0L) {
         this.lastLogout = System.currentTimeMillis();
      }

      this.lastLogin = 0L;
      this.setSessionFlags(0L);
   }

   public final int getSleepLeft() {
      if (this.sleep <= 0) {
         this.frozenSleep = true;
      }

      return this.sleep;
   }

   public boolean isSleepFrozen() {
      return this.frozenSleep;
   }

   final boolean hasSleepBonus() {
      if (this.frozenSleep) {
         return false;
      } else {
         return this.sleep > 0;
      }
   }

   private void calculateSleep() {
      if (this.bed > 0L) {
         long sleepTime = System.currentTimeMillis() - this.lastLogout;
         if (sleepTime > 10800000L) {
            Optional<Item> beds = Items.getItemOptional(this.bed);
            if (beds.isPresent()) {
               Item bed = beds.get();
               if (bed.isBed()) {
                  bed.setData(0L);
               }
            }
         }

         if (sleepTime > 3600000L) {
            sleepTime /= 1000L;
            long secs = sleepTime / 24L;
            this.setSleep((int)((long)this.sleep + secs));
         }

         this.setBed(0L);
      }
   }

   public final void addToSleep(int secs) {
      this.setSleep(this.sleep + secs);

      try {
         Player p = Players.getInstance().getPlayer(this.wurmId);
         p.getCommunicator().sendSleepInfo();
      } catch (NoSuchPlayerException var3) {
      }
   }

   final boolean eligibleForAffinity() {
      if (System.currentTimeMillis() <= this.nextAffinity && this.nextAffinity != 0L) {
         return false;
      } else {
         this.setNextAffinity(System.currentTimeMillis() + 2419200000L + (long)Server.rand.nextInt(50000));
         return true;
      }
   }

   abstract void setReputation(int var1);

   public abstract void setMuted(boolean var1, String var2, long var3);

   abstract void setFatigueSecs(int var1, long var2);

   abstract void setCheated(String var1);

   public abstract void updatePassword(String var1) throws IOException;

   public abstract void setRealDeath(byte var1) throws IOException;

   public abstract void setFavor(float var1) throws IOException;

   public abstract void setFaith(float var1) throws IOException;

   abstract void setDeity(Deity var1) throws IOException;

   abstract void setAlignment(float var1) throws IOException;

   abstract void setGod(Deity var1) throws IOException;

   public abstract void load() throws IOException;

   public abstract void warn() throws IOException;

   public abstract void save() throws IOException;

   public abstract void setLastTrigger(int var1);

   public int getLastTrigger() {
      return this.lastTriggerEffect;
   }

   abstract void setIpaddress(String var1) throws IOException;

   abstract void setSteamId(SteamId var1) throws IOException;

   public abstract void setRank(int var1) throws IOException;

   public abstract void setReimbursed(boolean var1) throws IOException;

   abstract void setPlantedSign() throws IOException;

   abstract void setChangedDeity() throws IOException;

   public abstract String getIpaddress();

   abstract void setDead(boolean var1);

   public abstract void setSessionKey(String var1, long var2) throws IOException;

   abstract void setName(String var1) throws IOException;

   public abstract void setVersion(long var1) throws IOException;

   abstract void saveFriend(long var1, long var3, byte var5, String var6) throws IOException;

   abstract void updateFriend(long var1, long var3, byte var5, String var6) throws IOException;

   abstract void deleteFriend(long var1, long var3) throws IOException;

   abstract void saveEnemy(long var1, long var3) throws IOException;

   abstract void deleteEnemy(long var1, long var3) throws IOException;

   abstract void saveIgnored(long var1, long var3) throws IOException;

   abstract void deleteIgnored(long var1, long var3) throws IOException;

   public abstract void setNumFaith(byte var1, long var2) throws IOException;

   abstract long getFlagLong();

   abstract long getFlag2Long();

   public abstract void setMoney(long var1) throws IOException;

   abstract void setSex(byte var1) throws IOException;

   abstract void setClimbing(boolean var1) throws IOException;

   abstract void setChangedKingdom(byte var1, boolean var2) throws IOException;

   public abstract void setFace(long var1) throws IOException;

   abstract boolean addTitle(Titles.Title var1);

   abstract boolean removeTitle(Titles.Title var1);

   abstract void setAlcohol(float var1);

   abstract void setPet(long var1);

   public abstract void setNicotineTime(long var1);

   public abstract boolean setAlcoholTime(long var1);

   abstract void setNicotine(float var1);

   public abstract void setMayMute(boolean var1);

   public abstract void setEmailAddress(String var1);

   abstract void setPriest(boolean var1);

   public abstract void setOverRideShop(boolean var1);

   public abstract void setReferedby(long var1);

   public abstract void setBed(long var1);

   abstract void setLastChangedVillage(long var1);

   abstract void setSleep(int var1);

   abstract void setTheftwarned(boolean var1);

   public abstract void setHasNoReimbursementLeft(boolean var1);

   abstract void setDeathProtected(boolean var1);

   public int getCurrentServer() {
      return this.currentServer;
   }

   final void addAppointment(int aid) {
      if (!this.hasAppointment(aid)) {
         this.appointments += 1L << aid;
         this.saveAppointments();
      }
   }

   final void removeAppointment(int aid) {
      if (this.hasAppointment(aid)) {
         this.appointments -= 1L << aid;
         this.saveAppointments();
      }
   }

   final void clearAppointments() {
      this.appointments = 0L;
      this.saveAppointments();
   }

   final boolean hasAppointment(int aid) {
      return (this.appointments >> aid & 1L) == 1L;
   }

   public final boolean isPlayerAssistant() {
      return this.playerAssistant;
   }

   public final boolean mayAppointPlayerAssistant() {
      return this.mayAppointPlayerAssistant;
   }

   public final boolean seesPlayerAssistantWindow() {
      return this.seesPlayerAssistantWindow;
   }

   public final boolean hasMovedInventory() {
      return this.hasMovedInventory;
   }

   public final boolean mayUseLastGasp() {
      return System.currentTimeMillis() - this.lastGasp > 21600000L;
   }

   public final void useLastGasp() {
      this.lastGasp = System.currentTimeMillis();
   }

   public final boolean isUsingLastGasp() {
      return System.currentTimeMillis() - this.lastGasp < 120000L;
   }

   public final byte getChaosKingdom() {
      return this.chaosKingdom;
   }

   public final short getHotaWins() {
      return this.hotaWins;
   }

   public final long getLastDeath() {
      return this.lastDeath;
   }

   public final void died() {
      this.lastDeath = System.currentTimeMillis();
   }

   protected final void checkHotaTitles() {
      if (this.hotaWins == 1) {
         this.addTitle(Titles.Title.Hota_One);
      }

      if (this.hotaWins == 3) {
         this.addTitle(Titles.Title.Hota_Two);
      }

      if (this.hotaWins == 7) {
         this.addTitle(Titles.Title.Hota_Three);
      }

      if (this.hotaWins == 15) {
         this.addTitle(Titles.Title.Hota_Four);
      }

      if (this.hotaWins == 30) {
         this.addTitle(Titles.Title.Hota_Five);
      }
   }

   public abstract void setCurrentServer(int var1);

   public abstract void setDevTalk(boolean var1);

   public abstract void transferDeity(@Nullable Deity var1) throws IOException;

   abstract void saveSwitchFatigue();

   abstract void saveFightMode(byte var1);

   abstract void setNextAffinity(long var1);

   public abstract void saveAppointments();

   abstract void setTutorialLevel(int var1);

   abstract void setAutofight(boolean var1);

   abstract void setLastVehicle(long var1);

   public abstract void setIsPlayerAssistant(boolean var1);

   public abstract void setMayAppointPlayerAssistant(boolean var1);

   public abstract boolean togglePlayerAssistantWindow(boolean var1);

   public abstract void setLastTaggedTerr(byte var1);

   public abstract void setNewPriestType(byte var1, long var2);

   public abstract void setChangedJoat();

   public abstract void setMovedInventory(boolean var1);

   public abstract void setFreeTransfer(boolean var1);

   public abstract boolean setHasSkillGain(boolean var1);

   public abstract void loadIgnored(long var1);

   public abstract void loadTitles(long var1);

   public abstract void loadFriends(long var1);

   public abstract void loadHistorySteamIds(long var1);

   public abstract void loadHistoryIPs(long var1);

   public abstract void loadHistoryEmails(long var1);

   public abstract boolean setChampionPoints(short var1);

   public abstract void setChangedKingdom();

   public abstract void setChampionTimeStamp();

   public abstract void setChampChanneling(float var1);

   public abstract void setMuteTimes(short var1);

   public abstract void setVotedKing(boolean var1);

   public abstract void setEpicLocation(byte var1, int var2);

   public abstract void setChaosKingdom(byte var1);

   public abstract void setHotaWins(short var1);

   public abstract void setSpamMode(boolean var1);

   public abstract void setKarma(int var1);

   public abstract void setScenarioKarma(int var1);

   public int getKarma() {
      return this.karma;
   }

   public int getMaxKarma() {
      return this.maxKarma;
   }

   public int getTotalKarma() {
      return this.totalKarma;
   }

   public int getScenarioKarma() {
      return this.scenarioKarma;
   }

   public final boolean isAbilityBitSet(int abilityBit) {
      return this.abilities != 0L ? this.abilityBits.get(abilityBit) : false;
   }

   public final boolean isFlagSet(int flagBit) {
      if (flagBit < 64) {
         if (this.flags != 0L) {
            return this.flagBits.get(flagBit);
         }
      } else if (this.flags2 != 0L) {
         return this.flag2Bits.get(flagBit - 64);
      }

      return false;
   }

   public byte getBlood() {
      return this.blood;
   }

   public abstract void setBlood(byte var1);

   public abstract void setFlag(int var1, boolean var2);

   public abstract void setFlagBits(long var1);

   public abstract void setFlag2Bits(long var1);

   public abstract void forceFlagsUpdate();

   public abstract void setAbility(int var1, boolean var2);

   public abstract void setCurrentAbilityTitle(int var1);

   public abstract void setUndeadData();

   public ConcurrentHashMap<String, Long> getAllTargetPMIds() {
      return this.targetPMIds;
   }

   public boolean hasPMTarget(String targetName) {
      return this.targetPMIds.containsKey(targetName);
   }

   public long getPMTargetId(String targetName) {
      return this.targetPMIds.containsKey(targetName) ? this.targetPMIds.get(targetName) : -10L;
   }

   public void addPMTarget(String targetName, long targetId) {
      if (!this.targetPMIds.containsKey(targetName)) {
         this.targetPMIds.put(targetName, targetId);
      }
   }

   public void removePMTarget(String targetName) {
      if (this.targetPMIds.containsKey(targetName)) {
         this.targetPMIds.remove(targetName);
      }
   }

   public long getSessionFlags() {
      return this.sessionFlags;
   }

   public void setSessionFlags(long aFlags) {
      this.sessionFlags = aFlags;
      this.sessionFlagBits.clear();

      for(int x = 0; x < 64; ++x) {
         if ((aFlags >>> x & 1L) == 1L) {
            this.sessionFlagBits.set(x);
         }
      }
   }

   public final boolean isSessionFlagSet(int flagBit) {
      return this.sessionFlags != 0L ? this.sessionFlagBits.get(flagBit) : false;
   }

   public final void setSessionFlag(int number, boolean value) {
      this.sessionFlagBits.set(number, value);
      this.sessionFlags = this.getSessionFlagLong();
   }

   public final String getModelName() {
      return this.modelName;
   }

   public abstract void setModelName(String var1);

   private final long getSessionFlagLong() {
      long ret = 0L;

      for(int x = 0; x < 64; ++x) {
         if (this.sessionFlagBits.get(x)) {
            ret += 1L << x;
         }
      }

      return ret;
   }

   @Override
   public final String toString() {
      return "PlayerInfo [wurmId: "
         + this.wurmId
         + ", name: "
         + this.name
         + ", currentServer: "
         + this.currentServer
         + ", lastLogin: "
         + this.lastLogin
         + ", lastLogout: "
         + this.lastLogout
         + ", banned: "
         + this.banned
         + ", ipaddress: "
         + this.ipaddress
         + ", power: "
         + this.power
         + ", creationDate: "
         + this.creationDate
         + ", paymentExpireDate: "
         + this.paymentExpireDate
         + ", playingTime: "
         + this.playingTime
         + ", money: "
         + this.money
         + ']';
   }

   public long getMoneyEarnedBySellingLastHour() {
      return this.moneyEarnedBySellingLastHour;
   }

   public final long getMoneyToSend() {
      return this.moneyToSend;
   }

   public final void resetMoneyToSend() {
      this.moneyToSend = 0L;
   }

   public void addMoneyEarnedBySellingLastHour(long aMoney) {
      this.moneyToSend += aMoney;
      if (this.getMoneyEarnedBySellingLastHour() == 0L) {
         this.setLastResetEarningsCounter(System.currentTimeMillis());
      }

      this.setMoneyEarnedBySellingLastHour(this.getMoneyEarnedBySellingLastHour() + aMoney);
      this.addMoneyEarnedBySellingEver(aMoney);
   }

   public void checkIfResetSellEarning() {
      if (System.currentTimeMillis() - this.getLastResetEarningsCounter() > (Servers.isThisATestServer() ? 20000L : 3600000L)) {
         this.setLastResetEarningsCounter(System.currentTimeMillis());
         this.setMoneyEarnedBySellingLastHour(0L);
      }
   }

   public long getLastResetEarningsCounter() {
      return this.lastResetEarningsCounter;
   }

   public long getMoneyEarnedBySellingEver() {
      return this.moneyEarnedBySellingEver;
   }

   public abstract void addMoneyEarnedBySellingEver(long var1);

   public abstract void setPointsForChamp();

   public abstract void switchChamp();

   public void setMoneyEarnedBySellingLastHour(long aMoneyEarnedBySellingLastHour) {
      this.moneyEarnedBySellingLastHour = aMoneyEarnedBySellingLastHour;
   }

   public void setLastResetEarningsCounter(long aLastResetEarningsCounter) {
      this.lastResetEarningsCounter = aLastResetEarningsCounter;
   }

   public abstract void setPassRetrieval(String var1, String var2) throws IOException;

   public final float addSpellResistance(short spellId) {
      if (this.spellResistances == null) {
         this.spellResistances = new ConcurrentHashMap<>();
      }

      SpellResistance existing = this.spellResistances.get(spellId);
      if (existing == null) {
         existing = new SpellResistance(spellId);
         this.spellResistances.put(spellId, existing);
      }

      float toReturn = existing.getResistance();
      existing.setResistance();
      return 1.0F - toReturn;
   }

   public final SpellResistance getSpellResistance(short spellId) {
      if (this.spellResistances == null) {
         this.spellResistances = new ConcurrentHashMap<>();
      }

      return this.spellResistances.get(spellId);
   }

   public final void pollResistances(Communicator comm) {
      if (this.spellResistances != null) {
         SpellResistance[] resisArr = this.spellResistances.values().toArray(new SpellResistance[this.spellResistances.size()]);

         for(SpellResistance resist : resisArr) {
            if (resist.tickSecond(comm)) {
               this.spellResistances.remove(resist.getSpellType());
            }
         }

         if (this.spellResistances.isEmpty()) {
            this.spellResistances = null;
         }
      }
   }

   public final void clearSpellResistances(Communicator communicator) {
      if (this.spellResistances != null) {
         for(SpellResistance resist : this.spellResistances.values()) {
            resist.sendUpdateToClient(communicator, (byte)0);
         }

         this.spellResistances.clear();
      }
   }

   public final void sendSpellResistances(Communicator communicator) {
      if (this.spellResistances != null) {
         for(SpellResistance resist : this.spellResistances.values()) {
            resist.sendUpdateToClient(communicator, (byte)2);
         }
      }
   }

   public final void setArmourLimitingFactor(float factor, Communicator communicator, boolean initializing) {
      float factorToUse = factor;
      if (this.favor >= 35.0F && this.faith >= 70.0F && this.deity != null && this.deity.number == 2) {
         float tempfactor = this.limitingArmourFactor;
         if (factor == -0.15F) {
            tempfactor = 0.0F;
            if (tempfactor == this.limitingArmourFactor) {
               return;
            }

            factorToUse = tempfactor;
         }
      }

      if (this.limitingArmourFactor != factorToUse || initializing) {
         this.limitingArmourFactor = factorToUse;
         communicator.sendRemoveSpellEffect(SpellEffectsEnum.ARMOUR_LIMIT_NONE);
         communicator.sendRemoveSpellEffect(SpellEffectsEnum.ARMOUR_LIMIT_LIGHT);
         communicator.sendRemoveSpellEffect(SpellEffectsEnum.ARMOUR_LIMIT_MEDIUM);
         communicator.sendRemoveSpellEffect(SpellEffectsEnum.ARMOUR_LIMIT_HEAVY);
         SpellEffectsEnum toSend = SpellEffectsEnum.ARMOUR_LIMIT_NONE;
         if (this.limitingArmourFactor == -0.3F) {
            toSend = SpellEffectsEnum.ARMOUR_LIMIT_HEAVY;
         } else if (this.limitingArmourFactor == -0.15F) {
            toSend = SpellEffectsEnum.ARMOUR_LIMIT_MEDIUM;
         } else if (this.limitingArmourFactor == 0.0F) {
            toSend = SpellEffectsEnum.ARMOUR_LIMIT_LIGHT;
         }

         if (toSend != null) {
            communicator.sendAddStatusEffect(toSend, 100000);
         }
      }
   }

   public final float getArmourLimitingFactor() {
      return this.limitingArmourFactor;
   }

   public long getLastChangedPath() {
      return this.lastChangedPath;
   }

   public void setLastChangedPath(long lastChangedPath) {
      this.lastChangedPath = lastChangedPath;
   }

   public final byte getSex() {
      return this.sex;
   }

   public final boolean isMale() {
      return this.sex == 0;
   }

   public final boolean isFemale() {
      return this.sex == 1;
   }

   public final boolean isOnlineHere() {
      return this.currentServer == Servers.localServer.id && Players.getInstance().getPlayerOrNull(this.wurmId) != null;
   }

   List<Route> getHighwayPath() {
      return this.highwayPath;
   }

   void setHighwayPath(String newDestination, List<Route> newPath) {
      this.highwayPath = newPath;
      if (newPath == null) {
         this.highwayPathDestination = "";
      } else {
         this.highwayPathDestination = newDestination;
      }
   }

   String getHighwayPathDestination() {
      return this.highwayPathDestination;
   }

   public SteamId getSteamId() {
      return this.steamId;
   }
}
