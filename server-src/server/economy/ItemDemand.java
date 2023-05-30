package com.wurmonline.server.economy;

final class ItemDemand {
   private final int templateId;
   private float demand;

   ItemDemand(int itemTemplateId, float dem) {
      this.templateId = itemTemplateId;
      this.demand = dem;
   }

   int getTemplateId() {
      return this.templateId;
   }

   float getDemand() {
      return this.demand;
   }

   void setDemand(float aDemand) {
      this.demand = aDemand;
   }
}
