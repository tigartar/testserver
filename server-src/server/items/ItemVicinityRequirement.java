package com.wurmonline.server.items;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public final class ItemVicinityRequirement extends CreationRequirement {
   public ItemVicinityRequirement(int aNumber, int aResourceTemplateId, int aNumberNeeded, boolean aConsume, int aDistance) {
      super(aNumber, aResourceTemplateId, aNumberNeeded, aConsume);
      this.setDistance(aDistance);
   }

   @Override
   public boolean fill(Creature performer, Item creation) {
      boolean toReturn = false;
      VolaTile tile = performer.getCurrentTile();
      if (tile == null) {
         return false;
      } else {
         VolaTile[] tiles = Zones.getTilesSurrounding(tile.tilex, tile.tiley, performer.isOnSurface(), this.getDistance());
         if (this.canBeFilled(tiles)) {
            if (this.willBeConsumed()) {
               int found = 0;

               for(int x = 0; x < tiles.length; ++x) {
                  Item[] items = tiles[x].getItems();

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
            } else {
               toReturn = true;
            }
         }

         return toReturn;
      }
   }

   public boolean canBeFilled(VolaTile[] tiles) {
      int found = 0;

      for(int x = 0; x < tiles.length; ++x) {
         Item[] items = tiles[x].getItems();

         for(int i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() == this.getResourceTemplateId()) {
               if (++found == this.getResourceNumber()) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
