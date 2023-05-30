package com.wurmonline.server.utils.logging;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.TimeConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileLog implements TimeConstants, WurmLoggable {
   private static Logger logger = Logger.getLogger(TileLog.class.getName());
   static long lastPruned = 0L;
   static int numBatchEvents = 50;
   static long pruneInterval = 432000000L;
   private static PreparedStatement lastmPS = null;
   private static int lastmPSCount = 0;
   public static int overallLastmPSCount = 0;
   public static final String LOAD_ALL_ENTRIES = "SELECT * FROM TILE_LOG";
   public static final String PRUNE_ENTRIES = "DELETE FROM TILE_LOG WHERE DATE<?";
   static final String INSERT_TILE_LOG = "INSERT INTO TILE_LOG (TILEX,TILEY, LAYER, PERFORMER, ACTION, DATE) VALUES ( ?, ?, ?, ?, ?, ?)";
   int tilex;
   int tiley;
   int layer;
   long performer;
   int action;
   long date;
   public static final LinkedList<TileLog> logEntries = new LinkedList<>();

   public TileLog(int _tx, int _ty, int _layer, long _performer, int _action, boolean load) {
      this.tilex = _tx;
      this.tiley = _ty;
      this.layer = _layer;
      this.performer = _performer;
      this.action = _action;
      if (!load) {
         this.date = System.currentTimeMillis();
         this.save();
      }

      logEntries.add(this);
   }

   public static final int getLogSize() {
      return logEntries.size();
   }

   public void setDate(long newDate) {
      this.date = newDate;
   }

   public static void clearBatches() {
      try {
         if (lastmPS != null) {
            int[] x = lastmPS.executeBatch();
            logger.log(Level.INFO, "Saved tile log batch size " + x.length);
            lastmPS.close();
            lastmPS = null;
            lastmPSCount = 0;
         }
      } catch (SQLException var1) {
         logger.log(Level.WARNING, var1.getMessage(), (Throwable)var1);
      }
   }

   public void save() {
      try {
         if (lastmPS == null) {
            Connection dbcon = DbConnector.getLogsDbCon();
            lastmPS = dbcon.prepareStatement("INSERT INTO TILE_LOG (TILEX,TILEY, LAYER, PERFORMER, ACTION, DATE) VALUES ( ?, ?, ?, ?, ?, ?)");
         }

         lastmPS.setInt(1, this.getTileX());
         lastmPS.setInt(2, this.getTileY());
         lastmPS.setInt(3, this.getLayer());
         lastmPS.setLong(4, this.getPerformer());
         lastmPS.setInt(5, this.getAction());
         lastmPS.setLong(6, this.getDate());
         lastmPS.addBatch();
         ++overallLastmPSCount;
         ++lastmPSCount;
         if (lastmPSCount > numBatchEvents) {
            long checkms = System.currentTimeMillis();
            lastmPS.executeBatch();
            lastmPS.close();
            lastmPS = null;
            if (System.currentTimeMillis() - checkms > 300L || logger.isLoggable(Level.FINEST)) {
               logger.log(Level.WARNING, "TileLog batch took " + (System.currentTimeMillis() - checkms) + " ms for " + lastmPSCount + " updates.");
            }

            lastmPSCount = 0;
         }
      } catch (SQLException var3) {
         logger.log(Level.WARNING, "Failed to save log entry.", (Throwable)var3);
      }
   }

   public static void loadAllLogEntries() {
      try {
         Connection dbcon = DbConnector.getLogsDbCon();
         PreparedStatement ps = dbcon.prepareStatement("SELECT * FROM TILE_LOG");
         ResultSet rs = ps.executeQuery();

         while(rs.next()) {
            TileLog tl = new TileLog(rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getInt("LAYER"), rs.getLong("PERFORMER"), rs.getInt("ACTION"), true);
            tl.setDate(rs.getLong("DATE"));
         }
      } catch (SQLException var4) {
         logger.log(Level.WARNING, "Failed to load log entry.", (Throwable)var4);
      }
   }

   public static void pruneLogEntries() {
      if (System.currentTimeMillis() - lastPruned > pruneInterval) {
         lastPruned = System.currentTimeMillis();
         long cutDate = System.currentTimeMillis() - pruneInterval;

         try {
            Connection dbcon = DbConnector.getLogsDbCon();
            PreparedStatement ps = dbcon.prepareStatement("DELETE FROM TILE_LOG WHERE DATE<?");
            ps.setLong(1, cutDate);
            ps.execute();
         } catch (SQLException var4) {
            logger.log(Level.WARNING, "Failed to prune log entries.", (Throwable)var4);
         }

         ListIterator<TileLog> lit = logEntries.listIterator();

         while(lit.hasNext() && lit.next().getDate() < cutDate) {
            lit.remove();
         }
      }
   }

   public static final List<TileLog> getEventsFor(int tilex, int tiley, int layer) {
      LinkedList<TileLog> matches = new LinkedList<>();

      for(TileLog t : logEntries) {
         if (t.getTileX() == tilex && t.getTileY() == tiley && t.getLayer() == layer) {
            matches.add(t);
         }
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
   public String toString() {
      return "ItemTransfer [tilex="
         + this.tilex
         + ", tiley="
         + this.tiley
         + ", performer="
         + this.performer
         + ", action="
         + this.action
         + ", date="
         + this.date
         + "]";
   }

   @Override
   public final String getDatabaseInsertStatement() {
      return "INSERT INTO TILE_LOG (TILEX,TILEY, LAYER, PERFORMER, ACTION, DATE) VALUES ( ?, ?, ?, ?, ?, ?)";
   }
}
