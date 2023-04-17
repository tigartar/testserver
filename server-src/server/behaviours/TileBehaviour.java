/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.Emotes;
import com.wurmonline.server.behaviours.Flattening;
import com.wurmonline.server.behaviours.FloorBehaviour;
import com.wurmonline.server.behaviours.Forage;
import com.wurmonline.server.behaviours.Herb;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.MethodsFishing;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.behaviours.WurmPermissions;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Order;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.EpicTargetItems;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.Valrei;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.FragmentUtilities;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.questions.TeamManagementQuestion;
import com.wurmonline.server.questions.TestQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcEpicStatusReport;
import com.wurmonline.server.zones.EncounterType;
import com.wurmonline.server.zones.ErrorChecks;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.HiveZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.SpawnTable;
import com.wurmonline.server.zones.TilePoller;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.TurretZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.StructureConstants;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

class TileBehaviour
extends Behaviour
implements ItemTypes,
MiscConstants,
ItemMaterials,
TimeConstants {
    private static final Logger logger = Logger.getLogger(TileBehaviour.class.getName());
    private static final int MIN_SKILL_FORAGE_STEPPE = 23;
    private static final int MIN_SKILL_BOTANIZE_MARSH = 27;
    private static final int MIN_SKILL_FORAGE_TUNDRA = 33;
    private static final int MIN_SKILL_BOTANIZE_MOSS = 35;
    private static final int MIN_SKILL_FORAGE_MARSH = 43;
    private static final int MIN_SKILL_BOTANIZE_PEAT = 42;
    static final Random r = new Random();

    TileBehaviour() {
        super((short)5);
    }

    TileBehaviour(short type) {
        super(type);
    }

    public boolean isCave() {
        return false;
    }

    public static void sendVillageString(Creature performer, int tilex, int tiley, boolean surfaced) {
        VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, surfaced);
        Communicator comm = performer.getCommunicator();
        if (vtile.getVillage() != null) {
            comm.sendNormalServerMessage("This is within the village of " + vtile.getVillage().getName() + ".");
        } else {
            Village v = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
            if (v != null) {
                comm.sendNormalServerMessage("This is within the perimeter of " + v.getName() + ".");
            }
        }
        if (vtile.getStructure() != null) {
            comm.sendNormalServerMessage("This is within the structure of " + vtile.getStructure().getName() + ".");
            if (performer.getPower() > 0) {
                comm.sendNormalServerMessage(vtile.getStructure().getName() + " at " + vtile.getStructure().getCenterX() + ", " + vtile.getStructure().getCenterY() + " has wurmid " + vtile.getStructure().getWurmId());
            }
        }
    }

    @Override
    @Nonnull
    public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item source, int tilex, int tiley, boolean onSurface, int tile) {
        MineDoorPermission md;
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, source, tilex, tiley, onSurface, tile));
        byte type = Tiles.decodeType(tile);
        byte data = Tiles.decodeData(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        int templateId = source.getTemplateId();
        if (!source.isTraded()) {
            boolean water;
            MineDoorPermission md2;
            HighwayPos highwaypos;
            if (source.getTemplateId() == 176 && performer.getPower() >= 2) {
                toReturn.add(Actions.actionEntrys[604]);
            }
            if (source.isDiggingtool() && Terraforming.isPackable(type)) {
                toReturn.add(Actions.actionEntrys[154]);
            }
            if (source.isDiggingtool() && onSurface && !Terraforming.isNonDiggableTile(type)) {
                toReturn.add(Actions.actionEntrys[144]);
                if (TileBehaviour.isCloseTile(performer.getTileX(), performer.getTileY(), tilex, tiley)) {
                    toReturn.add(Actions.actionEntrys[150]);
                }
                if (TileBehaviour.isAdjacentTile(performer.getTileX(), performer.getTileY(), tilex, tiley)) {
                    toReturn.add(Actions.actionEntrys[532]);
                }
            }
            if (source.isDredgingTool() && onSurface && !Terraforming.isNonDiggableTile(type)) {
                toReturn.add(Actions.actionEntrys[362]);
                if (TileBehaviour.isCloseTile(performer.getTileX(), performer.getTileY(), tilex, tiley)) {
                    toReturn.add(Actions.actionEntrys[150]);
                }
                if (TileBehaviour.isAdjacentTile(performer.getTileX(), performer.getTileY(), tilex, tiley)) {
                    toReturn.add(Actions.actionEntrys[532]);
                }
            }
            if (source.isTrap()) {
                if (Trap.mayTrapTemplateOnTile(templateId, type)) {
                    if (Trap.getTrap(tilex, tiley, performer.getLayer()) == null) {
                        toReturn.add(Actions.actionEntrys[374]);
                    }
                } else if (templateId == 612 && Trap.mayPlantCorrosion(tilex, tiley, performer.getLayer()) && Trap.getTrap(tilex, tiley, performer.getLayer()) == null) {
                    toReturn.add(Actions.actionEntrys[374]);
                }
            } else if (source.isDisarmTrap()) {
                toReturn.add(Actions.actionEntrys[375]);
            }
            if (onSurface && source.isDiggingtool() && (Terraforming.isRoad(type) || type == Tiles.Tile.TILE_PLANKS.id || type == Tiles.Tile.TILE_PLANKS_TARRED.id)) {
                highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
                if (!MethodsHighways.onHighway(highwaypos)) {
                    toReturn.add(Actions.actionEntrys[191]);
                }
            } else if (templateId == 153 && Tiles.decodeType(tile) == Tiles.Tile.TILE_PLANKS.id && onSurface) {
                toReturn.add(new ActionEntry(231, "Tar", "tarring"));
            }
            if (onSurface && Tiles.isRoadType(type) && source.isPaveable() && source.getTemplateId() != 495 && MethodsHighways.onHighway(highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface))) {
                toReturn.add(new ActionEntry(-2, "Re-Pave", "re-paving", emptyIntArr));
                toReturn.add(new ActionEntry(155, "Replace paving", "re-paving"));
                toReturn.add(new ActionEntry(576, "Replace using nearest corner", "re-paving"));
            }
            if (type == Tiles.Tile.TILE_DIRT_PACKED.id) {
                if (source.isPaveable() && source.getTemplateId() != 495) {
                    toReturn.add(new ActionEntry(-2, "Pave", "paving", emptyIntArr));
                    toReturn.add(Actions.actionEntrys[155]);
                    toReturn.add(Actions.actionEntrys[576]);
                }
            } else if (type == Tiles.Tile.TILE_MARSH.id && templateId == 495) {
                toReturn.add(new ActionEntry(-2, "Lay boards", "paving", emptyIntArr));
                toReturn.add(new ActionEntry(155, "Over marsh", "laying", new int[]{43}));
                toReturn.add(new ActionEntry(576, "In nearest corner", "laying", new int[]{43}));
            }
            if (Terraforming.isCultivatable(type) && (templateId == 27 || templateId == 25)) {
                toReturn.add(Actions.actionEntrys[318]);
            }
            if ((templateId == 1115 || source.isWand() && Tiles.isMineDoor(type)) && (md2 = MineDoorPermission.getPermission(tilex, tiley)) != null && (md2.mayManage(performer) || md2.isActualOwner(performer.getWurmId()))) {
                toReturn.add(new ActionEntry(906, "Remove mine door", "removing"));
            }
            if (Terraforming.isSwitchableTiles(templateId, type)) {
                toReturn.add(Actions.actionEntrys[927]);
            }
            if ((source.isWeapon() || source.isWand()) && Tiles.isMineDoor(type)) {
                toReturn.add(new ActionEntry(174, "Destroy door", "destroying"));
            }
            if (Terraforming.isBuildTile(type) && onSurface) {
                toReturn.addAll(TileBehaviour.getBuildableTileBehaviours(tilex, tiley, performer, templateId));
            }
            if (source.getTemplate().isRune()) {
                Skill soulDepth = performer.getSoulDepth();
                double diff = (double)(20.0f + source.getDamage()) - ((double)(source.getCurrentQualityLevel() + (float)source.getRarity()) - 45.0);
                double chance = soulDepth.getChance(diff, null, source.getCurrentQualityLevel());
                if (RuneUtilities.isSingleUseRune(source) && RuneUtilities.getSpellForRune(source) != null && RuneUtilities.getSpellForRune(source).isTargetTile()) {
                    toReturn.add(new ActionEntry(945, "Use Rune: " + chance + "%", "using rune", emptyIntArr));
                }
            }
            if (onSurface && (source.getTemplate().isDiggingtool() || source.getTemplateId() == 493)) {
                if (source.getTemplateId() == 493) {
                    toReturn.add(Actions.actionEntrys[910]);
                } else {
                    Skill arch = performer.getSkills().getSkillOrLearn(10069);
                    if (arch.getKnowledge(0.0) >= 20.0) {
                        toReturn.add(Actions.actionEntrys[910]);
                    }
                }
            }
            if (templateId == 174 || templateId == 524 || templateId == 525) {
                int tx = source.getData1();
                int ty = source.getData2();
                if (tx != -1 && ty != -1) {
                    toReturn.add(Actions.actionEntrys[95]);
                }
                if (templateId == 174 || templateId == 524) {
                    toReturn.add(Actions.actionEntrys[94]);
                }
            }
            toReturn.addAll(this.getTileAndFloorBehavioursFor(performer, source, tilex, tiley, tile));
            if (templateId == 489 || WurmPermissions.mayUseGMWand(performer) && (templateId == 176 || templateId == 315)) {
                toReturn.add(Actions.actionEntrys[329]);
            } else if (performer.getCultist() != null) {
                if (performer.getCultist().mayInfoLocal()) {
                    toReturn.add(Actions.actionEntrys[185]);
                }
                if ((type == Tiles.Tile.TILE_LAVA.id || type == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) && performer.getCultist().maySpawnVolcano()) {
                    toReturn.add(new ActionEntry(78, "Freeze", "freezing"));
                }
            }
            if (templateId == 602) {
                toReturn.add(Actions.actionEntrys[369]);
            }
            if (water = Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                if (source.getTemplateId() == 1343 || source.getTemplateId() == 705 || source.getTemplateId() == 707 || source.getTemplateId() == 1344 || source.getTemplateId() == 1346) {
                    toReturn.add(Actions.actionEntrys[160]);
                }
                if (source.isContainerLiquid() && !source.isSealedByPlayer()) {
                    toReturn.add(Actions.actionEntrys[189]);
                }
                toReturn.add(Actions.actionEntrys[19]);
                toReturn.add(Actions.actionEntrys[183]);
                if (performer.getDeity() != null && performer.getDeity().isWaterGod()) {
                    Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
                }
                if (source.getTemplateId() == 1344 || source.getTemplateId() == 1343 || source.getTemplateId() == 705 || source.getTemplateId() == 707 || source.getTemplateId() == 1346) {
                    toReturn.add(new ActionEntry(285, "Lore", "thinking"));
                }
            }
            if (performer.getVehicle() != -10L) {
                Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
                VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
                if (t == null || t.getStructure() == null) {
                    if (vehicle.isChair()) {
                        toReturn.add(Actions.actionEntrys[708]);
                    } else {
                        toReturn.add(Actions.actionEntrys[333]);
                    }
                }
            }
            if (performer.getKingdomTemplateId() == 3 && Tiles.getTile(type).isMycelium()) {
                toReturn.add(Actions.actionEntrys[347]);
            }
            if (Features.Feature.TRANSFORM_RESOURCE_TILES.isEnabled() && source.getTemplateId() == 654 && source.getAuxData() != 0 && source.getBless() != null) {
                if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled() && (source.getAuxData() == 1 && type == Tiles.Tile.TILE_SAND.id || source.getAuxData() == 2 && type == Tiles.Tile.TILE_GRASS.id || source.getAuxData() == 2 && type == Tiles.Tile.TILE_MYCELIUM.id || source.getAuxData() == 3 && type == Tiles.Tile.TILE_STEPPE.id || source.getAuxData() == 7 && type == Tiles.Tile.TILE_MOSS.id)) {
                    toReturn.add(new ActionEntry(-1, "Alchemy", "Alchemy"));
                    toReturn.add(Actions.actionEntrys[462]);
                } else if (source.getAuxData() == 4 && type == Tiles.Tile.TILE_CLAY.id || source.getAuxData() == 5 && type == Tiles.Tile.TILE_PEAT.id || source.getAuxData() == 6 && type == Tiles.Tile.TILE_TAR.id || source.getAuxData() == 8 && type == Tiles.Tile.TILE_TUNDRA.id) {
                    toReturn.add(new ActionEntry(-1, "Alchemy", "Alchemy"));
                    toReturn.add(Actions.actionEntrys[462]);
                }
            }
        }
        if (Tiles.isMineDoor(type) && (md = MineDoorPermission.getPermission(tilex, tiley)) != null) {
            LinkedList<ActionEntry> permissions = new LinkedList<ActionEntry>();
            if (md.mayManage(performer) || md.isActualOwner(performer.getWurmId())) {
                permissions.add(Actions.actionEntrys[364]);
            }
            if (md.mayManage(performer) && type != Tiles.Tile.TILE_MINE_DOOR_STONE.id && source.getTemplateId() == MethodsStructure.getImproveItem(type) && !source.isTraded()) {
                toReturn.add(Actions.actionEntrys[192]);
            }
            if (md.maySeeHistory(performer)) {
                permissions.add(new ActionEntry(691, "History of Mine Door", "viewing"));
            }
            if (!permissions.isEmpty()) {
                if (permissions.size() > 1) {
                    Collections.sort(permissions);
                    toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
                }
                toReturn.addAll(permissions);
            }
        }
        if (type != Tiles.Tile.TILE_GRASS.id && type != Tiles.Tile.TILE_MYCELIUM.id && !theTile.isBush() && !theTile.isTree()) {
            LinkedList<ActionEntry> nature = new LinkedList<ActionEntry>();
            toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
        }
        TileBehaviour.addEmotes(toReturn);
        if (performer.isOnSurface() && performer.getPower() >= 2) {
            if (Zones.protectedTiles[tilex][tiley]) {
                toReturn.add(Actions.actionEntrys[382]);
            } else {
                toReturn.add(Actions.actionEntrys[381]);
            }
            toReturn.add(Actions.actionEntrys[476]);
        }
        if ((templateId == 176 || templateId == 315) && WurmPermissions.mayUseGMWand(performer) && performer.getTaggedItemId() != -10L) {
            toReturn.add(new ActionEntry(675, "Summon '" + performer.getTaggedItemName() + "'", "summoning"));
        }
        if (performer.isTeamLeader()) {
            toReturn.add(Actions.actionEntrys[471]);
        }
        if (performer.getTeam() != null) {
            toReturn.add(Actions.actionEntrys[470]);
        }
        return toReturn;
    }

    List<ActionEntry> getNatureMenu(Creature performer, int tilex, int tiley, byte type, byte data, List<ActionEntry> menu) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        int sz = -menu.size();
        boolean canForage = this.canForage(performer, type, data);
        boolean canBotanize = this.canBotanize(performer, type, data);
        boolean canCollect = TileBehaviour.canCollectSnow(performer, tilex, tiley, type, data);
        if (canForage) {
            --sz;
        }
        if (canBotanize) {
            --sz;
        }
        if (sz < 0) {
            toReturn.add(new ActionEntry((short)sz, "Nature", "nature", emptyIntArr));
            toReturn.addAll(menu);
            if (canForage) {
                toReturn.addAll(this.getBehavioursForForage(performer));
            }
            if (canBotanize) {
                toReturn.addAll(this.getBehavioursForBotanize(performer));
            }
        }
        if (canCollect) {
            toReturn.add(Actions.actionEntrys[741]);
        }
        return toReturn;
    }

    public List<ActionEntry> getBehavioursForForage(Creature performer) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        try {
            Skill forage = performer.getSkills().getSkill(10071);
            if (forage.getKnowledge(0.0) > 20.0) {
                toReturn.add(new ActionEntry(-4, "Forage for", "foraging", emptyIntArr));
                toReturn.add(new ActionEntry(223, "Anything", "foraging", new int[]{43}));
                toReturn.add(Actions.actionEntrys[569]);
                toReturn.add(Actions.actionEntrys[570]);
                toReturn.add(Actions.actionEntrys[571]);
            } else {
                toReturn.add(Actions.actionEntrys[223]);
            }
        }
        catch (NoSuchSkillException nss) {
            toReturn.add(Actions.actionEntrys[223]);
        }
        return toReturn;
    }

    public List<ActionEntry> getBehavioursForBotanize(Creature performer) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        try {
            Skill botanize = performer.getSkills().getSkill(10072);
            if (botanize.getKnowledge(0.0) > 20.0) {
                toReturn.add(new ActionEntry(-5, "Botanize for", "botanizing", emptyIntArr));
                toReturn.add(new ActionEntry(224, "Anything", "botanizing", new int[]{43}));
                toReturn.add(Actions.actionEntrys[573]);
                toReturn.add(Actions.actionEntrys[575]);
                toReturn.add(Actions.actionEntrys[572]);
                toReturn.add(Actions.actionEntrys[720]);
            } else {
                toReturn.add(Actions.actionEntrys[224]);
            }
        }
        catch (NoSuchSkillException nss) {
            toReturn.add(Actions.actionEntrys[224]);
        }
        return toReturn;
    }

    @Nonnull
    static List<ActionEntry> getBuildableTileBehaviours(int tilex, int tiley, Creature performer, int toolTemplateId) {
        ArrayList<ActionEntry> toReturn = new ArrayList<ActionEntry>();
        toReturn.addAll(TileBehaviour.getExistingStructureBehaviours(tilex, tiley, performer, toolTemplateId));
        toReturn.addAll(TileBehaviour.getStructurePlanningBehaviours(tilex, tiley, performer, toolTemplateId));
        return toReturn;
    }

    private static List<ActionEntry> getExistingStructureBehaviours(int tilex, int tiley, Creature performer, int toolTemplateId) {
        ArrayList<ActionEntry> toReturn = new ArrayList<ActionEntry>();
        Structure existingStructure = MethodsStructure.getStructureAt(tilex, tiley, performer.isOnSurface());
        if (existingStructure == null) {
            List<Structure> structuresNear;
            if (MethodsStructure.isCorrectToolForPlanning(performer, toolTemplateId) && (structuresNear = MethodsStructure.getStructuresNear(tilex, tiley, performer.isOnSurface())).size() == 1) {
                for (Structure structure : structuresNear) {
                    if (!structure.isTypeHouse() || !structure.isFinalized()) continue;
                    toReturn.add(Actions.actionEntrys[530]);
                }
            }
            return toReturn;
        }
        if (existingStructure.isFinalized() && existingStructure.isTypeHouse()) {
            short groundLevel;
            int performerAtHeight;
            int performerFloorLevel;
            if (MethodsStructure.isCorrectToolForBuilding(performer, toolTemplateId) && (performerFloorLevel = (performerAtHeight = (groundLevel = GeneralUtilities.getHeight(tilex, tiley, performer.isOnSurface())) - (int)performer.getStatus().getPositionZ() * 10) / 30) == 0) {
                toReturn.addAll(FloorBehaviour.getCompletedFloorsBehaviour(false, performer.isOnSurface()));
            }
            if (toolTemplateId == 62 || toolTemplateId == 63 || toolTemplateId == 176 && performer.getPower() >= 3) {
                toReturn.add(Actions.actionEntrys[531]);
            }
        }
        return toReturn;
    }

    private static List<ActionEntry> getStructurePlanningBehaviours(int tilex, int tiley, Creature performer, int toolTemplateId) {
        ArrayList<ActionEntry> toReturn = new ArrayList<ActionEntry>();
        if (!MethodsStructure.isCorrectToolForPlanning(performer, toolTemplateId)) {
            return toReturn;
        }
        if (MethodsStructure.tileBordersToFence(tilex, tiley, 0, performer.isOnSurface())) {
            return toReturn;
        }
        Structure planningStructure = null;
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
        if (planningStructure != null && planningStructure.contains(tilex, tiley) && !planningStructure.isFinalized()) {
            toReturn.add(Actions.actionEntrys[57]);
            toReturn.add(Actions.actionEntrys[58]);
            return toReturn;
        }
        toReturn.add(Actions.actionEntrys[56]);
        return toReturn;
    }

    List<ActionEntry> getTileAndFloorBehavioursFor(Creature performer, Item subject, int tilex, int tiley, int tile) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        byte type = Tiles.decodeType(tile);
        if (subject != null && !subject.isTraded()) {
            int ty;
            int tx;
            int nums;
            int templateId = subject.getTemplateId();
            if (Servers.localServer.testServer || performer.getPower() >= 5) {
                toReturn.add(Actions.actionEntrys[486]);
            }
            if (templateId == 301 && WurmPermissions.mayCreateItems(performer)) {
                toReturn.add(Actions.actionEntrys[148]);
            }
            if (templateId == 176 && WurmPermissions.mayUseDeityWand(performer)) {
                nums = -7;
                tx = subject.getData1();
                ty = subject.getData2();
                if (tx != -1 && ty != -1) {
                    --nums;
                }
                if (Servers.localServer.serverNorth != null) {
                    --nums;
                }
                if (Servers.localServer.serverEast != null) {
                    --nums;
                }
                if (Servers.localServer.serverSouth != null) {
                    --nums;
                }
                if (Servers.localServer.serverWest != null) {
                    --nums;
                }
                if (WurmPermissions.mayCreateItems(performer)) {
                    --nums;
                }
                if (WurmPermissions.mayChangeTile(performer)) {
                    --nums;
                }
                if (performer.getPower() >= 3) {
                    --nums;
                    if (performer.getPower() >= 5) {
                        --nums;
                        --nums;
                        --nums;
                        if (Servers.localServer.testServer) {
                            --nums;
                        }
                    }
                }
                boolean spike = false;
                if (TileBehaviour.isSpike(performer, tilex, tiley, false)) {
                    spike = true;
                    --nums;
                }
                toReturn.add(new ActionEntry((short)nums, "Specials", "specials"));
                toReturn.add(Actions.actionEntrys[64]);
                toReturn.add(Actions.actionEntrys[88]);
                toReturn.add(Actions.actionEntrys[179]);
                toReturn.add(Actions.actionEntrys[34]);
                toReturn.add(Actions.actionEntrys[94]);
                if (tx != -1 && ty != -1) {
                    toReturn.add(Actions.actionEntrys[95]);
                }
                if (Servers.localServer.serverNorth != null) {
                    toReturn.add(Actions.actionEntrys[240]);
                }
                if (Servers.localServer.serverEast != null) {
                    toReturn.add(Actions.actionEntrys[241]);
                }
                if (Servers.localServer.serverSouth != null) {
                    toReturn.add(Actions.actionEntrys[242]);
                }
                if (Servers.localServer.serverWest != null) {
                    toReturn.add(Actions.actionEntrys[243]);
                }
                if (spike) {
                    toReturn.add(Actions.actionEntrys[162]);
                }
                if (WurmPermissions.mayCreateItems(performer)) {
                    toReturn.add(Actions.actionEntrys[148]);
                }
                if (WurmPermissions.mayChangeTile(performer)) {
                    toReturn.add(Actions.actionEntrys[335]);
                }
                if (performer.getStatus().visible) {
                    toReturn.add(Actions.actionEntrys[577]);
                } else {
                    toReturn.add(Actions.actionEntrys[578]);
                }
                if (((Player)performer).GMINVULN) {
                    toReturn.add(Actions.actionEntrys[580]);
                } else {
                    toReturn.add(Actions.actionEntrys[579]);
                }
                if (performer.getPower() >= 2) {
                    toReturn.add(Actions.actionEntrys[185]);
                    if (performer.getPower() >= 5) {
                        toReturn.add(Actions.actionEntrys[90]);
                        toReturn.add(Actions.actionEntrys[194]);
                        toReturn.add(Actions.actionEntrys[352]);
                    }
                    if ((Servers.localServer.testServer || performer.getPower() >= 5) && performer.getPower() >= 3) {
                        toReturn.add(Actions.actionEntrys[483]);
                    }
                }
                toReturn.add(new ActionEntry(-1, "Skills", "Skills stuff"));
                toReturn.add(Actions.actionEntrys[92]);
                try {
                    if (performer.getBody().getWounds() != null && performer.getBody().getWounds().getWounds().length > 0) {
                        toReturn.add(Actions.actionEntrys[346]);
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, performer.getName() + ": " + ex.getMessage(), ex);
                }
            } else if (templateId == 315 && WurmPermissions.mayUseGMWand(performer)) {
                nums = -5;
                tx = subject.getData1();
                ty = subject.getData2();
                if (tx != -1 && ty != -1) {
                    --nums;
                }
                if (Servers.localServer.serverNorth != null) {
                    --nums;
                }
                if (Servers.localServer.serverEast != null) {
                    --nums;
                }
                if (Servers.localServer.serverSouth != null) {
                    --nums;
                }
                if (Servers.localServer.serverWest != null) {
                    --nums;
                }
                toReturn.add(new ActionEntry((short)nums, "Specials", "specials"));
                if (tx != -1 && ty != -1) {
                    toReturn.add(Actions.actionEntrys[95]);
                }
                toReturn.add(Actions.actionEntrys[64]);
                toReturn.add(Actions.actionEntrys[94]);
                toReturn.add(Actions.actionEntrys[179]);
                if (performer.getStatus().visible) {
                    toReturn.add(Actions.actionEntrys[577]);
                } else {
                    toReturn.add(Actions.actionEntrys[578]);
                }
                if (((Player)performer).GMINVULN) {
                    toReturn.add(Actions.actionEntrys[580]);
                } else {
                    toReturn.add(Actions.actionEntrys[579]);
                }
                if (Servers.localServer.serverNorth != null) {
                    toReturn.add(Actions.actionEntrys[240]);
                }
                if (Servers.localServer.serverEast != null) {
                    toReturn.add(Actions.actionEntrys[241]);
                }
                if (Servers.localServer.serverSouth != null) {
                    toReturn.add(Actions.actionEntrys[242]);
                }
                if (Servers.localServer.serverWest != null) {
                    toReturn.add(Actions.actionEntrys[243]);
                }
                try {
                    if (performer.getBody().getWounds() != null && performer.getBody().getWounds().getWounds().length > 0) {
                        toReturn.add(Actions.actionEntrys[346]);
                    }
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, "Problem getting " + performer.getName() + "'s body wounds for HealFast action due to: " + ex.getMessage(), ex);
                }
                nums = -1;
                if (type == Tiles.Tile.TILE_TREE.id || type == Tiles.Tile.TILE_BUSH.id) {
                    --nums;
                }
                toReturn.add(new ActionEntry((short)nums, "Nature", "nature"));
                toReturn.add(new ActionEntry(118, "Grow trees", "growing"));
                if (type == Tiles.Tile.TILE_TREE.id || type == Tiles.Tile.TILE_BUSH.id) {
                    toReturn.add(Actions.actionEntrys[90]);
                }
            }
            if (subject.isSign() || subject.isEnchantedTurret() || subject.isUnenchantedTurret()) {
                toReturn.add(Actions.actionEntrys[176]);
            }
            if (subject.isHolyItem()) {
                if (subject.isHolyItem(performer.getDeity()) && (performer.isPriest() || performer.getPower() > 0)) {
                    float faith = performer.getFaith();
                    Spell[] spells = performer.getDeity().getSpellsTargettingTiles((int)faith);
                    if (spells.length > 0) {
                        toReturn.add(new ActionEntry((short)(-spells.length), "Spells", "spells"));
                        for (int x = 0; x < spells.length; ++x) {
                            toReturn.add(Actions.actionEntrys[spells[x].number]);
                        }
                    }
                    if (performer.isLinked()) {
                        toReturn.add(Actions.actionEntrys[399]);
                    }
                }
            } else if (templateId == 676) {
                if (subject.getOwnerId() == performer.getWurmId()) {
                    toReturn.add(Actions.actionEntrys[472]);
                }
            } else if (subject.isMagicStaff() || subject.getTemplateId() == 176 && performer.getPower() >= 2 && Servers.isThisATestServer()) {
                LinkedList<ActionEntry> slist = new LinkedList<ActionEntry>();
                if (performer.knowsKarmaSpell(631)) {
                    slist.add(Actions.actionEntrys[631]);
                }
                if (performer.knowsKarmaSpell(630)) {
                    slist.add(Actions.actionEntrys[630]);
                }
                if (performer.knowsKarmaSpell(629)) {
                    slist.add(Actions.actionEntrys[629]);
                }
                if (performer.knowsKarmaSpell(560)) {
                    slist.add(Actions.actionEntrys[560]);
                }
                if (performer.knowsKarmaSpell(561)) {
                    slist.add(Actions.actionEntrys[561]);
                }
                if (performer.knowsKarmaSpell(562)) {
                    slist.add(Actions.actionEntrys[562]);
                }
                if (performer.getPower() >= 4) {
                    toReturn.add(new ActionEntry((short)(-slist.size()), "Sorcery", "casting"));
                }
                toReturn.addAll(slist);
            }
            if (templateId == 901) {
                toReturn.add(Actions.actionEntrys[636]);
            }
        }
        if (performer.getPet() != null) {
            short nums = -2;
            if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                nums = (short)(nums - 1);
            }
            toReturn.add(new ActionEntry(nums, "Pet", "Pet"));
            toReturn.add(Actions.actionEntrys[41]);
            toReturn.add(Actions.actionEntrys[40]);
            if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                if (performer.getPet().isStayonline()) {
                    toReturn.add(Actions.actionEntrys[45]);
                } else {
                    toReturn.add(Actions.actionEntrys[44]);
                }
            }
        }
        return toReturn;
    }

    @Override
    @Nonnull
    public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile) {
        MineDoorPermission md;
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
        byte type = Tiles.decodeType(tile);
        byte data = Tiles.decodeData(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        boolean water = Terraforming.isWater(tile, tilex, tiley, onSurface);
        if (water) {
            toReturn.add(Actions.actionEntrys[19]);
            toReturn.add(Actions.actionEntrys[183]);
            if (performer.getDeity() != null && performer.getDeity().isWaterGod()) {
                Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
            }
        }
        if (performer.getVehicle() != -10L) {
            Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
            VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
            if (t == null || t.getStructure() == null) {
                if (vehicle.isChair()) {
                    toReturn.add(Actions.actionEntrys[708]);
                } else {
                    toReturn.add(Actions.actionEntrys[333]);
                }
            }
        }
        if (performer.getKingdomTemplateId() == 3 && Tiles.getTile(type).isMycelium()) {
            toReturn.add(Actions.actionEntrys[347]);
        }
        if (performer.getPet() != null) {
            short nums = -2;
            if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                nums = (short)(nums - 1);
            }
            toReturn.add(new ActionEntry(nums, "Pet", "Pet"));
            toReturn.add(Actions.actionEntrys[41]);
            toReturn.add(Actions.actionEntrys[40]);
            if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                if (performer.getPet().isStayonline()) {
                    toReturn.add(Actions.actionEntrys[45]);
                } else {
                    toReturn.add(Actions.actionEntrys[44]);
                }
            }
        }
        if (Tiles.isMineDoor(Tiles.decodeType(tile)) && (md = MineDoorPermission.getPermission(tilex, tiley)) != null) {
            LinkedList<ActionEntry> permissions = new LinkedList<ActionEntry>();
            if (md.mayManage(performer) || md.isActualOwner(performer.getWurmId())) {
                permissions.add(Actions.actionEntrys[364]);
            }
            if (md.maySeeHistory(performer)) {
                permissions.add(new ActionEntry(691, "History of Mine Door", "viewing"));
            }
            if (!permissions.isEmpty()) {
                if (permissions.size() > 1) {
                    Collections.sort(permissions);
                    toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
                }
                toReturn.addAll(permissions);
            }
        }
        if (onSurface && type != Tiles.Tile.TILE_GRASS.id && !theTile.isBush() && !theTile.isTree()) {
            LinkedList<ActionEntry> nature = new LinkedList<ActionEntry>();
            toReturn.addAll(this.getNatureMenu(performer, tilex, tiley, type, data, nature));
        }
        TileBehaviour.addEmotes(toReturn);
        if (performer.isOnSurface() && performer.getPower() >= 2) {
            if (Zones.protectedTiles[tilex][tiley]) {
                toReturn.add(Actions.actionEntrys[382]);
            } else {
                toReturn.add(Actions.actionEntrys[381]);
            }
            toReturn.add(Actions.actionEntrys[476]);
        }
        if (performer.getCultist() != null) {
            if (performer.getCultist().mayInfoLocal()) {
                toReturn.add(Actions.actionEntrys[185]);
            }
            if ((type == Tiles.Tile.TILE_LAVA.id || type == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) && performer.getCultist().maySpawnVolcano()) {
                toReturn.add(new ActionEntry(78, "Freeze", "freezing"));
            }
        }
        if (performer.isTeamLeader()) {
            toReturn.add(Actions.actionEntrys[471]);
        }
        if (performer.getTeam() != null) {
            toReturn.add(Actions.actionEntrys[470]);
        }
        return toReturn;
    }

    @Override
    public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, short action, float counter) {
        boolean done = true;
        byte data = Tiles.decodeData(tile);
        MineDoorPermission md = MineDoorPermission.getPermission(tilex, tiley);
        Communicator comm = performer.getCommunicator();
        switch (action) {
            case 1: {
                TileBehaviour.handleEXAMINE(performer, tilex, tiley, tile, md);
                break;
            }
            case 223: 
            case 642: {
                done = this.forage(act, performer, tilex, tiley, tile, data, counter);
                break;
            }
            case 569: 
            case 570: 
            case 571: {
                done = this.forageV11(act, performer, tilex, tiley, tile, data, counter);
                break;
            }
            case 224: {
                done = this.herbalize(act, performer, tilex, tiley, tile, data, counter);
                break;
            }
            case 572: 
            case 573: 
            case 575: 
            case 720: {
                done = this.botanizeV11(act, performer, tilex, tiley, tile, data, counter);
                break;
            }
            case 741: {
                done = this.collectSnow(act, performer, tilex, tiley, tile, data, counter);
                break;
            }
            case 109: {
                done = MethodsCreatures.track(performer, tilex, tiley, tile, counter);
                break;
            }
            case 347: {
                if (performer.getKingdomTemplateId() != 3) break;
                done = MethodsCreatures.absorb(performer, tilex, tiley, tile, counter, act);
                break;
            }
            case 38: {
                if (performer.isClimbing()) {
                    comm.sendNormalServerMessage("You are already climbing.", (byte)3);
                    break;
                }
                try {
                    performer.setClimbing(true);
                }
                catch (Exception iox) {
                    comm.sendAlertServerMessage("Failed to start climbing. This is a bug.");
                    logger.log(Level.WARNING, performer.getName() + " Failed to start climbing " + iox.getMessage(), iox);
                }
                break;
            }
            case 39: {
                if (!performer.isClimbing()) {
                    comm.sendNormalServerMessage("You are not climbing.");
                    break;
                }
                try {
                    performer.setClimbing(false);
                }
                catch (Exception iox) {
                    comm.sendAlertServerMessage("Failed to stop climbing. This is a bug.");
                    logger.log(Level.WARNING, performer.getName() + " Failed to stop climbing. " + iox.getMessage(), iox);
                }
                break;
            }
            case 34: {
                BlockingResult result;
                done = true;
                if (performer.getPower() <= 0) break;
                if (performer.isOnSurface() && (result = Blocking.getBlockerBetween(performer, act.getTarget(), true, 6, performer.getBridgeId(), -10L)) != null) {
                    Blocker firstBlocker = result.getFirstBlocker();
                    assert (firstBlocker != null);
                    comm.sendNormalServerMessage("Between tiles detected blocker: " + firstBlocker.getName());
                }
                Path path = null;
                try {
                    path = performer.findPath(tilex, tiley, null);
                }
                catch (NoPathException firstBlocker) {
                    // empty catch block
                }
                if (path == null) {
                    comm.sendNormalServerMessage("No path available.");
                    break;
                }
                while (!path.isEmpty()) {
                    try {
                        PathTile p = path.getFirst();
                        ItemFactory.createItem(344, 1.0f, (p.getTileX() << 2) + 2, (p.getTileY() << 2) + 2, 180.0f, performer.isOnSurface(), (byte)0, performer.getBridgeId(), null);
                        path.removeFirst();
                    }
                    catch (FailedException | NoSuchTemplateException e) {
                        logger.log(Level.INFO, performer.getName() + " " + e.getMessage(), e);
                        comm.sendNormalServerMessage("Failed to create marker.");
                    }
                }
                break;
            }
            case 577: {
                performer.setVisible(false);
                comm.sendSafeServerMessage("You are now invisible. Only gms can see you. Some actions and emotes may still be visible though.");
                return true;
            }
            case 578: {
                performer.setVisible(true);
                comm.sendSafeServerMessage("You are now visible again.");
                return true;
            }
            case 579: {
                ((Player)performer).GMINVULN = true;
                comm.sendNormalServerMessage("You are now invulnerable again.");
                return true;
            }
            case 580: {
                ((Player)performer).GMINVULN = false;
                comm.sendNormalServerMessage("You are now no longer invulnerable.");
                return true;
            }
            case 333: 
            case 708: {
                done = true;
                if (performer.getVehicle() == -10L) break;
                Vehicle vehic = Vehicles.getVehicleForId(performer.getVehicle());
                if (!vehic.isCreature()) {
                    try {
                        Item vehicle = Items.getItem(performer.getVehicle());
                        if (!vehicle.isChair() && TileBehaviour.checkTileDisembark(performer, tilex, tiley)) {
                            return true;
                        }
                        if (!(vehicle.isChair() || Math.abs(vehicle.getTileX() - tilex) <= 2 && Math.abs(vehicle.getTileY() - tiley) <= 2)) {
                            comm.sendNormalServerMessage("That is too far away", (byte)3);
                            break;
                        }
                        if (vehicle.isChair()) {
                            if (performer.getVisionArea() != null) {
                                performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                            }
                            performer.disembark(true);
                            break;
                        }
                        VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
                        if (t != null && t.getStructure() != null) {
                            comm.sendNormalServerMessage("The structure is in the way.");
                            break;
                        }
                        BlockingResult result = Blocking.getBlockerBetween(performer, vehicle.getPosX(), vehicle.getPosY(), (tilex << 2) + 2, (tiley << 2) + 2, vehicle.getPosZ(), vehicle.getPosZ(), vehicle.isOnSurface(), vehicle.isOnSurface(), false, 4, -1L, performer.getBridgeId(), performer.getBridgeId(), false);
                        if (result != null) {
                            comm.sendNormalServerMessage("You can't get there.");
                            break;
                        }
                        if (performer.getVisionArea() != null) {
                            performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                        }
                        performer.disembark(true, tilex, tiley);
                    }
                    catch (NoSuchItemException nsi) {
                        comm.sendNormalServerMessage("An error has occurred. Please log on again to correct this.");
                        logger.log(Level.WARNING, nsi.getMessage(), nsi);
                    }
                    break;
                }
                if (TileBehaviour.checkTileDisembark(performer, tilex, tiley)) {
                    return true;
                }
                try {
                    Creature vehicle = Creatures.getInstance().getCreature(performer.getVehicle());
                    if (Math.abs(vehicle.getTileX() - tilex) <= 2 && Math.abs(vehicle.getTileY() - tiley) <= 2) {
                        VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
                        if (t != null && t.getStructure() != null) {
                            comm.sendNormalServerMessage("The structure is in the way.");
                            break;
                        }
                        BlockingResult result = Blocking.getBlockerBetween(performer, vehicle.getPosX(), vehicle.getPosY(), (tilex << 2) + 2, (tiley << 2) + 2, vehicle.getPositionZ(), vehicle.getPositionZ(), vehicle.isOnSurface(), vehicle.isOnSurface(), false, 4, -1L, performer.getBridgeId(), performer.getBridgeId(), false);
                        if (result != null) {
                            comm.sendNormalServerMessage("You can't get there.");
                            break;
                        }
                        if (performer.getVisionArea() != null) {
                            performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                        }
                        performer.disembark(true, tilex, tiley);
                        break;
                    }
                    comm.sendNormalServerMessage("That is too far away");
                }
                catch (NoSuchCreatureException nsi) {
                    comm.sendNormalServerMessage("An error has occurred. Please log on again to correct this.");
                    logger.log(Level.WARNING, nsi.getMessage(), nsi);
                }
                break;
            }
            case 471: {
                if (!performer.isTeamLeader()) break;
                try {
                    TeamManagementQuestion tme = new TeamManagementQuestion(performer, "Managing the team", "Managing " + performer.getTeam().getName(), false, performer.getWurmId(), true, false);
                    tme.sendQuestion();
                }
                catch (Exception tme) {}
                break;
            }
            case 470: {
                if (performer.getTeam() == null) break;
                performer.setTeam(null, true);
                break;
            }
            case 64: {
                done = true;
                if (performer.getPower() > 0) {
                    comm.sendNormalServerMessage("That tile is at " + tilex + ", " + tiley + ", you surfaced=" + performer.isOnSurface());
                }
                if (performer.getPower() < 4) break;
                int zid = -1;
                try {
                    Zone z = Zones.getZone(tilex, tiley, true);
                    zid = z.getId();
                }
                catch (NoSuchZoneException z) {
                    // empty catch block
                }
                int surf = Server.surfaceMesh.getTile(tilex, tiley);
                int cave = Server.caveMesh.getTile(tilex, tiley);
                int rock = Server.rockMesh.getTile(tilex, tiley);
                byte caveCeil = Tiles.decodeData(cave);
                String cavename = Tiles.getTile((byte)Tiles.decodeType((int)cave)).tiledesc;
                if (performer.getPower() >= 4) {
                    String msg = "ZoneId=" + zid + " Surface=" + Tiles.decodeHeight(surf) + ", rock=" + Tiles.decodeHeight(rock) + " cave=" + Tiles.decodeHeight(cave) + " ceiling=" + caveCeil;
                    if (performer.getPower() >= 5) {
                        msg = msg + ". Cave is " + cavename;
                    }
                    comm.sendNormalServerMessage(msg);
                }
                int ttx = performer.getTileX();
                int tty = (int)performer.getStatus().getPositionY() >> 2;
                VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
                comm.sendNormalServerMessage("You are on " + ttx + ", " + tty + " z=" + performer.getStatus().getPositionZ() + ". Tile here has ZoneId=" + vtile.getZone().getId() + ". Flat=" + Terraforming.isFlat(ttx, tty, performer.isOnSurface(), 0));
                try {
                    Item marker = ItemFactory.createItem(344, 1.0f, null);
                    marker.setPosXY((tilex << 2) + 2, (tiley << 2) + 2);
                    vtile.addItem(marker, false, false);
                    comm.sendNormalServerMessage("The marker ended up on " + marker.getTileX() + ", " + marker.getTileY() + ". It now has ZoneId=" + marker.getZoneId());
                }
                catch (FailedException | NoSuchTemplateException e) {
                    logger.log(Level.INFO, performer.getName() + " " + e.getMessage(), e);
                    comm.sendNormalServerMessage("Failed to create marker.");
                }
                if (!onSurface) break;
                short dirtHeight = (short)(Tiles.decodeHeight(surf) - Tiles.decodeHeight(rock));
                comm.sendNormalServerMessage("Dirt height = " + dirtHeight + ".");
                break;
            }
            case 19: {
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    if (WaterType.isBrackish(tilex, tiley, performer.isOnSurface())) {
                        comm.sendNormalServerMessage("The water tastes slightly salty.");
                        break;
                    }
                    comm.sendNormalServerMessage("The water tastes fresh.");
                    break;
                }
                comm.sendNormalServerMessage("The taste is very dry.");
                break;
            }
            case 183: {
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    done = false;
                    if (!act.justTickedSecond()) break;
                    done = MethodsItems.drink(performer, tilex, tiley, tile, counter, act);
                    break;
                }
                done = true;
                break;
            }
            case 141: {
                if (performer.getDeity() == null || !performer.getDeity().isWaterGod()) break;
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    done = MethodsReligion.pray(act, performer, counter);
                    break;
                }
                done = true;
                break;
            }
            case 40: {
                if (performer.getPet() != null && DbCreatureStatus.getIsLoaded(performer.getPet().getWurmId()) == 1) {
                    performer.getCommunicator().sendNormalServerMessage("The " + performer.getPet().getName() + " tilts " + performer.getPet().getHisHerItsString() + " head while looking at you. There is a cage stopping " + performer.getPet().getHimHerItString() + " from moving there.", (byte)3);
                    return true;
                }
                done = true;
                Creature pet = performer.getPet();
                if (pet == null) break;
                if (pet.isWithinDistanceTo(performer.getPosX(), performer.getPosY(), performer.getPositionZ(), 200.0f, 0.0f)) {
                    if (pet.mayReceiveOrder()) {
                        boolean ok = true;
                        int layer = 0;
                        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(Tiles.decodeType(tile))) {
                            layer = -1;
                        } else if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id && pet.isOnSurface()) {
                            layer = -1;
                        } else if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
                            comm.sendNormalServerMessage("You cannot order " + pet.getName() + " into the rock.");
                            ok = false;
                        } else if (tilex < 10 || tiley < 10 || tilex > Zones.worldTileSizeX - 10 || tiley > Zones.worldTileSizeY - 10) {
                            comm.sendNormalServerMessage("The " + pet.getName() + " hesitates and does not go there.");
                            ok = false;
                        }
                        Village v = Villages.getVillage(tilex, tiley, true);
                        if (v != null && v.isEnemy(performer)) {
                            comm.sendNormalServerMessage("The " + pet.getName() + " hesitates and does not enter " + v.getName() + ".");
                            ok = false;
                        }
                        if (pet.getHitched() != null || pet.isRidden()) {
                            comm.sendNormalServerMessage("The " + pet.getName() + " is restrained and ignores your order.");
                            ok = false;
                        }
                        if (!ok) break;
                        Order o = new Order(tilex, tiley, layer);
                        pet.addOrder(o);
                        comm.sendNormalServerMessage("You issue an order to " + pet.getName() + ".");
                        break;
                    }
                    comm.sendNormalServerMessage("The " + pet.getName() + " ignores your order.");
                    break;
                }
                comm.sendNormalServerMessage("The " + pet.getName() + " is too far away.");
                break;
            }
            case 41: {
                Creature pet = performer.getPet();
                if (pet == null) break;
                if (pet.isWithinDistanceTo(performer.getPosX(), performer.getPosY(), performer.getPositionZ(), 200.0f, 0.0f)) {
                    pet.clearOrders();
                    comm.sendNormalServerMessage("You order the " + pet.getName() + " to forget all you told " + pet.getHimHerItString() + ".");
                    break;
                }
                comm.sendNormalServerMessage("The " + pet.getName() + " is too far away.");
                break;
            }
            case 352: {
                done = true;
                if (performer.getPower() < 5) break;
                comm.sendNormalServerMessage("Logging on = " + (-10L == performer.loggerCreature1));
                if (performer.loggerCreature1 == -10L) {
                    performer.loggerCreature1 = performer.getWurmId();
                    break;
                }
                performer.loggerCreature1 = -10L;
                break;
            }
            case 45: {
                done = true;
                if (performer.getPet() == null) break;
                if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                    performer.getPet().setStayOnline(false);
                    comm.sendNormalServerMessage("The " + performer.getPet().getName() + " will now leave the world when you do.");
                    break;
                }
                comm.sendNormalServerMessage("The " + performer.getPet().getName() + " may not go offline.");
                break;
            }
            case 44: {
                done = true;
                if (performer.getPet() == null) break;
                if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                    performer.getPet().setStayOnline(true);
                    comm.sendNormalServerMessage("The " + performer.getPet().getName() + " will now stay in the world when you log off.");
                    break;
                }
                comm.sendNormalServerMessage("The " + performer.getPet().getName() + " may not go offline.");
                break;
            }
            case 364: {
                if (md == null || !md.mayManage(performer) && !md.isActualOwner(performer.getWurmId())) break;
                ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.MINEDOOR, md, false, -10L, false, null, "");
                mp.sendQuestion();
                break;
            }
            case 691: {
                if (md == null || !md.maySeeHistory(performer)) break;
                PermissionsHistory ph = new PermissionsHistory(performer, md.getWurmId());
                ph.sendQuestion();
                break;
            }
            case 476: {
                List<TileEvent> events;
                if (performer.getPower() <= 0) break;
                if (!Constants.useTileEventLog) {
                    comm.sendNormalServerMessage("This server does not register tile events.");
                    break;
                }
                if (performer.getLogger() != null) {
                    performer.getLogger().log(Level.INFO, performer.getName() + " checked tile logs @" + tilex + "," + tiley + "," + performer.isOnSurface());
                }
                if (!(events = TileEvent.getEventsFor(tilex, tiley, performer.isOnSurface() ? 0 : -1)).isEmpty()) {
                    for (TileEvent t : events) {
                        comm.sendNormalServerMessage(TileBehaviour.getStringForTileEvent(t));
                    }
                    break;
                }
                comm.sendNormalServerMessage("No events registered here.");
                break;
            }
            case 240: 
            case 241: 
            case 242: 
            case 243: {
                done = Methods.transferPlayer(performer, performer, act, counter);
                break;
            }
            case 162: {
                if (performer.getPower() <= 0) break;
                if (TileBehaviour.isSpike(performer, tilex, tiley, true)) {
                    comm.sendNormalServerMessage("You level the terrain.");
                    break;
                }
                comm.sendNormalServerMessage("The terrain was not considered to contain a spike or hole.");
                break;
            }
            case 381: {
                if (!performer.isOnSurface() || performer.getPower() < 2) break;
                Zones.protectedTiles[tilex][tiley] = true;
                if (Zones.isOnPvPServer(tilex, tiley)) {
                    comm.sendNormalServerMessage("You protect the tile. Please note that this should be extremely rare on pvp servers as it may be regarded as power abuse.");
                    break;
                }
                comm.sendNormalServerMessage("You protect the tile.");
                break;
            }
            case 382: {
                if (!performer.isOnSurface() || performer.getPower() < 2) break;
                Zones.protectedTiles[tilex][tiley] = false;
                comm.sendNormalServerMessage("You remove the  protection from the tile.");
                break;
            }
            case 388: {
                if (!Methods.isActionAllowed(performer, (short)384) || performer.getCultist() == null || !performer.getCultist().mayEnchantNature()) break;
                done = Terraforming.enchantNature(performer, tilex, tiley, onSurface, tile, counter, act);
                break;
            }
            case 185: {
                done = true;
                if (!Methods.isActionAllowed(performer, (short)384) || performer.getCultist() == null || !performer.getCultist().mayInfoLocal()) break;
                performer.getVisionArea().getSurface().sendHostileCreatures();
                performer.getVisionArea().getUnderGround().sendHostileCreatures();
                performer.getCultist().touchCooldown2();
                break;
            }
            case 78: {
                done = true;
                if (!Methods.isActionAllowed(performer, (short)384) || performer.getCultist() == null) {
                    comm.sendNormalServerMessage("You do not have that power.");
                    break;
                }
                if (!performer.getCultist().maySpawnVolcano()) {
                    comm.sendNormalServerMessage("Nothing happens.");
                    break;
                }
                done = Terraforming.freezeLava(performer, tilex, tiley, onSurface, tile, counter, true);
                break;
            }
            case 399: {
                performer.disableLink();
                break;
            }
            case 180: {
                done = this.destroyAllFloorsAt(act, performer, counter);
            }
        }
        return done;
    }

    private static void handleEXAMINE(Creature performer, int tilex, int tiley, int tile, MineDoorPermission md) {
        Communicator comm = performer.getCommunicator();
        byte decodedTileType = Tiles.decodeType(tile);
        if (Tiles.isMineDoor(decodedTileType)) {
            String doorName = "";
            String ownerName = "Unknown";
            String ownerSig = "";
            boolean maypass = false;
            int str = Server.getWorldResource(tilex, tiley);
            if (md != null) {
                long oId = md.getOwnerId();
                ownerName = PlayerInfoFactory.getPlayerName(oId);
                int ql = str / 100;
                if (ql >= 20 && ql < 90) {
                    ownerSig = Item.obscureWord(ownerName, ql);
                } else if (ql >= 90) {
                    ownerSig = ownerName;
                }
                doorName = md.getObjectName();
                maypass = md.mayPass(performer);
            }
            switch (decodedTileType) {
                case 27: {
                    comm.sendNormalServerMessage("You see a golden door.");
                    break;
                }
                case 28: {
                    comm.sendNormalServerMessage("You see a silver door.");
                    break;
                }
                case 29: {
                    comm.sendNormalServerMessage("You see a steel door.");
                    break;
                }
                case 25: {
                    comm.sendNormalServerMessage("You see a wooden mine door.");
                    break;
                }
                case 26: {
                    comm.sendNormalServerMessage("You see hard rock.");
                }
            }
            if (decodedTileType == 26) {
                if (performer.getPower() > 0 || (double)Server.rand.nextInt(100) <= performer.getMindLogical().getKnowledge(0.0)) {
                    comm.sendNormalServerMessage("You skillfully detect a mine door in the rock.");
                    comm.sendNormalServerMessage("Strength=" + str + ".");
                    if (!doorName.isEmpty()) {
                        comm.sendNormalServerMessage("You notice something chiselled out near the top of the door, it's \"" + doorName + "\"");
                    }
                }
            } else {
                comm.sendNormalServerMessage("Strength=" + str + ".");
                int template = MethodsStructure.getImproveItem(decodedTileType);
                try {
                    ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(template);
                    comm.sendNormalServerMessage("It could be improved with " + temp.getNameWithGenus() + ".");
                }
                catch (NoSuchTemplateException noSuchTemplateException) {
                    // empty catch block
                }
                if (!doorName.isEmpty()) {
                    comm.sendNormalServerMessage("You notice something inscribed near the top of the door, it's \"" + doorName + "\"");
                }
            }
            if (performer.getPower() >= 2 && !ownerName.isEmpty()) {
                comm.sendNormalServerMessage("In the bottom right corner, you notice a signature of \"" + ownerName + "\"");
            } else if (maypass && !ownerSig.isEmpty()) {
                comm.sendNormalServerMessage("In the bottom right corner, you notice a signature of \"" + ownerSig + "\"");
            }
        } else if (Tiles.decodeHeight(tile) < -7) {
            comm.sendNormalServerMessage("You see the glittering surface of water.");
        } else {
            comm.sendNormalServerMessage(TileBehaviour.getTileDescription(tile));
        }
        TileBehaviour.sendVillageString(performer, tilex, tiley, true);
        TileBehaviour.sendTileTransformationState(performer, tilex, tiley, decodedTileType);
        Trap t = Trap.getTrap(tilex, tiley, performer.getLayer());
        if (performer.getPower() >= 3) {
            comm.sendNormalServerMessage("Your rot: " + Creature.normalizeAngle(performer.getStatus().getRotation()) + ", Wind rot=" + Server.getWeather().getWindRotation() + ", pow=" + Server.getWeather().getWindPower() + " x=" + Server.getWeather().getXWind() + ", y=" + Server.getWeather().getYWind());
            comm.sendNormalServerMessage("Tile is spring=" + Zone.hasSpring(tilex, tiley));
            if (performer.getPower() >= 5) {
                comm.sendNormalServerMessage("tilex: " + tilex + ", tiley=" + tiley);
            }
            if (t != null) {
                String villageName = "none";
                if (t.getVillage() > 0) {
                    try {
                        villageName = Villages.getVillage(t.getVillage()).getName();
                    }
                    catch (NoSuchVillageException ownerSig) {
                        // empty catch block
                    }
                }
                comm.sendNormalServerMessage("A " + t.getName() + ", ql=" + t.getQualityLevel() + " kingdom=" + Kingdoms.getNameFor(t.getKingdom()) + ", vill=" + villageName + ", rotdam=" + t.getRotDamage() + " firedam=" + t.getFireDamage() + " speed=" + t.getSpeedBon());
            }
            return;
        }
        if (t == null) {
            return;
        }
        if (t.getKingdom() != performer.getKingdomId() && performer.getDetectDangerBonus() <= 0.0f) {
            return;
        }
        String qlString = "average";
        if (t.getQualityLevel() < 20) {
            qlString = "low";
        } else if (t.getQualityLevel() > 80) {
            qlString = "deadly";
        } else if (t.getQualityLevel() > 50) {
            qlString = "high";
        }
        String villageName = ".";
        if (t.getVillage() > 0) {
            try {
                villageName = " of " + Villages.getVillage(t.getVillage()).getName() + ".";
            }
            catch (NoSuchVillageException maypass) {
                // empty catch block
            }
        }
        String rotDam = "";
        if (t.getRotDamage() > 0) {
            rotDam = " It has ugly black-green speckles.";
        }
        String fireDam = "";
        if (t.getFireDamage() > 0) {
            fireDam = " It has the rune of fire.";
        }
        comm.sendNormalServerMessage("You detect a " + t.getName() + " here, of " + qlString + " quality. It has been set by people from " + Kingdoms.getNameFor(t.getKingdom()) + villageName + rotDam + fireDam);
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short action, float counter) {
        boolean done = true;
        int stid = source.getTemplateId();
        byte type = Tiles.decodeType(tile);
        Communicator comm = performer.getCommunicator();
        switch (action) {
            case 150: 
            case 532: {
                if (Flattening.isTileTooDeep(tilex, tiley, 2, 2, 4)) {
                    if (source.isDredgingTool()) {
                        done = Flattening.flatten(performer, source, tile, tilex, tiley, counter, act);
                        break;
                    }
                    comm.sendNormalServerMessage("You need a dredge to do that at that depth.");
                    done = true;
                    break;
                }
                if (source.isDiggingtool()) {
                    done = Flattening.flatten(performer, source, tile, tilex, tiley, counter, act);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 144: {
                if (source.isDiggingtool() && onSurface) {
                    done = Terraforming.dig(performer, source, tilex, tiley, tile, counter, false, performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 921: {
                if (source.isDiggingtool() && onSurface) {
                    done = Terraforming.dig(performer, source, tilex, tiley, tile, counter, false, performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh, true);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 362: {
                if (source.isDredgingTool() && onSurface) {
                    done = Terraforming.dig(performer, source, tilex, tiley, tile, counter, false, performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 604: {
                if (source.getTemplateId() == 176 && performer.getPower() >= 2) {
                    Terraforming.paintTerrain((Player)performer, source, tilex, tiley);
                    done = true;
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 927: {
                if (Terraforming.isSwitchableTiles(source.getTemplateId(), type)) {
                    done = Terraforming.switchTileTypes(performer, source, tilex, tiley, counter, act);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 154: {
                if (source.isDiggingtool() && onSurface) {
                    done = Terraforming.pack(performer, source, tilex, tiley, onSurface, tile, counter, act);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 155: {
                if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MARSH.id) {
                    if (stid != 495) break;
                    done = Terraforming.makeFloor(performer, source, tilex, tiley, onSurface, tile, counter);
                    break;
                }
                if (!onSurface && source.isCavePaveable()) {
                    HighwayPos highwaypos;
                    if (Tiles.isRoadType(type) && !MethodsHighways.onHighway(highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface))) {
                        comm.sendSafeServerMessage("You can only replace paving on highways.");
                        return true;
                    }
                    done = Terraforming.pave(performer, source, tilex, tiley, onSurface, tile, counter, act);
                    break;
                }
                if (!onSurface || !source.isPaveable() || source.getTemplateId() == 495) break;
                if (Tiles.isRoadType(type)) {
                    if (performer.getStrengthSkill() < 20.0) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be stronger to replace pavement.");
                        return true;
                    }
                    HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
                    if (!MethodsHighways.onHighway(highwaypos)) {
                        comm.sendSafeServerMessage("You can only replace paving on highways.");
                        return true;
                    }
                }
                done = Terraforming.pave(performer, source, tilex, tiley, onSurface, tile, counter, act);
                break;
            }
            case 576: {
                if (Tiles.decodeType(tile) == Tiles.Tile.TILE_MARSH.id) {
                    if (stid != 495) break;
                    done = Terraforming.makeFloor(performer, source, tilex, tiley, onSurface, tile, counter);
                    break;
                }
                if (!onSurface && source.isCavePaveable()) {
                    if (Tiles.isRoadType(type)) {
                        if (performer.getStrengthSkill() < 20.0) {
                            performer.getCommunicator().sendNormalServerMessage("You need to be stronger to replace pavement.");
                            return true;
                        }
                        HighwayPos highwaypos = MethodsHighways.getHighwayPos(tilex, tiley, onSurface);
                        if (!MethodsHighways.onHighway(highwaypos)) {
                            comm.sendSafeServerMessage("You can only replace paving on highways.");
                            return true;
                        }
                    }
                    done = Terraforming.pave(performer, source, tilex, tiley, onSurface, tile, counter, act);
                    break;
                }
                if (!onSurface || !source.isPaveable() || source.getTemplateId() == 495) break;
                done = Terraforming.pave(performer, source, tilex, tiley, onSurface, tile, counter, act);
                break;
            }
            case 318: {
                if (stid == 27 || stid == 25) {
                    done = Terraforming.cultivate(performer, source, tilex, tiley, onSurface, tile, counter);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 160: {
                if (source.getTemplateId() != 1343 && source.getTemplateId() != 705 && source.getTemplateId() != 707 && source.getTemplateId() != 1344 && source.getTemplateId() != 1346) break;
                done = MethodsFishing.fish(performer, source, tilex, tiley, tile, counter, act);
                break;
            }
            case 285: {
                done = true;
                if (source.getTemplateId() != 1344 && source.getTemplateId() != 1343 && source.getTemplateId() != 705 && source.getTemplateId() != 707 && source.getTemplateId() != 1346) break;
                done = MethodsFishing.showFishTable(performer, source, tilex, tiley, counter, act);
                break;
            }
            case 109: {
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 191: {
                if (source.isDiggingtool()) {
                    done = Terraforming.destroyPave(performer, source, tilex, tiley, onSurface, tile, counter);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 231: {
                if (stid == 153 && Tiles.decodeType(tile) == Tiles.Tile.TILE_PLANKS.id && onSurface) {
                    done = Terraforming.tarFloor(performer, source, tilex, tiley, onSurface, tile, counter);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 141: {
                if (!Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    done = true;
                    break;
                }
                if (performer.getDeity() == null || !performer.getDeity().isWaterGod()) {
                    done = true;
                    break;
                }
                done = MethodsReligion.pray(act, performer, counter);
                break;
            }
            case 374: {
                if (!source.isTrap() || !Trap.mayTrapTemplateOnTile(stid, type) && (stid != 612 || !Trap.mayPlantCorrosion(tilex, tiley, performer.getLayer())) || Trap.getTrap(tilex, tiley, performer.getLayer()) != null) break;
                return Trap.trap(performer, source, tile, tilex, tiley, performer.getLayer(), counter, act);
            }
            case 375: {
                return Trap.disarm(performer, source, tilex, tiley, performer.getLayer(), counter, act);
            }
            case 56: {
                done = MethodsStructure.buildPlan(performer, source, tilex, tiley, tile, counter);
                break;
            }
            case 530: {
                done = MethodsStructure.expandHouseTile(performer, source, tilex, tiley, tile, counter);
                break;
            }
            case 531: {
                done = MethodsStructure.removeHouseTile(performer, tilex, tiley, tile, counter);
                break;
            }
            case 57: {
                done = MethodsStructure.buildPlanRemove(performer, tilex, tiley, tile, counter);
                break;
            }
            case 58: {
                MethodsStructure.tryToFinalize(performer, tilex, tiley);
                break;
            }
            case 508: {
                return MethodsStructure.floorPlanAbove(performer, source, tilex, tiley, tile, performer.getLayer(), counter, act, StructureConstants.FloorType.FLOOR);
            }
            case 514: {
                return MethodsStructure.floorPlanAbove(performer, source, tilex, tiley, tile, performer.getLayer(), counter, act, StructureConstants.FloorType.DOOR);
            }
            case 515: {
                return MethodsStructure.floorPlanAbove(performer, source, tilex, tiley, tile, performer.getLayer(), counter, act, StructureConstants.FloorType.OPENING);
            }
            case 509: {
                return MethodsStructure.floorPlanBelow(performer, source, tilex, tiley, tile, performer.getLayer(), counter, act);
            }
            case 507: {
                return MethodsStructure.floorPlanRoof(performer, source, tilex, tiley, tile, performer.getLayer(), counter, act);
            }
            case 95: {
                done = true;
                MethodsCreatures.teleportCreature(performer, source);
                break;
            }
            case 94: {
                done = true;
                if ((stid == 176 || stid == 315) && performer.getPower() >= 2 || stid == 174 && performer.getPower() >= 1) {
                    Methods.sendTeleportQuestion(performer, source);
                    break;
                }
                if (stid != 174 && stid != 524 && stid != 525) break;
                MethodsCreatures.teleportSet(performer, source, tilex, tiley);
                break;
            }
            case 577: {
                performer.setVisible(false);
                comm.sendSafeServerMessage("You are now invisible. Only gms can see you. Some actions and emotes may still be visible though.");
                return true;
            }
            case 578: {
                performer.setVisible(true);
                comm.sendSafeServerMessage("You are now visible again.");
                return true;
            }
            case 579: {
                ((Player)performer).GMINVULN = true;
                comm.sendNormalServerMessage("You are now invulnerable again.");
                return true;
            }
            case 580: {
                ((Player)performer).GMINVULN = false;
                comm.sendNormalServerMessage("You are now no longer invulnerable.");
                return true;
            }
            case 88: {
                done = true;
                if (stid == 176 && performer.getPower() >= 4 || stid == 176 && performer.getPower() >= 2 && Servers.isThisATestServer()) {
                    Methods.sendTileDataQuestion(performer, source, tilex, tiley);
                    break;
                }
                logger.log(Level.WARNING, performer.getName() + " tried to set tile data without a wand or power.");
                break;
            }
            case 148: {
                done = true;
                if (!WurmPermissions.mayCreateItems(performer) || stid != 176 && stid != 301) break;
                Methods.sendCreateQuestion(performer, source);
                break;
            }
            case 335: {
                done = true;
                if (!WurmPermissions.mayChangeTile(performer) || stid != 176) break;
                Methods.sendTerraformingQuestion(performer, source, tilex, tiley);
                break;
            }
            case 369: {
                if (stid != 602) break;
                done = Terraforming.obliterate(performer, act, source, tilex, tiley, tile, counter, source.getAuxData(), performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
                break;
            }
            case 90: {
                VolaTile tempvtile1;
                byte aData;
                int age;
                done = true;
                if (Servers.localServer.entryServer && (Tiles.decodeType(tile) == Tiles.Tile.TILE_TREE.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_BUSH.id) && (age = (aData = Tiles.decodeData(tile)) >> 4 & 0xF) < 14) {
                    int tree = aData & 0xF;
                    int newData = (++age << 4) + tree & 0xFF;
                    Server.setSurfaceTile(tilex, tiley, Tiles.decodeHeight(tile), Tiles.decodeType(tile), (byte)newData);
                    Players.getInstance().sendChangedTile(tilex, tiley, true, false);
                }
                if (performer.getPower() < 5 || stid != 176) break;
                boolean oldSurf = TilePoller.pollingSurface;
                TilePoller.pollingSurface = true;
                TilePoller.currentMesh = Server.surfaceMesh;
                TilePoller.checkEffects(tile, tilex, tiley, Tiles.decodeType(tile), Tiles.decodeData(tile));
                comm.sendNormalServerMessage("You poll " + tilex + "," + tiley + " surfaced=" + TilePoller.pollingSurface);
                TilePoller.pollingSurface = oldSurf;
                if (!oldSurf) {
                    TilePoller.currentMesh = Server.caveMesh;
                }
                if ((tempvtile1 = Zones.getTileOrNull(tilex, tiley, onSurface)) == null) break;
                tempvtile1.pollStructures(System.currentTimeMillis());
                tempvtile1.poll(true, 0, false);
                break;
            }
            case 185: {
                done = true;
                this.handle_GETINFO(performer, tilex, tiley, stid);
                break;
            }
            case 194: {
                done = true;
                if (performer.getPower() < 5) break;
                Methods.sendPlayerPaymentQuestion(performer);
                break;
            }
            case 352: {
                done = true;
                if (performer.getPower() < 5) break;
                comm.sendNormalServerMessage("Logging on = " + (-10L == performer.loggerCreature1));
                if (performer.loggerCreature1 == -10L) {
                    performer.loggerCreature1 = performer.getWurmId();
                    break;
                }
                performer.loggerCreature1 = -10L;
                break;
            }
            case 179: {
                done = true;
                if (stid != 176 && stid != 315 || !WurmPermissions.mayUseGMWand(performer)) break;
                Methods.sendSummonQuestion(performer, source, tilex, tiley, -10L);
                break;
            }
            case 675: {
                if (performer.getTaggedItemId() == -10L) {
                    done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                    break;
                }
                done = true;
                if ((stid == 176 || stid == 315) && WurmPermissions.mayUseGMWand(performer)) {
                    try {
                        Item target = Items.getItem(performer.getTaggedItemId());
                        Zone currZone = Zones.getZone((int)target.getPosX() >> 2, (int)target.getPosY() >> 2, target.isOnSurface());
                        currZone.removeItem(target);
                        target.putItemInfrontof(performer);
                    }
                    catch (NoSuchZoneException nsz) {
                        comm.sendNormalServerMessage("Failed to locate the zone for that item. Failed to summon.");
                        logger.log(Level.WARNING, performer.getTaggedItemId() + ": " + nsz.getMessage(), nsz);
                    }
                    catch (NoSuchCreatureException nsc) {
                        comm.sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
                        logger.log(Level.WARNING, performer.getTaggedItemId() + ": " + nsc.getMessage(), nsc);
                    }
                    catch (NoSuchItemException nsi) {
                        comm.sendNormalServerMessage("Failed to locate the item for that request! Failed to summon.");
                        logger.log(Level.WARNING, performer.getTaggedItemId() + ":" + nsi.getMessage(), nsi);
                    }
                    catch (NoSuchPlayerException nsp) {
                        comm.sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
                        logger.log(Level.WARNING, performer.getTaggedItemId() + ":" + nsp.getMessage(), nsp);
                    }
                }
                performer.setTagItem(-10L, "");
                break;
            }
            case 176: {
                if (source.isSign()) {
                    done = MethodsItems.plantSign(performer, source, counter, false, 0, 0, performer.isOnSurface(), performer.getBridgeId(), false, -1L);
                    break;
                }
                done = true;
                break;
            }
            case 189: {
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    MethodsItems.fillContainer(source, performer, WaterType.isBrackish(tilex, tiley, performer.isOnSurface()));
                }
                done = true;
                break;
            }
            case 19: {
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    if (WaterType.isBrackish(tilex, tiley, performer.isOnSurface())) {
                        comm.sendNormalServerMessage("The water tastes slightly salty.");
                        break;
                    }
                    comm.sendNormalServerMessage("The water tastes fresh.");
                    break;
                }
                comm.sendNormalServerMessage("The taste is very dry.");
                break;
            }
            case 183: {
                if (Terraforming.isWater(tile, tilex, tiley, performer.isOnSurface())) {
                    done = false;
                    if (!act.justTickedSecond()) break;
                    done = MethodsItems.drink(performer, tilex, tiley, tile, counter, act);
                    break;
                }
                done = true;
                break;
            }
            case 192: {
                MineDoorPermission md;
                if (!Tiles.isMineDoor(type) || type == Tiles.Tile.TILE_MINE_DOOR_STONE.id || (md = MineDoorPermission.getPermission(tilex, tiley)) == null || !md.mayManage(performer)) break;
                done = MethodsStructure.improveTileDoor(performer, source, tilex, tiley, tile, act, counter);
                break;
            }
            case 92: {
                if (!WurmPermissions.mayUseDeityWand(performer) || stid != 176) break;
                Methods.sendLearnSkillQuestion(performer, source, -10L);
                break;
            }
            case 346: {
                if (stid != 176 && stid != 315 || !WurmPermissions.mayUseGMWand(performer)) break;
                try {
                    Wound[] wounds;
                    for (Wound wound : wounds = performer.getBody().getWounds().getWounds()) {
                        wound.heal();
                    }
                    break;
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                    break;
                }
            }
            case 329: {
                if (stid != 489 && (!WurmPermissions.mayUseGMWand(performer) || stid != 176 && source.getTemplateId() != 315)) break;
                done = MethodsItems.watchSpyglass(performer, source, act, counter);
                break;
            }
            case 906: {
                if (stid != 1115 && (!source.isWand() || !Tiles.isMineDoor(Tiles.decodeType(tile)))) break;
                done = Terraforming.removeMineDoor(performer, act, source, tilex, tiley, onSurface, counter);
                break;
            }
            case 174: {
                if (!source.isWeapon() && !source.isWand() || !Tiles.isMineDoor(Tiles.decodeType(tile))) break;
                done = Terraforming.destroyMineDoor(performer, act, source, tilex, tiley, onSurface, counter);
                break;
            }
            case 180: {
                done = this.destroyAllFloorsAt(act, performer, counter);
                break;
            }
            case 118: {
                if (source.getTemplateId() == 315 && WurmPermissions.mayUseGMWand(performer) && !Zones.isOnPvPServer(tilex, tiley)) {
                    Terraforming.rampantGrowth(performer, tilex, tiley);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 945: {
                if (!source.getTemplate().isRune() || !RuneUtilities.isSingleUseRune(source) || RuneUtilities.getSpellForRune(source) == null || !RuneUtilities.getSpellForRune(source).isTargetTile()) break;
                done = TileBehaviour.useRuneOnTile(act, performer, source, tilex, tiley, performer.getLayer(), heightOffset, action, counter);
                break;
            }
            case 910: {
                if (!onSurface || !source.getTemplate().isDiggingtool() && source.getTemplateId() != 493) break;
                done = TileBehaviour.investigateTile(act, performer, source, tilex, tiley, performer.getLayer(), tile, heightOffset, action, counter);
                break;
            }
            case 636: {
                if (source.getTemplateId() == 901) {
                    done = this.hold(act, performer, source, tilex, tiley, tile, counter);
                    break;
                }
                done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                break;
            }
            case 472: {
                done = true;
                if (source.getTemplateId() != 676 || source.getOwnerId() != performer.getWurmId()) break;
                MissionManager m = new MissionManager(performer, "Manage missions", "Select action", act.getTarget(), Tiles.getTile(type).getHelpSubject(Tiles.decodeData(tile)), source.getWurmId());
                m.sendQuestion();
                break;
            }
            case 486: {
                this.handle_TESTCASE(performer, source, tilex, tiley, tile);
                done = true;
                break;
            }
            case 462: {
                if (!Features.Feature.TRANSFORM_RESOURCE_TILES.isEnabled()) break;
                if (onSurface) {
                    if (source.getTemplateId() == 654 && source.getAuxData() != 0 && source.getBless() != null) {
                        if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled() && (source.getAuxData() == 1 && type == Tiles.Tile.TILE_SAND.id || source.getAuxData() == 2 && type == Tiles.Tile.TILE_GRASS.id || source.getAuxData() == 2 && type == Tiles.Tile.TILE_MYCELIUM.id || source.getAuxData() == 3 && type == Tiles.Tile.TILE_STEPPE.id || source.getAuxData() == 7 && type == Tiles.Tile.TILE_MOSS.id)) {
                            if (Zones.getKingdom(tilex, tiley) != performer.getKingdomId()) {
                                comm.sendNormalServerMessage("You can only transmutate to a resource tiles within your own kingdom influence.", (byte)3);
                                break;
                            }
                            done = this.handle_TRANSMUTATE(performer, source, tilex, tiley, tile, act, counter);
                            break;
                        }
                        if (source.getAuxData() == 4 && type == Tiles.Tile.TILE_CLAY.id || source.getAuxData() == 5 && type == Tiles.Tile.TILE_PEAT.id || source.getAuxData() == 6 && type == Tiles.Tile.TILE_TAR.id || source.getAuxData() == 8 && type == Tiles.Tile.TILE_TUNDRA.id) {
                            done = this.handle_TRANSMUTATE(performer, source, tilex, tiley, tile, act, counter);
                            break;
                        }
                        comm.sendNormalServerMessage("That would be a waste of this liquid.");
                        break;
                    }
                    comm.sendNormalServerMessage("That would be a waste of this liquid.");
                    break;
                }
                comm.sendNormalServerMessage("That would be a waste of this liquid.");
                break;
            }
            default: {
                if (source.isEnchantedTurret() || source.isUnenchantedTurret()) {
                    if (source.isSign()) {
                        done = MethodsItems.plantSign(performer, source, counter, false, 0, 0, performer.isOnSurface(), performer.getBridgeId(), false, -1L);
                        break;
                    }
                    done = true;
                    break;
                }
                if (!act.isSpell()) {
                    done = this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                    break;
                }
                done = true;
                Spell spell = Spells.getSpell(action);
                if (spell == null) {
                    logger.log(Level.INFO, performer.getName() + " tries to cast unknown spell:" + Actions.actionEntrys[action].getActionString());
                    comm.sendNormalServerMessage("That spell is unknown.");
                    break;
                }
                if (spell.religious) {
                    if (performer.getDeity() == null) {
                        comm.sendNormalServerMessage("You have no deity and cannot cast the spell.");
                        break;
                    }
                    if (!(source.isHolyItem(performer.getDeity()) || performer.isSpellCaster() || performer.isSummoner())) {
                        comm.sendNormalServerMessage(performer.getDeity().name + " will not let you use that item.");
                        break;
                    }
                    if (!Methods.isActionAllowed(performer, (short)245)) break;
                    done = Methods.castSpell(performer, spell, tilex, tiley, performer.getLayer(), heightOffset, counter);
                    break;
                }
                if (performer.isSpellCaster() || performer.isSummoner() || source.isMagicStaff() || source.getTemplateId() == 176 && performer.getPower() >= 2 && Servers.isThisATestServer()) {
                    if (!Methods.isActionAllowed(performer, (short)547)) break;
                    done = Methods.castSpell(performer, spell, tilex, tiley, performer.getLayer(), heightOffset, counter);
                    break;
                }
                comm.sendNormalServerMessage("You need to use a magic staff.");
                done = true;
            }
        }
        return done;
    }

    private void displayVillageInfo(Creature performer, Communicator comm, Village village, int tilex, int tiley, boolean inVillage) {
        String message = inVillage ? "This is within the village of " + village.getName() + "." : "This is within the perimeter of " + village.getName() + ".";
        int tokenX = village.getTokenX();
        int tokenY = village.getTokenY();
        int distX = Math.abs(tokenX - tilex);
        int distY = Math.abs(tokenY - tiley);
        int maxDist = Math.max(distX, distY);
        message = message + String.format(" %s has it's token %d tiles away at %d, %d.", village.getName(), maxDist, tokenX, tokenY);
        comm.sendNormalServerMessage(message);
    }

    private void handle_GETINFO(Creature performer, int tilex, int tiley, int stid) {
        VolaTile t;
        if (Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().mayInfoLocal()) {
            performer.getVisionArea().getSurface().sendHostileCreatures();
            performer.getVisionArea().getUnderGround().sendHostileCreatures();
            performer.getCultist().touchCooldown2();
            return;
        }
        if (performer.getPower() < 2) {
            return;
        }
        if (stid != 176 && stid != 315) {
            return;
        }
        ErrorChecks.getInfo(performer, tilex, tiley, performer.getLayer());
        Communicator comm = performer.getCommunicator();
        VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, true);
        Village village = vtile.getVillage();
        boolean inVillage = false;
        if (village != null) {
            inVillage = true;
        } else {
            village = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
        }
        if (village != null) {
            this.displayVillageInfo(performer, comm, village, tilex, tiley, inVillage);
        }
        if ((t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface())) != null) {
            Wall[] walls;
            Floor[] floors;
            Door[] doors;
            for (Door door : doors = t.getDoors()) {
                comm.sendNormalServerMessage(" door: " + door.getTileX() + ", " + door.getTileY());
            }
            for (Floor floor : floors = t.getFloors()) {
                comm.sendNormalServerMessage(" floor: " + floor.getTileX() + ", " + floor.getTileY() + " " + floor.getHeightOffset());
            }
            for (Wall wall : walls = t.getWalls()) {
                Object msg = "";
                if (performer.getPower() >= 3) {
                    msg = (String)msg + "#number=" + wall.getNumber();
                }
                comm.sendNormalServerMessage((String)msg + " wall at tile [" + wall.getTileX() + "," + wall.getTileY() + ", " + wall.getHeight() + "]:  start [" + wall.getStartX() + ", " + wall.getStartY() + "] to end [" + wall.getEndX() + ", " + wall.getEndY() + "] (t=" + (Object)((Object)wall.getType()) + ", state=" + (Object)((Object)wall.getState()) + ", indoor=" + wall.isIndoor() + ", m=" + wall.getMaterialString() + ")");
            }
        }
        try {
            TurretZone tz;
            HiveZone hz;
            FaithZone fz = Zones.getFaithZone(tilex, tiley, performer.isOnSurface());
            if (fz != null && fz.getCurrentRuler() != null) {
                int strength = Features.Feature.NEWDOMAINS.isEnabled() ? fz.getStrengthForTile(tilex, tiley, performer.isOnSurface()) : fz.getStrength();
                String faithZoneMessage = String.format("Faith zone for %s with strength of %d.", fz.getCurrentRuler().getName(), strength);
                comm.sendNormalServerMessage(faithZoneMessage);
            }
            if ((hz = Zones.getHiveZoneAt(tilex, tiley, performer.isOnSurface())) != null) {
                Item hive = hz.getCurrentHive();
                String hiveMessage = String.format("Hive named %s with strength %d found at %d, %d. Quality %.2f.", hive.getName(), hz.getStrengthForTile(tilex, tiley, performer.isOnSurface()), hive.getTileX(), hive.getTileY(), Float.valueOf(hive.getCurrentQualityLevel()));
                comm.sendNormalServerMessage(hiveMessage);
            }
            if ((tz = Zones.getTurretZone(tilex, tiley, performer.isOnSurface())) != null) {
                Item turret = tz.getZoneItem();
                comm.sendNormalServerMessage("Current turret: '" + turret.getName() + "' with strength: " + tz.getStrengthForTile(tilex, tiley, performer.isOnSurface()));
            }
            if (Features.Feature.TOWER_CHAINING.isEnabled() && t != null) {
                byte kingdom = t.getKingdom();
                InfluenceChain chain = InfluenceChain.getInfluenceChain(kingdom);
                Item closestTower = null;
                int closest = Integer.MAX_VALUE;
                for (Item item : chain.getChainMarkers()) {
                    if (!item.isGuardTower()) continue;
                    int distx = Math.abs(item.getTileX() - performer.getTileX());
                    int disty = Math.abs(item.getTileY() - performer.getTileY());
                    int currDist = Math.max(distx, disty);
                    if (closestTower == null) {
                        closestTower = item;
                        closest = currDist;
                        continue;
                    }
                    if (closest <= currDist) continue;
                    closestTower = item;
                    closest = currDist;
                }
                if (closestTower != null) {
                    String towerMessage = String.format("Kingdom %s has their closest tower %d tiles away at %d, %d.", Kingdoms.getNameFor(kingdom), closest, closestTower.getTileX(), closestTower.getTileY());
                    comm.sendNormalServerMessage(towerMessage);
                }
            }
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
    }

    private void handle_TESTCASE(Creature performer, Item source, int tilex, int tiley, int tile) {
        if (!Servers.localServer.testServer && performer.getPower() < 3) {
            return;
        }
        performer.getStatus().refresh(0.99f, true);
        Communicator comm = performer.getCommunicator();
        if (source.getTemplateId() == 315) {
            if (source.getAuxData() == 1 && performer.isOnSurface()) {
                int sx = Zones.safeTileX(performer.getTileX() - 10);
                int ex = Zones.safeTileX(performer.getTileX() + 10);
                int sy = Zones.safeTileY(performer.getTileY() - 10);
                int ey = Zones.safeTileY(performer.getTileY() + 10);
                for (int x = sx; x <= ex; ++x) {
                    for (int y = sy; y <= ey; ++y) {
                        int ttile = Zones.getTileIntForTile(x, y, 0);
                        if (Tiles.decodeType(ttile) != Tiles.Tile.TILE_TREE.id) continue;
                        int flowerType = Server.rand.nextInt(60000);
                        flowerType = flowerType >= 1000 ? 0 : (flowerType > 998 ? 7 : (flowerType > 990 ? 6 : (flowerType > 962 ? 5 : (flowerType > 900 ? 4 : (flowerType > 800 ? 3 : (flowerType > 500 ? 2 : 1))))));
                        Server.setSurfaceTile(x, y, Tiles.decodeHeight(ttile), Tiles.Tile.TILE_GRASS.id, (byte)flowerType);
                        Players.getInstance().sendChangedTile(x, y, true, false);
                    }
                }
                return;
            }
            if (source.getAuxData() == 2) {
                float xmod = 0.0f;
                float ymod = 0.0f;
                float ymode = 2.0f;
                float xmode = 2.0f;
                float stx = 0.0f;
                float sty = 0.0f;
                float etx = 0.0f;
                float ety = 0.0f;
                float posStartX = 0.0f;
                float posStartY = 0.0f;
                float posEndX = 0.0f;
                float posEndY = 0.0f;
                boolean blocked = true;
                for (TilePos tPos : TilePos.areaIterator(1, 1, 9, 9)) {
                    xmod = 4.0f * (float)tPos.x / 10.0f;
                    ymod = 4.0f * (float)tPos.y / 10.0f;
                    posStartX = (float)(performer.getTileX() * 4) + xmod;
                    posStartY = (float)(performer.getTileY() * 4) + ymod;
                    stx = posStartX / 4.0f;
                    sty = posStartY / 4.0f;
                    posEndX = (float)(tilex * 4) + xmode;
                    posEndY = (float)(tiley * 4) + ymode;
                    etx = posEndX / 4.0f;
                    ety = posEndY / 4.0f;
                    BlockingResult b = Blocking.getBlockerBetween(performer, posStartX, posStartY, posEndX, posEndY, performer.getPositionZ(), Zones.getHeightForNode((int)etx, (int)ety, this.isCave() ? -1 : 0), true, !this.isCave(), false, 6, -1L, performer.getBridgeId(), performer.getBridgeId(), false);
                    if (b == null) {
                        blocked = false;
                        comm.sendNormalServerMessage("A: No blocker between " + posStartX + "," + posStartY + " to " + posEndX + ", " + posEndY + " tiles :" + stx + "," + sty + " to " + etx + "," + ety);
                    }
                    for (TilePos tPos2 : TilePos.areaIterator(1, 1, 9, 9)) {
                        xmode = 4.0f * (float)tPos2.x / 10.0f;
                        ymode = 4.0f * (float)tPos2.y / 10.0f;
                        posStartX = (float)(performer.getTileX() * 4) + xmod;
                        posStartY = (float)(performer.getTileY() * 4) + ymod;
                        stx = posStartX / 4.0f;
                        sty = posStartY / 4.0f;
                        posEndX = (float)(tilex * 4) + xmode;
                        posEndY = (float)(tiley * 4) + ymode;
                        etx = posEndX / 4.0f;
                        ety = posEndY / 4.0f;
                        BlockingResult b2 = Blocking.getBlockerBetween(performer, posStartX, posStartY, posEndX, posEndY, performer.getPositionZ(), Zones.getHeightForNode((int)etx, (int)ety, this.isCave() ? -1 : 0), true, !this.isCave(), false, 6, -1L, performer.getBridgeId(), performer.getBridgeId(), false);
                        if (b2 != null) continue;
                        blocked = false;
                        comm.sendNormalServerMessage("B: No blocker between " + posStartX + "," + posStartY + " to " + posEndX + ", " + posEndY + " tiles :" + stx + "," + sty + " to " + etx + "," + ety);
                    }
                }
                if (blocked) {
                    comm.sendNormalServerMessage("A: " + stx + "," + sty + " to " + etx + "," + ety + " Blocked!");
                }
                if (blocked) {
                    comm.sendNormalServerMessage("B: " + stx + "," + sty + " to " + etx + "," + ety + " Blocked!");
                }
                return;
            }
            if (source.getAuxData() == 22) {
                if (performer.getPower() < 3) {
                    return;
                }
                ((Player)performer).reimbursePacks(true);
                return;
            }
            if (source.getAuxData() == 23) {
                if (performer.getPower() < 3) {
                    return;
                }
                ((Player)performer).reimbAnniversaryGift(true);
                return;
            }
        }
        if (source.getTemplateId() != 176) {
            TestQuestion tq = new TestQuestion(performer, tile);
            tq.sendQuestion();
            return;
        }
        switch (source.getAuxData()) {
            case 3: {
                Item inventory = performer.getInventory();
                float ql = 50.0f + Server.rand.nextFloat() * 40.0f;
                try {
                    Item c = Creature.createItem(274, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(274, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(279, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(277, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(277, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(278, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(278, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(275, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(276, ql);
                    inventory.insertItem(c);
                    performer.wearItems();
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 4: {
                Item inventory = performer.getInventory();
                try {
                    Item c = Creature.createItem(87, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(21, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(80, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(290, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(291, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(706, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(707, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                    c = Creature.createItem(705, 50.0f + Server.rand.nextFloat() * 40.0f);
                    inventory.insertItem(c, true);
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 5: {
                Item inventory = performer.getInventory();
                try {
                    float ql = 50.0f + Server.rand.nextFloat() * 40.0f;
                    Item c = Creature.createItem(105, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(105, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(107, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(106, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(106, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(103, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(103, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(108, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(104, ql);
                    inventory.insertItem(c);
                    performer.wearItems();
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 6: {
                Item inventory = performer.getInventory();
                try {
                    float ql = 50.0f + Server.rand.nextFloat() * 40.0f;
                    Item c = Creature.createItem(116, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(116, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(117, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(115, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(115, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(119, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(119, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(118, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(120, ql);
                    inventory.insertItem(c);
                    performer.wearItems();
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 7: {
                Item inventory = performer.getInventory();
                try {
                    float ql = 50.0f + Server.rand.nextFloat() * 40.0f;
                    Item c = Creature.createItem(474, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(474, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(476, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(477, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(477, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(478, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(478, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(475, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(285, ql);
                    inventory.insertItem(c);
                    performer.wearItems();
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 8: {
                Item inventory = performer.getInventory();
                try {
                    float ql = 50.0f + Server.rand.nextFloat() * 40.0f;
                    Item c = Creature.createItem(280, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(280, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(282, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(283, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(283, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(284, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(284, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(281, ql);
                    inventory.insertItem(c);
                    c = Creature.createItem(286, ql);
                    inventory.insertItem(c);
                    performer.wearItems();
                }
                catch (Exception ex) {
                    comm.sendNormalServerMessage("Failed to create:" + ex.getMessage());
                }
                break;
            }
            case 9: {
                EpicMission em;
                logger.log(Level.INFO, "Testing epic mission progress");
                if (performer.getDeity() == null || (em = EpicServerStatus.getEpicMissionForEntity(Deities.translateEntityForDeity(performer.getDeity().number))) == null) break;
                logger.log(Level.INFO, "Got " + em.getScenarioName() + ". Mission id=" + em.getMissionId());
                TriggerEffect[] trigs = TriggerEffects.getEffectsForMission(em.getMissionId());
                logger.log(Level.INFO, "Found " + trigs.length + " triggers");
                MissionPerformer mp = new MissionPerformer(performer.getWurmId());
                MissionPerformed perf = new MissionPerformed(em.getMissionId(), mp);
                for (TriggerEffect t : trigs) {
                    logger.log(Level.INFO, "Effect " + t.getName());
                    t.effect(performer, perf, performer.getWurmId(), false, true);
                }
                break;
            }
            case 10: {
                logger.log(Level.INFO, performer.getName() + " testing achievement " + source.getData1());
                performer.achievement(source.getData1(), source.getData2());
                break;
            }
            case 11: {
                logger.log(Level.INFO, performer.getName() + " testing encounters " + (byte)source.getData1() + ", " + (byte)source.getData2());
                EncounterType et = SpawnTable.getType((byte)source.getData1(), (byte)source.getData2());
                if (et == null) break;
                et.getRandomEncounter(performer);
                break;
            }
            case 12: {
                performer.modifyKarma(4);
                performer.setKarma(performer.getKarma() + 110);
                break;
            }
            case 14: {
                Zones.createBattleCamp(tilex, tiley);
                break;
            }
            case 15: {
                int template = source.getData1();
                try {
                    ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(template);
                    comm.sendNormalServerMessage(it.getName() + "...");
                }
                catch (Exception it) {
                    // empty catch block
                }
                EpicTargetItems targs = EpicTargetItems.getEpicTargets(performer.getKingdomTemplateId());
                comm.sendNormalServerMessage(targs.getGlobalMapPlacementRequirementString(template) + " (region " + targs.getGlobalMapPlacementRequirement(template) + ")");
                int current = targs.getCurrentCounter(template);
                comm.sendNormalServerMessage("Current counter =" + current);
                if (source.getData2() == 10) {
                    comm.sendNormalServerMessage("Setting current to " + current);
                    targs.testSetCounter(current, Server.rand.nextLong());
                }
                return;
            }
            case 16: {
                break;
            }
            case 17: {
                break;
            }
            case 18: {
                break;
            }
            case 19: {
                break;
            }
            case 20: {
                break;
            }
            case 21: {
                logger.log(Level.INFO, performer.getName() + " deity=" + performer.getDeity());
                if (performer.getDeity() == null) break;
                WcEpicStatusReport wce = new WcEpicStatusReport(WurmId.getNextWCCommandId(), true, performer.getDeity().number, 101, 4);
                wce.sendToLoginServer();
                break;
            }
            case 22: {
                if (!Servers.localServer.testServer) break;
                int deityNumber = 1;
                String entityName = "Fo";
                if (performer.getDeity() != null) {
                    deityNumber = performer.getDeity().number;
                    entityName = performer.getDeity().getName();
                }
                EpicServerStatus es = new EpicServerStatus();
                es.generateNewMissionForEpicEntity(deityNumber, entityName, 1, 600, entityName + "'s funny stuff", Server.rand.nextInt(), "You must really do this for " + entityName + " because yeah you know.", true);
                break;
            }
            case 23: {
                if (!Servers.localServer.testServer) break;
                int days = source.getData1();
                int times = source.getData2();
                int timesPerDay = 144;
                Skill lockPicking = null;
                try {
                    lockPicking = performer.getSkills().getSkill(10076);
                }
                catch (Exception ex) {
                    lockPicking = performer.getSkills().learn(10076, 1.0f);
                }
                lockPicking.minimum = lockPicking.getKnowledge();
                int nums = days * 144;
                int succ = 0;
                int fails = 0;
                comm.sendNormalServerMessage("Going to run for " + days + " days and " + 144 + "=" + nums + " times at factor " + times + ".");
                while (succ < nums) {
                    lockPicking.lastUsed = 0L;
                    if (lockPicking.skillCheck(lockPicking.getKnowledge(0.0), 10.0, false, times) > 0.0) {
                        ++succ;
                        continue;
                    }
                    ++fails;
                }
                comm.sendNormalServerMessage("Ok. " + succ + " successes after " + days + " days of practice. " + fails + " fails. Skill now at " + lockPicking.getKnowledge() + ". This is an epic server:" + Servers.localServer.EPIC);
                break;
            }
            case 24: {
                break;
            }
            case 25: {
                if (!Servers.localServer.testServer) break;
                ((Valrei)EpicServerStatus.getValrei()).testValreiFight(performer);
                break;
            }
            case 26: {
                if (!Servers.localServer.testServer) break;
                ((Valrei)EpicServerStatus.getValrei()).testSingleValreiFight(performer, source);
            }
        }
    }

    private boolean destroyAllFloorsAt(Action act, Creature performer, float counter) {
        Floor[] floors;
        Item tool = null;
        try {
            tool = Items.getItem(act.getSubjectId());
        }
        catch (NoSuchItemException nsie) {
            logger.log(Level.WARNING, nsie.getMessage(), nsie);
        }
        VolaTile vtile = Zones.getOrCreateTile(act.getTileX(), act.getTileY(), performer.getLayer() >= 0);
        for (Floor floor : floors = vtile.getFloors()) {
            FloorBehaviour.actionDestroyFloor(act, performer, tool, floor, act.getNumber(), counter);
        }
        return true;
    }

    private boolean herbalize(Action act, Creature performer, int tilex, int tiley, int tile, byte data, float counter) {
        return this.botanizeV11(act, performer, tilex, tiley, tile, data, counter);
    }

    private boolean forage(Action act, Creature performer, int tilex, int tiley, int tile, byte data, float counter) {
        boolean toReturn = false;
        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_ROCK.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_CLIFF.id) {
            Skill forage;
            if (Tiles.decodeHeight(tile) < -1) {
                performer.getCommunicator().sendNormalServerMessage("You can't rummage around down there.");
                return true;
            }
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you find.");
                return true;
            }
            int time = 200;
            boolean containsForage = Server.isForagable(tilex, tiley);
            if (counter == 1.0f) {
                forage = performer.getSkills().getSkillOrLearn(10071);
                if (!containsForage) {
                    if (performer.getPlayingTime() < 86400000L) {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean. New resources may appear over time so check back later.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean.");
                    }
                    return true;
                }
                time = (int)Math.max(100.0, (100.0 - forage.getKnowledge(0.0)) * 2.0);
                performer.getCommunicator().sendNormalServerMessage("You start to rummage for something useful.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to rummage for something useful.", performer, 5);
                performer.sendActionControl("rummaging", true, time);
                performer.getStatus().modifyStamina(-500.0f);
                act.setTimeLeft(time);
            } else {
                time = act.getTimeLeft();
            }
            if (counter * 10.0f >= (float)time) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                toReturn = true;
                forage = performer.getSkills().getSkillOrLearn(10071);
                int herbCreated = 0;
                int knowledge = (int)forage.getKnowledge(0.0);
                if (knowledge > 50) {
                    forage.skillCheck(knowledge - 20, 0.0, false, counter);
                } else {
                    forage.skillCheck(knowledge + 20, 0.0, false, counter);
                }
                if (containsForage) {
                    int num = Server.rand.nextInt(100);
                    if (num < 20) {
                        herbCreated = 146;
                    } else if (num < 95) {
                        herbCreated = 684;
                    } else if (num < 100) {
                        herbCreated = 446;
                    }
                    if (herbCreated > 0) {
                        TilePoller.setGrassHasSeeds(tilex, tiley, false, false);
                        double power = forage.skillCheck(knowledge, 0.0, false, counter);
                        try {
                            Item newItem = ItemFactory.createItem(herbCreated, Math.max(Math.abs((float)power * 0.1f), 1.0f), (byte)0, act.getRarity(), null);
                            Item inventory = performer.getInventory();
                            inventory.insertItem(newItem);
                            performer.getCommunicator().sendNormalServerMessage("You find " + newItem.getName() + "!");
                            Server.getInstance().broadCastAction(performer.getName() + " puts something in " + performer.getHisHerItsString() + " pocket.", performer, 5);
                            if (performer.getTutorialLevel() == 6 && !performer.skippedTutorial() && performer.getKingdomId() != 3) {
                                performer.missionFinished(true, true);
                            }
                        }
                        catch (FailedException fe) {
                            logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), fe);
                        }
                        catch (NoSuchTemplateException nst) {
                            logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
                        }
                    }
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You find nothing useful.");
                }
            }
        } else {
            toReturn = this.forageV11(act, performer, tilex, tiley, tile, data, counter);
        }
        return toReturn;
    }

    public static boolean isCloseTile(int cx, int cy, int tilex, int tiley) {
        return Math.abs(cx - tilex) <= 1 && Math.abs(cy - tiley) <= 1;
    }

    public static boolean isAdjacentTile(int cx, int cy, int tilex, int tiley) {
        if (cx == tilex && cy == tiley) {
            return false;
        }
        return Math.abs(cx - tilex) <= 1 && Math.abs(cy - tiley) <= 1;
    }

    public static boolean isAdjacent(int tilex, int tiley, int tilexx, int tileyy) {
        return tilex - tilexx == 0 || tiley - tileyy == 0;
    }

    public static boolean isSpike(Creature performer, int x, int y, boolean fix) {
        boolean min = true;
        int ms = Constants.meshSize;
        int max = (1 << ms) - 1;
        if (x > 1 && x < max && y > 1 && y < max) {
            int tile = Server.surfaceMesh.getTile(x, y);
            short height = Tiles.decodeHeight(tile);
            int prevTile = Server.surfaceMesh.getTile(x - 1, y);
            short prevHeight = Tiles.decodeHeight(prevTile);
            short nextHeight = Tiles.decodeHeight(Server.surfaceMesh.getTile(x + 1, y));
            if (Math.abs(prevHeight - height) > 500 || Math.abs(nextHeight - height) > 500) {
                if (fix) {
                    byte prevType = Tiles.decodeType(prevTile);
                    byte prevData = Tiles.decodeData(prevTile);
                    logger.log(Level.INFO, performer.getName() + " levelling layer at " + x + "," + y + ", height=" + height + " prevHeight=" + prevHeight);
                    Server.setSurfaceTile(x, y, prevHeight, prevType, prevData);
                    short prevRockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(x - 1, y));
                    Server.rockMesh.setTile(x, y, Tiles.encode(prevRockHeight, (short)0));
                    performer.getMovementScheme().touchFreeMoveCounter();
                    Players.getInstance().sendChangedTile(x, y, performer.isOnSurface(), true);
                    try {
                        Zone toCheckForChange = Zones.getZone(x, y, performer.isOnSurface());
                        toCheckForChange.changeTile(x, y);
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.INFO, "no such zone?: " + x + ", " + y, nsz);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static String getStringForTileEvent(TileEvent t) {
        String performer = "Unknown";
        PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(t.getPerformer());
        if (p != null) {
            performer = p.getName();
        }
        String action = "Unknown";
        try {
            action = Actions.getVerbForAction((short)t.getAction());
            if (Actions.actionEntrys[t.getAction()].isSpell()) {
                action = action + " ";
                action = action + Actions.actionEntrys[t.getAction()].getActionString();
                action = action + " (successful cast)";
            }
        }
        catch (Exception ex) {
            logger.warning("TileEvent malformed, event larger than ActionEntrys list. N=" + (t != null ? Integer.valueOf(t.getAction()) : null));
        }
        String date = new SimpleDateFormat("yyyy.MMM.dd.HH.mm.ss").format(new Timestamp(t.getDate()));
        return date + ' ' + performer + ' ' + action + " (" + t.getTileX() + ", " + t.getTileY() + "," + t.getLayer() + ")";
    }

    public static String getTileDescription(int encodedTile) {
        byte tileType = Tiles.decodeType(encodedTile);
        switch (tileType) {
            case 2: 
            case 5: 
            case 32: 
            case 33: {
                return "You see an eerie part of the lands of Wurm.";
            }
            case 3: 
            case 31: {
                return "You see an eerie part of the lands of Wurm.";
            }
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: {
                return "You see an eerie mine door";
            }
            case 4: {
                return "You see an eerie rock. It is part of the lands of Wurm.";
            }
            case 0: {
                return "You see a hole.";
            }
            case 1: {
                return "You see a lot of sand.";
            }
            case 15: {
                return "You see some planks laid down by someone.";
            }
            case 39: {
                return "You see some planks laid down by someone. They are protected from the environment by a thin layer of tar.";
            }
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: 
            case 13: 
            case 14: 
            case 16: 
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 30: 
            case 34: 
            case 43: {
                return "You see a part of the lands of Wurm.";
            }
        }
        return "You see a part of the lands of Wurm.";
    }

    static String getShardQlDescription(int shardQl) {
        if (shardQl < 10) {
            return "really poor quality";
        }
        if (shardQl < 30) {
            return "poor quality";
        }
        if (shardQl < 40) {
            return "acceptable quality";
        }
        if (shardQl < 60) {
            return "normal quality";
        }
        if (shardQl < 80) {
            return "good quality";
        }
        if (shardQl < 95) {
            return "very good quality";
        }
        return "utmost quality";
    }

    static boolean canCollectSnow(Creature performer, int tilex, int tiley, byte type, byte data) {
        if (!performer.isOnSurface()) {
            return false;
        }
        if (!WurmCalendar.isSeasonWinter()) {
            return false;
        }
        VolaTile vt = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
        if (vt != null && vt.getStructure() != null) {
            return false;
        }
        if (!Server.isGatherable(tilex, tiley)) {
            return false;
        }
        Tiles.Tile theTile = Tiles.getTile(type);
        if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_LAWN.id) {
            return true;
        }
        if (type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_MYCELIUM_LAWN.id) {
            return true;
        }
        if (type == Tiles.Tile.TILE_SLATE_SLABS.id || type == Tiles.Tile.TILE_MARBLE_SLABS.id || type == Tiles.Tile.TILE_STONE_SLABS.id || type == Tiles.Tile.TILE_SANDSTONE_SLABS.id) {
            return true;
        }
        return theTile.isNormalTree() || theTile.isNormalBush() || theTile.isMyceliumTree() || theTile.isMyceliumBush();
    }

    private boolean collectSnow(Action act, Creature performer, int tilex, int tiley, int tile, byte data, float counter) {
        boolean toReturn = false;
        byte tileType = Tiles.decodeType(tile);
        if (counter > 1.0f || TileBehaviour.canCollectSnow(performer, tilex, tiley, tileType, data)) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you find.");
                return true;
            }
            int time = 20;
            if (counter == 1.0f) {
                if (!Server.isGatherable(tilex, tiley)) {
                    if (performer.getPlayingTime() < 86400000L) {
                        performer.getCommunicator().sendNormalServerMessage("This is not enough snow here to make a snowball, try somewhere else. Snow will gather here over time if it snows enough, so check back later.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("This is not enough snow here to make a snowball, try somewhere else.");
                    }
                    return true;
                }
                int maxSearches = TileBehaviour.getFBGrassLength(tile).getCode() + 1;
                act.setNextTick(time);
                act.setTickCount(1);
                float totalTime = time * maxSearches;
                try {
                    performer.getCurrentAction().setTimeLeft((int)totalTime);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                performer.getCommunicator().sendNormalServerMessage("You start to collect snow in the area.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to collect snow in the area.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, (int)totalTime);
                performer.getStatus().modifyStamina(-100.0f);
            } else {
                time = act.getTimeLeft();
            }
            if (act.mayPlaySound()) {
                Methods.sendSound(performer, "sound.work.foragebotanize");
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                int maxSearches = TileBehaviour.getFBGrassLength(tile).getCode() + 1;
                act.incTickCount();
                act.incNextTick(20.0f);
                performer.getStatus().modifyStamina(-200 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                if (searchCount == 1) {
                    Server.setGatherable(tilex, tiley, false);
                }
                try {
                    float power = Math.min(((float)(maxSearches - searchCount + 1) + Server.rand.nextFloat()) * (float)Math.max(5 - maxSearches, 1) * 20.0f, 99.0f);
                    Item newItem = ItemFactory.createItem(1276, Math.max(Math.abs(power), 1.0f), (byte)0, act.getRarity(), null);
                    Item inventory = performer.getInventory();
                    inventory.insertItem(newItem);
                    performer.getCommunicator().sendNormalServerMessage("You collect enough snow to make a " + newItem.getName() + "!");
                    Server.getInstance().broadCastAction(performer.getName() + " puts something in " + performer.getHisHerItsString() + " pocket.", performer, 5);
                    if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                        performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                        toReturn = true;
                    }
                }
                catch (FailedException fe) {
                    logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), fe);
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            toReturn = true;
        }
        return toReturn;
    }

    private boolean botanizeV11(Action act, Creature performer, int tilex, int tiley, int tile, byte data, float counter) {
        boolean toReturn = false;
        byte tileType = Tiles.decodeType(tile);
        if (this.canBotanize(performer, tileType, data)) {
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you find.");
                return true;
            }
            int time = 200;
            boolean containsForage = Server.isForagable(tilex, tiley);
            boolean containsHerb = Server.isBotanizable(tilex, tiley);
            Skill botanize = performer.getSkills().getSkillOrLearn(10072);
            if (act.getNumber() != 224 && botanize.getKnowledge(0.0) <= 20.0) {
                return true;
            }
            if (counter == 1.0f) {
                if (!containsHerb) {
                    if (performer.getPlayingTime() < 86400000L) {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean. You should check another spot. New plants and herbs will grow over time so check back later.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean.", (byte)2);
                    }
                    return true;
                }
                int maxSearches = TileBehaviour.calcFBMaxSearches(TileBehaviour.getFBGrassLength(tile), botanize.getKnowledge(0.0));
                time = TileBehaviour.calcFBTickTimer(performer, botanize);
                act.setNextTick(time);
                act.setTickCount(1);
                float totalTime = time * maxSearches;
                try {
                    performer.getCurrentAction().setTimeLeft((int)totalTime);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                performer.getCommunicator().sendNormalServerMessage("You start to botanize in the area.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to botanize in the area.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, (int)totalTime);
                performer.getStatus().modifyStamina(-500.0f);
            } else {
                time = act.getTimeLeft();
            }
            if (act.mayPlaySound()) {
                Methods.sendSound(performer, "sound.work.foragebotanize");
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                GrassData.GrowthStage growthStage = TileBehaviour.getFBGrassLength(tile);
                int currentSkill = (int)botanize.getKnowledge(0.0);
                int maxSearches = TileBehaviour.calcFBMaxSearches(growthStage, currentSkill);
                act.incTickCount();
                act.incNextTick(TileBehaviour.calcFBTickTimer(performer, botanize));
                performer.getStatus().modifyStamina(-1500 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                int knowledge = (int)botanize.getKnowledge(0.0);
                GrassData.GrowthStage currentGrowthStage = GrassData.GrowthStage.fromInt(maxSearches - searchCount);
                Herb herb = Herb.getRandomHerb(performer, tileType, currentGrowthStage, act.getNumber(), knowledge, tilex, tiley);
                if (searchCount == 1) {
                    TilePoller.setGrassHasSeeds(tilex, tiley, containsForage, false);
                }
                if (herb == null) {
                    String stritem;
                    String string = act.getNumber() == 224 ? "anything" : (stritem = "any " + (act.getData() > 1L ? "more " : "") + act.getActionEntry().getActionString().toLowerCase());
                    if (maxSearches == 1) {
                        performer.getCommunicator().sendNormalServerMessage("You fail to find " + stritem + "!");
                    } else if (searchCount >= maxSearches) {
                        performer.getCommunicator().sendNormalServerMessage("You fail to find " + stritem + " and decide to stop looking!");
                    }
                    if (searchCount >= maxSearches && act.getData() == 0L) {
                        Server.getInstance().broadCastAction(performer.getName() + " looks displeased.", performer, 5);
                    }
                    botanize.skillCheck(0.0, 0.0, false, counter / (float)searchCount);
                } else {
                    float diff = herb.getDifficultyAt(knowledge);
                    double power = botanize.skillCheck(diff, 0.0, false, counter / (float)searchCount);
                    try {
                        float ql = Herb.getQL(power, knowledge);
                        Item newItem = ItemFactory.createItem(herb.getItem(), Math.max(1.0f, ql), herb.getMaterial(), act.getRarity(), null);
                        if (ql < 0.0f) {
                            newItem.setDamage(-ql / 2.0f);
                        } else {
                            newItem.setIsFresh(true);
                        }
                        Item inventory = performer.getInventory();
                        inventory.insertItem(newItem);
                        performer.getCommunicator().sendNormalServerMessage("You find " + newItem.getName() + "!");
                        Server.getInstance().broadCastAction(performer.getName() + " puts something in " + performer.getHisHerItsString() + " pocket.", performer, 5);
                        if (performer.getTutorialLevel() == 6 && !performer.skippedTutorial() && performer.getKingdomId() != 3) {
                            performer.missionFinished(true, true);
                        }
                        if (performer.checkCoinAward(100 * (maxSearches - searchCount + 1))) {
                            performer.getCommunicator().sendSafeServerMessage("You also find a rare coin!");
                        }
                        if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                            performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                            toReturn = true;
                        }
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), fe);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
                    }
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            toReturn = true;
        }
        return toReturn;
    }

    private boolean canBotanize(Creature performer, byte type, byte data) {
        Tiles.Tile theTile = Tiles.getTile(type);
        if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_MYCELIUM.id) {
            return true;
        }
        if (type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_MOSS.id || type == Tiles.Tile.TILE_PEAT.id) {
            try {
                Skill botanize = performer.getSkills().getSkill(10072);
                if (type == Tiles.Tile.TILE_MARSH.id && botanize.getKnowledge(0.0) >= 27.0) {
                    return true;
                }
                if (type == Tiles.Tile.TILE_MOSS.id && botanize.getKnowledge(0.0) >= 35.0) {
                    return true;
                }
                if (type == Tiles.Tile.TILE_PEAT.id && botanize.getKnowledge(0.0) >= 42.0) {
                    return true;
                }
            }
            catch (NoSuchSkillException noSuchSkillException) {}
        } else {
            if (theTile == Tiles.Tile.TILE_BUSH_LINGONBERRY) {
                return false;
            }
            if (theTile.isNormalTree() || theTile.isNormalBush() || theTile.isMyceliumTree() || theTile.isMyceliumBush()) {
                return GrassData.GrowthTreeStage.decodeTileData(data) != GrassData.GrowthTreeStage.LAWN;
            }
        }
        return false;
    }

    private boolean forageV11(Action act, Creature performer, int tilex, int tiley, int tile, byte data, float counter) {
        boolean toReturn = false;
        byte tileType = Tiles.decodeType(tile);
        if (this.canForage(performer, tileType, data)) {
            if (Tiles.decodeHeight(tile) < 0) {
                performer.getCommunicator().sendNormalServerMessage("Nothing useful will be found there.");
                return true;
            }
            if (!performer.getInventory().mayCreatureInsertItem()) {
                performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you find.");
                return true;
            }
            int time = 200;
            boolean containsForage = Server.isForagable(tilex, tiley);
            boolean containsHerb = Server.isBotanizable(tilex, tiley);
            Skill foraging = performer.getSkills().getSkillOrLearn(10071);
            if (act.getNumber() != 223 && foraging.getKnowledge(0.0) <= 20.0) {
                return true;
            }
            if (counter == 1.0f) {
                if (!containsForage) {
                    if (performer.getPlayingTime() < 86400000L) {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean. You should check another spot. New plants and herbs will grow over time so check back later.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("This area looks picked clean.");
                    }
                    return true;
                }
                int maxSearches = TileBehaviour.calcFBMaxSearches(TileBehaviour.getFBGrassLength(tile), foraging.getKnowledge(0.0));
                time = TileBehaviour.calcFBTickTimer(performer, foraging);
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
                performer.getCommunicator().sendNormalServerMessage("You start to forage in the area.");
                Server.getInstance().broadCastAction(performer.getName() + " starts to forage in the area.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, (int)totalTime);
                performer.getStatus().modifyStamina(-500.0f);
            } else {
                time = act.getTimeLeft();
            }
            if (act.mayPlaySound()) {
                Methods.sendSound(performer, "sound.work.foragebotanize");
            }
            if (counter * 10.0f >= act.getNextTick()) {
                if (act.getRarity() != 0) {
                    performer.playPersonalSound("sound.fx.drumroll");
                }
                int searchCount = act.getTickCount();
                GrassData.GrowthStage growthStage = TileBehaviour.getFBGrassLength(tile);
                int currentSkill = (int)foraging.getKnowledge(0.0);
                int maxSearches = TileBehaviour.calcFBMaxSearches(growthStage, currentSkill);
                act.incTickCount();
                act.incNextTick(TileBehaviour.calcFBTickTimer(performer, foraging));
                performer.getStatus().modifyStamina(-1500 * searchCount);
                if (searchCount >= maxSearches) {
                    toReturn = true;
                }
                int knowledge = (int)foraging.getKnowledge(0.0);
                GrassData.GrowthStage currentGrowthStage = GrassData.GrowthStage.fromInt(Math.max(0, maxSearches - searchCount));
                Forage forage = Forage.getRandomForage(performer, tileType, currentGrowthStage, act.getNumber(), knowledge, tilex, tiley);
                if (searchCount == 1) {
                    TilePoller.setGrassHasSeeds(tilex, tiley, false, containsHerb);
                }
                if (forage == null) {
                    String stritem;
                    String string = act.getNumber() == 223 ? "anything" : (stritem = "any " + (act.getData() > 1L ? "more " : "") + act.getActionEntry().getActionString().toLowerCase());
                    if (maxSearches == 1) {
                        performer.getCommunicator().sendNormalServerMessage("You fail to find " + stritem + "!");
                    } else if (searchCount >= maxSearches) {
                        performer.getCommunicator().sendNormalServerMessage("You fail to find " + stritem + " and decide to stop looking!");
                    }
                    if (searchCount >= maxSearches && act.getData() == 0L) {
                        Server.getInstance().broadCastAction(performer.getName() + " looks displeased.", performer, 5);
                    }
                    foraging.skillCheck(0.0, 0.0, false, counter / (float)searchCount);
                } else {
                    act.setData(act.getData() + 1L);
                    float diff = forage.getDifficultyAt(knowledge);
                    double power = foraging.skillCheck(diff, 0.0, false, counter / (float)searchCount);
                    try {
                        float ql = Forage.getQL(power, knowledge);
                        Item newItem = ItemFactory.createItem(forage.getItem(), Math.max(1.0f, ql), forage.getMaterial(), act.getRarity(), null);
                        if (ql < 0.0f) {
                            newItem.setDamage(-ql / 2.0f);
                        } else {
                            newItem.setIsFresh(true);
                        }
                        if (forage.getItem() == 464 && Items.mayLayEggs() && Server.rand.nextInt(5) == 0) {
                            newItem.setData1(48);
                        }
                        if (forage.getItem() == 466) {
                            performer.getCommunicator().sendNormalServerMessage("You find your Easter egg!");
                            if (performer.isPlayer()) {
                                try {
                                    ((Player)performer).getSaveFile().setReimbursed(true);
                                }
                                catch (IOException iOException) {}
                            }
                        } else if (newItem.getTemplateId() == 266) {
                            String mat = Materials.convertMaterialByteIntoString(newItem.getMaterial());
                            performer.getCommunicator().sendNormalServerMessage("You find " + StringUtilities.addGenus(mat) + " " + newItem.getName() + "!");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("You find " + StringUtilities.addGenus(newItem.getName()) + "!");
                        }
                        Item inventory = performer.getInventory();
                        inventory.insertItem(newItem);
                        if (performer.checkCoinAward(100 * (maxSearches - searchCount + 1))) {
                            performer.getCommunicator().sendSafeServerMessage("You also find a rare coin!");
                        }
                        Server.getInstance().broadCastAction(performer.getName() + " puts something in " + performer.getHisHerItsString() + " pocket.", performer, 5);
                        if (performer.getTutorialLevel() == 6 && !performer.skippedTutorial() && performer.getKingdomId() != 3) {
                            performer.missionFinished(true, true);
                        }
                        if (searchCount < maxSearches && !performer.getInventory().mayCreatureInsertItem()) {
                            performer.getCommunicator().sendNormalServerMessage("Your inventory is now full. You would have no space to put whatever you find.");
                            toReturn = true;
                        }
                    }
                    catch (FailedException fe) {
                        logger.log(Level.WARNING, performer.getName() + " " + fe.getMessage(), fe);
                    }
                    catch (NoSuchTemplateException nst) {
                        logger.log(Level.WARNING, performer.getName() + " " + nst.getMessage(), nst);
                    }
                }
                if (searchCount < maxSearches) {
                    act.setRarity(performer.getRarity());
                }
            }
        } else {
            toReturn = true;
        }
        return toReturn;
    }

    private boolean canForage(Creature performer, byte type, byte data) {
        Tiles.Tile theTile = Tiles.getTile(type);
        if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id) {
            return true;
        }
        if (type == Tiles.Tile.TILE_STEPPE.id || type == Tiles.Tile.TILE_TUNDRA.id || type == Tiles.Tile.TILE_MARSH.id) {
            try {
                Skill forage = performer.getSkills().getSkill(10071);
                if (type == Tiles.Tile.TILE_STEPPE.id && forage.getKnowledge(0.0) >= 23.0) {
                    return true;
                }
                if (type == Tiles.Tile.TILE_TUNDRA.id && forage.getKnowledge(0.0) >= 33.0) {
                    return true;
                }
                if (type == Tiles.Tile.TILE_MARSH.id && forage.getKnowledge(0.0) >= 43.0) {
                    return true;
                }
            }
            catch (NoSuchSkillException noSuchSkillException) {}
        } else if (theTile.isNormalTree() || theTile.isNormalBush() || theTile.isMyceliumTree() || theTile.isMyceliumBush()) {
            return GrassData.GrowthTreeStage.decodeTileData(data) != GrassData.GrowthTreeStage.LAWN;
        }
        return false;
    }

    private static int calcFBMaxSearches(GrassData.GrowthStage grassLength, double currentSkill) {
        return Math.min(grassLength.getCode() + 1, (int)(currentSkill + 28.0) / 27);
    }

    private static GrassData.GrowthStage getFBGrassLength(int tile) {
        byte tileType = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(tileType);
        if (tileType == Tiles.Tile.TILE_GRASS.id) {
            return GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile));
        }
        if (theTile.isTree() || theTile.isBush()) {
            return GrassData.GrowthStage.decodeTreeData(Tiles.decodeData(tile));
        }
        return GrassData.GrowthStage.SHORT;
    }

    private static int calcFBTickTimer(Creature performer, Skill skill) {
        return Actions.getQuickActionTime(performer, skill, null, 0.0);
    }

    private boolean hold(Action act, Creature performer, Item source, int tilex, int tiley, int tile, float counter) {
        boolean toReturn = false;
        int staminaDrainMod = 1;
        if (Servers.localServer.EPIC) {
            staminaDrainMod = 3;
        }
        int time = 12000;
        try {
            performer.getCurrentAction().setTimeLeft(time);
        }
        catch (NoSuchActionException nsa) {
            logger.log(Level.INFO, "This action does not exist?", nsa);
        }
        if (counter == 1.0f) {
            performer.getCommunicator().sendNormalServerMessage("You hold the range pole vertically in front of you.");
            Server.getInstance().broadCastAction(performer.getName() + " holds a range pole vertically in front of them.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, time);
            performer.getStatus().modifyStamina(-3000 / staminaDrainMod);
            act.setTickCount(0);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() % 5 == 0) {
            if (act.getTickCount() == 0) {
                act.incTickCount();
                Emotes.emoteAt((short)2014, performer, source);
            }
            performer.getStatus().modifyStamina(-2000 / staminaDrainMod);
        }
        if (performer.getStatus().getStamina() < 1000) {
            performer.getCommunicator().sendNormalServerMessage("Your arms have got tired holding the range pole vertical in front of you, so you stop.");
            Server.getInstance().broadCastAction(performer.getName() + " stops holding the range pole vertically in front of them.", performer, 5);
            toReturn = true;
        } else if (counter * 10.0f >= (float)act.getTimeLeft()) {
            performer.getCommunicator().sendNormalServerMessage("You have got borred holding the range pole vertical in front of you, so you stop.");
            Server.getInstance().broadCastAction(performer.getName() + " stops holding the range pole vertically in front of them.", performer, 5);
            toReturn = true;
        }
        return toReturn;
    }

    private static boolean checkTileDisembark(Creature performer, int tilex, int tiley) {
        try {
            float targPosz = Zones.calculateHeight((tilex << 2) + 2, (tiley << 2) + 2, performer.isOnSurface());
            float posz = performer.getPositionZ() + performer.getAltOffZ();
            if (Math.abs(posz - targPosz) > 6.0f && targPosz > -1.0f) {
                performer.getCommunicator().sendNormalServerMessage("That is too high.");
                return true;
            }
        }
        catch (NoSuchZoneException nsz) {
            performer.getCommunicator().sendNormalServerMessage("That place is inaccessible.");
            return true;
        }
        return false;
    }

    private boolean handle_TRANSMUTATE(Creature performer, Item source, int tilex, int tiley, int tile, Action act, float counter) {
        float mod;
        int templateWeight;
        VolaTile vt = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
        if (vt != null && vt.getStructure() != null && vt.getStructure().isTypeHouse()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot transform a tile in a house.", (byte)3);
            return true;
        }
        if (!performer.isPaying()) {
            performer.getCommunicator().sendNormalServerMessage("You need to have premium time left in order to transform tiles.", (byte)3);
            return true;
        }
        byte type = Tiles.decodeType(tile);
        Tiles.Tile theTile = Tiles.getTile(type);
        boolean reverting = Server.wasTransformed(tilex, tiley);
        float potionQL = source.getCurrentQualityLevel();
        int potionWeight = source.getWeightGrams();
        int numbSolid = potionWeight / (templateWeight = MethodsItems.getTransmutationSolidTemplateWeightGrams(source.getAuxData()));
        int extraQL = (int)((float)numbSolid * potionQL / (100.0f * (mod = MethodsItems.getTransmutationMod(performer, tilex, tiley, source.getAuxData(), reverting))));
        if (extraQL == 0) {
            performer.getCommunicator().sendNormalServerMessage("There is not enough " + source.getName() + " there to make any difference to the " + theTile.getName() + ".", (byte)3);
            return true;
        }
        if (counter == 1.0f) {
            if (!Server.isBeingTransformed(tilex, tiley)) {
                Server.setBeingTransformed(tilex, tiley, true);
                Server.setPotionQLCount(tilex, tiley, 0);
            }
            int time = 100;
            act.setTimeLeft(100);
            performer.getCommunicator().sendNormalServerMessage("You start to pour the " + source.getName() + " onto the " + theTile.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to pour something onto a " + theTile.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[462].getVerbString(), true, act.getTimeLeft());
        }
        if (act.currentSecond() == 2) {
            performer.getCommunicator().sendNormalServerMessage("You see the " + theTile.getName() + " absorb the " + source.getName() + ".");
        } else if (act.currentSecond() == 4) {
            performer.getCommunicator().sendNormalServerMessage("You see the " + theTile.getName() + " start to effervesce.");
        } else if (act.currentSecond() == 6) {
            performer.getCommunicator().sendNormalServerMessage("The bubbles now obscure the " + theTile.getName() + ".");
        } else if (act.currentSecond() == 8) {
            performer.getCommunicator().sendNormalServerMessage("The bubbles start receeding.");
        }
        if (act.currentSecond() % 2 == 0) {
            performer.getStatus().modifyStamina(-500.0f);
        }
        if (counter > (float)(act.getTimeLeft() / 10)) {
            int potionTileQLCount;
            int newTileQLCount;
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
                switch (act.getRarity()) {
                    case 1: {
                        extraQL = (int)((float)extraQL * 1.2f);
                        break;
                    }
                    case 2: {
                        extraQL = (int)((float)extraQL * 1.5f);
                        break;
                    }
                    case 3: {
                        extraQL = (int)((float)extraQL * 1.8f);
                    }
                }
            }
            if ((newTileQLCount = (potionTileQLCount = Server.getPotionQLCount(tilex, tiley)) + extraQL) >= 100) {
                byte newTileType = MethodsItems.getTransmutedToTileType(source.getAuxData());
                short height = Tiles.decodeHeight(tile);
                Server.setSurfaceTile(tilex, tiley, height, newTileType, (byte)0);
                Server.setBeingTransformed(tilex, tiley, false);
                Server.setTransformed(tilex, tiley, true);
                Players.getInstance().sendChangedTiles(tilex, tiley, 1, 1, true, true);
                try {
                    Zone toCheckForChange = Zones.getZone(tilex, tiley, true);
                    toCheckForChange.changeTile(tilex, tiley);
                }
                catch (NoSuchZoneException nsz) {
                    logger.log(Level.INFO, "no such zone?: " + tilex + ", " + tiley, nsz);
                }
                newTileQLCount = 0;
                Tiles.Tile newTile = Tiles.getTile(newTileType);
                performer.getCommunicator().sendNormalServerMessage("Yeah! You changed the " + theTile.getName() + " tile to " + newTile.getName() + "!", (byte)2);
                if (performer.fireTileLog()) {
                    TileEvent.log(tilex, tiley, 0, performer.getWurmId(), 462);
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("Looks like that tile needs more of that liquid to change it.", (byte)2);
            }
            Server.setPotionQLCount(tilex, tiley, newTileQLCount);
            Items.destroyItem(source.getWurmId());
            return true;
        }
        return false;
    }

    static void sendTileTransformationState(Creature performer, int tilex, int tiley, byte tileType) {
        if (tileType == Tiles.Tile.TILE_GRASS.id || tileType == Tiles.Tile.TILE_MYCELIUM.id || tileType == Tiles.Tile.TILE_SAND.id || tileType == Tiles.Tile.TILE_STEPPE.id || tileType == Tiles.Tile.TILE_CLAY.id || tileType == Tiles.Tile.TILE_PEAT.id || tileType == Tiles.Tile.TILE_TAR.id) {
            if (Server.wasTransformed(tilex, tiley)) {
                performer.getCommunicator().sendNormalServerMessage("The tile has been transformed before.");
            }
            if (Server.isBeingTransformed(tilex, tiley)) {
                int potionCount = Server.getPotionQLCount(tilex, tiley);
                if (potionCount == 255) {
                    return;
                }
                if (potionCount > 97) {
                    performer.getCommunicator().sendNormalServerMessage("The tile is so close to being completely transformed.");
                } else if (potionCount > 90) {
                    performer.getCommunicator().sendNormalServerMessage("The tile has almost been completely transformed.");
                } else if (potionCount > 75) {
                    performer.getCommunicator().sendNormalServerMessage("The tile is over three quarters transformed.");
                } else if (potionCount > 50) {
                    performer.getCommunicator().sendNormalServerMessage("The tile is over half way transformed.");
                } else if (potionCount > 25) {
                    performer.getCommunicator().sendNormalServerMessage("The tile is over a quarter transformed.");
                } else if (potionCount > 0) {
                    performer.getCommunicator().sendNormalServerMessage("Someone has started transforming this tile.");
                }
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    private static boolean useRuneOnTile(Action act, Creature performer, Item source, int x, int y, int layer, int heightOffset, short action, float counter) {
        double power;
        double diff;
        Skill soulDepth;
        block9: {
            if (RuneUtilities.isEnchantRune(source)) {
                performer.getCommunicator().sendNormalServerMessage("You cannot use the rune on that.", (byte)3);
                return true;
            }
            if (RuneUtilities.isSingleUseRune(source) && RuneUtilities.getSpellForRune(source) != null && !Methods.isActionAllowed(performer, (short)245, x, y)) {
                performer.getCommunicator().sendNormalServerMessage("You are not allowed to use that here.", (byte)3);
                return true;
            }
            int time = act.getTimeLeft();
            if (counter == 1.0f) {
                String actionString = "use the rune";
                performer.getCommunicator().sendNormalServerMessage("You start to use the rune.");
                time = Actions.getSlowActionTime(performer, performer.getSoulDepth(), null, 0.0);
                act.setTimeLeft(time);
                performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
                performer.getStatus().modifyStamina(-600.0f);
            }
            if (act.currentSecond() % 5 == 0) {
                performer.getStatus().modifyStamina(-300.0f);
            }
            if (!(counter * 10.0f > (float)time)) {
                return false;
            }
            soulDepth = performer.getSoulDepth();
            power = soulDepth.skillCheck(diff = (double)(20.0f + source.getDamage()) - ((double)(source.getCurrentQualityLevel() + (float)source.getRarity()) - 45.0), source.getCurrentQualityLevel(), false, counter);
            if (power > 0.0) {
                if (RuneUtilities.getSpellForRune(source) != null && RuneUtilities.getSpellForRune(source).isTargetTile()) {
                    RuneUtilities.getSpellForRune(source).castSpell(50.0, performer, x, y, layer, heightOffset);
                    break block9;
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You can't use the rune on that.", (byte)3);
                    return true;
                }
            }
            performer.getCommunicator().sendNormalServerMessage("You try to use the rune but fail.", (byte)3);
        }
        if (Servers.isThisATestServer()) {
            performer.getCommunicator().sendNormalServerMessage("Diff: " + diff + ", bonus: " + source.getCurrentQualityLevel() + ", sd: " + soulDepth.getKnowledge() + ", power: " + power + ", chance: " + soulDepth.getChance(diff, null, source.getCurrentQualityLevel()));
        }
        Items.destroyItem(source.getWurmId());
        return true;
    }

    private static boolean investigateTile(Action act, Creature performer, Item source, int tilex, int tiley, int layer, int tile, int heightOffset, short action, float counter) {
        if (!source.getTemplate().isDiggingtool() && source.getTemplateId() != 493) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " wouldn't really help for investigating this area.", (byte)3);
            return true;
        }
        if (!performer.getInventory().mayCreatureInsertItem()) {
            performer.getCommunicator().sendNormalServerMessage("You have no more space in your inventory for any found items. Make some room first.", (byte)3);
            return true;
        }
        if (!performer.canCarry(1000)) {
            performer.getCommunicator().sendNormalServerMessage("You're unable to carry the weight of any found items. Make some room first.", (byte)3);
            return true;
        }
        Skill archaeology = performer.getSkills().getSkillOrLearn(10069);
        double archSkill = archaeology.getKnowledge(0.0);
        if (source.getTemplate().isDiggingtool() && archSkill < 20.0) {
            performer.getCommunicator().sendNormalServerMessage("You don't have enough skill to use the " + source.getName() + " as an investigative tool.", (byte)3);
            return true;
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
        if (t != null && t.getStructure() != null) {
            performer.getCommunicator().sendNormalServerMessage("You decide against investigating inside the structure. Maybe outside will be better.");
            return true;
        }
        boolean canInvestigate = Server.isInvestigatable(tilex, tiley);
        int time = act.getTimeLeft();
        if (counter == 1.0f) {
            if (!canInvestigate) {
                performer.getCommunicator().sendNormalServerMessage("The area looks picked clean.");
                return true;
            }
            performer.getCommunicator().sendNormalServerMessage("You start to investigate the area.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to investigate the area.", performer, 5);
            time = Actions.getVariableActionTime(performer, archaeology, source, 0.0, 200, 60, 2500);
            if (source.getTemplateId() == 493) {
                time = (int)((float)time - (float)time / 10.0f);
            }
            act.setTimeLeft(time);
            performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
            performer.getStatus().modifyStamina(-1000.0f);
        }
        if (act.currentSecond() % 5 == 0) {
            double power;
            performer.getStatus().modifyStamina(-500.0f);
            if ((Server.rand.nextInt(25) == 0 || performer.getPower() >= 4) && (power = archaeology.skillCheck(30.0, source, 0.0, false, 10.0f)) > 0.0) {
                ArrayList<DeadVillage> currentTargets = Villages.getDeadVillagesFor(tilex, tiley);
                ArrayList<DeadVillage> nearbyTargets = Villages.getDeadVillagesNear(tilex, tiley, (int)(power * 2.5));
                nearbyTargets.removeAll(currentTargets);
                if (!nearbyTargets.isEmpty()) {
                    String toSend = "You spot some markers of an old settlement in the area.";
                    if (archSkill >= 30.0) {
                        int randomDeed = Server.rand.nextInt(nearbyTargets.size());
                        DeadVillage actualDeed = nearbyTargets.get(randomDeed);
                        String distance = "off";
                        if (archSkill >= 50.0) {
                            distance = actualDeed.getDistanceFrom(tilex, tiley);
                        }
                        toSend = toSend + " It looks like it may be " + distance + " to the " + actualDeed.getDirectionFrom(tilex, tiley) + " from here.";
                        if (archSkill >= 70.0) {
                            toSend = toSend + " You find a small scrap of something that has the deed name on it... '" + actualDeed.getDeedName() + "'.";
                        }
                    }
                    performer.getCommunicator().sendNormalServerMessage(toSend);
                } else if (currentTargets.isEmpty()) {
                    performer.getCommunicator().sendNormalServerMessage("You can't find any traces of any recognizable nearby settlements.");
                } else {
                    performer.getCommunicator().sendNormalServerMessage("You can't find any traces of any other distance settlements, only what used to be in this area.");
                }
            }
        }
        if (counter * 10.0f > (float)time) {
            if (act.getRarity() != 0) {
                performer.playPersonalSound("sound.fx.drumroll");
            }
            byte type = Tiles.decodeType(tile);
            ArrayList<DeadVillage> possibleTargets = Villages.getDeadVillagesFor(tilex, tiley);
            double negBase = 0.1 * archSkill;
            double negBonus = 0.0;
            Server.setInvestigatable(tilex, tiley, false);
            if (possibleTargets.isEmpty()) {
                negBonus += negBase * 3.0;
                ArrayList<DeadVillage> nearbyTargets = Villages.getDeadVillagesNear(tilex, tiley, (int)(archSkill * 2.0));
                if (nearbyTargets.isEmpty()) {
                    negBonus += negBase * 3.0;
                }
            } else {
                for (DeadVillage dv : possibleTargets) {
                    negBonus -= Math.min(negBase * 5.0, (double)(dv.getTotalAge() * dv.getTimeSinceDisband()));
                    if (dv.getKingdomId() == performer.getKingdomId()) continue;
                    negBonus -= 5.0;
                }
            }
            if (Terraforming.isRoad(type)) {
                negBonus += negBase * 2.0;
            } else if (Tiles.isGrassType(type) || Tiles.isTree(type)) {
                GrassData.GrowthStage grass;
                GrassData.GrowthStage growthStage = grass = Tiles.isGrassType(type) ? GrassData.GrowthStage.decodeTileData(Tiles.decodeData(tile)) : GrassData.GrowthStage.decodeTreeData(Tiles.decodeData(tile));
                if (grass == GrassData.GrowthStage.TALL) {
                    negBonus -= negBase * 0.5;
                } else if (grass == GrassData.GrowthStage.WILD) {
                    negBonus -= negBase;
                }
            }
            if (t != null) {
                Fence[] allFences;
                if (t.getVillage() != null) {
                    negBonus += negBase * 2.0;
                }
                if ((allFences = t.getAllFences()).length > 0) {
                    negBonus += (double)allFences.length * negBase;
                }
                negBonus += (double)t.getNumberOfItems(0) * negBase * 0.5;
                negBonus += (double)t.getNumberOfDecorations(0) * negBase * 0.5;
            }
            if (source.isDiggingtool()) {
                negBonus += negBase;
            }
            double tileMax = Math.min(archSkill + (100.0 - archSkill) * (double)0.2f, archSkill * 0.75 - negBonus);
            double diffBonus = Math.max(0.0, Math.min(archSkill * 0.25, -negBonus * 2.0 - tileMax) * 0.5);
            double power = archaeology.skillCheck(Math.max(tileMax * (double)0.9f - diffBonus, archSkill / 5.0), source, 0.0, false, counter);
            double difference = Math.max(0.0, archSkill - tileMax);
            try {
                performer.getSkills().getSkillOrLearn(source.getPrimarySkill()).skillCheck(Math.max(tileMax, archSkill / 5.0), 0.0, false, counter);
            }
            catch (NoSuchSkillException noSuchSkillException) {
                // empty catch block
            }
            source.setDamage((float)((double)source.getDamage() + (100.0 - power) * (double)1.0E-4f * (double)source.getDamageModifier()));
            if (Servers.localServer.testServer) {
                performer.getCommunicator().sendNormalServerMessage("[TEST: negBonus: " + negBonus + ", tileMax: " + tileMax + ", arch: " + archSkill + ", power: " + power + ", #deadDeeds: " + possibleTargets.size() + "]");
            }
            if (power >= difference) {
                try {
                    double fragmentPower = tileMax < 0.0 && Server.rand.nextInt(3) == 0 ? archSkill / 5.0 : tileMax;
                    FragmentUtilities.Fragment f = FragmentUtilities.getRandomFragmentForSkill(fragmentPower, !(power >= tileMax && tileMax >= archSkill));
                    if (f != null) {
                        Item fragment = ItemFactory.createItem(1307, (float)Math.min(power, archSkill + (100.0 - archSkill) * (double)0.2f), act.getRarity(), null);
                        fragment.setRealTemplate(f.getItemId());
                        if (fragment.getRealTemplate().getMaterial() != f.getMaterial()) {
                            fragment.setMaterial((byte)f.getMaterial());
                        }
                        if (power > 75.0) {
                            fragment.setData1(1);
                            fragment.setData2((int)(power / 2.0));
                            fragment.setAuxData((byte)1);
                            fragment.setWeight(fragment.getRealTemplate().getWeightGrams() / fragment.getRealTemplate().getFragmentAmount(), false);
                        }
                        performer.getInventory().insertItem(fragment);
                        performer.getCommunicator().sendNormalServerMessage("You pick out a fragment of some item wedged into the ground." + (Servers.localServer.testServer ? " [TEST: " + fragment.getActualName() + " ql: " + fragment.getCurrentQualityLevel() + " realTemplate: " + fragment.getRealTemplate().getName() + "]" : ""));
                        Server.getInstance().broadCastAction(performer.getName() + " looks excited as " + performer.getHeSheItString() + " picks up a small fragment of something.", performer, 5);
                        performer.achievement(479);
                        if (fragment.getRarity() > 0) {
                            performer.achievement(481);
                        }
                    }
                    if (WurmCalendar.isAnniversary() && !performer.hasFlag(55) && Server.rand.nextInt(15) == 0 && performer.isPaying()) {
                        Item specialFragment = ItemFactory.createItem(1307, 80.0f, act.getRarity(), null);
                        specialFragment.setRealTemplate(651);
                        specialFragment.setName("special fragment");
                        performer.getInventory().insertItem(specialFragment);
                        performer.getCommunicator().sendSafeServerMessage("You also find another fragment that looks a bit different.");
                    }
                }
                catch (FailedException | NoSuchTemplateException fragmentPower) {
                    // empty catch block
                }
            }
            if (power > 0.0 && !possibleTargets.isEmpty()) {
                EpicMission m;
                StringBuilder toSend = new StringBuilder();
                if (archSkill >= 15.0 && Server.rand.nextInt(3) == 0) {
                    DeadVillage dv;
                    if (possibleTargets.size() > 1) {
                        dv = possibleTargets.get(Server.rand.nextInt(possibleTargets.size()));
                        toSend.append("You can see multiple markers of abandoned settlements here. ");
                        if (archSkill >= 20.0) {
                            toSend.append("Based on your knowledge of the area and small hints you can find, one of the settlements must have been called " + dv.getDeedName() + ". ");
                        }
                    } else {
                        dv = possibleTargets.get(0);
                        toSend.append("You can see signs of a single abandoned settlement here. ");
                        if (archSkill >= 20.0) {
                            toSend.append("Based on your knowledge of the area and small hints you can find, the settlement must have been called " + dv.getDeedName() + ". ");
                        }
                    }
                    Item report = null;
                    for (Item i : performer.getInventory().getAllItems(false)) {
                        boolean gotExistingReport = false;
                        if (i.getTemplateId() == 1404) {
                            for (Item j : i.getAllItems(false)) {
                                if (j.getTemplateId() != 1403) continue;
                                if (report == null && j.getData() == -1L) {
                                    report = j;
                                    continue;
                                }
                                if (j.getData() != dv.getDeedId()) continue;
                                report = j;
                                gotExistingReport = true;
                                break;
                            }
                        }
                        if (gotExistingReport) break;
                    }
                    if (report != null && report.getData() == -1L) {
                        report.setData(dv.getDeedId());
                        report.setName(dv.getDeedName() + " report");
                        report.setAuxBit(0, true);
                        report.sendUpdate();
                        toSend.append("You write down some initial location details about " + dv.getDeedName() + " in a blank report. ");
                    } else if (report != null) {
                        if (!report.getAuxBit(1) && archSkill >= 25.0 && (Server.rand.nextInt(5) == 0 || performer.getPower() >= 4)) {
                            report.setAuxBit(1, true);
                            toSend.append("You note some additional details about the location of " + dv.getDeedName() + " in the report. ");
                        } else if (report.getAuxBit(1) && !report.getAuxBit(2) && archSkill >= 30.0 && (Server.rand.nextInt(10) == 0 || performer.getPower() >= 4)) {
                            report.setAuxBit(2, true);
                            toSend.append("You find some extra clues that help narrow down where " + dv.getDeedName() + " once was and write it down in the report. ");
                        } else if (report.getAuxBit(2) && !report.getAuxBit(3) && archSkill >= 35.0 && (Server.rand.nextInt(20) == 0 || performer.getPower() >= 4)) {
                            report.setAuxBit(3, true);
                            toSend.append("You feel confident you know exactly where " + dv.getDeedName() + " once lay, and complete the location details in the report. ");
                        }
                    }
                    if (archSkill >= 40.0 && (Server.rand.nextInt(10) == 0 || performer.getPower() >= 4)) {
                        toSend.append("You find a scrap of washed out parchment signed by the last mayor, " + dv.getMayorName() + ". ");
                        if (report != null && !report.getAuxBit(4)) {
                            report.setAuxBit(4, true);
                            toSend.append("You write that down in your report. ");
                        }
                    }
                    if (archSkill >= 55.0 && (Server.rand.nextInt(10) == 0 || performer.getPower() >= 4)) {
                        toSend.append("You recall this settlement, and remember the name of the founder as " + dv.getFounderName() + ". ");
                        if (report != null && !report.getAuxBit(5)) {
                            report.setAuxBit(5, true);
                            toSend.append("You write that down in your report. ");
                        }
                    }
                    if (archSkill >= 70.0 && (Server.rand.nextInt(10) == 0 || performer.getPower() >= 4)) {
                        toSend.append("It looks to have been abandoned for roughly " + DeadVillage.getTimeString(dv.getTimeSinceDisband(), dv.getTimeSinceDisband() > 12.0f) + ". ");
                        if (report != null && !report.getAuxBit(6)) {
                            report.setAuxBit(6, true);
                            toSend.append("You write that down in your report. ");
                        }
                    }
                    if (archSkill >= 80.0 && (Server.rand.nextInt(10) == 0 || performer.getPower() >= 4)) {
                        toSend.append("You make a rough estimate that the settlement was inhabited for about " + DeadVillage.getTimeString(dv.getTotalAge(), false) + ". ");
                        if (report != null && !report.getAuxBit(7)) {
                            report.setAuxBit(7, true);
                            toSend.append("You write that down in your report. ");
                        }
                    }
                    if (report == null) {
                        toSend.append("If you had an archaeology journal with a blank report in it you could record your findings.");
                    }
                } else {
                    toSend.append("You can't quite make anything definitive out, but there may have once been a settlement here.");
                }
                performer.getCommunicator().sendNormalServerMessage(toSend.toString());
                if (Server.rand.nextInt(50) == 0 && (t != null && t.getVillage() == null || t == null) && (m = EpicServerStatus.getMISacrificeMission()) != null) {
                    try {
                        Item missionItem = ItemFactory.createItem(737, 20 + Server.rand.nextInt(80), act.getRarity(), m.getEntityName());
                        missionItem.setName(HexMap.generateFirstName(m.getMissionId()) + ' ' + HexMap.generateSecondName(m.getMissionId()));
                        performer.getInventory().insertItem(missionItem);
                        performer.getCommunicator().sendNormalServerMessage("You find a " + missionItem.getName() + " in amongst the dirt.");
                    }
                    catch (FailedException | NoSuchTemplateException wurmServerException) {}
                }
            } else if (power > 0.0) {
                performer.getCommunicator().sendNormalServerMessage("You can't find any traces of any abandoned settlements here.");
            } else {
                performer.getCommunicator().sendNormalServerMessage("You can't seem to find anything of use.");
            }
            return true;
        }
        return false;
    }
}

