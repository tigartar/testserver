/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.MapAnnotation;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.AllianceWar;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class PvPAlliance {
    private static final Logger logger = Logger.getLogger(PvPAlliance.class.getName());
    private static final String LOAD_ENTRIES = "SELECT * FROM PVPALLIANCE";
    private static final String CREATE_ENTRY = "INSERT INTO PVPALLIANCE (ALLIANCENUMBER,NAME) VALUES(?,?)";
    private static final String DELETE_ENTRY = "DELETE FROM PVPALLIANCE WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_NAME = "UPDATE PVPALLIANCE SET NAME=? WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_WINS = "UPDATE PVPALLIANCE SET WINS=? WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_DEITYONE = "UPDATE PVPALLIANCE SET DEITYONE=? WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_DEITYTWO = "UPDATE PVPALLIANCE SET DEITYTWO=? WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_MOTD = "UPDATE PVPALLIANCE SET MOTD=? WHERE ALLIANCENUMBER=?";
    private static final String UPDATE_ALLIANCENUMBER = "UPDATE PVPALLIANCE SET ALLIANCENUMBER=? WHERE ALLIANCENUMBER=?";
    private static final String GET_ALLIANCE_MAP_POI = "SELECT * FROM MAP_ANNOTATIONS WHERE POITYPE=2 AND OWNERID=?";
    private static final String DELETE_ALLIANCE_MAP_POIS = "DELETE FROM MAP_ANNOTATIONS WHERE OWNERID=? AND POITYPE=2;";
    private String name = "unknown";
    private int idNumber = 0;
    private int wins = 0;
    private byte deityOneId = 0;
    private byte deityTwoId = 0;
    private static final ConcurrentHashMap<Integer, PvPAlliance> alliances = new ConcurrentHashMap();
    private final CopyOnWriteArraySet<AllianceWar> wars = new CopyOnWriteArraySet();
    private static final PvPAlliance[] emptyAlliances = new PvPAlliance[0];
    private static final AllianceWar[] emptyWars = new AllianceWar[0];
    private String motd = "";
    private final Set<MapAnnotation> mapAnnotations = new HashSet<MapAnnotation>();

    public PvPAlliance(int id, String allianceName) {
        this.idNumber = id;
        this.name = allianceName.substring(0, Math.min(allianceName.length(), 20));
        this.create();
        PvPAlliance.addAlliance(this);
    }

    public final void addHotaWin() {
        this.setWins(this.wins + 1);
        for (Village vill : this.getVillages()) {
            vill.addHotaWin();
            if (vill.getAllianceNumber() != vill.getId()) continue;
            vill.createHotaPrize(this.getNumberOfWins());
        }
    }

    public void addAllianceWar(AllianceWar toadd) {
        this.wars.add(toadd);
    }

    public final void removeWar(AllianceWar war) {
        this.wars.remove(war);
    }

    public final AllianceWar getWarWith(int allianceNum) {
        for (AllianceWar war : this.wars) {
            if (war.getAggressor() != allianceNum && war.getDefender() != allianceNum) continue;
            return war;
        }
        return null;
    }

    public final AllianceWar[] getWars() {
        if (this.wars.isEmpty()) {
            return emptyWars;
        }
        return this.wars.toArray(new AllianceWar[this.wars.size()]);
    }

    public static final PvPAlliance[] getAllAlliances() {
        if (alliances.size() > 0) {
            return alliances.values().toArray(new PvPAlliance[alliances.size()]);
        }
        return emptyAlliances;
    }

    public final boolean isAtWarWith(int allianceNum) {
        if (allianceNum > 0) {
            for (AllianceWar war : this.wars) {
                if (war.getAggressor() != allianceNum && war.getDefender() != allianceNum) continue;
                return war.isActive();
            }
        }
        return false;
    }

    public PvPAlliance(int id, String allianceName, byte deityOne, byte deityTwo, int _wins, String _motd) {
        this.idNumber = id;
        this.name = allianceName.substring(0, Math.min(allianceName.length(), 20));
        this.deityOneId = deityOne;
        this.deityTwoId = deityTwo;
        this.wins = _wins;
        this.motd = _motd;
    }

    private static final void addAlliance(PvPAlliance alliance) {
        alliances.put(alliance.getId(), alliance);
    }

    public static final PvPAlliance getPvPAlliance(int allianceId) {
        return alliances.get(allianceId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadPvPAlliances() {
        logger.log(Level.INFO, "Loading all PVP Alliances.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_ENTRIES);
            rs = ps.executeQuery();
            while (rs.next()) {
                int allianceId = rs.getInt("ALLIANCENUMBER");
                String name = rs.getString("NAME");
                byte deityOne = rs.getByte("DEITYONE");
                byte deityTwo = rs.getByte("DEITYTWO");
                int wins = rs.getInt("WINS");
                String motd = rs.getString("MOTD");
                PvPAlliance toAdd = new PvPAlliance(allianceId, name, deityOne, deityTwo, wins, motd);
                toAdd.loadAllianceMapAnnotations();
                PvPAlliance.addAlliance(toAdd);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load pvp alliances " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded PVP Alliances from database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded PVP Alliances from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded PVP Alliances from database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void loadAllianceMapAnnotations() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_ALLIANCE_MAP_POI);
            ps.setLong(1, this.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                long wid = rs.getLong("ID");
                String poiName = rs.getString("NAME");
                long position = rs.getLong("POSITION");
                byte type = rs.getByte("POITYPE");
                long ownerId = rs.getLong("OWNERID");
                String server = rs.getString("SERVER");
                byte icon = rs.getByte("ICON");
                this.addAllianceMapAnnotation(new MapAnnotation(wid, poiName, type, position, ownerId, server, icon), false);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading all alliance POI's for alliance nr: " + this.getId() + " - " + sqx.getMessage(), sqx);
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

    public int getId() {
        return this.idNumber;
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfWins() {
        return this.wins;
    }

    public byte getFavoredDeityOne() {
        return this.deityOneId;
    }

    public byte getFavoredDeityTwo() {
        return this.deityTwoId;
    }

    public String getMotd() {
        return this.motd;
    }

    public final Message getMotdMessage() {
        return new Message(null, 15, "Alliance", "MOTD: " + this.motd, 250, 150, 250);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private final void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_ENTRY);
            ps.setInt(1, this.idNumber);
            ps.setString(2, this.name);
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

    public final boolean exists() {
        for (Village v : Villages.getVillages()) {
            if (v.getAllianceNumber() != this.getId() || v.getAllianceNumber() == v.getId()) continue;
            return true;
        }
        return false;
    }

    public final Village[] getVillages() {
        HashSet<Village> toReturn = new HashSet<Village>();
        for (Village v : Villages.getVillages()) {
            if (v.getAllianceNumber() != this.getId()) continue;
            toReturn.add(v);
        }
        return toReturn.toArray(new Village[toReturn.size()]);
    }

    public final Village getAllianceCapital() {
        Village[] villages;
        for (Village village : villages = this.getVillages()) {
            if (village.getId() != village.getAllianceNumber()) continue;
            return village;
        }
        return null;
    }

    public final void disband() {
        for (Village v : Villages.getVillages()) {
            if (v.getAllianceNumber() != this.getId()) continue;
            v.setAllianceNumber(0);
            v.sendClearMapAnnotationsOfType((byte)2);
        }
        this.delete();
        this.deleteAllianceMapAnnotations();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void deleteAllianceMapAnnotations() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_ALLIANCE_MAP_POIS);
            ps.setLong(1, this.idNumber);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete alliance map annotations with alliance id=" + this.idNumber, sqx);
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
    public final void delete() {
        alliances.remove(this.idNumber);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_ENTRY);
            ps.setInt(1, this.idNumber);
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
    public void setName(@Nullable Creature changer, String aName) {
        block5: {
            if (this.name.equals(aName)) break block5;
            this.name = aName.substring(0, Math.min(aName.length(), 20));
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_NAME);
                ps.setString(1, aName);
                ps.setInt(2, this.idNumber);
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
            if (changer != null) {
                this.broadCastAllianceMessage(changer, " changed the name of the alliance to " + this.getName() + ".");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setIdNumber(int aIdNumber) {
        if (this.idNumber != aIdNumber) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_ALLIANCENUMBER);
                ps.setInt(1, aIdNumber);
                ps.setInt(2, this.idNumber);
                ps.executeUpdate();
                alliances.remove(this.idNumber);
                this.idNumber = aIdNumber;
                PvPAlliance.addAlliance(this);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setWins(int aWins) {
        if (this.wins != aWins) {
            this.wins = aWins;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_WINS);
                ps.setInt(1, aWins);
                ps.setInt(2, this.idNumber);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDeityOneId(byte aDeityOneId) {
        if (this.deityOneId != aDeityOneId) {
            this.deityOneId = aDeityOneId;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_DEITYONE);
                ps.setByte(1, aDeityOneId);
                ps.setInt(2, this.idNumber);
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
    }

    public final void broadCastMessage(Message message) {
        for (Village v : this.getVillages()) {
            v.broadCastMessage(message, false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setDeityTwoId(byte aDeityTwoId) {
        if (this.deityTwoId != aDeityTwoId) {
            this.deityTwoId = aDeityTwoId;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_DEITYTWO);
                ps.setByte(1, aDeityTwoId);
                ps.setInt(2, this.idNumber);
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setMotd(String aMotd) {
        block5: {
            if (this.motd == null || this.motd.equals(aMotd)) break block5;
            this.motd = aMotd;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATE_MOTD);
                ps.setString(1, aMotd);
                ps.setInt(2, this.idNumber);
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
            if (aMotd != null && aMotd.length() > 0) {
                this.broadCastMessage(this.getMotdMessage());
            }
        }
    }

    public final void broadCastAllianceMessage(Creature sender, String toSend) {
        this.broadCastMessage(this.createAllianceMessage(sender, toSend));
    }

    public final Message createAllianceMessage(Creature sender, String toSend) {
        return new Message(sender, 15, "Alliance", "<" + sender.getName() + "> " + toSend);
    }

    public final Message createAllianceEventMessage(Creature sender, String toSend) {
        return new Message(sender, 1, ":Event", "<" + sender.getName() + "> " + toSend);
    }

    public final void disband(Creature disbander) {
        for (Village v : this.getVillages()) {
            v.broadCastMessage(this.createAllianceEventMessage(disbander, "disbanded " + this.getName() + "."));
            v.setAllianceNumber(0);
            v.sendClearMapAnnotationsOfType((byte)2);
        }
        this.delete();
        this.deleteAllianceMapAnnotations();
    }

    public final void transferControl(Creature disbander, int newCapital) {
        try {
            Village vill = Villages.getVillage(newCapital);
            for (Village v : this.getVillages()) {
                v.broadCastMessage(this.createAllianceEventMessage(disbander, "transfers control of " + this.getName() + " to " + vill.getName() + "."));
                v.setAllianceNumber(newCapital);
            }
            this.setIdNumber(newCapital);
        }
        catch (NoSuchVillageException noSuchVillageException) {
            // empty catch block
        }
    }

    public final boolean addAllianceMapAnnotation(MapAnnotation annotation2, boolean send) {
        if (this.mapAnnotations.size() < 500) {
            this.mapAnnotations.add(annotation2);
            if (send) {
                this.sendAddAnnotations(new MapAnnotation[]{annotation2});
            }
            return true;
        }
        return false;
    }

    public void removeAllianceMapAnnotation(MapAnnotation annotation2) {
        if (this.mapAnnotations.contains(annotation2)) {
            try {
                this.mapAnnotations.remove(annotation2);
                MapAnnotation.deleteAnnotation(annotation2.getId());
                this.sendRemoveAnnotation(annotation2);
            }
            catch (IOException iex) {
                logger.log(Level.WARNING, "Error deleting alliance map annotation: " + annotation2.getId() + " " + iex.getMessage(), iex);
            }
        }
    }

    private final void sendAddAnnotations(MapAnnotation[] annotations) {
        for (Village v : this.getVillages()) {
            v.sendMapAnnotationsToVillagers(annotations);
        }
    }

    private final void sendRemoveAnnotation(MapAnnotation annotation2) {
        for (Village v : this.getVillages()) {
            v.sendRemoveMapAnnotationToVillagers(annotation2);
        }
    }

    public final void sendClearAllianceAnnotations() {
        for (Village village : this.getVillages()) {
            village.sendClearMapAnnotationsOfType((byte)2);
        }
    }

    public final Set<MapAnnotation> getAllianceMapAnnotations() {
        return this.mapAnnotations;
    }

    public final MapAnnotation[] getAllianceMapAnnotationsArray() {
        if (this.mapAnnotations == null || this.mapAnnotations.size() == 0) {
            return null;
        }
        MapAnnotation[] annos = new MapAnnotation[this.mapAnnotations.size()];
        this.mapAnnotations.toArray(annos);
        return annos;
    }
}

