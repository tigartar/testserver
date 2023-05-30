package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.spells.SpellResist;
import java.util.Properties;

public class LocatePlayerQuestion extends Question {
   private boolean properlySent = false;
   private boolean override = false;
   private double power;

   public LocatePlayerQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, boolean eyeVyn, double power) {
      super(aResponder, aTitle, aQuestion, 79, aTarget);
      if (eyeVyn) {
         this.override = true;
      }

      this.power = power;
   }

   public static String locatePlayerString(int targetDistance, String name, String direction, int maxDistance) {
      if (targetDistance > maxDistance) {
         return "No such soul found.";
      } else {
         String toReturn = "";
         if (targetDistance == 0) {
            toReturn = toReturn + "You are practically standing on the " + name + "! ";
         } else if (targetDistance < 1) {
            toReturn = toReturn + "The " + name + " is " + direction + " a few steps away! ";
         } else if (targetDistance < 4) {
            toReturn = toReturn + "The " + name + " is " + direction + " a stone's throw away! ";
         } else if (targetDistance < 6) {
            toReturn = toReturn + "The " + name + " is " + direction + " very close. ";
         } else if (targetDistance < 10) {
            toReturn = toReturn + "The " + name + " is " + direction + " pretty close by. ";
         } else if (targetDistance < 20) {
            toReturn = toReturn + "The " + name + " is " + direction + " fairly close by. ";
         } else if (targetDistance < 50) {
            toReturn = toReturn + "The " + name + " is some distance away " + direction + ". ";
         } else if (targetDistance < 200) {
            toReturn = toReturn + "The " + name + " is quite some distance away " + direction + ". ";
         } else if (targetDistance < 500) {
            toReturn = toReturn + "The " + name + " is rather a long distance away " + direction + ". ";
         } else if (targetDistance < 1000) {
            toReturn = toReturn + "The " + name + " is pretty far away " + direction + ". ";
         } else if (targetDistance < 2000) {
            toReturn = toReturn + "The " + name + " is far away " + direction + ". ";
         } else {
            toReturn = toReturn + "The " + name + " is very far away " + direction + ". ";
         }

         return toReturn;
      }
   }

   @Override
   public void answer(Properties aAnswers) {
      if (this.properlySent) {
         boolean found = false;
         String name = aAnswers.getProperty("name");
         if (name != null && name.length() > 1) {
            found = locateCorpse(name, this.getResponder(), this.power, this.override);
         }

         if (!found) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such soul found.");
         }
      }
   }

   public static boolean locateCorpse(String name, Creature responder, double power, boolean overrideNolocate) {
      boolean found = false;
      PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
      if (pinf != null && pinf.loaded) {
         if (pinf.getPower() > responder.getPower()) {
            return false;
         } else {
            try {
               Creature player = Server.getInstance().getCreature(pinf.wurmId);
               boolean nolocation = (double)player.getBonusForSpellEffect((byte)29) >= power;
               if (!nolocation) {
                  nolocation = SpellResist.getSpellResistance(player, 451) < 1.0;
               }

               if (!nolocation || overrideNolocate) {
                  int maxDistance = Integer.MAX_VALUE;
                  if (Servers.isThisAnEpicOrChallengeServer()) {
                     maxDistance = 200;
                  } else if (Servers.isThisAChaosServer()) {
                     maxDistance = 500;
                  }

                  found = true;
                  int centerx = player.getTileX();
                  int centery = player.getTileY();
                  int dx = Math.abs(centerx - responder.getTileX());
                  int dy = Math.abs(centery - responder.getTileY());
                  int mindist = (int)Math.sqrt((double)(dx * dx + dy * dy));
                  int dir = MethodsCreatures.getDir(responder, centerx, centery);
                  float bon = player.getNoLocateItemBonus(mindist <= maxDistance);
                  if (bon > 0.0F && 1.0 + power < (double)bon) {
                     found = false;
                  }

                  if (found) {
                     String direction = MethodsCreatures.getLocationStringFor(responder.getStatus().getRotation(), dir, "you");
                     String toReturn = locatePlayerString(mindist, player.getName(), direction, maxDistance);
                     responder.getCommunicator().sendNormalServerMessage(toReturn);
                     if (bon > 0.0F && responder.getKingdomId() != player.getKingdomId()) {
                        SpellResist.addSpellResistance(player, 451, (double)bon);
                     }
                  }
               }
            } catch (NoSuchPlayerException | NoSuchCreatureException var19) {
            }

            Item[] its = Items.getAllItems();

            for(int itx = 0; itx < its.length; ++itx) {
               if (its[itx].getZoneId() > -1 && its[itx].getTemplateId() == 272 && its[itx].getName().equals("corpse of " + pinf.getName())) {
                  found = true;
                  int centerx = its[itx].getTileX();
                  int centery = its[itx].getTileY();
                  int mindist = Math.max(Math.abs(centerx - responder.getTileX()), Math.abs(centery - responder.getTileY()));
                  if (responder.getPower() <= 0) {
                     int dir = MethodsCreatures.getDir(responder, centerx, centery);
                     String direction = MethodsCreatures.getLocationStringFor(responder.getStatus().getRotation(), dir, "you");
                     String toReturn = EndGameItems.getDistanceString(mindist, its[itx].getName(), direction, false);
                     if (!its[itx].isOnSurface()) {
                        responder.getCommunicator().sendNormalServerMessage(toReturn + " It lies below ground.");
                     } else {
                        responder.getCommunicator().sendNormalServerMessage(toReturn);
                     }
                  } else {
                     responder.getCommunicator()
                        .sendNormalServerMessage(its[itx].getName() + " at " + centerx + ", " + centery + " surfaced=" + its[itx].isOnSurface());
                  }
               }
            }

            return found;
         }
      } else {
         return false;
      }
   }

   @Override
   public void sendQuestion() {
      boolean ok = true;
      if (this.getResponder().getPower() <= 0) {
         try {
            ok = false;
            Action act = this.getResponder().getCurrentAction();
            if (act.getNumber() == 419 || act.getNumber() == 118) {
               ok = true;
            }
         } catch (NoSuchActionException var4) {
         }
      }

      if (!ok) {
         try {
            Item arti = Items.getItem(this.target);
            if (arti.getTemplateId() == 332 && arti.getOwnerId() == this.getResponder().getWurmId()) {
               ok = true;
            }
         } catch (NoSuchItemException var3) {
         }
      }

      if (ok) {
         this.properlySent = true;
         StringBuilder sb = new StringBuilder();
         sb.append(this.getBmlHeader());
         sb.append("text{text='Which soul do you wish to locate?'};");
         sb.append("label{text='Name:'};input{id='name';maxchars='40';text=\"\"};");
         sb.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(300, 300, true, true, sb.toString(), 200, 200, 200, this.title);
      }
   }
}
