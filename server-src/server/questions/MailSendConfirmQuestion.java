package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmMail;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MailSendConfirmQuestion extends Question implements MonetaryConstants, MiscConstants, TimeConstants {
   private static final Logger logger = Logger.getLogger(MailSendConfirmQuestion.class.getName());
   private final Item mailbox;
   private final Item[] items;
   private final String receiver;
   private final boolean[] cods;
   private long fullprice = 0L;
   private final long receiverId;
   private final int targetServer;

   MailSendConfirmQuestion(
      Creature aResponder, String aTitle, String aQuestion, Item aMailbox, Item[] aItems, boolean[] aCods, String aReceiver, long[] aReceiverInfo
   ) {
      super(aResponder, aTitle, aQuestion, 55, aMailbox.getWurmId());
      this.mailbox = aMailbox;
      this.items = aItems;
      this.receiver = LoginHandler.raiseFirstLetter(aReceiver);
      this.cods = aCods;
      this.receiverId = aReceiverInfo[1];
      this.targetServer = (int)aReceiverInfo[0];
   }

   @Override
   public void answer(Properties answers) {
      if (!MailSendQuestion.validateMailboxContents(this.items, this.mailbox)) {
         this.getResponder().getCommunicator().sendNormalServerMessage("The items in the mailbox have changed. Please try sending again.");
      } else {
         if (this.getResponder().getMoney() < this.fullprice) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You can not afford sending the packages.");
         } else if (!Servers.loginServer.isAvailable(5, true)) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You may not send mail right now. The service is unavailable.");
         } else {
            boolean charge = false;
            boolean revert = false;
            int codprice = 0;
            boolean local = Servers.localServer.id == this.targetServer;
            WurmMail mail = null;
            int revertx = 0;
            Set<WurmMail> mails = null;
            Set<Item> mailitems = null;
            if (!local) {
               boolean changingCluster = false;
               ServerEntry entry = Servers.getServerWithId(this.targetServer);
               if (entry == null) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("You can not mail that far.");
                  return;
               }

               if (changingCluster) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("You can not mail that far.");
                  return;
               }

               if (!entry.isConnected()) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("That island is not available currently. Please try again later.");
                  return;
               }

               mails = new HashSet<>();
               mailitems = new HashSet<>();
            }

            long timeavail = System.currentTimeMillis() + (long)(101 - (int)this.mailbox.getSpellCourierBonus()) * 60000L;
            if (this.getResponder().getPower() > 0) {
               timeavail = System.currentTimeMillis() + 60000L;
            }

            Item realItem = null;
            int x = 0;

            while(true) {
               label289: {
                  if (x < this.items.length) {
                     try {
                        label292: {
                           realItem = Items.getItem(this.items[x].getWurmId());
                           if (this.items[x].getTemplateId() == 651) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("The gift boxes are not handled by the mail service.");
                              return;
                           }

                           if (this.items[x].isCoin()) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("Coins are currently not handled by the mail service.");
                              return;
                           }

                           if (this.items[x].isBanked()) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("The " + this.items[x].getName() + " is currently unavailable.");
                              return;
                           }

                           if (this.items[x].isUnfinished()) {
                              this.getResponder().getCommunicator().sendNormalServerMessage("Unfinished items would be broken by the mail service.");
                              return;
                           }

                           if ((this.items[x].getTemplateId() != 665 || !this.items[x].isLocked())
                              && (this.items[x].getTemplateId() != 192 || !this.items[x].isLocked())) {
                              codprice = 0;
                              String key = "";
                              String val = "";
                              if (this.cods[x]) {
                                 key = x + "g";
                                 val = answers.getProperty(key);
                                 if (val != null && val.length() > 0) {
                                    try {
                                       codprice = Integer.parseInt(val) * 1000000;
                                    } catch (NumberFormatException var22) {
                                       this.getResponder()
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "Failed to set the gold price for "
                                                + realItem.getName()
                                                + ". Note that a coin value is in whole numbers, no decimals."
                                          );
                                    }
                                 }

                                 key = x + "s";
                                 val = answers.getProperty(key);
                                 if (val != null && val.length() > 0) {
                                    try {
                                       codprice += Integer.parseInt(val) * 10000;
                                    } catch (NumberFormatException var21) {
                                       this.getResponder()
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "Failed to set a silver price for "
                                                + realItem.getName()
                                                + ". Note that a coin value is in whole numbers, no decimals."
                                          );
                                    }
                                 }

                                 key = x + "c";
                                 val = answers.getProperty(key);
                                 if (val != null && val.length() > 0) {
                                    try {
                                       codprice += Integer.parseInt(val) * 100;
                                    } catch (NumberFormatException var20) {
                                       this.getResponder()
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "Failed to set a copper price for "
                                                + realItem.getName()
                                                + ". Note that a coin value is in whole numbers, no decimals."
                                          );
                                    }
                                 }

                                 key = x + "i";
                                 val = answers.getProperty(key);
                                 if (val != null && val.length() > 0) {
                                    try {
                                       codprice += Integer.parseInt(val);
                                    } catch (NumberFormatException var19) {
                                       this.getResponder()
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "Failed to set an iron price for "
                                                + realItem.getName()
                                                + ". Note that a coin value is in whole numbers, no decimals."
                                          );
                                    }
                                 }

                                 if (codprice <= 0) {
                                    codprice = 1;
                                    this.getResponder().getCommunicator().sendNormalServerMessage("Cod price set to 1 iron, since it was negative or zero.");
                                 }

                                 mail = new WurmMail(
                                    (byte)1,
                                    realItem.getWurmId(),
                                    this.getResponder().getWurmId(),
                                    this.receiverId,
                                    (long)codprice,
                                    timeavail,
                                    System.currentTimeMillis() + (Servers.localServer.testServer ? 3600000L : 604800000L) * 2L,
                                    Servers.localServer.id,
                                    false,
                                    false
                                 );
                              } else {
                                 charge = true;
                                 mail = new WurmMail(
                                    (byte)0,
                                    realItem.getWurmId(),
                                    this.getResponder().getWurmId(),
                                    this.receiverId,
                                    (long)codprice,
                                    timeavail,
                                    System.currentTimeMillis() + (Servers.localServer.testServer ? 3600000L : 604800000L) * 2L,
                                    Servers.localServer.id,
                                    false,
                                    false
                                 );
                              }

                              if (local) {
                                 WurmMail.addWurmMail(mail);
                                 mail.createInDatabase();
                              } else {
                                 mails.add(mail);
                              }

                              if (realItem.getParentId() != this.mailbox.getWurmId()) {
                                 revert = true;
                                 this.getResponder().getCommunicator().sendAlertServerMessage("The " + realItem.getName() + " is no longer in the mailbox!");
                                 break label292;
                              }

                              realItem.putInVoid();
                              realItem.setMailed(true);
                              realItem.setMailTimes((byte)(realItem.getMailTimes() + 1));
                              Item[] contained = realItem.getAllItems(true);

                              for(int c = 0; c < contained.length; ++c) {
                                 contained[c].setMailed(true);
                                 contained[c].setMailTimes((byte)(contained[c].getMailTimes() + 1));
                                 if (!local) {
                                    mailitems.add(contained[c]);
                                 }
                              }

                              if (!local) {
                                 mailitems.add(realItem);
                              }

                              revertx = x;
                              break label289;
                           }

                           this.getResponder().getCommunicator().sendNormalServerMessage(this.items[x].getNameWithGenus() + " cannot be mailed while locked.");
                           return;
                        }
                     } catch (NoSuchItemException var24) {
                        revert = true;
                        this.getResponder().getCommunicator().sendAlertServerMessage("The " + this.items[x].getName() + " is no longer in the mailbox!");
                     }
                  }

                  if (!local) {
                     revert = sendMailSetToServer(
                        this.getResponder().getWurmId(),
                        this.getResponder(),
                        this.targetServer,
                        mails,
                        this.receiverId,
                        mailitems.toArray(new Item[mailitems.size()])
                     );
                  }

                  if (revert) {
                     charge = false;
                  }

                  if (charge) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     long newBalance = lsw.chargeMoney(this.getResponder().getName(), this.fullprice);
                     if (newBalance < 0L) {
                        this.getResponder().getCommunicator().sendAlertServerMessage("The spirits seem to deliver for free this time.");
                        logger.log(Level.WARNING, "Failed to withdraw " + this.fullprice + " iron from " + this.getResponder().getName() + ". Mail was free.");
                     } else {
                        try {
                           this.getResponder().setMoney(newBalance);
                        } catch (IOException var18) {
                           logger.log(Level.WARNING, this.getResponder().getName() + " " + var18.getMessage(), (Throwable)var18);
                        }

                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You have been charged " + new Change(this.fullprice).getChangeString() + ".");
                     }
                  }

                  if (revert) {
                     for(int xx = 0; xx < revertx + 1; ++xx) {
                        try {
                           realItem = Items.getItem(this.items[xx].getWurmId());
                           if (realItem.getParentId() != this.mailbox.getWurmId()) {
                              WurmMail.removeMail(this.items[xx].getWurmId());
                              realItem.setMailed(false);
                              realItem.setMailTimes((byte)(realItem.getMailTimes() - 1));
                              Item[] contained = realItem.getAllItems(true);

                              for(int c = 0; c < contained.length; ++c) {
                                 contained[c].setMailed(false);
                                 contained[c].setMailTimes((byte)(contained[c].getMailTimes() - 1));
                                 contained[c].setLastOwnerId(this.getResponder().getWurmId());
                              }
                           }
                        } catch (NoSuchItemException var23) {
                        }
                     }

                     return;
                  } else {
                     String time = "just under two hours.";
                     float bon = this.mailbox.getSpellCourierBonus();
                     if (bon > 90.0F) {
                        time = "less than ten minutes.";
                     } else if (bon > 70.0F) {
                        time = "less than thirty minutes.";
                     } else if (bon > 40.0F) {
                        time = "less than an hour.";
                     } else if (bon > 10.0F) {
                        time = "a bit more than an hour.";
                     }

                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("The items silently disappear from the " + this.mailbox.getName() + ". You expect them to arrive in " + time);
                     if (!local) {
                        for(int xx = 0; xx < this.items.length; ++xx) {
                           Item[] contained = this.items[xx].getAllItems(true);

                           for(int c = 0; c < contained.length; ++c) {
                              logger.log(
                                 Level.INFO,
                                 this.getResponder().getName()
                                    + " destroying contained "
                                    + contained[c].getName()
                                    + ", ql "
                                    + contained[c].getQualityLevel()
                                    + " wid="
                                    + contained[c].getWurmId()
                              );
                              Items.destroyItem(contained[c].getWurmId(), true, true);
                           }

                           logger.log(
                              Level.INFO,
                              this.getResponder().getName()
                                 + " destroying "
                                 + this.items[xx].getName()
                                 + ", ql "
                                 + this.items[xx].getQualityLevel()
                                 + " wid="
                                 + this.items[xx].getWurmId()
                           );
                           Items.destroyItem(this.items[xx].getWurmId(), true, true);
                        }
                     }
                     break;
                  }
               }

               ++x;
            }
         }
      }
   }

   public static final boolean sendMailSetToServer(
      long senderId, @Nullable Creature responder, int targetServer, Set<WurmMail> mails, long receiverId, Item[] items
   ) {
      boolean revert = false;
      LoginServerWebConnection lsw = null;
      ServerEntry entry = Servers.getServerWithId(targetServer);
      if (entry != null) {
         if (entry.isAvailable(5, true)) {
            lsw = new LoginServerWebConnection(targetServer);
         } else {
            if (responder != null) {
               responder.getCommunicator().sendNormalServerMessage("The inter-island mail service is on strike right now. Please try later.");
            }

            revert = true;
         }
      } else {
         lsw = new LoginServerWebConnection();
      }

      if (!revert) {
         WurmMail[] mailarr = mails.toArray(new WurmMail[mails.size()]);
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ByteArrayOutputStream bos2 = new ByteArrayOutputStream();

         try {
            DataOutputStream dos = new DataOutputStream(bos);
            DataOutputStream dos2 = new DataOutputStream(bos2);
            dos.writeInt(items.length);

            for(int x = 0; x < items.length; ++x) {
               if (responder != null) {
                  logger.log(
                     Level.INFO,
                     responder.getName()
                        + " sending "
                        + items[x].getName()
                        + ", ql "
                        + items[x].getQualityLevel()
                        + " wid="
                        + items[x].getWurmId()
                        + " to "
                        + (entry != null ? entry.getName() : targetServer)
                  );
               } else {
                  logger.log(
                     Level.INFO,
                     senderId
                        + " sending "
                        + items[x].getName()
                        + ", ql "
                        + items[x].getQualityLevel()
                        + " wid="
                        + items[x].getWurmId()
                        + " to "
                        + (entry != null ? entry.getName() : targetServer)
                  );
               }

               PlayerTransfer.sendItem(items[x], dos, false);
            }

            dos.flush();
            dos.close();
            dos2.writeInt(mailarr.length);

            for(int x = 0; x < mailarr.length; ++x) {
               dos2.writeByte(mailarr[x].type);
               dos2.writeLong(mailarr[x].itemId);
               dos2.writeLong(mailarr[x].sender);
               dos2.writeLong(mailarr[x].receiver);
               dos2.writeLong(mailarr[x].price);
               dos2.writeLong(mailarr[x].sent);
               dos2.writeLong(mailarr[x].expiration);
               dos2.writeInt(mailarr[x].sourceserver);
               dos2.writeBoolean(mailarr[x].rejected);
            }

            dos2.flush();
            dos2.close();
         } catch (Exception var17) {
            logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
         }

         byte[] itemdata = bos.toByteArray();
         byte[] maildata = bos2.toByteArray();
         String result = lsw.sendMail(maildata, itemdata, senderId, receiverId, targetServer);
         if (result.length() != 0) {
            revert = true;
            if (responder != null) {
               responder.getCommunicator()
                  .sendAlertServerMessage(
                     "The spirits in the mailbox reported a problem when sending the contents of the mailbox. Reverting. The message was: " + result
                  );
               logger.log(Level.WARNING, responder.getName() + ", " + result);
            }
         }
      }

      return revert;
   }

   public static final int getCostForItem(Item i, float priceMod) {
      if (i.isComponentItem()) {
         return 0;
      } else if (i.getTemplateId() == 1392) {
         return 0;
      } else {
         int pcost = 10;
         int combinePriceMod = 1;
         if (i.getTemplateId() == 748 || i.getTemplateId() == 1272 || i.getTemplateId() == 1403) {
            InscriptionData insData = i.getInscription();
            if (insData != null) {
               if (insData.hasBeenInscribed()) {
                  return 1;
               }
            } else {
               Item parent = i.getParentOrNull();
               if (parent != null
                  && (parent.getTemplateId() == 1409 || parent.getTemplateId() == 1404 || parent.getTemplateId() == 1127 || parent.getTemplateId() == 1128)) {
                  return 1;
               }
            }
         }

         if (i.isCombine() || i.isLiquid()) {
            combinePriceMod = Math.max(1, i.getWeightGrams() / 5000);
         }

         pcost *= combinePriceMod;
         pcost = (int)((float)pcost * priceMod);
         return pcost * 1 * 10;
      }
   }

   @Override
   public void sendQuestion() {
      float priceMod = 1.0F;
      String lHtml = this.getBmlHeader();
      StringBuilder buf = new StringBuilder(lHtml);
      buf.append(
         "text{text='This should give you an overview of how much the cost will be for sending the contents of the mailbox, visible on the bottom of this dialogue.'}"
      );
      buf.append("text{text='C.O.D means Cash On Delivery, which is good for selling items to other players. A C.O.D item costs nothing for you to send.'}");
      buf.append(
         "text{text='The Mail cost is what the spirits charge to deliver the item. The C.O.D cost payed by the receiver will be what you enter plus the Mail cost.'}"
      );
      buf.append("text{text='Example: You check the checkbox for a Mallet which say has a 1 copper coins Mail cost.'}");
      buf.append(
         "text{text='You enter 20 in the C.O.D copper coins textbox. This means the receiver will have to pay 21 copper in all in order to receive the mallet of which you receive 20.'}"
      );
      buf.append(
         "text{type='italic';text='Note that if a C.O.D receiver returns the item you have to pay a 1 copper (or 1 iron for paper/papryus) fee to retrieve it.'}"
      );
      buf.append(
         "text{text='If the item is rejected you have two weeks to pick it up or it will be destroyed by the spirits since it conflicts with their banking policy.'}"
      );
      buf.append("text{text=''};");
      this.fullprice = 0L;

      for(int x = 0; x < this.items.length; ++x) {
         if (!this.cods[x]) {
            int pcost = getCostForItem(this.items[x], priceMod);
            this.fullprice += (long)pcost;
            Item[] contained = this.items[x].getAllItems(true);

            for(int c = 0; c < contained.length; ++c) {
               pcost = getCostForItem(contained[c], priceMod);
               this.fullprice += (long)pcost;
            }
         }
      }

      Change change = new Change(this.fullprice);
      buf.append("text{type='bold';text=\"The cost for sending these items will be " + change.getChangeString() + ".\"};");
      if (this.getResponder().getMoney() < this.fullprice) {
         buf.append("text{type='bold';text=\"You can not afford that. You need to add some money to your bank account.\"};");
      } else {
         int pcost = 0;
         buf.append(
            "table{rows='"
               + (this.items.length + 1)
               + "'; cols='9';label{text='Item name'};label{text='QL'};label{text='DAM'};label{text='C.O.D'};label{text='Mail cost'};label{text='Your price in G'};label{text=',S'};label{text=',C'};label{text=',I'};"
         );

         for(int x = 0; x < this.items.length; ++x) {
            buf.append(itemNameWithColorByRarity(this.items[x]));
            buf.append("label{text=\"" + String.format("%.2f", this.items[x].getQualityLevel()) + "\"};");
            buf.append("label{text=\"" + String.format("%.2f", this.items[x].getDamage()) + "\"};");
            pcost = getCostForItem(this.items[x], priceMod);
            Item[] contained = this.items[x].getAllItems(true);

            for(int c = 0; c < contained.length; ++c) {
               pcost += getCostForItem(contained[c], priceMod);
            }

            change = new Change((long)pcost);
            if (this.cods[x]) {
               buf.append("label{text=\"yes\"};");
               buf.append("label{text=\"" + change.getChangeShortString() + "\"};");
               buf.append("input{maxchars='2'; id='" + x + "g';text='0'};");
               buf.append("input{maxchars='2'; id='" + x + "s';text='0'};");
               buf.append("input{maxchars='2'; id='" + x + "c';text='0'};");
               buf.append("input{maxchars='2'; id='" + x + "i';text='0'}");
            } else {
               buf.append("label{text=\"no\"};");
               buf.append("label{text=\"" + change.getChangeShortString() + "\"};");
               buf.append("label{text='0'};");
               buf.append("label{text='0'};");
               buf.append("label{text='0'};");
               buf.append("label{text='0'};");
            }
         }

         buf.append("}");
      }

      buf.append(this.createAnswerButton3());
      this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
