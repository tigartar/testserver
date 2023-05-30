package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.Missions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionStats extends Question implements TimeConstants {
   private static final Logger logger = Logger.getLogger(MissionStats.class.getName());
   private final int targetMission;
   private MissionManager root = null;
   private final Map<Float, Integer> perfstats = new HashMap<>();
   private static final String red = "color=\"255,127,127\"";
   private static final String green = "color=\"127,255,127\"";

   public MissionStats(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 93, aTarget);
      this.targetMission = (int)aTarget;
   }

   @Override
   public void answer(Properties aAnswers) {
      this.setAnswer(aAnswers);
      boolean update = this.getBooleanProp("update");
      if (update) {
         MissionStats ms = new MissionStats(this.getResponder(), this.title, this.question, (long)this.targetMission);
         ms.setRoot(this.root);
         ms.sendQuestion();
      } else {
         if (this.root != null) {
            this.root.reshow();
         }
      }
   }

   @Override
   public void sendQuestion() {
      try {
         Mission m = Missions.getMissionWithId(this.targetMission);
         MissionPerformer[] mps = MissionPerformed.getAllPerformers();
         StringBuilder buf = new StringBuilder(this.getBmlHeader());
         buf.append("text{text=\"\"}");
         this.perfstats.clear();
         float total = 0.0F;

         for(int x = 0; x < mps.length; ++x) {
            MissionPerformed mp = mps[x].getMission(this.targetMission);
            if (mp != null) {
               float reached = mp.getState();
               Integer numbers = this.perfstats.get(reached);
               if (numbers == null) {
                  numbers = 1;
                  this.perfstats.put(reached, numbers);
               } else {
                  numbers = numbers + 1;
                  this.perfstats.put(reached, numbers);
               }

               ++total;
            }
         }

         buf.append("text{type=\"bold\";text=\"Total statistics for mission " + m.getName() + ":\"}");
         this.showStats(buf, total);
         this.perfstats.clear();
         total = 0.0F;

         for(int x = 0; x < mps.length; ++x) {
            MissionPerformed mp = mps[x].getMission(this.targetMission);
            if (mp != null && System.currentTimeMillis() - mp.getStartTimeMillis() < 86400000L) {
               float reached = mp.getState();
               Integer numbers = this.perfstats.get(reached);
               if (numbers == null) {
                  numbers = 1;
                  this.perfstats.put(reached, numbers);
               } else {
                  numbers = numbers + 1;
                  this.perfstats.put(reached, numbers);
               }

               ++total;
            }
         }

         buf.append("text{type=\"bold\";text=\"Statistics for mission " + m.getName() + " started within last 24 hours:\"}");
         this.showStats(buf, total);
         this.perfstats.clear();
         total = 0.0F;

         for(int x = 0; x < mps.length; ++x) {
            MissionPerformed mp = mps[x].getMission(this.targetMission);
            if (mp != null && System.currentTimeMillis() - mp.getStartTimeMillis() < 259200000L) {
               float reached = mp.getState();
               Integer numbers = this.perfstats.get(reached);
               if (numbers == null) {
                  numbers = 1;
                  this.perfstats.put(reached, numbers);
               } else {
                  numbers = numbers + 1;
                  this.perfstats.put(reached, numbers);
               }

               ++total;
            }
         }

         buf.append("text{type=\"bold\";text=\"Statistics for mission " + m.getName() + " started within last three days:\"}");
         this.showStats(buf, total);
         buf.append("harray{button{text=\"Refresh Statistics\";id=\"update\"};label{text=\"  \"};button{text=\"Back to mission list\";id=\"back\"};}");
         buf.append("}};null;null;}");
         this.getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      } catch (Exception var9) {
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("Problem sending a question about target mission: " + this.targetMission);
         }
      }
   }

   private void showStats(StringBuilder buf, float total) {
      buf.append("table{rows=\"1\"; cols=\"3\";label{text=\"Percent complete\"};label{text=\"People reached\"};label{text=\"Percent of total\"}");
      Float[] farr = this.perfstats.keySet().toArray(new Float[this.perfstats.size()]);
      Arrays.sort((Object[])farr);

      for(Float f : farr) {
         String perc = f + "";
         String colour = "";
         if ((double)f.floatValue() == -1.0) {
            perc = "Failed (-1.0)";
            colour = "color=\"255,127,127\"";
         } else if ((double)f.floatValue() == 100.0) {
            perc = "Completed (100.0)";
            colour = "color=\"127,255,127\"";
         }

         buf.append("label{" + colour + "text=\"" + perc + "\"};");
         buf.append("label{text=\"" + this.perfstats.get(f) + "\"};");
         buf.append("label{text=\"" + (float)this.perfstats.get(f).intValue() / total * 100.0F + "\"};");
      }

      buf.append("}");
      if (farr.length == 0) {
         buf.append("text{text=\"none\"}");
      }

      buf.append("text{text=\"\"}");
   }

   void setRoot(MissionManager aRoot) {
      this.root = aRoot;
   }
}
