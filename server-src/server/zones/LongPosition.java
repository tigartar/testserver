/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.Server;
import com.wurmonline.shared.constants.EffectConstants;
import java.util.Random;

public final class LongPosition
implements EffectConstants {
    private final long id;
    private final int tilex;
    private final int tiley;
    private final short effectType;

    LongPosition(long _id, int _tilex, int _tiley) {
        this.id = _id;
        this.tilex = _tilex;
        this.tiley = _tiley;
        this.effectType = LongPosition.getRandomEffectType(Server.rand);
    }

    static short getRandomEffectType(Random randomSource) {
        return (short)(5 + randomSource.nextInt(5));
    }

    long getId() {
        return this.id;
    }

    public int getTilex() {
        return this.tilex;
    }

    public int getTiley() {
        return this.tiley;
    }

    short getEffectType() {
        return this.effectType;
    }
}

