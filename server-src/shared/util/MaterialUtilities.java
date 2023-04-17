/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

import com.wurmonline.shared.constants.ItemMaterials;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaterialUtilities
implements ItemMaterials {
    private static final Pattern prefixPattern = Pattern.compile("^(?:small|medium|large|huge|unfinished|pile of)", 2);
    public static final byte COMMON = 0;
    public static final byte RARE = 1;
    public static final byte SUPREME = 2;
    public static final byte FANTASTIC = 3;
    public static final String emptyString = "";

    private MaterialUtilities() {
    }

    public static final int getPrefixEndPos(String baseName) {
        Matcher m = prefixPattern.matcher(baseName);
        if (!m.find()) {
            return -1;
        }
        return m.end();
    }

    public static final String getRarityString(byte rarity) {
        switch (rarity) {
            case 0: {
                return emptyString;
            }
            case 1: {
                return "rare ";
            }
            case 2: {
                return "supreme ";
            }
            case 3: {
                return "fantastic ";
            }
        }
        return emptyString;
    }

    public static final void insertRarityDescription(StringBuilder sb, byte rarity) {
        if (rarity > 0) {
            sb.append(MaterialUtilities.getRarityString(rarity));
        }
    }

    public static final void appendNameWithMaterial(StringBuilder sb, String baseName, byte materialId) {
        String materialName = MaterialUtilities.getClientMaterialString(materialId, true);
        if (materialName == null || baseName.indexOf(materialName) != -1 || baseName.charAt(0) == '\"') {
            sb.append(baseName);
        } else {
            String baseNameAfter;
            String baseNameBefore;
            int pos = MaterialUtilities.getPrefixEndPos(baseName);
            if (pos != -1) {
                baseNameBefore = baseName.substring(0, pos);
                baseNameAfter = baseName.substring(pos + 1);
            } else {
                baseNameBefore = null;
                baseNameAfter = baseName;
            }
            if (baseNameBefore != null) {
                sb.append(baseNameBefore).append(" ");
            }
            sb.append(materialName).append(" ");
            sb.append(baseNameAfter);
        }
    }

    public static final void appendNameWithMaterialSuffix(StringBuilder sb, String baseName, byte materialId) {
        String materialName = MaterialUtilities.getClientMaterialString(materialId, true);
        if (materialName == null || baseName.length() == 0 || baseName.indexOf(materialName) != -1 || baseName.charAt(0) == '\"') {
            sb.append(baseName);
        } else {
            sb.append(baseName.trim());
            sb.append(", ");
            sb.append(materialName);
        }
    }

    public static final String getClientMaterialString(byte material, boolean incMeat) {
        switch (material) {
            case 11: {
                return "iron";
            }
            case 9: {
                return "steel";
            }
            case 12: {
                return "lead";
            }
            case 10: {
                return "copper";
            }
            case 7: {
                return "gold";
            }
            case 8: {
                return "silver";
            }
            case 30: {
                return "brass";
            }
            case 31: {
                return "bronze";
            }
            case 13: {
                return "zinc";
            }
            case 34: {
                return "tin";
            }
            case 56: {
                return "adamantine";
            }
            case 57: {
                return "glimmersteel";
            }
            case 14: {
                return "birchwood";
            }
            case 37: {
                return "pinewood";
            }
            case 38: {
                return "oakenwood";
            }
            case 63: {
                return "chestnut";
            }
            case 64: {
                return "walnut";
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
            case 66: {
                return "lindenwood";
            }
            case 65: {
                return "firwood";
            }
            case 71: {
                return "hazelnutwood";
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
            case 2: {
                return null;
            }
            case 72: {
                if (incMeat) {
                    return "bear";
                }
                return null;
            }
            case 73: {
                if (incMeat) {
                    return "beef";
                }
                return null;
            }
            case 74: {
                if (incMeat) {
                    return "canine";
                }
                return null;
            }
            case 75: {
                if (incMeat) {
                    return "feline";
                }
                return null;
            }
            case 76: {
                if (incMeat) {
                    return "dragon";
                }
                return null;
            }
            case 77: {
                if (incMeat) {
                    return "fowl";
                }
                return null;
            }
            case 78: {
                if (incMeat) {
                    return "game";
                }
                return null;
            }
            case 79: {
                if (incMeat) {
                    return "horse";
                }
                return null;
            }
            case 80: {
                if (incMeat) {
                    return "human";
                }
                return null;
            }
            case 81: {
                if (incMeat) {
                    return "humanoid";
                }
                return null;
            }
            case 82: {
                if (incMeat) {
                    return "insect";
                }
                return null;
            }
            case 83: {
                if (incMeat) {
                    return "lamb";
                }
                return null;
            }
            case 84: {
                if (incMeat) {
                    return "pork";
                }
                return null;
            }
            case 85: {
                if (incMeat) {
                    return "seafood";
                }
                return null;
            }
            case 86: {
                if (incMeat) {
                    return "snake";
                }
                return null;
            }
            case 87: {
                if (incMeat) {
                    return "tough";
                }
                return null;
            }
            case 16: {
                return "leather";
            }
            case 17: {
                return "cotton";
            }
            case 69: {
                return "wool";
            }
            case 18: {
                return "clay";
            }
            case 19: {
                return "pottery";
            }
            case 58: {
                return "tar";
            }
            case 59: {
                return "peat";
            }
            case 61: {
                return "slate";
            }
            case 62: {
                return "marble";
            }
            case 89: {
                return "sandstone";
            }
            case 67: {
                return "seryll";
            }
            case 70: {
                return "straw";
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
        return null;
    }

    public static final String getMaterialString(byte material) {
        String toReturn = "unknown";
        switch (material) {
            case 3: {
                toReturn = "rye";
                break;
            }
            case 4: {
                toReturn = "oat";
                break;
            }
            case 5: {
                toReturn = "barley";
                break;
            }
            case 6: {
                toReturn = "wheat";
                break;
            }
            case 11: {
                toReturn = "iron";
                break;
            }
            case 9: {
                toReturn = "steel";
                break;
            }
            case 12: {
                toReturn = "lead";
                break;
            }
            case 10: {
                toReturn = "copper";
                break;
            }
            case 7: {
                toReturn = "gold";
                break;
            }
            case 8: {
                toReturn = "silver";
                break;
            }
            case 30: {
                toReturn = "brass";
                break;
            }
            case 31: {
                toReturn = "bronze";
                break;
            }
            case 14: {
                toReturn = "birchwood";
                break;
            }
            case 37: {
                toReturn = "pinewood";
                break;
            }
            case 38: {
                toReturn = "oakenwood";
                break;
            }
            case 63: {
                toReturn = "chestnut";
                break;
            }
            case 64: {
                toReturn = "walnut";
                break;
            }
            case 39: {
                toReturn = "cedarwood";
                break;
            }
            case 40: {
                toReturn = "willow";
                break;
            }
            case 41: {
                toReturn = "maplewood";
                break;
            }
            case 42: {
                toReturn = "applewood";
                break;
            }
            case 43: {
                toReturn = "lemonwood";
                break;
            }
            case 44: {
                toReturn = "olivewood";
                break;
            }
            case 45: {
                toReturn = "cherrywood";
                break;
            }
            case 46: {
                toReturn = "lavenderwood";
                break;
            }
            case 47: {
                toReturn = "rosewood";
                break;
            }
            case 48: {
                toReturn = "thorn";
                break;
            }
            case 49: {
                toReturn = "grapewood";
                break;
            }
            case 50: {
                toReturn = "camelliawood";
                break;
            }
            case 51: {
                toReturn = "oleanderwood";
                break;
            }
            case 66: {
                toReturn = "lindenwood";
                break;
            }
            case 65: {
                toReturn = "firwood";
                break;
            }
            case 68: {
                toReturn = "ivy";
                break;
            }
            case 13: {
                toReturn = "zinc";
                break;
            }
            case 1: {
                toReturn = "flesh";
                break;
            }
            case 15: {
                toReturn = "stone";
                break;
            }
            case 16: {
                toReturn = "leather";
                break;
            }
            case 17: {
                toReturn = "cotton";
                break;
            }
            case 69: {
                toReturn = "wool";
                break;
            }
            case 18: {
                toReturn = "clay";
                break;
            }
            case 19: {
                toReturn = "pottery";
                break;
            }
            case 34: {
                toReturn = "tin";
                break;
            }
            case 20: {
                toReturn = "glass";
                break;
            }
            case 21: {
                toReturn = "magic";
                break;
            }
            case 22: {
                toReturn = "vegetarian";
                break;
            }
            case 23: {
                toReturn = "fire";
                break;
            }
            case 28: {
                toReturn = "dairy";
                break;
            }
            case 25: {
                toReturn = "oil";
                break;
            }
            case 26: {
                toReturn = "water";
                break;
            }
            case 27: {
                toReturn = "charcoal";
                break;
            }
            case 29: {
                toReturn = "honey";
                break;
            }
            case 32: {
                toReturn = "fat";
                break;
            }
            case 33: {
                toReturn = "paper";
                break;
            }
            case 35: {
                toReturn = "bone";
                break;
            }
            case 36: {
                toReturn = "salt";
                break;
            }
            case 52: {
                toReturn = "crystal";
                break;
            }
            case 54: {
                toReturn = "diamond";
                break;
            }
            case 56: {
                toReturn = "adamantine";
                break;
            }
            case 57: {
                toReturn = "glimmersteel";
                break;
            }
            case 58: {
                toReturn = "tar";
                break;
            }
            case 59: {
                toReturn = "peat";
                break;
            }
            case 60: {
                toReturn = "reed";
                break;
            }
            case 62: {
                toReturn = "marble";
                break;
            }
            case 89: {
                toReturn = "sandstone";
                break;
            }
            case 67: {
                toReturn = "seryll";
                break;
            }
            case 61: {
                toReturn = "slate";
                break;
            }
            case 70: {
                toReturn = "straw";
                break;
            }
            case 71: {
                toReturn = "hazelnutwood";
                break;
            }
            case 72: {
                toReturn = "bear";
                break;
            }
            case 73: {
                toReturn = "beef";
                break;
            }
            case 74: {
                toReturn = "canine";
                break;
            }
            case 75: {
                toReturn = "feline";
                break;
            }
            case 76: {
                toReturn = "dragon";
                break;
            }
            case 77: {
                toReturn = "fowl";
                break;
            }
            case 78: {
                toReturn = "game";
                break;
            }
            case 79: {
                toReturn = "horse";
                break;
            }
            case 80: {
                toReturn = "human";
                break;
            }
            case 81: {
                toReturn = "humanoid";
                break;
            }
            case 82: {
                toReturn = "insect";
                break;
            }
            case 83: {
                toReturn = "lamb";
                break;
            }
            case 84: {
                toReturn = "pork";
                break;
            }
            case 85: {
                toReturn = "seafood";
                break;
            }
            case 86: {
                toReturn = "snake";
                break;
            }
            case 87: {
                toReturn = "tough";
                break;
            }
            case 88: {
                toReturn = "orangewood";
                break;
            }
            case 90: {
                toReturn = "raspberrywood";
                break;
            }
            case 91: {
                toReturn = "blueberrywood";
                break;
            }
            case 92: {
                toReturn = "lingonberrywood";
                break;
            }
            case 93: {
                toReturn = "metal";
                break;
            }
            case 94: {
                toReturn = "alloy";
                break;
            }
            case 95: {
                toReturn = "moonmetal";
                break;
            }
            case 96: {
                toReturn = "electrum";
                break;
            }
            default: {
                toReturn = "unknown";
            }
        }
        return toReturn;
    }

    public static final String getDragonLeatherMaterialNameFromColour(float red, float green, float blue) {
        if (red == 10.0f && green == 210.0f && blue == 10.0f) {
            return ".green";
        }
        if (red == 10.0f && green == 10.0f && blue == 10.0f) {
            return ".black";
        }
        if (red == 255.0f && green == 255.0f && blue == 255.0f) {
            return ".white";
        }
        if (red == 215.0f && green == 40.0f && blue == 40.0f) {
            return ".red";
        }
        if (red == 40.0f && green == 40.0f && blue == 215.0f) {
            return ".blue";
        }
        return emptyString;
    }

    public static boolean isLeather(byte material) {
        switch (material) {
            case 16: {
                return true;
            }
        }
        return false;
    }

    public static boolean isCloth(byte material) {
        switch (material) {
            case 17: 
            case 69: {
                return true;
            }
        }
        return false;
    }

    public static boolean isPaper(byte material) {
        switch (material) {
            case 33: {
                return true;
            }
        }
        return false;
    }

    public static boolean isStone(byte material) {
        switch (material) {
            case 15: 
            case 61: 
            case 62: 
            case 89: {
                return true;
            }
        }
        return false;
    }

    public static boolean isGlass(byte material) {
        switch (material) {
            case 20: 
            case 52: 
            case 54: {
                return true;
            }
        }
        return false;
    }

    public static boolean isPottery(byte material) {
        switch (material) {
            case 19: {
                return true;
            }
        }
        return false;
    }

    public static boolean isClay(byte material) {
        switch (material) {
            case 18: 
            case 19: {
                return true;
            }
        }
        return false;
    }

    public static boolean isWood(byte material) {
        switch (material) {
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
                return true;
            }
        }
        return false;
    }

    public static boolean isMetal(byte material) {
        switch (material) {
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 30: 
            case 31: 
            case 34: 
            case 56: 
            case 57: 
            case 67: 
            case 93: 
            case 94: 
            case 95: 
            case 96: {
                return true;
            }
        }
        return false;
    }

    public static boolean isGrain(byte material) {
        switch (material) {
            case 3: 
            case 4: 
            case 5: 
            case 6: {
                return true;
            }
        }
        return false;
    }

    public static boolean isLiquid(byte material) {
        switch (material) {
            case 25: 
            case 26: 
            case 28: 
            case 29: {
                return true;
            }
        }
        return false;
    }
}

