package com.wurmonline.server.deities;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.spells.RiteEvent;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbRitual {
   private static final Logger logger = Logger.getLogger(DbRitual.class.getName());
   private static final String CREATE_RITE_EVENT = "INSERT INTO RITUALCASTS (ID,CASTERID,SPELLID,DEITYID,CASTTIME,DURATION) VALUES(?,?,?,?,?,?)";
   private static final String LOAD_RITE_EVENTS = "SELECT * FROM RITUALCASTS";
   private static final String CREATE_RITE_CLAIM = "INSERT INTO RITUALCLAIMS (ID,PLAYERID,RITUALCASTSID,CLAIMTIME) VALUES(?,?,?,?)";
   private static final String LOAD_RITE_CLAIMS = "SELECT * FROM RITUALCLAIMS";

   public static void createRiteEvent(RiteEvent event) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RITUALCASTS (ID,CASTERID,SPELLID,DEITYID,CASTTIME,DURATION) VALUES(?,?,?,?,?,?)");
         ps.setInt(1, event.getId());
         ps.setLong(2, event.getCasterId());
         ps.setInt(3, event.getSpellId());
         ps.setInt(4, event.getDeityNum());
         ps.setLong(5, event.getCastTime());
         ps.setLong(6, event.getDuration());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to create RiteEvent " + event.getId(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void loadRiteEvents() throws IOException {
      long lStart = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int found = 0;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM RITUALCASTS");

         for(rs = ps.executeQuery(); rs.next(); ++found) {
            int id = rs.getInt("ID");
            long casterId = rs.getLong("CASTERID");
            int spellId = rs.getInt("SPELLID");
            int deityNum = rs.getInt("DEITYID");
            long castTime = rs.getLong("CASTTIME");
            long duration = rs.getLong("DURATION");
            RiteEvent.createGenericRiteEvent(id, casterId, spellId, deityNum, castTime, duration);
         }
      } catch (SQLException var18) {
         logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
         throw new IOException(var18);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         logger.info("Finished loading " + found + " RiteEvents, which took " + (float)(System.nanoTime() - lStart) / 1000000.0F + " millis.");
      }
   }

   public static void createRiteClaim(int id, long playerId, int ritualCastsId, long claimTime) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RITUALCLAIMS (ID,PLAYERID,RITUALCASTSID,CLAIMTIME) VALUES(?,?,?,?)");
         ps.setInt(1, id);
         ps.setLong(2, playerId);
         ps.setInt(3, ritualCastsId);
         ps.setLong(4, claimTime);
         ps.executeUpdate();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to create Rite claim for player " + playerId + " claiming rite " + id, (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void loadRiteClaims() throws IOException {
      long lStart = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      int found = 0;

      try {
         dbcon = DbConnector.getDeityDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM RITUALCLAIMS");

         for(rs = ps.executeQuery(); rs.next(); ++found) {
            int id = rs.getInt("ID");
            long playerId = rs.getLong("PLAYERID");
            int ritualCastsId = rs.getInt("RITUALCASTSID");
            long claimTime = rs.getLong("CLAIMTIME");
            RiteEvent.addRitualClaim(id, playerId, ritualCastsId, claimTime);
         }
      } catch (SQLException var15) {
         logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
         throw new IOException(var15);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         logger.info("Finished loading " + found + " Ritual claims, which took " + (float)(System.nanoTime() - lStart) / 1000000.0F + " millis.");
      }
   }
}
