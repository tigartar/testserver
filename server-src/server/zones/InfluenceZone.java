/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.GenericZone;

public class InfluenceZone
extends GenericZone {
    public InfluenceZone(Item i) {
        super(i);
    }

    @Override
    public float getStrengthForTile(int tileX, int tileY, boolean surfaced) {
        if (this.getZoneItem() == null) {
            return 0.0f;
        }
        int xDiff = Math.abs(this.getZoneItem().getTileX() - tileX);
        int yDiff = Math.abs(this.getZoneItem().getTileY() - tileY);
        return this.getCurrentQL() - (float)Math.max(xDiff, yDiff);
    }

    @Override
    public void updateZone() {
        if (this.getZoneItem() == null) {
            this.setCachedQL(0.0f);
            return;
        }
        int dist = (int)this.getZoneItem().getCurrentQualityLevel();
        this.setBounds(this.getZoneItem().getTileX() - dist, this.getZoneItem().getTileY() - dist, this.getZoneItem().getTileX() + dist, this.getZoneItem().getTileY() + dist);
        this.setCachedQL(this.getZoneItem().getCurrentQualityLevel());
    }

    @Override
    protected float getCurrentQL() {
        if (this.getZoneItem() == null) {
            return 0.0f;
        }
        return this.getZoneItem().getCurrentQualityLevel();
    }
}

