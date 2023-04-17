/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Stairs {
    public static final Map<Integer, Set<Integer>> stairTiles = new ConcurrentHashMap<Integer, Set<Integer>>();

    private Stairs() {
    }

    public static final void addStair(int volatileId, int floorLevel) {
        Set<Integer> stairSet = stairTiles.get(volatileId);
        if (stairSet == null) {
            stairSet = new HashSet<Integer>();
        }
        stairSet.add(floorLevel);
        stairTiles.put(volatileId, stairSet);
    }

    public static final boolean hasStair(int volatileId, int floorLevel) {
        Set<Integer> stairSet = stairTiles.get(volatileId);
        if (stairSet == null) {
            return false;
        }
        return stairSet.contains(floorLevel);
    }

    public static final void removeStair(int volatileId, int floorLevel) {
        Set<Integer> stairSet = stairTiles.get(volatileId);
        if (stairSet == null) {
            return;
        }
        stairSet.remove(floorLevel);
    }
}

