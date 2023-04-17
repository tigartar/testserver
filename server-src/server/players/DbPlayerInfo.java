/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Features;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.intra.MoneyTransfer;
import com.wurmonline.server.players.Awards;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.SteamIdHistory;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.players.WurmRecord;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class DbPlayerInfo
extends PlayerInfo
implements MonetaryConstants {
    private static final Logger logger = Logger.getLogger(DbPlayerInfo.class.getName());
    private static final int MAX_HISTORY_IP = 10;
    private static final int MAX_HISTORY_EMAIL = 5;
    private static final String GET_PLAYER = "select * from PLAYERS where NAME=?";
    private static final String GET_TITLES = "select TITLEID from TITLES where WURMID=?";
    private static final String CHECK_PLAYER = "select NAME from PLAYERS where NAME=?";
    private static final String SAVE_PLAYER = "update PLAYERS set WURMID=?, PASSWORD=?, LASTLOGOUT=?, PLAYINGTIME=?,  REIMBURSED=?, BANNED=?, PAYMENTEXPIRE=?,POWER=?, RANK=?,LASTCHANGEDDEITY=?,FATIGUE=?, LASTFATIGUE=?, SESSIONKEY=?,SESSIONEXPIRE=?,VERSION=?,CREATIONDATE=?,FACE=?,REPUTATION=?,LASTPOLLEDREP=?,TITLE=?,CURRENTSERVER=?, LASTSERVER=?, EMAIL=?,PWQUESTION=?, PWANSWER=?,BED=?,SLEEP=?, FATIGUETODAY=?, FATIGUEYDAY=?, LASTCHANGEDKINGDOM=?, SECONDTITLE=? where NAME=?";
    private static final String CREATE_PLAYER = "insert into PLAYERS ( WURMID, PASSWORD, LASTLOGOUT, PLAYINGTIME, REIMBURSED,BANNED, PAYMENTEXPIRE,POWER,RANK,LASTCHANGEDDEITY,FATIGUE,LASTFATIGUE,SESSIONKEY,SESSIONEXPIRE,VERSION,CREATIONDATE,FACE,REPUTATION,LASTPOLLEDREP,TITLE,CURRENTSERVER, LASTSERVER,EMAIL,PWQUESTION,PWANSWER,BED,SLEEP,FATIGUETODAY,FATIGUEYDAY,LASTCHANGEDKINGDOM,SECONDTITLE,NAME) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String LOAD_AWARDS = "SELECT * FROM AWARDS WHERE WURMID=?";
    private static final String SET_IPADDRESS = "update PLAYERS set IPADDRESS=? WHERE NAME=?";
    private static final String SET_REIMBURSED = "update PLAYERS set REIMBURSED=? WHERE NAME=?";
    private static final String SET_PLANTEDSIGN = "update PLAYERS set PLANTEDSIGN=? WHERE NAME=?";
    private static final String SET_BANNED = "update PLAYERS set BANNED=?,BANREASON=?,BANEXPIRY=? WHERE NAME=?";
    private static final String SET_POWER = "update PLAYERS set POWER=? WHERE NAME=?";
    private static final String SET_PASSWORD = "update PLAYERS set PASSWORD=? WHERE NAME=?";
    private static final String SET_NAME = "update PLAYERS set NAME=? WHERE NAME=?";
    private static final String SET_PAYMENTEXPIRE = "update PLAYERS set PAYMENTEXPIRE=? WHERE NAME=?";
    private static final String SET_ENEMYTERR = "update PLAYERS set ENEMYTERR=?, LASTMOVEDTERR=? WHERE NAME=?";
    private static final String SET_FREETRANSFER = "update PLAYERS set FREETRANSFER=? WHERE NAME=?";
    private static final String SET_RANK = "update PLAYERS set RANK=?, MAXRANK=?, LASTMODIFIEDRANK=? WHERE NAME=?";
    private static final String SET_CHAMPION = "update CHAMPIONS set POINTS=?,NAME=? WHERE WURMID=? AND CURRENT=1";
    private static final String ADD_CHAMPION = "INSERT INTO CHAMPIONS(POINTS,NAME,WURMID,CURRENT) VALUES (?,?,?,1)";
    private static final String SET_INACTIVE_CHAMPION = "update CHAMPIONS set POINTS=?,NAME=?,CURRENT=0 WHERE WURMID=?";
    private static final String SET_UNDEAD = "update PLAYERS SET UNDEADTYPE=?,UNDEADKILLS=?,UNDEADPKILLS=?,UNDEADPSECS=? WHERE NAME=?";
    private static final String SET_MONEY = "update PLAYERS set MONEY=? WHERE NAME=?";
    private static final String SET_MONEYSALES = "update PLAYERS set MONEYSALES =? WHERE NAME=?";
    private static final String SET_EMAIL = "update PLAYERS set EMAIL=? WHERE NAME=?";
    private static final String SET_APPOINTMENTS = "update PLAYERS set APPOINTMENTS=? WHERE NAME=?";
    private static final String SET_PRIEST = "update PLAYERS set PRIEST=? WHERE NAME=?";
    private static final String SET_DEITY = "update PLAYERS set DEITY=? WHERE NAME=?";
    private static final String TOUCH_JOAT = "update PLAYERS set LASTJOAT=? WHERE NAME=?";
    private static final String SET_GOD = "update PLAYERS set GOD=? WHERE NAME=?";
    private static final String SET_MODELNAME = "update PLAYERS set MODELNAME=? WHERE NAME=?";
    private static final String SET_FAITH = "update PLAYERS set FAITH=? WHERE NAME=?";
    private static final String SET_FAVOR = "update PLAYERS set FAVOR=? WHERE NAME=?";
    private static final String SET_CHEATED = "update PLAYERS set CHEATED=?,CHEATREASON=? WHERE NAME=?";
    private static final String SET_CLIMBING = "update PLAYERS set CLIMBING=? WHERE NAME=?";
    private static final String SET_ALIGNMENT = "update PLAYERS set ALIGNMENT=? WHERE NAME=?";
    private static final String SET_VERSION = "update PLAYERS set VERSION=? WHERE NAME=?";
    private static final String SET_LASTTRIGGER = "update PLAYERS set LASTTRIGGER=? WHERE NAME=?";
    private static final String SET_DEAD = "update PLAYERS set DEAD=? WHERE NAME=?";
    private static final String SET_FACE = "update PLAYERS set FACE=? WHERE NAME=?";
    private static final String SET_MUTED = "update PLAYERS set MUTED=?,MUTEREASON=?,MUTEEXPIRY=? WHERE NAME=?";
    private static final String SET_REALDEATH = "update PLAYERS set REALDEATH=? WHERE NAME=?";
    private static final String SET_CHANGED_DEITY = "update PLAYERS set LASTCHANGEDDEITY=? WHERE NAME=?";
    private static final String WARN = "update PLAYERS set WARNINGS=?, LASTWARNED=? WHERE NAME=?";
    private static final String SET_SESSION = "update PLAYERS set SESSIONKEY=?, SESSIONEXPIRE=? WHERE NAME=?";
    private static final String SET_FATIGUE = "update PLAYERS set FATIGUE=?,FATIGUETODAY=?, LASTFATIGUE=? WHERE NAME=?";
    private static final String SWITCH_FATIGUE = "update PLAYERS set FATIGUETODAY=?, FATIGUEYDAY=? WHERE NAME=?";
    private static final String SET_NUMCHANGEKINGDOM = "update PLAYERS set NUMSCHANGEDKINGDOM=? WHERE NAME=?";
    private static final String ADD_FRIEND = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO FRIENDS (WURMID,FRIEND,CATEGORY,NOTE) VALUES(?,?,?,?)" : "INSERT IGNORE INTO FRIENDS (WURMID,FRIEND,CATEGORY,NOTE) VALUES(?,?,?,?)";
    private static final String UPDATE_FRIEND = "UPDATE FRIENDS SET CATEGORY=?,NOTE=? WHERE WURMID=? AND FRIEND=?";
    private static final String LOAD_FRIENDS = "SELECT FRIEND,CATEGORY,NOTE FROM FRIENDS WHERE WURMID=?";
    private static final String REMOVE_FRIEND = "DELETE FROM FRIENDS WHERE WURMID=? AND FRIEND=?";
    private static final String ADD_ENEMY = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ENEMIES (WURMID,ENEMY) VALUES(?,?)" : "INSERT IGNORE INTO ENEMIES (WURMID,ENEMY) VALUES(?,?)";
    private static final String REMOVE_ENEMY = "DELETE FROM ENEMIES WHERE WURMID=? AND ENEMY=?";
    private static final String ADD_IGNORED = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO IGNORED (WURMID,IGNOREE) VALUES(?,?)" : "INSERT IGNORE INTO IGNORED (WURMID,IGNOREE) VALUES(?,?)";
    private static final String LOAD_IGNORED = "SELECT IGNOREE FROM IGNORED WHERE WURMID=?";
    private static final String REMOVE_IGNORED = "DELETE FROM IGNORED WHERE WURMID=? AND IGNOREE=?";
    private static final String SET_NUMFAITH = "update PLAYERS set NUMFAITH=?, LASTFAITH=? WHERE NAME=?";
    private static final String SET_SEX = "update PLAYERS set SEX=? WHERE NAME=?";
    private static final String ADD_TITLE = "INSERT INTO TITLES (WURMID, TITLEID, TITLENAME) VALUES(?,?,?)";
    private static final String REMOVE_TITLE = "DELETE FROM TITLES WHERE WURMID=? AND TITLEID=?";
    private static final String SET_REPUTATION = "update PLAYERS set REPUTATION=?, LASTPOLLEDREP=? WHERE NAME=?";
    private static final String SET_PET = "update PLAYERS set PET=? WHERE NAME=?";
    private static final String SET_NICOTINE = "update PLAYERS set NICOTINE=? WHERE NAME=?";
    private static final String SET_NICOTINETIME = "update PLAYERS set NICOTINETIME=? WHERE NAME=?";
    private static final String SET_ALCOHOL = "update PLAYERS set ALCOHOL=? WHERE NAME=?";
    private static final String SET_ALCOHOLTIME = "update PLAYERS set ALCOHOLTIME=? WHERE NAME=?";
    private static final String SET_MAYMUTE = "update PLAYERS set MAYMUTE=? WHERE NAME=?";
    private static final String SET_REFERRER = "update PLAYERS set REFERRER=? WHERE NAME=?";
    private static final String SET_BED = "update PLAYERS set BED=? WHERE NAME=?";
    private static final String SET_LASTCHANGEDVILLAGE = "update PLAYERS set CHANGEDVILLAGE=? WHERE NAME=?";
    private static final String SET_MAYUSESHOP = "update PLAYERS set MAYUSESHOP=? WHERE NAME=?";
    private static final String SET_THEFTWARNED = "update PLAYERS set THEFTWARNED=? WHERE NAME=?";
    private static final String SET_NOREIMB = "update PLAYERS set NOREIMB=? WHERE NAME=?";
    private static final String SET_DEATHPROT = "update PLAYERS set DEATHPROT=? WHERE NAME=?";
    private static final String SET_SLEEP = "update PLAYERS set SLEEP=? WHERE NAME=?";
    private static final String SET_DEVTALK = "update PLAYERS set DEVTALK=? WHERE NAME=?";
    private static final String SET_VOTEDKING = "update PLAYERS set VOTEDKING=? WHERE NAME=?";
    private static final String SET_CURRENTSERVER = "update PLAYERS set LASTSERVER=?,CURRENTSERVER=? WHERE NAME=?";
    private static final String SET_FIGHTMODE = "update PLAYERS set FIGHTMODE=? WHERE NAME=?";
    private static final String SET_NEXTAFFINITY = "update PLAYERS set NEXTAFFINITY=? WHERE NAME=?";
    private static final String SET_MOVEDINVENTORY = "update PLAYERS set MOVEDINV=? WHERE NAME=?";
    private static final String SET_TUTORIAL = "update PLAYERS set TUTORIALLEVEL=? WHERE NAME=?";
    private static final String SET_AUTOFIGHT = "update PLAYERS set AUTOFIGHT=? WHERE NAME=?";
    private static final String SET_VEHICLE = "update PLAYERS set VEHICLE=? WHERE NAME=?";
    private static final String SET_PA = "update PLAYERS set PA=? WHERE NAME=?";
    private static final String SET_APPOINTPA = "update PLAYERS set APPOINTPA=? WHERE NAME=?";
    private static final String SET_PAWINDOW = "update PLAYERS set PAWINDOW=? WHERE NAME=?";
    private static final String SET_PRIESTTYPE = "update PLAYERS set PRIESTTYPE=?,LASTCHANGEDPRIEST=? WHERE NAME=?";
    private static final String SET_HASSKILLGAIN = "update PLAYERS set HASSKILLGAIN=? WHERE NAME=?";
    private static final String SET_CHANGEDKINGDOM = "update PLAYERS set LASTCHANGEDKINGDOM=? WHERE NAME=?";
    private static final String SET_LOSTCHAMPION = "update PLAYERS set LASTLOSTCHAMPION=? WHERE NAME=?";
    private static final String SET_CHAMPIONPOINTS = "update PLAYERS set CHAMPIONPOINTS=? WHERE NAME=?";
    private static final String SET_CHAMPCHANNELING = "update PLAYERS set CHAMPCHANNELING=? WHERE NAME=?";
    private static final String SET_MUTETIMES = "update PLAYERS set MUTETIMES=? WHERE NAME=?";
    private static final String SET_EPICLOCATION = "update PLAYERS set EPICKINGDOM=?, EPICSERVER=? WHERE NAME=?";
    private static final String SET_CHAOSKINGDOM = "update PLAYERS set CHAOSKINGDOM=? WHERE NAME=?";
    private static final String SET_HOTA_WINS = "update PLAYERS set HOTA_WINS=? WHERE NAME=?";
    private static final String SET_SPAMMODE = "update PLAYERS set SPAMMODE=? WHERE NAME=?";
    private static final String SET_BLOOD = "update PLAYERS set BLOOD=? WHERE NAME=?";
    private static final String SET_FLAGS = "update PLAYERS set FLAGS=?,FLAGS2=? WHERE NAME=?";
    private static final String SET_ABILITIES = "update PLAYERS set ABILITIES=? WHERE NAME=?";
    private static final String SET_ABILITYTITLE = "update PLAYERS set ABILITYTITLE=? WHERE NAME=?";
    private static final String SET_KARMAVALUES = "update PLAYERS set KARMA=?, MAXKARMA=?, TOTALKARMA=? WHERE NAME=?";
    private static final String SET_SCENARIOKARMA = "update PLAYERS set SCENARIOKARMA=? WHERE NAME=?";
    private static final String SET_PASSRETRIEVAL = "update PLAYERS set PWQUESTION=?,PWANSWER=? WHERE NAME=?";
    private static final String GET_HISTORY_IPS = "SELECT * FROM PLAYERHISTORYIPS WHERE PLAYERID=?";
    private static final String ADD_HISTORY_IP = "INSERT INTO PLAYERHISTORYIPS(PLAYERID,IPADDRESS,FIRSTUSED,LASTUSED) VALUES(?,?,?,?)";
    private static final String UPDATE_HISTORY_IP = "UPDATE PLAYERHISTORYIPS SET LASTUSED=? WHERE PLAYERID=? AND IPADDRESS=?";
    private static final String DELETE_HISTORY_IP = "DELETE FROM PLAYERHISTORYIPS WHERE PLAYERID=? AND IPADDRESS=?";
    private static final String GET_HISTORY_EMAIL = "SELECT * FROM PLAYEREHISTORYEMAIL WHERE PLAYERID=?";
    private static final String ADD_HISTORY_EMAIL = "INSERT INTO PLAYEREHISTORYEMAIL(PLAYERID,EMAIL_ADDRESS,DATED) VALUES(?,?,?)";
    private static final String DELETE_HISTORY_EMAIL = "DELETE FROM PLAYEREHISTORYEMAIL WHERE PLAYERID=? AND EMAIL_ADDRESS=?";
    private static final String GET_HISTORY_STEAM_IDS = "SELECT * FROM STEAM_IDS WHERE PLAYER_ID=?";
    private static final String ADD_HISTORY_STEAM_ID = "INSERT INTO STEAM_IDS(PLAYER_ID,STEAM_ID,FIRST_USED,LAST_USED) VALUES(?,?,?,?)";
    private static final String UPDATE_HISTORY_STEAM_ID = "UPDATE STEAM_IDS SET LAST_USED=? WHERE PLAYER_ID=? AND STEAM_ID=?";
    private static final String DELETE_HISTORY_STEAM_ID = "DELETE FROM STEAM_IDS WHERE PLAYER_ID=? AND STEAM_ID=?";

    DbPlayerInfo(String filename) {
        super(filename);
    }

    @Override
    public void load() throws IOException {
        if (this.loaded) {
            return;
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_PLAYER);
            ps.setString(1, this.name);
            rs = ps.executeQuery();
            boolean existed = false;
            if (rs.next()) {
                int titnum;
                Deity d;
                this.wurmId = rs.getLong("WURMID");
                this.password = rs.getString("PASSWORD");
                this.playingTime = rs.getLong("PLAYINGTIME");
                this.reimbursed = rs.getBoolean("REIMBURSED");
                this.plantedSign = rs.getLong("PLANTEDSIGN");
                this.banned = rs.getBoolean("BANNED");
                this.power = rs.getByte("POWER");
                this.rank = rs.getInt("RANK");
                this.maxRank = rs.getInt("MAXRANK");
                this.lastModifiedRank = rs.getLong("LASTMODIFIEDRANK");
                this.mayHearDevTalk = rs.getBoolean("DEVTALK");
                this.paymentExpireDate = rs.getLong("PAYMENTEXPIRE");
                this.lastLogout = rs.getLong("LASTLOGOUT");
                this.lastWarned = rs.getLong("LASTWARNED");
                this.warnings = rs.getShort("WARNINGS");
                this.lastCheated = rs.getLong("CHEATED");
                this.lastFatigue = rs.getLong("LASTFATIGUE");
                this.fatigueSecsLeft = rs.getInt("FATIGUE");
                this.fatigueSecsToday = rs.getInt("FATIGUETODAY");
                this.fatigueSecsYesterday = rs.getInt("FATIGUEYDAY");
                this.dead = rs.getBoolean("DEAD");
                this.sessionKey = rs.getString("SESSIONKEY");
                this.sessionExpiration = rs.getLong("SESSIONEXPIRE");
                this.version = rs.getLong("VERSION");
                this.money = rs.getLong("MONEY");
                this.climbing = rs.getBoolean("CLIMBING");
                this.banexpiry = rs.getLong("BANEXPIRY");
                this.banreason = rs.getString("BANREASON");
                if (this.banreason == null) {
                    this.banreason = "";
                }
                this.logging = rs.getBoolean("LOGGING");
                this.referrer = rs.getLong("REFERRER");
                this.appointments = rs.getLong("APPOINTMENTS");
                this.hasFreeTransfer = rs.getBoolean("FREETRANSFER");
                this.votedKing = rs.getBoolean("VOTEDKING");
                this.sex = rs.getByte("SEX");
                if (this.sessionKey == null) {
                    this.sessionKey = "";
                }
                if (this.playingTime < 0L) {
                    this.playingTime = 0L;
                }
                if (this.playingTime > 0L) {
                    logger.log(Level.INFO, this.name + " has played " + Methods.getTimeString(this.playingTime) + " at load.");
                }
                this.alignment = rs.getFloat("ALIGNMENT");
                byte deityNum = rs.getByte("DEITY");
                this.deity = deityNum > 0 ? (d = Deities.getDeity(deityNum)) : null;
                this.favor = rs.getFloat("FAVOR");
                this.faith = rs.getFloat("FAITH");
                byte gid = rs.getByte("GOD");
                if (gid > 0) {
                    Deity d2;
                    this.god = d2 = Deities.getDeity(gid);
                }
                this.lastChangedDeity = rs.getLong("LASTCHANGEDDEITY");
                this.changedKingdom = rs.getByte("NUMSCHANGEDKINGDOM");
                this.realdeath = rs.getByte("REALDEATH");
                this.muted = rs.getBoolean("MUTED");
                this.muteTimes = rs.getShort("MUTETIMES");
                this.lastFaith = rs.getLong("LASTFAITH");
                this.numFaith = rs.getByte("NUMFAITH");
                this.creationDate = rs.getLong("CREATIONDATE");
                this.face = rs.getLong("FACE");
                this.reputation = rs.getInt("REPUTATION");
                this.lastPolledReputation = rs.getLong("LASTPOLLEDREP");
                if (this.lastPolledReputation == 0L) {
                    this.lastPolledReputation = System.currentTimeMillis();
                }
                if ((titnum = rs.getInt("TITLE")) > 0) {
                    this.title = Titles.Title.getTitle(titnum);
                }
                try {
                    int secTitleNum = rs.getInt("SECONDTITLE");
                    if (secTitleNum > 0) {
                        this.secondTitle = Titles.Title.getTitle(secTitleNum);
                    }
                }
                catch (SQLException ex) {
                    logger.severe("You may need to run the script addSecondTitle.sql!");
                    logger.severe(ex.getMessage());
                    this.secondTitle = null;
                }
                this.pet = rs.getLong("PET");
                this.nicotine = rs.getFloat("NICOTINE");
                this.alcohol = rs.getFloat("ALCOHOL");
                this.nicotineAddiction = rs.getLong("NICOTINETIME");
                this.alcoholAddiction = rs.getLong("ALCOHOLTIME");
                this.mayMute = rs.getBoolean("MAYMUTE");
                this.overRideShop = rs.getBoolean("MAYUSESHOP");
                this.muteexpiry = rs.getLong("MUTEEXPIRY");
                this.mutereason = rs.getString("MUTEREASON");
                this.lastServer = rs.getInt("LASTSERVER");
                this.currentServer = rs.getInt("CURRENTSERVER");
                this.emailAddress = rs.getString("EMAIL");
                this.pwQuestion = rs.getString("PWQUESTION");
                this.pwAnswer = rs.getString("PWANSWER");
                this.isPriest = rs.getBoolean("PRIEST");
                this.bed = rs.getLong("BED");
                this.sleep = rs.getInt("SLEEP");
                this.isTheftWarned = rs.getBoolean("THEFTWARNED");
                this.noReimbursementLeft = rs.getBoolean("NOREIMB");
                this.deathProtected = rs.getBoolean("DEATHPROT");
                this.lastChangedVillage = rs.getLong("CHANGEDVILLAGE");
                this.fightmode = rs.getByte("FIGHTMODE");
                this.nextAffinity = rs.getLong("NEXTAFFINITY");
                this.tutorialLevel = rs.getInt("TUTORIALLEVEL");
                this.autoFighting = rs.getBoolean("AUTOFIGHT");
                this.playerAssistant = rs.getBoolean("PA");
                this.mayAppointPlayerAssistant = rs.getBoolean("APPOINTPA");
                this.seesPlayerAssistantWindow = rs.getBoolean("PAWINDOW");
                this.lastTaggedKindom = rs.getByte("ENEMYTERR");
                this.lastMovedBetweenKingdom = rs.getLong("LASTMOVEDTERR");
                this.priestType = rs.getByte("PRIESTTYPE");
                this.lastChangedPriestType = rs.getLong("LASTCHANGEDPRIEST");
                this.hasMovedInventory = rs.getBoolean("MOVEDINV");
                this.hasSkillGain = rs.getBoolean("HASSKILLGAIN");
                this.lastTriggerEffect = rs.getInt("LASTTRIGGER");
                this.lastChangedKindom = rs.getLong("LASTCHANGEDKINGDOM");
                this.championTimeStamp = rs.getLong("LASTLOSTCHAMPION");
                this.championPoints = rs.getShort("CHAMPIONPOINTS");
                this.champChanneling = rs.getFloat("CHAMPCHANNELING");
                this.epicKingdom = rs.getByte("EPICKINGDOM");
                this.epicServerId = rs.getInt("EPICSERVER");
                this.chaosKingdom = rs.getByte("CHAOSKINGDOM");
                this.hotaWins = rs.getShort("HOTA_WINS");
                this.spamMode = rs.getBoolean("SPAMMODE");
                this.karma = rs.getInt("KARMA");
                this.maxKarma = rs.getInt("MAXKARMA");
                this.totalKarma = rs.getInt("TOTALKARMA");
                this.blood = rs.getByte("BLOOD");
                this.flags = rs.getLong("FLAGS");
                this.flags2 = rs.getLong("FLAGS2");
                this.abilities = rs.getLong("ABILITIES");
                this.abilityTitle = rs.getInt("ABILITYTITLE");
                this.undeadType = rs.getByte("UNDEADTYPE");
                this.undeadKills = rs.getInt("UNDEADKILLS");
                this.undeadPlayerKills = rs.getInt("UNDEADPKILLS");
                this.undeadPlayerSeconds = rs.getInt("UNDEADPSECS");
                this.moneyEarnedBySellingEver = rs.getLong("MONEYSALES");
                this.setFlagBits(this.flags);
                this.setFlag2Bits(this.flags2);
                this.setAbilityBits(this.abilities);
                this.scenarioKarma = rs.getInt("SCENARIOKARMA");
                if ((Servers.localServer.id == this.currentServer || Servers.localServer.LOGINSERVER) && this.paymentExpireDate > 0L) {
                    this.awards = DbPlayerInfo.loadAward(this.wurmId);
                }
                existed = true;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            if (!existed) {
                throw new IOException("No such player - " + this.name);
            }
            this.loadIgnored(this.wurmId);
            this.loadFriends(this.wurmId);
            this.loadTitles(this.wurmId);
            this.loadHistoryIPs(this.wurmId);
            this.loadHistorySteamIds(this.wurmId);
            this.loadHistoryEmails(this.wurmId);
            this.loaded = true;
            PlayerInfoFactory.addPlayerInfo(this);
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage());
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(CHECK_PLAYER);
            ps.setString(1, this.name);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SAVE_PLAYER);
            } else {
                ps = dbcon.prepareStatement(CREATE_PLAYER);
                this.lastServer = Servers.localServer.id;
                this.currentServer = Servers.localServer.id;
            }
            ps.setLong(1, this.wurmId);
            ps.setString(2, this.password);
            ps.setLong(3, this.lastLogout);
            ps.setLong(4, this.playingTime);
            ps.setBoolean(5, this.reimbursed);
            ps.setBoolean(6, this.banned);
            ps.setLong(7, this.paymentExpireDate);
            ps.setByte(8, this.power);
            ps.setInt(9, this.rank);
            ps.setLong(10, this.lastChangedDeity);
            ps.setInt(11, this.fatigueSecsLeft);
            ps.setLong(12, this.lastFatigue);
            ps.setString(13, this.sessionKey);
            ps.setLong(14, this.sessionExpiration);
            ps.setLong(15, WurmCalendar.currentTime);
            ps.setLong(16, this.creationDate);
            ps.setLong(17, this.face);
            ps.setInt(18, this.reputation);
            ps.setLong(19, this.lastPolledReputation);
            if (this.title != null) {
                ps.setInt(20, this.title.id);
            } else {
                ps.setInt(20, 0);
            }
            ps.setInt(21, this.currentServer);
            ps.setInt(22, this.lastServer);
            ps.setString(23, this.emailAddress);
            ps.setString(24, this.pwQuestion);
            if (this.pwAnswer.length() > 20) {
                this.pwAnswer = this.pwAnswer.substring(0, 20);
            }
            ps.setString(25, this.pwAnswer);
            ps.setLong(26, this.bed);
            ps.setInt(27, this.sleep);
            ps.setInt(28, this.fatigueSecsToday);
            ps.setInt(29, this.fatigueSecsYesterday);
            ps.setLong(30, this.lastChangedKindom);
            if (this.secondTitle != null) {
                ps.setInt(31, this.secondTitle.id);
            } else {
                ps.setInt(31, 0);
            }
            ps.setString(32, this.name);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public String getIpaddress() {
        if (this.ipaddress == null) {
            this.ipaddress = "";
        }
        return this.ipaddress;
    }

    @Override
    public void setIpaddress(String address) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_IPADDRESS);
                ps.setString(1, address);
                ps.setString(2, this.name);
                ps.executeUpdate();
                long now = System.currentTimeMillis();
                if (this.historyIPStart.containsKey(address)) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ps = dbcon.prepareStatement(UPDATE_HISTORY_IP);
                    ps.setLong(1, now);
                    ps.setLong(2, this.wurmId);
                    ps.setString(3, address);
                    ps.executeUpdate();
                } else {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ps = dbcon.prepareStatement(ADD_HISTORY_IP);
                    ps.setLong(1, this.wurmId);
                    ps.setString(2, address);
                    ps.setLong(3, now);
                    ps.setLong(4, now);
                    ps.executeUpdate();
                    this.historyIPStart.put(address, now);
                }
                this.historyIPLast.put(address, now);
                if (this.historyIPStart.size() > 10) {
                    long oldest = -1L;
                    String ipAddress = "";
                    for (Map.Entry entry : this.historyIPStart.entrySet()) {
                        if (oldest != -1L && (Long)entry.getValue() >= oldest) continue;
                        ipAddress = (String)entry.getKey();
                        oldest = (Long)entry.getValue();
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ps = dbcon.prepareStatement(DELETE_HISTORY_IP);
                    ps.setLong(1, this.wurmId);
                    ps.setString(2, ipAddress);
                    ps.executeUpdate();
                    this.historyIPStart.remove(ipAddress);
                    this.historyIPLast.remove(ipAddress);
                }
            }
            this.ipaddress = address;
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setSteamId(SteamId aSteamId) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (!this.exists(dbcon)) {
                return;
            }
            long now = System.currentTimeMillis();
            if (this.historySteamId.containsKey(aSteamId)) {
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(UPDATE_HISTORY_STEAM_ID);
                ps.setLong(1, now);
                ps.setLong(2, this.wurmId);
                ps.setLong(3, aSteamId.getSteamID64());
                ps.executeUpdate();
                ((SteamIdHistory)this.historySteamId.get(aSteamId)).setLastUsed(now);
            } else {
                DbUtilities.closeDatabaseObjects(ps, null);
                ps = dbcon.prepareStatement(ADD_HISTORY_STEAM_ID);
                ps.setLong(1, this.wurmId);
                ps.setLong(2, aSteamId.getSteamID64());
                ps.setLong(3, now);
                ps.setLong(4, now);
                ps.executeUpdate();
                SteamIdHistory history = new SteamIdHistory(this.wurmId, aSteamId, now, now);
                this.historySteamId.put(aSteamId, history);
            }
            this.steamId = aSteamId;
            DbUtilities.closeDatabaseObjects(ps, null);
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setPassRetrieval(String pwQ, String pwA) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.pwQuestion = pwQ;
            this.pwAnswer = pwA;
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_PASSRETRIEVAL);
                ps.setString(1, this.pwQuestion);
                ps.setString(2, this.pwAnswer);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setReimbursed(boolean reimb) throws IOException {
        if (reimb != this.reimbursed) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.reimbursed = reimb;
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_REIMBURSED);
                    ps.setBoolean(1, this.reimbursed);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setPlantedSign() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.plantedSign = System.currentTimeMillis();
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_PLANTEDSIGN);
                ps.setLong(1, this.plantedSign);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setBanned(boolean ban, String reason, long expiry) throws IOException {
        this.banned = ban;
        this.banexpiry = expiry;
        this.banreason = reason;
        if (this.banned) {
            Village v = Villages.getVillageForCreature(this.wurmId);
            if (v != null && v.getMayor().wurmId == this.wurmId) {
                v.setDemocracy(true);
            }
            this.setRank(1000);
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_BANNED);
                ps.setBoolean(1, this.banned);
                ps.setString(2, this.banreason);
                ps.setLong(3, this.banexpiry);
                ps.setString(4, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setPower(byte pow) throws IOException {
        if (this.power != pow) {
            this.power = pow;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_POWER);
                    ps.setByte(1, this.power);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setPaymentExpire(long paymentExpire) throws IOException {
        this.setPaymentExpire(paymentExpire, true);
    }

    @Override
    public void setPaymentExpire(long paymentExpire, boolean silverReturn) throws IOException {
        int numsPaying = PlayerInfoFactory.getNumberOfPayingPlayers();
        if (this.getPaymentExpire() <= 0L && paymentExpire > System.currentTimeMillis()) {
            if (this.awards == null) {
                this.awards = new Awards(this.wurmId, 0, 0, 0, 0, 0, System.currentTimeMillis(), 0, 0, true);
            }
            if (Servers.localServer.LOGINSERVER) {
                if (silverReturn) {
                    this.setMoney(this.money + 20000L);
                    new MoneyTransfer(this.name, this.wurmId, this.money, 20000L, this.name + "Premium", 3, "", false);
                    logger.log(Level.INFO, "Added 2 silver to " + this.name + " as premium bonus.");
                }
                if (!Servers.isThisATestServer()) {
                    long timeplayed = 0L;
                    timeplayed = this.lastLogin > 0L ? this.playingTime + System.currentTimeMillis() - this.lastLogin : this.playingTime;
                }
            }
        } else if (Features.Feature.RETURNER_PACK_REGISTRATION.isEnabled()) {
            logger.log(Level.INFO, this.getName() + " already prem v2: " + (System.currentTimeMillis() > this.getPaymentExpire()) + ", received return pack: " + this.isFlagSet(47) + ", received gift pack: " + this.isFlagSet(46) + ", on this server: " + (Servers.localServer.id == this.currentServer) + " (on " + this.currentServer + ") Days play time: " + this.getDaysPlayTime());
            if (System.currentTimeMillis() > this.getPaymentExpire() && !this.isFlagSet(47) && !this.isFlagSet(46) && Servers.localServer.id == this.currentServer && this.getPaymentExpire() > 0L && this.getDaysPlayTime() > 14L) {
                logger.log(Level.INFO, this.getName() + " setting to receive returners pack!");
                this.setFlag(47, true);
            }
        }
        if (this.isFlagSet(8) && paymentExpire > this.paymentExpireDate) {
            this.setFlag(8, false);
        }
        this.paymentExpireDate = paymentExpire;
        this.setFlag(63, false);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            try {
                Skills skills = Players.getInstance().getPlayer(this.wurmId).getSkills();
                if (skills != null && !Servers.isThisATestServer()) {
                    skills.paying = true;
                }
            }
            catch (NoSuchPlayerException skills) {
                // empty catch block
            }
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_PAYMENTEXPIRE);
                ps.setLong(1, this.paymentExpireDate);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        if (Servers.localServer.LOGINSERVER && numsPaying < 3000 && PlayerInfoFactory.getNumberOfPayingPlayers() == 3000) {
            logger.log(Level.INFO, this.name + " NUMBER 3000!");
        }
    }

    private long getDaysPlayTime() {
        return this.playingTime / 86400000L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCheated(String reason) {
        if (this.lastCheated == 0L || "CLASS_CHECK_DISCONNECT".equals(reason)) {
            PreparedStatement ps;
            Connection dbcon;
            block6: {
                this.lastCheated = System.currentTimeMillis();
                dbcon = null;
                ps = null;
                try {
                    if (reason.length() > 254) {
                        reason = reason.substring(0, 254);
                    }
                    if (!this.exists(dbcon = DbConnector.getPlayerDbCon())) break block6;
                    ps = dbcon.prepareStatement(SET_CHEATED);
                    ps.setLong(1, this.lastCheated);
                    ps.setString(2, reason);
                    ps.setString(3, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        logger.log(Level.WARNING, this.name + " CHEATED " + reason);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    logger.log(Level.WARNING, this.name + " CHEATED " + reason);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            logger.log(Level.WARNING, this.name + " CHEATED " + reason);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFreeTransfer(boolean freeTrans) {
        this.hasFreeTransfer = freeTrans;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_FREETRANSFER);
            ps.setBoolean(1, this.hasFreeTransfer);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastTaggedTerr(byte newKingdom) {
        if (this.lastTaggedKindom != newKingdom) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                this.lastTaggedKindom = newKingdom;
                this.lastMovedBetweenKingdom = System.currentTimeMillis();
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_ENEMYTERR);
                    ps.setByte(1, this.lastTaggedKindom);
                    ps.setLong(2, this.lastMovedBetweenKingdom);
                    ps.setString(3, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void setRank(int r) throws IOException {
        if (this.rank != Math.max(1000, r)) {
            if (this.realdeath > 0 && r > this.rank) {
                this.setChampionPoints((short)(this.getChampionPoints() + (Math.max(1000, r) - this.rank)));
            }
            if (Servers.localServer.isChallengeServer() && Math.max(1000, r) - this.rank != 0) {
                ChallengeSummary.addToScore(this, ChallengePointEnum.ChallengePoint.BATTLEPOINTS.getEnumtype(), Math.max(1000, r) - this.rank);
                ChallengeSummary.addToScore(this, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), Math.max(1000, r) - this.rank);
            }
            this.rank = Math.max(1000, r);
            if (this.rank > this.maxRank) {
                this.maxRank = this.rank;
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (!this.exists(dbcon)) return;
                this.lastModifiedRank = System.currentTimeMillis();
                ps = dbcon.prepareStatement(SET_RANK);
                ps.setInt(1, this.rank);
                ps.setInt(2, this.maxRank);
                ps.setLong(3, this.lastModifiedRank);
                ps.setString(4, this.name);
                ps.executeUpdate();
                return;
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        } else {
            this.lastModifiedRank = System.currentTimeMillis();
        }
    }

    @Override
    public void warn() throws IOException {
        this.lastWarned = System.currentTimeMillis();
        ++this.warnings;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(WARN);
                ps.setShort(1, (short)this.warnings);
                ps.setLong(2, this.lastWarned);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void resetWarnings() throws IOException {
        this.lastWarned = System.currentTimeMillis();
        this.warnings = 0;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(WARN);
                ps.setShort(1, (short)0);
                ps.setLong(2, 0L);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setFaith(float aNewFaith) throws IOException {
        float lFaith = Math.max(aNewFaith, 0.0f);
        lFaith = Math.min(100.0f, lFaith);
        if (!this.isPriest) {
            lFaith = Math.min(30.0f, lFaith);
        } else if ((lFaith = Math.max(lFaith, 1.0f)) < 30.0f) {
            lFaith = Math.min(lFaith, 20.0f);
            this.setPriest(false);
            try {
                Players.getInstance().getPlayer(this.wurmId).getCommunicator().sendAlertServerMessage(this.deity.name + " no longer accepts you as a priest!", (byte)2);
                Players.getInstance().getPlayer(this.wurmId).clearLinks();
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
        if (this.faith != lFaith) {
            this.faith = lFaith;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_FAITH);
                    ps.setFloat(1, this.faith);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    this.sendReligionStatus(0x7FFFFFFD, this.faith);
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setFavor(float fav) throws IOException {
        float lFav = Math.max(fav, 0.0f);
        lFav = Math.min(100.0f, lFav);
        lFav = Math.min(this.faith, lFav);
        if (!this.isPaying()) {
            lFav = Math.min(20.0f, lFav);
        }
        if (this.favor != lFav) {
            if (this.deity != null && this.deity.number == 2 && this.favor >= 35.0f && lFav < 35.0f || this.favor < 35.0f && lFav >= 35.0f) {
                try {
                    Players.getInstance().getPlayer(this.wurmId).recalcLimitingFactor(null);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
            this.favor = lFav;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_FAVOR);
                    ps.setFloat(1, this.favor);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    this.sendReligionStatus(0x7FFFFFFC, this.favor);
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setDeity(@Nullable Deity d) throws IOException {
        if (this.deity != d) {
            if (this.isPriest) {
                this.setPriest(false);
                try {
                    Players.getInstance().getPlayer(this.wurmId).clearLinks();
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
            this.deity = d;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                byte num = 0;
                if (this.deity != null) {
                    num = (byte)this.deity.number;
                }
                if (this.exists(dbcon = DbConnector.getPlayerDbCon())) {
                    ps = dbcon.prepareStatement(SET_DEITY);
                    ps.setByte(1, num);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
                if (num == 0) {
                    this.setFaith(0.0f);
                    this.setFavor(0.0f);
                }
                this.sendAttitudeChange();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    throw new IOException(sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void transferDeity(@Nullable Deity d) throws IOException {
        if (this.deity != d && this.deity != null) {
            PreparedStatement ps;
            Connection dbcon;
            if (d.isHateGod() != this.deity.isHateGod()) {
                this.alignment *= -1.0f;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (this.exists(dbcon)) {
                        ps = dbcon.prepareStatement(SET_ALIGNMENT);
                        ps.setFloat(1, this.alignment);
                        ps.setString(2, this.name);
                        ps.executeUpdate();
                        DbUtilities.closeDatabaseObjects(ps, null);
                        this.sendReligionStatus(0x7FFFFFFA, this.alignment);
                    }
                }
                catch (SQLException sqex) {
                    logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    throw new IOException(sqex);
                }
                finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            this.deity = d;
            dbcon = null;
            ps = null;
            try {
                byte num = (byte)this.deity.number;
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_DEITY);
                    ps.setByte(1, num);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
                this.sendAttitudeChange();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    throw new IOException(sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setAlignment(float align) throws IOException {
        float lAlign = Math.max(-100.0f, align);
        lAlign = this.deity != null && this.deity.isHateGod() ? Math.min(-1.0f, lAlign) : Math.min(100.0f, lAlign);
        if (this.alignment != lAlign) {
            this.alignment = lAlign;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_ALIGNMENT);
                    ps.setFloat(1, this.alignment);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    DbUtilities.closeDatabaseObjects(ps, null);
                    this.sendReligionStatus(0x7FFFFFFA, this.alignment);
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            if (this.deity != null && !this.deity.isHateGod() && this.realdeath == 0 && !this.deity.accepts(lAlign)) {
                try {
                    Players.getInstance().getPlayer(this.wurmId).getCommunicator().sendNormalServerMessage(this.deity.name + " no longer accepts you as a follower.");
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
                this.setDeity(null);
                this.setChangedDeity();
            }
            if (this.alignment == -100.0f && this.deity != null && this.deity.isHateGod() || this.alignment == 100.0f && this.deity != null && !this.deity.isHateGod()) {
                try {
                    Players.getInstance().getPlayer(this.wurmId).maybeTriggerAchievement(626, true);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    @Override
    public void setGod(Deity g) throws IOException {
        if (this.god != g && this.realdeath == 0) {
            this.god = g;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                byte num = 0;
                if (this.god != null) {
                    num = (byte)this.god.number;
                }
                if (this.exists(dbcon = DbConnector.getPlayerDbCon())) {
                    ps = dbcon.prepareStatement(SET_GOD);
                    ps.setByte(1, num);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setModelName(String newModelName) {
        if (!this.modelName.equals(newModelName)) {
            this.modelName = newModelName;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_MODELNAME);
                ps.setString(1, this.modelName);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " " + sqex.getMessage() + " fail to set modelname to " + this.modelName, sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setChangedJoat() {
        PreparedStatement ps;
        Connection dbcon;
        block4: {
            dbcon = null;
            ps = null;
            try {
                this.lastChangedJoat = System.currentTimeMillis();
                dbcon = DbConnector.getPlayerDbCon();
                if (!this.exists(dbcon)) break block4;
                ps = dbcon.prepareStatement(TOUCH_JOAT);
                ps.setLong(1, this.lastChangedJoat);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " touch joat " + sqex.getMessage(), sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setChangedDeity() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            this.lastChangedDeity = System.currentTimeMillis();
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_CHANGED_DEITY);
                ps.setLong(1, this.lastChangedDeity);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFatigueSecs(int fatigueSecs, long lastReceived) {
        PreparedStatement ps;
        Connection dbcon;
        block4: {
            dbcon = null;
            ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (!this.exists(dbcon)) break block4;
                ps = dbcon.prepareStatement(SET_FATIGUE);
                ps.setInt(1, fatigueSecs);
                ps.setInt(2, this.fatigueSecsToday);
                ps.setLong(3, lastReceived);
                ps.setString(4, this.name);
                ps.executeUpdate();
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void saveSwitchFatigue() {
        if (this.fatigueSecsYesterday > 0 || this.fatigueSecsToday > 0) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                this.fatigueSecsYesterday = this.fatigueSecsToday;
                this.fatigueSecsToday = 0;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SWITCH_FATIGUE);
                    ps.setInt(1, this.fatigueSecsToday);
                    ps.setInt(2, this.fatigueSecsYesterday);
                    ps.setString(3, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDead(boolean isdead) {
        if (this.dead != isdead) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                this.dead = isdead;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_DEAD);
                    ps.setBoolean(1, this.dead);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMuted(boolean mute, String reason, long expiry) {
        block6: {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                if (this.muted == mute) break block6;
                dbcon = null;
                ps = null;
                try {
                    this.muted = mute;
                    this.mutereason = reason;
                    this.muteexpiry = expiry;
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_MUTED);
                    ps.setBoolean(1, this.muted);
                    ps.setString(2, this.mutereason);
                    ps.setLong(3, this.muteexpiry);
                    ps.setString(4, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            if (this.muted) {
                this.setMuteTimes((short)(this.muteTimes + 1));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMayMute(boolean mmute) {
        if (this.mayMute != mmute) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                this.mayMute = mmute;
                if (this.mayMute) {
                    this.addTitle(Titles.Title.CM);
                } else {
                    this.removeTitle(Titles.Title.CM);
                }
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block7;
                    ps = dbcon.prepareStatement(SET_MAYMUTE);
                    ps.setBoolean(1, this.mayMute);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setReferedby(long referer) {
        if (this.referrer != referer) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                dbcon = null;
                ps = null;
                try {
                    this.referrer = referer;
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_REFERRER);
                    ps.setLong(1, this.referrer);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setName(String newname) throws IOException {
        if (!this.name.equals(newname)) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_NAME);
                    ps.setString(1, newname);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                this.name = newname;
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, this.name + " changing to " + newname + " " + sqex.getMessage(), sqex);
                    throw new IOException(sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setVersion(long newversion) {
        if (this.version != newversion) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                this.version = newversion;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_VERSION);
                    ps.setLong(1, this.version);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastTrigger(int newTrigger) {
        if (this.lastTriggerEffect != newTrigger) {
            PreparedStatement ps;
            Connection dbcon;
            block5: {
                this.lastTriggerEffect = newTrigger;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    if (!this.exists(dbcon)) break block5;
                    ps = dbcon.prepareStatement(SET_LASTTRIGGER);
                    ps.setInt(1, this.lastTriggerEffect);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
                catch (SQLException sqex) {
                    try {
                        logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setSessionKey(String key, long expiration) throws IOException {
        this.sessionKey = key;
        this.sessionExpiration = expiration;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_SESSION);
                ps.setString(1, this.sessionKey);
                ps.setLong(2, this.sessionExpiration);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setRealDeath(byte rdcounter) throws IOException {
        if (this.realdeath != rdcounter) {
            this.realdeath = rdcounter;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_REALDEATH);
                    ps.setByte(1, this.realdeath);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setClimbing(boolean climb) throws IOException {
        if (this.climbing != climb) {
            this.climbing = climb;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_CLIMBING);
                    ps.setBoolean(1, this.climbing);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    @Override
    public void setMoney(long newmoney) throws IOException {
        if (newmoney != this.money) {
            this.money = newmoney;
            if (this.money < 0L) {
                this.money = 0L;
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                if (this.exists(dbcon)) {
                    ps = dbcon.prepareStatement(SET_MONEY);
                    ps.setLong(1, this.money);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                }
            }
            catch (SQLException sqex) {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void updatePassword(String newpassword) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            String encp;
            this.password = encp = LoginHandler.hashPassword(newpassword, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(this.name)));
            dbcon = DbConnector.getPlayerDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(SET_PASSWORD);
                ps.setString(1, encp);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
                throw new IOException(sqex);
                catch (Exception ex) {
                    logger.log(Level.INFO, "Failed to encrypt password for player " + this.name, ex);
                    throw new IOException(ex);
                }
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void updateFriend(long wurmid, long friend, byte catId, String note) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(UPDATE_FRIEND);
            ps.setByte(1, catId);
            ps.setString(2, note);
            ps.setLong(3, wurmid);
            ps.setLong(4, friend);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to update friend for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void saveFriend(long wurmid, long friend, byte catId, String note) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(ADD_FRIEND);
            ps.setLong(1, wurmid);
            ps.setLong(2, friend);
            ps.setByte(3, catId);
            ps.setString(4, note);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to add friend for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void deleteFriend(long wurmid, long friend) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(REMOVE_FRIEND);
            ps.setLong(1, wurmid);
            ps.setLong(2, friend);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to remove friend for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void saveEnemy(long wurmid, long enemy) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(ADD_ENEMY);
            ps.setLong(1, wurmid);
            ps.setLong(2, enemy);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to add enemy for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void deleteEnemy(long wurmid, long enemy) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(REMOVE_ENEMY);
            ps.setLong(1, wurmid);
            ps.setLong(2, enemy);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to remove enemy for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void loadFriends(long wurmid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOAD_FRIENDS);
            ps.setLong(1, wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.addFriend(rs.getLong("FRIEND"), rs.getByte("CATEGORY"), rs.getString("NOTE"), true);
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to load friends for  " + wurmid, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        this.setLoadedFriends(true);
    }

    @Override
    public void saveIgnored(long wurmid, long ignored) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(ADD_IGNORED);
            ps.setLong(1, wurmid);
            ps.setLong(2, ignored);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to add ignored for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void deleteIgnored(long wurmid, long ignored) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(REMOVE_IGNORED);
            ps.setLong(1, wurmid);
            ps.setLong(2, ignored);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to remove ignored for  " + wurmid, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void loadIgnored(long wurmid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOAD_IGNORED);
            ps.setLong(1, wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.addIgnored(rs.getLong("IGNOREE"), true);
            }
        }
        catch (SQLException ex) {
            logger.log(Level.INFO, "Failed to load ignored for  " + wurmid, ex);
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        catch (IOException ex2) {
            logger.log(Level.INFO, "Failed to load ignored for  " + wurmid, ex2);
            {
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setNumFaith(byte aNumFaith, long aLastFaith) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        this.numFaith = aNumFaith;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_NUMFAITH);
            ps.setByte(1, aNumFaith);
            ps.setLong(2, aLastFaith);
            ps.setString(3, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to set lastfaith for " + this.name, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    public void setSex(byte newsex) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_SEX);
            ps.setByte(1, newsex);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to set sex for " + this.name, ex);
                throw new IOException(ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPet(long newpet) {
        if (newpet != this.pet) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.pet = newpet;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PET);
                ps.setLong(1, this.pet);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set pet for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setNicotine(float newnicotine) {
        if (newnicotine != this.nicotine) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.nicotine = newnicotine;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_NICOTINE);
                ps.setFloat(1, this.nicotine);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set nicotine for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAlcohol(float newalcohol) {
        float lNewalcohol = Math.max(0.0f, newalcohol);
        if ((lNewalcohol = Math.min(100.0f, lNewalcohol)) != this.alcohol) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.alcohol = lNewalcohol;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_ALCOHOL);
                ps.setFloat(1, this.alcohol);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set alcohol for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setAlcoholTime(long newalcohol) {
        boolean titleAdded = false;
        long lNewalcohol = Math.max(0L, Math.min(10000L, newalcohol));
        if (lNewalcohol != this.alcoholAddiction) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.alcoholAddiction = lNewalcohol;
                if (this.alcoholAddiction >= 10000L) {
                    titleAdded = this.addTitle(Titles.Title.Alcoholic);
                }
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_ALCOHOLTIME);
                ps.setLong(1, this.alcoholAddiction);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set alcoholTime for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        return titleAdded;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setNicotineTime(long newnicotine) {
        if (newnicotine != this.nicotineAddiction) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.nicotineAddiction = newnicotine;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_NICOTINETIME);
                ps.setLong(1, this.nicotineAddiction);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set nicotineTime for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setChangedKingdom(byte changed, boolean setTimeStamp) throws IOException {
        block5: {
            if (this.changedKingdom == changed) break block5;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.changedKingdom = changed;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_NUMCHANGEKINGDOM);
                ps.setByte(1, this.changedKingdom);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.INFO, "Failed to set changedKingdom for " + this.name, ex);
                    throw new IOException(ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            if (setTimeStamp) {
                this.setChangedKingdom();
            }
        }
    }

    @Override
    public void setFace(long _face) throws IOException {
        if (this.face != _face) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.face = _face;
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_FACE);
                ps.setLong(1, this.face);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.INFO, "Failed to set face for " + this.name, ex);
                    throw new IOException(ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setReputation(int rep) {
        int lNewReputation = Math.min(rep, 100);
        if (this.reputation != (lNewReputation = Math.max(lNewReputation, -300))) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.reputation = lNewReputation;
                this.lastPolledReputation = System.currentTimeMillis();
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_REPUTATION);
                ps.setInt(1, this.reputation);
                ps.setLong(2, this.lastPolledReputation);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set reputation for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean addTitle(Titles.Title aTitle) {
        if (!this.titles.contains((Object)aTitle)) {
            boolean bl;
            this.titles.add(aTitle);
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(ADD_TITLE);
                ps.setLong(1, this.wurmId);
                ps.setInt(2, aTitle.id);
                ps.setString(3, aTitle.getName(true));
                ps.executeUpdate();
                bl = true;
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to add title for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return bl;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean removeTitle(Titles.Title aTitle) {
        if (this.titles.contains((Object)aTitle)) {
            boolean bl;
            this.titles.remove((Object)aTitle);
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(REMOVE_TITLE);
                ps.setLong(1, this.wurmId);
                ps.setInt(2, aTitle.id);
                ps.executeUpdate();
                bl = true;
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to remove title for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return bl;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void loadHistorySteamIds(long wurmid) {
        Connection db = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            db = DbConnector.getPlayerDbCon();
            ps = db.prepareStatement(GET_HISTORY_STEAM_IDS);
            ps.setLong(1, wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                SteamIdHistory history = new SteamIdHistory(rs);
                this.historySteamId.put(history.getSteamId(), history);
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to load Steam ID history for " + wurmid, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(db);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(db);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(db);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void loadHistoryIPs(long wurmid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_HISTORY_IPS);
            ps.setLong(1, wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.historyIPStart.put(rs.getString("IPADDRESS"), rs.getLong("FIRSTUSED"));
                this.historyIPLast.put(rs.getString("IPADDRESS"), rs.getLong("LASTUSED"));
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to load IP history for  " + wurmid, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void loadHistoryEmails(long wurmid) {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(GET_HISTORY_EMAIL);
                ps.setLong(1, wurmid);
                rs = ps.executeQuery();
                while (rs.next()) {
                    this.historyEmail.put(rs.getString("EMAIL_ADDRESS"), rs.getLong("DATED"));
                }
                if (!this.historyEmail.isEmpty()) break block5;
                DbUtilities.closeDatabaseObjects(ps, rs);
                long now = System.currentTimeMillis();
                ps = dbcon.prepareStatement(ADD_HISTORY_EMAIL);
                ps.setLong(1, this.wurmId);
                ps.setString(2, this.emailAddress);
                ps.setLong(3, now);
                ps.executeUpdate();
                this.historyEmail.put(this.emailAddress, now);
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.INFO, "Failed to load email history for  " + wurmid, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void loadTitles(long wurmid) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_TITLES);
            ps.setLong(1, wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.titles.add(Titles.Title.getTitle(rs.getInt("TITLEID")));
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.INFO, "Failed to load titles for  " + wurmid, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        if (this.playerAssistant) {
            this.addTitle(Titles.Title.PA);
        } else {
            this.removeTitle(Titles.Title.PA);
        }
        if (this.mayMute) {
            this.addTitle(Titles.Title.CM);
        } else {
            this.removeTitle(Titles.Title.CM);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEmailAddress(String email) {
        if (!this.emailAddress.equals(email)) {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                this.emailAddress = email;
                dbcon = null;
                ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(SET_EMAIL);
                    ps.setString(1, this.emailAddress);
                    ps.setString(2, this.name);
                    ps.executeUpdate();
                    if (!this.historyEmail.containsKey(email)) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        long now = System.currentTimeMillis();
                        ps = dbcon.prepareStatement(ADD_HISTORY_EMAIL);
                        ps.setLong(1, this.wurmId);
                        ps.setString(2, this.emailAddress);
                        ps.setLong(3, now);
                        ps.executeUpdate();
                        this.historyEmail.put(this.emailAddress, now);
                    }
                    if (this.historyEmail.size() <= 5) break block7;
                    long oldest = -1L;
                    String oldEmail = "";
                    for (Map.Entry entry : this.historyEmail.entrySet()) {
                        if (oldest != -1L && (Long)entry.getValue() >= oldest) continue;
                        oldEmail = (String)entry.getKey();
                        oldest = (Long)entry.getValue();
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ps = dbcon.prepareStatement(DELETE_HISTORY_EMAIL);
                    ps.setLong(1, this.wurmId);
                    ps.setString(2, oldEmail);
                    ps.executeUpdate();
                    this.historyEmail.remove(oldEmail);
                }
                catch (SQLException ex) {
                    try {
                        logger.log(Level.WARNING, "Failed to set email for " + this.name, ex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setPriest(boolean priest) {
        if (this.isPriest != priest) {
            Player p;
            this.isPriest = priest;
            if (this.isPriest && (p = Players.getInstance().getPlayerOrNull(this.wurmId)) != null) {
                p.maybeTriggerAchievement(604, true);
            }
            if (Servers.localServer.isChallengeOrEpicServer() && this.realdeath == 0) {
                try {
                    Skills skills = Players.getInstance().getPlayer(this.wurmId).getSkills();
                    if (skills != null) {
                        skills.priest = this.isPriest;
                    }
                }
                catch (NoSuchPlayerException skills) {
                    // empty catch block
                }
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PRIEST);
                ps.setBoolean(1, this.isPriest);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set priest for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBed(long _bed) {
        if (this.bed != _bed) {
            this.bed = _bed;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_BED);
                ps.setLong(1, this.bed);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set bed for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void setSleep(int _sleep) {
        this.setSleep(_sleep, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setSleep(int _sleep, boolean cap5h) {
        int lSleep = (int)Math.min(Math.max((long)this.sleep, cap5h ? 3600L * (long)(this.isFlagSet(77) ? 6 : 5) : 36000L), (long)_sleep);
        if (this.sleep != lSleep) {
            this.sleep = lSleep;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_SLEEP);
                ps.setInt(1, this.sleep);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set sleep for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setOverRideShop(boolean mayUse) {
        if (this.overRideShop != mayUse) {
            this.overRideShop = mayUse;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_MAYUSESHOP);
                ps.setBoolean(1, this.overRideShop);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set use shop for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTheftwarned(boolean warned) {
        if (this.isTheftWarned != warned) {
            this.isTheftWarned = warned;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_THEFTWARNED);
                ps.setBoolean(1, this.isTheftWarned);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set theftwarned for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setHasNoReimbursementLeft(boolean hasReimbursementLeft) {
        if (this.noReimbursementLeft != hasReimbursementLeft) {
            this.noReimbursementLeft = hasReimbursementLeft;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_NOREIMB);
                ps.setBoolean(1, this.noReimbursementLeft);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set noReimbursementLeft for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDeathProtected(boolean _deathProtected) {
        if (this.deathProtected != _deathProtected) {
            this.deathProtected = _deathProtected;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_DEATHPROT);
                ps.setBoolean(1, this.deathProtected);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set deathProtected for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setCurrentServer(int _currentServer) {
        if (this.lastServer != this.currentServer || this.currentServer != _currentServer) {
            this.lastServer = this.currentServer;
            this.currentServer = _currentServer;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_CURRENTSERVER);
                ps.setInt(1, this.lastServer);
                ps.setInt(2, this.currentServer);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set currentServer for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setDevTalk(boolean _devtalk) {
        if (this.mayHearDevTalk != _devtalk) {
            this.mayHearDevTalk = _devtalk;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_DEVTALK);
                ps.setBoolean(1, this.mayHearDevTalk);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set devtalk for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastChangedVillage(long _lastChanged) {
        if (this.lastChangedVillage != _lastChanged) {
            this.lastChangedVillage = _lastChanged;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_LASTCHANGEDVILLAGE);
                ps.setLong(1, this.lastChangedVillage);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set changedvillage for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void saveFightMode(byte _mode) {
        if (this.fightmode != _mode) {
            this.fightmode = _mode;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_FIGHTMODE);
                ps.setByte(1, _mode);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save fightmode for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setNextAffinity(long next) {
        if (this.nextAffinity != next) {
            this.nextAffinity = next;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_NEXTAFFINITY);
                ps.setLong(1, this.nextAffinity);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save next affinity for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setTutorialLevel(int newLevel) {
        if (this.tutorialLevel != newLevel) {
            this.tutorialLevel = newLevel;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_TUTORIAL);
                ps.setInt(1, this.tutorialLevel);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save tutorialLevel for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setAutofight(boolean af) {
        if (this.autoFighting != af) {
            this.autoFighting = af;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_AUTOFIGHT);
                ps.setBoolean(1, this.autoFighting);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set autofight for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void saveAppointments() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_APPOINTMENTS);
            ps.setLong(1, this.appointments);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save appointments for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastVehicle(long _lastvehicle) {
        if (this.lastvehicle != _lastvehicle) {
            this.lastvehicle = _lastvehicle;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_VEHICLE);
                ps.setLong(1, this.lastvehicle);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save last vehicle for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setIsPlayerAssistant(boolean assistant) {
        if (this.playerAssistant != assistant) {
            this.playerAssistant = assistant;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PA);
                ps.setBoolean(1, this.playerAssistant);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save pa for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMayAppointPlayerAssistant(boolean mayAppoint) {
        if (this.mayAppointPlayerAssistant != mayAppoint) {
            this.mayAppointPlayerAssistant = mayAppoint;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_APPOINTPA);
                ps.setBoolean(1, this.mayAppointPlayerAssistant);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save pa for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setHasSkillGain(boolean hasGain) {
        if (this.hasSkillGain != hasGain) {
            this.hasSkillGain = hasGain;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_HASSKILLGAIN);
                ps.setBoolean(1, this.hasSkillGain);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save skillgain for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        return this.hasSkillGain;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setChangedKingdom() {
        this.lastChangedKindom = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_CHANGEDKINGDOM);
            ps.setLong(1, this.lastChangedKindom);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save kingdom stamp for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setChampionTimeStamp() {
        this.championTimeStamp = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_LOSTCHAMPION);
            ps.setLong(1, this.championTimeStamp);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save kingdom stamp for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setChaosKingdom(byte newKingdom) {
        if (this.chaosKingdom != newKingdom) {
            this.chaosKingdom = newKingdom;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_CHAOSKINGDOM);
                ps.setByte(1, this.chaosKingdom);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save chaos kingdom for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setChampChanneling(float channeling) {
        this.champChanneling = channeling;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_CHAMPCHANNELING);
            ps.setFloat(1, this.champChanneling);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save kingdom stamp for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setChampionPoints(short points) {
        block4: {
            if (this.championPoints == (points = (short)Math.max(0, points)) && (this.realdeath <= 0 || points != 0)) break block4;
            this.championPoints = points;
            logger.log(Level.INFO, "Set CHAMPION points of " + this.getName() + " to " + this.championPoints);
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_CHAMPIONPOINTS);
                ps.setShort(1, this.championPoints);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save champion points for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            this.setPointsForChamp();
        }
        return this.championPoints <= 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean togglePlayerAssistantWindow(boolean sees) {
        if (this.seesPlayerAssistantWindow != sees) {
            this.seesPlayerAssistantWindow = sees;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PAWINDOW);
                ps.setBoolean(1, this.seesPlayerAssistantWindow);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save pa for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        return this.seesPlayerAssistantWindow;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setNewPriestType(byte newType, long changeTime) {
        if (this.priestType != newType) {
            this.priestType = newType;
            this.lastChangedPriestType = changeTime;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_PRIESTTYPE);
                ps.setByte(1, this.priestType);
                ps.setLong(2, this.lastChangedPriestType);
                ps.setString(3, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set priest type for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMovedInventory(boolean isMoved) {
        this.hasMovedInventory = isMoved;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_MOVEDINVENTORY);
            ps.setBoolean(1, this.hasMovedInventory);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set moved inventory for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setMuteTimes(short mutetimes) {
        this.muteTimes = mutetimes;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_MUTETIMES);
            ps.setShort(1, this.muteTimes);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set mute times for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setVotedKing(boolean voted) {
        this.votedKing = voted;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_VOTEDKING);
            ps.setBoolean(1, voted);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set voted king for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEpicLocation(byte kingdom, int server) {
        this.epicKingdom = kingdom;
        this.epicServerId = server;
        this.lastUsedEpicPortal = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_EPICLOCATION);
            ps.setByte(1, this.epicKingdom);
            ps.setInt(2, this.epicServerId);
            ps.setString(3, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set epic location for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setHotaWins(short newHotaWins) {
        block4: {
            if (this.hotaWins == newHotaWins) break block4;
            this.hotaWins = newHotaWins;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_HOTA_WINS);
                ps.setShort(1, this.hotaWins);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set hota wins for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            this.checkHotaTitles();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setSpamMode(boolean newSpamMode) {
        if (this.spamMode != newSpamMode) {
            this.spamMode = newSpamMode;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_SPAMMODE);
                ps.setBoolean(1, this.spamMode);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set spamMode for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setKarma(int newKarma) {
        if (this.karma != newKarma) {
            int diff = newKarma - this.karma;
            this.karma = newKarma;
            this.totalKarma += diff;
            if (this.karma > this.maxKarma) {
                this.maxKarma = this.karma;
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_KARMAVALUES);
                ps.setInt(1, this.karma);
                ps.setInt(2, this.maxKarma);
                ps.setInt(3, this.totalKarma);
                ps.setString(4, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set karma values for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    protected void setAbilityBits(long bits) {
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((bits & 1L) == 1L) {
                    this.abilityBits.set(x, true);
                    continue;
                }
                this.abilityBits.set(x, false);
                continue;
            }
            if ((bits >> x & 1L) == 1L) {
                this.abilityBits.set(x, true);
                continue;
            }
            this.abilityBits.set(x, false);
        }
    }

    @Override
    public void setFlagBits(long bits) {
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((bits & 1L) == 1L) {
                    this.flagBits.set(x, true);
                    continue;
                }
                this.flagBits.set(x, false);
                continue;
            }
            if ((bits >> x & 1L) == 1L) {
                this.flagBits.set(x, true);
                continue;
            }
            this.flagBits.set(x, false);
        }
    }

    @Override
    public void setFlag2Bits(long bits) {
        for (int x = 0; x < 64; ++x) {
            if (x == 0) {
                if ((bits & 1L) == 1L) {
                    this.flag2Bits.set(x, true);
                    continue;
                }
                this.flag2Bits.set(x, false);
                continue;
            }
            if ((bits >> x & 1L) == 1L) {
                this.flag2Bits.set(x, true);
                continue;
            }
            this.flag2Bits.set(x, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setBlood(byte newBlood) {
        if (this.blood != newBlood) {
            this.blood = newBlood;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_BLOOD);
                ps.setByte(1, this.blood);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set blood for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public final void setFlag(int number, boolean value) {
        if (number < 64) {
            this.flagBits.set(number, value);
            this.flags = this.getFlagLong();
        } else {
            this.flag2Bits.set(number - 64, value);
            this.flags2 = this.getFlag2Long();
        }
        this.forceFlagsUpdate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forceFlagsUpdate() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_FLAGS);
            ps.setLong(1, this.flags);
            ps.setLong(2, this.flags2);
            ps.setString(3, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save flags for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    @Override
    final long getFlagLong() {
        long ret = 0L;
        for (int x = 0; x < 64; ++x) {
            if (!this.flagBits.get(x)) continue;
            ret += 1L << x;
        }
        return ret;
    }

    @Override
    final long getFlag2Long() {
        long ret = 0L;
        for (int x = 0; x < 64; ++x) {
            if (!this.flag2Bits.get(x)) continue;
            ret += 1L << x;
        }
        return ret;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setAbility(int number, boolean value) {
        this.abilityBits.set(number, value);
        this.abilities = this.getAbilityLong();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_ABILITIES);
            ps.setLong(1, this.abilities);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save abilities for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setCurrentAbilityTitle(int number) {
        this.abilityTitle = number;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_ABILITYTITLE);
            ps.setLong(1, this.abilityTitle);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save abilities for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setScenarioKarma(int newValue) {
        if (newValue != this.scenarioKarma) {
            this.scenarioKarma = newValue;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_SCENARIOKARMA);
                ps.setInt(1, this.scenarioKarma);
                ps.setString(2, this.name);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set scenario karma for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setUndeadData() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_UNDEAD);
            ps.setByte(1, this.undeadType);
            ps.setInt(2, this.undeadKills);
            ps.setInt(3, this.undeadPlayerKills);
            ps.setInt(4, this.undeadPlayerSeconds);
            ps.setString(5, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set scenario karma for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    protected final long getAbilityLong() {
        long ret = 0L;
        for (int x = 0; x < 64; ++x) {
            if (!this.abilityBits.get(x)) continue;
            ret += 1L << x;
        }
        return ret;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addMoneyEarnedBySellingEver(long aMoney) {
        this.moneyEarnedBySellingEver += aMoney;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_MONEYSALES);
            ps.setLong(1, this.moneyEarnedBySellingEver);
            ps.setString(2, this.name);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set moneyEarnedBySellingEver for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final Awards loadAward(long wurmId) {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block4: {
            Awards awards;
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(LOAD_AWARDS);
                ps.setLong(1, wurmId);
                rs = ps.executeQuery();
                if (!rs.next()) break block4;
                awards = new Awards(rs.getLong("WURMID"), rs.getInt("DAYSPREM"), rs.getInt("MONTHSEVER"), rs.getInt("CONSECMONTHS"), rs.getInt("MONTHSPREM"), rs.getInt("SILVERSPURCHASED"), rs.getLong("LASTTICKEDPREM"), rs.getInt("CURRENTLOYALTY"), rs.getInt("TOTALLOYALTY"), false);
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, sqex.getMessage(), sqex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            return awards;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setPointsForChamp() {
        block8: {
            block9: {
                WurmRecord record = PlayerInfoFactory.getChampionRecord(this.name);
                if (record != null) break block9;
                PlayerInfoFactory.addChampRecord(new WurmRecord(this.championPoints, this.name, true));
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getPlayerDbCon();
                    ps = dbcon.prepareStatement(ADD_CHAMPION);
                    ps.setInt(1, this.championPoints);
                    ps.setString(2, this.name);
                    ps.setLong(3, this.wurmId);
                    ps.executeUpdate();
                }
                catch (SQLException ex) {
                    try {
                        logger.log(Level.WARNING, "Failed to set SET_CHAMPION for " + this.name, ex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, null);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    break block8;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                break block8;
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(SET_CHAMPION);
                ps.setInt(1, this.championPoints);
                ps.setString(2, this.name);
                ps.setLong(3, this.wurmId);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to set SET_CHAMPION for " + this.name, ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void switchChamp() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(SET_INACTIVE_CHAMPION);
            ps.setInt(1, this.championPoints);
            ps.setString(2, this.name);
            ps.setLong(3, this.wurmId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to set SET_INACTIVE_CHAMPION for " + this.name, ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }
}

