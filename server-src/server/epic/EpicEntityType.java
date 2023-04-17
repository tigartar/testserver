/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.epic;

enum EpicEntityType {
    TYPE_DEITY(0),
    TYPE_SOURCE(1),
    TYPE_COLLECT(2),
    TYPE_WURM(4),
    TYPE_MONSTER_SENTINEL(5),
    TYPE_ALLY(6),
    TYPE_DEMIGOD(7);

    private final int code;

    private EpicEntityType(int aCode) {
        this.code = aCode;
    }

    int getCode() {
        return this.code;
    }
}

