/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.common;

import com.wurmonline.properties.Property;

public enum CommonProperties {
    VERSION("version", "build.properties", "UNKNOWN"),
    BUILD_TIME("build-time", "build.properties", "unknown"),
    COMMIT("git-sha-1", "build.properties", "unknown");

    Property property;

    private CommonProperties(String _key, String _file, String _default) {
        this.property = new Property(_key, CommonProperties.class.getResource(_file), _default);
    }

    public Property getProperty() {
        return this.property;
    }
}

