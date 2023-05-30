package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum StructureTypeEnum {
   SOLID((byte)0, "wall", ""),
   WINDOW((byte)1, "window", "window"),
   DOOR((byte)2, "door", "door"),
   DOUBLE_DOOR((byte)3, "double door", "doubledoor"),
   ARCHED((byte)4, "arched", "arched"),
   NARROW_WINDOW((byte)5, "narrow window", "windownarrow"),
   PORTCULLIS((byte)6, "portcullis", "portcullis"),
   BARRED((byte)7, "barred", "bars1"),
   RUBBLE((byte)8, "rubble", "rubble"),
   BALCONY((byte)9, "balcony", "balcony"),
   JETTY((byte)10, "jetty", "jetty"),
   ORIEL((byte)11, "oriel", "oriel2"),
   CANOPY_DOOR((byte)12, "canopy", "canopy"),
   WIDE_WINDOW((byte)13, "wide window", "widewindow"),
   ARCHED_LEFT((byte)14, "left arch", "archleft"),
   ARCHED_RIGHT((byte)15, "right arch", "archright"),
   ARCHED_T((byte)16, "T arch", "archt"),
   SCAFFOLDING((byte)17, "scaffolding", "scaffolding"),
   FENCE((byte)0, "fence", ""),
   PARAPET((byte)0, "parapet", "parapet"),
   PALISADE((byte)0, "palisade", "palisade"),
   FENCE_WALL((byte)0, "stone wall", ""),
   GATE((byte)0, "gate", "gate"),
   FENCE_TALL((byte)0, "tall wall", ""),
   WOVEN((byte)0, "woven", ""),
   NO_WALL((byte)0, "missing wall", ""),
   FENCE_IRON_BARS((byte)0, "iron fence", ""),
   FENCE_IRON_BARS_GATE((byte)0, "iron fence gate", ""),
   FENCE_IRON_BARS_TALL((byte)0, "tall iron fence", ""),
   FENCE_IRON_BARS_TALL_GATE((byte)0, "tall iron fence gate", ""),
   ROPE_LOW((byte)0, "low rope fence", ""),
   ROPE_HIGH((byte)0, "tall rope fence", ""),
   GARDESGARD_LOW((byte)0, "low roundpole fence", ""),
   GARDESGARD_HIGH((byte)0, "tall roundpole fence", ""),
   GARDESGARD_GATE((byte)0, "roundpole fence gate", ""),
   CURB((byte)0, "curb", ""),
   HEDGE_LOW((byte)0, "", ""),
   HEDGE_MEDIUM((byte)0, "", ""),
   HEDGE_HIGH((byte)0, "", ""),
   MAGIC_FENCE((byte)0, "", ""),
   FLOWERBED((byte)0, "", ""),
   MEDIUM_CHAIN((byte)0, "", ""),
   SIEGWALL((byte)0, "", ""),
   FENCE_PLAN_WOODEN((byte)0, "", ""),
   FENCE_PLAN_WOODEN_GATE((byte)0, "", ""),
   FENCE_PLAN_PALISADE((byte)0, "", ""),
   FENCE_PLAN_PALISADE_GATE((byte)0, "", ""),
   FENCE_PLAN_STONEWALL((byte)0, "", ""),
   FENCE_PLAN_STONEWALL_HIGH((byte)0, "", ""),
   FENCE_PLAN_IRON_BARS((byte)0, "", ""),
   FENCE_PLAN_IRON_BARS_GATE((byte)0, "", ""),
   FENCE_PLAN_IRON_BARS_TALL((byte)0, "", ""),
   FENCE_PLAN_IRON_BARS_TALL_GATE((byte)0, "", ""),
   FENCE_PLAN_STONE_PARAPET((byte)0, "", ""),
   FENCE_PLAN_WOODEN_PARAPET((byte)0, "", ""),
   FENCE_PLAN_IRON_BARS_PARAPET((byte)0, "", ""),
   FENCE_PLAN_CRUDE((byte)0, "", ""),
   FENCE_PLAN_CRUDE_GATE((byte)0, "", ""),
   FENCE_PLAN_WOVEN((byte)0, "", ""),
   FENCE_PLAN_ROPE_LOW((byte)0, "", ""),
   FENCE_PLAN_ROPE_HIGH((byte)0, "", ""),
   FENCE_PLAN_CURB((byte)0, "", ""),
   FENCE_PLAN_GARDESGARD_LOW((byte)0, "", ""),
   FENCE_PLAN_GARDESGARD_HIGH((byte)0, "", ""),
   FENCE_PLAN_GARDESGARD_GATE((byte)0, "", ""),
   FENCE_PLAN_STONE_FENCE((byte)0, "", ""),
   FENCE_PLAN_MEDIUM_CHAIN((byte)0, "", ""),
   FENCE_PLAN_PORTCULLIS((byte)0, "", ""),
   HOUSE_PLAN_SOLID((byte)0, "", ""),
   HOUSE_PLAN_DOOR((byte)0, "", ""),
   HOUSE_PLAN_DOUBLE_DOOR((byte)0, "", ""),
   HOUSE_PLAN_WINDOW((byte)0, "", ""),
   HOUSE_PLAN_BARRED((byte)0, "", ""),
   HOUSE_PLAN_ORIEL((byte)0, "", ""),
   HOUSE_PLAN_ARCHED((byte)0, "", ""),
   HOUSE_PLAN_ARCH_LEFT((byte)0, "", ""),
   HOUSE_PLAN_ARCH_RIGHT((byte)0, "", ""),
   HOUSE_PLAN_ARCH_T((byte)0, "", ""),
   HOUSE_PLAN_PORTCULLIS((byte)0, "", ""),
   HOUSE_PLAN_NARROW_WINDOW((byte)0, "", ""),
   HOUSE_PLAN_BALCONY((byte)0, "", ""),
   HOUSE_PLAN_JETTY((byte)0, "", ""),
   HOUSE_PLAN_CANOPY((byte)0, "", ""),
   PLAN((byte)127, "plan", "plan");

   public final String typeName;
   public final String modelShortName;
   public final byte value;
   private static HashMap<String, StructureTypeEnum> lookupMap = null;

   private StructureTypeEnum(byte _value, String _typeName, String _modelShortName) {
      this.value = _value;
      this.typeName = _typeName;
      this.modelShortName = _modelShortName;
   }

   public static StructureTypeEnum getTypeByINDEX(int id) {
      if (id >= 0 && id <= values().length) {
         return values()[id];
      } else {
         if (id != 127 && id != -1) {
            Logger.getGlobal().warning("Value not a valid array position: " + id + " RETURNING PLAN(VAL=40)!");
         }

         return PLAN;
      }
   }

   public static Optional<StructureTypeEnum> lookup(String name) {
      Optional<StructureTypeEnum> optional = Optional.empty();
      if (lookupMap == null) {
         lookupMap = new HashMap<>();

         for(StructureTypeEnum stt : values()) {
            lookupMap.put(stt.name(), stt);
         }
      }

      StructureTypeEnum temp = lookupMap.get(name);
      if (temp != null) {
         optional = Optional.of(temp);
      } else if (Logger.getGlobal().isLoggable(Level.FINE)) {
         Logger.getGlobal().fine(name + " not found in lookup!");
      }

      return optional;
   }
}
