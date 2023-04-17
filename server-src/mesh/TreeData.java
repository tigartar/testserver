/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

import com.wurmonline.mesh.GrassData;
import com.wurmonline.shared.util.StringUtilities;

public final class TreeData {
    private TreeData() {
    }

    public static String getHelpSubject(byte type, boolean infected) {
        return "Terrain:" + TreeData.getTypeName(type).replace(' ', '_');
    }

    public static boolean isValidTree(int treeId) {
        return treeId < TreeType.getLength();
    }

    public static int getType(byte data) {
        return data & 0xF;
    }

    public static boolean hasFruit(int treeData) {
        return (treeData >> 3 & 1) == 1;
    }

    public static boolean isCentre(int treeData) {
        return (treeData >> 2 & 1) == 1;
    }

    public static GrassData.GrowthTreeStage getGrassLength(int treeData) {
        return GrassData.GrowthTreeStage.fromInt(treeData & 3);
    }

    public static String getTypeName(byte data) {
        return TreeType.fromTileData(data).getName();
    }

    public static enum TreeType {
        BIRCH(0, 14, false, 2, 100, 114, 128, 0.7887417f, 0.6493875f, 0.03f, "model.tree.birch", 0, 0, false),
        PINE(1, 37, false, 2, 101, 115, 129, 0.7200847f, 0.4f, 0.04f, "model.tree.pine", 1, 0, true),
        OAK(2, 38, false, 20, 102, 116, 130, 0.63670415f, 0.7f, 0.135f, "model.tree.oak", 2, 0, true),
        CEDAR(3, 39, false, 5, 103, 117, 131, 0.614782f, 0.37f, 0.05f, "model.tree.cedar", 3, 0, false),
        WILLOW(4, 40, false, 18, 104, 118, 132, 0.8156737f, 0.9655433f, 0.05f, "model.tree.willow", 0, 1, false),
        MAPLE(5, 41, false, 4, 105, 119, 133, 0.6439394f, 0.52989763f, 0.08f, "model.tree.maple", 1, 1, true),
        APPLE(6, 42, true, 2, 106, 120, 134, 1.4137214f, 1.1328298f, 0.03f, "model.tree.apple", 2, 1, true),
        LEMON(7, 43, true, 2, 107, 121, 135, 1.4890511f, 1.4594362f, 0.02f, "model.tree.lemon", 3, 1, true),
        OLIVE(8, 44, true, 2, 108, 122, 136, 0.84542066f, 1.0500308f, 0.07f, "model.tree.olive", 0, 2, true),
        CHERRY(9, 45, true, 2, 109, 123, 137, 1.1129296f, 1.1271963f, 0.025f, "model.tree.cherry", 1, 2, true),
        CHESTNUT(10, 63, false, 12, 110, 124, 138, 0.792233f, 0.68f, 0.07f, "model.tree.chestnut", 2, 2, true),
        WALNUT(11, 64, false, 15, 111, 125, 139, 0.7f, 0.65028346f, 0.07f, "model.tree.walnut", 3, 2, true),
        FIR(12, 65, false, 5, 112, 126, 140, 0.77708626f, 0.77f, 0.05f, "model.tree.fir", 0, 3, false),
        LINDEN(13, 66, false, 12, 113, 127, 141, 0.7157274f, 0.69f, 0.05f, "model.tree.linden", 1, 3, false),
        ORANGE(14, 88, true, 2, 163, 164, 165, 1.4890511f, 1.4594362f, 0.02f, "model.tree.orange", 2, 3, true);

        private final int typeId;
        private final byte materialId;
        private final boolean fruitTree;
        private int woodDifficulty;
        private final byte normalTree;
        private final byte myceliumTree;
        private final byte enchantedTree;
        private final float width;
        private final float height;
        private final float radius;
        private final String modelName;
        private final int posX;
        private final int posY;
        private final boolean canBearFruit;
        private static final TreeType[] types;

        private TreeType(int type, byte material, boolean isFruitTree, int woodDifficulty, int normalTree, int myceliumTree, int enchantedTree, float width, float height, float radius, String modelName, int posX, int posY, boolean canBearFruit) {
            this.typeId = type;
            this.materialId = material;
            this.fruitTree = isFruitTree;
            this.woodDifficulty = woodDifficulty;
            this.normalTree = (byte)normalTree;
            this.myceliumTree = (byte)myceliumTree;
            this.enchantedTree = (byte)enchantedTree;
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
            String name = TreeType.fromInt(this.typeId).toString() + " tree";
            return StringUtilities.raiseFirstLetter(name);
        }

        public byte getMaterial() {
            return this.materialId;
        }

        public boolean isFruitTree() {
            return this.fruitTree;
        }

        public byte asNormalTree() {
            return this.normalTree;
        }

        public byte asMyceliumTree() {
            return this.myceliumTree;
        }

        public byte asEnchantedTree() {
            return this.enchantedTree;
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

        public static TreeType fromTileData(int tileData) {
            return TreeType.fromInt(tileData & 0xF);
        }

        public static TreeType fromInt(int typeId) {
            if (typeId >= TreeType.getLength()) {
                return types[0];
            }
            return types[typeId & 0xFF];
        }

        public static int encodeTileData(int tage, int ttype) {
            ttype = Math.min(ttype, types.length - 1);
            ttype = Math.max(ttype, 0);
            return (tage & 0xF) << 4 | ttype & 0xF;
        }

        static {
            types = TreeType.values();
        }
    }
}

