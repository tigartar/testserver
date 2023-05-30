package com.wurmonline.server.spells;

import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.awt.Rectangle;

public class RevealSettlements extends ReligiousSpell {
   public static final int RANGE = 4;

   public RevealSettlements() {
      super("Reveal Settlements", 443, 20, 30, 25, 30, 0L);
      this.targetTile = true;
      this.description = "locates nearby settlements";
      this.type = 2;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset) {
      performer.getCommunicator().sendNormalServerMessage("You receive insights about the area.");
      int sx = Zones.safeTileX(performer.getTileX() - 100 - performer.getNumLinks() * 20);
      int sy = Zones.safeTileY(performer.getTileY() - 100 - performer.getNumLinks() * 20);
      int ex = Zones.safeTileX(performer.getTileX() + 100 + performer.getNumLinks() * 20);
      int ey = Zones.safeTileY(performer.getTileY() + 100 + performer.getNumLinks() * 20);
      Rectangle zoneRect = new Rectangle(sx, sy, ex - sx, ey - sy);
      Village[] vills = Villages.getVillages();

      for(Village vill : vills) {
         if (vill != performer.getCurrentVillage()) {
            Rectangle villageRect = new Rectangle(vill.startx, vill.starty, vill.endx - vill.startx + 1, vill.endy - vill.starty + 1);
            if (villageRect.intersects(zoneRect)) {
               int centerx = (int)villageRect.getCenterX();
               int centery = (int)villageRect.getCenterY();
               int mindist = Math.max(Math.abs(centerx - performer.getTileX()), Math.abs(centery - performer.getTileY()));
               int dir = MethodsCreatures.getDir(performer, centerx, centery);
               String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
               String toReturn = EndGameItems.getDistanceString(mindist, vill.getName(), direction, true);
               performer.getCommunicator().sendNormalServerMessage(toReturn);
            }
         }
      }
   }
}
