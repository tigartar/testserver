/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils.logging;

import com.wurmonline.server.Constants;
import com.wurmonline.server.utils.logging.DatabaseLogger;
import com.wurmonline.server.utils.logging.ItemTransfer;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public final class ItemTransferDatabaseLogger
extends DatabaseLogger<ItemTransfer> {
    private static Logger logger = Logger.getLogger(ItemTransferDatabaseLogger.class.getName());

    public ItemTransferDatabaseLogger(String aLoggerDescription, int aMaxLoggablesToRemovePerCycle) {
        super(aLoggerDescription, ItemTransfer.class, aMaxLoggablesToRemovePerCycle);
        logger.info("Creating Item Transfer logger, System useItemTransferLog option: " + Constants.useItemTransferLog);
    }

    @Override
    void addLoggableToBatch(PreparedStatement logsStatement, ItemTransfer object) throws SQLException {
        ItemTransfer itemTransfer = object;
        logsStatement.setLong(1, itemTransfer.getItemId());
        logsStatement.setString(2, itemTransfer.getItemName());
        logsStatement.setLong(3, itemTransfer.getOldOwnerId());
        logsStatement.setString(4, itemTransfer.getOldOwnerName());
        logsStatement.setLong(5, itemTransfer.getNewOwnerId());
        logsStatement.setString(6, itemTransfer.getNewOwnerName());
        logsStatement.setDate(7, new Date(itemTransfer.getTransferTime()));
        logsStatement.addBatch();
    }
}

