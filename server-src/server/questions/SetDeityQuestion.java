package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public final class SetDeityQuestion extends Question {
   private final List<Player> playlist = new LinkedList<>();
   private final Map<Integer, Integer> deityMap = new ConcurrentHashMap<>();

   public SetDeityQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 26, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseSetDeityQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("harray{label{text='Player: '};dropdown{id='wurmid';options='");
      Player[] players = Players.getInstance().getPlayers();
      Arrays.sort((Object[])players);

      for(int x = 0; x < players.length; ++x) {
         if (x > 0) {
            buf.append(",");
         }

         buf.append(players[x].getName());
         this.playlist.add(players[x]);
      }

      buf.append("'}}");
      Deity[] deitys = Deities.getDeities();
      int counter = 0;
      buf.append("harray{label{text=\"Deity\"};dropdown{id=\"deityid\";options='None");

      for(Deity d : deitys) {
         this.deityMap.put(++counter, d.getNumber());
         buf.append(",");
         buf.append(d.getName());
      }

      buf.append("'}}");
      buf.append(
         "harray{label{text=\"Faith\"};input{maxchars=\"3\";id=\"faith\";text=\"1\"}label{text=\".\"}input{maxchars=\"6\"; id=\"faithdec\"; text=\"000000\"}}"
      );
      buf.append("harray{label{text=\"Favor\"};input{maxchars='3';id=\"favor\";text=\"1\"}}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public final int getDeityNumberFromArrayPos(int arrayPos) {
      return arrayPos == 0 ? 0 : this.deityMap.get(arrayPos);
   }

   Player getPlayer(int aPosition) {
      return this.playlist.get(aPosition);
   }
}
