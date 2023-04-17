/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.zones.CropTile;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.SoundNames;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CropTilePoller
implements TimeConstants,
SoundNames {
    private static final Logger logger = Logger.getLogger(CropTilePoller.class.getName());
    public static boolean logTilePolling = false;
    private static volatile long lastPolledTiles = 0L;
    private static final Map<Long, CropTile> tiles = new HashMap<Long, CropTile>();
    private static boolean isInitialized = false;

    private static Long cropTileIndexFor(CropTile cropTile) {
        return Tiles.getTileId(cropTile.getX(), cropTile.getY(), 0, cropTile.isOnSurface());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addCropTile(int tileData, int x, int y, int cropType, boolean surface) {
        Map<Long, CropTile> map = tiles;
        synchronized (map) {
            CropTile cTile = new CropTile(tileData, x, y, cropType, surface);
            tiles.put(CropTilePoller.cropTileIndexFor(cTile), cTile);
        }
    }

    public static void initializeFields() {
        logger.log(Level.INFO, "CROPS_POLLER: Collecting tile data.");
        CropTilePoller.addAllFieldsInMesh(Server.surfaceMesh, true);
        CropTilePoller.addAllFieldsInMesh(Server.caveMesh, false);
        CropTilePoller.logCropFields();
        isInitialized = true;
        logger.log(Level.INFO, "CROPS_POLLER: Collecting tile Finished.");
    }

    private static void addAllFieldsInMesh(MeshIO mesh, boolean surface) {
        int mapSize = mesh.getSize();
        Tiles.Tile tileToLookFor = Tiles.Tile.TILE_FIELD;
        Tiles.Tile tile2ToLookFor = Tiles.Tile.TILE_FIELD2;
        for (int x = 0; x < mapSize; ++x) {
            for (int y = 0; y < mapSize; ++y) {
                int tileId = mesh.getTile(x, y);
                byte type = Tiles.decodeType(tileId);
                Tiles.Tile tileEnum = Tiles.getTile(type);
                if (tileEnum == null || tileEnum.id != tileToLookFor.id && tileEnum.id != tile2ToLookFor.id) continue;
                byte data = Tiles.decodeData(tileId);
                int crop = Crops.getCropNumber(type, data);
                CropTile cropTile = new CropTile(tileId, x, y, crop, surface);
                tiles.put(CropTilePoller.cropTileIndexFor(cropTile), cropTile);
            }
        }
    }

    private static void logCropFields() {
        Integer count;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (CropTile tile : tiles.values()) {
            Integer crop = tile.getCropType();
            count = (Integer)map.get(crop);
            if (count == null) {
                map.put(crop, 1);
                continue;
            }
            count = count + 1;
            map.put(crop, count);
        }
        String text = "\n";
        for (Integer crop : map.keySet()) {
            count = (Integer)map.get(crop);
            String cropName = Crops.getCropName(crop);
            text = text + cropName + " fields: " + count.toString() + "\n";
        }
        logger.log(Level.INFO, text);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void pollCropTiles() {
        long fieldGrowthTime;
        boolean timeToPoll;
        if (!isInitialized) {
            return;
        }
        long now = System.currentTimeMillis();
        long elapsedSinceLastPoll = now - lastPolledTiles;
        boolean bl = timeToPoll = elapsedSinceLastPoll >= (fieldGrowthTime = Servers.localServer.getFieldGrowthTime());
        if (!timeToPoll) {
            return;
        }
        ArrayList<CropTile> toRemove = new ArrayList<CropTile>();
        Map<Long, CropTile> map = tiles;
        synchronized (map) {
            if (now - lastPolledTiles < fieldGrowthTime) {
                return;
            }
            lastPolledTiles = System.currentTimeMillis();
            for (CropTile cTile : tiles.values()) {
                int currTileId;
                byte type;
                Tiles.Tile tileEnum;
                MeshIO meshToUse = Server.surfaceMesh;
                if (!cTile.isOnSurface()) {
                    meshToUse = Server.caveMesh;
                }
                if ((tileEnum = Tiles.getTile(type = Tiles.decodeType(currTileId = meshToUse.getTile(cTile.getX(), cTile.getY())))) == null || tileEnum.id != Tiles.Tile.TILE_FIELD.id && tileEnum.id != Tiles.Tile.TILE_FIELD2.id) {
                    toRemove.add(cTile);
                    continue;
                }
                byte data = Tiles.decodeData(currTileId);
                CropTilePoller.checkForFarmGrowth(currTileId, cTile.getX(), cTile.getY(), type, data, meshToUse, cTile.isOnSurface());
            }
            for (CropTile t : toRemove) {
                tiles.remove(CropTilePoller.cropTileIndexFor(t));
            }
        }
        logger.fine("Completed poll of crop tiles.");
    }

    public static void checkForFarmGrowth(int tile, int tilex, int tiley, byte type, byte aData, MeshIO currentMesh, boolean pollingSurface) {
        if (Zones.protectedTiles[tilex][tiley]) {
            return;
        }
        int tileAge = Crops.decodeFieldAge(aData);
        int crop = Crops.getCropNumber(type, aData);
        boolean farmed = Crops.decodeFieldState(aData);
        if (logTilePolling) {
            logger.log(Level.INFO, "Polling farm at " + tilex + "," + tiley + ", age=" + tileAge + ", crop=" + crop + ", farmed=" + farmed);
        }
        if (tileAge == 7) {
            if (Server.rand.nextInt(100) <= 10) {
                currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
                Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
                Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
                if (logTilePolling) {
                    logger.log(Level.INFO, "Set to dirt");
                }
            }
            if (WurmCalendar.isNight()) {
                SoundPlayer.playSound("sound.birdsong.bird1", tilex, tiley, pollingSurface, 2.0f);
            } else {
                SoundPlayer.playSound("sound.birdsong.bird3", tilex, tiley, pollingSurface, 2.0f);
            }
        } else if (tileAge < 7) {
            if ((tileAge == 5 || tileAge == 6) && Server.rand.nextInt(3) < 2) {
                return;
            }
            ++tileAge;
            VolaTile tempvtile1 = Zones.getOrCreateTile(tilex, tiley, pollingSurface);
            if (tempvtile1.getStructure() != null) {
                currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
                Server.modifyFlagsByTileType(tilex, tiley, Tiles.Tile.TILE_DIRT.id);
            } else {
                currentMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), type, Crops.encodeFieldData(false, tileAge, crop)));
                Server.modifyFlagsByTileType(tilex, tiley, type);
                if (logTilePolling) {
                    logger.log(Level.INFO, "Changed the tile");
                }
            }
            if (WurmCalendar.isNight()) {
                SoundPlayer.playSound("sound.ambient.night.crickets", tilex, tiley, pollingSurface, 0.0f);
            } else {
                SoundPlayer.playSound("sound.birdsong.bird2", tilex, tiley, pollingSurface, 1.0f);
            }
            Players.getInstance().sendChangedTile(tilex, tiley, pollingSurface, false);
        } else {
            logger.log(Level.WARNING, "Strange, tile " + tilex + ", " + tiley + " is field but has age above 7:" + tileAge + " crop is " + crop + " farmed is " + farmed);
        }
    }

    static {
        lastPolledTiles = System.currentTimeMillis();
    }
}

