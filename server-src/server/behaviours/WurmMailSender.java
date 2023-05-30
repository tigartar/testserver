package com.wurmonline.server.behaviours;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.questions.MailReceiveQuestion;
import com.wurmonline.server.questions.MailSendQuestion;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import java.util.Set;

final class WurmMailSender implements MonetaryConstants, MiscConstants {
   private WurmMailSender() {
   }

   static final void checkForWurmMail(Creature performer, Item mailbox) {
      if (mailbox.getOwnerId() == -10L) {
         if (performer.isWithinDistanceTo(mailbox.getPosX(), mailbox.getPosY(), mailbox.getPosZ(), 4.0F)) {
            BlockingResult result = Blocking.getBlockerBetween(performer, mailbox, 4);
            if (result == null) {
               if (mailbox.getSpellCourierBonus() > 0.0F) {
                  if (!mailbox.hasDarkMessenger() && !mailbox.hasCourier()) {
                     performer.getCommunicator().sendNormalServerMessage("The entities inside refuse to help you.");
                  } else {
                     Set<WurmMail> set = WurmMail.getSentMailsFor(performer.getWurmId(), 100);
                     if (!set.isEmpty()) {
                        new MailReceiveQuestion(performer, "Retrieving mail", "Which items do you wish to retrieve?", mailbox).sendQuestion();
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You have no mail.");
                     }
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You have heard rumours that the mailbox will need some kind of enchantment to work.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You can't reach the " + mailbox.getName() + " now.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You are too far away to do that now.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The mailbox must be planted on the ground.");
      }
   }

   static final void sendWurmMail(Creature performer, Item mailbox) {
      if (mailbox.getOwnerId() == -10L) {
         if (performer.isWithinDistanceTo(mailbox.getPosX(), mailbox.getPosY(), mailbox.getPosZ(), 4.0F)) {
            BlockingResult result = Blocking.getBlockerBetween(performer, mailbox, 4);
            if (result == null) {
               if (mailbox.getSpellCourierBonus() > 0.0F) {
                  if (!mailbox.hasDarkMessenger() && !mailbox.hasCourier()) {
                     performer.getCommunicator().sendNormalServerMessage("The entities inside refuse to help you.");
                  } else {
                     boolean ok = true;
                     Item[] containedItems = mailbox.getItemsAsArray();
                     if (containedItems.length != 0) {
                        for(Item lContainedItem : containedItems) {
                           if (!lContainedItem.isNoDrop()
                              && !lContainedItem.isArtifact()
                              && !lContainedItem.isBodyPart()
                              && !lContainedItem.isTemporary()
                              && !lContainedItem.isLiquid()) {
                              if (lContainedItem.lastOwner != performer.getWurmId()) {
                                 performer.getCommunicator()
                                    .sendNormalServerMessage("You must possess the " + lContainedItem.getName() + " in order to send it.");
                                 ok = false;
                              }
                           } else {
                              performer.getCommunicator().sendNormalServerMessage("You may not send the " + lContainedItem.getName() + ".");
                              ok = false;
                           }
                        }

                        if (ok) {
                           new MailSendQuestion(performer, "Sending mail", "Calculate the cost", mailbox).sendQuestion();
                        }
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("The " + mailbox.getName() + " is empty.");
                     }
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You have heard rumours that the mailbox will need some kind of enchantment to work.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You can't reach the " + mailbox.getName() + " now.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You are too far away to do that now.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The mailbox must be planted on the ground.");
      }
   }
}
