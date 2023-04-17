/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.StructureConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbFloor
extends Floor {
    private static final String CREATE_FLOOR = "INSERT INTO FLOORS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE,COLOR, MATERIAL,HEIGHTOFFSET,LAYER,DIR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_FLOOR = "UPDATE FLOORS SET TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=? WHERE ID=?";
    private static final String GET_FLOOR = "SELECT * FROM FLOORS WHERE ID=?";
    private static final String DELETE_FLOOR = "DELETE FROM FLOORS WHERE ID=?";
    private static final String SET_DAMAGE = "UPDATE FLOORS SET DAMAGE=? WHERE ID=?";
    private static final String SET_QUALITY_LEVEL = "UPDATE FLOORS SET CURRENTQL=? WHERE ID=?";
    private static final String SET_STATE = "UPDATE FLOORS SET STATE=?,MATERIAL=? WHERE ID=?";
    private static final String SET_LAST_USED = "UPDATE FLOORS SET LASTMAINTAINED=? WHERE ID=?";
    private static final String SET_SETTINGS = "UPDATE FLOORS SET SETTINGS=? WHERE ID=?";
    private static final Logger logger = Logger.getLogger(DbWall.class.getName());

    @Override
    public boolean isFence() {
        return false;
    }

    @Override
    public boolean isWall() {
        return false;
    }

    public DbFloor(int id, StructureConstants.FloorType floorType, int tilex, int tiley, byte aDbState, int heightOffset, float currentQl, long structureId, StructureConstants.FloorMaterial floorMaterial, int layer, float origQL, float aDamage, long lastmaintained, byte dir) {
        super(id, floorType, tilex, tiley, aDbState, heightOffset, currentQl, structureId, floorMaterial, layer, origQL, aDamage, lastmaintained, dir);
    }

    public DbFloor(StructureConstants.FloorType floorType, int tilex, int tiley, int heightOffset, float qualityLevel, long structure, StructureConstants.FloorMaterial material, int layer) {
        super(floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, layer);
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
                ps = dbcon.prepareStatement(SET_STATE);
                ps.setByte(1, this.dbState);
                ps.setByte(2, this.getMaterial().getCode());
                ps.setInt(3, this.getNumber());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set state to " + newState + " for floor with id " + this.getNumber(), sqx);
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
                ps = dbcon.prepareStatement(UPDATE_FLOOR);
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
                ps.setInt(11, this.getNumber());
                ps.executeUpdate();
            } else {
                ps = dbcon.prepareStatement(CREATE_FLOOR, 1);
                ps.setByte(1, this.getType().getCode());
                ps.setLong(2, this.getLastUsed());
                ps.setFloat(3, this.getCurrentQL());
                ps.setFloat(4, this.getOriginalQL());
                ps.setFloat(5, this.getDamage());
                ps.setLong(6, this.getStructureId());
                ps.setInt(7, this.getTileX());
                ps.setInt(8, this.getTileY());
                ps.setByte(9, this.getState());
                ps.setInt(10, this.getColor());
                ps.setByte(11, this.getMaterial().getCode());
                ps.setInt(12, this.getHeightOffset());
                ps.setByte(13, this.getLayer());
                ps.setByte(14, this.getDir());
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
        block13: {
            block14: {
                VolaTile tile;
                boolean forcePlan = false;
                if (this.isIndestructible()) {
                    return false;
                }
                if (aDamage >= 100.0f) {
                    Structure struct;
                    VolaTile tile2 = this.getTile();
                    if (tile2 != null && (struct = tile2.getStructure()) != null && struct.wouldCreateFlyingStructureIfRemoved(this)) {
                        forcePlan = true;
                    }
                    if (forcePlan) {
                        this.setFloorState(StructureConstants.FloorState.PLANNING);
                        this.setQualityLevel(1.0f);
                        if (tile2 != null) {
                            tile2.updateFloor(this);
                        }
                    }
                }
                if (this.damage == aDamage) break block13;
                boolean updateState = false;
                if (this.damage >= 60.0f && aDamage < 60.0f || this.damage < 60.0f && aDamage >= 60.0f) {
                    updateState = true;
                }
                this.damage = aDamage;
                if (forcePlan) {
                    this.damage = 0.0f;
                }
                if (!(this.damage < 100.0f)) break block14;
                Connection dbcon = null;
                PreparedStatement ps = null;
                try {
                    dbcon = DbConnector.getZonesDbCon();
                    ps = dbcon.prepareStatement(SET_DAMAGE);
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
                    this.getTile().updateFloorDamageState(this);
                }
                break block13;
            }
            VolaTile t = this.getTile();
            if (t != null) {
                t.removeFloor(this);
            }
            this.delete();
        }
        return this.damage >= 100.0f;
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
                ps = dbcon.prepareStatement(SET_LAST_USED);
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
                ps = dbcon.prepareStatement(SET_QUALITY_LEVEL);
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
            ps = dbcon.prepareStatement(GET_FLOOR);
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
            ps = dbcon.prepareStatement(DELETE_FLOOR);
            ps.setInt(1, this.getNumber());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete floor with id " + this.getNumber(), sqx);
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
                logger.log(Level.WARNING, "Failed to save settings for floor with id " + this.getNumber(), sqx);
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

