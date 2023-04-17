/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;

public final class IoUtilities {
    private IoUtilities() {
    }

    public static void closeClosable(Closeable closableObject) {
        if (closableObject != null) {
            try {
                closableObject.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public static void closeHttpURLConnection(HttpURLConnection httpURLConnection) {
        if (httpURLConnection != null) {
            try {
                httpURLConnection.disconnect();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

