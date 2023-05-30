package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.skills.Skills;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

public final class LearnSkillQuestion extends Question {
   public LearnSkillQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 16, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseLearnSkillQuestion(this);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("harray{label{text='Skill'}dropdown{id='data1';options='");
      Collection<SkillTemplate> temps = SkillSystem.templates.values();
      SkillTemplate[] templates = temps.toArray(new SkillTemplate[temps.size()]);
      Arrays.sort((Object[])templates);
      Creature receiver = null;
      boolean hadError = false;

      try {
         if (WurmId.getType(this.target) != 1 && WurmId.getType(this.target) != 0) {
            receiver = this.getResponder();
         } else {
            receiver = Server.getInstance().getCreature(this.target);
         }

         Skills skills = receiver.getSkills();

         for(int x = 0; x < templates.length; ++x) {
            if (x > 0) {
               buf.append(",");
            }

            int sk = templates[x].getNumber();

            try {
               Skill skill = skills.getSkill(sk);
               String affs = "*****".substring(0, skill.affinity);
               buf.append(templates[x].getName() + " " + affs + " (" + skill.getKnowledge() + ")");
            } catch (NoSuchSkillException var13) {
               buf.append(templates[x].getName());
            }
         }
      } catch (NoSuchPlayerException var14) {
         hadError = true;
      } catch (NoSuchCreatureException var15) {
         hadError = true;
      }

      if (hadError) {
         for(int x = 0; x < templates.length; ++x) {
            if (x > 0) {
               buf.append(",");
            }

            buf.append(templates[x].getName());
         }
      }

      buf.append("'}}");
      buf.append("label{type=\"bolditalic\";text=\"Skill of 0 = no change\"}");
      buf.append(
         "harray{label{text=\"Skill level\"}input{maxchars=\"3\"; id=\"val\"; text=\"0\"}label{text=\".\"}input{maxchars=\"6\"; id=\"dec\"; text=\"000000\"}}"
      );
      buf.append(
         "harray{label{text=\"Affinities\"}radio{group=\"aff\";id=\"-1\";text=\"Leave as is\";selected=\"true\"};radio{group=\"aff\";id=\"0\";text=\"None\"};radio{group=\"aff\";id=\"1\";text=\"One\"};radio{group=\"aff\";id=\"2\";text=\"Two\"};radio{group=\"aff\";id=\"3\";text=\"Three\"};radio{group=\"aff\";id=\"4\";text=\"Four\"};radio{group=\"aff\";id=\"5\";text=\"Five\"}}"
      );
      float align = this.getResponder().getAlignment();
      buf.append("text{text=\"\"}");
      buf.append("label{type=\"bolditalic\";text=\"Alignment, leave blank for no change\"}");
      buf.append("harray{label{text=\"Alignment (" + align + ")\"}input{maxchars=\"4\"; id=\"align\"; text=\"\"}}");
      int karma = this.getResponder().getKarma();
      buf.append("label{type=\"bolditalic\";text=\"Karma, leave blank for no change\"}");
      buf.append("harray{label{text=\"Karma (" + karma + ")\"}input{maxchars=\"5\"; id=\"karma\"; text=\"\"}}");
      int height = 270;
      if (WurmId.getType(this.target) == 0
         && Servers.isThisATestServer()
         && (this.getResponder().getPower() == 5 || this.getResponder().getName().equals("Hestia"))) {
         height += 70;
         buf.append("label{text=\"----- Cultist --- Test Server Only -----\"}");
         Cultist cultist = Cultist.getCultist(this.target);
         byte path = 0;
         byte level = 0;
         if (cultist != null) {
            path = cultist.getPath();
            level = cultist.getLevel();
         }

         String pathName = this.getShortPathName(path);
         buf.append(
            "harray{label{text=\"Path ("
               + pathName
               + ")\"}dropdown{id=\"path\";options=\"none,Love,Hate,Knowledge,Insanity,Power\";default=\""
               + path
               + "\"}}"
         );
         buf.append("harray{label{text=\"Level (" + level + ") leave blank for no change\"}input{maxchars=\"2\"; id=\"level\"; text=\"\"}}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(360, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String getShortPathName(byte path) {
      switch(path) {
         case 1:
            return "Love";
         case 2:
            return "Hate";
         case 3:
            return "Knowledge";
         case 4:
            return "Insanity";
         case 5:
            return "Power";
         default:
            return "none";
      }
   }
}
