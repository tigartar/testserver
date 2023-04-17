/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.villages;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchRoleException
extends WurmServerException {
    private static final long serialVersionUID = -6630727392157751483L;

    public NoSuchRoleException(String message) {
        super(message);
    }

    public NoSuchRoleException(Throwable cause) {
        super(cause);
    }

    public NoSuchRoleException(String message, Throwable cause) {
        super(message, cause);
    }
}

