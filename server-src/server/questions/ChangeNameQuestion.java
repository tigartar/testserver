package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChangeNameQuestion extends Question {
   private static final Logger logger = Logger.getLogger(ChangeNameQuestion.class.getName());
   private final int maxSize = 40;
   private final int minSize = 3;
   private Item certificate;

   public ChangeNameQuestion(Creature aResponder, Item cert) {
      super(aResponder, "Name change", "Do you wish to change your name?", 109, cert.getWurmId());
      this.certificate = cert;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      Creature responder = this.getResponder();
      String oldname = responder.getName();
      String newname = answers.getProperty("answer");
      String oldpw = answers.getProperty("oldpw");
      String newpw = answers.getProperty("newpw");
      if (this.certificate == null || this.certificate.deleted || this.certificate.getOwnerId() != this.getResponder().getWurmId()) {
         responder.getCommunicator().sendNormalServerMessage("You are no longer in possession of the certificate it seems.");
      } else if (oldpw == null || oldpw.length() < 6) {
         responder.getCommunicator().sendNormalServerMessage("The old password contains at least 6 characters.");
      } else if (newpw == null || newpw.length() < 6) {
         responder.getCommunicator().sendNormalServerMessage("The new password needs at least 6 characters.");
      } else if (newpw.length() > 40) {
         responder.getCommunicator().sendNormalServerMessage("The new password is over 40 characters long.");
      } else {
         String hashedpw = "";

         try {
            hashedpw = LoginHandler.hashPassword(oldpw, LoginHandler.encrypt(LoginHandler.raiseFirstLetter(this.getResponder().getName())));
         } catch (Exception var10) {
            logger.log(Level.WARNING, "Failed to encrypt pw for " + this.getResponder().getName() + " with " + oldpw);
         }

         if (!hashedpw.equals(((Player)this.getResponder()).getSaveFile().getPassword())) {
            responder.getCommunicator().sendNormalServerMessage("You provided the wrong password.");
         } else if (newname == null || newname.length() < 3) {
            responder.getCommunicator().sendNormalServerMessage("Your name remains the same since it would be too short.");
         } else if (QuestionParser.containsIllegalCharacters(newname)) {
            responder.getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
         } else if (newname.equalsIgnoreCase(this.getResponder().getName())) {
            responder.getCommunicator().sendNormalServerMessage("Your name remains the same.");
         } else if (newname.length() > 40) {
            responder.getCommunicator().sendNormalServerMessage("Too long. Your name remains the same.");
         } else {
            if (Deities.isNameOkay(newname)) {
               if (Players.getInstance().doesPlayerNameExist(newname)) {
                  responder.getCommunicator().sendNormalServerMessage("The name " + newname + " is already in use.");
               } else {
                  newname = LoginHandler.raiseFirstLetter(newname);
                  LoginServerWebConnection lsw = new LoginServerWebConnection();
                  String toReturn = lsw.renamePlayer(oldname, newname, newpw, this.getResponder().getPower());
                  responder.getCommunicator()
                     .sendNormalServerMessage("You try to change the name from " + oldname + " to " + newname + " and set the password to '" + newpw + "'.");
                  responder.getCommunicator().sendNormalServerMessage("The result is:");
                  responder.getCommunicator().sendNormalServerMessage(toReturn);
                  if (!toReturn.contains("Error.")) {
                     Items.destroyItem(this.certificate.getWurmId());
                     logger.info(
                        oldname
                           + " ("
                           + this.getResponder().getWurmId()
                           + ") changed "
                           + this.getResponder().getHisHerItsString()
                           + " name to "
                           + newname
                           + '.'
                     );
                     Server.getInstance().broadCastSafe(oldname + " changed " + this.getResponder().getHisHerItsString() + " name to " + newname + '.');
                     this.getResponder().refreshVisible();
                  }
               }
            } else {
               responder.getCommunicator().sendNormalServerMessage("The name  " + newname + " is illegal.");
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("text{text=\"The name change system is spread across several servers and the name is used in a lot of complex situations.\"}");
      buf.append("text{text=\"It will not work perfectly and there will be certain data loss, especially regarding signatures and statistics.\"}");
      buf.append("text{text=\"In case you are not prepared to risk this you should close this window and sell the certificate back.\"}");
      buf.append("text{text=\"What would you like your name to be?\"}");
      buf.append("input{id=\"answer\";maxchars=\"40\";text=\"" + this.getResponder().getName() + "\"}");
      buf.append("text{text=\"Your password is required for security reasons. You can keep your old password.\"}");
      buf.append("harray{label{text=\"Old password\"};input{id=\"oldpw\";maxchars=\"40\";text=\"\"};}");
      buf.append("harray{label{text=\"New password\"};input{id=\"newpw\";maxchars=\"40\";text=\"\"};}");
      buf.append("text{text=\"\"}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
