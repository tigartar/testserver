/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;

public class BuildMaterial {
    private final int templateId;
    private final int weightGrams;
    private final int totalQuantityRequired;
    private int neededQuantity;

    public BuildMaterial(int tid, int quantity) throws NoSuchTemplateException {
        int qty = quantity < 0 ? 0 : quantity;
        this.templateId = tid;
        this.weightGrams = ItemTemplateFactory.getInstance().getTemplate(tid).getWeightGrams();
        this.totalQuantityRequired = qty;
        this.neededQuantity = qty;
    }

    public int getTemplateId() {
        return this.templateId;
    }

    int getTotalQuantityRequired() {
        return this.totalQuantityRequired;
    }

    int getWeightGrams() {
        return this.weightGrams;
    }

    public String toString() {
        String toReturn = "";
        try {
            toReturn = "" + this.weightGrams / ItemTemplateFactory.getInstance().getTemplate(this.templateId).getWeightGrams() + " " + ItemTemplateFactory.getInstance().getTemplate(this.templateId).getName();
        }
        catch (NoSuchTemplateException noSuchTemplateException) {
            // empty catch block
        }
        return toReturn;
    }

    public void setNeededQuantity(int qty) {
        this.neededQuantity = qty < 0 ? 0 : qty;
    }

    public int getNeededQuantity() {
        return this.neededQuantity;
    }
}

