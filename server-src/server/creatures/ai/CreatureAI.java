/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public abstract class CreatureAI {
    private static final Logger logger = Logger.getLogger(CreatureAI.class.getName());
    private static final boolean DETAILED_TIME_LOG = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void simpleMovementTick(Creature c) {
        long start = System.nanoTime();
        int tileX = -1;
        int tileY = -1;
        if (Server.rand.nextInt(100) < 5) {
            tileX = c.getTileX() + (Server.rand.nextInt(4) - 2);
            tileY = c.getTileY() + (Server.rand.nextInt(4) - 2);
        }
        this.moveTowardsTile(c, tileX, tileY, true);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void pathedMovementTick(Creature c) {
        long start = System.nanoTime();
        Path p = c.getStatus().getPath();
        if (p != null && !p.isEmpty()) {
            PathTile nextTile = p.getFirst();
            if (nextTile.hasSpecificPos()) {
                float aiDataMoveModifier;
                c.turnTowardsPoint(nextTile.getPosX(), nextTile.getPosY());
                this.creatureMovementTick(c, true);
                float diffX = c.getStatus().getPositionX() - nextTile.getPosX();
                float diffY = c.getStatus().getPositionY() - nextTile.getPosY();
                double totalDist = Math.sqrt(diffX * diffX + diffY * diffY);
                float lMod = c.getMoveModifier((c.isOnSurface() ? Server.surfaceMesh : Server.caveMesh).getTile((int)c.getStatus().getPositionX() >> 2, (int)c.getStatus().getPositionY() >> 2));
                float f = aiDataMoveModifier = c.getCreatureAIData() != null ? c.getCreatureAIData().getMovementSpeedModifier() : 1.0f;
                if (totalDist <= (double)(c.getSpeed() * lMod * aiDataMoveModifier)) {
                    p.removeFirst();
                }
            } else {
                if (nextTile.getTileX() == c.getTileX() && nextTile.getTileY() == c.getTileY()) {
                    p.removeFirst();
                    if (p.isEmpty()) {
                        return;
                    }
                    nextTile = p.getFirst();
                }
                this.moveTowardsTile(c, nextTile.getTileX(), nextTile.getTileY(), true);
                if (nextTile.getTileX() == c.getTileX() && nextTile.getTileY() == c.getTileY() || c.getTarget() != null && c.getPos2f().distance(c.getTarget().getPos2f()) < 4.0f) {
                    p.removeFirst();
                }
            }
        }
    }

    protected void moveTowardsTile(Creature c, int tileX, int tileY, boolean moveToTarget) {
        if (c.getTarget() != null && moveToTarget) {
            c.turnTowardsCreature(c.getTarget());
        } else if (tileX > 0 && tileY > 0) {
            c.turnTowardsTile((short)tileX, (short)tileY);
        }
        this.creatureMovementTick(c, true);
    }

    protected void creatureMovementTick(Creature c, boolean rotateFromBlocker) {
        float lPosX = c.getStatus().getPositionX();
        float lPosY = c.getStatus().getPositionY();
        float lPosZ = c.getStatus().getPositionZ();
        float lRotation = c.getStatus().getRotation();
        float oldPosX = lPosX;
        float oldPosY = lPosY;
        float oldPosZ = lPosZ;
        int lOldTileX = (int)lPosX >> 2;
        int lOldTileY = (int)lPosY >> 2;
        float lMoveModifier = !c.isOnSurface() ? c.getMoveModifier(Server.caveMesh.getTile(lOldTileX, lOldTileY)) : c.getMoveModifier(Server.surfaceMesh.getTile(lOldTileX, lOldTileY));
        float aiDataMoveModifier = c.getCreatureAIData() != null ? c.getCreatureAIData().getMovementSpeedModifier() : 1.0f;
        float lXPosMod = (float)Math.sin(lRotation * ((float)Math.PI / 180)) * c.getSpeed() * lMoveModifier * aiDataMoveModifier;
        float lYPosMod = -((float)Math.cos(lRotation * ((float)Math.PI / 180))) * c.getSpeed() * lMoveModifier * aiDataMoveModifier;
        int lNewTileX = (int)(lPosX + lXPosMod) >> 2;
        int lNewTileY = (int)(lPosY + lYPosMod) >> 2;
        int lDiffTileX = lNewTileX - lOldTileX;
        int lDiffTileY = lNewTileY - lOldTileY;
        long newBridgeId = c.getBridgeId();
        if (lDiffTileX != 0 || lDiffTileY != 0) {
            if (!c.isOnSurface()) {
                if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lOldTileX, lOldTileY)))) {
                    logger.log(Level.INFO, "Destroying creature " + c.getName() + " due to being inside rock.");
                    c.die(false, "Suffocating in rock (3)");
                    return;
                }
                if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(lNewTileX, lNewTileY)))) {
                    if (c.getCurrentTile().isTransition()) {
                        if (!Tiles.isMineDoor(Tiles.decodeType(Server.caveMesh.getTile(c.getTileX(), c.getTileY()))) || MineDoorPermission.getPermission(c.getTileX(), c.getTileY()).mayPass(c)) {
                            c.setLayer(0, true);
                        } else if (rotateFromBlocker) {
                            c.rotateRandom(lRotation, 45);
                        }
                        return;
                    }
                    if (rotateFromBlocker) {
                        c.rotateRandom(lRotation, 45);
                    }
                    return;
                }
            } else if (Tiles.Tile.TILE_LAVA.id == Tiles.decodeType(Server.surfaceMesh.getTile(lNewTileX, lNewTileY))) {
                if (rotateFromBlocker) {
                    c.rotateRandom(lRotation, 45);
                }
                return;
            }
            VolaTile t = Zones.getOrCreateTile(lNewTileX, lNewTileY, c.isOnSurface());
            if (!c.isHuman() && t.isGuarded() && !c.getCurrentTile().isGuarded() || c.isAnimal() && t.hasFire()) {
                if (rotateFromBlocker) {
                    c.rotateRandom(lRotation, 100);
                }
                return;
            }
            newBridgeId = this.getNewBridgeId(lOldTileX, lOldTileY, c.isOnSurface(), c.getBridgeId(), c.getFloorLevel(), lNewTileX, lNewTileY);
            BlockingResult result = Blocking.getBlockerBetween(c, lPosX, lPosY, lPosX + lXPosMod, lPosY + lYPosMod, c.getPositionZ(), c.getPositionZ(), c.isOnSurface(), c.isOnSurface(), false, 6, -1L, c.getBridgeId(), newBridgeId, c.followsGround());
            if (!(result == null || c.isWagoner() || (c.isKingdomGuard() || c.isSpiritGuard()) && result.getFirstBlocker().isDoor())) {
                if (rotateFromBlocker) {
                    c.rotateRandom(lRotation, 100);
                }
                return;
            }
        }
        lPosY += lYPosMod;
        if ((lPosX += lXPosMod) >= (float)(Zones.worldTileSizeX - 1 << 2) || lPosX < 0.0f || lPosY < 0.0f || lPosY >= (float)(Zones.worldTileSizeY - 1 << 2)) {
            c.destroy();
            return;
        }
        VolaTile vt = Zones.getOrCreateTile(lNewTileX, lNewTileY, c.isOnSurface());
        lPosZ = Zones.calculatePosZ(lPosX, lPosY, vt, c.isOnSurface(), c.isFloating(), c.getPositionZ(), c, newBridgeId);
        if (c.isFloating()) {
            lPosZ = Math.max(c.getTemplate().offZ, lPosZ);
        }
        if ((double)lPosZ < 0.5) {
            if ((lPosZ > -2.0f || oldPosZ <= -2.0f) && (oldPosZ < 0.0f || c.getTarget() != null) && c.isSwimming()) {
                lPosZ = Math.max(-1.25f, lPosZ);
                if (c.isFloating()) {
                    lPosZ = Math.max(c.getTemplate().offZ, lPosZ);
                }
            } else if ((double)lPosZ < -0.7 && !c.isSubmerged()) {
                if (rotateFromBlocker) {
                    c.rotateRandom(lRotation, 100);
                }
                if (c.getTarget() != null) {
                    c.setTarget(-10L, true);
                }
                return;
            }
        }
        c.getStatus().setPositionX(lPosX);
        c.getStatus().setPositionY(lPosY);
        c.getStatus().setRotation(lRotation);
        if (Structure.isGroundFloorAtPosition(lPosX, lPosY, c.isOnSurface())) {
            c.getStatus().setPositionZ(lPosZ + 0.25f);
        } else {
            c.getStatus().setPositionZ(lPosZ);
        }
        c.moved(lPosX - oldPosX, lPosY - oldPosY, lPosZ - oldPosZ, lDiffTileX, lDiffTileY);
        if (c.getTarget() != null && c.getTarget().getCurrentTile() == c.getCurrentTile() && c.getTarget().getFloorLevel() != c.getFloorLevel()) {
            if (c.isSpiritGuard()) {
                c.pushToFloorLevel(c.getTarget().getFloorLevel());
            } else {
                Floor[] currentTileFloors;
                for (Floor f : currentTileFloors = c.getCurrentTile().getFloors()) {
                    if (c.getTarget().getFloorLevel() > c.getFloorLevel()) {
                        if (f.getFloorLevel() != c.getFloorLevel() + 1 || !f.isOpening() && !f.isStair()) continue;
                        c.pushToFloorLevel(f.getFloorLevel());
                        continue;
                    }
                    if (f.getFloorLevel() != c.getFloorLevel() - 1 || !f.isOpening() && !f.isStair()) continue;
                    c.pushToFloorLevel(f.getFloorLevel());
                }
            }
        }
    }

    private long getNewBridgeId(int oldTileX, int oldTileY, boolean onSurface, long oldBridgeId, int floorLevel, int newTileX, int newTileY) {
        if (oldBridgeId == -10L) {
            BridgePart bp = Zones.getBridgePartFor(newTileX, newTileY, onSurface);
            if (bp != null && bp.isFinished() && bp.hasAnExit()) {
                if (newTileY < oldTileY && bp.getSouthExitFloorLevel() == floorLevel) {
                    return bp.getStructureId();
                }
                if (newTileX > oldTileX && bp.getWestExitFloorLevel() == floorLevel) {
                    return bp.getStructureId();
                }
                if (newTileY > oldTileY && bp.getNorthExitFloorLevel() == floorLevel) {
                    return bp.getStructureId();
                }
                if (newTileX < oldTileX && bp.getEastExitFloorLevel() == floorLevel) {
                    return bp.getStructureId();
                }
            }
        } else {
            BridgePart bp = Zones.getBridgePartFor(newTileX, newTileY, onSurface);
            if (bp == null) {
                return -10L;
            }
        }
        return oldBridgeId;
    }

    protected PathTile getMovementTarget(Creature c, int tilePosX, int tilePosY) {
        tilePosX = Zones.safeTileX(tilePosX);
        tilePosY = Zones.safeTileY(tilePosY);
        if (!c.isOnSurface()) {
            int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
            if (!Tiles.isSolidCave(Tiles.decodeType(tile)) && (Tiles.decodeHeight(tile) > -c.getHalfHeightDecimeters() || c.isSwimming() || c.isSubmerged())) {
                return new PathTile(tilePosX, tilePosY, tile, c.isOnSurface(), -1);
            }
        } else {
            int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
            if (Tiles.decodeHeight(tile) > -c.getHalfHeightDecimeters() || c.isSwimming() || c.isSubmerged()) {
                return new PathTile(tilePosX, tilePosY, tile, c.isOnSurface(), c.getFloorLevel());
            }
        }
        return null;
    }

    public boolean pollCreature(Creature c, long delta) {
        long start = System.nanoTime();
        boolean isDead = false;
        if (c.getSpellEffects() != null) {
            c.getSpellEffects().poll();
        }
        if (this.pollSpecialPreAttack(c, delta)) {
            return true;
        }
        isDead = this.pollAttack(c, delta);
        if (c.getActions().poll(c)) {
            if (c.isFighting()) {
                c.setFighting();
            } else {
                if (isDead) {
                    return true;
                }
                if (this.pollSpecialPreBreeding(c, delta)) {
                    return true;
                }
                isDead = this.pollBreeding(c, delta);
                if (isDead) {
                    return true;
                }
                if (this.pollSpecialPreMovement(c, delta)) {
                    return true;
                }
                isDead = this.pollMovement(c, delta);
                if (isDead) {
                    return true;
                }
                if (System.currentTimeMillis() - c.lastSavedPos > 3600000L) {
                    c.lastSavedPos = System.currentTimeMillis() + (long)(Server.rand.nextInt(3600) * 1000);
                    try {
                        c.savePosition(c.getStatus().getZoneId());
                        c.getStatus().save();
                    }
                    catch (IOException e) {
                        logger.warning("Unable to save creature position, creature id: " + c.getWurmId() + " reason: " + e.getMessage());
                    }
                }
            }
        }
        if (c.getDamageCounter() > 0) {
            c.setDamageCounter((short)(c.getDamageCounter() - 1));
            isDead = this.pollDamageCounter(c, delta, c.getDamageCounter());
        }
        if (isDead) {
            return true;
        }
        if (this.pollSpecialPreItems(c, delta)) {
            return true;
        }
        this.pollItems(c, delta);
        if (this.pollSpecialPreMisc(c, delta)) {
            return true;
        }
        isDead = this.pollMisc(c, delta);
        if (isDead) {
            return true;
        }
        isDead = this.pollSpecialFinal(c, delta);
        return isDead;
    }

    public int woundDamageChanged(Creature c, int damageToAdd) {
        return damageToAdd;
    }

    public double causedWound(Creature c, @Nullable Creature target, byte dmgType, int dmgPosition, float armourMod, double damage) {
        return damage;
    }

    public double receivedWound(Creature c, @Nullable Creature performer, byte dmgType, int dmgPosition, float armourMod, double damage) {
        return damage;
    }

    public boolean creatureDied(Creature creature) {
        return false;
    }

    public boolean maybeAttackCreature(Creature c, VirtualZone vz, Creature mover) {
        return false;
    }

    protected abstract boolean pollMovement(Creature var1, long var2);

    protected abstract boolean pollAttack(Creature var1, long var2);

    protected abstract boolean pollBreeding(Creature var1, long var2);

    protected boolean pollSpecialPreBreeding(Creature c, long delta) {
        return false;
    }

    protected boolean pollSpecialPreMovement(Creature c, long delta) {
        return false;
    }

    protected boolean pollSpecialPreAttack(Creature c, long delta) {
        return false;
    }

    protected boolean pollSpecialPreItems(Creature c, long delta) {
        return false;
    }

    protected boolean pollSpecialPreMisc(Creature c, long delta) {
        return false;
    }

    protected boolean pollSpecialFinal(Creature c, long delta) {
        return false;
    }

    protected boolean pollDamageCounter(Creature c, long delta, short damageCounter) {
        if (damageCounter == 0) {
            c.removeWoundMod();
        }
        return false;
    }

    protected void pollItems(Creature c, long delta) {
        c.pollItems();
        if (c.getBody() != null) {
            c.getBody().poll();
        }
    }

    protected boolean pollMisc(Creature c, long delta) {
        c.pollStamina();
        c.pollFavor();
        c.pollLoyalty();
        c.trimAttackers(false);
        c.numattackers = 0;
        c.hasAddedToAttack = false;
        if (!c.isUnique() && !c.isFighting() && c.isDominated() && c.goOffline) {
            logger.log(Level.INFO, c.getName() + " going offline.");
            Creatures.getInstance().setCreatureOffline(c);
            c.goOffline = false;
            return true;
        }
        return false;
    }

    protected boolean isTimerReady(Creature c, int timerId, long minTime) {
        return c.getCreatureAIData().getTimer(timerId) >= minTime;
    }

    protected void increaseTimer(Creature c, long delta, int ... timerIds) {
        for (int id : timerIds) {
            c.getCreatureAIData().setTimer(id, c.getCreatureAIData().getTimer(id) + delta);
        }
    }

    protected void resetTimer(Creature c, int ... timerIds) {
        for (int timerId : timerIds) {
            c.getCreatureAIData().setTimer(timerId, 0L);
        }
    }

    public abstract CreatureAIData createCreatureAIData();

    public abstract void creatureCreated(Creature var1);
}

