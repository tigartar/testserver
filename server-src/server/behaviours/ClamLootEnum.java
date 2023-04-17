/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum ClamLootEnum {
    NONE(-1, false, false),
    PEARL(1397, false, false),
    COIN(51, false, false),
    IRON_LUMP(46, true, false),
    LEAD_LUMP(49, true, false),
    COPPER_LUMP(47, true, false),
    TIN_LUMP(220, true, false),
    ZINC_LUMP(48, true, false),
    SILVER_LUMP(45, true, false),
    GOLD_LUMP(44, true, false),
    SALT(349, true, false),
    FLINT(446, true, false),
    MEAT(92, false, false),
    LINE_LIGHT(1348, true, false),
    LINE_MEDIUM(1349, true, false),
    LINE_HEAVY(1350, true, false),
    LINE_BRAIDED(1351, true, false),
    HANDLE(99, true, true),
    HANDLE_LEATHER(101, true, true),
    HANDLE_REINFORCED(1370, true, true),
    HANDLE_PADDED(1371, true, true),
    REEL_LIGHT(1372, true, true),
    REEL_MEDIUM(1373, true, true),
    REEL_DEEP(1374, true, true),
    REEL_PROFESSIONAL(1375, true, true),
    REEL_WOOD(1367, true, true),
    REEL_METAL(1368, true, true),
    HOOK_WOOD(1356, true, true),
    HOOK_METAL(1357, true, true),
    HOOK_BONE(1358, true, false),
    NAILS_SMALL(218, true, true),
    NAILS_LARGE(217, true, true),
    RIVET(131, true, true),
    SEED_CABBAGE(1146, true, false),
    SEED_PUMPKIN(34, true, false),
    SEED_WEMP(317, true, false),
    SEED_REED(744, true, false),
    SEED_COTTON(145, true, false),
    SEED_STRAWBERRY(750, true, false),
    SEED_FENNEL(1151, true, false),
    SEED_CARROT(1145, true, false),
    SEED_TOMATO(1147, true, false),
    SEED_SUGARBEET(1148, true, false),
    SEED_LETTUCE(1149, true, false),
    SEED_CUCUMBER(1248, true, false),
    SEED_PAPRIKA(1153, true, false),
    SEED_TURMERIC(1154, true, false),
    COCOABEAN(1155, true, false),
    FRAGMENT(1307, false, false);

    private final int templateId;
    private final boolean canHaveDamage;
    private final boolean randomMaterial;
    private static final Logger logger;

    private ClamLootEnum(int templateId, boolean canHaveDamage, boolean randomMaterial) {
        this.templateId = templateId;
        this.canHaveDamage = canHaveDamage;
        this.randomMaterial = randomMaterial;
    }

    public int getTemplateId() {
        return this.templateId;
    }

    public boolean canHaveDamage() {
        return this.canHaveDamage;
    }

    public boolean randomMaterial() {
        return this.randomMaterial;
    }

    public static ClamLootEnum[] getLootTable() {
        ClamLootEnum[] loot = new ClamLootEnum[]{COIN, IRON_LUMP, IRON_LUMP, IRON_LUMP, IRON_LUMP, IRON_LUMP, IRON_LUMP, LEAD_LUMP, COPPER_LUMP, LEAD_LUMP, TIN_LUMP, ZINC_LUMP, SILVER_LUMP, GOLD_LUMP, SALT, FLINT, MEAT, LINE_LIGHT, LINE_MEDIUM, LINE_HEAVY, LINE_BRAIDED, PEARL, PEARL, PEARL, HANDLE, HANDLE_LEATHER, HANDLE_REINFORCED, HANDLE_PADDED, REEL_LIGHT, REEL_MEDIUM, REEL_DEEP, REEL_PROFESSIONAL, REEL_WOOD, REEL_METAL, HOOK_WOOD, HOOK_METAL, HOOK_BONE, NAILS_SMALL, NAILS_LARGE, RIVET, SEED_CABBAGE, SEED_PUMPKIN, SEED_WEMP, SEED_REED, SEED_COTTON, SEED_STRAWBERRY, SEED_FENNEL, SEED_CARROT, SEED_TOMATO, SEED_SUGARBEET, SEED_LETTUCE, SEED_CUCUMBER, SEED_PAPRIKA, SEED_TURMERIC, COCOABEAN, PEARL, FRAGMENT, FRAGMENT, FRAGMENT, FRAGMENT, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, PEARL, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, MEAT, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE};
        if (loot.length != 256) {
            logger.log(Level.SEVERE, "Wrong lenght (" + loot.length + ") loot table", new Exception("Bad loot table!"));
        }
        return loot;
    }

    static {
        logger = Logger.getLogger(ClamLootEnum.class.getName());
    }
}

