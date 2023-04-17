/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.creatures.ai.StaticPathFinder;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaturePathFinder
extends TimerTask {
    private final Map<Creature, PathTile> pathTargets = new ConcurrentHashMap<Creature, PathTile>();
    private boolean keeprunning = true;
    public static final long SLEEP_TIME = 25L;
    private static final StaticPathFinder pathFinder = new StaticPathFinder();
    private static Logger logger = Logger.getLogger(CreaturePathFinder.class.getName());
    private static boolean log = false;

    public final void addTarget(Creature c, PathTile target) {
        this.pathTargets.put(c, target);
    }

    public final void removeTarget(Creature c) {
        this.pathTargets.remove(c);
    }

    public boolean isLog() {
        return log;
    }

    public final void toggleLog() {
        this.setLog(!this.isLog());
    }

    public void setLog(boolean nlog) {
        log = nlog;
    }

    public final void startRunning() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate((TimerTask)this, 40000L, 25L);
    }

    public final void shutDown() {
        this.keeprunning = false;
    }

    @Override
    public void run() {
        if (this.keeprunning) {
            long now = System.currentTimeMillis();
            if (!this.pathTargets.isEmpty()) {
                Iterator<Map.Entry<Creature, PathTile>> it = this.pathTargets.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Creature, PathTile> entry = it.next();
                    Creature creature = entry.getKey();
                    PathTile p = entry.getValue();
                    try {
                        Path path = creature.findPath(p.getTileX(), p.getTileY(), pathFinder);
                        if (path == null) continue;
                        if (p.hasSpecificPos()) {
                            if (path.getPathTiles().isEmpty()) {
                                path.getPathTiles().add(new PathTile(creature.getTileX(), creature.getTileY(), creature.getCurrentTileNum(), creature.isOnSurface(), creature.getFloorLevel()));
                            }
                            PathTile lastTile = path.getPathTiles().getLast();
                            lastTile.setSpecificPos(p.getPosX(), p.getPosY());
                        }
                        creature.sendToLoggers("Found path to " + p.getTileX() + "," + p.getTileY());
                        creature.getStatus().setPath(path);
                        creature.receivedPath = true;
                        it.remove();
                    }
                    catch (NoPathException np) {
                        creature.sendToLoggers("No Path to " + p.getTileX() + "," + p.getTileY() + " pathfindcounter=" + creature.getPathfindCounter() + " || " + np.getMessage());
                        it.remove();
                        creature.setPathing(false, false);
                    }
                }
            }
            if (log && System.currentTimeMillis() - now > 0L) {
                logger.log(Level.INFO, "Norm Finding paths took " + (System.currentTimeMillis() - now) + " ms for " + this.pathTargets.size());
            }
        } else {
            logger.log(Level.INFO, "Shutting down Norm pathfinder");
            this.cancel();
        }
    }
}

