package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;

public class LightOfFo extends ReligiousSpell {
   public static final int RANGE = 4;

   public LightOfFo() {
      super("Light of Fo", 438, 15, 60, 40, 33, 120000L);
      this.targetTile = true;
      this.healing = true;
      this.description = "covers an area with healing energy, healing multiple wounds from allies";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator().sendNormalServerMessage("You place the Mark of Fo in the area, declaring a sanctuary.");
      int sx = Zones.safeTileX(tilex - (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
      int sy = Zones.safeTileY(tiley - (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
      int ex = Zones.safeTileX(tilex + (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
      int ey = Zones.safeTileY(tiley + (int)Math.max(1.0, power / 10.0 + (double)performer.getNumLinks()));
      int totalHealed = 0;

      for(int x = sx; x <= ex; ++x) {
         for(int y = sy; y <= ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
            if (t != null) {
               for(Creature lCret : t.getCreatures()) {
                  boolean doHeal = false;
                  if (lCret.getKingdomId() == performer.getKingdomId() || lCret.getAttitude(performer) == 1) {
                     doHeal = true;
                  }

                  Village lVill = lCret.getCitizenVillage();
                  if (lVill != null && lVill.isEnemy(performer)) {
                     doHeal = false;
                  }

                  Village pVill = performer.getCitizenVillage();
                  if (pVill != null && pVill.isEnemy(lCret)) {
                     doHeal = false;
                  }

                  if (doHeal && lCret.getBody() != null && lCret.getBody().getWounds() != null) {
                     Wounds tWounds = lCret.getBody().getWounds();
                     double healingPool = 16375.0;
                     healingPool += 98250.0 * (power / 100.0);
                     if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
                        healingPool *= 2.0;
                     }

                     double resistance = SpellResist.getSpellResistance(lCret, this.getNumber());
                     healingPool *= resistance;
                     int woundsHealed = 0;
                     int maxWoundHeal = (int)(healingPool * 0.2);

                     for(Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 5) {
                           break;
                        }

                        if (!(w.getSeverity() < (float)maxWoundHeal)) {
                           healingPool -= (double)maxWoundHeal;
                           SpellResist.addSpellResistance(lCret, this.getNumber(), (double)maxWoundHeal);
                           w.modifySeverity(-maxWoundHeal);
                           ++woundsHealed;
                        }
                     }

                     while(woundsHealed < 5 && tWounds.getWounds().length > 0) {
                        Wound targetWound = tWounds.getWounds()[0];

                        for(Wound w : tWounds.getWounds()) {
                           if (w.getSeverity() > targetWound.getSeverity()) {
                              targetWound = w;
                           }
                        }

                        SpellResist.addSpellResistance(lCret, 249, (double)targetWound.getSeverity());
                        targetWound.heal();
                        ++woundsHealed;
                     }

                     if (woundsHealed < 5) {
                        for(Wound w : tWounds.getWounds()) {
                           if (woundsHealed >= 5) {
                              break;
                           }

                           if (w.getSeverity() <= (float)maxWoundHeal) {
                              SpellResist.addSpellResistance(lCret, this.getNumber(), (double)w.getSeverity());
                              w.heal();
                              ++woundsHealed;
                           } else {
                              SpellResist.addSpellResistance(lCret, this.getNumber(), (double)maxWoundHeal);
                              w.modifySeverity(-maxWoundHeal);
                              ++woundsHealed;
                           }
                        }
                     }

                     VolaTile tt = Zones.getTileOrNull(lCret.getTileX(), lCret.getTileY(), lCret.isOnSurface());
                     if (tt != null) {
                        tt.sendAttachCreatureEffect(lCret, (byte)11, (byte)0, (byte)0, (byte)0, (byte)0);
                     }

                     ++totalHealed;
                     String heal = performer == lCret ? "heal" : "heals";
                     ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                     segments.add(new CreatureLineSegment(performer));
                     segments.add(new MulticolorLineSegment(" " + heal + " some of your wounds with " + this.getName() + ".", (byte)0));
                     lCret.getCommunicator().sendColoredMessageCombat(segments);
                  }
               }
            }
         }
      }
   }
}
