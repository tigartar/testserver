/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.CreatureMove;

public class MovementEntity {
    private long wurmid;
    private long creatorId;
    private CreatureMove movePosition;
    private final long expireTime;

    public MovementEntity(long creatorWurmId, long _expireTime) {
        this.setWurmid(WurmId.getNextIllusionId());
        this.setCreatorId(creatorWurmId);
        this.expireTime = _expireTime;
    }

    public boolean shouldExpire() {
        return System.currentTimeMillis() > this.expireTime;
    }

    public final long getWurmid() {
        return this.wurmid;
    }

    private final void setWurmid(long aWurmid) {
        this.wurmid = aWurmid;
    }

    public CreatureMove getMovePosition() {
        return this.movePosition;
    }

    public void setMovePosition(CreatureMove aMovePosition) {
        this.movePosition = aMovePosition;
    }

    public final long getCreatorId() {
        return this.creatorId;
    }

    private final void setCreatorId(long aCreatorId) {
        this.creatorId = aCreatorId;
    }

    public final void checkIfChangeDirection() {
        if (Server.rand.nextInt(10) == 0) {
            this.movePosition.diffX = (byte)(-3 + Server.rand.nextInt(7));
            this.movePosition.diffY = (byte)(-3 + Server.rand.nextInt(7));
        }
    }
}

