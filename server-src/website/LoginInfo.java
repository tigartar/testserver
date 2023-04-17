/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.website;

public class LoginInfo {
    private String name;

    public LoginInfo(String aName) {
        this.name = aName;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAdmin() {
        return this.name.equals("admin");
    }
}

