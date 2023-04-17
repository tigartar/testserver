/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.utils.CreaturePositionDbUpdatable;
import com.wurmonline.server.utils.DatabaseUpdater;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaturePositionDatabaseUpdater
extends DatabaseUpdater<CreaturePositionDbUpdatable> {
    private static final Logger logger = Logger.getLogger(CreaturePositionDatabaseUpdater.class.getName());
    private final Map<Long, CreaturePositionDbUpdatable> updatesMap = new ConcurrentHashMap<Long, CreaturePositionDbUpdatable>();

    public CreaturePositionDatabaseUpdater(String aUpdaterDescription, int aMaxUpdatablesToRemovePerCycle) {
        super(aUpdaterDescription, CreaturePositionDbUpdatable.class, aMaxUpdatablesToRemovePerCycle);
        logger.info("Creating Creature Position Updater.");
    }

    @Override
    public void addToQueue(CreaturePositionDbUpdatable updatable) {
        if (updatable != null) {
            CreaturePositionDbUpdatable waiting;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Adding to database " + updatable + " updatable queue: " + updatable);
            }
            if ((waiting = this.updatesMap.get(updatable.getId())) != null) {
                this.queue.remove(waiting);
            }
            this.updatesMap.put(updatable.getId(), updatable);
            this.queue.add(updatable);
        }
    }

    @Override
    Connection getDatabaseConnection() throws SQLException {
        return DbConnector.getCreatureDbCon();
    }

    @Override
    void addUpdatableToBatch(PreparedStatement updateStatement, CreaturePositionDbUpdatable aDbUpdatable) throws SQLException {
        this.updatesMap.remove(aDbUpdatable.getId());
        updateStatement.setFloat(1, aDbUpdatable.getPositionX());
        updateStatement.setFloat(2, aDbUpdatable.getPositionY());
        updateStatement.setFloat(3, aDbUpdatable.getPositionZ());
        float rot = Creature.normalizeAngle(aDbUpdatable.getRotation());
        updateStatement.setFloat(4, rot);
        updateStatement.setInt(5, aDbUpdatable.getZoneid());
        updateStatement.setInt(6, aDbUpdatable.getLayer());
        updateStatement.setLong(7, aDbUpdatable.getBridgeId());
        updateStatement.setLong(8, aDbUpdatable.getId());
        updateStatement.addBatch();
    }
}

