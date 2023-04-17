/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils.logging;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.logging.TileEventDatabaseLogger;
import com.wurmonline.server.utils.logging.WurmLoggable;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TileEvent
implements WurmLoggable,
TimeConstants {
    private static final Logger logger = Logger.getLogger(TileEvent.class.getName());
    private static long lastPruned = System.currentTimeMillis() + 21600000L;
    private static long pruneInterval = 432000000L;
    private static final String PRUNE_ENTRIES = "DELETE FROM TILE_LOG WHERE DATE<?";
    private static final String FIND_ENTRIES_FOR_A_TILE = "SELECT * FROM TILE_LOG WHERE TILEX = ? AND TILEY = ? AND LAYER = ? ORDER BY DATE ASC";
    private static final String INSERT_TILE_LOG = "INSERT INTO TILE_LOG (TILEX,TILEY, LAYER, PERFORMER, ACTION, DATE) VALUES ( ?, ?, ?, ?, ?, ?)";
    private final int tilex;
    private final int tiley;
    private final int layer;
    private final long performer;
    private final int action;
    private final long date;
    private static final TileEventDatabaseLogger tileLogger = new TileEventDatabaseLogger("Tile logger", 500);
    private static final ConcurrentHashMap<Long, TileEvent> playersLog = new ConcurrentHashMap();

    public TileEvent(int _tileX, int _tileY, int _layer, long _performer, int _action) {
        this.tilex = _tileX;
        this.tiley = _tileY;
        this.layer = _layer;
        this.performer = _performer;
        this.action = _action;
        this.date = System.currentTimeMillis();
    }

    public TileEvent(int _tileX, int _tileY, int _layer, long _performer, int _action, long _date) {
        this.tilex = _tileX;
        this.tiley = _tileY;
        this.layer = _layer;
        this.performer = _performer;
        this.action = _action;
        this.date = _date;
    }

    public static TileEventDatabaseLogger getTilelogger() {
        return tileLogger;
    }

    public static void log(int _tileX, int _tileY, int _layer, long _performer, int _action) {
        if (Constants.useTileEventLog) {
            TileEvent lEvent = null;
            try {
                TileEvent oEvent = playersLog.get(_performer);
                if (oEvent != null && oEvent.tilex == _tileX && oEvent.tiley == _tileY && oEvent.layer == _layer && oEvent.action == _action && oEvent.date > System.currentTimeMillis() - 300000L) {
                    return;
                }
                lEvent = new TileEvent(_tileX, _tileY, _layer, _performer, _action);
                playersLog.put(_performer, lEvent);
                tileLogger.addToQueue(lEvent);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Could not add to queue: " + lEvent + " due to " + ex.getMessage(), ex);
            }
        }
    }

    static final int getLogSize() {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void pruneLogEntries() {
        if (System.currentTimeMillis() - lastPruned > pruneInterval) {
            lastPruned = System.currentTimeMillis();
            long cutDate = System.currentTimeMillis() - pruneInterval;
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getLogsDbCon();
                ps = dbcon.prepareStatement(PRUNE_ENTRIES);
                ps.setLong(1, cutDate);
                ps.execute();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to prune log entries.", sqx);
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
    @NonNull
    public static List<TileEvent> getEventsFor(int aTileX, int aTileY, int aLayer) {
        LinkedList<TileEvent> matches = new LinkedList<TileEvent>();
        if (Constants.useTileEventLog) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                dbcon = DbConnector.getLogsDbCon();
                ps = dbcon.prepareStatement(FIND_ENTRIES_FOR_A_TILE);
                ps.setInt(1, aTileX);
                ps.setInt(2, aTileY);
                ps.setInt(3, aLayer);
                rs = ps.executeQuery();
                while (rs.next()) {
                    TileEvent lEvent = new TileEvent(rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getInt("LAYER"), rs.getLong("PERFORMER"), rs.getInt("ACTION"), rs.getLong("DATE"));
                    matches.add(lEvent);
                }
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to load log entry.", sqx);
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
        return matches;
    }

    public int getTileX() {
        return this.tilex;
    }

    public int getTileY() {
        return this.tiley;
    }

    public int getLayer() {
        return this.layer;
    }

    public int getAction() {
        return this.action;
    }

    public long getPerformer() {
        return this.performer;
    }

    public long getDate() {
        return this.date;
    }

    @Override
    public String getDatabaseInsertStatement() {
        return INSERT_TILE_LOG;
    }

    @Override
    public String toString() {
        return "TileEvent [tilex=" + this.tilex + ", tiley=" + this.tiley + ", layer=" + this.layer + ", performer=" + this.performer + ", action=" + this.action + ", date=" + this.date + "]";
    }
}

