/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import com.wurmonline.shared.util.StringUtilities;
import java.util.HashMap;
import java.util.Map;

public class NameCountList {
    final Map<String, Integer> localMap = new HashMap<String, Integer>();

    public void add(String name) {
        int cnt = 1;
        if (this.localMap.containsKey(name)) {
            cnt = this.localMap.get(name) + 1;
        }
        this.localMap.put(name, cnt);
    }

    public boolean isEmpty() {
        return this.localMap.isEmpty();
    }

    public String toString() {
        String line = "";
        int count = 0;
        for (Map.Entry<String, Integer> entry : this.localMap.entrySet()) {
            ++count;
            if (line.length() > 0) {
                line = count == this.localMap.size() ? line + " and " : line + ", ";
            }
            line = line + StringUtilities.getWordForNumber(entry.getValue()) + " " + entry.getKey();
        }
        return line;
    }
}

