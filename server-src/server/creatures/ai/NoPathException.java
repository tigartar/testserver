/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures.ai;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoPathException
extends WurmServerException {
    private static final long serialVersionUID = 372320709229086812L;

    public NoPathException(String message) {
        super(message);
    }

    NoPathException(Throwable cause) {
        super(cause);
    }

    NoPathException(String message, Throwable cause) {
        super(message, cause);
    }
}

