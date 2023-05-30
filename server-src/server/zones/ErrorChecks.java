package com.wurmonline.server.zones;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cults;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ErrorChecks implements MiscConstants, CounterTypes {
   private static final Logger logger = Logger.getLogger(ErrorChecks.class.getName());

   private ErrorChecks() {
   }

   public static void checkCreatures(Creature performer, String searchString) {
      long lStart = System.nanoTime();
      int nums = 0;
      Creature[] crets = Creatures.getInstance().getCreatures();
      boolean empty = searchString == null || searchString.length() == 0;
      performer.getCommunicator().sendSafeServerMessage("Starting creature check...");

      for(int x = 0; x < crets.length; ++x) {
         if (empty || crets[x].getName().contains(searchString)) {
            VolaTile t = crets[x].getCurrentTile();
            if (t != null) {
               try {
                  Zone z = Zones.getZone(crets[x].getTileX(), crets[x].getTileY(), crets[x].isOnSurface());
                  VolaTile rt = z.getTileOrNull(crets[x].getTileX(), crets[x].getTileY());
                  if (rt == null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           crets[x].getName() + " [" + crets[x].getWurmId() + "] null tile but current at " + t.getTileX() + ", " + t.getTileY()
                        );
                  } else {
                     if (rt.getTileX() != t.getTileX() || rt.getTileY() != t.getTileY()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              crets[x].getName()
                                 + " ["
                                 + crets[x].getWurmId()
                                 + "] at "
                                 + crets[x].getTileX()
                                 + ","
                                 + crets[x].getTileY()
                                 + " currenttile at "
                                 + t.getTileX()
                                 + " "
                                 + t.getTileY()
                           );
                        ++nums;
                     }

                     boolean found = false;
                     Creature[] cc = rt.getCreatures();

                     for(int xx = 0; xx < cc.length; ++xx) {
                        if (cc[xx].getWurmId() == crets[x].getWurmId()) {
                           found = true;
                        }
                     }

                     if (!found) {
                        if (!crets[x].isDead()) {
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 crets[x].getName()
                                    + " ["
                                    + crets[x].getWurmId()
                                    + "] not in list on tile "
                                    + rt.getTileX()
                                    + " "
                                    + rt.getTileY()
                                    + " #"
                                    + rt.hashCode()
                                    + " xy="
                                    + crets[x].getTileX()
                                    + ", "
                                    + crets[x].getTileY()
                                    + " surf="
                                    + crets[x].isOnSurface()
                                    + " inactive="
                                    + rt.isInactive()
                              );
                           ++nums;
                        }

                        found = false;
                        cc = t.getCreatures();

                        for(int xx = 0; xx < cc.length; ++xx) {
                           if (cc[xx].getWurmId() == crets[x].getWurmId()) {
                              found = true;
                           }
                        }

                        if (!found) {
                           if (!crets[x].isDead()) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    crets[x].getName()
                                       + " ["
                                       + crets[x].getWurmId()
                                       + "] not in list on CURRENT tile "
                                       + t.getTileX()
                                       + " "
                                       + t.getTileY()
                                       + " #"
                                       + t.hashCode()
                                       + " xy="
                                       + crets[x].getTileX()
                                       + ", "
                                       + crets[x].getTileY()
                                       + " surf="
                                       + crets[x].isOnSurface()
                                       + " inactive="
                                       + t.isInactive()
                                 );
                           }

                           if (crets[x].isDead()) {
                              boolean delete = true;
                              if (crets[x].isKingdomGuard()) {
                                 GuardTower tower = Kingdoms.getTower(crets[x]);
                                 if (tower != null) {
                                    try {
                                       delete = false;
                                       tower.returnGuard(crets[x]);
                                       performer.getCommunicator()
                                          .sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] returned to tower.");
                                    } catch (IOException var16) {
                                    }
                                 }
                              }

                              if (delete) {
                                 if (DbCreatureStatus.getIsLoaded(crets[x].getWurmId()) == 0) {
                                    crets[x].destroy();
                                 }

                                 performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] destroyed.");
                              }
                           }
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 crets[x].getName()
                                    + " ["
                                    + crets[x].getWurmId()
                                    + "] IS in list on CURRENT tile "
                                    + t.getTileX()
                                    + " "
                                    + t.getTileY()
                                    + " #"
                                    + t.hashCode()
                                    + " xy="
                                    + crets[x].getTileX()
                                    + ", "
                                    + crets[x].getTileY()
                                    + " surf="
                                    + crets[x].isOnSurface()
                                    + " inactive="
                                    + t.isInactive()
                              );
                        }
                     }
                  }
               } catch (NoSuchZoneException var17) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] no zone at " + t.getTileX() + ", " + t.getTileY());
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage(crets[x].getName() + " [" + crets[x].getWurmId() + "] null current tile.");
            }
         }
      }

      performer.getCommunicator().sendSafeServerMessage("...done. " + nums + " errors.");
      logger.info("#checkCreatures took " + (float)(System.nanoTime() - lStart) / 1000000.0F + "ms.");
   }

   public static void checkItems(Creature performer, String searchString) {
      long lStart = System.nanoTime();
      logger.info(performer + " is checking Items using search string: " + searchString);
      Item[] items = Items.getAllItems();
      boolean empty = searchString == null || searchString.length() == 0;
      performer.getCommunicator().sendSafeServerMessage("Starting items check...");
      int nums = 0;

      for(int x = 0; x < items.length; ++x) {
         if ((empty || items[x].getName().contains(searchString)) && items[x].getZoneId() >= 0 && items[x].getTemplateId() != 177) {
            try {
               Zone z = Zones.getZone(items[x].getTileX(), items[x].getTileY(), items[x].isOnSurface());
               VolaTile rt = z.getTileOrNull(items[x].getTileX(), items[x].getTileY());
               if (rt == null) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        items[x].getName()
                           + " ["
                           + items[x].getWurmId()
                           + "] last:"
                           + items[x].getLastParentId()
                           + " pile="
                           + (WurmId.getType(items[x].lastParentId) == 6)
                           + ", null tile but current at "
                           + items[x].getTileX()
                           + ", "
                           + items[x].getTileY()
                     );
                  ++nums;
               } else {
                  if (rt.getTileX() != items[x].getTileX() || rt.getTileY() != items[x].getTileY()) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           items[x].getName()
                              + " ["
                              + items[x].getWurmId()
                              + "] at "
                              + items[x].getTileX()
                              + ","
                              + items[x].getTileY()
                              + " currenttile at "
                              + rt.getTileX()
                              + " "
                              + rt.getTileY()
                        );
                     ++nums;
                  }

                  Item[] cc = rt.getItems();
                  boolean found = false;

                  for(int xx = 0; xx < cc.length; ++xx) {
                     if (cc[xx].getWurmId() == items[x].getWurmId()) {
                        found = true;
                     }
                  }

                  if (!found) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           items[x].getName()
                              + " ["
                              + items[x].getWurmId()
                              + "] not in list on tile "
                              + rt.getTileX()
                              + " "
                              + rt.getTileY()
                              + " inactive="
                              + rt.isInactive()
                        );
                     ++nums;
                  }
               }
            } catch (NoSuchZoneException var13) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     items[x].getName() + " [" + items[x].getWurmId() + "] no zone at " + items[x].getTileX() + ", " + items[x].getTileY()
                  );
            }
         }
      }

      performer.getCommunicator().sendSafeServerMessage("...done. " + nums + " errors.");
      logger.info("#checkItems took " + (float)(System.nanoTime() - lStart) / 1000000.0F + "ms.");
   }

   public static void getInfo(Creature performer, int tilex, int tiley, int layer) {
      try {
         Zone z = Zones.getZone(tilex, tiley, layer >= 0);
         VolaTile rt = z.getOrCreateTile(tilex, tiley);
         Creature[] cc = rt.getCreatures();
         VirtualZone[] watchers = rt.getWatchers();

         for(int xx = 0; xx < cc.length; ++xx) {
            performer.getCommunicator().sendNormalServerMessage(tilex + ", " + tiley + " contains " + cc[xx].getName());

            try {
               Server.getInstance().getCreature(cc[xx].getWurmId());
            } catch (NoSuchCreatureException var15) {
               performer.getCommunicator().sendNormalServerMessage("The Creatures list does NOT contain " + cc[xx].getWurmId());
            } catch (NoSuchPlayerException var16) {
               performer.getCommunicator().sendNormalServerMessage("The Players list does NOT contain " + cc[xx].getWurmId());
            }

            for(int v = 0; v < watchers.length; ++v) {
               try {
                  if (!watchers[v].containsCreature(cc[xx])) {
                     if (watchers[v].getWatcher() != null && watchers[v].getWatcher().getWurmId() != cc[xx].getWurmId()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(cc[xx].getName() + " (" + cc[xx].getWurmId() + ") is not visible to " + watchers[v].getWatcher().getName());
                     } else if (watchers[v].getWatcher() == null) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The tile is monitored by an unknown creature or player who will not see the creature.");
                     }
                  }
               } catch (Exception var17) {
                  logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
               }
            }
         }

         Item[] items = rt.getItems();
         if (Servers.localServer.testServer) {
            for(Item i : items) {
               String itemMessage = String.format("It contains %s, at floor level %d, at Z position %.2f", i.getName(), i.getFloorLevel(), i.getPosZ());
               performer.getCommunicator().sendNormalServerMessage(itemMessage);
            }

            if (performer.getPower() >= 5) {
               String zoneMessage = String.format(
                  "Tile belongs to zone %d, which covers %d, %d to %d, %d.", z.getId(), z.getStartX(), z.getStartY(), z.getEndX(), z.getEndY()
               );
               performer.getCommunicator().sendNormalServerMessage(zoneMessage);
               VolaTile caveTile = Zones.getOrCreateTile(rt.tilex, rt.tiley, false);
               String caveVTMessage = String.format(
                  "Cave VolaTile instance transition is %s, layer is %d. It contains %d items.",
                  caveTile.isTransition(),
                  caveTile.getLayer(),
                  caveTile.getItems().length
               );
               performer.getCommunicator().sendNormalServerMessage(caveVTMessage);
               VolaTile surfTile = Zones.getOrCreateTile(rt.tilex, rt.tiley, true);
               String surfVTMessage = String.format(
                  "Surface VolaTile instance transition is %s, layer is %d. It contains %d items.",
                  surfTile.isTransition(),
                  surfTile.getLayer(),
                  surfTile.getItems().length
               );
               performer.getCommunicator().sendNormalServerMessage(surfVTMessage);
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("It contains " + items.length + " items.");
         }
      } catch (NoSuchZoneException var18) {
         performer.getCommunicator().sendNormalServerMessage(tilex + "," + tiley + " no zone.");
      }

      try {
         float height = Zones.calculateHeight((float)((tilex << 2) + 2), (float)((tiley << 2) + 2), performer.isOnSurface()) * 10.0F;
         byte path = Cults.getPathFor(tilex, tiley, 0, (int)height);
         performer.getCommunicator().sendNormalServerMessage("Meditation path is " + Cults.getPathNameFor(path) + ".");
      } catch (NoSuchZoneException var14) {
         logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
      }
   }

   public static void checkZones(Creature checker) {
      logger.info(checker.getName() + " checking zones");
      checker.getCommunicator().sendNormalServerMessage("Checking cave zone tiles:");
      Zones.checkAllCaveZones(checker);
      checker.getCommunicator().sendNormalServerMessage("Checking surface zone tiles:");
      Zones.checkAllSurfaceZones(checker);
      checker.getCommunicator().sendNormalServerMessage("Done.");
      logger.info(checker.getName() + " finished checking zones");
   }

   public static void checkItemWatchers() {
   }
}
