/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DatabaseUpdater;
import com.wurmonline.server.utils.ItemLastOwnerDatabaseUpdatable;
import com.wurmonline.server.utils.ItemOwnerDatabaseUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemLastOwnerDatabaseUpdater
extends DatabaseUpdater<ItemLastOwnerDatabaseUpdatable> {
    private static final Logger logger = Logger.getLogger(ItemOwnerDatabaseUpdater.class.getName());

    public ItemLastOwnerDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle) {
        super(aUpdaterDescription, ItemLastOwnerDatabaseUpdatable.class, aMaxUpdatablesToRemovePerCycle);
        logger.info("Creating Item Last Owner Updater.");
    }

    @Override
    Connection getDatabaseConnection() throws SQLException {
        return DbConnector.getItemDbCon();
    }

    @Override
    void addUpdatableToBatch(PreparedStatement updateStatement, ItemLastOwnerDatabaseUpdatable aDbUpdatable) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Adding to batch: " + aDbUpdatable);
        }
        updateStatement.setLong(1, aDbUpdatable.getOwner());
        updateStatement.setLong(2, aDbUpdatable.getId());
        updateStatement.addBatch();
    }
}

