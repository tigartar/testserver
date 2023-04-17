/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

public final class Point4f {
    private float posx;
    private float posy;
    private float posz;
    private float rot;

    public Point4f() {
        this.posx = 0.0f;
        this.posy = 0.0f;
        this.posz = 0.0f;
        this.rot = 0.0f;
    }

    public Point4f(float posx, float posy) {
        this.posx = posx;
        this.posy = posy;
        this.posz = 0.0f;
        this.rot = 0.0f;
    }

    public Point4f(float posx, float posy, float posz) {
        this.posx = posx;
        this.posy = posy;
        this.posz = posz;
        this.rot = 0.0f;
    }

    public Point4f(float posx, float posy, float posz, float rot) {
        this.posx = posx;
        this.posy = posy;
        this.posz = posz;
        this.rot = rot;
    }

    public Point4f(Point4f point) {
        this.posx = point.posx;
        this.posy = point.posy;
        this.posz = point.posz;
        this.rot = point.rot;
    }

    public float getPosX() {
        return this.posx;
    }

    public void setPosX(float posx) {
        this.posx = posx;
    }

    public float getPosY() {
        return this.posy;
    }

    public void setPosY(float posy) {
        this.posy = posy;
    }

    public float getPosZ() {
        return this.posz;
    }

    public void setPosZ(float posz) {
        this.posz = posz;
    }

    public float getRot() {
        return this.rot;
    }

    public void setRot(float rot) {
        this.rot = rot;
    }

    public void setXY(float posx, float posy) {
        this.posx = posx;
        this.posy = posy;
    }

    public void setXYZ(float posx, float posy, float posz) {
        this.posx = posx;
        this.posy = posy;
        this.posz = posz;
    }

    public void setXYR(float posx, float posy, float rot) {
        this.posx = posx;
        this.posy = posy;
        this.rot = rot;
    }

    public void setXYZR(float posx, float posy, float posz, float rot) {
        this.posx = posx;
        this.posy = posy;
        this.posz = posz;
        this.rot = rot;
    }

    public int getTileX() {
        return (int)this.posx >> 2;
    }

    public int getTileY() {
        return (int)this.posy >> 2;
    }
}

