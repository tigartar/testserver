/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

import com.wurmonline.shared.util.StringUtilities;

public final class BushData {
    private BushData() {
    }

    public static boolean isBush(int treeId) {
        return true;
    }

    public static boolean isTree(int treeId) {
        return false;
    }

    public static String getHelpSubject(byte type, boolean infected) {
        return "Terrain:" + BushData.getTypeName(type).replace(' ', '_');
    }

    public static boolean isValidBush(int treeId) {
        return treeId < BushType.getLength();
    }

    public static int getType(byte data) {
        return data & 0xF;
    }

    public static String getTypeName(byte data) {
        return BushType.fromTileData(BushData.getType(data)).getName();
    }

    public static enum BushType {
        LAVENDER(0, 46, 4, 142, 148, 154, 1.0f, 1.0f, 0.0f, "model.bush.lavendel", 0, 0, true),
        ROSE(1, 47, 5, 143, 149, 155, 2.0f, 1.0f, 0.0f, "model.bush.rose", 1, 0, true),
        THORN(2, 48, 15, 144, 150, 156, 0.5f, 0.5f, 0.0f, "model.bush.thorn", 2, 0, false),
        GRAPE(3, 49, 5, 145, 151, 157, 1.4f, 1.2f, 0.0f, "model.bush.grape", 3, 0, true),
        CAMELLIA(4, 50, 3, 146, 152, 158, 1.6f, 1.25f, 0.0f, "model.bush.camellia", 0, 1, true),
        OLEANDER(5, 51, 2, 147, 153, 159, 1.55f, 1.45f, 0.0f, "model.bush.oleander", 1, 1, true),
        HAZELNUT(6, 71, 2, 160, 161, 162, 1.7f, 1.32f, 0.0f, "model.bush.hazelnut", 2, 1, true),
        RASPBERRY(7, 90, 2, 166, 167, 168, 1.7f, 1.32f, 0.0f, "model.bush.raspberry", 3, 1, true),
        BLUEBERRY(8, 91, 2, 169, 170, 171, 1.7f, 1.32f, 0.0f, "model.bush.blueberry", 0, 2, true),
        LINGONBERRY(9, 92, 2, 172, 172, 172, 1.7f, 1.32f, 0.0f, "model.bush.lingonberry", 1, 2, true);

        private final int typeId;
        private final byte materialId;
        private final int woodDifficulty;
        private final byte normalBush;
        private final byte myceliumBush;
        private final byte enchantedBush;
        private final float width;
        private final float height;
        private final float radius;
        private final String modelName;
        private final int posX;
        private final int posY;
        private final boolean canBearFruit;
        private static final BushType[] types;

        private BushType(int id, byte material, int woodDifficulty, int normalBush, int myceliumBush, int enchantedBush, float width, float height, float radius, String modelName, int posX, int posY, boolean canBearFruit) {
            this.typeId = id;
            this.materialId = material;
            this.woodDifficulty = woodDifficulty;
            this.normalBush = (byte)normalBush;
            this.myceliumBush = (byte)myceliumBush;
            this.enchantedBush = (byte)enchantedBush;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.modelName = modelName;
            this.posX = posX;
            this.posY = posY;
            this.canBearFruit = canBearFruit;
        }

        public int getTypeId() {
            return this.typeId;
        }

        public String getName() {
            String name = BushType.fromInt(this.typeId).toString() + " bush";
            return StringUtilities.raiseFirstLetter(name);
        }

        public byte getMaterial() {
            return this.materialId;
        }

        public byte asNormalBush() {
            return this.normalBush;
        }

        public byte asMyceliumBush() {
            return this.myceliumBush;
        }

        public byte asEnchantedBush() {
            return this.enchantedBush;
        }

        public int getDifficulty() {
            return this.woodDifficulty;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }

        public float getRadius() {
            return this.radius;
        }

        String getModelName() {
            return this.modelName;
        }

        public String getModelResourceName(int treeAge) {
            if (treeAge < 4) {
                return this.getModelName() + ".young";
            }
            if (treeAge == 15) {
                return this.getModelName() + ".shrivelled";
            }
            return this.getModelName();
        }

        public int getTexturPosX() {
            return this.posX;
        }

        public int getTexturPosY() {
            return this.posY;
        }

        public boolean canBearFruit() {
            return this.canBearFruit;
        }

        public static final int getLength() {
            return types.length;
        }

        public static BushType fromTileData(int tileData) {
            return BushType.fromInt(tileData & 0xF);
        }

        public static BushType fromInt(int i) {
            if (i >= BushType.getLength()) {
                return types[0];
            }
            return types[i & 0xFF];
        }

        public static BushType decodeTileData(int tileData) {
            return BushType.fromInt(tileData & 0xF);
        }

        public static int encodeTileData(int tage, int ttype) {
            ttype = Math.min(ttype, types.length - 1);
            ttype = Math.max(ttype, 0);
            return (tage & 0xF) << 4 | ttype & 0xF;
        }

        static {
            types = BushType.values();
        }
    }
}

