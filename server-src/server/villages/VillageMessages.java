package com.wurmonline.server.villages;

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

public final class VillageMessages {
   private static final Logger logger = Logger.getLogger(VillageMessages.class.getName());
   private static final Map<Integer, VillageMessages> villagesMessages = new ConcurrentHashMap<>();
   private static final String LOAD_ALL_MSGS = "SELECT * FROM VILLAGEMESSAGES";
   private static final String DELETE_VILLAGE_MSGS = "DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=?";
   private static final String CREATE_MSG = "INSERT INTO VILLAGEMESSAGES (VILLAGEID,FROMID,TOID,MESSAGE,POSTED,PENCOLOR,EVERYONE) VALUES (?,?,?,?,?,?,?);";
   private static final String DELETE_MSG = "DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=? AND TOID=? AND POSTED=?";
   private static final String DELETE_PLAYER_MSGS = "DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=? AND TOID=?";
   private Map<Long, Map<Long, VillageMessage>> villageMsgs = new ConcurrentHashMap<>();

   public VillageMessage put(long toId, VillageMessage value) {
      Map<Long, VillageMessage> msgs = this.villageMsgs.get(toId);
      if (msgs == null) {
         msgs = new ConcurrentHashMap<>();
         this.villageMsgs.put(toId, msgs);
      }

      return msgs.put(value.getPostedTime(), value);
   }

   public Map<Long, VillageMessage> get(long toId) {
      Map<Long, VillageMessage> msgs = this.villageMsgs.get(toId);
      return (Map<Long, VillageMessage>)(msgs == null ? new ConcurrentHashMap<>() : msgs);
   }

   public void remove(long playerId, long posted) {
      Map<Long, VillageMessage> msgs = this.villageMsgs.get(playerId);
      if (msgs != null) {
         msgs.remove(posted);
      }
   }

   public void remove(long playerId) {
      this.villageMsgs.remove(playerId);
   }

   public static void loadVillageMessages() {
      long start = System.nanoTime();
      int loadedMsgs = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM VILLAGEMESSAGES");

         for(rs = ps.executeQuery(); rs.next(); ++loadedMsgs) {
            VillageMessage villageMsg = new VillageMessage(
               rs.getInt("VILLAGEID"),
               rs.getLong("FROMID"),
               rs.getLong("TOID"),
               rs.getString("MESSAGE"),
               rs.getInt("PENCOLOR"),
               rs.getLong("POSTED"),
               rs.getBoolean("EVERYONE")
            );
            add(villageMsg);
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Failed to load village messages due to " + var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.info("Loaded " + loadedMsgs + " village messages from the database took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   public static void add(VillageMessage villageMsg) {
      VillageMessages villageMsgs = villagesMessages.get(villageMsg.getVillageId());
      if (villageMsgs == null) {
         villageMsgs = new VillageMessages();
         villagesMessages.put(villageMsg.getVillageId(), villageMsgs);
      }

      villageMsgs.put(villageMsg.getToId(), villageMsg);
   }

   public static VillageMessage[] getVillageMessages(int villageId, long toId) {
      VillageMessages villageMsgs = villagesMessages.get(villageId);
      return villageMsgs == null ? new VillageMessage[0] : villageMsgs.get(toId).values().toArray(new VillageMessage[villageMsgs.size()]);
   }

   private int size() {
      return 0;
   }

   public static final VillageMessage create(int villageId, long fromId, long toId, String message, int penColour, boolean everyone) {
      long posted = System.currentTimeMillis();
      dbCreate(villageId, fromId, toId, message, posted, penColour, everyone);
      VillageMessage villageMsg = new VillageMessage(villageId, fromId, toId, message, penColour, posted, everyone);
      add(villageMsg);
      return villageMsg;
   }

   public static final void delete(int villageId) {
      dbDelete(villageId);
      villagesMessages.remove(villageId);
   }

   public static final void delete(int villageId, long toId) {
      dbDelete(villageId, toId);
      VillageMessages villageMsgs = villagesMessages.get(villageId);
      if (villageMsgs != null) {
         villageMsgs.remove(toId);
      }
   }

   public static final void delete(int villageId, long toId, long posted) {
      dbDelete(villageId, toId, posted);
      VillageMessages villageMsgs = villagesMessages.get(villageId);
      if (villageMsgs != null) {
         villageMsgs.remove(toId, posted);
      }
   }

   private static final void dbCreate(int villageId, long fromId, long toId, String message, long posted, int penColour, boolean everyone) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO VILLAGEMESSAGES (VILLAGEID,FROMID,TOID,MESSAGE,POSTED,PENCOLOR,EVERYONE) VALUES (?,?,?,?,?,?,?);");
         ps.setInt(1, villageId);
         ps.setLong(2, fromId);
         ps.setLong(3, toId);
         ps.setString(4, message);
         ps.setLong(5, posted);
         ps.setInt(6, penColour);
         ps.setBoolean(7, everyone);
         ps.executeUpdate();
      } catch (SQLException var16) {
         logger.log(Level.WARNING, "Failed to create new message for village: " + villageId + ": " + var16.getMessage(), (Throwable)var16);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static final void dbDelete(int villageId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=?");
         ps.setInt(1, villageId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete all messages for village: " + villageId + ": " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static final void dbDelete(int villageId, long toId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=? AND TOID=?");
         ps.setInt(1, villageId);
         ps.setLong(2, toId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete message for village " + villageId + ", and player " + toId + " : " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static final void dbDelete(int villageId, long toId, long posted) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VILLAGEMESSAGES WHERE VILLAGEID=? AND TOID=? AND POSTED=?");
         ps.setInt(1, villageId);
         ps.setLong(2, toId);
         ps.setLong(3, posted);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(
            Level.WARNING,
            "Failed to delete message for village " + villageId + ", and player " + toId + " and posted " + posted + ": " + var11.getMessage(),
            (Throwable)var11
         );
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
