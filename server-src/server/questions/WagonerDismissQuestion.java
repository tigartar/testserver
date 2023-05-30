package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WagonerDismissQuestion extends Question {
   private static final Logger logger = Logger.getLogger(WagonerDismissQuestion.class.getName());
   private final Wagoner wagoner;

   public WagonerDismissQuestion(Creature aResponder, Wagoner wagoner) {
      super(aResponder, "Dismiss " + wagoner.getName() + " question", "Dismiss " + wagoner.getName() + " question", 149, wagoner.getWurmId());
      this.wagoner = wagoner;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 149) {
            boolean close = this.getBooleanProp("close");
            if (close) {
               return;
            }

            boolean dismiss = this.getBooleanProp("dismiss");
            if (dismiss) {
               if (this.wagoner.getVillageId() == -1) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("Wagoner is already dismissing!");
               } else {
                  try {
                     Village village = Villages.getVillage(this.wagoner.getVillageId());
                     village.deleteWagoner(this.wagoner.getCreature());
                  } catch (NoSuchVillageException var5) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find wagoner's village!");
                     logger.log(Level.WARNING, "Cannot find wagoner's village!", (Throwable)var5);
                  }
               }
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());

      try {
         Village village = Villages.getVillage(this.wagoner.getVillageId());
         int waiting = Delivery.countWaitingForAccept(this.wagoner.getWurmId());
         int queued = Delivery.getQueueLength(this.wagoner.getWurmId());
         boolean onDelivery = this.wagoner.getDeliveryId() != -10L;
         boolean atCamp = this.wagoner.isIdle();
         buf.append("label{text=\"\"}");
         buf.append("label{text=\"You are about to dismiss " + this.wagoner.getName() + ".\"}");
         buf.append("label{text=\"This will do the following:\"}");
         buf.append("label{text=\"1. They will be kicked out of the village '" + village.getName() + "'.\"}");
         buf.append("label{text=\"\"}");
         if (waiting == 0) {
            buf.append("label{text=\"2. Luckily " + this.wagoner.getName() + " does not have any deliveries waiting to be accepted.\"}");
            buf.append("label{text=\"   a. But if there were any, they would have been auto-rejected.\"}");
         } else {
            buf.append("label{text=\"2. The " + waiting + (waiting == 1 ? " delivery" : " deliveries") + " waiting to be accepted will be auto-rejected.\"}");
            buf.append("label{text=\"   a. Note: this will NOT unseal the associated containers.\"}");
         }

         buf.append("label{text=\"\"}");
         if (queued == 0) {
            buf.append("label{text=\"3. Luckily " + this.wagoner.getName() + " does not have any deliveries queued.\"}");
            buf.append("label{text=\"   a. But if they did have some then the sender of the queued deliveries would have to pick a different wagoner.\"}");
         } else {
            buf.append(
               "label{text=\"3. The "
                  + queued
                  + (queued == 1 ? " delivery" : " deliveries")
                  + " in "
                  + this.wagoner.getName()
                  + "'s queue will be unassigned.\"}"
            );
            buf.append("label{text=\"   a. The sender of these deliveries will have to pick another wagoner for each of the deliveries.\"}");
         }

         buf.append("label{text=\"\"}");
         if (onDelivery) {
            buf.append("label{text=\"4. As " + this.wagoner.getName() + " is performing a delivery then: \"}");
            buf.append("label{text=\"   a. They will contine on that delivery.\"}");
            buf.append("label{text=\"   b. When the delivery is complete, they will drive back to their home waystone.\"}");
         } else {
            buf.append("label{text=\"4. If " + this.wagoner.getName() + " was performing a delivery (which they are not) then: \"}");
            buf.append("label{text=\"   a. They would have continued on that delivery.\"}");
            buf.append("label{text=\"   b. And when the delivery was complete, they would have driven back to their home waystone.\"}");
         }

         buf.append("label{text=\"\"}");
         buf.append("label{text=\"5. " + (atCamp ? "As" : "When") + " the wagoner is back at camp then\"}");
         buf.append("label{text=\"   a. They will pack up their camp and vanish.\"}");
         buf.append("label{text=\"   b. The contract can then be used elsewhere.\"}");
         buf.append("label{text=\"\"}");
         buf.append(
            "harray{button{text=\"Dismiss\";id=\"dismiss\";hover=\"This will dismiss "
               + this.wagoner.getName()
               + "\";confirm=\"You are about to dismiss "
               + this.wagoner.getName()
               + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};button{text=\"Close\";id=\"close\";}"
         );
      } catch (NoSuchVillageException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
         buf.append("harray{button{text=\"Close\";id=\"close\";}");
      }

      buf.append("text=\"\"}");
      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(560, 460, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
