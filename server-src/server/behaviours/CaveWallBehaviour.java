/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.TileBehaviour;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.behaviours.WurmPermissions;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.questions.OreQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class CaveWallBehaviour
extends TileBehaviour {
    private static final Logger logger = Logger.getLogger(CaveWallBehaviour.class.getName());

    CaveWallBehaviour() {
        super((short)38);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, tilex, tiley, onSurface, tile));
        byte type = Tiles.decodeType(tile);
        if (performer.getDeity() != null && performer.getDeity().isMountainGod()) {
            Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || CaveWallBehaviour.isPartlyClad(type)) {
            toReturn.add(Actions.actionEntrys[607]);
        }
        return toReturn;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        LinkedList<ActionEntry> toReturn = new LinkedList<ActionEntry>();
        toReturn.addAll(super.getBehavioursFor(performer, subject, tilex, tiley, onSurface, tile));
        byte type = Tiles.decodeType(tile);
        if (type != Tiles.Tile.TILE_LAVA.id) {
            if (subject.isMiningtool()) {
                if (!CaveWallBehaviour.isClad(type)) {
                    toReturn.add(new ActionEntry(-4, "Mining", "Mining options"));
                    toReturn.add(Actions.actionEntrys[145]);
                    toReturn.add(Actions.actionEntrys[147]);
                    toReturn.add(Actions.actionEntrys[146]);
                    toReturn.add(Actions.actionEntrys[156]);
                }
            } else if (subject.getTemplateId() == 429 && type == Tiles.Tile.TILE_CAVE_WALL.id) {
                toReturn.add(Actions.actionEntrys[229]);
            } else if (subject.getTemplateId() == 525) {
                toReturn.add(Actions.actionEntrys[94]);
            } else if (subject.getTemplateId() == 526 || subject.getTemplateId() == 667) {
                if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                    toReturn.add(Actions.actionEntrys[118]);
                }
            } else if (subject.getTemplateId() == 668) {
                toReturn.add(Actions.actionEntrys[78]);
            }
            if (performer.getDeity() != null && performer.getDeity().isMountainGod()) {
                Methods.addActionIfAbsent(toReturn, Actions.actionEntrys[141]);
            }
            if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id && (subject.getTemplateId() == 493 || subject.isWand() && performer.getPower() >= 4)) {
                LinkedList<ActionEntry> reinforce = new LinkedList<ActionEntry>();
                reinforce.add(Actions.actionEntrys[856]);
                reinforce.add(Actions.actionEntrys[857]);
                reinforce.add(Actions.actionEntrys[858]);
                reinforce.add(Actions.actionEntrys[859]);
                reinforce.add(Actions.actionEntrys[860]);
                reinforce.add(Actions.actionEntrys[861]);
                if (subject.isWand() && performer.getPower() >= 4) {
                    reinforce.add(Actions.actionEntrys[862]);
                }
                Collections.sort(reinforce);
                toReturn.add(new ActionEntry((short)(-reinforce.size()), "Clad", "Clad options"));
                toReturn.addAll(reinforce);
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id && (subject.getTemplateId() == 63 || subject.getTemplateId() == 62)) {
                toReturn.add(Actions.actionEntrys[862]);
            } else if (subject.getTemplateId() == 493 || subject.isWand() && performer.getPower() >= 4) {
                if (type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id) {
                    toReturn.add(new ActionEntry(856, "Continue cladding", "cladding"));
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id) {
                    toReturn.add(new ActionEntry(857, "Continue cladding", "cladding"));
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id) {
                    toReturn.add(new ActionEntry(858, "Continue cladding", "cladding"));
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id) {
                    toReturn.add(new ActionEntry(859, "Continue cladding", "cladding"));
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id) {
                    toReturn.add(new ActionEntry(860, "Continue cladding", "cladding"));
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id) {
                    toReturn.add(new ActionEntry(861, "Continue cladding", "cladding"));
                } else if (subject.isWand() && performer.getPower() >= 4 && type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id) {
                    toReturn.add(new ActionEntry(862, "Continue cladding", "cladding"));
                }
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id && (subject.getTemplateId() == 63 || subject.getTemplateId() == 62)) {
                toReturn.add(new ActionEntry(862, "Continue cladding", "cladding"));
            }
            if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id && (subject.getTemplateId() == 130 || subject.isWand() && performer.getPower() >= 4)) {
                toReturn.add(Actions.actionEntrys[847]);
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.id && (subject.getTemplateId() == 1115 || subject.isWand() && performer.getPower() >= 4)) {
                toReturn.add(new ActionEntry(847, "Remove render", "removing"));
            } else if (CaveWallBehaviour.isClad(type) && (subject.getTemplateId() == 1115 || subject.isWand() && performer.getPower() >= 4)) {
                toReturn.add(new ActionEntry(78, "Remove cladding", "removing"));
            }
            if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || CaveWallBehaviour.isPartlyClad(type)) {
                toReturn.add(Actions.actionEntrys[607]);
            }
        }
        return toReturn;
    }

    public static boolean isClad(byte type) {
        return type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_SLATE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_POTTERY_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_MARBLE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id;
    }

    public static boolean isPartlyClad(byte type) {
        return type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id;
    }

    @Override
    public boolean action(Action act, Creature performer, int tilex, int tiley, boolean onSurface, int tile, int dir, short action, float counter) {
        boolean done = true;
        byte type = Tiles.decodeType(tile);
        if (action == 1) {
            Communicator comm = performer.getCommunicator();
            if (Tiles.isOreCave(type)) {
                if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_COPPER.id) {
                    comm.sendNormalServerMessage("A vein of pure copper emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GOLD.id) {
                    comm.sendNormalServerMessage("A vein of pure gold emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_SILVER.id) {
                    comm.sendNormalServerMessage("A vein of pure silver emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_TIN.id) {
                    comm.sendNormalServerMessage("A vein of pure tin emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_IRON.id) {
                    comm.sendNormalServerMessage("A vein of pure iron emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ZINC.id) {
                    comm.sendNormalServerMessage("A vein of pure zinc emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_LEAD.id) {
                    comm.sendNormalServerMessage("A vein of pure lead emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_ADAMANTINE.id) {
                    comm.sendNormalServerMessage("A vein of black adamantine emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_ORE_GLIMMERSTEEL.id) {
                    comm.sendNormalServerMessage("A vein of shiny glimmersteel emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE.id) {
                    comm.sendNormalServerMessage("A vein of an interlocking mosaic of carbonate crystals, otherwise known as marble, emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_SLATE.id) {
                    comm.sendNormalServerMessage("A vein of fine-grained, foliated, homogeneous metamorphic rock, or slate as we know it, emerges here.");
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
                    comm.sendNormalServerMessage("A vein of sand-sized minerals or rock grains, composed of quartz or feldspar, because these are the most common minerals in the Wurm's crust.");
                } else {
                    comm.sendNormalServerMessage("Unknown vein!");
                }
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams and metal bands.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and planks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and stone bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_SLATE_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and slate bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and rounded stone bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_POTTERY_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and pottery bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and sandstone bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and stone bricks that have been rendered.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_MARBLE_REINFORCED.id) {
                comm.sendNormalServerMessage("The cave wall has been reinforced with thick wooden beams, metal bands and marble bricks.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_LAVA.id) {
                comm.sendNormalServerMessage("Thick, slow flowing lava blocks your way.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
                comm.sendNormalServerMessage("A vein of crystalized sediment formed over time by evaporation.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_SANDSTONE.id) {
                comm.sendNormalServerMessage("A vein of clastic sedimentary rock composed mainly of sand-sized minerals or rock grains.");
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "plank", false));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "stone brick", true));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "slate brick", true));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "rounded stone brick", true));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "pottery brick", true));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "sandstone brick", true));
            } else if (type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id) {
                comm.sendNormalServerMessage(this.partlyCladExamine(tilex, tiley, "marble brick", true));
            } else {
                comm.sendNormalServerMessage("You see dark dungeons.");
            }
        } else if (action == 109) {
            performer.getCommunicator().sendNormalServerMessage("You cannot track there.");
        } else if (action == 141 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
            done = MethodsReligion.pray(act, performer, counter);
        } else if (action == 607) {
            long target = Tiles.getTileId(tilex, tiley, 0, false);
            performer.getCommunicator().sendAddCaveWallToCreationWindow(target, type, -10L);
        } else {
            done = super.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
        }
        return done;
    }

    private String partlyCladExamine(int tilex, int tiley, String brickName, boolean needsMortar) {
        int resource = Server.getCaveResource(tilex, tiley);
        int needs = resource >>> 8;
        String requires = "  It requires " + needs + " more " + brickName + (needs > 1 ? "s" : "") + (needsMortar ? " and the same number of mortar" : (needs == 10 ? " and one large nail" : "")) + ".";
        return "The cave wall has been reinforced with thick wooden beams, metal bands and partly clad with " + brickName + "." + requires;
    }

    public static final Blocker checkForBlockersBetween(TilePos pos, int tileSurfaceSide, int floorLevel) {
        VolaTile posTile = Zones.getTileOrNull(pos, false);
        if (posTile != null) {
            Fence[] fences;
            Wall[] walls;
            for (Wall wall : walls = posTile.getWalls()) {
                if (wall.getFloorLevel() != floorLevel || wall.isDoor() || !CaveWallBehaviour.isBlockerBlockingSurfaceSide(pos, wall, tileSurfaceSide)) continue;
                return wall;
            }
            for (Fence fence : fences = posTile.getFences()) {
                if (fence.getFloorLevel() != floorLevel || fence.isDoor() || !CaveWallBehaviour.isBlockerBlockingSurfaceSide(pos, fence, tileSurfaceSide)) continue;
                return fence;
            }
        }
        return null;
    }

    public static final boolean isBlockerBlockingSurfaceSide(TilePos pos, Blocker blocker, int surfaceSide) {
        switch (surfaceSide) {
            case 2: {
                return blocker.isOnWestBorder(pos);
            }
            case 5: {
                return blocker.isOnSouthBorder(pos);
            }
            case 3: {
                return blocker.isOnNorthBorder(pos);
            }
            case 4: {
                return blocker.isOnEastBorder(pos);
            }
        }
        return false;
    }

    public static final Blocker isCaveWallBlocked(TilePos checkedTile, int actionSurface, int floorLevel) {
        int newSurface;
        TilePos vtile;
        switch (actionSurface) {
            case 2: {
                vtile = checkedTile.East();
                break;
            }
            case 5: {
                vtile = checkedTile.North();
                break;
            }
            case 3: {
                vtile = checkedTile.South();
                break;
            }
            case 4: {
                vtile = checkedTile.West();
                break;
            }
            default: {
                return null;
            }
        }
        Blocker blocker = CaveWallBehaviour.checkForBlockersBetween(vtile, actionSurface, floorLevel);
        if (blocker != null) {
            return blocker;
        }
        switch (actionSurface) {
            case 2: {
                newSurface = 4;
                break;
            }
            case 5: {
                newSurface = 3;
                break;
            }
            case 3: {
                newSurface = 5;
                break;
            }
            case 4: {
                newSurface = 2;
                break;
            }
            default: {
                return null;
            }
        }
        Blocker blocker2 = CaveWallBehaviour.checkForBlockersBetween(checkedTile, newSurface, floorLevel);
        if (blocker2 != null) {
            return blocker2;
        }
        return null;
    }

    @Override
    public boolean action(Action act, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, int dir, short action, float counter) {
        boolean done = true;
        byte type = Tiles.decodeType(tile);
        if (source.isMiningtool() && !CaveWallBehaviour.isClad(type) && (action == 145 || action == 147 || action == 146)) {
            if (!GeneralUtilities.isValidTileLocation(tilex, tiley)) {
                performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
                return true;
            }
            if (Zones.isTileProtected(tilex, tiley)) {
                performer.getCommunicator().sendNormalServerMessage("This tile is protected by the gods. You can not mine here.", (byte)3);
                return true;
            }
            boolean ok = true;
            if (type != Tiles.Tile.TILE_CAVE_WALL.id && type != Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id && !Tiles.isOreCave(type)) {
                ok = false;
                if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                    if (performer.getPower() >= 2) {
                        logger.log(Level.WARNING, performer.getName() + " removed reinforced cave wall at " + tile + " with GM powers.");
                        ok = true;
                    } else {
                        VolaTile vt = Zones.getOrCreateTile(tilex, tiley, onSurface);
                        Village vill = vt.getVillage();
                        if (vill == null) {
                            ok = false;
                        } else {
                            if (!Methods.isActionAllowed(performer, (short)229, false, tilex, tiley, tile, 2) || !Methods.isActionAllowed(performer, (short)145, false, tilex, tiley, tile, 2)) {
                                return true;
                            }
                            ok = true;
                        }
                    }
                } else if (!Methods.isActionAllowed(performer, (short)145, false, tilex, tiley, tile, 2)) {
                    return true;
                }
                if (!ok) {
                    performer.getCommunicator().sendNormalServerMessage("The rock is too hard to mine.", (byte)3);
                    return true;
                }
            }
            if (performer.isOnSurface()) {
                performer.getCommunicator().sendNormalServerMessage("You are too far away to mine there.", (byte)3);
                return true;
            }
            int digTilex = performer.getTileX();
            int digTiley = performer.getTileY();
            if (dir == 1) {
                performer.getCommunicator().sendNormalServerMessage("The rock is too hard to mine.", (byte)3);
                logger.log(Level.WARNING, performer.getName() + " Tried to mine the roof of a cave wall.");
                return true;
            }
            Blocker blocker = CaveWallBehaviour.isCaveWallBlocked(TilePos.fromXY(tilex, tiley), dir, performer.getFloorLevel());
            if (blocker != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + blocker.getName() + " is in the way.", (byte)3);
                if (performer.getPower() > 0) {
                    performer.getCommunicator().sendNormalServerMessage(blocker.getName() + " is at " + blocker.getPositionX() + ", " + blocker.getPositionY() + " horiz=" + blocker.isHorizontal() + " wurmid=" + blocker.getId());
                }
                return done;
            }
            short h = Tiles.decodeHeight(tile);
            if (h >= -25 || h == -100) {
                done = false;
                Skills skills = performer.getSkills();
                Skill mining = null;
                Skill tool = null;
                boolean insta = performer.getPower() >= 2 && source.isWand();
                try {
                    mining = skills.getSkill(1008);
                }
                catch (Exception ex) {
                    mining = skills.learn(1008, 1.0f);
                }
                try {
                    tool = skills.getSkill(source.getPrimarySkill());
                }
                catch (Exception ex) {
                    try {
                        tool = skills.learn(source.getPrimarySkill(), 1.0f);
                    }
                    catch (NoSuchSkillException nse) {
                        logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + source.getName());
                    }
                }
                int time = 0;
                if (counter == 1.0f) {
                    if (!Methods.isActionAllowed(performer, action, false, tilex, tiley, tile, dir)) {
                        return true;
                    }
                    VolaTile dropTile = Zones.getTileOrNull(performer.getTilePos(), onSurface);
                    if (dropTile != null && dropTile.getNumberOfItems(performer.getFloorLevel()) > 99) {
                        performer.getCommunicator().sendNormalServerMessage("There is no space to mine here. Clear the area first.", (byte)3);
                        return true;
                    }
                    time = Actions.getVariableActionTime(performer, mining, source, 0.0, 250, 25, 8000);
                    try {
                        performer.getCurrentAction().setTimeLeft(time);
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.INFO, "This action does not exist?", nsa);
                    }
                    String dirstring = "";
                    if (action == 147) {
                        dirstring = " down";
                    } else if (action == 146) {
                        dirstring = " up";
                    }
                    Server.getInstance().broadCastAction(performer.getName() + " starts mining" + dirstring + ".", performer, 5);
                    performer.getCommunicator().sendNormalServerMessage("You start to mine" + dirstring + ".");
                    performer.sendActionControl(Actions.actionEntrys[145].getVerbString() + " " + dirstring, true, time);
                    source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                    performer.getStatus().modifyStamina(-1000.0f);
                } else {
                    try {
                        time = performer.getCurrentAction().getTimeLeft();
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.INFO, "This action does not exist?", nsa);
                    }
                    if (counter * 10.0f <= (float)time && !insta) {
                        if (act.currentSecond() % 5 == 0 || act.currentSecond() == 3 && time < 50) {
                            String sstring = "sound.work.mining1";
                            int x = Server.rand.nextInt(3);
                            if (x == 0) {
                                sstring = "sound.work.mining2";
                            } else if (x == 1) {
                                sstring = "sound.work.mining3";
                            }
                            SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0f);
                            source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
                            performer.getStatus().modifyStamina(-7000.0f);
                        }
                    } else {
                        boolean normal;
                        VolaTile dropTile;
                        if (act.getRarity() != 0) {
                            performer.playPersonalSound("sound.fx.drumroll");
                        }
                        if ((dropTile = Zones.getTileOrNull(performer.getTilePos(), onSurface)) != null && dropTile.getNumberOfItems(performer.getFloorLevel()) > 99) {
                            performer.getCommunicator().sendNormalServerMessage("There is no space to mine here. Clear the area first.", (byte)3);
                            return true;
                        }
                        double bonus = 0.0;
                        double power = 0.0;
                        done = true;
                        int resource = Server.getCaveResource(tilex, tiley);
                        if (resource == 65535) {
                            resource = Server.rand.nextInt(10000);
                        }
                        int itemTemplateCreated = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(tile));
                        if (resource > 50 && (itemTemplateCreated == 693 || itemTemplateCreated == 697)) {
                            resource = 50;
                        }
                        float diff = TileRockBehaviour.getDifficultyForTile(Tiles.decodeType(tile));
                        if (resource > 0) {
                            --resource;
                        }
                        Server.setCaveResource(tilex, tiley, resource);
                        boolean dryrun = false;
                        byte state = Zones.getMiningState(tilex, tiley);
                        boolean createItem = true;
                        int randint = Server.rand.nextInt(10);
                        boolean bl = normal = itemTemplateCreated == 146 || itemTemplateCreated == 1238;
                        if (normal && state > Servers.localServer.getTunnelingHits() && state <= Servers.localServer.getTunnelingHits() + 5 + randint && !TileRockBehaviour.isInsideTunnelOk(tilex, tiley, tile, action, dir, performer, false)) {
                            dryrun = true;
                            createItem = false;
                        }
                        if (normal && state > Servers.localServer.getTunnelingHits() + randint || !normal && resource <= 0 || insta) {
                            if (!TileRockBehaviour.createInsideTunnel(tilex, tiley, tile, performer, action, dir, false, act)) {
                                dryrun = true;
                                if (!normal || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                                    TileEvent.log(tilex, tiley, -1, performer.getWurmId(), action);
                                    if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                                        performer.getCommunicator().sendNormalServerMessage("You manage to remove the reinforcement.");
                                    } else {
                                        performer.getCommunicator().sendNormalServerMessage("The mine is now depleted.", (byte)3);
                                    }
                                    Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(tile)));
                                    TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                                }
                                createItem = false;
                            } else {
                                if (itemTemplateCreated == 1238 || itemTemplateCreated == 1116) {
                                    Zones.setMiningState(tilex, tiley, (byte)-1, false);
                                } else {
                                    Zones.setMiningState(tilex, tiley, (byte)0, false);
                                    Zones.deleteMiningTile(tilex, tiley);
                                }
                                performer.getCommunicator().sendNormalServerMessage("The wall breaks!");
                                Random rockRandom = new Random();
                                rockRandom.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * TileRockBehaviour.SOURCE_PRIME);
                                if (rockRandom.nextInt(TileRockBehaviour.sourceFactor) == 0) {
                                    TileRockBehaviour.SOURCE_PRIME += (long)Server.rand.nextInt(10000);
                                }
                            }
                        } else if (normal) {
                            if (state < 56 && (state = (byte)(state + 1)) >= Servers.localServer.getTunnelingHits()) {
                                performer.getCommunicator().sendNormalServerMessage("The wall will break soon.");
                            }
                            Zones.setMiningState(tilex, tiley, state, false);
                        } else if (resource < 5) {
                            performer.getCommunicator().sendNormalServerMessage("The wall will break soon.");
                        }
                        boolean useNewSystem = false;
                        float tickCounter = counter;
                        if (tool != null) {
                            bonus = tool.skillCheck(diff, source, 0.0, dryrun, tickCounter) / 5.0;
                        }
                        power = Math.max(1.0, mining.skillCheck(diff, source, bonus, dryrun, tickCounter));
                        if (performer.getTutorialLevel() == 10 && !performer.skippedTutorial()) {
                            performer.missionFinished(true, true);
                        }
                        if (createItem) {
                            try {
                                double imbueEnhancement = 1.0 + 0.23047 * (double)source.getSkillSpellImprovement(1008) / 100.0;
                                if (mining.getKnowledge(0.0) * imbueEnhancement < power) {
                                    power = mining.getKnowledge(0.0) * imbueEnhancement;
                                }
                                r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                                int m = TileRockBehaviour.MAX_QL;
                                if (normal || itemTemplateCreated == 38) {
                                    m = 100;
                                }
                                int max = (int)Math.min((double)m, (double)(20 + r.nextInt(80)) * imbueEnhancement);
                                if (state == -1) {
                                    max = 99;
                                }
                                power = Math.min(power, (double)max);
                                if (source.isCrude()) {
                                    power = 1.0;
                                }
                                float modifier = 1.0f;
                                if (source.getSpellEffects() != null) {
                                    modifier *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                                }
                                float orePower = GeneralUtilities.calcOreRareQuality(power * (double)modifier, act.getRarity(), source.getRarity());
                                Item newItem = ItemFactory.createItem(itemTemplateCreated, orePower, act.getRarity(), null);
                                if (itemTemplateCreated == 39) {
                                    performer.achievement(372);
                                }
                                newItem.setLastOwnerId(performer.getWurmId());
                                newItem.setDataXY(tilex, tiley);
                                newItem.putItemInfrontof(performer, 0.0f);
                                performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                                Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                                TileRockBehaviour.createGem(tilex, tiley, digTilex, digTiley, performer, power, false, act);
                                if (performer.getDeity() != null && performer.getDeity().number == 2) {
                                    performer.maybeModifyAlignment(0.5f);
                                }
                                if (itemTemplateCreated == 38) {
                                    performer.achievement(516);
                                    PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.MINE_IRON);
                                } else if (itemTemplateCreated == 43) {
                                    performer.achievement(527);
                                } else if (itemTemplateCreated == 207) {
                                    performer.achievement(528);
                                }
                                if (newItem.isMetal() && newItem.getCurrentQualityLevel() >= 80.0f) {
                                    performer.achievement(603);
                                }
                            }
                            catch (Exception ex) {
                                logger.log(Level.WARNING, "Factory failed to produce item", ex);
                            }
                        }
                    }
                }
            } else {
                performer.getCommunicator().sendNormalServerMessage("The water is too deep to mine.", (byte)3);
            }
        } else if (source.isMiningtool() && action == 156) {
            if (!GeneralUtilities.isValidTileLocation(tilex, tiley)) {
                performer.getCommunicator().sendNormalServerMessage("The water is too deep to prospect.", (byte)3);
                return true;
            }
            Blocker blocker = CaveWallBehaviour.isCaveWallBlocked(TilePos.fromXY(tilex, tiley), dir, performer.getFloorLevel());
            if (blocker != null) {
                performer.getCommunicator().sendNormalServerMessage("The " + blocker.getName() + " is in the way.", (byte)3);
                return done;
            }
            Skills skills = performer.getSkills();
            Skill prospecting = null;
            done = false;
            try {
                prospecting = skills.getSkill(10032);
            }
            catch (Exception ex) {
                prospecting = skills.learn(10032, 1.0f);
            }
            int time = 0;
            if (counter == 1.0f) {
                String sstring = "sound.work.prospecting1";
                int x = Server.rand.nextInt(3);
                if (x == 0) {
                    sstring = "sound.work.prospecting2";
                } else if (x == 1) {
                    sstring = "sound.work.prospecting3";
                }
                SoundPlayer.playSound(sstring, tilex, tiley, performer.isOnSurface(), 1.0f);
                time = (int)Math.max(30.0, 100.0 - prospecting.getKnowledge(source, 0.0));
                try {
                    performer.getCurrentAction().setTimeLeft(time);
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
                performer.getCommunicator().sendNormalServerMessage("You start to gather fragments of the rock.");
                Server.getInstance().broadCastAction(performer.getName() + " starts gathering fragments of the rock.", performer, 5);
                performer.sendActionControl(Actions.actionEntrys[156].getVerbString(), true, time);
            } else {
                try {
                    time = performer.getCurrentAction().getTimeLeft();
                }
                catch (NoSuchActionException nsa) {
                    logger.log(Level.INFO, "This action does not exist?", nsa);
                }
            }
            if (counter * 10.0f > (float)time) {
                performer.getStatus().modifyStamina(-3000.0f);
                prospecting.skillCheck(1.0, source, 0.0, false, counter);
                source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
                done = true;
                int resource = Server.getCaveResource(tilex, tiley);
                if (resource == 65535) {
                    resource = Server.rand.nextInt(10000);
                    Server.setCaveResource(tilex, tiley, resource);
                }
                int itemTemplate = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(tile));
                try {
                    ItemTemplate t = ItemTemplateFactory.getInstance().getTemplate(itemTemplate);
                    if (type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || CaveWallBehaviour.isClad(type)) {
                        performer.getCommunicator().sendNormalServerMessage("This wall is reinforced and may not be mined.");
                    } else if (type == Tiles.Tile.TILE_CAVE_WALL_ROCKSALT.id) {
                        performer.getCommunicator().sendNormalServerMessage("You would mine " + t.getName() + " here.");
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("You would mine " + MaterialUtilities.getMaterialString(t.getMaterial()) + " " + t.getName() + " here.");
                    }
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, performer.getName() + " - " + nst.getMessage() + ": " + itemTemplate + " at " + tilex + ", " + tiley);
                }
                if (type != Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id && !CaveWallBehaviour.isClad(type)) {
                    if (prospecting.getKnowledge(0.0) > 20.0) {
                        r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                        int m = TileRockBehaviour.MAX_QL;
                        if (itemTemplate == 146 || itemTemplate == 1238 || itemTemplate == 1116 || itemTemplate == 38) {
                            m = 100;
                        }
                        int max = Math.min(m, 20 + r.nextInt(80));
                        byte state = Zones.getMiningState(tilex, tiley);
                        if (state == -1) {
                            max = 99;
                        }
                        if (prospecting.getKnowledge(0.0) > 80.0) {
                            performer.getCommunicator().sendNormalServerMessage("It has a max quality of " + max + ".");
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("It is of " + CaveWallBehaviour.getShardQlDescription(max) + ".");
                        }
                    }
                    if (prospecting.getKnowledge(0.0) > 60.0 && itemTemplate != 146 && itemTemplate != 1238) {
                        if (prospecting.getKnowledge(0.0) > 90.0) {
                            performer.getCommunicator().sendNormalServerMessage("You will be able to mine here " + Server.getCaveResource(tilex, tiley) + " more times.");
                        } else {
                            String numString = "There is plenty of ore left.";
                            numString = resource > 10000 ? "There is plenty of ore left." : (resource > 5000 ? "Only a few weeks mining remain here." : (resource > 3000 ? "The ore is starting to deplete." : (resource > 1000 ? "You should start to prospect for another vein of this ore." : (resource > 100 ? "The ore will run out soon." : "The ore will run out any hour."))));
                            performer.getCommunicator().sendNormalServerMessage(numString);
                        }
                    }
                    if (prospecting.getKnowledge(0.0) > 40.0) {
                        boolean saltExists;
                        r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 102533L);
                        boolean bl = saltExists = r.nextInt(100) == 0;
                        if (saltExists) {
                            performer.getCommunicator().sendNormalServerMessage("You will find salt here!");
                        }
                    }
                    if (prospecting.getKnowledge(0.0) > 20.0) {
                        r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 6883L);
                        if (r.nextInt(200) == 0) {
                            performer.getCommunicator().sendNormalServerMessage("You will find flint here!");
                        }
                    }
                }
            }
        } else {
            if (source.getTemplateId() == 429 && action == 229) {
                if (Tiles.decodeType(tile) != Tiles.Tile.TILE_CAVE_WALL.id) {
                    if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                        performer.getCommunicator().sendNormalServerMessage("You can not reinforce ore veins.", (byte)3);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The wall does not need reinforcing.", (byte)3);
                    }
                    return true;
                }
                if (!Methods.isActionAllowed(performer, (short)229, false, tilex, tiley, tile, 2) || !Methods.isActionAllowed(performer, (short)145, false, tilex, tiley, tile, 2)) {
                    return true;
                }
                if (!GeneralUtilities.isValidTileLocation(tilex, tiley)) {
                    performer.getCommunicator().sendNormalServerMessage("The water is too deep here.", (byte)3);
                    return true;
                }
                done = false;
                Skills skills = performer.getSkills();
                Skill mining = null;
                boolean insta = performer.getPower() > 3;
                try {
                    mining = skills.getSkill(1008);
                }
                catch (Exception ex) {
                    mining = skills.learn(1008, 1.0f);
                }
                int time = 0;
                if (counter == 1.0f) {
                    SoundPlayer.playSound("sound.work.masonry", tilex, tiley, performer.isOnSurface(), 1.0f);
                    time = Math.min(250, Actions.getStandardActionTime(performer, mining, source, 0.0));
                    try {
                        performer.getCurrentAction().setTimeLeft(time);
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.INFO, "This action does not exist?", nsa);
                    }
                    performer.getCommunicator().sendNormalServerMessage("You start to reinforce the rock wall.");
                    Server.getInstance().broadCastAction(performer.getName() + " starts to reinforce the rock wall.", performer, 5);
                    performer.sendActionControl(Actions.actionEntrys[229].getVerbString(), true, time);
                } else {
                    try {
                        time = performer.getCurrentAction().getTimeLeft();
                    }
                    catch (NoSuchActionException nsa) {
                        logger.log(Level.INFO, "This action does not exist?", nsa);
                    }
                }
                if (counter * 10.0f > (float)time || insta) {
                    mining.skillCheck(20.0, source, 0.0, false, counter);
                    done = true;
                    performer.getCommunicator().sendNormalServerMessage("You reinforce the wall.");
                    Server.getInstance().broadCastAction(performer.getName() + " reinforces the rock wall.", performer, 5);
                    Items.destroyItem(source.getWurmId());
                    Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id, Tiles.decodeData(tile)));
                    Zones.setMiningState(tilex, tiley, (byte)-1, false);
                    Players.getInstance().sendChangedTile(tilex, tiley, false, true);
                }
                return done;
            }
            if (action == 78 && source.getTemplateId() == 668) {
                done = true;
                if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_WALL.id) {
                    OreQuestion ctq = new OreQuestion(performer, tilex, tiley, source);
                    ctq.sendQuestion();
                } else {
                    performer.getCommunicator().sendNormalServerMessage("This rod only works on normal cave walls.");
                }
            } else if (action == 118 && (source.getTemplateId() == 526 || source.getTemplateId() == 667)) {
                done = true;
                if (source.getTemplateId() == 526) {
                    performer.getCommunicator().sendNormalServerMessage("You draw a circle in the air in front of you with " + source.getNameWithGenus() + ".");
                    Server.getInstance().broadCastAction(performer.getName() + " draws a circle in the air in front of " + performer.getHimHerItString() + " with " + source.getNameWithGenus() + ".", performer, 5);
                    if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                        if (source.getAuxData() > 0) {
                            int resource;
                            int digTilex = (int)performer.getStatus().getPositionX() + 2 >> 2;
                            int digTiley = (int)performer.getStatus().getPositionY() + 2 >> 2;
                            if (digTilex > tilex + 1 || digTilex < tilex || digTiley > tiley + 1 || digTiley < tiley) {
                                performer.getCommunicator().sendNormalServerMessage("You are too far away to use the wand.");
                                return true;
                            }
                            int itemTemplateCreated = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(tile));
                            r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                            int m = TileRockBehaviour.MAX_QL;
                            if (itemTemplateCreated == 146 || itemTemplateCreated == 38) {
                                m = 100;
                            }
                            int max = Math.min(m, 20 + r.nextInt(80));
                            byte state = Zones.getMiningState(tilex, tiley);
                            if (state == -1) {
                                max = 99;
                            }
                            float power = max;
                            if (source.isCrude()) {
                                power = 1.0f;
                            }
                            if ((resource = Server.getCaveResource(tilex, tiley)) == 65535) {
                                resource = Server.rand.nextInt(10000);
                            }
                            if (resource > 10) {
                                Server.setCaveResource(tilex, tiley, Math.max(1, resource - 10));
                                for (int x = 0; x < 10; ++x) {
                                    try {
                                        Item newItem = ItemFactory.createItem(itemTemplateCreated, power, performer.getStatus().getPositionX(), performer.getStatus().getPositionY(), Server.rand.nextFloat() * 360.0f, false, act.getRarity(), -10L, null);
                                        newItem.setLastOwnerId(performer.getWurmId());
                                        newItem.setDataXY(tilex, tiley);
                                        continue;
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, "Factory failed to produce item", ex);
                                    }
                                }
                                TileRockBehaviour.createGem(digTilex, digTiley, performer, power, false, act);
                                source.setAuxData((byte)(source.getAuxData() - 1));
                            } else {
                                performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " fails you.");
                            }
                        } else {
                            performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                        }
                    }
                } else if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                    int resource = Server.getCaveResource(tilex, tiley);
                    if (resource == 65535) {
                        resource = Server.rand.nextInt(10000);
                    }
                    if (resource > 10) {
                        Server.setCaveResource(tilex, tiley, Math.max(1, resource - 10));
                    }
                    byte state = Zones.getMiningState(tilex, tiley);
                    int itemTemplateCreated = TileRockBehaviour.getItemTemplateForTile(Tiles.decodeType(tile));
                    r.setSeed((long)(tilex + tiley * Zones.worldTileSizeY) * 789221L);
                    int m = TileRockBehaviour.MAX_QL;
                    if (itemTemplateCreated == 146 || itemTemplateCreated == 38) {
                        m = 100;
                    }
                    int max = Math.min(m, 20 + r.nextInt(80));
                    if (state == -1) {
                        max = 99;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You ding the " + source.getName() + " on the wall. From the sounds you realize that the ore here has a max quality of " + max + ". You will be able to mine here " + Server.getCaveResource(tilex, tiley) + " more times.");
                    Items.destroyItem(source.getWurmId());
                } else {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens as you ding the " + source.getName() + " on the wall.");
                }
            } else if (action == 856) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 132, Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id);
                }
            } else if (action == 857) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 1123, Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_SLATE_REINFORCED.id);
                }
            } else if (action == 858) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 776, Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_POTTERY_REINFORCED.id);
                }
            } else if (action == 859) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 1122, Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED.id);
                }
            } else if (action == 860) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 1121, Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_SANDSTONE_REINFORCED.id);
                }
            } else if (action == 861) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id) && (source.getTemplateId() == 493 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 786, Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_MARBLE_REINFORCED.id);
                }
            } else if (action == 862) {
                if ((type == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id || type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id) && (source.getTemplateId() == 63 || source.getTemplateId() == 62 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.clad(act, performer, source, tilex, tiley, tile, action, counter, 22, Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id);
                }
            } else if (action == 78 && CaveWallBehaviour.isClad(type) && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
                done = CaveWallBehaviour.removeCladding(act, performer, source, tilex, tiley, tile, action, counter);
            } else if (action == 78 && type == Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id && (source.getTemplateId() == 219 || source.isWand() && performer.getPower() >= 4)) {
                done = CaveWallBehaviour.removeCladding(act, performer, source, tilex, tiley, tile, action, counter);
            } else if (action == 847) {
                if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id && (source.getTemplateId() == 130 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.toggleRenderWall(act, performer, source, tilex, tiley, tile, action, counter);
                } else if (type == Tiles.Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.id && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
                    done = CaveWallBehaviour.toggleRenderWall(act, performer, source, tilex, tiley, tile, action, counter);
                }
            } else if (action == 141 && performer.getDeity() != null && performer.getDeity().isMountainGod()) {
                done = MethodsReligion.pray(act, performer, counter);
            } else if (action == 109) {
                performer.getCommunicator().sendNormalServerMessage("You cannot track there.");
            } else if (action == 607) {
                long target = Tiles.getTileId(tilex, tiley, 0, false);
                performer.getCommunicator().sendAddCaveWallToCreationWindow(target, type, -10L);
            } else {
                done = super.action(act, performer, source, tilex, tiley, onSurface, heightOffset, tile, action, counter);
            }
        }
        return done;
    }

    static boolean toggleRenderWall(Action act, Creature performer, Item source, int tilex, int tiley, int tile, short action, float counter) {
        String walltype;
        String renders;
        String render;
        boolean insta = source.isWand() && performer.getPower() >= 4;
        byte type = Tiles.decodeType(tile);
        if (!Methods.isActionAllowed(performer, (short)229, tilex, tiley) || !Methods.isActionAllowed(performer, (short)116, tilex, tiley)) {
            return true;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id && !insta && source.getWeightGrams() < 10000) {
            performer.getCommunicator().sendNormalServerMessage("It takes 10kg of " + source.getName() + " to render the wall.");
            return true;
        }
        int time = 40;
        if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id) {
            render = "render";
            renders = "renders";
            walltype = "stone reinforced wall";
        } else {
            render = "remove the render from";
            renders = "removes the render from";
            walltype = "rendered reinforced wall";
        }
        if (counter == 1.0f) {
            act.setTimeLeft(time);
            performer.sendActionControl("Rendering wall", true, time);
            performer.getCommunicator().sendNormalServerMessage("You start to " + render + " the " + walltype + ".");
            Server.getInstance().broadCastAction(StringUtil.format("%s starts to " + render + " the " + walltype + ".", performer.getName()), performer, 5);
            return false;
        }
        time = act.getTimeLeft();
        if (counter * 10.0f > (float)time || insta) {
            performer.getCommunicator().sendNormalServerMessage("You " + render + " the " + walltype + ".");
            Server.getInstance().broadCastAction(StringUtil.format("%s " + renders + " the %s.", performer.getName(), walltype), performer, 5);
            if (type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id && !insta) {
                source.setWeight(source.getWeightGrams() - 10000, true);
            }
            byte newWallType = type == Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id ? Tiles.Tile.TILE_CAVE_WALL_RENDERED_REINFORCED.id : Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id;
            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newWallType, Tiles.decodeData(tile)));
            TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
            return true;
        }
        return false;
    }

    static boolean removeCladding(Action act, Creature performer, Item source, int tilex, int tiley, int tile, short action, float counter) {
        boolean insta = source.isWand() && performer.getPower() >= 4;
        byte type = Tiles.decodeType(tile);
        if (!Methods.isActionAllowed(performer, (short)229, tilex, tiley) || !Methods.isActionAllowed(performer, (short)145, tilex, tiley)) {
            return true;
        }
        int time = 400;
        if (counter == 1.0f) {
            act.setTimeLeft(time);
            performer.sendActionControl("Removing cladding", true, time);
            performer.getCommunicator().sendNormalServerMessage("You start to remove the cladding from the " + Tiles.getTile(type).getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to remove the cladding from the " + Tiles.getTile(type).getName() + ".", performer, 5);
            return false;
        }
        time = act.getTimeLeft();
        if (!insta && act.currentSecond() % 5 == 0) {
            SoundPlayer.playSound(Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2", tilex, tiley, performer.isOnSurface(), 1.6f);
            performer.getStatus().modifyStamina(-10000.0f);
            source.setDamage(source.getDamage() + 0.001f * source.getDamageModifier());
        }
        if (counter * 10.0f > (float)time || insta) {
            performer.getCommunicator().sendNormalServerMessage("You remove the cladding from the " + Tiles.getTile(type).getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " removes the chadding from the " + Tiles.getTile(type).getName() + ".", performer, 5);
            byte newWallType = Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id;
            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), newWallType, Tiles.decodeData(tile)));
            TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
            return true;
        }
        return false;
    }

    static boolean clad(Action act, Creature performer, Item source, int tilex, int tiley, int tile, short action, float counter, int brick, byte partwalltype, byte finishedWallType) {
        boolean insta = source.isWand() && performer.getPower() >= 4;
        byte tiletype = Tiles.decodeType(tile);
        if (!Methods.isActionAllowed(performer, (short)229, tilex, tiley) || !Methods.isActionAllowed(performer, (short)116, tilex, tiley)) {
            return true;
        }
        int skillTemplate = brick == 22 ? 1005 : 1013;
        Skill skill = performer.getSkills().getSkillOrLearn(skillTemplate);
        Skill toolskill = null;
        try {
            toolskill = performer.getSkills().getSkillOrLearn(source.getPrimarySkill());
        }
        catch (NoSuchSkillException noSuchSkillException) {
            // empty catch block
        }
        if (skillTemplate == 1013 && skill.getKnowledge(0.0) < 30.0) {
            performer.getCommunicator().sendNormalServerMessage("You need at least 30 masonry to clad reinforced walls with stone.");
            performer.getCommunicator().sendActionResult(false);
            return true;
        }
        int resource = Server.getCaveResource(tilex, tiley);
        int needs = resource >>> 8;
        if (tiletype == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
            needs = 10;
            Server.setCaveResource(tilex, tiley, (needs << 8) + (resource & 0xF));
            Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), partwalltype, Tiles.decodeData(tile)));
            TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
            long target = Tiles.getTileId(tilex, tiley, 0, false);
            performer.getCommunicator().sendAddCaveWallToCreationWindow(target, partwalltype, target);
        }
        int[] templatesNeeded = CaveWallBehaviour.getTemplatesNeeded(tilex, tiley, partwalltype);
        if (!insta && !CaveWallBehaviour.hasTemplateItems(performer, templatesNeeded)) {
            return true;
        }
        int time = 40;
        if (counter == 1.0f) {
            act.setTimeLeft(time);
            performer.sendActionControl("Cladding reinforced wall", true, time);
            performer.getCommunicator().sendNormalServerMessage("You start to clad the reinforced wall.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to clad the reinforced wall.", performer, 5);
        } else {
            time = act.getTimeLeft();
        }
        if (act.currentSecond() % 5 == 0) {
            if (source.getTemplateId() == 493) {
                SoundPlayer.playSound("sound.work.masonry", tilex, tiley, performer.isOnSurface(), 1.6f);
            } else {
                SoundPlayer.playSound(Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2", tilex, tiley, performer.isOnSurface(), 1.6f);
            }
            performer.getStatus().modifyStamina(-10000.0f);
            if (source.getTemplateId() == 63) {
                source.setDamage(source.getDamage() + 0.0015f * source.getDamageModifier());
            } else if (source.getTemplateId() == 62) {
                source.setDamage(source.getDamage() + 3.0E-4f * source.getDamageModifier());
            } else if (source.getTemplateId() == 493) {
                source.setDamage(source.getDamage() + 5.0E-4f * source.getDamageModifier());
            }
        }
        if (counter * 10.0f > (float)time || insta) {
            if (!insta && !CaveWallBehaviour.depleteTemplateItems(performer, templatesNeeded, act)) {
                return true;
            }
            if (insta) {
                performer.getCommunicator().sendNormalServerMessage("You use the wand and summon the required materials and add those to the wall.");
                act.setPower(50.0f);
                needs = needs > 1 ? 1 : 0;
            } else {
                --needs;
            }
            Server.setCaveResource(tilex, tiley, (needs << 8) + (resource & 0xF));
            if (!insta) {
                double bonus = 0.0;
                if (toolskill != null) {
                    toolskill.skillCheck(10.0, source, 0.0, false, counter);
                    bonus = toolskill.getKnowledge(source, 0.0) / 10.0;
                }
                skill.skillCheck(10.0, source, bonus, false, counter);
            }
            long target = Tiles.getTileId(tilex, tiley, 0, false);
            if (needs <= 0) {
                Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), finishedWallType, Tiles.decodeData(tile)));
                TileRockBehaviour.sendCaveTile(tilex, tiley, 0, 0);
                performer.getCommunicator().sendRemoveFromCreationWindow(target);
            } else {
                performer.getCommunicator().sendAddCaveWallToCreationWindow(target, partwalltype, target);
            }
            return true;
        }
        return false;
    }

    static final boolean hasTemplateItems(Creature performer, int[] neededTemplates) {
        int f;
        int neededTemplateWeightGrams;
        int n;
        int x;
        boolean found = true;
        Item[] inventoryItems = performer.getInventory().getAllItems(false);
        Item[] bodyItems = performer.getBody().getAllItems();
        int[] foundTemplates = CaveWallBehaviour.getNotInitializedIntArray(neededTemplates.length);
        for (x = 0; x < inventoryItems.length; ++x) {
            for (n = 0; n < neededTemplates.length; ++n) {
                if (inventoryItems[x].getTemplateId() != neededTemplates[n] || (neededTemplateWeightGrams = CaveWallBehaviour.getItemTemplateWeightInGrams(inventoryItems[x].getTemplateId())) > inventoryItems[x].getWeightGrams()) continue;
                foundTemplates[n] = neededTemplates[n];
            }
        }
        for (f = 0; f < foundTemplates.length; ++f) {
            if (foundTemplates[f] != -1) continue;
            found = false;
        }
        if (!found) {
            for (x = 0; x < bodyItems.length; ++x) {
                for (n = 0; n < neededTemplates.length; ++n) {
                    if (bodyItems[x].getTemplateId() != neededTemplates[n] || (neededTemplateWeightGrams = CaveWallBehaviour.getItemTemplateWeightInGrams(bodyItems[x].getTemplateId())) > bodyItems[x].getWeightGrams()) continue;
                    foundTemplates[n] = neededTemplates[n];
                }
            }
            found = true;
            for (f = 0; f < foundTemplates.length; ++f) {
                if (foundTemplates[f] != -1) continue;
                found = false;
            }
        }
        if (!found) {
            for (int n2 = 0; n2 < foundTemplates.length; ++n2) {
                if (foundTemplates[n2] != -1) continue;
                try {
                    if (neededTemplates[n2] == 217) {
                        performer.getCommunicator().sendNormalServerMessage("You need large iron nails.");
                        continue;
                    }
                    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededTemplates[n2]);
                    if (template.isCombine()) {
                        performer.getCommunicator().sendNormalServerMessage("You need " + template.getName() + ".");
                        continue;
                    }
                    performer.getCommunicator().sendNormalServerMessage("You need " + template.getNameWithGenus() + ".");
                    continue;
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
            return false;
        }
        return true;
    }

    static final boolean depleteTemplateItems(Creature performer, int[] neededTemplates, Action act) {
        int i;
        Item[] inventoryItems = performer.getInventory().getAllItems(false);
        Item[] bodyItems = performer.getBody().getAllItems();
        float qlevel = 0.0f;
        int[] foundTemplates = CaveWallBehaviour.getNotInitializedIntArray(neededTemplates.length);
        Item[] depleteItems = new Item[neededTemplates.length];
        block4: for (int i2 = 0; i2 < neededTemplates.length; ++i2) {
            if (foundTemplates[i2] != -1) continue;
            for (int j = 0; j < inventoryItems.length; ++j) {
                int neededTemplateWeightGrams;
                if (inventoryItems[j].getTemplateId() != neededTemplates[i2] || (neededTemplateWeightGrams = CaveWallBehaviour.getItemTemplateWeightInGrams(inventoryItems[j].getTemplateId())) > inventoryItems[j].getWeightGrams()) continue;
                depleteItems[i2] = inventoryItems[j];
                foundTemplates[i2] = neededTemplates[i2];
                continue block4;
            }
        }
        boolean allInitialized = true;
        for (i = 0; i < foundTemplates.length; ++i) {
            if (foundTemplates[i] != -1) continue;
            allInitialized = false;
            break;
        }
        if (!allInitialized) {
            block7: for (i = 0; i < neededTemplates.length; ++i) {
                if (foundTemplates[i] != -1) continue;
                for (int j = 0; j < bodyItems.length; ++j) {
                    int neededTemplateWeightGrams;
                    if (bodyItems[j].getTemplateId() != neededTemplates[i] || (neededTemplateWeightGrams = CaveWallBehaviour.getItemTemplateWeightInGrams(bodyItems[j].getTemplateId())) > bodyItems[j].getWeightGrams()) continue;
                    depleteItems[i] = bodyItems[j];
                    foundTemplates[i] = neededTemplates[i];
                    continue block7;
                }
            }
            allInitialized = true;
            for (i = 0; i < foundTemplates.length; ++i) {
                if (foundTemplates[i] != -1) continue;
                allInitialized = false;
                break;
            }
        }
        if (!allInitialized) {
            for (i = 0; i < foundTemplates.length; ++i) {
                if (foundTemplates[i] != -1) continue;
                try {
                    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededTemplates[i]);
                    performer.getCommunicator().sendNormalServerMessage("You did not have enough " + template.getPlural() + ".");
                    continue;
                }
                catch (NoSuchTemplateException nst) {
                    logger.log(Level.WARNING, nst.getMessage(), nst);
                }
            }
            return false;
        }
        StringBuilder buf = new StringBuilder();
        for (int i3 = 0; i3 < depleteItems.length; ++i3) {
            try {
                ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(neededTemplates[i3]);
                if (i3 > 0) {
                    buf.append(" and ");
                }
                buf.append(template.getNameWithGenus());
            }
            catch (NoSuchTemplateException nst) {
                logger.log(Level.WARNING, nst.getMessage(), nst);
            }
            if (depleteItems[i3].isCombine()) {
                depleteItems[i3].setWeight(depleteItems[i3].getWeightGrams() - depleteItems[i3].getTemplate().getWeightGrams(), true);
            } else {
                Items.destroyItem(depleteItems[i3].getWurmId());
            }
            qlevel += depleteItems[i3].getCurrentQualityLevel() / 21.0f;
        }
        performer.getCommunicator().sendNormalServerMessage("You use " + buf.toString() + ".");
        act.setPower(qlevel);
        return true;
    }

    static final int[] getNotInitializedIntArray(int len) {
        int[] notInitializedArray = new int[len];
        for (int x = 0; x < notInitializedArray.length; ++x) {
            notInitializedArray[x] = -1;
        }
        return notInitializedArray;
    }

    static final int getItemTemplateWeightInGrams(int itemTemplateId) {
        int neededTemplateWeightGrams;
        try {
            neededTemplateWeightGrams = ItemTemplateFactory.getInstance().getTemplate(itemTemplateId).getWeightGrams();
        }
        catch (NoSuchTemplateException nst) {
            switch (itemTemplateId) {
                case 22: {
                    neededTemplateWeightGrams = 2000;
                    break;
                }
                case 217: {
                    neededTemplateWeightGrams = 300;
                    break;
                }
                case 492: {
                    neededTemplateWeightGrams = 2000;
                    break;
                }
                case 132: {
                    neededTemplateWeightGrams = 150000;
                    break;
                }
                default: {
                    neededTemplateWeightGrams = 150000;
                }
            }
        }
        return neededTemplateWeightGrams;
    }

    public static boolean isCorrectTool(byte type, Creature performer, @Nullable Item tool) {
        if (tool == null) {
            return false;
        }
        List<Integer> tools = CaveWallBehaviour.getToolsForType(type, performer);
        for (Integer t : tools) {
            if (t.intValue() != tool.getTemplateId()) continue;
            return true;
        }
        return false;
    }

    public static List<Integer> getToolsForType(byte type, @Nullable Creature performer) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id) {
            list.add(62);
            list.add(63);
            if (performer != null) {
                if (performer.getPower() >= 2 && Servers.isThisATestServer()) {
                    list.add(315);
                }
                if (WurmPermissions.mayUseGMWand(performer)) {
                    list.add(176);
                }
            }
        } else {
            list.add(493);
            if (performer != null && WurmPermissions.mayUseGMWand(performer)) {
                list.add(176);
            }
        }
        return list;
    }

    public static short actionFromWallType(byte type) {
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id) {
            return 856;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id) {
            return 857;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id) {
            return 858;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id) {
            return 859;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id) {
            return 860;
        }
        if (type == Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id) {
            return 861;
        }
        return 862;
    }

    public static int[] getTemplatesNeeded(int tilex, int tiley, byte type) {
        int resource = Server.getCaveResource(tilex, tiley);
        int needs = resource >>> 8;
        int brick = CaveWallBehaviour.getBrickTypeNeeded(type);
        int[] templatesNeeded = brick == 22 ? (needs == 10 ? new int[]{22, 217} : new int[]{22}) : new int[]{brick, 492};
        return templatesNeeded;
    }

    public static int[] getMaterialsNeeded(int tilex, int tiley, byte type) {
        int resource = Server.getCaveResource(tilex, tiley);
        int needs = resource >>> 8;
        int brick = CaveWallBehaviour.getBrickTypeNeeded(type);
        int[] materialsNeeded = brick == 22 ? (needs == 10 ? new int[]{22, 10, 217, 1} : new int[]{22, needs}) : new int[]{brick, needs, 492, needs};
        return materialsNeeded;
    }

    public static int getBrickTypeNeeded(byte type) {
        switch (type & 0xFF) {
            case 239: {
                return 132;
            }
            case 240: {
                return 1123;
            }
            case 242: {
                return 1122;
            }
            case 241: {
                return 776;
            }
            case 243: {
                return 1121;
            }
            case 244: {
                return 786;
            }
            case 245: {
                return 22;
            }
        }
        return 132;
    }

    public static byte[] getMaterialsFromToolType(Creature performer, Item tool) {
        switch (tool.getTemplateId()) {
            case 62: 
            case 63: {
                return new byte[]{Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id};
            }
            case 493: {
                return new byte[]{Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id};
            }
            case 176: {
                return new byte[]{Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id};
            }
            case 315: {
                if (performer.getPower() >= 2 && Servers.isThisATestServer()) {
                    return new byte[]{Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id, Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id};
                }
                return new byte[0];
            }
        }
        return new byte[0];
    }

    public static final boolean canCladWall(byte type, Creature performer) {
        int skillNumber = type == Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id ? 1005 : 1013;
        Skill skill = performer.getSkills().getSkillOrLearn(skillNumber);
        if (skill == null) {
            return false;
        }
        return skillNumber != 1013 || !(skill.getKnowledge(0.0) < 30.0);
    }

    public static final int getSkillNumberNeededForCladding(short action) {
        if (action == 862) {
            return 1005;
        }
        return 1013;
    }

    public static final int[] getCorrectToolsForCladding(short action) {
        if (action == 862) {
            return new int[]{62, 63};
        }
        return new int[]{493};
    }

    public static byte getPartReinforcedWallFromAction(short action) {
        switch (action) {
            case 856: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_STONE_REINFORCED.id;
            }
            case 857: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_SLATE_REINFORCED.id;
            }
            case 858: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_POTTERY_REINFORCED.id;
            }
            case 859: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_ROUNDED_STONE_REINFORCED.id;
            }
            case 860: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_SANDSTONE_REINFORCED.id;
            }
            case 861: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_MARBLE_REINFORCED.id;
            }
            case 862: {
                return Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id;
            }
        }
        logger.log(Level.WARNING, "Part reinforced wall for action " + action + " is not found!", new Exception());
        return Tiles.Tile.TILE_CAVE_WALL_PART_WOOD_REINFORCED.id;
    }

    public static byte getReinforcedWallFromAction(short action) {
        switch (action) {
            case 856: {
                return Tiles.Tile.TILE_CAVE_WALL_STONE_REINFORCED.id;
            }
            case 857: {
                return Tiles.Tile.TILE_CAVE_WALL_SLATE_REINFORCED.id;
            }
            case 858: {
                return Tiles.Tile.TILE_CAVE_WALL_POTTERY_REINFORCED.id;
            }
            case 859: {
                return Tiles.Tile.TILE_CAVE_WALL_ROUNDED_STONE_REINFORCED.id;
            }
            case 860: {
                return Tiles.Tile.TILE_CAVE_WALL_SANDSTONE_REINFORCED.id;
            }
            case 861: {
                return Tiles.Tile.TILE_CAVE_WALL_MARBLE_REINFORCED.id;
            }
            case 862: {
                return Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id;
            }
        }
        logger.log(Level.WARNING, "Reinforced wall for action " + action + " is not found!", new Exception());
        return Tiles.Tile.TILE_CAVE_WALL_WOOD_REINFORCED.id;
    }

    public static int[] getMaterialsNeededTotal(short action) {
        switch (action) {
            case 856: {
                return new int[]{132, 10, 492, 10};
            }
            case 857: {
                return new int[]{1123, 10, 492, 10};
            }
            case 858: {
                return new int[]{776, 10, 492, 10};
            }
            case 859: {
                return new int[]{1122, 10, 492, 10};
            }
            case 860: {
                return new int[]{1121, 10, 492, 10};
            }
            case 861: {
                return new int[]{786, 10, 492, 10};
            }
            case 862: {
                return new int[]{22, 10, 217, 1};
            }
        }
        logger.log(Level.WARNING, "Materials for reinforced wall for action " + action + " is not found!", new Exception());
        return new int[0];
    }
}

