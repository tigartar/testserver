package com.wurmonline.server.players;

import com.wurmonline.server.steam.SteamId;

public interface Ban {
   boolean isExpired();

   String getIdentifier();

   String getReason();

   void setReason(String var1);

   long getExpiry();

   void setExpiry(long var1);

   default String getUpdateSql() {
      return "";
   }

   default String getInsertSql() {
      return "";
   }

   default String getDeleteSql() {
      return "";
   }

   static Ban fromString(String identifier) {
      return fromString(identifier, "", 0L);
   }

   static Ban fromString(String identifier, String reason, long expiry) {
      SteamId id = SteamId.fromAnyString(identifier);
      return (Ban)(id != null ? new SteamIdBan(id, reason, expiry) : new IPBan(identifier, reason, expiry));
   }
}
