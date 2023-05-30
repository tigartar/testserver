package com.wurmonline.server;

import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WurmId implements Serializable, CounterTypes {
   private static final long serialVersionUID = -1805883548433788244L;
   private static long playerIdCounter = 0L;
   private static long creatureIdCounter = 0L;
   private static long itemIdCounter = 0L;
   private static long structureIdCounter = 0L;
   private static long tempIdCounter = 0L;
   private static long illusionIdCounter = 0L;
   private static long woundIdCounter = 0L;
   private static long temporaryWoundIdCounter = 0L;
   private static long spellIdCounter = 0L;
   private static long creatureSkillsIdCounter = 0L;
   private static long templateSkillsIdCounter = 0L;
   private static long playerSkillsIdCounter = 0L;
   private static long temporarySkillsIdCounter = 0L;
   private static long planIdCounter = 0L;
   private static long bankIdCounter = 0L;
   private static long bodyIdCounter = 0L;
   private static long coinIdCounter = 0L;
   private static long poiIdCounter = 0L;
   private static long couponIdCounter = 0L;
   private static long wccommandCounter = 0L;
   private static int savecounter = 0;
   private static final Logger logger = Logger.getLogger(WurmId.class.getName());
   private static final String getMaxPlayerId = "SELECT MAX(WURMID) FROM PLAYERS";
   private static final String getMaxCreatureId = "SELECT MAX(WURMID) FROM CREATURES";
   private static final String getMaxItemId = "SELECT MAX(WURMID) FROM ITEMS";
   private static final String getMaxStructureId = "SELECT MAX(WURMID) FROM STRUCTURES";
   private static final String getMaxWoundId = "SELECT MAX(ID) FROM WOUNDS";
   private static final String getMaxSkillId = "SELECT MAX(ID) FROM SKILLS";
   private static final String getMaxBankId = "SELECT MAX(WURMID) FROM BANKS";
   private static final String getMaxSpellId = "SELECT MAX(WURMID) FROM SPELLEFFECTS";
   private static final String getMaxBodyId = "SELECT MAX(WURMID) FROM BODYPARTS";
   private static final String getMaxCoinId = "SELECT MAX(WURMID) FROM COINS";
   private static final String getMaxPoiId = "SELECT MAX(ID) FROM MAP_ANNOTATIONS";
   private static final String getMaxCouponId = "SELECT MAX(CODEID) FROM REDEEMCODE";
   private static final String getIds = "SELECT * FROM IDS WHERE SERVER=?";
   private static final String createIds = "INSERT INTO IDS (SERVER,PLAYERIDS,CREATUREIDS,ITEMIDS,STRUCTUREIDS,WOUNDIDS,PLAYERSKILLIDS,CREATURESKILLIDS,BANKIDS,SPELLIDS,PLANIDS,BODYIDS,COINIDS,WCCOMMANDS, POIIDS, REDEEMIDS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
   private static final String updateIds = "UPDATE IDS SET PLAYERIDS=?,CREATUREIDS=?,ITEMIDS=?,STRUCTUREIDS=?,WOUNDIDS=?,PLAYERSKILLIDS=?,CREATURESKILLIDS=?,BANKIDS=?,SPELLIDS=?,PLANIDS=?,BODYIDS=?,COINIDS=?,WCCOMMANDS=?, POIIDS=?, REDEEMIDS=? WHERE SERVER=?";

   private WurmId() {
   }

   public static final int getType(long id) {
      return (int)(id & 255L);
   }

   public static final int getOrigin(long id) {
      return (int)(id >> 8) & 65535;
   }

   public static final long getNumber(long id) {
      return id >> 24;
   }

   public static final long getId(long id) {
      return id;
   }

   public static final long getNextItemId() {
      ++itemIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(itemIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 2L;
   }

   public static final long getNextPlayerId() {
      ++playerIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(playerIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 0L;
   }

   public static final long getNextBodyPartId(long creatureId, byte bodyplace, boolean isPlayer) {
      return BigInteger.valueOf(BigInteger.valueOf(creatureId >> 8).shiftLeft(1).longValue() + (long)(isPlayer ? 1 : 0)).shiftLeft(16).longValue()
         + (long)(bodyplace << 8)
         + 19L;
   }

   public static final long getCreatureIdForBodyPart(long bodypartId) {
      boolean isPlayer = (BigInteger.valueOf(bodypartId).shiftRight(16).longValue() & 1L) == 1L;
      return (bodypartId >> 17) + (long)(isPlayer ? 0 : 1);
   }

   public static final int getBodyPlaceForBodyPart(long bodypartId) {
      return (int)(bodypartId >> 8 & 255L);
   }

   public static final long getNextCreatureId() {
      ++creatureIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(creatureIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 1L;
   }

   public static final long getNextStructureId() {
      ++structureIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(structureIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 4L;
   }

   public static final long getNextTempItemId() {
      ++tempIdCounter;
      return BigInteger.valueOf(tempIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 6L;
   }

   public static final long getNextIllusionId() {
      ++illusionIdCounter;
      return BigInteger.valueOf(illusionIdCounter).shiftLeft(24).longValue() + 24L;
   }

   public static final long getNextTemporaryWoundId() {
      ++temporaryWoundIdCounter;
      return BigInteger.valueOf(temporaryWoundIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 32L;
   }

   public static final long getNextWoundId() {
      ++woundIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(woundIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 8L;
   }

   public static final long getNextTemporarySkillId() {
      ++temporarySkillsIdCounter;
      return BigInteger.valueOf(temporarySkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 31L;
   }

   public static final long getNextPlayerSkillId() {
      ++playerSkillsIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(playerSkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 10L;
   }

   public static final long getNextCreatureSkillId() {
      ++creatureSkillsIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(creatureSkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 9L;
   }

   public static final long getNextBankId() {
      ++bankIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(bankIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 13L;
   }

   public static final long getNextSpellId() {
      ++spellIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(spellIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 15L;
   }

   public static final long getNextWCCommandId() {
      ++wccommandCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(wccommandCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 21L;
   }

   public static final long getNextPlanId() {
      ++planIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(planIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 16L;
   }

   public static final long getNextBodyId() {
      ++bodyIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(bodyIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 19L;
   }

   public static final long getNextCoinId() {
      ++coinIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(coinIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 20L;
   }

   public static final long getNextPoiId() {
      ++poiIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(poiIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 26L;
   }

   public static final long getNextCouponId() {
      ++couponIdCounter;
      ++savecounter;
      checkSave();
      return BigInteger.valueOf(couponIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 29L;
   }

   private static final void loadIdNumbers() {
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM IDS WHERE SERVER=?");
         ps.setInt(1, Servers.localServer.id);
         rs = ps.executeQuery();
         if (rs.next()) {
            logger.log(Level.INFO, "Loading ids.");
            playerIdCounter = rs.getLong("PLAYERIDS");
            woundIdCounter = rs.getLong("WOUNDIDS");
            playerSkillsIdCounter = rs.getLong("PLAYERSKILLIDS");
            creatureSkillsIdCounter = rs.getLong("CREATURESKILLIDS");
            creatureIdCounter = rs.getLong("CREATUREIDS");
            structureIdCounter = rs.getLong("STRUCTUREIDS");
            itemIdCounter = rs.getLong("ITEMIDS");
            bankIdCounter = rs.getLong("BANKIDS");
            spellIdCounter = rs.getLong("SPELLIDS");
            wccommandCounter = rs.getLong("WCCOMMANDS");
            planIdCounter = rs.getLong("PLANIDS");
            bodyIdCounter = rs.getLong("BODYIDS");
            coinIdCounter = rs.getLong("COINIDS");
            poiIdCounter = rs.getLong("POIIDS");
            couponIdCounter = rs.getLong("REDEEMIDS");
         }

         rs.close();
         ps.close();
         if (itemIdCounter == 0L) {
            loadIdNumbers(true);
         } else {
            itemIdCounter += 3000L;
            playerIdCounter += 3000L;
            woundIdCounter += 3000L;
            playerSkillsIdCounter += 3000L;
            creatureSkillsIdCounter += 3000L;
            creatureIdCounter += 3000L;
            structureIdCounter += 3000L;
            itemIdCounter += 3000L;
            bankIdCounter += 3000L;
            spellIdCounter += 3000L;
            wccommandCounter += 3000L;
            planIdCounter += 3000L;
            bodyIdCounter += 3000L;
            coinIdCounter += 3000L;
            poiIdCounter += 1000L;
            couponIdCounter += 100L;
            updateNumbers();
            logger.log(Level.INFO, "Added to ids, creatrureIdcounter is now " + creatureIdCounter);
         }
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to load max playerid: " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      logger.info("Finished loading Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0F + " millis.");
   }

   public static final void checkSave() {
      if (savecounter >= 1000) {
         updateNumbers();
         savecounter = 0;
      }
   }

   public static final void loadIdNumbers(boolean create) {
      logger.log(Level.WARNING, "LOADING WURMIDS 'MANUALLY'. This should only happen at convert or on a new server.");
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM PLAYERS");
         rs = ps.executeQuery();
         if (rs.next()) {
            playerIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var380) {
         logger.log(Level.WARNING, "Failed to load max playerid: " + var380.getMessage(), (Throwable)var380);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(ID) FROM WOUNDS");
         rs = ps.executeQuery();
         if (rs.next()) {
            woundIdCounter = rs.getLong("MAX(ID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var378) {
         logger.log(Level.WARNING, "Failed to load max woundid: " + var378.getMessage(), (Throwable)var378);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(ID) FROM SKILLS");
         rs = ps.executeQuery();
         if (rs.next()) {
            playerSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var376) {
         logger.log(Level.WARNING, "Failed to load max player skill id: " + var376.getMessage(), (Throwable)var376);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(ID) FROM SKILLS");
         rs = ps.executeQuery();
         if (rs.next()) {
            creatureSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var374) {
         logger.log(Level.WARNING, "Failed to load max creature skill id: " + var374.getMessage(), (Throwable)var374);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getTemplateDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(ID) FROM SKILLS");
         rs = ps.executeQuery();
         if (rs.next()) {
            templateSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var372) {
         logger.log(Level.WARNING, "Failed to load max templateid: " + var372.getMessage(), (Throwable)var372);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getCreatureDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM CREATURES");
         rs = ps.executeQuery();
         if (rs.next()) {
            creatureIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         logger.log(Level.WARNING, "Max creatureid: " + creatureIdCounter + " when loading manually");
         rs.close();
         ps.close();
      } catch (SQLException var370) {
         logger.log(Level.WARNING, "Failed to load max creatureid: " + var370.getMessage(), (Throwable)var370);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getZonesDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM STRUCTURES");
         rs = ps.executeQuery();
         if (rs.next()) {
            structureIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var368) {
         logger.log(Level.WARNING, "Failed to load max structureid: " + var368.getMessage(), (Throwable)var368);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM ITEMS");
         rs = ps.executeQuery();
         if (rs.next()) {
            itemIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var366) {
         logger.log(Level.WARNING, "Failed to load max itemid: " + var366.getMessage(), (Throwable)var366);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM BODYPARTS");
         rs = ps.executeQuery();
         if (rs.next()) {
            bodyIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var364) {
         logger.log(Level.WARNING, "Failed to load max body id: " + var364.getMessage(), (Throwable)var364);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM COINS");
         rs = ps.executeQuery();
         if (rs.next()) {
            coinIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var362) {
         logger.log(Level.WARNING, "Failed to load max coin id: " + var362.getMessage(), (Throwable)var362);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(ID) FROM MAP_ANNOTATIONS");
         rs = ps.executeQuery();
         if (rs.next()) {
            poiIdCounter = rs.getLong("MAX(ID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var360) {
         logger.log(Level.WARNING, "Failed to load max poi id: " + var360.getMessage(), (Throwable)var360);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getEconomyDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM BANKS");
         rs = ps.executeQuery();
         if (rs.next()) {
            bankIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var358) {
         logger.log(Level.WARNING, "Failed to load max bank id: " + var358.getMessage(), (Throwable)var358);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT MAX(WURMID) FROM SPELLEFFECTS");
         rs = ps.executeQuery();
         if (rs.next()) {
            spellIdCounter = rs.getLong("MAX(WURMID)") >> 24;
         }

         rs.close();
         ps.close();
      } catch (SQLException var356) {
         logger.log(Level.WARNING, "Failed to load max spell id: " + var356.getMessage(), (Throwable)var356);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      if (create) {
         saveNumbers();
      }

      logger.info(
         "Loaded id numbers from database, playerids:"
            + playerIdCounter
            + ", creatureids:"
            + creatureIdCounter
            + ", itemids:"
            + itemIdCounter
            + ", structureIds:"
            + structureIdCounter
            + ", woundids:"
            + woundIdCounter
            + ", playerSkillIds: "
            + playerSkillsIdCounter
            + ", creatureSkillIds: "
            + creatureSkillsIdCounter
            + ", templateSkillIds: "
            + templateSkillsIdCounter
            + ", bankIds: "
            + bankIdCounter
            + ", spellIds: "
            + spellIdCounter
            + ", planIds: "
            + planIdCounter
            + ", bodyIds: "
            + bodyIdCounter
            + ", coinIds: "
            + coinIdCounter
            + ", wccommandCounter: "
            + wccommandCounter
            + ", poiIdCounter: "
            + poiIdCounter
      );
   }

   public static final void updateNumbers() {
      long start = System.nanoTime();
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "UPDATE IDS SET PLAYERIDS=?,CREATUREIDS=?,ITEMIDS=?,STRUCTUREIDS=?,WOUNDIDS=?,PLAYERSKILLIDS=?,CREATURESKILLIDS=?,BANKIDS=?,SPELLIDS=?,PLANIDS=?,BODYIDS=?,COINIDS=?,WCCOMMANDS=?, POIIDS=?, REDEEMIDS=? WHERE SERVER=?"
         );
         ps.setLong(1, playerIdCounter);
         ps.setLong(2, creatureIdCounter);
         ps.setLong(3, itemIdCounter);
         ps.setLong(4, structureIdCounter);
         ps.setLong(5, woundIdCounter);
         ps.setLong(6, playerSkillsIdCounter);
         ps.setLong(7, creatureSkillsIdCounter);
         ps.setLong(8, bankIdCounter);
         ps.setLong(9, spellIdCounter);
         ps.setLong(10, planIdCounter);
         ps.setLong(11, bodyIdCounter);
         ps.setLong(12, coinIdCounter);
         ps.setLong(13, wccommandCounter);
         ps.setLong(14, poiIdCounter);
         ps.setLong(15, couponIdCounter);
         ps.setInt(16, Servers.localServer.id);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to update idnums into logindb! " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished updating Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0F + " millis.");
            logger.fine(
               "Saved id numbers to database, playerids:"
                  + playerIdCounter
                  + ", creatureids:"
                  + creatureIdCounter
                  + ", itemids:"
                  + itemIdCounter
                  + ", structureIds:"
                  + structureIdCounter
                  + ", woundids:"
                  + woundIdCounter
                  + ", playerSkillIds: "
                  + playerSkillsIdCounter
                  + ", creatureSkillIds: "
                  + creatureSkillsIdCounter
                  + ", bankIds: "
                  + bankIdCounter
                  + ", spellIds: "
                  + spellIdCounter
                  + ", planIds: "
                  + planIdCounter
                  + ", bodyIds: "
                  + bodyIdCounter
                  + ", coinIds: "
                  + coinIdCounter
                  + ", wccommandCounter: "
                  + wccommandCounter
                  + ", poiIdCounter: "
                  + poiIdCounter
            );
         }
      }
   }

   private static final void saveNumbers() {
      long start = System.nanoTime();
      PreparedStatement ps = null;
      Connection dbcon = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO IDS (SERVER,PLAYERIDS,CREATUREIDS,ITEMIDS,STRUCTUREIDS,WOUNDIDS,PLAYERSKILLIDS,CREATURESKILLIDS,BANKIDS,SPELLIDS,PLANIDS,BODYIDS,COINIDS,WCCOMMANDS, POIIDS, REDEEMIDS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
         );
         ps.setInt(1, Servers.localServer.id);
         ps.setLong(2, playerIdCounter);
         ps.setLong(3, creatureIdCounter);
         ps.setLong(4, itemIdCounter);
         ps.setLong(5, structureIdCounter);
         ps.setLong(6, woundIdCounter);
         ps.setLong(7, playerSkillsIdCounter);
         ps.setLong(8, creatureSkillsIdCounter);
         ps.setLong(9, bankIdCounter);
         ps.setLong(10, spellIdCounter);
         ps.setLong(11, planIdCounter);
         ps.setLong(12, bodyIdCounter);
         ps.setLong(13, coinIdCounter);
         ps.setLong(14, wccommandCounter);
         ps.setLong(15, poiIdCounter);
         ps.setLong(16, couponIdCounter);
         ps.executeUpdate();
         ps.close();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to insert idnums into logindb! Trying update instead." + var8.getMessage(), (Throwable)var8);
         updateNumbers();
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
         logger.info("Finished saving Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0F + " millis.");
      }
   }

   static {
      loadIdNumbers();
   }
}
