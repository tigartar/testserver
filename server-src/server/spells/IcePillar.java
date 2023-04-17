/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.AreaSpellEffect;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import java.util.logging.Logger;

public class IcePillar
extends ReligiousSpell
implements AttitudeConstants {
    private static Logger logger = Logger.getLogger(IcePillar.class.getName());
    public static final int RANGE = 24;
    public static final double BASE_DAMAGE = 150.0;
    public static final double DAMAGE_PER_SECOND = 4.0;
    public static final int RADIUS = 2;

    public IcePillar() {
        super("Ice Pillar", 414, 10, 30, 10, 35, 120000L);
        this.targetTile = true;
        this.offensive = true;
        this.description = "covers an area with frost dealing damage to enemies over time";
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
        if (layer < 0) {
            tile = Server.caveMesh.getTile(tilex, tiley);
        }
        if (Tiles.isSolidCave(type = Tiles.decodeType(tile))) {
            performer.getCommunicator().sendNormalServerMessage("You fail to find a spot to direct the power to.", (byte)3);
            return;
        }
        performer.getCommunicator().sendNormalServerMessage("You freeze the air around the " + Tiles.getTile((byte)Tiles.decodeType((int)tile)).tiledesc.toLowerCase() + ".");
        Structure currstr = performer.getCurrentTile().getStructure();
        int sx = Zones.safeTileX(tilex - 2 - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 2 + performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 2 - performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 2 + performer.getNumLinks());
        this.calculateArea(sx, sy, ex, ey, tilex, tiley, layer, currstr);
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                if (tilex == x && y == tiley) {
                    new AreaSpellEffect(performer.getWurmId(), x, y, layer, 36, System.currentTimeMillis() + 1000L * (long)(30 + (int)power / 10), (float)power * 2.0f, layer, 0, true);
                    if (!((double)Server.rand.nextInt(1000) < power) || layer < 0 || !Tiles.canSpawnTree(type) && !Tiles.isEnchanted(type)) continue;
                    Server.setSurfaceTile(x, y, Tiles.decodeHeight(tile), Tiles.Tile.TILE_TUNDRA.id, (byte)0);
                    Players.getInstance().sendChangedTile(x, y, true, true);
                    continue;
                }
                int currAreaX = x - sx;
                int currAreaY = y - sy;
                if (this.area[currAreaX][currAreaY]) continue;
                new AreaSpellEffect(performer.getWurmId(), x, y, layer, 53, System.currentTimeMillis() + 1000L * (long)(30 + (int)power / 10), (float)power * 2.0f, 0, 0, true);
            }
        }
    }
}

