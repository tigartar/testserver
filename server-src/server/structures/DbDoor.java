package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbDoor extends Door implements MiscConstants {
   private static final Logger logger = Logger.getLogger(DbDoor.class.getName());
   private static final String GET_DOOR = "SELECT * FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
   private static final String EXISTS_DOOR = "SELECT 1 FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
   private static final String CREATE_DOOR = "INSERT INTO DOORS (LOCKID,NAME,SETTINGS,STRUCTURE,INNERWALL) VALUES(?,?,?,?,?)";
   private static final String UPDATE_DOOR = "UPDATE DOORS SET LOCKID=?,NAME=?,SETTINGS=? WHERE STRUCTURE=? AND INNERWALL=?";
   private static final String DELETE_DOOR = "DELETE FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?";
   private static final String SET_NAME = "UPDATE DOORS SET NAME=? WHERE INNERWALL=?";

   public DbDoor(Wall aWall) {
      super(aWall);
   }

   @Override
   public void save() throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         String string = "INSERT INTO DOORS (LOCKID,NAME,SETTINGS,STRUCTURE,INNERWALL) VALUES(?,?,?,?,?)";
         if (this.exists(dbcon)) {
            string = "UPDATE DOORS SET LOCKID=?,NAME=?,SETTINGS=? WHERE STRUCTURE=? AND INNERWALL=?";
         }

         ps = dbcon.prepareStatement(string);
         ps.setLong(1, this.lock);
         ps.setString(2, this.name);
         ps.setInt(3, 0);
         ps.setLong(4, this.structure);
         long iid = this.wall.getId();
         ps.setLong(5, iid);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to save door for structure with id " + this.structure, (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
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
         ps = dbcon.prepareStatement("SELECT * FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
         ps.setLong(1, this.structure);
         ps.setLong(2, this.wall.getId());
         rs = ps.executeQuery();
         if (rs.next()) {
            this.lock = rs.getLong("LOCKID");
            this.name = rs.getString("NAME");
         } else {
            this.save();
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to load door for structure with id " + this.structure, (Throwable)var8);
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
         ps = dbcon.prepareStatement("SELECT 1 FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
         ps.setLong(1, this.structure);
         ps.setLong(2, this.wall.getId());
         rs = ps.executeQuery();
         var4 = rs.next();
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      return var4;
   }

   @Override
   public void delete() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM DOORS WHERE STRUCTURE=? AND INNERWALL=?");
         ps.setLong(1, this.structure);
         ps.setLong(2, this.wall.getId());
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to delete wall for structure with id " + this.structure, (Throwable)var8);
      } catch (Exception var9) {
         logger.log(Level.WARNING, this.structure + ":" + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      if (this.lock != -10L) {
         Items.decay(this.lock, null);
         this.lock = -10L;
      }
   }

   @Override
   public void setName(String aName) {
      String newname = aName.substring(0, Math.min(39, aName.length()));
      if (!this.getName().equals(newname)) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.setNewName(newname);
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE DOORS SET NAME=? WHERE INNERWALL=?");
            ps.setString(1, this.getName());
            ps.setLong(2, this.wall.getId());
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to set name to " + this.getName() + " for door with innerwall of " + this.wall.getId(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public boolean isItem() {
      return false;
   }
}
