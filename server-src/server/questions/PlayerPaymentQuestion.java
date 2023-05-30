package com.wurmonline.server.questions;

import com.wurmonline.server.Features;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.players.Player;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public final class PlayerPaymentQuestion extends Question implements MonetaryConstants {
   public static final long silverCost = 10L;
   public static final long silverCostFirstTime = 2L;
   public static final long silverCost15Day = 5L;

   public PlayerPaymentQuestion(Creature aResponder) {
      super(aResponder, "Purchase Premium Time", "Choose an option from the below:", 20, aResponder.getWurmId());
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      long money = this.getResponder().getMoney();
      if (((Player)this.getResponder()).getSaveFile().getPaymentExpire() > 0L && !this.getResponder().hasFlag(63)) {
         String purchaseStr = answers.getProperty("purchase");
         if ("30day".equals(purchaseStr)) {
            if (money >= 100000L) {
               try {
                  if (this.getResponder().chargeMoney(100000L)) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     lsw.addPlayingTime(this.getResponder(), this.getResponder().getName(), 0, 30, System.currentTimeMillis() + Servers.localServer.name);
                     this.getResponder()
                        .getCommunicator()
                        .sendSafeServerMessage(
                           "Your request for playing time is being processed. It may take up to half an hour until the system is fully updated."
                        );
                     Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + 30000L);
                     logger.log(
                        Level.INFO,
                        this.getResponder().getName() + " purchased 1 month premium time for " + 10L + " silver coins. " + 30000L + " iron added to king."
                     );
                  } else {
                     this.getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 10 silvers. Please try later.");
                  }
               } catch (IOException var9) {
                  this.getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
               }
            } else {
               this.getResponder()
                  .getCommunicator()
                  .sendNormalServerMessage("You need at least 10 silver in your account to purchase 30 days of premium game time.");
            }
         } else if ("15day".equals(purchaseStr)) {
            if (money >= 50000L) {
               try {
                  if (this.getResponder().chargeMoney(50000L)) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     lsw.addPlayingTime(this.getResponder(), this.getResponder().getName(), 0, 15, System.currentTimeMillis() + Servers.localServer.name);
                     this.getResponder()
                        .getCommunicator()
                        .sendSafeServerMessage(
                           "Your request for playing time is being processed. It may take up to half an hour until the system is fully updated."
                        );
                     Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + -20000L);
                     logger.log(
                        Level.INFO,
                        this.getResponder().getName() + " purchased 1 month premium time for " + 5L + " silver coins. " + -20000L + " iron added to king."
                     );
                  } else {
                     this.getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 10 silvers. Please try later.");
                  }
               } catch (IOException var8) {
                  this.getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
               }
            } else {
               this.getResponder()
                  .getCommunicator()
                  .sendNormalServerMessage("You need at least 5 silver in your account to purchase 15 days of premium game time.");
            }
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to buy any premium game time for now.");
         }
      } else if (money < 20000L) {
         this.getResponder().getCommunicator().sendNormalServerMessage("You need at least 2 silver in your account to purchase premium game time.");
      } else {
         boolean purchaseFirstTime = Boolean.parseBoolean(answers.getProperty("purchaseFirstTime"));
         long referredBy = ((Player)this.getResponder()).getSaveFile().referrer;
         if (purchaseFirstTime && referredBy == 0L) {
            try {
               if (this.getResponder().chargeMoney(20000L)) {
                  LoginServerWebConnection lsw = new LoginServerWebConnection();
                  lsw.addPlayingTime(
                     this.getResponder(),
                     this.getResponder().getName(),
                     0,
                     30,
                     "firstBuy" + (System.currentTimeMillis() - 1400000000000L) + Servers.localServer.name
                  );
                  this.getResponder()
                     .getCommunicator()
                     .sendSafeServerMessage(
                        "Your request for playing time is being processed. It may take up to half an hour until the system is fully updated."
                     );
                  ((Player)this.getResponder()).getSaveFile().setReferedby(this.getResponder().getWurmId());
                  this.getResponder().setFlag(63, false);
               } else {
                  this.getResponder().getCommunicator().sendAlertServerMessage("Failed to charge you 2 silvers. Please try later.");
               }
            } catch (IOException var10) {
               this.getResponder().getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
            }
         } else if (purchaseFirstTime && referredBy != 0L) {
            this.getResponder()
               .getCommunicator()
               .sendNormalServerMessage(
                  "You have already purchased this option once, if you still have not received your play time after 30 minutes, please contact /support."
               );
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to buy any premium game time for now.");
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      long money = this.getResponder().getMoney();
      Change change = Economy.getEconomy().getChangeFor(money);
      buf.append(this.getBmlHeader());
      if (Features.Feature.RETURNER_PACK_REGISTRATION.isEnabled() && this.getResponder().hasFlag(47)) {
         buf.append("text{text='You are successfully registered for the returner pack!'}");
      }

      if (((Player)this.getResponder()).getSaveFile().getPaymentExpire() > 0L && !this.getResponder().hasFlag(63)) {
         if (money < 50000L) {
            buf.append("text{text='To purchase more premium game time you will need at least 5 silver in your bank account.'}");
            buf.append("text{text=''}");
            buf.append("text{text='You currently only have " + change.getChangeString() + " in your account.'}");
         } else {
            buf.append("text{text='You may purchase another 30 days of premium playing time for 10 silver, or 15 days of premium playing time for 5 silver.'}");
            buf.append("text{text=''}");
            buf.append("text{text='You currently have " + change.getChangeString() + " in your account.'}");
            buf.append("text{text=''}");
            buf.append("label{text=\"Purchase Premium Time?\"};");
            if (money >= 100000L) {
               buf.append("radio{group='purchase';id='30day';selected='false';text='30 days for 10 silver'};");
            }

            buf.append("radio{group='purchase';id='15day';selected='false';text='15 days for 5 silver'};");
            buf.append("radio{group='purchase';id='none';selected='true';text='Nothing'};");
         }
      } else if (money < 20000L) {
         buf.append("text{text='As this is your first time purchasing premium game time, you will need at least 2 silver in your bank account.'}");
         buf.append("text{text=''}");
         buf.append("text{text='You currently only have " + change.getChangeString() + " in your account.'}");
      } else {
         buf.append(
            "text{text='As this is your first time purchasing premium game time, you may purchase 30 days for 2 silver. After this first time the price will become 5 silver for 15 days, or 10 silver for 30 days.'}"
         );
         buf.append("text{text=''}");
         buf.append("text{text='You currently have " + change.getChangeString() + " in your account.'}");
         buf.append("text{text=''}");
         buf.append("checkbox{id='purchaseFirstTime'; selected='true'; text='Purchase 30 days of premium playing time for 2 silver.'}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
