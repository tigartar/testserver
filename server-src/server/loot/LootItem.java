/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.loot.DefaultItemCreateFunc;
import com.wurmonline.server.loot.ItemCreateFunc;
import java.util.Optional;

public class LootItem {
    private int itemTemplateId;
    private byte maxRarity;
    private byte minRarity;
    private float minQuality = 1.0f;
    private float maxQuality = 100.0f;
    private double itemChance = 1.0;
    private ItemCreateFunc itemCreateFunc = new DefaultItemCreateFunc();

    public LootItem() {
    }

    public LootItem(int aItemTemplateId, byte aMinRarity, byte aMaxRarity, float aMinQuality, float aMaxQuality, double aItemChance, ItemCreateFunc aItemCreateFunc) {
        this(aItemTemplateId, aMinRarity, aMaxRarity, aMinQuality, aMaxQuality, aItemChance);
        this.itemCreateFunc = aItemCreateFunc;
    }

    public LootItem(int aItemTemplateId, byte aMinRarity, byte aMaxRarity, float aMinQuality, float aMaxQuality, double aItemChance) {
        this.itemTemplateId = aItemTemplateId;
        this.maxRarity = aMaxRarity;
        this.minRarity = aMinRarity;
        this.maxQuality = aMaxQuality;
        this.minQuality = aMinQuality;
        this.itemChance = aItemChance;
    }

    public byte getMaxRarity() {
        return this.maxRarity;
    }

    public LootItem setMaxRarity(byte aMaxRarity) {
        this.maxRarity = aMaxRarity;
        return this;
    }

    public byte getMinRarity() {
        return this.minRarity;
    }

    public LootItem setMinRarity(byte aMinRarity) {
        this.minRarity = aMinRarity;
        return this;
    }

    public float getMaxQuality() {
        return this.maxQuality;
    }

    public LootItem setMaxQuality(float aMaxQuality) {
        this.maxQuality = aMaxQuality;
        return this;
    }

    public float getMinQuality() {
        return this.minQuality;
    }

    public LootItem setMinQuality(float aMinQuality) {
        this.minQuality = aMinQuality;
        return this;
    }

    public double getItemChance() {
        return this.itemChance;
    }

    public LootItem setItemChance(double itemChance) {
        this.itemChance = itemChance;
        return this;
    }

    public LootItem setItemCreateFunc(ItemCreateFunc func) {
        this.itemCreateFunc = func;
        return this;
    }

    public Optional<Item> createItem(Creature victim, Creature receiver) {
        return this.itemCreateFunc.create(victim, receiver, this);
    }

    public int getItemTemplateId() {
        return this.itemTemplateId;
    }

    public LootItem setItemTemplateId(int id) {
        this.itemTemplateId = id;
        return this;
    }

    public String getItemName() {
        try {
            return ItemTemplateFactory.getInstance().getTemplate(this.getItemTemplateId()).getName();
        }
        catch (NoSuchTemplateException e) {
            e.printStackTrace();
            return "<invalid template for id# " + this.getItemTemplateId() + ">";
        }
    }
}

