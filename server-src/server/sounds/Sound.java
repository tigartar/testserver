package com.wurmonline.server.sounds;

public final class Sound {
   private final float posx;
   private final float posy;
   private final float posz;
   private final String name;
   private final float volume;
   private final float pitch;
   private final float priority;

   public Sound(String aResourceName, float aPosx, float aPosy, float aPosz, float aVolume, float aPitch, float aPriority) {
      if (aResourceName.startsWith("sound")) {
         this.name = aResourceName;
      } else {
         this.name = "sound." + aResourceName;
      }

      this.posx = aPosx;
      this.posy = aPosy;
      this.posz = aPosz;
      this.volume = aVolume;
      this.pitch = aPitch;
      this.priority = aPriority;
   }

   public float getPosX() {
      return this.posx;
   }

   public float getPosY() {
      return this.posy;
   }

   public float getPosZ() {
      return this.posz;
   }

   public float getPitch() {
      return this.pitch;
   }

   public float getVolume() {
      return this.volume;
   }

   public String getName() {
      return this.name;
   }

   public float getPriority() {
      return this.priority;
   }
}
