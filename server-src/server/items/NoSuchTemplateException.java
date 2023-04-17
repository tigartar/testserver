/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchTemplateException
extends WurmServerException {
    private static final long serialVersionUID = 1157557174258373795L;

    public NoSuchTemplateException(String message) {
        super(message);
    }

    public NoSuchTemplateException(Throwable cause) {
        super(cause);
    }

    public NoSuchTemplateException(String message, Throwable cause) {
        super(message, cause);
    }
}

