/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Eigc;
import com.wurmonline.server.Server;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EigcClient {
    private static final Logger logger = Logger.getLogger(EigcClient.class.getName());
    private final String eigcUserName;
    private String eigcUserPassword;
    private String currentPlayerName = "";
    private String serviceBundle = "";
    private String playerAccount = "";
    private long serviceExpirationTime = Long.MAX_VALUE;
    private static final String UPDATE_EIGC_ACCOUNT = "UPDATE EIGC SET PASSWORD=?,SERVICEBUNDLE=?,EXPIRATION=?,EMAIL=? WHERE USERNAME=?";
    private long lastUsed = 0L;

    public EigcClient(String eigcUserId, String clientPass, String services, long expirationTime, String accountName) {
        this.eigcUserName = eigcUserId;
        this.eigcUserPassword = clientPass;
        this.serviceBundle = services;
        this.serviceExpirationTime = expirationTime;
        this.playerAccount = accountName;
        if (logger.isLoggable(Level.FINER)) {
            logger.fine("Created EIGC Client for user ID: " + eigcUserId);
        }
    }

    public void setExpiration(long expirationDate) {
        this.serviceExpirationTime = expirationDate;
    }

    public long getExpiration() {
        return this.serviceExpirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > this.serviceExpirationTime;
    }

    public void setPlayerName(String newPlayerName, String reason) {
        logger.log(Level.INFO, "Setting client " + this.getClientId() + " to player name " + newPlayerName + " reason=" + reason);
        this.currentPlayerName = newPlayerName;
        if (this.currentPlayerName == null || this.currentPlayerName.length() == 0) {
            this.setLastUsed(System.currentTimeMillis());
        } else {
            this.setLastUsed(Long.MAX_VALUE);
        }
        if (System.currentTimeMillis() > this.getExpiration()) {
            Eigc.modifyUser(this.getClientId(), "proximity", Long.MAX_VALUE);
        }
    }

    public String getPlayerName() {
        return this.currentPlayerName;
    }

    public String getAccountName() {
        return this.playerAccount;
    }

    public void setAccountName(String newAccountName) {
        this.playerAccount = newAccountName;
    }

    public boolean isPermanent() {
        return this.playerAccount != null && this.playerAccount.length() > 0;
    }

    public void setServiceBundle(String newServices) {
        this.serviceBundle = newServices;
    }

    public String getServiceBundle() {
        return this.serviceBundle;
    }

    public void setPassword(String newPassword) {
        this.eigcUserPassword = newPassword;
    }

    public String getPassword() {
        return this.eigcUserPassword;
    }

    public String getClientId() {
        return this.eigcUserName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateAccount() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATE_EIGC_ACCOUNT);
            ps.setString(1, this.eigcUserPassword);
            ps.setString(2, this.serviceBundle);
            ps.setLong(3, this.serviceExpirationTime);
            ps.setString(4, this.playerAccount);
            ps.setString(5, this.eigcUserName);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem updating EIGC for username " + this.eigcUserName + " due to " + sqx, sqx);
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

    protected final long getLastUsed() {
        return this.lastUsed;
    }

    private final void setLastUsed(long time) {
        this.lastUsed = time;
    }

    protected final boolean isUsed() {
        return this.lastUsed > System.currentTimeMillis();
    }

    protected final String timeSinceLastUse() {
        return Server.getTimeFor(System.currentTimeMillis() - this.lastUsed);
    }
}

