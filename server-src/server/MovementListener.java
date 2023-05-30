package com.wurmonline.server;

import com.wurmonline.server.creatures.NoSuchCreatureException;

public interface MovementListener {
   void creatureMoved(long var1, float var3, float var4, float var5, int var6, int var7) throws NoSuchCreatureException, NoSuchPlayerException;
}
