/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Groups;
import com.wurmonline.server.HistoryEvent;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.DbCitizen;
import com.wurmonline.server.villages.DbGuard;
import com.wurmonline.server.villages.DbVillageRole;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRecruitee;
import com.wurmonline.server.villages.VillageRole;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbVillage
extends Village {
    private static final Logger logger = Logger.getLogger(DbVillage.class.getName());
    private static final String CREATE_VILLAGE = "INSERT INTO VILLAGES (NAME,FOUNDER ,MAYOR ,CREATIONDATE, STARTX ,ENDX,STARTY ,ENDY, DEEDID, SURFACED, DEMOCRACY, DEVISE, HOMESTEAD, TOKEN,LASTLOGIN, KINGDOM,UPKEEP,ACCEPTSHOMESTEADS,PERMANENT,MERCHANTS,SPAWNKINGDOM,PERIMETER,MOTD) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String GET_CITIZENS = "SELECT * FROM CITIZENS WHERE VILLAGEID=?";
    private static final String GET_ROLES = "SELECT * FROM VILLAGEROLE WHERE VILLAGEID=?";
    private static final String SET_MAYOR = "UPDATE VILLAGES SET MAYOR=? WHERE ID=?";
    private static final String SET_DEVISE = "UPDATE VILLAGES SET DEVISE=? WHERE ID=?";
    private static final String SET_STARTX = "UPDATE VILLAGES SET STARTX=? WHERE ID=?";
    private static final String SET_STARTY = "UPDATE VILLAGES SET STARTY=? WHERE ID=?";
    private static final String SET_ENDX = "UPDATE VILLAGES SET ENDX=? WHERE ID=?";
    private static final String SET_ENDY = "UPDATE VILLAGES SET ENDY=? WHERE ID=?";
    private static final String SET_DEEDID = "UPDATE VILLAGES SET DEEDID=? WHERE ID=?";
    private static final String SET_NAME = "UPDATE VILLAGES SET NAME=? WHERE ID=?";
    private static final String SET_DISBAND = "UPDATE VILLAGES SET DISBAND=? WHERE ID=?";
    private static final String SET_DISBANDER = "UPDATE VILLAGES SET DISBANDER=? WHERE ID=?";
    private static final String SET_LASTLOGIN = "UPDATE VILLAGES SET LASTLOGIN=? WHERE ID=?";
    private static final String SET_DEMOCRACY = "UPDATE VILLAGES SET DEMOCRACY=? WHERE ID=?";
    private static final String SET_TOKEN = "UPDATE VILLAGES SET TOKEN=? WHERE ID=?";
    private static final String SET_MAXCITIZENS = "UPDATE VILLAGES SET MAXCITIZENS=? WHERE ID=?";
    private static final String DELETE = "UPDATE VILLAGES SET DISBANDED=1 WHERE ID=?";
    private static final String SET_PERIMETER = "UPDATE VILLAGES SET PERIMETER=? WHERE ID=?";
    private static final String SET_UPKEEP = "UPDATE VILLAGES SET UPKEEP=? WHERE ID=?";
    private static final String SET_MAYPICKUP = "UPDATE VILLAGES SET MAYPICKUP=? WHERE ID=?";
    private static final String SET_UNLIMITEDCITIZENS = "UPDATE VILLAGES SET ACCEPTSHOMESTEADS=? WHERE ID=?";
    private static final String SET_ACCEPTSMERCHANTS = "UPDATE VILLAGES SET MERCHANTS=? WHERE ID=?";
    private static final String SET_ALLOWSAGGRO = "UPDATE VILLAGES SET AGGROS=? WHERE ID=?";
    private static final String GET_REPUTATIONS = "SELECT REPUTATION,PERMANENT, WURMID FROM REPUTATION WHERE VILLAGEID=?";
    private static final String GET_GUARDS = "SELECT * FROM GUARDS WHERE VILLAGEID=?";
    private static final String ADD_HISTORY = "INSERT INTO HISTORY(EVENTDATE,VILLAGEID,PERFORMER,EVENT) VALUES (?,?,?,?)";
    private static final String GET_HISTORY = "SELECT EVENTDATE, VILLAGEID, PERFORMER, EVENT FROM HISTORY WHERE VILLAGEID=? ORDER BY EVENTDATE DESC";
    private static final String SET_KINGDOM = "UPDATE VILLAGES SET KINGDOM=? WHERE ID=?";
    private static final String SET_TWITTER = "UPDATE VILLAGES SET TWITKEY=?,TWITSECRET=?,TWITAPP=?,TWITAPPSECRET=?,TWITCHAT=?,TWITENABLE=? WHERE ID=?";
    private static final String UPDATE_FAITHWAR = "UPDATE VILLAGES SET FAITHWAR=? WHERE ID=?";
    private static final String UPDATE_FAITHHEAL = "UPDATE VILLAGES SET FAITHHEAL=? WHERE ID=?";
    private static final String UPDATE_FAITHCREATE = "UPDATE VILLAGES SET FAITHCREATE=? WHERE ID=?";
    private static final String SET_SPAWNSITUATION = "UPDATE VILLAGES SET SPAWNSITUATION=? WHERE ID=?";
    private static final String SET_ALLIANCENUMBER = "UPDATE VILLAGES SET ALLIANCENUMBER=? WHERE ID=?";
    private static final String SET_HOTAWINS = "UPDATE VILLAGES SET HOTAWINS=? WHERE ID=?";
    private static final String SET_LASTCHANGENAMED = "UPDATE VILLAGES SET NAMECHANGED=? WHERE ID=?";
    private static final String SET_MOTD = "UPDATE VILLAGES SET MOTD=? WHERE ID=?";
    private static final String SET_VILLAGEREP = "UPDATE VILLAGES SET VILLAGEREP=? WHERE ID=?";
    private static final String GET_VILLAGE_MAP_POI = "SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=1 AND OWNERID=?";
    private static final String DELETE_VILLAGE_MAP_POIS = "DELETE FROM MAP_ANNOTATIONS WHERE OWNERID=? AND POITYPE=1;";
    private static final String GET_RECRUITEES = "SELECT RECRUITEEID, RECRUITEENAME FROM VILLAGERECRUITEES WHERE VILLAGEID =?;";
    private static final String INSERT_RECRUITEE = "INSERT INTO VILLAGERECRUITEES(VILLAGEID, RECRUITEEID, RECRUITEENAME) VALUES (?,?,?)";
    private static final String DELETE_RECRUITEE = "DELETE FROM VILLAGERECRUITEES WHERE(VILLAGEID=? AND RECRUITEEID=?);";

    DbVillage(int aStartX, int aEndX, int aStartY, int aEndY, String aName, Creature aFounder, long aDeedid, boolean aSurfaced, boolean aDemocracy, String aDevise, boolean aPermanent, byte aSpawnKingdom, int initialPerimeter) throws NoSuchCreatureException, NoSuchPlayerException, IOException {
        super(aStartX, aEndX, aStartY, aEndY, aName, aFounder, aDeedid, aSurfaced, aDemocracy, aDevise, aPermanent, aSpawnKingdom, initialPerimeter);
    }

    DbVillage(int aId, int aStartX, int aEndX, int aStartY, int aEndY, String aName, String aFounderName, String aMayor, long aDeedid, boolean aSurfaced, boolean aDemocracy, String aDevise, long aCreationDate, boolean aHomestead, long aTokenid, long aDisband, long aDisbander, long aLastlogin, byte aKingdom, long aUpkeep, byte aSettings, boolean aAcceptsHomesteads, boolean aAcceptsMerchants, int aMaxcitiz, boolean aPermanent, byte aSpawnKingdom, int perimetert, boolean aggros, String _consumerKeyToUse, String _consumerSecretToUse, String _applicationToken, String _applicationSecret, boolean _twitChat, boolean _twitEnabled, float _faithWar, float _faithHeal, float _faithCreate, byte _spawnSituation, int _allianceNumber, short _hotaWins, long lastChangeName, String _motd) {
        super(aId, aStartX, aEndX, aStartY, aEndY, aName, aFounderName, aMayor, aDeedid, aSurfaced, aDemocracy, aDevise, aCreationDate, aHomestead, aTokenid, aDisband, aDisbander, aLastlogin, aKingdom, aUpkeep, aSettings, aAcceptsHomesteads, aAcceptsMerchants, aMaxcitiz, aPermanent, aSpawnKingdom, perimetert, aggros, _consumerKeyToUse, _consumerSecretToUse, _applicationToken, _applicationSecret, _twitChat, _twitEnabled, _faithWar, _faithHeal, _faithCreate, _spawnSituation, _allianceNumber, _hotaWins, lastChangeName, _motd);
    }

    @Override
    int create() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_VILLAGE, 1);
            ps.setString(1, this.name);
            ps.setString(2, this.founderName);
            ps.setString(3, this.mayorName);
            ps.setLong(4, this.creationDate);
            ps.setInt(5, this.startx);
            ps.setInt(6, this.endx);
            ps.setInt(7, this.starty);
            ps.setInt(8, this.endy);
            ps.setLong(9, this.deedid);
            ps.setBoolean(10, this.surfaced);
            ps.setBoolean(11, this.democracy);
            ps.setString(12, this.motto);
            ps.setBoolean(13, false);
            ps.setLong(14, this.tokenId);
            ps.setLong(15, this.lastLogin);
            ps.setByte(16, this.kingdom);
            ps.setLong(17, this.upkeep);
            ps.setBoolean(18, this.unlimitedCitizens);
            ps.setBoolean(19, this.isPermanent);
            ps.setBoolean(20, this.acceptsMerchants);
            ps.setByte(21, this.spawnKingdom);
            ps.setInt(22, this.perimeterTiles);
            ps.setString(23, this.motd);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
            }
        }
        catch (SQLException sqx) {
            try {
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return this.id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void loadVillageRecruitees() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_RECRUITEES);
            ps.setInt(1, this.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                long pid = rs.getLong("RECRUITEEID");
                String sName = rs.getString("RECRUITEENAME");
                this.addVillageRecruitee(new VillageRecruitee(this.getId(), pid, sName));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading all village recruitees for village: " + this.getId() + " - " + sqx.getMessage(), sqx);
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
    void deleteRecruitee(VillageRecruitee vr) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_RECRUITEE);
            ps.setInt(1, vr.getVillageId());
            ps.setLong(2, vr.getRecruiteeId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem deleting from recruitees: " + this.getId() + " - " + sqx.getMessage(), sqx);
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
    void loadVillageMapAnnotations() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_VILLAGE_MAP_POI);
            ps.setLong(1, this.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                long wid = rs.getLong("ID");
                String sName = rs.getString("NAME");
                long position = rs.getLong("POSITION");
                byte type = rs.getByte("POITYPE");
                long ownerId = rs.getLong("OWNERID");
                String server = rs.getString("SERVER");
                byte icon = rs.getByte("ICON");
                this.addVillageMapAnnotation(new MapAnnotation(wid, sName, type, position, ownerId, server, icon), false);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading all village POI's for village: " + this.getId() + " - " + sqx.getMessage(), sqx);
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
    void loadCitizens() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_CITIZENS);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                long wurmid = rs.getLong("WURMID");
                int roleid = rs.getInt("ROLEID");
                long votedate = rs.getLong("VOTEDATE");
                if (votedate == 0L) {
                    votedate = -10L;
                }
                long votedfor = rs.getLong("VOTEDFOR");
                DbCitizen citizen = null;
                if (WurmId.getType(wurmid) == 0) {
                    try {
                        String n = Players.getInstance().getNameFor(wurmid);
                        citizen = new DbCitizen(wurmid, n, this.getRole(roleid), votedate, votedfor);
                    }
                    catch (NoSuchPlayerException nsp) {
                        try {
                            Citizen.delete(wurmid);
                        }
                        catch (IOException iox2) {
                            logger.log(Level.WARNING, "Failed to remove citiz " + wurmid, iox2);
                        }
                    }
                    catch (IOException iox) {
                        logger.log(Level.INFO, iox.getMessage(), iox);
                    }
                    catch (NoSuchRoleException nsr) {
                        logger.log(Level.WARNING, nsr.getMessage(), nsr);
                    }
                } else {
                    try {
                        Creature c = Creatures.getInstance().getCreature(wurmid);
                        citizen = new DbCitizen(wurmid, c.getName(), this.getRole(roleid), votedate, votedfor);
                        c.setCitizenVillage(this);
                    }
                    catch (NoSuchCreatureException nsp) {
                        logger.log(Level.INFO, nsp.getMessage(), nsp);
                        try {
                            Citizen.delete(wurmid);
                        }
                        catch (IOException iox2) {
                            logger.log(Level.WARNING, "Failed to remove citiz " + wurmid, iox2);
                        }
                    }
                    catch (NoSuchRoleException nsr) {
                        logger.log(Level.WARNING, nsr.getMessage(), nsr);
                    }
                }
                if (citizen == null) continue;
                this.citizens.put(new Long(citizen.getId()), citizen);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load citizens for village with id " + this.id, sqx);
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

    @Override
    public void setMayor(String aName) throws IOException {
        block6: {
            if (this.mayorName.equals(aName)) break block6;
            this.mayorName = aName;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAYOR);
                ps.setString(1, this.mayorName);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set mayor name for village with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            try {
                this.addHistory(this.mayorName, "new " + this.getRoleForStatus((byte)2).getName());
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.WARNING, this.getName() + " this village doesn't have the correct roles: " + nsr.getMessage(), nsr);
            }
        }
    }

    @Override
    public void setMotto(String devise) throws IOException {
        if (!this.motto.equals(devise)) {
            this.motto = devise;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DEVISE);
                ps.setString(1, devise);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set devise for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setVillageRep(int newRep) {
        block5: {
            if (!Servers.localServer.HOMESERVER || !Servers.localServer.EPIC || newRep == this.villageReputation || newRep > 150) break block5;
            this.villageReputation = newRep;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_VILLAGEREP);
                ps.setInt(1, this.villageReputation);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set reputation for village with id " + this.id, sqx);
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
            boolean mayDeclare = this.villageReputation > 100;
            String toSend = "Settlement reputation now at " + this.villageReputation + (mayDeclare ? "Other settlements may now declare war." : "");
            this.group.sendMessage(this.getRepMessage(toSend));
            if (this.twitChat()) {
                this.twit(toSend);
            }
        }
    }

    @Override
    public void setMotd(String newMotd) throws IOException {
        block6: {
            if (this.motd.equals(newMotd)) break block6;
            this.motd = newMotd;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MOTD);
                ps.setString(1, newMotd);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set motd for village with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            if (newMotd != null && newMotd.length() > 0) {
                this.group.sendMessage(this.getMotdMessage());
                if (this.twitChat()) {
                    this.twit("MOTD:" + this.motd);
                }
            }
        }
    }

    @Override
    void setStartX(int aStartx) throws IOException {
        if (this.startx != aStartx) {
            this.startx = aStartx;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_STARTX);
                ps.setInt(1, aStartx);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set startx for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setName(String aName) throws IOException {
        if (!this.name.equals(aName)) {
            Groups.renameGroup(this.name, aName);
            this.name = aName;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_NAME);
                ps.setString(1, aName);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set name for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    void setEndX(int aEndx) throws IOException {
        if (this.endx != aEndx) {
            this.endx = aEndx;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ENDX);
                ps.setInt(1, aEndx);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set endx for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    void setStartY(int aStarty) throws IOException {
        if (this.starty != aStarty) {
            this.starty = aStarty;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_STARTY);
                ps.setInt(1, aStarty);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set starty for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    void setEndY(int aEndy) throws IOException {
        if (this.endy != aEndy) {
            this.endy = aEndy;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ENDY);
                ps.setInt(1, aEndy);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set endy for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    void setDeedId(long aDeedid) throws IOException {
        if (this.deedid != aDeedid) {
            this.deedid = aDeedid;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DEEDID);
                ps.setLong(1, aDeedid);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set deedid=" + aDeedid + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setDemocracy(boolean aDemocracy) throws IOException {
        block6: {
            if (this.democracy == aDemocracy) break block6;
            this.democracy = aDemocracy;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DEMOCRACY);
                ps.setBoolean(1, aDemocracy);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set democracy=" + aDemocracy + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            if (aDemocracy) {
                this.addHistory(this.getName(), "is now a democracy");
            } else {
                this.addHistory(this.getName(), "is now a dictatorship");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void loadRoles() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_ROLES);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                int extraSettings;
                int moreSettings;
                int settings;
                long playerAppliedTo;
                boolean mayPickLocks;
                boolean mayAttachLock;
                boolean mayButcher;
                boolean mayLoad;
                boolean mayTame;
                boolean mayPickup;
                boolean mayLead;
                boolean mayUpdateMap;
                int villageAppliedTo;
                byte status;
                boolean diplomat;
                boolean mayPushPullTurn;
                boolean mayCutOldTrees;
                boolean mayFish;
                boolean mayAttackNonCitizens;
                boolean mayAttackCitizens;
                boolean mayLockFences;
                boolean mayPassAllFences;
                boolean mayExpand;
                boolean mayManageRoles;
                boolean mayDestroy;
                boolean mayInvite;
                boolean mayHire;
                boolean mayBuild;
                boolean mayFarm;
                boolean mayMine;
                boolean mayCuttrees;
                boolean mayTerraform;
                String roleName;
                int roleid = rs.getInt("ID");
                DbVillageRole role = new DbVillageRole(roleid, this.id, roleName = rs.getString("NAME"), mayTerraform = rs.getBoolean("MAYTERRAFORM"), mayCuttrees = rs.getBoolean("MAYCUTTREE"), mayMine = rs.getBoolean("MAYMINE"), mayFarm = rs.getBoolean("MAYFARM"), mayBuild = rs.getBoolean("MAYBUILD"), mayHire = rs.getBoolean("MAYHIRE"), mayInvite = rs.getBoolean("MAYINVITE"), mayDestroy = rs.getBoolean("MAYDESTROY"), mayManageRoles = rs.getBoolean("MAYMANAGEROLES"), mayExpand = rs.getBoolean("MAYEXPAND"), mayPassAllFences = rs.getBoolean("MAYPASSFENCES"), mayLockFences = rs.getBoolean("MAYLOCKFENCES"), mayAttackCitizens = rs.getBoolean("MAYATTACKCITIZ"), mayAttackNonCitizens = rs.getBoolean("MAYATTACKNONCITIZ"), mayFish = rs.getBoolean("MAYFISH"), mayCutOldTrees = rs.getBoolean("MAYCUTOLD"), mayPushPullTurn = rs.getBoolean("MAYPUSHPULLTURN"), diplomat = rs.getBoolean("DIPLOMAT"), status = rs.getByte("STATUS"), villageAppliedTo = rs.getInt("VILLAGEAPPLIEDTO"), mayUpdateMap = rs.getBoolean("MAYUPDATEMAP"), mayLead = rs.getBoolean("MAYLEAD"), mayPickup = rs.getBoolean("MAYPICKUP"), mayTame = rs.getBoolean("MAYTAME"), mayLoad = rs.getBoolean("MAYLOAD"), mayButcher = rs.getBoolean("MAYBUTCHER"), mayAttachLock = rs.getBoolean("MAYATTACHLOCK"), mayPickLocks = rs.getBoolean("MAYPICKLOCKS"), playerAppliedTo = rs.getLong("PLAYERAPPLIEDTO"), settings = rs.getInt("SETTINGS"), moreSettings = rs.getInt("MORESETTINGS"), extraSettings = rs.getInt("EXTRASETTINGS"));
                if (role.getStatus() == 1 && (role.mayDestroy || role.mayDestroyAnyBuilding())) {
                    logger.warning("Loading RoleID " + this.id + ": mayDestroy/mayDestroyAnyBuilding set on ROLE_EVERYBODY, defaulting to false");
                    try {
                        ((VillageRole)role).setMayDestroy(false);
                        role.setCanDestroyAnyBuilding(false);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                boolean insert = true;
                if (role.getStatus() == 1) {
                    try {
                        if (this.getRoleForStatus((byte)1) != null) {
                            ((VillageRole)role).delete();
                            logger.log(Level.INFO, "Deleted everybody role for " + this.getName());
                            insert = false;
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (!insert) continue;
                this.roles.put(roleid, role);
            }
            try {
                this.everybody = this.getRoleForStatus((byte)1);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, this.getName() + " - role everybody doesn't exist. Creating.");
                this.createRoleEverybody();
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load roles for village with id " + this.id, sqx);
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

    @Override
    public void setTokenId(long tokenid) throws IOException {
        if (this.tokenId != tokenid) {
            this.tokenId = tokenid;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_TOKEN);
                ps.setLong(1, this.tokenId);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set tokenid=" + this.tokenId + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    void setMaxcitizens(int maxcitizens) {
        if (maxcitizens != this.maxCitizens) {
            this.maxCitizens = maxcitizens;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_MAXCITIZENS);
                ps.setInt(1, this.maxCitizens);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set max citizens=" + this.maxCitizens + " for village with id " + this.id, sqx);
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
    public void setDisbandTime(long disbandTime) throws IOException {
        if (this.disband != disbandTime) {
            this.disband = disbandTime;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DISBAND);
                ps.setLong(1, this.disband);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set disbanding=" + disbandTime + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setDisbander(long disb) throws IOException {
        if (this.disbander != disb) {
            this.disbander = disb;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DISBANDER);
                ps.setLong(1, this.disbander);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set disbander=" + this.disbander + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void saveSettings() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_MAYPICKUP);
            ps.setByte(1, (byte)this.settings.getPermissions());
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to seve settings: for village with id " + this.id, sqx);
                throw new IOException(sqx);
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
    public void setPerimeter(int newPerimeter) throws IOException {
        if (this.perimeterTiles != newPerimeter) {
            this.perimeterTiles = newPerimeter;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_PERIMETER);
                ps.setInt(1, this.perimeterTiles);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set perimeter=" + newPerimeter + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setAcceptsMerchants(boolean accepts) throws IOException {
        if (this.acceptsMerchants != accepts) {
            this.acceptsMerchants = accepts;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ACCEPTSMERCHANTS);
                ps.setBoolean(1, this.acceptsMerchants);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set acceptsMerchants=" + this.acceptsMerchants + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setAllowsAggroCreatures(boolean allows) throws IOException {
        if (this.allowsAggCreatures != allows) {
            this.allowsAggCreatures = allows;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ALLOWSAGGRO);
                ps.setBoolean(1, this.allowsAggCreatures);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set acceptsMerchants=" + this.acceptsMerchants + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setUnlimitedCitizens(boolean accepts) throws IOException {
        if (this.unlimitedCitizens != accepts) {
            this.unlimitedCitizens = accepts;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_UNLIMITEDCITIZENS);
                ps.setBoolean(1, this.unlimitedCitizens);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set unlimitedcitizens=" + this.unlimitedCitizens + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setLogin() {
        this.lastLogin = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_LASTLOGIN);
            ps.setLong(1, this.lastLogin);
            ps.setInt(2, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set last login=now for village with id " + this.id, sqx);
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
        if (!this.isCitizen(this.disbander)) {
            this.stopDisbanding();
        }
    }

    @Override
    void delete() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE);
            ps.setInt(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete village with id=" + this.id, sqx);
                throw new IOException(sqx);
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
    void deleteVillageMapAnnotations() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_VILLAGE_MAP_POIS);
            ps.setLong(1, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete village map annotations with id=" + this.id, sqx);
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
    public void save() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void loadReputations() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_REPUTATIONS);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                long wurmId = rs.getLong("WURMID");
                byte reputation = rs.getByte("REPUTATION");
                boolean perma = rs.getBoolean("PERMANENT");
                Reputation rep = new Reputation(wurmId, this.id, perma, reputation, false, true);
                this.reputations.put(new Long(wurmId), rep);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load reputations for village with id " + this.id, sqx);
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

    @Override
    void setUpkeep(long aUpk) throws IOException {
        long upk = Math.max(0L, aUpk);
        if (upk != this.upkeep) {
            this.upkeep = upk;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_UPKEEP);
                ps.setLong(1, this.upkeep);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set upkeep=" + this.upkeep + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void loadGuards() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_GUARDS);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                long wurmId = rs.getLong("WURMID");
                long expireDate = rs.getLong("EXPIREDATE");
                try {
                    Creature creature = Creatures.getInstance().getCreature(wurmId);
                    DbGuard guard = new DbGuard(this.id, creature, expireDate);
                    creature.setCitizenVillage(this);
                    this.guards.put(new Long(wurmId), guard);
                }
                catch (NoSuchCreatureException nsc) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    ps = dbcon.prepareStatement("DELETE FROM GUARDS WHERE WURMID=?");
                    ps.setLong(1, wurmId);
                    ps.executeUpdate();
                    logger.log(Level.WARNING, "Deleted guard with id " + wurmId + ". These messages should disappear over time since 040707.");
                }
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load guards for village with id " + this.id, sqx);
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
    final void loadHistory() {
        this.history = new LinkedList();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_HISTORY);
            ps.setInt(1, this.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                this.history.add(new HistoryEvent(rs.getLong("EVENTDATE"), rs.getString("PERFORMER"), rs.getString("EVENT"), this.id));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load history for village with id " + this.id, sqx);
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
    public final void addHistory(String performerName, String event) {
        HistoryEvent h = new HistoryEvent(System.currentTimeMillis(), performerName, event, this.id);
        this.history.addFirst(h);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(ADD_HISTORY);
            ps.setLong(1, h.time);
            ps.setInt(2, h.identifier);
            ps.setString(3, h.performer);
            ps.setString(4, h.event);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    void saveRecruitee(VillageRecruitee vr) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(INSERT_RECRUITEE);
            ps.setInt(1, vr.getVillageId());
            ps.setLong(2, vr.getRecruiteeId());
            ps.setString(3, vr.getRecruiteeName());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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
    public void setKingdom(byte newkingdom) throws IOException {
        this.setKingdom(newkingdom, true);
    }

    @Override
    public void setKingdom(byte newkingdom, boolean updateTimeStamp) throws IOException {
        if (this.kingdom != newkingdom) {
            this.kingdom = newkingdom;
            Kingdom k = Kingdoms.getKingdom(this.kingdom);
            if (k != null) {
                k.setExistsHere(true);
            }
            this.convertOfflineCitizensToKingdom(newkingdom, updateTimeStamp);
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_KINGDOM);
                ps.setByte(1, this.kingdom);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set kingdom=" + newkingdom + " for village with id " + this.id, sqx);
                    throw new IOException(sqx);
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
    public void setFaithCreate(float newFaith) {
        if (this.faithCreate != newFaith) {
            this.faithCreate = newFaith;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_FAITHCREATE);
                ps.setFloat(1, this.faithCreate);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set faithCreate=" + this.faithCreate + " for village with id " + this.id, sqx);
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
    public void setFaithWar(float newFaith) {
        if (this.faithWar != newFaith) {
            this.faithWar = newFaith;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_FAITHWAR);
                ps.setFloat(1, this.faithWar);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set faithCreate=" + this.faithWar + " for village with id " + this.id, sqx);
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
    public void setFaithHeal(float newFaith) {
        if (this.faithHeal != newFaith) {
            this.faithHeal = newFaith;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_FAITHHEAL);
                ps.setFloat(1, this.faithHeal);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set faithHeal=" + this.faithHeal + " for village with id " + this.id, sqx);
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
    public final void setTwitCredentials(String _consumerKeyToUse, String _consumerSecretToUse, String _applicationToken, String _applicationSecret, boolean _twitChat, boolean enabled) {
        this.consumerKeyToUse = _consumerKeyToUse;
        this.consumerSecretToUse = _consumerSecretToUse;
        this.applicationToken = _applicationToken;
        this.applicationSecret = _applicationSecret;
        this.twitChat = _twitChat;
        this.twitEnabled = enabled;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_TWITTER);
            ps.setString(1, this.consumerKeyToUse);
            ps.setString(2, this.consumerSecretToUse);
            ps.setString(3, this.applicationToken);
            ps.setString(4, this.applicationSecret);
            ps.setBoolean(5, this.twitChat);
            ps.setBoolean(6, this.twitEnabled);
            ps.setInt(7, this.id);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set twitter info for village with id " + this.id, sqx);
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
        this.canTwit();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public final void setHotaWins(short newWins) {
        if (this.hotaWins != newWins) {
            this.hotaWins = newWins;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_HOTAWINS);
                ps.setShort(1, this.hotaWins);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set hotaWins=" + newWins + " for village with id " + this.id, sqx);
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
    public final void setLastChangedName(long newDate) {
        if (this.lastChangedName != newDate) {
            this.lastChangedName = newDate;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_LASTCHANGENAMED);
                ps.setLong(1, this.lastChangedName);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set lastChangedName=" + newDate + " for village with id " + this.id, sqx);
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
    public final void setSpawnSituation(byte newSpawnSituation) {
        if (this.spawnSituation != newSpawnSituation) {
            this.spawnSituation = newSpawnSituation;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_SPAWNSITUATION);
                ps.setByte(1, this.spawnSituation);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set spawnSituation=" + this.spawnSituation + " for village with id " + this.id, sqx);
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
    public final void setAllianceNumber(int newAllianceNumber) {
        block7: {
            if (this.allianceNumber == newAllianceNumber) break block7;
            this.allianceNumber = newAllianceNumber;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ALLIANCENUMBER);
                ps.setInt(1, this.allianceNumber);
                ps.setInt(2, this.id);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set allianceNumber=" + this.allianceNumber + " for village with id " + this.id, sqx);
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
            if (this.allianceNumber > 0) {
                try {
                    this.getRoleForStatus((byte)5);
                }
                catch (NoSuchRoleException nsr) {
                    this.createRoleAlly();
                }
            }
        }
    }
}

