package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;

public final class Referer implements MiscConstants {
   public static final byte R_TYPE_NOTHANDLED = 0;
   public static final byte R_TYPE_MONEY = 1;
   static final byte R_TYPE_TIME = 2;
   private final long wurmid;
   private final long referer;
   private boolean money = false;
   private boolean handled = false;

   Referer(long aWurmid, long aReferer) {
      this.wurmid = aWurmid;
      this.referer = aReferer;
   }

   Referer(long aWurmid, long aReferer, boolean aMoney, boolean aHandled) {
      this.wurmid = aWurmid;
      this.referer = aReferer;
      this.money = aMoney;
      this.handled = aHandled;
   }

   long getWurmid() {
      return this.wurmid;
   }

   long getReferer() {
      return this.referer;
   }

   boolean isMoney() {
      return this.money;
   }

   void setMoney(boolean aMoney) {
      this.money = aMoney;
   }

   boolean isHandled() {
      return this.handled;
   }

   void setHandled(boolean aHandled) {
      this.handled = aHandled;
   }
}
