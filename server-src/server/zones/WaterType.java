/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.server.Constants;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import java.util.logging.Logger;

public class WaterType
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WaterType.class.getName());
    public static final byte NOT_WATER = 0;
    public static final byte WATER = 1;
    public static final byte POND = 2;
    public static final byte LAKE = 3;
    public static final byte SEA = 4;
    public static final byte SEA_SHALLOWS = 5;
    public static final byte LAKE_SHALLOWS = 6;
    public static final byte SHALLOWS = 7;
    static final byte LAKE_RADIUS = 7;
    static final byte POND_RADIUS = 2;
    static final byte SHALLOWS_DEPTH = -25;
    static final byte[][] waterSurface = new byte[1 << Constants.meshSize][1 << Constants.meshSize];
    static final byte[][] waterCave = new byte[1 << Constants.meshSize][1 << Constants.meshSize];

    private WaterType() {
    }

    public static final void calcWaterTypes() {
        long start = System.nanoTime();
        int max = Server.surfaceMesh.getSize() - 1;
        WaterType.surfaceByDepth(waterSurface, (byte)0, 0, true, (byte)1);
        WaterType.setBorder(waterSurface, (byte)1, (byte)2);
        WaterType.fill(waterSurface, (byte)1, (byte)2);
        WaterType.surfaceByDepth(waterSurface, (byte)2, -25, false, (byte)5);
        WaterType.setBorder(waterSurface, (byte)2, (byte)4);
        WaterType.fill(waterSurface, (byte)2, (byte)4);
        WaterType.setTypeByArea(waterSurface, 7, (byte)1, (byte)3);
        WaterType.fill(waterSurface, (byte)1, (byte)3);
        WaterType.setTypeByArea(waterSurface, 7, (byte)2, (byte)3);
        WaterType.fill(waterSurface, (byte)2, (byte)3);
        WaterType.surfaceByDepth(waterSurface, (byte)3, -25, false, (byte)6);
        WaterType.postProcessShallows(waterSurface);
        WaterType.setTypeByArea(waterSurface, 2, (byte)1, (byte)2);
        WaterType.fill(waterSurface, (byte)1, (byte)2);
        WaterType.caveByDepth(waterCave, (byte)0, 0, true, (byte)1);
        WaterType.setTypeByArea(waterCave, 7, (byte)1, (byte)3);
        WaterType.fill(waterCave, (byte)1, (byte)3);
        WaterType.setTypeByArea(waterCave, 2, (byte)1, (byte)2);
        WaterType.fill(waterCave, (byte)1, (byte)2);
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.info("Calculated water types, size: " + max + " took " + lElapsedTime + " ms");
    }

    private static final void fill(byte[][] map, byte checkType, byte setType) {
        int max = Server.surfaceMesh.getSize() - 1;
        boolean looping = true;
        while (looping) {
            int y;
            int x;
            looping = false;
            for (x = 1; x < max - 1; ++x) {
                for (y = 1; y < max - 1; ++y) {
                    looping = WaterType.fillTile(map, x, y, checkType, setType, looping);
                }
            }
            for (x = max - 1; x > 1; --x) {
                for (y = 1; y < max - 1; ++y) {
                    looping = WaterType.fillTile(map, x, y, checkType, setType, looping);
                }
            }
            for (x = 1; x < max - 1; ++x) {
                for (y = max - 1; y > 1; --y) {
                    looping = WaterType.fillTile(map, x, y, checkType, setType, looping);
                }
            }
            for (x = max - 1; x > 1; --x) {
                for (y = max - 1; y > 1; --y) {
                    looping = WaterType.fillTile(map, x, y, checkType, setType, looping);
                }
            }
        }
    }

    private static final boolean fillTile(byte[][] map, int x, int y, byte checkType, byte setType, boolean looping) {
        if (map[x][y] == checkType && (map[x - 1][y] == setType || map[x + 1][y] == setType || map[x][y - 1] == setType || map[x][y + 1] == setType)) {
            map[x][y] = setType;
            return true;
        }
        return looping;
    }

    private static final void surfaceByDepth(byte[][] map, byte checkType, int depth, boolean under, byte setType) {
        int max = Server.surfaceMesh.getSize() - 1;
        for (int x = 0; x <= max; ++x) {
            for (int y = 0; y <= max; ++y) {
                MeshTile mt;
                if (map[x][y] != checkType || (mt = new MeshTile(Server.surfaceMesh, x, y)).isUnder(depth) != under) continue;
                map[x][y] = setType;
            }
        }
    }

    private static final void caveByDepth(byte[][] map, byte checkType, int depth, boolean under, byte setType) {
        int max = Server.caveMesh.getSize() - 1;
        for (int x = 0; x <= max; ++x) {
            for (int y = 0; y <= max; ++y) {
                MeshTile mt = new MeshTile(Server.caveMesh, x, y);
                if (map[x][y] != checkType || mt.isUnder(depth) != under) continue;
                map[x][y] = setType;
            }
        }
    }

    private static final void setBorder(byte[][] map, byte checkType, byte setType) {
        int max = Server.surfaceMesh.getSize() - 1;
        for (int i = 0; i <= max; ++i) {
            if (map[i][0] == checkType) {
                map[i][0] = setType;
                continue;
            }
            if (map[i][max] == checkType) {
                map[i][max] = setType;
                continue;
            }
            if (map[0][i] == checkType) {
                map[0][i] = setType;
                continue;
            }
            if (map[max][i] != checkType) continue;
            map[max][i] = setType;
        }
    }

    private static final void setTypeByArea(byte[][] map, int radius, byte checkType, byte setType) {
        int max = Server.surfaceMesh.getSize() - 1;
        for (int x = radius; x <= max - radius; ++x) {
            for (int y = radius; y <= max - radius; ++y) {
                boolean ok = map[x][y] == checkType;
                for (int i = 1; i < radius && ok; ++i) {
                    if (map[x - i][y] != checkType) {
                        ok = false;
                        continue;
                    }
                    if (map[x + i][y] != checkType) {
                        ok = false;
                        continue;
                    }
                    if (map[x][y - i] != checkType) {
                        ok = false;
                        continue;
                    }
                    if (map[x][y + i] == checkType) continue;
                    ok = false;
                }
                if (!ok) continue;
                map[x][y] = setType;
            }
        }
    }

    private static final void postProcessShallows(byte[][] map) {
        int max = Server.surfaceMesh.getSize() - 1;
        for (int x = 0; x <= max; ++x) {
            for (int y = 0; y <= max; ++y) {
                if (map[x][y] != 5) continue;
                byte closest = 5;
                for (int ii = 1; ii < max && closest == 5; ++ii) {
                    closest = WaterType.checkShallows(map, x, y, ii, closest, (byte)4);
                    closest = WaterType.checkShallows(map, x, y, ii, closest, (byte)3);
                }
                if (closest != 3) continue;
                map[x][y] = 6;
            }
        }
    }

    private static final byte checkShallows(byte[][] map, int x, int y, int ii, byte closest, byte wType) {
        for (int jj = -ii; jj < ii && closest == 5; ++jj) {
            int mapSize = Server.surfaceMesh.getSize() - 1;
            if (x + jj >= mapSize || x - jj < 1 || y + jj >= mapSize || y - jj < 1 || x + ii >= mapSize || x - ii < 1 || y + ii >= mapSize || y - ii < 1) continue;
            if (map[x + jj][y - ii] == wType) {
                closest = wType;
            }
            if (map[x + ii][y + jj] == wType) {
                closest = wType;
            }
            if (map[x + jj][y + ii] == wType) {
                closest = wType;
            }
            if (map[x - ii][y + jj] != wType) continue;
            closest = wType;
        }
        return closest;
    }

    public static final byte getWaterType(int tilex, int tiley, boolean onSurface) {
        if (onSurface) {
            return waterSurface[tilex][tiley];
        }
        return waterCave[tilex][tiley];
    }

    public static final String getWaterTypeString(int tilex, int tiley, boolean onSurface) {
        byte waterType = WaterType.getWaterType(tilex, tiley, onSurface);
        return WaterType.getWaterTypeString(waterType);
    }

    public static final String getWaterTypeString(byte waterType) {
        switch (waterType) {
            case 0: {
                return "Not Water";
            }
            case 1: {
                return "Water";
            }
            case 2: {
                return "Pond";
            }
            case 3: {
                return "Lake";
            }
            case 4: {
                return "Sea";
            }
            case 5: {
                return "Sea Shallows";
            }
            case 6: {
                return "Lake Shallows";
            }
            case 7: {
                return "Shallows";
            }
        }
        return "Unknown (" + waterType + ")";
    }

    public static final boolean isBrackish(int tilex, int tiley, boolean onSurface) {
        byte waterType = WaterType.getWaterType(tilex, tiley, onSurface);
        return waterType == 4 || waterType == 5;
    }
}

