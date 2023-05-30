package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class CultQuestion extends Question implements TimeConstants {
   private Cultist cultist;
   private final boolean leavePath;
   private final boolean askStatus;
   private final byte path;
   private static final Logger logger = Logger.getLogger(CultQuestion.class.getName());

   public CultQuestion(
      Creature aResponder, String aTitle, String aQuestion, long aTarget, @Nullable Cultist _cultist, byte _path, boolean leave, boolean _askStatus
   ) {
      super(aResponder, aTitle, aQuestion, 78, aTarget);
      this.cultist = _cultist;
      this.path = _path;
      this.leavePath = leave;
      this.askStatus = _askStatus;
   }

   @Override
   public void answer(Properties aAnswers) {
      if (!this.askStatus) {
         String prop = aAnswers.getProperty("quit");
         if (prop == null) {
            prop = aAnswers.getProperty("answer");
            if (prop != null) {
               try {
                  int num = Integer.parseInt(prop);
                  if (this.cultist == null) {
                     if (num == 1) {
                        if (this.getResponder().isPlayer() && System.currentTimeMillis() - ((Player)this.getResponder()).getLastChangedPath() < 86400000L) {
                           this.getResponder()
                              .getCommunicator()
                              .sendNormalServerMessage(
                                 "You recently left a cult and need to contemplate the changes for another "
                                    + Server.getTimeFor(((Player)this.getResponder()).getLastChangedPath() + 86400000L - System.currentTimeMillis())
                                    + " before embarking on a new philosophical journey."
                              );
                           return;
                        }

                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You decide to start pursuing the insights of " + Cults.getPathNameFor(this.path) + ".");
                        this.cultist = new Cultist(this.getResponder().getWurmId(), this.path);
                        this.getResponder().achievement(548);
                     } else {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to follow " + Cults.getPathNameFor(this.path) + ".");
                     }
                  } else if (num == Cults.getCorrectAnswerForNextLevel(this.cultist.getPath(), this.cultist.getLevel())) {
                     if (this.cultist == null) {
                        this.cultist = new Cultist(this.getResponder().getWurmId(), this.path);
                     }

                     this.getResponder().getCommunicator().sendSafeServerMessage(Cults.getCorrectAnswerStringForNextLevel(this.path, this.cultist.getLevel()));
                     this.cultist.increaseLevel();

                     try {
                        this.cultist.saveCultist(false);
                     } catch (IOException var5) {
                        logger.log(Level.WARNING, "Failed to set " + this.getResponder().getName() + " to level " + var5.getMessage(), (Throwable)var5);
                     }
                  } else if (this.cultist == null) {
                     this.getResponder().getCommunicator().sendNormalServerMessage(Cults.getWrongAnswerStringForLevel(this.path, (byte)0));
                  } else {
                     this.cultist.failedToLevel();
                     this.getResponder().getCommunicator().sendNormalServerMessage(Cults.getWrongAnswerStringForLevel(this.path, this.cultist.getLevel()));
                  }
               } catch (NumberFormatException var6) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("The answer you provided was impossible to understand. You are sorry.");
                  return;
               }
            } else {
               this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to answer the question right now and instead meditate more.");
            }
         } else {
            try {
               int num = Integer.parseInt(prop);
               if (num == 1) {
                  if (this.cultist == null) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You are not following a philosophical path!");
                  } else {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("You decide to stop pursuing the insights of " + Cults.getPathNameFor(this.path) + ".");

                     try {
                        this.cultist.deleteCultist();
                        if (this.getResponder().isPlayer()) {
                           ((Player)this.getResponder()).setLastChangedPath(System.currentTimeMillis());
                        }

                        return;
                     } catch (IOException var7) {
                        logger.log(Level.WARNING, this.getResponder().getName() + ":" + var7.getMessage(), (Throwable)var7);
                     }
                  }
               }
            } catch (NumberFormatException var8) {
               this.getResponder().getCommunicator().sendNormalServerMessage("The answer you provided was impossible to understand. You are sorry.");
               return;
            }

            this.getResponder()
               .getCommunicator()
               .sendNormalServerMessage("You decide to keep pursuing the insights of " + Cults.getPathNameFor(this.path) + ".");
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      int width = 300;
      int height = 330;
      if (this.askStatus) {
         buf.append("text{text='You consider the local leaders of the path:'}");
         Map<Integer, Set<Cultist>> treemap = Cultist.getCultistLeaders(this.cultist.getPath(), this.getResponder().getKingdomId());
         boolean showedLevel = false;
         int localServer = Servers.localServer.id;

         for(Integer level : treemap.keySet()) {
            Set<Cultist> subset = treemap.get(level);
            buf.append("text{text='" + Cults.getNameForLevel(this.cultist.getPath(), level.byteValue()) + ":'}");

            for(Cultist cist : subset) {
               PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(cist.getWurmId());
               if (pinf != null && pinf.currentServer == localServer) {
                  if (pinf.wurmId == this.cultist.getWurmId()) {
                     buf.append("text{type=\"bold\";text='" + pinf.getName() + " '}");
                  } else {
                     if (!showedLevel && level.byteValue() - this.cultist.getLevel() == 3) {
                        buf.append("text{type='bold';text='Those on this level may help you advance the path:'}");
                        showedLevel = true;
                     }

                     buf.append("label{text=\"" + pinf.getName() + " \"}");
                  }
               }
            }

            buf.append("text{text=''}");
            width = 500;
            height = 400;
         }
      } else if (this.leavePath) {
         buf.append("text{text='Select quit to stop following this path. The result is immediate and dramatic:'}");
         buf.append("radio{ group='quit'; id='0';text='Stay';selected='true'}");
         buf.append("radio{ group='quit'; id='1';text='Quit'}");
         buf.append("text{text=''}");
      } else if (this.cultist == null) {
         buf.append("text{text=\"As you meditate upon these things you realize that there is a pattern of thinking that you can try to follow.\"}");
         buf.append("text{text=\"If this path contains the truth or simply the figment of someones imagination, you do not know.\"}");
         buf.append("text{text=\"Nonetheless, it may pose an interesting challenge.\"}");
         buf.append("text{text=\"Do you wish to embark on the philosophical journey of " + Cults.getPathNameFor(this.path) + "?\"}");
         buf.append(
            "text{text=\"If you choose yes, know that you join the Cult of "
               + Cults.getPathNameFor(this.path)
               + " with secrets supposed to lead to enlightenment. Divulging those secrets may lead to expulsion.\"}"
         );
         buf.append(
            "text{type='bold';text=\"If you decide to join, you will be challenged by the selected path as you visit more places like this one and meditate.\"}"
         );
         buf.append("text{text=''}");
         buf.append("radio{ group='answer'; id='0';text='No';selected='true'}");
         buf.append("radio{ group='answer'; id='1';text='Yes'}");
      } else {
         buf.append(
            "text{text=\"If "
               + Cults.getPathNameFor(this.cultist.getPath())
               + " contains the truth or simply is the figment of someones imagination, you do not know.\"}"
         );
         buf.append("text{text=\"Nonetheless, it poses an interesting challenge.\"}");
         buf.append("text{text=\"The following question springs to mind:\"}");
         buf.append("text{type='bold';text=\"" + Cults.getQuestionForLevel(this.path, this.cultist.getLevel()) + "\"}");
         buf.append("text{text=''}");
         String[] answers = Cults.getAnswerAlternativesForLevel(this.path, this.cultist.getLevel());

         for(int x = 0; x < answers.length; ++x) {
            buf.append("radio{ group='answer'; id='" + x + "';text=\"" + answers[x] + "\"}");
         }

         buf.append(
            "text{text=\"Know that you are part of the Cult of "
               + Cults.getPathNameFor(this.path)
               + " with secrets supposed to lead to enlightenment. Divulging those secrets may lead to expulsion.\"}"
         );
         buf.append("text{text=''}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(width, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
