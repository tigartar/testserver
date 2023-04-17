/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.PermissionsHistory;
import com.wurmonline.server.players.PermissionsHistoryEntry;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PermissionsHistories {
    private static final Logger logger = Logger.getLogger(PermissionsHistories.class.getName());
    private static final String GET_HISTORY = "SELECT * FROM PERMISSIONSHISTORY ORDER BY OBJECTID, EVENTDATE";
    private static final String ADD_HISTORY = "INSERT INTO PERMISSIONSHISTORY(OBJECTID, EVENTDATE, PLAYERID, PERFORMER, EVENT) VALUES(?,?,?,?,?)";
    private static final String DELETE_HISTORY = "DELETE FROM PERMISSIONSHISTORY WHERE OBJECTID=?";
    private static final String PURGE_HISTORY = "DELETE FROM PERMISSIONSHISTORY WHERE EVENTDATE<?";
    private static Map<Long, PermissionsHistory> objectHistories = new ConcurrentHashMap<Long, PermissionsHistory>();

    private PermissionsHistories() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAll() {
        long end;
        logger.log(Level.INFO, "Purging permissions history over 6 months old.");
        long start = System.nanoTime();
        long count = 0L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(PURGE_HISTORY);
            ps.setLong(1, System.currentTimeMillis() - 14515200000L);
            count = ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load history for permissions.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end2 = System.nanoTime();
                logger.log(Level.INFO, "Purged " + count + " permissions history. That took " + (float)(end2 - start) / 1000000.0f + " ms.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            end = System.nanoTime();
            logger.log(Level.INFO, "Purged " + count + " permissions history. That took " + (float)(end - start) / 1000000.0f + " ms.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        end = System.nanoTime();
        logger.log(Level.INFO, "Purged " + count + " permissions history. That took " + (float)(end - start) / 1000000.0f + " ms.");
        logger.log(Level.INFO, "Loading all permissions history.");
        count = 0L;
        start = System.nanoTime();
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_HISTORY);
            rs = ps.executeQuery();
            while (rs.next()) {
                long objectId = rs.getLong("OBJECTID");
                long eventDate = rs.getLong("EVENTDATE");
                long playerId = rs.getLong("PLAYERID");
                String performer = rs.getString("PERFORMER");
                String event = rs.getString("EVENT");
                PermissionsHistories.add(objectId, eventDate, playerId, performer, event);
                ++count;
            }
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to load history for permissions.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end3 = System.nanoTime();
                logger.log(Level.INFO, "Loaded " + count + " permissions history. That took " + (float)(end3 - start) / 1000000.0f + " ms.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            end = System.nanoTime();
            logger.log(Level.INFO, "Loaded " + count + " permissions history. That took " + (float)(end - start) / 1000000.0f + " ms.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        end = System.nanoTime();
        logger.log(Level.INFO, "Loaded " + count + " permissions history. That took " + (float)(end - start) / 1000000.0f + " ms.");
    }

    public static void moveHistories(long fromId, long toId) {
        Long id = fromId;
        if (objectHistories.containsKey(id)) {
            PermissionsHistory oldHistories = objectHistories.get(id);
            for (PermissionsHistoryEntry phe : oldHistories.getHistoryEvents()) {
                PermissionsHistories.addHistoryEntry(toId, phe.getEventDate(), phe.getPlayerId(), phe.getPlayerName(), phe.getEvent());
            }
            PermissionsHistories.dbRemove(fromId);
        }
    }

    public static PermissionsHistory getPermissionsHistoryFor(long objectId) {
        Long id = objectId;
        if (objectHistories.containsKey(id)) {
            return objectHistories.get(id);
        }
        PermissionsHistory ph = new PermissionsHistory();
        objectHistories.put(id, ph);
        return ph;
    }

    private static void add(long objectId, long eventTime, long playerId, String playerName, String event) {
        PermissionsHistory ph = PermissionsHistories.getPermissionsHistoryFor(objectId);
        ph.add(eventTime, playerId, playerName, event);
    }

    public static void addHistoryEntry(long objectId, long eventTime, long playerId, String playerName, String event) {
        PermissionsHistories.add(objectId, eventTime, playerId, playerName, event);
        PermissionsHistories.dbAddHistoryEvent(objectId, eventTime, playerId, playerName, event);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbAddHistoryEvent(long objectId, long eventTime, long playerId, String playerName, String event) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(ADD_HISTORY);
            String newEvent = event.replace("\"", "'");
            if (newEvent.length() > 255) {
                newEvent = newEvent.substring(0, 250) + "...";
            }
            ps.setLong(1, objectId);
            ps.setLong(2, eventTime);
            ps.setLong(3, playerId);
            ps.setString(4, playerName);
            ps.setString(5, newEvent);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to add permissions history for object (" + objectId + ")", ex);
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

    public static void remove(long objectId) {
        Long id = objectId;
        if (objectHistories.containsKey(id)) {
            PermissionsHistories.dbRemove(objectId);
            objectHistories.remove(id);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbRemove(long objectId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETE_HISTORY);
            ps.setLong(1, objectId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to delete permissions history for object " + objectId + ".", ex);
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

    public String[] getHistory(long objectId, int numevents) {
        Long id = objectId;
        if (objectHistories.containsKey(id)) {
            PermissionsHistory ph = objectHistories.get(id);
            return ph.getHistory(numevents);
        }
        return new String[0];
    }
}

