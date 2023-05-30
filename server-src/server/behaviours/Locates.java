package com.wurmonline.server.behaviours;

import com.wurmonline.server.Features;
import com.wurmonline.server.Point;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Logger;

public final class Locates {
   private static final Logger logger = Logger.getLogger(Locates.class.getName());

   private Locates() {
   }

   static void locateSpring(Creature performer, Item pendulum, Skill primSkill) {
      int[] closest = Zones.getClosestSpring(performer.getTileX(), performer.getTileY(), (int)(10.0F * getMaterialPendulumModifier(pendulum.getMaterial())));
      int max = Math.max(closest[0], closest[1]);
      double knowl = primSkill.getKnowledge((double)pendulum.getCurrentQualityLevel());
      float difficulty = Server.rand.nextFloat() * (float)(max + 3) * 30.0F;
      double result = (double)Server.rand.nextFloat() * knowl * 10.0;
      result -= (double)difficulty;
      Server.getInstance()
         .broadCastAction(performer.getName() + " lets out a mild sigh as " + performer.getHeSheItString() + " starts breathing again.", performer, 5);
      if (closest[0] == -1) {
         performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
      } else if (result > 0.0) {
         if (max < 1) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " now swings frantically! There is something here!");
         } else if (max < 2) {
            performer.getCommunicator()
               .sendNormalServerMessage("The " + pendulum.getName() + " swings rapidly back and forth! You are close to a water source!");
         } else if (max < 3) {
            performer.getCommunicator()
               .sendNormalServerMessage("The " + pendulum.getName() + " is swinging in a circle, there is probably a water source in the ground nearby.");
         } else if (max < 5) {
            performer.getCommunicator()
               .sendNormalServerMessage("The " + pendulum.getName() + " is starting to move, indicating a flow of energy somewhere near.");
         } else if (result > 30.0) {
            performer.getCommunicator().sendNormalServerMessage("You think you detect some faint tugs in the " + pendulum.getName() + ".");
         } else {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " seems dead.");
      }
   }

   static void useLocateItem(Creature performer, Item pendulum, Skill primSkill) {
      if (pendulum.getSpellLocChampBonus() > 0.0F) {
         locateChamp(performer, pendulum, primSkill);
      } else if (pendulum.getSpellLocEnemyBonus() > 0.0F) {
         locateEnemy(performer, pendulum, primSkill);
      } else if (pendulum.getSpellLocFishBonus() > 0.0F) {
         if (Servers.isThisATestServer() && performer.isOnSurface()) {
            performer.getCommunicator().sendAlertServerMessage("New fishing...");
            locateFish(performer, pendulum, primSkill, true);
            performer.getCommunicator().sendAlertServerMessage("Old fishing...");
            locateFish(performer, pendulum, primSkill, false);
         } else {
            locateFish(performer, pendulum, primSkill, true);
         }
      }
   }

   static void locateChamp(Creature performer, Item pendulum, Skill primSkill) {
      int x = performer.getTileX();
      int y = performer.getTileY();
      int dist = (int)(pendulum.getSpellLocChampBonus() / 100.0F * (float)Zones.worldTileSizeX / 32.0F * getMaterialPendulumModifier(pendulum.getMaterial()));
      Creature firstChamp = findFirstCreature(x, y, dist, performer.isOnSurface(), true, performer);
      if (firstChamp != null) {
         int dx = Math.abs(x - firstChamp.getTileX());
         int dy = Math.abs(y - firstChamp.getTileY());
         int maxd = (int)Math.sqrt((double)(dx * dx + dy * dy));
         if (primSkill.skillCheck((double)((float)maxd / 10.0F), pendulum, 0.0, false, 5.0F) > 0.0) {
            int dir = MethodsCreatures.getDir(performer, firstChamp.getTileX(), firstChamp.getTileY());
            String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
            String toReturn = EndGameItems.getDistanceString(maxd, firstChamp.getName(), direction, false);
            performer.getCommunicator().sendNormalServerMessage(toReturn);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
         }
      } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0F) > 0.0) {
         performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
      }
   }

   static void locateEnemy(Creature performer, Item pendulum, Skill primSkill) {
      int x = performer.getTileX();
      int y = performer.getTileY();
      int dist = (int)(pendulum.getSpellLocEnemyBonus() * getMaterialPendulumModifier(pendulum.getMaterial()));
      Creature firstEnemy = findFirstCreature(x, y, dist, performer.isOnSurface(), false, performer);
      if (firstEnemy != null) {
         int dx = Math.abs(x - firstEnemy.getTileX());
         int dy = Math.abs(y - firstEnemy.getTileY());
         int maxd = (int)Math.sqrt((double)(dx * dx + dy * dy));
         if (primSkill.skillCheck((double)((float)maxd / 10.0F), pendulum, 0.0, false, 5.0F) > 0.0) {
            int dir = MethodsCreatures.getDir(performer, firstEnemy.getTileX(), firstEnemy.getTileY());
            String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
            String toReturn = EndGameItems.getDistanceString(maxd, "enemy", direction, false);
            performer.getCommunicator().sendNormalServerMessage(toReturn);
            locateTraitor(performer, pendulum, primSkill);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
         }
      } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0F) > 0.0) {
         if (!locateTraitor(performer, pendulum, primSkill)) {
            performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
      }
   }

   static boolean locateTraitor(Creature performer, Item pendulum, Skill primSkill) {
      Creature[] possibleTraitors = EpicServerStatus.getCurrentTraitors();
      if (possibleTraitors != null) {
         int maxDist = (int)(
            pendulum.getSpellLocEnemyBonus() / 100.0F * (float)Zones.worldTileSizeX / 16.0F * getMaterialPendulumModifier(pendulum.getMaterial())
         );

         for(Creature c : possibleTraitors) {
            if (performer.isWithinDistanceTo(c, (float)maxDist)) {
               int dx = Math.abs(performer.getTileX() - c.getTileX());
               int dy = Math.abs(performer.getTileY() - c.getTileY());
               int maxd = (int)Math.sqrt((double)(dx * dx + dy * dy));
               int dir = MethodsCreatures.getDir(performer, c.getTileX(), c.getTileY());
               String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
               String toReturn = EndGameItems.getDistanceString(maxd, c.getName(), direction, false);
               performer.getCommunicator().sendNormalServerMessage(toReturn);
               return true;
            }
         }
      }

      return false;
   }

   static void locateFish(Creature performer, Item pendulum, Skill primSkill, boolean newFishing) {
      if (!performer.isOnSurface()) {
         performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " does not move.");
      } else {
         int maxDist = (int)(pendulum.getSpellLocFishBonus() / 10.0F * getMaterialPendulumModifier(pendulum.getMaterial()));
         Point[] points;
         if (newFishing) {
            int season = WurmCalendar.getSeasonNumber();
            points = MethodsFishing.getSpecialSpots(performer.getTileX(), performer.getTileY(), season);
         } else {
            points = Fish.getRareSpots(performer.getTileX(), performer.getTileY());
         }

         boolean found = false;

         for(Point point : points) {
            if (performer.isWithinTileDistanceTo(point.getX(), point.getY(), 0, maxDist + 5)) {
               sendFishFound(point.getX(), point.getY(), point.getH(), performer, primSkill, pendulum);
               found = true;
            }
         }

         if (!found) {
            performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
         }
      }
   }

   private static final void sendFishFound(int targx, int targy, int fish, Creature performer, Skill primSkill, Item pendulum) {
      int x = performer.getTileX();
      int y = performer.getTileY();
      if (fish > 0) {
         int dx = Math.max(0, Math.abs(x - targx) - 5);
         int dy = Math.max(0, Math.abs(y - targy) - 5);
         int maxd = (int)Math.sqrt((double)(dx * dx + dy * dy));
         double skillCheck = primSkill.skillCheck((double)maxd, pendulum, 0.0, false, 5.0F);
         if (skillCheck > 0.0) {
            int dir = MethodsCreatures.getDir(performer, targx, targy);
            String direction = MethodsCreatures.getLocationStringFor(performer.getStatus().getRotation(), dir, "you");
            String spot = "fishing spot";
            if (skillCheck > 75.0) {
               String name = ItemTemplateFactory.getInstance().getTemplateName(fish);
               if (name.length() > 0) {
                  spot = name + " fishing spot";
               }
            }

            String loc = "";
            if (performer.getPower() >= 2) {
               loc = " (" + targx + "," + targy + ")";
            }

            String toReturn = EndGameItems.getDistanceString(maxd, spot + loc, direction, false);
            performer.getCommunicator().sendNormalServerMessage(toReturn);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You feel there is something there but cannot determine what.");
         }
      } else if (primSkill.skillCheck(10.0, pendulum, 0.0, false, 5.0F) > 0.0) {
         performer.getCommunicator().sendNormalServerMessage("The " + pendulum.getName() + " doesn't seem to move.");
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to make sense of the " + pendulum.getName() + ".");
      }
   }

   static final Creature findFirstCreature(int x, int y, int maxdist, boolean surfaced, boolean champ, Creature performer) {
      for(int tdist = 0; tdist <= maxdist; ++tdist) {
         if (tdist == 0) {
            Creature c = getCreatureOnTile(x, y, tdist, surfaced, champ, performer);
            if (c != null) {
               return c;
            }
         } else {
            Creature c = findCreatureOnRow(x, y, tdist, surfaced, champ, performer);
            if (c != null) {
               return c;
            }
         }
      }

      return null;
   }

   private static final Creature findCreatureOnRow(int x, int y, int dist, boolean surfaced, boolean champ, Creature performer) {
      for(int tx = x; tx < x + dist; ++tx) {
         if (tx < Zones.worldTileSizeX && tx > 0) {
            Creature toReturn = getCreatureOnTile(tx, y - dist, dist, surfaced, champ, performer);
            if (toReturn != null) {
               return toReturn;
            }
         }
      }

      for(int ty = y - dist; ty < y; ++ty) {
         if (ty < Zones.worldTileSizeY && ty > 0) {
            Creature toreturn = getCreatureOnTile(x + dist, dist, ty, surfaced, champ, performer);
            if (toreturn != null) {
               return toreturn;
            }
         }
      }

      for(int ty = y; ty <= y + dist; ++ty) {
         if (ty < Zones.worldTileSizeY && ty > 0) {
            Creature toreturn = getCreatureOnTile(x + dist, dist, ty, surfaced, champ, performer);
            if (toreturn != null) {
               return toreturn;
            }
         }
      }

      for(int tx = x; tx < x + dist; ++tx) {
         if (tx < Zones.worldTileSizeX && tx > 0) {
            Creature toReturn = getCreatureOnTile(tx, y + dist, dist, surfaced, champ, performer);
            if (toReturn != null) {
               return toReturn;
            }
         }
      }

      for(int ty = y - dist; ty < y; ++ty) {
         if (ty < Zones.worldTileSizeY && ty > 0) {
            Creature toreturn = getCreatureOnTile(x - dist, dist, ty, surfaced, champ, performer);
            if (toreturn != null) {
               return toreturn;
            }
         }
      }

      for(int tx = x - dist; tx < x; ++tx) {
         if (tx < Zones.worldTileSizeX && tx > 0) {
            Creature toReturn = getCreatureOnTile(tx, y + dist, dist, surfaced, champ, performer);
            if (toReturn != null) {
               return toReturn;
            }
         }
      }

      for(int ty = y; ty < y + dist; ++ty) {
         if (ty < Zones.worldTileSizeY && ty > 0) {
            Creature toreturn = getCreatureOnTile(x - dist, ty, dist, surfaced, champ, performer);
            if (toreturn != null) {
               return toreturn;
            }
         }
      }

      for(int tx = x - dist; tx < x; ++tx) {
         if (tx < Zones.worldTileSizeX && tx > 0) {
            Creature toReturn = getCreatureOnTile(tx, y - dist, dist, surfaced, champ, performer);
            if (toReturn != null) {
               return toReturn;
            }
         }
      }

      return null;
   }

   static final Creature getCreatureOnTile(int x, int y, int dist, boolean surfaced, boolean champ, Creature performer) {
      VolaTile t = Zones.getTileOrNull(x, y, surfaced);
      if (t != null) {
         Creature[] crets = t.getCreatures();

         for(Creature c : crets) {
            if (champ && (c.getStatus().isChampion() || c.isUnique())) {
               return c;
            }

            if (!champ && c.isPlayer()) {
               boolean found = c.getAttitude(performer) == 2;
               if (!found) {
                  found = performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(c);
               }

               if (found && (!c.isStealth() || dist <= 25)) {
                  float nolocateEnchantPower = c.getNoLocateItemBonus(false);
                  if (nolocateEnchantPower > 0.0F) {
                     int maxDistance = 100;
                     int distReduction = (int)(nolocateEnchantPower / 2.0F);
                     if (dist > maxDistance - distReduction) {
                        continue;
                     }
                  }

                  if (c.getBonusForSpellEffect((byte)29) <= 0.0F) {
                     return c;
                  }
               }
            }
         }
      }

      return null;
   }

   private static float getMaterialPendulumModifier(byte material) {
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         switch(material) {
            case 7:
               return 1.1F;
            case 8:
               return 1.05F;
            case 9:
               return 1.025F;
            case 10:
               return 0.95F;
            case 12:
               return 0.9F;
            case 13:
               return 0.95F;
            case 30:
               return 1.025F;
            case 31:
               return 1.05F;
            case 34:
               return 0.95F;
            case 56:
               return 1.15F;
            case 57:
               return 1.2F;
            case 67:
               return 1.25F;
            case 96:
               return 1.075F;
         }
      }

      return 1.0F;
   }
}
