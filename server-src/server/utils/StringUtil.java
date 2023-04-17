/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.utils;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtil {
    private static final Logger logger = Logger.getLogger(StringUtil.class.getName());
    private static Locale locale = Locale.ENGLISH;

    public static String format(String format, Object ... args) {
        try {
            return String.format(locale, format, args);
        }
        catch (IllegalFormatException ife) {
            logger.log(Level.WARNING, format, ife);
            return "";
        }
    }

    public static String toLowerCase(String original) {
        return original.toLowerCase(locale);
    }

    public static String toLowerCase(Object obj) {
        if (obj == null) {
            return "";
        }
        return StringUtil.toLowerCase(obj.toString());
    }

    public static String toUpperCase(String original) {
        return original.toUpperCase(locale);
    }

    public static String toUpperCase(Object obj) {
        if (obj == null) {
            return "";
        }
        return StringUtil.toUpperCase(obj.toString());
    }
}

