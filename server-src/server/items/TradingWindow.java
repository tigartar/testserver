package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.tutorial.MissionTargets;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.util.MaterialUtilities;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class TradingWindow implements MiscConstants, ItemTypes, VillageStatus, CreatureTypes, MonetaryConstants {
   private final Creature windowowner;
   private final Creature watcher;
   private final boolean offer;
   private final long wurmId;
   private Set<Item> items;
   private final Trade trade;
   private static final Logger logger = Logger.getLogger(TradingWindow.class.getName());
   private static final Map<String, Logger> loggers = new HashMap<>();

   TradingWindow(Creature aOwner, Creature aWatcher, boolean aOffer, long aWurmId, Trade aTrade) {
      this.windowowner = aOwner;
      this.watcher = aWatcher;
      this.offer = aOffer;
      this.wurmId = aWurmId;
      this.trade = aTrade;
   }

   public static final void stopLoggers() {
      for(Logger logger : loggers.values()) {
         if (logger != null) {
            for(Handler h : logger.getHandlers()) {
               h.close();
            }
         }
      }
   }

   private static Logger getLogger(long wurmid) {
      String name = "trader" + wurmid;
      Logger personalLogger = loggers.get(name);
      if (personalLogger == null) {
         personalLogger = Logger.getLogger(name);
         personalLogger.setUseParentHandlers(false);
         Handler[] h = logger.getHandlers();

         for(int i = 0; i != h.length; ++i) {
            personalLogger.removeHandler(h[i]);
         }

         try {
            FileHandler fh = new FileHandler(name + ".log", 0, 1, true);
            fh.setFormatter(new SimpleFormatter());
            personalLogger.addHandler(fh);
         } catch (IOException var6) {
            Logger.getLogger(name).log(Level.WARNING, name + ":no redirection possible!");
         }

         loggers.put(name, personalLogger);
      }

      return personalLogger;
   }

   public boolean mayMoveItemToWindow(Item item, Creature creature, long window) {
      boolean toReturn = false;
      if (this.wurmId == 3L) {
         if (window == 1L) {
            toReturn = true;
         }
      } else if (this.wurmId == 4L) {
         if (window == 2L) {
            toReturn = true;
         }
      } else if (this.wurmId == 2L) {
         if (!this.windowowner.equals(creature)) {
            if (creature.isPlayer() && item.isCoin() && !this.windowowner.isPlayer()) {
               return false;
            }

            if (window == 4L) {
               toReturn = true;
            }
         }
      } else if (this.wurmId == 1L
         && !this.windowowner.equals(creature)
         && window == 3L
         && this.watcher == creature
         && item.getOwnerId() == this.windowowner.getWurmId()) {
         toReturn = true;
      }

      return toReturn;
   }

   public boolean mayAddFromInventory(Creature creature, Item item) {
      if (!item.isTraded()) {
         if (item.isNoTrade()) {
            creature.getCommunicator().sendSafeServerMessage(item.getNameWithGenus() + " is not tradable.");
         } else if (this.windowowner.equals(creature)) {
            try {
               long owneri = item.getOwner();
               if (owneri != this.watcher.getWurmId() && owneri != this.windowowner.getWurmId()) {
                  this.windowowner.setCheated("Traded " + item.getName() + "[" + item.getWurmId() + "] with " + this.watcher.getName() + " owner=" + owneri);
               }
            } catch (NotOwnedException var8) {
               this.windowowner.setCheated("Traded " + item.getName() + "[" + item.getWurmId() + "] with " + this.watcher.getName() + " not owned?");
            }

            if (this.wurmId == 2L || this.wurmId == 1L) {
               if (item.isHollow()) {
                  Item[] its = item.getAllItems(true);

                  for(Item lIt : its) {
                     if (lIt.isNoTrade() || lIt.isVillageDeed() || lIt.isHomesteadDeed() || lIt.getTemplateId() == 781) {
                        creature.getCommunicator().sendSafeServerMessage(item.getNameWithGenus() + " contains a non-tradable item.");
                        return false;
                     }
                  }
               }

               return true;
            }
         }
      }

      return false;
   }

   public long getWurmId() {
      return this.wurmId;
   }

   public Item[] getItems() {
      return this.items != null ? this.items.toArray(new Item[this.items.size()]) : new Item[0];
   }

   private void removeExistingContainedItems(Item item) {
      if (item.isHollow()) {
         Item[] itemarr = item.getItemsAsArray();

         for(Item lElement : itemarr) {
            this.removeExistingContainedItems(lElement);
            if (lElement.getTradeWindow() == this) {
               this.removeFromTrade(lElement, false);
            } else if (lElement.getTradeWindow() != null) {
               lElement.getTradeWindow().removeItem(lElement);
            }
         }
      }
   }

   public Item[] getAllItems() {
      if (this.items == null) {
         return new Item[0];
      } else {
         Set<Item> toRet = new HashSet<>();

         for(Item item : this.items) {
            toRet.add(item);
            Item[] toAdd = item.getAllItems(false);

            for(Item lElement : toAdd) {
               if (lElement.tradeWindow == this) {
                  toRet.add(lElement);
               }
            }
         }

         return toRet.toArray(new Item[toRet.size()]);
      }
   }

   public void stopReceivingItems() {
   }

   public void startReceivingItems() {
   }

   public void addItem(Item item) {
      if (this.items == null) {
         this.items = new HashSet<>();
      }

      if (item.tradeWindow == null) {
         this.removeExistingContainedItems(item);
         Item parent = item;

         try {
            parent = item.getParent();
         } catch (NoSuchItemException var4) {
         }

         this.items.add(item);
         this.addToTrade(item, parent);
         if (item == parent || parent.isViewableBy(this.windowowner)) {
            if (!this.windowowner.isPlayer()) {
               this.windowowner.getCommunicator().sendAddToInventory(item, this.wurmId, parent.tradeWindow == this ? parent.getWurmId() : 0L, 0);
            } else if (!this.watcher.isPlayer()) {
               this.windowowner
                  .getCommunicator()
                  .sendAddToInventory(
                     item, this.wurmId, parent.tradeWindow == this ? parent.getWurmId() : 0L, this.watcher.getTradeHandler().getTraderBuyPriceForItem(item)
                  );
            } else {
               this.windowowner.getCommunicator().sendAddToInventory(item, this.wurmId, parent.tradeWindow == this ? parent.getWurmId() : 0L, item.getPrice());
            }
         }

         if (item == parent || parent.isViewableBy(this.watcher)) {
            if (!this.watcher.isPlayer()) {
               this.watcher.getCommunicator().sendAddToInventory(item, this.wurmId, parent.tradeWindow == this ? parent.getWurmId() : 0L, 0);
            } else if (!this.windowowner.isPlayer()) {
               this.watcher
                  .getCommunicator()
                  .sendAddToInventory(
                     item,
                     this.wurmId,
                     parent.tradeWindow == this ? parent.getWurmId() : 0L,
                     this.windowowner.getTradeHandler().getTraderSellPriceForItem(item, this)
                  );
            } else {
               this.watcher.getCommunicator().sendAddToInventory(item, this.wurmId, parent.tradeWindow == this ? parent.getWurmId() : 0L, item.getPrice());
            }
         }
      }

      this.tradeChanged();
   }

   private void addToTrade(Item item, Item parent) {
      if (item.tradeWindow != this) {
         item.setTradeWindow(this);
      }

      Item[] its = item.getItemsAsArray();

      for(Item lIt : its) {
         this.addToTrade(lIt, item);
      }
   }

   private void removeFromTrade(Item item, boolean noSwap) {
      this.windowowner.getCommunicator().sendRemoveFromInventory(item, this.wurmId);
      this.watcher.getCommunicator().sendRemoveFromInventory(item, this.wurmId);
      if (noSwap && item.isCoin()) {
         if (item.getOwnerId() == -10L) {
            Economy.getEconomy().returnCoin(item, "Notrade", true);
         }

         item.setTradeWindow(null);
      } else {
         item.setTradeWindow(null);
      }
   }

   public void removeItem(Item item) {
      if (this.items != null && item.tradeWindow == this) {
         this.removeExistingContainedItems(item);
         this.items.remove(item);
         this.removeFromTrade(item, true);
         this.tradeChanged();
      }
   }

   public void updateItem(Item item) {
      if (this.items != null && item.tradeWindow == this) {
         if (!this.windowowner.isPlayer()) {
            this.windowowner.getCommunicator().sendUpdateInventoryItem(item, this.wurmId, 0);
         } else if (!this.watcher.isPlayer()) {
            this.windowowner.getCommunicator().sendUpdateInventoryItem(item, this.wurmId, this.watcher.getTradeHandler().getTraderBuyPriceForItem(item));
         } else {
            this.windowowner.getCommunicator().sendUpdateInventoryItem(item, this.wurmId, item.getPrice());
         }

         if (!this.watcher.isPlayer()) {
            this.watcher.getCommunicator().sendUpdateInventoryItem(item, this.wurmId, 0);
         } else if (!this.windowowner.isPlayer()) {
            this.watcher
               .getCommunicator()
               .sendUpdateInventoryItem(item, this.wurmId, this.windowowner.getTradeHandler().getTraderSellPriceForItem(item, this));
         } else {
            this.watcher.getCommunicator().sendUpdateInventoryItem(item, this.wurmId, item.getPrice());
         }

         this.tradeChanged();
      }
   }

   private void tradeChanged() {
      if (this.wurmId == 2L && !this.trade.creatureTwo.isPlayer()) {
         this.trade.setCreatureTwoSatisfied(false);
      }

      if (this.wurmId == 3L || this.wurmId == 4L) {
         this.trade.setCreatureOneSatisfied(false);
         this.trade.setCreatureTwoSatisfied(false);
         int c = this.trade.getNextTradeId();
         this.windowowner.getCommunicator().sendTradeChanged(c);
         this.watcher.getCommunicator().sendTradeChanged(c);
      }
   }

   boolean hasInventorySpace() {
      if (this.offer) {
         this.windowowner.getCommunicator().sendAlertServerMessage("There is a bug in the trade system. This shouldn't happen. Please report.");
         this.watcher.getCommunicator().sendAlertServerMessage("There is a bug in the trade system. This shouldn't happen. Please report.");
         logger.log(
            Level.WARNING,
            "Inconsistency! This is offer window number " + this.wurmId + ". Traders are " + this.watcher.getName() + ", " + this.windowowner.getName()
         );
         return false;
      } else if (!(this.watcher instanceof Player)) {
         return true;
      } else {
         Item inventory = this.watcher.getInventory();
         if (inventory == null) {
            this.windowowner.getCommunicator().sendAlertServerMessage("Could not find inventory for " + this.watcher.getName() + ". Trade aborted.");
            this.watcher.getCommunicator().sendAlertServerMessage("Could not find your inventory item. Trade aborted. Please contact administrators.");
            logger.log(Level.WARNING, "Failed to locate inventory for " + this.watcher.getName());
            return false;
         } else {
            if (this.items != null) {
               int nums = 0;

               for(Item item : this.items) {
                  if (!inventory.testInsertItem(item)) {
                     return false;
                  }

                  if (!item.isCoin()) {
                     ++nums;
                  }

                  if (!item.canBeDropped(false) && ((Player)this.watcher).isGuest()) {
                     this.windowowner.getCommunicator().sendAlertServerMessage("Guests cannot receive the item " + item.getName() + ".");
                     this.watcher.getCommunicator().sendAlertServerMessage("Guests cannot receive the item " + item.getName() + ".");
                     return false;
                  }
               }

               if (this.watcher.getPower() <= 0 && nums + inventory.getNumItemsNotCoins() > 99) {
                  this.watcher.getCommunicator().sendAlertServerMessage("You may not carry that many items in your inventory.");
                  this.windowowner
                     .getCommunicator()
                     .sendAlertServerMessage(this.watcher.getName() + " may not carry that many items in " + this.watcher.getHisHerItsString() + " inventory.");
                  return false;
               }
            }

            return true;
         }
      }
   }

   int getWeight() {
      int toReturn = 0;
      if (this.items != null) {
         for(Item item : this.items) {
            toReturn += item.getFullWeight();
         }
      }

      return toReturn;
   }

   boolean validateTrade() {
      if (this.windowowner.isDead()) {
         return false;
      } else if (this.windowowner instanceof Player && !this.windowowner.hasLink()) {
         return false;
      } else {
         if (this.items != null) {
            for(Item tit : this.items) {
               if ((this.windowowner instanceof Player || !tit.isCoin()) && tit.getOwnerId() != this.windowowner.getWurmId()) {
                  this.windowowner.getCommunicator().sendAlertServerMessage(tit.getName() + " is not owned by you. Trade aborted.");
                  this.watcher.getCommunicator().sendAlertServerMessage(tit.getName() + " is not owned by " + this.windowowner.getName() + ". Trade aborted.");
                  return false;
               }

               Item[] allItems = tit.getAllItems(false);

               for(Item lAllItem : allItems) {
                  if ((this.windowowner instanceof Player || !lAllItem.isCoin()) && lAllItem.getOwnerId() != this.windowowner.getWurmId()) {
                     this.windowowner.getCommunicator().sendAlertServerMessage(lAllItem.getName() + " is not owned by you. Trade aborted.");
                     this.watcher
                        .getCommunicator()
                        .sendAlertServerMessage(lAllItem.getName() + " is not owned by " + this.windowowner.getName() + ". Trade aborted.");
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   void swapOwners() {
      boolean errors = false;
      if (!this.offer) {
         Item inventory = this.watcher.getInventory();
         Item ownInventory = this.windowowner.getInventory();
         Shop shop = null;
         int moneyAdded = 0;
         int moneyLost = 0;
         if (this.windowowner.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(this.windowowner);
         } else if (this.watcher.isNpcTrader()) {
            shop = Economy.getEconomy().getShop(this.watcher);
         }

         if (this.items != null) {
            Item[] its = this.items.toArray(new Item[this.items.size()]);

            for(Item lIt : its) {
               Item item = lIt;
               this.removeExistingContainedItems(lIt);
               this.removeFromTrade(lIt, false);
               boolean coin = lIt.isCoin();
               long parentId = lIt.getParentId();
               boolean ok = true;
               if (!(this.windowowner instanceof Player)) {
                  if (this.watcher.isLogged()) {
                     this.watcher
                        .getLogger()
                        .log(
                           Level.INFO,
                           this.windowowner.getName() + " buying " + lIt.getName() + " with id " + lIt.getWurmId() + " from " + this.watcher.getName()
                        );
                  }
               } else {
                  if (this.watcher instanceof Player) {
                     if (!lIt.isVillageDeed() && !lIt.isHomesteadDeed()) {
                        if (lIt.getTemplateId() == 300) {
                           long traderId = lIt.getData();
                           if (traderId != -1L) {
                              try {
                                 Creature trader = Server.getInstance().getCreature(traderId);
                                 if (trader.isNpcTrader()) {
                                    shop = Economy.getEconomy().getShop(trader);
                                 }

                                 shop.setOwner(this.watcher.getWurmId());
                                 this.watcher.getCommunicator().sendNormalServerMessage("You are now in control of " + trader.getName() + ".");
                                 this.windowowner.getCommunicator().sendNormalServerMessage("You are no longer in control of " + trader.getName() + ".");
                              } catch (NoSuchPlayerException var27) {
                                 logger.log(Level.WARNING, "Trader for " + traderId + " is a player? Well it can't be found.");
                                 lIt.setData(-10L);
                              } catch (NoSuchCreatureException var28) {
                                 logger.log(Level.WARNING, "Trader for " + traderId + " can't be found.");
                                 lIt.setData(-10L);
                              }
                           }
                        } else if (lIt.getTemplateId() == 1129) {
                           long wagonerId = lIt.getData();
                           if (wagonerId != -1L) {
                              Wagoner wagoner = Wagoner.getWagoner(wagonerId);
                              if (wagoner != null) {
                                 wagoner.setOwnerId(this.watcher.getWurmId());
                                 this.watcher.getCommunicator().sendNormalServerMessage("You are now in control of " + wagoner.getName() + ".");
                                 this.windowowner.getCommunicator().sendNormalServerMessage("You are no longer in control of " + wagoner.getName() + ".");
                              }
                           }
                        } else if (lIt.isRoyal()) {
                           if (lIt.getTemplateId() != 530 && lIt.getTemplateId() != 533 && lIt.getTemplateId() != 536 && !this.watcher.isKing()) {
                              this.watcher
                                 .getCommunicator()
                                 .sendNormalServerMessage(
                                    this.windowowner.getName()
                                       + " seems hesitatant about trading "
                                       + lIt.getName()
                                       + ". You need to be crowned the ruler first."
                                 );
                              this.windowowner
                                 .getCommunicator()
                                 .sendNormalServerMessage(
                                    "Those noble items should not be tainted by simple trade. You need to crown " + this.watcher.getName() + " ruler first."
                                 );
                              ok = false;
                           }
                        } else if (lIt.getTemplateId() == 781) {
                           this.watcher.getCommunicator().sendNormalServerMessage("You may not trade the " + lIt.getName() + ".");
                           this.windowowner.getCommunicator().sendNormalServerMessage("You may not trade the " + lIt.getName() + ".");
                           ok = false;
                        }
                     } else {
                        int data = lIt.getData2();
                        if (data > 0) {
                           if (!this.watcher.isPaying()) {
                              this.windowowner.getCommunicator().sendNormalServerMessage("You need to be premium in order to receive a deed.");
                              ok = false;
                           } else {
                              try {
                                 Village village = Villages.getVillage(data);
                                 Citizen oldMayor = village.getCitizen(this.windowowner.getWurmId());
                                 Village oldVillage = this.watcher.getCitizenVillage();
                                 if (this.windowowner.getKingdomId() != this.watcher.getKingdomId()) {
                                    this.windowowner
                                       .getCommunicator()
                                       .sendNormalServerMessage("You cannot trade the deed for " + village.getName() + " to another kingdom.");
                                    ok = false;
                                 }

                                 if (ok && oldVillage != null && oldVillage != village) {
                                    Citizen oldCit = oldVillage.getCitizen(this.watcher.getWurmId());
                                    VillageRole role = oldCit.getRole();
                                    if (role.getStatus() == 2) {
                                       this.watcher
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "You cannot trade the deed for "
                                                + village.getName()
                                                + " since you are already the mayor of "
                                                + oldVillage.getName()
                                          );
                                       this.windowowner
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             "You cannot trade the deed for "
                                                + village.getName()
                                                + " since "
                                                + this.watcher.getName()
                                                + " is already the mayor of "
                                                + oldVillage.getName()
                                          );
                                       ok = false;
                                    }

                                    if (ok && oldCit != null) {
                                       oldVillage.removeCitizen(this.watcher);
                                    }
                                 }

                                 if (ok) {
                                    if (oldMayor != null) {
                                       try {
                                          if (item.isVillageDeed()) {
                                             oldMayor.setRole(village.getRoleForStatus((byte)3));
                                          } else {
                                             village.removeCitizen(oldMayor);
                                          }
                                       } catch (IOException var33) {
                                          logger.log(
                                             Level.WARNING,
                                             "Error when removing " + this.windowowner.getName() + " as mayor: " + var33.getMessage(),
                                             (Throwable)var33
                                          );
                                          this.watcher
                                             .getCommunicator()
                                             .sendSafeServerMessage(
                                                "An error occured when removing " + this.windowowner.getName() + " as mayor. Please contact administration."
                                             );
                                          this.windowowner
                                             .getCommunicator()
                                             .sendSafeServerMessage("An error occured when removing you as mayor. Please contact administration.");
                                       }
                                    }

                                    if (village.getMayor() != null) {
                                       logger.log(
                                          Level.WARNING,
                                          "Error when changing mayor. Mayor should have been removed - "
                                             + this.windowowner.getName()
                                             + " with wurmid: "
                                             + this.windowowner.getWurmId()
                                             + ". Current mayor is "
                                             + village.getMayor().getId()
                                             + ". Removing that mayor anyways."
                                       );

                                       try {
                                          village.getMayor().setRole(village.getRoleForStatus((byte)3));
                                       } catch (IOException var32) {
                                          logger.log(
                                             Level.WARNING,
                                             "Error when removing " + this.windowowner.getName() + " as mayor: " + var32.getMessage(),
                                             (Throwable)var32
                                          );
                                          this.watcher
                                             .getCommunicator()
                                             .sendSafeServerMessage(
                                                "An error occured when removing " + this.windowowner.getName() + " as mayor. Please contact administration."
                                             );
                                          this.windowowner
                                             .getCommunicator()
                                             .sendSafeServerMessage("An error occured when removing you as mayor. Please contact administration.");
                                       }
                                    }

                                    Citizen newMayor = village.getCitizen(this.watcher.getWurmId());
                                    if (newMayor == null) {
                                       try {
                                          village.addCitizen(this.watcher, village.getRoleForStatus((byte)2));
                                       } catch (IOException var31) {
                                          logger.log(
                                             Level.WARNING,
                                             "Error when setting " + this.watcher.getName() + " as mayor: " + var31.getMessage(),
                                             (Throwable)var31
                                          );
                                          this.windowowner
                                             .getCommunicator()
                                             .sendSafeServerMessage(
                                                "An error occured when setting " + this.watcher.getName() + " as mayor. Please contact administration."
                                             );
                                          this.watcher
                                             .getCommunicator()
                                             .sendSafeServerMessage("An error occured when setting you as mayor. Please contact administration.");
                                       }
                                    } else {
                                       try {
                                          newMayor.setRole(village.getRoleForStatus((byte)2));
                                       } catch (IOException var30) {
                                          logger.log(
                                             Level.WARNING,
                                             "Error when setting " + this.watcher.getName() + " as mayor: " + var30.getMessage(),
                                             (Throwable)var30
                                          );
                                          this.windowowner
                                             .getCommunicator()
                                             .sendSafeServerMessage(
                                                "An error occured when setting " + this.watcher.getName() + " as mayor. Please contact administration."
                                             );
                                          this.watcher
                                             .getCommunicator()
                                             .sendSafeServerMessage("An error occured when setting you as mayor. Please contact administration.");
                                       }
                                    }

                                    try {
                                       village.setMayor(this.watcher.getName());
                                    } catch (IOException var29) {
                                       logger.log(
                                          Level.WARNING,
                                          this.watcher.getName() + ", " + this.windowowner.getName() + ":" + var29.getMessage(),
                                          (Throwable)var29
                                       );
                                    }
                                 }
                              } catch (NoSuchVillageException var34) {
                                 logger.log(
                                    Level.WARNING,
                                    "Weird. No village with id " + data + " when " + this.windowowner.getName() + " sold deed with id " + lIt.getWurmId()
                                 );
                              } catch (NoSuchRoleException var35) {
                                 logger.log(
                                    Level.WARNING, "Error when setting " + this.watcher.getName() + " as mayor: " + var35.getMessage(), (Throwable)var35
                                 );
                                 this.windowowner
                                    .getCommunicator()
                                    .sendSafeServerMessage(
                                       "An error occured when setting " + this.watcher.getName() + " as mayor. Please contact administration."
                                    );
                                 this.watcher
                                    .getCommunicator()
                                    .sendSafeServerMessage("An error occured when setting you as mayor. Please contact administration.");
                              }
                           }
                        }
                     }
                  } else {
                     if (lIt.isVillageDeed() || lIt.isHomesteadDeed()) {
                        int data = lIt.getData2();
                        if (data > 0) {
                           try {
                              Village village = Villages.getVillage(data);
                              Citizen oldMayor = village.getCitizen(this.windowowner.getWurmId());
                              if (oldMayor != null) {
                                 try {
                                    oldMayor.setRole(village.getRoleForStatus((byte)3));
                                 } catch (IOException var24) {
                                    logger.log(
                                       Level.WARNING,
                                       "Error when removing " + this.windowowner.getName() + " as mayor: " + var24.getMessage(),
                                       (Throwable)var24
                                    );
                                    this.watcher
                                       .getCommunicator()
                                       .sendSafeServerMessage(
                                          "An error occured when removing " + this.windowowner.getName() + " as mayor. Please contact administration."
                                       );
                                    this.windowowner
                                       .getCommunicator()
                                       .sendSafeServerMessage("An error occured when removing you as mayor. Please contact administration.");
                                 } catch (NoSuchRoleException var25) {
                                    logger.log(
                                       Level.WARNING,
                                       "Error when removing " + this.windowowner.getName() + " as mayor: " + var25.getMessage(),
                                       (Throwable)var25
                                    );
                                    this.watcher
                                       .getCommunicator()
                                       .sendSafeServerMessage(
                                          "An error occured when removing " + this.windowowner.getName() + " as mayor. Please contact administration."
                                       );
                                    this.windowowner
                                       .getCommunicator()
                                       .sendSafeServerMessage("An error occured when removing you as mayor. Please contact administration.");
                                 }
                              }
                           } catch (NoSuchVillageException var26) {
                              logger.log(
                                 Level.WARNING,
                                 "Weird. No village with id " + data + " when " + this.windowowner.getName() + " sold deed with id " + lIt.getWurmId()
                              );
                           }
                        }
                     }

                     if (this.windowowner.isLogged()) {
                        this.windowowner
                           .getLogger()
                           .log(
                              Level.INFO,
                              this.windowowner.getName() + " selling " + lIt.getName() + " with id " + lIt.getWurmId() + " to " + this.watcher.getName()
                           );
                     }
                  }

                  if (lIt.getTemplateId() == 166 && this.watcher.getPower() == 0) {
                     try {
                        Structure s = Structures.getStructureForWrit(item.getWurmId());
                        if (s != null && MissionTargets.destroyStructureTargets(s.getWurmId(), this.watcher.getName())) {
                           this.watcher.getCommunicator().sendAlertServerMessage("A mission trigger was removed for " + s.getName() + ".");
                        }
                     } catch (NoSuchStructureException var23) {
                     }
                  }
               }

               if (ok) {
                  try {
                     Item parent = Items.getItem(parentId);
                     parent.dropItem(item.getWurmId(), false);
                  } catch (NoSuchItemException var36) {
                     if (!coin) {
                        logger.log(Level.WARNING, "Parent not found for item " + lIt.getWurmId());
                     }
                  }

                  if (!(this.watcher instanceof Player)) {
                     if (coin) {
                        if (shop != null) {
                           if (shop.isPersonal()) {
                              getLogger(shop.getWurmId())
                                 .log(
                                    Level.INFO,
                                    this.watcher.getName()
                                       + " received "
                                       + MaterialUtilities.getMaterialString(lIt.getMaterial())
                                       + " "
                                       + lIt.getName()
                                       + ", id: "
                                       + lIt.getWurmId()
                                       + ", QL: "
                                       + lIt.getQualityLevel()
                                 );
                              if (this.windowowner.getWurmId() == shop.getOwnerId()) {
                                 inventory.insertItem(lIt);
                                 moneyAdded += Economy.getValueFor(lIt.getTemplateId());
                              } else {
                                 Economy.getEconomy().returnCoin(lIt, "PersonalShop");
                              }
                           } else {
                              Economy.getEconomy().returnCoin(lIt, "TraderShop");
                              long v = (long)Economy.getValueFor(lIt.getTemplateId());
                              moneyAdded = (int)((long)moneyAdded + v);
                           }
                        } else {
                           logger.log(Level.WARNING, this.windowowner.getName() + ", id=" + this.windowowner.getWurmId() + " failed to locate TraderMoney.");
                        }
                     } else {
                        inventory.insertItem(lIt);
                        if (shop != null) {
                           if (!shop.isPersonal()) {
                              lIt.setPrice(0);
                              int deminc = 1;
                              if (lIt.isCombine()) {
                                 deminc = Math.max(1, lIt.getWeightGrams() / lIt.getTemplate().getWeightGrams());
                              }

                              Economy.getEconomy().addItemBoughtByTraders(lIt.getTemplateId());
                              shop.getLocalSupplyDemand().addItemPurchased(lIt.getTemplateId(), (float)deminc);
                              if (lIt.isVillageDeed() || lIt.isHomesteadDeed()) {
                                 Shop kingsMoney = Economy.getEconomy().getKingsShop();
                                 kingsMoney.setMoney(kingsMoney.getMoney() - (long)lIt.getValue());
                                 lIt.setAuxData((byte)0);
                                 logger.log(Level.INFO, "King bought a deed for " + lIt.getValue() + " and now has " + kingsMoney.getMoney());
                                 long v = (long)Economy.getValueFor(lIt.getTemplateId());
                                 moneyLost = (int)((long)moneyLost - v);
                              }
                           } else {
                              getLogger(shop.getWurmId())
                                 .log(
                                    Level.INFO,
                                    this.watcher.getName()
                                       + " received "
                                       + MaterialUtilities.getMaterialString(lIt.getMaterial())
                                       + " "
                                       + lIt.getName()
                                       + ", id: "
                                       + lIt.getWurmId()
                                       + ", QL: "
                                       + lIt.getQualityLevel()
                                 );
                           }
                        }
                     }
                  } else {
                     inventory.insertItem(lIt);
                     if (coin && shop != null) {
                        if (!shop.isPersonal() || shop.getOwnerId() == this.watcher.getWurmId()) {
                           long v = (long)Economy.getValueFor(lIt.getTemplateId());
                           moneyLost = (int)((long)moneyLost + v);
                        }

                        if (shop.getOwnerId() == this.watcher.getWurmId()) {
                           getLogger(shop.getWurmId())
                              .log(
                                 Level.INFO,
                                 this.watcher.getName()
                                    + " received "
                                    + MaterialUtilities.getMaterialString(lIt.getMaterial())
                                    + " "
                                    + lIt.getName()
                                    + ", id: "
                                    + lIt.getWurmId()
                                    + ", QL: "
                                    + lIt.getQualityLevel()
                              );
                        }
                     } else if (shop != null) {
                        if (!shop.isPersonal()) {
                           int deminc = 1;
                           if (lIt.isCombine()) {
                              deminc = Math.max(1, lIt.getWeightGrams() / lIt.getTemplate().getWeightGrams());
                           }

                           Economy.getEconomy().addItemSoldByTraders(lIt.getTemplateId());
                           shop.getLocalSupplyDemand().addItemSold(lIt.getTemplateId(), (float)deminc);
                        } else {
                           getLogger(shop.getWurmId())
                              .log(
                                 Level.INFO,
                                 this.watcher.getName()
                                    + " received "
                                    + MaterialUtilities.getMaterialString(lIt.getMaterial())
                                    + " "
                                    + lIt.getName()
                                    + ", id: "
                                    + lIt.getWurmId()
                                    + ", QL: "
                                    + lIt.getQualityLevel()
                              );
                        }
                     }
                  }
               }

               if (!(this.windowowner instanceof Player) && !coin && ok && !lIt.isPurchased() && !shop.isPersonal()) {
                  try {
                     if (this.windowowner.getCarriedItem(item.getTemplateId()) == null) {
                        byte material = item.getMaterial();
                        if (item.isFullprice() || item.isNoSellback()) {
                           material = item.getTemplate().getMaterial();
                        }

                        Item newItem = ItemFactory.createItem(item.getTemplateId(), item.getQualityLevel(), material, (byte)0, null);
                        ownInventory.insertItem(newItem);
                     }

                     if (item.isVillageDeed() || item.isHomesteadDeed()) {
                        Shop kingsMoney = Economy.getEconomy().getKingsShop();
                        kingsMoney.setMoney(kingsMoney.getMoney() + (long)(item.getValue() / 2));
                        item.setLeftAuxData(0);
                        Economy.getEconomy()
                           .addItemSoldByTraders(
                              item.getName(), (long)item.getValue(), this.windowowner.getName(), this.watcher.getName(), item.getTemplateId()
                           );
                        moneyAdded -= item.getValue();
                     }

                     if (item.isNoSellback() || item.getTemplateId() == 682) {
                        Shop kingsMoney = Economy.getEconomy().getKingsShop();
                        kingsMoney.setMoney(kingsMoney.getMoney() + (long)(item.getValue() / 4));
                        Economy.getEconomy()
                           .addItemSoldByTraders(
                              item.getName(), (long)item.getValue(), this.windowowner.getName(), this.watcher.getName(), item.getTemplateId()
                           );
                        moneyAdded -= item.getValue() * 3 / 4;
                     }

                     if (item.getTemplateId() == 300 || item.getTemplateId() == 299) {
                        Shop kingsMoney = Economy.getEconomy().getKingsShop();
                        kingsMoney.setMoney(kingsMoney.getMoney() + (long)(item.getValue() / 4));
                        Economy.getEconomy()
                           .addItemSoldByTraders(
                              item.getName(), (long)item.getValue(), this.windowowner.getName(), this.watcher.getName(), item.getTemplateId()
                           );
                        moneyAdded -= item.getValue() * 3 / 4;
                     } else if (item.getTemplateId() == 1129) {
                        Shop kingsMoney = Economy.getEconomy().getKingsShop();
                        kingsMoney.setMoney(kingsMoney.getMoney() + (long)(item.getValue() / 2));
                        Economy.getEconomy()
                           .addItemSoldByTraders(
                              item.getName(), (long)item.getValue(), this.windowowner.getName(), this.watcher.getName(), item.getTemplateId()
                           );
                        moneyAdded -= item.getValue();
                     }
                  } catch (NoSuchTemplateException var37) {
                     logger.log(Level.WARNING, var37.getMessage(), (Throwable)var37);
                  } catch (FailedException var38) {
                     logger.log(Level.WARNING, var38.getMessage(), (Throwable)var38);
                  }
               }

               if (!ok) {
                  errors = true;
               }
            }
         }

         if (!errors) {
            this.windowowner.getCommunicator().sendNormalServerMessage("The trade was completed successfully.");
         } else {
            this.windowowner.getCommunicator().sendNormalServerMessage("The trade was completed, not all items were traded.");
         }

         if (shop != null) {
            int diff = moneyAdded - moneyLost;
            if (shop.isPersonal()) {
               if (diff != 0) {
                  shop.setMoney(shop.getMoney() + (long)diff);
               }

               long moneyToAdd = (long)((float)this.trade.getMoneyAdded() * 0.9F);
               long kadd = this.trade.getMoneyAdded() - moneyToAdd;
               if (moneyToAdd != 0L) {
                  if (this.windowowner.isNpcTrader()) {
                     Item[] c = Economy.getEconomy().getCoinsFor(moneyToAdd);

                     for(Item lElement : c) {
                        ownInventory.insertItem(lElement, true);
                     }

                     shop.setMoney(shop.getMoney() + moneyToAdd);
                     if (this.watcher.getWurmId() != shop.getOwnerId()) {
                        if (kadd != 0L) {
                           Shop kingsMoney = Economy.getEconomy().getKingsShop();
                           kingsMoney.setMoney(kingsMoney.getMoney() + kadd);
                           shop.addMoneySpent(kadd);
                        }

                        shop.addMoneyEarned(moneyToAdd);
                     }
                  }

                  shop.setLastPolled(System.currentTimeMillis());
               }
            } else {
               if (diff >= 1000000) {
                  this.watcher.achievement(132);
               }

               this.trade.addShopDiff((long)diff);
            }
         }
      } else {
         this.windowowner.getCommunicator().sendAlertServerMessage("There is a bug in the trade system. This shouldn't happen. Please report.");
         this.watcher.getCommunicator().sendAlertServerMessage("There is a bug in the trade system. This shouldn't happen. Please report.");
         logger.log(
            Level.WARNING,
            "Inconsistency! This is offer window number " + this.wurmId + ". Traders are " + this.watcher.getName() + ", " + this.windowowner.getName()
         );
      }
   }

   void endTrade() {
      if (this.items != null) {
         Item[] its = this.items.toArray(new Item[this.items.size()]);

         for(Item lIt : its) {
            this.removeExistingContainedItems(lIt);
            this.items.remove(lIt);
            this.removeFromTrade(lIt, true);
         }
      }

      this.items = null;
   }
}
