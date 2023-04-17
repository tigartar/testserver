/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbStructure;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.DbZone;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.GenericZone;
import com.wurmonline.server.zones.HiveZone;
import com.wurmonline.server.zones.InfluenceZone;
import com.wurmonline.server.zones.LongPosition;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.SpawnTable;
import com.wurmonline.server.zones.TilePoller;
import com.wurmonline.server.zones.TurretZone;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Water;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.EffectConstants;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Zones
implements MiscConstants,
EffectConstants,
CombatConstants,
TimeConstants {
    private static Zone[][] surfaceZones;
    private static Zone[][] caveZones;
    private static final Map<Integer, VirtualZone> virtualZones;
    private static final Map<Integer, Map<Integer, Byte>> miningTiles;
    public static final int zoneSize = 64;
    public static final int zoneShifter = 6;
    private static final Set<Item> duelRings;
    public static final int worldTileSizeX;
    public static final int worldTileSizeY;
    public static final float worldMeterSizeX;
    public static final float worldMeterSizeY;
    public static boolean[][] protectedTiles;
    static boolean[][] walkedTiles;
    private static final byte[][] kingdoms;
    public static final int faithSizeX;
    public static final int faithSizeY;
    public static final int DOMAIN_DIVISION = 64;
    public static final int domainSizeX;
    public static final int domainSizeY;
    public static final int INFLUENCE_DIVISION = 256;
    public static final int influenceSizeX;
    public static final int influenceSizeY;
    public static final int HIVE_DIVISION = 32;
    public static final int hiveZoneSizeX;
    public static final int hiveZoneSizeY;
    private static boolean hasLoadedChristmas;
    private static final Logger logger;
    private static int currentSaveZoneX;
    private static int currentSaveZoneY;
    private static boolean loading;
    public static int numberOfZones;
    private static int rest;
    private static int maxRest;
    private static int zonesPerRun;
    private static boolean haslogged;
    private static int coverHolder;
    private static final FaithZone[][] surfaceDomains;
    private static final FaithZone[][] caveDomains;
    private static final LinkedList<Item> altars;
    private static final ArrayList<HashMap<Item, FaithZone>> altarZones;
    private static final ArrayList<HashMap<Item, InfluenceZone>> influenceZones;
    private static final ArrayList<ConcurrentHashMap<Item, HiveZone>> hiveZones;
    private static final ConcurrentHashMap<Item, TurretZone> turretZones;
    private static final byte[][] influenceCache;
    private static int pollnum;
    private static int MESHSIZE;
    private static final String UPDATE_MININGTILE = "UPDATE MINING SET STATE=? WHERE TILEX=? AND TILEY=?";
    private static final String INSERT_MININGTILE = "INSERT INTO MINING (STATE,TILEX,TILEY) VALUES(?,?,?)";
    private static final String DELETE_MININGTILE = "DELETE FROM MINING WHERE TILEX=? AND TILEY=?";
    private static final String GET_ALL_MININGTILES = "SELECT * FROM MINING";
    private static final String GET_MININGTILE = "SELECT STATE FROM MINING WHERE TILEX=? AND TILEY=?";
    private static final LinkedList<Item> guardTowers;
    private static final String protectedTileFile;
    public static boolean shouldCreateWarTargets;
    public static boolean shouldSourceSprings;
    private static Map<Byte, Float> landPercent;
    public static Creature evilsanta;
    public static Creature santa;
    public static Creature santaMolRehan;
    public static final ConcurrentHashMap<Long, Creature> santas;
    static final Random zrand;
    private static int currentPollZoneX;
    private static int currentPollZoneY;
    private static boolean devlog;
    private static final Object ZONE_SYNC_LOCK;
    private static LinkedList<LongPosition> posmap;
    private static boolean hasStartedYet;
    private static long lastCounted;

    static void setLandPercent(byte kingdom, float percent) {
        landPercent.put(kingdom, Float.valueOf(percent));
    }

    public static float getPercentLandForKingdom(byte kingdom) {
        Float f = landPercent.get(kingdom);
        if (f != null) {
            return f.floatValue();
        }
        return 0.0f;
    }

    public static void saveProtectedTiles() {
        File f = new File(protectedTileFile);
        try {
            f.createNewFile();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        try {
            DataOutputStream ds = new DataOutputStream(new FileOutputStream(f));
            ObjectOutputStream oos = new ObjectOutputStream(ds);
            oos.writeObject(protectedTiles);
            oos.flush();
            oos.close();
            ds.close();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
    }

    public static final void addGuardTowerInfluence(Item tower, boolean silent) {
        if (Features.Feature.NEW_KINGDOM_INF.isEnabled()) {
            int actualZone;
            HashMap<Item, InfluenceZone> thisZone;
            if (influenceZones.isEmpty()) {
                Zones.initInfluenceZones();
            }
            if ((thisZone = influenceZones.get(actualZone = Math.max(0, tower.getTileY() / 256) * influenceSizeX + Math.max(0, tower.getTileX() / 256))) == null) {
                thisZone = new HashMap();
            }
            InfluenceZone newZone = new InfluenceZone(tower);
            thisZone.put(tower, newZone);
            influenceZones.set(actualZone, thisZone);
            int i = (int)((float)tower.getTileX() - tower.getCurrentQualityLevel());
            while ((float)i < (float)tower.getTileX() + tower.getCurrentQualityLevel()) {
                int j = (int)((float)tower.getTileY() - tower.getCurrentQualityLevel());
                while ((float)j < (float)tower.getTileY() + tower.getCurrentQualityLevel()) {
                    Zones.influenceCache[i][j] = -1;
                    ++j;
                }
                ++i;
            }
        }
    }

    public static final void removeGuardTowerInfluence(Item tower, boolean silent) {
        if (Features.Feature.NEW_KINGDOM_INF.isEnabled()) {
            int actualZone;
            HashMap<Item, InfluenceZone> thisZone;
            if (influenceZones.isEmpty()) {
                Zones.initInfluenceZones();
            }
            if ((thisZone = influenceZones.get(actualZone = Math.max(0, tower.getTileY() / 256) * influenceSizeX + Math.max(0, tower.getTileX() / 256))) == null) {
                return;
            }
            thisZone.remove(tower);
            int i = (int)((float)tower.getTileX() - tower.getCurrentQualityLevel());
            while ((float)i < (float)tower.getTileX() + tower.getCurrentQualityLevel()) {
                int j = (int)((float)tower.getTileY() - tower.getCurrentQualityLevel());
                while ((float)j < (float)tower.getTileY() + tower.getCurrentQualityLevel()) {
                    Zones.influenceCache[i][j] = -1;
                    ++j;
                }
                ++i;
            }
        }
    }

    public static final byte getKingdom(int tilex, int tiley) {
        if (Servers.localServer.HOMESERVER) {
            return Servers.localServer.KINGDOM;
        }
        if (Features.Feature.NEW_KINGDOM_INF.isEnabled()) {
            if (influenceZones.isEmpty()) {
                Zones.initInfluenceZones();
                if (!guardTowers.isEmpty()) {
                    for (Item i : guardTowers) {
                        Zones.addGuardTowerInfluence(i, true);
                    }
                }
            }
            if (influenceCache[Zones.safeTileX(tilex)][Zones.safeTileY(tiley)] != -1) {
                return influenceCache[Zones.safeTileX(tilex)][Zones.safeTileY(tiley)];
            }
            VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
            if (t != null && t.getVillage() != null) {
                return t.getVillage().kingdom;
            }
            GenericZone toReturn = null;
            HashMap<Item, InfluenceZone> thisZone = null;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    int actualZone = Math.max(0, Math.min(tiley / 256 + j, influenceSizeY - 1)) * influenceSizeX + Math.max(0, Math.min(tilex / 256 + i, influenceSizeX - 1));
                    if (actualZone >= influenceZones.size() || (thisZone = influenceZones.get(actualZone)) == null) continue;
                    for (InfluenceZone inf : thisZone.values()) {
                        if (!inf.containsTile(tilex, tiley) || inf.getStrengthForTile(tilex, tiley, true) <= 0.0f) continue;
                        if (toReturn == null) {
                            toReturn = inf;
                            continue;
                        }
                        if (!(((InfluenceZone)toReturn).getStrengthForTile(tilex, tiley, true) <= inf.getStrengthForTile(tilex, tiley, true))) continue;
                        toReturn = inf;
                    }
                }
            }
            if (toReturn == null) {
                return 0;
            }
            Zones.influenceCache[tilex][tiley] = toReturn.getZoneItem().getKingdom();
            return toReturn.getZoneItem().getKingdom();
        }
        return kingdoms[Zones.safeTileX(tilex)][Zones.safeTileY(tiley)];
    }

    public static boolean isKingdomBlocking(int tilex, int tiley, int endx, int endy, byte founderKingdom, int exclusionZoneX, int exclusionZoneY, int exclusionZoneEndX, int exclusionZoneEndY) {
        int startx = Zones.safeTileX(tilex);
        int starty = Zones.safeTileY(tiley);
        int ex = Zones.safeTileX(endx);
        int ey = Zones.safeTileY(endy);
        boolean hasExclusionZone = exclusionZoneX != -1;
        int exclusionX = Zones.safeTileX(exclusionZoneX);
        int exclusionY = Zones.safeTileY(exclusionZoneY);
        int exclusionEndX = Zones.safeTileX(exclusionZoneEndX);
        int exclusionEndY = Zones.safeTileY(exclusionZoneEndY);
        for (int x = startx; x < ex; ++x) {
            if (hasExclusionZone && x >= exclusionX && x <= exclusionEndX) continue;
            for (int y = starty; y < ey; ++y) {
                if (hasExclusionZone && y >= exclusionY && y <= exclusionEndY || Zones.getKingdom(x, y) == 0 || Zones.getKingdom(x, y) == founderKingdom) continue;
                return false;
            }
        }
        return true;
    }

    public static boolean isKingdomBlocking(int tilex, int tiley, int endx, int endy, byte founderKingdom) {
        return Zones.isKingdomBlocking(tilex, tiley, endx, endy, founderKingdom, -1, -1, -1, -1);
    }

    public static boolean isWithinDuelRing(int tilex, int tiley, int endx, int endy) {
        int startx = Zones.safeTileX(tilex);
        int starty = Zones.safeTileY(tiley);
        int ex = Zones.safeTileX(endx);
        int ey = Zones.safeTileY(endy);
        for (int x = startx; x < ex; ++x) {
            for (int y = starty; y < ey; ++y) {
                Item ring = Zones.isWithinDuelRing(x, y, true);
                if (ring == null) continue;
                return true;
            }
        }
        return false;
    }

    public static final void setKingdom(int tilex, int tiley, byte kingdom) {
        Zones.kingdoms[Zones.safeTileX((int)tilex)][Zones.safeTileY((int)tiley)] = kingdom;
        if (Server.getSecondsUptime() > 10) {
            Zones.setKingdomOn(tilex, tiley, kingdom, true);
            Zones.setKingdomOn(tilex, tiley, kingdom, false);
        }
    }

    private static final void setKingdomOn(int tilex, int tiley, byte kingdom, boolean onSurface) {
        VolaTile t = Zones.getTileOrNull(tilex, tiley, onSurface);
        if (t != null) {
            Creature[] crets;
            for (Creature c : crets = t.getCreatures()) {
                c.setCurrentKingdom(kingdom);
            }
        }
    }

    public static final void setKingdom(int tilex, int tiley, int sizex, int sizey, byte kingdom) {
        for (int x = tilex; x < tilex + sizex; ++x) {
            for (int y = tiley; y < tiley + sizey; ++y) {
                Zones.kingdoms[Zones.safeTileX((int)x)][Zones.safeTileY((int)y)] = kingdom;
            }
        }
    }

    public static void loadProtectedTiles() {
        logger.info("Loading protected tiles from file: " + protectedTileFile);
        long start = System.nanoTime();
        File f = new File(protectedTileFile);
        try {
            if (f.createNewFile()) {
                Zones.saveProtectedTiles();
                logger.log(Level.INFO, "Created first instance of protected tiles file.");
            }
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        try {
            DataInputStream ds = new DataInputStream(new FileInputStream(f));
            ObjectInputStream oos = new ObjectInputStream(ds);
            boolean[][] protectedTileArr = (boolean[][])oos.readObject();
            oos.close();
            ds.close();
            boolean[][] tmpTiles = new boolean[worldTileSizeX][worldTileSizeY];
            int protectedt = 0;
            int wtx = worldTileSizeX;
            int wty = worldTileSizeY;
            try {
                for (int x = 0; x < wtx; ++x) {
                    for (int y = 0; y < wty; ++y) {
                        tmpTiles[x][y] = protectedTileArr[x][y];
                        if (!tmpTiles[x][y]) continue;
                        ++protectedt;
                    }
                }
                protectedTiles = tmpTiles;
            }
            catch (Exception ex) {
                for (int x = 0; x < wtx; ++x) {
                    for (int y = 0; y < wty; ++y) {
                        Zones.protectedTiles[x][y] = false;
                    }
                }
                f.delete();
            }
            logger.log(Level.INFO, "Loaded " + protectedt + " protected tiles. It took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms.");
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        catch (ClassNotFoundException nsc) {
            logger.log(Level.WARNING, nsc.getMessage(), nsc);
        }
    }

    public static final boolean isTileProtected(int tilex, int tiley) {
        return protectedTiles[tilex][tiley];
    }

    public static final boolean isTileCornerProtected(int tilex, int tiley) {
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                if (!Zones.isTileProtected(tilex - x, tiley - y)) continue;
                return true;
            }
        }
        return false;
    }

    private Zones() {
    }

    static void initializeWalkTiles() {
        logger.info("Initialising walked tiles");
        long start = System.nanoTime();
        int wsx = worldTileSizeX;
        int wsy = worldTileSizeY;
        boolean[][] tmptiles = new boolean[wsx][wsy];
        for (int x = 0; x < wsx; ++x) {
            for (int y = 0; y < wsy; ++y) {
                tmptiles[x][y] = true;
            }
        }
        walkedTiles = tmptiles;
        logger.log(Level.INFO, "Initialised walked tiles. It took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms.");
    }

    static void addGuardTower(Item tower) {
        if (loading) {
            guardTowers.add(tower);
        } else {
            Kingdoms.addTower(tower);
        }
        Zones.addGuardTowerInfluence(tower, true);
    }

    public static void removeGuardTower(Item tower) {
        guardTowers.remove(tower);
        Zones.removeGuardTowerInfluence(tower, true);
    }

    public static LinkedList<Item> getGuardTowers() {
        return guardTowers;
    }

    private static void initInfluenceZones() {
        if (!influenceZones.isEmpty()) {
            return;
        }
        for (int y = 0; y < influenceSizeY; ++y) {
            for (int x = 0; x < influenceSizeX; ++x) {
                influenceZones.add(null);
            }
        }
    }

    public static void loadTowers() {
        logger.info("Loading guard towers.");
        long now = System.nanoTime();
        if (Features.Feature.NEW_KINGDOM_INF.isEnabled() && influenceZones.isEmpty()) {
            Zones.initInfluenceZones();
        }
        ListIterator it = guardTowers.listIterator();
        while (it.hasNext()) {
            Item gt = (Item)it.next();
            Kingdom k = Kingdoms.getKingdom(gt.getAuxData());
            it.remove();
            if (!k.existsHere()) {
                logger.log(Level.INFO, "Removing tower for non-existent kingdom of " + k.getName());
                Kingdoms.destroyTower(gt, true);
                continue;
            }
            Kingdoms.addTower(gt);
        }
        if (Features.Feature.NEW_KINGDOM_INF.isEnabled()) {
            for (int i = 0; i < worldTileSizeX; ++i) {
                for (int j = 0; j < worldTileSizeY; ++j) {
                    Zones.getKingdom(i, j);
                }
            }
        }
        logger.log(Level.INFO, "Loaded " + Kingdoms.getNumberOfGuardTowers() + " Guard towers. That took " + (float)(System.nanoTime() - now) / 1000000.0f + " ms.");
    }

    public static final int getCoverHolder() {
        return coverHolder;
    }

    public static final void resetCoverHolder() {
        coverHolder = 0;
    }

    public static void calculateZones(boolean overRide) {
        if (System.currentTimeMillis() - lastCounted > 60000L || overRide) {
            landPercent.clear();
            long now = System.currentTimeMillis();
            int[] zoneControl = new int[255];
            for (int x = 0; x < worldTileSizeX; ++x) {
                for (int y = 0; y < worldTileSizeY; ++y) {
                    byte kingdom = Zones.getKingdom(x, y);
                    int n = kingdom & 0xFF;
                    zoneControl[n] = zoneControl[n] + 1;
                }
            }
            lastCounted = System.currentTimeMillis();
            long numberOfTiles = worldTileSizeX * worldTileSizeY;
            for (int x = 0; x < 255; ++x) {
                if (zoneControl[x] <= 0) continue;
                Zones.setLandPercent((byte)x, (float)zoneControl[x] * 100.0f / (float)numberOfTiles);
            }
            if (System.currentTimeMillis() - now > 1000L) {
                logger.log(Level.INFO, "Calculating zones took " + (System.currentTimeMillis() - now) + " millis");
            }
        }
    }

    static void addAltar(Item altar, boolean silent) {
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            int actualZone = Math.max(0, altar.getTileY() / 64) * domainSizeX + Math.max(0, altar.getTileX() / 64);
            HashMap<Item, FaithZone> thisZone = altarZones.get(actualZone);
            if (thisZone == null) {
                thisZone = new HashMap();
            }
            FaithZone newZone = new FaithZone(altar);
            FaithZone oldZone = null;
            VolaTile tempTile = null;
            thisZone.put(altar, newZone);
            altarZones.set(actualZone, thisZone);
            if (!silent && newZone.getCurrentRuler() != null) {
                try {
                    for (int i = newZone.getStartX(); i < newZone.getEndX(); ++i) {
                        for (int j = newZone.getStartY(); j < newZone.getEndY(); ++j) {
                            tempTile = Zones.getTileOrNull(i, j, altar.isOnSurface());
                            if (tempTile == null) continue;
                            oldZone = Zones.getFaithZone(i, j, altar.isOnSurface());
                            if (newZone.getStrengthForTile(i, j, altar.isOnSurface()) <= 0) continue;
                            if (oldZone == null) {
                                tempTile.broadCast("The domain of " + newZone.getCurrentRuler().getName() + " has now reached this place.");
                                continue;
                            }
                            if (oldZone.getStrengthForTile(i, j, altar.isOnSurface()) >= newZone.getStrengthForTile(i, j, altar.isOnSurface())) continue;
                            tempTile.broadCast(newZone.getCurrentRuler().getName() + "'s domain is now the strongest here!");
                        }
                    }
                }
                catch (NoSuchZoneException e) {
                    logger.log(Level.WARNING, "Error getting existing zones when adding new altar.");
                }
            }
        } else {
            altars.add(altar);
        }
    }

    static void removeAltar(Item altar, boolean silent) {
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            int actualZone = Math.max(0, altar.getTileY() / 64) * domainSizeX + Math.max(0, altar.getTileX() / 64);
            HashMap<Item, FaithZone> thisZone = altarZones.get(actualZone);
            if (thisZone == null) {
                logger.log(Level.WARNING, "AltarZone was NULL when it should not have been: " + actualZone);
                return;
            }
            FaithZone oldZone = thisZone.remove(altar);
            FaithZone newZone = null;
            VolaTile tempTile = null;
            if (!silent && oldZone != null && oldZone.getCurrentRuler() != null) {
                try {
                    for (int i = oldZone.getStartX(); i < oldZone.getEndX(); ++i) {
                        for (int j = oldZone.getStartY(); j < oldZone.getEndY(); ++j) {
                            tempTile = Zones.getTileOrNull(i, j, altar.isOnSurface());
                            if (tempTile == null) continue;
                            newZone = Zones.getFaithZone(i, j, altar.isOnSurface());
                            if (newZone == null) {
                                tempTile.broadCast(oldZone.getCurrentRuler().getName() + " has had to lose " + oldZone.getCurrentRuler().getHisHerItsString() + " hold over this area!");
                                continue;
                            }
                            tempTile.broadCast(newZone.getCurrentRuler().getName() + "'s domain is now the strongest here!");
                        }
                    }
                }
                catch (NoSuchZoneException e) {
                    logger.log(Level.WARNING, "Error getting existing zones when adding new altar.");
                }
            }
        } else {
            altars.remove(altar);
        }
    }

    public static void calcCreatures(Creature responder) {
        int c;
        Creature[] crets;
        int y;
        int x;
        int visible = 0;
        int offline = 0;
        int total = 0;
        int submerged = 0;
        int surfbelowsurface = 0;
        int cavebelowsurface = 0;
        for (x = 0; x < worldTileSizeX >> 6; ++x) {
            for (y = 0; y < worldTileSizeY >> 6; ++y) {
                crets = surfaceZones[x][y].getAllCreatures();
                for (c = 0; c < crets.length; ++c) {
                    ++total;
                    if (crets[c].isVisible()) {
                        ++visible;
                    }
                    if (crets[c].isOffline()) {
                        ++offline;
                    }
                    if (!(crets[c].getPositionZ() < -10.0f)) continue;
                    ++surfbelowsurface;
                }
            }
        }
        for (x = 0; x < worldTileSizeX >> 6; ++x) {
            for (y = 0; y < worldTileSizeY >> 6; ++y) {
                crets = caveZones[x][y].getAllCreatures();
                for (c = 0; c < crets.length; ++c) {
                    ++total;
                    ++submerged;
                    if (crets[c].isVisible()) {
                        ++visible;
                    }
                    if (crets[c].isOffline()) {
                        ++offline;
                    }
                    if (!(crets[c].getPositionZ() < -10.0f)) continue;
                    ++cavebelowsurface;
                }
            }
        }
        responder.getCommunicator().sendNormalServerMessage("Creatures total:" + total + ", On surface=" + (total - submerged) + " (of which " + surfbelowsurface + " are below -10 meters), in Caves=" + submerged + " (of which " + cavebelowsurface + " are below -10 meters), visible=" + visible + ", offline=" + offline + ".");
    }

    public static Item[] getAltars() {
        return altars.toArray(new Item[altars.size()]);
    }

    public static Item[] getAltars(int deityId) {
        HashSet<Item> lAltars = new HashSet<Item>();
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            for (HashMap<Item, FaithZone> m : altarZones) {
                if (m == null) continue;
                for (Item altar : m.keySet()) {
                    Deity deity;
                    if (altar == null || ((deity = altar.getBless()) != null || deityId != 0) && (deity == null || deity.getNumber() != deityId)) continue;
                    lAltars.add(altar);
                }
            }
        } else {
            for (Item altar : altars) {
                Deity deity = altar.getBless();
                if ((deity != null || deityId != 0) && (deity == null || deity.getNumber() != deityId)) continue;
                lAltars.add(altar);
            }
        }
        return lAltars.toArray(new Item[lAltars.size()]);
    }

    public static void checkAltars() {
        long now = 0L;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Checking altars.");
            now = System.nanoTime();
        }
        for (Item altar : altars) {
            Zones.addToDomains(altar);
        }
        if (logger.isLoggable(Level.FINEST)) {
            int numberOfAltars = altars.size();
            logger.log(Level.FINEST, "Checked " + numberOfAltars + " altars. That took " + (float)(System.nanoTime() - now) / 1000000.0f + " ms.");
        }
        if (shouldCreateWarTargets) {
            shouldCreateWarTargets = false;
            int x = worldTileSizeX / 6;
            int y = worldTileSizeY / 6;
            Zones.createCampsOnLine(x, y);
            y = worldTileSizeY / 2;
            Zones.createCampsOnLine(x, y);
            Zones.createCampsOnLine(x, y += worldTileSizeY / 6);
        }
        if (shouldSourceSprings) {
            shouldSourceSprings = false;
            Zones.createSprings();
        }
    }

    public static final void addWarDomains() {
        Item[] targs = Items.getWarTargets();
        if (targs != null && targs.length > 0) {
            for (Item target : targs) {
                Kingdoms.addWarTargetKingdom(target);
            }
        }
    }

    private static final void createSprings() {
        int springs = worldTileSizeX / 50;
        for (int a = 0; a < springs; ++a) {
            boolean found = false;
            int tries = 0;
            logger.log(Level.INFO, "Trying to create spring " + a);
            while (!found && tries < 1000) {
                int ty;
                ++tries;
                int tx = worldTileSizeX / 3 + Server.rand.nextInt(worldTileSizeY / 3);
                int tile = Server.surfaceMesh.getTile(tx, ty = worldTileSizeX / 3 + Server.rand.nextInt(worldTileSizeY / 3));
                if (Tiles.decodeHeight(tile) <= 5) continue;
                try {
                    int type = 766;
                    if (Server.rand.nextBoolean()) {
                        type = 767;
                    }
                    Item target1 = ItemFactory.createItem(type, 100.0f, tx * 4 + 2, ty * 4 + 2, Server.rand.nextInt(360), true, (byte)0, -10L, "");
                    target1.setSizes(target1.getSizeX() + Server.rand.nextInt(1), target1.getSizeY() + Server.rand.nextInt(2), target1.getSizeZ() + Server.rand.nextInt(3));
                    logger.log(Level.INFO, "Created " + target1.getName() + " at " + target1.getTileX() + " " + target1.getTileY() + " sizes " + target1.getSizeX() + "," + target1.getSizeY() + "," + target1.getSizeZ() + ")");
                    Items.addSourceSpring(target1);
                    found = true;
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
        }
    }

    private static final void createCampsOnLine(int x, int y) {
        Village[] villages = Villages.getVillages();
        boolean found = false;
        int tries = 0;
        logger.log(Level.INFO, "Trying to create camp at " + x + "," + y);
        while (!found && tries < 1000) {
            int tile;
            ++tries;
            int tx = Zones.safeTileY(x + Server.rand.nextInt(worldTileSizeX / 3));
            int ty = Zones.safeTileY(y + Server.rand.nextInt(worldTileSizeY / 3));
            boolean inVillage = false;
            for (Village v : villages) {
                if (!v.coversWithPerimeterAndBuffer(tx, ty, 60)) continue;
                inVillage = true;
                break;
            }
            if (inVillage || Tiles.decodeHeight(tile = Server.surfaceMesh.getTile(tx, ty)) <= 2 || !Terraforming.isAllCornersInsideHeightRange(tx, ty, true, (short)(Tiles.decodeHeight(tile) + 5), (short)(Tiles.decodeHeight(tile) - 5))) continue;
            try {
                int type = Server.rand.nextInt(3);
                if (type == 0) {
                    type = 760;
                } else if (type == 1) {
                    type = 762;
                } else if (type == 2) {
                    type = 761;
                }
                Item target1 = ItemFactory.createItem(type, 100.0f, tx * 4 + 2, ty * 4 + 2, Server.rand.nextInt(360), true, (byte)0, -10L, "");
                target1.setName(Zones.createTargName(target1.getName()));
                logger.log(Level.INFO, "Created " + target1.getName() + " at " + target1.getTileX() + " " + target1.getTileY());
                Items.addWarTarget(target1);
                new FocusZone(tx - 60, tx + 60, ty - 60, ty + 60, 7, target1.getName(), "", true);
                found = true;
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
            }
        }
        x = x == 0 ? worldTileSizeX / 2 - worldTileSizeX / 6 : worldTileSizeX / 2 + worldTileSizeX / 6;
    }

    public static final void createBattleCamp(int tx, int ty) {
        try {
            int type = Server.rand.nextInt(3);
            if (type == 0) {
                type = 760;
            } else if (type == 1) {
                type = 762;
            } else if (type == 2) {
                type = 761;
            }
            Item target1 = ItemFactory.createItem(type, 100.0f, tx * 4 + 2, ty * 4 + 2, Server.rand.nextInt(360), true, (byte)0, -10L, "");
            target1.setName(Zones.createTargName(target1.getName()));
            logger.log(Level.INFO, "Created " + target1.getName() + " at " + target1.getTileX() + " " + target1.getTileY());
            Items.addWarTarget(target1);
            new FocusZone(tx - 60, tx + 60, ty - 60, ty + 60, 7, target1.getName(), "", true);
        }
        catch (FailedException fe) {
            logger.log(Level.WARNING, fe.getMessage(), fe);
        }
        catch (NoSuchTemplateException nst) {
            logger.log(Level.WARNING, nst.getMessage(), nst);
        }
    }

    private static final String createTargName(String origName) {
        switch (Server.rand.nextInt(30)) {
            case 0: {
                return origName + " Unicorn One";
            }
            case 1: {
                return origName + " Deepwoods";
            }
            case 2: {
                return origName + " Goldbar";
            }
            case 3: {
                return origName + " Stinger";
            }
            case 4: {
                return origName + " Forefront";
            }
            case 5: {
                return origName + " Pike Hill";
            }
            case 6: {
                return origName + " Glory Day";
            }
            case 7: {
                return origName + " Silver Anchor";
            }
            case 8: {
                return origName + " Bloody Tip";
            }
            case 9: {
                return origName + " Goreplain";
            }
            case 10: {
                return origName + " Of The Bull";
            }
            case 11: {
                return origName + " Muddyknee";
            }
            case 12: {
                return origName + " First Fist";
            }
            case 13: {
                return origName + " Golden Day";
            }
            case 14: {
                return origName + " Stone Valley";
            }
            case 15: {
                return origName + " New Day";
            }
            case 16: {
                return origName + " Ramona Hill";
            }
            case 17: {
                return "The High " + origName;
            }
            case 18: {
                return "The " + origName + " of Spite";
            }
            case 19: {
                return "The Trolls " + origName;
            }
            case 20: {
                return "Diamond " + origName;
            }
            case 21: {
                return "Silver " + origName;
            }
            case 22: {
                return "Jackal's " + origName;
            }
            case 23: {
                return "Stonefort " + origName;
            }
            case 24: {
                return "Three rings " + origName;
            }
            case 25: {
                return "Fifteen Tears " + origName;
            }
            case 26: {
                return "Final Days " + origName;
            }
            case 27: {
                return "Victory " + origName;
            }
            case 28: {
                return "Cappa Cat " + origName;
            }
            case 29: {
                return "Headstrong " + origName;
            }
            case 30: {
                return "No Surrender " + origName;
            }
        }
        return "No Way Back " + origName;
    }

    public static final FaithZone[][] getFaithZones(boolean surfaced) {
        if (surfaced) {
            return surfaceDomains;
        }
        return caveDomains;
    }

    private static void addToDomains(Item item) {
        Deity deity;
        if (item.getData1() != 0 && (deity = item.getBless()) != null) {
            int maxy;
            int maxx;
            int miny;
            int tilex = item.getTileX();
            int tiley = item.getTileY();
            int ql = (int)(Servers.localServer.isChallengeServer() ? item.getCurrentQualityLevel() / 3.0f : item.getCurrentQualityLevel());
            int minx = Math.max(0, tilex - ql);
            FaithZone[] lCoveredFaithZones = Zones.getFaithZonesCoveredBy(minx, miny = Math.max(0, tiley - ql), maxx = Math.min(worldTileSizeX - 1, tilex + ql), maxy = Math.min(worldTileSizeY - 1, tiley + ql), item.isOnSurface());
            if (lCoveredFaithZones != null) {
                for (int x = 0; x < lCoveredFaithZones.length; ++x) {
                    int dist = Math.max(Math.abs(tilex - lCoveredFaithZones[x].getCenterX()), Math.abs(tiley - lCoveredFaithZones[x].getCenterY()));
                    if (100 - dist <= 0) continue;
                    lCoveredFaithZones[x].addToFaith(deity, Math.min(ql, 100 - dist));
                }
            }
        }
    }

    public static void fixTrees() {
        logger.log(Level.INFO, "Fixing trees.");
        int found = 0;
        MeshIO mesh = Server.surfaceMesh;
        Random random = new Random();
        int ms = Constants.meshSize;
        int max = 1 << ms;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                int tile = mesh.getTile(x, y);
                byte type = Tiles.decodeType(tile);
                byte data = Tiles.decodeData(tile);
                Tiles.Tile theTile = Tiles.getTile(type);
                if (type != Tiles.Tile.TILE_TREE.id && type != Tiles.Tile.TILE_BUSH.id && type != Tiles.Tile.TILE_ENCHANTED_TREE.id && type != Tiles.Tile.TILE_ENCHANTED_BUSH.id && type != Tiles.Tile.TILE_MYCELIUM_TREE.id && type != Tiles.Tile.TILE_MYCELIUM_BUSH.id) continue;
                ++found;
                byte newLen = (byte)(1 + random.nextInt(3));
                byte age = FoliageAge.getAgeAsByte(data);
                byte newData = Tiles.encodeTreeData(age, false, false, newLen);
                byte newType = type == Tiles.Tile.TILE_TREE.id ? theTile.getTreeType(data).asNormalTree() : (type == Tiles.Tile.TILE_ENCHANTED_TREE.id ? theTile.getTreeType(data).asEnchantedTree() : (type == Tiles.Tile.TILE_MYCELIUM_TREE.id ? theTile.getTreeType(data).asMyceliumTree() : (type == Tiles.Tile.TILE_BUSH.id ? theTile.getBushType(data).asNormalBush() : (type == Tiles.Tile.TILE_ENCHANTED_BUSH.id ? theTile.getBushType(data).asEnchantedBush() : theTile.getBushType(data).asMyceliumBush()))));
                mesh.setTile(x, y, Tiles.encode(Tiles.decodeHeight(tile), newType, newData));
            }
        }
        try {
            mesh.saveAll();
            logger.log(Level.INFO, "Set " + found + " trees to new type.");
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to fix trees", iox);
        }
        Constants.RUNBATCH = false;
    }

    public static void flash() {
        int tilex = Server.rand.nextInt(worldTileSizeX);
        int tiley = Server.rand.nextInt(worldTileSizeY);
        Zones.flash(tilex, tiley, true);
    }

    public static void flash(int tilex, int tiley, boolean doDamage) {
        VolaTile t;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Flashing tile at " + tilex + ", " + tiley + ", damage: " + doDamage);
        }
        int tile = Server.surfaceMesh.getTile(tilex, tiley);
        float height = Math.max(0.0f, Tiles.decodeHeightAsFloat(tile));
        Players.getInstance().weatherFlash(tilex, tiley, height);
        if (doDamage && (t = Zones.getTileOrNull(tilex, tiley, true)) != null) {
            t.flashStrike();
        }
    }

    public static void flashSpell(int tilex, int tiley, float baseDamage, Creature caster) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Flashing tile at " + tilex + ", " + tiley);
        }
        int tile = Server.surfaceMesh.getTile(tilex, tiley);
        float height = Math.max(0.0f, Tiles.decodeHeightAsFloat(tile));
        Players.getInstance().weatherFlash(tilex, tiley, height);
        VolaTile t = Zones.getTileOrNull(tilex, tiley, true);
        if (t != null) {
            t.lightningStrikeSpell(baseDamage, caster);
        }
    }

    public static final void fixCaveResources() {
        int ms = Constants.meshSize;
        boolean min = false;
        int max = 1 << ms;
        int count = 0;
        long now = System.nanoTime();
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                if (Server.getCaveResource(x, y) != 0) continue;
                ++count;
                Server.setCaveResource(x, y, 65535);
            }
        }
        try {
            Server.resourceMesh.saveAll();
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        logger.log(Level.INFO, "Fixed " + count + " cave resources. It took " + (float)(System.nanoTime() - now) / 1000000.0f + " ms.");
    }

    public static final void createInvestigatables() {
        int ms = Constants.meshSize;
        boolean min = false;
        int max = 1 << ms;
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                if (!(Server.rand.nextFloat() < 0.6f)) continue;
                Server.setInvestigatable(x, y, true);
            }
        }
    }

    public static final void createSeeds() {
        MeshIO mesh = Server.surfaceMesh;
        int ms = Constants.meshSize;
        boolean min = false;
        int max = 1 << ms;
        int count = 0;
        long now = System.nanoTime();
        for (int x = 0; x < max; ++x) {
            for (int y = 0; y < max; ++y) {
                int tile = mesh.getTile(x, y);
                byte tileType = Tiles.decodeType(tile);
                if (tileType == Tiles.Tile.TILE_GRASS.id || tileType == Tiles.Tile.TILE_MARSH.id || tileType == Tiles.Tile.TILE_MYCELIUM.id) {
                    if (Tiles.decodeHeight(tile) <= -20 || !TilePoller.checkForSeedGrowth(tile, x, y)) continue;
                    ++count;
                    continue;
                }
                if (tileType == Tiles.Tile.TILE_STEPPE.id || tileType == Tiles.Tile.TILE_TUNDRA.id || tileType == Tiles.Tile.TILE_MOSS.id || tileType == Tiles.Tile.TILE_PEAT.id) {
                    if (Tiles.decodeHeight(tile) <= 0 || !TilePoller.checkForSeedGrowth(tile, x, y)) continue;
                    ++count;
                    continue;
                }
                if (tileType == Tiles.Tile.TILE_TREE.id || tileType == Tiles.Tile.TILE_BUSH.id || tileType == Tiles.Tile.TILE_MYCELIUM_TREE.id || tileType == Tiles.Tile.TILE_MYCELIUM_BUSH.id) {
                    if (!TilePoller.checkForSeedGrowth(tile, x, y)) continue;
                    ++count;
                    continue;
                }
                if (tileType == Tiles.Tile.TILE_ROCK.id || tileType == Tiles.Tile.TILE_CLIFF.id) {
                    if (!TilePoller.checkCreateIronRock(x, y)) continue;
                    ++count;
                    continue;
                }
                if (!Tiles.isRoadType(tileType) && !Tiles.isEnchanted(tileType) && tileType != Tiles.Tile.TILE_DIRT_PACKED.id && tileType != Tiles.Tile.TILE_DIRT.id && tileType != Tiles.Tile.TILE_LAWN.id && tileType != Tiles.Tile.TILE_MYCELIUM_LAWN.id) continue;
                TilePoller.checkInvestigateGrowth(tile, x, y);
            }
        }
        float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
        logger.log(Level.INFO, "Created " + count + " seeds. It took " + lElapsedTime + " millis.");
    }

    public static final void addDuelRing(Item item) {
        duelRings.add(item);
    }

    public static final void removeDuelRing(Item item) {
        duelRings.remove(item);
    }

    public static final Item isWithinDuelRing(int tileX, int tileY, boolean surfaced) {
        if (!surfaced) {
            return null;
        }
        int maxDist = 20;
        for (Item ring : duelRings) {
            if (ring.getZoneId() <= 0 || ring.deleted || ring.getTileX() >= tileX + 20 || ring.getTileX() <= tileX - 20 || ring.getTileY() >= tileY + 20 || ring.getTileY() <= tileY - 20) continue;
            return ring;
        }
        return null;
    }

    public static boolean isTreeCapable(int x, int y, MeshIO mesh, int width, int tile) {
        if (Tiles.decodeHeight(tile) < 0) {
            return false;
        }
        if (Tiles.decodeType(tile) != Tiles.Tile.TILE_DIRT.id) {
            return false;
        }
        int neighborTrees = 0;
        for (int xx = x - 1; xx <= x + 1; ++xx) {
            for (int yy = y - 1; yy <= y + 1; ++yy) {
                int xxx = xx & width - 1;
                int yyy = yy & width - 1;
                int t = mesh.getTile(xxx, yyy);
                if (Tiles.decodeType(t) != Tiles.Tile.TILE_TREE.id) continue;
                ++neighborTrees;
            }
        }
        return neighborTrees < 5;
    }

    private static final void initializeDomains() {
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            for (int y = 0; y < domainSizeY; ++y) {
                for (int x = 0; x < domainSizeX; ++x) {
                    altarZones.add(null);
                }
            }
        } else {
            for (int x = 0; x < faithSizeX; ++x) {
                for (int y = 0; y < faithSizeY; ++y) {
                    Zones.surfaceDomains[x][y] = new FaithZone((short)(x * 8), (short)(y * 8), (short)(x * 8 + 7), (short)(y * 8 + 7));
                    Zones.caveDomains[x][y] = new FaithZone((short)(x * 8), (short)(y * 8), (short)(x * 8 + 7), (short)(y * 8 + 7));
                }
            }
            logger.log(Level.INFO, "Number of faithzones=" + faithSizeX * faithSizeX + " surfaced as well as underground.");
        }
    }

    public static final FaithZone getFaithZone(int tilex, int tiley, boolean surfaced) throws NoSuchZoneException {
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            FaithZone toReturn = null;
            HashMap<Item, FaithZone> thisZone = null;
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    int actualZone = Math.max(0, Math.min(tiley / 64 + j, domainSizeY - 1)) * domainSizeX + Math.max(0, Math.min(tilex / 64 + i, domainSizeX - 1));
                    if (actualZone >= altarZones.size() || (thisZone = altarZones.get(actualZone)) == null) continue;
                    for (FaithZone f : thisZone.values()) {
                        int rDist;
                        int fDist;
                        if (f.getCurrentRuler() == null || !f.containsTile(tilex, tiley) || f.getStrengthForTile(tilex, tiley, surfaced) <= 0) continue;
                        if (toReturn == null) {
                            toReturn = f;
                            continue;
                        }
                        if (toReturn.getStrengthForTile(tilex, tiley, surfaced) < f.getStrengthForTile(tilex, tiley, surfaced)) {
                            toReturn = f;
                            continue;
                        }
                        if (toReturn.getStrengthForTile(tilex, tiley, surfaced) != f.getStrengthForTile(tilex, tiley, surfaced) || (fDist = Math.min(Math.abs(f.getCenterX() - tilex), Math.abs(f.getCenterY() - tiley))) >= (rDist = Math.min(Math.abs(toReturn.getCenterX() - tilex), Math.abs(toReturn.getCenterY() - tiley)))) continue;
                        toReturn = f;
                    }
                }
            }
            return toReturn;
        }
        if (tilex < 0 || tilex >= worldTileSizeX || tiley < 0 || tiley >= worldTileSizeY) {
            throw new NoSuchZoneException("No faith zone at " + tilex + ", " + tiley);
        }
        if (surfaced) {
            return surfaceDomains[tilex >> 3][tiley >> 3];
        }
        return caveDomains[tilex >> 3][tiley >> 3];
    }

    private static boolean areaOverlapsFaithZone(FaithZone f, int startx, int starty, int endx, int endy) {
        for (int i = startx; i < endx; ++i) {
            for (int j = starty; j < endy; ++j) {
                if (!f.containsTile(i, j)) continue;
                return true;
            }
        }
        return false;
    }

    public static final ArrayList<HashMap<Item, FaithZone>> getCoveredZones(int startx, int starty, int endx, int endy) {
        ArrayList<HashMap<Item, FaithZone>> returnList = new ArrayList<HashMap<Item, FaithZone>>();
        for (int y = Math.min(0, starty / 64); y <= Math.min(0, endy / 64); ++y) {
            for (int x = Math.min(0, startx / 64); x <= Math.min(0, endx / 64); ++x) {
                HashMap<Item, FaithZone> thisZone = altarZones.get(y * domainSizeX + x);
                if (thisZone == null || returnList.contains(thisZone)) continue;
                returnList.add(thisZone);
            }
        }
        return returnList;
    }

    public static final FaithZone[] getFaithZonesCoveredBy(int startx, int starty, int endx, int endy, boolean surfaced) {
        HashSet<FaithZone> zoneList = new HashSet<FaithZone>();
        if (Features.Feature.NEWDOMAINS.isEnabled()) {
            ArrayList<HashMap<Item, FaithZone>> coveredZones = Zones.getCoveredZones(startx, starty, endx, endy);
            for (HashMap<Item, FaithZone> z : coveredZones) {
                for (FaithZone f : z.values()) {
                    if (f.getCurrentRuler() == null || !Zones.areaOverlapsFaithZone(f, startx, starty, endx, endy)) continue;
                    zoneList.add(f);
                }
            }
        } else {
            for (int x = startx >> 3; x <= endx >> 3; ++x) {
                for (int y = starty >> 3; y <= endy >> 3; ++y) {
                    FaithZone zone2 = surfaceDomains[x][y];
                    if (!surfaced) {
                        zone2 = caveDomains[x][y];
                    }
                    zoneList.add(zone2);
                }
            }
        }
        FaithZone[] toReturn = new FaithZone[zoneList.size()];
        return zoneList.toArray(toReturn);
    }

    public static final FaithZone[] getFaithZones() {
        ArrayList<FaithZone> allZones = new ArrayList<FaithZone>();
        for (HashMap<Item, FaithZone> z : altarZones) {
            if (z == null) continue;
            allZones.addAll(z.values());
        }
        FaithZone[] toReturn = new FaithZone[allZones.size()];
        return allZones.toArray(toReturn);
    }

    private static void createZones() throws IOException {
        int y;
        int x;
        logger.log(Level.INFO, "Creating zones: size is " + worldTileSizeX + ", " + worldTileSizeY);
        loading = true;
        long now = System.nanoTime();
        MESHSIZE = 1 << Constants.meshSize;
        Zones.initializeDomains();
        Zones.initializeHiveZones();
        int numz = 0;
        int ms = MESHSIZE >> 6;
        int zs = 6;
        int zsize = 64;
        boolean useDB = true;
        Zone[][] szones = new Zone[worldTileSizeX >> 6][worldTileSizeY >> 6];
        numberOfZones = szones.length * szones[0].length;
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Number of zones x=" + (worldTileSizeX >> 6) + ", total=" + numberOfZones);
            logger.fine("This should equal zones x=" + (MESHSIZE >> 6));
            logger.finer("Zone 54, 54=3456, 3519,3456,3519");
        }
        for (int x2 = 0; x2 < ms; ++x2) {
            for (int y2 = 0; y2 < ms; ++y2) {
                szones[x2][y2] = new DbZone(x2 << 6, (x2 << 6) + 64 - 1, y2 << 6, (y2 << 6) + 64 - 1, true);
                ++numz;
            }
        }
        logger.info("Initialised surface zones - array size: [" + ms + "][" + ms + "];");
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("This should equal number of zones: " + numz);
        }
        Zone[][] cZones = new Zone[worldTileSizeX >> 6][worldTileSizeY >> 6];
        for (x = 0; x < ms; ++x) {
            for (y = 0; y < ms; ++y) {
                cZones[x][y] = new DbZone(x << 6, (x << 6) + 64 - 1, y << 6, (y << 6) + 64 - 1, false);
            }
        }
        logger.info("Initialised cave zones - array size: [" + (worldTileSizeX >> 6) + "][" + (worldTileSizeY >> 6) + "];");
        logger.log(Level.INFO, "Seconds between polls=800, zonespolled=" + Zone.zonesPolled + ", maxnumberofzonespolled=" + Zone.maxZonesPolled);
        surfaceZones = szones;
        caveZones = cZones;
        logger.info("Loading surface and cave zone structures");
        Structures.loadAllStructures();
        numz = 0;
        for (x = 0; x < ms; ++x) {
            for (y = 0; y < ms; ++y) {
                surfaceZones[x][y].loadFences();
                caveZones[x][y].loadFences();
            }
        }
        logger.info("Loaded fences");
        DbStructure.loadBuildTiles();
        logger.info("Loaded build tiles");
        Structures.endLoadAll();
        logger.info("Ended loading of structures");
        for (x = 0; x < ms; ++x) {
            for (y = 0; y < ms; ++y) {
                ++numz;
                surfaceZones[x][y].loadAllItemsForZone();
                caveZones[x][y].loadAllItemsForZone();
            }
        }
        logger.info("Loaded zone items");
        logger.info("Loaded " + numz + " surface and cave zones");
        float mod = 40.0f;
        zonesPerRun = (int)((float)numberOfZones / 40.0f);
        rest = maxRest = (int)((float)numberOfZones - (float)zonesPerRun * 40.0f);
        loading = false;
        float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
        logger.log(Level.INFO, "Zones created and loaded. Number of zones=" + numberOfZones + ". PollFraction is " + rest + ". That took " + lElapsedTime + " millis.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeZones() {
        if (!Servers.localServer.LOGINSERVER) {
            File f = new File("zones" + Server.rand.nextInt(10000) + ".txt");
            BufferedWriter output = null;
            try {
                output = new BufferedWriter(new FileWriter(f));
                output.write("Legend:\n");
                output.write("No kingdom: 0\n");
                output.write("J/K: =\n");
                output.write("J/K village: v\n");
                output.write("J/K tower: t\n");
                output.write("J/K village+tower: V\n");
                output.write("HOTS: #\n");
                output.write("HOTS village: w\n");
                output.write("HOTS tower: g\n");
                output.write("HOTS village+tower: W\n\n");
                for (int x = 0; x < MESHSIZE >> 6; ++x) {
                    for (int y = 0; y < MESHSIZE >> 6; ++y) {
                        surfaceZones[x][y].write(output);
                    }
                    output.write("\n");
                    output.flush();
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                }
            }
        }
    }

    public static boolean containsVillage(int x, int y, boolean surfaced) {
        try {
            Zone zone = Zones.getZone(x, y, surfaced);
            return zone.containsVillage(x, y);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            return false;
        }
    }

    public static Village getVillage(@Nonnull TilePos tilePos, boolean surfaced) {
        return Zones.getVillage(tilePos.x, tilePos.y, surfaced);
    }

    public static Village getVillage(int x, int y, boolean surfaced) {
        try {
            Zone zone = Zones.getZone(x, y, surfaced);
            return zone.getVillage(x, y);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void pollNextZones(long sleepTime) throws IOException {
        int bonusZone;
        if (rest > 0) {
            bonusZone = 1;
            --rest;
        } else {
            bonusZone = 0;
        }
        Object object = ZONE_SYNC_LOCK;
        synchronized (object) {
            for (int x = 0; x < zonesPerRun + bonusZone; ++x) {
                Zone lCaveZone;
                Zone lSurfaceZone;
                if (currentPollZoneY == 1) {
                    Creatures.getInstance().pollAllCreatures(currentPollZoneX);
                }
                if (currentPollZoneX >= worldTileSizeX >> 6) {
                    currentPollZoneX = 0;
                    ++currentPollZoneY;
                }
                if (currentPollZoneY >= worldTileSizeY >> 6) {
                    currentPollZoneY = 0;
                    rest = maxRest;
                    if (pollnum > numberOfZones) {
                        pollnum = 0;
                    }
                    pollnum += 16;
                    Server.incrementCombatCounter();
                    Server.incrementSecondsUptime();
                    PlayerInfoFactory.pollPremiumPlayers();
                    FocusZone.pollAll();
                    if (Server.getCombatCounter() % 10 == 0) {
                        CombatHandler.resolveRound();
                    }
                    Players.getInstance().pollDeadPlayers();
                    Players.getInstance().pollKosWarnings();
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Pollnum=" + pollnum + ", max=" + numberOfZones + " cpzy=" + currentPollZoneY + " cpzx=" + currentPollZoneX);
                    }
                }
                if ((lSurfaceZone = surfaceZones[currentPollZoneX][currentPollZoneY]) != null && lSurfaceZone.isLoaded()) {
                    lSurfaceZone.poll(pollnum);
                }
                if ((lCaveZone = caveZones[currentPollZoneX][currentPollZoneY]) != null && lCaveZone.isLoaded()) {
                    lCaveZone.poll(numberOfZones + pollnum);
                }
                ++currentPollZoneX;
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Polled " + (zonesPerRun + bonusZone) + " zones to " + currentPollZoneX + ", " + currentPollZoneY);
        }
    }

    public static void saveAllZones() {
        int worldSize = 1 << Constants.meshSize;
        for (int x = 0; x < worldSize >> 6; ++x) {
            for (int y = 0; y < worldSize >> 6; ++y) {
                try {
                    if (surfaceZones[x][y].isLoaded()) {
                        surfaceZones[x][y].save();
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save surface zone " + x + ", " + y);
                }
                try {
                    if (!caveZones[x][y].isLoaded()) continue;
                    caveZones[x][y].save();
                    continue;
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save cave zone " + x + ", " + y);
                }
            }
        }
    }

    public static void saveNextZone() throws IOException {
        Zone lCaveSaveZone;
        Zone lSurfaceSaveZone;
        if (currentSaveZoneX >= worldTileSizeX >> 6) {
            currentSaveZoneX = 0;
            ++currentSaveZoneY;
        }
        if (currentSaveZoneY >= worldTileSizeY >> 6) {
            currentSaveZoneY = 0;
        }
        if ((lSurfaceSaveZone = surfaceZones[currentSaveZoneX][currentSaveZoneY]) != null && lSurfaceSaveZone.isLoaded()) {
            lSurfaceSaveZone.save();
        }
        if ((lCaveSaveZone = caveZones[currentSaveZoneX][currentSaveZoneY]) != null && lCaveSaveZone.isLoaded()) {
            lCaveSaveZone.save();
        }
        ++currentSaveZoneX;
    }

    public static VirtualZone createZone(Creature watcher, int startX, int startY, int centerX, int centerY, int size, boolean surface) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Creating virtual zone " + startX + ", " + startY + ", size: " + size + ", surface: " + surface + " for " + watcher.getName());
        }
        VirtualZone zone = new VirtualZone(watcher, startX, startY, centerX, centerY, size, surface);
        virtualZones.put(zone.getId(), zone);
        return zone;
    }

    public static void removeZone(int id) {
        virtualZones.remove(id);
    }

    static VirtualZone getVirtualZone(int number) throws NoSuchZoneException {
        VirtualZone toReturn = virtualZones.get(number);
        if (toReturn == null) {
            throw new NoSuchZoneException("No zone with number " + number);
        }
        return toReturn;
    }

    public static Zone getZone(int number) throws NoSuchZoneException {
        int y;
        int x;
        Zone toReturn = null;
        if (toReturn == null) {
            for (x = 0; x < worldTileSizeX >> 6; ++x) {
                for (y = 0; y < worldTileSizeY >> 6; ++y) {
                    if (surfaceZones[x][y].getId() != number) continue;
                    return surfaceZones[x][y];
                }
            }
        }
        if (toReturn == null) {
            for (x = 0; x < worldTileSizeX >> 6; ++x) {
                for (y = 0; y < worldTileSizeY >> 6; ++y) {
                    if (caveZones[x][y].getId() != number) continue;
                    return caveZones[x][y];
                }
            }
        }
        if (toReturn == null) {
            throw new NoSuchZoneException("No zone with number " + number);
        }
        return toReturn;
    }

    public static int getZoneIdFor(int x, int y, boolean surfaced) throws NoSuchZoneException {
        Zone toReturn = Zones.getZone(x, y, surfaced);
        return toReturn.getId();
    }

    public static Zone getZone(@Nonnull TilePos tilePos, boolean surfaced) throws NoSuchZoneException {
        return Zones.getZone(tilePos.x, tilePos.y, surfaced);
    }

    /*
     * Unable to fully structure code
     */
    public static Zone getZone(int tilex, int tiley, boolean surfaced) throws NoSuchZoneException {
        toReturn = null;
        if (surfaced) {
            try {
                toReturn = Zones.surfaceZones[tilex >> 6][tiley >> 6];
                if (toReturn.covers(tilex, tiley)) ** GOTO lbl15
                Zones.logger.log(Level.WARNING, "Error in the way zones are fetched. Doesn't work for " + (tilex >> 6) + " to cover " + tilex + " or " + (tiley >> 6) + " to cover " + tiley);
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new NoSuchZoneException("No such zone: x=" + (tilex >> 6) + ", y=" + (tiley >> 6), ex);
            }
        } else {
            try {
                toReturn = Zones.caveZones[tilex >> 6][tiley >> 6];
            }
            catch (Exception ex) {
                throw new NoSuchZoneException("No such cave zone: x=" + (tilex >> 6) + ", y=" + (tiley >> 6), ex);
            }
        }
lbl15:
        // 3 sources

        try {
            if (!toReturn.isLoaded()) {
                Zones.logger.log(Level.WARNING, "THIS SHOULD NOT HAPPEN - zone: x=" + (tilex >> 6) + ", y=" + (tiley >> 6) + " - surfaced: " + surfaced, new Exception());
                toReturn.load();
                toReturn.loadFences();
            }
        }
        catch (IOException ex) {
            Zones.logger.log(Level.WARNING, "Failed to load zone " + (tilex >> 6) + ", " + (tiley >> 6) + ". Zone will be empty.", ex);
        }
        return toReturn;
    }

    public static Zone[] getZonesCoveredBy(VirtualZone zone) {
        HashSet<Zone> zoneList = new HashSet<Zone>();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Getting zones covered by " + zone.getId() + ": " + zone.getStartX() + "," + zone.getEndX() + ",y:" + zone.getStartY() + "," + zone.getEndY() + " which starts at " + (zone.getStartX() >> 6) + "," + (zone.getStartY() >> 6) + " and ends at " + (zone.getEndX() >> 6) + "," + (zone.getEndY() >> 6));
        }
        for (int x = zone.getStartX() >> 6; x <= zone.getEndX() >> 6; ++x) {
            for (int y = zone.getStartY() >> 6; y <= zone.getEndY() >> 6; ++y) {
                try {
                    Zone zone2 = Zones.getZone(x << 6, y << 6, zone.isOnSurface());
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Adding " + zone2.getId() + ": " + zone2.getStartX() + "," + zone2.getStartY() + "-" + zone2.getEndX() + "," + zone2.getEndY() + " which is at " + x + "," + y);
                    }
                    zoneList.add(zone2);
                    continue;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
        }
        Zone[] toReturn = new Zone[zoneList.size()];
        return zoneList.toArray(toReturn);
    }

    public static final void checkAllSurfaceZones(Creature checker) {
        for (int x = 0; x < worldTileSizeX >> 6; ++x) {
            for (int y = 0; y < worldTileSizeY >> 6; ++y) {
                surfaceZones[x][y].checkIntegrity(checker);
            }
        }
    }

    public static final void checkAllCaveZones(Creature checker) {
        for (int x = 0; x < worldTileSizeX >> 6; ++x) {
            for (int y = 0; y < worldTileSizeY >> 6; ++y) {
                caveZones[x][y].checkIntegrity(checker);
            }
        }
    }

    public static Zone[] getZonesCoveredBy(int startx, int starty, int endx, int endy, boolean surfaced) {
        HashSet<Zone> zoneList = new HashSet<Zone>();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Getting zones covered by x: " + startx + "," + endx + ",y:" + starty + "," + endy + " which starts at " + (startx >> 6) + "," + (starty >> 6) + " and ends at " + (endx >> 6) + "," + (endy >> 6));
        }
        for (int x = startx >> 6; x <= endx >> 6; ++x) {
            for (int y = starty >> 6; y <= endy >> 6; ++y) {
                try {
                    Zone zone2 = Zones.getZone(x << 6, y << 6, surfaced);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Adding " + zone2.getId() + ": " + zone2.getStartX() + "," + zone2.getStartY() + "-" + zone2.getEndX() + "," + zone2.getEndY() + " which is at " + x + "," + y);
                    }
                    zoneList.add(zone2);
                    continue;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
        }
        Zone[] toReturn = new Zone[zoneList.size()];
        return zoneList.toArray(toReturn);
    }

    public static boolean isStructureFinished(Creature creature, Wall wall) {
        try {
            Structure struct = Structures.getStructure(wall.getStructureId());
            return struct.isFinished();
        }
        catch (NoSuchStructureException nss) {
            logger.log(Level.WARNING, "No structure for wall with id " + wall.getId());
            return false;
        }
    }

    public static int getTileIntForTile(int xTile, int yTile, int layer) {
        if (layer < 0) {
            return Server.caveMesh.getTile(Zones.safeTileX(xTile), Zones.safeTileY(yTile));
        }
        return Server.surfaceMesh.getTile(Zones.safeTileX(xTile), Zones.safeTileY(yTile));
    }

    public static byte getTextureForTile(int xTile, int yTile, int layer) {
        if (layer < 0) {
            if (xTile < 0 || xTile > worldTileSizeX || yTile < 0 || yTile > worldTileSizeY) {
                return Tiles.Tile.TILE_ROCK.id;
            }
            return Tiles.decodeType(Server.caveMesh.getTile(xTile, yTile));
        }
        if (xTile < 0 || xTile > worldTileSizeX || yTile < 0 || yTile > worldTileSizeY) {
            return Tiles.Tile.TILE_DIRT.id;
        }
        return Tiles.decodeType(Server.surfaceMesh.getTile(xTile, yTile));
    }

    private static final double getDir(float x, float y) {
        double degree = 1.0;
        if (x != 0.0f) {
            if (y != 0.0f) {
                degree = Math.atan2(y, x);
                for (degree = degree * 57.29577951308232 + 90.0; degree < 0.0; degree += 360.0) {
                }
                while (degree >= 360.0) {
                    degree -= 360.0;
                }
            } else {
                degree = x < 0.0f ? 270.0 : 90.0;
            }
        } else if (y != 0.0f) {
            degree = y > 0.0f ? 180.0 : 360.0;
        }
        return degree;
    }

    public static int getTileZoneFor(float posX, float posY, int tilex, int tiley) {
        float xa = posX - (float)(tilex * 4);
        float ya = posY - (float)(tiley * 4);
        double rot = Zones.getDir(xa -= 2.0f, ya -= 2.0f);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Rot to " + xa + ", " + ya + " is " + rot);
        }
        if (rot <= 45.0) {
            return 0;
        }
        if (rot <= 90.0) {
            return 1;
        }
        if (rot <= 135.0) {
            return 2;
        }
        if (rot <= 180.0) {
            return 3;
        }
        if (rot <= 225.0) {
            return 4;
        }
        if (rot <= 270.0) {
            return 5;
        }
        if (rot <= 315.0) {
            return 6;
        }
        return 7;
    }

    public static final float calculateRockHeight(float posX, float posY) throws NoSuchZoneException {
        float height = 0.0f;
        try {
            int tilex = (int)posX >> 2;
            int tiley = (int)posY >> 2;
            MeshIO mesh = Server.rockMesh;
            int[] meshData = mesh.data;
            float xa = posX / 4.0f - (float)tilex;
            float ya = posY / 4.0f - (float)tiley;
            if (xa > ya) {
                if (ya >= 0.999f) {
                    ya = 0.999f;
                }
                xa -= ya;
                if ((xa /= 1.0f - ya) < 0.0f) {
                    xa = 0.0f;
                }
                if (xa > 1.0f) {
                    xa = 1.0f;
                }
                float xheight1 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley << Constants.meshSize]) * (1.0f - xa) + Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley << Constants.meshSize]) * xa;
                float xheight2 = Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley + 1 << Constants.meshSize]);
                height = xheight1 * (1.0f - ya) + xheight2 * ya;
            } else {
                if (ya <= 0.001f) {
                    ya = 0.001f;
                }
                if ((xa /= ya) < 0.0f) {
                    xa = 0.0f;
                }
                if (xa > 1.0f) {
                    xa = 1.0f;
                }
                float xheight1 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley << Constants.meshSize]);
                float xheight2 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley + 1 << Constants.meshSize]) * (1.0f - xa) + Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley + 1 << Constants.meshSize]) * xa;
                height = xheight1 * (1.0f - ya) + xheight2 * ya;
            }
        }
        catch (Exception ex) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "No such zone at " + posX + ", " + posY, ex);
            }
            throw new NoSuchZoneException("No such zone", ex);
        }
        return height;
    }

    public static final float calculateHeight(float posX, float posY, boolean surfaced) throws NoSuchZoneException {
        float height = 0.0f;
        try {
            int tilex = (int)posX >> 2;
            int tiley = (int)posY >> 2;
            MeshIO mesh = Server.surfaceMesh;
            if (!surfaced) {
                mesh = Server.caveMesh;
            } else if (Tiles.decodeType(mesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_HOLE.id) {
                mesh = Server.caveMesh;
            }
            int[] meshData = mesh.data;
            float xa = posX / 4.0f - (float)tilex;
            float ya = posY / 4.0f - (float)tiley;
            if (xa > ya) {
                if (ya >= 0.999f) {
                    ya = 0.999f;
                }
                xa -= ya;
                if ((xa /= 1.0f - ya) < 0.0f) {
                    xa = 0.0f;
                }
                if (xa > 1.0f) {
                    xa = 1.0f;
                }
                float xheight1 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley << Constants.meshSize]) * (1.0f - xa) + Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley << Constants.meshSize]) * xa;
                float xheight2 = Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley + 1 << Constants.meshSize]);
                height = xheight1 * (1.0f - ya) + xheight2 * ya;
            } else {
                if (ya <= 0.001f) {
                    ya = 0.001f;
                }
                if ((xa /= ya) < 0.0f) {
                    xa = 0.0f;
                }
                if (xa > 1.0f) {
                    xa = 1.0f;
                }
                float xheight1 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley << Constants.meshSize]);
                float xheight2 = Tiles.decodeHeightAsFloat(meshData[tilex | tiley + 1 << Constants.meshSize]) * (1.0f - xa) + Tiles.decodeHeightAsFloat(meshData[tilex + 1 | tiley + 1 << Constants.meshSize]) * xa;
                height = xheight1 * (1.0f - ya) + xheight2 * ya;
            }
        }
        catch (Exception ex) {
            if (logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "No such zone at " + posX + ", " + posY, ex);
            }
            throw new NoSuchZoneException("No such zone", ex);
        }
        return height;
    }

    public static final float calculatePosZ(float posx, float posy, VolaTile tile, boolean isOnSurface, boolean floating, float currentPosZ, @Nullable Creature creature, long bridgeId) {
        try {
            float basePosZ = Zones.calculateHeight(posx, posy, isOnSurface);
            if (bridgeId > 0L) {
                int xx = (int)StrictMath.floor(posx / 4.0f);
                int yy = (int)StrictMath.floor(posy / 4.0f);
                float xa = posx / 4.0f - (float)xx;
                float ya = posy / 4.0f - (float)yy;
                float[] hts = Zones.getNodeHeights(xx, yy, isOnSurface ? 0 : -1, bridgeId);
                return hts[0] * (1.0f - xa) * (1.0f - ya) + hts[1] * xa * (1.0f - ya) + hts[2] * (1.0f - xa) * ya + hts[3] * xa * ya;
            }
            int currentFloorLevel = 0;
            if (creature != null) {
                currentFloorLevel = creature.getFloorLevel();
            } else if (currentPosZ > basePosZ) {
                currentFloorLevel = (int)(currentPosZ - basePosZ) / 3;
            }
            if (tile != null) {
                int dfl = tile.getDropFloorLevel(currentFloorLevel);
                Floor[] f = tile.getFloors(dfl * 30, dfl * 30);
                float h = basePosZ + (float)Math.max(0, dfl * 3) + (f.length > 0 ? 0.25f : 0.0f);
                return floating ? Math.max(0.0f + (creature != null ? creature.getTemplate().offZ : 0.0f), h) : h;
            }
            VolaTile ttile = Zones.getTileOrNull((int)posx / 4, (int)posy / 4, isOnSurface);
            if (ttile != null) {
                int dfl = ttile.getDropFloorLevel(currentFloorLevel);
                Floor[] f = ttile.getFloors(dfl * 30, dfl * 30);
                float h = basePosZ + (float)Math.max(0, dfl * 3) + (f.length > 0 ? 0.25f : 0.0f);
                return floating ? Math.max(0.0f + (creature != null ? creature.getTemplate().offZ : 0.0f), h) : h;
            }
            if (floating) {
                return Math.max(0.0f + (creature != null ? creature.getTemplate().offZ : 0.0f), basePosZ);
            }
            return basePosZ;
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "No Zone for tile " + posx + ", " + posy + " " + isOnSurface, new Exception());
            return currentPosZ;
        }
    }

    public static final float[] getNodeHeights(int xNode, int yNode, int layer, long bridgeId) {
        VolaTile vt;
        float NW = 0.0f;
        float NE = 0.0f;
        float SW = 0.0f;
        float SE = 0.0f;
        if (bridgeId > 0L && (vt = Zones.getTileOrNull(xNode, yNode, layer == 0)) != null) {
            for (BridgePart bp : vt.getBridgeParts()) {
                if (bp.getStructureId() != bridgeId || bp.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) continue;
                NW = (float)bp.getHeightOffset() / 10.0f;
                SE = (float)(bp.getHeightOffset() + bp.getSlope()) / 10.0f;
                if (bp.getDir() == 0 || bp.getDir() == 4) {
                    NE = NW;
                    SW = SE;
                } else {
                    NE = SE;
                    SW = NW;
                }
                return new float[]{NW, NE, SW, SE};
            }
        }
        NW = Zones.getHeightForNode(xNode, yNode, layer);
        NE = Zones.getHeightForNode(xNode + 1, yNode, layer);
        SW = Zones.getHeightForNode(xNode, yNode + 1, layer);
        SE = Zones.getHeightForNode(xNode + 1, yNode + 1, layer);
        return new float[]{NW, NE, SW, SE};
    }

    public static final float getHeightForNode(int xNode, int yNode, int layer) {
        MeshIO mesh = Server.surfaceMesh;
        if (layer < 0) {
            mesh = Server.caveMesh;
        }
        if (xNode < 0 || xNode >= 1 << Constants.meshSize) {
            xNode = 0;
        }
        if (yNode < 0 || yNode >= 1 << Constants.meshSize) {
            yNode = 0;
        }
        return Tiles.decodeHeightAsFloat(mesh.getTile(xNode, yNode));
    }

    public static VolaTile getTileOrNull(@Nonnull TilePos tilePos, boolean surfaced) {
        return Zones.getTileOrNull(tilePos.x, tilePos.y, surfaced);
    }

    public static VolaTile getTileOrNull(int tilex, int tiley, boolean surfaced) {
        VolaTile tile;
        block3: {
            tile = null;
            try {
                Zone zone = Zones.getZone(tilex, tiley, surfaced);
                tile = zone.getTileOrNull(tilex, tiley);
            }
            catch (NoSuchZoneException nsz) {
                if (haslogged) break block3;
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "HERE _ No such zone: " + tilex + ", " + tiley + " surf=" + surfaced, nsz);
                }
                haslogged = true;
            }
        }
        return tile;
    }

    public static VolaTile getOrCreateTile(@Nonnull TilePos tilePos, boolean surfaced) {
        return Zones.getOrCreateTile(tilePos.x, tilePos.y, surfaced);
    }

    public static VolaTile getOrCreateTile(int tilex, int tiley, boolean surfaced) {
        VolaTile tile = null;
        try {
            Zone zone = Zones.getZone(tilex, tiley, surfaced);
            tile = zone.getOrCreateTile(tilex, tiley);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
        return tile;
    }

    public static VolaTile[] getTilesSurrounding(int tilex, int tiley, boolean surfaced, int distance) {
        HashSet<VolaTile> tiles = new HashSet<VolaTile>();
        for (int x = -distance; x <= distance; ++x) {
            for (int y = -distance; y <= distance; ++y) {
                VolaTile tile = Zones.getTileOrNull(tilex + x, tiley + y, surfaced);
                if (tile == null) continue;
                tiles.add(tile);
            }
        }
        return tiles.toArray(new VolaTile[tiles.size()]);
    }

    public static final boolean isNoBuildZone(int tilex, int tiley) {
        return FocusZone.isNoBuildZoneAt(tilex, tiley);
    }

    public static final boolean isPremSpawnZoneAt(int tilex, int tiley) {
        if (Servers.localServer.isChaosServer()) {
            Village v = Villages.getVillage(tilex, tiley, true);
            return v == null || !v.isPermanent;
        }
        return FocusZone.isPremSpawnOnlyZoneAt(tilex, tiley);
    }

    public static final boolean isInPvPZone(int tilex, int tiley) {
        if (FocusZone.isNonPvPZoneAt(tilex, tiley)) {
            return false;
        }
        if (FocusZone.isPvPZoneAt(tilex, tiley)) {
            return true;
        }
        return Zones.isWithinDuelRing(tilex, tiley, true) != null;
    }

    public static final boolean isOnPvPServer(@Nonnull TilePos tilePos) {
        return Zones.isOnPvPServer(tilePos.x, tilePos.y);
    }

    public static final boolean isOnPvPServer(int tilex, int tiley) {
        if (Servers.localServer.PVPSERVER) {
            return !FocusZone.isNonPvPZoneAt(tilex, tiley);
        }
        if (FocusZone.isPvPZoneAt(tilex, tiley)) {
            return true;
        }
        return Zones.isWithinDuelRing(tilex, tiley, true) != null;
    }

    public static final boolean willEnterStructure(Creature creature, float startx, float starty, float endx, float endy, boolean surfaced) {
        int starttilex = (int)startx >> 2;
        int starttiley = (int)starty >> 2;
        int endtilex = (int)endx >> 2;
        int endtiley = (int)endy >> 2;
        int max = 100;
        if (creature != null) {
            max = creature.getMaxHuntDistance() + 5;
        }
        if (Math.abs(endtilex - starttilex) > max || Math.abs(endtiley - starttiley) > max) {
            if (creature != null) {
                logger.log(Level.WARNING, creature.getName() + " checking more than his maxdist of " + creature.getMaxHuntDistance(), new Exception());
            } else {
                logger.log(Level.WARNING, "Too far: " + starttilex + "," + starttiley + "->" + endtilex + ", " + endtiley, new Exception());
            }
            return true;
        }
        if (starttilex == endtilex && starttiley == endtiley) {
            return false;
        }
        double rot = Zones.getRotation(startx, starty, endx, endy);
        double xPosMod = Math.sin(rot * 0.01745329238474369) * 2.0;
        double yPosMod = -Math.cos(rot * 0.01745329238474369) * 2.0;
        double currX = startx;
        double currY = starty;
        int currTileX = starttilex;
        int currTileY = starttiley;
        int lastTileX = starttilex;
        int lastTileY = starttiley;
        boolean found = false;
        while (true) {
            if (Math.abs(endtilex - currTileX) <= 1 && Math.abs(endtiley - currTileY) <= 1) {
                VolaTile startTile = Zones.getTileOrNull(currTileX, currTileY, surfaced);
                VolaTile endTile = Zones.getTileOrNull(endtilex, endtiley, surfaced);
                return startTile != null ? endTile != null && startTile.getStructure() == null && endTile.getStructure() != null && endTile.getStructure().isFinished() : endTile != null && endTile.getStructure() != null && endTile.getStructure().isFinished();
            }
            currTileX = (int)(currX += xPosMod) >> 2;
            currTileY = (int)(currY += yPosMod) >> 2;
            if (Math.abs(currTileX - starttilex) > max || Math.abs(currTileY - starttiley) > max) {
                if (creature != null) {
                    logger.log(Level.WARNING, creature.getName() + " missed target " + creature.getMaxHuntDistance(), new Exception());
                } else {
                    logger.log(Level.WARNING, "missed target : " + starttilex + "," + starttiley + "->" + endtilex + ", " + endtiley, new Exception());
                }
                return true;
            }
            int diffX = currTileX - lastTileX;
            int diffY = currTileY - lastTileY;
            if (diffX == 0 && diffY == 0) continue;
            VolaTile startTile = Zones.getTileOrNull(lastTileX, lastTileY, surfaced);
            VolaTile endTile = Zones.getTileOrNull(currTileX, currTileY, surfaced);
            if (startTile != null ? endTile != null && startTile.getStructure() == null && endTile.getStructure() != null && endTile.getStructure().isFinished() : endTile != null && endTile.getStructure() != null && endTile.getStructure().isFinished()) {
                return true;
            }
            lastTileY = currTileY;
            lastTileX = currTileX;
        }
    }

    @Nullable
    public static final Floor[] getFloorsAtTile(int tilex, int tiley, int startHeightOffset, int endHeightOffset, int layer) {
        return Zones.getFloorsAtTile(tilex, tiley, startHeightOffset, endHeightOffset, layer != -1);
    }

    @Nullable
    public static final Floor[] getFloorsAtTile(int tilex, int tiley, int startHeightOffset, int endHeightOffset, boolean onSurface) {
        Floor[] floors;
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, onSurface);
        if (tile != null && (floors = tile.getFloors(startHeightOffset, endHeightOffset)) != null && floors.length > 0) {
            return floors;
        }
        return null;
    }

    public static final Floor getFloor(int tilex, int tiley, boolean onSurface, int floorLevel) {
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, onSurface);
        if (tile != null) {
            return tile.getFloor(floorLevel);
        }
        return null;
    }

    public static final BridgePart[] getBridgePartsAtTile(int tilex, int tiley, boolean onSurface) {
        BridgePart[] bridgeParts;
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, onSurface);
        if (tile != null && (bridgeParts = tile.getBridgeParts()) != null && bridgeParts.length > 0) {
            return bridgeParts;
        }
        return null;
    }

    @Nullable
    public static BridgePart getBridgePartFor(int tilex, int tiley, boolean onSurface) {
        BridgePart[] bps = Zones.getBridgePartsAtTile(tilex, tiley, onSurface);
        if (bps != null && bps.length > 0) {
            return bps[0];
        }
        return null;
    }

    public static Structure[] getStructuresInArea(int startX, int startY, int endX, int endY, boolean surfaced) {
        HashSet<Structure> set = new HashSet<Structure>();
        for (int x = startX; x <= endX; ++x) {
            for (int y = startY; y <= endY; ++y) {
                Structure structure;
                VolaTile tile = Zones.getTileOrNull(x, y, surfaced);
                if (tile == null || (structure = tile.getStructure()) == null || set.contains(structure) || !structure.isTypeHouse()) continue;
                set.add(structure);
            }
        }
        Structure[] toReturn = set.size() > 0 ? set.toArray(new Structure[set.size()]) : new Structure[]{};
        return toReturn;
    }

    private static final double getRotation(float startx, float starty, float endx, float endy) {
        double newrot = Math.atan2(endy - starty, endx - startx);
        return newrot * 57.29577951308232 + 90.0;
    }

    public static final LongPosition getEndTile(float startPosX, float startPosY, float rot, int tiledist) {
        float xPosMod = (float)Math.sin(rot * ((float)Math.PI / 180)) * (float)(4 * tiledist);
        float yPosMod = -((float)Math.cos(rot * ((float)Math.PI / 180))) * (float)(4 * tiledist);
        float newPosX = startPosX + xPosMod;
        float newPosY = startPosY + yPosMod;
        return new LongPosition(0L, (int)newPosX >> 2, (int)newPosY >> 2);
    }

    static final Den getNorthTop(int templateId) {
        for (int tries = 0; tries < 1000; ++tries) {
            VolaTile t;
            boolean startX = false;
            boolean startY = false;
            int endX = worldTileSizeX - 10;
            int endY = Math.min(worldTileSizeY / 10, 500);
            Zone[] zones = Zones.getZonesCoveredBy(0, 0, endX, endY, true);
            Zone highest = null;
            float top = 0.0f;
            for (int x = 0; x < zones.length; ++x) {
                if (!((float)zones[x].highest > top) || top != 0.0f && Server.rand.nextInt(2) != 0) continue;
                top = zones[x].highest;
                highest = zones[x];
            }
            if (highest == null) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = highest.startX; x <= highest.endX; ++x) {
                for (int y = highest.startY; y < highest.endY; ++y) {
                    int tile = mesh.getTile(x, y);
                    short hi = Tiles.decodeHeight(tile);
                    if (hi <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 0 || (t = Zones.getTileOrNull(Zones.safeTileX(tx), Zones.safeTileY(ty), true)) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx), Zones.safeTileY(ty), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx - 20), Zones.safeTileY(ty - 20), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx - 20), Zones.safeTileY(ty), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx + 20), Zones.safeTileY(ty), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx), Zones.safeTileY(ty), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx + 20), Zones.safeTileY(ty - 20), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx + 20), Zones.safeTileY(ty + 20), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx), Zones.safeTileY(ty + 20), true) != null || Villages.getVillageWithPerimeterAt(Zones.safeTileX(tx - 20), Zones.safeTileY(ty + 20), true) != null) continue;
            logger.log(Level.INFO, "Created den for " + templateId + " after " + tries + " tries.");
            return new Den(templateId, tx, ty, true);
        }
        return null;
    }

    static final Den getWestTop(int templateId) {
        for (int a = 0; a < 100; ++a) {
            boolean startX = false;
            int startY = Math.min(worldTileSizeY / 10, 500);
            int endX = (worldTileSizeX - 10) / 2;
            int endY = worldTileSizeY - Math.min(worldTileSizeY / 10, 500);
            Zone[] zones = Zones.getZonesCoveredBy(0, startY, endX, endY, true);
            Zone highest = null;
            float top = 0.0f;
            for (int x = 0; x < zones.length; ++x) {
                if (!((float)zones[x].highest > top) || top != 0.0f && Server.rand.nextInt(2) != 0) continue;
                top = zones[x].highest;
                highest = zones[x];
            }
            if (highest == null) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = highest.startX; x <= highest.endX; ++x) {
                for (int y = highest.startY; y < highest.endY; ++y) {
                    int tile;
                    short hi;
                    Village v = Villages.getVillageWithPerimeterAt(x, y, true);
                    if (v != null || (hi = Tiles.decodeHeight(tile = mesh.getTile(x, y))) <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 0) continue;
            return new Den(templateId, tx, ty, true);
        }
        return null;
    }

    static final Den getSouthTop(int templateId) {
        for (int a = 0; a < 100; ++a) {
            boolean startX = false;
            int startY = worldTileSizeY - Math.min(worldTileSizeY / 10, 500);
            int endX = worldTileSizeX - 10;
            int endY = worldTileSizeY - 10;
            Zone[] zones = Zones.getZonesCoveredBy(0, startY, endX, endY, true);
            Zone highest = null;
            float top = 0.0f;
            for (int x = 0; x < zones.length; ++x) {
                if (!((float)zones[x].highest > top) || top != 0.0f && Server.rand.nextInt(2) != 0) continue;
                top = zones[x].highest;
                highest = zones[x];
            }
            if (highest == null) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = highest.startX; x <= highest.endX; ++x) {
                for (int y = highest.startY; y < highest.endY; ++y) {
                    int tile;
                    short hi;
                    Village v = Villages.getVillageWithPerimeterAt(x, y, true);
                    if (v != null || (hi = Tiles.decodeHeight(tile = mesh.getTile(x, y))) <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 0) continue;
            return new Den(templateId, tx, ty, true);
        }
        return null;
    }

    public static final TilePos getRandomCenterLand() {
        return TilePos.fromXY(worldTileSizeX / 4 + Server.rand.nextInt(worldTileSizeX / 2), worldTileSizeY / 4 + Server.rand.nextInt(worldTileSizeY / 2));
    }

    public static final Den getRandomTop() {
        for (int a = 0; a < 100; ++a) {
            int minHeight = 200;
            int startx = Server.rand.nextInt(worldTileSizeX >> 6);
            int starty = Server.rand.nextInt(worldTileSizeY >> 6);
            int endx = Math.min(startx + 2, worldTileSizeX >> 6);
            int endy = Math.min(starty + 2, worldTileSizeY >> 6);
            Zone highest = null;
            float top = 0.0f;
            for (int x = startx; x < endx; ++x) {
                for (int y = starty; y < endy; ++y) {
                    if (!((float)Zones.surfaceZones[x][y].highest > top)) continue;
                    logger.info("Zone " + x + "," + y + " now highest for top.");
                    top = Zones.surfaceZones[x][y].highest;
                    if (!(top > 200.0f)) continue;
                    highest = surfaceZones[x][y];
                }
            }
            if (highest == null) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = highest.startX; x <= highest.endX; ++x) {
                for (int y = highest.startY; y < highest.endY; ++y) {
                    int tile;
                    short hi;
                    Village v = Villages.getVillageWithPerimeterAt(x, y, true);
                    if (v != null || (hi = Tiles.decodeHeight(tile = mesh.getTile(x, y))) <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 200) continue;
            return new Den(0, tx, ty, true);
        }
        return null;
    }

    static final Den getEastTop(int templateId) {
        for (int a = 0; a < 100; ++a) {
            int startX = (worldTileSizeX - 5) / 2;
            int startY = Math.min(worldTileSizeY / 10, 500);
            int endX = worldTileSizeX - 10;
            int endY = worldTileSizeY - Math.min(worldTileSizeY / 10, 500);
            Zone[] zones = Zones.getZonesCoveredBy(startX, startY, endX, endY, true);
            Zone highest = null;
            float top = 0.0f;
            for (int x = 0; x < zones.length; ++x) {
                if (!((float)zones[x].highest > top) || top != 0.0f && Server.rand.nextInt(2) != 0) continue;
                top = zones[x].highest;
                highest = zones[x];
            }
            if (highest == null) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = highest.startX; x <= highest.endX; ++x) {
                for (int y = highest.startY; y < highest.endY; ++y) {
                    int tile;
                    short hi;
                    Village v = Villages.getVillageWithPerimeterAt(x, y, true);
                    if (v != null || (hi = Tiles.decodeHeight(tile = mesh.getTile(x, y))) <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 0) continue;
            return new Den(templateId, tx, ty, true);
        }
        return null;
    }

    static final Den getRandomForest(int templateId) {
        for (int a = 0; a < 100; ++a) {
            int num1 = Server.rand.nextInt(surfaceZones.length);
            int num2 = Server.rand.nextInt(surfaceZones.length);
            Zone zone = surfaceZones[num1][num2];
            if (!zone.isForest) continue;
            int tx = 0;
            int ty = 0;
            short h = 0;
            MeshIO mesh = Server.surfaceMesh;
            for (int x = zone.startX; x <= zone.endX; ++x) {
                for (int y = zone.startY; y < zone.endY; ++y) {
                    int tile;
                    short hi;
                    Village v = Villages.getVillageWithPerimeterAt(x, y, true);
                    if (v != null || (hi = Tiles.decodeHeight(tile = mesh.getTile(x, y))) <= h) continue;
                    h = hi;
                    tx = x;
                    ty = y;
                }
            }
            if (h <= 0) continue;
            return new Den(templateId, tx, ty, true);
        }
        return null;
    }

    public static final void releaseAllCorpsesFor(Creature creature) {
        int itx;
        Item[] its;
        int y;
        int x;
        int worldSize = 1 << Constants.meshSize;
        for (x = 0; x < worldSize >> 6; ++x) {
            for (y = 0; y < worldSize >> 6; ++y) {
                its = surfaceZones[x][y].getAllItems();
                for (itx = 0; itx < its.length; ++itx) {
                    if (its[itx].getTemplateId() != 272 || !its[itx].getName().equals("corpse of " + creature.getName())) continue;
                    its[itx].setProtected(false);
                }
            }
        }
        for (x = 0; x < worldSize >> 6; ++x) {
            for (y = 0; y < worldSize >> 6; ++y) {
                its = caveZones[x][y].getAllItems();
                for (itx = 0; itx < its.length; ++itx) {
                    if (its[itx].getTemplateId() != 272 || !its[itx].getName().equals("corpse of " + creature.getName())) continue;
                    its[itx].setProtected(false);
                }
            }
        }
    }

    public static final byte getMiningState(int x, int y) {
        Map<Integer, Byte> tab = miningTiles.get(x);
        if (tab == null) {
            return 0;
        }
        Byte b = tab.get(y);
        if (b == null) {
            return 0;
        }
        return b;
    }

    public static final void setMiningState(int x, int y, byte state, boolean load) {
        Map<Integer, Byte> tab = miningTiles.get(x);
        boolean save = false;
        if (tab == null) {
            if (state > 0 || state == -1) {
                tab = new Hashtable<Integer, Byte>();
                tab.put(y, state);
                miningTiles.put(x, tab);
                save = true;
            }
        } else if (state > 0 || state == -1 || tab.get(y) != null) {
            tab.put(y, state);
            save = true;
        }
        if (!load && save) {
            Zones.saveMiningTile(x, y, state);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void loadAllMiningTiles() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            long start = System.nanoTime();
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_ALL_MININGTILES);
            rs = ps.executeQuery();
            int a = 0;
            while (rs.next()) {
                Zones.setMiningState(rs.getInt("TILEX"), rs.getInt("TILEY"), rs.getByte("STATE"), true);
                ++a;
            }
            logger.log(Level.INFO, "Loaded " + a + " mining tiles, that took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load miningtiles", sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void saveMiningTile(int tilex, int tiley, byte state) {
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            dbcon = DbConnector.getZonesDbCon();
            if (Zones.exists(dbcon, tilex, tiley)) break block5;
            Zones.createMiningTile(dbcon, tilex, tiley, state);
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return;
        }
        try {
            ps = dbcon.prepareStatement(UPDATE_MININGTILE);
            ps.setByte(1, state);
            ps.setInt(2, tilex);
            ps.setInt(3, tiley);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save miningtile " + tilex + ", " + tiley + ", " + state + ":" + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final void createMiningTile(Connection dbcon, int tilex, int tiley, byte state) {
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement(INSERT_MININGTILE);
            ps.setByte(1, state);
            ps.setInt(2, tilex);
            ps.setInt(3, tiley);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Failed to save miningtile " + tilex + ", " + tiley + ", " + state + ":" + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void deleteMiningTile(int tilex, int tiley) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(DELETE_MININGTILE);
            ps.setInt(1, tilex);
            ps.setInt(2, tiley);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to delete miningtile " + tilex + ", " + tiley + ":" + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static final boolean exists(Connection dbcon, int tilex, int tiley) throws SQLException {
        boolean bl;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement(GET_MININGTILE);
            ps.setInt(1, tilex);
            ps.setInt(2, tiley);
            rs = ps.executeQuery();
            bl = rs.next();
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        return bl;
    }

    private static final void spawnSanta(byte kingdom) {
        Village v = Villages.getFirstPermanentVillageForKingdom(kingdom);
        if (v == null) {
            v = Villages.getCapital(kingdom);
        }
        if (v == null) {
            v = Villages.getFirstVillageForKingdom(kingdom);
        }
        int tilex = v.startx + (v.endx - v.startx) / 2;
        int tiley = v.starty + (v.endy - v.starty) / 2;
        if (v != null) {
            try {
                tilex = v.getToken().getTileX();
                tiley = v.getToken().getTileY();
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
        int temp = 46;
        String name = "Santa Claus";
        Kingdom k = Kingdoms.getKingdom(kingdom);
        if (k != null && k.getTemplate() == 3) {
            temp = 47;
            name = "Evil Santa";
        }
        try {
            evilsanta = Creature.doNew(temp, tilex * 4 + 1, (float)(tiley * 4 + 1), 154.0f, 0, name, (byte)0, kingdom);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to create santa! " + ex.getMessage(), ex);
        }
        try {
            ItemFactory.createItem(442, 90.0f, tilex * 4, (tiley + 1) * 4, 154.0f, true, (byte)0, -10L, null);
            Zones.sendChristmasEffect(evilsanta, null);
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to create evil julbord " + ex.getMessage(), ex);
        }
    }

    public static final void loadChristmas() {
        if (!Zones.isHasLoadedChristmas()) {
            Server.getInstance().broadCastSafe("Merry Christmas!");
            if (!Servers.localServer.HOMESERVER) {
                int tilex = Servers.localServer.SPAWNPOINTLIBX - 2;
                int tiley = Servers.localServer.SPAWNPOINTLIBY - 2;
                Village v = Villages.getFirstPermanentVillageForKingdom((byte)3);
                if (v == null) {
                    v = Villages.getFirstVillageForKingdom((byte)3);
                }
                evilsanta = Zones.addSanta(v, tilex, tiley, 154, 47, "Evil Santa", (byte)3);
                tilex = Servers.localServer.SPAWNPOINTJENNX - 2;
                tiley = Servers.localServer.SPAWNPOINTJENNY - 2;
                v = Villages.getFirstPermanentVillageForKingdom((byte)1);
                if (v == null) {
                    v = Villages.getFirstVillageForKingdom((byte)1);
                }
                santa = Zones.addSanta(v, tilex, tiley, 154, 46, "Santa Claus", (byte)1);
                tilex = Servers.localServer.SPAWNPOINTMOLX - 2;
                tiley = Servers.localServer.SPAWNPOINTMOLY - 2;
                v = Villages.getFirstPermanentVillageForKingdom((byte)2);
                if (v == null) {
                    v = Villages.getFirstVillageForKingdom((byte)2);
                }
                santaMolRehan = Zones.addSanta(v, tilex, tiley, 94, 46, "Twin Santa", (byte)2);
                v = Villages.getFirstPermanentVillageForKingdom((byte)4);
                if (v == null) {
                    v = Villages.getFirstVillageForKingdom((byte)4);
                }
                if (v != null) {
                    Creature santa = Zones.addSanta(v, tilex, tiley, 94, 46, "Santa Claus", (byte)4);
                    santas.put(santa.getWurmId(), santa);
                }
            } else {
                int tilex = Servers.localServer.SPAWNPOINTJENNX - 2;
                int tiley = Servers.localServer.SPAWNPOINTJENNY - 2;
                Village v = Villages.getFirstPermanentVillageForKingdom(Servers.localServer.KINGDOM);
                if (v == null) {
                    v = Villages.getFirstVillageForKingdom(Servers.localServer.KINGDOM);
                }
                if (v != null) {
                    try {
                        tilex = v.getToken().getTileX();
                        tiley = v.getToken().getTileY();
                    }
                    catch (NoSuchItemException santa) {
                        // empty catch block
                    }
                }
                try {
                    santa = Creature.doNew(46, tilex * 4 + 1, (float)(tiley * 4 + 1), 90.0f, 0, "Santa Claus", (byte)0, Servers.localServer.KINGDOM);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to create santa! " + ex.getMessage(), ex);
                }
                try {
                    Item julbord = ItemFactory.createItem(442, 90.0f, (tilex + 1) * 4, tiley * 4, 96.0f, true, (byte)0, -10L, null);
                    Zones.sendChristmasEffect(santa, null);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Failed to create julbord " + ex.getMessage(), ex);
                }
            }
            Zones.setHasLoadedChristmas(true);
        }
    }

    @Nullable
    private static Creature addSanta(@Nullable Village v, int tilex, int tiley, int rot, int santaTemplate, String santaName, byte kingdom) {
        Creature santa = null;
        if (v != null) {
            try {
                tilex = v.getToken().getTileX();
                tiley = v.getToken().getTileY();
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
        try {
            santa = Creature.doNew(santaTemplate, tilex * 4 + 2, (float)(tiley * 4) + 0.75f, 90.0f, 0, santaName, (byte)0, kingdom);
            Zones.sendChristmasEffect(santa, null);
            try {
                ItemFactory.createItem(442, 90.0f, tilex * 4 + 1, tiley * 4 + 2, 90.0f, true, (byte)0, -10L, null);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Failed to create julbord " + ex.getMessage(), ex);
            }
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to create " + santaName + "! " + ex.getMessage(), ex);
        }
        return santa;
    }

    public static void sendChristmasEffect(Creature creature, Item item) {
        Player[] players = Players.getInstance().getPlayers();
        for (int x = 0; x < players.length; ++x) {
            if (item != null) {
                players[x].getCommunicator().sendAddEffect(creature.getWurmId(), (short)4, item.getPosX(), item.getPosY(), item.getPosZ(), (byte)0);
                continue;
            }
            players[x].getCommunicator().sendAddEffect(creature.getWurmId(), (short)4, creature.getPosX(), creature.getPosY(), creature.getPositionZ(), (byte)0);
        }
    }

    public static void removeChristmasEffect(Creature idcreature) {
        if (idcreature != null) {
            Player[] players = Players.getInstance().getPlayers();
            for (int x = 0; x < players.length; ++x) {
                players[x].getCommunicator().sendRemoveEffect(idcreature.getWurmId());
            }
        }
    }

    public static void deleteChristmas() {
        if (hasLoadedChristmas) {
            logger.log(Level.INFO, "Starting christmas deletion.");
            Zones.setHasLoadedChristmas(false);
            if (evilsanta != null) {
                Zones.removeChristmasEffect(evilsanta);
                MethodsCreatures.destroyCreature(evilsanta);
                evilsanta = null;
            }
            if (santa != null) {
                Zones.removeChristmasEffect(santa);
                MethodsCreatures.destroyCreature(santa);
                santa = null;
            }
            if (santaMolRehan != null) {
                Zones.removeChristmasEffect(santaMolRehan);
                MethodsCreatures.destroyCreature(santaMolRehan);
                santaMolRehan = null;
            }
            if (!santas.isEmpty()) {
                for (Creature santa : santas.values()) {
                    Zones.removeChristmasEffect(santa);
                    MethodsCreatures.destroyCreature(santa);
                }
                santas.clear();
            }
            Items.deleteChristmasItems();
            logger.log(Level.INFO, "Christmas deletion done.");
            Server.getInstance().broadCastSafe("Christmas is over!");
        }
    }

    public static TilePos safeTile(@Nonnull TilePos tilePos) {
        tilePos.set(Zones.safeTileX(tilePos.x), Zones.safeTileY(tilePos.y));
        return tilePos;
    }

    public static final int safeTileX(int tilex) {
        return Math.max(0, Math.min(tilex, worldTileSizeX - 1));
    }

    public static final int safeTileY(int tiley) {
        return Math.max(0, Math.min(tiley, worldTileSizeY - 1));
    }

    public static int[] getClosestSpring(int tilex, int tiley, int dist) {
        tilex = Zones.safeTileX(tilex);
        tiley = Zones.safeTileY(tiley);
        int closestX = -1;
        int closestY = -1;
        for (int x = Zones.safeTileX(tilex - dist); x < Zones.safeTileX(tilex + 1 + dist); ++x) {
            for (int y = Zones.safeTileY(tiley - dist); y < Zones.safeTileY(tiley + 1 + dist); ++y) {
                if (!Zone.hasSpring(x, y)) continue;
                if (closestX < 0 || closestY < 0) {
                    closestX = Math.abs(tilex - x);
                    closestY = Math.abs(tiley - y);
                    continue;
                }
                int dx1 = tilex - x;
                int dy1 = tiley - y;
                int dx2 = closestX;
                int dy2 = closestY;
                if (!(Math.sqrt(dx1 * dx1 + dy1 * dy1) < Math.sqrt(dx2 * dx2 + dy2 * dy2))) continue;
                closestX = Math.abs(dx1);
                closestY = Math.abs(dy1);
            }
        }
        return new int[]{closestX, closestY};
    }

    public static void setHasLoadedChristmas(boolean aHasLoadedChristmas) {
        hasLoadedChristmas = aHasLoadedChristmas;
    }

    public static boolean isHasLoadedChristmas() {
        return hasLoadedChristmas;
    }

    public static boolean sendNewYear() {
        for (int a = 0; a < Math.min(500, worldTileSizeX / 4); ++a) {
            short effectType = (short)(5 + Server.rand.nextInt(5));
            float x = Server.rand.nextInt(worldTileSizeX) * 4;
            float y = Server.rand.nextInt(worldTileSizeY) * 4;
            try {
                float h = Zones.calculateHeight(x, y, true);
                Players.getInstance().sendEffect(effectType, x, y, Math.max(0.0f, h) + 10.0f + (float)Server.rand.nextInt(30), true, 500.0f);
                continue;
            }
            catch (NoSuchZoneException noSuchZoneException) {
                // empty catch block
            }
        }
        return true;
    }

    public static void removeNewYear() {
        if (hasStartedYet) {
            Player[] players = Players.getInstance().getPlayers();
            LongPosition l = posmap.removeFirst();
            for (int p = 0; p < players.length; ++p) {
                players[p].getCommunicator().sendRemoveEffect(l.getId());
            }
            if (posmap.isEmpty()) {
                hasStartedYet = false;
            }
            return;
        }
    }

    public static void sendNewYearsEffectsToPlayer(Player p) {
        if (posmap.isEmpty()) {
            return;
        }
        Iterator it = posmap.iterator();
        while (it.hasNext()) {
            try {
                LongPosition l = (LongPosition)it.next();
                p.getCommunicator().sendAddEffect(l.getId(), l.getEffectType(), l.getTilex() << 2, l.getTiley() << 2, Zones.calculateHeight(l.getTilex() << 2, l.getTiley() << 2, true), (byte)0);
            }
            catch (NoSuchZoneException noSuchZoneException) {}
        }
    }

    public static final boolean interruptedRange(Creature performer, Creature defender) {
        BlockingResult blockers;
        if (performer == null || defender == null) {
            return true;
        }
        if (performer.equals(defender)) {
            return false;
        }
        if (performer.isOnSurface() != defender.isOnSurface()) {
            performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view of " + defender.getName() + ".");
            return true;
        }
        Zones.resetCoverHolder();
        if (!(!performer.isOnSurface() || (blockers = Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), defender.getPosX(), defender.getPosY(), performer.getPositionZ(), defender.getPositionZ(), performer.isOnSurface(), defender.isOnSurface(), true, 4, -1L, performer.getBridgeId(), performer.getBridgeId(), false)) == null || !(blockers.getTotalCover() >= 100.0f) && (performer.isOnPvPServer() && defender.isOnPvPServer() || blockers.getFirstBlocker() == null) || performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(defender))) {
            performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view of " + defender.getName() + ".");
            return true;
        }
        if (!defender.isWithinDistanceTo(performer, 4.0f) && !VirtualZone.isCreatureTurnedTowardsTarget(defender, performer, 60.0f, false)) {
            performer.getCommunicator().sendCombatNormalMessage("You must turn towards " + defender.getName() + " in order to see it.");
            return true;
        }
        PathFinder pf = new PathFinder(true);
        try {
            Path path = pf.rayCast(performer.getCurrentTile().tilex, performer.getCurrentTile().tiley, defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface(), ((int)Creature.getRange(performer, defender.getPosX(), defender.getPosY()) >> 2) + 5);
            float initialHeight = Math.max(0.0f, performer.getPositionZ() + performer.getAltOffZ() + 1.4f);
            float targetHeight = Math.max(0.0f, defender.getPositionZ() + defender.getAltOffZ() + 1.4f);
            double distx = Math.pow(performer.getCurrentTile().tilex - defender.getCurrentTile().tilex, 2.0);
            double disty = Math.pow(performer.getCurrentTile().tiley - defender.getCurrentTile().tiley, 2.0);
            double dist = Math.sqrt(distx + disty);
            double dx = (double)(targetHeight - initialHeight) / dist;
            while (!path.isEmpty()) {
                PathTile p = path.getFirst();
                distx = Math.pow(p.getTileX() - defender.getCurrentTile().tilex, 2.0);
                disty = Math.pow(p.getTileY() - defender.getCurrentTile().tiley, 2.0);
                double currdist = Math.sqrt(distx + disty);
                float currHeight = Math.max(0.0f, Zones.getLowestCorner(p.getTileX(), p.getTileY(), performer.getLayer()));
                double distmod = currdist * dx;
                if (dx < 0.0) {
                    if ((double)currHeight > (double)targetHeight - distmod) {
                        performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view.");
                        return true;
                    }
                } else if ((double)currHeight > (double)targetHeight - distmod) {
                    performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view.");
                    return true;
                }
                path.removeFirst();
            }
        }
        catch (NoPathException np) {
            performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view.");
            return true;
        }
        return false;
    }

    public static final Set<PathTile> explode(int tilex, int tiley, int floorLevel, boolean followStructure, int diameter) {
        Set<PathTile> toRet;
        block5: {
            block4: {
                toRet = new HashSet<PathTile>();
                VolaTile current = Zones.getTileOrNull(tilex, tiley, floorLevel >= 0);
                if (current == null || current.getStructure() == null) break block4;
                Structure structure = current.getStructure();
                if (!followStructure) break block5;
                for (int x = -diameter; x <= diameter; ++x) {
                    for (int y = -diameter; y <= diameter; ++y) {
                        toRet = Zones.checkStructureTile(structure, tilex, tiley, x, y, floorLevel, toRet);
                    }
                }
                break block5;
            }
            for (int x = -diameter; x <= diameter; ++x) {
                for (int y = -diameter; y <= diameter; ++y) {
                    toRet = Zones.checkStructureTile(null, tilex, tiley, x, y, floorLevel, toRet);
                }
            }
        }
        return toRet;
    }

    private static final Set<PathTile> checkStructureTile(Structure structure, int tilex, int tiley, int xmod, int ymod, int floorLevel, Set<PathTile> toRet) {
        int tx = tilex + xmod;
        int ty = tiley + ymod;
        if (Zones.containsStructure(structure, tx, ty, floorLevel)) {
            toRet.add(Zones.getPathTile(tx, ty, floorLevel >= 0, floorLevel));
        }
        return toRet;
    }

    private static final boolean containsStructure(Structure structure, int tx, int ty, int floorLevel) {
        VolaTile current = Zones.getTileOrNull(tx, ty, floorLevel >= 0);
        if (structure == null && (current == null || current.getStructure() == null)) {
            return true;
        }
        return current != null && current.getStructure() == structure;
    }

    public static final PathTile getPathTile(int tx, int ty, boolean surfaced, int floorLevel) {
        boolean surface = floorLevel >= 0;
        int tileNum2 = Zones.getMesh(surface).getTile(tx, ty);
        return new PathTile(tx, ty, tileNum2, surface, (int)((byte)floorLevel));
    }

    public static final float getLowestCorner(int tilex, int tiley, int layer) {
        tilex = Zones.safeTileX(tilex);
        tiley = Zones.safeTileY(tiley);
        if (layer >= 0) {
            float lowest = Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(tilex, tiley));
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    lowest = Math.min(Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(tilex + y, tiley + y)), lowest);
                }
            }
            return lowest;
        }
        float lowest = Tiles.decodeHeightAsFloat(Server.caveMesh.getTile(tilex, tiley));
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                lowest = Math.min(Tiles.decodeHeightAsFloat(Server.caveMesh.getTile(tilex + y, tiley + y)), lowest);
            }
        }
        return lowest;
    }

    public static final int getSpiritsForTile(int tilex, int tiley, boolean surfaced) {
        int y;
        int x;
        if (!surfaced) {
            return 3;
        }
        for (x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); ++x) {
            for (y = Zones.safeTileY(tiley - 2); y <= Zones.safeTileY(tiley + 2); ++y) {
                if (Features.Feature.SURFACEWATER.isEnabled() && Water.getSurfaceWater(x, y) > 0) {
                    return 2;
                }
                if (Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y)) > 0) continue;
                return 2;
            }
        }
        for (x = Zones.safeTileX(tilex - 2); x <= Zones.safeTileX(tilex + 2); ++x) {
            for (y = Zones.safeTileX(tiley - 2); y <= Zones.safeTileY(tiley + 2); ++y) {
                if (Tiles.decodeType(Server.surfaceMesh.getTile(x, y)) == Tiles.Tile.TILE_LAVA.id) {
                    return 1;
                }
                VolaTile t = Zones.getTileOrNull(x, y, surfaced);
                if (t == null) continue;
                for (Item i : t.getItems()) {
                    if (i.getTemperature() <= 1000) continue;
                    return 1;
                }
            }
        }
        if (Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)) > 1000) {
            return 4;
        }
        return 0;
    }

    public static MeshIO getMesh(boolean surface) {
        return surface ? Server.surfaceMesh : Server.caveMesh;
    }

    private static final void initializeHiveZones() {
        for (int y = 0; y < hiveZoneSizeY; ++y) {
            for (int x = 0; x < hiveZoneSizeX; ++x) {
                hiveZones.add(null);
            }
        }
    }

    public static void addHive(Item hive, boolean silent) {
        int actualZone = Math.max(0, hive.getTileY() / 32) * hiveZoneSizeX + Math.max(0, hive.getTileX() / 32);
        ConcurrentHashMap<Item, HiveZone> thisZone = hiveZones.get(actualZone);
        if (thisZone == null) {
            thisZone = new ConcurrentHashMap();
        }
        thisZone.put(hive, new HiveZone(hive));
        hiveZones.set(actualZone, thisZone);
    }

    public static void removeHive(Item hive, boolean silent) {
        int actualZone = Math.max(0, hive.getTileY() / 32) * hiveZoneSizeX + Math.max(0, hive.getTileX() / 32);
        ConcurrentHashMap<Item, HiveZone> thisZone = hiveZones.get(actualZone);
        if (thisZone == null) {
            logger.log(Level.WARNING, "HiveZone was NULL when it should not have been: " + actualZone);
            return;
        }
        thisZone.remove(hive);
    }

    public static final HiveZone getHiveZoneAt(int tilex, int tiley, boolean surfaced) {
        return Zones.getHiveZone(tilex, tiley, surfaced);
    }

    public static final boolean isFarFromAnyHive(int tilex, int tiley, boolean surfaced) {
        if (!surfaced) {
            return false;
        }
        ConcurrentHashMap<Item, HiveZone> thisZone = null;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                int actualZone = Math.max(0, Math.min(tiley / 32 + j, hiveZoneSizeY - 1)) * hiveZoneSizeX + Math.max(0, Math.min(tilex / 32 + i, hiveZoneSizeX - 1));
                if (actualZone >= hiveZones.size() || (thisZone = hiveZones.get(actualZone)) == null) continue;
                for (HiveZone hz : thisZone.values()) {
                    if (!hz.isCloseToTile(tilex, tiley)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    public static final HiveZone getHiveZone(int tilex, int tiley, boolean surfaced) {
        if (!surfaced) {
            return null;
        }
        HiveZone toReturn = null;
        ConcurrentHashMap<Item, HiveZone> thisZone = null;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                int actualZone = Math.max(0, Math.min(tiley / 32 + j, hiveZoneSizeY - 1)) * hiveZoneSizeX + Math.max(0, Math.min(tilex / 32 + i, hiveZoneSizeX - 1));
                if (actualZone >= hiveZones.size() || (thisZone = hiveZones.get(actualZone)) == null) continue;
                for (HiveZone hz : thisZone.values()) {
                    if (!hz.containsTile(tilex, tiley) || hz.getStrengthForTile(tilex, tiley, surfaced) <= 0) continue;
                    if (toReturn == null) {
                        toReturn = hz;
                        continue;
                    }
                    if (toReturn.getStrengthForTile(tilex, tiley, surfaced) < hz.getStrengthForTile(tilex, tiley, surfaced)) {
                        toReturn = hz;
                        continue;
                    }
                    if (toReturn.getStrengthForTile(tilex, tiley, surfaced) != hz.getStrengthForTile(tilex, tiley, surfaced) || !(hz.getCurrentHive().getCurrentQualityLevel() > toReturn.getCurrentHive().getCurrentQualityLevel())) continue;
                    toReturn = hz;
                }
            }
        }
        return toReturn;
    }

    public static final Item[] getHives(int hiveType) {
        HashSet<Item> hiveZoneSet = new HashSet<Item>();
        for (ConcurrentHashMap<Item, HiveZone> thisZone : hiveZones) {
            if (thisZone == null) continue;
            for (HiveZone hz : thisZone.values()) {
                if (hz.getCurrentHive().getTemplateId() != hiveType) continue;
                hiveZoneSet.add(hz.getCurrentHive());
            }
        }
        return hiveZoneSet.toArray(new Item[hiveZoneSet.size()]);
    }

    @Nullable
    public static final Item getCurrentHive(int tilex, int tiley) {
        HiveZone z = Zones.getHiveZone(tilex, tiley, true);
        if (z != null && z.getCurrentHive() != null) {
            return z.getCurrentHive();
        }
        return null;
    }

    @Nullable
    public static final Item getWildHive(int tilex, int tiley) {
        ConcurrentHashMap<Item, HiveZone> thisZone = null;
        int actualZone = Math.max(0, Math.min(tiley / 32, hiveZoneSizeY - 1)) * hiveZoneSizeX + Math.max(0, Math.min(tilex / 32, hiveZoneSizeX - 1));
        if (actualZone >= hiveZones.size()) {
            return null;
        }
        thisZone = hiveZones.get(actualZone);
        if (thisZone == null) {
            return null;
        }
        for (HiveZone hz : thisZone.values()) {
            Item hive;
            if (!hz.hasHive(tilex, tiley) || (hive = hz.getCurrentHive()).getTemplateId() != 1239) continue;
            return hive;
        }
        return null;
    }

    public static final Item[] getActiveDomesticHives(int tilex, int tiley) {
        HashSet<Item> hiveZoneSet = new HashSet<Item>();
        ConcurrentHashMap<Item, HiveZone> thisZone = null;
        int actualZone = Math.max(0, Math.min(tiley / 32, hiveZoneSizeY - 1)) * hiveZoneSizeX + Math.max(0, Math.min(tilex / 32, hiveZoneSizeX - 1));
        if (actualZone >= hiveZones.size()) {
            return null;
        }
        thisZone = hiveZones.get(actualZone);
        if (thisZone == null) {
            return null;
        }
        for (HiveZone hz : thisZone.values()) {
            Item hive;
            if (!hz.hasHive(tilex, tiley) || (hive = hz.getCurrentHive()).getTemplateId() != 1175) continue;
            hiveZoneSet.add(hive);
        }
        return hiveZoneSet.toArray(new Item[hiveZoneSet.size()]);
    }

    public static final boolean removeWildHive(int tilex, int tiley) {
        Item hive = Zones.getWildHive(tilex, tiley);
        if (hive != null) {
            for (Item item : hive.getItemsAsArray()) {
                Items.destroyItem(item.getWurmId());
            }
            Items.destroyItem(hive.getWurmId());
            return true;
        }
        return false;
    }

    public static boolean isGoodTileForSpawn(int tilex, int tiley, boolean surfaced) {
        return Zones.isGoodTileForSpawn(tilex, tiley, surfaced, false);
    }

    public static boolean isGoodTileForSpawn(int tilex, int tiley, boolean surfaced, boolean canBeOccupied) {
        if (tilex < 0 || tiley < 0 || tilex > worldTileSizeX || tiley > worldTileSizeY) {
            return false;
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, surfaced);
        short[] steepness = Creature.getTileSteepness(tilex, tiley, surfaced);
        return !(!canBeOccupied && t != null || Tiles.decodeHeight(Zones.getTileIntForTile(tilex, tiley, surfaced ? 0 : -1)) <= 0 || steepness[0] >= 23 && steepness[1] >= 23);
    }

    public static boolean isVillagePremSpawn(Village village) {
        return Servers.localServer.testServer || Zones.isPremSpawnZoneAt(village.getStartX(), village.getStartY()) || Zones.isPremSpawnZoneAt(village.getStartX(), village.getEndY()) || Zones.isPremSpawnZoneAt(village.getEndX(), village.getStartY()) || Zones.isPremSpawnZoneAt(village.getEndX(), village.getEndY());
    }

    public static void reposWildHive(int tilex, int tiley, Tiles.Tile theTile, byte aData) {
        Item hive = Zones.getWildHive(tilex, tiley);
        if (hive == null) {
            return;
        }
        Point4f p = MethodsItems.getHivePos(tilex, tiley, theTile, aData);
        if (p.getPosZ() > 0.0f) {
            hive.setPosZ(p.getPosZ());
            hive.updatePos();
        }
    }

    public static void addTurret(Item turret, boolean silent) {
        turretZones.put(turret, new TurretZone(turret));
        if (Servers.localServer.testServer) {
            Players.getInstance().sendGmMessage(null, "System", "Turret added to " + turret.getTileX() + "," + turret.getTileY(), false);
        }
    }

    public static void removeTurret(Item turret, boolean silent) {
        turretZones.remove(turret);
        if (Servers.localServer.testServer) {
            Players.getInstance().sendGmMessage(null, "System", "Turret removed from " + turret.getTileX() + "," + turret.getTileY(), false);
        }
    }

    public static TurretZone getTurretZone(int tileX, int tileY, boolean surfaced) {
        TurretZone bestTurret = null;
        for (TurretZone tz : turretZones.values()) {
            if (!tz.containsTile(tileX, tileY)) continue;
            if (bestTurret == null) {
                bestTurret = tz;
                continue;
            }
            if (!(bestTurret.getStrengthForTile(tileX, tileY, surfaced) < tz.getStrengthForTile(tileX, tileY, surfaced))) continue;
            bestTurret = tz;
        }
        if (bestTurret == null) {
            return null;
        }
        return bestTurret;
    }

    public static Item getCurrentTurret(int tileX, int tileY, boolean surfaced) {
        TurretZone tz = Zones.getTurretZone(tileX, tileY, surfaced);
        if (tz == null) {
            return null;
        }
        return tz.getZoneItem();
    }

    static {
        virtualZones = new HashMap<Integer, VirtualZone>();
        miningTiles = new Hashtable<Integer, Map<Integer, Byte>>();
        duelRings = new HashSet<Item>();
        worldTileSizeX = 1 << Constants.meshSize;
        worldTileSizeY = 1 << Constants.meshSize;
        worldMeterSizeX = (worldTileSizeX - 1) * 4;
        worldMeterSizeY = (worldTileSizeY - 1) * 4;
        protectedTiles = new boolean[worldTileSizeX][worldTileSizeY];
        walkedTiles = new boolean[worldTileSizeX][worldTileSizeY];
        kingdoms = new byte[worldTileSizeX][worldTileSizeY];
        faithSizeX = worldTileSizeX >> 3;
        faithSizeY = worldTileSizeY >> 3;
        domainSizeX = worldTileSizeX / 64;
        domainSizeY = worldTileSizeY / 64;
        influenceSizeX = worldTileSizeX / 256;
        influenceSizeY = worldTileSizeY / 256;
        hiveZoneSizeX = worldTileSizeX / 32;
        hiveZoneSizeY = worldTileSizeY / 32;
        hasLoadedChristmas = false;
        logger = Logger.getLogger(Zones.class.getName());
        currentSaveZoneX = 0;
        currentSaveZoneY = 0;
        loading = false;
        numberOfZones = 0;
        haslogged = false;
        coverHolder = 0;
        surfaceDomains = new FaithZone[faithSizeX][faithSizeY];
        caveDomains = new FaithZone[faithSizeX][faithSizeY];
        altars = new LinkedList();
        altarZones = new ArrayList();
        influenceZones = new ArrayList();
        hiveZones = new ArrayList();
        turretZones = new ConcurrentHashMap();
        influenceCache = new byte[worldTileSizeX][worldTileSizeY];
        pollnum = 0;
        guardTowers = new LinkedList();
        protectedTileFile = ServerDirInfo.getFileDBPath() + File.separator + "protectedTiles.bmap";
        shouldCreateWarTargets = false;
        shouldSourceSprings = false;
        landPercent = new HashMap<Byte, Float>();
        evilsanta = null;
        santa = null;
        santaMolRehan = null;
        santas = new ConcurrentHashMap();
        zrand = new Random();
        currentPollZoneX = zrand.nextInt(worldTileSizeX);
        currentPollZoneY = zrand.nextInt(worldTileSizeY);
        devlog = false;
        ZONE_SYNC_LOCK = new Object();
        posmap = new LinkedList();
        hasStartedYet = false;
        try {
            Zones.createZones();
            Zones.loadAllMiningTiles();
            SpawnTable.createEncounters();
            Zones.initializeWalkTiles();
            Zones.loadProtectedTiles();
            Creatures.getInstance().numberOfZonesX = worldTileSizeX >> 6;
            for (int i = 0; i < worldTileSizeX; ++i) {
                for (int j = 0; j < worldTileSizeY; ++j) {
                    Zones.influenceCache[i][j] = -1;
                }
            }
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to load zones!", ex);
        }
        lastCounted = 0L;
    }
}

