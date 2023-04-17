/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import java.util.Random;

public final class WurmColor {
    private static final Random mixRand = new Random();

    private WurmColor() {
    }

    public static final int createColor(int r, int g, int b) {
        return ((b & 0xFF) << 16) + ((g & 0xFF) << 8) + (r & 0xFF);
    }

    public static final int getColorRed(int color) {
        return color & 0xFF;
    }

    public static final int getColorGreen(int color) {
        return color >> 8 & 0xFF;
    }

    public static final int getColorBlue(int color) {
        return color >> 16 & 0xFF;
    }

    public static final int mixColors(int color1, int weight1, int color2, int weight2, float avgQl) {
        int r;
        float modifier = 0.0f;
        if (avgQl < 100.0f && mixRand.nextInt(3) == 0) {
            modifier = 0.01f * (100.0f - avgQl) / 100.0f;
        }
        r = (r = (WurmColor.getColorRed(color1) * weight1 + WurmColor.getColorRed(color2) * weight2) / (weight1 + weight2)) > 128 ? (int)(128.0f + (float)(r - 128) * (1.0f - modifier)) : (int)((float)r + (float)(128 - r) * modifier);
        int g = (WurmColor.getColorGreen(color1) * weight1 + WurmColor.getColorGreen(color2) * weight2) / (weight1 + weight2);
        g = g > 128 ? (int)(128.0f + (float)(g - 128) * (1.0f - modifier)) : (int)((float)g + (float)(128 - g) * modifier);
        int b = (WurmColor.getColorBlue(color1) * weight1 + WurmColor.getColorBlue(color2) * weight2) / (weight1 + weight2);
        b = b > 128 ? (int)(128.0f + (float)(b - 128) * (1.0f - modifier)) : (int)((float)b + (float)(128 - b) * modifier);
        return WurmColor.createColor(r, g, b);
    }

    public static int getInitialColor(int itemTemplateId, float qualityLevel) {
        if (itemTemplateId == 431) {
            return WurmColor.getBaseBlack(qualityLevel);
        }
        if (itemTemplateId == 432) {
            return WurmColor.getBaseWhite(qualityLevel);
        }
        if (itemTemplateId == 433) {
            return WurmColor.getBaseRed(qualityLevel);
        }
        if (itemTemplateId == 435) {
            return WurmColor.getBaseGreen(qualityLevel);
        }
        if (itemTemplateId == 434) {
            return WurmColor.getBaseBlue(qualityLevel);
        }
        return -1;
    }

    public static int getCompositeColor(int color, int weight, int itemTemplateId, float qualityLevel) {
        int componentWeight = 1000;
        if (itemTemplateId == 439) {
            int r = (WurmColor.getColorRed(color) * weight + WurmColor.getColorRed(WurmColor.getInitialColor(433, qualityLevel)) * 1000) / (weight + 1000);
            int g = WurmColor.getColorGreen(color);
            int b = WurmColor.getColorBlue(color);
            return WurmColor.createColor(r, g, b);
        }
        if (itemTemplateId == 47 || itemTemplateId == 195) {
            int r = WurmColor.getColorRed(color);
            int g = (WurmColor.getColorGreen(color) * weight + WurmColor.getColorGreen(WurmColor.getInitialColor(435, qualityLevel)) * 1000) / (weight + 1000);
            int b = WurmColor.getColorBlue(color);
            return WurmColor.createColor(r, g, b);
        }
        if (itemTemplateId == 440) {
            int r = WurmColor.getColorRed(color);
            int g = WurmColor.getColorGreen(color);
            int b = (WurmColor.getColorBlue(color) * weight + WurmColor.getColorBlue(WurmColor.getInitialColor(434, qualityLevel)) * 1000) / (weight + 1000);
            return WurmColor.createColor(r, g, b);
        }
        return color;
    }

    public static int getCompositeColor(int color, int itemTemplateId, float qualityLevel) {
        if (itemTemplateId == 433) {
            int r = WurmColor.getColorRed(color);
            int g = WurmColor.getColorGreen(color);
            int b = WurmColor.getColorBlue(color);
            int newR = WurmColor.getColorRed(WurmColor.getBaseRed(qualityLevel));
            if (newR > r) {
                r = newR;
            }
            return WurmColor.createColor(r, g, b);
        }
        if (itemTemplateId == 435) {
            int r = WurmColor.getColorRed(color);
            int g = WurmColor.getColorGreen(color);
            int b = WurmColor.getColorBlue(color);
            int newG = WurmColor.getColorGreen(WurmColor.getBaseGreen(qualityLevel));
            if (newG > g) {
                g = newG;
            }
            return WurmColor.createColor(r, g, b);
        }
        if (itemTemplateId == 434) {
            int r = WurmColor.getColorRed(color);
            int g = WurmColor.getColorGreen(color);
            int b = WurmColor.getColorBlue(color);
            int newB = WurmColor.getColorBlue(WurmColor.getBaseBlue(qualityLevel));
            if (newB > b) {
                b = newB;
            }
            return WurmColor.createColor(r, g, b);
        }
        return color;
    }

    static final int getBaseRed(float ql) {
        return WurmColor.createColor(155 + (int)ql, 100 - (int)ql, 100 - (int)ql);
    }

    static final int getBaseGreen(float ql) {
        return WurmColor.createColor(100 - (int)ql, 155 + (int)ql, 100 - (int)ql);
    }

    static final int getBaseBlue(float ql) {
        return WurmColor.createColor(100 - (int)ql, 100 - (int)ql, 155 + (int)ql);
    }

    static final int getBaseWhite(float ql) {
        return WurmColor.createColor(155 + (int)ql, 155 + (int)ql, 155 + (int)ql);
    }

    static final int getBaseBlack(float ql) {
        return WurmColor.createColor(100 - (int)ql, 100 - (int)ql, 100 - (int)ql);
    }

    public static final String getRGBDescription(int aWurmColor) {
        return "R=" + WurmColor.getColorRed(aWurmColor) + ", G=" + WurmColor.getColorGreen(aWurmColor) + ", B=" + WurmColor.getColorBlue(aWurmColor);
    }
}

