/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class HumidDrizzle
extends ReligiousSpell {
    public static final int RANGE = 4;

    public HumidDrizzle() {
        super("Humid Drizzle", 407, 30, 30, 20, 21, 30000L);
        this.targetTile = true;
        this.description = "tends to animals in area";
        this.type = 1;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You tend to the animals here.", (byte)2);
        int sx = Zones.safeTileX(tilex - 5 - performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 5 - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 5 + performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 5 + performer.getNumLinks());
        for (int x = sx; x < ex; ++x) {
            for (int y = sy; y < ey; ++y) {
                Creature[] crets;
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : crets = t.getCreatures()) {
                    if (lCret.isMonster() || lCret.isPlayer() || !(SpellResist.getSpellResistance(lCret, this.getNumber()) >= 1.0)) continue;
                    lCret.setMilked(false);
                    lCret.setLastGroomed(System.currentTimeMillis());
                    lCret.getBody().healFully();
                    performer.getCommunicator().sendNormalServerMessage(lCret.getNameWithGenus() + " now shines with health.");
                    SpellResist.addSpellResistance(lCret, this.getNumber(), power);
                }
            }
        }
    }
}

