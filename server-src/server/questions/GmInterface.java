package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Message;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmInterface extends Question implements TimeConstants {
   private static final Logger logger = Logger.getLogger(GmInterface.class.getName());
   private final LinkedList<Player> playlist = new LinkedList<>();
   private final LinkedList<String> iplist = new LinkedList<>();
   private static final String muteReasonOne = "Profane language";
   private static final String muteReasonTwo = "Racial or sexist remarks";
   private static final String muteReasonThree = "Staff bashing";
   private static final String muteReasonFour = "Harassment";
   private static final String muteReasonFive = "Spam";
   private static final String muteReasonSix = "Insubordination";
   private static final String muteReasonSeven = "Repeated warnings";
   private PlayerInfo playerInfo;
   private Player targetPlayer;
   private boolean doneSomething = false;

   public GmInterface(Creature aResponder, long aTarget) {
      super(aResponder, "Player Management", "How may we help you?", 83, aTarget);
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      if (this.type == 0) {
         logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
      } else {
         if (this.type == 83 && (this.getResponder().getPower() >= 2 || this.getResponder().mayMute())) {
            this.doneSomething = false;
            String pname = aAnswer.getProperty("pname");
            if (pname == null || pname.length() == 0) {
               String pid = aAnswer.getProperty("ddname");
               int num = Integer.parseInt(pid);
               if (num > 0) {
                  this.targetPlayer = this.playlist.get(num - 1);
                  pname = this.targetPlayer.getName();
                  if (!this.targetPlayer.hasLink()) {
                     this.targetPlayer = null;
                  }
               }
            }

            boolean nameSpecified = pname != null && pname.length() > 0;
            if (nameSpecified) {
               this.playerInfo = PlayerInfoFactory.createPlayerInfo(pname);

               try {
                  this.playerInfo.load();
               } catch (IOException var22) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("Failed to load data for the player with name " + pname + ".");
                  return;
               }

               if (this.playerInfo == null || this.playerInfo.wurmId <= 0L) {
                  long[] var10000 = new long[]{(long)Servers.localServer.id, -1L};
                  LoginServerWebConnection lsw = new LoginServerWebConnection();

                  long[] var30;
                  try {
                     var30 = lsw.getCurrentServer(pname, -1L);
                  } catch (Exception var20) {
                     var30 = new long[]{-1L, -1L};
                  }

                  if (var30[0] == -1L) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Player with name " + pname + " not found anywhere!.");
                  } else {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage(
                           "Player with name "
                              + pname
                              + " has never been on this server, but is currently on server "
                              + var30[0]
                              + ", their WurmId is "
                              + var30[1]
                              + "."
                        );
                  }

                  return;
               }

               if (this.getResponder().mayMute() || this.getResponder().getPower() >= 2) {
                  this.checkMute(pname);
               }

               if (this.getResponder().getPower() >= 2) {
                  this.checkBans(pname);
                  String key = "summon";
                  String val = aAnswer.getProperty(key);
                  if (val != null) {
                     boolean summon = val.equals("true");
                     if (summon) {
                        if (pname.equalsIgnoreCase(this.getResponder().getName())) {
                           this.getResponder().getCommunicator().sendNormalServerMessage("You cannot summon yourself.");
                        } else {
                           this.doneSomething = true;
                           int tilex = this.getResponder().getTileX();
                           int tiley = this.getResponder().getTileY();
                           byte layer = (byte)this.getResponder().getLayer();
                           key = "tilex";
                           val = aAnswer.getProperty(key);
                           if (val != null && val.length() > 0) {
                              tilex = Integer.parseInt(val);
                           }

                           key = "tiley";
                           val = aAnswer.getProperty(key);
                           if (val != null && val.length() > 0) {
                              tiley = Integer.parseInt(val);
                           }

                           key = "surfaced";
                           val = aAnswer.getProperty(key);
                           if (val != null && val.length() > 0) {
                              layer = (byte)(val.equals("true") ? 0 : -1);
                           }

                           QuestionParser.summon(pname, this.getResponder(), tilex, tiley, layer);
                        }
                     }
                  }

                  key = "locate";
                  val = aAnswer.getProperty(key);
                  if (val != null) {
                     boolean locate = val.equals("true");
                     if (locate) {
                        logger.log(Level.INFO, this.getResponder().getName() + " locating " + pname);
                        this.doneSomething = true;
                        if (!LocatePlayerQuestion.locateCorpse(pname, this.getResponder(), 100.0, true)) {
                           this.getResponder().getCommunicator().sendNormalServerMessage("No such soul found.");
                        }
                     }
                  }
               }
            }

            if (!this.doneSomething && this.getResponder().getPower() >= 2) {
               String key = "gmtool";
               String val = aAnswer.getProperty("gmtool");
               if (val != null) {
                  boolean gmtool = val.equals("true");
                  if (gmtool) {
                     String strWurmId = aAnswer.getProperty("wurmid");
                     String searchemail = aAnswer.getProperty("searchemail");
                     String searchip = aAnswer.getProperty("searchip");
                     boolean wurmIdSpecified = strWurmId != null && strWurmId.length() > 0;
                     boolean searchemailSpecified = searchemail != null && searchemail.length() > 0;
                     boolean searchipSpecified = searchip != null && searchip.length() > 0;
                     int optCount = 0;
                     if (nameSpecified) {
                        ++optCount;
                     }

                     if (wurmIdSpecified) {
                        ++optCount;
                     }

                     if (searchemailSpecified) {
                        ++optCount;
                     }

                     if (searchipSpecified) {
                        ++optCount;
                     }

                     if (optCount != 1) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Name or Number or Email search or IP search must be specified");
                     } else {
                        long toolWurmId = -1L;
                        byte toolType = 1;
                        byte toolSubType = 1;
                        String toolSearch = "";
                        if (this.getResponder().getLogger() != null && this.playerInfo != null) {
                           this.getResponder().getLogger().log(Level.INFO, "GM Tool for " + this.playerInfo.getName() + ", " + this.playerInfo.wurmId);
                        }

                        if (nameSpecified) {
                           toolWurmId = this.playerInfo.wurmId;
                        } else if (wurmIdSpecified) {
                           try {
                              toolWurmId = Long.parseLong(strWurmId);
                           } catch (Exception var21) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("Wurm ID is not a number!");
                              return;
                           }
                        } else if (searchemailSpecified) {
                           toolType = 2;
                           toolSubType = 1;
                           toolSearch = searchemail;
                        } else if (searchipSpecified) {
                           toolType = 2;
                           toolSubType = 2;
                           toolSearch = searchip;
                        }

                        this.doneSomething = true;
                        GmTool gt = new GmTool(this.getResponder(), toolType, toolSubType, toolWurmId, toolSearch, "", 50, (byte)0);
                        gt.sendQuestion();
                     }
                  }
               }

               if (!this.doneSomething) {
                  if (!nameSpecified) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("No player name provided. Doing nothing!");
                  } else {
                     this.getResponder().getCommunicator().sendNormalServerMessage("Nothing selected. Doing nothing!");
                  }
               }
            }
         }
      }
   }

   private void checkMute(String pname) {
      String muteReason = this.getAnswer().getProperty("mutereason");
      String mutereasontb = this.getAnswer().getProperty("mutereasontb");
      if (mutereasontb != null && mutereasontb.length() > 2) {
         muteReason = mutereasontb;
      } else if (muteReason != null && muteReason.length() > 0) {
         try {
            int reason = Integer.parseInt(muteReason);
            switch(reason) {
               case 0:
                  muteReason = "Profane language";
                  break;
               case 1:
                  muteReason = "Racial or sexist remarks";
                  break;
               case 2:
                  muteReason = "Staff bashing";
                  break;
               case 3:
                  muteReason = "Harassment";
                  break;
               case 4:
                  muteReason = "Spam";
                  break;
               case 5:
                  muteReason = "Insubordination";
                  break;
               case 6:
                  muteReason = "Repeated warnings";
                  break;
               default:
                  logger.warning("Unexpected parsed value: " + reason + " from mute reason: " + muteReason + ". Responder: " + this.getResponder());
            }
         } catch (NumberFormatException var12) {
            logger.log(
               Level.WARNING, "Problem parsing the mute reason: " + muteReason + ". Responder: " + this.getResponder() + " due to " + var12, (Throwable)var12
            );
         }
      }

      String key = "mutewarn";
      String val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true") && this.targetPlayer != null) {
         if (this.targetPlayer.getPower() <= this.getResponder().getPower()) {
            this.logMgmt("mutewarns " + pname + " (" + muteReason + ")");
            this.targetPlayer
               .getCommunicator()
               .sendAlertServerMessage(
                  this.getResponder().getName()
                     + " issues a warning that you may be muted. Be silent for a while and try to understand why or change the subject of your conversation please."
               );
            if (muteReason.length() > 0) {
               this.targetPlayer.getCommunicator().sendAlertServerMessage("The reason for this is '" + muteReason + "'");
            }

            this.getResponder()
               .getCommunicator()
               .sendSafeServerMessage("You warn " + this.targetPlayer.getName() + " that " + this.targetPlayer.getHeSheItString() + " may be muted.");
            this.getResponder().getCommunicator().sendSafeServerMessage("The reason you gave was '" + muteReason + "'.");
            this.doneSomething = true;
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You threaten " + pname + " with muting!");
            this.targetPlayer.getCommunicator().sendNormalServerMessage(this.getResponder().getName() + " tried to threaten you with muting!");
            if (muteReason.length() > 0) {
               this.targetPlayer.getCommunicator().sendNormalServerMessage("The formal reason for this is '" + muteReason + "'");
            }

            this.doneSomething = true;
         }
      }

      key = "unmute";
      val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true")) {
         if (this.playerInfo != null) {
            this.playerInfo.setMuted(false, "", 0L);
            this.logMgmt("unmutes " + pname);
            this.doneSomething = true;
            this.getResponder().getCommunicator().sendNormalServerMessage("You have given " + pname + " the voice back.");
         }

         if (this.targetPlayer != null) {
            this.targetPlayer.getCommunicator().sendAlertServerMessage("You have been given your voice back and can shout again.");
         }
      }

      int hours = 1;
      key = "mute";
      val = this.getAnswer().getProperty(key);
      long expiry = 0L;
      if (val != null && val.equals("true")) {
         logger.log(Level.INFO, "Muting");
         this.doneSomething = true;
         String muteTime = this.getAnswer().getProperty("mutetime");
         if (muteTime != null && muteTime.length() > 0) {
            try {
               int index = Integer.parseInt(muteTime);
               switch(index) {
                  case 0:
                     hours = 1;
                     break;
                  case 1:
                     hours = 2;
                     break;
                  case 2:
                     hours = 5;
                     break;
                  case 3:
                     hours = 8;
                     break;
                  case 4:
                     hours = 24;
                     break;
                  case 5:
                     hours = 48;
                     break;
                  default:
                     logger.warning("Unexpected muteTime value: " + muteTime + ". Responder: " + this.getResponder());
               }

               expiry = System.currentTimeMillis() + (long)hours * 3600000L;
            } catch (NumberFormatException var11) {
               this.getResponder().getCommunicator().sendNormalServerMessage("An error occurred with the number of hours for the mute: " + muteTime + ".");
               return;
            }
         }

         if (expiry > System.currentTimeMillis()) {
            logger.log(Level.INFO, "Muting");
            if (this.playerInfo != null) {
               if (this.playerInfo.getPower() >= this.getResponder().getPower() && this.playerInfo.getPower() != 0) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("You are too weak to mute " + pname + '!');
               } else {
                  this.playerInfo.setMuted(true, muteReason, expiry);
                  this.getResponder().getCommunicator().sendNormalServerMessage("You have muted " + this.playerInfo.getName() + " for " + hours + " hours.");
                  this.logMgmt("muted " + pname + " for " + hours + " hours. Reason: " + muteReason);
                  if (this.targetPlayer != null) {
                     this.targetPlayer
                        .getCommunicator()
                        .sendAlertServerMessage(
                           "You have been muted by "
                              + this.getResponder().getName()
                              + " for "
                              + hours
                              + " hours and cannot shout anymore. Reason: "
                              + muteReason
                        );
                  } else {
                     this.getResponder().getCommunicator().sendNormalServerMessage(this.playerInfo.getName() + " is offline.");
                  }
               }
            }
         }
      }
   }

   private void checkBans(String pname) {
      String banTime = this.getAnswer().getProperty("bantime");
      long expiry = 0L;
      int days = 0;
      String banReason = this.getAnswer().getProperty("banreason");
      if (banReason == null || banReason.length() < 2) {
         banReason = "Banned";
      }

      String key = "pardon";
      String val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true")) {
         this.doneSomething = true;
         if (this.playerInfo != null && this.playerInfo.isBanned()) {
            try {
               this.playerInfo.setBanned(false, "", 0L);
               String bip = this.playerInfo.getIpaddress();
               Players.getInstance().removeBan(bip);
               this.getResponder().getCommunicator().sendSafeServerMessage("You have gratiously pardoned " + pname + " and the ipaddress " + bip);
               this.log("pardons player " + pname + " and ipaddress " + bip + '.');
            } catch (IOException var25) {
            }
         }
      }

      if (banTime != null && banTime.length() > 0) {
         try {
            int index = Integer.parseInt(banTime);
            switch(index) {
               case 0:
                  days = 1;
                  break;
               case 1:
                  days = 3;
                  break;
               case 2:
                  days = 7;
                  break;
               case 3:
                  days = 30;
                  break;
               case 4:
                  days = 90;
                  break;
               case 5:
                  days = 365;
                  break;
               case 6:
                  days = 9999;
                  break;
               default:
                  logger.warning("Unexpected banTime value: " + banTime + ". Responder: " + this.getResponder());
            }

            expiry = System.currentTimeMillis() + (long)days * 86400000L;
         } catch (NumberFormatException var24) {
            this.getResponder().getCommunicator().sendNormalServerMessage("An error occurred with the number of days for the ban: " + banTime + ".");
            return;
         }
      }

      boolean bannedip = false;
      key = "ban";
      val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true") && expiry > System.currentTimeMillis()) {
         this.doneSomething = true;

         try {
            if (this.targetPlayer != null && this.targetPlayer.hasLink()) {
               this.targetPlayer
                  .getCommunicator()
                  .sendAlertServerMessage("You have been banned for " + days + " days and thrown out from the game. The reason is " + banReason, (byte)1);
               this.targetPlayer.setFrozen(true);
               this.targetPlayer.logoutIn(10, "banned");
               this.getResponder()
                  .getCommunicator()
                  .sendSafeServerMessage(
                     String.format("Player %s was successfully found and will be removed from the world in 10 seconds.", this.targetPlayer.getName())
                  );
            } else {
               this.getResponder()
                  .getCommunicator()
                  .sendSafeServerMessage(
                     String.format("Something went wrong and %s was not removed from the world. You may need to kick them.", this.playerInfo.getName())
                  );
            }

            this.playerInfo.setBanned(true, banReason, expiry);
            key = "banip";
            val = this.getAnswer().getProperty(key);
            if (val != null && val.equals("true")) {
               bannedip = true;
               String bip = this.playerInfo.getIpaddress();
               this.getResponder()
                  .getCommunicator()
                  .sendSafeServerMessage("You ban and kick " + pname + ". The server won't accept connections from " + bip + " anymore.", (byte)1);
               this.log("bans player " + pname + " for " + days + " days and ipaddress " + bip + " for " + Math.min(days, 7) + " days.");
               if (Servers.localServer.LOGINSERVER) {
                  Players.getInstance().addBannedIp(bip, "[" + pname + "] " + banReason, Math.min(expiry, System.currentTimeMillis() + 604800000L));
               } else {
                  try {
                     LoginServerWebConnection c = new LoginServerWebConnection();
                     this.getResponder().getCommunicator().sendSafeServerMessage(c.addBannedIp(bip, "[" + pname + "] " + banReason, Math.min(days, 7)));
                  } catch (Exception var23) {
                     this.getResponder().getCommunicator().sendAlertServerMessage("Failed to ban on login server:" + var23.getMessage());
                     logger.log(Level.INFO, this.getResponder().getName() + " banning ip on login server failed: " + var23.getMessage(), (Throwable)var23);
                  }
               }
            } else {
               this.getResponder().getCommunicator().sendSafeServerMessage("You ban and kick " + pname + ". No IP ban was issued.");
               this.log("bans player " + pname + " for " + days + " days. No IP ban was issued.");
            }

            if (!Servers.localServer.LOGINSERVER) {
               try {
                  LoginServerWebConnection c = new LoginServerWebConnection();
                  this.getResponder().getCommunicator().sendSafeServerMessage(c.ban(pname, banReason, days));
               } catch (Exception var22) {
                  this.getResponder().getCommunicator().sendAlertServerMessage("Failed to ban on login server:" + var22.getMessage());
                  logger.log(
                     Level.INFO, this.getResponder().getName() + " banning " + pname + " on login server failed: " + var22.getMessage(), (Throwable)var22
                  );
               }
            }
         } catch (IOException var26) {
            this.getResponder().getCommunicator().sendAlertServerMessage("Failed to ban on local server:" + var26.getMessage());
            logger.log(Level.INFO, this.getResponder().getName() + " banning " + pname + " on local server failed: " + var26.getMessage(), (Throwable)var26);
         }
      }

      key = "banip";
      val = this.getAnswer().getProperty(key);
      if (!bannedip && val != null && val.equals("true") && expiry > System.currentTimeMillis()) {
         this.doneSomething = true;
         boolean ok = true;
         String ipToBan = this.getAnswer().getProperty("iptoban");
         if (ipToBan == null || ipToBan.length() < 5) {
            ok = false;
         }

         if (ok) {
            try {
               if (ipToBan.charAt(0) != '/') {
                  ipToBan = '/' + ipToBan;
               }
            } catch (Exception var21) {
               ok = false;
            }
         }

         if (ok) {
            int dots = ipToBan.indexOf(42);
            if (dots > 0 && dots < 5) {
               this.getResponder().getCommunicator().sendAlertServerMessage("Failed to ban the ip. The ip address must be at least 5 characters long.");
               return;
            }

            Player[] players = Players.getInstance().getPlayers();

            for(int x = 0; x < players.length; ++x) {
               if (players[x].hasLink()) {
                  boolean ban = players[x].getCommunicator().getConnection().getIp().equals(ipToBan);
                  if (!ban && dots > 0) {
                     ban = players[x].getCommunicator().getConnection().getIp().startsWith(ipToBan.substring(0, dots));
                  }

                  if (ban) {
                     if (players[x].getPower() < this.getResponder().getPower()) {
                        Players.getInstance().logoutPlayer(players[x]);
                     } else {
                        ok = false;
                        this.getResponder().getCommunicator().sendNormalServerMessage("You cannot kick " + players[x].getName() + '!');
                        players[x]
                           .getCommunicator()
                           .sendAlertServerMessage(this.getResponder().getName() + " tried to kick you from the game and ban your ip.");
                     }
                  }
               }
            }

            if (Servers.localServer.LOGINSERVER) {
               Players.getInstance().addBannedIp(ipToBan, banReason, expiry);
            } else {
               try {
                  LoginServerWebConnection c = new LoginServerWebConnection();
                  this.getResponder().getCommunicator().sendSafeServerMessage(c.addBannedIp(ipToBan, banReason, days));
               } catch (Exception var20) {
                  this.getResponder().getCommunicator().sendAlertServerMessage("Failed to ban on login server:" + var20.getMessage());
                  logger.log(Level.INFO, this.getResponder().getName() + " banning ip on login server failed: " + var20.getMessage(), (Throwable)var20);
               }
            }

            this.getResponder()
               .getCommunicator()
               .sendSafeServerMessage("You ban " + ipToBan + " for " + days + " days. The server won't accept connections from " + ipToBan + " anymore.");
            this.log("bans ipaddress " + ipToBan + " for " + days + " days. Reason " + banReason);
         }
      }

      key = "warn";
      val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true")) {
         this.doneSomething = true;
         long lastWarned = this.playerInfo.getLastWarned();

         try {
            this.playerInfo.warn();
         } catch (IOException var19) {
            logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
         }

         String wst = this.playerInfo.getWarningStats(lastWarned);
         this.getResponder().getCommunicator().sendSafeServerMessage("You have officially warned " + pname + ". " + wst, (byte)1);
         if (this.targetPlayer != null) {
            this.targetPlayer
               .getCommunicator()
               .sendAlertServerMessage("You have just received an official warning. Too many of these will get you banned from the game.", (byte)1);
         }

         this.log("issues an official warning to " + pname + '.');
      }

      key = "resetWarn";
      val = this.getAnswer().getProperty(key);
      if (val != null && val.equals("true")) {
         this.doneSomething = true;

         try {
            this.playerInfo.resetWarnings();
            this.getResponder().getCommunicator().sendSafeServerMessage("You have officially removed the warnings for " + pname + '.');
            if (this.targetPlayer != null) {
               this.targetPlayer.getCommunicator().sendSafeServerMessage("Your warnings have just been officially removed.", (byte)1);
            }

            this.log("removes warnings for " + pname + '.');
         } catch (IOException var18) {
            logger.log(Level.INFO, this.getResponder().getName() + " fails to reset warnings for " + pname + '.', (Throwable)var18);
         }
      }

      key = "pardonip";
      val = this.getAnswer().getProperty(key);
      if (val != null && val.length() > 0) {
         try {
            int num = Integer.parseInt(val);
            if (num > 0) {
               this.doneSomething = true;
               String ip = this.iplist.get(num - 1);
               Ban bip = Players.getInstance().getBannedIp(ip);
               if (bip != null) {
                  if (Players.getInstance().removeBan(ip)) {
                     this.getResponder().getCommunicator().sendSafeServerMessage("You have gratiously pardoned the ipaddress " + ip, (byte)1);
                     this.log("pardons ipaddress " + ip + '.');

                     try {
                        LoginServerWebConnection c = new LoginServerWebConnection();
                        this.getResponder().getCommunicator().sendSafeServerMessage(c.removeBannedIp(ip));
                     } catch (Exception var16) {
                        this.getResponder().getCommunicator().sendAlertServerMessage("Failed to remove ip ban on login server:" + var16.getMessage());
                        logger.log(
                           Level.INFO,
                           this.getResponder().getName() + " removing ip ban " + bip + " on login server failed: " + var16.getMessage(),
                           (Throwable)var16
                        );
                     }
                  } else {
                     this.getResponder().getCommunicator().sendAlertServerMessage("Failed to unban ip " + ip + '.');
                  }
               }
            }
         } catch (NumberFormatException var17) {
            logger.log(
               Level.WARNING, "Problem parsing the pardonip value: " + val + ". Responder: " + this.getResponder() + " due to " + var17, (Throwable)var17
            );
         }
      }
   }

   private final void logMgmt(String logString) {
      if (this.getResponder().getLogger() != null) {
         this.getResponder().getLogger().log(Level.INFO, this.getResponder().getName() + " " + logString);
      }

      logger.log(Level.INFO, this.getResponder().getName() + " " + logString);
      Players.addMgmtMessage(this.getResponder().getName(), logString);
      Message mess = new Message(this.getResponder(), (byte)9, "MGMT", "<" + this.getResponder().getName() + "> " + logString);
      Server.getInstance().addMessage(mess);
   }

   private final void log(String logString) {
      if (this.getResponder().getLogger() != null) {
         this.getResponder().getLogger().log(Level.INFO, this.getResponder().getName() + " " + logString);
      }

      logger.log(Level.INFO, this.getResponder().getName() + " " + logString);
      Players.addGmMessage(this.getResponder().getName(), logString);
      Message mess = new Message(this.getResponder(), (byte)11, "GM", "<" + this.getResponder().getName() + "> " + logString);
      Server.getInstance().addMessage(mess);
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      if (this.getResponder().getPower() >= 2 || this.getResponder().mayMute()) {
         Player[] players = Players.getInstance().getPlayers();
         Arrays.sort((Object[])players);
         buf.append("text{type=\"bold\";text=\"---------------- Select Player ------------------\"}");
         String displayWarnings = "";
         if (this.getResponder().getPower() >= 2) {
            displayWarnings = " - warnings:";
         }

         buf.append("harray{label{text=\"Player name - (m)uted" + displayWarnings + "\"}");
         buf.append("dropdown{id='ddname';options=\"");
         buf.append("Use textbox");
         StringBuilder chattingBuild = new StringBuilder();

         for(int x = 0; x < players.length; ++x) {
            if (players[x].getWurmId() != this.getResponder().getWurmId()
               && (long)players[x].getPower() <= this.getResponder().getWurmId()
               && (this.getResponder().getPower() >= 2 || this.getResponder().getKingdomId() == players[x].getKingdomId() && players[x].isActiveInChat())) {
               buf.append(",");
               this.playlist.add(players[x]);
               buf.append(players[x].getName());
               if (players[x].isMute()) {
                  buf.append(" (m)");
               }

               if (this.getResponder().getPower() >= 2 && players[x].getWarnings() > 0) {
                  buf.append(" - ");
                  buf.append(players[x].getWarnings());
               }

               if (this.getResponder().getPower() >= 2 && players[x].isActiveInChat()) {
                  chattingBuild.append(players[x].getName() + " ");
               }
            }
         }

         buf.append("\"}label{text=\"  \"};input{id='pname';maxchars='32'}}");
         buf.append("text{text=\"\"};");
         buf.append("text{type=\"bold\";text=\"-------------- Chat control --------------------\"}");
         if (this.getResponder().getPower() > 0) {
            buf.append("text{text=\"Recently active in kchat:\"}");
            buf.append("text{text=\"" + chattingBuild.toString() + "\"}");
         }

         buf.append(
            "harray{checkbox{id=\"mute\";text=\"Mute \"};checkbox{id=\"unmute\";text=\"Unmute \"};checkbox{id=\"mutewarn\";text=\"Mutewarn \"};dropdown{id='mutereason';options=\""
         );
         buf.append("Profane language");
         buf.append(", ");
         buf.append("Racial or sexist remarks");
         buf.append(", ");
         buf.append("Staff bashing");
         buf.append(", ");
         buf.append("Harassment");
         buf.append(", ");
         buf.append("Spam");
         buf.append(", ");
         buf.append("Insubordination");
         buf.append(", ");
         buf.append("Repeated warnings");
         buf.append("\"}");
         buf.append("label{text=\"Hours:\"};dropdown{id='mutetime';default='0';options=\"");
         buf.append(1);
         buf.append(", ");
         buf.append(2);
         buf.append(", ");
         buf.append(5);
         buf.append(", ");
         buf.append(8);
         buf.append(", ");
         buf.append(24);
         buf.append(", ");
         buf.append(48);
         buf.append("\"}}");
         buf.append("harray{label{text=\"Or enter reason:\"};input{maxchars=\"40\";id=\"mutereasontb\"};}");
         buf.append("text{text=\"\"};");
         if (this.getResponder().getPower() >= 2) {
            buf.append("text{type=\"bold\";text=\"---------------- Summon ------------------\"}");
            String sel = ";selected=\"true\"";
            if (!this.getResponder().isOnSurface()) {
               sel = ";selected=\"false\"";
            }

            buf.append(
               "harray{checkbox{id='summon';selected='false';text=\"Teleport/Set to \"};label{text=\"TX:\"};input{id='tilex';maxchars='5';text=\""
                  + this.getResponder().getTileX()
                  + "\"};label{text=\"TY:\"};input{id='tiley';maxchars='5';text=\""
                  + this.getResponder().getTileY()
                  + "\"};checkbox{id='surfaced'"
                  + sel
                  + ";text=\"Surfaced \"}}"
            );
            buf.append("text{text=\"\"};");
            buf.append("text{type=\"bold\";text=\"--------------- IPBan control -------------------\"}");
            buf.append(
               "harray{checkbox{id=\"pardon\";text=\"Pardon ban \"};checkbox{id=\"ban\";text=\"IPBan \"};checkbox{id=\"banip\";text=\"IPBan IP \"};checkbox{id=\"warn\";text=\"Warn \"};checkbox{id=\"resetWarn\";text=\"Reset warnings \"}};"
            );
            buf.append("harray{label{text=\"Ip to ban:\"};input{id='iptoban';maxchars='16'}};");
            buf.append("harray{label{text=\"IPBan reason (max 250 chars):\"};input{id='banreason';text=\"Griefing\"}};");
            buf.append("harray{label{text=\"Days:\"};dropdown{id='bantime';default=\"1\";options=\"");
            buf.append(1);
            buf.append(", ");
            buf.append(3);
            buf.append(", ");
            buf.append(7);
            buf.append(", ");
            buf.append(30);
            buf.append(", ");
            buf.append(90);
            buf.append(", ");
            buf.append(365);
            buf.append(", ");
            buf.append(9999);
            buf.append("\"}}");
            buf.append("text{text=\"\"};");
            buf.append("text{type=\"bold\";text=\"--------------- Bans -------------------\"}");
            buf.append("harray{label{text=\"Pardon:\"};dropdown{id='pardonip';options=\"");
            buf.append("None");
            Ban[] bans = Players.getInstance().getBans();

            for(int x = 0; x < bans.length; ++x) {
               buf.append(", ");
               buf.append(bans[x].getIdentifier());
               this.iplist.add(bans[x].getIdentifier());
            }

            buf.append("\"}};");
            buf.append("text{text=\"\"};");
            buf.append("text{type=\"bold\";text=\"---------------- Locate Corpse ------------------\"}");
            buf.append("text{type=\"italic\";text=\"Uses name at top of form now.\"}");
            buf.append("harray{checkbox{id=\"locate\";text=\"Locate Corpse? \"}}");
            buf.append("text{text=\"\"};");
            buf.append("text{type=\"bold\";text=\"---------------- GM Tool (In-Game GM Interface) ------------------\"}");
            buf.append("text{type=\"italic\";text=\"This should only be used on the server pertaining to the item or player.\"}");
            buf.append("checkbox{id=\"gmtool\";text=\"Start GM Tool? \"}");
            buf.append("label{text=\"Either select player name at top\"}");
            buf.append("harray{label{text=\"or specify a WurmId: \"};input{id=\"wurmid\";maxchars=\"20\";text=\"\"}}");
            buf.append("harray{label{text=\"or specify an Email Address to search for: \"};input{id=\"searchemail\";maxchars=\"60\";text=\"\"}}");
            buf.append("harray{label{text=\"or specify an IP Address to search for: \"};input{id=\"searchip\";maxchars=\"30\";text=\"\"}}");
            buf.append("text{text=\"\"};");
         }

         buf.append("text{type=\"bold\";text=\"--------------- Help -------------------\"}");
         buf.append("text{text=\"Either type a name in the textbox or select a name from the list.\"}");
         buf.append("text{text=\"You may check as many boxes you wish and all options will apply to the player you select.\"}");
         if (this.getResponder().getPower() >= 2) {
            buf.append("text{text=\"If you cross the ban ip checkbox when a player is selected, his ip will be banned for 7 days max.\"}");
            buf.append("text{text=\"If you want to extend this ban you will have to type it in the ip address box.\"}");
         }

         buf.append(this.createAnswerButton2());
         int len = this.getResponder().getPower() >= 2 ? 500 : 300;
         this.getResponder().getCommunicator().sendBml(500, len, true, true, buf.toString(), 200, 200, 200, this.title);
      }
   }
}
