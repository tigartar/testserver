package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.util.Properties;

public final class AppointmentsQuestion extends Question {
   public AppointmentsQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 63, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      King k = King.getKing(this.getResponder().getKingdomId());
      if (k != null && k.kingid == this.getResponder().getWurmId()) {
         Appointments a = Appointments.getAppointments(k.era);
         if (a != null) {
            this.addAppointments(a, k, answers);
         }
      }
   }

   public void addAppointments(Appointments a, King k, Properties answers) {
      for(int x = 0; x < a.availableOrders.length; ++x) {
         String val = answers.getProperty("order" + x);
         if (val != null && val.length() > 0) {
            Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
            if (p == null) {
               this.getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
            } else {
               p.addAppointment(a.getAppointment(x + 30), this.getResponder());
            }
         }
      }

      for(int x = 0; x < a.availableTitles.length; ++x) {
         String val = answers.getProperty("title" + x);
         if (val != null && val.length() > 0) {
            Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
            if (p == null) {
               this.getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
            } else {
               p.addAppointment(a.getAppointment(x), this.getResponder());
            }
         }
      }

      for(int x = 0; x < a.officials.length; ++x) {
         String val = answers.getProperty("official" + x);
         if (val == null || val.length() <= 0) {
            Appointment app = a.getAppointment(x + 1500);
            if (app != null && a.officials[x] > 0L) {
               Player oldp = Players.getInstance().getPlayerOrNull(a.officials[x]);
               if (oldp != null) {
                  oldp.getCommunicator()
                     .sendNormalServerMessage(
                        "You are hereby notified that you have been removed of the office as " + app.getNameForGender(oldp.getSex()) + ".", (byte)2
                     );
               } else {
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(a.officials[x]);
                  if (pinf != null) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage(
                           "Failed to notify " + pinf.getName() + " that they have been removed from the office of " + app.getNameForGender((byte)0) + ".",
                           (byte)3
                        );
                  }
               }

               this.getResponder().getCommunicator().sendNormalServerMessage("You vacate the office of " + app.getNameForGender((byte)0) + ".", (byte)2);
               a.setOfficial(x + 1500, 0L);
            }
         } else if (val.compareToIgnoreCase(PlayerInfoFactory.getPlayerName(a.officials[x])) != 0) {
            Player p = Players.getInstance().getPlayerOrNull(LoginHandler.raiseFirstLetter(val));
            if (p == null) {
               this.getResponder().getCommunicator().sendNormalServerMessage("There is no person with the name " + val + " present in your kingdom.");
            } else {
               p.addAppointment(a.getAppointment(x + 1500), this.getResponder());
            }
         }
      }
   }

   private void addTitleStrings(Appointments a, King k, StringBuilder buf) {
      buf.append("text{type='italic';text='Titles'}");

      for(int x = 0; x < a.availableTitles.length; ++x) {
         String key = "title" + x;
         if (a.getAvailTitlesForId(x) > 0) {
            Appointment app = a.getAppointment(x);
            if (app != null) {
               buf.append(
                  "harray{label{text='"
                     + app.getNameForGender((byte)0)
                     + " ("
                     + a.getAvailTitlesForId(x)
                     + ")'}};input{id='"
                     + key
                     + "'; maxchars='40'; text=''}"
               );
            }
         }
      }

      buf.append("text{text=''}");
   }

   private void addOrderStrings(Appointments a, King k, StringBuilder buf) {
      buf.append("text{type='italic';text='Orders and decorations'}");

      for(int x = 0; x < a.availableOrders.length; ++x) {
         String key = "order" + x;
         if (a.getAvailOrdersForId(x + 30) > 0) {
            Appointment app = a.getAppointment(x + 30);
            if (app != null) {
               buf.append(
                  "harray{label{text='"
                     + app.getNameForGender((byte)0)
                     + " ("
                     + a.getAvailOrdersForId(x + 30)
                     + ")'}};input{id='"
                     + key
                     + "'; maxchars='40'; text=''}"
               );
            }
         }
      }

      buf.append("text{text=''}");
   }

   private void addOfficeStrings(Appointments a, King k, StringBuilder buf) {
      buf.append("text{type='italic';text='Offices. Note: You can only set these once per week and only to players who are online.'}");

      for(int x = 0; x < a.officials.length; ++x) {
         String key = "official" + x;
         String oldval = "";
         long current = a.getOfficialForId(x + 1500);
         if (current > 0L) {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(current);
            if (pinf != null) {
               oldval = pinf.getName();
            }
         }

         Appointment app = a.getAppointment(x + 1500);
         if (app != null) {
            String set = "(available)";
            if (a.isOfficeSet(x + 1500)) {
               set = "(not available)";
            }

            String aname = app.getNameForGender((byte)0);
            if (this.getResponder().getSex() == 0 && app.getId() == 1507) {
               aname = app.getNameForGender((byte)1);
            }

            buf.append("harray{label{text='" + aname + " " + set + "'}};input{id='" + key + "'; maxchars='40'; text='" + oldval + "'}");
         }
      }

      buf.append("text{text=''}");
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("header{text='Kingdom appointments:'}text{text=''}");
      King k = King.getKing(this.getResponder().getKingdomId());
      if (k != null && k.kingid == this.getResponder().getWurmId()) {
         Appointments a = Appointments.getAppointments(k.era);
         if (a == null) {
            return;
         }

         long timeLeft = a.getResetTimeRemaining();
         if (timeLeft <= 0L) {
            buf.append("text{text='Titles and orders will refresh shortly.'}");
         } else {
            buf.append("text{text='Titles and orders will refresh in " + Server.getTimeFor(timeLeft) + ".'}");
         }

         buf.append("text{text=''}");
         this.addTitleStrings(a, k, buf);
         this.addOrderStrings(a, k, buf);
         this.addOfficeStrings(a, k, buf);
      } else {
         buf.append("text{text='You are not the current ruler.'}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(600, 600, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
