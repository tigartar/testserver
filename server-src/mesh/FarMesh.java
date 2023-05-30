package com.wurmonline.mesh;

public class FarMesh extends Mesh {
   public FarMesh(int width, int height, int meshWidth) {
      super(width, height, meshWidth);
      this.generateEmpty(true);
   }

   public void setFarTiles(int xStart, int yStart, int w, int h, short[][] tiles) {
      for(int x = 0; x < w; ++x) {
         for(int y = 0; y < h; ++y) {
            Node node = this.getNode(x + xStart, y + yStart);
            node.setHeight(Tiles.shortHeightToFloat(tiles[x][y]));
         }
      }

      this.calculateNormals(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
      this.processData(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
   }
}
