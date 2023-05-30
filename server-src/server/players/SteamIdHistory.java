package com.wurmonline.server.players;

import com.wurmonline.server.steam.SteamId;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SteamIdHistory {
   private int id;
   private long playerId;
   private SteamId steamId;
   private long firstUsed;
   private long lastUsed;

   public SteamIdHistory(long playerId, SteamId steamId, long firstUsed, long lastUsed) {
      this.playerId = playerId;
      this.steamId = steamId;
      this.firstUsed = firstUsed;
      this.lastUsed = lastUsed;
   }

   public SteamIdHistory(ResultSet rs) throws SQLException {
      this.id = rs.getInt("ID");
      this.playerId = rs.getLong("PLAYER_ID");
      this.steamId = SteamId.fromSteamID64(Long.valueOf(rs.getLong("STEAM_ID")));
      this.firstUsed = rs.getLong("FIRST_USED");
      this.lastUsed = rs.getLong("LAST_USED");
   }

   public int getId() {
      return this.id;
   }

   public long getPlayerId() {
      return this.playerId;
   }

   public SteamId getSteamId() {
      return this.steamId;
   }

   public long getFirstUsed() {
      return this.firstUsed;
   }

   public long getLastUsed() {
      return this.lastUsed;
   }

   public void setLastUsed(long lastUsed) {
      this.lastUsed = lastUsed;
   }
}
