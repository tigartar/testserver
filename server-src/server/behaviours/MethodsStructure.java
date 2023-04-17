/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.BridgePartBehaviour;
import com.wurmonline.server.behaviours.FloorBehaviour;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.WurmPermissions;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.DbFenceGate;
import com.wurmonline.server.structures.DbFloor;
import com.wurmonline.server.structures.DbWall;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.IFloor;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MethodsStructure
implements MiscConstants,
QuestionTypes,
ItemTypes,
CounterTypes,
ItemMaterials,
SoundNames,
VillageStatus,
TimeConstants {
    public static final String cvsversion = "$Id: MethodsStructure.java,v 1.39 2007-04-19 23:05:18 root Exp $";
    private static final Logger logger = Logger.getLogger(MethodsStructure.class.getName());
    private static final float DEITY_FENCE_QL_GV = 100.0f;
    private static final float DEITY_FENCE_QL_OTHER = 80.0f;
    public static final int minDistanceToAltars = 20;

    private MethodsStructure() {
    }

    static void tryToFinalize(Creature performer, int tilex, int tiley) {
        Structure structure;
        try {
            structure = performer.getStructure();
            if (structure.isFinalized()) {
                performer.getCommunicator().sendNormalServerMessage("The " + structure.getName() + " is already finalized.", (byte)3);
                return;
            }
            if (structure.getWurmId() != performer.getBuildingId()) {
                performer.getCommunicator().sendNormalServerMessage("You are not planning this house.", (byte)3);
                return;
            }
            if (!structure.contains(tilex, tiley)) {
                performer.getCommunicator().sendNormalServerMessage("You don't even plan to build there!", (byte)3);
                return;
            }
            if (!Methods.isActionAllowed(performer, (short)58)) {
                return;
            }
        }
        catch (NoSuchStructureException nss) {
            performer.getCommunicator().sendNormalServerMessage("You don't even plan to build there!", (byte)3);
            return;
        }
        try {
            if (!MethodsStructure.hasEnoughSkillToExpandStructure(performer, tilex, tiley, structure)) {
                return;
            }
            if (!structure.makeFinal(performer, performer.getName() + "'s ")) {
                performer.getCommunicator().sendNormalServerMessage("You don't even have a house planned.", (byte)3);
                return;
            }
            performer.getCommunicator().sendNormalServerMessage("You finish planning the house.");
            Server.getInstance().broadCastAction(performer.getName() + " finishes planning the house.", performer, 5);
            performer.getStatus().setBuildingId(structure.getWurmId());
            performer.achievement(518);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to save house for " + performer.getWurmId() + " at " + tilex + ", " + tiley, iox);
            performer.getCommunicator().sendNormalServerMessage("You finish planning the house, but there was a problem on the server. Please report this.");
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "Failed to locate zone for " + performer.getWurmId() + " building at " + tilex + ", " + tiley, nsz);
            performer.getCommunicator().sendNormalServerMessage("You cannot finish planning the house, as there was a problem on the server. Please report this.");
        }
    }

    static boolean canPlanStructureAt(Creature performer, Item tool, int tilex, int tiley, int tile) {
        VolaTile vtile;
        int maxwest;
        int maxeast;
        int maxsouth;
        int maxnorth;
        byte type = Tiles.decodeType(tile);
        if (performer.isGuest()) {
            performer.getCommunicator().sendNormalServerMessage("Sorry, guests can't build structures.", (byte)3);
            return false;
        }
        if (tool == null) {
            performer.getCommunicator().sendNormalServerMessage("You need a proper building tool activated to do that.", (byte)3);
            return false;
        }
        if (!MethodsStructure.isCorrectToolForPlanning(performer, tool.getTemplateId())) {
            performer.getCommunicator().sendNormalServerMessage("You need a proper building tool activated to do that.", (byte)3);
            return false;
        }
        if (MethodsStructure.tileBordersToFence(tilex, tiley, 0, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("A fence already exists there.", (byte)3);
            return false;
        }
        if (Terraforming.isTileModBlocked(performer, tilex, tiley, true)) {
            return false;
        }
        EndGameItem alt = EndGameItems.getEvilAltar();
        if (alt != null) {
            maxnorth = Zones.safeTileY(tiley - 20);
            maxsouth = Zones.safeTileY(tiley + 20);
            maxeast = Zones.safeTileX(tilex - 20);
            maxwest = Zones.safeTileX(tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.", (byte)3);
                return false;
            }
        }
        if ((alt = EndGameItems.getGoodAltar()) != null) {
            maxnorth = Zones.safeTileY(tiley - 20);
            maxsouth = Zones.safeTileY(tiley + 20);
            maxeast = Zones.safeTileX(tilex - 20);
            maxwest = Zones.safeTileX(tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, since this is holy ground.", (byte)3);
                return false;
            }
        }
        if (Features.Feature.CAVE_DWELLINGS.isEnabled() && !performer.isOnSurface()) {
            if (!Tiles.isReinforcedFloor(type) && !Tiles.isRoadType(type)) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, you need to reinforce the floor first.", (byte)3);
                return false;
            }
            if (MethodsStructure.needSurroundingTilesFloors(performer, tilex, tiley)) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, there must be a gap around the building.", (byte)3);
                return false;
            }
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    int theTtile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    short ceil = (short)(Tiles.decodeData(theTtile) & 0xFF);
                    if (ceil >= 30) continue;
                    performer.getCommunicator().sendNormalServerMessage("The ceiling is too close.");
                    return false;
                }
            }
        } else if (performer.isOnSurface() && !Terraforming.isBuildTile(type)) {
            performer.getCommunicator().sendSafeServerMessage("You cannot build here, you need to prepare the ground first.", (byte)3);
            return false;
        }
        if (!Terraforming.isFlat(tilex, tiley, performer.isOnSurface(), 0)) {
            performer.getCommunicator().sendNormalServerMessage("The ground is not flat enough.", (byte)3);
            return false;
        }
        if (Terraforming.isCornerUnderWater(tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("You can't build on water.", (byte)3);
            return false;
        }
        if (!Zones.isOnPvPServer(tilex, tiley) && ((vtile = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface())) == null || vtile.getVillage() == null) && Tiles.isRoadType(type) && MethodsHighways.onHighway(tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to build structures on highways outside of settlements.", (byte)3);
            return false;
        }
        if (!Methods.isActionAllowed(performer, (short)56, tilex, tiley)) {
            return false;
        }
        if (MethodsStructure.wouldBuildOnOutsideItem(tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("An item is in the way.");
            return false;
        }
        return true;
    }

    static boolean expandHouseTile(Creature performer, Item tool, int tilex, int tiley, int tile, float counter) {
        List<Structure> nearStructures = null;
        Structure plannedStructure = null;
        if (!MethodsStructure.canPlanStructureAt(performer, tool, tilex, tiley, tile)) {
            return true;
        }
        VolaTile newTile = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
        if (newTile.getStructure() != null) {
            performer.getCommunicator().sendNormalServerMessage("There is already a building there.");
            return true;
        }
        nearStructures = MethodsStructure.getStructuresNear(tilex, tiley, performer.isOnSurface());
        if (nearStructures.isEmpty()) {
            performer.getCommunicator().sendNormalServerMessage("There is no building near that can be expanded upon.");
            return true;
        }
        if (nearStructures.size() > 1) {
            performer.getCommunicator().sendNormalServerMessage("You cannot expand in that direction. Too many buildings are close.");
            return true;
        }
        Structure structureToExpand = nearStructures.get(0);
        try {
            plannedStructure = performer.getStructure();
            if (plannedStructure != null && plannedStructure.getWurmId() != structureToExpand.getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("You already have another building under construction. Finish that one before trying to expand another.");
                return true;
            }
        }
        catch (NoSuchStructureException noSuchStructureException) {
            // empty catch block
        }
        if (Features.Feature.CAVE_DWELLINGS.isEnabled() && !performer.isOnSurface()) {
            byte type = Tiles.decodeType(tile);
            if (!Tiles.isReinforcedFloor(type) && !Tiles.isRoadType(type)) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, you need to reinforce the floor first.", (byte)3);
                return false;
            }
            if (MethodsStructure.needSurroundingTilesFloors(performer, tilex, tiley)) {
                performer.getCommunicator().sendSafeServerMessage("You cannot build here, there must be a gap around the building.", (byte)3);
                return false;
            }
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    int theTtile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    short ceil = (short)(Tiles.decodeData(theTtile) & 0xFF);
                    if (ceil >= 30) continue;
                    performer.getCommunicator().sendNormalServerMessage("The ceiling is too close.");
                    return false;
                }
            }
        } else if (performer.isOnSurface() && !Terraforming.isBuildTile(Tiles.decodeType(tile))) {
            performer.getCommunicator().sendSafeServerMessage("You cannot build here, you need to prepare the ground first.", (byte)3);
            return false;
        }
        if (!MethodsStructure.hasEnoughSkillToExpandStructure(performer, tilex, tiley, structureToExpand)) {
            return true;
        }
        if (!MethodsStructure.mayModifyStructure(performer, structureToExpand, newTile, (short)116)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to expand " + structureToExpand.getName() + ".");
            return true;
        }
        if (!MethodsStructure.tileisNextToStructure(structureToExpand.getStructureTiles(), tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("That is not adjacent to any building.");
            return true;
        }
        try {
            Structure.expandStructureToTile(structureToExpand, newTile);
            structureToExpand.addNewBuildTile(newTile.tilex, newTile.tiley, newTile.getLayer());
            newTile.addBuildMarker(structureToExpand);
            structureToExpand.addMissingWallPlans(newTile);
            Structure.adjustWallsAroundAddedStructureTile(structureToExpand, tilex, tiley);
            newTile.addStructure(structureToExpand);
            structureToExpand.save();
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("A strong wind blows your markers away.");
            return true;
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Exception when trying to save structure after expansion: ", e);
        }
        structureToExpand.updateStructureFinishFlag();
        performer.getCommunicator().sendNormalServerMessage("You successfully expand your building.");
        return true;
    }

    private static boolean hasEnoughSkillToExpandStructure(Creature performer, int tilex, int tiley, Structure plannedStructure) {
        Skill carpentry = null;
        try {
            carpentry = performer.getSkills().getSkill(1005);
        }
        catch (NoSuchSkillException nss) {
            performer.getCommunicator().sendNormalServerMessage("You have no idea of how you would build a house.");
            return false;
        }
        if (carpentry == null) {
            performer.getCommunicator().sendNormalServerMessage("You have no idea of how you would build a house.");
            return false;
        }
        int limit = 5;
        limit = plannedStructure != null ? plannedStructure.getLimitFor(tilex, tiley, performer.isOnSurface(), true) : 5;
        if (limit == 0) {
            performer.getCommunicator().sendNormalServerMessage("The house seems to have no walls. Please replan.");
            return false;
        }
        if ((double)limit > carpentry.getKnowledge(0.0)) {
            performer.getCommunicator().sendNormalServerMessage("You are not skilled enough in Carpentry to build this size of structure.");
            return false;
        }
        return true;
    }

    private static boolean hasEnoughSkillToContractStructure(Creature performer, int tilex, int tiley, Structure plannedStructure) {
        Skill carpentry = null;
        try {
            carpentry = performer.getSkills().getSkill(1005);
        }
        catch (NoSuchSkillException nss) {
            performer.getCommunicator().sendNormalServerMessage("You have no idea of how you would modify a house.");
            return false;
        }
        if (carpentry == null) {
            performer.getCommunicator().sendNormalServerMessage("You have no idea of how you would modify a house.");
            return false;
        }
        int limit = 5;
        limit = plannedStructure.getSize() > 1 ? plannedStructure.getLimitFor(tilex, tiley, performer.isOnSurface(), false) : 5;
        if (limit == 0) {
            performer.getCommunicator().sendNormalServerMessage("The house seems to have no walls. Please replan.");
            return false;
        }
        if ((double)limit > carpentry.getKnowledge(0.0)) {
            performer.getCommunicator().sendNormalServerMessage("You are not skilled enough in Carpentry to modify this structure in that way.");
            return false;
        }
        return true;
    }

    static boolean buildPlan(Creature performer, Item tool, int tilex, int tiley, int tile, float counter) {
        boolean done = true;
        Structure planningStructure = null;
        if (!MethodsStructure.canPlanStructureAt(performer, tool, tilex, tiley, tile)) {
            return true;
        }
        try {
            planningStructure = performer.getStructure();
        }
        catch (NoSuchStructureException noSuchStructureException) {
            // empty catch block
        }
        if (planningStructure != null && (planningStructure.isFinalFinished() || System.currentTimeMillis() - planningStructure.getCreationDate() > 345600000L)) {
            performer.setStructure(null);
            logger.log(Level.INFO, performer.getName() + " just made another structure possible.");
            planningStructure = null;
        }
        if (MethodsStructure.getStructureAt(tilex, tiley, performer.isOnSurface()) != null) {
            performer.getCommunicator().sendNormalServerMessage("You cannot build a building inside a building.");
            return true;
        }
        if (planningStructure == null) {
            List<Structure> nearStructures = MethodsStructure.getStructuresNear(tilex, tiley, performer.isOnSurface());
            if (!nearStructures.isEmpty()) {
                performer.getCommunicator().sendNormalServerMessage("You cannot build a building next to another building.");
                return true;
            }
        } else if (MethodsStructure.hasOtherStructureNear(planningStructure, tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("You cannot build a building next to another building.");
            return true;
        }
        VolaTile t = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
        if (planningStructure == null) {
            if (!MethodsStructure.hasEnoughSkillToExpandStructure(performer, tilex, tiley, planningStructure)) {
                return true;
            }
            performer.addStructureTile(t, (byte)0);
            if (t.getVillage() == null) {
                if (t.getKingdom() == 0) {
                    performer.getCommunicator().sendAlertServerMessage("You are planning a structure outside a kingdom, and in no village. This is extremely risky, and the structure will probably be pillaged and looted by other players.");
                } else {
                    performer.getCommunicator().sendAlertServerMessage("You are planning a structure outside any known village. This is very risky, and the structure may very well be pillaged and looted by other players.");
                }
            }
            return true;
        }
        if (planningStructure.isTypeBridge()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot design a house as your mind keeps reverting back to the bridge \"" + planningStructure.getName() + "\" that you are currently constructing.");
            return true;
        }
        if (MethodsStructure.hasOtherStructureNear(planningStructure, tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("You need space to build the walls. Another building is too close.");
            return true;
        }
        if (MethodsStructure.wouldBuildOnStructure(planningStructure, tilex, tiley, performer.isOnSurface())) {
            performer.getCommunicator().sendNormalServerMessage("There is already a building there.");
            return true;
        }
        if (planningStructure.contains(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("You already plan to build there.");
            return true;
        }
        if (!MethodsStructure.tileisNextToStructure(planningStructure.getStructureTiles(), tilex, tiley)) {
            if (planningStructure.isFinalized()) {
                performer.getCommunicator().sendNormalServerMessage("You cannot design a new house as your mind keeps reverting back to the house \"" + planningStructure.getName() + "\" that you are currently constructing.");
            } else {
                performer.getCommunicator().sendNormalServerMessage("You cannot design a new house as your mind keeps reverting back to the house that you are currently in the process of planning.");
            }
            return true;
        }
        if (planningStructure.isFinalized()) {
            performer.getCommunicator().sendNormalServerMessage("Your current planning phase is already complete, use \"Add to building\" to expand it.");
            return true;
        }
        if (!MethodsStructure.hasEnoughSkillToExpandStructure(performer, tilex, tiley, planningStructure)) {
            return true;
        }
        performer.addStructureTile(t, (byte)0);
        return true;
    }

    private static final boolean wouldBuildOnOutsideItem(int tilex, int tiley, boolean surfaced) {
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, surfaced);
        if (tile == null) {
            return false;
        }
        Item[] items = tile.getItems();
        for (int x = 0; x < items.length; ++x) {
            if (!items[x].isOutsideOnly()) continue;
            return true;
        }
        return false;
    }

    static boolean buildPlanRemove(Creature performer, int tilex, int tiley, int tile, float counter) {
        boolean done = true;
        try {
            Structure structure = performer.getStructure();
            if (structure.isFinalized()) {
                return true;
            }
            if (structure.getWurmId() != performer.getBuildingId()) {
                performer.getCommunicator().sendNormalServerMessage("You are not planning this house.");
            } else if (structure.contains(tilex, tiley)) {
                if (!MethodsStructure.hasEnoughSkillToContractStructure(performer, tilex, tiley, structure)) {
                    return true;
                }
                if (!structure.removeTileFromPlannedStructure(performer, tilex, tiley)) {
                    performer.getCommunicator().sendNormalServerMessage("You can't divide the house in different parts.");
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You don't even plan to build there!");
            }
        }
        catch (NoSuchStructureException nss) {
            performer.getCommunicator().sendNormalServerMessage("You don't even plan to build there!");
        }
        return true;
    }

    public static VolaTile findFenceStart(VolaTile a, VolaTile b) {
        int tileax = a.getTileX();
        int tileay = a.getTileY();
        int tilebx = b.getTileX();
        int tileby = b.getTileY();
        if (tileay < tileby) {
            return a;
        }
        if (tileby < tileay) {
            return b;
        }
        if (tileax < tilebx) {
            return a;
        }
        return b;
    }

    public static Fence getFenceAtTileBorderOrNull(int tilex, int tiley, Tiles.TileBorderDirection dir, int heightOffset, boolean surfaced) {
        Fence[] fences;
        VolaTile tile = null;
        tile = Zones.getTileOrNull(tilex, tiley, surfaced);
        if (tile != null && (fences = tile.getFences()) != null) {
            for (Fence fence : fences) {
                if (fence.getDir() != dir || fence.getHeightOffset() != heightOffset) continue;
                return fence;
            }
        }
        return null;
    }

    public static final boolean doesTileBorderContainWallOrFence(int x, int y, int heightOffset, Tiles.TileBorderDirection dir, boolean surfaced, boolean ignoreArchs) {
        block8: {
            Wall[] walls;
            VolaTile tile;
            block7: {
                tile = null;
                tile = Zones.getTileOrNull(x, y, surfaced);
                if (tile != null) {
                    Fence[] fences = tile.getFences();
                    if (fences != null) {
                        for (Fence fence : fences) {
                            if (fence.getDir() != dir || fence.getHeightOffset() != heightOffset || fence.isOnSurface() != surfaced) continue;
                            return true;
                        }
                    }
                    Wall[] walls2 = tile.getWalls();
                    for (int s = 0; s < walls2.length; ++s) {
                        if (walls2[s].getStartX() != x || walls2[s].getStartY() != y || walls2[s].getHeight() != heightOffset || walls2[s].isOnSurface() != surfaced || (!walls2[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ) && (walls2[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN) || ignoreArchs && walls2[s].isArched() && walls2[s].isFinished()) continue;
                        return true;
                    }
                }
                if (dir != Tiles.TileBorderDirection.DIR_HORIZ) break block7;
                tile = Zones.getTileOrNull(x, y - 1, surfaced);
                if (tile == null) break block8;
                walls = tile.getWalls();
                for (int s = 0; s < walls.length; ++s) {
                    if (walls[s].getStartX() != x || walls[s].getStartY() != y || walls[s].getHeight() != heightOffset || walls[s].isOnSurface() != surfaced || (!walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ) && (walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN) || ignoreArchs && walls[s].isArched() && walls[s].isFinished()) continue;
                    return true;
                }
                break block8;
            }
            tile = Zones.getTileOrNull(x - 1, y, surfaced);
            if (tile != null) {
                walls = tile.getWalls();
                for (int s = 0; s < walls.length; ++s) {
                    if (walls[s].getStartX() != x || walls[s].getStartY() != y || walls[s].isOnSurface() != surfaced || (!walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ || walls[s].getHeight() != heightOffset) && (walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN || walls[s].getHeight() != heightOffset) || ignoreArchs && walls[s].isArched() && walls[s].isFinished()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static final boolean doesTileBorderContainUnfinishedWallOrFenceBelow(int x, int y, int heightOffset, Tiles.TileBorderDirection dir, boolean surfaced, boolean ignoreArchs) {
        block8: {
            Wall[] walls;
            VolaTile tile;
            block7: {
                tile = null;
                tile = Zones.getTileOrNull(x, y, surfaced);
                if (tile != null) {
                    Fence[] fences = tile.getFences();
                    if (fences != null) {
                        for (Fence fence : fences) {
                            if (fence.getDir() != dir || fence.getHeightOffset() != heightOffset - 30 || fence.isFinished()) continue;
                            return true;
                        }
                    }
                    Wall[] walls2 = tile.getWalls();
                    for (int s = 0; s < walls2.length; ++s) {
                        if (walls2[s].getStartX() != x || walls2[s].getStartY() != y || walls2[s].getHeight() != heightOffset - 30 || (!walls2[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ) && (walls2[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN) || walls2[s].isFinished()) continue;
                        return true;
                    }
                }
                if (dir != Tiles.TileBorderDirection.DIR_HORIZ) break block7;
                tile = Zones.getTileOrNull(x, y - 1, surfaced);
                if (tile == null) break block8;
                walls = tile.getWalls();
                for (int s = 0; s < walls.length; ++s) {
                    if (walls[s].getStartX() != x || walls[s].getStartY() != y || walls[s].getHeight() != heightOffset - 30 || (!walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ) && (walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN) || walls[s].isFinished()) continue;
                    return true;
                }
                break block8;
            }
            tile = Zones.getTileOrNull(x - 1, y, surfaced);
            if (tile != null) {
                walls = tile.getWalls();
                for (int s = 0; s < walls.length; ++s) {
                    if (walls[s].getStartX() != x || walls[s].getStartY() != y || (!walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ || walls[s].getHeight() != heightOffset - 30) && (walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN || walls[s].getHeight() != heightOffset - 30) || walls[s].isFinished()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static final boolean tileCornerBordersToCaveExit(int tilex, int tiley) {
        for (int x = -1; x <= 0; ++x) {
            for (int y = -1; y <= 0; ++y) {
                if (Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX(tilex + x), Zones.safeTileY(tiley + y))) != Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isFirstFenceTileOk(Creature performer, VolaTile vtile, int tile, int tilex, int tiley, int heightOffset, boolean horizontal) {
        int maxwest;
        int maxeast;
        int maxsouth;
        int maxnorth;
        if (vtile == null) {
            performer.getCommunicator().sendAlertServerMessage("You cannot place a fence there.");
            return false;
        }
        MeshIO mesh = performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh;
        int x = 0;
        int y = 0;
        if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
            performer.getCommunicator().sendAlertServerMessage("The water is too deep.");
            return false;
        }
        if (Math.abs(performer.getPosX() - (float)(tilex + x << 2)) > 4.0f || Math.abs(performer.getPosX() - (float)(tilex << 2)) > 4.0f || Math.abs(performer.getPosY() - (float)(tiley + y << 2)) > 4.0f || Math.abs(performer.getPosY() - (float)(tiley << 2)) > 4.0f) {
            performer.getCommunicator().sendAlertServerMessage("You are too far from the end.");
            return false;
        }
        if (Terraforming.isTileModBlocked(performer, tilex, tiley, performer.isOnSurface())) {
            return false;
        }
        if (horizontal) {
            x = 1;
        } else {
            y = 1;
        }
        if (tilex + x < 0 || tilex + x > 1 << Constants.meshSize || tiley + y < 0 || tiley + y > 1 << Constants.meshSize) {
            performer.getCommunicator().sendAlertServerMessage("The water is too deep.");
            return false;
        }
        if (Math.abs(performer.getPosX() - (float)(tilex + x << 2)) > 4.0f || Math.abs(performer.getPosX() - (float)(tilex << 2)) > 4.0f || Math.abs(performer.getPosY() - (float)(tiley + y << 2)) > 4.0f || Math.abs(performer.getPosY() - (float)(tiley << 2)) > 4.0f) {
            performer.getCommunicator().sendAlertServerMessage("You are too far from the end.");
            return false;
        }
        if (Servers.localServer.entryServer && MethodsStructure.tileCornerBordersToCaveExit(tilex + x, tiley + y)) {
            performer.getCommunicator().sendAlertServerMessage("Regulations in these lands require you to build further from the cave entrance. Use a mine door to protect it instead.");
            return false;
        }
        short h = Tiles.decodeHeight(mesh.getTile(tilex, tiley));
        if (h <= -15) {
            performer.getCommunicator().sendAlertServerMessage("The water is too deep.");
            return false;
        }
        h = Tiles.decodeHeight(mesh.getTile(tilex + x, tiley + y));
        if (h <= -15) {
            performer.getCommunicator().sendAlertServerMessage("The water is too deep.");
            return false;
        }
        Fence[] fences = vtile.getFencesForLevel(Math.max(0, heightOffset / 30));
        if (fences.length > 1 && fences[0] != null && fences[1] != null) {
            performer.getCommunicator().sendAlertServerMessage("You cannot place a fence there. Fences already exist.");
            return false;
        }
        EndGameItem alt = EndGameItems.getEvilAltar();
        if (alt != null) {
            maxnorth = Math.max(0, tiley - 20);
            maxsouth = Math.min(Zones.worldTileSizeY, tiley + 20);
            maxeast = Math.max(0, tilex - 20);
            maxwest = Math.min(Zones.worldTileSizeX, tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                performer.getCommunicator().sendSafeServerMessage("You cannot place a fence here, since this is holy ground.");
                return false;
            }
        }
        if ((alt = EndGameItems.getGoodAltar()) != null) {
            maxnorth = Math.max(0, tiley - 20);
            maxsouth = Math.min(Zones.worldTileSizeY, tiley + 20);
            maxeast = Math.max(0, tilex - 20);
            maxwest = Math.min(Zones.worldTileSizeX, tilex + 20);
            if (alt.getItem() != null && (int)alt.getItem().getPosX() >> 2 < maxwest && (int)alt.getItem().getPosX() >> 2 > maxeast && (int)alt.getItem().getPosY() >> 2 < maxsouth && (int)alt.getItem().getPosY() >> 2 > maxnorth) {
                performer.getCommunicator().sendSafeServerMessage("You cannot place a fence here, since this is holy ground.");
                return false;
            }
        }
        return true;
    }

    static boolean startFenceSection(Creature performer, Item source, Tiles.TileBorderDirection dir, int tilex, int tiley, boolean onSurface, int heightOffset, long borderId, int action, float counter, boolean instaFinish) {
        VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, onSurface);
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(tilex, tiley, dir, onSurface);
        if (heightOffset > 0 && structure == null) {
            performer.getCommunicator().sendNormalServerMessage("The structural integrity of the building is at risk.");
            logger.log(Level.WARNING, "Structure not found while trying to add a wall at [" + tilex + "," + tiley + "]");
            return true;
        }
        MeshIO mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
        int tile = mesh.getTile(tilex, tiley);
        if (!MethodsStructure.isFirstFenceTileOk(performer, vtile, tile, tilex, tiley, heightOffset, dir == Tiles.TileBorderDirection.DIR_HORIZ)) {
            return true;
        }
        boolean horizontal = dir == Tiles.TileBorderDirection.DIR_HORIZ;
        try {
            Zone zone = Zones.getZone(tilex, tiley, onSurface);
            DbFence fence = new DbFence(Fence.getFencePlanType(action), tilex, tiley, heightOffset, 1.0f, dir, zone.getId(), onSurface ? 0 : -1);
            Skill primskill = null;
            primskill = Fence.getSkillNeededForFence(performer, fence);
            if (primskill == null) {
                performer.getCommunicator().sendNormalServerMessage("Failed to locate skill needed for this " + fence.getName() + ". You cannot progress.");
                logger.log(Level.WARNING, "Failed to find out what skill was needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
                return true;
            }
            double knowledge = primskill.getKnowledge(0.0);
            short height = Tiles.decodeHeight(tile);
            int nexttile = mesh.getTile(tilex + 1, tiley);
            if (!horizontal) {
                nexttile = mesh.getTile(tilex, tiley + 1);
            }
            VolaTile lastT = Zones.getTileOrNull(tilex - 1, tiley, onSurface);
            int lasttile = mesh.getTile(tilex - 1, tiley);
            if (horizontal) {
                lastT = Zones.getTileOrNull(tilex, tiley - 1, onSurface);
                lasttile = mesh.getTile(tilex, tiley - 1);
            }
            if (!fence.isOnPvPServer() && performer.getPower() < 2 && vtile.getVillage() == null && (lastT == null || lastT.getVillage() == null) && Tiles.isRoadType(lasttile) && Tiles.isRoadType(tile) && heightOffset == 0) {
                performer.getCommunicator().sendNormalServerMessage("You are not allowed to build fences on roads outside of settlements.");
                return true;
            }
            if (fence.getType() != StructureConstantsEnum.FENCE_PLAN_STONEWALL || Tiles.decodeType(tile) != Tiles.Tile.TILE_HOLE.id && Tiles.decodeType(lasttile) != Tiles.Tile.TILE_HOLE.id) {
                short secondHeight = Tiles.decodeHeight(nexttile);
                if (fence.getType() != StructureConstantsEnum.FENCE_PLAN_PORTCULLIS) {
                    if ((double)Math.abs(secondHeight - height) > Math.sqrt(knowledge) * 3.0 + 10.0) {
                        performer.getCommunicator().sendAlertServerMessage("You are not skilled enough to build in such steep slopes.");
                        return true;
                    }
                } else {
                    int maxSlope = 10;
                    int slope = Math.abs(secondHeight - height);
                    if (slope > 10) {
                        String message = StringUtil.format("You are not allowed to build this type of fence in a slope of: %d. The slope must be %d or less.", slope, 10);
                        performer.getCommunicator().sendAlertServerMessage(message);
                        return true;
                    }
                }
            }
            if (structure != null && (double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(heightOffset / 30, false) > knowledge) {
                performer.getCommunicator().sendAlertServerMessage("You are not skilled enough to build at this height.");
                return true;
            }
            try {
                if (MethodsStructure.buildFirstFence(performer, source, fence, counter, action, instaFinish)) {
                    zone.addFence(fence);
                    if (instaFinish) {
                        if (fence.isDoor()) {
                            DbFenceGate gate = new DbFenceGate(fence);
                            gate.addToTiles();
                            vtile = gate.getInnerTile();
                            Village village = vtile.getVillage();
                            if (village != null) {
                                village.addGate(gate);
                            } else {
                                vtile = gate.getOuterTile();
                                village = vtile.getVillage();
                                if (village != null) {
                                    village.addGate(gate);
                                }
                            }
                        }
                    } else {
                        performer.getCommunicator().sendAddFenceToCreationWindow(fence, borderId);
                    }
                    performer.getCommunicator().sendActionResult(true);
                    return true;
                }
            }
            catch (FailedException fe) {
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            return false;
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.INFO, performer.getName() + ": " + nsz.getMessage(), nsz);
            return true;
        }
    }

    static void instaDestroyFence(Creature performer, Fence fence) {
        try {
            Zone zone = Zones.getZone(fence.getZoneId());
            zone.removeFence(fence);
            fence.delete();
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "Fence in nonexistant zone? " + performer.getName() + " " + nsz.getMessage(), nsz);
        }
    }

    static boolean removeFencePlan(Creature performer, Item source, Fence fence, float counter, int action, Action act) {
        Structure struct;
        VolaTile tile;
        boolean toReturn = false;
        int time = 300;
        if (fence.isFinished()) {
            return true;
        }
        if (counter == 1.0f) {
            if (fence.getDamageModifierForItem(source, false) <= 0.0f) {
                performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the " + fence.getName() + " with that.");
                return true;
            }
            tile = Zones.getTileOrNull(fence.getTileX(), fence.getTileY(), fence.isOnSurface());
            if (tile != null && (struct = tile.getStructure()) != null && struct.wouldCreateFlyingStructureIfRemoved(fence)) {
                performer.getCommunicator().sendNormalServerMessage("Removing that would cause a collapsing section.");
                return true;
            }
            try {
                performer.getCurrentAction().setTimeLeft(time);
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
            performer.getCommunicator().sendNormalServerMessage("You start to remove the " + fence.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to remove a " + fence.getName() + ".", performer, 5);
            performer.sendActionControl("Removing " + fence.getName(), true, time);
        } else {
            try {
                time = performer.getCurrentAction().getTimeLeft();
                if (act.currentSecond() % 5 == 0) {
                    MethodsStructure.sendDestroySound(performer, source, !fence.isStone() && !fence.isIron());
                    performer.getStatus().modifyStamina(-5000.0f);
                    if (!(source == null || source.isBodyPart() && source.getAuxData() != 100)) {
                        source.setDamage(source.getDamage() + fence.getDamageModifierForItem(source, false) * source.getDamageModifier());
                    }
                }
            }
            catch (NoSuchActionException nsa) {
                logger.log(Level.INFO, "This action does not exist?", nsa);
            }
        }
        if (counter * 10.0f > (float)time) {
            tile = Zones.getTileOrNull(fence.getTileX(), fence.getTileY(), fence.isOnSurface());
            if (tile != null && (struct = tile.getStructure()) != null && struct.wouldCreateFlyingStructureIfRemoved(fence)) {
                performer.getCommunicator().sendNormalServerMessage("Removing that would cause a collapsing section.");
                return true;
            }
            toReturn = true;
            try {
                Zone zone = Zones.getZone(fence.getZoneId());
                zone.removeFence(fence);
                fence.delete();
            }
            catch (NoSuchZoneException nsz) {
                logger.log(Level.WARNING, "Fence in nonexistant zone? " + performer.getName() + " " + nsz.getMessage(), nsz);
            }
        }
        return toReturn;
    }

    private static boolean buildFirstFence(Creature performer, Item source, Fence fence, float counter, int action, boolean instaFinish) throws FailedException {
        Item check;
        ItemTemplate template;
        boolean toReturn = false;
        Skill primskill = null;
        primskill = Fence.getSkillNeededForFence(performer, fence);
        if (primskill == null) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate skill needed for this " + fence.getName() + ". You cannot progress.");
            logger.log(Level.WARNING, "Failed to find out what skill was needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
            throw new FailedException("Failed to locate skill needed for this " + fence.getName());
        }
        if (fence.getLayer() != performer.getLayer()) {
            performer.getCommunicator().sendNormalServerMessage("You would not be able to reach the " + fence.getName() + ".");
            throw new FailedException("You would not be able to reach the " + fence.getName() + ".");
        }
        Skill hammer = null;
        int[] tNeeded = null;
        int time = 100;
        double bonus = 0.0;
        Action act = null;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.INFO, performer.getName() + " - this action does not exist?", nsa);
            return true;
        }
        if (!instaFinish) {
            if (counter == 1.0f) {
                tNeeded = Fence.getItemTemplatesNeededForFence(fence);
                if (tNeeded == null) {
                    performer.getCommunicator().sendNormalServerMessage("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
                    logger.log(Level.WARNING, "Failed to find out what items were needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
                    throw new FailedException("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
                }
                for (int x = 0; x < tNeeded.length; ++x) {
                    if (tNeeded[x] != -1) {
                        if (instaFinish) continue;
                        try {
                            template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
                            check = MethodsStructure.creatureHasItem(template, performer, false);
                            if (check == null) {
                                performer.getCommunicator().sendNormalServerMessage("You need at least " + template.getNameWithGenus() + " to continue this " + fence.getName() + ".");
                                throw new FailedException("You need at least " + template.getNameWithGenus() + " to continue this " + fence.getName() + ".");
                            }
                            if (template.getTemplateId() == 385) {
                                if (!((double)check.getWeightGrams() < (double)template.getWeightGrams() * 0.5)) continue;
                                performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                                throw new FailedException("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName());
                            }
                            if (check.getWeightGrams() >= template.getWeightGrams()) continue;
                            performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                            throw new FailedException("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName());
                        }
                        catch (NoSuchTemplateException nst) {
                            performer.getCommunicator().sendNormalServerMessage("You can't figure out what is needed to continue this " + fence.getName() + ".");
                            logger.log(Level.WARNING, "Failed to find out what items were needed for fence at :" + fence.getTileX() + ", " + fence.getTileY() + " Template was: " + tNeeded[x]);
                            throw new FailedException("You can't figure out what is needed to continue this " + fence.getName() + ".");
                        }
                    }
                    performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is already finished.");
                    throw new FailedException("The " + fence.getName() + " is already finished.");
                }
                time = Actions.getStandardActionTime(performer, primskill, source, 0.0);
                act.setTimeLeft(time);
                performer.getCommunicator().sendNormalServerMessage("You start to build a " + fence.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to build a " + fence.getName() + ".", performer, 5);
                performer.sendActionControl("Building " + fence.getName(), true, time);
                performer.getStatus().modifyStamina(-1500.0f);
                if (source.getTemplateId() == 63) {
                    source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                } else if (source.getTemplateId() == 62) {
                    source.setDamage(source.getDamage() + 1.0E-4f * source.getDamageModifier());
                }
            } else {
                time = act.getTimeLeft();
                if (act.currentSecond() % 5 == 0) {
                    performer.getStatus().modifyStamina(-10000.0f);
                    if (source.getTemplateId() == 63) {
                        source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                    } else if (source.getTemplateId() == 62) {
                        source.setDamage(source.getDamage() + 1.0E-4f * source.getDamageModifier());
                    }
                }
                if (act.mayPlaySound()) {
                    String s;
                    String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
                    if (fence.isStone()) {
                        s = "sound.work.masonry";
                    }
                    SoundPlayer.playSound(s, performer, 1.0f);
                }
            }
        }
        if (counter * 10.0f > (float)time || instaFinish) {
            tNeeded = Fence.getItemTemplatesNeededForFence(fence);
            if (tNeeded == null) {
                performer.getCommunicator().sendNormalServerMessage("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
                logger.log(Level.WARNING, "Failed to find out what items were needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
                throw new FailedException("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
            }
            for (int x = 0; x < tNeeded.length; ++x) {
                if (tNeeded[x] != -1) {
                    try {
                        template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
                        check = MethodsStructure.creatureHasItem(template, performer, false);
                        if (instaFinish) {
                            performer.getCommunicator().sendNormalServerMessage("You conjure up " + template.getNameWithGenus() + " to build that " + fence.getName() + ".");
                        } else {
                            if (check == null) {
                                performer.getCommunicator().sendNormalServerMessage("You need " + template.getNameWithGenus() + " to build that " + fence.getName() + ".");
                                throw new FailedException("You need " + template.getNameWithGenus() + " to build that " + fence.getName() + ".");
                            }
                            if (template.getTemplateId() == 385) {
                                if ((double)check.getWeightGrams() < (double)template.getWeightGrams() * 0.5) {
                                    performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to build that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                                    throw new FailedException("The " + check.getName() + " you try to use contains too little material to build that " + fence.getName());
                                }
                                Items.destroyItem(check.getWurmId());
                            } else {
                                if (check.getWeightGrams() < template.getWeightGrams()) {
                                    performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to build that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                                    throw new FailedException("The " + check.getName() + " you try to use contains too little material to build that " + fence.getName());
                                }
                                check.setWeight(check.getWeightGrams() - template.getWeightGrams(), true);
                            }
                        }
                        if (instaFinish) continue;
                        act.setPower(check.getCurrentQualityLevel());
                        continue;
                    }
                    catch (NoSuchTemplateException nst) {
                        performer.getCommunicator().sendNormalServerMessage("You can't figure out what is needed to build that " + fence.getName() + ".");
                        logger.log(Level.WARNING, "Failed to find out what items were needed for fence at :" + fence.getTileX() + ", " + fence.getTileY() + " Template was: " + tNeeded[x]);
                        throw new FailedException("You can't figure out what is needed to build that " + fence.getName() + ".");
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is already finished.");
                throw new FailedException("The " + fence.getName() + " is already finished.");
            }
            try {
                hammer = performer.getSkills().getSkill(source.getPrimarySkill());
            }
            catch (NoSuchSkillException nss) {
                try {
                    performer.getSkills().learn(source.getPrimarySkill(), 1.0f);
                }
                catch (NoSuchSkillException noSuchSkillException) {
                    // empty catch block
                }
            }
            if (hammer != null) {
                hammer.skillCheck(10.0, source, 0.0, false, counter);
                bonus = hammer.getKnowledge(source, 0.0) / 10.0;
            }
            primskill.skillCheck(10.0, source, bonus, false, counter);
            double power = (double)act.getPower() / (double)fence.getFinishState().state;
            power = Math.min((double)fence.getQualityLevel() + power, primskill.getKnowledge(0.0));
            if (instaFinish) {
                power = Math.min(100.0, (double)source.getAuxData());
                fence.setState(fence.getFinishState());
                fence.setType(Fence.getFenceForPlan(fence.getType()));
            } else {
                fence.setState(StructureStateEnum.INITIALIZED);
            }
            fence.setQualityLevel((float)power);
            fence.improveOrigQualityLevel((float)power);
            try {
                fence.save();
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to save fence " + fence.getTileX() + ", " + fence.getTileY() + ", " + performer.getName() + ":" + iox.getMessage(), iox);
            }
            if (!instaFinish) {
                performer.getCommunicator().sendNormalServerMessage("You lay the foundation to the " + fence.getName() + ".");
            }
            return true;
        }
        return false;
    }

    public static Item creatureHasItem(ItemTemplate template, Creature performer, boolean checkWeight) {
        int i;
        Item[] items = performer.getInventory().getAllItems(false);
        for (i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != template.getTemplateId() || items[i].getWeightGrams() < template.getWeightGrams() && checkWeight) continue;
            return items[i];
        }
        items = performer.getBody().getBodyItem().getAllItems(false);
        for (i = 0; i < items.length; ++i) {
            if (items[i].getTemplateId() != template.getTemplateId() || items[i].getWeightGrams() < template.getWeightGrams() && checkWeight) continue;
            return items[i];
        }
        return null;
    }

    static boolean continueFence(Creature performer, Fence fence, Item source, float counter, int action, Action act) {
        boolean toReturn = false;
        Skill primskill = Fence.getSkillNeededForFence(performer, fence);
        if (primskill == null) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate skill needed for this " + fence.getName() + ". You cannot progress.");
            logger.log(Level.WARNING, "Failed to find out what skill was needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
            performer.getCommunicator().sendActionResult(false);
            return true;
        }
        if (fence.isFinished()) {
            return true;
        }
        if (fence.getLayer() != performer.getLayer()) {
            performer.getCommunicator().sendNormalServerMessage("You would not be able to reach the " + fence.getName() + ".");
            return true;
        }
        if (performer.isFighting()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
            return true;
        }
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(fence.getTileX(), fence.getTileY(), fence.getDir(), performer.isOnSurface());
        if (structure != null && (double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(fence.getFloorLevel(), false) > primskill.getKnowledge(0.0)) {
            performer.getCommunicator().sendAlertServerMessage("You are not skilled enough to build at this height.", (byte)3);
            performer.getCommunicator().sendActionResult(false);
            return true;
        }
        Skill hammer = null;
        int[] tNeeded = null;
        int time = 100;
        double bonus = 0.0;
        StructureStateEnum state = fence.getState();
        boolean mayFinish = true;
        if (counter == 1.0f) {
            tNeeded = Fence.getItemTemplatesNeededForFence(fence);
            if (tNeeded == null) {
                performer.getCommunicator().sendNormalServerMessage("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
                logger.log(Level.WARNING, "Failed to find out what items were needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            for (int x = 0; x < tNeeded.length; ++x) {
                if (tNeeded[x] != -1) {
                    try {
                        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
                        Item check = MethodsStructure.creatureHasItem(template, performer, false);
                        if (check == null) {
                            performer.getCommunicator().sendNormalServerMessage("You need " + template.getNameWithGenus() + " to continue that " + fence.getName() + ".");
                            performer.getCommunicator().sendActionResult(false);
                            return true;
                        }
                        if (template.getTemplateId() == 385) {
                            if (!((double)check.getWeightGrams() < (double)template.getWeightGrams() * 0.5)) continue;
                            performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                            performer.getCommunicator().sendActionResult(false);
                            return true;
                        }
                        if (check.getWeightGrams() >= template.getWeightGrams()) continue;
                        performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                    catch (NoSuchTemplateException nst) {
                        performer.getCommunicator().sendNormalServerMessage("You can't figure out what is needed to continue this " + fence.getName() + ".");
                        logger.log(Level.WARNING, "Failed to find out what items were needed for fence at :" + fence.getTileX() + ", " + fence.getTileY() + " Template was: " + tNeeded[x]);
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is already finished.");
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            time = Actions.getStandardActionTime(performer, primskill, source, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You continue to build a " + fence.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " continues to build a " + fence.getName() + ".", performer, 5);
            performer.sendActionControl("Continuing " + fence.getName(), true, time);
            performer.getStatus().modifyStamina(-1500.0f);
            if (source.getTemplateId() == 63) {
                source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
            } else if (source.getTemplateId() == 62) {
                source.setDamage(source.getDamage() + 3.0E-4f * source.getDamageModifier());
            }
            if (performer.getDeity() != null && performer.getDeity().number == 3) {
                performer.maybeModifyAlignment(0.5f);
            }
        } else {
            time = act.getTimeLeft();
            if (state.state >= fence.getFinishState().state) {
                performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is already finished.");
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            if (Math.abs(performer.getPosX() - (float)(fence.getEndX() << 2)) > 8.0f || Math.abs(performer.getPosX() - (float)(fence.getStartX() << 2)) > 8.0f || Math.abs(performer.getPosY() - (float)(fence.getEndY() << 2)) > 8.0f || Math.abs(performer.getPosY() - (float)(fence.getStartY() << 2)) > 8.0f) {
                performer.getCommunicator().sendAlertServerMessage("You are too far from the end.");
                return true;
            }
            if (state.state == fence.getFinishState().state - 1) {
                float tx;
                float posx = performer.getStatus().getPositionX();
                float posy = performer.getStatus().getPositionY();
                int tilex = (int)posx >> 2;
                int tiley = (int)posy >> 2;
                if (fence.isHorizontal()) {
                    float ty;
                    if (tilex == fence.getTileX() && posy > (ty = (float)(fence.getTileY() << 2)) - 2.0f && posy < ty + 2.0f) {
                        mayFinish = false;
                    }
                } else if (tiley == fence.getTileY() && posx > (tx = (float)(fence.getTileX() << 2)) - 2.0f && posx < tx + 2.0f) {
                    mayFinish = false;
                }
            }
            if (act.currentSecond() % 5 == 0) {
                performer.getStatus().modifyStamina(-10000.0f);
                if (source.getTemplateId() == 63) {
                    source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                } else if (source.getTemplateId() == 62) {
                    source.setDamage(source.getDamage() + 3.0E-4f * source.getDamageModifier());
                }
                if (!mayFinish) {
                    performer.getCommunicator().sendNormalServerMessage("You are standing in the way of finishing this wall or fence. You have to move to the side.");
                    performer.getActions().clear();
                    return true;
                }
            }
            if (act.mayPlaySound()) {
                String s;
                String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
                if (fence.isStone()) {
                    s = "sound.work.masonry";
                }
                SoundPlayer.playSound(s, performer, 1.0f);
            }
        }
        if (counter * 10.0f > (float)time && mayFinish) {
            double power;
            toReturn = true;
            tNeeded = Fence.getItemTemplatesNeededForFence(fence);
            Item check = null;
            if (Math.abs(performer.getPosX() - (float)(fence.getEndX() << 2)) > 8.0f || Math.abs(performer.getPosX() - (float)(fence.getStartX() << 2)) > 8.0f || Math.abs(performer.getPosY() - (float)(fence.getEndY() << 2)) > 8.0f || Math.abs(performer.getPosY() - (float)(fence.getStartY() << 2)) > 8.0f) {
                performer.getCommunicator().sendAlertServerMessage("You are too far from the end.");
                return true;
            }
            if (tNeeded == null) {
                performer.getCommunicator().sendNormalServerMessage("You failed to figure out what is needed for this " + fence.getName() + ". You cannot progress.");
                logger.log(Level.WARNING, "Failed to find out what items were needed for " + fence.getName() + " at :" + fence.getTileX() + ", " + fence.getTileY());
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            HashSet<Item> destroyedItems = new HashSet<Item>();
            for (int x = 0; x < tNeeded.length; ++x) {
                check = null;
                if (tNeeded[x] != -1) {
                    try {
                        ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
                        check = MethodsStructure.creatureHasItem(template, performer, false);
                        if (check == null) {
                            performer.getCommunicator().sendNormalServerMessage("You need " + template.getNameWithGenus() + " to continue that " + fence.getName() + ".");
                            destroyedItems.clear();
                            performer.getCommunicator().sendActionResult(false);
                            return true;
                        }
                        if (template.getTemplateId() == 385) {
                            if ((double)check.getWeightGrams() < (double)template.getWeightGrams() * 0.5) {
                                performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to build that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                                destroyedItems.clear();
                                performer.getCommunicator().sendActionResult(false);
                                return true;
                            }
                            destroyedItems.add(check);
                        } else {
                            if (check.getWeightGrams() < template.getWeightGrams()) {
                                performer.getCommunicator().sendNormalServerMessage("The " + check.getName() + " you try to use contains too little material to continue that " + fence.getName() + ". Please drop it or combine it with another item of the same kind.");
                                destroyedItems.clear();
                                performer.getCommunicator().sendActionResult(false);
                                return true;
                            }
                            destroyedItems.add(check);
                        }
                        act.setPower(check.getCurrentQualityLevel());
                        continue;
                    }
                    catch (NoSuchTemplateException nst) {
                        performer.getCommunicator().sendNormalServerMessage("You can't figure out what is needed to continue this " + fence.getName() + ".");
                        logger.log(Level.WARNING, "Failed to find out what items were needed for fence at :" + fence.getTileX() + ", " + fence.getTileY() + " Template was: " + tNeeded[x]);
                        destroyedItems.clear();
                        performer.getCommunicator().sendActionResult(false);
                        return true;
                    }
                }
                performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is already finished.");
                destroyedItems.clear();
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
            try {
                hammer = performer.getSkills().getSkill(source.getPrimarySkill());
            }
            catch (NoSuchSkillException nss) {
                try {
                    hammer = performer.getSkills().learn(source.getPrimarySkill(), 1.0f);
                }
                catch (NoSuchSkillException noSuchSkillException) {
                    // empty catch block
                }
            }
            if (hammer != null) {
                hammer.skillCheck(fence.getDifficulty(), source, 0.0, false, counter);
                bonus = hammer.getKnowledge(source, 0.0) / 10.0;
            }
            if ((power = primskill.skillCheck(fence.getDifficulty(), source, bonus, false, counter)) > 0.0) {
                performer.getCommunicator().sendActionResult(true);
                Item[] destroyed = destroyedItems.toArray(new Item[destroyedItems.size()]);
                for (int x = 0; x < destroyed.length; ++x) {
                    if (destroyed[x].getTemplateId() == 385) {
                        Items.destroyItem(destroyed[x].getWurmId());
                        continue;
                    }
                    destroyed[x].setWeight(destroyed[x].getWeightGrams() - destroyed[x].getTemplate().getWeightGrams(), true);
                }
                destroyedItems.clear();
                power = (double)act.getPower() / (double)fence.getFinishState().state;
                double skilladdition = (double)fence.getQualityLevel() + primskill.getKnowledge(0.0) / (double)fence.getFinishState().state;
                fence.setQualityLevel((float)Math.min((double)fence.getQualityLevel() + power, skilladdition));
                fence.improveOrigQualityLevel(fence.getQualityLevel());
                if (fence.getState().state < fence.getFinishState().state) {
                    fence.setState(StructureStateEnum.getStateByValue((byte)(state.state + 1)));
                }
                try {
                    fence.save();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save fence " + fence.getTileX() + ", " + fence.getTileY() + ", " + performer.getName() + ":" + iox.getMessage(), iox);
                }
                if (fence.isFinished()) {
                    performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is finished now.");
                    TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), action);
                    VolaTile tile = Zones.getTileOrNull(fence.getTileX(), fence.getTileY(), fence.isOnSurface());
                    if (tile != null) {
                        fence.setType(Fence.getFenceForPlan(fence.getType()));
                        try {
                            fence.save();
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, "Failed to save fence " + fence.getTileX() + ", " + fence.getTileY() + ", " + performer.getName() + ":" + iox.getMessage(), iox);
                        }
                        tile.updateFence(fence);
                        if (fence.isDoor()) {
                            DbFenceGate gate = new DbFenceGate(fence);
                            gate.addToTiles();
                            VolaTile vtile = gate.getInnerTile();
                            Village village = vtile.getVillage();
                            if (village != null) {
                                village.addGate(gate);
                            } else {
                                vtile = gate.getOuterTile();
                                village = vtile.getVillage();
                                if (village != null) {
                                    village.addGate(gate);
                                }
                            }
                        }
                    } else {
                        logger.log(Level.WARNING, "Tried to finish and update fence on tile where no fence was located: " + fence.getTileX() + "," + fence.getTileY());
                    }
                    return true;
                }
                destroyedItems.clear();
                performer.getCommunicator().sendNormalServerMessage("You continue on the " + fence.getName() + ".");
            } else {
                destroyedItems.clear();
                performer.getCommunicator().sendNormalServerMessage("You fail to continue the " + fence.getName() + ".");
                performer.getCommunicator().sendActionResult(false);
                return true;
            }
        }
        return toReturn;
    }

    static boolean tileBordersToFence(int tilex, int tiley, int heightOffset, boolean surfaced) {
        VolaTile tile = Zones.getOrCreateTile(tilex, tiley, surfaced);
        for (Fence f : tile.getFences()) {
            if (f.getHeightOffset() != heightOffset) continue;
            return true;
        }
        tile = Zones.getTileOrNull(tilex, tiley + 1, surfaced);
        if (tile != null) {
            for (Fence f : tile.getFences()) {
                if (!f.isHorizontal() || f.getHeightOffset() != heightOffset) continue;
                return true;
            }
        }
        if ((tile = Zones.getTileOrNull(tilex + 1, tiley, surfaced)) != null) {
            for (Fence f : tile.getFences()) {
                if (f.isHorizontal() || f.getHeightOffset() != heightOffset) continue;
                return true;
            }
        }
        return false;
    }

    static final boolean repairFence(Action act, Creature performer, Item repairItem, Fence fence, float counter) {
        if (performer.isFighting()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
            return true;
        }
        if (fence.getLayer() != performer.getLayer()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot repair that, you are too far away.");
            return true;
        }
        if (fence.getDamage() == 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " does not need repairing.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, fence.getTileX(), fence.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 5;
        int templateId = fence.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot repair the " + fence.getName() + " with that item.");
            return true;
        }
        float f = repairItem.getWeightGrams();
        float f2 = repairItem.getTemplate().getWeightGrams();
        float f3 = repairItem.getTemplateId() == 9 ? 0.7f : 1.0f;
        if (f < f2 * f3) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to repair the " + fence.getName() + ".");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(1005);
        if (fence.isStone() || fence.isSlate() || fence.isMarble() || fence.isPlastered() || fence.isRoundedStone() || fence.isPottery() || fence.isSandstone() || fence.isIron()) {
            buildSkill = performer.getSkills().getSkillOrLearn(1013);
        }
        Skill repairSkill = performer.getSkills().getSkillOrLearn(10035);
        int time = 400;
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start to repair the " + fence.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to repair a " + fence.getName() + ".", performer, 5);
            time = Actions.getRepairActionTime(performer, repairSkill, buildSkill.getKnowledge(0.0));
            performer.sendActionControl(Actions.actionEntrys[193].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-500.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (buildSkill.getNumber() == 1013) {
                s = "sound.work.masonry";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                if (repairItem.getWeightGrams() <= 0) {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
            } else if (insta) {
                performer.sendToLoggers("Repairing fence with ID:" + fence.getId() + " located at " + fence.getTile().toString());
                fence.setDamage(0.0f);
                performer.getCommunicator().sendNormalServerMessage("You magically repair the " + fence.getName() + " fence (ID:" + fence.getId() + ") with your powers.");
            } else {
                repairItem.putInVoid();
                act.setDestroyedItem(repairItem);
            }
            double power = repairSkill.skillCheck(fence.getDamage() / 10.0f, repairItem, 0.0, false, counter);
            Item destroyed = act.getDestroyedItem();
            if (destroyed != null) {
                Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
            }
            act.setDestroyedItem(null);
            if (insta) {
                power = 100.0;
            }
            if (power > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You repair the " + fence.getName() + " a bit.");
                float cq = fence.getCurrentQualityLevel();
                float diffcq = fence.getQualityLevel() - cq;
                float newOrigcq = fence.getQualityLevel() - (float)((double)diffcq * (100.0 - power)) / 10000.0f;
                float repairAmnt = 5.0f + (float)(5.0 * (repairSkill.getKnowledge(buildSkill.getKnowledge(0.0)) + (double)repairItem.getCurrentQualityLevel()) / 200.0);
                fence.setQualityLevel(newOrigcq);
                fence.setDamage(Math.max(0.0f, fence.getDamage() - repairAmnt));
                performer.achievement(155);
            } else if (power < -90.0) {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the " + fence.getName() + " and damage it instead!");
                fence.setDamage((float)Math.min(100.0, (double)fence.getDamage() - power / 100.0));
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the " + fence.getName() + ".");
            }
            fence.setLastUsed(System.currentTimeMillis());
            Server.getInstance().broadCastAction(performer.getName() + " repairs a " + fence.getName() + " a bit.", performer, 5);
            return true;
        }
        return false;
    }

    static final boolean destroyFence(short action, Creature performer, Item destroyItem, Fence fence, boolean dealItems, float counter) {
        if (!Methods.isActionAllowed(performer, action, false, fence.getTileX(), fence.getTileY(), 0, 0)) {
            return true;
        }
        boolean toReturn = true;
        int time = 1000;
        boolean insta = performer.getPower() >= 2;
        float mod = fence.getDamageModifierForItem(destroyItem, true);
        if (Servers.localServer.entryServer && fence.getOriginalQualityLevel() > 99.0f && !Servers.localServer.testServer) {
            mod = 0.0f;
        }
        if (Servers.localServer.isChallengeOrEpicServer() && Zones.protectedTiles[fence.getTileX()][fence.getTileY()]) {
            mod = 0.0f;
        }
        if (mod <= 0.0f && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the " + fence.getName() + " with that.");
            return true;
        }
        toReturn = false;
        Action act = null;
        String destString = "destroy";
        if (dealItems) {
            destString = "disassemble";
        }
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "No Action for " + performer.getName() + "!", nsa);
            return true;
        }
        if (counter == 1.0f) {
            time = Servers.localServer.challengeServer ? (fence.getType() == StructureConstantsEnum.FENCE_STONEWALL_HIGH || fence.getType() == StructureConstantsEnum.FENCE_PORTCULLIS ? 150 : 100) : 300;
            performer.getCommunicator().sendNormalServerMessage("You start to " + destString + " the " + fence.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to " + destString + " a " + fence.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-500.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() % 5 == 0) {
            MethodsStructure.sendDestroySound(performer, destroyItem, !fence.isStone() && !fence.isIron());
            performer.getStatus().modifyStamina(-5000.0f);
            float toolMod = fence.getDamageModifierForItem(destroyItem, false);
            if (!(destroyItem == null || destroyItem.isBodyPart() && destroyItem.getAuxData() != 100)) {
                destroyItem.setDamage(destroyItem.getDamage() + toolMod * destroyItem.getDamageModifier());
            }
        }
        if (counter * 10.0f > (float)time || insta) {
            Skills skills = performer.getSkills();
            Skill destroySkill = null;
            try {
                destroySkill = skills.getSkill(102);
            }
            catch (NoSuchSkillException nss) {
                destroySkill = skills.learn(102, 1.0f);
            }
            destroySkill.skillCheck(20.0, destroyItem, 0.0, false, counter);
            double damage = 0.0;
            if (insta && mod <= 0.0f) {
                damage = 20.0;
                mod = 1.0f;
            } else {
                damage = Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 5.0;
                if (!Servers.localServer.challengeServer) {
                    damage /= (double)(fence.getQualityLevel() / 10.0f);
                } else {
                    float divider = 10.0f;
                    divider = fence.getType() == StructureConstantsEnum.FENCE_STONEWALL_HIGH || fence.getType() == StructureConstantsEnum.FENCE_PORTCULLIS ? (divider -= 1.0f) : (divider += 2.0f);
                    damage /= (double)(fence.getQualityLevel() / divider);
                }
            }
            Village vill = MethodsStructure.getVillageForFence(fence);
            boolean citizen = false;
            if (vill != null) {
                if (MethodsStructure.isCitizenAndMayPerformAction(action, performer, vill)) {
                    damage *= 50.0;
                    citizen = true;
                } else if (MethodsStructure.isAllyAndMayPerformAction(action, performer, vill)) {
                    damage *= 25.0;
                    citizen = true;
                }
            } else if (Zones.isInPvPZone(fence.getTileX(), fence.getTileY())) {
                damage *= 10.0;
            }
            if (!citizen) {
                damage = performer.getCultist() != null && performer.getCultist().doubleStructDamage() ? (damage *= 4.0) : (damage *= 2.0);
            }
            damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
            if (!fence.isFinished()) {
                int modifier = fence.getFinishState().state - fence.getState().state;
                damage *= (double)modifier;
            }
            float newDam = (float)((double)fence.getDamage() + damage * (double)mod * 10.0);
            fence.setDamage(newDam);
            if (newDam >= 100.0f) {
                performer.getCommunicator().sendNormalServerMessage("The last parts of the " + fence.getName() + " falls down with a crash.");
                Server.getInstance().broadCastAction(performer.getName() + " damages a " + fence.getName() + " and it falls down with a crash.", performer, 5);
                if (performer.getDeity() != null) {
                    performer.performActionOkey(act);
                }
                TileEvent.log(fence.getTileX(), fence.getTileY(), 0, performer.getWurmId(), action);
                if (Servers.localServer.isChallengeServer() && fence.getType() != StructureConstantsEnum.FENCE_RUBBLE) {
                    DbFence newfence = new DbFence(StructureConstantsEnum.FENCE_RUBBLE, fence.getTileX(), fence.getTileY(), fence.getHeightOffset(), 100.0f, fence.getDir(), fence.getZoneId(), fence.getLayer());
                    try {
                        newfence.setState(StructureStateEnum.FINISHED);
                        Zone zone = Zones.getZone(fence.getZoneId());
                        zone.addFence(newfence);
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, nsz.getMessage(), nsz);
                    }
                }
            } else if (damage > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You damage the " + fence.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " damages the " + fence.getName() + ".", performer, 5);
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to damage the " + fence.getName() + ".");
            }
            toReturn = true;
        }
        return toReturn;
    }

    public static final Village getVillageForBlocker(Blocker blocker) {
        int tiley;
        if (blocker == null) {
            return null;
        }
        int tilex = blocker.getTileX();
        Village village = Zones.getVillage(tilex, tiley = blocker.getTileY(), blocker.isOnSurface());
        if (village != null) {
            return village;
        }
        if (blocker.isHorizontal() ? (village = Zones.getVillage(tilex, tiley - 1, blocker.isOnSurface())) != null : (village = Zones.getVillage(tilex - 1, tiley, blocker.isOnSurface())) != null) {
            return village;
        }
        return null;
    }

    public static final Village getVillageForFence(Fence fence) {
        int tiley;
        if (fence == null) {
            return null;
        }
        int tilex = fence.getTileX();
        Village village = Zones.getVillage(tilex, tiley = fence.getTileY(), fence.isOnSurface());
        if (village != null) {
            return village;
        }
        if (fence.getDirAsByte() == 2 ? (village = Zones.getVillage(tilex - 1, tiley, fence.isOnSurface())) != null : fence.getDirAsByte() == 0 && (village = Zones.getVillage(tilex, tiley - 1, fence.isOnSurface())) != null) {
            return village;
        }
        return null;
    }

    public static final boolean isCitizenAndMayPerformAction(short action, Creature performer, Village village) {
        return village != null && village.isCitizen(performer) && village.isActionAllowed(action, performer);
    }

    public static final boolean isAllyAndMayPerformAction(short action, Creature performer, Village village) {
        return village != null && village.isAlly(performer) && village.isActionAllowed(action, performer);
    }

    private static final Village getVillageForWall(Creature performer, Wall wall) {
        try {
            VolaTile t = wall.getOrCreateInnerTile(true);
            int tilex = t.tilex;
            int tiley = t.tiley;
            return Zones.getVillage(tilex, tiley, wall.isOnSurface());
        }
        catch (NoSuchTileException nst) {
            logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, performer.getName() + " " + nsz.getMessage(), nsz);
        }
        return null;
    }

    static final boolean improveFence(Action act, Creature performer, Item repairItem, Fence fence, float counter) {
        if (fence.getQualityLevel() == 100.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " does not need improving.");
            return true;
        }
        if (!fence.isFinished()) {
            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " is not finished yet so you can not improve it.");
            return true;
        }
        if (fence.getDamage() > 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + fence.getName() + " has damage you need to repair first.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, fence.getTileX(), fence.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 5;
        int templateId = fence.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + fence.getName() + " with that item.");
            return true;
        }
        float f = repairItem.getWeightGrams();
        float f2 = repairItem.getTemplate().getWeightGrams();
        float f3 = repairItem.getTemplateId() == 9 ? 0.7f : 1.0f;
        if (f < f2 * f3) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to improve the " + fence.getName() + ".");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(1005);
        if (fence.isStone() || fence.isSlate() || fence.isMarble() || fence.isPlastered() || fence.isRoundedStone() || fence.isPottery() || fence.isSandstone() || fence.isIron()) {
            buildSkill = performer.getSkills().getSkillOrLearn(1013);
        }
        int time = 400;
        if (counter == 1.0f) {
            double power = ((double)repairItem.getCurrentQualityLevel() + buildSkill.getKnowledge(0.0)) / 2.0;
            double diff = power - (double)fence.getQualityLevel();
            if (diff < 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + fence.getName() + " with that item and your knowledge.");
                return true;
            }
            act.setPower((float)(diff * 0.2));
            performer.getCommunicator().sendNormalServerMessage("You start to improve the " + fence.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to improve a " + fence.getName() + ".", performer, 5);
            time = Actions.getRepairActionTime(performer, buildSkill, 0.0);
            performer.sendActionControl(Actions.actionEntrys[192].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (buildSkill.getNumber() == 1013) {
                s = "sound.work.masonry";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                if (repairItem.getWeightGrams() <= 0) {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
            } else {
                repairItem.putInVoid();
                act.setDestroyedItem(repairItem);
            }
            buildSkill.skillCheck(buildSkill.getKnowledge(0.0) - 10.0, repairItem, 0.0, false, counter);
            Item destroyed = act.getDestroyedItem();
            if (destroyed != null) {
                Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
            }
            act.setDestroyedItem(null);
            double power = insta ? 100.0 : (double)act.getPower();
            float min = performer.getPower() > 0 && performer.getPower() < 5 ? 30.0f : 100.0f;
            performer.getCommunicator().sendNormalServerMessage("You improve the " + fence.getName() + " a bit.");
            fence.improveOrigQualityLevel(Math.min(min, (float)((double)fence.getOriginalQualityLevel() + power)));
            fence.setQualityLevel(Math.min(min, (float)((double)fence.getQualityLevel() + power)));
            Server.getInstance().broadCastAction(performer.getName() + " improves a " + fence.getName() + " a bit.", performer, 5);
            return true;
        }
        return false;
    }

    static final boolean repairFloor(Creature performer, Item repairItem, IFloor floor, float counter, Action act) {
        if (performer.isFighting()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
            return true;
        }
        if (floor.getDamage() == 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " does not need repairing.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, floor.getTileX(), floor.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 5;
        int templateId = floor.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot repair the " + floor.getName() + " with that item.");
            return true;
        }
        if (repairItem.getWeightGrams() < repairItem.getTemplate().getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to repair the " + floor.getName() + ".");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(MethodsStructure.getSkillFor(floor));
        Skill repairSkill = performer.getSkills().getSkillOrLearn(10035);
        int time = 400;
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start to repair the " + floor.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to repair a " + floor.getName() + ".", performer, 5);
            time = Actions.getRepairActionTime(performer, repairSkill, buildSkill.getKnowledge(0.0));
            performer.sendActionControl(Actions.actionEntrys[193].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-500.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (floor.isStone() || floor.isMarble() || floor.isSlate() || floor.isSandstone()) {
                s = "sound.work.masonry";
            } else if (floor.isMetal()) {
                s = "sound.work.smithing.metal";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                if (repairItem.getWeightGrams() <= 0) {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
            } else if (insta) {
                try {
                    Structure struct = Structures.getStructure(floor.getStructureId());
                    performer.sendToLoggers("Repairing wall with ID:" + floor.getId() + " part of structure with  ID:" + floor.getStructureId() + " Owned by  " + struct.getOwnerName() + " at " + floor.getTile().toString());
                }
                catch (NoSuchStructureException nss) {
                    logger.warning("No such structure on attempting to repair a wall? " + nss);
                }
                floor.setDamage(0.0f);
                performer.getCommunicator().sendNormalServerMessage("You magically repair the " + floor.getName() + " (ID:" + floor.getId() + ") with your powers.");
            } else {
                repairItem.putInVoid();
                act.setDestroyedItem(repairItem);
            }
            double power = repairSkill.skillCheck(floor.getDamage() / 10.0f, repairItem, 0.0, false, counter);
            Item destroyed = act.getDestroyedItem();
            if (destroyed != null) {
                Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
            }
            act.setDestroyedItem(null);
            if (insta) {
                power = 100.0;
            }
            if (power > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You repair the " + floor.getName() + " a bit.");
                float cq = floor.getCurrentQualityLevel();
                float diffcq = floor.getQualityLevel() - cq;
                float newOrigcq = floor.getQualityLevel() - (float)((double)diffcq * (100.0 - power)) / 10000.0f;
                float repairAmnt = 5.0f + (float)(5.0 * (repairSkill.getKnowledge(buildSkill.getKnowledge(0.0)) + (double)repairItem.getCurrentQualityLevel()) / 200.0);
                floor.setQualityLevel(newOrigcq);
                floor.setDamage(Math.max(0.0f, floor.getDamage() - repairAmnt));
                performer.achievement(155);
            } else if (power < -90.0) {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the " + floor.getName() + " and damage it instead!");
                floor.setDamage((float)Math.min(100.0, (double)floor.getDamage() - power / 100.0));
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the " + floor.getName() + ".");
            }
            floor.setLastUsed(System.currentTimeMillis());
            Server.getInstance().broadCastAction(performer.getName() + " repairs a " + floor.getName() + " a bit.", performer, 5);
            return true;
        }
        return false;
    }

    static final boolean repairWall(Action act, Creature performer, Item repairItem, Wall wall, float counter) {
        if (performer.getLayer() != wall.getLayer()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot reach the wall.");
            return true;
        }
        if (performer.isFighting()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot do that while in combat.");
            return true;
        }
        if (wall.getDamage() == 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The wall does not need repairing.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, wall.getTileX(), wall.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 4;
        int templateId = wall.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot repair the wall with that item.");
            return true;
        }
        if (repairItem.getWeightGrams() < repairItem.getTemplate().getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to repair the wall.");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(1005);
        if (wall.isStone() || wall.isPlainStone() || wall.isSlate() || wall.isMarble() || wall.isRoundedStone() || wall.isRendered() || wall.isPottery() || wall.isSandstone()) {
            buildSkill = performer.getSkills().getSkillOrLearn(1013);
        }
        Skill repairSkill = performer.getSkills().getSkillOrLearn(10035);
        int time = 400;
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start to repair the wall.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to repair a wall.", performer, 5);
            time = Actions.getRepairActionTime(performer, repairSkill, buildSkill.getKnowledge(0.0));
            performer.sendActionControl(Actions.actionEntrys[193].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-500.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (buildSkill.getNumber() == 1013) {
                s = "sound.work.masonry";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                if (repairItem.getWeightGrams() <= 0) {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
            } else if (insta) {
                try {
                    Structure struct = Structures.getStructure(wall.getStructureId());
                    performer.sendToLoggers("Repairing wall with ID:" + wall.getId() + " part of structure with  ID:" + wall.getStructureId() + " Owned by  " + struct.getOwnerName() + " at " + wall.getTile().toString());
                }
                catch (NoSuchStructureException nss) {
                    logger.warning("No such structure on attempting to repair a wall? " + nss);
                }
                wall.setDamage(0.0f);
                performer.getCommunicator().sendNormalServerMessage("You magically repair the " + wall.getMaterialString() + " wall (ID:" + wall.getId() + ") with your powers.");
            } else {
                repairItem.putInVoid();
                act.setDestroyedItem(repairItem);
            }
            double power = repairSkill.skillCheck(wall.getDamage() / 10.0f, repairItem, 0.0, false, counter);
            Item destroyed = act.getDestroyedItem();
            if (destroyed != null) {
                Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
            }
            act.setDestroyedItem(null);
            if (insta) {
                power = 100.0;
            }
            if (power > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You repair the wall a bit.");
                float cq = wall.getCurrentQualityLevel();
                float diffcq = wall.getQualityLevel() - cq;
                float newOrigcq = wall.getQualityLevel() - (float)((double)diffcq * (100.0 - power)) / 10000.0f;
                float repairAmnt = 5.0f + (float)(5.0 * (repairSkill.getKnowledge(buildSkill.getKnowledge(0.0)) + (double)repairItem.getCurrentQualityLevel()) / 200.0);
                wall.setQualityLevel(newOrigcq);
                wall.setDamage(Math.max(0.0f, wall.getDamage() - repairAmnt));
                performer.achievement(155);
            } else if (power < -90.0) {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the wall and damage it instead!");
                wall.setDamage((float)Math.min(100.0, (double)wall.getDamage() - power / 100.0));
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to repair the wall.");
            }
            wall.setLastUsed(System.currentTimeMillis());
            Server.getInstance().broadCastAction(performer.getName() + " repairs a wall a bit.", performer, 5);
            return true;
        }
        return false;
    }

    static final boolean improveFloor(Creature performer, Item repairItem, IFloor floor, float counter, Action act) {
        double power;
        if (floor.getQualityLevel() == 100.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " does not need improving.");
            return true;
        }
        if (!floor.isFinished()) {
            performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " is not finished yet so you can not improve it.");
            return true;
        }
        if (floor.getDamage() > 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + floor.getName() + " has damage you need to repair first.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, floor.getTileX(), floor.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 5;
        int templateId = floor.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + floor.getName() + " with that item.");
            return true;
        }
        if (repairItem.getWeightGrams() < repairItem.getTemplate().getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to improve the " + floor.getName() + ".");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(MethodsStructure.getSkillFor(floor));
        int time = 400;
        if (counter == 1.0f) {
            power = ((double)repairItem.getCurrentQualityLevel() + buildSkill.getKnowledge(0.0)) / 2.0;
            double diff = power - (double)floor.getQualityLevel();
            if (diff < 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + floor.getName() + " with that item and your knowledge.");
                return true;
            }
            act.setPower((float)(diff * 0.2));
            performer.getCommunicator().sendNormalServerMessage("You start to improve the " + floor.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to improve a " + floor.getName() + ".", performer, 5);
            time = Actions.getRepairActionTime(performer, buildSkill, 0.0);
            performer.sendActionControl(Actions.actionEntrys[192].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (floor.isStone() || floor.isSlate() || floor.isMarble() || floor.isSandstone()) {
                s = "sound.work.masonry";
            } else if (floor.isMetal()) {
                s = "sound.work.smithing.metal";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                if (repairItem.getWeightGrams() <= 0) {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
            } else {
                repairItem.putInVoid();
                act.setDestroyedItem(repairItem);
            }
            buildSkill.skillCheck(buildSkill.getKnowledge(0.0) - 10.0, repairItem, 0.0, false, counter);
            power = performer.getPower() >= 4 ? (float)(performer.getPower() >= 5 ? 100 : 80) : act.getPower();
            Item destroyed = act.getDestroyedItem();
            if (destroyed != null) {
                Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
            }
            act.setDestroyedItem(null);
            performer.getCommunicator().sendNormalServerMessage("You improve the " + floor.getName() + " a bit.");
            float min = 100.0f;
            if (performer.getPower() >= 3 || performer.getPower() == 4) {
                min = 80.0f;
            }
            floor.setQualityLevel(Math.min(min, (float)((double)floor.getQualityLevel() + power)));
            Server.getInstance().broadCastAction(performer.getName() + " improves the " + floor.getName() + " a bit.", performer, 5);
            if (performer.getDeity() != null && performer.getDeity().getBuildWallBonus() > 0.0f) {
                performer.maybeModifyAlignment(0.5f);
            }
            return true;
        }
        return false;
    }

    static final boolean improveWall(Action act, Creature performer, Item repairItem, Wall wall, float counter) {
        double power;
        if (wall.getQualityLevel() == 100.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + wall.getName() + " does not need improving.");
            return true;
        }
        if (!wall.isFinished()) {
            performer.getCommunicator().sendNormalServerMessage("The " + wall.getName() + " is not finished yet so you can not improve it.");
            return true;
        }
        if (wall.getDamage() > 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("The " + wall.getName() + " has damage you need to repair first.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, wall.getTileX(), wall.getTileY())) {
            return true;
        }
        boolean insta = performer.getPower() >= 5;
        int templateId = wall.getRepairItemTemplate();
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + wall.getName() + " with that item.");
            return true;
        }
        if (repairItem.getWeightGrams() < repairItem.getTemplate().getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to improve the " + wall.getName() + ".");
            return true;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(1005);
        if (wall.isStone() || wall.isPlainStone() || wall.isSlate() || wall.isMarble() || wall.isRoundedStone() || wall.isRendered() || wall.isPottery() || wall.isSandstone()) {
            buildSkill = performer.getSkills().getSkillOrLearn(1013);
        }
        int time = 400;
        if (counter == 1.0f) {
            power = ((double)repairItem.getCurrentQualityLevel() + buildSkill.getKnowledge(0.0)) / 2.0;
            double diff = power - (double)wall.getQualityLevel();
            if (diff < 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + wall.getName() + " with that item and your knowledge.");
                return true;
            }
            act.setPower((float)(diff * 0.2));
            performer.getCommunicator().sendNormalServerMessage("You start to improve the " + wall.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to improve a " + wall.getName() + ".", performer, 5);
            time = Actions.getRepairActionTime(performer, buildSkill, 0.0);
            performer.sendActionControl(Actions.actionEntrys[192].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (buildSkill.getNumber() == 1013) {
                s = "sound.work.masonry";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (counter * 10.0f >= (float)time || insta) {
            if (!insta) {
                if (repairItem.isCombine()) {
                    repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), false);
                    if (repairItem.getWeightGrams() <= 0) {
                        repairItem.putInVoid();
                        act.setDestroyedItem(repairItem);
                    }
                } else {
                    repairItem.putInVoid();
                    act.setDestroyedItem(repairItem);
                }
                buildSkill.skillCheck(buildSkill.getKnowledge(0.0) - 10.0, repairItem, 0.0, false, counter);
                Item destroyed = act.getDestroyedItem();
                if (destroyed != null) {
                    Items.decay(destroyed.getWurmId(), destroyed.getDbStrings());
                }
                act.setDestroyedItem(null);
            }
            power = insta ? 100.0 : (double)act.getPower();
            float min = performer.getPower() > 0 && performer.getPower() < 5 ? 30.0f : 100.0f;
            performer.getCommunicator().sendNormalServerMessage("You improve the " + wall.getName() + " a bit.");
            wall.improveOrigQualityLevel(Math.min(min, (float)((double)wall.getOriginalQualityLevel() + power)));
            wall.setQualityLevel(Math.min(min, (float)((double)wall.getQualityLevel() + power)));
            Server.getInstance().broadCastAction(performer.getName() + " improves a " + wall.getName() + " a bit.", performer, 5);
            if (performer.getDeity() != null && performer.getDeity().getBuildWallBonus() > 0.0f) {
                performer.maybeModifyAlignment(0.5f);
            }
            return true;
        }
        return false;
    }

    static final boolean destroyFloor(short action, Creature performer, Item destroyItem, IFloor floor, float counter) {
        boolean toReturn = false;
        Structure structure = null;
        int time = 300;
        if (performer.getPower() >= 2) {
            time = 15;
        }
        float mod = 1.0f;
        mod = destroyItem == null && !performer.isPlayer() ? 0.003f : (destroyItem != null ? floor.getDamageModifierForItem(destroyItem) : 0.0f);
        if (!floor.isOnSurface()) {
            mod *= 1.5f;
        }
        if (mod <= 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the " + floor.getName() + " with that.");
            return true;
        }
        try {
            structure = Structures.getStructure(floor.getStructureId());
        }
        catch (NoSuchStructureException nss) {
            if (performer.getPower() > 0) {
                performer.getCommunicator().sendNormalServerMessage("Could not find the structure that " + floor.getName() + " belongs to.");
            }
            return true;
        }
        Action act = null;
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "No Action for " + performer.getName() + "!", nsa);
            return true;
        }
        if (counter == 1.0f) {
            if (MethodsStructure.checkStructureDestruction(performer, structure, floor.getTile())) {
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("You start to destroy the " + floor.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to destroy a " + floor.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() % 5 == 0) {
            MethodsStructure.sendDestroySound(performer, destroyItem, floor.isThatch() || floor.isWood());
            performer.getStatus().modifyStamina(-5000.0f);
            if (destroyItem != null && !destroyItem.isBodyPartAttached()) {
                destroyItem.setDamage(destroyItem.getDamage() + mod * destroyItem.getDamageModifier());
            }
        }
        if (counter * 10.0f > (float)time) {
            float newdam;
            toReturn = true;
            boolean citizen = false;
            Skills skills = performer.getSkills();
            Skill destroySkill = null;
            try {
                destroySkill = skills.getSkill(102);
            }
            catch (NoSuchSkillException nss) {
                destroySkill = skills.learn(102, 1.0f);
            }
            Village vill = floor.getTile().getVillage();
            double damage = 10.0;
            if (destroyItem != null) {
                destroySkill.skillCheck(20.0, destroyItem, 0.0, false, counter);
                damage = floor.isWood() && destroyItem.isCarpentryTool() ? 100.0 * (1.0 + destroySkill.getKnowledge(0.0) / 100.0) : (destroyItem.isWeapon() ? Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 2.0 : (double)floor.getDamageModifierForItem(destroyItem));
                damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
            }
            if (vill != null) {
                if (MethodsStructure.isCitizenAndMayPerformAction(action, performer, vill)) {
                    damage *= 50.0;
                    citizen = true;
                } else if (MethodsStructure.isAllyAndMayPerformAction(action, performer, vill)) {
                    damage *= 25.0;
                    citizen = true;
                }
            } else if (Zones.isInPvPZone(floor.getTileX(), floor.getTileY())) {
                damage *= 10.0;
            }
            if (!citizen && performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                damage *= 2.0;
            }
            if (floor.setDamage(newdam = (float)(performer.getPower() >= 5 ? (double)(floor.getDamage() + 40.0f) : (double)floor.getDamage() + (damage /= (double)(floor.getQualityLevel() / 20.0f)) * (double)mod * 10.0))) {
                floor.destroyOrRevertToPlan();
            }
            if (newdam >= 100.0f || floor.isAPlan()) {
                TileEvent.log(floor.getTileX(), floor.getTileY(), 0, performer.getWurmId(), action);
                performer.getCommunicator().sendNormalServerMessage("The last parts of the " + floor.getName() + " fall down with a crash.");
                Server.getInstance().broadCastAction(performer.getName() + " damages a " + floor.getName() + " and the last parts fall down with a crash.", performer, 5);
                if (performer.getDeity() != null) {
                    performer.performActionOkey(act);
                }
                if (structure.isBridgeGone()) {
                    performer.getCommunicator().sendNormalServerMessage("The last parts of the bridge fall down with a crash.");
                    Server.getInstance().broadCastAction(performer.getName() + " cheers as the last parts of the bridge fall down with a crash.", performer, 5);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You damage the " + floor.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " damages a " + floor.getName() + ".", performer, 5);
            }
        }
        return toReturn;
    }

    static final boolean checkStructureDestruction(Creature performer, Structure structure, VolaTile tile) {
        Village village = null;
        if (tile != null && (village = tile.getVillage()) != null && village.isActionAllowed((short)82, performer)) {
            return false;
        }
        if (!Servers.isThisAChaosServer() && performer.getKingdomTemplateId() != 3 && Servers.localServer.HOMESERVER && !performer.isOnHostileHomeServer()) {
            if (!(MethodsStructure.mayModifyStructure(performer, structure, tile, (short)82) || Servers.localServer.isChallengeOrEpicServer() && Players.getInstance().getKingdomForPlayer(structure.getOwnerId()) != Servers.localServer.KINGDOM)) {
                performer.getCommunicator().sendNormalServerMessage("You need permission in order to make modifications to this structure.");
                return true;
            }
        } else if (!MethodsStructure.mayModifyStructure(performer, structure, tile, (short)82)) {
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
        return false;
    }

    static final boolean destroyWall(short action, Creature performer, Item destroyItem, Wall wall, boolean dealItems, float counter) {
        boolean toReturn = true;
        Structure structure = null;
        int time = 300;
        if (Servers.localServer.challengeServer) {
            time = 100;
        }
        float mod = 1.0f;
        mod = destroyItem == null && !(performer instanceof Player) ? 0.003f : (destroyItem != null ? wall.getDamageModifierForItem(destroyItem) : 0.0f);
        boolean insta = false;
        if (mod <= 0.0f) {
            performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the wall with that.");
            return true;
        }
        try {
            structure = Structures.getStructure(wall.getStructureId());
        }
        catch (NoSuchStructureException nss) {
            return true;
        }
        toReturn = false;
        Action act = null;
        String destString = "destroy";
        if (dealItems) {
            destString = "disassemble";
        }
        try {
            act = performer.getCurrentAction();
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.WARNING, "No Action for " + performer.getName() + "!", nsa);
            return true;
        }
        if (counter == 1.0f) {
            if (!wall.isIndoor() && wall.getState() == StructureStateEnum.INITIALIZED) {
                performer.getCommunicator().sendNormalServerMessage("The " + wall.getName() + " can not be destroyed further.");
                return true;
            }
            if (MethodsStructure.checkStructureDestruction(performer, structure, wall.getTile())) {
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("You start to " + destString + " the wall.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to " + destString + " a wall.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() % 5 == 0) {
            MethodsStructure.sendDestroySound(performer, destroyItem, wall.isWood());
            performer.getStatus().modifyStamina(-5000.0f);
            if (destroyItem != null && !destroyItem.isBodyPartAttached()) {
                destroyItem.setDamage(destroyItem.getDamage() + mod * destroyItem.getDamageModifier());
            }
        }
        if (counter * 10.0f > (float)time) {
            Skills skills = performer.getSkills();
            Skill destroySkill = null;
            try {
                destroySkill = skills.getSkill(102);
            }
            catch (NoSuchSkillException nss) {
                destroySkill = skills.learn(102, 1.0f);
            }
            destroySkill.skillCheck(20.0, destroyItem, 0.0, false, counter);
            toReturn = true;
            boolean citizen = false;
            double damage = 1.0;
            Village vill = MethodsStructure.getVillageForWall(performer, wall);
            if (destroyItem != null) {
                damage = wall.isWood() && destroyItem.isCarpentryTool() ? 100.0 * (1.0 + destroySkill.getKnowledge(0.0) / 100.0) : Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 2.0;
                damage /= (double)(wall.getQualityLevel() / 20.0f);
                if (vill != null) {
                    if (MethodsStructure.isCitizenAndMayPerformAction(action, performer, vill)) {
                        damage *= 50.0;
                        citizen = true;
                    } else if (MethodsStructure.isAllyAndMayPerformAction(action, performer, vill)) {
                        damage *= 25.0;
                        citizen = true;
                    }
                } else if (Zones.isInPvPZone(wall.getTileX(), wall.getTileY())) {
                    damage *= 10.0;
                }
                if (wall.isIndoor()) {
                    damage *= 5.0;
                }
                if (wall.isArched()) {
                    damage *= 3.0;
                }
                if (!wall.isFinished()) {
                    int modifier = wall.getFinalState().state - wall.getState().state;
                    damage *= (double)modifier;
                }
                if (!wall.isOnSurface()) {
                    damage *= 1.5;
                }
                damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
                if (!citizen && performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                    damage *= 2.0;
                }
            }
            float newdam = (float)((double)wall.getDamage() + damage * (double)mod * 10.0);
            wall.setDamage(newdam);
            if (newdam >= 100.0f || wall.getState() == StructureStateEnum.INITIALIZED) {
                TileEvent.log(wall.getTileX(), wall.getTileY(), 0, performer.getWurmId(), action);
                performer.getCommunicator().sendNormalServerMessage("The last parts of the wall fall down with a crash.");
                Server.getInstance().broadCastAction(performer.getName() + " damages a wall and the last parts fall down with a crash.", performer, 5);
                if (performer.getDeity() != null) {
                    performer.performActionOkey(act);
                }
                if (structure != null) {
                    if (!structure.hasWalls()) {
                        structure.totallyDestroy();
                    } else {
                        structure.updateStructureFinishFlag();
                    }
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("You damage the wall.");
                Server.getInstance().broadCastAction(performer.getName() + " damages a wall.", performer, 5);
            }
        }
        return toReturn;
    }

    public static void sendDestroySound(Creature performer, Item item, boolean wallIsWood) {
        String sound = "sound.destroywall.wood.axe";
        if (item.isWeaponCrush() && !wallIsWood) {
            sound = "sound.destroywall.stone.maul";
        } else if (item.isWeaponCrush()) {
            sound = "sound.destroywall.wood.maul";
        } else if (!wallIsWood) {
            sound = "sound.destroywall.stone.axe";
        }
        Methods.sendSound(performer, sound);
    }

    static final boolean colorWall(Creature performer, Item colour, Wall target, Action act) {
        boolean insta;
        boolean done = true;
        int colourNeeded = 1000;
        boolean bl = insta = performer.getPower() >= 5;
        if (!insta && 1000 > colour.getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("You need more dye to paint the " + target.getName() + " - at least " + 1000 + "g.");
        } else {
            done = false;
            if (act.currentSecond() == 1) {
                performer.getCommunicator().sendNormalServerMessage("You start to paint the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to paint a " + target.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[231].getVerbString(), true, 100);
            } else if (insta || act.getCounterAsFloat() >= 10.0f) {
                done = true;
                target.setColor(colour.getColor());
                colour.setWeight(colour.getWeightGrams() - 1000, true);
                performer.getCommunicator().sendNormalServerMessage("You put some colour on the " + target.getName() + ".");
            }
        }
        return done;
    }

    static final boolean removeColor(Creature performer, Item brush, Wall target, Action act) {
        boolean done = true;
        if (brush.getTemplateId() == 441) {
            boolean insta;
            boolean bl = insta = performer.getPower() >= 5;
            if (!insta && target.getColor() == -1) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no paint.");
            } else {
                done = false;
                if (act.currentSecond() == 1) {
                    act.setTimeLeft(150);
                    performer.getCommunicator().sendNormalServerMessage("You start to brush the " + target.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to brush a " + target.getName() + ".", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[232].getVerbString(), true, act.getTimeLeft());
                } else {
                    int timeleft = act.getTimeLeft();
                    if (insta || act.getCounterAsFloat() * 10.0f >= (float)timeleft) {
                        done = true;
                        target.setColor(-1);
                        brush.setDamage((float)((double)brush.getDamage() + 0.5 * (double)brush.getDamageModifier()));
                        performer.getCommunicator().sendNormalServerMessage("You remove the paint from the " + target.getName() + ".");
                    }
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to remove the paint.");
        }
        return done;
    }

    static final boolean colorFence(Creature performer, Item colour, Fence target, Action act) {
        boolean insta;
        boolean done = true;
        int colourNeeded = 1000;
        boolean bl = insta = performer.getPower() >= 5;
        if (!insta && 1000 > colour.getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("You need more dye to paint the " + target.getName() + " - at least " + 1000 + "g.");
        } else {
            done = false;
            if (act.currentSecond() == 1) {
                performer.getCommunicator().sendNormalServerMessage("You start to paint the " + target.getName() + ".");
                Server.getInstance().broadCastAction(performer.getName() + " starts to paint a " + target.getName() + ".", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[231].getVerbString(), true, 100);
            } else if (insta || act.getCounterAsFloat() >= 10.0f) {
                done = true;
                target.setColor(colour.getColor());
                colour.setWeight(colour.getWeightGrams() - 1000, true);
                performer.getCommunicator().sendNormalServerMessage("You put some colour on the " + target.getName() + ".");
            }
        }
        return done;
    }

    static final boolean removeColor(Creature performer, Item brush, Fence target, Action act) {
        boolean done = true;
        if (brush.getTemplateId() == 441) {
            boolean insta;
            boolean bl = insta = performer.getPower() >= 5;
            if (!insta && target.getColor() == -1) {
                performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no paint.");
            } else {
                done = false;
                if (act.currentSecond() == 1) {
                    act.setTimeLeft(150);
                    performer.getCommunicator().sendNormalServerMessage("You start to brush the " + target.getName() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to brush a " + target.getName() + ".", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[232].getVerbString(), true, act.getTimeLeft());
                } else {
                    int timeleft = act.getTimeLeft();
                    if (insta || act.getCounterAsFloat() * 10.0f >= (float)timeleft) {
                        done = true;
                        target.setColor(-1);
                        brush.setDamage((float)((double)brush.getDamage() + 0.5 * (double)brush.getDamageModifier()));
                        performer.getCommunicator().sendNormalServerMessage("You remove the paint from the " + target.getName() + ".");
                    }
                }
            }
        } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to remove the paint.");
        }
        return done;
    }

    protected static final int getImproveItem(byte minedoortype) {
        if (minedoortype == Tiles.Tile.TILE_MINE_DOOR_GOLD.id) {
            return 44;
        }
        if (minedoortype == Tiles.Tile.TILE_MINE_DOOR_STEEL.id) {
            return 205;
        }
        if (minedoortype == Tiles.Tile.TILE_MINE_DOOR_SILVER.id) {
            return 45;
        }
        if (minedoortype == Tiles.Tile.TILE_MINE_DOOR_STONE.id) {
            return 146;
        }
        return 22;
    }

    static final boolean improveTileDoor(Creature performer, Item repairItem, int tilex, int tiley, int tile, Action act, float counter) {
        double power;
        boolean insta = performer.getPower() >= 5;
        int templateId = MethodsStructure.getImproveItem(Tiles.decodeType(tile));
        if (repairItem.getTemplateId() != templateId && !insta) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the mine door with that item.");
            return true;
        }
        if (repairItem.getWeightGrams() < repairItem.getTemplate().getWeightGrams()) {
            performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " contains too little material to improve the mine door.");
            return true;
        }
        if (!insta && repairItem.isCombine() && repairItem.isMetal() && repairItem.getTemperature() < 3500) {
            performer.getCommunicator().sendNormalServerMessage("Metal needs to be glowing hot while improving mine doors.");
            return true;
        }
        int strength = Server.getWorldResource(tilex, tiley);
        double mineQuality = strength <= 100 ? 1.0 : (double)(strength / 100);
        int skillnum = 10015;
        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MINE_DOOR_STONE.id) {
            skillnum = 1013;
        } else if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MINE_DOOR_WOOD.id) {
            skillnum = 1005;
        }
        Skill buildSkill = performer.getSkills().getSkillOrLearn(skillnum);
        int time = 400;
        if (counter == 1.0f) {
            power = buildSkill.skillCheck(mineQuality, repairItem, 0.0, true, 1.0f);
            float imbueEnhancement = 1.0f + repairItem.getSkillSpellImprovement(skillnum) / 100.0f;
            double improveBonus = 0.23047 * (double)imbueEnhancement;
            float improveItemBonus = ItemBonus.getImproveSkillMaxBonus(performer);
            double max = buildSkill.getKnowledge(0.0) * (double)improveItemBonus + (100.0 - buildSkill.getKnowledge(0.0) * (double)improveItemBonus) * improveBonus;
            double diff = Math.max(0.0, max - mineQuality);
            double mod = (100.0 - mineQuality) / 20.0 / 100.0 * (double)(Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat()) / 2.0;
            if (power < 0.0) {
                act.setPower((float)(-mod * Math.max(1.0, diff)));
            } else {
                if (diff <= 0.0) {
                    mod *= (double)0.01f;
                }
                act.setPower((float)(mod * Math.max(1.0, diff)));
            }
            if ((double)repairItem.getCurrentQualityLevel() <= mineQuality) {
                performer.getCommunicator().sendNormalServerMessage("The " + repairItem.getName() + " is in too poor shape to improve the mine door.");
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("You start to strengthen the mine door.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to strengthen a mine door.", performer, 5);
            time = Actions.getImproveActionTime(performer, repairItem) * 2;
            performer.sendActionControl(Actions.actionEntrys[192].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-counter * 1000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s = "sound.work.smithing.hammer";
            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MINE_DOOR_STONE.id) {
                s = "sound.work.masonry";
            } else if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MINE_DOOR_WOOD.id) {
                s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (counter * 10.0f > (float)time || insta) {
            buildSkill.skillCheck(buildSkill.getKnowledge(0.0) - 10.0, repairItem, 0.0, false, counter);
            power = act.getPower();
            if (insta) {
                power = 5.0;
            }
            if (power > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You strengthen the mine door a bit.");
                Server.getInstance().broadCastAction(performer.getName() + " strengthens the mine door a bit.", performer, 5);
                Server.setWorldResource(tilex, tiley, Math.min(10000, strength + (int)Math.round(power * 100.0)));
            } else {
                performer.getCommunicator().sendNormalServerMessage("You fail to strengthen the mine door.");
                Server.getInstance().broadCastAction(performer.getName() + " fails to strengthen the mine door.", performer, 5);
            }
            if (repairItem.isCombine()) {
                repairItem.setWeight(repairItem.getWeightGrams() - repairItem.getTemplate().getWeightGrams(), true);
            } else {
                Items.destroyItem(repairItem.getWurmId());
            }
            return true;
        }
        return false;
    }

    static boolean picklock(Creature performer, Item lockpick, Door target, String name, float counter, Action act) {
        long lockId = -10L;
        boolean done = false;
        try {
            lockId = target.getLockId();
        }
        catch (NoSuchLockException nsl) {
            performer.getCommunicator().sendNormalServerMessage("The " + name + " has no lock to pick.", (byte)3);
            return true;
        }
        if (lockpick.getTemplateId() != 463) {
            performer.getCommunicator().sendNormalServerMessage("The " + lockpick.getName() + " can not be used as a lockpick.", (byte)3);
            return true;
        }
        if (target.isNotLockpickable()) {
            performer.getCommunicator().sendNormalServerMessage("The " + name + " can not be lockpicked.", (byte)3);
            return true;
        }
        if (target.getLockCounter() > 0) {
            performer.getCommunicator().sendNormalServerMessage("The " + name + " has already been lockpicked.", (byte)3);
            return true;
        }
        boolean insta = performer.getPower() >= 5 || Servers.localServer.testServer && performer.getPower() > 1;
        Skill lockpicking = null;
        Skills skills = performer.getSkills();
        try {
            lockpicking = skills.getSkill(10076);
        }
        catch (NoSuchSkillException nss) {
            lockpicking = skills.learn(10076, 1.0f);
        }
        int time = 300;
        if (counter == 1.0f) {
            for (Player p : Players.getInstance().getPlayers()) {
                if (p.getWurmId() == performer.getWurmId()) continue;
                try {
                    Action pact = p.getCurrentAction();
                    if (act.getNumber() != pact.getNumber() || act.getTarget() != pact.getTarget()) continue;
                    performer.getCommunicator().sendNormalServerMessage("The " + name + " is already being picked by " + p.getName() + ".", (byte)3);
                    return true;
                }
                catch (NoSuchActionException pact) {
                    // empty catch block
                }
            }
            try {
                Item lock = Items.getItem(lockId);
                if (lock.getQualityLevel() - lockpick.getQualityLevel() > 20.0f) {
                    performer.getCommunicator().sendNormalServerMessage("You need a more advanced lock pick for this high quality lock.", (byte)3);
                    return true;
                }
            }
            catch (NoSuchItemException nsi) {
                performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
                logger.log(Level.WARNING, "No such lock, but it should be locked." + nsi.getMessage(), nsi);
                return true;
            }
            time = Actions.getPickActionTime(performer, lockpicking, lockpick, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to pick the lock of the " + name + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to pick the lock of the " + name + ".", performer, 5);
            if (target.isLocked()) {
                performer.sendActionControl("picking lock", true, time);
            } else {
                performer.sendActionControl("locking", true, time);
            }
            performer.getStatus().modifyStamina(-2000.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() == 2) {
            MethodsItems.checkLockpickBreakage(performer, lockpick, 100, 80.0);
        }
        if (counter * 10.0f > (float)time || insta) {
            boolean dryRun = false;
            performer.getStatus().modifyStamina(-2000.0f);
            try {
                Item lock = Items.getItem(lockId);
                double bonus = 100.0f * Item.getMaterialLockpickBonus(lockpick.getMaterial());
                int breakBonus = (int)(bonus * 2.0);
                bonus = Math.min(99.0, bonus);
                done = true;
                float wallQl = target.getQualityLevel();
                if (performer.getPower() <= 1 || Servers.localServer.testServer) {
                    Village village = Zones.getVillage(act.getTileX(), act.getTileY(), performer.isOnSurface());
                    if (village != null) {
                        if (!village.isActionAllowed(act.getNumber(), performer)) {
                            if (Action.checkLegalMode(performer)) {
                                return true;
                            }
                            if (MethodsItems.setTheftEffects(performer, act, act.getTileX(), act.getTileY(), performer.isOnSurface())) {
                                double power = lockpicking.skillCheck(lock.getCurrentQualityLevel() / 3.0f + wallQl / 3.0f, lockpick, bonus, false, 10.0f);
                                MethodsItems.checkLockpickBreakage(performer, lockpick, breakBonus, power);
                                return true;
                            }
                        }
                    } else if (MethodsItems.setTheftEffects(performer, act, act.getTileX(), act.getTileY(), performer.isOnSurface())) {
                        double power = lockpicking.skillCheck(lock.getCurrentQualityLevel() + wallQl / 3.0f, lockpick, bonus, false, 10.0f);
                        MethodsItems.checkLockpickBreakage(performer, lockpick, breakBonus, power);
                        return true;
                    }
                }
                float rarityMod = 1.0f;
                if (lock.getRarity() > 0) {
                    rarityMod += (float)lock.getRarity() * 0.2f;
                }
                if (lockpick.getRarity() > 0) {
                    rarityMod -= (float)lockpick.getRarity() * 0.1f;
                }
                double power = lockpicking.skillCheck(lock.getCurrentQualityLevel(), lockpick, bonus, false, 10.0f);
                float chance = MethodsItems.getPickChance(wallQl, lockpick.getCurrentQualityLevel(), lock.getCurrentQualityLevel(), (float)lockpicking.getRealKnowledge(), (byte)1) / rarityMod * (1.0f + Item.getMaterialLockpickBonus(lockpick.getMaterial()));
                if (Server.rand.nextFloat() * 100.0f < chance) {
                    String opentime = "";
                    target.setLockCounter((short)1200);
                    opentime = " It will stay unlocked for 10 minutes.";
                    PermissionsHistories.addHistoryEntry(target.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Lock Picked");
                    performer.getCommunicator().sendNormalServerMessage("You pick the lock of the " + name + " and unlock it." + opentime);
                    Server.getInstance().broadCastAction(performer.getName() + " picks the lock of the " + name + " and unlocks it.", performer, 5);
                    TileEvent.log(performer.getTileX(), performer.getTileY(), performer.isOnSurface() ? 0 : -1, performer.getWurmId(), 101);
                    performer.achievement(111);
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You fail to pick the lock of the " + name + ".", (byte)3);
                    Server.getInstance().broadCastAction(performer.getName() + " silently curses as " + performer.getHeSheItString() + " fails to pick the lock of the " + name + ".", performer, 5);
                }
                if (power > 0.0) {
                    MethodsItems.checkLockpickBreakage(performer, lockpick, breakBonus, 100.0);
                } else {
                    MethodsItems.checkLockpickBreakage(performer, lockpick, breakBonus, -100.0);
                }
            }
            catch (NoSuchItemException nsi) {
                performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
                logger.log(Level.WARNING, "No such lock, but it should be locked." + nsi.getMessage(), nsi);
                return true;
            }
        }
        return done;
    }

    protected static final void addLockPickEntry(Creature performer, Item source, Door target, boolean isFence, Item lock, List<ActionEntry> toReturn) {
        float rarityMod = 1.0f;
        if (lock.getRarity() > 0) {
            rarityMod += (float)lock.getRarity() * 0.2f;
        }
        if (source.getRarity() > 0) {
            rarityMod -= (float)source.getRarity() * 0.1f;
        }
        float difficulty = MethodsItems.getPickChance(target.getQualityLevel(), source.getCurrentQualityLevel(), lock.getCurrentQualityLevel(), (float)performer.getLockPickingSkillVal(), isFence ? (byte)3 : 1) / rarityMod * (1.0f + Item.getMaterialLockpickBonus(source.getMaterial()));
        String pick = "Pick lock: " + difficulty + "%";
        toReturn.add(new ActionEntry(101, pick, "picking lock"));
    }

    protected static final void addLockUnlockEntry(Creature performer, Wall wall, List<ActionEntry> toReturn) {
        Door door = wall.getDoor();
        if (door == null) {
            return;
        }
        long structureId = wall.getStructureId();
        Structure structure = null;
        try {
            structure = Structures.getStructure(structureId);
        }
        catch (NoSuchStructureException nse) {
            structure = null;
            logger.log(Level.WARNING, "Wall " + wall.getId() + " missing structure: getStructure('" + structureId + "')");
        }
        if (door.isLocked()) {
            toReturn.add(new ActionEntry(102, "Unlock door", "unlocking door"));
        } else {
            toReturn.add(new ActionEntry(28, "Lock door", "unlocking door"));
        }
        if (structure.getOwnerId() == performer.getWurmId()) {
            toReturn.add(new ActionEntry(59, "Rename door", "renaming door"));
        }
    }

    public static final boolean isWallInsideStructure(Wall wall, boolean surfaced) {
        if (wall.getFloorLevel() > 0) {
            return true;
        }
        int x = Math.min(wall.getStartX(), wall.getEndX());
        int y = Math.min(wall.getStartY(), wall.getEndY());
        Tiles.TileBorderDirection dir = wall.getStartY() == wall.getEndY() ? Tiles.TileBorderDirection.DIR_HORIZ : Tiles.TileBorderDirection.DIR_DOWN;
        VolaTile vtile = Zones.getTileOrNull(x, y, surfaced);
        Structure structure = null;
        if (vtile != null) {
            structure = vtile.getStructure();
        }
        if (structure == null) {
            return false;
        }
        structure = null;
        if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
            vtile = Zones.getTileOrNull(x - 1, y, surfaced);
            if (vtile != null) {
                structure = vtile.getStructure();
            }
        } else {
            vtile = Zones.getTileOrNull(x, y - 1, surfaced);
            if (vtile != null) {
                structure = vtile.getStructure();
            }
        }
        return structure != null;
    }

    public static final boolean isBorderInsideStructure(int x, int y, Tiles.TileBorderDirection dir, boolean surfaced) {
        return MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, surfaced) == null;
    }

    public static boolean floorPlanAbove(Creature performer, Item source, int tilex, int tiley, int encodedTile, int layer, float counter, Action act, StructureConstants.FloorType floorType) {
        return MethodsStructure.floorPlan(performer, source, tilex, tiley, encodedTile, layer, counter, act, (short)30, floorType);
    }

    public static boolean floorPlanBelow(Creature performer, Item source, int tilex, int tiley, int encodedTile, int layer, float counter, Action act) {
        return MethodsStructure.floorPlan(performer, source, tilex, tiley, encodedTile, layer, counter, act, (short)0, StructureConstants.FloorType.FLOOR);
    }

    public static boolean floorPlanRoof(Creature performer, Item source, int tilex, int tiley, int encodedTile, int layer, float counter, Action act) {
        return MethodsStructure.floorPlan(performer, source, tilex, tiley, encodedTile, layer, counter, act, (short)30, StructureConstants.FloorType.ROOF);
    }

    static final boolean hasSupportAtTile(Creature performer, int tilex, int tiley, Structure structure, int heightOffset) {
        Floor[] floors;
        for (Floor floor : floors = structure.getFloorsAtTile(tilex, tiley, heightOffset - 30, heightOffset + 30)) {
            if (floor.getHeightOffset() != heightOffset || !floor.isFinished()) continue;
            return true;
        }
        return false;
    }

    public static boolean floorPlan(Creature performer, Item source, int tilex, int tiley, int encodedTile, int layer, float counter, Action act, short heightOffsetFromPerformer, StructureConstants.FloorType floorType) {
        boolean neighbourSection;
        Skill floorBuildSkill;
        boolean surfaced = layer >= 0;
        VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, layer != -1);
        if (vtile == null) {
            performer.getCommunicator().sendNormalServerMessage("Your sensitive mind notices an anomaly in the fabric of space.");
            return true;
        }
        if (!Terraforming.isFlat(tilex, tiley, surfaced, 0)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot build that here. The ground needs to be absolutely flat!");
            return true;
        }
        Structure structure = vtile.getStructure();
        if (structure == null) {
            performer.getCommunicator().sendNormalServerMessage("There is no structure here to remodel.");
            return true;
        }
        if (!structure.isFinalFinished()) {
            performer.getCommunicator().sendNormalServerMessage("You need to finish the outer walls on the first floor before adding floors or roof to this structure.");
            return true;
        }
        if (!structure.isTypeHouse()) {
            performer.getCommunicator().sendNormalServerMessage("You can only build floors and roofs in a buildinge.");
            return true;
        }
        if (!MethodsStructure.mayModifyStructure(performer, structure, vtile, (short)116)) {
            performer.getCommunicator().sendNormalServerMessage("You need permission in order to make modifications to this structure.");
            return true;
        }
        if (!Methods.isActionAllowed(performer, (short)116, tilex, tiley)) {
            return true;
        }
        short decodedTileHeight = Tiles.decodeHeight(encodedTile);
        int playerPosition = (int)(performer.getStatus().getPositionZ() * 10.0f) - decodedTileHeight;
        playerPosition = Math.abs(playerPosition) - Math.abs(playerPosition % 30);
        int buildOffset = 1;
        int fLevel = performer.getFloorLevel(true);
        if (heightOffsetFromPerformer == 0) {
            buildOffset = 0;
        }
        if (!FloorBehaviour.mayPlanAtLevel(performer, fLevel + buildOffset, floorBuildSkill = FloorBehaviour.getBuildSkill(floorType, StructureConstants.FloorMaterial.WOOD, performer), floorType == StructureConstants.FloorType.ROOF)) {
            return true;
        }
        if (fLevel + buildOffset == 0 && MethodsHighways.onHighway(tilex, tiley, surfaced)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to add ground flooring to a highway.");
            return true;
        }
        if (floorBuildSkill.getKnowledge(0.0) < (double)FloorBehaviour.getRequiredBuildSkillForFloorType(StructureConstants.FloorMaterial.WOOD)) {
            performer.getCommunicator().sendNormalServerMessage("You need higher " + floorBuildSkill.getName() + " skill to plan here.");
            return true;
        }
        int floorBuildOffset = playerPosition + heightOffsetFromPerformer;
        performer.sendToLoggers("Player pos=" + playerPosition + ", floorBuildOffset=" + floorBuildOffset + " heightOffsetFromPerformer=" + heightOffsetFromPerformer + " floorBuildOffset=" + floorBuildOffset);
        long structureId = structure.getWurmId();
        Floor[] floors = structure.getFloorsAtTile(tilex, tiley, floorBuildOffset - 30, floorBuildOffset + 30);
        boolean hasFloorUnder = false;
        boolean bl = neighbourSection = heightOffsetFromPerformer == 0;
        if (floors != null) {
            for (Floor floor : floors) {
                if (floor.getTileX() != tilex || floor.getTileY() != tiley) continue;
                if (floor.getHeightOffset() == floorBuildOffset) {
                    performer.getCommunicator().sendNormalServerMessage("There is already a " + (floor.getFloorState() == StructureConstants.FloorState.PLANNING ? "planned " : "") + floor.getName() + " there. ");
                    return true;
                }
                if (floorBuildOffset < 30 || floor.getHeightOffset() != floorBuildOffset - 30) continue;
                hasFloorUnder = true;
                if (!floor.isRoof()) continue;
                performer.getCommunicator().sendNormalServerMessage("There is already a " + (floor.getFloorState() == StructureConstants.FloorState.PLANNING ? "planned " : "") + floor.getName() + " there. ");
                return true;
            }
        }
        if (floorBuildOffset > 0) {
            neighbourSection = MethodsStructure.hasSupportAtTile(performer, tilex - 1, tiley, structure, floorBuildOffset);
            if (!neighbourSection) {
                neighbourSection = MethodsStructure.hasSupportAtTile(performer, tilex + 1, tiley, structure, floorBuildOffset);
            }
            if (!neighbourSection) {
                neighbourSection = MethodsStructure.hasSupportAtTile(performer, tilex, tiley - 1, structure, floorBuildOffset);
            }
            if (!neighbourSection) {
                neighbourSection = MethodsStructure.hasSupportAtTile(performer, tilex, tiley + 1, structure, floorBuildOffset);
            }
        }
        if (!hasFloorUnder && floorBuildOffset > 30) {
            performer.getCommunicator().sendNormalServerMessage("You need to have a floor under this one to be able to build safely.");
            return true;
        }
        if (!Terraforming.isAllCornersInsideHeightRange(tilex, tiley, surfaced, decodedTileHeight, decodedTileHeight)) {
            performer.getCommunicator().sendNormalServerMessage("The ground must be flat for advanced constructions.");
            return true;
        }
        if (layer < 0 && (floorType == StructureConstants.FloorType.OPENING || floorType == StructureConstants.FloorType.DOOR || floorType == StructureConstants.FloorType.STAIRCASE || floorType == StructureConstants.FloorType.WIDE_STAIRCASE || floorType == StructureConstants.FloorType.LEFT_STAIRCASE || floorType == StructureConstants.FloorType.RIGHT_STAIRCASE || floorType == StructureConstants.FloorType.ANTICLOCKWISE_STAIRCASE || floorType == StructureConstants.FloorType.CLOCKWISE_STAIRCASE)) {
            int ceil = 260;
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    int tile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    short ht = (short)(Tiles.decodeData(tile) & 0xFF);
                    if (ht >= ceil) continue;
                    ceil = ht;
                }
            }
            if (floorBuildOffset + 30 > ceil) {
                performer.getCommunicator().sendNormalServerMessage("There is not enough room for a further floor. E.g. ceiling is too close.");
                return true;
            }
        }
        if (!neighbourSection) {
            VolaTile southTile;
            VolaTile eastTile;
            for (Wall wall : vtile.getWalls()) {
                performer.sendToLoggers(wall.getFloorLevel() + ";" + wall.getFloorLevel() * 30 + "==" + floorBuildOffset + " OR " + (wall.getFloorLevel() * 30 + 30));
                if (wall.getFloorLevel() * 30 != floorBuildOffset && wall.getFloorLevel() * 30 + 30 != floorBuildOffset || !wall.isFinished()) continue;
                neighbourSection = true;
                break;
            }
            if ((eastTile = Zones.getTileOrNull(tilex + 1, tiley, structure.isOnSurface())) != null) {
                for (Wall wall : eastTile.getWalls()) {
                    performer.sendToLoggers(wall.getFloorLevel() + ";" + wall.getFloorLevel() * 30 + "==" + floorBuildOffset + " OR " + (wall.getFloorLevel() * 30 + 30));
                    if (wall.getFloorLevel() * 30 != floorBuildOffset && wall.getFloorLevel() * 30 + 30 != floorBuildOffset || !wall.isFinished()) continue;
                    if (wall.isHorizontal() || wall.getStartX() != eastTile.getTileX()) break;
                    neighbourSection = true;
                    break;
                }
            }
            if ((southTile = Zones.getTileOrNull(tilex, tiley + 1, structure.isOnSurface())) != null) {
                for (Wall wall : southTile.getWalls()) {
                    performer.sendToLoggers(wall.getFloorLevel() + ";" + wall.getFloorLevel() * 30 + "==" + floorBuildOffset + " OR " + (wall.getFloorLevel() * 30 + 30));
                    if (wall.getFloorLevel() * 30 != floorBuildOffset && wall.getFloorLevel() * 30 + 30 != floorBuildOffset || !wall.isFinished()) continue;
                    if (!wall.isHorizontal() || wall.getStartY() != southTile.getTileY()) break;
                    neighbourSection = true;
                    break;
                }
            }
        }
        if (!neighbourSection) {
            performer.getCommunicator().sendNormalServerMessage("You need to build in connection with a finished neighbouring floor, roof or wall.");
            return true;
        }
        Skill craftSkill = performer.getSkills().getSkillOrLearn(1005);
        float qualityLevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), craftSkill.getKnowledge(0.0), 0.0f, 21);
        DbFloor floor = new DbFloor(floorType, tilex, tiley, floorBuildOffset, qualityLevel, structureId, StructureConstants.FloorMaterial.WOOD, layer);
        try {
            ((Floor)floor).save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        vtile.addFloor(floor);
        performer.getCommunicator().sendNormalServerMessage("You plan a " + floor.getName() + (heightOffsetFromPerformer > 0 ? " above you." : " below you."));
        return true;
    }

    public static boolean floorDestroy(Creature performer, Item source, int tilex, int tiley, int layer, float counter, Action act, boolean surface) {
        int time = 200;
        VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, layer != -1);
        if (vtile == null) {
            performer.getCommunicator().sendNormalServerMessage("Your sensitive mind notices a wrongness in the fabric of space.");
            return true;
        }
        Structure structure = vtile.getStructure();
        if (structure == null) {
            performer.getCommunicator().sendNormalServerMessage("There is no structure here to remodel.");
            return true;
        }
        if (structure.getOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("Only the owner of " + structure.getName() + " is allowed to remodel it.");
            return true;
        }
        Floor[] floors = vtile.getFloors(0, 30);
        if (floors == null) {
            performer.getCommunicator().sendNormalServerMessage("No floors found here!");
            return true;
        }
        if (!MethodsStructure.mayModifyStructure(performer, structure, vtile, (short)524)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that!");
            return true;
        }
        if (floors.length > 1) {
            logger.log(Level.WARNING, "Weirdness! Multiple floors found for tile: " + tilex + " " + tiley);
        }
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You start to destroy the floor.");
        } else {
            time = act.getTimeLeft();
        }
        if (counter * 10.0f > (float)time) {
            for (int i = 0; i < floors.length; ++i) {
                vtile.removeFloor(floors[i]);
                floors[i].delete();
            }
            performer.getCommunicator().sendNormalServerMessage("The last parts of the floor falls down with a loud crash.");
            performer.performActionOkey(act);
            return true;
        }
        return false;
    }

    static boolean isCorrectToolForPlanning(Creature performer, int toolTemplateId) {
        return toolTemplateId == 62 || toolTemplateId == 63 || toolTemplateId == 176 && WurmPermissions.mayUseGMWand(performer) || toolTemplateId == 315 && performer.getPower() >= 2 && Servers.isThisATestServer();
    }

    public static boolean isCorrectToolForBuilding(Creature performer, int toolTemplateId) {
        return toolTemplateId == 62 || toolTemplateId == 63 || toolTemplateId == 493 || toolTemplateId == 176 && WurmPermissions.mayUseGMWand(performer) || toolTemplateId == 315 && performer.getPower() >= 2 && Servers.isThisATestServer();
    }

    public static final int[] getCorrectToolsForBuildingFences() {
        int[] tools = new int[]{62, 63, 493};
        return tools;
    }

    public static boolean planWallAt(Action act, Creature aPerformer, Item aSource, int aTileX, int aTileY, boolean onSurfaced, int heightOffset, Tiles.TileBorderDirection dir, short action, float counter) {
        int yend;
        int xend;
        Skill craftSkill;
        if (aSource == null) {
            aPerformer.getCommunicator().sendNormalServerMessage("You need an activated tool to plan a wall.");
            return true;
        }
        if (!MethodsStructure.isCorrectToolForBuilding(aPerformer, aSource.getTemplateId())) {
            aPerformer.getCommunicator().sendNormalServerMessage("You cannot do that with " + aSource.getNameWithGenus() + ".");
            return true;
        }
        if (action != 20000 + StructureTypeEnum.SOLID.value) {
            aPerformer.getCommunicator().sendNormalServerMessage("You can only plan walls here.");
            return true;
        }
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(aTileX, aTileY, dir, onSurfaced);
        if (structure == null) {
            aPerformer.getCommunicator().sendNormalServerMessage("The structural integrity of the building is at risk.");
            logger.log(Level.WARNING, "Structure not found while trying to add a wall at [" + aTileX + "," + aTileY + "]");
            return true;
        }
        structure.updateStructureFinishFlag();
        try {
            craftSkill = aSource.getTemplateId() == 493 ? aPerformer.getSkills().getSkill(1013) : aPerformer.getSkills().getSkill(1005);
        }
        catch (NoSuchSkillException nss) {
            craftSkill = aSource.getTemplateId() == 493 ? aPerformer.getSkills().learn(1013, 1.0f) : aPerformer.getSkills().learn(1005, 1.0f);
        }
        if ((double)FloorBehaviour.getRequiredBuildSkillForFloorLevel(heightOffset / 30, false) > craftSkill.getKnowledge(0.0)) {
            aPerformer.getCommunicator().sendNormalServerMessage("Construction of walls is reserved for craftsmen of higher rank than yours.");
            if (Servers.localServer.testServer) {
                aPerformer.getCommunicator().sendNormalServerMessage("You have " + craftSkill.getRealKnowledge() + " and need " + FloorBehaviour.getRequiredBuildSkillForFloorLevel(heightOffset / 30, false));
            }
            return true;
        }
        VolaTile vtile = Zones.getOrCreateTile(aTileX, aTileY, onSurfaced);
        if (vtile == null) {
            aPerformer.getCommunicator().sendNormalServerMessage("The structural integrity of the building is at risk.");
            return true;
        }
        int structureTileX = aTileX;
        int structureTileY = aTileY;
        if (vtile.getStructure() == null || !vtile.getStructure().isTypeHouse()) {
            if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
                --structureTileX;
            } else if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
                --structureTileY;
            }
        }
        if ((vtile = Zones.getOrCreateTile(structureTileX, structureTileY, onSurfaced)) == null) {
            aPerformer.getCommunicator().sendNormalServerMessage("The structural integrity of the building is at risk.");
            return true;
        }
        if (!MethodsStructure.mayModifyStructure(aPerformer, structure, vtile, (short)116)) {
            aPerformer.getCommunicator().sendNormalServerMessage(" You are not allowed to add to " + structure.getName() + " without permission.");
            return true;
        }
        if (!Methods.isActionAllowed(aPerformer, (short)116, structureTileX, structureTileY)) {
            return true;
        }
        Wall[] walls = vtile.getWalls();
        for (int s = 0; s < walls.length; ++s) {
            if (walls[s].getStartX() != aTileX || walls[s].getStartY() != aTileY || (!walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_HORIZ) && (walls[s].isHorizontal() || dir != Tiles.TileBorderDirection.DIR_DOWN)) continue;
            if (walls[s].getHeight() == heightOffset) {
                aPerformer.getCommunicator().sendNormalServerMessage("There is already a wall here!");
                logger.log(Level.WARNING, "Wall already exists here: " + structure.getWurmId());
                return true;
            }
            if (walls[s].getHeight() != heightOffset - 30 || walls[s].isFinished()) continue;
            aPerformer.getCommunicator().sendNormalServerMessage("You need to finish the " + walls[s].getName() + " below first.");
            return true;
        }
        int xstart = aTileX;
        int ystart = aTileY;
        if (dir == Tiles.TileBorderDirection.DIR_DOWN) {
            xend = aTileX;
            yend = aTileY + 1;
        } else if (dir == Tiles.TileBorderDirection.DIR_HORIZ) {
            xend = aTileX + 1;
            yend = aTileY;
        } else {
            aPerformer.getCommunicator().sendNormalServerMessage("You don't know how to build a wall in that direction.");
            return true;
        }
        StructureTypeEnum wallType = StructureTypeEnum.PLAN;
        StructureMaterialEnum wallMaterial = StructureMaterialEnum.WOOD;
        if (aSource.getTemplateId() == 493) {
            wallMaterial = StructureMaterialEnum.STONE;
        }
        float qualityLevel = MethodsStructure.calculateNewQualityLevel(act.getPower(), craftSkill.getRealKnowledge(), 0.0f, 21);
        DbWall wallInner = new DbWall(wallType, structureTileX, structureTileY, xstart, ystart, xend, yend, qualityLevel, structure.getWurmId(), wallMaterial, true, heightOffset, structure.getLayer());
        ((Wall)wallInner).setState(StructureStateEnum.INITIALIZED);
        vtile.addWall(wallInner);
        vtile.updateWall(wallInner);
        aPerformer.getCommunicator().sendNormalServerMessage("You plan a wall for " + structure.getName() + ".");
        return true;
    }

    public static Structure getStructureOrNullAtTileBorder(int tilex, int tiley, Tiles.TileBorderDirection dir, boolean surfaced) {
        VolaTile vtile = null;
        return MethodsStructure.getStructureOrNullAtTileBorder(tilex, tiley, dir, surfaced, vtile);
    }

    public static Structure getStructureOrNullAtTileBorder(int tilex, int tiley, Tiles.TileBorderDirection dir, boolean surfaced, @Nullable VolaTile vtile) {
        Structure lstructure;
        vtile = Zones.getTileOrNull(tilex, tiley, surfaced);
        Structure structure = null;
        if (vtile != null && (lstructure = vtile.getStructure()) != null && lstructure.isTypeHouse()) {
            structure = lstructure;
        }
        if (structure == null && (dir == Tiles.TileBorderDirection.DIR_DOWN || dir == Tiles.TileBorderDirection.CORNER) && (vtile = Zones.getTileOrNull(tilex - 1, tiley, surfaced)) != null && (lstructure = vtile.getStructure()) != null && lstructure.isTypeHouse()) {
            structure = lstructure;
        }
        if (structure == null && (dir == Tiles.TileBorderDirection.DIR_HORIZ || dir == Tiles.TileBorderDirection.CORNER) && (vtile = Zones.getTileOrNull(tilex, tiley - 1, surfaced)) != null && (lstructure = vtile.getStructure()) != null && lstructure.isTypeHouse()) {
            structure = lstructure;
        }
        return structure;
    }

    public static boolean buildFence(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, Tiles.TileBorderDirection dir, long borderId, short action, float counter) {
        if (source == null) {
            performer.getCommunicator().sendNormalServerMessage("You need an activated tool to plan that fence.");
            return true;
        }
        int templateId = source.getTemplateId();
        if (!MethodsStructure.isCorrectToolForBuilding(performer, templateId)) {
            performer.getCommunicator().sendNormalServerMessage("You need to use the right tool to do that.");
            return true;
        }
        if (MethodsStructure.doesTileBorderContainWallOrFence(tilex, tiley, heightOffset, dir, onSurface, true)) {
            performer.getCommunicator().sendNormalServerMessage("There is already a wall or fence there.");
            return true;
        }
        if (MethodsStructure.doesTileBorderContainUnfinishedWallOrFenceBelow(tilex, tiley, heightOffset, dir, onSurface, true)) {
            performer.getCommunicator().sendNormalServerMessage("There is an unfinished wall or fence below which you need to finish first.");
            return true;
        }
        VolaTile vtile = null;
        Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(tilex, tiley, dir, onSurface, vtile);
        if (structure != null && !MethodsStructure.mayModifyStructure(performer, structure, vtile, (short)116)) {
            performer.getCommunicator().sendNormalServerMessage(" You are not allowed to add to " + structure.getName() + " without permission.");
            return true;
        }
        if (!onSurface) {
            StructureConstantsEnum fencePlanType = Fence.getFencePlanType(action);
            StructureConstantsEnum fenceType = Fence.getFenceForPlan(fencePlanType);
            float heightClearanceNeeded = WallConstants.getCollisionHeight(fenceType);
            boolean ceilingTooClose = false;
            int tile = Server.caveMesh.getTile(tilex, tiley);
            short ht = (short)(Tiles.decodeData(tile) & 0xFF);
            if ((float)ht < heightClearanceNeeded * 10.0f) {
                ceilingTooClose = true;
            } else {
                tile = dir == Tiles.TileBorderDirection.DIR_HORIZ ? Server.caveMesh.getTile(tilex + 1, tiley) : Server.caveMesh.getTile(tilex, tiley + 1);
                ht = (short)(Tiles.decodeData(tile) & 0xFF);
                if ((float)ht < heightClearanceNeeded * 10.0f) {
                    ceilingTooClose = true;
                }
            }
            if (ceilingTooClose) {
                if (ht == 0) {
                    performer.getCommunicator().sendNormalServerMessage("Fences cannot be built next to a cave exit.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("The ceiling is too close for that type of fence.");
                }
                return true;
            }
        }
        return MethodsStructure.startFenceSection(performer, source, dir, tilex, tiley, onSurface, heightOffset, borderId, action, counter, performer.getPower() >= 2);
    }

    public static boolean mayModifyStructure(Creature performer, Structure structure, @Nullable VolaTile tile, short action) {
        Village village;
        if (performer.getPower() > 1) {
            return true;
        }
        if (structure.isTypeHouse()) {
            Village village2;
            long ownerId = structure.getOwnerId();
            byte k = Players.getInstance().getKingdomForPlayer(ownerId);
            if (ownerId != -10L && !performer.isFriendlyKingdom(k)) {
                return false;
            }
            if (structure.isEnemy(performer) && structure.isEnemyAllowed(performer, action) || structure.mayModify(performer)) {
                return true;
            }
            return tile != null && !structure.isFinished() && (village2 = tile.getVillage()) != null && village2.isCitizen(performer.getWurmId()) && village2.isActionAllowed(action, performer);
        }
        if (tile != null && (village = tile.getVillage()) != null) {
            return village.isActionAllowed(action, performer);
        }
        return true;
    }

    public static Structure getStructureAt(int tilex, int tiley, boolean surfaced) {
        Structure structure = null;
        try {
            Zone zone = Zones.getZone(tilex, tiley, surfaced);
            VolaTile tile = zone.getOrCreateTile(tilex, tiley);
            structure = tile.getStructure();
            if (structure == null) {
                return null;
            }
            return structure;
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, nsz.getMessage());
            return structure;
        }
    }

    public static List<Structure> getStructuresNear(int tilex, int tiley, boolean surfaced) {
        ArrayList<Structure> structures = new ArrayList<Structure>();
        for (int x = 1; x >= -1; --x) {
            for (int y = 1; y >= -1; --y) {
                Structure structure = MethodsStructure.getStructureAt(tilex + x, tiley + y, surfaced);
                if (structure == null || !structure.isTypeHouse() || structures.contains(structure)) continue;
                structures.add(structure);
            }
        }
        return structures;
    }

    public static final boolean wouldBuildOnStructure(Structure structure, int tilex, int tiley, boolean surfaced) {
        Structure struct = MethodsStructure.getStructureAt(tilex, tiley, surfaced);
        if (struct != null) {
            return true;
        }
        List<Structure> structures = MethodsStructure.getStructuresNear(tilex, tiley, surfaced);
        if (structures == null) {
            return false;
        }
        if (structures.isEmpty()) {
            return false;
        }
        return !structures.contains(structure);
    }

    public static final boolean tileBordersToHouse(int tilex, int tiley, boolean surfaced) {
        List<Structure> structures = MethodsStructure.getStructuresNear(tilex, tiley, surfaced);
        if (structures == null) {
            return false;
        }
        return !structures.isEmpty();
    }

    public static boolean removeHouseTile(Creature performer, int tilex, int tiley, int tile, float counter) {
        if (!Methods.isActionAllowed(performer, (short)116, tilex, tiley)) {
            return true;
        }
        VolaTile volaTile = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
        if (MethodsStructure.hasFloors(volaTile)) {
            performer.getCommunicator().sendNormalServerMessage("You must remove existing floor and roofs first, before you can tear down this part of the building.");
            return true;
        }
        if (MethodsStructure.hasWalls(volaTile)) {
            performer.getCommunicator().sendNormalServerMessage("You must destroy adjacent walls first, before you can tear down this part of the building.");
            return true;
        }
        if (MethodsStructure.hasFences(volaTile)) {
            performer.getCommunicator().sendNormalServerMessage("You must destroy adjacent fences first, before you can tear down this part of the building.");
            return true;
        }
        if (MethodsStructure.hasBridgeEntrance(volaTile)) {
            performer.getCommunicator().sendNormalServerMessage("You must destroy the bridge that has an entrance on this tile first, before you can tear down this part of the building.");
            return true;
        }
        Structure structureToShrink = MethodsStructure.getStructureAt(tilex, tiley, performer.isOnSurface());
        if (structureToShrink == null || !structureToShrink.isTypeHouse()) {
            performer.getCommunicator().sendNormalServerMessage("Could not find a building there.");
            return true;
        }
        if (structureToShrink.getSize() == 1) {
            performer.getCommunicator().sendNormalServerMessage("Cannot remove the last part of a building doing that, please use the writ to destroy the building.");
            return true;
        }
        try {
            Structure plannedStructure = performer.getStructure();
            if (plannedStructure != null && structureToShrink.getWurmId() != plannedStructure.getWurmId()) {
                performer.getCommunicator().sendNormalServerMessage("You already have a " + (plannedStructure.isTypeBridge() ? "bridge" : "building") + " under construction. Finish that one before trying to work here.");
                return true;
            }
        }
        catch (NoSuchStructureException noSuchStructureException) {
            // empty catch block
        }
        if (!MethodsStructure.mayModifyStructure(performer, structureToShrink, volaTile, (short)82)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to shrink " + structureToShrink.getName() + ".");
            return true;
        }
        if (!structureToShrink.testRemove(volaTile)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot remove this part of the building. Your building must connect everywhere.");
            return true;
        }
        if (!structureToShrink.contains(tilex, tiley)) {
            performer.getCommunicator().sendNormalServerMessage("There is no structure there to modify.");
            return true;
        }
        if (!structureToShrink.removeTileFromFinishedStructure(performer, tilex, tiley, performer.isOnSurface() ? 0 : -1)) {
            performer.getCommunicator().sendNormalServerMessage("You can't divide the house in different parts.");
            return true;
        }
        if (!MethodsStructure.hasEnoughSkillToContractStructure(performer, tilex, tiley, structureToShrink)) {
            return true;
        }
        structureToShrink.updateStructureFinishFlag();
        performer.getCommunicator().sendNormalServerMessage("You remove this area from the building.");
        return true;
    }

    public static void removeBuildMarker(Structure structure, int tilex, int tiley) {
        VolaTile tile = Zones.getTileOrNull(tilex, tiley, structure.isOnSurface());
        if (tile == null) {
            return;
        }
        tile.removeBuildMarker(structure, tilex, tiley);
    }

    static final boolean hasFences(VolaTile tile) {
        Fence[] fences;
        Structure struct = tile.getStructure();
        boolean isBadFence = false;
        for (Fence fence : fences = tile.getAllFences()) {
            if (fence.isSupportedByGround()) continue;
            if (fence.isHorizontal() && fence.getStartY() == tile.getTileY() && fence.getEndY() == tile.getTileY() && fence.getStartX() == tile.getTileX() && fence.getEndX() == tile.getTileX() + 1 && !struct.contains(tile.getTileX(), tile.getTileY() - 1)) {
                isBadFence = true;
            }
            if (fence.isHorizontal() && fence.getStartY() == tile.getTileY() + 1 && fence.getEndY() == tile.getTileY() + 1 && fence.getStartX() == tile.getTileX() && fence.getEndX() == tile.getTileX() + 1 && !struct.contains(tile.getTileX(), tile.getTileY() + 1)) {
                isBadFence = true;
            }
            if (!fence.isHorizontal() && fence.getStartX() == tile.getTileX() + 1 && fence.getEndX() == tile.getTileX() + 1 && fence.getStartY() == tile.getTileY() && fence.getEndY() == tile.getTileY() + 1 && !struct.contains(tile.getTileX() + 1, tile.getTileY())) {
                isBadFence = true;
            }
            if (fence.isHorizontal() || fence.getStartX() != tile.getTileX() || fence.getEndX() != tile.getTileX() || fence.getStartY() != tile.getTileY() || fence.getEndY() != tile.getTileY() + 1 || struct.contains(tile.getTileX() - 1, tile.getTileY())) continue;
            isBadFence = true;
        }
        return isBadFence;
    }

    static final boolean hasWalls(VolaTile tile) {
        Wall[] walls = tile.getWalls();
        Structure struct = tile.getStructure();
        if (walls.length == 0) {
            return false;
        }
        for (Wall wall : walls) {
            VolaTile t;
            if (wall.isHorizontal() && wall.getStartY() == tile.getTileY() && wall.getEndY() == tile.getTileY() && wall.getStartX() == tile.getTileX() && wall.getEndX() == tile.getTileX() + 1) {
                if (struct.contains(tile.getTileX(), tile.getTileY() - 1)) {
                    t = Zones.getTileOrNull(tile.getTileX(), tile.getTileY() - 1, tile.isOnSurface());
                    if (t != null) {
                        tile.removeWall(wall, true);
                        wall.setTile(tile.getTileX(), tile.getTileY() - 1);
                        t.addWall(wall);
                    }
                } else if (wall.getType() != StructureTypeEnum.PLAN) {
                    return true;
                }
            }
            if (wall.isHorizontal() && wall.getStartY() == tile.getTileY() + 1 && wall.getEndY() == tile.getTileY() + 1 && wall.getStartX() == tile.getTileX() && wall.getEndX() == tile.getTileX() + 1) {
                if (struct.contains(tile.getTileX(), tile.getTileY() + 1)) {
                    t = Zones.getTileOrNull(tile.getTileX(), tile.getTileY() + 1, tile.isOnSurface());
                    if (t != null) {
                        tile.removeWall(wall, true);
                        wall.setTile(tile.getTileX(), tile.getTileY() + 1);
                        t.addWall(wall);
                    }
                } else if (wall.getType() != StructureTypeEnum.PLAN) {
                    return true;
                }
            }
            if (!wall.isHorizontal() && wall.getStartX() == tile.getTileX() + 1 && wall.getEndX() == tile.getTileX() + 1 && wall.getStartY() == tile.getTileY() && wall.getEndY() == tile.getTileY() + 1) {
                if (struct.contains(tile.getTileX() + 1, tile.getTileY())) {
                    t = Zones.getTileOrNull(tile.getTileX() + 1, tile.getTileY(), tile.isOnSurface());
                    if (t != null) {
                        tile.removeWall(wall, true);
                        wall.setTile(tile.getTileX() + 1, tile.getTileY());
                        t.addWall(wall);
                    }
                } else if (wall.getType() != StructureTypeEnum.PLAN) {
                    return true;
                }
            }
            if (wall.isHorizontal() || wall.getStartX() != tile.getTileX() || wall.getEndX() != tile.getTileX() || wall.getStartY() != tile.getTileY() || wall.getEndY() != tile.getTileY() + 1) continue;
            if (struct.contains(tile.getTileX() - 1, tile.getTileY())) {
                t = Zones.getTileOrNull(tile.getTileX() - 1, tile.getTileY(), tile.isOnSurface());
                if (t == null) continue;
                tile.removeWall(wall, true);
                wall.setTile(tile.getTileX() - 1, tile.getTileY());
                t.addWall(wall);
                continue;
            }
            if (wall.getType() == StructureTypeEnum.PLAN) continue;
            return true;
        }
        return false;
    }

    static final boolean hasFloors(VolaTile tile) {
        Floor[] floors;
        for (Floor floor : floors = tile.getFloors()) {
            if (floor.getFloorState() == StructureConstants.FloorState.PLANNING) continue;
            return true;
        }
        return false;
    }

    static final boolean hasBridgeEntrance(VolaTile tile) {
        BridgePart[] bps;
        Structure structWest;
        BridgePart[] bps2;
        Structure structSouth;
        BridgePart[] bps3;
        Structure structEast;
        BridgePart[] bps4;
        Structure structNorth;
        VolaTile vtNorth = Zones.getTileOrNull(tile.getTileX(), tile.getTileY() - 1, tile.isOnSurface());
        if (vtNorth != null && (structNorth = vtNorth.getStructure()) != null && structNorth.isTypeBridge() && (bps4 = vtNorth.getBridgeParts()).length == 1 && bps4[0].hasHouseSouthExit()) {
            return true;
        }
        VolaTile vtEast = Zones.getTileOrNull(tile.getTileX() + 1, tile.getTileY(), tile.isOnSurface());
        if (vtEast != null && (structEast = vtEast.getStructure()) != null && structEast.isTypeBridge() && (bps3 = vtEast.getBridgeParts()).length == 1 && bps3[0].hasHouseWestExit()) {
            return true;
        }
        VolaTile vtSouth = Zones.getTileOrNull(tile.getTileX(), tile.getTileY() + 1, tile.isOnSurface());
        if (vtSouth != null && (structSouth = vtSouth.getStructure()) != null && structSouth.isTypeBridge() && (bps2 = vtSouth.getBridgeParts()).length == 1 && bps2[0].hasHouseNorthExit()) {
            return true;
        }
        VolaTile vtWest = Zones.getTileOrNull(tile.getTileX() - 1, tile.getTileY(), tile.isOnSurface());
        return vtWest != null && (structWest = vtWest.getStructure()) != null && structWest.isTypeBridge() && (bps = vtWest.getBridgeParts()).length == 1 && bps[0].hasHouseEastExit();
    }

    public static boolean tileisNextToStructure(VolaTile[] structureTiles, int tilex, int tiley) {
        if (structureTiles.length <= 0) {
            return true;
        }
        for (VolaTile tile : structureTiles) {
            if (!(tiley == tile.getTileY() ? tilex == tile.getTileX() + 1 || tilex == tile.getTileX() - 1 : tilex == tile.getTileX() && (tiley == tile.getTileY() + 1 || tiley == tile.getTileY() - 1))) continue;
            return true;
        }
        return false;
    }

    public static boolean hasOtherStructureNear(Structure structure, int tilex, int tiley, boolean surfaced) {
        List<Structure> structsNear = MethodsStructure.getStructuresNear(tilex, tiley, surfaced);
        if (structure == null && !structsNear.isEmpty()) {
            return true;
        }
        if (structure != null) {
            for (Structure struct : structsNear) {
                if (struct.getWurmId() == structure.getWurmId()) continue;
                return true;
            }
        }
        return false;
    }

    static final float calculateNewQualityLevel(float materialPower, double realKnowledge, float oldql, int needed) {
        float qualityLevel = (float)Math.min((double)(materialPower + oldql), (double)oldql + realKnowledge / (double)((float)needed + 1.0f));
        qualityLevel = Math.max(1.0f, qualityLevel);
        return qualityLevel;
    }

    private static int getSkillFor(IFloor floor) {
        if (floor instanceof Floor) {
            return FloorBehaviour.getSkillForFloor(((Floor)floor).getMaterial());
        }
        return BridgePartBehaviour.getRequiredSkill(((BridgePart)floor).getMaterial());
    }

    public static boolean isWritHolder(Creature performer, long structureId) {
        try {
            Structure structure = Structures.getStructure(structureId);
            long writId = structure.getWritId();
            if (writId != -10L) {
                Item[] items = performer.getKeys();
                for (int x = 0; x < items.length; ++x) {
                    if (items[x].getWurmId() != writId) continue;
                    return true;
                }
            }
        }
        catch (NoSuchStructureException nss) {
            return false;
        }
        return false;
    }

    static final boolean rotateFloor(Creature performer, Floor floor, float counter, Action act) {
        boolean insta = performer.getPower() >= 4;
        VolaTile floorTile = Zones.getOrCreateTile(floor.getTileX(), floor.getTileY(), floor.isOnSurface());
        if (floorTile == null) {
            return true;
        }
        Structure structure = floorTile.getStructure();
        if (!insta && !MethodsStructure.mayModifyStructure(performer, structure, floorTile, act.getNumber())) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
            return true;
        }
        if (!floor.isFinished() && !floor.getType().isStair()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot rotate an unfinished floor.");
            return true;
        }
        boolean toReturn = false;
        int time = 40;
        if (counter == 1.0f) {
            if (!Methods.isActionAllowed(performer, (short)116, floor.getTileX(), floor.getTileY())) {
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("You start to rotate the " + floor.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to rotate a " + floor.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, time);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-300.0f);
        } else {
            time = act.getTimeLeft();
        }
        if (act.mayPlaySound()) {
            String s;
            String string = s = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            if (floor.isStone() || floor.isMarble() || floor.isSlate() || floor.isSandstone()) {
                s = "sound.work.masonry";
            } else if (floor.isMetal()) {
                s = "sound.work.smithing.metal";
            }
            SoundPlayer.playSound(s, performer, 1.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-700.0f);
        } else if (counter * 10.0f > (float)time || insta) {
            if (act.getNumber() == 177) {
                floor.rotate(2);
            } else {
                floor.rotate(-2);
            }
            performer.getCommunicator().sendNormalServerMessage("You rotate the " + floor.getName() + " through 90 degrees.");
            floor.setLastUsed(System.currentTimeMillis());
            Server.getInstance().broadCastAction(performer.getName() + " rotates a " + floor.getName() + " a bit.", performer, 5);
            toReturn = true;
        }
        return toReturn;
    }

    private static boolean needSurroundingTilesFloors(Creature performer, int tilex, int tiley) {
        VolaTile vt;
        if (Features.Feature.CAVE_DWELLINGS.isEnabled() && !performer.isOnSurface() && (vt = Zones.getOrCreateTile(tilex, tiley, false)).getVillage() == null) {
            for (int x = -1; x <= 1; ++x) {
                for (int y = -1; y <= 1; ++y) {
                    int ttile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    byte ttype = Tiles.decodeType(ttile);
                    if (Tiles.isReinforcedFloor(ttype) || ttype == Tiles.Tile.TILE_CAVE.id) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasInsideFence(Wall wall) {
        VolaTile vt = wall.getTile();
        for (Fence f : vt.getAllFences()) {
            if (f.getStartX() != wall.getStartX() || f.getStartY() != wall.getStartY() || f.getEndX() != wall.getEndX() || f.getEndY() != wall.getEndY() || f.getHeightOffset() != wall.getHeight()) continue;
            return true;
        }
        return false;
    }
}

