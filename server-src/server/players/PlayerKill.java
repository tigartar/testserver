/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerKill
implements TimeConstants {
    private static final Logger logger = Logger.getLogger(PlayerKill.class.getName());
    private static final String ADD_KILL = "INSERT INTO KILLS(WURMID,VICTIM, VICTIMNAME,KILLTIME) VALUES(?,?,?,?)";
    private final long victimId;
    private long lastKilled = -10L;
    private int timesKilled = 0;
    private int timesKilledSinceRestart = 0;
    private String name = "";

    PlayerKill(long _victimId, long _lastKilled, String _name, int kills) {
        this.victimId = _victimId;
        this.name = _name;
        this.lastKilled = _lastKilled;
        this.timesKilled = kills;
    }

    void addKill(long time, String victimname, boolean loading) {
        ++this.timesKilled;
        if (!loading) {
            ++this.timesKilledSinceRestart;
        }
        if (time > this.lastKilled) {
            this.lastKilled = time;
            this.name = victimname;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void kill(long killerId, String victimname) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(ADD_KILL);
            ps.setLong(1, killerId);
            ps.setLong(2, this.victimId);
            ps.setString(3, victimname);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to add kill for  " + killerId, ex);
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
        this.addKill(System.currentTimeMillis(), victimname, false);
        if (this.isOverkilling()) {
            String kname = String.valueOf(killerId);
            try {
                kname = Players.getInstance().getNameFor(killerId);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.INFO, "weird " + kname + " not online while killing " + killerId);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            logger.log(Level.INFO, kname + " overkilling " + this.name + " since restart: " + this.timesKilledSinceRestart + " overall: " + this.timesKilled);
        }
    }

    long getLastKill() {
        return this.lastKilled;
    }

    int getNumKills() {
        return this.timesKilled;
    }

    boolean isOverkilling() {
        return this.lastKilled > System.currentTimeMillis() - 21600000L && this.timesKilledSinceRestart > 3 || this.timesKilled > 20;
    }
}

