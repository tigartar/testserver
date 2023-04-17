/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.util.logging.Logger;

public enum StructureMaterialEnum {
    WOOD(0, "wood"),
    STONE(1, "stone"),
    METAL(2, "metal"),
    TIMBER_FRAMED(3, "timber framed"),
    PLAIN_STONE(4, "plain stone"),
    SLATE(5, "slate"),
    ROUNDED_STONE(6, "rounded stone"),
    POTTERY(7, "pottery"),
    SANDSTONE(8, "sandstone"),
    RENDERED(9, "rendered"),
    MARBLE(10, "marble"),
    IRON(11, "iron"),
    LOG(12, "log"),
    CRUDE_WOOD(13, "crude wood"),
    FLOWER1(14, "flower"),
    FLOWER2(15, "flower"),
    FLOWER3(16, "flower"),
    FLOWER4(17, "flower"),
    FLOWER5(18, "flower"),
    FLOWER6(19, "flower"),
    FLOWER7(20, "flower"),
    ICE(21, "ice"),
    FIRE(22, "fire");

    public final byte material;
    public final String nameString;

    private StructureMaterialEnum(byte _material, String _nameString) {
        this.material = _material;
        this.nameString = _nameString;
    }

    public static StructureMaterialEnum getEnumByMaterial(byte material) {
        if (material >= 0 && material < StructureMaterialEnum.values().length) {
            return StructureMaterialEnum.values()[material];
        }
        Logger.getGlobal().warning("Reached default return value for material=" + material);
        return WOOD;
    }
}

