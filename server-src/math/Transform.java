/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.math;

import com.wurmonline.math.Quaternion;
import com.wurmonline.math.Vector3f;

public class Transform {
    public final Quaternion rotation = new Quaternion();
    public final Vector3f translation = new Vector3f();

    public final void identity() {
        this.rotation.identity();
        this.translation.zero();
    }
}

