package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WarDeclaration {
   private static final String DELETE = "DELETE FROM VILLAGEWARDECLARATIONS WHERE VILLONE=? AND VILLTWO=?";
   private static final String CREATE = "INSERT INTO VILLAGEWARDECLARATIONS (VILLONE, VILLTWO,DECLARETIME ) VALUES (?,?,?)";
   public final Village receiver;
   public final Village declarer;
   public final long time;
   private static final Logger logger = Logger.getLogger(WarDeclaration.class.getName());

   WarDeclaration(Village aDeclarer, Village aReceiver) {
      this.declarer = aDeclarer;
      this.receiver = aReceiver;
      this.time = System.currentTimeMillis();
      this.save();
   }

   WarDeclaration(Village aDeclarer, Village aReceiver, long aTime) {
      this.declarer = aDeclarer;
      this.receiver = aReceiver;
      this.time = aTime;
   }

   public void accept() {
      if (this.receiver.warDeclarations != null) {
         this.receiver.warDeclarations.remove(this.declarer);
      }

      if (this.declarer.warDeclarations != null) {
         this.declarer.warDeclarations.remove(this.receiver);
      }

      Villages.createWar(this.declarer, this.receiver);
      this.delete();
   }

   public void dissolve(boolean expire) {
      if (this.receiver.warDeclarations != null) {
         if (expire) {
            this.receiver.broadCastSafe("The declaration of war from " + this.declarer.getName() + " expires.");
         } else {
            this.receiver.broadCastSafe(this.declarer.getName() + " has withdrawn their declaration of war.");
         }

         this.receiver.warDeclarations.remove(this.declarer);
      }

      if (this.declarer.warDeclarations != null) {
         if (expire) {
            this.declarer.broadCastSafe(this.receiver.getName() + " lets your declaration of war expire.");
         } else {
            this.declarer.broadCastSafe("You withdraw your declaration of war with " + this.receiver.getName() + ".");
         }

         this.declarer.warDeclarations.remove(this.receiver);
      }

      this.delete();
   }

   private void save() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("INSERT INTO VILLAGEWARDECLARATIONS (VILLONE, VILLTWO,DECLARETIME ) VALUES (?,?,?)");
         ps.setInt(1, this.declarer.getId());
         ps.setInt(2, this.receiver.getId());
         ps.setLong(3, this.time);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to create war between " + this.declarer.getName() + " and " + this.receiver.getName(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   void delete() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VILLAGEWARDECLARATIONS WHERE VILLONE=? AND VILLTWO=?");
         ps.setInt(1, this.declarer.getId());
         ps.setInt(2, this.receiver.getId());
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete war between " + this.declarer.getName() + " and " + this.receiver.getName(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
