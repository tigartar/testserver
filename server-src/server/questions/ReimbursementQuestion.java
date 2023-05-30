package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class ReimbursementQuestion extends Question {
   private String[] nameArr = new String[0];

   public ReimbursementQuestion(Creature aResponder, long aTarget) {
      super(aResponder, "Reimbursements", "These are your available reimbursements:", 50, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      String key = "";
      String value = "";

      for(int x = 0; x < this.nameArr.length; ++x) {
         int days = 0;
         int trinkets = 0;
         int silver = 0;
         boolean boktitle = false;
         boolean mbok = false;
         key = "silver" + this.nameArr[x];
         value = answers.getProperty(key);
         if (value != null) {
            try {
               silver = Integer.parseInt(value);
            } catch (Exception var11) {
               this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of silver for " + this.nameArr[x]);
               return;
            }
         }

         key = "days" + this.nameArr[x];
         value = answers.getProperty(key);
         if (value != null) {
            try {
               days = Integer.parseInt(value);
            } catch (Exception var12) {
               this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of days for " + this.nameArr[x]);
               return;
            }
         }

         key = "trinket" + this.nameArr[x];
         value = answers.getProperty(key);
         if (value != null) {
            try {
               trinkets = Integer.parseInt(value);
            } catch (Exception var13) {
               this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of trinkets for " + this.nameArr[x]);
               return;
            }
         }

         key = "mbok" + this.nameArr[x];
         value = answers.getProperty(key);
         if (value != null) {
            try {
               boktitle = Boolean.parseBoolean(value);
               if (boktitle) {
                  mbok = true;
               }
            } catch (Exception var14) {
               this.getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the MBoK/Title answer for " + this.nameArr[x]);
               return;
            }
         }

         if (!boktitle) {
            key = "bok" + this.nameArr[x];
            value = answers.getProperty(key);
            if (value != null) {
               try {
                  boktitle = Boolean.parseBoolean(value);
               } catch (Exception var15) {
                  this.getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the BoK/Title answer for " + this.nameArr[x]);
                  return;
               }
            }
         }

         if (days > 0 || trinkets > 0 || silver > 0 || boktitle) {
            if (days >= 0 && trinkets >= 0 && silver >= 0) {
               LoginServerWebConnection lsw = new LoginServerWebConnection();
               this.getResponder()
                  .getCommunicator()
                  .sendNormalServerMessage(
                     lsw.withDraw(
                        (Player)this.getResponder(),
                        this.nameArr[x],
                        ((Player)this.getResponder()).getSaveFile().emailAddress,
                        trinkets,
                        silver,
                        boktitle,
                        mbok,
                        days
                     )
                  );
            } else {
               this.getResponder().getCommunicator().sendAlertServerMessage("Less than 0 value entered for " + this.nameArr[x]);
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      LoginServerWebConnection lsw = new LoginServerWebConnection();
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      String s = lsw.getReimburseInfo((Player)this.getResponder());
      if (s.equals("text{text='You have no reimbursements pending.'}")) {
         ((Player)this.getResponder()).getSaveFile().setHasNoReimbursementLeft(true);
      } else {
         String ttext = s;
         String newName = "";
         Set<String> names = new HashSet<>();
         boolean keepGoing = true;

         while(keepGoing) {
            newName = this.getNextName(ttext);
            if (newName.equals("")) {
               keepGoing = false;
            } else {
               names.add(newName);
               ttext = ttext.substring(ttext.indexOf(" - '}") + 5, ttext.length());
            }
         }

         this.nameArr = names.toArray(new String[names.size()]);
      }

      buf.append(s);
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String getNextName(String ttext) {
      int place = ttext.indexOf("Name=");
      return place > 0 ? ttext.substring(place + 5, ttext.indexOf(" - '}")) : "";
   }
}
