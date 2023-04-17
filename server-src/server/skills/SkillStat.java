/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public final class SkillStat
implements TimeConstants {
    private static Logger logger = Logger.getLogger(SkillStat.class.getName());
    public final Map<Long, Double> stats = new HashMap<Long, Double>();
    @GuardedBy(value="RW_LOCK")
    private static final Map<Integer, SkillStat> allStats = new HashMap<Integer, SkillStat>();
    private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private final String skillName;
    private final int skillnum;
    private static final String loadAllPlayerSkills = "select NUMBER,OWNER,VALUE from SKILLS sk INNER JOIN PLAYERS p ON p.WURMID=sk.OWNER AND p.CURRENTSERVER=? WHERE sk.VALUE>25 ";

    private SkillStat(int num, String name) {
        this.skillName = name;
        this.skillnum = num;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int loadAllStats() {
        Connection dbcon = null;
        int numberSkillsLoaded = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(loadAllPlayerSkills);
            ps.setInt(1, Servers.localServer.id);
            rs = ps.executeQuery();
            while (rs.next()) {
                SkillStat sk = SkillStat.getSkillStatForSkill(rs.getInt("NUMBER"));
                if (sk != null) {
                    sk.stats.put(new Long(rs.getLong("OWNER")), new Double(rs.getDouble("VALUE")));
                }
                ++numberSkillsLoaded;
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading the Skill stats due to " + sqx.getMessage(), sqx);
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
        return numberSkillsLoaded;
    }

    static final void addSkill(int skillNum, String name) {
        RW_LOCK.writeLock().lock();
        try {
            allStats.put(skillNum, new SkillStat(skillNum, name));
        }
        finally {
            RW_LOCK.writeLock().unlock();
        }
    }

    public static final void pollSkills() {
        Thread statsPoller = new Thread("StatsPoller"){

            @Override
            public void run() {
                try {
                    long now = System.currentTimeMillis();
                    int numberSkillsLoaded = SkillStat.loadAllStats();
                    logger.log(Level.WARNING, "Polling " + numberSkillsLoaded + " skills for stats v2 took " + (System.currentTimeMillis() - now) + " ms.");
                }
                catch (RuntimeException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
        };
        statsPoller.start();
    }

    public static final SkillStat getSkillStatForSkill(int num) {
        RW_LOCK.readLock().lock();
        try {
            SkillStat skillStat = allStats.get(num);
            return skillStat;
        }
        finally {
            RW_LOCK.readLock().unlock();
        }
    }
}

