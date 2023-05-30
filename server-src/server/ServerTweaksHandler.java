package com.wurmonline.server;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;

public final class ServerTweaksHandler {
   public static boolean isTweakCommand(String message) {
      for(ServerTweaksHandler.Tweak tweak : ServerTweaksHandler.Tweak.values()) {
         if (message.startsWith(tweak.getCommand())) {
            return true;
         }
      }

      return false;
   }

   public static void handleTweakCommand(String message, Player admin) {
      StringTokenizer tokenizer = new StringTokenizer(message);
      String cmd = tokenizer.nextToken();
      ServerTweaksHandler.Tweak tweak = ServerTweaksHandler.Tweak.getByCommand(cmd);
      tweak.execute(tokenizer, admin);
   }

   private static boolean validatePassword(String pass, Player admin) {
      String adminPass = ServerProperties.getString("ADMINPASSWORD", "");
      if (adminPass.isEmpty()) {
         admin.getCommunicator().sendNormalServerMessage("There is no admin password on this server, so admin commands is disabled.");
         return false;
      } else if (pass.equals(adminPass)) {
         return true;
      } else {
         admin.getCommunicator().sendNormalServerMessage("Incorrect admin password.");
         return false;
      }
   }

   public static void handleUnknownCommad(StringTokenizer tokenizer, Player admin) {
      admin.getCommunicator().sendNormalServerMessage("Unknown command.");
   }

   private static boolean tokenCheck(ServerTweaksHandler.Tweak tweak, StringTokenizer tokenizer, Player admin) {
      int numTokens = tokenizer.countTokens();
      if (numTokens != tweak.tokenCount()) {
         String message = "Incorrect number of parameters! Provided: %d Expected: %d";
         admin.getCommunicator()
            .sendNormalServerMessage(String.format("Incorrect number of parameters! Provided: %d Expected: %d", numTokens, tweak.tokenCount()));
         return false;
      } else {
         return true;
      }
   }

