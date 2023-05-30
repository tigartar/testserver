package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import java.io.IOException;
import java.util.Properties;

public final class CreatureChangeAgeQuestion extends Question {
   public CreatureChangeAgeQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 153, aTarget);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      int width = 150;
      int height = 150;

      try {
         Creature target = Creatures.getInstance().getCreature(this.target);
         int age = target.getStatus().age;
         buf.append("harray{input{id='newAge'; maxchars='3'; text='").append(age).append("'}label{text='Age'}}");
      } catch (NoSuchCreatureException var6) {
         var6.printStackTrace();
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(width, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      this.init(this);
   }

   private void init(CreatureChangeAgeQuestion question) {
      Creature responder = question.getResponder();
      int newAge = 0;
      long target = question.getTarget();

      try {
         Creature creature = Creatures.getInstance().getCreature(target);
         String age = question.getAnswer().getProperty("newAge");
         newAge = Integer.parseInt(age);
         ((DbCreatureStatus)creature.getStatus()).updateAge(newAge);
         creature.getStatus().lastPolledAge = 0L;
         creature.pollAge();
         creature.refreshVisible();
      } catch (IOException | NoSuchCreatureException var8) {
         var8.printStackTrace();
      }

      responder.getCommunicator().sendNormalServerMessage("Age = " + newAge + ".");
   }
}
