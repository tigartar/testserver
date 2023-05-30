package com.wurmonline.server.players;

public class Friend implements Comparable<Friend> {
   private final long id;
   private final Friend.Category cat;
   private final String note;

   public Friend(long aId, byte catId, String note) {
      this(aId, Friend.Category.catFromInt(catId), note);
   }

   public Friend(long aId, Friend.Category category, String note) {
      this.id = aId;
      this.cat = category;
      this.note = note;
   }

   public long getFriendId() {
      return this.id;
   }

   public Friend.Category getCategory() {
      return this.cat;
   }

   public byte getCatId() {
      return this.cat.getCatId();
   }

   public String getName() {
      return PlayerInfoFactory.getPlayerName(this.id);
   }

   public String getNote() {
      return this.note;
   }

   public int compareTo(Friend otherFriend) {
      if (this.getCatId() < otherFriend.getCatId()) {
         return 1;
      } else {
         return this.getCatId() > otherFriend.getCatId() ? -1 : this.getName().compareTo(otherFriend.getName());
      }
   }

   public static enum Category {
      Other(0),
      Contacts(1),
      Friends(2),
      Trusted(3);

      private final byte cat;
      private static final Friend.Category[] cats = values();

      private Category(int numb) {
         this.cat = (byte)numb;
      }

      public byte getCatId() {
         return this.cat;
      }

      public static final int getCatLength() {
         return cats.length;
      }

      public static final Friend.Category[] getCategories() {
         return cats;
      }

      public static Friend.Category catFromInt(int typeId) {
         return typeId >= getCatLength() ? cats[0] : cats[typeId & 0xFF];
      }

      public static Friend.Category catFromName(String catName) {
         for(Friend.Category c : cats) {
            if (c.name().toLowerCase().startsWith(catName.toLowerCase())) {
               return c;
            }
         }

         return null;
      }
   }
}
