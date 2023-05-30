package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ItemPileBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(ItemPileBehaviour.class.getName());

   ItemPileBehaviour() {
      super((short)2);
   }

   private ItemPileBehaviour(short type) {
      super(type);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 6.0F)) {
         BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
         if (performer.getPower() > 1 || result == null) {
            toReturn.add(Actions.actionEntrys[6]);
            if (target.isHollow()) {
               try {
                  Creature[] watchers = target.getWatchers();
                  boolean watching = false;

                  for(int x = 0; x < watchers.length; ++x) {
                     if (watchers[x] == performer) {
                        watching = true;
                     }
                  }

                  if (watching) {
                     toReturn.add(Actions.actionEntrys[4]);
                  } else {
                     toReturn.add(Actions.actionEntrys[3]);
                  }
               } catch (NoSuchCreatureException var8) {
                  toReturn.add(Actions.actionEntrys[3]);
               }
            }
         }
      }

      if (performer.getPower() > 2) {
         int cnt = -2;
         if (Servers.isThisATestServer()) {
            --cnt;
         }

         toReturn.add(new ActionEntry((short)cnt, "Specials", "Specials"));
         toReturn.add(Actions.actionEntrys[179]);
         toReturn.add(Actions.actionEntrys[185]);
         if (Servers.isThisATestServer()) {
            toReturn.add(Actions.actionEntrys[180]);
         }
      } else if (Players.isArtist(performer.getWurmId(), false, false)) {
         toReturn.add(Actions.actionEntrys[185]);
      }

      addEmotes(toReturn);
      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      return this.getBehavioursFor(performer, target);
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean toReturn = true;
      if (action == 6) {
         toReturn = MethodsItems.takePile(act, performer, target);
      } else if (action == 7) {
         String[] msg = MethodsItems.drop(performer, target, true);
         if (msg.length > 0) {
            performer.getCommunicator().sendNormalServerMessage(msg[0] + msg[1] + msg[2]);
            Server.getInstance().broadCastAction(performer.getName() + " drops " + msg[1] + msg[3], performer, 5);
         }
      } else if (action == 3) {
         toReturn = true;
         if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 6.0F)) {
            performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
            return toReturn;
         }

         if (performer.addItemWatched(target)) {
            performer.getCommunicator().sendOpenInventoryWindow(target.getWurmId(), target.getName());
            target.addWatcher(target.getWurmId(), performer);
            target.sendContainedItems(target.getWurmId(), performer);
         }
      } else if (action == 4) {
         toReturn = true;
         performer.removeItemWatched(target);
         performer.getCommunicator().sendCloseInventoryWindow(target.getWurmId());
         target.removeWatcher(performer, true);
      } else if (action == 1) {
         performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
         target.sendEnchantmentStrings(performer.getCommunicator());
      } else if (action == 179) {
         toReturn = true;
         if (performer.getPower() > 2) {
            Item[] items = target.getItemsAsArray();
            logger.log(
               Level.INFO,
               performer.getName() + " summoning pile at " + target.getTileX() + ", " + target.getTileY() + ". Number of items=" + items.length + "."
            );

            for(int x = 0; x < items.length; ++x) {
               try {
                  Zone currZone = Zones.getZone((int)items[x].getPosX() >> 2, (int)items[x].getPosY() >> 2, items[x].isOnSurface());
                  currZone.removeItem(items[x]);
                  items[x].putItemInfrontof(performer);
               } catch (NoSuchZoneException var20) {
                  performer.getCommunicator().sendNormalServerMessage("Failed to locate the zone for that item. Failed to summon.");
                  logger.log(Level.WARNING, target.getWurmId() + ": " + var20.getMessage(), (Throwable)var20);
               } catch (NoSuchCreatureException var21) {
                  performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
                  logger.log(Level.WARNING, target.getWurmId() + ": " + var21.getMessage(), (Throwable)var21);
               } catch (NoSuchItemException var22) {
                  performer.getCommunicator().sendNormalServerMessage("Failed to locate the item for that request! Failed to summon.");
                  logger.log(Level.WARNING, target.getWurmId() + ": " + var22.getMessage(), (Throwable)var22);
               } catch (NoSuchPlayerException var23) {
                  performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
                  logger.log(Level.WARNING, target.getWurmId() + ": " + var23.getMessage(), (Throwable)var23);
               }
            }
         }
      } else if (action == 180 && Servers.isThisATestServer()) {
         toReturn = true;
         if (performer.getPower() > 2) {
            Item[] items = target.getItemsAsArray();
            logger.log(
               Level.INFO,
               performer.getName()
                  + " destroying items in pile at "
                  + target.getTileX()
                  + ", "
                  + target.getTileY()
                  + ". Number of items="
                  + items.length
                  + "."
            );

            for(int x = 0; x < items.length; ++x) {
               performer.getLogger()
                  .log(
                     Level.INFO,
                     performer.getName()
                        + " destroyed item "
                        + items[x].getWurmId()
                        + ", "
                        + items[x].getNameWithGenus()
                        + ", ql="
                        + items[x].getQualityLevel()
                  );
               Items.destroyItem(items[x].getWurmId());
            }
         }
      }

      if (action == 185) {
         toReturn = true;
         if (performer.getPower() >= 0 || Players.isArtist(performer.getWurmId(), false, false)) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "WurmId:"
                     + target.getWurmId()
                     + ", posx="
                     + target.getPosX()
                     + "("
                     + ((int)target.getPosX() >> 2)
                     + "), posy="
                     + target.getPosY()
                     + "("
                     + ((int)target.getPosY() >> 2)
                     + "), posz="
                     + target.getPosZ()
                     + ", rot"
                     + target.getRotation()
                     + " layer="
                     + (target.isOnSurface() ? 0 : -1)
                     + " fl="
                     + target.getFloorLevel()
                     + " bridge="
                     + target.getBridgeId()
               );
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "Ql:"
                     + target.getQualityLevel()
                     + ", damage="
                     + target.getDamage()
                     + ", weight="
                     + target.getWeightGrams()
                     + ", temp="
                     + target.getTemperature()
               );
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "parentid="
                     + target.getParentId()
                     + " ownerid="
                     + target.getOwnerId()
                     + " zoneid="
                     + target.getZoneId()
                     + " sizex="
                     + target.getSizeX()
                     + ", sizey="
                     + target.getSizeY()
                     + " sizez="
                     + target.getSizeZ()
                     + "."
               );
            long timeSince = WurmCalendar.currentTime - target.getLastMaintained();
            String timeString = Server.getTimeFor(timeSince * 1000L);
            performer.getCommunicator().sendNormalServerMessage("Last maintained " + timeString + " ago.");
            String lastOwnerS = Long.toString(target.lastOwner);
            PlayerInfo p = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getLastOwnerId());
            if (p != null) {
               lastOwnerS = p.getName();
            } else {
               try {
                  Creature c = Creatures.getInstance().getCreature(target.lastOwner);
                  lastOwnerS = c.getName();
               } catch (NoSuchCreatureException var19) {
                  lastOwnerS = "dead " + lastOwnerS;
               }
            }

            performer.getCommunicator().sendNormalServerMessage("lastownerid=" + lastOwnerS + ", Model=" + target.getModelName());
            if (performer.getPower() >= 5) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "Zoneid="
                        + target.getZoneId()
                        + " real zid="
                        + target.zoneId
                        + " Counter="
                        + WurmId.getNumber(target.getWurmId())
                        + " origin="
                        + WurmId.getOrigin(target.getWurmId())
                  );
               if (target.isVehicle()) {
                  float diffposx = target.getPosX() - performer.getPosX();
                  float diffposy = target.getPosY() - performer.getPosY();
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "Relative: offx=" + diffposx + ", offy=" + diffposy + ", offz=" + performer.getPositionZ() + " altOffZ=" + performer.getAltOffZ()
                     );
               }
            }

            if (target.hasData()) {
               performer.getCommunicator()
                  .sendNormalServerMessage("data=" + target.getData() + ", data1=" + target.getData1() + " data2=" + target.getData2());
            }

            String creator = ", creator=" + target.creator;
            if (target.creator == null || target.creator.length() == 0) {
               creator = "";
            }

            performer.getCommunicator().sendNormalServerMessage("auxdata=" + target.getAuxData() + creator);
            if (target.isKey()) {
               performer.getCommunicator().sendNormalServerMessage("lock id=" + target.getLockId());
            }

            if (target.isLock()) {
               long[] keys = target.getKeyIds();
               performer.getCommunicator().sendNormalServerMessage("Keys:");

               for(long lKey : keys) {
                  performer.getCommunicator().sendNormalServerMessage(String.valueOf(lKey));
               }
            }
         }
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      return this.action(act, performer, target, action, counter);
   }
}
