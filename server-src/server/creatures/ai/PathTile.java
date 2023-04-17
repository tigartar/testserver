/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.zones.Zones;

public class PathTile
implements Blocker,
MiscConstants {
    protected int tilex;
    protected int tiley;
    private float posX;
    private float posY;
    private final int tile;
    private int floorLevel;
    private boolean used = false;
    private final boolean surfaced;
    private boolean door = false;
    private final float moveCost;
    private float distFromStart = Float.MAX_VALUE;
    private PathTile linkedFrom;
    private static final Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);

    public PathTile(int tx, int ty, int t, boolean surf, int aFloorLevel) {
        this(tx, ty, t, surf, PathFinder.getCost(t), aFloorLevel);
    }

    public PathTile(float posX, float posY, int tile, boolean surface, int floorLevel) {
        this((int)posX >> 2, (int)posY >> 2, tile, surface, PathFinder.getCost(tile), floorLevel, posX, posY);
    }

    public PathTile(int aTileX, int aTileY, int aTile, boolean aSurface, float aMoveCost, int aFloorLevel) {
        this(aTileX, aTileY, aTile, aSurface, aMoveCost, aFloorLevel, -1.0f, -1.0f);
    }

    PathTile(int aTileX, int aTileY, int aTile, boolean aSurface, float aMoveCost, int aFloorLevel, float posX, float posY) {
        this.tilex = aTileX;
        this.tiley = aTileY;
        this.tile = aTile;
        this.surfaced = aSurface;
        this.moveCost = aMoveCost;
        this.floorLevel = aFloorLevel;
        this.posX = posX;
        this.posY = posY;
    }

    public final void setFloorLevel(int aFloorLevel) {
        this.floorLevel = aFloorLevel;
    }

    float getDistanceFromStart() {
        return this.distFromStart;
    }

    PathTile getLink() {
        return this.linkedFrom;
    }

    void setDistanceFromStart(PathTile link, float val) {
        if (val < this.distFromStart) {
            this.linkedFrom = link;
            this.distFromStart = val;
        }
    }

    float getMoveCost() {
        return this.moveCost;
    }

    @Override
    public int getTileX() {
        return this.tilex;
    }

    @Override
    public int getTileY() {
        return this.tiley;
    }

    public float getPosX() {
        return this.posX;
    }

    public float getPosY() {
        return this.posY;
    }

    public boolean hasSpecificPos() {
        return this.posX > 0.0f && this.posY > 0.0f;
    }

    public void setSpecificPos(float posX, float posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public boolean isSurfaced() {
        return this.surfaced;
    }

    @Override
    public boolean isOnSurface() {
        return this.surfaced;
    }

    public int getTile() {
        return this.tile;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.surfaced ? 1231 : 1237);
        result = 31 * result + this.tilex;
        result = 31 * result + this.tiley;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PathTile)) {
            return false;
        }
        PathTile other = (PathTile)obj;
        if (this.surfaced != other.surfaced) {
            return false;
        }
        if (this.tilex != other.tilex) {
            return false;
        }
        return this.tiley == other.tiley;
    }

    public String toString() {
        return "x=" + this.tilex + ", y=" + this.tiley + ", surf=" + this.surfaced + " fl=" + this.floorLevel;
    }

    @Override
    public boolean isDoor() {
        return this.door;
    }

    void setDoor(boolean isdoor) {
        this.door = isdoor;
    }

    boolean isUsed() {
        return this.used;
    }

    boolean isNotUsed() {
        return !this.used;
    }

    void setUsed() {
        this.used = true;
    }

    @Override
    public int getFloorLevel() {
        return this.floorLevel;
    }

    @Override
    public final String getName() {
        return Tiles.getTile(Tiles.decodeType(this.tile)).getName();
    }

    @Override
    public final float getPositionX() {
        if (this.hasSpecificPos()) {
            return this.getPosX();
        }
        return 2 + this.tilex * 4;
    }

    @Override
    public final float getPositionY() {
        if (this.hasSpecificPos()) {
            return this.getPosY();
        }
        return 2 + this.tiley * 4;
    }

    @Override
    public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel) {
        int lFloorLevel = this.surfaced ? 0 : -1;
        return lFloorLevel >= minFloorLevel && lFloorLevel <= maxFloorLevel;
    }

    @Override
    public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f aNormal, int blockType, long target, boolean followGround) {
        if (this.surfaced) {
            return null;
        }
        if (Tiles.isSolidCave(Tiles.decodeType(this.tile))) {
            return new Vector3f(this.getPositionX(), this.getPositionY(), Tiles.decodeHeightAsFloat(this.tile));
        }
        return null;
    }

    @Override
    public final float getBlockPercent(Creature creature) {
        if (this.surfaced) {
            return 0.0f;
        }
        if (Tiles.isSolidCave(Tiles.decodeType(this.tile))) {
            return 100.0f;
        }
        return 0.0f;
    }

    @Override
    public boolean isFence() {
        return false;
    }

    @Override
    public boolean isWall() {
        return false;
    }

    @Override
    public boolean isFloor() {
        return false;
    }

    @Override
    public boolean isRoof() {
        return false;
    }

    @Override
    public Vector3f getNormal() {
        return normal;
    }

    @Override
    public Vector3f getCenterPoint() {
        return new Vector3f(this.getPositionX(), this.getPositionY(), Tiles.decodeHeightAsFloat(this.tile));
    }

    @Override
    public boolean isHorizontal() {
        return false;
    }

    @Override
    public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor) {
        return true;
    }

    @Override
    public final boolean isStone() {
        return Tiles.isSolidCave(Tiles.decodeType(this.tile)) || Tiles.decodeType(this.tile) == Tiles.Tile.TILE_ROCK.id || Tiles.decodeType(this.tile) == Tiles.Tile.TILE_STONE_SLABS.id;
    }

    @Override
    public final boolean isWood() {
        return Tiles.isTree(Tiles.decodeType(this.tile));
    }

    @Override
    public final boolean isMetal() {
        return Tiles.isOreCave(Tiles.decodeType(this.tile));
    }

    @Override
    public final boolean isTile() {
        return true;
    }

    @Override
    public float getFloorZ() {
        return this.floorLevel * 3;
    }

    @Override
    public float getMinZ() {
        return Zones.getHeightForNode(this.tilex, this.tiley, this.isOnSurface() ? 0 : -1) + this.getFloorZ();
    }

    @Override
    public float getMaxZ() {
        return this.getMinZ() + 4.0f;
    }

    @Override
    public boolean isWithinZ(float maxZ, float minZ, boolean followGround) {
        return this.getFloorLevel() <= 0 && followGround || minZ <= this.getMaxZ() && maxZ >= this.getMinZ();
    }

    @Override
    public final boolean setDamage(float newDamage) {
        return false;
    }

    @Override
    public final float getDamage() {
        return 0.0f;
    }

    @Override
    public final float getDamageModifier() {
        return 0.0f;
    }

    @Override
    public final long getId() {
        return Tiles.getTileId(this.getTileX(), this.getTileY(), this.getFloorLevel() * 30);
    }

    @Override
    public long getTempId() {
        return -10L;
    }

    @Override
    public final boolean isStair() {
        return false;
    }

    @Override
    public final boolean isOnSouthBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnNorthBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnWestBorder(TilePos pos) {
        return false;
    }

    @Override
    public final boolean isOnEastBorder(TilePos pos) {
        return false;
    }
}

