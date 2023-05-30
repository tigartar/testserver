package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LocalSupplyDemand {
   private final Map<Integer, Float> demandList;
   private final long traderId;
   private static final Logger logger = Logger.getLogger(LocalSupplyDemand.class.getName());
   private static final float MAX_DEMAND = -200.0F;
   private static final float INITIAL_DEMAND = -100.0F;
   private static final float MIN_DEMAND = -0.001F;
   private static final String GET_ALL_ITEM_DEMANDS = "SELECT * FROM LOCALSUPPLYDEMAND WHERE TRADERID=?";
   private static final String UPDATE_DEMAND = "UPDATE LOCALSUPPLYDEMAND SET DEMAND=? WHERE ITEMID=? AND TRADERID=?";
   private static final String INCREASE_ALL_DEMANDS = DbConnector.isUseSqlite()
      ? "UPDATE LOCALSUPPLYDEMAND SET DEMAND=MAX(-200.0,DEMAND*1.1)"
      : "UPDATE LOCALSUPPLYDEMAND SET DEMAND=GREATEST(-200.0,DEMAND*1.1)";
   private static final String CREATE_DEMAND = "INSERT INTO LOCALSUPPLYDEMAND (DEMAND,ITEMID,TRADERID) VALUES(?,?,?)";

   LocalSupplyDemand(long aTraderId) {
      this.traderId = aTraderId;
      this.demandList = new HashMap<>();
      this.loadAllItemDemands();
      this.createUnexistingDemands();
   }

   double getPrice(int itemTemplateId, double basePrice, int nums, boolean selling) {
      Float dem = this.demandList.get(itemTemplateId);
      float demand = -100.0F;
      if (dem != null) {
         demand = dem;
      }

      double price = 1.0;
      float halfSize = 100.0F;

      try {
         halfSize = ItemTemplateFactory.getInstance().getTemplate(itemTemplateId).priceHalfSize;
      } catch (NoSuchTemplateException var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
      }

      for(int x = 0; x < nums; ++x) {
         if (selling) {
            price = basePrice * Math.max(0.2F, Math.pow((double)demand / (double)halfSize, 2.0));
            demand = Math.max(-200.0F, demand - 1.0F);
         } else {
            demand = Math.min(-0.001F, demand + 1.0F);
            price = basePrice * Math.pow((double)demand / (double)halfSize, 2.0);
         }
      }

      return Math.max(0.0, price);
   }

   public void addItemSold(int itemTemplateId, float times) {
      Float dem = this.demandList.get(itemTemplateId);
      float demand = -100.0F;
      if (dem != null) {
         demand = dem;
      }

      demand -= times;
      demand = Math.max(-200.0F, demand);
      this.demandList.put(itemTemplateId, new Float(demand));
      if (dem == null) {
         this.createDemand(itemTemplateId, demand);
      } else {
         this.updateDemand(itemTemplateId, demand);
      }
   }

   public void addItemPurchased(int itemTemplateId, float times) {
      Float dem = this.demandList.get(itemTemplateId);
      float demand = -100.0F;
      if (dem != null) {
         demand = dem;
      }

      demand = Math.min(-0.001F, demand + times);
      this.demandList.put(itemTemplateId, new Float(demand));
      if (dem == null) {
         this.createDemand(itemTemplateId, demand);
      } else {
         this.updateDemand(itemTemplateId, demand);
      }
   }

   public void lowerDemands() {
      ItemDemand[] dems = this.getItemDemands();

      for(ItemDemand lDem : dems) {
         lDem.setDemand(Math.max(-200.0F, lDem.getDemand() * 1.1F));
         this.demandList.put(lDem.getTemplateId(), new Float(lDem.getDemand()));
      }
   }

   public ItemDemand[] getItemDemands() {
      ItemDemand[] dems = new ItemDemand[0];
      if (this.demandList.size() > 0) {
         dems = new ItemDemand[this.demandList.size()];
         int x = 0;

         for(Entry<Integer, Float> entry : this.demandList.entrySet()) {
            int item = entry.getKey();
            float demand = entry.getValue();
            dems[x] = new ItemDemand(item, demand);
            ++x;
         }
      }

      return dems;
   }

   private void loadAllItemDemands() {
      long start = System.currentTimeMillis();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM LOCALSUPPLYDEMAND WHERE TRADERID=?");
         ps.setLong(1, this.traderId);
         rs = ps.executeQuery();

         while(rs.next()) {
            this.demandList.put(rs.getInt("ITEMID"), new Float(Math.min(-0.001F, rs.getFloat("DEMAND"))));
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Failed to load supplyDemand for trader " + this.traderId + ": " + var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         if (logger.isLoggable(Level.FINER)) {
            long end = System.currentTimeMillis();
            logger.finer("Loading LocalSupplyDemand for Trader: " + this.traderId + " took " + (end - start) + " ms");
         }
      }
   }

   private void createUnexistingDemands() {
      ItemTemplate[] templates = ItemTemplateFactory.getInstance().getTemplates();

      for(ItemTemplate lTemplate : templates) {
         if (lTemplate.isPurchased()) {
            Float dem = this.demandList.get(lTemplate.getTemplateId());
            if (dem == null) {
               this.createDemand(lTemplate.getTemplateId(), -100.0F);
               this.demandList.put(lTemplate.getTemplateId(), -100.0F);
            }
         }
      }
   }

   private void updateDemand(int itemId, float demand) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE LOCALSUPPLYDEMAND SET DEMAND=? WHERE ITEMID=? AND TRADERID=?");
         ps.setFloat(1, Math.min(-0.001F, demand));
         ps.setInt(2, itemId);
         ps.setLong(3, this.traderId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update trader " + this.traderId + ": " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static void increaseAllDemands() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement(INCREASE_ALL_DEMANDS);
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to increase all demands due to " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private void createDemand(int itemId, float demand) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("INSERT INTO LOCALSUPPLYDEMAND (DEMAND,ITEMID,TRADERID) VALUES(?,?,?)");
         ps.setFloat(1, Math.min(-0.001F, demand));
         ps.setInt(2, itemId);
         ps.setLong(3, this.traderId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update trader " + this.traderId + ": " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
