/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchTileException
extends WurmServerException {
    private static final long serialVersionUID = -974263427293465936L;

    NoSuchTileException(String message) {
        super(message);
    }

    NoSuchTileException(Throwable cause) {
        super(cause);
    }

    NoSuchTileException(String message, Throwable cause) {
        super(message, cause);
    }
}

