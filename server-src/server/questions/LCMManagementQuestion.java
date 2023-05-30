package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public class LCMManagementQuestion extends Question {
   private short actionType;

   public LCMManagementQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, short actionType) {
      super(aResponder, aTitle, aQuestion, 128, aTarget);
      this.actionType = actionType;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseLCMManagementQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("text{text='Who do you want to " + this.getActionVerb() + "?'};");
      buf.append("label{text'Name:'};input{id='name';maxchars='40';text=\"\"};");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String getActionVerb() {
      if (this.actionType == 698) {
         return "add or remove their CA status from";
      } else if (this.actionType == 699) {
         return "add or remove their CM status from";
      } else {
         return this.actionType == 700 ? "see their info of" : "";
      }
   }

   public short getActionType() {
      return this.actionType;
   }
}
