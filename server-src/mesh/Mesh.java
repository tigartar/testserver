/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

import com.wurmonline.mesh.Node;
import com.wurmonline.mesh.Tiles;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class Mesh {
    public static final float MAX_NOISE = 200.0f;
    private final int width;
    private final int height;
    Node[][] nodes;
    private final int meshWidth;
    private final float textureScale;
    private final int heightMinusOne;
    private final int widthMinusOne;
    private boolean wrap = false;

    public Mesh(int width, int height, int meshWidth) {
        this.width = width;
        this.height = height;
        this.widthMinusOne = width - 1;
        this.heightMinusOne = height - 1;
        this.meshWidth = meshWidth;
        this.textureScale = 1.0f;
        this.nodes = new Node[width][height];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < width; ++y) {
                this.nodes[x][y] = new Node();
            }
        }
    }

    public float getTextureScale() {
        return this.textureScale;
    }

    public final int getMeshWidth() {
        return this.meshWidth;
    }

    public void generateMesh(InputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(in);
        int[][] tileDatas = (int[][])ois.readObject();
        ois.close();
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                this.nodes[x][y] = new Node();
                this.nodes[x][y].setHeight(Tiles.decodeHeightAsFloat(tileDatas[x][y]));
                this.nodes[x][y].setTexture(Tiles.decodeType(tileDatas[x][y]));
            }
        }
        this.processData();
    }

    public void generateEmpty(boolean createObjects) {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.width; ++y) {
                if (createObjects) {
                    this.nodes[x][y] = new Node();
                }
                this.nodes[x][y].setHeight(-100.0f);
                this.nodes[x][y].setTexture(Tiles.Tile.TILE_SAND.getId());
                if (createObjects) {
                    this.nodes[x][y].setNormals(new float[3]);
                }
                this.nodes[x][y].normals[0] = 0.0f;
                this.nodes[x][y].normals[1] = 1.0f;
                this.nodes[x][y].normals[2] = 0.0f;
            }
        }
        this.processData();
    }

    public void processData() {
        this.processData(0, 0, this.width, this.height);
    }

    public void processData(int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2; ++x) {
            for (int y = y1; y < y2; ++y) {
                Node node = this.getNode(x, y);
                float b = node.getHeight();
                float t = node.getHeight();
                if (this.getNode(x + 1, y).getHeight() < b) {
                    b = this.getNode(x + 1, y).getHeight();
                }
                if (this.getNode(x + 1, y).getHeight() > t) {
                    t = this.getNode(x + 1, y).getHeight();
                }
                if (this.getNode(x + 1, y + 1).getHeight() < b) {
                    b = this.getNode(x + 1, y + 1).getHeight();
                }
                if (this.getNode(x + 1, y + 1).getHeight() > t) {
                    t = this.getNode(x + 1, y + 1).getHeight();
                }
                if (this.getNode(x, y + 1).getHeight() < b) {
                    b = this.getNode(x, y + 1).getHeight();
                }
                if (this.getNode(x, y + 1).getHeight() > t) {
                    t = this.getNode(x, y + 1).getHeight();
                }
                float h = t - b;
                node.setBbBottom(b);
                node.setBbHeight(h);
            }
        }
    }

    public void calculateNormals() {
        this.calculateNormals(0, 0, this.width, this.height);
    }

    public void calculateNormals(int x1, int y1, int x2, int y2) {
        for (int x = x1; x < x2; ++x) {
            for (int y = y1; y < y2; ++y) {
                Node n1 = this.getNode(x, y);
                float v1x = this.getMeshWidth();
                float v1y = this.getNode(x + 1, y).getHeight() - n1.getHeight();
                float v1z = 0.0f;
                float v2x = 0.0f;
                float v2y = this.getNode(x, y + 1).getHeight() - n1.getHeight();
                float v2z = this.getMeshWidth();
                float vx = v1y * v2z - v1z * v2y;
                float vy = v1z * v2x - v1x * v2z;
                float vz = v1x * v2y - v1y * v2x;
                v1x = -this.getMeshWidth();
                v1y = this.getNode(x - 1, y).getHeight() - n1.getHeight();
                v1z = 0.0f;
                v2x = 0.0f;
                v2y = this.getNode(x, y - 1).getHeight() - n1.getHeight();
                v2z = -this.getMeshWidth();
                float dist = (float)Math.sqrt((vx += v1y * v2z - v1z * v2y) * vx + (vy += v1z * v2x - v1x * v2z) * vy + (vz += v1x * v2y - v1y * v2x) * vz);
                n1.normals[0] = -(vx /= dist);
                n1.normals[1] = -(vy /= dist);
                n1.normals[2] = -(vz /= dist);
            }
        }
    }

    public FloatBuffer createFloatBuffer(int size) {
        ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
        temp.order(ByteOrder.nativeOrder());
        return temp.asFloatBuffer();
    }

    public void setWraparound() {
        this.wrap = true;
    }

    public Node getNode(int x, int y) {
        if (!(this.wrap || x >= 0 && y >= 0 && x < this.width && y < this.height)) {
            return this.nodes[0][0];
        }
        return this.nodes[x & this.widthMinusOne][y & this.heightMinusOne];
    }

    public void generateHills() {
        int y;
        int x;
        float[][] heights = new float[this.width][this.height];
        for (x = 0; x < this.width; ++x) {
            for (y = 0; y < this.height; ++y) {
                boolean lower;
                Node node = this.nodes[x][y];
                heights[x][y] = node.getHeight();
                boolean bl = lower = heights[x][y] < -64.0f;
                if (heights[x][y] < 0.0f) {
                    heights[x][y] = heights[x][y] * 0.1f;
                }
                if (!lower) continue;
                heights[x][y] = heights[x][y] * 2.0f - 3.0f;
            }
        }
        for (x = 0; x < this.width; ++x) {
            for (y = 0; y < this.height; ++y) {
                this.nodes[x][y].setHeight(heights[x][y]);
            }
        }
    }

    public void smooth(float bias) {
        int y;
        int x;
        float[][] heights = new float[this.width][this.height];
        for (x = 0; x < this.width; ++x) {
            for (y = 0; y < this.height; ++y) {
                Node node = this.nodes[x][y];
                float surroundingHeight = 0.0f;
                surroundingHeight += this.getNode(x - 1, y).getHeight();
                surroundingHeight += this.getNode(x + 1, y).getHeight();
                surroundingHeight += this.getNode(x, y - 1).getHeight();
                heights[x][y] = node.getHeight() * bias + (surroundingHeight += this.getNode(x, y + 1).getHeight()) / 4.0f * (1.0f - bias);
            }
        }
        for (x = 0; x < this.width; ++x) {
            for (y = 0; y < this.height; ++y) {
                this.nodes[x][y].setHeight(heights[x][y]);
            }
        }
    }

    public void noise(Random random, float noiseLevel) {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                this.nodes[x][y].setHeight(this.nodes[x][y].getHeight() + (random.nextFloat() - 0.5f) * noiseLevel);
            }
        }
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean isTransition(float xp, float yp) {
        int yy;
        while (xp < 0.0f) {
            xp += (float)(this.getWidth() * this.getMeshWidth());
        }
        while (yp < 0.0f) {
            yp += (float)(this.getHeight() * this.getMeshWidth());
        }
        int xx = (int)(xp / (float)this.getMeshWidth());
        return this.getNode(xx, yy = (int)(yp / (float)this.getMeshWidth())).getTexture() == Tiles.Tile.TILE_HOLE.getId();
    }

    public float getHeight(float xp, float yp) {
        int yy;
        while (xp < 0.0f) {
            xp += (float)(this.getWidth() * this.getMeshWidth());
        }
        while (yp < 0.0f) {
            yp += (float)(this.getHeight() * this.getMeshWidth());
        }
        int xx = (int)(xp / (float)this.getMeshWidth());
        if (this.getNode(xx, yy = (int)(yp / (float)this.getMeshWidth())).getTexture() == Tiles.Tile.TILE_HOLE.getId()) {
            return 1.0f;
        }
        float xa = xp / (float)this.getMeshWidth() - (float)xx;
        float ya = yp / (float)this.getMeshWidth() - (float)yy;
        if (xa < 0.0f) {
            xa = -xa;
        }
        if (ya < 0.0f) {
            ya = -ya;
        }
        float height = 0.0f;
        if (xa > ya) {
            xa -= ya;
            float xheight1 = this.getNode(xx, yy).getHeight() * (1.0f - (xa /= 1.0f - ya)) + this.getNode(xx + 1, yy).getHeight() * xa;
            float xheight2 = this.getNode(xx + 1, yy + 1).getHeight();
            height = xheight1 * (1.0f - ya) + xheight2 * ya;
        } else {
            if (ya <= 0.001f) {
                ya = 0.001f;
            }
            float xheight1 = this.getNode(xx, yy).getHeight();
            float xheight2 = this.getNode(xx, yy + 1).getHeight() * (1.0f - (xa /= ya)) + this.getNode(xx + 1, yy + 1).getHeight() * xa;
            height = xheight1 * (1.0f - ya) + xheight2 * ya;
        }
        return height;
    }

    public boolean setTiles(int xStart, int yStart, int w, int h, int[][] tiles) {
        boolean changed = false;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                Node node = this.getNode(x + xStart, y + yStart);
                node.setHeight(Tiles.decodeHeightAsFloat(tiles[x][y]));
                short t = Tiles.decodeType(tiles[x][y]);
                short d = Tiles.decodeData(tiles[x][y]);
                if (t == node.getTexture() && d == node.data) continue;
                node.setTexture((byte)t);
                node.setData((byte)d);
                changed = true;
            }
        }
        return changed;
    }

    public void refresh(int xStart, int yStart, int w, int h) {
        this.calculateNormals(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
        this.processData(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
    }

    public void reset() {
        this.generateEmpty(false);
    }
}

