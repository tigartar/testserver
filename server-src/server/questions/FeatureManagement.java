package com.wurmonline.server.questions;

import com.wurmonline.server.Features;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeatureManagement extends Question implements TimeConstants {
   private static final Logger logger = Logger.getLogger(FeatureManagement.class.getName());
   private boolean somethingChanged = false;

   public FeatureManagement(Creature aResponder, long aTarget) {
      super(aResponder, "Feature Management", "---- Manage Features ----", 102, aTarget);
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 102 && this.getResponder().getPower() >= 4) {
            Features.Feature[] features = Features.Feature.values();

            for(int x = 0; x < features.length; ++x) {
               if (features[x].isShown()) {
                  boolean enabled = aAnswer.getProperty("enable" + x).equals("true");
                  boolean override = aAnswer.getProperty("override" + x).equals("true");
                  boolean global = aAnswer.getProperty("global" + x).equals("true");
                  if (global || features[x].isEnabled() != enabled || features[x].isOverridden() != override) {
                     this.somethingChanged = true;
                  }

                  int featureId = features[x].getFeatureId();
                  Features.Feature.setOverridden(Servers.getLocalServerId(), featureId, override, enabled, global);
               }
            }

            if (this.somethingChanged) {
               FeatureManagement fm = new FeatureManagement(this.getResponder(), this.target);
               fm.sendQuestion();
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      if (this.getResponder().getPower() >= 4) {
         Features.Feature[] features = Features.Feature.values();
         buf.append("text{text=\"Current version is " + Features.getVerionsNo() + "\"};");
         buf.append(
            "text{text=\"The version number for each feature is used to determine if it is enabled. If on Live then the number needs to be equal or less than the current one above, and if on Test then everything is enable by default. Override will change this behaviour.\"};"
         );
         buf.append(
            "table{rows=\""
               + (features.length + 1)
               + "\";cols=\"5\";text{type=\"bold\";text=\"Enable  \"};text{type=\"bold\";text=\"Override  \"};text{type=\"bold\";text=\"Global  \"};text{type=\"bold\";text=\"Version\"};text{type=\"bold\";text=\"Name\"};"
         );

         for(int x = 0; x < features.length; ++x) {
            if (features[x].isShown()) {
               String colour = "";
               String bold = "";
               if (!features[x].isAvailable() && !features[x].isEnabled()) {
                  buf.append("label{text=\"\"};");
                  buf.append("label{text=\"\"};");
                  buf.append("label{text=\"\"};");
               } else {
                  buf.append("checkbox{id=\"enable" + x + "\";selected=\"" + features[x].isEnabled() + "\"};");
                  buf.append("checkbox{id=\"override" + x + "\";selected=\"" + features[x].isOverridden() + "\"};");
                  buf.append("checkbox{id=\"global" + x + "\";};");
               }

               if (features[x].getState() == Features.State.FUTURE) {
                  colour = "color=\"255,127,127\"";
               } else if (features[x].getState() == Features.State.INDEV) {
                  colour = "color=\"127,127,255\"";
               } else if (features[x].isEnabled()) {
                  colour = "color=\"127,255,127\"";
               }

               if (features[x].isOverridden()) {
                  bold = "type=\"bold\"";
               }

               buf.append("label{" + bold + colour + "text=\"" + features[x].getVersion() + "\"};");
               buf.append("label{" + bold + colour + "text=\"" + features[x].getName() + "\"};");
            }
         }

         buf.append("}");
         buf.append("text{type=\"bold\";text=\"--------------- Help -------------------\"}");
         buf.append("text{text=\"This is a list of the features that can be enabled or disabled by server or for all servers.\"}");
         buf.append("text{text=\"Setting the override will record the enabled state on the database.\"}");
         buf.append("text{text=\"Clearing the override will set the enabled back to it's default state.\"}");
         buf.append("text{type=\"bold\";text=\"Selecting global will attempt to change the same feature on all servers.\"}");
         buf.append("text{text=\"If anything gets altered then this window will be reshown.\"}");
         buf.append("text{type=\"italics\";text=\"Note only completed features are now shown.\"}");
         buf.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(500, 500, true, true, buf.toString(), 200, 200, 200, this.title);
      }
   }
}
