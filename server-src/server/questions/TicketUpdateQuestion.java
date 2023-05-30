package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicketUpdateQuestion extends Question {
   private static final Logger logger = Logger.getLogger(TicketUpdateQuestion.class.getName());
   private int ticketId;
   private short action;

   public TicketUpdateQuestion(Creature aResponder, int aTicketId, short aAction) {
      super(aResponder, "Ticket: #" + aTicketId, makeQuestion(aResponder, aAction), 108, (long)aTicketId);
      this.ticketId = aTicketId;
      this.action = aAction;
   }

   static String makeQuestion(Creature aResponder, short aAction) {
      switch(aAction) {
         case 587:
            if (!((Player)aResponder).mayHearDevTalk() && !((Player)aResponder).mayHearMgmtTalk()) {
               return "Append to ticket description.";
            }

            return "Add note to ticket.";
         case 588:
            return "Add reason why you are cancelling this ticket.";
         case 589:
         case 595:
         default:
            return "Question for unknown action " + aAction;
         case 590:
            return "Add note for how the ticket was resolved.";
         case 591:
            return "Please add why you are passing this to GMs.";
         case 592:
            return "Please add why you are passing this to Arch.";
         case 593:
            return "Please add why you are passing this to Dev.";
         case 594:
            return "Please add why you are putting this ticket on hold.";
         case 596:
            return "Please add why you are passing it back to CMs.";
         case 597:
            return "Ticket Feedback.";
      }
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 108) {
            this.ticketId = Integer.parseInt(aAnswer.getProperty("tid"));
            this.action = Short.parseShort(aAnswer.getProperty("action"));
            boolean append = Boolean.parseBoolean(aAnswer.getProperty("append"));
            byte level = Byte.parseByte(aAnswer.getProperty("level"));
            String note = aAnswer.getProperty("note");
            note = note.replace('"', '\'');
            if (note.length() == 0) {
               this.getResponder().getCommunicator().sendNormalServerMessage("Must supply a note for ticket action");
               return;
            }

            Ticket ticket = Tickets.getTicket(this.ticketId);
            if (this.action == 587 && append && note.length() > 0) {
               ticket.appendDescription(note);
            }

            switch(this.action) {
               case 587:
                  if (note.length() > 0) {
                     ticket.addNewTicketAction((byte)0, this.getResponder().getName(), note, level);
                  }
                  break;
               case 588:
                  ticket.addNewTicketAction((byte)1, this.getResponder().getName(), note, level);
               case 589:
               case 595:
               case 598:
               default:
                  break;
               case 590:
                  ticket.addNewTicketAction((byte)9, this.getResponder().getName(), note, level);
                  break;
               case 591:
                  ticket.addNewTicketAction((byte)6, this.getResponder().getName(), note, level);
                  break;
               case 592:
                  ticket.addNewTicketAction((byte)7, this.getResponder().getName(), note, level);
                  break;
               case 593:
                  ticket.addNewTicketAction((byte)8, this.getResponder().getName(), note, level);
                  break;
               case 594:
                  ticket.addNewTicketAction((byte)10, this.getResponder().getName(), note, level);
                  break;
               case 596:
                  ticket.addNewTicketAction((byte)13, this.getResponder().getName(), note, level);
                  break;
               case 597:
                  if (!ticket.hasFeedback()) {
                     byte service = Byte.parseByte(aAnswer.getProperty("service"));
                     byte courteous = Byte.parseByte(aAnswer.getProperty("courteous"));
                     byte knowledgeable = Byte.parseByte(aAnswer.getProperty("knowledgeable"));
                     boolean general1 = Boolean.parseBoolean(aAnswer.getProperty("general1"));
                     boolean general2 = Boolean.parseBoolean(aAnswer.getProperty("general2"));
                     boolean general3 = Boolean.parseBoolean(aAnswer.getProperty("general3"));
                     boolean general4 = Boolean.parseBoolean(aAnswer.getProperty("general4"));
                     boolean general5 = Boolean.parseBoolean(aAnswer.getProperty("general5"));
                     boolean general6 = Boolean.parseBoolean(aAnswer.getProperty("general6"));
                     boolean general7 = Boolean.parseBoolean(aAnswer.getProperty("general7"));
                     byte general = (byte)(
                        (general1 ? 1 : 0)
                           + (general2 ? 2 : 0)
                           + (general3 ? 4 : 0)
                           + (general4 ? 8 : 0)
                           + (general5 ? 16 : 0)
                           + (general6 ? 32 : 0)
                           + (general7 ? 64 : 0)
                     );
                     boolean quality1 = Boolean.parseBoolean(aAnswer.getProperty("quality1"));
                     boolean quality2 = Boolean.parseBoolean(aAnswer.getProperty("quality2"));
                     boolean quality3 = Boolean.parseBoolean(aAnswer.getProperty("quality3"));
                     boolean quality4 = Boolean.parseBoolean(aAnswer.getProperty("quality4"));
                     boolean quality5 = Boolean.parseBoolean(aAnswer.getProperty("quality5"));
                     boolean quality6 = Boolean.parseBoolean(aAnswer.getProperty("quality6"));
                     byte quality = (byte)(
                        (quality1 ? 1 : 0) + (quality2 ? 2 : 0) + (quality3 ? 4 : 0) + (quality4 ? 8 : 0) + (quality5 ? 16 : 0) + (quality6 ? 32 : 0)
                     );
                     boolean irked1 = Boolean.parseBoolean(aAnswer.getProperty("irked1"));
                     boolean irked2 = Boolean.parseBoolean(aAnswer.getProperty("irked2"));
                     boolean irked3 = Boolean.parseBoolean(aAnswer.getProperty("irked3"));
                     boolean irked4 = Boolean.parseBoolean(aAnswer.getProperty("irked4"));
                     boolean irked5 = Boolean.parseBoolean(aAnswer.getProperty("irked5"));
                     boolean irked6 = Boolean.parseBoolean(aAnswer.getProperty("irked6"));
                     byte irked = (byte)((irked1 ? 1 : 0) + (irked2 ? 2 : 0) + (irked3 ? 4 : 0) + (irked4 ? 8 : 0) + (irked5 ? 16 : 0) + (irked6 ? 32 : 0));
                     ticket.addNewTicketAction(
                        (byte)14, this.getResponder().getName(), note, level, service, courteous, knowledgeable, general, quality, irked
                     );
                  }
                  break;
               case 599:
                  ticket.addNewTicketAction((byte)15, this.getResponder().getName(), note, level);
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      Ticket ticket = Tickets.getTicket(this.ticketId);
      Player player = (Player)this.getResponder();
      int lines = 0;
      buf.append(this.getBmlHeader());
      buf.append("passthrough{id=\"tid\";text=\"" + this.ticketId + "\"}");
      buf.append("passthrough{id=\"action\";text=\"" + this.action + "\"}");
      buf.append("text{type=\"bolditalic\";text=\"Description: " + ticket.getDateAsString() + "\"};");
      buf.append("text{text=\"" + ticket.getDescription() + "\"};");
      String[] descLines = ticket.getDescription().split("\\n");
      int descLineCount = descLines.length;
      buf.append("text{type=\"bolditalic\";text=\"Actions so far:\"};");
      buf.append("text{color=\"66,255,255\";type=\"italic\";text=\"Times are in server time.\"};");
      TicketAction[] ticketActions = ticket.getTicketActions(player);
      if (ticketActions.length == 0) {
         buf.append("text{text=\"none!\"};");
      } else {
         for(TicketAction ta : ticketActions) {
            buf.append("text{text=\"" + ta.getLine(player) + "\"};");
            String note = ta.getNotePlus(player);
            if (note.length() > 0) {
               buf.append("text{text=\"  " + note + "\"};");
               ++lines;
            }
         }
      }

      if (player.getWurmId() == ticket.getPlayerId() && this.action != 591) {
         buf.append("passthrough{id=\"level\";text=\"0\"}");
         if (ticket.isOpen()) {
            if (this.action == 588) {
               buf.append("passthrough{id=\"append\";text=\"false\"}");
               buf.append("text{type=\"bold\";text=\"Please do let us know how you resolved this ticket.\"};");
            } else {
               buf.append("checkbox{id=\"append\";text=\"Select to append to description.\";selected=\"false\"};");
               buf.append("text{type=\"bold\";text=\"You may add a note which can be optionally appended to the description.\"};");
            }

            buf.append("input{id=\"note\";maxchars=\"200\";text=\"\"}");
         } else if (this.action == 597) {
            buf.append("passthrough{id=\"append\";text=\"false\"}");
            if (ticket.hasFeedback()) {
               this.showReadOnlyFeedback(ticket, buf);
            } else {
               buf.append("text{text=\"\"}");
               buf.append("text{type=\"bold\";color=\"66,255,255\";text=\"Feedback Survey\"}");
               buf.append("text{type=\"bold\";text=\"How would you rate the quality of service for this ticket?\"}");
               buf.append("radio{group=\"service\";id=\"0\";hidden=\"true\";text=\"hidden\"}");
               buf.append("radio{group=\"service\";id=\"1\";text=\"Superior\"}");
               buf.append("radio{group=\"service\";id=\"2\";text=\"Good\"}");
               buf.append("radio{group=\"service\";id=\"3\";text=\"Average\"}");
               buf.append("radio{group=\"service\";id=\"4\";text=\"Fair\"}");
               buf.append("radio{group=\"service\";id=\"5\";text=\"Poor\"}");
               buf.append("text{type=\"bold\";text=\"The Support Team was very courteous:\"}");
               buf.append("radio{group=\"courteous\";id=\"0\";hidden=\"true\";text=\"hidden\"}");
               buf.append("radio{group=\"courteous\";id=\"1\";text=\"Strongly Agree\"}");
               buf.append("radio{group=\"courteous\";id=\"2\";text=\"Somewhat Agree\"}");
               buf.append("radio{group=\"courteous\";id=\"3\";text=\"Neutral\"}");
               buf.append("radio{group=\"courteous\";id=\"4\";text=\"Somewhat Disagree\"}");
               buf.append("radio{group=\"courteous\";id=\"5\";text=\"Strongly Disagree\"}");
               buf.append("text{type=\"bold\";text=\"The Support Team was very knowledgeable:\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"0\";hidden=\"true\";text=\"hidden\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"1\";text=\"Strongly Agree\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"2\";text=\"Somewhat Agree\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"3\";text=\"Neutral\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"4\";text=\"Somewhat Disagree\"}");
               buf.append("radio{group=\"knowledgeable\";id=\"5\";text=\"Strongly Disagree\"}");
               buf.append("text{type=\"bold\";text=\"The Support Team:\"}");
               buf.append("checkbox{id=\"general1\";text=\"Gave me the wrong information\"}");
               buf.append("checkbox{id=\"general2\";text=\"Didn't understand the question\"}");
               buf.append("checkbox{id=\"general3\";text=\"Gave unclear answers\"}");
               buf.append("checkbox{id=\"general4\";text=\"Couldn't solve the problem\"}");
               buf.append("checkbox{id=\"general5\";text=\"Was disorganized\"}");
               buf.append("checkbox{id=\"general6\";text=\"Other\"}");
               buf.append("checkbox{id=\"general7\";text=\"No improvement needed\"}");
               buf.append("text{type=\"bold\";text=\"Which of the following qualities of the Support Team stood out?\"}");
               buf.append("checkbox{id=\"quality1\";text=\"Patient\"}");
               buf.append("checkbox{id=\"quality2\";text=\"Enthusiastic\"}");
               buf.append("checkbox{id=\"quality3\";text=\"Listened Carefully\"}");
               buf.append("checkbox{id=\"quality4\";text=\"Friendly\"}");
               buf.append("checkbox{id=\"quality5\";text=\"Responsive\"}");
               buf.append("checkbox{id=\"quality6\";text=\"Nothing stood out.\"}");
               buf.append("text{type=\"bold\";text=\"What qualities of the Support Team irked you?\"}");
               buf.append("checkbox{id=\"irked1\";text=\"Not Patient\"}");
               buf.append("checkbox{id=\"irked2\";text=\"Not Enthusiastic\"}");
               buf.append("checkbox{id=\"irked3\";text=\"Didn't Listen Carefully\"}");
               buf.append("checkbox{id=\"irked4\";text=\"Unfriendly\"}");
               buf.append("checkbox{id=\"irked5\";text=\"Unresponsive\"}");
               buf.append("checkbox{id=\"irked6\";text=\"No qualities irked me\"}");
               buf.append(
                  "text{type=\"bold\";text=\"Briefly describe any aspect of the process and/or team member which you considered outstanding or could be improved.\"}"
               );
               buf.append("input{id=\"note\";maxchars=\"200\";text=\"\"}");
               buf.append(
                  "text{text=\"Thank you for your feedback. We sincerely appreciate your honest opinion and will take your input into consideration while providing services in the future.\"};"
               );
            }
         } else {
            buf.append("passthrough{id=\"append\";text=\"false\"}");
            buf.append("passthrough{id=\"note\";text=\"\"}");
         }
      } else if (player.mayHearDevTalk() && this.action == 597 && ticket.hasFeedback()) {
         buf.append("passthrough{id=\"append\";text=\"false\"}");
         this.showReadOnlyFeedback(ticket, buf);
      } else if (player.mayMute()) {
         buf.append("label{type=\"bold\";text=\"Who can see the associated note?\"};");
         buf.append("harray{");
         buf.append("radio{group=\"level\";id=\"0\";text=\"All?\"};");
         if (player.mayHearDevTalk()) {
            buf.append("radio{group=\"level\";id=\"1\";text=\"CM and above?\"};");
            buf.append("radio{group=\"level\";id=\"2\";text=\"GM and above?\";selected=\"true\"};");
         } else {
            buf.append("radio{group=\"level\";id=\"1\";text=\"CM and above?\";selected=\"true\"};");
         }

         buf.append("}");
         buf.append("passthrough{id=\"append\";text=\"false\"}");
         if (this.action == 590) {
            buf.append("text{type=\"bold\";text=\"How was this ticket resolved?\"};");
         } else if (this.action == 594) {
            buf.append("text{type=\"bold\";text=\"Why was this ticket put on hold?\"};");
         } else if (this.action == 587) {
            buf.append("text{type=\"bold\";text=\"Please add your note here.\"};");
         } else if (this.action == 596) {
            buf.append("text{type=\"bold\";text=\"Why are you forwarding this to CM?\"};");
         } else if (this.action == 591) {
            buf.append("text{type=\"bold\";text=\"Why are you forwarding this to GM?\"};");
         } else if (this.action == 592) {
            buf.append("text{type=\"bold\";text=\"Why are you forwarding this to Arch?\"};");
         } else if (this.action == 593) {
            buf.append("text{type=\"bold\";text=\"Why are you forwarding this to Dev?\"};");
         } else if (this.action == 599) {
            buf.append("text{type=\"bold\";text=\"Why are you Re-opening this?\"};");
         }

         buf.append("input{id=\"note\";maxchars=\"200\";text=\"\"}");
      }

      buf.append(this.createAnswerButton2());
      int height = Math.min(220 + ticketActions.length * 23 + lines * 23 + descLineCount * 23, 500);
      this.getResponder().getCommunicator().sendBml(500, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private void showReadOnlyFeedback(Ticket ticket, StringBuilder buf) {
      TicketAction ta = ticket.getFeedback();
      buf.append("passthrough{id=\"note\";text=\"\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";color=\"66,255,255\";text=\"Feedback Survey - Read Only\"}");
      buf.append("text{type=\"bold\";text=\"How would you rate the quality of service for this ticket?\"}");
      buf.append("radio{group=\"service\";id=\"1\";text=\"Superior\";enabled=\"false\";selected=\"" + ta.wasServiceSuperior() + "\"}");
      buf.append("radio{group=\"service\";id=\"2\";text=\"Good\";enabled=\"false\";selected=\"" + ta.wasServiceGood() + "\"}");
      buf.append("radio{group=\"service\";id=\"3\";text=\"Average\";enabled=\"false\";selected=\"" + ta.wasServiceAverage() + "\"}");
      buf.append("radio{group=\"service\";id=\"4\";text=\"Fair\";enabled=\"false\";selected=\"" + ta.wasServiceFair() + "\"}");
      buf.append("radio{group=\"service\";id=\"5\";text=\"Poor\";enabled=\"false\";selected=\"" + ta.wasServicePoor() + "\"}");
      buf.append("text{type=\"bold\";text=\"The Support Team was very courteous:\"}");
      buf.append("radio{group=\"courteous\";id=\"1\";text=\"Strongly Agree\";enabled=\"false\";selected=\"" + ta.wasCourteousStronglyAgree() + "\"}");
      buf.append("radio{group=\"courteous\";id=\"2\";text=\"Somewhat Agree\";enabled=\"false\";selected=\"" + ta.wasCourteousSomewhatAgree() + "\"}");
      buf.append("radio{group=\"courteous\";id=\"3\";text=\"Neutral\";enabled=\"false\";selected=\"" + ta.wasCourteousNeutral() + "\"}");
      buf.append("radio{group=\"courteous\";id=\"4\";text=\"Somewhat Disagree\";enabled=\"false\";selected=\"" + ta.wasCourteousSomewhatDisagree() + "\"}");
      buf.append("radio{group=\"courteous\";id=\"5\";text=\"Strongly Disagree\";enabled=\"false\";selected=\"" + ta.wasCourteousStronglyDisagree() + "\"}");
      buf.append("text{type=\"bold\";text=\"The Support Team was very knowledgeable:\"}");
      buf.append("radio{group=\"knowledgeable\";id=\"1\";text=\"Strongly Agree\";enabled=\"false\";selected=\"" + ta.wasKnowledgeableStronglyAgree() + "\"}");
      buf.append("radio{group=\"knowledgeable\";id=\"2\";text=\"Somewhat Agree\";enabled=\"false\";selected=\"" + ta.wasKnowledgeableSomewhatAgree() + "\"}");
      buf.append("radio{group=\"knowledgeable\";id=\"3\";text=\"Neutral\";enabled=\"false\";selected=\"" + ta.wasKnowledgeableNeutral() + "\"}");
      buf.append(
         "radio{group=\"knowledgeable\";id=\"4\";text=\"Somewhat Disagree\";enabled=\"false\";selected=\"" + ta.wasKnowledgeableSomewhatDisagree() + "\"}"
      );
      buf.append(
         "radio{group=\"knowledgeable\";id=\"5\";text=\"Strongly Disagree\";enabled=\"false\";selected=\"" + ta.wasKnowledgeableStronglyDisagree() + "\"}"
      );
      buf.append("text{type=\"bold\";text=\"The Support Team:\"}");
      buf.append("checkbox{id=\"general1\";text=\"Gave me the wrong information\";enabled=\"false\";selected=\"" + ta.wasGeneralWrongInfo() + "\"}");
      buf.append("checkbox{id=\"general2\";text=\"Didn't understand the question\";enabled=\"false\";selected=\"" + ta.wasGeneralNoUnderstand() + "\"}");
      buf.append("checkbox{id=\"general3\";text=\"Gave unclear answers\";enabled=\"false\";selected=\"" + ta.wasGeneralUnclear() + "\"}");
      buf.append("checkbox{id=\"general4\";text=\"Couldn't solve the problem\";enabled=\"false\";selected=\"" + ta.wasGeneralNoSolve() + "\"}");
      buf.append("checkbox{id=\"general5\";text=\"Was disorganized\";enabled=\"false\";selected=\"" + ta.wasGeneralDisorganized() + "\"}");
      buf.append("checkbox{id=\"general6\";text=\"Other\";enabled=\"false\";selected=\"" + ta.wasGeneralOther() + "\"}");
      buf.append("checkbox{id=\"general7\";text=\"No improvement needed\";enabled=\"false\";selected=\"" + ta.wasGeneralFine() + "\"}");
      buf.append("text{type=\"bold\";text=\"Which of the following qualities of the Support Team stood out?\"}");
      buf.append("checkbox{id=\"quality1\";text=\"Patient\";enabled=\"false\";selected=\"" + ta.wasQualityPatient() + "\"}");
      buf.append("checkbox{id=\"quality2\";text=\"Enthusiastic\";enabled=\"false\";selected=\"" + ta.wasQualityEnthusiastic() + "\"}");
      buf.append("checkbox{id=\"quality3\";text=\"Listened Carefully\";enabled=\"false\";selected=\"" + ta.wasQualityListened() + "\"}");
      buf.append("checkbox{id=\"quality4\";text=\"Friendly\";enabled=\"false\";selected=\"" + ta.wasQualityFriendly() + "\"}");
      buf.append("checkbox{id=\"quality5\";text=\"Responsive\";enabled=\"false\";selected=\"" + ta.wasQualityResponsive() + "\"}");
      buf.append("checkbox{id=\"quality6\";text=\"Nothing stood out.\";enabled=\"false\";selected=\"" + ta.wasQualityNothing() + "\"}");
      buf.append("text{type=\"bold\";text=\"What qualities of the Support Team irked you?\"}");
      buf.append("checkbox{id=\"irked1\";text=\"Not Patient\";enabled=\"false\";selected=\"" + ta.wasIrkedPatient() + "\"}");
      buf.append("checkbox{id=\"irked2\";text=\"Not Enthusiastic\";enabled=\"false\";selected=\"" + ta.wasIrkedEnthusiastic() + "\"}");
      buf.append("checkbox{id=\"irked3\";text=\"Didn't Listen Carefully\";enabled=\"false\";selected=\"" + ta.wasIrkedListened() + "\"}");
      buf.append("checkbox{id=\"irked4\";text=\"Unfriendly\";enabled=\"false\";selected=\"" + ta.wasIrkedFriendly() + "\"}");
      buf.append("checkbox{id=\"irked5\";text=\"Unresponsive\";enabled=\"false\";selected=\"" + ta.wasIrkedResponsive() + "\"}");
      buf.append("checkbox{id=\"irked6\";text=\"No qualities irked me\";enabled=\"false\";selected=\"" + ta.wasIrkedNothing() + "\"}");
      buf.append(
         "text{type=\"bold\";text=\"Briefly describe any aspect of the process and/or team member which you considered outstanding or could be improved.\"}"
      );
      buf.append("text{text=\"" + ta.getNote() + "\"}");
      buf.append(
         "text{text=\"Thank you for your feedback. We sincerely appreciate your honest opinion and will take your input into consideration while providing services in the future.\"};"
      );
   }
}
