/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.DamageSpell;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.util.ArrayList;

public class WrathMagranon
extends DamageSpell
implements AttitudeConstants {
    public static final int RANGE = 4;
    public static final double BASE_DAMAGE = 3000.0;
    public static final double DAMAGE_PER_POWER = 60.0;
    public static final float BASE_STRUCTURE_DAMAGE = 7.5f;
    public static final float STRUCTURE_DAMAGE_PER_POWER = 0.15f;
    public static final int RADIUS = 1;

    public WrathMagranon() {
        super("Wrath of Magranon", 441, 10, 50, 50, 50, 300000L);
        this.targetTile = true;
        this.offensive = true;
        this.description = "covers an area with exploding power, damaging enemies and walls";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You slam down the fist of Magranon, which crushes enemy structures in the area!");
        int radiusBonus = (int)(power / 80.0);
        int sx = Zones.safeTileX(tilex - 1 - radiusBonus - performer.getNumLinks());
        int sy = Zones.safeTileY(tiley - 1 - radiusBonus - performer.getNumLinks());
        int ex = Zones.safeTileX(tilex + 1 + radiusBonus + performer.getNumLinks());
        int ey = Zones.safeTileY(tiley + 1 + radiusBonus + performer.getNumLinks());
        float structureDamage = 7.5f + (float)power * 0.15f;
        ArrayList<MiscConstants> damagedFences = new ArrayList<MiscConstants>();
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                Item ring;
                VolaTile t = Zones.getTileOrNull(x, y, layer == 0);
                if (t == null || (ring = Zones.isWithinDuelRing(x, y, layer >= 0)) != null) continue;
                for (Creature creature : t.getCreatures()) {
                    if (creature.isInvulnerable() || creature.getAttitude(performer) != 2) continue;
                    creature.addAttacker(performer);
                    double damage = this.calculateDamage(creature, power, 3000.0, 60.0);
                    creature.addWoundOfType(performer, (byte)0, 1, true, 1.0f, false, damage, (float)power / 5.0f, 0.0f, false, true);
                }
                if (Servers.isThisAPvpServer()) {
                    for (MiscConstants miscConstants : t.getWalls()) {
                        Structure structure;
                        if (((Wall)miscConstants).getType() == StructureTypeEnum.PLAN) continue;
                        boolean dealDam = true;
                        try {
                            structure = Structures.getStructure(((Wall)miscConstants).getStructureId());
                        }
                        catch (NoSuchStructureException nss) {
                            continue;
                        }
                        int tx = ((Wall)miscConstants).getTileX();
                        int ty = ((Wall)miscConstants).getTileY();
                        Village v = Zones.getVillage(tx, ty, performer.isOnSurface());
                        if (v != null && !v.isEnemy(performer) && !MethodsStructure.mayModifyStructure(performer, structure, ((Wall)miscConstants).getTile(), (short)82)) {
                            dealDam = false;
                        }
                        if (!dealDam) continue;
                        float wallql = ((Wall)miscConstants).getCurrentQualityLevel();
                        float damageToDeal = structureDamage * ((150.0f - wallql) / 100.0f);
                        ((Wall)miscConstants).setDamage(((Wall)miscConstants).getDamage() + damageToDeal);
                    }
                }
                for (MiscConstants miscConstants : t.getAllFences()) {
                    if (!((Fence)miscConstants).isFinished() || damagedFences.contains(miscConstants)) continue;
                    boolean dealDam = true;
                    Village vill = MethodsStructure.getVillageForFence((Fence)miscConstants);
                    if (vill != null && !vill.isEnemy(performer)) {
                        dealDam = false;
                    }
                    float mult = 1.0f;
                    if (performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                        mult *= 2.0f;
                    }
                    if (!dealDam) continue;
                    float fenceql = ((Fence)miscConstants).getCurrentQualityLevel();
                    float damageToDeal = structureDamage * ((150.0f - fenceql) / 100.0f);
                    ((Fence)miscConstants).setDamage(((Fence)miscConstants).getDamage() + damageToDeal * mult);
                    damagedFences.add(miscConstants);
                }
            }
        }
        VolaTile t = Zones.getTileOrNull(tilex, tiley, performer.isOnSurface());
        if (t != null && layer == 0) {
            Zones.flash(tilex, tiley, false);
        }
    }
}

