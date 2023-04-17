/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbFence
extends Fence {
    private static final Logger logger = Logger.getLogger(DbFence.class.getName());
    private static final String CREATE_FENCE = "insert into FENCES(TYPE,LASTMAINTAINED,CURRENTQL,ORIGINALQL,DAMAGE,TILEX,TILEY,DIR,ZONEID,STATE,HEIGHTOFFSET,LAYER) values(?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_FENCE = "update FENCES set TYPE=?, LASTMAINTAINED=?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STATE=? where ID=?";
    private static final String GET_FENCE = "select * from FENCES where ID=?";
    private static final String DELETE_FENCE = "delete from FENCES where ID=?";
    private static final String SET_ZONE_ID = "update FENCES set ZONEID=? where ID=?";
    private static final String SET_DAMAGE = "update FENCES set DAMAGE=? where ID=?";
    private static final String SET_QL = "update FENCES set CURRENTQL=? where ID=?";
    private static final String SET_ORIGINAL_QL = "update FENCES set ORIGINALQL=? where ID=?";
    private static final String SET_LAST_USED = "update FENCES set LASTMAINTAINED=? where ID=?";
    private static final String SET_COLOR = "update FENCES set COLOR=? WHERE ID=?";
    private static final String SET_SETTINGS = "UPDATE FENCES SET SETTINGS=? WHERE ID=?";

    public DbFence(StructureConstantsEnum aType, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, Tiles.TileBorderDirection aDir, int aZoneId, int aLayer) {
        super(aType, aTileX, aTileY, aHeightOffset, aQualityLevel, aDir, aZoneId, aLayer);
    }

    public DbFence(int aNumber, StructureConstantsEnum aType, StructureStateEnum aState, int aColor, int aTileX, int aTileY, int aHeightOffset, float aQualityLevel, float aOriginalQl, long aLastUsed, Tiles.TileBorderDirection aDir, int aZoneId, boolean aSurfaced, float aDamage, int aLayer, int aSettings) {
        super(aNumber, aType, aState, aColor, aTileX, aTileY, aHeightOffset, aQualityLevel, aOriginalQl, aLastUsed, aDir, aZoneId, aSurfaced, aDamage, aLayer, aSettings);
    }

    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            if (this.exists(dbcon)) {
                ps = dbcon.prepareStatement(UPDATE_FENCE);
                ps.setShort(1, this.type.value);
                ps.setLong(2, this.lastUsed);
                ps.setFloat(3, this.currentQL);
                ps.setFloat(4, this.originalQL);
                ps.setFloat(5, this.damage);
                ps.setByte(6, this.state.state);
                ps.setInt(7, this.number);
                ps.executeUpdate();
            } else {
                ps = dbcon.prepareStatement(CREATE_FENCE, 1);
                ps.setShort(1, this.type.value);
                ps.setLong(2, this.lastUsed);
                ps.setFloat(3, this.currentQL);
                ps.setFloat(4, this.originalQL);
                ps.setFloat(5, 0.0f);
                ps.setInt(6, this.tilex);
                ps.setInt(7, this.tiley);
                ps.setByte(8, this.dir);
                ps.setInt(9, this.zoneId);
                ps.setByte(10, this.state.state);
                ps.setInt(11, this.heightOffset);
                ps.setInt(12, this.layer);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    this.number = rs.getInt(1);
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

    @Override
    void load() throws IOException {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(GET_FENCE);
                ps.setInt(1, this.number);
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.tilex = rs.getInt("TILEX");
                    this.tiley = rs.getInt("TILEY");
                    this.currentQL = rs.getFloat("ORIGINALQL");
                    this.originalQL = rs.getFloat("CURRENTQL");
                    this.lastUsed = rs.getLong("LASTMAINTAINED");
                    this.type = StructureConstantsEnum.getEnumByValue(rs.getShort("TYPE"));
                    this.state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
                    this.zoneId = rs.getInt("ZONEID");
                    this.dir = rs.getByte("DIR");
                    this.damage = rs.getFloat("DAMAGE");
                    this.setSettings(rs.getInt("SETTINGS"));
                    break block5;
                }
                logger.log(Level.WARNING, "Failed to find fence with number " + this.number);
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
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(GET_FENCE);
            ps.setInt(1, this.number);
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
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_FENCE);
            ps.setInt(1, this.number);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete fence with id " + this.number, sqx);
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
    public void setZoneId(int zid) {
        if (this.zoneId != zid) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.zoneId = zid;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ZONE_ID);
                ps.setInt(1, this.zoneId);
                ps.setInt(2, this.number);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set zoneid to " + zid + " for fence with id " + this.number, sqx);
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
    public boolean setDamage(float dam) {
        boolean destroyed;
        block13: {
            block12: {
                destroyed = false;
                if (this.isIndestructible()) {
                    return false;
                }
                if (dam >= 100.0f) {
                    DoorSettings.remove(this.getId());
                    PermissionsHistories.remove(this.getId());
                    destroyed = true;
                    if (this.supports()) {
                        Structure struct;
                        boolean forcePlan = false;
                        VolaTile tile = this.getTile();
                        if (tile != null && (struct = tile.getStructure()) != null && struct.wouldCreateFlyingStructureIfRemoved(this)) {
                            forcePlan = true;
                        }
                        if (forcePlan) {
                            dam = 0.0f;
                            this.setType(DbFence.getFencePlanForType(this.getType()));
                            this.setQualityLevel(1.0f);
                            if (tile != null) {
                                tile.updateFence(this);
                            }
                        }
                    }
                }
                if (!(dam >= 100.0f)) break block12;
                this.destroy();
                break block13;
            }
            if (this.damage == dam) break block13;
            boolean updateState = false;
            if (this.damage >= 60.0f && dam < 60.0f || this.damage < 60.0f && dam >= 60.0f) {
                updateState = true;
            }
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.damage = Math.max(0.0f, dam);
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_DAMAGE);
                ps.setFloat(1, this.damage);
                ps.setInt(2, this.number);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set damage to " + dam + " for fence with id " + this.number, sqx);
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
            if (updateState && !this.isMagic() && this.getTile() != null) {
                this.getTile().updateFenceState(this);
            }
        }
        return destroyed;
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
                ps = dbcon.prepareStatement(SET_QL);
                ps.setFloat(1, this.currentQL);
                ps.setInt(2, this.number);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set QL to " + ql + " for fence with id " + this.number, sqx);
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
    @Override
    public void improveOrigQualityLevel(float ql) {
        if (ql > 100.0f) {
            ql = 100.0f;
        }
        if (this.originalQL != ql) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.originalQL = ql;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_ORIGINAL_QL);
                ps.setFloat(1, this.originalQL);
                ps.setInt(2, this.number);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set original QL to " + ql + " for fence with id " + this.number, sqx);
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
    public void setLastUsed(long last) {
        if (this.lastUsed != last) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.lastUsed = last;
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_LAST_USED);
                ps.setLong(1, last);
                ps.setInt(2, this.number);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set lastUsed to " + last + " for fence with id " + this.number, sqx);
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
    boolean changeColor(int newcolor) {
        if (this.getColor() != newcolor) {
            boolean bl;
            this.color = newcolor;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_COLOR);
                ps.setInt(1, newcolor);
                ps.setInt(2, this.number);
                ps.executeUpdate();
                VolaTile tile = Zones.getOrCreateTile(this.getTileX(), this.getTileY(), true);
                tile.updateFence(this);
                bl = true;
            }
            catch (SQLException sqx) {
                boolean bl2;
                try {
                    logger.log(Level.WARNING, "Failed to set color to " + this.getColor() + " for fence with id " + this.number, sqx);
                    bl2 = true;
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                return bl2;
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
                logger.log(Level.WARNING, "Failed to save settings for fence id " + this.getNumber(), sqx);
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

