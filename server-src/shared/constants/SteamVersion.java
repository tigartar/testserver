/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SteamVersion {
    public static final String UNKNOWN_VERSION = "UNKNOWN";
    static final int MAJOR = 1;
    static final int COMPATIBILITY = 9;
    static final int CLIENT = 1;
    static final int SERVER = 5;
    private static final SteamVersion current = new SteamVersion(1, 9, 1, 5);
    private static final String patternString = "^(?:version=)?(?<major>\\d+)\\.(?<compatibility>\\d+)\\.(?<client>\\d+)\\.(?<server>\\d+);?$";
    private static final Pattern versionPattern = Pattern.compile("^(?:version=)?(?<major>\\d+)\\.(?<compatibility>\\d+)\\.(?<client>\\d+)\\.(?<server>\\d+);?$");
    private int major = 0;
    private int compatibility = 0;
    private int client = 0;
    private int server = 0;

    public static SteamVersion getCurrentVersion() {
        return current;
    }

    public SteamVersion(String version) {
        if (version == null) {
            return;
        }
        Matcher matcher = versionPattern.matcher(version);
        if (matcher.matches()) {
            this.major = Integer.valueOf(matcher.group("major"));
            this.compatibility = Integer.valueOf(matcher.group("compatibility"));
            this.client = Integer.valueOf(matcher.group("client"));
            this.server = Integer.valueOf(matcher.group("server"));
        }
    }

    SteamVersion(int major, int compatibility, int client, int server) {
        this.major = major;
        this.compatibility = compatibility;
        this.client = client;
        this.server = server;
    }

    public boolean isCompatibleWith(SteamVersion version) {
        return this.major == version.major && this.compatibility == version.compatibility;
    }

    public String getTag() {
        return "version=" + this.toString() + ";";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SteamVersion version = (SteamVersion)o;
        return this.major == version.major && this.compatibility == version.compatibility && this.client == version.client && this.server == version.server;
    }

    public int hashCode() {
        return Objects.hash(this.major, this.compatibility, this.client, this.server);
    }

    public String toString() {
        return String.format("%d.%d.%d.%d", this.major, this.compatibility, this.client, this.server);
    }

    public boolean isCompatibleWith(String version) {
        return this.isCompatibleWith(new SteamVersion(version));
    }
}

