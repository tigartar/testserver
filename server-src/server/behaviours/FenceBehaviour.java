package com.wurmonline.server.behaviours;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.FenceConstants;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

final class FenceBehaviour extends Behaviour implements FenceConstants, ItemTypes, MiscConstants {
   private static final Logger logger = Logger.getLogger(FenceBehaviour.class.getName());

   FenceBehaviour() {
      super((short)22);
   }

   FenceBehaviour(short type) {
      super(type);
   }

   @Nonnull
   @Override
   public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Fence target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      long targetId = target.getId();
      FenceGate gate = FenceGate.getFenceGate(targetId);
      int templateId = subject.getTemplateId();
      if (!target.isFinished()) {
         toReturn.add(Actions.actionEntrys[171]);
         toReturn.add(Actions.actionEntrys[607]);
      } else {
         if (target.isItemRepair(subject) && !target.isFlowerbed()) {
            if (target.getDamage() > 0.0F
               && (!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0)
               && !target.isNoRepair()
               && !toReturn.contains(Actions.actionEntrys[193])) {
               toReturn.add(Actions.actionEntrys[193]);
            }

            if (target.getQualityLevel() < 100.0F && !target.isNoImprove() && target.getDamage() == 0.0F && !toReturn.contains(Actions.actionEntrys[192])) {
               toReturn.add(Actions.actionEntrys[192]);
            }
         } else if (templateId == 676) {
            if (subject.getOwnerId() == performer.getWurmId()) {
               toReturn.add(Actions.actionEntrys[472]);
            }
         } else if (subject.isContainerLiquid() && target.isFlowerbed()) {
            Item[] items = subject.getItemsAsArray();

            for(Item item : items) {
               if (item.getTemplateId() == 128) {
                  toReturn.add(Actions.actionEntrys[565]);
                  break;
               }
            }
         }

         if (target.isFinished()) {
            VolaTile fenceTile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
            Structure structure = fenceTile == null ? null : fenceTile.getStructure();
            if (structure == null || MethodsStructure.mayModifyStructure(performer, structure, fenceTile, (short)683)) {
               if (!target.isStoneFence() || subject.getTemplateId() != 130 && (!subject.isWand() || performer.getPower() < 4)) {
                  if (target.isPlasteredFence() && (subject.getTemplateId() == 1115 || subject.isWand() && performer.getPower() >= 4)) {
                     toReturn.add(new ActionEntry((short)847, "Remove render", "removing"));
                  }
               } else {
                  toReturn.add(new ActionEntry((short)847, "Render fence", "rendering"));
               }
            }
         }

         if (!performer.isGuest() && !target.isIndestructible()) {
            Skills skills = performer.getSkills();
            Skill str = skills.getSkillOrLearn(102);
            if (str.getKnowledge(0.0) > 21.0 && !target.isRubble()) {
               toReturn.add(Actions.actionEntrys[172]);
            }
         }

         if (subject.isColor() && !target.isNotPaintable()) {
            toReturn.add(Actions.actionEntrys[231]);
         }

         if (target.isDoor()) {
            toReturn.addAll(this.getBehavioursForGate(performer, subject, target, gate));
         }

         if (subject.isTrellis() && performer.getFloorLevel() == 0) {
            toReturn.add(new ActionEntry((short)-3, "Plant", "Plant options"));
            toReturn.add(Actions.actionEntrys[746]);
            toReturn.add(new ActionEntry((short)176, "In center", "planting"));
            toReturn.add(Actions.actionEntrys[747]);
         }
      }

      if (MethodsStructure.isCorrectToolForBuilding(performer, templateId)) {
         if (!target.isHedge() && !target.isFinished()) {
            toReturn.add(Actions.actionEntrys[170]);
         }
      } else if (templateId == 267 && target.isHedge()) {
         toReturn.add(Actions.actionEntrys[373]);
         if (performer.getPower() > 0) {
            toReturn.add(Actions.actionEntrys[188]);
         }
      }

      if (target.isHedge() && (subject.isWeaponSlash() || subject.getTemplateId() == 24)) {
         toReturn.add(Actions.actionEntrys[96]);
      }

      if ((templateId == 315 || templateId == 176) && performer.getPower() >= 2) {
         toReturn.add(Actions.actionEntrys[180]);
         if (target.getDamage() > 0.0F && (!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0)) {
            toReturn.add(Actions.actionEntrys[193]);
         }

         if (target.getQualityLevel() < 100.0F) {
            toReturn.add(Actions.actionEntrys[192]);
         }

         if (templateId == 176 && Servers.isThisATestServer() && !target.isMagic()) {
            toReturn.add(Actions.actionEntrys[581]);
         }

         toReturn.add(Actions.actionEntrys[684]);
         if (target.isHedge()) {
            toReturn.add(Actions.actionEntrys[373]);
            toReturn.add(Actions.actionEntrys[188]);
            toReturn.add(Actions.actionEntrys[96]);
         }
      } else if (templateId == 441 && target.getColor() != -1 && !target.isNotPaintable()) {
         toReturn.add(Actions.actionEntrys[232]);
      }

      if (MissionTriggers.getMissionTriggersWith(templateId, 473, targetId).length > 0) {
         toReturn.add(Actions.actionEntrys[473]);
      }

      if (MissionTriggers.getMissionTriggersWith(templateId, 474, targetId).length > 0) {
         toReturn.add(Actions.actionEntrys[474]);
      }

