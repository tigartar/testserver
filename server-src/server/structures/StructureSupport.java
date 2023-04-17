/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

public interface StructureSupport {
    public boolean supports(StructureSupport var1);

    public int getFloorLevel();

    public int getStartX();

    public int getStartY();

    public int getMinX();

    public int getMinY();

    public boolean isHorizontal();

    public boolean isFloor();

    public boolean isFence();

    public boolean isWall();

    public int getEndX();

    public int getEndY();

    public boolean isSupportedByGround();

    public boolean supports();

    public String getName();

    public long getId();

    public boolean equals(StructureSupport var1);
}

