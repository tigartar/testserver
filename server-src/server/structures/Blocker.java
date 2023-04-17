/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.creatures.Creature;

public interface Blocker {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_FENCE = 1;
    public static final int TYPE_WALL = 2;
    public static final int TYPE_FLOOR = 3;
    public static final int TYPE_ALL = 4;
    public static final int TYPE_ALL_BUT_OPEN = 5;
    public static final int TYPE_MOVEMENT = 6;
    public static final int TYPE_TARGET_TILE = 7;
    public static final int TYPE_NOT_DOOR = 8;

    public boolean isFence();

    public boolean isStone();

    public boolean isWood();

    public boolean isMetal();

    public boolean isWall();

    public boolean isDoor();

    public boolean isFloor();

    public boolean isRoof();

    public boolean isTile();

    public boolean isStair();

    public boolean canBeOpenedBy(Creature var1, boolean var2);

    public int getFloorLevel();

    public String getName();

    public Vector3f getNormal();

    public Vector3f getCenterPoint();

    public int getTileX();

    public int getTileY();

    public boolean isOnSurface();

    public float getPositionX();

    public float getPositionY();

    public boolean isHorizontal();

    public boolean isWithinFloorLevels(int var1, int var2);

    public Vector3f isBlocking(Creature var1, Vector3f var2, Vector3f var3, Vector3f var4, int var5, long var6, boolean var8);

    public float getBlockPercent(Creature var1);

    public float getDamageModifier();

    public boolean setDamage(float var1);

    public float getDamage();

    public long getId();

    public long getTempId();

    public float getMinZ();

    public float getMaxZ();

    public float getFloorZ();

    public boolean isWithinZ(float var1, float var2, boolean var3);

    public boolean isOnSouthBorder(TilePos var1);

    public boolean isOnNorthBorder(TilePos var1);

    public boolean isOnWestBorder(TilePos var1);

    public boolean isOnEastBorder(TilePos var1);
}

