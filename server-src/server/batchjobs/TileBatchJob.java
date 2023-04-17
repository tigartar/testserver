/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.batchjobs;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TileBatchJob {
    private static Logger logger = Logger.getLogger(TileBatchJob.class.getName());

    private TileBatchJob() {
    }

    public static final void convertTreeTilesToBushTiles() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int tile = 0;
        int fixed = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                tile = Server.surfaceMesh.getTile(x, y);
                byte tileType = Tiles.decodeType(tile);
                if (tileType != Tiles.Tile.TILE_TREE.id && tileType != Tiles.Tile.TILE_ENCHANTED_TREE.id && tileType != Tiles.Tile.TILE_MYCELIUM_TREE.id) continue;
                byte data = Tiles.decodeData(tile);
                byte treeType = (byte)(data & 0xF);
                byte treeAge = (byte)(data >>> 4 & 0xF);
                if (treeType >= 0 && treeType < 10 || treeType == 16 || treeType == 17 || treeType < 10 || treeType > 15) continue;
                switch (treeType) {
                    case 10: {
                        treeType = (byte)BushData.BushType.LAVENDER.getTypeId();
                        break;
                    }
                    case 11: {
                        treeType = (byte)BushData.BushType.ROSE.getTypeId();
                        break;
                    }
                    case 12: {
                        treeType = (byte)BushData.BushType.THORN.getTypeId();
                        break;
                    }
                    case 13: {
                        treeType = (byte)BushData.BushType.GRAPE.getTypeId();
                        break;
                    }
                    case 14: {
                        treeType = (byte)BushData.BushType.CAMELLIA.getTypeId();
                        break;
                    }
                    case 15: {
                        treeType = (byte)BushData.BushType.OLEANDER.getTypeId();
                        break;
                    }
                }
                byte newTileType = Tiles.Tile.TILE_BUSH.id;
                if (tileType == Tiles.Tile.TILE_ENCHANTED_TREE.id) {
                    newTileType = Tiles.Tile.TILE_ENCHANTED_BUSH.id;
                } else if (tileType == Tiles.Tile.TILE_MYCELIUM_TREE.id) {
                    newTileType = Tiles.Tile.TILE_MYCELIUM_BUSH.id;
                }
                data = (byte)(treeAge << 4 | treeType);
                Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), newTileType, data));
                ++fixed;
            }
        }
        logger.log(Level.INFO, "Fixed " + fixed + " tiles to bush.");
    }

    public static final void convertKelpReedToNewTileTypes() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int tile = 0;
        int fixedReed = 0;
        int fixedKelp = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                tile = Server.surfaceMesh.getTile(x, y);
                if (Tiles.decodeType(tile) != Tiles.Tile.TILE_GRASS.id) continue;
                byte data = Tiles.decodeData(tile);
                GrassData.GrassType grassType = GrassData.GrassType.decodeTileData(data);
                GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(data);
                GrassData.FlowerType flowerType = GrassData.FlowerType.decodeTileData(data);
                if (grassType == GrassData.GrassType.KELP) {
                    ++fixedKelp;
                    Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_KELP.id, GrassData.encodeGrassTileData(growthStage, grassType, flowerType)));
                    continue;
                }
                if (grassType == GrassData.GrassType.REED) {
                    ++fixedReed;
                    Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_REED.id, GrassData.encodeGrassTileData(growthStage, grassType, flowerType)));
                    continue;
                }
                if (grassType != GrassData.GrassType.GRASS) continue;
                Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, GrassData.encodeGrassTileData(growthStage, grassType, flowerType)));
            }
        }
        logger.log(Level.INFO, "Fixed " + fixedReed + " reed tiles and " + fixedKelp + " kelp tiles.");
    }

    public static final void changeTarredPlanksToNewType() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int fixed = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                byte data;
                int tile = Server.surfaceMesh.getTile(x, y);
                if (Tiles.decodeType(tile) != Tiles.Tile.TILE_PLANKS.id || (data = Tiles.decodeData(tile)) != 1) continue;
                short h = Tiles.decodeHeight(tile);
                Server.setSurfaceTile(x, y, h, Tiles.Tile.TILE_PLANKS_TARRED.id, (byte)0);
                ++fixed;
            }
        }
        logger.log(Level.INFO, "Fixed " + fixed + " tarred floorboards tiles.");
    }

    public static final void clearFlowers() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int tile = 0;
        int fixed = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                tile = Server.surfaceMesh.getTile(x, y);
                if (Tiles.decodeType(tile) != Tiles.Tile.TILE_GRASS.id || Tiles.decodeData(tile & 0xF) <= 0) continue;
                ++fixed;
                Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, (byte)0));
            }
        }
        logger.log(Level.INFO, "Fixed " + fixed + " flowers.");
    }

    public static final void fixCorruptBushData() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int tile = 0;
        int fixed = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                tile = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                if (type != 31 || (Tiles.decodeData(tile) & 0xF) != 15) continue;
                ++fixed;
                int bushType = Server.rand.nextInt(BushData.BushType.getLength());
                byte newData = (byte)((Tiles.decodeData(tile) & 0xF0) + bushType);
                Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), type, newData));
            }
        }
        logger.log(Level.INFO, "Fixed " + fixed + " bushes.");
    }

    public static final void splitTreeAndBushes() {
        boolean min = false;
        int ms = Constants.meshSize;
        int max = 1 << ms;
        int treefixed = 0;
        int bushfixed = 0;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                int tile = Server.surfaceMesh.getTile(x, y);
                byte tileType = Tiles.decodeType(tile);
                byte tileData = Tiles.decodeData(tile);
                byte newType = tileType;
                byte newData = tileData;
                if (tileType == 3) {
                    newType = TileBatchJob.getNewNormalTreeType(tileType, tileData);
                    ++treefixed;
                } else if (tileType == 11) {
                    newType = TileBatchJob.getNewMyceliumTreeType(tileType, tileData);
                    ++treefixed;
                } else if (tileType == 14) {
                    newType = TileBatchJob.getNewEnchantedTreeType(tileType, tileData);
                    ++treefixed;
                }
                if (tileType == 31) {
                    newType = TileBatchJob.getNewNormalBushType(tileType, tileData);
                    ++bushfixed;
                } else if (tileType == 35) {
                    newType = TileBatchJob.getNewMyceliumBushType(tileType, tileData);
                    ++bushfixed;
                } else if (tileType == 34) {
                    newType = TileBatchJob.getNewEnchantedBushType(tileType, tileData);
                    ++bushfixed;
                }
                if (newType == tileType) continue;
                newData = (byte)((tileData & 0xF0) + 1);
                Server.surfaceMesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), newType, newData));
            }
        }
        logger.log(Level.INFO, "Converted " + treefixed + " trees and " + bushfixed + " bushes.");
    }

    private static byte getNewNormalTreeType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return 100;
            }
            case 1: {
                return 101;
            }
            case 2: {
                return 102;
            }
            case 3: {
                return 103;
            }
            case 4: {
                return 104;
            }
            case 5: {
                return 105;
            }
            case 6: {
                return 106;
            }
            case 7: {
                return 107;
            }
            case 8: {
                return 108;
            }
            case 9: {
                return 109;
            }
            case 10: {
                return 110;
            }
            case 11: {
                return 111;
            }
            case 12: {
                return 112;
            }
            case 13: {
                return 113;
            }
        }
        return tileType;
    }

    private static byte getNewEnchantedTreeType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return -128;
            }
            case 1: {
                return -127;
            }
            case 2: {
                return -126;
            }
            case 3: {
                return -125;
            }
            case 4: {
                return -124;
            }
            case 5: {
                return -123;
            }
            case 6: {
                return -122;
            }
            case 7: {
                return -121;
            }
            case 8: {
                return -120;
            }
            case 9: {
                return -119;
            }
            case 10: {
                return -118;
            }
            case 11: {
                return -117;
            }
            case 12: {
                return -116;
            }
            case 13: {
                return -115;
            }
        }
        return tileType;
    }

    private static byte getNewMyceliumTreeType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return 114;
            }
            case 1: {
                return 115;
            }
            case 2: {
                return 116;
            }
            case 3: {
                return 117;
            }
            case 4: {
                return 118;
            }
            case 5: {
                return 119;
            }
            case 6: {
                return 120;
            }
            case 7: {
                return 121;
            }
            case 8: {
                return 122;
            }
            case 9: {
                return 123;
            }
            case 10: {
                return 124;
            }
            case 11: {
                return 125;
            }
            case 12: {
                return 126;
            }
            case 13: {
                return 127;
            }
        }
        return tileType;
    }

    private static byte getNewNormalBushType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return -114;
            }
            case 1: {
                return -113;
            }
            case 2: {
                return -112;
            }
            case 3: {
                return -111;
            }
            case 4: {
                return -110;
            }
            case 5: {
                return -109;
            }
        }
        return tileType;
    }

    private static byte getNewEnchantedBushType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return -102;
            }
            case 1: {
                return -101;
            }
            case 2: {
                return -100;
            }
            case 3: {
                return -99;
            }
            case 4: {
                return -98;
            }
            case 5: {
                return -97;
            }
        }
        return tileType;
    }

    private static byte getNewMyceliumBushType(byte tileType, byte tileData) {
        switch (tileData & 0xF) {
            case 0: {
                return -108;
            }
            case 1: {
                return -107;
            }
            case 2: {
                return -106;
            }
            case 3: {
                return -105;
            }
            case 4: {
                return -104;
            }
            case 5: {
                return -103;
            }
        }
        return tileType;
    }
}

