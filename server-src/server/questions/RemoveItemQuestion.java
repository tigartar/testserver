package com.wurmonline.server.questions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.utils.StringUtil;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveItemQuestion extends Question {
   private static final Logger logger = Logger.getLogger(RemoveItemQuestion.class.getName());
   private static final int MAXNUMS = Integer.MAX_VALUE;
   private long moveTarget = 0L;

   public RemoveItemQuestion(Creature aResponder, long aTarget) {
      super(aResponder, "Removing items", "How many items do you wish to remove?", 84, aTarget);
   }

   public RemoveItemQuestion(Creature aResponder, long aTarget, long aMoveTarget) {
      super(aResponder, "Removing items", "How many items do you wish to remove?", 84, aTarget);
      this.moveTarget = aMoveTarget;
   }

   @Override
   public void answer(Properties aAnswers) {
      String numstext = aAnswers.getProperty("numstext");
      String nums = aAnswers.getProperty("items");
      if (numstext != null && numstext.length() > 0) {
         nums = numstext;
      }

      if (nums != null && nums.length() > 0) {
         if (nums.equals(String.valueOf(Integer.MAX_VALUE))) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You selected max.");
         } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You selected " + nums + ".");
         }

         try {
            int i = Integer.parseInt(nums);
            if (i > 0) {
               try {
                  Item bulkitem = Items.getItem(this.target);
                  long topParentId = bulkitem.getTopParent();
                  Item topParent = Items.getItem(topParentId);
                  float maxDist = 4.0F;
                  if (topParent.isVehicle()) {
                     Vehicle vehicle = Vehicles.getVehicle(topParent);
                     if (vehicle != null) {
                        maxDist = Math.max(maxDist, (float)vehicle.getMaxAllowedLoadDistance());
                     }
                  }

                  if (!this.getResponder().isWithinDistanceTo(topParent.getPosX(), topParent.getPosY(), topParent.getPosZ(), maxDist)
                     && bulkitem.getTopParent() != this.getResponder().getVehicle()) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You are too far away from the " + bulkitem.getName() + " now.");
                     return;
                  }

                  boolean full = false;
                  Item parent = null;

                  try {
                     parent = bulkitem.getParent();
                     full = parent.isFull();
                  } catch (NoSuchItemException var34) {
                  }

                  boolean max = i == Integer.MAX_VALUE;
                  int bnums = 0;
                  if (bulkitem.getRealTemplate() != null && bulkitem.getRealTemplate().isCombine()) {
                     bnums = (int)Math.ceil((double)bulkitem.getBulkNumsFloat(false));
                  } else {
                     bnums = bulkitem.getBulkNums();
                  }

                  Item toInsert = null;
                  int current = this.getResponder().getInventory().getNumItemsNotCoins();
                  int maxCapac = Math.max(0, 100 - current);
                  if (i > maxCapac) {
                     i = Math.min(bnums, maxCapac);
                  }

                  if (bnums < i) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("The " + bulkitem.getName() + " does not contain " + i + " items.");
                     return;
                  }

                  int weightReduced = 0;
                  ItemTemplate template = bulkitem.getRealTemplate();
                  if (template != null) {
                     int volume = template.getVolume();
                     int tweight = template.getWeightGrams();
                     Item targetInventory = null;

                     try {
                        if (this.moveTarget != 0L) {
                           targetInventory = Items.getItem(this.moveTarget);
                        }
                     } catch (NoSuchItemException var39) {
                        String message = StringUtil.format("Unable to find item: %d.", this.moveTarget);
                        logger.log(Level.WARNING, message, (Throwable)var39);
                        return;
                     }

                     if (template.isFish() && template.getTemplateId() != 369) {
                        double ql = (double)(bulkitem.getCurrentQualityLevel() / 100.0F);
                        tweight = (int)((double)tweight * ql);
                     }

                     if (max) {
                        i = Math.min(maxCapac, this.getResponder().getCarryCapacityFor(tweight));
                        if (i <= 0) {
                           this.getResponder().getCommunicator().sendNormalServerMessage("You can not even carry one of those.");
                           return;
                        }

                        i = Math.min(i, bnums);
                     } else if (!this.getResponder().canCarry(tweight * i)
                        && (targetInventory == null || !targetInventory.isBulkContainer())
                        && (i > 1 || bulkitem.getWeightGrams() >= volume)) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You may not carry that weight.");
                        return;
                     }

                     if (targetInventory.isContainerLiquid()
                        && (
                           targetInventory.getSizeX() < template.getSizeX()
                              || targetInventory.getSizeY() < template.getSizeY()
                              || targetInventory.getSizeZ() < template.getSizeZ()
                        )) {
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("The " + template.getName() + " will not fit inside the " + targetInventory.getName() + ".");
                        return;
                     }

                     byte auxdata = bulkitem.getAuxData();
                     int toMake = bulkitem.getRealTemplateId();
                     String aName = bulkitem.getActualName();
                     if (toMake == 129 && auxdata == 0) {
                        toMake = 92;
                        aName = "meat";
                     }

                     if (Features.Feature.MOVE_BULK_TO_BULK.isEnabled() && targetInventory.isBulkContainer() && this.moveTarget > 0L) {
                        try {
                           BehaviourDispatcher.action(this.getResponder(), this.getResponder().getCommunicator(), this.target, this.moveTarget, (short)914);
                           Action act = ((Player)this.getResponder()).getActions().getLastSlowAction();
                           if (act != null) {
                              act.setData((long)Integer.parseInt(nums));
                           } else {
                              this.getResponder().getCommunicator().sendAlertServerMessage("ERROR: Action was null, could not set amount!");
                           }
                        } catch (NoSuchPlayerException var29) {
                           logger.fine("No such player Ex");
                        } catch (NoSuchCreatureException var30) {
                           logger.fine("No such creature Ex");
                        } catch (NoSuchBehaviourException var31) {
                           logger.fine("No such behaviour Ex :(");
                        } catch (NoSuchWallException var32) {
                           logger.fine("no such wall ex");
                        } catch (FailedException var33) {
                           logger.fine("Failed EX?");
                        }

                        return;
                     }

                     for(int created = 0; created < i; ++created) {
                        try {
                           int weight = bulkitem.getWeightGrams() - weightReduced;
                           float percent = 1.0F;
                           if (weight < volume) {
                              percent = (float)weight / (float)volume;
                           } else {
                              weight = Math.min(bulkitem.getWeightGrams(), volume);
                           }

                           if (weight > 0) {
                              toInsert = ItemFactory.createItem(toMake, bulkitem.getCurrentQualityLevel(), bulkitem.getMaterial(), (byte)0, null);
                              if (!toInsert.isFish()) {
                                 toInsert.setCreator(this.getResponder().getName());
                              }

                              toInsert.setLastOwnerId(this.getResponder().getWurmId());
                              if (toInsert.isRepairable()) {
                                 toInsert.setCreationState((byte)0);
                              }

                              if (toInsert.usesFoodState()) {
                                 toInsert.setAuxData(auxdata);
                                 ItemMealData imd = ItemMealData.getItemMealData(bulkitem.getWurmId());
                                 if (imd != null) {
                                    ItemMealData.save(
                                       toInsert.getWurmId(),
                                       imd.getRecipeId(),
                                       imd.getCalories(),
                                       imd.getCarbs(),
                                       imd.getFats(),
                                       imd.getProteins(),
                                       imd.getBonus(),
                                       imd.getStages(),
                                       imd.getIngredients()
                                    );
                                 }
                              }

                              if (template.isFish() && template.getTemplateId() != 369) {
                                 toInsert.setSizes(tweight);
                                 toInsert.setWeight(tweight, true);
                              } else {
                                 toInsert.setWeight((int)(percent * (float)template.getWeightGrams()), true);
                              }

                              if (bulkitem.getData1() != -1) {
                                 toInsert.setRealTemplate(bulkitem.getData1());
                              }

                              if (!bulkitem.getActualName().equalsIgnoreCase("bulk item")) {
                                 toInsert.setName(aName);
                              }

                              if (this.moveTarget == 0L) {
                                 this.getResponder().getInventory().insertItem(toInsert);
                              } else if (!targetInventory.isBulkContainer()) {
                                 if (!targetInventory.testInsertItem(toInsert) || !targetInventory.mayCreatureInsertItem()) {
                                    String message = "There is not enough space for any more items.";
                                    this.getResponder().getCommunicator().sendNormalServerMessage("There is not enough space for any more items.");
                                    Items.destroyItem(toInsert.getWurmId());
                                    break;
                                 }

                                 targetInventory.insertItem(toInsert);
                              } else {
                                 try {
                                    if ((targetInventory.isCrate() || !targetInventory.hasSpaceFor(toInsert.getVolume()))
                                       && (!targetInventory.isCrate() || !targetInventory.canAddToCrate(toInsert))) {
                                       String message = "The %s will not fit in the %s.";
                                       this.getResponder()
                                          .getCommunicator()
                                          .sendNormalServerMessage(
                                             StringUtil.format("The %s will not fit in the %s.", toInsert.getName(), targetInventory.getName())
                                          );
                                       Items.destroyItem(toInsert.getWurmId());
                                       break;
                                    }

                                    if (!toInsert.moveToItem(this.getResponder(), targetInventory.getWurmId(), false)) {
                                       Items.destroyItem(toInsert.getWurmId());
                                       break;
                                    }
                                 } catch (NoSuchPlayerException var35) {
                                 } catch (NoSuchCreatureException var36) {
                                 }
                              }

                              weightReduced += weight;
                           }
                        } catch (NoSuchTemplateException var37) {
                           logger.log(Level.WARNING, var37.getMessage(), (Throwable)var37);
                        } catch (FailedException var38) {
                           logger.log(Level.WARNING, var38.getMessage(), (Throwable)var38);
                        }
                     }

                     this.getResponder().achievement(167, -i);
                     if (!bulkitem.setWeight(bulkitem.getWeightGrams() - weightReduced, true)) {
                     }
                  }

                  if (parent != null && (full != parent.isFull() || parent.isCrate())) {
                     parent.updateModelNameOnGroundItem();
                  }
               } catch (NoSuchItemException var40) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("No such item.");
                  return;
               }
            }
         } catch (NumberFormatException var41) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Not a number.");
         }
      }
   }

   @Override
   public void sendQuestion() {
      Item temp;
      try {
         if (this.moveTarget > 0L) {
            temp = Items.getItem(this.moveTarget);
         } else {
            temp = null;
         }
      } catch (NoSuchItemException var7) {
         temp = null;
      }

      Item moveTargetItem = temp;

      try {
         Item bulkitem = Items.getItem(this.target);
         String carryString = moveTargetItem == null || !moveTargetItem.isBulk() && !moveTargetItem.isBulkContainer() ? "As many as I can carry" : "All items";
         StringBuilder buf = new StringBuilder();
         int nums = bulkitem.getBulkNums();
         buf.append(this.getBmlHeader());
         if (nums > 0) {
            buf.append("text{text=\"How many items do you wish to remove?\"};");
            buf.append("text{text=''}");
            buf.append("input{text='';id='numstext';maxlength='2'};");
            buf.append("text{text=''}");
            buf.append("radio{ group='items'; id='2147483647';selected='true';text='" + carryString + "'}");
            buf.append("radio{ group='items'; id='0';text='None'}");
            if (nums < 100 && nums != 1) {
               buf.append("radio{ group='items'; id='" + nums + "';text='" + nums + "'}");
            }

            buf.append("radio{ group='items'; id='1';text='1'}");
            if (nums > 2 && nums != 2) {
               buf.append("radio{ group='items'; id='2';text='2'}");
            }

            if (nums > 5 && nums != 5) {
               buf.append("radio{ group='items'; id='5';text='5'}");
            }

            if (nums > 10 && nums != 10) {
               buf.append("radio{ group='items'; id='10';text='10'}");
            }

            if (nums > 20 && nums != 20) {
               buf.append("radio{ group='items'; id='20';text='20'}");
            }

            if (nums > 50 && nums != 50) {
               buf.append("radio{ group='items'; id='50';text='50'}");
            }
         } else {
            buf.append("text{text=\"The " + bulkitem.getName() + " is empty.\"}");
         }

         buf.append(this.createAnswerButton2());
         this.getResponder().getCommunicator().sendBml(300, 340, true, true, buf.toString(), 200, 200, 200, this.title);
      } catch (NoSuchItemException var8) {
      }
   }
}
