/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchZoneException
extends WurmServerException {
    private static final long serialVersionUID = 7094119477458750028L;

    NoSuchZoneException(String message) {
        super(message);
    }

    NoSuchZoneException(Throwable cause) {
        super(cause);
    }

    NoSuchZoneException(String message, Throwable cause) {
        super(message, cause);
    }
}

