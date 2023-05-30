package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MountAction;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.DbItem;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.questions.SetDestinationQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class VehicleBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(VehicleBehaviour.class.getName());
   private static Vehicle vehicle;
   private static boolean addedPassenger;
   private static boolean addedDriver;

   public VehicleBehaviour() {
      super((short)41);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, target));
      toReturn.addAll(this.getVehicleBehaviours(performer, target));
      return toReturn;
   }

   private List<ActionEntry> getVehicleBehaviours(Creature performer, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (performer.getVehicle() == -10L
         && target.getOwnerId() == -10L
         && performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)
         && (performer.isSwimming() || performer.getFloorLevel() == target.getFloorLevel() || performer.getPower() > 0)) {
         VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), performer.isOnSurface());
         if (t == null
            || performer.getCurrentTile().getStructure() == t.getStructure()
            || performer.getCurrentTile().getStructure() != null && performer.getCurrentTile().getStructure().isTypeBridge()
            || t.getStructure() != null && t.getStructure().isTypeBridge()) {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null) {
               boolean havePermission = hasPermission(performer, target);
               addedPassenger = !havePermission && !target.mayPassenger(performer);
               addedDriver = !havePermission && !target.mayCommand(performer);
               VehicleBehaviour.vehicle = Vehicles.getVehicle(target);
               if (VehicleBehaviour.vehicle != null) {
                  if (!VehicleBehaviour.vehicle.isUnmountable()) {
                     for(int x = 0; x < VehicleBehaviour.vehicle.seats.length; ++x) {
                        if (!addedDriver && !VehicleBehaviour.vehicle.seats[x].isOccupied() && VehicleBehaviour.vehicle.seats[x].type == 0) {
                           if (!Items.isItemDragged(target)) {
                              toReturn.add(Actions.actionEntrys[331]);
                           }

                           addedDriver = true;
                        } else if (!addedPassenger && !VehicleBehaviour.vehicle.seats[x].isOccupied() && VehicleBehaviour.vehicle.seats[x].type == 1) {
                           if (VehicleBehaviour.vehicle.isChair()) {
                              toReturn.add(Actions.actionEntrys[701]);
                              if (VehicleBehaviour.vehicle.seats.length == 2
                                 && VehicleBehaviour.vehicle.seats[0].getType() == 1
                                 && VehicleBehaviour.vehicle.seats[1].getType() == 1) {
                                 if (!VehicleBehaviour.vehicle.seats[0].isOccupied()) {
                                    toReturn.add(Actions.actionEntrys[702]);
                                 }

                                 if (!VehicleBehaviour.vehicle.seats[1].isOccupied()) {
                                    toReturn.add(Actions.actionEntrys[703]);
                                 }
                              }
                           } else {
                              toReturn.add(Actions.actionEntrys[332]);
                           }

                           addedPassenger = true;
                           if (addedDriver) {
                              break;
                           }
                        }
                     }
                  }

                  if (VehicleBehaviour.vehicle.hitched.length > 0
                     && performer.getFollowers().length > 0
                     && !Items.isItemDragged(target)
                     && target.getTopParent() == target.getWurmId()
                     && (!target.isTent() || target.getLastOwnerId() == performer.getWurmId())) {
                     toReturn.add(Actions.actionEntrys[377]);
                  }
               }
            }
         }
      }

      if (performer.getVehicle() != -10L) {
         if (performer.getVehicle() == target.getWurmId() && target.isBoat()) {
            if (target.getData() != -1L && performer.isVehicleCommander()) {
               toReturn.add(Actions.actionEntrys[361]);
            }

            if (target.getExtra() != -1L) {
               toReturn.add(new ActionEntry((short)944, "Detach keep net", "detaching"));
            }

            toReturn.add(Actions.actionEntrys[383]);
            if ((Features.Feature.BOAT_DESTINATION.isEnabled() || performer.getPower() >= 2) && performer.isVehicleCommander()) {
               toReturn.add(Actions.actionEntrys[717]);
            }
         }

         Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
         if (vehicle.isChair()) {
            toReturn.add(Actions.actionEntrys[708]);
         } else {
            toReturn.add(Actions.actionEntrys[333]);
         }
      }

      if (target.getExtra() != -1L
         && performer.isWithinDistanceTo(
            target.getPosX(), target.getPosY(), target.getPosZ(), target.isVehicle() && !target.isTent() ? (float)Math.max(6, target.getSizeZ() / 100) : 6.0F
         )) {
         boolean watching = false;

         try {
            Item keepnet = Items.getItem(target.getExtra());
            Creature[] watchers = keepnet.getWatchers();

            for(Creature lWatcher : watchers) {
               if (lWatcher == performer) {
                  watching = true;
                  break;
               }
            }

            if (watching) {
               toReturn.add(Actions.actionEntrys[941]);
            }
         } catch (NoSuchCreatureException | NoSuchItemException var11) {
         }

         if (!watching) {
            toReturn.add(Actions.actionEntrys[940]);
         }
      }

      List<ActionEntry> permissions = new LinkedList<>();
      if (target.mayManage(performer)) {
         if (target.getTemplateId() == 186) {
            permissions.add(Actions.actionEntrys[687]);
         } else if (target.getTemplateId() == 539) {
            permissions.add(Actions.actionEntrys[665]);
         } else if (target.getTemplateId() == 850) {
            permissions.add(Actions.actionEntrys[669]);
         } else if (target.getTemplateId() == 853) {
            permissions.add(new ActionEntry((short)669, "Manage Ship Carrier", "managing"));
         } else if (target.getTemplateId() == 1410) {
            permissions.add(new ActionEntry((short)669, "Manage creature transporter", "managing"));
         } else if (target.isBoat()) {
            permissions.add(Actions.actionEntrys[668]);
         }
      }

      if (target.maySeeHistory(performer)) {
         if (target.getTemplateId() == 186) {
            permissions.add(new ActionEntry((short)691, "History of Small Cart", "viewing"));
         } else if (target.getTemplateId() == 539) {
            permissions.add(new ActionEntry((short)691, "History of Large Cart", "viewing"));
         } else if (target.getTemplateId() == 850) {
            permissions.add(new ActionEntry((short)691, "History of Wagon", "viewing"));
         } else if (target.getTemplateId() == 853) {
            permissions.add(new ActionEntry((short)691, "History of Ship Carrier", "viewing"));
         } else if (target.getTemplateId() == 1410) {
            permissions.add(new ActionEntry((short)691, "History of Creature Transporter", "viewing"));
         } else if (target.isBoat()) {
            permissions.add(new ActionEntry((short)691, "History of Ship", "viewing"));
         }
      }

      if (!permissions.isEmpty()) {
         if (permissions.size() > 1) {
            Collections.sort(permissions);
            toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
         }

         toReturn.addAll(permissions);
      }

      if (target.getWurmId() == performer.getVehicle()) {
         if (performer.isVehicleCommander()) {
            VehicleBehaviour.vehicle = Vehicles.getVehicle(target);
            if (VehicleBehaviour.vehicle != null && VehicleBehaviour.vehicle.getDraggers() != null && VehicleBehaviour.vehicle.getDraggers().size() > 0) {
               toReturn.add(new ActionEntry((short)-1, "Animals", "Animal options"));
               toReturn.add(Actions.actionEntrys[378]);
            }
         }
      } else if (performer.getVehicle() == -10L && (hasKeyForVehicle(performer, target) || mayDriveVehicle(performer, target, null))) {
         VehicleBehaviour.vehicle = Vehicles.getVehicle(target);
         if (VehicleBehaviour.vehicle != null && VehicleBehaviour.vehicle.getDraggers() != null && VehicleBehaviour.vehicle.getDraggers().size() > 0) {
            toReturn.add(new ActionEntry((short)-1, "Animals", "Animal options"));
            toReturn.add(Actions.actionEntrys[378]);
         }
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(super.getBehavioursFor(performer, source, target));
      toReturn.addAll(this.getVehicleBehaviours(performer, target));
      if (performer.getVehicle() == target.getWurmId() && target.isBoat()) {
         if (target.getData() == -1L && source.isAnchor()) {
            toReturn.add(Actions.actionEntrys[360]);
         }

         if ((target.getTemplateId() == 490 || target.getTemplateId() == 491) && target.getExtra() == -1L && source.getTemplateId() == 1342) {
            toReturn.add(new ActionEntry((short)943, "Attach keep net", "attaching"));
         }

         if (source.isDredgingTool() && target.isBoat() && performer.getVehicle() == target.getWurmId() && performer.isOnSurface()) {
            toReturn.add(Actions.actionEntrys[362]);
         }

         if (source.getTemplateId() == 1344 || source.getTemplateId() == 1346) {
            toReturn.add(Actions.actionEntrys[160]);
         }

         if (source.getTemplateId() == 1344 || source.getTemplateId() == 1346) {
            toReturn.add(new ActionEntry((short)285, "Lore", "thinking"));
         }
      }

      return toReturn;
   }

   public static final boolean hasKeyForVehicle(Creature performer, Item aVehicle) {
      if (aVehicle.isTent()) {
         return aVehicle.getLastOwnerId() == performer.getWurmId();
      } else {
         long lockId = aVehicle.getLockId();
         if (lockId != -10L) {
            try {
               Item lock = Items.getItem(lockId);
               if (performer.hasKeyForLock(lock)) {
                  return true;
               }

               return false;
            } catch (NoSuchItemException var6) {
            }
         }

         VolaTile vt = Zones.getTileOrNull(aVehicle.getTileX(), aVehicle.getTileY(), aVehicle.isOnSurface());
         Village vill = vt == null ? null : vt.getVillage();
         return vill == null || vill.isActionAllowed((short)6, performer);
      }
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean done = true;
      if (action == 331 || action == 332 || action == 701 || action == 702 || action == 703) {
         if (target.getTopParent() != target.getWurmId()) {
            performer.getCommunicator()
               .sendNormalServerMessage(StringUtil.format("You can't embark upon the %s right now, it needs to be on the ground.", target.getName()));
            return true;
         }

         if (performer.getFloorLevel() != target.getFloorLevel() && !performer.isSwimming()) {
            performer.getCommunicator().sendNormalServerMessage("You are too far away now.");
            return done;
         }

         if (!GeneralUtilities.isOnSameLevel(performer, target)) {
            performer.getCommunicator().sendNormalServerMessage("You must be on the same level to embark.");
         }

         if (performer.isClimbing()) {
            performer.getCommunicator().sendNormalServerMessage("You need to stop climbing first.");
            return done;
         }

         if (Math.abs(target.getPosZ() - performer.getPositionZ()) > 4.0F) {
            performer.getCommunicator().sendNormalServerMessage("You need to get closer to the " + target.getName() + ".");
            return done;
         }

         if (target.isChair() && target.getPosZ() < 0.0F) {
            performer.getCommunicator().sendNormalServerMessage("You cannot sit on " + target.getName() + ". It is too wet.");
            return done;
         }

         if (target.isOwnedByWagoner()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot use the " + target.getName() + "  as a wagoner owns it.");
            return done;
         }

         if (performer.getPower() < 2 && action == 331 && targetHasActiveQueen(target, performer)) {
            performer.getCommunicator().sendNormalServerMessage("The bees sting you.");
         }

         if (performer.getVehicle() == -10L && target.getOwnerId() == -10L) {
            if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
               if (target.getCurrentQualityLevel() < 10.0F) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is in too poor shape to be used.");
                  return true;
               }

               if (target.isOnSurface() != performer.isOnSurface()) {
                  if (performer.isOnSurface()) {
                     performer.getCommunicator().sendNormalServerMessage("You need to enter the cave first.");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You need to leave the cave first.");
                  }

                  return true;
               }

               if (performer.getBridgeId() != target.getBridgeId()) {
                  performer.getCommunicator().sendNormalServerMessage("You need to be in the same structure as the " + target.getName() + ".");
                  return true;
               }

               vehicle = Vehicles.getVehicle(target);
               if (vehicle != null) {
                  String vehicName = Vehicle.getVehicleName(vehicle);
                  if (action == 331) {
                     if (!hasKeyForVehicle(performer, target) && !mayDriveVehicle(performer, target, act)) {
                        String text = "You are not allowed to %s the %s as a %s.";
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              StringUtil.format("You are not allowed to %s the %s as a %s.", vehicle.embarkString, vehicName, vehicle.pilotName)
                           );
                     } else if (!Items.isItemDragged(target)) {
                        if (canBeDriverOfVehicle(performer, vehicle)) {
                           addedDriver = false;

                           for(int x = 0; x < vehicle.seats.length; ++x) {
                              if (!vehicle.seats[x].isOccupied() && vehicle.seats[x].type == 0) {
                                 float r = -(target.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                                 float s = (float)Math.sin((double)r);
                                 float c = (float)Math.cos((double)r);
                                 float xo = s * -vehicle.seats[x].offx - c * -vehicle.seats[x].offy;
                                 float yo = c * -vehicle.seats[x].offx + s * -vehicle.seats[x].offy;
                                 float newposx = target.getPosX() + xo;
                                 float newposy = target.getPosY() + yo;
                                 BlockingResult result = Blocking.getBlockerBetween(
                                    performer,
                                    performer.getPosX(),
                                    performer.getPosY(),
                                    newposx,
                                    newposy,
                                    performer.getPositionZ(),
                                    target.getPosZ(),
                                    performer.isOnSurface(),
                                    target.isOnSurface(),
                                    false,
                                    4,
                                    -1L,
                                    performer.getBridgeId(),
                                    target.getBridgeId(),
                                    false
                                 );
                                 if (result == null) {
                                    TileEvent.log(target.getTileX(), target.getTileY(), target.isOnSurface() ? 0 : -1, performer.getWurmId(), action);
                                    addedDriver = true;
                                    vehicle.seats[x].occupy(vehicle, performer);
                                    vehicle.pilotId = performer.getWurmId();
                                    performer.setVehicleCommander(true);
                                    MountAction m = new MountAction(null, target, vehicle, x, true, vehicle.seats[x].offz);
                                    performer.setMountAction(m);
                                    performer.setVehicle(target.getWurmId(), true, (byte)0);
                                    if (performer.isPlayer() && ((Player)performer).getAlcohol() > 5.0F) {
                                       if (target.isBoat()) {
                                          performer.achievement(133);
                                       } else if (!target.isChair()) {
                                          performer.achievement(134);
                                       }
                                    }

                                    switch(target.getTemplateId()) {
                                       case 490:
                                          performer.achievement(57);
                                          break;
                                       case 491:
                                          performer.achievement(58);
                                          break;
                                       case 540:
                                          performer.achievement(54);
                                          break;
                                       case 541:
                                          performer.achievement(56);
                                          break;
                                       case 542:
                                          performer.achievement(55);
                                          break;
                                       case 543:
                                          performer.achievement(59);
                                    }

                                    if (vehicle.hasDestinationSet()) {
                                       ServerEntry entry = vehicle.getDestinationServer();
                                       if (Servers.mayEnterServer(performer, entry)
                                          && (!entry.PVPSERVER && !entry.isChaosServer() || !((Player)performer).isBlockingPvP())) {
                                          performer.getCommunicator()
                                             .sendAlertServerMessage("The " + vehicName + " is on a course for " + entry.getName() + ".");
                                       } else {
                                          vehicle.clearDestination();
                                          performer.getCommunicator().sendAlertServerMessage("The previous course is unavailable and has been cleared.");
                                       }
                                    }

                                    if (target.isBoat() && !PlonkData.ON_A_BOAT.hasSeenThis(performer)) {
                                       PlonkData.ON_A_BOAT.trigger(performer);
                                    }
                                    break;
                                 }
                              }
                           }

                           if (!addedDriver) {
                              String text = "You may not %s the %s as a %s right now.The space is occupied or unreachable.";
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    StringUtil.format(
                                       "You may not %s the %s as a %s right now.The space is occupied or unreachable.",
                                       vehicle.embarkString,
                                       vehicName,
                                       vehicle.pilotName
                                    )
                                 );
                           }
                        } else {
                           String text = "You are not smart enough to figure out how to be the %s of the %s. You need %.2f in %s.";
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 StringUtil.format(
                                    "You are not smart enough to figure out how to be the %s of the %s. You need %.2f in %s.",
                                    vehicle.pilotName,
                                    vehicName,
                                    vehicle.skillNeeded,
                                    SkillSystem.getNameFor(100)
                                 )
                              );
                        }
                     } else {
                        String text = "You may not %s the %s as a %s right now. It is being dragged.";
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              StringUtil.format(
                                 "You may not %s the %s as a %s right now. It is being dragged.", vehicle.embarkString, vehicName, vehicle.pilotName
                              )
                           );
                     }
                  } else if (action == 332 || action == 701 || action == 702 || action == 703) {
                     this.actionEmbarkPassenger(performer, target, action);
                  }
               } else if (action != 701 && action != 702 && action != 703) {
                  performer.getCommunicator().sendNormalServerMessage("You can't embark on that right now.");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You can't sit on that right now.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You are too far away now.");
            }
         }
      } else if (Actions.isActionDisembark(action)) {
         if (performer.getVehicle() != -10L) {
            if (performer.getVisionArea() != null) {
               performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
            }

            performer.disembark(true);
         }
      } else if (action == 361) {
         done = this.actionRaiseAnchor(performer, target, counter, done);
      } else if (action == 944) {
         done = this.actionDetachKeepnet(act, performer, target, counter, done);
      } else if (action == 940) {
         if (target.getExtra() != -1L && performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
            try {
               Item keepnet = Items.getItem(target.getExtra());
               if (performer.addItemWatched(keepnet)) {
                  performer.getCommunicator().sendOpenInventoryWindow(keepnet.getWurmId(), keepnet.getName());
                  keepnet.addWatcher(keepnet.getWurmId(), performer);
                  keepnet.sendContainedItems(keepnet.getWurmId(), performer);
               }

               performer.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
            } catch (NoSuchItemException var23) {
               logger.log(Level.WARNING, var23.getMessage(), (Throwable)var23);
            }
         }
      } else {
         if (action == 941) {
            if (target.getExtra() != -1L) {
               try {
                  Item keepnet = Items.getItem(target.getExtra());
                  keepnet.close(performer);
                  performer.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
               } catch (NoSuchItemException var19) {
                  logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
               }
            }

            return true;
         }

         if (action != 717 || !Features.Feature.BOAT_DESTINATION.isEnabled() && performer.getPower() < 2) {
            if (action == 717 && Features.Feature.BOAT_DESTINATION.isEnabled()) {
               SetDestinationQuestion sdq = new SetDestinationQuestion(performer, target);
               sdq.sendQuestion();
               done = true;
            } else if (action == 378) {
               boolean ok = false;
               if (target.getWurmId() == performer.getVehicle()) {
                  if (performer.isVehicleCommander()) {
                     ok = true;
                  }
               } else if (performer.getVehicle() == -10L && (hasKeyForVehicle(performer, target) || mayDriveVehicle(performer, target, act))) {
                  ok = true;
               }

               if (!ok) {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to do that.");
                  return true;
               }

               if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
                  performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
                  return true;
               }

               vehicle = Vehicles.getVehicle(target);
               if (vehicle != null && vehicle.draggers != null && vehicle.draggers.size() > 0) {
                  Creature[] crets = vehicle.draggers.toArray(new Creature[vehicle.draggers.size()]);

                  for(int x = 0; x < crets.length; ++x) {
                     if (!vehicle.positionDragger(crets[x], performer)) {
                        performer.getCommunicator().sendNormalServerMessage("You can't unhitch the " + crets[x].getName() + " here. Please move the vehicle.");
                        return true;
                     }

                     Zone z = null;

                     try {
                        z = Zones.getZone(crets[x].getTileX(), crets[x].getTileY(), crets[x].isOnSurface());
                        crets[x].getStatus().savePosition(crets[x].getWurmId(), true, z.getId(), true);
                     } catch (Exception var22) {
                     }

                     Creatures.getInstance().setLastLed(crets[x].getWurmId(), performer.getWurmId());
                     vehicle.removeDragger(crets[x]);
                     VolaTile t = crets[x].getCurrentTile();
                     if (t != null) {
                        t.sendAttachCreature(crets[x].getWurmId(), -10L, 0.0F, 0.0F, 0.0F, 0);
                        if (z != null) {
                           try {
                              z.removeCreature(crets[x], true, false);
                              z.addCreature(crets[x].getWurmId());
                           } catch (NoSuchCreatureException var20) {
                           } catch (NoSuchPlayerException var21) {
                           }
                        }
                     }
                  }
               }
            } else if (action == 383) {
               if (performer.getVehicle() == target.getWurmId()) {
                  Item[] its = target.getAllItems(true);
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " contains " + its.length + " items.");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You miscalculate.. 200-43.. is that 12? No. No that can't be it. Damn it's so hard.");
               }

               done = true;
            } else if (action == 687) {
               if (target.mayManage(performer)) {
                  ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.SMALL_CART, target, false, -10L, false, null, "");
                  mp.sendQuestion();
               }
            } else if (action == 665) {
               if (target.mayManage(performer)) {
                  ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.LARGE_CART, target, false, -10L, false, null, "");
                  mp.sendQuestion();
               }
            } else if (action == 669) {
               if (target.mayManage(performer)) {
                  ManageObjectList.Type molt = ManageObjectList.Type.WAGON;
                  if (target.getTemplateId() == 853) {
                     molt = ManageObjectList.Type.SHIP_CARRIER;
                  } else if (target.getTemplateId() == 1410) {
                     molt = ManageObjectList.Type.CREATURE_CARRIER;
                  }

                  ManagePermissions mp = new ManagePermissions(performer, molt, target, false, -10L, false, null, "");
                  mp.sendQuestion();
               }
            } else if (action == 668) {
               if (target.mayManage(performer)) {
                  ManagePermissions mp = new ManagePermissions(performer, ManageObjectList.Type.SHIP, target, false, -10L, false, null, "");
                  mp.sendQuestion();
               }
            } else if (action == 691) {
               if (target.maySeeHistory(performer)) {
                  PermissionsHistory ph = new PermissionsHistory(performer, target.getWurmId());
                  ph.sendQuestion();
               }
            } else if (action == 377) {
               if (performer.getDraggedItem() != null && performer.getDraggedItem() == target) {
                  performer.getCommunicator().sendNormalServerMessage("You must stop dragging the " + target.getName() + " before you hitch creatures to it.");
                  return true;
               }

               if (target.getTopParent() != target.getWurmId()) {
                  String message = "The %s needs to be on the ground before you can hitch anything to it.";
                  performer.getCommunicator()
                     .sendNormalServerMessage(StringUtil.format("The %s needs to be on the ground before you can hitch anything to it.", target.getName()));
                  return true;
               }

               if (Items.isItemDragged(target)) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is dragged and you can not hitch creatures to it.");
                  return true;
               }

               if (target.getCurrentQualityLevel() < 10.0F && !target.isTent()) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is in too poor shape to be dragged by animals.");
                  return true;
               }

               if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 8.0F)) {
                  if (!hasKeyForVehicle(performer, target) && !mayDriveVehicle(performer, target, act)) {
                     performer.getCommunicator().sendNormalServerMessage("You can't mount that right now so you can't hitch either.");
                  } else if (!target.isTent() || target.getLastOwnerId() == performer.getWurmId()) {
                     vehicle = Vehicles.getVehicle(target);
                     this.actionHitch(performer, target);
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You can't reach that right now.");
               }

               done = true;
            } else {
               done = super.action(act, performer, target, action, counter);
            }
         } else {
            SetDestinationQuestion sdq = new SetDestinationQuestion(performer, target);
            sdq.sendQuestion();
            done = true;
         }
      }

      return done;
   }

   private static final boolean targetHasActiveQueen(Item carrier, Creature performer) {
      for(Item item : carrier.getItemsAsArray()) {
         if (item.getTemplateId() == 1175 && item.hasQueen() && !WurmCalendar.isSeasonWinter() && performer.getBestBeeSmoker() == null) {
            performer.addWoundOfType(null, (byte)5, 2, true, 1.0F, false, (double)(4000.0F + Server.rand.nextFloat() * 3000.0F), 0.0F, 0.0F, false, false);
            return true;
         }
      }

      return false;
   }

   private void actionEmbarkPassenger(Creature performer, Item target, short action) {
      if (vehicle.hasDestinationSet() && vehicle.getDestinationServer().PVPSERVER && ((Player)performer).isBlockingPvP()) {
         performer.getCommunicator()
            .sendAlertServerMessage(
               "The "
                  + Vehicle.getVehicleName(vehicle)
                  + " is on a course for hostile territory, but you have elected to avoid hostility. You may change this preference in your profile."
            );
      } else {
         if (!hasKeyForVehicle(performer, target) && !mayEmbarkVehicle(performer, target)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to " + vehicle.embarkString + " the " + target.getName() + ".");
         } else if (performer.getDraggedItem() != null && performer.getDraggedItem().getWurmId() == target.getWurmId()) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  StringUtil.format("You may not %s the %s since you are dragging it.", vehicle.embarkString, Vehicle.getVehicleName(vehicle))
               );
         } else {
            addedPassenger = false;
            TileEvent.log(target.getTileX(), target.getTileY(), target.isOnSurface() ? 0 : -1, performer.getWurmId(), action);
            boolean wallInWay = false;
            if (action != 332 && action != 701) {
               if (action == 702) {
                  wallInWay = addPassenger(performer, vehicle, target, 0) ? true : wallInWay;
               } else if (action == 703) {
                  wallInWay = addPassenger(performer, vehicle, target, 1) ? true : wallInWay;
               }
            } else {
               for(int x = 0; x < vehicle.seats.length; ++x) {
                  wallInWay = addPassenger(performer, vehicle, target, x) ? true : wallInWay;
                  if (addedPassenger) {
                     break;
                  }
               }
            }

            if (!addedPassenger) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     StringUtil.format("You may not %s the %s as a passenger right now.", vehicle.embarkString, Vehicle.getVehicleName(vehicle))
                  );
               if (wallInWay) {
                  performer.getCommunicator().sendNormalServerMessage("The wall is in the way. You can not reach a seat.");
               } else {
                  performer.getCommunicator().sendNormalServerMessage("The seats are all occupied.");
               }
            }
         }
      }
   }

   public static boolean addPassenger(Creature performer, Vehicle vehicle, Item target, int seatNum) {
      boolean wallInWay = false;
      if (seatNum >= 0 && seatNum < vehicle.seats.length && !vehicle.seats[seatNum].isOccupied() && vehicle.seats[seatNum].type == 1) {
         float r = -(target.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
         float s = (float)Math.sin((double)r);
         float c = (float)Math.cos((double)r);
         float xo = s * -vehicle.seats[seatNum].offx - c * -vehicle.seats[seatNum].offy;
         float yo = c * -vehicle.seats[seatNum].offx + s * -vehicle.seats[seatNum].offy;
         float newposx = target.getPosX() + xo;
         float newposy = target.getPosY() + yo;
         BlockingResult result = Blocking.getBlockerBetween(
            performer,
            performer.getPosX(),
            performer.getPosY(),
            newposx,
            newposy,
            performer.getPositionZ(),
            target.getPosZ(),
            performer.isOnSurface(),
            target.isOnSurface(),
            false,
            4,
            -1L,
            performer.getBridgeId(),
            target.getBridgeId(),
            false
         );
         if (result == null) {
            addedPassenger = true;
            vehicle.seats[seatNum].occupy(vehicle, performer);
            MountAction m = new MountAction(null, target, vehicle, seatNum, false, vehicle.seats[seatNum].offz);
            performer.setMountAction(m);
            performer.setVehicle(target.getWurmId(), true, (byte)1);
            if (performer.isPlayer() && ((Player)performer).getAlcohol() > 5.0F && target.isBoat()) {
               performer.achievement(133);
            }

            if (vehicle.hasDestinationSet()) {
               ServerEntry entry = vehicle.getDestinationServer();
               if (entry.PVPSERVER && (!entry.EPIC || Server.getInstance().isPS())) {
                  byte pKingdom = ((Player)performer).getSaveFile().getChaosKingdom() == 0 ? 4 : ((Player)performer).getSaveFile().getChaosKingdom();
                  String toWhere = "The " + vehicle.getName() + " will be heading to " + entry.getName() + ", which is hostile territory";
                  if (pKingdom != performer.getKingdomId()) {
                     toWhere = toWhere + " and you will join the " + Kingdoms.getNameFor(pKingdom) + " kingdom until you return";
                  }

                  performer.getCommunicator().sendAlertServerMessage(toWhere + ".");
                  vehicle.alertPassengerOfEnemies(performer, entry, true);
               } else {
                  performer.getCommunicator().sendAlertServerMessage("The " + vehicle.getName() + " will be heading to " + entry.getName() + ".");
               }
            }

            if (target.isBoat() && !PlonkData.ON_A_BOAT.hasSeenThis(performer)) {
               PlonkData.ON_A_BOAT.trigger(performer);
            }
         } else {
            wallInWay = true;
         }
      }

      return wallInWay;
   }

   private boolean actionRaiseAnchor(Creature performer, Item target, float counter, boolean done) {
      if (performer.getVehicle() != -10L
         && performer.getVehicle() == target.getWurmId()
         && target.isBoat()
         && target.getData() != -1L
         && performer.isVehicleCommander()) {
         try {
            Item anchor = Items.getItem(target.getData());
            done = false;
            if (counter == 1.0F) {
               performer.getCommunicator().sendNormalServerMessage("You start to raise the " + anchor.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to raise the " + anchor.getName() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[361].getVerbString(), true, 100);
            }

            if (counter > 10.0F) {
               done = true;
               performer.getInventory().insertItem(anchor, true);
               target.setData(-1L);
               performer.getCommunicator().sendNormalServerMessage("You raise the " + anchor.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " raises the " + anchor.getName() + ".", performer, 5);
               Vehicle veh = Vehicles.getVehicle(target);

               try {
                  Creature pilot = Server.getInstance().getCreature(veh.getPilotId());
                  pilot.getMovementScheme().addWindImpact(veh.getWindImpact());
                  pilot.getMovementScheme().setMooredMod(false);
               } catch (NoSuchCreatureException var8) {
               } catch (NoSuchPlayerException var9) {
               }
            }
         } catch (NoSuchItemException var10) {
            logger.log(Level.INFO, "No such anchor item.");
         }
      }

      return done;
   }

   private boolean actionDetachKeepnet(Action act, Creature performer, Item target, float counter, boolean done) {
      if (performer.getVehicle() != -10L && performer.getVehicle() == target.getWurmId() && target.isBoat() && target.getExtra() != -1L) {
         try {
            Item keepnet = Items.getItem(target.getExtra());
            if (!keepnet.isEmpty(false)) {
               performer.getCommunicator().sendNormalServerMessage("The " + keepnet.getName() + " must be empty to be detached.");
               return true;
            }

            done = false;
            int time = 20;
            if (counter == 1.0F) {
               performer.getCommunicator().sendNormalServerMessage("You start to detach the " + keepnet.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to detach a " + keepnet.getName() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[944].getVerbString(), true, time);
               act.setTimeLeft(time);
            }

            time = act.getTimeLeft();
            if (counter * 10.0F > (float)time) {
               try {
                  for(Creature c : keepnet.getWatchers()) {
                     keepnet.close(c);
                     c.getCommunicator().sendUpdateSelectBar(target.getWurmId(), false);
                  }
               } catch (NoSuchCreatureException var12) {
               }

               done = true;
               performer.getInventory().insertItem(keepnet, true);
               target.setExtra(-1L);
               keepnet.setData(-1L);
               VolaTile vt = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
               vt.sendBoatDetachment(target.getWurmId(), keepnet.getTemplateId(), (byte)1);
               performer.getCommunicator().sendNormalServerMessage("You detach the " + keepnet.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " detachs the " + keepnet.getName() + ".", performer, 5);
            }
         } catch (NoSuchItemException var13) {
            logger.log(Level.INFO, "No such keepnet item.");
         }
      }

      return done;
   }

   private void actionHitch(Creature performer, Item target) {
      if (vehicle.hitched.length > 0) {
         if (vehicle.hasHumanDragger()) {
            performer.getCommunicator().sendNormalServerMessage("Someone is dragging the " + target.getName().toLowerCase() + " already.");
            return;
         }

         if (vehicle.mayAddDragger()) {
            Creature[] folls = performer.getFollowers();
            if (folls.length > 0) {
               for(int x = 0; x < folls.length; ++x) {
                  if (folls[x].isOnSurface() == target.isOnSurface()) {
                     if (!folls[x].isRidden()) {
                        if (!folls[x].isDomestic() && !(folls[x].getStatus().getBattleRatingTypeModifier() <= 1.2F)) {
                           performer.getCommunicator()
                              .sendNormalServerMessage(StringUtil.format("The %s is too unruly to be hitched.", StringUtil.toLowerCase(folls[x].getName())));
                        } else if (this.isStrongEnoughToDrag(folls[x], target)) {
                           if (folls[x].canUseWithEquipment()) {
                              if (vehicle.addDragger(folls[x])) {
                                 folls[x].setLeader(null);
                                 folls[x].setHitched(vehicle, false);
                                 folls[x].setVisible(false);
                                 performer.getCommunicator()
                                    .sendNormalServerMessage(
                                       StringUtil.format(
                                          "You hitch the %s to the %s.", StringUtil.toLowerCase(folls[x].getName()), StringUtil.toLowerCase(target.getName())
                                       )
                                    );
                                 Seat driverseat = vehicle.getHitchSeatFor(folls[x].getWurmId());
                                 float _r = (-target.getRotation() + 180.0F) * (float) Math.PI / 180.0F;
                                 float _s = (float)Math.sin((double)_r);
                                 float _c = (float)Math.cos((double)_r);
                                 float xo = _s * -driverseat.offx - _c * -driverseat.offy;
                                 float yo = _c * -driverseat.offx + _s * -driverseat.offy;
                                 float nPosX = target.getPosX() + xo;
                                 float nPosY = target.getPosY() + yo;
                                 float nPosZ = target.getPosZ() + driverseat.offz;
                                 folls[x].getStatus().setPositionX(nPosX);
                                 folls[x].getStatus().setPositionY(nPosY);
                                 folls[x].setRotation(-target.getRotation());
                                 folls[x]
                                    .getMovementScheme()
                                    .setPosition(
                                       folls[x].getStatus().getPositionX(),
                                       folls[x].getStatus().getPositionY(),
                                       nPosZ,
                                       folls[x].getStatus().getRotation(),
                                       folls[x].getLayer()
                                    );
                                 folls[x]
                                    .getCurrentTile()
                                    .sendAttachCreature(
                                       folls[x].getWurmId(),
                                       target.getWurmId(),
                                       vehicle.getHitchSeatFor(folls[x].getWurmId()).offx,
                                       vehicle.getHitchSeatFor(folls[x].getWurmId()).offy,
                                       vehicle.getHitchSeatFor(folls[x].getWurmId()).offz,
                                       vehicle.getSeatNumberFor(vehicle.getHitchSeatFor(folls[x].getWurmId()))
                                    );
                                 folls[x].setVisible(true);
                                 break;
                              }

                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    StringUtil.format("The %s could not be hitched right now.", StringUtil.toLowerCase(folls[x].getName()))
                                 );
                           } else {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    StringUtil.format(
                                       "The %s looks confused by its equipment and refuses to move.", StringUtil.toLowerCase(folls[x].getName())
                                    )
                                 );
                           }
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage(StringUtil.format("The %s is too weak.", StringUtil.toLowerCase(folls[x].getName())));
                        }
                     } else {
                        performer.getCommunicator()
                           .sendNormalServerMessage(StringUtil.format("The %s is ridden and may not drag now.", StringUtil.toLowerCase(folls[x].getName())));
                     }
                  } else {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           StringUtil.format(
                              "The %s is not close enough to the %s.", StringUtil.toLowerCase(folls[x].getName()), StringUtil.toLowerCase(target.getName())
                           )
                        );
                  }
               }
            } else {
               performer.getCommunicator()
                  .sendNormalServerMessage(StringUtil.format("You have no creature to hitch to the %s.", StringUtil.toLowerCase(target.getName())));
            }
         } else {
            performer.getCommunicator()
               .sendNormalServerMessage(StringUtil.format("The %s has no spaces left to hitch to.", StringUtil.toLowerCase(target.getName())));
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage(StringUtil.format("The %s has no spaces to hitch to.", StringUtil.toLowerCase(target.getName())));
      }
   }

   public boolean isStrongEnoughToDrag(Creature creature, Item aVehicle) {
      return creature.getStrengthSkill() > (double)((float)aVehicle.getTemplate().getWeightGrams() / 10000.0F);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = true;
      if (action != 331
         && action != 332
         && action != 333
         && action != 708
         && action != 361
         && action != 383
         && action != 378
         && action != 687
         && action != 665
         && action != 669
         && action != 668
         && action != 691
         && action != 944
         && action != 940) {
         if (action == 671 || action == 672) {
            if (source.getTemplateId() != 319 && source.getTemplateId() != 1029) {
               return true;
            }

            return CargoTransportationMethods.haul(performer, target, counter, action, act);
         }

         if (action == 360) {
            if (performer.getVehicle() == target.getWurmId() && target.isBoat() && target.getData() == -1L && source.isAnchor()) {
               done = false;
               if (counter == 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You start to moor the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to moor the " + target.getName() + ".", performer, 5);
                  performer.sendActionControl(Actions.actionEntrys[360].getVerbString(), true, 10);
               }

               if (counter > 1.0F) {
                  done = true;
                  source.putInVoid();
                  target.setData(source.getWurmId());
                  Vehicle veh = Vehicles.getVehicle(target);

                  try {
                     Creature pilot = Server.getInstance().getCreature(veh.getPilotId());
                     pilot.getMovementScheme().addWindImpact((byte)0);
                     pilot.getMovementScheme().setMooredMod(true);
                  } catch (NoSuchCreatureException var11) {
                  } catch (NoSuchPlayerException var12) {
                  }

                  target.savePosition();
                  performer.getCommunicator().sendNormalServerMessage("You moor the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " moors the " + target.getName() + ".", performer, 5);
                  if (Item.getMaterialAnchorBonus(source.getMaterial()) < 1.0F) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You're unsure if the "
                              + source.getName()
                              + " is of a heavy enough material to completely stop the "
                              + target.getName()
                              + " from drifting."
                        );
                  }
               }
            }
         } else if (action == 943) {
            if (performer.getVehicle() == target.getWurmId()
               && (target.getTemplateId() == 490 || target.getTemplateId() == 491)
               && target.getExtra() == -1L
               && source.getTemplateId() == 1342) {
               done = false;
               int time = 20;
               if (counter == 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You start to attach the " + source.getName() + " to the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to attach a " + source.getName() + ".", performer, 5);
                  performer.sendActionControl(Actions.actionEntrys[943].getVerbString(), true, time);
                  act.setTimeLeft(time);
               }

               time = act.getTimeLeft();
               if (counter * 10.0F > (float)time) {
                  done = true;
                  source.putInVoid();
                  target.setExtra(source.getWurmId());
                  source.setData(target.getWurmId());
                  ((DbItem)target).maybeUpdateKeepnetPos();
                  VolaTile vt = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
                  vt.sendBoatAttachment(target.getWurmId(), source.getTemplateId(), source.getMaterial(), (byte)1, source.getAuxData());
                  performer.getCommunicator().sendNormalServerMessage("You attach the " + source.getName() + " to the " + target.getName() + ".");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " attaches a " + source.getName() + " to the " + target.getName() + ".", performer, 5);
               }
            }
         } else if (action == 362) {
            done = true;
            if (performer.getVehicle() == target.getWurmId() && target.isBoat()) {
               if (source.isDredgingTool()) {
                  if (performer.isOnSurface()) {
                     try {
                        Item boat = Items.getItem(performer.getVehicle());
                        if (boat.isOnSurface() && boat.getPosZ() <= 0.0F) {
                           int tile = Server.surfaceMesh.getTile(boat.getTileX(), boat.getTileY());
                           if (!Terraforming.isNonDiggableTile(Tiles.decodeType(tile))) {
                              done = Terraforming.dig(
                                 performer,
                                 source,
                                 boat.getTileX(),
                                 boat.getTileY(),
                                 tile,
                                 counter,
                                 false,
                                 performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh
                              );
                           } else {
                              performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
                           }
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
                        }
                     } catch (NoSuchItemException var14) {
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You may not dredge here.");
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You may not use that.");
               }
            }
         } else if (action == 160) {
            try {
               Item boat = Items.getItem(performer.getVehicle());
               int tile = Server.surfaceMesh.getTile(boat.getTileX(), boat.getTileY());
               if (!performer.isOnSurface()) {
                  tile = Server.caveMesh.getTile(boat.getTileX(), boat.getTileY());
               }

               if (source.getTemplateId() != 1344 && source.getTemplateId() != 1346) {
                  done = true;
               } else {
                  done = MethodsFishing.fish(performer, source, boat.getTileX(), boat.getTileY(), tile, counter, act);
               }
            } catch (NoSuchItemException var13) {
            }
         } else if (action == 285) {
            done = true;
            if (source.getTemplateId() == 1344 || source.getTemplateId() == 1346) {
               try {
                  Item boat = Items.getItem(performer.getVehicle());
                  done = MethodsFishing.showFishTable(performer, source, boat.getTileX(), boat.getTileY(), counter, act);
               } catch (NoSuchItemException var10) {
               }
            }
         } else {
            done = super.action(act, performer, source, target, action, counter);
         }
      } else {
         done = this.action(act, performer, target, action, counter);
      }

      return done;
   }

   public static boolean canBeDriverOfVehicle(Creature aPerformer, Vehicle aVehicle) {
      boolean toReturn = false;
      if (aVehicle != null && aPerformer != null) {
         if (aVehicle.isUnmountable()) {
            return false;
         }

         Skill checkSkill = null;
         if (aVehicle.creature) {
            try {
               checkSkill = aPerformer.getSkills().getSkill(104);
            } catch (NoSuchSkillException var6) {
               logger.log(Level.WARNING, aPerformer.getName() + " no body control.");
               checkSkill = aPerformer.getSkills().learn(104, 1.0F);
            }
         } else {
            try {
               checkSkill = aPerformer.getSkills().getSkill(100);
            } catch (NoSuchSkillException var5) {
               logger.log(Level.WARNING, aPerformer.getName() + " no mind logic.");
               checkSkill = aPerformer.getSkills().learn(100, 1.0F);
            }
         }

         if (checkSkill.getRealKnowledge() > (double)aVehicle.skillNeeded) {
            toReturn = true;
         }
      }

      return toReturn;
   }

   public static boolean mayDriveVehicle(Creature aPerformer, Item aVehicle, @Nullable Action act) {
      if (aPerformer == null || aVehicle == null) {
         logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
         return false;
      } else if (aVehicle.isTent()) {
         return false;
      } else if (aPerformer.getWurmId() == aVehicle.lastOwner || aVehicle.lastOwner == -10L) {
         return true;
      } else if (aVehicle.mayCommand(aPerformer)) {
         return true;
      } else {
         return aVehicle.isInPvPZone() && MethodsItems.checkIfStealing(aVehicle, aPerformer, act);
      }
   }

   public static boolean mayEmbarkVehicle(Creature aPerformer, Item aVehicle) {
      if (aPerformer == null || aVehicle == null) {
         logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
         return false;
      } else if (aVehicle.isTent()) {
         return false;
      } else {
         return aVehicle.isChair() ? true : aVehicle.mayPassenger(aPerformer);
      }
   }

   public static final boolean isFriendAndMayMount(Creature aPerformer, Item aVehicle) {
      if (aVehicle.isTent()) {
         return false;
      } else {
         return ItemSettings.mayCommand(aVehicle, aPerformer);
      }
   }

   public static boolean mayDriveVehicle(Creature aPerformer, Creature aVehicle) {
      if (aPerformer != null && aVehicle != null) {
         if (aPerformer.getWurmId() != aVehicle.dominator && aVehicle.getLeader() != aPerformer) {
            return false;
         } else {
            return !Servers.isThisAPvpServer() && aVehicle.isBranded() ? aVehicle.mayCommand(aPerformer) : true;
         }
      } else {
         logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
         return false;
      }
   }

   public static boolean mayEmbarkVehicle(Creature aPerformer, Creature aVehicle) {
      if (aPerformer == null || aVehicle == null) {
         logger.warning("null arguments - Performer: " + aPerformer + ", Vehicle: " + aVehicle);
         return false;
      } else if (aVehicle.dominator == -10L && aVehicle.getLeader() == null) {
         return false;
      } else {
         return !Servers.isThisAPvpServer() && aVehicle.isBranded() ? aVehicle.mayPassenger(aPerformer) : true;
      }
   }

   static boolean hasPermission(Creature performer, Item target) {
      if (target.isChair()) {
         return !target.isOwnedByWagoner();
      } else if (!target.isLocked()) {
         VolaTile vt = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
         Village vill = vt == null ? null : vt.getVillage();
         return vill == null || vill.isActionAllowed((short)6, performer);
      } else {
         return false;
      }
   }
}
