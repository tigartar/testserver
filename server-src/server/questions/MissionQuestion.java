package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.OldMission;
import java.util.Properties;

public final class MissionQuestion extends Question {
   private int missionNumber;

   public MissionQuestion(int aMissionNum, Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 61, aTarget);
      this.missionNumber = aMissionNum;
   }

   @Override
   public void answer(Properties answers) {
      Creature guide = null;

      try {
         guide = Server.getInstance().getCreature(this.target);
      } catch (NoSuchCreatureException var9) {
         this.getResponder().getCommunicator().sendNormalServerMessage("Your guide has left!");
         return;
      } catch (NoSuchPlayerException var10) {
         this.getResponder().getCommunicator().sendNormalServerMessage("Your guide has left!");
         return;
      }

      OldMission cm = OldMission.getMission(this.missionNumber, this.getResponder().getKingdomId());
      boolean ok = false;
      boolean skip = false;
      if (cm.hasCheckBox()) {
         boolean done = answers.getProperty("check").equals("true");
         if (done) {
            skip = true;
            if (this.missionNumber == 9999) {
               this.missionNumber = this.getResponder().getTutorialLevel();
               if (this.missionNumber != 9999) {
                  this.getResponder()
                     .getCommunicator()
                     .sendNormalServerMessage("You decide to continue following the instructions from the " + guide.getName() + ".");
               }
            } else {
               ++this.missionNumber;
               this.getResponder().missionFinished(true, false);
            }

            if (this.missionNumber != 9999) {
               OldMission m = OldMission.getMission(this.missionNumber, this.getResponder().getKingdomId());
               if (m != null) {
                  MissionQuestion ms = new MissionQuestion(m.number, this.getResponder(), m.title, m.missionDescription, this.target);
                  ms.sendQuestion();
               }
            } else {
               ((Player)this.getResponder()).setTutorialLevel(9999);
               SimplePopup popup = new SimplePopup(
                  this.getResponder(),
                  "Tutorial done!",
                  "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!"
               );
               popup.sendQuestion();
            }
         } else {
            SimplePopup popup = new SimplePopup(this.getResponder(), "Wait for now.", "You decide to take a pause and maybe come back later.");
            popup.sendQuestion();
         }
      } else if (answers.getProperty("mission") != null) {
         ok = answers.getProperty("mission").equals("do");
         if (ok) {
            OldMission m = OldMission.getMission(this.missionNumber, this.getResponder().getKingdomId());
            if (m != null) {
               SimplePopup popup = new SimplePopup(this.getResponder(), this.title, "You accept the mission.");
               popup.sendQuestion();
            } else {
               ((Player)this.getResponder()).setTutorialLevel(9999);
               SimplePopup popup = new SimplePopup(
                  this.getResponder(),
                  "Tutorial done!",
                  "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!"
               );
               popup.sendQuestion();
            }
         }
      }

      if (answers.getProperty("mission") != null) {
         ok = answers.getProperty("mission").equals("wait");
         if (!skip && ok) {
            SimplePopup popup = new SimplePopup(this.getResponder(), "Wait for now.", "You decide to take a pause and maybe come back later.");
            popup.sendQuestion();
         }

         ok = answers.getProperty("mission").equals("skip");
         if (ok) {
            OldMission m = OldMission.getMission(this.missionNumber + 1, this.getResponder().getKingdomId());
            if (m != null) {
               ((Player)this.getResponder()).setTutorialLevel(this.missionNumber + 1);
               MissionQuestion ms = new MissionQuestion(m.number, this.getResponder(), m.title, m.missionDescription, this.target);
               ms.sendQuestion();
            } else {
               ((Player)this.getResponder()).setTutorialLevel(9999);
               SimplePopup popup = new SimplePopup(
                  this.getResponder(),
                  "Tutorial done!",
                  "That concludes the tutorial! The " + guide.getName() + " is most pleased. Congratulations and good luck!"
               );
               popup.sendQuestion();
            }
         }

         ok = answers.getProperty("mission").equals("skipall");
         if (ok) {
            ((Player)this.getResponder()).setTutorialLevel(9999);
            SimplePopup popup = new SimplePopup(
               this.getResponder(), "Tutorial done!", "You decide to skip all the missions for now. You may return later. Good luck!"
            );
            popup.sendQuestion();
         }
      }
   }

   @Override
   public void sendQuestion() {
      Creature guide = null;

      try {
         guide = Server.getInstance().getCreature(this.target);
      } catch (NoSuchCreatureException var4) {
      } catch (NoSuchPlayerException var5) {
      }

      OldMission m = OldMission.getMission(this.missionNumber, this.getResponder().getKingdomId());
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeaderNoQuestion());
      if (guide != null) {
         buf.append("text{text=\"The " + guide.getName() + " looks at you sternly and says:\"}");
      }

      buf.append("text{text=''}");
      buf.append("text{text=\"" + this.getQuestion() + "\"}");
      buf.append("text{text=''}");
      if (m.missionDescription2.length() > 0) {
         buf.append("text{text=\"" + m.missionDescription2 + "\"}");
         buf.append("text{text=''}");
      }

      if (m.missionDescription3.length() > 0) {
         buf.append("text{text=\"" + m.missionDescription3 + "\"}");
         buf.append("text{text=''}");
      }

      if (m.hasCheckBox()) {
         buf.append("checkbox{id='check';selected='false';text=\"" + m.checkBoxString + "\"}");
      } else {
         buf.append("radio{ group='mission'; id='do';text='I will do this';selected='true'}");
         buf.append("radio{ group='mission'; id='wait';text='I want to wait a while'}");
      }

      buf.append("text{text=''}");
      buf.append("text{type='italic';text='You may see your current instructions by typing /mission in a chat window.'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
