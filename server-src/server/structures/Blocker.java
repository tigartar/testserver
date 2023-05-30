package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.server.creatures.Creature;

public interface Blocker {
   int TYPE_NONE = 0;
   int TYPE_FENCE = 1;
   int TYPE_WALL = 2;
   int TYPE_FLOOR = 3;
   int TYPE_ALL = 4;
   int TYPE_ALL_BUT_OPEN = 5;
   int TYPE_MOVEMENT = 6;
   int TYPE_TARGET_TILE = 7;
   int TYPE_NOT_DOOR = 8;

   boolean isFence();

   boolean isStone();

   boolean isWood();

   boolean isMetal();

   boolean isWall();

   boolean isDoor();

   boolean isFloor();

   boolean isRoof();

   boolean isTile();

   boolean isStair();

   boolean canBeOpenedBy(Creature var1, boolean var2);

   int getFloorLevel();

   String getName();

   Vector3f getNormal();

   Vector3f getCenterPoint();

   int getTileX();

   int getTileY();

   boolean isOnSurface();

   float getPositionX();

   float getPositionY();

   boolean isHorizontal();

   boolean isWithinFloorLevels(int var1, int var2);

   Vector3f isBlocking(Creature var1, Vector3f var2, Vector3f var3, Vector3f var4, int var5, long var6, boolean var8);

   float getBlockPercent(Creature var1);

   float getDamageModifier();

   boolean setDamage(float var1);

   float getDamage();

   long getId();

   long getTempId();

   float getMinZ();

   float getMaxZ();

   float getFloorZ();

   boolean isWithinZ(float var1, float var2, boolean var3);

   boolean isOnSouthBorder(TilePos var1);

   boolean isOnNorthBorder(TilePos var1);

   boolean isOnWestBorder(TilePos var1);

   boolean isOnEastBorder(TilePos var1);
}
