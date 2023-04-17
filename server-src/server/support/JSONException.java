/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

public class JSONException
extends RuntimeException {
    private static final long serialVersionUID = 0L;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}

