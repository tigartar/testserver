package com.wurmonline.server.epic;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionHelper implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MissionHelper.class.getName());
   private static final String LOAD_ALL_MISSION_HELPERS = "SELECT * FROM MISSIONHELPERS";
   private static final String INSERT_MISSION_HELPER = DbConnector.isUseSqlite()
      ? "INSERT OR IGNORE INTO MISSIONHELPERS (NUMS, MISSIONID, PLAYERID) VALUES(?,?,?)"
      : "INSERT IGNORE INTO MISSIONHELPERS (NUMS, MISSIONID, PLAYERID) VALUES(?,?,?)";
   private static final String MOVE_MISSION_HELPER = "UPDATE MISSIONHELPERS SET MISSIONID=? WHERE MISSIONID=?";
   private static final String DELETE_MISSION_HELPER = "DELETE FROM MISSIONHELPERS WHERE MISSIONID=?";
   private static final String UPDATE_MISSION_HELPER = "UPDATE MISSIONHELPERS SET NUMS=? WHERE MISSIONID=? AND PLAYERID=?";
   private static final Map<Long, MissionHelper> MISSION_HELPERS = new ConcurrentHashMap<>();
   private static boolean INITIALIZED = false;
   private final Map<Long, Integer> missionsHelped = new ConcurrentHashMap<>();
   private final long playerId;

   public MissionHelper(long playerid) {
      this.playerId = playerid;
      addHelper(this);
   }

   public final void increaseHelps(long missionId) {
      this.setHelps(missionId, this.getHelps(missionId) + 1);
   }

   public final void increaseHelps(long missionId, int nums) {
      this.setHelps(missionId, this.getHelps(missionId) + nums);
   }

   public static final void addHelper(MissionHelper helper) {
      MISSION_HELPERS.put(helper.getPlayerId(), helper);
   }

   private final void setHelpsAtLoad(long missionId, int nums) {
      this.missionsHelped.put(missionId, nums);
   }

   public static final Map<Long, MissionHelper> getHelpers() {
      return MISSION_HELPERS;
   }

   public static final void loadAll() {
      if (!INITIALIZED) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT * FROM MISSIONHELPERS");

            MissionHelper helper;
            for(rs = ps.executeQuery(); rs.next(); helper.setHelpsAtLoad(rs.getLong("MISSIONID"), rs.getInt("NUMS"))) {
               long helperId = rs.getLong("PLAYERID");
               helper = MISSION_HELPERS.get(helperId);
               if (helper == null) {
                  helper = new MissionHelper(helperId);
               }
            }

            INITIALIZED = true;
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to load epic item helpers.", (Throwable)var9);
            INITIALIZED = false;
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   public static final void printHelpForMission(long missionId, String missionName, Creature performer) {
      float total = 0.0F;
      if (!INITIALIZED) {
         loadAll();
      }

      for(MissionHelper helper : MISSION_HELPERS.values()) {
         total += (float)helper.getHelps(missionId);
      }

      if (total > 0.0F) {
         SimplePopup sp = new SimplePopup(performer, "Plaque on " + missionName, "These helped:", missionId, total);
         sp.sendQuestion();
      }
   }

   public static final void addKarmaForItem(long itemId) {
      for(MissionHelper helper : MISSION_HELPERS.values()) {
         int i = helper.getHelps(itemId);
         if (i > 10) {
            try {
               Player p = Players.getInstance().getPlayer(helper.getPlayerId());
               p.modifyKarma(i / 10);
            } catch (NoSuchPlayerException var7) {
               PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(helper.getPlayerId());
               pinf.setKarma(pinf.getKarma() + i / 10);
            }
         }
      }
   }

   public static final MissionHelper getOrCreateHelper(long playerId) {
      MissionHelper helper = MISSION_HELPERS.get(playerId);
      if (helper == null) {
         helper = new MissionHelper(playerId);
      }

      return helper;
   }

   public final long getPlayerId() {
      return this.playerId;
   }

   public final int getHelps(long missionId) {
      Integer nums = this.missionsHelped.get(missionId);
      return nums == null ? 0 : nums;
   }

   private final void moveLocalMissionId(long oldMissionId, long newMissionId) {
      int oldHelps = this.getHelps(oldMissionId);
      if (oldHelps > 0) {
         this.missionsHelped.remove(oldMissionId);
         this.missionsHelped.put(newMissionId, oldHelps);
      }
   }

   private final void removeMissionId(long missionId) {
      this.missionsHelped.remove(missionId);
   }

   public static final void moveGlobalMissionId(long oldmissionId, long newMissionId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("UPDATE MISSIONHELPERS SET MISSIONID=? WHERE MISSIONID=?");
         ps.setLong(1, newMissionId);
         ps.setLong(2, oldmissionId);
         ps.executeUpdate();

         for(MissionHelper h : MISSION_HELPERS.values()) {
            h.moveLocalMissionId(oldmissionId, newMissionId);
         }
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to move epic mission helps from mission " + oldmissionId + ", to" + newMissionId, (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final void deleteMissionId(long missionId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MISSIONHELPERS WHERE MISSIONID=?");
         ps.setLong(1, missionId);
         ps.executeUpdate();

         for(MissionHelper h : MISSION_HELPERS.values()) {
            h.removeMissionId(missionId);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete epic mission helps for mission " + missionId, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void setHelps(long missionId, int helps) {
      int oldHelps = this.getHelps(missionId);
      if (oldHelps != helps) {
         this.missionsHelped.put(missionId, helps);
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            if (oldHelps == 0) {
               ps = dbcon.prepareStatement(INSERT_MISSION_HELPER);
            } else {
               ps = dbcon.prepareStatement("UPDATE MISSIONHELPERS SET NUMS=? WHERE MISSIONID=? AND PLAYERID=?");
            }

            ps.setInt(1, helps);
            ps.setLong(2, missionId);
            ps.setLong(3, this.playerId);
            ps.executeUpdate();
         } catch (SQLException var11) {
            logger.log(Level.WARNING, "Failed to save epic item helps " + helps + " for mission " + missionId + ", pid=" + this.playerId, (Throwable)var11);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }
}
