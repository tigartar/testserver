/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.Zones;

public class DeepTentacles
extends ReligiousSpell {
    public static final int RANGE = 24;
    public static final double BASE_DAMAGE = 400.0;
    public static final double DAMAGE_PER_SECOND = 1.0;
    public static final int RADIUS = 1;

    public DeepTentacles() {
        super("Tentacles", 418, 10, 30, 20, 33, 120000L);
        this.targetTile = true;
        this.offensive = true;
        this.description = "covers an area with bludgeoning tentacles that damage enemies over time";
        this.type = (byte)2;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        int tile;
        if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(tile = Server.caveMesh.getTile(tilex, tiley)))) {
            performer.getCommunicator().sendNormalServerMessage("The spell doesn't work there.", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        byte type;
        int tile = Server.surfaceMesh.getTile(tilex, tiley);
        if (layer < 0 && Tiles.isSolidCave(type = Tiles.decodeType(tile = Server.caveMesh.getTile(tilex, tiley)))) {
            performer.getCommunicator().sendNormalServerMessage("You fail to find a spot to direct the power to.", (byte)3);
            return;
        }
        Structure currstr = performer.getCurrentTile().getStructure();
        performer.getCommunicator().sendNormalServerMessage("Waving tentacles appear around the " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.toLowerCase() + ".");
        int sx = Zones.safeTileX(tilex - 1 - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 1 + performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 1 - performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 1 + performer.getNumLinks());
        this.calculateArea(sx, sy, ex, ey, tilex, tiley, layer, currstr);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                int currAreaX = x - sx;
                int currAreaY = y - sy;
                if (this.area[currAreaX][currAreaY]) continue;
                new AreaSpellEffect(performer.getWurmId(), x, y, layer, 34, System.currentTimeMillis() + 1000L * (long)(30 + (int)power / 10), (float)power * 1.5f, layer, 0, true);
            }
        }
    }
}

