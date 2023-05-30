package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

public final class KingdomHistory extends Question {
   public KingdomHistory(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 66, aTarget);
   }

   @Override
   public void answer(Properties answers) {
   }

   @Override
   public void sendQuestion() {
      String lHtml = this.getBmlHeaderWithScroll();
      StringBuilder buf = new StringBuilder(lHtml);
      Map<Integer, King> kings = King.eras;
      Map<String, LinkedList<King>> counters = new HashMap<>();

      for(King k : kings.values()) {
         LinkedList<King> kinglist = counters.get(k.kingdomName);
         if (kinglist == null) {
            kinglist = new LinkedList<>();
         }

         kinglist.add(k);
         counters.put(k.kingdomName, kinglist);
      }

      for(Entry<String, LinkedList<King>> entry : counters.entrySet()) {
         this.addKing(entry.getValue(), entry.getKey(), buf);
      }

      if (Servers.localServer.isChallengeServer()) {
         for(Kingdom kingdom : Kingdoms.getAllKingdoms()) {
            if (kingdom.existsHere()) {
               buf.append("label{text=\"" + kingdom.getName() + " points:\"};");
               buf.append("label{text=\"" + kingdom.getWinpoints() + "\"};text{text=''};");
            }
         }
      }

      buf.append(this.createAnswerButton3());
      this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public void addKing(Collection<King> kings, String kingdomName, StringBuilder buf) {
      buf.append("text{type=\"bold\";text=\"History of " + kingdomName + ":\"}text{text=''}");
      buf.append(
         "table{rows='"
            + (kings.size() + 1)
            + "'; cols='10';label{text='Ruler'};label{text='Capital'};label{text='Start Land'};label{text='End Land'};label{text='Land Difference'};label{text='Levels Killed'};label{text='Levels Lost'};label{text='Levels Appointed'};label{text='Start Date'};label{text='End Date'};"
      );

      for(King k : kings) {
         buf.append("label{text=\"" + k.getFullTitle() + "\"};");
         buf.append("label{text=\"" + k.capital + "\"};");
         buf.append("label{text=\"" + String.format("%.2f%%", k.startLand) + "\"};");
         buf.append("label{text=\"" + String.format("%.2f%%", k.currentLand) + "\"};");
         buf.append("label{text=\"" + String.format("%.2f%%", k.currentLand - k.startLand) + "\"};");
         buf.append("label{text=\"" + k.levelskilled + "\"};");
         buf.append("label{text=\"" + k.levelslost + "\"};");
         buf.append("label{text=\"" + k.appointed + "\"};");
         buf.append("label{text=\"" + WurmCalendar.getDateFor(k.startWurmTime) + "\"};");
         if (k.endWurmTime > 0L) {
            buf.append("label{text=\"" + WurmCalendar.getDateFor(k.endWurmTime) + "\"};");
         } else {
            buf.append("label{text=\"N/A\"};");
         }
      }

      buf.append("}");
      buf.append("text{text=\"\"}");
   }
}
