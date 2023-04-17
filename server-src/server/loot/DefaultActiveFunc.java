/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.loot.ActiveFunc;

public class DefaultActiveFunc
implements ActiveFunc {
    @Override
    public boolean active(Creature victim, Creature receiver) {
        return true;
    }
}

