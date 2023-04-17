/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplateFactory;
import java.util.ArrayList;

public class ContainerRestriction {
    private final boolean onlyOneOf;
    private ArrayList<Integer> itemTemplateIds;
    private String emptySlotName = null;

    public ContainerRestriction(boolean onlyOneOf, int ... itemTemplateId) {
        this.onlyOneOf = onlyOneOf;
        this.itemTemplateIds = new ArrayList();
        for (int i : itemTemplateId) {
            this.itemTemplateIds.add(i);
        }
    }

    public ContainerRestriction(boolean onlyOneOf, String emptySlotName, int ... itemTemplateId) {
        this(onlyOneOf, itemTemplateId);
        this.setEmptySlotName(emptySlotName);
    }

    public boolean canInsertItem(Item[] existing, Item toInsert) {
        if (!this.itemTemplateIds.contains(toInsert.getTemplateId())) {
            return false;
        }
        if (this.onlyOneOf) {
            for (Item i : existing) {
                if (!this.itemTemplateIds.contains(i.getTemplateId())) continue;
                return false;
            }
        }
        return true;
    }

    public void setEmptySlotName(String name) {
        this.emptySlotName = name;
    }

    public String getEmptySlotName() {
        if (this.emptySlotName != null) {
            return this.emptySlotName;
        }
        return "empty " + ItemTemplateFactory.getInstance().getTemplateName(this.getEmptySlotTemplateId()) + " slot";
    }

    public int getEmptySlotTemplateId() {
        return this.itemTemplateIds.get(0);
    }

    public boolean contains(int id) {
        return this.itemTemplateIds.contains(id);
    }

    public boolean doesItemOverrideSlot(Item toInsert) {
        return this.itemTemplateIds.contains(toInsert.getTemplateId());
    }
}

