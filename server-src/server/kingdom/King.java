/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.kingdom;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.Zones;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class King
implements MiscConstants,
TimeConstants {
    private static final String CREATE_KING_ERA = "insert into KING_ERA ( ERA,KINGDOM,KINGDOMNAME, KINGID,KINGSNAME,GENDER,STARTTIME,STARTWURMTIME,STARTLANDPERCENT, CURRENTLANDPERCENT,      NEXTCHALLENGE,CURRENT) VALUES (?,?,?,?,?,?,?,?,?,?,  ?,1)";
    private static final String UPDATE_KING_ERA = "UPDATE KING_ERA SET KINGSNAME=?,GENDER=?,ENDTIME=?,ENDWURMTIME=?, CURRENTLANDPERCENT=?, CAPITAL=?, CURRENT=?,KINGDOM=? WHERE ERA=?";
    private static final String UPDATE_LEVELSKILLED = "UPDATE KING_ERA SET LEVELSKILLED=? WHERE ERA=?";
    private static final String UPDATE_LEVELSLOST = "UPDATE KING_ERA SET LEVELSLOST=? WHERE ERA=?";
    private static final String UPDATE_APPOINTMENTS = "UPDATE KING_ERA SET APPOINTMENTS=? WHERE ERA=?";
    private static final String GET_ALL_KING_ERA = "select * FROM KING_ERA";
    private static final String UPDATE_CHALLENGES = "UPDATE KING_ERA SET NEXTCHALLENGE=?,DECLINEDCHALLENGES=?,ACCEPTDATE=?,CHALLENGEDATE=? WHERE ERA=?";
    public String kingdomName = "unknown kingdom";
    private static Logger logger = Logger.getLogger(King.class.getName());
    public static int currentEra = 0;
    public int era = 0;
    public String kingName = "";
    public long kingid = -10L;
    private long startTime = 0L;
    private long endTime = 0L;
    public long startWurmTime = 0L;
    public long endWurmTime = 0L;
    public float startLand = 0.0f;
    public float currentLand = 0.0f;
    public int appointed = 0;
    public int levelskilled = 0;
    public int levelslost = 0;
    public boolean current = false;
    public byte kingdom = 0;
    private long nextChallenge = 0L;
    private int declinedChallenges = 0;
    private long challengeDate = 0L;
    private long acceptDate = 0L;
    public byte gender = 0;
    public String capital = "";
    private String rulerMaleTitle = "Grand Prince";
    private String rulerFemaleTitle = "Grand Princess";
    private static King kingJenn = null;
    private static King kingMolRehan = null;
    private static King kingHots = null;
    private Appointments appointments = null;
    public static final Map<Integer, King> eras = new HashMap<Integer, King>();
    public static final Map<Long, Integer> challenges = new HashMap<Long, Integer>();
    private static final int challengesRequired = Servers.isThisATestServer() ? 3 : 10;
    private static final int votesRequired = Servers.isThisATestServer() ? 1 : 10;
    private static final Set<King> kings = new HashSet<King>();
    private static final long challengeFactor = Servers.isThisATestServer() ? 60000L : 604800000L;
    public static final float landPercentRequiredForBonus = 2.0f;
    long lastCapital = System.currentTimeMillis();

    private King() {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Creating new King");
        }
    }

    private static void addKing(King king) {
        eras.put(king.era, king);
        logger.log(Level.INFO, "Loading kings, adding " + king.kingName);
        if (king.current) {
            if (king.kingdom == 1) {
                logger.log(Level.INFO, "Setting current jenn king: " + king.kingName);
                kingJenn = king;
            } else if (king.kingdom == 2) {
                logger.log(Level.INFO, "Setting current mol rehan king: " + king.kingName);
                kingMolRehan = king;
            } else if (king.kingdom == 3) {
                logger.log(Level.INFO, "Setting current hots king: " + king.kingName);
                kingHots = king;
            }
            kings.add(king);
        }
    }

    public static King getKing(byte _kingdom) {
        if (_kingdom == 1) {
            return kingJenn;
        }
        if (_kingdom == 2) {
            return kingMolRehan;
        }
        if (_kingdom == 3) {
            return kingHots;
        }
        for (King k : kings) {
            if (k.kingdom != _kingdom || !k.current) continue;
            return k;
        }
        return null;
    }

    public static boolean isKing(long wurmid, byte kingdom) {
        King k = King.getKing(kingdom);
        if (k != null) {
            return k.kingid == wurmid;
        }
        return false;
    }

    public static void purgeKing(byte _kingdom) {
        Zones.calculateZones(true);
        if (_kingdom == 1) {
            if (kingJenn != null) {
                King.kingJenn.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingJenn);
            }
            kingJenn = null;
            new Appointments(-1, 1, true);
        } else if (_kingdom == 2) {
            if (kingMolRehan != null) {
                King.kingMolRehan.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingMolRehan);
            }
            kingMolRehan = null;
            new Appointments(-2, 2, true);
        } else if (_kingdom == 3) {
            if (kingHots != null) {
                King.kingHots.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingHots);
            }
            kingHots = null;
            new Appointments(-3, 3, true);
        } else {
            King[] kingarr;
            for (King k : kingarr = King.getKings()) {
                if (k.kingdom != _kingdom) continue;
                k.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(k);
            }
        }
    }

    public static void pollKings() {
        King[] kingarr;
        for (King k : kingarr = King.getKings()) {
            k.poll();
        }
    }

    public static final King[] getKings() {
        return kings.toArray(new King[kings.size()]);
    }

    private void poll() {
        if (System.currentTimeMillis() - this.appointments.lastChecked > 604800000L) {
            PlayerInfo pinf;
            this.appointments.resetAppointments(this.kingdom);
            Kingdom k = Kingdoms.getKingdom(this.kingdom);
            if (k.isCustomKingdom() && (pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.kingid)) != null && System.currentTimeMillis() - pinf.lastLogout > 2419200000L && System.currentTimeMillis() - pinf.lastLogin > 2419200000L) {
                Items.deleteRoyalItemForKingdom(this.kingdom, true, false);
                logger.log(Level.INFO, this.kingName + " has not logged in for a month. A new king for " + this.kingdomName + " will be found.");
                King.purgeKing(this.kingdom);
            }
        } else {
            Kingdom k;
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.kingid);
            if (pinf != null && pinf.currentServer == Servers.localServer.id && !pinf.isPaying() && !(k = Kingdoms.getKingdom(this.kingdom)).isCustomKingdom()) {
                Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
                logger.log(Level.INFO, this.kingName + " is no longer premium. Deleted the regalia.");
                King.purgeKing(this.kingdom);
                return;
            }
            Zones.calculateZones(false);
            float oldland = this.currentLand;
            this.currentLand = Zones.getPercentLandForKingdom(this.kingdom);
            if (oldland != this.currentLand) {
                logger.log(Level.INFO, "Saving " + this.kingName + " because new land is " + this.currentLand + " compared to " + oldland);
                this.save();
            }
            if (this.hasFailedToRespondToChallenge()) {
                HistoryManager.addHistory(this.kingName, "decided not to respond to a challenge.");
                Server.getInstance().broadCastAlert(this.kingName + " has decided not to respond to a challenge.");
                logger.log(Level.INFO, this.kingName + " did not respond to a challenge.");
                this.setChallengeDeclined();
                if (this.hasFailedAllChallenges()) {
                    HistoryManager.addHistory(this.kingName, "may now be voted away from the throne within one week at the duelling stone.");
                    Server.getInstance().broadCastAlert(this.getFullTitle() + " may now be voted away from the throne within one week at the duelling stone.");
                    logger.log(Level.INFO, this.kingName + " may now be voted away.");
                }
            }
            if (this.hasFailedAllChallenges()) {
                if (this.getVotesNeeded() == 0) {
                    this.removeByVote();
                } else if (this.getNextChallenge() < System.currentTimeMillis()) {
                    PlayerInfoFactory.resetVotesForKingdom(this.kingdom);
                    this.declinedChallenges = 0;
                    this.updateChallenges();
                    HistoryManager.addHistory(this.kingName, "was not voted away from the throne this time. The " + this.getRulerTitle() + " remains on the throne of " + this.kingdomName + ".");
                    Server.getInstance().broadCastNormal(this.kingName + " was not voted away from the throne this time. The " + this.getRulerTitle() + " remains on the throne of " + this.kingdomName + ".");
                    logger.log(Level.INFO, this.kingName + " may no longer be voted away.");
                }
            }
            if (this.acceptDate > 0L && System.currentTimeMillis() > this.acceptDate) {
                try {
                    Player p = Players.getInstance().getPlayer(this.kingid);
                    if (p.isInOwnDuelRing()) {
                        if (Servers.isThisATestServer()) {
                            if (System.currentTimeMillis() - this.getChallengeAcceptedDate() > 300000L) {
                                this.passedChallenge();
                            }
                        } else if (System.currentTimeMillis() - this.acceptDate > 1800000L) {
                            this.passedChallenge();
                        }
                        p.getCommunicator().sendAlertServerMessage("Unseen eyes watch you.");
                    } else {
                        this.setFailedChallenge();
                    }
                }
                catch (NoSuchPlayerException nsp) {
                    this.setFailedChallenge();
                }
            }
        }
    }

    public final void removeByVote() {
        HistoryManager.addHistory(this.kingName, "has been voted away from the throne by the people of " + this.kingdomName + "!");
        Server.getInstance().broadCastAlert(this.getFullTitle() + " has been voted away from the throne by the people of " + this.kingdomName + "!");
        Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
        King.purgeKing(this.kingdom);
        logger.log(Level.INFO, this.kingName + " has been voted away.");
    }

    public final void removeByFailChallenge() {
        HistoryManager.addHistory(this.kingName, "has failed the challenge by the people of " + this.kingdomName + "!");
        Server.getInstance().broadCastNormal(this.getFullTitle() + " has failed the challenge by the people of " + this.kingdomName + "!");
        Items.deleteRoyalItemForKingdom(this.kingdom, true, true);
        King.purgeKing(this.kingdom);
        logger.log(Level.INFO, this.kingName + " has failed the challenge.");
    }

    private static void setRulerName(King king) {
        king.rulerMaleTitle = King.getRulerTitle(true, king.kingdom);
        king.rulerFemaleTitle = King.getRulerTitle(false, king.kingdom);
    }

    public String getRulerTitle() {
        if (this.gender == 1) {
            return this.rulerFemaleTitle;
        }
        return this.rulerMaleTitle;
    }

    public static String getRulerTitle(boolean male, byte kingdom) {
        if (kingdom == 1) {
            if (male) {
                return "Grand Prince";
            }
            return "Grand Princess";
        }
        if (kingdom == 2) {
            if (male) {
                return "Chancellor";
            }
            return "Chancellor";
        }
        if (kingdom == 3) {
            if (male) {
                return "Emperor";
            }
            return "Empress";
        }
        if (male) {
            return "Chief";
        }
        return "Chieftain";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAllEra() {
        logger.log(Level.INFO, "Loading all kingdom eras.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_ALL_KING_ERA);
            rs = ps.executeQuery();
            while (rs.next()) {
                King k = new King();
                k.era = rs.getInt("ERA");
                k.kingdom = rs.getByte("KINGDOM");
                k.current = rs.getBoolean("CURRENT");
                if (k.era > currentEra) {
                    currentEra = k.era;
                }
                k.kingName = rs.getString("KINGSNAME");
                k.gender = rs.getByte("GENDER");
                k.startLand = rs.getFloat("STARTLANDPERCENT");
                k.startTime = rs.getLong("STARTTIME");
                k.endTime = rs.getLong("ENDTIME");
                k.startWurmTime = rs.getLong("STARTWURMTIME");
                k.endWurmTime = rs.getLong("ENDWURMTIME");
                k.currentLand = rs.getFloat("CURRENTLANDPERCENT");
                k.appointed = rs.getInt("APPOINTMENTS");
                k.levelskilled = rs.getInt("LEVELSKILLED");
                k.levelslost = rs.getInt("LEVELSLOST");
                k.capital = rs.getString("CAPITAL");
                k.kingid = rs.getLong("KINGID");
                k.appointed = rs.getInt("APPOINTMENTS");
                k.nextChallenge = rs.getLong("NEXTCHALLENGE");
                k.declinedChallenges = rs.getInt("DECLINEDCHALLENGES");
                k.acceptDate = rs.getLong("ACCEPTDATE");
                k.challengeDate = rs.getLong("CHALLENGEDATE");
                k.kingdomName = rs.getString("KINGDOMNAME");
                byte template = k.kingdom;
                Kingdom kingd = Kingdoms.getKingdom(k.kingdom);
                if (kingd != null) {
                    template = kingd.getTemplate();
                    logger.log(Level.INFO, "Template for " + k.kingdom + "=" + template + " (" + kingd.getId() + ")");
                }
                k.appointments = new Appointments(k.era, template, k.current);
                King.setRulerName(k);
                King.addKing(k);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load kingdom eras: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded kingdom eras from database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded kingdom eras from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded kingdom eras from database took " + (float)(end - start) / 1000000.0f + " ms");
        if (Appointments.jenn == null) {
            new Appointments(-1, 1, true);
        }
        if (Appointments.hots == null) {
            new Appointments(-3, 3, true);
        }
        if (Appointments.molr == null) {
            new Appointments(-2, 2, true);
        }
        if (Appointments.none == null) {
            new Appointments(-5, 0, true);
        }
    }

    public static void setToNoKingdom(byte oldKingdom) {
        for (King k : eras.values()) {
            if (k.kingdom != oldKingdom) continue;
            k.kingdom = 0;
            k.save();
        }
        for (King k : kings) {
            if (k.kingdom != oldKingdom) continue;
            k.kingdom = 0;
            k.save();
        }
    }

    public static Appointments getCurrentAppointments(byte kingdom) {
        King k = King.getKing(kingdom);
        if (k != null && k.current) {
            return Appointments.getAppointments(k.era);
        }
        Kingdom kingd = Kingdoms.getKingdom(kingdom);
        if (kingd != null) {
            return Appointments.getCurrentAppointments(kingd.getTemplate());
        }
        return null;
    }

    public void abdicate(boolean isOnSurface, boolean destroyItems) {
        Items.deleteRoyalItemForKingdom(this.kingdom, isOnSurface, destroyItems);
        King.purgeKing(this.kingdom);
    }

    public static King createKing(byte _kingdom, String kingname, long kingwurmid, byte kinggender) {
        King k = new King();
        k.era = ++currentEra;
        k.kingdom = _kingdom;
        k.kingid = kingwurmid;
        k.kingName = kingname;
        k.gender = kinggender;
        k.startTime = System.currentTimeMillis();
        k.startWurmTime = WurmCalendar.currentTime;
        k.nextChallenge = System.currentTimeMillis() + challengeFactor;
        k.kingdomName = Kingdoms.getNameFor(_kingdom);
        Zones.calculateZones(true);
        k.startLand = Zones.getPercentLandForKingdom(_kingdom);
        boolean foundCapital = false;
        try {
            Player p = Players.getInstance().getPlayer(kingwurmid);
            p.achievement(321);
            if (p.getCitizenVillage() != null) {
                foundCapital = true;
                k.setCapital(p.getCitizenVillage().getName(), true);
            }
        }
        catch (NoSuchPlayerException p) {
            // empty catch block
        }
        if (_kingdom == 1) {
            if (kingJenn != null) {
                King.kingJenn.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingJenn);
            }
            kingJenn = k;
        } else if (_kingdom == 2) {
            if (kingMolRehan != null) {
                King.kingMolRehan.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingMolRehan);
            }
            kingMolRehan = k;
        } else if (_kingdom == 3) {
            if (kingHots != null) {
                King.kingHots.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                King.switchCurrent(kingHots);
            }
            kingHots = k;
        } else {
            King oldKing = King.getKing(_kingdom);
            if (oldKing != null) {
                oldKing.currentLand = Zones.getPercentLandForKingdom(_kingdom);
                logger.log(Level.INFO, "Found old king " + oldKing.kingName + " when creating new.");
                King.switchCurrent(oldKing);
                if (!foundCapital) {
                    k.setCapital(oldKing.capital, true);
                }
            }
        }
        k.currentLand = k.startLand;
        k.current = true;
        k.create();
        byte template = k.kingdom;
        Kingdom kingd = Kingdoms.getKingdomOrNull(k.kingdom);
        if (kingd != null) {
            template = kingd.getTemplate();
            logger.log(Level.INFO, "Using " + Kingdoms.getNameFor(template) + " for " + kingd.getName());
        }
        k.appointments = new Appointments(k.era, template, k.current);
        King.setRulerName(k);
        King.addKing(k);
        HistoryManager.addHistory(k.kingName, "is appointed new " + k.getRulerTitle() + " of " + k.kingdomName);
        Items.transferRegaliaForKingdom(_kingdom, kingwurmid);
        King.pollKings();
        return k;
    }

    private static void switchCurrent(King oldking) {
        oldking.endTime = System.currentTimeMillis();
        oldking.endWurmTime = WurmCalendar.currentTime;
        oldking.current = false;
        HistoryManager.addHistory(oldking.kingName, "no longer is the " + oldking.getRulerTitle() + " of " + oldking.kingdomName);
        Server.getInstance().broadCastNormal(oldking.kingName + " no longer is the " + oldking.getRulerTitle() + " of " + oldking.kingdomName);
        oldking.save();
        kings.remove(oldking);
        PlayerInfoFactory.resetVotesForKingdom(oldking.kingdom);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_KING_ERA);
            ps.setInt(1, this.era);
            ps.setByte(2, this.kingdom);
            ps.setString(3, this.kingdomName);
            ps.setLong(4, this.kingid);
            ps.setString(5, this.kingName);
            ps.setByte(6, this.gender);
            ps.setLong(7, this.startTime);
            ps.setLong(8, this.startWurmTime);
            ps.setFloat(9, this.startLand);
            ps.setFloat(10, this.currentLand);
            ps.setLong(11, this.nextChallenge);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to create kingdom for era " + this.era + sqex.getMessage(), sqex);
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
    private final void save() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_KING_ERA);
            ps.setString(1, this.kingName);
            ps.setByte(2, this.gender);
            ps.setLong(3, this.endTime);
            ps.setLong(4, this.endWurmTime);
            ps.setFloat(5, this.currentLand);
            ps.setString(6, this.capital);
            ps.setBoolean(7, this.current);
            ps.setByte(8, this.kingdom);
            ps.setInt(9, this.era);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save kingdom for era " + this.era + sqex.getMessage(), sqex);
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

    public final boolean setCapital(String newcapital, boolean forced) {
        if (System.currentTimeMillis() - this.lastCapital > 21600000L || forced || Servers.isThisATestServer()) {
            this.capital = newcapital;
            this.lastCapital = System.currentTimeMillis();
            this.save();
            return true;
        }
        return false;
    }

    public final void setGender(byte newgender) {
        this.gender = newgender;
        this.save();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void addAppointment(Appointment app) {
        this.appointed += app.getLevel();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_APPOINTMENTS);
            ps.setInt(1, this.appointed);
            ps.setInt(2, this.era);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update appointed: " + this.appointed + " for era " + this.era + sqex.getMessage(), sqex);
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

    public final void resetNextChallenge(long nextTime) {
        this.nextChallenge = nextTime;
        challenges.clear();
        this.updateChallenges();
    }

    public final long getNextChallenge() {
        return this.nextChallenge;
    }

    public final void setChallengeDate() {
        this.challengeDate = System.currentTimeMillis();
        this.updateChallenges();
    }

    public final long getChallengeDate() {
        return this.challengeDate;
    }

    public final void setChallengeAccepted(long date) {
        this.acceptDate = date;
        this.challengeDate = 0L;
        this.resetNextChallenge(this.acceptDate + challengeFactor * (long)(3 - this.declinedChallenges));
        this.updateChallenges();
    }

    public final void setChallengeDeclined() {
        this.resetNextChallenge(System.currentTimeMillis() + challengeFactor);
        this.challengeDate = 0L;
        ++this.declinedChallenges;
        this.updateChallenges();
    }

    public final long getChallengeAcceptedDate() {
        return this.acceptDate;
    }

    public final int getDeclinedChallengesNumber() {
        return this.declinedChallenges;
    }

    public final void passedChallenge() {
        HistoryManager.addHistory(this.kingName, "passed the challenge put forth by the people of " + this.kingdomName + "!");
        Server.getInstance().broadCastNormal(this.getFullTitle() + " passed the challenge put forth by the people of " + this.kingdomName + "!");
        this.acceptDate = 0L;
        this.challengeDate = 0L;
        this.updateChallenges();
    }

    public final void setFailedChallenge() {
        if (!this.hasFailedAllChallenges()) {
            HistoryManager.addHistory(this.kingName, "failed the challenge put forth by the people of " + this.kingdomName + " and may now be voted away from the throne.");
            Message mess = new Message(null, 10, Kingdoms.getChatNameFor(this.kingdom), "<" + this.kingName + "> has failed the challenge and may now be voted away from the throne.");
            Player[] playarr = Players.getInstance().getPlayers();
            byte windowKingdom = this.kingdom;
            for (Player lElement : playarr) {
                if (windowKingdom != lElement.getKingdomId() && lElement.getPower() <= 0) continue;
                lElement.getCommunicator().sendMessage(mess);
            }
            this.resetNextChallenge(System.currentTimeMillis() + challengeFactor);
            this.acceptDate = 0L;
            this.challengeDate = 0L;
            this.declinedChallenges = 3;
            this.updateChallenges();
        }
    }

    public final boolean mayBeChallenged() {
        return System.currentTimeMillis() - this.challengeDate > challengeFactor && System.currentTimeMillis() > this.getNextChallenge();
    }

    public final boolean hasFailedToRespondToChallenge() {
        return this.challengeDate != 0L && System.currentTimeMillis() - this.challengeDate > challengeFactor;
    }

    public final boolean hasFailedAllChallenges() {
        return this.declinedChallenges >= 3;
    }

    public final int getVotes() {
        return PlayerInfoFactory.getVotesForKingdom(this.kingdom);
    }

    public final int getVotesNeeded() {
        return Math.max(0, votesRequired - this.getVotes());
    }

    public final boolean hasBeenChallenged() {
        int challengesCast = 0;
        for (Integer i : challenges.values()) {
            if (i != this.era) continue;
            ++challengesCast;
        }
        return challengesCast >= challengesRequired;
    }

    public final boolean addChallenge(Creature challenger) {
        if (challenger.getKingdomId() == this.kingdom) {
            if (Servers.isThisATestServer()) {
                boolean wasChallenged = this.hasBeenChallenged();
                challenges.put(Server.rand.nextLong(), this.era);
                if (this.hasBeenChallenged() != wasChallenged) {
                    this.setChallengeDate();
                }
                return true;
            }
            if (challenges.containsKey(challenger.getWurmId())) {
                return false;
            }
            boolean wasChallenged = this.hasBeenChallenged();
            challenges.put(challenger.getWurmId(), this.era);
            if (this.hasBeenChallenged() != wasChallenged) {
                this.setChallengeDate();
            }
            return true;
        }
        return false;
    }

    public final int getChallengeSize() {
        return challenges.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateChallenges() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_CHALLENGES);
            ps.setLong(1, this.nextChallenge);
            ps.setLong(2, this.declinedChallenges);
            ps.setLong(3, this.acceptDate);
            ps.setLong(4, this.challengeDate);
            ps.setInt(5, this.era);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update challenges: for era " + this.era + sqex.getMessage(), sqex);
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
    public void addLevelsLost(int lost) {
        this.levelslost += lost;
        logger.log(Level.INFO, this.kingName + " adding " + lost + " levels lost to " + this.levelslost + " for kingdom " + Kingdoms.getChatNameFor(this.kingdom) + " era " + this.era);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_LEVELSLOST);
            ps.setInt(1, this.levelslost);
            ps.setInt(2, this.era);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update for era " + this.era + sqex.getMessage(), sqex);
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
    public void addLevelsKilled(int killed, String name, int worth) {
        this.levelskilled += killed;
        logger.log(Level.INFO, this.kingName + " killed " + name + " worth " + worth + " adding " + killed + " levels killed to " + this.levelskilled + " for kingdom " + Kingdoms.getChatNameFor(this.kingdom) + " era " + this.era);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_LEVELSKILLED);
            ps.setInt(1, this.levelskilled);
            ps.setInt(2, this.era);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update for era " + this.era + sqex.getMessage(), sqex);
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

    public float getLandSuccessPercent() {
        if (this.startLand == 0.0f) {
            this.startLand = this.currentLand;
        }
        if (this.startLand == 0.0f) {
            return 100.0f;
        }
        return this.currentLand / this.startLand * 100.0f;
    }

    public float getAppointedSuccessPercent() {
        if (this.levelskilled == 0 && this.levelslost == 0) {
            return 100.0f;
        }
        if (this.levelslost < 20 && this.levelskilled < 20) {
            return 100.0f;
        }
        if (this.levelslost == 0 && this.levelskilled != 0) {
            return 100 + this.levelskilled;
        }
        if (this.levelslost != 0 && this.levelskilled == 0) {
            return 100 - this.levelslost;
        }
        return (float)this.levelskilled / (float)this.levelslost * 100.0f;
    }

    private String getSuccessTitle() {
        float successPercentSinceStart = this.getLandSuccessPercent();
        if (successPercentSinceStart < 100.0f) {
            if (successPercentSinceStart < 10.0f) {
                return "the Traitor";
            }
            if (successPercentSinceStart < 20.0f) {
                return "the Tragic";
            }
            if (successPercentSinceStart < 30.0f) {
                return "the Joke";
            }
            if (successPercentSinceStart < 50.0f) {
                return "the Imbecile";
            }
            if (successPercentSinceStart < 70.0f) {
                return "the Failed";
            }
            if (successPercentSinceStart < 90.0f) {
                return "the Stupid";
            }
            return "the Acceptable";
        }
        if (successPercentSinceStart < 110.0f) {
            return "the Acceptable";
        }
        if (successPercentSinceStart < 120.0f) {
            return "the Lucky";
        }
        if (successPercentSinceStart < 130.0f) {
            return "the Conquering";
        }
        if (successPercentSinceStart < 140.0f) {
            return "the Strong";
        }
        if (successPercentSinceStart < 150.0f) {
            return "the Impressive";
        }
        if (successPercentSinceStart < 180.0f) {
            return "the Great";
        }
        if (successPercentSinceStart < 200.0f) {
            return "the Fantastic";
        }
        if (successPercentSinceStart < 400.0f) {
            return "the Magnificent";
        }
        return "the Divine";
    }

    private String getAppointmentSuccess() {
        float successPercentSinceStart = this.getAppointedSuccessPercent();
        if (successPercentSinceStart < 110.0f) {
            return "";
        }
        if (successPercentSinceStart < 120.0f) {
            return "";
        }
        if (successPercentSinceStart < 150.0f) {
            return " Warrior";
        }
        if (successPercentSinceStart < 180.0f) {
            return " Defender";
        }
        if (successPercentSinceStart < 200.0f) {
            return " Statesman";
        }
        if (successPercentSinceStart < 400.0f) {
            return " Saviour";
        }
        return " Holiness";
    }

    public String getFullTitle() {
        return this.getRulerTitle() + " " + this.kingName + " " + this.getSuccessTitle() + this.getAppointmentSuccess();
    }

    public static boolean isOfficial(int officeId, long wurmid, byte kingdom) {
        King tempKing = King.getKing(kingdom);
        if (tempKing != null && tempKing.appointments != null) {
            return tempKing.appointments.officials[officeId - 1500] == wurmid;
        }
        return false;
    }

    public static Creature getOfficial(byte _kingdom, int officeId) {
        King tempKing = King.getKing(_kingdom);
        if (tempKing != null && tempKing.appointments != null) {
            long wurmid = tempKing.appointments.officials[officeId - 1500];
            try {
                Player p = Players.getInstance().getPlayer(wurmid);
                return p;
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
        return null;
    }
}

