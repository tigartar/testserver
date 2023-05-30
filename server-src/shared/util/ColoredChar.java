package com.wurmonline.shared.util;

public class ColoredChar {
   public char chr;
   public float color;

   public ColoredChar(char chr, byte color) {
      this.chr = chr;
      this.color = (float)color;
   }

   public char getChr() {
      return this.chr;
   }

   public void setChr(char chr) {
      this.chr = chr;
   }

   public float getColor() {
      return this.color;
   }

   public void setColor(float color) {
      this.color = color;
   }
}
