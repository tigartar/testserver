/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.PvPAlliance;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AllianceWar
implements TimeConstants {
    private static final Logger logger = Logger.getLogger(AllianceWar.class.getName());
    private final int aggressor;
    private final int defender;
    private final long declarationTime;
    private long warStartedTime;
    private long peaceDeclaredTime;
    private static final String LOAD_ALL = "SELECT * FROM ALLIANCEWARS";
    private static final String INSERTWAR = "INSERT INTO ALLIANCEWARS (ALLIANCEONE,ALLIANCETWO,TIMEDECLARED) VALUES (?,?,?)";
    private static final String DELETEWAR = "DELETE FROM ALLIANCEWARS WHERE ALLIANCEONE=? AND ALLIANCETWO=?";
    private static final String SETWARSTARTED = "UPDATE ALLIANCEWARS SET TIMESTARTED=? WHERE ALLIANCEONE=? AND ALLIANCETWO=?";
    private static final String SETPEACEWANTED = "UPDATE ALLIANCEWARS SET TIMEPEACE=? WHERE ALLIANCEONE=? AND ALLIANCETWO=?";
    public static final long TIME_UNTIL_PEACE = 345600000L;

    public AllianceWar(int aggressorId, int defenderId) {
        PvPAlliance two;
        this.aggressor = aggressorId;
        this.defender = defenderId;
        this.declarationTime = System.currentTimeMillis();
        PvPAlliance one = PvPAlliance.getPvPAlliance(this.aggressor);
        if (one != null && (two = PvPAlliance.getPvPAlliance(this.defender)) != null) {
            one.addAllianceWar(this);
            two.addAllianceWar(this);
            this.create();
        }
    }

    public AllianceWar(int aggressorId, int defenderId, long declared, long started, long peacetime) {
        this.aggressor = aggressorId;
        this.defender = defenderId;
        this.declarationTime = declared;
        this.warStartedTime = started;
        this.peaceDeclaredTime = peacetime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAll() {
        logger.log(Level.INFO, "Loading all alliance wars.");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(LOAD_ALL);
            rs = ps.executeQuery();
            while (rs.next()) {
                PvPAlliance two;
                int allianceOne = rs.getInt("ALLIANCEONE");
                int allianceTwo = rs.getInt("ALLIANCETWO");
                long declared = rs.getLong("TIMEDECLARED");
                long started = rs.getLong("TIMESTARTED");
                long peace = rs.getLong("TIMEPEACE");
                PvPAlliance one = PvPAlliance.getPvPAlliance(allianceOne);
                if (one == null || (two = PvPAlliance.getPvPAlliance(allianceTwo)) == null) continue;
                AllianceWar war = new AllianceWar(allianceOne, allianceTwo, declared, started, peace);
                one.addAllianceWar(war);
                two.addAllianceWar(war);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load pvp alliance wars " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded alliance wars from database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded alliance wars from database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded alliance wars from database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void delete() {
        PvPAlliance two;
        PvPAlliance one = PvPAlliance.getPvPAlliance(this.aggressor);
        if (one != null) {
            one.removeWar(this);
        }
        if ((two = PvPAlliance.getPvPAlliance(this.defender)) != null) {
            two.removeWar(this);
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETEWAR);
            ps.setInt(1, this.aggressor);
            ps.setInt(2, this.defender);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to insert pvp alliance war " + sqx.getMessage(), sqx);
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
    private final void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(INSERTWAR);
            ps.setInt(1, this.aggressor);
            ps.setInt(2, this.defender);
            ps.setLong(3, this.declarationTime);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to insert pvp alliance war " + sqx.getMessage(), sqx);
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

    public final boolean hasBeenAccepted() {
        return this.warStartedTime > 0L;
    }

    public final boolean isActive() {
        return this.hasBeenAccepted() && !this.hasEnded();
    }

    public final boolean hasEnded() {
        return System.currentTimeMillis() > this.peaceDeclaredTime;
    }

    public int getAggressor() {
        return this.aggressor;
    }

    public int getDefender() {
        return this.defender;
    }

    public long getDeclarationTime() {
        return this.declarationTime;
    }

    public long getWarStartedTime() {
        return this.warStartedTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setWarStartedTime() {
        this.warStartedTime = System.currentTimeMillis();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SETWARSTARTED);
            ps.setLong(1, this.warStartedTime);
            ps.setInt(2, this.aggressor);
            ps.setInt(3, this.defender);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set war started in pvp alliance war " + sqx.getMessage(), sqx);
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

    public long getPeaceDeclaredTime() {
        return this.peaceDeclaredTime;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setPeaceDeclaredTime() {
        this.peaceDeclaredTime = System.currentTimeMillis() + 345600000L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(SETPEACEWANTED);
            ps.setLong(1, this.peaceDeclaredTime);
            ps.setInt(2, this.aggressor);
            ps.setInt(3, this.defender);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to set peace declared in pvp alliance war " + sqx.getMessage(), sqx);
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

