package com.wurmonline.server.behaviours;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TextInputQuestion;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.List;

public final class GravestoneBehaviour extends ItemBehaviour {
   public GravestoneBehaviour() {
      super((short)48);
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      if (action == 506) {
         return readInscription(performer, target);
      } else if (action != 177 && action != 178 && action != 181 && action != 99) {
         if (action == 1) {
            performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
            target.sendEnchantmentStrings(performer.getCommunicator());
            sendInscription(performer, target);
            return true;
         } else {
            return super.action(act, performer, target, action, counter);
         }
      } else if (canManipulateGrave(target, performer)) {
         return MethodsItems.moveItem(performer, target, counter, action, act);
      } else {
         performer.getCommunicator().sendNormalServerMessage("You may not push, pull or turn that item.");
         return true;
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      if (action == 505) {
         return inscribe(performer, source, target);
      } else if (action == 506) {
         return this.action(act, performer, target, action, counter);
      } else {
         if (action == 192) {
            if (target.creationState == 0) {
               return MethodsItems.improveItem(act, performer, source, target, counter);
            }

            int tid = MethodsItems.getItemForImprovement(target.getMaterial(), target.creationState);
            if (source.getTemplateId() == tid) {
               return MethodsItems.polishItem(act, performer, source, target, counter);
            }
         } else {
            if (action == 83 || action == 180) {
               if (!performer.mayDestroy(target) && performer.getPower() < 2) {
                  return true;
               }

               return MethodsItems.destroyItem(action, performer, source, target, false, counter);
            }

            if (action == 177 || action == 178 || action == 181 || action == 99 || action == 1) {
               return this.action(act, performer, target, action, counter);
            }

            if (action == 179) {
               summon(performer, source, target);
               return true;
            }

            if (action == 91) {
               if ((source.getTemplateId() == 176 || source.getTemplateId() == 315) && performer.getPower() >= 2) {
                  float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0F;
                  performer.getStatus().refresh(nut, false);
               }

               return true;
            }

            if (action != 503) {
               return super.action(act, performer, source, target, action, counter);
            }

            if (performer.getPower() >= 2 && (source.getTemplateId() == 176 || source.getTemplateId() == 315)) {
               Methods.sendCreateZone(performer);
            }
         }

         return true;
      }
   }

   private static final boolean canManipulateGrave(Item grave, Creature performer) {
      if (grave.lastOwner == performer.getWurmId()) {
         return true;
      } else if (performer.getPower() >= 2) {
         return true;
      } else {
         VolaTile t = Zones.getTileOrNull(grave.getTileX(), grave.getTileY(), grave.isOnSurface());
         return t != null && t.getVillage() != null && t.getVillage().isCitizen(performer);
      }
   }

   private static final boolean inscribe(Creature performer, Item chisel, Item gravestone) {
      if (chisel == null) {
         performer.getCommunicator().sendNormalServerMessage("You fumble with the " + chisel + " but you cannot figure out how it works.");
         return true;
      } else {
         InscriptionData inscriptionData = gravestone.getInscription();
         if (!gravestone.canHaveInscription()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot inscribe on that!");
            return true;
         } else if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
            performer.getCommunicator()
               .sendNormalServerMessage("This " + gravestone.getName() + " has already been inscribed by " + inscriptionData.getInscriber() + ".");
            return true;
         } else {
            int numberOfChars = (int)(gravestone.getQualityLevel() * 2.0F);
            TextInputQuestion tiq = new TextInputQuestion(
               performer,
               "Inscribing a message on " + gravestone.getName() + ".",
               "Inscribing is an irreversible process. Enter your important message here:",
               2,
               gravestone.getWurmId(),
               numberOfChars,
               false
            );
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " starts to inscribe with " + chisel.getName() + " on " + gravestone.getNameWithGenus() + ".", performer, 5
               );
            tiq.sendQuestion();
            return true;
         }
      }
   }

   private static final boolean readInscription(Creature performer, Item gravestone) {
      InscriptionData inscriptionData = gravestone.getInscription();
      if (inscriptionData != null) {
         SimplePopup pp = new SimplePopup(performer, gravestone.getName(), inscriptionData.getInscription());
         performer.getCommunicator().sendNormalServerMessage("You read the " + gravestone.getName() + ".");
         pp.sendQuestion("Close");
      } else {
         performer.getCommunicator().sendNormalServerMessage("There was no inscription to read.");
      }

      return true;
   }

   private static void summon(Creature performer, Item wand, Item target) {
      int stid = wand.getTemplateId();
      if ((stid == 176 || stid == 315) && performer.getPower() >= 2) {
         try {
            Zone currZone = Zones.getZone((int)target.getPosX() >> 2, (int)target.getPosY() >> 2, target.isOnSurface());
            currZone.removeItem(target);
            target.putItemInfrontof(performer);
         } catch (NoSuchZoneException var5) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the zone for that item. Failed to summon.");
         } catch (NoSuchCreatureException var6) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
         } catch (NoSuchItemException var7) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the item for that request! Failed to summon.");
         } catch (NoSuchPlayerException var8) {
            performer.getCommunicator().sendNormalServerMessage("Failed to locate the creature for that request.. you! Failed to summon.");
         }
      }
   }

   private static void sendInscription(Creature performer, Item gravestone) {
      InscriptionData inscriptionData = gravestone.getInscription();
      if (inscriptionData != null) {
         String inscription = inscriptionData.getInscription();
         if (inscription.length() > 0) {
            performer.getCommunicator().sendNormalServerMessage("There is an inscription carved into the gravestone.");
            performer.getCommunicator().sendNormalServerMessage(inscription);
         }
      }
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      if (target.getTemplateId() == 822) {
         InscriptionData inscriptionData = target.getInscription();
         if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
            toReturn.add(Actions.actionEntrys[506]);
         }
      }

      if (toReturn.contains(Actions.actionEntrys[59]) && !canManipulateGrave(target, performer)) {
         toReturn.remove(Actions.actionEntrys[59]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      if (target.getTemplateId() == 822) {
         InscriptionData inscriptionData = target.getInscription();
         if (source.getTemplateId() == 97
            && target.canHaveInscription()
            && (inscriptionData == null || !inscriptionData.hasBeenInscribed() || performer.getPower() >= 2)) {
            toReturn.add(Actions.actionEntrys[505]);
         }

         if (inscriptionData != null && inscriptionData.hasBeenInscribed()) {
            toReturn.add(Actions.actionEntrys[506]);
         }
      }

      if (toReturn.contains(Actions.actionEntrys[59]) && !canManipulateGrave(target, performer)) {
         toReturn.remove(Actions.actionEntrys[59]);
      }

      return toReturn;
   }
}
