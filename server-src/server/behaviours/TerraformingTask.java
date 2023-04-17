/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.meshgen.ImprovedNoise;
import com.wurmonline.server.meshgen.IslandAdder;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Den;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerraformingTask
implements MiscConstants,
ItemMaterials {
    private int counter = 0;
    private final int task;
    private final byte kingdom;
    private final int entityId;
    private final String entityName;
    private int startX = 0;
    private int startY = 0;
    private int startHeight = 0;
    private TerraformingTask next = null;
    private final int tasksRemaining;
    private int totalTasks = 0;
    private final Random random = new Random();
    private final boolean firstTask;
    public static final int ERUPT = 0;
    public static final int INDENT = 1;
    public static final int PLATEAU = 2;
    public static final int CRATERS = 3;
    public static final int MULTIPLATEAU = 4;
    public static final int RAVINE = 5;
    public static final int ISLAND = 6;
    public static final int MULTIRAVINE = 7;
    private int radius = 0;
    private int length = 0;
    private int direction = 0;
    private MeshIO topLayer;
    private MeshIO rockLayer;
    private final String[] prefixes = new String[]{"Et", "De", "Old", "Gaz", "Mak", "Fir", "Fyre", "Eld", "Vagn", "Mag", "Lav", "Volc", "Rad", "Ash", "Ask"};
    private final String[] suffixes = new String[]{"na", "cuse", "fir", "egap", "dire", "haul", "vann", "un", "lik", "ingan", "enken", "mosh", "kil", "atrask", "eskap"};
    private static final Logger logger = Logger.getLogger(TerraformingTask.class.getName());

    public TerraformingTask(int whatToDo, byte targetKingdom, String epicEntityName, int epicEntityId, int tasksLeft, boolean isFirstTask) {
        this.task = whatToDo;
        this.kingdom = targetKingdom;
        this.entityName = epicEntityName;
        this.entityId = epicEntityId;
        if (tasksLeft < 0) {
            if (this.task == 3) {
                boolean numCratersRoot = true;
                tasksLeft = this.totalTasks = 1 + 1 * this.random.nextInt(Math.max(1, 1));
            } else if (this.task == 4 || this.task == 7) {
                tasksLeft = this.totalTasks = 1 + this.random.nextInt(2);
            }
        }
        this.firstTask = isFirstTask;
        this.tasksRemaining = tasksLeft;
        this.startX = 0;
        this.startY = 0;
        Server.getInstance().addTerraformingTask(this);
    }

    public void setSXY(int sx, int sy) {
        this.startX = sx;
        this.startY = sy;
    }

    private void setHeight(int sheight) {
        this.startHeight = sheight;
    }

    private void setTotalTasks(int total) {
        this.totalTasks = total;
    }

    public final boolean setCoordinates() {
        boolean toReturn = false;
        switch (this.task) {
            case 0: {
                toReturn = this.eruptCoord();
                break;
            }
            case 1: {
                toReturn = this.indentCoord();
                break;
            }
            case 2: 
            case 4: {
                toReturn = this.plateauCoord();
                break;
            }
            case 3: {
                toReturn = this.craterCoord();
                break;
            }
            case 5: 
            case 7: {
                toReturn = this.ravineCoord();
                break;
            }
            case 6: {
                toReturn = this.islandCoord();
                break;
            }
            default: {
                toReturn = false;
            }
        }
        return toReturn;
    }

    public boolean poll() {
        if (this.next != null) {
            return this.next.poll();
        }
        if (this.counter == 0) {
            if (this.startX != 0 && this.startY != 0) {
                this.sendEffect();
            } else if (this.startX == 0 && this.startY == 0 && this.setCoordinates()) {
                this.sendEffect();
            } else {
                return true;
            }
        }
        if (this.counter == 60) {
            this.terraform();
            if (this.tasksRemaining == 0) {
                return true;
            }
        }
        if (this.counter == 65 && this.tasksRemaining > 0) {
            this.next = new TerraformingTask(this.task, this.kingdom, this.entityName, this.entityId, this.tasksRemaining - 1, false);
            this.next.setCoordinates();
            this.next.setSXY(this.startX, this.startY);
            if (this.task == 4 || this.task == 7) {
                if (this.random.nextBoolean()) {
                    int modx = (this.startX + this.radius - (this.startX - this.radius)) / (1 + this.random.nextInt(4));
                    int mody = (this.startY + this.radius - (this.startY - this.radius)) / (1 + this.random.nextInt(4));
                    if (this.random.nextBoolean()) {
                        modx = -modx;
                    }
                    if (this.random.nextBoolean()) {
                        mody = -mody;
                    }
                    if (this.startX + modx > Zones.worldTileSizeX - 200) {
                        this.startX -= 200;
                    }
                    if (this.startY + mody > Zones.worldTileSizeY - 200) {
                        this.startY -= 200;
                    }
                    if (this.startX + modx < 200) {
                        this.startX += 200;
                    }
                    if (this.startY + mody < 200) {
                        this.startY += 200;
                    }
                    this.next.setSXY(this.startX + modx, this.startY + mody);
                }
            } else if (this.task == 3) {
                int modx = (int)(this.random.nextGaussian() * 3.0 * (double)this.radius);
                int mody = (int)(this.random.nextGaussian() * 3.0 * (double)this.radius);
                if (this.startX + modx > Zones.worldTileSizeX - 200) {
                    this.startX -= modx;
                }
                if (this.startY + mody > Zones.worldTileSizeY - 200) {
                    this.startY -= mody;
                }
                if (this.startX + modx < 200) {
                    this.startX += modx;
                }
                if (this.startY + mody < 200) {
                    this.startY += mody;
                }
                this.next.setSXY(this.startX + modx, this.startY + mody);
            }
            this.next.setHeight(this.startHeight);
            this.next.setTotalTasks(this.totalTasks);
        }
        ++this.counter;
        return false;
    }

    private void terraform() {
        switch (this.task) {
            case 0: {
                this.erupt();
                break;
            }
            case 1: {
                this.indent();
                break;
            }
            case 2: 
            case 4: {
                this.plateau();
                break;
            }
            case 3: {
                this.crater();
                break;
            }
            case 5: 
            case 7: {
                this.ravine();
                break;
            }
            case 6: {
                this.island();
                break;
            }
        }
    }

    private final boolean ravineCoord() {
        boolean toReturn = false;
        this.radius = 5 + this.random.nextInt(5);
        this.length = 20 + this.random.nextInt(40);
        this.direction = this.random.nextInt(8);
        for (int runs = 0; runs < 20; ++runs) {
            if (this.firstTask) {
                this.startX = this.random.nextInt(Zones.worldTileSizeX);
                this.startY = this.random.nextInt(Zones.worldTileSizeY);
                if (this.startX > Zones.worldTileSizeX - 200) {
                    this.startX -= 200;
                }
                if (this.startY > Zones.worldTileSizeY - 200) {
                    this.startY -= 200;
                }
                if (this.startX < 200) {
                    this.startX += 200;
                }
                if (this.startY < 200) {
                    this.startY += 200;
                }
                if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) <= 0 || !this.isOutsideOwnKingdom(this.startX, this.startY)) continue;
                toReturn = true;
                break;
            }
            return true;
        }
        return toReturn;
    }

    private void ravine() {
        if (this.totalTasks > 0 && this.totalTasks % 2 == 0) {
            this.direction = this.totalTasks;
        }
        IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
        Map<Integer, Set<Integer>> changes = null;
        changes = isl.createRavine(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), this.length, this.direction);
        logger.log(Level.INFO, "Ravine at " + this.startX + "," + this.startY);
        if (changes != null) {
            int minx = Zones.worldTileSizeX;
            int miny = Zones.worldTileSizeY;
            int maxx = 0;
            int maxy = 0;
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                if (x < minx) {
                    minx = x;
                }
                if (x > maxx) {
                    maxx = x;
                }
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    if (y < miny) {
                        miny = y;
                    }
                    if (y > maxy) {
                        maxy = y;
                    }
                    Terraforming.forceSetAsRock(x, y, Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id, 100);
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
            try {
                ItemFactory.createItem(696, 99.0f, (minx + (maxx - minx) / 2) * 4 + 2, (miny + (maxy - miny) / 2) * 4 + 2, this.random.nextFloat() * 350.0f, true, (byte)57, (byte)0, -10L, null);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
        }
    }

    private static final void destroyStructures(int x, int y) {
        VolaTile t = Zones.getTileOrNull(x, y, true);
        if (t != null) {
            short[] steepness = Creature.getTileSteepness(x, y, true);
            if (t.getStructure() != null && steepness[1] > 40) {
                for (TimeConstants timeConstants : t.getWalls()) {
                    ((Wall)timeConstants).setAsPlan();
                }
            }
            for (TimeConstants timeConstants : t.getFences()) {
                if (steepness[1] <= 40) continue;
                ((Fence)timeConstants).destroy();
            }
        }
    }

    private final boolean craterCoord() {
        boolean toReturn = false;
        this.radius = 10 + this.random.nextInt(20);
        for (int runs = 0; runs < 20; ++runs) {
            if (this.firstTask && this.startX <= 0 && this.startY <= 0) {
                this.startX = this.random.nextInt(Zones.worldTileSizeX);
                this.startY = this.random.nextInt(Zones.worldTileSizeY);
                if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) <= 0 || !this.isOutsideOwnKingdom(this.startX, this.startY)) continue;
                toReturn = true;
                break;
            }
            return true;
        }
        return toReturn;
    }

    public final void changeTile(int x, int y) {
        VolaTile tile2;
        VolaTile tile1 = Zones.getTileOrNull(x, y, true);
        if (tile1 != null) {
            Creature[] crets;
            for (Creature lCret2 : crets = tile1.getCreatures()) {
                lCret2.setChangedTileCounter();
            }
            tile1.change();
        }
        if ((tile2 = Zones.getTileOrNull(x, y, false)) != null) {
            Creature[] crets;
            for (Creature lCret2 : crets = tile2.getCreatures()) {
                lCret2.setChangedTileCounter();
            }
            tile2.change();
        }
    }

    private void crater() {
        boolean ok = true;
        if (this.radius == 0) {
            this.radius = 10 + this.random.nextInt(20);
        }
        for (int x = 0; x < 10; ++x) {
            int ey;
            int ex;
            int sy;
            int sx = Zones.safeTileX(this.startX - this.radius);
            Set<Village> blockers = Villages.getVillagesWithin(sx, sy = Zones.safeTileY(this.startY - this.radius), ex = Zones.safeTileX(this.startX + this.radius), ey = Zones.safeTileY(this.startY + this.radius));
            if (blockers == null || blockers.size() == 0) {
                ok = true;
                break;
            }
            for (Village v : blockers) {
                logger.log(Level.WARNING, v.getName() + " is in the way at " + sx + "," + sy + " to " + ex + "," + ey);
            }
            ok = false;
            int modx = (int)(this.random.nextGaussian() * (double)this.radius);
            int mody = (int)(this.random.nextGaussian() * (double)this.radius);
            if (this.startX + modx > Zones.worldTileSizeX - 200) {
                this.startX -= modx;
            }
            if (this.startY + mody > Zones.worldTileSizeY - 200) {
                this.startY -= mody;
            }
            if (this.startX + modx < 200) {
                this.startX += modx;
            }
            if (this.startY + mody < 200) {
                this.startY += mody;
            }
            if (Servers.localServer.testServer) {
                logger.log(Level.INFO, "MOdx=" + modx + ", mody=" + mody + " radius=" + this.radius + " yields sx=" + (this.startX + modx) + " sy " + (this.startY + mody));
            }
            this.setSXY(this.startX + modx, this.startY + mody);
        }
        if (!ok) {
            logger.log(Level.INFO, "Avoiding Crater at " + this.startX + "," + this.startY + " radius=" + this.radius);
            return;
        }
        Map<Integer, Set<Integer>> changes = null;
        IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
        int sx = Zones.safeTileX(this.startX - this.radius);
        int sy = Zones.safeTileY(this.startY - this.radius);
        int ex = Zones.safeTileX(this.startX + this.radius);
        int ey = Zones.safeTileY(this.startY + this.radius);
        changes = isl.createCrater(sx, sy, ex, ey);
        logger.log(Level.INFO, "Crater at " + this.startX + "," + this.startY + " radius=" + this.radius);
        if (changes != null) {
            int minx = Zones.worldTileSizeX;
            int miny = Zones.worldTileSizeY;
            int maxx = 0;
            int maxy = 0;
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                if (x < minx) {
                    minx = x;
                }
                if (x > maxx) {
                    maxx = x;
                }
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    if (y < miny) {
                        miny = y;
                    }
                    if (y > maxy) {
                        maxy = y;
                    }
                    Terraforming.forceSetAsRock(x, y, Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id, 100);
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
            try {
                ItemFactory.createItem(696, 99.0f, this.startX * 4 + 2, this.startY * 4 + 2, this.random.nextFloat() * 350.0f, true, (byte)57, (byte)0, -10L, null);
            }
            catch (Exception exs) {
                logger.log(Level.WARNING, exs.getMessage(), exs);
            }
        }
    }

    private final boolean indentCoord() {
        boolean toReturn = false;
        this.radius = 10 + this.random.nextInt(20);
        for (int runs = 0; runs < 20; ++runs) {
            this.startX = this.random.nextInt(Zones.worldTileSizeX);
            this.startY = this.random.nextInt(Zones.worldTileSizeY);
            if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) <= 0 || !this.isOutsideOwnKingdom(this.startX, this.startY)) continue;
            toReturn = true;
            break;
        }
        return toReturn;
    }

    private void indent() {
        Map<Integer, Set<Integer>> changes = null;
        IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
        changes = isl.createRockIndentation(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), Zones.safeTileX(this.startX + this.radius), Zones.safeTileY(this.startY + this.radius));
        logger.log(Level.INFO, "Rock Indentation at " + this.startX + "," + this.startY);
        if (changes != null) {
            int minx = Zones.worldTileSizeX;
            int miny = Zones.worldTileSizeY;
            int maxx = 0;
            int maxy = 0;
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                if (x < minx) {
                    minx = x;
                }
                if (x > maxx) {
                    maxx = x;
                }
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    if (y < miny) {
                        miny = y;
                    }
                    if (y > maxy) {
                        maxy = y;
                    }
                    Terraforming.forceSetAsRock(x, y, (byte)1, 100);
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
        }
    }

    private final boolean isOutsideOwnKingdom(int tilex, int tiley) {
        byte kingdomId = Zones.getKingdom(tilex, tiley);
        Kingdom k = Kingdoms.getKingdom(kingdomId);
        return k == null || k.getTemplate() != this.kingdom;
    }

    private boolean eruptCoord() {
        boolean toReturn = false;
        int maxTries = 20;
        for (int x = 0; x < 20; ++x) {
            Den d = Zones.getRandomTop();
            this.radius = 10 + this.random.nextInt(20);
            if (this.startX > 0 || this.startY > 0) break;
            if (d == null || !this.isOutsideOwnKingdom(d.getTilex(), d.getTiley())) continue;
            this.startX = d.getTilex();
            this.startY = d.getTiley();
            toReturn = true;
            break;
        }
        return toReturn;
    }

    private void erupt() {
        IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
        Map<Integer, Set<Integer>> changes = null;
        changes = isl.createVolcano(Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), Zones.safeTileX(this.startX + this.radius), Zones.safeTileY(this.startY + this.radius));
        logger.log(Level.INFO, "Volcano Eruption at " + this.startX + "," + this.startY);
        if (changes != null) {
            int minx = Zones.worldTileSizeX;
            int miny = Zones.worldTileSizeY;
            int maxx = 0;
            int maxy = 0;
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                if (x < minx) {
                    minx = x;
                }
                if (x > maxx) {
                    maxx = x;
                }
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    if (y < miny) {
                        miny = y;
                    }
                    if (y > maxy) {
                        maxy = y;
                    }
                    Terraforming.forceSetAsRock(x, y, Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id, 100);
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    Players.getInstance().sendChangedTile(x, y, false, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
            String name = "Unknown";
            if (Server.rand.nextBoolean()) {
                name = this.prefixes[Server.rand.nextInt(this.prefixes.length)];
                if (Server.rand.nextInt(10) > 0) {
                    name = name + this.suffixes[Server.rand.nextInt(this.suffixes.length)];
                }
            }
            if (Server.rand.nextBoolean()) {
                name = this.suffixes[Server.rand.nextInt(this.suffixes.length)];
                if (Server.rand.nextInt(10) > 0) {
                    name = name + this.prefixes[Server.rand.nextInt(this.prefixes.length)];
                }
            }
            name = LoginHandler.raiseFirstLetter(name);
            new FocusZone(minx, maxx, miny, maxy, 1, name, "", true);
        }
    }

    private final boolean plateauCoord() {
        boolean toReturn = false;
        this.radius = 10 + this.random.nextInt(20);
        for (int runs = 0; runs < 20; ++runs) {
            if (this.firstTask) {
                this.startX = this.random.nextInt(Zones.worldTileSizeX);
                this.startY = this.random.nextInt(Zones.worldTileSizeY);
                this.startHeight = 200;
                if (Tiles.decodeHeight(Server.surfaceMesh.getTile(this.startX, this.startY)) <= 0 || !this.isOutsideOwnKingdom(this.startX, this.startY)) continue;
                toReturn = true;
                break;
            }
            return true;
        }
        return toReturn;
    }

    private void plateau() {
        int modx = 0;
        int mody = 0;
        boolean ok = true;
        if (!this.firstTask) {
            for (int x = 0; x < 20; ++x) {
                int ey;
                modx = (this.startX + this.radius - (this.startX - this.radius)) / (1 + this.random.nextInt(4));
                mody = (this.startY + this.radius - (this.startY - this.radius)) / (1 + this.random.nextInt(4));
                if (this.random.nextBoolean()) {
                    modx = -modx;
                }
                if (this.random.nextBoolean()) {
                    mody = -mody;
                }
                int sx = Zones.safeTileX(this.startX + modx - this.radius);
                int ex = Zones.safeTileX(this.startX + modx + this.radius);
                int sy = Zones.safeTileY(this.startY + mody - this.radius);
                Set<Village> vills = Villages.getVillagesWithin(sx, sy, ex, ey = Zones.safeTileY(this.startY + mody + this.radius));
                if (vills == null || vills.size() == 0) {
                    ok = true;
                    break;
                }
                ok = false;
            }
        }
        if (!ok) {
            logger.log(Level.INFO, "Skipping Plateu at " + this.startX + "," + this.startY);
            return;
        }
        int sx = Zones.safeTileX(this.startX + modx - this.radius);
        int ex = Zones.safeTileX(this.startX + modx + this.radius);
        int sy = Zones.safeTileY(this.startY + mody - this.radius);
        int ey = Zones.safeTileY(this.startY + mody + this.radius);
        IslandAdder isl = new IslandAdder(Server.surfaceMesh, Server.rockMesh);
        Map<Integer, Set<Integer>> changes = null;
        changes = isl.createPlateau(sx, sy, ex, ey, this.startHeight + this.random.nextInt(150));
        logger.log(Level.INFO, "Plateu at " + this.startX + "," + this.startY);
        if (changes != null) {
            int minx = Zones.worldTileSizeX;
            int miny = Zones.worldTileSizeY;
            int maxx = 0;
            int maxy = 0;
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                if (x < minx) {
                    minx = x;
                }
                if (x > maxx) {
                    maxx = x;
                }
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    if (y < miny) {
                        miny = y;
                    }
                    if (y > maxy) {
                        maxy = y;
                    }
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
        }
    }

    public void sendEffect() {
        boolean tx = false;
        boolean ty = false;
        switch (this.task) {
            case 0: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)12, this.startX, this.startY, Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
                break;
            }
            case 1: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)13, this.startX, this.startY, Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
                break;
            }
            case 2: 
            case 4: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)11, this.startX, this.startY, Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.direction, this.length, this.kingdom, (byte)this.entityId);
                break;
            }
            case 3: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)10, this.startX, this.startY, Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(0, 0)), this.radius, this.tasksRemaining, this.direction, this.kingdom, (byte)this.entityId);
                break;
            }
            case 5: 
            case 7: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)14, Zones.safeTileX(this.startX - this.radius), Zones.safeTileY(this.startY - this.radius), Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.length, this.direction, this.kingdom, (byte)this.entityId);
                break;
            }
            case 6: {
                Players.getInstance().sendGlobalNonPersistantComplexEffect(-10L, (short)15, this.startX, this.startY, Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(this.startX, this.startY)), this.radius, this.length, this.direction, this.kingdom, (byte)this.entityId);
                break;
            }
        }
    }

    private final boolean islandCoord() {
        this.rockLayer = Server.rockMesh;
        this.topLayer = Server.surfaceMesh;
        int minSize = Zones.worldTileSizeX / 15;
        for (int i = 800; i >= minSize; --i) {
            for (int j = 0; j < 2; ++j) {
                int y;
                int width;
                int height = width = i;
                int x = this.random.nextInt(Zones.worldTileSizeX - width - 128) + 64;
                if (!this.isIslandOk(x, y = this.random.nextInt(Zones.worldTileSizeY - width - 128) + 64, x + width, y + height)) continue;
                this.startX = x + width / 2;
                this.startY = y + width / 2;
                this.length = width / 2;
                this.radius = height / 2;
                logger.info("Found island location " + i + " @ " + (x + width / 2) + ", " + (y + height / 2));
                return true;
            }
        }
        return false;
    }

    private void island() {
        Map<Integer, Set<Integer>> changes = null;
        changes = this.addIsland(this.startX - this.length, this.startX + this.length, this.startY - this.radius, this.startY + this.radius);
        if (changes != null) {
            for (Map.Entry<Integer, Set<Integer>> me : changes.entrySet()) {
                Integer x = me.getKey();
                Set<Integer> set = me.getValue();
                for (Integer y : set) {
                    this.changeTile(x, y);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    TerraformingTask.destroyStructures(x, y);
                }
            }
        }
    }

    public Map<Integer, Set<Integer>> addToChanges(Map<Integer, Set<Integer>> changes, int x, int y) {
        Set<Integer> s = changes.get(x);
        if (s == null) {
            s = new HashSet<Integer>();
        }
        if (!s.contains(y)) {
            s.add(y);
        }
        changes.put(x, s);
        return changes;
    }

    public final boolean isIslandOk(int x0, int y0, int x1, int y1) {
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        for (int x = x0; x < x1; ++x) {
            double xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (int y = y0; y < y1; ++y) {
                short height;
                double yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                double d = Math.sqrt(xd * xd + yd * yd);
                if (!(d < 1.0) || (height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()])) <= -2) continue;
                return false;
            }
        }
        return true;
    }

    public Map<Integer, Set<Integer>> addIsland(int x0, int y0, int x1, int y1) {
        double od;
        double d;
        short height;
        double yd;
        int y;
        double xd;
        int x;
        int xm = (x1 + x0) / 2;
        int ym = (y1 + y0) / 2;
        double dirOffs = this.random.nextDouble() * Math.PI * 2.0;
        int branchCount = this.random.nextInt(7) + 3;
        Map<Integer, Set<Integer>> changes = new HashMap<Integer, Set<Integer>>();
        float[] branches = new float[branchCount];
        for (int i = 0; i < branchCount; ++i) {
            branches[i] = this.random.nextFloat() * 0.25f + 0.75f;
        }
        ImprovedNoise noise = new ImprovedNoise(this.random.nextLong());
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                double dir;
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                double od2 = Math.sqrt(xd * xd + yd * yd);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float nextBranch = branches[(branch + 1) % branchCount];
                float pow = last + (nextBranch - last) * step;
                double d2 = od2;
                if (!((d2 /= (double)pow) < 1.0)) continue;
                d2 *= d2;
                d2 *= d2;
                d2 = 1.0 - d2;
                height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                float n = (float)(noise.perlinNoise(x, y) * 64.0) + 100.0f;
                int hh = (int)((double)height + (double)((n *= 2.0f) - (float)height) * d2);
                byte type = Tiles.Tile.TILE_DIRT.id;
                if (hh > 5 && this.random.nextInt(100) == 0) {
                    type = Tiles.Tile.TILE_GRASS.id;
                }
                hh = hh > 0 ? (int)((float)hh + 0.07f) : (int)((float)hh - 0.07f);
                this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode((short)hh, type, (byte)0);
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                float n;
                double dir;
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                d = Math.sqrt(xd * xd + yd * yd);
                od = d * (double)(x1 - x0);
                for (dir = (Math.atan2(yd, xd) + Math.PI) / (Math.PI * 2) + dirOffs; dir < 0.0; dir += 1.0) {
                }
                while (dir >= 1.0) {
                    dir -= 1.0;
                }
                int branch = (int)(dir * (double)branchCount);
                float step = (float)dir * (float)branchCount - (float)branch;
                float last = branches[branch];
                float nextBranch = branches[(branch + 1) % branchCount];
                float pow = last + (nextBranch - last) * step;
                d /= (double)pow;
                height = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                int dd = this.rockLayer.data[x | y << this.topLayer.getSizeLevel()];
                float hh = (float)height / 10.0f - 8.0f;
                if ((d = 1.0 - d) < 0.0) {
                    d = 0.0;
                }
                if ((d = Math.sin(d * Math.PI) * 2.0 - 1.0) < 0.0) {
                    d = 0.0;
                }
                if ((n = (float)noise.perlinNoise((double)x / 2.0, (double)y / 2.0)) > 0.5f) {
                    n -= (n - 0.5f) * 2.0f;
                }
                if ((n /= 0.5f) < 0.0f) {
                    n = 0.0f;
                }
                hh = (float)((double)hh + (double)(n * (float)(x1 - x0) / 8.0f) * d);
                this.rockLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd));
                changes = this.addToChanges(changes, x, y);
                float ddd = (float)od / 16.0f;
                if (ddd < 1.0f) {
                    if ((ddd = ddd * 2.0f - 1.0f) > 1.0f) {
                        ddd = 1.0f;
                    }
                    if (ddd < 0.0f) {
                        ddd = 0.0f;
                    }
                    dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                    float hh1 = Tiles.decodeHeightAsFloat(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                    hh = Tiles.decodeHeightAsFloat(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()]);
                    hh += (hh1 - hh) * ddd;
                    this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd));
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                hh = Tiles.decodeHeightAsFloat(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                hh = (hh = hh * 0.5f + (float)((int)hh / 2 * 2) * 0.5f) > 0.0f ? (hh += 0.07f) : (hh -= 0.07f);
                this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(hh, Tiles.decodeType(dd), Tiles.decodeData(dd));
                changes = this.addToChanges(changes, x, y);
            }
        }
        for (x = x0; x < x1; ++x) {
            xd = (double)(x - xm) * 2.0 / (double)(x1 - x0);
            for (y = y0; y < y1; ++y) {
                int dd;
                yd = (double)(y - ym) * 2.0 / (double)(y1 - y0);
                d = Math.sqrt(xd * xd + yd * yd);
                od = d * (double)(x1 - x0);
                boolean rock = true;
                for (int xx = 0; xx < 2; ++xx) {
                    for (int yy = 0; yy < 2; ++yy) {
                        short height2 = Tiles.decodeHeight(this.topLayer.data[x | y << this.topLayer.getSizeLevel()]);
                        short groundHeight = Tiles.decodeHeight(this.rockLayer.data[x | y << this.topLayer.getSizeLevel()]);
                        if (groundHeight < height2) {
                            rock = false;
                            continue;
                        }
                        int dd2 = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                        this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(groundHeight, Tiles.decodeType(dd2), Tiles.decodeData(dd2));
                        changes = this.addToChanges(changes, x, y);
                    }
                }
                if (!rock) continue;
                float ddd = (float)od / 16.0f;
                if (ddd < 1.0f) {
                    dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                    this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_LAVA.id, (byte)-1);
                    changes = this.addToChanges(changes, x, y);
                    continue;
                }
                dd = this.topLayer.data[x | y << this.topLayer.getSizeLevel()];
                this.topLayer.data[x | y << this.topLayer.getSizeLevel()] = Tiles.encode(Tiles.decodeHeight(dd), Tiles.Tile.TILE_ROCK.id, (byte)0);
                changes = this.addToChanges(changes, x, y);
            }
        }
        return changes;
    }
}

