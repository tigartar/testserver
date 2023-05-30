package com.wurmonline.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public enum PropertiesRepository {
   INSTANCE;

   private static final Logger logger = Logger.getLogger(PropertiesRepository.class.getName());
   private static final HashMap<URL, Properties> propertiesHashMap = new HashMap<>();

   public static PropertiesRepository getInstance() {
      return INSTANCE;
   }

   Properties getProperties(URL file) {
      if (propertiesHashMap.containsKey(file)) {
         return propertiesHashMap.get(file);
      } else {
         Properties properties = new Properties();
         propertiesHashMap.put(file, properties);

         try (InputStream is = file.openStream()) {
            properties.load(is);
         } catch (IOException var16) {
            logger.warning("Unable to open properties file " + file.toString());
         }

         return properties;
      }
   }

   public String getValueFor(URL file, String key) {
      return this.getProperties(file).getProperty(key);
   }
}
