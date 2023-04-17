/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.batchjobs;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Skillbatchjob {
    private static final String setSkillKnow = "UPDATE SKILLS SET VALUE=? WHERE ID=?";
    private static final String changeNumber = "UPDATE SKILLS SET NUMBER=10049 WHERE NUMBER=1004";
    private static final String getIdsForFarming = "SELECT OWNER FROM SKILLS WHERE NUMBER=10049";
    private static final String createCreatureSkill = "insert into SKILLS (VALUE, LASTUSED, MINVALUE, NUMBER, OWNER ) values(?,?,?,?,?)";
    private static final Logger logger = Logger.getLogger(Skillbatchjob.class.getName());

    private Skillbatchjob() {
    }

    public static void runbatch() {
        PreparedStatement ps = null;
        Connection dbcon = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(changeNumber);
            ps.executeUpdate();
            ps.close();
            DbConnector.returnConnection(dbcon);
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(changeNumber);
            ps.executeUpdate();
            ps.close();
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
        Skillbatchjob.addNature();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addNature() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection dbcon = null;
        try {
            PreparedStatement ps2;
            long owner;
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getIdsForFarming);
            rs = ps.executeQuery();
            long time = System.currentTimeMillis();
            while (rs.next()) {
                owner = rs.getLong("OWNER");
                ps2 = dbcon.prepareStatement(createCreatureSkill);
                ps2.setDouble(1, 1.0);
                ps2.setLong(2, time);
                ps2.setDouble(3, 1.0);
                ps2.setInt(4, 1019);
                ps2.setLong(5, owner);
                ps2.executeUpdate();
                ps2.close();
            }
            rs.close();
            ps.close();
            DbConnector.returnConnection(dbcon);
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(getIdsForFarming);
            rs = ps.executeQuery();
            while (rs.next()) {
                owner = rs.getLong("OWNER");
                ps2 = dbcon.prepareStatement(createCreatureSkill);
                ps2.setDouble(1, 1.0);
                ps2.setLong(2, time);
                ps2.setDouble(3, 1.0);
                ps2.setInt(4, 1019);
                ps2.setLong(5, owner);
                ps2.executeUpdate();
                ps2.close();
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
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

    public static final void fixPlayer(long wurmid) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void modifySkillKnowledge(int id, int number, double knowledge) {
        if (number == 104 || number == 103 || number == 102 || number == 100 || number == 101 || number == 106 || number == 105) {
            PreparedStatement ps = null;
            Connection dbcon = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(setSkillKnow);
                ps.setDouble(1, knowledge += 10.0);
                ps.setInt(2, id);
                ps.executeUpdate();
                ps.close();
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
}

