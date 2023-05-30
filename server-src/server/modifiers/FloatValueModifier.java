package com.wurmonline.server.modifiers;

public final class FloatValueModifier extends ValueModifier {
   private float modifier = 0.0F;

   public FloatValueModifier(float value) {
      this.modifier = value;
   }

   public FloatValueModifier(int aType, float value) {
      super(aType);
      this.modifier = value;
   }

   public float getModifier() {
      return this.modifier;
   }

   public void setModifier(float newValue) {
      this.modifier = newValue;
   }
}
