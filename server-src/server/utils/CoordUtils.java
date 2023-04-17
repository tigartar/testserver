/*
 * Decompiled with CFR 0.152.
 */
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
        return TilePos.fromXY(CoordUtils.WorldToTile(woldPosX), CoordUtils.WorldToTile(woldPosY));
    }

    @Nonnull
    public static TilePos WorldToTile(Vector2f woldPos) {
        return TilePos.fromXY(CoordUtils.WorldToTile(woldPos.x), CoordUtils.WorldToTile(woldPos.y));
    }

    public static float TileToWorld(int tilePos) {
        return tilePos << 2;
    }

    @Nonnull
    public static Vector2f TileToWorld(@Nonnull TilePos tilePos) {
        return new Vector2f(CoordUtils.TileToWorld(tilePos.x), CoordUtils.TileToWorld(tilePos.y));
    }

    public static float TileToWorldTileCenter(int tilePos) {
        return (tilePos << 2) + 2;
    }

    @Nonnull
    public static Vector2f TileToWorldTileCenter(@Nonnull TilePos tilePos) {
        return new Vector2f(CoordUtils.TileToWorldTileCenter(tilePos.x), CoordUtils.TileToWorldTileCenter(tilePos.y));
    }

    public static int TileToWorldInt(int tilePos) {
        return tilePos << 2;
    }
}

