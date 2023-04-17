/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.DbDoor;
import com.wurmonline.server.structures.DbFloor;
import com.wurmonline.server.structures.DbStructure;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Structures
implements MiscConstants,
CounterTypes {
    private static final String GET_STRUCTURES = "SELECT * FROM STRUCTURES";
    private static Map<Long, Structure> structures;
    private static Map<Long, Structure> bridges;
    private static final Structure[] emptyStructures;
    private static final Logger logger;

    private Structures() {
    }

    public static int getNumberOfStructures() {
        if (structures != null) {
            return structures.size();
        }
        return 0;
    }

    public static final Structure[] getAllStructures() {
        if (structures == null) {
            return emptyStructures;
        }
        return structures.values().toArray(new Structure[structures.size()]);
    }

    public static final Structure[] getManagedBuildingsFor(Player player, int villageId, boolean includeAll) {
        if (structures == null) {
            return emptyStructures;
        }
        HashSet<Structure> buildings = new HashSet<Structure>();
        for (Structure structure : structures.values()) {
            if (!structure.isTypeHouse()) continue;
            if (structure.canManage(player)) {
                buildings.add(structure);
            }
            if (includeAll && (villageId >= 0 && structure.getVillageId() == villageId || structure.isActualOwner(player.getWurmId()))) {
                buildings.add(structure);
            }
            if (structure.getWritid() == -10L || !structure.isActualOwner(player.getWurmId())) continue;
            Items.destroyItem(structure.getWritId());
            structure.setWritid(-10L, true);
        }
        return buildings.toArray(new Structure[buildings.size()]);
    }

    public static final Structure[] getOwnedBuildingFor(Player player) {
        if (structures == null) {
            return emptyStructures;
        }
        HashSet<Structure> buildings = new HashSet<Structure>();
        for (Structure structure : structures.values()) {
            if (!structure.isTypeHouse() || !structure.isOwner(player) && !structure.isActualOwner(player.getWurmId())) continue;
            buildings.add(structure);
        }
        return buildings.toArray(new Structure[buildings.size()]);
    }

    public static final Structure getStructureOrNull(long id) {
        Structure structure = null;
        if (structures == null) {
            structures = new ConcurrentHashMap<Long, Structure>();
        } else {
            structure = structures.get(new Long(id));
        }
        if (structure == null && WurmId.getType(id) == 4) {
            try {
                structure = Structures.loadStructure(id);
                Structures.addStructure(structure);
            }
            catch (IOException iOException) {
            }
            catch (NoSuchStructureException noSuchStructureException) {
                // empty catch block
            }
        }
        return structure;
    }

    public static final Structure getStructure(long id) throws NoSuchStructureException {
        Structure structure = Structures.getStructureOrNull(id);
        if (structure == null) {
            throw new NoSuchStructureException("No such structure.");
        }
        return structure;
    }

    public static void addStructure(Structure structure) {
        if (structures == null) {
            structures = new ConcurrentHashMap<Long, Structure>();
        }
        structures.put(new Long(structure.getWurmId()), structure);
        if (structure.isTypeBridge()) {
            Structures.addBridge(structure);
        }
    }

    public static final void addBridge(Structure bridge) {
        if (bridges == null) {
            bridges = new ConcurrentHashMap<Long, Structure>();
        }
        bridges.put(new Long(bridge.getWurmId()), bridge);
    }

    public static void removeBridge(long id) {
        if (bridges != null) {
            bridges.remove(new Long(id));
        }
    }

    public static final Structure getBridge(long id) {
        Structure bridge = null;
        if (bridges != null) {
            bridge = bridges.get(new Long(id));
        }
        return bridge;
    }

    public static void removeStructure(long id) {
        if (structures != null) {
            structures.remove(new Long(id));
        }
    }

    public static final Structure createStructure(byte theStructureType, String name, long id, int startx, int starty, boolean surfaced) {
        DbStructure toReturn = null;
        toReturn = new DbStructure(theStructureType, name, id, startx, starty, surfaced);
        Structures.addStructure(toReturn);
        return toReturn;
    }

    private static final Structure loadStructure(long id) throws IOException, NoSuchStructureException {
        DbStructure toReturn = null;
        toReturn = new DbStructure(id);
        Structures.addStructure(toReturn);
        return toReturn;
    }

    public static Structure getStructureForWrit(long writId) throws NoSuchStructureException {
        if (writId == -10L) {
            throw new NoSuchStructureException("No structure for writid " + writId);
        }
        for (Structure s : structures.values()) {
            if (s.getWritId() != writId) continue;
            return s;
        }
        throw new NoSuchStructureException("No structure for writid " + writId);
    }

    public static void endLoadAll() {
        if (structures != null) {
            for (Structure struct : structures.values()) {
                try {
                    struct.endLoading();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage() + ": " + struct.getWurmId() + " writ " + struct.getWritid());
                }
            }
        }
    }

    public static void loadAllStructures() throws IOException {
        logger.info("Loading all Structures");
        long start = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GET_STRUCTURES);
            rs = ps.executeQuery();
            while (rs.next()) {
                long writid;
                int villageId;
                int settings;
                long ownerId;
                String planner;
                boolean allowsKingdom;
                boolean allowsAllies;
                boolean allowsCitizens;
                boolean finalfinished;
                boolean finished;
                String name;
                byte roof;
                boolean surfaced;
                byte structureType;
                long wurmid;
                block9: {
                    wurmid = rs.getLong("WURMID");
                    structureType = rs.getByte("STRUCTURETYPE");
                    surfaced = rs.getBoolean("SURFACED");
                    roof = rs.getByte("ROOF");
                    name = rs.getString("NAME");
                    if (name == null) {
                        name = "Unknown structure";
                    }
                    if (name.length() >= 50) {
                        name = name.substring(0, 49);
                    }
                    finished = rs.getBoolean("FINISHED");
                    finalfinished = rs.getBoolean("FINFINISHED");
                    allowsCitizens = rs.getBoolean("ALLOWSVILLAGERS");
                    allowsAllies = rs.getBoolean("ALLOWSALLIES");
                    allowsKingdom = rs.getBoolean("ALLOWSKINGDOM");
                    planner = rs.getString("PLANNER");
                    ownerId = rs.getLong("OWNERID");
                    settings = rs.getInt("SETTINGS");
                    villageId = rs.getInt("VILLAGE");
                    writid = -10L;
                    try {
                        writid = rs.getLong("WRITID");
                    }
                    catch (Exception nsi) {
                        if (structureType != 0) break block9;
                        logger.log(Level.INFO, "No writ for house with id:" + wurmid + " creating new after loading.", nsi);
                    }
                }
                Structures.addStructure(new DbStructure(structureType, name, wurmid, surfaced, roof, finished, finalfinished, writid, planner, ownerId, settings, villageId, allowsCitizens, allowsAllies, allowsKingdom));
            }
        }
        catch (SQLException sqex) {
            try {
                throw new IOException(sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                int numberOfStructures = structures != null ? structures.size() : 0;
                logger.log(Level.INFO, "Structures loaded. Number of structures=" + numberOfStructures + ". That took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms.");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        int numberOfStructures = structures != null ? structures.size() : 0;
        logger.log(Level.INFO, "Structures loaded. Number of structures=" + numberOfStructures + ". That took " + (float)(System.nanoTime() - start) / 1000000.0f + " ms.");
    }

    public static Structure getStructureForTile(int tilex, int tiley, boolean onSurface) {
        if (structures != null) {
            for (Structure s : structures.values()) {
                if (s.isOnSurface() != onSurface || !s.contains(tilex, tiley)) continue;
                return s;
            }
        }
        return null;
    }

    public static Structure getBuildingForTile(int tilex, int tiley) {
        if (structures != null) {
            for (Structure s : structures.values()) {
                if (!s.contains(tilex, tiley)) continue;
                return s;
            }
        }
        return null;
    }

    public static final void createRandomStructure(Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname) {
        if (creator.getCurrentTile() == null || creator.getCurrentTile().getStructure() == null) {
            try {
                Structure struct = Structures.createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);
                for (int currx = stx; currx <= endtx; ++currx) {
                    for (int curry = sty; curry <= endty; ++curry) {
                        if (currx == stx && (curry == sty || Server.rand.nextInt(3) >= 2)) continue;
                        VolaTile vtile = Zones.getOrCreateTile(currx, curry, true);
                        struct.addBuildTile(vtile, false);
                        struct.clearAllWallsAndMakeWallsForStructureBorder(vtile);
                    }
                }
                float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
                struct.makeFinal(creator, sname);
                for (VolaTile bt : struct.getStructureTiles()) {
                    StructureTypeEnum wtype = StructureTypeEnum.SOLID;
                    if (Server.rand.nextInt(2) == 0) {
                        wtype = StructureTypeEnum.WINDOW;
                    }
                    for (Wall plan : bt.getWalls()) {
                        if (!plan.isHorizontal() && plan.getStartY() == creator.getTileY() && rot <= 315.0f && rot >= 235.0f) {
                            wtype = StructureTypeEnum.DOOR;
                        }
                        if (plan.isHorizontal() && plan.getStartX() == creator.getTileX() && (rot >= 315.0f && rot <= 360.0f || rot >= 0.0f && rot <= 45.0f)) {
                            wtype = StructureTypeEnum.DOOR;
                        }
                        if (plan.isHorizontal() && plan.getStartX() == creator.getTileX() && rot >= 135.0f && rot <= 215.0f) {
                            wtype = StructureTypeEnum.DOOR;
                        }
                        if (!plan.isHorizontal() && plan.getStartY() == creator.getTileY() && rot <= 135.0f && rot >= 45.0f) {
                            wtype = StructureTypeEnum.DOOR;
                        }
                        if (material == 15) {
                            plan.setMaterial(StructureMaterialEnum.STONE);
                        } else {
                            plan.setMaterial(StructureMaterialEnum.WOOD);
                        }
                        plan.setType(wtype);
                        plan.setQualityLevel(80.0f);
                        plan.setState(StructureStateEnum.FINISHED);
                        bt.updateWall(plan);
                        if (!plan.isDoor()) continue;
                        DbDoor door = new DbDoor(plan);
                        door.setStructureId(struct.getWurmId());
                        struct.addDoor(door);
                        ((Door)door).save();
                        door.addToTiles();
                    }
                }
                struct.setFinished(true);
                struct.setFinalFinished(true);
                for (VolaTile bt : struct.getStructureTiles()) {
                    DbFloor floor = new DbFloor(StructureConstants.FloorType.FLOOR, bt.getTileX(), bt.getTileY(), 0, 80.0f, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0);
                    floor.setFloorState(StructureConstants.FloorState.COMPLETED);
                    bt.addFloor(floor);
                    ((Floor)floor).save();
                    DbFloor roof = new DbFloor(StructureConstants.FloorType.ROOF, bt.getTileX(), bt.getTileY(), 30, 80.0f, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0);
                    roof.setFloorState(StructureConstants.FloorState.COMPLETED);
                    bt.addFloor(roof);
                    ((Floor)roof).save();
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "exception " + ex, ex);
                creator.getCommunicator().sendAlertServerMessage(ex.getMessage());
            }
        }
    }

    public static final void createSquareStructure(Creature creator, int stx, int endtx, int sty, int endty, int centerx, int centery, byte material, String sname) {
        if (creator.getCurrentTile() == null || creator.getCurrentTile().getStructure() == null) {
            try {
                VolaTile vtile;
                int currx;
                Structure struct = Structures.createStructure((byte)0, sname, WurmId.getNextPlanId(), centerx, centery, true);
                for (int currx2 = stx; currx2 <= endtx; ++currx2) {
                    for (int curry = sty; curry <= endty; ++curry) {
                        VolaTile vtile2 = Zones.getOrCreateTile(currx2, curry, true);
                        struct.addBuildTile(vtile2, false);
                        struct.clearAllWallsAndMakeWallsForStructureBorder(vtile2);
                    }
                }
                float rot = Creature.normalizeAngle(creator.getStatus().getRotation());
                struct.makeFinal(creator, sname);
                for (currx = stx; currx <= endtx; ++currx) {
                    for (int curry = sty; curry <= endty; ++curry) {
                        DbDoor door;
                        vtile = Zones.getOrCreateTile(currx, curry, true);
                        StructureTypeEnum wtype = StructureTypeEnum.SOLID;
                        if (Server.rand.nextInt(2) == 0) {
                            wtype = StructureTypeEnum.WINDOW;
                        }
                        if (currx == stx) {
                            for (Wall plan : vtile.getWalls()) {
                                if (plan.isHorizontal() || plan.getStartX() != currx) continue;
                                if (curry == creator.getTileY() && rot <= 315.0f && rot >= 235.0f) {
                                    wtype = StructureTypeEnum.DOOR;
                                }
                                if (material == 15) {
                                    plan.setMaterial(StructureMaterialEnum.STONE);
                                } else {
                                    plan.setMaterial(StructureMaterialEnum.WOOD);
                                }
                                plan.setType(wtype);
                                plan.setQualityLevel(80.0f);
                                plan.setState(StructureStateEnum.FINISHED);
                                vtile.updateWall(plan);
                                if (!plan.isDoor()) continue;
                                door = new DbDoor(plan);
                                door.setStructureId(struct.getWurmId());
                                struct.addDoor(door);
                                ((Door)door).save();
                                door.addToTiles();
                            }
                        }
                        if (curry == sty) {
                            for (Wall plan : vtile.getWalls()) {
                                if (!plan.isHorizontal() || plan.getStartY() != curry) continue;
                                if (currx == creator.getTileX() && (rot >= 315.0f && rot <= 360.0f || rot >= 0.0f && rot <= 45.0f)) {
                                    wtype = StructureTypeEnum.DOOR;
                                }
                                if (material == 15) {
                                    plan.setMaterial(StructureMaterialEnum.STONE);
                                } else {
                                    plan.setMaterial(StructureMaterialEnum.WOOD);
                                }
                                plan.setType(wtype);
                                plan.setQualityLevel(80.0f);
                                plan.setState(StructureStateEnum.FINISHED);
                                vtile.updateWall(plan);
                                if (!plan.isDoor()) continue;
                                door = new DbDoor(plan);
                                door.setStructureId(struct.getWurmId());
                                struct.addDoor(door);
                                ((Door)door).save();
                                door.addToTiles();
                            }
                        }
                        if (curry == endty) {
                            for (Wall plan : vtile.getWalls()) {
                                if (!plan.isHorizontal() || plan.getStartY() != curry + 1) continue;
                                if (currx == creator.getTileX() && rot >= 135.0f && rot <= 215.0f) {
                                    wtype = StructureTypeEnum.DOOR;
                                }
                                if (material == 15) {
                                    plan.setMaterial(StructureMaterialEnum.STONE);
                                } else {
                                    plan.setMaterial(StructureMaterialEnum.WOOD);
                                }
                                plan.setType(wtype);
                                plan.setQualityLevel(80.0f);
                                plan.setState(StructureStateEnum.FINISHED);
                                vtile.updateWall(plan);
                                if (!plan.isDoor()) continue;
                                door = new DbDoor(plan);
                                door.setStructureId(struct.getWurmId());
                                struct.addDoor(door);
                                ((Door)door).save();
                                door.addToTiles();
                            }
                        }
                        if (currx != endtx) continue;
                        for (Wall plan : vtile.getWalls()) {
                            if (plan.isHorizontal() || plan.getStartX() != currx + 1) continue;
                            if (curry == creator.getTileY() && rot <= 135.0f && rot >= 45.0f) {
                                wtype = StructureTypeEnum.DOOR;
                            }
                            if (material == 15) {
                                plan.setMaterial(StructureMaterialEnum.STONE);
                            } else {
                                plan.setMaterial(StructureMaterialEnum.WOOD);
                            }
                            plan.setType(wtype);
                            plan.setQualityLevel(80.0f);
                            plan.setState(StructureStateEnum.FINISHED);
                            vtile.updateWall(plan);
                            if (!plan.isDoor()) continue;
                            door = new DbDoor(plan);
                            door.setStructureId(struct.getWurmId());
                            struct.addDoor(door);
                            ((Door)door).save();
                            door.addToTiles();
                        }
                    }
                }
                struct.setFinished(true);
                struct.setFinalFinished(true);
                for (currx = stx; currx <= endtx; ++currx) {
                    for (int curry = sty; curry <= endty; ++curry) {
                        vtile = Zones.getOrCreateTile(currx, curry, true);
                        DbFloor floor = new DbFloor(StructureConstants.FloorType.FLOOR, currx, curry, 0, 80.0f, struct.getWurmId(), StructureConstants.FloorMaterial.WOOD, 0);
                        floor.setFloorState(StructureConstants.FloorState.COMPLETED);
                        vtile.addFloor(floor);
                        ((Floor)floor).save();
                        DbFloor roof = new DbFloor(StructureConstants.FloorType.ROOF, currx, curry, 30, 80.0f, struct.getWurmId(), StructureConstants.FloorMaterial.THATCH, 0);
                        roof.setFloorState(StructureConstants.FloorState.COMPLETED);
                        vtile.addFloor(roof);
                        ((Floor)roof).save();
                    }
                }
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "exception " + ex, ex);
                creator.getCommunicator().sendAlertServerMessage(ex.getMessage());
            }
        }
    }

    static {
        emptyStructures = new Structure[0];
        logger = Logger.getLogger(Structures.class.getName());
    }
}

