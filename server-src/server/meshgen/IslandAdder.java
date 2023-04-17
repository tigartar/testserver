/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.meshgen;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.meshgen.ImprovedNoise;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IslandAdder {
    private static final Logger logger = Logger.getLogger(IslandAdder.class.getName());
    private final MeshIO topLayer;
    private final MeshIO rockLayer;
    private final Random random = new Random();
    private final Map<Integer, Set<Integer>> specials = new HashMap<Integer, Set<Integer>>();
    public static final byte north = 0;
    public static final byte northeast = 1;
    public static final byte east = 2;
    public static final byte southeast = 3;
    public static final byte south = 4;
    public static final byte southwest = 5;
    public static final byte west = 6;
    public static final byte northwest = 7;

    public IslandAdder() throws IOException {
        this(MeshIO.open("top_layer.map"), MeshIO.open("rock_layer.map"));
    }

    public IslandAdder(String directoryName) throws IOException {
        this(MeshIO.open(directoryName + File.separatorChar + "top_layer.map"), MeshIO.open(directoryName + File.separatorChar + "rock_layer.map"));
    }

    public IslandAdder(MeshIO aTopLayer, MeshIO aRockLayer) {
        this.topLayer = aTopLayer;
        this.rockLayer = aRockLayer;
    }

    public void addIslands(int maxSize) {
        int maxw = maxSize / 4;
        int minw = maxSize / 8;
        for (int i = maxw; i >= minw; --i) {
            for (int j = 0; j < 2; ++j) {
                int y;
                int width;
                int height = width = i;
                int x = this.random.nextInt(this.topLayer.getSize() - width - 128) + 64;
                Map<Integer, Set<Integer>> changes = this.maybeAddIsland(x, y = this.random.nextInt(maxSize - width - 128) + 64, x + width, y + height, false);
                if (changes == null) continue;
                logger.info("Added island size " + i + " @ " + (x + width / 2) + ", " + (y + height / 2));
            }
        }
    }

    public Map<Integer, Set<Integer>> addOneIsland(int maxSizeX, int maxSizeY) {
        for (int i = 800; i >= 300; --i) {
            for (int j = 0; j < 2; ++j) {
                int y;
                int width;
                int height = width = i;
                int x = this.random.nextInt(maxSizeX - width - 128) + 64;
                Map<Integer, Set<Integer>> changes = this.maybeAddIsland(x, y = this.random.nextInt(maxSizeY - width - 128) + 64, x + width, y + height, false);
                if (changes == null) continue;
                logger.info("Added island size " + i + " @ " + (x + width / 2) + ", " + (y + height / 2));
                return changes;
            }
        }
        return null;
    }

    public final Map<Integer, Set<Integer>> forceIsland(int maxSizeX, int maxSizeY, int tilex, int tiley) {
        Map<Integer, Set<Integer>> changes = this.maybeAddIsland(tilex, tiley, tilex + maxSizeX, tiley + maxSizeY, true);
        if (changes != null) {
            logger.info("Added island size " + maxSizeX + "," + maxSizeY + " @ " + (tilex + maxSizeX / 2) + ", " + (tilex + maxSizeY / 2));
            return changes;
        }
        return null;
    }

    public Map<Integer, Set<Integer>> addToSpecials(int x, int y) {
        Set<Integer> s = this.specials.get(x);
        if (s == null) {
            s = new HashSet<Integer>();
        }
        if (!s.contains(y)) {
            s.add(y);
        }
        this.specials.put(x, s);
        return this.specials;
    }

    public Map<Integer, Set<Integer>> addToChanges(Map<Integer, Set<Integer>> changes, int x, int y) {
        Set<Integer> s = changes.get(x);
        if (s == null) {
            s = new HashSet<Integer>();
        }
        if (!s.contains(y)) {
            s.add(y);
        }
        changes.put(x, s);
        return changes;
    }

    public Map<Integer, Set<Integer>> createMultiPlateau(int x0, int y0, int x1, int y1, int iterations, int startHeight) {
        int lastx0 = x0;
        int lasty0 = y0;
        int lastx1 = x1;
        int lasty1 = y1;
        Map<Integer, Set<Integer>> changes = this.createPlateau(x0, y0, x1, y1, startHeight);
        for (int i = 0; i < iterations; ++i) {
            int modx = (lastx1 - lastx0) / (1 + this.random.nextInt(4));
            int mody = (lasty1 - lasty0) / (1 + this.random.nextInt(4));
            if (this.random.nextBoolean()) {
                modx = -modx;
            }
            if (this.random.nextBoolean()) {
                mody = -mody;
            }
            Map<Integer, Set<Integer>> changes2 = this.createPlateau(lastx0 + modx, lasty0 + mody, lastx1 + modx, lasty1 + mody, startHeight);
            for (Integer inte : changes2.keySet()) {
                Set<Integer> vals = changes2.get(inte);
                if (!changes.containsKey(inte)) {
                    changes.put(inte, vals);
                    continue;
                }
                Set<Integer> oldvals = changes.get(inte);
                for (Integer newint : vals) {
                    if (oldvals.contains(newint)) continue;
                    oldvals.add(newint);
                }
            }
            if (!this.random.nextBoolean()) continue;
            lastx0 += modx;
            lasty0 += mody;
            lastx1 += modx;
            lasty1 += mody;
        }
        return changes;
    }

    public Map<Integer, Set<Integer>> createPlateau(int x0, int y0, int x1, int y1, int startHeight) {
        short height;
        int oldTile;
        double d;
        float pow;
        float next;
        float last;
        float step;
        int branch;
        double dir;
        double od;
        double yd;
        int y;
        double xd;
        int x;
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        double dirOffs = this.random.nextDouble() * Math.PI * 2.0;
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        int branchCount = this.random.nextInt(7) + 3;
        float[] branches = new float[branchCount];
        for (int i = 0; i < branchCount; ++i) {
            branches[i] = this.random.nextFloat() * 0.25f + 0.75f;
        }
        ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
        int highestHeight = Short.MIN_VALUE;
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                od = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                branch = (int)(dir * (double)branchCount);
                step = (float)dir * (float)branchCount - (float)branch;
                last = branches[branch];
                next = branches[(branch + 1) % branchCount];
                pow = last + (next - last) * step;
                d = od;
                if (!((d /= (double)pow) < 1.0)) continue;
                d *= d;
                d *= d;
                d = 1.0 - d;
                oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                height = Tiles.decodeHeight(oldTile);
                float n = (float)(noise.perlinNoise(x, y) * 64.0) + 100.0f;
                int hh = (int)((double)height + (double)((n *= 2.0f) - (float)height) * d);
                if (hh <= highestHeight) continue;
                highestHeight = hh;
            }
        }
        highestHeight += startHeight + this.random.nextInt(startHeight);
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                od = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                branch = (int)(dir * (double)branchCount);
                step = (float)dir * (float)branchCount - (float)branch;
                last = branches[branch];
                next = branches[(branch + 1) % branchCount];
                pow = last + (next - last) * step;
                d = od;
                if (!((d /= (double)pow) < 1.0)) continue;
                if (d < (double)0.3f) {
                    this.topLayer.setTile(x, y, Tiles.encode((short)((double)highestHeight * 0.7), Tiles.Tile.TILE_ROCK.id, (byte)0));
                    this.rockLayer.setTile(x, y, Tiles.encode((short)((double)highestHeight * 0.7), Tiles.Tile.TILE_ROCK.id, (byte)0));
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                short newHeight = (short)((double)highestHeight * (1.0 - d) * (double)pow);
                oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                height = Tiles.decodeHeight(oldTile);
                if (newHeight <= height) continue;
                this.topLayer.setTile(x, y, Tiles.encode(newHeight, Tiles.Tile.TILE_ROCK.id, (byte)0));
                this.rockLayer.setTile(x, y, Tiles.encode(newHeight, Tiles.Tile.TILE_ROCK.id, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        return changes;
    }

    public Map<Integer, Set<Integer>> createRavine(int startX, int startY, int length, int direction) {
        int mody;
        int modx;
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        switch (direction) {
            case 0: {
                modx = 0;
                mody = -1;
                break;
            }
            case 1: {
                mody = -1;
                modx = 1;
                break;
            }
            case 2: {
                modx = 1;
                mody = 0;
                break;
            }
            case 3: {
                mody = 1;
                modx = 1;
                break;
            }
            case 4: {
                modx = 0;
                mody = 1;
                break;
            }
            case 5: {
                mody = 1;
                modx = -1;
                break;
            }
            case 6: {
                modx = -1;
                mody = 0;
                break;
            }
            case 7: {
                modx = -1;
                mody = -1;
                break;
            }
            default: {
                modx = 0;
                mody = 0;
            }
        }
        int width = 1;
        int maxWidth = Math.max(4, length / 10);
        float maximumLengthDepthPoint = 1 + length / 2;
        float maximumWidthDepthPoint = 1 + maxWidth / 2;
        float maxDepth = (float)length / 3.0f;
        int currX = startX;
        int currY = startY;
        logger.log(Level.INFO, "Max depth=" + maxDepth + " length=" + length + ", " + maximumLengthDepthPoint + "," + maximumWidthDepthPoint);
        for (int dist = 0; dist < length; ++dist) {
            float currLengthDepth = 1.0f - Math.abs(maximumLengthDepthPoint - (float)dist) / maximumLengthDepthPoint;
            for (int w = 0; w <= width; ++w) {
                int tx = currX + modx * w;
                int ty = currY + mody * w;
                Set yset = (Set)changes.get(tx);
                if (yset != null && yset.contains(ty)) continue;
                try {
                    int oldTile = this.topLayer.data[tx | ty << this.topLayer.getSizeLevel()];
                    float height = Tiles.decodeHeightAsFloat(oldTile);
                    int nt = this.topLayer.data[tx | ty - 1 << this.topLayer.getSizeLevel()];
                    float nth = Tiles.decodeHeightAsFloat(nt);
                    int st = this.topLayer.data[tx | ty + 1 << this.topLayer.getSizeLevel()];
                    float sth = Tiles.decodeHeightAsFloat(st);
                    int et = this.topLayer.data[tx + 1 | ty << this.topLayer.getSizeLevel()];
                    float eth = Tiles.decodeHeightAsFloat(et);
                    int wt = this.topLayer.data[tx - 1 | ty << this.topLayer.getSizeLevel()];
                    float wth = Tiles.decodeHeightAsFloat(wt);
                    float minPrevHeight = Math.min(nth, Math.min(sth, Math.min(eth, wth)));
                    float change = currLengthDepth * maxDepth;
                    if (change < height - minPrevHeight) {
                        change = Math.min(change, height - minPrevHeight - 3.0f);
                    } else if (change > height - minPrevHeight) {
                        change = Math.min(change, height - minPrevHeight + 3.0f);
                    }
                    if (change == 0.0f) continue;
                    float newDepth = height - change;
                    if (Tiles.decodeHeightAsFloat(this.rockLayer.data[tx | ty << this.rockLayer.getSizeLevel()]) >= newDepth) {
                        logger.log(Level.INFO, "Setting rock at " + tx + "," + ty + " to " + newDepth);
                        this.topLayer.setTile(tx, ty, Tiles.encode(newDepth, Tiles.Tile.TILE_ROCK.id, (byte)0));
                        this.rockLayer.setTile(tx, ty, Tiles.encode(newDepth, Tiles.Tile.TILE_ROCK.id, (byte)0));
                    } else {
                        logger.log(Level.INFO, "Rock at " + tx + "," + ty + " is " + Tiles.decodeHeightAsFloat(this.rockLayer.data[tx | ty << this.rockLayer.getSizeLevel()]) + " so setting to " + newDepth);
                        if (this.random.nextInt(5) == 0) {
                            this.topLayer.setTile(tx, ty, Tiles.encode(newDepth, Tiles.decodeType(oldTile), Tiles.decodeData(oldTile)));
                        } else {
                            this.topLayer.setTile(tx, ty, Tiles.encode(newDepth, Tiles.Tile.TILE_DIRT.id, (byte)0));
                        }
                    }
                    changes = this.addToChanges(changes, tx, ty);
                    continue;
                }
                catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                    // empty catch block
                }
            }
            int rand = this.random.nextInt(20);
            if (modx <= 0 && rand == 0) {
                ++currX;
            } else if (modx >= 0 && rand == 1) {
                --currX;
            }
            if (mody <= 0 && rand == 2) {
                ++currY;
            } else if (mody >= 0 && rand == 3) {
                --currY;
            }
            int wmod = 0;
            if (rand == 4) {
                wmod = 1;
            } else if (rand == 5) {
                wmod = -1;
            }
            currX += modx;
            currY += mody;
            width = (int)Math.max(4.0f, (float)wmod + (float)maxWidth * (currLengthDepth * 2.0f));
        }
        return changes;
    }

    private Map<Integer, Set<Integer>> createIndentationXxx(int x0, int y0, int x1, int y1, byte newTopLayerTileId, byte newTopLayerData) {
        double d;
        float pow;
        float next;
        float last;
        float step;
        int branch;
        double dir;
        double od;
        double yd;
        int y;
        double xd;
        int x;
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        double dirOffs = this.random.nextDouble() * Math.PI * 2.0;
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        int branchCount = this.random.nextInt(7) + 3;
        float[] branches = new float[branchCount];
        for (int i = 0; i < branchCount; ++i) {
            branches[i] = this.random.nextFloat() * 0.25f + 0.75f;
        }
        ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
        int lowestHeight = Short.MAX_VALUE;
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                od = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                branch = (int)(dir * (double)branchCount);
                step = (float)dir * (float)branchCount - (float)branch;
                last = branches[branch];
                next = branches[(branch + 1) % branchCount];
                pow = last + (next - last) * step;
                d = od;
                if (!((d /= (double)pow) < 1.0)) continue;
                d *= d;
                d *= d;
                d = 1.0 - d;
                int oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                short height = Tiles.decodeHeight(oldTile);
                float n = (float)(noise.perlinNoise(x, y) * 64.0) + 100.0f;
                int hh = (int)((double)height + (double)((n *= 2.0f) - (float)height) * d);
                if (hh >= lowestHeight) continue;
                lowestHeight = hh;
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                od = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                branch = (int)(dir * (double)branchCount);
                step = (float)dir * (float)branchCount - (float)branch;
                last = branches[branch];
                next = branches[(branch + 1) % branchCount];
                pow = last + (next - last) * step;
                d = od;
                if ((d /= (double)pow) < 1.0) {
                    this.topLayer.setTile(x, y, Tiles.encode((short)lowestHeight, newTopLayerTileId, newTopLayerData));
                    this.rockLayer.setTile(x, y, Tiles.encode((short)lowestHeight, Tiles.Tile.TILE_ROCK.id, (byte)0));
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                if (this.random.nextInt(3) != 0) continue;
                this.topLayer.setTile(x, y, Tiles.encode(Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]), Tiles.Tile.TILE_ROCK.id, (byte)0));
                this.rockLayer.setTile(x, y, Tiles.encode(Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]), Tiles.Tile.TILE_ROCK.id, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        return changes;
    }

    public Map<Integer, Set<Integer>> createRockIndentation(int x0, int y0, int x1, int y1) {
        return this.createIndentationXxx(x0, y0, x1, y1, Tiles.Tile.TILE_ROCK.id, (byte)0);
    }

    public Map<Integer, Set<Integer>> createVolcano(int x0, int y0, int x1, int y1) {
        Map<Integer, Set<Integer>> changes = this.createIndentationXxx(x0, y0, x1, y1, Tiles.Tile.TILE_LAVA.id, (byte)-1);
        for (int x = x0; x < x1; ++x) {
            for (int y = y0; y < y1; ++y) {
                int oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                byte oldType = Tiles.decodeType(oldTile);
                short height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                if (oldType != Tiles.Tile.TILE_LAVA.id || this.isTopLayerFlat(x, y)) continue;
                this.topLayer.setTile(x, y, Tiles.encode(height, Tiles.Tile.TILE_ROCK.id, (byte)0));
                this.rockLayer.setTile(x, y, Tiles.encode(height, Tiles.Tile.TILE_ROCK.id, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        return changes;
    }

    public boolean isTopLayerFlat(int tilex, int tiley) {
        short heightChecked = Short.MIN_VALUE;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                short ch = Tiles.decodeHeight(this.topLayer.getTile(tilex + x, tiley + y));
                if (heightChecked == Short.MIN_VALUE) {
                    heightChecked = ch;
                }
                if (ch == heightChecked) continue;
                return false;
            }
        }
        return true;
    }

    public Map<Integer, Set<Integer>> createCrater(int x0, int y0, int x1, int y1) {
        float n;
        double yd;
        int y;
        double xd;
        int x;
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        double dirOffs = this.random.nextDouble() * Math.PI * 2.0;
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        int branchCount = this.random.nextInt(7) + 3;
        float[] branches = new float[branchCount];
        for (int i = 0; i < branchCount; ++i) {
            branches[i] = this.random.nextFloat() * 0.25f + 0.75f;
        }
        ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                double dir;
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                double od = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float next = branches[(branch + 1) % branchCount];
                float pow = last + (next - last) * step;
                double d = od;
                d /= (double)pow;
                int oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                byte oldType = Tiles.decodeType(oldTile);
                if (!(d < 1.0)) continue;
                d *= d;
                d *= d;
                d = 1.0 - d;
                short height = Tiles.decodeHeight(oldTile);
                n = (float)noise.perlinNoise(x, y) * 5.0f;
                int hh = (int)((double)height + (double)((n *= 2.0f) - 1.0f) * d * 50.0);
                byte type = Tiles.Tile.TILE_DIRT.id;
                int diff = hh - height;
                if (diff < 0 && (oldType == Tiles.Tile.TILE_ROCK.id || oldType == Tiles.Tile.TILE_CLIFF.id)) {
                    type = oldType;
                } else {
                    if (hh <= 0 && this.random.nextInt(5) == 0) {
                        type = Tiles.Tile.TILE_SAND.id;
                    }
                    if (hh > 5 && this.random.nextInt(100) == 0) {
                        type = Tiles.Tile.TILE_GRASS.id;
                    }
                }
                this.topLayer.setTile(x, y, Tiles.encode((short)hh, type, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                double dir;
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                double d = Math.sqrt(xd * xd + yd * yd);
                double od = d * (double)(x1 - x0);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float next = branches[(branch + 1) % branchCount];
                float pow = last + (next - last) * step;
                d /= (double)pow;
                short height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                int dd = 0;
                float hh = (float)height / 10.0f - 8.0f;
                if ((d = 1.0 - d) < 0.0) {
                    d = 0.0;
                }
                if ((d = Math.sin(d * Math.PI) * 2.0 - 1.0) < 0.0) {
                    d = 0.0;
                }
                if ((n = (float)noise.perlinNoise((double)x / 2.0, (double)y / 2.0)) > 0.5f) {
                    n -= (n - 0.5f) * 2.0f;
                }
                if ((n /= 0.5f) < 0.0f) {
                    n = 0.0f;
                }
                hh = (float)((double)hh + (double)(n * (float)(x1 - x0) / 8.0f) * d);
                int oldTile = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                byte oldType = Tiles.decodeType(oldTile);
                if (oldType != Tiles.Tile.TILE_ROCK.id && oldType != Tiles.Tile.TILE_CLIFF.id) {
                    float ddd = (float)od / 16.0f;
                    if (ddd < 1.0f) {
                        if ((ddd = ddd * 2.0f - 1.0f) > 1.0f) {
                            ddd = 1.0f;
                        }
                        if (ddd < 0.0f) {
                            ddd = 0.0f;
                        }
                        dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                        float hh1 = Tiles.decodeHeightAsFloat(dd);
                        hh = Tiles.decodeHeightAsFloat(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()]);
                        hh = hh1 - Math.min(5.0f, (hh1 - hh) * ddd);
                        this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                        changes = this.addToChanges(changes, x, y);
                        continue;
                    }
                    dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                    hh = Tiles.decodeHeightAsFloat(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                    hh = (hh = hh * 0.5f + (float)((int)hh / 2 * 2) * 0.5f) > 0.0f ? (hh += 0.07f) : (hh -= 0.07f);
                    this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                    if (hh < Tiles.decodeHeightAsFloat(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()])) {
                        this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.Tile.TILE_ROCK.id, (byte)0));
                        this.rockLayer.setTile(x, y, Tiles.encode(hh, Tiles.Tile.TILE_ROCK.id, (byte)0));
                    } else {
                        this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                    }
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            for (int y2 = y0; y2 < y1; ++y2) {
                boolean rock = true;
                for (int xx = 0; xx < 2; ++xx) {
                    for (int yy = 0; yy < 2; ++yy) {
                        short height = Tiles.decodeHeight(this.topLayer.data[x | y2 << this.topLayer.getSizeLevel()]);
                        short groundHeight = Tiles.decodeHeight(this.rockLayer.data[x | y2 << this.topLayer.getSizeLevel()]);
                        if (groundHeight < height) {
                            rock = false;
                            continue;
                        }
                        int dd = this.topLayer.data[x | y2 << this.topLayer.getSizeLevel()];
                        this.topLayer.setTile(x, y2, Tiles.encode(groundHeight, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                        changes = this.addToChanges(changes, x, y2);
                    }
                }
                if (!rock) continue;
                int dd = this.topLayer.data[x | y2 << this.topLayer.getSizeLevel()];
                this.topLayer.setTile(x, y2, Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_ROCK.id, (byte)0));
                changes = this.addToChanges(changes, x, y2);
            }
        }
        return changes;
    }

    public Map<Integer, Set<Integer>> maybeAddIsland(int x0, int y0, int x1, int y1, boolean forced) {
        double od;
        double d;
        short height;
        int y;
        double xd;
        int x;
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        double dirOffs = this.random.nextDouble() * Math.PI * 2.0;
        for (int x2 = x0; x2 < x1; ++x2) {
            double xd2 = (double)(x2 - xm) * 2.0 / (double)(x1 - x0);
            for (int y2 = y0; y2 < y1; ++y2) {
                short height2;
                double yd = (double)(y2 - ym) * 2.0 / (double)(y1 - y0);
                double d2 = Math.sqrt(xd2 * xd2 + yd * yd);
                if (!(d2 < 1.0) || (height2 = Tiles.decodeHeight(this.topLayer.data[x2 | y2 << this.topLayer.getSizeLevel()])) <= -5 || forced) continue;
                return null;
            }
        }
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        int branchCount = this.random.nextInt(7) + 3;
        float[] branches = new float[branchCount];
        for (int i = 0; i < branchCount; ++i) {
            branches[i] = this.random.nextFloat() * 0.25f + 0.75f;
        }
        ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                double dir;
                double yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                double od2 = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float next = branches[(branch + 1) % branchCount];
                float pow = last + (next - last) * step;
                double d3 = od2;
                if (!((d3 /= (double)pow) < 1.0)) continue;
                d3 *= d3;
                d3 *= d3;
                d3 = 1.0 - d3;
                height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                float n = (float)(noise.perlinNoise(x, y) * 64.0) + 100.0f;
                int hh = (int)((double)height + (double)((n *= 2.0f) - (float)height) * d3);
                byte type = Tiles.Tile.TILE_DIRT.id;
                if (hh > 5 && this.random.nextInt(100) == 0) {
                    type = Tiles.Tile.TILE_GRASS.id;
                }
                hh = hh > 0 ? (int)((float)hh + 0.07f) : (int)((float)hh - 0.07f);
                this.topLayer.setTile(x, y, Tiles.encode((short)hh, type, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                float n;
                double dir;
                double yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                d = Math.sqrt(xd * xd + yd * yd);
                od = d * (double)(x1 - x0);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float next = branches[(branch + 1) % branchCount];
                float pow = last + (next - last) * step;
                d /= (double)pow;
                height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                int dd = this.rockLayer.data[x | y << this.topLayer.getSizeLevel()];
                float hh = (float)height / 10.0f - 8.0f;
                if ((d = 1.0 - d) < 0.0) {
                    d = 0.0;
                }
                if ((d = Math.sin(d * Math.PI) * 2.0 - 1.0) < 0.0) {
                    d = 0.0;
                }
                if ((n = (float)noise.perlinNoise((double)x / 2.0, (double)y / 2.0)) > 0.5f) {
                    n -= (n - 0.5f) * 2.0f;
                }
                if ((n /= 0.5f) < 0.0f) {
                    n = 0.0f;
                }
                hh = (float)((double)hh + (double)(n * (float)(x1 - x0) / 8.0f) * d);
                this.rockLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                changes = this.addToChanges(changes, x, y);
                float ddd = (float)od / 16.0f;
                if (ddd < 1.0f) {
                    if ((ddd = ddd * 2.0f - 1.0f) > 1.0f) {
                        ddd = 1.0f;
                    }
                    if (ddd < 0.0f) {
                        ddd = 0.0f;
                    }
                    dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                    float hh1 = Tiles.decodeHeightAsFloat(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                    hh = Tiles.decodeHeightAsFloat(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()]);
                    hh += (hh1 - hh) * ddd;
                    this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                hh = Tiles.decodeHeightAsFloat(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                hh = (hh = hh * 0.5f + (float)((int)hh / 2 * 2) * 0.5f) > 0.0f ? (hh += 0.07f) : (hh -= 0.07f);
                this.topLayer.setTile(x, y, Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd)));
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                int dd;
                double yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                d = Math.sqrt(xd * xd + yd * yd);
                od = d * (double)(x1 - x0);
                boolean rock = true;
                for (int xx = 0; xx < 2; ++xx) {
                    for (int yy = 0; yy < 2; ++yy) {
                        short height3 = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                        short groundHeight = Tiles.decodeHeight(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()]);
                        if (groundHeight < height3) {
                            rock = false;
                            continue;
                        }
                        int dd2 = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                        this.topLayer.setTile(x, y, Tiles.encode(groundHeight, Tiles.decodeType(dd2), Tiles.decodeData(dd2)));
                        changes = this.addToChanges(changes, x, y);
                    }
                }
                if (!rock) continue;
                float ddd = (float)od / 16.0f;
                if (ddd < 1.0f) {
                    dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                    this.topLayer.setTile(x, y, Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_LAVA.id, (byte)0));
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                this.topLayer.setTile(x, y, Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_ROCK.id, (byte)0));
                changes = this.addToChanges(changes, x, y);
            }
        }
        return changes;
    }

    public void save() throws IOException {
        this.topLayer.setAllRowsDirty();
        this.topLayer.saveAll();
        this.rockLayer.saveAll();
        this.topLayer.close();
        this.rockLayer.close();
    }

    public static void main(String[] args) {
        try {
            logger.info("Loading maps..");
            IslandAdder islandAdder = new IslandAdder();
            logger.info("Adding islands..");
            islandAdder.addIslands(2096);
            logger.info("Saving islands..");
            islandAdder.save();
            logger.info("Finished");
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to add islands!", e);
        }
    }

    public MeshIO getTopLayer() {
        return this.topLayer;
    }

    public MeshIO getRockLayer() {
        return this.rockLayer;
    }
}

