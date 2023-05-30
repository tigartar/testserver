package com.wurmonline.server.questions;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoyalChallenge extends Question implements TimeConstants {
   private static Logger logger = Logger.getLogger(RoyalChallenge.class.getName());

   public RoyalChallenge(Creature aResponder) {
      super(aResponder, "Challenge for power", "You have been challenged", 94, aResponder.getWurmId());
      if (aResponder.isPlayer()) {
         ((Player)aResponder).sentChallenge = true;
      }
   }

   @Override
   public void answer(Properties aAnswers) {
      if (this.getResponder().isKing()) {
         King k = King.getKing(this.getResponder().getKingdomId());
         if (k != null) {
            boolean decline = false;
            boolean accept = false;
            String key2 = "decide";
            String val2 = aAnswers.getProperty("decide");
            if (val2 != null && val2.equals("decline")) {
               decline = true;
            } else if (val2 != null && val2.equals("accept")) {
               accept = true;
            }

            if (decline) {
               this.getResponder().getCommunicator().sendNormalServerMessage("You decline the challenge.");
               k.setChallengeDeclined();
               if (k.hasFailedAllChallenges()) {
                  this.getResponder()
                     .getCommunicator()
                     .sendAlertServerMessage("The people of " + Kingdoms.getNameFor(k.kingdom) + " may now vote you from the throne at the duelling ring.");
                  HistoryManager.addHistory(this.getResponder().getName(), " may now be voted away from the throne within one week at the duelling stone.");
                  Server.getInstance()
                     .broadCastNormal(this.getResponder().getName() + " may now be voted away from the throne within one week at the duelling stone.");
                  logger.log(Level.INFO, this.getResponder().getName() + " may now be voted away.");
               }

               return;
            }

            if (!accept) {
               this.getResponder().getCommunicator().sendNormalServerMessage("You decide to wait with answering the challenge.");
               long timeLeft = 604800000L + k.getChallengeDate() - System.currentTimeMillis();
               String tl = Server.getTimeFor(timeLeft);
               this.getResponder()
                  .getCommunicator()
                  .sendNormalServerMessage(
                     "Unless you answer this challenge within "
                        + tl
                        + " you will automatically have declined "
                        + (k.getDeclinedChallengesNumber() + 1)
                        + " challenges."
                  );
               this.getResponder().getCommunicator().sendNormalServerMessage("You may bring this window up again by typing /challenge.");
               return;
            }

            String keyday = "day";
            String valday = aAnswers.getProperty("day");
            if (valday != null && valday.length() > 0) {
               try {
                  int day = Integer.parseInt(valday);
                  String keyhour = "hours";
                  String valhour = aAnswers.getProperty("hours");
                  if (valhour != null && valhour.length() > 0) {
                     try {
                        int hour = Integer.parseInt(valhour);
                        long time = System.currentTimeMillis() + (long)day * 86400000L + (long)hour * 3600000L;
                        if (Servers.localServer.testServer) {
                           this.getResponder()
                              .getCommunicator()
                              .sendSafeServerMessage(
                                 "You have accepted the challenge and since this is the test server you must be at the duelling ring exactly in 2 minutes instead of "
                                    + day
                                    + " days and "
                                    + hour
                                    + " hours (which is "
                                    + (day * 24 + hour)
                                    + " hours away). You must stay there a bit more than half an hour until you receive a message."
                              );
                           time = System.currentTimeMillis() + 120000L;
                        } else {
                           this.getResponder()
                              .getCommunicator()
                              .sendSafeServerMessage(
                                 "You have accepted the challenge and must be at the duelling ring exactly in "
                                    + day
                                    + " days and "
                                    + hour
                                    + " hours (which is "
                                    + (day * 24 + hour)
                                    + " hours away). You must stay there a bit more than half an hour until you receive a message."
                              );
                        }

                        k.setChallengeAccepted(time);
                        Message mess = new Message(
                           this.getResponder(),
                           (byte)10,
                           Kingdoms.getChatNameFor(this.getResponder().getKingdomId()),
                           "<"
                              + this.getResponder().getName()
                              + "> has accepted the challenge and must be at the duelling ring exactly in "
                              + day
                              + " days and "
                              + hour
                              + " hours"
                        );
                        Player[] playarr = Players.getInstance().getPlayers();
                        byte windowKingdom = this.getResponder().getKingdomId();

                        for(Player lElement : playarr) {
                           if (windowKingdom == lElement.getKingdomId() || lElement.getPower() > 0) {
                              lElement.getCommunicator().sendMessage(mess);
                           }
                        }
                     } catch (NumberFormatException var22) {
                        this.getResponder().getCommunicator().sendAlertServerMessage("You must select a valid hour in order to accept the challenge!");
                     }
                  } else {
                     this.getResponder().getCommunicator().sendAlertServerMessage("You must select a valid hour in order to accept the challenge!");
                  }
               } catch (NumberFormatException var23) {
                  this.getResponder().getCommunicator().sendAlertServerMessage("You must select a valid day in order to accept the challenge!");
               }
            } else {
               this.getResponder().getCommunicator().sendAlertServerMessage("You must select a valid day in order to accept the challenge!");
            }

            return;
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      if (this.getResponder().isKing()) {
         King k = King.getKing(this.getResponder().getKingdomId());
         if (k != null) {
            buf.append("text{text='Your people has challenged your rulership at the duelling ring.'};text{text=''}");
            buf.append(
               "text{text='The duelling ring is an area where your people can fight and kill eachother without harm to their reputation.'};text{text=''}"
            );
            buf.append("text{text='Eventually you have to go there in order to present yourself and answer the challenge.'}");
            buf.append("text{text='You will have to stay in the proximity of the ring for at least half an hour.'}");
            buf.append("text{text=''}");
            buf.append(
               "text{text='In case you leave the proximity or die within the proximity during this time you may be removed from the throne of "
                  + Kingdoms.getNameFor(this.getResponder().getKingdomId())
                  + ".'}"
            );
            buf.append("text{text=''}");
            buf.append(
               "text{text='You may be challenged once per week and must respond within one week or you are considered to have declined. You may decline these challenges two times but the third time you are strongly adviced to accept. In case you accept the challenge the first week nobody may challenge you for two more weeks.'}"
            );
            buf.append("text{text=''}");
            buf.append(
               "text{text='If you fail to respond to or decline the third challenge you may also be removed from the throne if enough people vote at the duelling ring.'}"
            );
            buf.append("text{text=''}");
            if (k.hasFailedToRespondToChallenge()) {
               buf.append("text{text=\"You failed to accept a challenge in time and now have to wait for a decision by your people.\"}");
            } else if (k.getChallengeAcceptedDate() > 0L) {
               if (k.getChallengeAcceptedDate() > System.currentTimeMillis()) {
                  long timeLeft = k.getChallengeAcceptedDate() - System.currentTimeMillis();
                  String tl = Server.getTimeFor(timeLeft);
                  buf.append("text{text=\"You have accepted to be at the duelling ring in " + tl + " and defend your sovereignty.\"}");
               } else {
                  buf.append("text{text=\"You have accepted to be at the duelling ring now.\"}");
               }
            } else if (k.getChallengeDate() < System.currentTimeMillis()) {
               long timeLeft = 604800000L + k.getChallengeDate() - System.currentTimeMillis();
               String tl = Server.getTimeFor(timeLeft);
               buf.append(
                  "text{text=\"Unless you answer this challenge within "
                     + tl
                     + " you will have declined "
                     + (k.getDeclinedChallengesNumber() + 1)
                     + " challenges.\"}"
               );
               if (k.getDeclinedChallengesNumber() + 1 == 3) {
                  buf.append("text{text=\"Since this would be the third time your people will be able to vote you from the throne.\"}");
               }

               buf.append("text{text=''}");
               buf.append("radio{ group='decide'; id='decline';text=\" Decline\"}");
               buf.append("radio{ group='decide'; id='accept';text=\" Accept\"}");
               buf.append("radio{ group='decide'; id='wait';text=\" Wait\";selected=\"true\"}");
               buf.append("text{text=\"If you wish to wait with this decision, you may also close this window.\"}");
               buf.append("text{text=''}");
               buf.append("text{text=\"You decide yourself when you wish to enter the duelling ring given the options below.\"}");
               if (Servers.localServer.testServer) {
                  buf.append("label{text=\"This is the test server and it will always be in 2 minutes:\"}");
                  buf.append("text{text=''}");
               }

               buf.append("label{text=\"First select in how many days (24 hour periods). Minimum is in 2 days:\"}");
               buf.append("radio{ group='day'; id='2';text='2'}");
               buf.append("radio{ group='day'; id='3';text='3'}");
               buf.append("radio{ group='day'; id='4';text='4'}");
               buf.append("radio{ group='day'; id='5';text='5'}");
               buf.append("radio{ group='day'; id='6';text='6'}");
               buf.append("label{text='Then select in how many hours to pinpoint your appearance.:'}");
               buf.append("radio{ group='hours'; id='0';text='0'}");
               buf.append("radio{ group='hours'; id='3';text='3'}");
               buf.append("radio{ group='hours'; id='6';text='6'}");
               buf.append("radio{ group='hours'; id='9';text='9'}");
               buf.append("radio{ group='hours'; id='12';text='12'}");
               buf.append("radio{ group='hours'; id='15';text='15'}");
               buf.append("radio{ group='hours'; id='18';text='18'}");
               buf.append("radio{ group='hours'; id='21';text='21'}");
            }

            buf.append("label{text='Good luck!'}");
         } else {
            buf.append("label{text='You are not the ruler!'}");
         }
      } else {
         buf.append("label{text='You are not the ruler!'}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
