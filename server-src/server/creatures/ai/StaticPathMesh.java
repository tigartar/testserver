package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import java.util.logging.Level;
import java.util.logging.Logger;

final class StaticPathMesh {
   private static final Logger logger = Logger.getLogger(PathMesh.class.getName());
   private final PathTile start;
   private final PathTile finish;
   private static PathTile[][] pathables;
   private static final int WORLD_SIZE = 1 << Constants.meshSize;
   private final int sizex;
   private final int sizey;
   private final int borderStartX;
   private final int borderEndX;
   private final int borderStartY;
   private final int borderEndY;
   private final boolean surfaced;

   StaticPathMesh(int startx, int starty, int endx, int endy, boolean surf, int borderSize, int[] aMesh) {
      this.surfaced = surf;
      int[] lMesh = aMesh;
      this.borderStartX = Math.max(0, Math.min(startx, endx) - borderSize);
      this.borderEndX = Math.min(WORLD_SIZE - 1, Math.max(startx, endx) + borderSize);
      this.borderStartY = Math.max(0, Math.min(starty, endy) - borderSize);
      this.borderEndY = Math.min(WORLD_SIZE - 1, Math.max(starty, endy) + borderSize);
      this.sizex = this.borderEndX - this.borderStartX + 1;
      this.sizey = this.borderEndY - this.borderStartY + 1;
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("PathMesh start-end: " + startx + "," + starty + "-" + endx + "," + endy);
         logger.finest(
            "PathMesh border start-end: "
               + this.borderStartX
               + ", "
               + this.borderStartY
               + " - "
               + this.borderEndX
               + ", "
               + this.borderEndY
               + ", sz="
               + this.sizex
               + ","
               + this.sizey
         );
      }

      pathables = new PathTile[this.sizex][this.sizey];
      PathTile lStart = null;
      PathTile lFinish = null;

      for(int y = 0; y < this.sizey; ++y) {
         for(int x = 0; x < this.sizex; ++x) {
            pathables[x][y] = new PathTile(
               this.borderStartX + x,
               this.borderStartY + y,
               lMesh[this.borderStartX + x | this.borderStartY + y << Constants.meshSize],
               this.surfaced,
               (byte)(this.surfaced ? 0 : -1)
            );
            if (pathables[x][y].getTileX() == startx && pathables[x][y].getTileY() == starty) {
               lStart = pathables[x][y];
            }

            if (pathables[x][y].getTileX() == endx && pathables[x][y].getTileY() == endy) {
               lFinish = pathables[x][y];
            }
         }
      }

      this.start = lStart;
      this.finish = lFinish;
      if (this.start == this.finish) {
         logger.log(Level.INFO, "START=FINISH!");
      }
   }

   StaticPathMesh(int startx, int starty, int endx, int endy, boolean surf, int borderSize) {
      this(startx, starty, endx, endy, surf, borderSize, surf ? Server.surfaceMesh.data : Server.caveMesh.data);
   }

   PathTile getPathTile(int realx, int realy) {
      int diffX = realx - this.borderStartX;
      int diffY = realy - this.borderStartY;
      return pathables[diffX][diffY];
   }

   boolean contains(int realx, int realy) {
      return realx >= this.borderStartX && realx <= this.borderEndX && realy >= this.borderStartY && realy <= this.borderEndY;
   }

   PathTile[] getAdjacent(PathTile p) {
      PathTile[] surrPathables = new PathTile[4];
      int x = p.getTileX();
      int y = p.getTileY();
      int minOneX = x - 1;
      int plusOneX = x + 1;
      int minOneY = y - 1;
      int plusOneY = y + 1;
      if (minOneX >= this.borderStartX) {
         surrPathables[3] = this.getPathTile(minOneX, y);
      }

      if (plusOneX < this.borderEndX) {
         surrPathables[1] = this.getPathTile(plusOneX, y);
      }

      if (minOneY >= this.borderStartY) {
         surrPathables[0] = this.getPathTile(x, minOneY);
      }

      if (plusOneY < this.borderEndY) {
         surrPathables[2] = this.getPathTile(x, plusOneY);
      }

      return surrPathables;
   }

   PathTile getStart() {
      return this.start;
   }

   PathTile getFinish() {
      return this.finish;
   }

   static void clearPathables() {
      pathables = (PathTile[][])null;
   }

   int getSizex() {
      return this.sizex;
   }

   int getSizey() {
      return this.sizey;
   }

   int getBorderStartX() {
      return this.borderStartX;
   }

   int getBorderEndX() {
      return this.borderEndX;
   }

   int getBorderStartY() {
      return this.borderStartY;
   }

   int getBorderEndY() {
      return this.borderEndY;
   }

   boolean isSurfaced() {
      return this.surfaced;
   }
}
