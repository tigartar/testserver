package com.wurmonline.server;

import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public final class HistoryManager {
   private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
   private static final String ADD_HISTORY = "INSERT INTO HISTORY(EVENTDATE,SERVER,PERFORMER,EVENT) VALUES (?,?,?,?)";
   private static final String GET_HISTORY = "SELECT EVENTDATE, SERVER, PERFORMER, EVENT FROM HISTORY WHERE SERVER=? ORDER BY EVENTDATE DESC";
   @GuardedBy("HISTORY_RW_LOCK")
   private static final LinkedList<HistoryEvent> HISTORY = new LinkedList<>();
   private static final ReentrantReadWriteLock HISTORY_RW_LOCK = new ReentrantReadWriteLock();

   private HistoryManager() {
   }

   static HistoryEvent[] getHistoryEvents() {
      HISTORY_RW_LOCK.readLock().lock();

      HistoryEvent[] var0;
      try {
         var0 = HISTORY.toArray(new HistoryEvent[HISTORY.size()]);
      } finally {
         HISTORY_RW_LOCK.readLock().unlock();
      }

      return var0;
   }

   public static String[] getHistory(int numevents) {
      String[] hist = new String[0];
      HISTORY_RW_LOCK.readLock().lock();

      int lHistorySize;
      try {
         lHistorySize = HISTORY.size();
      } finally {
         HISTORY_RW_LOCK.readLock().unlock();
      }

      if (lHistorySize > 0) {
         int numbersToFetch = Math.min(numevents, lHistorySize);
         hist = new String[numbersToFetch];
         HistoryEvent[] events = getHistoryEvents();

         for(int x = 0; x < numbersToFetch; ++x) {
            hist[x] = events[x].getLongDesc();
         }
      }

      return hist;
   }

   static void loadHistory() {
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      HISTORY_RW_LOCK.writeLock().lock();

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT EVENTDATE, SERVER, PERFORMER, EVENT FROM HISTORY WHERE SERVER=? ORDER BY EVENTDATE DESC");
         ps.setInt(1, Servers.localServer.id);
         rs = ps.executeQuery();

         while(rs.next()) {
            HISTORY.add(new HistoryEvent(rs.getLong("EVENTDATE"), rs.getString("PERFORMER"), rs.getString("EVENT"), rs.getInt("SERVER")));
         }
      } catch (SQLException var10) {
         logger.log(
            Level.WARNING, "Problem loading History for loacl server id: " + Servers.localServer.id + " due to " + var10.getMessage(), (Throwable)var10
         );
      } finally {
         HISTORY_RW_LOCK.writeLock().unlock();
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
         logger.info("Loaded " + HISTORY.size() + " HISTORY events from the database took " + lElapsedTime + " ms");
      }
   }

   public static void addHistory(String performerName, String event) {
      addHistory(performerName, event, true);
   }

   public static void addHistory(String performerName, String event, boolean twit) {
      HISTORY_RW_LOCK.writeLock().lock();

      try {
         HISTORY.addFirst(new HistoryEvent(System.currentTimeMillis(), performerName, event, Servers.localServer.id));
      } finally {
         HISTORY_RW_LOCK.writeLock().unlock();
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("INSERT INTO HISTORY(EVENTDATE,SERVER,PERFORMER,EVENT) VALUES (?,?,?,?)");
         ps.setLong(1, System.currentTimeMillis());
         ps.setInt(2, Servers.localServer.id);
         ps.setString(3, performerName);
         ps.setString(4, event);
         ps.executeUpdate();
      } catch (SQLException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      if (twit) {
         Server.getInstance().twitLocalServer(performerName + " " + event);
      }
   }
}
