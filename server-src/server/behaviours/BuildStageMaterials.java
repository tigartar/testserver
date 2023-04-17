/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.structures.BridgePart;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BuildStageMaterials {
    private final List<BuildMaterial> bms;
    private final String name;
    private int stageNo = -1;

    public BuildStageMaterials(String newName) {
        this.name = newName;
        this.bms = new ArrayList<BuildMaterial>();
    }

    public void add(int templateId, int qty) throws NoSuchTemplateException {
        this.bms.add(new BuildMaterial(templateId, qty));
    }

    public void setStageNumber(int numb) {
        this.stageNo = numb;
    }

    public String getStageNumber() {
        if (this.stageNo >= 0) {
            return "Stage " + this.stageNo + " ";
        }
        return "";
    }

    public String getName() {
        return this.name;
    }

    public String getStageName() {
        return this.getStageNumber() + this.name;
    }

    public List<BuildMaterial> getBuildMaterials() {
        return this.bms;
    }

    public void setNoneNeeded() {
        for (BuildMaterial mat : this.bms) {
            mat.setNeededQuantity(0);
        }
    }

    public void setMaxNeeded() {
        for (BuildMaterial mat : this.bms) {
            mat.setNeededQuantity(mat.getTotalQuantityRequired());
        }
    }

    public void reduceNeededBy(int qty) {
        for (BuildMaterial mat : this.bms) {
            int newQty = mat.getTotalQuantityRequired() - qty;
            if (newQty < 0) {
                newQty = 0;
            }
            mat.setNeededQuantity(newQty);
        }
    }

    public boolean isStageComplete(BridgePart bridgePart) {
        for (BuildMaterial mat : this.bms) {
            if (mat.getTotalQuantityRequired() <= bridgePart.getMaterialCount()) continue;
            return false;
        }
        return true;
    }

    public boolean isStageComplete() {
        for (BuildMaterial bm : this.getBuildMaterials()) {
            if (bm.getNeededQuantity() <= 0) continue;
            return false;
        }
        return true;
    }

    public List<BuildMaterial> getRequiredMaterials() {
        ArrayList<BuildMaterial> mats = new ArrayList<BuildMaterial>();
        for (BuildMaterial mat : this.bms) {
            if (mat.getNeededQuantity() <= 0) continue;
            mats.add(mat);
        }
        return mats;
    }

    public String getRequiredMaterialString(boolean detailed) {
        HashSet<String> mats = new HashSet<String>();
        for (BuildMaterial mat : this.bms) {
            if (mat.getNeededQuantity() <= 0) continue;
            try {
                String description = "";
                ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(mat.getTemplateId());
                if (template != null) {
                    if (detailed) {
                        description = description + mat.getNeededQuantity() + " ";
                    }
                    if (template.sizeString.length() > 0) {
                        description = description + template.sizeString;
                    }
                    description = description + (mat.getNeededQuantity() > 1 ? template.getPlural() : template.getName());
                }
                if (description.length() == 0) {
                    description = "unknown quantities of unknown materials";
                }
                mats.add(description);
            }
            catch (NoSuchTemplateException noSuchTemplateException) {}
        }
        String description = "";
        int cnt = 0;
        for (String s : mats) {
            if (++cnt == mats.size() && mats.size() > 1) {
                description = description + " and ";
            } else if (cnt > 1) {
                description = description + ", ";
            }
            description = description + s;
        }
        if (description.length() == 0) {
            description = "no materials";
        }
        return description;
    }

    public int getTotalQuantityRequired() {
        int count = 0;
        for (BuildMaterial bm : this.bms) {
            count += bm.getTotalQuantityRequired();
        }
        return count;
    }

    public int getTotalQuantityDone() {
        int count = 0;
        for (BuildMaterial bm : this.bms) {
            count += bm.getTotalQuantityRequired() - bm.getNeededQuantity();
        }
        return count;
    }
}

