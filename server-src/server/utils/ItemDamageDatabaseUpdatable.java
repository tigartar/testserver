package com.wurmonline.server.utils;

public final class ItemDamageDatabaseUpdatable implements WurmDbUpdatable {
   private final long id;
   private final float damage;
   private final long lastMaintained;
   private final String updateStatement;

   public ItemDamageDatabaseUpdatable(long aId, float aDamage, long aLastMaintained, String aUpdateStatement) {
      this.id = aId;
      this.damage = aDamage;
      this.lastMaintained = aLastMaintained;
      this.updateStatement = aUpdateStatement;
   }

   @Override
   public String getDatabaseUpdateStatement() {
      return this.updateStatement;
   }

   long getId() {
      return this.id;
   }

   public float getDamage() {
      return this.damage;
   }

   public long getLastMaintained() {
      return this.lastMaintained;
   }

   @Override
   public String toString() {
      return "ItemDamageDatabaseUpdatable [id="
         + this.id
         + ", damage="
         + this.damage
         + ", lastMaintained="
         + this.lastMaintained
         + ", updateStatement="
         + this.updateStatement
         + "]";
   }
}
