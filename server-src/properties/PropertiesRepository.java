/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public enum PropertiesRepository {
    INSTANCE;

    private static final Logger logger;
    private static final HashMap<URL, Properties> propertiesHashMap;

    public static PropertiesRepository getInstance() {
        return INSTANCE;
    }

    Properties getProperties(URL file) {
        if (propertiesHashMap.containsKey(file)) {
            return propertiesHashMap.get(file);
        }
        Properties properties = new Properties();
        propertiesHashMap.put(file, properties);
        try (InputStream is = file.openStream();){
            properties.load(is);
        }
        catch (IOException e) {
            logger.warning("Unable to open properties file " + file.toString());
        }
        return properties;
    }

    public String getValueFor(URL file, String key) {
        return this.getProperties(file).getProperty(key);
    }

    static {
        logger = Logger.getLogger(PropertiesRepository.class.getName());
        propertiesHashMap = new HashMap();
    }
}

