/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Terraforming;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class StrongWall
extends ReligiousSpell {
    public static final int RANGE = 40;

    public StrongWall() {
        super("Strongwall", 440, 180, 70, 70, 70, 0L);
        this.targetTile = true;
        this.description = "causes an open tile underground to collapse, or reinforces an existing wall";
        this.type = 1;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        int tile = Server.caveMesh.getTile(tilex, tiley);
        if (layer < 0 || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(Tiles.decodeType(tile))) {
                int ts;
                byte type;
                HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, false);
                if (highwayPos != null && MethodsHighways.onHighway(highwayPos)) {
                    return false;
                }
                VolaTile t = Zones.getOrCreateTile(tilex, tiley, false);
                if (t.getCreatures().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("That tile is occupied by creatures.", (byte)3);
                    return false;
                }
                if (t.getStructure() != null) {
                    performer.getCommunicator().sendNormalServerMessage("The structure gets in the way.", (byte)3);
                    return false;
                }
                if (t.getItems().length > 0) {
                    performer.getCommunicator().sendNormalServerMessage("You should remove the items first.", (byte)3);
                    return false;
                }
                if (t.getVillage() == null) {
                    for (int x = -1; x <= 1; ++x) {
                        for (int y = -1; y <= 1; ++y) {
                            VolaTile vt;
                            if (x == 0 && y == 0 || (vt = Zones.getTileOrNull(tilex + x, tiley + y, false)) == null || vt.getStructure() == null) continue;
                            performer.getCommunicator().sendNormalServerMessage("The nearby structure gets in the way.", (byte)3);
                            return false;
                        }
                    }
                }
                if (Tiles.isMineDoor(type = Tiles.decodeType(ts = Server.surfaceMesh.getTile(tilex, tiley)))) {
                    performer.getCommunicator().sendNormalServerMessage("You need to destroy the mine door first.", (byte)3);
                    return false;
                }
            } else {
                if (Tiles.isOreCave(Tiles.decodeType(tile))) {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens on the ore.", (byte)3);
                    return false;
                }
                if (Tiles.isReinforcedCave(Tiles.decodeType(tile))) {
                    performer.getCommunicator().sendNormalServerMessage("Nothing happens on the reinforced rock.", (byte)3);
                    return false;
                }
            }
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("This spell works on rock below ground.", (byte)3);
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        int tile = Server.caveMesh.getTile(tilex, tiley);
        byte type = Tiles.decodeType(tile);
        if (Tiles.isSolidCave(type)) {
            if (type == Tiles.Tile.TILE_CAVE_WALL.id) {
                Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id, Tiles.decodeData(tile)));
                Players.getInstance().sendChangedTile(tilex, tiley, false, true);
                performer.getCommunicator().sendNormalServerMessage("You reinforce the rock.", (byte)2);
            } else {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens to the " + Tiles.getTile((byte)type).tiledesc.toLowerCase() + ".", (byte)3);
            }
            return;
        }
        if (Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE_EXIT.id || Tiles.decodeType(tile) == Tiles.Tile.TILE_CAVE.id || Tiles.isReinforcedFloor(Tiles.decodeType(tile))) {
            int ts;
            byte stype;
            HighwayPos highwayPos = MethodsHighways.getHighwayPos(tilex, tiley, false);
            if (highwayPos != null && MethodsHighways.onHighway(highwayPos)) {
                performer.getCommunicator().sendNormalServerMessage("That highway gets in the way.", (byte)3);
                return;
            }
            VolaTile t = Zones.getOrCreateTile(tilex, tiley, false);
            if (t.getStructure() != null) {
                performer.getCommunicator().sendNormalServerMessage("The structure gets in the way.", (byte)3);
                return;
            }
            if (t.getCreatures().length > 0) {
                performer.getCommunicator().sendNormalServerMessage("That tile is occupied by creatures.", (byte)3);
                return;
            }
            if (t.getItems().length > 0) {
                performer.getCommunicator().sendNormalServerMessage("You should remove the items first.", (byte)3);
                return;
            }
            if (t.getVillage() == null) {
                for (int x = -1; x <= 1; ++x) {
                    for (int y = -1; y <= 1; ++y) {
                        VolaTile vt;
                        if (x == 0 && y == 0 || (vt = Zones.getTileOrNull(tilex + x, tiley + y, false)) == null || vt.getStructure() == null) continue;
                        performer.getCommunicator().sendNormalServerMessage("The nearby structure gets in the way.", (byte)3);
                        return;
                    }
                }
            }
            if (Tiles.isMineDoor(stype = Tiles.decodeType(ts = Server.surfaceMesh.getTile(tilex, tiley)))) {
                performer.getCommunicator().sendNormalServerMessage("You need to destroy the mine door first.", (byte)3);
                return;
            }
        }
        Terraforming.setAsRock(tilex, tiley, false);
    }
}

