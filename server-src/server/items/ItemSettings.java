package com.wurmonline.server.items;

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

public class ItemSettings implements MiscConstants {
   private static final Logger logger = Logger.getLogger(ItemSettings.class.getName());
   private static final String GET_ALL_SETTINGS = "SELECT * FROM ITEMSETTINGS";
   private static final String ADD_PLAYER = "INSERT INTO ITEMSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)";
   private static final String DELETE_SETTINGS = "DELETE FROM ITEMSETTINGS WHERE WURMID=?";
   private static final String REMOVE_PLAYER = "DELETE FROM ITEMSETTINGS WHERE WURMID=? AND PLAYERID=?";
   private static final String UPDATE_PLAYER = "UPDATE ITEMSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?";
   private static int MAX_PLAYERS_PER_OBJECT = 1000;
   private static Map<Long, PermissionsPlayerList> objectSettings = new ConcurrentHashMap<>();

   private ItemSettings() {
   }

   public static void loadAll() throws IOException {
      logger.log(Level.INFO, "Loading all item settings.");
      long start = System.nanoTime();
      long count = 0L;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM ITEMSETTINGS");

         for(rs = ps.executeQuery(); rs.next(); ++count) {
            long wurmId = rs.getLong("WURMID");
            long playerId = rs.getLong("PLAYERID");
            int settings = rs.getInt("SETTINGS");
            add(wurmId, playerId, settings);
         }
      } catch (SQLException var17) {
         logger.log(Level.WARNING, "Failed to load settings for items.", (Throwable)var17);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.log(Level.INFO, "Loaded " + count + " item settings. That took " + (float)(end - start) / 1000000.0F + " ms.");
      }
   }

   public static int getMaxAllowed() {
      return Servers.isThisATestServer() ? 10 : MAX_PLAYERS_PER_OBJECT;
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
      } else {
         logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for item " + wurmId + ".");
      }
   }

   private static void dbAddPlayer(long wurmId, long playerId, int settings, boolean add) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         if (add) {
            ps = dbcon.prepareStatement("INSERT INTO ITEMSETTINGS (SETTINGS,WURMID,PLAYERID) VALUES(?,?,?)");
         } else {
            ps = dbcon.prepareStatement("UPDATE ITEMSETTINGS SET SETTINGS=? WHERE WURMID=? AND PLAYERID=?");
         }

         ps.setInt(1, settings);
         ps.setLong(2, wurmId);
         ps.setLong(3, playerId);
         ps.executeUpdate();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to " + (add ? "add" : "update") + " player (" + playerId + ") for item with id " + wurmId, (Throwable)var12);
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
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM ITEMSETTINGS WHERE WURMID=? AND PLAYERID=?");
         ps.setLong(1, wurmId);
         ps.setLong(2, playerId);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to remove player " + playerId + " from settings for item " + wurmId + ".", (Throwable)var11);
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
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM ITEMSETTINGS WHERE WURMID=?");
         ps.setLong(1, wurmId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete settings for item " + wurmId + ".", (Throwable)var9);
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
      return hasPermission(is, creature, ItemSettings.VehiclePermissions.MANAGE.getBit());
   }

   public static boolean mayCommand(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.VehiclePermissions.COMMANDER.getBit());
   }

   public static boolean mayPassenger(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.VehiclePermissions.PASSENGER.getBit());
   }

   public static boolean mayAccessHold(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.VehiclePermissions.ACCESS_HOLD.getBit());
   }

   public static boolean mayUseBed(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.BedPermissions.MAY_USE_BED.getBit());
   }

   public static boolean mayFreeSleep(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.BedPermissions.FREE_SLEEP.getBit());
   }

   public static boolean mayDrag(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.VehiclePermissions.DRAG.getBit());
   }

   public static boolean mayPostNotices(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.MessageBoardPermissions.MAY_POST_NOTICES.getBit());
   }

   public static boolean mayAddPMs(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? true : hasPermission(is, creature, ItemSettings.MessageBoardPermissions.MAY_ADD_PMS.getBit());
   }

   public static boolean isExcluded(PermissionsPlayerList.ISettings is, Creature creature) {
      return creature.getPower() > 1 ? false : hasPermission(is, creature, ItemSettings.VehiclePermissions.EXCLUDE.getBit());
   }

   public static enum BedPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      MAY_USE_BED(4, "May Use Bed", "May Use", "Bed", "Allows acess to use this bed."),
      FREE_SLEEP(5, "Free Sleep", "Free", "Sleep", "Allows this bed to be used for free (requires 'May Use Bed' as well)."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private BedPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum CorpsePermissions implements Permissions.IPermission {
      COMMANDER(1, "Commander", "Can", "Access", "Allows looting of this corpse."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = ItemSettings.ItemPermissions.values();

      private CorpsePermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum CreatureTransporterPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      COMMANDER(1, "Commander", "Can", "Command", "Allows commanding of this vehicle."),
      PASSENGER(2, "Passenger", "Can be", "Passenger", "Allows being a passenger of this vehicle."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      DRAG(6, "Drag", "May", "Drag", "Allows the Vehicle to be dragged."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = ItemSettings.ShipTransporterPermissions.values();

      private CreatureTransporterPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum GMItemPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      COMMANDER(1, "Commander", "Can", "Command", "Allows commanding of this vehicle."),
      PASSENGER(2, "Passenger", "Can be", "Passenger", "Allows being a passenger of this vehicle."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      MAY_USE_BED(4, "Can Sleep", "Can", "Sleep", ""),
      FREE_SLEEP(5, "Free Use", "Free", "Use", ""),
      DRAG(6, "Drag", "May", "Drag", "Allows the Vehicle to be dragged."),
      MAY_POST_NOTICES(7, "Notices", "May Post", "Notices", "Allows notices to be posted."),
      MAY_ADD_PMS(8, "PMs", "May Add", "PMs", "Allows PMs to be added."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private GMItemPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum ItemPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      ACCESS_HOLD(3, "Access Item", "May", "Open", "Allows acces to this container."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private ItemPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum MessageBoardPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      MANAGE_NOTICES(1, "Manage Notices", "Manage", "Notices", "Allows managing of any notices."),
      ACCESS_HOLD(3, "Access Item", "May View", "Messages", "Allows viewing of mesages on this board."),
      MAY_POST_NOTICES(7, "Notices", "May Post", "Notices", "Allows notices to be posted."),
      MAY_ADD_PMS(8, "PMs", "May Add", "PMs", "Allows PMs to be added."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = ItemSettings.ItemPermissions.values();

      private MessageBoardPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum ShipTransporterPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      COMMANDER(1, "Commander", "Can", "Command", "Allows commanding of this vehicle."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      DRAG(6, "Drag", "May", "Drag", "Allows the Vehicle to be dragged."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private ShipTransporterPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum SmallCartPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      DRAG(6, "Drag", "May", "Drag", "Allows the Vehicle to be dragged."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private SmallCartPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum VehiclePermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      COMMANDER(1, "Commander", "Can", "Command", "Allows commanding of this vehicle."),
      PASSENGER(2, "Passenger", "Can be", "Passenger", "Allows being a passenger of this vehicle."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      DRAG(6, "Drag", "May", "Drag", "Allows the Vehicle to be dragged."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private VehiclePermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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

   public static enum WagonPermissions implements Permissions.IPermission {
      MANAGE(0, "Manage Item", "Manage", "Item", "Allows managing of these permissions."),
      COMMANDER(1, "Commander", "Can", "Command", "Allows commanding of this vehicle."),
      PASSENGER(2, "Passenger", "Can be", "Passenger", "Allows being a passenger of this vehicle."),
      ACCESS_HOLD(3, "Access Hold", "Access", "Hold", "Allows acces to the hold."),
      EXCLUDE(15, "Deny All", "Deny", "All", "Deny all access.");

      final byte bit;
      final String description;
      final String header1;
      final String header2;
      final String hover;
      private static final Permissions.IPermission[] types = values();

      private WagonPermissions(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover) {
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
