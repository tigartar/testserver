package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Point;
import com.wurmonline.server.Point4f;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemMealData;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.items.Puppet;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Abilities;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.SleepQuestion;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.TempFence;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.utils.logging.TileEvent;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.WaterType;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.util.TerrainUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MethodsItems implements MiscConstants, QuestionTypes, ItemTypes, CounterTypes, ItemMaterials, SoundNames, VillageStatus, TimeConstants {
   private static final float spawnDamageMod = Servers.localServer.isChallengeOrEpicServer() ? 5.0E-4F : 0.005F;
   public static final String cvsversion = "$Id: MethodsItems.java,v 1.84 2007-04-19 23:05:18 root Exp $";
   private static final Logger logger = Logger.getLogger(MethodsItems.class.getName());
   static final byte PICKTYPE_NONE = 0;
   static final byte PICKTYPE_DOOR = 1;
   static final byte PICKTYPE_LARGEVEHICLE = 2;
   static final byte PICKTYPE_GATE = 3;
   private static final int MAX_STRAIGHT_SLOPE = 40;
   private static final int MAX_DIAGONAL_SLOPE = 56;

   static final TakeResultEnum take(Action act, Creature performer, Item target) {
      if (target.isBusy() && act.getNumber() != 925) {
         TakeResultEnum.TARGET_IN_USE.setIndexText(performer.getWurmId(), target.getName());
         return TakeResultEnum.TARGET_IN_USE;
      } else {
         try {
            long ownId = target.getOwner();
            if (ownId != -10L && act.getNumber() != 582) {
               return TakeResultEnum.TARGET_HAS_NO_OWNER;
            }

            if (ownId == performer.getWurmId()) {
               return TakeResultEnum.PERFORMER_IS_OWNER;
            }
         } catch (Exception var23) {
            if (!target.isCoin() && !performer.getPossessions().getInventory().mayCreatureInsertItem() && performer.getPower() <= 0) {
               return TakeResultEnum.INVENTORY_FULL;
            }

            if (target.mailed) {
               return TakeResultEnum.TARGET_IS_UNREACHABLE;
            }

            if (target.isLiquid()) {
               return TakeResultEnum.TARGET_IS_LIQUID;
            }

            if ((target.isBulkContainer() || target.isTent()) && !target.isEmpty(true)) {
               return TakeResultEnum.TARGET_FILLED_BULK_CONTAINER;
            }

            if (target.isBulkItem()) {
               return TakeResultEnum.TARGET_BULK_ITEM;
            }

            if (target.isTent()) {
               Vehicle vehicle = Vehicles.getVehicle(target);
               if (vehicle != null && vehicle.getDraggers() != null && vehicle.getDraggers().size() > 0) {
                  return TakeResultEnum.HITCHED;
               }
            }

            float weight = (float)target.getFullWeight();
            if (weight != 0.0F && !performer.canCarry(target.getFullWeight())) {
               performer.achievement(165);
               TakeResultEnum.CARRYING_TOO_MUCH.setIndexText(performer.getWurmId(), target.getName());
               return TakeResultEnum.CARRYING_TOO_MUCH;
            }

            try {
               BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
               if (result != null) {
                  TakeResultEnum.TARGET_BLOCKED.setIndexText(performer.getWurmId(), target.getName(), result.getFirstBlocker().getName());
                  return TakeResultEnum.TARGET_BLOCKED;
               }

               if (!target.isNoTake()) {
                  boolean sameVehicle = false;
                  Item top = target.getTopParentOrNull();
                  if (top != null && top.isVehicle() && top.getWurmId() == performer.getVehicle()) {
                     sameVehicle = true;
                  }

                  if (!sameVehicle && !performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 5.0F)) {
                     TakeResultEnum.TOO_FAR_AWAY.setIndexText(performer.getWurmId(), target.getName());
                     return TakeResultEnum.TOO_FAR_AWAY;
                  }

                  Zone tzone = Zones.getZone((int)target.getPosX() >> 2, (int)target.getPosY() >> 2, target.isOnSurface());
                  VolaTile tile = tzone.getTileOrNull((int)target.getPosX() >> 2, (int)target.getPosY() >> 2);
                  if (tile != null) {
                     Structure struct = tile.getStructure();
                     VolaTile tile2 = performer.getCurrentTile();
                     if (tile2 != null) {
                        if (tile.getStructure() != struct && (struct == null || !struct.isTypeBridge())) {
                           performer.getCommunicator().sendNormalServerMessage("You can't reach the " + target.getName() + " through the wall.");
                           return TakeResultEnum.TARGET_BLOCKED;
                        }
                     } else if (struct != null && !struct.isTypeBridge()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't reach the " + target.getName() + " through the wall.");
                        return TakeResultEnum.TARGET_BLOCKED;
                     }
                  }

                  long toppar = target.getTopParent();
                  if (!isLootableBy(performer, target)) {
                     return TakeResultEnum.MAY_NOT_LOOT_THAT_ITEM;
                  }

                  boolean mayUseVehicle = true;

                  try {
                     Item topparent = Items.getItem(toppar);
                     if (topparent.isDraggable()) {
                        mayUseVehicle = mayUseInventoryOfVehicle(performer, topparent);
                     }

                     if (!mayUseVehicle
                        && target.lastOwner != performer.getWurmId()
                        && (topparent.isVehicle() && topparent.getLockId() != -10L || Items.isItemDragged(topparent))
                        && performer.getDraggedItem() != topparent) {
                        TakeResultEnum.VEHICLE_IS_WATCHED.setIndexText(performer.getWurmId(), topparent.getName());
                        return TakeResultEnum.VEHICLE_IS_WATCHED;
                     }
                  } catch (NoSuchItemException var21) {
                  }

                  if (checkIfStealing(target, performer, act)) {
                     if (act.getNumber() != 100) {
                        TakeResultEnum.NEEDS_TO_STEAL.setIndexText(performer.getWurmId(), target.getName());
                        return TakeResultEnum.NEEDS_TO_STEAL;
                     }

                     if (Action.checkLegalMode(performer)) {
                        return TakeResultEnum.IN_LEGAL_MODE;
                     }

                     if (!performer.maySteal()) {
                        return TakeResultEnum.MAY_NOT_STEAL;
                     }

                     if (target.getItems().size() > 0) {
                        TakeResultEnum.NEED_TO_BE_EMPTY_BEFORE_THEFT.setIndexText(performer.getWurmId(), target.getName());
                        return TakeResultEnum.NEED_TO_BE_EMPTY_BEFORE_THEFT;
                     }

                     Skill stealing = null;
                     boolean dryRun = false;
                     Village v = Zones.getVillage(target.getTileX(), target.getTileY(), true);
                     if (v != null) {
                        Reputation rep = v.getReputationObject(performer.getWurmId());
                        if (rep != null && rep.getValue() >= 0 && rep.isPermanent()) {
                           dryRun = true;
                        }
                     }

                     if (setTheftEffects(performer, act, target)) {
                        stealing = performer.getStealSkill();
                        stealing.skillCheck((double)target.getQualityLevel(), 0.0, dryRun, 10.0F);
                        return TakeResultEnum.PREVENTED_THEFT;
                     }

                     stealing = performer.getStealSkill();
                     stealing.skillCheck((double)target.getQualityLevel(), 0.0, dryRun, 10.0F);
                  }

                  if (target.getParentId() != -10L && WurmId.getType(target.getParentId()) != 6) {
                     Item parent = Items.getItem(target.getParentId());
                     parent.dropItem(target.getWurmId(), true);
                  } else {
                     long targid = target.getWurmId();
                     if (toppar == targid) {
                        try {
                           Creature[] watchers = target.getWatchers();

                           for(Creature lWatcher : watchers) {
                              lWatcher.getCommunicator().sendCloseInventoryWindow(targid);
                           }
                        } catch (NoSuchCreatureException var20) {
                        }
                     }

                     if (WurmId.getType(target.getParentId()) == 6) {
                        Item parent = Items.getItem(target.getParentId());
                        parent.dropItem(target.getWurmId(), true);
                     }

                     int x = (int)target.getPosX() >> 2;
                     int y = (int)target.getPosY() >> 2;
                     Zone zone = Zones.getZone(x, y, target.isOnSurface());
                     zone.removeItem(target);
                     if (performer.getDraggedItem() == target) {
                        stopDragging(performer, target);
                     }
                  }

                  if (target.isPlanted()
                     && (target.isSign() || target.isStreetLamp() || target.isFlag() || target.isBulkContainer() || target.getTemplateId() == 742)) {
                     target.setIsPlanted(false);
                     if (target.isAbility()) {
                        target.hatching = false;
                        target.setRarity((byte)0);
                     }
                  }

                  if (target == performer.getDraggedItem()) {
                     performer.setDraggedItem(null);
                  }

                  if (target.isMushroom() && target.getLastOwnerId() <= 0L) {
                     performer.achievement(139);
                  }

                  if (target.getTemplate().isContainerWithSubItems()) {
                     ArrayList<Item> toMove = new ArrayList<>();

                     for(Item i : target.getItems()) {
                        if (i.isPlacedOnParent()) {
                           toMove.add(i);
                        }
                     }

                     for(Item i : toMove) {
                        target.dropItem(i.getWurmId(), true);
                        Zones.getZone(i.getTileX(), i.getTileY(), target.isOnSurface()).addItem(i);
                        performer.getCommunicator().sendNormalServerMessage("The " + i.getName() + " drops to the ground.");
                     }
                  }

                  performer.getPossessions().getInventory().insertItem(target);
                  target.setOnBridge(-10L);
                  target.setLastMaintained(WurmCalendar.currentTime);
                  return TakeResultEnum.SUCCESS;
               }
            } catch (NoSuchItemException | NoSuchZoneException var22) {
               logger.log(Level.WARNING, var22.getMessage(), (Throwable)var22);
            }
         }

         return TakeResultEnum.UNKNOWN_FAILURE;
      }
   }

   static final boolean setTheftEffects(Creature performer, Action act, Item target) {
      return setTheftEffects(performer, act, target.getTileX(), target.getTileY(), target.isOnSurface());
   }

   static final boolean setTheftEffects(Creature performer, Action act, int tilex, int tiley, boolean surfaced) {
      boolean noticed = false;
      boolean deityUpset = true;
      Village village = Zones.getVillage(tilex, tiley, performer.isOnSurface());
      if (village != null) {
         if (village.guards.size() > 0 && village.checkGuards(act, performer)) {
            noticed = true;
            if (!village.isEnemy(performer.getCitizenVillage())) {
               performer.setUnmotivatedAttacker();
               if (performer.getKingdomTemplateId() != 3) {
                  if (Servers.localServer.HOMESERVER) {
                     performer.setReputation(Math.max(-3, performer.getReputation() - 35));
                     performer.setStealth(false);
                  } else {
                     performer.setReputation(performer.getReputation() - 35);
                  }
               }

               village.modifyReputations(act, performer);
            } else {
               deityUpset = false;
            }

            performer.getCommunicator().sendNormalServerMessage("A guard has noted you!", (byte)4);
         }
      } else {
         VolaTile tile = Zones.getTileOrNull(tilex, tiley, surfaced);
         if (tile != null && performer.isFriendlyKingdom(tile.getKingdom()) && tile.getKingdom() != 3) {
            Structure struct = tile.getStructure();
            if (struct != null && struct.isFinished()) {
               if (Servers.localServer.HOMESERVER && !performer.isOnPvPServer()) {
                  performer.setUnmotivatedAttacker();
                  performer.setReputation(Math.max(-3, performer.getReputation() - 35));
                  performer.getCommunicator().sendNormalServerMessage("You get the feeling someone noticed you!", (byte)4);
               }

               for(VirtualZone vz : tile.getWatchers()) {
                  try {
                     if (vz.getWatcher() != null && vz.getWatcher().getCurrentTile() != null && vz.getWatcher().isFriendlyKingdom(performer.getKingdomId())) {
                        performer.setUnmotivatedAttacker();
                        boolean cares = false;
                        if (Servers.localServer.HOMESERVER && !performer.isOnPvPServer()) {
                           cares = true;
                           if (vz.getWatcher().isPlayer() && vz.getWatcher().getWurmId() != performer.getWurmId()) {
                              performer.setStealth(false);
                              vz.getWatcher()
                                 .getCommunicator()
                                 .sendNormalServerMessage("You notice " + performer.getName() + " trying to do something fishy!", (byte)4);
                           }

                           if (vz.getWatcher().isKingdomGuard()) {
                              cares = true;
                           }

                           if (!cares) {
                              cares = struct.isGuest(vz.getWatcher());
                           }

                           if (vz.getWatcher().getWurmId() != performer.getWurmId()) {
                              float dist = (float)Math.max(
                                 Math.abs(vz.getWatcher().getCurrentTile().tilex - tilex), Math.abs(vz.getWatcher().getCurrentTile().tiley - tiley)
                              );
                              if (cares
                                 && dist <= 20.0F
                                 && cares
                                 && performer.getStealSkill()
                                       .skillCheck(
                                          (double)(
                                             100
                                                - Math.min(
                                                      Math.abs(vz.getWatcher().getCurrentTile().tilex - tilex),
                                                      Math.abs(vz.getWatcher().getCurrentTile().tiley - tiley)
                                                   )
                                                   * 4
                                          ),
                                          0.0,
                                          false,
                                          10.0F
                                       )
                                    < 0.0) {
                                 noticed = true;
                                 performer.setReputation(performer.getReputation() - 10);
                                 performer.getCommunicator().sendNormalServerMessage("You get the feeling someone noticed you!", (byte)4);
                                 vz.getWatcher()
                                    .getCommunicator()
                                    .sendNormalServerMessage("You notice " + performer.getName() + " trying to do something fishy!", (byte)4);
                                 performer.setStealth(false);
                                 break;
                              }
                           }
                        }
                     }
                  } catch (Exception var16) {
                     logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                  }
               }
            }
         }
      }

      if (deityUpset
         && performer.getDeity() != null
         && !performer.getDeity().isLibila()
         && Server.rand.nextInt(Math.max(1, (int)performer.getFaith())) < 5
         && act.getNumber() != 101) {
         performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " has noticed you and is upset at your thieving behaviour!", (byte)4);
         performer.modifyFaith(-0.25F);
         performer.maybeModifyAlignment(-5.0F);
      }

      return noticed;
   }

   public static final boolean checkIfStealing(Item target, Creature player, @Nullable Action act) {
      if (target.getOwnerId() == -10L) {
         if (player.getPower() > 0) {
            return false;
         }

         if (target.lastOwner == player.getWurmId()) {
            return false;
         }

         if (target.getTemplateId() == 128) {
            return false;
         }

         if (target.getParentId() != -10L) {
            try {
               Item topparent = Items.getItem(target.getTopParent());
               if (topparent.isDraggable() && topparent.isLockable() && mayUseInventoryOfVehicle(player, topparent)) {
                  return false;
               }

               if (topparent.isLocked() && topparent.mayShowPermissions(player) && topparent.mayAccessHold(player)) {
                  return false;
               }

               VolaTile parentTile = Zones.getTileOrNull(topparent.getTilePos(), target.isOnSurface());
               if (parentTile != null && parentTile.getStructure() != null) {
                  try {
                     if (parentTile.getStructure().mayPass(player)) {
                        Item current = target.getParentOrNull();

                        boolean mayEatThis;
                        for(mayEatThis = false; current != null; current = current.getParentOrNull()) {
                           if (current.isLocked() && current.mayAccessHold(player)) {
                              mayEatThis = true;
                           } else if (current.isLocked() && !current.mayAccessHold(player)) {
                              return true;
                           }
                        }

                        if (mayEatThis && act != null && act.getNumber() == 182) {
                           return false;
                        }
                     }

                     if (!mayTakeThingsFromStructure(player, target, parentTile.tilex, parentTile.tiley)
                        && target.lastOwner != -10L
                        && target.lastOwner != player.getWurmId()) {
                        return true;
                     }
                  } catch (NoSuchStructureException var9) {
                  }
               }
            } catch (NoSuchItemException var10) {
            }
         }

         if (act != null && act.getNumber() == 74 && target.isVehicle() && target.mayShowPermissions(player) && target.mayDrag(player)) {
            return false;
         }

         if (act != null && act.getNumber() == 606) {
            return false;
         }

         if (act != null && act.getNumber() == 189 && target.isOilConsuming()) {
            return false;
         }

         if (act != null && act.getNumber() == 183 && target.mayAccessHold(player)) {
            return false;
         }

         if (act == null && target.mayShowPermissions(player) && target.mayAccessHold(player)) {
            return false;
         }

         int tilex = target.getTileX();
         int tiley = target.getTileY();
         Village village = Zones.getVillage(tilex, tiley, target.isOnSurface());
         if (village != null) {
            if (!player.isOnPvPServer() || player.isFriendlyKingdom(village.kingdom) && !village.isEnemy(player.getCitizenVillage())) {
               try {
                  if (!mayTakeThingsFromStructure(player, target, tilex, tiley) && target.lastOwner != -10L && target.lastOwner != player.getWurmId()) {
                     return true;
                  }
               } catch (NoSuchStructureException var8) {
                  if (act != null && act.getNumber() == 83) {
                     if (target.lastOwner != -10L && target.lastOwner != player.getWurmId() && !Methods.isActionAllowed(player, (short)83, target)) {
                        return true;
                     }
                  } else {
                     if (target.lastOwner != -10L && target.lastOwner != player.getWurmId() && !village.isActionAllowed((short)6, player, false, 0, 0)) {
                        return true;
                     }

                     if (act != null
                        && act.getNumber() == 6
                        && target.isPlanted()
                        && (target.isSign() || target.isStreetLamp() || target.isFlag() || target.isBulkContainer() || target.getTemplateId() == 742)
                        && target.lastOwner != -10L
                        && target.lastOwner != player.getWurmId()
                        && !village.isActionAllowed((short)685, player, false, 0, 0)) {
                        return true;
                     }
                  }
               }

               return isStealingInVicinity(target, player);
            }
         } else if (player.getKingdomTemplateId() != 3) {
            try {
               if (!mayTakeThingsFromStructure(player, target, tilex, tiley) && target.lastOwner != -10L && target.lastOwner != player.getWurmId()) {
                  return true;
               }
            } catch (NoSuchStructureException var7) {
            }

            return isStealingInVicinity(target, player);
         }
      }

      return false;
   }

   public static final boolean isStealingInVicinity(Item target, Creature player) {
      if (!Servers.localServer.PVPSERVER
         && target.lastOwner != -10L
         && target.lastOwner != player.getWurmId()
         && player.getPower() < 2
         && WurmId.getType(target.lastOwner) == 0) {
         try {
            Player lastOwner = Players.getInstance().getPlayer(target.lastOwner);
            if (lastOwner.getPower() < 2
               && (lastOwner.getCitizenVillage() == null || lastOwner.getCitizenVillage() != player.getCitizenVillage())
               && !lastOwner.isFriend(player.getWurmId())
               && (lastOwner.getCitizenVillage() == null || !lastOwner.getCitizenVillage().isAlly(player))
               && (lastOwner.getTeam() == null || lastOwner.getTeam() != player.getTeam())
               && lastOwner.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), target.isVehicle() ? 100.0F : 10.0F)) {
               return true;
            }
         } catch (NoSuchPlayerException var3) {
         }
      }

      return false;
   }

   public static final boolean mayTakeThingsFromStructure(Creature performer, @Nullable Item item, int tilex, int tiley) throws NoSuchStructureException {
      boolean onSurface = false;
      if (item == null) {
         onSurface = performer.isOnSurface();
      } else {
         onSurface = item.isOnSurface();
      }

      VolaTile tile = Zones.getTileOrNull(tilex, tiley, onSurface);
      if (tile != null) {
         Structure struct = tile.getStructure();
         if (performer.isInPvPZone()) {
            return true;
         }

         if (struct != null && struct.isFinished() && struct.isTypeHouse()) {
            if (!struct.isEnemy(performer) && !struct.mayPass(performer)) {
               return false;
            }

            long wid = performer.getWurmId();
            if (wid != struct.getOwnerId()) {
               if (!struct.isActionAllowed(performer, (short)6)) {
                  return false;
               }

               if (item != null && item.isPlanted()) {
                  return struct.isActionAllowed(performer, (short)685);
               }
            }

            return true;
         }
      }

      throw new NoSuchStructureException("No structure");
   }

   public static final boolean isEnemiesNearby(Creature performer, int tileDist, boolean requireSameVillage) {
      int sx = Zones.safeTileX(performer.getTileX() - tileDist);
      int sy = Zones.safeTileY(performer.getTileY() - tileDist);
      int ex = Zones.safeTileX(performer.getTileX() + tileDist);
      int ey = Zones.safeTileY(performer.getTileY() + tileDist);

      for(int x = sx; x <= ex; ++x) {
         for(int y = sy; y <= ey; ++y) {
            VolaTile t = Zones.getTileOrNull(x, y, performer.isOnSurface());
            if (t != null) {
               for(Creature c : t.getCreatures()) {
                  if (c.isPlayer() && !c.isFriendlyKingdom(performer.getKingdomId()) && c.isPaying() && c.getPower() == 0) {
                     if (!requireSameVillage) {
                        return true;
                     }

                     if (c.getCurrentVillage() == performer.getCurrentVillage()) {
                        return true;
                     }
                  }
               }
            }

            VolaTile oT = Zones.getTileOrNull(x, y, !performer.isOnSurface());
            if (oT != null) {
               for(Creature c : oT.getCreatures()) {
                  if (c.isPlayer() && !c.isFriendlyKingdom(performer.getKingdomId()) && c.isPaying() && c.getPower() == 0) {
                     if (!requireSameVillage) {
                        return true;
                     }

                     if (c.getCurrentVillage() == performer.getCurrentVillage()) {
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public static final boolean isLootableBy(Creature performer, Item target) {
      if (performer.getPower() > 0) {
         return true;
      } else if (target.getTemplateId() == 272 && target.getWasBrandedTo() != -10L) {
         return target.mayCommand(performer);
      } else {
         long targid = target.getWurmId();
         long toppar = target.getTopParent();
         Item topparent = null;

         try {
            if (toppar != targid) {
               topparent = Items.getItem(toppar);
            } else {
               topparent = target;
            }

            if (topparent.isTent() || topparent.isUseMaterialAndKingdom()) {
               if (topparent.isNewbieItem() && !Servers.localServer.PVPSERVER) {
                  if (topparent.lastOwner != performer.getWurmId()) {
                     return false;
                  }
               } else if (topparent.getLockId() > -10L && topparent.lastOwner != performer.getWurmId()) {
                  return false;
               }
            }

            if (topparent.getTemplateId() != 272) {
               return true;
            } else {
               VolaTile ttile = Zones.getTileOrNull(topparent.getTileX(), topparent.getTileY(), topparent.isOnSurface());
               int dist = ttile != null && ttile.getVillage() != null ? 5 : 10;
               if (Servers.localServer.isChallengeOrEpicServer() && isEnemiesNearby(performer, dist, true)) {
                  return false;
               } else {
                  if (!topparent.isCorpseLootable() && WurmId.getType(topparent.lastOwner) == 0 && topparent.lastOwner != performer.getWurmId()) {
                     byte kingdom = Players.getInstance().getKingdomForPlayer(topparent.lastOwner);
                     if (kingdom != 3 && kingdom == performer.getKingdomId() && !Servers.isThisAChaosServer()) {
                        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(topparent.lastOwner);
                        if (pinf != null) {
                           if (!pinf.isFlagSet(34)) {
                              Friend[] friends = pinf.getFriends();

                              for(Friend lFriend : friends) {
                                 if (lFriend.getFriendId() == performer.getWurmId() && lFriend.getCategory() == Friend.Category.Trusted) {
                                    return true;
                                 }
                              }
                           }

                           try {
                              Village citizenVillage = Villages.getVillageForCreature(topparent.lastOwner);
                              if (citizenVillage != null) {
                                 if (citizenVillage.isCitizen(performer) && !pinf.isFlagSet(33)) {
                                    return true;
                                 }

                                 if (citizenVillage.isAlly(performer) && !pinf.isFlagSet(32)) {
                                    return true;
                                 }
                              }

                              Player p = Players.getInstance().getPlayer(topparent.lastOwner);
                              if (p.getCitizenVillage() != null) {
                                 if (p.getCitizenVillage().isCitizen(performer) && !pinf.isFlagSet(33)) {
                                    return true;
                                 }

                                 if (p.getCitizenVillage().isAlly(performer) && !pinf.isFlagSet(32)) {
                                    return true;
                                 }
                              }
                           } catch (NoSuchPlayerException var16) {
                           }
                        }

                        return false;
                     }
                  }

                  return true;
               }
            }
         } catch (NoSuchItemException var17) {
            logger.log(Level.INFO, "No top parent for " + target.getTopParent());
            return false;
         }
      }
   }

   static final boolean mayDropDirt(Creature performer) {
      int dropTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
      int dropTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
      Point tiles = findDropTile(dropTileX, dropTileY, performer.isOnSurface() ? Server.surfaceMesh : Server.caveMesh);
      dropTileX = tiles.getX();
      dropTileY = tiles.getY();

      for(int x = 0; x >= -1; --x) {
         for(int y = 0; y >= -1; --y) {
            Village village = Zones.getVillage(dropTileX + x, dropTileY + y, performer.isOnSurface());
            if (village != null && !village.isActionAllowed((short)37, performer)) {
               return false;
            }

            if (Zones.protectedTiles[dropTileX + x][dropTileY + y]) {
               performer.getCommunicator().sendAlertServerMessage("The tile is protected by the gods.");
               return false;
            }
         }
      }

      if (Features.Feature.WAGONER.isEnabled() && MethodsHighways.onWagonerCamp(dropTileX, dropTileY, performer.isOnSurface())) {
         performer.getCommunicator().sendNormalServerMessage("The wagoner whips you once and tells you never to try dropping that here again.");
         return false;
      } else if (Terraforming.wouldDestroyCobble(performer, dropTileX, dropTileY, true)) {
         if (Features.Feature.HIGHWAYS.isEnabled()) {
            performer.getCommunicator().sendAlertServerMessage("The tile is protected by the highway.", (byte)3);
         } else {
            performer.getCommunicator().sendAlertServerMessage("This would destroy the pavement.", (byte)3);
         }

         return false;
      } else {
         return true;
      }
   }

   public static void handlePlaceItem(Creature performer, long itemId, long parentId, float xPos, float yPos, float zPos, float rotation) {
      boolean largeItem = performer.getPlacementItem() != null && performer.getPlacementItem().getWurmId() == itemId;
      if (!performer.isPlacingItem()) {
         performer.getCommunicator().sendNormalServerMessage("An error occured while placing that item.");
      } else {
         if (!largeItem) {
            performer.setPlacingItem(false);
         }

         if (itemId == -10L) {
            performer.getCommunicator().sendNormalServerMessage("You decide against placing the item.");
            performer.setPlacingItem(false);
         } else {
            Item parent = null;
            if (parentId != -10L) {
               try {
                  parent = Items.getItem(parentId);
               } catch (NoSuchItemException var30) {
                  parent = null;
               }
            }

            if (parent != null && largeItem) {
               performer.getCommunicator().sendNormalServerMessage("You must place the item from your inventory to put it there.");
               performer.setPlacingItem(false);
            } else {
               float newPosX = Math.max(1.0F, parent != null ? parent.getPosX() + xPos : xPos);
               float newPosY = Math.max(1.0F, parent != null ? parent.getPosY() + yPos : yPos);
               newPosY = Math.min(Zones.worldMeterSizeY, newPosY);
               newPosX = Math.min(Zones.worldMeterSizeX, newPosX);
               if (!Methods.isActionAllowed(performer, (short)(largeItem ? 99 : 7), (int)newPosX >> 2, (int)newPosY >> 2)
                  && parent != null
                  && Methods.isActionAllowed(performer, (short)3, parent)) {
                  performer.setPlacingItem(false);
               } else {
                  float xDist = Math.abs(performer.getPosX() - newPosX);
                  float yDist = Math.abs(performer.getPosY() - newPosY);
                  if ((xDist > 4.0F || yDist > 4.0F) && performer.getPower() < 2) {
                     performer.getCommunicator().sendNormalServerMessage("You cannot place the item that far away.");
                     performer.setPlacingItem(false);
                  } else {
                     try {
                        Item target = Items.getItem(itemId);
                        boolean onSurface = performer.isOnSurface();
                        BlockingResult result = Blocking.getBlockerBetween(
                           performer,
                           performer.getPosX(),
                           performer.getPosY(),
                           newPosX,
                           newPosY,
                           performer.getPositionZ(),
                           performer.getPositionZ(),
                           onSurface,
                           onSurface,
                           false,
                           4,
                           -1L,
                           performer.getBridgeId(),
                           performer.getBridgeId(),
                           false
                        );
                        if (result != null) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot reach that spot to place the " + target.getName() + ".");
                           return;
                        }

                        if (target.isOnePerTile() && !mayDropOnTile((int)newPosX >> 2, (int)newPosY >> 2, onSurface, performer.getFloorLevel())) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You cannot place that item here, since there is not enough space on that tile.", (byte)3);
                           performer.setPlacingItem(false);
                           return;
                        }

                        if (target.isTent() && !mayDropTentOnTile(performer)) {
                           performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
                           performer.setPlacingItem(false);
                           return;
                        }

                        if (!onSurface
                           && Tiles.isSolidCave(
                              Tiles.decodeType(Server.caveMesh.getTile(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2)))
                           )) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot place the " + target.getName() + " inside the wall.");
                           performer.setPlacingItem(false);
                           return;
                        }

                        if (parent != null) {
                           if (!parent.testInsertItem(target) || !parent.mayCreatureInsertItem()) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("There is no room for the " + target.getName() + " on the " + parent.getName() + ".");
                              performer.setPlacingItem(false);
                              return;
                           }

                           if (target.isUnfinished() && target.getRealTemplate() != null && target.getRealTemplate().getVolume() > parent.getFreeVolume()) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("There is no room for the " + target.getName() + " on the " + parent.getName() + ".");
                              performer.setPlacingItem(false);
                              return;
                           }

                           if (parent.getPlacedItemCount() >= parent.getMaxPlaceableItems()) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    "You cannot put the " + target.getName() + " there, that would mean too many items on the " + parent.getName() + "."
                                 );
                              performer.setPlacingItem(false);
                              return;
                           }
                        }

                        if (!performer.isWithinDistanceTo(newPosX, newPosY, zPos, 6.0F) && performer.getPower() < 2) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot place the item that far away.");
                           performer.setPlacingItem(false);
                           return;
                        }

                        if (largeItem) {
                           float actualDist = (float)Math.sqrt((double)(xDist * xDist + yDist * yDist));
                           float totalTime = (float)(
                                 (double)((float)performer.getPlacementItem().getWeightGrams(true) / 100000.0F * actualDist)
                                    * (20.0 / performer.getBodyStrength().getKnowledge(0.0))
                              )
                              * 100.0F;

                           try {
                              performer.getCurrentAction().setTimeLeft((int)Math.max(50.0F, Math.min(900.0F, totalTime)));
                              performer.getCurrentAction().resetCounter();
                              performer.sendActionControl(Actions.actionEntrys[926].getActionString(), true, (int)Math.max(50.0F, Math.min(900.0F, totalTime)));
                              float rot = target.isVehicle() ? rotation + 180.0F : rotation;

                              while(rot > 360.0F) {
                                 rot -= 360.0F;
                              }

                              while(rot < 0.0F) {
                                 rot += 360.0F;
                              }

                              performer.setPendingPlacement(newPosX, newPosY, zPos, rot);
                           } catch (NoSuchActionException var32) {
                              performer.getCommunicator().sendNormalServerMessage("An error occured while placing that item, please try again.");
                              performer.setPlacingItem(false, null);
                           }
                        } else {
                           try {
                              Zone zone = Zones.getZone(Zones.safeTileX((int)newPosX >> 2), Zones.safeTileY((int)newPosY >> 2), onSurface);
                              long lParentId = target.getParentId();
                              if (lParentId != -10L) {
                                 Item currentParent = Items.getItem(lParentId);
                                 currentParent.dropItem(target.getWurmId(), false);
                              }

                              if (parent != null) {
                                 target.setPos(newPosX - parent.getPosX(), newPosY - parent.getPosY(), zPos, rotation, parent.onBridge());
                                 if (parent.insertItem(target, false, false, true)) {
                                    if ((parent.isLight() || parent.isFire() || parent.getTemplate().isCooker()) && target.isBurnable()) {
                                       performer.getCommunicator()
                                          .sendNormalServerMessage(
                                             "The " + target.getName() + " will take damage if the " + parent.getName() + " is lit.", (byte)4
                                          );
                                    }

                                    if (parent.getTemplate().isContainerWithSubItems()) {
                                       target.setPlacedOnParent(true);
                                    }

                                    VolaTile vt = Zones.getTileOrNull(parent.getTileX(), parent.getTileY(), parent.isOnSurface());
                                    if (vt != null) {
                                       for(VirtualZone vz : vt.getWatchers()) {
                                          if (vz.isVisible(parent, vt)) {
                                             vz.getWatcher().getCommunicator().sendItem(target, -10L, false);
                                             if (target.isLight() && target.isOnFire()) {
                                                vt.addLightSource(target);
                                             }

                                             if (target.getEffects().length > 0) {
                                                for(Effect e : target.getEffects()) {
                                                   vz.addEffect(e, false);
                                                }
                                             }

                                             if (target.getColor() != -1) {
                                                vz.sendRepaint(
                                                   target.getWurmId(),
                                                   (byte)WurmColor.getColorRed(target.getColor()),
                                                   (byte)WurmColor.getColorGreen(target.getColor()),
                                                   (byte)WurmColor.getColorBlue(target.getColor()),
                                                   (byte)-1,
                                                   (byte)0
                                                );
                                             }

                                             if (target.getColor2() != -1) {
                                                vz.sendRepaint(
                                                   target.getWurmId(),
                                                   (byte)WurmColor.getColorRed(target.getColor2()),
                                                   (byte)WurmColor.getColorGreen(target.getColor2()),
                                                   (byte)WurmColor.getColorBlue(target.getColor2()),
                                                   (byte)-1,
                                                   (byte)1
                                                );
                                             }
                                          }
                                       }
                                    }

                                    performer.achievement(509);
                                 }
                              } else {
                                 target.setOnBridge(performer.getBridgeId());
                                 float npsz = Zones.calculatePosZ(
                                    newPosX,
                                    newPosY,
                                    null,
                                    onSurface,
                                    target.isFloating() && target.getCurrentQualityLevel() > 10.0F,
                                    performer.getPositionZ(),
                                    performer,
                                    target.onBridge()
                                 );
                                 float rot = target.isVehicle() ? rotation + 180.0F : rotation;

                                 while(rot > 360.0F) {
                                    rot -= 360.0F;
                                 }

                                 while(rot < 0.0F) {
                                    rot += 360.0F;
                                 }

                                 target.setPos(newPosX, newPosY, npsz, rot, target.onBridge());
                                 target.setSurfaced(onSurface);
                                 zone.addItem(target);
                              }

                              SoundPlayer.playSound("sound.object.move.pushpull", target, 0.0F);
                              performer.getCommunicator().sendNormalServerMessage("You place " + target.getNameWithGenus() + ".");
                              Server.getInstance().broadCastAction(performer.getName() + " places " + target.getNameWithGenus() + ".", performer, 5);
                              PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.PLACED_ITEM);
                           } catch (NoSuchZoneException var31) {
                              logger.log(Level.WARNING, var31.getMessage(), (Throwable)var31);
                              performer.getCommunicator().sendNormalServerMessage("Unable to place the " + target.getName() + " there.");
                           }
                        }
                     } catch (NoSuchItemException var33) {
                        logger.log(Level.INFO, "Unable to find item " + itemId + " from " + performer.getName() + " place item response.");
                     }
                  }
               }
            }
         }
      }
   }

   static boolean placeItem(Creature performer, Item target, Action act, float counter) {
      if (counter > 1.0F && !performer.isPlacingItem()) {
         return true;
      } else if (!target.canBeDropped(true)) {
         if (target.isHollow()) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to drop that. Make sure it doesn't contain non-dropable items.", (byte)3);
         } else {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to drop that.", (byte)3);
         }

         return true;
      } else if (target.isOnePerTile() && !mayDropOnTile(performer)) {
         performer.getCommunicator().sendNormalServerMessage("You cannot drop that item here, since there is not enough space in front of you.", (byte)3);
         return true;
      } else if (target.isTent() && !mayDropTentOnTile(performer)) {
         performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
         return true;
      } else {
         long ownId = target.getOwnerId();
         if (ownId == -10L) {
            return true;
         } else if (ownId != performer.getWurmId()) {
            logger.log(Level.WARNING, "Hmm " + performer.getName() + " tried to drop " + target.getName() + " which wasn't his.");
            return true;
         } else if (!Methods.isActionAllowed(performer, (short)7)) {
            return true;
         } else if (counter == 1.0F) {
            performer.getCommunicator().sendNormalServerMessage("You start to place " + target.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to place " + target.getNameWithGenus() + ".", performer, 5);
            act.setTimeLeft(1200);
            performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
            performer.getCommunicator().sendPlaceItem(target);
            performer.setPlacingItem(true);
            return false;
         } else if (performer.isPlacingItem()) {
            if (counter * 10.0F >= (float)act.getTimeLeft()) {
               performer.getCommunicator().sendNormalServerMessage("You decide against placing " + target.getNameWithGenus() + ".");
               performer.getCommunicator().sendCancelPlacingItem();
               performer.setPlacingItem(false, null);
               return true;
            } else {
               return false;
            }
         } else {
            return true;
         }
      }
   }

   static boolean placeLargeItem(Creature performer, Item target, Action act, float counter) {
      if (!(counter > 1.0F) || performer.isPlacingItem() && performer.getPlacementItem() == target) {
         if (target.getParentId() != -10L) {
            performer.getCommunicator().sendNormalServerMessage("You can not place that right now.", (byte)3);
            return true;
         } else if (performer.isGuest()) {
            performer.getCommunicator().sendNormalServerMessage("Sorry, but we cannot allow our guests to place items.", (byte)3);
            return true;
         } else if (!target.isTurnable(performer) || !target.isMoveable(performer)) {
            performer.getCommunicator().sendNormalServerMessage("Sorry, but you are not allowed to place that.", (byte)3);
            return true;
         } else if (checkIfStealing(target, performer, act)) {
            if (!performer.maySteal()) {
               performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.", (byte)3);
               return true;
            } else {
               performer.getCommunicator().sendNormalServerMessage("You have to steal the " + target.getName() + " instead.", (byte)3);
               return true;
            }
         } else if (target.isCorpse() && target.getWasBrandedTo() != -10L && !target.mayCommand(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You may not move the corpse as you do not have permissions.", (byte)3);
            return true;
         } else {
            if (target.isCorpse() && Servers.localServer.isChallengeOrEpicServer()) {
               VolaTile ttile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
               int distance = ttile != null && ttile.getVillage() != null ? 5 : 10;
               if (isEnemiesNearby(performer, distance, true)) {
                  performer.getCommunicator().sendNormalServerMessage("You may not move the corpse when there are enemies nearby.", (byte)3);
                  return true;
               }
            }

            if (Items.isItemDragged(target)) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + target.getName() + " is being dragged and may not be moved that way at the moment.", (byte)3);
               return true;
            } else {
               Vehicle vehicle = Vehicles.getVehicle(target);
               boolean performerIsAllowedToDriveVehicle = false;
               if (vehicle != null) {
                  for(Seat lSeat : vehicle.seats) {
                     if (lSeat.isOccupied()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The " + target.getName() + " is occupied and may not be moved that way at the moment.", (byte)3);
                        return true;
                     }
                  }

                  if (vehicle.draggers != null && vehicle.draggers.size() > 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " may not be moved that way at the moment.", (byte)3);
                     return true;
                  }

                  if (VehicleBehaviour.mayDriveVehicle(performer, target, act) && VehicleBehaviour.canBeDriverOfVehicle(performer, vehicle)) {
                     performerIsAllowedToDriveVehicle = true;
                  }
               }

               if (target.isBoat() && target.getData() != -1L) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " won't budge. It's moored.", (byte)3);
                  return true;
               } else if ((!performerIsAllowedToDriveVehicle || !target.isVehicle()) && !Methods.isActionAllowed(performer, (short)99, target)) {
                  return true;
               } else if (counter == 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You start to place " + target.getNameWithGenus() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to place " + target.getNameWithGenus() + ".", performer, 5);
                  act.setTimeLeft(1200);
                  performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
                  performer.getCommunicator().sendPlaceItem(target);
                  performer.setPlacingItem(true, target);
                  return false;
               } else if (!performer.isPlacingItem()) {
                  return true;
               } else {
                  if (performer.getPendingPlacement() != null) {
                     if (act.justTickedSecond()) {
                        float[] targetPoint = performer.getPendingPlacement();
                        if (targetPoint == null) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You somehow forget where you were moving " + target.getNameWithGenus() + " to.");
                           performer.getCommunicator().sendCancelPlacingItem();
                           performer.setPlacingItem(false, null);
                           return true;
                        }

                        float percentComplete = Math.min(1.0F, (counter - 1.0F) * 10.0F / (float)act.getTimeLeft());
                        float[] diff = new float[]{
                           (targetPoint[4] - targetPoint[0]) * percentComplete,
                           (targetPoint[5] - targetPoint[1]) * percentComplete,
                           (targetPoint[6] - targetPoint[2]) * percentComplete,
                           (targetPoint[7] - targetPoint[3]) * percentComplete
                        };
                        float newPosX = targetPoint[0] + diff[0];
                        float newPosY = targetPoint[1] + diff[1];
                        float newPosZ = targetPoint[2] + diff[2];
                        float rotation = targetPoint[3] + diff[3];
                        boolean onSurface = performer.getPlacementItem().isOnSurface();

                        try {
                           Zone oldZone = Zones.getZone(target.getTileX(), target.getTileY(), target.isOnSurface());
                           oldZone.removeItem(target, true, true);
                           long lParentId = target.getParentId();
                           if (lParentId != -10L) {
                              Item parent = Items.getItem(lParentId);
                              parent.dropItem(target.getWurmId(), false);
                           }

                           target.setOnBridge(performer.getBridgeId());
                           float npsz = Zones.calculatePosZ(
                              newPosX,
                              newPosY,
                              null,
                              onSurface,
                              target.isFloating() && target.getCurrentQualityLevel() > 10.0F,
                              performer.getPositionZ(),
                              performer,
                              target.onBridge()
                           );
                           target.setPos(newPosX, newPosY, npsz, rotation, target.onBridge());
                           target.setSurfaced(onSurface);
                           Zone newZone = Zones.getZone(target.getTileX(), target.getTileY(), onSurface);
                           newZone.addItem(target, true, false, false);
                           performer.getStatus().modifyStamina(-1000.0F);
                        } catch (NoSuchItemException | NoSuchZoneException var19) {
                           logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
                           performer.getCommunicator().sendNormalServerMessage("Unable to place the " + target.getName() + " there.");
                        }

                        if (percentComplete >= 1.0F) {
                           performer.getCommunicator().sendNormalServerMessage("You finish placing " + target.getNameWithGenus() + ".");
                           Server.getInstance().broadCastAction(performer.getName() + " finishes placing " + target.getNameWithGenus() + ".", performer, 5);
                           performer.getCommunicator().sendCancelPlacingItem();
                           performer.setPlacingItem(false, null);
                           return true;
                        }

                        return false;
                     }
                  } else if (counter * 10.0F >= (float)act.getTimeLeft()) {
                     performer.getCommunicator().sendNormalServerMessage("You decide against placing " + target.getNameWithGenus() + ".");
                     performer.getCommunicator().sendCancelPlacingItem();
                     performer.setPlacingItem(false, null);
                     return true;
                  }

                  return false;
               }
            }
         }
      } else {
         return true;
      }
   }

   static String[] drop(Creature performer, Item target, boolean onGround) {
      String[] fail = new String[0];
      if (!target.canBeDropped(true)) {
         if (target.isHollow()) {
            performer.getCommunicator().sendSafeServerMessage("You are not allowed to drop that. Make sure it doesn't contain non-dropable items.");
         } else {
            performer.getCommunicator().sendSafeServerMessage("You are not allowed to drop that.", (byte)3);
         }
      } else {
         try {
            if (target.isOnePerTile() && !mayDropOnTile(performer)) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You cannot drop that item here, since there is not enough space in front of you.", (byte)3);
               return fail;
            }

            if (target.isBeingWorkedOn()) {
               performer.getCommunicator().sendNormalServerMessage("You cannot drop that, since you are busy working with it.", (byte)3);
               return fail;
            }

            if (target.isTent() && !mayDropTentOnTile(performer)) {
               performer.getCommunicator().sendNormalServerMessage("You are not allowed to put your tent there.", (byte)3);
               return fail;
            }

            long ownId = target.getOwnerId();
            if (ownId == -10L) {
               return fail;
            }

            if (ownId != performer.getWurmId()) {
               logger.log(Level.WARNING, "Hmm " + performer.getName() + " tried to drop " + target.getName() + " which wasn't his.");
               return fail;
            }

            if (!Methods.isActionAllowed(performer, (short)7)) {
               return fail;
            }

            boolean dropAsPile = true;
            if (!performer.isOnSurface() || performer.getCurrentTile().isTransition) {
               dropAsPile = false;
            }

            if (dropAsPile && onGround && (target.getTemplateId() == 26 || target.getTemplateId() == 298)) {
               int dropTileX = (int)performer.getStatus().getPositionX() + 2 >> 2;
               int dropTileY = (int)performer.getStatus().getPositionY() + 2 >> 2;
               if (dropTileX < 0 || dropTileX > 1 << Constants.meshSize || dropTileY < 0 || dropTileY > 1 << Constants.meshSize) {
                  String tName = target.getName();
                  Items.destroyItem(target.getWurmId());
                  return new String[]{"The deep water absorbs the ", tName, " and it disappears in the currents.", " into deep water and it vanishs."};
               }

               MeshIO mesh = Server.surfaceMesh;
               if (!performer.isOnSurface()) {
                  mesh = Server.caveMesh;
               }

               int digTile = mesh.getTile(dropTileX, dropTileY);
               short h = Tiles.decodeHeight(digTile);
               Point tiles = findDropTile(dropTileX, dropTileY, mesh);
               dropTileX = tiles.getX();
               dropTileY = tiles.getY();

               for(int x = 0; x >= -1; --x) {
                  for(int y = 0; y >= -1; --y) {
                     if (!Methods.isActionAllowed(performer, (short)37)) {
                        return fail;
                     }
                  }
               }

               if (!mayDropDirt(performer)) {
                  return fail;
               }

               for(int x = 0; x >= -1; --x) {
                  for(int y = 0; y >= -1; --y) {
                     VolaTile tile = Zones.getTileOrNull(dropTileX + x, dropTileY + y, performer.isOnSurface());
                     if (tile != null) {
                        Structure struct = tile.getStructure();
                        if (struct != null) {
                           if (!struct.isTypeBridge() || tile.getBridgeParts() == null) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    "The dirt would flow down into a structure. You are not allowed to drop dirt on structures.", (byte)3
                                 );
                              return fail;
                           }

                           BridgePart bridgePart = tile.getBridgeParts()[0];
                           if (bridgePart.getType().isSupportType()) {
                              performer.getCommunicator().sendNormalServerMessage("The bridge support nearby prevents dropping dirt.", (byte)3);
                              return fail;
                           }

                           if (x == -1 && bridgePart.hasEastExit()
                              || x == 0 && bridgePart.hasWestExit()
                              || y == -1 && bridgePart.hasSouthExit()
                              || y == 0 && bridgePart.hasNorthExit()) {
                              performer.getCommunicator().sendNormalServerMessage("You are too close to the end of the bridge to drop dirt here.", (byte)3);
                              return fail;
                           }

                           int bridgeHeight = bridgePart.getRealHeight();
                           int tileHeight = Tiles.decodeHeight(mesh.getTile(dropTileX, dropTileY));
                           if (bridgeHeight - tileHeight < 25) {
                              performer.getCommunicator().sendNormalServerMessage("You are too close to the bottom of the bridge to drop the dirt.", (byte)3);
                              return fail;
                           }
                        }

                        Fence[] fences = tile.getFencesForLevel(0);

                        for(Fence f : fences) {
                           if (y == 0 && f.isHorizontal() || x == 0 && !f.isHorizontal()) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("The dirt would flow down onto a fence. You are not allowed to drop dirt on fences.", (byte)3);
                              return fail;
                           }
                        }
                     }

                     if (!performer.isPaying()) {
                        int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
                        if (Terraforming.isRoad(Tiles.decodeType(newTile))) {
                           performer.getCommunicator()
                              .sendNormalServerMessage(
                                 "The dirt would flow down onto a road. Only premium players are allowed to drop dirt on roads.", (byte)3
                              );
                           return fail;
                        }
                     }
                  }
               }

               String tNameg = target.getNameWithGenus();
               boolean change = false;
               if (target.getWeightGrams() >= 20000) {
                  change = true;
               }

               String tName = target.getName();

               for(int x = 0; x >= -1; --x) {
                  for(int y = 0; y >= -1; --y) {
                     int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
                     byte newType = Tiles.decodeType(newTile);
                     if (newType == Tiles.Tile.TILE_HOLE.id || Tiles.isMineDoor(newType)) {
                        performer.getCommunicator().sendNormalServerMessage("The dirt would flow down into a mine.", (byte)3);
                        return fail;
                     }

                     if (newType == Tiles.Tile.TILE_LAVA.id) {
                        Items.destroyItem(target.getWurmId());
                        return new String[]{"The ", tName, " disappears into the lava.", " which then disappears into the lava."};
                     }
                  }
               }

               Items.destroyItem(target.getWurmId());
               if (change) {
                  for(int x = 0; x >= -1; --x) {
                     for(int y = 0; y >= -1; --y) {
                        boolean changed = false;
                        int newTile = mesh.getTile(dropTileX + x, dropTileY + y);
                        byte newType = Tiles.decodeType(newTile);
                        byte oldType = newType;
                        h = Tiles.decodeHeight(newTile);
                        short mod = 0;
                        if (x == 0 && y == 0) {
                           mod = 1;
                           changed = true;
                        }

                        short newHeight = (short)Math.min(32767, h + mod);
                        if (newType == Tiles.Tile.TILE_ROCK.id
                           || newType == Tiles.Tile.TILE_DIRT.id
                           || newType == Tiles.Tile.TILE_DIRT_PACKED.id
                           || newType == Tiles.Tile.TILE_STEPPE.id
                           || newType == Tiles.Tile.TILE_SAND.id
                           || newType == Tiles.Tile.TILE_CLIFF.id) {
                           if (target.getTemplateId() == 298) {
                              newType = Tiles.Tile.TILE_SAND.id;
                           } else if (target.getTemplateId() == 26) {
                              newType = Tiles.Tile.TILE_DIRT.id;
                           }

                           if (oldType != newType) {
                              TileEvent.log(dropTileX + x, dropTileY + y, 0, performer.getWurmId(), 7);
                           }

                           mesh.setTile(dropTileX + x, dropTileY + y, Tiles.encode(newHeight, newType, (byte)0));
                           Server.modifyFlagsByTileType(dropTileX + x, dropTileY + y, newType);
                           Server.isDirtHeightLower(dropTileX + x, dropTileY + y, newHeight);
                           changed = true;
                        } else if (mod != 0) {
                           mesh.setTile(dropTileX + x, dropTileY + y, Tiles.encode(newHeight, newType, Tiles.decodeData(newTile)));
                           Server.modifyFlagsByTileType(dropTileX + x, dropTileY + y, newType);
                           Server.isDirtHeightLower(dropTileX + x, dropTileY + y, newHeight);
                           changed = true;
                        }

                        if (changed) {
                           try {
                              Zone toCheckForChange = Zones.getZone(dropTileX + x, dropTileY + y, performer.isOnSurface());
                              toCheckForChange.changeTile(dropTileX + x, dropTileY + y);
                           } catch (NoSuchZoneException var26) {
                              logger.log(Level.INFO, "no such zone?: " + (dropTileX + x) + ", " + (dropTileY + y), (Throwable)var26);
                           }

                           Players.getInstance().sendChangedTile(dropTileX + x, dropTileY + y, performer.isOnSurface(), true);
                        }

                        Tiles.Tile theTile = Tiles.getTile(newType);
                        if (theTile.isTree()) {
                           byte data = Tiles.decodeData(newTile);
                           Zones.reposWildHive(dropTileX + x, dropTileY + y, theTile, data);
                        }
                     }
                  }

                  return new String[]{"You drop ", tNameg, ".", "."};
               }

               performer.getCommunicator().sendNormalServerMessage("You pour the " + tName + " on the ground. It's too little matter to change anything.");
            } else {
               if (!target.isLiquid()) {
                  if (!target.isCoin() && (performer.getPower() == 0 || Servers.localServer.testServer)) {
                     int[] tilecoords = Item.getDropTile(performer);
                     VolaTile t = Zones.getTileOrNull(tilecoords[0], tilecoords[1], performer.isOnSurface());
                     if (t != null) {
                        if (t.getNumberOfItems(t.getDropFloorLevel(performer.getFloorLevel())) > 99) {
                           performer.getCommunicator().sendNormalServerMessage("That place is too littered with items already.", (byte)3);
                           return fail;
                        }

                        if (target.isDecoration() && t.getStructure() != null && t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14
                           )
                         {
                           performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                           return fail;
                        }

                        if (target.isOutsideOnly() && t.getStructure() != null) {
                           performer.getCommunicator().sendNormalServerMessage("You cannot drop that inside.", (byte)3);
                           return fail;
                        }
                     }
                  }

                  if (performer.getCurrentTile().getNumberOfItems(performer.getFloorLevel()) > 120) {
                     performer.getCommunicator().sendNormalServerMessage("This area is too littered with items already.", (byte)3);
                     return fail;
                  }

                  target.putItemInfrontof(performer);
                  performer.checkTheftWarnQuestion();
                  if (target.isTent()) {
                     performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
                  }

                  return new String[]{"You drop ", target.getNameWithGenus(), ".", "."};
               }

               String tName = target.getName();
               Items.destroyItem(target.getWurmId());
               performer.getCommunicator().sendNormalServerMessage("You pour the " + tName + " on the ground.");
               Server.getInstance().broadCastAction(performer.getName() + " pours some " + tName + " on the ground.", performer, 5);
            }
         } catch (NoSuchItemException var27) {
            logger.log(Level.WARNING, var27.getMessage(), (Throwable)var27);
         } catch (NoSuchCreatureException var28) {
            logger.log(Level.WARNING, "Failed to locate creature " + performer.getWurmId(), (Throwable)var28);
         } catch (NoSuchPlayerException var29) {
            logger.log(Level.WARNING, "Failed to locate player " + performer.getWurmId(), (Throwable)var29);
         } catch (NoSuchZoneException var30) {
            Items.destroyItem(target.getWurmId());
         }
      }

      return fail;
   }

   static boolean startFire(Creature performer, Item source, Item target, float counter) {
      boolean toReturn = false;
      int time = 200;
      Skills skills = performer.getSkills();
      Skill primSkill = null;
      Action act = null;

      try {
         act = performer.getCurrentAction();
      } catch (NoSuchActionException var13) {
         logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), (Throwable)var13);
         return true;
      }

      try {
         primSkill = skills.getSkill(1010);
      } catch (Exception var12) {
         primSkill = skills.learn(1010, 1.0F);
      }

      if (target.isOnFire()) {
         performer.getCommunicator().sendNormalServerMessage("The fire is already burning.", (byte)3);
         return true;
      } else if (target.getTemplate().isTransportable() && target.getTopParent() != target.getWurmId()) {
         String message = StringUtil.format("The %s must be on the ground before you can light it.", target.getName());
         performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
         return true;
      } else {
         if (counter == 1.0F) {
            if (target.getTemplateId() == 74 && target.getData2() > 0) {
               performer.getCommunicator().sendNormalServerMessage("The dale is already burning.", (byte)3);
            }

            if (target.getTemplateId() != 1243) {
               Item kindling = performer.getCarriedItem(36);
               if (kindling == null) {
                  performer.getCommunicator().sendNormalServerMessage("You need at least one kindling to start a fire.", (byte)3);
                  return true;
               }

               int templateWeight = kindling.getTemplate().getWeightGrams();
               int currentWeight = kindling.getWeightGrams();
               if (currentWeight < templateWeight) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "The kindling contains too little material to light the "
                           + target.getName()
                           + ".  Try to combine any of them with a similar object to get larger pieces."
                     );
                  return true;
               }
            } else if (target.getAuxData() == 0) {
               performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
               return true;
            }

            time = Actions.getStandardActionTime(performer, primSkill, source, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to light the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to light the " + target.getName() + ".", performer, 5);
            performer.sendActionControl("Lighting " + target.getName(), true, time);
            performer.getStatus().modifyStamina(-1000.0F);
         } else {
            time = act.getTimeLeft();
         }

         if (act.mayPlaySound()) {
            SoundPlayer.playSound("sound.fire.lighting.flintsteel", performer, 1.0F);
         }

         if (act.currentSecond() == 5) {
            performer.getStatus().modifyStamina(-1000.0F);
         }

         if (counter * 10.0F > (float)time) {
            if (target.getTemplateId() != 1243) {
               Item kindling = performer.getCarriedItem(36);
               if (kindling == null) {
                  performer.getCommunicator().sendNormalServerMessage("You need at least one kindling to start a fire.", (byte)3);
                  return true;
               }

               int templateWeight = kindling.getTemplate().getWeightGrams();
               int currentWeight = kindling.getWeightGrams();
               if (currentWeight < templateWeight) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "The kindling contains too little material to light the "
                           + target.getName()
                           + ".  Try to combine it with other kindling to get larger pieces."
                     );
                  return true;
               }

               kindling.setWeight(currentWeight - templateWeight, true);
            } else if (target.getAuxData() == 0) {
               performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
               return true;
            }

            primSkill.skillCheck((double)(Server.getWeather().getRain() * 10.0F), (double)source.getCurrentQualityLevel(), false, counter);
            if (source != null && source.isBurnable() && source.getTemperature() > 1000 && !source.isIndestructible()) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You throw the burning remnants of " + source.getNameWithGenus() + " into " + target.getNameWithGenus() + ".");
               Server.getInstance()
                  .broadCastAction(
                     performer.getName() + " throws the burning remnants of " + source.getNameWithGenus() + " into " + target.getNameWithGenus() + ".",
                     performer,
                     5
                  );
               Items.destroyItem(source.getWurmId());
            }

            toReturn = setFire(performer, target);
         }

         return toReturn;
      }
   }

   static boolean setFire(Creature performer, Item target) {
      if (target.getTemplate().isTransportable() && target.getTopParent() != target.getWurmId()) {
         String message = StringUtil.format("The %s must be on the ground before you can light it.", target.getName());
         performer.getCommunicator().sendNormalServerMessage(message, (byte)3);
         return true;
      } else if (target.getTemplateId() == 1243 && target.getAuxData() == 0) {
         performer.getCommunicator().sendNormalServerMessage("You need to add fuel the bee smoker first.");
         return true;
      } else {
         performer.getCommunicator().sendNormalServerMessage("You light the " + target.getName() + ".");
         if (target.getTemplateId() == 178) {
            target.setTemperature((short)6000);
         } else {
            target.setTemperature((short)10000);
         }

         if (target.getTemplateId() == 889) {
            target.setAuxData((byte)Math.min(255, target.getAuxData() + 2));
         }

         if (target.getTemplateId() != 1243) {
            Effect effect = EffectFactory.getInstance()
               .createFire(target.getWurmId(), target.getPosX(), target.getPosY(), target.getPosZ(), performer.isOnSurface());
            target.addEffect(effect);
         }

         return true;
      }
   }

   static boolean gatherRiftResource(Creature performer, Item sourceTool, Item targetItem, float counter, Action act) {
      if (targetItem.deleted) {
         performer.getCommunicator().sendNormalServerMessage("The " + targetItem.getName() + " has nothing left to gather.", (byte)3);
         return true;
      } else {
         boolean toReturn = true;
         if (!performer.getInventory().mayCreatureInsertItem()) {
            performer.getCommunicator().sendNormalServerMessage("You have no space left in your inventory to put what you might gather.");
            return true;
         } else {
            if (performer.isWithinDistanceTo(targetItem.getPosX(), targetItem.getPosY(), targetItem.getPosZ(), 4.0F)) {
               toReturn = false;
               if (counter == 1.0F) {
                  Skills skills = performer.getSkills();
                  Skill relevantSkill = null;

                  try {
                     switch(act.getActionEntry().getNumber()) {
                        case 96:
                           relevantSkill = skills.getSkill(1007);
                           break;
                        case 145:
                           relevantSkill = skills.getSkill(1008);
                           break;
                        case 156:
                           relevantSkill = skills.getSkill(10032);
                     }
                  } catch (NoSuchSkillException var24) {
                     switch(act.getActionEntry().getNumber()) {
                        case 96:
                           relevantSkill = skills.learn(1007, 1.0F);
                           break;
                        case 145:
                           relevantSkill = skills.learn(1008, 1.0F);
                           break;
                        case 156:
                           relevantSkill = skills.learn(10032, 1.0F);
                     }
                  }

                  act.setTimeLeft(Actions.getSlowActionTime(performer, relevantSkill, sourceTool, 0.0) * 1);
                  performer.getCommunicator().sendNormalServerMessage("You start to gather resources from " + targetItem.getNameWithGenus() + ".");
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " starts to gather resources from " + targetItem.getNameWithGenus() + ".", performer, 5);
                  performer.sendActionControl("Gathering from " + targetItem.getName(), true, act.getTimeLeft());
                  performer.getStatus().modifyStamina(-1000.0F);
               } else {
                  if (act.mayPlaySound()) {
                     String soundName = "sound.work.mining1";
                     int soundNum = Server.rand.nextInt(3);
                     switch(act.getActionEntry().getNumber()) {
                        case 96:
                           if (soundNum == 0) {
                              soundName = "sound.work.woodcutting1";
                           } else if (soundNum == 1) {
                              soundName = "sound.work.woodcutting2";
                           } else if (soundNum == 2) {
                              soundName = "sound.work.woodcutting3";
                           }
                           break;
                        case 145:
                           if (soundNum == 1) {
                              soundName = "sound.work.mining2";
                           } else if (soundNum == 2) {
                              soundName = "sound.work.mining3";
                           }
                           break;
                        case 156:
                           if (soundNum == 0) {
                              soundName = "sound.work.prospecting1";
                           } else if (soundNum == 1) {
                              soundName = "sound.work.prospecting2";
                           } else if (soundNum == 2) {
                              soundName = "sound.work.prospecting3";
                           }
                     }

                     SoundPlayer.playSound(soundName, performer, 1.0F);
                  }

                  if (act.currentSecond() % 5 == 0) {
                     sourceTool.setDamage(sourceTool.getDamage() + 0.0015F * sourceTool.getDamageModifier());
                     performer.getStatus().modifyStamina(-3000.0F);
                  }
               }

               if (counter * 10.0F > (float)act.getTimeLeft()) {
                  if (act.getRarity() != 0) {
                     performer.playPersonalSound("sound.fx.drumroll");
                  }

                  Skills skills = performer.getSkills();
                  Skill relevantSkill = null;
                  Skill toolSkill = null;

                  try {
                     switch(act.getActionEntry().getNumber()) {
                        case 96:
                           relevantSkill = skills.getSkill(1007);
                           break;
                        case 145:
                           relevantSkill = skills.getSkill(1008);
                           break;
                        case 156:
                           relevantSkill = skills.getSkill(10032);
                     }
                  } catch (NoSuchSkillException var23) {
                     switch(act.getActionEntry().getNumber()) {
                        case 96:
                           relevantSkill = skills.learn(1007, 1.0F);
                           break;
                        case 145:
                           relevantSkill = skills.learn(1008, 1.0F);
                           break;
                        case 156:
                           relevantSkill = skills.learn(10032, 1.0F);
                     }
                  }

                  try {
                     toolSkill = skills.getSkill(sourceTool.getPrimarySkill());
                  } catch (Exception var22) {
                     try {
                        toolSkill = skills.learn(sourceTool.getPrimarySkill(), 1.0F);
                     } catch (NoSuchSkillException var21) {
                        logger.log(
                           Level.WARNING, performer.getName() + " trying to gather resources with an item with no primary skill: " + sourceTool.getName()
                        );
                     }
                  }

                  toReturn = true;
                  float difficulty = 60.0F;
                  double bonus = toolSkill.skillCheck(60.0, sourceTool, 0.0, false, counter) / 5.0;
                  double power = relevantSkill.skillCheck(60.0, sourceTool, bonus, false, counter);
                  float maxQL = targetItem.getCurrentQualityLevel();
                  if (targetItem.getTemplate().isRiftStoneDeco() || targetItem.getTemplate().isRiftCrystalDeco()) {
                     try {
                        if (power > 0.0) {
                           float resPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
                           Item riftResource = ItemFactory.createItem(
                              targetItem.getTemplate().isRiftCrystalDeco() ? 1103 : 1102, Math.min(maxQL, resPower), act.getRarity(), performer.getName()
                           );
                           performer.getInventory().insertItem(riftResource);
                           performer.getCommunicator().sendNormalServerMessage("You get " + riftResource.getNameWithGenus() + ".", (byte)2);
                           Server.getInstance().broadCastAction(performer.getName() + " finds " + riftResource.getNameWithGenus() + ".", performer, 5);
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You find it too difficult to get useful stone from the " + targetItem.getName() + ".", (byte)2);
                        }

                        float stonePower = GeneralUtilities.calcOreRareQuality(power, act.getRarity(), sourceTool.getRarity());
                        Item stoneShards = ItemFactory.createItem(
                           146,
                           Math.min(maxQL, stonePower),
                           performer.getPosX(),
                           performer.getPosY(),
                           Server.rand.nextFloat() * 360.0F,
                           performer.isOnSurface(),
                           act.getRarity(),
                           -10L,
                           null
                        );
                        if (targetItem.getWeightGrams() < stoneShards.getWeightGrams()) {
                           stoneShards.setWeight(targetItem.getWeightGrams(), true);
                        }

                        targetItem.setWeight(targetItem.getWeightGrams() - stoneShards.getWeightGrams(), true);
                        performer.getCommunicator()
                           .sendNormalServerMessage("You finish mining " + stoneShards.getName() + " from the " + targetItem.getName() + ".");
                        Server.getInstance()
                           .broadCastAction(performer.getName() + " mines " + stoneShards.getName() + " from the " + targetItem.getName() + ".", performer, 5);
                     } catch (Exception var20) {
                        logger.log(Level.WARNING, var20.getMessage());
                        performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
                     }
                  } else if (targetItem.getTemplate().isRiftPlantDeco()) {
                     try {
                        if (power > 0.0) {
                           float resPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
                           Item riftWood = ItemFactory.createItem(1104, Math.min(maxQL, resPower), act.getRarity(), performer.getName());
                           if (targetItem.getTemplateId() == 1041) {
                              riftWood.setMaterial((byte)38);
                           } else if (targetItem.getTemplateId() == 1042) {
                              riftWood.setMaterial((byte)40);
                           } else if (targetItem.getTemplateId() == 1043) {
                              riftWood.setMaterial((byte)41);
                           } else if (targetItem.getTemplateId() == 1044) {
                              riftWood.setMaterial((byte)51);
                           }

                           performer.getInventory().insertItem(riftWood);
                           performer.getCommunicator().sendNormalServerMessage("You get " + riftWood.getNameWithGenus() + ".", (byte)2);
                           Server.getInstance().broadCastAction(performer.getName() + " finds " + riftWood.getNameWithGenus() + ".", performer, 5);
                        } else {
                           performer.getCommunicator()
                              .sendNormalServerMessage("You find it too difficult to get useful wood from the " + targetItem.getName() + ".", (byte)2);
                        }

                        byte material = 0;
                        if (targetItem.getTemplateId() == 1041) {
                           material = 38;
                        } else if (targetItem.getTemplateId() == 1042) {
                           material = 40;
                        } else if (targetItem.getTemplateId() == 1043) {
                           material = 41;
                        } else if (targetItem.getTemplateId() == 1044) {
                           material = 51;
                        }

                        float woodPower = GeneralUtilities.calcRareQuality(power, act.getRarity(), sourceTool.getRarity());
                        Item woodLog = ItemFactory.createItem(
                           9,
                           Math.min(maxQL, woodPower),
                           performer.getPosX(),
                           performer.getPosY(),
                           Server.rand.nextFloat() * 360.0F,
                           performer.isOnSurface(),
                           material,
                           act.getRarity(),
                           -10L,
                           null
                        );
                        woodLog.setLastOwnerId(performer.getWurmId());
                        if (targetItem.getTemplateId() == 1044) {
                           if (targetItem.getWeightGrams() < 24000) {
                              float ratio = (float)targetItem.getWeightGrams() / 24000.0F;
                              woodLog.setWeight((int)(4000.0F * ratio), true);
                              targetItem.setWeight(0, true);
                           } else {
                              woodLog.setWeight(4000, true);
                              targetItem.setWeight(targetItem.getWeightGrams() - 24000, true);
                           }
                        } else {
                           if (targetItem.getWeightGrams() < woodLog.getWeightGrams()) {
                              woodLog.setWeight(targetItem.getWeightGrams(), true);
                           }

                           targetItem.setWeight(targetItem.getWeightGrams() - woodLog.getWeightGrams(), true);
                        }

                        performer.getCommunicator()
                           .sendNormalServerMessage("You finish chopping " + woodLog.getNameWithGenus() + " from the " + targetItem.getName() + ".");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " chops " + woodLog.getNameWithGenus() + " from the " + targetItem.getName() + ".", performer, 5
                           );
                     } catch (Exception var19) {
                        logger.log(Level.WARNING, var19.getMessage());
                        performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
                     }
                  }
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You can't reach the " + targetItem.getName() + ".", (byte)3);
            }

            return toReturn;
         }
      }
   }

   static boolean mine(Creature performer, Item pickAxe, Item boulder, float counter, Action act) {
      if (boulder.deleted) {
         performer.getCommunicator().sendNormalServerMessage("The " + boulder.getName() + " contains no more ore.", (byte)3);
         return true;
      } else {
         boolean toReturn = true;
         if ((long)boulder.getZoneId() != -10L) {
            if (performer.isWithinDistanceTo(boulder.getPosX(), boulder.getPosY(), boulder.getPosZ(), 4.0F)) {
               toReturn = false;
               int time = 150;
               if (counter == 1.0F) {
                  Skills skills = performer.getSkills();
                  Skill mining = null;

                  try {
                     mining = skills.getSkill(1008);
                  } catch (NoSuchSkillException var23) {
                     mining = skills.learn(1008, 1.0F);
                  }

                  time = Actions.getStandardActionTime(performer, mining, pickAxe, 0.0);
                  act.setTimeLeft(time);
                  performer.getCommunicator().sendNormalServerMessage("You start to mine the " + boulder.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to mine " + boulder.getNameWithGenus() + ".", performer, 5);
                  performer.sendActionControl("Mining " + boulder.getName(), true, time);
                  performer.getStatus().modifyStamina(-400.0F);
               } else {
                  time = act.getTimeLeft();
                  if (act.mayPlaySound()) {
                     String sstring = "sound.work.mining1";
                     int x = Server.rand.nextInt(3);
                     if (x == 0) {
                        sstring = "sound.work.mining2";
                     } else if (x == 1) {
                        sstring = "sound.work.mining3";
                     }

                     SoundPlayer.playSound(sstring, performer, 1.0F);
                  }

                  if (act.currentSecond() % 5 == 0) {
                     pickAxe.setDamage(pickAxe.getDamage() + 0.0015F * pickAxe.getDamageModifier());
                     performer.getStatus().modifyStamina(-7000.0F);
                  }
               }

               if (counter * 10.0F > (float)time) {
                  Skills skills = performer.getSkills();
                  Skill mining = null;

                  try {
                     mining = skills.getSkill(1008);
                  } catch (NoSuchSkillException var22) {
                     mining = skills.learn(1008, 1.0F);
                  }

                  Skill tool = null;

                  try {
                     tool = skills.getSkill(pickAxe.getPrimarySkill());
                  } catch (Exception var21) {
                     try {
                        tool = skills.learn(pickAxe.getPrimarySkill(), 1.0F);
                     } catch (NoSuchSkillException var20) {
                        logger.log(Level.WARNING, performer.getName() + " trying to mine with an item with no primary skill: " + pickAxe.getName());
                     }
                  }

                  toReturn = true;
                  int itemTemplateCreated = 146;
                  float diff = 1.0F;
                  double bonus = tool.skillCheck(1.0, pickAxe, 0.0, false, counter) / 5.0;
                  double power = Math.max(1.0, mining.skillCheck(1.0, pickAxe, bonus, false, counter));
                  if (mining.getKnowledge(0.0) < power) {
                     power = mining.getKnowledge(0.0);
                  }

                  if (power > 0.0) {
                     int m = 100;
                     power = Math.min(power, 100.0);
                     if (pickAxe.isCrude()) {
                        power = 1.0;
                     }

                     if (boulder.getTemplateId() == 692) {
                        itemTemplateCreated = 693;
                     } else if (boulder.getTemplateId() == 696) {
                        itemTemplateCreated = 697;
                     }
                  } else {
                     power = (double)Math.max(1.0F, (float)Server.rand.nextInt(10));
                  }

                  try {
                     float modifier = 1.0F;
                     if (pickAxe.getSpellEffects() != null) {
                        modifier = pickAxe.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RESGATHERED);
                     }

                     float orePower = GeneralUtilities.calcOreRareQuality(power * (double)modifier, act.getRarity(), pickAxe.getRarity());
                     Item newItem = ItemFactory.createItem(
                        itemTemplateCreated,
                        orePower,
                        performer.getPosX(),
                        performer.getPosY(),
                        Server.rand.nextFloat() * 360.0F,
                        performer.isOnSurface(),
                        act.getRarity(),
                        -10L,
                        null
                     );
                     newItem.setLastOwnerId(performer.getWurmId());
                     newItem.setWeight((int)((float)newItem.getWeightGrams() * 0.25F), true);
                     Items.destroyItem(boulder.getWurmId());
                     performer.getCommunicator().sendNormalServerMessage("You mine some " + newItem.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " mines some " + newItem.getName() + ".", performer, 5);
                  } catch (Exception var19) {
                     logger.log(Level.WARNING, var19.getMessage());
                     performer.getCommunicator().sendNormalServerMessage("You stumble at the last second and nothing happens.");
                  }
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You can't reach the " + boulder.getName() + " now.", (byte)3);
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You can't reach the " + boulder.getName() + " now.", (byte)3);
         }

         return toReturn;
      }
   }

   static boolean plantSign(
      Creature performer, Item sign, float counter, boolean inCorner, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet, long data
   ) {
      if (cannotPlant(performer, sign)) {
         return true;
      } else {
         String planted = sign.getParentId() == -10L ? "secured" : "planted";
         String plant = sign.getParentId() == -10L ? "secure" : "plant";
         if (counter == 1.0F && sign.isTrellis() && !performer.isOnSurface()) {
            performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be harvested if " + planted + " on the surface.");
         }

         if ((sign.isEnchantedTurret() || sign.isUnenchantedTurret()) && performer.getLayer() < 0) {
            performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can not be " + planted + " beneath ground.", (byte)3);
            return true;
         } else if (inCorner && !performer.isWithinDistanceTo((float)(cornerX << 2), (float)(cornerY << 2), 0.0F, 4.0F)) {
            performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.", (byte)3);
            return true;
         } else if (counter == 1.0F && sign.isAbility() && !surveyAbilitySigns(sign, performer)) {
            performer.getCommunicator().sendNormalServerMessage("You cannot activate this item here.", (byte)3);
            return true;
         } else {
            boolean toReturn = false;
            int time = Actions.getPlantActionTime(performer, sign);
            Action act = null;

            try {
               act = performer.getCurrentAction();
            } catch (NoSuchActionException var22) {
               logger.log(Level.WARNING, "This action doesn't exist? " + performer.getName(), (Throwable)var22);
               return true;
            }

            if (counter == 1.0F) {
               if (performer instanceof Player) {
                  Player p = (Player)performer;
                  boolean ownVillage = false;
                  if (performer.getCitizenVillage() != null && performer.getCurrentVillage() == performer.getCitizenVillage()) {
                     ownVillage = true;
                  }

                  if (sign.isPlantOneAWeek() && p.hasPlantedSign() && !ownVillage) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("You may only plant one " + sign.getName() + " outside your settlement per week.", (byte)3);
                     return true;
                  }

                  try {
                     Skills skills = p.getSkills();
                     Skill dig = skills.getSkill(1009);
                     if (dig.getRealKnowledge() < 10.0) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(
                              "You need to have 10 in the skill digging to secure " + sign.getTemplate().getPlural() + " to the ground.", (byte)3
                           );
                        return true;
                     }
                  } catch (NoSuchSkillException var21) {
                     performer.getCommunicator().sendNormalServerMessage("You need 10 digging to plant " + sign.getTemplate().getPlural() + ".", (byte)3);
                     return true;
                  }
               }

               if (!Methods.isActionAllowed(performer, (short)176)) {
                  return true;
               }

               if (sign.isSign() && sign.getDescription().length() == 0) {
                  performer.getCommunicator().sendNormalServerMessage("Write something on the " + sign.getName() + " first.", (byte)3);
                  return true;
               }

               int tile = performer.getCurrentTileNum();
               if (sign.getTemplateId() == 1342) {
                  if (performer.getStatus().getBridgeId() != -10L) {
                     performer.getCommunicator().sendNormalServerMessage(sign.getName() + " must be planted in water, not on a bridge.", (byte)3);
                     return true;
                  }

                  int depth = FishEnums.getWaterDepth(performer.getPosX(), performer.getPosY(), performer.isOnSurface());
                  if (depth < 2) {
                     performer.getCommunicator().sendNormalServerMessage("The water is not deep enough for the " + sign.getName() + ".");
                     return true;
                  }

                  if (depth > 30) {
                     performer.getCommunicator().sendNormalServerMessage("The water is too deep for the " + sign.getName() + ".");
                     return true;
                  }
               } else if (sign.getTemplateId() != 805 && sign.getTemplateId() != 1396 && performer.getStatus().getBridgeId() == -10L) {
                  if (sign.isRoadMarker() && Tiles.decodeHeight(tile) < -7 || !sign.isRoadMarker() && Tiles.decodeHeight(tile) < 0) {
                     performer.getCommunicator().sendNormalServerMessage("The water is too deep to plant the " + sign.getName() + ".", (byte)3);
                     return true;
                  }
               } else if (Tiles.decodeHeight(tile) < 0
                  && sign.getTemplateId() != 805
                  && sign.getTemplateId() != 1396
                  && !sign.isRoadMarker()
                  && performer.getStatus().getBridgeId() == -10L) {
                  performer.getCommunicator().sendNormalServerMessage("The water is too deep to plant the " + sign.getName() + ".", (byte)3);
                  return true;
               }

               act.setTimeLeft(time);
               performer.getCommunicator().sendNormalServerMessage("You start to " + plant + " the " + sign.getName() + ".");
               if (sign.isAbility()) {
                  performer.getCommunicator()
                     .sendAlertServerMessage("WARNING: This can NOT be aborted once the " + sign.getName() + " finishes planting!", (byte)2);
               }

               if (sign.isSign()) {
                  Server.getInstance()
                     .broadCastAction(performer.getName() + " starts to " + plant + " a sign which says: " + sign.getDescription() + ".", performer, 5);
               } else {
                  Server.getInstance().broadCastAction(performer.getName() + " starts to " + plant + " " + sign.getNameWithGenus() + ".", performer, 5);
               }

               performer.sendActionControl("Planting " + sign.getName(), true, time);
               performer.getStatus().modifyStamina(-400.0F);
            } else {
               time = act.getTimeLeft();
               if (act.currentSecond() % 5 == 0) {
                  performer.getStatus().modifyStamina(-1000.0F);
               }
            }

            if (counter * 10.0F > (float)time) {
               toReturn = plantSignFinish(performer, sign, inCorner, cornerX, cornerY, onSurface, bridgeId, atFeet, data);
            }

            return toReturn;
         }
      }
   }

   static boolean plantSignFinish(
      Creature performer, Item sign, boolean inCorner, int cornerX, int cornerY, boolean onSurface, long bridgeId, boolean atFeet, long data
   ) {
      if (cannotPlant(performer, sign)) {
         return true;
      } else {
         if (sign.getTemplateId() == 1342) {
            atFeet = true;
         }

         String plant = sign.getParentId() != -10L ? "plant" : "secure";

         try {
            if (data != -1L) {
               sign.setData(data);
            }

            sign.setIsPlanted(true);
            if (sign.getParentId() != -10L) {
               if (!inCorner && !atFeet) {
                  sign.putItemInfrontof(performer);
               } else {
                  sign.putItemInCorner(performer, cornerX, cornerY, onSurface, bridgeId, atFeet);
               }

               if (sign.isEnchantedTurret() || sign.isUnenchantedTurret()) {
                  int dist = 8;

                  for(int x = Zones.safeTileX(sign.getTileX() - 8); x < Zones.safeTileX(sign.getTileX() + 8); ++x) {
                     for(int y = Zones.safeTileY(sign.getTileY() - 8); y < Zones.safeTileY(sign.getTileY() + 8); ++y) {
                        VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
                        if (t != null && t.hasOnePerTileItem(0)) {
                           Item i = t.getOnePerTileItem(0);
                           if (sign != i && (i.isEnchantedTurret() || i.isUnenchantedTurret()) && i.isPlanted()) {
                              performer.getCommunicator().sendNormalServerMessage("The " + i.getName() + " is too close to " + sign.getName() + ".", (byte)3);
                              sign.setIsPlanted(false);
                              return true;
                           }
                        }
                     }
                  }
               }
            }

            performer.getCommunicator().sendNormalServerMessage("You " + plant + " the " + sign.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " " + plant + "s the " + sign.getName() + ".", performer, 5);
            boolean ownVillage = false;
            if (performer.getCitizenVillage() != null && performer.getCurrentVillage() == performer.getCitizenVillage()) {
               ownVillage = true;
            }

            if (sign.isPlantOneAWeek() && !ownVillage && performer instanceof Player) {
               ((Player)performer).plantSign();
            }

            if (sign.isAbility()) {
               int signTileX = sign.getTileX();
               int signTileY = sign.getTileY();
               if (surveyAbilitySigns(sign, performer)) {
                  sign.setIsNoTake(true);
                  sign.hatching = true;
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("Nothing seems to happen. Maybe you're too close to a settlement or in enemy territory.");
               }
            }
         } catch (NoSuchZoneException var17) {
            performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + var17.getMessage(), (Throwable)var17);
         } catch (NoSuchCreatureException var18) {
            performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + var18.getMessage(), (Throwable)var18);
         } catch (NoSuchPlayerException var19) {
            performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + var19.getMessage(), (Throwable)var19);
         } catch (NoSuchItemException var20) {
            performer.getCommunicator().sendNormalServerMessage("You fail to " + plant + " the " + sign.getName() + ". Something is weird.");
            logger.log(Level.WARNING, performer.getName() + ": " + var20.getMessage(), (Throwable)var20);
         }

         return true;
      }
   }

   static boolean surveyAbilitySigns(Item sign, Creature performer) {
      if (performer != null && sign != null && sign.getTemplate() != null && sign.isAbility()) {
         boolean ok = false;
         int signTileX = sign.getTileX();
         int signTileY = sign.getTileY();
         if (sign.getTemplateId() == 1009) {
            if (signTileX > 200 && signTileX < Zones.worldTileSizeX - 200 && signTileY > 200 && signTileY < Zones.worldTileSizeY - 200) {
               ok = true;

               for(int x = signTileX - 75; x < signTileX + 75; ++x) {
                  for(int y = signTileY - 75; y < signTileY + 75; ++y) {
                     byte kd = Zones.getKingdom(x, y);
                     if (kd != 0 && kd != performer.getKingdomId()) {
                        ok = false;
                        break;
                     }

                     if (Villages.getVillageWithPerimeterAt(x, y, true) != null) {
                        ok = false;
                        break;
                     }

                     VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
                     if (t != null && t.getStructure() != null) {
                        ok = false;
                     }
                  }
               }
            }
         } else if (sign.getTemplateId() == 805
            && signTileX > 200
            && signTileX < Zones.worldTileSizeX - 200
            && signTileY > 200
            && signTileY < Zones.worldTileSizeY - 200) {
            ok = true;

            for(int x = signTileX - 30; x < signTileX + 30; ++x) {
               for(int y = signTileY - 30; y < signTileY + 30; ++y) {
                  byte kd = Zones.getKingdom(x, y);
                  if (kd != 0 && kd != performer.getKingdomId()) {
                     ok = false;
                     break;
                  }

                  if (Villages.getVillageWithPerimeterAt(x, y, true) != null) {
                     ok = false;
                     break;
                  }

                  VolaTile t = Zones.getTileOrNull(x, y, sign.isOnSurface());
                  if (t != null && t.getStructure() != null) {
                     ok = false;
                  }

                  int tilenum = Zones.getTileIntForTile(x, y, 0);
                  if (Tiles.decodeHeight(tilenum) >= 0) {
                     performer.getCommunicator().sendNormalServerMessage("You are too close to land.", (byte)3);
                     ok = false;
                     break;
                  }
               }

               if (!ok) {
                  break;
               }
            }
         }

         return ok;
      } else {
         return false;
      }
   }

   static boolean cannotPlant(Creature performer, Item sign) {
      String planted = sign.getParentId() == -10L ? "secured" : "planted";
      String plant = sign.getParentId() == -10L ? "secure" : "plant";
      if (sign.getCurrentQualityLevel() < 10.0F) {
         performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is of too poor quality to be " + planted + ".");
         return true;
      } else if (sign.getDamage() > 70.0F) {
         performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is too heavily damaged to be " + planted + ".");
         return true;
      } else if (sign.isPlanted()) {
         performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " is already " + planted + ".", (byte)3);
         return true;
      } else if (sign.isSurfaceOnly() && !performer.isOnSurface()) {
         performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be " + planted + " on the surface.");
         return true;
      } else {
         if (sign.isOutsideOnly()) {
            VolaTile vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
            if (vt != null && vt.getStructure() != null) {
               performer.getCommunicator().sendNormalServerMessage("The " + sign.getName() + " can only be " + planted + " outside.");
               return true;
            }
         }

         if (sign.isFourPerTile()) {
            VolaTile vt = Zones.getTileOrNull(performer.getTileX(), performer.getTileY(), performer.isOnSurface());
            if (vt != null && vt.getFourPerTileCount(0) >= 4) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You cannot " + plant + " " + sign.getNameWithGenus() + " as there are four here already.", (byte)3);
               return true;
            }
         }

         if (!sign.canBeDropped(true)) {
            if (sign.isHollow()) {
               performer.getCommunicator()
                  .sendSafeServerMessage("You are not allowed to " + plant + " that. Make sure it doesn't contain non-dropable items.");
            } else {
               performer.getCommunicator().sendSafeServerMessage("You are not allowed to " + plant + " that.", (byte)3);
            }

            return true;
         } else {
            Item topParent = sign.getTopParentOrNull();
            if (topParent != sign && !topParent.isInventory()) {
               performer.getCommunicator().sendSafeServerMessage("You can only secure an item that is on the ground.", (byte)3);
               return true;
            } else {
               if (sign.getParentId() == -10L) {
                  if (sign.canHavePermissions() && !sign.canManage(performer)) {
                     performer.getCommunicator().sendSafeServerMessage("You do not have permission to " + plant + " that.", (byte)3);
                     return true;
                  }

                  if (sign.getLastOwnerId() != performer.getWurmId() && performer.getPower() <= 1) {
                     performer.getCommunicator().sendSafeServerMessage("You do not have permission to " + plant + " that.", (byte)3);
                     return true;
                  }
               } else if (sign.isOnePerTile() && !mayDropOnTile(performer)) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You cannot " + plant + " that item here, since there is not enough space in front of you.", (byte)3);
                  return true;
               }

               if (sign.getTemplateId() == 1342) {
                  int depth = FishEnums.getWaterDepth(sign.getPosX(), sign.getPosY(), sign.isOnSurface());
                  if (depth < 2) {
                     performer.getCommunicator().sendNormalServerMessage("The water is not deep enough for the " + sign.getName() + ".");
                     return true;
                  }

                  if (depth > 30) {
                     performer.getCommunicator().sendNormalServerMessage("The water is too deep for the " + sign.getName() + ".");
                     return true;
                  }
               }

               if (sign.getTemplateId() == 1396) {
                  int depth = FishEnums.getWaterDepth(sign.getPosX(), sign.getPosY(), sign.isOnSurface());
                  if (depth < 0) {
                     performer.getCommunicator().sendNormalServerMessage("You may not plant that out of water.");
                     return true;
                  }
               }

               return false;
            }
         }
      }
   }

   static boolean moveItem(Creature performer, Item item, float counter, short action, Action act) {
      boolean toReturn = false;
      int time = 150;
      if (item.getParentId() == -10L
         || item.getParentOrNull() == item.getTopParentOrNull()
            && item.getParentOrNull().getTemplate().hasViewableSubItems()
            && (!item.getParentOrNull().getTemplate().isContainerWithSubItems() || item.isPlacedOnParent())) {
         if (performer.isGuest()) {
            performer.getCommunicator().sendNormalServerMessage("Sorry, but we cannot allow our guests to move items.", (byte)3);
            return true;
         } else if ((action == 177 || action == 178) && !item.isTurnable(performer) && !item.isGuardTower()) {
            performer.getCommunicator().sendNormalServerMessage("Sorry, but you are not allowed to turn that.", (byte)3);
            return true;
         } else if ((action == 181 || action == 99 || action == 697 || action == 696) && checkIfStealing(item, performer, act)) {
            if (!performer.maySteal()) {
               performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.", (byte)3);
               return true;
            } else {
               performer.getCommunicator().sendNormalServerMessage("You have to steal the " + item.getName() + " instead.", (byte)3);
               return true;
            }
         } else if (item.isCorpse() && item.getWasBrandedTo() != -10L && !item.mayCommand(performer)) {
            performer.getCommunicator().sendNormalServerMessage("You may not move the corpse as you do not have permissions.", (byte)3);
            return true;
         } else {
            if (item.isCorpse() && Servers.localServer.isChallengeOrEpicServer()) {
               VolaTile ttile = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface());
               int distance = ttile != null && ttile.getVillage() != null ? 5 : 10;
               if (isEnemiesNearby(performer, distance, true)) {
                  performer.getCommunicator().sendNormalServerMessage("You may not move the corpse when there are enemies nearby.", (byte)3);
                  return true;
               }
            }

            if (item.getTemplateId() == 931 && Servers.localServer.PVPSERVER) {
               if (action == 181 || action == 697) {
                  performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " can only be pushed.", (byte)3);
                  return true;
               }

               if (performer.getTileX() != item.getTileX() || performer.getTileY() != item.getTileY()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You need to stand right behind the " + item.getName() + " in order to move it.", (byte)3);
                  return true;
               }
            }

            if (Items.isItemDragged(item)) {
               performer.getCommunicator()
                  .sendNormalServerMessage("The " + item.getName() + " is being dragged and may not be moved that way at the moment.", (byte)3);
               return true;
            } else {
               Vehicle vehicle = Vehicles.getVehicle(item);
               boolean performerIsAllowedToDriveVehicle = false;
               if (vehicle != null) {
                  for(Seat lSeat : vehicle.seats) {
                     if (lSeat.isOccupied()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The " + item.getName() + " is occupied and may not be moved that way at the moment.", (byte)3);
                        return true;
                     }
                  }

                  if (vehicle.draggers != null && vehicle.draggers.size() > 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " may not be moved that way at the moment.", (byte)3);
                     return true;
                  }

                  if (VehicleBehaviour.mayDriveVehicle(performer, item, act) && VehicleBehaviour.canBeDriverOfVehicle(performer, vehicle)) {
                     performerIsAllowedToDriveVehicle = true;
                  }
               }

               if (item.isBoat() && item.getData() != -1L) {
                  performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " won't budge. It's moored.", (byte)3);
                  return true;
               } else if ((!performerIsAllowedToDriveVehicle || !item.isVehicle()) && !Methods.isActionAllowed(performer, action, item)) {
                  return true;
               } else {
                  boolean insta = performer.getPower() > 0;
                  if (counter == 1.0F) {
                     time = Actions.getMoveActionTime(performer);
                     act.setTimeLeft(time);
                     String actString = "turn";
                     if (action == 864) {
                        actString = "move";
                     }

                     if (action == 99 || action == 696) {
                        actString = "push";
                     } else if (action == 181 || action == 697) {
                        actString = "pull";
                     }

                     performer.getCommunicator().sendNormalServerMessage("You start to " + actString + " the " + item.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " starts to " + actString + " the " + item.getName() + ".", performer, 5);
                     if (!insta) {
                        performer.sendActionControl("Moving " + item.getName(), true, time);
                        performer.getStatus().modifyStamina(-200.0F);
                     }
                  } else {
                     time = act.getTimeLeft();
                  }

                  if (counter * 10.0F > (float)time || insta) {
                     if (action == 99 || action == 181 || action == 696 || action == 697 || action == 864) {
                        SoundPlayer.playSound("sound.object.move.pushpull", item, 0.0F);
                     }

                     performer.getStatus().modifyStamina(-250.0F);
                     String actString = "turn";
                     if (action == 864) {
                        actString = "move";
                     }

                     if (action == 99 || action == 696) {
                        actString = "push";
                     } else if (action == 181 || action == 697) {
                        actString = "pull";
                     }

                     try {
                        float dir = item.getRotation();
                        if (action != 177 && action != 178) {
                           if (!item.isMoveable(performer)) {
                              performer.getCommunicator().sendNormalServerMessage("The " + item.getName() + " won't budge. It is stuck.", (byte)3);
                              return true;
                           }

                           float iposx = item.getPosX();
                           float iposy = item.getPosY();
                           int ix = item.getTileX();
                           int iy = item.getTileY();
                           long bridgeId = item.getBridgeId();
                           if (action == 181 || action == 697) {
                              double rotRads = Math.atan2(
                                 (double)(performer.getStatus().getPositionY() - iposy), (double)(performer.getStatus().getPositionX() - iposx)
                              );
                              float rot = (float)(rotRads * (180.0 / Math.PI)) + 90.0F;
                              float length = item.getTemplateId() != 938 && item.getTemplateId() != 931 ? (action == 181 ? 0.2F : 0.04F) : 4.0F;
                              float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * length;
                              float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * length;
                              float newPosX = iposx + xPosMod;
                              float newPosY = iposy + yPosMod;
                              int placedX = (int)newPosX >> 2;
                              int placedY = (int)newPosY >> 2;
                              boolean surf = item.isOnSurface();
                              if (surf && item.isSurfaceOnly() && Terraforming.isCaveEntrance(Zones.getTextureForTile(placedX, placedY, 0))) {
                                 performer.getCommunicator().sendNormalServerMessage("You cannot pull the " + item.getName() + " into a cave.", (byte)3);
                                 return true;
                              }

                              BlockingResult result = Blocking.getBlockerBetween(
                                 performer,
                                 iposx,
                                 iposy,
                                 newPosX,
                                 newPosY,
                                 performer.getPositionZ(),
                                 item.getPosZ(),
                                 performer.isOnSurface(),
                                 item.isOnSurface(),
                                 false,
                                 6,
                                 -1L,
                                 performer.getBridgeId(),
                                 Math.abs(performer.getPositionZ() - item.getPosZ()) < 2.0F ? performer.getBridgeId() : item.getBridgeId(),
                                 false
                              );
                              if (result != null) {
                                 performer.getCommunicator().sendNormalServerMessage("You cannot pull the " + item.getName() + " into the wall.", (byte)3);
                                 return true;
                              }

                              boolean changingTile = placedX != ix || placedY != iy;
                              if (changingTile) {
                                 if (item.isOnePerTile() || item.isFourPerTile()) {
                                    VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                                    if (t != null
                                       && (
                                          item.isOnePerTile() && t.hasOnePerTileItem(performer.getFloorLevel())
                                             || item.isFourPerTile() && t.getFourPerTileCount(performer.getFloorLevel()) >= 4
                                       )) {
                                       performer.getCommunicator()
                                          .sendNormalServerMessage("You cannot move that item here, since there is not enough space.", (byte)3);
                                       return true;
                                    }
                                 }

                                 if (item.getTemplateId() == 1309) {
                                    try {
                                       Item waystone = Items.getItem(item.getData());
                                       int dx = Math.abs(placedX - waystone.getTileX());
                                       int dy = Math.abs(placedY - waystone.getTileY());
                                       if (dx > 1 || dy > 1) {
                                          performer.getCommunicator()
                                             .sendNormalServerMessage(
                                                "You cannot move that item here, as it would be too far away from its associated waystone.", (byte)3
                                             );
                                          return true;
                                       }
                                    } catch (NoSuchItemException var41) {
                                       logger.log(Level.WARNING, "Associated waystone missing! " + var41.getMessage(), (Throwable)var41);
                                       performer.getCommunicator()
                                          .sendNormalServerMessage("You cannot move that item here, as its associated waystone is missing.", (byte)3);
                                       return true;
                                    }
                                 }
                              }

                              if (item.isDecoration()) {
                                 VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                                 if (t != null && t.getStructure() != null && t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14) {
                                    performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                                    return true;
                                 }
                              }

                              Zone zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
                              if (performer.getVisionArea() != null) {
                                 performer.getVisionArea().broadCastUpdateSelectBar(item.getWurmId(), true);
                              }

                              zone.removeItem(item, true, true);
                              item.setPosXY(newPosX, newPosY);
                              if (changingTile && placedX == performer.getTileX() && placedY == performer.getTileY()) {
                                 item.setOnBridge(performer.getBridgeId());
                              }

                              Zone z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, performer.isOnSurface());
                              z.addItem(item, true, false, false);
                              if (surf != item.isOnSurface()) {
                                 z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, item.isOnSurface());
                              }

                              Effect[] effects = item.getEffects();
                              if (effects != null) {
                                 for(Effect lEffect : effects) {
                                    zone.removeEffect(lEffect);
                                    lEffect.setPosX(newPosX);
                                    lEffect.setPosY(newPosY);
                                    z.addEffect(lEffect, false);
                                 }
                              }
                           }

                           if (action == 864) {
                              Zone zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
                              zone.removeItem(item, true, true);
                              item.setPosXY((float)(((int)item.getPosX() >> 2) * 4 + 2), (float)(((int)item.getPosY() >> 2) * 4 + 2));
                              zone.addItem(item, true, false, false);
                           } else if (action == 99 || action == 696) {
                              float rot = item.getTemplateId() == 931 ? item.getRotation() : performer.getStatus().getRotation();
                              float length = item.getTemplateId() != 938 && item.getTemplateId() != 931 ? (action == 99 ? 0.2F : 0.04F) : 4.0F;
                              float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * length;
                              float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * length;
                              float newPosX = iposx + xPosMod;
                              float newPosY = iposy + yPosMod;
                              int placedX = (int)newPosX >> 2;
                              int placedY = (int)newPosY >> 2;
                              boolean surf = item.isOnSurface();
                              if (surf && item.isSurfaceOnly() && Terraforming.isCaveEntrance(Zones.getTextureForTile(placedX, placedY, 0))) {
                                 performer.getCommunicator().sendNormalServerMessage("You cannot push the " + item.getName() + " into a cave.", (byte)3);
                                 return true;
                              }

                              BlockingResult result = Blocking.getBlockerBetween(
                                 performer,
                                 iposx,
                                 iposy,
                                 newPosX,
                                 newPosY,
                                 performer.getPositionZ(),
                                 item.getPosZ(),
                                 performer.isOnSurface(),
                                 item.isOnSurface(),
                                 false,
                                 6,
                                 -1L,
                                 performer.getBridgeId(),
                                 Math.abs(performer.getPositionZ() - item.getPosZ()) < 2.0F ? performer.getBridgeId() : item.getBridgeId(),
                                 false
                              );
                              if (result != null) {
                                 boolean skip = false;
                                 if (item.getTemplateId() == 931 && result.getBlockerArray().length < 2) {
                                    for(Blocker b : result.getBlockerArray()) {
                                       if (b.getTempId() == item.getWurmId()) {
                                          skip = true;
                                       }
                                    }
                                 }

                                 if (!skip) {
                                    performer.getCommunicator().sendNormalServerMessage("You cannot push the " + item.getName() + " into the wall.", (byte)3);
                                    return true;
                                 }
                              }

                              boolean changingTile = placedX != ix || placedY != iy;
                              if (changingTile) {
                                 if (item.isOnePerTile() || item.isFourPerTile()) {
                                    VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                                    if (t != null) {
                                       if (item.isOnePerTile() && t.hasOnePerTileItem(performer.getFloorLevel())
                                          || item.isFourPerTile() && t.getFourPerTileCount(performer.getFloorLevel()) >= 4) {
                                          performer.getCommunicator()
                                             .sendNormalServerMessage("You cannot move that item here, since there is not enough space.", (byte)3);
                                          return true;
                                       }

                                       if (item.isFence() && t.getCreatures().length > 0) {
                                          performer.getCommunicator()
                                             .sendNormalServerMessage("You cannot move that item here, since the creatures block you.", (byte)3);
                                          return true;
                                       }
                                    }
                                 }

                                 if (item.getTemplateId() == 1309) {
                                    try {
                                       Item waystone = Items.getItem(item.getData());
                                       int dx = Math.abs(placedX - waystone.getTileX());
                                       int dy = Math.abs(placedY - waystone.getTileY());
                                       if (dx > 1 || dy > 1) {
                                          performer.getCommunicator()
                                             .sendNormalServerMessage(
                                                "You cannot move that item there, as it would be too far away from its associated waystone.", (byte)3
                                             );
                                          return true;
                                       }
                                    } catch (NoSuchItemException var40) {
                                       logger.log(Level.WARNING, "Associated waystone missing! " + var40.getMessage(), (Throwable)var40);
                                       performer.getCommunicator()
                                          .sendNormalServerMessage("You cannot move that item there, as its associated waystone is missing.", (byte)3);
                                       return true;
                                    }
                                 }

                                 if (bridgeId == -10L) {
                                    int floorLevel = item.getFloorLevel();
                                    BridgePart bridgePart = Zones.getBridgePartFor(placedX, placedY, item.isOnSurface());
                                    if (bridgePart != null && bridgePart.hasAnExit()) {
                                       if (placedY < iy) {
                                          if (bridgePart.hasSouthExit() && bridgePart.getSouthExitFloorLevel() == floorLevel) {
                                             bridgeId = bridgePart.getStructureId();
                                          }
                                       } else if (placedX > ix) {
                                          if (bridgePart.hasWestExit() && bridgePart.getWestExitFloorLevel() == floorLevel) {
                                             bridgeId = bridgePart.getStructureId();
                                          }
                                       } else if (placedY > iy) {
                                          if (bridgePart.hasNorthExit() && bridgePart.getNorthExitFloorLevel() == floorLevel) {
                                             bridgeId = bridgePart.getStructureId();
                                          }
                                       } else if (placedX < ix && bridgePart.hasEastExit() && bridgePart.getEastExitFloorLevel() == floorLevel) {
                                          bridgeId = bridgePart.getStructureId();
                                       }
                                    }
                                 } else {
                                    BridgePart newBridgePart = Zones.getBridgePartFor(placedX, placedY, item.isOnSurface());
                                    if (newBridgePart == null) {
                                       BridgePart bridgePart = Zones.getBridgePartFor(ix, iy, item.isOnSurface());
                                       if (bridgePart == null || bridgePart.getStructureId() != bridgeId) {
                                          performer.getCommunicator().sendNormalServerMessage("Error: Item is on a bridge, but it isnt!.");
                                          return true;
                                       }

                                       int newFloorLevel = -1;
                                       if (placedY < iy) {
                                          if (bridgePart.hasNorthExit()) {
                                             newFloorLevel = bridgePart.getNorthExitFloorLevel();
                                          }
                                       } else if (placedX > ix) {
                                          if (bridgePart.hasEastExit()) {
                                             newFloorLevel = bridgePart.getEastExitFloorLevel();
                                          }
                                       } else if (placedY > iy) {
                                          if (bridgePart.hasSouthExit()) {
                                             newFloorLevel = bridgePart.getSouthExitFloorLevel();
                                          }
                                       } else if (placedX < ix && bridgePart.hasWestExit()) {
                                          newFloorLevel = bridgePart.getWestExitFloorLevel();
                                       }

                                       if (newFloorLevel == -1) {
                                          performer.getCommunicator().sendNormalServerMessage("Cannot find the floor level off the end of this bridge.");
                                          return true;
                                       }

                                       bridgeId = -10L;
                                    }
                                 }
                              }

                              if (item.isDecoration()) {
                                 VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                                 if (t != null && t.getStructure() != null && t.getNumberOfDecorations(t.getDropFloorLevel(performer.getFloorLevel())) > 14) {
                                    performer.getCommunicator().sendNormalServerMessage("That place is too littered with decorations already.", (byte)3);
                                    return true;
                                 }
                              }

                              if (item.getTemplateId() == 1311) {
                                 VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
                                 if (t != null && t.getFourPerTileCount(performer.getFloorLevel()) >= 4) {
                                    performer.getCommunicator().sendNormalServerMessage("You cannot move this item, there isn't enough room.");
                                    return true;
                                 }
                              }

                              Zone zone = Zones.getZone(item.getTileX(), item.getTileY(), item.isOnSurface());
                              if (item.getTemplateId() == 931) {
                                 VolaTile t = zone.getTileOrNull(item.getTileX(), item.getTileY());
                                 float newPosZ = Zones.calculatePosZ(newPosX, newPosY, t, performer.isOnSurface(), false, item.getPosZ(), performer, -10L);
                                 if (newPosZ < -1.0F && item.getPosZ() >= -1.0F) {
                                    performer.getCommunicator()
                                       .sendNormalServerMessage("That place is too deep. The " + item.getName() + " would get stuck.", (byte)3);
                                    return true;
                                 }

                                 t.moveItem(item, newPosX, newPosY, newPosZ, item.getRotation(), performer.isOnSurface(), item.getPosZ());
                              } else {
                                 zone.removeItem(item, true, true);
                                 item.setPosXY(newPosX, newPosY);
                                 item.setOnBridge(bridgeId);
                                 Zone z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, performer.isOnSurface());
                                 z.addItem(item, true, false, false);
                                 if (surf != item.isOnSurface()) {
                                    z = Zones.getZone((int)newPosX >> 2, (int)newPosY >> 2, item.isOnSurface());
                                 }

                                 Effect[] effects = item.getEffects();
                                 if (effects != null) {
                                    for(Effect lEffect : effects) {
                                       zone.removeEffect(lEffect);
                                       lEffect.setPosX(newPosX);
                                       lEffect.setPosY(newPosY);
                                       z.addEffect(lEffect, false);
                                    }
                                 }
                              }
                           }
                        } else {
                           float mod = item.isFence() ? 90.0F : 22.5F;
                           if (action == 177) {
                              dir += mod;
                           } else if (action == 178) {
                              dir -= mod;
                           }

                           if (dir < 0.0F) {
                              dir += 360.0F;
                           } else if (dir > 360.0F) {
                              dir -= 360.0F;
                           }

                           if (item.isFence()) {
                              VolaTile next2 = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item.isOnSurface());
                              if (next2 != null) {
                                 for(Creature c : next2.getCreatures()) {
                                    if (!c.isFriendlyKingdom(performer.getKingdomId())
                                       || performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(c)) {
                                       performer.getCommunicator().sendNormalServerMessage("There are enemies blocking your turning.", (byte)3);
                                       return true;
                                    }
                                 }
                              }

                              int offz = 0;

                              try {
                                 offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item.isOnSurface())) / 10.0F);
                              } catch (NoSuchZoneException var39) {
                                 logger.log(Level.WARNING, "Rotating fence item outside zones.");
                              }

                              float rot = Creature.normalizeAngle(item.getRotation());
                              if (rot >= 45.0F && rot < 135.0F) {
                                 VolaTile next1 = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), item.isOnSurface());
                                 if (next1 != null && next1.getCreatures().length > 0) {
                                    performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                                    return true;
                                 }

                                 VolaTile next = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), true);
                                 next.removeFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX() + 1,
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_DOWN,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else if (rot >= 135.0F && rot < 225.0F) {
                                 VolaTile next1 = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, item.isOnSurface());
                                 if (next1 != null && next1.getCreatures().length > 0) {
                                    performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                                    return true;
                                 }

                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, true);
                                 next.removeFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY() + 1,
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_HORIZ,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else if (rot >= 225.0F && rot < 315.0F) {
                                 VolaTile next1 = Zones.getOrCreateTile(item.getTileX() - 1, item.getTileY(), item.isOnSurface());
                                 if (next1 != null && next1.getCreatures().length > 0) {
                                    performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                                    return true;
                                 }

                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), true);
                                 next.removeFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_DOWN,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else {
                                 VolaTile next1 = Zones.getOrCreateTile(item.getTileX(), item.getTileY() - 1, item.isOnSurface());
                                 if (next1 != null && next1.getCreatures().length > 0) {
                                    performer.getCommunicator().sendNormalServerMessage("There are creatures blocking your turning.", (byte)3);
                                    return true;
                                 }

                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), true);
                                 next.removeFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_HORIZ,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              }
                           }

                           item.setRotation(dir);
                           if (item.isFence() && item.isOnSurface()) {
                              int offz = 0;

                              try {
                                 offz = (int)((item.getPosZ() - Zones.calculateHeight(item.getPosX(), item.getPosY(), item.isOnSurface())) / 10.0F);
                              } catch (NoSuchZoneException var38) {
                                 logger.log(Level.WARNING, "Dropping fence item outside zones.");
                              }

                              float rot = Creature.normalizeAngle(item.getRotation());
                              if (rot >= 45.0F && rot < 135.0F) {
                                 VolaTile next = Zones.getOrCreateTile(item.getTileX() + 1, item.getTileY(), item.isOnSurface());
                                 next.addFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX() + 1,
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_DOWN,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else if (rot >= 135.0F && rot < 225.0F) {
                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY() + 1, item.isOnSurface());
                                 next.addFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY() + 1,
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_HORIZ,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else if (rot >= 225.0F && rot < 315.0F) {
                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item.isOnSurface());
                                 next.addFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_DOWN,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              } else {
                                 VolaTile next = Zones.getOrCreateTile(item.getTileX(), item.getTileY(), item.isOnSurface());
                                 next.addFence(
                                    new TempFence(
                                       StructureConstantsEnum.FENCE_SIEGEWALL,
                                       item.getTileX(),
                                       item.getTileY(),
                                       offz,
                                       item,
                                       Tiles.TileBorderDirection.DIR_HORIZ,
                                       next.getZone().getId(),
                                       next.getLayer()
                                    )
                                 );
                              }
                           }

                           Zone zone = Zones.getZone((int)item.getPosX() >> 2, (int)item.getPosY() >> 2, item.isOnSurface());
                           if (performer.getVisionArea() != null) {
                              performer.getVisionArea().broadCastUpdateSelectBar(item.getWurmId(), true);
                           }

                           if (item.isGuardTower()) {
                              VolaTile tile = zone.getOrCreateTile(item.getTileX(), item.getTileY());
                              tile.makeInvisible(item);
                              tile.makeVisible(item);
                           } else {
                              Item parent = item.getParentOrNull();
                              if (parent != null
                                 && parent.getTemplate().hasViewableSubItems()
                                 && (!parent.getTemplate().isContainerWithSubItems() || item.isPlacedOnParent())) {
                                 VolaTile vt = Zones.getTileOrNull(parent.getTileX(), parent.getTileY(), parent.isOnSurface());
                                 if (vt != null) {
                                    for(VirtualZone vz : vt.getWatchers()) {
                                       if (vz.isVisible(parent, vt)) {
                                          vz.getWatcher().getCommunicator().sendItem(item, -10L, false);
                                       }
                                    }
                                 }
                              } else {
                                 zone.removeItem(item, true, true);
                                 zone.addItem(item, true, false, false);
                              }
                           }
                        }

                        performer.getCommunicator().sendNormalServerMessage("You " + actString + " the " + item.getName() + " a bit.");
                        if (action == 99 || action == 696) {
                           actString = actString + "e";
                        }

                        Server.getInstance().broadCastAction(performer.getName() + " " + actString + "s the " + item.getName() + " a bit.", performer, 5);
                     } catch (NoSuchZoneException var42) {
                        performer.getCommunicator().sendNormalServerMessage("You fail to " + actString + " the " + item.getName() + ". It must be stuck.");
                        logger.log(Level.WARNING, performer.getName() + ": " + var42.getMessage(), (Throwable)var42);
                     }

                     toReturn = true;
                  }

                  return toReturn;
               }
            }
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You can not turn that right now.", (byte)3);
         return true;
      }
   }

   static boolean lock(Creature performer, Item lock, Item target, float counter, boolean replacing) {
      if (lock.isLocked()) {
         performer.getCommunicator().sendNormalServerMessage("The " + lock.getName() + " is already in use.", (byte)3);
         return true;
      } else if (target.getLockId() == -10L && !replacing || target.getLockId() != -10L && replacing) {
         if (!target.isOwner(performer) && !Methods.isActionAllowed(performer, (short)161)) {
            return true;
         } else {
            if (target.isBoat()) {
               if (!lock.isBoatLock()) {
                  performer.getCommunicator().sendNormalServerMessage("You need to lock the " + target.getName() + " with a boat lock.", (byte)3);
                  return true;
               }

               if (performer.getWurmId() != target.getLastOwnerId()) {
                  performer.getCommunicator().sendNormalServerMessage("You do not have the right to lock this boat. Are you really the captain?", (byte)3);
                  return true;
               }
            } else if (!lock.mayLockItems()) {
               performer.getCommunicator().sendNormalServerMessage("You can't lock the " + target.getName() + " with that.", (byte)3);
               return true;
            }

            if (!performer.hasAllKeysForLock(lock)) {
               performer.getCommunicator().sendAlertServerMessage("Security Warning: You do not have all the keys for that lock!", (byte)2);
            }

            long oldLockId = target.getLockId();
            if (oldLockId != -10L) {
               try {
                  Item oldLock = Items.getItem(oldLockId);
                  oldLock.setLocked(false);
                  performer.getInventory().insertItem(oldLock);
               } catch (NoSuchItemException var9) {
                  logger.log(Level.WARNING, "Old lock was not found: " + var9.getMessage(), (Throwable)var9);
               }
            }

            lock.putInVoid();
            performer.getCommunicator().sendNormalServerMessage("You lock the " + target.getName() + " with the " + lock.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " locks the " + target.getName() + " with a " + lock.getName() + ".", performer, 5);
            target.setLockId(lock.getWurmId());
            int tilex = target.getTileX();
            int tiley = target.getTileY();
            SoundPlayer.playSound("sound.object.lockunlock", tilex, tiley, performer.isOnSurface(), 1.0F);
            lock.setLocked(true);
            PermissionsHistories.addHistoryEntry(
               target.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), replacing ? "Replaced Lock" : "Attached Lock"
            );
            return true;
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already locked.", (byte)3);
         return false;
      }
   }

   static boolean unlock(Creature performer, Item key, Item target, float counter) {
      long lockId = target.getLockId();
      if (lockId == -10L) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not locked.", (byte)3);
         return false;
      } else {
         try {
            Item lock = Items.getItem(lockId);
            long keyId = -10L;
            if (key != null) {
               keyId = key.getWurmId();
            }

            long[] keys = lock.getKeyIds();
            int tilex = target.getTileX();
            int tiley = target.getTileY();
            SoundPlayer.playSound("sound.object.lockunlock", tilex, tiley, performer.isOnSurface(), 1.0F);
            boolean hasKey = performer.hasKeyForLock(lock) || target.isOwner(performer);
            boolean foundKey = hasKey;
            if (!hasKey) {
               for(long lKey : keys) {
                  if (lKey == keyId) {
                     foundKey = true;
                     break;
                  }
               }
            }

            if (foundKey) {
               performer.getCommunicator().sendNormalServerMessage("You unlock the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " unlocks the " + target.getName() + ".", performer, 5);
               target.setLockId(-10L);
               lock.setLocked(false);
               ItemSettings.remove(target.getWurmId());
               PermissionsHistories.addHistoryEntry(target.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Removed Lock");
               performer.getInventory().insertItem(lock, true);
               return true;
            } else {
               performer.getCommunicator().sendNormalServerMessage("The key does not fit.", (byte)3);
               return false;
            }
         } catch (NoSuchItemException var19) {
            logger.log(Level.WARNING, "No such lock, but it should be locked." + var19.getMessage(), (Throwable)var19);
            return true;
         }
      }
   }

   public static final void checkLockpickBreakage(Creature performer, Item lockpick, int breakBonus, double power) {
      breakBonus += lockpick.getRarity() * 20;
      if (power > 0.0) {
         if ((float)Server.rand.nextInt(85 + breakBonus) <= (100.0F - lockpick.getCurrentQualityLevel()) / 5.0F) {
            performer.getCommunicator().sendNormalServerMessage("The lockpick breaks.", (byte)3);
            SoundPlayer.playSound("sound.object.lockpick.break.ogg", performer, 1.0F);
            Items.destroyItem(lockpick.getWurmId());
         } else {
            lockpick.setDamage(lockpick.getDamage() + (0.25F - (float)lockpick.getRarity() * 0.05F) * lockpick.getDamageModifier());
         }
      } else if ((float)Server.rand.nextInt(65 + breakBonus) <= (100.0F - lockpick.getCurrentQualityLevel()) / 5.0F) {
         performer.getCommunicator().sendNormalServerMessage("The lockpick breaks.", (byte)3);
         SoundPlayer.playSound("sound.object.lockpick.break.ogg", performer, 1.0F);
         Items.destroyItem(lockpick.getWurmId());
      } else {
         lockpick.setDamage(lockpick.getDamage() + (0.5F - (float)lockpick.getRarity() * 0.1F) * lockpick.getDamageModifier());
      }
   }

   static boolean picklock(Creature performer, Item lockpick, Item target, float counter, Action act) {
      long lockId = target.getLockId();
      boolean done = false;
      if (lockId == -10L) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not locked.", (byte)3);
         return true;
      } else if (lockpick.getTemplateId() != 463) {
         performer.getCommunicator().sendNormalServerMessage("The " + lockpick.getName() + " can not be used as a lockpick.", (byte)3);
         return true;
      } else {
         if (target.getWurmId() == 5390789413122L && target.getParentId() == 5390755858690L) {
            boolean ok = true;
            if (!performer.hasAbility(Abilities.getAbilityForItem(809, performer))) {
               ok = false;
            }

            if (!performer.hasAbility(Abilities.getAbilityForItem(808, performer))) {
               ok = false;
            }

            if (!performer.hasAbility(Abilities.getAbilityForItem(798, performer))) {
               ok = false;
            }

            if (!performer.hasAbility(Abilities.getAbilityForItem(810, performer))) {
               ok = false;
            }

            if (!performer.hasAbility(Abilities.getAbilityForItem(807, performer))) {
               ok = false;
            }

            if (!ok) {
               performer.getCommunicator().sendAlertServerMessage("There is some mysterious enchantment on this lock!");
               return true;
            }
         }

         boolean insta = performer.getPower() >= 5 || Servers.localServer.testServer && performer.getPower() > 1;
         Skill lockpicking = null;
         Skills skills = performer.getSkills();

         try {
            lockpicking = skills.getSkill(10076);
         } catch (NoSuchSkillException var24) {
            lockpicking = skills.learn(10076, 1.0F);
         }

         int time = 300;
         if (counter != 1.0F) {
            time = act.getTimeLeft();
         } else {
            for(Player p : Players.getInstance().getPlayers()) {
               if (p.getWurmId() != performer.getWurmId()) {
                  try {
                     Action pact = p.getCurrentAction();
                     if (act.getNumber() == pact.getNumber() && act.getTarget() == pact.getTarget()) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The " + target.getName() + " is already being picked by " + p.getName() + ".", (byte)3);
                        return true;
                     }
                  } catch (NoSuchActionException var23) {
                  }
               }
            }

            if (!performer.isOnPvPServer() && target.getOwnerId() == -10L) {
               boolean ok = false;
               if (target.getLastOwnerId() != -10L) {
                  Village v = Villages.getVillageForCreature(target.getLastOwnerId());
                  if (v != null) {
                     if (performer.getCitizenVillage() == v) {
                        ok = true;
                     }

                     if (v.isEnemy(performer.getCitizenVillage())) {
                        ok = true;
                     }
                  }

                  if (target.getLastOwnerId() == performer.getWurmId()) {
                     ok = true;
                  } else if (!ok) {
                     PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getLastOwnerId());
                     if (pinf != null) {
                        Friend[] friends = pinf.getFriends();

                        for(Friend f : friends) {
                           if (f.getFriendId() == performer.getWurmId() && f.getCategory() == Friend.Category.Trusted) {
                              ok = true;
                           }
                        }
                     }
                  }
               }

               if (!ok) {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to pick the lock of that in these peaceful lands.", (byte)3);
                  return true;
               }
            }

            try {
               Item lock = Items.getItem(lockId);
               if (lock.getQualityLevel() - lockpick.getQualityLevel() > 20.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You need a more advanced lock pick for this high quality lock.", (byte)3);
                  return true;
               }
            } catch (NoSuchItemException var26) {
               performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
               logger.log(Level.WARNING, "No such lock, but it should be locked." + var26.getMessage(), (Throwable)var26);
               return true;
            }

            time = Actions.getPickActionTime(performer, lockpicking, lockpick, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to pick the lock of the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to pick the lock of the " + target.getName() + ".", performer, 5);
            performer.sendActionControl("picking lock", true, time);
            performer.getStatus().modifyStamina(-2000.0F);
         }

         if (act.currentSecond() == 2) {
            checkLockpickBreakage(performer, lockpick, 100, 80.0);
         }

         if (!(counter * 10.0F > (float)time) && !insta) {
            return done;
         } else {
            performer.getStatus().modifyStamina(-2000.0F);
            boolean dryRun = false;

            try {
               Item lock = Items.getItem(lockId);
               done = true;
               if (target.getOwnerId() != performer.getWurmId() && !performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
                  performer.getCommunicator().sendNormalServerMessage("You are too far away from the " + target.getName() + " to pick the lock.", (byte)3);
               } else {
                  boolean stealing = checkIfStealing(target, performer, act);
                  double bonus = (double)(100.0F * Item.getMaterialLockpickBonus(lockpick.getMaterial()));
                  int breakBonus = (int)(bonus * 2.0);
                  bonus -= (double)(lock.getRarity() * 10);
                  bonus = Math.min(99.0, bonus);
                  if (stealing) {
                     if (Action.checkLegalMode(performer)) {
                        return true;
                     }

                     if (!performer.maySteal()) {
                        performer.getCommunicator().sendNormalServerMessage("You need more body control to pick locks.", (byte)3);
                        return true;
                     }

                     if (setTheftEffects(performer, act, target)) {
                        double power = lockpicking.skillCheck((double)lock.getCurrentQualityLevel(), lockpick, bonus, false, 10.0F);
                        checkLockpickBreakage(performer, lockpick, breakBonus, power);
                        return true;
                     }
                  }

                  double power = lockpicking.skillCheck((double)lock.getCurrentQualityLevel(), lockpick, bonus, false, 10.0F);
                  float rarityMod = 1.0F;
                  if (lock.getRarity() > 0) {
                     rarityMod += (float)lock.getRarity() * 0.2F;
                  }

                  if (target.getRarity() > 0) {
                     rarityMod += (float)target.getRarity() * 0.2F;
                  }

                  if (lockpick.getRarity() > 0) {
                     rarityMod -= (float)lockpick.getRarity() * 0.1F;
                  }

                  byte picktype = 0;
                  if (target.isVehicle() && target.getPosZ() < 1.0F && target.getSizeZ() > 5) {
                     picktype = 2;
                  }

                  float chance = getPickChance(
                        target.getCurrentQualityLevel(),
                        lockpick.getCurrentQualityLevel(),
                        lock.getCurrentQualityLevel(),
                        (float)lockpicking.getRealKnowledge(),
                        picktype
                     )
                     / rarityMod
                     * (1.0F + Item.getMaterialLockpickBonus(lockpick.getMaterial()));
                  if (Server.rand.nextFloat() * 100.0F < chance) {
                     performer.getCommunicator().sendNormalServerMessage("You pick the lock of the " + target.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " picks the lock of the " + target.getName() + ".", performer, 5);
                     target.setLockId(-10L);
                     ItemSettings.remove(target.getWurmId());
                     PermissionsHistories.addHistoryEntry(
                        target.getWurmId(), System.currentTimeMillis(), performer.getWurmId(), performer.getName(), "Lock Picked"
                     );
                     lock.setLocked(false);
                     performer.getInventory().insertItem(lock, true);
                     SoundPlayer.playSound("sound.object.lockunlock", target, 0.2F);
                     performer.achievement(111);
                     if (target.isBoat()) {
                        performer.achievement(108);
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You fail to pick the lock of the " + target.getName() + ".", (byte)3);
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName()
                              + " silently curses as "
                              + performer.getHeSheItString()
                              + " fails to pick the lock of the "
                              + target.getName()
                              + ".",
                           performer,
                           5
                        );
                  }

                  if (power > 0.0) {
                     checkLockpickBreakage(performer, lockpick, breakBonus, 100.0);
                  } else {
                     checkLockpickBreakage(performer, lockpick, breakBonus, -100.0);
                  }
               }

               return true;
            } catch (NoSuchItemException var25) {
               performer.getCommunicator().sendNormalServerMessage("There is no lock to pick.", (byte)3);
               logger.log(Level.WARNING, "No such lock, but it should be locked." + var25.getMessage(), (Throwable)var25);
               return true;
            }
         }
      }
   }

   static final float getPickModChance(float chance) {
      return (1000.0F + chance * chance) / 11000.0F;
   }

   public static final float getPickChance(float containerQl, float pickQl, float lockQl, float skill, byte pickType) {
      float chance = getPickModChance(skill);
      float baseChance = Math.max(1.0F, Math.min(95.0F, 100.0F - lockQl));
      if (skill > lockQl && pickType != 2) {
         baseChance += skill - lockQl;
      }

      chance *= baseChance;
      if (pickType == 3) {
         chance *= 2.0F;
      }

      float contMod = 1.0F + (100.0F - containerQl) / 100.0F;
      chance *= contMod;
      if (pickType == 2) {
         chance /= 3.0F;
      }

      float mod = getPickModChance(pickQl);
      chance *= mod;
      return Math.max(0.001F, Math.min(99.0F, chance));
   }

   private static Point findDropTile(int tileX, int tileY, MeshIO mesh) {
      List<Point> slopes = new ArrayList<>();
      short h = Tiles.decodeHeight(mesh.getTile(tileX, tileY));

      for(int xx = 1; xx >= -1; --xx) {
         for(int yy = 1; yy >= -1; --yy) {
            if (GeneralUtilities.isValidTileLocation(tileX + xx, tileY + yy)) {
               short th = Tiles.decodeHeight(mesh.getTile(tileX + xx, tileY + yy));
               if ((xx == 0 && yy != 0 || yy == 0 && xx != 0) && th <= h - 40) {
                  slopes.add(new Point(tileX + xx, tileY + yy));
               }

               if (xx != 0 && yy != 0 && th <= h - 56) {
                  slopes.add(new Point(tileX + xx, tileY + yy));
               }
            }
         }
      }

      if (slopes.size() > 0) {
         int r = 0;
         if (slopes.size() > 1) {
            r = Server.rand.nextInt(slopes.size());
         }

         return findDropTile(slopes.get(r).getX(), slopes.get(r).getY(), mesh);
      } else {
         return new Point(tileX, tileY);
      }
   }

   static boolean takePile(Action act, Creature performer, Item origTarget) {
      boolean toReturn = true;

      try {
         long ownId = origTarget.getOwner();
         if (ownId != -10L) {
            return true;
         }

         if (ownId == performer.getWurmId()) {
            return true;
         }
      } catch (Exception var14) {
         try {
            Zone zone = Zones.getZone((int)origTarget.getPosX() >> 2, (int)origTarget.getPosY() >> 2, origTarget.isOnSurface());
            VolaTile tile = zone.getTileOrNull((int)origTarget.getPosX() >> 2, (int)origTarget.getPosY() >> 2);
            if (tile == null) {
               logger.log(Level.WARNING, performer.getName() + " scam?:No tile found in zone.");
               return true;
            }

            if (performer.getPower() == 0) {
               Structure struct = tile.getStructure();
               VolaTile tile2 = performer.getCurrentTile();
               if (tile2 != null) {
                  if (tile2.getStructure() != struct) {
                     performer.getCommunicator().sendNormalServerMessage("You can't reach the " + origTarget.getName() + " through the wall.");
                     return true;
                  }
               } else if (struct != null) {
                  performer.getCommunicator().sendNormalServerMessage("You can't reach the " + origTarget.getName() + " through the wall.");
                  return true;
               }
            }
         } catch (NoSuchZoneException var13) {
            logger.log(Level.WARNING, performer.getName() + " scam?:" + var13.getMessage(), (Throwable)var13);
            return true;
         }

         Set<Item> items = origTarget.getItems();
         Item[] itemarr = items.toArray(new Item[items.size()]);
         List<TakeResultEnum> printed = new ArrayList<>();
         Map<String, Integer> taken = new HashMap<>();
         int largestItem = 0;

         for(int i = 0; i < itemarr.length; ++i) {
            TakeResultEnum result = take(act, performer, itemarr[i]);
            if (result.shouldPrint()) {
               if (!printed.contains(result)) {
                  result.sendToPerformer(performer);
                  printed.add(result);
               }

               if (result.abortsTakeFromPile()) {
                  break;
               }
            } else {
               if (result.abortsTakeFromPile()) {
                  break;
               }

               if (result == TakeResultEnum.SUCCESS) {
                  if (!taken.containsKey(itemarr[i].getName())) {
                     taken.put(itemarr[i].getName(), 1);
                  } else {
                     taken.put(itemarr[i].getName(), taken.get(itemarr[i].getName()) + 1);
                  }

                  int size = itemarr[i].getSizeZ() / 10;
                  if (size > largestItem) {
                     largestItem = size;
                  }
               }
            }
         }

         if (taken.size() > 0) {
            String takeString = "";

            for(String t : taken.keySet()) {
               if (takeString.isEmpty()) {
                  takeString = StringUtil.format("%d %s", taken.get(t), t);
               } else {
                  takeString = StringUtil.format("%s, %d %s", takeString, taken.get(t), t);
               }
            }

            performer.getCommunicator().sendNormalServerMessage("You get " + takeString + ".");
            Server.getInstance().broadCastAction(performer.getName() + " gets " + takeString + ".", performer, Math.max(3, largestItem));
         }
      }

      return true;
   }

   public static void fillContainer(Item source, @Nullable Creature performer, boolean isBrackish) {
      if (source.isContainerLiquid()) {
         if (source.isTraded()) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("The container is traded.");
            }
         } else {
            Item contained = null;
            Item liquid = null;

            for(Item var12 : source.getItems()) {
               if (!var12.isFood() && !var12.isLiquid() && !var12.isRecipeItem() || var12.isLiquid() && var12.getTemplateId() != 128) {
                  if (performer != null) {
                     performer.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
                  }

                  return;
               }

               if (var12.isLiquid()) {
                  liquid = var12;
               }
            }

            int volAvail = source.getFreeVolume();
            if (liquid != null && liquid.getRarity() != 0) {
               if (performer != null) {
                  performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " would lose its rarity.");
               }

               return;
            }

            if (volAvail > 0) {
               int capac = volAvail;
               if (performer != null) {
                  capac = performer.getCarryingCapacityLeft();
               }

               float fract = 1.0F;
               if (capac < volAvail) {
                  fract = (float)capac / (float)volAvail;
                  volAvail = (int)(fract * (float)volAvail);
               }

               if (volAvail >= 1) {
                  if (liquid != null) {
                     int allWeight = liquid.getWeightGrams() + volAvail;
                     float newQl = ((float)(100 * volAvail) + liquid.getCurrentQualityLevel() * (float)liquid.getWeightGrams()) / (float)allWeight;
                     liquid.setWeight(liquid.getWeightGrams() + volAvail, true);
                     liquid.setQualityLevel(newQl);
                     liquid.setDamage(0.0F);
                     if (isBrackish) {
                        liquid.setIsSalted(true);
                     }
                  } else {
                     try {
                        Item water = ItemFactory.createItem(128, 100.0F, (byte)26, (byte)0, null);
                        water.setSizes(1, 1, 1);
                        water.setWeight(volAvail, false);
                        if (isBrackish) {
                           water.setIsSalted(true);
                        }

                        if (!source.insertItem(water)) {
                           if (performer != null) {
                              performer.getCommunicator().sendNormalServerMessage("The container can't keep any of the water.");
                           }

                           Items.decay(water.getWurmId(), water.getDbStrings());
                           return;
                        }
                     } catch (NoSuchTemplateException var10) {
                        logger.log(Level.WARNING, "No template for water?!", (Throwable)var10);
                     } catch (FailedException var11) {
                        logger.log(Level.WARNING, "Creation of water failed: ", (Throwable)var11);
                     }
                  }

                  int tid = source.getTemplateId();
                  String sound = "sound.liquid.fillcontainer";
                  if (tid == 190 || tid == 189 || tid == 768) {
                     sound = "sound.liquid.fillcontainer.barrel";
                  } else if (tid == 421) {
                     sound = "sound.liquid.fillcontainer.bucket";
                  } else if (tid == 76) {
                     sound = "sound.liquid.fillcontainer.jar";
                  }

                  if (performer != null) {
                     Methods.sendSound(performer, sound);
                     performer.getCommunicator()
                        .sendNormalServerMessage("You fill the " + source.getName() + " with " + (isBrackish ? "salty " : "") + "water.");
                  }
               }
            } else if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("You wouldn't be able to carry the weight of the water.");
            }
         }
      } else if (performer != null) {
         performer.getCommunicator().sendNormalServerMessage("You cannot keep liquid in that.");
      }
   }

   public static void fillContainer(Action act, Item source, Item target, Creature performer, boolean quiet) {
      if (source.isContainerLiquid()) {
         if (!source.isTraded() && !target.isTraded()) {
            if (target.isNoTake()) {
               performer.getCommunicator().sendSafeServerMessage("The " + target.getName() + " cannot be taken.");
               return;
            }

            Item contained = null;
            Item liquid = null;
            int volAvail = source.getFreeVolume();
            if (source.getOwnerId() == performer.getWurmId()) {
               int canCarry = performer.getCarryingCapacityLeft();
               if (canCarry <= 0) {
                  performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
                  return;
               }

               if (volAvail > canCarry) {
                  volAvail = canCarry;
               }
            }

            for(Item var16 : source.getItems()) {
               if (wouldDestroyLiquid(source, var16, target)) {
                  performer.getCommunicator().sendNormalServerMessage("That would destroy the liquid.");
                  return;
               }

               if (var16.isLiquid() && var16.getTemplateId() == target.getTemplateId() && var16.getRealTemplateId() == target.getRealTemplateId()) {
                  liquid = var16;
               }
            }

            if (liquid != null && liquid.getRarity() > target.getRarity()) {
               liquid.setRarity(target.getRarity());
            }

            if (volAvail <= 0) {
               return;
            }

            if (checkIfStealing(target, performer, act)) {
               if (Action.checkLegalMode(performer)) {
                  return;
               }

               if (!performer.maySteal()) {
                  performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
                  return;
               }

               if (setTheftEffects(performer, act, target)) {
                  return;
               }
            }

            if (volAvail < target.getWeightGrams()) {
               if (!performer.canCarry(volAvail) && source.getOwnerId() == performer.getWurmId()) {
                  performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
                  return;
               }

               if (liquid == null) {
                  try {
                     Item splitItem = splitLiquid(target, volAvail, performer);
                     source.insertItem(splitItem);
                  } catch (FailedException var14) {
                     logger.log(Level.WARNING, var14.getMessage(), (Throwable)var14);
                     return;
                  } catch (NoSuchTemplateException var15) {
                     logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
                     return;
                  }
               } else {
                  boolean differentOwners = target.getOwnerId() != source.getOwnerId();
                  target.setWeight(target.getWeightGrams() - volAvail, true, differentOwners);
                  int allWeight = liquid.getWeightGrams() + volAvail;
                  float newQl = (target.getCurrentQualityLevel() * (float)volAvail + liquid.getCurrentQualityLevel() * (float)liquid.getWeightGrams())
                     / (float)allWeight;
                  if (liquid.isColor()) {
                     liquid.setColor(WurmColor.mixColors(liquid.color, liquid.getWeightGrams(), target.color, volAvail, newQl));
                  }

                  float tmod = (float)volAvail / (float)allWeight;
                  float contMod = (float)liquid.getWeightGrams() / (float)allWeight;
                  liquid.setTemperature((short)((int)((float)target.getTemperature() * tmod + contMod * (float)liquid.getTemperature())));
                  liquid.setQualityLevel(newQl);
                  liquid.setWeight(liquid.getWeightGrams() + volAvail, true, differentOwners);
                  liquid.setDamage(0.0F);
               }
            } else {
               if (!performer.canCarry(target.getWeightGrams()) && source.getOwnerId() == performer.getWurmId()) {
                  performer.getCommunicator().sendNormalServerMessage("You won't be able to carry that much.");
                  return;
               }

               if (liquid == null) {
                  if (!source.testInsertItem(target)) {
                     return;
                  }

                  try {
                     Item parent = target.getParent();
                     parent.dropItem(target.getWurmId(), false);
                  } catch (NoSuchItemException var13) {
                  }

                  source.insertItem(target);
               } else {
                  if ((liquid.getTemplateId() == 417 || target.getTemplateId() == 417) && liquid.getRealTemplateId() != target.getRealTemplateId()) {
                     String name1 = "fruit";
                     String name2 = "fruit";
                     ItemTemplate t = liquid.getRealTemplate();
                     if (t != null) {
                        name1 = t.getName();
                     }

                     ItemTemplate t2 = target.getRealTemplate();
                     if (t2 != null) {
                        name2 = t2.getName();
                     }

                     if (!name1.equals(name2)) {
                        liquid.setName(name1 + " and " + name2 + " juice");
                     }

                     liquid.setRealTemplate(-10);
                  }

                  int allWeight = target.getWeightGrams() + liquid.getWeightGrams();
                  float newQl = (
                        target.getCurrentQualityLevel() * (float)target.getWeightGrams() + liquid.getCurrentQualityLevel() * (float)liquid.getWeightGrams()
                     )
                     / (float)allWeight;
                  if (liquid.isColor()) {
                     liquid.setColor(WurmColor.mixColors(liquid.color, liquid.getWeightGrams(), target.color, target.getWeightGrams(), newQl));
                  }

                  float tmod = (float)target.getWeightGrams() / (float)allWeight;
                  float contMod = (float)liquid.getWeightGrams() / (float)allWeight;
                  liquid.setTemperature((short)((int)((float)target.getTemperature() * tmod + contMod * (float)liquid.getTemperature())));
                  liquid.setQualityLevel(newQl);
                  liquid.setDamage(0.0F);
                  liquid.setWeight(allWeight, true);
                  Items.destroyItem(target.getWurmId());
               }
            }

            if (act.getNumber() != 345) {
               int tid = source.getTemplateId();
               String sound = "sound.liquid.fillcontainer";
               if (tid == 190 || tid == 189 || tid == 768) {
                  sound = "sound.liquid.fillcontainer.barrel";
               } else if (tid == 421) {
                  sound = "sound.liquid.fillcontainer.bucket";
               } else if (tid == 76) {
                  sound = "sound.liquid.fillcontainer.jar";
               }

               Methods.sendSound(performer, sound);
            }

            if (!quiet) {
               int tilex = performer.getTileX();
               int tiley = (int)performer.getStatus().getPositionY() >> 2;
               VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, performer.isOnSurface());
               performer.getCommunicator().sendNormalServerMessage("You fill the " + source.getName() + " with " + target.getName() + ".");
               vtile.broadCastAction(performer.getName() + " fills a " + source.getName() + " with " + target.getName() + ".", performer, false);
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You are trading one of those items.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You cannot keep liquid in that.");
      }
   }

   public static Item splitLiquid(Item target, int volAvail, Creature performer) throws FailedException, NoSuchTemplateException {
      Item splitItem = ItemFactory.createItem(target.getTemplateId(), target.getQualityLevel(), target.creator);
      splitItem.setWeight(volAvail, true);
      splitItem.setTemperature(target.getTemperature());
      splitItem.setAuxData(target.getAuxData());
      splitItem.setName(target.getActualName());
      splitItem.setCreator(target.creator);
      splitItem.setDamage(target.getDamage());
      splitItem.setMaterial(target.getMaterial());
      splitItem.setSizes(1, 1, 1);
      if (target.getRealTemplate() != null) {
         splitItem.setRealTemplate(target.getRealTemplateId());
      }

      if (target.descIsExam()) {
         splitItem.setDescription(target.examine(performer));
      } else {
         splitItem.setDescription(target.getDescription());
      }

      if (target.color != -1) {
         splitItem.setColor(target.color);
      }

      ItemMealData imd = ItemMealData.getItemMealData(target.getWurmId());
      if (imd != null) {
         ItemMealData.save(
            splitItem.getWurmId(),
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

      target.setWeight(target.getWeightGrams() - volAvail, true);
      return splitItem;
   }

   public static int eat(Creature performer, Item food) {
      float qlevel = food.getCurrentQualityLevel();
      int weight = food.getWeightGrams();
      float damage = food.getDamage();
      float hungerStilled = (float)weight * qlevel * (100.0F - damage) / 100.0F;
      if (performer.getSize() == 5) {
         hungerStilled *= 0.5F;
      } else if (performer.getSize() == 4) {
         hungerStilled *= 0.7F;
      } else if (performer.getSize() == 2) {
         hungerStilled *= 5.0F;
      } else if (performer.getSize() == 1) {
         hungerStilled *= 10.0F;
      }

      if (food.getTemplateId() == 272) {
         int fat = food.getFat();
         if (fat > 0) {
            food.setDamage(food.getDamage() + 50.0F);
            food.setButchered();
         } else {
            hungerStilled = 0.0F;
         }
      } else {
         Items.destroyItem(food.getWurmId());
      }

      return (int)hungerStilled;
   }

   static boolean eat(Action act, Creature performer, Item food, float counter) {
      boolean done = false;
      float qlevel = food.getCurrentQualityLevel();
      int weight = food.getWeightGrams();
      int temp = food.getTemperature();
      String hotness = "";
      if (temp > 1000) {
         hotness = "hot ";
      }

      if (!food.isFood()) {
         return true;
      } else {
         if (food.getOwnerId() != performer.getWurmId()) {
            if (!isLootableBy(performer, food)) {
               performer.getCommunicator().sendNormalServerMessage("You may not loot that item.");
               return true;
            }

            if (food.isBanked()) {
               performer.getCommunicator().sendNormalServerMessage("You can't eat from there.");
               return true;
            }

            if (checkIfStealing(food, performer, act)) {
               if (Action.checkLegalMode(performer)) {
                  return true;
               }

               if (!performer.maySteal()) {
                  performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
                  return true;
               }

               if (setTheftEffects(performer, act, food)) {
                  return true;
               }
            }
         }

         float bonus = 1.0F;
         if (performer.getDeity() != null && performer.getDeity().isFoodBonus() && performer.getFaith() >= 20.0F && performer.getFavor() >= 20.0F) {
            bonus = 1.25F;
         }

         if (food.getDamage() > 90.0F) {
            performer.getCommunicator().sendNormalServerMessage("Eww.. the " + hotness + food.getName() + " tastes funny and won't feed you at all!");
            return true;
         } else if ((food.getTemplateId() != 488 || food.getRealTemplateId() != 488)
            && food.getTemplateId() != 666
            && !food.isSource()
            && performer.getStatus().getHunger() - 3000 < 0) {
            performer.getCommunicator().sendNormalServerMessage("You are so full, you cannot possibly eat anything else.");
            if (counter != 1.0F && food.isWrapped() && (food.canBeClothWrapped() || food.canBePapyrusWrapped() || food.canBeRawWrapped())) {
               performer.getCommunicator().sendNormalServerMessage("You carefully re-wrap the " + hotness + food.getName(false) + " to keep it fresher.");
            }

            return true;
         } else {
            if (counter == 1.0F) {
               if (temp > 2500) {
                  performer.getCommunicator().sendNormalServerMessage("The " + food.getName() + " is too hot to eat.");
                  return true;
               }

               if (food.isCheese() && food.isZombiefied()) {
                  if (performer.getKingdomTemplateId() != 3) {
                     performer.getCommunicator().sendNormalServerMessage("The " + food.getName() + " is horrible, and you can't eat it.");
                     return true;
                  }

                  performer.getCommunicator().sendNormalServerMessage("The " + food.getName() + " tastes weird, but good.");
               }

               if (food.isWrapped() && (food.canBeClothWrapped() || food.canBePapyrusWrapped() || food.canBeRawWrapped())) {
                  performer.getCommunicator().sendNormalServerMessage("You carefully unwrap the " + hotness + food.getName(false) + " before eating it.");
               }

               int time = 0;
               time = (int)Math.min((float)performer.getStatus().getHunger() / (30.0F * qlevel), (float)food.getWeightGrams() / 30.0F) * 10;
               Server.getInstance().broadCastAction(performer.getName() + " starts to eat " + food.getNameWithGenus() + ".", performer, 3);
               performer.sendActionControl("Eating " + hotness + food.getName(), true, time);
               if (qlevel > 50.0F) {
                  performer.getCommunicator().sendNormalServerMessage("The " + hotness + food.getName() + " tastes " + food.getTasteString());
                  if (food.getTasteString().contains("singing")) {
                     performer.achievement(146);
                  }
               }
            }

            if (((Player)performer).getAlcohol() > 90.0F && (float)Server.rand.nextInt(100) < ((Player)performer).getAlcohol()) {
               performer.getCommunicator().sendNormalServerMessage("You miss the mouth and it ends up in your face. Who gives a damn?");
               Server.getInstance()
                  .broadCastAction(
                     performer.getName()
                        + " eats "
                        + food.getNameWithGenus()
                        + " with "
                        + performer.getHisHerItsString()
                        + " whole face instead of only with the mouth.",
                     performer,
                     3
                  );
               if (food.getTemplateId() != 666) {
                  Items.destroyItem(food.getWurmId());
                  SoundPlayer.playSound("sound.fish.splash", performer, 10.0F);
               }

               return true;
            } else {
               if (food.getSpellFoodBonus() > 0.0F) {
                  qlevel *= 1.0F + food.getSpellFoodBonus() / 100.0F;
               }

               float rarityMod = 0.0F;
               if (food.getRarity() > 0) {
                  rarityMod = (float)Math.max(1, food.getWeightGrams() / food.getTemplate().getWeightGrams());
                  rarityMod = (float)food.getRarity() * 10.0F / rarityMod;
                  rarityMod /= 100.0F;
                  rarityMod = Math.max(0.0F, Math.min(0.3F, rarityMod));
               }

               int fed = (int)(weight < 30 ? (float)weight * qlevel * bonus : 30.0F * qlevel * bonus);
               if (food.getTemplateId() == 488) {
                  performer.getStatus().modifyStamina((float)((int)(qlevel * 100.0F)));
               }

               if (weight < 30) {
                  done = true;
                  if (food.getTemplateId() == 666 && performer.isPlayer()) {
                     if ((long)((Player)performer).getSaveFile().getSleepLeft() >= (long)(((Player)performer).getSaveFile().isFlagSet(77) ? 5 : 4) * 3600L) {
                        performer.getCommunicator().sendNormalServerMessage("You just taste it, because eating it would be a waste right now.");
                        return true;
                     }

                     ((Player)performer).getSaveFile().addToSleep(3600);
                  }

                  modifyHunger(performer, food, weight, rarityMod, fed);
                  if (food.isSource()) {
                     performer.modifyKarma(weight);
                  }

                  if (food.isWrapped() && (food.canBeClothWrapped() || food.canBePapyrusWrapped() || food.canBeRawWrapped())) {
                     performer.getCommunicator().sendNormalServerMessage("You throw away the old wrapping, litter-bug.");
                  }

                  Items.destroyItem(food.getWurmId());
               } else {
                  if (food.isCheese() && food.isZombiefied() && performer.getKingdomId() == 3) {
                     performer.healRandomWound((int)(qlevel / 20.0F));
                  }

                  modifyHunger(performer, food, 30, rarityMod, fed);
                  food.setWeight(weight - 30, true);
                  if (food.isFish()) {
                     food.setIsUnderWeight(true);
                  }

                  if (food.isSource()) {
                     performer.modifyKarma(30);
                  }

                  if (food.getTemplateId() == 572 && WurmCalendar.currentTime - food.creationDate < 7200L && act.currentSecond() % 5 == 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + food.getName() + " is still alive! Its tentacles writhes about your face!");
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName()
                              + " eats a live "
                              + food.getName()
                              + "! Its tentacles writhes about "
                              + performer.getHisHerItsString()
                              + " face!",
                           performer,
                           5
                        );
                  }
               }

               if (act.currentSecond() % 5 == 0) {
                  SoundPlayer.playSound("sound.food.eat", performer, 1.0F);
               }

               if (performer.getStatus().getHunger() == 1) {
                  if (food.isWrapped() && (food.canBeClothWrapped() || food.canBePapyrusWrapped() || food.canBeRawWrapped())) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("You carefully re-wrap the " + hotness + food.getName(false) + " to keep it fresher.");
                  }

                  done = true;
               }

               return done;
            }
         }
      }
   }

   static void modifyHunger(Creature performer, Item food, int weight, float rarityMod, int fed) {
      float complexity = food.getFoodComplexity() * 50.0F;
      if (food.getTemplateId() == 1276) {
         complexity *= 0.1F;
         performer.getStatus()
            .modifyThirst(
               (float)(-fed * 2 / 3),
               food.getCaloriesByWeight(weight) * complexity,
               food.getCarbsByWeight(weight) * complexity,
               food.getFatsByWeight(weight) * complexity,
               food.getProteinsByWeight(weight) * complexity
            );
      } else {
         performer.getStatus()
            .modifyHunger(
               -fed,
               Math.min(0.99F, food.getNutritionLevel() + rarityMod),
               food.getCaloriesByWeight(weight) * complexity,
               food.getCarbsByWeight(weight) * complexity,
               food.getFatsByWeight(weight) * complexity,
               food.getProteinsByWeight(weight) * complexity
            );
         AffinitiesTimed.addTimedAffinityFromBonus(performer, weight, food);
      }
   }

   static boolean drinkChampagne(Creature performer, Item target) {
      Item parent = target.getParentOrNull();
      if (parent != null && parent.isSealedByPlayer()) {
         performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
         return true;
      } else if (target.getOwnerId() == performer.getWurmId()) {
         if (target.getAuxData() < 10) {
            target.setAuxData((byte)(target.getAuxData() + 1));
            if (performer.isPlayer()) {
               if (((Player)performer).getAlcohol() > 90.0F && (float)Server.rand.nextInt(100) < ((Player)performer).getAlcohol()) {
                  performer.getCommunicator().sendNormalServerMessage("You spill the " + target.getName() + " out. Who cares?");
                  Server.getInstance()
                     .broadCastAction(
                        performer.getName()
                           + " throws the "
                           + target.getName()
                           + " over "
                           + performer.getHisHerItsString()
                           + " shoulder instead of drinking it.",
                        performer,
                        3
                     );
                  return true;
               }

               ((Player)performer).setRarityShader(target.getRarity() > 0 ? target.getRarity() : 1);
            }

            performer.getStatus().modifyStamina((float)((int)(target.getQualityLevel() * (float)(20 + target.getRarity() * 10))));
            addAlcohol(performer);
            performer.getStatus().modifyThirst((float)((int)(-6554.0F * target.getQualityLevel() / 100.0F)));
            performer.getCommunicator().sendNormalServerMessage("Ahh! The " + target.getName() + " tastes goood!");
            return true;
         } else {
            performer.getCommunicator().sendNormalServerMessage("Sadly, the Champagne bottle is empty.");
            return true;
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You need to hold the Champagne to drink it.");
         return true;
      }
   }

   static boolean drink(Action act, Creature performer, Item drink, float counter) {
      if (performer.getStatus().getThirst() < 1000 && !drink.isSource() && !drink.isAlcohol()) {
         performer.getCommunicator().sendNormalServerMessage("You are so bloated you cannot bring yourself to drink any thing else.");
         return true;
      } else {
         Item parent = drink.getParentOrNull();
         if (parent != null && parent.isSealedByPlayer()) {
            performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
            return true;
         } else {
            boolean done = false;
            float fillmod = 6554.0F;
            float qlevel = drink.getCurrentQualityLevel();
            if (drink.getRarity() > 0) {
               float spamModifier = (float)Math.max(1, drink.getWeightGrams() / drink.getTemplate().getWeightGrams());
               qlevel = Math.min(99.99F, qlevel + (float)drink.getRarity() * 10.0F / spamModifier);
            }

            int temp = drink.getTemperature();
            int weight = drink.getWeightGrams();
            int template = drink.getTemplateId();
            if (template == 128) {
               qlevel = Math.max(90.0F, qlevel);
            } else {
               if (drink.getOwnerId() != performer.getWurmId()) {
                  if (drink.isBanked()) {
                     performer.getCommunicator().sendNormalServerMessage("You can't drink from there.");
                     return true;
                  }

                  if (drink.getTemplateId() != 128) {
                     if (!isLootableBy(performer, drink)) {
                        performer.getCommunicator().sendNormalServerMessage("You may not loot that item.");
                        return true;
                     }

                     if (checkIfStealing(drink, performer, act)) {
                        if (Action.checkLegalMode(performer)) {
                           return true;
                        }

                        if (!performer.maySteal()) {
                           performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
                           return true;
                        }

                        if (setTheftEffects(performer, act, drink)) {
                           return true;
                        }
                     }
                  }
               }

               if (drink.isMilk()) {
                  if (qlevel > 5.0F) {
                     qlevel = Math.min(100.0F, (float)((double)qlevel * 1.5));
                  }
               } else if (!drink.isDrinkable()) {
                  qlevel = 0.0F;
               }
            }

            if (counter == 1.0F && qlevel < 5.0F) {
               performer.getCommunicator().sendNormalServerMessage("Eww.. the " + drink.getName() + " tastes funny and won't quench your thirst at all!");
               done = true;
            } else {
               if (performer.isPlayer() && ((Player)performer).getAlcohol() > 90.0F && (float)Server.rand.nextInt(100) < ((Player)performer).getAlcohol()) {
                  performer.getCommunicator().sendNormalServerMessage("You spill the " + drink.getName() + " out. Who cares?");
                  Server.getInstance()
                     .broadCastAction(
                        performer.getName()
                           + " throws the "
                           + drink.getName()
                           + " over "
                           + performer.getHisHerItsString()
                           + " shoulder instead of drinking it.",
                        performer,
                        3
                     );
                  Items.destroyItem(drink.getWurmId());
                  return true;
               }

               if (counter == 1.0F) {
                  if (temp > 600 && template != 425) {
                     performer.getCommunicator().sendNormalServerMessage("The " + drink.getName() + " is too hot to drink.");
                     return true;
                  }

                  if (drink.isMilk() && drink.isZombiefied()) {
                     if (performer.getKingdomTemplateId() != 3) {
                        performer.getCommunicator().sendNormalServerMessage("Eww.. the " + drink.getName() + " tastes horrible! You can't drink it.");
                        return true;
                     }

                     performer.getCommunicator().sendNormalServerMessage("Hmm.. the " + drink.getName() + " tastes very special.");
                  }

                  int time = 10;
                  time = (int)Math.min((float)performer.getStatus().getThirst() / (6554.0F * qlevel / 100.0F) * 10.0F, (float)drink.getWeightGrams() / 200.0F);
                  Server.getInstance().broadCastAction(performer.getName() + " drinks some " + drink.getName() + ".", performer, 3);
                  performer.sendActionControl("Drinking " + drink.getName(), true, time);
                  if (qlevel > 50.0F) {
                     if (drink.getTemplateId() == 128) {
                        if (temp < 300) {
                           if (drink.isSalted()) {
                              performer.getCommunicator().sendNormalServerMessage("The water is slightly salty but still cools you down.");
                           } else {
                              performer.getCommunicator().sendNormalServerMessage("The water is refreshing and it cools you down.");
                           }
                        } else if (drink.isSalted()) {
                           performer.getCommunicator().sendNormalServerMessage("The water isn't exactly cold and has a salty taste.");
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("The water isn't exactly cold but still refreshens you.");
                        }
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("The " + drink.getName() + " tastes " + drink.getTasteString());
                     }
                  } else if (drink.isSalted()) {
                     performer.getCommunicator().sendNormalServerMessage("The water has a salty taste.");
                  }
               }

               if (drink.getTemplateId() == 427 || drink.isAlcohol()) {
                  performer.getStatus().modifyStamina((float)((int)(qlevel * (float)(20 + drink.getRarity() * 10))));
                  if (drink.isAlcohol() && performer.isPlayer()) {
                     Player player = (Player)performer;
                     float drinkMod = 1.0F;
                     drinkMod += drink.getCurrentQualityLevel() * 0.005F;
                     if (drink.getWeightGrams() < 200) {
                        drinkMod *= (float)drink.getWeightGrams() / 200.0F;
                     }

                     float addAlcohol = (float)drink.getAlcoholStrength() * 0.2F * drinkMod;
                     player.setAlcohol(((Player)performer).getAlcohol() + addAlcohol);
                     if (player.getAlcohol() > 20.0F && player.getSaveFile().setAlcoholTime(player.getAlcoholAddiction() + 10L)) {
                        player.getCommunicator()
                           .sendNormalServerMessage("You have just received the title '" + Titles.Title.Alcoholic.getName(player.isNotFemale()) + "'!");
                     }

                     if (player.getAlcohol() == 100.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You made it to the top! You are perfectly drunk!");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName()
                                 + " hits the record in drunkenness! "
                                 + performer.getHeSheItString()
                                 + " is perfectly drunk and can't drink any more!",
                              performer,
                              3
                           );
                        player.addTitle(Titles.Title.Drunkard);
                        player.achievement(296);
                     } else if (((Player)performer).getAlcohol() >= 95.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You are setting some kind of record.");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " has that dead, watery look in " + performer.getHisHerItsString() + " eyes now.", performer, 3
                           );
                     } else if (player.getAlcohol() >= 90.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You can barely walk.");
                        Server.getInstance().broadCastAction(performer.getName() + " looks concentrated and sways back and forth.", performer, 3);
                        performer.getMovementScheme().setDrunkMod(true);
                     } else if (player.getAlcohol() >= 60.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You are really really drunk.");
                        Server.getInstance().broadCastAction(performer.getName() + " is starting to drool.", performer, 3);
                     } else if (player.getAlcohol() >= 30.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You are drunk.");
                        Server.getInstance().broadCastAction(performer.getName() + " is verifyibly drunk now.", performer, 3);
                        performer.achievement(567);
                     } else if (player.getAlcohol() >= 20.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You are getting drunk.");
                        Server.getInstance().broadCastAction(performer.getName() + " suddenly giggles uncontrollably.", performer, 3);
                     } else if (player.getAlcohol() >= 10.0F) {
                        performer.getCommunicator().sendNormalServerMessage("You are tipsy.");
                     }
                  }
               }

               float complexity = drink.getFoodComplexity() * 20.0F;
               if ((float)weight * (qlevel / 10.0F) <= 2000.0F) {
                  performer.getStatus()
                     .modifyThirst(
                        (float)((int)(-((float)weight * qlevel / 20000.0F) * 6554.0F)),
                        drink.getCaloriesByWeight(weight) * complexity,
                        drink.getCarbsByWeight(weight) * complexity,
                        drink.getFatsByWeight(weight) * complexity,
                        drink.getProteinsByWeight(weight) * complexity
                     );
                  AffinitiesTimed.addTimedAffinityFromBonus(performer, weight, drink);
                  done = true;
                  Items.destroyItem(drink.getWurmId());
                  if (drink.isSource()) {
                     performer.modifyKarma(weight);
                  }
               } else {
                  if (drink.isMilk()
                     && drink.isZombiefied()
                     && performer.getStatus().getThirst() > 1000
                     && performer.getKingdomId() == 3
                     && Server.rand.nextInt(10) == 0) {
                     performer.healRandomWound((int)(qlevel / 10.0F));
                  }

                  performer.getStatus()
                     .modifyThirst(
                        (float)((int)(-6554.0F * qlevel / 100.0F)),
                        drink.getCaloriesByWeight(200) * complexity,
                        drink.getCarbsByWeight(200) * complexity,
                        drink.getFatsByWeight(200) * complexity,
                        drink.getProteinsByWeight(200) * complexity
                     );
                  AffinitiesTimed.addTimedAffinityFromBonus(performer, 200, drink);
                  drink.setWeight(weight - 200, false);
                  if (drink.isSource()) {
                     performer.modifyKarma(200);
                  }

                  if (drink.getWeightGrams() <= 0) {
                     Items.destroyItem(drink.getWurmId());
                  }
               }

               if (act.mayPlaySound()) {
                  SoundPlayer.playSound("sound.liquid.drink", performer, 1.0F);
               }
            }

            if (performer.getStatus().getThirst() <= 1) {
               done = true;
            }

            return done;
         }
      }
   }

   public static final void addAlcohol(Creature performer) {
      if (performer.isPlayer()) {
         Player player = (Player)performer;
         player.setAlcohol(((Player)performer).getAlcohol() + 3.0F);
         if (player.getAlcohol() > 20.0F && player.getSaveFile().setAlcoholTime(player.getAlcoholAddiction() + 10L)) {
            player.getCommunicator()
               .sendNormalServerMessage("You have just received the title '" + Titles.Title.Alcoholic.getName(player.isNotFemale()) + "'!");
         }

         if (player.getAlcohol() == 100.0F) {
            performer.getCommunicator().sendNormalServerMessage("You made it to the top! You are perfectly drunk!");
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " hits the record in drunkenness! " + performer.getHeSheItString() + " is perfectly drunk and can't drink any more!",
                  performer,
                  3
               );
            player.addTitle(Titles.Title.Drunkard);
            player.achievement(296);
         } else if (player.getAlcohol() >= 95.0F) {
            performer.getCommunicator().sendNormalServerMessage("You are setting some kind of record.");
            Server.getInstance()
               .broadCastAction(performer.getName() + " has that dead, watery look in " + performer.getHisHerItsString() + " eyes now.", performer, 3);
         } else if (player.getAlcohol() >= 90.0F) {
            performer.getCommunicator().sendNormalServerMessage("You can barely walk.");
            Server.getInstance().broadCastAction(performer.getName() + " looks concentrated and sways back and forth.", performer, 3);
            performer.getMovementScheme().setDrunkMod(true);
         } else if (player.getAlcohol() >= 60.0F) {
            performer.getCommunicator().sendNormalServerMessage("You are really really drunk.");
            Server.getInstance().broadCastAction(performer.getName() + " is starting to drool.", performer, 3);
         } else if (player.getAlcohol() >= 30.0F) {
            performer.getCommunicator().sendNormalServerMessage("You are drunk.");
            Server.getInstance().broadCastAction(performer.getName() + " is verifyibly drunk now.", performer, 3);
         } else if (player.getAlcohol() >= 20.0F) {
            performer.getCommunicator().sendNormalServerMessage("You are getting drunk.");
            Server.getInstance().broadCastAction(performer.getName() + " suddenly giggles uncontrollably.", performer, 3);
         } else if (player.getAlcohol() >= 10.0F) {
            performer.getCommunicator().sendNormalServerMessage("You are tipsy.");
         }
      }
   }

   static boolean drink(Creature performer, int tilex, int tiley, int tile, float counter, Action act) {
      boolean done = false;
      if (performer.isPlayer() && ((Player)performer).getAlcohol() > 90.0F && (float)Server.rand.nextInt(100) < ((Player)performer).getAlcohol()) {
         performer.getCommunicator().sendNormalServerMessage("You fall into the water and crawl back up all soaked. Who gives a damn?");
         SoundPlayer.playSound("sound.fish.splash", performer, 10.0F);
         return true;
      } else {
         if (counter == 1.0F) {
            int time = 10;
            time = (int)((float)performer.getStatus().getThirst() / 6554.0F) * 10;
            if (WaterType.isBrackish(tilex, tiley, performer.isOnSurface())) {
               Server.getInstance().broadCastAction(performer.getNameWithGenus() + " drinks some salty water.", performer, 3);
               performer.getCommunicator().sendNormalServerMessage("The water is slightly salty but still cools you down.");
            } else {
               Server.getInstance().broadCastAction(performer.getNameWithGenus() + " drinks some water.", performer, 3);
               performer.getCommunicator().sendNormalServerMessage("The water is refreshing and it cools you down.");
            }

            performer.sendActionControl("Drinking water", true, time);
         }

         performer.getStatus().modifyThirst(-6554.0F);
         if (act.mayPlaySound()) {
            SoundPlayer.playSound("sound.liquid.drink", performer, 10.0F);
         }

         if (performer.getStatus().getThirst() <= 1) {
            done = true;
         }

         return done;
      }
   }

   static boolean startDragging(Action act, Creature performer, Item dragged) {
      if (dragged.isVehicle()) {
         Vehicle vehic = Vehicles.getVehicle(dragged);
         if (vehic.pilotId != -10L) {
            performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " can not be dragged right now.");
            return true;
         }

         if (dragged.isMooredBoat()) {
            performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " is moored and can not be dragged right now.");
            return true;
         }

         if (dragged.getWurmId() == performer.getVehicle()) {
            performer.getCommunicator().sendNormalServerMessage("You can not drag the " + dragged.getName() + " now.");
            return true;
         }

         if (dragged.getLockId() != -10L
            && VehicleBehaviour.hasKeyForVehicle(performer, dragged)
            && !VehicleBehaviour.mayDriveVehicle(performer, dragged, act)) {
            performer.getCommunicator().sendNormalServerMessage("You are not allowed to drag the " + dragged.getName() + ".");
            return true;
         }

         if (vehic.draggers != null && vehic.draggers.size() > 0) {
            performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " won't budge since it is already dragged.");
            return true;
         }

         if (performer.isPlayer() && System.currentTimeMillis() - ((Player)performer).lastStoppedDragging < 2000L) {
            performer.getCommunicator().sendNormalServerMessage("You need to take a breath first.");
            return true;
         }
      }

      if (dragged.getTemplateId() == 445 && Servers.localServer.PVPSERVER && performer.getBodyStrength().getRealKnowledge() < 21.0) {
         performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " is too heavy.");
         return true;
      } else if (!performer.isWithinDistanceTo(dragged.getPosX(), dragged.getPosY(), dragged.getPosZ(), 4.0F)) {
         performer.getCommunicator().sendNormalServerMessage("You are too far away.");
         return true;
      } else {
         if (!Items.isItemDragged(dragged)) {
            if (checkIfStealing(dragged, performer, act)) {
               if (Action.checkLegalMode(performer)) {
                  return true;
               }

               if (!performer.maySteal()) {
                  performer.getCommunicator().sendNormalServerMessage("You need more body control to steal things.");
                  return true;
               }

               if (dragged.getItems().size() > 0) {
                  performer.getCommunicator().sendNormalServerMessage("You must empty the " + dragged.getName() + " before you steal it.");
                  return true;
               }

               if (setTheftEffects(performer, act, dragged)) {
                  return true;
               }
            }

            if (dragged.isBoat() && dragged.getCurrentQualityLevel() < 10.0F) {
               performer.getCommunicator().sendNormalServerMessage("The " + dragged.getName() + " is in too poor shape to be used.");
               return true;
            }

            TileEvent.log(dragged.getTileX(), dragged.getTileY(), dragged.isOnSurface() ? 0 : -1, performer.getWurmId(), 74);
            Items.startDragging(performer, dragged);
         } else {
            performer.getCommunicator().sendNormalServerMessage("That item is already being dragged by someone.");
         }

         return true;
      }
   }

   public static boolean stopDragging(Creature performer, Item dragged) {
      BlockingResult result = Blocking.getBlockerBetween(performer, dragged, 4);
      if (result == null && performer.getDraggedItem() == dragged) {
         Items.stopDragging(dragged);
         if (performer.getVisionArea() != null) {
            performer.getVisionArea().broadCastUpdateSelectBar(dragged.getWurmId());
         }
      }

      return true;
   }

   static final boolean yoyo(Creature performer, Item yoyo, float counter, Action act) {
      boolean toReturn = false;
      Skills skills = performer.getSkills();
      Skill yoyosk = null;
      String succ = "but fail";
      String point = ".";
      String trick = "hurricane";
      double diff = 5.0;
      double check = 0.0;

      try {
         yoyosk = skills.getSkill(10050);
      } catch (NoSuchSkillException var15) {
         yoyosk = skills.learn(10050, 1.0F);
      }

      if (counter == 1.0F) {
         performer.getCommunicator().sendNormalServerMessage("You start to spin your " + yoyo.getName() + ".");
         Server.getInstance()
            .broadCastAction(performer.getName() + " starts to spin " + performer.getHisHerItsString() + " " + yoyo.getName() + ".", performer, 5);
         performer.sendActionControl(Actions.actionEntrys[190].getVerbString(), true, 800);
      } else if (act.currentSecond() == 5) {
         performer.getCommunicator().sendNormalServerMessage("The " + yoyo.getName() + " has good speed now.");
         Server.getInstance().broadCastAction("The " + yoyo.getName() + " has good speed now.", performer, 5);
      } else if (act.currentSecond() == 10) {
         diff = (double)(5 - yoyo.getRarity());
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "sun";
      } else if (act.currentSecond() == 20) {
         diff = (double)(10 - yoyo.getRarity());
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "grind";
      } else if (act.currentSecond() == 30) {
         diff = (double)(20 - yoyo.getRarity());
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "cradle";
      } else if (act.currentSecond() == 40) {
         diff = (double)(40 - yoyo.getRarity());
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "suicide basilisk";
      } else if (act.currentSecond() == 50) {
         diff = (double)(60.0F - (float)yoyo.getRarity() * 1.5F);
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "whip";
      } else if (act.currentSecond() == 60) {
         diff = (double)(70 - yoyo.getRarity() * 2);
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "orbit";
      } else if (act.currentSecond() == 70) {
         diff = (double)(80.0F - (float)yoyo.getRarity() * 2.5F);
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "orbit over back";
      } else if (act.currentSecond() == 80) {
         diff = (double)(90 - yoyo.getRarity() * 3);
         check = yoyosk.skillCheck(diff, yoyo, 0.0, false, 10.0F);
         trick = "dragon knot";
         toReturn = true;
      }

      boolean destroyed = false;
      if (check != 0.0) {
         if (check > 0.0) {
            succ = "and succeed";
            point = "!";
         } else {
            toReturn = true;
         }

         performer.getCommunicator().sendNormalServerMessage("You try the '" + trick + "' " + succ + point);
         Server.getInstance().broadCastAction(performer.getName() + " tries the " + trick + " " + succ + "s" + point, performer, 5);
         if (yoyo.setDamage(yoyo.getDamage() + 0.005F * yoyo.getDamageModifier())) {
            destroyed = true;
            toReturn = true;
         }
      }

      if (!destroyed && toReturn) {
         performer.getCommunicator().sendNormalServerMessage("You reel the " + yoyo.getName() + " in.");
         Server.getInstance().broadCastAction(performer.getName() + " reels the " + yoyo.getName() + " in.", performer, 5);
      }

      return toReturn;
   }

   public static final int getImproveTemplateId(Item item) {
      if (item.isNoImprove()) {
         return -10;
      } else {
         byte material = getImproveMaterial(item);
         return material == 0 ? -10 : Materials.getTemplateIdForMaterial(material);
      }
   }

   public static final int getImproveSkill(Item item) {
      int material = item.getMaterial();
      if (material == 0) {
         return -10;
      } else {
         CreationEntry entry = CreationMatrix.getInstance().getCreationEntry(item.getTemplateId());
         if (entry == null) {
            if (item.getTemplateId() == 430 || item.getTemplateId() == 528 || item.getTemplateId() == 638) {
               return 1013;
            } else {
               return item.getTemplate().isStatue() ? 10074 : -10;
            }
         } else {
            return item.getTemplateId() != 623 || material != 7 && material != 8 && material != 96 ? entry.getPrimarySkill() : 10043;
         }
      }
   }

   private static final int getImproveSkill(byte material, int templateId) {
      if (material == 0) {
         return -10;
      } else {
         CreationEntry entry = CreationMatrix.getInstance().getCreationEntry(templateId);
         if (entry != null) {
            return entry.getPrimarySkill();
         } else {
            return templateId != 430 && templateId != 528 && templateId != 638 ? -10 : 1013;
         }
      }
   }

   static void triggerImproveAchievements(Creature performer, Item target, Skill improve, boolean wasHighest, float oldQL) {
      performer.achievement(205);
      if ((double)target.getQualityLevel() > improve.getKnowledge(0.0) && improve.getKnowledge(0.0) > 30.0) {
         performer.achievement(217);
      }

      if (!wasHighest && Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target.getWurmId(), false)) {
         performer.achievement(317);
      }

      if (target.getQualityLevel() >= 99.0F && oldQL < 99.0F) {
         performer.achievement(222);
      } else if (target.getQualityLevel() >= 90.0F && oldQL < 90.0F) {
         performer.achievement(221);
      } else if (target.getQualityLevel() >= 70.0F && oldQL < 70.0F) {
         performer.achievement(220);
         if (target.isTool()) {
            performer.achievement(565);
         }
      } else if (target.getQualityLevel() >= 50.0F && oldQL < 50.0F) {
         performer.achievement(219);
         if (target.isTool()) {
            performer.achievement(543);
         }

         if (target.isWeapon()) {
            performer.achievement(554);
         }
      } else if (target.getQualityLevel() >= 15.0F && oldQL < 15.0F && target.isMetal()) {
         performer.achievement(520);
      }

      if (target.isArmour() && target.getQualityLevel() >= 80.0F && oldQL < 80.0F) {
         performer.achievement(597);
      }
   }

   static final boolean improveItem(Action act, Creature performer, Item source, Item target, float counter) {
      boolean toReturn = false;
      boolean insta = performer.getPower() >= 5;
      if (counter == 0.0F || counter == 1.0F || act.justTickedSecond()) {
         if (source.getWurmId() == target.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + target.getName() + " using itself as a tool.");
            return true;
         }

         if (!target.isRepairable()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve that item.");
            return true;
         }

         if (target.getParentId() != -10L) {
            try {
               ItemTemplate temp = target.getRealTemplate();
               if (temp != null && !temp.isVehicle()) {
                  Item parent = target.getParent();
                  if ((parent.getSizeX() < temp.getSizeX() || parent.getSizeY() < temp.getSizeY() || parent.getSizeZ() <= temp.getSizeZ())
                     && parent.getTemplateId() != 177
                     && parent.getTemplateId() != 0) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("It's too tight to try and work on the " + target.getName() + " in the " + parent.getName() + ".");
                     return true;
                  }
               }
            } catch (NoSuchItemException var40) {
            }
         }

         if (target.creationState != 0) {
            performer.getCommunicator().sendNormalServerMessage("You can not improve the " + target.getName() + " by adding more material right now.");
            return true;
         }

         if (!insta) {
            if (target.getDamage() > 0.0F) {
               performer.getCommunicator().sendNormalServerMessage("Repair the " + target.getName() + " before you try to improve it.");
               return true;
            }

            if (target.isMetal() && !target.isNoTake() && target.getTemperature() < 3500) {
               performer.getCommunicator().sendNormalServerMessage("Metal needs to be glowing hot while smithing.");
               return true;
            }

            if (source.isCombine() && source.isMetal() && source.getTemperature() < 3500) {
               performer.getCommunicator().sendNormalServerMessage("Metal needs to be glowing hot while smithing.");
               return true;
            }
         }
      }

      Skills skills = performer.getSkills();
      Skill improve = null;
      int skillNum = getImproveSkill(target);
      if (skillNum != -10 && !target.isNewbieItem() && !target.isChallengeNewbieItem()) {
         int time = 1000;
         int templateId = getImproveTemplateId(target);
         if (source.getTemplateId() == templateId) {
            try {
               improve = skills.getSkill(skillNum);
            } catch (NoSuchSkillException var39) {
               improve = skills.learn(skillNum, 1.0F);
            }

            Skill secondarySkill = null;

            try {
               secondarySkill = skills.getSkill(source.getPrimarySkill());
            } catch (Exception var38) {
               try {
                  secondarySkill = skills.learn(source.getPrimarySkill(), 1.0F);
               } catch (Exception var37) {
               }
            }

            double power = 0.0;
            double bonus = 0.0;
            if (performer.isPriest()) {
               bonus = -20.0;
            }

            float runeModifier = 1.0F;
            if (target.getSpellEffects() != null) {
               runeModifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
            }

            float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
            double improveBonus = 0.23047 * (double)imbueEnhancement * (double)runeModifier;
            float improveItemBonus = ItemBonus.getImproveSkillMaxBonus(performer);
            double max = improve.getKnowledge(0.0) * (double)improveItemBonus + (100.0 - improve.getKnowledge(0.0) * (double)improveItemBonus) * improveBonus;
            double diff = Math.max(0.0, max - (double)target.getQualityLevel());
            float skillgainMod = 1.0F;
            if (diff <= 0.0) {
               skillgainMod = 2.0F;
            }

            if (counter != 1.0F) {
               time = act.getTimeLeft();
               float failsec = act.getFailSecond();
               power = (double)act.getPower();
               if (counter >= failsec) {
                  if (secondarySkill != null) {
                     bonus = Math.max(
                        bonus,
                        secondarySkill.skillCheck(
                           (double)target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 3.0F : counter / 2.0F
                        )
                     );
                  }

                  if (performer.isPriest()) {
                     bonus = Math.min(bonus, 0.0);
                  }

                  improve.skillCheck((double)target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 2.0F : counter);
                  if (power != 0.0) {
                     if (!target.isBodyPart()) {
                        if (!target.isLiquid()) {
                           target.setDamage(target.getDamage() - act.getPower());
                           performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + " a little.");
                           Server.getInstance()
                              .broadCastAction(
                                 performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + target.getNameWithGenus() + " a little.",
                                 performer,
                                 5
                              );
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("You fail.");
                           Server.getInstance().broadCastAction(performer.getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + target.getName() + " and stop.");
                     Server.getInstance().broadCastAction(performer.getName() + " stops improving " + target.getNameWithGenus() + ".", performer, 5);
                  }

                  performer.getStatus().modifyStamina(-counter * 1000.0F);
                  return true;
               }
            } else {
               if ((source.isCombine() || templateId == 9) && source.getCurrentQualityLevel() <= target.getQualityLevel()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + source.getName() + " is in too poor shape to improve the " + target.getName() + ".");
                  return true;
               }

               performer.getCommunicator().sendNormalServerMessage("You start to improve the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to improve " + target.getNameWithGenus() + ".", performer, 5);
               time = Actions.getImproveActionTime(performer, source);
               performer.sendActionControl(Actions.actionEntrys[192].getVerbString(), true, time);
               act.setTimeLeft(time);
               if (performer.getDeity() != null && performer.getDeity().isRepairer() && performer.getFaith() >= 80.0F && performer.getFavor() >= 40.0F) {
                  bonus += 10.0;
               }

               power = improve.skillCheck((double)target.getQualityLevel(), source, bonus, true, 1.0F);
               double mod = (double)(
                  (100.0F - target.getQualityLevel())
                     / 20.0F
                     / 100.0F
                     * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat())
                     / 2.0F
               );
               if (power < 0.0) {
                  act.setFailSecond((float)((int)Math.max(20.0F, (float)time * Server.rand.nextFloat())));
                  act.setPower((float)(-mod * Math.max(1.0, diff)));
               } else {
                  if (diff <= 0.0) {
                     mod *= 0.01F;
                  }

                  double regain = 1.0;
                  if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
                     regain = 2.0;
                  }

                  diff *= regain;
                  int tid = target.getTemplateId();
                  if (target.isArmour()
                     || target.isCreatureWearableOnly()
                     || target.isWeapon()
                     || target.isShield()
                     || tid == 455
                     || tid == 454
                     || tid == 456
                     || tid == 453
                     || tid == 451
                     || tid == 452) {
                     mod *= 2.0;
                  }

                  if (tid == 455 || tid == 454 || tid == 456 || tid == 453 || tid == 451 || tid == 452) {
                     mod *= 2.0;
                  }

                  Titles.Title title = performer.getTitle();
                  if (title != null && title.getSkillId() == improve.getNumber() && (target.isArmour() || target.isCreatureWearableOnly())) {
                     mod *= 1.3F;
                  }

                  if (target.getRarity() > 0) {
                     mod *= (double)(1.0F + (float)target.getRarity() * 0.1F);
                  }

                  act.setPower((float)(mod * Math.max(1.0, diff)));
               }
            }

            if (act.mayPlaySound()) {
               sendImproveSound(performer, source, target, skillNum);
            }

            if (counter * 10.0F > (float)time || insta) {
               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }

               float maxGain = 1.0F;
               byte rarity = target.getRarity();
               float rarityChance = 0.2F;
               if (target.getSpellEffects() != null) {
                  rarityChance *= target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
               }

               if (act.getRarity() > rarity && Server.rand.nextFloat() <= rarityChance) {
                  rarity = act.getRarity();
               }

               float switchImproveChance = 1.0F;
               if (source.isCombine() || source.getTemplateId() == 9 || source.getTemplateId() == 72 || source.isDragonArmour()) {
                  float mod = 0.05F;
                  if (Servers.localServer.EPIC && source.isDragonArmour()) {
                     mod = 0.01F;
                  }

                  int usedWeight = (int)Math.min(500.0F, Math.max(1.0F, (float)target.getWeightGrams() * mod));
                  if (source.getWeightGrams() < Math.min(source.getTemplate().getWeightGrams(), usedWeight)) {
                     maxGain = Math.min(1.0F, (float)source.getWeightGrams() / (float)usedWeight);
                     switchImproveChance = (float)source.getWeightGrams() / (float)usedWeight;
                  }

                  source.setWeight(source.getWeightGrams() - usedWeight, true);
                  if (source.deleted && source.getRarity() > rarity && Server.rand.nextInt(100) == 0) {
                     rarity = source.getRarity();
                  }
               } else if (!source.isBodyPart() && !source.isLiquid()) {
                  source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
               }

               if (secondarySkill != null) {
                  bonus = Math.max(
                     bonus,
                     secondarySkill.skillCheck(
                        (double)target.getQualityLevel(), source, bonus, false, skillgainMod * (performer.isPriest() ? counter / 3.0F : counter / 2.0F)
                     )
                  );
               }

               if (performer.isPriest()) {
                  bonus = Math.min(bonus, 0.0);
               }

               improve.skillCheck((double)target.getQualityLevel(), source, bonus, false, skillgainMod * (performer.isPriest() ? counter / 2.0F : counter));
               power = (double)act.getPower();
               if (power > 0.0) {
                  performer.getCommunicator().sendNormalServerMessage("You improve the " + target.getName() + " a bit.");
                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "before: " + target.getQualityLevel() + " now: " + ((double)target.getQualityLevel() + power) + " power=" + power
                        );
                  }

                  if (Servers.isThisATestServer()) {
                     performer.getCommunicator().sendNormalServerMessage("switchImproveChance = " + switchImproveChance);
                  }

                  Server.getInstance().broadCastAction(performer.getName() + " improves " + target.getNameWithGenus() + " a bit.", performer, 5);
                  byte newState = 0;
                  if (switchImproveChance >= Server.rand.nextFloat()) {
                     newState = (byte)Server.rand.nextInt(5);
                  }

                  if (Server.rand.nextFloat() * 20.0F > target.getQualityLevel()) {
                     newState = 0;
                  }

                  Item toRarify = target;
                  if (target.getTemplateId() == 128) {
                     toRarify = source;
                  }

                  if (rarity > toRarify.getRarity()) {
                     toRarify.setRarity(rarity);

                     for(Item sub : toRarify.getItems()) {
                        if (sub != null && sub.isComponentItem()) {
                           sub.setRarity(rarity);
                        }
                     }

                     if (toRarify.getRarity() > 2) {
                        performer.achievement(300);
                     } else if (toRarify.getRarity() == 1) {
                        performer.achievement(301);
                     } else if (toRarify.getRarity() == 2) {
                        performer.achievement(302);
                     }
                  }

                  if (newState != 0) {
                     target.setCreationState(newState);
                     String newString = getNeededCreationAction(getImproveMaterial(target), newState, target);
                     performer.getCommunicator().sendNormalServerMessage(newString);
                  } else if (skillNum != -10) {
                     try {
                        ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                        performer.getCommunicator()
                           .sendNormalServerMessage("The " + target.getName() + " could be improved with some more " + temp.getName() + ".");
                     } catch (NoSuchTemplateException var36) {
                     }
                  }

                  boolean wasHighest = Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target.getWurmId(), true);
                  float oldQL = target.getQualityLevel();
                  float modifier = 1.0F;
                  if (target.getSpellEffects() != null) {
                     modifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
                  }

                  modifier *= target.getMaterialImpBonus();
                  target.setQualityLevel(Math.min(100.0F, (float)((double)target.getQualityLevel() + power * (double)maxGain * (double)modifier)));
                  if (target.getQualityLevel() > target.getOriginalQualityLevel()) {
                     target.setOriginalQualityLevel(target.getQualityLevel());
                     triggerImproveAchievements(performer, target, improve, wasHighest, oldQL);
                  }
               } else {
                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Dam before: " + target.getDamage() + " now: " + ((double)target.getDamage() - power) + " power=" + power);
                  }

                  if (!target.isBodyPart()) {
                     if (!target.isLiquid()) {
                        target.setDamage(target.getDamage() - (float)power);
                        performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + " a little.");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + target.getNameWithGenus() + " a little.",
                              performer,
                              5
                           );
                        performer.achievement(206);
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You fail.");
                        Server.getInstance().broadCastAction(performer.getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
                     }
                  }
               }

               performer.getStatus().modifyStamina(-counter * 1000.0F);
               toReturn = true;
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the item with that.");
            toReturn = true;
         }

         return toReturn;
      } else {
         performer.getCommunicator().sendNormalServerMessage("You cannot improve that item.");
         return true;
      }
   }

   public static final boolean destroyItem(int action, Creature performer, Item destroyItem, Item target, boolean dealItems, float counter) {
      if (!Methods.isActionAllowed(performer, (short)action, target)) {
         return true;
      } else {
         if (target.getTemplateId() == 1432) {
            for(Item item : target.getItemsAsArray()) {
               if (item.getTemplateId() == 1436 && !item.isEmpty(true)) {
                  performer.getCommunicator().sendNormalServerMessage("You cannot destroy this coop with a creature inside.");
                  return true;
               }
            }
         }

         boolean toReturn = true;
         boolean ok = false;
         if (target.isTraded()) {
            ok = false;
         } else if (target.getOwnerId() == performer.getWurmId()) {
            ok = true;
         } else if ((long)target.getZoneId() != -10L) {
            if (!performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
               performer.getCommunicator().sendNormalServerMessage("You are too far away to do that.");
               return true;
            }

            ok = true;
            if ((target.isKingdomMarker() && target.getKingdom() == performer.getKingdomId() || target.isTent() && !Servers.localServer.PVPSERVER)
               && performer.getWurmId() != target.lastOwner) {
               VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
               if (t == null || t.getVillage() == null || t.getVillage() != performer.getCitizenVillage()) {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to destroy the " + target.getName() + ".");
                  return true;
               }
            }

            if (target.isGuardTower() && target.getKingdom() != performer.getKingdomId()) {
               GuardTower tower = Kingdoms.getTower(target);
               if (tower.getGuardCount() > 0) {
                  performer.getCommunicator().sendNormalServerMessage("A nearby guard stops you with a warning.");
                  return true;
               }
            }
         }

         if (!ok) {
            return true;
         } else {
            int time = 1000;
            boolean insta = performer.getPower() >= 2 && destroyItem.isWand();
            float mod = target.getDamageModifierForItem(destroyItem);
            if (mod <= 0.0F && !insta) {
               performer.getCommunicator().sendNormalServerMessage("You will not do any damage to the " + target.getName() + " with that.");
               return true;
            } else {
               toReturn = false;
               Action act = null;
               String destString = "destroy";
               if (dealItems) {
                  destString = "disassemble";
               }

               if (action == 757) {
                  destString = "pry";
               }

               try {
                  act = performer.getCurrentAction();
               } catch (NoSuchActionException var24) {
                  logger.log(Level.WARNING, "No Action for " + performer.getName() + "!", (Throwable)var24);
                  return true;
               }

               if (counter == 1.0F) {
                  time = 300;
                  if (action == 757) {
                     time = 200;
                  }

                  if (Servers.localServer.isChallengeServer()) {
                     if (!target.isEnchantedTurret()) {
                        time /= 2;
                     } else {
                        time /= 3;
                     }
                  }

                  performer.getCommunicator().sendNormalServerMessage("You start to " + destString + " the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to " + destString + " " + target.getNameWithGenus() + ".", performer, 5);
                  performer.sendActionControl(Actions.actionEntrys[action].getVerbString(), true, time);
                  act.setTimeLeft(time);
                  performer.getStatus().modifyStamina(-800.0F);
               } else {
                  time = act.getTimeLeft();
               }

               if (act.currentSecond() % 5 == 0) {
                  String s = "sound.destroyobject.wood.axe";
                  if (destroyItem.isWeaponCrush()) {
                     s = "sound.destroyobject.wood.maul";
                  }

                  if (target.isStone()) {
                     if (destroyItem.isWeaponCrush()) {
                        s = "sound.destroyobject.stone.maul";
                     } else {
                        s = "sound.destroyobject.stone.axe";
                     }
                  }

                  if (target.isMetal()) {
                     if (destroyItem.isWeaponCrush()) {
                        s = "sound.destroyobject.metal.maul";
                     } else {
                        s = "sound.destroyobject.metal.axe";
                     }
                  }

                  SoundPlayer.playSound(s, target, 0.5F);
                  performer.getStatus().modifyStamina(-5000.0F);
                  if (destroyItem != null && !destroyItem.isBodyPartAttached()) {
                     destroyItem.setDamage(destroyItem.getDamage() + mod * destroyItem.getDamageModifier());
                  }
               }

               if (counter * 10.0F > (float)time || insta) {
                  Skills skills = performer.getSkills();
                  Skill destroySkill = null;

                  try {
                     destroySkill = skills.getSkill(102);
                  } catch (NoSuchSkillException var23) {
                     destroySkill = skills.learn(102, 1.0F);
                  }

                  destroySkill.skillCheck(20.0, destroyItem, 0.0, false, Math.min(10.0F, counter));
                  double damage = 0.0;
                  if (insta && mod <= 0.0F) {
                     damage = 100.0;
                     mod = 1.0F;
                  } else {
                     damage = Weapon.getModifiedDamageForWeapon(destroyItem, destroySkill) * 50.0;
                     damage /= (double)(target.getQualityLevel() / 10.0F);
                     if (target.getTemplateId() == 445 || target.getTemplateId() == 1125) {
                        damage *= 50.0;
                     } else if (target.getTemplateId() == 937) {
                        damage *= 25.0;
                     } else if (Servers.localServer.isChallengeServer()) {
                        damage *= 5.0;
                     }
                  }

                  VolaTile tile = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
                  if (target.isKingdomMarker()) {
                     if (target.getKingdom() != performer.getKingdomId()) {
                        mod *= 0.75F;
                        if (!Features.Feature.TOWER_CHAINING.isEnabled()) {
                           GuardTower t = Kingdoms.getTower(target);
                           if (t != null) {
                              t.sendAttackWarning();
                           }
                        }
                     } else if (performer.getWurmId() == target.getLastOwnerId()) {
                        mod = 2.0F;
                     }
                  } else if (target.isRoadMarker() && action == 757 && destroyItem.getTemplateId() == 1115) {
                     damage *= 15.0;
                     mod *= 10.0F;
                  }

                  if (tile != null && tile.getVillage() != null) {
                     if (MethodsStructure.isCitizenAndMayPerformAction((short)83, performer, tile.getVillage())) {
                        if (!target.isKingdomMarker() || !Servers.localServer.PVPSERVER || target.getLastOwnerId() == performer.getWurmId()) {
                           damage *= 50.0;
                        }
                     } else if (MethodsStructure.isAllyAndMayPerformAction((short)83, performer, tile.getVillage())
                        && (!target.isKingdomMarker() || !Servers.localServer.PVPSERVER || target.getLastOwnerId() == performer.getWurmId())) {
                        damage *= 25.0;
                     }
                  } else if (target.isStreetLamp()) {
                     damage *= 20.0;
                  } else if (target.isSign() && Servers.localServer.PVPSERVER) {
                     damage *= 10.0;
                  } else if (target.isMarketStall()) {
                     damage *= 10.0;
                  } else if (!target.isKingdomMarker() && !Servers.localServer.PVPSERVER && target.getLastOwnerId() == performer.getWurmId()) {
                     damage *= 5.0;
                  }

                  damage *= Weapon.getMaterialBashModifier(destroyItem.getMaterial());
                  if (performer.getCultist() != null && performer.getCultist().doubleStructDamage()) {
                     damage *= 2.0;
                  }

                  if (target.getTemplateId() == 521) {
                     mod += 0.01F;
                     if (Server.rand.nextInt(100) == 0 && target.getData1() > 0) {
                        try {
                           CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                           byte sex = ctemplate.getSex();
                           if (sex == 0 && !ctemplate.keepSex && Server.rand.nextInt(2) == 0) {
                              sex = 1;
                           }

                           byte ctype = target.getAuxData();
                           if (Server.rand.nextInt(40) == 0) {
                              ctype = 99;
                           }

                           Creature.doNew(
                              ctemplate.getTemplateId(),
                              ctype,
                              target.getPosX(),
                              target.getPosY(),
                              (float)Server.rand.nextInt(360),
                              target.isOnSurface() ? 0 : -1,
                              "",
                              sex
                           );
                        } catch (Exception var22) {
                           logger.log(Level.WARNING, var22.getMessage(), (Throwable)var22);
                        }
                     }
                  }

                  if (target.getTemplateId() == 731) {
                     damage = 100.0;
                     mod = 1.0F;
                  }

                  float newDam = (float)((double)target.getDamage() + damage * (double)mod);
                  float oldDam = target.getDamage();
                  String towerName = "Unknown";
                  if (Features.Feature.TOWER_CHAINING.isEnabled() && target.isKingdomMarker()) {
                     GuardTower t = Kingdoms.getTower(target);
                     if (t != null) {
                        if (newDam >= 100.0F) {
                           towerName = t.getName();
                        }

                        t.checkBashDamage(oldDam, newDam);
                     }
                  }

                  if (target.isRoadMarker()) {
                     if (performer.fireTileLog()) {
                        TileEvent.log(target.getTileX(), target.getTileY(), target.isOnSurface() ? 0 : -1, performer.getWurmId(), action);
                     }

                     if (newDam >= 100.0F) {
                        target.setWhatHappened("bashed by " + performer.getName());
                     }
                  }

                  if (target.setDamage(newDam)) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " falls apart with a crash.");
                     Server.getInstance()
                        .broadCastAction(performer.getName() + " damages " + target.getNameWithGenus() + " and it falls apart with a crash.", performer, 5);
                     if (target.isKingdomMarker() && Features.Feature.TOWER_CHAINING.isEnabled()) {
                        Players.getInstance().broadCastDestroyInfo(performer, towerName + " has been destroyed.");
                        Server.getInstance().broadCastEpicEvent(towerName + " has been destroyed.");
                     }

                     if (performer.getDeity() != null) {
                        performer.performActionOkey(act);
                     }

                     MissionTriggers.activateTriggers(performer, destroyItem, 913, target.getWurmId(), (int)counter);
                     if (target.getTemplateId() == 521 && Servers.localServer.PVPSERVER && !Servers.isThisAChaosServer()) {
                        performer.getFightingSkill()
                           .setKnowledge(
                              performer.getFightingSkill().getRealKnowledge()
                                 + (100.0 - performer.getFightingSkill().getRealKnowledge()) * (double)spawnDamageMod,
                              false
                           );
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " damages " + target.getNameWithGenus() + ".", performer, 5);
                  }

                  toReturn = true;
               }

               return toReturn;
            }
         }
      }
   }

   static final boolean filet(Action act, Creature performer, Item source, Item target, float counter) {
      boolean done = false;
      if (!target.isRoyal() && !target.isIndestructible()) {
         int time = 200;
         if (target.getOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You need to be in possession of the " + target.getName() + " in order to filet it.");
            return true;
         } else {
            if (counter == 1.0F) {
               Skill butchering = performer.getSkills().getSkillOrLearn(10059);
               int nums = 2;
               int tid = 368;

               try {
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tid);
                  nums = target.getWeightGrams() / template.getWeightGrams();
                  if (nums == 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too small to produce a filet.");
                     done = true;
                  }
               } catch (NoSuchTemplateException var25) {
                  logger.log(Level.WARNING, "No template for filet?" + var25.getMessage(), (Throwable)var25);
                  done = true;
               }

               if (!done) {
                  performer.getCommunicator().sendNormalServerMessage("You start to filet the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " starts to filet a " + target.getName() + ".", performer, 5);
                  time = Actions.getStandardActionTime(performer, butchering, source, 0.0);
                  act.setTimeLeft(time);
                  performer.sendActionControl(Actions.actionEntrys[225].getVerbString(), true, time);
                  SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
               }
            } else {
               time = act.getTimeLeft();
               if (act.currentSecond() % 5 == 0) {
                  source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
                  SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
               }
            }

            if (counter * 10.0F >= (float)time) {
               Skill butchering = performer.getSkills().getSkillOrLearn(10059);
               done = true;
               boolean filet = true;
               int nums = 2;

               try {
                  int tid = 368;
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(tid);
                  int tweight = template.getWeightGrams();
                  nums = target.getWeightGrams() / tweight;
                  if (nums == 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too small to produce a filet.");
                     done = true;
                     filet = false;
                  }

                  int invnums = performer.getInventory().getNumItemsNotCoins();
                  if (invnums + nums >= 100) {
                     performer.getCommunicator().sendNormalServerMessage("You can't make space in your inventory for the filets.");
                     done = true;
                     filet = false;
                  }

                  if (filet) {
                     double bonus = 0.0;

                     try {
                        int primarySkill = source.getPrimarySkill();
                        Skill primskill = null;

                        try {
                           primskill = performer.getSkills().getSkill(primarySkill);
                        } catch (Exception var23) {
                           primskill = performer.getSkills().learn(primarySkill, 1.0F);
                        }

                        bonus = primskill.skillCheck(10.0, 0.0, false, counter);
                     } catch (NoSuchSkillException var24) {
                     }

                     performer.getCommunicator().sendNormalServerMessage("You filet the " + target.getName() + ".");
                     Server.getInstance().broadCastAction(performer.getName() + " filets " + target.getNameWithGenus() + ".", performer, 5);
                     float ql = 0.0F;
                     float imbueEnhancement = 1.0F;
                     float max = target.getCurrentQualityLevel() * 1.0F;

                     for(int x = 0; x < nums; ++x) {
                        ql = Math.max(1.0F, (float)butchering.skillCheck((double)target.getDamage(), source, bonus, nums > 10, 1.0F));

                        try {
                           Item fil = ItemFactory.createItem(tid, Math.min(max, ql), null);
                           fil.setName("fillet of " + target.getActualName().toLowerCase());
                           if (target.isMeat()) {
                              fil.setMaterial(target.getMaterial());
                           } else {
                              fil.setRealTemplate(target.getTemplateId());
                           }

                           fil.setAuxData(target.getAuxData());
                           if (target.getTemperature() > 200) {
                              fil.setTemperature(target.getTemperature());
                           }

                           performer.getInventory().insertItem(fil);
                        } catch (FailedException var21) {
                           logger.log(Level.WARNING, performer.getName() + ":" + var21.getMessage(), (Throwable)var21);
                        } catch (NoSuchTemplateException var22) {
                           logger.log(Level.WARNING, "No template for filet?" + var22.getMessage(), (Throwable)var22);
                        }
                     }

                     Items.destroyItem(target.getWurmId());
                  }
               } catch (NoSuchTemplateException var26) {
                  logger.log(Level.WARNING, "No template for filet?" + var26.getMessage(), (Throwable)var26);
                  done = true;
               }
            }

            return done;
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You mysteriously cut yourself when trying to filet that!");
         CombatEngine.addWound(
            performer, performer, (byte)1, 13, (double)(2000 + Server.rand.nextInt(2000)), 0.0F, "cut", null, 0.0F, 0.0F, false, false, false, false
         );
         return true;
      }
   }

   static final boolean filetFish(Action act, Creature performer, Item source, Item target, float counter) {
      if (!target.isRoyal() && !target.isIndestructible()) {
         int time = 200;
         if (target.getOwnerId() != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You need to be in possession of the " + target.getName() + " in order to filet it.");
            return true;
         } else if (counter == 1.0F) {
            try {
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(369);
               int tweight = template.getWeightGrams();
               int waste = tweight / 10;
               int nums = (target.getWeightGrams() + waste) / (tweight + waste);
               if (nums == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too small to produce a filet.");
                  return true;
               }

               time = nums * 10;
            } catch (NoSuchTemplateException var28) {
               performer.getCommunicator().sendNormalServerMessage("Something went horribly wrong with " + target.getName() + ". Please use /support.");
               logger.log(Level.WARNING, "No template for filet?" + var28.getMessage(), (Throwable)var28);
               return true;
            }

            performer.getCommunicator().sendNormalServerMessage("You start to filet the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to filet a " + target.getName() + ".", performer, 5);
            act.setTimeLeft(time);
            performer.sendActionControl(Actions.actionEntrys[225].getVerbString(), true, time);
            SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
            return false;
         } else {
            time = act.getTimeLeft();
            if (act.mayPlaySound()) {
               source.setDamage(source.getDamage() + 5.0E-4F * source.getDamageModifier());
               SoundPlayer.playSound("sound.butcherKnife", performer, 1.0F);
            }

            if (act.justTickedSecond()) {
               int invnums = performer.getInventory().getNumItemsNotCoins();
               if (invnums + 1 >= 100) {
                  performer.getCommunicator().sendNormalServerMessage("You can't make space in your inventory for the filet.");
                  return true;
               }

               int nums;
               int tweight;
               int waste;
               try {
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(369);
                  tweight = template.getWeightGrams();
                  waste = tweight / 10;
                  nums = (target.getWeightGrams() + waste) / (tweight + waste);
                  if (nums == 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too small to produce a filet.");
                     return true;
                  }
               } catch (NoSuchTemplateException var29) {
                  performer.getCommunicator().sendNormalServerMessage("Something went horribly wrong with " + target.getName() + ". Please use /support.");
                  logger.log(Level.WARNING, "No template for filet?" + var29.getMessage(), (Throwable)var29);
                  return true;
               }

               Skill butchering = performer.getSkills().getSkillOrLearn(10059);
               double bonus = 0.0;

               try {
                  Skill primskill = null;
                  int primarySkill = source.getPrimarySkill();

                  try {
                     primskill = performer.getSkills().getSkill(primarySkill);
                  } catch (Exception var26) {
                     primskill = performer.getSkills().learn(primarySkill, 1.0F);
                  }

                  bonus = primskill.skillCheck(10.0, 0.0, false, 1.0F);
               } catch (NoSuchSkillException var27) {
               }

               performer.getCommunicator().sendNormalServerMessage("You filet the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " filets " + target.getNameWithGenus() + ".", performer, 5);
               float max = target.getCurrentQualityLevel();
               float ql = Math.max(1.0F, (float)butchering.skillCheck((double)target.getDamage(), source, bonus, counter > 10.0F, 1.0F));
               boolean destroyed = false;

               try {
                  Item fil = ItemFactory.createItem(369, Math.min(max, ql), null);
                  fil.setName("fillet of " + target.getActualName().toLowerCase());
                  fil.setRealTemplate(target.getTemplateId());
                  fil.setAuxData(target.getAuxData());
                  fil.setIsUnderWeight(false);
                  if (target.getTemperature() > 200) {
                     fil.setTemperature(target.getTemperature());
                  }

                  performer.getInventory().insertItem(fil);
                  target.setIsUnderWeight(true);
                  destroyed = target.setWeight(target.getWeightGrams() - tweight - waste, true);
               } catch (FailedException var24) {
                  logger.log(Level.WARNING, performer.getName() + ":" + var24.getMessage(), (Throwable)var24);
               } catch (NoSuchTemplateException var25) {
                  logger.log(Level.WARNING, "No template for filet?" + var25.getMessage(), (Throwable)var25);
               }

               if (nums == 1) {
                  int rand = Server.rand.nextInt(80 + (target != null ? source.getRarity() * 10 : 0));
                  if (rand > 60 && target.getTemplateId() == 572) {
                     if (invnums + 1 > 100) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("You can't make space in your inventory for the item you found in the " + target.getName() + ".");
                        return true;
                     }

                     try {
                        Item bonusItem = ItemFactory.createItem(752, Math.min(max, ql), null);
                        performer.getInventory().insertItem(bonusItem);
                     } catch (FailedException var22) {
                        logger.log(Level.WARNING, performer.getName() + ":" + var22.getMessage(), (Throwable)var22);
                     } catch (NoSuchTemplateException var23) {
                        logger.log(Level.WARNING, "No template for inkSac?" + var23.getMessage(), (Throwable)var23);
                     }
                  }

                  if (!destroyed) {
                     int rweight = target.getWeightGrams();
                     if (rweight < tweight) {
                        try {
                           Items.destroyItem(target.getWurmId());
                           Item bonusItem = ItemFactory.createItem(1363, Math.min(max, ql), null);
                           performer.getInventory().insertItem(bonusItem);
                        } catch (FailedException var20) {
                           logger.log(Level.WARNING, performer.getName() + ":" + var20.getMessage(), (Throwable)var20);
                        } catch (NoSuchTemplateException var21) {
                           logger.log(Level.WARNING, "No template for inkSac?" + var21.getMessage(), (Throwable)var21);
                        }
                     }
                  }

                  return true;
               }
            }

            return false;
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You mysteriously cut yourself when trying to filet that!");
         CombatEngine.addWound(
            performer, performer, (byte)1, 13, (double)(2000 + Server.rand.nextInt(2000)), 0.0F, "cut", null, 0.0F, 0.0F, false, false, false, false
         );
         return true;
      }
   }

   static final boolean chop(Action act, Creature performer, Item source, Item target, float counter) {
      boolean done = false;

      try {
         if (!source.isWeaponAxe() && source.getTemplateId() != 24) {
            performer.getCommunicator().sendNormalServerMessage("You cannot chop with a " + source.getName() + ".");
            return true;
         }

         String action = source.getTemplateId() != 24 ? "chop" : "saw";
         if (target.getTemplateId() != 385) {
            performer.getCommunicator()
               .sendNormalServerMessage("The " + target.getName() + " is not a huge log. You cannot " + action + " that into to smaller logs.");
            return true;
         }

         if (target.getLastOwnerId() != performer.getWurmId() && !Methods.isActionAllowed(performer, (short)6, target)) {
            return true;
         }

         int time = 200;
         if (counter == 1.0F) {
            float posX = performer.getStatus().getPositionX();
            float posY = performer.getStatus().getPositionY();
            float rot = performer.getStatus().getRotation();
            float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0)));
            float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0))));
            posX += xPosMod;
            posY += yPosMod;
            int placedX = (int)posX >> 2;
            int placedY = (int)posY >> 2;
            VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
            if (t != null && target.getOwnerId() == -10L && t.getNumberOfItems(performer.getFloorLevel()) > 99) {
               performer.getCommunicator().sendNormalServerMessage("There is no space to " + action + " wood here. Clear the area first.");
               return true;
            }

            Skill woodcutting = performer.getSkills().getSkillOrLearn(1007);
            Skill primskill = performer.getSkills().getSkillOrLearn(source.getPrimarySkill());
            performer.getCommunicator().sendNormalServerMessage("You start to " + action + " up the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to " + action + " up a " + target.getName() + ".", performer, 5);
            int treeAge = 5;
            int var40 = (int)((float)Terraforming.calcTime(5, source, primskill, woodcutting) * Actions.getStaminaModiferFor(performer, 20000));
            time = Math.min(65535, var40);
            act.setTimeLeft(time);
            performer.getStatus().modifyStamina(-1000.0F);
            performer.sendActionControl(source.getTemplateId() != 24 ? "Chopping" : "Sawing", true, time);
         } else {
            time = act.getTimeLeft();
            if (act.justTickedSecond() && (time < 50 && act.currentSecond() % 2 == 0 || act.currentSecond() % 5 == 0)) {
               source.setDamage(source.getDamage() + 0.001F * source.getDamageModifier());
               performer.getStatus().modifyStamina(-5000.0F);
            }

            if (act.justTickedSecond() && counter * 10.0F < (float)(time - 30)) {
               if (source.getTemplateId() != 24) {
                  if ((act.currentSecond() - 2) % 4 == 0) {
                     String sstring = "sound.work.woodcutting1";
                     int x = Server.rand.nextInt(3);
                     if (x == 0) {
                        sstring = "sound.work.woodcutting2";
                     } else if (x == 1) {
                        sstring = "sound.work.woodcutting3";
                     }

                     SoundPlayer.playSound(sstring, target, 1.0F);
                  }
               } else if ((act.currentSecond() - 2) % 5 == 0 && counter * 10.0F < (float)(time - 50)) {
                  String sstring = "sound.work.carpentry.saw";
                  SoundPlayer.playSound("sound.work.carpentry.saw", target, 1.0F);
               }
            }
         }

         if (counter * 10.0F >= (float)time) {
            if (act.getRarity() != 0) {
               performer.playPersonalSound("sound.fx.drumroll");
            }

            if (source.getTemplateId() != 24) {
               String sstring = "sound.work.woodcutting1";
               int x = Server.rand.nextInt(3);
               if (x == 0) {
                  sstring = "sound.work.woodcutting2";
               } else if (x == 1) {
                  sstring = "sound.work.woodcutting3";
               }

               SoundPlayer.playSound(sstring, target, 1.0F);
            }

            float posX = performer.getStatus().getPositionX();
            float posY = performer.getStatus().getPositionY();
            float rot = performer.getStatus().getRotation();
            float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * 2.0F;
            float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * 2.0F;
            posX += xPosMod;
            posY += yPosMod;
            int placedX = (int)posX >> 2;
            int placedY = (int)posY >> 2;
            VolaTile t = Zones.getTileOrNull(placedX, placedY, performer.isOnSurface());
            if (t != null && target.getOwnerId() == -10L && t.getNumberOfItems(performer.getFloorLevel()) > 99) {
               performer.getCommunicator().sendNormalServerMessage("There is no space to " + action + " wood here. Clear the area first.");
               return true;
            }

            float tickCounter = counter;
            TreeData.TreeType tType = Materials.getTreeTypeForWood(target.getMaterial());
            double difficulty = (double)tType.getDifficulty();
            Skill woodcutting = performer.getSkills().getSkillOrLearn(1007);

            try {
               int primarySkill = source.getPrimarySkill();
               Skill primskill = performer.getSkills().getSkillOrLearn(primarySkill);
               if (primskill.getKnowledge() < 20.0 || primarySkill == 10003 || primarySkill == 10008) {
                  primskill.skillCheck(difficulty, source, 0.0, false, tickCounter);
               }
            } catch (NoSuchSkillException var37) {
               logger.log(Level.WARNING, "No primary skill for " + source.getName());
            }

            done = true;
            int nums = 1;

            try {
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(9);
               nums = target.getWeightGrams() / template.getWeightGrams();
               if (nums == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is too small to produce a log.");
                  target.setTemplateId(9);
                  done = true;
                  if (performer.getTutorialLevel() == 3 && !performer.skippedTutorial()) {
                     performer.missionFinished(true, true);
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You create a smaller log from the " + target.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " creates a smaller log from the " + target.getName() + ".", performer, 5);
                  double power = woodcutting.skillCheck(difficulty, source, 0.0, false, tickCounter);
                  double cappedPower = Math.min(power, (double)target.getCurrentQualityLevel());
                  float ql = GeneralUtilities.calcRareQuality(cappedPower, act.getRarity(), source.getRarity(), target.getRarity());

                  try {
                     Item log = ItemFactory.createItem(9, ql, target.getMaterial(), act.getRarity(), null);
                     if (target.getOwnerId() != -10L) {
                        performer.getInventory().insertItem(log);
                     } else {
                        log.putItemInfrontof(performer);
                     }

                     log.setLastOwnerId(performer.getWurmId());
                     target.setWeight(target.getWeightGrams() - log.getWeightGrams(), true);
                     if (!target.deleted && target.getWeightGrams() <= log.getTemplate().getWeightGrams()) {
                        if (target.getWeightGrams() < 1000) {
                           Items.destroyItem(target.getWurmId());
                        } else {
                           Item spareLog = ItemFactory.createItem(9, target.getCurrentQualityLevel(), target.getMaterial(), target.getRarity(), null);
                           spareLog.setWeight(target.getWeightGrams(), false);
                           Items.destroyItem(target.getWurmId());
                           if (log.getParentId() == performer.getInventory().getWurmId()) {
                              performer.getInventory().insertItem(spareLog);
                           } else {
                              spareLog.putItemInfrontof(performer);
                           }
                        }
                     }

                     if (performer.getTutorialLevel() == 3 && !performer.skippedTutorial()) {
                        performer.missionFinished(true, true);
                     }
                  } catch (FailedException var30) {
                     logger.log(Level.WARNING, performer.getName() + ":" + var30.getMessage(), (Throwable)var30);
                  } catch (NoSuchTemplateException var31) {
                     logger.log(Level.WARNING, "No template for log?" + var31.getMessage(), (Throwable)var31);
                  } catch (NoSuchItemException var32) {
                     logger.log(Level.WARNING, performer.getName() + " no such item?", (Throwable)var32);
                  } catch (NoSuchPlayerException var33) {
                     logger.log(Level.WARNING, performer.getName() + " no such player?", (Throwable)var33);
                  } catch (NoSuchCreatureException var34) {
                     logger.log(Level.WARNING, performer.getName() + " no such creature?", (Throwable)var34);
                  } catch (NoSuchZoneException var35) {
                     logger.log(Level.WARNING, performer.getName() + " no such zone?", (Throwable)var35);
                  }
               }
            } catch (NoSuchTemplateException var36) {
               logger.log(Level.WARNING, "No template for log?" + var36.getMessage(), (Throwable)var36);
            }

            PlayerTutorial.firePlayerTrigger(performer.getWurmId(), PlayerTutorial.PlayerTrigger.CREATE_LOG);
         }
      } catch (NoSuchSkillException var38) {
         logger.log(Level.WARNING, var38.getMessage(), (Throwable)var38);
         done = true;
      }

      return done;
   }

   public static final byte getNewCreationState(byte material) {
      if (Materials.isWood(material)) {
         return (byte)Server.rand.nextInt(5);
      } else if (Materials.isMetal(material)) {
         return (byte)Server.rand.nextInt(5);
      } else if (Materials.isLeather(material)) {
         return (byte)Server.rand.nextInt(5);
      } else if (Materials.isCloth(material)) {
         return (byte)Server.rand.nextInt(5);
      } else if (Materials.isClay(material)) {
         return (byte)Server.rand.nextInt(5);
      } else {
         return Materials.isStone(material) ? (byte)Server.rand.nextInt(5) : 0;
      }
   }

   public static final byte getImproveMaterial(Item item) {
      if (!item.isImproveUsingTypeAsMaterial()) {
         return item.getMaterial();
      } else {
         return item.getTemplate().isCloth() && item.getMaterial() != 69 ? 17 : item.getMaterial();
      }
   }

   public static final int getItemForImprovement(byte material, byte state) {
      int template = -10;
      if (Materials.isWood(material)) {
         switch(state) {
            case 1:
               template = 8;
               break;
            case 2:
               template = 63;
               break;
            case 3:
               template = 388;
               break;
            case 4:
               template = 313;
               break;
            default:
               template = -10;
         }
      } else if (Materials.isMetal(material)) {
         switch(state) {
            case 1:
               template = 296;
               break;
            case 2:
               template = 62;
               break;
            case 3:
               template = 128;
               break;
            case 4:
               template = 313;
               break;
            default:
               template = -10;
         }
      } else if (Materials.isLeather(material)) {
         switch(state) {
            case 1:
               template = 215;
               break;
            case 2:
               template = 390;
               break;
            case 3:
               template = 392;
               break;
            case 4:
               template = 63;
               break;
            default:
               template = -10;
         }
      } else if (Materials.isCloth(material)) {
         switch(state) {
            case 1:
               template = 215;
               break;
            case 2:
               template = 394;
               break;
            case 3:
               template = 128;
               break;
            case 4:
               template = 215;
               break;
            default:
               template = -10;
         }
      } else if (Materials.isStone(material)) {
         switch(state) {
            case 1:
               template = 97;
               break;
            case 2:
               template = 97;
               break;
            case 3:
               template = 97;
               break;
            case 4:
               template = 97;
               break;
            default:
               template = -10;
         }
      } else if (Materials.isClay(material)) {
         switch(state) {
            case 1:
               template = 14;
               break;
            case 2:
               template = 128;
               break;
            case 3:
               template = 396;
               break;
            case 4:
               template = 397;
               break;
            default:
               template = -10;
         }
      }

      return template;
   }

   public static final String getNeededCreationAction(byte material, byte state, Item item) {
      String todo = "";
      String fstring = "improve";
      if (item.getTemplateId() == 386) {
         fstring = "finish";
      }

      if (Materials.isWood(material)) {
         switch(state) {
            case 1:
               todo = "You notice some notches you must carve away in order to " + fstring + " the " + item.getName() + ".";
               break;
            case 2:
               todo = "You must use a mallet on the " + item.getName() + " in order to " + fstring + " it.";
               break;
            case 3:
               todo = "You must use a file to smooth out the " + item.getName() + " in order to " + fstring + " it.";
               break;
            case 4:
               todo = "You will want to polish the " + item.getName() + " with a pelt to " + fstring + " it.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isMetal(material)) {
         switch(state) {
            case 1:
               todo = "The " + item.getName() + (item.isNamePlural() ? " need" : " needs") + " to be sharpened with a whetstone.";
               break;
            case 2:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some dents that must be flattened by a hammer.";
               break;
            case 3:
               todo = "You need to temper the " + item.getName() + " by dipping it in water while it's hot.";
               break;
            case 4:
               todo = "You need to polish the " + item.getName() + " with a pelt.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isLeather(material)) {
         switch(state) {
            case 1:
               todo = "The "
                  + item.getName()
                  + (item.isNamePlural() ? " have" : " has")
                  + " some holes and must be tailored with an iron needle to "
                  + fstring
                  + ".";
               break;
            case 2:
               todo = "The " + item.getName() + (item.isNamePlural() ? " need" : " needs") + " some holes punched with an awl.";
               break;
            case 3:
               todo = "The "
                  + item.getName()
                  + (item.isNamePlural() ? " have" : " has")
                  + " some excess leather that needs to be cut away with a leather knife.";
               break;
            case 4:
               todo = "A mallet must be used on the " + item.getName() + " in order to smooth out a quirk.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isCloth(material)) {
         switch(state) {
            case 1:
               todo = "The "
                  + item.getName()
                  + (item.isNamePlural() ? " have" : " has")
                  + " an open seam that must be backstitched with an iron needle to "
                  + fstring
                  + ".";
               break;
            case 2:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some excess cloth that needs to be cut away with a scissors.";
               break;
            case 3:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some stains that must be washed away.";
               break;
            case 4:
               todo = "The "
                  + item.getName()
                  + (item.isNamePlural() ? " have" : " has")
                  + " a seam that needs to be hidden by slipstitching with an iron needle.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isStone(material)) {
         switch(state) {
            case 1:
            case 2:
            case 3:
            case 4:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some irregularities that must be removed with a stone chisel.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isClay(material)) {
         switch(state) {
            case 1:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be removed by hand.";
               break;
            case 2:
               todo = "The " + item.getName() + " needs water.";
               break;
            case 3:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be fixed with a clay shaper.";
               break;
            case 4:
               todo = "The " + item.getName() + (item.isNamePlural() ? " have" : " has") + " some flaws that must be fixed with a spatula.";
               break;
            default:
               todo = "";
         }
      }

      return todo;
   }

   public String getCreationActionString(byte material, byte state, Item used, Item item) {
      String todo = "";
      if (Materials.isWood(material)) {
         switch(state) {
            case 1:
               todo = "You carve on the " + item.getName() + ".";
               break;
            case 2:
               todo = "You hammer on the " + item.getName() + ".";
               break;
            case 3:
               todo = "You file on the " + item.getName() + " meticulously.";
               break;
            case 4:
               todo = "You polish the " + item.getName() + " carefully.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isMetal(material)) {
         switch(state) {
            case 1:
               todo = "You sharpen the " + item.getName() + ".";
               break;
            case 2:
               todo = "You hammer on the " + item.getName() + ".";
               break;
            case 3:
               todo = "You dip the " + item.getName() + " in the water.";
               break;
            case 4:
               todo = "You polish the " + item.getName() + " carefully.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isLeather(material)) {
         switch(state) {
            case 1:
               todo = "You sew the " + item.getName() + ".";
               break;
            case 2:
               todo = "You punch some holes in the " + item.getName() + ".";
               break;
            case 3:
               todo = "You cut away some excess leather from the " + item.getName() + ".";
               break;
            case 4:
               todo = "You hammer the " + item.getName() + " and flatten a bulge.";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isCloth(material)) {
         switch(state) {
            case 1:
               todo = "You backstitch the " + item.getName() + " elegantly.";
               break;
            case 2:
               todo = "You cut away some excess cloth from the " + item.getName() + ".";
               break;
            case 3:
               todo = "You wash the " + item.getName() + ".";
               break;
            case 4:
               todo = "You nimbly slipstitch the " + item.getName() + ".";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isStone(material)) {
         switch(state) {
            case 1:
               todo = "You carefully chip away some rock from the " + item.getName() + ".";
               break;
            case 2:
               todo = "You carefully chip away some rock from the " + item.getName() + ".";
               break;
            case 3:
               todo = "You carefully chip away some rock from the " + item.getName() + ".";
               break;
            case 4:
               todo = "You carefully chip away some rock from the " + item.getName() + ".";
               break;
            default:
               todo = "";
         }
      } else if (Materials.isClay(material)) {
         switch(state) {
            case 1:
               todo = "You skillfully fix some irregularities in the " + item.getName() + ".";
               break;
            case 2:
               todo = "You add some water to the " + item.getName() + ".";
               break;
            case 3:
               todo = "Meticulously you use the " + used.getName() + " to create the desired form of the " + item.getName() + ".";
               break;
            case 4:
               todo = "You carefully use the " + used.getName() + " to remove some unnecessary clay from the " + item.getName() + ".";
               break;
            default:
               todo = "";
         }
      }

      return todo;
   }

   private static final String getImproveActionString(byte material, byte state) {
      String todo = "fixing";
      if (Materials.isWood(material)) {
         switch(state) {
            case 1:
               todo = "carving";
               break;
            case 2:
               todo = "hammering";
               break;
            case 3:
               todo = "filing";
               break;
            case 4:
               todo = "polishing";
               break;
            default:
               todo = "fixing";
         }
      } else if (Materials.isMetal(material)) {
         switch(state) {
            case 1:
               todo = "sharpening";
               break;
            case 2:
               todo = "hammering";
               break;
            case 3:
               todo = "tempering";
               break;
            case 4:
               todo = "polishing";
               break;
            default:
               todo = "fixing";
         }
      } else if (Materials.isLeather(material)) {
         switch(state) {
            case 1:
               todo = "sewing";
               break;
            case 2:
               todo = "punching";
               break;
            case 3:
               todo = "cutting";
               break;
            case 4:
               todo = "hammering";
               break;
            default:
               todo = "fixing";
         }
      } else if (Materials.isCloth(material)) {
         switch(state) {
            case 1:
               todo = "backstitching";
               break;
            case 2:
               todo = "cutting";
               break;
            case 3:
               todo = "washing";
               break;
            case 4:
               todo = "slipstitching";
               break;
            default:
               todo = "fixing";
         }
      } else if (Materials.isStone(material)) {
         switch(state) {
            case 1:
               todo = "chipping";
               break;
            case 2:
               todo = "chipping";
               break;
            case 3:
               todo = "chipping";
               break;
            case 4:
               todo = "chipping";
               break;
            default:
               todo = "fixing";
         }
      } else if (Materials.isClay(material)) {
         switch(state) {
            case 1:
               todo = "molding";
               break;
            case 2:
               todo = "watering";
               break;
            case 3:
               todo = "molding";
               break;
            case 4:
               todo = "molding";
               break;
            default:
               todo = "fixing";
         }
      }

      return todo;
   }

   static final String getImproveAction(byte material, byte state) {
      String todo = "Fix";
      if (Materials.isWood(material)) {
         switch(state) {
            case 1:
               todo = "Carve";
               break;
            case 2:
               todo = "Hammer";
               break;
            case 3:
               todo = "File";
               break;
            case 4:
               todo = "Polish";
               break;
            default:
               todo = "Fix";
         }
      } else if (Materials.isMetal(material)) {
         switch(state) {
            case 1:
               todo = "Sharpen";
               break;
            case 2:
               todo = "Hammer";
               break;
            case 3:
               todo = "Temper";
               break;
            case 4:
               todo = "Polish";
               break;
            default:
               todo = "Fix";
         }
      } else if (Materials.isLeather(material)) {
         switch(state) {
            case 1:
               todo = "Sew";
               break;
            case 2:
               todo = "Punch";
               break;
            case 3:
               todo = "Cut";
               break;
            case 4:
               todo = "Hammer";
               break;
            default:
               todo = "Fix";
         }
      } else if (Materials.isCloth(material)) {
         switch(state) {
            case 1:
               todo = "Backstitch";
               break;
            case 2:
               todo = "Cut";
               break;
            case 3:
               todo = "Wash";
               break;
            case 4:
               todo = "Slipstitch";
               break;
            default:
               todo = "Fix";
         }
      } else if (Materials.isStone(material)) {
         switch(state) {
            case 1:
               todo = "Chip";
               break;
            case 2:
               todo = "Chip";
               break;
            case 3:
               todo = "Chip";
               break;
            case 4:
               todo = "Chip";
               break;
            default:
               todo = "Fix";
         }
      } else if (Materials.isClay(material)) {
         switch(state) {
            case 1:
               todo = "Mold";
               break;
            case 2:
               todo = "Water";
               break;
            case 3:
               todo = "Mold";
               break;
            case 4:
               todo = "Mold";
               break;
            default:
               todo = "Fix";
         }
      }

      return todo;
   }

   public static final String getRarityDesc(byte rarity) {
      switch(rarity) {
         case 1:
            return " This is a very rare and interesting version of the item.";
         case 2:
            return " This is a supreme example of the item, with fine details and slick design.";
         case 3:
            return " This is a fantastic example of the item, with fascinating design details and perfect ideas for functionality.";
         default:
            return "";
      }
   }

   public static final String getRarityName(byte rarity) {
      switch(rarity) {
         case 1:
            return "rare";
         case 2:
            return "supreme";
         case 3:
            return "fantastic";
         default:
            return "";
      }
   }

   private static final int getTemperWaterAmountFor(Item target) {
      if (target.getWeightGrams() > 10000) {
         return 1000;
      } else {
         return target.getWeightGrams() < 2000 ? 200 : target.getWeightGrams() / 10;
      }
   }

   public static final boolean polishItem(Action act, Creature performer, Item source, Item target, float counter) {
      byte state = target.creationState;
      boolean improving = act.getNumber() == 192;
      if (counter == 0.0F || counter == 1.0F || act.justTickedSecond()) {
         if (state == 0 || target.isNewbieItem() || target.isChallengeNewbieItem()) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " does not need that.");
            return true;
         }

         if (source.getWurmId() == target.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("You cannot improve the " + source.getName() + " using itself as a tool.");
            return true;
         }

         int templateId = getItemForImprovement(getImproveMaterial(target), state);
         if (templateId != source.getTemplateId()) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " does not need the touch of " + source.getNameWithGenus() + ".");
            return true;
         }

         if (templateId == 128 && (target.isMetal() || target.isCloth()) && source.getWeightGrams() < getTemperWaterAmountFor(target)) {
            if (target.isCloth()) {
               performer.getCommunicator().sendNormalServerMessage("You need more water in order to wash the " + target.getName() + ".");
            } else {
               performer.getCommunicator().sendNormalServerMessage("You need more water in order to cool the " + target.getName() + ".");
            }

            return true;
         }

         if (target.getParentId() != -10L && target.getTemplateId() == 386) {
            try {
               ItemTemplate temp = target.getRealTemplate();
               if (temp != null && !temp.isVehicle()) {
                  Item parent = target.getParent();
                  if (parent.isNoWorkParent()) {
                     performer.getCommunicator().sendNormalServerMessage("You can't work with the " + target.getName() + " in the " + parent.getName() + ".");
                     throw new NoSuchItemException("The " + target.getName() + " can't be modified in the " + parent.getName() + ".");
                  }

                  if ((
                        parent.getContainerSizeX() < temp.getSizeX()
                           || parent.getContainerSizeY() < temp.getSizeY()
                           || parent.getContainerSizeZ() <= temp.getSizeZ()
                     )
                     && parent.getTemplateId() != 177
                     && parent.getTemplateId() != 0) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("It's too tight to try and work on the " + target.getName() + " in the " + parent.getName() + ".");
                     return true;
                  }
               }
            } catch (NoSuchItemException var41) {
            }
         }
      }

      Skills skills = performer.getSkills();
      Skill improve = null;
      int skillNum = -10;
      if (improving) {
         skillNum = getImproveSkill(target);
      } else {
         skillNum = getImproveSkill(target.getMaterial(), target.realTemplate);
      }

      if (skillNum == -10) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " can not be improved right now.");
         return true;
      } else {
         int time = 1000;
         boolean insta = performer.getPower() >= 5;

         try {
            improve = skills.getSkill(skillNum);
         } catch (NoSuchSkillException var40) {
            improve = skills.learn(skillNum, 1.0F);
         }

         if (!insta && target.getDamage() > 0.0F) {
            performer.getCommunicator()
               .sendNormalServerMessage("Repair the " + target.getName() + " before you try to " + (target.isUnfinished() ? "finish" : "improve") + " it.");
            return true;
         } else {
            double power = 0.0;
            double bonus = 0.0;
            if (performer.isPriest()) {
               bonus = -20.0;
            }

            Skill secondarySkill = null;

            try {
               secondarySkill = skills.getSkill(source.getPrimarySkill());
            } catch (Exception var39) {
               try {
                  secondarySkill = skills.learn(source.getPrimarySkill(), 1.0F);
               } catch (Exception var38) {
               }
            }

            float runeModifier = 1.0F;
            if (target.getSpellEffects() != null) {
               runeModifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
            }

            float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
            double improveBonus = 0.23047 * (double)imbueEnhancement * (double)runeModifier;
            double max = improve.getKnowledge(0.0) + (100.0 - improve.getKnowledge(0.0)) * improveBonus;
            double diff = Math.max(0.0, max - (double)target.getQualityLevel());
            float skillgainMod = 1.0F;
            if (diff <= 0.0) {
               skillgainMod = 2.0F;
            }

            if (counter == 1.0F) {
               if (!insta && target.isMetal() && !target.isNoTake() && target.getTemperature() < 3500) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " needs to be glowing hot to be improved.");
                  return true;
               }

               String improvestring = getImproveActionString(getImproveMaterial(target), state);
               performer.getCommunicator().sendNormalServerMessage("You start " + improvestring + " the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts " + improvestring + " " + target.getNameWithGenus() + ".", performer, 5);
               time = Actions.getImproveActionTime(performer, source);
               performer.sendActionControl(improvestring, true, time);
               act.setTimeLeft(time);
               double impmod = 0.5;
               if (improving) {
                  impmod = 1.0;
               }

               double mod = impmod
                  * (double)(
                     (100.0F - target.getQualityLevel())
                        / 20.0F
                        / 100.0F
                        * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat())
                        / 2.0F
                  );
               if (improving) {
                  power = improve.skillCheck((double)target.getQualityLevel(), source, bonus, true, 1.0F);
                  if (power < 0.0) {
                     act.setFailSecond((float)((int)Math.max(20.0F, (float)time * Server.rand.nextFloat())));
                     act.setPower((float)(-mod * Math.max(1.0, diff)));
                  } else {
                     if (diff <= 0.0) {
                        mod *= 0.01F;
                     }

                     double regain = 1.0;
                     if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
                        regain = 2.0;
                     }

                     diff *= regain;
                     int tid = target.getTemplateId();
                     if (target.isArmour()
                        || target.isWeapon()
                        || target.isShield()
                        || tid == 455
                        || tid == 454
                        || tid == 456
                        || tid == 453
                        || tid == 451
                        || tid == 452) {
                        mod *= 2.0;
                     }

                     Titles.Title title = performer.getTitle();
                     if (title != null && title.getSkillId() == improve.getNumber() && (target.isArmour() || target.isCreatureWearableOnly())) {
                        mod *= 1.3F;
                     }

                     act.setPower((float)(mod * Math.max(1.0, diff)));
                  }
               } else {
                  double regain = 1.0;
                  if (target.getQualityLevel() < target.getOriginalQualityLevel()) {
                     regain = 2.0;
                  }

                  diff *= regain;
                  int tid = target.getTemplateId();
                  if (target.isArmour()
                     || target.isCreatureWearableOnly()
                     || target.isWeapon()
                     || target.isShield()
                     || tid == 455
                     || tid == 454
                     || tid == 456
                     || tid == 453
                     || tid == 451
                     || tid == 452) {
                     mod *= 2.0;
                  }

                  Titles.Title title = performer.getTitle();
                  if (title != null && title.getSkillId() == improve.getNumber() && (target.isArmour() || target.isCreatureWearableOnly())) {
                     mod *= 1.3F;
                  }

                  act.setPower((float)(mod * Math.max(1.0, diff)));
               }
            } else {
               time = act.getTimeLeft();
               float failsec = act.getFailSecond();
               power = (double)act.getPower();
               if (counter >= failsec) {
                  if (secondarySkill != null) {
                     bonus = Math.max(
                        bonus,
                        secondarySkill.skillCheck(
                           (double)target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 3.0F : counter / 2.0F
                        )
                     );
                  }

                  if (performer.isPriest()) {
                     bonus = Math.min(bonus, 0.0);
                  }

                  improve.skillCheck((double)target.getQualityLevel(), source, bonus, false, performer.isPriest() ? counter / 2.0F : counter);
                  if (power != 0.0) {
                     if (!target.isBodyPart()) {
                        if (!target.isLiquid()) {
                           target.setDamage(target.getDamage() - act.getPower());
                           performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + " a little.");
                           Server.getInstance()
                              .broadCastAction(
                                 performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + target.getNameWithGenus() + " a little.",
                                 performer,
                                 5
                              );
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("You fail.");
                           Server.getInstance().broadCastAction(performer.getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + target.getName() + " and stop.");
                     Server.getInstance().broadCastAction(performer.getName() + " stops improving " + target.getNameWithGenus() + ".", performer, 5);
                  }

                  performer.getStatus().modifyStamina(-counter * 1000.0F);
                  return true;
               }
            }

            if (act.mayPlaySound()) {
               sendImproveSound(performer, source, target, skillNum);
            }

            if (!(counter * 10.0F > (float)time) && !insta) {
               return false;
            } else {
               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }

               float maxGain = 1.0F;
               if (source.isLiquid() && (target.isMetal() || target.isUnfired() || target.isCloth() || Materials.isCloth(getImproveMaterial(target)))) {
                  if (source.getTemplateId() == 128) {
                     source.setWeight(source.getWeightGrams() - getTemperWaterAmountFor(target), true);
                  } else {
                     int usedWeight = (int)Math.min(500.0, Math.max(1.0, (double)target.getWeightGrams() * 0.05));
                     if (source.getWeightGrams() < usedWeight) {
                        maxGain = Math.min(1.0F, (float)source.getWeightGrams() / (float)usedWeight);
                     }

                     source.setWeight(source.getWeightGrams() - usedWeight, true);
                  }
               } else if (!source.isLiquid()) {
                  source.setDamage(source.getDamage() + 0.0015F * source.getDamageModifier());
               }

               if (secondarySkill != null) {
                  bonus = Math.max(
                     bonus,
                     secondarySkill.skillCheck(
                        (double)target.getQualityLevel(), source, bonus, false, skillgainMod * (performer.isPriest() ? counter / 3.0F : counter / 2.0F)
                     )
                  );
               }

               if (performer.isPriest()) {
                  bonus = Math.min(bonus, 0.0);
               }

               improve.skillCheck((double)target.getQualityLevel(), source, bonus, false, skillgainMod * (performer.isPriest() ? counter / 2.0F : counter));
               String improvestring = getImproveActionString(getImproveMaterial(target), state);
               Server.getInstance().broadCastAction(performer.getName() + " ceases " + improvestring + " " + target.getNameWithGenus() + ".", performer, 5);
               power = (double)act.getPower();
               if (power > 0.0) {
                  byte rarity = target.getRarity();
                  float rarityChance = 0.2F;
                  if (target.getSpellEffects() != null) {
                     rarityChance *= target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
                  }

                  if (act.getRarity() > rarity && Server.rand.nextFloat() <= rarityChance) {
                     rarity = act.getRarity();
                  }

                  byte newState = getNewCreationState(getImproveMaterial(target));

                  while(newState != 0 && newState == state) {
                     newState = getNewCreationState(getImproveMaterial(target));
                     int temp = getItemForImprovement(getImproveMaterial(target), newState);
                     if (temp == target.getTemplateId()) {
                        --newState;
                     }
                  }

                  target.setCreationState(newState);
                  Item toRarify = target;
                  if (target.getTemplateId() == 128) {
                     toRarify = source;
                  }

                  if (rarity > toRarify.getRarity()) {
                     toRarify.setRarity(rarity);

                     for(Item sub : toRarify.getItems()) {
                        if (sub != null && sub.isComponentItem()) {
                           sub.setRarity(rarity);
                        }
                     }

                     if (toRarify.getRarity() > 2) {
                        performer.achievement(300);
                     } else if (toRarify.getRarity() == 1) {
                        performer.achievement(301);
                     } else if (toRarify.getRarity() == 2) {
                        performer.achievement(302);
                     }
                  }

                  if (newState != 0) {
                     String newString = getNeededCreationAction(getImproveMaterial(target), newState, target);
                     performer.getCommunicator().sendNormalServerMessage(newString);
                  } else if (target.getTemplateId() == 386) {
                     MissionTriggers.activateTriggers(performer, target, 148, 0L, 1);
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is finished!");
                     target.setTemplateId(target.realTemplate);
                     if (target.isUseOnGroundOnly() && target.getOwnerId() != performer.getWurmId()) {
                        try {
                           target.putItemInfrontof(performer);
                        } catch (Exception var37) {
                           logger.log(Level.INFO, performer.getName() + ": " + var37.getMessage());
                        }
                     }

                     skillNum = getImproveSkill(target);
                     int templateId = getImproveTemplateId(target);
                     if (skillNum != -10) {
                        try {
                           ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                           performer.getCommunicator()
                              .sendNormalServerMessage("The " + target.getName() + " could be improved with " + temp.getNameWithGenus() + ".");
                        } catch (NoSuchTemplateException var36) {
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You improve the " + target.getName() + " a bit.");
                     skillNum = getImproveSkill(target);
                     int templateId = getImproveTemplateId(target);
                     if (skillNum != -10) {
                        try {
                           ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                           performer.getCommunicator()
                              .sendNormalServerMessage("The " + target.getName() + " could be improved with " + temp.getNameWithGenus() + ".");
                        } catch (NoSuchTemplateException var35) {
                        }
                     }
                  }

                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "before: "
                              + target.getQualityLevel()
                              + " now: "
                              + ((double)target.getQualityLevel() + power * (double)maxGain)
                              + " power="
                              + power
                              + " maxGain="
                              + maxGain
                        );
                  }

                  float oldQL = target.getQualityLevel();
                  boolean wasHighest = Items.isHighestQLForTemplate(target.getTemplateId(), target.getQualityLevel(), target.getWurmId(), true);
                  float modifier = 1.0F;
                  if (target.getSpellEffects() != null) {
                     modifier = target.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
                  }

                  modifier *= target.getMaterialImpBonus();
                  target.setQualityLevel(Math.min(100.0F, (float)((double)target.getQualityLevel() + power * (double)maxGain * (double)modifier)));
                  if (target.getQualityLevel() > target.getOriginalQualityLevel()) {
                     target.setOriginalQualityLevel(target.getQualityLevel());
                     triggerImproveAchievements(performer, target, improve, wasHighest, oldQL);
                  }
               } else {
                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Dam before: " + target.getDamage() + " now: " + ((double)target.getDamage() - power) + " power=" + power);
                  }

                  if (!target.isBodyPart()) {
                     if (!target.isLiquid()) {
                        target.setDamage(target.getDamage() - (float)power);
                        performer.getCommunicator().sendNormalServerMessage("You damage the " + target.getName() + " a little.");
                        Server.getInstance()
                           .broadCastAction(
                              performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + target.getNameWithGenus() + " a little.",
                              performer,
                              5
                           );
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You fail.");
                        Server.getInstance().broadCastAction(performer.getName() + " grunts as " + performer.getHeSheItString() + " fails.", performer, 5);
                     }
                  }
               }

               performer.getStatus().modifyStamina(-counter * 1000.0F);
               return true;
            }
         }
      }
   }

   static final boolean temper(Action act, Creature performer, Item source, Item target, float counter) {
      byte state = source.creationState;
      boolean improving = act.getNumber() == 192;
      boolean insta = performer.getPower() >= 5;
      if (counter == 0.0F || counter == 1.0F || act.justTickedSecond()) {
         if (state == 0 || source.isNewbieItem() || source.isChallengeNewbieItem()) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " does not need tempering right now.");
            return true;
         }

         int templateId = getItemForImprovement(source.getMaterial(), state);
         if (templateId != target.getTemplateId()) {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " does not need tempering right now.");
            return true;
         }

         if (!insta && source.getDamage() > 0.0F) {
            performer.getCommunicator().sendNormalServerMessage("Repair the " + source.getName() + " before you temper it.");
            return true;
         }

         if (target.getWeightGrams() < getTemperWaterAmountFor(source)) {
            performer.getCommunicator().sendNormalServerMessage("You need more water in order to cool the " + source.getName() + ".");
            return true;
         }

         if (source.getParentId() != -10L && source.getTemplateId() == 386) {
            try {
               ItemTemplate temp = source.getRealTemplate();
               if (temp != null && !temp.isVehicle()) {
                  Item parent = source.getParent();
                  if (parent.isNoWorkParent()) {
                     performer.getCommunicator().sendNormalServerMessage("You can't work with the " + source.getName() + " in the " + parent.getName() + ".");
                     throw new NoSuchItemException("The " + source.getName() + " can't be modified in the " + parent.getName() + ".");
                  }

                  if ((
                        parent.getContainerSizeX() < temp.getSizeX()
                           || parent.getContainerSizeY() < temp.getSizeY()
                           || parent.getContainerSizeZ() <= temp.getSizeZ()
                     )
                     && parent.getTemplateId() != 177
                     && parent.getTemplateId() != 0) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("It's too tight to try and work on the " + source.getName() + " in the " + parent.getName() + ".");
                     return true;
                  }
               }
            } catch (NoSuchItemException var36) {
            }
         }
      }

      Skills skills = performer.getSkills();
      Skill improve = null;
      int skillNum = -10;
      if (improving) {
         skillNum = getImproveSkill(source);
      } else {
         skillNum = getImproveSkill(source.getMaterial(), source.realTemplate);
      }

      if (skillNum == -10) {
         performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " can not be tempered with that.");
         return true;
      } else {
         int time = 1000;

         try {
            improve = skills.getSkill(skillNum);
         } catch (NoSuchSkillException var35) {
            improve = skills.learn(skillNum, 1.0F);
         }

         double power = 0.0;
         double bonus = 0.0;
         if (performer.isPriest()) {
            bonus -= 10.0;
         }

         float runeModifier = 1.0F;
         if (source.getSpellEffects() != null) {
            runeModifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPPERCENT);
         }

         float imbueEnhancement = 1.0F + source.getSkillSpellImprovement(skillNum) / 100.0F;
         double improveBonus = 0.23047 * (double)imbueEnhancement * (double)runeModifier;
         double max = improve.getKnowledge(0.0) + (100.0 - improve.getKnowledge(0.0)) * improveBonus;
         double diff = Math.max(0.0, max - (double)source.getQualityLevel());
         float skillgainMod = 1.0F;
         if (diff <= 0.0) {
            skillgainMod = 2.0F;
         }

         if (counter == 1.0F) {
            if (!insta && source.isMetal() && source.getTemperature() < 3500 && !source.isNoTake()) {
               performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " needs to be glowing hot to be tempered.");
               return true;
            }

            if (improving) {
            }

            String improvestring = getImproveActionString(getImproveMaterial(source), state);
            performer.getCommunicator().sendNormalServerMessage("You start " + improvestring + " the " + source.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts " + improvestring + " " + source.getNameWithGenus() + ".", performer, 5);
            time = Actions.getImproveActionTime(performer, source);
            performer.sendActionControl(improvestring, true, time);
            act.setTimeLeft(time);
            double impmod = 0.5;
            if (improving) {
               impmod = 1.0;
            }

            double mod = impmod
               * (double)(
                  (100.0F - source.getQualityLevel())
                     / 20.0F
                     / 100.0F
                     * (Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat() + Server.rand.nextFloat())
                     / 2.0F
               );
            if (improving) {
               power = improve.skillCheck((double)source.getQualityLevel(), bonus, true, 1.0F);
               if (power < 0.0) {
                  act.setFailSecond((float)((int)Math.max(20.0F, (float)time * Server.rand.nextFloat())));
                  act.setPower((float)(-mod * Math.max(1.0, diff)));
               } else {
                  if (diff <= 0.0) {
                     mod *= 0.01F;
                  }

                  double regain = 1.0;
                  if (source.getQualityLevel() < source.getOriginalQualityLevel()) {
                     regain = 2.0;
                  }

                  diff *= regain;
                  int tid = source.getTemplateId();
                  if (tid == 455 || tid == 454 || tid == 456 || tid == 453 || tid == 451 || tid == 452) {
                     mod *= 2.0;
                  }

                  act.setPower((float)(mod * Math.max(1.0, diff)));
               }
            } else {
               double regain = 1.0;
               if (source.getQualityLevel() < source.getOriginalQualityLevel()) {
                  regain = 5.0;
               }

               diff = Math.min(diff, 10.0 * regain);
               int tid = source.getTemplateId();
               if (tid == 455 || tid == 454 || tid == 456 || tid == 453 || tid == 451 || tid == 452) {
                  mod *= 3.0;
               }

               act.setPower((float)(mod * Math.max(1.0, diff)));
            }
         } else {
            time = act.getTimeLeft();
            float failsec = act.getFailSecond();
            power = (double)act.getPower();
            if (counter >= failsec) {
               improve.skillCheck((double)source.getQualityLevel(), bonus, false, performer.isPriest() ? counter / 2.0F : counter);
               if (power != 0.0) {
                  source.setDamage(source.getDamage() - act.getPower());
                  performer.getCommunicator().sendNormalServerMessage("You damage the " + source.getName() + " a little.");
                  Server.getInstance()
                     .broadCastAction(
                        performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + source.getNameWithGenus() + " a little.",
                        performer,
                        5
                     );
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You realize you almost damaged the " + source.getName() + " and stop.");
                  Server.getInstance().broadCastAction(performer.getName() + " stops improving " + source.getNameWithGenus() + ".", performer, 5);
               }

               performer.getStatus().modifyStamina(-counter * 1000.0F);
               return true;
            }
         }

         if (act.mayPlaySound()) {
            Methods.sendSound(performer, "sound.work.smithing.temper");
         }

         if (!(counter * 10.0F > (float)time) && !insta) {
            return false;
         } else {
            if (act.getRarity() != 0) {
               performer.playPersonalSound("sound.fx.drumroll");
            }

            if (target.getWeightGrams() < getTemperWaterAmountFor(source)) {
               target.setWeight(target.getWeightGrams() - getTemperWaterAmountFor(source), true);
               performer.getCommunicator().sendNormalServerMessage("There is too little water to temper the metal. It boils away instantly.");
               return true;
            } else {
               target.setWeight(target.getWeightGrams() - getTemperWaterAmountFor(source), true);
               improve.skillCheck((double)source.getQualityLevel(), bonus, false, skillgainMod * (performer.isPriest() ? counter / 2.0F : counter));
               Server.getInstance().broadCastAction(performer.getName() + " tempers " + source.getNameWithGenus() + ".", performer, 5);
               power = (double)act.getPower();
               if (power > 0.0) {
                  byte newState = getNewCreationState(source.getMaterial());
                  int tempr = getItemForImprovement(source.getMaterial(), newState);
                  if (tempr == source.getTemplateId()) {
                     --newState;
                  }

                  source.setCreationState(newState);
                  byte rarity = source.getRarity();
                  float rarityChance = 0.2F;
                  if (source.getSpellEffects() != null) {
                     rarityChance *= source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_RARITYIMP);
                  }

                  if (act.getRarity() > rarity && Server.rand.nextFloat() <= rarityChance) {
                     rarity = act.getRarity();
                  }

                  Item toRarify = source;
                  if (source.getTemplateId() == 128) {
                     toRarify = target;
                  }

                  if (rarity > toRarify.getRarity()) {
                     toRarify.setRarity(rarity);

                     for(Item sub : toRarify.getItems()) {
                        if (sub != null && sub.isComponentItem()) {
                           sub.setRarity(rarity);
                        }
                     }

                     if (toRarify.getRarity() > 2) {
                        performer.achievement(300);
                     } else if (toRarify.getRarity() == 1) {
                        performer.achievement(301);
                     } else if (toRarify.getRarity() == 2) {
                        performer.achievement(302);
                     }
                  }

                  if (newState != 0) {
                     String newString = getNeededCreationAction(source.getMaterial(), newState, source);
                     performer.getCommunicator().sendNormalServerMessage(newString);
                  } else if (source.getTemplateId() == 386) {
                     performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " is finished!");
                     source.setTemplateId(source.realTemplate);
                     skillNum = getImproveSkill(source);
                     int templateId = getImproveTemplateId(source);
                     if (skillNum != -10) {
                        try {
                           ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                           performer.getCommunicator()
                              .sendNormalServerMessage("The " + source.getName() + " could be improved with " + temp.getNameWithGenus() + ".");
                        } catch (NoSuchTemplateException var34) {
                        }
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You improve the " + source.getName() + " a bit.");
                     skillNum = getImproveSkill(source);
                     int templateId = getImproveTemplateId(source);
                     if (skillNum != -10) {
                        try {
                           ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
                           performer.getCommunicator()
                              .sendNormalServerMessage("The " + source.getName() + " could be improved with " + temp.getNameWithGenus() + ".");
                        } catch (NoSuchTemplateException var33) {
                        }
                     }
                  }

                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "before: " + source.getQualityLevel() + " now: " + ((double)source.getQualityLevel() + power) + " power=" + power
                        );
                  }

                  float oldQL = source.getQualityLevel();
                  boolean wasHighest = Items.isHighestQLForTemplate(source.getTemplateId(), source.getQualityLevel(), source.getWurmId(), true);
                  float modifier = 1.0F;
                  if (source.getSpellEffects() != null) {
                     modifier = source.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_IMPQL);
                  }

                  modifier *= target.getMaterialImpBonus();
                  source.setQualityLevel(Math.min(100.0F, (float)((double)source.getQualityLevel() + power * (double)modifier)));
                  if (source.getQualityLevel() > source.getOriginalQualityLevel()) {
                     source.setOriginalQualityLevel(source.getQualityLevel());
                     triggerImproveAchievements(performer, source, improve, wasHighest, oldQL);
                  }
               } else {
                  if (insta) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Dam before: " + source.getDamage() + " now: " + ((double)source.getDamage() - power) + " power=" + power);
                  }

                  source.setDamage(source.getDamage() - (float)power);
                  performer.getCommunicator().sendNormalServerMessage("You damage the " + source.getName() + " a little.");
                  Server.getInstance()
                     .broadCastAction(
                        performer.getName() + " grunts as " + performer.getHeSheItString() + " damages " + source.getNameWithGenus() + " a little.",
                        performer,
                        5
                     );
                  performer.achievement(206);
               }

               performer.getStatus().modifyStamina(-counter * 1000.0F);
               return true;
            }
         }
      }
   }

   public static void sendImproveSound(Creature performer, Item source, Item target, int skillNum) {
      String sound = "";
      int stid = source.getTemplateId();
      if (stid == 296) {
         sound = "sound.work.smithing.whetstone";
      } else if (stid == 313 || stid == 171) {
         sound = "sound.work.smithing.polish";
      } else if (stid == 24) {
         sound = "sound.work.carpentry.saw";
      } else if (stid == 8) {
         sound = "sound.work.carpentry.carvingknife";
      } else if (stid == 388) {
         sound = "sound.work.carpentry.rasp";
      } else if (target.isWood()) {
         sound = Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
      } else if (target.isMetal()) {
         sound = "sound.work.smithing.metal";
      } else if (target.isStone()) {
         if (skillNum != 10074 && stid != 97) {
            sound = "sound.work.masonry";
         } else {
            sound = "sound.work.stonecutting";
         }
      }

      Methods.sendSound(performer, sound);
   }

   static final boolean seal(Creature performer, Item source, Item target, Action act) {
      boolean done = false;
      if (!target.canBeSealedByPlayer() && !target.canBePeggedByPlayer()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " cannot be sealed.");
         return true;
      } else if (target.getItems().isEmpty()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " cannot be sealed as there is nothing in it.");
         return true;
      } else {
         Item liquid = null;

         for(Item item : target.getItems()) {
            if (item.isLiquid()) {
               liquid = item;
            }
         }

         if (liquid == null) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " cannot be sealed as there is no liquid in it.");
            return true;
         } else {
            if (act.currentSecond() == 1) {
               act.setTimeLeft(50);
               performer.getCommunicator().sendNormalServerMessage("You start to seal the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to seal " + target.getNameWithGenus() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[739].getVerbString(), true, act.getTimeLeft());
            } else {
               if (source.getTemplateId() == 561) {
                  if (act.currentSecond() == 2) {
                     performer.getCommunicator().sendNormalServerMessage("You position the peg over the hole on the top.");
                  } else if (act.currentSecond() == 3) {
                     performer.getCommunicator().sendNormalServerMessage("You press the peg in slowly so not to disturb the liquid.");
                  } else if (act.currentSecond() == 4) {
                     performer.getCommunicator().sendNormalServerMessage("You give the peg one final tap.");
                  }
               } else if (act.currentSecond() == 2) {
                  performer.getCommunicator().sendNormalServerMessage("You carefully wax the cloth.");
               } else if (act.currentSecond() == 3) {
                  performer.getCommunicator().sendNormalServerMessage("You put the waxed cloth over the " + target.getName() + ".");
               } else if (act.currentSecond() == 4) {
                  performer.getCommunicator().sendNormalServerMessage("You tie the string around " + target.getName() + ".");
               }

               int timeleft = act.getTimeLeft();
               if (act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  target.closeAll();
                  done = true;
                  performer.getCommunicator().sendNormalServerMessage("You sealed the " + target.getName() + ".");
                  target.setIsSealedByPlayer(true);
                  if (source.getTemplateId() == 1255) {
                     target.setData((int)source.getQualityLevel(), (int)source.getDamage());
                  }

                  Items.destroyItem(source.getWurmId());
               }
            }

            return done;
         }
      }
   }

   static final boolean breakSeal(Creature performer, Item target, Action act) {
      boolean done = false;
      Item liquid = null;

      for(Item item : target.getItemsAsArray()) {
         if (item.isLiquid()) {
            liquid = item;
            break;
         }
      }

      if (liquid != null && liquid.isFermenting()) {
         performer.getCommunicator()
            .sendNormalServerMessage("The " + target.getName() + " is still fermenting, therefore it makes no sense to unseal at this time..");
         return true;
      } else {
         if (act.currentSecond() == 1) {
            act.setTimeLeft(50);
            performer.getCommunicator().sendNormalServerMessage("You start to unseal the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to unseal " + target.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[740].getVerbString(), true, act.getTimeLeft());
         } else {
            if (target.canBePeggedByPlayer()) {
               if (act.currentSecond() == 2) {
                  performer.getCommunicator().sendNormalServerMessage("You put your hand on the peg on the top.");
               } else if (act.currentSecond() == 3) {
                  performer.getCommunicator().sendNormalServerMessage("You carefully remove the peg from the top.");
               } else if (act.currentSecond() == 4) {
                  performer.getCommunicator().sendNormalServerMessage("But you notice the peg is damaged so discard it.");
               }
            } else if (act.currentSecond() == 2) {
               performer.getCommunicator().sendNormalServerMessage("You remove the string.");
            } else if (act.currentSecond() == 3) {
               performer.getCommunicator().sendNormalServerMessage("You break the wax seal.");
            } else if (act.currentSecond() == 4) {
               performer.getCommunicator().sendNormalServerMessage("You try to get the wax seal off so it can be used again.");
            }

            int timeleft = act.getTimeLeft();
            if (act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
               target.closeAll();
               if (target.getData1() != -1) {
                  boolean decayed = true;

                  try {
                     Item kit = ItemFactory.createItem(1255, (float)target.getData1(), performer.getName());
                     decayed = kit.setDamage((float)(target.getData2() + 3 + Server.rand.nextInt(5)), true);
                     if (!decayed) {
                        performer.getInventory().insertItem(kit);
                        performer.getCommunicator().sendNormalServerMessage("You managed to get the wax sealing kit off, but damage it a bit.");
                     }
                  } catch (FailedException var9) {
                     logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
                  } catch (NoSuchTemplateException var10) {
                     logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
                  }

                  target.setData(-1, -1);
                  if (decayed) {
                     performer.getCommunicator().sendNormalServerMessage("You unsealed the " + target.getName() + " by destroying the wax sealing.");
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You unsealed the " + target.getName() + ".");
               }

               target.setIsSealedByPlayer(false);
               done = true;
            }
         }

         return done;
      }
   }

   static final boolean removeSecuritySeal(Creature performer, Item target, Action act) {
      boolean done = false;
      if (act.currentSecond() == 1) {
         act.setTimeLeft(50);
         performer.getCommunicator().sendNormalServerMessage("You start to remove the security seal on the " + target.getName() + ".");
         Server.getInstance()
            .broadCastAction(performer.getName() + " starts to remove the security seal on the " + target.getNameWithGenus() + ".", performer, 5);
         performer.sendActionControl(Actions.actionEntrys[740].getVerbString(), true, act.getTimeLeft());
      } else {
         int timeleft = act.getTimeLeft();
         if (act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
            target.closeAll();
            performer.getCommunicator().sendNormalServerMessage("You have removed the security seal from the " + target.getName() + ".");
            target.setIsSealedByPlayer(false);
            if (target.isCrate()) {
               for(Item item : target.getItems()) {
                  item.setLastOwnerId(target.getLastOwnerId());
                  item.setLastMaintained(WurmCalendar.currentTime);
               }

               target.setData(-10L);
            }

            done = true;
         }
      }

      return done;
   }

   static final boolean wrap(Creature performer, @Nullable Item source, Item target, Action act) {
      boolean done = true;
      if (target.isWrapped()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already wrapped.");
         return true;
      } else if (!target.usesFoodState()) {
         performer.getCommunicator().sendNormalServerMessage("You just cannot figure out how to wrap the " + target.getName() + ".");
         return true;
      } else {
         boolean insta = performer.getPower() >= 5;
         done = false;
         if (act.currentSecond() == 1) {
            act.setTimeLeft(50);
            if (source != null) {
               performer.getCommunicator().sendNormalServerMessage("You start to wrap the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to wrap " + target.getNameWithGenus() + ".", performer, 5);
            } else {
               performer.getCommunicator().sendNormalServerMessage("You start to wrap the " + target.getName() + " using some grass, leaves and moss.");
               Server.getInstance()
                  .broadCastAction(performer.getName() + " starts to wrap " + target.getNameWithGenus() + " using some grass, leaves and moss.", performer, 5);
            }

            performer.sendActionControl(Actions.actionEntrys[739].getVerbString(), true, act.getTimeLeft());
         } else {
            int timeleft = act.getTimeLeft();
            if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
               done = true;
               performer.getCommunicator().sendNormalServerMessage("You wrapped the " + target.getName() + ".");
               target.setIsWrapped(true);
               if (source != null) {
                  Items.destroyItem(source.getWurmId());
               }
            }
         }

         return done;
      }
   }

   static final boolean unwrap(Creature performer, Item target, Action act) {
      boolean done = true;
      if (!target.isWrapped()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is not wrapped.");
         return true;
      } else {
         boolean insta = performer.getPower() >= 5;
         done = false;
         if (act.currentSecond() == 1) {
            act.setTimeLeft(50);
            performer.getCommunicator().sendNormalServerMessage("You start to unwrap the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to unwrap " + target.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[740].getVerbString(), true, act.getTimeLeft());
         } else {
            int timeleft = act.getTimeLeft();
            if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
               done = true;
               target.setIsWrapped(false);
               performer.getCommunicator().sendNormalServerMessage("You unwrapped the " + target.getName() + " and throw away the used wrapping.");
            }
         }

         return done;
      }
   }

   public static boolean conquerTarget(Creature performer, Item target, Communicator comm, float counter, Action act) {
      if (target.isWarTarget()) {
         if (target.getKingdom() == performer.getKingdomId() && target.getData1() == 100) {
            comm.sendNormalServerMessage("The " + target.getName() + " is already conquered.");
            return true;
         } else {
            Kingdom pk = Kingdoms.getKingdom(performer.getKingdomId());
            if (pk != null && pk.isAllied(target.getKingdom())) {
               comm.sendNormalServerMessage("The " + target.getName() + " is already conquered by your alliance.");
               return true;
            } else if (performer.isOnSurface() != target.isOnSurface()) {
               if (performer.isOnSurface()) {
                  comm.sendNormalServerMessage("You need to be in cave to conquer the " + target.getName() + " (because it is).");
               } else {
                  comm.sendNormalServerMessage("You need to be on surface to conquer the " + target.getName() + ".");
               }

               return true;
            } else {
               int time = 6000;
               if (performer.getPower() > 0) {
                  int var17 = true;
               }

               int var18;
               if (target.getKingdom() == 0) {
                  if (target.getData2() == performer.getKingdomId()) {
                     var18 = (100 - target.getData1()) * 30;
                  } else {
                     var18 = target.getData1() * 30;
                  }
               } else if (target.getData2() == performer.getKingdomId()) {
                  var18 = (100 - target.getData1()) * 30;
               } else {
                  var18 = target.getData1() * 30;
               }

               if (!Servers.localServer.HOMESERVER && target.isWarTarget()) {
                  var18 = (int)((float)var18 * (1.0F + Zones.getPercentLandForKingdom(performer.getKingdomId()) / 300.0F));
               }

               act.setTimeLeft(var18);
               if (counter == 1.0F) {
                  for(Player player : Players.getInstance().getPlayers()) {
                     if (player.getWurmId() != performer.getWurmId()) {
                        try {
                           Action acta = player.getCurrentAction();
                           if (acta.getNumber() == 504 && acta.getTarget() == target.getWurmId()) {
                              comm.sendNormalServerMessage("The " + target.getName() + " can not be used by more than one person.");
                              return true;
                           }
                        } catch (NoSuchActionException var15) {
                        }
                     }
                  }

                  Long last = ItemBehaviour.conquers.get(target.getWurmId());
                  if (last != null && System.currentTimeMillis() - last < 3600000L) {
                     comm.sendAlertServerMessage(
                        String.format(
                           "You will have to wait %s if you want to receive battle rank for conquering the %s.",
                           Server.getTimeFor(last + 3600000L - System.currentTimeMillis()),
                           target.getName()
                        )
                     );
                  }

                  if (target.getKingdom() == performer.getKingdomId()) {
                     comm.sendNormalServerMessage("You start to secure the " + target.getName() + ".");
                     performer.sendActionControl(Actions.actionEntrys[504].getVerbString(), true, var18);
                     Server.getInstance().broadCastAction(performer.getName() + " starts securing the " + target.getName() + ".", performer, 10);
                  } else {
                     comm.sendNormalServerMessage("You start to conquer the " + target.getName() + ".");
                     performer.sendActionControl(Actions.actionEntrys[504].getVerbString(), true, var18);
                     Server.getInstance().broadCastAction(performer.getName() + " starts conquering the " + target.getName() + ".", performer, 10);
                  }
               }

               if (act.justTickedSecond() && (int)counter % 60 == 0) {
                  String name = target.getName();
                  if (target.getKingdom() != performer.getKingdomId()) {
                     Players.getInstance().broadCastConquerInfo(performer, name + " is being conquered.");
                     Server.getInstance().broadCastEpicEvent(name + " is being conquered.");
                  }

                  if (target.getKingdom() == 0) {
                     if (target.getData2() != performer.getKingdomId()) {
                        target.setData1(Math.max(0, target.getData1() - 20));
                     } else {
                        target.setData1(Math.min(100, target.getData1() + 20));
                     }
                  } else if (target.getData2() == performer.getKingdomId()) {
                     target.setData1(Math.min(100, target.getData1() + 20));
                  } else {
                     target.setData1(Math.max(0, target.getData1() - 20));
                  }

                  VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
                  t.updateTargetStatus(target.getWurmId(), (byte)target.getData2(), (float)target.getData1());
               }

               if ((target.getData1() == 100 || target.getData1() == 0) && counter > 60.0F) {
                  boolean dealPoints = target.getKingdom() != performer.getKingdomId() && target.getData1() == 100;
                  Long last = ItemBehaviour.conquers.get(target.getWurmId());
                  if (last != null && System.currentTimeMillis() - last < 3600000L) {
                     dealPoints = false;
                  }

                  if (dealPoints) {
                     ItemBehaviour.conquers.put(target.getWurmId(), System.currentTimeMillis());

                     for(Player p : Players.getInstance().getPlayers()) {
                        if (p.isFriendlyKingdom(performer.getKingdomId())
                           && p.isWithinDistanceTo(target.getPosX(), target.getPosY(), p.getPositionZ(), 300.0F)) {
                           p.modifyKarma(10);

                           try {
                              p.setRank(p.getRank());
                           } catch (IOException var14) {
                           }
                        }
                     }
                  }

                  if (target.getKingdom() != performer.getKingdomId()) {
                     if (target.getData1() == 100) {
                        Players.getInstance().broadCastConquerInfo(performer, performer.getName() + " conquers " + target.getName() + ".");
                        Server.getInstance().broadCastEpicEvent(performer.getName() + " conquers " + target.getName() + ".");
                        performer.achievement(368);
                        target.setData2(performer.getKingdomId());
                        target.setAuxData(performer.getKingdomId());
                        Kingdoms.addWarTargetKingdom(target);

                        for(int x = 0; x < 2 + Server.rand.nextInt(4); ++x) {
                           try {
                              GuardTower.spawnSoldier(target, performer.getKingdomId());
                           } catch (Exception var13) {
                              logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
                           }
                        }

                        GuardTower.spawnCommander(target, performer.getKingdomId());
                     } else if (target.getData1() == 0) {
                        target.setData2(performer.getKingdomId());
                        Players.getInstance().broadCastConquerInfo(performer, performer.getName() + " neutralizes " + target.getName() + ".");
                        Server.getInstance().broadCastEpicEvent(performer.getName() + " neutralizes " + target.getName() + ".");
                        comm.sendNormalServerMessage("You neutralize the " + target.getName() + ".");
                        target.setAuxData((byte)0);
                     }
                  } else if (target.getData1() == 100) {
                     comm.sendNormalServerMessage("You secure the " + target.getName() + ".");
                     target.setData2(performer.getKingdomId());
                  } else if (target.getData1() == 0) {
                     target.setData2(performer.getKingdomId());
                     Players.getInstance().broadCastConquerInfo(performer, performer.getName() + " neutralizes " + target.getName() + ".");
                     Server.getInstance().broadCastEpicEvent(performer.getName() + " neutralizes " + target.getName() + ".");
                     comm.sendNormalServerMessage("You neutralize the " + target.getName() + ".");
                     target.setAuxData((byte)0);
                  }

                  VolaTile t = Zones.getOrCreateTile(target.getTileX(), target.getTileY(), target.isOnSurface());
                  t.updateTargetStatus(target.getWurmId(), (byte)target.getData2(), (float)target.getData1());
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else if (!performer.isPaying()) {
         comm.sendNormalServerMessage("Due to exploitability, only premium players may conquer pillars.");
         return true;
      } else if (performer.getFightingSkill().getRealKnowledge() < 20.0) {
         comm.sendNormalServerMessage("You need fighting skill 20 in order to conquer pillars.");
         return true;
      } else if (Servers.localServer.getNextHota() != Long.MAX_VALUE) {
         comm.sendNormalServerMessage("The Hunt is not on.");
         return true;
      } else if (performer.getCitizenVillage() == null) {
         comm.sendNormalServerMessage("You have no alliance and can't assume control of the " + target.getName() + ".");
         return true;
      } else if (target.getData1() == 0
         || performer.getCitizenVillage() == null
         || target.getData1() != performer.getCitizenVillage().getAllianceNumber() && target.getData1() != performer.getCitizenVillage().getId()) {
         if (counter == 1.0F) {
            comm.sendNormalServerMessage("You start to conquer the " + target.getName() + ".");
            performer.sendActionControl(Actions.actionEntrys[504].getVerbString(), true, 1000);
            Server.getInstance().broadCastAction(performer.getName() + " starts conquering the " + target.getName() + ".", performer, 10);
            Hota.addPillarTouched(performer, target);
         }

         if (counter > 90.0F && target.getData1() != 0) {
            target.deleteAllEffects();
            target.setData1(0);
            Server.getInstance().broadCastSafe(performer.getName() + " neutralizes the " + target.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " neutralizes the " + target.getName() + ".", performer, 10);
            if (performer.getCitizenVillage() != null) {
               Hota.addPillarConquered(performer, target);
            }
         }

         if (counter > 100.0F) {
            if (performer.getCitizenVillage() != null) {
               Server.getInstance().broadCastSafe(performer.getName() + " conquers the " + target.getName() + ".");
               target.addEffect(EffectFactory.getInstance().createFire(target.getWurmId(), target.getPosX(), target.getPosY(), target.getPosZ(), true));
               if (performer.getCitizenVillage().getAllianceNumber() == 0) {
                  target.setData1(performer.getCitizenVillage().getId() + 2000000);
               } else {
                  target.setData1(performer.getCitizenVillage().getAllianceNumber());
               }

               Hota.addPillarConquered(performer, target);
            } else {
               comm.sendNormalServerMessage("You can only conquer the pillar in the name of a settlement or alliance.");
            }

            return true;
         } else {
            return false;
         }
      } else {
         comm.sendNormalServerMessage("Your alliance is already in control of the " + target.getName() + ".");
         Hota.addPillarTouched(performer, target);
         return true;
      }
   }

   static final boolean colorItem(Creature performer, Item colour, Item target, Action act, boolean primary) {
      boolean done = true;
      if (target.isIndestructible()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is indestructible and the colour can not be replaced.");
         return true;
      } else {
         String sItem = "";
         if (primary) {
            if (target.getTemplateId() == 1396) {
               sItem = "barrel";
            }

            if (target.color != -1) {
               if (sItem.length() == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has colour on it.");
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + target.getName() + " already has colour on it's " + sItem + ". Remove it first with a metal brush.");
               }

               return true;
            }
         }

         if (!primary) {
            if (target.getTemplateId() == 1396) {
               sItem = "lamp";
            } else {
               sItem = target.getSecondryItemName();
            }

            if (target.color2 != -1) {
               if (sItem.length() == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has colour on it.");
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage("The " + target.getName() + " already has colour on it's " + sItem + ". Remove it first with some lye.");
               }

               return true;
            }
         }

         int dyeOverride = target.getTemplate().getDyePrimaryAmountGrams();
         if (!primary && !target.isDragonArmour()) {
            if (target.getTemplate().getDyeSecondaryAmountGrams() > 0) {
               dyeOverride = target.getTemplate().getDyeSecondaryAmountGrams();
            } else if (dyeOverride > 0) {
               dyeOverride = (int)((float)dyeOverride * 0.3F);
            }
         }

         int colourNeeded;
         if (dyeOverride > 0) {
            colourNeeded = dyeOverride;
         } else {
            colourNeeded = (int)Math.max(1.0, (double)target.getSurfaceArea() * (primary ? 1.0 : 0.3) / 25.0);
         }

         boolean insta = performer.getPower() >= 5;
         String type;
         if (!primary && !target.isDragonArmour()) {
            type = "dye";
         } else {
            type = "paint";
         }

         if (!insta && colourNeeded > colour.getWeightGrams()) {
            performer.getCommunicator()
               .sendNormalServerMessage("You need more " + type + " to colour that item - at least " + colourNeeded + "g of " + type + ".");
         } else {
            done = false;
            if (act.currentSecond() == 1) {
               act.setTimeLeft(Math.max(50, colourNeeded / 50));
               if ((primary || target.isDragonArmour()) && target.getTemplateId() != 1396) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You start to " + type + " the " + target.getName() + " (using " + colourNeeded + "g of " + type + ").");
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You start to " + type + " the " + target.getName() + "'s " + sItem + " (using " + colourNeeded + "g of " + type + ")."
                     );
               }

               Server.getInstance().broadCastAction(performer.getName() + " starts to " + type + " " + target.getNameWithGenus() + ".", performer, 5);
               String verb;
               if (primary) {
                  verb = Actions.actionEntrys[231].getVerbString();
               } else {
                  verb = Actions.actionEntrys[923].getVerbString();
               }

               performer.sendActionControl(verb, true, act.getTimeLeft());
            } else {
               int timeleft = act.getTimeLeft();
               if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  done = true;
                  if (primary) {
                     target.setColor(colour.getColor());
                  } else {
                     target.setColor2(colour.getColor());
                  }

                  colour.setWeight(colour.getWeightGrams() - colourNeeded, true);
                  if (!primary && !target.isDragonArmour()) {
                     if (target.getTemplateId() == 1396) {
                        performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + "'s " + sItem + ".");
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You dye the " + target.getName() + "'s " + sItem + ".");
                     }
                  } else if (target.getTemplateId() == 1396) {
                     performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + "'s " + sItem + ".");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You paint the " + target.getName() + ".");
                  }

                  if (target.isBoat() && !primary) {
                     performer.achievement(494);
                  }

                  if (target.isArmour()) {
                     performer.achievement(493);
                  }
               }
            }
         }

         return done;
      }
   }

   static final boolean improveColor(Creature performer, Item colorComponent, Item dye, Action act) {
      boolean done = true;
      boolean insta = performer.getPower() >= 5;
      performer.sendToLoggers(
         colorComponent.getName() + " weight: " + colorComponent.getWeightGrams() + ", " + dye.getName() + " weight: " + dye.getWeightGrams()
      );
      if (colorComponent.getTemplate().getWeightGrams() > colorComponent.getWeightGrams()) {
         performer.getCommunicator().sendNormalServerMessage("You need more " + colorComponent.getName() + " to improve the dye.");
      } else {
         done = false;
         if (act.currentSecond() == 1) {
            act.setTimeLeft(50);
            performer.getCommunicator().sendNormalServerMessage("You start to improve the " + dye.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to improve " + dye.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[283].getVerbString(), true, act.getTimeLeft());
         } else {
            int timeleft = act.getTimeLeft();
            if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
               done = true;
               dye.setColor(WurmColor.getCompositeColor(dye.color, dye.getWeightGrams(), colorComponent.getTemplateId(), colorComponent.getQualityLevel()));
               colorComponent.setWeight(colorComponent.getWeightGrams() - colorComponent.getTemplate().getWeightGrams(), true);
               performer.getCommunicator().sendNormalServerMessage("You try to improve the " + dye.getName() + ".");
            }
         }
      }

      return done;
   }

   static final boolean removeColor(Creature performer, Item brush, Item target, Action act, boolean primary) {
      boolean done = true;
      if (target.isIndestructible()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is indestructible and the colour can not be removed.");
         return true;
      } else if (target.isDragonArmour() && primary) {
         performer.getCommunicator()
            .sendNormalServerMessage("The " + target.getName() + " is too tough for the " + brush.getName() + " and the colour refuses to disappear.");
         return true;
      } else {
         boolean insta = performer.getPower() >= 5;
         int colourNeeded = 0;
         String sItem = "";
         if (target.getTemplateId() == 1396) {
            if (primary && brush.getTemplateId() != 441 || !primary && brush.getTemplateId() != 73) {
               performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
               return true;
            }

            if (primary) {
               sItem = "barrel";
            } else {
               sItem = "lamp";
            }
         } else {
            if (primary && brush.getTemplateId() != 441) {
               performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
               return true;
            }

            if (!primary) {
               sItem = target.getSecondryItemName();
            }
         }

         if (!insta && target.color == -1 && primary) {
            performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no colour.");
            return true;
         } else {
            if (!primary) {
               if (brush.getTemplateId() == 441) {
                  if (!insta && target.color2 == -1) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " has no colour.");
                     return true;
                  }

                  if (target.isColorable() && target.getTemplateId() != 1396) {
                     performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
                     return true;
                  }
               } else {
                  if (brush.getTemplateId() != 73) {
                     performer.getCommunicator().sendNormalServerMessage("You cannot use the " + brush.getName() + " to do this.");
                     return true;
                  }

                  int dyeOverride = target.getTemplate().getDyePrimaryAmountGrams();
                  if (!primary) {
                     if (target.getTemplate().getDyeSecondaryAmountGrams() > 0) {
                        dyeOverride = target.getTemplate().getDyeSecondaryAmountGrams();
                     } else if (dyeOverride > 0) {
                        dyeOverride = (int)((float)dyeOverride * 0.3F);
                     }
                  }

                  if (dyeOverride > 0) {
                     colourNeeded = dyeOverride;
                  } else {
                     colourNeeded = (int)Math.max(1.0, (double)target.getSurfaceArea() * (primary ? 1.0 : 0.3) / 25.0);
                  }

                  colourNeeded = Math.max(1, colourNeeded / 2);
                  if (!insta && colourNeeded > brush.getWeightGrams()) {
                     performer.getCommunicator().sendNormalServerMessage("You need more lye (" + colourNeeded + "g) to bleach that item.");
                     return true;
                  }
               }
            }

            done = false;
            if (act.currentSecond() == 1) {
               String type;
               if (brush.getTemplateId() == 441) {
                  type = "brush";
               } else {
                  type = "bleach";
               }

               act.setTimeLeft(150);
               performer.getCommunicator().sendNormalServerMessage("You start to " + type + " the " + target.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to " + type + " " + target.getNameWithGenus() + ".", performer, 5);
               String verb;
               if (brush.getTemplateId() == 441 && target.getTemplateId() != 1396) {
                  verb = Actions.actionEntrys[232].getVerbString();
               } else {
                  verb = Actions.actionEntrys[924].getVerbString();
               }

               performer.sendActionControl(verb, true, act.getTimeLeft());
            } else {
               int timeleft = act.getTimeLeft();
               if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  done = true;
                  if (primary) {
                     target.setColor(-1);
                     if (brush.getTemplateId() == 441) {
                        brush.setDamage((float)((double)brush.getDamage() + 0.5 * (double)brush.getDamageModifier()));
                     } else {
                        brush.setWeight(brush.getWeightGrams() - colourNeeded, true);
                     }

                     if (sItem.length() == 0) {
                        performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target.getName() + ".");
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target.getName() + "'s " + sItem + ".");
                     }
                  } else {
                     target.setColor2(-1);
                     if (brush.getTemplateId() == 441) {
                        brush.setDamage((float)((double)brush.getDamage() + 0.5 * (double)brush.getDamageModifier()));
                     } else {
                        brush.setWeight(brush.getWeightGrams() - colourNeeded, true);
                     }

                     if (sItem.length() == 0) {
                        performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target.getName() + ".");
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You remove the colour from the " + target.getName() + "'s " + sItem + ".");
                     }
                  }
               }
            }

            return done;
         }
      }
   }

   static final boolean attachBags(Creature performer, Item bags, Item saddle, Action act) {
      boolean done = true;
      if (bags.getTemplateId() != 1333 || saddle.getTemplateId() != 621) {
         performer.getCommunicator().sendNormalServerMessage("You can not attach the " + bags.getName() + " to the " + saddle.getName() + ".");
      } else if (saddle.getAuxData() == 0) {
         saddle.setAuxData((byte)1);
         if (bags.getRarity() > saddle.getRarity()) {
            saddle.setRarity(bags.getRarity());
         }

         Items.destroyItem(bags.getWurmId());
         Item topParent = saddle.getTopParentOrNull();
         if (topParent != null && topParent.isHollow()) {
            try {
               Creature[] watchers = saddle.getWatchers();
               if (watchers != null) {
                  long inventoryWindow = saddle.getTopParent();
                  if (topParent.isInventory()) {
                     inventoryWindow = -1L;
                  }

                  for(Creature watcher : watchers) {
                     watcher.getCommunicator().sendRemoveFromInventory(saddle, inventoryWindow);
                     watcher.getCommunicator().sendAddToInventory(saddle, inventoryWindow, -1L, -1);
                  }
               }
            } catch (NoSuchCreatureException var13) {
            }
         }

         performer.getCommunicator().sendNormalServerMessage("You add bags to the saddle.");
         Server.getInstance().broadCastAction(performer.getName() + " adds bags to a saddle.", performer, 5);
      } else {
         performer.getCommunicator().sendNormalServerMessage("The saddle already has bags attached.");
      }

      return done;
   }

   static final boolean removeBags(Creature performer, Item saddle, Action act) {
      boolean done = true;
      if (saddle.getTemplateId() == 621) {
         if (saddle.getAuxData() == 1) {
            if (saddle.getItemCount() > 0) {
               performer.getCommunicator().sendNormalServerMessage("You must empty the bags before removing them.");
            } else {
               try {
                  saddle.setAuxData((byte)0);
                  Item bags = ItemFactory.createItem(1333, 20.0F, (byte)16, (byte)0, null);
                  Item inventory = performer.getInventory();
                  inventory.insertItem(bags);
                  Item topParent = saddle.getTopParentOrNull();
                  if (topParent != null && topParent.isHollow()) {
                     try {
                        Creature[] watchers = saddle.getWatchers();
                        if (watchers != null) {
                           long inventoryWindow = saddle.getTopParent();
                           if (topParent.isInventory()) {
                              inventoryWindow = -1L;
                           }

                           for(Creature watcher : watchers) {
                              watcher.getCommunicator().sendRemoveFromInventory(saddle, inventoryWindow);
                              watcher.getCommunicator().sendAddToInventory(saddle, inventoryWindow, -1L, -1);
                           }
                        }
                     } catch (NoSuchCreatureException var14) {
                     }
                  }

                  performer.getCommunicator().sendNormalServerMessage("You remove the bags from the saddle.");
                  Server.getInstance().broadCastAction(performer.getName() + " removes the bags from a saddle.", performer, 5);
               } catch (FailedException var15) {
                  logger.log(Level.WARNING, performer.getName() + " " + var15.getMessage(), (Throwable)var15);
               } catch (NoSuchTemplateException var16) {
                  logger.log(Level.WARNING, performer.getName() + " " + var16.getMessage(), (Throwable)var16);
               }
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("The " + saddle.getName() + " does not have bags attached.");
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You can not remove bags from the " + saddle.getName() + ".");
      }

      return done;
   }

   static final boolean string(Creature performer, Item bowstring, Item bow, Action act) {
      boolean done = true;
      if (bowstring.getTemplateId() == 457) {
         boolean insta = performer.getPower() >= 5;
         if (!bow.isBowUnstringed()) {
            performer.getCommunicator().sendNormalServerMessage("The " + bow.getName() + " cannot be strung.");
         } else {
            done = false;

            try {
               Item parent = bow.getParent();
               if (parent.isBodyPartAttached()) {
                  if (parent.getPlace() == 13) {
                     Item rightHandWeapon = performer.getRighthandWeapon();
                     if (rightHandWeapon != null) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("You can not string the " + bow.getName() + " while wielding " + rightHandWeapon.getNameWithGenus() + ".");
                        return true;
                     }
                  } else if (parent.getPlace() == 14) {
                     Item leftHandWeapon = performer.getLefthandWeapon();
                     if (leftHandWeapon != null) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("You can not string the " + bow.getName() + " while wielding " + leftHandWeapon.getNameWithGenus() + ".");
                        return true;
                     }
                  }
               }
            } catch (NoSuchItemException var8) {
            }

            if (act.currentSecond() == 1) {
               act.setTimeLeft(150);
               performer.getCommunicator().sendNormalServerMessage("You start to string the " + bow.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to string " + bow.getNameWithGenus() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[132].getVerbString(), true, act.getTimeLeft());
            } else {
               int timeleft = act.getTimeLeft();
               if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  if (bowstring.getRarity() != 0) {
                     performer.playPersonalSound("sound.fx.drumroll");
                  }

                  int realTemplate = 447;
                  if (bow.getTemplateId() == 461) {
                     realTemplate = 449;
                  } else if (bow.getTemplateId() == 460) {
                     realTemplate = 448;
                  }

                  done = true;
                  bow.setTemplateId(realTemplate);
                  bow.setAuxData((byte)((int)bowstring.getCurrentQualityLevel()));
                  if (bowstring.getRarity() != 0 && Server.rand.nextInt(100) == 0 && bowstring.getRarity() > bow.getRarity()) {
                     bow.setRarity(bowstring.getRarity());
                  }

                  Items.destroyItem(bowstring.getWurmId());
                  performer.getCommunicator().sendNormalServerMessage("You string the " + bow.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " strings " + bow.getNameWithGenus() + ".", performer, 5);
               }
            }
         }
      } else {
         performer.getCommunicator().sendNormalServerMessage("You cannot use the " + bowstring.getName() + " to string the bow.");
      }

      return done;
   }

   static final boolean stringRod(Creature performer, Item string, Item rod, Action act) {
      boolean done = true;
      if (string.getTemplateId() != 150 && string.getTemplateId() != 151) {
         performer.getCommunicator().sendNormalServerMessage("You cannot use the " + string.getName() + " to string the rod.");
      } else {
         boolean insta = performer.getPower() >= 5;
         if (rod.getTemplateId() != 780) {
            performer.getCommunicator().sendNormalServerMessage("The " + rod.getName() + " cannot be stringed.");
         } else {
            done = false;
            if (act.currentSecond() == 1) {
               act.setTimeLeft(150);
               performer.getCommunicator().sendNormalServerMessage("You start to string the " + rod.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to string " + rod.getNameWithGenus() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[132].getVerbString(), true, act.getTimeLeft());
            } else {
               int timeleft = act.getTimeLeft();
               if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  if (string.getRarity() != 0) {
                     performer.playPersonalSound("sound.fx.drumroll");
                  }

                  int realTemplate = 94;
                  if (string.getTemplateId() == 150) {
                     realTemplate = 94;
                  } else if (string.getTemplateId() == 151) {
                     realTemplate = 152;
                  }

                  done = true;
                  rod.setTemplateId(realTemplate);
                  if (string.getRarity() != 0 && Server.rand.nextInt(100) == 0 && string.getRarity() > rod.getRarity()) {
                     rod.setRarity(string.getRarity());
                  }

                  Items.destroyItem(string.getWurmId());
                  performer.getCommunicator().sendNormalServerMessage("You string the " + rod.getName() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " strings " + rod.getNameWithGenus() + ".", performer, 5);
               }
            }
         }
      }

      return done;
   }

   static final boolean unstringBow(Creature performer, Item bow, Action act, float counter) {
      boolean done = true;
      if (bow.getTopParent() != performer.getInventory().getWurmId()) {
         performer.getCommunicator().sendNormalServerMessage("You must first pick the " + bow.getTemplate().getName() + " up in order to do that.");
         return true;
      } else {
         if (bow.isBowUnstringed()) {
            performer.getCommunicator().sendNormalServerMessage("The bow is already unstringed.");
         } else if (bow.isWeaponBow()) {
            boolean insta = performer.getPower() >= 5;
            done = false;
            if (act.currentSecond() == 1) {
               act.setTimeLeft(150);
               performer.getCommunicator().sendNormalServerMessage("You start to unstring the " + bow.getName() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " starts to unstring " + bow.getNameWithGenus() + ".", performer, 5);
               performer.sendActionControl(Actions.actionEntrys[133].getVerbString(), true, act.getTimeLeft());
            } else {
               int timeleft = act.getTimeLeft();
               if (insta || act.getCounterAsFloat() * 10.0F >= (float)timeleft) {
                  done = true;
                  byte ql = (byte)Math.max(2, bow.getAuxData() - 1);

                  try {
                     Item bowstring = ItemFactory.createItem(457, (float)ql, null);
                     int realTemplate = 459;
                     if (bow.getTemplateId() == 449) {
                        realTemplate = 461;
                     } else if (bow.getTemplateId() == 448) {
                        realTemplate = 460;
                     }

                     performer.getCommunicator().sendNormalServerMessage("You unstring the " + bow.getName() + ".");
                     Server.getInstance()
                        .broadCastAction(performer.getName() + " unstrings " + performer.getHisHerItsString() + " " + bow.getName() + ".", performer, 5);
                     bow.setTemplateId(realTemplate);
                     bow.setAuxData((byte)0);
                     performer.getInventory().insertItem(bowstring, true);
                  } catch (NoSuchTemplateException var10) {
                     logger.log(Level.WARNING, performer.getName() + ":" + bow.getName() + " " + var10.getMessage(), (Throwable)var10);
                     performer.getCommunicator().sendNormalServerMessage("You fail. The string seems stuck.");
                  } catch (FailedException var11) {
                     logger.log(Level.WARNING, performer.getName() + ":" + bow.getName() + " " + var11.getMessage(), (Throwable)var11);
                     performer.getCommunicator().sendNormalServerMessage("You fail. The string seems stuck.");
                  }
               }
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You can not unstring that!");
         }

         return done;
      }
   }

   static final boolean smear(Creature performer, Item potion, Item target, Action act, float counter) {
      boolean done = false;
      if (!Spell.mayBeEnchanted(target)) {
         performer.getCommunicator().sendNormalServerMessage("You can't imbue that.");
         return true;
      } else {
         ItemTemplate potionTemplate;
         try {
            potionTemplate = ItemTemplateFactory.getInstance().getTemplate(potion.getTemplateId());
         } catch (NoSuchTemplateException var17) {
            performer.getCommunicator().sendAlertServerMessage("ERROR: Could not load item template for" + potion.getName() + ". Please report this.");
            logger.warning("Could not locate template for " + potion.getName() + " with wid=" + potion.getWurmId() + ". " + var17);
            return true;
         }

         if (potion.isLiquid() && potion.getWeightGrams() < potionTemplate.getWeightGrams()) {
            performer.getCommunicator()
               .sendNormalServerMessage("The " + potion.getName() + " does not contain enough liquid to smear the " + target.getName());
            return true;
         } else {
            if (!done && counter == 1.0F) {
               byte enchantment = potion.getEnchantForPotion();
               if ((enchantment == 91 || enchantment == 90 || enchantment == 92) && target.enchantment != 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has that kind of enchantment.");
                  return true;
               }

               ItemSpellEffects currentEffects = target.getSpellEffects();
               if (currentEffects == null) {
                  currentEffects = new ItemSpellEffects(target.getWurmId());
               }

               SpellEffect eff = currentEffects.getSpellEffect(enchantment);
               if (eff != null && enchantment == 98) {
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is already protected against shattering.");
                  return true;
               }

               if (eff != null && eff.getPower() >= 100.0F) {
                  performer.getCommunicator().sendNormalServerMessage("The power of the " + target.getName() + " is already at max.");
                  return true;
               }

               act.setTimeLeft(200);
               performer.getCommunicator().sendNormalServerMessage("You start to smear the " + potion.getName() + " on the " + target.getName() + ".");
               Server.getInstance()
                  .broadCastAction(
                     performer.getName() + " starts to smear " + potion.getNameWithGenus() + " on " + target.getNameWithGenus() + ".", performer, 5
                  );
               performer.sendActionControl(Actions.actionEntrys[633].getVerbString(), true, act.getTimeLeft());
            }

            if (!done && counter > (float)(act.getTimeLeft() / 10)) {
               done = true;
               performer.getCommunicator().sendNormalServerMessage("You imbue " + target.getNameWithGenus() + " with " + potion.getNameWithGenus() + ".");
               Server.getInstance().broadCastAction(performer.getName() + " imbues " + target.getNameWithGenus() + ".", performer, 5);
               Skill alch = null;

               try {
                  alch = performer.getSkills().getSkill(10042);
               } catch (NoSuchSkillException var16) {
                  alch = performer.getSkills().learn(10042, 1.0F);
               }

               byte enchantment = potion.getEnchantForPotion();
               if (enchantment != 91 && enchantment != 90 && enchantment != 92) {
                  ItemSpellEffects spellEffects = target.getSpellEffects();
                  if (spellEffects == null) {
                     spellEffects = new ItemSpellEffects(target.getWurmId());
                  }

                  SpellEffect eff = spellEffects.getSpellEffect(enchantment);
                  double skpower = alch.skillCheck(50.0, (double)potion.getCurrentQualityLevel(), false, 1.0F);
                  double power = 5.0 + Math.max(20.0, skpower) / 20.0 + (double)(potion.getCurrentQualityLevel() / 20.0F);
                  if (eff == null && potion.getTemplateId() == 1091) {
                     float toReturnPower = 100.0F;
                     performer.getCommunicator()
                        .sendNormalServerMessage("The " + target.getName() + " will now be protected against cracking and shattering.");
                     eff = new SpellEffect(target.getWurmId(), enchantment, toReturnPower, 20000000);
                     spellEffects.addSpellEffect(eff);
                     Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
                  } else if (eff == null) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("The " + target.getName() + " will now be more effective when used for the specific purpose.");
                     eff = new SpellEffect(target.getWurmId(), enchantment, (float)power, 20000000);
                     spellEffects.addSpellEffect(eff);
                     Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
                  } else if (eff.getPower() < 100.0F) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("You succeed in improving the power of the effectiveness of the " + target.getName() + ".");
                     eff.setPower(Math.min(100.0F, eff.getPower() + (float)power));
                     Server.getInstance().broadCastAction(performer.getName() + " looks pleased.", performer, 5);
                  }
               } else {
                  if (target.enchantment != 0) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " already has that kind of enchantment.");
                     return true;
                  }

                  target.enchant(enchantment);
                  String damtype = "fire";
                  if (enchantment == 90) {
                     damtype = "acid";
                  } else if (enchantment == 92) {
                     damtype = "frost";
                  }

                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " will now deal " + damtype + " damage.");
               }

               potion.setWeight(potion.getWeightGrams() - potionTemplate.getWeightGrams(), true);
            }

            return done;
         }
      }
   }

   static final boolean createOil(Creature performer, Item sourceSalt, Item target, Action act, float counter) {
      boolean done = true;
      Skill alch = null;

      try {
         alch = performer.getSkills().getSkill(10042);
      } catch (NoSuchSkillException var13) {
         alch = performer.getSkills().learn(10042, 1.0F);
      }

      int knowl = (int)alch.getKnowledge(0.0);
      done = false;
      if (sourceSalt.getWeightGrams() < sourceSalt.getTemplate().getWeightGrams() * 10) {
         performer.getCommunicator().sendNormalServerMessage("The " + sourceSalt.getName() + " contains too little material.");
         return true;
      } else if (target.getWeightGrams() < target.getTemplate().getWeightGrams()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " contains too little material.");
         return true;
      } else if (!performer.getInventory().mayCreatureInsertItem()) {
         performer.getCommunicator().sendNormalServerMessage("You do not have enough room in your inventory.");
         return true;
      } else {
         if (!done && counter == 1.0F) {
            int time = 200;
            time = Math.max(50, time - knowl);
            act.setTimeLeft(time);
            performer.getCommunicator()
               .sendNormalServerMessage("You start to create something from the " + sourceSalt.getName() + " and the " + target.getName() + ".");
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " starts to create something from " + sourceSalt.getNameWithGenus() + " and " + target.getNameWithGenus() + ".",
                  performer,
                  5
               );
            performer.sendActionControl(Actions.actionEntrys[283].getVerbString(), true, act.getTimeLeft());
         }

         if (!done && counter > (float)(act.getTimeLeft() / 10)) {
            done = true;
            double power = alch.skillCheck(20.0, sourceSalt, 0.0, false, counter);
            if (power > 0.0) {
               try {
                  Item potion = ItemFactory.createItem(target.getPotionTemplateIdForBlood(), (float)power, null);
                  performer.getCommunicator().sendNormalServerMessage("You create " + potion.getNameWithGenus() + ".");
                  Server.getInstance().broadCastAction(performer.getName() + " creates " + potion.getNameWithGenus() + ".", performer, 5);
                  int allWeight = sourceSalt.getTemplate().getWeightGrams() * 10 + target.getTemplate().getWeightGrams();
                  potion.setWeight(allWeight, true);
                  performer.getInventory().insertItem(potion, true);
               } catch (Exception var12) {
                  performer.getCommunicator().sendNormalServerMessage("You fail to create anything useful.");
                  Server.getInstance().broadCastAction(performer.getName() + " fails to create anything useful.", performer, 5);
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You fail to create anything useful.");
               Server.getInstance().broadCastAction(performer.getName() + " fails to create anything useful.", performer, 5);
            }

            sourceSalt.setWeight(sourceSalt.getWeightGrams() - sourceSalt.getTemplate().getWeightGrams() * 10, true);
            target.setWeight(target.getWeightGrams() - target.getTemplate().getWeightGrams(), true);
         }

         return done;
      }
   }

   static final boolean createSalve(Creature performer, Item source, Item target, Action act, float counter) {
      boolean done = true;
      Skill alch = null;

      try {
         alch = performer.getSkills().getSkill(10042);
      } catch (NoSuchSkillException var14) {
         alch = performer.getSkills().learn(10042, 1.0F);
      }

      int knowl = (int)alch.getKnowledge(0.0);
      int pow = source.getAlchemyType() * target.getAlchemyType();
      done = false;
      if (source.getWeightGrams() < source.getTemplate().getWeightGrams()) {
         performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " contains too little material.");
         return true;
      } else if (target.getWeightGrams() < target.getTemplate().getWeightGrams()) {
         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " contains too little material.");
         return true;
      } else if (!performer.getInventory().mayCreatureInsertItem()) {
         performer.getCommunicator().sendNormalServerMessage("You do not have enough room in your inventory.");
         return true;
      } else {
         if (!done && counter == 1.0F) {
            int time = 200;
            time = Math.max(50, time - knowl);
            act.setTimeLeft(time);
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You start to create a healing cover of the " + source.getName() + ", the " + target.getName() + " and some grass, leaves and moss."
               );
            Server.getInstance()
               .broadCastAction(
                  performer.getName()
                     + " starts to create something from "
                     + source.getNameWithGenus()
                     + ", "
                     + target.getNameWithGenus()
                     + " and some grass, leaves and moss.",
                  performer,
                  5
               );
            performer.sendActionControl(Actions.actionEntrys[283].getVerbString(), true, act.getTimeLeft());
         }

         if (!done && counter > (float)(act.getTimeLeft() / 10)) {
            done = true;
            double power = alch.skillCheck((double)pow, source, 0.0, false, counter);
            if (power > 0.0) {
               try {
                  Item cover = ItemFactory.createItem(481, (float)power, null);
                  performer.getCommunicator().sendNormalServerMessage("You create a healing cover.");
                  Server.getInstance().broadCastAction(performer.getName() + " creates a healing cover.", performer, 5);
                  int allWeight = source.getTemplate().getWeightGrams() + target.getTemplate().getWeightGrams();
                  cover.setAuxData((byte)pow);
                  cover.setWeight(allWeight, true);
                  cover.setDescription("made from " + source.getName().toLowerCase() + " and " + target.getName().toLowerCase());
                  performer.getInventory().insertItem(cover, true);
                  performer.achievement(546);
               } catch (Exception var13) {
                  performer.getCommunicator().sendNormalServerMessage("You fail to create a healing cover.");
                  Server.getInstance().broadCastAction(performer.getName() + " fails to create a healing cover.", performer, 5);
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You fail to create a healing cover.");
               Server.getInstance().broadCastAction(performer.getName() + " fails to create a healing cover.", performer, 5);
            }

            source.setWeight(source.getWeightGrams() - source.getTemplate().getWeightGrams(), true);
            target.setWeight(target.getWeightGrams() - target.getTemplate().getWeightGrams(), true);
         }

         return done;
      }
   }

   static final boolean createPotion(Creature performer, Item source, Item target, Action act, float counter, byte potionAuxData) {
      boolean done = true;
      Skill alch = performer.getSkills().getSkillOrLearn(10042);
      int knowl = (int)alch.getKnowledge(0.0);
      done = false;
      int totalWeight = 0;
      Item[] contents = target.getAllItems(false);
      int templateWeight = 0;
      boolean allSame = false;
      float solidAverageQL = 0.0F;
      if (contents.length < 1) {
         performer.getCommunicator().sendNormalServerMessage("There is nothing in the " + target.getName() + " to do anything with.");
         return true;
      } else {
         allSame = true;
         int lookingFor = contents[0].getTemplateId();
         templateWeight = contents[0].getTemplate().getWeightGrams();

         for(Item i : contents) {
            int solidWeight = i.getWeightGrams();
            if (i.getTemplateId() != lookingFor) {
               allSame = false;
               break;
            }

            float qlwt = solidAverageQL * (float)totalWeight;
            qlwt += (float)solidWeight * i.getCurrentQualityLevel();
            totalWeight += solidWeight;
            solidAverageQL = qlwt / (float)totalWeight;
         }

         if (!allSame) {
            performer.getCommunicator().sendNormalServerMessage("There are contaminants in the " + target.getName() + ", so liquid would be ruined.");
            return true;
         } else {
            lookingFor = totalWeight / templateWeight;
            if (lookingFor == 0) {
               performer.getCommunicator().sendNormalServerMessage("There is not enough of " + contents[0].getName() + " to do anything with.");
               return true;
            } else {
               int neededLiquid = (int)((float)lookingFor * 50.0F);
               if (neededLiquid > source.getWeightGrams()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage("You dont have enough " + source.getName() + " for that much " + contents[0].getName() + ".");
                  return true;
               } else {
                  if (!done && counter == 1.0F) {
                     int time = 200;
                     time = Math.max(50, time - knowl);
                     act.setTimeLeft(time);
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You start to create some transmutation liquid using " + source.getName() + " on the " + contents[0].getName() + "."
                        );
                     Server.getInstance()
                        .broadCastAction(
                           performer.getName() + " starts to create something from " + source.getNameWithGenus() + " and " + contents[0].getNameWithGenus(),
                           performer,
                           5
                        );
                     performer.sendActionControl(Actions.actionEntrys[283].getVerbString(), true, act.getTimeLeft());
                  }

                  if (act.currentSecond() == 2) {
                     performer.getCommunicator().sendNormalServerMessage("You see the " + contents[0].getName() + " absorb the " + source.getName() + ".");
                  } else if (act.currentSecond() == 4) {
                     performer.getCommunicator().sendNormalServerMessage("Now the " + contents[0].getName() + " starts to effervesce.");
                  } else if (act.currentSecond() == 6) {
                     performer.getCommunicator().sendNormalServerMessage("The bubbles now obscure the " + contents[0].getName() + ".");
                  } else if (act.currentSecond() == 8) {
                     performer.getCommunicator().sendNormalServerMessage("The bubbles start receeding.");
                  }

                  if (act.currentSecond() % 5 == 0) {
                     performer.getStatus().modifyStamina(-1000.0F);
                  }

                  if (!done && counter > (float)(act.getTimeLeft() / 10)) {
                     int difficulty = getTransmutationLiquidDifficulty(potionAuxData);
                     if (act.getRarity() != 0) {
                        performer.playPersonalSound("sound.fx.drumroll");
                        switch(act.getRarity()) {
                           case 1:
                              difficulty = (int)((float)difficulty * 0.8F);
                              break;
                           case 2:
                              difficulty = (int)((float)difficulty * 0.5F);
                              break;
                           case 3:
                              difficulty = (int)((float)difficulty * 0.2F);
                        }
                     }

                     done = true;
                     float alc = 0.0F;
                     if (performer.isPlayer()) {
                        alc = ((Player)performer).getAlcohol();
                     }

                     float bonus = (solidAverageQL + source.getCurrentQualityLevel()) / 2.0F;
                     double power = alch.skillCheck((double)((float)difficulty + alc), source, (double)bonus, false, counter);
                     switch(act.getRarity()) {
                        case 1:
                           power *= 1.2F;
                           break;
                        case 2:
                           power *= 1.5;
                           break;
                        case 3:
                           power *= 1.8F;
                     }

                     power = Math.min(power, 100.0);
                     if (power > 0.0) {
                        try {
                           Item potion = ItemFactory.createItem(654, (float)power, null);
                           potion.setAuxData(potionAuxData);
                           potion.setWeight(lookingFor * templateWeight, false);
                           potion.setDescription(getTransmutationLiquidDescription(potionAuxData));

                           for(Item i : contents) {
                              Items.destroyItem(i.getWurmId());
                           }

                           source.setWeight(source.getWeightGrams() - neededLiquid, true);
                           target.insertItem(potion, true);
                           performer.getCommunicator().sendNormalServerMessage("You create some transmutation liquid.");
                           Server.getInstance().broadCastAction(performer.getName() + " creates some transmutation liquid.", performer, 5);
                        } catch (Exception var26) {
                           performer.getCommunicator().sendNormalServerMessage("You fail to create the transmutation liquid.");
                           Server.getInstance().broadCastAction(performer.getName() + " fails to create the transmutation liquid.", performer, 5);
                        }
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You fail to create the transmutation liquid.");
                        Server.getInstance().broadCastAction(performer.getName() + " fails to create the transmutation liquid.", performer, 5);
                     }
                  }

                  return done;
               }
            }
         }
      }
   }

   static final boolean askSleep(Action act, Creature performer, Item target, float counter) {
      if (!performer.isPlayer()) {
         return true;
      } else if (performer.isGuest()) {
         performer.getCommunicator().sendNormalServerMessage("Guests are not allowed to sleep in beds.");
         return true;
      } else if (((Player)performer).getSaveFile().bed > 0L) {
         performer.getCommunicator().sendNormalServerMessage("You are already asleep. Or are you sleepwalking?");
         return true;
      } else {
         PlayerInfo info = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
         if (info != null) {
            performer.getCommunicator().sendNormalServerMessage("The bed is already occupied by " + info.getName() + ".");
            return true;
         } else {
            VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
            if (t != null) {
               if (t.getStructure() == null || !t.getStructure().isTypeHouse()) {
                  performer.getCommunicator().sendNormalServerMessage("You would get no sleep outside tonight.");
                  return true;
               }

               if (!t.getStructure().isFinished()) {
                  performer.getCommunicator().sendNormalServerMessage("The house is too windy to provide protection.");
                  return true;
               }

               if (target.getWhenRented() > System.currentTimeMillis() - 86400000L) {
                  if (target.getData() != performer.getWurmId()) {
                     PlayerInfo renter = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getData());
                     if (renter != null) {
                        performer.getCommunicator().sendNormalServerMessage(renter.getName() + " has already rented that bed, so you are unable to use it.");
                        return true;
                     }

                     target.setWhenRented(0L);
                  }
               } else {
                  target.setWhenRented(0L);
               }

               if ((target.getRentCost() <= 0 || target.getData() != performer.getWurmId()) && !target.mayUseBed(performer)) {
                  performer.getCommunicator().sendNormalServerMessage("You are not allowed to sleep here.");
                  return true;
               }
            } else {
               logger.log(Level.WARNING, "Why is tile for bed at " + target.getTileX() + "," + target.getTileY() + "," + target.isOnSurface() + " null?");
            }

            SleepQuestion spm = new SleepQuestion(performer, "Go to sleep?", "Sleep:", target.getWurmId());
            spm.sendQuestion();
            return true;
         }
      }
   }

   static final boolean sleep(Action act, Creature performer, Item target, float counter) {
      boolean done = true;
      if (!performer.isPlayer()) {
         return true;
      } else if (((Player)performer).getSaveFile().bed > 0L && counter <= act.getPower()) {
         performer.getCommunicator().sendNormalServerMessage("You are already asleep. Or are you sleepwalking?");
         return true;
      } else if (target.getTopParent() != target.getWurmId()) {
         performer.getCommunicator().sendNormalServerMessage("The bed needs to be on the ground.");
         return true;
      } else {
         PlayerInfo info = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
         if (info != null && info.wurmId != performer.getWurmId()) {
            performer.getCommunicator().sendNormalServerMessage("The bed is already occupied by " + info.getName() + ".");
            performer.achievement(100);
            return true;
         } else {
            VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
            if (t == null) {
               logger.log(Level.WARNING, "Why is tile for bed at " + target.getTileX() + "," + target.getTileY() + "," + target.isOnSurface() + " null?");
            } else {
               if (performer.getPower() <= 0) {
                  if (t.getStructure() == null || !t.getStructure().isTypeHouse()) {
                     performer.getCommunicator().sendNormalServerMessage("You would get no sleep outside tonight.");
                     return true;
                  }

                  if (!t.getStructure().isFinished()) {
                     performer.getCommunicator().sendNormalServerMessage("The house is too windy to provide protection.");
                     return true;
                  }

                  if ((target.getRentCost() <= 0 || target.getData() != performer.getWurmId()) && !target.mayUseBed(performer)) {
                     performer.getCommunicator().sendNormalServerMessage("You are not allowed to sleep here.");
                     return true;
                  }

                  if (target.getData() == performer.getWurmId()) {
                  }
               }

               done = false;
            }

            if (counter == 1.0F) {
               int timetologout = ((Player)performer).getSecondsToLogout() * 10;
               act.setPower((float)((Player)performer).getSecondsToLogout());
               performer.getCommunicator().sendNormalServerMessage("You start to go to sleep.");
               Server.getInstance().broadCastAction(performer.getName() + " starts to go to sleep in " + target.getNameWithGenus() + ".", performer, 5);
               performer.sendActionControl("Sleep - logging off", true, timetologout);
            }

            if ((float)act.currentSecond() > act.getPower() && act.justTickedSecond()) {
               performer.getCommunicator().sendShutDown("You went to sleep. Sweet dreams.", true);
               ((Player)performer).getSaveFile().setBed(target.getWurmId());
               ((Player)performer).setLogout();
               Server.getInstance().broadCastAction(performer.getName() + " goes to sleep in " + target.getNameWithGenus() + ".", performer, 5);
               target.setDamage(target.getDamage() + 0.01F * target.getDamageModifier());
               if ((float)act.currentSecond() > act.getPower() + 2.0F) {
                  done = true;
                  if (performer.getCurrentTile() != null) {
                     performer.getCurrentTile().deleteCreature(performer);
                  }

                  Players.getInstance().logoutPlayer((Player)performer);
                  performer.achievement(547);
               }
            }

            return done;
         }
      }
   }

   public static boolean mayDropOnTile(int tileX, int tileY, boolean surfaced, int floorLevel) {
      VolaTile t = Zones.getTileOrNull(tileX, tileY, surfaced);
      if (t != null) {
         if (t.hasOnePerTileItem(t.getDropFloorLevel(floorLevel))) {
            return false;
         }

         if (t.getNumberOfItems(t.getDropFloorLevel(floorLevel)) >= 100) {
            return false;
         }

         if (t.getFourPerTileCount(floorLevel) >= 4) {
            return false;
         }
      }

      return true;
   }

   public static boolean mayDropOnTile(Creature performer) {
      float posX = performer.getStatus().getPositionX();
      float posY = performer.getStatus().getPositionY();
      float rot = performer.getStatus().getRotation();
      float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * 2.0F;
      float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * 2.0F;
      posX += xPosMod;
      posY += yPosMod;
      int placedX = (int)posX >> 2;
      int placedY = (int)posY >> 2;
      return mayDropOnTile(placedX, placedY, performer.isOnSurface(), performer.getFloorLevel());
   }

   public static byte[] getAllNormalWoodTypes() {
      return new byte[]{14, 37, 38, 39, 40, 41, 42, 43, 44, 45, 63, 64, 65, 66, 88, 51, 46, 47, 48, 49, 50, 71, 90, 91};
   }

   public static byte[] getAllMetalTypes() {
      return new byte[]{7, 8, 9, 10, 11, 12, 13, 30, 31, 34, 56, 57, 67, 96};
   }

   public static boolean mayDropTentOnTile(Creature performer) {
      for(int x = performer.getTileX() - 1; x <= performer.getTileX() + 1; ++x) {
         for(int y = performer.getTileY() - 1; y <= performer.getTileY() + 1; ++y) {
            VolaTile t = Zones.getTileOrNull(Zones.safeTileX(x), Zones.safeTileY(y), performer.isOnSurface());
            if (t != null) {
               if (t.getVillage() != null && t.getVillage() != performer.getCitizenVillage()) {
                  return false;
               }

               if (Villages.getVillageWithPerimeterAt(Zones.safeTileX(x), Zones.safeTileY(y), true) != null) {
                  return false;
               }
            }

            if (performer.isOnSurface()) {
               int tile = Server.surfaceMesh.getTile(Zones.safeTileX(x), Zones.safeTileY(y));
               if (Tiles.decodeHeight(tile) < 1) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   static final boolean setRent(Action act, Creature performer, Item target) {
      if (!target.isBed()) {
         performer.getCommunicator().sendNormalServerMessage("You may not rent that!");
         return true;
      } else {
         VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
         if (t != null) {
            if (t.getStructure() == null) {
               performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
               return true;
            }

            if (t.getStructure().isOwner(performer)) {
               if (act.getNumber() == 319) {
                  target.setAuxData((byte)1);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 1 copper.");
               } else if (act.getNumber() == 320) {
                  target.setAuxData((byte)2);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 copper.");
               } else if (act.getNumber() == 321) {
                  target.setAuxData((byte)3);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 1 silver.");
               } else if (act.getNumber() == 322) {
                  target.setAuxData((byte)4);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 silver.");
               } else if (act.getNumber() == 365) {
                  target.setAuxData((byte)5);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 10 iron.");
               } else if (act.getNumber() == 366) {
                  target.setAuxData((byte)6);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 25 iron.");
               } else if (act.getNumber() == 367) {
                  target.setAuxData((byte)7);
                  performer.getCommunicator().sendNormalServerMessage("You set the rent to 50 iron.");
               } else if (act.getNumber() == 323) {
                  target.setAuxData((byte)0);
                  performer.getCommunicator().sendNormalServerMessage("The bed may now only be used by you and your guests.");
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You do not own this house, so you can not charge for the bed.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
         }

         return true;
      }
   }

   static final boolean rent(Action act, Creature performer, Item target) {
      if (!target.isBed()) {
         performer.getCommunicator().sendNormalServerMessage("You may not rent that!");
         return true;
      } else if (performer.isGuest()) {
         performer.getCommunicator().sendNormalServerMessage("Guests are not allowed to sleep in or hire beds.");
         return true;
      } else {
         if (performer.isPlayer()) {
            try {
               long oldbed = ((Player)performer).getSaveFile().bed;
               if (oldbed > 0L && oldbed != target.getWurmId()) {
                  Item beds = Items.getItem(oldbed);
                  beds.setData(0L);
                  beds.setWhenRented(0L);
                  performer.getCommunicator().sendNormalServerMessage("Your old bed is now available for someone else to rent.");
               }
            } catch (NoSuchItemException var13) {
            }
         }

         PlayerInfo pinf = PlayerInfoFactory.getPlayerSleepingInBed(target.getWurmId());
         if (pinf != null) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "Some kind of mysterious haze lingers over the bed, and you notice that the bed is occupied by the spirit of " + pinf.getName() + "."
               );
            return true;
         } else {
            if (target.getData() > 0L && target.getData() != performer.getWurmId()) {
               PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(target.getData());
               if (info != null) {
                  if (target.getWhenRented() > System.currentTimeMillis() - 86400000L) {
                     performer.getCommunicator().sendNormalServerMessage(info.getName() + " has already rented that bed.");
                     return true;
                  }

                  target.setWhenRented(0L);
               }
            }

            VolaTile t = Zones.getTileOrNull(target.getTileX(), target.getTileY(), target.isOnSurface());
            if (t != null) {
               if (t.getStructure() == null) {
                  performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not rent the bed.");
                  return true;
               }

               if (t.getStructure().getOwnerId() == performer.getWurmId()) {
                  performer.getCommunicator().sendNormalServerMessage("You don't need to rent the bed in order to sleep in it.");
                  return true;
               }

               Village vill = t.getVillage();
               long rentMoney = (long)target.getRentCost();
               if (rentMoney > 0L) {
                  LoginServerWebConnection conn = new LoginServerWebConnection();

                  try {
                     if (charge(performer, rentMoney)) {
                        if (vill != null && vill.plan != null) {
                           vill.plan.addMoney(rentMoney / 2L);
                           rentMoney /= 2L;
                        }

                        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(t.getStructure().getOwnerId());
                        if (info != null) {
                           conn.addMoney(t.getStructure().getOwnerId(), info.getName(), rentMoney, "Rented bed");
                        }

                        target.setData(performer.getWurmId());
                        target.setWhenRented(System.currentTimeMillis());
                        performer.getCommunicator().sendNormalServerMessage("You rent the bed.");
                        if (info != null) {
                           try {
                              Player p = Players.getInstance().getPlayer(info.wurmId);
                              p.getCommunicator()
                                 .sendNormalServerMessage("You rent a bed in " + t.getStructure().getName() + " to " + performer.getName() + ".");
                           } catch (NoSuchPlayerException var11) {
                           }
                        }

                        return false;
                     }

                     performer.getCommunicator().sendNormalServerMessage("You cannot rent the bed right now. Make sure you have enough money in the bank.");
                     return true;
                  } catch (Exception var12) {
                     performer.getCommunicator().sendNormalServerMessage(var12.getMessage());
                     return true;
                  }
               }

               logger.log(Level.INFO, "No Rent cost=" + target.getRentCost());
            } else {
               performer.getCommunicator().sendNormalServerMessage("There is no structure here, so you can not charge for the bed.");
            }

            return true;
         }
      }
   }

   private static final boolean charge(Creature responder, long coinsNeeded) throws FailedException {
      Item[] items = responder.getInventory().getAllItems(false);
      List<Item> coins = new LinkedList<>();
      long value = 0L;

      for(Item lItem : items) {
         if (lItem.isCoin()) {
            coins.add(lItem);
            value += (long)Economy.getValueFor(lItem.getTemplateId());
         }
      }

      if (value < coinsNeeded) {
         Change change = new Change(coinsNeeded);
         throw new FailedException("You need " + change.getChangeString() + " coins.");
      } else {
         long curv = 0L;

         for(Item coin : coins) {
            curv += (long)Economy.getValueFor(coin.getTemplateId());

            try {
               Item parent = coin.getParent();
               parent.dropItem(coin.getWurmId(), false);
               Economy.getEconomy().returnCoin(coin, "Charged");
            } catch (NoSuchItemException var18) {
               logger.log(
                  Level.WARNING,
                  responder.getName()
                     + ":  Failed to locate the container for coin "
                     + coin.getName()
                     + ". Value returned is "
                     + new Change(curv).getChangeString()
                     + " coins."
               );
               Item[] newCoins = Economy.getEconomy().getCoinsFor((long)Economy.getValueFor(coin.getTemplateId()));
               Item inventory = responder.getInventory();

               for(Item lNewCoin : newCoins) {
                  inventory.insertItem(lNewCoin);
               }

               throw new FailedException(
                  "Failed to locate the container for coin "
                     + coin.getName()
                     + ". This is serious and should be reported. Returned "
                     + new Change(curv).getChangeString()
                     + " coins."
               );
            }

            if (curv >= coinsNeeded) {
               break;
            }
         }

         if (curv > coinsNeeded) {
            Item[] newCoins = Economy.getEconomy().getCoinsFor(curv - coinsNeeded);
            Item inventory = responder.getInventory();

            for(Item lNewCoin : newCoins) {
               inventory.insertItem(lNewCoin);
            }
         }

         return true;
      }
   }

   static final boolean watchSpyglass(Creature performer, Item spyglass, Action act, float counter) {
      boolean done = false;
      if (act.currentSecond() == 1) {
         performer.getCommunicator().sendNormalServerMessage("You start spying through the " + spyglass.getName() + ".");
         Server.getInstance().broadCastAction(performer.getName() + " starts spying through " + spyglass.getNameWithGenus() + ".", performer, 5);
         performer.sendActionControl(Actions.actionEntrys[329].getVerbString(), true, 300);
      } else if (act.currentSecond() >= 30) {
         done = true;
         performer.getCommunicator().sendNormalServerMessage("You stop spying through the " + spyglass.getName() + ".");
         Server.getInstance().broadCastAction(performer.getName() + " stops spying through " + spyglass.getNameWithGenus() + ".", performer, 5);
      }

      return done;
   }

   public static final boolean mayUseInventoryOfVehicle(Creature performer, Item vehicle) {
      return performer.getWurmId() == vehicle.lastOwner ? true : vehicle.mayAccessHold(performer);
   }

   public static final boolean hasKeyForContainer(Creature performer, Item target) {
      try {
         Item lock = Items.getItem(target.getLockId());
         if (!lock.getLocked() || target.isOwner(performer)) {
            return true;
         }

         long[] keys = lock.getKeyIds();

         for(long keyId : keys) {
            Item key = Items.getItem(keyId);
            if (key.getTopParent() == performer.getInventory().getWurmId()) {
               return true;
            }
         }
      } catch (NoSuchItemException var10) {
         logger.log(Level.WARNING, "Item not found for key " + var10.getMessage(), (Throwable)var10);
      }

      return false;
   }

   static boolean useBellTower(Creature performer, Item bellTower, Action act, float counter) {
      boolean toReturn = false;
      if (counter == 1.0F) {
         performer.getCommunicator().sendNormalServerMessage("You start swinging the bell.");
         Server.getInstance().broadCastAction(performer.getName() + " starts to swing the bell.", performer, 5);
         performer.sendActionControl("playing", true, 600);
         act.setTimeLeft(600);
         performer.getStatus().modifyStamina(-1000.0F);
      } else if (act.currentSecond() % 10 == 0) {
         performer.getStatus().modifyStamina(-1000.0F);
         String soundName = "sound.bell.dong.1";
         int sound = (int)(bellTower.getCurrentQualityLevel() / 10.0F);
         if (sound < 3) {
            soundName = "sound.bell.dong.1";
         } else if (sound < 5) {
            soundName = "sound.bell.dong.2";
         } else if (sound < 7) {
            soundName = "sound.bell.dong.3";
         } else if (sound < 8) {
            soundName = "sound.bell.dong.4";
         } else {
            soundName = "sound.bell.dong.5";
         }

         SoundPlayer.playSound(soundName, bellTower, 3.0F);
         bellTower.setDamage(bellTower.getDamage() + 0.001F);
      }

      if (counter > 60.0F) {
         toReturn = true;
      }

      return toReturn;
   }

   static boolean usePendulum(Creature performer, Item pendulum, Action act, float counter) {
      boolean toReturn = false;
      int time = 200;
      Skills skills = performer.getSkills();
      Skill primSkill = null;

      try {
         primSkill = skills.getSkill(106);
      } catch (Exception var9) {
         primSkill = skills.learn(106, 1.0F);
      }

      if (counter == 1.0F) {
         time = Actions.getStandardActionTime(performer, primSkill, pendulum, 0.0);
         act.setTimeLeft(time);
         performer.getCommunicator().sendNormalServerMessage("You concentrate on the " + pendulum.getName() + ".");
         Server.getInstance().broadCastAction(performer.getName() + " concentrates on the " + pendulum.getName() + ".", performer, 5);
         performer.sendActionControl("Concentrating", true, time);
         performer.getStatus().modifyStamina(-1000.0F);
      } else {
         time = act.getTimeLeft();
      }

      if (act.currentSecond() == 5) {
         performer.getStatus().modifyStamina(-1000.0F);
      }

      if (counter * 10.0F > (float)time) {
         toReturn = true;
         if (pendulum.isLocateItem()) {
            Locates.useLocateItem(performer, pendulum, primSkill);
         } else {
            Locates.locateSpring(performer, pendulum, primSkill);
         }

         pendulum.setDamage(pendulum.getDamage() + 0.01F);
      }

      return toReturn;
   }

   static boolean puppetSpeak(Creature performerOne, Item puppetOne, Item puppetTwo, Action act, float counter) {
      if (puppetTwo == null) {
         performerOne.getCommunicator().sendNormalServerMessage("The " + puppetOne.getName() + " needs someone to speak with.");
         return true;
      } else {
         try {
            puppetOne.getOwner();
            puppetTwo.getOwner();
            if (puppetOne.equals(puppetTwo)) {
               performerOne.getCommunicator().sendNormalServerMessage("The " + puppetOne.getName() + " is not interested in speaking to itself.");
               return true;
            }
         } catch (NotOwnedException var14) {
            performerOne.getCommunicator()
               .sendNormalServerMessage("Both the " + puppetOne.getName() + " and the " + puppetTwo.getName() + " need to be held.");
            return true;
         }

         boolean toReturn = false;
         int time = 0;

         Creature performerTwo;
         try {
            performerTwo = Server.getInstance().getCreature(puppetTwo.getOwnerId());
         } catch (NoSuchCreatureException var12) {
            performerOne.getCommunicator().sendNormalServerMessage("The " + puppetTwo.getName() + " must be played by someone.");
            return true;
         } catch (NoSuchPlayerException var13) {
            performerOne.getCommunicator().sendNormalServerMessage("The " + puppetTwo.getName() + " must be played by someone.");
            return true;
         }

         if (performerOne.getPower() > 0 && performerOne.getPower() < 5 && Servers.localServer.testServer) {
            performerOne.getCommunicator().sendNormalServerMessage("Nothing happens.");
            return true;
         } else if (performerTwo.getPower() > 0 && performerTwo.getPower() < 5 && Servers.localServer.testServer) {
            performerOne.getCommunicator().sendNormalServerMessage("Nothing happens.");
            return true;
         } else {
            int conversationCounter = 0;
            if (counter == 1.0F) {
               if (!puppetTwo.isPuppet()) {
                  performerOne.getCommunicator()
                     .sendNormalServerMessage("The " + puppetOne.getName() + " does not want to speak to " + puppetTwo.getName() + ".");
                  return true;
               }

               if (!Puppet.mayPuppetMaster(performerOne)) {
                  performerOne.getCommunicator().sendNormalServerMessage("You are still mentally exhausted from your last show.");
                  return true;
               }

               int nums = Puppet.getConversationLength(true, act, puppetOne, puppetTwo, performerOne, performerTwo, 0);
               int nums2 = Puppet.getConversationLength(true, act, puppetTwo, puppetOne, performerTwo, performerOne, 0);
               int seconds = nums + nums2;
               time = (seconds + 2) * 5 * 10;
               act.setPower((float)conversationCounter);
               if (performerTwo.equals(performerOne)) {
                  performerOne.getCommunicator()
                     .sendNormalServerMessage("You start playing with the " + puppetOne.getName() + " and " + puppetTwo.getName() + ".");
                  Server.getInstance()
                     .broadCastAction(
                        performerOne.getName() + " starts playing with " + puppetOne.getNameWithGenus() + " " + puppetTwo.getNameWithGenus() + ".",
                        performerOne,
                        5
                     );
               } else {
                  if (!Puppet.mayPuppetMaster(performerTwo)) {
                     performerTwo.getCommunicator().sendNormalServerMessage("You are still mentally exhausted from your last show.");
                     return true;
                  }

                  if (performerTwo.getPower() < 5) {
                     Puppet.startPuppeteering(performerTwo);
                  }

                  performerTwo.sendActionControl("Puppeteering", true, time);
                  performerOne.getCommunicator()
                     .sendNormalServerMessage("You start playing with the " + puppetOne.getName() + " and the " + puppetTwo.getName() + ".");
                  performerTwo.getCommunicator().sendNormalServerMessage(performerOne.getName() + " starts playing with your " + puppetTwo.getName() + ".");
                  Server.getInstance()
                     .broadCastAction(performerOne.getName() + " starts a show with " + performerTwo.getName() + ".", performerOne, performerTwo, 5);
               }

               if (performerOne.getPower() < 5) {
                  Puppet.startPuppeteering(performerOne);
               }

               performerOne.sendActionControl("Puppeteering", true, time);
            } else {
               conversationCounter = (int)act.getPower();
            }

            if (act.currentSecond() % 5 == 0) {
               performerOne.getStatus().modifyStamina(-500.0F);
               performerTwo.getStatus().modifyStamina(-500.0F);
               if (act.currentSecond() % 10 == 0) {
                  toReturn = Puppet.sendConversationString(act, puppetTwo, puppetOne, performerTwo, performerOne, conversationCounter);
                  if (toReturn) {
                     toReturn = false;
                     if (act.getTimeLeft() > 0) {
                        if (act.getTimeLeft() >= 100) {
                           toReturn = true;
                           act.setTimeLeft(125);
                        }
                     } else {
                        act.setTimeLeft(25);
                     }
                  }

                  puppetTwo.setDamage(puppetTwo.getDamage() + puppetTwo.getDamageModifier() * 0.02F);
               } else {
                  toReturn = Puppet.sendConversationString(act, puppetOne, puppetTwo, performerOne, performerTwo, conversationCounter);
                  if (toReturn) {
                     toReturn = false;
                     if (act.getTimeLeft() > 0) {
                        if (act.getTimeLeft() < 100) {
                           toReturn = true;
                           act.setTimeLeft(125);
                        }
                     } else {
                        act.setTimeLeft(100);
                     }
                  }

                  puppetOne.setDamage(puppetOne.getDamage() + puppetOne.getDamageModifier() * 0.02F);
               }

               if (act.getFailSecond() == 1.0F || act.currentSecond() % 10 == 0 || puppetOne.getTemplateId() == puppetTwo.getTemplateId()) {
                  act.setFailSecond(10.0F);
                  ++conversationCounter;
               }

               act.setPower((float)conversationCounter);
               if (toReturn && !performerTwo.equals(performerOne)) {
                  performerTwo.sendActionControl("Puppeteering", false, 0);
               }
            }

            return toReturn;
         }
      }
   }

   public static String getColorDesc(int color) {
      return " Colors: R=" + WurmColor.getColorRed(color) + ", G=" + WurmColor.getColorGreen(color) + ", B=" + WurmColor.getColorBlue(color) + ".";
   }

   public static String getImpDesc(@Nonnull Creature performer, @Nonnull Item item) {
      String toReturn = "";
      if (performer.getPlayingTime() < 604800000L) {
         toReturn = " It can not be improved.";
      }

      if (item.isRepairable() && item.creationState == 0 && !item.isNoImprove()) {
         int skillNum = getImproveSkill(item);
         int templateId = getImproveTemplateId(item);
         if (skillNum != -10) {
            try {
               ItemTemplate temp = ItemTemplateFactory.getInstance().getTemplate(templateId);
               toReturn = " It could be improved with " + temp.getNameWithGenus() + ".";
            } catch (NoSuchTemplateException var6) {
            }
         }
      } else if (item.creationState != 0) {
         toReturn = ' ' + getNeededCreationAction(getImproveMaterial(item), item.creationState, item);
      }

      return toReturn;
   }

   public static List<String> getEnhancementStrings(Item item) {
      List<String> strings = new ArrayList<>();
      if (item.enchantment != 0) {
         Spell ench = Spells.getEnchantment(item.enchantment);
         if (ench != null) {
            strings.add("It is enchanted with " + ench.name + ", and " + ench.effectdesc);
         }
      }

      ItemSpellEffects eff = item.getSpellEffects();
      if (eff != null) {
         SpellEffect[] speffs = eff.getEffects();

         for(int x = 0; x < speffs.length; ++x) {
            if ((long)speffs[x].type < -10L) {
               strings.add("A single " + speffs[x].getName() + " has been attached to it, so it " + speffs[x].getLongDesc());
            } else {
               strings.add(speffs[x].getName() + " has been cast on it, so it " + speffs[x].getLongDesc() + " [" + (int)speffs[x].power + "]");
            }
         }
      }

      return strings;
   }

   public static final void sendEnchantmentStrings(Item item, Creature creature) {
      if (creature != null) {
         if (item != null) {
            for(String s : getEnhancementStrings(item)) {
               creature.getCommunicator().sendNormalServerMessage(s);
            }
         }
      }
   }

   public static byte getTransmutationLiquidAuxByteFor(Item source, Item target) {
      if (source.getTemplateId() == 417) {
         if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled()) {
            if (target.getTemplateId() == 220 && source.getRealTemplateId() == 6) {
               return 1;
            }

            if (target.getTemplateId() == 204 && source.getRealTemplateId() == 410) {
               return 2;
            }

            if (target.getTemplateId() == 46 && source.getRealTemplateId() == 409) {
               return 3;
            }

            if (target.getTemplateId() == 47 && source.getRealTemplateId() == 1283) {
               return 7;
            }
         }

         if (target.getTemplateId() == 48 && source.getRealTemplateId() == 410) {
            return 4;
         }

         if (target.getTemplateId() == 479 && source.getRealTemplateId() == 409) {
            return 5;
         }

         if (target.getTemplateId() == 49 && source.getRealTemplateId() == 6) {
            return 6;
         }

         if (target.getTemplateId() == 46 && source.getRealTemplateId() == 1196) {
            return 8;
         }
      }

      return 0;
   }

   public static String getTransmutationLiquidDescription(byte auxData) {
      if (Features.Feature.TRANSFORM_TO_RESOURCE_TILES.isEnabled()) {
         switch(auxData) {
            case 1:
               return "Transmutes sand to clay";
            case 2:
               return "Transmutes grass or mycelium to peat";
            case 3:
               return "Transmutes steppe to tar";
            case 4:
            case 5:
            case 6:
            default:
               break;
            case 7:
               return "Transmutes moss to tundra";
         }
      }

      switch(auxData) {
         case 4:
            return "Transmutes clay to dirt";
         case 5:
            return "Transmutes peat to dirt";
         case 6:
            return "Transmutes tar to dirt";
         case 7:
         default:
            return "";
         case 8:
            return "Transmutes tundra to dirt";
      }
   }

   public static int getTransmutationLiquidDifficulty(byte auxData) {
      switch(auxData) {
         case 1:
            return 30;
         case 2:
            return 20;
         case 3:
            return 10;
         case 4:
            return 35;
         case 5:
            return 25;
         case 6:
            return 15;
         case 7:
            return 5;
         case 8:
            return 10;
         default:
            return 1;
      }
   }

   static int getTransmutationSolidTemplateWeightGrams(byte auxData) {
      int templateId = 46;
      short var4;
      switch(auxData) {
         case 1:
            var4 = 220;
            break;
         case 2:
            var4 = 204;
            break;
         case 3:
            var4 = 46;
            break;
         case 4:
            var4 = 48;
            break;
         case 5:
            var4 = 479;
            break;
         case 6:
            var4 = 49;
            break;
         case 7:
            var4 = 47;
            break;
         case 8:
            var4 = 46;
            break;
         default:
            var4 = 46;
      }

      try {
         ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(var4);
         return it.getWeightGrams();
      } catch (NoSuchTemplateException var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         return 1000;
      }
   }

   static byte getTransmutedToTileType(byte auxData) {
      switch(auxData) {
         case 1:
            return Tiles.Tile.TILE_CLAY.id;
         case 2:
            return Tiles.Tile.TILE_PEAT.id;
         case 3:
            return Tiles.Tile.TILE_TAR.id;
         case 4:
            return Tiles.Tile.TILE_DIRT.id;
         case 5:
            return Tiles.Tile.TILE_DIRT.id;
         case 6:
            return Tiles.Tile.TILE_DIRT.id;
         case 7:
            return Tiles.Tile.TILE_TUNDRA.id;
         case 8:
            return Tiles.Tile.TILE_DIRT.id;
         default:
            return Tiles.Tile.TILE_DIRT.id;
      }
   }

   static float getTransmutationMod(Creature performer, int tilex, int tiley, byte auxData, boolean reverting) {
      float mod = 1.0F;
      switch(auxData) {
         case 1:
            if (Servers.isThisAPvpServer()) {
               mod *= 1.5F;
            }
            break;
         case 2:
            if (Servers.isThisAPvpServer()) {
               mod *= 1.5F;
            }
            break;
         case 3:
            if (Servers.isThisAPvpServer()) {
               mod *= 1.5F;
            }
            break;
         case 4:
            if (reverting) {
               mod /= 2.0F;
            }
            break;
         case 5:
            if (reverting) {
               mod /= 2.0F;
            }
            break;
         case 6:
            if (reverting) {
               mod /= 2.0F;
            }
            break;
         case 7:
            mod /= 5.0F;
            if (reverting) {
               mod /= 2.0F;
            }
            break;
         case 8:
            mod /= 5.0F;
            if (Servers.isThisAPvpServer()) {
               mod *= 1.5F;
            }
      }

      Village vp = Villages.getVillageWithPerimeterAt(tilex, tiley, true);
      if (vp != null && (vp.isCitizen(performer) || vp.isAlly(performer) || vp.isEnemy(performer))) {
         mod /= 1.5F;
      }

      return mod;
   }

   public static boolean wouldDestroyLiquid(Item container, Item contained, Item target) {
      if (container.getTemplate().isContainerWithSubItems() && contained.isPlacedOnParent()) {
         return false;
      } else if (!contained.isFood() && !contained.isLiquid() && !contained.isRecipeItem()) {
         return true;
      } else if ((contained.isFood() || contained.isRecipeItem()) && !contained.isLiquid()) {
         return false;
      } else {
         ItemTemplate ct = contained.isBulkItem() ? contained.getRealTemplate() : contained.getTemplate();
         int cid = ct.getTemplateId();
         ItemTemplate rt = target.isBulkItem() ? target.getRealTemplate() : target.getTemplate();
         int tid = rt.getTemplateId();
         if (rt.isFood() && !rt.isLiquid()) {
            return false;
         } else {
            if (container.isFoodMaker()
               && (container.getTemplateId() != 768 || !rt.isLiquid() || !contained.isLiquid())
               && contained.getTemplate().isLiquidCooking()
               && rt.isLiquidCooking()
               && container.getTemplateId() != 76) {
               if (container.getTemplateId() != 75) {
                  return false;
               }

               if (rt.getFoodGroup() == 1263 && cid == tid) {
                  return false;
               }

               if (tid == 1212 || cid == 1212) {
                  return false;
               }
            }

            if (cid != tid) {
               return true;
            } else if (contained.getAuxData() != target.getAuxData()) {
               return true;
            } else if (contained.getBless() != target.getBless()) {
               return true;
            } else if (contained.getRarity() != target.getRarity()) {
               return true;
            } else {
               return !target.isBulkItem() && contained.getRealTemplateId() != target.getRealTemplateId();
            }
         }
      }
   }

   public static void setSizes(@Nullable Item container, int newWeight, Item item) {
      float mod = (float)newWeight / (float)item.getWeightGrams();
      int maxSizeMod = 4;
      ItemTemplate template = item.getTemplate();
      int sizeX = 0;
      int sizeY = 0;
      int sizeZ = 0;
      if (mod > 64.0F) {
         sizeX = template.getSizeX() * 4;
         sizeY = template.getSizeY() * 4;
         sizeZ = template.getSizeZ() * 4;
      } else if (mod > 16.0F) {
         mod = mod / 4.0F * 4.0F;
         sizeX = (int)((float)template.getSizeX() * mod);
         sizeY = template.getSizeY() * 4;
         sizeZ = template.getSizeZ() * 4;
      } else if (mod > 4.0F) {
         sizeX = template.getSizeX();
         sizeY = (int)((float)template.getSizeY() * mod);
         sizeZ = template.getSizeZ() * 4;
         mod /= 4.0F;
      } else {
         sizeX = Math.max(1, (int)((float)template.getSizeX() * mod));
         sizeY = Math.max(1, (int)((float)template.getSizeY() * mod));
         sizeZ = Math.max(1, (int)((float)template.getSizeZ() * mod));
      }

      if (container != null) {
         sizeX = Math.min(sizeX, container.getSizeX());
         sizeY = Math.min(sizeY, container.getSizeY());
         sizeZ = Math.min(sizeZ, container.getSizeZ());
      }

      item.setSizes(sizeX, sizeY, sizeZ);
   }

   public static Point4f getHivePos(int tilex, int tiley, Tiles.Tile theTile, byte data) {
      byte age = FoliageAge.getAgeAsByte(data);
      float treex = 2.0F;
      float treey = 2.0F;
      if (!TreeData.isCentre(data)) {
         treex = TerrainUtilities.getTreePosX(tilex, tiley) * 4.0F;
         treey = TerrainUtilities.getTreePosY(tilex, tiley) * 4.0F;
      }

      float posx = (float)(tilex * 4) + treex;
      float posy = (float)(tiley * 4) + treey;
      float dir = Creature.normalizeAngle(TerrainUtilities.getTreeRotation(tilex, tiley));

      try {
         float basePosZ = Zones.calculateHeight(posx, posy, true);
         float tht = theTile.getTreeImageHeight(data);
         float posz = basePosZ + tht * (float)(age + 1) * 0.4F;
         return new Point4f(posx, posy, posz, dir);
      } catch (NoSuchZoneException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
         return new Point4f(posx, posy, 0.0F, dir);
      }
   }

   public static float getAverageQL(@Nullable Item source, Item target) {
      int count = 0;
      float totalQL = 0.0F;
      if (source != null && !source.isCookingTool() && !source.isRecipeItem()) {
         ++count;
         float rarityMult = 1.0F + (float)(source.getRarity() * source.getRarity()) * 0.1F;
         totalQL += source.getCurrentQualityLevel() * rarityMult;
      }

      if (!target.isFoodMaker() && !target.getTemplate().isCooker()) {
         ++count;
         float rarityMult = 1.0F + (float)(target.getRarity() * target.getRarity()) * 0.1F;
         totalQL += target.getCurrentQualityLevel() * rarityMult;
      } else {
         Item[] items = target.getItemsAsArray();

         for(Item item : items) {
            ++count;
            float rarityMult = 1.0F + (float)(item.getRarity() * item.getRarity()) * 0.1F;
            totalQL += item.getCurrentQualityLevel() * rarityMult;
         }

         float rarityMult = 1.0F + (float)(target.getRarity() * target.getRarity()) * 0.1F;
         totalQL += target.getCurrentQualityLevel() * rarityMult / (float)Math.max(1, 11 - items.length);
      }

      return count > 0 ? Math.min(99.995F, Math.max(1.0F, totalQL / (float)count)) : 1.0F;
   }
}
