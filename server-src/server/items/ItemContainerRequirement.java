package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;

final class ItemContainerRequirement extends CreationRequirement {
   ItemContainerRequirement(int aNumber, int aResourceTemplateId, int aResourceNumber, int aVolumeNeeded, boolean aConsume) {
      super(aNumber, aResourceTemplateId, aResourceNumber, aConsume);
      this.setVolumeNeeded(aVolumeNeeded);
   }

   @Override
   boolean fill(Creature performer, Item container) {
      if (this.canBeFilled(container) && this.willBeConsumed()) {
         int found = 0;
         Item[] items = container.getAllItems(false);

         for(int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() == this.getResourceTemplateId()) {
               ++found;
               Items.destroyItem(items[i].getWurmId());
               if (found == this.getResourceNumber()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean canBeFilled(Item container) {
      int found = 0;
      Item[] items = container.getAllItems(false);

      for(int i = 0; i < items.length; ++i) {
         if (items[i].getTemplateId() == this.getResourceTemplateId()) {
            if (++found == this.getResourceNumber()) {
               return true;
            }
         }
      }

      return false;
   }
}
