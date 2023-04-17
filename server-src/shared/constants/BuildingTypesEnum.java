/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

public enum BuildingTypesEnum {
    HOUSE("structure.wall.house"),
    ALLFENCES("structure.wall.fence"),
    FLOOR("structure.floor"),
    ROOF("structure.roof"),
    STAIRCASE("structure.staircase");

    public final String modelString;

    private BuildingTypesEnum(String _modelString) {
        this.modelString = _modelString;
    }

    public final String getModelString() {
        return "model." + this.modelString;
    }

    public final String getTextureString() {
        return "img.texture." + this.modelString;
    }
}

