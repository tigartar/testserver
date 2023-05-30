package com.wurmonline.server.behaviours;

import com.wurmonline.server.items.NoSuchTemplateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BuildAllMaterials {
   private final List<BuildStageMaterials> bsms = new ArrayList<>();

   public void add(BuildStageMaterials bms) {
      this.bsms.add(bms);
   }

   public List<BuildStageMaterials> getBuildStageMaterials() {
      return this.bsms;
   }

   public BuildStageMaterials getBuildStageMaterials(byte stage) {
      return this.bsms.get(Math.max(0, stage));
   }

   public int getStageCount() {
      return this.bsms.size();
   }

   public List<BuildMaterial> getCurrentRequiredMaterials() {
      Iterator var1 = this.bsms.iterator();
      if (var1.hasNext()) {
         BuildStageMaterials bsm = (BuildStageMaterials)var1.next();
         return bsm.getRequiredMaterials();
      } else {
         return new ArrayList<>();
      }
   }

   public String getStageCountAsString() {
      switch(this.bsms.size()) {
         case 1:
            return "one";
         case 2:
            return "two";
         case 3:
            return "three";
         case 4:
            return "four";
         case 5:
            return "five";
         case 6:
            return "six";
         case 7:
            return "seven";
         default:
            return "" + this.bsms.size();
      }
   }

   public void setNeeded(byte currentStage, int done) {
      for(int stage = 0; stage < this.bsms.size(); ++stage) {
         if (currentStage > stage) {
            this.getBuildStageMaterials((byte)stage).setNoneNeeded();
         } else if (currentStage == stage) {
            this.getBuildStageMaterials((byte)stage).reduceNeededBy(done);
         } else {
            this.getBuildStageMaterials((byte)stage).setMaxNeeded();
         }
      }
   }

   public List<BuildMaterial> getTotalMaterialsNeeded() {
      BuildStageMaterials all = this.getTotalMaterialsRequired();
      return all.getBuildMaterials();
   }

   private BuildStageMaterials getTotalMaterialsRequired() {
      Map<Integer, Integer> mats = new HashMap<>();

      for(BuildStageMaterials bsm : this.bsms) {
         for(BuildMaterial bm : bsm.getBuildMaterials()) {
            int qty = bm.getNeededQuantity();
            if (qty > 0) {
               Integer key = bm.getTemplateId();
               if (mats.containsKey(key)) {
                  qty += mats.get(key);
               }

               mats.put(key, qty);
            }
         }
      }

      BuildStageMaterials all = new BuildStageMaterials("All");

      for(Entry<Integer, Integer> entry : mats.entrySet()) {
         try {
            all.add(entry.getKey(), entry.getValue());
         } catch (NoSuchTemplateException var8) {
         }
      }

      return all;
   }

   public BuildAllMaterials getRemainingMaterialsNeeded() {
      BuildAllMaterials toReturn = new BuildAllMaterials();

      for(BuildStageMaterials bsm : this.bsms) {
         if (!bsm.isStageComplete()) {
            toReturn.add(bsm);
         }
      }

      return toReturn;
   }

   public String getRequiredMaterialString(boolean detailed) {
      BuildStageMaterials all = this.getTotalMaterialsRequired();
      return all.getRequiredMaterialString(detailed);
   }

   public int getTotalQuantityRequired() {
      int count = 0;

      for(BuildStageMaterials bsm : this.bsms) {
         count += bsm.getTotalQuantityRequired();
      }

      return count;
   }

   public int getTotalQuantityDone() {
      int count = 0;

      for(BuildStageMaterials bsm : this.bsms) {
         count += bsm.getTotalQuantityDone();
      }

      return count;
   }
}
