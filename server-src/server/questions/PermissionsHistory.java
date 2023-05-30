package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PermissionsHistory extends Question implements CounterTypes {
   private static final Logger logger = Logger.getLogger(PlanBridgeQuestion.class.getName());

   public PermissionsHistory(Creature aResponder, long aTarget) {
      super(aResponder, "History of Permission Changes", getQuestion(aTarget), 126, aTarget);
   }

   public static String getQuestion(long aTarget) {
      int ct = WurmId.getType(aTarget);
      if (ct == 2) {
         try {
            Item item = Items.getItem(aTarget);
            if (item.isBoat()) {
               return "History of this Ship's Permissions changes";
            } else if (item.isBed()) {
               return "History of this Bed's Permissions changes";
            } else if (item.getTemplateId() == 186) {
               return "History of this Small Cart's Permissions changes";
            } else if (item.getTemplateId() == 539) {
               return "History of this Large Cart's Permissions changes";
            } else if (item.getTemplateId() == 850) {
               return "History of this Large Wagon's Permissions changes";
            } else if (item.getTemplateId() == 853) {
               return "History of this Large Ship Carrier's Permissions changes";
            } else {
               return item.getTemplateId() == 1410
                  ? "History of this Creature Transporter's Permission changes"
                  : "History of this Item's Permissions changes";
            }
         } catch (NoSuchItemException var4) {
            return "History of this not found Item's Permissions changes";
         }
      } else if (ct == 4) {
         return "History of this Building's Permissions changes";
      } else if (ct == 5) {
         return "History of this Door's Permissions changes";
      } else if (ct == 7) {
         return "History of this Gate's Permissions changes";
      } else if (ct == 1) {
         try {
            Creature creature = Creatures.getInstance().getCreature(aTarget);
            if (creature.isWagoner()) {
               return "History of this Wagoner's Permissions changes";
            }
         } catch (NoSuchCreatureException var5) {
         }

         return "History of this Creature's Permissions changes";
      } else {
         return "History of this Mine Door's Permissions changes";
      }
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 126) {
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeaderWithScrollAndQuestion());
      buf.append("label{text=\"\"}");
      String[] histories = PermissionsHistories.getPermissionsHistoryFor(this.target).getHistory(100);
      if (histories.length == 0) {
         buf.append("label{text=\"No History found!\"}");
      } else {
         for(String history : histories) {
            buf.append("label{text=\"" + history + "\"}");
         }
      }

      buf.append("label{text=\"\"}");
      buf.append(this.createAnswerButton3());
      this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
