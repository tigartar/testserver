/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

public final class CommonConstantsUtility {
    private CommonConstantsUtility() {
    }

    public static String getAttitudeDescription(byte aAttitudeTypeCode) {
        String lDescription;
        switch (aAttitudeTypeCode) {
            case 1: {
                lDescription = "Ally";
                break;
            }
            case 2: {
                lDescription = "Hostile";
                break;
            }
            case 0: {
                lDescription = "Neutral";
                break;
            }
            case 5: {
                lDescription = "Good";
                break;
            }
            case 4: {
                lDescription = "Evil";
                break;
            }
            case 3: {
                lDescription = "GM";
                break;
            }
            case 6: {
                lDescription = "Dev";
                break;
            }
            default: {
                lDescription = "Unknown attitude: " + aAttitudeTypeCode;
            }
        }
        return lDescription;
    }

    public static String getEffectDescription(short aEffectTypeCode) {
        String lDescription;
        switch (aEffectTypeCode) {
            case 0: {
                lDescription = "Campfire";
                break;
            }
            case 1: {
                lDescription = "Lightning Bolt";
                break;
            }
            case 2: {
                lDescription = "Altar Light Beam Holy";
                break;
            }
            case 3: {
                lDescription = "Altar Light Beam Unholy";
                break;
            }
            case 4: {
                lDescription = "Christmas Lights";
                break;
            }
            case 19: {
                lDescription = "Item Spawn";
                break;
            }
            default: {
                lDescription = "Unknown effect: " + aEffectTypeCode;
            }
        }
        return lDescription;
    }

    public static String getAttachEffectDescription(short aEffectTypeCode) {
        String lDescription;
        switch (aEffectTypeCode) {
            case 0: {
                lDescription = "Light";
                break;
            }
            case 1: {
                lDescription = "FireEffect";
                break;
            }
            case 2: {
                lDescription = "Transparent";
                break;
            }
            case 3: {
                lDescription = "Glow";
                break;
            }
            case 4: {
                lDescription = "Flames";
                break;
            }
            default: {
                lDescription = "Unknown effect: " + aEffectTypeCode;
            }
        }
        return lDescription;
    }
}

