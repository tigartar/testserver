package com.wurmonline.server.spells;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;

public class MassStamina extends ReligiousSpell implements AttitudeConstants {
   public static final int RANGE = 12;

   public MassStamina() {
      super("Mass Stamina", 425, 15, 50, 20, 40, 900000L);
      this.targetTile = true;
      this.description = "covers an area with revitalising energy, refreshing stamina for allies";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      int sx = Zones.safeTileX(tilex - 5 - performer.getNumLinks());
      int sy = Zones.safeTileY(tiley - 5 - performer.getNumLinks());
      int ex = Zones.safeTileX(tilex + 5 + performer.getNumLinks());
      int ey = Zones.safeTileY(tiley + 5 + performer.getNumLinks());

      for(int x = sx; x < ex; ++x) {
         for(int y = sy; y < ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
            if (t != null) {
               Creature[] crets = t.getCreatures();

               for(Creature lCret : crets) {
                  if (lCret.getAttitude(performer) != 2) {
                     lCret.getStatus().modifyStamina2(100.0F);
                  }
               }
            }
         }
      }
   }
}
