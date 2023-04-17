/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbDoor
extends Door
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(DbDoor.class.getName());
    private static final String GET_DOOR = "SELECT * FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
    private static final String EXISTS_DOOR = "SELECT 1 FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
    private static final String CREATE_DOOR = "INSERT INTO DOORS (LOCKID,NAME,SETTINGS,STRUCTURE,INNERWALL) VALUES(?,?,?,?,?)";
    private static final String UPDATE_DOOR = "UPDATE DOORS SET LOCKID=?,NAME=?,SETTINGS=? WHERE STRUCTURE=? AND INNERWALL=?";
    private static final String DELETE_DOOR = "DELETE FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
    private static final String SET_NAME = "UPDATE DOORS SET NAME=? WHERE INNERWALL=?";

    public DbDoor(Wall aWall) {
        super(aWall);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            String string = CREATE_DOOR;
            if (this.exists(dbcon)) {
                string = UPDATE_DOOR;
            }
            ps = dbcon.prepareStatement(string);
            ps.setLong(1, this.lock);
            ps.setString(2, this.name);
            ps.setInt(3, 0);
            ps.setLong(4, this.structure);
            long iid = this.wall.getId();
            ps.setLong(5, iid);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to save door for structure with id " + this.structure, ex);
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
                ps = dbcon.prepareStatement(GET_DOOR);
                ps.setLong(1, this.structure);
                ps.setLong(2, this.wall.getId());
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.lock = rs.getLong("LOCKID");
                    this.name = rs.getString("NAME");
                    break block5;
                }
                this.save();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to load door for structure with id " + this.structure, ex);
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
    private boolean exists(Connection dbcon) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(EXISTS_DOOR);
            ps.setLong(1, this.structure);
            ps.setLong(2, this.wall.getId());
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
     * Loose catch block
     */
    @Override
    public void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_DOOR);
            ps.setLong(1, this.structure);
            ps.setLong(2, this.wall.getId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to delete wall for structure with id " + this.structure, sqx);
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, this.structure + ":" + ex.getMessage(), ex);
            {
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (this.lock != -10L) {
            Items.decay(this.lock, null);
            this.lock = -10L;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setName(String aName) {
        String newname = aName.substring(0, Math.min(39, aName.length()));
        if (!this.getName().equals(newname)) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                this.setNewName(newname);
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_NAME);
                ps.setString(1, this.getName());
                ps.setLong(2, this.wall.getId());
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to set name to " + this.getName() + " for door with innerwall of " + this.wall.getId(), sqx);
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
    public boolean isItem() {
        return false;
    }
}

