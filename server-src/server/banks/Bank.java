package com.wurmonline.server.banks;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.concurrency.Pollable;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Bank implements MiscConstants, TimeConstants, Pollable {
   public static final String VERSION = "$Revision: 1.2 $";
   private static final String DELETESLOT = "DELETE FROM BANKS_ITEMS WHERE ITEMID=?";
   public final long owner;
   private final long lastPolled;
   public final long id;
   public long startedMoving = -10L;
   public final int size;
   public final BankSlot[] slots;
   public int currentVillage = -10;
   public int targetVillage = -10;
   public boolean open = false;
   private static final String CREATE = "INSERT INTO BANKS (WURMID,OWNER,LASTPOLLED,STARTEDMOVE,SIZE,CURRENTVILLAGE,TARGETVILLAGE) VALUES(?,?,?,?,?,?,?)";
   private static final String LOADITEMS = "SELECT * FROM BANKS_ITEMS WHERE BANKID=?";
   private static final String MOVEINFO = "UPDATE BANKS SET STARTEDMOVE=?,TARGETVILLAGE=?,CURRENTVILLAGE=? WHERE WURMID=?";
   private static final Logger logger = Logger.getLogger(Bank.class.getName());

   public Bank(long aOwnerId, int aSize, int aVillage) {
      this.owner = aOwnerId;
      this.size = aSize;
      this.currentVillage = aVillage;
      this.lastPolled = System.currentTimeMillis();
      this.id = WurmId.getNextBankId();
      this.slots = new BankSlot[aSize];
      this.save();
   }

   public Bank(long aId, long aOwnerId, int aSize, long aLastPolled, long aStartedMoving, int aCurrentVillage, int aTargetVillage) {
      this.owner = aOwnerId;
      this.size = aSize;
      this.id = aId;
      this.startedMoving = aStartedMoving;
      this.lastPolled = aLastPolled;
      this.currentVillage = aCurrentVillage;
      this.targetVillage = aTargetVillage;
      this.slots = new BankSlot[aSize];
      this.loadAllItems();
   }

   public void open() throws BankUnavailableException {
      if (this.startedMoving > 0L) {
         Village target = null;

         try {
            target = Villages.getVillage(this.targetVillage);
         } catch (NoSuchVillageException var4) {
         }

         if (target == null) {
            throw new BankUnavailableException("The bank account is put on hold. Please select a new village.");
         }

         long now = System.currentTimeMillis();
         if (this.startedMoving + 86400000L >= now) {
            throw new BankUnavailableException(
               "The bank account is moving to "
                  + target.getName()
                  + ". It will arrive in approximately "
                  + Server.getTimeFor(this.startedMoving + 86400000L - now)
                  + "."
            );
         }

         this.poll(now);
      }

      this.open = true;
   }

   public void pollItems(long now) {
      for(int x = 0; x < this.slots.length; ++x) {
         if (this.slots[x] != null) {
            if (this.slots[x].item.isFood()) {
               this.slots[x].item.setDamage(this.slots[x].item.getDamage() + 10.0F);
            }

            this.slots[x].item.lastMaintained = now;
         }
      }
   }

   @Override
   public void poll(long now) {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Bank polling now: " + now + ", id: " + this.id);
      }

      if (this.startedMoving > 0L && this.startedMoving + 86400000L < now) {
         this.stopMoving();
      }
   }

   public boolean removeItem(Item item) {
      for(int x = 0; x < this.slots.length; ++x) {
         if (this.slots[x] != null && this.slots[x].item == item) {
            this.slots[x].delete();
            this.slots[x] = null;
            item.setBanked(false);
            return true;
         }
      }

      return false;
   }

   public boolean addItem(Item item) {
      for(int x = 0; x < this.slots.length; ++x) {
         if (this.slots[x] == null) {
            this.slots[x] = new BankSlot(item, this.id, false, System.currentTimeMillis(), true);
            item.setBanked(true);
            if (item.isCoin()) {
               Server.getInstance().transaction(item.getWurmId(), item.lastOwner, this.id, "Banked", (long)item.getValue());
            }

            return true;
         }
      }

      return false;
   }

   private void save() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("INSERT INTO BANKS (WURMID,OWNER,LASTPOLLED,STARTEDMOVE,SIZE,CURRENTVILLAGE,TARGETVILLAGE) VALUES(?,?,?,?,?,?,?)");
         ps.setLong(1, this.id);
         ps.setLong(2, this.owner);
         ps.setLong(3, this.lastPolled);
         ps.setLong(4, this.startedMoving);
         ps.setInt(5, this.size);
         ps.setInt(6, this.currentVillage);
         ps.setInt(7, this.targetVillage);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(
            Level.WARNING,
            "Failed to create bank account for owner " + this.owner + ", SqlState: " + var8.getSQLState() + ", ErrorCode: " + var8.getErrorCode(),
            (Throwable)var8
         );
         Exception lNext = var8.getNextException();
         if (lNext != null) {
            logger.log(Level.WARNING, "Failed to create bank account for owner " + this.owner + ", Next Exception", (Throwable)lNext);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public Village getCurrentVillage() throws BankUnavailableException {
      Village village = null;

      try {
         return Villages.getVillage(this.currentVillage);
      } catch (NoSuchVillageException var3) {
         throw new BankUnavailableException("The bank account is currently not located in a village.");
      }
   }

   public boolean startMoving(int aTargetVillage) {
      if (!this.open) {
         this.targetVillage = aTargetVillage;
         this.currentVillage = -10;
         this.startedMoving = System.currentTimeMillis();
         this.setMoveInfo();
         return true;
      } else {
         return false;
      }
   }

   public void stopMoving() {
      this.currentVillage = this.targetVillage;
      this.targetVillage = -10;
      this.startedMoving = -10L;
      this.setMoveInfo();
   }

   private void setMoveInfo() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE BANKS SET STARTEDMOVE=?,TARGETVILLAGE=?,CURRENTVILLAGE=? WHERE WURMID=?");
         ps.setLong(1, this.startedMoving);
         ps.setInt(2, this.targetVillage);
         ps.setInt(3, this.currentVillage);
         ps.setLong(4, this.id);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(
            Level.WARNING,
            "Failed to set move info for bank account with owner " + this.owner + ", SqlState: " + var8.getSQLState() + ", ErrorCode: " + var8.getErrorCode(),
            (Throwable)var8
         );
         Exception lNext = var8.getNextException();
         if (lNext != null) {
            logger.log(Level.WARNING, "Failed to set move info for bank account with owner " + this.owner + ", Next Exception", (Throwable)lNext);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void loadAllItems() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM BANKS_ITEMS WHERE BANKID=?");
         ps.setLong(1, this.id);
         rs = ps.executeQuery();
         long itemid = -10L;
         int x = 0;

         while(rs.next()) {
            try {
               itemid = rs.getLong("ITEMID");
               long inserted = rs.getLong("INSERTED");
               boolean stasis = rs.getBoolean("STASIS");
               Item item = Items.getItem(itemid);
               if (x < this.size) {
                  this.slots[x] = new BankSlot(item, this.id, stasis, inserted, false);
               } else {
                  logger.log(Level.WARNING, "Bank account with owner " + this.owner + " has too many items.");
               }

               ++x;
            } catch (NoSuchItemException var15) {
               this.deleteSlot(itemid);
               logger.log(Level.WARNING, itemid + " not found:" + var15.getMessage() + ". Deleting bank slot.");
            }
         }
      } catch (SQLException var16) {
         logger.log(Level.WARNING, "Failed to load bank items, SqlState: " + var16.getSQLState() + ", ErrorCode: " + var16.getErrorCode(), (Throwable)var16);
         Exception lNext = var16.getNextException();
         if (lNext != null) {
            logger.log(Level.WARNING, "Failed to load bank items, Next Exception", (Throwable)lNext);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void deleteSlot(long wurmid) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("DELETE FROM BANKS_ITEMS WHERE ITEMID=?");
         ps.setLong(1, wurmid);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(
            Level.WARNING,
            "Failed to delete bankslot for bank " + this.id + ", SqlState: " + var10.getSQLState() + ", ErrorCode: " + var10.getErrorCode(),
            (Throwable)var10
         );
         Exception lNext = var10.getNextException();
         if (lNext != null) {
            logger.log(Level.WARNING, "Failed to delete bankslot for bank " + this.id + ", next exception", (Throwable)lNext);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public String toString() {
      return "Bank [id: "
         + this.id
         + ", owner: "
         + this.owner
         + ", currentVillage: "
         + this.currentVillage
         + ", targetVillage: "
         + this.targetVillage
         + ", open: "
         + this.open
         + ", lastPolled: "
         + this.lastPolled
         + ']';
   }
}
