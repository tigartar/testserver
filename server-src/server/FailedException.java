/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class FailedException
extends WurmServerException {
    private static final long serialVersionUID = 3728193914548210778L;

    public FailedException(String message) {
        super(message);
    }

    public FailedException(Throwable cause) {
        super(cause);
    }

    public FailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

