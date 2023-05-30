package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class TeleportQuestion extends Question {
   private final List<Player> playerlist = new LinkedList<>();
   private final List<Village> villagelist = new LinkedList<>();
   private String filter = "";
   private boolean filterPlayers = false;
   private boolean filterVillages = false;

   public TeleportQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 17, aTarget);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("harray{label{text='Tile x'};input{id='data1'; text='-1'}}");
      buf.append("harray{label{text='Tile y'};input{id='data2'; text='-1'}}");
      buf.append("harray{label{text='Surfaced: '};dropdown{id='layer';options='true,false'}}");
      Player[] players = Players.getInstance().getPlayers();
      Arrays.sort((Object[])players);

      for(int x = 0; x < players.length; ++x) {
         if (!this.filterPlayers || PlayerInfoFactory.wildCardMatch(players[x].getName().toLowerCase(), this.filter.toLowerCase())) {
            this.playerlist.add(players[x]);
         }
      }

      buf.append("text{text=''};");
      buf.append(
         "harray{label{text=\"Filter by: \"};input{maxchars=\"20\";id=\"filtertext\";text=\""
            + this.filter
            + "\"; onenter='filterboth'};label{text=' (Use * as a wildcard)'};}"
      );
      buf.append("harray{label{text='Player:    '}; dropdown{id='wurmid';options='");

      for(int x = 0; x < this.playerlist.size(); ++x) {
         if (x > 0) {
            buf.append(",");
         }

         buf.append(this.playerlist.get(x).getName());
      }

      buf.append("'};button{text='Filter'; id='filterplayer'}}");
      buf.append("harray{label{text='Village:   '}; dropdown{id='villid';default='0';options=\"none,");
      Village[] vills = Villages.getVillages();
      Arrays.sort((Object[])vills);
      int lastPerm = 0;

      for(int x = 0; x < vills.length; ++x) {
         if (!this.filterVillages || PlayerInfoFactory.wildCardMatch(vills[x].getName().toLowerCase(), this.filter.toLowerCase())) {
            if (vills[x].isPermanent) {
               this.villagelist.add(lastPerm, vills[x]);
               ++lastPerm;
            } else {
               this.villagelist.add(vills[x]);
            }
         }
      }

      for(int x = 0; x < this.villagelist.size(); ++x) {
         if (x > 0) {
            buf.append(",");
         }

         if (this.villagelist.get(x).isPermanent) {
            buf.append("#");
         }

         buf.append(this.villagelist.get(x).getName());
      }

      buf.append("\"};button{text='Filter'; id='filtervillage'}}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      boolean filterP = false;
      boolean filterV = false;
      String val = this.getAnswer().getProperty("filterplayer");
      if (val != null && val.equals("true")) {
         filterP = true;
      }

      val = this.getAnswer().getProperty("filtervillage");
      if (val != null && val.equals("true")) {
         filterV = true;
      }

      val = this.getAnswer().getProperty("filterboth");
      if (val != null && val.equals("true")) {
         filterP = true;
         filterV = true;
      }

      if (!filterP && !filterV) {
         QuestionParser.parseTeleportQuestion(this);
      } else {
         val = this.getAnswer().getProperty("filtertext");
         if (val == null || val.length() == 0) {
            val = "*";
         }

         TeleportQuestion tq = new TeleportQuestion(this.getResponder(), this.title, this.question, this.target);
         tq.filter = val;
         tq.filterPlayers = filterP;
         tq.filterVillages = filterV;
         tq.sendQuestion();
      }
   }

   Player getPlayer(int aPosition) {
      return this.playerlist.get(aPosition);
   }

   Village getVillage(int aPosition) {
      return this.villagelist.get(aPosition);
   }
}
