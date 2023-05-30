package com.wurmonline.server.economy;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbShop extends Shop {
   private static final Logger logger = Logger.getLogger(DbShop.class.getName());
   private static final String updateTraderMoney = "UPDATE TRADER SET MONEY=? WHERE WURMID=?";
   private static final String setUseGlobalPrice = "UPDATE TRADER SET FOLLOWGLOBALPRICE=? WHERE WURMID=?";
   private static final String setUseLocalPrice = "UPDATE TRADER SET USELOCALPRICE=? WHERE WURMID=?";
   private static final String updateTraderPriceMod = "UPDATE TRADER SET PRICEMODIFIER=? WHERE WURMID=?";
   private static final String createTrader = "INSERT INTO TRADER(WURMID,MONEY, OWNER, PRICEMODIFIER, FOLLOWGLOBALPRICE , USELOCALPRICE , LASTPOLLED, NUMBEROFITEMS, WHENEMPTY) VALUES(?,?,?,?,?,?,?,?,?)";
   private static final String checkTrader = "SELECT WURMID FROM TRADER WHERE WURMID=?";
   private static final String deleteTrader = "DELETE FROM TRADER WHERE WURMID=?";
   private static final String setLastPolled = "UPDATE TRADER SET LASTPOLLED=? WHERE WURMID=?";
   private static final String setEarnings = "UPDATE TRADER SET EARNED=?,EARNEDLIFE=? WHERE WURMID=?";
   private static final String setSpendings = "UPDATE TRADER SET SPENT=?,SPENTLIFE=? WHERE WURMID=?";
   private static final String resetSpendings = "UPDATE TRADER SET EARNED=0,SPENTLASTMONTH=SPENT,SPENT=0 WHERE WURMID=?";
   private static final String setTaxRate = "UPDATE TRADER SET TAX=? WHERE WURMID=?";
   private static final String setOwner = "UPDATE TRADER SET OWNER=? WHERE WURMID=?";
   private static final String setTaxPaid = "UPDATE TRADER SET TAXPAID=? WHERE WURMID=?";
   private static final String setMerchantData = "UPDATE TRADER SET NUMBEROFITEMS=?, WHENEMPTY=? WHERE WURMID=?";

   DbShop(long aWurmid, long aMoney) {
      super(aWurmid, aMoney);
   }

   DbShop(long aWurmid, long aMoney, long aOwnerId) {
      super(aWurmid, aMoney, aOwnerId);
   }

   DbShop(
      long aWurmid,
      long aMoney,
      long aOwner,
      float aPriceMod,
      boolean aFollowGlobalPrice,
      boolean aUseLocalPrice,
      long aLastPolled,
      float aTax,
      long spentMonth,
      long spentLife,
      long earnedMonth,
      long earnedLife,
      long spentLast,
      long _taxPaid,
      int _numberOfItems,
      long _whenEmpty,
      boolean aLoad
   ) {
      super(
         aWurmid,
         aMoney,
         aOwner,
         aPriceMod,
         aFollowGlobalPrice,
         aUseLocalPrice,
         aLastPolled,
         aTax,
         spentMonth,
         spentLife,
         earnedMonth,
         earnedLife,
         spentLast,
         _taxPaid,
         _numberOfItems,
         _whenEmpty,
         aLoad
      );
   }

   @Override
   void create() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.lastPolled = System.currentTimeMillis();
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO TRADER(WURMID,MONEY, OWNER, PRICEMODIFIER, FOLLOWGLOBALPRICE , USELOCALPRICE , LASTPOLLED, NUMBEROFITEMS, WHENEMPTY) VALUES(?,?,?,?,?,?,?,?,?)"
         );
         ps.setLong(1, this.wurmid);
         ps.setLong(2, this.money);
         ps.setLong(3, this.ownerId);
         ps.setFloat(4, this.priceModifier);
         ps.setBoolean(5, this.followGlobalPrice);
         ps.setBoolean(6, this.useLocalPrice);
         ps.setLong(7, this.lastPolled);
         ps.setInt(8, this.numberOfItems);
         ps.setLong(9, this.whenEmpty);
         ps.executeUpdate();
         if (this.ownerId == -10L) {
            ++numTraders;
         }
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to create traderMoney for " + this.wurmid + ": " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   boolean traderMoneyExists() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("SELECT WURMID FROM TRADER WHERE WURMID=?");
         ps.setLong(1, this.wurmid);
         rs = ps.executeQuery();
         return rs.next();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to check trader with id " + this.wurmid + ": " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      return false;
   }

   @Override
   public void setMoney(long mon) {
      if (this.money != mon) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.money = mon;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET MONEY=? WHERE WURMID=?");
            ps.setLong(1, this.money);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to update traderMoney for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setFollowGlobalPrice(boolean global) {
      if (this.followGlobalPrice != global) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.followGlobalPrice = global;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET FOLLOWGLOBALPRICE=? WHERE WURMID=?");
            ps.setBoolean(1, this.followGlobalPrice);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update followGlobalPrice for " + this.wurmid + ": " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setUseLocalPrice(boolean local) {
      if (this.useLocalPrice != local) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.useLocalPrice = local;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET USELOCALPRICE=? WHERE WURMID=?");
            ps.setBoolean(1, this.useLocalPrice);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update useLocalPrice for " + this.wurmid + ": " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void addMoneySpent(long moneyS) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.moneySpent += moneyS;
         this.moneySpentLife += moneyS;
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE TRADER SET SPENT=?,SPENTLIFE=? WHERE WURMID=?");
         ps.setLong(1, this.moneySpent);
         ps.setLong(2, this.moneySpentLife);
         ps.setLong(3, this.wurmid);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update lastPolled for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void resetEarnings() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.moneySpentLastMonth = this.moneySpent;
         this.moneySpent = 0L;
         this.moneyEarned = 0L;
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE TRADER SET EARNED=0,SPENTLASTMONTH=SPENT,SPENT=0 WHERE WURMID=?");
         ps.setLong(1, this.wurmid);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to update lastPolled for " + this.wurmid + ": " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void addMoneyEarned(long moneyE) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         this.moneyEarned += moneyE;
         this.moneyEarnedLife += moneyE;
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE TRADER SET EARNED=?,EARNEDLIFE=? WHERE WURMID=?");
         ps.setLong(1, this.moneyEarned);
         ps.setLong(2, this.moneyEarnedLife);
         ps.setLong(3, this.wurmid);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update lastPolled for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void setLastPolled(long lastPoll) {
      if (this.lastPolled != lastPoll) {
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            this.lastPolled = lastPoll;
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET LASTPOLLED=? WHERE WURMID=?");
            ps.setLong(1, this.lastPolled);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to update lastPolled for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void delete() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("DELETE FROM TRADER WHERE WURMID=?");
         ps.setLong(1, this.wurmid);
         ps.executeUpdate();
         if (this.ownerId == -10L) {
            --numTraders;
         }
      } catch (SQLException var7) {
         logger.log(Level.WARNING, "Failed to delete trader" + this.wurmid + ": " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }

      Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + Math.max(0L, this.money));
   }

   @Override
   public void setPriceModifier(float priceMod) {
      if (this.priceModifier != priceMod) {
         this.priceModifier = priceMod;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET PRICEMODIFIER=? WHERE WURMID=?");
            ps.setFloat(1, this.priceModifier);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update trader pricemodifier for " + this.wurmid + ": " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setTax(float newtax) {
      if (this.tax != newtax) {
         this.tax = newtax;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET TAX=? WHERE WURMID=?");
            ps.setFloat(1, this.tax);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var8) {
            logger.log(Level.WARNING, "Failed to update trader tax for " + this.wurmid + ": " + var8.getMessage(), (Throwable)var8);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void addTax(long addedtax) {
      this.taxPaid += addedtax;
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("UPDATE TRADER SET TAXPAID=? WHERE WURMID=?");
         ps.setLong(1, this.taxPaid);
         ps.setLong(2, this.wurmid);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update trader tax for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   @Override
   public void setOwner(long newOwnerId) {
      if (newOwnerId != this.ownerId) {
         this.ownerId = newOwnerId;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET OWNER=? WHERE WURMID=?");
            ps.setLong(1, this.ownerId);
            ps.setLong(2, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var9) {
            logger.log(Level.WARNING, "Failed to update trader owner " + this.ownerId + " for " + this.wurmid + ": " + var9.getMessage(), (Throwable)var9);
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }

   @Override
   public void setMerchantData(int _numberOfItems, long _whenEmpty) {
      if (_numberOfItems != this.numberOfItems || _whenEmpty != this.whenEmpty) {
         this.numberOfItems = _numberOfItems;
         this.whenEmpty = _whenEmpty;
         Connection dbcon = null;
         PreparedStatement ps = null;

         try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement("UPDATE TRADER SET NUMBEROFITEMS=?, WHENEMPTY=? WHERE WURMID=?");
            ps.setInt(1, this.numberOfItems);
            ps.setLong(2, this.whenEmpty);
            ps.setLong(3, this.wurmid);
            ps.executeUpdate();
         } catch (SQLException var10) {
            logger.log(
               Level.WARNING,
               "Failed to update merchant data " + this.numberOfItems + "," + this.whenEmpty + " for " + this.wurmid + ": " + var10.getMessage(),
               (Throwable)var10
            );
         } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
         }
      }
   }
}
