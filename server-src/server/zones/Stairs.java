package com.wurmonline.server.zones;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Stairs {
   public static final Map<Integer, Set<Integer>> stairTiles = new ConcurrentHashMap<>();

   private Stairs() {
   }

   public static final void addStair(int volatileId, int floorLevel) {
      Set<Integer> stairSet = stairTiles.get(volatileId);
      if (stairSet == null) {
         stairSet = new HashSet<>();
      }

      stairSet.add(floorLevel);
      stairTiles.put(volatileId, stairSet);
   }

   public static final boolean hasStair(int volatileId, int floorLevel) {
      Set<Integer> stairSet = stairTiles.get(volatileId);
      return stairSet == null ? false : stairSet.contains(floorLevel);
   }

   public static final void removeStair(int volatileId, int floorLevel) {
      Set<Integer> stairSet = stairTiles.get(volatileId);
      if (stairSet != null) {
         stairSet.remove(floorLevel);
      }
   }
}
