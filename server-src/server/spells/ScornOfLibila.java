/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.DamageSpell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class ScornOfLibila
extends DamageSpell {
    public static final int RANGE = 4;
    public static final double BASE_DAMAGE = 4000.0;
    public static final double DAMAGE_PER_POWER = 40.0;
    public static final int RADIUS = 3;

    public ScornOfLibila() {
        super("Scorn of Libila", 448, 15, 40, 50, 40, 120000L);
        this.targetTile = true;
        this.offensive = true;
        this.healing = true;
        this.description = "covers an area with draining energy, causing internal wounds on enemies and healing allies";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        int y;
        int x;
        performer.getCommunicator().sendNormalServerMessage("You place the Mark of Libila where you stand, declaring a sanctuary.");
        Structure currstr = performer.getCurrentTile().getStructure();
        int radiusBonus = (int)(power / 40.0);
        int sx = Zones.safeTileX(performer.getTileX() - 3 - radiusBonus - performer.getNumLinks());
        int sy = Zones.safeTileY(performer.getTileY() - 3 - radiusBonus - performer.getNumLinks());
        int ex = Zones.safeTileX(performer.getTileX() + 3 + radiusBonus + performer.getNumLinks());
        int ey = Zones.safeTileY(performer.getTileY() + 3 + radiusBonus + performer.getNumLinks());
        this.calculateArea(sx, sy, ex, ey, tilex, tiley, layer, currstr);
        int damdealt = 3;
        int maxRiftPart = 5;
        for (x = sx; x <= ex; ++x) {
            for (y = sy; y <= ey; ++y) {
                Creature[] crets;
                VolaTile t;
                boolean isValidTargetTile = false;
                if (tilex == x && tiley == y) {
                    isValidTargetTile = true;
                } else {
                    int currAreaX = x - sx;
                    int currAreaY = y - sy;
                    if (!this.area[currAreaX][currAreaY]) {
                        isValidTargetTile = true;
                    }
                }
                if (!isValidTargetTile || (t = Zones.getTileOrNull(x, y, performer.isOnSurface())) == null) continue;
                Creature[] creatureArray = crets = t.getCreatures();
                int n = creatureArray.length;
                for (int i = 0; i < n; ++i) {
                    Creature lCret = creatureArray[i];
                    if (lCret.isInvulnerable() || lCret.getAttitude(performer) != 2) continue;
                    t.sendAttachCreatureEffect(lCret, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0);
                    damdealt += 3;
                    double damage = this.calculateDamage(lCret, power, 4000.0, 40.0);
                    if (lCret.addWoundOfType(performer, (byte)9, 1, false, 1.0f, false, damage, 0.0f, 0.0f, false, true)) continue;
                    lCret.setTarget(performer.getWurmId(), false);
                }
            }
        }
        for (x = sx; x <= ex && damdealt > 0; ++x) {
            for (y = sy; y <= ey && damdealt > 0; ++y) {
                Creature[] crets;
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null) continue;
                block5: for (Creature lCret : crets = t.getCreatures()) {
                    if (lCret.getAttitude(performer) != 1 && (lCret.getAttitude(performer) != 0 || lCret.isAggHuman()) && lCret.getKingdomId() != performer.getKingdomId() || lCret.getBody() == null || lCret.getBody().getWounds() == null) continue;
                    Wounds tWounds = lCret.getBody().getWounds();
                    double healingPool = 58950.0;
                    healingPool += 58950.0 * (power / 100.0);
                    if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
                        healingPool *= 2.0;
                    }
                    double resistance = SpellResist.getSpellResistance(lCret, 249);
                    int woundsHealed = 0;
                    int maxWoundHeal = (int)((healingPool *= resistance) * 0.33);
                    for (Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 3 || damdealt <= 0) break;
                        if (w.getSeverity() < (float)maxWoundHeal) continue;
                        healingPool -= (double)maxWoundHeal;
                        SpellResist.addSpellResistance(lCret, 249, maxWoundHeal);
                        w.modifySeverity(-maxWoundHeal);
                        ++woundsHealed;
                        --damdealt;
                    }
                    while (woundsHealed < 3 && damdealt > 0 && tWounds.getWounds().length > 0) {
                        Wound targetWound = tWounds.getWounds()[0];
                        Wound[] woundArray = tWounds.getWounds();
                        int n = woundArray.length;
                        for (int w = 0; w < n; ++w) {
                            Wound w2 = woundArray[w];
                            if (!(w2.getSeverity() > targetWound.getSeverity())) continue;
                            targetWound = w2;
                        }
                        SpellResist.addSpellResistance(lCret, 249, targetWound.getSeverity());
                        targetWound.heal();
                        ++woundsHealed;
                        --damdealt;
                    }
                    if (woundsHealed >= 3 || damdealt <= 0 || tWounds.getWounds().length <= 0) continue;
                    for (Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 3 || damdealt <= 0) continue block5;
                        if (w.getSeverity() <= (float)maxWoundHeal) {
                            SpellResist.addSpellResistance(lCret, 249, w.getSeverity());
                            w.heal();
                            ++woundsHealed;
                            --damdealt;
                            continue;
                        }
                        SpellResist.addSpellResistance(lCret, this.getNumber(), maxWoundHeal);
                        w.modifySeverity(-maxWoundHeal);
                        ++woundsHealed;
                    }
                }
            }
        }
    }
}

