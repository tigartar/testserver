package com.wurmonline.server.questions;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageSettingsManageQuestion extends Question implements VillageStatus, TimeConstants {
   private static final Logger logger = Logger.getLogger(VillageSettingsManageQuestion.class.getName());

   public VillageSettingsManageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 10, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseVillageSettingsManageQuestion(this);
   }

   @Override
   public void sendQuestion() {
      try {
         Village village;
         if (this.target == -10L) {
            village = this.getResponder().getCitizenVillage();
            if (village == null) {
               throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
            }
         } else {
            Item deed = Items.getItem(this.target);
            int villageId = deed.getData2();
            village = Villages.getVillage(villageId);
         }

         StringBuilder buf = new StringBuilder(this.getBmlHeaderWithScroll());
         if (village.isDisbanding()) {
            long timeleft = village.getDisbanding() - System.currentTimeMillis();
            String times = Server.getTimeFor(timeleft);
            buf.append("text{type='bold';text='This village is disbanding'}");
            if (timeleft > 0L) {
               buf.append("text{type='bold';text=\"Eta: " + times + ".\"};text{text=''};");
            } else {
               buf.append("text{type='bold';text='Eta:  any minute now.'};text{text=''};");
            }
         }

         if (!village.isDisbanding() && village.mayChangeName() && village.getRoleFor(this.getResponder()).mayResizeSettlement()) {
            buf.append("header{text=\"Settlement Name:\"};input{maxchars=\"40\";id=\"vname\";text=\"" + village.getName() + "\"}");
            buf.append(
               "text{type=\"bold\";color=\"255,50,0\";text=\"NOTE: Changing the name will"
                  + (Servers.localServer.isFreeDeeds() ? "" : " cost 5 silver,")
                  + " remove all the faith bonuses, and lock the name for 6 months.\"}"
            );
         } else {
            buf.append("header{text=\"Settlement Name\"};text{text=\"" + village.getName() + "\"}");
            buf.append("passthrough{id=\"vname\";text=\"" + village.getName() + "\"}");
         }

         buf.append("header{text=\"Settlement motto:\"}");
         buf.append("input{maxchars=\"100\";id=\"motto\"; text=\"" + village.getMotto() + "\"}");
         buf.append("header{text=\"Settlement message of the day:\"}");
         buf.append("input{maxchars=\"200\";id=\"motd\"; text=\"" + village.getMotd() + "\"}");
         buf.append("text{type=\"bold\";text=\"Politics:\"}");
         if ((
               !village.isDemocracy()
                  || !village.getFounderName().equals(this.getResponder().getName())
                  || village.getMayor().getId() != this.getResponder().getWurmId()
            )
            && this.getResponder().getPower() < 4) {
            buf.append("checkbox{id=\"democracy\";text=\"Mark this if you want to make this settlement a permanent democracy (founding mayors can revert):\"}");
         } else {
            buf.append(
               "checkbox{id=\"nondemocracy\";text=\"Mark this if you want to make this settlement a non-democracy so you cannot be removed from office: \"}"
            );
         }

         String ch = village.allowsAggCreatures() ? ";selected='true'" : "";
         buf.append("text{type=\"bold\";text=\"Aggressive creatures:\"}");
         buf.append("checkbox{id=\"aggros\"" + ch + ";text=\"Mark this if you want guards to ignore aggressive creatures\"}");
         ch = village.unlimitedCitizens ? ";selected='true'" : "";
         buf.append("text{type=\"bold\";text=\"Unlimited citizens:\"}");
         buf.append(
            "checkbox{id=\"unlimitC\""
               + ch
               + ";text=\"Mark this if you want to be able to recruit more than "
               + village.getMaxCitizens()
               + " citizens.\"}label{text=\"Your upkeep costs are doubled as long as you have more than that amount of citizens.\"}"
         );
         if (Features.Feature.HIGHWAYS.isEnabled()) {
            boolean hasHighway = village.hasHighway();
            boolean hasKOS = village.getReputations().length > 0;
            String disable = ";enabled=\"false\"";
            String hover = "";
            String en = "";
            ch = "";
            if (village.isHighwayAllowed()) {
               ch = ";selected='true'";
               if (hasHighway) {
                  en = ";enabled=\"false\"";
                  hover = ";hover=\"Cannot disable as a highway marker is in or next to the village.\"";
               }
            } else if (hasKOS) {
               en = ";enabled=\"false\"";
               hover = ";hover=\"Cannot enable as there is an active kos.\"";
            }

            buf.append("text{type=\"bold\";text=\"Allow Highways:\"}");
            buf.append(
               "checkbox{id=\"highways\"" + ch + en + hover + ";text=\"Mark this if you want citizens to be able to make a highway to your village.\"}"
            );
            hover = "";
            en = "";
            ch = "";
            if (village.isKosAllowed()) {
               ch = ";selected='true'";
               if (hasKOS) {
                  en = ";enabled=\"false\"";
                  hover = ";hover=\"Cannot disable as there is an active kos.\"";
               }
            } else if (hasHighway) {
               en = ";enabled=\"false\"";
               hover = ";hover=\"Cannot enable as there is a highway marker in or right next to the village.\"";
            }

            buf.append("text{type=\"bold\";text=\"Allow KOS:\"}");
            buf.append("checkbox{id=\"kos\"" + ch + en + hover + ";text=\"Mark this if you want to be able to use  KOS.\"}");
            buf.append("label{text=\"Note: Allow highways and Allow KOS cannot be both enabled.\"}");
            hover = "";
            en = "";
            ch = "";
            if (village.isHighwayFound()) {
               ch = ";selected='true'";
               if (village.isPermanent) {
                  en = ";enabled=\"false\"";
                  hover = ";hover=\"Cannot disable as this is a permanent village.\"";
               }
            } else if (hasKOS) {
               en = ";enabled=\"false\"";
               hover = ";hover=\"Cannot enable as there is an active kos.\"";
            }

            buf.append("text{type=\"bold\";text=\"Highway Routing:\"}");
            buf.append(
               "checkbox{id=\"routing\""
                  + ch
                  + en
                  + hover
                  + ";text=\"Mark this if you want your village to show in the find route village list.\";hover=\"It will only show in the list if there is a route to your village.\"}"
            );
            buf.append("label{text=\"Note: This will only be available if Allow Highways is enabled.\"}");
            buf.append("label{text=\"Note: They will only be able to find a route here if you are part of the (same) highway network.\"}");
         }

         buf.append("text{text=\"\"}");
         buf.append(this.createAnswerButton3());
         this.getResponder().getCommunicator().sendBml(535, 430, true, true, buf.toString(), 200, 200, 200, this.title);
      } catch (NoSuchItemException var9) {
         logger.log(Level.WARNING, "Failed to locate village deed with id " + this.target, (Throwable)var9);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
      } catch (NoSuchVillageException var10) {
         logger.log(Level.WARNING, "Failed to locate village for deed with id " + this.target, (Throwable)var10);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
      }
   }
}
