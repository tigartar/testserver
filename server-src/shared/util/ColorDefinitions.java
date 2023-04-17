/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

public final class ColorDefinitions {
    public static final float[] COLOR_SYSTEM = new float[]{0.5f, 1.0f, 0.5f};
    public static final float[] COLOR_ERROR = new float[]{1.0f, 0.3f, 0.3f};
    public static final float[] COLOR_WHITE = new float[]{1.0f, 1.0f, 1.0f};
    public static final float[] COLOR_BLACK = new float[]{0.0f, 0.0f, 0.0f};
    public static final float[] COLOR_NAVY_BLUE = new float[]{0.23f, 0.39f, 1.0f};
    public static final float[] COLOR_GREEN = new float[]{0.08f, 1.0f, 0.08f};
    public static final float[] COLOR_RED = new float[]{1.0f, 0.0f, 0.0f};
    public static final float[] COLOR_MAROON = new float[]{0.5f, 0.0f, 0.0f};
    public static final float[] COLOR_PURPLE = new float[]{0.5f, 0.0f, 0.5f};
    public static final float[] COLOR_ORANGE = new float[]{1.0f, 0.85f, 0.24f};
    public static final float[] COLOR_YELLOW = new float[]{1.0f, 1.0f, 0.0f};
    public static final float[] COLOR_LIME = new float[]{0.0f, 1.0f, 0.0f};
    public static final float[] COLOR_TEAL = new float[]{0.0f, 0.5f, 0.5f};
    public static final float[] COLOR_CYAN = new float[]{0.0f, 1.0f, 1.0f};
    public static final float[] COLOR_ROYAL_BLUE = new float[]{0.23f, 0.39f, 1.0f};
    public static final float[] COLOR_FUCHSIA = new float[]{1.0f, 0.0f, 1.0f};
    public static final float[] COLOR_GREY = new float[]{0.5f, 0.5f, 0.5f};
    public static final float[] COLOR_SILVER = new float[]{0.75f, 0.75f, 0.75f};

    private ColorDefinitions() {
    }

    public static float[] getColor(byte colorCode) {
        switch (colorCode) {
            case 0: {
                return COLOR_WHITE;
            }
            case 1: {
                return COLOR_BLACK;
            }
            case 2: {
                return COLOR_NAVY_BLUE;
            }
            case 3: {
                return COLOR_GREEN;
            }
            case 4: {
                return COLOR_RED;
            }
            case 5: {
                return COLOR_MAROON;
            }
            case 6: {
                return COLOR_PURPLE;
            }
            case 7: {
                return COLOR_ORANGE;
            }
            case 8: {
                return COLOR_YELLOW;
            }
            case 9: {
                return COLOR_LIME;
            }
            case 10: {
                return COLOR_TEAL;
            }
            case 11: {
                return COLOR_CYAN;
            }
            case 12: {
                return COLOR_ROYAL_BLUE;
            }
            case 13: {
                return COLOR_FUCHSIA;
            }
            case 14: {
                return COLOR_GREY;
            }
            case 15: {
                return COLOR_SILVER;
            }
            case 100: {
                return COLOR_SYSTEM;
            }
            case 101: {
                return COLOR_ERROR;
            }
        }
        return COLOR_BLACK;
    }
}

