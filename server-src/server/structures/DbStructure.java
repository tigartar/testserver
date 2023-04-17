/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.BuildTile;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbStructure
extends Structure {
    private static final Logger logger = Logger.getLogger(DbStructure.class.getName());
    private static final String GET_STRUCTURE = "SELECT * FROM STRUCTURES WHERE WURMID=?";
    private static final String SAVE_STRUCTURE = "UPDATE STRUCTURES SET CENTERX=?,CENTERY=?,ROOF=?,SURFACED=?,NAME=?,FINISHED=?,WRITID=?,FINFINISHED=?,ALLOWSVILLAGERS=?,ALLOWSALLIES=?,ALLOWSKINGDOM=?,PLANNER=?,OWNERID=?,SETTINGS=?,VILLAGE=? WHERE WURMID=?";
    private static final String CREATE_STRUCTURE = "INSERT INTO STRUCTURES(WURMID, STRUCTURETYPE) VALUES(?,?)";
    private static final String DELETE_STRUCTURE = "DELETE FROM STRUCTURES WHERE WURMID=?";
    private static final String ADD_BUILDTILE = "INSERT INTO BUILDTILES(STRUCTUREID,TILEX,TILEY,LAYER) VALUES (?,?,?,?)";
    private static final String DELETE_BUILDTILE = "DELETE FROM BUILDTILES WHERE STRUCTUREID=? AND TILEX=? AND TILEY=? AND LAYER=?";
    private static final String DELETE_ALLBUILDTILES = "DELETE FROM BUILDTILES WHERE STRUCTUREID=?";
    private static final String LOAD_ALLBUILDTILES = "SELECT * FROM BUILDTILES";
    private static final String SET_FINISHED = "UPDATE STRUCTURES SET FINISHED=? WHERE WURMID=?";
    private static final String SET_FIN_FINISHED = "UPDATE STRUCTURES SET FINFINISHED=? WHERE WURMID=?";
    private static final String SET_WRITID = "UPDATE STRUCTURES SET WRITID=? WHERE WURMID=?";
    private static final String SET_OWNERID = "UPDATE STRUCTURES SET OWNERID=? WHERE WURMID=?";
    private static final String SET_SETTINGS = "UPDATE STRUCTURES SET SETTINGS=?,VILLAGE=? WHERE WURMID=?";
    private static final String SET_NAME = "UPDATE STRUCTURES SET NAME=? WHERE WURMID=?";

    DbStructure(byte theStructureType, String aName, long id, int x, int y, boolean isSurfaced) {
        super(theStructureType, aName, id, x, y, isSurfaced);
    }

    DbStructure(long id) throws IOException, NoSuchStructureException {
        super(id);
    }

    DbStructure(byte theStructureType, String aName, long aId, boolean aIsSurfaced, byte aRoof, boolean aFinished, boolean aFinFinished, long aWritId, String aPlanner, long aOwnerId, int aSettings, int aVillageId, boolean aAllowsVillagers, boolean aAllowsAllies, boolean aAllowKingdom) {
        super(theStructureType, aName, aId, aIsSurfaced, aRoof, aFinished, aFinFinished, aWritId, aPlanner, aOwnerId, aSettings, aVillageId, aAllowsVillagers, aAllowsAllies, aAllowKingdom);
    }

    @Override
    void load() throws IOException, NoSuchStructureException {
        if (!this.isLoading()) {
            ResultSet rs;
            PreparedStatement ps;
            Connection dbcon;
            block11: {
                dbcon = null;
                ps = null;
                rs = null;
                try {
                    this.setLoading(true);
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement(GET_STRUCTURE);
                    ps.setLong(1, this.getWurmId());
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        this.setStructureType(rs.getByte("STRUCTURETYPE"));
                        this.setSurfaced(rs.getBoolean("SURFACED"));
                        this.setRoof(rs.getByte("ROOF"));
                        String lName = rs.getString("NAME");
                        if (lName == null) {
                            lName = "Unknown structure";
                        }
                        if (lName.length() >= 50) {
                            lName = lName.substring(0, 49);
                        }
                        this.setName(lName, false);
                        this.finished = rs.getBoolean("FINISHED");
                        this.finalfinished = rs.getBoolean("FINFINISHED");
                        this.allowsVillagers = rs.getBoolean("ALLOWSVILLAGERS");
                        this.allowsAllies = rs.getBoolean("ALLOWSALLIES");
                        this.allowsKingdom = rs.getBoolean("ALLOWSKINGDOM");
                        this.setPlanner(rs.getString("PLANNER"));
                        this.setOwnerId(rs.getLong("OWNERID"));
                        this.setSettings(rs.getInt("SETTINGS"));
                        this.villageId = rs.getInt("VILLAGE");
                        if (this.isTypeHouse()) {
                            try {
                                this.setWritid(rs.getLong("WRITID"), false);
                            }
                            catch (SQLException nsi) {
                                logger.log(Level.INFO, "No writ for house with id:" + this.getWurmId() + " creating new after loading.", nsi);
                            }
                        }
                        break block11;
                    }
                    throw new NoSuchStructureException("No structure found with id " + this.getWurmId());
                }
                catch (SQLException sqex) {
                    try {
                        throw new IOException(sqex);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, rs);
                        DbConnector.returnConnection(dbcon);
                        throw throwable;
                    }
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }

    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            if (!this.exists(dbcon)) {
                this.create(dbcon);
            }
            ps = dbcon.prepareStatement(SAVE_STRUCTURE);
            ps.setInt(1, this.getCenterX());
            ps.setInt(2, this.getCenterY());
            for (VolaTile t : this.structureTiles) {
                Wall[] wallArr = t.getWalls();
                for (int x = 0; x < wallArr.length; ++x) {
                    try {
                        wallArr[x].save();
                        continue;
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, "Failed to save wall: " + wallArr[x]);
                    }
                }
                Floor[] floorArr = t.getFloors();
                for (int x = 0; x < floorArr.length; ++x) {
                    try {
                        floorArr[x].save();
                        continue;
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, "Failed to save floor: " + floorArr[x]);
                    }
                }
                BridgePart[] partsArr = t.getBridgeParts();
                for (int x = 0; x < partsArr.length; ++x) {
                    try {
                        partsArr[x].save();
                        continue;
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, "Failed to save bridge part: " + partsArr[x]);
                    }
                }
            }
            ps.setByte(3, this.getRoof());
            ps.setBoolean(4, this.isSurfaced());
            ps.setString(5, this.getName());
            ps.setBoolean(6, this.isFinished());
            ps.setLong(7, this.getWritId());
            ps.setBoolean(8, this.isFinalFinished());
            ps.setBoolean(9, this.allowsCitizens());
            ps.setBoolean(10, this.allowsAllies());
            ps.setBoolean(11, this.allowsKingdom());
            ps.setString(12, this.getPlanner());
            ps.setLong(13, this.getOwnerId());
            ps.setInt(14, this.getSettings().getPermissions());
            ps.setInt(15, this.getVillageId());
            ps.setLong(16, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Problem", sqex);
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

    private void create(Connection dbcon) throws IOException {
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement(CREATE_STRUCTURE);
            ps.setLong(1, this.getWurmId());
            ps.setByte(2, this.getStructureType());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Problem", sqex);
            throw new IOException(sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(GET_STRUCTURE);
            ps.setLong(1, this.getWurmId());
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_STRUCTURE);
            ps.setLong(1, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete structure with id=" + this.getWurmId(), sqx);
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
        StructureSettings.remove(this.getWurmId());
        PermissionsHistories.remove(this.getWurmId());
        Structures.removeStructure(this.getWurmId());
        this.deleteAllBuildTiles();
    }

    @Override
    public void endLoading() throws IOException {
        if (!this.hasLoaded()) {
            int tiley;
            int tilex;
            Set<Floor> floorset;
            this.setHasLoaded(true);
            List<Wall> structureWalls = Wall.getWallsAsArrayListFor(this.getWurmId());
            if (this.loadStructureTiles(structureWalls)) {
                while (this.fillHoles()) {
                    logger.log(Level.INFO, "Filling holes " + this.getWurmId());
                }
            }
            if ((floorset = Floor.getFloorsFor(this.getWurmId())) != null) {
                Iterator<Floor> it = floorset.iterator();
                while (it.hasNext()) {
                    try {
                        Floor floor = it.next();
                        tilex = floor.getTileX();
                        tiley = floor.getTileY();
                        Zone zone = Zones.getZone(tilex, tiley, this.isSurfaced());
                        VolaTile tile = zone.getOrCreateTile(tilex, tiley);
                        if (this.structureTiles.contains(tile)) {
                            tile.addFloor(floor);
                            continue;
                        }
                        logger.log(Level.FINE, "Floor #" + floor.getId() + " thinks it belongs to structure " + this.getWurmId() + " but structureTiles disagrees.");
                    }
                    catch (NoSuchZoneException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            Set<BridgePart> bridgePartsset = BridgePart.getBridgePartsFor(this.getWurmId());
            Iterator<BridgePart> it = bridgePartsset.iterator();
            while (it.hasNext()) {
                try {
                    BridgePart bridgePart = it.next();
                    tilex = bridgePart.getTileX();
                    tiley = bridgePart.getTileY();
                    Zone zone = Zones.getZone(tilex, tiley, this.isSurfaced());
                    VolaTile tile = zone.getOrCreateTile(tilex, tiley);
                    if (this.structureTiles.contains(tile)) {
                        tile.addBridgePart(bridgePart);
                        continue;
                    }
                    logger.log(Level.FINE, "BridgePart #" + bridgePart.getId() + " thinks it belongs to structure " + this.getWurmId() + " but structureTiles disagrees.");
                }
                catch (NoSuchZoneException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
            Zone northW = null;
            Zone northE = null;
            Zone southW = null;
            Zone southE = null;
            try {
                northW = Zones.getZone(this.minX, this.minY, this.surfaced);
                northW.addStructure(this);
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                northE = Zones.getZone(this.maxX, this.minY, this.surfaced);
                if (northE != northW) {
                    northE.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                southE = Zones.getZone(this.maxX, this.maxY, this.surfaced);
                if (southE != northE && southE != northW) {
                    southE.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
            try {
                southW = Zones.getZone(this.minX, this.maxY, this.surfaced);
                if (southW != northE && southW != northW && southW != southE) {
                    southW.addStructure(this);
                }
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setFinished(boolean finish) {
        if (this.isFinished() != finish) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.finished = finish;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_FINISHED);
                ps.setBoolean(1, this.isFinished());
                ps.setLong(2, this.getWurmId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set finished to " + finish + " for structure " + this.getName() + " with id " + this.getWurmId(), sqx);
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
    public void setFinalFinished(boolean finfinish) {
        if (this.isFinalFinished() != finfinish) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.finalfinished = finfinish;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_FIN_FINISHED);
                ps.setBoolean(1, this.isFinalFinished());
                ps.setLong(2, this.getWurmId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set finfinished to " + finfinish + " for structure " + this.getName() + " with id " + this.getWurmId(), sqx);
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
    public void saveWritId() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_WRITID);
            ps.setLong(1, this.writid);
            ps.setLong(2, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set writId to " + this.writid + " for structure " + this.getName() + " with id " + this.getWurmId(), sqx);
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
    public void saveOwnerId() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_OWNERID);
            ps.setLong(1, this.ownerId);
            ps.setLong(2, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set ownerId to " + this.ownerId + " for structure " + this.getName() + " with id " + this.getWurmId(), sqx);
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
    public void saveSettings() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_SETTINGS);
            ps.setInt(1, this.getSettings().getPermissions());
            ps.setInt(2, this.getVillageId());
            ps.setLong(3, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set settings to " + this.getSettings().getPermissions() + " for structure " + this.getName() + " with id " + this.getWurmId(), sqx);
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
    public void saveName() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_NAME);
            ps.setString(1, this.getName());
            ps.setLong(2, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set name to " + this.getName() + " for structure with id " + this.getWurmId(), sqx);
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
    public void setAllowVillagers(boolean allow) {
        if (this.allowsCitizens() != allow) {
            this.allowsVillagers = allow;
            if (allow) {
                this.addDefaultCitizenPermissions();
            } else {
                this.removeStructureGuest(-30L);
            }
        }
    }

    @Override
    public void setAllowKingdom(boolean allow) {
        if (this.allowsKingdom() != allow) {
            this.allowsKingdom = allow;
            if (allow) {
                this.addDefaultKingdomPermissions();
            } else {
                this.removeStructureGuest(-40L);
            }
        }
    }

    @Override
    public void setAllowAllies(boolean allow) {
        if (this.allowsAllies() != allow) {
            this.allowsAllies = allow;
            if (allow) {
                this.addDefaultAllyPermissions();
            } else {
                this.removeStructureGuest(-20L);
            }
        }
    }

    @Override
    public void addNewGuest(long guestId, int aSettings) {
        StructureSettings.addPlayer(this.getWurmId(), guestId, aSettings);
    }

    @Override
    public void removeStructureGuest(long guestId) {
        StructureSettings.removePlayer(this.getWurmId(), guestId);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeBuildTile(int tilex, int tiley, int layer) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_BUILDTILE);
            ps.setLong(1, this.getWurmId());
            ps.setInt(2, tilex);
            ps.setInt(3, tiley);
            ps.setInt(4, layer);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to remove build tile for structure with id " + this.getWurmId(), ex);
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
    public void addNewBuildTile(int tilex, int tiley, int layer) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(ADD_BUILDTILE);
            ps.setLong(1, this.getWurmId());
            ps.setInt(2, tilex);
            ps.setInt(3, tiley);
            ps.setInt(4, layer);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to add build tile for structure with id " + this.getWurmId(), ex);
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
    public static final void loadBuildTiles() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_ALLBUILDTILES);
            rs = ps.executeQuery();
            while (rs.next()) {
                try {
                    Structure structure = Structures.getStructure(rs.getLong("STRUCTUREID"));
                    structure.addBuildTile(new BuildTile(rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getInt("LAYER")));
                }
                catch (NoSuchStructureException nss) {
                    logger.log(Level.WARNING, nss.getMessage());
                }
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load all tiles for structures" + ex.getMessage(), ex);
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
    public void deleteAllBuildTiles() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_ALLBUILDTILES);
            ps.setLong(1, this.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to delete all build tiles for structure with id " + this.getWurmId(), ex);
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
    public boolean isItem() {
        return false;
    }
}

