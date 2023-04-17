/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.players.AwardLadder;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Awards {
    public static final String INSERT_AWARDS = "INSERT INTO AWARDS(WURMID, DAYSPREM, MONTHSPREM, MONTHSEVER, CONSECMONTHS, SILVERSPURCHASED, LASTTICKEDPREM, CURRENTLOYALTY, TOTALLOYALTY) VALUES(?,?,?,?,?,?,?,?,?)";
    public static final String UPDATE_AWARDS = "UPDATE AWARDS SET DAYSPREM=?, MONTHSPREM=?, MONTHSEVER=?, CONSECMONTHS=?, SILVERSPURCHASED=?, LASTTICKEDPREM=?, TOTALLOYALTY=?, CURRENTLOYALTY=? WHERE WURMID=?";
    public static final String DELETE_AWARDS = "DELETE FROM AWARDS WHERE WURMID=?";
    private long wurmId;
    private static final Logger logger = Logger.getLogger(Awards.class.getName());
    private int daysPrem;
    private int monthsPaidEver;
    private int monthsPaidSinceReset;
    private int monthsPaidInARow;
    private int silversPaidEver;
    private long lastTickedDay;
    private int currentLoyaltyPoints;
    private int totalLoyaltyPoints;
    public static final Map<Long, Awards> allAwards = new ConcurrentHashMap<Long, Awards>();

    public Awards(long playerId, int daysPremium, int _monthsPaidEver, int monthsPaidInSuccession, int _monthsPaidSinceReset, int silversPurchased, long _lastTickedDay, int _currentLoyalty, int _totalLoyalty, boolean createInDb) {
        this.wurmId = playerId;
        this.daysPrem = daysPremium;
        this.monthsPaidEver = _monthsPaidEver;
        this.monthsPaidInARow = monthsPaidInSuccession;
        this.silversPaidEver = silversPurchased;
        this.lastTickedDay = _lastTickedDay;
        this.monthsPaidSinceReset = _monthsPaidSinceReset;
        this.currentLoyaltyPoints = _currentLoyalty;
        this.totalLoyaltyPoints = _totalLoyalty;
        allAwards.put(this.wurmId, this);
        if (createInDb) {
            this.save();
        }
    }

    public final String toString() {
        return "Awards for " + this.wurmId + ", days=" + this.daysPrem + ", mo's ever=" + this.monthsPaidEver + ", mo's row=" + this.monthsPaidInARow + ", mo's reset=" + this.monthsPaidSinceReset + ", silvers=" + this.silversPaidEver + ", loyalty=" + this.currentLoyaltyPoints + ", totalLoyalty=" + this.totalLoyaltyPoints + ", tick=" + Server.getTimeFor(System.currentTimeMillis() - this.lastTickedDay) + " ago";
    }

    public long getWurmId() {
        return this.wurmId;
    }

    public static final Awards getAwards(long wurmid) {
        return allAwards.get(wurmid);
    }

    public void setWurmId(long aWurmId) {
        this.wurmId = aWurmId;
    }

    public int getMonthsPaidEver() {
        return this.monthsPaidEver;
    }

    public void setMonthsPaidEver(int aMonthsPaidEver) {
        this.monthsPaidEver = aMonthsPaidEver;
    }

    public AwardLadder getNextReward() {
        return AwardLadder.getNextTotalAward(this.getMonthsPaidSinceReset());
    }

    public int getMonthsPaidInARow() {
        return this.monthsPaidInARow;
    }

    public void setMonthsPaidInARow(int aMonthsPaidInARow) {
        this.monthsPaidInARow = aMonthsPaidInARow;
    }

    public int getSilversPaidEver() {
        return this.silversPaidEver;
    }

    public void setSilversPaidEver(int aSilversPaidEver) {
        this.silversPaidEver = aSilversPaidEver;
    }

    public long getLastTickedDay() {
        return this.lastTickedDay;
    }

    public void setLastTickedDay(long aLastTickedDay) {
        this.lastTickedDay = aLastTickedDay;
    }

    public int getDaysPrem() {
        return this.daysPrem;
    }

    public void setDaysPrem(int aDaysPrem) {
        this.daysPrem = aDaysPrem;
    }

    public int getMonthsPaidSinceReset() {
        return this.monthsPaidSinceReset;
    }

    public void setMonthsPaidSinceReset(int aMonthsPaidSinceReset) {
        this.monthsPaidSinceReset = aMonthsPaidSinceReset;
    }

    public int getCurrentLoyalty() {
        return this.currentLoyaltyPoints;
    }

    public void setCurrentLoyalty(int newLoyalty) {
        this.currentLoyaltyPoints = newLoyalty;
    }

    public int getTotalLoyalty() {
        return this.totalLoyaltyPoints;
    }

    public void setTotalLoyaltyPoints(int newTotal) {
        this.totalLoyaltyPoints = newTotal;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void save() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(INSERT_AWARDS);
            ps.setLong(1, this.wurmId);
            ps.setInt(2, this.daysPrem);
            ps.setInt(3, this.monthsPaidSinceReset);
            ps.setInt(4, this.monthsPaidEver);
            ps.setInt(5, this.monthsPaidInARow);
            ps.setInt(6, this.silversPaidEver);
            ps.setLong(7, this.lastTickedDay);
            ps.setInt(8, this.currentLoyaltyPoints);
            ps.setInt(9, this.totalLoyaltyPoints);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.wurmId + " " + sqex.getMessage(), sqex);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void update() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(UPDATE_AWARDS);
            ps.setInt(1, this.daysPrem);
            ps.setInt(2, this.monthsPaidSinceReset);
            ps.setInt(3, this.monthsPaidEver);
            ps.setInt(4, this.monthsPaidInARow);
            ps.setInt(5, this.silversPaidEver);
            ps.setLong(6, this.lastTickedDay);
            ps.setInt(7, this.currentLoyaltyPoints);
            ps.setInt(8, this.totalLoyaltyPoints);
            ps.setLong(9, this.wurmId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.wurmId + " " + sqex.getMessage(), sqex);
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
}

