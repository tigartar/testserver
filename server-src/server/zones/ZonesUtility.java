package com.wurmonline.server.zones;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.MethodsFishing;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public final class ZonesUtility {
   private static final Logger logger = Logger.getLogger(ZonesUtility.class.getName());
   static final Color waystoneSurface = Color.CYAN;
   static final Color waystoneCave = new Color(0, 153, 153);
   static final Color catseye0Surface = new Color(255, 255, 255);
   static final Color catseye0Cave = new Color(224, 224, 224);
   static final Color catseye1Surface = new Color(255, 0, 0);
   static final Color catseye1Cave = new Color(204, 0, 0);
   static final Color catseye2Surface = new Color(0, 0, 255);
   static final Color catseye2Cave = new Color(0, 0, 153);
   static final Color catseye3Surface = new Color(0, 255, 0);
   static final Color catseye3Cave = new Color(0, 153, 0);

   private ZonesUtility() {
   }

   public static void saveFishSpots(MeshIO mesh) {
      logger.log(Level.INFO, "[FISH_SPOTS]: Started");
      int size = 256;
      Color[][] colours = new Color[256][256];

      for(int season = 0; season < 4; ++season) {
         Point offset = MethodsFishing.getSeasonOffset(season);
         Color bgColour = MethodsFishing.getBgColour(season);

         for(int x = 0; x < 128; ++x) {
            for(int y = 0; y < 128; ++y) {
               colours[offset.getX() + x][offset.getY() + y] = bgColour;
            }
         }

         Point[] spots = MethodsFishing.getSpecialSpots(0, 0, season);

         for(Point spot : spots) {
            Color fishColour = MethodsFishing.getFishColour(spot.getH());

            for(int x = spot.getX() - 5; x <= spot.getX() + 5; ++x) {
               for(int y = spot.getY() - 5; y <= spot.getY() + 5; ++y) {
                  colours[offset.getX() + x][offset.getY() + y] = fishColour;
               }
            }
         }
      }

      BufferedImage bmi = makeBufferedImage(colours, 256);
      String filename = "fishSpots.png";
      File f = new File("fishSpots.png");

      try {
         ImageIO.write(bmi, "png", f);
         logger.log(Level.INFO, "[FISH_SPOTS]: Finished");
      } catch (IOException var14) {
         logger.log(Level.WARNING, "[FISH_SPOTS]: Failed to produce fishSpots.png", (Throwable)var14);
      }
   }

   public static void saveMapDump(MeshIO mesh) {
      int size = mesh.getSize();
      Color[][] colors = new Color[size][size];
      int maxH = 0;
      int minH = 0;
      float divider = 10.0F;

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            int tileId = mesh.getTile(x, y);
            int height = Tiles.decodeHeight(tileId);
            if (height > maxH) {
               maxH = height;
            }

            if (height < minH) {
               minH = height;
            }
         }
      }

      maxH = (int)((float)maxH / 10.0F);
      minH = (int)((float)minH / 10.0F);

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            int tileId = mesh.getTile(x, y);
            byte type = Tiles.decodeType(tileId);
            Tiles.Tile tile = Tiles.getTile(type);
            int height = Tiles.decodeHeight(tileId);
            if (height > 0) {
               if (type == 4) {
                  float tenth = (float)height / 10.0F;
                  float percent = tenth / (float)maxH;
                  int step = (int)(150.0F * percent);
                  colors[x][y] = new Color(Math.min(200, 10 + step), Math.min(200, 10 + step), Math.min(200, 10 + step));
               } else if (type == 12) {
                  colors[x][y] = Color.MAGENTA;
               } else {
                  float tenth = (float)height / 10.0F;
                  float percent = tenth / (float)maxH;
                  int step = (int)(190.0F * percent);
                  Color c = tile.getColor();
                  colors[x][y] = new Color(c.getRed(), Math.min(255, c.getGreen() + step), c.getBlue());
               }
            } else {
               float tenth = (float)height / 10.0F;
               float percent = tenth / (float)minH;
               int step = (int)(255.0F * percent);
               colors[x][y] = new Color(0, 0, Math.max(20, 255 - Math.abs(step)));
            }
         }
      }

      BufferedImage bmi = new BufferedImage(size * 4, size * 4, 2);
      Graphics g = bmi.createGraphics();

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            g.setColor(colors[x][y]);
            g.fillRect(x * 4, y * 4, 4, 4);
         }
      }

      File f = new File("mapdump.png");

      try {
         ImageIO.write(bmi, "png", f);
      } catch (IOException var16) {
         logger.log(Level.WARNING, "Failed to produce mapdump.png", (Throwable)var16);
      }
   }

   public static void saveCreatureDistributionAsImg(MeshIO mesh) {
      logger.log(Level.INFO, "[CREATURE_DUMP]: Started");
      int size = mesh.getSize();
      Color[][] colors = getHeightMap(size);
      Creature[] crets = Creatures.getInstance().getCreatures();

      for(Creature c : crets) {
         if (c.isGuard()) {
            colors[c.getTileX()][c.getTileY()] = Color.decode("#006000");
         } else if (c.isBred()) {
            colors[c.getTileX()][c.getTileY()] = Color.CYAN;
         } else if (c.isDomestic()) {
            colors[c.getTileX()][c.getTileY()] = Color.YELLOW;
         } else {
            colors[c.getTileX()][c.getTileY()] = Color.RED;
         }
      }

      BufferedImage bmi = makeBufferedImage(colors, size);
      File f = new File("creatures.png");

      try {
         ImageIO.write(bmi, "png", f);
         logger.log(Level.INFO, "[CREATURE_DUMP]: Finished");
      } catch (IOException var8) {
         logger.log(Level.WARNING, "[CREATURE_DUMP]: Failed to produce creatures.png", (Throwable)var8);
      }
   }

   public static void saveMapWithMarkersAsImg() {
      logger.log(Level.INFO, "[MAP WITH MARKERS DUMP]: Started");
      int size = Server.surfaceMesh.getSize();
      Color[][] colours = getHeightMap(size);
      AddOptedInVillages(size, colours);
      AddMarkers(Items.getMarkers(), colours);
      BufferedImage bmi = makeBufferedImage(colours, size);
      File f = new File("markersdump.png");

      try {
         ImageIO.write(bmi, "png", f);
         logger.log(Level.INFO, "[MAP WITH MARKERS DUMP]: Finished");
      } catch (IOException var5) {
         logger.log(Level.WARNING, "[MAP WITH MARKERS DUMP]: Failed to produce markers.png", (Throwable)var5);
      }
   }

   public static void saveRoutesAsImg() {
      logger.log(Level.INFO, "[ROUTES_DUMP]: Started");
      int size = Server.surfaceMesh.getSize();
      Color[][] colours = getHeightMap(size);
      AddMarkers(Routes.getMarkers(), colours);
      BufferedImage bmi = makeBufferedImage(colours, size);
      File f = new File("routes.png");

      try {
         ImageIO.write(bmi, "png", f);
         logger.log(Level.INFO, "[ROUTES_DUMP]: Finished");
      } catch (IOException var5) {
         logger.log(Level.WARNING, "[ROUTES_DUMP]: Failed to produce routes.png", (Throwable)var5);
      }
   }

   public static void saveWaterTypesAsImg(boolean onSurface) {
      logger.log(Level.INFO, "[WATER_TYPES_DUMP]: Started");
      int size = Server.surfaceMesh.getSize();
      Color[][] colours = getHeightMap(size);

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            switch(WaterType.getWaterType(x, y, onSurface)) {
               case 1:
                  colours[x][y] = new Color(174, 168, 253);
                  break;
               case 2:
                  colours[x][y] = new Color(137, 128, 252);
                  break;
               case 3:
                  colours[x][y] = new Color(115, 150, 165);
                  break;
               case 4:
                  colours[x][y] = new Color(163, 200, 176);
                  break;
               case 5:
                  colours[x][y] = new Color(235, 200, 176);
                  break;
               case 6:
                  colours[x][y] = new Color(185, 150, 165);
            }
         }
      }

      BufferedImage bmi = makeBufferedImage(colours, size);
      String filename = onSurface ? "waterSurfaceTypes.png" : "waterCaveTypes.png";
      File f = new File(filename);

      try {
         ImageIO.write(bmi, "png", f);
         logger.log(Level.INFO, "[WATER_TYPES_DUMP]: Finished");
      } catch (IOException var7) {
         logger.log(Level.WARNING, "[WATER_TYPES_DUMP]: Failed to produce (" + onSurface + ") waterTypes.png", (Throwable)var7);
      }
   }

   private static Color[][] getHeightMap(int size) {
      Color[][] colours = new Color[size][size];
      int maxH = 0;
      int minH = 0;
      float divider = 10.0F;

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            int tileId = Server.surfaceMesh.getTile(x, y);
            int height = Tiles.decodeHeight(tileId);
            if (height > maxH) {
               maxH = height;
            }

            if (height < minH) {
               minH = height;
            }
         }
      }

      maxH = (int)((float)maxH / 10.0F);
      minH = (int)((float)minH / 10.0F);

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            int tileId = Server.surfaceMesh.getTile(x, y);
            byte type = Tiles.decodeType(tileId);
            Tiles.Tile tile = Tiles.getTile(type);
            int height = Tiles.decodeHeight(tileId);
            if (height > 0) {
               if (type == 4) {
                  float tenth = (float)height / 10.0F;
                  float percent = tenth / (float)maxH;
                  int step = (int)(150.0F * percent);
                  colours[x][y] = new Color(Math.min(200, 10 + step), Math.min(200, 10 + step), Math.min(200, 10 + step));
               } else if (type == 12) {
                  colours[x][y] = Color.MAGENTA;
               } else {
                  float tenth = (float)height / 10.0F;
                  float percent = tenth / (float)maxH;
                  int step = (int)(190.0F * percent);
                  Color c = tile.getColor();
                  colours[x][y] = new Color(c.getRed(), Math.min(255, c.getGreen() + step), c.getBlue());
               }
            } else {
               float tenth = (float)height / 10.0F;
               float percent = tenth / (float)minH;
               int step = (int)(255.0F * percent);
               colours[x][y] = new Color(0, 0, Math.max(20, 255 - Math.abs(step)));
            }
         }
      }

      return colours;
   }

   private static void AddOptedInVillages(int size, Color[][] colours) {
      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            VolaTile tt = Zones.getOrCreateTile(x, y, true);
            Village vill = tt == null ? null : tt.getVillage();
            if (vill != null && vill.isHighwayFound() && (x == vill.getStartX() || x == vill.getEndX() || y == vill.getStartY() || y == vill.getEndY())) {
               colours[x][y] = new Color(255, 127, 39);
            }
         }
      }
   }

   private static void AddMarkers(Item[] markers, Color[][] colours) {
      for(Item marker : markers) {
         int id = marker.getTemplateId();
         switch(id) {
            case 1112:
               if (marker.isOnSurface()) {
                  addMarker(colours, marker, waystoneSurface);
               } else {
                  Color colour = colours[marker.getTileX()][marker.getTileY()];
                  if (colour != waystoneSurface) {
                     addMarker(colours, marker, waystoneCave);
                  }
               }
               break;
            case 1114:
               Color colour = colours[marker.getTileX()][marker.getTileY()];
               if (colour != waystoneSurface && colour == waystoneCave) {
               }

               if (marker.isOnSurface()) {
                  switch(MethodsHighways.numberOfSetBits(marker.getAuxData())) {
                     case 0:
                        addMarker(colours, marker, catseye0Surface);
                        break;
                     case 1:
                        addMarker(colours, marker, catseye1Surface);
                        break;
                     default:
                        if (Routes.isCatseyeUsed(marker)) {
                           addMarker(colours, marker, catseye3Surface);
                        } else {
                           addMarker(colours, marker, catseye2Surface);
                        }
                  }
               } else if (colour != catseye0Surface && colour != catseye2Surface && colour != catseye2Surface && colour != catseye3Surface) {
                  switch(MethodsHighways.numberOfSetBits(marker.getAuxData())) {
                     case 0:
                        addMarker(colours, marker, catseye0Cave);
                        break;
                     case 1:
                        addMarker(colours, marker, catseye1Cave);
                        break;
                     default:
                        if (Routes.isCatseyeUsed(marker)) {
                           addMarker(colours, marker, catseye3Cave);
                        } else {
                           addMarker(colours, marker, catseye2Cave);
                        }
                  }
               }
         }
      }
   }

   private static void addMarker(Color[][] colours, Item marker, Color colour) {
      int x = marker.getTileX();
      int y = marker.getTileY();
      colours[x][y] = colour;
      addColour(colours, x - 1, y, colour);
      addColour(colours, x, y - 1, colour);
      addColour(colours, x - 1, y - 1, colour);
      MeshIO mesh = marker.isOnSurface() ? Server.surfaceMesh : Server.caveMesh;

      for(int xx = 0; xx <= 1; ++xx) {
         for(int yy = 0; yy <= 1; ++yy) {
            if (xx != 0 && yy != 0) {
               int tileId = mesh.getTile(x + xx, y + yy);
               byte type = Tiles.decodeType(tileId);
               if (Tiles.isRoadType(type) || type == 201) {
                  addColour(colours, x + xx, y + yy, colour);
               }
            }
         }
      }
   }

   private static void addColour(Color[][] colours, int x, int y, Color colour) {
      if (colours[x][y] != waystoneSurface && colours[x][y] != waystoneCave) {
         colours[x][y] = colour;
      }
   }

   private static BufferedImage makeBufferedImage(Color[][] colors, int size) {
      BufferedImage bmi = new BufferedImage(size, size, 2);
      Graphics g = bmi.createGraphics();

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            g.setColor(colors[x][y]);
            g.fillRect(x, y, 1, 1);
         }
      }

      return bmi;
   }

   public static void saveAsImg(MeshIO mesh) {
      long lStart = System.nanoTime();
      int size = mesh.getSize();
      logger.info("Size:" + size);
      logger.info("Data Size: " + mesh.data.length);
      byte[][] tiles = new byte[size][size];

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            int tileId = mesh.getTile(x, y);
            byte type = Tiles.decodeType(tileId);
            tiles[x][y] = type;
         }
      }

      BufferedImage bmi = new BufferedImage(size, size, 2);
      Graphics g = bmi.createGraphics();

      for(int x = 0; x < size; ++x) {
         for(int y = 0; y < size; ++y) {
            boolean paint = true;
            if (tiles[x][y] == -34) {
               g.setColor(Color.darkGray);
            } else if (tiles[x][y] == -50) {
               g.setColor(Color.white);
            } else if (tiles[x][y] == -52) {
               g.setColor(Color.ORANGE);
            } else if (tiles[x][y] == -36) {
               g.setColor(Color.YELLOW);
            } else if (tiles[x][y] == -35) {
               g.setColor(Color.GREEN);
            } else if (tiles[x][y] == -32) {
               g.setColor(Color.CYAN);
            } else if (tiles[x][y] == -51) {
               g.setColor(Color.MAGENTA);
            } else if (tiles[x][y] == -33) {
               g.setColor(Color.PINK);
            } else if (tiles[x][y] == -30) {
               g.setColor(Color.BLUE);
            } else if (tiles[x][y] == -31) {
               g.setColor(Color.RED);
            } else {
               paint = false;
            }

            if (paint) {
               g.fillRect(x, y, 1, 1);
            }
         }
      }

      File f = new File("Ore.png");

      try {
         ImageIO.write(bmi, "png", f);
      } catch (IOException var15) {
         logger.log(Level.WARNING, "Failed to produce ore.png", (Throwable)var15);
      } finally {
         long lElapsedTime = System.nanoTime() - lStart;
         logger.info("Saved Mesh to '" + f.getAbsoluteFile() + "', that took " + (float)lElapsedTime / 1000000.0F + ", millis.");
      }
   }
}
