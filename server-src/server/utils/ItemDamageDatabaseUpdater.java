/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DatabaseUpdater;
import com.wurmonline.server.utils.ItemDamageDatabaseUpdatable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemDamageDatabaseUpdater
extends DatabaseUpdater<ItemDamageDatabaseUpdatable> {
    private static final Logger logger = Logger.getLogger(ItemDamageDatabaseUpdater.class.getName());

    public ItemDamageDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle) {
        super(aUpdaterDescription, ItemDamageDatabaseUpdatable.class, aMaxUpdatablesToRemovePerCycle);
        logger.info("Creating Item Damage Updater.");
    }

    @Override
    Connection getDatabaseConnection() throws SQLException {
        return DbConnector.getItemDbCon();
    }

    @Override
    void addUpdatableToBatch(PreparedStatement updateStatement, ItemDamageDatabaseUpdatable aDbUpdatable) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Adding to batch: " + aDbUpdatable);
        }
        updateStatement.setFloat(1, aDbUpdatable.getDamage());
        updateStatement.setLong(2, aDbUpdatable.getLastMaintained());
        updateStatement.setLong(3, aDbUpdatable.getId());
        updateStatement.addBatch();
    }
}

