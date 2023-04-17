/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.math.Vector2f;
import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.TileGrassBehaviour;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.TileTreeBehaviour;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.util.TerrainUtilities;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Terraforming
implements MiscConstants,
QuestionTypes,
ItemTypes,
CounterTypes,
ItemMaterials,
SoundNames,
VillageStatus,
TimeConstants {
    public static final String cvsversion = "$Id: Terraforming.java,v 1.61 2007-04-19 23:05:18 root Exp $";
    public static final short MAX_WATER_DIG_DEPTH = -7;
    public static final short MAX_PAVE_DEPTH = -100;
    public static final short MAX_HEIGHT_DIFF = 20;
    public static final short MAX_DIAG_HEIGHT_DIFF = 28;
    private static final Logger logger = Logger.getLogger(Terraforming.class.getName());
    private static int[][] flattenTiles = new int[4][4];
    private static int[][] rockTiles = new int[4][4];
    private static final int[] noCaveDoor = new int[]{-1, -1};
    private static int flattenImmutable = 0;
    private static byte newType = 0;
    private static byte oldType = 0;
    private static int newTile = 0;
    private static final float DIGGING_SKILL_MULT = 3.0f;
    private static final int saltUsed = 1000;
    private static final Random r = new Random();

    private Terraforming() {
    }

    static final boolean isImmutableTile(byte type) {
        return Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_PEAT.id || type == Tiles.Tile.TILE_TAR.id || type == Tiles.Tile.TILE_HOLE.id || type == Tiles.Tile.TILE_MOSS.id || type == Tiles.Tile.TILE_LAVA.id || Tiles.isMineDoor(type);
    }

    static final boolean isImmutableOrRoadTile(byte type) {
        return Terraforming.isRoad(type) || Terraforming.isImmutableTile(type);
    }

    static final boolean isTileOverriddenByDirt(byte type) {
        return type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_LAWN.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id;
    }

    public static final boolean isRoad(byte type) {
        return Tiles.isRoadType(type);
    }

    public static final boolean isSculptable(byte type) {
        return type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_KELP.id || type == Tiles.Tile.TILE_REED.id || type == Tiles.Tile.TILE_LAWN.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id;
    }

    public static final boolean isNonDiggableTile(byte type) {
        return Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_LAVA.id || Tiles.isSolidCave(type) || type == Tiles.Tile.TILE_CAVE_EXIT.id || Tiles.isMineDoor(type) || type == Tiles.Tile.TILE_HOLE.id || type == Tiles.Tile.TILE_CAVE.id;
    }

    static final boolean isTileTurnToDirt(byte type) {
        return type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_REED.id || type == Tiles.Tile.TILE_KELP.id || type == Tiles.Tile.TILE_LAWN.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id || type == Tiles.Tile.TILE_FIELD2.id;
    }

    static final boolean isCultivatable(byte type) {
        return type == Tiles.Tile.TILE_DIRT_PACKED.id || type == Tiles.Tile.TILE_MOSS.id || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MYCELIUM.id;
    }

    static final boolean isSwitchableTiles(int templateId, byte tileType) {
        return templateId == 26 && tileType == Tiles.Tile.TILE_SAND.id || templateId == 298 && tileType == Tiles.Tile.TILE_DIRT.id;
    }

    static final boolean isTileGrowTree(byte type) {
        return type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MOSS.id || type == Tiles.Tile.TILE_REED.id || type == Tiles.Tile.TILE_KELP.id;
    }

    static final boolean isTileGrowHedge(byte type) {
        return type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MOSS.id || Tiles.isTree(type) || Tiles.isBush(type) || type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_REED.id || type == Tiles.Tile.TILE_KELP.id || type == Tiles.Tile.TILE_LAWN.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id || type == Tiles.Tile.TILE_ENCHANTED_GRASS.id;
    }

    static final boolean isRockTile(byte type) {
        return Tiles.isSolidCave(type) || type == Tiles.Tile.TILE_CAVE.id || type == Tiles.Tile.TILE_CAVE_EXIT.id || type == Tiles.Tile.TILE_CLIFF.id || type == Tiles.Tile.TILE_ROCK.id || type == Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id;
    }

    static final boolean isBuildTile(byte type) {
        return !Terraforming.isRockTile(type) && type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id && type != Tiles.Tile.TILE_CLAY.id && type != Tiles.Tile.TILE_SAND.id && type != Tiles.Tile.TILE_HOLE.id && !Tiles.isTree(type) && !Tiles.isBush(type) && type != Tiles.Tile.TILE_LAVA.id && type != Tiles.Tile.TILE_MARSH.id && !Tiles.isMineDoor(type);
    }

    public static final boolean isBridgeableTile(byte type) {
        return !Tiles.isTree(type) && !Tiles.isBush(type);
    }

    public static final boolean isCaveEntrance(byte type) {
        return type == Tiles.Tile.TILE_HOLE.id || Tiles.isMineDoor(type);
    }

    static final boolean isPackable(byte type) {
        return !Terraforming.isRockTile(type) && !Terraforming.isImmutableTile(type) && type != Tiles.Tile.TILE_DIRT_PACKED.id && !Terraforming.isRoad(type) && type != Tiles.Tile.TILE_SAND.id && !Tiles.isReinforcedFloor(type);
    }

    private static final boolean isCornerDone(int x, int y, int preferredHeight) {
        if (Tiles.decodeHeight(flattenTiles[x][y]) == Tiles.decodeHeight(rockTiles[x][y])) {
            return true;
        }
        return Tiles.decodeHeight(flattenTiles[x][y]) == preferredHeight;
    }

    public static final boolean checkHouse(Creature performer, int tilex, int tiley, int xx, int yy, int preferredHeight) {
        for (int x = 0; x >= -1; --x) {
            for (int y = 0; y >= -1; --y) {
                if (!Terraforming.isCornerDone(xx + x, yy + y, preferredHeight)) {
                    try {
                        Zone zone = Zones.getZone(tilex + x, tiley + y, performer.isOnSurface());
                        VolaTile vtile = zone.getTileOrNull(tilex + x, tiley + y);
                        if (vtile == null || vtile.getStructure() == null) continue;
                        ++flattenImmutable;
                        performer.getCommunicator().sendNormalServerMessage("The structure is in the way.");
                        return true;
                    }
                    catch (NoSuchZoneException nsz) {
                        ++flattenImmutable;
                        performer.getCommunicator().sendNormalServerMessage("The water is too deep to flatten.");
                        return true;
                    }
                }
                logger.log(Level.INFO, "Corner at " + (xx + x) + "," + (yy + y) + " is ok already. Not checking");
            }
        }
        return false;
    }

    static boolean obliterateCave(Creature performer, Action act, Item source, int tilex, int tiley, int tile, float counter, int decimeterDug) {
        int lNewTile;
        boolean obliteratingCeiling;
        boolean done = false;
        boolean insta = performer.getPower() >= 5;
        byte type = Tiles.decodeType(tile);
        int rockTile = Server.rockMesh.getTile(tilex, tiley);
        short rockHeight = Tiles.decodeHeight(rockTile);
        int caveTile = Server.caveMesh.getTile(tilex, tiley);
        short caveFloor = Tiles.decodeHeight(caveTile);
        short caveCeilingHeight = (short)(Tiles.decodeData(caveTile) & 0xFF);
        int dir = (int)(act.getTarget() >> 48) & 0xFF;
        boolean bl = obliteratingCeiling = type == Tiles.Tile.TILE_CAVE.id && dir == 1;
        if (caveCeilingHeight + decimeterDug > 254) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens. 1");
            return true;
        }
        if (obliteratingCeiling) {
            if (caveFloor + caveCeilingHeight + decimeterDug >= rockHeight) {
                performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens. 2");
                return true;
            }
        } else {
            if (caveFloor - decimeterDug < -150) {
                performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens.");
                return true;
            }
            if (caveFloor == rockHeight) {
                performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens.");
                return true;
            }
        }
        done = false;
        boolean abort = false;
        if (source.getQualityLevel() < (float)(decimeterDug + 1)) {
            abort = true;
        }
        if (Terraforming.checkSculptCaveTile(lNewTile = Server.caveMesh.getTile(tilex - 1, tiley), performer, caveFloor, caveCeilingHeight, decimeterDug, obliteratingCeiling)) {
            abort = true;
        }
        if (Terraforming.checkSculptCaveTile(lNewTile = Server.caveMesh.getTile(tilex + 1, tiley), performer, caveFloor, caveCeilingHeight, decimeterDug, obliteratingCeiling)) {
            abort = true;
        }
        if (Terraforming.checkSculptCaveTile(lNewTile = Server.caveMesh.getTile(tilex, tiley - 1), performer, caveFloor, caveCeilingHeight, decimeterDug, obliteratingCeiling)) {
            abort = true;
        }
        if (Terraforming.checkSculptCaveTile(lNewTile = Server.caveMesh.getTile(tilex, tiley + 1), performer, caveFloor, caveCeilingHeight, decimeterDug, obliteratingCeiling)) {
            abort = true;
        }
        if (abort) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens. 3");
            return true;
        }
        int time = 30;
        for (int x = 0; x >= -1; --x) {
            for (int y = 0; y >= -1; --y) {
                try {
                    Zone zone = Zones.getZone(tilex + x, tiley + y, false);
                    VolaTile vtile = zone.getTileOrNull(tilex + x, tiley + y);
                    if (vtile == null) continue;
                    if (vtile.getStructure() != null) {
                        performer.getCommunicator().sendNormalServerMessage("The structure is in the way.");
                        return true;
                    }
                    if (x == 0 && y == 0) {
                        int n = 0;
                        Fence[] fenceArray = vtile.getFences();
                        int n2 = fenceArray.length;
                        if (n >= n2) continue;
                        Fence fence = fenceArray[n];
                        performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                        return true;
                    }
                    if (x == -1 && y == 0) {
                        for (Fence fence : vtile.getFences()) {
                            if (!fence.isHorizontal()) continue;
                            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                            return true;
                        }
                        continue;
                    }
                    if (y != -1 || x != 0) continue;
                    for (Fence fence : vtile.getFences()) {
                        if (fence.isHorizontal()) continue;
                        performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                        return true;
                    }
                    continue;
                }
                catch (NoSuchZoneException nsz) {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                    return true;
                }
            }
        }
        if (counter == 1.0f && !insta) {
            act.setTimeLeft(30);
            performer.getCommunicator().sendNormalServerMessage("You use the " + source.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " uses " + source.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[118].getVerbString(), true, 30);
        }
        if (counter * 10.0f > 30.0f || insta) {
            done = true;
            int newCeil = Math.min(255, caveCeilingHeight + decimeterDug);
            if (newCeil != 255 && !insta) {
                source.setQualityLevel(source.getQualityLevel() - (float)decimeterDug);
            }
            if (obliteratingCeiling) {
                Server.caveMesh.setTile(tilex, tiley, Tiles.encode(caveFloor, type, (byte)newCeil));
            } else {
                Server.caveMesh.setTile(tilex, tiley, Tiles.encode((short)(caveFloor - decimeterDug), type, (byte)newCeil));
            }
            Players.getInstance().sendChangedTile(tilex, tiley, false, true);
            for (int x = -1; x <= 0; ++x) {
                for (int y = -1; y <= 0; ++y) {
                    try {
                        Zone toCheckForChange = Zones.getZone(tilex + x, tiley + y, false);
                        toCheckForChange.changeTile(tilex + x, tiley + y);
                        continue;
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.INFO, "no such zone?: " + (tilex + x) + "," + (tiley + y), nsz);
                        performer.getCommunicator().sendNormalServerMessage("You can't mine there.");
                        return true;
                    }
                }
            }
            performer.getCommunicator().sendNormalServerMessage("You obliterate some rock.");
        }
        return done;
    }

    static boolean obliterate(Creature performer, Action act, Item source, int tilex, int tiley, int tile, float counter, int decimeterDug, MeshIO mesh) {
        boolean insta;
        boolean done = false;
        boolean bl = insta = performer.getPower() >= 5;
        if (source.getAuxData() <= 0 || source.getQualityLevel() > 99.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates strongly!");
            source.setAuxData((byte)1);
            source.setQualityLevel(Math.min(source.getQualityLevel(), 60.0f));
            return true;
        }
        if (tilex < 1 || tilex > (1 << Constants.meshSize) - 2 || tiley < 1 || tiley > (1 << Constants.meshSize) - 2) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep there.");
            return true;
        }
        byte type = Tiles.decodeType(tile);
        if (Tiles.isSolidCave(type) || type == Tiles.Tile.TILE_CAVE.id || type == Tiles.Tile.TILE_CAVE_EXIT.id) {
            return Terraforming.obliterateCave(performer, act, source, tilex, tiley, tile, counter, decimeterDug);
        }
        if (Math.abs(Terraforming.getMaxSurfaceDifference(Server.surfaceMesh.getTile(tilex, tiley), tilex, tiley)) > 60) {
            performer.getCommunicator().sendNormalServerMessage("That is too steep. Nothing happens.");
            return true;
        }
        short tileHeight = Tiles.decodeHeight(tile);
        int rockTile = Server.rockMesh.getTile(tilex, tiley);
        short rockHeight = Tiles.decodeHeight(rockTile);
        int caveTile = Server.caveMesh.getTile(tilex, tiley);
        short caveFloor = Tiles.decodeHeight(caveTile);
        int caveCeilingHeight = caveFloor + (short)(Tiles.decodeData(caveTile) & 0xFF);
        int minHeight = -5000;
        if (tileHeight - decimeterDug > -5000) {
            if (tileHeight - decimeterDug <= caveCeilingHeight) {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                return true;
            }
            done = false;
            if (!insta && source.getQualityLevel() < (float)(decimeterDug + 1)) {
                performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates, but nothing happens.");
                return true;
            }
            if (!Terraforming.isSculptable(Tiles.decodeType(tile))) {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens on the " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc + ". The " + source.getName() + " only seems to work on grass, rock, dirt and similar terrain.");
                return true;
            }
            int lNewTile = mesh.getTile(tilex - 1, tiley);
            if (Terraforming.checkSculptTile(lNewTile, performer, tileHeight, decimeterDug)) {
                return true;
            }
            lNewTile = mesh.getTile(tilex + 1, tiley);
            if (Terraforming.checkSculptTile(lNewTile, performer, tileHeight, decimeterDug)) {
                return true;
            }
            lNewTile = mesh.getTile(tilex, tiley - 1);
            if (Terraforming.checkSculptTile(lNewTile, performer, tileHeight, decimeterDug)) {
                return true;
            }
            lNewTile = mesh.getTile(tilex, tiley + 1);
            if (Terraforming.checkSculptTile(lNewTile, performer, tileHeight, decimeterDug)) {
                return true;
            }
            int time = 30;
            for (int x = 0; x >= -1; --x) {
                for (int y = 0; y >= -1; --y) {
                    try {
                        Zone zone = Zones.getZone(tilex + x, tiley + y, performer.isOnSurface());
                        VolaTile vtile = zone.getTileOrNull(tilex + x, tiley + y);
                        if (vtile == null) continue;
                        if (vtile.getStructure() != null) {
                            performer.getCommunicator().sendNormalServerMessage("The structure is in the way.");
                            return true;
                        }
                        if (x == 0 && y == 0) {
                            int n = 0;
                            Fence[] fenceArray = vtile.getFences();
                            int n2 = fenceArray.length;
                            if (n >= n2) continue;
                            Fence fence = fenceArray[n];
                            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                            return true;
                        }
                        if (x == -1 && y == 0) {
                            for (Fence fence : vtile.getFences()) {
                                if (!fence.isHorizontal()) continue;
                                performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                                return true;
                            }
                            continue;
                        }
                        if (y != -1 || x != 0) continue;
                        for (Fence fence : vtile.getFences()) {
                            if (fence.isHorizontal()) continue;
                            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is in the way.");
                            return true;
                        }
                        continue;
                    }
                    catch (NoSuchZoneException nsz) {
                        performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                        return true;
                    }
                }
            }
            if (counter == 1.0f && !insta) {
                act.setTimeLeft(30);
                performer.getCommunicator().sendNormalServerMessage("You use the " + source.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " uses " + source.getNameWithGenus() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[118].getVerbString(), true, 30);
            }
            if (counter * 10.0f > 30.0f || insta) {
                done = true;
                int clayNum = Server.getDigCount(tilex, tiley);
                if (clayNum <= 0 || clayNum > 100) {
                    clayNum = 50 + Server.rand.nextInt(50);
                }
                boolean allCornersRock = false;
                for (int x = 0; x >= -1; --x) {
                    for (int y = 0; y >= -1; --y) {
                        Item[] foundItems;
                        VolaTile atile;
                        int aa;
                        Item[] foundArtifacts;
                        boolean lChanged = false;
                        lNewTile = mesh.getTile(tilex + x, tiley + y);
                        type = Tiles.decodeType(lNewTile);
                        short newTileHeight = Tiles.decodeHeight(lNewTile);
                        rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                        rockHeight = Tiles.decodeHeight(rockTile);
                        if (x == 0 && y == 0) {
                            lChanged = true;
                            newTileHeight = (short)(newTileHeight - decimeterDug);
                            mesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, type, Tiles.decodeData(lNewTile)));
                            if (newTileHeight < rockHeight) {
                                Server.rockMesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, (short)0));
                                rockHeight = newTileHeight;
                            }
                            if (insta) {
                                performer.getCommunicator().sendNormalServerMessage("Tile " + (tilex + x) + ", " + (tiley + y) + " now at " + newTileHeight + ", rock at " + rockHeight + ".");
                            }
                        }
                        allCornersRock = Terraforming.allCornersAtRockLevel(tilex + x, tiley + y, mesh);
                        if (!Terraforming.isImmutableTile(type) && allCornersRock) {
                            lChanged = true;
                            Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_ROCK.id);
                            mesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, Tiles.Tile.TILE_ROCK.id, (byte)0));
                        } else if (Terraforming.isTileTurnToDirt(type)) {
                            lChanged = true;
                            Server.modifyFlagsByTileType(tilex + x, tiley + y, Tiles.Tile.TILE_DIRT.id);
                            mesh.setTile(tilex + x, tiley + y, Tiles.encode(newTileHeight, Tiles.Tile.TILE_DIRT.id, (byte)0));
                        }
                        if (lChanged) {
                            if (x == 0 && y == 0) {
                                source.setQualityLevel(source.getQualityLevel() - (float)decimeterDug);
                            }
                            performer.getMovementScheme().touchFreeMoveCounter();
                            Players.getInstance().sendChangedTile(tilex + x, tiley + y, performer.isOnSurface(), true);
                            try {
                                Zone toCheckForChange = Zones.getZone(tilex + x, tiley + y, performer.isOnSurface());
                                toCheckForChange.changeTile(tilex + x, tiley + y);
                            }
                            catch (NoSuchZoneException nsz) {
                                logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                            }
                        }
                        if (performer.isOnSurface() && (foundArtifacts = EndGameItems.getArtifactDugUp(tilex + x, tiley + y, (float)newTileHeight / 10.0f, allCornersRock)).length > 0) {
                            for (aa = 0; aa < foundArtifacts.length; ++aa) {
                                atile = Zones.getOrCreateTile(tilex + x, tiley + y, performer.isOnSurface());
                                atile.addItem(foundArtifacts[aa], false, false);
                                performer.getCommunicator().sendNormalServerMessage("You find something weird! You found the " + foundArtifacts[aa].getName() + "!");
                                logger.log(Level.INFO, performer.getName() + " found the " + foundArtifacts[aa].getName() + " at tile " + (tilex + x) + ", " + (tiley + y) + "! " + foundArtifacts[aa]);
                                HistoryManager.addHistory(performer.getName(), "reveals the " + foundArtifacts[aa].getName());
                                EndGameItem egi = EndGameItems.getEndGameItem(foundArtifacts[aa]);
                                if (egi == null) continue;
                                egi.setLastMoved(System.currentTimeMillis());
                                foundArtifacts[aa].setAuxData((byte)120);
                            }
                        }
                        if ((foundItems = Items.getHiddenItemsAt(tilex + x, tiley + y, (float)newTileHeight / 10.0f, true)).length <= 0) continue;
                        for (aa = 0; aa < foundItems.length; ++aa) {
                            foundItems[aa].setHidden(false);
                            Items.revealItem(foundItems[aa]);
                            atile = Zones.getOrCreateTile(tilex + x, tiley + y, performer.isOnSurface());
                            atile.addItem(foundItems[aa], false, false);
                            performer.getCommunicator().sendNormalServerMessage("You find something! You found a " + foundItems[aa].getName() + "!");
                            logger.log(Level.INFO, performer.getName() + " found a " + foundItems[aa].getName() + " at tile " + (tilex + x) + ", " + (tiley + y) + ".");
                        }
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("You obliterate some matter.");
            }
        } else {
            done = true;
            performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
        }
        return done;
    }

    public static final int getCaveDoorDifference(int digTile, int digTilex, int digTiley) {
        short difference = 0;
        short maxdifference = 0;
        short digTileHeight = Tiles.decodeHeight(digTile);
        int lNewTile = Server.surfaceMesh.getTile(digTilex, digTiley + 1);
        short height = Tiles.decodeHeight(lNewTile);
        difference = (short)Math.abs(height - digTileHeight);
        if (difference > maxdifference) {
            maxdifference = difference;
        }
        lNewTile = Server.surfaceMesh.getTile(digTilex + 1, digTiley);
        digTileHeight = Tiles.decodeHeight(lNewTile);
        lNewTile = Server.surfaceMesh.getTile(digTilex + 1, digTiley + 1);
        height = Tiles.decodeHeight(lNewTile);
        difference = (short)Math.abs(height - digTileHeight);
        if (difference > maxdifference) {
            maxdifference = difference;
        }
        return maxdifference;
    }

    public static final int getMaxSurfaceDifference(int digTile, int digTilex, int digTiley) {
        short difference = 0;
        int maxdifference = 0;
        short digTileHeight = Tiles.decodeHeight(digTile);
        int lNewTile = Server.surfaceMesh.getTile(digTilex - 1, digTiley);
        short height = Tiles.decodeHeight(lNewTile);
        difference = (short)(height - digTileHeight);
        if (Math.abs(difference) > Math.abs(maxdifference)) {
            maxdifference = difference;
        }
        if (Math.abs(difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex, digTiley + 1))) - digTileHeight)) > Math.abs(maxdifference)) {
            maxdifference = difference;
        }
        if (Math.abs(difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex, digTiley - 1))) - digTileHeight)) > Math.abs(maxdifference)) {
            maxdifference = difference;
        }
        if (Math.abs(difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex + 1, digTiley))) - digTileHeight)) > Math.abs(maxdifference)) {
            maxdifference = difference;
        }
        return maxdifference;
    }

    public static final int getMaxSurfaceDownSlope(int digTilex, int digTiley) {
        int digTile = Server.surfaceMesh.getTile(digTilex, digTiley);
        short difference = 0;
        short maxdifference = 0;
        short digTileHeight = Tiles.decodeHeight(digTile);
        int lNewTile = Server.surfaceMesh.getTile(digTilex - 1, digTiley);
        short height = Tiles.decodeHeight(lNewTile);
        difference = (short)(height - digTileHeight);
        if (difference < maxdifference) {
            maxdifference = difference;
        }
        if ((difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex, digTiley + 1))) - digTileHeight)) < maxdifference) {
            maxdifference = difference;
        }
        if ((difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex, digTiley - 1))) - digTileHeight)) < maxdifference) {
            maxdifference = difference;
        }
        if ((difference = (short)((height = Tiles.decodeHeight(lNewTile = Server.surfaceMesh.getTile(digTilex + 1, digTiley))) - digTileHeight)) < maxdifference) {
            maxdifference = difference;
        }
        return maxdifference;
    }

    public static int getTileResource(byte type) {
        int templateId = 26;
        if (type == Tiles.Tile.TILE_CLAY.id) {
            templateId = 130;
        } else if (type == Tiles.Tile.TILE_SAND.id) {
            templateId = 298;
        } else if (type == Tiles.Tile.TILE_PEAT.id) {
            templateId = 467;
        } else if (type == Tiles.Tile.TILE_TAR.id) {
            templateId = 153;
        } else if (type == Tiles.Tile.TILE_MOSS.id) {
            templateId = 479;
        }
        return templateId;
    }

    static boolean dig(Creature performer, Item source, int tilex, int tiley, int tile, float counter, boolean corner, MeshIO mesh) {
        return Terraforming.dig(performer, source, tilex, tiley, tile, counter, corner, mesh, false);
    }

    static boolean dig(Creature performer, Item source, int tilex, int tiley, int tile, float counter, boolean corner, MeshIO mesh, boolean toPile) {
        boolean done;
        block176: {
            done = false;
            boolean dredging = false;
            boolean digToPile = toPile && source != null && (!source.isDredgingTool() || source.isWand());
            try {
                int currentTileRock;
                short currentRockHeight;
                int digTiley;
                int digTilex;
                boolean insta;
                boolean bl = insta = source.isWand() && performer.getPower() >= 2;
                if (source.isDredgingTool() && (source.getTemplateId() != 176 || performer.getPositionZ() < 0.0f)) {
                    dredging = true;
                }
                if (corner) {
                    digTilex = tilex;
                    digTiley = tiley;
                } else {
                    Vector2f pos = performer.getPos2f();
                    digTilex = CoordUtils.WorldToTile(pos.x + 2.0f);
                    digTiley = CoordUtils.WorldToTile(pos.y + 2.0f);
                }
                if (digTilex < 0 || digTilex > 1 << Constants.meshSize || digTiley < 0 || digTiley > 1 << Constants.meshSize) {
                    performer.getCommunicator().sendNormalServerMessage("The water is too deep to dig.");
                    return true;
                }
                if (Features.Feature.WAGONER.isEnabled() && MethodsHighways.onWagonerCamp(digTilex, digTiley, true)) {
                    performer.getCommunicator().sendNormalServerMessage("The wagoner whips you once and tells you never to try digging here again.");
                    return true;
                }
                int digTile = mesh.getTile(digTilex, digTiley);
                byte type = Tiles.decodeType(digTile);
                int templateId = Terraforming.getTileResource(type);
                short currentTileHeight = Tiles.decodeHeight(digTile);
                if (currentTileHeight <= (currentRockHeight = Tiles.decodeHeight(currentTileRock = Server.rockMesh.getTile(digTilex, digTiley)))) {
                    performer.getCommunicator().sendNormalServerMessage("You can not dig in the solid rock.");
                    HighwayPos highwayPos = MethodsHighways.getHighwayPos(digTilex, digTiley, true);
                    if (!MethodsHighways.onHighway(highwayPos)) {
                        for (int x = 0; x >= -1; --x) {
                            for (int y = 0; y >= -1; --y) {
                                int theTile = mesh.getTile(digTilex + x, digTiley + y);
                                byte theType = Tiles.decodeType(theTile);
                                boolean allCornersRock = Terraforming.allCornersAtRockLevel(digTilex + x, digTiley + y, mesh);
                                if (Terraforming.isRockTile(theType) || Terraforming.isImmutableTile(theType) || !allCornersRock || Tiles.isTree(type) || Tiles.isBush(type)) continue;
                                float oldTileHeight = Tiles.decodeHeightAsFloat(theTile);
                                Server.modifyFlagsByTileType(digTilex + x, digTiley + y, Tiles.Tile.TILE_ROCK.id);
                                mesh.setTile(digTilex + x, digTiley + y, Tiles.encode(oldTileHeight, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                Players.getInstance().sendChangedTile(digTilex + x, digTiley + y, performer.isOnSurface(), true);
                            }
                        }
                    }
                    return true;
                }
                Village village = null;
                int encodedTile = Server.surfaceMesh.getTile(digTilex, digTiley);
                village = Zones.getVillage(digTilex, digTiley, performer.isOnSurface());
                int checkX = digTilex;
                int checkY = digTiley;
                if (village == null) {
                    checkX = (int)performer.getStatus().getPositionX() - 2 >> 2;
                    village = Zones.getVillage(checkX, checkY, performer.isOnSurface());
                }
                if (village == null) {
                    checkY = (int)performer.getStatus().getPositionY() - 2 >> 2;
                    village = Zones.getVillage(checkX, checkY, performer.isOnSurface());
                }
                if (village == null) {
                    checkX = (int)performer.getStatus().getPositionX() + 2 >> 2;
                    village = Zones.getVillage(checkX, checkY, performer.isOnSurface());
                }
                if (village != null && !village.isActionAllowed((short)144, performer, false, encodedTile, 0)) {
                    if (!Zones.isOnPvPServer(tilex, tiley)) {
                        performer.getCommunicator().sendNormalServerMessage("This action is not allowed here, because the tile is on a player owned deed that has disallowed it.", (byte)3);
                        return true;
                    }
                    if (!village.isEnemy(performer) && performer.isLegal()) {
                        performer.getCommunicator().sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.", (byte)3);
                        return true;
                    }
                }
                int weight = ItemTemplateFactory.getInstance().getTemplate(templateId).getWeightGrams();
                if (!insta) {
                    if (performer.getInventory().getNumItemsNotCoins() >= 100) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the " + ItemTemplateFactory.getInstance().getTemplate(templateId).getName() + ". You need to drop some things first.");
                        return true;
                    }
                    if (!performer.canCarry(weight)) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the " + ItemTemplateFactory.getInstance().getTemplate(templateId).getName() + ". You need to drop some things first.");
                        return true;
                    }
                    if (dredging && source.getFreeVolume() < 1000) {
                        performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is full.");
                        return true;
                    }
                }
                short digTileHeight = Tiles.decodeHeight(digTile);
                int rockTile = Server.rockMesh.getTile(digTilex, digTiley);
                short rockHeight = Tiles.decodeHeight(rockTile);
                short h = Tiles.decodeHeight(digTile);
                short minHeight = -7;
                short maxHeight = 20000;
                Skills skills = performer.getSkills();
                Skill digging = null;
                try {
                    digging = skills.getSkill(1009);
                }
                catch (Exception ex) {
                    digging = skills.learn(1009, 0.0f);
                }
                if (insta) {
                    minHeight = -300;
                } else if (dredging) {
                    maxHeight = -1;
                    minHeight = (short)(-Math.max(3.0, digging.getKnowledge(source, 0.0) * 3.0));
                }
                if (h > minHeight && h < maxHeight) {
                    double power;
                    Skill shovel;
                    block175: {
                        done = false;
                        shovel = null;
                        power = 0.0;
                        if (!insta) {
                            try {
                                shovel = skills.getSkill(source.getPrimarySkill());
                            }
                            catch (Exception ex) {
                                try {
                                    shovel = skills.learn(source.getPrimarySkill(), 1.0f);
                                }
                                catch (NoSuchSkillException nse) {
                                    if (performer.getPower() > 0) break block175;
                                    logger.log(Level.WARNING, performer.getName() + " trying to dig with an item with no primary skill: " + source.getName());
                                }
                            }
                        }
                    }
                    short maxdifference = 0;
                    if (!insta) {
                        if (Terraforming.checkIfTerraformingOnPermaObject(digTilex, digTiley)) {
                            performer.getCommunicator().sendNormalServerMessage("The object nearby prevents digging further down.");
                            return true;
                        }
                        if (Zones.isTileCornerProtected(digTilex, digTiley)) {
                            performer.getCommunicator().sendNormalServerMessage("Your shovel fails to penetrate the earth no matter what you try. Weird.");
                            return true;
                        }
                        if (Terraforming.isTileModBlocked(performer, digTilex, digTiley, true)) {
                            return true;
                        }
                        if ((Features.Feature.HIGHWAYS.isEnabled() || digTileHeight > 0) && Terraforming.wouldDestroyCobble(performer, digTilex, digTiley, false)) {
                            if (Features.Feature.HIGHWAYS.isEnabled()) {
                                performer.getCommunicator().sendNormalServerMessage("The highway would be too steep to traverse.");
                            } else {
                                performer.getCommunicator().sendNormalServerMessage("The road would be too steep to traverse.");
                            }
                            return true;
                        }
                        if (Terraforming.nextToTundra(mesh, digTilex, digTiley)) {
                            performer.getCommunicator().sendNormalServerMessage("The frozen soil is too hard to dig effectively.");
                            return true;
                        }
                        if (Terraforming.countNonDiggables(mesh, digTilex, digTiley) >= 3) {
                            performer.getCommunicator().sendNormalServerMessage("You cannot dig in such terrain.");
                            return true;
                        }
                        int lNewTile = mesh.getTile(digTilex, digTiley - 1);
                        short height = Tiles.decodeHeight(lNewTile);
                        short difference = (short)Math.abs(height - digTileHeight);
                        if (difference > maxdifference) {
                            maxdifference = difference;
                        }
                        if (Terraforming.checkDigTile(lNewTile, performer, digging, digTileHeight, difference)) {
                            return true;
                        }
                        lNewTile = mesh.getTile(digTilex + 1, digTiley);
                        height = Tiles.decodeHeight(lNewTile);
                        difference = (short)Math.abs(height - digTileHeight);
                        if (difference > maxdifference) {
                            maxdifference = difference;
                        }
                        if (Terraforming.checkDigTile(lNewTile, performer, digging, digTileHeight, difference)) {
                            return true;
                        }
                        lNewTile = mesh.getTile(digTilex, digTiley + 1);
                        height = Tiles.decodeHeight(lNewTile);
                        difference = (short)Math.abs(height - digTileHeight);
                        if (difference > maxdifference) {
                            maxdifference = difference;
                        }
                        if (Terraforming.checkDigTile(lNewTile, performer, digging, digTileHeight, difference)) {
                            return true;
                        }
                        lNewTile = mesh.getTile(digTilex - 1, digTiley);
                        height = Tiles.decodeHeight(lNewTile);
                        difference = (short)Math.abs(height - digTileHeight);
                        if (difference > maxdifference) {
                            maxdifference = difference;
                        }
                        if (Terraforming.checkDigTile(lNewTile, performer, digging, digTileHeight, difference)) {
                            return true;
                        }
                    }
                    Action act = null;
                    try {
                        act = performer.getCurrentAction();
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.WARNING, "Weird: " + nsa.getMessage(), nsa);
                        return true;
                    }
                    int time = 1000;
                    for (int x = 0; x >= -1; --x) {
                        for (int y = 0; y >= -1; --y) {
                            try {
                                Zone zone = Zones.getZone(digTilex + x, digTiley + y, performer.isOnSurface());
                                VolaTile vtile = zone.getTileOrNull(digTilex + x, digTiley + y);
                                if (vtile != null) {
                                    TimeConstants[] bps;
                                    if (vtile.getStructure() != null) {
                                        if (vtile.getStructure().isTypeHouse()) {
                                            performer.getCommunicator().sendNormalServerMessage("The house is in the way.");
                                            return true;
                                        }
                                        bps = vtile.getBridgeParts();
                                        if (bps.length == 1) {
                                            if (((BridgePart)bps[0]).getType().isSupportType()) {
                                                performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents digging.");
                                                return true;
                                            }
                                            if (x == -1 && ((BridgePart)bps[0]).hasEastExit() || x == 0 && ((BridgePart)bps[0]).hasWestExit() || y == -1 && ((BridgePart)bps[0]).hasSouthExit() || y == 0 && ((BridgePart)bps[0]).hasNorthExit()) {
                                                performer.getCommunicator().sendNormalServerMessage("The end of the bridge nearby prevents digging.");
                                                return true;
                                            }
                                        }
                                    }
                                    if (x == 0 && y == 0) {
                                        int n = 0;
                                        bps = vtile.getFences();
                                        int n2 = bps.length;
                                        if (n < n2) {
                                            TimeConstants timeConstants = bps[n];
                                            performer.getCommunicator().sendNormalServerMessage("The " + ((Fence)timeConstants).getName() + " is in the way.");
                                            return true;
                                        }
                                    } else if (x == -1 && y == 0) {
                                        for (TimeConstants timeConstants : vtile.getFences()) {
                                            if (!((Fence)timeConstants).isHorizontal()) continue;
                                            performer.getCommunicator().sendNormalServerMessage("The " + ((Fence)timeConstants).getName() + " is in the way.");
                                            return true;
                                        }
                                    } else if (y == -1 && x == 0) {
                                        for (TimeConstants timeConstants : vtile.getFences()) {
                                            if (((Fence)timeConstants).isHorizontal()) continue;
                                            performer.getCommunicator().sendNormalServerMessage("The " + ((Fence)timeConstants).getName() + " is in the way.");
                                            return true;
                                        }
                                    }
                                }
                            }
                            catch (NoSuchZoneException nsz) {
                                performer.getCommunicator().sendNormalServerMessage("The water is too deep to dig in.");
                                return true;
                            }
                            if (!(performer.getStrengthSkill() < 20.0) || !Terraforming.isRoad(Tiles.decodeType(newTile = mesh.getTile(digTilex + x, digTiley + y)))) continue;
                            performer.getCommunicator().sendNormalServerMessage("You need to be stronger to dig on roads.");
                            return true;
                        }
                    }
                    if (counter == 1.0f && !insta) {
                        if (dredging && (double)h < -0.5) {
                            time = Actions.getStandardActionTime(performer, digging, source, 0.0);
                            act.setTimeLeft(time);
                            performer.getCommunicator().sendNormalServerMessage("You start to dredge.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to dredge.", performer, 5);
                            performer.sendActionControl(Actions.actionEntrys[362].getVerbString(), true, time);
                            performer.getStatus().modifyStamina(-3000.0f);
                        } else {
                            time = Actions.getStandardActionTime(performer, digging, source, 0.0);
                            act.setTimeLeft(time);
                            performer.getCommunicator().sendNormalServerMessage("You start to dig.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to dig.", performer, 5);
                            performer.sendActionControl(Actions.actionEntrys[144].getVerbString(), true, time);
                            performer.getStatus().modifyStamina(-1000.0f);
                        }
                        source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                    } else if (!insta) {
                        time = act.getTimeLeft();
                        if (act.justTickedSecond() && (time < 50 && act.currentSecond() % 2 == 0 || act.currentSecond() % 5 == 0)) {
                            String sstring = "sound.work.digging1";
                            int x = Server.rand.nextInt(3);
                            if (x == 0) {
                                sstring = "sound.work.digging2";
                            } else if (x == 1) {
                                sstring = "sound.work.digging3";
                            }
                            SoundPlayer.playSound(sstring, performer, 0.0f);
                            source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                        }
                    }
                    if (!(counter * 10.0f > (float)time) && !insta) break block176;
                    int performerTileX = performer.getTileX();
                    int performerTileY = performer.getTileY();
                    int performerTile = mesh.getTile(performerTileX, performerTileY);
                    byte resType = Tiles.decodeType(performerTile);
                    if (!insta) {
                        if (act.getRarity() != 0) {
                            performer.playPersonalSound("sound.fx.drumroll");
                        }
                        double diff = 1 + maxdifference / 5;
                        if (resType == Tiles.Tile.TILE_CLAY.id) {
                            diff = 20.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_SAND.id) {
                            diff = 10.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_TAR.id) {
                            diff = 35.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_MOSS.id) {
                            diff = 10.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_MARSH.id) {
                            diff = 30.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_STEPPE.id) {
                            diff = 40.0 + diff;
                        } else if (resType == Tiles.Tile.TILE_TUNDRA.id) {
                            diff = 20.0 + diff;
                        }
                        if (shovel != null) {
                            shovel.skillCheck(diff, source, 0.0, false, counter);
                        }
                        if ((power = digging.skillCheck(diff, source, 0.0, false, counter)) < 0.0) {
                            for (int i = 0; i < 20 && !((power = digging.skillCheck(diff, source, 0.0, true, 1.0f)) > 1.0); ++i) {
                                power = 1.0;
                            }
                        }
                        float staminaCost = act.getTimeLeft() * -100;
                        performer.getStatus().modifyStamina(staminaCost);
                    }
                    done = true;
                    boolean hitRock = false;
                    boolean dealDirt = false;
                    short newDigHeight = 30000;
                    boolean dealClay = false;
                    boolean dealSand = false;
                    boolean dealPeat = false;
                    boolean dealTar = false;
                    boolean dealMoss = false;
                    int clayNum = Server.getDigCount(tilex, tiley);
                    if (clayNum <= 0 || clayNum > 100) {
                        clayNum = 50 + Server.rand.nextInt(50);
                    }
                    boolean allCornersRock = false;
                    for (int x = 0; x >= -1; --x) {
                        for (int y = 0; y >= -1; --y) {
                            Item[] foundItems;
                            boolean lChanged = false;
                            int lNewTile = mesh.getTile(digTilex + x, digTiley + y);
                            type = Tiles.decodeType(lNewTile);
                            short s = Tiles.decodeHeight(lNewTile);
                            rockTile = Server.rockMesh.getTile(digTilex + x, digTiley + y);
                            rockHeight = Tiles.decodeHeight(rockTile);
                            if (x == 0 && y == 0) {
                                if (resType == Tiles.Tile.TILE_CLAY.id) {
                                    dealClay = true;
                                } else if (resType == Tiles.Tile.TILE_SAND.id) {
                                    dealSand = true;
                                } else if (resType == Tiles.Tile.TILE_PEAT.id) {
                                    dealPeat = true;
                                } else if (resType == Tiles.Tile.TILE_TAR.id) {
                                    dealTar = true;
                                } else if (resType == Tiles.Tile.TILE_MOSS.id) {
                                    dealMoss = true;
                                }
                                if (s > rockHeight) {
                                    dealDirt = true;
                                    lChanged = true;
                                    if (dealClay) {
                                        if (--clayNum == 0) {
                                            s = (short)Math.max(s - 1, rockHeight);
                                        }
                                    } else if (!(dealTar || dealMoss || dealPeat)) {
                                        s = (short)Math.max(s - 1, rockHeight);
                                    }
                                    if (insta) {
                                        performer.getCommunicator().sendNormalServerMessage("Tile " + (digTilex + x) + ", " + (digTiley + y) + " now at " + s + ", rock at " + rockHeight + ".");
                                    }
                                    newDigHeight = s;
                                    mesh.setTile(digTilex + x, digTiley + y, Tiles.encode(s, type, Tiles.decodeData(lNewTile)));
                                    if (performer.fireTileLog()) {
                                        TileEvent.log(digTilex + x, digTiley + y, 0, performer.getWurmId(), 144);
                                    }
                                }
                                if (s <= rockHeight) {
                                    hitRock = true;
                                }
                            }
                            HighwayPos highwayPos = MethodsHighways.getHighwayPos(digTilex + x, digTiley + y, true);
                            allCornersRock = Terraforming.allCornersAtRockLevel(digTilex + x, digTiley + y, mesh);
                            if (!Terraforming.isImmutableTile(type) && allCornersRock && !MethodsHighways.onHighway(highwayPos)) {
                                lChanged = true;
                                Server.modifyFlagsByTileType(digTilex + x, digTiley + y, Tiles.Tile.TILE_ROCK.id);
                                mesh.setTile(digTilex + x, digTiley + y, Tiles.encode(s, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                TileEvent.log(digTilex + x, digTiley + y, 0, performer.getWurmId(), 144);
                            } else if (Terraforming.isTileTurnToDirt(type)) {
                                if (type != Tiles.Tile.TILE_DIRT.id) {
                                    TileEvent.log(digTilex + x, digTiley + y, 0, performer.getWurmId(), 144);
                                }
                                lChanged = true;
                                Server.modifyFlagsByTileType(digTilex + x, digTiley + y, Tiles.Tile.TILE_DIRT.id);
                                mesh.setTile(digTilex + x, digTiley + y, Tiles.encode(s, Tiles.Tile.TILE_DIRT.id, (byte)0));
                            } else if (Terraforming.isRoad(type)) {
                                if (Methods.isActionAllowed(performer, (short)144, false, digTilex + x, digTiley + y, digTile, 0)) {
                                    lChanged = true;
                                    Server.modifyFlagsByTileType(digTilex + x, digTiley + y, type);
                                    mesh.setTile(digTilex + x, digTiley + y, Tiles.encode(s, type, Tiles.decodeData(lNewTile)));
                                }
                                if (performer.fireTileLog()) {
                                    TileEvent.log(digTilex + x, digTiley + y, 0, performer.getWurmId(), 144);
                                }
                            }
                            if (performer.getTutorialLevel() == 8 && !performer.skippedTutorial()) {
                                performer.missionFinished(true, true);
                            }
                            if (lChanged) {
                                performer.getMovementScheme().touchFreeMoveCounter();
                                Players.getInstance().sendChangedTile(digTilex + x, digTiley + y, performer.isOnSurface(), true);
                                try {
                                    Zone toCheckForChange = Zones.getZone(digTilex + x, digTiley + y, performer.isOnSurface());
                                    toCheckForChange.changeTile(digTilex + x, digTiley + y);
                                }
                                catch (NoSuchZoneException nsz) {
                                    logger.log(Level.INFO, "no such zone?: " + digTilex + ", " + digTiley, nsz);
                                }
                            }
                            if (performer.isOnSurface()) {
                                Item[] foundArtifacts;
                                Tiles.Tile theTile = Tiles.getTile(type);
                                if (theTile.isTree()) {
                                    byte data = Tiles.decodeData(lNewTile);
                                    Zones.reposWildHive(digTilex + x, digTiley + y, theTile, data);
                                }
                                if ((foundArtifacts = EndGameItems.getArtifactDugUp(digTilex + x, digTiley + y, (float)s / 10.0f, allCornersRock)).length > 0) {
                                    for (int aa = 0; aa < foundArtifacts.length; ++aa) {
                                        VolaTile atile = Zones.getOrCreateTile(digTilex + x, digTiley + y, performer.isOnSurface());
                                        atile.addItem(foundArtifacts[aa], false, false);
                                        performer.getCommunicator().sendNormalServerMessage("You find something weird! You found the " + foundArtifacts[aa].getName() + "!", (byte)2);
                                        logger.log(Level.INFO, performer.getName() + " found the " + foundArtifacts[aa].getName() + " at tile " + (digTilex + x) + ", " + (digTiley + y) + "! " + foundArtifacts[aa]);
                                        HistoryManager.addHistory(performer.getName(), "reveals the " + foundArtifacts[aa].getName());
                                        EndGameItem egi = EndGameItems.getEndGameItem(foundArtifacts[aa]);
                                        if (egi == null) continue;
                                        egi.setLastMoved(System.currentTimeMillis());
                                        foundArtifacts[aa].setAuxData((byte)120);
                                    }
                                }
                            }
                            if ((foundItems = Items.getHiddenItemsAt(digTilex + x, digTiley + y, (float)s / 10.0f, true)).length <= 0) continue;
                            for (int aa = 0; aa < foundItems.length; ++aa) {
                                foundItems[aa].setHidden(false);
                                Items.revealItem(foundItems[aa]);
                                VolaTile atile = Zones.getOrCreateTile(digTilex + x, digTiley + y, performer.isOnSurface());
                                atile.addItem(foundItems[aa], false, false);
                                performer.getCommunicator().sendNormalServerMessage("You find something! You found a " + foundItems[aa].getName() + "!", (byte)2);
                                logger.log(Level.INFO, performer.getName() + " found a " + foundItems[aa].getName() + " at tile " + (digTilex + x) + ", " + (digTiley + y) + ".");
                            }
                        }
                    }
                    if (dealClay) {
                        Server.setDigCount(tilex, tiley, clayNum);
                    }
                    if (hitRock) {
                        performer.getCommunicator().sendNormalServerMessage("You hit rock.", (byte)3);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You dig a hole.");
                        Server.getInstance().broadCastAction(performer.getName() + " digs a hole.", performer, 5);
                    }
                    if (!dealDirt) break block176;
                    try {
                        double dig;
                        power = !insta ? (power > (dig = digging.getKnowledge(0.0)) ? dig : Math.max(1.0, power)) : 50.0;
                        int createdItemTemplate = 26;
                        if (dealClay) {
                            createdItemTemplate = 130;
                        } else if (dealSand) {
                            createdItemTemplate = 298;
                        } else if (dealTar) {
                            createdItemTemplate = 153;
                        } else if (dealPeat) {
                            createdItemTemplate = 467;
                        } else if (dealMoss) {
                            createdItemTemplate = 479;
                        }
                        float modifier = 1.0f;
                        if (source.getSpellEffects() != null) {
                            modifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                        }
                        VolaTile ttile = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
                        if (digToPile && ttile != null && ttile.getNumberOfItems(performer.getFloorLevel()) <= 99) {
                            Item newItem = ItemFactory.createItem(createdItemTemplate, Math.min((float)(power + (double)source.getRarity()) * modifier, 100.0f), performer.getPosX(), performer.getPosY(), Server.rand.nextFloat() * 360.0f, performer.isOnSurface(), act.getRarity(), -10L, null);
                            newItem.setLastOwnerId(performer.getWurmId());
                            performer.getCommunicator().sendNormalServerMessage("You drop a " + newItem.getName() + ".");
                        } else {
                            Item created = ItemFactory.createItem(createdItemTemplate, Math.min((float)(power + (double)source.getRarity()) * modifier, 100.0f), null);
                            created.setRarity(act.getRarity());
                            if (dredging && (double)h < -0.5) {
                                boolean addedToBoat = false;
                                if (performer.getVehicle() != -10L) {
                                    try {
                                        Item ivehic = Items.getItem(performer.getVehicle());
                                        if (ivehic.isBoat() && ivehic.testInsertItem(created)) {
                                            ivehic.insertItem(created);
                                            performer.getCommunicator().sendNormalServerMessage("You put the " + created.getName() + " in the " + ivehic.getName() + ".");
                                            addedToBoat = true;
                                        }
                                    }
                                    catch (NoSuchItemException ivehic) {
                                        // empty catch block
                                    }
                                }
                                if (!addedToBoat) {
                                    source.insertItem(created, true);
                                }
                            } else {
                                performer.getInventory().insertItem(created);
                            }
                        }
                        if (Server.isDirtHeightLower(digTilex, digTiley, newDigHeight)) {
                            EpicMission m;
                            VolaTile t;
                            if (Server.rand.nextInt(2500) == 0) {
                                int gemTemplateId = 374;
                                if (Server.rand.nextFloat() * 100.0f >= 99.0f) {
                                    gemTemplateId = 375;
                                }
                                float ql = Math.max(Math.min((float)(power + (double)source.getRarity()), 100.0f), 1.0f);
                                Item gem = ItemFactory.createItem(gemTemplateId, ql, null);
                                gem.setLastOwnerId(performer.getWurmId());
                                gem.setRarity(act.getRarity());
                                if (gem.getQualityLevel() > 99.0f) {
                                    performer.achievement(363);
                                } else if (gem.getQualityLevel() > 90.0f) {
                                    performer.achievement(364);
                                }
                                if (act.getRarity() > 2) {
                                    performer.achievement(365);
                                }
                                performer.getInventory().insertItem(gem, true);
                                performer.getCommunicator().sendNormalServerMessage("You find " + gem.getNameWithGenus() + "!", (byte)2);
                            }
                            if (act.getRarity() != 0 && performer.isPaying() && Server.rand.nextInt(100) == 0) {
                                float ql = Math.max(Math.min((float)(power + (double)source.getRarity()), 100.0f), 1.0f);
                                Item bone = ItemFactory.createItem(867, ql, null);
                                bone.setRarity(act.getRarity());
                                performer.getInventory().insertItem(bone, true);
                                performer.getCommunicator().sendNormalServerMessage("You find something! You found a " + MethodsItems.getRarityName(act.getRarity()) + " " + bone.getName() + "!", (byte)2);
                                performer.achievement(366);
                            }
                            if (Server.rand.nextInt(250) == 0 && ((t = Zones.getTileOrNull(digTilex, digTiley, performer.isOnSurface())) != null && t.getVillage() == null || t == null) && (m = EpicServerStatus.getMISacrificeMission()) != null) {
                                try {
                                    Item missionItem = ItemFactory.createItem(737, 20 + Server.rand.nextInt(80), act.getRarity(), m.getEntityName());
                                    missionItem.setName(HexMap.generateFirstName(m.getMissionId()) + ' ' + HexMap.generateSecondName(m.getMissionId()));
                                    performer.getInventory().insertItem(missionItem);
                                    performer.getCommunicator().sendNormalServerMessage("You find a " + missionItem.getName() + " in amongst the dirt.");
                                }
                                catch (FailedException | NoSuchTemplateException wurmServerException) {
                                    // empty catch block
                                }
                            }
                        }
                        PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.DIG_TILE);
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, fe.getMessage(), fe);
                    }
                    break block176;
                }
                done = true;
                if (dredging) {
                    if (h <= minHeight) {
                        performer.getCommunicator().sendNormalServerMessage("You do not have sufficient skill to dredge at that depth.", (byte)3);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The water is too shallow to be dredged.", (byte)3);
                    }
                } else if (h <= minHeight) {
                    performer.getCommunicator().sendNormalServerMessage("The water is too deep to dig.", (byte)3);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You do not have sufficient skill to dig at that height.", (byte)3);
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
                done = true;
            }
        }
        return done;
    }

    private static int countNonDiggables(MeshIO mesh, int digTilex, int digTiley) {
        int nonDiggables = 0;
        int lNewTile = mesh.getTile(digTilex, digTiley);
        if (Terraforming.isNonDiggableTile(Tiles.decodeType(lNewTile))) {
            ++nonDiggables;
        }
        if (Terraforming.isNonDiggableTile(Tiles.decodeType(lNewTile = mesh.getTile(digTilex, digTiley - 1)))) {
            ++nonDiggables;
        }
        if (Terraforming.isNonDiggableTile(Tiles.decodeType(lNewTile = mesh.getTile(digTilex - 1, digTiley - 1)))) {
            ++nonDiggables;
        }
        if (Terraforming.isNonDiggableTile(Tiles.decodeType(lNewTile = mesh.getTile(digTilex - 1, digTiley)))) {
            ++nonDiggables;
        }
        return nonDiggables;
    }

    public static boolean checkSculptCaveTile(int lNewTile, Creature performer, short floorHeight, int ceilingHeight, int decimeterChange, boolean obliterateCeiling) {
        if (Tiles.decodeType(lNewTile) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            return true;
        }
        short nfloorHeight = Tiles.decodeHeight(lNewTile);
        if (nfloorHeight != -100) {
            int checkedCeilingHeight;
            int nceilingHeight = nfloorHeight + (short)(Tiles.decodeData(lNewTile) & 0xFF);
            if (obliterateCeiling ? Math.abs((checkedCeilingHeight = floorHeight + ceilingHeight + decimeterChange) - nceilingHeight) > 254 : Math.abs(floorHeight - decimeterChange - nfloorHeight) > 254) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkMineSurfaceTile(int lNewTile, Creature performer, short digTileHeight, short maxDiff) {
        short height = Tiles.decodeHeight(lNewTile);
        short difference = (short)Math.abs(height - digTileHeight - 1);
        if (difference > maxDiff) {
            performer.getCommunicator().sendNormalServerMessage("You are not skilled enough to mine in the steep slope.");
            return true;
        }
        return false;
    }

    public static boolean checkSculptTile(int lNewTile, Creature performer, short digTileHeight, int decimeterChange) {
        if (!Terraforming.isSculptable(Tiles.decodeType(lNewTile))) {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens on the " + Tiles.getTile((byte)Tiles.decodeType((int)lNewTile)).tiledesc + ". The wand only seems to work on grass, rock, dirt and similar terrain.");
            return true;
        }
        short height = Tiles.decodeHeight(lNewTile);
        short difference = (short)Math.abs(height - digTileHeight - decimeterChange);
        if (difference > 200) {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens on the steep slope.");
            return true;
        }
        return false;
    }

    public static boolean checkDigTile(int lNewTile, Creature performer, Skill digging, short digTileHeight, short difference) {
        double diffMod = digging.getKnowledge(0.0);
        if ((double)difference > Math.max(10.0, 1.0 + diffMod * 3.0)) {
            performer.getCommunicator().sendNormalServerMessage("You are not skilled enough to dig in such steep slopes.", (byte)3);
            return true;
        }
        return false;
    }

    private static final boolean checkHeightDiff(boolean raise, short myHeight, short checkHeight, short maxHeightDiff) {
        return raise ? myHeight - checkHeight > maxHeightDiff : checkHeight - myHeight > maxHeightDiff;
    }

    public static final boolean nextToTundra(MeshIO mesh, int digTilex, int digTiley) {
        if (Tiles.isTundra(Tiles.decodeType(mesh.getTile(digTilex, digTiley)))) {
            return true;
        }
        if (Tiles.isTundra(Tiles.decodeType(mesh.getTile(digTilex - 1, digTiley)))) {
            return true;
        }
        if (Tiles.isTundra(Tiles.decodeType(mesh.getTile(digTilex, digTiley - 1)))) {
            return true;
        }
        return Tiles.isTundra(Tiles.decodeType(mesh.getTile(digTilex - 1, digTiley - 1)));
    }

    public static final boolean wouldDestroyCobble(Creature performer, int changeTileX, int changeTileY, boolean raise) {
        short modDiff = (short)(raise ? 1 : -1);
        MeshIO mesh = Server.surfaceMesh;
        int mytile = mesh.getTile(changeTileX, changeTileY);
        short myHeight = (short)(Tiles.decodeHeight(mytile) + modDiff);
        if (myHeight < 0) {
            return false;
        }
        int checkTile = mesh.getTile(changeTileX + 1, changeTileY);
        short checkHeight = Tiles.decodeHeight(checkTile);
        if (Tiles.isRoadType(mytile) && MethodsHighways.onHighway(changeTileX + 1, changeTileY, true)) {
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX, changeTileY + 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX + 1, changeTileY + 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)28)) {
                return true;
            }
        }
        checkTile = mesh.getTile(changeTileX - 1, changeTileY);
        checkHeight = Tiles.decodeHeight(checkTile);
        if (Tiles.isRoadType(checkTile) && MethodsHighways.onHighway(changeTileX - 1, changeTileY, true)) {
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX, changeTileY + 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX - 1, changeTileY + 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)28)) {
                return true;
            }
        }
        if (Tiles.isRoadType(checkTile = mesh.getTile(changeTileX - 1, changeTileY - 1)) && MethodsHighways.onHighway(changeTileX - 1, changeTileY - 1, true)) {
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)28)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX, changeTileY - 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX - 1, changeTileY);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
        }
        checkTile = mesh.getTile(changeTileX, changeTileY - 1);
        checkHeight = Tiles.decodeHeight(checkTile);
        if (Tiles.isRoadType(checkTile) && MethodsHighways.onHighway(changeTileX, changeTileY - 1, true)) {
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX + 1, changeTileY);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)20)) {
                return true;
            }
            checkTile = mesh.getTile(changeTileX + 1, changeTileY - 1);
            checkHeight = Tiles.decodeHeight(checkTile);
            if (Terraforming.checkHeightDiff(raise, myHeight, checkHeight, (short)28)) {
                return true;
            }
        }
        return false;
    }

    static boolean pack(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter, Action act) {
        boolean done = false;
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize || Tiles.decodeHeight(tile) < -100) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to pack the dirt here.");
            done = true;
        } else {
            byte type = Tiles.decodeType(tile);
            if (Terraforming.isPackable(type)) {
                if (type != Tiles.Tile.TILE_DIRT_PACKED.id) {
                    Skill shovel;
                    Skill paving;
                    block22: {
                        done = false;
                        Skills skills = performer.getSkills();
                        paving = null;
                        shovel = null;
                        try {
                            paving = skills.getSkill(10031);
                        }
                        catch (Exception ex) {
                            paving = skills.learn(10031, 1.0f);
                        }
                        if (performer.getPower() > 0) {
                            try {
                                shovel = skills.getSkill(source.getPrimarySkill());
                            }
                            catch (Exception ex) {
                                try {
                                    shovel = skills.learn(source.getPrimarySkill(), 1.0f);
                                }
                                catch (NoSuchSkillException nse) {
                                    if (performer.getPower() > 0) break block22;
                                    logger.log(Level.WARNING, performer.getName() + " trying to pack with an item with no primary skill: " + source.getName());
                                }
                            }
                        }
                    }
                    int time = 2000;
                    if (counter == 1.0f) {
                        time = Actions.getStandardActionTime(performer, paving, source, 0.0);
                        act.setTimeLeft(time);
                        performer.getCommunicator().sendNormalServerMessage("You start to pack the ground.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to pack the ground.", performer, 5);
                        performer.sendActionControl(Actions.actionEntrys[154].getVerbString(), true, time);
                        source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                        performer.getStatus().modifyStamina(-1000.0f);
                    } else {
                        time = act.getTimeLeft();
                        if (act.currentSecond() % 5 == 0) {
                            SoundPlayer.playSound("sound.work.digging.pack", tilex, tiley, onSurface, 0.0f);
                            performer.getStatus().modifyStamina(-10000.0f);
                            source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                        }
                    }
                    if (counter * 10.0f > (float)time) {
                        if (shovel != null) {
                            shovel.skillCheck(10.0, source, 0.0, false, counter);
                        }
                        paving.skillCheck(1.0, source, 0.0, false, counter);
                        done = true;
                        int t = Server.surfaceMesh.getTile(tilex, tiley);
                        short h = Tiles.decodeHeight(t);
                        if (!Terraforming.isRockTile(Tiles.decodeType(t))) {
                            TileEvent.log(tilex, tiley, 0, performer.getWurmId(), act.getNumber());
                            Server.setSurfaceTile(tilex, tiley, h, Tiles.Tile.TILE_DIRT_PACKED.id, (byte)0);
                            performer.getCommunicator().sendNormalServerMessage("The dirt is packed and hard now.");
                            try {
                                Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
                                toCheckForChange.changeTile(tilex, tiley);
                                Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, onSurface, true);
                            }
                            catch (NoSuchZoneException nsz) {
                                logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("The rock has been bared. No dirt remains to pack anymore.");
                        }
                    }
                } else {
                    done = true;
                    performer.getCommunicator().sendNormalServerMessage("The dirt is packed here already.");
                }
            } else {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("You can't pack the dirt in that place. A " + source.getName() + " just won't do.");
            }
        }
        return done;
    }

    static boolean destroyPave(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter) {
        boolean done = false;
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to destroy the pavement here.");
            done = true;
        } else {
            byte actualType;
            if (performer.getStrengthSkill() < 20.0) {
                performer.getCommunicator().sendNormalServerMessage("You need to be stronger to destroy pavement.");
                return true;
            }
            if (Zones.protectedTiles[tilex][tiley]) {
                performer.getCommunicator().sendNormalServerMessage("Your muscles go limp and refuse. You just can't bring yourself to do this.");
                return true;
            }
            int digTile = Server.surfaceMesh.getTile(tilex, tiley);
            if (!onSurface) {
                digTile = Server.caveMesh.getTile(tilex, tiley);
            }
            byte type = (actualType = Tiles.decodeType(digTile)) == Tiles.Tile.TILE_CAVE_EXIT.id ? Server.getClientCaveFlags(tilex, tiley) : actualType;
            Action act = null;
            try {
                act = performer.getCurrentAction();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, nsa.getMessage(), nsa);
                return true;
            }
            if (Terraforming.isRoad(type)) {
                String fromWhere;
                HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
                if (MethodsHighways.onHighway(highwaypos)) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot remove paving next to a marker.");
                    return true;
                }
                done = false;
                Skills skills = performer.getSkills();
                Skill digging = skills.getSkillOrLearn(1009);
                int time = 6000;
                if (counter == 1.0f) {
                    if (digging.getRealKnowledge() < 10.0) {
                        if (type == Tiles.Tile.TILE_PLANKS.id || type == Tiles.Tile.TILE_PLANKS_TARRED.id) {
                            performer.getCommunicator().sendNormalServerMessage("You can't figure out how to remove the floor boards. You must become a bit better at digging first.");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You can't figure out how to remove the stone. You must become a bit better at digging first.");
                        }
                        return true;
                    }
                    time = Actions.getDestroyActionTime(performer, digging, source, 0.0);
                    act.setTimeLeft(time);
                    if (type == Tiles.Tile.TILE_PLANKS.id || type == Tiles.Tile.TILE_PLANKS_TARRED.id) {
                        performer.getCommunicator().sendNormalServerMessage("You start to remove the floor boards.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to remove the floor boards.", performer, 5);
                    } else {
                        fromWhere = onSurface ? "paved dirt." : (actualType == Tiles.Tile.TILE_CAVE_EXIT.id ? "cave exit." : "cave floor.");
                        performer.getCommunicator().sendNormalServerMessage("You start to remove the stones from the " + fromWhere);
                        Server.getInstance().broadCastAction(performer.getName() + " starts to remove the stones from the " + fromWhere, performer, 5);
                    }
                    performer.sendActionControl(Actions.actionEntrys[191].getVerbString(), true, time);
                    performer.getStatus().modifyStamina(-1500.0f);
                } else {
                    time = act.getTimeLeft();
                    if (act.currentSecond() % 5 == 0) {
                        performer.getStatus().modifyStamina(-2000.0f);
                        source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                    }
                }
                if (counter * 10.0f > (float)time) {
                    if (digging != null) {
                        digging.skillCheck(40.0, source, 0.0, false, counter);
                    }
                    done = true;
                    fromWhere = onSurface ? "ground" : (actualType == Tiles.Tile.TILE_CAVE_EXIT.id ? "cave exit" : "cave floor");
                    short h = Tiles.decodeHeight(digTile);
                    if (Terraforming.isRoad(type) || type == Tiles.Tile.TILE_PLANKS.id || type == Tiles.Tile.TILE_PLANKS_TARRED.id) {
                        TileEvent.log(tilex, tiley, performer.getLayer(), performer.getWurmId(), 191);
                        if (type == Tiles.Tile.TILE_PLANKS.id || type == Tiles.Tile.TILE_PLANKS_TARRED.id) {
                            performer.getCommunicator().sendNormalServerMessage("The " + fromWhere + " is no longer covered with planks.");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("The " + fromWhere + " is no longer paved with stones.");
                        }
                        if (onSurface) {
                            Server.setSurfaceTile(tilex, tiley, h, Tiles.Tile.TILE_DIRT.id, (byte)0);
                        } else if (actualType == Tiles.Tile.TILE_CAVE_EXIT.id) {
                            Server.setClientCaveFlags(tilex, tiley, Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id);
                        } else {
                            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(h, Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id, Tiles.decodeData(digTile)));
                        }
                        performer.getMovementScheme().touchFreeMoveCounter();
                        Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
                        try {
                            Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
                            toCheckForChange.changeTile(tilex, tiley);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                        }
                        performer.performActionOkey(act);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The " + fromWhere + " isn't paved any longer, and your efforts are ruined.");
                    }
                }
            } else {
                done = true;
                if (onSurface) {
                    performer.getCommunicator().sendNormalServerMessage("The dirt isn't even paved here.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The cave floor isn't even paved here.");
                }
            }
        }
        return done;
    }

    static boolean pave(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter, Action act) {
        byte newTileType;
        Communicator comm = performer.getCommunicator();
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
            comm.sendNormalServerMessage("The water is too deep to pave here.");
            return true;
        }
        if (Tiles.decodeHeight(tile) < -100) {
            comm.sendNormalServerMessage("The water is too deep to pave here.");
            return true;
        }
        int digTile = Server.surfaceMesh.getTile(tilex, tiley);
        byte type = Tiles.decodeType(digTile);
        byte fType = Server.getClientCaveFlags(tilex, tiley);
        boolean repaving = false;
        if (Tiles.isRoadType(type) || type == Tiles.Tile.TILE_CAVE_EXIT.id && Tiles.isRoadType(fType)) {
            repaving = true;
            Village village = Villages.getVillageWithPerimeterAt(tilex, tiley, onSurface);
            if (village != null && !village.isActionAllowed(act.getNumber(), performer)) {
                comm.sendNormalServerMessage("You do not have permissions to do that.");
                return true;
            }
        } else if (type != Tiles.Tile.TILE_DIRT_PACKED.id) {
            comm.sendNormalServerMessage("The ground isn't packed here. You have to pack it first.");
            return true;
        }
        if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
            comm.sendNormalServerMessage("The amount of " + source.getName() + " is too little to pave. You may need to combine them with other " + source.getTemplate().getPlural() + ".");
            return true;
        }
        int pavingItem = source.getTemplateId();
        short actNumber = act.getNumber();
        if (counter == 1.0f) {
            Skill paving = performer.getSkills().getSkillOrLearn(10031);
            int time = Actions.getStandardActionTime(performer, paving, source, 0.0);
            act.setTimeLeft(time);
            if (pavingItem == 519) {
                comm.sendNormalServerMessage("You break up the collosus brick and start to pave with the parts.");
            } else if (repaving) {
                comm.sendNormalServerMessage("You start to repave with the " + source.getName() + ".");
            } else {
                comm.sendNormalServerMessage("You start to pave the packed dirt with the " + source.getName() + ".");
            }
            Server.getInstance().broadCastAction(performer.getName() + " starts to pave the packed dirt.", performer, 5);
            performer.sendActionControl(act.getActionEntry().getVerbString(), true, time);
            performer.getStatus().modifyStamina(-1000.0f);
            return false;
        }
        int time = act.getTimeLeft();
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-10000.0f);
        }
        if (act.mayPlaySound()) {
            Methods.sendSound(performer, "sound.work.paving");
        }
        if (counter * 10.0f <= (float)time) {
            return false;
        }
        if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
            comm.sendNormalServerMessage("The amount of " + source.getName() + " is too little to pave. You may need to combine them with other " + source.getTemplate().getPlural() + ".");
            return true;
        }
        int t = Server.surfaceMesh.getTile(tilex, tiley);
        short h = Tiles.decodeHeight(t);
        if (Tiles.decodeType(t) != Tiles.Tile.TILE_DIRT_PACKED.id && !repaving) {
            comm.sendNormalServerMessage("The ground isn't fit for paving any longer, and your efforts are ruined.");
            return true;
        }
        Skill paving = performer.getSkills().getSkillOrLearn(10031);
        paving.skillCheck(pavingItem == 146 ? 5.0 : 30.0, source, 0.0, false, counter);
        TileEvent.log(tilex, tiley, 0, performer.getWurmId(), actNumber);
        byte dir = Terraforming.getDiagonalDir(performer, tilex, tiley, actNumber);
        switch (pavingItem) {
            case 132: {
                newTileType = Tiles.Tile.TILE_COBBLESTONE.id;
                break;
            }
            case 1122: {
                newTileType = Tiles.Tile.TILE_COBBLESTONE_ROUND.id;
                break;
            }
            case 519: {
                newTileType = Tiles.Tile.TILE_COBBLESTONE_ROUGH.id;
                break;
            }
            case 406: {
                newTileType = Tiles.Tile.TILE_STONE_SLABS.id;
                break;
            }
            case 1123: {
                newTileType = Tiles.Tile.TILE_SLATE_BRICKS.id;
                break;
            }
            case 771: {
                newTileType = Tiles.Tile.TILE_SLATE_SLABS.id;
                break;
            }
            case 1121: {
                newTileType = Tiles.Tile.TILE_SANDSTONE_BRICKS.id;
                break;
            }
            case 1124: {
                newTileType = Tiles.Tile.TILE_SANDSTONE_SLABS.id;
                break;
            }
            case 787: {
                newTileType = Tiles.Tile.TILE_MARBLE_SLABS.id;
                break;
            }
            case 786: {
                newTileType = Tiles.Tile.TILE_MARBLE_BRICKS.id;
                break;
            }
            case 776: {
                newTileType = Tiles.Tile.TILE_POTTERY_BRICKS.id;
                break;
            }
            default: {
                newTileType = Tiles.Tile.TILE_GRAVEL.id;
            }
        }
        Server.setSurfaceTile(tilex, tiley, h, newTileType, dir);
        Items.destroyItem(source.getWurmId());
        comm.sendNormalServerMessage("The ground is paved now.");
        Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, onSurface, true);
        try {
            Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
            toCheckForChange.changeTile(tilex, tiley);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
        return true;
    }

    static boolean makeFloor(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter) {
        boolean done = false;
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to pave here.");
            done = true;
        } else if (Tiles.decodeHeight(tile) < -100) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to pave here.");
            done = true;
        } else if (source.getTemplateId() != 495) {
            performer.getCommunicator().sendNormalServerMessage("You need floor boards.");
            done = true;
        } else {
            int digTile = Server.surfaceMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(digTile);
            Action act = null;
            try {
                act = performer.getCurrentAction();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, nsa.getMessage(), nsa);
                return true;
            }
            if (type == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_MARSH.id) {
                done = false;
                int time = 2000;
                if (counter == 1.0f) {
                    Skills skills = performer.getSkills();
                    Skill paving = null;
                    if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
                        performer.getCommunicator().sendNormalServerMessage("The amount of planks is too little to do this. You may need to use another item.");
                        return true;
                    }
                    try {
                        paving = skills.getSkill(10031);
                    }
                    catch (Exception ex) {
                        paving = skills.learn(10031, 1.0f);
                    }
                    time = Actions.getStandardActionTime(performer, paving, source, 0.0);
                    act.setTimeLeft(time);
                    if (type == Tiles.Tile.TILE_MARSH.id) {
                        performer.getCommunicator().sendNormalServerMessage("You start to put the floorboard in the marsh.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to put the floorboard in the marsh.", performer, 5);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You start to fit the floorboard in the dirt.");
                        Server.getInstance().broadCastAction(performer.getName() + " starts to fit the floorboard in the dirt.", performer, 5);
                    }
                    performer.sendActionControl(Actions.actionEntrys[155].getVerbString(), true, time);
                    performer.getStatus().modifyStamina(-1000.0f);
                } else {
                    time = act.getTimeLeft();
                    if (act.currentSecond() % 5 == 0) {
                        performer.getStatus().modifyStamina(-10000.0f);
                    }
                }
                if (counter * 10.0f > (float)time) {
                    long parentId = source.getParentId();
                    act.setDestroyedItem(source);
                    if (parentId != -10L) {
                        try {
                            Items.getItem(parentId).dropItem(source.getWurmId(), false);
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.INFO, performer.getName() + " tried to make floor with nonexistant floorboards.", nsi);
                        }
                    } else {
                        logger.log(Level.WARNING, performer.getName() + " managed to pave with floorboards on ground?");
                        try {
                            Zone zone = Zones.getZone((int)source.getPosX() >> 2, (int)source.getPosY() >> 2, source.isOnSurface());
                            zone.removeItem(source);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.WARNING, performer.getName() + " failed to locate zone", nsz);
                        }
                    }
                    Skills skills = performer.getSkills();
                    Skill paving = null;
                    try {
                        paving = skills.getSkill(10031);
                    }
                    catch (Exception ex) {
                        paving = skills.learn(10031, 1.0f);
                    }
                    if (paving != null) {
                        paving.skillCheck(5.0, source, 0.0, false, counter);
                    }
                    done = true;
                    int t = Server.surfaceMesh.getTile(tilex, tiley);
                    short h = Tiles.decodeHeight(t);
                    if (Tiles.decodeType(t) == Tiles.Tile.TILE_DIRT.id || type == Tiles.Tile.TILE_MARSH.id) {
                        byte dir = Terraforming.getDiagonalDir(performer, tilex, tiley, act.getNumber());
                        Server.setSurfaceTile(tilex, tiley, h, Tiles.Tile.TILE_PLANKS.id, dir);
                        if (Tiles.decodeType(t) == Tiles.Tile.TILE_MARSH.id) {
                            performer.getCommunicator().sendNormalServerMessage("You cover parts of the marsh with boards.");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("The ground has some fine floor boards now.");
                        }
                        Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, onSurface, true);
                        try {
                            Items.decay(source.getWurmId(), source.getDbStrings());
                            act.setDestroyedItem(null);
                            Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
                            toCheckForChange.changeTile(tilex, tiley);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The ground isn't fit for floor boards any longer, and your efforts are ruined.");
                    }
                }
            } else {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("The dirt isn't loose here. You have to cultivate it first.");
            }
        }
        return done;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static final boolean switchTileTypes(Creature performer, @NonNull Item source, int tilex, int tiley, float counter, Action act) {
        boolean done = false;
        int time = 50;
        int digTile = Server.surfaceMesh.getTile(tilex, tiley);
        byte type = Tiles.decodeType(digTile);
        Village vill = Zones.getVillage(tilex, tiley, performer.isOnSurface());
        if (!Terraforming.isSwitchableTiles(source.getTemplateId(), type)) {
            performer.getCommunicator().sendNormalServerMessage("You can no longer switch here.");
            return true;
        }
        if (vill != null) {
            if (!vill.isActionAllowed((short)927, performer)) {
                return true;
            }
        } else {
            if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
                performer.getCommunicator().sendNormalServerMessage("You need to have a full weight " + source.getName() + " to switch the tile type.");
                return true;
            }
            if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
                performer.getCommunicator().sendNormalServerMessage("You can't switch here.");
                return true;
            }
            if (!performer.isOnSurface()) {
                performer.getCommunicator().sendNormalServerMessage("You have to be on the surface to be able to do this.");
                return true;
            }
        }
        if (counter == 1.0f) {
            Village v = Zones.getVillage(tilex, tiley, performer.isOnSurface());
            if (v != null && !v.isActionAllowed((short)927, performer)) {
                performer.getCommunicator().sendNormalServerMessage("You may not do that here.");
                return true;
            }
        } else {
            byte toSwitchTo;
            if (!(counter * 10.0f > 50.0f)) return done;
            if (!act.justTickedSecond()) return done;
            performer.getStatus().modifyStamina(-10000.0f);
            switch (source.getTemplateId()) {
                case 26: {
                    toSwitchTo = 5;
                    break;
                }
                case 298: {
                    toSwitchTo = 1;
                    break;
                }
                default: {
                    toSwitchTo = 1;
                    logger.warning("Reached DEFAULT case in SWITCH. TemplateID = " + source.getTemplateId() + ". Performer = " + performer.getName());
                }
            }
            source.setWeight(source.getWeightGrams() - source.getTemplate().getWeightGrams(), true);
            Server.surfaceMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(digTile), toSwitchTo, (byte)0));
            Server.modifyFlagsByTileType(tilex, tiley, toSwitchTo);
            try {
                Zone toCheckForChange = Zones.getZone(tilex, tiley, performer.isOnSurface());
                toCheckForChange.changeTile(tilex, tiley);
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
            }
            Players.getInstance().sendChangedTile(tilex, tiley, performer.isOnSurface(), true);
            return true;
        }
        act.setTimeLeft(50);
        performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
        return done;
    }

    private static byte getDiagonalDir(Creature performer, int tilex, int tiley, short action) {
        if (action == 576 || action == 694 || action == 695) {
            Vector2f pos = performer.getPos2f();
            int digTilex = CoordUtils.WorldToTile(pos.x + 2.0f);
            int digTiley = CoordUtils.WorldToTile(pos.y + 2.0f);
            if (tilex == digTilex && tiley == digTiley) {
                return Tiles.TileRoadDirection.DIR_NW.getCode();
            }
            if (tilex + 1 == digTilex && tiley == digTiley) {
                return Tiles.TileRoadDirection.DIR_NE.getCode();
            }
            if (tilex + 1 == digTilex && tiley + 1 == digTiley) {
                return Tiles.TileRoadDirection.DIR_SE.getCode();
            }
            if (tilex == digTilex && tiley + 1 == digTiley) {
                return Tiles.TileRoadDirection.DIR_SW.getCode();
            }
        }
        return 0;
    }

    static boolean tarFloor(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter) {
        boolean done = false;
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep here.");
            done = true;
        } else if (source.getTemplateId() != 153) {
            performer.getCommunicator().sendNormalServerMessage("You need tar.");
            done = true;
        } else if (Tiles.decodeHeight(tile) < -100) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to tar here.");
            done = true;
        } else {
            MeshIO mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
            int digTile = mesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(digTile);
            Action act = null;
            try {
                act = performer.getCurrentAction();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, nsa.getMessage(), nsa);
                return true;
            }
            if (type == Tiles.Tile.TILE_PLANKS.id) {
                Skill paving;
                Skills skills;
                done = false;
                int time = 2000;
                if (counter == 1.0f) {
                    skills = performer.getSkills();
                    paving = null;
                    if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
                        performer.getCommunicator().sendNormalServerMessage("The amount of tar is too little to do this. You may need to use another item.");
                        return true;
                    }
                    try {
                        paving = skills.getSkill(10031);
                    }
                    catch (Exception ex) {
                        paving = skills.learn(10031, 1.0f);
                    }
                    time = Actions.getStandardActionTime(performer, paving, source, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start to put tar on the floorboards.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to put tar on the floorboards.", performer, 5);
                    performer.sendActionControl("tarring", true, time);
                    performer.getStatus().modifyStamina(-1000.0f);
                } else {
                    time = act.getTimeLeft();
                    if (act.currentSecond() % 5 == 0) {
                        performer.getStatus().modifyStamina(-10000.0f);
                    }
                }
                if (counter * 10.0f > (float)time) {
                    skills = performer.getSkills();
                    paving = null;
                    try {
                        paving = skills.getSkill(10031);
                    }
                    catch (Exception ex) {
                        paving = skills.learn(10031, 1.0f);
                    }
                    if (paving != null) {
                        paving.skillCheck(5.0, source, 0.0, false, counter);
                    }
                    done = true;
                    int t = mesh.getTile(tilex, tiley);
                    short h = Tiles.decodeHeight(t);
                    if (Tiles.decodeType(t) == Tiles.Tile.TILE_PLANKS.id) {
                        byte data = Tiles.decodeData(t);
                        mesh.setTile(tilex, tiley, Tiles.encode(h, Tiles.Tile.TILE_PLANKS_TARRED.id, data));
                        performer.getCommunicator().sendNormalServerMessage("The floor boards are well protected now.");
                        TileEvent.log(tilex, tiley, 0, performer.getWurmId(), act.getNumber());
                        Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, onSurface, true);
                        try {
                            source.setWeight(source.getWeightGrams() - source.getTemplate().getWeightGrams(), true);
                            Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
                            toCheckForChange.changeTile(tilex, tiley);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The ground doesn't consist of floor boards any longer.");
                    }
                }
            } else {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("The ground doesn't consist of floor boards any longer.");
            }
        }
        return done;
    }

    static boolean cultivate(Creature performer, Item source, int tilex, int tiley, boolean onSurface, int tile, float counter) {
        boolean done = false;
        if (tilex < 0 || tilex > 1 << Constants.meshSize || tiley < 0 || tiley > 1 << Constants.meshSize || Tiles.decodeHeight(tile) < 0) {
            performer.getCommunicator().sendNormalServerMessage("The water is too deep to cultivate here.");
            done = true;
        } else {
            int digTile = Server.surfaceMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(digTile);
            Action act = null;
            try {
                act = performer.getCurrentAction();
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, nsa.getMessage(), nsa);
                return true;
            }
            if (Terraforming.isCultivatable(type)) {
                Skills skills;
                done = false;
                int time = 2000;
                if (counter == 1.0f) {
                    skills = performer.getSkills();
                    Skill digging = null;
                    try {
                        digging = skills.getSkill(1009);
                    }
                    catch (Exception ex) {
                        digging = skills.learn(1009, 1.0f);
                    }
                    time = Actions.getStandardActionTime(performer, digging, source, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start to cultivate the soil.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to cultivate the soil.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[318].getVerbString(), true, time);
                    performer.getStatus().modifyStamina(-1000.0f);
                } else {
                    time = act.getTimeLeft();
                    if (act.mayPlaySound()) {
                        performer.getStatus().modifyStamina(-10000.0f);
                        String sstring = "sound.work.digging1";
                        int x = Server.rand.nextInt(3);
                        if (x == 0) {
                            sstring = "sound.work.digging2";
                        } else if (x == 1) {
                            sstring = "sound.work.digging3";
                        }
                        Methods.sendSound(performer, sstring);
                    }
                    if (act.currentSecond() % 5 == 0) {
                        source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                    }
                }
                if (counter * 10.0f > (float)time) {
                    skills = performer.getSkills();
                    Skill digging = null;
                    try {
                        digging = skills.getSkill(1009);
                    }
                    catch (Exception ex) {
                        digging = skills.learn(1009, 1.0f);
                    }
                    if (digging != null) {
                        digging.skillCheck(14.0, source, 0.0, false, counter);
                    }
                    done = true;
                    int t = Server.surfaceMesh.getTile(tilex, tiley);
                    short h = Tiles.decodeHeight(t);
                    if (Terraforming.isCultivatable(Tiles.decodeType(t))) {
                        Server.setSurfaceTile(tilex, tiley, h, Tiles.Tile.TILE_DIRT.id, (byte)0);
                        performer.getCommunicator().sendNormalServerMessage("The ground is cultivated and ready to sow now.");
                        Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, onSurface, true);
                        try {
                            Zone toCheckForChange = Zones.getZone(tilex, tiley, onSurface);
                            toCheckForChange.changeTile(tilex, tiley);
                        }
                        catch (NoSuchZoneException nsz) {
                            logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                        }
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The ground isn't fit for cultivating any longer, and your efforts are ruined.");
                    }
                }
            } else {
                done = true;
                performer.getCommunicator().sendNormalServerMessage("The soil isn't cultivatable here. You may have to pack it first.");
            }
        }
        return done;
    }

    public static boolean allCornersAtRockLevel(int tilex, int tiley, MeshIO mesh) {
        int numberOfCornersAtRockHeight = 0;
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                int rockTile;
                short rockHeight;
                int tile = mesh.getTile(tilex + x, tiley + y);
                short tileHeight = Tiles.decodeHeight(tile);
                if (tileHeight > (rockHeight = Tiles.decodeHeight(rockTile = Server.rockMesh.getTile(tilex + x, tiley + y)))) continue;
                ++numberOfCornersAtRockHeight;
            }
        }
        return numberOfCornersAtRockHeight == 4;
    }

    public static void setAsRock(int tilex, int tiley, boolean natural) {
        Terraforming.setAsRock(tilex, tiley, natural, false);
    }

    public static void setAsRock(int tilex, int tiley, boolean natural, boolean lavaflow) {
        int y;
        int x;
        MethodsHighways.removeNearbyMarkers(tilex, tiley, false);
        boolean keepTopLeftHeight = false;
        boolean keepTopRightHeight = false;
        boolean keepLowerLeftHeight = false;
        boolean keepLowerRightHeight = false;
        for (x = -1; x <= 1; ++x) {
            for (y = -1; y <= 1; ++y) {
                int t = Server.caveMesh.getTile(tilex + x, tiley + y);
                byte type = Tiles.decodeType(t);
                if (x == 0 && y == 0) {
                    if (type != Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                    if (lavaflow) {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_LAVA.id, (byte)0);
                        Server.rockMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_CAVE_WALL_LAVA.id, (byte)0));
                        Server.setWorldResource(tilex, tiley, 0);
                        Server.setCaveResource(tilex, tiley, Server.rand.nextInt(10000));
                    } else {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                        Server.rockMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0));
                        Server.setWorldResource(tilex, tiley, 0);
                        Server.setCaveResource(tilex, tiley, Server.rand.nextInt(10000));
                    }
                    MineDoorPermission.deleteMineDoor(x, y);
                    continue;
                }
                if (type != Tiles.Tile.TILE_CAVE.id && !Tiles.isReinforcedFloor(type) && type != Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                if (x == -1 && y == -1) {
                    keepTopLeftHeight = true;
                    continue;
                }
                if (x == 0 && y == -1) {
                    keepTopRightHeight = true;
                    keepTopLeftHeight = true;
                    continue;
                }
                if (x == 1 && y == -1) {
                    keepTopRightHeight = true;
                    continue;
                }
                if (x == -1 && y == 0) {
                    keepTopLeftHeight = true;
                    keepLowerLeftHeight = true;
                    continue;
                }
                if (x == 1 && y == 0) {
                    keepTopRightHeight = true;
                    keepLowerRightHeight = true;
                    continue;
                }
                if (x == -1 && y == 1) {
                    keepLowerLeftHeight = true;
                    continue;
                }
                if (x == 0 && y == 1) {
                    keepLowerRightHeight = true;
                    keepLowerLeftHeight = true;
                    continue;
                }
                if (x != 1 || y != 1) continue;
                keepLowerRightHeight = true;
            }
        }
        for (x = 0; x <= 1; ++x) {
            for (y = 0; y <= 1; ++y) {
                int encodedTile = Server.caveMesh.getTile(tilex + x, tiley + y);
                if (x == 0 && y == 0) {
                    byte tileType = Tiles.Tile.TILE_CAVE_WALL.id;
                    if (lavaflow || Tiles.decodeType(Server.surfaceMesh.getTile(tilex + x, tiley + y)) == Tiles.Tile.TILE_LAVA.id && (Tiles.decodeData(Server.surfaceMesh.getTile(tilex + x, tiley + y)) & 0xFF) == 255) {
                        tileType = Tiles.Tile.TILE_CAVE_WALL_LAVA.id;
                    } else if (natural) {
                        tileType = TileRockBehaviour.prospect(tilex + x, tiley + y, false);
                    }
                    if (keepTopLeftHeight) {
                        Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode(Tiles.decodeHeight(encodedTile), tileType, Tiles.decodeData(encodedTile)));
                        continue;
                    }
                    Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, tileType, (byte)0));
                    continue;
                }
                if (x == 1 && y == 0) {
                    if (keepTopRightHeight) continue;
                    Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)), (byte)0));
                    continue;
                }
                if (x == 0 && y == 1) {
                    if (keepLowerLeftHeight) continue;
                    Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)), (byte)0));
                    continue;
                }
                if (x != 1 || y != 1 || keepLowerRightHeight) continue;
                Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(Server.caveMesh.getTile(tilex + x, tiley + y)), (byte)0));
            }
        }
        Players.getInstance().sendChangedTiles(tilex, tiley, 2, 2, true, true);
        Players.getInstance().sendChangedTiles(tilex - 1, tiley - 1, 3, 3, false, true);
        Server.setCaveResource(tilex, tiley, 65535);
        int block = 0;
        r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 102533L);
        if (r.nextInt(100) == 0) {
            block = -1;
        } else {
            r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 123307L);
            if (r.nextInt(64) == 0) {
                block = -1;
            }
        }
        Zones.setMiningState(tilex, tiley, (byte)block, false);
        Zones.deleteMiningTile(tilex, tiley);
    }

    public static void forceSetAsRock(int tilex, int tiley, byte suggestedTile, int suggestedTileSeed) {
        boolean changed;
        byte newTileType;
        suggestedTileSeed = Math.max(suggestedTileSeed, 1);
        int surfaceTile = Server.surfaceMesh.getTile(tilex, tiley);
        boolean isLava = Tiles.decodeType(surfaceTile) == Tiles.Tile.TILE_LAVA.id;
        int initialTile = Server.caveMesh.getTile(tilex, tiley);
        byte oldTType = Tiles.decodeType(initialTile);
        byte by = newTileType = Tiles.isOreCave(oldTType) ? oldTType : Tiles.Tile.TILE_CAVE_WALL.id;
        if (isLava) {
            newTileType = Tiles.Tile.TILE_CAVE_WALL_LAVA.id;
        } else if (suggestedTile > 0 && Server.rand.nextInt(suggestedTileSeed) == 0) {
            logger.log(Level.INFO, "Setting " + tilex + "," + tiley + " to suggested " + Tiles.getTile((byte)suggestedTile).tiledesc);
            newTileType = suggestedTile;
        }
        boolean bl = changed = newTileType != oldTType;
        if (changed) {
            int y;
            int x;
            boolean keepTopLeftHeight = false;
            boolean keepTopRightHeight = false;
            boolean keepLowerLeftHeight = false;
            boolean keepLowerRightHeight = false;
            for (x = -1; x <= 1; ++x) {
                for (y = -1; y <= 1; ++y) {
                    int t = Server.caveMesh.getTile(tilex + x, tiley + y);
                    byte type = Tiles.decodeType(t);
                    if (x == 0 && y == 0 || type != Tiles.Tile.TILE_CAVE.id && type != Tiles.Tile.TILE_CAVE_EXIT.id && !Tiles.isReinforcedFloor(type)) continue;
                    if (x == -1 && y == -1) {
                        keepTopLeftHeight = true;
                        continue;
                    }
                    if (x == 0 && y == -1) {
                        keepTopRightHeight = true;
                        keepTopLeftHeight = true;
                        continue;
                    }
                    if (x == 1 && y == -1) {
                        keepTopRightHeight = true;
                        continue;
                    }
                    if (x == -1 && y == 0) {
                        keepTopLeftHeight = true;
                        keepLowerLeftHeight = true;
                        continue;
                    }
                    if (x == 1 && y == 0) {
                        keepTopRightHeight = true;
                        keepLowerRightHeight = true;
                        continue;
                    }
                    if (x == -1 && y == 1) {
                        keepLowerLeftHeight = true;
                        continue;
                    }
                    if (x == 0 && y == 1) {
                        keepLowerRightHeight = true;
                        keepLowerLeftHeight = true;
                        continue;
                    }
                    if (x != 1 || y != 1) continue;
                    keepLowerRightHeight = true;
                }
            }
            for (x = 0; x <= 1; ++x) {
                for (y = 0; y <= 1; ++y) {
                    int encodedTile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    boolean send = false;
                    if (x == 0 && y == 0) {
                        if (keepTopLeftHeight) {
                            if (Tiles.decodeType(encodedTile) != newTileType) {
                                Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode(Tiles.decodeHeight(encodedTile), newTileType, Tiles.decodeData(encodedTile)));
                                send = true;
                            }
                        } else if (Tiles.decodeHeight(encodedTile) != -100 || Tiles.decodeType(encodedTile) != newTileType || Tiles.decodeData(encodedTile) != 0) {
                            Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, newTileType, (byte)0));
                            send = true;
                        }
                    } else if (x == 1 && y == 0) {
                        if (!(keepTopRightHeight || Tiles.decodeHeight(encodedTile) == -100 && Tiles.decodeData(encodedTile) == 0)) {
                            Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(encodedTile), (byte)0));
                            send = true;
                        }
                    } else if (x == 0 && y == 1) {
                        if (!(keepLowerLeftHeight || Tiles.decodeHeight(encodedTile) == -100 && Tiles.decodeData(encodedTile) == 0)) {
                            Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(encodedTile), (byte)0));
                            send = true;
                        }
                    } else if (!(x != 1 || y != 1 || keepLowerRightHeight || Tiles.decodeHeight(encodedTile) == -100 && Tiles.decodeData(encodedTile) == 0)) {
                        Server.caveMesh.setTile(tilex + x, tiley + y, Tiles.encode((short)-100, Tiles.decodeType(encodedTile), (byte)0));
                        send = true;
                    }
                    if (!send) continue;
                    Players.getInstance().sendChangedTile(tilex + x, tiley + y, false, true);
                }
            }
            Server.setCaveResource(tilex, tiley, 65535);
            Zones.setMiningState(tilex, tiley, (byte)-1, false);
            Zones.deleteMiningTile(tilex, tiley);
        }
    }

    public static void caveIn(int tilex, int tiley) {
        Terraforming.setAsRock(tilex, tiley, true);
    }

    public static boolean isAllCornersInsideHeightRange(int tilex, int tiley, boolean surfaced, short maxheight, short minheight) {
        if (surfaced) {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                        return true;
                    }
                    short h = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                    if (minheight > h || h > maxheight) continue;
                    return true;
                }
            }
        } else {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                        return true;
                    }
                    short h = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
                    if (minheight > h && h > maxheight) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isCornerUnderWater(int tilex, int tiley, boolean surfaced) {
        if (surfaced) {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                        return true;
                    }
                    short h = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                    if (h > 0) continue;
                    return true;
                }
            }
        } else {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                        return true;
                    }
                    short h = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
                    if (h > 0) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTileUnderWater(int tile, int tilex, int tiley, boolean surfaced) {
        if (surfaced) {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                        return true;
                    }
                    if (!((double)Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y)) > -0.5)) continue;
                    return false;
                }
            }
        } else {
            if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                return false;
            }
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (!((double)Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y)) > -0.5)) continue;
                    return false;
                }
            }
        }
        return true;
    }

    static final boolean isWater(int tile, int tilex, int tiley, boolean surfaced) {
        if (surfaced) {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    if (Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y)) >= 0) continue;
                    return true;
                }
            }
        } else {
            if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                return false;
            }
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    int ttile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    if (Tiles.decodeHeight(ttile) >= 0) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFlat(int tilex, int tiley, boolean surfaced, int maxDifference) throws IllegalArgumentException {
        int y;
        int x;
        int lAverageHeight = 0;
        for (x = 0; x <= 1; ++x) {
            for (y = 0; y <= 1; ++y) {
                if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
                    throw new IllegalArgumentException("This tile is at the end of the world. Don't flatten it.");
                }
                short ch = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                if (!surfaced) {
                    ch = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
                }
                lAverageHeight += ch;
            }
        }
        lAverageHeight = (short)(lAverageHeight / 4);
        for (x = 0; x <= 1; ++x) {
            for (y = 0; y <= 1; ++y) {
                short h = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + x, tiley + y));
                if (!surfaced) {
                    h = Tiles.decodeHeight(Server.caveMesh.getTile(tilex + x, tiley + y));
                }
                if (h > lAverageHeight + maxDifference) {
                    return false;
                }
                if (h >= lAverageHeight - maxDifference) continue;
                return false;
            }
        }
        return true;
    }

    static boolean plantSprout(Creature performer, Item sprout, int tilex, int tiley, boolean onSurface, int tile, float counter, boolean inCenter) {
        boolean toReturn;
        block35: {
            toReturn = true;
            if (sprout.getTemplateId() == 266) {
                if (!onSurface) {
                    performer.getCommunicator().sendNormalServerMessage("The sprout would never grow inside a cave.");
                    return true;
                }
                byte type = Tiles.decodeType(tile);
                if (Terraforming.isTileGrowTree(type)) {
                    if (!Methods.isActionAllowed(performer, (short)660, tilex, tiley)) {
                        return true;
                    }
                    VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, onSurface);
                    if (vtile != null && vtile.getStructure() != null) {
                        if (vtile.getStructure().isTypeHouse()) {
                            performer.getCommunicator().sendNormalServerMessage("The sprout would never grow inside a house.");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("The sprout would never grow under a bridge.");
                        }
                        return true;
                    }
                    try {
                        Skill gardening;
                        Action act = performer.getCurrentAction();
                        double power = 0.0;
                        int time = 2000;
                        toReturn = false;
                        if (counter == 1.0f) {
                            gardening = null;
                            try {
                                gardening = performer.getSkills().getSkill(10045);
                            }
                            catch (NoSuchSkillException nss) {
                                gardening = performer.getSkills().learn(10045, 1.0f);
                            }
                            if (Terraforming.isCornerUnderWater(tilex, tiley, onSurface)) {
                                performer.getCommunicator().sendNormalServerMessage("The ground is too moist here, so the sprout would rot.");
                                return true;
                            }
                            time = Actions.getStandardActionTime(performer, gardening, sprout, 0.0);
                            act.setTimeLeft(time);
                            performer.getCommunicator().sendNormalServerMessage("You start planting the sprout.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to plant a sprout.", performer, 5);
                            performer.sendActionControl(Actions.actionEntrys[186].getVerbString(), true, time);
                        } else {
                            time = act.getTimeLeft();
                        }
                        if (!(counter * 10.0f > (float)time)) break block35;
                        gardening = null;
                        try {
                            gardening = performer.getSkills().getSkill(10045);
                        }
                        catch (NoSuchSkillException nss) {
                            gardening = performer.getSkills().learn(10045, 1.0f);
                        }
                        power = gardening.skillCheck(1.0f + sprout.getDamage(), sprout.getCurrentQualityLevel(), false, counter);
                        toReturn = true;
                        if (power > 0.0 && sprout.getMaterial() != 92) {
                            SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, onSurface, 0.0f);
                            TreeData.TreeType treeType = Materials.getTreeTypeForWood(sprout.getMaterial());
                            if (treeType != null) {
                                byte newData = Tiles.encodeTreeData(FoliageAge.YOUNG_ONE, false, inCenter, GrassData.GrowthTreeStage.SHORT);
                                if (type == Tiles.Tile.TILE_MYCELIUM.id) {
                                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), treeType.asMyceliumTree(), newData);
                                } else {
                                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), treeType.asNormalTree(), newData);
                                }
                            } else {
                                BushData.BushType bushType = Materials.getBushTypeForWood(sprout.getMaterial());
                                byte newData = Tiles.encodeTreeData(FoliageAge.YOUNG_ONE, false, inCenter, GrassData.GrowthTreeStage.SHORT);
                                if (type == Tiles.Tile.TILE_MYCELIUM.id) {
                                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), bushType.asMyceliumBush(), newData);
                                } else {
                                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), bushType.asNormalBush(), newData);
                                }
                            }
                            Server.setWorldResource(tilex, tiley, 0);
                            performer.getMovementScheme().touchFreeMoveCounter();
                            Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
                            if (performer.getDeity() != null && performer.getDeity().number == 1) {
                                performer.maybeModifyAlignment(1.0f);
                            }
                            String tosend = "You plant the sprout.";
                            performer.achievement(119);
                            double gard = gardening.getKnowledge(0.0);
                            if (gard > 50.0) {
                                if (gard < 60.0) {
                                    tosend = "You plant the sprout, and you can almost feel it start sucking nutrition from the earth.";
                                } else if (gard < 70.0) {
                                    tosend = "You plant the sprout, and you get a weird feeling that the plant thanks you.";
                                } else if (gard < 80.0) {
                                    tosend = "You plant the sprout, and you see the plant perform an almost unnoticable bow as it whispers its thanks.";
                                } else if (gard < 100.0) {
                                    tosend = "You plant the sprout. As you see the plant bow you hear the voice in your head of hundreds of plants thanking you.";
                                }
                            }
                            performer.getStatus().modifyStamina(-1000.0f);
                            performer.getCommunicator().sendNormalServerMessage(tosend);
                            Server.getInstance().broadCastAction(performer.getName() + " plants a sprout.", performer, 5);
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("Sadly, the sprout does not survive despite your best efforts.");
                        }
                        Items.destroyItem(sprout.getWurmId());
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.WARNING, performer.getName() + ": " + nsa.getMessage(), nsa);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You cannot plant a tree there.");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You need to plant with a sprout, not a " + sprout.getName() + ".");
            }
        }
        return toReturn;
    }

    /*
     * WARNING - void declaration
     */
    static final boolean createMagicWall(Creature performer, Item source, int tilex, int tiley, int heightOffset, boolean onSurface, Tiles.TileBorderDirection dir, float counter, Action act) {
        boolean toReturn = true;
        if (source.getTemplateId() == 764) {
            if (source.getWeightGrams() < 1000) {
                performer.getCommunicator().sendNormalServerMessage("There is too little " + source.getName() + ". You probably will need at least " + 1000 + " g.");
                return true;
            }
            if (!onSurface) {
                performer.getCommunicator().sendNormalServerMessage("You can not reach that.");
                return true;
            }
            VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, onSurface);
            if (vtile != null && vtile.getStructure() != null) {
                performer.getCommunicator().sendNormalServerMessage("The source would never work inside.");
                return true;
            }
            double power = 0.0;
            int time = 2000;
            toReturn = false;
            if (counter == 1.0f) {
                Creature c;
                Object t2;
                VolaTile t1;
                int n;
                VolaTile t = Zones.getTileOrNull(tilex, tiley, onSurface);
                if (t != null) {
                    void var19_28;
                    CounterTypes[] fences;
                    Wall[] walls;
                    for (Wall wall : walls = t.getWallsForLevel(heightOffset / 30)) {
                        if (wall.isHorizontal() != (dir == Tiles.TileBorderDirection.DIR_HORIZ) || wall.getStartX() != tilex || wall.getStartY() != tiley) continue;
                        return true;
                    }
                    CounterTypes[] counterTypesArray = fences = t.getFencesForDir(dir);
                    n = counterTypesArray.length;
                    boolean bl = false;
                    while (var19_28 < n) {
                        CounterTypes f = counterTypesArray[var19_28];
                        if (((Fence)f).getHeightOffset() == heightOffset) {
                            return true;
                        }
                        ++var19_28;
                    }
                }
                if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
                    t1 = Zones.getTileOrNull(tilex, tiley, onSurface);
                    if (t1 != null) {
                        for (CounterTypes counterTypes : t1.getCreatures()) {
                            if (!((Creature)counterTypes).isPlayer()) continue;
                            return true;
                        }
                    }
                    if ((t2 = Zones.getTileOrNull(tilex - 1, tiley, onSurface)) != null) {
                        void var19_31;
                        Creature[] creatureArray = ((VolaTile)t2).getCreatures();
                        n = creatureArray.length;
                        boolean bl = false;
                        while (var19_31 < n) {
                            c = creatureArray[var19_31];
                            if (c.isPlayer()) {
                                return true;
                            }
                            ++var19_31;
                        }
                    }
                } else {
                    t1 = Zones.getTileOrNull(tilex, tiley, onSurface);
                    if (t1 != null) {
                        t2 = t1.getCreatures();
                        int n2 = ((Creature[])t2).length;
                        for (n = 0; n < n2; ++n) {
                            Object object = t2[n];
                            if (!((Creature)object).isPlayer()) continue;
                            return false;
                        }
                    }
                    if ((t2 = Zones.getTileOrNull(tilex, tiley - 1, onSurface)) != null) {
                        void var19_34;
                        Creature[] creatureArray = ((VolaTile)t2).getCreatures();
                        n = creatureArray.length;
                        boolean bl = false;
                        while (var19_34 < n) {
                            c = creatureArray[var19_34];
                            if (c.isPlayer()) {
                                return false;
                            }
                            ++var19_34;
                        }
                    }
                }
                Skill mind = null;
                try {
                    mind = performer.getSkills().getSkill(100);
                }
                catch (NoSuchSkillException nss) {
                    mind = performer.getSkills().learn(100, 1.0f);
                }
                time = Actions.getQuickActionTime(performer, mind, source, 0.0);
                act.setTimeLeft(time);
                performer.getCommunicator().sendNormalServerMessage("You start to weave the source.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to weave the source.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[512].getVerbString(), true, time);
            } else {
                time = act.getTimeLeft();
            }
            if (counter * 10.0f > (float)time) {
                Skill mind = null;
                try {
                    mind = performer.getSkills().getSkill(100);
                }
                catch (NoSuchSkillException nss) {
                    mind = performer.getSkills().learn(100, 1.0f);
                }
                power = mind.skillCheck(mind.getRealKnowledge(), source.getCurrentQualityLevel(), false, counter);
                toReturn = true;
                if (power > 0.0) {
                    SoundPlayer.playSound("sound.religion.channel", tilex, tiley, onSurface, 0.0f);
                    try {
                        Zone zone = Zones.getZone(tilex, tiley, true);
                        DbFence fence = new DbFence(StructureConstantsEnum.FENCE_MAGIC_STONE, tilex, tiley, heightOffset, 1.0f, dir, zone.getId(), performer.getLayer());
                        fence.setState(fence.getFinishState());
                        ((Fence)fence).setQualityLevel((float)power);
                        ((Fence)fence).improveOrigQualityLevel((float)power);
                        zone.addFence(fence);
                        performer.achievement(320);
                        performer.getCommunicator().sendNormalServerMessage("You weave the source and create a wall.");
                        Server.getInstance().broadCastAction(performer.getName() + " creates a wall.", performer, 5);
                    }
                    catch (NoSuchZoneException noSuchZoneException) {}
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Sadly, you fail to weave the source properly.");
                }
                source.setWeight(source.getWeightGrams() - 1000, true);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You need to use the source, not a " + source.getName() + ".");
        }
        return toReturn;
    }

    /*
     * WARNING - void declaration
     */
    static final boolean plantFlowerbed(Creature performer, Item flower, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, float counter, Action act) {
        boolean toReturn = true;
        if (flower.isFlower()) {
            if (!onSurface) {
                performer.getCommunicator().sendNormalServerMessage("You can not reach that.");
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            int tile = Server.surfaceMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(tile);
            int diffx = 0;
            int diffy = 0;
            if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
                diffx = -1;
            } else {
                diffy = -1;
            }
            int tile2 = Server.surfaceMesh.getTile(tilex + diffx, tiley + diffy);
            byte type2 = Tiles.decodeType(tile2);
            if (Terraforming.isTileGrowHedge(type) || Terraforming.isTileGrowHedge(type2)) {
                Item[] inventoryItems;
                int plankCount;
                boolean dirtFound;
                boolean nailsFound;
                boolean planksFound;
                boolean flowersFound;
                int found;
                StructureConstantsEnum fenceType;
                VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, onSurface);
                if (vtile != null && vtile.getStructure() != null) {
                    performer.getCommunicator().sendNormalServerMessage("The flowers would never grow inside.");
                    performer.getCommunicator().sendActionResult(false);
                    return true;
                }
                double power = 0.0;
                int time = 2000;
                toReturn = false;
                if (counter == 1.0f) {
                    void var27_30;
                    fenceType = Fence.getFlowerbedType(flower.getTemplateId());
                    if (fenceType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
                        performer.getCommunicator().sendNormalServerMessage("Nobody has managed to grow those in flowerbeds yet.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    found = 0;
                    flowersFound = false;
                    planksFound = false;
                    nailsFound = false;
                    dirtFound = false;
                    plankCount = 0;
                    Item[] itemArray = inventoryItems = performer.getInventory().getAllItems(false);
                    int n = itemArray.length;
                    for (int i = 0; i < n; ++i) {
                        Item item = itemArray[i];
                        if (item.getTemplateId() == flower.getTemplateId() && !flowersFound) {
                            if (item != flower && ++found >= 4) {
                                flowersFound = true;
                            }
                        } else if (item.getTemplateId() == 22 && !planksFound) {
                            if (++plankCount >= 3) {
                                planksFound = true;
                            }
                        } else if (item.getTemplateId() == 26 && !dirtFound) {
                            if (item.getWeightGrams() >= item.getTemplate().getWeightGrams()) {
                                dirtFound = true;
                            }
                        } else if (item.getTemplateId() == 218 && !nailsFound) {
                            nailsFound = true;
                        }
                        if (flowersFound && planksFound && nailsFound && dirtFound) break;
                    }
                    if (!(flowersFound && planksFound && nailsFound && dirtFound)) {
                        performer.getCommunicator().sendNormalServerMessage("You need to have at least 5 flowers of the same kind and 3 planks, 1 small nails and atleast 20kg of dirt in your inventory.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    Object var27_27 = null;
                    try {
                        Skill skill = performer.getSkills().getSkill(10045);
                    }
                    catch (NoSuchSkillException nss) {
                        Skill skill = performer.getSkills().learn(10045, 1.0f);
                    }
                    if (Terraforming.isCornerUnderWater(tilex, tiley, onSurface)) {
                        performer.getCommunicator().sendNormalServerMessage("The ground is too moist here, so the flowers would rot.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    time = Actions.getStandardActionTime(performer, (Skill)var27_30, flower, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start planting the flowers.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to plant some flowers.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[563].getVerbString(), true, time);
                } else {
                    time = act.getTimeLeft();
                }
                if (counter * 10.0f > (float)time) {
                    fenceType = Fence.getFlowerbedType(flower.getTemplateId());
                    if (fenceType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
                        performer.getCommunicator().sendNormalServerMessage("Nobody has managed to grow those in flowerbeds yet.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    found = 0;
                    flowersFound = false;
                    planksFound = false;
                    nailsFound = false;
                    dirtFound = false;
                    plankCount = 0;
                    for (Item item : inventoryItems = performer.getInventory().getAllItems(false)) {
                        if (item.getTemplateId() == flower.getTemplateId() && !flowersFound) {
                            if (item != flower && ++found >= 4) {
                                flowersFound = true;
                            }
                        } else if (item.getTemplateId() == 22 && !planksFound) {
                            if (++plankCount >= 3) {
                                planksFound = true;
                            }
                        } else if (item.getTemplateId() == 26 && !dirtFound) {
                            if (item.getWeightGrams() >= item.getTemplate().getWeightGrams()) {
                                dirtFound = true;
                            }
                        } else if (item.getTemplateId() == 218 && !nailsFound) {
                            nailsFound = true;
                        }
                        if (flowersFound && planksFound && nailsFound && dirtFound) break;
                    }
                    if (found < 4) {
                        performer.getCommunicator().sendNormalServerMessage("You need to have at least 5 flowers of the same kind and 3 planks, 1 small nails and atleast 20kg of dirt in your inventory.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    float f = flower.getQualityLevel();
                    float dam = flower.getDamage();
                    boolean flowersDone = false;
                    boolean dirtDone = false;
                    boolean planksDone = false;
                    boolean nailsDone = false;
                    for (Item item : inventoryItems) {
                        if (item.getTemplateId() == flower.getTemplateId() && !flowersDone) {
                            if (item != flower && found > 0) {
                                f += item.getQualityLevel();
                                dam += item.getDamage();
                                if (--found <= 0) {
                                    flowersDone = true;
                                }
                            }
                        } else if (item.getTemplateId() == 22 && !planksDone) {
                            if (plankCount > 0) {
                                f += item.getQualityLevel();
                                dam += item.getDamage();
                                if (--plankCount <= 0) {
                                    planksDone = true;
                                }
                            }
                        } else if (item.getTemplateId() == 218 && !nailsDone) {
                            f += item.getQualityLevel();
                            dam += item.getDamage();
                            nailsDone = true;
                        } else if (item.getTemplateId() == 26 && !dirtDone) {
                            f += item.getQualityLevel();
                            dam += item.getDamage();
                            dirtDone = true;
                        }
                        if (flowersDone && dirtDone && planksDone && nailsDone) break;
                    }
                    f /= 10.0f;
                    dam /= 10.0f;
                    Skill gardening = null;
                    try {
                        gardening = performer.getSkills().getSkill(10045);
                    }
                    catch (NoSuchSkillException nss) {
                        gardening = performer.getSkills().learn(10045, 1.0f);
                    }
                    power = gardening.skillCheck(1.0f + dam, f, false, counter);
                    toReturn = true;
                    if (power > 0.0) {
                        SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, onSurface, 0.0f);
                        try {
                            Zone zone = Zones.getZone(tilex, tiley, true);
                            DbFence fence = new DbFence(Fence.getFlowerbedType(flower.getTemplateId()), tilex, tiley, 0, 1.0f, dir, zone.getId(), performer.getLayer());
                            try {
                                fence.setState(fence.getFinishState());
                                ((Fence)fence).setQualityLevel((float)power);
                                ((Fence)fence).improveOrigQualityLevel((float)power);
                                ((Fence)fence).save();
                                zone.addFence(fence);
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, iox.getMessage(), iox);
                            }
                            if (performer.getDeity() != null && performer.getDeity().number == 1) {
                                performer.maybeModifyAlignment(1.0f);
                            }
                            found = 4;
                            plankCount = 3;
                            dirtDone = false;
                            planksDone = false;
                            flowersDone = false;
                            nailsDone = false;
                            for (Item item : inventoryItems) {
                                if (item.getTemplateId() == flower.getTemplateId() && !flowersDone) {
                                    if (item != flower && found > 0) {
                                        Items.destroyItem(item.getWurmId());
                                        if (--found <= 0) {
                                            flowersDone = true;
                                        }
                                    }
                                } else if (item.getTemplateId() == 26 && !dirtDone) {
                                    Items.destroyItem(item.getWurmId());
                                    dirtDone = true;
                                } else if (item.getTemplateId() == 22 && !planksDone) {
                                    Items.destroyItem(item.getWurmId());
                                    if (--plankCount <= 0) {
                                        planksDone = true;
                                    }
                                } else if (item.getTemplateId() == 218 && !nailsDone) {
                                    Items.destroyItem(item.getWurmId());
                                    nailsDone = true;
                                }
                                if (flowersDone && dirtDone && planksDone && nailsDone) break;
                            }
                            String tosend = "You plant the flowers and create a fine flowerbed.";
                            performer.achievement(318);
                            double gard = gardening.getKnowledge(0.0);
                            if (gard > 50.0 && Server.rand.nextBoolean()) {
                                if (gard < 60.0) {
                                    tosend = "You plant the flowerbed, and you can almost feel the plants start sucking nutrition from the earth.";
                                } else if (gard < 70.0) {
                                    tosend = "You plant the flowerbed, and you get a weird feeling that the plants thanks you.";
                                } else if (gard < 80.0) {
                                    tosend = "You plant the flowerbed, and you see the plants perform an almost unnoticable bow as they whisper their thanks.";
                                } else if (gard < 100.0) {
                                    tosend = "You plant the flowerbed. As you see the plants bow you hear the voice in your head of hundreds of other plants thanking you.";
                                    performer.getStatus().modifyStamina(-1000.0f);
                                }
                            }
                            TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 563);
                            performer.getCommunicator().sendNormalServerMessage(tosend);
                            Server.getInstance().broadCastAction(performer.getName() + " plants a flowerbed.", performer, 5);
                            performer.getCommunicator().sendActionResult(true);
                        }
                        catch (NoSuchZoneException noSuchZoneException) {}
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("Sadly, the flowers do not survive despite your best efforts.");
                        performer.getCommunicator().sendActionResult(false);
                    }
                    Items.destroyItem(flower.getWurmId());
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot plant a flowerbed there.");
                performer.getCommunicator().sendActionResult(false);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You need to plant with a flower, not a " + flower.getName() + ".");
            performer.getCommunicator().sendActionResult(false);
        }
        return toReturn;
    }

    /*
     * WARNING - void declaration
     */
    static final boolean plantHedge(Creature performer, Item sprout, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, float counter, Action act) {
        boolean toReturn = true;
        if (sprout.getTemplateId() == 266) {
            if (!onSurface || !performer.isOnSurface()) {
                performer.getCommunicator().sendNormalServerMessage("The hedge would never grow inside a cave.");
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            int tile = Server.surfaceMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(tile);
            int diffx = 0;
            int diffy = 0;
            if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
                diffx = -1;
            } else {
                diffy = -1;
            }
            int tile2 = Server.surfaceMesh.getTile(tilex + diffx, tiley + diffy);
            byte type2 = Tiles.decodeType(tile2);
            if (Terraforming.isTileGrowHedge(type) || Terraforming.isTileGrowHedge(type2)) {
                Item[] inventoryItems;
                int found;
                StructureConstantsEnum fenceType;
                if (!Methods.isActionAllowed(performer, (short)660, tilex, tiley)) {
                    return true;
                }
                byte treeMaterial = sprout.getMaterial();
                double power = 0.0;
                int time = 2000;
                toReturn = false;
                if (counter == 1.0f) {
                    void var22_25;
                    Item item;
                    fenceType = Fence.getLowHedgeType(treeMaterial);
                    if (fenceType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
                        performer.getCommunicator().sendNormalServerMessage("Nobody has managed to grow those in hedges yet.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    found = 0;
                    Item[] itemArray = inventoryItems = performer.getInventory().getAllItems(false);
                    int n = itemArray.length;
                    for (int i = 0; i < n && ((item = itemArray[i]).getTemplateId() != 266 || item == sprout || item.getMaterial() != treeMaterial || ++found < 4); ++i) {
                    }
                    if (found < 4) {
                        performer.getCommunicator().sendNormalServerMessage("You need to have at least 5 sprouts of the same kind in your inventory.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    Object var22_22 = null;
                    try {
                        Skill skill = performer.getSkills().getSkill(10045);
                    }
                    catch (NoSuchSkillException nss) {
                        Skill skill = performer.getSkills().learn(10045, 1.0f);
                    }
                    if (Terraforming.isCornerUnderWater(tilex, tiley, onSurface)) {
                        performer.getCommunicator().sendNormalServerMessage("The ground is too moist here, so the sprout would rot.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    time = Actions.getStandardActionTime(performer, (Skill)var22_25, sprout, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start planting the sprout.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to plant a sprout.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[186].getVerbString(), true, time);
                } else {
                    time = act.getTimeLeft();
                }
                if (counter * 10.0f > (float)time) {
                    fenceType = Fence.getLowHedgeType(treeMaterial);
                    if (fenceType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
                        performer.getCommunicator().sendNormalServerMessage("Nobody has managed to grow those in hedges yet.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    found = 0;
                    for (Item item : inventoryItems = performer.getInventory().getAllItems(false)) {
                        if (item.getTemplateId() == 266 && item != sprout && item.getMaterial() == treeMaterial && ++found >= 4) break;
                    }
                    if (found < 4) {
                        performer.getCommunicator().sendNormalServerMessage("You need to have at least 5 sprouts of the same kind in your inventory.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    float f = sprout.getQualityLevel();
                    float dam = sprout.getDamage();
                    for (Item item : inventoryItems) {
                        if (item.getTemplateId() != 266 || item == sprout || found <= 0 || item.getMaterial() != treeMaterial) continue;
                        f += item.getQualityLevel();
                        dam += item.getDamage();
                        if (--found <= 0) break;
                    }
                    f /= 5.0f;
                    dam /= 5.0f;
                    Skill gardening = null;
                    try {
                        gardening = performer.getSkills().getSkill(10045);
                    }
                    catch (NoSuchSkillException nss) {
                        gardening = performer.getSkills().learn(10045, 1.0f);
                    }
                    power = gardening.skillCheck(1.0f + dam, f, false, counter);
                    toReturn = true;
                    if (power > 0.0) {
                        SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, onSurface, 0.0f);
                        try {
                            Zone zone = Zones.getZone(tilex, tiley, true);
                            DbFence fence = new DbFence(Fence.getLowHedgeType(treeMaterial), tilex, tiley, 0, 1.0f, dir, zone.getId(), performer.getLayer());
                            try {
                                fence.setState(fence.getFinishState());
                                ((Fence)fence).setQualityLevel((float)power);
                                ((Fence)fence).improveOrigQualityLevel((float)power);
                                ((Fence)fence).save();
                                zone.addFence(fence);
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, iox.getMessage(), iox);
                            }
                            if (performer.getDeity() != null && performer.getDeity().number == 1) {
                                performer.maybeModifyAlignment(1.0f);
                            }
                            found = 4;
                            for (Item item : inventoryItems) {
                                if (item.getTemplateId() != 266 || item == sprout || found <= 0 || item.getMaterial() != treeMaterial) continue;
                                Items.destroyItem(item.getWurmId());
                                if (--found <= 0) break;
                            }
                            String tosend = "You plant the sprouts and create a fine hedge.";
                            performer.achievement(318);
                            double gard = gardening.getKnowledge(0.0);
                            if (gard > 50.0 && Server.rand.nextBoolean()) {
                                if (gard < 60.0) {
                                    tosend = "You plant the hedge, and you can almost feel the plants start sucking nutrition from the earth.";
                                } else if (gard < 70.0) {
                                    tosend = "You plant the hedge, and you get a weird feeling that the plants thanks you.";
                                } else if (gard < 80.0) {
                                    tosend = "You plant the hedge, and you see the plants perform an almost unnoticable bow as they whisper their thanks.";
                                } else if (gard < 100.0) {
                                    tosend = "You plant the hedge. As you see the plants bow you hear the voice in your head of hundreds of other plants thanking you.";
                                    performer.getStatus().modifyStamina(-1000.0f);
                                }
                            }
                            TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), 186);
                            performer.getCommunicator().sendNormalServerMessage(tosend);
                            Server.getInstance().broadCastAction(performer.getName() + " plants a hedge.", performer, 5);
                            performer.getCommunicator().sendActionResult(true);
                        }
                        catch (NoSuchZoneException noSuchZoneException) {}
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("Sadly, the sprout does not survive despite your best efforts.");
                        performer.getCommunicator().sendActionResult(false);
                    }
                    Items.destroyItem(sprout.getWurmId());
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot plant a hedge there.");
                performer.getCommunicator().sendActionResult(false);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You need to plant with a sprout, not a " + sprout.getName() + ".");
            performer.getCommunicator().sendActionResult(false);
        }
        return toReturn;
    }

    static boolean plantFlower(Creature performer, Item flower, int tilex, int tiley, boolean onSurface, int tile, float counter) {
        boolean toReturn;
        block32: {
            toReturn = true;
            VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, onSurface);
            if (vtile != null && vtile.getStructure() != null && vtile.getStructure().isTypeHouse() && flower.getTemplateId() != 756) {
                performer.getCommunicator().sendNormalServerMessage("The " + flower.getName() + " would never grow inside a building.");
                return true;
            }
            if (!Methods.isActionAllowed(performer, (short)186, tilex, tiley)) {
                return true;
            }
            try {
                Skill gardening;
                Action act = performer.getCurrentAction();
                double power = 0.0;
                int time = 2000;
                toReturn = false;
                if (counter == 1.0f) {
                    gardening = null;
                    try {
                        gardening = performer.getSkills().getSkill(10045);
                    }
                    catch (NoSuchSkillException nss) {
                        gardening = performer.getSkills().learn(10045, 1.0f);
                    }
                    if (Terraforming.isCornerUnderWater(tilex, tiley, onSurface)) {
                        performer.getCommunicator().sendNormalServerMessage("The ground is too moist here, so the " + flower.getName() + " would rot.");
                        return true;
                    }
                    time = Actions.getStandardActionTime(performer, gardening, flower, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start planting the " + flower.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to plant some " + flower.getName() + ".", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[186].getVerbString(), true, time);
                } else {
                    time = act.getTimeLeft();
                }
                if (!(counter * 10.0f > (float)time)) break block32;
                gardening = null;
                try {
                    gardening = performer.getSkills().getSkill(10045);
                }
                catch (NoSuchSkillException nss) {
                    gardening = performer.getSkills().learn(10045, 1.0f);
                }
                power = gardening.skillCheck(10.0f + flower.getDamage(), flower.getCurrentQualityLevel(), false, counter);
                toReturn = true;
                if (power > 0.0) {
                    byte newData = TileGrassBehaviour.getDataForFlower(flower.getTemplateId());
                    byte nty = Tiles.Tile.TILE_GRASS.id;
                    if (flower.getTemplateId() == 620) {
                        nty = Tiles.Tile.TILE_STEPPE.id;
                    } else if (flower.getTemplateId() == 479) {
                        nty = Tiles.Tile.TILE_MOSS.id;
                    }
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), nty, newData);
                    Server.setWorldResource(tilex, tiley, 0);
                    Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
                    try {
                        Zone z = Zones.getZone(tilex, tiley, true);
                        z.changeTile(tilex, tiley);
                    }
                    catch (NoSuchZoneException z) {
                        // empty catch block
                    }
                    String tosend = "You plant the " + flower.getName() + ".";
                    performer.achievement(123);
                    double gard = gardening.getKnowledge(0.0);
                    if (gard > 60.0 && flower.getTemplate().isFlower()) {
                        if (gard < 70.0) {
                            tosend = "You plant the " + flower.getName() + ", and you can almost feel them start sucking nutrition from the earth.";
                        } else if (gard < 80.0) {
                            tosend = "You plant the " + flower.getName() + ", and you get a weird feeling that they thank you.";
                        } else if (gard < 90.0) {
                            tosend = "You plant the " + flower.getName() + ", and you see them perform an almost unnoticable bow. Or was it the wind?";
                        } else if (gard < 100.0) {
                            tosend = "You plant the " + flower.getName() + ". As you see them bow you hear their thankful tiny voices in your head.";
                            performer.getStatus().modifyStamina(-1000.0f);
                        }
                    } else {
                        tosend = "You plant the " + flower.getName();
                        performer.getStatus().modifyStamina(-1000.0f);
                    }
                    if (performer.getDeity() != null && performer.getDeity().number == 1) {
                        performer.maybeModifyAlignment(1.0f);
                    }
                    performer.getCommunicator().sendNormalServerMessage(tosend);
                    Server.getInstance().broadCastAction(performer.getName() + " plants some " + flower.getName() + ".", performer, 5);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Sadly, the " + flower.getName() + " do not survive despite your best efforts.");
                }
                if (!flower.isFlower()) {
                    int weight = flower.getFullWeight();
                    if (weight <= flower.getTemplate().getWeightGrams()) {
                        Items.destroyItem(flower.getWurmId());
                    } else {
                        flower.setWeight(weight -= flower.getTemplate().getWeightGrams(), false);
                    }
                } else {
                    Items.destroyItem(flower.getWurmId());
                }
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.WARNING, performer.getName() + ": " + nsa.getMessage(), nsa);
            }
        }
        return toReturn;
    }

    static boolean pickFlower(Creature performer, Item sickle, int tilex, int tiley, int tile, float counter, Action act) {
        boolean toReturn = true;
        if (sickle.getTemplateId() == 267 || sickle.getTemplateId() == 176) {
            byte tileData = Tiles.decodeData(tile);
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put the flowers.");
                return true;
            }
            GrassData.FlowerType flowerType = GrassData.FlowerType.decodeTileData(tileData);
            if (Tiles.decodeType(tile) != Tiles.Tile.TILE_GRASS.id || flowerType == GrassData.FlowerType.NONE) {
                performer.getCommunicator().sendNormalServerMessage("No flowers grow here.");
                return true;
            }
            toReturn = false;
            int time = act.getTimeLeft();
            if (counter == 1.0f) {
                try {
                    int weight = ItemTemplateFactory.getInstance().getTemplate(498).getWeightGrams();
                    if (!performer.canCarry(weight)) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the flowers. You need to drop some things first.");
                        return true;
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                    return true;
                }
                Skill gardening = null;
                Skill sickskill = null;
                try {
                    gardening = performer.getSkills().getSkill(10045);
                }
                catch (NoSuchSkillException nss) {
                    gardening = performer.getSkills().learn(10045, 1.0f);
                }
                try {
                    sickskill = performer.getSkills().getSkill(10046);
                }
                catch (NoSuchSkillException nss) {
                    sickskill = performer.getSkills().learn(10046, 1.0f);
                }
                time = Actions.getStandardActionTime(performer, gardening, sickle, sickskill.getKnowledge(0.0));
                performer.getCommunicator().sendNormalServerMessage("You start picking the flowers.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to pick some flowers.", performer, 5);
                performer.sendActionControl("picking flowers", true, time);
                act.setTimeLeft(time);
            }
            if (counter * 10.0f >= (float)time) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                try {
                    int weight = ItemTemplateFactory.getInstance().getTemplate(498).getWeightGrams();
                    if (!performer.canCarry(weight)) {
                        performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the flowers. You need to drop some things first.");
                        return true;
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                    return true;
                }
                sickle.setDamage(sickle.getDamage() + 0.003f * sickle.getDamageModifier());
                double bonus = 0.0;
                double power = 0.0;
                Skill gardening = null;
                Skill sickskill = null;
                try {
                    gardening = performer.getSkills().getSkill(10045);
                }
                catch (NoSuchSkillException nss) {
                    gardening = performer.getSkills().learn(10045, 1.0f);
                }
                try {
                    sickskill = performer.getSkills().getSkill(10046);
                }
                catch (NoSuchSkillException nss) {
                    sickskill = performer.getSkills().learn(10046, 1.0f);
                }
                bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                power = gardening.skillCheck(1.0, sickle, bonus, false, counter);
                toReturn = true;
                try {
                    float modifier = 1.0f;
                    if (sickle.getSpellEffects() != null) {
                        modifier = sickle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                    }
                    GrassData.GrowthStage growthStage = GrassData.GrowthStage.decodeTileData(tileData);
                    Item flower = ItemFactory.createItem(TileGrassBehaviour.getFlowerTypeFor(flowerType), Math.max(1.0f, Math.min(100.0f, (float)power * modifier + (float)sickle.getRarity())), act.getRarity(), null);
                    if (power < 0.0) {
                        flower.setDamage((float)(-power) / 2.0f);
                    }
                    performer.getInventory().insertItem(flower);
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_GRASS.id, GrassData.encodeGrassTileData(growthStage, GrassData.FlowerType.NONE));
                    Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                    performer.getCommunicator().sendNormalServerMessage("You pick some flowers.");
                    Server.getInstance().broadCastAction(performer.getName() + " picks some flowers.", performer, 5);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for flowers!", nst);
                    performer.getCommunicator().sendNormalServerMessage("You fail to pick the flowers. You realize something is wrong with the world.");
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                    performer.getCommunicator().sendNormalServerMessage("You fail to pick the flowers. You realize something is wrong with the world.");
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot pick sprouts with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to pick sprout with a " + sickle.getName());
        }
        return toReturn;
    }

    static boolean growFarm(Creature performer, int tile, int tilex, int tiley, boolean onSurface) {
        int data = Tiles.decodeData(tile) & 0xFF;
        byte type = Tiles.decodeType(tile);
        int tileState = data >> 4;
        int tileAge = tileState & 7;
        if (tileAge < 7) {
            int crop = data & 0xF;
            ++tileAge;
            if (!onSurface) {
                Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), type, (byte)((tileAge << 4) + crop & 0xFF)));
            } else {
                Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), type, (byte)((tileAge << 4) + crop & 0xFF));
            }
            if (WurmCalendar.isNight()) {
                SoundPlayer.playSound("sound.birdsong.bird1", tilex, tiley, onSurface, 3.0f);
            } else {
                SoundPlayer.playSound("sound.birdsong.bird2", tilex, tiley, onSurface, 3.0f);
            }
            Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
        }
        return true;
    }

    static boolean harvest(Creature performer, int tilex, int tiley, boolean onSurface, int tile, float counter, @Nullable Item item) {
        boolean done = true;
        byte type = Tiles.decodeType(tile);
        if (type != Tiles.Tile.TILE_FIELD.id && type != Tiles.Tile.TILE_FIELD2.id) {
            return done;
        }
        byte data = Tiles.decodeData(tile);
        byte tileAge = Crops.decodeFieldAge(data);
        int crop = Crops.getCropNumber(type, data);
        if (!performer.getInventory().mayCreatureInsertItem()) {
            performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to keep whatever you harvest.");
            return true;
        }
        if (crop > 3 || item != null && item.getTemplateId() == 268) {
            if (tileAge != 0 && tileAge != 7) {
                Skill farming;
                double diff = Crops.getDifficultyFor(crop);
                done = false;
                Action act = null;
                try {
                    act = performer.getCurrentAction();
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.WARNING, nsa.getMessage(), nsa);
                    return true;
                }
                int time = 100;
                if (counter == 1.0f) {
                    farming = performer.getSkills().getSkillOrLearn(10049);
                    time = Actions.getStandardActionTime(performer, farming, null, 0.0);
                    act.setTimeLeft(time);
                    performer.getCommunicator().sendNormalServerMessage("You start harvesting the field.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts harvesting the field.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[152].getVerbString(), true, time);
                    performer.getStatus().modifyStamina(-1000.0f);
                } else {
                    time = act.getTimeLeft();
                }
                if (crop <= 3 && item != null && act.justTickedSecond()) {
                    item.setDamage(item.getDamage() + 3.0E-4f * item.getDamageModifier());
                }
                if (act.justTickedSecond()) {
                    if (act.currentSecond() % 5 == 0) {
                        performer.getStatus().modifyStamina(-10000.0f);
                    }
                    if (act.mayPlaySound()) {
                        if (crop <= 3 && item != null && item.getTemplateId() == 268) {
                            Methods.sendSound(performer, "sound.work.farming.scythe");
                        } else {
                            Methods.sendSound(performer, "sound.work.farming.harvest");
                        }
                    }
                }
                if (counter * 10.0f > (float)time) {
                    if (act.getRarity() != 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                    }
                    farming = performer.getSkills().getSkillOrLearn(10049);
                    double power = farming.skillCheck(diff, 0.0, false, counter);
                    Skill primskill = null;
                    byte itemRarity = 0;
                    if (crop <= 3 && item != null && item.getTemplateId() == 268) {
                        itemRarity = item.getRarity();
                        try {
                            int primarySkill = item.getPrimarySkill();
                            primskill = performer.getSkills().getSkillOrLearn(primarySkill);
                        }
                        catch (NoSuchSkillException nss) {
                            logger.log(Level.WARNING, "Scythe has no prim skill? :" + nss.getMessage(), nss);
                        }
                    }
                    if (primskill != null) {
                        Math.max(0.0, primskill.skillCheck(diff, item, 0.0, false, counter));
                    }
                    TileEvent.log(tilex, tiley, 0, performer.getWurmId(), 152);
                    done = true;
                    int templateId = Crops.getProductTemplate(crop);
                    float knowledge = (float)farming.getKnowledge(0.0);
                    float ql = knowledge + (100.0f - knowledge) * ((float)power / 500.0f);
                    float ageYieldFactor = 0.0f;
                    float ageQLFactor = 0.0f;
                    boolean ripe = false;
                    String failMessage = "You realize you harvested so early that nothing had a chance to grow here.";
                    String passMessage = "";
                    if (tileAge >= 3) {
                        if (tileAge < 4) {
                            ageQLFactor = 0.7f;
                            ageYieldFactor = 0.5f;
                            failMessage = "You realize you harvested much too early. There was nothing here to harvest.";
                            passMessage = "You realize you harvested much too early. Only sprouts grew here.";
                        } else if (tileAge < 5) {
                            ageQLFactor = 0.9f;
                            ageYieldFactor = 0.7f;
                            failMessage = "You realize you harvested too early. There was nothing here to harvest.";
                            passMessage = "You realize you harvested too early. The harvest is of low quality.";
                        } else if (tileAge < 7) {
                            ripe = true;
                            ageQLFactor = 1.0f;
                            ageYieldFactor = 1.0f;
                            failMessage = "You realize you harvested in perfect time, tending the field would have resulted in a better yield.";
                            passMessage = "You realize you harvested in perfect time. The harvest is of top quality.";
                        }
                    }
                    float realKnowledge = (float)farming.getKnowledge(0.0);
                    int worldResource = Server.getWorldResource(tilex, tiley);
                    int farmedCount = worldResource >>> 11;
                    int farmedChance = worldResource & 0x7FF;
                    short resource = (short)(farmedChance + act.getRarity() * 110 + itemRarity * 50 + Math.min(5, farmedCount) * 50);
                    float div = 100.0f - realKnowledge / 15.0f;
                    short bonusYield = (short)((float)resource / div / 1.5f);
                    float baseYield = realKnowledge / 15.0f;
                    int quantity = (int)((baseYield + (float)bonusYield) * ageYieldFactor);
                    Server.setWorldResource(tilex, tiley, 0);
                    Server.getInstance().broadCastAction(performer.getName() + " has harvested the field.", performer, 5);
                    ql *= ageQLFactor;
                    if (crop <= 3 && item != null && item.getSpellEffects() != null) {
                        float modifier = item.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                        ql *= modifier;
                    }
                    if (quantity == 0 && ripe) {
                        quantity = 1;
                    }
                    if (quantity == 1 && farmedCount > 0 && ripe) {
                        ++quantity;
                    }
                    if (quantity == 2 && farmedCount >= 4 && ripe) {
                        ++quantity;
                    }
                    if (quantity == 0 || ripe && quantity == 1) {
                        performer.getCommunicator().sendNormalServerMessage(failMessage);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage(passMessage);
                    }
                    String cropString = Crops.getCropName(crop);
                    performer.getCommunicator().sendNormalServerMessage("You managed to get a yield of " + quantity + " " + cropString + ".");
                    if (templateId == 144 && quantity >= 5) {
                        performer.achievement(544);
                    }
                    try {
                        for (int x = 0; x < quantity; ++x) {
                            Item result = ItemFactory.createItem(templateId, Math.max(Math.min(ql, 100.0f), 1.0f), null);
                            if (performer.getInventory().insertItem(result, true)) continue;
                            performer.getCommunicator().sendNormalServerMessage("You can't carry the harvest. It falls to the ground and is ruined!");
                        }
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, "No such template", nst);
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, "Failed to create harvest", fe);
                    }
                    if (onSurface) {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0);
                    } else {
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_DIRT.id, (byte)0));
                    }
                    performer.getMovementScheme().touchFreeMoveCounter();
                    Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("There is nothing here to harvest.");
                done = true;
            }
        }
        return done;
    }

    private static final boolean checkIfTerraformingOnPermaObject(int digTilex, int digTiley) {
        short hh = Tiles.decodeHeight(Server.surfaceMesh.getTile(digTilex, digTiley));
        if (hh < 1) {
            VolaTile t = Zones.getTileOrNull(digTilex, digTiley, true);
            if (t != null && t.hasOnePerTileItem(0)) {
                return true;
            }
            t = Zones.getTileOrNull(digTilex - 1, digTiley - 1, true);
            if (t != null && t.hasOnePerTileItem(0)) {
                return true;
            }
            t = Zones.getTileOrNull(digTilex, digTiley - 1, true);
            if (t != null && t.hasOnePerTileItem(0)) {
                return true;
            }
            t = Zones.getTileOrNull(digTilex - 1, digTiley, true);
            if (t != null && t.hasOnePerTileItem(0)) {
                return true;
            }
        }
        return false;
    }

    static boolean pickSprout(Creature performer, Item sickle, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter, Action act) {
        boolean toReturn;
        block26: {
            toReturn = true;
            byte tileType = Tiles.decodeType(tile);
            if (sickle.getTemplateId() == 267 && !theTile.isEnchanted()) {
                if (!performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put the sprout.");
                    return true;
                }
                byte data = Tiles.decodeData(tile);
                int age = FoliageAge.getAgeAsByte(data);
                if (age == 7 || age == 9 || age == 11 || age == 13) {
                    int weight;
                    Skill forestry = performer.getSkills().getSkillOrLearn(10048);
                    toReturn = false;
                    int time = Actions.getStandardActionTime(performer, forestry, sickle, 0.0);
                    if (counter == 1.0f) {
                        try {
                            weight = ItemTemplateFactory.getInstance().getTemplate(266).getWeightGrams();
                            if (!performer.canCarry(weight)) {
                                performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the sprout. You need to drop some things first.");
                                return true;
                            }
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                            return true;
                        }
                        if (theTile.isBush()) {
                            performer.getCommunicator().sendNormalServerMessage("You start cutting a sprout from the bush.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to cut a sprout off a bush.", performer, 5);
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You start cutting a sprout from the tree.");
                            Server.getInstance().broadCastAction(performer.getName() + " starts to cut a sprout off a tree.", performer, 5);
                        }
                        performer.sendActionControl(Actions.actionEntrys[187].getVerbString(), true, time);
                    }
                    if (counter * 10.0f >= (float)time) {
                        if (act.getRarity() != 0) {
                            performer.playPersonalSound("sound.fx.drumroll");
                        }
                        try {
                            weight = ItemTemplateFactory.getInstance().getTemplate(266).getWeightGrams();
                            if (!performer.canCarry(weight)) {
                                performer.getCommunicator().sendNormalServerMessage("You would not be able to carry the sprout. You need to drop some things first.");
                                return true;
                            }
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, nst.getLocalizedMessage(), nst);
                            return true;
                        }
                        sickle.setDamage(sickle.getDamage() + 0.003f * sickle.getDamageModifier());
                        double bonus = 0.0;
                        double power = 0.0;
                        Skill sickskill = performer.getSkills().getSkillOrLearn(10046);
                        bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                        power = forestry.skillCheck(1.0, sickle, bonus, false, counter);
                        toReturn = true;
                        try {
                            byte material = 0;
                            if (theTile.isBush()) {
                                BushData.BushType bushType = theTile.getBushType(data);
                                material = bushType.getMaterial();
                            } else {
                                TreeData.TreeType treeType = theTile.getTreeType(data);
                                material = treeType.getMaterial();
                            }
                            float modifier = 1.0f;
                            if (sickle.getSpellEffects() != null) {
                                modifier = sickle.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                            }
                            Item sprout = ItemFactory.createItem(266, Math.max(1.0f, Math.min(100.0f, (float)power * modifier + (float)sickle.getRarity())), material, act.getRarity(), null);
                            if (power < 0.0) {
                                sprout.setDamage((float)(-power) / 2.0f);
                            }
                            SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 2.0f);
                            performer.getInventory().insertItem(sprout);
                            byte newData = (byte)((--age << 4) + (data & 0xF) & 0xFF);
                            Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.decodeType(tile), newData);
                            Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                            if (theTile.isBush()) {
                                performer.getCommunicator().sendNormalServerMessage("You cut a sprout from the bush.");
                                Server.getInstance().broadCastAction(performer.getName() + " cuts a sprout off a bush.", performer, 5);
                                break block26;
                            }
                            performer.getCommunicator().sendNormalServerMessage("You cut a sprout from the tree.");
                            Server.getInstance().broadCastAction(performer.getName() + " cuts a sprout off a tree.", performer, 5);
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, "No template for sprout!", nst);
                            performer.getCommunicator().sendNormalServerMessage("You fail to pick the sprout. You realize something is wrong with the world.");
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, fe.getMessage(), fe);
                            performer.getCommunicator().sendNormalServerMessage("You fail to pick the sprout. You realize something is wrong with the world.");
                        }
                    }
                } else if (theTile.isBush()) {
                    performer.getCommunicator().sendNormalServerMessage("The bush has no sprout to pick.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The tree has no sprout to pick.");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot pick sprouts with that.");
                logger.log(Level.WARNING, performer.getName() + " tried to pick sprout with a " + sickle.getName());
            }
        }
        return toReturn;
    }

    public static final int getTreeHarvestingToolTemplate(TreeData.TreeType type) {
        if (type == TreeData.TreeType.MAPLE) {
            return 421;
        }
        return 267;
    }

    static boolean harvestTree(Action act, Creature performer, Item tool, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        byte data = Tiles.decodeData(tile);
        byte age = FoliageAge.getAgeAsByte(data);
        TreeData.TreeType treeType = theTile.getTreeType(data);
        if (tool.getTemplateId() == Terraforming.getTreeHarvestingToolTemplate(treeType)) {
            String treeName = treeType.getName();
            if (counter == 1.0f && !TileTreeBehaviour.hasFruit(performer, tilex, tiley, age)) {
                performer.getCommunicator().sendNormalServerMessage("There is nothing to harvest on the " + treeName + ".");
                return true;
            }
            int templateId = TileTreeBehaviour.getItem(tilex, tiley, (int)age, treeType);
            if (templateId == -10) {
                performer.getCommunicator().sendNormalServerMessage("There is nothing to harvest on the " + treeName + ".");
                return true;
            }
            toReturn = false;
            int time = 150;
            Skill skill = performer.getSkills().getSkillOrLearn(10048);
            Skill toolSkill = null;
            if (tool.getTemplateId() == 267) {
                toolSkill = performer.getSkills().getSkillOrLearn(10046);
            }
            if (counter == 1.0f) {
                if (!performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you harvest.");
                    return true;
                }
                if (tool.getTemplate().isContainerLiquid() && tool.getFreeVolume() <= 0) {
                    performer.getCommunicator().sendNormalServerMessage("The " + tool.getName() + " is already full!");
                    return true;
                }
                int maxSearches = Terraforming.calcMaxHarvest(tile, skill.getKnowledge(0.0), tool);
                time = Actions.getQuickActionTime(performer, skill, null, 0.0);
                act.setNextTick(time);
                act.setTickCount(1);
                act.setData(0L);
                float totalTime = time * maxSearches;
                try {
                    performer.getCurrentAction().setTimeLeft((int)totalTime);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                performer.getCommunicator().sendNormalServerMessage("You start to harvest the " + treeName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to harvest a tree.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[152].getVerbString(), true, (int)totalTime);
                performer.getStatus().modifyStamina(-500.0f);
            }
            if (tool != null && act.justTickedSecond()) {
                tool.setDamage(tool.getDamage() + 3.0E-4f * tool.getDamageModifier());
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                int maxSearches = Terraforming.calcMaxHarvest(tile, skill.getKnowledge(0.0), tool);
                act.incTickCount();
                act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
                int knowledge = (int)skill.getKnowledge(0.0);
                performer.getStatus().modifyStamina(-1500 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                act.setData(act.getData() + 1L);
                double bonus = 0.0;
                if (tool.getTemplateId() == 267) {
                    bonus = Math.max(1.0, toolSkill.skillCheck(1.0, tool, 0.0, false, counter / (float)searchCount));
                }
                double power = skill.skillCheck(skill.getKnowledge(0.0) - 5.0, tool, bonus, false, counter / (float)searchCount);
                try {
                    float modifier = 1.0f;
                    if (tool.getSpellEffects() != null) {
                        modifier = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                    }
                    float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                    ql = Math.min(100.0f, (ql + (float)tool.getRarity()) * modifier);
                    Item harvested = ItemFactory.createItem(templateId, Math.max(1.0f, ql), act.getRarity(), null);
                    if (ql < 0.0f) {
                        harvested.setDamage(-ql / 2.0f);
                    }
                    if (tool.getTemplateId() == 267) {
                        performer.getInventory().insertItem(harvested);
                        SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                    } else {
                        MethodsItems.fillContainer(act, tool, harvested, performer, false);
                        if (!harvested.deleted && harvested.getParentId() == -10L) {
                            performer.getCommunicator().sendNormalServerMessage("Not all the " + harvested.getName() + " would fit in the " + tool.getName() + ".");
                            Items.destroyItem(harvested.getWurmId());
                            toReturn = true;
                        }
                    }
                    if (searchCount == 1) {
                        TileTreeBehaviour.pick(tilex, tiley);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You harvest " + harvested.getNameWithGenus() + " from the " + treeName + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " harvests " + harvested.getName() + " from a tree.", performer, 5);
                    if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                        performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                        toReturn = true;
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for " + templateId, nst);
                    performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                    performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot harvest with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to harvest a tree with a " + tool.getName());
        }
        return toReturn;
    }

    static boolean harvestBush(Action act, Creature performer, Item tool, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        if (tool.getTemplateId() == 267) {
            byte data = Tiles.decodeData(tile);
            byte age = FoliageAge.getAgeAsByte(data);
            BushData.BushType bushType = theTile.getBushType(data);
            String treeName = bushType.getName();
            if (counter == 1.0f && !TileTreeBehaviour.hasFruit(performer, tilex, tiley, age)) {
                performer.getCommunicator().sendNormalServerMessage("There is nothing to harvest on the " + treeName + ".");
                return true;
            }
            int templateId = TileTreeBehaviour.getItem(tilex, tiley, (int)age, bushType);
            if (templateId == -10) {
                performer.getCommunicator().sendNormalServerMessage("There is nothing to harvest on the " + treeName + ".");
                return true;
            }
            toReturn = false;
            int time = 150;
            Skill skill = performer.getSkills().getSkillOrLearn(10048);
            Skill toolSkill = performer.getSkills().getSkillOrLearn(10046);
            if (counter == 1.0f) {
                if (!performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you harvest.");
                    return true;
                }
                int maxSearches = Terraforming.calcMaxHarvest(tile, skill.getKnowledge(0.0), tool);
                time = Actions.getQuickActionTime(performer, skill, null, 0.0);
                act.setNextTick(time);
                act.setTickCount(1);
                act.setData(0L);
                float totalTime = time * maxSearches;
                performer.getCommunicator().sendNormalServerMessage("You start to harvest the " + treeName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to harvest a bush.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[152].getVerbString(), true, (int)totalTime);
            }
            if (act.justTickedSecond()) {
                tool.setDamage(tool.getDamage() + 3.0E-4f * tool.getDamageModifier());
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                int maxSearches = Terraforming.calcMaxHarvest(tile, skill.getKnowledge(0.0), tool);
                act.incTickCount();
                act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
                int knowledge = (int)skill.getKnowledge(0.0);
                performer.getStatus().modifyStamina(-1500 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                act.setData(act.getData() + 1L);
                double bonus = Math.max(1.0, toolSkill.skillCheck(1.0, tool, 0.0, false, counter / (float)searchCount));
                double power = skill.skillCheck(skill.getKnowledge(0.0) - 5.0, tool, bonus, false, counter / (float)searchCount);
                try {
                    float modifier = 1.0f;
                    if (tool.getSpellEffects() != null) {
                        modifier = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                    }
                    float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                    ql = Math.min(100.0f, (ql + (float)tool.getRarity()) * modifier);
                    Item harvested = ItemFactory.createItem(templateId, Math.max(1.0f, ql), act.getRarity(), null);
                    if (ql < 0.0f) {
                        harvested.setDamage(-ql / 2.0f);
                    }
                    performer.getInventory().insertItem(harvested);
                    SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                    if (searchCount == 1) {
                        TileTreeBehaviour.pick(tilex, tiley);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You harvest " + harvested.getName() + " from the " + treeName + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " harvests " + harvested.getName() + " from a bush.", performer, 5);
                    if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                        performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                        toReturn = true;
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for " + templateId, nst);
                    performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                    performer.getCommunicator().sendNormalServerMessage("You fail to harvest. You realize something is wrong with the world.");
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot harvest with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to harvest a bush with a " + tool.getName());
        }
        return toReturn;
    }

    static boolean prune(Action action, Creature performer, Item sickle, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        if (sickle.getTemplateId() == 267) {
            if (theTile.isEnchanted()) {
                performer.getCommunicator().sendNormalServerMessage("It does not make sense to prune that.");
                return true;
            }
            byte data = Tiles.decodeData(tile);
            FoliageAge age = FoliageAge.getFoliageAge(data);
            String treeName = theTile.getTileName(data).toLowerCase();
            boolean ok = false;
            if (age.isPrunable() || age == FoliageAge.SHRIVELLED && theTile.isThorn(data)) {
                ok = true;
            }
            if (!ok) {
                performer.getCommunicator().sendNormalServerMessage("It does not make sense to prune now.");
                return true;
            }
            toReturn = false;
            int time = 150;
            Skill forestry = performer.getSkills().getSkillOrLearn(10048);
            Skill sickskill = performer.getSkills().getSkillOrLearn(10046);
            if (sickle.getTemplateId() == 267) {
                time = Actions.getStandardActionTime(performer, forestry, sickle, sickskill.getKnowledge(0.0));
            }
            if (counter == 1.0f) {
                performer.getCommunicator().sendNormalServerMessage("You start to prune the " + treeName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to prune the " + treeName + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[373].getVerbString(), true, time);
            }
            if (action.justTickedSecond()) {
                sickle.setDamage(sickle.getDamage() + 3.0E-4f * sickle.getDamageModifier());
            }
            if (counter * 10.0f >= (float)time) {
                double bonus = 0.0;
                double power = 0.0;
                bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                power = forestry.skillCheck(forestry.getKnowledge(0.0) - 10.0, sickle, bonus, false, counter);
                toReturn = true;
                SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                if (power < 0.0) {
                    performer.getCommunicator().sendNormalServerMessage("You make a lot of errors and need to take a break.");
                    return toReturn;
                }
                FoliageAge newage = age.getPrunedAge();
                int newData = newage.encodeAsData() + (data & 0xF) & 0xFF;
                Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.decodeType(tile), (byte)newData);
                TileEvent.log(tilex, tiley, 0, performer.getWurmId(), 373);
                Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                performer.getCommunicator().sendNormalServerMessage("You prune the " + treeName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " prunes the " + treeName + ".", performer, 5);
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot prune with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to prune with a " + sickle.getName());
        }
        return toReturn;
    }

    static boolean pickWurms(Action act, Creature performer, int tilex, int tiley, int tile, float counter) {
        boolean toReturn = true;
        if (counter == 1.0f && !Server.hasGrubs(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("There see no wurms casts here.");
            return true;
        }
        toReturn = false;
        int time = 150;
        Skill skill = performer.getSkills().getSkillOrLearn(10071);
        if (counter == 1.0f) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put any grubs.");
                return true;
            }
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            time = Actions.getQuickActionTime(performer, skill, null, 0.0);
            act.setNextTick(time);
            act.setTickCount(1);
            act.setData(0L);
            float totalTime = time * maxSearches;
            try {
                performer.getCurrentAction().setTimeLeft((int)totalTime);
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            performer.getCommunicator().sendNormalServerMessage("You start to search the dirt tile for wurms.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to search a dirt tile for wurms.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[935].getVerbString(), true, (int)totalTime);
            performer.getStatus().modifyStamina(-500.0f);
        }
        if (counter * 10.0f >= act.getNextTick()) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            int searchCount = act.getTickCount();
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            act.incTickCount();
            act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
            int knowledge = (int)skill.getKnowledge(0.0);
            performer.getStatus().modifyStamina(-1500 * searchCount);
            if (searchCount >= maxSearches) {
                toReturn = true;
            }
            act.setData(act.getData() + 1L);
            double bonus = 0.0;
            double diff = skill.getKnowledge(0.0) - 10.0 + (double)(searchCount * 5);
            double power = skill.skillCheck(diff, null, bonus, false, counter / (float)searchCount);
            try {
                float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                Item wurm = ItemFactory.createItem(1362, Math.max(1.0f, ql), act.getRarity(), null);
                if (ql < 0.0f) {
                    wurm.setDamage(-ql / 2.0f);
                }
                performer.getInventory().insertItem(wurm);
                SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                if (searchCount == 1) {
                    Server.setGrubs(tilex, tiley, false);
                }
                performer.getCommunicator().sendNormalServerMessage("You do a rain danec on the dirt tile and " + wurm.getNameWithGenus() + " pops to the surface, which you grab.");
                Server.getInstance().broadCastAction(performer.getName() + " danecs on a dirt tile and grabs " + wurm.getNameWithGenus() + " that pops to the surface.", performer, 5);
                if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                    return true;
                }
                if (ql < 0.0f && searchCount < maxSearches) {
                    performer.getCommunicator().sendNormalServerMessage("You make such a mess, you stop searching.");
                    return true;
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for 1364", nst);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any wurms. You realize something is wrong with the world.");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any wurms. You realize something is wrong with the world.");
            }
            if (searchCount < maxSearches) {
                act.setRarity(performer.getRarity());
            }
        }
        return toReturn;
    }

    static boolean pickGrubs(Action act, Creature performer, Item tool, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        byte data = Tiles.decodeData(tile);
        byte age = FoliageAge.getAgeAsByte(data);
        TreeData.TreeType treeType = theTile.getTreeType(data);
        if (tool.getTemplateId() == 390) {
            String treeName = treeType.getName();
            if (!(counter != 1.0f || age == 15 && Server.hasGrubs(tilex, tiley))) {
                performer.getCommunicator().sendNormalServerMessage("You find no grubs on the " + treeName + ".");
                return true;
            }
            toReturn = false;
            int time = 150;
            Skill skill = performer.getSkills().getSkillOrLearn(10048);
            if (counter == 1.0f) {
                if (!performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put any grubs.");
                    return true;
                }
                int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), tool);
                time = Actions.getQuickActionTime(performer, skill, null, 0.0);
                act.setNextTick(time);
                act.setTickCount(1);
                act.setData(0L);
                float totalTime = time * maxSearches;
                try {
                    performer.getCurrentAction().setTimeLeft((int)totalTime);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                performer.getCommunicator().sendNormalServerMessage("You start to search the " + treeName + " for grubs.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to search a tree for grubs.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[935].getVerbString(), true, (int)totalTime);
                performer.getStatus().modifyStamina(-500.0f);
            }
            if (tool != null && act.justTickedSecond()) {
                tool.setDamage(tool.getDamage() + 3.0E-4f * tool.getDamageModifier());
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), tool);
                act.incTickCount();
                act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
                int knowledge = (int)skill.getKnowledge(0.0);
                performer.getStatus().modifyStamina(-1500 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                act.setData(act.getData() + 1L);
                double bonus = 0.0;
                if (tool.getTemplateId() == 390) {
                    // empty if block
                }
                double diff = skill.getKnowledge(0.0) - 10.0 + (double)(searchCount * 5);
                double power = skill.skillCheck(diff, tool, bonus, false, counter / (float)searchCount);
                try {
                    float modifier = 1.0f;
                    if (tool.getSpellEffects() != null) {
                        modifier = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                    }
                    float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                    ql = Math.min(100.0f, (ql + (float)tool.getRarity()) * modifier);
                    Item grub = ItemFactory.createItem(1364, Math.max(1.0f, ql), act.getRarity(), null);
                    if (ql < 0.0f) {
                        grub.setDamage(-ql / 2.0f);
                    }
                    performer.getInventory().insertItem(grub);
                    SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                    if (searchCount == 1) {
                        Server.setGrubs(tilex, tiley, false);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You prise " + grub.getNameWithGenus() + " from the " + treeName + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " prises a " + grub.getName() + " from a tree.", performer, 5);
                    if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                        performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                        return true;
                    }
                    if (ql < 0.0f && searchCount < maxSearches) {
                        performer.getCommunicator().sendNormalServerMessage("You make such a mess, you stop searching.");
                        return true;
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, "No template for 1364", nst);
                    performer.getCommunicator().sendNormalServerMessage("You fail to find any grubs. You realize something is wrong with the world.");
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, fe.getMessage(), fe);
                    performer.getCommunicator().sendNormalServerMessage("You fail to find any grubs. You realize something is wrong with the world.");
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot prise with that.");
            logger.log(Level.WARNING, performer.getName() + " tried to prise grubs from a tree with a " + tool.getName());
        }
        return toReturn;
    }

    static boolean pickBark(Action act, Creature performer, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        byte data = Tiles.decodeData(tile);
        byte age = FoliageAge.getAgeAsByte(data);
        TreeData.TreeType treeType = theTile.getTreeType(data);
        String treeName = treeType.getName();
        if (!(counter != 1.0f || age == 14 && Server.hasGrubs(tilex, tiley))) {
            performer.getCommunicator().sendNormalServerMessage("There see no loose bark on the " + treeName + ".");
            return true;
        }
        toReturn = false;
        int time = 150;
        Skill skill = performer.getSkills().getSkillOrLearn(10048);
        if (counter == 1.0f) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put any bark.");
                return true;
            }
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            time = Actions.getQuickActionTime(performer, skill, null, 0.0);
            act.setNextTick(time);
            act.setTickCount(1);
            act.setData(0L);
            float totalTime = time * maxSearches;
            try {
                performer.getCurrentAction().setTimeLeft((int)totalTime);
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            performer.getCommunicator().sendNormalServerMessage("You start to search the " + treeName + " for loose bark.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to search a tree.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[935].getVerbString(), true, (int)totalTime);
            performer.getStatus().modifyStamina(-500.0f);
        }
        if (counter * 10.0f >= act.getNextTick()) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            int searchCount = act.getTickCount();
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            act.incTickCount();
            act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
            int knowledge = (int)skill.getKnowledge(0.0);
            performer.getStatus().modifyStamina(-1500 * searchCount);
            if (searchCount >= maxSearches) {
                toReturn = true;
            }
            act.setData(act.getData() + 1L);
            double bonus = 0.0;
            double diff = skill.getKnowledge(0.0) - 10.0 + (double)(searchCount * 5);
            double power = skill.skillCheck(diff, null, bonus, false, counter / (float)searchCount);
            try {
                float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                Item bark = ItemFactory.createItem(1355, Math.max(1.0f, ql), act.getRarity(), null);
                if (ql < 0.0f) {
                    bark.setDamage(-ql / 2.0f);
                }
                performer.getInventory().insertItem(bark);
                SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                if (searchCount == 1) {
                    Server.setGrubs(tilex, tiley, false);
                }
                performer.getCommunicator().sendNormalServerMessage("You remove " + bark.getNameWithGenus() + " from the " + treeName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " break a piece of " + bark.getName() + " from a tree.", performer, 5);
                if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                    return true;
                }
                if (ql < 0.0f && searchCount < maxSearches) {
                    performer.getCommunicator().sendNormalServerMessage("You make such a mess, you stop searching.");
                    return true;
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for 1364", nst);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any loose bark. You realize something is wrong with the world.");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any loose bark. You realize something is wrong with the world.");
            }
            if (searchCount < maxSearches) {
                act.setRarity(performer.getRarity());
            }
        }
        return toReturn;
    }

    static boolean findTwigs(Action act, Creature performer, int tilex, int tiley, int tile, Tiles.Tile theTile, float counter) {
        boolean toReturn = true;
        byte data = Tiles.decodeData(tile);
        byte age = FoliageAge.getAgeAsByte(data);
        BushData.BushType bushType = theTile.getBushType(data);
        String bushName = bushType.getName();
        if (!(counter != 1.0f || age == 14 && Server.hasGrubs(tilex, tiley))) {
            performer.getCommunicator().sendNormalServerMessage("There see no twigs under the " + bushName + ".");
            return true;
        }
        toReturn = false;
        int time = 150;
        Skill skill = performer.getSkills().getSkillOrLearn(10048);
        if (counter == 1.0f) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put any twigs.");
                return true;
            }
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            time = Actions.getQuickActionTime(performer, skill, null, 0.0);
            act.setNextTick(time);
            act.setTickCount(1);
            act.setData(0L);
            float totalTime = time * maxSearches;
            try {
                performer.getCurrentAction().setTimeLeft((int)totalTime);
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            performer.getCommunicator().sendNormalServerMessage("You start to search under the " + bushName + " for twigs.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to search under a bush.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[935].getVerbString(), true, (int)totalTime);
            performer.getStatus().modifyStamina(-500.0f);
        }
        if (counter * 10.0f >= act.getNextTick()) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            int searchCount = act.getTickCount();
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            act.incTickCount();
            act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
            int knowledge = (int)skill.getKnowledge(0.0);
            performer.getStatus().modifyStamina(-1500 * searchCount);
            if (searchCount >= maxSearches) {
                toReturn = true;
            }
            act.setData(act.getData() + 1L);
            double bonus = 0.0;
            double diff = skill.getKnowledge(0.0) - 10.0 + (double)(searchCount * 5);
            double power = skill.skillCheck(diff, null, bonus, false, counter / (float)searchCount);
            try {
                float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                ql = Math.min(100.0f, ql);
                Item twig = ItemFactory.createItem(1353, Math.max(1.0f, ql), act.getRarity(), null);
                twig.setMaterial(bushType.getMaterial());
                if (ql < 0.0f) {
                    twig.setDamage(-ql / 2.0f);
                }
                performer.getInventory().insertItem(twig);
                SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                if (searchCount == 1) {
                    Server.setGrubs(tilex, tiley, false);
                }
                performer.getCommunicator().sendNormalServerMessage("You find " + twig.getNameWithGenus() + " under the " + bushName + ".");
                Server.getInstance().broadCastAction(performer.getName() + " finds something under a bush.", performer, 5);
                if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                    return true;
                }
                if (ql < 0.0f && searchCount < maxSearches) {
                    performer.getCommunicator().sendNormalServerMessage("You make such a mess, you stop searching.");
                    return true;
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for 1364", nst);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any twigs. You realize something is wrong with the world.");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any twigs. You realize something is wrong with the world.");
            }
            if (searchCount < maxSearches) {
                act.setRarity(performer.getRarity());
            }
        }
        return toReturn;
    }

    static boolean findFeathers(Action act, Creature performer, int tilex, int tiley, int tile, float counter) {
        boolean toReturn = true;
        if (counter == 1.0f && !Server.hasGrubs(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("The area looks picked clean.");
            return true;
        }
        toReturn = false;
        int time = 150;
        Skill skill = performer.getSkills().getSkillOrLearn(10071);
        if (counter == 1.0f) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put any feathers.");
                return true;
            }
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            time = Actions.getQuickActionTime(performer, skill, null, 0.0);
            act.setNextTick(time);
            act.setTickCount(1);
            act.setData(0L);
            float totalTime = time * maxSearches;
            try {
                performer.getCurrentAction().setTimeLeft((int)totalTime);
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            performer.getCommunicator().sendNormalServerMessage("You start to search the tile for feathers.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to search a tile for feathers.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[935].getVerbString(), true, (int)totalTime);
            performer.getStatus().modifyStamina(-500.0f);
        }
        if (counter * 10.0f >= act.getNextTick()) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            int searchCount = act.getTickCount();
            int maxSearches = Terraforming.calcMaxGrubs(skill.getKnowledge(0.0), null);
            act.incTickCount();
            act.incNextTick(Actions.getQuickActionTime(performer, skill, null, 0.0));
            int knowledge = (int)skill.getKnowledge(0.0);
            performer.getStatus().modifyStamina(-1500 * searchCount);
            if (searchCount >= maxSearches) {
                toReturn = true;
            }
            act.setData(act.getData() + 1L);
            double bonus = 0.0;
            double diff = skill.getKnowledge(0.0) - 10.0 + (double)(searchCount * 5);
            double power = skill.skillCheck(diff, null, bonus, false, counter / (float)searchCount);
            try {
                float ql = (float)knowledge + (float)(100 - knowledge) * ((float)power / 500.0f);
                Item feather = ItemFactory.createItem(1352, Math.max(1.0f, ql), act.getRarity(), null);
                if (ql < 0.0f) {
                    feather.setDamage(-ql / 2.0f);
                }
                performer.getInventory().insertItem(feather);
                SoundPlayer.playSound("sound.forest.branchsnap", tilex, tiley, true, 3.0f);
                if (searchCount == 1) {
                    Server.setGrubs(tilex, tiley, false);
                }
                performer.getCommunicator().sendNormalServerMessage("You find " + feather.getNameWithGenus() + " on the tile.");
                Server.getInstance().broadCastAction(performer.getName() + " finds a " + feather.getName() + " on the tile.", performer, 5);
                if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                    performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                    return true;
                }
                if (ql < 0.0f && searchCount < maxSearches) {
                    performer.getCommunicator().sendNormalServerMessage("You make such a mess, you stop searching.");
                    return true;
                }
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, "No template for 1364", nst);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any feathers. You realize something is wrong with the world.");
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
                performer.getCommunicator().sendNormalServerMessage("You fail to find any feathers. You realize something is wrong with the world.");
            }
            if (searchCount < maxSearches) {
                act.setRarity(performer.getRarity());
            }
        }
        return toReturn;
    }

    static boolean pruneHedge(Action action, Creature performer, Item sickle, Fence hedge, boolean onSurface, float counter) {
        boolean insta;
        boolean toReturn = true;
        boolean bl = insta = sickle.getTemplateId() == 176 && performer.getPower() >= 2;
        if (sickle.getTemplateId() == 267 || insta) {
            if (!hedge.isHedge()) {
                performer.getCommunicator().sendNormalServerMessage("It does not make sense to prune that.");
                return true;
            }
            if (hedge.isLowHedge()) {
                performer.getCommunicator().sendNormalServerMessage("The hedge is too low to be pruned.");
                return true;
            }
            toReturn = false;
            int time = 1;
            Skill forestry = performer.getSkills().getSkillOrLearn(10048);
            Skill sickskill = performer.getSkills().getSkillOrLearn(10046);
            if (sickle.getTemplateId() == 267) {
                time = Actions.getStandardActionTime(performer, forestry, sickle, sickskill.getKnowledge(0.0));
            }
            if (counter == 1.0f && !insta) {
                performer.getCommunicator().sendNormalServerMessage("You start to prune the hedge.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to prune the hedge.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[373].getVerbString(), true, time);
            }
            if (action.justTickedSecond() && sickle.getTemplateId() == 267) {
                sickle.setDamage(sickle.getDamage() + 3.0E-4f * sickle.getDamageModifier());
            }
            if (counter * 10.0f >= (float)time) {
                double bonus = 0.0;
                double power = 0.0;
                bonus = Math.max(1.0, sickskill.skillCheck(1.0, sickle, 0.0, false, counter));
                power = forestry.skillCheck(forestry.getKnowledge(0.0) - 10.0, sickle, bonus, false, counter);
                toReturn = true;
                SoundPlayer.playSound("sound.forest.branchsnap", hedge.getTileX(), hedge.getTileY(), onSurface, 2.0f);
                if (power < 0.0) {
                    performer.getCommunicator().sendNormalServerMessage("You make a lot of errors and need to take a break.");
                    return toReturn;
                }
                hedge.setDamage(0.0f);
                hedge.setType(StructureConstantsEnum.getEnumByValue((byte)(hedge.getType().value - 1)));
                try {
                    hedge.save();
                    VolaTile tile = Zones.getTileOrNull(hedge.getTileX(), hedge.getTileY(), hedge.isOnSurface());
                    if (tile != null) {
                        tile.updateFence(hedge);
                    }
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                }
                TileEvent.log(hedge.getTileX(), hedge.getTileY(), 0, performer.getWurmId(), 373);
                performer.getCommunicator().sendNormalServerMessage("You prune the hedge.");
                Server.getInstance().broadCastAction(performer.getName() + " prunes the hedge.", performer, 5);
            }
        }
        return toReturn;
    }

    static boolean chopHedge(Action act, Creature performer, Item tool, Fence hedge, boolean onSurface, float counter) {
        boolean insta;
        boolean toReturn = true;
        boolean bl = insta = tool.getTemplateId() == 176 && performer.getPower() >= 2;
        if (!tool.isWeaponSlash() && tool.getTemplateId() != 24 && !insta) {
            return true;
        }
        if (!hedge.isHedge()) {
            performer.getCommunicator().sendNormalServerMessage("It does not make sense to chop that.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)96, hedge.getTileX(), hedge.getTileY())) {
            return true;
        }
        toReturn = false;
        int time = 1;
        try {
            Skill forestry = performer.getSkills().getSkillOrLearn(10048);
            Skill primskill = performer.getSkills().getSkillOrLearn(tool.getPrimarySkill());
            int hedgeAge = 4;
            if (hedge.isMediumHedge()) {
                hedgeAge = 9;
            }
            if (hedge.isHighHedge()) {
                hedgeAge = 14;
            }
            float qualityLevel = 0.0f;
            if (counter == 1.0f && !insta) {
                time = (int)((float)Terraforming.calcTime(hedgeAge, tool, primskill, forestry) * Actions.getStaminaModiferFor(performer, 20000));
                time = Math.min(65535, time);
                act.setTimeLeft(time);
                performer.getCommunicator().sendNormalServerMessage("You start to cut down the " + hedge.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to cut down the " + hedge.getName() + ".", performer, 5);
                act.setActionString("cutting down " + hedge.getName());
                performer.sendActionControl("cutting down " + hedge.getName(), true, time);
                performer.getStatus().modifyStamina(-1500.0f);
                if (tool.isWeaponAxe()) {
                    tool.setDamage(tool.getDamage() + 0.001f * tool.getDamageModifier());
                } else {
                    tool.setDamage(tool.getDamage() + 0.0025f * tool.getDamageModifier());
                }
            } else if (!insta) {
                time = act.getTimeLeft();
                if (act.justTickedSecond() && (time < 50 && act.currentSecond() % 2 == 0 || act.currentSecond() % 5 == 0)) {
                    performer.getStatus().modifyStamina(-6000.0f);
                    if (tool.isWeaponAxe()) {
                        tool.setDamage(tool.getDamage() + 0.001f * tool.getDamageModifier());
                    } else {
                        tool.setDamage(tool.getDamage() + 0.0025f * tool.getDamageModifier());
                    }
                }
                if (act.justTickedSecond() && counter * 10.0f < (float)(time - 30)) {
                    String sstring;
                    if (tool.getTemplateId() != 24) {
                        if ((act.currentSecond() - 2) % 3 == 0) {
                            sstring = "sound.work.woodcutting1";
                            int x = Server.rand.nextInt(3);
                            if (x == 0) {
                                sstring = "sound.work.woodcutting2";
                            } else if (x == 1) {
                                sstring = "sound.work.woodcutting3";
                            }
                            SoundPlayer.playSound(sstring, hedge.getTileX(), hedge.getTileY(), performer.isOnSurface(), 1.0f);
                        }
                    } else if ((act.currentSecond() - 2) % 6 == 0 && counter * 10.0f < (float)(time - 50)) {
                        sstring = "sound.work.carpentry.saw";
                        SoundPlayer.playSound("sound.work.carpentry.saw", hedge.getTileX(), hedge.getTileY(), performer.isOnSurface(), 1.0f);
                    }
                }
            }
            if (counter * 10.0f > (float)time || insta) {
                toReturn = true;
                double bonus = 0.0;
                double hedgeDifficulty = hedge.getDifficulty();
                float skillTick = 0.0f;
                skillTick = Servers.localServer.challengeServer ? Math.min(20.0f, counter) : Math.min(20.0f, counter);
                bonus = tool.getTemplateId() == 7 || tool.getTemplateId() == 24 ? Math.max(0.0, primskill.skillCheck(hedgeDifficulty, tool, 0.0, false, skillTick)) : Math.max(0.0, primskill.skillCheck(hedgeDifficulty, tool, 0.0, primskill.getKnowledge(0.0) > 20.0, skillTick));
                qualityLevel = Math.max(1.0f, (float)forestry.skillCheck(hedgeDifficulty, tool, bonus, false, skillTick));
                double imbueEnhancement = 1.0 + 0.23047 * (double)tool.getSkillSpellImprovement(1007) / 100.0;
                double woodc = forestry.getKnowledge(0.0) * imbueEnhancement;
                if (woodc < (double)qualityLevel) {
                    qualityLevel = (float)woodc;
                }
                if (qualityLevel == 1.0f && imbueEnhancement > 1.0) {
                    qualityLevel = (float)(1.0 + (double)(Server.rand.nextFloat() * 10.0f) * imbueEnhancement);
                }
                if (tool.getSpellEffects() != null) {
                    qualityLevel *= tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                }
                qualityLevel += (float)tool.getRarity();
                Skill strength = performer.getSkills().getSkillOrLearn(102);
                double damage = Weapon.getModifiedDamageForWeapon(tool, strength) * 2.0;
                if (tool.getTemplateId() == 7) {
                    damage = tool.getCurrentQualityLevel();
                    damage *= 1.0 + strength.getKnowledge(0.0) / 100.0;
                    damage *= (double)((50.0f + Server.rand.nextFloat() * 50.0f) / 100.0f);
                }
                damage = insta ? 100.0 : (damage += (double)((float)(15 - hedgeAge) / 15.0f * 100.0f));
                boolean destroyed = hedge.setDamage(hedge.getDamage() + (float)damage);
                if (destroyed) {
                    performer.getCommunicator().sendNormalServerMessage("You cut down the " + hedge.getName() + ".");
                    if (!insta) {
                        Server.getInstance().broadCastAction(performer.getName() + " cuts down the " + hedge.getName() + ".", performer, 5);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You chip away some wood from the " + hedge.getName() + ".");
                    if (!insta) {
                        Server.getInstance().broadCastAction(performer.getName() + " chips away some wood from the " + hedge.getName() + ".", performer, 5);
                    }
                }
            }
        }
        catch (NoSuchSkillException ex) {
            logger.log(Level.WARNING, ex.getMessage(), ex);
            toReturn = true;
        }
        return toReturn;
    }

    static void rampantGrowth(Creature performer, int tilex, int tiley) {
        logger.log(Level.INFO, performer.getName() + " creates trees and bushes at " + tilex + ", " + tiley);
        if (performer.getLogger() != null) {
            performer.getLogger().log(Level.INFO, "Creates trees and bushes at " + tilex + ", " + tiley);
        }
        for (int x = tilex - 5; x < tilex + 5; ++x) {
            for (int y = tiley - 5; y < tiley + 5; ++y) {
                int t = Server.surfaceMesh.getTile(x, y);
                if (Tiles.decodeHeight(t) <= 0 || Tiles.decodeType(t) != Tiles.Tile.TILE_DIRT.id && Tiles.decodeType(t) != Tiles.Tile.TILE_GRASS.id && Tiles.decodeType(t) != Tiles.Tile.TILE_SAND.id || Server.rand.nextInt(3) != 0) continue;
                int type = 0;
                if (Server.rand.nextBoolean()) {
                    type = Server.rand.nextInt(BushData.BushType.getLength());
                    newType = BushData.BushType.fromInt(type).asNormalBush();
                } else {
                    type = Server.rand.nextInt(TreeData.TreeType.getLength());
                    newType = TreeData.TreeType.fromInt(type).asNormalTree();
                }
                byte tage = (byte)Server.rand.nextInt(FoliageAge.OVERAGED.getAgeId());
                byte grasslen = (byte)(Server.rand.nextInt(3) + 1);
                Server.setSurfaceTile(x, y, Tiles.decodeHeight(t), newType, Tiles.encodeTreeData(tage, false, false, grasslen));
                Players.getInstance().sendChangedTile(x, y, true, false);
            }
        }
    }

    public static int[] getCaveOpeningCoords(int tilex, int tiley) {
        if (tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1) {
            if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_HOLE.id) {
                return new int[]{tilex, tiley};
            }
            if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex - 1, tiley)) == Tiles.Tile.TILE_HOLE.id) {
                return new int[]{tilex - 1, tiley};
            }
            if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex + 1, tiley)) == Tiles.Tile.TILE_HOLE.id) {
                return new int[]{tilex + 1, tiley};
            }
            if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley - 1)) == Tiles.Tile.TILE_HOLE.id) {
                return new int[]{tilex, tiley - 1};
            }
            if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley + 1)) == Tiles.Tile.TILE_HOLE.id) {
                return new int[]{tilex, tiley + 1};
            }
        }
        return noCaveDoor;
    }

    public static Set<int[]> getAllMineDoors(int tilex, int tiley) {
        if (tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1 && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex - 1, tiley)))) {
            HashSet<int[]> toReturn = new HashSet<int[]>();
            toReturn.add(new int[]{tilex - 1, tiley});
            return Terraforming.getEastMineDoor(tilex, tiley, toReturn);
        }
        return Terraforming.getEastMineDoor(tilex, tiley, null);
    }

    public static Set<int[]> getEastMineDoor(int tilex, int tiley, @Nullable Set<int[]> toReturn) {
        if (tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1 && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex + 1, tiley)))) {
            if (toReturn == null) {
                toReturn = new HashSet<int[]>();
            }
            toReturn.add(new int[]{tilex + 1, tiley});
        }
        return Terraforming.getNorthMineDoor(tilex, tiley, toReturn);
    }

    public static Set<int[]> getNorthMineDoor(int tilex, int tiley, @Nullable Set<int[]> toReturn) {
        if (tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1 && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley - 1)))) {
            if (toReturn == null) {
                toReturn = new HashSet<int[]>();
            }
            toReturn.add(new int[]{tilex, tiley - 1});
        }
        return Terraforming.getSouthMineDoor(tilex, tiley, toReturn);
    }

    public static Set<int[]> getSouthMineDoor(int tilex, int tiley, @Nullable Set<int[]> toReturn) {
        if (tilex > 0 && tilex < Zones.worldTileSizeX - 1 && tiley > 0 && tiley < Zones.worldTileSizeY - 1 && Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley + 1)))) {
            if (toReturn == null) {
                toReturn = new HashSet<int[]>();
            }
            toReturn.add(new int[]{tilex, tiley + 1});
        }
        return toReturn;
    }

    public static final float getDamageModifierForItem(Item item, byte tileid) {
        float mod = 0.0f;
        if (tileid == Tiles.Tile.TILE_MINE_DOOR_WOOD.id) {
            if (item.isWeaponAxe()) {
                mod = 0.03f;
            } else if (item.isWeaponCrush()) {
                mod = 0.02f;
            } else if (item.isWeaponSlash()) {
                mod = 0.015f;
            } else if (item.isWeaponPierce()) {
                mod = 0.01f;
            } else if (item.isWeaponMisc()) {
                mod = 0.007f;
            }
        } else if (tileid == Tiles.Tile.TILE_MINE_DOOR_STONE.id) {
            if (item.getTemplateId() == 20) {
                mod = 0.015f;
            } else if (item.isWeaponCrush()) {
                mod = 0.01f;
            } else if (item.isWeaponAxe()) {
                mod = 0.005f;
            } else if (item.isWeaponSlash()) {
                mod = 0.001f;
            } else if (item.isWeaponPierce()) {
                mod = 0.001f;
            } else if (item.isWeaponMisc()) {
                mod = 0.001f;
            }
        } else if (tileid == Tiles.Tile.TILE_MINE_DOOR_GOLD.id) {
            if (item.getTemplateId() == 20) {
                mod = 0.012f;
            } else if (item.isWeaponCrush()) {
                mod = 0.007f;
            } else if (item.isWeaponAxe()) {
                mod = 0.002f;
            } else if (item.isWeaponSlash()) {
                mod = 8.0E-4f;
            } else if (item.isWeaponPierce()) {
                mod = 8.0E-4f;
            } else if (item.isWeaponMisc()) {
                mod = 8.0E-4f;
            }
        } else if (tileid == Tiles.Tile.TILE_MINE_DOOR_SILVER.id) {
            if (item.getTemplateId() == 20) {
                mod = 0.012f;
            } else if (item.isWeaponCrush()) {
                mod = 0.007f;
            } else if (item.isWeaponAxe()) {
                mod = 0.002f;
            } else if (item.isWeaponSlash()) {
                mod = 8.0E-4f;
            } else if (item.isWeaponPierce()) {
                mod = 8.0E-4f;
            } else if (item.isWeaponMisc()) {
                mod = 8.0E-4f;
            }
        } else if (tileid == Tiles.Tile.TILE_MINE_DOOR_STEEL.id) {
            if (item.getTemplateId() == 20) {
                mod = 0.01f;
            } else if (item.isWeaponCrush()) {
                mod = 0.005f;
            } else if (item.isWeaponAxe()) {
                mod = 0.001f;
            } else if (item.isWeaponSlash()) {
                mod = 6.0E-4f;
            } else if (item.isWeaponPierce()) {
                mod = 6.0E-4f;
            } else if (item.isWeaponMisc()) {
                mod = 1.0E-4f;
            }
        }
        return mod;
    }

    public static final boolean removeMineDoor(Creature performer, Action act, Item destroyItem, int tilex, int tiley, boolean onSurface, float counter) {
        if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)))) {
            int time = 600;
            boolean toReturn = false;
            boolean insta = destroyItem.isWand();
            if (!onSurface) {
                performer.getCommunicator().sendNormalServerMessage("You need to do this from the outside.");
                return true;
            }
            if (counter == 1.0f) {
                if (!insta) {
                    Skills skills = performer.getSkills();
                    try {
                        Skill str = skills.getSkill(102);
                        if (!(str.getRealKnowledge() > 21.0)) {
                            performer.getCommunicator().sendNormalServerMessage("You are too weak to do that.");
                            return true;
                        }
                    }
                    catch (NoSuchSkillException nss) {
                        logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
                        performer.getCommunicator().sendNormalServerMessage("You are too weak to do that.");
                        return true;
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("You start to remove the door.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to remove a door.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[906].getVerbString(), true, time);
                act.setTimeLeft(time);
                performer.getStatus().modifyStamina(-1000.0f);
            } else {
                time = act.getTimeLeft();
            }
            if (act.currentSecond() % 5 == 0) {
                MethodsStructure.sendDestroySound(performer, destroyItem, Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == 25);
                performer.getStatus().modifyStamina(-5000.0f);
            }
            if (counter * 10.0f > (float)time || insta) {
                int currQl = Server.getWorldResource(tilex, tiley) / 100;
                try {
                    byte tile = Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley));
                    int doorType = Terraforming.getMineDoorTemplateForTile(tile);
                    Item removed = ItemFactory.createItem(doorType, Math.min(100.0f, Math.max(1.0f, (float)currQl)), (byte)0, null);
                    performer.getInventory().insertItem(removed);
                }
                catch (Exception ex) {
                    logger.log(Level.SEVERE, "Factory failed to produce minedoor for " + performer.getName(), ex);
                }
                TileEvent.log(tilex, tiley, 0, performer.getWurmId(), act.getNumber());
                TileEvent.log(tilex, tiley, -1, performer.getWurmId(), act.getNumber());
                if (Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                } else {
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                }
                Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                try {
                    MineDoorPermission md = MineDoorPermission.getPermission(tilex, tiley);
                    Zones.getZone(tilex, tiley, true).getOrCreateTile(tilex, tiley).removeMineDoor(md);
                }
                catch (NoSuchZoneException e) {
                    logger.log(Level.WARNING, "Zone for mine door removal not found");
                }
                MineDoorPermission.deleteMineDoor(tilex, tiley);
                performer.getCommunicator().sendNormalServerMessage("You remove the mine door from the opening.");
                return true;
            }
            return false;
        }
        return true;
    }

    public static final boolean destroyMineDoor(Creature performer, Action act, Item destroyItem, int tilex, int tiley, boolean onSurface, float counter) {
        if (Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)))) {
            Skills skills;
            boolean toReturn = true;
            int time = 300;
            if (Servers.localServer.isChallengeServer()) {
                time = 100;
            }
            float mod = 1.0f;
            mod = destroyItem == null && !(performer instanceof Player) ? 0.003f : (destroyItem != null ? Terraforming.getDamageModifierForItem(destroyItem, Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley))) : 0.0f);
            boolean insta = destroyItem.isWand();
            if (!performer.isWithinDistanceTo(tilex, tiley, 1)) {
                performer.getCommunicator().sendNormalServerMessage("You are too far away from the mine door.");
                return true;
            }
            if (!onSurface) {
                performer.getCommunicator().sendNormalServerMessage("You need to do this from the outside.");
                return true;
            }
            if (mod <= 0.0f && !insta) {
                performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the door with that.");
                return true;
            }
            toReturn = false;
            if (counter == 1.0f) {
                if (!insta) {
                    skills = performer.getSkills();
                    try {
                        Skill str = skills.getSkill(102);
                        if (!(str.getRealKnowledge() > 21.0)) {
                            performer.getCommunicator().sendNormalServerMessage("You are too weak to do that.");
                            return true;
                        }
                    }
                    catch (NoSuchSkillException nss) {
                        logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
                        performer.getCommunicator().sendNormalServerMessage("You are too weak to do that.");
                        return true;
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("You start to destroy the door.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to destroy a door.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[82].getVerbString(), true, time);
                act.setTimeLeft(time);
                performer.getStatus().modifyStamina(-1000.0f);
            } else {
                time = act.getTimeLeft();
            }
            if (act.currentSecond() % 5 == 0) {
                MethodsStructure.sendDestroySound(performer, destroyItem, Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == 25);
                performer.getStatus().modifyStamina(-5000.0f);
                if (destroyItem != null && !destroyItem.isBodyPartAttached()) {
                    destroyItem.setDamage(destroyItem.getDamage() + mod * destroyItem.getDamageModifier());
                }
            }
            if (counter * 10.0f > (float)time || insta) {
                skills = performer.getSkills();
                Skill destroySkill = null;
                try {
                    destroySkill = skills.getSkill(102);
                }
                catch (NoSuchSkillException nss) {
                    destroySkill = skills.learn(102, 1.0f);
                }
                destroySkill.skillCheck(20.0, destroyItem, 0.0, false, counter);
                toReturn = true;
                double damage = 1.0;
                int currQl = Server.getWorldResource(tilex, tiley);
                if (insta && mod == 0.0f) {
                    damage = 20.0;
                } else if (destroyItem != null) {
                    boolean iswood = Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_MINE_DOOR_WOOD.id;
                    damage = iswood && destroyItem.isCarpentryTool() ? 100.0 * (1.0 + destroySkill.getKnowledge(0.0) / 100.0) : Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 2.0;
                    damage /= (double)((float)currQl / 20.0f);
                    VolaTile t = Zones.getOrCreateTile(tilex, tiley, true);
                    Village vill = t.getVillage();
                    if (vill != null) {
                        if (MethodsStructure.isCitizenAndMayPerformAction((short)82, performer, vill)) {
                            damage *= 50.0;
                        } else if (MethodsStructure.isAllyAndMayPerformAction((short)82, performer, vill)) {
                            damage *= 25.0;
                        } else if (!vill.isChained()) {
                            damage *= 3.0;
                        }
                    } else {
                        damage *= 5.0;
                    }
                    damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
                    if (performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                        damage *= 2.0;
                    }
                    damage = (float)(damage * (double)mod * 100.0);
                }
                damage *= 100.0;
                if (Servers.localServer.isChallengeServer()) {
                    damage *= 2.5;
                }
                currQl = (int)Math.max(0.0, (double)currQl - damage);
                Server.setWorldResource(tilex, tiley, currQl);
                if (currQl == 0) {
                    TileEvent.log(tilex, tiley, 0, performer.getWurmId(), act.getNumber());
                    TileEvent.log(tilex, tiley, -1, performer.getWurmId(), act.getNumber());
                    if (Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_CAVE_EXIT.id) {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_HOLE.id, (byte)0);
                    } else {
                        Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Tiles.Tile.TILE_ROCK.id, (byte)0);
                    }
                    Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                    try {
                        MineDoorPermission md = MineDoorPermission.getPermission(tilex, tiley);
                        Zones.getZone(tilex, tiley, true).getOrCreateTile(tilex, tiley).removeMineDoor(md);
                    }
                    catch (NoSuchZoneException e) {
                        logger.log(Level.WARNING, "Zone for mine door removal not found");
                    }
                    MineDoorPermission.deleteMineDoor(tilex, tiley);
                    performer.getCommunicator().sendNormalServerMessage("The last parts of the door fall down with a crash.");
                    Server.getInstance().broadCastAction(performer.getName() + " damages a door and the last parts fall down with a crash.", performer, 5);
                    if (performer.getDeity() != null) {
                        performer.performActionOkey(act);
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You damage the door.");
                    Server.getInstance().broadCastAction(performer.getName() + " damages the door.", performer, 5);
                }
            }
            return toReturn;
        }
        return true;
    }

    public static final boolean isAltarBlocking(Creature performer, int tilex, int tiley) {
        int maxwest;
        int maxeast;
        int maxsouth;
        int maxnorth;
        EndGameItem alt = EndGameItems.getEvilAltar();
        if (alt != null) {
            maxnorth = Zones.safeTileY(tiley - 20);
            maxsouth = Zones.safeTileY(tiley + 20);
            maxeast = Zones.safeTileX(tilex - 20);
            maxwest = Zones.safeTileX(tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                return true;
            }
        }
        if ((alt = EndGameItems.getGoodAltar()) != null) {
            maxnorth = Zones.safeTileY(tiley - 20);
            maxsouth = Zones.safeTileY(tiley + 20);
            maxeast = Zones.safeTileX(tilex - 20);
            maxwest = Zones.safeTileX(tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                return true;
            }
        }
        return false;
    }

    public static final boolean buildMineDoor(Creature performer, Item source, Action act, int tilex, int tiley, boolean onSurface, float counter) {
        boolean done = true;
        if (Tiles.decodeType(Server.surfaceMesh.getTile(tilex, tiley)) == Tiles.Tile.TILE_HOLE.id) {
            if (!performer.isOnSurface()) {
                performer.getCommunicator().sendNormalServerMessage("You need to do this from the outside.");
                return true;
            }
            if (performer.getPower() < 5) {
                if (Zones.isInPvPZone(tilex, tiley)) {
                    performer.getCommunicator().sendNormalServerMessage("You are not allowed to build this in the PvP zone.");
                    return true;
                }
                if (Terraforming.getCaveDoorDifference(Server.surfaceMesh.getTile(tilex, tiley), tilex, tiley) > 90) {
                    performer.getCommunicator().sendNormalServerMessage("That hole is too big to be covered.");
                    return true;
                }
                if (Terraforming.isTileModBlocked(performer, tilex, tiley, true)) {
                    return true;
                }
                if (Terraforming.isAltarBlocking(performer, tilex, tiley)) {
                    performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.");
                    return true;
                }
                if (!Methods.isActionAllowed(performer, (short)363)) {
                    return true;
                }
            }
            if (source.isMineDoor() || source.isWand()) {
                Skills skills;
                boolean insta;
                done = false;
                boolean bl = insta = performer.getPower() > 0;
                if (counter == 1.0f && !insta) {
                    skills = performer.getSkills();
                    try {
                        Skill str = skills.getSkill(1008);
                        if (source.getTemplateId() != 592 && !(str.getRealKnowledge() > 21.0)) {
                            performer.getCommunicator().sendNormalServerMessage("You do not know how to do that effectively.");
                            return true;
                        }
                    }
                    catch (NoSuchSkillException nss) {
                        performer.getCommunicator().sendNormalServerMessage("You do not know how to do that effectively.");
                        return true;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You start to fit the door in the entrance.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to fit a door in the entrance.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[363].getVerbString(), true, 150);
                    performer.getStatus().modifyStamina(-1000.0f);
                }
                if (act.currentSecond() % 5 == 0) {
                    performer.getStatus().modifyStamina(-5000.0f);
                }
                if (act.mayPlaySound()) {
                    String s;
                    String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
                    if (source.isStone()) {
                        s = "sound.work.masonry";
                    }
                    if (source.isMetal()) {
                        s = "sound.work.smithing.metal";
                    }
                    SoundPlayer.playSound(s, performer, 1.0f);
                }
                if (counter > 15.0f || insta) {
                    skills = performer.getSkills();
                    Skill mining = null;
                    try {
                        mining = skills.getSkill(1008);
                        mining.skillCheck(20.0, source.getQualityLevel(), false, 15.0f);
                    }
                    catch (NoSuchSkillException noSuchSkillException) {
                        // empty catch block
                    }
                    if (!Terraforming.flattenTopMineBorder(performer, tilex, tiley)) {
                        performer.getCommunicator().sendNormalServerMessage("Mine door cannot be placed as the upper part of the entrance is not flat.");
                        return true;
                    }
                    if (MineDoorPermission.getPermission(tilex, tiley) != null) {
                        MineDoorPermission.deleteMineDoor(tilex, tiley);
                    }
                    Village vill = Villages.getVillage(tilex, tiley, onSurface);
                    Server.setWorldResource(tilex, tiley, Math.max(1, (int)source.getCurrentQualityLevel() * 100));
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex, tiley)), Terraforming.getNewTileTypeForMineDoor(source.getTemplateId()), (byte)0);
                    TileEvent.log(tilex, tiley, 0, performer.getWurmId(), act.getNumber());
                    TileEvent.log(tilex, tiley, -1, performer.getWurmId(), act.getNumber());
                    Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                    new MineDoorPermission(tilex, tiley, performer.getWurmId(), vill, false, false, "", 0);
                    if (source.isMineDoor()) {
                        Items.destroyItem(source.getWurmId());
                    }
                    return true;
                }
            }
        }
        return done;
    }

    private static final boolean flattenTopMineBorder(Creature performer, int tilex, int tiley) {
        Point highestCorner = TileRockBehaviour.findHighestCorner(tilex, tiley);
        if (highestCorner == null) {
            return false;
        }
        Point nextHighestCorner = TileRockBehaviour.findNextHighestCorner(tilex, tiley, highestCorner);
        if (nextHighestCorner == null) {
            return false;
        }
        if (nextHighestCorner.getH() != highestCorner.getH() && TileRockBehaviour.isStructureNear(highestCorner.getX(), highestCorner.getY())) {
            return false;
        }
        short targetUpperHeight = (short)nextHighestCorner.getH();
        short tileData = Tiles.decodeTileData(Server.surfaceMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Server.surfaceMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
        tileData = Tiles.decodeTileData(Server.rockMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Server.rockMesh.setTile(highestCorner.getX(), highestCorner.getY(), Tiles.encode(targetUpperHeight, tileData));
        tileData = Tiles.decodeTileData(Server.caveMesh.getTile(highestCorner.getX(), highestCorner.getY()));
        Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), true, true);
        Players.getInstance().sendChangedTile(highestCorner.getX(), highestCorner.getY(), false, true);
        Players.getInstance().sendChangedTile(tilex, tiley, true, true);
        return true;
    }

    public static final int getMineDoorTemplateForTile(byte tile) {
        if (tile == 27) {
            return 594;
        }
        if (tile == 25) {
            return 592;
        }
        if (tile == 26) {
            return 593;
        }
        if (tile == 28) {
            return 595;
        }
        if (tile == 29) {
            return 596;
        }
        return -1;
    }

    public static final byte getNewTileTypeForMineDoor(int templateId) {
        if (templateId == 594) {
            return 27;
        }
        if (templateId == 592) {
            return 25;
        }
        if (templateId == 593) {
            return 26;
        }
        if (templateId == 595) {
            return 28;
        }
        if (templateId == 596) {
            return 29;
        }
        if (templateId == 315) {
            return 25;
        }
        if (templateId == 176) {
            return 25;
        }
        return 0;
    }

    public static final boolean enchantNature(Creature performer, int tilex, int tiley, boolean onSurface, int tile, float counter, Action act) {
        boolean done = true;
        if (Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().mayEnchantNature()) {
            oldType = Tiles.decodeType(tile);
            Tiles.Tile oldTile = Tiles.getTile(oldType);
            newType = Tiles.Tile.TILE_ENCHANTED_GRASS.id;
            byte oldData = Tiles.decodeData(tile);
            if (oldType == Tiles.Tile.TILE_KELP.id || oldType == Tiles.Tile.TILE_REED.id || oldType == Tiles.Tile.TILE_LAWN.id || oldType == Tiles.Tile.TILE_BUSH_LINGONBERRY.id) {
                performer.getCommunicator().sendNormalServerMessage("The area refuses to accept your love.");
                return true;
            }
            if (oldType == Tiles.Tile.TILE_GRASS.id || oldTile.isNormalTree() || oldTile.isNormalBush()) {
                done = false;
                BlockingResult result = Blocking.getBlockerBetween(performer, performer.getPosX(), performer.getPosY(), (tilex << 2) + 2, (tiley << 2) + 2, performer.getPositionZ(), Zones.getHeightForNode(tilex, tiley, 0), true, true, false, 5, -1L, performer.getBridgeId(), -10L, false);
                if (result != null) {
                    performer.getCommunicator().sendCombatNormalMessage("The " + result.getFirstBlocker().getName() + " is in the way. You fail to focus.");
                    return true;
                }
                if (counter == 1.0f) {
                    performer.getCommunicator().sendNormalServerMessage("You start to focus your love on the surroundings.");
                    Server.getInstance().broadCastAction(performer.getName() + " smiles and closes " + performer.getHisHerItsString() + " eyes.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[388].getVerbString(), true, 50);
                    performer.getStatus().modifyStamina(-1000.0f);
                } else if (act.currentSecond() >= 5) {
                    TileEvent.log(tilex, tilex, 0, performer.getWurmId(), act.getNumber());
                    if (performer.getCultist() != null) {
                        performer.getCultist().touchCooldown2();
                    }
                    if (oldTile.isNormalTree()) {
                        newType = oldTile.getTreeType(oldData).asEnchantedTree();
                    } else if (oldTile.isNormalBush()) {
                        newType = oldTile.getBushType(oldData).asEnchantedBush();
                    }
                    byte newData = oldData;
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), newType, newData);
                    performer.getCommunicator().sendNormalServerMessage("You let your love change the area.");
                    Server.getInstance().broadCastAction(performer.getName() + " changes the area with " + performer.getHisHerItsString() + " love.", performer, 5);
                    done = true;
                    performer.getMovementScheme().touchFreeMoveCounter();
                    Players.getInstance().sendChangedTile(tilex, tiley, onSurface, false);
                    return done;
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to enchant the spot.");
        }
        return done;
    }

    public static final boolean freezeLava(Creature performer, int tilex, int tiley, boolean onSurface, int tile, float counter, boolean cultistSpawn) {
        VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
        byte type = Tiles.decodeType(tile);
        if (type != Tiles.Tile.TILE_LAVA.id && type != Tiles.Tile.TILE_CAVE_WALL_LAVA.id) {
            performer.getCommunicator().sendNormalServerMessage("The tile is not lava any longer.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)384)) {
            return true;
        }
        if (vt.getVillage() != null && !vt.getVillage().isCitizen(performer) && vt.getKingdom() == performer.getKingdomId() && !vt.getVillage().isAlly(performer)) {
            performer.getCommunicator().sendNormalServerMessage("Some psychological issue stops you from freezing the lava here. If you were an ally of the village maybe you would feel more comfortable.");
            return true;
        }
        boolean done = false;
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start concentrating on the lava.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to stare intensely at the lava.", performer, 5);
            if (cultistSpawn) {
                performer.sendActionControl("Freezing", true, 1000);
            }
        }
        if (!cultistSpawn || counter > 100.0f) {
            done = true;
            if ((Tiles.decodeData(tile) & 0xFF) == 255) {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens with the lava.. the permanent flow from beneath is too powerful.");
            } else {
                performer.getCommunicator().sendNormalServerMessage("The lava cools down and turns grey.");
                Server.getInstance().broadCastAction("The previously hot lava is now still and grey rock instead.", performer, 5);
                TileEvent.log(tilex, tiley, 0, performer.getWurmId(), 327);
                if (cultistSpawn) {
                    performer.getCultist().touchCooldown2();
                }
                if (type == Tiles.Tile.TILE_LAVA.id) {
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.Tile.TILE_ROCK.id, (byte)0);
                    for (int xx = 0; xx <= 1; ++xx) {
                        for (int yy = 0; yy <= 1; ++yy) {
                            try {
                                short tempint3 = Tiles.decodeHeight(Server.surfaceMesh.getTile(tilex + xx, tiley + yy));
                                Server.rockMesh.setTile(tilex + xx, tiley + yy, Tiles.encode(tempint3, Tiles.Tile.TILE_ROCK.id, (byte)0));
                                continue;
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                    }
                    int caveTile = Server.caveMesh.getTile(tilex, tiley);
                    byte caveType = Tiles.decodeType(caveTile);
                    if (caveType == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) {
                        Terraforming.setAsRock(tilex, tiley, false, false);
                    }
                    Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                } else {
                    Terraforming.setAsRock(tilex, tiley, false, false);
                }
            }
        }
        return done;
    }

    public static boolean handleChopAction(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter) {
        boolean done;
        block63: {
            done = false;
            if (!source.isWeaponSlash() && source.getTemplateId() != 24) {
                return true;
            }
            byte tileType = Tiles.decodeType(tile);
            Tiles.Tile theTile = Tiles.getTile(tileType);
            byte tileData = Tiles.decodeData(tile);
            int treeAge = tileData >> 4 & 0xF;
            if (!theTile.isBush() && !theTile.isTree()) {
                return true;
            }
            if (Zones.protectedTiles[tilex][tiley]) {
                performer.getCommunicator().sendNormalServerMessage("Your muscles weaken as you try to cut down the tree. You just can't bring yourself to do it.");
                return true;
            }
            if (!Methods.isActionAllowed(performer, (short)96, false, tilex, tiley, tile, 0)) {
                return true;
            }
            VolaTile vt = Zones.getTileOrNull(tilex, tiley, onSurface);
            Item hive = null;
            if (vt != null && (hive = vt.findHive(1239, true)) != null && performer.getBestBeeSmoker() == null) {
                performer.getCommunicator().sendSafeServerMessage("The bees get angry and defend the wild hive by stinging you.");
                performer.addWoundOfType(null, (byte)5, 2, true, 1.0f, false, 5000.0f + Server.rand.nextFloat() * 7000.0f, 0.0f, 35.0f, false, false);
                return true;
            }
            if (!onSurface) {
                performer.getCommunicator().sendNormalServerMessage("You can not reach that.");
                return true;
            }
            try {
                Skill woodcutting = performer.getSkills().getSkillOrLearn(1007);
                Skill primskill = performer.getSkills().getSkillOrLearn(source.getPrimarySkill());
                int time = 0;
                float qualityLevel = 0.0f;
                if (counter == 1.0f) {
                    time = (int)((float)Terraforming.calcTime(treeAge, source, primskill, woodcutting) * Actions.getStaminaModiferFor(performer, 20000));
                    time = Math.min(65535, time);
                    act.setTimeLeft(time);
                    String treeString = theTile.getTileName(tileData);
                    performer.getCommunicator().sendNormalServerMessage("You start to cut down the " + treeString + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to cut down the " + treeString + ".", performer, 5);
                    act.setActionString("cutting down " + treeString);
                    performer.sendActionControl("cutting down " + treeString, true, time);
                    performer.getStatus().modifyStamina(-1500.0f);
                    if (source.isWeaponAxe()) {
                        source.setDamage(source.getDamage() + 0.001f * source.getDamageModifier());
                    } else {
                        source.setDamage(source.getDamage() + 0.0025f * source.getDamageModifier());
                    }
                } else {
                    time = act.getTimeLeft();
                    if (act.justTickedSecond() && (time < 50 && act.currentSecond() % 2 == 0 || act.currentSecond() % 5 == 0)) {
                        performer.getStatus().modifyStamina(-6000.0f);
                        if (source.isWeaponAxe()) {
                            source.setDamage(source.getDamage() + 0.001f * source.getDamageModifier());
                        } else {
                            source.setDamage(source.getDamage() + 0.0025f * source.getDamageModifier());
                        }
                    }
                    if (act.justTickedSecond() && counter * 10.0f < (float)(time - 30)) {
                        String sstring;
                        if (source.getTemplateId() != 24) {
                            if ((act.currentSecond() - 2) % 3 == 0) {
                                sstring = "sound.work.woodcutting1";
                                int x = Server.rand.nextInt(3);
                                if (x == 0) {
                                    sstring = "sound.work.woodcutting2";
                                } else if (x == 1) {
                                    sstring = "sound.work.woodcutting3";
                                }
                                SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0f);
                            }
                        } else if ((act.currentSecond() - 2) % 6 == 0 && counter * 10.0f < (float)(time - 50)) {
                            sstring = "sound.work.carpentry.saw";
                            SoundPlayer.playSound("sound.work.carpentry.saw", tilex, tiley, performer.isOnSurface(), 1.0f);
                        }
                    }
                }
                if (!(counter * 10.0f > (float)time)) break block63;
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                done = true;
                double bonus = 0.0;
                int stumpAge = treeAge;
                if (treeAge == 15) {
                    treeAge = 3;
                }
                double treeDifficulty = 2.0;
                float skillTick = 0.0f;
                if (Servers.localServer.challengeServer) {
                    skillTick = Math.min(20.0f, counter);
                    int base = theTile.getWoodDificulity();
                    treeDifficulty = (double)base * (1.0 + (double)treeAge / 14.0);
                } else {
                    skillTick = Math.min(20.0f, counter);
                    treeDifficulty = 15 - treeAge;
                }
                bonus = source.getTemplateId() == 7 || source.getTemplateId() == 24 ? Math.max(0.0, primskill.skillCheck(treeDifficulty, source, 0.0, false, skillTick)) : Math.max(0.0, primskill.skillCheck(treeDifficulty, source, 0.0, primskill.getKnowledge(0.0) > 20.0, skillTick));
                qualityLevel = Math.max(1.0f, (float)woodcutting.skillCheck(treeDifficulty, source, bonus, false, skillTick));
                double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1007) / 100.0;
                double woodc = woodcutting.getKnowledge(0.0) * imbueEnhancement;
                if (woodc < (double)qualityLevel) {
                    qualityLevel = (float)woodc;
                }
                if (qualityLevel == 1.0f && imbueEnhancement > 1.0) {
                    qualityLevel = (float)(1.0 + (double)(Server.rand.nextFloat() * 10.0f) * imbueEnhancement);
                }
                if (source.getSpellEffects() != null) {
                    qualityLevel *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                }
                qualityLevel += (float)source.getRarity();
                Skill strength = performer.getSkills().getSkillOrLearn(102);
                double damage = Weapon.getModifiedDamageForWeapon(source, strength) * 2.0;
                if (source.getTemplateId() == 7) {
                    damage = source.getCurrentQualityLevel();
                    damage *= 1.0 + strength.getKnowledge(0.0) / 100.0;
                    damage *= (double)((50.0f + Server.rand.nextFloat() * 50.0f) / 100.0f);
                }
                damage += (double)((float)(15 - treeAge) / 15.0f * 100.0f);
                float dam = Server.getWorldResource(tilex, tiley);
                if (dam == 65535.0f) {
                    dam = 0.0f;
                }
                if (dam < 100.0f) {
                    dam = (float)((double)dam + damage);
                }
                String treeString = theTile.getTileName(tileData);
                if (dam >= 100.0f) {
                    Server.setWorldResource(tilex, tiley, 0);
                    TileEvent.log(tilex, tiley, 0, performer.getWurmId(), action);
                    performer.getCommunicator().sendNormalServerMessage("You cut down the " + treeString + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " cuts down the " + treeString + ".", performer, 5);
                    MissionTriggers.activateTriggers(performer, source, 492, EpicServerStatus.getTileId(tilex, tiley), 1);
                    if (treeAge > 4) {
                        performer.achievement(129);
                        if (source.getTemplateId() == 25) {
                            performer.achievement(135);
                        }
                    }
                    GrassData.GrowthTreeStage treeStage = TreeData.getGrassLength(tileData);
                    int newGrassLength = Math.max(0, treeStage.getCode() - 1);
                    GrassData.GrowthStage grassStage = GrassData.GrowthStage.fromInt(newGrassLength);
                    GrassData.FlowerType flowerType = GrassData.FlowerType.NONE;
                    byte newData = GrassData.encodeGrassTileData(grassStage, flowerType);
                    byte newt = Tiles.Tile.TILE_GRASS.id;
                    if (Terraforming.allCornersAtRockLevel(tilex, tiley, Server.surfaceMesh)) {
                        newt = Tiles.Tile.TILE_ROCK.id;
                        newData = 0;
                    } else if (theTile.isMycelium()) {
                        newt = Tiles.Tile.TILE_MYCELIUM.id;
                    } else if (theTile.isEnchanted()) {
                        newt = Tiles.Tile.TILE_ENCHANTED_GRASS.id;
                        newData = 0;
                    } else if (tileType == Tiles.Tile.TILE_BUSH_LINGONBERRY.id) {
                        newt = Tiles.Tile.TILE_TUNDRA.id;
                    }
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), newt, newData);
                    try {
                        Zone z = Zones.getZone(tilex, tiley, true);
                        z.changeTile(tilex, tiley);
                    }
                    catch (NoSuchZoneException z) {
                        // empty catch block
                    }
                    Players.getInstance().sendChangedTile(tilex, tiley, onSurface, true);
                    if (theTile.isTree()) {
                        TreeData.TreeType treeType = theTile.getTreeType(tileData);
                        boolean inCenter = TreeData.isCentre(tileData);
                        try {
                            ItemTemplate t;
                            int weight;
                            byte material = treeType.getMaterial();
                            int templateId = 9;
                            if (treeAge >= 8 && !treeType.isFruitTree()) {
                                templateId = 385;
                            }
                            double sizeMod = (double)treeAge / 15.0;
                            if (treeType.isFruitTree()) {
                                sizeMod *= 0.25;
                            }
                            float dir = Creature.normalizeAngle(TerrainUtilities.getTreeRotation(tilex, tiley));
                            float xNew = (float)(tilex << 2) + 2.0f;
                            float yNew = (float)(tiley << 2) + 2.0f;
                            if (!inCenter) {
                                xNew = (float)(tilex << 2) + 4.0f * TerrainUtilities.getTreePosX(tilex, tiley);
                                yNew = (float)(tiley << 2) + 4.0f * TerrainUtilities.getTreePosY(tilex, tiley);
                            }
                            if ((weight = (int)Math.max(1000.0, sizeMod * (double)(t = ItemTemplateFactory.getInstance().getTemplate(templateId)).getWeightGrams())) < 1500) {
                                templateId = 169;
                            }
                            if (templateId == 385) {
                                SoundPlayer.playSound("sound.tree.falling", tilex, tiley, true, 1.0f);
                                Item stump = ItemFactory.createItem(731, Math.min(100.0f, qualityLevel), xNew, yNew, dir, onSurface, material, act.getRarity(), -10L, null, (byte)stumpAge);
                                stump.setLastOwnerId(-10L);
                                stump.setWeight(weight, true);
                            }
                            int ta = treeAge;
                            Item newItem = ItemFactory.createItem(templateId, Math.min(100.0f, qualityLevel), xNew, yNew, dir, onSurface, material, act.getRarity(), -10L, null, (byte)ta);
                            if (templateId == 385) {
                                newItem.setAuxData((byte)treeAge);
                            }
                            if (treeAge >= 5 && performer.getDeity() != null && performer.getDeity().number == 3) {
                                performer.maybeModifyAlignment(1.0f);
                            }
                            newItem.setWeight(weight, true);
                            newItem.setLastOwnerId(performer.getWurmId());
                            if (performer.getTutorialLevel() == 3 && !performer.skippedTutorial()) {
                                if (templateId == 9) {
                                    performer.missionFinished(true, true);
                                } else {
                                    String text = "You should now chop the tree up. Right-click the " + newItem.getName() + " and chop it up. Then get the log.";
                                    SimplePopup popup = new SimplePopup(performer, "Chop up the tree", text);
                                    popup.sendQuestion();
                                }
                            }
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Factory failed to produce item log", ex);
                        }
                    }
                    if (hive != null) {
                        if (performer.getBestBeeSmoker() == null) {
                            performer.addWoundOfType(null, (byte)5, 2, true, 1.0f, false, 4000.0f + Server.rand.nextFloat() * 3000.0f, 0.0f, 0.0f, false, false);
                        }
                        for (Item item : hive.getItemsAsArray()) {
                            Items.destroyItem(item.getWurmId());
                        }
                        Items.destroyItem(hive.getWurmId());
                    }
                    PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.FELL_TREE);
                } else {
                    Server.setWorldResource(tilex, tiley, (short)dam);
                    performer.getCommunicator().sendNormalServerMessage("You chip away some wood from the " + treeString + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " chips away some wood from the " + treeString + ".", performer, 5);
                }
                PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.CUT_TREE);
            }
            catch (NoSuchSkillException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
                done = true;
            }
            catch (Exception noe) {
                logger.log(Level.WARNING, "Failed to chop at tree: ", noe);
                done = true;
            }
        }
        return done;
    }

    static short calcTime(int treeage, Item source, Skill weaponSkill, Skill woodcuttingskill) {
        if (treeage == 15) {
            treeage = 7;
        }
        int mintime = 30 + treeage * 5;
        mintime = (int)((float)mintime * 0.75f + (float)mintime * 0.2f * Math.max(1.0f, 100.0f - source.getSpellSpeedBonus()) / 100.0f);
        short time = (short)mintime;
        double bonus = 0.0;
        if (weaponSkill != null) {
            bonus = weaponSkill.getKnowledge(source, 0.0);
        }
        time = (short)((double)time * (1.0 + (100.0 - woodcuttingskill.getKnowledge(source, bonus)) / 100.0));
        time = (short)((float)time / Servers.localServer.getActionTimer());
        return time;
    }

    public static final boolean isTileModBlocked(Creature performer, int tilex, int tiley, boolean surfaced) {
        if (performer.getPower() <= 0) {
            if (Zones.isWithinDuelRing(tilex, tiley, true) != null) {
                performer.getCommunicator().sendAlertServerMessage("This is too close to the duelling ring.");
                return true;
            }
            if (Features.Feature.BLOCK_HOTA.isEnabled()) {
                for (FocusZone fz : FocusZone.getZonesAt(tilex, tiley)) {
                    if (!fz.isBattleCamp() && !fz.isPvPHota() && !fz.isNoBuild() || !fz.covers(tilex, tiley)) continue;
                    performer.getCommunicator().sendAlertServerMessage("This land is protected by the deities and may not be modified.");
                    return true;
                }
            }
        }
        return false;
    }

    public static void paintTerrain(Player player, Item wand, int tileX, int tileY) {
        byte aux = wand.getAuxData();
        if (aux == 0) {
            return;
        }
        byte newtype = Tiles.getTile((byte)aux).id;
        if (!player.isOnSurface()) {
            Terraforming.paintCaveTerrain(player, newtype, tileX, tileY);
            return;
        }
        int dx = Math.max(0, wand.getData1());
        int dy = Math.max(0, wand.getData2());
        if (dx > 10) {
            dx = 0;
        }
        if (dy > 10) {
            dy = 0;
        }
        if (dx > 10 || dy > 10 || dx < 0 || dy < 0) {
            player.getCommunicator().sendNormalServerMessage("The data1 and data2 range should be between 0 and 10.");
            return;
        }
        if (dx == 0 && dy == 0 && Tiles.decodeType(Server.surfaceMesh.getTile(tileX, tileY)) == newtype) {
            player.getCommunicator().sendNormalServerMessage("The terrain is already of that type.");
            return;
        }
        if (Tiles.isSolidCave(newtype) || newtype == Tiles.Tile.TILE_CAVE.id || newtype == Tiles.Tile.TILE_CAVE_FLOOR_REINFORCED.id) {
            if (player.getPower() >= 5) {
                if (Tiles.isSolidCave(newtype)) {
                    Tiles.Tile theNewTile = Tiles.getTile(newtype);
                    if (theNewTile != null) {
                        Server.caveMesh.setTile(tileX, tileY, Tiles.encode(Tiles.decodeHeight(Server.caveMesh.getTile(tileX, tileY)), theNewTile.id, Tiles.decodeData(Server.caveMesh.getTile(tileX, tileY))));
                        Players.getInstance().sendChangedTiles(tileX, tileY, 1, 1, false, false);
                    }
                } else {
                    player.getCommunicator().sendNormalServerMessage("You can only change to solid rock types at the moment.");
                }
            } else {
                player.getCommunicator().sendNormalServerMessage("Only implementors may set the terrain to some sort of rock.");
            }
            return;
        }
        for (int x = 0; x < Math.max(1, dx); ++x) {
            for (int y = 0; y < Math.max(1, dy); ++y) {
                GrassData.FlowerType flowerType;
                byte oldType = Tiles.decodeType(Server.surfaceMesh.getTile(tileX - dx / 2 + x, tileY - dy / 2 + y));
                if (player.getPower() < 5 && (newtype == Tiles.Tile.TILE_ROCK.id || oldType == Tiles.Tile.TILE_ROCK.id || newtype == Tiles.Tile.TILE_CLIFF.id || oldType == Tiles.Tile.TILE_CLIFF.id)) {
                    player.getCommunicator().sendNormalServerMessage("That would have impact on the rock layer, and is not allowed for now.");
                    continue;
                }
                Tiles.Tile theNewTile = Tiles.getTile(newtype);
                byte data = 0;
                byte theNewType = newtype;
                if (newtype == Tiles.Tile.TILE_GRASS.id && (flowerType = Terraforming.getRandomFlower(GrassData.FlowerType.NONE, false)) != GrassData.FlowerType.NONE) {
                    GrassData.GrowthStage stage = GrassData.GrowthStage.decodeTileData(0);
                    data = GrassData.encodeGrassTileData(stage, flowerType);
                }
                if (newtype == Tiles.Tile.TILE_ROCK.id) {
                    Server.caveMesh.setTile(tileX - dx / 2 + x, tileY - dy / 2 + y, Tiles.encode((short)-100, Tiles.Tile.TILE_CAVE_WALL.id, (byte)0));
                    Server.rockMesh.setTile(tileX - dx / 2 + x, tileY - dy / 2 + y, Tiles.encode(Tiles.decodeHeight(Server.surfaceMesh.getTile(tileX - dx / 2 + x, tileY - dy / 2 + y)), Tiles.Tile.TILE_ROCK.id, (byte)0));
                } else if (theNewTile.isTree() || theNewTile.isBush()) {
                    byte treeAge = (byte)Server.rand.nextInt(FoliageAge.values().length);
                    byte grass = (byte)(1 + Server.rand.nextInt(3));
                    data = Tiles.encodeTreeData(treeAge, false, false, grass);
                }
                if (Tiles.getTile((byte)aux).id == Tiles.Tile.TILE_ROCK.id) {
                    Server.caveMesh.setTile(tileX - dx / 2 + x, tileY - dy / 2 + y, Tiles.encode((short)-100, Tiles.Tile.TILE_CAVE_WALL.id, (byte)0));
                    Server.rockMesh.setTile(tileX - dx / 2 + x, tileY - dy / 2 + y, Tiles.encode(Tiles.decodeHeight(Server.surfaceMesh.getTile(tileX - dx / 2 + x, tileY - dy / 2 + y)), Tiles.Tile.TILE_ROCK.id, (byte)0));
                    continue;
                }
                if (oldType == Tiles.Tile.TILE_HOLE.id || Tiles.isMineDoor(oldType)) continue;
                Server.setSurfaceTile(tileX - dx / 2 + x, tileY - dy / 2 + y, Tiles.decodeHeight(Server.surfaceMesh.getTile(tileX - dx / 2 + x, tileY - dy / 2 + y)), theNewType, data);
            }
        }
        Players.getInstance().sendChangedTiles(tileX - dx / 2, tileY - dy / 2, Math.max(1, dx), Math.max(1, dy), true, true);
    }

    public static void paintCaveTerrain(Player player, byte newtype, int tilex, int tiley) {
        int currentTile = Server.caveMesh.getTile(tilex, tiley);
        byte currentType = Tiles.decodeType(currentTile);
        if (currentType != Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (newtype == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(newtype) || Tiles.isSolidCave(newtype)) {
                boolean succeeded;
                if (Tiles.isSolidCave(currentType)) {
                    if (Tiles.isSolidCave(newtype)) {
                        succeeded = true;
                    } else {
                        int rtx = player.getTileX();
                        int rty = player.getTileY();
                        int dir = 2;
                        if (rty - tiley < 0) {
                            dir = 5;
                        } else if (rty - tiley > 0) {
                            dir = 3;
                        } else if (rtx - tilex < 0) {
                            dir = 4;
                        }
                        succeeded = TileRockBehaviour.createInsideTunnel(tilex, tiley, currentTile, player, 145, dir, true, null);
                    }
                } else if (currentType == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(currentType)) {
                    if (Tiles.isSolidCave(newtype)) {
                        Terraforming.setAsRock(tilex, tiley, false);
                        succeeded = true;
                    } else {
                        succeeded = true;
                    }
                } else {
                    succeeded = true;
                }
                if (succeeded) {
                    int returnTile = Server.caveMesh.getTile(tilex, tiley);
                    Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(returnTile), newtype, Tiles.decodeData(returnTile)));
                    Players.getInstance().sendChangedTile(tilex, tiley, false, true);
                }
            } else {
                player.getCommunicator().sendNormalServerMessage("You must select a cave tile to change to.");
            }
        } else if (currentType == Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (Tiles.isRoadType(newtype)) {
                Server.setClientCaveFlags(tilex, tiley, newtype);
                Players.getInstance().sendChangedTile(tilex, tiley, false, true);
            } else {
                player.getCommunicator().sendNormalServerMessage("Removing cave openings is not supported. Use a shaker orb.");
            }
        } else {
            player.getCommunicator().sendNormalServerMessage("Removing cave openings is not supported. Use a shaker orb.");
        }
    }

    public static GrassData.FlowerType getRandomFlower(GrassData.FlowerType flowerType, boolean ignoreSeason) {
        int rnd = Server.rand.nextInt(60000);
        if (rnd < 1000) {
            if (flowerType == GrassData.FlowerType.NONE && (ignoreSeason || !WurmCalendar.isAutumnWinter())) {
                if (rnd > 998) {
                    return GrassData.FlowerType.FLOWER_7;
                }
                if (rnd > 990) {
                    return GrassData.FlowerType.FLOWER_6;
                }
                if (rnd > 962) {
                    return GrassData.FlowerType.FLOWER_5;
                }
                if (rnd > 900) {
                    return GrassData.FlowerType.FLOWER_4;
                }
                if (rnd > 800) {
                    return GrassData.FlowerType.FLOWER_3;
                }
                if (rnd > 500) {
                    return GrassData.FlowerType.FLOWER_2;
                }
                return GrassData.FlowerType.FLOWER_1;
            }
            if (!ignoreSeason && WurmCalendar.isAutumnWinter()) {
                return GrassData.FlowerType.NONE;
            }
        }
        return flowerType;
    }

    public static boolean cannotMakeLawn(Creature performer, int tilex, int tiley) {
        return !Methods.isActionAllowed(performer, (short)644, performer.getTileX(), performer.getTileY());
    }

    private static final boolean isCaveExitBorder(int x, int y) {
        int currtile = Server.caveMesh.getTile(x, y);
        short cceil = (short)(Tiles.decodeData(currtile) & 0xFF);
        return cceil == 0;
    }

    public static final void flattenImmediately(Creature performer, int stx, int endtx, int sty, int endty, float minDirtDist, int lowerByAmount, boolean toRock) {
        int curry;
        int currx;
        float flattenToHeight = performer.getPositionZ() - (float)lowerByAmount / 10.0f;
        float totalHeightRock = flattenToHeight - (toRock ? 0.0f : minDirtDist);
        float totalHeightDirt = flattenToHeight;
        if (performer.getPower() < 4) {
            logger.warning(performer.getName() + " attempted to use flattenImmediately with a power level of " + performer.getPower() + ", DENYING!");
            return;
        }
        logger.info(performer.getName() + " used flattenImmediately stx:" + stx + ", sty:" + sty + ", endx:" + endtx + ", endy:" + endty + ", lower by extra amount:" + lowerByAmount + ", minDirtDist:" + minDirtDist + ", flattenToRock:" + toRock);
        double mapSize = Math.pow(2.0, Constants.meshSize) - 1.0;
        if ((double)endtx > mapSize || (double)endty > mapSize || stx < 1 || sty < 1) {
            performer.getCommunicator().sendAlertServerMessage("YOU CAN NOT MAKE THE FLATTEN ZONE EXPAND PAST A SERVER BORDER");
            return;
        }
        if (performer.getPositionZ() + minDirtDist > 32767.0f || performer.getPositionZ() + (float)lowerByAmount > 32767.0f || performer.getPositionZ() + minDirtDist - (float)lowerByAmount > 32767.0f) {
            performer.getCommunicator().sendAlertServerMessage("You may not set a (combined) value larger than 32767");
            return;
        }
        if (performer.getPositionZ() - minDirtDist < -32768.0f || performer.getPositionZ() - (float)lowerByAmount < -32768.0f || performer.getPositionZ() - minDirtDist + (float)lowerByAmount < -32768.0f) {
            performer.getCommunicator().sendAlertServerMessage("You may not set a (combined) value less than 32768");
            return;
        }
        class CompareCoordinates
        implements Comparable<CompareCoordinates> {
            private final int x;
            private final int y;

            public CompareCoordinates(int _x, int _y) {
                this.x = _x;
                this.y = _y;
            }

            public int getX() {
                return this.x;
            }

            public int getY() {
                return this.y;
            }

            @Override
            public int compareTo(CompareCoordinates o) {
                if (o.getX() == this.x) {
                    if (o.getY() == this.getY()) {
                        return 0;
                    }
                    if (o.getY() < this.getY()) {
                        return 1;
                    }
                    return -1;
                }
                if (o.getX() < this.getX()) {
                    return 1;
                }
                return -1;
            }
        }
        class InstantFlattenHolder {
            private HolderCave hasCave = HolderCave.NO_CAVE;
            private float flattenToHeightDirt;
            private float flattenToHeightRock;
            private final int x;
            private final int y;
            private final int storedSurface;
            private final int storedRock;
            private final int storedCave;

            public InstantFlattenHolder(int _x, int _y, int _surface, int _rock, int _cave, float _flattenToHeightRock, float _flattenToHeightDirt) {
                this.flattenToHeightRock = _flattenToHeightRock;
                this.flattenToHeightDirt = _flattenToHeightDirt;
                this.x = _x;
                this.y = _y;
                this.storedSurface = _surface;
                this.storedRock = _rock;
                this.storedCave = _cave;
            }

            public HolderCave getCave() {
                return this.hasCave;
            }

            public void setCave(HolderCave cave) {
                this.hasCave = cave;
            }

            public float getFlattenToHeightRock() {
                return this.flattenToHeightRock;
            }

            public void setFlattenToHeightRock(float _flattenToHeightRock) {
                this.flattenToHeightRock = _flattenToHeightRock;
            }

            public float getFlattenToHeightDirt() {
                return this.flattenToHeightDirt;
            }

            public void setFlattenToHeightDirt(float _flattenToHeightDirt) {
                this.flattenToHeightDirt = _flattenToHeightDirt;
            }

            public void handleCaveCalcMagic() {
                float surfaceHeight = Tiles.decodeHeightAsFloat(this.storedSurface);
                float rockHeight = Tiles.decodeHeightAsFloat(this.storedRock);
                float caveHeight = Tiles.decodeHeightAsFloat(this.storedCave);
                short ceilingHeight = (short)(Tiles.decodeData(this.storedCave) & 0xFF);
                float totalCaveHeightCaves = (float)ceilingHeight / 10.0f + caveHeight + 0.2f;
                float totalCaveHeightExits = (float)ceilingHeight / 10.0f + caveHeight;
                if (this.getCave().value == HolderCave.CAVE.value || this.getCave().value == HolderCave.CAVE_NEIGHBOUR.value) {
                    if (toRock) {
                        if (this.flattenToHeightRock < totalCaveHeightCaves) {
                            this.setFlattenToHeightRock(totalCaveHeightCaves);
                            this.setFlattenToHeightDirt(totalCaveHeightCaves);
                        }
                    } else {
                        float dirtHeight;
                        float toRockMod = toRock ? 0.0f : minDirtDist;
                        float currentDifference = surfaceHeight - rockHeight;
                        float f = dirtHeight = currentDifference < toRockMod ? currentDifference : toRockMod;
                        if (this.flattenToHeightRock < totalCaveHeightCaves) {
                            this.setFlattenToHeightRock(totalCaveHeightCaves);
                            this.setFlattenToHeightDirt(totalCaveHeightCaves + dirtHeight);
                        }
                    }
                } else if (this.getCave().value >= HolderCave.CAVE_EXIT.value) {
                    this.setFlattenToHeightDirt(totalCaveHeightExits);
                    this.setFlattenToHeightRock(totalCaveHeightExits);
                } else {
                    performer.getCommunicator().sendAlertServerMessage("This should never be reached, but were for [" + this.x + ", " + this.y + "] it has a getCave value of " + this.getCave().toString());
                    this.setCave(HolderCave.VALUE_OF_SHAME);
                }
            }
        }
        TreeMap<CompareCoordinates, InstantFlattenHolder> flattenArea = new TreeMap<CompareCoordinates, InstantFlattenHolder>();
        for (currx = stx; currx <= endtx + 1; ++currx) {
            for (curry = sty; curry <= endty + 1; ++curry) {
                float currentRockLayerHeight;
                int currTileCave = Server.caveMesh.getTile(currx, curry);
                int currTileRock = Server.rockMesh.getTile(currx, curry);
                int currTileSurface = Server.surfaceMesh.getTile(currx, curry);
                float toPutRockHeight = !toRock ? (totalHeightRock > (currentRockLayerHeight = Tiles.decodeHeightAsFloat(currTileRock)) ? currentRockLayerHeight : totalHeightRock) : totalHeightRock;
                flattenArea.put(new CompareCoordinates(currx, curry), new InstantFlattenHolder(currx, curry, currTileSurface, currTileRock, currTileCave, toPutRockHeight, totalHeightDirt));
            }
        }
        for (currx = stx; currx <= endtx + 1; ++currx) {
            for (curry = sty; curry <= endty + 1; ++curry) {
                InstantFlattenHolder SW;
                InstantFlattenHolder SE;
                InstantFlattenHolder NE;
                InstantFlattenHolder NW = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(currx, curry));
                byte ID2 = Tiles.decodeType(NW.storedCave);
                if (ID2 == Tiles.Tile.TILE_CAVE_EXIT.id) {
                    NE = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x + 1, NW.y));
                    SE = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x + 1, NW.y + 1));
                    SW = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x, NW.y + 1));
                    if (Terraforming.isCaveExitBorder(currx, curry)) {
                        if (Terraforming.isCaveExitBorder(currx + 1, curry)) {
                            if (NW != null) {
                                NW.setCave(HolderCave.CAVE_EXIT_NORTH_NW);
                            }
                            if (NE != null) {
                                NE.setCave(HolderCave.CAVE_EXIT_NORTH_NE);
                            }
                            if (SE != null) {
                                SE.setCave(HolderCave.CAVE_EXIT_NORTH_SE);
                            }
                            if (SW != null) {
                                SW.setCave(HolderCave.CAVE_EXIT_NORTH_SW);
                            }
                            if (!Constants.devmode) continue;
                            performer.getCommunicator().sendAlertServerMessage("Cave entrance with north border at [" + currx + ", " + curry + "]");
                            continue;
                        }
                        if (NW != null) {
                            NW.setCave(HolderCave.CAVE_EXIT_WEST_NW);
                        }
                        if (NE != null) {
                            NE.setCave(HolderCave.CAVE_EXIT_WEST_NE);
                        }
                        if (SE != null) {
                            SE.setCave(HolderCave.CAVE_EXIT_WEST_SE);
                        }
                        if (SW != null) {
                            SW.setCave(HolderCave.CAVE_EXIT_WEST_SW);
                        }
                        if (!Constants.devmode) continue;
                        performer.getCommunicator().sendAlertServerMessage("Cave entrance with west border at [" + currx + ", " + curry + "]");
                        continue;
                    }
                    if (Terraforming.isCaveExitBorder(currx + 1, curry)) {
                        if (NW != null) {
                            NW.setCave(HolderCave.CAVE_EXIT_EAST_NW);
                        }
                        if (NE != null) {
                            NE.setCave(HolderCave.CAVE_EXIT_EAST_NE);
                        }
                        if (SE != null) {
                            SE.setCave(HolderCave.CAVE_EXIT_EAST_SE);
                        }
                        if (SW != null) {
                            SW.setCave(HolderCave.CAVE_EXIT_EAST_SW);
                        }
                        if (!Constants.devmode) continue;
                        performer.getCommunicator().sendAlertServerMessage("Cave entrance with east border at [" + currx + ", " + curry + "]");
                        continue;
                    }
                    if (NW != null) {
                        NW.setCave(HolderCave.CAVE_EXIT_SOUTH_NW);
                    }
                    if (NE != null) {
                        NE.setCave(HolderCave.CAVE_EXIT_SOUTH_NE);
                    }
                    if (SE != null) {
                        SE.setCave(HolderCave.CAVE_EXIT_SOUTH_SE);
                    }
                    if (SW != null) {
                        SW.setCave(HolderCave.CAVE_EXIT_SOUTH_SW);
                    }
                    if (!Constants.devmode) continue;
                    performer.getCommunicator().sendAlertServerMessage("Cave entrance with south border at [" + currx + ", " + curry + "]");
                    continue;
                }
                if (Tiles.isSolidCave(ID2)) continue;
                NE = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x + 1, NW.y));
                SE = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x + 1, NW.y + 1));
                SW = (InstantFlattenHolder)flattenArea.get(new CompareCoordinates(NW.x, NW.y + 1));
                if (NW != null && NW.getCave().value < HolderCave.CAVE.value) {
                    NW.setCave(HolderCave.CAVE);
                }
                if (NE != null && NE.getCave().value < HolderCave.CAVE_NEIGHBOUR.value) {
                    NE.setCave(HolderCave.CAVE_NEIGHBOUR);
                }
                if (SE != null && SE.getCave().value < HolderCave.CAVE_NEIGHBOUR.value) {
                    SE.setCave(HolderCave.CAVE_NEIGHBOUR);
                }
                if (SW == null || SW.getCave().value >= HolderCave.CAVE_NEIGHBOUR.value) continue;
                SW.setCave(HolderCave.CAVE_NEIGHBOUR);
            }
        }
        for (InstantFlattenHolder ifh : flattenArea.values()) {
            if (ifh.getCave() == HolderCave.NO_CAVE) continue;
            ifh.handleCaveCalcMagic();
        }
        for (InstantFlattenHolder ifh : flattenArea.values()) {
            float toReturnDirtHeight;
            int spawnedTypeSurface;
            byte spawnedTypeRock;
            float toReturnRock;
            byte rockData = Tiles.decodeData(ifh.storedRock);
            if (ifh.getCave().value == HolderCave.CAVE_EXIT_CORNER_MATTERS_ROCK.value || ifh.getCave().value == HolderCave.CAVE_EXIT_CORNER_MATTERS_BOTH.value) {
                toReturnRock = ifh.getFlattenToHeightRock();
                spawnedTypeRock = Tiles.decodeType(ifh.storedRock);
            } else {
                toReturnRock = ifh.getFlattenToHeightRock();
                spawnedTypeRock = 4;
            }
            Server.rockMesh.setTile(ifh.x, ifh.y, Tiles.encode(toReturnRock, spawnedTypeRock, rockData));
            if (ifh.getCave() == HolderCave.VALUE_OF_SHAME) {
                spawnedTypeSurface = 37;
                toReturnDirtHeight = Tiles.decodeHeightAsFloat(ifh.storedRock);
            } else if (ifh.getCave().value == HolderCave.CAVE_EXIT_CORNER_MATTERS_DIRT.value || ifh.getCave().value == HolderCave.CAVE_EXIT_CORNER_MATTERS_BOTH.value) {
                toReturnDirtHeight = ifh.getFlattenToHeightRock();
                spawnedTypeSurface = Tiles.decodeType(ifh.storedSurface);
            } else if (ifh.getCave() == HolderCave.CAVE || ifh.getCave() == HolderCave.CAVE_NEIGHBOUR || ifh.getCave().value == HolderCave.CAVE_EXIT.value) {
                spawnedTypeSurface = toRock ? 4 : (int)((byte)(performer.getKingdomTemplateId() == 3 ? 10 : 2));
                toReturnDirtHeight = ifh.getFlattenToHeightDirt();
            } else if (toRock) {
                spawnedTypeSurface = 4;
                toReturnDirtHeight = ifh.getFlattenToHeightDirt();
            } else {
                spawnedTypeSurface = (byte)(performer.getKingdomTemplateId() == 3 ? 10 : 2);
                toReturnDirtHeight = totalHeightDirt;
            }
            Server.surfaceMesh.setTile(ifh.x, ifh.y, Tiles.encode(toReturnDirtHeight, (byte)spawnedTypeSurface, (byte)0));
        }
        Players.getInstance().sendChangedTiles(stx, sty, endtx - stx + 2, endty - sty + 2, true, false);
        Players.getInstance().sendChangedTiles(stx, sty, endtx - stx + 2, endty - sty + 2, false, false);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    static final boolean plantTrellis(Creature performer, Item trellis, int tilex, int tiley, boolean onSurface, Tiles.TileBorderDirection dir, short action, float counter, Action act) {
        float hoffy;
        float hoffx;
        float rot;
        block29: {
            block30: {
                if (trellis.getCurrentQualityLevel() < 10.0f) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " is of too poor quality to be planted.");
                    return true;
                }
                if (trellis.getDamage() > 70.0f) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " is too heavily damaged to be planted.");
                    return true;
                }
                if (performer.getFloorLevel() != 0) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " can not be planted unless on ground level.");
                    return true;
                }
                if (trellis.isSurfaceOnly() && !performer.isOnSurface()) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " can only be planted on the surface.");
                    return true;
                }
                if (!onSurface) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " would never grow inside a cave.");
                    return true;
                }
                if (trellis.isPlanted()) {
                    performer.getCommunicator().sendNormalServerMessage("The " + trellis.getName() + " is already planted.", (byte)3);
                    return true;
                }
                rot = 0.0f;
                float hoff = 0.3f;
                hoffx = 2.0f;
                hoffy = 2.0f;
                if (tilex != performer.getTileX()) break block30;
                if (tiley == performer.getTileY()) {
                    if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
                        hoffy = 0.3f;
                        hoffx = action == 746 ? 1.0f : (action == 747 ? 3.0f : 2.0f);
                        break block29;
                    } else {
                        hoffx = 0.3f;
                        rot = 270.0f;
                        hoffy = action == 746 ? 3.0f : (action == 747 ? 1.0f : 2.0f);
                    }
                    break block29;
                } else {
                    if (tiley - 1 != performer.getTileY()) {
                        performer.getCommunicator().sendNormalServerMessage("You cannot reach that far.");
                        return true;
                    }
                    hoffy = 3.7f;
                    rot = 180.0f;
                    hoffx = action == 746 ? 3.0f : (action == 747 ? 1.0f : 2.0f);
                }
                break block29;
            }
            if (tilex - 1 != performer.getTileX()) {
                performer.getCommunicator().sendNormalServerMessage("You cannot reach that far.");
                return true;
            }
            if (tiley != performer.getTileY()) {
                performer.getCommunicator().sendNormalServerMessage("You cannot reach that far.");
                return true;
            }
            hoffx = 3.7f;
            rot = 90.0f;
            hoffy = action == 746 ? 1.0f : (action == 747 ? 3.0f : 2.0f);
        }
        int time = Actions.getPlantActionTime(performer, trellis);
        if (counter == 1.0f) {
            VolaTile vt;
            if (performer instanceof Player) {
                Player p = (Player)performer;
                try {
                    Skills skills = p.getSkills();
                    Skill dig = skills.getSkill(1009);
                    if (dig.getRealKnowledge() < 10.0) {
                        performer.getCommunicator().sendNormalServerMessage("You need to have 10 in the skill digging to secure " + trellis.getTemplate().getPlural() + " to the ground.", (byte)3);
                        return true;
                    }
                }
                catch (NoSuchSkillException nss) {
                    performer.getCommunicator().sendNormalServerMessage("You need 10 digging to plant " + trellis.getTemplate().getPlural() + ".", (byte)3);
                    return true;
                }
            }
            if (!Methods.isActionAllowed(performer, (short)176)) {
                return true;
            }
            int tile = performer.getCurrentTileNum();
            if (Tiles.decodeHeight(tile) < 0) {
                performer.getCommunicator().sendNormalServerMessage("The water is too deep to plant the " + trellis.getName() + ".", (byte)3);
                return true;
            }
            if (performer.getStatus().getBridgeId() != -10L) {
                performer.getCommunicator().sendNormalServerMessage("You cannot plant a " + trellis.getName() + " on a bridge as no soil for it to grow from.", (byte)3);
                return true;
            }
            if (trellis.isFourPerTile() && (vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), trellis.isOnSurface())) != null && vt.getFourPerTileCount(0) >= 4) {
                performer.getCommunicator().sendNormalServerMessage("You cannot plant a " + trellis.getName() + " as there are four here already.", (byte)3);
                return true;
            }
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to plant the " + trellis.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to plant " + trellis.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl("Planting " + trellis.getName(), true, time);
            performer.getStatus().modifyStamina(-400.0f);
        } else {
            time = act.getTimeLeft();
            if (act.currentSecond() % 5 == 0) {
                performer.getStatus().modifyStamina(-1000.0f);
            }
        }
        if (!(counter * 10.0f > (float)time)) return false;
        try {
            VolaTile vt;
            if (trellis.isFourPerTile() && (vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), trellis.isOnSurface())) != null && vt.getFourPerTileCount(0) == 4) {
                performer.getCommunicator().sendNormalServerMessage("You cannot plant a " + trellis.getName() + " as there are four here already.", (byte)3);
                return true;
            }
            long lParentId = trellis.getParentId();
            if (lParentId != -10L) {
                Item parent = Items.getItem(lParentId);
                parent.dropItem(trellis.getWurmId(), false);
            }
            int encodedTile = Server.surfaceMesh.getTile(performer.getTileX(), performer.getTileY());
            float npsz = Tiles.decodeHeightAsFloat(encodedTile);
            trellis.setPos((float)(performer.getTileX() * 4) + hoffx, (float)(performer.getTileY() * 4) + hoffy, npsz, rot, -10L);
            Zone zone = Zones.getZone(Zones.safeTileX(performer.getTileX()), Zones.safeTileY(performer.getTileY()), performer.isOnSurface());
            zone.addItem(trellis);
            trellis.setIsPlanted(true);
            performer.getCommunicator().sendNormalServerMessage("You plant the " + trellis.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " plants the " + trellis.getName() + ".", performer, 5);
            return true;
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("You fail to plant the " + trellis.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + nsz.getMessage(), nsz);
            return true;
        }
        catch (NoSuchItemException nsie) {
            performer.getCommunicator().sendNormalServerMessage("You fail to plant the " + trellis.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + nsie.getMessage(), nsie);
        }
        return true;
    }

    private static int calcMaxGrubs(double currentSkill, @Nullable Item tool) {
        float extraChance;
        int bonus = 0;
        if (tool != null && tool.getSpellEffects() != null && (extraChance = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FARMYIELD) - 1.0f) > 0.0f && Server.rand.nextFloat() < extraChance) {
            ++bonus;
        }
        return Math.min(4, (int)(currentSkill + 28.0) / 27 + bonus);
    }

    private static int calcMaxHarvest(int tile, double currentSkill, Item tool) {
        float extraChance;
        byte data = Tiles.decodeData(tile);
        byte age = FoliageAge.getAgeAsByte(data);
        int maxByAge = 1;
        if (age < FoliageAge.OLD_ONE.getAgeId()) {
            maxByAge = 1;
        } else if (age < FoliageAge.OLD_TWO.getAgeId()) {
            maxByAge = 2;
        } else if (age < FoliageAge.VERY_OLD.getAgeId()) {
            maxByAge = 3;
        } else if (age < FoliageAge.OVERAGED.getAgeId()) {
            maxByAge = 4;
        }
        int bonus = 0;
        if (tool.getSpellEffects() != null && (extraChance = tool.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_FARMYIELD) - 1.0f) > 0.0f && Server.rand.nextFloat() < extraChance) {
            ++bonus;
        }
        return Math.min(maxByAge, (int)(currentSkill + 28.0) / 27 + bonus);
    }

    static enum HolderCave {
        NO_CAVE(0),
        VALUE_OF_SHAME(0),
        CAVE_NEIGHBOUR(1),
        CAVE(2),
        CAVE_EXIT(3),
        CAVE_EXIT_CORNER_MATTERS_ROCK(20),
        CAVE_EXIT_CORNER_MATTERS_DIRT(30),
        CAVE_EXIT_CORNER_MATTERS_BOTH(40),
        CAVE_EXIT_NORTH_NW(40),
        CAVE_EXIT_NORTH_NE(20),
        CAVE_EXIT_NORTH_SE(3),
        CAVE_EXIT_NORTH_SW(3),
        CAVE_EXIT_EAST_NW(30),
        CAVE_EXIT_EAST_NE(20),
        CAVE_EXIT_EAST_SE(20),
        CAVE_EXIT_EAST_SW(3),
        CAVE_EXIT_SOUTH_NW(30),
        CAVE_EXIT_SOUTH_NE(3),
        CAVE_EXIT_SOUTH_SE(20),
        CAVE_EXIT_SOUTH_SW(3),
        CAVE_EXIT_WEST_NW(40),
        CAVE_EXIT_WEST_NE(3),
        CAVE_EXIT_WEST_SE(3),
        CAVE_EXIT_WEST_SW(3);

        private int value;

        private HolderCave(int value) {
            this.value = value;
        }
    }
}

