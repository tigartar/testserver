package com.wurmonline.server.tutorial;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MissionTrigger implements MiscConstants, Comparable<MissionTrigger>, CounterTypes {
   private static Logger logger = Logger.getLogger(MissionTrigger.class.getName());
   private static final String UPDATE_TRIGGER = "UPDATE MISSIONTRIGGERS SET NAME=?,DESCRIPTION=?,ONITEMCREATED=?,ONACTIONPERFORMED=?,ONTARGET=?,MISSION_REQ=?,MISSION_STATE_REQ=?,MISSION_STATE_END=?,SECONDS=?,INACTIVE=?,CREATOR=?,CREATEDDATE=?,LASTMODIFIER=?,SPAWNPOINT=? WHERE ID=?";
   private static final String CREATE_TRIGGER = "INSERT INTO MISSIONTRIGGERS (NAME,DESCRIPTION,ONITEMCREATED,ONACTIONPERFORMED,ONTARGET,MISSION_REQ,MISSION_STATE_REQ,MISSION_STATE_END,SECONDS,INACTIVE,CREATOR,CREATORID,CREATORTYPE,CREATEDDATE,LASTMODIFIER,SPAWNPOINT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String DELETE_TRIGGER = "DELETE FROM MISSIONTRIGGERS WHERE ID=?";
   private int id = 0;
   private String name;
   private String description;
   private int onItemUsedId;
   private int onActionPerformed;
   private long onActionTargetId;
   private int missionRequired = 0;
   private float stateRequired = 100.0F;
   private float stateEnd = 0.0F;
   private int seconds = 0;
   private boolean inActive = false;
   private String creatorName;
   private String createdDate;
   private String lastModifierName;
   private Timestamp lastModifiedDate;
   private long ownerId = 0L;
   private byte creatorType = 0;
   private boolean spawnPoint = false;

   public int getOnActionPerformed() {
      return this.onActionPerformed;
   }

   public String getDescription() {
      return this.description;
   }

   void setCreatedDate(String aCreatedDate) {
      this.createdDate = aCreatedDate;
   }

   public String getCreatedDate() {
      return this.createdDate;
   }

   void setLastModifiedDate(Timestamp aLastModifiedDate) {
      this.lastModifiedDate = aLastModifiedDate;
   }

   public String getLastModifiedDate() {
      return DateFormat.getDateInstance(2).format(this.lastModifiedDate);
   }

   public String getName() {
      return this.name;
   }

   public void setDescription(String n) {
      this.description = n;
      this.description = this.description.substring(0, Math.min(this.description.length(), 100));
   }

   public void setName(String n) {
      this.name = n;
      this.name = this.name.substring(0, Math.min(this.name.length(), 40));
   }

   public long getTarget() {
      return this.onActionTargetId;
   }

   public int getItemUsedId() {
      return this.onItemUsedId;
   }

   public int getMissionRequired() {
      return this.missionRequired;
   }

   public void setCreatorType(byte aCreatorType) {
      this.creatorType = aCreatorType;
   }

   public byte getCreatorType() {
      return this.creatorType;
   }

   public void setOwnerId(long aWurmId) {
      this.ownerId = aWurmId;
   }

   public long getOwnerId() {
      return this.ownerId;
   }

   void setId(int aId) {
      this.id = aId;
   }

   public int getId() {
      return this.id;
   }

   public float getStateRequired() {
      return this.stateRequired;
   }

   public float getStateEnd() {
      return this.stateEnd;
   }

   public String getStateRange() {
      return this.getStateEnd() > this.getStateRequired() ? this.getStateRequired() + " to " + this.getStateEnd() : Float.toString(this.getStateRequired());
   }

   public boolean isTriggered(float missionState, boolean checkActive) {
      boolean stateOk = false;
      if (this.getStateEnd() != 0.0F && this.getStateEnd() > this.getStateRequired()) {
         if (missionState >= this.getStateRequired() && missionState <= this.getStateEnd()) {
            stateOk = true;
         }
      } else if (this.getStateRequired() == missionState) {
         stateOk = true;
      }

      if (stateOk) {
         if (!checkActive) {
            return true;
         }

         if (!this.isInactive()) {
            return true;
         }
      }

      return false;
   }

   public int getSeconds() {
      return this.seconds;
   }

   public void setSeconds(int secs) {
      this.seconds = secs;
   }

   public boolean isInactive() {
      return this.inActive;
   }

   public boolean isSpawnPoint() {
      return this.spawnPoint;
   }

   public void setOnTargetId(long t) {
      this.onActionTargetId = t;
   }

   public void setOnItemUsedId(int n) {
      this.onItemUsedId = n;
   }

   public void setOnActionPerformed(int a) {
      this.onActionPerformed = a;
   }

   public void setMissionRequirement(int rq) {
      this.missionRequired = rq;
   }

   public void setStateRequirement(float sq) {
      this.stateRequired = sq;
   }

   public void setStateEnd(float sq) {
      this.stateEnd = sq;
   }

   public void setInactive(boolean inactive) {
      this.inActive = inactive;
   }

   public void setIsSpawnpoint(boolean isSpawnPoint) {
      this.spawnPoint = isSpawnPoint;
   }

   public String getCreatorName() {
      return this.creatorName;
   }

   public String getLastModifierName() {
      return this.lastModifierName;
   }

   public void setCreatorName(String n) {
      this.creatorName = n;
      this.creatorName = this.creatorName.substring(0, Math.min(this.creatorName.length(), 40));
   }

   public void setLastModifierName(String n) {
      this.lastModifierName = n;
      this.lastModifierName = this.lastModifierName.substring(0, Math.min(this.lastModifierName.length(), 40));
   }

   public void update() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE MISSIONTRIGGERS SET NAME=?,DESCRIPTION=?,ONITEMCREATED=?,ONACTIONPERFORMED=?,ONTARGET=?,MISSION_REQ=?,MISSION_STATE_REQ=?,MISSION_STATE_END=?,SECONDS=?,INACTIVE=?,CREATOR=?,CREATEDDATE=?,LASTMODIFIER=?,SPAWNPOINT=? WHERE ID=?"
         );
         ps.setString(1, this.name);
         ps.setString(2, this.description);
         ps.setInt(3, this.onItemUsedId);
         ps.setInt(4, this.onActionPerformed);
         ps.setLong(5, this.onActionTargetId);
         ps.setInt(6, this.missionRequired);
         ps.setFloat(7, this.stateRequired);
         ps.setFloat(8, this.stateEnd);
         ps.setInt(9, this.seconds);
         ps.setBoolean(10, this.inActive);
         ps.setString(11, this.creatorName);
         ps.setString(12, this.createdDate);
         this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
         ps.setString(13, this.lastModifierName);
         ps.setBoolean(14, this.spawnPoint);
         ps.setInt(15, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void create() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO MISSIONTRIGGERS (NAME,DESCRIPTION,ONITEMCREATED,ONACTIONPERFORMED,ONTARGET,MISSION_REQ,MISSION_STATE_REQ,MISSION_STATE_END,SECONDS,INACTIVE,CREATOR,CREATORID,CREATORTYPE,CREATEDDATE,LASTMODIFIER,SPAWNPOINT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            1
         );
         ps.setString(1, this.name);
         ps.setString(2, this.description);
         ps.setInt(3, this.onItemUsedId);
         ps.setInt(4, this.onActionPerformed);
         ps.setLong(5, this.onActionTargetId);
         ps.setInt(6, this.missionRequired);
         ps.setFloat(7, this.stateRequired);
         ps.setFloat(8, this.stateEnd);
         ps.setInt(9, this.seconds);
         ps.setBoolean(10, this.inActive);
         ps.setString(11, this.creatorName);
         ps.setLong(12, this.ownerId);
         ps.setByte(13, this.creatorType);
         this.createdDate = DateFormat.getDateInstance(2).format(new Timestamp(System.currentTimeMillis()));
         this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
         ps.setString(14, this.createdDate);
         ps.setString(15, this.lastModifierName);
         ps.setBoolean(16, this.spawnPoint);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (rs.next()) {
            this.id = rs.getInt(1);
         }

         logger.log(Level.INFO, "Mission trigger " + this.name + " (" + this.id + ") created at " + this.createdDate);
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public void destroy() {
      MissionTriggers.removeTrigger(this.id);
      Triggers2Effects.deleteTrigger(this.id);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MISSIONTRIGGERS WHERE ID=?");
         ps.setInt(1, this.id);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public Spawnpoint getSpawnPoint() {
      Spawnpoint toReturn = null;
      long targetId = this.getTarget();
      if (WurmId.getType(targetId) == 1) {
         try {
            Creature c = Creatures.getInstance().getCreature(targetId);
            toReturn = new Spawnpoint((byte)1, c.getName(), (short)c.getTileX(), (short)c.getTileY(), c.isOnSurface(), c.getKingdomId());
         } catch (NoSuchCreatureException var12) {
         }
      }

      if (WurmId.getType(targetId) == 0) {
         try {
            Player p = Players.getInstance().getPlayer(targetId);
            toReturn = new Spawnpoint((byte)1, p.getName(), (short)p.getTileX(), (short)p.getTileY(), p.isOnSurface(), p.getKingdomId());
         } catch (NoSuchPlayerException var11) {
         }
      }

      if (WurmId.getType(targetId) == 5) {
         int x = (int)(targetId >> 32) & 65535;
         int y = (int)(targetId >> 16) & 65535;
         Wall wall = Wall.getWall(targetId);
         if (wall != null) {
            toReturn = new Spawnpoint((byte)1, wall.getName(), (short)x, (short)y, true, (byte)0);
         }
      }

      if (WurmId.getType(targetId) == 2 || WurmId.getType(targetId) == 6 || WurmId.getType(targetId) == 19 || WurmId.getType(targetId) == 20) {
         try {
            Item targetItem = Items.getItem(targetId);
            toReturn = new Spawnpoint(
               (byte)1, targetItem.getName(), (short)targetItem.getTileX(), (short)targetItem.getTileY(), targetItem.isOnSurface(), (byte)0
            );
         } catch (NoSuchItemException var10) {
         }
      } else if (WurmId.getType(targetId) == 7) {
         int x = (int)(targetId >> 32) & 65535;
         int y = (int)(targetId >> 16) & 65535;
         Fence fence = Fence.getFence(targetId);
         if (fence != null) {
            toReturn = new Spawnpoint((byte)1, fence.getName(), (short)x, (short)y, true, (byte)0);
         }
      } else if (WurmId.getType(targetId) == 3) {
         int x = (int)(targetId >> 32) & 65535;
         int y = (int)(targetId >> 16) & 65535;
         if (x > Zones.worldTileSizeX) {
            int oldx = x;
            x = (int)(targetId >> 40) & 16777215;
            int heightOffset = (int)(targetId >> 48) & 65535;
            long newTarg = Tiles.getTileId(x, y, heightOffset);
            this.setOnTargetId(newTarg);
            this.update();
            logger.log(Level.INFO, "Updated mission trigger " + this.getName() + " to " + x + "," + y + " from " + oldx + "," + y);
         }

         int tile = Server.surfaceMesh.getTile(x, y);
         byte type = Tiles.decodeType(tile);
         Tiles.Tile t = Tiles.getTile(type);
         toReturn = new Spawnpoint((byte)1, t.tiledesc, (short)x, (short)y, true, (byte)0);
      } else if (WurmId.getType(targetId) == 17) {
         int x = (int)(targetId >> 32) & 65535;
         int y = (int)(targetId >> 16) & 65535;
         int tile = Server.caveMesh.getTile(x, y);
         byte type = Tiles.decodeType(tile);
         Tiles.Tile t = Tiles.getTile(type);
         toReturn = new Spawnpoint((byte)1, t.tiledesc, (short)x, (short)y, false, (byte)0);
      }

      return toReturn;
   }

   public int compareTo(MissionTrigger aOtherMissionTrigger) {
      return this.name.compareTo(aOtherMissionTrigger.getName());
   }

   public boolean hasTargetOf(long currentTargetId, Creature performer) {
      return this.onActionTargetId == currentTargetId;
   }

   public String getActionString() {
      return Actions.getActionString((short)this.onActionPerformed);
   }

   public String getTargetAsString(Creature creature) {
      return MissionTriggers.getTargetAsString(creature, this.onActionTargetId);
   }
}
