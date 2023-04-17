/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Materials
implements ItemMaterials,
MiscConstants {
    private static final Logger logger = Logger.getLogger(Materials.class.getName());

    private Materials() {
    }

    public static int getTemplateIdForMaterial(byte material) {
        if (material == 0) {
            return -10;
        }
        switch (material) {
            case 2: 
            case 72: 
            case 73: 
            case 74: 
            case 75: 
            case 76: 
            case 77: 
            case 78: 
            case 79: 
            case 80: 
            case 81: 
            case 82: 
            case 83: 
            case 84: 
            case 85: 
            case 86: 
            case 87: {
                return 92;
            }
            case 3: {
                return 30;
            }
            case 4: {
                return 31;
            }
            case 5: {
                return 28;
            }
            case 6: {
                return 29;
            }
            case 7: {
                return 44;
            }
            case 8: {
                return 45;
            }
            case 9: {
                return 205;
            }
            case 10: {
                return 47;
            }
            case 11: {
                return 46;
            }
            case 12: {
                return 49;
            }
            case 13: {
                return 48;
            }
            case 14: 
            case 37: 
            case 38: 
            case 39: 
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 44: 
            case 45: 
            case 46: 
            case 47: 
            case 48: 
            case 49: 
            case 50: 
            case 51: 
            case 63: 
            case 64: 
            case 65: 
            case 66: 
            case 71: 
            case 88: 
            case 90: 
            case 91: 
            case 92: {
                return 9;
            }
            case 15: {
                return 146;
            }
            case 16: {
                return 72;
            }
            case 17: {
                return 214;
            }
            case 69: {
                return 925;
            }
            case 18: {
                return 130;
            }
            case 19: {
                return 130;
            }
            case 20: {
                return -10;
            }
            case 21: {
                return -10;
            }
            case 22: {
                return -10;
            }
            case 23: {
                return -10;
            }
            case 25: {
                return -10;
            }
            case 26: {
                return 128;
            }
            case 27: {
                return 204;
            }
            case 28: {
                return 142;
            }
            case 29: {
                return 70;
            }
            case 30: {
                return 221;
            }
            case 31: {
                return 223;
            }
            case 32: {
                return -10;
            }
            case 33: {
                return -10;
            }
            case 34: {
                return 220;
            }
            case 35: {
                return -10;
            }
            case 36: {
                return 349;
            }
            case 52: {
                return -10;
            }
            case 54: {
                return 380;
            }
            case 53: {
                return 318;
            }
            case 56: {
                return 694;
            }
            case 57: {
                return 698;
            }
            case 62: {
                return 785;
            }
            case 61: {
                return 770;
            }
            case 89: {
                return 1116;
            }
            case 67: {
                return 837;
            }
            case 96: {
                return 1411;
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Returning Template NOID for unexpected material: " + material);
        }
        return -10;
    }

    public static String getTreeName(byte data) {
        return TreeData.TreeType.fromInt(data).toString() + " tree";
    }

    public static BushData.BushType getBushTypeForWood(byte wood) {
        switch (wood) {
            case 46: {
                return BushData.BushType.LAVENDER;
            }
            case 47: {
                return BushData.BushType.ROSE;
            }
            case 48: {
                return BushData.BushType.THORN;
            }
            case 49: {
                return BushData.BushType.GRAPE;
            }
            case 50: {
                return BushData.BushType.CAMELLIA;
            }
            case 51: {
                return BushData.BushType.OLEANDER;
            }
            case 71: {
                return BushData.BushType.HAZELNUT;
            }
            case 90: {
                return BushData.BushType.RASPBERRY;
            }
            case 91: {
                return BushData.BushType.BLUEBERRY;
            }
            case 92: {
                return BushData.BushType.LINGONBERRY;
            }
        }
        return null;
    }

    public static TreeData.TreeType getTreeTypeForWood(byte wood) {
        switch (wood) {
            case 14: {
                return TreeData.TreeType.BIRCH;
            }
            case 37: {
                return TreeData.TreeType.PINE;
            }
            case 38: {
                return TreeData.TreeType.OAK;
            }
            case 39: {
                return TreeData.TreeType.CEDAR;
            }
            case 40: {
                return TreeData.TreeType.WILLOW;
            }
            case 41: {
                return TreeData.TreeType.MAPLE;
            }
            case 42: {
                return TreeData.TreeType.APPLE;
            }
            case 43: {
                return TreeData.TreeType.LEMON;
            }
            case 44: {
                return TreeData.TreeType.OLIVE;
            }
            case 45: {
                return TreeData.TreeType.CHERRY;
            }
            case 63: {
                return TreeData.TreeType.CHESTNUT;
            }
            case 64: {
                return TreeData.TreeType.WALNUT;
            }
            case 65: {
                return TreeData.TreeType.FIR;
            }
            case 66: {
                return TreeData.TreeType.LINDEN;
            }
            case 88: {
                return TreeData.TreeType.ORANGE;
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Returning Birch for unexpected material type: " + wood);
        }
        return null;
    }

    public static boolean isWood(byte material) {
        return MaterialUtilities.isWood(material);
    }

    public static boolean isLeather(byte material) {
        return MaterialUtilities.isLeather(material);
    }

    public static boolean isMetal(byte material) {
        return MaterialUtilities.isMetal(material);
    }

    public static boolean isCloth(byte material) {
        return MaterialUtilities.isCloth(material);
    }

    public static boolean isPaper(byte material) {
        return MaterialUtilities.isPaper(material);
    }

    public static boolean isStone(byte material) {
        return MaterialUtilities.isStone(material);
    }

    public static boolean isGlass(byte material) {
        return MaterialUtilities.isGlass(material);
    }

    public static boolean isPottery(byte material) {
        return MaterialUtilities.isPottery(material);
    }

    public static boolean isClay(byte material) {
        return MaterialUtilities.isClay(material);
    }

    public static final int getTransmutedTemplate(int templateId) {
        int toReturn = 26;
        switch (templateId) {
            case 479: {
                toReturn = 467;
                break;
            }
            case 467: {
                toReturn = 204;
                break;
            }
            case 204: {
                toReturn = 26;
                break;
            }
            case 26: {
                toReturn = 298;
                break;
            }
            case 298: {
                toReturn = 130;
                break;
            }
            case 130: {
                toReturn = 146;
                break;
            }
            case 146: {
                toReturn = 38;
                break;
            }
            case 38: {
                toReturn = 43;
                break;
            }
            case 43: {
                toReturn = 42;
                break;
            }
            case 42: {
                toReturn = 40;
                break;
            }
            case 40: {
                toReturn = 207;
                break;
            }
            case 207: {
                toReturn = 39;
                break;
            }
            case 39: {
                toReturn = 41;
                break;
            }
            case 46: {
                toReturn = 47;
                break;
            }
            case 47: {
                toReturn = 221;
                break;
            }
            case 221: {
                toReturn = 223;
                break;
            }
            case 223: {
                toReturn = 48;
                break;
            }
            case 48: {
                toReturn = 45;
                break;
            }
            case 45: {
                toReturn = 220;
                break;
            }
            case 220: {
                toReturn = 44;
                break;
            }
            case 44: {
                toReturn = 49;
                break;
            }
            default: {
                toReturn = 26;
            }
        }
        return toReturn;
    }

    public static byte convertMaterialStringIntoByte(String material) {
        switch (material) {
            case "flesh": {
                return 1;
            }
            case "meat": {
                return 2;
            }
            case "rye": {
                return 3;
            }
            case "oat": {
                return 4;
            }
            case "barley": {
                return 5;
            }
            case "wheat": {
                return 6;
            }
            case "gold": {
                return 7;
            }
            case "silver": {
                return 8;
            }
            case "steel": {
                return 9;
            }
            case "copper": {
                return 10;
            }
            case "iron": {
                return 11;
            }
            case "lead": {
                return 12;
            }
            case "zinc": {
                return 13;
            }
            case "birchwood": {
                return 14;
            }
            case "stone": {
                return 15;
            }
            case "leather": {
                return 16;
            }
            case "cotton": {
                return 17;
            }
            case "clay": {
                return 18;
            }
            case "pottery": {
                return 19;
            }
            case "glass": {
                return 20;
            }
            case "magic": {
                return 21;
            }
            case "vegetarian": {
                return 22;
            }
            case "fire": {
                return 23;
            }
            case "oil": {
                return 25;
            }
            case "water": {
                return 26;
            }
            case "charcoal": {
                return 27;
            }
            case "dairy": {
                return 28;
            }
            case "honey": {
                return 29;
            }
            case "brass": {
                return 30;
            }
            case "bronze": {
                return 31;
            }
            case "fat": {
                return 32;
            }
            case "paper": {
                return 33;
            }
            case "tin": {
                return 34;
            }
            case "bone": {
                return 35;
            }
            case "salt": {
                return 36;
            }
            case "pinewood": {
                return 37;
            }
            case "oakenwood": {
                return 38;
            }
            case "cedarwood": {
                return 39;
            }
            case "willow": {
                return 40;
            }
            case "maplewood": {
                return 41;
            }
            case "applewood": {
                return 42;
            }
            case "lemonwood": {
                return 43;
            }
            case "olivewood": {
                return 44;
            }
            case "cherrywood": {
                return 45;
            }
            case "lavenderwood": {
                return 46;
            }
            case "rosewood": {
                return 47;
            }
            case "thorn": {
                return 48;
            }
            case "grapewood": {
                return 49;
            }
            case "camelliawood": {
                return 50;
            }
            case "oleanderwood": {
                return 51;
            }
            case "crystal": {
                return 52;
            }
            case "wemp": {
                return 53;
            }
            case "diamond": {
                return 54;
            }
            case "animal": {
                return 55;
            }
            case "adamantine": {
                return 56;
            }
            case "glimmersteel": {
                return 57;
            }
            case "tar": {
                return 58;
            }
            case "peat": {
                return 59;
            }
            case "reed": {
                return 60;
            }
            case "slate": {
                return 61;
            }
            case "marble": {
                return 62;
            }
            case "chestnut": {
                return 63;
            }
            case "walnut": {
                return 64;
            }
            case "firwood": {
                return 65;
            }
            case "lindenwood": {
                return 66;
            }
            case "seryll": {
                return 67;
            }
            case "ivy": {
                return 68;
            }
            case "wool": {
                return 69;
            }
            case "straw": {
                return 70;
            }
            case "hazelnutwood": {
                return 71;
            }
            case "bear": {
                return 72;
            }
            case "beef": {
                return 73;
            }
            case "canine": {
                return 74;
            }
            case "feline": {
                return 75;
            }
            case "dragon": {
                return 76;
            }
            case "fowl": {
                return 77;
            }
            case "game": {
                return 78;
            }
            case "horse": {
                return 79;
            }
            case "human": {
                return 80;
            }
            case "humanoid": {
                return 81;
            }
            case "insect": {
                return 82;
            }
            case "lamb": {
                return 83;
            }
            case "pork": {
                return 84;
            }
            case "seafood": {
                return 85;
            }
            case "snake": {
                return 86;
            }
            case "tough": {
                return 87;
            }
            case "orangewood": {
                return 88;
            }
            case "raspberrywood": {
                return 90;
            }
            case "blueberrywood": {
                return 91;
            }
            case "lingonberrywood": {
                return 92;
            }
            case "metal": {
                return 93;
            }
            case "alloy": {
                return 94;
            }
            case "moonmetal": {
                return 95;
            }
            case "electrum": {
                return 96;
            }
        }
        return 0;
    }

    public static String convertMaterialByteIntoString(byte material) {
        switch (material) {
            case 1: {
                return "flesh";
            }
            case 2: {
                return "meat";
            }
            case 3: {
                return "rye";
            }
            case 4: {
                return "oat";
            }
            case 5: {
                return "barley";
            }
            case 6: {
                return "wheat";
            }
            case 7: {
                return "gold";
            }
            case 8: {
                return "silver";
            }
            case 9: {
                return "steel";
            }
            case 10: {
                return "copper";
            }
            case 11: {
                return "iron";
            }
            case 12: {
                return "lead";
            }
            case 13: {
                return "zinc";
            }
            case 14: {
                return "birchwood";
            }
            case 15: {
                return "stone";
            }
            case 16: {
                return "leather";
            }
            case 17: {
                return "cotton";
            }
            case 18: {
                return "clay";
            }
            case 19: {
                return "pottery";
            }
            case 20: {
                return "glass";
            }
            case 21: {
                return "magic";
            }
            case 22: {
                return "vegetarian";
            }
            case 23: {
                return "fire";
            }
            case 25: {
                return "oil";
            }
            case 26: {
                return "water";
            }
            case 27: {
                return "charcoal";
            }
            case 28: {
                return "dairy";
            }
            case 29: {
                return "honey";
            }
            case 30: {
                return "brass";
            }
            case 31: {
                return "bronze";
            }
            case 32: {
                return "fat";
            }
            case 33: {
                return "paper";
            }
            case 34: {
                return "tin";
            }
            case 35: {
                return "bone";
            }
            case 36: {
                return "salt";
            }
            case 37: {
                return "pinewood";
            }
            case 38: {
                return "oakenwood";
            }
            case 39: {
                return "cedarwood";
            }
            case 40: {
                return "willow";
            }
            case 41: {
                return "maplewood";
            }
            case 42: {
                return "applewood";
            }
            case 43: {
                return "lemonwood";
            }
            case 44: {
                return "olivewood";
            }
            case 45: {
                return "cherrywood";
            }
            case 46: {
                return "lavenderwood";
            }
            case 47: {
                return "rosewood";
            }
            case 48: {
                return "thorn";
            }
            case 49: {
                return "grapewood";
            }
            case 50: {
                return "camelliawood";
            }
            case 51: {
                return "oleanderwood";
            }
            case 52: {
                return "crystal";
            }
            case 53: {
                return "wemp";
            }
            case 54: {
                return "diamond";
            }
            case 55: {
                return "animal";
            }
            case 56: {
                return "adamantine";
            }
            case 57: {
                return "glimmersteel";
            }
            case 58: {
                return "tar";
            }
            case 59: {
                return "peat";
            }
            case 60: {
                return "reed";
            }
            case 61: {
                return "slate";
            }
            case 62: {
                return "marble";
            }
            case 63: {
                return "chestnut";
            }
            case 64: {
                return "walnut";
            }
            case 65: {
                return "firwood";
            }
            case 66: {
                return "lindenwood";
            }
            case 67: {
                return "seryll";
            }
            case 68: {
                return "ivy";
            }
            case 69: {
                return "wool";
            }
            case 70: {
                return "straw";
            }
            case 71: {
                return "hazelnutwood";
            }
            case 72: {
                return "bear";
            }
            case 73: {
                return "beef";
            }
            case 74: {
                return "canine";
            }
            case 75: {
                return "feline";
            }
            case 76: {
                return "dragon";
            }
            case 77: {
                return "fowl";
            }
            case 78: {
                return "game";
            }
            case 79: {
                return "horse";
            }
            case 80: {
                return "human";
            }
            case 81: {
                return "humanoid";
            }
            case 82: {
                return "insect";
            }
            case 83: {
                return "lamb";
            }
            case 84: {
                return "pork";
            }
            case 85: {
                return "seafood";
            }
            case 86: {
                return "snake";
            }
            case 87: {
                return "tough";
            }
            case 88: {
                return "orangewood";
            }
            case 90: {
                return "raspberrywood";
            }
            case 91: {
                return "blueberrywood";
            }
            case 92: {
                return "lingonberrywood";
            }
            case 93: {
                return "metal";
            }
            case 94: {
                return "alloy";
            }
            case 95: {
                return "moonmetal";
            }
            case 96: {
                return "electrum";
            }
        }
        return "";
    }
}

