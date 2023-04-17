/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

public final class Point {
    private int px;
    private int py;
    private int ph;

    public Point(int x, int y) {
        this.px = x;
        this.py = y;
        this.ph = 0;
    }

    public Point(int x, int y, int h) {
        this.px = x;
        this.py = y;
        this.ph = h;
    }

    public Point(Point point) {
        this.px = point.px;
        this.py = point.py;
        this.ph = point.ph;
    }

    public int getX() {
        return this.px;
    }

    public void setX(int x) {
        this.px = x;
    }

    public int getY() {
        return this.py;
    }

    public void setY(int y) {
        this.py = y;
    }

    public int getH() {
        return this.ph;
    }

    public void setH(int h) {
        this.ph = h;
    }

    public void setXY(int x, int y) {
        this.px = x;
        this.py = y;
    }

    public void setXYH(int x, int y, int h) {
        this.px = x;
        this.py = y;
        this.ph = h;
    }
}

