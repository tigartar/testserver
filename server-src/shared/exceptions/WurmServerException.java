/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.exceptions;

import com.wurmonline.shared.exceptions.WurmException;

public class WurmServerException
extends WurmException {
    private static final long serialVersionUID = 1268608703615765075L;

    public WurmServerException(String message) {
        super(message);
    }

    public WurmServerException(Throwable cause) {
        super(cause);
    }

    public WurmServerException(String message, Throwable cause) {
        super(message, cause);
    }
}

