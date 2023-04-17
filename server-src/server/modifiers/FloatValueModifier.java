/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.modifiers;

import com.wurmonline.server.modifiers.ValueModifier;

public final class FloatValueModifier
extends ValueModifier {
    private float modifier = 0.0f;

    public FloatValueModifier(float value) {
        this.modifier = value;
    }

    public FloatValueModifier(int aType, float value) {
        super(aType);
        this.modifier = value;
    }

    public float getModifier() {
        return this.modifier;
    }

    public void setModifier(float newValue) {
        this.modifier = newValue;
    }
}

