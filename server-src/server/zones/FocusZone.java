/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FocusZone
extends Zone
implements TimeConstants {
    private static final String loadAll = "SELECT * FROM FOCUSZONES";
    private static final String addZone = "INSERT INTO FOCUSZONES (STARTX,STARTY,ENDX,ENDY,TYPE,NAME,DESCRIPTION) VALUES (?,?,?,?,?,?,?)";
    private static final String deleteZone = "DELETE FROM FOCUSZONES WHERE STARTX=? AND STARTY=? AND ENDX=? AND ENDY=? AND TYPE=? AND NAME=?";
    private static final Set<FocusZone> focusZones = new HashSet<FocusZone>();
    private static final Logger logger = Logger.getLogger(FocusZone.class.getName());
    private final byte type;
    public static final byte TYPE_NONE = 0;
    public static final byte TYPE_VOLCANO = 1;
    public static final byte TYPE_PVP = 2;
    public static final byte TYPE_NAME = 3;
    public static final byte TYPE_NAME_POPUP = 4;
    public static final byte TYPE_NON_PVP = 5;
    public static final byte TYPE_PVP_HOTA = 6;
    public static final byte TYPE_PVP_BATTLECAMP = 7;
    public static final byte TYPE_FLATTEN_DIRT = 8;
    public static final byte TYPE_HOUSE_WOOD = 9;
    public static final byte TYPE_HOUSE_STONE = 10;
    public static final byte TYPE_PREM_SPAWN = 11;
    public static final byte TYPE_NO_BUILD = 12;
    public static final byte TYPE_TALLWALLS = 13;
    public static final byte TYPE_FOG = 14;
    public static final byte TYPE_FLATTEN_ROCK = 15;
    public static final byte TYPE_REPLENISH_DIRT = 16;
    public static final byte TYPE_REPLENISH_TREES = 17;
    public static final byte TYPE_REPLENISH_ORES = 18;
    private int polls = 0;
    private Item projectile = null;
    private int pollSecondLanded = 0;
    private final String name;
    private final String description;

    public FocusZone(int aStartX, int aEndX, int aStartY, int aEndY, byte zoneType, String aName, String aDescription, boolean save) {
        super(aStartX, aEndX, aStartY, aEndY, true);
        this.name = aName;
        this.description = aDescription;
        this.type = zoneType;
        if (save) {
            try {
                this.save();
                focusZones.add(this);
            }
            catch (IOException iox) {
                logger.log(Level.INFO, iox.getMessage(), iox);
            }
        }
    }

    public final String getName() {
        return this.name;
    }

    public final String getDescription() {
        return this.description;
    }

    public final boolean isPvP() {
        return this.type == 2 || this.type == 6;
    }

    public final boolean isNonPvP() {
        return this.type == 5;
    }

    public final boolean isNamePopup() {
        return this.type == 4;
    }

    public final boolean isName() {
        return this.type == 3 || this.type == 7;
    }

    public final boolean isBattleCamp() {
        return this.type == 7;
    }

    public final boolean isPvPHota() {
        return this.type == 6;
    }

    public final boolean isPremSpawnOnly() {
        return this.type == 11;
    }

    public final boolean isNoBuild() {
        return this.type == 12;
    }

    public final boolean isFog() {
        return this.type == 14;
    }

    public final boolean isType(byte wantedType) {
        return this.type == wantedType;
    }

    @Override
    void load() throws IOException {
    }

    public static void pollAll() {
        for (FocusZone fz : focusZones) {
            fz.poll();
        }
    }

    public static final Set<FocusZone> getZonesAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            HashSet<FocusZone> toReturn = new HashSet<FocusZone>();
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley)) continue;
                toReturn.add(fz);
            }
            return toReturn;
        }
        return focusZones;
    }

    public static final boolean isPvPZoneAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isPvP()) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final boolean isNonPvPZoneAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isNonPvP()) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final boolean isPremSpawnOnlyZoneAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isPremSpawnOnly()) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final boolean isNoBuildZoneAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isNoBuild()) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final boolean isFogZoneAt(int tilex, int tiley) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isFog()) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final boolean isZoneAt(int tilex, int tiley, byte wantedType) {
        if (focusZones.size() > 0) {
            for (FocusZone fz : focusZones) {
                if (!fz.covers(tilex, tiley) || !fz.isType(wantedType)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    public static final FocusZone[] getAllZones() {
        return focusZones.toArray(new FocusZone[focusZones.size()]);
    }

    public static final FocusZone getHotaZone() {
        for (FocusZone fz : FocusZone.getAllZones()) {
            if (!fz.isPvPHota()) continue;
            return fz;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void loadAll() {
        long now = System.nanoTime();
        int numberOfZonesLoaded = 0;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(loadAll);
            rs = ps.executeQuery();
            while (rs.next()) {
                FocusZone fz = new FocusZone(rs.getInt("STARTX"), rs.getInt("ENDX"), rs.getInt("STARTY"), rs.getInt("ENDY"), rs.getByte("TYPE"), rs.getString("NAME"), rs.getString("DESCRIPTION"), false);
                focusZones.add(fz);
                ++numberOfZonesLoaded;
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Problem loading focus zone, count is " + numberOfZonesLoaded + " due to " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
                logger.log(Level.INFO, "Loaded " + numberOfZonesLoaded + " focus zones. It took " + lElapsedTime + " millis.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
            logger.log(Level.INFO, "Loaded " + numberOfZonesLoaded + " focus zones. It took " + lElapsedTime + " millis.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
        logger.log(Level.INFO, "Loaded " + numberOfZonesLoaded + " focus zones. It took " + lElapsedTime + " millis.");
    }

    @Override
    void loadFences() throws IOException {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void delete() throws IOException {
        if (FocusZone.getHotaZone() == this) {
            Hota.destroyHota();
        }
        focusZones.remove(this);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(deleteZone);
            ps.setInt(1, this.startX);
            ps.setInt(2, this.startY);
            ps.setInt(3, this.endX);
            ps.setInt(4, this.endY);
            ps.setByte(5, this.type);
            ps.setString(6, this.name);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, sqex.getMessage(), sqex);
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
    @Override
    void save() throws IOException {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(addZone);
            ps.setInt(1, this.startX);
            ps.setInt(2, this.startY);
            ps.setInt(3, this.endX);
            ps.setInt(4, this.endY);
            ps.setByte(5, this.type);
            ps.setString(6, this.name);
            ps.setString(7, this.description);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, sqex.getMessage(), sqex);
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

    public void poll() {
        block34: {
            block40: {
                block39: {
                    block35: {
                        block38: {
                            block37: {
                                block36: {
                                    ++this.polls;
                                    if (this.type != 1 || this.polls % 5 != 0) break block35;
                                    boolean foundLava = false;
                                    block15: for (int x = this.startX; x < this.endX; ++x) {
                                        for (int y = this.startY; y < this.endY; ++y) {
                                            if (Tiles.decodeType(Server.caveMesh.getTile(x, y)) == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) {
                                                block17: for (int xx = -1; xx <= 1; ++xx) {
                                                    for (int yy = -1; yy <= 1; ++yy) {
                                                        if (xx == 0 && yy == 0 || xx != 0 && yy != 0 || Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x + xx, y + yy)))) continue;
                                                        logger.log(Level.INFO, "Lava flow at " + (x + xx) + "," + (y + yy));
                                                        Terraforming.setAsRock(x + xx, y + yy, true, true);
                                                        foundLava = true;
                                                        continue block17;
                                                    }
                                                }
                                            }
                                            if (foundLava) continue block15;
                                        }
                                    }
                                    if (this.pollSecondLanded <= 0 || this.polls < this.pollSecondLanded) break block36;
                                    if (this.projectile != null) {
                                        try {
                                            Zone z = Zones.getZone(this.projectile.getTileX(), this.projectile.getTileY(), true);
                                            z.addItem(this.projectile);
                                            logger.log(Level.INFO, "Added projectile to " + this.projectile.getTileX() + "," + this.projectile.getTileY());
                                        }
                                        catch (NoSuchZoneException nsz) {
                                            logger.log(Level.WARNING, nsz.getMessage(), nsz);
                                        }
                                        this.projectile = null;
                                    }
                                    this.pollSecondLanded = 0;
                                    this.polls = 0;
                                    break block34;
                                }
                                if ((long)this.polls != 42600L) break block37;
                                Server.getInstance().broadCastNormal(this.name + " rumbles.");
                                break block34;
                            }
                            if ((long)this.polls != 43200L) break block38;
                            Server.getInstance().broadCastNormal(this.name + " rumbles intensely.");
                            break block34;
                        }
                        if ((long)this.polls < 43200L || Server.rand.nextInt(3600) != 0) break block34;
                        try {
                            this.projectile = ItemFactory.createItem(692, 80.0f + Server.rand.nextFloat() * 20.0f, null);
                            int centerX = this.getStartX() + this.getSize() / 2;
                            int centerY = this.getStartY() + this.getSize() / 2;
                            int randX = Zones.safeTileX(centerX - 10 + Server.rand.nextInt(21));
                            int randY = Zones.safeTileY(centerY - 10 + Server.rand.nextInt(21));
                            int landX = Zones.safeTileX(randX - 100 + Server.rand.nextInt(200));
                            int landY = Zones.safeTileY(randY - 100 + Server.rand.nextInt(200));
                            int secondsInAir = Math.max(5, Math.max(Math.abs(randX - landX), Math.abs(randY - landY)) / 10);
                            this.pollSecondLanded = this.polls + secondsInAir;
                            float sx = randX * 4 + 2;
                            float sy = randY * 4 + 2;
                            float ex = landX * 4 + 2;
                            float ey = landY * 4 + 2;
                            float rot = Server.rand.nextFloat() * 360.0f;
                            logger.log(Level.INFO, "Creating projectile from " + randX + "," + randY + " to " + landX + "," + landY);
                            try {
                                float sh = Zones.calculateHeight(sx, sy, true) - 10.0f;
                                float eh = Zones.calculateHeight(ex, ey, true);
                                this.projectile.setPosXYZRotation(ex, ey, eh, rot);
                                Player[] players = Players.getInstance().getPlayers();
                                for (int x = 0; x < players.length; ++x) {
                                    if (!players[x].isWithinDistanceTo(sx, sy, sh, 500.0f) && !players[x].isWithinDistanceTo(ex, ey, eh, 500.0f)) continue;
                                    players[x].getCommunicator().sendProjectile(this.projectile.getWurmId(), (byte)3, this.projectile.getModelName(), this.projectile.getName(), this.projectile.getMaterial(), sx, sy, sh, rot, (byte)0, landX, landY, eh, -10L, -10L, secondsInAir, secondsInAir);
                                }
                                break block34;
                            }
                            catch (NoSuchZoneException nsz) {
                                logger.log(Level.WARNING, nsz.getMessage(), nsz);
                                this.projectile = null;
                                this.pollSecondLanded = 0;
                                this.polls = 0;
                            }
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, fe.getMessage(), fe);
                            this.projectile = null;
                            this.pollSecondLanded = 0;
                            this.polls = 0;
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, nst.getMessage(), nst);
                            this.projectile = null;
                            this.pollSecondLanded = 0;
                            this.polls = 0;
                        }
                        break block34;
                    }
                    if (this.type != 16) break block39;
                    if ((long)this.polls % 900L != 0L) break block34;
                    float avgHeight = (Zones.getHeightForNode(this.getStartX(), this.getStartY(), 1) + Zones.getHeightForNode(this.getStartX(), this.getEndY() + 1, 1) + Zones.getHeightForNode(this.getEndX() + 1, this.getStartY(), 1) + Zones.getHeightForNode(this.getEndX() + 1, this.getEndY() + 1, 1)) / 4.0f;
                    for (int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
                        for (int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
                            int tile = Server.surfaceMesh.getTile(tileX, tileY);
                            byte type = Tiles.decodeType(tile);
                            if (type != Tiles.Tile.TILE_DIRT.id && type != Tiles.Tile.TILE_DIRT_PACKED.id && type != Tiles.Tile.TILE_SAND.id && !Tiles.isGrassType(type)) continue;
                            short actualHeight = Tiles.decodeHeight(tile);
                            if ((float)actualHeight > avgHeight * 10.0f + 5.0f) {
                                Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode((short)(actualHeight - 1), type, Tiles.decodeData(tile)));
                            } else {
                                if (!((float)actualHeight < avgHeight * 10.0f - 5.0f)) continue;
                                Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode((short)(actualHeight + 1), type, Tiles.decodeData(tile)));
                            }
                            Players.getInstance().sendChangedTile(tileX, tileY, true, true);
                            try {
                                Zone toCheckForChange = Zones.getZone(tileX, tileY, true);
                                toCheckForChange.changeTile(tileX, tileY);
                                continue;
                            }
                            catch (NoSuchZoneException nsz) {
                                logger.log(Level.INFO, "no such zone?: " + tileX + ", " + tileY, nsz);
                            }
                        }
                    }
                    break block34;
                }
                if (this.type != 17) break block40;
                if ((long)this.polls % 300L != 0L) break block34;
                for (int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
                    for (int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
                        int tile = Server.surfaceMesh.getTile(tileX, tileY);
                        byte type = Tiles.decodeType(tile);
                        if (Tiles.isTree(type)) {
                            byte age = FoliageAge.getAgeAsByte(Tiles.decodeData(tile));
                            if (age > FoliageAge.MATURE_THREE.getAgeId()) continue;
                            byte newData = Tiles.encodeTreeData((byte)(age + 1), false, false, GrassData.GrowthTreeStage.decodeTileData(Tiles.decodeData(tile)));
                            Server.surfaceMesh.setTile(tileX, tileY, Tiles.encode(Tiles.decodeHeight(tile), Tiles.decodeType(tile), newData));
                            Players.getInstance().sendChangedTile(tileX, tileY, true, false);
                            continue;
                        }
                        boolean skip = false;
                        for (int x = tileX - 1; x < tileX + 1; ++x) {
                            for (int y = tileY - 1; y < tileY + 1; ++y) {
                                if (!Tiles.isTree(Tiles.decodeType(Server.surfaceMesh.getTile(x, y))) || tileX != 0 && tileY != 0) continue;
                                skip = true;
                            }
                        }
                        if (skip) continue;
                        TreeData.TreeType treeType = TreeData.TreeType.BIRCH;
                        switch (Server.rand.nextInt(5)) {
                            case 0: {
                                treeType = TreeData.TreeType.LINDEN;
                                break;
                            }
                            case 1: {
                                treeType = TreeData.TreeType.PINE;
                                break;
                            }
                            case 2: {
                                treeType = TreeData.TreeType.WALNUT;
                                break;
                            }
                            case 3: {
                                treeType = TreeData.TreeType.CEDAR;
                            }
                        }
                        byte newData = Tiles.encodeTreeData(FoliageAge.YOUNG_ONE, false, false, GrassData.GrowthTreeStage.SHORT);
                        Server.setSurfaceTile(tileX, tileY, Tiles.decodeHeight(tile), treeType.asNormalTree(), newData);
                        Server.setWorldResource(tileX, tileY, 0);
                        Players.getInstance().sendChangedTile(tileX, tileY, true, true);
                    }
                }
                break block34;
            }
            if (this.type == 18 && (long)this.polls % 900L == 0L) {
                for (int tileX = this.getStartX() + 1; tileX < this.getEndX(); ++tileX) {
                    for (int tileY = this.getStartY() + 1; tileY < this.getEndY(); ++tileY) {
                        int resource;
                        int tile = Server.caveMesh.getTile(tileX, tileY);
                        byte type = Tiles.decodeType(tile);
                        if (!Tiles.isOreCave(type) || (resource = Server.getCaveResource(tileX, tileY)) >= 1000) continue;
                        resource = Server.rand.nextInt(10000) + 10000;
                        Server.setCaveResource(tileX, tileY, resource);
                    }
                }
            }
        }
    }
}

