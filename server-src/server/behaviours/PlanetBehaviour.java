package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.LinkedList;
import java.util.List;

final class PlanetBehaviour extends Behaviour implements MiscConstants {
   PlanetBehaviour() {
      super((short)36);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int planetId) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, object, planetId));
      if (performer instanceof Player && object.getTemplateId() == 903) {
         toReturn.add(Actions.actionEntrys[118]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, int planetId) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, planetId));
      return toReturn;
   }

   @Override
   public boolean action(Action action, Creature performer, int planetId, short num, float counter) {
      if (num == 1) {
         if (planetId == 0) {
            performer.getCommunicator().sendNormalServerMessage("You see Jackal, stealer of souls and flowing with the blood of the damned.");
         } else if (planetId == 1) {
            performer.getCommunicator().sendNormalServerMessage("You see Valrei, home of the Gods.");
         } else if (planetId == 2) {
            performer.getCommunicator().sendNormalServerMessage("You see Seris, home of the Dead.");
         } else if (planetId == 3) {
            performer.getCommunicator().sendNormalServerMessage("You see Haven. Rumours have it that this is where Golden Valley lies.");
         } else if (planetId == 99) {
            performer.getCommunicator().sendNormalServerMessage("You see Sol, home of the demons.");
         }
      }

      return true;
   }

   @Override
   public boolean action(Action action, Creature performer, Item source, int planetId, short num, float counter) {
      if (num == 1) {
         return this.action(action, performer, planetId, num, counter);
      } else if (num != 118) {
         return true;
      } else {
         boolean done = false;
         if (!performer.isOnSurface()) {
            performer.getCommunicator().sendNormalServerMessage("You must be on the surface for astral navigation.");
            done = true;
         } else {
            boolean insta = Servers.localServer.testServer && performer.getPower() > 1;
            int timePerStage = insta ? 2 : 10;
            int time = timePerStage * 30;
            if (counter == 1.0F) {
               action.setTimeLeft(time);
               performer.sendActionControl(Actions.actionEntrys[118].getVerbString(), true, time);
               performer.getCommunicator().sendNormalServerMessage("You carefully place the dioptra on its tripod in front of you.");
               Server.getInstance().broadCastAction(performer.getName() + " starts to work out where they are.", performer, 5);
               action.setTickCount(0);
            } else {
               time = action.getTimeLeft();
            }

            if (action.currentSecond() % timePerStage == 0) {
               action.incTickCount();
               String pMsg = "";
               String bMsg = "";
               switch(action.getTickCount()) {
                  case 1:
                     pMsg = "You make sure the dioptra is level.";
                     bMsg = performer.getName() + " levels a dioptra.";
                     break;
                  case 2:
                     pMsg = "You line up the dioptra with " + getName(planetId) + ".";
                     bMsg = performer.getName() + " points the dioptra.";
                     break;
                  case 3:
                     pMsg = "You work out you are in the " + EpicServerStatus.getAreaString(performer.getTileX(), performer.getTileY()) + ".";
               }

               if (pMsg.length() > 0) {
                  performer.getCommunicator().sendNormalServerMessage(pMsg);
               }

               if (bMsg.length() > 0) {
                  Server.getInstance().broadCastAction(bMsg, performer, 5);
               }

               if (action.getTickCount() >= 3 || done) {
                  performer.getCommunicator().sendNormalServerMessage("You pack up the dioptra.");
                  Server.getInstance().broadCastAction(performer.getName() + " packs up a dioptra.", performer, 5);
                  done = true;
               }
            }
         }

         return done;
      }
   }

   static final String getName(int planetId) {
      if (planetId == 0) {
         return "Jackal";
      } else if (planetId == 1) {
         return "Valrei";
      } else if (planetId == 2) {
         return "Seris";
      } else if (planetId == 3) {
         return "Haven";
      } else {
         return planetId == 99 ? "Sol" : "unknown";
      }
   }
}
