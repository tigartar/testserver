package com.wurmonline.shared.constants;

public enum PlayerOnlineStatus {
   ONLINE(1, "online"),
   OTHER_SERVER(2, "other server"),
   LOST_LINK(3, "lost link"),
   OFFLINE(0, "offline"),
   DELETE_ME(4, ""),
   UNKNOWN(-1, "unknown");

   private final byte id;
   private final String name;
   private static final PlayerOnlineStatus[] types = values();

   private PlayerOnlineStatus(int aId, String aName) {
      this.id = (byte)aId;
      this.name = aName;
   }

   public byte getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public static PlayerOnlineStatus playerOnlineStatusFromId(byte aId) {
      for(int i = 0; i < types.length; ++i) {
         if (aId == types[i].getId()) {
            return types[i];
         }
      }

      return DELETE_ME;
   }
}
