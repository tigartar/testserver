package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorSettings;
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

public final class DoorSettings implements MiscConstants {
   private static final Logger logger = Logger.getLogger(DoorSettings.class.getName());
   private static final String GET_ALL_SETTINGS = "SELECT * FROM DOORSETTINGS";
   private static final String ADD_PLAYER = "INSERT INTO DOORSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)";
   private static final String DELETE_SETTINGS = "DELETE FROM DOORSETTINGS WHERE WURMID=?";
   private static final String REMOVE_PLAYER = "DELETE FROM DOORSETTINGS WHERE WURMID=? AND PLAYERID=?";
   private static final String UPDATE_PLAYER = "UPDATE DOORSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?";
   private static final int MAX_PLAYERS_PER_OBJECT = 1000;
   private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap<>();

   private DoorSettings() {
   }

   public static void loadAll() throws IOException {
      logger.log(Level.INFO, "Loading all door (and gate) settings.");
      long start = System.nanoTime();
      long count = 0L;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM DOORSETTINGS");

         for(rs = ps.executeQuery(); rs.next(); ++count) {
            long wurmId = rs.getLong("WURMID");
            long playerId = rs.getLong("PLAYERID");
            int settings = rs.getInt("SETTINGS");
            add(wurmId, playerId, settings);
         }
      } catch (SQLException var17) {
         logger.log(Level.WARNING, "Failed to load settings for doors (and gates).", (Throwable)var17);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.log(Level.INFO, "Loaded " + count + " door (and gate) settings. That took " + (float)(end - start) / 1000000.0F + " ms.");
      }
   }

   public static int getMaxAllowed() {
      return Servers.isThisATestServer() ? 10 : 1000;
   }

   private static PermissionsByPlayer add(long wurmId, long playerId, int settings) {
      Long id = wurmId;
      if (objectSettings.containsKey(id)) {
         PermissionsPlayerList ppl = objectSettings.get(id);
         return ppl.add(playerId, settings);
      } else {
         PermissionsPlayerList ppl = new PermissionsPlayerList();
         objectSettings.put(id, ppl);
         return ppl.add(playerId, settings);
      }
   }

   public static void addPlayer(long wurmId, long playerId, int settings) {
      PermissionsByPlayer pbp = add(wurmId, playerId, settings);
      if (pbp == null) {
         dbAddPlayer(wurmId, playerId, settings, true);
      } else if (pbp.getSettings() != settings) {
         dbAddPlayer(wurmId, playerId, settings, false);
      }
   }

   public static void removePlayer(long wurmId, long playerId) {
      Long id = wurmId;
      if (objectSettings.containsKey(id)) {
         PermissionsPlayerList ppl = objectSettings.get(id);
         ppl.remove(playerId);
         dbRemovePlayer(wurmId, playerId);
         if (ppl.isEmpty()) {
            objectSettings.remove(id);
         }
      }
   }

   private static void dbAddPlayer(long wurmId, long playerId, int settings, boolean add) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         if (add) {
            ps = dbcon.prepareStatement("INSERT INTO DOORSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)");
         } else {
            ps = dbcon.prepareStatement("UPDATE DOORSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?");
         }

         ps.setInt(1, settings);
         ps.setLong(2, wurmId);
         ps.setLong(3, playerId);
         ps.executeUpdate();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for door with id " + wurmId, (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbRemovePlayer(long wurmId, long playerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM DOORSETTINGS WHERE WURMID=? AND PLAYERID=?");
         ps.setLong(1, wurmId);
         ps.setLong(2, playerId);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for door " + wurmId + ".", (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static boolean exists(long wurmId) {
      Long id = wurmId;
      return objectSettings.containsKey(id);
   }

   public static void remove(long wurmId) {
      Long id = wurmId;
      if (objectSettings.containsKey(id)) {
         dbRemove(wurmId);
         objectSettings.remove(id);
      }
   }

   private static void dbRemove(long wurmId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM DOORSETTINGS WHERE WURMID=?");
         ps.setLong(1, wurmId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete settings for door " + wurmId + ".", (Throwable)var9);
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

   private static boolean hasPermission(PermissionsPlayerList.ISettings is, Creature creature, int bit) {
      if (is.isOwner(creature)) {
         return bit != MineDoorSettings.MinedoorPermissions.EXCLUDE.getBit();
      } else {
         Long id = is.getWurmId();
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

   public static boolean canManage(PermissionsPlayerList.ISettings is, Creature creature) {
      return hasPermission(is, creature, DoorSettings.GatePermissions.MANAGE.getBit());
   }

   public static boolean mayPass(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, DoorSettings.DoorPermissions.PASS.getBit());
   }

   public static boolean mayLock(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, DoorSettings.GatePermissions.LOCK.getBit());
   }

   public static boolean isExcluded(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? false : hasPermission(is, creature, DoorSettings.DoorPermissions.EXCLUDE.getBit());
   }

   public static enum DoorPermissions implements Permissions.IPermission {
      PASS(1, "Pass Door", "Pass", "Door", "Allows entry through this door even when its locked."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private DoorPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum GatePermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      PASS(1, "Pass Gate", "Pass", "Gate", "Allows entry through this gate even when its locked."),
      LOCK(2, "(Un)Lock Gate", "(Un)Lock", "Gate", "Allows locking (and unlocking) of this gate."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private GatePermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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
