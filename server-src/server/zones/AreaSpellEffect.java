package com.wurmonline.server.zones;

import com.wurmonline.server.MiscConstants;
import java.util.HashMap;
import java.util.Map;

public final class AreaSpellEffect implements MiscConstants {
   private final int tilex;
   private final int tiley;
   private final int layer;
   private final byte type;
   private final long expireTime;
   private final long creator;
   private final float power;
   private final long id;
   private final int floorLevel;
   private final int heightOffset;
   private static final Map<Long, AreaSpellEffect> LAYER_0 = new HashMap<>();
   private static final Map<Long, AreaSpellEffect> LAYER_MINI = new HashMap<>();

   public AreaSpellEffect(
      long _creator, int _tilex, int _tiley, int _layer, byte _type, long _expireTime, float _power, int _floorLevel, int _heightOffset, boolean loop
   ) {
      this.tilex = _tilex;
      this.tiley = _tiley;
      this.layer = _layer;
      this.type = _type;
      this.expireTime = _expireTime;
      this.power = _power;
      this.floorLevel = _floorLevel;
      this.id = calculateId(this.tilex, this.tiley);
      this.creator = _creator;
      this.heightOffset = _heightOffset;
      addToMap(this);
      addToWorld(this, loop);
   }

   public int getFloorLevel() {
      return this.floorLevel;
   }

   int getTilex() {
      return this.tilex;
   }

   int getTiley() {
      return this.tiley;
   }

   int getHeightOffset() {
      return this.heightOffset;
   }

   int getLayer() {
      return this.layer;
   }

   public byte getType() {
      return this.type;
   }

   public long getCreator() {
      return this.creator;
   }

   public float getPower() {
      return this.power;
   }

   public long getExpireTime() {
      return this.expireTime;
   }

   public long getId() {
      return this.id;
   }

   private static void addToMap(AreaSpellEffect sp) {
      switch(sp.layer) {
         case -1:
            LAYER_MINI.put(sp.id, sp);
            break;
         case 0:
            LAYER_0.put(sp.id, sp);
            break;
         default:
            LAYER_0.put(sp.id, sp);
      }
   }

   public static void addToWorld(AreaSpellEffect sp, boolean loop) {
      VolaTile vt = Zones.getOrCreateTile(sp.tilex, sp.tiley, sp.layer >= 0);
      vt.sendAddTileEffect(sp, loop);
   }

   private static Map<Long, AreaSpellEffect> getMap(int layer) {
      switch(layer) {
         case -1:
            return LAYER_MINI;
         case 0:
            return LAYER_0;
         default:
            return LAYER_0;
      }
   }

   private static long calculateId(int tileX, int tileY) {
      return (long)((tileX << 16) + tileY);
   }

   public static void pollEffects() {
      pollEffects(LAYER_0, 0);
      pollEffects(LAYER_MINI, -1);
   }

   private static void pollEffects(Map<Long, AreaSpellEffect> map, int layer) {
      AreaSpellEffect[] eff = map.values().toArray(new AreaSpellEffect[map.size()]);
      long now = System.currentTimeMillis();

      for(int as = 0; as < eff.length; ++as) {
         if (eff[as].expireTime < now) {
            map.remove(eff[as].getId());
            VolaTile vt = Zones.getOrCreateTile(eff[as].tilex, eff[as].tiley, layer >= 0);
            vt.sendRemoveTileEffect(eff[as]);
         }
      }
   }

   public static void removeAreaEffect(int tilex, int tiley, int layer) {
      Map<Long, AreaSpellEffect> map = getMap(layer);
      AreaSpellEffect sp = map.remove(calculateId(tilex, tiley));
      if (sp != null) {
         VolaTile vt = Zones.getOrCreateTile(sp.tilex, sp.tiley, layer >= 0);
         vt.sendRemoveTileEffect(sp);
      }
   }

   public static AreaSpellEffect getEffect(int tilex, int tiley, int layer) {
      Map<Long, AreaSpellEffect> map = getMap(layer);
      return map != null ? map.get(calculateId(tilex, tiley)) : null;
   }
}
