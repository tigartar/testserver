package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class PaymentQuestion extends Question {
   private final List<Long> playerIds = new LinkedList<>();

   public PaymentQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 20, aTarget);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("harray{label{text='Days'};input{id='days'; text='0'; maxchars='3'}}");
      buf.append("harray{label{text='Months'};input{id='months'; text='0'; maxchars='3'}}");
      buf.append("checkbox{id='extend';text='Mark this if you want to EXTEND a current payment instead of just setting from NOW'}");
      buf.append("harray{label{text='Player'};dropdown{id='wurmid';options='");
      Player[] players = Players.getInstance().getPlayers();
      Arrays.sort((Object[])players);
      this.playerIds.add(new Long(-10L));
      buf.append("none");

      for(int x = 0; x < players.length; ++x) {
         buf.append(",");
         buf.append(players[x].getName());
         this.playerIds.add(new Long(players[x].getWurmId()));
      }

      buf.append("'}}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parsePaymentQuestion(this);
   }

   Long getPlayerId(int aPlayerID) {
      return this.playerIds.get(aPlayerID);
   }
}
