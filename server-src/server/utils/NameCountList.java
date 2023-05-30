package com.wurmonline.server.utils;

import com.wurmonline.shared.util.StringUtilities;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class NameCountList {
   final Map<String, Integer> localMap = new HashMap<>();

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

   @Override
   public String toString() {
      String line = "";
      int count = 0;

      for(Entry<String, Integer> entry : this.localMap.entrySet()) {
         ++count;
         if (line.length() > 0) {
            if (count == this.localMap.size()) {
               line = line + " and ";
            } else {
               line = line + ", ";
            }
         }

         line = line + StringUtilities.getWordForNumber(entry.getValue()) + " " + (String)entry.getKey();
      }

      return line;
   }
}
