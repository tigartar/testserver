/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

import java.io.InputStream;
import java.io.OutputStream;

public final class StreamUtilities {
    private StreamUtilities() {
    }

    public static void closeInputStreamIgnoreExceptions(InputStream aInputStream) {
        if (aInputStream != null) {
            try {
                aInputStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public static void closeOutputStreamIgnoreExceptions(OutputStream aOutputStream) {
        if (aOutputStream != null) {
            try {
                aOutputStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

