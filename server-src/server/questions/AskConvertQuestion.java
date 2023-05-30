package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import java.util.Properties;

public final class AskConvertQuestion extends Question {
   private final Item holyItem;

   public AskConvertQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, Item aHolyItem) {
      super(aResponder, aTitle, aQuestion, 27, aTarget);
      this.holyItem = aHolyItem;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseAskConvertQuestion(this);
   }

   @Override
   public void sendQuestion() {
      try {
         Creature asker = Server.getInstance().getCreature(this.target);
         StringBuilder buf = new StringBuilder();
         buf.append(this.getBmlHeader());
         buf.append(
            "text{text='"
               + asker.getName()
               + " wants to teach you about "
               + asker.getDeity().name
               + ".'}text{text=''}text{text=''}text{text='Do you want to listen?'}"
         );
         buf.append("text{text='After you listen, you will get the option to join the followers of " + asker.getDeity().name + ".'}");
         buf.append("text{text=''}");
         buf.append("radio{ group='conv'; id='true';text='Yes'}");
         buf.append("radio{ group='conv'; id='false';text='No';selected='true'}");
         buf.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
         asker.getCommunicator().sendNormalServerMessage("You ask " + this.getResponder().getName() + " to listen to you.");
      } catch (NoSuchCreatureException var3) {
      } catch (NoSuchPlayerException var4) {
      }
   }

   Item getHolyItem() {
      return this.holyItem;
   }
}
