package com.wurmonline.server.questions;

import com.wurmonline.server.behaviours.FishEnums;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.scripts.FishAI;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.Properties;

public final class CreatureDataQuestion extends Question implements ItemMaterials {
   private static final String red = "color=\"255,127,127\"";
   private static final String green = "color=\"127,255,127\"";
   private final Creature creature;

   public CreatureDataQuestion(Creature aResponder, Creature target) {
      super(aResponder, "Creature data", "Set the desired data:", 154, target.getWurmId());
      this.creature = target;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      FishAI.FishAIData faid = (FishAI.FishAIData)this.creature.getCreatureAIData();
      boolean accept = this.getBooleanProp("accept");
      boolean settarget = this.getBooleanProp("settarget");
      if (accept) {
         byte fishTypeId = Byte.parseByte(answers.getProperty("ftype"));
         float ql = Float.parseFloat(answers.getProperty("ql"));
         this.creature.setVisible(false);
         faid.setFishTypeId(fishTypeId);
         faid.setQL((double)ql);
         this.creature.setVisible(true);
         CreatureDataQuestion spm = new CreatureDataQuestion(this.getResponder(), this.creature);
         spm.sendQuestion();
      } else if (settarget) {
         float tx = Float.parseFloat(answers.getProperty("tx"));
         float ty = Float.parseFloat(answers.getProperty("ty"));
         if (tx != -1.0F && ty != -1.0F) {
            faid.setTargetPos(tx, ty);
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("label{text=\"Name: " + this.creature.getName() + "\"}");
      FishAI.FishAIData faid = (FishAI.FishAIData)this.creature.getCreatureAIData();
      FishEnums.FishData fd = faid.getFishData();
      String options = "None";

      for(FishEnums.FishData ffd : FishEnums.FishData.values()) {
         if (ffd.getTypeId() > 0) {
            options = options + "," + ffd.getName();
         }
      }

      buf.append("label{type=\"bolditalic\"; text=\"When changing type, accept before changing anything else. \"}");
      buf.append("table{rows=\"1\";cols=\"5\";");
      buf.append("label{text=\"Fish type:\"}dropdown{id=\"ftype\";options=\"" + options + "\";default=\"" + faid.getFishTypeId() + "\"}");
      buf.append("label{text=\" QL:\"}input{id=\"ql\"; maxchars=\"6\";text=\"" + faid.getQL() + "\"}");
      buf.append("harray{label{text=\" \"};button{text=\"Accept\";id=\"accept\"}}");
      buf.append("}");
      buf.append("label{type=\"bolditalic\"; text=\"Movement, set target position (m).\";hover=\"position in meters\"}");
      buf.append("table{rows=\"1\";cols=\"5\";");
      buf.append(
         "label{text=\"Target:\"}input{id=\"tx\"; maxchars=\"6\";text=\""
            + faid.getTargetPosX()
            + "\"}label{text=\",\"}input{id=\"ty\"; maxchars=\"6\";text=\""
            + faid.getTargetPosY()
            + "\"}"
      );
      buf.append("harray{label{text=\" \"};button{text=\"Set Target\";id=\"settarget\"}}");
      buf.append("}");
      buf.append("label{type=\"bolditalic\";text=\"Following cannot be changed, but are calculated from type and ql. \"}");
      buf.append("table{rows=\"1\";cols=\"6\";");
      buf.append(
         "label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Value\"};label{type=\"bold\";text=\"Base\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Value\"};label{type=\"bold\";text=\"Base\"}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Speed:\"};label{text=\""
            + faid.getSpeed()
            + "\"};label{text=\""
            + fd.getBaseSpeed()
            + "\"};label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"\"}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Body Strength:\"};label{text=\""
            + faid.getBodyStrength()
            + "\"};label{text=\""
            + fd.getBodyStrength()
            + "\"};label{type=\"bold\";text=\"Body Stamina:\"};label{text=\""
            + faid.getBodyStamina()
            + "\"};label{text=\""
            + fd.getBodyStamina()
            + "\"}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Body Control:\"};label{text=\""
            + faid.getBodyControl()
            + "\"};label{text=\""
            + fd.getBodyControl()
            + "\"};label{type=\"bold\";text=\"Mind Speed:\"};label{text=\""
            + faid.getMindSpeed()
            + "\"};label{text=\""
            + fd.getMindSpeed()
            + "\"}"
      );
      buf.append("}");
      buf.append("label{type=\"bolditalic\";text=\"Following cannot be changed, Fish Data from fish type id. \"}");
      buf.append("table{rows=\"1\";cols=\"6\";");
      buf.append(
         "label{type=\"bold\";text=\"Name:\"};label{text=\""
            + fd.getName()
            + "\"}label{text=\"\"};label{text=\"\"}label{type=\"bold\";text=\"Special:\"};label{"
            + this.showBoolean(fd.isSpecialFish())
            + "};"
      );
      buf.append(
         "label{type=\"bold\";text=\"Surface:\"};label{"
            + this.showBoolean(fd.onSurface())
            + "};label{type=\"bold\";text=\"Water:\"};label{"
            + this.showBoolean(fd.inWater())
            + "}label{type=\"bold\";text=\"Pond:\"};label{"
            + this.showBoolean(fd.inPond())
            + "};"
      );
      buf.append(
         "label{type=\"bold\";text=\"Lake:\"};label{"
            + this.showBoolean(fd.inLake())
            + "}label{type=\"bold\";text=\"Sea:\"};label{"
            + this.showBoolean(fd.inSea())
            + "};label{type=\"bold\";text=\"Shallows:\"};label{"
            + this.showBoolean(fd.inShallows())
            + "}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Min depth:\"};label{text=\""
            + fd.getMinDepth()
            + "\"}label{type=\"bold\";text=\"Max depth:\"};label{text=\""
            + fd.getMaxDepth()
            + "\"};label{type=\"bold\";text=\"Keeper weight:\"};label{text=\""
            + fd.getMinWeight()
            + "\"}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Use Pole:\"};label{"
            + this.showBoolean(fd.useFishingPole())
            + "}label{type=\"bold\";text=\"Use Net:\"};label{"
            + this.showBoolean(fd.useFishingNet())
            + "};label{type=\"bold\";text=\"Use Spear:\"};label{"
            + this.showBoolean(fd.useSpear())
            + "}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Use Basic Rod:\"};label{"
            + this.showBoolean(fd.useReelBasic())
            + "}label{type=\"bold\";text=\"Use Fine Rod:\"};label{"
            + this.showBoolean(fd.useReelFine())
            + "};label{type=\"bold\";text=\"Use Deep Water Rod:\"};label{"
            + this.showBoolean(fd.useReelWater())
            + "}"
      );
      buf.append(
         "label{type=\"bold\";text=\"Use Professional Rod:\"};label{"
            + this.showBoolean(fd.useReelProfessional())
            + "}label{type=\"bold\";text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"\"};label{text=\"\"}"
      );
      buf.append("}");
      buf.append("label{type=\"bolditalic\";text=\"Lots more could be added here...\";hover=\"e.g. feed heights, baits?, damage mod\"}");
      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(400, 440, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String showBoolean(boolean flag) {
      StringBuilder buf = new StringBuilder();
      if (flag) {
         buf.append("color=\"127,255,127\"");
      } else {
         buf.append("color=\"255,127,127\"");
      }

      buf.append("text=\"" + flag + "\"");
      return buf.toString();
   }
}
