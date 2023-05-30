package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbBridgePart extends BridgePart {
   private static final String CREATEBRIDGEPART = "INSERT INTO BRIDGEPARTS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE, MATERIAL, HEIGHTOFFSET, DIR, SLOPE, STAGECOUNT, NORTHEXIT, EASTEXIT, SOUTHEXIT, WESTEXIT, LAYER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String UPDATEBRIDGEPART = "UPDATE BRIDGEPARTS SET TYPE=?,LASTMAINTAINED=?,CURRENTQL=?,ORIGINALQL=?,DAMAGE=?,STRUCTURE=?,STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=?,SLOPE=?,STAGECOUNT=?,NORTHEXIT=?,EASTEXIT=?,SOUTHEXIT=?,WESTEXIT=?,LAYER=? WHERE ID=?";
   private static final String GETBRIDGEPART = "SELECT * FROM BRIDGEPARTS WHERE ID=?";
   private static final String DELETEBRIDGEPART = "DELETE FROM BRIDGEPARTS WHERE ID=?";
   private static final String SETDAMAGE = "UPDATE BRIDGEPARTS SET DAMAGE=? WHERE ID=?";
   private static final String SETQUALITYLEVEL = "UPDATE BRIDGEPARTS SET CURRENTQL=? WHERE ID=?";
   private static final String SETSTATE = "UPDATE BRIDGEPARTS SET STATE=?,MATERIAL=? WHERE ID=?";
   private static final String SETLASTUSED = "UPDATE BRIDGEPARTS SET LASTMAINTAINED=? WHERE ID=?";
   private static final String SET_SETTINGS = "UPDATE BRIDGEPARTS SET SETTINGS=? WHERE ID=?";
   private static final String SETROADTYPE = "UPDATE BRIDGEPARTS SET ROADTYPE=? WHERE ID=?";
   private static final Logger logger = Logger.getLogger(DbWall.class.getName());

   @Override
   public boolean isFence() {
      return false;
   }

   @Override
   public boolean isWall() {
      return false;
   }

   public DbBridgePart(
      int id,
      BridgeConstants.BridgeType floorType,
      int tilex,
      int tiley,
      byte aDbState,
      int heightOffset,
      float currentQl,
      long structureId,
      BridgeConstants.BridgeMaterial floorMaterial,
      float origQL,
      float dam,
      int materialCount,
      long lastmaintained,
      byte dir,
      byte slope,
      int aNorthExit,
      int aEastExit,
      int aSouthExit,
      int aWestExit,
      byte roadType,
      int layer
   ) {
      super(
         id,
         floorType,
         tilex,
         tiley,
         aDbState,
         heightOffset,
         currentQl,
         structureId,
         floorMaterial,
         origQL,
         dam,
         materialCount,
         lastmaintained,
         dir,
         slope,
         aNorthExit,
         aEastExit,
         aSouthExit,
         aWestExit,
         roadType,
         layer
      );
   }

   public DbBridgePart(
      BridgeConstants.BridgeType floorType,
      int tilex,
      int tiley,
      int heightOffset,
      float qualityLevel,
      long structure,
      BridgeConstants.BridgeMaterial material,
      byte dir,
      byte slope,
      int aNorthExit,
      int aEastExit,
      int aSouthExit,
      int aWestExit,
      byte roadType,
      int layer
   ) {
      super(
         floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, dir, slope, aNorthExit, aEastExit, aSouthExit, aWestExit, roadType, layer
      );
   }

   @Override
   protected void setState(byte newState) {
      if (this.dbState != newState) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.dbState = newState;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET STATE=?,MATERIAL=? WHERE ID=?");
            ps.setByte(1, this.dbState);
            ps.setByte(2, this.getMaterial().getCode());
            ps.setInt(3, this.getNumber());
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set state to " + newState + " for bridge part with id " + this.getNumber(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void save() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         if (this.exists(dbcon)) {
            ps = dbcon.prepareStatement(
               "UPDATE BRIDGEPARTS SET TYPE=?,LASTMAINTAINED=?,CURRENTQL=?,ORIGINALQL=?,DAMAGE=?,STRUCTURE=?,STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=?,SLOPE=?,STAGECOUNT=?,NORTHEXIT=?,EASTEXIT=?,SOUTHEXIT=?,WESTEXIT=?,LAYER=? WHERE ID=?"
            );
            ps.setByte(1, this.getType().getCode());
            ps.setLong(2, this.getLastUsed());
            ps.setFloat(3, this.getCurrentQL());
            ps.setFloat(4, this.getOriginalQL());
            ps.setFloat(5, this.getDamage());
            ps.setLong(6, this.getStructureId());
            ps.setByte(7, this.getState());
            ps.setByte(8, this.getMaterial().getCode());
            ps.setInt(9, this.getHeightOffset());
            ps.setByte(10, this.getDir());
            ps.setByte(11, this.getSlope());
            ps.setInt(12, this.getMaterialCount());
            ps.setInt(13, this.getNorthExit());
            ps.setInt(14, this.getEastExit());
            ps.setInt(15, this.getSouthExit());
            ps.setInt(16, this.getWestExit());
            ps.setInt(17, this.getLayer());
            ps.setInt(18, this.getNumber());
            ps.executeUpdate();
         } else {
            ps = dbcon.prepareStatement(
               "INSERT INTO BRIDGEPARTS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE, MATERIAL, HEIGHTOFFSET, DIR, SLOPE, STAGECOUNT, NORTHEXIT, EASTEXIT, SOUTHEXIT, WESTEXIT, LAYER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
               1
            );
            ps.setByte(1, this.getType().getCode());
            ps.setLong(2, this.getLastUsed());
            ps.setFloat(3, this.getCurrentQL());
            ps.setFloat(4, this.getOriginalQL());
            ps.setFloat(5, this.getDamage());
            ps.setLong(6, this.getStructureId());
            ps.setInt(7, this.getTileX());
            ps.setInt(8, this.getTileY());
            ps.setByte(9, this.getState());
            ps.setByte(10, this.getMaterial().getCode());
            ps.setInt(11, this.getHeightOffset());
            ps.setByte(12, this.getDir());
            ps.setByte(13, this.getSlope());
            ps.setInt(14, this.getMaterialCount());
            ps.setInt(15, this.getNorthExit());
            ps.setInt(16, this.getEastExit());
            ps.setInt(17, this.getSouthExit());
            ps.setInt(18, this.getWestExit());
            ps.setInt(19, this.getLayer());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
               this.setNumber(rs.getInt(1));
            }
         }
      } catch (SQLException var8) {
         throw new IOException(var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public boolean setDamage(float aDamage) {
      boolean forcePlan = false;
      if (this.isIndestructible()) {
         return false;
      } else {
         if (aDamage >= 100.0F) {
            VolaTile tile = this.getTile();
            forcePlan = true;
            BridgeConstants.BridgeState oldBridgeState = this.getBridgePartState();
            this.setBridgePartState(BridgeConstants.BridgeState.PLANNED);
            this.setQualityLevel(1.0F);
            this.saveRoadType((byte)0);
            if (tile != null) {
               tile.updateBridgePart(this);
               if (oldBridgeState != BridgeConstants.BridgeState.PLANNED) {
                  BridgeConstants.BridgeType bType = this.getType();
                  switch(this.getMaterial()) {
                     case BRICK:
                     case MARBLE:
                     case POTTERY:
                     case RENDERED:
                     case ROUNDED_STONE:
                     case SANDSTONE:
                     case SLATE:
                        if (bType.isSupportType()) {
                           this.damageAdjacent("abutment", 50);
                        } else if (bType.isAbutment()) {
                           this.damageAdjacent("bracing", 25);
                        } else if (bType.isBracing()) {
                           this.damageAdjacent("crown", 10);
                           this.damageAdjacent("floating", 10);
                        }
                        break;
                     case WOOD:
                        if (bType.isSupportType()) {
                           this.damageAdjacent("abutment", 50);
                           this.damageAdjacent("crown", 25);
                        } else if (bType.isAbutment()) {
                           this.damageAdjacent("crown", 10);
                        }
                        break;
                     case ROPE:
                        if (bType.isAbutment()) {
                           this.damageAdjacent("crown", 50);
                        }
                  }
               }
            }
         }

         if (this.damage != aDamage) {
            boolean updateState = false;
            if (this.damage >= 60.0F && aDamage < 60.0F || this.damage < 60.0F && aDamage >= 60.0F) {
               updateState = true;
            }

            this.damage = aDamage;
            if (forcePlan) {
               this.damage = 0.0F;
            }

            if (this.damage < 100.0F) {
               Connection dbcon = null;
               PreparedStatement ps = null;

               try {
                  dbcon = DbConnector.getZonesDbCon();
                  ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET DAMAGE=? WHERE ID=?");
                  ps.setFloat(1, this.getDamage());
                  ps.setInt(2, this.getNumber());
                  ps.executeUpdate();
               } catch (SQLException var10) {
                  logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + var10.getMessage(), (Throwable)var10);
               } finally {
                  DbUtilities.closeDatabaseObjects(ps, null);
                  DbConnector.returnConnection(dbcon);
               }

               if (updateState) {
                  VolaTile tile = this.getTile();
                  if (tile != null) {
                     this.getTile().updateBridgePartDamageState(this);
                  }
               }
            } else {
               VolaTile t = this.getTile();
               if (t != null) {
                  t.removeBridgePart(this);
               }

               this.delete();
            }
         }

         return this.damage >= 100.0F;
      }
   }

   private void damageAdjacent(String typeName, int addDamage) {
      VolaTile vtNorth = Zones.getTileOrNull(this.getTileX(), this.getTileY() - 1, this.isOnSurface());
      if (vtNorth != null) {
         Structure structNorth = vtNorth.getStructure();
         if (structNorth != null && structNorth.getWurmId() == this.getStructureId()) {
            BridgePart[] bps = vtNorth.getBridgeParts();
            if (bps.length == 1 && bps[0].getType().getName().equalsIgnoreCase(typeName)) {
               bps[0].setDamage(bps[0].getDamage() + (float)addDamage);
            }
         }
      }

      VolaTile vtEast = Zones.getTileOrNull(this.getTileX() + 1, this.getTileY(), this.isOnSurface());
      if (vtEast != null) {
         Structure structEast = vtEast.getStructure();
         if (structEast != null && structEast.getWurmId() == this.getStructureId()) {
            BridgePart[] bps = vtEast.getBridgeParts();
            if (bps.length == 1 && bps[0].getType().getName().equalsIgnoreCase(typeName)) {
               bps[0].setDamage(bps[0].getDamage() + (float)addDamage);
            }
         }
      }

      VolaTile vtSouth = Zones.getTileOrNull(this.getTileX(), this.getTileY() + 1, this.isOnSurface());
      if (vtSouth != null) {
         Structure structSouth = vtSouth.getStructure();
         if (structSouth != null && structSouth.getWurmId() == this.getStructureId()) {
            BridgePart[] bps = vtSouth.getBridgeParts();
            if (bps.length == 1 && bps[0].getType().getName().equalsIgnoreCase(typeName)) {
               bps[0].setDamage(bps[0].getDamage() + (float)addDamage);
            }
         }
      }

      VolaTile vtWest = Zones.getTileOrNull(this.getTileX() - 1, this.getTileY(), this.isOnSurface());
      if (vtWest != null) {
         Structure structWest = vtWest.getStructure();
         if (structWest != null && structWest.getWurmId() == this.getStructureId()) {
            BridgePart[] bps = vtWest.getBridgeParts();
            if (bps.length == 1 && bps[0].getType().getName().equalsIgnoreCase(typeName)) {
               bps[0].setDamage(bps[0].getDamage() + (float)addDamage);
            }
         }
      }
   }

   @Override
   public void setLastUsed(long now) {
      if (this.lastUsed != now) {
         this.lastUsed = now;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET LASTMAINTAINED=? WHERE ID=?");
            ps.setLong(1, this.lastUsed);
            ps.setInt(2, this.getNumber());
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + var9.getMessage(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public boolean setQualityLevel(float ql) {
      if (ql > 100.0F) {
         ql = 100.0F;
      }

      if (this.currentQL != ql) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.currentQL = ql;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET CURRENTQL=? WHERE ID=?");
            ps.setFloat(1, this.currentQL);
            ps.setInt(2, this.getNumber());
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, this.getName() + ", " + this.getNumber() + " " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }

      return ql >= 100.0F;
   }

   private boolean exists(Connection dbcon) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var4;
      try {
         ps = dbcon.prepareStatement("SELECT * FROM BRIDGEPARTS WHERE ID=?");
         ps.setInt(1, this.getNumber());
         rs = ps.executeQuery();
         var4 = rs.next();
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      return var4;
   }

   @Override
   public void delete() {
      MissionTargets.destroyMissionTarget(this.getId(), true);
      MethodsHighways.removeNearbyMarkers(this);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM BRIDGEPARTS WHERE ID=?");
         ps.setInt(1, this.getNumber());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete bridge part with id " + this.getNumber(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public long getTempId() {
      return -10L;
   }

   @Override
   public void savePermissions() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET SETTINGS=? WHERE ID=?");
         ps.setLong(1, (long)this.permissions.getPermissions());
         ps.setLong(2, (long)this.getNumber());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save settings for bridge part with id " + this.getNumber(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void saveRoadType(byte roadType) {
      if (this.roadType != roadType) {
         this.roadType = roadType;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE BRIDGEPARTS SET ROADTYPE=? WHERE ID=?");
            ps.setByte(1, this.roadType);
            ps.setLong(2, (long)this.getNumber());
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to save roadtype for bridge part with id " + this.getNumber(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public final boolean isOnSouthBorder(TilePos pos) {
      return false;
   }

   @Override
   public final boolean isOnNorthBorder(TilePos pos) {
      return false;
   }

   @Override
   public final boolean isOnWestBorder(TilePos pos) {
      return false;
   }

   @Override
   public final boolean isOnEastBorder(TilePos pos) {
      return false;
   }
}