      addEmotes(toReturn);
      addWarStuff(toReturn, performer, target);
      return toReturn;
   }

   private static void addWarStuff(@Nonnull List<ActionEntry> toReturn, @Nonnull Creature performer, @Nonnull Fence fence) {
      Village targVill = fence.getVillage();
      Village village = performer.getCitizenVillage();
      if (village != null && village.mayDoDiplomacy(performer) && targVill != null) {
         if (village != targVill) {
            boolean atPeace = village.mayDeclareWarOn(targVill);
            if (atPeace) {
               toReturn.add(new ActionEntry((short)-1, "Village", "Village options", emptyIntArr));
               toReturn.add(Actions.actionEntrys[209]);
            }
         }
      }
   }

   @Nonnull
   @Override
   public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Fence target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      FenceGate gate = FenceGate.getFenceGate(target.getId());
      if (!target.isFinished()) {
         toReturn.add(Actions.actionEntrys[607]);
      } else if (target.isDoor()) {
         toReturn.addAll(this.getBehavioursForGate(performer, null, target, gate));
      }

      addEmotes(toReturn);
      return toReturn;
   }

   @Override
   public boolean action(
      @Nonnull Action act, @Nonnull Creature performer, @Nonnull Item source, boolean onSurface, @Nonnull Fence target, short action, float counter
   ) {
      boolean done = true;
      FenceGate gate = FenceGate.getFenceGate(target.getId());
      Communicator comm = performer.getCommunicator();
      switch(action) {
         case 1:
            if (source.getTemplateId() == 176 && performer.getPower() >= 2) {
               done = true;
               this.action(act, performer, onSurface, target, action, counter);
               comm.sendNormalServerMessage("Startx=" + target.getTileX() + ", Starty=" + target.getTileY() + " dir=" + target.getDir());
            } else {
               done = this.action(act, performer, onSurface, target, action, counter);
            }
            break;
         case 28:
            done = true;
            if (!target.isNotLockable()) {
               try {
                  if (gate == null) {
                     logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
                     return true;
                  }

                  Item lock = gate.getLock();
                  if (gate.mayLock(performer) || performer.hasKeyForLock(lock)) {
                     lock.lock();
                     PermissionsHistories.addHistoryEntry(
                        gate.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Locked gate"
                     );
                     comm.sendNormalServerMessage("You lock the gate.");
                     Server.getInstance().broadCastAction(performer.getName() + " locks the gate.", performer, 5);
                  }
               } catch (NoSuchLockException var30) {
               }
            }
            break;
         case 96:
            if (target.isHedge() && (source.isWeaponSlash() || source.getTemplateId() == 24 || source.isWand())) {
               done = Terraforming.chopHedge(act, performer, source, target, onSurface, counter);
            }
            break;
         case 101:
            done = true;
            if (!target.isNotLockpickable() && gate != null) {
               done = MethodsStructure.picklock(performer, source, gate, gate.getFence().getName(), counter, act);
            }
            break;
         case 102:
            done = true;
            if (!target.isNotLockable()) {
               try {
                  if (gate == null) {
                     logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
                     return true;
                  }

                  Item lock = gate.getLock();
                  if (gate.mayLock(performer) || performer.hasKeyForLock(lock)) {
                     lock.unlock();
                     PermissionsHistories.addHistoryEntry(
                        gate.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Unlocked gate"
                     );
                     comm.sendNormalServerMessage("You unlock the gate.");
                     Server.getInstance().broadCastAction(performer.getName() + " unlocks the gate.", performer, 5);
                  }
               } catch (NoSuchLockException var29) {
               }
            }
            break;
         case 161:
            if (source.isLock() && source.isLocked()) {
               comm.sendNormalServerMessage("The " + source.getName() + " is already in use.");
               return true;
            }

            if (source.getTemplateId() != 252) {
               done = super.action(act, performer, onSurface, target, action, counter);
            } else if (target.isDoor()) {
               if (target.isFinished() && !target.isNotLockable()) {
                  if (gate == null) {
                     logger.log(Level.WARNING, "No gate found for fence with id " + target.getId());
                     return true;
                  }

                  try {
                     long lockid = gate.getLockId();
                     if (lockid == source.getWurmId()) {
                        comm.sendNormalServerMessage("You may not attach the lock to this gate twice. Are you crazy or supernatural?");
                        return true;
                     }
                  } catch (NoSuchLockException var25) {
                  }

                  if (!Methods.isActionAllowed(performer, action) || !gate.mayAttachLock(performer)) {
                     comm.sendNormalServerMessage("You may not attach the lock to this gate as you do not have permission to do so.");
                     return true;
                  }

                  boolean insta = Servers.isThisATestServer() && performer.getPower() > 0;
                  Village village = null;
                  Skill carpentry = performer.getSkills().getSkillOrLearn(1005);
                  int time = 10;
                  done = false;
                  if (counter == 1.0F) {
                     time = (int)Math.max(100.0, (100.0 - carpentry.getKnowledge(source, 0.0)) * 5.0);

                     try {
                        performer.getCurrentAction().setTimeLeft(time);
                     } catch (NoSuchActionException var24) {
                        logger.log(Level.INFO, "This action does not exist?", (Throwable)var24);
                     }

                     comm.sendNormalServerMessage("You start to attach the lock.");
                     Server.getInstance().broadCastAction(performer.getName() + " starts to attach a lock.", performer, 5);
                     performer.sendActionControl(Actions.actionEntrys[161].getVerbString(), true, time);
                  } else {
                     try {
                        time = performer.getCurrentAction().getTimeLeft();
                     } catch (NoSuchActionException var23) {
                        logger.log(Level.INFO, "This action does not exist?", (Throwable)var23);
                     }

                     if (!(counter * 10.0F <= (float)time) || insta) {
                        carpentry.skillCheck((double)(100.0F - source.getCurrentQualityLevel()), 0.0, false, counter);
                        long parentId = source.getParentId();
                        if (parentId != -10L) {
                           try {
                              Items.getItem(parentId).dropItem(source.getWurmId(), true);
                           } catch (NoSuchItemException var22) {
                              logger.log(Level.INFO, performer.getName() + " tried to build with nonexistant nail.");
                           }
                        }

                        done = true;

                        try {
                           village = gate.getVillage();
                           long lockid = gate.getLockId();
                           if (lockid != source.getLockId()) {
                              try {
                                 Item oldlock = Items.getItem(lockid);
                                 if (village != null) {
                                    oldlock.removeKey(village.getDeedId());
                                 }

                                 oldlock.setLocked(false);
                                 performer.getInventory().insertItem(oldlock, true);
                              } catch (NoSuchItemException var20) {
                                 logger.log(Level.WARNING, "Weird. Lock id exists, but not the item.", (Throwable)var20);
                              }
                           }
                        } catch (NoSuchLockException var21) {
                        }

                        if (village != null) {
                           source.addKey(village.getDeedId());
                        }

                        if (source.getLastOwnerId() != performer.getWurmId()) {
                           logger.log(Level.INFO, "Weird. Lock has wrong last owner.");
                        }

                        source.setLastOwnerId(performer.getWurmId());
                        gate.setLock(source.getWurmId());
                        source.setLocked(true);
                        PermissionsHistories.addHistoryEntry(
                           gate.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Attached lock to gate"
                        );
                        comm.sendNormalServerMessage("You attach the lock to the gate.");
                        Server.getInstance().broadCastAction(performer.getName() + " attaches a lock to the gate.", performer, 5);
                        if (village != null) {
                           gate.setIsManaged(true, (Player)performer);
                           gate.addGuest(performer.getWurmId(), DoorSettings.DoorPermissions.PASS.getValue());
                        }
                     }
                  }
               } else {
                  comm.sendNormalServerMessage("This fence is not finished yet. Attach the lock when it is finished.");
               }
            }
            break;
         case 170:
            if (target.getLayer() != performer.getLayer() && Servers.isThisAPvpServer()) {
               performer.getCommunicator().sendNormalServerMessage("You cannot continue that, you are too far away.");
               return true;
            }

            if (MethodsStructure.isCorrectToolForBuilding(performer, source.getTemplateId())) {
               done = MethodsStructure.continueFence(performer, target, source, counter, action, act);
               if (done) {
                  if (!target.isFinished()) {
                     comm.sendAddFenceToCreationWindow(target, target.getId());
                  } else {
                     comm.sendRemoveFromCreationWindow(target.getId());
                  }
               }
            }
            break;
         case 171:
            done = MethodsStructure.removeFencePlan(performer, source, target, counter, action, act);
            break;
         case 172:
            done = true;
            if (!target.isRubble() && !target.isIndestructible()) {
               try {
                  Skill str = performer.getSkills().getSkill(102);
                  if (str.getKnowledge(0.0) > 21.0) {
                     done = MethodsStructure.destroyFence(action, performer, source, target, false, counter);
                  }
               } catch (NoSuchSkillException var27) {
                  logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
               }
            }
            break;
         case 173:
            done = true;
            if (!target.isIndestructible()) {
               try {
                  Skill str = performer.getSkills().getSkill(102);
                  if (str.getKnowledge(0.0) > 21.0) {
                     done = MethodsStructure.destroyFence(action, performer, source, target, true, counter);
                  }
               } catch (NoSuchSkillException var26) {
                  logger.log(Level.WARNING, "Weird, " + performer.getName() + " has no strength!");
               }
            }
            break;
         case 176:
         case 746:
         case 747:
            if (source.isTrellis()) {
               done = Terraforming.plantTrellis(performer, source, target.getTileX(), target.getTileY(), onSurface, target.getDir(), action, counter, act);
            } else {
               done = true;
            }
            break;
         case 180:
            if (performer.getPower() >= 2) {
               MethodsStructure.instaDestroyFence(performer, target);
            } else {
               done = super.action(act, performer, onSurface, target, action, counter);
            }
            break;
         case 188:
            if (performer.getPower() > 0) {
               if (!target.isHighHedge()
                  && target.getType() != StructureConstantsEnum.HEDGE_FLOWER1_LOW
                  && target.getType() != StructureConstantsEnum.HEDGE_FLOWER3_MEDIUM) {
                  target.setDamage(0.0F);
                  target.setType(StructureConstantsEnum.getEnumByValue((short)((byte)(target.getType().value + 1))));

                  try {
                     target.save();
                     VolaTile tile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), onSurface);
                     if (tile != null) {
                        tile.updateFence(target);
                     }
                  } catch (IOException var28) {
                     logger.log(Level.WARNING, var28.getMessage(), (Throwable)var28);
                  }

                  TileEvent.log(target.getTileX(), target.getTileY(), 0, performer.getWurmId(), 188);
               } else {
                  comm.sendNormalServerMessage("You can't grow that hedge any further, clown.");
               }
            }
            break;
         case 192:
            if (target.isFinished() && target.isItemRepair(source) && !target.isNoImprove()) {
               done = MethodsStructure.improveFence(act, performer, source, target, counter);
            } else {
               done = true;
            }
            break;
         case 193:
            if (!target.isFlowerbed()) {
               if ((!Servers.localServer.challengeServer || performer.getEnemyPresense() <= 0) && !target.isNoRepair()) {
                  done = MethodsStructure.repairFence(act, performer, source, target, counter);
               } else {
                  done = true;
               }
            } else {
               done = super.action(act, performer, onSurface, target, action, counter);
            }
            break;
         case 209:
            done = this.action(act, performer, onSurface, target, action, counter);
            break;
         case 231:
            if (!target.isFinished()) {
               comm.sendNormalServerMessage("Finish the wall first.");
               return true;
            }

            if (target.isNotPaintable() || !Methods.isActionAllowed(performer, action)) {
               comm.sendNormalServerMessage("You are not allowed to paint this fence.");
               return true;
            }

            done = MethodsStructure.colorFence(performer, source, target, act);
            break;
         case 232:
            if (target.isNotPaintable() || !Methods.isActionAllowed(performer, action)) {
               comm.sendNormalServerMessage("You are not allowed to remove the paint from this wall.");
               return true;
            }

            done = MethodsStructure.removeColor(performer, source, target, act);
            break;
         case 373:
            done = Terraforming.pruneHedge(act, performer, source, target, onSurface, counter);
            break;
         case 472:
            done = true;
            if (source.getTemplateId() == 676 && source.getOwnerId() == performer.getWurmId()) {
               MissionManager m = new MissionManager(performer, "Manage missions", "Select action", target.getId(), target.getName(), source.getWurmId());
               m.sendQuestion();
            }
            break;
         case 565:
            if (target.isFlowerbed()) {
               done = waterFlower(act, performer, source, target, counter);
            } else {
               done = super.action(act, performer, onSurface, target, action, counter);
            }
            break;
         case 581:
            if (source.getTemplateId() == 176 && Servers.isThisATestServer()) {
               decay(target, performer);
               done = true;
            } else {
               done = super.action(act, performer, onSurface, target, action, counter);
            }
            break;
         case 607:
            comm.sendAddFenceToCreationWindow(target, -10L);
            return true;
         case 667:
            if (gate != null && (gate.mayManage(performer) || gate.isActualOwner(performer.getWurmId()))) {
               ManagePermissions mp = new ManagePermissions(
                  performer, ManageObjectList.Type.GATE, FenceGate.getFenceGate(target.getId()), false, -10L, false, null, ""
               );
               mp.sendQuestion();
            }

            done = true;
            break;
         case 684:
            done = true;
            if ((source.getTemplateId() == 315 || source.getTemplateId() == 176) && performer.getPower() >= 2) {
               Methods.sendItemRestrictionManagement(performer, target, target.getId());
            } else {
               logger.log(
                  Level.WARNING, performer.getName() + " hacking the protocol by trying to set the restrictions of " + target + ", counter: " + counter + '!'
               );
            }
            break;
         case 691:
            if (gate != null && gate.maySeeHistory(performer)) {
               PermissionsHistory ph = new PermissionsHistory(performer, target.getId());
               ph.sendQuestion();
            }
            break;
         case 847:
            if (target.isStoneFence() && (source.getTemplateId() == 130 || source.isWand() && performer.getPower() >= 4)) {
               return toggleRenderFence(performer, source, target, act, counter);
            }

            if (target.isPlasteredFence() && (source.getTemplateId() == 1115 || source.isWand() && performer.getPower() >= 4)) {
               return toggleRenderFence(performer, source, target, act, counter);
            }
         default:
            done = super.action(act, performer, onSurface, target, action, counter);
      }

      return done;
   }

   static final boolean toggleRenderFence(Creature performer, Item tool, Fence fence, Action act, float counter) {
      boolean insta = tool.isWand() && performer.getPower() >= 4;
      VolaTile fenceTile = getFenceTile(fence);
      if (fenceTile == null) {
         return true;
      } else {
         Structure structure = fenceTile.getStructure();
         if (!insta && structure != null && !MethodsStructure.mayModifyStructure(performer, structure, fenceTile, (short)683)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to modify the structure.");
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)116, fenceTile.getTileX(), fenceTile.getTileY())) {
            return true;
         } else if (fence.isStoneFence() && !insta && tool.getWeightGrams() < 5000) {
            performer.getCommunicator().sendNormalServerMessage("It takes 5kg of " + tool.getName() + " to render the " + fence.getName() + ".");
            return true;
         } else {
            int time = 40;
            if (counter == 1.0F) {
               String render = fence.isStoneFence() ? "render" : "remove the render from";
               String rendering = fence.isStoneFence() ? "rending" : "removing the render from";
               act.setTimeLeft(time);
               performer.sendActionControl(rendering + " the fence", true, time);
               performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You start to " + render + " the %s.", fence.getName()));
               Server.getInstance()
                  .broadCastAction(StringUtil.format("%s starts to " + render + " the %s.", performer.getName(), fence.getName()), performer, 5);
               return false;
            } else {
               time = act.getTimeLeft();
               if (!(counter * 10.0F > (float)time) && !insta) {
                  return false;
               } else {
                  String render = fence.isStoneFence() ? "render" : "remove the render from";
                  String renders = fence.isStoneFence() ? "renders" : "removes the render from";
                  performer.getCommunicator().sendNormalServerMessage(StringUtil.format("You " + render + " the %s.", fence.getName()));
                  Server.getInstance().broadCastAction(StringUtil.format("%s " + renders + " the %s.", performer.getName(), fence.getName()), performer, 5);
                  if (fence.isStoneFence() && !insta) {
                     tool.setWeight(tool.getWeightGrams() - 5000, true);
                  }

                  if (fence.getType() == StructureConstantsEnum.FENCE_STONE) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_IRON);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_GATE) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_IRON_GATE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED) {
                     fence.setType(StructureConstantsEnum.FENCE_STONE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_IRON) {
                     fence.setType(StructureConstantsEnum.FENCE_IRON);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_IRON_GATE) {
                     fence.setType(StructureConstantsEnum.FENCE_IRON_GATE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_HIGH) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE) {
                     fence.setType(StructureConstantsEnum.FENCE_IRON_HIGH);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE) {
                     fence.setType(StructureConstantsEnum.FENCE_IRON_GATE_HIGH);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_IRON_GATE_HIGH) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_HIGH_IRON_FENCE_GATE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS) {
                     fence.setType(StructureConstantsEnum.FENCE_PORTCULLIS);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_PORTCULLIS) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_PORTCULLIS);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET) {
                     fence.setType(StructureConstantsEnum.FENCE_STONE_PARAPET);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_STONE_PARAPET) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_STONE_PARAPET);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE) {
                     fence.setType(StructureConstantsEnum.FENCE_MEDIUM_CHAIN);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_MEDIUM_CHAIN) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_CHAIN_FENCE);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL) {
                     fence.setType(StructureConstantsEnum.FENCE_STONEWALL_HIGH);
                  } else if (fence.getType() == StructureConstantsEnum.FENCE_STONEWALL_HIGH) {
                     fence.setType(StructureConstantsEnum.FENCE_RENDERED_TALL_STONE_WALL);
                  }

                  try {
                     fence.save();
                  } catch (IOException var12) {
                     logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
                  }

                  fenceTile.updateFence(fence);
                  return true;
               }
            }
         }
      }
   }

   @Nullable
   static final VolaTile getFenceTile(Fence fence) {
      int tilex = fence.getStartX();
      int tiley = fence.getStartY();

      for(int xx = 1; xx >= -1; --xx) {
         for(int yy = 1; yy >= -1; --yy) {
            try {
               Zone zone = Zones.getZone(tilex + xx, tiley + yy, fence.isOnSurface());
               VolaTile tile = zone.getTileOrNull(tilex + xx, tiley + yy);
               if (tile != null) {
                  Fence[] fences = tile.getFences();

                  for(int s = 0; s < fences.length; ++s) {
                     if (fences[s].getId() == fence.getId()) {
                        return tile;
                     }
                  }
               }
            } catch (NoSuchZoneException var9) {
            }
         }
      }

      return null;
   }

   private static void decay(Fence fence, Creature performer) {
      if (!fence.isMagic()) {
         long decayTime = 86400000L;
         if (fence.isHedge()) {
            if (fence.isLowHedge()) {
               decayTime *= 3L;
            } else if (fence.isMediumHedge()) {
               decayTime *= 10L;
            } else {
               Village vill = fence.getVillage();
               if (vill != null) {
                  if (vill.moreThanMonthLeft()) {
                     performer.getCommunicator().sendNormalServerMessage("There is more then a month left of upkeep, no decay will take place.");
                     return;
                  }

                  if (vill.lessThanWeekLeft()) {
                     decayTime *= (long)(fence.isFlowerbed() ? 2 : 10);
                  }
               } else if (Zones.getKingdom(fence.getTileX(), fence.getTileY()) == 0) {
                  decayTime = (long)((float)decayTime * 0.5F);
               }
            }
         }

         fence.setLastUsed(WurmCalendar.currentTime - decayTime - 10L);
         fence.poll(WurmCalendar.currentTime);
      }
   }

   private static boolean waterFlower(@Nonnull Action act, @Nonnull Creature performer, @Nonnull Item waterSource, @Nonnull Fence flowerbed, float counter) {
      int time = 0;
      Item water = null;

      for(Item item : waterSource.getItemsAsArray()) {
         if (item.getTemplateId() == 128) {
            water = item;
            break;
         }
      }

      Communicator comm = performer.getCommunicator();
      if (water == null) {
         comm.sendNormalServerMessage("You need water to water the flowerbed.", (byte)3);
         return true;
      } else if (water.getWeightGrams() < 100) {
         comm.sendNormalServerMessage("You need more water in order to water the flowerbed.", (byte)3);
         return true;
      } else if (flowerbed.getDamage() == 0.0F) {
         comm.sendNormalServerMessage("This flowerbed is in no need of watering.", (byte)3);
         return true;
      } else {
         Skill gardening = performer.getSkills().getSkillOrLearn(10045);
         if (counter == 1.0F) {
            time = Actions.getStandardActionTime(performer, gardening, waterSource, 0.0);
            act.setTimeLeft(time);
            comm.sendNormalServerMessage("You start watering the flowerbed.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to water a flowerbed.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[565].getVerbString(), true, time);
            return false;
         } else {
            time = act.getTimeLeft();
            if (counter * 10.0F <= (float)time) {
               return false;
            } else {
               double power = gardening.skillCheck(15.0, 0.0, false, counter);
               if (power > 0.0) {
                  float dmgChange = 20.0F * (float)(power / 100.0);
                  flowerbed.setDamage(Math.max(0.0F, flowerbed.getDamage() - dmgChange));
                  water.setWeight(water.getWeightGrams() - 100, true);
                  comm.sendNormalServerMessage("You successfully watered the flowerbed.");
                  return true;
               } else {
                  int waterReduction = 100;
                  if (power >= -20.0) {
                     comm.sendNormalServerMessage("You accidentally miss the flowerbed and pour the water on the ground instead.", (byte)3);
                  } else if (power > -50.0 && power < -20.0) {
                     comm.sendNormalServerMessage("You spill water all over your clothes.", (byte)3);
                  } else {
                     comm.sendNormalServerMessage(
                        "For some inexplicable reason you poured all of the water on the ground, how you thought it would help you will never know."
                     );
                     waterReduction = Math.min(water.getWeightGrams(), 200);
                  }

                  water.setWeight(water.getWeightGrams() - waterReduction, true);
                  return true;
               }
            }
         }
      }
   }

   @Override
   public boolean action(@Nonnull Action act, @Nonnull Creature performer, boolean onSurface, @Nonnull Fence target, short action, float counter) {
      boolean done = false;
      FenceGate gate = FenceGate.getFenceGate(target.getId());
      Communicator comm = performer.getCommunicator();
      switch(action) {
         case 1:
            StructureStateEnum state = target.getState();
            StructureConstantsEnum type = target.getType();
            String toSend;
            if (target.isFinished()) {
               toSend = getFinishedString(performer, target, gate, type);
            } else {
               toSend = getUnfinishedString(target, state, type);
            }

            if (toSend.length() > 0) {
               comm.sendNormalServerMessage(toSend);
               comm.sendNormalServerMessage("QL=" + target.getQualityLevel() + ", dam=" + target.getDamage());
            }

            done = true;
            break;
         case 28:
            done = true;
            if (gate != null) {
               try {
                  Item lock = gate.getLock();
                  if (gate.mayLock(performer) || performer.hasKeyForLock(lock)) {
                     lock.lock();
                     comm.sendNormalServerMessage("You lock the gate.");
                     Server.getInstance().broadCastAction(performer.getNameWithGenus() + " locks the gate.", performer, 5);
                  }
               } catch (NoSuchLockException var14) {
               }
            }
            break;
         case 102:
            done = true;

            try {
               Item lock = gate.getLock();
               if (gate.mayLock(performer) || performer.hasKeyForLock(lock)) {
                  lock.unlock();
                  comm.sendNormalServerMessage("You unlock the gate.");
                  Server.getInstance().broadCastAction(performer.getNameWithGenus() + " unlocks the gate.", performer, 5);
               }
            } catch (NoSuchLockException var13) {
            }
            break;
         case 193:
            comm.sendNormalServerMessage("'Repair' requires an active item.");
            done = true;
            break;
         case 209:
            done = true;
            if (performer.getCitizenVillage() == null) {
               comm.sendAlertServerMessage("You are no longer a citizen of a village.");
            } else if (target.getVillage() == null) {
               comm.sendAlertServerMessage(target.getName() + " is no longer in a village.");
            } else if (!performer.getCitizenVillage().mayDeclareWarOn(target.getVillage())) {
               comm.sendAlertServerMessage(target.getName() + " is already at war with your village.");
            } else {
               Methods.sendWarDeclarationQuestion(performer, target.getVillage());
            }
            break;
         case 607:
            comm.sendAddFenceToCreationWindow(target, -10L);
            done = true;
            break;
         case 667:
            done = true;
            if (gate != null && (gate.mayManage(performer) || gate.isActualOwner(performer.getWurmId()))) {
               ManagePermissions mp = new ManagePermissions(
                  performer, ManageObjectList.Type.GATE, FenceGate.getFenceGate(target.getId()), false, -10L, false, null, ""
               );
               mp.sendQuestion();
            }
            break;
         case 691:
            done = true;
            if (gate != null && gate.maySeeHistory(performer)) {
               PermissionsHistory ph = new PermissionsHistory(performer, target.getId());
               ph.sendQuestion();
            }
      }

      return done;
   }

   private static String getMaterialName(ItemTemplate template) {
      switch(template.getTemplateId()) {
         case 217:
            return "large iron " + template.getName();
         case 218:
            return "small iron " + template.getName();
         default:
            return template.getName();
      }
   }

   private List<ActionEntry> getBehavioursForGate(Creature performer, @Nullable Item subject, Fence target, @Nonnull FenceGate gate) {
      List<ActionEntry> toReturn = new LinkedList<>();
      List<ActionEntry> permissions = new LinkedList<>();
      if (gate.mayManage(performer) || gate.isActualOwner(performer.getWurmId())) {
         permissions.add(new ActionEntry((short)667, "Manage Gate", "managing permissions"));
      }

      if (gate.maySeeHistory(performer)) {
         permissions.add(new ActionEntry((short)691, "History of Gate", "viewing"));
      }

      if (!permissions.isEmpty()) {
         if (permissions.size() > 1) {
            Collections.sort(permissions);
            toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
         }

         toReturn.addAll(permissions);
      }

      try {
         Item lock = gate.getLock();
         if (!target.isNotLockable() && (gate.mayLock(performer) || performer.hasKeyForLock(lock))) {
            if (lock.isLocked()) {
               toReturn.add(Actions.actionEntrys[102]);
            } else {
               toReturn.add(Actions.actionEntrys[28]);
            }
         }

         if (performer.isWithinDistanceTo(target.getTileX(), target.getTileY(), 1)
            && subject != null
            && subject.getTemplateId() == 463
            && !target.isNotLockpickable()) {
            MethodsStructure.addLockPickEntry(performer, subject, gate, false, lock, toReturn);
         }
      } catch (NoSuchLockException var8) {
      }

      if (!target.isNotLockable() && subject != null && subject.getTemplateId() == 252 && gate.mayAttachLock(performer)) {
         toReturn.add(Actions.actionEntrys[161]);
      }

      return toReturn;
   }

   @Nonnull
   private static String getUnfinishedString(@Nonnull Fence target, StructureStateEnum state, StructureConstantsEnum type) {
      String toSend = "You see an unfinished fence.";
      switch(type) {
         case FENCE_PLAN_PALISADE:
         case FENCE_PALISADE:
            toSend = "You see an unfinished wooden palisade.";
            break;
         case FENCE_PLAN_STONEWALL:
         case FENCE_STONEWALL:
         case FENCE_STONE_PARAPET:
            toSend = "You see an unfinished stone wall.";
            break;
         case FENCE_PLAN_STONE_PARAPET:
            toSend = "You see an unfinished stone parapet.";
            break;
         case FENCE_PLAN_STONE_IRON_PARAPET:
            toSend = "You see an unfinished stone and iron parapet.";
            break;
         case FENCE_PLAN_WOODEN_PARAPET:
            toSend = "You see an unfinished wooden parapet.";
            break;
         case FENCE_PLAN_WOODEN:
         case FENCE_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
            toSend = "You see an unfinished wooden fence.";
            break;
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_WOODEN_GATE:
            toSend = "You see an unfinished wooden fence gate.";
            break;
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE_GATE:
            toSend = "You see an unfinished wooden palisade gate.";
            break;
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_WOODEN_CRUDE_GATE:
            toSend = "You see an unfinished crude wooden fence gate.";
            break;
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_GARDESGARD_GATE:
            toSend = "You see an unfinished wooden roundpole fence gate.";
            break;
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
            toSend = "You see an unfinished tall stone wall.";
            break;
         case FENCE_PLAN_MEDIUM_CHAIN:
            toSend = "You see an unfinished chain fence.";
            break;
         case FENCE_PLAN_PORTCULLIS:
            toSend = "You see an unfinished portcullis.";
            break;
         case FENCE_PLAN_IRON:
            toSend = "You see an unfinished iron fence.";
            break;
         case FENCE_PLAN_IRON_GATE:
            toSend = "You see an unfinished iron fence gate.";
            break;
         case FENCE_PLAN_WOVEN:
            toSend = "You see an unfinished woven fence.";
            break;
         case FENCE_PLAN_STONE:
            toSend = "You see an unfinished stone fence.";
            break;
         case FENCE_PLAN_CURB:
            toSend = "You see an unfinished curb.";
            break;
         case FENCE_PLAN_ROPE_LOW:
            toSend = "You see an unfinished low rope fence.";
            break;
         case FENCE_PLAN_ROPE_HIGH:
            toSend = "You see an unfinished high rope fence.";
            break;
         case FENCE_PLAN_GARDESGARD_HIGH:
            toSend = "You see an unfinished high roundpole fence.";
            break;
         case FENCE_PLAN_GARDESGARD_LOW:
            toSend = "You see an unfinished low roundpole fence.";
      }

      int[] tNeeded = Fence.getConstructionMaterialsNeededTotal(target);
      if (tNeeded.length <= 0) {
         return toSend;
      } else if (tNeeded[0] == -1) {
         logger.log(
            Level.WARNING, "Weird. This shouldn't happen. The fence is finished, of type " + type + " and state " + state, (Throwable)(new Exception())
         );
         return toSend;
      } else {
         try {
            toSend = toSend + " Total materials needed ";

            for(int x = 0; x < tNeeded.length - 1; x += 2) {
               toSend = toSend + tNeeded[x + 1] + " ";
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tNeeded[x]);
               if (!template.getName().endsWith("s") && tNeeded[x + 1] > 1) {
                  toSend = toSend + getMaterialName(template) + "s";
               } else {
                  toSend = toSend + getMaterialName(template);
               }

               if (x < tNeeded.length - 2) {
                  toSend = toSend + " and ";
               }
            }

            toSend = toSend + ".";
            if (tNeeded.length > 2) {
               toSend = toSend + " Current stage needs 1 ";
               int[] materials = Fence.getItemTemplatesNeededForFence(target);

               for(int i = 0; i < materials.length; ++i) {
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(materials[i]);
                  toSend = toSend + getMaterialName(template);
                  if (i < materials.length - 1) {
                     toSend = toSend + " and ";
                  }
               }

               toSend = toSend + ".";
            }
         } catch (NoSuchTemplateException var8) {
            logger.log(Level.WARNING, "Failed to locate template: " + var8.getMessage(), (Throwable)var8);
         }

         return toSend;
      }
   }

   @Nonnull
   private static String getFinishedString(@Nonnull Creature performer, @Nonnull Fence target, @Nullable FenceGate gate, StructureConstantsEnum type) {
      String toSend = "";
      Communicator comm = performer.getCommunicator();
      toSend = getDescription(target, gate, type);
      comm.sendNormalServerMessage(toSend);
      toSend = "";
      if (target.isGate()) {
         sendGateDescription(target, gate, comm);
         sendLockDescription(target, gate, performer, comm);
         if (performer.getPower() > 0) {
            comm.sendNormalServerMessage(
               "State="
                  + target.getState()
                  + " inner x="
                  + gate.getInnerTile().getTileX()
                  + ", "
                  + gate.getInnerTile().getTileY()
                  + ", outer: "
                  + gate.getOuterTile().getTileX()
                  + ", y="
                  + gate.getOuterTile().getTileY()
            );
         }
      }

      if (target.getColor() != -1) {
         comm.sendNormalServerMessage(
            "Colors: R="
               + WurmColor.getColorRed(target.getColor())
               + ", G="
               + WurmColor.getColorGreen(target.getColor())
               + ", B="
               + WurmColor.getColorBlue(target.getColor())
               + "."
         );
      }

      toSend = "";
      comm.sendNormalServerMessage("QL=" + target.getQualityLevel() + ", dam=" + target.getDamage());
      return toSend;
   }

   private static String getDescription(Fence target, FenceGate gate, StructureConstantsEnum type) {
      String toSend = "";
      if (!target.isGate()) {
         toSend = getFenceDescription(type);
      } else {
         toSend = getFenceGateDescription(gate, type);
      }

      if (target.isFlowerbed() && toSend.isEmpty()) {
         toSend = "A flowerbed filled with unknown flowers.";
      }

      if (target.isLowHedge()) {
         if (type != StructureConstantsEnum.HEDGE_FLOWER1_LOW) {
            toSend = "This low hedge is growing steadily.";
         } else {
            toSend = "This pretty lavender hedge will probably not grow further.";
         }

         return toSend;
      } else if (target.isMediumHedge()) {
         return "This medium sized hedge is growing steadily.";
      } else if (target.isHighHedge()) {
         return "This hedge seems to be at peak height.";
      } else {
         if (toSend.isEmpty()) {
            toSend = "Unknown fence type.\n";
            logger.log(Level.WARNING, "Missing fence description for type: " + type);
         }

         return toSend;
      }
   }

   private static void sendLockDescription(Fence target, FenceGate gate, Creature performer, Communicator comm) {
      try {
         String toSend = "";
         Item lock = gate.getLock();
         String lockStrength = lock.getLockStrength();
         comm.sendNormalServerMessage("You see a gate with a lock. The lock is of " + lockStrength + " quality.");
         if (performer.getPower() >= 5) {
            comm.sendNormalServerMessage("Lock WurmId=" + lock.getWurmId() + ", dam=" + lock.getDamage());
         }

         if (gate.getLockCounter() > 0) {
            comm.sendNormalServerMessage("The gate is picked open and will shut in " + gate.getLockCounterTime());
         } else if (lock.isLocked()) {
            toSend = toSend + "It is locked.";
         } else {
            toSend = toSend + "It is unlocked.";
         }

         comm.sendNormalServerMessage(toSend);
         if (performer.getPower() > 1) {
            String ownerName = "unknown";
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(lock.getLastOwnerId());
            if (info != null) {
               ownerName = info.getName();
            }

            comm.sendNormalServerMessage("Last lock owner: " + ownerName);
         }
      } catch (NoSuchLockException var9) {
      }
   }

   private static void sendGateDescription(Fence target, FenceGate gate, Communicator comm) {
      String name = gate.getName();
      String toSend = "";
      toSend = toSend + "A plaque is attached to it:";
      comm.sendNormalServerMessage(toSend);
      comm.sendNormalServerMessage("-----------------");
      toSend = "";
      if (name.length() > 0) {
         toSend = toSend + "Named: \"" + name + "\"";
         comm.sendNormalServerMessage(toSend);
      }

      boolean showOwner = true;
      if (gate.isManaged()) {
         try {
            Village vc = Villages.getVillage(gate.getVillageId());
            comm.sendNormalServerMessage(vc.getMotto());
            showOwner = false;
         } catch (NoSuchVillageException var9) {
            gate.setIsManaged(false, null);
            target.savePermissions();
         }
      }

      if (showOwner) {
         long ownerId = gate.getOwnerId();
         if (ownerId != -10L) {
            String owner = PlayerInfoFactory.getPlayerName(ownerId);
            comm.sendNormalServerMessage("Owner:" + owner);
         }
      }

      comm.sendNormalServerMessage("-----------------");
   }

   private static String getFenceGateDescription(FenceGate gate, StructureConstantsEnum type) {
      Village village = gate != null ? gate.getVillage() : null;
      boolean noVillage = village == null;
      String villageName = noVillage ? null : village.getName();
      String toSend = "";
      switch(type) {
         case FENCE_WOODEN_GATE:
            if (noVillage) {
               toSend = "You see a wooden fence gate.\n";
            } else {
               toSend = "You see a wooden fence gate in the settlement of " + villageName + ".\n";
            }
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_STONEWALL_HIGH:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_LOW:
         default:
            break;
         case FENCE_PALISADE_GATE:
            if (noVillage) {
               toSend = "You see a wooden palisade gate.\n";
            } else {
               toSend = "You see a wooden palisade gate in the settlement of " + villageName + ".";
            }
            break;
         case FENCE_WOODEN_CRUDE_GATE:
            if (noVillage) {
               toSend = "You see a crude wooden fence gate.\n";
            } else {
               toSend = "You see a crude wooden fence gate in the settlement of " + villageName + ".\n";
            }
            break;
         case FENCE_GARDESGARD_GATE:
            if (noVillage) {
               toSend = "You see a wooden roundpole fence gate.\n";
            } else {
               toSend = "You see a wooden roundpole fence gate in the settlement of " + villageName + ".\n";
            }
            break;
         case FENCE_IRON_GATE:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
            if (noVillage) {
               toSend = "You see an iron fence gate.\n";
            } else {
               toSend = "You see an iron fence gate in the settlement of " + villageName + ".\n";
            }
            break;
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
            if (noVillage) {
               toSend = "You see a portcullis.\n";
            } else {
               toSend = "You see a portcullis in the settlement of " + villageName + ".\n";
            }
      }

      return toSend;
   }

   private static String getFenceDescription(StructureConstantsEnum type) {
      String toSend = "";
      switch(type) {
         case FENCE_PALISADE:
            toSend = "You see a sturdy wooden palisade.";
         case FENCE_PLAN_STONEWALL:
         case FENCE_PLAN_STONE_PARAPET:
         case FENCE_PLAN_STONE_IRON_PARAPET:
         case FENCE_PLAN_WOODEN_PARAPET:
         case FENCE_PLAN_WOODEN:
         case FENCE_PLAN_WOODEN_CRUDE:
         case FENCE_PLAN_WOODEN_GATE:
         case FENCE_WOODEN_GATE:
         case FENCE_PLAN_PALISADE_GATE:
         case FENCE_PALISADE_GATE:
         case FENCE_PLAN_WOODEN_GATE_CRUDE:
         case FENCE_WOODEN_CRUDE_GATE:
         case FENCE_PLAN_GARDESGARD_GATE:
         case FENCE_GARDESGARD_GATE:
         case FENCE_PLAN_STONEWALL_HIGH:
         case FENCE_PLAN_MEDIUM_CHAIN:
         case FENCE_PLAN_PORTCULLIS:
         case FENCE_PLAN_IRON:
         case FENCE_PLAN_IRON_GATE:
         case FENCE_PLAN_WOVEN:
         case FENCE_PLAN_STONE:
         case FENCE_PLAN_CURB:
         case FENCE_PLAN_ROPE_LOW:
         case FENCE_PLAN_ROPE_HIGH:
         case FENCE_PLAN_GARDESGARD_HIGH:
         case FENCE_PLAN_GARDESGARD_LOW:
         case FENCE_IRON_GATE:
         case FENCE_IRON_GATE_HIGH:
         case FENCE_SLATE_IRON_GATE:
         case FENCE_ROUNDED_STONE_IRON_GATE:
         case FENCE_POTTERY_IRON_GATE:
         case FENCE_SANDSTONE_IRON_GATE:
         case FENCE_RENDERED_IRON_GATE:
         case FENCE_MARBLE_IRON_GATE:
         case FENCE_SLATE_HIGH_IRON_FENCE_GATE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE:
         case FENCE_RENDERED_HIGH_IRON_FENCE_GATE:
         case FENCE_POTTERY_HIGH_IRON_FENCE_GATE:
         case FENCE_MARBLE_HIGH_IRON_FENCE_GATE:
         case FENCE_PORTCULLIS:
         case FENCE_SLATE_PORTCULLIS:
         case FENCE_ROUNDED_STONE_PORTCULLIS:
         case FENCE_SANDSTONE_PORTCULLIS:
         case FENCE_RENDERED_PORTCULLIS:
         case FENCE_POTTERY_PORTCULLIS:
         case FENCE_MARBLE_PORTCULLIS:
         default:
            break;
         case FENCE_STONEWALL:
            toSend = "You see a strong stone wall.";
            break;
         case FENCE_STONE_PARAPET:
         case FENCE_SANDSTONE_STONE_PARAPET:
         case FENCE_SLATE_STONE_PARAPET:
         case FENCE_ROUNDED_STONE_STONE_PARAPET:
         case FENCE_RENDERED_STONE_PARAPET:
         case FENCE_POTTERY_STONE_PARAPET:
         case FENCE_MARBLE_STONE_PARAPET:
            toSend = "You see a strong stone parapet.";
            break;
         case FENCE_WOODEN:
            toSend = "You see a wooden fence.";
            break;
         case FENCE_STONEWALL_HIGH:
         case FENCE_SLATE_TALL_STONE_WALL:
         case FENCE_ROUNDED_STONE_TALL_STONE_WALL:
         case FENCE_SANDSTONE_TALL_STONE_WALL:
         case FENCE_RENDERED_TALL_STONE_WALL:
         case FENCE_POTTERY_TALL_STONE_WALL:
         case FENCE_MARBLE_TALL_STONE_WALL:
            toSend = "You see a strong tall stone wall.";
            break;
         case FENCE_WOODEN_CRUDE:
            toSend = "You see a crude wooden fence.";
            break;
         case FENCE_GARDESGARD_LOW:
            toSend = "You see a low wooden roundpole fence.";
            break;
         case FENCE_GARDESGARD_HIGH:
            toSend = "You see a high wooden roundpole fence";
            break;
         case FENCE_IRON:
         case FENCE_SLATE_IRON:
         case FENCE_ROUNDED_STONE_IRON:
         case FENCE_POTTERY_IRON:
         case FENCE_SANDSTONE_IRON:
         case FENCE_RENDERED_IRON:
         case FENCE_MARBLE_IRON:
            toSend = "You see an iron fence.";
            break;
         case FENCE_IRON_HIGH:
         case FENCE_SLATE_HIGH_IRON_FENCE:
         case FENCE_ROUNDED_STONE_HIGH_IRON_FENCE:
         case FENCE_SANDSTONE_HIGH_IRON_FENCE:
         case FENCE_RENDERED_HIGH_IRON_FENCE:
         case FENCE_POTTERY_HIGH_IRON_FENCE:
         case FENCE_MARBLE_HIGH_IRON_FENCE:
            toSend = "You see an high iron fence";
            break;
         case FENCE_WOVEN:
            toSend = "This woven fence is purely a decoration and stops neither creature nor man.";
            break;
         case FENCE_WOODEN_PARAPET:
            toSend = "You see a wooden parapet.";
            break;
         case FENCE_STONE_IRON_PARAPET:
            toSend = "You see a strong parapet made from stone and iron.";
            break;
         case FENCE_MEDIUM_CHAIN:
         case FENCE_SLATE_CHAIN_FENCE:
         case FENCE_ROUNDED_STONE_CHAIN_FENCE:
         case FENCE_SANDSTONE_CHAIN_FENCE:
         case FENCE_RENDERED_CHAIN_FENCE:
         case FENCE_POTTERY_CHAIN_FENCE:
         case FENCE_MARBLE_CHAIN_FENCE:
            toSend = "You see a chain fence.";
            break;
         case FENCE_STONE:
            toSend = "You see a stone fence.";
            break;
         case FENCE_SLATE:
         case FENCE_ROUNDED_STONE:
         case FENCE_POTTERY:
         case FENCE_SANDSTONE:
         case FENCE_RENDERED:
         case FENCE_MARBLE:
            toSend = "You see a strong fence.";
            break;
         case FENCE_CURB:
            toSend = "You see a curb.";
            break;
         case FENCE_ROPE_LOW:
            toSend = "You see a low rope fence.";
            break;
         case FENCE_ROPE_HIGH:
            toSend = "You see a high rope fence.";
            break;
         case FENCE_MAGIC_STONE:
            toSend = "This stone wall is magic! You can see how it slowly crumbles as the weave disperses the Source.";
            break;
         case FENCE_MAGIC_FIRE:
            toSend = "This wall of fire is magic! You can see how it slowly dissipates as the weave disperses the Source.";
            break;
         case FENCE_MAGIC_ICE:
            toSend = "This ice wall is magic! You can see how it slowly melts as the weave disperses the Source.";
            break;
         case FLOWERBED_BLUE:
            toSend = "A flowerbed filled with crooked but beautiful blue flowers.";
            break;
         case FLOWERBED_GREENISH_YELLOW:
            toSend = "A flowerbed filled with greenish-yellow furry flowers.";
            break;
         case FLOWERBED_ORANGE_RED:
            toSend = "A flowerbed filled with long-stemmed orange-red flowers with thick, pointy leaves.";
            break;
         case FLOWERBED_PURPLE:
            toSend = "A flowerbed filled with purple fluffy flowers.";
            break;
         case FLOWERBED_WHITE:
            toSend = "A flowerbed filled with thick-stemmed white flowers with long leaves.";
            break;
         case FLOWERBED_WHITE_DOTTED:
            toSend = "A flowerbed filled with uncommon white-dotted flowers.";
            break;
         case FLOWERBED_YELLOW:
            toSend = "A flowerbed filled with yellow prickly flowers.";
      }

      return toSend;
   }
}
