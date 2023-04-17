/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Blocking
implements MiscConstants {
    public static final double RADS_TO_DEGS = 57.29577951308232;
    public static final float DEGS_TO_RADS = (float)Math.PI / 180;
    private static final float MAXSTEP = 3.0f;
    private static final Logger logger = Logger.getLogger(Blocking.class.getName());

    private Blocking() {
    }

    public static final BlockingResult getRangedBlockerBetween(Creature performer, Item target) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), target.getPosX(), target.getPosY(), performer.getPositionZ(), target.getPosZ(), performer.isOnSurface(), target.isOnSurface(), true, 5, target.getWurmId(), performer.getBridgeId(), target.getBridgeId(), performer.followsGround() || target.getFloorLevel() == 0 && target.getBridgeId() == -10L);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, Item target, int blockingType) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), target.getPosX(), target.getPosY(), performer.getPositionZ(), target.getPosZ(), performer.isOnSurface(), target.isOnSurface(), false, blockingType, target.getWurmId(), performer.getBridgeId(), target.getBridgeId(), performer.followsGround() || target.getFloorLevel() == 0 && target.getBridgeId() == -10L);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, Floor floor, int blockingType) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), 2 + Tiles.decodeTileX(floor.getId()) * 4, 2 + Tiles.decodeTileY(floor.getId()) * 4, performer.getPositionZ(), floor.getMinZ() - 0.25f, performer.isOnSurface(), floor.isOnSurface(), false, !performer.isOnSurface() ? 7 : blockingType, floor.getId(), performer.getBridgeId(), -10L, false);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, long target, boolean targetSurfaced, int blockingType, long sourceBridgeId, long targetBridgeId) {
        return Blocking.getBlockerBetween(performer, target, targetSurfaced, blockingType, sourceBridgeId, targetBridgeId, 0);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, long target, boolean targetSurfaced, int blockingType, long sourceBridgeId, long targetBridgeId, int ceilingHeight) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), 2 + Tiles.decodeTileX(target) * 4, 2 + Tiles.decodeTileY(target) * 4, performer.getPositionZ(), Zones.getHeightForNode(Tiles.decodeTileX(target), Tiles.decodeTileY(target), targetSurfaced ? 0 : -1) + (float)(Tiles.decodeFloorLevel(target) * 3) + (float)ceilingHeight, performer.isOnSurface(), targetSurfaced, false, !performer.isOnSurface() && blockingType != 8 ? 7 : blockingType, target, sourceBridgeId, targetBridgeId, false);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, Wall target, int blockingType) {
        float tpx = target.getPositionX();
        float tpy = target.getPositionY();
        if (target.getDir() == Tiles.TileBorderDirection.DIR_HORIZ) {
            if (tpy > (float)(performer.getTileY() * 4)) {
                tpy -= 4.0f;
            }
        } else if (tpx > (float)(performer.getTileX() * 4)) {
            tpx -= 4.0f;
        }
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), tpx, tpy, performer.getPositionZ(), target.getMinZ(), performer.isOnSurface(), target.isOnSurface(), false, blockingType, target.getId(), performer.getBridgeId(), -10L, false);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, Fence target, int blockingType) {
        float tpx = target.getPositionX();
        float tpy = target.getPositionY();
        if (target.getDir() == Tiles.TileBorderDirection.DIR_HORIZ) {
            if (tpy > (float)(performer.getTileY() * 4)) {
                tpy -= 4.0f;
            }
        } else if (tpx > (float)(performer.getTileX() * 4)) {
            tpx -= 4.0f;
        }
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), tpx, tpy, performer.getPositionZ(), target.getMinZ(), performer.isOnSurface(), target.isOnSurface(), false, blockingType, target.getId(), performer.getBridgeId(), -10L, false);
    }

    public static final BlockingResult getRangedBlockerBetween(Creature performer, Creature target) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), target.getPosX(), target.getPosY(), performer.getPositionZ() + (float)performer.getHalfHeightDecimeters() / 10.0f, target.getPositionZ() + (float)target.getHalfHeightDecimeters() / 10.0f, performer.isOnSurface(), target.isOnSurface(), true, 4, target.getWurmId(), performer.getBridgeId(), target.getBridgeId(), false);
    }

    public static final BlockingResult getBlockerBetween(Creature performer, Creature target, int blockingType) {
        return Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), target.getPosX(), target.getPosY(), performer.getPositionZ(), target.getPositionZ(), performer.isOnSurface(), target.isOnSurface(), false, blockingType, target.getWurmId(), performer.getBridgeId(), target.getBridgeId(), performer.followsGround() || target.followsGround());
    }

    public static final BlockingResult getBlockerBetween(@Nullable Creature creature, float startx, float starty, float endx, float endy, float startZ, float endZ, boolean surfaced, boolean targetSurfaced, boolean rangedAttack, int typeChecked, long target, long sourceBridgeId, long targetBridgeId, boolean followGround) {
        return Blocking.getBlockerBetween(creature, startx, starty, endx, endy, startZ, endZ, surfaced, targetSurfaced, rangedAttack, typeChecked, true, target, sourceBridgeId, targetBridgeId, followGround);
    }

    public static final boolean isSameFloorLevel(float startZ, float endZ) {
        return Math.abs(startZ - endZ) < 3.0f;
    }

    public static final BlockingResult getBlockerBetween(@Nullable Creature creature, float startx, float starty, float endx, float endy, float startZ, float endZ, boolean surfaced, boolean targetSurfaced, boolean rangedAttack, int typeChecked, boolean test, long target, long sourceBridgeId, long targetBridgeId, boolean followGround) {
        int max;
        int starttilex = Zones.safeTileX((int)startx >> 2);
        int starttiley = Zones.safeTileY((int)starty >> 2);
        int endtilex = Zones.safeTileX((int)endx >> 2);
        int endtiley = Zones.safeTileY((int)endy >> 2);
        int n = max = rangedAttack ? 100 : 50;
        if (starttilex == endtilex && starttiley == endtiley && Blocking.isSameFloorLevel(startZ, endZ)) {
            return null;
        }
        if (typeChecked == 0) {
            return null;
        }
        if (!rangedAttack && creature != null) {
            Creature targetCret;
            if (!creature.isPlayer()) {
                max = creature.getMaxHuntDistance() + 5;
            }
            if ((targetCret = creature.getTarget()) != null && targetCret.getWurmId() == target) {
                creature.sendToLoggers("Now checking " + starttilex + "," + starttiley + " to " + endtilex + "," + endtiley + " startZ=" + startZ + " endZ=" + endZ + " surf=" + surfaced + "," + targetSurfaced + " follow ground=" + followGround);
            }
        }
        int nextTileX = starttilex;
        int nextTileY = starttiley;
        int lastTileX = starttilex;
        int lastTileY = starttiley;
        boolean isTransition = false;
        if (creature != null && !creature.isOnSurface()) {
            isTransition = false;
            if (Tiles.decodeType(Server.caveMesh.getTile(starttilex, starttiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                isTransition = true;
            }
            if (creature.isPlayer() && typeChecked != 6) {
                int actualStartY;
                Vector3f actualStart = ((Player)creature).getActualPosVehicle();
                int actualStartX = Zones.safeTileX((int)actualStart.x >> 2);
                int tile = Server.caveMesh.getTile(actualStartX, actualStartY = Zones.safeTileX((int)actualStart.y >> 2));
                if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                    BlockingResult toReturn = new BlockingResult();
                    PathTile blocker = new PathTile(actualStartX, actualStartY, tile, false, -1);
                    toReturn.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                    return toReturn;
                }
            }
        }
        Vector3f startPos = new Vector3f(startx, starty, startZ + 0.5f);
        Vector3f endPos = new Vector3f(endx, endy, endZ + 0.5f);
        Vector3f lastPos = new Vector3f(startPos);
        Vector3f nextPos = new Vector3f(startPos);
        Vector3f dir = new Vector3f(endPos.subtract(startPos)).normalize();
        BlockingResult result = null;
        boolean found = false;
        int debugChecks = 0;
        while (!found) {
            Vector3f remain = endPos.subtract(lastPos);
            if (remain.length() < 3.0f) {
                if (debugChecks++ > 60) {
                    found = true;
                } else if (remain.length() == 0.0f) {
                    found = true;
                }
                nextPos.addLocal(remain);
            } else {
                nextPos.addLocal(dir.mult(3.0f));
                if (debugChecks++ > 60) {
                    if (creature != null) {
                        logger.log(Level.INFO, creature.getName() + " checking " + 3.0f + " meters failed. Checks=" + debugChecks);
                    }
                    found = true;
                }
            }
            lastTileY = nextTileY;
            lastTileX = nextTileX;
            nextTileX = (int)nextPos.x >> 2;
            nextTileY = (int)nextPos.y >> 2;
            int diffX = nextTileX - lastTileX;
            int diffY = nextTileY - lastTileY;
            if (diffX == 0 && diffY == 0) {
                nextPos.z = endPos.z;
                lastPos.z = startPos.z;
            }
            if (diffX != 0 || diffY != 0 || !Blocking.isSameFloorLevel(lastPos.z, nextPos.z)) {
                int y;
                int x;
                if (!(surfaced || isTransition && targetSurfaced || typeChecked == 1 || typeChecked == 2 || typeChecked == 3)) {
                    int t = Server.caveMesh.getTile(endtilex, endtiley);
                    if (Tiles.isSolidCave(Tiles.decodeType(t)) && typeChecked == 6) {
                        result = new BlockingResult();
                        PathTile blocker = new PathTile(endtilex, endtiley, t, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        return result;
                    }
                    result = Blocking.isDiagonalRockBetween(creature, lastTileX, lastTileY, nextTileX, nextTileY);
                    if (result == null) {
                        result = Blocking.isStraightRockBetween(creature, lastTileX, lastTileY, nextTileX, nextTileY);
                    }
                    if (result != null) {
                        if (typeChecked != 7 && typeChecked != 8 || result.getFirstBlocker().getTileX() != endtilex || result.getFirstBlocker().getTileY() != endtiley) {
                            return result;
                        }
                        result = null;
                    }
                }
                VolaTile checkedTile = null;
                if (diffX >= 0 && diffY >= 0) {
                    for (x = Math.min(0, diffX); x <= diffX; ++x) {
                        for (y = Math.min(0, diffY); y <= diffY; ++y) {
                            checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                            if (checkedTile != null) {
                                result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                            }
                            if (surfaced == targetSurfaced || (checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced)) == null) continue;
                            result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                        }
                    }
                }
                if (diffX < 0 && diffY >= 0) {
                    for (x = 0; x >= diffX; --x) {
                        for (y = Math.min(0, diffY); y <= diffY; ++y) {
                            checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                            if (checkedTile != null) {
                                result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                            }
                            if (surfaced == targetSurfaced || (checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced)) == null) continue;
                            result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                        }
                    }
                }
                if (diffX >= 0 && diffY < 0) {
                    for (x = Math.min(0, diffX); x <= diffX; ++x) {
                        for (y = 0; y >= diffY; --y) {
                            checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                            if (checkedTile != null) {
                                result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                            }
                            if (surfaced == targetSurfaced || (checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced)) == null) continue;
                            result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                        }
                    }
                }
                if (diffX < 0 && diffY < 0) {
                    for (x = 0; x >= diffX; --x) {
                        for (y = 0; y >= diffY; --y) {
                            checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, surfaced);
                            if (checkedTile != null) {
                                result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                            }
                            if (surfaced == targetSurfaced || (checkedTile = Zones.getTileOrNull(lastTileX + x, lastTileY + y, targetSurfaced)) == null) continue;
                            result = Blocking.returnIterativeCheck(checkedTile, result, creature, dir, lastPos, nextPos, rangedAttack, starttilex, nextTileX, starttiley, nextTileY, typeChecked, target, sourceBridgeId, targetBridgeId, followGround);
                        }
                    }
                }
            }
            lastPos.set(nextPos);
            if (found) {
                return result;
            }
            if (Math.abs(nextTileX - starttilex) <= max && Math.abs(nextTileY - starttiley) <= max) continue;
            return result;
        }
        return result;
    }

    private static final BlockingResult returnIterativeCheck(VolaTile checkedTile, BlockingResult result, Creature creature, Vector3f dir, Vector3f lastPos, Vector3f nextPos, boolean rangedAttack, int startTileX, int nextTileX, int startTileY, int nextTileY, int typeChecked, long targetId, long sourceBridgeId, long targetBridgeId, boolean followGround) {
        TimeConstants[] blockers = null;
        Vector3f toCheck = lastPos.clone();
        if ((typeChecked == 4 || typeChecked == 2 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) && (result = Blocking.checkForResult(creature, result, (Blocker[])(blockers = checkedTile.getWalls()), dir, toCheck, nextPos, rangedAttack, startTileX, nextTileX, startTileY, nextTileY, typeChecked, targetId, sourceBridgeId, targetBridgeId, followGround)) != null && result.getTotalCover() >= 100.0f) {
            return result;
        }
        if ((typeChecked == 4 || typeChecked == 1 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) && (result = Blocking.checkForResult(creature, result, (Blocker[])(blockers = checkedTile.getFences()), dir, toCheck = lastPos.clone(), nextPos, rangedAttack, startTileX, nextTileX, startTileY, nextTileY, typeChecked, targetId, sourceBridgeId, targetBridgeId, followGround)) != null && result.getTotalCover() >= 100.0f) {
            return result;
        }
        if (typeChecked == 4 || typeChecked == 3 || typeChecked == 5 || typeChecked == 6 || typeChecked == 7) {
            blockers = checkedTile.getFloors();
            toCheck = lastPos.clone();
            result = Blocking.checkForResult(creature, result, (Blocker[])blockers, dir, toCheck, nextPos, rangedAttack, startTileX, nextTileX, startTileY, nextTileY, typeChecked, targetId, sourceBridgeId, targetBridgeId, followGround);
            blockers = checkedTile.getBridgeParts();
            if ((result = Blocking.checkForResult(creature, result, (Blocker[])blockers, dir, toCheck = lastPos.clone(), nextPos, rangedAttack, startTileX, nextTileX, startTileY, nextTileY, typeChecked, targetId, sourceBridgeId, targetBridgeId, followGround)) != null && result.getTotalCover() >= 100.0f) {
                return result;
            }
        }
        return result;
    }

    private static final BlockingResult checkForResult(Creature creature, BlockingResult result, Blocker[] blockers, Vector3f dir, Vector3f startPos, Vector3f endPos, boolean rangedAttack, int starttilex, int currTileX, int starttiley, int currTileY, int blockType, long target, long sourceBridgeId, long targetBridgeId, boolean followGround) {
        for (int w = 0; w < blockers.length; ++w) {
            Vector3f intersection;
            BridgePart bp;
            if (!blockers[w].isWithinZ(Math.max(startPos.z, endPos.z), Math.min(startPos.z, endPos.z), followGround)) continue;
            boolean skip = false;
            if (blockers[w] instanceof BridgePart && (bp = (BridgePart)blockers[w]).getStructureId() == sourceBridgeId && (sourceBridgeId == targetBridgeId || blockType == 6)) {
                skip = true;
            }
            if (skip || (intersection = blockers[w].isBlocking(creature, startPos, endPos, dir, blockType, target, followGround)) == null) continue;
            if (result == null) {
                result = new BlockingResult();
            }
            if (!rangedAttack && blockType != 5) {
                result.addBlocker(blockers[w], intersection, 100.0f);
                return result;
            }
            float addedCover = blockers[w].getBlockPercent(creature);
            if (Math.abs(starttilex - currTileX) > 1 || Math.abs(starttiley - currTileY) > 1 || addedCover >= 100.0f) {
                if (addedCover >= 100.0f) {
                    result.addBlocker(blockers[w], intersection, 100.0f);
                    return result;
                }
                if (!(result.addBlocker(blockers[w], intersection, addedCover) >= 100.0f)) continue;
                return result;
            }
            if (!(result.addBlocker(blockers[w], intersection, addedCover) >= 100.0f)) continue;
            return result;
        }
        return result;
    }

    public static final BlockingResult isDiagonalRockBetween(Creature creature, int startx, int starty, int endx, int endy) {
        if (startx != endx && endy != starty) {
            int northTile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1));
            int southTile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1));
            int westTile = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty));
            int eastTile = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty));
            if (endx < startx) {
                if (endy < starty) {
                    if (Tiles.isSolidCave(Tiles.decodeType(westTile)) && Tiles.isSolidCave(Tiles.decodeType(northTile))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), westTile, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), northTile, false, -1);
                        result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0f);
                        return result;
                    }
                    int nw = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty - 1));
                    if (Tiles.isSolidCave(Tiles.decodeType(nw))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty - 1), nw, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        return result;
                    }
                }
                if (endy > starty) {
                    if (Tiles.isSolidCave(Tiles.decodeType(westTile)) && Tiles.isSolidCave(Tiles.decodeType(southTile))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), westTile, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), southTile, false, -1);
                        result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0f);
                        return result;
                    }
                    int sw = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty + 1));
                    if (Tiles.isSolidCave(Tiles.decodeType(sw))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty + 1), sw, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        return result;
                    }
                }
            } else {
                if (endy < starty) {
                    if (Tiles.isSolidCave(Tiles.decodeType(eastTile)) && Tiles.isSolidCave(Tiles.decodeType(northTile))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), eastTile, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), northTile, false, -1);
                        result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0f);
                        return result;
                    }
                    int ne = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty - 1));
                    if (Tiles.isSolidCave(Tiles.decodeType(ne))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty - 1), ne, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        return result;
                    }
                }
                if (endy > starty) {
                    if (Tiles.isSolidCave(Tiles.decodeType(eastTile)) && Tiles.isSolidCave(Tiles.decodeType(southTile))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), eastTile, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        PathTile blocker2 = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), southTile, false, -1);
                        result.addBlocker(blocker2, blocker2.getCenterPoint(), 100.0f);
                        return result;
                    }
                    int se = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty + 1));
                    if (Tiles.isSolidCave(Tiles.decodeType(se))) {
                        BlockingResult result = new BlockingResult();
                        PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty + 1), se, false, -1);
                        result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public static final BlockingResult isStraightRockBetween(Creature creature, int startx, int starty, int endx, int endy) {
        if (startx == endx || endy == starty) {
            int tile;
            if (endx < startx) {
                int tile2;
                if (endy == starty && Tiles.isSolidCave(Tiles.decodeType(tile2 = Server.caveMesh.getTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty))))) {
                    BlockingResult result = new BlockingResult();
                    PathTile blocker = new PathTile(Zones.safeTileX(startx - 1), Zones.safeTileY(starty), tile2, false, -1);
                    result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                    return result;
                }
            } else if (endx > startx) {
                int tile3;
                if (endy == starty && Tiles.isSolidCave(Tiles.decodeType(tile3 = Server.caveMesh.getTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty))))) {
                    BlockingResult result = new BlockingResult();
                    PathTile blocker = new PathTile(Zones.safeTileX(startx + 1), Zones.safeTileY(starty), tile3, false, -1);
                    result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                    return result;
                }
            } else if (endy > starty) {
                int tile4;
                if (endx == startx && Tiles.isSolidCave(Tiles.decodeType(tile4 = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1))))) {
                    BlockingResult result = new BlockingResult();
                    PathTile blocker = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty + 1), tile4, false, -1);
                    result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                    return result;
                }
            } else if (endy < starty && endx == startx && Tiles.isSolidCave(Tiles.decodeType(tile = Server.caveMesh.getTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1))))) {
                BlockingResult result = new BlockingResult();
                PathTile blocker = new PathTile(Zones.safeTileX(startx), Zones.safeTileY(starty - 1), tile, false, -1);
                result.addBlocker(blocker, blocker.getCenterPoint(), 100.0f);
                return result;
            }
        }
        return null;
    }
}

