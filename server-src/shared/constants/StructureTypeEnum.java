/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum StructureTypeEnum {
    SOLID(0, "wall", ""),
    WINDOW(1, "window", "window"),
    DOOR(2, "door", "door"),
    DOUBLE_DOOR(3, "double door", "doubledoor"),
    ARCHED(4, "arched", "arched"),
    NARROW_WINDOW(5, "narrow window", "windownarrow"),
    PORTCULLIS(6, "portcullis", "portcullis"),
    BARRED(7, "barred", "bars1"),
    RUBBLE(8, "rubble", "rubble"),
    BALCONY(9, "balcony", "balcony"),
    JETTY(10, "jetty", "jetty"),
    ORIEL(11, "oriel", "oriel2"),
    CANOPY_DOOR(12, "canopy", "canopy"),
    WIDE_WINDOW(13, "wide window", "widewindow"),
    ARCHED_LEFT(14, "left arch", "archleft"),
    ARCHED_RIGHT(15, "right arch", "archright"),
    ARCHED_T(16, "T arch", "archt"),
    SCAFFOLDING(17, "scaffolding", "scaffolding"),
    FENCE(0, "fence", ""),
    PARAPET(0, "parapet", "parapet"),
    PALISADE(0, "palisade", "palisade"),
    FENCE_WALL(0, "stone wall", ""),
    GATE(0, "gate", "gate"),
    FENCE_TALL(0, "tall wall", ""),
    WOVEN(0, "woven", ""),
    NO_WALL(0, "missing wall", ""),
    FENCE_IRON_BARS(0, "iron fence", ""),
    FENCE_IRON_BARS_GATE(0, "iron fence gate", ""),
    FENCE_IRON_BARS_TALL(0, "tall iron fence", ""),
    FENCE_IRON_BARS_TALL_GATE(0, "tall iron fence gate", ""),
    ROPE_LOW(0, "low rope fence", ""),
    ROPE_HIGH(0, "tall rope fence", ""),
    GARDESGARD_LOW(0, "low roundpole fence", ""),
    GARDESGARD_HIGH(0, "tall roundpole fence", ""),
    GARDESGARD_GATE(0, "roundpole fence gate", ""),
    CURB(0, "curb", ""),
    HEDGE_LOW(0, "", ""),
    HEDGE_MEDIUM(0, "", ""),
    HEDGE_HIGH(0, "", ""),
    MAGIC_FENCE(0, "", ""),
    FLOWERBED(0, "", ""),
    MEDIUM_CHAIN(0, "", ""),
    SIEGWALL(0, "", ""),
    FENCE_PLAN_WOODEN(0, "", ""),
    FENCE_PLAN_WOODEN_GATE(0, "", ""),
    FENCE_PLAN_PALISADE(0, "", ""),
    FENCE_PLAN_PALISADE_GATE(0, "", ""),
    FENCE_PLAN_STONEWALL(0, "", ""),
    FENCE_PLAN_STONEWALL_HIGH(0, "", ""),
    FENCE_PLAN_IRON_BARS(0, "", ""),
    FENCE_PLAN_IRON_BARS_GATE(0, "", ""),
    FENCE_PLAN_IRON_BARS_TALL(0, "", ""),
    FENCE_PLAN_IRON_BARS_TALL_GATE(0, "", ""),
    FENCE_PLAN_STONE_PARAPET(0, "", ""),
    FENCE_PLAN_WOODEN_PARAPET(0, "", ""),
    FENCE_PLAN_IRON_BARS_PARAPET(0, "", ""),
    FENCE_PLAN_CRUDE(0, "", ""),
    FENCE_PLAN_CRUDE_GATE(0, "", ""),
    FENCE_PLAN_WOVEN(0, "", ""),
    FENCE_PLAN_ROPE_LOW(0, "", ""),
    FENCE_PLAN_ROPE_HIGH(0, "", ""),
    FENCE_PLAN_CURB(0, "", ""),
    FENCE_PLAN_GARDESGARD_LOW(0, "", ""),
    FENCE_PLAN_GARDESGARD_HIGH(0, "", ""),
    FENCE_PLAN_GARDESGARD_GATE(0, "", ""),
    FENCE_PLAN_STONE_FENCE(0, "", ""),
    FENCE_PLAN_MEDIUM_CHAIN(0, "", ""),
    FENCE_PLAN_PORTCULLIS(0, "", ""),
    HOUSE_PLAN_SOLID(0, "", ""),
    HOUSE_PLAN_DOOR(0, "", ""),
    HOUSE_PLAN_DOUBLE_DOOR(0, "", ""),
    HOUSE_PLAN_WINDOW(0, "", ""),
    HOUSE_PLAN_BARRED(0, "", ""),
    HOUSE_PLAN_ORIEL(0, "", ""),
    HOUSE_PLAN_ARCHED(0, "", ""),
    HOUSE_PLAN_ARCH_LEFT(0, "", ""),
    HOUSE_PLAN_ARCH_RIGHT(0, "", ""),
    HOUSE_PLAN_ARCH_T(0, "", ""),
    HOUSE_PLAN_PORTCULLIS(0, "", ""),
    HOUSE_PLAN_NARROW_WINDOW(0, "", ""),
    HOUSE_PLAN_BALCONY(0, "", ""),
    HOUSE_PLAN_JETTY(0, "", ""),
    HOUSE_PLAN_CANOPY(0, "", ""),
    PLAN(127, "plan", "plan");

    public final String typeName;
    public final String modelShortName;
    public final byte value;
    private static HashMap<String, StructureTypeEnum> lookupMap;

    private StructureTypeEnum(byte _value, String _typeName, String _modelShortName) {
        this.value = _value;
        this.typeName = _typeName;
        this.modelShortName = _modelShortName;
    }

    public static StructureTypeEnum getTypeByINDEX(int id) {
        if (id >= 0 && id <= StructureTypeEnum.values().length) {
            return StructureTypeEnum.values()[id];
        }
        if (id != 127 && id != -1) {
            Logger.getGlobal().warning("Value not a valid array position: " + id + " RETURNING PLAN(VAL=40)!");
        }
        return PLAN;
    }

    public static Optional<StructureTypeEnum> lookup(String name) {
        StructureTypeEnum temp;
        Optional<StructureTypeEnum> optional = Optional.empty();
        if (lookupMap == null) {
            lookupMap = new HashMap();
            for (StructureTypeEnum stt : StructureTypeEnum.values()) {
                lookupMap.put(stt.name(), stt);
            }
        }
        if ((temp = lookupMap.get(name)) != null) {
            optional = Optional.of(temp);
        } else if (Logger.getGlobal().isLoggable(Level.FINE)) {
            Logger.getGlobal().fine(name + " not found in lookup!");
        }
        return optional;
    }

    static {
        lookupMap = null;
    }
}

