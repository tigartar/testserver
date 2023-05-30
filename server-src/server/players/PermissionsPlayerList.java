package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlayerList implements MiscConstants {
   private Map<Long, PermissionsByPlayer> playerPermissions = new ConcurrentHashMap<>();

   public void remove(long aPlayerId) {
      this.playerPermissions.remove(aPlayerId);
   }

   public boolean isEmpty() {
      return this.playerPermissions.isEmpty();
   }

   public int size() {
      return this.playerPermissions.size();
   }

   public PermissionsByPlayer[] getPermissionsByPlayer() {
      return this.playerPermissions.values().toArray(new PermissionsByPlayer[this.playerPermissions.size()]);
   }

   public PermissionsByPlayer add(long aPlayerId, int aPermissions) {
      return this.playerPermissions.put(aPlayerId, new PermissionsByPlayer(aPlayerId, aPermissions));
   }

   public boolean hasPermission(long playerId, int bit) {
      Long id = playerId;
      PermissionsByPlayer playerPerm = this.playerPermissions.get(id);
      return playerPerm == null ? false : playerPerm.hasPermission(bit);
   }

   public PermissionsByPlayer getPermissionsByPlayer(long playerId) {
      Long id = playerId;
      return this.playerPermissions.get(id);
   }

   public Permissions getPermissionsFor(long playerId) {
      Long id = playerId;
      PermissionsByPlayer playerPerm = this.playerPermissions.get(id);
      if (playerPerm == null) {
         PermissionsByPlayer everyone = this.playerPermissions.get(-10L);
         return everyone == null ? new Permissions() : everyone.getPermissions();
      } else {
         return playerPerm.getPermissions();
      }
   }

   public boolean exists(long playerId) {
      return this.playerPermissions.containsKey(playerId);
   }

   public interface ISettings {
      void addDefaultCitizenPermissions();

      void addGuest(long var1, int var3);

      boolean canAllowEveryone();

      boolean canChangeOwner(Creature var1);

      boolean canChangeName(Creature var1);

      boolean canHavePermissions();

      String getAllianceName();

      String getKingdomName();

      int getMaxAllowed();

      String getObjectName();

      String getOwnerName();

      PermissionsPlayerList getPermissionsPlayerList();

      String getRolePermissionName();

      String getSettlementName();

      String getTypeName();

      String getWarning();

      long getWurmId();

      int getTemplateId();

      boolean isActualOwner(long var1);

      boolean isAllied(Creature var1);

      boolean isCitizen(Creature var1);

      boolean isGuest(Creature var1);

      boolean isGuest(long var1);

      boolean isManaged();

      boolean isManageEnabled(Player var1);

      boolean isOwner(Creature var1);

      boolean isOwner(long var1);

      boolean isSameKingdom(Creature var1);

      String mayManageHover(Player var1);

      String mayManageText(Player var1);

      boolean mayShowPermissions(Creature var1);

      String messageOnTick();

      String messageUnTick();

      String questionOnTick();

      String questionUnTick();

      void removeGuest(long var1);

      void save() throws IOException;

      void setIsManaged(boolean var1, Player var2);

      boolean setNewOwner(long var1);

      boolean setObjectName(String var1, Creature var2);

      boolean isItem();
   }
}
