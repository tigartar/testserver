package com.wurmonline.properties;

import java.net.URL;
import java.util.logging.Logger;

public class Property {
   private static final Logger logger = Logger.getLogger(Property.class.getName());
   private final String key;
   private final URL file;
   private final String defaultValue;
   private boolean isLoaded = false;
   private String value;

   public Property(String _key, URL _file, String _default) {
      this.key = _key;
      this.file = _file;
      if (this.file == null) {
         this.value = _default;
         this.isLoaded = true;
      }

      this.defaultValue = _default;
   }

   private void fetch() {
      this.value = PropertiesRepository.getInstance().getValueFor(this.file, this.key);
      if (this.value == null || this.value.isEmpty() || this.value.startsWith("${") && this.value.endsWith("}")) {
         this.value = this.defaultValue;
      }

      this.isLoaded = true;
   }

   public String getValue() {
      if (!this.isLoaded) {
         this.fetch();
      }

      return this.value;
   }

   public final int getIntValue() {
      try {
         return Integer.parseInt(this.getValue());
      } catch (NumberFormatException var2) {
         logger.warning("Unable to get integer value from " + this.getValue());
         return 0;
      }
   }

   public final boolean getBooleanValue() {
      return Boolean.parseBoolean(this.getValue());
   }
}
