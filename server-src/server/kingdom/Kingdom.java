/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.kingdom;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.KingdomBuff;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.webinterface.WcExpelMember;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Kingdom
implements MiscConstants,
TimeConstants {
    private static final Logger logger = Logger.getLogger(Kingdom.class.getName());
    final byte kingdomId;
    private final byte template;
    private final String name;
    private final String chatname;
    private final String suffix;
    private final String password;
    private String firstMotto = "";
    private String secondMotto = "";
    private boolean existsHere = false;
    private boolean shouldBeDeleted = false;
    private final byte red;
    private final byte blue;
    private final byte green;
    public static final byte ALLIANCE_TYPE_NONE = 0;
    public static final byte ALLIANCE_TYPE_ALLIANCE = 1;
    public static final byte ALLIANCE_TYPE_SENT_REQUEST = 2;
    private static int winPoints = 0;
    private long startedDisbandWarning = 0L;
    public int activePremiums = 0;
    public boolean countedAtleastOnce = false;
    private boolean acceptsTransfers = true;
    private static final String LOAD_ALL_KINGDOMS = "SELECT * FROM KINGDOMS";
    private static final String LOAD_ALLIANCES = "SELECT * FROM KALLIANCES";
    private static final String INSERT_KINGDOM = "INSERT INTO KINGDOMS (KINGDOM, KINGDOMNAME,PASSWORD, TEMPLATE, SUFFIX, CHATNAME, FIRSTMOTTO,SECONDMOTTO,ACCEPTSTRANSFERS) VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_KINGDOM = "UPDATE KINGDOMS SET KINGDOMNAME=?, PASSWORD=?,TEMPLATE=?, SUFFIX=?, CHATNAME=?, FIRSTMOTTO=?,SECONDMOTTO=?,ACCEPTSTRANSFERS=? WHERE KINGDOM=?";
    private static final String INSERT_ALLIANCE = "INSERT INTO KALLIANCES (ALLIANCETYPE,KINGDOMONE, KINGDOMTWO) VALUES (?,?,?)";
    private static final String UPDATE_ALLIANCE = "UPDATE KALLIANCES SET ALLIANCETYPE=? WHERE KINGDOMONE=? AND KINGDOMTWO=?";
    private static final String DELETE_ALLIANCE = "DELETE FROM KALLIANCES WHERE KINGDOMONE=? AND KINGDOMTWO=?";
    private static final String DELETE_ALL_ALLIANCE = "DELETE FROM KALLIANCES WHERE KINGDOMONE=? OR KINGDOMTWO=?";
    private static final String SET_ERA_NONE = "UPDATE KING_ERA SET KINGDOM=0 WHERE KINGDOM=?";
    private static final String SET_WINPOINTS = "UPDATE KINGDOMS SET WINPOINTS=? WHERE KINGDOM=?";
    private static final String GET_MEMBERS = "SELECT WURMID FROM PLAYERS WHERE KINGDOM=?";
    private static final Random colorRand = new Random();
    private Map<Byte, Byte> alliances = new HashMap<Byte, Byte>();
    private List<Long> members = new LinkedList<Long>();
    private ArrayList<KingdomBuff> kingdomBuffs = new ArrayList();
    public int lastConfrontationTileX;
    public int lastConfrontationTileY;
    private long lastMemberLoad = 0L;

    public Kingdom(byte id, byte templateKingdom, String _name, String _password, String _chatName, String _suffix, String mottoOne, String mottoTwo, boolean acceptsPortals) {
        this.kingdomId = id;
        this.template = templateKingdom;
        this.name = _name;
        this.password = _password;
        this.chatname = _chatName;
        this.suffix = _suffix;
        this.firstMotto = mottoOne;
        this.secondMotto = mottoTwo;
        this.acceptsTransfers = acceptsPortals;
        colorRand.setSeed(this.name.hashCode());
        this.red = (byte)colorRand.nextInt(255);
        this.blue = (byte)colorRand.nextInt(255);
        this.green = (byte)colorRand.nextInt(255);
        this.loadAllMembers();
    }

    public byte getId() {
        return this.kingdomId;
    }

    public byte getTemplate() {
        return this.template;
    }

    public Map<Byte, Byte> getAllianceMap() {
        return this.alliances;
    }

    void setAlliances(Map<Byte, Byte> newAlliances) {
        this.alliances = newAlliances;
    }

    public boolean existsHere() {
        return this.existsHere;
    }

    public void setExistsHere(boolean exists) {
        if (this.existsHere != exists) {
            Servers.loginServer.shouldResendKingdoms = true;
        }
        this.existsHere = exists;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getChatName() {
        return this.chatname;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setAcceptsTransfers(boolean accepts) {
        if (!this.isCustomKingdom()) {
            return;
        }
        this.acceptsTransfers = accepts;
    }

    public boolean acceptsTransfers() {
        return this.acceptsTransfers;
    }

    public void setFirstMotto(String motto) {
        this.firstMotto = motto;
    }

    public void setSecondMotto(String motto) {
        this.secondMotto = motto;
    }

    public String getFirstMotto() {
        return this.firstMotto;
    }

    public String getSecondMotto() {
        return this.secondMotto;
    }

    boolean isShouldBeDeleted() {
        return this.shouldBeDeleted;
    }

    void setShouldBeDeleted(boolean aShouldBeDeleted) {
        this.shouldBeDeleted = aShouldBeDeleted;
    }

    public byte getColorRed() {
        return this.red;
    }

    public byte getColorBlue() {
        return this.blue;
    }

    public byte getColorGreen() {
        return this.green;
    }

    public boolean isCustomKingdom() {
        return this.kingdomId < 0 || this.kingdomId > 4;
    }

    public void disband() {
        PlayerInfo[] pinfs;
        byte newKingdomId;
        Kingdom[] kingdomArr;
        Kingdoms.destroyTowersWithKingdom(this.kingdomId);
        for (Kingdom k2 : kingdomArr = Kingdoms.getAllKingdoms()) {
            if (k2.getId() == this.getId()) continue;
            k2.removeKingdomFromAllianceMap(this.getId());
        }
        King k = King.getKing(this.kingdomId);
        byte by = this.getTemplate() == 3 ? (byte)3 : (newKingdomId = Servers.localServer.isChallengeOrEpicServer() ? (byte)this.getTemplate() : (byte)4);
        if (k != null) {
            k.abdicate(true, true);
            Players.getInstance().convertFromKingdomToKingdom(this.kingdomId, newKingdomId);
        }
        this.existsHere = false;
        for (PlayerInfo pinf : pinfs = PlayerInfoFactory.getPlayerInfos()) {
            if (pinf.epicKingdom == this.kingdomId) {
                pinf.setEpicLocation(newKingdomId, pinf.epicServerId);
            }
            if (pinf.getChaosKingdom() != this.kingdomId) continue;
            pinf.setChaosKingdom(newKingdomId);
        }
        this.delete();
    }

    public boolean isAllied(byte otherKingdom) {
        Byte b = this.alliances.get(otherKingdom);
        if (b == null) {
            return false;
        }
        return b == 1;
    }

    public boolean hasSentRequestingAlliance(byte otherKingdom) {
        Byte b = this.alliances.get(otherKingdom);
        if (b == null) {
            return false;
        }
        return b == 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setAlliance(byte kingdId, byte allianceType) {
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                if (allianceType == 0) {
                    this.alliances.remove(kingdId);
                    ps = dbcon.prepareStatement(DELETE_ALLIANCE);
                    ps.setByte(1, this.kingdomId);
                    ps.setByte(2, kingdId);
                    ps.executeUpdate();
                    break block5;
                }
                ps = this.alliances.containsKey(kingdId) ? dbcon.prepareStatement(UPDATE_ALLIANCE) : dbcon.prepareStatement(INSERT_ALLIANCE);
                ps.setByte(1, allianceType);
                ps.setByte(2, this.kingdomId);
                ps.setByte(3, kingdId);
                ps.executeUpdate();
                this.alliances.put(kingdId, allianceType);
            }
            catch (SQLException sqex) {
                try {
                    logger.log(Level.WARNING, "Failed to load kingdom: " + sqex.getMessage(), sqex);
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

    void removeKingdomFromAllianceMap(byte kingdom) {
        this.alliances.remove(kingdom);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllKingdoms() {
        logger.log(Level.INFO, "Loading all kingdoms.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_ALL_KINGDOMS);
            rs = ps.executeQuery();
            while (rs.next()) {
                Kingdom k = new Kingdom(rs.getByte("KINGDOM"), rs.getByte("TEMPLATE"), rs.getString("KINGDOMNAME"), rs.getString("PASSWORD"), rs.getString("CHATNAME"), rs.getString("SUFFIX"), rs.getString("FIRSTMOTTO"), rs.getString("SECONDMOTTO"), rs.getBoolean("ACCEPTSTRANSFERS"));
                k.setWinpoints(rs.getInt("WINPOINTS"));
                Kingdoms.loadKingdom(k);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load kingdom: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded kingdoms from database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded kingdoms from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded kingdoms from database took " + (float)(end - start) / 1000000.0f + " ms");
        Kingdom.loadAlliances();
        if (Kingdoms.numKingdoms() == 0) {
            Kingdoms.createBasicKingdoms();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void loadAllMembers() {
        if (System.currentTimeMillis() - this.lastMemberLoad < 900000L) {
            return;
        }
        this.lastMemberLoad = System.currentTimeMillis();
        if (!Servers.localServer.PVPSERVER || this.getId() == 4) {
            return;
        }
        logger.log(Level.INFO, "Loading all members for " + this.getName() + ".");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_MEMBERS);
            ps.setByte(1, this.kingdomId);
            rs = ps.executeQuery();
            while (rs.next()) {
                long wurmId = rs.getLong("WURMID");
                if (wurmId == -10L) continue;
                this.addMember(wurmId);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load kingdom members: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + this.members.size() + " kingdom members from database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + this.members.size() + " kingdom members from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + this.members.size() + " kingdom members from database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    final void loadAlliance(byte otherKingdom, byte allianceType) {
        logger.log(Level.INFO, "Alliance between " + this.getId() + " and " + otherKingdom + ":" + allianceType);
        this.alliances.put(otherKingdom, allianceType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void loadAlliances() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_ALLIANCES);
            rs = ps.executeQuery();
            while (rs.next()) {
                Kingdom k = Kingdoms.getKingdom(rs.getByte("KINGDOMONE"));
                if (k == null) continue;
                k.loadAlliance(rs.getByte("KINGDOMTWO"), rs.getByte("ALLIANCETYPE"));
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load alliances: " + sqex.getMessage(), sqex);
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
    public void delete() {
        logger.log(Level.INFO, "Deleting " + this.kingdomId + ", " + this.name);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_ALL_ALLIANCE);
            ps.setByte(1, this.kingdomId);
            ps.setByte(2, this.kingdomId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete all alliances for " + this.kingdomId + ", " + this.name + " : " + sqex.getMessage(), sqex);
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
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_ERA_NONE);
            ps.setByte(1, this.kingdomId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Failed to update king era set to none for " + this.kingdomId + ", " + this.name + " : " + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        King.setToNoKingdom(this.kingdomId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updatePointsDB() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_WINPOINTS);
            ps.setInt(1, winPoints);
            ps.setByte(2, this.kingdomId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update king era set to none for " + this.kingdomId + ", " + this.name + " : " + sqex.getMessage(), sqex);
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
    public void update() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(UPDATE_KINGDOM);
            ps.setString(1, this.name);
            ps.setString(2, this.password);
            ps.setByte(3, this.template);
            ps.setString(4, this.suffix);
            ps.setString(5, this.chatname);
            ps.setString(6, this.firstMotto);
            ps.setString(7, this.secondMotto);
            ps.setBoolean(8, this.acceptsTransfers);
            ps.setByte(9, this.kingdomId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete kingdom " + this.kingdomId + ", " + this.name + " : " + sqex.getMessage(), sqex);
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
    void saveToDisk() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            logger.log(Level.INFO, "Saving " + this.name + " id=" + this.kingdomId);
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(INSERT_KINGDOM);
            ps.setByte(1, this.kingdomId);
            ps.setString(2, this.name);
            ps.setString(3, this.password);
            ps.setByte(4, this.template);
            ps.setString(5, this.suffix);
            ps.setString(6, this.chatname);
            ps.setString(7, this.firstMotto);
            ps.setString(8, this.secondMotto);
            ps.setBoolean(9, this.acceptsTransfers);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save kingdom " + this.kingdomId + ", " + this.name + " : " + sqex.getMessage(), sqex);
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

    public void sendDisbandTick() {
        if (this.getStartedDisbandWarning() == 0L) {
            this.setStartedDisbandWarning(System.currentTimeMillis());
        }
        logger.log(Level.INFO, "The appointments of " + this.getName() + " does not work because of low population.");
    }

    public int getWinpoints() {
        return winPoints;
    }

    public void setWinpoints(int newpoints) {
        winPoints = newpoints;
        this.updatePointsDB();
    }

    public void addWinpoints(int pointsAdded) {
        winPoints += pointsAdded;
        this.updatePointsDB();
    }

    public void addMember(long wurmId) {
        PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
        if (p == null) {
            return;
        }
        if (p.getPower() != 0) {
            return;
        }
        if (this.members.contains(new Long(wurmId))) {
            return;
        }
        this.members.add(wurmId);
    }

    public void removeMember(long wurmId) {
        if (!this.members.contains(new Long(wurmId))) {
            return;
        }
        this.members.remove(new Long(wurmId));
    }

    public final PlayerInfo getMember(long wurmId) {
        if (!this.members.contains(new Long(wurmId))) {
            return null;
        }
        return PlayerInfoFactory.getPlayerInfoWithWurmId(wurmId);
    }

    public final PlayerInfo[] getAllMembers() {
        if (this.members.size() == 0) {
            logger.log(Level.WARNING, "No members to return for kingdom id " + this.getId() + "!");
            return new PlayerInfo[0];
        }
        LinkedList<PlayerInfo> m = new LinkedList<PlayerInfo>();
        for (long w : this.members) {
            PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(w);
            if (p != null) {
                m.add(p);
                continue;
            }
            logger.log(Level.WARNING, w + " returns null player info!");
        }
        return m.toArray(new PlayerInfo[m.size()]);
    }

    public void expelMember(Creature performer, String ostra) {
        boolean isOnline = true;
        Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(ostra));
        byte by = Servers.localServer.EPIC || this.getTemplate() == 3 ? this.getTemplate() : 4;
        PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithName(LoginHandler.raiseFirstLetter(ostra));
        if (pInfo == null) {
            performer.getCommunicator().sendNormalServerMessage("That player does not exist.", (byte)3);
            return;
        }
        if (pInfo.realdeath > 0) {
            performer.getCommunicator().sendNormalServerMessage("You cannot expel a champion of your kingdom.", (byte)3);
            return;
        }
        if (p == null) {
            PlayerState ps = PlayerInfoFactory.getPlayerState(pInfo.wurmId);
            if (ps == null || ps.getServerId() != Servers.localServer.getId()) {
                WcExpelMember wcx = new WcExpelMember(pInfo.wurmId, this.getId(), by, Servers.localServer.getId());
                if (!Servers.isThisLoginServer()) {
                    wcx.sendToLoginServer();
                } else {
                    wcx.sendFromLoginServer();
                }
            }
            isOnline = false;
            try {
                pInfo.load();
                p = new Player(pInfo);
            }
            catch (Exception ex) {
                performer.getCommunicator().sendNormalServerMessage("Failed to load '" + ostra + "' to expel, please /support.", (byte)3);
                ex.printStackTrace();
                return;
            }
        }
        if (p.getWurmId() == performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot expel yourself!", (byte)3);
            return;
        }
        if (p.getKingdomId() != this.getId()) {
            performer.getCommunicator().sendNormalServerMessage("Only " + p.getName() + "'s king may expel them.", (byte)3);
            return;
        }
        Village village = p.getCitizenVillage();
        if (village != null && village.isMayor(p)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot expel " + p.getName() + " as they are mayor of " + p.getVillageName() + ".", (byte)3);
            return;
        }
        try {
            if (!p.setKingdomId(by, false, false, isOnline)) {
                performer.getCommunicator().sendNormalServerMessage("Unable to expel " + p.getName() + ", please /support.", (byte)3);
                return;
            }
            if (isOnline) {
                p.getCommunicator().sendAlertServerMessage("You have been expelled from " + this.getName() + "!");
                p.getCommunicator().sendAlertServerMessage("You better leave the kingdom immediately!");
            }
        }
        catch (IOException iox) {
            performer.getCommunicator().sendNormalServerMessage("Failed to expel '" + p.getName() + "', please /support.", (byte)3);
            iox.printStackTrace();
            return;
        }
        performer.getCommunicator().sendSafeServerMessage("You successfully expel " + p.getName() + ". Let the dog run!");
        Message mess = new Message(performer, 10, this.getChatName(), "<" + performer.getName() + "> expelled " + p.getName());
        Server.getInstance().addMessage(mess);
    }

    public int getPremiumMemberCount() {
        this.activePremiums = 0;
        this.members.forEach(w -> {
            PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(w);
            if (p != null && p.isPaying()) {
                ++this.activePremiums;
            }
        });
        return this.activePremiums;
    }

    public long getStartedDisbandWarning() {
        return this.startedDisbandWarning;
    }

    public void setStartedDisbandWarning(long startedDisbandWarning) {
        this.startedDisbandWarning = startedDisbandWarning;
    }
}

