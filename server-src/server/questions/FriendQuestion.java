package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FriendQuestion extends Question {
   private static final Logger logger = Logger.getLogger(FriendQuestion.class.getName());

   public FriendQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 25, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseFriendQuestion(this);
   }

   @Override
   public void sendQuestion() {
      try {
         Player sender = Players.getInstance().getPlayer(this.target);
         StringBuilder buf = new StringBuilder();
         buf.append(this.getBmlHeader());
         buf.append("text{text='" + sender.getName() + " wants to add you to " + sender.getHisHerItsString() + " friends list.'}");
         buf.append(
            "text{text='This will mean "
               + sender.getHeSheItString()
               + " will see you log on and off, and be able to allow you into structures "
               + sender.getHeSheItString()
               + " controls.'}"
         );
         buf.append("text{text='Do you accept?'}");
         buf.append("radio{ group='join'; id='accept';text='Accept'}");
         buf.append("radio{ group='join'; id='decline';text='Decline';selected='true'}");
         buf.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
      } catch (NoSuchPlayerException var3) {
         logger.log(Level.WARNING, "Player with id " + this.target + " trying to send a question, but cant be found?", (Throwable)var3);
      }
   }
}
