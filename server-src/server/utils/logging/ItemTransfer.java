/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils.logging;

import com.wurmonline.server.utils.logging.WurmLoggable;

public final class ItemTransfer
implements WurmLoggable {
    private long itemId;
    private String itemName;
    private long oldOwnerId;
    private String oldOwnerName;
    private long newOwnerId;
    private String newOwnerName;
    private long transferTime;
    private static final String INSERT_ITEM_TRANSFER = "INSERT INTO ITEM_TRANSFER_LOG (ITEMID, ITEMNAME, OLDOWNERID, OLDOWNERNAME, NEWOWNERID, NEWOWNERNAME, TRANSFERTIME) VALUES ( ?, ?, ?, ?, ?, ?, ?)";

    public ItemTransfer(long aItemId, String aItemName, long aOldOwnerId, String aOldOwnerName, long aNewOwnerId, String aNewOwnerName, long aTransferTime) {
        this.itemId = aItemId;
        this.itemName = aItemName;
        this.oldOwnerId = aOldOwnerId;
        this.oldOwnerName = aOldOwnerName;
        this.newOwnerId = aNewOwnerId;
        this.newOwnerName = aNewOwnerName;
        this.transferTime = aTransferTime;
    }

    public long getItemId() {
        return this.itemId;
    }

    public void setItemId(long aItemId) {
        this.itemId = aItemId;
    }

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String aItemName) {
        this.itemName = aItemName;
    }

    public long getOldOwnerId() {
        return this.oldOwnerId;
    }

    public void setOldOwnerId(long aOldOwnerId) {
        this.oldOwnerId = aOldOwnerId;
    }

    public String getOldOwnerName() {
        return this.oldOwnerName;
    }

    public void setOldOwnerName(String aOldOwnerName) {
        this.oldOwnerName = aOldOwnerName;
    }

    public long getNewOwnerId() {
        return this.newOwnerId;
    }

    public void setNewOwnerId(long aNewOwnerId) {
        this.newOwnerId = aNewOwnerId;
    }

    public String getNewOwnerName() {
        return this.newOwnerName;
    }

    public void setNewOwnerName(String aNewOwnerName) {
        this.newOwnerName = aNewOwnerName;
    }

    public long getTransferTime() {
        return this.transferTime;
    }

    public void setTransferTime(long aTransferTime) {
        this.transferTime = aTransferTime;
    }

    @Override
    public String getDatabaseInsertStatement() {
        return INSERT_ITEM_TRANSFER;
    }

    @Override
    public String toString() {
        return "ItemTransfer [itemId=" + this.itemId + ", itemName=" + this.itemName + ", newOwnerId=" + this.newOwnerId + ", newOwnerName=" + this.newOwnerName + ", oldOwnerId=" + this.oldOwnerId + ", oldOwnerName=" + this.oldOwnerName + ", transferTime=" + this.transferTime + "]";
    }
}

