package com.wurmonline.server;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Ban;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.webinterface.WcCreateEpicMission;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.webinterface.WebInterfaceImpl;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoginServerWebConnection {
   private WebInterface wurm = null;
   private static Logger logger = Logger.getLogger(LoginServerWebConnection.class.getName());
   private int serverId = Servers.loginServer.id;
   private static final char EXCLAMATION_MARK = '!';
   private static final String FAILED_TO_CREATE_TRINKET = ", failed to create trinket! ";
   private static final String YOU_RECEIVED = "You received ";
   private static final String AN_ERROR_OCCURRED_WHEN_CONTACTING_THE_LOGIN_SERVER = "An error occurred when contacting the login server. Please try later.";
   private static final String FAILED_TO_CONTACT_THE_LOGIN_SERVER = "Failed to contact the login server ";
   private static final String FAILED_TO_CONTACT_THE_LOGIN_SERVER_PLEASE_TRY_LATER = "Failed to contact the login server. Please try later.";
   private static final String FAILED_TO_CONTACT_THE_BANK_PLEASE_TRY_LATER = "Failed to contact the bank. Please try later.";
   private static final String GAME_SERVER_IS_CURRENTLY_UNAVAILABLE = "The game server is currently unavailable.";
   private static final char COLON_CHAR = ':';
   private String intraServerPassword = Servers.localServer.INTRASERVERPASSWORD;
   static final int[] failedIntZero = new int[]{-1, -1};

   public LoginServerWebConnection() {
   }

   public LoginServerWebConnection(int aServerId) {
      this.serverId = aServerId;
   }

   private void connect() throws MalformedURLException, RemoteException, NotBoundException {
      if (this.wurm == null) {
         if (Servers.localServer.id == this.serverId) {
            this.wurm = new WebInterfaceImpl();
         } else {
            long lStart = System.nanoTime();
            String name = null;

            try {
               ServerEntry server = Servers.getServerWithId(this.serverId);
               if (server == null) {
                  throw new RemoteException("Server " + this.serverId + " not found");
               }

               if (!server.isAvailable(5, true)) {
                  throw new RemoteException("Server unavailable");
               }

               this.intraServerPassword = server.INTRASERVERPASSWORD;
               name = "//" + server.INTRASERVERADDRESS + ':' + server.RMI_PORT + "/" + "wuinterface";
               this.wurm = (WebInterface)Naming.lookup(name);
            } finally {
               if (logger.isLoggable(Level.FINE)) {
                  logger.fine("Looking up WebInterface RMI: " + name + " took " + (float)(System.nanoTime() - lStart) / 1000000.0F + "ms.");
               }
            }
         }
      }
   }

   public int getServerId() {
      return this.serverId;
   }

   public byte[] createAndReturnPlayer(
      String playerName,
      String hashedIngamePassword,
      String challengePhrase,
      String challengeAnswer,
      String emailAddress,
      byte kingdom,
      byte power,
      long appearance,
      byte gender,
      boolean titleKeeper,
      boolean addPremium,
      boolean passwordIsHashed
   ) throws Exception {
      if (this.wurm == null) {
         this.connect();
      }

      if (this.wurm != null) {
         return this.wurm
            .createAndReturnPlayer(
               this.intraServerPassword,
               playerName,
               hashedIngamePassword,
               challengePhrase,
               challengeAnswer,
               emailAddress,
               kingdom,
               power,
               appearance,
               gender,
               titleKeeper,
               addPremium,
               passwordIsHashed
            );
      } else {
         throw new RemoteException("Failed to create web connection.");
      }
   }

   public long chargeMoney(String playerName, long moneyToCharge) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, playerName + " + Failed to contact the login server " + var6.getMessage());
            return -10L;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.chargeMoney(this.intraServerPassword, playerName, moneyToCharge);
         } catch (RemoteException var5) {
            return -10L;
         }
      } else {
         return -10L;
      }
   }

   public boolean addPlayingTime(Creature player, String name, int months, int days, String detail) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var9) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var9.getMessage());
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            Map<String, String> result = this.wurm
               .addPlayingTime(this.intraServerPassword, name, months, days, detail, Servers.localServer.testServer || player.getPower() > 0);

            for(Entry<String, String> e : result.entrySet()) {
               if (e.getKey().equals("error")) {
                  player.getCommunicator().sendAlertServerMessage(e.getValue());
                  return false;
               }

               if (e.getKey().equals("ok")) {
                  return true;
               }
            }
         } catch (RemoteException var10) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            return false;
         }
      }

      return false;
   }

   public boolean addMoney(Creature player, String name, long money, String detail) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var9) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var9.getMessage());
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            Map<String, String> result = this.wurm.addMoneyToBank(this.intraServerPassword, name, money, detail, false);

            for(Entry<String, String> e : result.entrySet()) {
               if (e.getKey().equals("error")) {
                  player.getCommunicator().sendAlertServerMessage(e.getValue());
                  return false;
               }

               if (e.getKey().equals("ok")) {
                  return true;
               }
            }
         } catch (RemoteException var10) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            return false;
         }
      }

      return false;
   }

   public long getMoney(Creature player) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            logger.log(Level.WARNING, player.getName() + " " + "Failed to contact the login server " + " " + this.serverId + " " + var4.getMessage());
            return 0L;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getMoney(this.intraServerPassword, player.getWurmId(), player.getName());
         } catch (RemoteException var3) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the bank. Please try later.");
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public boolean addMoney(long wurmid, String name, long money, String detail) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var10) {
            logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + var10.getMessage());
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            Map<String, String> result = this.wurm.addMoneyToBank(this.intraServerPassword, name, wurmid, money, detail, false);

            for(Entry<String, String> e : result.entrySet()) {
               if (e.getKey().equals("error")) {
                  logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + (String)e.getValue());
                  return false;
               }

               if (e.getKey().equals("ok")) {
                  return true;
               }
            }
         } catch (RemoteException var11) {
            logger.log(Level.WARNING, wurmid + ": failed to receive " + money + ", " + detail + ", " + var11, (Throwable)var11);
            return false;
         }
      }

      return false;
   }

   public void testAdding(String playerName) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, playerName + ": " + var6.getMessage(), (Throwable)var6);
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
            return;
         }
      }

      try {
         Map<String, String> result = this.wurm.addPlayingTime(this.intraServerPassword, playerName, 1, 4, "test" + System.currentTimeMillis());

         for(Entry<String, String> e : result.entrySet()) {
            logger.log(Level.INFO, (String)e.getKey() + ':' + (String)e.getValue());
         }

         Map<String, String> result2 = this.wurm.addMoneyToBank(this.intraServerPassword, playerName, 10000L, "test" + System.currentTimeMillis());

         for(Entry<String, String> e : result2.entrySet()) {
            logger.log(Level.INFO, (String)e.getKey() + ':' + (String)e.getValue());
         }
      } catch (RemoteException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      }
   }

   public void setWeather(float windRotation, float windpower, float windDir) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            return;
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm.setWeather(this.intraServerPassword, windRotation, windpower, windDir);
         } catch (RemoteException var5) {
            return;
         }
      }
   }

   public Map<String, Byte> getReferrers(Creature player, long wurmid) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var5) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the login server. Please try later.");
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var5.getMessage());
            return null;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getReferrers(this.intraServerPassword, wurmid);
         } catch (RemoteException var6) {
            player.getCommunicator().sendAlertServerMessage("An error occurred when contacting the login server. Please try later.");
         }
      }

      return null;
   }

   public void addReferrer(Player player, String receiver) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var8) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the login server. Please try later.");
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var8.getMessage());
            return;
         }
      }

      if (this.wurm != null) {
         try {
            String mess = this.wurm.addReferrer(this.intraServerPassword, receiver, player.getWurmId());

            try {
               long referrer = Long.parseLong(mess);
               player.getSaveFile().setReferedby(referrer);
               player.getCommunicator().sendNormalServerMessage("Okay, you have set " + receiver + " as your referrer.");
            } catch (NumberFormatException var6) {
               player.getCommunicator().sendNormalServerMessage(mess);
            }
         } catch (RemoteException var7) {
            player.getCommunicator().sendAlertServerMessage("An error occurred when contacting the login server. Please try later.");
         }
      }
   }

   public void acceptReferrer(Creature player, String referrerName, boolean money) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            player.getCommunicator().sendAlertServerMessage("Failed to contact the login server. Please try later.");
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
            return;
         }
      }

      if (this.wurm != null) {
         try {
            player.getCommunicator().sendNormalServerMessage(this.wurm.acceptReferrer(this.intraServerPassword, player.getWurmId(), referrerName, money));
         } catch (RemoteException var5) {
            player.getCommunicator().sendAlertServerMessage("An error occurred when contacting the login server. Please try later.");
         }
      }
   }

   public String getReimburseInfo(Player player) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var4.getMessage());
            return "Failed to contact the login server. Please try later.";
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getReimbursementInfo(this.intraServerPassword, player.getSaveFile().emailAddress);
         } catch (RemoteException var3) {
            return "An error occurred when contacting the login server. Please try later.";
         }
      } else {
         return "Failed to contact the login server. Please try later.";
      }
   }

   public long[] getCurrentServer(String name, long wurmid) throws Exception {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
            throw new WurmServerException("Failed to contact the login server. Please try later.");
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getCurrentServerAndWurmid(this.intraServerPassword, name, wurmid);
         } catch (RemoteException var5) {
            throw new WurmServerException("An error occurred when contacting the login server. Please try later.", var5);
         }
      } else {
         throw new WurmServerException("Failed to contact the login server. Please try later.");
      }
   }

   public Map<Long, byte[]> getPlayerStates(long[] wurmids) throws WurmServerException {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var4.getMessage());
            throw new WurmServerException("Failed to contact the login server. Please try later.");
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getPlayerStates(this.intraServerPassword, wurmids);
         } catch (RemoteException var3) {
            throw new WurmServerException("An error occurred when contacting the login server. Please try later.", var3);
         }
      } else {
         throw new WurmServerException("Failed to contact the login server. Please try later.");
      }
   }

   public void manageFeature(int aServerId, int featureId, boolean aOverridden, boolean aEnabled, boolean global) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var8) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var8.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm.manageFeature(this.intraServerPassword, aServerId, featureId, aOverridden, aEnabled, global);
         } catch (RemoteException var7) {
            logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + var7.getMessage());
         }
      }
   }

   public void startShutdown(String instigator, int seconds, String reason) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm.startShutdown(this.intraServerPassword, instigator, seconds, reason);
         } catch (RemoteException var5) {
            logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + var5.getMessage());
         }
      }
   }

   public String withDraw(Player player, String name, String _email, int _months, int _silvers, boolean titlebok, boolean mbok, int _daysLeft) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var26) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var26.getMessage());
            return "Failed to contact the login server. Please try later.";
         }
      }

      if (this.wurm == null) {
         return "Failed to contact the login server. Please try later.";
      } else {
         try {
            if (this.wurm.withDraw(this.intraServerPassword, player.getName(), name, _email, _months, _silvers, titlebok, _daysLeft)) {
               if (titlebok) {
                  try {
                     Item bok = ItemFactory.createItem(443, 99.0F, player.getName());
                     if (mbok) {
                        bok.setName("Master bag of keeping");
                        bok.setSizes(3, 10, 20);
                     }

                     player.getInventory().insertItem(bok, true);
                     player.getCommunicator().sendSafeServerMessage("You received " + bok.getNameWithGenus() + '!');
                  } catch (FailedException var24) {
                     logger.log(Level.WARNING, player.getName() + ", failed to create bok! " + var24.getMessage(), (Throwable)var24);
                  } catch (NoSuchTemplateException var25) {
                     logger.log(Level.WARNING, player.getName() + ", failed to create bok! " + var25.getMessage(), (Throwable)var25);
                  }

                  player.addTitle(Titles.Title.Ageless);
                  if (mbok) {
                     player.addTitle(Titles.Title.KeeperTruth);
                  }
               }

               if (_months > 0) {
                  try {
                     Item spyglass = ItemFactory.createItem(489, 80.0F + (float)Server.rand.nextInt(20), player.getName());
                     player.getInventory().insertItem(spyglass, true);
                     player.getCommunicator().sendSafeServerMessage("You received " + spyglass.getNameWithGenus() + '!');
                  } catch (FailedException var22) {
                     logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var22.getMessage(), (Throwable)var22);
                  } catch (NoSuchTemplateException var23) {
                     logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var23.getMessage(), (Throwable)var23);
                  }

                  Item trinket = null;
                  if (_months > 1) {
                     try {
                        trinket = ItemFactory.createItem(509, 80.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                     } catch (FailedException var20) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var20.getMessage(), (Throwable)var20);
                     } catch (NoSuchTemplateException var21) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var21.getMessage(), (Throwable)var21);
                     }

                     try {
                        trinket = ItemFactory.createItem(93, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(79, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(20, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(313, 40.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(8, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(90, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                     } catch (FailedException var18) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var18.getMessage(), (Throwable)var18);
                     } catch (NoSuchTemplateException var19) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var19.getMessage(), (Throwable)var19);
                     }
                  }

                  if (_months > 2) {
                     try {
                        trinket = ItemFactory.createItem(105, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(105, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(107, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(103, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(103, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(108, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(104, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(106, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(106, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(4, 30.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                     } catch (FailedException var16) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var16.getMessage(), (Throwable)var16);
                     } catch (NoSuchTemplateException var17) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var17.getMessage(), (Throwable)var17);
                     }
                  }

                  if (_months > 3) {
                     try {
                        trinket = ItemFactory.createItem(135, 50.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        trinket = ItemFactory.createItem(480, 70.0F, player.getName());
                        player.getInventory().insertItem(trinket, true);
                        player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                     } catch (FailedException var14) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var14.getMessage(), (Throwable)var14);
                     } catch (NoSuchTemplateException var15) {
                        logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var15.getMessage(), (Throwable)var15);
                     }
                  }

                  if (_months > 4) {
                     for(int x = 0; x < 3; ++x) {
                        try {
                           trinket = ItemFactory.createItem(509, 80.0F, player.getName());
                           player.getInventory().insertItem(trinket, true);
                           player.getCommunicator().sendSafeServerMessage("You received " + trinket.getNameWithGenus() + '!');
                        } catch (FailedException var12) {
                           logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var12.getMessage(), (Throwable)var12);
                        } catch (NoSuchTemplateException var13) {
                           logger.log(Level.WARNING, player.getName() + ", failed to create trinket! " + var13.getMessage(), (Throwable)var13);
                        }
                     }
                  }
               }

               return "You have been reimbursed.";
            } else {
               return "There was an error with your request. The server may be unavailable. You may also want to verify the amounts entered.";
            }
         } catch (RemoteException var27) {
            return "An error occurred when contacting the login server. Please try later.";
         }
      }
   }

   public boolean transferPlayer(Player player, String playerName, int posx, int posy, boolean surfaced, byte[] data) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var9) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + "," + var9.getMessage());
            if (player != null) {
               player.getCommunicator().sendAlertServerMessage("Failed to contact the login server. Please try later.");
            }

            return false;
         }
      }

      if (this.wurm != null) {
         try {
            if (!this.wurm.transferPlayer(this.intraServerPassword, playerName, posx, posy, surfaced, player.getPower(), data)) {
               if (player != null) {
                  player.getCommunicator()
                     .sendAlertServerMessage(
                        "An error was reported from the login server. Please try later or report this using /support if the problem persists."
                     );
               }

               return false;
            } else {
               return true;
            }
         } catch (RemoteException var8) {
            logger.log(Level.WARNING, "Failed to transfer " + playerName + " to the login server " + var8.getMessage());
            if (player != null) {
               player.getCommunicator().sendAlertServerMessage("An error occurred when contacting the login server. Please try later.");
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean changePassword(long wurmId, String newPassword) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage() + " server=" + this.serverId);
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.changePassword(this.intraServerPassword, wurmId, newPassword);
         } catch (RemoteException var5) {
            logger.log(Level.WARNING, "Failed to change password for  " + wurmId + "." + var5.getMessage());
            return false;
         }
      } else {
         return false;
      }
   }

   public int[] getPremTimeSilvers(long wurmId) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            return failedIntZero;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getPremTimeSilvers(this.intraServerPassword, wurmId);
         } catch (RemoteException var5) {
         }
      }

      return failedIntZero;
   }

   public boolean setCurrentServer(String name, int currentServer) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var5) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var5.getMessage());
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            if (this.wurm.setCurrentServer(this.intraServerPassword, name, currentServer)) {
               return true;
            }
         } catch (RemoteException var4) {
            logger.log(Level.WARNING, "failed to set current server of " + name + " to " + currentServer + ", " + var4.getMessage());
            return false;
         }
      }

      return false;
   }

   public String renamePlayer(String oldName, String newName, String newPass, int power) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var7) {
            logger.log(Level.WARNING, "Failed to contact the login server " + var7.getMessage() + "" + this.serverId);
            return "Failed to contact server. Try later. This is an Error.";
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.rename(this.intraServerPassword, oldName, newName, newPass, power);
         } catch (RemoteException var6) {
            logger.log(Level.WARNING, "Failed to change name of " + oldName + ", " + var6.getMessage());
            return "Failed to contact server. Try later. This is an Error.";
         }
      } else {
         return "";
      }
   }

   public String changePassword(String changerName, String name, String newPass, int power) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var7) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var7.getMessage());
            return var7.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.changePassword(this.intraServerPassword, changerName, name, newPass, power);
         } catch (RemoteException var6) {
            logger.log(Level.WARNING, changerName + " failed to change password of " + name + ", " + var6.getMessage());
            return var6.getMessage();
         }
      } else {
         return "";
      }
   }

   public String ascend(
      int newDeityId,
      String deityName,
      long wurmid,
      byte existingDeity,
      byte gender,
      byte newPower,
      float initialBStr,
      float initialBSta,
      float initialBCon,
      float initialML,
      float initialMS,
      float initialSS,
      float initialSD
   ) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var17) {
            logger.log(Level.WARNING, "Failed to contact the login server " + var17.getMessage() + " " + this.serverId);
            return var17.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm
               .ascend(
                  this.intraServerPassword,
                  newDeityId,
                  deityName,
                  wurmid,
                  existingDeity,
                  gender,
                  newPower,
                  initialBStr,
                  initialBSta,
                  initialBCon,
                  initialML,
                  initialMS,
                  initialSS,
                  initialSD
               );
         } catch (RemoteException var16) {
            logger.log(Level.WARNING, wurmid + " failed to create deity " + deityName + ", " + var16.getMessage());
            return var16.getMessage();
         }
      } else {
         return "";
      }
   }

   public String changeEmail(String changerName, String name, String newEmail, String password, int power, String pwQuestion, String pwAnswer) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var10) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var10.getMessage());
            return var10.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.changeEmail(this.intraServerPassword, changerName, name, newEmail, password, power, pwQuestion, pwAnswer);
         } catch (RemoteException var9) {
            logger.log(Level.WARNING, changerName + " failed to change email of " + name + ", " + var9.getMessage());
            return var9.getMessage();
         }
      } else {
         return "";
      }
   }

   public String addReimb(String changerName, String name, int numMonths, int _silver, int _daysLeft, boolean setbok) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var9) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var9.getMessage());
            return var9.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.addReimb(this.intraServerPassword, changerName, name, numMonths, _silver, _daysLeft, setbok);
         } catch (RemoteException var8) {
            logger.log(Level.WARNING, changerName + " failed to add reimb of " + name + ", " + var8.getMessage());
            return var8.getMessage();
         }
      } else {
         return "";
      }
   }

   public String sendMail(byte[] maildata, byte[] items, long sender, long wurmid, int targetServer) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var10) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var10.getMessage());
            return var10.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.sendMail(this.intraServerPassword, maildata, items, sender, wurmid, targetServer);
         } catch (RemoteException var9) {
            logger.log(Level.WARNING, "Failed to send mail " + var9.getMessage());
            return var9.getMessage();
         }
      } else {
         return "";
      }
   }

   public String ban(String name, String reason, int days) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
            return var6.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.ban(this.intraServerPassword, name, reason, days);
         } catch (RemoteException var5) {
            logger.log(Level.WARNING, "Failed to ban " + name + ':' + var5.getMessage());
            return "Failed to ban " + name + ':' + var5.getMessage();
         }
      } else {
         return "Failed to contact the login server. Please try later.";
      }
   }

   public String addBannedIp(String ip, String reason, int days) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
            return var6.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.addBannedIp(this.intraServerPassword, ip, reason, days);
         } catch (RemoteException var5) {
            logger.log(Level.WARNING, "Failed to ban " + ip + ':' + var5.getMessage());
            return "Failed to ban " + ip + ':' + var5.getMessage();
         }
      } else {
         return "Failed to contact the login server. Please try later.";
      }
   }

   public Ban[] getPlayersBanned() throws Exception {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var3) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var3.getMessage());
            throw new WurmServerException("Failed to contact the login server:" + var3.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getPlayersBanned(this.intraServerPassword);
         } catch (RemoteException var2) {
            logger.log(Level.WARNING, "Failed to retrieve banned players :" + var2.getMessage());
            throw new WurmServerException("Failed to retrieve banned players :" + var2.getMessage());
         }
      } else {
         return null;
      }
   }

   public Ban[] getIpsBanned() throws Exception {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var3) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var3.getMessage());
            throw new WurmServerException("Failed to contact the login server:" + var3.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.getIpsBanned(this.intraServerPassword);
         } catch (RemoteException var2) {
            logger.log(Level.WARNING, "Failed to retrieve banned ips :" + var2.getMessage());
            throw new WurmServerException("Failed to retrieve banned ips :" + var2.getMessage());
         }
      } else {
         return null;
      }
   }

   public String pardonban(String name) throws RemoteException {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var4.getMessage());
            return var4.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.pardonban(this.intraServerPassword, name);
         } catch (RemoteException var3) {
            logger.log(Level.WARNING, "Failed to pardon " + name + ':' + var3.getMessage());
            return "Failed to pardon " + name + ':' + var3.getMessage();
         }
      } else {
         return "Failed to contact the login server. Please try later.";
      }
   }

   public String removeBannedIp(String ip) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var4.getMessage());
            return var4.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.removeBannedIp(this.intraServerPassword, ip);
         } catch (RemoteException var3) {
            logger.log(Level.WARNING, "Failed to ban " + ip + ':' + var3.getMessage());
            return "Failed to ban " + ip + ':' + var3.getMessage();
         }
      } else {
         return "Failed to contact the login server. Please try later.";
      }
   }

   public Map<String, String> doesPlayerExist(String playerName) {
      Map<String, String> toReturn = new HashMap<>();
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var4) {
            toReturn.put("ResponseCode", "NOTOK");
            toReturn.put("ErrorMessage", "The game server is currently unavailable.");
            toReturn.put("display_text", "The game server is currently unavailable.");
            return toReturn;
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.doesPlayerExist(this.intraServerPassword, playerName);
         } catch (RemoteException var5) {
            logger.log(Level.WARNING, "Failed to contact server.");
            toReturn.put("ResponseCode", "NOTOK");
            toReturn.put("ErrorMessage", "The game server is currently unavailable.");
            toReturn.put("display_text", "The game server is currently unavailable.");
         }
      } else {
         toReturn.put("ResponseCode", "NOTOK");
         toReturn.put("ErrorMessage", "The game server is currently unavailable.");
         toReturn.put("display_text", "The game server is currently unavailable.");
      }

      return toReturn;
   }

   public String sendVehicle(
      byte[] passengerdata, byte[] itemdata, long pilot, long vehicleId, int targetServer, int tilex, int tiley, int layer, float rotation
   ) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var15) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var15.getMessage());
            return var15.getMessage();
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.sendVehicle(this.intraServerPassword, passengerdata, itemdata, pilot, vehicleId, targetServer, tilex, tiley, layer, rotation);
         } catch (RemoteException var13) {
            logger.log(Level.WARNING, "Failed to send vehicle " + var13.getMessage());
            return var13.getMessage();
         } catch (Exception var14) {
            return var14.getMessage();
         }
      } else {
         return "";
      }
   }

   public void sendWebCommand(final short type, final WebCommand command) {
      (new Thread() {
            @Override
            public void run() {
               boolean ok = false;
               if (LoginServerWebConnection.this.wurm == null) {
                  try {
                     LoginServerWebConnection.this.connect();
                  } catch (Exception var5) {
                  }
               }
   
               if (LoginServerWebConnection.this.wurm != null) {
                  try {
                     LoginServerWebConnection.this.wurm
                        .genericWebCommand(LoginServerWebConnection.this.intraServerPassword, type, command.getWurmId(), command.getData());
                     ok = true;
                  } catch (RemoteException var4) {
                     LoginServerWebConnection.logger.log(Level.WARNING, "Failed to send command " + var4.getMessage());
                  }
               }
   
               if (!ok && command.getType() == 11 && Servers.localServer.LOGINSERVER) {
                  try {
                     EpicEntity entity = Server.getEpicMap().getEntity(((WcCreateEpicMission)command).entityNumber);
                     if (entity != null) {
                        entity.addFailedServer(LoginServerWebConnection.this.serverId);
                     }
                  } catch (Exception var3) {
                     LoginServerWebConnection.logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
                  }
               }
            }
         })
         .start();
   }

   public void setKingdomInfo(
      byte kingdomId,
      byte templateKingdom,
      String _name,
      String _password,
      String _chatName,
      String _suffix,
      String mottoOne,
      String mottoTwo,
      boolean acceptsPortals
   ) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var12) {
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm
               .setKingdomInfo(
                  this.intraServerPassword,
                  Servers.localServer.id,
                  kingdomId,
                  templateKingdom,
                  _name,
                  _password,
                  _chatName,
                  _suffix,
                  mottoOne,
                  mottoTwo,
                  acceptsPortals
               );
         } catch (RemoteException var11) {
            logger.log(Level.WARNING, "Failed to send command " + var11.getMessage());
         }
      }
   }

   public boolean kingdomExists(int thisServerId, byte kingdomId, boolean exists) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var5) {
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.kingdomExists(this.intraServerPassword, thisServerId, kingdomId, exists);
         } catch (RemoteException var6) {
            logger.log(Level.WARNING, "Failed to send command " + var6.getMessage());
         }
      }

      return true;
   }

   public void requestDemigod(byte existingDeity, String deityName) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var5) {
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm.requestDemigod(this.intraServerPassword, existingDeity, deityName);
         } catch (RemoteException var4) {
            logger.log(Level.WARNING, "Failed to send command " + var4.getMessage());
         }
      }
   }

   public boolean requestDeityMove(int deityNum, int desiredHex, String guide) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var5) {
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.requestDeityMove(this.intraServerPassword, deityNum, desiredHex, guide);
         } catch (RemoteException var6) {
            logger.log(Level.WARNING, "Failed to send command " + var6.getMessage());
         }
      }

      return false;
   }

   public boolean awardPlayer(long wurmid, String name, int days, int months) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var8) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var8.getMessage());
            return false;
         }
      }

      if (this.wurm != null) {
         try {
            this.wurm.awardPlayer(this.intraServerPassword, wurmid, name, days, months);
         } catch (RemoteException var7) {
            logger.log(Level.WARNING, "failed to set award " + wurmid + " (" + name + ") " + months + " months, " + days + " days, " + var7.getMessage());
            return false;
         }
      }

      return false;
   }

   public boolean isFeatureEnabled(int featureId) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var3) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var3.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.isFeatureEnabled(this.intraServerPassword, featureId);
         } catch (RemoteException var4) {
            logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + var4.getMessage());
         }
      }

      return false;
   }

   public boolean setPlayerFlag(long wurmid, int flag, boolean set) {
      if (this.wurm == null) {
         try {
            this.connect();
         } catch (Exception var6) {
            logger.log(Level.WARNING, "Failed to contact the login server  " + this.serverId + " " + var6.getMessage());
         }
      }

      if (this.wurm != null) {
         try {
            return this.wurm.setPlayerFlag(Servers.localServer.INTRASERVERPASSWORD, wurmid, flag, set);
         } catch (RemoteException var7) {
            logger.log(Level.WARNING, "An error occurred when contacting the login server. Please try later. " + this.serverId + " " + var7.getMessage());
         }
      }

      return false;
   }
}
