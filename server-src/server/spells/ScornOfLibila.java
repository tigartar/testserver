package com.wurmonline.server.spells;

import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class ScornOfLibila extends DamageSpell {
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
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
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

      for(int x = sx; x <= ex; ++x) {
         for(int y = sy; y <= ey; ++y) {
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

            if (isValidTargetTile) {
               VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
               if (t != null) {
                  Creature[] crets = t.getCreatures();

                  for(Creature lCret : crets) {
                     if (!lCret.isInvulnerable() && lCret.getAttitude(performer) == 2) {
                        t.sendAttachCreatureEffect(lCret, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0);
                        damdealt += 3;
                        double damage = this.calculateDamage(lCret, power, 4000.0, 40.0);
                        if (!lCret.addWoundOfType(performer, (byte)9, 1, false, 1.0F, false, damage, 0.0F, 0.0F, false, true)) {
                           lCret.setTarget(performer.getWurmId(), false);
                        }
                     }
                  }
               }
            }
         }
      }

      for(int x = sx; x <= ex && damdealt > 0; ++x) {
         for(int y = sy; y <= ey && damdealt > 0; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
            if (t != null) {
               Creature[] crets = t.getCreatures();

               for(Creature lCret : crets) {
                  if ((
                        lCret.getAttitude(performer) == 1
                           || lCret.getAttitude(performer) == 0 && !lCret.isAggHuman()
                           || lCret.getKingdomId() == performer.getKingdomId()
                     )
                     && lCret.getBody() != null
                     && lCret.getBody().getWounds() != null) {
                     Wounds tWounds = lCret.getBody().getWounds();
                     double healingPool = 58950.0;
                     healingPool += 58950.0 * (power / 100.0);
                     if (performer.getCultist() != null && performer.getCultist().healsFaster()) {
                        healingPool *= 2.0;
                     }

                     double resistance = SpellResist.getSpellResistance(lCret, 249);
                     healingPool *= resistance;
                     int woundsHealed = 0;
                     int maxWoundHeal = (int)(healingPool * 0.33);

                     for(Wound w : tWounds.getWounds()) {
                        if (woundsHealed >= 3 || damdealt <= 0) {
                           break;
                        }

                        if (!(w.getSeverity() < (float)maxWoundHeal)) {
                           healingPool -= (double)maxWoundHeal;
                           SpellResist.addSpellResistance(lCret, 249, (double)maxWoundHeal);
                           w.modifySeverity(-maxWoundHeal);
                           ++woundsHealed;
                           --damdealt;
                        }
                     }

                     while(woundsHealed < 3 && damdealt > 0 && tWounds.getWounds().length > 0) {
                        Wound targetWound = tWounds.getWounds()[0];

                        for(Wound w : tWounds.getWounds()) {
                           if (w.getSeverity() > targetWound.getSeverity()) {
                              targetWound = w;
                           }
                        }

                        SpellResist.addSpellResistance(lCret, 249, (double)targetWound.getSeverity());
                        targetWound.heal();
                        ++woundsHealed;
                        --damdealt;
                     }

                     if (woundsHealed < 3 && damdealt > 0 && tWounds.getWounds().length > 0) {
                        for(Wound w : tWounds.getWounds()) {
                           if (woundsHealed >= 3 || damdealt <= 0) {
                              break;
                           }

                           if (w.getSeverity() <= (float)maxWoundHeal) {
                              SpellResist.addSpellResistance(lCret, 249, (double)w.getSeverity());
                              w.heal();
                              ++woundsHealed;
                              --damdealt;
                           } else {
                              SpellResist.addSpellResistance(lCret, this.getNumber(), (double)maxWoundHeal);
                              w.modifySeverity(-maxWoundHeal);
                              ++woundsHealed;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
