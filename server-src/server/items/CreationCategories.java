package com.wurmonline.server.items;

public enum CreationCategories {
   UNKNOWN("Unknown"),
   SHIPBUILDING("Shipbuilding resources"),
   STATUES("Statues"),
   STORAGE("Storage"),
   TOOLS("Tools"),
   CONSTRUCTION_MATERIAL("Construction material"),
   WEAPONS("Weapons"),
   BOWS("Bows"),
   KINDLINGS("Kindlings"),
   FIRE("Furnaces"),
   POTTERY("Pottery"),
   CONTAINER("Container"),
   BLADES("Weapon blades"),
   TOOL_PARTS("Tool parts"),
   FLETCHING("Fletching"),
   WEAPON_HEADS("Weapon heads"),
   RESOURCES("Resources"),
   ARMOUR("Armour"),
   SAILS("Sails"),
   CLOTHES("Clothes"),
   COOKING_UTENSILS("Cooking utensils"),
   DYES("Dyes"),
   HEALING("Healing"),
   LIGHTS_AND_LAMPS("Lights and lamps"),
   RUGS("Rugs"),
   FLAGS("Flags"),
   DECORATION("Decoration"),
   BAGS("Bags"),
   SHIELDS("Shields"),
   STATUETTES("Statuettes"),
   JEWELRY("Jewelry"),
   TOYS("Toys"),
   FOUNTAINS_AND_WELLS("Fountains and wells"),
   MINE_DOORS("Mine doors and support beams"),
   LOCKS("Locks"),
   ROPES("Ropes"),
   FOOD("Food"),
   ALTAR("Altar"),
   MAGIC("Magic"),
   EPIC("Epic"),
   FURNITURE("Furniture"),
   CARTS("Carts"),
   WARMACHINES("Warmachines"),
   SIGNS("Signs"),
   TOWERS("Towers"),
   MAILBOXES("Mailboxes"),
   TRAPS("Traps"),
   ANIMAL_EQUIPMENT("Animal equipment"),
   ANIMAL_EQUIPMENT_PART("Animal equipment parts"),
   ALCHEMY("Alchemy"),
   WRITING("Writing"),
   SHIPS("Ships"),
   CART_PARTS("Cart parts"),
   COMBAT_TRAINING("Combat training"),
   PRODUCTION("Production"),
   TENTS("Tents"),
   HIGHWAY("Highway");

   private final String categoryName;

   private CreationCategories(String categoryName) {
      this.categoryName = categoryName;
   }

   public final String getCategoryName() {
      return this.categoryName;
   }
}
