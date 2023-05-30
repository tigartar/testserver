package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.WurmMail;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TraderManagementQuestion extends Question implements TimeConstants {
   private static final Logger logger = Logger.getLogger(TraderManagementQuestion.class.getName());
   public static final int maxTraders = 1;
   private final boolean isDismissing;

   public TraderManagementQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 22, aTarget);
      this.isDismissing = false;
   }

   public TraderManagementQuestion(Creature aResponder, String aTitle, String aQuestion, Creature aTarget) {
      super(aResponder, aTitle, aQuestion, 22, aTarget.getWurmId());
      this.isDismissing = true;
   }

   public static void dismissMerchant(Creature dismisser, long target) {
      try {
         Creature trader = Creatures.getInstance().getCreature(target);
         if (trader != null) {
            if (!trader.isTrading()) {
               Server.getInstance().broadCastAction(trader.getName() + " grunts, packs " + trader.getHisHerItsString() + " things and is off.", trader, 5);
               if (dismisser != null) {
                  dismisser.getCommunicator().sendNormalServerMessage("You dismiss " + trader.getName() + " from " + trader.getHisHerItsString() + " post.");
                  logger.log(Level.INFO, dismisser.getName() + " dismisses trader " + trader.getName() + " with WurmID: " + target);
               } else {
                  logger.log(Level.INFO, "Merchant " + trader.getName() + " with WurmID: " + target + " dismissed by timeout");
               }

               Item[] items = Items.getAllItems();

               for(int x = 0; x < items.length; ++x) {
                  if (items[x].getTemplateId() == 300 && items[x].getData() == target) {
                     items[x].setData(-1, -1);
                     break;
                  }
               }

               Shop shop = Economy.getEconomy().getShop(trader);
               if (shop != null) {
                  try {
                     Item backPack = ItemFactory.createItem(1, 10.0F + Server.rand.nextFloat() * 10.0F, trader.getName());
                     backPack.setDescription("Due to poor business I have moved on. Thank you for your time. " + trader.getName());
                     ArrayList<Item> largeItems = new ArrayList<>();

                     for(Item realItem : trader.getInventory().getAllItems(false)) {
                        if (realItem.getTemplate() != null && !realItem.getTemplate().isComponentItem() && !backPack.insertItem(realItem, false)) {
                           largeItems.add(realItem);
                        }
                     }

                     WurmMail mail = new WurmMail(
                        (byte)0,
                        backPack.getWurmId(),
                        shop.getOwnerId(),
                        shop.getOwnerId(),
                        0L,
                        System.currentTimeMillis() + 60000L,
                        System.currentTimeMillis() + (Servers.isThisATestServer() ? 3600000L : 14515200000L),
                        Servers.localServer.id,
                        false,
                        false
                     );
                     WurmMail.addWurmMail(mail);
                     mail.createInDatabase();
                     backPack.putInVoid();
                     backPack.setMailed(true);
                     backPack.setMailTimes((byte)(backPack.getMailTimes() + 1));

                     for(Item i : largeItems) {
                        WurmMail largeMail = new WurmMail(
                           (byte)0,
                           i.getWurmId(),
                           shop.getOwnerId(),
                           shop.getOwnerId(),
                           0L,
                           System.currentTimeMillis() + 60000L,
                           System.currentTimeMillis() + (Servers.isThisATestServer() ? 3600000L : 14515200000L),
                           Servers.localServer.id,
                           false,
                           false
                        );
                        WurmMail.addWurmMail(largeMail);
                        largeMail.createInDatabase();
                        i.putInVoid();
                        i.setMailed(true);
                        i.setMailTimes((byte)(i.getMailTimes() + 1));
                     }
                  } catch (Exception var12) {
                     logger.log(
                        Level.WARNING, var12.getMessage() + " " + trader.getName() + " at " + trader.getTileX() + ", " + trader.getTileY(), (Throwable)var12
                     );
                  }
               } else {
                  logger.log(Level.WARNING, "No shop when dismissing trader " + trader.getName() + " " + trader.getWurmId());
               }

               trader.destroy();
            } else if (dismisser != null) {
               dismisser.getCommunicator().sendNormalServerMessage(trader.getName() + " is trading. Try later.");
            }
         } else if (dismisser != null) {
            dismisser.getCommunicator().sendNormalServerMessage("An error occured on the server while dismissing the trader.");
         }
      } catch (NoSuchCreatureException var13) {
         if (dismisser != null) {
            dismisser.getCommunicator().sendNormalServerMessage("The merchant can not be dismissed now.");
         }
      }
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      if (this.isDismissing) {
         String key = "dism";
         String val = answers.getProperty("dism");
         if (Boolean.parseBoolean(val)) {
            dismissMerchant(this.getResponder(), this.target);
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to dismiss the trader.");
         }
      } else {
         QuestionParser.parseTraderManagementQuestion(this);
      }
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      if (this.isDismissing) {
         buf.append(this.mayorDismissingQuestion());
      } else {
         buf.append(this.contractQuestion());
      }

      buf.append(this.createAnswerButton3());
      if (this.isDismissing) {
         this.getResponder().getCommunicator().sendBml(250, 200, true, true, buf.toString(), 200, 200, 200, this.title);
      } else {
         this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      }
   }

   private String contractQuestion() {
      StringBuilder buf = new StringBuilder();
      Item contract = null;
      Creature trader = null;
      Shop shop = null;
      long traderId = -1L;

      try {
         contract = Items.getItem(this.target);
         traderId = contract.getData();
         if (traderId != -1L) {
            trader = Server.getInstance().getCreature(traderId);
            if (trader.isNpcTrader()) {
               shop = Economy.getEconomy().getShop(trader);
            }
         }
      } catch (NoSuchItemException var17) {
         logger.log(Level.WARNING, this.getResponder().getName() + " contract is missing!");
      } catch (NoSuchPlayerException var18) {
         logger.log(Level.WARNING, "Trader for " + this.getResponder().getName() + " is a player? Well it can't be found.");
         contract.setData(-10L);
      } catch (NoSuchCreatureException var19) {
         logger.log(Level.WARNING, "Trader for " + this.getResponder().getName() + " can't be found.");
         contract.setData(-10L);
      }

      if (shop != null) {
         buf.append(this.getBmlHeaderWithScroll());
      } else {
         buf.append(this.getBmlHeader());
      }

      buf.append("text{type=\"bold\";text=\"Trader information:\"}");
      buf.append(
         "text{type=\"italic\";text=\"A personal merchant tries to sell anything you give him to other players. Then you can come back and collect the money.\"}"
      );
      buf.append("text{text=\"Merchants will only appear by market stalls or in finished structures where no other creatures but you stand.\"}");
      buf.append(
         "text{type=\"bold\";text=\"Note that if you change kingdom for any reason, you will lose this contract since the merchant stays in the old kingdom.\"}"
      );
      buf.append(
         "text{text=\"If you are away for several months the merchant may leave or be forced to leave with all the items and coins in his inventory.\"}"
      );
      if (shop != null) {
         buf.append(
            "text{type=\"bold\";text=\"Use local price\"};text{text=\" means that the merchant will use his local supply when determining price for items.\"}"
         );
         buf.append("text{text=\"Otherwise the price used will be the standard base value for the item.\"}");
         buf.append("text{type=\"bold\";text=\"Price modifier\"};text{text=\" is the value he will apply to the price.\"}");
         buf.append("text{text=\"If you set specific prices on items, those prices will be used regardless of any price settings.\"}");
         buf.append(
            "text{type=\"bold\";text=\"Last sold\"};text{text=\"is the number of days, hours and minutes since a personal merchant last sold an item.\"}"
         );
         long timeleft = 0L;
         if (trader != null) {
            buf.append(
               "table{rows=\"2\";cols=\"9\";label{text=\"name\"};label{text=\"Use local price\"};label{text=\"Price modifier\"};label{text=\"Manage prices\"};label{text=\"Last sold\"};label{text=\"Sold month\"};label{text=\"Sold life\"};label{text=\"Ratio\"};label{text=\"Free slots\"}"
            );
            timeleft = System.currentTimeMillis() - shop.getLastPolled();
            long daysleft = timeleft / 86400000L;
            long hoursleft = (timeleft - daysleft * 86400000L) / 3600000L;
            long minutesleft = (timeleft - daysleft * 86400000L - hoursleft * 3600000L) / 60000L;
            String times = "";
            if (daysleft > 0L) {
               times = times + daysleft + " days";
            }

            if (hoursleft > 0L) {
               String aft = "";
               if (daysleft > 0L && minutesleft > 0L) {
                  times = times + ", ";
                  aft = aft + " and ";
               } else if (daysleft > 0L) {
                  times = times + " and ";
               } else if (minutesleft > 0L) {
                  aft = aft + " and ";
               }

               times = times + hoursleft + " hours" + aft;
            }

            if (minutesleft > 0L) {
               times = times + minutesleft + " minutes";
            }

            buf.append("label{text=\"" + trader.getName() + "\"};");
            String ch = shop.usesLocalPrice() ? "selected=\"true\";" : "selected=\"false\";";
            buf.append("checkbox{id=\"" + traderId + "local\";" + ch + "text=\" \"}");
            buf.append("input{maxchars=\"4\"; id=\"" + traderId + "pricemod\"; text=\"" + shop.getPriceModifier() + "\"}");
            buf.append("checkbox{id=\"" + traderId + "manage\";selected=\"false\";text=\" \"}");
            buf.append("label{text=\"" + times + "\"}");
            buf.append("label{text=\"" + new Change(shop.getMoneyEarnedMonth()).getChangeShortString() + "\"}");
            buf.append("label{text=\"" + new Change(shop.getMoneyEarnedLife()).getChangeShortString() + "\"}");
            buf.append("label{text=\"" + shop.getSellRatio() + "\"}");
            buf.append("label{text=\"" + (TradeHandler.getMaxNumPersonalItems() - trader.getNumberOfShopItems()) + "\"}}");
            buf.append("text{type=\"bold\";text=\"Dismissing\"};text{text=\"if you dismiss a merchant they will take all items with them!\"}");
            buf.append("harray{label{text=\"Dismiss\"};checkbox{id=\"" + traderId + "dismiss\";selected=\"false\";text=\" \"}}");
         } else {
            buf.append("label{text=\"A merchant that should be here is missing. The id is " + traderId + "\"}");
         }
      } else {
         buf.append("text{type=\"bold\";text=\"Hire personal merchant:\"}");
         buf.append("text{text=\"By using this contract a personal merchant will appear.\"}");
         buf.append("text{text=\"The merchant will appear where you stand, if the tile contains no other creature.\"}");
         buf.append("text{text=\"Every trade he does he will charge one tenth (10%) of the value sold.\"}");
         buf.append("text{text=\"You add items to his stock, and retrieve money for items he has sold by trading with him.\"}");
         buf.append("text{text=\"Gender: \"}");
         if (this.getResponder().getSex() == 1) {
            buf.append("radio{ group=\"gender\"; id=\"male\";text=\"Male\"}");
            buf.append("radio{ group=\"gender\"; id=\"female\";text=\"Female\";selected=\"true\"}");
         } else {
            buf.append("radio{ group=\"gender\"; id=\"male\";text=\"Male\";selected=\"true\"}");
            buf.append("radio{ group=\"gender\"; id=\"female\";text=\"Female\"}");
         }

         buf.append("harray{label{text=\"The merchant shalt be called \"};input{id=\"ptradername\";maxchars=\"20\"};label{text=\"!\"}}");
      }

      return buf.toString();
   }

   private String mayorDismissingQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());

      try {
         Creature trader = Creatures.getInstance().getCreature(this.target);
         if (trader.isNpcTrader()) {
            Shop shop = Economy.getEconomy().getShop(trader);
            buf.append("text{text=\"You may dismiss this merchant now, since ");
            if (shop.getNumberOfItems() == 0 && shop.howLongEmpty() > 2419200000L) {
               buf.append("it has not had anything for sale for a long time and is bored.\"}");
            } else {
               buf.append("the person controlling it is long gone.\"}");
            }

            buf.append("text{text=\"Will you dismiss this merchant?\"}");
            buf.append("radio{ group=\"dism\";id=\"true\";text=\"Yes\"}");
            buf.append("radio{ group=\"dism\";id=\"false\";text=\"No\";selected=\"true\"}");
         } else {
            buf.append("text{text=\"Not a merchant?\"}");
         }
      } catch (NoSuchCreatureException var4) {
         logger.log(Level.WARNING, "Merchant for " + this.getResponder().getName() + " can't be found.");
         buf.append("label{text=\"Missing merchant?\"}");
      }

      return buf.toString();
   }
}
