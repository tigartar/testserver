package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.util.MaterialUtilities;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WagonerSetupDeliveryQuestion extends Question implements MonetaryConstants {
   private static final Logger logger = Logger.getLogger(WagonerSetupDeliveryQuestion.class.getName());
   private static final String red = "color=\"255,127,127\"";
   private static final DecimalFormat df = new DecimalFormat("#0.00");
   private final Item container;
   private int sortBy = 1;
   private int pageNo = 1;
   private long wagonerId = -10L;
   private long receiverId = -10L;
   private String receiverName = "";
   private String error = "";
   private int crates = 0;

   public WagonerSetupDeliveryQuestion(Creature aResponder, Item container) {
      super(aResponder, getTitle(1), getTitle(1), 145, -10L);
      this.container = container;
      container.setIsSealedOverride(true);
   }

   public WagonerSetupDeliveryQuestion(
      Creature aResponder, Item container, int sortBy, int pageNo, long wagonerId, long receiverId, String receiverName, String error
   ) {
      super(aResponder, getTitle(pageNo), getTitle(pageNo), 145, -10L);
      this.container = container;
      this.sortBy = sortBy;
      this.pageNo = pageNo;
      this.wagonerId = wagonerId;
      this.receiverId = receiverId;
      this.receiverName = receiverName;
      this.error = error;
   }

   private static final String getTitle(int pageNo) {
      return "Set up Delivery page " + pageNo + " of 2";
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      boolean cancel = this.getBooleanProp("cancel");
      if (cancel) {
         this.getResponder().getCommunicator().sendNormalServerMessage("Set up Delivery Cancelled!");
         this.container.setIsSealedOverride(false);
      } else {
         switch(this.pageNo) {
            case 1:
               boolean next = this.getBooleanProp("next");
               if (next) {
                  this.error = "";
                  String sel = aAnswer.getProperty("sel");
                  long selId = Long.parseLong(sel);
                  if (selId == -10L) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You decide to do nothing.");
                     this.container.setIsSealedOverride(false);
                     return;
                  }

                  Wagoner wagoner = Wagoner.getWagoner(selId);
                  if (wagoner == null) {
                     this.wagonerId = -10L;
                     this.error = "Wagoner has vanished!";
                  } else {
                     this.wagonerId = wagoner.getWurmId();
                  }

                  String who = aAnswer.getProperty("playername");
                  if (who == null || who.length() <= 2) {
                     this.error = "Player name was too short!";
                  } else if (LoginHandler.containsIllegalCharacters(who)) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("The name of the receiver contains illegal characters. Please check the name.");
                     this.error = "Player " + who + " contains illegal characters!";
                  }

                  if (this.error.length() == 0) {
                     this.receiverName = LoginHandler.raiseFirstLetter(who);
                     PlayerState ps = PlayerInfoFactory.getPlayerState(this.receiverName);
                     if (ps == null) {
                        this.error = "Player " + this.receiverName + " not found!";
                     } else if (ps.getServerId() != Servers.getLocalServerId()) {
                        this.error = "Player " + this.receiverName + " is not on this server!";
                     } else {
                        this.receiverId = ps.getPlayerId();
                     }
                  }

                  if (this.receiverId == -10L && this.error.length() == 0) {
                     this.error = "Player " + who + " not found!";
                  }

                  if (this.error.length() == 0) {
                     this.pageNo = 2;
                  }
               } else {
                  for(String key : this.getAnswer().stringPropertyNames()) {
                     if (key.startsWith("sort")) {
                        String sid = key.substring(4);
                        this.sortBy = Integer.parseInt(sid);
                        break;
                     }
                  }
               }

               WagonerSetupDeliveryQuestion wdq = new WagonerSetupDeliveryQuestion(
                  this.getResponder(), this.container, this.sortBy, this.pageNo, this.wagonerId, this.receiverId, this.receiverName, this.error
               );
               switch(this.pageNo) {
                  case 1:
                     wdq.sendQuestion();
                     break;
                  case 2:
                     wdq.sendQuestion2();
               }

               return;
            case 2:
               int codprice = 0;
               String val = aAnswer.getProperty("g");
               if (val != null && val.length() > 0) {
                  try {
                     codprice = Integer.parseInt(val) * 1000000;
                  } catch (NumberFormatException var14) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("Failed to set the gold price for delivery. Note that a coin value is in whole numbers, no decimals.");
                  }
               }

               val = aAnswer.getProperty("s");
               if (val != null && val.length() > 0) {
                  try {
                     codprice += Integer.parseInt(val) * 10000;
                  } catch (NumberFormatException var13) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("Failed to set a silver price for delivery. Note that a coin value is in whole numbers, no decimals.");
                  }
               }

               val = aAnswer.getProperty("c");
               if (val != null && val.length() > 0) {
                  try {
                     codprice += Integer.parseInt(val) * 100;
                  } catch (NumberFormatException var12) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("Failed to set a copper price for delivery. Note that a coin value is in whole numbers, no decimals.");
                  }
               }

               val = aAnswer.getProperty("i");
               if (val != null && val.length() > 0) {
                  try {
                     codprice += Integer.parseInt(val);
                  } catch (NumberFormatException var11) {
                     this.getResponder()
                        .getCommunicator()
                        .sendNormalServerMessage("Failed to set an iron price for delivery. Note that a coin value is in whole numbers, no decimals.");
                  }
               }

               int senderCost = 0;
               String sel = aAnswer.getProperty("fee");
               int selId = Integer.parseInt(sel);
               if (selId == 0) {
                  senderCost += this.crates * 100;
                  long money = this.getResponder().getMoney();
                  if (money < (long)senderCost) {
                     this.getResponder().getCommunicator().sendNormalServerMessage("You cannot afford to pay for the delivery fee, so its been cancelled.");
                     this.container.setIsSealedOverride(false);
                     return;
                  }
               } else {
                  codprice += this.crates * 100;
               }

               boolean failed = false;
               if (senderCost > 0) {
                  try {
                     if (this.getResponder().chargeMoney((long)senderCost)) {
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("You have been charged " + new Change((long)senderCost).getChangeString() + ".");
                        Change change = Economy.getEconomy().getChangeFor(this.getResponder().getMoney());
                        this.getResponder().getCommunicator().sendNormalServerMessage("You now have " + change.getChangeString() + " in the bank.");
                        this.getResponder()
                           .getCommunicator()
                           .sendNormalServerMessage("If this amount is incorrect, please wait a while since the information may not immediately be updated.");
                     } else {
                        failed = true;
                     }
                  } catch (IOException var10) {
                     failed = true;
                     logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
                  }
               }

               if (failed) {
                  this.getResponder().getCommunicator().sendNormalServerMessage("Something went wrong, delivery set up cancelled.");
                  this.container.setIsSealedOverride(false);
                  return;
               } else {
                  this.container.setIsSealedByPlayer(true);
                  Delivery.addDelivery(
                     this.container.getData(),
                     this.container.getWurmId(),
                     this.crates,
                     this.getResponder().getWurmId(),
                     (long)senderCost,
                     this.receiverId,
                     (long)codprice,
                     this.wagonerId
                  );
                  this.getResponder()
                     .getCommunicator()
                     .sendNormalServerMessage(
                        "You have set up a delivery to " + this.receiverName + " for " + new Change((long)codprice).getChangeString() + "."
                     );
               }
         }
      }
   }

   @Override
   public void sendQuestion() {
      int height = 300;
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("closebutton{id=\"cancel\"};");
      buf.append("text{type=\"bold\";color=\"255,127,127\"text=\"" + this.error + "\"}");
      Set<Creature> creatureSet = Creatures.getMayUseWagonersFor(this.getResponder());
      long endWaystoneId = this.container.getData();
      Item endWaystone = null;

      try {
         endWaystone = Items.getItem(endWaystoneId);
      } catch (NoSuchItemException var18) {
      }

      Set<WagonerSetupDeliveryQuestion.Distanced> wagonerSet = new HashSet<>();

      for(Creature creature : creatureSet) {
         Wagoner wagoner = Wagoner.getWagoner(creature.getWurmId());
         if (wagoner != null) {
            float dist = PathToCalculate.getRouteDistance(wagoner.getHomeWaystoneId(), endWaystoneId);
            if (dist != 99999.0F) {
               boolean isPublic = creature.publicMayUse(this.getResponder());
               String villName = "";
               Village vill = creature.getCitizenVillage();
               if (vill != null) {
                  villName = vill.getName();
               }

               wagonerSet.add(new WagonerSetupDeliveryQuestion.Distanced(wagoner, isPublic, villName, dist));
            }
         }
      }

      WagonerSetupDeliveryQuestion.Distanced[] wagonerArr = wagonerSet.toArray(new WagonerSetupDeliveryQuestion.Distanced[wagonerSet.size()]);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      switch(absSortBy) {
         case 1:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getWagoner().getName().compareTo(param2.getWagoner().getName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getType().compareTo(param2.getType()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getVillageName().compareTo(param2.getVillageName()) * upDown;
               }
            });
            break;
         case 4:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getDistance() < param2.getDistance() ? 1 * upDown : -1 * upDown;
               }
            });
            break;
         case 5:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getWagoner().getStateName().compareTo(param2.getWagoner().getStateName()) * upDown;
               }
            });
            break;
         case 6:
            Arrays.sort(wagonerArr, new Comparator<WagonerSetupDeliveryQuestion.Distanced>() {
               public int compare(WagonerSetupDeliveryQuestion.Distanced param1, WagonerSetupDeliveryQuestion.Distanced param2) {
                  return param1.getQueueLength() < param2.getQueueLength() ? 1 * upDown : -1 * upDown;
               }
            });
      }

      buf.append("label{text=\"Select which wagoner to use \"};");
      buf.append(
         "table{rows=\"1\";cols=\"8\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Type", 2, this.sortBy)
            + this.colHeader("Village", 3, this.sortBy)
            + this.colHeader("Distance", 4, this.sortBy)
            + this.colHeader("State", 5, this.sortBy)
            + this.colHeader("Queue", 6, this.sortBy)
            + "label{type=\"bold\";text=\"\"};"
      );
      String noneSelected = "selected=\"true\";";

      for(WagonerSetupDeliveryQuestion.Distanced distanced : wagonerArr) {
         String selected = "";
         if (this.wagonerId == distanced.getWagoner().getWurmId()) {
            selected = "selected=\"true\";";
            noneSelected = "";
         }

         buf.append(
            distanced.getVillageName().length() == 0
               ? "label{text=\"\"}"
               : "radio{group=\"sel\";id=\""
                  + distanced.getWagoner().getWurmId()
                  + "\";"
                  + selected
                  + "text=\"\"}label{text=\""
                  + distanced.getWagoner().getName()
                  + "\"};label{text=\""
                  + distanced.getType()
                  + "\"};label{text=\""
                  + distanced.getVillageName()
                  + "\"};label{text=\""
                  + (int)distanced.getDistance()
                  + "\"};label{text=\""
                  + distanced.getWagoner().getStateName()
                  + "\"};label{text=\""
                  + distanced.getQueueLength()
                  + "\"};label{text=\"\"}"
         );
      }

      buf.append("}");
      buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
      if (endWaystone == null) {
         buf.append("text{text=\"Could not find the associated waystone for this wagoner container.\"}");
      }

      buf.append("text{text=\"\"}");
      boolean ownsAll = true;
      Item[] crates = this.container.getItemsAsArray();

      for(Item crate : crates) {
         if (crate.getLastOwnerId() != this.getResponder().getWurmId()) {
            ownsAll = false;
         }
      }

      if (!ownsAll) {
         buf.append("text{text=\"You cannot set up a delivery on this container as you did not load all the crates.\"}");
         buf.append("harray{button{text=\"Close\";id=\"cancel\"}}");
      } else {
         buf.append("text{text=\"Specify who to deliver the contents of this container to.\"}");
         buf.append("harray{label{text=\"Deliver to \"}input{maxchars=\"40\";id=\"playername\";text=\"" + this.receiverName + "\"}}");
         buf.append("text{text=\"\"}");
         buf.append("text{text=\"They will be informed and will have to accept it before the delivery can start.\"}");
         buf.append("text{text=\"\"}");
         buf.append("harray{label{text=\"Continue to \"};button{text=\"Next\";id=\"next\"}label{text=\" screen to add costs.\"};}");
      }

      buf.append("}};null;null;}");
      height += wagonerArr.length * 20;
      this.getResponder().getCommunicator().sendBml(420, height, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public void sendQuestion2() {
      StringBuilder buf = new StringBuilder();
      String header = "border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + this.getId() + "'}";
      buf.append(header);
      buf.append("closebutton{id=\"cancel\"};");
      Wagoner wagoner = Wagoner.getWagoner(this.wagonerId);
      buf.append("text{text=\"You have selected " + wagoner.getName() + " to perform the delivery.\"}");
      buf.append("text{text=\"They will be delivering to " + this.receiverName + ".\"}");
      long money = this.getResponder().getMoney();
      if (money <= 0L) {
         buf.append("text{text='You have no money in the bank.'}");
      } else {
         buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
      }

      buf.append("}};null;");
      buf.append(
         "tree{id=\"t1\";cols=\"3\";showheader=\"true\";height=\"300\"col{text=\"QL\";width=\"50\"};col{text=\"DMG\";width=\"50\"};col{text=\"Weight\";width=\"50\"};"
      );
      Item[] crates = this.container.getItemsAsArray();

      for(Item crate : crates) {
         buf.append(this.addCrate(crate));
      }

      buf.append("}");
      buf.append("null;varray{");
      this.crates = crates.length;
      buf.append("label{text=\"" + wagoner.getName() + " charges a delivery fee of 1C per crate. So a total of " + this.crates + "C.\"}");
      buf.append(
         "harray{label{text=\"These fees will be paid for by:\"}radio{group=\"fee\";id=\"0\";text=\"You \"}radio{group=\"fee\";id=\"1\";selected=\"true\";text=\""
            + this.receiverName
            + ".\"}}"
      );
      buf.append("harray{label{text=\"You are charging \"};");
      buf.append(
         "table{rows=\"1\"; cols=\"8\";label{text=\"G:\"};input{maxchars=\"2\"; id=\"g\";text=\"0\"};label{text=\"S:\"};input{maxchars=\"2\"; id=\"s\";text=\"0\"};label{text=\"C:\"};input{maxchars=\"2\"; id=\"c\";text=\"0\"};label{text=\"I:\"};input{maxchars=\"2\"; id=\"i\";text=\"0\"};}"
      );
      buf.append("label{text=\" for the goods:\"}}");
      buf.append("harray{button{text=\"Add to " + wagoner.getName() + "'s queue\";id=\"queue\"};}");
      buf.append("text=\"\"}}");
      this.getResponder().getCommunicator().sendBml(450, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String addCrate(Item crate) {
      StringBuilder buf = new StringBuilder();
      String sQL = "" + df.format((double)crate.getQualityLevel());
      String sDMG = "" + df.format((double)crate.getDamage());
      String sWeight = "" + df.format((double)((float)crate.getFullWeight(true) / 1000.0F));
      String itemName = longItemName(crate);
      Item[] contained = crate.getItemsAsArray();
      int children = contained.length;
      buf.append(
         "row{id=\""
            + this.id
            + "\";hover=\""
            + itemName
            + "\";name=\""
            + itemName
            + "\";rarity=\""
            + crate.getRarity()
            + "\";children=\""
            + children
            + "\";col{text=\""
            + sQL
            + "\"};col{text=\""
            + sDMG
            + "\"};col{text=\""
            + sWeight
            + "\"}}"
      );

      for(Item bulkItem : contained) {
         buf.append(this.addBulkItem(bulkItem));
      }

      return buf.toString();
   }

   private String addBulkItem(Item bulkItem) {
      StringBuilder buf = new StringBuilder();
      String sQL = "" + df.format((double)bulkItem.getQualityLevel());
      String sWeight = "" + df.format((double)((float)bulkItem.getFullWeight(true) / 1000.0F));
      String itemName = longItemName(bulkItem);
      buf.append(
         "row{id=\""
            + this.id
            + "\";hover=\""
            + itemName
            + "\";name=\""
            + itemName
            + "\";rarity=\"0\";children=\"0\";col{text=\""
            + sQL
            + "\"};col{text=\"0.00\"};col{text=\""
            + sWeight
            + "\"}}"
      );
      return buf.toString();
   }

   public static String longItemName(Item litem) {
      StringBuilder sb = new StringBuilder();
      if (litem.getRarity() == 1) {
         sb.append("rare ");
      } else if (litem.getRarity() == 2) {
         sb.append("supreme ");
      } else if (litem.getRarity() == 3) {
         sb.append("fantastic ");
      }

      String name = litem.getName().length() == 0 ? litem.getTemplate().getName() : litem.getName();
      MaterialUtilities.appendNameWithMaterialSuffix(sb, name.replace("\"", "''"), litem.getMaterial());
      if (litem.getDescription().length() > 0) {
         sb.append(" (" + litem.getDescription() + ")");
      }

      return sb.toString();
   }

   @Override
   public void timedOut() {
      this.container.setIsSealedOverride(false);
   }

   class Distanced {
      private final Wagoner wagoner;
      private final boolean isPublic;
      private final String villageName;
      private final float distance;
      private final int queueLength;

      Distanced(Wagoner wagoner, boolean isPublic, String villageName, float distance) {
         this.wagoner = wagoner;
         this.isPublic = isPublic;
         this.villageName = villageName;
         this.distance = distance;
         this.queueLength = Delivery.getQueueLength(wagoner.getWurmId());
      }

      Wagoner getWagoner() {
         return this.wagoner;
      }

      float getDistance() {
         return this.distance;
      }

      boolean isPublic() {
         return this.isPublic;
      }

      String getType() {
         return this.isPublic ? "Public" : "Private";
      }

      String getVillageName() {
         return this.villageName;
      }

      int getQueueLength() {
         return this.queueLength;
      }
   }
}
