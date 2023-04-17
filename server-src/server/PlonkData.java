/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.creatures.Creature;

public enum PlonkData {
    FIRST_DAMAGE(1, 12),
    LOW_STAMINA(2, 13),
    THIRSTY(3, 15),
    HUNGRY(4, 14),
    FALL_DAMAGE(9, 16),
    DEATH(11, 18),
    SWIMMING(12, 17),
    ENCUMBERED(13, 20),
    ON_A_BOAT(14, 19),
    TREE_ACTIONS(16, 22),
    BOAT_SECURITY(48, 23);

    private final short plonkId;
    private final int flagBit;
    private final int flagColumn;

    private PlonkData(short _plonkId, int _flagBit, int _flagColumn) {
        this.plonkId = _plonkId;
        this.flagBit = _flagBit;
        this.flagColumn = _flagColumn;
    }

    private PlonkData(short _plonkId, int _flagBit) {
        this(_plonkId, _flagBit, 0);
    }

    public final short getPlonkId() {
        return this.plonkId;
    }

    public final int getFlagBit() {
        return this.flagBit;
    }

    public final int getFlagColumn() {
        return this.flagColumn;
    }

    public void trigger(Creature player) {
        if (player.isPlayer() && !player.hasFlag(this.getFlagBit())) {
            player.getCommunicator().sendPlonk(this.getPlonkId());
            player.setFlag(this.getFlagBit(), true);
        }
    }

    public final boolean hasSeenThis(Creature player) {
        if (player.isPlayer()) {
            return player.hasFlag(this.getFlagBit());
        }
        return true;
    }
}

