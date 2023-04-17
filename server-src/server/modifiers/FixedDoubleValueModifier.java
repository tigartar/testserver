/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.modifiers;

import com.wurmonline.server.modifiers.DoubleValueModifier;

public final class FixedDoubleValueModifier
extends DoubleValueModifier {
    public FixedDoubleValueModifier(double aValue) {
        super(aValue);
    }

    public FixedDoubleValueModifier(int aType, double aValue) {
        super(aType, aValue);
    }

    @Override
    public void setModifier(double aNewValue) {
        assert (false) : "Do not call FixedDoubleValueModifier.setModifier()";
        throw new IllegalArgumentException("Do not call FixedDoubleValueModifier.setModifier(). The modifier cannot be changed.");
    }
}

