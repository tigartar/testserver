/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.banks;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BankSlot
implements MiscConstants {
    public static final String VERSION = "$Revision: 1.2 $";
    public Item item;
    public boolean stasis;
    public long inserted = -10L;
    private final long bank;
    private static final String CREATE = "INSERT INTO BANKS_ITEMS (ITEMID,BANKID,INSERTED,STASIS) VALUES(?,?,?,?)";
    private static final String DELETESLOT = "DELETE FROM BANKS_ITEMS WHERE ITEMID=?";
    private static final Logger logger = Logger.getLogger(BankSlot.class.getName());

    BankSlot(Item aItem, long aBank, boolean aStas, long aInserted, boolean aCreate) {
        this.item = aItem;
        this.bank = aBank;
        this.stasis = aStas;
        this.inserted = aInserted;
        if (aCreate) {
            this.create();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void create() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(CREATE);
            ps.setLong(1, this.item.getWurmId());
            ps.setLong(2, this.bank);
            ps.setLong(3, System.currentTimeMillis());
            ps.setBoolean(4, this.stasis);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            block4: {
                try {
                    logger.log(Level.WARNING, "Failed to create bank slot for item " + this.item.getWurmId() + ", SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                    SQLException lNext = sqx.getNextException();
                    if (lNext == null) break block4;
                    logger.log(Level.WARNING, "Failed to create bank slot for item " + this.item.getWurmId() + ", next exception", lNext);
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
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void delete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getEconomyDbCon();
            ps = dbcon.prepareStatement(DELETESLOT);
            ps.setLong(1, this.item.getWurmId());
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            block4: {
                try {
                    logger.log(Level.WARNING, "Failed to delete bankslot for bank " + this.bank + ", SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                    SQLException lNext = sqx.getNextException();
                    if (lNext == null) break block4;
                    logger.log(Level.WARNING, "Failed to delete bankslot for bank " + this.bank + ", next exception", lNext);
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
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item aItem) {
        this.item = aItem;
    }

    public long getInserted() {
        return this.inserted;
    }

    public void setInserted(long aInserted) {
        this.inserted = aInserted;
    }

    public boolean isStasis() {
        return this.stasis;
    }

    public long getBank() {
        return this.bank;
    }
}

