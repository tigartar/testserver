package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerMetaData implements MiscConstants {
   private static final String CREATE_PLAYER = "insert into PLAYERS (MAYUSESHOP,PASSWORD,WURMID,LASTLOGOUT,PLAYINGTIME,TEMPLATENAME,SEX,CENTIMETERSHIGH,CENTIMETERSLONG,CENTIMETERSWIDE,INVENTORYID,BODYID,MONEYSALES,BUILDINGID,STAMINA,HUNGER,THIRST,IPADDRESS,REIMBURSED,PLANTEDSIGN,BANNED,PAYMENTEXPIRE,POWER,RANK,DEVTALK,WARNINGS,LASTWARNED,FAITH,DEITY,ALIGNMENT,GOD,FAVOR,LASTCHANGEDDEITY,REALDEATH,CHEATED,LASTFATIGUE,FATIGUE,DEAD,KINGDOM,SESSIONKEY,SESSIONEXPIRE,VERSION,MUTED,LASTFAITH,NUMFAITH,MONEY,CLIMBING,NUMSCHANGEDKINGDOM,AGE,LASTPOLLEDAGE,FAT,BANEXPIRY,BANREASON,FACE,REPUTATION,LASTPOLLEDREP,TITLE,PET,NICOTINE,NICOTINETIME,ALCOHOL,ALCOHOLTIME,LOGGING,MAYMUTE,MUTEEXPIRY,MUTEREASON,LASTSERVER,CURRENTSERVER,REFERRER,EMAIL,PWQUESTION,PWANSWER,PRIEST,BED,SLEEP,CREATIONDATE,THEFTWARNED,NOREIMB,DEATHPROT,FATIGUETODAY,FATIGUEYDAY,FIGHTMODE,NEXTAFFINITY,DETECTIONSECS,TUTORIALLEVEL,AUTOFIGHT,APPOINTMENTS,PA,APPOINTPA,PAWINDOW,NUTRITION,DISEASE,PRIESTTYPE,LASTCHANGEDPRIEST,LASTCHANGEDKINGDOM,LASTLOSTCHAMPION,CHAMPIONPOINTS,CHAMPCHANNELING,MUTETIMES,VOTEDKING,EPICKINGDOM,EPICSERVER,CHAOSKINGDOM,FREETRANSFER,HOTA_WINS,LASTMODIFIEDRANK,MAXRANK,KARMA,MAXKARMA,TOTALKARMA,BLOOD,FLAGS,FLAGS2,ABILITIES,ABILITYTITLE,SCENARIOKARMA,UNDEADTYPE,UNDEADKILLS,UNDEADPKILLS,UNDEADPSECS,NAME,CALORIES,CARBS,FATS,PROTEINS,SECONDTITLE) VALUES(?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String DELETE_PLAYER = "DELETE FROM PLAYERS WHERE WURMID=?";
   private static final String GET_PLAYER = "select NAME from PLAYERS where NAME=?";
   private static final String INSERT_TITLE = "INSERT INTO TITLES (WURMID,TITLEID,TITLENAME) VALUES(?,?,?)";
   private static final String INSERT_FRIEND = DbConnector.isUseSqlite()
      ? "INSERT OR IGNORE INTO FRIENDS(WURMID,FRIEND,CATEGORY) VALUES(?,?,?)"
      : "INSERT IGNORE INTO FRIENDS(WURMID,FRIEND,CATEGORY) VALUES(?,?,?)";
   private static final String DELETE_FRIENDS = "DELETE FROM FRIENDS WHERE WURMID=?";
   private static final String INSERT_ENEMY = DbConnector.isUseSqlite()
      ? "INSERT OR IGNORE INTO ENEMIES(WURMID,ENEMY) VALUES(?,?)"
      : "INSERT IGNORE INTO ENEMIES(WURMID,ENEMY) VALUES(?,?)";
   private static final String INSERT_IGNORED = DbConnector.isUseSqlite()
      ? "INSERT OR IGNORE INTO IGNORED(WURMID,IGNOREE) VALUES(?,?)"
      : "INSERT IGNORE INTO IGNORED(WURMID,IGNOREE) VALUES(?,?)";
   private static final String DELETE_IGNORED = "DELETE FROM IGNORED WHERE WURMID=?";
   private static final Logger logger = Logger.getLogger(PlayerMetaData.class.getName());
   private final long wurmid;
   private final String name;
   public String password;
   private final String session;
   private final short centhigh;
   private final short centlong;
   private final short centwide;
   private final long lastChangedDeity;
   private final byte changedKingdom;
   private final long sessionExpiration;
   private final byte power;
   private final byte deity;
   private final float align;
   private final float faith;
   private final float favor;
   private final byte god;
   private final byte realdeathcounter;
   private final int fatiguesecsleft;
   public int fatigueSecsToday;
   public int fatigueSecsYday;
   public short muteTimes;
   private final long lastfatigue;
   private final long lastwarned;
   private final long lastcheated;
   private final long plantedSign;
   private final long playingTime;
   private final byte kingdom;
   private final int rank;
   public int maxRank;
   private final boolean banned;
   private final long banexpiry;
   private final String banreason;
   private final boolean reimbursed;
   private final int warnings;
   private final boolean mayHearDevtalk;
   public long paymentExpire;
   private final long[] ignored;
   private final long[] friends;
   private final byte[] friendcats;
   private long[] enemies;
   private final String lastip;
   private final String templateName;
   private final long bodyId;
   private final long buildingId;
   private final int damage;
   private final int hunger;
   public float nutrition = 0.0F;
   private final int stunned;
   private final int thirst;
   private final int stamina;
   private final byte sex;
   private final long inventoryId;
   private final boolean onSurface;
   private final boolean unconscious;
   private final float posx;
   private final float posy;
   private final float posz;
   private final float rotation;
   private final int zoneid;
   private final boolean dead;
   private final boolean mute;
   private final long ver;
   private final long lastfaith;
   private final byte numfaith;
   public long money;
   private final boolean climbing;
   private final int age;
   private final long lastPolledAge;
   private final byte fat;
   private final long face;
   public byte blood = 0;
   private final int reputation;
   private final long lastPolledReputation;
   private final int title;
   private final int secondTitle;
   private final int[] titleArr;
   public long pet = -10L;
   public long nicotineTime = 0L;
   public long alcoholTime = 0L;
   public float alcohol = 0.0F;
   public float nicotine = 0.0F;
   public boolean logging = false;
   public long muteexpiry = 0L;
   public String mutereason = "";
   public boolean mayMute = false;
   public boolean overrideshop = false;
   public long lastModifiedRank = 0L;
   public int currentServer;
   public int lastServer;
   public long referrer;
   public String emailAdress = "";
   public String pwQuestion = "";
   public String pwAnswer = "";
   public boolean isPriest = false;
   public byte priestType = 0;
   public long lastChangedPriestType = 0L;
   public long bed = -10L;
   public int sleep = 0;
   public long creationDate = 0L;
   public boolean istheftwarned = false;
   public boolean noReimbLeft = false;
   public boolean deathProt = false;
   public byte fightmode;
   public long nextAffinity;
   public short detectionSecs = 0;
   public int tutLevel = 0;
   public boolean autofight = false;
   public long appointments = 0L;
   public boolean seesPAWin = false;
   public boolean isPA = false;
   public boolean mayAppointPA = false;
   public byte disease = 0;
   public long lastChangedKingdom = System.currentTimeMillis();
   public long lastLostChampion = 0L;
   public short championPoints = 0;
   public float champChanneling = 0.0F;
   public boolean voteKing = false;
   public Map<Integer, Long> cooldowns = new HashMap<>();
   public byte epicKingdom = 0;
   public int epicServerId = 0;
   public byte chaosKingdom = 0;
   public boolean hasFreeTransfer = false;
   public short hotaWins = 0;
   public int karma = 0;
   public int maxKarma = 0;
   public int totalKarma = 0;
   public long abilities = 0L;
   public long flags = 0L;
   public long flags2 = 0L;
   public int abilityTitle = -1;
   public int scenarioKarma = 0;
   public byte undeadType;
   public int undeadKills;
   public int undeadPKills;
   public int undeadPSecs;
   public long moneySalesEver;
   public int daysPrem;
   public long lastTicked;
   public int currentLoyaltyPoints;
   public int totalLoyaltyPoints;
   public int monthsPaidEver;
   public int monthsPaidInARow;
   public int monthsPaidSinceReset;
   public int silverPaidEver;
   public boolean hasAwards;
   public float calories;
   public float carbs;
   public float fats;
   public float proteins;

   public PlayerMetaData(
      long aWurmid,
      String aName,
      String aPassword,
      String aSession,
      short chigh,
      short clong,
      short cwide,
      long aSessionExpiration,
      byte aPower,
      byte aDeity,
      float aAlign,
      float aFaith,
      float aFavor,
      byte aGod,
      byte realdeathc,
      long aLastChangedDeity,
      int aFatiguesecsleft,
      long aLastfatigue,
      long aLastwarned,
      long aLastcheated,
      long aPlantedSign,
      long aPlayingTime,
      byte aKingdom,
      int aRank,
      boolean aBanned,
      long aBanexpiry,
      String aBanreason,
      boolean aReimbursed,
      int aWarnings,
      boolean aMayHearDevtalk,
      long aPaymentExpire,
      long[] aIgnored,
      long[] aFriends,
      byte[] aCats,
      String aTemplateName,
      String aLastip,
      boolean aDead,
      boolean aMute,
      long aBodyId,
      long aBuildingId,
      int aDamage,
      int aHunger,
      int aStunned,
      int aThirst,
      int aStamina,
      byte aSex,
      long aInventoryId,
      boolean aOnSurface,
      boolean aUnconscious,
      float aPosx,
      float aPosy,
      float aPosz,
      float aRotation,
      int aZoneid,
      long version,
      long lastFaith,
      byte numFaith,
      long aMoney,
      boolean climb,
      byte changedKingd,
      int aAge,
      long aLastPolledAge,
      byte aFat,
      long _face,
      int rep,
      long lastPolledRep,
      int _title,
      int _secondTitle,
      int[] _titleArr
   ) {
      this.wurmid = aWurmid;
      this.name = aName;
      this.password = aPassword;
      this.session = aSession;
      this.centhigh = chigh;
      this.centlong = clong;
      this.centwide = cwide;
      this.sessionExpiration = aSessionExpiration;
      this.power = aPower;
      this.deity = aDeity;
      this.align = aAlign;
      this.faith = aFaith;
      this.favor = aFavor;
      this.god = aGod;
      this.realdeathcounter = realdeathc;
      this.lastChangedDeity = aLastChangedDeity;
      this.fatiguesecsleft = aFatiguesecsleft;
      this.lastfatigue = aLastfatigue;
      this.lastwarned = aLastwarned;
      this.lastcheated = 0L;
      this.plantedSign = aPlantedSign;
      this.playingTime = aPlayingTime;
      this.kingdom = aKingdom;
      this.rank = aRank;
      this.banned = aBanned;
      this.banexpiry = aBanexpiry;
      this.banreason = aBanreason;
      this.reimbursed = aReimbursed;
      this.warnings = aWarnings;
      this.mayHearDevtalk = aMayHearDevtalk;
      this.paymentExpire = aPaymentExpire;
      this.ignored = aIgnored;
      this.friends = aFriends;
      this.friendcats = aCats;
      this.lastip = aLastip;
      this.dead = aDead;
      this.mute = aMute;
      this.templateName = aTemplateName;
      this.bodyId = aBodyId;
      this.buildingId = aBuildingId;
      this.damage = aDamage;
      this.hunger = aHunger;
      this.stunned = aStunned;
      this.thirst = aThirst;
      this.stamina = aStamina;
      this.sex = aSex;
      this.inventoryId = aInventoryId;
      this.onSurface = aOnSurface;
      this.unconscious = aUnconscious;
      this.posx = aPosx;
      this.posy = aPosy;
      this.posz = aPosz;
      this.rotation = aRotation;
      this.zoneid = aZoneid;
      this.lastfaith = lastFaith;
      this.numfaith = numFaith;
      this.money = aMoney;
      this.ver = version;
      this.climbing = climb;
      this.changedKingdom = changedKingd;
      this.age = aAge;
      this.lastPolledAge = aLastPolledAge;
      this.fat = aFat;
      this.face = _face;
      this.reputation = rep;
      this.lastPolledReputation = lastPolledRep;
      this.title = _title;
      this.secondTitle = _secondTitle;
      this.titleArr = _titleArr;
   }

   public void save() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         CreaturePos pos = CreaturePos.getPosition(this.wurmid);
         if (pos != null) {
            pos.setPosX(this.posx);
            pos.setPosY(this.posy);
            pos.setPosZ(this.posz, false);
            pos.setRotation(this.rotation);
            pos.setZoneId(this.zoneid);
            pos.setLayer(0);
         } else {
            new CreaturePos(this.wurmid, this.posx, this.posy, this.posz, this.rotation, this.zoneid, 0, -10L, true);
         }

         dbcon = DbConnector.getPlayerDbCon();
         if (this.exists(dbcon)) {
            ps = dbcon.prepareStatement("DELETE FROM PLAYERS WHERE WURMID=?");
            ps.setLong(1, this.wurmid);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
         }

         ps = dbcon.prepareStatement(
            "insert into PLAYERS (MAYUSESHOP,PASSWORD,WURMID,LASTLOGOUT,PLAYINGTIME,TEMPLATENAME,SEX,CENTIMETERSHIGH,CENTIMETERSLONG,CENTIMETERSWIDE,INVENTORYID,BODYID,MONEYSALES,BUILDINGID,STAMINA,HUNGER,THIRST,IPADDRESS,REIMBURSED,PLANTEDSIGN,BANNED,PAYMENTEXPIRE,POWER,RANK,DEVTALK,WARNINGS,LASTWARNED,FAITH,DEITY,ALIGNMENT,GOD,FAVOR,LASTCHANGEDDEITY,REALDEATH,CHEATED,LASTFATIGUE,FATIGUE,DEAD,KINGDOM,SESSIONKEY,SESSIONEXPIRE,VERSION,MUTED,LASTFAITH,NUMFAITH,MONEY,CLIMBING,NUMSCHANGEDKINGDOM,AGE,LASTPOLLEDAGE,FAT,BANEXPIRY,BANREASON,FACE,REPUTATION,LASTPOLLEDREP,TITLE,PET,NICOTINE,NICOTINETIME,ALCOHOL,ALCOHOLTIME,LOGGING,MAYMUTE,MUTEEXPIRY,MUTEREASON,LASTSERVER,CURRENTSERVER,REFERRER,EMAIL,PWQUESTION,PWANSWER,PRIEST,BED,SLEEP,CREATIONDATE,THEFTWARNED,NOREIMB,DEATHPROT,FATIGUETODAY,FATIGUEYDAY,FIGHTMODE,NEXTAFFINITY,DETECTIONSECS,TUTORIALLEVEL,AUTOFIGHT,APPOINTMENTS,PA,APPOINTPA,PAWINDOW,NUTRITION,DISEASE,PRIESTTYPE,LASTCHANGEDPRIEST,LASTCHANGEDKINGDOM,LASTLOSTCHAMPION,CHAMPIONPOINTS,CHAMPCHANNELING,MUTETIMES,VOTEDKING,EPICKINGDOM,EPICSERVER,CHAOSKINGDOM,FREETRANSFER,HOTA_WINS,LASTMODIFIEDRANK,MAXRANK,KARMA,MAXKARMA,TOTALKARMA,BLOOD,FLAGS,FLAGS2,ABILITIES,ABILITYTITLE,SCENARIOKARMA,UNDEADTYPE,UNDEADKILLS,UNDEADPKILLS,UNDEADPSECS,NAME,CALORIES,CARBS,FATS,PROTEINS,SECONDTITLE) VALUES(?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
         );
         ps.setBoolean(1, this.overrideshop);
         ps.setString(2, this.password);
         ps.setLong(3, this.wurmid);
         ps.setLong(4, System.currentTimeMillis());
         ps.setLong(5, this.playingTime);
         ps.setString(6, this.templateName);
         ps.setByte(7, this.sex);
         ps.setShort(8, this.centhigh);
         ps.setShort(9, this.centlong);
         ps.setShort(10, this.centwide);
         ps.setLong(11, this.inventoryId);
         ps.setLong(12, this.bodyId);
         ps.setLong(13, this.moneySalesEver);
         ps.setLong(14, this.buildingId);
         ps.setShort(15, (short)this.stamina);
         ps.setShort(16, (short)this.hunger);
         ps.setShort(17, (short)this.thirst);
         ps.setString(18, this.lastip);
         ps.setBoolean(19, this.reimbursed);
         ps.setLong(20, this.plantedSign);
         ps.setBoolean(21, this.banned);
         ps.setLong(22, this.paymentExpire);
         ps.setByte(23, this.power);
         ps.setInt(24, this.rank);
         ps.setBoolean(25, this.mayHearDevtalk);
         ps.setShort(26, (short)this.warnings);
         ps.setLong(27, this.lastwarned);
         ps.setFloat(28, this.faith);
         ps.setByte(29, this.deity);
         ps.setFloat(30, this.align);
         ps.setByte(31, this.god);
         ps.setFloat(32, this.favor);
         ps.setLong(33, this.lastChangedDeity);
         ps.setByte(34, this.realdeathcounter);
         ps.setLong(35, this.lastcheated);
         ps.setLong(36, this.lastfatigue);
         ps.setInt(37, this.fatiguesecsleft);
         ps.setBoolean(38, this.dead);
         ps.setByte(39, this.kingdom);
         ps.setString(40, this.session);
         ps.setLong(41, this.sessionExpiration);
         ps.setLong(42, this.ver);
         ps.setBoolean(43, this.mute);
         ps.setLong(44, this.lastfaith);
         ps.setByte(45, this.numfaith);
         ps.setLong(46, this.money);
         ps.setBoolean(47, this.climbing);
         ps.setByte(48, this.changedKingdom);
         ps.setInt(49, this.age);
         ps.setLong(50, this.lastPolledAge);
         ps.setByte(51, this.fat);
         ps.setLong(52, this.banexpiry);
         ps.setString(53, this.banreason);
         ps.setLong(54, this.face);
         ps.setInt(55, this.reputation);
         ps.setLong(56, this.lastPolledReputation);
         ps.setInt(57, this.title);
         ps.setLong(58, this.pet);
         ps.setFloat(59, this.nicotine);
         ps.setLong(60, this.nicotineTime);
         ps.setFloat(61, this.alcohol);
         ps.setLong(62, this.alcoholTime);
         ps.setBoolean(63, this.logging);
         ps.setBoolean(64, this.mayMute);
         ps.setLong(65, this.muteexpiry);
         ps.setString(66, this.mutereason);
         ps.setInt(67, this.lastServer);
         ps.setInt(68, this.currentServer);
         ps.setLong(69, this.referrer);
         ps.setString(70, this.emailAdress);
         ps.setString(71, this.pwQuestion);
         ps.setString(72, this.pwAnswer);
         ps.setBoolean(73, this.isPriest);
         ps.setLong(74, this.bed);
         ps.setInt(75, this.sleep);
         ps.setLong(76, this.creationDate);
         ps.setBoolean(77, this.istheftwarned);
         ps.setBoolean(78, this.noReimbLeft);
         ps.setBoolean(79, this.deathProt);
         ps.setInt(80, this.fatigueSecsToday);
         ps.setInt(81, this.fatigueSecsYday);
         ps.setByte(82, this.fightmode);
         ps.setLong(83, this.nextAffinity);
         ps.setShort(84, this.detectionSecs);
         ps.setInt(85, this.tutLevel);
         ps.setBoolean(86, this.autofight);
         ps.setLong(87, this.appointments);
         ps.setBoolean(88, this.isPA);
         ps.setBoolean(89, this.mayAppointPA);
         ps.setBoolean(90, this.seesPAWin);
         ps.setFloat(91, this.nutrition);
         ps.setByte(92, this.disease);
         ps.setByte(93, this.priestType);
         ps.setLong(94, this.lastChangedPriestType);
         ps.setLong(95, this.lastChangedKingdom);
         ps.setLong(96, this.lastLostChampion);
         ps.setShort(97, this.championPoints);
         ps.setFloat(98, this.champChanneling);
         ps.setShort(99, this.muteTimes);
         ps.setBoolean(100, this.voteKing);
         ps.setByte(101, this.epicKingdom);
         ps.setInt(102, this.epicServerId);
         ps.setInt(103, this.chaosKingdom);
         ps.setBoolean(104, this.hasFreeTransfer);
         ps.setShort(105, this.hotaWins);
         ps.setLong(106, this.lastModifiedRank);
         ps.setInt(107, this.maxRank);
         ps.setInt(108, this.karma);
         ps.setInt(109, this.maxKarma);
         ps.setInt(110, this.totalKarma);
         ps.setByte(111, this.blood);
         ps.setLong(112, this.flags);
         ps.setLong(113, this.flags2);
         ps.setLong(114, this.abilities);
         ps.setInt(115, this.abilityTitle);
         ps.setInt(116, this.scenarioKarma);
         ps.setByte(117, this.undeadType);
         ps.setInt(118, this.undeadKills);
         ps.setInt(119, this.undeadPKills);
         ps.setInt(120, this.undeadPSecs);
         ps.setString(121, this.name);
         ps.setFloat(122, this.calories);
         ps.setFloat(123, this.carbs);
         ps.setFloat(124, this.fats);
         ps.setFloat(125, this.proteins);
         ps.setInt(126, this.secondTitle);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         Players.getInstance().registerNewKingdom(this.wurmid, this.kingdom);
         ps = dbcon.prepareStatement("DELETE FROM FRIENDS WHERE WURMID=?");
         ps.setLong(1, this.wurmid);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         if (this.friends != null) {
            for(int x = 0; x < this.friends.length; ++x) {
               ps = dbcon.prepareStatement(INSERT_FRIEND);
               ps.setLong(1, this.wurmid);
               ps.setLong(2, this.friends[x]);
               ps.setByte(3, this.friendcats[x]);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }

         ps = dbcon.prepareStatement("DELETE FROM IGNORED WHERE WURMID=?");
         ps.setLong(1, this.wurmid);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         if (this.ignored != null) {
            for(int x = 0; x < this.ignored.length; ++x) {
               ps = dbcon.prepareStatement(INSERT_IGNORED);
               ps.setLong(1, this.wurmid);
               ps.setLong(2, this.ignored[x]);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }

         if (this.enemies != null) {
            for(int x = 0; x < this.enemies.length; ++x) {
               ps = dbcon.prepareStatement(INSERT_ENEMY);
               ps.setLong(1, this.wurmid);
               ps.setLong(2, this.enemies[x]);
               ps.executeUpdate();
               DbUtilities.closeDatabaseObjects(ps, null);
            }
         }

         if (this.titleArr.length > 0) {
            for(int x = 0; x < this.titleArr.length; ++x) {
               Titles.Title t = Titles.Title.getTitle(this.titleArr[x]);
               if (t != null) {
                  ps = dbcon.prepareStatement("INSERT INTO TITLES (WURMID,TITLEID,TITLENAME) VALUES(?,?,?)");
                  ps.setLong(1, this.wurmid);
                  ps.setInt(2, this.titleArr[x]);
                  ps.setString(3, t.getName(true));
                  ps.executeUpdate();
                  DbUtilities.closeDatabaseObjects(ps, null);
               }
            }
         }

         if (this.hasAwards) {
            ps = dbcon.prepareStatement("DELETE FROM AWARDS WHERE WURMID=?");
            ps.setLong(1, this.wurmid);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(
               "INSERT INTO AWARDS(WURMID, DAYSPREM, MONTHSPREM, MONTHSEVER, CONSECMONTHS, SILVERSPURCHASED, LASTTICKEDPREM, CURRENTLOYALTY, TOTALLOYALTY) VALUES(?,?,?,?,?,?,?,?,?)"
            );
            ps.setLong(1, this.wurmid);
            ps.setInt(2, this.daysPrem);
            ps.setInt(3, this.monthsPaidSinceReset);
            ps.setInt(4, this.monthsPaidEver);
            ps.setInt(5, this.monthsPaidInARow);
            ps.setInt(6, this.silverPaidEver);
            ps.setLong(7, this.lastTicked);
            ps.setInt(8, this.currentLoyaltyPoints);
            ps.setInt(9, this.totalLoyaltyPoints);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
         }

         if (this.cooldowns.size() > 0) {
            Cooldowns cd = Cooldowns.getCooldownsFor(this.wurmid, true);

            for(Entry<Integer, Long> ent : this.cooldowns.entrySet()) {
               cd.addCooldown(ent.getKey(), ent.getValue(), false);
            }
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, this.name + " " + var10.getMessage(), (Throwable)var10);
         throw new IOException(var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private boolean exists(Connection dbcon) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean existed;
      try {
         ps = dbcon.prepareStatement("select NAME from PLAYERS where NAME=?");
         ps.setString(1, this.name);
         rs = ps.executeQuery();
         existed = rs.next();
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      return existed;
   }
}
