/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import javax.annotation.Nullable;

public class HighwayPos {
    private int tilex;
    private int tiley;
    private boolean onSurface;
    private BridgePart bridgePart;
    private Floor floor;

    public HighwayPos(int tilex, int tiley, boolean onSurface, @Nullable BridgePart bridgePart, @Nullable Floor floor) {
        this.tilex = tilex;
        this.tiley = tiley;
        this.onSurface = onSurface;
        this.bridgePart = bridgePart;
        this.floor = floor;
    }

    public int getTilex() {
        return this.tilex;
    }

    public int getTiley() {
        return this.tiley;
    }

    public boolean isOnSurface() {
        return this.onSurface;
    }

    public boolean isSurfaceTile() {
        return this.onSurface && this.bridgePart == null && this.floor == null;
    }

    public boolean isCaveTile() {
        return !this.onSurface && this.bridgePart == null && this.floor == null;
    }

    @Nullable
    public BridgePart getBridgePart() {
        return this.bridgePart;
    }

    @Nullable
    public Floor getFloor() {
        return this.floor;
    }

    public long getBridgeId() {
        if (this.bridgePart == null) {
            return -10L;
        }
        return this.bridgePart.getStructureId();
    }

    public int getFloorLevel() {
        if (this.floor == null) {
            return 0;
        }
        return this.floor.getFloorLevel();
    }

    public void setX(int tilex) {
        this.tilex = tilex;
    }

    public void setY(int tiley) {
        this.tiley = tiley;
    }

    public void setOnSurface(boolean onSurface) {
        this.onSurface = onSurface;
    }

    public void setBridgePart(@Nullable BridgePart bridgePart) {
        this.bridgePart = bridgePart;
    }

    public void setFloor(@Nullable Floor floor) {
        this.floor = floor;
    }
}

