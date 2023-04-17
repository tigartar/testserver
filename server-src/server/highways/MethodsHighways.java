/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.highways;

import com.wurmonline.math.Vector2f;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MeshTile;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.HighwayConstants;
import com.wurmonline.shared.constants.StructureConstants;
import java.util.HashSet;
import javax.annotation.Nullable;

public class MethodsHighways
implements MiscConstants,
HighwayConstants {
    private MethodsHighways() {
    }

    public static final boolean middleOfHighway(HighwayPos highwayPos) {
        int tilex = highwayPos.getTilex();
        int tiley = highwayPos.getTiley();
        boolean onSurface = highwayPos.isOnSurface();
        BridgePart currentBridgePart = highwayPos.getBridgePart();
        Floor currentFloor = highwayPos.getFloor();
        if (currentBridgePart != null) {
            return MethodsHighways.bridgeChecks(tilex, tiley, onSurface, currentBridgePart);
        }
        if (currentFloor != null) {
            return MethodsHighways.floorChecks(tilex, tiley, onSurface, currentFloor);
        }
        if (!onSurface) {
            return MethodsHighways.caveChecks(tilex, tiley);
        }
        return MethodsHighways.surfaceChecks(tilex, tiley);
    }

    public static final boolean onHighway(Item item) {
        if (!Features.Feature.HIGHWAYS.isEnabled()) {
            return false;
        }
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(item);
        return MethodsHighways.onHighway(highwayPos);
    }

    public static final boolean onHighway(int cornerx, int cornery, boolean onSurface) {
        if (!Features.Feature.HIGHWAYS.isEnabled()) {
            return false;
        }
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(cornerx, cornery, onSurface);
        return MethodsHighways.onHighway(highwayPos);
    }

    public static final boolean onWagonerCamp(int cornerx, int cornery, boolean onSurface) {
        if (!Features.Feature.HIGHWAYS.isEnabled()) {
            return false;
        }
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(cornerx, cornery, onSurface);
        return MethodsHighways.onWagonerCamp(highwayPos);
    }

    public static final boolean onHighway(BridgePart bridgePart) {
        if (!Features.Feature.HIGHWAYS.isEnabled()) {
            return false;
        }
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(bridgePart);
        return MethodsHighways.onHighway(highwayPos);
    }

    public static final boolean onHighway(Floor floor) {
        if (!Features.Feature.HIGHWAYS.isEnabled()) {
            return false;
        }
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(floor);
        return MethodsHighways.onHighway(highwayPos);
    }

    public static final boolean onHighway(@Nullable HighwayPos highwaypos) {
        if (highwaypos == null) {
            return false;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)0)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)1)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)2)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)4)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)8)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)16)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)32)) {
            return true;
        }
        if (MethodsHighways.containsMarker(highwaypos, (byte)64)) {
            return true;
        }
        return MethodsHighways.containsMarker(highwaypos, (byte)-128);
    }

    public static final boolean onWagonerCamp(@Nullable HighwayPos highwaypos) {
        if (highwaypos == null) {
            return false;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)0)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)1)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)2)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)4)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)8)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)16)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)32)) {
            return true;
        }
        if (MethodsHighways.containsWagonerWaystone(highwaypos, (byte)64)) {
            return true;
        }
        return MethodsHighways.containsWagonerWaystone(highwaypos, (byte)-128);
    }

    private static final boolean caveChecks(int tilex, int tiley) {
        MeshIO caveMesh = Server.caveMesh;
        int currentEncodedTile = caveMesh.getTile(tilex, tiley);
        byte currentType = Tiles.decodeType(currentEncodedTile);
        boolean onSurface = false;
        if (currentType != Tiles.Tile.TILE_CAVE_EXIT.id) {
            int northWestEncodedTile;
            byte northWestType;
            BridgePart bridgePart;
            boolean foundBridge = false;
            if (!Tiles.isReinforcedFloor(currentType) && currentType != Tiles.Tile.TILE_CAVE_EXIT.id) {
                return false;
            }
            int northEncodedTile = caveMesh.getTile(tilex, tiley - 1);
            byte northType = Tiles.decodeType(northEncodedTile);
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, false);
            if (bridgePartNorth != null) {
                if (bridgePartNorth.getSouthExit() == 0) {
                    foundBridge = true;
                }
            } else if (!Tiles.isReinforcedFloor(northType) && northType != Tiles.Tile.TILE_CAVE_EXIT.id) {
                return false;
            }
            int westEncodedTile = caveMesh.getTile(tilex - 1, tiley);
            byte westType = Tiles.decodeType(westEncodedTile);
            BridgePart bridgePartWest = Zones.getBridgePartFor(tilex, tiley - 1, false);
            if (bridgePartWest != null) {
                if (bridgePartWest.getEastExit() == 0) {
                    foundBridge = true;
                }
            } else if (!Tiles.isReinforcedFloor(westType) && westType != Tiles.Tile.TILE_CAVE_EXIT.id) {
                return false;
            }
            return !(foundBridge ? (bridgePart = Zones.getBridgePartFor(tilex - 1, tiley - 1, false)) == null : !Tiles.isReinforcedFloor(northWestType = Tiles.decodeType(northWestEncodedTile = caveMesh.getTile(tilex - 1, tiley - 1))) && northWestType != Tiles.Tile.TILE_CAVE_EXIT.id);
        }
        for (int x = -1; x <= 0; ++x) {
            for (int y = -1; y <= 0; ++y) {
                int encodedTile = caveMesh.getTile(tilex + x, tiley + y);
                byte type = Tiles.decodeType(encodedTile);
                if (Tiles.isReinforcedFloor(type) || Tiles.isRoadType(type) || type == Tiles.Tile.TILE_CAVE_EXIT.id) continue;
                if (Tiles.isSolidCave(type)) {
                    int surfaceTile = Server.surfaceMesh.getTile(tilex + x, tiley + y);
                    byte surfaceType = Tiles.decodeType(surfaceTile);
                    if (Tiles.isRoadType(surfaceType)) continue;
                    return false;
                }
                return false;
            }
        }
        return true;
    }

    private static final boolean surfaceChecks(int tilex, int tiley) {
        int northWestEncodedTile;
        byte northWestType;
        BridgePart bridgePart;
        int westEncodedTile;
        byte westType;
        boolean foundBridge = false;
        boolean onSurface = true;
        int currentEncodedTile = Server.surfaceMesh.getTile(tilex, tiley);
        byte currentType = Tiles.decodeType(currentEncodedTile);
        if (!Tiles.isRoadType(currentType) && currentType != Tiles.Tile.TILE_HOLE.id) {
            return false;
        }
        int northEncodedTile = Server.surfaceMesh.getTile(tilex, tiley - 1);
        byte northType = Tiles.decodeType(northEncodedTile);
        if (!Tiles.isRoadType(northType) && northType != Tiles.Tile.TILE_HOLE.id) {
            BridgePart bridgePart2 = Zones.getBridgePartFor(tilex, tiley - 1, true);
            if (bridgePart2 == null) {
                return false;
            }
            if (bridgePart2.getSouthExit() == 0) {
                foundBridge = true;
            }
        }
        if (!Tiles.isRoadType(westType = Tiles.decodeType(westEncodedTile = Server.surfaceMesh.getTile(tilex - 1, tiley))) && westType != Tiles.Tile.TILE_HOLE.id) {
            bridgePart = Zones.getBridgePartFor(tilex - 1, tiley, true);
            if (bridgePart == null) {
                return false;
            }
            if (bridgePart.getEastExit() == 0) {
                foundBridge = true;
            }
        }
        return !(foundBridge ? (bridgePart = Zones.getBridgePartFor(tilex - 1, tiley - 1, true)) == null : !Tiles.isRoadType(northWestType = Tiles.decodeType(northWestEncodedTile = Server.surfaceMesh.getTile(tilex - 1, tiley - 1))) && northWestType != Tiles.Tile.TILE_HOLE.id);
    }

    private static final boolean bridgeChecks(int tilex, int tiley, boolean onSurface, BridgePart currentBridgePart) {
        if (currentBridgePart.hasNorthExit()) {
            if (currentBridgePart.getNorthExit() == 0) {
                MeshIO mesh;
                MeshIO meshIO = mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
                if (!Tiles.isRoadType(mesh.getTile(tilex, tiley - 1))) {
                    return false;
                }
                if (!Tiles.isRoadType(mesh.getTile(tilex - 1, tiley - 1))) {
                    return false;
                }
                BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
                return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
            }
            Floor floorNorth = Zones.getFloor(tilex, tiley - 1, onSurface, currentBridgePart.getNorthExitFloorLevel());
            if (floorNorth == null || floorNorth.getFloorState() != StructureConstants.FloorState.COMPLETED) {
                return false;
            }
            Floor floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentBridgePart.getNorthExitFloorLevel());
            if (floorNorthWest == null || floorNorthWest.getFloorState() != StructureConstants.FloorState.COMPLETED) {
                return false;
            }
            BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
            return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
        }
        if (currentBridgePart.hasWestExit()) {
            if (currentBridgePart.getWestExit() == 0) {
                MeshIO mesh = onSurface ? Server.surfaceMesh : Server.caveMesh;
                BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
                if (bridgePartNorth == null || bridgePartNorth.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) {
                    return false;
                }
                if (!Tiles.isRoadType(mesh.getTile(tilex - 1, tiley - 1))) {
                    return false;
                }
                return Tiles.isRoadType(mesh.getTile(tilex - 1, tiley));
            }
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
            if (bridgePartNorth == null || bridgePartNorth.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) {
                return false;
            }
            Floor floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentBridgePart.getWestExitFloorLevel());
            if (floorNorthWest == null || floorNorthWest.getFloorState() != StructureConstants.FloorState.COMPLETED) {
                return false;
            }
            Floor floorWest = Zones.getFloor(tilex - 1, tiley, onSurface, currentBridgePart.getWestExitFloorLevel());
            return floorWest != null && floorWest.getFloorState() == StructureConstants.FloorState.COMPLETED;
        }
        BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
        if (bridgePartNorth == null || bridgePartNorth.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) {
            return false;
        }
        BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
        if (bridgePartNorthWest == null || bridgePartNorthWest.getBridgePartState() != BridgeConstants.BridgeState.COMPLETED) {
            return false;
        }
        BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
        return bridgePartWest != null && bridgePartWest.getBridgePartState() == BridgeConstants.BridgeState.COMPLETED;
    }

    private static final boolean floorChecks(int tilex, int tiley, boolean onSurface, Floor currentFloor) {
        Floor floorNorthWest;
        Floor floorWest;
        Floor floorNorth = Zones.getFloor(tilex, tiley - 1, onSurface, currentFloor.getFloorLevel());
        if (floorNorth == null) {
            BridgePart bridgePartNorth = Zones.getBridgePartFor(tilex, tiley - 1, onSurface);
            if (bridgePartNorth == null || bridgePartNorth.getSouthExitFloorLevel() != currentFloor.getFloorLevel()) {
                return false;
            }
            BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
            if (bridgePartNorthWest == null || bridgePartNorthWest.getSouthExitFloorLevel() != currentFloor.getFloorLevel()) {
                return false;
            }
        }
        if ((floorWest = Zones.getFloor(tilex - 1, tiley, onSurface, currentFloor.getFloorLevel())) == null) {
            BridgePart bridgePartWest = Zones.getBridgePartFor(tilex - 1, tiley, onSurface);
            if (bridgePartWest == null || bridgePartWest.getEastExitFloorLevel() != currentFloor.getFloorLevel()) {
                return false;
            }
            BridgePart bridgePartNorthWest = Zones.getBridgePartFor(tilex - 1, tiley - 1, onSurface);
            if (bridgePartNorthWest == null || bridgePartNorthWest.getEastExitFloorLevel() != currentFloor.getFloorLevel()) {
                return false;
            }
        }
        return floorNorth == null || floorWest == null || (floorNorthWest = Zones.getFloor(tilex - 1, tiley - 1, onSurface, currentFloor.getFloorLevel())) != null;
    }

    public static final boolean hasLink(byte dirs, byte linkdir) {
        return (dirs & linkdir) != 0;
    }

    public static final byte getPossibleLinksFrom(Item marker) {
        HighwayPos highwayPos = MethodsHighways.getHighwayPosFromMarker(marker);
        return MethodsHighways.getPossibleLinksFrom(highwayPos, marker, marker.getAuxData());
    }

    public static final byte getPossibleLinksFrom(HighwayPos highwayPos, Item marker) {
        return MethodsHighways.getPossibleLinksFrom(highwayPos, marker, (byte)0);
    }

    private static final byte getPossibleLinksFrom(HighwayPos highwayPos, Item marker, byte currentLinks) {
        int uplo;
        int lonew;
        int lower;
        int loup;
        int upper;
        int upnew;
        byte poss;
        byte possibles = (byte)(~currentLinks & 0xFF);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)1);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)2);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)4);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)8);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)16);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)32);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)64);
        possibles = MethodsHighways.checkLink(possibles, highwayPos, (byte)-128);
        if (marker.getTemplateId() == 1114 && MethodsHighways.numberOfSetBits(possibles) > 2 && MethodsHighways.numberOfSetBits(poss = (byte)((upnew = (upper = possibles & 0xF0) & (loup = (lower = possibles & 0xF) << 4)) | (lonew = lower & (uplo = upper >>> 4)))) == 2) {
            possibles = poss;
        }
        return possibles;
    }

    private static final byte checkLink(byte possibles, HighwayPos currentHighwayPos, byte checkdir) {
        if (MethodsHighways.hasLink(possibles, checkdir)) {
            HighwayPos highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, checkdir);
            if (highwayPos != null) {
                Item marker = MethodsHighways.getMarker(highwayPos);
                if (marker == null) {
                    return (byte)(possibles & ~checkdir);
                }
                if (MethodsHighways.hasLink(MethodsHighways.getOppositedir(checkdir), marker.getAuxData())) {
                    return (byte)(possibles & ~checkdir);
                }
                if (marker.getTemplateId() == 1114 && MethodsHighways.numberOfSetBits(marker.getAuxData()) > 1) {
                    return (byte)(possibles & ~checkdir);
                }
            } else {
                return (byte)(possibles & ~checkdir);
            }
        }
        return possibles;
    }

    public static final void autoLink(Item newMarker, byte possibleLinks) {
        HighwayPos currentHighwayPos = MethodsHighways.getHighwayPosFromMarker(newMarker);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)1, (byte)16);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)2, (byte)32);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)4, (byte)64);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)8, (byte)-128);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)16, (byte)1);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)32, (byte)2);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)64, (byte)4);
        MethodsHighways.addLink(newMarker, currentHighwayPos, possibleLinks, (byte)-128, (byte)8);
        Routes.checkForNewRoutes(newMarker);
    }

    private static final void addLink(Item newMarker, HighwayPos currentHighwayPos, byte possibles, byte linkdir, byte reversedir) {
        Item linkMarker;
        if (MethodsHighways.hasLink(possibles, linkdir) && (linkMarker = MethodsHighways.getMarker(currentHighwayPos, linkdir)) != null) {
            newMarker.setAuxData((byte)(newMarker.getAuxData() | linkdir));
            linkMarker.setAuxData((byte)(linkMarker.getAuxData() | reversedir));
            newMarker.updateModelNameOnGroundItem();
            linkMarker.updateModelNameOnGroundItem();
        }
    }

    public static final void removeLinksTo(Item fromMarker) {
        Item[] markers = Routes.getRouteMarkers(fromMarker);
        HighwayPos currentHighwayPos = MethodsHighways.getHighwayPosFromMarker(fromMarker);
        MethodsHighways.removeLink(currentHighwayPos, (byte)1, (byte)16);
        MethodsHighways.removeLink(currentHighwayPos, (byte)2, (byte)32);
        MethodsHighways.removeLink(currentHighwayPos, (byte)4, (byte)64);
        MethodsHighways.removeLink(currentHighwayPos, (byte)8, (byte)-128);
        MethodsHighways.removeLink(currentHighwayPos, (byte)16, (byte)1);
        MethodsHighways.removeLink(currentHighwayPos, (byte)32, (byte)2);
        MethodsHighways.removeLink(currentHighwayPos, (byte)64, (byte)4);
        MethodsHighways.removeLink(currentHighwayPos, (byte)-128, (byte)8);
        fromMarker.setAuxData((byte)0);
        Items.removeMarker(fromMarker);
        fromMarker.updateModelNameOnGroundItem();
        for (Item marker : markers) {
            marker.updateModelNameOnGroundItem();
        }
    }

    private static final void removeLink(HighwayPos currentHighwayPos, byte fromdir, byte linkdir) {
        Item marker = MethodsHighways.getMarker(currentHighwayPos, fromdir);
        if (marker != null && MethodsHighways.hasLink(marker.getAuxData(), linkdir)) {
            marker.setAuxData((byte)(marker.getAuxData() & ~linkdir));
            marker.updateModelNameOnGroundItem();
        }
    }

    @Nullable
    public static final Item getMarker(Item marker, byte dir) {
        HighwayPos currentHighwayPos = MethodsHighways.getHighwayPosFromMarker(marker);
        switch (dir) {
            case 1: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)1);
            }
            case 2: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)2);
            }
            case 4: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)4);
            }
            case 8: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)8);
            }
            case 16: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)16);
            }
            case 32: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)32);
            }
            case 64: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)64);
            }
            case -128: {
                return MethodsHighways.getMarker(currentHighwayPos, (byte)-128);
            }
        }
        return null;
    }

    public static final boolean viewProtection(Creature performer, Item marker) {
        HighwayPos highwayPos = MethodsHighways.getHighwayPosFromMarker(marker);
        return MethodsHighways.sendShowProtection(performer, marker, highwayPos);
    }

    public static final boolean viewProtection(Creature performer, HighwayPos highwayPos, Item marker) {
        return MethodsHighways.sendShowProtection(performer, marker, highwayPos);
    }

    public static final boolean viewLinks(Creature performer, Item marker) {
        HighwayPos highwayPos = MethodsHighways.getHighwayPosFromMarker(marker);
        return MethodsHighways.viewLinks(performer, highwayPos, marker, (byte)1, marker.getAuxData());
    }

    public static final boolean viewLinks(Creature performer, HighwayPos highwayPos, Item marker) {
        byte links = MethodsHighways.getPossibleLinksFrom(highwayPos, marker);
        return MethodsHighways.viewLinks(performer, highwayPos, marker, (byte)0, links);
    }

    public static final boolean viewLinks(Creature performer, HighwayPos currentHighwayPos, Item marker, byte linktype, byte links) {
        String linktypeString = linktype == 1 ? "Links" : "Possible links";
        boolean showing = false;
        if (links == 0) {
            performer.getCommunicator().sendNormalServerMessage("There are no " + linktypeString.toLowerCase() + " from there!");
        } else {
            showing = MethodsHighways.sendShowLinks(performer, currentHighwayPos, marker, linktype, links);
            if (Servers.isThisATestServer()) {
                int count = 0;
                int todo = MethodsHighways.numberOfSetBits(links);
                StringBuilder buf = new StringBuilder();
                buf.append(linktypeString + " are: ");
                if (MethodsHighways.hasLink(links, (byte)1) && MethodsHighways.containsMarker(currentHighwayPos, (byte)1)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)1));
                }
                if (MethodsHighways.hasLink(links, (byte)2) && MethodsHighways.containsMarker(currentHighwayPos, (byte)2)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)2));
                }
                if (MethodsHighways.hasLink(links, (byte)4) && MethodsHighways.containsMarker(currentHighwayPos, (byte)4)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)4));
                }
                if (MethodsHighways.hasLink(links, (byte)8) && MethodsHighways.containsMarker(currentHighwayPos, (byte)8)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)8));
                }
                if (MethodsHighways.hasLink(links, (byte)16) && MethodsHighways.containsMarker(currentHighwayPos, (byte)16)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)16));
                }
                if (MethodsHighways.hasLink(links, (byte)32) && MethodsHighways.containsMarker(currentHighwayPos, (byte)32)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)32));
                }
                if (MethodsHighways.hasLink(links, (byte)64) && MethodsHighways.containsMarker(currentHighwayPos, (byte)64)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)64));
                }
                if (MethodsHighways.hasLink(links, (byte)-128) && MethodsHighways.containsMarker(currentHighwayPos, (byte)-128)) {
                    if (count++ > 0) {
                        if (count == todo) {
                            buf.append(" and ");
                        } else {
                            buf.append(", ");
                        }
                    }
                    buf.append(MethodsHighways.getLinkDirString((byte)-128));
                }
                performer.getCommunicator().sendNormalServerMessage("test only:" + buf.toString());
            }
        }
        return showing;
    }

    private static final boolean sendShowLinks(Creature performer, HighwayPos currentHighwayPos, Item marker, byte linktype, byte links) {
        boolean markerType = marker.getTemplateId() == 1112;
        byte[] glows = new byte[]{MethodsHighways.getLinkGlow(linktype, marker, links, (byte)1), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)2), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)4), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)8), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)16), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)32), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)64), MethodsHighways.getLinkGlow(linktype, marker, links, (byte)-128)};
        return performer.getCommunicator().sendShowLinks(markerType, currentHighwayPos, glows);
    }

    private static final byte getLinkGlow(byte linktype, Item marker, byte links, byte link) {
        if (MethodsHighways.hasLink(links, link)) {
            if (linktype == 1) {
                if (marker.getTemplateId() == 1112) {
                    Route route;
                    Node node = Routes.getNode(marker.getWurmId());
                    if (node != null && (route = node.getRoute(link)) != null) {
                        return 3;
                    }
                    return 1;
                }
                int count = MethodsHighways.numberOfSetBits(marker.getAuxData());
                if (count == 2) {
                    return 3;
                }
                if (count == 1) {
                    return 2;
                }
                return 1;
            }
            if (marker.getTemplateId() == 1112) {
                // empty if block
            }
            return 2;
        }
        return -1;
    }

    public static final boolean sendShowProtection(Creature performer, Item marker, HighwayPos currentHighwayPos) {
        StringBuilder buf = new StringBuilder();
        buf.append("Protected: center");
        boolean markerType = marker.getTemplateId() == 1112;
        HashSet<HighwayPos> protectedTiles = new HashSet<HighwayPos>();
        HighwayPos highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)1);
        if (highwayPos != null) {
            protectedTiles.add(highwayPos);
            buf.append(", north");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)2)) != null && MethodsHighways.isPaved(highwayPos)) {
            protectedTiles.add(highwayPos);
            buf.append(", northeast");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)4)) != null && MethodsHighways.isPaved(highwayPos)) {
            protectedTiles.add(highwayPos);
            buf.append(", east");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)8)) != null && MethodsHighways.isPaved(highwayPos)) {
            protectedTiles.add(highwayPos);
            buf.append(", southeast");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)16)) != null && MethodsHighways.isPaved(highwayPos)) {
            protectedTiles.add(highwayPos);
            buf.append(", south");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)32)) != null && MethodsHighways.isPaved(highwayPos)) {
            protectedTiles.add(highwayPos);
            buf.append(", southwest");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)64)) != null) {
            protectedTiles.add(highwayPos);
            buf.append(", west");
        }
        if ((highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)-128)) != null) {
            protectedTiles.add(highwayPos);
            buf.append(", northwest");
        }
        HighwayPos[] protectedHPs = protectedTiles.toArray(new HighwayPos[protectedTiles.size()]);
        if (Servers.isThisATestServer()) {
            int pos = buf.lastIndexOf(",");
            if (pos > 0) {
                buf.replace(pos, pos + 1, " and");
            }
            performer.getCommunicator().sendNormalServerMessage("test only:" + buf.toString());
        }
        return performer.getCommunicator().sendShowProtection(markerType, currentHighwayPos, protectedHPs);
    }

    private static boolean isPaved(HighwayPos highwayPos) {
        int caveTile;
        byte caveType;
        int surfaceTile;
        byte surfaceType;
        if (highwayPos.getBridgeId() != -10L) {
            return true;
        }
        if (highwayPos.getFloorLevel() > 0) {
            return true;
        }
        return !(highwayPos.isOnSurface() ? !Tiles.isRoadType(surfaceType = Tiles.decodeType(surfaceTile = Server.surfaceMesh.getTile(highwayPos.getTilex(), highwayPos.getTiley()))) : !Tiles.isReinforcedFloor(caveType = Tiles.decodeType(caveTile = Server.caveMesh.getTile(highwayPos.getTilex(), highwayPos.getTiley()))) && !Tiles.isRoadType(caveType) && caveType != Tiles.Tile.TILE_CAVE_EXIT.id);
    }

    public static final String getLinkAsString(byte links) {
        int count = 0;
        int todo = MethodsHighways.numberOfSetBits(links);
        StringBuilder buf = new StringBuilder();
        if (MethodsHighways.hasLink(links, (byte)1)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)1));
        }
        if (MethodsHighways.hasLink(links, (byte)2)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)2));
        }
        if (MethodsHighways.hasLink(links, (byte)4)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)4));
        }
        if (MethodsHighways.hasLink(links, (byte)8)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)8));
        }
        if (MethodsHighways.hasLink(links, (byte)16)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)16));
        }
        if (MethodsHighways.hasLink(links, (byte)32)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)32));
        }
        if (MethodsHighways.hasLink(links, (byte)64)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)64));
        }
        if (MethodsHighways.hasLink(links, (byte)-128)) {
            if (count++ > 0) {
                if (count == todo) {
                    buf.append(" and ");
                } else {
                    buf.append(", ");
                }
            }
            buf.append(MethodsHighways.getLinkDirString((byte)-128));
        }
        if (count == 0) {
            buf.append("none");
        }
        return buf.toString();
    }

    public static final boolean containsWagonerWaystone(HighwayPos highwayPos, byte fromdir) {
        Item marker = MethodsHighways.getMarker(highwayPos, fromdir);
        if (marker == null || marker.getTemplateId() == 1114) {
            return false;
        }
        return marker.getData() != -1L;
    }

    public static final boolean containsMarker(HighwayPos highwayPos, byte fromdir) {
        return MethodsHighways.getMarker(highwayPos, fromdir) != null;
    }

    @Nullable
    public static final Item getMarker(@Nullable HighwayPos currentHighwayPos, byte fromdir) {
        if (currentHighwayPos == null) {
            return null;
        }
        if (fromdir == 0) {
            return MethodsHighways.getMarker(currentHighwayPos);
        }
        HighwayPos highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, fromdir);
        if (highwayPos != null) {
            return MethodsHighways.getMarker(highwayPos);
        }
        return null;
    }

    @Nullable
    public static final Item getMarker(HighwayPos highwaypos) {
        if (highwaypos == null) {
            return null;
        }
        return Items.getMarker(highwaypos.getTilex(), highwaypos.getTiley(), highwaypos.isOnSurface(), highwaypos.getFloorLevel(), highwaypos.getBridgeId());
    }

    @Nullable
    public static final Item getMarker(Creature creature) {
        return Items.getMarker(creature.getTileX(), creature.getTileY(), creature.isOnSurface(), creature.getFloorLevel(), creature.getBridgeId());
    }

    @Nullable
    public static final HighwayPos getHighwayPos(Item marker) {
        int tilex = marker.getTileX();
        int tiley = marker.getTileY();
        boolean onSurface = marker.isOnSurface();
        if (marker.getBridgeId() != -10L) {
            return new HighwayPos(tilex, tiley, onSurface, Zones.getBridgePartFor(tilex, tiley, onSurface), null);
        }
        if (marker.getFloorLevel() > 0) {
            return new HighwayPos(tilex, tiley, onSurface, null, Zones.getFloor(tilex, tiley, onSurface, marker.getFloorLevel()));
        }
        return new HighwayPos(tilex, tiley, onSurface, null, null);
    }

    @Nullable
    public static final HighwayPos getHighwayPos(BridgePart bridgePart) {
        int tilex = bridgePart.getTileX();
        int tiley = bridgePart.getTileY();
        boolean onSurface = bridgePart.isOnSurface();
        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
    }

    @Nullable
    public static final HighwayPos getHighwayPos(Floor floor) {
        int tilex = floor.getTileX();
        int tiley = floor.getTileY();
        boolean onSurface = floor.isOnSurface();
        return new HighwayPos(tilex, tiley, onSurface, null, floor);
    }

    @Nullable
    public static final HighwayPos getNewHighwayPosLinked(@Nullable HighwayPos currentHighwayPos, byte todir) {
        if (currentHighwayPos == null) {
            return null;
        }
        int tilex = currentHighwayPos.getTilex();
        int tiley = currentHighwayPos.getTiley();
        switch (todir) {
            case 1: {
                --tiley;
                break;
            }
            case 2: {
                --tiley;
                ++tilex;
                break;
            }
            case 4: {
                ++tilex;
                break;
            }
            case 8: {
                ++tiley;
                ++tilex;
                break;
            }
            case 16: {
                ++tiley;
                break;
            }
            case 32: {
                ++tiley;
                --tilex;
                break;
            }
            case 64: {
                --tilex;
                break;
            }
            case -128: {
                --tiley;
                --tilex;
            }
        }
        boolean onSurface = currentHighwayPos.isOnSurface();
        if (currentHighwayPos.getBridgePart() != null) {
            return MethodsHighways.getNewHighwayPosFromBridge(tilex, tiley, onSurface, currentHighwayPos.getBridgePart(), todir);
        }
        if (currentHighwayPos.getFloor() != null) {
            return MethodsHighways.getNewHighwayPosFromFloor(tilex, tiley, onSurface, currentHighwayPos.getFloor(), todir);
        }
        if (onSurface) {
            int encodedtile = Server.surfaceMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(encodedtile);
            if (type == Tiles.Tile.TILE_HOLE.id) {
                return new HighwayPos(tilex, tiley, false, null, null);
            }
            BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
            if (bridgePart != null) {
                if (bridgePart.getSouthExit() == 0 && (todir == -128 || todir == 1 || todir == 2)) {
                    return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getWestExit() == 0 && (todir == 2 || todir == 4 || todir == 8)) {
                    return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getNorthExit() == 0 && (todir == 8 || todir == 16 || todir == 32)) {
                    return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getEastExit() == 0 && (todir == 32 || todir == 64 || todir == -128)) {
                    return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                }
            }
        } else {
            int encodedCurrentTile = Server.caveMesh.getTile(currentHighwayPos.getTilex(), currentHighwayPos.getTiley());
            byte currentType = Tiles.decodeType(encodedCurrentTile);
            int encodedtile = Server.caveMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(encodedtile);
            if (currentType == Tiles.Tile.TILE_CAVE_EXIT.id) {
                if (Tiles.isSolidCave(type)) {
                    return new HighwayPos(tilex, tiley, true, null, null);
                }
            } else {
                if (Tiles.isSolidCave(type)) {
                    return null;
                }
                BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
                if (bridgePart != null) {
                    if (bridgePart.getSouthExit() == 0 && (todir == -128 || todir == 1 || todir == 2)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                    }
                    if (bridgePart.getWestExit() == 0 && (todir == 2 || todir == 4 || todir == 8)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                    }
                    if (bridgePart.getNorthExit() == 0 && (todir == 8 || todir == 16 || todir == 32)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                    }
                    if (bridgePart.getEastExit() == 0 && (todir == 32 || todir == 64 || todir == -128)) {
                        return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
                    }
                }
            }
        }
        return new HighwayPos(tilex, tiley, onSurface, null, null);
    }

    @Nullable
    public static final HighwayPos getHighwayPos(int cornerx, int cornery, boolean onSurface) {
        int encodedTile;
        byte type;
        if (onSurface && (type = Tiles.decodeType(encodedTile = Server.surfaceMesh.getTile(cornerx, cornery))) == Tiles.Tile.TILE_HOLE.id) {
            return new HighwayPos(cornerx, cornery, false, null, null);
        }
        BridgePart bridgePart = Zones.getBridgePartFor(cornerx, cornery, onSurface);
        if (bridgePart != null && (bridgePart.getNorthExit() == 0 || bridgePart.getEastExit() == 0 || bridgePart.getSouthExit() == 0 || bridgePart.getWestExit() == 0)) {
            return new HighwayPos(cornerx, cornery, onSurface, bridgePart, null);
        }
        return new HighwayPos(cornerx, cornery, onSurface, null, null);
    }

    @Nullable
    public static final HighwayPos getHighwayPos(int cornerx, int cornery, boolean onSurface, int heightOffset) {
        BridgePart bridgePart;
        if (heightOffset == 0) {
            return MethodsHighways.getHighwayPos(cornerx, cornery, onSurface);
        }
        Floor[] floors = Zones.getFloorsAtTile(cornerx, cornery, heightOffset, heightOffset, onSurface);
        if (floors != null && floors.length == 1) {
            return MethodsHighways.getHighwayPos(floors[0]);
        }
        if (heightOffset > 0 && (bridgePart = Zones.getBridgePartFor(cornerx, cornery, onSurface)) != null) {
            return MethodsHighways.getHighwayPos(bridgePart);
        }
        return null;
    }

    @Nullable
    public static final HighwayPos getHighwayPos(Creature creature) {
        Floor floor;
        BridgePart bridgePart;
        int tilex = creature.getTileX();
        int tiley = creature.getTileY();
        boolean onSurface = creature.isOnSurface();
        if (creature.getBridgeId() != -10L && (bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface)) != null) {
            return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
        }
        if (creature.getFloorLevel() > 0 && (floor = Zones.getFloor(tilex, tiley, onSurface, creature.getFloorLevel())) != null) {
            return new HighwayPos(tilex, tiley, onSurface, null, floor);
        }
        return new HighwayPos(tilex, tiley, onSurface, null, null);
    }

    @Nullable
    public static final HighwayPos getNewHighwayPosCorner(Creature performer, int currentTilex, int currentTiley, boolean onSurface, @Nullable BridgePart currentBridgePart, @Nullable Floor currentFloor) {
        Vector2f pos = performer.getPos2f();
        int posTilex = CoordUtils.WorldToTile(pos.x + 2.0f);
        int posTiley = CoordUtils.WorldToTile(pos.y + 2.0f);
        if (posTilex == currentTilex && posTiley == currentTiley) {
            return new HighwayPos(currentTilex, currentTiley, onSurface, currentBridgePart, currentFloor);
        }
        int fromdir = 0;
        if (posTilex == currentTilex && posTiley < currentTiley) {
            fromdir = 1;
        } else if (posTilex > currentTilex && posTiley < currentTiley) {
            fromdir = 2;
        } else if (posTilex > currentTilex && posTiley == currentTiley) {
            fromdir = 4;
        } else if (posTilex > currentTilex && posTiley > currentTiley) {
            fromdir = 8;
        } else if (posTilex == currentTilex && posTiley > currentTiley) {
            fromdir = 16;
        } else if (posTilex < currentTilex && posTiley > currentTiley) {
            fromdir = 32;
        } else if (posTilex < currentTilex && posTiley == currentTiley) {
            fromdir = 64;
        } else if (posTilex < currentTilex && posTiley < currentTiley) {
            fromdir = -128;
        }
        if (currentBridgePart != null) {
            return MethodsHighways.getNewHighwayPosFromBridge(posTilex, posTiley, onSurface, currentBridgePart, (byte)fromdir);
        }
        if (currentFloor != null) {
            return MethodsHighways.getNewHighwayPosFromFloor(posTilex, posTiley, onSurface, currentFloor, (byte)fromdir);
        }
        if (onSurface) {
            int encodedtile = Server.surfaceMesh.getTile(posTilex, posTiley);
            byte type = Tiles.decodeType(encodedtile);
            if (type == Tiles.Tile.TILE_HOLE.id) {
                return new HighwayPos(posTilex, posTiley, false, null, null);
            }
            BridgePart bridgePart = Zones.getBridgePartFor(posTilex, posTiley, onSurface);
            if (bridgePart != null) {
                if (bridgePart.getSouthExit() == 0 && (fromdir == -128 || fromdir == 1 || fromdir == 2)) {
                    return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getWestExit() == 0 && (fromdir == 2 || fromdir == 4 || fromdir == 8)) {
                    return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getNorthExit() == 0 && (fromdir == 8 || fromdir == 16 || fromdir == 32)) {
                    return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                }
                if (bridgePart.getEastExit() == 0 && (fromdir == 32 || fromdir == 64 || fromdir == -128)) {
                    return new HighwayPos(posTilex, posTiley, onSurface, bridgePart, null);
                }
            }
        } else {
            int encodedtile = Server.caveMesh.getTile(posTilex, posTiley);
            byte type = Tiles.decodeType(encodedtile);
            if (Tiles.isSolidCave(type)) {
                return new HighwayPos(posTilex, posTiley, true, null, null);
            }
        }
        return new HighwayPos(posTilex, posTiley, onSurface, null, null);
    }

    @Nullable
    private static final HighwayPos getNewHighwayPosFromBridge(int tilex, int tiley, boolean onSurface, BridgePart currentBridgePart, byte fromdir) {
        if (currentBridgePart.hasNorthExit() && (fromdir == -128 || fromdir == 1 || fromdir == 2)) {
            if (currentBridgePart.hasHouseNorthExit()) {
                Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getNorthExitFloorLevel());
                if (floor == null) {
                    return null;
                }
                return new HighwayPos(tilex, tiley, onSurface, null, floor);
            }
            return new HighwayPos(tilex, tiley, onSurface, null, null);
        }
        if (currentBridgePart.hasEastExit() && (fromdir == 2 || fromdir == 4 || fromdir == 32 || fromdir == 2)) {
            if (currentBridgePart.hasHouseEastExit()) {
                Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getEastExitFloorLevel());
                if (floor == null) {
                    return null;
                }
                return new HighwayPos(tilex, tiley, onSurface, null, floor);
            }
            return new HighwayPos(tilex, tiley, onSurface, null, null);
        }
        if (currentBridgePart.hasSouthExit() && (fromdir == 8 || fromdir == 16 || fromdir == 32)) {
            if (currentBridgePart.hasHouseSouthExit()) {
                Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getSouthExitFloorLevel());
                if (floor == null) {
                    return null;
                }
                return new HighwayPos(tilex, tiley, onSurface, null, floor);
            }
            return new HighwayPos(tilex, tiley, onSurface, null, null);
        }
        if (currentBridgePart.hasWestExit() && (fromdir == 32 || fromdir == 64 || fromdir == -128 || fromdir == 2)) {
            if (currentBridgePart.hasHouseWestExit()) {
                Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentBridgePart.getWestExitFloorLevel());
                if (floor == null) {
                    return null;
                }
                return new HighwayPos(tilex, tiley, onSurface, null, floor);
            }
            return new HighwayPos(tilex, tiley, onSurface, null, null);
        }
        BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
        if (bridgePart != null) {
            return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
        }
        return null;
    }

    @Nullable
    private static final HighwayPos getNewHighwayPosFromFloor(int tilex, int tiley, boolean onSurface, Floor currentFloor, byte fromdir) {
        Floor floor = Zones.getFloor(tilex, tiley, onSurface, currentFloor.getFloorLevel());
        if (floor != null) {
            return new HighwayPos(tilex, tiley, onSurface, null, floor);
        }
        BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
        if (bridgePart != null) {
            if (bridgePart.getSouthExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == -128 || fromdir == 1 || fromdir == 2)) {
                return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }
            if (bridgePart.getWestExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 2 || fromdir == 4 || fromdir == 8)) {
                return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }
            if (bridgePart.getNorthExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 8 || fromdir == 16 || fromdir == 32)) {
                return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }
            if (bridgePart.getEastExitFloorLevel() == currentFloor.getFloorLevel() && (fromdir == 32 || fromdir == 64 || fromdir == -128)) {
                return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
            }
        }
        return null;
    }

    @Nullable
    private static final HighwayPos getHighwayPosFromMarker(Item marker) {
        int tilex = marker.getTileX();
        int tiley = marker.getTileY();
        boolean onSurface = marker.isOnSurface();
        if (marker.getBridgeId() != -10L) {
            BridgePart bridgePart = Zones.getBridgePartFor(tilex, tiley, onSurface);
            return new HighwayPos(tilex, tiley, onSurface, bridgePart, null);
        }
        if (marker.getFloorLevel() > 0) {
            Floor floor = Zones.getFloor(tilex, tiley, marker.isOnSurface(), marker.getFloorLevel());
            return new HighwayPos(marker.getTileX(), marker.getTileY(), marker.isOnSurface(), null, floor);
        }
        return new HighwayPos(marker.getTileX(), marker.getTileY(), marker.isOnSurface(), null, null);
    }

    public static final String getLinkDirString(byte linkdir) {
        switch (linkdir) {
            case 1: {
                return "north";
            }
            case 2: {
                return "northeast";
            }
            case 4: {
                return "east";
            }
            case 8: {
                return "southeast";
            }
            case 16: {
                return "south";
            }
            case 32: {
                return "southwest";
            }
            case 64: {
                return "west";
            }
            case -128: {
                return "northwest";
            }
        }
        return "unknown(" + linkdir + ")";
    }

    public static final boolean canPlantMarker(@Nullable Creature performer, HighwayPos currentHighwayPos, Item marker, byte possibleLinks) {
        int cornerX = currentHighwayPos.getTilex();
        int cornerY = currentHighwayPos.getTiley();
        Village village = Villages.getVillagePlus(cornerX, cornerY, true, 2);
        int pcount = MethodsHighways.numberOfSetBits(possibleLinks);
        if (marker.getTemplateId() == 1112) {
            if (pcount == 0 && village == null) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Can only plant if there is an adjacent marker.");
                }
                return false;
            }
        } else {
            if (pcount == 0) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Can only plant if there is an adjacent marker.");
                }
                return false;
            }
            if (pcount > 2) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("Catseyes can only be planted if there is a maximum of two possible links.");
                }
                return false;
            }
        }
        if (performer != null) {
            Skill skill;
            if (village != null) {
                if (!village.isActionAllowed((short)176, performer)) {
                    performer.getCommunicator().sendNormalServerMessage("You do not have permission to plant a " + marker.getName() + " on (or next to) \"" + village.getName() + "\".");
                    return false;
                }
                if (!village.isHighwayAllowed()) {
                    performer.getCommunicator().sendNormalServerMessage("\"" + village.getName() + "\" does not allow highways.");
                    return false;
                }
                if (village.getReputations().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("You cannot plant a " + marker.getName() + " on (or next to) \"" + village.getName() + "\" as it has an active kos list.");
                    return false;
                }
            }
            if ((skill = performer.getSkills().getSkillOrLearn(10031)).getRealKnowledge() < 20.1) {
                performer.getCommunicator().sendNormalServerMessage("You do not have enough skill to plant that.");
                return false;
            }
            if (!performer.isPaying()) {
                performer.getCommunicator().sendNormalServerMessage("You need to be premium to plant that.");
                return false;
            }
            if (MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("This area is too sloped to allow highway markers.");
                }
                return false;
            }
            HighwayPos highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)1);
            if (highwayPos != null && MethodsHighways.checkSlopes(highwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("North tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)2);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("North East tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)4);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("East tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)8);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("South East tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)16);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("South tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)32);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("South West tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)64);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("West tile is too sloped to allow highway markers.");
                }
                return false;
            }
            highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)-128);
            if (highwayPos != null && MethodsHighways.checkSlopes(currentHighwayPos)) {
                if (performer != null) {
                    performer.getCommunicator().sendNormalServerMessage("North West tile is too sloped to allow highway markers.");
                }
                return false;
            }
        }
        return true;
    }

    static boolean checkSlopes(HighwayPos highwayPos) {
        MeshTile meshTile;
        if (highwayPos.isSurfaceTile() && Tiles.isRoadType((meshTile = new MeshTile(Server.surfaceMesh, highwayPos.getTilex(), highwayPos.getTiley())).getTileType()) && meshTile.checkSlopes(20, 28)) {
            return true;
        }
        return highwayPos.isCaveTile() && Tiles.isRoadType((meshTile = new MeshTile(Server.caveMesh, highwayPos.getTilex(), highwayPos.getTiley())).getTileType()) && meshTile.checkSlopes(20, 28);
    }

    public static final void removeNearbyMarkers(Floor floor) {
        HighwayPos highwayPos = new HighwayPos(floor.getTileX(), floor.getTileY(), floor.isOnSurface(), null, floor);
        MethodsHighways.removeNearbyMarkers(highwayPos);
    }

    public static final void removeNearbyMarkers(BridgePart bridgePart) {
        HighwayPos highwayPos = new HighwayPos(bridgePart.getTileX(), bridgePart.getTileY(), bridgePart.isOnSurface(), bridgePart, null);
        MethodsHighways.removeNearbyMarkers(highwayPos);
    }

    public static final void removeNearbyMarkers(int tilex, int tiley, boolean onSurface) {
        HighwayPos highwayPos = new HighwayPos(tilex, tiley, onSurface, null, null);
        MethodsHighways.removeNearbyMarkers(highwayPos);
    }

    private static final void removeNearbyMarkers(HighwayPos highwayPos) {
        Item marker = MethodsHighways.getMarker(highwayPos);
        if (marker != null) {
            marker.setDamage(100.0f);
        }
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)1);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)2);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)4);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)8);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)16);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)32);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)64);
        MethodsHighways.removeNearbyMarker(highwayPos, (byte)-128);
    }

    private static final void removeNearbyMarker(HighwayPos currentHighwayPos, byte linkdir) {
        Item marker;
        HighwayPos highwayPos = MethodsHighways.getNewHighwayPosLinked(currentHighwayPos, (byte)1);
        if (highwayPos != null && (marker = MethodsHighways.getMarker(highwayPos)) != null) {
            if (currentHighwayPos.getBridgeId() != -10L || currentHighwayPos.getFloorLevel() != 0) {
                if (marker.getBridgeId() != -10L || marker.getFloorLevel() != 0) {
                    marker.setDamage(100.0f);
                }
            } else if (marker.getBridgeId() == -10L || marker.getFloorLevel() == 0) {
                marker.setDamage(100.0f);
            }
        }
    }

    public static final byte convertLink(byte link) {
        switch (link) {
            case 1: {
                return 0;
            }
            case 2: {
                return 1;
            }
            case 4: {
                return 2;
            }
            case 8: {
                return 3;
            }
            case 16: {
                return 4;
            }
            case 32: {
                return 5;
            }
            case 64: {
                return 6;
            }
            case -128: {
                return 7;
            }
        }
        return -1;
    }

    public static final byte getOppositedir(byte fromdir) {
        int lr4 = (fromdir & 0xFF) >>> 4;
        int ll4 = (fromdir & 0xFF) << 4;
        int lc4 = lr4 | ll4;
        byte oppositedir = (byte)(lc4 & 0xFF);
        return oppositedir;
    }

    public static final byte getOtherdir(byte dirs, byte fromdir) {
        byte otherdir = (byte)(dirs & ~fromdir);
        return otherdir;
    }

    public static final boolean isNextToACamp(HighwayPos currentHighwayPos) {
        Item marker = MethodsHighways.getMarker(currentHighwayPos, (byte)1);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)2);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)4);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)8);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)16);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)32);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)64);
        if (marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L) {
            return true;
        }
        marker = MethodsHighways.getMarker(currentHighwayPos, (byte)-128);
        return marker != null && marker.getTemplateId() == 1112 && marker.getData() != -1L;
    }

    public static final int numberOfSetBits(byte b) {
        return Integer.bitCount(b & 0xFF);
    }
}

