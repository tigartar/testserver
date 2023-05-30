package com.wurmonline.shared.util;

public class MulticolorLineSegment {
   private byte color;
   private String text;

   public MulticolorLineSegment(String text, byte color) {
      this.text = text.replaceAll("\\p{C}", "?");
      this.color = color;
   }

   public ColoredChar[] convertToCharArray() {
      ColoredChar[] arr = new ColoredChar[this.getText().length()];

      for(int i = 0; i < this.getText().length(); ++i) {
         arr[i] = new ColoredChar(this.getText().charAt(i), this.getColor());
      }

      return arr;
   }

   public void setText(String text) {
      this.text = text;
   }

   public String getText() {
      return this.text;
   }

   public void setColor(byte color) {
      this.color = color;
   }

   public byte getColor() {
      return this.color;
   }
}
