/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

public enum FoliageAge {
    YOUNG_ONE(0, "young", 0, false),
    YOUNG_TWO(1, "young", 0, false),
    YOUNG_THREE(2, "young", 1, false),
    YOUNG_FOUR(3, "young", 2, true),
    MATURE_ONE(4, "mature", 3, true),
    MATURE_TWO(5, "mature", 4, false),
    MATURE_THREE(6, "mature", 5, false),
    MATURE_SPROUTING(7, "mature, sprouting", 6, false),
    OLD_ONE(8, "old", 6, false),
    OLD_ONE_SPROUTING(9, "old, sprouting", 8, false),
    OLD_TWO(10, "old", 8, false),
    OLD_TWO_SPROUTING(11, "old, sprouting", 10, false),
    VERY_OLD(12, "very old", 10, false),
    VERY_OLD_SPROUTING(13, "very old, sprouting", 12, true),
    OVERAGED(14, "overaged", 12, true),
    SHRIVELLED(15, "shriveled", 14, false);

    private byte ageId;
    private String name;
    private byte prunedAge;
    private boolean isPrunable;
    private static final FoliageAge[] ages;

    private FoliageAge(int id, String name, int prunedAge, boolean isPrunable) {
        this.ageId = (byte)(id & 0xFF);
        this.name = name;
        this.prunedAge = (byte)(prunedAge & 0xFF);
        this.isPrunable = isPrunable;
    }

    public byte getAgeId() {
        return this.ageId;
    }

    public String getAgeName() {
        return this.name;
    }

    public boolean isPrunable() {
        return this.isPrunable;
    }

    public FoliageAge getPrunedAge() {
        return FoliageAge.fromByte(this.prunedAge);
    }

    public byte encodeAsData() {
        return (byte)(this.ageId << 4);
    }

    public static FoliageAge fromByte(byte i) {
        return ages[i];
    }

    public static FoliageAge getFoliageAge(byte tileData) {
        return FoliageAge.fromByte(FoliageAge.getAgeAsByte(tileData));
    }

    public static byte getAgeAsByte(byte tileData) {
        return (byte)(tileData >> 4 & 0xF);
    }

    static {
        ages = FoliageAge.values();
    }
}

