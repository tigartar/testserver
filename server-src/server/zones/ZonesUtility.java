/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zones;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
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
        for (int season = 0; season < 4; ++season) {
            Point[] spots;
            Point offset = MethodsFishing.getSeasonOffset(season);
            Color bgColour = MethodsFishing.getBgColour(season);
            for (int x = 0; x < 128; ++x) {
                for (int y = 0; y < 128; ++y) {
                    colours[offset.getX() + x][offset.getY() + y] = bgColour;
                }
            }
            for (Point spot : spots = MethodsFishing.getSpecialSpots(0, 0, season)) {
                Color fishColour = MethodsFishing.getFishColour(spot.getH());
                for (int x = spot.getX() - 5; x <= spot.getX() + 5; ++x) {
                    for (int y = spot.getY() - 5; y <= spot.getY() + 5; ++y) {
                        colours[offset.getX() + x][offset.getY() + y] = fishColour;
                    }
                }
            }
        }
        BufferedImage bmi = ZonesUtility.makeBufferedImage(colours, 256);
        String filename = "fishSpots.png";
        File f = new File("fishSpots.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
            logger.log(Level.INFO, "[FISH_SPOTS]: Finished");
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "[FISH_SPOTS]: Failed to produce fishSpots.png", e);
        }
    }

    public static void saveMapDump(MeshIO mesh) {
        int tileId;
        int y;
        int x;
        int size = mesh.getSize();
        Color[][] colors = new Color[size][size];
        int maxH = 0;
        int minH = 0;
        float divider = 10.0f;
        for (x = 0; x < size; ++x) {
            for (y = 0; y < size; ++y) {
                tileId = mesh.getTile(x, y);
                short height = Tiles.decodeHeight(tileId);
                if (height > maxH) {
                    maxH = height;
                }
                if (height >= minH) continue;
                minH = height;
            }
        }
        maxH = (int)((float)maxH / 10.0f);
        minH = (int)((float)minH / 10.0f);
        for (x = 0; x < size; ++x) {
            for (y = 0; y < size; ++y) {
                int step;
                float percent;
                float tenth;
                tileId = mesh.getTile(x, y);
                byte type = Tiles.decodeType(tileId);
                Tiles.Tile tile = Tiles.getTile(type);
                short height = Tiles.decodeHeight(tileId);
                if (height > 0) {
                    if (type == 4) {
                        tenth = (float)height / 10.0f;
                        percent = tenth / (float)maxH;
                        step = (int)(150.0f * percent);
                        colors[x][y] = new Color(Math.min(200, 10 + step), Math.min(200, 10 + step), Math.min(200, 10 + step));
                        continue;
                    }
                    if (type == 12) {
                        colors[x][y] = Color.MAGENTA;
                        continue;
                    }
                    tenth = (float)height / 10.0f;
                    percent = tenth / (float)maxH;
                    step = (int)(190.0f * percent);
                    Color c = tile.getColor();
                    colors[x][y] = new Color(c.getRed(), Math.min(255, c.getGreen() + step), c.getBlue());
                    continue;
                }
                tenth = (float)height / 10.0f;
                percent = tenth / (float)minH;
                step = (int)(255.0f * percent);
                colors[x][y] = new Color(0, 0, Math.max(20, 255 - Math.abs(step)));
            }
        }
        BufferedImage bmi = new BufferedImage(size * 4, size * 4, 2);
        Graphics2D g = bmi.createGraphics();
        for (int x2 = 0; x2 < size; ++x2) {
            for (int y2 = 0; y2 < size; ++y2) {
                g.setColor(colors[x2][y2]);
                g.fillRect(x2 * 4, y2 * 4, 4, 4);
            }
        }
        File f = new File("mapdump.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Failed to produce mapdump.png", e);
        }
    }

    public static void saveCreatureDistributionAsImg(MeshIO mesh) {
        Creature[] crets;
        logger.log(Level.INFO, "[CREATURE_DUMP]: Started");
        int size = mesh.getSize();
        Color[][] colors = ZonesUtility.getHeightMap(size);
        for (Creature c : crets = Creatures.getInstance().getCreatures()) {
            colors[c.getTileX()][c.getTileY()] = c.isGuard() ? Color.decode("#006000") : (c.isBred() ? Color.CYAN : (c.isDomestic() ? Color.YELLOW : Color.RED));
        }
        BufferedImage bmi = ZonesUtility.makeBufferedImage(colors, size);
        File f = new File("creatures.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
            logger.log(Level.INFO, "[CREATURE_DUMP]: Finished");
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "[CREATURE_DUMP]: Failed to produce creatures.png", e);
        }
    }

    public static void saveMapWithMarkersAsImg() {
        logger.log(Level.INFO, "[MAP WITH MARKERS DUMP]: Started");
        int size = Server.surfaceMesh.getSize();
        Color[][] colours = ZonesUtility.getHeightMap(size);
        ZonesUtility.AddOptedInVillages(size, colours);
        ZonesUtility.AddMarkers(Items.getMarkers(), colours);
        BufferedImage bmi = ZonesUtility.makeBufferedImage(colours, size);
        File f = new File("markersdump.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
            logger.log(Level.INFO, "[MAP WITH MARKERS DUMP]: Finished");
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "[MAP WITH MARKERS DUMP]: Failed to produce markers.png", e);
        }
    }

    public static void saveRoutesAsImg() {
        logger.log(Level.INFO, "[ROUTES_DUMP]: Started");
        int size = Server.surfaceMesh.getSize();
        Color[][] colours = ZonesUtility.getHeightMap(size);
        ZonesUtility.AddMarkers(Routes.getMarkers(), colours);
        BufferedImage bmi = ZonesUtility.makeBufferedImage(colours, size);
        File f = new File("routes.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
            logger.log(Level.INFO, "[ROUTES_DUMP]: Finished");
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "[ROUTES_DUMP]: Failed to produce routes.png", e);
        }
    }

    public static void saveWaterTypesAsImg(boolean onSurface) {
        logger.log(Level.INFO, "[WATER_TYPES_DUMP]: Started");
        int size = Server.surfaceMesh.getSize();
        Color[][] colours = ZonesUtility.getHeightMap(size);
        for (int x = 0; x < size; ++x) {
            block11: for (int y = 0; y < size; ++y) {
                switch (WaterType.getWaterType(x, y, onSurface)) {
                    case 1: {
                        colours[x][y] = new Color(174, 168, 253);
                        continue block11;
                    }
                    case 2: {
                        colours[x][y] = new Color(137, 128, 252);
                        continue block11;
                    }
                    case 3: {
                        colours[x][y] = new Color(115, 150, 165);
                        continue block11;
                    }
                    case 4: {
                        colours[x][y] = new Color(163, 200, 176);
                        continue block11;
                    }
                    case 5: {
                        colours[x][y] = new Color(235, 200, 176);
                        continue block11;
                    }
                    case 6: {
                        colours[x][y] = new Color(185, 150, 165);
                    }
                }
            }
        }
        BufferedImage bmi = ZonesUtility.makeBufferedImage(colours, size);
        String filename = onSurface ? "waterSurfaceTypes.png" : "waterCaveTypes.png";
        File f = new File(filename);
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
            logger.log(Level.INFO, "[WATER_TYPES_DUMP]: Finished");
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "[WATER_TYPES_DUMP]: Failed to produce (" + onSurface + ") waterTypes.png", e);
        }
    }

    private static Color[][] getHeightMap(int size) {
        int tileId;
        int y;
        int x;
        Color[][] colours = new Color[size][size];
        int maxH = 0;
        int minH = 0;
        float divider = 10.0f;
        for (x = 0; x < size; ++x) {
            for (y = 0; y < size; ++y) {
                tileId = Server.surfaceMesh.getTile(x, y);
                short height = Tiles.decodeHeight(tileId);
                if (height > maxH) {
                    maxH = height;
                }
                if (height >= minH) continue;
                minH = height;
            }
        }
        maxH = (int)((float)maxH / 10.0f);
        minH = (int)((float)minH / 10.0f);
        for (x = 0; x < size; ++x) {
            for (y = 0; y < size; ++y) {
                int step;
                float percent;
                float tenth;
                tileId = Server.surfaceMesh.getTile(x, y);
                byte type = Tiles.decodeType(tileId);
                Tiles.Tile tile = Tiles.getTile(type);
                short height = Tiles.decodeHeight(tileId);
                if (height > 0) {
                    if (type == 4) {
                        tenth = (float)height / 10.0f;
                        percent = tenth / (float)maxH;
                        step = (int)(150.0f * percent);
                        colours[x][y] = new Color(Math.min(200, 10 + step), Math.min(200, 10 + step), Math.min(200, 10 + step));
                        continue;
                    }
                    if (type == 12) {
                        colours[x][y] = Color.MAGENTA;
                        continue;
                    }
                    tenth = (float)height / 10.0f;
                    percent = tenth / (float)maxH;
                    step = (int)(190.0f * percent);
                    Color c = tile.getColor();
                    colours[x][y] = new Color(c.getRed(), Math.min(255, c.getGreen() + step), c.getBlue());
                    continue;
                }
                tenth = (float)height / 10.0f;
                percent = tenth / (float)minH;
                step = (int)(255.0f * percent);
                colours[x][y] = new Color(0, 0, Math.max(20, 255 - Math.abs(step)));
            }
        }
        return colours;
    }

    private static void AddOptedInVillages(int size, Color[][] colours) {
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                Village vill;
                VolaTile tt = Zones.getOrCreateTile(x, y, true);
                Village village = vill = tt == null ? null : tt.getVillage();
                if (vill == null || !vill.isHighwayFound() || x != vill.getStartX() && x != vill.getEndX() && y != vill.getStartY() && y != vill.getEndY()) continue;
                colours[x][y] = new Color(255, 127, 39);
            }
        }
    }

    private static void AddMarkers(Item[] markers, Color[][] colours) {
        block12: for (Item marker : markers) {
            int id = marker.getTemplateId();
            switch (id) {
                case 1112: {
                    if (marker.isOnSurface()) {
                        ZonesUtility.addMarker(colours, marker, waystoneSurface);
                        continue block12;
                    }
                    Color colour = colours[marker.getTileX()][marker.getTileY()];
                    if (colour == waystoneSurface) continue block12;
                    ZonesUtility.addMarker(colours, marker, waystoneCave);
                    continue block12;
                }
                case 1114: {
                    Color colour = colours[marker.getTileX()][marker.getTileY()];
                    if (colour == waystoneSurface || colour == waystoneCave) {
                        // empty if block
                    }
                    if (marker.isOnSurface()) {
                        switch (MethodsHighways.numberOfSetBits(marker.getAuxData())) {
                            case 0: {
                                ZonesUtility.addMarker(colours, marker, catseye0Surface);
                                continue block12;
                            }
                            case 1: {
                                ZonesUtility.addMarker(colours, marker, catseye1Surface);
                                continue block12;
                            }
                        }
                        if (Routes.isCatseyeUsed(marker)) {
                            ZonesUtility.addMarker(colours, marker, catseye3Surface);
                            continue block12;
                        }
                        ZonesUtility.addMarker(colours, marker, catseye2Surface);
                        continue block12;
                    }
                    if (colour == catseye0Surface || colour == catseye2Surface || colour == catseye2Surface || colour == catseye3Surface) continue block12;
                    switch (MethodsHighways.numberOfSetBits(marker.getAuxData())) {
                        case 0: {
                            ZonesUtility.addMarker(colours, marker, catseye0Cave);
                            continue block12;
                        }
                        case 1: {
                            ZonesUtility.addMarker(colours, marker, catseye1Cave);
                            continue block12;
                        }
                    }
                    if (Routes.isCatseyeUsed(marker)) {
                        ZonesUtility.addMarker(colours, marker, catseye3Cave);
                        continue block12;
                    }
                    ZonesUtility.addMarker(colours, marker, catseye2Cave);
                    continue block12;
                }
            }
        }
    }

    private static void addMarker(Color[][] colours, Item marker, Color colour) {
        int x = marker.getTileX();
        int y = marker.getTileY();
        colours[x][y] = colour;
        ZonesUtility.addColour(colours, x - 1, y, colour);
        ZonesUtility.addColour(colours, x, y - 1, colour);
        ZonesUtility.addColour(colours, x - 1, y - 1, colour);
        MeshIO mesh = marker.isOnSurface() ? Server.surfaceMesh : Server.caveMesh;
        for (int xx = 0; xx <= 1; ++xx) {
            for (int yy = 0; yy <= 1; ++yy) {
                int tileId;
                byte type;
                if (xx == 0 || yy == 0 || !Tiles.isRoadType(type = Tiles.decodeType(tileId = mesh.getTile(x + xx, y + yy))) && type != 201) continue;
                ZonesUtility.addColour(colours, x + xx, y + yy, colour);
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
        Graphics2D g = bmi.createGraphics();
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                g.setColor(colors[x][y]);
                g.fillRect(x, y, 1, 1);
            }
        }
        return bmi;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void saveAsImg(MeshIO mesh) {
        long lStart = System.nanoTime();
        int size = mesh.getSize();
        logger.info("Size:" + size);
        logger.info("Data Size: " + mesh.data.length);
        byte[][] tiles = new byte[size][size];
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                byte type;
                int tileId = mesh.getTile(x, y);
                tiles[x][y] = type = Tiles.decodeType(tileId);
            }
        }
        BufferedImage bmi = new BufferedImage(size, size, 2);
        Graphics2D g = bmi.createGraphics();
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
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
                if (!paint) continue;
                g.fillRect(x, y, 1, 1);
            }
        }
        File f = new File("Ore.png");
        try {
            ImageIO.write((RenderedImage)bmi, "png", f);
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Failed to produce ore.png", e);
        }
        finally {
            long lElapsedTime = System.nanoTime() - lStart;
            logger.info("Saved Mesh to '" + f.getAbsoluteFile() + "', that took " + (float)lElapsedTime / 1000000.0f + ", millis.");
        }
    }
}

