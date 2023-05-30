package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import java.io.IOException;
import java.util.logging.Logger;

public class PermissionsByPlayer implements MiscConstants, Comparable<PermissionsByPlayer> {
   private static Logger logger = Logger.getLogger(PermissionsByPlayer.class.getName());
   private long id;
   private Permissions permissions;

   PermissionsByPlayer(long aPlayerId, int aSettings) {
      this.id = aPlayerId;
      this.permissions = new Permissions();
      this.permissions.setPermissionBits(aSettings);
   }

   public long getPlayerId() {
      return this.id;
   }

   Permissions getPermissions() {
      return this.permissions;
   }

   public boolean hasPermission(int bit) {
      return this.permissions.hasPermission(bit);
   }

   public int getSettings() {
      return this.permissions.getPermissions();
   }

   public String getName() {
      return getPlayerOrGroupName(this.id);
   }

   public int compareTo(PermissionsByPlayer pbp) {
      return this.getName().compareTo(pbp.getName());
   }

   public static String getPlayerOrGroupName(long playerOrGroupId) {
      try {
         if (playerOrGroupId == -20L) {
            return "Allies";
         } else if (playerOrGroupId == -30L) {
            return "Citizens";
         } else if (playerOrGroupId == -40L) {
            return "Kingdom";
         } else if (playerOrGroupId == -50L) {
            return "Everyone";
         } else {
            return playerOrGroupId == -60L ? "Brand Group" : Players.getInstance().getNameFor(playerOrGroupId);
         }
      } catch (IOException | NoSuchPlayerException var3) {
         return "Unknown";
      }
   }
}
