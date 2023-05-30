package com.wurmonline.server.utils;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector2f;
import javax.annotation.Nonnull;

public final class CoordUtils {
   public static int WorldToTile(float woldPos) {
      return (int)woldPos >> 2;
   }

   @Nonnull
   public static TilePos WorldToTile(float woldPosX, float woldPosY) {
      return TilePos.fromXY(WorldToTile(woldPosX), WorldToTile(woldPosY));
   }

   @Nonnull
   public static TilePos WorldToTile(Vector2f woldPos) {
      return TilePos.fromXY(WorldToTile(woldPos.x), WorldToTile(woldPos.y));
   }

   public static float TileToWorld(int tilePos) {
      return (float)(tilePos << 2);
   }

   @Nonnull
   public static Vector2f TileToWorld(@Nonnull TilePos tilePos) {
      return new Vector2f(TileToWorld(tilePos.x), TileToWorld(tilePos.y));
   }

   public static float TileToWorldTileCenter(int tilePos) {
      return (float)((tilePos << 2) + 2);
   }

   @Nonnull
   public static Vector2f TileToWorldTileCenter(@Nonnull TilePos tilePos) {
      return new Vector2f(TileToWorldTileCenter(tilePos.x), TileToWorldTileCenter(tilePos.y));
   }

   public static int TileToWorldInt(int tilePos) {
      return tilePos << 2;
   }
}
