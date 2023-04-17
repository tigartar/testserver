/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.intra.IntraClient;
import com.wurmonline.server.intra.IntraCommand;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PasswordTransfer
extends IntraCommand
implements MiscConstants {
    private static final String CREATE_PASSWORD_TRANSFER = "INSERT INTO PASSWORDTRANSFERS(NAME,WURMID,TIMESTAMP,PASSWORD) VALUES (?,?,?,?)";
    private static final String DELETE_PASSWORD_TRANSFER = "DELETE FROM PASSWORDTRANSFERS WHERE NAME=? AND WURMID=? AND TIMESTAMP=? AND PASSWORD=?";
    private static final String GET_ALL_PASSWORDTRANSFERS = "SELECT * FROM PASSWORDTRANSFERS";
    private static Logger logger = Logger.getLogger(PasswordTransfer.class.getName());
    private final String name;
    private final long wurmid;
    private final String newPassword;
    private final long timestamp;
    private boolean done = false;
    private IntraClient client = null;
    private boolean started = false;
    public boolean deleted = false;
    private boolean sentTransfer = false;
    public static final List<PasswordTransfer> transfers = new LinkedList<PasswordTransfer>();

    public PasswordTransfer(String aName, long playerId, String password, long _timestamp, boolean load) {
        this.name = aName;
        this.wurmid = playerId;
        this.newPassword = password;
        this.timestamp = _timestamp;
        this.timeOutTime = 30000L;
        if (!load) {
            this.save();
        }
        transfers.add(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void save() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(CREATE_PASSWORD_TRANSFER);
            ps.setString(1, this.name);
            ps.setLong(2, this.wurmid);
            ps.setLong(3, this.timestamp);
            ps.setString(4, this.newPassword);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
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
    private void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(DELETE_PASSWORD_TRANSFER);
            ps.setString(1, this.name);
            ps.setLong(2, this.wurmid);
            ps.setLong(3, this.timestamp);
            ps.setString(4, this.newPassword);
            ps.executeUpdate();
            this.deleted = true;
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, this.name + " " + sqex.getMessage(), sqex);
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
    public static void loadAllPasswordTransfers() {
        long start = System.nanoTime();
        int loadedPasswordTransfers = 0;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(GET_ALL_PASSWORDTRANSFERS);
            rs = ps.executeQuery();
            while (rs.next()) {
                new PasswordTransfer(rs.getString("Name"), rs.getLong("WURMID"), rs.getString("PASSWORD"), rs.getLong("TIMESTAMP"), true);
                ++loadedPasswordTransfers;
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + loadedPasswordTransfers + " PasswordTransfers from the database took " + (float)(end - start) / 1000000.0f + " ms");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + loadedPasswordTransfers + " PasswordTransfers from the database took " + (float)(end - start) / 1000000.0f + " ms");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.info("Loaded " + loadedPasswordTransfers + " PasswordTransfers from the database took " + (float)(end - start) / 1000000.0f + " ms");
    }

    @Override
    public boolean poll() {
        if (System.currentTimeMillis() > this.timeOutAt) {
            PlayerInfo info = PlayerInfoFactory.createPlayerInfo(this.name);
            try {
                info.load();
            }
            catch (Exception eex) {
                logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ".", eex);
                this.delete();
                this.done = true;
            }
            if (info.wurmId <= 0L) {
                logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ". No info available.");
                this.delete();
                this.done = true;
            } else if (info.currentServer == Servers.localServer.id) {
                this.delete();
                this.done = true;
            }
            if (!this.done) {
                this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
                ServerEntry entry = Servers.getServerWithId(info.currentServer);
                if (entry != null) {
                    if (new LoginServerWebConnection(info.currentServer).changePassword(this.wurmid, this.newPassword)) {
                        this.sentTransfer = true;
                        this.done = true;
                        this.delete();
                        return true;
                    }
                } else {
                    logger.log(Level.INFO, this.wurmid + " for currentserver " + info.currentServer + ": the server does not exist.");
                    this.delete();
                    this.done = true;
                }
            }
        }
        return false;
    }

    public boolean pollOld() {
        logger2.log(Level.INFO, "PT poll " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
        PlayerInfo info = PlayerInfoFactory.createPlayerInfo(this.name);
        try {
            info.load();
        }
        catch (Exception eex) {
            logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ".", eex);
            this.delete();
            this.done = true;
        }
        if (info.wurmId <= 0L) {
            logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ". No info available.");
            this.delete();
            this.done = true;
        } else if (info.currentServer == Servers.localServer.id) {
            this.delete();
            this.done = true;
        } else if (!(this.client != null || System.currentTimeMillis() <= this.timeOutAt && this.started)) {
            logger2.log(Level.INFO, "PT starting " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
            ServerEntry entry = Servers.getServerWithId(info.currentServer);
            if (entry != null) {
                try {
                    this.startTime = System.currentTimeMillis();
                    this.timeOutAt = this.startTime + this.timeOutTime;
                    this.started = true;
                    this.client = new IntraClient(entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), this);
                    this.client.login(entry.INTRASERVERPASSWORD, true);
                    this.done = false;
                }
                catch (IOException iox) {
                    this.done = true;
                }
            } else {
                this.delete();
                this.done = true;
                logger.log(Level.WARNING, "No server entry for server with id " + info.currentServer);
            }
        }
        if (this.client != null && !this.done) {
            if (System.currentTimeMillis() > this.timeOutAt) {
                logger2.log(Level.INFO, "PT timeout " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
                this.done = true;
            }
            if (this.client.loggedIn && !this.done && !this.sentTransfer) {
                try {
                    this.client.executePasswordUpdate(this.wurmid, this.newPassword, this.timestamp);
                    this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
                    this.sentTransfer = true;
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                    this.done = true;
                }
            }
            if (!this.done) {
                try {
                    this.client.update();
                }
                catch (Exception ex) {
                    this.done = true;
                }
            }
        }
        if (this.done && this.client != null) {
            this.sentTransfer = false;
            this.client.disconnect("Done");
            this.client = null;
            logger2.log(Level.INFO, "PT Disconnected " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
        }
        if (this.done) {
            logger2.log(Level.INFO, "PT finishing " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
        }
        return this.done;
    }

    @Override
    public void reschedule(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void remove(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void commandExecuted(IntraClient aClient) {
        this.delete();
        logger2.log(Level.INFO, "PT accepted " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", timestamp: " + this.timestamp);
        this.done = true;
    }

    @Override
    public void commandFailed(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void dataReceived(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void receivingData(ByteBuffer buffer) {
        this.done = true;
    }
}

