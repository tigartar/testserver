/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.Player;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionsPlayerList
implements MiscConstants {
    private Map<Long, PermissionsByPlayer> playerPermissions = new ConcurrentHashMap<Long, PermissionsByPlayer>();

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
        if (playerPerm == null) {
            return false;
        }
        return playerPerm.hasPermission(bit);
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
            if (everyone == null) {
                return new Permissions();
            }
            return everyone.getPermissions();
        }
        return playerPerm.getPermissions();
    }

    public boolean exists(long playerId) {
        return this.playerPermissions.containsKey(playerId);
    }

    public static interface ISettings {
        public void addDefaultCitizenPermissions();

        public void addGuest(long var1, int var3);

        public boolean canAllowEveryone();

        public boolean canChangeOwner(Creature var1);

        public boolean canChangeName(Creature var1);

        public boolean canHavePermissions();

        public String getAllianceName();

        public String getKingdomName();

        public int getMaxAllowed();

        public String getObjectName();

        public String getOwnerName();

        public PermissionsPlayerList getPermissionsPlayerList();

        public String getRolePermissionName();

        public String getSettlementName();

        public String getTypeName();

        public String getWarning();

        public long getWurmId();

        public int getTemplateId();

        public boolean isActualOwner(long var1);

        public boolean isAllied(Creature var1);

        public boolean isCitizen(Creature var1);

        public boolean isGuest(Creature var1);

        public boolean isGuest(long var1);

        public boolean isManaged();

        public boolean isManageEnabled(Player var1);

        public boolean isOwner(Creature var1);

        public boolean isOwner(long var1);

        public boolean isSameKingdom(Creature var1);

        public String mayManageHover(Player var1);

        public String mayManageText(Player var1);

        public boolean mayShowPermissions(Creature var1);

        public String messageOnTick();

        public String messageUnTick();

        public String questionOnTick();

        public String questionUnTick();

        public void removeGuest(long var1);

        public void save() throws IOException;

        public void setIsManaged(boolean var1, Player var2);

        public boolean setNewOwner(long var1);

        public boolean setObjectName(String var1, Creature var2);

        public boolean isItem();
    }
}

