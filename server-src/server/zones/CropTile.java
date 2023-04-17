/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

public class CropTile
implements Comparable<CropTile> {
    private int data;
    private int x;
    private int y;
    private int cropType;
    private boolean onSurface;

    public CropTile(int tileData, int tileX, int tileY, int typeOfCrop, boolean surface) {
        this.data = tileData;
        this.x = tileX;
        this.y = tileY;
        this.cropType = typeOfCrop;
        this.onSurface = surface;
    }

    public final int getData() {
        return this.data;
    }

    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getCropType() {
        return this.cropType;
    }

    public final boolean isOnSurface() {
        return this.onSurface;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CropTile)) {
            return false;
        }
        CropTile c = (CropTile)obj;
        return c.getCropType() == this.getCropType() && c.getData() == this.getData() && c.getX() == this.getX() && c.getY() == this.getY() && c.isOnSurface() == this.isOnSurface();
    }

    @Override
    public int compareTo(CropTile o) {
        boolean EQUAL = false;
        boolean AFTER = true;
        int BEFORE = -1;
        if (o.equals(this)) {
            return 0;
        }
        if (o.getCropType() > this.getCropType()) {
            return 1;
        }
        return -1;
    }
}

