/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.players.IPBan;
import com.wurmonline.server.players.SteamIdBan;
import com.wurmonline.server.steam.SteamId;

public interface Ban {
    public boolean isExpired();

    public String getIdentifier();

    public String getReason();

    public void setReason(String var1);

    public long getExpiry();

    public void setExpiry(long var1);

    default public String getUpdateSql() {
        return "";
    }

    default public String getInsertSql() {
        return "";
    }

    default public String getDeleteSql() {
        return "";
    }

    public static Ban fromString(String identifier) {
        return Ban.fromString(identifier, "", 0L);
    }

    public static Ban fromString(String identifier, String reason, long expiry) {
        SteamId id = SteamId.fromAnyString(identifier);
        if (id != null) {
            return new SteamIdBan(id, reason, expiry);
        }
        return new IPBan(identifier, reason, expiry);
    }
}

