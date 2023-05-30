package com.wurmonline.server.utils;

import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringUtil {
   private static final Logger logger = Logger.getLogger(StringUtil.class.getName());
   private static Locale locale = Locale.ENGLISH;

   public static String format(String format, Object... args) {
      try {
         return String.format(locale, format, args);
      } catch (IllegalFormatException var3) {
         logger.log(Level.WARNING, format, (Throwable)var3);
         return "";
      }
   }

   public static String toLowerCase(String original) {
      return original.toLowerCase(locale);
   }

   public static String toLowerCase(Object obj) {
      return obj == null ? "" : toLowerCase(obj.toString());
   }

   public static String toUpperCase(String original) {
      return original.toUpperCase(locale);
   }

   public static String toUpperCase(Object obj) {
      return obj == null ? "" : toUpperCase(obj.toString());
   }
}
