package com.wurmonline.server.effects;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbEffect extends Effect implements CounterTypes {
   private static final Logger logger = Logger.getLogger(DbEffect.class.getName());
   private static final long serialVersionUID = 1839666903728378027L;
   private static final String CREATE_EFFECT_SQL = "insert into EFFECTS(OWNER, TYPE, POSX, POSY, POSZ, STARTTIME) values(?,?,?,?,?,?)";
   private static final String UPDATE_EFFECT_SQL = "update EFFECTS set OWNER=?, TYPE=?, POSX=?, POSY=?, POSZ=? where ID=?";
   private static final String GET_EFFECT_SQL = "select * from EFFECTS where ID=?";
   private static final String DELETE_EFFECT_SQL = "delete from EFFECTS where ID=?";

   DbEffect(long aOwner, short aType, float aPosX, float aPosY, float aPosZ, boolean aSurfaced) {
      super(aOwner, aType, aPosX, aPosY, aPosZ, aSurfaced);
   }

   DbEffect(long aOwner, int aNumber) throws IOException {
      super(aOwner, aNumber);
   }

   DbEffect(int num, long ownerid, short typ, float posx, float posy, float posz, long stime) {
      super(num, ownerid, typ, posx, posy, posz, stime);
   }

   @Override
   public void save() throws IOException {
      if (WurmId.getType(this.getOwner()) != 6) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            if (WurmId.getType(this.getOwner()) == 2 || WurmId.getType(this.getOwner()) == 19 || WurmId.getType(this.getOwner()) == 20) {
               dbcon = DbConnector.getItemDbCon();
               if (this.exists(dbcon)) {
                  ps = dbcon.prepareStatement("update EFFECTS set OWNER=?, TYPE=?, POSX=?, POSY=?, POSZ=? where ID=?");
                  ps.setLong(1, this.getOwner());
                  ps.setShort(2, this.getType());
                  ps.setFloat(3, this.getPosX());
                  ps.setFloat(4, this.getPosY());
                  ps.setFloat(5, this.getPosZ());
                  ps.setInt(6, this.getId());
                  ps.executeUpdate();
               } else {
                  ps = dbcon.prepareStatement("insert into EFFECTS(OWNER, TYPE, POSX, POSY, POSZ, STARTTIME) values(?,?,?,?,?,?)", 1);
                  ps.setLong(1, this.getOwner());
                  ps.setShort(2, this.getType());
                  ps.setFloat(3, this.getPosX());
                  ps.setFloat(4, this.getPosY());
                  ps.setFloat(5, this.getPosZ());
                  ps.setLong(6, this.getStartTime());
                  ps.executeUpdate();
                  rs = ps.getGeneratedKeys();
                  if (rs.next()) {
                     this.setId(rs.getInt(1));
                  }
               }
            } else if (this.getId() == 0) {
               this.setId(-Math.abs(Server.rand.nextInt()));
            }
         } catch (SQLException var8) {
            throw new IOException(var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      } else if (this.getId() == 0) {
         this.setId(-Math.abs(Server.rand.nextInt()));
      }
   }

   @Override
   void load() throws IOException {
      if (WurmId.getType(this.getOwner()) != 6) {
         Connection dbcon = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            if (WurmId.getType(this.getOwner()) == 2 || WurmId.getType(this.getOwner()) == 19 || WurmId.getType(this.getOwner()) == 20) {
               dbcon = DbConnector.getItemDbCon();
               ps = dbcon.prepareStatement("select * from EFFECTS where ID=?");
               ps.setInt(1, this.getId());
               rs = ps.executeQuery();
               if (rs.next()) {
                  this.setPosX(rs.getFloat("POSX"));
                  this.setPosY(rs.getFloat("POSY"));
                  this.setPosZ(rs.getFloat("POSZ"));
                  this.setType(rs.getShort("TYPE"));
                  this.setOwner(rs.getLong("OWNER"));
                  this.setStartTime(rs.getLong("STARTTIME"));
               } else {
                  logger.log(Level.WARNING, "Failed to find effect with number " + this.getId());
               }
            }
         } catch (SQLException var8) {
            throw new IOException(var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   void delete() {
      if (WurmId.getType(this.getOwner()) != 6) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            if (WurmId.getType(this.getOwner()) == 2 || WurmId.getType(this.getOwner()) == 19 || WurmId.getType(this.getOwner()) == 20) {
               dbcon = DbConnector.getItemDbCon();
               ps = dbcon.prepareStatement("delete from EFFECTS where ID=?");
               ps.setInt(1, this.getId());
               ps.executeUpdate();
            }
         } catch (SQLException var7) {
            logger.log(Level.WARNING, "Failed to delete effect with id " + this.getId(), (Throwable)var7);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   private boolean exists(Connection dbcon) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var4;
      try {
         ps = dbcon.prepareStatement("select * from EFFECTS where ID=?");
         ps.setInt(1, this.getId());
         rs = ps.executeQuery();
         var4 = rs.next();
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      return var4;
   }
}
