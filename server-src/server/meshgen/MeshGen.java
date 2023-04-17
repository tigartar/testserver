/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.meshgen;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.meshgen.MeshGenGui;
import com.wurmonline.server.meshgen.PerlinNoise;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MeshGen {
    private static final Logger logger = Logger.getLogger(MeshGen.class.getName());
    private float[][] groundHeight;
    private float[][] height;
    private byte[][] textures;
    private byte[][] textureDatas;
    private int level;
    private int width;
    private static final float MAP_HEIGHT = 1000.0f;
    private static final boolean USE_SPLIT_TREES = true;
    private static final boolean USE_DROP_DIRT_METHOD = false;
    private static final int NUMBER_OF_DIRT_TO_DROP = 40;
    private static final boolean CHECK_STRAIGHT_SLOPES = true;
    private static final int MAX_STRAIGHT_SLOPE = 20;
    private static final boolean CHECK_DIAGONAL_SLOPES = true;
    private static final int MAX_DIAGONAL_SLOPE = 20;
    private static final boolean OLD_MAP_STYLE_ROTATE_AND_FLIP = true;
    private static final float waterBias = 0.1f;
    private int imageLayer = 0;
    private static final byte TREEID = Tiles.Tile.TILE_TREE.id;
    private static final byte BUSHID = Tiles.Tile.TILE_BUSH.id;
    private static final byte GRASSID = Tiles.Tile.TILE_GRASS.id;

    public MeshGen(int level1, MeshGenGui.Task task) throws Exception {
        this.level = level1;
        this.width = 1 << level1;
        logger.info("Level: " + level1);
        logger.info("Width: " + this.width);
        task.setNote(0, "Allocating memory");
        task.setNote(1, "  heights");
        this.height = new float[this.width][this.width];
        task.setNote(25, "  textures");
        this.textures = new byte[this.width][this.width];
        task.setNote(50, "  ground heights");
        this.groundHeight = new float[this.width][this.width];
        task.setNote(75, "  texture data");
        this.textureDatas = new byte[this.width][this.width];
    }

    public MeshGen(Random random, int aLevel, MeshGenGui.Task task) throws Exception {
        this(aLevel, task);
        PerlinNoise perlin = new PerlinNoise(random, aLevel);
        int steps = 3;
        task.setMax(this.width * 3 * 2);
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                this.textures[x][y] = -1;
                this.height[x][y] = 0.0f;
            }
        }
        perlin.setRandom(new Random(random.nextLong()));
        for (int i = 0; i < 3; ++i) {
            perlin.setRandom(new Random(random.nextLong()));
            task.setNote("Calculating perlin noise..");
            float[][] hs1 = perlin.generatePerlinNoise(0.3f, i == 0 ? 0 : 1, task, this.width * i * 2, 0);
            for (int x = 0; x < this.width; ++x) {
                task.setNote(x + this.width * i * 2 + this.width);
                for (int y = 0; y < this.width; ++y) {
                    float h = hs1[x][y] - this.height[x][y];
                    if (h < 0.0f) {
                        h = -h;
                    }
                    this.height[x][y] = h = (float)Math.pow(h, 1.2);
                }
            }
        }
    }

    public final int getWidth() {
        return this.width;
    }

    protected void setData(float[] data, MeshGenGui.Task task) throws Exception {
        int y;
        int x;
        int x2;
        task.setMax(this.width * 7);
        task.setNote(0, "Normalizing.");
        Random grassRand = new Random();
        float lowest = Float.MAX_VALUE;
        float highest = Float.MIN_VALUE;
        float numsover50 = 0.0f;
        task.setNote("  Pass 1");
        for (int x3 = 0; x3 < this.width; ++x3) {
            task.setNote(x3);
            int y2 = 0;
            while (y2 < this.width) {
                this.height[x3][y2] = data[y2 + x3 * this.width];
                float n1 = this.height[x3][y2];
                if (n1 < lowest) {
                    lowest = n1;
                }
                if (n1 > highest) {
                    highest = n1;
                }
                if (n1 > 0.1f) {
                    numsover50 += 1.0f;
                }
                for (int xx = x3 - 1; xx <= x3 + 1; ++xx) {
                    for (int yy = y2 - 1; yy <= y2 + 1; ++yy) {
                        if (xx >= 0 && yy >= 0 && xx < this.width && yy < this.width) {
                            float[] fArray = this.height[x3];
                            int n = y2;
                            fArray[n] = fArray[n] + Math.min(data[y2 + x3 * this.width], data[yy + xx * this.width]);
                            continue;
                        }
                        float[] fArray = this.height[x3];
                        int n = y2;
                        fArray[n] = fArray[n] + data[y2 + x3 * this.width];
                    }
                }
                float[] fArray = this.height[x3];
                int n = y2++;
                fArray[n] = fArray[n] / 10.0f;
            }
        }
        float maxHeight = highest - lowest;
        System.out.println("Before percent over 0.1=" + numsover50 / (float)(this.width * this.width) + " highest=" + highest + " lowest=" + lowest + " maxheight=" + maxHeight);
        lowest = Float.MAX_VALUE;
        highest = Float.MIN_VALUE;
        numsover50 = 0.0f;
        task.setNote("  Pass 2");
        for (x2 = 0; x2 < this.width; ++x2) {
            task.setNote(x2 + this.width);
            for (int y3 = 0; y3 < this.width; ++y3) {
                float n1 = this.height[x2][y3];
                if (n1 < lowest) {
                    lowest = n1;
                }
                if (n1 > highest) {
                    highest = n1;
                }
                if (!(n1 > 0.1f)) continue;
                numsover50 += 1.0f;
            }
        }
        maxHeight = highest - lowest;
        System.out.println("After percent over 0.1=" + numsover50 / (float)(this.width * this.width) + " highest=" + highest + " lowest=" + lowest + " maxheight=" + maxHeight);
        task.setNote("  Pass 3");
        for (x2 = 0; x2 < this.width; ++x2) {
            task.setNote(x2 + this.width * 2);
            for (int y4 = 0; y4 < this.width; ++y4) {
                float n1 = this.height[x2][y4];
                float h = (n1 - 0.1f) / 0.9f;
                if (h > 0.0f) {
                    h += 1.0E-4f;
                } else {
                    h *= 0.5f;
                    h -= 1.0E-4f;
                }
                this.height[x2][y4] = h;
                this.groundHeight[x2][y4] = h;
            }
        }
        lowest = Float.MAX_VALUE;
        highest = Float.MIN_VALUE;
        numsover50 = 0.0f;
        task.setNote("  Pass 4");
        for (x2 = 0; x2 < this.width; ++x2) {
            task.setNote(x2 + this.width * 3);
            for (int y5 = 0; y5 < this.width; ++y5) {
                float n1 = this.height[x2][y5];
                if (n1 < lowest) {
                    lowest = n1;
                }
                if (n1 > highest) {
                    highest = n1;
                }
                if (!(n1 > 0.1f)) continue;
                numsover50 += 1.0f;
            }
        }
        maxHeight = highest - lowest;
        System.out.println("After THIRD percent over 0.1=" + numsover50 / (float)(this.width * this.width) + " highest=" + highest + " lowest=" + lowest + " maxheight=" + maxHeight);
        System.out.println("Creating rock layer.");
        float waterConstant = 1.1f;
        float mapSizeInfluence = 0.035f;
        float mapSizeMod = (float)this.width * 0.035f;
        float influenceMod = 0.02f;
        System.out.println("mapSizeMod=" + mapSizeMod + ", waterConstant=" + 1.1f + ", influenceMod=" + 0.02f);
        task.setNote("  Pass 5 - Creating rock layer.");
        for (x = 0; x < this.width; ++x) {
            task.setNote(x + this.width * 4);
            for (y = 0; y < this.width; ++y) {
                float h;
                float hh = Math.max(0.0f, (this.height[x][y] + 0.1f) / 1.1f);
                float heightModifier = 1.1f - hh;
                float subtracted = (1.0f - this.getDirtSlope(x, y) * mapSizeMod) * 0.02f * heightModifier / 3.0f;
                if (x == 3274 && y == 1425) {
                    System.out.println("dslope=" + this.getDirtSlope(x, y) + ", subtracted=" + subtracted + " heightmod=" + heightModifier + " height " + x + "," + y + "=" + this.height[x][y]);
                }
                this.groundHeight[x][y] = h = this.height[x][y] - subtracted;
                if (!(this.groundHeight[x][y] > this.height[x][y])) continue;
                this.groundHeight[x][y] = this.height[x][y];
            }
        }
        task.setNote("  Pass 6 - Applying Cliff, Rock, and Grass tiles.");
        for (x = 0; x < this.width; ++x) {
            task.setNote(x + this.width * 5);
            for (y = 0; y < this.width; ++y) {
                boolean rock = true;
                for (int xx = 0; xx < 2; ++xx) {
                    for (int yy = 0; yy < 2; ++yy) {
                        if (!(this.getGroundHeight(x + xx, y + yy) < this.getHeight(x + xx, y + yy))) continue;
                        if (x == 3274 && y == 1425) {
                            System.out.println("ggh=" + this.getGroundHeight(x + xx, y + yy) + ", gh=" + this.getHeight(x + xx, y + yy) + ": rock false");
                        }
                        rock = false;
                    }
                }
                if (rock) {
                    this.textures[x][y] = Tiles.Tile.TILE_ROCK.id;
                    this.textureDatas[x][y] = 0;
                    continue;
                }
                this.setTile(GRASSID, x, y, grassRand);
            }
        }
        task.setNote("  Pass 7 - Check Heights of Rock and Cliff tiles");
        for (x = 0; x < this.width; ++x) {
            task.setNote(x + this.width * 6);
            for (y = 0; y < this.width; ++y) {
                if (this.textures[x][y] != Tiles.Tile.TILE_CLIFF.id && this.textures[x][y] != Tiles.Tile.TILE_ROCK.id || !(this.getGroundHeight(x, y) < this.getHeight(x, y))) continue;
                System.out.println("Cliff Error at " + x + ", " + y);
            }
        }
    }

    private float getDirtSlope(int x, int y) {
        float hs1 = Math.abs(this.getHeight(x - 1, y) - this.getHeight(x, y));
        float hs2 = Math.abs(this.getHeight(x + 1, y) - this.getHeight(x, y));
        float vs1 = Math.abs(this.getHeight(x, y - 1) - this.getHeight(x, y));
        float vs2 = Math.abs(this.getHeight(x, y + 1) - this.getHeight(x, y));
        float hs = hs1 + hs2;
        float vs = vs1 + vs2;
        return (float)Math.sqrt(vs * vs + hs * hs);
    }

    public void generateGround(Random random, MeshGenGui.Task task) {
        boolean blurSteps = true;
        task.setMax(this.width * 1);
        task.setNote("Flowing water..");
        for (int i = 0; i < this.width * this.width / 1000; ++i) {
            task.setNote(i);
        }
        float[][] h = new float[this.width][this.width];
        for (int i = 0; i < 1; ++i) {
            int y;
            int x;
            for (x = 0; x < this.width; ++x) {
                for (y = 0; y < this.width; ++y) {
                    System.out.println("Setting " + x + ", " + y + " to 0");
                    h[x][y] = 0.0f;
                }
            }
            for (x = 0; x < this.width; ++x) {
                task.setNote(x + i * this.width);
                for (y = 0; y < this.width; ++y) {
                }
            }
            for (x = 0; x < this.width; ++x) {
                for (y = 0; y < this.width; ++y) {
                    h[x][y] = 0.0f;
                }
            }
        }
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
            }
        }
    }

    public void generateWater() {
    }

    public float getHeight(int x, int y) {
        if (x < 0 || y < 0 || x > this.width - 1 || y > this.width - 1) {
            return 0.0f;
        }
        return this.height[x & this.width - 1][y & this.width - 1];
    }

    public float getGroundHeight(int x, int y) {
        if (x < 0 || y < 0 || x > this.width - 1 || y > this.width - 1) {
            return 0.0f;
        }
        return this.groundHeight[x & this.width - 1][y & this.width - 1];
    }

    public float getHeightAndWater(int x, int y) {
        return this.height[x &= this.width - 1][y &= this.width - 1];
    }

    public void setHeight(int x, int y, float h) {
        this.height[x & this.width - 1][y & this.width - 1] = h;
    }

    public void dropADirt(boolean forceDrop, Random random) {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                if (!forceDrop && !(this.getHeight(x, y) > -1.0f)) continue;
                Point p = this.findDropTile(x, y, random);
                this.incHeight(p.getX(), p.getY());
            }
        }
    }

    private void incHeight(int x, int y) {
        this.setHeight(x, y, this.height[x & this.width - 1][y & this.width - 1] + 1.0E-4f);
    }

    private Point findDropTile(int tileX, int tileY, Random random) {
        ArrayList<Point> slopes = new ArrayList<Point>();
        short h = (short)(this.getHeight(tileX, tileY) * 1000.0f * 10.0f);
        for (int xx = 1; xx >= -1; --xx) {
            for (int yy = 1; yy >= -1; --yy) {
                short th = (short)(this.getHeight(tileX + xx, tileY + yy) * 1000.0f * 10.0f);
                if ((xx == 0 && yy != 0 || yy == 0 && xx != 0) && th < h - 20) {
                    slopes.add(new Point(tileX + xx, tileY + yy));
                }
                if (xx == 0 || yy == 0 || th >= h - 20) continue;
                slopes.add(new Point(tileX + xx, tileY + yy));
            }
        }
        if (slopes.size() > 0) {
            int r = 0;
            if (slopes.size() > 1) {
                r = random.nextInt(slopes.size());
            }
            return this.findDropTile(((Point)slopes.get(r)).getX(), ((Point)slopes.get(r)).getY(), random);
        }
        return new Point(tileX, tileY);
    }

    public final void createReeds2() {
        System.out.println("Skipping reeds");
    }

    public final void createReeds() {
        logger.info("Creating reeds");
        Random grassRand = new Random();
        int grassSeed = grassRand.nextInt();
        int grassMod = 5;
        int grassCommonality = this.width / 20;
        for (int xk = 0; xk < this.width - 1; ++xk) {
            for (int yk = 0; yk < this.width - 1; ++yk) {
                float theight = this.getHeight(xk, yk) * 1000.0f;
                if (!(theight <= 0.0f) || !(theight > -40.0f)) continue;
                grassRand.setSeed(grassSeed + (xk / grassCommonality + yk / grassCommonality * 10000));
                int randXResult = grassRand.nextInt(5);
                int randYResult = grassRand.nextInt(5);
                if (randXResult != 0 || randYResult != 0) continue;
                grassRand.setSeed(System.nanoTime());
                if (!grassRand.nextBoolean()) continue;
                byte tileType = Tiles.Tile.TILE_REED.id;
                if (theight < -2.0f) {
                    tileType = Tiles.Tile.TILE_KELP.id;
                }
                byte tileData = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, GrassData.FlowerType.NONE);
                this.textures[xk][yk] = tileType;
                this.textureDatas[xk][yk] = tileData;
            }
        }
    }

    private void blotch(byte id, long amount, int spread, int sizeScale, boolean underwater, boolean nearRock, boolean waterOrNoWater, boolean shallowOnly, Random random) {
        logger.info("Adding blotch of " + Tiles.getTile(id).getName() + ". Amount=" + amount + ", spread=" + spread + ", sizeSale=" + sizeScale + ", underwater=" + underwater + ", nearRock=" + nearRock + ", waterOrNoWater=" + waterOrNoWater + ", shallowOnly=" + shallowOnly);
        int i = 0;
        while ((long)i < amount) {
            int xo = random.nextInt(this.width);
            int yo = random.nextInt(this.width);
            boolean ok = false;
            if (nearRock) {
                ok = this.isRock(xo, yo);
            } else if (underwater) {
                ok = this.isWater(xo, yo);
            } else {
                if (waterOrNoWater) {
                    System.out.println("Water or no water triggered:");
                    new Exception().printStackTrace();
                }
                boolean bl = ok = (waterOrNoWater || !this.isWater(xo, yo)) && !this.isRock(xo, yo);
            }
            if (ok) {
                int size = (random.nextInt(80) + 10) * sizeScale;
                for (int j = 0; j < size / 5; ++j) {
                    int x = xo;
                    int y = yo;
                    for (int k = 0; k < size / 5; ++k) {
                        if (random.nextInt(2) == 0) {
                            x = x + random.nextInt(spread * 2 + 1) - spread;
                        } else {
                            y = y + random.nextInt(spread * 2 + 1) - spread;
                        }
                        y &= this.width - 1;
                        block3: for (int xk = x &= this.width - 1; xk < x + sizeScale / 2 + 1; ++xk) {
                            for (int yk = y; yk < y + sizeScale / 2 + 1; ++yk) {
                                boolean tileSubmerged = this.isWater(xk, yk);
                                if (underwater && !tileSubmerged) {
                                    ++k;
                                }
                                if (tileSubmerged && !underwater || this.isRock(xk, yk)) continue block3;
                                float theight = this.getHeight(xk, yk);
                                if (shallowOnly) {
                                    if (!(theight * 1000.0f > -1.0f)) continue;
                                    this.setTile(id, xk & this.width - 1, yk & this.width - 1, random);
                                    continue;
                                }
                                this.setTile(id, xk & this.width - 1, yk & this.width - 1, random);
                            }
                        }
                    }
                }
            }
            ++i;
        }
    }

    private void setTile(byte id, int x, int y, Random random) {
        if (id == GRASSID) {
            GrassData.GrowthStage growthStage = GrassData.GrowthStage.fromInt(random.nextInt(4));
            GrassData.FlowerType flowerType = MeshGen.getRandomFlower(6);
            this.textures[x][y] = id;
            this.textureDatas[x][y] = GrassData.encodeGrassTileData(growthStage, flowerType);
        } else if (id == Tiles.Tile.TILE_REED.id) {
            float theight = this.getHeight(x, y) * 1000.0f;
            this.textures[x][y] = theight < -2.0f ? Tiles.Tile.TILE_KELP.id : Tiles.Tile.TILE_REED.id;
            this.textureDatas[x][y] = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, GrassData.FlowerType.NONE);
        } else {
            this.textures[x][y] = id;
            this.textureDatas[x][y] = 0;
        }
    }

    public static GrassData.FlowerType getRandomFlower(int chance) {
        int rnd = Server.rand.nextInt(chance * 1000);
        if (rnd < 1000) {
            if (rnd > 998) {
                return GrassData.FlowerType.FLOWER_7;
            }
            if (rnd > 990) {
                return GrassData.FlowerType.FLOWER_6;
            }
            if (rnd > 962) {
                return GrassData.FlowerType.FLOWER_5;
            }
            if (rnd > 900) {
                return GrassData.FlowerType.FLOWER_4;
            }
            if (rnd > 800) {
                return GrassData.FlowerType.FLOWER_3;
            }
            if (rnd > 500) {
                return GrassData.FlowerType.FLOWER_2;
            }
            return GrassData.FlowerType.FLOWER_1;
        }
        return GrassData.FlowerType.NONE;
    }

    private void exposeClay(long amount, Random random) {
        logger.info("Attempting to expose " + amount + " tiles of clay");
        int i = 0;
        while ((long)i < amount) {
            int y;
            int x;
            boolean above;
            int xo = random.nextInt(this.width - 10) + 1;
            int yo = random.nextInt(this.width - 10) + 1;
            int w = random.nextInt(2) + 2;
            int h = random.nextInt(2) + 2;
            boolean fail = false;
            boolean below = this.height[xo][yo] * 1000.0f < -4.0f;
            boolean bl = above = this.height[xo][yo] * 1000.0f > 2.0f;
            if (below || above) {
                fail = true;
            } else if (this.height[xo][yo] < 0.0f && this.height[xo + w][yo + h] < 0.0f) {
                fail = true;
            }
            if (!fail) {
                for (x = xo - 1; x < xo + w + 1; ++x) {
                    for (y = yo - 1; y < yo + h + 1; ++y) {
                        if (this.textures[x][y] == Tiles.Tile.TILE_ROCK.id) {
                            fail = true;
                        }
                        if (this.textures[x][y] == Tiles.Tile.TILE_CLIFF.id) {
                            fail = true;
                        }
                        if (this.textures[x][y] == Tiles.Tile.TILE_TAR.id) {
                            fail = true;
                        }
                        if (this.textures[x][y] != Tiles.Tile.TILE_CLAY.id) continue;
                        fail = true;
                    }
                }
            }
            if (!fail) {
                System.out.print(".");
                for (x = xo; x < xo + w + 1; ++x) {
                    for (y = yo; y < yo + h + 1; ++y) {
                        if (this.height[x][y] > this.groundHeight[x][y]) {
                            this.height[x][y] = this.height[x][y] * 0.95f + this.groundHeight[x][y] * 0.05f;
                        }
                        if (x >= xo + w || y >= yo + h) continue;
                        this.textures[x][y] = Tiles.Tile.TILE_CLAY.id;
                    }
                }
            } else {
                --i;
            }
            ++i;
        }
    }

    private void exposeTar(long amount, Random random) {
        logger.info("Attempting to expose " + amount + " tiles of tar");
        int i = 0;
        while ((long)i < amount) {
            int y;
            int x;
            int xo = random.nextInt(this.width - 10) + 1;
            int yo = random.nextInt(this.width - 10) + 1;
            int w = random.nextInt(2) + 2;
            int h = random.nextInt(2) + 2;
            boolean fail = false;
            for (x = xo - 1; x < xo + w + 1; ++x) {
                for (y = yo - 1; y < yo + h + 1; ++y) {
                    if (this.textures[x][y] == Tiles.Tile.TILE_ROCK.id) {
                        fail = true;
                    }
                    if (this.textures[x][y] == Tiles.Tile.TILE_CLIFF.id) {
                        fail = true;
                    }
                    if (this.textures[x][y] != Tiles.Tile.TILE_TAR.id) continue;
                    fail = true;
                }
            }
            if (!fail) {
                for (x = xo; x < xo + w + 1; ++x) {
                    for (y = yo; y < yo + h + 1; ++y) {
                        if (this.height[x][y] > this.groundHeight[x][y]) {
                            this.height[x][y] = this.height[x][y] * 0.95f + this.groundHeight[x][y] * 0.05f;
                        }
                        if (x >= xo + w || y >= yo + h) continue;
                        this.textures[x][y] = Tiles.Tile.TILE_TAR.id;
                    }
                }
            }
            ++i;
        }
    }

    private boolean isRock(int x, int y) {
        for (int xx = 0; xx < 2; ++xx) {
            for (int yy = 0; yy < 2; ++yy) {
                if (!(this.getGroundHeight(x + xx, y + yy) < this.getHeight(x + xx, y + yy))) continue;
                return false;
            }
        }
        return true;
    }

    private boolean isWater(int x, int y) {
        for (int xx = 0; xx < 2; ++xx) {
            for (int yy = 0; yy < 2; ++yy) {
                if (!(this.getHeight(x + xx, y + yy) < 0.0f)) continue;
                return true;
            }
        }
        return false;
    }

    public void generateTextures(Random random, MeshGenGui.Task task) {
        logger.info("Generating texture");
        this.textureDatas = new byte[this.width][this.width];
        task.setMax(100);
        task.setNote(0, "  Convert underwater grass into dirt.");
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                if (this.isRock(x, y)) continue;
                if (!this.isWater(x, y)) {
                    this.setTile(GRASSID, x, y, random);
                    continue;
                }
                this.textures[x][y] = Tiles.Tile.TILE_DIRT.id;
            }
        }
        task.setNote(10, "  Adding blotches.");
        int wsqd = this.width * this.width / 10240;
        boolean sizeMod = true;
        task.setNote(10, "    dirt.");
        this.blotch(Tiles.Tile.TILE_DIRT.id, wsqd / 3, 2, 1, false, true, false, false, random);
        task.setNote(11, "    peat.");
        this.blotch(Tiles.Tile.TILE_PEAT.id, wsqd / 4, 3, 2, false, false, false, false, random);
        task.setNote(12, "    steppe.");
        this.blotch(Tiles.Tile.TILE_STEPPE.id, wsqd / 17 / 1, 4, 8, false, false, false, false, random);
        task.setNote(13, "    desert.");
        this.blotch(Tiles.Tile.TILE_SAND.id, wsqd / 15 / 1, 3, 8, false, false, false, false, random);
        task.setNote(14, "    tundra.");
        this.blotch(Tiles.Tile.TILE_TUNDRA.id, wsqd / 18 / 1, 3, 7, false, false, false, false, random);
        task.setNote(15, "    moss.");
        this.blotch(Tiles.Tile.TILE_MOSS.id, wsqd / 5, 1, 2, false, false, false, false, random);
        task.setNote(16, "    gravel.");
        this.blotch(Tiles.Tile.TILE_GRAVEL.id, wsqd / 3, 5, 2, false, true, false, true, random);
        task.setNote(17, "    underwater sand.");
        this.blotch(Tiles.Tile.TILE_SAND.id, wsqd * 4, 2, 3, true, false, true, false, random);
        task.setNote(18, "    marsh.");
        this.blotch(Tiles.Tile.TILE_MARSH.id, wsqd / 2, 4, 5, true, false, false, true, random);
        task.setNote(19, "    reeds and kelp.");
        this.blotch(Tiles.Tile.TILE_REED.id, wsqd / 2, 2, 3, true, false, false, true, random);
        logger.info("Adding random trees.");
        task.setNote(25, "  Adding random trees.");
        for (int i = 0; i < this.width * this.width * 1; ++i) {
            int type;
            int x = random.nextInt(this.width);
            int y = random.nextInt(this.width);
            if (!(random.nextFloat() < 0.04f) || !this.isTreeCapable(x, y, 5)) continue;
            int age = this.generateAge(random);
            GrassData.GrowthTreeStage grassLen = GrassData.GrowthTreeStage.fromInt(random.nextInt(3) + 1);
            if (random.nextInt(4) == 2) {
                type = random.nextInt(BushData.BushType.getLength());
                this.textures[x][y] = BushData.BushType.fromInt(type).asNormalBush();
                this.textureDatas[x][y] = Tiles.encodeTreeData(FoliageAge.fromByte((byte)age), false, false, grassLen);
                continue;
            }
            type = random.nextInt(TreeData.TreeType.getLength());
            if (type == TreeData.TreeType.OAK.getTypeId() && random.nextInt(3) != 0) {
                type = TreeData.TreeType.BIRCH.getTypeId();
            }
            if (type == TreeData.TreeType.WILLOW.getTypeId() && random.nextInt(2) != 0) {
                type = TreeData.TreeType.PINE.getTypeId();
            }
            this.textures[x][y] = TreeData.TreeType.fromInt(type).asNormalTree();
            this.textureDatas[x][y] = Tiles.encodeTreeData(FoliageAge.fromByte((byte)age), false, false, grassLen);
        }
        logger.info("Making Forests.");
        task.setNote(30, "  Making forests.");
        int info = 31;
        int infotick = this.width * 16 / 60;
        for (int i = 0; i < this.width * 16; ++i) {
            int y;
            int x;
            Tiles.Tile tex;
            if ((i + 1) % infotick == 0) {
                task.setNote(info);
                ++info;
            }
            if (!(tex = Tiles.getTile(this.textures[x = random.nextInt(this.width)][y = random.nextInt(this.width)])).isBush() && !tex.isTree()) continue;
            this.makeForest(x, y, tex, this.textureDatas[x][y], random);
        }
        task.setNote(92, "  Adding grass plains...");
        this.blotch(Tiles.Tile.TILE_GRASS.id, wsqd / 2 / 1, 3, 20, false, false, false, false, random);
        task.setNote(95, "  Exposing some tar...");
        this.exposeTar(wsqd, random);
        task.setNote(96, "  Exposing some clay...");
        this.exposeClay(wsqd, random);
        logger.info("Finished!");
    }

    private void makeForest(int x, int y, Tiles.Tile theTile, byte data, Random random) {
        int sqrw = (int)Math.sqrt(this.width);
        int maxForestSize = theTile.isBush() ? this.width / sqrw / 2 : this.width / sqrw;
        for (int i = 0; i < sqrw * 10; ++i) {
            int count = 0;
            int scarcity = theTile.isOak(data) || theTile.isWillow(data) ? 0 : random.nextInt(3) * 2 + 1;
            for (int j = 0; j < maxForestSize; ++j) {
                int yy;
                int xx = x + random.nextInt(maxForestSize * 2 + 1) - maxForestSize;
                if (!this.isTreeCapable(xx, yy = y + random.nextInt(maxForestSize * 2 + 1) - maxForestSize, scarcity)) continue;
                this.addTree(x, y, xx, yy, random);
                ++count;
            }
            if (count == 0) break;
        }
    }

    private void addTree(int origx, int origy, int x, int y, Random random) {
        this.textures[x][y] = this.textures[origx][origy];
        int age = this.generateAge(random);
        GrassData.GrowthTreeStage grassLen = GrassData.GrowthTreeStage.fromInt(random.nextInt(3) + 1);
        this.textureDatas[x][y] = Tiles.encodeTreeData(FoliageAge.fromByte((byte)age), false, false, grassLen);
    }

    private int generateAge(Random random) {
        int age = random.nextInt(13) + 1;
        int age2 = random.nextInt(13) + 1;
        if (age2 > age) {
            age = age2;
        }
        if ((age2 = random.nextInt(13) + 1) > age) {
            age = age2;
        }
        return age;
    }

    private boolean isTreeCapable(int x, int y, int maxNeighbours) {
        if (x < 0 || y < 0 || x >= this.width || y >= this.width) {
            return false;
        }
        if (this.textures[x & this.width - 1][y & this.width - 1] != GRASSID) {
            return false;
        }
        if (this.getGroundHeight(x, y) > 0.65f) {
            return false;
        }
        int neighborTrees = 0;
        for (int xx = x - 1; xx <= x + 1; ++xx) {
            for (int yy = y - 1; yy <= y + 1; ++yy) {
                int xxx = xx & this.width - 1;
                int yyy = yy & this.width - 1;
                Tiles.Tile theTile = Tiles.getTile(this.textures[xxx][yyy]);
                if (theTile.isTree()) {
                    byte data = this.textureDatas[xxx][yyy];
                    if (theTile.isOak(data) || theTile.isWillow(data)) {
                        return false;
                    }
                    ++neighborTrees;
                    continue;
                }
                if (!theTile.isBush()) continue;
                ++neighborTrees;
            }
        }
        return neighborTrees <= maxNeighbours;
    }

    public BufferedImage getImage(MeshGenGui.Task task) {
        return this.getImage(this.imageLayer, task);
    }

    public BufferedImage getImage(int layer, MeshGenGui.Task task) {
        int xo;
        int yo;
        task.setMax(this.width + 50);
        task.setNote(0, "Generating image.");
        int lWidth = 16384;
        if (lWidth > this.width) {
            lWidth = this.width;
        }
        if ((yo = this.width - lWidth) < 0) {
            yo = 0;
        }
        if ((xo = this.width - lWidth) < 0) {
            xo = 0;
        }
        Random random = new Random();
        if (xo > 0) {
            xo = random.nextInt(xo);
        }
        if (yo > 0) {
            yo = random.nextInt(yo);
        }
        BufferedImage bi2 = new BufferedImage(lWidth, lWidth, 1);
        float[] data = new float[lWidth * lWidth * 3];
        task.setNote(1, "  Generate colours.");
        for (int x = 0; x < lWidth; ++x) {
            task.setNote(x + 2);
            int alt = lWidth - 1;
            for (int y = lWidth - 1; y >= 0; --y) {
                float node = 0.0f;
                float node2 = 0.0f;
                if (layer == 0) {
                    node = this.getHeight(x + xo, y + yo);
                    node2 = this.getHeight(x + 1 + xo, y + 1 + yo);
                } else {
                    node = this.getGroundHeight(x + xo, y + yo);
                    node2 = this.getGroundHeight(x + 1 + xo, y + 1 + yo);
                }
                byte tex = this.textures[x + xo][y + yo];
                float hh = node;
                float h = (node2 - node) * 1500.0f / 256.0f * (float)(1 << this.level) / 128.0f + hh / 2.0f + 1.0f;
                float r = h *= 0.4f;
                float g = h;
                float b = h;
                if (layer == 0) {
                    Color color = Tiles.getTile(tex).getColor();
                    r *= (float)color.getRed() / 255.0f * 2.0f;
                    g *= (float)color.getGreen() / 255.0f * 2.0f;
                    b *= (float)color.getBlue() / 255.0f * 2.0f;
                }
                if (r < 0.0f) {
                    r = 0.0f;
                }
                if (r > 1.0f) {
                    r = 1.0f;
                }
                if (g < 0.0f) {
                    g = 0.0f;
                }
                if (g > 1.0f) {
                    g = 1.0f;
                }
                if (b < 0.0f) {
                    b = 0.0f;
                }
                if (b > 1.0f) {
                    b = 1.0f;
                }
                if (node < 0.0f) {
                    r = r * 0.2f + 0.16000001f;
                    g = g * 0.2f + 0.2f;
                    b = b * 0.2f + 0.4f;
                }
                int altTarget = y - (int)(this.getHeight(x, y) * 1000.0f / 4.0f);
                while (alt > altTarget && alt >= 0) {
                    data[(x + alt * lWidth) * 3 + 0] = r * 255.0f;
                    data[(x + alt * lWidth) * 3 + 1] = g * 255.0f;
                    data[(x + alt * lWidth) * 3 + 2] = b * 255.0f;
                    --alt;
                }
            }
        }
        task.setNote(this.width + 10, "  Convert colours to image.");
        bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
        return bi2;
    }

    protected int[] getData(MeshGenGui.Task task) {
        logger.info("Getting data for a " + this.width + 'x' + this.width + " map. Map height is " + 1000.0f);
        task.setMax(this.width);
        task.setNote(0, "getting Surface Data");
        int[] data = new int[this.width * this.width];
        int x = 0;
        int y = 0;
        try {
            for (y = 0; y < this.width; ++y) {
                task.setNote(y);
                for (x = 0; x < this.width; ++x) {
                    float lHeight = this.getHeight(x, y) * 1000.0f;
                    byte tex = this.textures[x][y];
                    byte texdata = this.textureDatas[x][y];
                    data[x + (y << this.level)] = Tiles.encode(lHeight, tex, texdata);
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.WARNING, "data: " + data.length + ", x: " + x + ", y: " + y + ", x + (y << (level + 1): " + (x + (y << this.level + 1)), e);
            throw e;
        }
        return data;
    }

    protected int[] getRockData(MeshGenGui.Task task) {
        logger.info("Getting rock data for a " + this.width + 'x' + this.width + " map. Map height is " + 1000.0f);
        task.setMax(this.width);
        task.setNote(0, "getting Surface Data");
        int[] data = new int[this.width * this.width];
        for (int y = 0; y < this.width; ++y) {
            task.setNote(y);
            for (int x = 0; x < this.width; ++x) {
                float lHeight = this.getGroundHeight(x, y);
                boolean tex = false;
                boolean texdata = false;
                data[x + (y << this.level)] = Tiles.encode(lHeight * 1000.0f, (byte)0, (byte)0);
            }
        }
        return data;
    }

    protected int getLevel() {
        return this.level;
    }

    public void setData(MeshIO meshIO, MeshIO meshIO2, MeshGenGui.Task task) {
        int tile;
        int y;
        int x;
        task.setMax(this.width * 2);
        task.setNote(0, "setting Data");
        for (x = 0; x < this.width; ++x) {
            task.setNote(x);
            for (y = 0; y < this.width; ++y) {
                tile = meshIO.getTile(x, y);
                this.setHeight(x, y, Tiles.decodeHeightAsFloat(tile) / 1000.0f);
                this.textures[x][y] = Tiles.decodeType(tile);
                this.textureDatas[x][y] = Tiles.decodeData(tile);
            }
        }
        for (x = 0; x < this.width; ++x) {
            task.setNote(this.width + x);
            for (y = 0; y < this.width; ++y) {
                float orgHeight;
                tile = meshIO2.getTile(x, y);
                this.groundHeight[x][y] = orgHeight = Tiles.decodeHeightAsFloat(tile) / 1000.0f;
            }
        }
    }

    public int getImageLayer() {
        return this.imageLayer;
    }

    public void setImageLayer(int aImageLayer) {
        this.imageLayer = aImageLayer;
    }
}

