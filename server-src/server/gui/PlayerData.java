/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class PlayerData
implements MiscConstants {
    private static final String saveData = "UPDATE PLAYERS SET CURRENTSERVER=?, POWER=?, REIMBURSED=?, UNDEADTYPE=? WHERE WURMID=?";
    private static final String savePosition = "UPDATE POSITION SET POSX=?,POSY=? WHERE WURMID=?";
    private String name;
    private long wurmid;
    private int power;
    private float posx;
    private float posy;
    private int server;
    private boolean reimbursed;
    private byte undeadType = 0;
    private static final Logger logger = Logger.getLogger(PlayerData.class.getName());

    public final void save() throws SQLException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(saveData);
            ps.setInt(1, this.getServer());
            ps.setByte(2, (byte)this.getPower());
            ps.setBoolean(3, this.isReimbursed());
            ps.setByte(4, this.undeadType);
            ps.setLong(5, this.getWurmid());
            ps.executeUpdate();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
        this.savePosition();
    }

    public final void savePosition() throws SQLException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(savePosition);
            ps.setFloat(1, this.getPosx());
            ps.setFloat(2, this.getPosy());
            ps.setLong(3, this.getWurmid());
            ps.executeUpdate();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getWurmid() {
        return this.wurmid;
    }

    public void setWurmid(long wurmid) {
        this.wurmid = wurmid;
    }

    public int getPower() {
        return this.power;
    }

    public void setPower(int power) {
        if (power < 0) {
            power = 0;
        }
        if (power > 5) {
            power = 5;
        }
        if (power > this.power) {
            this.setReimbursed(false);
        }
        this.power = power;
    }

    public float getPosx() {
        return this.posx;
    }

    public void setPosx(float posx) {
        this.posx = posx;
    }

    public float getPosy() {
        return this.posy;
    }

    public void setPosy(float posy) {
        this.posy = posy;
    }

    public int getServer() {
        return this.server;
    }

    public void setServer(int server) {
        this.server = server;
    }

    public boolean isReimbursed() {
        return this.reimbursed;
    }

    public void setReimbursed(boolean reimbursed) {
        this.reimbursed = reimbursed;
    }

    public byte getUndeadType() {
        return this.undeadType;
    }

    public void setUndeadType(byte undeadType) {
        this.undeadType = undeadType;
    }

    public boolean isUndead() {
        return this.undeadType != 0;
    }
}

