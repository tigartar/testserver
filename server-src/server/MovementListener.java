/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.NoSuchCreatureException;

public interface MovementListener {
    public void creatureMoved(long var1, float var3, float var4, float var5, int var6, int var7) throws NoSuchCreatureException, NoSuchPlayerException;
}

