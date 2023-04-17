/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.structures.TilePoint;
import com.wurmonline.server.zones.Zones;

public class StructureBounds {
    TilePoint max;
    TilePoint min;

    StructureBounds(int minTileX, int minTileY, int maxTileX, int maxTileY) {
        this.max = new TilePoint(maxTileX, maxTileY);
        this.min = new TilePoint(minTileX, minTileY);
    }

    StructureBounds(TilePoint pMax, TilePoint pMin) {
        if (pMax.getTileX() > Zones.worldTileSizeX) {
            pMax.setTileX(Zones.worldTileSizeX);
        }
        if (pMax.getTileY() > Zones.worldTileSizeY) {
            pMax.setTileY(Zones.worldTileSizeY);
        }
        this.max = pMax;
        this.min = pMin;
    }

    public TilePoint getMax() {
        return this.max;
    }

    public TilePoint getMin() {
        return this.min;
    }
}

