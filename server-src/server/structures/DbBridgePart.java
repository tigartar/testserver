/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbBridgePart
extends BridgePart {
    private static final String CREATEBRIDGEPART = "INSERT INTO BRIDGEPARTS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE, MATERIAL, HEIGHTOFFSET, DIR, SLOPE, STAGECOUNT, NORTHEXIT, EASTEXIT, SOUTHEXIT, WESTEXIT, LAYER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATEBRIDGEPART = "UPDATE BRIDGEPARTS SET TYPE=?,LASTMAINTAINED=?,CURRENTQL=?,ORIGINALQL=?,DAMAGE=?,STRUCTURE=?,STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=?,SLOPE=?,STAGECOUNT=?,NORTHEXIT=?,EASTEXIT=?,SOUTHEXIT=?,WESTEXIT=?,LAYER=? WHERE ID=?";
    private static final String GETBRIDGEPART = "SELECT * FROM BRIDGEPARTS WHERE ID=?";
    private static final String DELETEBRIDGEPART = "DELETE FROM BRIDGEPARTS WHERE ID=?";
    private static final String SETDAMAGE = "UPDATE BRIDGEPARTS SET DAMAGE=? WHERE ID=?";
    private static final String SETQUALITYLEVEL = "UPDATE BRIDGEPARTS SET CURRENTQL=? WHERE ID=?";
    private static final String SETSTATE = "UPDATE BRIDGEPARTS SET STATE=?,MATERIAL=? WHERE ID=?";
    private static final String SETLASTUSED = "UPDATE BRIDGEPARTS SET LASTMAINTAINED=? WHERE ID=?";
    private static final String SET_SETTINGS = "UPDATE BRIDGEPARTS SET SETTINGS=? WHERE ID=?";
    private static final String SETROADTYPE = "UPDATE BRIDGEPARTS SET ROADTYPE=? WHERE ID=?";
    private static final Logger logger = Logger.getLogger(DbWall.class.getName());

    @Override
    public boolean isFence() {
        return false;
    }

    @Override
    public boolean isWall() {
        return false;
    }

    public DbBridgePart(int id, BridgeConstants.BridgeType floorType, int tilex, int tiley, byte aDbState, int heightOffset, float currentQl, long structureId, BridgeConstants.BridgeMaterial floorMaterial, float origQL, float dam, int materialCount, long lastmaintained, byte dir, byte slope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer) {
        super(id, floorType, tilex, tiley, aDbState, heightOffset, currentQl, structureId, floorMaterial, origQL, dam, materialCount, lastmaintained, dir, slope, aNorthExit, aEastExit, aSouthExit, aWestExit, roadType, layer);
    }

    public DbBridgePart(BridgeConstants.BridgeType floorType, int tilex, int tiley, int heightOffset, float qualityLevel, long structure, BridgeConstants.BridgeMaterial material, byte dir, byte slope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer) {
        super(floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, dir, slope, aNorthExit, aEastExit, aSouthExit, aWestExit, roadType, layer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void setState(byte newState) {
        if (this.dbState != newState) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.dbState = newState;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SETSTATE);
                ps.setByte(1, this.dbState);
                ps.setByte(2, this.getMaterial().getCode());
                ps.setInt(3, this.getNumber());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set state to " + newState + " for bridge part with id " + this.getNumber(), sqx);
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
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(UPDATEBRIDGEPART);
                ps.setByte(1, this.getType().getCode());
                ps.setLong(2, this.getLastUsed());
                ps.setFloat(3, this.getCurrentQL());
                ps.setFloat(4, this.getOriginalQL());
                ps.setFloat(5, this.getDamage());
                ps.setLong(6, this.getStructureId());
                ps.setByte(7, this.getState());
                ps.setByte(8, this.getMaterial().getCode());
                ps.setInt(9, this.getHeightOffset());
                ps.setByte(10, this.getDir());
                ps.setByte(11, this.getSlope());
                ps.setInt(12, this.getMaterialCount());
                ps.setInt(13, this.getNorthExit());
                ps.setInt(14, this.getEastExit());
                ps.setInt(15, this.getSouthExit());
                ps.setInt(16, this.getWestExit());
                ps.setInt(17, this.getLayer());
                ps.setInt(18, this.getNumber());
                ps.executeUpdate();
            } else {
                ps = dbcon.prepareStatement(CREATEBRIDGEPART, 1);
                ps.setByte(1, this.getType().getCode());
                ps.setLong(2, this.getLastUsed());
                ps.setFloat(3, this.getCurrentQL());
                ps.setFloat(4, this.getOriginalQL());
                ps.setFloat(5, this.getDamage());
                ps.setLong(6, this.getStructureId());
                ps.setInt(7, this.getTileX());
                ps.setInt(8, this.getTileY());
                ps.setByte(9, this.getState());
                ps.setByte(10, this.getMaterial().getCode());
                ps.setInt(11, this.getHeightOffset());
                ps.setByte(12, this.getDir());
                ps.setByte(13, this.getSlope());
                ps.setInt(14, this.getMaterialCount());
                ps.setInt(15, this.getNorthExit());
                ps.setInt(16, this.getEastExit());
                ps.setInt(17, this.getSouthExit());
                ps.setInt(18, this.getWestExit());
                ps.setInt(19, this.getLayer());
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    this.setNumber(rs.getInt(1));
                }
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
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean setDamage(float aDamage) {
        block20: {
            block21: {
                VolaTile tile;
                boolean forcePlan = false;
                if (this.isIndestructible()) {
                    return false;
                }
                if (aDamage >= 100.0f) {
                    VolaTile tile2 = this.getTile();
                    forcePlan = true;
                    BridgeConstants.BridgeState oldBridgeState = this.getBridgePartState();
                    this.setBridgePartState(BridgeConstants.BridgeState.PLANNED);
                    this.setQualityLevel(1.0f);
                    this.saveRoadType((byte)0);
                    if (tile2 != null) {
                        tile2.updateBridgePart(this);
                        if (oldBridgeState != BridgeConstants.BridgeState.PLANNED) {
                            BridgeConstants.BridgeType bType = this.getType();
                            switch (this.getMaterial()) {
                                case BRICK: 
                                case MARBLE: 
                                case POTTERY: 
                                case RENDERED: 
                                case ROUNDED_STONE: 
                                case SANDSTONE: 
                                case SLATE: {
                                    if (bType.isSupportType()) {
                                        this.damageAdjacent("abutment", 50);
                                        break;
                                    }
                                    if (bType.isAbutment()) {
                                        this.damageAdjacent("bracing", 25);
                                        break;
                                    }
                                    if (!bType.isBracing()) break;
                                    this.damageAdjacent("crown", 10);
                                    this.damageAdjacent("floating", 10);
                                    break;
                                }
                                case WOOD: {
                                    if (bType.isSupportType()) {
                                        this.damageAdjacent("abutment", 50);
                                        this.damageAdjacent("crown", 25);
                                        break;
                                    }
                                    if (!bType.isAbutment()) break;
                                    this.damageAdjacent("crown", 10);
                                    break;
                                }
                                case ROPE: {
                                    if (!bType.isAbutment()) break;
                                    this.damageAdjacent("crown", 50);
                                }
                            }
                        }
                    }
                }
                if (this.damage == aDamage) break block20;
                boolean updateState = false;
                if (this.damage >= 60.0f && aDamage < 60.0f || this.damage < 60.0f && aDamage >= 60.0f) {
                    updateState = true;
                }
                this.damage = aDamage;
                if (forcePlan) {
                    this.damage = 0.0f;
                }
                if (!(this.damage < 100.0f)) break block21;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement(SETDAMAGE);
                    ps.setFloat(1, this.getDamage());
                    ps.setInt(2, this.getNumber());
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    try {
                        logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + sqx.getMessage(), sqx);
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
                if (updateState && (tile = this.getTile()) != null) {
                    this.getTile().updateBridgePartDamageState(this);
                }
                break block20;
            }
            VolaTile t = this.getTile();
            if (t != null) {
                t.removeBridgePart(this);
            }
            this.delete();
        }
        return this.damage >= 100.0f;
    }

    private void damageAdjacent(String typeName, int addDamage) {
        BridgePart[] bps;
        Structure structWest;
        VolaTile vtWest;
        BridgePart[] bps2;
        Structure structSouth;
        VolaTile vtSouth;
        BridgePart[] bps3;
        Structure structEast;
        VolaTile vtEast;
        BridgePart[] bps4;
        Structure structNorth;
        VolaTile vtNorth = Zones.getTileOrNull(this.getTileX(), this.getTileY() - 1, this.isOnSurface());
        if (vtNorth != null && (structNorth = vtNorth.getStructure()) != null && structNorth.getWurmId() == this.getStructureId() && (bps4 = vtNorth.getBridgeParts()).length == 1 && bps4[0].getType().getName().equalsIgnoreCase(typeName)) {
            bps4[0].setDamage(bps4[0].getDamage() + (float)addDamage);
        }
        if ((vtEast = Zones.getTileOrNull(this.getTileX() + 1, this.getTileY(), this.isOnSurface())) != null && (structEast = vtEast.getStructure()) != null && structEast.getWurmId() == this.getStructureId() && (bps3 = vtEast.getBridgeParts()).length == 1 && bps3[0].getType().getName().equalsIgnoreCase(typeName)) {
            bps3[0].setDamage(bps3[0].getDamage() + (float)addDamage);
        }
        if ((vtSouth = Zones.getTileOrNull(this.getTileX(), this.getTileY() + 1, this.isOnSurface())) != null && (structSouth = vtSouth.getStructure()) != null && structSouth.getWurmId() == this.getStructureId() && (bps2 = vtSouth.getBridgeParts()).length == 1 && bps2[0].getType().getName().equalsIgnoreCase(typeName)) {
            bps2[0].setDamage(bps2[0].getDamage() + (float)addDamage);
        }
        if ((vtWest = Zones.getTileOrNull(this.getTileX() - 1, this.getTileY(), this.isOnSurface())) != null && (structWest = vtWest.getStructure()) != null && structWest.getWurmId() == this.getStructureId() && (bps = vtWest.getBridgeParts()).length == 1 && bps[0].getType().getName().equalsIgnoreCase(typeName)) {
            bps[0].setDamage(bps[0].getDamage() + (float)addDamage);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setLastUsed(long now) {
        if (this.lastUsed != now) {
            this.lastUsed = now;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SETLASTUSED);
                ps.setLong(1, this.lastUsed);
                ps.setInt(2, this.getNumber());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + sqx.getMessage(), sqx);
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
    public boolean setQualityLevel(float ql) {
        if (ql > 100.0f) {
            ql = 100.0f;
        }
        if (this.currentQL != ql) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.currentQL = ql;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SETQUALITYLEVEL);
                ps.setFloat(1, this.currentQL);
                ps.setInt(2, this.getNumber());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + sqx.getMessage(), sqx);
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
        return ql >= 100.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(GETBRIDGEPART);
            ps.setInt(1, this.getNumber());
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
    public void delete() {
        MissionTargets.destroyMissionTarget(this.getId(), true);
        MethodsHighways.removeNearbyMarkers(this);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETEBRIDGEPART);
            ps.setInt(1, this.getNumber());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete bridge part with id " + this.getNumber(), sqx);
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
    public long getTempId() {
        return -10L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void savePermissions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SET_SETTINGS);
            ps.setLong(1, this.permissions.getPermissions());
            ps.setLong(2, this.getNumber());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to save settings for bridge part with id " + this.getNumber(), sqx);
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
    public void saveRoadType(byte roadType) {
        if (this.roadType != roadType) {
            this.roadType = roadType;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SETROADTYPE);
                ps.setByte(1, this.roadType);
                ps.setLong(2, this.getNumber());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to save roadtype for bridge part with id " + this.getNumber(), sqx);
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
    public final boolean isOnSouthBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnNorthBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnWestBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnEastBorder(TilePos pos) {
        return false;
    }
}

