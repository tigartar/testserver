/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorChecks
implements MiscConstants,
CounterTypes {
    private static final Logger logger = Logger.getLogger(ErrorChecks.class.getName());

    private ErrorChecks() {
    }

    public static void checkCreatures(Creature performer, String searchString) {
        long lStart = System.nanoTime();
        int nums = 0;
        Creature[] crets = Creatures.getInstance().getCreatures();
        boolean empty = searchString == null || searchString.length() == 0;
        performer.getCommunicator().sendSafeServerMessage("Starting creature check...");
        for (int x = 0; x < crets.length; ++x) {
            if (!empty && !crets[x].getName().contains(searchString)) continue;
            VolaTile t = crets[x].getCurrentTile();
            if (t != null) {
                try {
                    Zone z = Zones.getZone(crets[x].getTileX(), crets[x].getTileY(), crets[x].isOnSurface());
                    VolaTile rt = z.getTileOrNull(crets[x].getTileX(), crets[x].getTileY());
                    if (rt != null) {
                        int xx;
                        if (rt.getTileX() != t.getTileX() || rt.getTileY() != t.getTileY()) {
                            performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] at " + crets[x].getTileX() + "," + crets[x].getTileY() + " currenttile at " + t.getTileX() + " " + t.getTileY());
                            ++nums;
                        }
                        boolean found = false;
                        Creature[] cc = rt.getCreatures();
                        for (xx = 0; xx < cc.length; ++xx) {
                            if (cc[xx].getWurmId() != crets[x].getWurmId()) continue;
                            found = true;
                        }
                        if (found) continue;
                        if (!crets[x].isDead()) {
                            performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] not in list on tile " + rt.getTileX() + " " + rt.getTileY() + " #" + rt.hashCode() + " xy=" + crets[x].getTileX() + ", " + crets[x].getTileY() + " surf=" + crets[x].isOnSurface() + " inactive=" + rt.isInactive());
                            ++nums;
                        }
                        found = false;
                        cc = t.getCreatures();
                        for (xx = 0; xx < cc.length; ++xx) {
                            if (cc[xx].getWurmId() != crets[x].getWurmId()) continue;
                            found = true;
                        }
                        if (!found) {
                            GuardTower tower;
                            if (!crets[x].isDead()) {
                                performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] not in list on CURRENT tile " + t.getTileX() + " " + t.getTileY() + " #" + t.hashCode() + " xy=" + crets[x].getTileX() + ", " + crets[x].getTileY() + " surf=" + crets[x].isOnSurface() + " inactive=" + t.isInactive());
                            }
                            if (!crets[x].isDead()) continue;
                            boolean delete = true;
                            if (crets[x].isKingdomGuard() && (tower = Kingdoms.getTower(crets[x])) != null) {
                                try {
                                    delete = false;
                                    tower.returnGuard(crets[x]);
                                    performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] returned to tower.");
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                            }
                            if (!delete) continue;
                            if (DbCreatureStatus.getIsLoaded(crets[x].getWurmId()) == 0) {
                                crets[x].destroy();
                            }
                            performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] destroyed.");
                            continue;
                        }
                        performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] IS in list on CURRENT tile " + t.getTileX() + " " + t.getTileY() + " #" + t.hashCode() + " xy=" + crets[x].getTileX() + ", " + crets[x].getTileY() + " surf=" + crets[x].isOnSurface() + " inactive=" + t.isInactive());
                        continue;
                    }
                    performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] null tile but current at " + t.getTileX() + ", " + t.getTileY());
                }
                catch (NoSuchZoneException nsz) {
                    performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] no zone at " + t.getTileX() + ", " + t.getTileY());
                }
                continue;
            }
            performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] null current tile.");
        }
        performer.getCommunicator().sendSafeServerMessage("...done. " + nums + " errors.");
        logger.info("#checkCreatures took " + (float)(System.nanoTime() - lStart) / 1000000.0f + "ms.");
    }

    public static void checkItems(Creature performer, String searchString) {
        long lStart = System.nanoTime();
        logger.info(performer + " is checking Items using search string: " + searchString);
        Item[] items = Items.getAllItems();
        boolean empty = searchString == null || searchString.length() == 0;
        performer.getCommunicator().sendSafeServerMessage("Starting items check...");
        int nums = 0;
        for (int x = 0; x < items.length; ++x) {
            if (!empty && !items[x].getName().contains(searchString) || items[x].getZoneId() < 0 || items[x].getTemplateId() == 177) continue;
            try {
                Zone z = Zones.getZone(items[x].getTileX(), items[x].getTileY(), items[x].isOnSurface());
                VolaTile rt = z.getTileOrNull(items[x].getTileX(), items[x].getTileY());
                if (rt != null) {
                    if (rt.getTileX() != items[x].getTileX() || rt.getTileY() != items[x].getTileY()) {
                        performer.getCommunicator().sendNormalServerMessage(items[x].getName() + " [" + items[x].getWurmId() + "] at " + items[x].getTileX() + "," + items[x].getTileY() + " currenttile at " + rt.getTileX() + " " + rt.getTileY());
                        ++nums;
                    }
                    Item[] cc = rt.getItems();
                    boolean found = false;
                    for (int xx = 0; xx < cc.length; ++xx) {
                        if (cc[xx].getWurmId() != items[x].getWurmId()) continue;
                        found = true;
                    }
                    if (found) continue;
                    performer.getCommunicator().sendNormalServerMessage(items[x].getName() + " [" + items[x].getWurmId() + "] not in list on tile " + rt.getTileX() + " " + rt.getTileY() + " inactive=" + rt.isInactive());
                    ++nums;
                    continue;
                }
                performer.getCommunicator().sendNormalServerMessage(items[x].getName() + " [" + items[x].getWurmId() + "] last:" + items[x].getLastParentId() + " pile=" + (WurmId.getType(items[x].lastParentId) == 6) + ", null tile but current at " + items[x].getTileX() + ", " + items[x].getTileY());
                ++nums;
                continue;
            }
            catch (NoSuchZoneException nsz) {
                performer.getCommunicator().sendNormalServerMessage(items[x].getName() + " [" + items[x].getWurmId() + "] no zone at " + items[x].getTileX() + ", " + items[x].getTileY());
            }
        }
        performer.getCommunicator().sendSafeServerMessage("...done. " + nums + " errors.");
        logger.info("#checkItems took " + (float)(System.nanoTime() - lStart) / 1000000.0f + "ms.");
    }

    public static void getInfo(Creature performer, int tilex, int tiley, int layer) {
        try {
            Zone z = Zones.getZone(tilex, tiley, layer >= 0);
            VolaTile rt = z.getOrCreateTile(tilex, tiley);
            Creature[] cc = rt.getCreatures();
            VirtualZone[] watchers = rt.getWatchers();
            for (int xx = 0; xx < cc.length; ++xx) {
                performer.getCommunicator().sendNormalServerMessage(tilex + ", " + tiley + " contains " + cc[xx].getName());
                try {
                    Server.getInstance().getCreature(cc[xx].getWurmId());
                }
                catch (NoSuchCreatureException nsc) {
                    performer.getCommunicator().sendNormalServerMessage("The Creatures list does NOT contain " + cc[xx].getWurmId());
                }
                catch (NoSuchPlayerException nsp) {
                    performer.getCommunicator().sendNormalServerMessage("The Players list does NOT contain " + cc[xx].getWurmId());
                }
                for (int v = 0; v < watchers.length; ++v) {
                    try {
                        if (watchers[v].containsCreature(cc[xx])) continue;
                        if (watchers[v].getWatcher() != null && watchers[v].getWatcher().getWurmId() != cc[xx].getWurmId()) {
                            performer.getCommunicator().sendNormalServerMessage(cc[xx].getName() + " (" + cc[xx].getWurmId() + ") is not visible to " + watchers[v].getWatcher().getName());
                            continue;
                        }
                        if (watchers[v].getWatcher() != null) continue;
                        performer.getCommunicator().sendNormalServerMessage("The tile is monitored by an unknown creature or player who will not see the creature.");
                        continue;
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            Item[] items = rt.getItems();
            if (Servers.localServer.testServer) {
                for (Item i : items) {
                    String itemMessage = String.format("It contains %s, at floor level %d, at Z position %.2f", i.getName(), i.getFloorLevel(), Float.valueOf(i.getPosZ()));
                    performer.getCommunicator().sendNormalServerMessage(itemMessage);
                }
                if (performer.getPower() >= 5) {
                    String zoneMessage = String.format("Tile belongs to zone %d, which covers %d, %d to %d, %d.", z.getId(), z.getStartX(), z.getStartY(), z.getEndX(), z.getEndY());
                    performer.getCommunicator().sendNormalServerMessage(zoneMessage);
                    VolaTile caveTile = Zones.getOrCreateTile(rt.tilex, rt.tiley, false);
                    String caveVTMessage = String.format("Cave VolaTile instance transition is %s, layer is %d. It contains %d items.", caveTile.isTransition(), caveTile.getLayer(), caveTile.getItems().length);
                    performer.getCommunicator().sendNormalServerMessage(caveVTMessage);
                    VolaTile surfTile = Zones.getOrCreateTile(rt.tilex, rt.tiley, true);
                    String surfVTMessage = String.format("Surface VolaTile instance transition is %s, layer is %d. It contains %d items.", surfTile.isTransition(), surfTile.getLayer(), surfTile.getItems().length);
                    performer.getCommunicator().sendNormalServerMessage(surfVTMessage);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("It contains " + items.length + " items.");
            }
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage(tilex + "," + tiley + " no zone.");
        }
        try {
            float height = Zones.calculateHeight((tilex << 2) + 2, (tiley << 2) + 2, performer.isOnSurface()) * 10.0f;
            byte path = Cults.getPathFor(tilex, tiley, 0, (int)height);
            performer.getCommunicator().sendNormalServerMessage("Meditation path is " + Cults.getPathNameFor(path) + ".");
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, nsz.getMessage(), nsz);
        }
    }

    public static void checkZones(Creature checker) {
        logger.info(checker.getName() + " checking zones");
        checker.getCommunicator().sendNormalServerMessage("Checking cave zone tiles:");
        Zones.checkAllCaveZones(checker);
        checker.getCommunicator().sendNormalServerMessage("Checking surface zone tiles:");
        Zones.checkAllSurfaceZones(checker);
        checker.getCommunicator().sendNormalServerMessage("Done.");
        logger.info(checker.getName() + " finished checking zones");
    }

    public static void checkItemWatchers() {
    }
}

