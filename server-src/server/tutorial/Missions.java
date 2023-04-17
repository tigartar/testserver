/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Missions
implements MiscConstants {
    private static final Map<Integer, Mission> missions = new HashMap<Integer, Mission>();
    private static final String LOADALLMISSIONS = "SELECT * FROM MISSIONS";
    private static final String DELETEMISSION = "DELETE FROM MISSIONS WHERE ID=?";
    private static Logger logger = Logger.getLogger(Missions.class.getName());
    public static final byte CREATOR_UNSET = 0;
    public static final byte CREATOR_GM = 1;
    public static final byte CREATOR_SYSTEM = 2;
    public static final byte CREATOR_PLAYER = 3;
    public static final int SHOW_ALL = 0;
    public static final int SHOW_WITH = 1;
    public static final int SHOW_NONE = 2;

    private Missions() {
    }

    public static int getNumMissions() {
        return missions.size();
    }

    public static Mission[] getAllMissions() {
        return missions.values().toArray(new Mission[missions.size()]);
    }

    public static Mission[] getFilteredMissions(Creature creature, int showTriggers, boolean incInactive, boolean dontListMine, boolean listMineOnly, long listForUser, String groupName, boolean onlyCurrent, long currentTargetId) {
        HashSet<Mission> missionSet = new HashSet<Mission>();
        for (Mission mission : missions.values()) {
            boolean userMatch;
            boolean own = mission.getOwnerId() == creature.getWurmId();
            boolean show = creature.getPower() > 0 || own;
            boolean bl = userMatch = mission.getOwnerId() == listForUser;
            if (own) {
                if (dontListMine) {
                    show = false;
                }
            } else if (listMineOnly) {
                show = false;
                if (listForUser != -10L && userMatch) {
                    show = true;
                }
            } else if (listForUser != -10L) {
                show = false;
                if (userMatch) {
                    show = true;
                }
            }
            if (show && showTriggers == 2 && mission.hasTriggers()) {
                show = false;
            }
            if (show && showTriggers == 1 && !mission.hasTriggers()) {
                show = false;
            }
            if (show && !incInactive && mission.isInactive()) {
                show = false;
            }
            if (show && mission.getCreatorType() == 2 && creature.getPower() < 2) {
                show = false;
            }
            if (show && !groupName.isEmpty() && !mission.getGroupName().equals(groupName)) {
                show = false;
            }
            if (show && onlyCurrent && !mission.hasTargetOf(currentTargetId, creature)) {
                show = false;
            }
            if (!show || mission.getCreatorType() == 2 && mission.getCreatedDate() <= System.currentTimeMillis() - 2419200000L) continue;
            missionSet.add(mission);
        }
        return missionSet.toArray(new Mission[missionSet.size()]);
    }

    public static void addMission(Mission m) {
        missions.put(m.getId(), m);
    }

    public static Mission getMissionWithId(int mid) {
        return missions.get(mid);
    }

    public static Mission[] getMissionsWithTargetId(long tid, Creature performer) {
        MissionTrigger[] triggers = MissionTriggers.getAllTriggers();
        HashSet<Mission> toReturn = new HashSet<Mission>();
        for (MissionTrigger mt : triggers) {
            Mission m;
            if (mt.getTarget() != tid || (m = Missions.getMissionWithId(mt.getMissionRequired())) == null || m.getCreatorType() == 2 && performer.getPower() < 5 || performer.getPower() <= 0 && !m.getMissionCreatorName().equals(performer.getName())) continue;
            toReturn.add(m);
        }
        return toReturn.toArray(new Mission[toReturn.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deleteMission(int misid) {
        Missions.removeMission(misid);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETEMISSION);
            ps.setInt(1, misid);
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
    private static void loadAllMissions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOADALLMISSIONS);
            rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp st = new Timestamp(System.currentTimeMillis());
                try {
                    String lastModified = rs.getString("LASTMODIFIEDDATE");
                    if (lastModified != null) {
                        st = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastModified).getTime());
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                Mission m = new Mission(rs.getInt("ID"), rs.getString("NAME"), rs.getString("INSTRUCTION"), rs.getBoolean("INACTIVE"), rs.getString("CREATOR"), rs.getString("CREATEDDATE"), rs.getString("LASTMODIFIER"), st, rs.getInt("MAXTIMESECS"), rs.getBoolean("MAYBERESTARTED"));
                m.setCreatorType(rs.getByte("CREATORTYPE"));
                m.setOwnerId(rs.getLong("CREATORID"));
                m.setSecondChance(rs.getBoolean("SECONDCHANCE"));
                m.setFailOnDeath(rs.getBoolean("FAILONDEATH"));
                m.setGroupName(rs.getString("GROUP_NAME"));
                m.setIsHidden(rs.getBoolean("HIDDEN"));
                Missions.addMission(m);
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

    protected static void removeMission(int mid) {
        missions.remove(mid);
    }

    static {
        try {
            Missions.loadAllMissions();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading all Missions", ex);
        }
    }
}

