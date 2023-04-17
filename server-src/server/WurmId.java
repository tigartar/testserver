/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
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

public final class WurmId
implements Serializable,
CounterTypes {
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
        return (int)(id & 0xFFL);
    }

    public static final int getOrigin(long id) {
        return (int)(id >> 8) & 0xFFFF;
    }

    public static final long getNumber(long id) {
        return id >> 24;
    }

    public static final long getId(long id) {
        return id;
    }

    public static final long getNextItemId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++itemIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 2L;
    }

    public static final long getNextPlayerId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++playerIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 0L;
    }

    public static final long getNextBodyPartId(long creatureId, byte bodyplace, boolean isPlayer) {
        return BigInteger.valueOf(BigInteger.valueOf(creatureId >> 8).shiftLeft(1).longValue() + (long)(isPlayer ? 1 : 0)).shiftLeft(16).longValue() + (long)(bodyplace << 8) + 19L;
    }

    public static final long getCreatureIdForBodyPart(long bodypartId) {
        boolean isPlayer = (BigInteger.valueOf(bodypartId).shiftRight(16).longValue() & 1L) == 1L;
        return (bodypartId >> 17) + (long)(isPlayer ? 0 : 1);
    }

    public static final int getBodyPlaceForBodyPart(long bodypartId) {
        return (int)(bodypartId >> 8 & 0xFFL);
    }

    public static final long getNextCreatureId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++creatureIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 1L;
    }

    public static final long getNextStructureId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++structureIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 4L;
    }

    public static final long getNextTempItemId() {
        return BigInteger.valueOf(++tempIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 6L;
    }

    public static final long getNextIllusionId() {
        return BigInteger.valueOf(++illusionIdCounter).shiftLeft(24).longValue() + 24L;
    }

    public static final long getNextTemporaryWoundId() {
        return BigInteger.valueOf(++temporaryWoundIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 32L;
    }

    public static final long getNextWoundId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++woundIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 8L;
    }

    public static final long getNextTemporarySkillId() {
        return BigInteger.valueOf(++temporarySkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 31L;
    }

    public static final long getNextPlayerSkillId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++playerSkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 10L;
    }

    public static final long getNextCreatureSkillId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++creatureSkillsIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 9L;
    }

    public static final long getNextBankId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++bankIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 13L;
    }

    public static final long getNextSpellId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++spellIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 15L;
    }

    public static final long getNextWCCommandId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++wccommandCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 21L;
    }

    public static final long getNextPlanId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++planIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 16L;
    }

    public static final long getNextBodyId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++bodyIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 19L;
    }

    public static final long getNextCoinId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++coinIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 20L;
    }

    public static final long getNextPoiId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++poiIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 26L;
    }

    public static final long getNextCouponId() {
        ++savecounter;
        WurmId.checkSave();
        return BigInteger.valueOf(++couponIdCounter).shiftLeft(24).longValue() + (long)(Servers.localServer.id << 8) + 29L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void loadIdNumbers() {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        long start;
        block6: {
            start = System.nanoTime();
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(getIds);
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
                    WurmId.loadIdNumbers(true);
                    break block6;
                }
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
                WurmId.updateNumbers();
                logger.log(Level.INFO, "Added to ids, creatrureIdcounter is now " + creatureIdCounter);
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to load max playerid: " + sqx.getMessage(), sqx);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        logger.info("Finished loading Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
    }

    public static final void checkSave() {
        if (savecounter >= 1000) {
            WurmId.updateNumbers();
            savecounter = 0;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadIdNumbers(boolean create) {
        logger.log(Level.WARNING, "LOADING WURMIDS 'MANUALLY'. This should only happen at convert or on a new server.");
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getMaxPlayerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                playerIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load max playerid: " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getMaxWoundId);
            rs = ps.executeQuery();
            if (rs.next()) {
                woundIdCounter = rs.getLong("MAX(ID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max woundid: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getMaxSkillId);
            rs = ps.executeQuery();
            if (rs.next()) {
                playerSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max player skill id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(getMaxSkillId);
            rs = ps.executeQuery();
            if (rs.next()) {
                creatureSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max creature skill id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getTemplateDbCon();
            ps = dbcon.prepareStatement(getMaxSkillId);
            rs = ps.executeQuery();
            if (rs.next()) {
                templateSkillsIdCounter = rs.getLong("MAX(ID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max templateid: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(getMaxCreatureId);
            rs = ps.executeQuery();
            if (rs.next()) {
                creatureIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            logger.log(Level.WARNING, "Max creatureid: " + creatureIdCounter + " when loading manually");
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max creatureid: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(getMaxStructureId);
            rs = ps.executeQuery();
            if (rs.next()) {
                structureIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max structureid: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(getMaxItemId);
            rs = ps.executeQuery();
            if (rs.next()) {
                itemIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max itemid: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(getMaxBodyId);
            rs = ps.executeQuery();
            if (rs.next()) {
                bodyIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max body id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(getMaxCoinId);
            rs = ps.executeQuery();
            if (rs.next()) {
                coinIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max coin id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getMaxPoiId);
            rs = ps.executeQuery();
            if (rs.next()) {
                poiIdCounter = rs.getLong("MAX(ID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max poi id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(getMaxBankId);
            rs = ps.executeQuery();
            if (rs.next()) {
                bankIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max bank id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(getMaxSpellId);
            rs = ps.executeQuery();
            if (rs.next()) {
                spellIdCounter = rs.getLong("MAX(WURMID)") >> 24;
            }
            rs.close();
            ps.close();
        }
        catch (SQLException sqx) {
            logger.log(Level.WARNING, "Failed to load max spell id: " + sqx.getMessage(), sqx);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        if (create) {
            WurmId.saveNumbers();
        }
        logger.info("Loaded id numbers from database, playerids:" + playerIdCounter + ", creatureids:" + creatureIdCounter + ", itemids:" + itemIdCounter + ", structureIds:" + structureIdCounter + ", woundids:" + woundIdCounter + ", playerSkillIds: " + playerSkillsIdCounter + ", creatureSkillIds: " + creatureSkillsIdCounter + ", templateSkillIds: " + templateSkillsIdCounter + ", bankIds: " + bankIdCounter + ", spellIds: " + spellIdCounter + ", planIds: " + planIdCounter + ", bodyIds: " + bodyIdCounter + ", coinIds: " + coinIdCounter + ", wccommandCounter: " + wccommandCounter + ", poiIdCounter: " + poiIdCounter);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void updateNumbers() {
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(updateIds);
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
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update idnums into logindb! " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Finished updating Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
                    logger.fine("Saved id numbers to database, playerids:" + playerIdCounter + ", creatureids:" + creatureIdCounter + ", itemids:" + itemIdCounter + ", structureIds:" + structureIdCounter + ", woundids:" + woundIdCounter + ", playerSkillIds: " + playerSkillsIdCounter + ", creatureSkillIds: " + creatureSkillsIdCounter + ", bankIds: " + bankIdCounter + ", spellIds: " + spellIdCounter + ", planIds: " + planIdCounter + ", bodyIds: " + bodyIdCounter + ", coinIds: " + coinIdCounter + ", wccommandCounter: " + wccommandCounter + ", poiIdCounter: " + poiIdCounter);
                }
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Finished updating Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
                logger.fine("Saved id numbers to database, playerids:" + playerIdCounter + ", creatureids:" + creatureIdCounter + ", itemids:" + itemIdCounter + ", structureIds:" + structureIdCounter + ", woundids:" + woundIdCounter + ", playerSkillIds: " + playerSkillsIdCounter + ", creatureSkillIds: " + creatureSkillsIdCounter + ", bankIds: " + bankIdCounter + ", spellIds: " + spellIdCounter + ", planIds: " + planIdCounter + ", bodyIds: " + bodyIdCounter + ", coinIds: " + coinIdCounter + ", wccommandCounter: " + wccommandCounter + ", poiIdCounter: " + poiIdCounter);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished updating Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
            logger.fine("Saved id numbers to database, playerids:" + playerIdCounter + ", creatureids:" + creatureIdCounter + ", itemids:" + itemIdCounter + ", structureIds:" + structureIdCounter + ", woundids:" + woundIdCounter + ", playerSkillIds: " + playerSkillsIdCounter + ", creatureSkillIds: " + creatureSkillsIdCounter + ", bankIds: " + bankIdCounter + ", spellIds: " + spellIdCounter + ", planIds: " + planIdCounter + ", bodyIds: " + bodyIdCounter + ", coinIds: " + coinIdCounter + ", wccommandCounter: " + wccommandCounter + ", poiIdCounter: " + poiIdCounter);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void saveNumbers() {
        long start = System.nanoTime();
        PreparedStatement ps = null;
        Connection dbcon = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(createIds);
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
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to insert idnums into logindb! Trying update instead." + sqex.getMessage(), sqex);
                WurmId.updateNumbers();
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                logger.info("Finished saving Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            logger.info("Finished saving Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        logger.info("Finished saving Wurm IDs, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " millis.");
    }

    static {
        WurmId.loadIdNumbers();
    }
}

