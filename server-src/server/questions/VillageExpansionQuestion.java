package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageExpansionQuestion extends Question implements VillageStatus, ItemTypes {
   private static final Logger logger = Logger.getLogger(VillageExpansionQuestion.class.getName());
   private final Item token;

   public VillageExpansionQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Item aToken) {
      super(aResponder, aTitle, aQuestion, 12, aTarget);
      this.token = aToken;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseVillageExpansionQuestion(this);
   }

   public Item getToken() {
      return this.token;
   }

   @Override
   public void sendQuestion() {
      int villid = this.token.getData2();

      try {
         Item deed = Items.getItem(this.target);
         int oldVill = deed.getData2();
         if (oldVill != -1) {
            try {
               this.getResponder()
                  .getCommunicator()
                  .sendSafeServerMessage("This is the deed for " + Villages.getVillage(oldVill).getName() + "! You cannot use it to expand a settlement!");
            } catch (NoSuchVillageException var7) {
               this.getResponder().getCommunicator().sendSafeServerMessage("This deed already is already used! You cannot use it to expand this settlement!");
            }

            return;
         }

         Village village = Villages.getVillage(villid);
         StringBuilder buf = new StringBuilder(this.getBmlHeader());
         if (village != null) {
            int size = Villages.getSizeForDeed(deed.getTemplateId());
            buf.append(
               "text{text='The expansion will set the size of the settlement to "
                  + size
                  + " tiles out in all directions from the "
                  + this.token.getName()
                  + ".'}"
            );
            buf.append("text{text='You will require all the house deeds for any houses in the new area.'}");
            buf.append(
               "text{text='Also note that in the case that the allowed number of citizens is decreased any surplus will be kicked from the settlement automatically and in no particular order so you may want to do that manually instead.'}"
            );
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
         } else {
            this.getResponder().getCommunicator().sendSafeServerMessage("This token has no settlement associated with it. It cannot be expanded.");
         }
      } catch (NoSuchItemException var8) {
         logger.log(Level.WARNING, "Failed to locate settlement with id " + this.target, (Throwable)var8);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
      } catch (NoSuchVillageException var9) {
         logger.log(Level.WARNING, "Failed to locate settlement with id " + villid, (Throwable)var9);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
      }
   }
}
