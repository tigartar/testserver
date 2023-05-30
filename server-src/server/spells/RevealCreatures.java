package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;

public class RevealCreatures extends ReligiousSpell {
   public static final int RANGE = 4;

   public RevealCreatures() {
      super("Reveal Creatures", 444, 40, 30, 25, 30, 0L);
      this.targetTile = true;
      this.description = "locates creatures nearby";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator().sendNormalServerMessage("You receive insights about the area.");
      int sx = Zones.safeTileX(performer.getTileX() - 40 - performer.getNumLinks() * 5);
      int sy = Zones.safeTileY(performer.getTileY() - 40 - performer.getNumLinks() * 5);
      int ex = Zones.safeTileX(performer.getTileX() + 40 + performer.getNumLinks() * 5);
      int ey = Zones.safeTileY(performer.getTileY() + 40 + performer.getNumLinks() * 5);
      Zone[] zones = Zones.getZonesCoveredBy(sx, sy, ex, ey, performer.isOnSurface());

      for(Zone lZone : zones) {
         Creature[] crets = lZone.getAllCreatures();

         for(Creature cret : crets) {
            if (cret.getPower() <= performer.getPower() && cret != performer && cret.getBonusForSpellEffect((byte)29) <= 0.0F) {
               int mindist = Math.max(Math.abs(cret.getTileX() - performer.getTileX()), Math.abs(cret.getTileY() - performer.getTileY()));
               int dir = MethodsCreatures.getDir(performer, cret.getTileX(), cret.getTileY());
               String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
               String toReturn;
               if (DbCreatureStatus.getIsLoaded(cret.getWurmId()) == 0) {
                  toReturn = EndGameItems.getDistanceString(mindist, cret.getName(), direction, false);
               } else {
                  toReturn = "";
               }

               performer.getCommunicator().sendNormalServerMessage(toReturn);
            }
         }
      }
   }
}
