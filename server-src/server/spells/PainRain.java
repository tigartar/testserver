package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;

public class PainRain extends DamageSpell implements AttitudeConstants {
   public static final int RANGE = 24;
   public static final double BASE_DAMAGE = 6000.0;
   public static final double DAMAGE_PER_POWER = 40.0;
   public static final int RADIUS = 2;

   public PainRain() {
      super("Pain Rain", 432, 10, 40, 20, 40, 120000L);
      this.targetTile = true;
      this.offensive = true;
      this.description = "covers an area with damaging energy causing infection wounds on enemies";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      Structure currstr = performer.getCurrentTile().getStructure();
      int radiusBonus = (int)(power / 40.0);
      int sx = Zones.safeTileX(tilex - 2 - radiusBonus - performer.getNumLinks());
      int sy = Zones.safeTileY(tiley - 2 - radiusBonus - performer.getNumLinks());
      int ex = Zones.safeTileX(tilex + 2 + radiusBonus + performer.getNumLinks());
      int ey = Zones.safeTileY(tiley + 2 + radiusBonus + performer.getNumLinks());

      for(int x = sx; x < ex; ++x) {
         for(int y = sy; y < ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, layer == 0);
            if (t != null) {
               Structure toCheck = t.getStructure();
               if (currstr == toCheck) {
                  Item ring = Zones.isWithinDuelRing(x, y, layer >= 0);
                  if (ring == null) {
                     Creature[] crets = t.getCreatures();
                     int affected = 0;

                     for(Creature lCret : crets) {
                        if (!lCret.isInvulnerable() && lCret.getAttitude(performer) == 2) {
                           lCret.addAttacker(performer);
                           double damage = this.calculateDamage(lCret, power, 6000.0, 40.0);
                           lCret.addWoundOfType(performer, (byte)6, 1, true, 1.0F, false, damage, (float)power / 5.0F, 0.0F, false, true);
                           ++affected;
                        }

                        if ((double)affected > power / 10.0 + (double)performer.getNumLinks()) {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
