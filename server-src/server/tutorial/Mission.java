/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Mission
implements Comparable<Mission> {
    private static final Logger logger = Logger.getLogger(Mission.class.getName());
    private static final String UPDATE_MISSION = "UPDATE MISSIONS SET NAME=?,INSTRUCTION=?,INACTIVE=?,CREATOR=?,CREATEDDATE=?,LASTMODIFIER=?,MAXTIMESECS=?,SECONDCHANCE=?,MAYBERESTARTED=?,FAILONDEATH=?,GROUP_NAME=?,HIDDEN=? WHERE ID=?";
    private static final String CREATE_MISSION = "INSERT INTO MISSIONS (NAME,INSTRUCTION,INACTIVE,CREATOR,CREATEDDATE,LASTMODIFIER,MAXTIMESECS,SECONDCHANCE,MAYBERESTARTED,CREATORID,CREATORTYPE,FAILONDEATH,GROUP_NAME,HIDDEN) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String DELETE_MISSION = "DELETE FROM MISSIONS WHERE ID=?";
    private int id;
    private String name;
    private String groupName = "";
    private String instruction = "unknown";
    private boolean inActive = false;
    private boolean isHidden = false;
    private String missionCreatorName;
    private String createdDate;
    private String lastModifierName;
    private Timestamp lastModifiedDate;
    private int maxTimeSeconds = 0;
    private boolean hasSecondChance = false;
    private boolean mayBeRestarted = false;
    private long ownerId = 0L;
    private byte creatorType = 0;
    private boolean failOnDeath = false;

    public Mission(String aMissionCreatorName, String aLastModifierName) {
        this.missionCreatorName = aMissionCreatorName;
        this.lastModifierName = aLastModifierName;
    }

    Mission(int aId, String aName, String aInstruction, boolean aInActive, String aMissionCreatorName, String aCreatedDate, String aLastModifierName, Timestamp aLastModifiedDate, int aMaxTimeSeconds, boolean aMayBeRestarted) {
        this.id = aId;
        this.name = aName;
        this.instruction = aInstruction;
        this.inActive = aInActive;
        this.missionCreatorName = aMissionCreatorName;
        this.createdDate = aCreatedDate;
        this.lastModifierName = aLastModifierName;
        this.lastModifiedDate = aLastModifiedDate;
        this.maxTimeSeconds = aMaxTimeSeconds;
        this.mayBeRestarted = aMayBeRestarted;
    }

    void setId(int aId) {
        this.id = aId;
    }

    public int getId() {
        return this.id;
    }

    public String getInstruction() {
        return this.instruction;
    }

    public String getName() {
        return this.name;
    }

    public String getMissionCreatorName() {
        return this.missionCreatorName;
    }

    public String getOwnerName() {
        if (this.getCreatorType() == 2) {
            return "System";
        }
        return PlayerInfoFactory.getPlayerName(this.getOwnerId());
    }

    public String getLastModifiedString() {
        return this.getLastModifierName() + ", " + this.getLastModifiedDate();
    }

    public String getLastModifierName() {
        return this.lastModifierName;
    }

    public boolean hasSecondChance() {
        return this.hasSecondChance;
    }

    public void setSecondChance(boolean restart) {
        this.hasSecondChance = restart;
    }

    public boolean isInactive() {
        return this.inActive;
    }

    public void setInactive(boolean inactive) {
        this.inActive = inactive;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    public void setIsHidden(boolean ishidden) {
        this.isHidden = ishidden;
    }

    public void setMaxTimeSeconds(int seconds) {
        this.maxTimeSeconds = seconds;
    }

    public int getMaxTimeSeconds() {
        return this.maxTimeSeconds;
    }

    public void setCreatorType(byte aCreatorType) {
        this.creatorType = aCreatorType;
    }

    public byte getCreatorType() {
        return this.creatorType;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setOwnerId(long aWurmId) {
        this.ownerId = aWurmId;
    }

    public void setMayBeRestarted(boolean restarted) {
        this.mayBeRestarted = restarted;
    }

    public boolean mayBeRestarted() {
        return this.mayBeRestarted;
    }

    public void setFailOnDeath(boolean fail) {
        this.failOnDeath = fail;
    }

    public boolean isFailOnDeath() {
        return this.failOnDeath;
    }

    public long getOwnerId() {
        return this.ownerId;
    }

    public void setInstruction(String n) {
        this.instruction = n;
        this.instruction = this.instruction.substring(0, Math.min(this.instruction.length(), 400));
    }

    public void setName(String n) {
        this.name = n;
        this.name = this.name.substring(0, Math.min(this.name.length(), 100));
    }

    @Nullable
    public void setGroupName(String gn) {
        this.groupName = gn == null ? "" : gn.substring(0, Math.min(gn.length(), 20));
    }

    public void setMissionCreatorName(String cn) {
        this.missionCreatorName = cn;
        this.missionCreatorName = this.missionCreatorName.substring(0, Math.min(this.missionCreatorName.length(), 40));
    }

    public void setLastModifierName(String mn) {
        this.lastModifierName = mn;
        this.lastModifierName = this.lastModifierName.substring(0, Math.min(this.lastModifierName.length(), 40));
    }

    void setCreatedDate(String aCreatedDate) {
        this.createdDate = aCreatedDate;
    }

    public long getCreatedDate() {
        DateFormat formatter = DateFormat.getDateInstance(2);
        try {
            Date date = formatter.parse(this.createdDate);
            return date.getTime();
        }
        catch (ParseException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return this.lastModifiedDate.getTime();
        }
    }

    void setLastModifiedDate(Timestamp aLastModifiedDate) {
        this.lastModifiedDate = aLastModifiedDate;
    }

    public long getLastModifiedAsLong() {
        return this.lastModifiedDate.getTime();
    }

    public String getLastModifiedDate() {
        if (this.lastModifiedDate != null) {
            return DateFormat.getDateInstance(2).format(this.lastModifiedDate);
        }
        return "";
    }

    public final boolean hasTargetOf(long currentTargetId, Creature performer) {
        MissionTrigger[] triggers = MissionTriggers.getAllTriggers();
        if (!(this.getCreatorType() == 2 && performer.getPower() < 5 || performer.getPower() <= 0 && !this.getMissionCreatorName().equals(performer.getName()))) {
            for (MissionTrigger mt : triggers) {
                if (mt.getMissionRequired() != this.id || mt.getTarget() != currentTargetId) continue;
                return true;
            }
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void update() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(UPDATE_MISSION);
            ps.setString(1, this.name);
            ps.setString(2, this.instruction);
            ps.setBoolean(3, this.inActive);
            ps.setString(4, this.missionCreatorName);
            ps.setString(5, this.createdDate);
            this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
            ps.setString(6, this.lastModifierName);
            ps.setInt(7, this.maxTimeSeconds);
            ps.setBoolean(8, this.hasSecondChance);
            ps.setBoolean(9, this.mayBeRestarted);
            ps.setBoolean(10, this.failOnDeath);
            ps.setString(11, this.groupName);
            ps.setBoolean(12, this.isHidden);
            ps.setInt(13, this.id);
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
    public final void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(CREATE_MISSION, 1);
            ps.setString(1, this.name);
            ps.setString(2, this.instruction);
            ps.setBoolean(3, this.inActive);
            ps.setString(4, this.missionCreatorName);
            this.createdDate = DateFormat.getDateInstance(2).format(new Timestamp(System.currentTimeMillis()));
            this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
            ps.setString(5, this.createdDate);
            ps.setString(6, this.lastModifierName);
            ps.setInt(7, this.maxTimeSeconds);
            ps.setBoolean(8, this.hasSecondChance);
            ps.setBoolean(9, this.mayBeRestarted);
            ps.setLong(10, this.ownerId);
            ps.setByte(11, this.creatorType);
            ps.setBoolean(12, this.failOnDeath);
            ps.setString(13, this.groupName);
            ps.setBoolean(14, this.isHidden);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                this.id = rs.getInt(1);
            }
            logger.log(Level.INFO, "Mission " + this.name + " (" + this.id + ") created at " + this.createdDate);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void destroy() {
        Missions.removeMission(this.id);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_MISSION);
            ps.setInt(1, this.id);
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

    @Override
    public int compareTo(Mission aMission) {
        return this.getName().compareTo(aMission.getName());
    }

    public String toString() {
        return "Mission [createdDate=" + this.createdDate + ", creatorType=" + this.creatorType + ", hasSecondChance=" + this.hasSecondChance + ", id=" + this.id + ", inActive=" + this.inActive + ", instruction=" + this.instruction + ", lastModifiedDate=" + this.lastModifiedDate + ", lastModifierName=" + this.lastModifierName + ", maxTimeSeconds=" + this.maxTimeSeconds + ", mayBeRestarted=" + this.mayBeRestarted + ", missionCreatorName=" + this.missionCreatorName + ", name=" + this.name + ", ownerId=" + this.ownerId + "]";
    }

    public String getRewards() {
        EpicMission em = EpicServerStatus.getEpicMissionForMission(this.id);
        if (em != null) {
            return em.getRewards();
        }
        return "Unknown";
    }

    public byte getDifficulty() {
        EpicMission em = EpicServerStatus.getEpicMissionForMission(this.id);
        if (em != null) {
            return (byte)em.getDifficulty();
        }
        return -10;
    }

    public MissionTrigger[] getTriggers() {
        return MissionTriggers.getMissionTriggers(this.id);
    }

    public boolean hasTriggers() {
        return MissionTriggers.hasMissionTriggers(this.id);
    }
}

