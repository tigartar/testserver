/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import java.util.HashMap;
import java.util.Map;

public final class Encounter {
    private final Map<Integer, Integer> types = new HashMap<Integer, Integer>();

    public void addType(int creatureTemplateId, int nums) {
        this.types.put(creatureTemplateId, nums);
    }

    public Map<Integer, Integer> getTypes() {
        return this.types;
    }

    public final String toString() {
        String toRet = "";
        for (Map.Entry<Integer, Integer> entry : this.types.entrySet()) {
            toRet = toRet + "Type " + entry.getKey() + " Numbers=" + entry.getValue() + ", ";
        }
        return toRet;
    }
}

