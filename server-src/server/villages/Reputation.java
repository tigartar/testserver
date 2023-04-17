/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Reputation
implements MiscConstants,
Comparable<Reputation> {
    private static final String DELETEREPUTATION = "DELETE FROM REPUTATION WHERE WURMID=? AND VILLAGEID=?";
    private static final String UPDATEREPUTATION = "UPDATE REPUTATION SET REPUTATION=?,PERMANENT=? WHERE WURMID=? AND VILLAGEID=?";
    private static final String CREATEREPUTATION = "INSERT INTO REPUTATION (REPUTATION,PERMANENT, WURMID,VILLAGEID) VALUES(?,?,?,?)";
    private final long wurmid;
    private final int villageId;
    private boolean permanent = false;
    private byte value = 0;
    private static final Logger logger = Logger.getLogger(Reputation.class.getName());
    private final boolean guest;

    Reputation(long wurmId, int village, boolean perma, int val, boolean isGuest, boolean loading) {
        this.wurmid = wurmId;
        this.villageId = village;
        this.permanent = perma;
        if (val > 100) {
            val = 100;
        } else if (val < -100) {
            val = -100;
        }
        this.value = (byte)val;
        this.guest = isGuest;
        if (!loading) {
            this.create();
        }
    }

    @Override
    public int compareTo(Reputation otherReputation) {
        try {
            return this.getNameFor().compareTo(otherReputation.getNameFor());
        }
        catch (NoSuchPlayerException e) {
            return 0;
        }
    }

    public int getValue() {
        return this.value;
    }

    public boolean isGuest() {
        return this.guest;
    }

    void setValue(int val, boolean override) {
        if (val > 100) {
            val = 100;
        } else if (val < -100) {
            val = -100;
        }
        if (!this.permanent || override) {
            this.value = (byte)val;
        }
        if (this.value == 0) {
            this.delete();
        } else {
            this.update();
        }
    }

    public long getWurmId() {
        return this.wurmid;
    }

    public Creature getCreature() {
        Creature toReturn = null;
        try {
            toReturn = Server.getInstance().getCreature(this.wurmid);
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
            // empty catch block
        }
        return toReturn;
    }

    public String getNameFor() throws NoSuchPlayerException {
        String name = "Unknown";
        if (this.guest) {
            name = name + " guest";
        }
        if (WurmId.getType(this.wurmid) == 0) {
            try {
                name = Players.getInstance().getNameFor(this.wurmid);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
                name = "";
            }
        } else {
            name = name + " creature";
        }
        return name;
    }

    public Village getVillage() {
        Village toReturn = null;
        try {
            toReturn = Villages.getVillage(this.villageId);
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, "No village for reputation with wurmid " + this.wurmid + " and villageid " + this.villageId, nsv);
        }
        return toReturn;
    }

    public boolean isPermanent() {
        return this.permanent;
    }

    public void setPermanent(boolean perma) {
        this.permanent = perma;
        this.update();
    }

    void modify(int val) {
        if (val != 0 && !this.permanent) {
            this.value = (byte)(this.value + val);
            if (this.value > 100) {
                this.value = (byte)100;
            } else if (this.value < -100) {
                this.value = (byte)-100;
            }
            if (this.value == 0) {
                this.delete();
            } else {
                this.update();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETEREPUTATION);
            ps.setLong(1, this.wurmid);
            ps.setInt(2, this.villageId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to delete reputation for wurmid=" + this.wurmid + ", village with id=" + this.villageId, sqx);
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
    private void create() {
        if (!this.guest) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(CREATEREPUTATION);
                ps.setByte(1, this.value);
                ps.setBoolean(2, this.permanent);
                ps.setLong(3, this.wurmid);
                ps.setInt(4, this.villageId);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to create reputation for wurmid=" + this.wurmid + ", village with id=" + this.villageId, sqx);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void update() {
        if (!this.guest) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            try {
                dbcon = DbConnector.getZonesDbCon();
                ps = dbcon.prepareStatement(UPDATEREPUTATION);
                ps.setByte(1, this.value);
                ps.setBoolean(2, this.permanent);
                ps.setLong(3, this.wurmid);
                ps.setInt(4, this.villageId);
                ps.executeUpdate();
            }
            catch (SQLException sqx) {
                try {
                    logger.log(Level.WARNING, "Failed to update reputation for wurmid=" + this.wurmid + ", village with id=" + this.villageId, sqx);
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
}

