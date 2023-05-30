package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.Properties;

public final class DropInfoQuestion extends Question {
   public DropInfoQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 49, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      String val = answers.getProperty("okaycb");
      if (val != null && val.equals("true")) {
         this.getResponder().setTheftWarned(true);
      }

      ((Player)this.getResponder()).setQuestion(null);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("header{text=\"Theft warning:\"}");
      buf.append("text{text=''}");
      buf.append("text{text=\"You are dropping an item.\"}");
      if (!Servers.localServer.PVPSERVER) {
         buf.append("text{text=\"Usually, if you stay within one tile of the item nobody else may pick them up unless you team up with them.\"}");
      }

      buf.append("text{text=\"\"}");
      buf.append(
         "text{text=\"Otherwise, if this area is not on a settlement deed it may be stolen. Anyone may pass by and steal this unless you pick it up first. You need to build a house for your things to be protected.\"}"
      );
      buf.append("text{text=''}");
      buf.append("checkbox{id='okaycb';selected='false';text='I have understood this message and do not need to see it ever again'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
