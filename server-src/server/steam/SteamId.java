/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.steam;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamId {
    private long steamID64;
    private int accountNumber;
    private int accountInstance;
    private byte accountType;
    private byte accountUniverse;
    static Pattern idPattern = Pattern.compile("^STEAM_(?<x>\\d):(?<y>[01]):(?<z>\\d+)$");
    static Pattern id3Pattern = Pattern.compile("^\\[U:1:(?<w>\\d+)]$");
    static long uIdentifier = 0x110000100000000L;
    static long gIdentifier = 0x170000000000000L;

    SteamId() {
    }

    public static SteamId fromSteamID64(long steamID64) {
        SteamId id = new SteamId();
        id.accountNumber = (int)(0xFFFFFFFFL & steamID64);
        id.accountInstance = (int)((0xFFFFF00000000L & steamID64) >> 32);
        id.accountType = (byte)((0xF0000000000000L & steamID64) >> 52);
        id.accountUniverse = (byte)((0xFF00000000000000L & steamID64) >> 56);
        id.steamID64 = steamID64;
        return id;
    }

    public static SteamId fromSteamIDString(String steamIDString) {
        return SteamId.fromSteamIDString(steamIDString, true);
    }

    public static SteamId fromSteamIDString(String steamIDString, boolean individual) {
        Matcher m = idPattern.matcher(steamIDString);
        if (!m.matches() || m.groupCount() < 3) {
            return null;
        }
        int y = Integer.valueOf(m.group("y"));
        int z = Integer.valueOf(m.group("z"));
        return SteamId.fromSteamID64((long)(z * 2) + (individual ? uIdentifier : gIdentifier) + (long)y);
    }

    public static SteamId fromSteamID3String(String steamID3String) {
        Matcher m = id3Pattern.matcher(steamID3String);
        if (!m.matches()) {
            return null;
        }
        int w = Integer.valueOf(m.group("w"));
        return SteamId.fromSteamID64((long)w + uIdentifier);
    }

    public String steamIDString() {
        return String.format("STEAM_%d:%d:%d", this.accountUniverse, this.accountNumber & 1, this.accountNumber >> 1);
    }

    public String steamID3String() {
        return String.format("[U:1:%d]", (this.accountNumber >> 1) * 2 + (this.accountNumber & 1));
    }

    public long getSteamID64() {
        return this.steamID64;
    }

    public String toString() {
        return String.format("%d", this.steamID64);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof SteamId) {
            SteamId id = (SteamId)obj;
            return id.getSteamID64() == this.getSteamID64();
        }
        if (obj instanceof String) {
            String s = (String)obj;
            return this.steamID3String().equals(s) || this.steamIDString().equals(s) || this.toString().equals(s);
        }
        if (obj instanceof Long) {
            return ((Long)obj).longValue() == this.getSteamID64();
        }
        return false;
    }

    public static SteamId fromAnyString(String input) {
        long id64 = Long.valueOf(input);
        if (id64 != 0L) {
            return SteamId.fromSteamID64(id64);
        }
        SteamId id = SteamId.fromSteamIDString(input);
        if (id != null) {
            return id;
        }
        return SteamId.fromSteamID3String(input);
    }
}

