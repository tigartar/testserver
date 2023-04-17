/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.players.Ban;

public final class IPBan
implements Ban {
    private final String identifier;
    private String reason;
    private long expiry;
    private static final String ADD_BANNED_IP = "insert into BANNEDIPS (IPADDRESS,BANREASON,BANEXPIRY) values(?,?,?)";
    private static final String UPDATE_BANNED_IP = "UPDATE BANNEDIPS SET BANREASON=?,BANEXPIRY=? WHERE IPADDRESS=?";
    private static final String GET_BANNED_IPS = "select * from BANNEDIPS";
    private static final String REMOVE_BANNED_IP = "delete from BANNEDIPS where IPADDRESS=?";

    public IPBan(String _identifier, String _reason, long _expiry) {
        this.identifier = _identifier;
        this.setReason(_reason);
        this.setExpiry(_expiry);
    }

    @Override
    public boolean isExpired() {
        return System.currentTimeMillis() > this.getExpiry();
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
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

    @Override
    public String getUpdateSql() {
        return UPDATE_BANNED_IP;
    }

    @Override
    public String getInsertSql() {
        return ADD_BANNED_IP;
    }

    @Override
    public String getDeleteSql() {
        return REMOVE_BANNED_IP;
    }

    public static String getSelectSql() {
        return GET_BANNED_IPS;
    }
}

