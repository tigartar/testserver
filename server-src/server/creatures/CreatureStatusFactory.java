/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.DbCreatureStatus;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CreatureStatusFactory {
    private static final Logger logger = Logger.getLogger(CreatureStatusFactory.class.getName());

    private CreatureStatusFactory() {
    }

    static CreatureStatus createCreatureStatus(Creature creature, float posx, float posy, float rot, int layer) throws Exception {
        DbCreatureStatus toReturn = null;
        toReturn = new DbCreatureStatus(creature, posx, posy, rot, layer);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Created new CreatureStatus: " + toReturn);
        }
        return toReturn;
    }
}

