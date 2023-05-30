package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
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
   private static Map<Long, PermissionsHistory> objectHistories = new ConcurrentHashMap<>();

   private PermissionsHistories() {
   }

   public static void loadAll() {
      logger.log(Level.INFO, "Purging permissions history over 6 months old.");
      long start = System.nanoTime();
      long count = 0L;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM PERMISSIONSHISTORY WHERE EVENTDATE<?");
         ps.setLong(1, System.currentTimeMillis() - 14515200000L);
         count = (long)ps.executeUpdate();
      } catch (SQLException var26) {
         logger.log(Level.WARNING, "Failed to load history for permissions.", (Throwable)var26);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.log(Level.INFO, "Purged " + count + " permissions history. That took " + (float)(end - start) / 1000000.0F + " ms.");
      }

      logger.log(Level.INFO, "Loading all permissions history.");
      count = 0L;
      start = System.nanoTime();

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM PERMISSIONSHISTORY ORDER BY OBJECTID, EVENTDATE");

         for(rs = ps.executeQuery(); rs.next(); ++count) {
            long objectId = rs.getLong("OBJECTID");
            long eventDate = rs.getLong("EVENTDATE");
            long playerId = rs.getLong("PLAYERID");
            String performer = rs.getString("PERFORMER");
            String event = rs.getString("EVENT");
            add(objectId, eventDate, playerId, performer, event);
         }
      } catch (SQLException var28) {
         logger.log(Level.WARNING, "Failed to load history for permissions.", (Throwable)var28);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.log(Level.INFO, "Loaded " + count + " permissions history. That took " + (float)(end - start) / 1000000.0F + " ms.");
      }
   }

   public static void moveHistories(long fromId, long toId) {
      Long id = fromId;
      if (objectHistories.containsKey(id)) {
         PermissionsHistory oldHistories = objectHistories.get(id);

         for(PermissionsHistoryEntry phe : oldHistories.getHistoryEvents()) {
            addHistoryEntry(toId, phe.getEventDate(), phe.getPlayerId(), phe.getPlayerName(), phe.getEvent());
         }

         dbRemove(fromId);
      }
   }

   public static PermissionsHistory getPermissionsHistoryFor(long objectId) {
      Long id = objectId;
      if (objectHistories.containsKey(id)) {
         return objectHistories.get(id);
      } else {
         PermissionsHistory ph = new PermissionsHistory();
         objectHistories.put(id, ph);
         return ph;
      }
   }

   private static void add(long objectId, long eventTime, long playerId, String playerName, String event) {
      PermissionsHistory ph = getPermissionsHistoryFor(objectId);
      ph.add(eventTime, playerId, playerName, event);
   }

   public static void addHistoryEntry(long objectId, long eventTime, long playerId, String playerName, String event) {
      add(objectId, eventTime, playerId, playerName, event);
      dbAddHistoryEvent(objectId, eventTime, playerId, playerName, event);
   }

   private static void dbAddHistoryEvent(long objectId, long eventTime, long playerId, String playerName, String event) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("INSERT INTO PERMISSIONSHISTORY(OBJECTID, EVENTDATE, PLAYERID, PERFORMER, EVENT) VALUES(?,?,?,?,?)");
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
      } catch (SQLException var14) {
         logger.log(Level.WARNING, "Failed to add permissions history for object (" + objectId + ")", (Throwable)var14);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void remove(long objectId) {
      Long id = objectId;
      if (objectHistories.containsKey(id)) {
         dbRemove(objectId);
         objectHistories.remove(id);
      }
   }

   private static void dbRemove(long objectId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM PERMISSIONSHISTORY WHERE OBJECTID=?");
         ps.setLong(1, objectId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete permissions history for object " + objectId + ".", (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public String[] getHistory(long objectId, int numevents) {
      Long id = objectId;
      if (objectHistories.containsKey(id)) {
         PermissionsHistory ph = objectHistories.get(id);
         return ph.getHistory(numevents);
      } else {
         return new String[0];
      }
   }
}
