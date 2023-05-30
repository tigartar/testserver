package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MineDoorSettings implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MineDoorSettings.class.getName());
   private static final String GET_ALL_SETTINGS = "SELECT * FROM MDPERMS";
   private static final String ADD_PLAYER = "INSERT INTO MDPERMS (SETTINGS,ID,PERMITTED) VALUES(?,?,?)";
   private static final String DELETE_SETTINGS = "DELETE FROM MDPERMS WHERE ID=?";
   private static final String REMOVE_PLAYER = "DELETE FROM MDPERMS WHERE ID=? AND PERMITTED=?";
   private static final String UPDATE_PLAYER = "UPDATE MDPERMS SET SETTINGS=? WHERE ID=? AND PERMITTED=?";
   private static int MAX_PLAYERS_PER_OBJECT = 1000;
   private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap<>();

   private MineDoorSettings() {
   }

   public static void loadAll() throws IOException {
      logger.log(Level.INFO, "Loading all minedoor settings.");
      long start = System.nanoTime();
      long count = 0L;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM MDPERMS");

         for(rs = ps.executeQuery(); rs.next(); ++count) {
            int minedoorId = rs.getInt("ID");
            long playerId = rs.getLong("PERMITTED");
            int settings = rs.getInt("SETTINGS");
            if (settings == 0) {
               settings = MineDoorSettings.MinedoorPermissions.PASS.getValue();
            }

            add(minedoorId, playerId, settings);
         }
      } catch (SQLException var16) {
         logger.log(Level.WARNING, "Failed to load settings for minedoors.", (Throwable)var16);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.log(Level.INFO, "Loaded " + count + " minedoor settings. That took " + (float)(end - start) / 1000000.0F + " ms.");
      }
   }

   public static int getMaxAllowed() {
      return Servers.isThisATestServer() ? 10 : MAX_PLAYERS_PER_OBJECT;
   }

   private static PermissionsByPlayer add(int minedoorId, long playerId, int settings) {
      Long id = (long)minedoorId;
      if (objectSettings.containsKey(id)) {
         PermissionsPlayerList ppl = objectSettings.get(id);
         return ppl.add(playerId, settings);
      } else {
         PermissionsPlayerList ppl = new PermissionsPlayerList();
         objectSettings.put(id, ppl);
         return ppl.add(playerId, settings);
      }
   }

   public static void addPlayer(int minedoorId, long playerId, int settings) {
      PermissionsByPlayer pbp = add(minedoorId, playerId, settings);
      if (pbp == null) {
         dbAddPlayer(minedoorId, playerId, settings, true);
      } else if (pbp.getSettings() != settings) {
         dbAddPlayer(minedoorId, playerId, settings, false);
      }
   }

   public static void removePlayer(int minedoorId, long playerId) {
      Long id = (long)minedoorId;
      if (objectSettings.containsKey(id)) {
         PermissionsPlayerList ppl = objectSettings.get(id);
         ppl.remove(playerId);
         dbRemovePlayer(minedoorId, playerId);
         if (ppl.isEmpty()) {
            objectSettings.remove(id);
         }
      } else {
         logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for minedoor " + minedoorId + ".");
      }
   }

   private static void dbAddPlayer(int minedoorId, long playerId, int settings, boolean add) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         if (add) {
            ps = dbcon.prepareStatement("INSERT INTO MDPERMS (SETTINGS,ID,PERMITTED) VALUES(?,?,?)");
         } else {
            ps = dbcon.prepareStatement("UPDATE MDPERMS SET SETTINGS=? WHERE ID=? AND PERMITTED=?");
         }

         ps.setInt(1, settings);
         ps.setInt(2, minedoorId);
         ps.setLong(3, playerId);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for minedoor with id " + minedoorId, (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbRemovePlayer(int minedoorId, long playerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MDPERMS WHERE ID=? AND PERMITTED=?");
         ps.setInt(1, minedoorId);
         ps.setLong(2, playerId);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for minedoor " + minedoorId + ".", (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static boolean exists(long wurmId) {
      Long id = wurmId;
      return objectSettings.containsKey(id);
   }

   public static void remove(int minedoorId) {
      Long id = (long)minedoorId;
      if (objectSettings.containsKey(id)) {
         dbRemove(minedoorId);
         objectSettings.remove(id);
      }
   }

   private static void dbRemove(int minedoorId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MDPERMS WHERE ID=?");
         ps.setInt(1, minedoorId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to delete settings for minedoor " + minedoorId + ".", (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static PermissionsPlayerList getPermissionsPlayerList(long wurmId) {
      Long id = wurmId;
      PermissionsPlayerList ppl = objectSettings.get(id);
      return ppl == null ? new PermissionsPlayerList() : ppl;
   }

   private static boolean hasPermission(PermissionsPlayerList.ISettings is, long objectId, Creature creature, int bit) {
      if (is.isOwner(creature)) {
         return bit != MineDoorSettings.MinedoorPermissions.EXCLUDE.getBit();
      } else {
         Long id = objectId;
         PermissionsPlayerList ppl = objectSettings.get(id);
         if (ppl == null) {
            return false;
         } else if (ppl.exists(creature.getWurmId())) {
            return ppl.getPermissionsFor(creature.getWurmId()).hasPermission(bit);
         } else if (is.isCitizen(creature) && ppl.exists(-30L)) {
            return ppl.getPermissionsFor(-30L).hasPermission(bit);
         } else if (is.isAllied(creature) && ppl.exists(-20L)) {
            return ppl.getPermissionsFor(-20L).hasPermission(bit);
         } else if (is.isSameKingdom(creature) && ppl.exists(-40L)) {
            return ppl.getPermissionsFor(-40L).hasPermission(bit);
         } else {
            return ppl.exists(-50L) && ppl.getPermissionsFor(-50L).hasPermission(bit);
         }
      }
   }

   public static boolean isGuest(PermissionsPlayerList.ISettings is, Creature creature) {
      return isGuest(is, creature.getWurmId());
   }

   public static boolean isGuest(PermissionsPlayerList.ISettings is, long playerId) {
      if (is.isOwner(playerId)) {
         return true;
      } else {
         Long id = is.getWurmId();
         PermissionsPlayerList ppl = objectSettings.get(id);
         return ppl == null ? false : ppl.exists(playerId);
      }
   }

   public static boolean canManage(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
      return hasPermission(is, objectId, creature, MineDoorSettings.MinedoorPermissions.MANAGE.getBit());
   }

   public static boolean mayPass(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, objectId, creature, MineDoorSettings.MinedoorPermissions.PASS.getBit());
   }

   public static boolean isExcluded(PermissionsPlayerList.ISettings is, long objectId, Creature creature) {
      return creature.getPower() > 1 ? false : hasPermission(is, objectId, creature, MineDoorSettings.MinedoorPermissions.EXCLUDE.getBit());
   }

   public static enum MinedoorPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      PASS(1, "Pass Door", "Pass", "Mine Door", "Allows entry through this mine door."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.Allow[] types = Permissions.Allow.values();

      private MinedoorPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
         this.bit = (byte)aBit;
         this.description = aDescription;
         this.header1 = aHeader1;
         this.header2 = aHeader2;
         this.hover = aHover;
      }

      @Override
      public byte getBit() {
         return this.bit;
      }

      @Override
      public int getValue() {
         return 1 << this.bit;
      }

      @Override
      public String getDescription() {
         return this.description;
      }

      @Override
      public String getHeader1() {
         return this.header1;
      }

      @Override
      public String getHeader2() {
         return this.header2;
      }

      @Override
      public String getHover() {
         return this.hover;
      }

      public static Permissions.IPermission[] getPermissions() {
         return types;
      }
   }
}
