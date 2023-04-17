/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui;

public enum GuiCommandLineArgument {
    START("start"),
    QUERY_PORT("queryport"),
    INTERNAL_PORT("internalport"),
    EXTERNAL_PORT("externalport"),
    IP_ADDR("ip"),
    RMI_REG("rmiregport"),
    RMI_PORT("rmiport"),
    SERVER_PASS("serverpassword"),
    PLAYER_NUM("maxplayers"),
    LOGIN_SERVER("loginserver"),
    PVP("pvp"),
    HOME_SERVER("homeserver"),
    HOME_KINGDOM("homekingdom"),
    EPIC_SETTINGS("epicsettings"),
    SERVER_NAME("servername"),
    ADMIN_PWD("adminpwd");

    private final String argString;

    private GuiCommandLineArgument(String arg) {
        this.argString = arg;
    }

    public String getArgumentString() {
        return this.argString;
    }
}

