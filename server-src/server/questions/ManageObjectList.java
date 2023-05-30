package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MineDoorSettings;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.Zones;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManageObjectList extends Question {
   private static final Logger logger = Logger.getLogger(ManageObjectList.class.getName());
   private static final String red = "color=\"255,127,127\"";
   private static final String green = "color=\"127,255,127\"";
   private static final String orange = "color=\"255,177,40\"";
   private final Player player;
   private final ManageObjectList.Type objectType;
   private final boolean fromList;
   private final int sortBy;
   private String searchName;
   private final boolean includeAll;
   private PermissionsPlayerList.ISettings[] objects = null;
   private boolean showingQueue = false;
   private boolean inQueue = true;
   private boolean waitAccept = false;
   private boolean inProgress = false;
   private boolean delivered = false;
   private boolean rejected = false;
   private boolean cancelled = false;

   public ManageObjectList(Creature aResponder, ManageObjectList.Type aType) {
      this(aResponder, aType, -10L, false, 1, "", true);
   }

   public ManageObjectList(
      Creature aResponder, ManageObjectList.Type aType, long parent, boolean wasFromList, int aSortBy, String searchFor, boolean aIncludeAll
   ) {
      super(
         aResponder, getTitle(aResponder, aType, parent, wasFromList, searchFor), getQuestion(aResponder, aType, parent, wasFromList, searchFor), 121, parent
      );
      this.player = (Player)this.getResponder();
      this.fromList = wasFromList;
      this.objectType = aType;
      this.sortBy = aSortBy;
      this.searchName = searchFor;
      this.includeAll = aIncludeAll;
   }

   public ManageObjectList(
      Creature aResponder,
      ManageObjectList.Type aType,
      long parent,
      boolean wasFromList,
      int aSortBy,
      String searchFor,
      boolean aIncludeAll,
      boolean inqueue,
      boolean waitaccept,
      boolean inprogress,
      boolean delivered,
      boolean rejected,
      boolean cancelled
   ) {
      super(
         aResponder, getTitle(aResponder, aType, parent, wasFromList, searchFor), getQuestion(aResponder, aType, parent, wasFromList, searchFor), 121, parent
      );
      this.player = (Player)this.getResponder();
      this.fromList = wasFromList;
      this.objectType = aType;
      this.sortBy = aSortBy;
      this.searchName = searchFor;
      this.includeAll = aIncludeAll;
      this.showingQueue = aType == ManageObjectList.Type.WAGONER;
      this.inQueue = inqueue;
      this.waitAccept = waitaccept;
      this.inProgress = inprogress;
      this.delivered = delivered;
      this.rejected = rejected;
      this.cancelled = cancelled;
   }

   private static String getTitle(Creature aResponder, ManageObjectList.Type aType, long parent, boolean wasFromList, String lookingFor) {
      if (aType == ManageObjectList.Type.DOOR) {
         try {
            Structure structure = Structures.getStructure(parent);
            return structure.getName() + "'s List of doors";
         } catch (NoSuchStructureException var7) {
            return aResponder.getName() + "'s List of " + aType.getTitle() + "s";
         }
      } else if (aType == ManageObjectList.Type.SEARCH) {
         return "Player Search";
      } else if (aType == ManageObjectList.Type.REPLY) {
         return "Search Result for " + lookingFor;
      } else if (aType == ManageObjectList.Type.SMALL_CART
         || aType == ManageObjectList.Type.LARGE_CART
         || aType == ManageObjectList.Type.WAGON
         || aType == ManageObjectList.Type.SHIP_CARRIER
         || aType == ManageObjectList.Type.CREATURE_CARRIER) {
         return aResponder.getName() + "'s List of Small Carts, Large Carts, Wagons and Carriers";
      } else if (aType == ManageObjectList.Type.WAGONER && wasFromList) {
         Wagoner wagoner = Wagoner.getWagoner(parent);
         return "Wagoners " + wagoner.getName() + "'s Queue";
      } else {
         return aResponder.getName() + "'s List of " + aType.getTitle() + "s";
      }
   }

   private static String getQuestion(Creature aResponder, ManageObjectList.Type aType, long parent, boolean wasFromList, String lookingFor) {
      if (aType == ManageObjectList.Type.DOOR) {
         try {
            Structure structure = Structures.getStructure(parent);
            return "Manage List of Doors for " + structure.getName();
         } catch (NoSuchStructureException var7) {
            return "Manage Your List of " + aType.getTitle() + "s";
         }
      } else if (aType == ManageObjectList.Type.SEARCH) {
         return "Player Search";
      } else if (aType == ManageObjectList.Type.REPLY) {
         return "Search Result for " + lookingFor;
      } else if (aType == ManageObjectList.Type.SMALL_CART
         || aType == ManageObjectList.Type.LARGE_CART
         || aType == ManageObjectList.Type.WAGON
         || aType == ManageObjectList.Type.SHIP_CARRIER
         || aType == ManageObjectList.Type.CREATURE_CARRIER) {
         return "Manage Your List of Small Carts, Large Carts, Wagons and Carriers";
      } else if (aType == ManageObjectList.Type.WAGONER && wasFromList) {
         Wagoner wagoner = Wagoner.getWagoner(parent);
         return "Viewing " + wagoner.getName() + "'s Queue";
      } else {
         return "Manage Your List of " + aType.getTitle() + "s";
      }
   }

   @Override
   public void answer(Properties aAnswer) {
      this.setAnswer(aAnswer);
      boolean managePermissions = this.getBooleanProp("permissions");
      boolean manageDoors = this.getBooleanProp("doors");
      boolean back = this.getBooleanProp("back");
      boolean close = this.getBooleanProp("close");
      boolean search = this.getBooleanProp("search");
      boolean remall = this.getBooleanProp("remall");
      boolean findAnimal = this.getBooleanProp("find");
      boolean inc = this.getBooleanProp("inc");
      boolean exc = this.getBooleanProp("exc");
      boolean queue = this.getBooleanProp("queue");
      boolean viewDelivery = this.getBooleanProp("delivery");
      if (!close) {
         if (inc) {
            ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, this.sortBy, this.searchName, true);
            mol.sendQuestion();
         } else if (exc) {
            ManageObjectList mol = new ManageObjectList(this.player, this.objectType, this.target, this.fromList, this.sortBy, this.searchName, false);
            mol.sendQuestion();
         } else {
            if (back) {
               if (this.objectType == ManageObjectList.Type.DOOR) {
                  ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.BUILDING, this.target, false, 1, "", this.includeAll);
                  mol.sendQuestion();
                  return;
               }

               if (this.objectType == ManageObjectList.Type.WAGONER) {
                  ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.WAGONER, this.target, false, 1, "", this.includeAll);
                  mol.sendQuestion();
                  return;
               }

               if (this.objectType == ManageObjectList.Type.DELIVERY) {
                  ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.WAGONER, this.target, false, 1, "", this.includeAll);
                  mol.sendQuestion2();
                  return;
               }

               if (this.objectType == ManageObjectList.Type.REPLY) {
                  ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.SEARCH);
                  mol.sendQuestion();
                  return;
               }
            }

            if (search) {
               String who = aAnswer.getProperty("who");
               String lookingFor = LoginHandler.raiseFirstLetter(who);
               long lookId = PlayerInfoFactory.getWurmId(lookingFor);
               ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.REPLY, lookId, true, 1, lookingFor, this.includeAll);
               mol.sendQuestion();
            } else {
               if (remall) {
                  for(PermissionsPlayerList.ISettings is : this.objects) {
                     if (!is.isActualOwner(this.target)) {
                        is.removeGuest(this.target);
                        this.player
                           .getCommunicator()
                           .sendNormalServerMessage("You removed " + this.searchName + " from " + is.getTypeName() + " (" + is.getObjectName() + ")");
                     }
                  }
               }

               if (managePermissions || manageDoors || findAnimal || queue || viewDelivery) {
                  String sel = aAnswer.getProperty("sel");
                  long selId = Long.parseLong(sel);
                  if (selId == -10L) {
                     this.player.getCommunicator().sendNormalServerMessage("You decide to do nothing.");
                     return;
                  }

                  if (managePermissions) {
                     int ct = WurmId.getType(selId);
                     if (ct == 1) {
                        try {
                           Creature creature = Creatures.getInstance().getCreature(selId);
                           if (creature.isWagoner()) {
                              ManagePermissions mp = new ManagePermissions(
                                 this.player, ManageObjectList.Type.WAGONER, creature, true, this.target, false, this.objectType, ""
                              );
                              mp.sendQuestion();
                           } else {
                              Vehicle vehicle = Vehicles.getVehicle(creature);
                              if (vehicle == null) {
                                 ManagePermissions mp = new ManagePermissions(
                                    this.player, ManageObjectList.Type.ANIMAL0, creature, true, this.target, false, this.objectType, ""
                                 );
                                 mp.sendQuestion();
                              } else if (vehicle.isUnmountable()) {
                                 ManagePermissions mp = new ManagePermissions(
                                    this.player, ManageObjectList.Type.ANIMAL0, creature, true, this.target, false, this.objectType, ""
                                 );
                                 mp.sendQuestion();
                              } else if (vehicle.getMaxPassengers() == 0) {
                                 ManagePermissions mp = new ManagePermissions(
                                    this.player, ManageObjectList.Type.ANIMAL1, creature, true, this.target, false, this.objectType, ""
                                 );
                                 mp.sendQuestion();
                              } else {
                                 ManagePermissions mp = new ManagePermissions(
                                    this.player, ManageObjectList.Type.ANIMAL2, creature, true, this.target, false, this.objectType, ""
                                 );
                                 mp.sendQuestion();
                              }
                           }
                        } catch (NoSuchCreatureException var27) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find animal, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find animal, it was here a minute ago! Id:" + selId, (Throwable)var27);
                        }
                     } else if (ct == 4) {
                        try {
                           PermissionsPlayerList.ISettings theItem = Structures.getStructure(selId);
                           ManagePermissions mp = new ManagePermissions(
                              this.player, ManageObjectList.Type.BUILDING, theItem, true, this.target, false, this.objectType, ""
                           );
                           mp.sendQuestion();
                        } catch (NoSuchStructureException var26) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + selId, (Throwable)var26);
                        }
                     } else if (ct == 5) {
                        try {
                           Structure structure = Structures.getStructure(this.target);

                           for(Door door : structure.getAllDoors()) {
                              if (door.getWurmId() == selId) {
                                 ManagePermissions mp = new ManagePermissions(
                                    this.player, ManageObjectList.Type.DOOR, door, true, this.target, this.fromList, this.objectType, ""
                                 );
                                 mp.sendQuestion();
                                 return;
                              }
                           }

                           this.player.getCommunicator().sendNormalServerMessage("Cannot find door, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find door, it was here a minute ago! Id:" + selId);
                        } catch (NoSuchStructureException var34) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + selId, (Throwable)var34);
                        }
                     } else if (ct == 7) {
                        FenceGate gate = FenceGate.getFenceGate(selId);
                        if (gate != null) {
                           ManagePermissions mp = new ManagePermissions(
                              this.player, ManageObjectList.Type.GATE, gate, true, this.target, this.fromList, this.objectType, ""
                           );
                           mp.sendQuestion();
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find gate, it was here a minute ago!");
                        }
                     } else if (ct == 2) {
                        try {
                           Item item = Items.getItem(selId);
                           ManageObjectList.Type itemType = ManageObjectList.Type.SHIP;
                           if (item.getTemplateId() == 186) {
                              itemType = ManageObjectList.Type.SMALL_CART;
                           } else if (item.getTemplateId() == 539) {
                              itemType = ManageObjectList.Type.LARGE_CART;
                           } else if (item.getTemplateId() == 850) {
                              itemType = ManageObjectList.Type.WAGON;
                           } else if (item.getTemplateId() == 853) {
                              itemType = ManageObjectList.Type.SHIP_CARRIER;
                           } else if (item.getTemplateId() == 1410) {
                              itemType = ManageObjectList.Type.CREATURE_CARRIER;
                           }

                           ManagePermissions mp = new ManagePermissions(this.player, itemType, item, true, this.target, this.fromList, this.objectType, "");
                           mp.sendQuestion();
                        } catch (NoSuchItemException var25) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find vehicle, it was here a minute ago!");
                        }
                     } else if (ct == 3) {
                        MineDoorPermission mineDoor = MineDoorPermission.getPermission(selId);
                        if (mineDoor != null) {
                           ManagePermissions mp = new ManagePermissions(
                              this.player, ManageObjectList.Type.MINEDOOR, mineDoor, true, this.target, this.fromList, this.objectType, ""
                           );
                           mp.sendQuestion();
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find minedoor, it was here a minute ago!");
                        }
                     } else {
                        this.player.getCommunicator().sendNormalServerMessage("Unknown object!");
                     }

                     return;
                  }

                  if (manageDoors) {
                     ManageObjectList mol = new ManageObjectList(this.player, ManageObjectList.Type.DOOR, selId, true, 1, "", this.includeAll);
                     mol.sendQuestion();
                  } else {
                     if (!findAnimal || Servers.isThisAPvpServer()) {
                        if (queue && this.objectType == ManageObjectList.Type.WAGONER) {
                           this.inQueue = this.getBooleanProp("inqueue");
                           this.waitAccept = this.getBooleanProp("waitaccept");
                           this.inProgress = this.getBooleanProp("inprogress");
                           this.delivered = this.getBooleanProp("delivered");
                           this.rejected = this.getBooleanProp("rejected");
                           this.cancelled = this.getBooleanProp("cancelled");
                           ManageObjectList mol = new ManageObjectList(
                              this.player,
                              ManageObjectList.Type.WAGONER,
                              selId,
                              true,
                              1,
                              "",
                              this.includeAll,
                              this.inQueue,
                              this.waitAccept,
                              this.inProgress,
                              this.delivered,
                              this.rejected,
                              this.cancelled
                           );
                           mol.sendQuestion2();
                           return;
                        } else {
                           this.inQueue = this.getBooleanProp("inqueue");
                           this.waitAccept = this.getBooleanProp("waitaccept");
                           this.inProgress = this.getBooleanProp("inprogress");
                           this.delivered = this.getBooleanProp("delivered");
                           this.rejected = this.getBooleanProp("rejected");
                           this.cancelled = this.getBooleanProp("cancelled");
                           ManageObjectList mol = new ManageObjectList(
                              this.player,
                              ManageObjectList.Type.DELIVERY,
                              selId,
                              true,
                              1,
                              "",
                              this.includeAll,
                              this.inQueue,
                              this.waitAccept,
                              this.inProgress,
                              this.delivered,
                              this.rejected,
                              this.cancelled
                           );
                           mol.sendQuestion3();
                           return;
                        }
                     }

                     try {
                        Creature creature = Creatures.getInstance().getCreature(selId);
                        int centerx = creature.getTileX();
                        int centery = creature.getTileY();
                        int dx = Math.abs(centerx - this.player.getTileX());
                        int dy = Math.abs(centery - this.player.getTileY());
                        int mindist = (int)Math.sqrt((double)(dx * dx + dy * dy));
                        int dir = MethodsCreatures.getDir(this.player, centerx, centery);
                        if (DbCreatureStatus.getIsLoaded(creature.getWurmId()) == 0) {
                           String direction = MethodsCreatures.getLocationStringFor(this.player.getStatus().getRotation(), dir, "you");
                           String toReturn = EndGameItems.getDistanceString(mindist, creature.getName(), direction, false);
                           this.player.getCommunicator().sendNormalServerMessage(toReturn);
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage("This creature is loaded in a cage, or on another server.");
                        }
                     } catch (NoSuchCreatureException var33) {
                        this.player.getCommunicator().sendNormalServerMessage("Cannot find animal, it was here a minute ago!");
                        logger.log(Level.WARNING, "Cannot find animal, it was here a minute ago! Id:" + selId, (Throwable)var33);
                     }
                  }
               }

               for(String key : this.getAnswer().stringPropertyNames()) {
                  if (key.startsWith("sort")) {
                     String sid = key.substring(4);
                     int newSort = Integer.parseInt(sid);
                     if (this.showingQueue) {
                        ManageObjectList mol = new ManageObjectList(
                           this.player,
                           this.objectType,
                           this.target,
                           this.fromList,
                           newSort,
                           this.searchName,
                           this.includeAll,
                           this.inQueue,
                           this.waitAccept,
                           this.inProgress,
                           this.delivered,
                           this.rejected,
                           this.cancelled
                        );
                        mol.sendQuestion2();
                        return;
                     }

                     if (this.objectType == ManageObjectList.Type.DELIVERY) {
                        ManageObjectList mol = new ManageObjectList(
                           this.player,
                           this.objectType,
                           this.target,
                           this.fromList,
                           newSort,
                           this.searchName,
                           this.includeAll,
                           this.inQueue,
                           this.waitAccept,
                           this.inProgress,
                           this.delivered,
                           this.rejected,
                           this.cancelled
                        );
                        mol.sendQuestion3();
                        return;
                     }

                     ManageObjectList mol = new ManageObjectList(
                        this.player, this.objectType, this.target, this.fromList, newSort, this.searchName, this.includeAll
                     );
                     mol.sendQuestion();
                     return;
                  }
               }

               if (this.objectType == ManageObjectList.Type.BUILDING) {
                  for(String key : this.getAnswer().stringPropertyNames()) {
                     if (key.startsWith("demolish")) {
                        String sid = key.substring(8);
                        long id = Long.parseLong(sid);

                        try {
                           Structure structure = Structures.getStructure(id);
                           if (structure.isOnSurface()) {
                              Zones.flash(structure.getCenterX(), structure.getCenterY(), false);
                           }

                           structure.totallyDestroy();
                        } catch (NoSuchStructureException var32) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find structure, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + id, (Throwable)var32);
                        }
                     }
                  }
               } else if (this.objectType == ManageObjectList.Type.WAGONER) {
                  for(String key : this.getAnswer().stringPropertyNames()) {
                     if (key.startsWith("dismiss")) {
                        String sid = key.substring(7);
                        long id = Long.parseLong(sid);

                        try {
                           Creature creature = Creatures.getInstance().getCreature(id);
                           Wagoner wagoner = creature.getWagoner();
                           if (wagoner.getVillageId() == -1) {
                              this.player.getCommunicator().sendNormalServerMessage("Wagoner is already dismissing!");
                           } else {
                              WagonerDismissQuestion wdq = new WagonerDismissQuestion(this.getResponder(), wagoner);
                              wdq.sendQuestion();
                           }
                        } catch (NoSuchCreatureException var31) {
                           this.player.getCommunicator().sendNormalServerMessage("Cannot find wagoner, it was here a minute ago!");
                           logger.log(Level.WARNING, "Cannot find wagoner, it was here a minute ago! Id:" + id, (Throwable)var31);
                        }
                     }
                  }
               } else if (this.objectType == ManageObjectList.Type.ANIMAL0
                  || this.objectType == ManageObjectList.Type.ANIMAL1
                  || this.objectType == ManageObjectList.Type.ANIMAL2) {
                  for(String key : this.getAnswer().stringPropertyNames()) {
                     if (key.startsWith("uncarefor")) {
                        String sid = key.substring(9);
                        long id = Long.parseLong(sid);

                        try {
                           int tc = Creatures.getInstance().getNumberOfCreaturesProtectedBy(this.player.getWurmId());
                           int max = this.player.getNumberOfPossibleCreatureTakenCareOf();
                           Creature animal = Creatures.getInstance().getCreature(id);
                           if (animal.getCareTakerId() == this.player.getWurmId()) {
                              Creatures.getInstance().setCreatureProtected(animal, -10L, false);
                              this.player
                                 .getCommunicator()
                                 .sendNormalServerMessage(
                                    "You let "
                                       + animal.getName()
                                       + " go in order to care for other creatures. You may care for "
                                       + (max - tc + 1)
                                       + " more creatures."
                                 );
                           } else {
                              this.player.getCommunicator().sendNormalServerMessage("You are not caring for this animal!");
                           }
                        } catch (NoSuchCreatureException var30) {
                           logger.log(Level.WARNING, var30.getMessage(), (Throwable)var30);
                        }
                     }

                     if (key.startsWith("unbrand")) {
                        String sid = key.substring(7);
                        long id = Long.parseLong(sid);

                        try {
                           Creature animal = Creatures.getInstance().getCreature(id);
                           Brand brand = Creatures.getInstance().getBrand(animal.getWurmId());
                           if (brand != null) {
                              if (animal.getBrandVillage() == this.player.getCitizenVillage()) {
                                 if (this.player.getCitizenVillage().isActionAllowed((short)484, this.player)) {
                                    brand.deleteBrand();
                                    if (animal.getVisionArea() != null) {
                                       animal.getVisionArea().broadCastUpdateSelectBar(animal.getWurmId());
                                    }
                                 } else {
                                    this.player.getCommunicator().sendNormalServerMessage("You need to have deed permission to remove a brand.");
                                 }
                              } else {
                                 this.player.getCommunicator().sendNormalServerMessage("You need to be in same village as the brand on the animal.");
                              }
                           } else {
                              this.player.getCommunicator().sendNormalServerMessage("That animal is not branded.");
                           }
                        } catch (NoSuchCreatureException var29) {
                           logger.log(Level.WARNING, var29.getMessage(), (Throwable)var29);
                        }
                     }

                     if (key.startsWith("untame")) {
                        String sid = key.substring(6);
                        long id = Long.parseLong(sid);

                        try {
                           Creature animal = Creatures.getInstance().getCreature(id);
                           if (animal.getDominator() == this.player) {
                              if (DbCreatureStatus.getIsLoaded(animal.getWurmId()) == 1) {
                                 this.player.getCommunicator().sendNormalServerMessage("This animal is caged, remove it first.", (byte)3);
                                 return;
                              }

                              Creature pet = this.player.getPet();
                              if (animal.cantRideUntame()) {
                                 assert pet != null;

                                 Vehicle cret = Vehicles.getVehicleForId(pet.getWurmId());
                                 if (cret != null) {
                                    cret.kickAll();
                                 }
                              }

                              animal.setDominator(-10L);
                              this.player.setPet(-10L);
                              this.player.getCommunicator().sendNormalServerMessage("You no longer have this animal tamed!");
                           } else {
                              this.player.getCommunicator().sendNormalServerMessage("You do not have this animal tamed!");
                           }
                        } catch (NoSuchCreatureException var28) {
                           logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      int width = 300;
      StringBuilder buf = new StringBuilder();
      String closeBtn = "harray{"
         + (
            (this.objectType == ManageObjectList.Type.DOOR || this.objectType == ManageObjectList.Type.REPLY) && this.fromList
               ? "button{text=\"Back\";id=\"back\"};"
               : ""
         )
         + "label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
      buf.append(
         "border{border{size=\"20,20\";null;null;label{type='bold';text=\""
            + this.question
            + "\"};"
            + closeBtn
            + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      String extraButton = "";
      if (this.objectType == ManageObjectList.Type.SEARCH) {
         buf.append("text{text=\"Allow searching for all objects that the player has permissions and you can manage.\"}");
         buf.append("harray{label{text=\"Look For Player \"}input{id=\"who\"}}");
         buf.append("text{text=\"\"}");
         buf.append("harray{button{text=\"Search\";id=\"search\";default=\"true\"}};");
         buf.append("}};null;null;}");
         this.getResponder().getCommunicator().sendBml(300, 160, true, true, buf.toString(), 200, 200, 200, this.title);
      } else {
         if (this.objectType == ManageObjectList.Type.REPLY) {
            if (this.target == -10L) {
               buf.append("label{text=\"No such Player\"}");
               buf.append("}};null;null;}");
               this.getResponder().getCommunicator().sendBml(300, 150, true, true, buf.toString(), 200, 200, 200, this.title);
               return;
            }

            Village vill = this.getResponder().getCitizenVillage();
            int vid = vill != null && vill.getRoleFor(this.getResponder()).mayManageAllowedObjects() ? vill.getId() : -1;
            Set<PermissionsPlayerList.ISettings> result = new HashSet<>();
            this.objects = Creatures.getManagedAnimalsFor(this.player, vid, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = Structures.getManagedBuildingsFor(this.player, vid, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = FenceGate.getManagedGatesFor(this.player, vid, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = Items.getManagedCartsFor(this.player, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = MineDoorPermission.getManagedMineDoorsFor(this.player, vid, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = Items.getManagedShipsFor(this.player, true);

            for(PermissionsPlayerList.ISettings is : this.objects) {
               if (is.isGuest(this.target)) {
                  result.add(is);
               }
            }

            this.objects = result.toArray(new PermissionsPlayerList.ISettings[result.size()]);
            buf.append("text{text=\"List of objects that player '" + this.searchName + "' has permissions for that you may manage.\"}");
            int absSortBy = Math.abs(this.sortBy);
            final int upDown = Integer.signum(this.sortBy);
            buf.append(
               "table{rows=\"1\";cols=\"5\";label{text=\"\"};"
                  + this.colHeader("Name", 1, this.sortBy)
                  + this.colHeader("Type", 2, this.sortBy)
                  + this.colHeader("Owner?", 3, this.sortBy)
                  + "label{type=\"bold\";text=\"\"};"
            );
            switch(absSortBy) {
               case 1:
                  Arrays.sort(this.objects, new Comparator<PermissionsPlayerList.ISettings>() {
                     public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2) {
                        return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                     }
                  });
                  break;
               case 2:
                  Arrays.sort(this.objects, new Comparator<PermissionsPlayerList.ISettings>() {
                     public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2) {
                        return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
                     }
                  });
                  break;
               case 3:
                  Arrays.sort(this.objects, new Comparator<PermissionsPlayerList.ISettings>() {
                     public int compare(PermissionsPlayerList.ISettings param1, PermissionsPlayerList.ISettings param2) {
                        if (param1.isActualOwner(ManageObjectList.this.target) == param2.isActualOwner(ManageObjectList.this.target)) {
                           return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                        } else {
                           return param1.isActualOwner(ManageObjectList.this.target) ? -1 * upDown : 1 * upDown;
                        }
                     }
                  });
            }

            for(PermissionsPlayerList.ISettings object : this.objects) {
               buf.append(
                  "radio{group=\"sel\";id=\""
                     + object.getWurmId()
                     + "\";text=\"\"}label{text=\""
                     + object.getObjectName()
                     + "\"};label{text=\""
                     + object.getTypeName()
                     + "\"};label{"
                     + this.showBoolean(object.isActualOwner(this.target))
                     + "};label{text=\"\"}"
               );
            }

            buf.append("}");
            if (result.size() > 0) {
               extraButton = ";label{text=\"  \"};button{text=\"Remove all Permissions\";id=\"remall\"}";
            }
         } else if (this.objectType == ManageObjectList.Type.ANIMAL0
            || this.objectType == ManageObjectList.Type.ANIMAL1
            || this.objectType == ManageObjectList.Type.ANIMAL2) {
            buf.append(this.makeAnimalList());
            if (!Servers.isThisAPvpServer()) {
               extraButton = ";label{text=\"  \"};button{text=\"Give direction to\";id=\"find\"}";
            }

            width = 550;
         } else if (this.objectType == ManageObjectList.Type.BUILDING) {
            buf.append(this.makeBuildingList());
            extraButton = ";label{text=\"  \"};button{text=\"Manage All Doors\";id=\"doors\"}";
            width = 500;
         } else if (this.objectType == ManageObjectList.Type.LARGE_CART
            || this.objectType == ManageObjectList.Type.SMALL_CART
            || this.objectType == ManageObjectList.Type.WAGON
            || this.objectType == ManageObjectList.Type.SHIP_CARRIER
            || this.objectType == ManageObjectList.Type.CREATURE_CARRIER) {
            buf.append(this.makeLandVehicleList());
            width = 500;
         } else if (this.objectType == ManageObjectList.Type.DOOR) {
            buf.append(this.makeDoorList());
            width = 500;
         } else if (this.objectType == ManageObjectList.Type.GATE) {
            buf.append(this.makeGateList());
            width = 600;
         } else if (this.objectType == ManageObjectList.Type.MINEDOOR) {
            buf.append(this.makeMineDoorList());
            width = 400;
         } else if (this.objectType == ManageObjectList.Type.SHIP) {
            buf.append(this.makeShipList());
            width = 500;
         } else if (this.objectType == ManageObjectList.Type.WAGONER) {
            buf.append(this.makeWagonerList());
            width = 600;
         }

         buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";text=\"None\"}");
         buf.append("text{text=\"\"}");
         buf.append("harray{button{text=\"Manage Permissions\";id=\"permissions\"}" + extraButton + "};");
         if (this.objectType == ManageObjectList.Type.WAGONER) {
            buf.append("text{text=\"\"}");
            buf.append(
               "harray{button{text=\"View Deliveries\";id=\"queue\"};label{text=\" filter by \"};checkbox{id=\"inqueue\";text=\"In queue  \""
                  + (this.inQueue ? ";selected=\"true\"" : "")
                  + "};checkbox{id=\"waitaccept\";text=\"Waiting for accept  \""
                  + (this.waitAccept ? ";selected=\"true\"" : "")
                  + "};checkbox{id=\"inprogress\";text=\"In Progress  \""
                  + (this.inProgress ? ";selected=\"true\"" : "")
                  + "};checkbox{id=\"delivered\";text=\"Delivered  \""
                  + (this.delivered ? ";selected=\"true\"" : "")
                  + "};checkbox{id=\"rejected\";text=\"Rejected  \""
                  + (this.rejected ? ";selected=\"true\"" : "")
                  + "};checkbox{id=\"cancelled\";text=\"Cancelled  \""
                  + (this.cancelled ? ";selected=\"true\"" : "")
                  + "};};"
            );
         }

         buf.append("}};null;null;}");
         this.getResponder().getCommunicator().sendBml(width, 400, true, true, buf.toString(), 200, 200, 200, this.title);
      }
   }

   private void sendQuestion2() {
      StringBuilder buf = new StringBuilder();
      String closeBtn = "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
      buf.append(
         "border{border{size=\"20,20\";null;null;label{type='bold';text=\""
            + this.question
            + "\"};"
            + "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};"
            + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      Delivery[] deliveries = Delivery.getDeliveriesFor(this.target, this.inQueue, this.waitAccept, this.inProgress, this.rejected, this.delivered);
      buf.append(
         "table{rows=\"1\";cols=\"6\";label{text=\"\"};"
            + this.colHeader("id", 1, this.sortBy)
            + this.colHeader("Sender", 2, this.sortBy)
            + this.colHeader("Receiver", 3, this.sortBy)
            + this.colHeader("State", 4, this.sortBy)
            + "label{text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(deliveries, new Comparator<Delivery>() {
               public int compare(Delivery param1, Delivery param2) {
                  long value1 = param1.getDeliveryId();
                  long value2 = param2.getDeliveryId();
                  if (value1 == value2) {
                     return 0;
                  } else {
                     return value1 < value2 ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 2:
            Arrays.sort(deliveries, new Comparator<Delivery>() {
               public int compare(Delivery param1, Delivery param2) {
                  return param1.getSenderName().compareTo(param2.getSenderName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(deliveries, new Comparator<Delivery>() {
               public int compare(Delivery param1, Delivery param2) {
                  return param1.getReceiverName().compareTo(param2.getReceiverName()) * upDown;
               }
            });
            break;
         case 4:
            Arrays.sort(deliveries, new Comparator<Delivery>() {
               public int compare(Delivery param1, Delivery param2) {
                  return param1.getStateName().compareTo(param2.getStateName()) * upDown;
               }
            });
      }

      for(Delivery delivery : deliveries) {
         buf.append(
            "radio{group=\"sel\";id=\""
               + delivery.getDeliveryId()
               + "\";text=\"\"}label{text=\""
               + delivery.getDeliveryId()
               + "\"};label{text=\""
               + delivery.getSenderName()
               + "\"};label{text=\""
               + delivery.getReceiverName()
               + "\"};label{text=\""
               + delivery.getStateName()
               + "\"};label{text=\"  \"};"
         );
      }

      buf.append("}");
      buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";text=\"None\"}");
      buf.append("text{text=\"\"}");
      buf.append("harray{button{text=\"View Delivery\";id=\"delivery\"};};");
      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private void sendQuestion3() {
      StringBuilder buf = new StringBuilder();
      String closeBtn = "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};";
      buf.append(
         "border{border{size=\"20,20\";null;null;label{type='bold';text=\""
            + this.question
            + "\"};"
            + "harray{button{text=\"Back\";id=\"back\"};label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};"
            + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
            + this.getId()
            + "\"}"
      );
      Delivery delivery = Delivery.getDelivery(this.target);
      buf.append("table{rows=\"1\";cols=\"4\";");
      buf.append("label{text=\"\"};label{text=\"Id\"};label{text=\"\"};label{text=\"" + delivery.getDeliveryId() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Sender\"};label{text=\"\"};label{text=\"" + delivery.getSenderName() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Receiver\"};label{text=\"\"};label{text=\"" + delivery.getReceiverName() + "\"};");
      buf.append("label{text=\"\"};label{text=\"State\"};label{text=\"\"};label{text=\"" + delivery.getStateName() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Delivery setup\"};label{text=\"@\"};label{text=\"" + delivery.getStringWaitingForAccept() + "\"};");
      String reason = "Accepted";
      switch(delivery.getState()) {
         case 5:
         case 8:
            reason = "Rejected";
            break;
         case 6:
         case 11:
            reason = "Timed Out";
         case 7:
         default:
            break;
         case 9:
         case 10:
            reason = "Cancelled";
      }

      buf.append("label{text=\"\"};label{text=\"" + reason + "\"};label{text=\"@\"};label{text=\"" + delivery.getStringAcceptedOrRejected() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Delivery started\"};label{text=\"@\"};label{text=\"" + delivery.getStringDeliveryStarted() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Crates picked up\"};label{text=\"@\"};label{text=\"" + delivery.getStringPickedUp() + "\"};");
      buf.append("label{text=\"\"};label{text=\"Crates delivered\"};label{text=\"@\"};label{text=\"" + delivery.getStringDelivered() + "\"};");
      buf.append("}");
      buf.append("}};null;null;}");
      this.getResponder().getCommunicator().sendBml(400, 400, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   private String makeAnimalList() {
      StringBuilder buf = new StringBuilder();
      Village vill = this.getResponder().getCitizenVillage();
      final int vid = vill != null && vill.getRoleFor(this.getResponder()).mayManageAllowedObjects() ? vill.getId() : -1;
      buf.append(
         "text{text=\"As well as the list containing any animals that you care for, and any tamed animals you have. It also includes any animals that are branded to your village that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}"
      );
      buf.append("text{text=\"\"}");
      Creature[] animals = Creatures.getManagedAnimalsFor(this.player, vid, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"8\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Animal Type", 2, this.sortBy)
            + this.colHeader("On Deed?", 3, this.sortBy)
            + this.colHeader("Hitched?", 4, this.sortBy)
            + this.colHeader("Cared For?", 5, this.sortBy)
            + this.colHeader("Branded?", 6, this.sortBy)
            + this.colHeader("Tamed?", 7, this.sortBy)
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  return param1.getName().compareTo(param2.getName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  if (param1.isOnDeed() == param2.isOnDeed()) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  } else {
                     return param1.isOnDeed() ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  if (param1.isHitched() == param2.isHitched()) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  } else {
                     return param1.isHitched() ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 5:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  if (param1.isCaredFor(ManageObjectList.this.player) == param2.isCaredFor(ManageObjectList.this.player)) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  } else {
                     return param1.isCaredFor(ManageObjectList.this.player) ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 6:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  if (param1.isBrandedBy(vid) == param2.isBrandedBy(vid)) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  } else {
                     return param1.isBrandedBy(vid) ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 7:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  if (param1.isDominated() == param2.isDominated()) {
                     return param1.getName().compareTo(param2.getName()) * upDown;
                  } else {
                     return param1.isDominated() ? 1 * upDown : -1 * upDown;
                  }
               }
            });
      }

      for(Creature animal : animals) {
         buf.append(
            (animal.canHavePermissions() ? "radio{group=\"sel\";id=\"" + animal.getWurmId() + "\";text=\"\"}" : "label{text=\"\"};")
               + "label{text=\""
               + animal.getName()
               + "\"};label{text=\""
               + animal.getTypeName()
               + "\"};"
               + (animal.isBranded() ? "label{" + this.showBoolean(animal.isOnDeed()) + "};" : "label{text=\"not branded\"};")
               + "label{"
               + this.showBoolean(animal.isHitched())
               + "};"
         );
         if (animal.isCaredFor(this.player)) {
            buf.append(this.unCareForButton(animal));
         } else {
            buf.append("label{" + this.showBoolean(animal.getCareTakerId() != -10L) + "};");
         }

         if (animal.isBranded()
            && animal.getBrandVillage() == this.player.getCitizenVillage()
            && this.player.getCitizenVillage().isActionAllowed((short)484, this.player)) {
            buf.append(this.unBrandButton(animal));
         } else {
            buf.append("label{" + this.showBoolean(animal.isBranded()) + "};");
         }

         if (animal.isDominated() && animal.getDominator() == this.player) {
            buf.append(this.unTameButton(animal));
         } else {
            buf.append("label{" + this.showBoolean(animal.isDominated()) + "};");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   private String unCareForButton(Creature animal) {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "harray{button{text=\"Un-Care For\";id=\"uncarefor"
            + animal.getWurmId()
            + "\";confirm=\"You are about to un care for "
            + animal.getName()
            + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}"
      );
      return buf.toString();
   }

   private String unBrandButton(Creature animal) {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "harray{button{text=\"Un-Brand\";id=\"unbrand"
            + animal.getWurmId()
            + "\";confirm=\"You are about to remove the brand from "
            + animal.getName()
            + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}"
      );
      return buf.toString();
   }

   private String unTameButton(Creature animal) {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "harray{button{text=\"Un-Tame\";id=\"untame"
            + animal.getWurmId()
            + "\";confirm=\"You are about to un tame "
            + animal.getName()
            + ".\";question=\"Do you really want to do that?\"}label{text=\" \"}}"
      );
      return buf.toString();
   }

   private String makeBuildingList() {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "text{text=\"List includes any buildings that you are the owner of plus any buildings in your settlment that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}"
      );
      buf.append("text{text=\"\"}");
      Village vill = this.getResponder().getCitizenVillage();
      int vid = vill != null && vill.getRoleFor(this.getResponder()).mayManageAllowedObjects() ? vill.getId() : -1;
      Structure[] structures = Structures.getManagedBuildingsFor(this.player, vid, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"7\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Owner?", 2, this.sortBy)
            + this.colHeader("Doors have locks?", 3, this.sortBy)
            + this.colHeader("On Deed?", 4, this.sortBy)
            + this.colHeader("Deed Managed?", 5, this.sortBy)
            + "label{type=\"bold\";text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(structures, new Comparator<Structure>() {
               public int compare(Structure param1, Structure param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(structures, new Comparator<Structure>() {
               public int compare(Structure param1, Structure param2) {
                  if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isActualOwner(ManageObjectList.this.player.getWurmId()) ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 3:
            Arrays.sort(structures, new Comparator<Structure>() {
               public int compare(Structure param1, Structure param2) {
                  int value1 = param1.getAllDoors().length == 0 ? 0 : (param1.isLockable() ? 1 : 2);
                  int value2 = param2.getAllDoors().length == 0 ? 0 : (param2.isLockable() ? 1 : 2);
                  if (value1 == value2) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return value1 < value2 ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(structures, new Comparator<Structure>() {
               public int compare(Structure param1, Structure param2) {
                  int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
                  int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
                  if (value1 == value2) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return value1 < value2 ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 5:
            Arrays.sort(structures, new Comparator<Structure>() {
               public int compare(Structure param1, Structure param2) {
                  if (param1.isManaged() == param2.isManaged()) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isManaged() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(Structure structure : structures) {
         buf.append(
            (structure.canHavePermissions() ? "radio{group=\"sel\";id=\"" + structure.getWurmId() + "\";text=\"\"}" : "label{text=\"\"};")
               + "label{text=\""
               + structure.getObjectName()
               + "\"};label{"
               + this.showBoolean(structure.isActualOwner(this.player.getWurmId()))
               + "};"
         );
         if (structure.getAllDoors().length == 0) {
            buf.append("label{color=\"255,177,40\"text=\"No lockable doors.\"};");
         } else if (structure.isLockable()) {
            buf.append("label{color=\"127,255,127\"text=\"true\"};");
         } else {
            buf.append("label{color=\"255,127,127\"text=\"Not all doors have locks.\"};");
         }

         buf.append("label{" + this.showBoolean(structure.getVillage() != null) + "};");
         buf.append("label{" + this.showBoolean(structure.isManaged()) + "};");
         if (structure.isOwner(this.player.getWurmId())) {
            buf.append(
               "harray{label{text=\" \"};button{text=\"Demolish\";id=\"demolish"
                  + structure.getWurmId()
                  + "\";confirm=\"You are about to blow up the building '"
                  + structure.getObjectName()
                  + "'.\";question=\"Do you really want to do that?\"}label{text=\" \"}}"
            );
         } else {
            buf.append("label{text=\" \"}");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   private String makeDoorList() {
      StringBuilder buf = new StringBuilder();

      try {
         Structure structure = Structures.getStructure(this.target);
         buf.append(
            "text{text=\"List includes all doors in this building if you are the owner, or any doors in this building that have the 'Building may manage' Permission so long as you have the 'Manage Permissions' building permission.\"}"
         );
         buf.append("text{text=\"Note: Owner of the Door is the Owner of the bulding.\"}");
         buf.append("text{text=\"\"}");
         buf.append("text{type=\"bold\";text=\"List of Doors that you may manage in this building.\"}");
         if (this.includeAll) {
            buf.append(this.extraButton("Exclude Doors without locks", "exc"));
         } else {
            buf.append(this.extraButton("Include Doors without locks", "inc"));
         }

         Door[] doors = structure.getAllDoors(this.includeAll);
         int absSortBy = Math.abs(this.sortBy);
         final int upDown = Integer.signum(this.sortBy);
         buf.append(
            "table{rows=\"1\";cols=\"7\";label{text=\"\"};"
               + this.colHeader("Name", 1, this.sortBy)
               + this.colHeader("Door Type", 2, this.sortBy)
               + this.colHeader("Level", 3, this.sortBy)
               + this.colHeader("Has Lock?", 4, this.sortBy)
               + this.colHeader("Locked?", 5, this.sortBy)
               + this.colHeader("Building Managed?", 6, this.sortBy)
         );
         Arrays.sort(doors, new Comparator<Door>() {
            public int compare(Door param1, Door param2) {
               if (param1.getFloorLevel() == param2.getFloorLevel()) {
                  int comp = param1.getTypeName().compareTo(param2.getTypeName());
                  return comp == 0 ? param1.getObjectName().compareTo(param2.getObjectName()) * upDown : comp * upDown;
               } else {
                  return param1.getFloorLevel() < param2.getFloorLevel() ? 1 * upDown : -1 * upDown;
               }
            }
         });
         switch(absSortBy) {
            case 1:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  }
               });
               break;
            case 2:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
                  }
               });
               break;
            case 3:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     if (param1.getFloorLevel() == param2.getFloorLevel()) {
                        return 0;
                     } else {
                        return param1.getFloorLevel() < param2.getFloorLevel() ? 1 * upDown : -1 * upDown;
                     }
                  }
               });
               break;
            case 4:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     if (param1.hasLock() == param2.hasLock()) {
                        return 0;
                     } else {
                        return param1.hasLock() ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 5:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     if (param1.isLocked() == param2.isLocked()) {
                        return 0;
                     } else {
                        return param1.isLocked() ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
               break;
            case 6:
               Arrays.sort(doors, new Comparator<Door>() {
                  public int compare(Door param1, Door param2) {
                     if (param1.isManaged() == param2.isManaged()) {
                        return 0;
                     } else {
                        return param1.isManaged() ? -1 * upDown : 1 * upDown;
                     }
                  }
               });
         }

         for(Door door : doors) {
            buf.append(
               (door.canHavePermissions() ? "radio{group=\"sel\";id=\"" + door.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}")
                  + "label{text=\""
                  + door.getObjectName()
                  + "\"};label{text=\""
                  + door.getTypeName()
                  + "\"};label{text=\""
                  + door.getFloorLevel()
                  + "\"};label{"
                  + this.showBoolean(door.hasLock())
                  + "};label{"
                  + this.showBoolean(door.isLocked())
                  + "};label{"
                  + this.showBoolean(door.isManaged())
                  + "};"
            );
         }

         buf.append("}");
      } catch (NoSuchStructureException var10) {
         logger.log(Level.WARNING, "Cannot find structure, it was here a minute ago! Id:" + this.target, (Throwable)var10);
         buf.append("text{text=\"Cannot find structure, it was here a minute ago!\"}");
      }

      return buf.toString();
   }

   private String makeGateList() {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "text{text=\"As well as the list containing any gates that you are the owner of their lock it also includes any gate that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}"
      );
      Village vill = this.getResponder().getCitizenVillage();
      if (vill != null && vill.isMayor(this.player)) {
         buf.append("text{text=\"As you are a mayor, the list will have all gates on your deed.\"}");
      }

      buf.append("text{text=\"\"}");
      if (this.includeAll) {
         buf.append(this.extraButton("Exclude Gates without locks", "exc"));
      } else {
         buf.append(this.extraButton("Include Gates without locks", "inc"));
      }

      int vid = vill != null && vill.getRoleFor(this.getResponder()).mayManageAllowedObjects() ? vill.getId() : -1;
      FenceGate[] gates = FenceGate.getManagedGatesFor(this.player, vid, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"9\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Gate Type", 2, this.sortBy)
            + this.colHeader("Level", 3, this.sortBy)
            + this.colHeader("Has Lock?", 4, this.sortBy)
            + this.colHeader("Locked?", 5, this.sortBy)
            + this.colHeader("Owner?", 6, this.sortBy)
            + this.colHeader("On Deed?", 7, this.sortBy)
            + this.colHeader("Deed Managed?", 8, this.sortBy)
      );
      Arrays.sort(gates, new Comparator<FenceGate>() {
         public int compare(FenceGate param1, FenceGate param2) {
            if (param1.getFloorLevel() == param2.getFloorLevel()) {
               int comp = param1.getTypeName().compareTo(param2.getTypeName());
               return comp == 0 ? param1.getObjectName().compareTo(param2.getObjectName()) * upDown : comp * upDown;
            } else {
               return param1.getFloorLevel() < param2.getFloorLevel() ? 1 * upDown : -1 * upDown;
            }
         }
      });
      switch(absSortBy) {
         case 1:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  if (param1.getFloorLevel() == param2.getFloorLevel()) {
                     return 0;
                  } else {
                     return param1.getFloorLevel() < param2.getFloorLevel() ? 1 * upDown : -1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  if (param1.hasLock() == param2.hasLock()) {
                     return 0;
                  } else {
                     return param1.hasLock() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 5:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  if (param1.isLocked() == param2.isLocked()) {
                     return 0;
                  } else {
                     return param1.isLocked() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 6:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isActualOwner(ManageObjectList.this.player.getWurmId()) ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 7:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
                  int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
                  if (value1 == value2) {
                     return 0;
                  } else {
                     return value1 < value2 ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 8:
            Arrays.sort(gates, new Comparator<FenceGate>() {
               public int compare(FenceGate param1, FenceGate param2) {
                  if (param1.isManaged() == param2.isManaged()) {
                     return 0;
                  } else {
                     return param1.isManaged() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(FenceGate gate : gates) {
         buf.append(
            (gate.canHavePermissions() ? "radio{group=\"sel\";id=\"" + gate.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}")
               + "label{text=\""
               + gate.getObjectName()
               + "\"};label{text=\""
               + gate.getTypeName()
               + "\"};label{text=\""
               + gate.getFloorLevel()
               + "\"};label{"
               + this.showBoolean(gate.hasLock())
               + "};label{"
               + this.showBoolean(gate.isLocked())
               + "};label{"
               + this.showBoolean(gate.isActualOwner(this.player.getWurmId()))
               + "};label{"
               + this.showBoolean(gate.getVillage() != null)
               + "};label{"
               + this.showBoolean(gate.isManaged())
               + "};"
         );
      }

      buf.append("}");
      return buf.toString();
   }

   private String makeLandVehicleList() {
      StringBuilder buf = new StringBuilder();
      buf.append("text{text=\"List contains the Small Carts, Large Carts, Wagons and Carriers that you can manage.\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"List of Small Carts, Large Carts, Wagons and Carriers that you may manage.\"}");
      if (this.includeAll) {
         buf.append(this.extraButton("Exclude Vehicles without locks", "exc"));
      } else {
         buf.append(this.extraButton("Include Vehicles without locks", "inc"));
      }

      Item[] items = Items.getManagedCartsFor(this.player, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"6\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Type", 2, this.sortBy)
            + this.colHeader("Owner?", 3, this.sortBy)
            + this.colHeader("Locked?", 4, this.sortBy)
            + "label{type=\"bold\";text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isActualOwner(ManageObjectList.this.player.getWurmId()) ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  if (param1.isLocked() == param2.isLocked()) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isLocked() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(Item item : items) {
         buf.append(
            (item.canHavePermissions() ? "radio{group=\"sel\";id=\"" + item.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}")
               + "label{text=\""
               + item.getObjectName()
               + "\"};"
               + addRariryColour(item, item.getTypeName())
               + "label{"
               + this.showBoolean(item.isActualOwner(this.player.getWurmId()))
               + "};label{"
               + this.showBoolean(item.isLocked())
               + "};label{text=\"\"};"
         );
      }

      buf.append("}");
      return buf.toString();
   }

   private String makeMineDoorList() {
      Village vill = this.getResponder().getCitizenVillage();
      StringBuilder buf = new StringBuilder();
      buf.append(
         "text{text=\"As well as the list containing any mine doors that you are the owner of it also includes any mine doors that have 'Settlement may manage' Permission set to your village so long as you have the 'Manage Allowed Objects' settlement permission.\"}"
      );
      if (vill != null && vill.isMayor(this.player)) {
         buf.append("text{text=\"As you are a mayor, the list will have all minedoors on your deed.\"}");
      }

      buf.append("text{text=\"\"}");
      int vid = vill != null && vill.getRoleFor(this.getResponder()).mayManageAllowedObjects() ? vill.getId() : -1;
      MineDoorPermission[] mineDoors = MineDoorPermission.getManagedMineDoorsFor(this.player, vid, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"7\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Door Type", 2, this.sortBy)
            + this.colHeader("Owner?", 3, this.sortBy)
            + this.colHeader("On Deed?", 4, this.sortBy)
            + this.colHeader("Deed Managed?", 5, this.sortBy)
            + "label{type=\"bold\";text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(mineDoors, new Comparator<MineDoorPermission>() {
               public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(mineDoors, new Comparator<MineDoorPermission>() {
               public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(mineDoors, new Comparator<MineDoorPermission>() {
               public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                  if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isActualOwner(ManageObjectList.this.player.getWurmId()) ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(mineDoors, new Comparator<MineDoorPermission>() {
               public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                  int value1 = param1.getVillage() != null ? param1.getVillage().getId() : 0;
                  int value2 = param2.getVillage() != null ? param2.getVillage().getId() : 0;
                  if (value1 == value2) {
                     return 0;
                  } else {
                     return value1 < value2 ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 5:
            Arrays.sort(mineDoors, new Comparator<MineDoorPermission>() {
               public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                  if (param1.isManaged() == param2.isManaged()) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isManaged() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(MineDoorPermission mineDoor : mineDoors) {
         buf.append(
            (mineDoor.canHavePermissions() ? "radio{group=\"sel\";id=\"" + mineDoor.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}")
               + "label{text=\""
               + mineDoor.getObjectName()
               + "\"};label{text=\""
               + mineDoor.getTypeName()
               + "\"};label{"
               + this.showBoolean(mineDoor.isActualOwner(this.player.getWurmId()))
               + "};label{"
               + this.showBoolean(mineDoor.getVillage() != null)
               + "};label{"
               + this.showBoolean(mineDoor.isManaged())
               + "};label{text=\" \"}"
         );
      }

      buf.append("}");
      return buf.toString();
   }

   private String makeShipList() {
      StringBuilder buf = new StringBuilder();
      buf.append("text{text=\"List contains the Ships that you can manage.\"}");
      buf.append("text{text=\"\"}");
      buf.append("text{type=\"bold\";text=\"Will have List of Ships that you may manage.\"}");
      if (this.includeAll) {
         buf.append(this.extraButton("Exclude ships without locks", "exc"));
      } else {
         buf.append(this.extraButton("Include ships without locks", "inc"));
      }

      Item[] items = Items.getManagedShipsFor(this.player, this.includeAll);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"6\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("Type", 2, this.sortBy)
            + this.colHeader("Owner?", 3, this.sortBy)
            + this.colHeader("Locked?", 4, this.sortBy)
            + "label{type=\"bold\";text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  return param1.getTypeName().compareTo(param2.getTypeName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  if (param1.isActualOwner(ManageObjectList.this.player.getWurmId()) == param2.isActualOwner(ManageObjectList.this.player.getWurmId())) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isActualOwner(ManageObjectList.this.player.getWurmId()) ? -1 * upDown : 1 * upDown;
                  }
               }
            });
            break;
         case 4:
            Arrays.sort(items, new Comparator<Item>() {
               public int compare(Item param1, Item param2) {
                  if (param1.isLocked() == param2.isLocked()) {
                     return param1.getObjectName().compareTo(param2.getObjectName()) * upDown;
                  } else {
                     return param1.isLocked() ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(Item item : items) {
         buf.append(
            (item.canHavePermissions() ? "radio{group=\"sel\";id=\"" + item.getWurmId() + "\";text=\"\"}" : "label{text=\"\"}")
               + "label{text=\""
               + item.getObjectName()
               + "\"};"
               + addRariryColour(item, item.getTypeName())
               + "label{"
               + this.showBoolean(item.isActualOwner(this.player.getWurmId()))
               + "};label{"
               + this.showBoolean(item.isLocked())
               + "};label{text=\"\"};"
         );
      }

      buf.append("}");
      return buf.toString();
   }

   private String makeWagonerList() {
      StringBuilder buf = new StringBuilder();
      int vid = -1;
      Creature[] animals = Creatures.getManagedWagonersFor(this.player, -1);
      int absSortBy = Math.abs(this.sortBy);
      final int upDown = Integer.signum(this.sortBy);
      buf.append(
         "table{rows=\"1\";cols=\"6\";label{text=\"\"};"
            + this.colHeader("Name", 1, this.sortBy)
            + this.colHeader("State", 2, this.sortBy)
            + this.colHeader("Queue", 3, this.sortBy)
            + "label{text=\"\"};label{text=\"\"};"
      );
      switch(absSortBy) {
         case 1:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  return param1.getName().compareTo(param2.getName()) * upDown;
               }
            });
            break;
         case 2:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  return param1.getWagoner().getStateName().compareTo(param2.getWagoner().getStateName()) * upDown;
               }
            });
            break;
         case 3:
            Arrays.sort(animals, new Comparator<Creature>() {
               public int compare(Creature param1, Creature param2) {
                  int value1 = param1.getWagoner().getQueueLength();
                  int value2 = param2.getWagoner().getQueueLength();
                  if (value1 == value2) {
                     return 0;
                  } else {
                     return value1 < value2 ? -1 * upDown : 1 * upDown;
                  }
               }
            });
      }

      for(Creature animal : animals) {
         Wagoner wagoner = animal.getWagoner();
         int queueLength = Delivery.getQueueLength(wagoner.getWurmId());
         buf.append(
            (animal.canHavePermissions() ? "radio{group=\"sel\";id=\"" + animal.getWurmId() + "\";text=\"\"}" : "label{text=\"\"};")
               + "label{text=\""
               + animal.getName()
               + "\"};label{text=\""
               + wagoner.getStateName()
               + "\"};"
               + (queueLength == 0 ? "label{text=\"empty\"};" : "label{text=\"" + queueLength + "\"};")
               + "label{text=\"  \"};"
         );
         if (animal.mayManage(this.getResponder())) {
            if (wagoner.getVillageId() == -1) {
               buf.append("label{\"Dismissing.\"};");
            } else {
               buf.append(this.dismissButton(animal));
            }
         } else {
            buf.append("label{\"\"};");
         }
      }

      buf.append("}");
      return buf.toString();
   }

   private String dismissButton(Creature animal) {
      StringBuilder buf = new StringBuilder();
      buf.append("harray{button{text=\"Dismiss\";id=\"dismiss" + animal.getWurmId() + "\";}label{text=\" \"}}");
      return buf.toString();
   }

   private String extraButton(String txt, String id) {
      StringBuilder buf = new StringBuilder();
      buf.append("harray{label{text=\"Filter list:\"};button{text=\"" + txt + "\";id=\"" + id + "\"}};");
      return buf.toString();
   }

   public static String addRariryColour(Item item, String name) {
      StringBuilder buf = new StringBuilder();
      if (item.getRarity() == 1) {
         buf.append("label{color=\"66,153,225\";text=\"rare " + name + "\"};");
      } else if (item.getRarity() == 2) {
         buf.append("label{color=\"0,255,255\";text=\"supreme " + name + "\"};");
      } else if (item.getRarity() == 3) {
         buf.append("label{color=\"255,0,255\";text=\"fantastic " + name + "\"};");
      } else {
         buf.append("label{text=\"" + name + "\"};");
      }

      return buf.toString();
   }

   private String showBoolean(boolean flag) {
      StringBuilder buf = new StringBuilder();
      if (flag) {
         buf.append("color=\"127,255,127\"");
      } else {
         buf.append("color=\"255,127,127\"");
      }

      buf.append("text=\"" + flag + "\"");
      return buf.toString();
   }

   public static enum Type {
      ANIMAL0("Animal", AnimalSettings.Animal0Permissions.values()),
      ANIMAL1("Animal", AnimalSettings.Animal1Permissions.values()),
      ANIMAL2("Animal", AnimalSettings.Animal2Permissions.values()),
      WAGONER("Wagoner", AnimalSettings.WagonerPermissions.values()),
      DELIVERY("Wagoner", AnimalSettings.WagonerPermissions.values()),
      BUILDING("Building", StructureSettings.StructurePermissions.values()),
      LARGE_CART("Large Cart", ItemSettings.VehiclePermissions.values()),
      DOOR("Door", DoorSettings.DoorPermissions.values()),
      GATE("Gate", DoorSettings.GatePermissions.values()),
      MINEDOOR("Minedoor", MineDoorSettings.MinedoorPermissions.values()),
      SHIP("Ship", ItemSettings.VehiclePermissions.values()),
      WAGON("Wagon", ItemSettings.WagonPermissions.values()),
      SHIP_CARRIER("Ship Carrier", ItemSettings.ShipTransporterPermissions.values()),
      CREATURE_CARRIER("Creature Carrier", ItemSettings.CreatureTransporterPermissions.values()),
      SMALL_CART("Small Cart", ItemSettings.SmallCartPermissions.values()),
      ITEM("Item", ItemSettings.ItemPermissions.values()),
      BED("Bed", ItemSettings.BedPermissions.values()),
      MESSAGE_BOARD("Village Message Board", ItemSettings.MessageBoardPermissions.values()),
      CORPSE("Corpse", ItemSettings.CorpsePermissions.values()),
      SEARCH("Search", null),
      REPLY("Reply", null);

      private final String title;
      private final Permissions.IPermission[] enumValues;

      private Type(String aTitle, Permissions.IPermission[] values) {
         this.title = aTitle;
         this.enumValues = values;
      }

      public String getTitle() {
         return this.title;
      }

      public Permissions.IPermission[] getEnumValues() {
         return this.enumValues;
      }
   }
}
