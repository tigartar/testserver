package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerProfileQuestion extends Question {
   private static final Logger logger = Logger.getLogger(PlayerProfileQuestion.class.getName());
   private static final int WIDTH = 420;
   private static final int HEIGHT = 540;
   private static final String OPT_NO_PMS = "opt1";
   private static final String OPT_CROSS_KINGDOM = "opt2";
   private static final String OPT_CROSS_SERVER = "opt3";
   private static final String OPT_FRIENDS_OVERRIDE = "opt4";
   private static final String OPT_HIDE_EQUIP_OPTIONS = "opt5";
   private static final String OPT_KINGDOM_CHAT = "opt6";
   private static final String OPT_KINGDOM_MESSAGE = "opt7";
   private static final String OPT_GLOBAL_KINGDOM_CHAT = "opt8";
   private static final String OPT_GLOBAL_KINGDOM_MESSAGE = "opt9";
   private static final String OPT_TRADE_CHANNEL = "opt10";
   private static final String OPT_TRADE_MESSAGE = "opt11";
   private static final String OPT_CA = "opt16";
   private static final String OPT_LOOT_ALLIANCE = "opt17";
   private static final String OPT_LOOT_VILLAGE = "opt18";
   private static final String OPT_LOOT_TRUSTED_FRIENDS = "opt19";
   private static final String OPT_SB_IDLEOFF = "opt20";
   private static final String OPT_PVP_BLOCK = "opt21";
   private static final String OPT_GV = "opt22";
   private static final String OPT_COOKING_AFFINITIES = "opt23";
   private static final String OPT_NO_WAGONER_CHAT = "opt24";
   private static final String OPT_SEE_PLAYER_TITLES = "opt25";
   private static final String OPT_SEE_VILLAGE_TITLES = "opt26";
   private static final String OPT_SHOW_OWN_VILLAGE_TITLES = "opt27";
   private static final String OPT_HIDE_MY_PVE_DEATHS = "opt28";
   private static final String OPT_IGNORE_PVE_DEATHS_TAB = "opt29";

   public PlayerProfileQuestion(Creature aResponder) {
      super(aResponder, aResponder.getName() + " profile", "Profile maintenance", 106, aResponder.getWurmId());
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 106) {
            boolean opt = Boolean.parseBoolean(aAnswer.getProperty("opt1"));
            this.checkFlag(1, opt, false, "incoming PMs");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt2"));
            this.checkFlag(2, opt, true, "cross kingdom PMs");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt3"));
            this.checkFlag(3, opt, true, "cross server PMs");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt4"));
            this.checkFlag(4, opt, false, "override for friend PMs");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt5"));
            this.checkFlag(7, opt, false, "equip options in menu");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt6"));
            this.checkFlag(29, opt, false, "kingdom chat");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt8"));
            this.checkFlag(30, opt, false, "global kingdom chat");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt10"));
            this.checkFlag(31, opt, false, "trade channel");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt7"));
            this.checkFlag(35, opt, false, "kingdom start message");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt9"));
            this.checkFlag(36, opt, false, "global kingdom start message");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt11"));
            this.checkFlag(37, opt, false, "trade start message");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt16"));
            boolean seeCA = ((Player)this.getResponder()).seesPlayerAssistantWindow();
            if (seeCA != opt) {
               if (opt) {
                  ((Player)this.getResponder()).togglePlayerAssistantWindow(true);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You have switched on CA Help.");
               } else {
                  ((Player)this.getResponder()).togglePlayerAssistantWindow(false);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You have switched off CA Help.");
               }
            }

            opt = Boolean.parseBoolean(aAnswer.getProperty("opt17"));
            this.checkFlag(32, opt, false, "alliance looting of your corpse");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt18"));
            this.checkFlag(33, opt, false, "village looting of your corpse");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt19"));
            this.checkFlag(34, opt, false, "trusted friends looting of your corpse");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt20"));
            this.checkFlag(43, opt, false, "auto-freezing sleep bonus after " + Server.getTimeFor(600000L) + " of inactivity");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt21"));
            this.checkFlag(44, opt, false, "blocking PvP travel");
            boolean sendextra = Boolean.parseBoolean(aAnswer.getProperty("sendextra"));
            ((Player)this.getResponder()).setSendExtraBytes(sendextra);
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt22"));
            boolean seeGV = ((Player)this.getResponder()).seesGVHelpWindow();
            if (seeGV != opt) {
               if (opt) {
                  ((Player)this.getResponder()).toggleGVHelpWindow(true);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You have switched on GV Help.");
               } else {
                  ((Player)this.getResponder()).toggleGVHelpWindow(false);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You have switched off GV Help.");
               }
            }

            opt = Boolean.parseBoolean(aAnswer.getProperty("opt23"));
            this.checkFlag(53, opt, true, "fixed cooking affinities");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt24"));
            this.checkFlag(54, opt, false, "hearing wagoner chat");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt25"));
            this.checkFlag(56, opt, false, "showing other player titles");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt26"));
            this.checkFlag(57, opt, false, "showing other player village title");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt27"));
            this.checkFlag(58, opt, false, "showing your village title");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt28"));
            this.checkFlag(59, opt, false, "participating in the PvE server death tabs");
            opt = Boolean.parseBoolean(aAnswer.getProperty("opt29"));
            this.checkFlag(60, opt, false, "viewing the Deaths tab on PvE");
         }
      }
   }

   public void checkFlag(int oldFlag, boolean newFlag, boolean state, String what) {
      if (oldFlag == 44
         && this.getResponder().getVehicle() != -10L
         && !this.getResponder().isVehicleCommander()
         && !((Player)this.getResponder()).isBlockingPvP()) {
         try {
            Item bItem = Items.getItem(this.getResponder().getVehicle());
            if (bItem.isBoat()) {
               this.getResponder().getCommunicator().sendNormalServerMessage("You cannot block PvP travel while embarked as a passenger on a boat.");
               return;
            }
         } catch (NoSuchItemException var6) {
         }
      }

      if (this.getResponder().hasFlag(oldFlag) != newFlag) {
         this.getResponder().setFlag(oldFlag, newFlag);
         String oo = newFlag == state ? "on " : "off ";
         this.getResponder().getCommunicator().sendNormalServerMessage("You have switched " + oo + what + ".");
         if (!newFlag) {
            switch(oldFlag) {
               case 29:
                  Players.getInstance().sendStartKingdomChat((Player)this.getResponder());
                  break;
               case 30:
                  Players.getInstance().sendStartGlobalKingdomChat((Player)this.getResponder());
                  break;
               case 31:
                  Players.getInstance().sendStartGlobalTradeChannel((Player)this.getResponder());
            }
         }

         if (oldFlag == 43 && ((Player)this.getResponder()).isSBIdleOffEnabled()) {
            ((Player)this.getResponder()).resetInactivity(true);
         }
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Player Chat settings\"}");
      buf.append("table{rows=\"5\";cols=\"3\";");
      buf.append(
         "label{text=\"1. Incoming PMs\"};radio{group=\"opt1\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(1)
            + "\";text=\"Allow\"};radio{group=\""
            + "opt1"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(1)
            + "\";text=\"Disallow\"};"
      );
      buf.append(
         "label{text=\"2. Cross-Kingdoms\"};radio{group=\"opt2\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(2)
            + "\";text=\"Same Only\"};radio{group=\""
            + "opt2"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(2)
            + "\";text=\"Any\"};"
      );
      buf.append(
         "label{text=\"3. Cross-Servers\"};radio{group=\"opt3\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(3)
            + "\";text=\"Local Only\"};radio{group=\""
            + "opt3"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(3)
            + "\";text=\"Any\"};"
      );
      buf.append(
         "label{text=\"4. Friends-Override\"};radio{group=\"opt4\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(4)
            + "\";text=\"Always\"};radio{group=\""
            + "opt4"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(4)
            + "\";text=\"Never\"};"
      );
      buf.append("}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Notes\"};");
      buf.append("text{text=\" GMs will still be able to PM you.\"}");
      buf.append("text{text=\" You will still be able to initiate a PM, so long as the person accepts them.\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Misc settings (show is default)\"}");
      if (Servers.localServer.isChallengeOrEpicServer()) {
         buf.append("table{rows=\"1\";cols=\"3\";");
      } else {
         buf.append("table{rows=\"2\";cols=\"3\";");
      }

      buf.append(
         "label{text=\"1. Equip options\"};radio{group=\"opt5\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(7)
            + "\";text=\"Show\"};radio{group=\""
            + "opt5"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(7)
            + "\";text=\"Hide\"};"
      );
      if (Server.getInstance().isPS() || !Servers.localServer.isChallengeOrEpicServer()) {
         buf.append(
            "label{text=\"2. Block PvP Crossing\"};radio{group=\"opt21\";id=\"false\";selected=\""
               + !this.getResponder().hasFlag(44)
               + "\";text=\"On\"};radio{group=\""
               + "opt21"
               + "\";id=\"true\";selected=\""
               + this.getResponder().hasFlag(44)
               + "\";text=\"Off\"};"
         );
      }

      buf.append("}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Other Chat Options (show is default)\"}");
      buf.append("table{rows=\"2\";cols=\"6\";");
      buf.append(
         "label{text=\"1. Kingdom Chat\"};radio{group=\"opt6\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(29)
            + "\";text=\"Show\"};radio{group=\""
            + "opt6"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(29)
            + "\";text=\"Hide\"};label{text=\" | Startup Message\"}radio{group=\""
            + "opt7"
            + "\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(35)
            + "\";text=\"Show\"};radio{group=\""
            + "opt7"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(35)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"2. Global Kingdom Chat\"};radio{group=\"opt8\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(30)
            + "\";text=\"Show\"};radio{group=\""
            + "opt8"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(30)
            + "\";text=\"Hide\"};label{text=\" | Startup Message\"}radio{group=\""
            + "opt9"
            + "\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(36)
            + "\";text=\"Show\"};radio{group=\""
            + "opt9"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(36)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"3. Global Trade Channel\"};radio{group=\"opt10\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(31)
            + "\";text=\"Show\"};radio{group=\""
            + "opt10"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(31)
            + "\";text=\"Hide\"};label{text=\" | Startup Message\"}radio{group=\""
            + "opt11"
            + "\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(37)
            + "\";text=\"Show\"};radio{group=\""
            + "opt11"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(37)
            + "\";text=\"Hide\"};"
      );
      boolean seeCA = ((Player)this.getResponder()).seesPlayerAssistantWindow();
      buf.append(
         "label{text=\"4. CA Help\"};radio{group=\"opt16\";id=\"true\";selected=\""
            + seeCA
            + "\";text=\"Show\"};radio{group=\""
            + "opt16"
            + "\";id=\"false\";selected=\""
            + !seeCA
            + "\";text=\"Hide\"};label{text=\"\"}label{text=\"\"};label{text=\"\"};"
      );
      if (((Player)this.getResponder()).maySeeGVHelpWindow()) {
         boolean seeGVHelp = ((Player)this.getResponder()).seesGVHelpWindow();
         buf.append(
            "label{text=\"5. GV Help\"};radio{group=\"opt22\";id=\"true\";selected=\""
               + seeGVHelp
               + "\";text=\"Show\"};radio{group=\""
               + "opt22"
               + "\";id=\"false\";selected=\""
               + !seeGVHelp
               + "\";text=\"Hide\"};label{text=\"\"}label{text=\"\"};label{text=\"\"};"
         );
      }

      buf.append("}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Your Corpse Lootability (allow is default)\"}");
      buf.append("table{rows=\"3\";cols=\"3\";");
      buf.append(
         "label{text=\"1. Alliance\"};radio{group=\"opt17\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(32)
            + "\";text=\"Allow\"};radio{group=\""
            + "opt17"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(32)
            + "\";text=\"Deny\"};"
      );
      buf.append(
         "label{text=\"2. Village\"};radio{group=\"opt18\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(33)
            + "\";text=\"Allow\"};radio{group=\""
            + "opt18"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(33)
            + "\";text=\"Deny\"};"
      );
      buf.append(
         "label{text=\"3. Trusted Friends\"};radio{group=\"opt19\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(34)
            + "\";text=\"Allow\"};radio{group=\""
            + "opt19"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(34)
            + "\";text=\"Deny\"};"
      );
      buf.append("}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Other Settings\"}");
      buf.append("table{rows=\"2\";cols=\"3\";");
      buf.append(
         "label{text=\"1. Sleep Bonus Idle Timeout ("
            + Server.getTimeFor(600000L)
            + ")\"};radio{group=\""
            + "opt20"
            + "\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(43)
            + "\";text=\"On\"};radio{group=\""
            + "opt20"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(43)
            + "\";text=\"Off\"};"
      );
      if (!this.getResponder().hasFlag(53)) {
         buf.append(
            "label{text=\"2. Fix Cooking Affinities\"};radio{group=\"opt23\";id=\"true\";selected=\""
               + this.getResponder().hasFlag(53)
               + "\";text=\"On\"};radio{group=\""
               + "opt23"
               + "\";id=\"false\";selected=\""
               + !this.getResponder().hasFlag(53)
               + "\";text=\"Off\"};"
         );
      } else {
         buf.append(
            "label{text=\"2. Fix Cooking Affinities\"};radio{group=\"opt23\";id=\"true\";selected=\""
               + this.getResponder().hasFlag(53)
               + "\";text=\"On\"};label{text=\"\"};"
         );
      }

      buf.append(
         "label{text=\"3. Wagoner Chat\"};radio{group=\"opt24\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(54)
            + "\";text=\"Hear\"};radio{group=\""
            + "opt24"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(54)
            + "\";text=\"Ignore\"};"
      );
      buf.append(
         "label{text=\"4. Show Other Player Titles\"};radio{group=\"opt25\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(56)
            + "\";text=\"Show\"};radio{group=\""
            + "opt25"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(56)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"5. Show Other Player Villages\"};radio{group=\"opt26\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(57)
            + "\";text=\"Show\"};radio{group=\""
            + "opt26"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(57)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"6. Show Own Village Titles\"};radio{group=\"opt27\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(58)
            + "\";text=\"Show\"};radio{group=\""
            + "opt27"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(58)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"7. Participate in PvE Deaths tab on Dying\"};radio{group=\"opt28\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(59)
            + "\";text=\"Show\"};radio{group=\""
            + "opt28"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(59)
            + "\";text=\"Hide\"};"
      );
      buf.append(
         "label{text=\"8. Ignore the PvE Deaths Tab\"};radio{group=\"opt29\";id=\"false\";selected=\""
            + !this.getResponder().hasFlag(60)
            + "\";text=\"Show\"};radio{group=\""
            + "opt29"
            + "\";id=\"true\";selected=\""
            + this.getResponder().hasFlag(60)
            + "\";text=\"Hide\"};"
      );
      buf.append("}");
      buf.append("text{type=\"italic\";color=\"237,28,36\";text=\"NOTE: Cooking affinities option cannot be turned off once enabled.\"};");
      buf.append("text{text=\"\"}");
      if (Servers.isThisATestServer()) {
         buf.append("text{type=\"italic\";text=\"Following are only shown on test server.\"}");
         buf.append("text{type=\"bold\";text=\"Player Session flags\"}");
         if (this.getResponder().getPower() >= 2) {
            buf.append("checkbox{id=\"signedin\";text=\"Signed In\";selected=\"" + ((Player)this.getResponder()).isSignedIn() + "\"}");
         }

         buf.append("checkbox{id=\"afk\";text=\"AFK\";selected=\"" + ((Player)this.getResponder()).isAFK() + "\"}");
         buf.append("checkbox{id=\"sendextra\";text=\"show Forage/Bot\";selected=\"" + ((Player)this.getResponder()).isSendExtraBytes() + "\"}");
         buf.append("text{text=\"\"}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(420, 540, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
