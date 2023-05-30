package com.wurmonline.server.zones;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class Encounter {
   private final Map<Integer, Integer> types = new HashMap<>();

   public void addType(int creatureTemplateId, int nums) {
      this.types.put(creatureTemplateId, nums);
   }

   public Map<Integer, Integer> getTypes() {
      return this.types;
   }

   @Override
   public final String toString() {
      String toRet = "";

      for(Entry<Integer, Integer> entry : this.types.entrySet()) {
         toRet = toRet + "Type " + entry.getKey() + " Numbers=" + entry.getValue() + ", ";
      }

      return toRet;
   }
}
