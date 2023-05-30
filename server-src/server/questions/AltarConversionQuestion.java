package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deity;
import java.util.Properties;

public final class AltarConversionQuestion extends Question {
   private final Deity deity;

   public AltarConversionQuestion(Creature aResponder, String aTitle, String aQuestion, long aAltar, Deity aDeity) {
      super(aResponder, aTitle, aQuestion, 31, aAltar);
      this.deity = aDeity;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseAltarConvertQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{text='The inscription talks about " + this.deity.name + ".'}");
      buf.append("text{text=''}");

      for(int x = 0; x < this.deity.altarConvertText1.length; ++x) {
         buf.append("text{text='" + this.deity.altarConvertText1[x] + "'}");
         buf.append("text{text=''}");
      }

      if (this.getResponder().isChampion() && this.getResponder().getDeity() != null) {
         buf.append(
            "text{text='You are already the devoted follower of "
               + this.getResponder().getDeity().name
               + ". "
               + this.deity.name
               + " would never accept you.'}"
         );
         buf.append("text{text=''}");
      } else if (!QuestionParser.doesKingdomTemplateAcceptDeity(this.getResponder().getKingdomTemplateId(), this.deity)) {
         buf.append("text{text='" + this.getResponder().getKingdomName() + " would never accept a follower of " + this.deity.name + ".'}");
         buf.append("text{text=''}");
      } else if (this.getResponder().getDeity() != null && this.getResponder().getDeity() == this.deity) {
         buf.append("text{text='You are already a follower of " + this.getResponder().getDeity().name + ".'}");
         buf.append("text{text=''}");
      } else {
         buf.append("text{type='italic';text='Do you want to become a follower of " + this.deity.name + "?'}");
         buf.append("text{text=''}");
         if (this.getResponder().getDeity() != null) {
            buf.append(
               "text{type='bold';text='If you answer yes, your faith and all your abilities granted by "
                  + this.getResponder().getDeity().name
                  + " will be lost!'}"
            );
         }

         if (!Servers.localServer.PVPSERVER) {
            buf.append(
               "text{type='bold';text='Warning: Converting to a deity on Freedom then travelling to a Chaos kingdom that does notalign with your deity you will lose all faith and abilities granted, and you will stop following that deity. Libila does not align with WL kingdoms and Fo/Vynora/Magranon do not align with BL kingdoms.'}"
            );
         }

         buf.append("text{text=''}");
         buf.append("radio{ group='conv'; id='true';text='Accept'}");
         buf.append("radio{ group='conv'; id='false';text='Decline';selected='true'}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   Deity getDeity() {
      return this.deity;
   }
}
