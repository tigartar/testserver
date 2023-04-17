/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Permissions;
import java.io.IOException;
import java.util.logging.Logger;

public class PermissionsByPlayer
implements MiscConstants,
Comparable<PermissionsByPlayer> {
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
        return PermissionsByPlayer.getPlayerOrGroupName(this.id);
    }

    @Override
    public int compareTo(PermissionsByPlayer pbp) {
        return this.getName().compareTo(pbp.getName());
    }

    public static String getPlayerOrGroupName(long playerOrGroupId) {
        try {
            if (playerOrGroupId == -20L) {
                return "Allies";
            }
            if (playerOrGroupId == -30L) {
                return "Citizens";
            }
            if (playerOrGroupId == -40L) {
                return "Kingdom";
            }
            if (playerOrGroupId == -50L) {
                return "Everyone";
            }
            if (playerOrGroupId == -60L) {
                return "Brand Group";
            }
            return Players.getInstance().getNameFor(playerOrGroupId);
        }
        catch (NoSuchPlayerException | IOException e) {
            return "Unknown";
        }
    }
}

