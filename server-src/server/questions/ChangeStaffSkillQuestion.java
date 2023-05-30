package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import java.util.Properties;

public class ChangeStaffSkillQuestion extends Question {
   public ChangeStaffSkillQuestion(Creature aResponder) {
      super(aResponder, "Switch skills", "Do you want to switch your spear and staff skills?", 110, aResponder.getWurmId());
   }

   @Override
   public void answer(Properties answers) {
      String key = "rd";
      String val = answers.getProperty("rd");
      if (Boolean.parseBoolean(val)) {
         if (this.getResponder().hasFlag(11) && this.getResponder().getPower() <= 0) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You have already switched those skills.");
            return;
         }

         Skill staff = null;

         try {
            staff = this.getResponder().getSkills().getSkill(10090);
         } catch (NoSuchSkillException var8) {
            staff = this.getResponder().getSkills().learn(10090, 1.0F);
         }

         Skill spear = null;

         try {
            spear = this.getResponder().getSkills().getSkill(10088);
         } catch (NoSuchSkillException var7) {
            spear = this.getResponder().getSkills().learn(10088, 1.0F);
         }

         if (spear != null && staff != null) {
            this.getResponder().getSkills().switchSkillNumbers(spear, staff);
            this.getResponder().setFlag(11, true);
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You lack one of the skills.");
         }
      } else {
         this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to switch those skills.");
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{text='Steel Spears had the stats Steel Staff should have had so you have the option to switch those skills once.'}");
      buf.append("text{text='Do you wish to switch your Staff skill with your Long Spear skill?'}");
      buf.append("radio{ group='rd'; id='true';text='Yes'}");
      buf.append("radio{ group='rd'; id='false';text='No';selected='true'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
