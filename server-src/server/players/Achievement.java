/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Achievement
implements MiscConstants {
    private final Timestamp dateAchieved;
    private final int achievement;
    private int counter = 0;
    private long holder = 0L;
    private int localId = -1;
    private static final String INSERT = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER) VALUES (?,?,?)" : "INSERT IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER) VALUES (?,?,?)";
    private static final String INSERT_TRANSFER = DbConnector.isUseSqlite() ? "INSERT OR IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER,ADATE) VALUES (?,?,?,?)" : "INSERT IGNORE INTO ACHIEVEMENTS (PLAYER,ACHIEVEMENT,COUNTER,ADATE) VALUES (?,?,?,?)";
    private static final String UPDATE_COUNTER = "UPDATE ACHIEVEMENTS SET COUNTER=? WHERE ID=?";
    private static final Logger logger = Logger.getLogger(Achievement.class.getName());
    private static final ConcurrentHashMap<Integer, AchievementTemplate> templates = new ConcurrentHashMap();
    protected static final List<AchievementTemplate> personalGoalSilverTemplates = new LinkedList<AchievementTemplate>();
    protected static final List<AchievementTemplate> personalGoalGoldTemplates = new LinkedList<AchievementTemplate>();
    protected static final List<AchievementTemplate> personalGoalDiamondTemplates = new LinkedList<AchievementTemplate>();

    public Achievement(int aAchievement, Timestamp date, long achiever, int timesTriggered, int localid) {
        this.achievement = aAchievement;
        this.setHolder(achiever);
        this.dateAchieved = date;
        this.counter = timesTriggered;
        this.localId = localid;
        if (Achievement.getTemplate(aAchievement) == null) {
            Achievement.addTemplate(new AchievementTemplate(aAchievement, "Unknown", false));
        }
    }

    public final Timestamp getDateAchieved() {
        return this.dateAchieved;
    }

    public final int getAchievement() {
        return this.achievement;
    }

    public final int getCounter() {
        return this.counter;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    final int[] setCounter(int aCounter) {
        block4: {
            if (this.counter == aCounter) break block4;
            this.counter = aCounter;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = dbcon.prepareStatement(UPDATE_COUNTER);
                ps.setInt(1, this.counter);
                ps.setInt(2, this.localId);
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save achievement " + this.achievement + " counter " + aCounter + " for " + this.holder);
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
            return this.getTriggeredAchievements();
        }
        return EMPTY_INT_ARRAY;
    }

    final int[] getTriggeredAchievements() {
        return this.getTemplate().getAchievementsTriggered();
    }

    public final AchievementTemplate getTemplate() {
        return Achievement.getTemplate(this.achievement);
    }

    public final long getHolder() {
        return this.holder;
    }

    private final void setHolder(long aHolder) {
        this.holder = aHolder;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void create(boolean transfer) {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        block7: {
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getPlayerDbCon();
                ps = transfer ? dbcon.prepareStatement(INSERT_TRANSFER, 1) : dbcon.prepareStatement(INSERT, 1);
                ps.setLong(1, this.holder);
                ps.setInt(2, this.achievement);
                ps.setInt(3, this.counter);
                if (transfer) {
                    if (DbConnector.isUseSqlite()) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String ts = sdf.format(this.getDateAchieved());
                        ps.setString(4, ts);
                    } else {
                        ps.setTimestamp(4, this.getDateAchieved());
                    }
                }
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (!rs.next()) break block7;
                this.localId = rs.getInt(1);
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to save achievement " + this.achievement + " for " + this.holder, ex);
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
        Achievements.addAchievement(this, true);
    }

    public final void sendNewAchievement(Creature creature) {
        creature.getCommunicator().sendAchievement(this, true);
    }

    public final void sendUpdateAchievement(Creature creature) {
        creature.getCommunicator().sendAchievement(this, false);
    }

    public final void sendUpdatePersonalGoal(Creature creature) {
        creature.getCommunicator().updatePersonalGoal(this, true);
    }

    final int getTriggerOnCounter() {
        return this.getTemplate().getTriggerOnCounter();
    }

    static final void addTemplate(AchievementTemplate template) {
        templates.put(template.getNumber(), template);
        if (template.isPersonalGoal()) {
            if (template.getType() == 3) {
                personalGoalSilverTemplates.add(template);
            } else if (template.getType() == 4) {
                personalGoalGoldTemplates.add(template);
            } else if (template.getType() == 5) {
                personalGoalDiamondTemplates.add(template);
            }
        }
    }

    static final void removeTemplate(AchievementTemplate template) {
        templates.remove(template.getNumber());
    }

    @Nullable
    public static final AchievementTemplate getTemplate(int number) {
        return templates.get(number);
    }

    @Nullable
    public static final AchievementTemplate getTemplate(String name) {
        for (AchievementTemplate achievement : templates.values()) {
            if (!achievement.getName().equalsIgnoreCase(name)) continue;
            return achievement;
        }
        return null;
    }

    public final boolean isInVisible() {
        return this.getTemplate().isInvisible();
    }

    public final boolean isPlaySoundOnUpdate() {
        return this.getTemplate().isPlaySoundOnUpdate();
    }

    public final boolean isOneTimer() {
        return this.getTemplate().isOneTimer();
    }

    public static final LinkedList<AchievementTemplate> getSteelAchievements(Creature creature) {
        LinkedList<AchievementTemplate> toReturn = new LinkedList<AchievementTemplate>();
        for (AchievementTemplate achievement : templates.values()) {
            if (achievement.getType() != 2 || creature.getPower() <= 0 && !creature.getName().equalsIgnoreCase(achievement.getCreator().toLowerCase())) continue;
            toReturn.add(achievement);
        }
        return toReturn;
    }

    public static final AchievementTemplate getRandomPersonalGoldAchievement(Random personalRandom) {
        int num = personalRandom.nextInt(personalGoalGoldTemplates.size());
        return personalGoalGoldTemplates.get(num);
    }

    public static final AchievementTemplate getRandomPersonalSilverAchievement(Random personalRandom) {
        int num = personalRandom.nextInt(personalGoalSilverTemplates.size());
        return personalGoalSilverTemplates.get(num);
    }

    public static final AchievementTemplate getRandomPersonalDiamondAchievement(Random personalRandom) {
        int num = personalRandom.nextInt(personalGoalDiamondTemplates.size());
        return personalGoalDiamondTemplates.get(num);
    }

    public static ConcurrentHashMap<Integer, AchievementTemplate> getAllTemplates() {
        return templates;
    }

    public static float getAchievementProgress(long wurmId, int achievementNum) {
        AchievementTemplate t = Achievement.getTemplate(achievementNum);
        if (t == null) {
            return 0.0f;
        }
        return t.getProgressFor(wurmId);
    }
}

