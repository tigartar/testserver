/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.KarmaSpell;
import com.wurmonline.server.structures.DbFence;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.StructureConstantsEnum;

public class WallOfIce
extends KarmaSpell {
    public static final int RANGE = 24;

    public WallOfIce() {
        super("Wall of Ice", 556, 10, 400, 10, 1, 0L);
        this.targetTileBorder = true;
        this.offensive = true;
        this.description = "creates a magical wall of ice on a tile border";
    }

    /*
     * WARNING - void declaration
     */
    @Override
    boolean precondition(Skill castSkill, Creature performer, int tileBorderx, int tileBordery, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
        block14: {
            Creature c;
            Object t2;
            VolaTile t1;
            int n;
            block13: {
                void var13_23;
                VolaTile t = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
                if (t != null) {
                    void var13_20;
                    CounterTypes[] fences;
                    Wall[] walls;
                    for (Wall wall : walls = t.getWallsForLevel(heightOffset / 30)) {
                        if (wall.isHorizontal() != (dir == Tiles.TileBorderDirection.DIR_HORIZ) || wall.getStartX() != tileBorderx || wall.getStartY() != tileBordery) continue;
                        return false;
                    }
                    CounterTypes[] counterTypesArray = fences = t.getFencesForDir(dir);
                    n = counterTypesArray.length;
                    boolean bl = false;
                    while (var13_20 < n) {
                        CounterTypes f = counterTypesArray[var13_20];
                        if (((Fence)f).getHeightOffset() == heightOffset) {
                            return false;
                        }
                        ++var13_20;
                    }
                }
                if (dir != Tiles.TileBorderDirection.DIR_DOWN) break block13;
                t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
                if (t1 != null) {
                    for (CounterTypes counterTypes : t1.getCreatures()) {
                        if (!((Creature)counterTypes).isPlayer()) continue;
                        return false;
                    }
                }
                if ((t2 = Zones.getTileOrNull(tileBorderx - 1, tileBordery, layer == 0)) == null) break block14;
                Creature[] creatureArray = ((VolaTile)t2).getCreatures();
                n = creatureArray.length;
                boolean bl = false;
                while (var13_23 < n) {
                    c = creatureArray[var13_23];
                    if (c.isPlayer()) {
                        return false;
                    }
                    ++var13_23;
                }
                break block14;
            }
            t1 = Zones.getTileOrNull(tileBorderx, tileBordery, layer == 0);
            if (t1 != null) {
                t2 = t1.getCreatures();
                int n2 = ((Creature[])t2).length;
                for (n = 0; n < n2; ++n) {
                    Object object = t2[n];
                    if (!((Creature)object).isPlayer()) continue;
                    return false;
                }
            }
            if ((t2 = Zones.getTileOrNull(tileBorderx, tileBordery - 1, layer == 0)) != null) {
                void var13_26;
                Creature[] creatureArray = ((VolaTile)t2).getCreatures();
                n = creatureArray.length;
                boolean bl = false;
                while (var13_26 < n) {
                    c = creatureArray[var13_26];
                    if (c.isPlayer()) {
                        return false;
                    }
                    ++var13_26;
                }
            }
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset, Tiles.TileBorderDirection dir) {
        SoundPlayer.playSound("sound.religion.channel", tilex, tiley, performer.isOnSurface(), 0.0f);
        try {
            Zone zone = Zones.getZone(tilex, tiley, true);
            DbFence fence = new DbFence(StructureConstantsEnum.FENCE_MAGIC_ICE, tilex, tiley, heightOffset, (float)(1.0 + power / 5.0), dir, zone.getId(), layer);
            fence.setState(fence.getFinishState());
            ((Fence)fence).setQualityLevel((float)power);
            ((Fence)fence).improveOrigQualityLevel((float)power);
            zone.addFence(fence);
            performer.achievement(320);
            performer.getCommunicator().sendNormalServerMessage("You weave the source and create a wall.");
            Server.getInstance().broadCastAction(performer.getName() + " creates a wall.", performer, 5);
        }
        catch (NoSuchZoneException noSuchZoneException) {
            // empty catch block
        }
    }
}

