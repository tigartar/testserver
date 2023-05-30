package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import java.text.NumberFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VillageUpkeep extends Question implements VillageStatus, TimeConstants, MonetaryConstants {
   private static final Logger logger = Logger.getLogger(VillageUpkeep.class.getName());
   private static final NumberFormat nf = NumberFormat.getInstance();

   public VillageUpkeep(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 120, aTarget);
      nf.setMaximumFractionDigits(6);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseVillageUpkeepQuestion(this);
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

         StringBuilder buf = new StringBuilder();
         buf.append(this.getBmlHeader());
         buf.append("header{text=\"" + village.getName() + "\"}");
         GuardPlan plan = village.plan;
         if (village.isPermanent) {
            buf.append("text{text='This village is permanent, and should never run out of money or be drained.'}");
         } else if (!Servers.localServer.isUpkeep()) {
            buf.append("text{text='There are no upkeep costs for settlements here.'}");
         } else if (plan != null) {
            if (village.isCitizen(this.getResponder()) || this.getResponder().getPower() >= 2) {
               Change c = Economy.getEconomy().getChangeFor(plan.moneyLeft);
               buf.append("text{text='The settlement has " + c.getChangeString() + " left in its coffers.'}");
               Change upkeep = Economy.getEconomy().getChangeFor(plan.getMonthlyCost());
               buf.append("text{text='Upkeep per month is " + upkeep.getChangeString() + ".'}");
               float left = (float)plan.moneyLeft / (float)plan.getMonthlyCost();
               buf.append("text{text=\"This means that the upkeep should last for about " + (int)(left * 28.0F) + " days.\"}");
               if (Servers.localServer.PVPSERVER) {
                  buf.append("text{text=\"A drain would cost " + Economy.getEconomy().getChangeFor(plan.getMoneyDrained()).getChangeString() + ".\"};");
                  if (plan.moneyLeft < 30000L) {
                     buf.append("text{type='bold';text='Since minimum drain is 75 copper it may be drained to disband in less than 5 days.'}");
                  }
               }

               if (village.isMayor(this.getResponder())
                  && Servers.localServer.isFreeDeeds()
                  && Servers.localServer.isUpkeep()
                  && village.getCreationDate() < System.currentTimeMillis() + 2419200000L) {
                  buf.append("text{text=\"\"}");
                  buf.append(
                     "text{type='bold';text='Free deeding is enabled and your settlement is less than 30 days old. You will not receive a refund if you choose to disband before your village is 30 days old.'}"
                  );
               }
            }
         } else {
            buf.append("text{text=\"No plan found!\"}");
         }

         buf.append("text{text=\"\"}");
         long money = this.getResponder().getMoney();
         if (money > 0L && (!village.isPermanent || this.getResponder().getPower() >= 2) && Servers.localServer.isUpkeep()) {
            buf.append("text{text=\"If you wish to contribute to the upkeep costs of this settlement, fill in the amount below:\"}");
            Change change = Economy.getEconomy().getChangeFor(money);
            buf.append("text{text=\"You may pay up to " + change.getChangeString() + ".\"}");
            buf.append("text{text=\"The money will be added to the settlement upkeep fund.\"}");
            buf.append(
               "text{type=\"italic\";text=\"If the settlement has more than one month worth of upkeep, there will be no decay on houses, fences, and bulk and food storage bins will not be subject to a 5% loss every 30 days. If there is less than a week, decay will be very fast and bulk and food storage bins will lose 5% of their contents every 30 days.\"};text{text=\"\"}"
            );
            long gold = change.getGoldCoins();
            long silver = change.getSilverCoins();
            long copper = change.getCopperCoins();
            long iron = change.getIronCoins();
            if (gold > 0L) {
               buf.append("harray{input{maxchars=\"10\";id=\"gold\";text=\"0\"};label{text=\"(" + gold + ") Gold coins\"}}");
            }

            if (silver > 0L || gold > 0L) {
               buf.append("harray{input{maxchars=\"10\";id=\"silver\";text=\"0\"};label{text=\"(" + silver + ") Silver coins\"}}");
            }

            if (copper > 0L || silver > 0L || gold > 0L) {
               buf.append("harray{input{maxchars=\"10\";id=\"copper\";text=\"0\"};label{text=\"(" + copper + ") Copper coins\"}}");
            }

            if (iron > 0L || copper > 0L || silver > 0L || gold > 0L) {
               buf.append("harray{input{maxchars=\"10\";id=\"iron\";text=\"0\"};label{text=\"(" + iron + ") Iron coins\"}}");
            }
         } else if (Servers.localServer.isUpkeep() && money == 0L) {
            buf.append("text{text=\"You may contribute to the upkeep costs of this settlement if you have money in the bank.\"}");
         }

         buf.append("text{text=\"\"}");
         Citizen mayor = village.getMayor();
         if (mayor != null) {
            buf.append("text{type=\"italic\";text=\"" + mayor.getName() + ", " + mayor.getRole().getName() + ", " + village.getName() + "\"};text{text=\"\"}");
         } else {
            buf.append("text{type=\"italic\";text=\"The Citizens, " + village.getName() + "\"};text{text=\"\"}");
         }

         buf.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      } catch (NoSuchItemException var15) {
         logger.log(Level.WARNING, this.getResponder().getName() + " tried to get info for null token with id " + this.target, (Throwable)var15);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
      } catch (NoSuchVillageException var16) {
         logger.log(Level.WARNING, this.getResponder().getName() + " tried to get info for null settlement for token with id " + this.target);
         this.getResponder().getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
      }
   }
}
