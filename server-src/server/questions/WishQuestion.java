package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.Mailer;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WishQuestion extends Question {
   private static final Logger logger = Logger.getLogger(WishQuestion.class.getName());
   private final long coinId;
   private static final String RESPONSE1 = ". Will the gods listen?";
   private static final String RESPONSE2 = ". Do you consider yourself lucky?";
   private static final String RESPONSE3 = ". Is this your turn?";
   private static final String RESPONSE4 = ". You get the feeling that someone listens.";
   private static final String RESPONSE5 = ". Good luck!";
   private static final String RESPONSE6 = ". Will it come true?";
   private static final Random rand = new Random();
   private static final String INSERT_WISH = "INSERT INTO WISHES (PLAYER,WISH,COIN,TOFULFILL) VALUES(?,?,?,?)";

   public WishQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, long coin) {
      super(aResponder, aTitle, aQuestion, 77, aTarget);
      this.coinId = coin;
   }

   @Override
   public void answer(Properties aAnswers) {
      Item coin = null;
      Item targetItem = null;

      try {
         targetItem = Items.getItem(this.target);
      } catch (NoSuchItemException var28) {
         this.getResponder().getCommunicator().sendNormalServerMessage("You fail to locate the target!");
         return;
      }

      try {
         coin = Items.getItem(this.coinId);
         if (coin.getOwner() == this.getResponder().getWurmId() && !coin.isBanked() && !coin.mailed) {
            String key = "data1";
            String val = aAnswers.getProperty("data1");
            if (val != null && val.length() > 0) {
               String tstring = ". Will the gods listen?";
               int x = rand.nextInt(6);
               if (x == 1) {
                  tstring = ". Do you consider yourself lucky?";
               } else if (x == 2) {
                  tstring = ". Is this your turn?";
               } else if (x == 3) {
                  tstring = ". You get the feeling that someone listens.";
               } else if (x == 4) {
                  tstring = ". Good luck!";
               } else if (x == 5) {
                  tstring = ". Will it come true?";
               }

               this.getResponder().getCommunicator().sendNormalServerMessage("You wish for " + val + tstring);
               long moneyVal = (long)Economy.getValueFor(coin.getTemplateId());
               float chance = (float)moneyVal / 3.0E7F;
               float chantLevel = targetItem.getSpellCourierBonus();
               float timeBonus = WurmCalendar.isNight() ? 1.05F : 1.0F;
               float newchance = chance
                  * (targetItem.getCurrentQualityLevel() / 100.0F)
                  * (1.0F + chantLevel / 100.0F)
                  * (1.0F + coin.getCurrentQualityLevel() / 1000.0F)
                  * timeBonus;
               logger.log(
                  Level.INFO, "New chance=" + newchance + " after coin=" + chance + ", chant=" + chantLevel + " ql=" + targetItem.getCurrentQualityLevel()
               );
               boolean toFulfill = rand.nextFloat() < newchance;
               if (this.getResponder().getPower() >= 5) {
                  toFulfill = true;
               }

               Connection dbcon = null;
               PreparedStatement ps = null;

               try {
                  dbcon = DbConnector.getPlayerDbCon();
                  ps = dbcon.prepareStatement("INSERT INTO WISHES (PLAYER,WISH,COIN,TOFULFILL) VALUES(?,?,?,?)");
                  ps.setLong(1, this.getResponder().getWurmId());
                  ps.setString(2, val);
                  ps.setLong(3, moneyVal);
                  ps.setBoolean(4, toFulfill);
                  ps.executeUpdate();
               } catch (SQLException var26) {
                  logger.log(Level.WARNING, var26.getMessage(), (Throwable)var26);
               } finally {
                  DbUtilities.closeDatabaseObjects(ps, null);
                  DbConnector.returnConnection(dbcon);
               }

               Items.destroyItem(coin.getWurmId());
               if (toFulfill) {
                  try {
                     Mailer.sendMail(
                        WebInterfaceImpl.mailAccount,
                        "rolf@wurmonline.com",
                        this.getResponder().getName() + " made a wish!",
                        this.getResponder().getName() + " wants the wish " + val + " to be fulfilled!"
                     );
                  } catch (Exception var25) {
                     logger.log(Level.WARNING, var25.getMessage(), (Throwable)var25);
                  }
               }
            } else {
               this.getResponder().getCommunicator().sendNormalServerMessage("You make no wish this time.");
            }
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the " + coin.getName() + "!");
         }
      } catch (NoSuchItemException var29) {
         this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the coin!");
      } catch (NotOwnedException var30) {
         this.getResponder().getCommunicator().sendNormalServerMessage("You are no longer in possesion of the coin!");
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("harray{label{text='What is your wish?'};input{maxchars='40';id='data1'; text=''}}");
      buf.append("label{text=\"Just leave it blank if you don't want to lose your coin.\"}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
