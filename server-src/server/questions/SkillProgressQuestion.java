package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkillProgressQuestion extends Question {
   private static final String progressDBString = "SELECT * FROM SKILLS WHERE OWNER=? AND NUMBER=?";
   private static final Logger logger = Logger.getLogger(SkillProgressQuestion.class.getName());
   boolean answering = false;
   String name = "";
   int skill = 0;

   public SkillProgressQuestion(Creature aResponder, long wurmId, String _name) {
      super(aResponder, "Skill progress check", "Progress for " + _name + ":", 124, wurmId);
      this.name = _name;
      this.answering = true;
   }

   public SkillProgressQuestion(Creature aResponder) {
      super(aResponder, "Skill progress check", "Select a player and skill to check latest progress", 124, -10L);
   }

   @Override
   public void answer(Properties answers) {
      if (this.getResponder().getPower() > 1) {
         String player = answers.getProperty("data1");
         if (player != null && player.length() > 0) {
            this.getResponder().getLogger().log(Level.INFO, this.getResponder().getName() + " checking " + player + " for skill progress.");
            logger.log(Level.INFO, this.getResponder().getName() + " checking " + player + " for skill progress.");
            player = LoginHandler.raiseFirstLetter(player);
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithName(player);
            if (pinf == null) {
               this.getResponder().getCommunicator().sendAlertServerMessage("No player found with name " + player + ".");
               return;
            }

            String sknums = answers.getProperty("data2");
            if (sknums == null || sknums.length() <= 0) {
               this.getResponder().getCommunicator().sendAlertServerMessage("No skill found in array.");
               return;
            }

            try {
               int sknum = Integer.parseInt(sknums);
               Collection<SkillTemplate> temps = SkillSystem.templates.values();
               SkillTemplate[] templates = temps.toArray(new SkillTemplate[temps.size()]);
               Arrays.sort((Object[])templates);
               int sk = templates[sknum].getNumber();
               SkillProgressQuestion newq = new SkillProgressQuestion(this.getResponder(), pinf.wurmId, pinf.getName());
               newq.skill = sk;
               newq.sendQuestion();
            } catch (Exception var10) {
               this.getResponder().getCommunicator().sendAlertServerMessage("No skill found in array at " + sknums + ".");
               return;
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      if (!this.answering) {
         buf.append("harray{label{text='Player name'};input{maxchars='40';id='data1'; text=''}}");
         buf.append("harray{label{text='Skill to check'}dropdown{id='data2';options='");
         Collection<SkillTemplate> temps = SkillSystem.templates.values();
         SkillTemplate[] templates = temps.toArray(new SkillTemplate[temps.size()]);
         Arrays.sort((Object[])templates);

         for(int x = 0; x < templates.length; ++x) {
            if (x > 0) {
               buf.append(",");
            }

            buf.append(templates[x].getName());
         }

         buf.append("'}}");
      } else {
         label92: {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
               dbcon = DbConnector.getPlayerDbCon();
               ps = dbcon.prepareStatement("SELECT * FROM SKILLS WHERE OWNER=? AND NUMBER=?");
               ps.setLong(1, this.getTarget());
               ps.setInt(2, this.skill);
               rs = ps.executeQuery();

               while(true) {
                  if (!rs.next()) {
                     break label92;
                  }

                  String skname = SkillSystem.getNameFor(this.skill);
                  buf.append("harray{label{type=\"bolditalic\";text=\"" + skname + "\"}}");
                  PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(this.getTarget());
                  if (pinf != null && pinf.currentServer != Servers.localServer.id) {
                     buf.append("text=\"" + pinf.getName() + " does not seem to currently be on this server!\"}");
                  }

                  buf.append("harray{label{text='Current value:'}label{text=\"" + String.valueOf(rs.getFloat("VALUE")) + "\"}}");
                  buf.append("harray{label{text='1 day ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY1")) + "\"}}");
                  buf.append("harray{label{text='2 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY2")) + "\"}}");
                  buf.append("harray{label{text='3 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY3")) + "\"}}");
                  buf.append("harray{label{text='4 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY4")) + "\"}}");
                  buf.append("harray{label{text='5 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY5")) + "\"}}");
                  buf.append("harray{label{text='6 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY6")) + "\"}}");
                  buf.append("harray{label{text='7 days ago:'};label{text=\"" + String.valueOf(rs.getFloat("DAY7")) + "\"}}");
                  buf.append("harray{label{text='2 weeks ago:'};label{text=\"" + String.valueOf(rs.getFloat("WEEK2")) + "\"}}");
                  buf.append("text{text=\"\"}");
                  buf.append("text{type=\"bolditalic\";text=\"Note that a 0 value usually means no change for the period or that the player was inactive.\"}");
               }
            } catch (SQLException var10) {
               logger.log(Level.WARNING, "Failed to show skill " + this.skill + " for " + this.name + " " + var10.getMessage(), (Throwable)(new Exception()));
               this.getResponder().getCommunicator().sendAlertServerMessage("Error when checking skill.");
            } finally {
               DbUtilities.closeDatabaseObjects(ps, rs);
               DbConnector.returnConnection(dbcon);
            }

            return;
         }
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
