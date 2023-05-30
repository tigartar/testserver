package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemData {
   int data1;
   int data2;
   int extra1;
   int extra2;
   public final long wurmid;
   private static final Logger logger = Logger.getLogger(ItemData.class.getName());

   public ItemData(long wid, int d1, int d2, int e1, int e2) {
      this.wurmid = wid;
      this.data1 = d1;
      this.data2 = d2;
      this.extra1 = e1;
      this.extra2 = e2;
      Items.addData(this);
   }

   public void createDataEntry(Connection dbcon) {
      PreparedStatement ps = null;

      try {
         ps = dbcon.prepareStatement(ItemDbStrings.getInstance().createData());
         ps.setInt(1, this.data1);
         ps.setInt(2, this.data2);
         ps.setInt(3, this.extra1);
         ps.setInt(4, this.extra2);
         ps.setLong(5, this.wurmid);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to save item data " + this.wurmid, (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
      }
   }

   int getData1() {
      return this.data1;
   }

   void setData1(int aData1) {
      this.data1 = aData1;
   }

   int getData2() {
      return this.data2;
   }

   void setData2(int aData2) {
      this.data2 = aData2;
   }

   int getExtra1() {
      return this.extra1;
   }

   void setExtra1(int aExtra1) {
      this.extra1 = aExtra1;
   }

   int getExtra2() {
      return this.extra2;
   }

   void setExtra2(int aExtra2) {
      this.extra2 = aExtra2;
   }

   public long getWurmid() {
      return this.wurmid;
   }
}
