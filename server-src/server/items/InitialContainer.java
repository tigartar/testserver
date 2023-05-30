package com.wurmonline.server.items;

public class InitialContainer {
   final int templateId;
   final String name;
   final byte material;

   InitialContainer(int aTemplateId, String aName) {
      this.templateId = aTemplateId;
      this.name = aName;
      this.material = 0;
   }

   InitialContainer(int aTemplateId, String aName, byte aMaterial) {
      this.templateId = aTemplateId;
      this.name = aName;
      this.material = aMaterial;
   }

   public int getTemplateId() {
      return this.templateId;
   }

   public String getName() {
      return this.name;
   }

   public byte getMaterial() {
      return this.material;
   }
}
