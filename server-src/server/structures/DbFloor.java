package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.StructureConstants;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbFloor extends Floor {
   private static final String CREATE_FLOOR = "INSERT INTO FLOORS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE,COLOR, MATERIAL,HEIGHTOFFSET,LAYER,DIR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String UPDATE_FLOOR = "UPDATE FLOORS SET TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=? WHERE ID=?";
   private static final String GET_FLOOR = "SELECT * FROM FLOORS WHERE ID=?";
   private static final String DELETE_FLOOR = "DELETE FROM FLOORS WHERE ID=?";
   private static final String SET_DAMAGE = "UPDATE FLOORS SET DAMAGE=? WHERE ID=?";
   private static final String SET_QUALITY_LEVEL = "UPDATE FLOORS SET CURRENTQL=? WHERE ID=?";
   private static final String SET_STATE = "UPDATE FLOORS SET STATE=?,MATERIAL=? WHERE ID=?";
   private static final String SET_LAST_USED = "UPDATE FLOORS SET LASTMAINTAINED=? WHERE ID=?";
   private static final String SET_SETTINGS = "UPDATE FLOORS SET SETTINGS=? WHERE ID=?";
   private static final Logger logger = Logger.getLogger(DbWall.class.getName());

   @Override
   public boolean isFence() {
      return false;
   }

   @Override
   public boolean isWall() {
      return false;
   }

   public DbFloor(
      int id,
      StructureConstants.FloorType floorType,
      int tilex,
      int tiley,
      byte aDbState,
      int heightOffset,
      float currentQl,
      long structureId,
      StructureConstants.FloorMaterial floorMaterial,
      int layer,
      float origQL,
      float aDamage,
      long lastmaintained,
      byte dir
   ) {
      super(id, floorType, tilex, tiley, aDbState, heightOffset, currentQl, structureId, floorMaterial, layer, origQL, aDamage, lastmaintained, dir);
   }

   public DbFloor(
      StructureConstants.FloorType floorType,
      int tilex,
      int tiley,
      int heightOffset,
      float qualityLevel,
      long structure,
      StructureConstants.FloorMaterial material,
      int layer
   ) {
      super(floorType, tilex, tiley, heightOffset, qualityLevel, structure, material, layer);
   }

   @Override
   protected void setState(byte newState) {
      if (this.dbState != newState) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.dbState = newState;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE FLOORS SET STATE=?,MATERIAL=? WHERE ID=?");
            ps.setByte(1, this.dbState);
            ps.setByte(2, this.getMaterial().getCode());
            ps.setInt(3, this.getNumber());
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set state to " + newState + " for floor with id " + this.getNumber(), (Throwable)var8);
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
               "UPDATE FLOORS SET TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,HEIGHTOFFSET=?,DIR=? WHERE ID=?"
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
            ps.setInt(11, this.getNumber());
            ps.executeUpdate();
         } else {
            ps = dbcon.prepareStatement(
               "INSERT INTO FLOORS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL, DAMAGE, STRUCTURE, TILEX, TILEY, STATE,COLOR, MATERIAL,HEIGHTOFFSET,LAYER,DIR) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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
            ps.setInt(10, this.getColor());
            ps.setByte(11, this.getMaterial().getCode());
            ps.setInt(12, this.getHeightOffset());
            ps.setByte(13, this.getLayer());
            ps.setByte(14, this.getDir());
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
            if (tile != null) {
               Structure struct = tile.getStructure();
               if (struct != null && struct.wouldCreateFlyingStructureIfRemoved(this)) {
                  forcePlan = true;
               }
            }

            if (forcePlan) {
               this.setFloorState(StructureConstants.FloorState.PLANNING);
               this.setQualityLevel(1.0F);
               if (tile != null) {
                  tile.updateFloor(this);
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
                  ps = dbcon.prepareStatement("UPDATE FLOORS SET DAMAGE=? WHERE ID=?");
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
                     this.getTile().updateFloorDamageState(this);
                  }
               }
            } else {
               VolaTile t = this.getTile();
               if (t != null) {
                  t.removeFloor((Floor)this);
               }

               this.delete();
            }
         }

         return this.damage >= 100.0F;
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
            ps = dbcon.prepareStatement("UPDATE FLOORS SET LASTMAINTAINED=? WHERE ID=?");
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
            ps = dbcon.prepareStatement("UPDATE FLOORS SET CURRENTQL=? WHERE ID=?");
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
         ps = dbcon.prepareStatement("SELECT * FROM FLOORS WHERE ID=?");
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
         ps = dbcon.prepareStatement("DELETE FROM FLOORS WHERE ID=?");
         ps.setInt(1, this.getNumber());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete floor with id " + this.getNumber(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void savePermissions() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("UPDATE FLOORS SET SETTINGS=? WHERE ID=?");
         ps.setLong(1, (long)this.permissions.getPermissions());
         ps.setLong(2, (long)this.getNumber());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save settings for floor with id " + this.getNumber(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
