/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.CaveTile;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.PlanetBehaviour;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.Missions;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BehaviourDispatcher
implements CounterTypes,
ItemTypes,
MiscConstants {
    private static final Logger logger = Logger.getLogger(BehaviourDispatcher.class.getName());
    private static List<ActionEntry> availableActions = null;
    private static final List<ActionEntry> emptyActions = new LinkedList<ActionEntry>();

    private BehaviourDispatcher() {
    }

    public static void requestSelectionActions(Creature creature, Communicator comm, byte requestId, long subject, long target) throws NoSuchBehaviourException, NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchWallException {
        if (!creature.isTeleporting()) {
            Item item = null;
            availableActions = null;
            if (WurmId.getType(subject) == 8 || WurmId.getType(subject) == 18 || WurmId.getType(subject) == 32) {
                subject = -1L;
            }
            int targetType = WurmId.getType(target);
            Behaviour behaviour = Action.getBehaviour(target, creature.isOnSurface());
            boolean onSurface = Action.getIsOnSurface(target, creature.isOnSurface());
            if (subject != -1L && (WurmId.getType(subject) == 2 || WurmId.getType(subject) == 6 || WurmId.getType(subject) == 19 || WurmId.getType(subject) == 20)) {
                try {
                    item = Items.getItem(subject);
                }
                catch (NoSuchItemException nsi) {
                    subject = -10L;
                    item = null;
                }
            }
            if (targetType == 3) {
                RequestParam param = BehaviourDispatcher.requestActionForTiles(creature, target, onSurface, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 1 || targetType == 0) {
                RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(creature, target, item, targetType, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 2 || targetType == 6 || targetType == 19 || targetType == 20) {
                RequestParam param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(creature, target, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 5) {
                RequestParam param = BehaviourDispatcher.requestActionForWalls(creature, target, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 7) {
                RequestParam param = BehaviourDispatcher.requestActionForFences(creature, target, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 12) {
                RequestParam param = BehaviourDispatcher.requestActionForTileBorder(creature, target, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 17) {
                RequestParam param = BehaviourDispatcher.requestActionForCaveTiles(creature, target, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 23) {
                RequestParam param = BehaviourDispatcher.requestActionForFloors(creature, target, onSurface, item, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else if (targetType == 24) {
                RequestParam param = BehaviourDispatcher.requestActionForIllusions(creature, target, item, targetType, behaviour);
                param.filterForSelectBar();
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            } else {
                RequestParam param = new RequestParam(new LinkedList<ActionEntry>(), "");
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, true);
            }
        } else {
            comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
        }
    }

    public static void requestActions(Creature creature, Communicator comm, byte requestId, long subject, long target) throws NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException, NoSuchWallException {
        if (!creature.isTeleporting()) {
            Item item = null;
            availableActions = null;
            if (WurmId.getType(subject) == 8 || WurmId.getType(subject) == 18 || WurmId.getType(subject) == 32) {
                subject = -1L;
            }
            int targetType = WurmId.getType(target);
            Behaviour behaviour = Action.getBehaviour(target, creature.isOnSurface());
            boolean onSurface = Action.getIsOnSurface(target, creature.isOnSurface());
            if (subject != -1L && (WurmId.getType(subject) == 2 || WurmId.getType(subject) == 6 || WurmId.getType(subject) == 19 || WurmId.getType(subject) == 20)) {
                try {
                    item = Items.getItem(subject);
                }
                catch (NoSuchItemException nsi) {
                    subject = -10L;
                    item = null;
                }
            }
            if (targetType == 3) {
                RequestParam param = BehaviourDispatcher.requestActionForTiles(creature, target, onSurface, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 1 || targetType == 0) {
                RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(creature, target, item, targetType, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 2 || targetType == 6 || targetType == 19 || targetType == 20) {
                RequestParam param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(creature, target, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 5) {
                RequestParam param = BehaviourDispatcher.requestActionForWalls(creature, target, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 7) {
                RequestParam param = BehaviourDispatcher.requestActionForFences(creature, target, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 8 || targetType == 32) {
                BehaviourDispatcher.requestActionForWounds(creature, comm, requestId, target, item, behaviour);
            } else if (targetType == 12) {
                RequestParam param = BehaviourDispatcher.requestActionForTileBorder(creature, target, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 14) {
                BehaviourDispatcher.requestActionForPlanets(creature, comm, requestId, target, item, behaviour);
            } else if (targetType == 30) {
                BehaviourDispatcher.requestActionForMenu(creature, comm, requestId, target, behaviour);
            } else if (targetType == 17) {
                RequestParam param = BehaviourDispatcher.requestActionForCaveTiles(creature, target, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 18) {
                BehaviourDispatcher.requestActionForSkillIds(comm, requestId, target);
            } else if (targetType == 23) {
                RequestParam param = BehaviourDispatcher.requestActionForFloors(creature, target, onSurface, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 22) {
                BehaviourDispatcher.requestActionForMissionPerformed(creature, comm, requestId, target, behaviour);
            } else if (targetType == 24) {
                RequestParam param = BehaviourDispatcher.requestActionForIllusions(creature, target, item, targetType, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 27) {
                RequestParam param = BehaviourDispatcher.requestActionForTileCorner(creature, target, onSurface, item, behaviour);
                BehaviourDispatcher.sendRequestResponse(requestId, comm, param, false);
            } else if (targetType == 28) {
                BehaviourDispatcher.requestActionForBridgeParts(creature, comm, requestId, target, onSurface, item, behaviour);
            } else if (targetType == 25) {
                BehaviourDispatcher.requestActionForTickets(creature, comm, requestId, target, behaviour);
            }
        } else {
            comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
        }
    }

    private static void sendRequestResponse(byte requestId, Communicator comm, RequestParam response, boolean sendToSelectBar) {
        if (!sendToSelectBar) {
            comm.sendAvailableActions(requestId, response.getAvailableActions(), response.getHelpString());
        } else {
            comm.sendAvailableSelectBarActions(requestId, response.getAvailableActions());
        }
    }

    public static final RequestParam requestActionForTiles(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        int tile = Server.surfaceMesh.getTile(x, y);
        availableActions = item == null ? behaviour.getBehavioursFor(creature, x, y, onSurface, tile) : behaviour.getBehavioursFor(creature, item, (int)x, y, onSurface, tile);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile t = Tiles.getTile(type);
        return new RequestParam(availableActions, t.tiledesc.replaceAll(" ", "_"));
    }

    private static final RequestParam requestActionForTileCorner(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        int heightOffset = Tiles.decodeHeightOffset(target);
        int tile = Server.surfaceMesh.getTile(x, y);
        availableActions = item == null ? behaviour.getBehavioursFor(creature, (int)x, y, onSurface, true, tile, heightOffset) : behaviour.getBehavioursFor(creature, item, (int)x, y, onSurface, true, tile, heightOffset);
        byte type = Tiles.decodeType(tile);
        Tiles.Tile t = Tiles.getTile(type);
        return new RequestParam(availableActions, t.tiledesc.replaceAll(" ", "_"));
    }

    public static final RequestParam requestActionForCreaturesPlayers(Creature creature, long target, Item item, int targetType, Behaviour behaviour) throws NoSuchPlayerException, NoSuchCreatureException {
        Creature targetc = Server.getInstance().getCreature(target);
        if (targetc.getTemplateId() == 119) {
            return new RequestParam(new ArrayList<ActionEntry>(), "Fishing");
        }
        availableActions = item == null ? behaviour.getBehavioursFor(creature, targetc) : behaviour.getBehavioursFor(creature, item, targetc);
        if (targetType == 1) {
            return new RequestParam(availableActions, targetc.getTemplate().getName().replaceAll(" ", "_"));
        }
        return new RequestParam(availableActions, "Player:" + targetc.getName().replaceAll(" ", "_"));
    }

    public static final RequestParam requestActionForItemsBodyIdsCoinIds(Creature creature, long target, Item item, Behaviour behaviour) throws NoSuchItemException {
        Item targetItem = Items.getItem(target);
        long ownerId = targetItem.getOwnerId();
        if (ownerId == -10L || ownerId == creature.getWurmId() || targetItem.isTraded()) {
            availableActions = item == null ? behaviour.getBehavioursFor(creature, targetItem) : behaviour.getBehavioursFor(creature, item, targetItem);
            if (targetItem.isKingdomMarker() && targetItem.isNoTake()) {
                return new RequestParam(availableActions, targetItem.getTemplate().getName().replaceAll(" ", "_"));
            }
            String name = "";
            name = targetItem.getTemplate().sizeString != null && !targetItem.getTemplate().sizeString.isEmpty() ? StringUtil.format("%s%s", targetItem.getTemplate().sizeString, targetItem.getTemplate().getName()).replaceAll(" ", "_") : targetItem.getTemplate().getName().replaceAll(" ", "_");
            return new RequestParam(availableActions, name);
        }
        if (ownerId != -10L) {
            availableActions = new LinkedList<ActionEntry>();
            availableActions.addAll(Actions.getDefaultItemActions());
            if (targetItem.isKingdomMarker() && targetItem.isNoTake()) {
                return new RequestParam(availableActions, targetItem.getTemplate().getName().replaceAll(" ", "_"));
            }
            String name = "";
            name = targetItem.getTemplate().sizeString.length() > 0 ? StringUtil.format("%s%s", targetItem.getTemplate().sizeString, targetItem.getTemplate().getName()).replaceAll(" ", "_") : targetItem.getTemplate().getName().replaceAll(" ", "_");
            return new RequestParam(availableActions, name);
        }
        return new RequestParam(new LinkedList<ActionEntry>(), "");
    }

    private static final RequestParam requestActionForWalls(Creature creature, long target, Item item, Behaviour behaviour) throws NoSuchWallException {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        boolean onSurface = Tiles.decodeLayer(target) == 0;
        Wall wall = null;
        for (int xx = 1; xx >= -1; --xx) {
            block3: for (int yy = 1; yy >= -1; --yy) {
                try {
                    Zone zone = Zones.getZone(x + xx, y + yy, onSurface);
                    VolaTile tile = zone.getTileOrNull(x + xx, y + yy);
                    if (tile == null) continue;
                    Wall[] walls = tile.getWalls();
                    for (int s = 0; s < walls.length; ++s) {
                        if (walls[s].getId() != target) continue;
                        wall = walls[s];
                        continue block3;
                    }
                    continue;
                }
                catch (NoSuchZoneException noSuchZoneException) {
                    // empty catch block
                }
            }
        }
        if (wall == null) {
            throw new NoSuchWallException("No wall with id " + target);
        }
        availableActions = item == null ? behaviour.getBehavioursFor(creature, wall) : behaviour.getBehavioursFor(creature, item, wall);
        return new RequestParam(availableActions, wall.getIdName());
    }

    private static final RequestParam requestActionForFences(Creature creature, long target, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        boolean onSurface = Tiles.decodeLayer(target) == 0;
        Fence fence = null;
        VolaTile tile = Zones.getTileOrNull(x, y, onSurface);
        if (tile != null) {
            fence = tile.getFence(target);
        }
        if (fence == null) {
            logger.log(Level.WARNING, "Checking for fence with id " + target + " in other tiles. ");
            block2: for (int tx = x - 1; tx <= x + 1; ++tx) {
                for (int ty = y - 1; ty <= y + 1; ++ty) {
                    tile = Zones.getTileOrNull(tx, ty, onSurface);
                    if (tile == null || (fence = tile.getFence(target)) == null) continue;
                    try {
                        Zone zone = Zones.getZone(tx, ty, true);
                        logger.log(Level.INFO, "Found fence in zone " + zone.getId() + " fence has id " + fence.getId() + " and tilex=" + fence.getTileX() + ", tiley=" + fence.getTileY() + " dir=" + (Object)((Object)fence.getDir()));
                        Zone correctZone = Zones.getZone(x, y, true);
                        logger.log(Level.INFO, "We looked for it in zone " + correctZone.getId());
                        if (zone.equals(correctZone)) continue block2;
                        logger.log(Level.INFO, "Correcting the mistake.");
                        zone.removeFence(fence);
                        fence.setZoneId(correctZone.getId());
                        correctZone.addFence(fence);
                        tile.broadCast("The server tried to remedy a fence problem here. Please report if anything happened.");
                    }
                    catch (NoSuchZoneException nsz) {
                        logger.log(Level.WARNING, "Weird: " + nsz.getMessage(), nsz);
                    }
                    continue block2;
                }
            }
        }
        if (fence != null) {
            availableActions = item == null ? behaviour.getBehavioursFor(creature, fence) : behaviour.getBehavioursFor(creature, item, fence);
            return new RequestParam(availableActions, fence.getName().replaceAll(" ", "_"));
        }
        logger.log(Level.WARNING, "Failed to locate fence with id " + target + ".");
        return new RequestParam(new LinkedList<ActionEntry>(), "");
    }

    private static void requestActionForWounds(Creature creature, Communicator comm, byte requestId, long target, Item item, Behaviour behaviour) {
        block4: {
            try {
                Wound wound;
                boolean found = false;
                Wounds wounds = creature.getBody().getWounds();
                if (wounds != null && (wound = wounds.getWound(target)) != null) {
                    found = true;
                    availableActions = item == null ? behaviour.getBehavioursFor(creature, wound) : behaviour.getBehavioursFor(creature, item, wound);
                    comm.sendAvailableActions(requestId, availableActions, wound.getDescription().replaceAll(", bandaged", "").replaceAll(" ", "_"));
                }
                if (!found && (wound = Wounds.getAnyWound(target)) != null) {
                    availableActions = item == null ? behaviour.getBehavioursFor(creature, wound) : behaviour.getBehavioursFor(creature, item, wound);
                    comm.sendAvailableActions(requestId, availableActions, wound.getDescription().replaceAll(", bandaged", "").replaceAll(" ", "_"));
                }
            }
            catch (Exception ex) {
                if (!logger.isLoggable(Level.FINE)) break block4;
                logger.log(Level.FINE, ex.getMessage(), ex);
            }
        }
    }

    private static final RequestParam requestActionForTileBorder(Creature creature, long target, Item item, Behaviour behaviour) {
        boolean onSurface;
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        int heightOffset = Tiles.decodeHeightOffset(target);
        Tiles.TileBorderDirection dir = Tiles.decodeDirection(target);
        boolean bl = onSurface = Tiles.decodeLayer(target) == 0;
        availableActions = MethodsStructure.doesTileBorderContainWallOrFence(x, y, heightOffset, dir, onSurface, true) ? behaviour.getBehavioursFor(creature, (int)x, y, onSurface, dir, true, heightOffset) : (item != null ? behaviour.getBehavioursFor(creature, item, (int)x, y, onSurface, dir, true, heightOffset) : behaviour.getBehavioursFor(creature, (int)x, y, onSurface, dir, true, heightOffset));
        return new RequestParam(availableActions, "Tile_Border");
    }

    private static void requestActionForPlanets(Creature creature, Communicator comm, byte requestId, long target, Item item, Behaviour behaviour) {
        int planetId = (int)(target >> 16) & 0xFFFF;
        availableActions = item == null ? behaviour.getBehavioursFor(creature, planetId) : behaviour.getBehavioursFor(creature, item, planetId);
        comm.sendAvailableActions(requestId, availableActions, PlanetBehaviour.getName(planetId));
    }

    private static void requestActionForMenu(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour) {
        int planetId = (int)(target >> 16) & 0xFFFF;
        availableActions = behaviour.getBehavioursFor(creature, planetId);
        comm.sendAvailableActions(requestId, availableActions, "");
    }

    private static final RequestParam requestActionForCaveTiles(Creature creature, long target, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        int dir = CaveTile.decodeCaveTileDir(target);
        int tile = Server.caveMesh.getTile(x, y);
        availableActions = item == null ? behaviour.getBehavioursFor(creature, x, y, false, tile, dir) : behaviour.getBehavioursFor(creature, item, (int)x, y, false, tile, dir);
        return new RequestParam(availableActions, Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.replaceAll(" ", "_"));
    }

    private static void requestActionForSkillIds(Communicator comm, byte requestId, long target) {
        int skillid = (int)(target >> 32) & 0xFFFFFFFF;
        String name = "unknown";
        if (skillid == 0x7FFFFFFC) {
            comm.sendAvailableActions(requestId, emptyActions, "Favor");
        } else if (skillid == 0x7FFFFFFD) {
            comm.sendAvailableActions(requestId, emptyActions, "Faith");
        } else if (skillid == 0x7FFFFFFA) {
            comm.sendAvailableActions(requestId, emptyActions, "Alignment");
        } else if (skillid == 0x7FFFFFFB) {
            comm.sendAvailableActions(requestId, emptyActions, "Religion");
        } else if (skillid == Integer.MAX_VALUE) {
            comm.sendAvailableActions(requestId, emptyActions, "Skills");
        } else if (skillid == 0x7FFFFFFE) {
            comm.sendAvailableActions(requestId, emptyActions, "Characteristics");
        } else {
            name = SkillSystem.getNameFor(skillid);
            comm.sendAvailableActions(requestId, emptyActions, name.replaceAll(" ", "_"));
        }
    }

    private static final RequestParam requestActionForFloors(Creature creature, long target, boolean onSurface, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        int heightOffset = Tiles.decodeHeightOffset(target);
        String fString = "unknown";
        Floor[] floors = Zones.getFloorsAtTile((int)x, y, heightOffset, heightOffset, onSurface ? 0 : -1);
        if (floors == null) {
            logger.log(Level.WARNING, "No such floor " + target + " (" + x + "," + y + " heightOffset=" + heightOffset + ")");
            return new RequestParam(new LinkedList<ActionEntry>(), "");
        }
        if (floors.length > 1) {
            logger.log(Level.WARNING, "Found more than 1 floor at " + x + "," + y + " heightOffset" + heightOffset);
        }
        Floor floor = floors[0];
        fString = floor.getName();
        availableActions = item == null ? behaviour.getBehavioursFor(creature, onSurface, floor) : behaviour.getBehavioursFor(creature, item, onSurface, floor);
        creature.sendToLoggers("Requesting floor " + floor.getId() + " target requested=" + target + " " + floor.getHeightOffset());
        return new RequestParam(availableActions, fString);
    }

    private static void requestActionForBridgeParts(Creature creature, Communicator comm, byte requestId, long target, boolean onSurface, Item item, Behaviour behaviour) {
        short x = Tiles.decodeTileX(target);
        int y = Tiles.decodeTileY(target);
        short ht = Tiles.decodeHeight(Server.surfaceMesh.getTile(x, y));
        int heightOffset = Tiles.decodeHeightOffset(target) - ht;
        String fString = "unknown";
        BridgePart[] bridgeParts = Zones.getBridgePartsAtTile(x, y, onSurface);
        if (bridgeParts == null) {
            logger.log(Level.WARNING, "No such Bridge Part " + target + " (" + x + "," + y + " heightOffset=" + heightOffset + ")");
        } else {
            if (bridgeParts.length > 1) {
                logger.log(Level.WARNING, "Found more than 1 bridge part at " + x + "," + y + " heightOffset" + heightOffset);
            }
            BridgePart bridgePart = bridgeParts[0];
            fString = bridgePart.getName();
            availableActions = item == null ? behaviour.getBehavioursFor(creature, onSurface, bridgePart) : behaviour.getBehavioursFor(creature, item, onSurface, bridgePart);
            creature.sendToLoggers("Requesting bridge part " + bridgePart.getId() + " target requested=" + target + " " + bridgePart.getHeightOffset());
            comm.sendAvailableActions(requestId, availableActions, fString);
        }
    }

    private static void requestActionForMissionPerformed(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour) {
        int missionId = MissionPerformed.decodeMissionId(target);
        Mission m = Missions.getMissionWithId(missionId);
        String mString = "unknown";
        if (m != null) {
            mString = m.getName();
        }
        comm.sendAvailableActions(requestId, behaviour.getBehavioursFor(creature, missionId), "Mission:" + mString);
    }

    private static final RequestParam requestActionForIllusions(Creature creature, long target, Item item, int targetType, Behaviour behaviour) throws NoSuchPlayerException, NoSuchCreatureException {
        long wid = Creature.getWurmIdForIllusion(target);
        return BehaviourDispatcher.requestActionForCreaturesPlayers(creature, wid, item, targetType, behaviour);
    }

    private static void requestActionForTickets(Creature creature, Communicator comm, byte requestId, long target, Behaviour behaviour) {
        int ticketId = Tickets.decodeTicketId(target);
        comm.sendAvailableActions(requestId, behaviour.getBehavioursFor(creature, ticketId), "Ticket:" + ticketId);
    }

    public static void action(Creature creature, Communicator comm, long subject, long target, short action) throws NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException, NoSuchWallException, FailedException {
        String s = "unknown";
        try {
            s = Actions.getVerbForAction(action);
        }
        catch (Exception e) {
            s = "" + action;
        }
        if (creature.isUndead() && action != 326 && action != 1 && !Action.isActionAttack(action) && !Action.isStanceChange(action) && action != 523 && action != 522) {
            creature.getCommunicator().sendNormalServerMessage("Unnn..");
            return;
        }
        creature.sendToLoggers("Received action number " + s + ", target " + target + ", source " + subject + ", action " + action, (byte)2);
        if (creature.isFrozen()) {
            creature.sendToLoggers("Frozen. Ignoring.", (byte)2);
            throw new FailedException("Frozen");
        }
        if (creature.isTeleporting()) {
            comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
            throw new FailedException("Teleporting");
        }
        if (action == 149) {
            try {
                if (creature.getCurrentAction().isSpell() || !creature.getCurrentAction().isOffensive() || !creature.isFighting()) {
                    creature.stopCurrentAction();
                }
            }
            catch (NoSuchActionException e) {}
        } else {
            float z;
            float y;
            float x = creature.getStatus().getPositionX();
            Action toSet = new Action(creature, subject, target, action, x, y = creature.getStatus().getPositionY(), z = creature.getStatus().getPositionZ() + creature.getAltOffZ(), creature.getStatus().getRotation());
            if (toSet.isQuick()) {
                toSet.poll();
            } else if (toSet.isStanceChange() && toSet.getNumber() != 340) {
                if (!toSet.poll()) {
                    creature.setAction(toSet);
                }
            } else {
                toSet.setRarity(creature.getRarity());
                creature.setAction(toSet);
            }
        }
    }

    public static void action(Creature creature, Communicator comm, long subject, long[] targets, short action) throws FailedException, NoSuchPlayerException, NoSuchCreatureException, NoSuchItemException, NoSuchBehaviourException {
        float z;
        float y;
        String s = "unknown";
        try {
            s = Actions.getVerbForAction(action);
        }
        catch (Exception e) {
            s = "" + action;
        }
        if (creature.isUndead()) {
            creature.getCommunicator().sendNormalServerMessage("Unnn..");
            return;
        }
        String tgts = "";
        for (int x = 0; x < targets.length; ++x) {
            if (tgts.length() > 0) {
                tgts = tgts + ", ";
            }
            tgts = tgts + targets[x];
        }
        creature.sendToLoggers("Received action number " + s + ", target " + tgts, (byte)2);
        if (creature.isFrozen()) {
            creature.sendToLoggers("Frozen. Ignoring.", (byte)2);
            throw new FailedException("Frozen");
        }
        if (creature.isTeleporting()) {
            comm.sendAlertServerMessage("You are teleporting and cannot perform actions right now.");
            throw new FailedException("Teleporting");
        }
        float x = creature.getStatus().getPositionX();
        Action toSet = new Action(creature, subject, targets, action, x, y = creature.getStatus().getPositionY(), z = creature.getStatus().getPositionZ() + creature.getAltOffZ(), creature.getStatus().getRotation());
        if (toSet.isQuick()) {
            toSet.poll();
        } else {
            toSet.setRarity(creature.getRarity());
            creature.setAction(toSet);
        }
    }

    public static class RequestParam {
        private final String helpString;
        private List<ActionEntry> availableActions;

        public RequestParam(List<ActionEntry> actions, String help) {
            this.availableActions = actions;
            this.helpString = help;
        }

        public final List<ActionEntry> getAvailableActions() {
            return this.availableActions;
        }

        public final String getHelpString() {
            return this.helpString;
        }

        public void filterForSelectBar() {
            for (int i = this.availableActions.size() - 1; i >= 0; --i) {
                ActionEntry entry = this.availableActions.get(i);
                if (entry.isShowOnSelectBar()) continue;
                this.availableActions.remove(i);
            }
        }
    }
}

