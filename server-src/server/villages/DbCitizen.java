package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbCitizen extends Citizen {
   private static final Logger logger = Logger.getLogger(DbCitizen.class.getName());
   private static final String SET_ROLEID = "UPDATE CITIZENS SET ROLEID=? WHERE WURMID=?";
   private static final String CREATE_CITIZEN = "INSERT INTO CITIZENS (WURMID, VILLAGEID, ROLEID, VOTEDATE, VOTEDFOR) VALUES (?,?,?,?,?)";
   private static final String GET_CITIZEN = "SELECT * FROM CITIZENS WHERE WURMID=?";
   private static final String SET_VOTEDATE = "UPDATE CITIZENS SET VOTEDATE=? WHERE WURMID=?";
   private static final String SET_VOTEDFOR = "UPDATE CITIZENS SET VOTEDFOR=? WHERE WURMID=?";

   DbCitizen(long aWurmId, String aName, VillageRole aRole, long aVotedate, long aVotedfor) {
      super(aWurmId, aName, aRole, aVotedate, aVotedfor);
   }

   @Override
   void create(Creature creature, int villageId) throws IOException {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         int num = this.exists(dbcon);
         if (num >= 0) {
            try {
               Village village = Villages.getVillage(num);
               creature.getCommunicator().sendSafeServerMessage("Removing your " + village.getName() + " citizenship.");
               village.removeCitizen(creature);
            } catch (NoSuchVillageException var11) {
               logger.log(Level.WARNING, "Citizens have village id " + num + " but it can't be found.", (Throwable)var11);
            }
         }

         ps = dbcon.prepareStatement("INSERT INTO CITIZENS (WURMID, VILLAGEID, ROLEID, VOTEDATE, VOTEDFOR) VALUES (?,?,?,?,?)");
         ps.setLong(1, this.wurmId);
         ps.setInt(2, villageId);
         ps.setInt(3, this.role.getId());
         ps.setLong(4, this.voteDate);
         ps.setLong(5, this.votedFor);
         ps.executeUpdate();
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to set status for citizen " + this.name + ": " + var12.getMessage(), (Throwable)var12);
         throw new IOException(var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void setRole(VillageRole aRole) throws IOException {
      if (this.role != aRole) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.role = aRole;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE CITIZENS SET ROLEID=? WHERE WURMID=?");
            ps.setInt(1, aRole.getId());
            ps.setLong(2, this.wurmId);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to set status for citizen " + this.name + ": " + var8.getMessage(), (Throwable)var8);
            throw new IOException(var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   void setVoteDate(long votedate) throws IOException {
      if (this.voteDate != votedate) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.voteDate = votedate;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE CITIZENS SET VOTEDATE=? WHERE WURMID=?");
            ps.setLong(1, this.voteDate);
            ps.setLong(2, this.wurmId);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to set vote date for citizen " + this.name + ": " + var9.getMessage(), (Throwable)var9);
            throw new IOException(var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   void setVotedFor(long votedfor) throws IOException {
      if (this.votedFor != votedfor) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.votedFor = votedfor;
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("UPDATE CITIZENS SET VOTEDFOR=? WHERE WURMID=?");
            ps.setLong(1, this.votedFor);
            ps.setLong(2, this.wurmId);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to set votedFor for citizen " + this.name + ": " + var9.getMessage(), (Throwable)var9);
            throw new IOException(var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   private int exists(Connection dbcon) throws IOException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         ps = dbcon.prepareStatement("SELECT * FROM CITIZENS WHERE WURMID=?");
         ps.setLong(1, this.wurmId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            return -1;
         }

         int village = rs.getInt("VILLAGEID");
         var5 = village;
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to check if citizen " + this.name + " exists: " + var9.getMessage(), (Throwable)var9);
         throw new IOException(var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      return var5;
   }
}
