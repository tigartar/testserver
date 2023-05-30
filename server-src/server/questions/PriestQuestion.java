package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public final class PriestQuestion extends Question {
   public PriestQuestion(Creature aResponder, String aTitle, String aQuestion, long aAltar) {
      super(aResponder, aTitle, aQuestion, 44, aAltar);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parsePriestQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{text='You may choose to become a priest of " + this.getResponder().getDeity().name + ".'}text{text=''}");
      buf.append("text{text='If you answer yes, you will receive special powers from your deity, such as the ability to cast spells.'}text{text=''}");
      if (Servers.localServer.PVPSERVER) {
         buf.append(
            "text{text='You must also walk this path if you strive to become a real champion of " + this.getResponder().getDeity().name + ".'}text{text=''}"
         );
      }

      buf.append("text{text='You will however be very limited in what you can do.'}");
      buf.append("text{text='You will for instance not be able to do such things as create, repair or improve items or use alchemy.'}");
      if (this.getResponder().getDeity().number == 4) {
         buf.append("text{text='You will also not be able to tame animals or farm to mention just a few other limitations.'}");
      } else if (Servers.localServer.PVPSERVER) {
         buf.append("text{text='You will also not be able to steal, pick locks or destroy structures to mention just a few other limitations.'}");
      }

      if (Servers.localServer.EPIC) {
         buf.append("text{text='You will also note that as you focus on your soul, you gain body and mind skills a lot slower.'}");
      }

      if (Servers.localServer.PVPSERVER) {
         buf.append("text{text='If you later decide to become a champion of " + this.getResponder().getDeity().name + " these restrictions will be lifted.'}");
         buf.append("text{text='As a champion, you may only escape death a few times though. After that your life ends permanently.'}text{text=''}");
      }

      buf.append("text{text='If your faith ever fails you, you will lose your priesthood.'}text{text=''}");
      buf.append(
         "text{type='italic';text='Do you want to become a priest of "
            + this.getResponder().getDeity().name
            + " despite the severe limitations it will have on your actions?'}"
      );
      buf.append("radio{ group='priest'; id='true';text='Yes'}");
      buf.append("radio{ group='priest'; id='false';text='No';selected='true'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
