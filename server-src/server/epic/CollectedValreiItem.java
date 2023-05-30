package com.wurmonline.server.epic;

import java.util.ArrayList;
import java.util.List;

public class CollectedValreiItem {
   private final long id;
   private final String nameOfCollected;
   private final int typeOfCollected;

   public CollectedValreiItem(long _id, String _name, int _type) {
      this.id = _id;
      this.nameOfCollected = _name;
      this.typeOfCollected = _type;
   }

   public final long getId() {
      return this.id;
   }

   public final String getName() {
      return this.nameOfCollected;
   }

   public final int getType() {
      return this.typeOfCollected;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof CollectedValreiItem)) {
         return false;
      } else {
         CollectedValreiItem cmp = (CollectedValreiItem)obj;
         return cmp.getId() == this.id && cmp.getName().equals(this.nameOfCollected) && cmp.typeOfCollected == this.typeOfCollected;
      }
   }

   @Override
   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (int)(this.id ^ this.id >>> 32);
      result = 31 * result + (this.nameOfCollected == null ? 0 : this.nameOfCollected.hashCode());
      return 31 * result + this.typeOfCollected;
   }

   public static List<CollectedValreiItem> fromList(List<EpicEntity> entities) {
      List<CollectedValreiItem> list = new ArrayList<>();
      if (entities != null) {
         for(EpicEntity ent : entities) {
            list.add(new CollectedValreiItem(ent.getId(), ent.getName(), ent.getType()));
         }
      }

      return list;
   }
}
