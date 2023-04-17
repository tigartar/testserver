/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathMesh;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.zones.Zones;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathFinder
implements MiscConstants {
    protected PathTile current;
    protected PathTile finish;
    protected PathTile start;
    protected int startX;
    protected int startY;
    protected int endX;
    protected int endY;
    protected float derivX;
    protected float derivY;
    protected float restX = 0.0f;
    protected float restY = 0.0f;
    private LinkedList<PathTile> pathList;
    private LinkedList<PathTile> pList;
    protected Creature creature = null;
    private static final int NOT_FOUND = 0;
    private static final int FOUND = 1;
    private static final int NO_PATH = 2;
    private static final int MAX_DISTANCE = 50;
    protected boolean surfaced = true;
    protected int stepsTaken = 0;
    private static final int MAX_ASTAR_STEPS = 10000;
    protected int maxSteps = 10000;
    protected boolean debug = false;
    protected int areaSize = 2;
    protected PathMesh mesh = null;
    private static final int WORLD_SIZE = 1 << Constants.meshSize;
    private static final Logger logger = Logger.getLogger(PathFinder.class.getName());
    protected boolean ignoreWalls = false;
    protected int creatureHalfHeight = 5;

    public PathFinder() {
    }

    public PathFinder(boolean ignoresWalls) {
        this.ignoreWalls = ignoresWalls;
    }

    public Path findPath(Creature aCreature, int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz) throws NoPathException {
        this.creature = aCreature;
        if (this.creature != null) {
            this.creatureHalfHeight = this.creature.getHalfHeightDecimeters();
        }
        return this.findPath(startTileX, startTileY, endTileX, endTileY, surf, areaSz);
    }

    private Path findPath(int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz) throws NoPathException {
        this.endX = Zones.safeTileX(endTileX);
        this.endY = Zones.safeTileY(endTileY);
        this.startX = Zones.safeTileX(startTileX);
        this.startY = Zones.safeTileY(startTileY);
        int _diffX = Math.abs(this.endX - this.startX);
        int _diffY = Math.abs(this.endY - this.startY);
        if (_diffX > 50) {
            int stepsX = Server.rand.nextInt(Math.min(50, _diffX + 1));
            if (this.endX < this.startX) {
                stepsX = -stepsX;
            }
            this.endX = this.startX + stepsX;
        }
        if (_diffY > 50) {
            int stepsY = Server.rand.nextInt(Math.min(50, _diffY + 1));
            if (this.endY < this.startY) {
                stepsY = -stepsY;
            }
            this.endY = this.startY + stepsY;
        }
        this.startX = Zones.safeTileX(this.startX);
        this.startY = Zones.safeTileY(this.startY);
        this.endX = Zones.safeTileX(this.endX);
        this.endY = Zones.safeTileY(this.endY);
        this.surfaced = surf;
        this.areaSize = areaSz;
        this.setMesh();
        Path toReturn = new Path();
        if (this.surfaced) {
            try {
                toReturn = this.rayCast(this.startX, this.startY, this.endX, this.endY, this.surfaced);
            }
            catch (NoPathException np) {
                toReturn = this.startAstar(this.startX, this.startY, this.endX, this.endY);
            }
        } else {
            toReturn = this.startAstar(this.startX, this.startY, this.endX, this.endY);
        }
        if (this.mesh != null) {
            this.mesh.clearPathables();
        }
        return toReturn;
    }

    Path startAstar(int _startX, int _startY, int _endX, int _endY) throws NoPathException {
        Path toReturn = new Path();
        try {
            toReturn = this.astar(_startX, _startY, _endX, _endY, this.surfaced);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest(this.creature.getName() + " astared a path.");
            }
        }
        catch (NoPathException np2) {
            if (this.creature != null) {
                if ((this.creature.isKingdomGuard() || this.creature.isSpiritGuard() || this.creature.isUnique() || this.creature.isDominated()) && this.creature.target == -10L) {
                    int _diffX = Math.max(1, Math.abs(_endX - _startX) / 2);
                    int _diffY = Math.max(1, Math.abs(_endY - _startY) / 2);
                    int stepsX = Server.rand.nextInt(Math.min(50, _diffX + 1));
                    if (this.endX < this.startX) {
                        stepsX = -stepsX;
                    }
                    this.endX = this.startX + stepsX;
                    int stepsY = Server.rand.nextInt(Math.min(50, _diffY + 1));
                    if (this.endY < this.startY) {
                        stepsY = -stepsY;
                    }
                    this.endY = this.startY + stepsY;
                    if (stepsY != 0 || stepsX != 0) {
                        this.setMesh();
                        toReturn = !this.surfaced || this.creature.isKingdomGuard() || this.creature.isUnique() || this.creature.isDominated() ? this.astar(_startX, _startY, _endX, _endY, this.surfaced) : this.rayCast(_startX, _startY, _endX, _endY, this.surfaced);
                    }
                }
                throw np2;
            }
            throw np2;
        }
        return toReturn;
    }

    public Path rayCast(int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz) throws NoPathException {
        this.startX = Math.max(0, startTileX);
        this.startY = Math.max(0, startTileY);
        this.endX = Math.min(WORLD_SIZE - 1, endTileX);
        this.endY = Math.min(WORLD_SIZE - 1, endTileY);
        this.surfaced = surf;
        this.areaSize = areaSz;
        this.setMesh();
        return this.rayCast(this.startX, this.startY, this.endX, this.endY, this.surfaced);
    }

    void setMesh() {
        this.mesh = new PathMesh(this.startX, this.startY, this.endX, this.endY, this.surfaced, this.areaSize);
        this.current = this.mesh.getStart();
        this.start = this.mesh.getStart();
        this.finish = this.mesh.getFinish();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Start is " + this.start.toString() + ", finish " + this.finish.toString());
        }
    }

    Path rayCast(int startTileX, int startTileY, int endTileX, int endTileY, boolean aSurfaced) throws NoPathException {
        short endHeight = Tiles.decodeHeight(this.finish.getTile());
        short startHeight = Tiles.decodeHeight(this.start.getTile());
        if (this.creature != null && !this.creature.isSwimming() && !this.creature.isSubmerged() && endHeight < -this.creatureHalfHeight && Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_CAVE_EXIT.id && Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_HOLE.id && endHeight < startHeight) {
            throw new NoPathException("Target in water.");
        }
        this.maxSteps = Math.max(Math.abs(this.endX - this.startX), Math.abs(this.endY - this.startY)) + 1;
        this.pathList = new LinkedList();
        float diffX = this.endX - this.startX;
        float diffY = this.endY - this.startY;
        if (diffX == 0.0f) {
            this.derivX = 0.0f;
            this.derivY = diffY;
        }
        if (diffY == 0.0f) {
            this.derivY = 0.0f;
            this.derivX = diffX;
        }
        if (diffX != 0.0f && diffY != 0.0f) {
            this.derivX = Math.abs(diffX / diffY);
            this.derivY = Math.abs(diffY / diffX);
        }
        if (diffY < 0.0f && this.derivY > 0.0f) {
            this.derivY = -this.derivY;
        }
        if (diffX < 0.0f && this.derivX > 0.0f) {
            this.derivX = -this.derivX;
        }
        while (!this.current.equals(this.finish)) {
            this.current = this.step();
            this.pathList.add(this.current);
        }
        return new Path(this.pathList);
    }

    PathTile step() throws NoPathException {
        int x = this.current.getTileX();
        int y = this.current.getTileY();
        boolean raycend = true;
        if (!this.surfaced) {
            raycend = false;
        }
        if (raycend && Math.abs(this.endX - x) <= 1 && Math.abs(this.endY - y) <= 1) {
            x = this.endX;
            y = this.endY;
        } else if (Math.abs(this.endX - x) < 1 && Math.abs(this.endY - y) < 1) {
            x = this.endX;
            y = this.endY;
            logger.log(Level.INFO, "This really shouldn't happen i guess, since it should have been detected already.");
        } else {
            if (this.derivX > 0.0f && x < this.endX) {
                if (this.derivX >= 1.0f) {
                    ++x;
                } else {
                    this.restX += this.derivX;
                    if (this.restX >= 1.0f) {
                        ++x;
                        this.restX -= 1.0f;
                    }
                }
            } else if (this.derivX < 0.0f && x > this.endX) {
                if (this.derivX <= -1.0f) {
                    --x;
                } else {
                    this.restX += this.derivX;
                    if (this.restX <= -1.0f) {
                        --x;
                        this.restX += 1.0f;
                    }
                }
            }
            if (this.derivY > 0.0f && y < this.endY) {
                if (this.derivY >= 1.0f) {
                    ++y;
                } else {
                    this.restY += this.derivY;
                    if (this.restY >= 1.0f) {
                        ++y;
                        this.restY -= 1.0f;
                    }
                }
            } else if (this.derivY < 0.0f && y > this.endY) {
                if (this.derivY <= -1.0f) {
                    --y;
                } else {
                    this.restY += this.derivY;
                    if (this.restY <= -1.0f) {
                        --y;
                        this.restY += 1.0f;
                    }
                }
            }
        }
        if (!this.mesh.contains(x, y)) {
            throw new NoPathException("Path missed at " + x + ", " + y);
        }
        PathTile toReturn = null;
        try {
            toReturn = this.mesh.getPathTile(x, y);
            if (!this.canPass(this.current, toReturn)) {
                throw new NoPathException("Path blocked between " + this.current.toString() + " and " + toReturn.toString());
            }
        }
        catch (ArrayIndexOutOfBoundsException ai) {
            logger.log(Level.WARNING, "OUT OF BOUNDS AT RAYCAST: " + x + ", " + y + ": " + ai.getMessage(), ai);
            logger.log(Level.WARNING, "Mesh info: " + this.mesh.getBorderStartX() + ", " + this.mesh.getBorderStartY() + ", to " + this.mesh.getBorderEndX() + ", " + this.mesh.getBorderEndY());
            logger.log(Level.WARNING, "Size of meshx=" + this.mesh.getSizex() + ", meshy=" + this.mesh.getSizey());
            throw new NoPathException("Path missed at " + x + ", " + y);
        }
        if (this.stepsTaken > this.maxSteps) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Raycaster stops searching after " + this.stepsTaken + " steps, suspecting it missed the target.");
            }
            throw new NoPathException("Probably missed target using raycaster.");
        }
        ++this.stepsTaken;
        return toReturn;
    }

    static float cbDist(PathTile a, PathTile b, float low) {
        return low * (float)(Math.abs(a.getTileX() - b.getTileX()) + Math.abs(a.getTileY() - b.getTileY()) - 1);
    }

    static float getCost(int tile) {
        if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
            return Float.MAX_VALUE;
        }
        if (Tiles.decodeHeight(tile) < 1) {
            return 3.0f;
        }
        return 1.0f;
    }

    Path astar(int startTileX, int startTileY, int endTileX, int endTileY, boolean aSurfaced) throws NoPathException {
        int pass;
        short endHeight = Tiles.decodeHeight(this.finish.getTile());
        short startHeight = Tiles.decodeHeight(this.start.getTile());
        if (this.creature != null && !this.creature.isSwimming() && !this.creature.isSubmerged() && endHeight < -this.creatureHalfHeight && Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_CAVE_EXIT.id && Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_HOLE.id && endHeight < startHeight) {
            throw new NoPathException("Target in water.");
        }
        this.pathList = new LinkedList();
        if (this.start != null && this.finish != null && this.start.equals(this.finish)) {
            return null;
        }
        if (this.finish == null) {
            if (this.creature != null) {
                logger.log(Level.WARNING, this.creature.getName() + " finish=null at " + endTileX + ", " + endTileY);
            } else {
                logger.log(Level.WARNING, "Finish=null at " + endTileX + ", " + endTileY);
            }
            return null;
        }
        if (this.start == null) {
            if (this.creature != null) {
                logger.log(Level.WARNING, this.creature.getName() + " start=null at " + startTileX + ", " + startTileY);
            } else {
                logger.log(Level.WARNING, "start=null at " + startTileX + ", " + startTileY);
            }
            return null;
        }
        this.start.setDistanceFromStart(this.start, 0.0f);
        this.pathList.add(this.start);
        int lState = 0;
        for (pass = 0; lState == 0 && pass < 10000; ++pass) {
            lState = this.step2();
        }
        if (lState == 1) {
            if (pass > 4000) {
                String cname = "Unknown";
                if (this.creature != null) {
                    cname = this.creature.getName();
                }
                logger.log(Level.INFO, cname + " pathed from " + this.startX + ", " + this.startY + " to " + this.endX + ", " + this.endY + " and found path after " + pass + " steps.");
            }
            return this.setPath();
        }
        if (lState == 2) {
            throw new NoPathException("No path possible after " + pass + " tries.");
        }
        throw new NoPathException("No path found after " + pass + " tries.");
    }

    private void setDebug(boolean aDebug) {
        this.debug = aDebug;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Debug in pathfinding - " + aDebug);
        }
    }

    int step2() {
        boolean found = false;
        float min = Float.MAX_VALUE;
        float score = 0.0f;
        PathTile best = this.pathList.get(this.pathList.size() - 1);
        PathTile now = null;
        for (int i = 0; i < this.pathList.size(); ++i) {
            now = this.pathList.get(i);
            score = now.getDistanceFromStart();
            score += PathFinder.cbDist(now, this.finish, PathFinder.getCost(now.getTile()));
            if (now.isUsed() || !(score < min)) continue;
            min = score;
            best = now;
        }
        now = best;
        this.pathList.remove(now);
        now.setUsed();
        PathTile[] next = this.mesh.getAdjacent(now);
        for (int i = 0; i < next.length; ++i) {
            if (next[i] != null && this.canPass(now, next[i])) {
                if (!this.pathList.contains(next[i]) && next[i].isNotUsed()) {
                    this.pathList.add(next[i]);
                }
                if (next[i] == this.finish) {
                    found = true;
                }
                score = now.getDistanceFromStart() + next[i].getMoveCost();
                next[i].setDistanceFromStart(now, score);
            }
            if (!found) continue;
            return 1;
        }
        if (this.pathList.isEmpty()) {
            return 2;
        }
        return 0;
    }

    boolean canPass(PathTile aStart, PathTile end) {
        if (Tiles.isSolidCave(Tiles.decodeType(end.getTile()))) {
            return false;
        }
        if (this.creature != null) {
            if (this.creature.isGhost()) {
                return true;
            }
            if (Tiles.decodeType(end.getTile()) == Tiles.Tile.TILE_LAVA.id) {
                return false;
            }
        }
        if (this.creature == null) {
            BlockingResult result;
            if (!this.ignoreWalls && (result = Blocking.getBlockerBetween(this.creature, aStart.getPositionX(), aStart.getPositionY(), end.getPositionX(), end.getPositionY(), aStart.getMinZ(), end.getMinZ(), this.surfaced, end.isSurfaced(), false, 6, -1L, -10L, -10L, false)) != null) {
                return false;
            }
        } else {
            if (this.creature.isSubmerged() && Tiles.decodeHeight(end.getTile()) > -20) {
                return false;
            }
            if (!this.ignoreWalls) {
                BlockingResult result = Blocking.getBlockerBetween(this.creature, aStart.getPositionX(), aStart.getPositionY(), end.getPositionX(), end.getPositionY(), this.creature.getPositionZ(), this.creature.getPositionZ(), this.surfaced, end.isSurfaced(), false, 6, -1L, this.creature.getBridgeId(), this.creature.getBridgeId(), this.creature.followsGround());
                if (result != null) {
                    Blocker first = result.getFirstBlocker();
                    if (first.isDoor() && first.canBeOpenedBy(this.creature, false)) {
                        if (this.creature.canOpenDoors()) {
                            end.setDoor(true);
                        }
                        return this.creature.canOpenDoors();
                    }
                    if (this.creature.getBridgeId() > 0L) {
                        try {
                            Structure bridge = Structures.getStructure(this.creature.getBridgeId());
                            if (bridge.contains(this.start.getTileX(), this.start.getTileY()) && (bridge.isHorizontal() ? end.getTileY() != this.start.getTileY() : end.getTileX() != this.start.getTileX())) {
                                return false;
                            }
                            for (Blocker blocker : result.getBlockerArray()) {
                                if (!(blocker.getCenterPoint().z > this.creature.getPositionZ())) continue;
                                return false;
                            }
                            return true;
                        }
                        catch (NoSuchStructureException nss) {
                            logger.log(Level.WARNING, this.creature.getWurmId() + " at " + this.creature.getTileX() + "," + this.creature.getTileY() + " no structure");
                        }
                    }
                    return false;
                }
                if (this.creature.getBridgeId() > 0L) {
                    try {
                        Structure bridge = Structures.getStructure(this.creature.getBridgeId());
                        if (bridge.contains(this.start.getTileX(), this.start.getTileY()) && (bridge.isHorizontal() ? end.getTileY() != this.start.getTileY() : end.getTileX() != this.start.getTileX())) {
                            return false;
                        }
                    }
                    catch (NoSuchStructureException nss) {
                        logger.log(Level.WARNING, this.creature.getWurmId() + " at " + this.creature.getTileX() + "," + this.creature.getTileY() + " no structure");
                    }
                }
            }
        }
        return true;
    }

    private PathTile findLowestDist(PathTile aStart, PathTile now) {
        return now.getLink();
    }

    Path setPath() {
        this.setDebug(this.debug);
        boolean finished = false;
        PathTile now = this.finish;
        PathTile stop = this.start;
        this.pList = new LinkedList();
        PathTile lastCurrent = now;
        while (!finished) {
            this.pList.add(now);
            PathTile next = this.findLowestDist(this.start, now);
            if (lastCurrent.equals(next)) {
                finished = true;
                logger.log(Level.WARNING, "Loop in heuristicastar.");
            }
            lastCurrent = now;
            now = next;
            if (!now.equals(stop)) continue;
            if (Math.abs(lastCurrent.getTileX() - now.getTileX()) > 1 || Math.abs(lastCurrent.getTileY() - now.getTileY()) > 1) {
                this.pList.add(now);
            }
            finished = true;
        }
        LinkedList<PathTile> inverted = new LinkedList<PathTile>();
        Iterator it = this.pList.iterator();
        while (it.hasNext()) {
            inverted.addFirst((PathTile)it.next());
        }
        Path path = new Path(inverted);
        this.setDebug(false);
        return path;
    }
}
