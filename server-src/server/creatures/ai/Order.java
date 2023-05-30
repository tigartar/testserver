package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.constants.CounterTypes;

public final class Order implements MiscConstants, CounterTypes {
   private final int tilex;
   private final int tiley;
   private final int layer;
   private final long wurmid;

   public Order(int tx, int ty, int lay) {
      this.wurmid = -10L;
      this.tilex = tx;
      this.tiley = ty;
      this.layer = lay;
   }

   public Order(long wid) {
      this.wurmid = wid;
      this.tilex = -1;
      this.tiley = -1;
      this.layer = 0;
   }

   public boolean isTile() {
      return this.tilex != -1;
   }

   private boolean isItem() {
      if (this.wurmid == -10L) {
         return false;
      } else {
         return WurmId.getType(this.wurmid) == 2 || WurmId.getType(this.wurmid) == 19 || WurmId.getType(this.wurmid) == 20;
      }
   }

   public boolean isCreature() {
      if (this.wurmid == -10L) {
         return false;
      } else {
         return WurmId.getType(this.wurmid) == 1 || WurmId.getType(this.wurmid) == 0;
      }
   }

   public boolean isResolved(int tx, int ty, int lay) {
      return tx == this.tilex && ty == this.tiley && this.layer == lay;
   }

   public boolean isResolved(long wid) {
      return wid == this.wurmid;
   }

   public Creature getCreature() {
      if (this.isCreature()) {
         try {
            return Server.getInstance().getCreature(this.wurmid);
         } catch (Exception var2) {
         }
      }

      return null;
   }

   public Item getItem() {
      if (this.isItem()) {
         try {
            return Items.getItem(this.wurmid);
         } catch (Exception var2) {
         }
      }

      return null;
   }

   public int getTileX() {
      return this.tilex;
   }

   public int getTileY() {
      return this.tiley;
   }
}
