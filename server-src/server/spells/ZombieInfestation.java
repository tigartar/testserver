/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.zones.Zones;

public class ZombieInfestation
extends ReligiousSpell {
    public static final int RANGE = 50;

    public ZombieInfestation() {
        super("Zombie Infestation", 431, 30, 120, 50, 50, 1800000L);
        this.targetTile = true;
        this.description = "summons your best friends";
        this.type = 1;
    }

    @Override
    boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer) {
        if (!Servers.localServer.PVPSERVER) {
            performer.getCommunicator().sendNormalServerMessage("This spell does not work here.", (byte)3);
            return false;
        }
        return true;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You call for aid from the dead spirits!");
        Server.getInstance().broadCastAction(performer.getName() + " calls for aid from the spirits of the dead.", performer, 10);
        int minx = Zones.safeTileX(tilex - 5);
        int miny = Zones.safeTileY(tiley - 5);
        double maxnums = 1.0f + performer.getFaith() / 5.0f;
        maxnums = maxnums * power / 100.0;
        int nums = 0;
        while ((double)nums < maxnums) {
            int x = Zones.safeTileX(minx + Server.rand.nextInt(10));
            int y = Zones.safeTileY(miny + Server.rand.nextInt(10));
            boolean skip = false;
            if (!performer.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(x, y)))) {
                skip = true;
            }
            if (!skip) {
                try {
                    if (Zones.calculateHeight((x << 2) + 2, (y << 2) + 2, performer.isOnSurface()) > 0.0f) {
                        byte sex = 0;
                        if (Server.rand.nextInt(2) == 0) {
                            sex = 1;
                        }
                        byte ctype = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
                        if (Server.rand.nextInt(20) == 0) {
                            ctype = 99;
                        }
                        Creature.doNew(69, true, (x << 2) + 2, (y << 2) + 2, Server.rand.nextFloat() * 360.0f, performer.getLayer(), "Zombie", sex, performer.getKingdomId(), ctype, true);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            ++nums;
        }
    }
}

