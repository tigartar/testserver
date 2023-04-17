/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.modifiers;

import com.wurmonline.server.modifiers.ValueModifier;

public final class IntValueModifier
extends ValueModifier {
    private int modifier = 0;

    public IntValueModifier(int value) {
        this.modifier = value;
    }

    public IntValueModifier(int aType, int value) {
        super(aType);
        this.modifier = value;
    }

    public int getModifier() {
        return this.modifier;
    }

    public void setModifier(int newValue) {
        this.modifier = newValue;
    }
}

