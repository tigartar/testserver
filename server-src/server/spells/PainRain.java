/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.DamageSpell;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;

public class PainRain
extends DamageSpell
implements AttitudeConstants {
    public static final int RANGE = 24;
    public static final double BASE_DAMAGE = 6000.0;
    public static final double DAMAGE_PER_POWER = 40.0;
    public static final int RADIUS = 2;

    public PainRain() {
        super("Pain Rain", 432, 10, 40, 20, 40, 120000L);
        this.targetTile = true;
        this.offensive = true;
        this.description = "covers an area with damaging energy causing infection wounds on enemies";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        Structure currstr = performer.getCurrentTile().getStructure();
        int radiusBonus = (int)(power / 40.0);
        int sx = Zones.safeTileX(tilex - 2 - radiusBonus - performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 2 - radiusBonus - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 2 + radiusBonus + performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 2 + radiusBonus + performer.getNumLinks());
        for (int x = sx; x < ex; ++x) {
            block1: for (int y = sy; y < ey; ++y) {
                Item ring;
                Structure toCheck;
                VolaTile t = Zones.getTileOrNull(x, y, layer == 0);
                if (t == null || currstr != (toCheck = t.getStructure()) || (ring = Zones.isWithinDuelRing(x, y, layer >= 0)) != null) continue;
                Creature[] crets = t.getCreatures();
                int affected = 0;
                for (Creature lCret : crets) {
                    if (!lCret.isInvulnerable() && lCret.getAttitude(performer) == 2) {
                        lCret.addAttacker(performer);
                        double damage = this.calculateDamage(lCret, power, 6000.0, 40.0);
                        lCret.addWoundOfType(performer, (byte)6, 1, true, 1.0f, false, damage, (float)power / 5.0f, 0.0f, false, true);
                        ++affected;
                    }
                    if ((double)affected > power / 10.0 + (double)performer.getNumLinks()) continue block1;
                }
            }
        }
    }
}

