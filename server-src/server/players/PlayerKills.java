/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerKill;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerKills {
    private final long wurmid;
    private static final Logger logger = Logger.getLogger(PlayerKills.class.getName());
    private static final String GET_KILLS = "SELECT * FROM KILLS WHERE WURMID=?";
    private final Map<Long, PlayerKill> kills = new HashMap<Long, PlayerKill>();

    public PlayerKills(long _wurmId) {
        this.wurmid = _wurmId;
        this.load();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private void load() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_KILLS);
            ps.setLong(1, this.wurmid);
            rs = ps.executeQuery();
            while (rs.next()) {
                Long vid = new Long(rs.getLong("VICTIM"));
                PlayerKill pk = this.kills.get(vid);
                if (pk != null) {
                    pk.addKill(rs.getLong("KILLTIME"), rs.getString("VICTIMNAME"), true);
                    continue;
                }
                this.kills.put(vid, new PlayerKill(vid, rs.getLong("KILLTIME"), rs.getString("VICTIMNAME"), 1));
            }
        }
        catch (SQLException ex) {
            logger.log(Level.INFO, "Failed to load kills for " + this.wurmid, ex);
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        catch (Exception ex2) {
            logger.log(Level.INFO, "Failed to load kills for " + this.wurmid, ex2);
            {
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    public long getLastKill(long victimId) {
        PlayerKill pk = this.kills.get(victimId);
        if (pk != null) {
            return pk.getLastKill();
        }
        return 0L;
    }

    public long getNumKills(long victimId) {
        PlayerKill pk = this.kills.get(victimId);
        if (pk != null) {
            return pk.getNumKills();
        }
        return 0L;
    }

    public void addKill(long victimId, String victimName) {
        Long vid = new Long(victimId);
        PlayerKill pk = this.kills.get(vid);
        if (pk != null) {
            pk.kill(this.wurmid, victimName);
        } else {
            Achievements ach;
            PlayerInfo pinf;
            pk = new PlayerKill(victimId, System.currentTimeMillis(), victimName, 0);
            if (Servers.localServer.isChallengeServer() && (pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.wurmid)) != null && (ach = Achievements.getAchievementObject(this.wurmid)) != null && ach.getAchievement(369) != null) {
                ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.PLAYERKILLS.getEnumtype(), 1.0f);
                ChallengeSummary.addToScore(pinf, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), 10.0f);
            }
            pk.kill(this.wurmid, victimName);
            this.kills.put(vid, pk);
        }
    }

    public boolean isOverKilling(long victimId) {
        Long vid = new Long(victimId);
        PlayerKill pk = this.kills.get(vid);
        if (pk != null) {
            return pk.isOverkilling();
        }
        return false;
    }

    public int getNumberOfKills() {
        return this.kills.size();
    }
}

