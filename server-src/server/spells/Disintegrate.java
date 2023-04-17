/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.TileRockBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.Villages;

public class Disintegrate
extends ReligiousSpell {
    public static final int RANGE = 4;

    public Disintegrate() {
        super("Disintegrate", 449, 60, 80, 70, 70, 0L);
        this.targetTile = true;
        this.description = "destroys cave walls and reinforcements";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        int tile;
        if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(tile = Server.caveMesh.getTile(tilex, tiley)))) {
            if (Tiles.isReinforcedCave(Tiles.decodeType(tile))) {
                if (Servers.localServer.PVPSERVER) {
                    Village v = Villages.getVillage(tilex, tiley, true);
                    if (v != null) {
                        boolean ok = false;
                        VillageRole r = v.getRoleFor(performer);
                        if (r != null && r.mayMineRock() && r.mayReinforce()) {
                            ok = true;
                        }
                        if (!ok && System.currentTimeMillis() - v.plan.getLastDrained() > 0x6DDD00L) {
                            performer.getCommunicator().sendNormalServerMessage("The settlement has not been drained during the last two hours and the wall still stands this time.", (byte)3);
                            return false;
                        }
                    }
                } else {
                    Village v = Villages.getVillage(tilex, tiley, true);
                    if (v != null) {
                        VillageRole r = v.getRoleFor(performer);
                        return r != null && r.mayMineRock() && r.mayReinforce();
                    }
                }
            }
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("This spell works on rock below ground.");
        return false;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        try {
            Action act = performer.getCurrentAction();
            int dir = (int)(act.getTarget() >> 48) & 0xFF;
            if (dir == 1) {
                performer.getCommunicator().sendNormalServerMessage("The roof just resounds hollowly.", (byte)3);
                return;
            }
            if (dir == 0) {
                performer.getCommunicator().sendNormalServerMessage("The floor just resounds hollowly.", (byte)3);
                return;
            }
            int tile = Server.caveMesh.getTile(tilex, tiley);
            byte type = Tiles.decodeType(tile);
            boolean dis = true;
            if (Tiles.isReinforcedCave(type)) {
                int num = performer.getDeity().isWarrior() ? Math.max(1, 100 - (int)power) : Math.max(1, 200 - 2 * (int)power);
                if (Server.rand.nextInt(Servers.localServer.testServer ? 1 : num) != 0) {
                    performer.getCommunicator().sendNormalServerMessage("You fail to find a weak spot to direct the power to. The wall still stands this time.", (byte)3);
                    dis = false;
                } else {
                    Server.setCaveResource(tilex, tiley, Server.rand.nextInt(100) + 50);
                    Server.caveMesh.setTile(tilex, tiley, Tiles.encode(Tiles.decodeHeight(tile), Tiles.Tile.TILE_CAVE_WALL.id, Tiles.decodeData(tile)));
                    Players.getInstance().sendChangedTile(tilex, tiley, false, false);
                    TileEvent.log(performer.getTileX(), performer.getTileY(), performer.getLayer(), performer.getWurmId(), 449);
                    Tiles.Tile t = Tiles.getTile(type);
                    if (t != null) {
                        logger.info(performer.getName() + " (" + performer.getWurmId() + ") disintegrated a " + t.getName() + " at [" + tilex + "," + tiley + ",false]");
                    }
                    performer.getCommunicator().sendNormalServerMessage("You disintegrate the reinforcement.", (byte)2);
                    return;
                }
            }
            if (dis && TileRockBehaviour.createInsideTunnel(tilex, tiley, tile, performer, 145, dir, true, act)) {
                performer.getCommunicator().sendNormalServerMessage("You disintegrate the " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.toLowerCase() + ".", (byte)2);
            }
        }
        catch (NoSuchActionException nsa) {
            performer.getCommunicator().sendNormalServerMessage("You fail to channel the spell. If this happens regurarly, talk to the gods.");
        }
    }
}

