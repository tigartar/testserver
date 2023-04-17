/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Alliance;
import com.wurmonline.server.villages.Village;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbAlliance
extends Alliance {
    private static final Logger logger = Logger.getLogger(DbAlliance.class.getName());
    private static final String createAlliance = "INSERT INTO ALLIANCES (VILLONE, VILLTWO) VALUES (?,?)";
    private static final String deleteAlliance = "DELETE FROM ALLIANCES WHERE VILLONE=? AND VILLTWO=?";

    DbAlliance(Village vone, Village vtwo) {
        super(vone, vtwo);
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
            ps = dbcon.prepareStatement(createAlliance);
            ps.setInt(1, this.villone.getId());
            ps.setInt(2, this.villtwo.getId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create alliance between " + this.villone.getName() + " and " + this.villtwo.getName(), sqx);
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
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(deleteAlliance);
            ps.setInt(1, this.villone.getId());
            ps.setInt(2, this.villtwo.getId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete alliance between " + this.villone.getName() + " and " + this.villtwo.getName(), sqx);
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

