package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchEntryException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.PlonkData;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UnfinishedItemBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(UnfinishedItemBehaviour.class.getName());

   UnfinishedItemBehaviour() {
      super((short)23);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      if (!target.isTraded()) {
         if (target.getTemplateId() == 386) {
            int tid = MethodsItems.getItemForImprovement(target.getMaterial(), target.creationState);
            if (tid == source.getTemplateId()) {
               String actString = MethodsItems.getImproveAction(target.getMaterial(), target.creationState);
               toReturn.add(new ActionEntry((short)228, actString, actString));
            }
         } else {
            int objectCreated = -1;

            try {
               objectCreated = AdvancedCreationEntry.getTemplateId(target);
               if (objectCreated == 430 || objectCreated == 528 || objectCreated == 638) {
                  objectCreated = 384;
               }

               AdvancedCreationEntry entry = CreationMatrix.getInstance().getAdvancedCreationEntry(objectCreated);
               if (entry.isItemNeeded(source, target)) {
                  toReturn.add(Actions.actionEntrys[169]);
               }
            } catch (NoSuchEntryException var7) {
               logger.log(Level.WARNING, "No creation entry for " + objectCreated, (Throwable)var7);
            }
         }
      }

      toReturn.addAll(super.getBehavioursFor(performer, source, target));
      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean toReturn = true;
      if (action == 1) {
         if (target.getTemplateId() == 386) {
            performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
            target.sendEnchantmentStrings(performer.getCommunicator());
         } else {
            int objectCreated = -1;

            try {
               objectCreated = AdvancedCreationEntry.getTemplateId(target);
               ItemTemplate template = null;

               try {
                  template = ItemTemplateFactory.getInstance().getTemplate(objectCreated);
                  String start = "You see "
                     + template.getNameWithGenus()
                     + " under construction. Ql: "
                     + target.getQualityLevel()
                     + ", Dam: "
                     + target.getDamage()
                     + ".";
                  if (objectCreated == 430 || objectCreated == 528 || objectCreated == 638) {
                     objectCreated = 384;
                  }

                  AdvancedCreationEntry creation = CreationMatrix.getInstance().getAdvancedCreationEntry(objectCreated);
                  String rarity = target.getRarity() == 0 ? "" : MethodsItems.getRarityDesc(target.getRarity());
                  String finish = creation.getItemsLeft(target);
                  performer.getCommunicator().sendNormalServerMessage(start + rarity + ' ' + finish);
               } catch (NoSuchTemplateException var13) {
                  logger.log(Level.WARNING, "No template with id " + objectCreated);
               }
            } catch (NoSuchEntryException var14) {
               logger.log(Level.WARNING, "No creation entry for " + objectCreated, (Throwable)var14);
            }
         }
      } else {
         toReturn = super.action(act, performer, target, action, counter);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = true;
      if (action == 169) {
         if (!target.isTraded()) {
            boolean actionResult = false;

            try {
               if ((target.isNoTake() || target.isUseOnGroundOnly() || target.getTopParent() == target.getWurmId())
                  && !performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
                  performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
                  return done;
               }

               if (source.getOwnerId() != performer.getWurmId()) {
                  performer.getCommunicator().sendSafeServerMessage("You must carry the " + source.getName() + " to use it.");
                  return done;
               }

               if (target.getOwnerId() == -10L && !Methods.isActionAllowed(performer, action)) {
                  return done;
               }

               int objectCreated = AdvancedCreationEntry.getTemplateId(target);
               if (objectCreated == 430 || objectCreated == 528 || objectCreated == 638) {
                  objectCreated = 384;
               }

               AdvancedCreationEntry creation = CreationMatrix.getInstance().getAdvancedCreationEntry(objectCreated);
               done = false;
               Item created = creation.cont(performer, source, target.getWurmId(), counter);
               actionResult = true;
               if (!creation.isCreateOnGround() && !performer.canCarry(created.getWeightGrams())) {
                  performer.getCommunicator().sendNormalServerMessage("You can't carry the " + created.getName() + ".");
               }

               if (!creation.isCreateOnGround() && performer.getInventory().insertItem(created) && performer.canCarry(created.getWeightGrams())) {
                  created.setLastOwnerId(performer.getWurmId());
                  performer.getCommunicator().sendNormalServerMessage("You create " + created.getNameWithGenus() + ".");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " creates " + created.getNameWithGenus() + ".", performer, Math.max(3, created.getSizeZ() / 10));
                  if (created.isEpicTargetItem()) {
                     AdvancedCreationEntry.onEpicItemCreated(performer, created, created.getTemplateId(), true);
                  } else {
                     MissionTriggers.activateTriggers(performer, created, 148, 0L, 1);
                  }
               } else {
                  try {
                     float posX = performer.getStatus().getPositionX();
                     float posY = performer.getStatus().getPositionY();
                     float rot = performer.getStatus().getRotation();
                     float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * 2.0F;
                     float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * 2.0F;
                     posX += xPosMod;
                     posY += yPosMod;
                     int placedX = (int)posX >> 2;
                     int placedY = (int)posY >> 2;
                     int placedTile;
                     if (performer.isOnSurface()) {
                        placedTile = Server.surfaceMesh.getTile(placedX, placedY);
                     } else {
                        placedTile = Server.caveMesh.getTile(placedX, placedY);
                     }

                     if (Tiles.decodeHeight(placedTile) < 0) {
                        if (created.getTemplateId() == 37) {
                           performer.getCommunicator().sendNormalServerMessage("The fire fizzles in the water and goes out.");
                           Items.decay(created.getWurmId(), created.getDbStrings());
                        } else {
                           created.setLastOwnerId(performer.getWurmId());
                           created.putItemInfrontof(performer);
                           performer.getCommunicator().sendNormalServerMessage("You create " + created.getNameWithGenus() + " in front of you on the ground.");
                           Server.getInstance()
                              .broadCastAction(
                                 performer.getName() + " creates " + created.getNameWithGenus() + ".", performer, Math.max(3, created.getSizeZ() / 10)
                              );
                        }
                     } else {
                        created.setLastOwnerId(performer.getWurmId());
                        created.putItemInfrontof(performer);
                        performer.getCommunicator().sendNormalServerMessage("You create " + created.getNameWithGenus() + " in front of you on the ground.");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " creates " + created.getNameWithGenus() + ".", performer, Math.max(3, created.getSizeZ() / 10)
                           );
                        if (created.getTemplateId() == 37) {
                           Effect effect = EffectFactory.getInstance()
                              .createFire(created.getWurmId(), created.getPosX(), created.getPosY(), created.getPosZ(), performer.isOnSurface());
                           created.addEffect(effect);
                        }
                     }

                     if (created.getTemplateId() == 1311) {
                        created.setName(created.getTemplate().getName() + " [Empty]");
                     }

                     if (created.isEpicTargetItem()) {
                        AdvancedCreationEntry.onEpicItemCreated(performer, created, created.getTemplateId(), true);
                     } else {
                        MissionTriggers.activateTriggers(performer, created, 148, 0L, 1);
                     }

                     if (created.isSpringFilled()) {
                        if (Zone.hasSpring(created.getTileX(), created.getTileY())) {
                           performer.achievement(207);
                        }
                     } else if (created.isKingdomMarker()) {
                        performer.achievement(208);
                     } else if (created.getTemplate().isStorageRack()) {
                        performer.achievement(508);
                     } else if (created.isBoat()) {
                        PlonkData.BOAT_SECURITY.trigger(performer);
                        switch(created.getTemplateId()) {
                           case 490:
                              performer.achievement(213);
                              break;
                           case 491:
                              performer.achievement(214);
                              break;
                           case 540:
                              performer.achievement(209);
                              break;
                           case 541:
                              performer.achievement(210);
                              break;
                           case 542:
                              performer.achievement(211);
                              break;
                           case 543:
                              performer.achievement(212);
                              break;
                           default:
                              logger.fine("Was a boat but not of a known type so no achievement: templateId: " + created.getTemplateId());
                        }
                     }
                  } catch (NoSuchZoneException var21) {
                     logger.log(Level.WARNING, var21.getMessage(), (Throwable)var21);
                  } catch (NoSuchPlayerException var22) {
                     logger.log(Level.INFO, var22.getMessage(), (Throwable)var22);
                  } catch (NoSuchCreatureException var23) {
                     logger.log(Level.INFO, var23.getMessage(), (Throwable)var23);
                  }
               }

               done = true;
            } catch (NoSuchEntryException var24) {
               logger.log(Level.WARNING, var24.getMessage(), (Throwable)var24);
               done = true;
            } catch (NoSuchSkillException var25) {
               logger.log(Level.WARNING, var25.getMessage(), (Throwable)var25);
               done = true;
            } catch (NoSuchItemException var26) {
               if (var26.getMessage().equalsIgnoreCase("Not done yet.")) {
                  actionResult = true;
               }

               done = true;
            } catch (FailedException var27) {
            } catch (Exception var28) {
               logger.log(Level.WARNING, performer.getName() + " weird:" + var28.getMessage(), (Throwable)var28);
               done = true;
            }

            if (done) {
               performer.getCommunicator().sendActionResult(actionResult);
            }
         }
      } else if (action == 228) {
         done = MethodsItems.polishItem(act, performer, source, target, counter);
      } else if (action == 180) {
         done = true;
         target.deleteAllEffects();
         Items.destroyItem(target.getWurmId());
      } else {
         done = super.action(act, performer, source, target, action, counter);
      }

      return done;
   }
}
