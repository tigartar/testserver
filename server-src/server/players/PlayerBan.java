package com.wurmonline.server.players;

public class PlayerBan implements Ban {
   private String playerName;
   private String reason;
   private long expiry;

   public PlayerBan(String playerName, String reason, long expiry) {
      this.playerName = playerName;
      this.reason = reason;
      this.expiry = expiry;
   }

   @Override
   public boolean isExpired() {
      return System.currentTimeMillis() > this.getExpiry();
   }

   @Override
   public String getIdentifier() {
      return this.playerName;
   }

   @Override
   public String getReason() {
      return this.reason;
   }

   @Override
   public void setReason(String reason) {
      this.reason = reason;
   }

   @Override
   public long getExpiry() {
      return this.expiry;
   }

   @Override
   public void setExpiry(long expiry) {
      this.expiry = expiry;
   }
}
