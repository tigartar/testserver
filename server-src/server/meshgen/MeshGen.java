package com.wurmonline.server.meshgen;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
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
   private static final float MAP_HEIGHT = 1000.0F;
   private static final boolean USE_SPLIT_TREES = true;
   private static final boolean USE_DROP_DIRT_METHOD = false;
   private static final int NUMBER_OF_DIRT_TO_DROP = 40;
   private static final boolean CHECK_STRAIGHT_SLOPES = true;
   private static final int MAX_STRAIGHT_SLOPE = 20;
   private static final boolean CHECK_DIAGONAL_SLOPES = true;
   private static final int MAX_DIAGONAL_SLOPE = 20;
   private static final boolean OLD_MAP_STYLE_ROTATE_AND_FLIP = true;
   private static final float waterBias = 0.1F;
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

      for(int x = 0; x < this.width; ++x) {
         for(int y = 0; y < this.width; ++y) {
            this.textures[x][y] = -1;
            this.height[x][y] = 0.0F;
         }
      }

      perlin.setRandom(new Random(random.nextLong()));

      for(int i = 0; i < 3; ++i) {
         perlin.setRandom(new Random(random.nextLong()));
         task.setNote("Calculating perlin noise..");
         float[][] hs1 = perlin.generatePerlinNoise(0.3F, i == 0 ? 0 : 1, task, this.width * i * 2, 0);

         for(int x = 0; x < this.width; ++x) {
            task.setNote(x + this.width * i * 2 + this.width);

            for(int y = 0; y < this.width; ++y) {
               float h = hs1[x][y] - this.height[x][y];
               if (h < 0.0F) {
                  h = -h;
               }

               h = (float)Math.pow((double)h, 1.2);
               this.height[x][y] = h;
            }
         }
      }
   }

   public final int getWidth() {
      return this.width;
   }

   protected void setData(float[] data, MeshGenGui.Task task) throws Exception {
      task.setMax(this.width * 7);
      task.setNote(0, "Normalizing.");
      Random grassRand = new Random();
      float lowest = Float.MAX_VALUE;
      float highest = Float.MIN_VALUE;
      float numsover50 = 0.0F;
      task.setNote("  Pass 1");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x);

         for(int y = 0; y < this.width; ++y) {
            this.height[x][y] = data[y + x * this.width];
            float n1 = this.height[x][y];
            if (n1 < lowest) {
               lowest = n1;
            }

            if (n1 > highest) {
               highest = n1;
            }

            if (n1 > 0.1F) {
               ++numsover50;
            }

            for(int xx = x - 1; xx <= x + 1; ++xx) {
               for(int yy = y - 1; yy <= y + 1; ++yy) {
                  if (xx >= 0 && yy >= 0 && xx < this.width && yy < this.width) {
                     this.height[x][y] += Math.min(data[y + x * this.width], data[yy + xx * this.width]);
                  } else {
                     this.height[x][y] += data[y + x * this.width];
                  }
               }
            }

            this.height[x][y] /= 10.0F;
         }
      }

      float maxHeight = highest - lowest;
      System.out
         .println(
            "Before percent over 0.1="
               + numsover50 / (float)(this.width * this.width)
               + " highest="
               + highest
               + " lowest="
               + lowest
               + " maxheight="
               + maxHeight
         );
      lowest = Float.MAX_VALUE;
      highest = Float.MIN_VALUE;
      numsover50 = 0.0F;
      task.setNote("  Pass 2");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width);

         for(int y = 0; y < this.width; ++y) {
            float n1 = this.height[x][y];
            if (n1 < lowest) {
               lowest = n1;
            }

            if (n1 > highest) {
               highest = n1;
            }

            if (n1 > 0.1F) {
               ++numsover50;
            }
         }
      }

      maxHeight = highest - lowest;
      System.out
         .println(
            "After percent over 0.1="
               + numsover50 / (float)(this.width * this.width)
               + " highest="
               + highest
               + " lowest="
               + lowest
               + " maxheight="
               + maxHeight
         );
      task.setNote("  Pass 3");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width * 2);

         for(int y = 0; y < this.width; ++y) {
            float n1 = this.height[x][y];
            float h = (n1 - 0.1F) / 0.9F;
            if (h > 0.0F) {
               h += 1.0E-4F;
            } else {
               h *= 0.5F;
               h -= 1.0E-4F;
            }

            this.height[x][y] = h;
            this.groundHeight[x][y] = h;
         }
      }

      lowest = Float.MAX_VALUE;
      highest = Float.MIN_VALUE;
      numsover50 = 0.0F;
      task.setNote("  Pass 4");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width * 3);

         for(int y = 0; y < this.width; ++y) {
            float n1 = this.height[x][y];
            if (n1 < lowest) {
               lowest = n1;
            }

            if (n1 > highest) {
               highest = n1;
            }

            if (n1 > 0.1F) {
               ++numsover50;
            }
         }
      }

      maxHeight = highest - lowest;
      System.out
         .println(
            "After THIRD percent over 0.1="
               + numsover50 / (float)(this.width * this.width)
               + " highest="
               + highest
               + " lowest="
               + lowest
               + " maxheight="
               + maxHeight
         );
      System.out.println("Creating rock layer.");
      float waterConstant = 1.1F;
      float mapSizeInfluence = 0.035F;
      float mapSizeMod = (float)this.width * 0.035F;
      float influenceMod = 0.02F;
      System.out.println("mapSizeMod=" + mapSizeMod + ", waterConstant=" + 1.1F + ", influenceMod=" + 0.02F);
      task.setNote("  Pass 5 - Creating rock layer.");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width * 4);

         for(int y = 0; y < this.width; ++y) {
            float hh = Math.max(0.0F, (this.height[x][y] + 0.1F) / 1.1F);
            float heightModifier = 1.1F - hh;
            float subtracted = (1.0F - this.getDirtSlope(x, y) * mapSizeMod) * 0.02F * heightModifier / 3.0F;
            if (x == 3274 && y == 1425) {
               System.out
                  .println(
                     "dslope="
                        + this.getDirtSlope(x, y)
                        + ", subtracted="
                        + subtracted
                        + " heightmod="
                        + heightModifier
                        + " height "
                        + x
                        + ","
                        + y
                        + "="
                        + this.height[x][y]
                  );
            }

            float h = this.height[x][y] - subtracted;
            this.groundHeight[x][y] = h;
            if (this.groundHeight[x][y] > this.height[x][y]) {
               this.groundHeight[x][y] = this.height[x][y];
            }
         }
      }

      task.setNote("  Pass 6 - Applying Cliff, Rock, and Grass tiles.");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width * 5);

         for(int y = 0; y < this.width; ++y) {
            boolean rock = true;

            for(int xx = 0; xx < 2; ++xx) {
               for(int yy = 0; yy < 2; ++yy) {
                  if (this.getGroundHeight(x + xx, y + yy) < this.getHeight(x + xx, y + yy)) {
                     if (x == 3274 && y == 1425) {
                        System.out.println("ggh=" + this.getGroundHeight(x + xx, y + yy) + ", gh=" + this.getHeight(x + xx, y + yy) + ": rock false");
                     }

                     rock = false;
                  }
               }
            }

            if (rock) {
               this.textures[x][y] = Tiles.Tile.TILE_ROCK.id;
               this.textureDatas[x][y] = 0;
            } else {
               this.setTile(GRASSID, x, y, grassRand);
            }
         }
      }

      task.setNote("  Pass 7 - Check Heights of Rock and Cliff tiles");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x + this.width * 6);

         for(int y = 0; y < this.width; ++y) {
            if ((this.textures[x][y] == Tiles.Tile.TILE_CLIFF.id || this.textures[x][y] == Tiles.Tile.TILE_ROCK.id)
               && this.getGroundHeight(x, y) < this.getHeight(x, y)) {
               System.out.println("Cliff Error at " + x + ", " + y);
            }
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
      return (float)Math.sqrt((double)(vs * vs + hs * hs));
   }

   public void generateGround(Random random, MeshGenGui.Task task) {
      int blurSteps = 1;
      task.setMax(this.width * 1);
      task.setNote("Flowing water..");

      for(int i = 0; i < this.width * this.width / 1000; ++i) {
         task.setNote(i);
      }

      float[][] h = new float[this.width][this.width];

      for(int i = 0; i < 1; ++i) {
         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.width; ++y) {
               System.out.println("Setting " + x + ", " + y + " to 0");
               h[x][y] = 0.0F;
            }
         }

         for(int x = 0; x < this.width; ++x) {
            task.setNote(x + i * this.width);
            int y = 0;

            while(y < this.width) {
               ++y;
            }
         }

         for(int x = 0; x < this.width; ++x) {
            for(int y = 0; y < this.width; ++y) {
               h[x][y] = 0.0F;
            }
         }
      }

      for(int x = 0; x < this.width; ++x) {
         int y = 0;

         while(y < this.width) {
            ++y;
         }
      }
   }

   public void generateWater() {
   }

   public float getHeight(int x, int y) {
      return x >= 0 && y >= 0 && x <= this.width - 1 && y <= this.width - 1 ? this.height[x & this.width - 1][y & this.width - 1] : 0.0F;
   }

   public float getGroundHeight(int x, int y) {
      return x >= 0 && y >= 0 && x <= this.width - 1 && y <= this.width - 1 ? this.groundHeight[x & this.width - 1][y & this.width - 1] : 0.0F;
   }

   public float getHeightAndWater(int x, int y) {
      x &= this.width - 1;
      y &= this.width - 1;
      return this.height[x][y];
   }

   public void setHeight(int x, int y, float h) {
      this.height[x & this.width - 1][y & this.width - 1] = h;
   }

   public void dropADirt(boolean forceDrop, Random random) {
      for(int x = 0; x < this.width; ++x) {
         for(int y = 0; y < this.width; ++y) {
            if (forceDrop || this.getHeight(x, y) > -1.0F) {
               Point p = this.findDropTile(x, y, random);
               this.incHeight(p.getX(), p.getY());
            }
         }
      }
   }

   private void incHeight(int x, int y) {
      this.setHeight(x, y, this.height[x & this.width - 1][y & this.width - 1] + 1.0E-4F);
   }

   private Point findDropTile(int tileX, int tileY, Random random) {
      ArrayList<Point> slopes = new ArrayList<>();
      short h = (short)((int)(this.getHeight(tileX, tileY) * 1000.0F * 10.0F));

      for(int xx = 1; xx >= -1; --xx) {
         for(int yy = 1; yy >= -1; --yy) {
            short th = (short)((int)(this.getHeight(tileX + xx, tileY + yy) * 1000.0F * 10.0F));
            if ((xx == 0 && yy != 0 || yy == 0 && xx != 0) && th < h - 20) {
               slopes.add(new Point(tileX + xx, tileY + yy));
            }

            if (xx != 0 && yy != 0 && th < h - 20) {
               slopes.add(new Point(tileX + xx, tileY + yy));
            }
         }
      }

      if (slopes.size() > 0) {
         int r = 0;
         if (slopes.size() > 1) {
            r = random.nextInt(slopes.size());
         }

         return this.findDropTile(slopes.get(r).getX(), slopes.get(r).getY(), random);
      } else {
         return new Point(tileX, tileY);
      }
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

      for(int xk = 0; xk < this.width - 1; ++xk) {
         for(int yk = 0; yk < this.width - 1; ++yk) {
            float theight = this.getHeight(xk, yk) * 1000.0F;
            if (theight <= 0.0F && theight > -40.0F) {
               grassRand.setSeed((long)(grassSeed + xk / grassCommonality + yk / grassCommonality * 10000));
               int randXResult = grassRand.nextInt(5);
               int randYResult = grassRand.nextInt(5);
               if (randXResult == 0 && randYResult == 0) {
                  grassRand.setSeed(System.nanoTime());
                  if (grassRand.nextBoolean()) {
                     byte tileType = Tiles.Tile.TILE_REED.id;
                     if (theight < -2.0F) {
                        tileType = Tiles.Tile.TILE_KELP.id;
                     }

                     byte tileData = GrassData.encodeGrassTileData(GrassData.GrowthStage.SHORT, GrassData.FlowerType.NONE);
                     this.textures[xk][yk] = tileType;
                     this.textureDatas[xk][yk] = tileData;
                  }
               }
            }
         }
      }
   }

   private void blotch(
      byte id, long amount, int spread, int sizeScale, boolean underwater, boolean nearRock, boolean waterOrNoWater, boolean shallowOnly, Random random
   ) {
      logger.info(
         "Adding blotch of "
            + Tiles.getTile(id).getName()
            + ". Amount="
            + amount
            + ", spread="
            + spread
            + ", sizeSale="
            + sizeScale
            + ", underwater="
            + underwater
            + ", nearRock="
            + nearRock
            + ", waterOrNoWater="
            + waterOrNoWater
            + ", shallowOnly="
            + shallowOnly
      );

      for(int i = 0; (long)i < amount; ++i) {
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

            ok = (waterOrNoWater || !this.isWater(xo, yo)) && !this.isRock(xo, yo);
         }

         if (ok) {
            int size = (random.nextInt(80) + 10) * sizeScale;

            for(int j = 0; j < size / 5; ++j) {
               int x = xo;
               int y = yo;

               for(int k = 0; k < size / 5; ++k) {
                  if (random.nextInt(2) == 0) {
                     x = x + random.nextInt(spread * 2 + 1) - spread;
                  } else {
                     y = y + random.nextInt(spread * 2 + 1) - spread;
                  }

                  x &= this.width - 1;
                  y &= this.width - 1;

                  for(int xk = x; xk < x + sizeScale / 2 + 1; ++xk) {
                     for(int yk = y; yk < y + sizeScale / 2 + 1; ++yk) {
                        boolean tileSubmerged = this.isWater(xk, yk);
                        if (underwater && !tileSubmerged) {
                           ++k;
                        }

                        if (tileSubmerged && !underwater || this.isRock(xk, yk)) {
                           break;
                        }

                        float theight = this.getHeight(xk, yk);
                        if (shallowOnly) {
                           if (theight * 1000.0F > -1.0F) {
                              this.setTile(id, xk & this.width - 1, yk & this.width - 1, random);
                           }
                        } else {
                           this.setTile(id, xk & this.width - 1, yk & this.width - 1, random);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void setTile(byte id, int x, int y, Random random) {
      if (id == GRASSID) {
         GrassData.GrowthStage growthStage = GrassData.GrowthStage.fromInt(random.nextInt(4));
         GrassData.FlowerType flowerType = getRandomFlower(6);
         this.textures[x][y] = id;
         this.textureDatas[x][y] = GrassData.encodeGrassTileData(growthStage, flowerType);
      } else if (id == Tiles.Tile.TILE_REED.id) {
         float theight = this.getHeight(x, y) * 1000.0F;
         if (theight < -2.0F) {
            this.textures[x][y] = Tiles.Tile.TILE_KELP.id;
         } else {
            this.textures[x][y] = Tiles.Tile.TILE_REED.id;
         }

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
         } else if (rnd > 990) {
            return GrassData.FlowerType.FLOWER_6;
         } else if (rnd > 962) {
            return GrassData.FlowerType.FLOWER_5;
         } else if (rnd > 900) {
            return GrassData.FlowerType.FLOWER_4;
         } else if (rnd > 800) {
            return GrassData.FlowerType.FLOWER_3;
         } else {
            return rnd > 500 ? GrassData.FlowerType.FLOWER_2 : GrassData.FlowerType.FLOWER_1;
         }
      } else {
         return GrassData.FlowerType.NONE;
      }
   }

   private void exposeClay(long amount, Random random) {
      logger.info("Attempting to expose " + amount + " tiles of clay");

      for(int i = 0; (long)i < amount; ++i) {
         int xo = random.nextInt(this.width - 10) + 1;
         int yo = random.nextInt(this.width - 10) + 1;
         int w = random.nextInt(2) + 2;
         int h = random.nextInt(2) + 2;
         boolean fail = false;
         boolean below = this.height[xo][yo] * 1000.0F < -4.0F;
         boolean above = this.height[xo][yo] * 1000.0F > 2.0F;
         if (!below && !above) {
            if (this.height[xo][yo] < 0.0F && this.height[xo + w][yo + h] < 0.0F) {
               fail = true;
            }
         } else {
            fail = true;
         }

         if (!fail) {
            for(int x = xo - 1; x < xo + w + 1; ++x) {
               for(int y = yo - 1; y < yo + h + 1; ++y) {
                  if (this.textures[x][y] == Tiles.Tile.TILE_ROCK.id) {
                     fail = true;
                  }

                  if (this.textures[x][y] == Tiles.Tile.TILE_CLIFF.id) {
                     fail = true;
                  }

                  if (this.textures[x][y] == Tiles.Tile.TILE_TAR.id) {
                     fail = true;
                  }

                  if (this.textures[x][y] == Tiles.Tile.TILE_CLAY.id) {
                     fail = true;
                  }
               }
            }
         }

         if (fail) {
            --i;
         } else {
            System.out.print(".");

            for(int x = xo; x < xo + w + 1; ++x) {
               for(int y = yo; y < yo + h + 1; ++y) {
                  if (this.height[x][y] > this.groundHeight[x][y]) {
                     this.height[x][y] = this.height[x][y] * 0.95F + this.groundHeight[x][y] * 0.05F;
                  }

                  if (x < xo + w && y < yo + h) {
                     this.textures[x][y] = Tiles.Tile.TILE_CLAY.id;
                  }
               }
            }
         }
      }
   }

   private void exposeTar(long amount, Random random) {
      logger.info("Attempting to expose " + amount + " tiles of tar");

      for(int i = 0; (long)i < amount; ++i) {
         int xo = random.nextInt(this.width - 10) + 1;
         int yo = random.nextInt(this.width - 10) + 1;
         int w = random.nextInt(2) + 2;
         int h = random.nextInt(2) + 2;
         boolean fail = false;

         for(int x = xo - 1; x < xo + w + 1; ++x) {
            for(int y = yo - 1; y < yo + h + 1; ++y) {
               if (this.textures[x][y] == Tiles.Tile.TILE_ROCK.id) {
                  fail = true;
               }

               if (this.textures[x][y] == Tiles.Tile.TILE_CLIFF.id) {
                  fail = true;
               }

               if (this.textures[x][y] == Tiles.Tile.TILE_TAR.id) {
                  fail = true;
               }
            }
         }

         if (!fail) {
            for(int x = xo; x < xo + w + 1; ++x) {
               for(int y = yo; y < yo + h + 1; ++y) {
                  if (this.height[x][y] > this.groundHeight[x][y]) {
                     this.height[x][y] = this.height[x][y] * 0.95F + this.groundHeight[x][y] * 0.05F;
                  }

                  if (x < xo + w && y < yo + h) {
                     this.textures[x][y] = Tiles.Tile.TILE_TAR.id;
                  }
               }
            }
         }
      }
   }

   private boolean isRock(int x, int y) {
      for(int xx = 0; xx < 2; ++xx) {
         for(int yy = 0; yy < 2; ++yy) {
            if (this.getGroundHeight(x + xx, y + yy) < this.getHeight(x + xx, y + yy)) {
               return false;
            }
         }
      }

      return true;
   }

   private boolean isWater(int x, int y) {
      for(int xx = 0; xx < 2; ++xx) {
         for(int yy = 0; yy < 2; ++yy) {
            if (this.getHeight(x + xx, y + yy) < 0.0F) {
               return true;
            }
         }
      }

      return false;
   }

   public void generateTextures(Random random, MeshGenGui.Task task) {
      logger.info("Generating texture");
      this.textureDatas = new byte[this.width][this.width];
      task.setMax(100);
      task.setNote(0, "  Convert underwater grass into dirt.");

      for(int x = 0; x < this.width; ++x) {
         for(int y = 0; y < this.width; ++y) {
            if (!this.isRock(x, y)) {
               if (!this.isWater(x, y)) {
                  this.setTile(GRASSID, x, y, random);
               } else {
                  this.textures[x][y] = Tiles.Tile.TILE_DIRT.id;
               }
            }
         }
      }

      task.setNote(10, "  Adding blotches.");
      int wsqd = this.width * this.width / 10240;
      int sizeMod = 1;
      task.setNote(10, "    dirt.");
      this.blotch(Tiles.Tile.TILE_DIRT.id, (long)(wsqd / 3), 2, 1, false, true, false, false, random);
      task.setNote(11, "    peat.");
      this.blotch(Tiles.Tile.TILE_PEAT.id, (long)(wsqd / 4), 3, 2, false, false, false, false, random);
      task.setNote(12, "    steppe.");
      this.blotch(Tiles.Tile.TILE_STEPPE.id, (long)(wsqd / 17 / 1), 4, 8, false, false, false, false, random);
      task.setNote(13, "    desert.");
      this.blotch(Tiles.Tile.TILE_SAND.id, (long)(wsqd / 15 / 1), 3, 8, false, false, false, false, random);
      task.setNote(14, "    tundra.");
      this.blotch(Tiles.Tile.TILE_TUNDRA.id, (long)(wsqd / 18 / 1), 3, 7, false, false, false, false, random);
      task.setNote(15, "    moss.");
      this.blotch(Tiles.Tile.TILE_MOSS.id, (long)(wsqd / 5), 1, 2, false, false, false, false, random);
      task.setNote(16, "    gravel.");
      this.blotch(Tiles.Tile.TILE_GRAVEL.id, (long)(wsqd / 3), 5, 2, false, true, false, true, random);
      task.setNote(17, "    underwater sand.");
      this.blotch(Tiles.Tile.TILE_SAND.id, (long)(wsqd * 4), 2, 3, true, false, true, false, random);
      task.setNote(18, "    marsh.");
      this.blotch(Tiles.Tile.TILE_MARSH.id, (long)(wsqd / 2), 4, 5, true, false, false, true, random);
      task.setNote(19, "    reeds and kelp.");
      this.blotch(Tiles.Tile.TILE_REED.id, (long)(wsqd / 2), 2, 3, true, false, false, true, random);
      logger.info("Adding random trees.");
      task.setNote(25, "  Adding random trees.");

      for(int i = 0; i < this.width * this.width * 1; ++i) {
         int x = random.nextInt(this.width);
         int y = random.nextInt(this.width);
         if (random.nextFloat() < 0.04F && this.isTreeCapable(x, y, 5)) {
            int age = this.generateAge(random);
            GrassData.GrowthTreeStage grassLen = GrassData.GrowthTreeStage.fromInt(random.nextInt(3) + 1);
            if (random.nextInt(4) == 2) {
               int type = random.nextInt(BushData.BushType.getLength());
               this.textures[x][y] = BushData.BushType.fromInt(type).asNormalBush();
               this.textureDatas[x][y] = Tiles.encodeTreeData(FoliageAge.fromByte((byte)age), false, false, grassLen);
            } else {
               int type = random.nextInt(TreeData.TreeType.getLength());
               if (type == TreeData.TreeType.OAK.getTypeId() && random.nextInt(3) != 0) {
                  type = TreeData.TreeType.BIRCH.getTypeId();
               }

               if (type == TreeData.TreeType.WILLOW.getTypeId() && random.nextInt(2) != 0) {
                  type = TreeData.TreeType.PINE.getTypeId();
               }

               this.textures[x][y] = TreeData.TreeType.fromInt(type).asNormalTree();
               this.textureDatas[x][y] = Tiles.encodeTreeData(FoliageAge.fromByte((byte)age), false, false, grassLen);
            }
         }
      }

      logger.info("Making Forests.");
      task.setNote(30, "  Making forests.");
      int info = 31;
      int infotick = this.width * 16 / 60;

      for(int i = 0; i < this.width * 16; ++i) {
         if ((i + 1) % infotick == 0) {
            task.setNote(info);
            ++info;
         }

         int x = random.nextInt(this.width);
         int y = random.nextInt(this.width);
         Tiles.Tile tex = Tiles.getTile(this.textures[x][y]);
         if (tex.isBush() || tex.isTree()) {
            this.makeForest(x, y, tex, this.textureDatas[x][y], random);
         }
      }

      task.setNote(92, "  Adding grass plains...");
      this.blotch(Tiles.Tile.TILE_GRASS.id, (long)(wsqd / 2 / 1), 3, 20, false, false, false, false, random);
      task.setNote(95, "  Exposing some tar...");
      this.exposeTar((long)wsqd, random);
      task.setNote(96, "  Exposing some clay...");
      this.exposeClay((long)wsqd, random);
      logger.info("Finished!");
   }

   private void makeForest(int x, int y, Tiles.Tile theTile, byte data, Random random) {
      int sqrw = (int)Math.sqrt((double)this.width);
      int maxForestSize = theTile.isBush() ? this.width / sqrw / 2 : this.width / sqrw;

      for(int i = 0; i < sqrw * 10; ++i) {
         int count = 0;
         int scarcity = !theTile.isOak(data) && !theTile.isWillow(data) ? random.nextInt(3) * 2 + 1 : 0;

         for(int j = 0; j < maxForestSize; ++j) {
            int xx = x + random.nextInt(maxForestSize * 2 + 1) - maxForestSize;
            int yy = y + random.nextInt(maxForestSize * 2 + 1) - maxForestSize;
            if (this.isTreeCapable(xx, yy, scarcity)) {
               this.addTree(x, y, xx, yy, random);
               ++count;
            }
         }

         if (count == 0) {
            break;
         }
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

      age2 = random.nextInt(13) + 1;
      if (age2 > age) {
         age = age2;
      }

      return age;
   }

   private boolean isTreeCapable(int x, int y, int maxNeighbours) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.width) {
         if (this.textures[x & this.width - 1][y & this.width - 1] != GRASSID) {
            return false;
         } else if (this.getGroundHeight(x, y) > 0.65F) {
            return false;
         } else {
            int neighborTrees = 0;

            for(int xx = x - 1; xx <= x + 1; ++xx) {
               for(int yy = y - 1; yy <= y + 1; ++yy) {
                  int xxx = xx & this.width - 1;
                  int yyy = yy & this.width - 1;
                  Tiles.Tile theTile = Tiles.getTile(this.textures[xxx][yyy]);
                  if (theTile.isTree()) {
                     byte data = this.textureDatas[xxx][yyy];
                     if (theTile.isOak(data) || theTile.isWillow(data)) {
                        return false;
                     }

                     ++neighborTrees;
                  } else if (theTile.isBush()) {
                     ++neighborTrees;
                  }
               }
            }

            return neighborTrees <= maxNeighbours;
         }
      } else {
         return false;
      }
   }

   public BufferedImage getImage(MeshGenGui.Task task) {
      return this.getImage(this.imageLayer, task);
   }

   public BufferedImage getImage(int layer, MeshGenGui.Task task) {
      task.setMax(this.width + 50);
      task.setNote(0, "Generating image.");
      int lWidth = 16384;
      if (lWidth > this.width) {
         lWidth = this.width;
      }

      int yo = this.width - lWidth;
      if (yo < 0) {
         yo = 0;
      }

      int xo = this.width - lWidth;
      if (xo < 0) {
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

      for(int x = 0; x < lWidth; ++x) {
         task.setNote(x + 2);
         int alt = lWidth - 1;

         for(int y = lWidth - 1; y >= 0; --y) {
            float node = 0.0F;
            float node2 = 0.0F;
            if (layer == 0) {
               node = this.getHeight(x + xo, y + yo);
               node2 = this.getHeight(x + 1 + xo, y + 1 + yo);
            } else {
               node = this.getGroundHeight(x + xo, y + yo);
               node2 = this.getGroundHeight(x + 1 + xo, y + 1 + yo);
            }

            byte tex = this.textures[x + xo][y + yo];
            float h = (node2 - node) * 1500.0F / 256.0F * (float)(1 << this.level) / 128.0F + node / 2.0F + 1.0F;
            h *= 0.4F;
            float r = h;
            float g = h;
            float b = h;
            if (layer == 0) {
               Color color = Tiles.getTile(tex).getColor();
               r = h * (float)color.getRed() / 255.0F * 2.0F;
               g = h * (float)color.getGreen() / 255.0F * 2.0F;
               b = h * (float)color.getBlue() / 255.0F * 2.0F;
            }

            if (r < 0.0F) {
               r = 0.0F;
            }

            if (r > 1.0F) {
               r = 1.0F;
            }

            if (g < 0.0F) {
               g = 0.0F;
            }

            if (g > 1.0F) {
               g = 1.0F;
            }

            if (b < 0.0F) {
               b = 0.0F;
            }

            if (b > 1.0F) {
               b = 1.0F;
            }

            if (node < 0.0F) {
               r = r * 0.2F + 0.16000001F;
               g = g * 0.2F + 0.2F;
               b = b * 0.2F + 0.4F;
            }

            for(int altTarget = y - (int)(this.getHeight(x, y) * 1000.0F / 4.0F); alt > altTarget && alt >= 0; --alt) {
               data[(x + alt * lWidth) * 3 + 0] = r * 255.0F;
               data[(x + alt * lWidth) * 3 + 1] = g * 255.0F;
               data[(x + alt * lWidth) * 3 + 2] = b * 255.0F;
            }
         }
      }

      task.setNote(this.width + 10, "  Convert colours to image.");
      bi2.getRaster().setPixels(0, 0, lWidth, lWidth, data);
      return bi2;
   }

   protected int[] getData(MeshGenGui.Task task) {
      logger.info("Getting data for a " + this.width + 'x' + this.width + " map. Map height is " + 1000.0F);
      task.setMax(this.width);
      task.setNote(0, "getting Surface Data");
      int[] data = new int[this.width * this.width];
      int x = 0;
      int y = 0;

      try {
         for(y = 0; y < this.width; ++y) {
            task.setNote(y);

            for(x = 0; x < this.width; ++x) {
               float lHeight = this.getHeight(x, y) * 1000.0F;
               byte tex = this.textures[x][y];
               byte texdata = this.textureDatas[x][y];
               data[x + (y << this.level)] = Tiles.encode(lHeight, tex, texdata);
            }
         }

         return data;
      } catch (ArrayIndexOutOfBoundsException var8) {
         logger.log(
            Level.WARNING, "data: " + data.length + ", x: " + x + ", y: " + y + ", x + (y << (level + 1): " + (x + (y << this.level + 1)), (Throwable)var8
         );
         throw var8;
      }
   }

   protected int[] getRockData(MeshGenGui.Task task) {
      logger.info("Getting rock data for a " + this.width + 'x' + this.width + " map. Map height is " + 1000.0F);
      task.setMax(this.width);
      task.setNote(0, "getting Surface Data");
      int[] data = new int[this.width * this.width];

      for(int y = 0; y < this.width; ++y) {
         task.setNote(y);

         for(int x = 0; x < this.width; ++x) {
            float lHeight = this.getGroundHeight(x, y);
            byte tex = 0;
            byte texdata = 0;
            data[x + (y << this.level)] = Tiles.encode(lHeight * 1000.0F, (byte)0, (byte)0);
         }
      }

      return data;
   }

   protected int getLevel() {
      return this.level;
   }

   public void setData(MeshIO meshIO, MeshIO meshIO2, MeshGenGui.Task task) {
      task.setMax(this.width * 2);
      task.setNote(0, "setting Data");

      for(int x = 0; x < this.width; ++x) {
         task.setNote(x);

         for(int y = 0; y < this.width; ++y) {
            int tile = meshIO.getTile(x, y);
            this.setHeight(x, y, Tiles.decodeHeightAsFloat(tile) / 1000.0F);
            this.textures[x][y] = Tiles.decodeType(tile);
            this.textureDatas[x][y] = Tiles.decodeData(tile);
         }
      }

      for(int x = 0; x < this.width; ++x) {
         task.setNote(this.width + x);

         for(int y = 0; y < this.width; ++y) {
            int tile = meshIO2.getTile(x, y);
            float orgHeight = Tiles.decodeHeightAsFloat(tile) / 1000.0F;
            this.groundHeight[x][y] = orgHeight;
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
