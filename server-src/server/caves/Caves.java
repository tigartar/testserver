package com.wurmonline.server.caves;

final class Caves {
   private static final float MAX_CAVE_SLOPE = 8.0F;

   public Caves() {
   }

   public void digHoleAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
      if (!this.isRockExposed(xTarget, yTarget)) {
         System.out.println("You can't mine an entrance here.. There's too much dirt on the tile.");
      } else if (!this.isCaveWall(xFrom, yFrom)) {
         System.out.println("You can't mine an entrance here.. There's a tunnel in the way.");
      } else {
         for(int x = xTarget - 1; x <= xTarget + 1; ++x) {
            for(int y = yTarget - 1; y <= yTarget + 1; ++y) {
               if (this.isTerrainHole(x, y)) {
                  System.out.println("You can't mine an entrance here.. Too close to an existing entrance.");
                  return;
               }
            }
         }

         if (this.isMinable(xTarget, yTarget)) {
            for(int x = xFrom; x <= xFrom + 1; ++x) {
               int y = yFrom;

               while(y <= yFrom + 1) {
                  ++y;
               }
            }

            this.mine(xFrom, yFrom, xTarget, yTarget, slope);
         }
      }
   }

   public void mineAt(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
      if (!this.isMinable(xTarget, yTarget)) {
         System.out.println("You can't mine here.. There's a tunnel in the way.");
      } else {
         if (!this.isCaveWall(xTarget, xTarget)) {
            this.mine(xFrom, yFrom, xTarget, yTarget, slope);
         }
      }
   }

   private void mine(int xFrom, int yFrom, int xTarget, int yTarget, int slope) {
   }

   private boolean isMinable(int xTarget, int yTarget) {
      float lowestFloor = 100000.0F;
      float highestFloor = -100000.0F;
      int tunnels = 0;

      for(int x = xTarget; x <= xTarget + 1; ++x) {
         for(int y = yTarget; y <= yTarget + 1; ++y) {
            if (this.isExitCorner(x, y)) {
               ++tunnels;
               float h = this.getTerrainHeight(x, y);
               if (h < lowestFloor) {
                  lowestFloor = h;
               }

               if (h > highestFloor) {
                  highestFloor = h;
               }
            } else if (this.isTunnelCorner(x, y)) {
               ++tunnels;
               float h = this.getCaveFloorHeight(x, y);
               if (h < lowestFloor) {
                  lowestFloor = h;
               }

               if (h > highestFloor) {
                  highestFloor = h;
               }
            }
         }
      }

      if (tunnels == 0) {
         return true;
      } else {
         float diff = highestFloor - lowestFloor;
         return diff < 8.0F;
      }
   }

   private boolean isTunnelCorner(int x, int y) {
      if (this.isCaveTunnel(x, y)) {
         return true;
      } else if (this.isCaveTunnel(x - 1, y)) {
         return true;
      } else if (this.isCaveTunnel(x - 1, y - 1)) {
         return true;
      } else {
         return this.isCaveTunnel(x, y - 1);
      }
   }

   private boolean isExitCorner(int x, int y) {
      if (this.isCaveExit(x, y)) {
         return true;
      } else if (this.isCaveExit(x - 1, y)) {
         return true;
      } else if (this.isCaveExit(x - 1, y - 1)) {
         return true;
      } else {
         return this.isCaveExit(x, y - 1);
      }
   }

   private float getTerrainHeight(int x, int y) {
      return 10.0F;
   }

   private float getCaveFloorHeight(int x, int y) {
      return 10.0F;
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
