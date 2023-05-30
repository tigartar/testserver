package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Properties;

public final class DeclareWarQuestion extends Question {
   private final Village targetVillage;

   public DeclareWarQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) throws NoSuchCreatureException, NoSuchPlayerException, NoSuchVillageException {
      super(aResponder, aTitle, aQuestion, 29, aTarget);
      this.targetVillage = Villages.getVillage((int)aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseVillageWarQuestion(this);
   }

   public Village getTargetVillage() {
      return this.targetVillage;
   }

   @Override
   public void sendQuestion() {
      Village village = this.getResponder().getCitizenVillage();
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("header{text=\"Declaring war on " + this.targetVillage.getName() + ":\"}");
      buf.append("text{text=\"Do you really want to declare war on " + this.targetVillage.getName() + "?\"}");
      buf.append("text{text=\"" + this.targetVillage.getName() + " will have 24 hours to accept the challenge.\"}");
      buf.append("text{text=\"If they haven't answered in that time the war will begin.\"}");
      if (village.isAlly(this.targetVillage)) {
         buf.append("text{text=\"Your alliance with " + this.targetVillage.getName() + " will be broken.\"}");
      }

      buf.append("radio{ group='declare'; id='true';text='Yes'}");
      buf.append("radio{ group='declare'; id='false';text='No';selected='true'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
