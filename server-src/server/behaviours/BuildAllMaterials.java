/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.behaviours.BuildStageMaterials;
import com.wurmonline.server.items.NoSuchTemplateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BuildAllMaterials {
    private final List<BuildStageMaterials> bsms = new ArrayList<BuildStageMaterials>();

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
        Iterator<BuildStageMaterials> iterator = this.bsms.iterator();
        if (iterator.hasNext()) {
            BuildStageMaterials bsm = iterator.next();
            return bsm.getRequiredMaterials();
        }
        return new ArrayList<BuildMaterial>();
    }

    public String getStageCountAsString() {
        switch (this.bsms.size()) {
            case 1: {
                return "one";
            }
            case 2: {
                return "two";
            }
            case 3: {
                return "three";
            }
            case 4: {
                return "four";
            }
            case 5: {
                return "five";
            }
            case 6: {
                return "six";
            }
            case 7: {
                return "seven";
            }
        }
        return "" + this.bsms.size();
    }

    public void setNeeded(byte currentStage, int done) {
        for (int stage = 0; stage < this.bsms.size(); ++stage) {
            if (currentStage > stage) {
                this.getBuildStageMaterials((byte)stage).setNoneNeeded();
                continue;
            }
            if (currentStage == stage) {
                this.getBuildStageMaterials((byte)stage).reduceNeededBy(done);
                continue;
            }
            this.getBuildStageMaterials((byte)stage).setMaxNeeded();
        }
    }

    public List<BuildMaterial> getTotalMaterialsNeeded() {
        BuildStageMaterials all = this.getTotalMaterialsRequired();
        return all.getBuildMaterials();
    }

    private BuildStageMaterials getTotalMaterialsRequired() {
        HashMap<Integer, Integer> mats = new HashMap<Integer, Integer>();
        for (BuildStageMaterials bsm : this.bsms) {
            for (BuildMaterial bm : bsm.getBuildMaterials()) {
                int qty = bm.getNeededQuantity();
                if (qty <= 0) continue;
                Integer key = bm.getTemplateId();
                if (mats.containsKey(key)) {
                    qty += ((Integer)mats.get(key)).intValue();
                }
                mats.put(key, qty);
            }
        }
        BuildStageMaterials all = new BuildStageMaterials("All");
        for (Map.Entry entry : mats.entrySet()) {
            try {
                all.add((Integer)entry.getKey(), (Integer)entry.getValue());
            }
            catch (NoSuchTemplateException noSuchTemplateException) {}
        }
        return all;
    }

    public BuildAllMaterials getRemainingMaterialsNeeded() {
        BuildAllMaterials toReturn = new BuildAllMaterials();
        for (BuildStageMaterials bsm : this.bsms) {
            if (bsm.isStageComplete()) continue;
            toReturn.add(bsm);
        }
        return toReturn;
    }

    public String getRequiredMaterialString(boolean detailed) {
        BuildStageMaterials all = this.getTotalMaterialsRequired();
        return all.getRequiredMaterialString(detailed);
    }

    public int getTotalQuantityRequired() {
        int count = 0;
        for (BuildStageMaterials bsm : this.bsms) {
            count += bsm.getTotalQuantityRequired();
        }
        return count;
    }

    public int getTotalQuantityDone() {
        int count = 0;
        for (BuildStageMaterials bsm : this.bsms) {
            count += bsm.getTotalQuantityDone();
        }
        return count;
    }
}

