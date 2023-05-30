package com.wurmonline.mesh;

public final class CaveMesh extends Mesh {
   public CaveMesh(Mesh mesh, int width, int height) {
      super(width, height, mesh.getMeshWidth());

      for(int x = 0; x < width; ++x) {
         for(int y = 0; y < height; ++y) {
            this.nodes[x][y] = new CaveNode();
            this.nodes[x][y].setTexture(CaveTile.TILE_ROCK.id);
            ((CaveNode)this.nodes[x][y]).setCeilingTexture(CaveTile.TILE_ROCK.id);
            this.nodes[x][y].setHeight((float)(Math.random() * Math.random() * Math.random()) * 100.0F + 0.2F);
            this.nodes[x][y].setNormals(new float[3]);
         }
      }

      this.processData();
      this.calculateNormals();
   }

   public CaveNode getCaveNode(int x, int y) {
      return (CaveNode)this.getNode(x, y);
   }

   public CaveMesh(int width, int height, int meshWidth) {
      super(width, height, meshWidth);
      this.setWraparound();

      for(int x = 0; x < width; ++x) {
         for(int y = 0; y < width; ++y) {
            this.nodes[x][y] = new CaveNode();
            this.nodes[x][y].setTexture(CaveTile.TILE_ROCK.id);
            this.getCaveNode(x, y).setCeilingTexture(CaveTile.TILE_ROCK.id);
            this.getCaveNode(x, y).setHeight(Float.MAX_VALUE);
            this.getCaveNode(x, y).setData(0.0F);
            this.getCaveNode(x, y).setSpecial(1);
         }
      }

      this.processData();
      this.calculateNormals();
   }

   @Override
   public boolean isTransition(float xp, float yp) {
      while(xp < 0.0F) {
         xp += (float)(this.getWidth() * this.getMeshWidth());
      }

      while(yp < 0.0F) {
         yp += (float)(this.getHeight() * this.getMeshWidth());
      }

      int xx = (int)(xp / (float)this.getMeshWidth());
      int yy = (int)(yp / (float)this.getMeshWidth());
      return this.getCaveNode(xx, yy).getSpecial() != 0;
   }

   @Override
   public void processData(int x1, int y1, int x2, int y2) {
      for(int x = x1; x < x2; ++x) {
         for(int y = y1; y < y2; ++y) {
            Node node = this.getNode(x, y);
            float b = node.getHeight();
            float t = node.getHeight() + this.getCaveNode(x, y).getData();
            if (this.getNode(x + 1, y).getHeight() < b) {
               b = this.getNode(x + 1, y).getHeight();
            }

            if (this.getNode(x + 1, y).getHeight() + this.getCaveNode(x + 1, y).getData() > t) {
               t = this.getNode(x + 1, y).getHeight() + this.getCaveNode(x + 1, y).getData();
            }

            if (this.getNode(x + 1, y + 1).getHeight() < b) {
               b = this.getNode(x + 1, y + 1).getHeight();
            }

            if (this.getNode(x + 1, y + 1).getHeight() + this.getCaveNode(x + 1, y + 1).getData() > t) {
               t = this.getNode(x + 1, y + 1).getHeight() + this.getCaveNode(x + 1, y + 1).getData();
            }

            if (this.getNode(x, y + 1).getHeight() < b) {
               b = this.getNode(x, y + 1).getHeight();
            }

            if (this.getNode(x, y + 1).getHeight() + this.getCaveNode(x, y + 1).getData() > t) {
               t = this.getNode(x, y + 1).getHeight() + this.getCaveNode(x, y + 1).getData();
            }

            float h = t - b;
            node.setBbBottom(b);
            node.setBbHeight(h);
         }
      }
   }

   @Override
   public void calculateNormals(int x1, int y1, int x2, int y2) {
      for(int x = x1; x < x2; ++x) {
         for(int y = y1; y < y2; ++y) {
            CaveNode n1 = this.getCaveNode(x, y);
            float v1x = (float)this.getMeshWidth();
            float v1y = this.getNode(x + 1, y).getHeight() - n1.getHeight();
            float v1z = 0.0F;
            float v2x = 0.0F;
            float v2y = this.getNode(x, y + 1).getHeight() - n1.getHeight();
            float v2z = (float)this.getMeshWidth();
            float vx = v1y * v2z - v1z * v2y;
            float vy = v1z * v2x - v1x * v2z;
            float vz = v1x * v2y - v1y * v2x;
            v1x = (float)(-this.getMeshWidth());
            v1y = this.getNode(x - 1, y).getHeight() - n1.getHeight();
            v1z = 0.0F;
            v2x = 0.0F;
            v2y = this.getNode(x, y - 1).getHeight() - n1.getHeight();
            v2z = (float)(-this.getMeshWidth());
            vx += v1y * v2z - v1z * v2y;
            vy += v1z * v2x - v1x * v2z;
            vz += v1x * v2y - v1y * v2x;
            float dist = (float)Math.sqrt((double)(vx * vx + vy * vy + vz * vz));
            vx /= dist;
            vy /= dist;
            vz /= dist;
            n1.normals[0] = -vx;
            n1.normals[1] = -vy;
            n1.normals[2] = -vz;
         }
      }
   }

   @Override
   public boolean setTiles(int xStart, int yStart, int w, int h, int[][] tiles) {
      for(int x = 0; x < w; ++x) {
         for(int y = 0; y < h; ++y) {
            CaveNode node = this.getCaveNode(x + xStart, y + yStart);
            node.setHeight(CaveTile.decodeHeightAsFloat(tiles[x][y]));
            node.setTexture(CaveTile.decodeFloorTexture(tiles[x][y]));
            node.setCeilingTexture(CaveTile.decodeCeilingTexture(tiles[x][y]));
            node.setData(CaveTile.decodeCeilingHeightAsFloat(tiles[x][y]));
            node.setSpecial(0);
            if (node.getData() < 0.0F) {
               node.setData(-node.getData());
               node.setSpecial(1);
            }
         }
      }

      return false;
   }
}
