package com.wurmonline.server.spells;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;

public class Ward extends ReligiousSpell {
   public static final int RANGE = 40;

   public Ward() {
      super("Ward", 437, 20, 20, 20, 43, 0L);
      this.targetTile = true;
      this.description = "drives away enemy creatures";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator().sendNormalServerMessage("You place the Mark of Fo in the area, declaring a sanctuary.");
      int sx = Zones.safeTileX(tilex - ((int)Math.max(5.0, power / 10.0) + performer.getNumLinks()));
      int sy = Zones.safeTileY(tiley - ((int)Math.max(5.0, power / 10.0) + performer.getNumLinks()));
      int ex = Zones.safeTileX(tilex + (int)Math.max(5.0, power / 10.0) + performer.getNumLinks());
      int ey = Zones.safeTileY(tiley + (int)Math.max(5.0, power / 10.0) + performer.getNumLinks());

      for(int x = sx; x < ex; ++x) {
         for(int y = sy; y < ey; ++y) {
            VolaTile t = Zones.getOrCreateTile(x, y, performer.isOnSurface());
            if (t != null) {
               Creature[] crets = t.getCreatures();

               for(Creature cret : crets) {
                  if (!cret.isPlayer() && !cret.isHuman() && !cret.isSpiritGuard() && !cret.isUnique() && cret.getLoyalty() <= 0.0F && !cret.isRidden()) {
                     cret.setTarget(-10L, true);
                     if (cret.opponent != null) {
                        cret.opponent.setTarget(-10L, true);
                        if (cret.opponent != null) {
                           cret.opponent.setOpponent(null);
                        }
                     }

                     cret.setOpponent(null);
                     cret.setFleeCounter(20, true);
                     Server.getInstance().broadCastAction(cret.getName() + " panics.", cret, 10);
                  }
               }
            }
         }
      }
   }
}
