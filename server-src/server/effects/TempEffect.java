package com.wurmonline.server.effects;

import java.io.IOException;

public class TempEffect extends Effect {
   public TempEffect(long aOwner, short aType, float aPosX, float aPosY, float aPosZ, boolean aSurfaced) {
      super(aOwner, aType, aPosX, aPosY, aPosZ, aSurfaced);
   }

   public TempEffect(int num, long ownerid, short typ, float posx, float posy, float posz, long stime) {
      super(num, ownerid, typ, posx, posy, posz, stime);
   }

   public TempEffect(long aOwner, int aNumber) throws IOException {
      super(aOwner, aNumber);
   }

   @Override
   public void save() throws IOException {
   }

   @Override
   void load() throws IOException {
   }

   @Override
   void delete() {
   }
}
