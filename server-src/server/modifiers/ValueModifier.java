/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.modifiers;

import com.wurmonline.server.modifiers.ModifierTypes;
import com.wurmonline.server.modifiers.ValueModifiedListener;
import java.util.HashSet;
import java.util.Set;

public abstract class ValueModifier
implements ModifierTypes {
    private final int type;
    private Set<ValueModifiedListener> listeners;

    ValueModifier() {
        this.type = 0;
    }

    ValueModifier(int typ) {
        this.type = typ;
    }

    public final int getType() {
        return this.type;
    }

    public final void addListener(ValueModifiedListener list) {
        if (this.listeners == null) {
            this.listeners = new HashSet<ValueModifiedListener>();
        }
        this.listeners.add(list);
    }

    public final void removeListener(ValueModifiedListener list) {
        if (this.listeners == null) {
            this.listeners = new HashSet<ValueModifiedListener>();
        } else {
            this.listeners.remove(list);
        }
    }

    Set<ValueModifiedListener> getListeners() {
        return this.listeners;
    }
}

