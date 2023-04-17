/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.ReligiousSpell;
import com.wurmonline.server.spells.SpellResist;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;

public class LightOfFo
extends ReligiousSpell {
    public static final int RANGE = 4;

    public LightOfFo() {
        super("Light of Fo", 438, 15, 60, 40, 33, 120000L);
        this.targetTile = true;
        this.healing = true;
        this.description = "covers an area with healing energy, healing multiple wounds from allies";
        this.type = (byte)2;
    }

    @Override
    void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
        performer.getCommunicator().sendNormalServerMessage("You place the Mark of Fo in the area, declaring a sanctuary.");
        int sx = Zones.safeTileX(tilex - (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
        int sy = Zones.safeTileY(tiley - (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
        int ex = Zones.safeTileX(tilex + (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
        int ey = Zones.safeTileY(tiley + (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
        int totalHealed = 0;
        for (int x = sx; x <= ex; ++x) {
            for (int y = sy; y <= ey; ++y) {
                VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
                if (t == null) continue;
                for (Creature lCret : t.getCreatures()) {
                    VolaTile tt;
                    Village pVill;
                    Village lVill;
                    boolean doHeal = false;
                    if (lCret.getKingdomId() == performer.getKingdomId() || lCret.getAttitude(performer) == 1) {
                        doHeal = true;
                    }
                    if ((lVill = lCret.getCitizenVillage()) != null && lVill.isEnemy(performer)) {
                        doHeal = false;
                    }
                    if ((pVill = performer.getCitizenVillage()) != null && pVill.isEnemy(lCret)) {
                        doHeal = false;
                    }
                    if (!doHeal || lCret.getBody() == null || lCret.getBody().getWounds() == null) continue;
                    Wounds tWounds = lCret.getBody().getWounds();
                    double healingPool = 16375.0;
                    healingPool += 98250.0 * (power / 100.0);
                    if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
                        healingPool *= 2.0;
                    }
                    double resistance = SpellResist.getSpellResistance(lCret, this.getNumber());
                    int woundsHealed = 0;
                    int maxWoundHeal = (int)((healingPool *= resistance) * 0.2);
                    for (Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 5) break;
                        if (w.getSeverity() < (float)maxWoundHeal) continue;
                        healingPool -= (double)maxWoundHeal;
                        SpellResist.addSpellResistance(lCret, this.getNumber(), maxWoundHeal);
                        w.modifySeverity(-maxWoundHeal);
                        ++woundsHealed;
                    }
                    while (woundsHealed < 5 && tWounds.getWounds().length > 0) {
                        Object targetWound = tWounds.getWounds()[0];
                        Wound[] woundArray = tWounds.getWounds();
                        int n = woundArray.length;
                        for (int w = 0; w < n; ++w) {
                            Wound w2 = woundArray[w];
                            if (!(w2.getSeverity() > ((Wound)targetWound).getSeverity())) continue;
                            targetWound = w2;
                        }
                        SpellResist.addSpellResistance(lCret, 249, ((Wound)targetWound).getSeverity());
                        ((Wound)targetWound).heal();
                        ++woundsHealed;
                    }
                    if (woundsHealed < 5) {
                        for (Wound w : tWounds.getWounds()) {
                            if (woundsHealed >= 5) break;
                            if (w.getSeverity() <= (float)maxWoundHeal) {
                                SpellResist.addSpellResistance(lCret, this.getNumber(), w.getSeverity());
                                w.heal();
                                ++woundsHealed;
                                continue;
                            }
                            SpellResist.addSpellResistance(lCret, this.getNumber(), maxWoundHeal);
                            w.modifySeverity(-maxWoundHeal);
                            ++woundsHealed;
                        }
                    }
                    if ((tt = Zones.getTileOrNull(lCret.getTileX(), lCret.getTileY(), lCret.isOnSurface())) != null) {
                        tt.sendAttachCreatureEffect(lCret, (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
                    }
                    ++totalHealed;
                    String heal = performer == lCret ? "heal" : "heals";
                    ArrayList<MulticolorLineSegment> segments = new ArrayList<MulticolorLineSegment>();
                    segments.add(new CreatureLineSegment(performer));
                    segments.add(new MulticolorLineSegment(" " + heal + " some of your wounds with " + this.getName() + ".", 0));
                    lCret.getCommunicator().sendColoredMessageCombat(segments);
                }
            }
        }
    }
}

