/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.KarmaSpell;
import com.wurmonline.server.zones.Trap;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;

public class Forecast
extends KarmaSpell {
    public static final int RANGE = 24;

    public Forecast() {
        super("Forecast", 560, 60, 300, 30, 1, 180000L);
        this.offensive = true;
        this.targetCreature = true;
        this.targetTile = true;
        this.description = "predicts the future for an area by placing deadly traps around it";
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, Creature target) {
        int x;
        int y;
        int y2;
        int x2;
        int villid = 0;
        if (performer.getCitizenVillage() != null) {
            villid = performer.getCitizenVillage().id;
        }
        int layer = target.getLayer();
        for (x2 = Zones.safeTileX(target.getTileX() - 5); x2 < Zones.safeTileX(target.getTileX() + 5); ++x2) {
            y2 = Zones.safeTileY(target.getTileY() - 5);
            this.createTrapAt(x2, y2, layer, villid, performer.getKingdomId());
        }
        for (x2 = Zones.safeTileX(target.getTileX() - 5); x2 < Zones.safeTileX(target.getTileX() + 5); ++x2) {
            y2 = Zones.safeTileY(target.getTileY() + 5);
            this.createTrapAt(x2, y2, layer, villid, performer.getKingdomId());
        }
        for (y = Zones.safeTileY(target.getTileY() - 5); y < Zones.safeTileY(target.getTileY() + 5); ++y) {
            x = Zones.safeTileX(target.getTileX() - 5);
            this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
        }
        for (y = Zones.safeTileY(target.getTileY() - 5); y < Zones.safeTileY(target.getTileY() + 5); ++y) {
            x = Zones.safeTileX(target.getTileX() + 5);
            this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
        }
        performer.getCommunicator().sendNormalServerMessage("You predict a grim future for " + target.getNameWithGenus() + " by placing deadly traps around " + target.getHimHerItString() + ".");
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        int x;
        int y;
        int y2;
        int x2;
        int villid = 0;
        if (performer.getCitizenVillage() != null) {
            villid = performer.getCitizenVillage().id;
        }
        for (x2 = Zones.safeTileX(tilex - 5); x2 < Zones.safeTileX(tilex + 5); ++x2) {
            y2 = Zones.safeTileY(tiley - 5);
            this.createTrapAt(x2, y2, layer, villid, performer.getKingdomId());
        }
        for (x2 = Zones.safeTileX(tilex - 5); x2 < Zones.safeTileX(tilex + 5); ++x2) {
            y2 = Zones.safeTileY(tiley + 5);
            this.createTrapAt(x2, y2, layer, villid, performer.getKingdomId());
        }
        for (y = Zones.safeTileY(tiley - 4); y < Zones.safeTileY(tiley + 4); ++y) {
            x = Zones.safeTileX(tilex - 5);
            this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
        }
        for (y = Zones.safeTileY(tiley - 4); y < Zones.safeTileY(tiley + 4); ++y) {
            x = Zones.safeTileX(tilex + 5);
            this.createTrapAt(x, y, layer, villid, performer.getKingdomId());
        }
        performer.getCommunicator().sendNormalServerMessage("You predict a grim future for enemies near the area by placing deadly traps around it.");
    }

    private final void createTrapAt(int x, int y, int layer, int villid, byte creatorKingdom) {
        int t;
        boolean ok = true;
        if (layer < 0) {
            t = Server.caveMesh.getTile(x, y);
            if (Tiles.isSolidCave(Tiles.decodeType(t))) {
                ok = false;
            }
            if (Tiles.decodeHeight(t) < 0) {
                ok = false;
            }
        } else {
            t = Server.surfaceMesh.getTile(x, y);
            if (Tiles.decodeHeight(t) < 0) {
                ok = false;
            }
        }
        if (ok) {
            try {
                VolaTile ttile = Zones.getOrCreateTile(x, y, layer >= 0);
                if (ttile != null) {
                    int fl = ttile.getDropFloorLevel(layer * 30);
                    layer = fl / 30;
                    ttile.sendAddQuickTileEffect((byte)71, layer * 30);
                }
                Trap t2 = new Trap(10, 99, creatorKingdom, villid, Trap.createId(x, y, layer), 100, 100, 100);
                t2.create();
            }
            catch (IOException iox) {
                return;
            }
        }
    }
}

