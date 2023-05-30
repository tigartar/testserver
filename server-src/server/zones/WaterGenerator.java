package com.wurmonline.server.zones;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.TempItem;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class WaterGenerator implements Comparable<WaterGenerator> {
   private static final Logger logger = Logger.getLogger(WaterGenerator.class.getName());
   public final int x;
   public final int y;
   public final int layer;
   private int height;
   private int lastHeight;
   private int resetHeight;
   private boolean changed = true;
   private boolean spring = false;
   private Map<Integer, WaterGenerator> waterPointsY;
   private boolean createItem = true;
   private static final Map<Integer, WaterGenerator> waterPointsX = new HashMap<>();
   private Item waterMarker;
   private boolean isReset = false;
   private static ItemTemplate template;

   public WaterGenerator(int tx, int ty, boolean isSpring, int tlayer, int waterHeight) {
      this.x = tx;
      this.y = ty;
      this.layer = tlayer;
      this.spring = isSpring;
      this.height = waterHeight;
      if (!this.spring) {
         this.putInMatrix(this);
      }
   }

   public final void setSpring(boolean isSpring) {
      this.spring = isSpring;
   }

   public WaterGenerator(int tx, int ty, int tlayer, int waterHeight) {
      this.x = tx;
      this.y = ty;
      this.layer = tlayer;
      this.height = waterHeight;
      this.lastHeight = this.height;
      this.putInMatrix(this);
   }

   public final boolean shouldCreateItem() {
      return this.createItem;
   }

   public final void createItem() {
      try {
         this.waterMarker = new TempItem(
            "" + this.height,
            template,
            99.0F,
            (float)(this.x * 4 + 2),
            (float)(this.y * 4 + 2),
            Zones.calculateHeight((float)(this.x * 4 + 2), (float)(this.y * 4 + 2), true),
            1.0F,
            -10L,
            ""
         );
         Zones.getZone(this.waterMarker.getTileX(), this.waterMarker.getTileY(), true).addItem(this.waterMarker);
      } catch (Exception var2) {
      }

      this.createItem = false;
   }

   private final void putInMatrix(WaterGenerator wg) {
      if (getXGeneral(wg.x) == null) {
         addXGeneral(wg);
         this.addY(wg);
      } else {
         WaterGenerator general = getXGeneral(wg.x);
         general.addY(wg);
      }
   }

   public static final WaterGenerator getXGeneral(int aX) {
      return waterPointsX.get(aX);
   }

   public final WaterGenerator getY(int aY) {
      return this.waterPointsY.get(aY);
   }

   public static final void addXGeneral(WaterGenerator wg) {
      waterPointsX.put(wg.x, wg);
      wg.generateXMap();
   }

   public final void generateXMap() {
      this.waterPointsY = new ConcurrentHashMap<>();
   }

   public final void addY(WaterGenerator wg) {
      this.waterPointsY.put(wg.y, wg);
   }

   public static final WaterGenerator getWG(int x, int y) {
      WaterGenerator xgeneral = getXGeneral(x);
      return xgeneral == null ? null : xgeneral.getY(y);
   }

   public final int getHeight() {
      return this.height;
   }

   public final boolean changed() {
      return this.changed;
   }

   public final boolean changedSinceReset() {
      return this.changed && this.height != this.resetHeight;
   }

   public final void setHeight(int aHeight) {
      if (this.lastHeight != aHeight) {
         this.changed = true;
         this.isReset = false;
         this.lastHeight = aHeight;
         this.height = aHeight;
      }
   }

   public final void updateItem() {
      if (this.changed) {
         if (this.shouldCreateItem()) {
            this.createItem();
         } else if (this.height == 0 && !this.spring) {
            this.deleteItem();
            if (this.waterPointsY != null) {
               this.waterPointsY.remove(this.y);
               waterPointsX.remove(this.x);
               if (this.waterPointsY.size() > 0) {
                  WaterGenerator[] gens = this.waterPointsY.values().toArray(new WaterGenerator[this.waterPointsY.size()]);
                  addXGeneral(gens[0]);
                  gens[0].addWaterPointsY(this.waterPointsY);
               }
            }
         } else {
            this.waterMarker.setName("" + this.height);
            this.waterMarker.updateIfGroundItem();
         }
      }
   }

   public final void addWaterPointsY(Map<Integer, WaterGenerator> wpy) {
      this.waterPointsY = wpy;
   }

   public final void deleteItem() {
      try {
         Items.destroyItem(this.waterMarker.getWurmId());
      } catch (Exception var2) {
      }
   }

   long getTileId() {
      return Tiles.getTileId(this.x, this.y, 0, this.layer >= 0);
   }

   public int compareTo(WaterGenerator o) {
      return o.x + o.y + o.layer + o.height - this.x + this.y + this.layer + this.height;
   }

   public boolean isReset() {
      return this.isReset;
   }

   public void setReset(boolean aIsReset) {
      this.isReset = aIsReset;
      this.resetHeight = this.height;
   }

   static {
      try {
         template = ItemTemplateFactory.getInstance().getTemplate(845);
      } catch (Exception var1) {
      }
   }
}