   public static void handleSkillGainRateCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.SKILL_GAIN_RATE, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               float rate = Float.parseFloat(param);
               rate = Math.max(0.01F, rate);
               admin.getCommunicator().sendNormalServerMessage("Changed skill gain multiplier to: " + rate + ".");
               Servers.localServer.setSkillGainRate(rate);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleFieldGrowthCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.FIELD_GROWTH, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float time = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed field growth timer to: " + time.toString() + " hours.");
               Servers.localServer.setFieldGrowthTime((long)(time * 3600.0F * 1000.0F));
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleCharacteristicsStartCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.CHARACTERISTICS_START, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float charVal = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed characteristics start value to: " + charVal.toString() + ".");
               Servers.localServer.setSkillbasicval(charVal);
               Servers.localServer.saveNewGui(Servers.localServer.id);
               admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleMindLogicStartCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.MIND_LOGIC_START, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed mind logic start value to: " + val.toString() + ".");
               Servers.localServer.setSkillmindval(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
               admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleBodyControlStartCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.BC_START, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed body control start value to: " + val.toString() + ".");
               Servers.localServer.setSkillbcval(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
               admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleFightingStartCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.FIGHT_START, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed fighting start value to: " + val.toString() + ".");
               Servers.localServer.setSkillfightval(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
               admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleOverallStartCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.OVERALL_START, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed overall start skill value to: " + val.toString() + ".");
               Servers.localServer.setSkilloverallval(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
               admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handlePlayerCRCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.PLAYER_CR, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed player CR mod to: " + val.toString() + ".");
               Servers.localServer.setCombatRatingModifier(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleActionSpeedCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.ACTION_SPEED, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Float val = Float.parseFloat(param);
               admin.getCommunicator().sendNormalServerMessage("Changed action speed mod to: " + val.toString() + ".");
               Servers.localServer.setActionTimer(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleHOTACommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.HOTA, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               Integer val = Integer.parseInt(param);
               admin.getCommunicator().sendNormalServerMessage("Changed HOTA delay to: " + val.toString() + ".");
               Servers.localServer.setHotaDelay(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleMaxCreaturesCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.MAX_CREATURES, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Changed max creatures to: " + val + ".");
               Servers.localServer.maxCreatures = val;
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleAggCreaturesCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.AGG_PERCENT, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               float val = Float.parseFloat(param);
               val = Math.max(0.0F, Math.min(100.0F, val));
               admin.getCommunicator().sendNormalServerMessage("Changed aggressive creature % to: " + val + ".");
               Servers.localServer.percentAggCreatures = val;
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleUpkeepCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.UPKEEP, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            boolean val = Boolean.parseBoolean(param);
            admin.getCommunicator().sendNormalServerMessage("Changed upkeep to: " + val + ".");
            Servers.localServer.setUpkeep(val);
            Servers.localServer.saveNewGui(Servers.localServer.id);
         }
      }
   }

   public static void handleFreeDeedsCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.FREE_DEEDS, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            boolean val = Boolean.parseBoolean(param);
            admin.getCommunicator().sendNormalServerMessage("Changed free deeding to: " + val + ".");
            Servers.localServer.setFreeDeeds(val);
            Servers.localServer.saveNewGui(Servers.localServer.id);
         }
      }
   }

   public static void handleTraderMaxMoneyCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.TRADER_MAX_MONEY, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Changed trader max money to: " + val + " silver.");
               Servers.localServer.setTraderMaxIrons(val * 10000);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleTraderInitialMoneyCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.TRADER_INITIAL_MONEY, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Changed trader initial money to: " + val + " silver.");
               Servers.localServer.setInitialTraderIrons(val * 10000);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleMinimumHitsCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.MINING_HITS, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Changed minimum mining hits on rock to: " + val + ".");
               Servers.localServer.setTunnelingHits(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleBreedingTimeCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.BREEDING_TIME, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               long val = Long.parseLong(param);
               val = Math.max(1L, val);
               admin.getCommunicator().sendNormalServerMessage("Changed breeding time modifier to: " + val + ".");
               Servers.localServer.setBreedingTimer(val);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var6) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleTreeSpreadOddsCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.TREE_GROWTH, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Changed tree spread odds to: " + val + ".");
               Servers.localServer.treeGrowth = val;
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void handleMoneyPoolCommand(StringTokenizer tokenizer, Player admin) {
      if (tokenCheck(ServerTweaksHandler.Tweak.MONEY_POOL, tokenizer, admin)) {
         String param = tokenizer.nextToken();
         String pass = tokenizer.nextToken();
         if (validatePassword(pass, admin)) {
            try {
               int val = Integer.parseInt(param);
               val = Math.max(0, val);
               admin.getCommunicator().sendNormalServerMessage("Money pool will be set to: " + val + " after a restart.");
               Servers.localServer.setKingsmoneyAtRestart(val * 10000);
               Servers.localServer.saveNewGui(Servers.localServer.id);
            } catch (NumberFormatException var5) {
               admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
            }
         }
      }
   }

   public static void sendHelp(Player player) {
      Communicator com = player.getCommunicator();

      for(ServerTweaksHandler.Tweak tweak : ServerTweaksHandler.Tweak.values()) {
         if (tweak != ServerTweaksHandler.Tweak.UNKNOWN) {
            com.sendHelpMessage(tweak.parameterString + " - " + tweak.helpDescription);
         }
      }
   }

   public static enum Tweak {
      UNKNOWN("UNKNOWN", "", "", 0, ServerTweaksHandler::handleUnknownCommad),
      FIELD_GROWTH(
         "/setfieldgrowthtime",
         "/setfieldgrowthtime <time> <password>",
         "Sets how often fields will be polled.",
         2,
         ServerTweaksHandler::handleFieldGrowthCommand
      ),
      SKILL_GAIN_RATE(
         "/setskillgainmultiplier",
         "/setskillgainmultiplier <rate> <password>",
         "Skill gain rate multiplier.",
         2,
         ServerTweaksHandler::handleSkillGainRateCommand
      ),
      CHARACTERISTICS_START(
         "/setcharacteristicsstartvalue",
         "/setcharacteristicsstartvalue <value> <password>",
         "Sets the starting value of the characteristics skills for new players. (requires restart)",
         2,
         ServerTweaksHandler::handleCharacteristicsStartCommand,
         true
      ),
      MIND_LOGIC_START(
         "/setmindlogicstartvalue",
         "/setmindlogicstartvalue <value> <password>",
         "Sets the starting value of mind logic for new players. (requires restart)",
         2,
         ServerTweaksHandler::handleMindLogicStartCommand,
         true
      ),
      BC_START(
         "/setbodycontrolstartvalue",
         "/setbodycontrolstartvalue <value> <password>",
         "Sets the starting value of the body control skill for new players. (requires restart)",
         2,
         ServerTweaksHandler::handleBodyControlStartCommand,
         true
      ),
      FIGHT_START(
         "/setfightingstartvalue",
         "/setfightingstartvalue <value> <password>",
         "Sets the fighting skill start value for new players. (requires restart)",
         2,
         ServerTweaksHandler::handleFightingStartCommand,
         true
      ),
      OVERALL_START(
         "/setoverallstartskillvalue",
         "/setoverallstartskillvalue <value> <password>",
         "Sets the overall starting skill value for new players. (restart required)",
         2,
         ServerTweaksHandler::handleOverallStartCommand,
         true
      ),
      PLAYER_CR("/setplayercrmod", "/setplayercrmod <CR> <password>", "Sets the combat rating of players.", 2, ServerTweaksHandler::handlePlayerCRCommand),
      ACTION_SPEED(
         "/setactionspeedmod",
         "/setactionspeedmod <mod> <password>",
         "Speeds up or slows down action timers.",
         2,
         ServerTweaksHandler::handleActionSpeedCommand
      ),
      HOTA("/sethotadelay", "/sethotadelay <delay> <password>", "HOTA delay", 2, ServerTweaksHandler::handleHOTACommand),
      MAX_CREATURES(
         "/setmaxcreatures",
         "/setmaxcreatures <max> <password>",
         "Sets the maximum number of creatures that can naturally spawn on the server.",
         2,
         ServerTweaksHandler::handleMaxCreaturesCommand
      ),
      AGG_PERCENT(
         "/setmaxaggcreatures",
         "/setmaxaggcreatures <percent> <password>",
         "Sets the % of the creature pool that can be aggressive creatures.",
         2,
         ServerTweaksHandler::handleAggCreaturesCommand
      ),
      UPKEEP("/setupkeep", "/setupkeep <true/false> <password>", "Toggle settlement upkeep on or off.", 2, ServerTweaksHandler::handleUpkeepCommand),
      FREE_DEEDS(
         "/setfreedeeds", "/setfreedeeds <true/false> <password>", "Toggle free deed creation on or off.", 2, ServerTweaksHandler::handleFreeDeedsCommand
      ),
      TRADER_MAX_MONEY(
         "/settradermaxmoney",
         "/settradermaxmoney <silver> <password>",
         "Sets the max amount of money a trader can have.",
         2,
         ServerTweaksHandler::handleTraderMaxMoneyCommand
      ),
      TRADER_INITIAL_MONEY(
         "/settraderinitialmoney",
         "/settraderinitialmoney <silver> <password>",
         "Sets the initial amount of money a trader has.",
         2,
         ServerTweaksHandler::handleTraderInitialMoneyCommand
      ),
      MINING_HITS(
         "/setminimummininghits",
         "/setminimummininghits <hits> <password>",
         "Sets the amount of hits required to tunnel through rock.",
         2,
         ServerTweaksHandler::handleMinimumHitsCommand
      ),
      BREEDING_TIME(
         "/setbreedingtime",
         "/setbreedingtime <mod> <password>",
         "Modifier to speed up or slow down breeding.",
         2,
         ServerTweaksHandler::handleBreedingTimeCommand
      ),
      TREE_GROWTH(
         "/settreespreadodds",
         "/settreespreadodds <odds> <password>",
         "Toggles the spreading of trees and mushrooms.",
         2,
         ServerTweaksHandler::handleTreeSpreadOddsCommand
      ),
      MONEY_POOL(
         "/setmoneypool",
         "/setmoneypool <silver> <password>",
         "Sets the amount of money in the server pool. (requires restart)",
         2,
         ServerTweaksHandler::handleMoneyPoolCommand
      );

      final String command;
      final String parameterString;
      final String helpDescription;
      final int expectedNumberOfTokens;
      final BiConsumer<StringTokenizer, Player> cmd;
      final boolean requiresRestart;

      private Tweak(String _command, String _parameter, String _helpDescription, int numberOfTokens, BiConsumer<StringTokenizer, Player> _cmd) {
         this.command = _command;
         this.parameterString = _parameter;
         this.helpDescription = _helpDescription;
         this.expectedNumberOfTokens = numberOfTokens;
         this.cmd = _cmd;
         this.requiresRestart = false;
      }

      private Tweak(String _command, String _parameter, String _helpDescription, int numberOfTokens, BiConsumer<StringTokenizer, Player> _cmd, boolean restart) {
         this.command = _command;
         this.parameterString = _parameter;
         this.helpDescription = _helpDescription;
         this.expectedNumberOfTokens = numberOfTokens;
         this.cmd = _cmd;
         this.requiresRestart = restart;
      }

      public final String getCommand() {
         return this.command;
      }

      public final String getParameterString() {
         return this.parameterString;
      }

      public final int tokenCount() {
         return this.expectedNumberOfTokens;
      }

      public final void execute(StringTokenizer tokenizer, Player admin) {
         if (this.cmd != null) {
            this.cmd.accept(tokenizer, admin);
         }
      }

      public static final ServerTweaksHandler.Tweak getByCommand(String cmd) {
         for(ServerTweaksHandler.Tweak tweak : values()) {
            if (tweak.getCommand().equalsIgnoreCase(cmd)) {
               return tweak;
            }
         }

         return UNKNOWN;
      }
   }
}
