/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSpaceException
extends WurmServerException {
    private static final long serialVersionUID = -7007492502695022234L;

    public NoSpaceException(String message) {
        super(message);
    }

    public NoSpaceException(Throwable cause) {
        super(cause);
    }

    public NoSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}

