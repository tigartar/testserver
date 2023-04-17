/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.modifiers;

import com.wurmonline.server.modifiers.ValueModifiedListener;
import com.wurmonline.server.modifiers.ValueModifier;

public class DoubleValueModifier
extends ValueModifier {
    private double modifier = 0.0;

    public DoubleValueModifier(double value) {
        this.modifier = value;
    }

    public DoubleValueModifier(int aType, double value) {
        super(aType);
        this.modifier = value;
    }

    public double getModifier() {
        return this.modifier;
    }

    public void setModifier(double newValue) {
        this.modifier = newValue;
        if (this.getListeners() != null) {
            for (ValueModifiedListener list : this.getListeners()) {
                list.valueChanged(this.modifier, newValue);
            }
        }
    }
}

