/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.RecruitmentAd;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RecruitmentAds {
    private static final Logger logger = Logger.getLogger(RecruitmentAds.class.getName());
    private static final Map<Integer, Map<Integer, RecruitmentAd>> recruitmentAds = new ConcurrentHashMap<Integer, Map<Integer, RecruitmentAd>>();
    private static final String loadAllAds = "SELECT * FROM VILLAGERECRUITMENT";
    private static final String deleteAd = "DELETE FROM VILLAGERECRUITMENT WHERE VILLAGE=?";
    private static final String createNewAdd = "INSERT INTO VILLAGERECRUITMENT (VILLAGE, DESCRIPTION, CONTACT, CREATED, KINGDOM) VALUES ( ?, ?, ?, ?, ?);";
    private static final String updateAd = "UPDATE VILLAGERECRUITMENT SET DESCRIPTION =?, CONTACT =?, CREATED =? WHERE VILLAGE=?;";

    public static void add(RecruitmentAd ad) {
        Map<Integer, RecruitmentAd> ads = recruitmentAds.get(ad.getKingdom());
        if (ads == null) {
            ads = new ConcurrentHashMap<Integer, RecruitmentAd>();
            recruitmentAds.put(ad.getKingdom(), ads);
        }
        ads.put(ad.getVillageId(), ad);
    }

    public static final boolean containsAdForVillage(int villageId) {
        Map<Integer, RecruitmentAd> ads;
        Integer key = villageId;
        boolean exists = false;
        Iterator<Map<Integer, RecruitmentAd>> iterator = recruitmentAds.values().iterator();
        while (iterator.hasNext() && !(exists = (ads = iterator.next()).containsKey(key))) {
        }
        return exists;
    }

    public static final RecruitmentAd create(int villageId, String description, long contactId, int kingdom) throws IOException {
        RecruitmentAd recruitmentAd;
        if (RecruitmentAds.containsAdForVillage(villageId)) {
            return null;
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            Date created = new Date(System.currentTimeMillis());
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(createNewAdd);
            ps.setInt(1, villageId);
            ps.setString(2, description);
            ps.setLong(3, contactId);
            ps.setDate(4, created);
            ps.setInt(5, kingdom);
            ps.executeUpdate();
            RecruitmentAd ad = new RecruitmentAd(villageId, contactId, description, created, kingdom);
            RecruitmentAds.add(ad);
            recruitmentAd = ad;
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create new recruitment ad for village: " + villageId + ": " + sqx.getMessage(), sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        return recruitmentAd;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void deleteAd(RecruitmentAd ad) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(deleteAd);
            ps.setInt(1, ad.getVillageId());
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete recruitment ad due to " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public static final void deleteVillageAd(Player player) {
        Village village = Villages.getVillageForCreature(player);
        if (village == null) {
            player.getCommunicator().sendNormalServerMessage("You are not part of a village and can not delete any recruitment ads.");
            return;
        }
        RecruitmentAd ad = RecruitmentAds.getVillageAd(village.getId(), (int)player.getKingdomId());
        RecruitmentAds.remove(ad);
    }

    public static final void deleteVillageAd(Village village) {
        RecruitmentAd ad = RecruitmentAds.getVillageAd(village, (int)village.kingdom);
        if (ad != null) {
            RecruitmentAds.remove(ad);
        }
    }

    public static final RecruitmentAd[] getAllRecruitmentAds() {
        HashSet<RecruitmentAd> adSet = new HashSet<RecruitmentAd>();
        for (Map<Integer, RecruitmentAd> ads : recruitmentAds.values()) {
            adSet.addAll(ads.values());
        }
        RecruitmentAd[] adsArray = new RecruitmentAd[adSet.size()];
        adSet.toArray(adsArray);
        return adsArray;
    }

    public static final RecruitmentAd[] getKingdomAds(int kingdom) {
        Map<Integer, RecruitmentAd> ads = recruitmentAds.get(kingdom);
        if (ads == null) {
            return null;
        }
        RecruitmentAd[] rad = new RecruitmentAd[ads.size()];
        return ads.values().toArray(rad);
    }

    public static final RecruitmentAd getVillageAd(int villageId, int kingdom) {
        Map<Integer, RecruitmentAd> ads = recruitmentAds.get(kingdom);
        if (ads == null) {
            return null;
        }
        return ads.get(villageId);
    }

    public static final RecruitmentAd getVillageAd(Village village, int kingdom) {
        return RecruitmentAds.getVillageAd(village.getId(), kingdom);
    }

    public static void loadRecruitmentAds() throws IOException {
        long start = System.nanoTime();
        int loadedAds = 0;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(loadAllAds);
            rs = ps.executeQuery();
            while (rs.next()) {
                RecruitmentAd ad = new RecruitmentAd(rs.getInt("VILLAGE"), rs.getLong("CONTACT"), rs.getString("DESCRIPTION"), rs.getDate("CREATED"), rs.getInt("KINGDOM"));
                RecruitmentAds.add(ad);
                ++loadedAds;
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load recruitment ads due to " + sqex.getMessage(), sqex);
                throw new IOException("Failed to load recruitment ads", sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + loadedAds + " ads from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + loadedAds + " ads from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    public static void poll() {
        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        long divider = 86400000L;
        for (Map<Integer, RecruitmentAd> kList : recruitmentAds.values()) {
            for (RecruitmentAd ad : kList.values()) {
                long daysAfterCreation = (nowDate.getTime() - ad.getCreated().getTime()) / 86400000L;
                if (daysAfterCreation <= 60L) continue;
                RecruitmentAds.remove(ad);
            }
        }
    }

    public static void remove(RecruitmentAd ad) {
        Integer kingKey = ad.getKingdom();
        Integer villKey = ad.getVillageId();
        if (!recruitmentAds.containsKey(kingKey)) {
            return;
        }
        Map<Integer, RecruitmentAd> kList = recruitmentAds.get(kingKey);
        if (kList.containsKey(villKey)) {
            kList.remove(villKey);
            RecruitmentAds.deleteAd(ad);
        }
    }

    public static final void update(int villageId, String description, long contact, Date updated, byte kingdomId) throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(updateAd);
            ps.setString(1, description);
            ps.setLong(2, contact);
            ps.setDate(3, updated);
            ps.setInt(4, villageId);
            ps.executeUpdate();
            RecruitmentAd ad = RecruitmentAds.getVillageAd(villageId, (int)kingdomId);
            ad.setDescription(description);
            ad.setCreated(updated);
            ad.setContactId(contact);
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to create new recruitment ad for village: " + villageId + ": " + sqx.getMessage(), sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }
}

