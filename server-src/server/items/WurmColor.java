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
      float modifier = 0.0F;
      if (avgQl < 100.0F && mixRand.nextInt(3) == 0) {
         modifier = 0.01F * (100.0F - avgQl) / 100.0F;
      }

      int r = (getColorRed(color1) * weight1 + getColorRed(color2) * weight2) / (weight1 + weight2);
      if (r > 128) {
         r = (int)(128.0F + (float)(r - 128) * (1.0F - modifier));
      } else {
         r = (int)((float)r + (float)(128 - r) * modifier);
      }

      int g = (getColorGreen(color1) * weight1 + getColorGreen(color2) * weight2) / (weight1 + weight2);
      if (g > 128) {
         g = (int)(128.0F + (float)(g - 128) * (1.0F - modifier));
      } else {
         g = (int)((float)g + (float)(128 - g) * modifier);
      }

      int b = (getColorBlue(color1) * weight1 + getColorBlue(color2) * weight2) / (weight1 + weight2);
      if (b > 128) {
         b = (int)(128.0F + (float)(b - 128) * (1.0F - modifier));
      } else {
         b = (int)((float)b + (float)(128 - b) * modifier);
      }

      return createColor(r, g, b);
   }

   public static int getInitialColor(int itemTemplateId, float qualityLevel) {
      if (itemTemplateId == 431) {
         return getBaseBlack(qualityLevel);
      } else if (itemTemplateId == 432) {
         return getBaseWhite(qualityLevel);
      } else if (itemTemplateId == 433) {
         return getBaseRed(qualityLevel);
      } else if (itemTemplateId == 435) {
         return getBaseGreen(qualityLevel);
      } else {
         return itemTemplateId == 434 ? getBaseBlue(qualityLevel) : -1;
      }
   }

   public static int getCompositeColor(int color, int weight, int itemTemplateId, float qualityLevel) {
      int componentWeight = 1000;
      if (itemTemplateId == 439) {
         int r = (getColorRed(color) * weight + getColorRed(getInitialColor(433, qualityLevel)) * 1000) / (weight + 1000);
         int g = getColorGreen(color);
         int b = getColorBlue(color);
         return createColor(r, g, b);
      } else if (itemTemplateId == 47 || itemTemplateId == 195) {
         int r = getColorRed(color);
         int g = (getColorGreen(color) * weight + getColorGreen(getInitialColor(435, qualityLevel)) * 1000) / (weight + 1000);
         int b = getColorBlue(color);
         return createColor(r, g, b);
      } else if (itemTemplateId == 440) {
         int r = getColorRed(color);
         int g = getColorGreen(color);
         int b = (getColorBlue(color) * weight + getColorBlue(getInitialColor(434, qualityLevel)) * 1000) / (weight + 1000);
         return createColor(r, g, b);
      } else {
         return color;
      }
   }

   public static int getCompositeColor(int color, int itemTemplateId, float qualityLevel) {
      if (itemTemplateId == 433) {
         int r = getColorRed(color);
         int g = getColorGreen(color);
         int b = getColorBlue(color);
         int newR = getColorRed(getBaseRed(qualityLevel));
         if (newR > r) {
            r = newR;
         }

         return createColor(r, g, b);
      } else if (itemTemplateId == 435) {
         int r = getColorRed(color);
         int g = getColorGreen(color);
         int b = getColorBlue(color);
         int newG = getColorGreen(getBaseGreen(qualityLevel));
         if (newG > g) {
            g = newG;
         }

         return createColor(r, g, b);
      } else if (itemTemplateId == 434) {
         int r = getColorRed(color);
         int g = getColorGreen(color);
         int b = getColorBlue(color);
         int newB = getColorBlue(getBaseBlue(qualityLevel));
         if (newB > b) {
            b = newB;
         }

         return createColor(r, g, b);
      } else {
         return color;
      }
   }

   static final int getBaseRed(float ql) {
      return createColor(155 + (int)ql, 100 - (int)ql, 100 - (int)ql);
   }

   static final int getBaseGreen(float ql) {
      return createColor(100 - (int)ql, 155 + (int)ql, 100 - (int)ql);
   }

   static final int getBaseBlue(float ql) {
      return createColor(100 - (int)ql, 100 - (int)ql, 155 + (int)ql);
   }

   static final int getBaseWhite(float ql) {
      return createColor(155 + (int)ql, 155 + (int)ql, 155 + (int)ql);
   }

   static final int getBaseBlack(float ql) {
      return createColor(100 - (int)ql, 100 - (int)ql, 100 - (int)ql);
   }

   public static final String getRGBDescription(int aWurmColor) {
      return "R=" + getColorRed(aWurmColor) + ", G=" + getColorGreen(aWurmColor) + ", B=" + getColorBlue(aWurmColor);
   }
}
