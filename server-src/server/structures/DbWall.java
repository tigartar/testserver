package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbWall extends Wall {
   private static final Logger logger = Logger.getLogger(DbWall.class.getName());
   private static final String createWall = "insert into WALLS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL,DAMAGE, STRUCTURE, STARTX, STARTY, ENDX, ENDY, OUTERWALL, TILEX, TILEY, STATE,MATERIAL,ISINDOOR, HEIGHTOFFSET, LAYER, WALLORIENTATION) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String updateWall = "update WALLS set TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,ISINDOOR=?,HEIGHTOFFSET=?,LAYER=?,TILEX=?,TILEY=? where ID=?";
   private static final String getWall = "select * from WALLS where ID=?";
   private static final String deleteWall = "delete from WALLS where ID=?";
   private static final String setDamage = "update WALLS set DAMAGE=? where ID=?";
   private static final String setState = "update WALLS set STATE=?,MATERIAL=? where ID=?";
   private static final String setQL = "update WALLS set CURRENTQL=? where ID=?";
   private static final String setOrigQL = "update WALLS set ORIGINALQL=? where ID=?";
   private static final String setLastUsed = "update WALLS set LASTMAINTAINED=? where ID=?";
   private static final String setIsIndoor = "update WALLS set ISINDOOR=? where ID=?";
   private static final String setColor = "update WALLS set COLOR=? WHERE ID=?";
   private static final String setOrientation = "update WALLS set WALLORIENTATION=? WHERE ID=?";
   private static final String SET_SETTINGS = "UPDATE WALLS SET SETTINGS=? WHERE ID=?";

   public DbWall(
      StructureTypeEnum aType,
      int aTileX,
      int aTileY,
      int aStartX,
      int aStartY,
      int aEndX,
      int aEndY,
      float aQualityLevel,
      long aStructure,
      StructureMaterialEnum aMaterial,
      boolean aIsIndoor,
      int aHeightOffset,
      int aLayer
   ) {
      super(aType, aTileX, aTileY, aStartX, aStartY, aEndX, aEndY, aQualityLevel, aStructure, aMaterial, aIsIndoor, aHeightOffset, aLayer);
   }

   DbWall(
      int aNumber,
      StructureTypeEnum aType,
      int aTileX,
      int aTileY,
      int aStartX,
      int aStartY,
      int aEndX,
      int aEndY,
      float aQualityLevel,
      float aOriginalQl,
      float aDamage,
      long aStructure,
      long aLastUsed,
      StructureStateEnum aState,
      int aColor,
      StructureMaterialEnum aMaterial,
      boolean aIsIndoor,
      int aHeightOffset,
      int aLayer,
      boolean wallOrientation,
      int aSettings
   ) {
      super(
         aNumber,
         aType,
         aTileX,
         aTileY,
         aStartX,
         aStartY,
         aEndX,
         aEndY,
         aQualityLevel,
         aOriginalQl,
         aDamage,
         aStructure,
         aLastUsed,
         aState,
         aColor,
         aMaterial,
         aIsIndoor,
         aHeightOffset,
         aLayer,
         wallOrientation,
         aSettings
      );
   }

   public DbWall(int aNumber) throws IOException {
      super(aNumber, false);
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
               "update WALLS set TYPE=?, LASTMAINTAINED =?, CURRENTQL=?, ORIGINALQL=?,DAMAGE=?, STRUCTURE=?, STATE=?,MATERIAL=?,ISINDOOR=?,HEIGHTOFFSET=?,LAYER=?,TILEX=?,TILEY=? where ID=?"
            );
            ps.setByte(1, this.type.value);
            ps.setLong(2, this.lastUsed);
            ps.setFloat(3, this.currentQL);
            ps.setFloat(4, this.originalQL);
            ps.setFloat(5, this.damage);
            ps.setLong(6, this.structureId);
            ps.setByte(7, this.state.state);
            ps.setByte(8, this.getMaterial().material);
            ps.setBoolean(9, this.isIndoor());
            ps.setInt(10, this.getHeight());
            ps.setInt(11, this.getLayer());
            ps.setInt(12, this.getTileX());
            ps.setInt(13, this.getTileY());
            ps.setInt(14, this.number);
            ps.executeUpdate();
         } else {
            ps = dbcon.prepareStatement(
               "insert into WALLS(TYPE, LASTMAINTAINED , CURRENTQL, ORIGINALQL,DAMAGE, STRUCTURE, STARTX, STARTY, ENDX, ENDY, OUTERWALL, TILEX, TILEY, STATE,MATERIAL,ISINDOOR, HEIGHTOFFSET, LAYER, WALLORIENTATION) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
               1
            );
            ps.setByte(1, this.type.value);
            ps.setLong(2, this.lastUsed);
            ps.setFloat(3, this.currentQL);
            ps.setFloat(4, this.originalQL);
            ps.setFloat(5, this.damage);
            ps.setLong(6, this.structureId);
            ps.setInt(7, this.getStartX());
            ps.setInt(8, this.getStartY());
            ps.setInt(9, this.getEndX());
            ps.setInt(10, this.getEndY());
            ps.setBoolean(11, false);
            ps.setInt(12, this.tilex);
            ps.setInt(13, this.tiley);
            ps.setByte(14, this.state.state);
            ps.setByte(15, this.getMaterial().material);
            ps.setBoolean(16, this.isIndoor());
            ps.setInt(17, this.getHeight());
            ps.setInt(18, this.getLayer());
            ps.setBoolean(19, false);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
               this.number = rs.getInt(1);
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
   void load() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("select * from WALLS where ID=?");
         ps.setInt(1, this.number);
         rs = ps.executeQuery();
         if (rs.next()) {
            this.x1 = rs.getInt("STARTX");
            this.x2 = rs.getInt("ENDX");
            this.y1 = rs.getInt("STARTY");
            this.y2 = rs.getInt("ENDY");
            this.tilex = rs.getInt("TILEX");
            this.tiley = rs.getInt("TILEY");
            this.currentQL = rs.getFloat("ORIGINALQL");
            this.originalQL = rs.getFloat("CURRENTQL");
            this.lastUsed = rs.getLong("LASTMAINTAINED");
            this.structureId = rs.getLong("STRUCTURE");
            this.type = StructureTypeEnum.getTypeByINDEX(rs.getByte("TYPE"));
            this.state = StructureStateEnum.getStateByValue(rs.getByte("STATE"));
            this.damage = rs.getFloat("DAMAGE");
            this.setColor(rs.getInt("COLOR"));
            this.setIndoor(rs.getBoolean("ISINDOOR"));
            this.heightOffset = rs.getInt("HEIGHTOFFSET");
            this.wallOrientationFlag = rs.getBoolean("WALLORIENTATION");
         } else {
            logger.log(Level.WARNING, "Failed to find wall with number " + this.number);
         }

         DbUtilities.closeDatabaseObjects(ps, rs);
         if (this.state.state <= StructureStateEnum.UNINITIALIZED.state) {
            this.state = StructureStateEnum.FINISHED;
            this.save();
         }

         if (this.type.value == 127) {
            this.type = StructureTypeEnum.PLAN;
            this.save();
         }

         if (this.type == StructureTypeEnum.RUBBLE) {
            addRubble(this);
         }
      } catch (SQLException var8) {
         throw new IOException(var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private boolean exists(Connection dbcon) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var4;
      try {
         ps = dbcon.prepareStatement("select * from WALLS where ID=?");
         ps.setInt(1, this.number);
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
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("delete from WALLS where ID=?");
         ps.setInt(1, this.number);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete wall with id " + this.number, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public boolean setDamage(float dam) {
      if (dam >= 100.0F) {
         if (Servers.localServer.testServer) {
            logger.fine(
               "TEMPORARY LOGGING FOR BUG #1264 - Destroying wall with ID:" + this.getId() + " which was part of structure with id:" + this.getStructureId()
            );
         }

         boolean forcePlan = false;
         VolaTile tile = this.getTile();
         if (tile != null) {
            Structure struct = tile.getStructure();
            if (struct != null && struct.wouldCreateFlyingStructureIfRemoved(this)) {
               forcePlan = true;
            }
         }

         if (MethodsStructure.isWallInsideStructure(this, this.isOnSurface()) && !forcePlan) {
            this.destroy();
            return true;
         }

         if (Servers.localServer.isChallengeServer()) {
            if (this.isFinished() && this.getType() != StructureTypeEnum.RUBBLE) {
               this.setAsRubble();
               return true;
            }

            dam = 0.0F;
            this.setAsPlan();
            this.setQualityLevel(1.0F);
         } else {
            dam = 0.0F;
            this.setAsPlan();
            this.setQualityLevel(1.0F);
         }
      }

      if (this.damage != dam) {
         boolean updateState = false;
         if (this.damage >= 60.0F && dam < 60.0F || this.damage < 60.0F && dam >= 60.0F) {
            updateState = true;
         }

         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.damage = dam;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set DAMAGE=? where ID=?");
            ps.setFloat(1, this.damage);
            ps.setInt(2, this.number);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to set damage to " + dam + " for wall with id " + this.number, (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }

         if (updateState) {
            VolaTile tile = this.getTile();
            if (tile != null) {
               this.getTile().updateWallDamageState(this);
            }
         }
      }

      return false;
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
            ps = dbcon.prepareStatement("update WALLS set CURRENTQL=? where ID=?");
            ps.setFloat(1, this.currentQL);
            ps.setInt(2, this.number);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set quality to " + ql + " for wall with id " + this.number, (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }

      return false;
   }

   @Override
   public void improveOrigQualityLevel(float ql) {
      if (ql > 100.0F) {
         ql = 100.0F;
      }

      if (this.originalQL != ql) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.originalQL = ql;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set ORIGINALQL=? where ID=?");
            ps.setFloat(1, this.originalQL);
            ps.setInt(2, this.number);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set original quality to " + ql + " for wall with id " + this.number, (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setIndoor(boolean indoor) {
      if (this.isIndoor != indoor) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.isIndoor = indoor;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set ISINDOOR=? where ID=?");
            ps.setBoolean(1, this.isIndoor);
            ps.setInt(2, this.number);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set indoor to " + indoor + " for wall with id " + this.number, (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setLastUsed(long last) {
      if (this.lastUsed != last) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.lastUsed = last;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set LASTMAINTAINED=? where ID=?");
            ps.setLong(1, last);
            ps.setInt(2, this.number);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to set lastUsed to " + last + " for wall with id " + this.number, (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setState(StructureStateEnum newState) {
      if (this.state != StructureStateEnum.FINISHED || newState == StructureStateEnum.INITIALIZED) {
         if (newState.state >= this.getFinalState().state) {
            newState = StructureStateEnum.FINISHED;
         }

         if (this.state != newState) {
            Connection dbcon = null;
            PreparedStatement ps = null;

            try {
               this.state = newState;
               dbcon = DbConnector.getZonesDbCon();
               ps = dbcon.prepareStatement("update WALLS set STATE=?,MATERIAL=? where ID=?");
               ps.setByte(1, this.state.state);
               ps.setByte(2, this.getMaterial().material);
               ps.setInt(3, this.number);
               ps.executeUpdate();
            } catch (SQLException var8) {
               logger.log(Level.WARNING, "Failed to set state to " + this.state + " for wall with id " + this.number, (Throwable)var8);
            } finally {
               DbUtilities.closeDatabaseObjects(ps, null);
               DbConnector.returnConnection(dbcon);
            }
         }
      }
   }

   @Override
   public void setWallOrientation(boolean rotated) {
      if (this.wallOrientationFlag != rotated) {
         this.wallOrientationFlag = rotated;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set WALLORIENTATION=? WHERE ID=?");
            ps.setBoolean(1, this.wallOrientationFlag);
            ps.setInt(2, this.number);
            ps.executeUpdate();

            try {
               Structure struct = Structures.getStructure(this.structureId);
               VolaTile tile = struct.getTileFor(this);
               if (tile != null) {
                  tile.updateWall(this);
               }
            } catch (NoSuchStructureException var10) {
               logger.log(
                  Level.WARNING,
                  "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + var10.getMessage(),
                  (Throwable)var10
               );
            }
         } catch (SQLException var11) {
            logger.log(Level.WARNING, "Failed to set wall orientation to " + this.wallOrientationFlag + " for wall with id " + this.number, (Throwable)var11);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   boolean changeColor(int newcolor) {
      if (this.getColor() != newcolor) {
         this.color = newcolor;
         Connection dbcon = null;
         PreparedStatement ps = null;

         boolean tile;
         try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("update WALLS set COLOR=? WHERE ID=?");
            ps.setInt(1, newcolor);
            ps.setInt(2, this.number);
            ps.executeUpdate();

            try {
               Structure struct = Structures.getStructure(this.structureId);
               VolaTile tile = struct.getTileFor(this);
               if (tile != null) {
                  tile.updateWall(this);
               }
            } catch (NoSuchStructureException var10) {
               logger.log(
                  Level.WARNING,
                  "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + var10.getMessage(),
                  (Throwable)var10
               );
            }

            return true;
         } catch (SQLException var11) {
            logger.log(Level.WARNING, "Failed to set color to " + this.getColor() + " for wall with id " + this.number, (Throwable)var11);
            tile = true;
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }

         return tile;
      } else {
         return false;
      }
   }

   @Override
   public void savePermissions() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("UPDATE WALLS SET SETTINGS=? WHERE ID=?");
         ps.setLong(1, (long)this.permissions.getPermissions());
         ps.setLong(2, (long)this.number);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save settings for wall with id " + this.number, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
