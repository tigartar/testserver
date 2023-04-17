/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Guard;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbGuard
extends Guard {
    private static final Logger logger = Logger.getLogger(DbGuard.class.getName());
    private static final String SET_EXPIREDATE = "UPDATE GUARDS SET EXPIREDATE=? WHERE WURMID=?";
    private static final String CREATE_GUARD = "INSERT INTO GUARDS (WURMID, VILLAGEID, EXPIREDATE ) VALUES (?,?,?)";
    static final String DELETE_GUARD = "DELETE FROM GUARDS WHERE WURMID=?";

    DbGuard(int aVillageId, Creature aCreature, long aExpireDate) {
        super(aVillageId, aCreature, aExpireDate);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    void save() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(CREATE_GUARD);
            ps.setLong(1, this.creature.getWurmId());
            ps.setInt(2, this.villageId);
            ps.setLong(3, this.expireDate);
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
    void setExpireDate(long newDate) {
        if (this.expireDate != newDate) {
            this.expireDate = newDate;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(SET_EXPIREDATE);
                ps.setLong(1, this.expireDate);
                ps.setLong(2, this.creature.getWurmId());
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
    @Override
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_GUARD);
            ps.setLong(1, this.creature.getWurmId());
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

