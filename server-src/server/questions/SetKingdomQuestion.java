package com.wurmonline.server.questions;

import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class SetKingdomQuestion extends Question {
   private final List<Player> playerlist = new LinkedList<>();
   private final LinkedList<Kingdom> availKingdoms = new LinkedList<>();

   public SetKingdomQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 37, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseSetKingdomQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      if (this.getResponder().getPower() <= 0) {
         if (Kingdoms.isCustomKingdom(this.getResponder().getKingdomId())) {
            if (!this.getResponder().mayChangeKingdom(null) && !Servers.isThisATestServer()) {
               buf.append(
                  "text{text=\"You may not select to leave this kingdom right now. Maybe you are the mayor of a settlement, or converted too recently?\"}text{text=\"\"}"
               );
            } else {
               byte targetKingdom = this.getResponder().getKingdomTemplateId();
               if (Servers.isThisAChaosServer() && targetKingdom != 3) {
                  targetKingdom = 4;
               }

               buf.append("text{text=\"You may select to leave this kingdom. Make sure to do this in a safe place.\"}text{text=\"\"}");
               buf.append(
                  "text{type=\"italic\";text=\"Do you want to leave "
                     + Kingdoms.getNameFor(this.getResponder().getKingdomId())
                     + " for "
                     + Kingdoms.getNameFor(targetKingdom)
                     + "?\"}"
               );
               buf.append("radio{ group='kingd'; id='true';text='Yes'}");
               buf.append("radio{ group='kingd'; id='false';text='No';selected='true'}");
            }
         } else {
            buf.append("text{text=\"You may not leave this kingdom.\"}text{text=\"\"}");
         }
      } else {
         buf.append("harray{label{text='Player: '};dropdown{id='wurmid';options='");
         Player[] players = Players.getInstance().getPlayers();
         Arrays.sort((Object[])players);

         for(int x = 0; x < players.length; ++x) {
            if (x > 0) {
               buf.append(",");
            }

            this.playerlist.add(players[x]);
            buf.append(players[x].getName());
         }

         buf.append("'}}");
         buf.append("harray{label{text='Kingdom: '};dropdown{id='kingdomid';options=\"None");
         Kingdom[] kingdoms = Kingdoms.getAllKingdoms();

         for(int x = 0; x < kingdoms.length; ++x) {
            if (kingdoms[x].getId() != 0) {
               buf.append(",");
               this.availKingdoms.add(kingdoms[x]);
               buf.append(kingdoms[x].getName());
            }
         }

         buf.append("\"}}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   Player getPlayer(int aPosition) {
      return this.playerlist.get(aPosition);
   }

   List<Kingdom> getAvailKingdoms() {
      return this.availKingdoms;
   }
}
