package com.wurmonline.server.utils;

public class ItemOwnerDatabaseUpdatable implements WurmDbUpdatable {
   private final long id;
   private final long owner;
   private final String updateStatement;

   public ItemOwnerDatabaseUpdatable(long aId, long aOwner, String aUpdateStatement) {
      this.id = aId;
      this.owner = aOwner;
      this.updateStatement = aUpdateStatement;
   }

   @Override
   public String getDatabaseUpdateStatement() {
      return this.updateStatement;
   }

   long getId() {
      return this.id;
   }

   public long getOwner() {
      return this.owner;
   }

   @Override
   public String toString() {
      return "ItemDamageDatabaseUpdatable [id=" + this.id + ", owner=" + this.owner + ", updateStatement=" + this.updateStatement + "]";
   }
}
