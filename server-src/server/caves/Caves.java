/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.caves;

final class Caves {
    private static final float MAX_CAVE_SLOPE = 8.0f;

    public void digHoleAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
        int y;
        int x;
        if (!this.isRockExposed(xTarget, yTarget)) {
            System.out.println("You can't mine an entrance here.. There's too much dirt on the tile.");
            return;
        }
        if (!this.isCaveWall(xFrom, yFrom)) {
            System.out.println("You can't mine an entrance here.. There's a tunnel in the way.");
            return;
        }
        for (x = xTarget - 1; x <= xTarget + 1; ++x) {
            for (y = yTarget - 1; y <= yTarget + 1; ++y) {
                if (!this.isTerrainHole(x, y)) continue;
                System.out.println("You can't mine an entrance here.. Too close to an existing entrance.");
                return;
            }
        }
        if (this.isMinable(xTarget, yTarget)) {
            for (x = xFrom; x <= xFrom + 1; ++x) {
                for (y = yFrom; y <= yFrom + 1; ++y) {
                }
            }
            this.mine(xFrom, yFrom, xTarget, yTarget, slope);
        }
    }

    public void mineAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
        if (!this.isMinable(xTarget, yTarget)) {
            System.out.println("You can't mine here.. There's a tunnel in the way.");
            return;
        }
        if (!this.isCaveWall(xTarget, xTarget)) {
            this.mine(xFrom, yFrom, xTarget, yTarget, slope);
        }
    }

    private void mine(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
    }

    private boolean isMinable(int xTarget, int yTarget) {
        float lowestFloor = 100000.0f;
        float highestFloor = -100000.0f;
        int tunnels = 0;
        for (int x = xTarget; x <= xTarget + 1; ++x) {
            for (int y = yTarget; y <= yTarget + 1; ++y) {
                float h;
                if (this.isExitCorner(x, y)) {
                    ++tunnels;
                    h = this.getTerrainHeight(x, y);
                    if (h < lowestFloor) {
                        lowestFloor = h;
                    }
                    if (!(h > highestFloor)) continue;
                    highestFloor = h;
                    continue;
                }
                if (!this.isTunnelCorner(x, y)) continue;
                ++tunnels;
                h = this.getCaveFloorHeight(x, y);
                if (h < lowestFloor) {
                    lowestFloor = h;
                }
                if (!(h > highestFloor)) continue;
                highestFloor = h;
            }
        }
        if (tunnels == 0) {
            return true;
        }
        float diff = highestFloor - lowestFloor;
        return diff < 8.0f;
    }

    private boolean isTunnelCorner(int x, int y) {
        if (this.isCaveTunnel(x, y)) {
            return true;
        }
        if (this.isCaveTunnel(x - 1, y)) {
            return true;
        }
        if (this.isCaveTunnel(x - 1, y - 1)) {
            return true;
        }
        return this.isCaveTunnel(x, y - 1);
    }

    private boolean isExitCorner(int x, int y) {
        if (this.isCaveExit(x, y)) {
            return true;
        }
        if (this.isCaveExit(x - 1, y)) {
            return true;
        }
        if (this.isCaveExit(x - 1, y - 1)) {
            return true;
        }
        return this.isCaveExit(x, y - 1);
    }

    private float getTerrainHeight(int x, int y) {
        return 10.0f;
    }

    private float getCaveFloorHeight(int x, int y) {
        return 10.0f;
    }

    private boolean isCaveExit(int x, int y) {
        return true;
    }

    private boolean isCaveTunnel(int x, int y) {
        return true;
    }

    private boolean isCaveWall(int x, int y) {
        return true;
    }

    private boolean isTerrainHole(int x, int y) {
        return false;
    }

    private boolean isRockExposed(int x, int y) {
        return true;
    }
}

