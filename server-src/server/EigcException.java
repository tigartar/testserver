/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class EigcException
extends WurmServerException {
    private static final long serialVersionUID = 5813764704231108263L;

    public EigcException(String message) {
        super(message);
    }

    public EigcException(Throwable cause) {
        super(cause);
    }

    public EigcException(String message, Throwable cause) {
        super(message, cause);
    }
}

