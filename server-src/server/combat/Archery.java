package com.wurmonline.server.combat;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Archery implements MiscConstants, ItemMaterials, SoundNames, TimeConstants {
   public static final int BREAKNUM = 2;
   private static final Logger logger = Logger.getLogger(Archery.class.getName());
   private static boolean fnd = false;
   private static final float swimDepth = 1.4F;

   private Archery() {
   }

   public static boolean attack(Creature performer, Creature defender, Item bow, float counter, Action act) {
      boolean done = false;
      if (defender.isInvulnerable()) {
         performer.getCommunicator().sendCombatNormalMessage("You can't attack " + defender.getNameWithGenus() + " right now.");
         return true;
      } else if (defender.equals(performer)) {
         return true;
      } else if ((double)performer.getPositionZ() < -0.5 && performer.getVehicle() == -10L) {
         performer.getCommunicator().sendCombatNormalMessage("You are too deep in the water to fire a bow.");
         return true;
      } else {
         if (performer.getPrimWeapon() != bow) {
            Item[] weps = performer.getSecondaryWeapons();
            fnd = false;

            for(int x = 0; x < weps.length; ++x) {
               if (weps[x] == bow) {
                  fnd = true;
               }
            }

            if (!fnd) {
               performer.getCommunicator().sendCombatNormalMessage("You need to wield the bow in order to use it.");
               return true;
            }
         }

         if (performer.getShield() != null) {
            performer.getCommunicator().sendCombatNormalMessage("You can not use the bow while wearing a shield.");
            return true;
         } else {
            int minRange = getMinimumRangeForBow(bow);
            double maxRange = 180.0;
            if (Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY()) < (double)minRange) {
               performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too close.");
               return true;
            } else if (Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY()) > 180.0) {
               performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too far away.");
               return true;
            } else if (defender.isDead()) {
               return true;
            } else {
               if (bow.isWeaponBow()) {
                  Skill archery = null;
                  Skill bowskill = null;

                  try {
                     int skillnum = bow.getPrimarySkill();
                     if ((long)skillnum != -10L) {
                        try {
                           bowskill = performer.getSkills().getSkill(skillnum);
                        } catch (NoSuchSkillException var42) {
                           bowskill = performer.getSkills().learn(skillnum, 1.0F);
                        }
                     }
                  } catch (NoSuchSkillException var44) {
                     performer.getCommunicator()
                        .sendCombatNormalMessage("This weapon has no skill attached. Please report this bug using the forums or the /dev channel.");
                     return true;
                  }

                  try {
                     archery = performer.getSkills().getSkill(1030);
                  } catch (NoSuchSkillException var41) {
                     archery = performer.getSkills().learn(1030, 1.0F);
                  }

                  if (bowskill == null || archery == null) {
                     performer.getCommunicator()
                        .sendCombatNormalMessage("There was a bug with the skill for this item. Please report this bug using the forums or the /dev channel.");
                     return true;
                  }

                  if (!Servers.isThisAPvpServer() && defender.getBrandVillage() != null && !defender.getBrandVillage().mayAttack(performer, defender)) {
                     performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is protected by its branding.");
                     return true;
                  }

                  if (performer.currentVillage != null && !performer.currentVillage.mayAttack(performer, defender) && performer.isLegal()) {
                     performer.getCommunicator().sendCombatNormalMessage("The permissions for the settlement you are on prevents this.");
                     return true;
                  }

                  if (defender.currentVillage != null && !defender.currentVillage.mayAttack(performer, defender) && performer.isLegal()) {
                     performer.getCommunicator().sendCombatNormalMessage("The permissions for the settlement that the target is on prevents this.");
                     return true;
                  }

                  int time = 200;
                  if (counter == 1.0F) {
                     if (performer.isOnSurface() != defender.isOnSurface()) {
                        performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                        return true;
                     }

                     Item arrow = getArrow(performer);
                     if (arrow == null) {
                        performer.getCommunicator().sendCombatNormalMessage("You have no arrows left to shoot!");
                        return true;
                     }

                     if (defender.isGhost() && (arrow.getSpellEffects() == null || arrow.getSpellEffects().getEffects().length == 0)) {
                        performer.getCommunicator().sendCombatNormalMessage("An unenchanted arrow would not harm the " + defender.getNameWithGenus() + ".");
                        return true;
                     }

                     time = Actions.getQuickActionTime(performer, archery, bow, 0.0);
                     time = Math.max(50, time);
                     if (act.getNumber() == 125 && time > 50) {
                        time = (int)Math.max(20.0F, (float)time * 0.8F);
                     }

                     performer.setStealth(false);
                     performer.getCommunicator().sendCombatNormalMessage("You start aiming at " + defender.getNameWithGenus() + ".");
                     if (WurmCalendar.getHour() > 20 || WurmCalendar.getHour() < 5) {
                        performer.getCommunicator()
                           .sendCombatNormalMessage("The dusk makes it harder to get a clear view of " + defender.getNameWithGenus() + ".");
                     }

                     performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, time);
                     Server.getInstance()
                        .broadCastAction(performer.getName() + " draws an arrow and raises " + performer.getHisHerItsString() + " bow.", performer, 5);
                     SoundPlayer.playSound("sound.arrow.aim", performer, 1.6F);
                     act.setTimeLeft(time);
                  } else {
                     time = act.getTimeLeft();
                  }

                  if (counter * 10.0F > (float)time || performer.getPower() > 0) {
                     done = true;
                     performer.getStatus().modifyStamina(-1000.0F);
                     if (performer.isOnSurface() != defender.isOnSurface()) {
                        performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                        return true;
                     }

                     boolean limitFail = false;
                     if (performer.getArmourLimitingFactor() < 0.0F
                        && Server.rand.nextFloat() < Math.abs(performer.getArmourLimitingFactor() * ItemBonus.getArcheryPenaltyReduction(performer))) {
                        limitFail = true;
                     }

                     Fence fence = null;
                     Zones.resetCoverHolder();
                     boolean isAttackingPenned = false;
                     if (performer.isOnSurface()
                        && !performer.isWithinDistanceTo(defender, 110.0F)
                        && defender.getCurrentTile() != null
                        && defender.getCurrentTile().getStructure() != null
                        && defender.getCurrentTile().getStructure().isFinalFinished()) {
                        performer.getCommunicator()
                           .sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + " inside the structure.");
                        return true;
                     }

                     BlockingResult result = Blocking.getRangedBlockerBetween(performer, defender);
                     if (result != null) {
                        if (!performer.isOnPvPServer() || !defender.isOnPvPServer()) {
                           performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                           return true;
                        }

                        for(Blocker b : result.getBlockerArray()) {
                           if (b.getBlockPercent(performer) >= 100.0F) {
                              performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                              return true;
                           }
                        }

                        if (!defender.isPlayer() && result.getTotalCover() > 0.0F) {
                           isAttackingPenned = true;
                        }
                     }

                     if (!defender.isPlayer() && defender.getFloorLevel() != performer.getFloorLevel()) {
                        isAttackingPenned = true;
                     }

                     if (!VirtualZone.isCreatureTurnedTowardsTarget(defender, performer, 60.0F, false)) {
                        performer.getCommunicator().sendCombatNormalMessage("You must turn towards " + defender.getNameWithGenus() + " in order to shoot it.");
                        return true;
                     }

                     int minRange1 = getMinimumRangeForBow(bow);
                     if (Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY()) < (double)minRange1) {
                        performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too close.");
                        return true;
                     }

                     int trees = 0;
                     int treetilex = -1;
                     int treetiley = -1;
                     int tileArrowDownX = -1;
                     int tileArrowDownY = -1;
                     PathFinder pf = new PathFinder(true);

                     try {
                        Path path = pf.rayCast(
                           performer.getCurrentTile().tilex,
                           performer.getCurrentTile().tiley,
                           defender.getCurrentTile().tilex,
                           defender.getCurrentTile().tiley,
                           performer.isOnSurface(),
                           ((int)Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY()) >> 2) + 5
                        );
                        float initialHeight = Math.max(-1.4F, performer.getPositionZ() + performer.getAltOffZ() + 1.4F);
                        float targetHeight = Math.max(-1.4F, defender.getPositionZ() + defender.getAltOffZ() + 1.4F);
                        double distx = Math.pow((double)(performer.getCurrentTile().tilex - defender.getCurrentTile().tilex), 2.0);
                        double disty = Math.pow((double)(performer.getCurrentTile().tiley - defender.getCurrentTile().tiley), 2.0);
                        double dist = Math.sqrt(distx + disty);

                        for(double dx = (double)(targetHeight - initialHeight) / dist; !path.isEmpty(); path.removeFirst()) {
                           PathTile p = path.getFirst();
                           if (Tiles.getTile(Tiles.decodeType(p.getTile())).isTree()) {
                              if (treetilex == -1 && Server.rand.nextInt(10) < ++trees) {
                                 treetilex = p.getTileX();
                                 treetiley = p.getTileY();
                              }
                           }

                           distx = Math.pow((double)(p.getTileX() - defender.getCurrentTile().tilex), 2.0);
                           disty = Math.pow((double)(p.getTileY() - defender.getCurrentTile().tiley), 2.0);
                           double currdist = Math.sqrt(distx + disty);
                           float currHeight = Math.max(-1.4F, Zones.getLowestCorner(p.getTileX(), p.getTileY(), performer.getLayer()));
                           double distmod = currdist * dx;
                           if (dx < 0.0) {
                              if ((double)currHeight > (double)targetHeight - distmod) {
                                 performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot.");
                                 return true;
                              }
                           } else if ((double)currHeight > (double)targetHeight - distmod) {
                              performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot.");
                              return true;
                           }

                           if (tileArrowDownX == -1 && Server.rand.nextInt(15) == 0) {
                              tileArrowDownX = p.getTileX();
                              tileArrowDownY = p.getTileY();
                           }
                        }
                     } catch (NoPathException var43) {
                        performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot.");
                        return true;
                     }

                     if (tileArrowDownX == -1 && result != null && result.getTotalCover() > 0.0F) {
                        for(Blocker b : result.getBlockerArray()) {
                           if ((float)Server.rand.nextInt(100) < b.getBlockPercent(performer)) {
                              tileArrowDownX = b.getTileX();
                              tileArrowDownY = b.getTileY();
                              if (!b.isHorizontal() && performer.getCurrentTile().tilex < tileArrowDownX) {
                                 --tileArrowDownX;
                              } else if (b.isHorizontal() && performer.getCurrentTile().tiley < tileArrowDownY) {
                                 --tileArrowDownY;
                              }
                           }
                        }
                     }

                     performer.getStatus().modifyStamina(-2000.0F);
                     byte stringql = bow.getAuxData();
                     if (Server.rand.nextInt(Math.max(2, stringql) * 10) == 0) {
                        int realTemplate = 459;
                        if (bow.getTemplateId() == 449) {
                           realTemplate = 461;
                        } else if (bow.getTemplateId() == 448) {
                           realTemplate = 460;
                        }

                        bow.setTemplateId(realTemplate);
                        bow.setAuxData((byte)0);
                        performer.getCommunicator().sendCombatNormalMessage("The string breaks!");
                        return true;
                     }

                     Item arrow = getArrow(performer);
                     if (arrow == null) {
                        performer.getCommunicator().sendCombatNormalMessage("You have no arrows left to shoot!");
                        return true;
                     }

                     try {
                        arrow.getParent().dropItem(arrow.getWurmId(), false);
                     } catch (NoSuchItemException var40) {
                        Items.destroyItem(arrow.getWurmId());
                        performer.getCommunicator().sendCombatNormalMessage("You have no arrows left to shoot!");
                        return true;
                     }

                     performer.setSecondsToLogout(300);
                     if (!defender.isPlayer()) {
                        performer.setSecondsToLogout(180);
                     }

                     Arrows.addToHitCreature(
                        arrow,
                        performer,
                        defender,
                        counter,
                        act,
                        trees,
                        bow,
                        bowskill,
                        archery,
                        isAttackingPenned,
                        tileArrowDownX,
                        tileArrowDownY,
                        treetilex,
                        treetiley,
                        fence,
                        limitFail
                     );
                  }
               } else {
                  performer.getCommunicator().sendCombatNormalMessage("You can't shoot with that.");
                  done = true;
               }

               return done;
            }
         }
      }
   }

   static int getMinimumRangeForBow(int bowTemplateId) {
      int minRange = 4;
      if (bowTemplateId == 449) {
         minRange = 40;
      } else if (bowTemplateId == 448) {
         minRange = 20;
      } else if (bowTemplateId == 447) {
         minRange = 4;
      } else if (bowTemplateId == 450) {
         minRange = 20;
      }

      return minRange;
   }

   static int getMinimumRangeForBow(Item bow) {
      assert bow != null : "Bow was null when trying to get minimum range";

      return getMinimumRangeForBow(bow.getTemplateId());
   }

   public static final float getRarityArrowModifier(Item arrow) {
      return 1.0F + (float)arrow.getRarity() * 0.3F;
   }

   public static final float getRarityBowModifier(Item bow) {
      return 1.0F + (float)bow.getRarity() * 0.05F;
   }

   public static final double getDamage(Creature performer, Creature defender, Item bow, Item arrow, Skill archery) {
      double damage = (double)(
            ((float)bow.getDamagePercent() * arrow.getCurrentQualityLevel() + (float)bow.getDamagePercent() * bow.getCurrentQualityLevel()) / 2.0F
         )
         + archery.getKnowledge(0.0) * (double)bow.getDamagePercent();
      if (!Servers.isThisAnEpicOrChallengeServer()) {
         damage += (double)(bow.getSpellExtraDamageBonus() / 5.0F);
         damage += (double)arrow.getSpellExtraDamageBonus();
      }

      damage += (double)getRarityArrowModifier(arrow);
      damage += (double)getRarityBowModifier(bow);
      damage *= 1.0 + (performer.getStrengthSkill() - 20.0) / 100.0;
      if (Servers.isThisAnEpicOrChallengeServer()) {
         damage *= (double)(1.0F + bow.getSpellExtraDamageBonus() / 30000.0F);
         damage *= (double)(1.0F + arrow.getSpellExtraDamageBonus() / 30000.0F);
      }

      if (defender != null) {
         float heightDiff = Math.max(-1.4F, performer.getPositionZ() + performer.getAltOffZ())
            - Math.max(-1.4F, defender.getPositionZ() + defender.getAltOffZ());
         float heightDamEffect = Math.max(-0.2F, Math.min(0.2F, heightDiff / 100.0F));
         damage *= (double)(1.0F + heightDamEffect);
         if (Math.max(-1.4F, defender.getPositionZ()) + (float)defender.getCentimetersHigh() / 100.0F < -1.0F) {
            damage *= 0.5;
         }
      }

      if (performer.getFightlevel() >= 1) {
         damage *= 1.2F;
      }

      if (performer.getFightlevel() >= 4) {
         damage *= 1.2F;
      }

      if (arrow.getTemplateId() == 456) {
         damage *= 1.2F;
      } else if (arrow.getTemplateId() == 454) {
         damage *= 0.2F;
      }

      return damage;
   }

   public static boolean throwItem(Creature performer, Creature defender, Item thrown, Action act) {
      if (defender.isInvulnerable()) {
         performer.getCommunicator().sendCombatNormalMessage("You can't attack " + defender.getNameWithGenus() + " right now.");
         return true;
      } else if (performer.getPositionZ() < -1.0F && performer.getVehicle() == -10L) {
         performer.getCommunicator().sendCombatNormalMessage("You are too deep in the water to throw anything effectively.");
         return true;
      } else if (thrown.isBodyPartAttached()) {
         performer.getCommunicator().sendCombatNormalMessage("You need to wield a weapon to throw.");
         return true;
      } else if (thrown.isNoDrop()) {
         performer.getCommunicator().sendCombatNormalMessage("You are not allowed to drop that.");
         return true;
      } else if (thrown.isArtifact()) {
         performer.getCommunicator().sendCombatNormalMessage("You can't bring yourself to let go.");
         return true;
      } else if (defender.isDead()) {
         return true;
      } else {
         Skill weaponSkill = null;

         try {
            int skillnum = thrown.getPrimarySkill();
            if ((long)skillnum != -10L) {
               try {
                  weaponSkill = performer.getSkills().getSkill(skillnum);
               } catch (NoSuchSkillException var47) {
                  weaponSkill = performer.getSkills().learn(skillnum, 1.0F);
               }
            }
         } catch (NoSuchSkillException var48) {
         }

         if (performer.isOnSurface() != defender.isOnSurface()) {
            performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear view of " + defender.getNameWithGenus() + ".");
            return true;
         } else {
            if (WurmCalendar.getHour() > 20 || WurmCalendar.getHour() < 5) {
               performer.getCommunicator().sendCombatNormalMessage("The dusk makes it harder to get a clear view of " + defender.getNameWithGenus() + ".");
            }

            Zones.resetCoverHolder();
            if (performer.isOnSurface()
               && !performer.isWithinDistanceTo(defender, 110.0F)
               && defender.getCurrentTile() != null
               && defender.getCurrentTile().getStructure() != null
               && defender.getCurrentTile().getStructure().isFinalFinished()) {
               performer.getCommunicator()
                  .sendCombatNormalMessage("You fail to get a clear view of " + defender.getNameWithGenus() + " inside the structure.");
               return true;
            } else {
               BlockingResult result = Blocking.getRangedBlockerBetween(performer, defender);
               if (result != null) {
                  if (!performer.isOnPvPServer() || !defender.isOnPvPServer()) {
                     performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                     return true;
                  }

                  for(Blocker b : result.getBlockerArray()) {
                     if (b.getBlockPercent(performer) >= 100.0F) {
                        performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot of " + defender.getNameWithGenus() + ".");
                        return true;
                     }
                  }
               }

               if (!VirtualZone.isCreatureTurnedTowardsTarget(defender, performer, 60.0F, false)) {
                  performer.getCommunicator().sendCombatNormalMessage("You must turn towards " + defender.getNameWithGenus() + " in order to throw at it.");
                  return true;
               } else {
                  double diff = Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY());
                  if (diff < 2.0) {
                     performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too close.");
                     return true;
                  } else if (diff * (double)Math.max(1, thrown.getWeightGrams() / 5000) > performer.getStrengthSkill()) {
                     performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too far away.");
                     return true;
                  } else if ((double)((float)thrown.getWeightGrams() / 1000.0F) > performer.getStrengthSkill()) {
                     performer.getCommunicator().sendCombatNormalMessage(defender.getNameWithGenus() + " is too heavy.");
                     return true;
                  } else {
                     float bon = thrown.getSpellNimbleness();
                     if (bon > 0.0F) {
                        diff -= (double)(bon / 10.0F);
                     }

                     int trees = 0;
                     int treetilex = -1;
                     int treetiley = -1;
                     int tileArrowDownX = -1;
                     int tileArrowDownY = -1;
                     if (performer.getCurrentTile() != defender.getCurrentTile()) {
                        PathFinder pf = new PathFinder(true);

                        try {
                           for(Path path = pf.rayCast(
                                 performer.getCurrentTile().tilex,
                                 performer.getCurrentTile().tiley,
                                 defender.getCurrentTile().tilex,
                                 defender.getCurrentTile().tiley,
                                 performer.isOnSurface(),
                                 ((int)Creature.getRange(performer, (double)defender.getPosX(), (double)defender.getPosY()) >> 2) + 5
                              );
                              !path.isEmpty();
                              path.removeFirst()
                           ) {
                              PathTile p = path.getFirst();
                              if (Tiles.getTile(Tiles.decodeType(p.getTile())).isTree()) {
                                 if (treetilex == -1 && Server.rand.nextInt(10) > ++trees) {
                                    treetilex = p.getTileX();
                                    treetiley = p.getTileY();
                                 }
                              }

                              if (tileArrowDownX == -1 && Server.rand.nextInt(15) == 0) {
                                 tileArrowDownX = p.getTileX();
                                 tileArrowDownY = p.getTileY();
                              }
                           }
                        } catch (NoPathException var50) {
                           performer.getCommunicator().sendCombatNormalMessage("You fail to get a clear shot.");
                           return true;
                        }

                        if (tileArrowDownX == -1 && result != null && result.getTotalCover() > 0.0F) {
                           for(Blocker b : result.getBlockerArray()) {
                              if ((float)Server.rand.nextInt(100) < b.getBlockPercent(performer)) {
                                 tileArrowDownX = b.getTileX();
                                 tileArrowDownY = b.getTileY();
                                 if (!b.isHorizontal() && performer.getCurrentTile().tilex < tileArrowDownX) {
                                    --tileArrowDownX;
                                 } else if (b.isHorizontal() && performer.getCurrentTile().tiley < tileArrowDownY) {
                                    --tileArrowDownY;
                                 }
                              }
                           }
                        }
                     }

                     try {
                        thrown.getParent().dropItem(thrown.getWurmId(), false);
                     } catch (NoSuchItemException var46) {
                        return true;
                     }

                     performer.setStealth(false);
                     if (WurmCalendar.getHour() > 19) {
                        diff += (double)(WurmCalendar.getHour() - 19);
                     } else if (WurmCalendar.getHour() < 6) {
                        diff += (double)(6 - WurmCalendar.getHour());
                     }

                     diff += (double)trees;
                     diff += (double)Zones.getCoverHolder();
                     if (defender.isMoving()) {
                        diff += 10.0;
                     }

                     if (Math.max(0.0F, defender.getPositionZ()) + (float)defender.getCentimetersHigh() / 100.0F < -1.0F) {
                        diff += 40.0;
                     }

                     performer.setSecondsToLogout(300);
                     Server.getInstance()
                        .broadCastAction(performer.getName() + " throws " + performer.getHisHerItsString() + " " + thrown.getName() + ".", performer, 5);
                     performer.getCommunicator().sendCombatNormalMessage("You throw the " + thrown.getName() + ".");
                     performer.getStatus().modifyStamina(-5000.0F);

                     try {
                        Skill bcontrol = defender.getSkills().getSkill(104);
                        diff += bcontrol.getKnowledge(0.0) / 5.0;
                     } catch (NoSuchSkillException var45) {
                        logger.log(Level.WARNING, defender.getWurmId() + ", " + defender.getName() + " no body control.");
                     }

                     diff *= 1.0 - performer.getMovementScheme().armourMod.getModifier();
                     double power = 0.0;
                     if (weaponSkill != null) {
                        power = weaponSkill.skillCheck(diff, thrown, (double)thrown.getCurrentQualityLevel(), true, 1.0F);
                     } else {
                        power = performer.getBodyControl() - (double)Server.rand.nextInt(100);
                     }

                     double defCheck = 0.0;
                     boolean parriedShield = false;
                     if (power > 0.0) {
                        Item defShield = defender.getShield();
                        if (defShield != null && defender.getStatus().getStamina() >= 300 && willParryWithShield(performer, defender)) {
                           Skill defShieldSkill = null;
                           Skills defenderSkills = defender.getSkills();
                           int skillnum = -10;

                           try {
                              skillnum = defShield.getPrimarySkill();
                              defShieldSkill = defenderSkills.getSkill(skillnum);
                           } catch (NoSuchSkillException var49) {
                              if (skillnum != -10) {
                                 defShieldSkill = defenderSkills.learn(skillnum, 1.0F);
                              }
                           }

                           if (defShieldSkill != null) {
                              defShieldSkill.skillCheck(20.0 + (defender.isMoving() ? power : Math.max(1.0, power - 20.0)), defShield, 0.0, false, 1.0F);
                              defCheck = defShieldSkill.getKnowledge(defShield, defender.isMoving() ? -20.0 : 0.0)
                                 - (double)(Server.rand.nextFloat() * 115.0F);
                           }

                           defCheck += (double)((float)(defShield.getSizeY() + defShield.getSizeZ()) / 3.0F);
                           if (defCheck > 0.0) {
                              parriedShield = true;
                           }
                        }

                        defender.addAttacker(performer);
                        if (defender.isFriendlyKingdom(performer.getKingdomId())) {
                           if (defender.isDominated()) {
                              if (!performer.hasBeenAttackedBy(defender.getWurmId()) && !performer.hasBeenAttackedBy(defender.dominator)) {
                                 performer.setUnmotivatedAttacker();
                              }
                           } else if (defender.isRidden()) {
                              if (performer.getCitizenVillage() == null || defender.getCurrentVillage() != performer.getCitizenVillage()) {
                                 for(Long riderLong : defender.getRiders()) {
                                    try {
                                       Creature rider = Server.getInstance().getCreature(riderLong);
                                       rider.addAttacker(performer);
                                    } catch (NoSuchCreatureException var43) {
                                    } catch (NoSuchPlayerException var44) {
                                    }
                                 }
                              }
                           } else if (!performer.hasBeenAttackedBy(defender.getWurmId())
                              && (performer.getCitizenVillage() == null || !performer.getCitizenVillage().isEnemy(defender))) {
                              performer.setUnmotivatedAttacker();
                           }
                        }

                        power = Math.max(power, 5.0);
                        byte pos = getWoundPos(defender, act.getNumber());
                        pos = (byte)CombatEngine.getRealPosition(pos);
                        float armourMod = defender.getArmourMod();
                        float evasionChance = 0.0F;
                        double damage = (double)(Weapon.getBaseDamageForWeapon(thrown) * thrown.getCurrentQualityLevel() * 10.0F);
                        damage *= 1.0 + (performer.getStrengthSkill() - 20.0) / 100.0;
                        if (performer.getFightlevel() >= 1) {
                           damage *= 1.5;
                        }

                        if (performer.getFightlevel() >= 4) {
                           damage *= 1.5;
                        }

                        if (thrown.isLiquid()) {
                           damage = (double)((float)(Math.max(0, (Math.min(1500, thrown.getTemperature()) - 800) / 200) * thrown.getWeightGrams()) / 30.0F);
                        }

                        if (defCheck > 0.0) {
                           performer.getCommunicator()
                              .sendCombatNormalMessage("Your " + thrown.getName() + " glances off " + defender.getNameWithGenus() + "'s shield.");
                           defender.getCommunicator().sendCombatSafeMessage("You instinctively block the " + thrown.getName() + " with your shield.");
                           if (damage > 500.0) {
                              defender.getStatus().modifyStamina(-300.0F);
                           }
                        } else if (armourMod == 1.0F || defender.isVehicle() || defender.isKingdomGuard()) {
                           try {
                              byte bodyPosition = ArmourTemplate.getArmourPosition(pos);
                              Item armour = defender.getArmour(bodyPosition);
                              if (!defender.isKingdomGuard()) {
                                 armourMod = ArmourTemplate.calculateDR(armour, (byte)2);
                              } else {
                                 armourMod *= ArmourTemplate.calculateDR(armour, (byte)2);
                              }

                              armour.setDamage(
                                 armour.getDamage()
                                    + (float)(damage * (double)armourMod / 30000.0)
                                       * armour.getDamageModifier()
                                       * ArmourTemplate.getArmourDamageModFor(armour, (byte)2)
                              );
                              CombatEngine.checkEnchantDestruction(thrown, armour, defender);
                              if (defender.getBonusForSpellEffect((byte)22) > 0.0F) {
                                 if (armourMod >= 1.0F) {
                                    armourMod = 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
                                 } else {
                                    armourMod = Math.min(armourMod, 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
                                 }
                              }
                           } catch (NoArmourException var41) {
                              evasionChance = 1.0F - defender.getArmourMod();
                           } catch (NoSpaceException var42) {
                              logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos);
                           }
                        }

                        if (!(defender instanceof Player) && !performer.isInvulnerable()) {
                           defender.setTarget(performer.getWurmId(), false);
                        }

                        if (defender.isUnique()) {
                           evasionChance = 0.5F;
                           damage *= (double)armourMod;
                        }

                        boolean dropattile = false;
                        if (thrown.getTemplateId() == 105 || thrown.getTemplateId() == 116) {
                           performer.achievement(138);
                        }

                        if (defCheck > 0.0) {
                           dropattile = true;
                        } else if (Server.rand.nextFloat() < evasionChance) {
                           dropattile = true;
                           performer.getCommunicator()
                              .sendCombatNormalMessage("Your " + thrown.getName() + " glances off " + defender.getNameWithGenus() + "'s armour.");
                           defender.getCommunicator()
                              .sendCombatSafeMessage(
                                 LoginHandler.raiseFirstLetter(thrown.getNameWithGenus())
                                    + " hits you on the "
                                    + defender.getBody().getWoundLocationString(pos)
                                    + " but glances off your armour."
                              );
                        } else if (damage > 500.0) {
                           hit(defender, performer, thrown, null, damage, 1.0F, armourMod, pos, true, false, 0.0, 0.0);
                           performer.achievement(41);
                        } else {
                           dropattile = true;
                           performer.getCommunicator()
                              .sendCombatNormalMessage("Your " + thrown.getName() + " glances off " + defender.getNameWithGenus() + " and does no damage.");
                           defender.getCommunicator()
                              .sendCombatSafeMessage(
                                 LoginHandler.raiseFirstLetter(thrown.getNameWithGenus())
                                    + " hits you on the "
                                    + defender.getBody().getWoundLocationString(pos)
                                    + " but does no damage."
                              );
                        }

                        float fullDamage = thrown.getDamage() + 5.0F * Server.rand.nextFloat();
                        if (dropattile) {
                           if (thrown.isLiquid()) {
                              Items.destroyItem(thrown.getWurmId());
                              return true;
                           }

                           if (defCheck > 0.0 && parriedShield) {
                              tileArrowDownX = defender.getCurrentTile().tilex;
                              tileArrowDownY = defender.getCurrentTile().tiley;
                              if (defShield.isWood()) {
                                 SoundPlayer.playSound("sound.arrow.hit.wood", defender, 1.6F);
                              } else if (defShield.isMetal()) {
                                 SoundPlayer.playSound("sound.arrow.hit.metal", defender, 1.6F);
                              }
                           }

                           if (!thrown.isIndestructible() && Server.rand.nextInt(Math.max(1, (int)thrown.getCurrentQualityLevel())) < 2) {
                              for(Item item : thrown.getAllItems(false)) {
                                 try {
                                    Zone z = Zones.getZone(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface());
                                    VolaTile t = z.getOrCreateTile(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley);
                                    t.addItem(item, false, false);
                                 } catch (NoSuchZoneException var40) {
                                    performer.getCommunicator().sendCombatNormalMessage("The " + item.getName() + " disappears from your view.");
                                    Items.destroyItem(item.getWurmId());
                                 }
                              }

                              performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " breaks.");
                              Items.destroyItem(thrown.getWurmId());
                           } else if (!(fullDamage < 100.0F) && !thrown.isIndestructible() && !thrown.isLocked()) {
                              Items.destroyItem(thrown.getWurmId());
                              performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " breaks.");

                              for(Item item : thrown.getAllItems(false)) {
                                 try {
                                    Zone z = Zones.getZone(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface());
                                    VolaTile t = z.getOrCreateTile(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley);
                                    t.addItem(item, false, false);
                                 } catch (NoSuchZoneException var38) {
                                    performer.getCommunicator().sendCombatNormalMessage("The " + item.getName() + " disappears from your view.");
                                    Items.destroyItem(item.getWurmId());
                                 }
                              }
                           } else {
                              tileArrowDownX = defender.getCurrentTile().tilex;
                              tileArrowDownY = defender.getCurrentTile().tiley;

                              try {
                                 Zone z = Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
                                 VolaTile t = z.getOrCreateTile(tileArrowDownX, tileArrowDownY);
                                 t.addItem(thrown, false, false);
                              } catch (NoSuchZoneException var39) {
                                 performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " disappears from your view.");
                                 Items.destroyItem(thrown.getWurmId());
                              }
                           }
                        }
                     } else {
                        if (thrown.isLiquid()) {
                           Items.destroyItem(thrown.getWurmId());
                           return true;
                        }

                        float fullDamage = thrown.getDamage() + 5.0F * Server.rand.nextFloat();
                        if (!thrown.isIndestructible() && !thrown.isLocked() && Server.rand.nextInt(Math.max(1, (int)thrown.getCurrentQualityLevel())) < 2) {
                           performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " breaks.");

                           for(Item item : thrown.getAllItems(false)) {
                              try {
                                 Zone z = Zones.getZone(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface());
                                 VolaTile t = z.getOrCreateTile(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley);
                                 t.addItem(item, false, false);
                              } catch (NoSuchZoneException var37) {
                                 performer.getCommunicator().sendCombatNormalMessage("The " + item.getName() + " disappears from your view.");
                                 Items.destroyItem(item.getWurmId());
                              }
                           }

                           Items.destroyItem(thrown.getWurmId());
                        } else if (fullDamage >= 100.0F && !thrown.isIndestructible() && !thrown.isLocked()) {
                           for(Item item : thrown.getAllItems(false)) {
                              try {
                                 Zone z = Zones.getZone(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley, performer.isOnSurface());
                                 VolaTile t = z.getOrCreateTile(defender.getCurrentTile().tilex, defender.getCurrentTile().tiley);
                                 t.addItem(item, false, false);
                              } catch (NoSuchZoneException var36) {
                                 performer.getCommunicator().sendCombatNormalMessage("The " + item.getName() + " disappears from your view.");
                                 Items.destroyItem(item.getWurmId());
                              }
                           }

                           thrown.setDamage(fullDamage);
                           performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " breaks.");
                        } else {
                           if (trees > 0 && treetilex > 0) {
                              tileArrowDownX = treetilex;
                              tileArrowDownY = treetiley;
                           }

                           boolean hitdef = false;
                           if (tileArrowDownX == -1) {
                              if (defender.opponent != null
                                 && performer.getKingdomId() == defender.opponent.getKingdomId()
                                 && power < -20.0
                                 && (double)Server.rand.nextInt(100) < Math.abs(power)
                                 && defender.opponent.isPlayer()
                                 && defender.opponent != performer
                                 && performer.getFightlevel() < 1) {
                                 byte pos = getWoundPos(defender.opponent, act.getNumber());
                                 pos = (byte)CombatEngine.getRealPosition(pos);
                                 float armourMod = defender.opponent.getArmourMod();
                                 double damage = (double)(Weapon.getBaseDamageForWeapon(thrown) * thrown.getCurrentQualityLevel() * 10.0F);
                                 damage *= 1.0 + (performer.getStrengthSkill() - 20.0) / 100.0;
                                 damage *= (double)(1.0F + thrown.getCurrentQualityLevel() / 100.0F);
                                 if (armourMod == 1.0F) {
                                    try {
                                       byte bodyPosition = ArmourTemplate.getArmourPosition(pos);
                                       Item armour = defender.opponent.getArmour(bodyPosition);
                                       armourMod = ArmourTemplate.calculateDR(armour, (byte)2);
                                       armour.setDamage(
                                          armour.getDamage()
                                             + (float)(damage * (double)armourMod / 50000.0)
                                                * armour.getDamageModifier()
                                                * ArmourTemplate.getArmourDamageModFor(armour, (byte)2)
                                       );
                                       CombatEngine.checkEnchantDestruction(thrown, armour, defender.opponent);
                                    } catch (NoArmourException var34) {
                                    } catch (NoSpaceException var35) {
                                       logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos);
                                    }
                                 }

                                 hit(defender.opponent, performer, thrown, null, damage, 1.0F, armourMod, pos, false, false, 0.0, 0.0);
                                 hitdef = true;
                              }

                              if (!hitdef) {
                                 tileArrowDownX = defender.getCurrentTile().tilex;
                                 tileArrowDownY = defender.getCurrentTile().tiley;
                              }
                           }

                           if (!hitdef) {
                              try {
                                 Zone z = Zones.getZone(tileArrowDownX, tileArrowDownY, performer.isOnSurface());
                                 VolaTile t = z.getOrCreateTile(tileArrowDownX, tileArrowDownY);
                                 t.addItem(thrown, false, false);
                                 if (treetilex > 0) {
                                    SoundPlayer.playSound("sound.arrow.stuck.wood", tileArrowDownX, tileArrowDownY, performer.isOnSurface(), 0.0F);
                                 } else if (result != null) {
                                    if (result.getFirstBlocker() != null) {
                                       if (result.getFirstBlocker().isFence()) {
                                          SoundPlayer.playSound("sound.work.masonry", tileArrowDownX, tileArrowDownY, performer.isOnSurface(), 0.0F);
                                       } else {
                                          SoundPlayer.playSound("sound.arrow.stuck.wood", tileArrowDownX, tileArrowDownY, performer.isOnSurface(), 0.0F);
                                       }
                                    }
                                 } else {
                                    SoundPlayer.playSound("sound.arrow.stuck.ground", tileArrowDownX, tileArrowDownY, performer.isOnSurface(), 0.0F);
                                 }
                              } catch (NoSuchZoneException var33) {
                                 Items.destroyItem(thrown.getWurmId());
                                 performer.getCommunicator().sendCombatNormalMessage("The " + thrown.getName() + " disappears from your view");
                              }
                           }
                        }
                     }

                     return true;
                  }
               }
            }
         }
      }
   }

   public static boolean hit(
      Creature defender,
      Creature performer,
      Item arrow,
      @Nullable Item bow,
      double damage,
      float damMod,
      float armourMod,
      byte pos,
      boolean intentional,
      boolean dryRun,
      double diff,
      double bonus
   ) {
      if (defender.isDead()) {
         return true;
      } else {
         boolean done = false;
         ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
         segments.add(new CreatureLineSegment(performer));
         segments.add(new MulticolorLineSegment("'s ", (byte)7));
         segments.add(new MulticolorLineSegment(" " + arrow.getName() + " hits ", (byte)7));
         segments.add(new CreatureLineSegment(defender));
         segments.add(new MulticolorLineSegment(" in the " + defender.getBody().getWoundLocationString(pos) + ".", (byte)7));
         defender.getCommunicator().sendColoredMessageCombat(segments);
         segments.get(1).setText("r");

         for(MulticolorLineSegment s : segments) {
            s.setColor((byte)3);
         }

         performer.getCommunicator().sendColoredMessageCombat(segments);
         Battle battle = performer.getBattle();
         float spdamb = arrow.getSpellDamageBonus();
         float rotdam = arrow.getWeaponSpellDamageBonus();
         if (arrow.isIndestructible() || !arrow.isLiquid() && Server.rand.nextInt(Math.max(1, (int)arrow.getCurrentQualityLevel())) >= 2) {
            if (!arrow.setDamage(arrow.getDamage() + 5.0F * damMod)) {
               defender.getInventory().insertItem(arrow, true);
               arrow.setBusy(false);
            }
         } else {
            Items.destroyItem(arrow.getWurmId());
            defender.getCommunicator().sendNormalServerMessage("The " + arrow.getName() + " breaks!");
            performer.getCommunicator().sendNormalServerMessage("The " + arrow.getName() + " breaks!");
         }

         damage = Math.min(1.2 * damage, Math.max(0.8F * damage, (double)(Server.rand.nextFloat() + Server.rand.nextFloat()) * damage));
         float ms = arrow.getSpellMindStealModifier();
         if (ms > 0.0F && !defender.isPlayer() && defender.getKingdomId() != performer.getKingdomId()) {
            Skills s = defender.getSkills();
            int r = Server.rand.nextInt(s.getSkills().length);
            Skill toSteal = s.getSkills()[r];
            double mod = 1.0;
            if (damage < 5000.0) {
               mod = damage / 5000.0;
            }

            double skillStolen = (double)(ms / 100.0F * 0.1F) * mod;

            try {
               Skill owned = defender.getSkills().getSkill(toSteal.getNumber());
               if (owned.getKnowledge() < toSteal.getKnowledge()) {
                  toSteal.setKnowledge(toSteal.getKnowledge() - skillStolen / 10.0, false);
                  owned.setKnowledge(owned.getKnowledge() + skillStolen / 10.0, false);
                  performer.getCommunicator().sendSafeServerMessage("The " + arrow.getName() + " steals some " + toSteal.getName() + ".");
               }
            } catch (NoSuchSkillException var31) {
            }
         }

         byte wtype = 2;
         if (arrow.isLiquid()) {
            wtype = 4;
         }

         boolean dead = false;
         float poisdam = arrow.getSpellVenomBonus();
         if (poisdam > 0.0F) {
            float half = Math.max(1.0F, poisdam / 2.0F);
            poisdam = half + (float)Server.rand.nextInt((int)half);
            wtype = 5;
            damage *= 0.8F;
         }

         if (bow != null && damage * (double)armourMod > 500.0 && bow.isWeaponBow()) {
            Skill archery = null;
            Skill bowskill = null;

            try {
               int skillnum = bow.getPrimarySkill();
               if ((long)skillnum != -10L) {
                  try {
                     bowskill = performer.getSkills().getSkill(skillnum);
                  } catch (NoSuchSkillException var30) {
                     bowskill = performer.getSkills().learn(skillnum, 1.0F);
                  }
               }
            } catch (NoSuchSkillException var32) {
               performer.getCommunicator()
                  .sendCombatNormalMessage("This weapon has no skill attached. Please report this bug using the forums or the /dev channel.");
               return true;
            }

            try {
               archery = performer.getSkills().getSkill(1030);
            } catch (NoSuchSkillException var29) {
               archery = performer.getSkills().learn(1030, 1.0F);
            }

            if (bowskill == null || archery == null) {
               performer.getCommunicator()
                  .sendCombatNormalMessage("There was a bug with the skill for this item. Please report this bug using the forums or the /dev channel.");
               return true;
            }

            bowskill.skillCheck(diff, bow, (double)arrow.getCurrentQualityLevel(), dryRun || arrow.getTemplateId() == 454, (float)damage / 500.0F);
         }

         if (rotdam > 0.0F) {
            dead = CombatEngine.addWound(
               performer,
               defender,
               wtype,
               pos,
               damage + damage * (double)rotdam / 500.0,
               armourMod,
               "hit",
               battle,
               (float)Server.rand.nextInt((int)Math.max(1.0F, rotdam)),
               poisdam,
               true,
               false,
               false,
               false
            );
         } else {
            dead = CombatEngine.addWound(performer, defender, wtype, pos, damage, armourMod, "hit", battle, 0.0F, poisdam, true, false, false, false);
            if (arrow.getSpellLifeTransferModifier() > 0.0F
               && damage * (double)arrow.getSpellLifeTransferModifier() / 100.0 > 500.0
               && performer.getBody() != null
               && performer.getBody().getWounds() != null) {
               Wound[] w = performer.getBody().getWounds().getWounds();
               if (w.length > 0) {
                  w[0]
                     .modifySeverity(
                        -(
                           (int)(
                              damage
                                 * (double)arrow.getSpellLifeTransferModifier()
                                 / (double)(performer.getCultist() != null && performer.getCultist().healsFaster() ? 250.0F : 500.0F)
                           )
                        )
                     );
               }
            }
         }

         if (!dead && spdamb > 0.0F && (double)(spdamb / 300.0F) * damage > 500.0) {
            dead = defender.addWoundOfType(performer, (byte)4, pos, false, armourMod, false, (double)(spdamb / 300.0F) * damage, 0.0F, 0.0F, false, true);
         }

         if (!dead && arrow.getSpellFrostDamageBonus() > 0.0F && (double)(arrow.getSpellFrostDamageBonus() / 300.0F) * damage > 500.0) {
            dead = defender.addWoundOfType(
               performer, (byte)8, pos, false, armourMod, false, (double)(arrow.getSpellFrostDamageBonus() / 300.0F) * damage, 0.0F, 0.0F, false, true
            );
         }

         if (!Players.getInstance().isOverKilling(performer.getWurmId(), defender.getWurmId())) {
            if (arrow.getSpellExtraDamageBonus() > 0.0F) {
               if (defender.isPlayer() && !defender.isNewbie()) {
                  SpellEffect speff = arrow.getSpellEffect((byte)45);
                  double gainMod = 1.0;
                  if (damage < 5000.0) {
                     gainMod = damage / 5000.0;
                  }

                  if (speff != null) {
                     speff.setPower(Math.min(10000.0F, speff.power + (float)(dead ? 50.0 : 10.0 * gainMod)));
                  }
               } else if (!defender.isPlayer() && !defender.isGuard() && dead) {
                  SpellEffect speff = arrow.getSpellEffect((byte)45);
                  float gainMod = 1.0F;
                  if (speff.getPower() > 5000.0F && !Servers.isThisAnEpicOrChallengeServer()) {
                     gainMod = Math.max(0.5F, 1.0F - (speff.getPower() - 5000.0F) / 5000.0F);
                  }

                  if (speff != null) {
                     speff.setPower(Math.min(10000.0F, speff.power + defender.getBaseCombatRating() * gainMod));
                  }
               }
            }

            if (bow != null && bow.getSpellExtraDamageBonus() > 0.0F) {
               if (defender.isPlayer() && !defender.isNewbie()) {
                  SpellEffect speff = bow.getSpellEffect((byte)45);
                  double gainMod = 1.0;
                  if (damage * (double)armourMod < 5000.0) {
                     gainMod = damage * (double)armourMod / 5000.0;
                  }

                  if (speff != null) {
                     speff.setPower(Math.min(10000.0F, speff.power + (float)(dead ? 30.0 : 2.0 * gainMod)));
                  }
               } else if (!defender.isPlayer() && !defender.isGuard() && dead) {
                  SpellEffect speff = bow.getSpellEffect((byte)45);
                  float gainMod = 1.0F;
                  if (speff.getPower() > 5000.0F && !Servers.isThisAnEpicOrChallengeServer()) {
                     gainMod = Math.max(0.5F, 1.0F - (speff.getPower() - 5000.0F) / 5000.0F);
                  }

                  if (speff != null) {
                     speff.setPower(Math.min(10000.0F, speff.power + defender.getBaseCombatRating() * gainMod));
                  }
               }
            }
         }

         if (dead) {
            performer.getCommunicator().sendCombatSafeMessage(defender.getNameWithGenus() + " is dead!");
            if (battle != null) {
               battle.addCasualty(performer, defender);
            }

            performer.getCombatHandler().setKillEffects(performer, defender);
            done = true;
         } else {
            SoundPlayer.playSound(defender.getHitSound(), defender, 1.6F);
            if (!defender.isPlayer() && defender.isTypeFleeing()) {
               defender.setFleeCounter(20, true);
               Server.getInstance().broadCastAction(defender.getNameWithGenus() + " panics.", defender, 10);
            }
         }

         return done;
      }
   }

   public static boolean isTargetTurnedTowardsCreature(Creature performer, Item target) {
      return VirtualZone.isItemTurnedTowardsCreature(performer, target, 60.0F);
   }

   public static boolean attack(Creature performer, Item target, Item bow, float counter, Action act) {
      boolean done = false;
      boolean boat = target.isBoat();
      if (!target.isDecoration() && !boat) {
         performer.getCommunicator().sendNormalServerMessage("You can't attack " + target.getNameWithGenus() + ".");
         return true;
      } else if ((double)performer.getPositionZ() < -0.5 && performer.getVehicle() == -10L) {
         performer.getCommunicator().sendNormalServerMessage("You are too deep in the water to fire a bow.");
         return true;
      } else {
         if (bow.isWeaponBow()) {
            int minRange = getMinimumRangeForBow(bow);
            if (Creature.getRange(performer, (double)target.getPosX(), (double)target.getPosY()) < (double)minRange) {
               performer.getCommunicator().sendNormalServerMessage(LoginHandler.raiseFirstLetter(target.getName()) + " is too close.");
               return true;
            }

            Skill bowskill = null;

            try {
               int skillnum = bow.getPrimarySkill();
               if ((long)skillnum != -10L) {
                  try {
                     bowskill = performer.getSkills().getSkill(skillnum);
                  } catch (NoSuchSkillException var26) {
                     bowskill = performer.getSkills().learn(skillnum, 1.0F);
                  }
               }
            } catch (NoSuchSkillException var28) {
               performer.getCommunicator()
                  .sendNormalServerMessage("This weapon has no skill attached. Please report this bug using the forums or the /dev channel.");
               return true;
            }

            if (bowskill == null) {
               performer.getCommunicator()
                  .sendNormalServerMessage("There was a bug with the skill for this item. Please report this bug using the forums or the /dev channel.");
               return true;
            }

            if (!isTargetTurnedTowardsCreature(performer, target) && !boat) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You must position yourself in front of the " + target.getName() + " in order to shoot at it.");
               return true;
            }

            if (!VirtualZone.isCreatureTurnedTowardsItem(target, performer, 60.0F)) {
               performer.getCommunicator().sendCombatNormalMessage("You must turn towards the " + target.getName() + " in order to shoot at it.");
               return true;
            }

            if (performer.attackingIntoIllegalDuellingRing(target.getTileX(), target.getTileY(), target.isOnSurface())) {
               performer.getCommunicator().sendNormalServerMessage("The duelling ring is holy ground and you may not attack across the border.");
               return true;
            }

            if (act.justTickedSecond()) {
               performer.decreaseFatigue();
            }

            int time = 200;
            if (counter == 1.0F) {
               if (performer.isOnSurface() != target.isOnSurface()) {
                  performer.getCommunicator().sendNormalServerMessage("You fail to get a clear shot of the " + target.getName() + ".");
                  return true;
               }

               Item arrow = getArrow(performer);
               if (arrow == null) {
                  performer.getCommunicator().sendNormalServerMessage("You have no arrows left to shoot!");
                  return true;
               }

               time = Actions.getQuickActionTime(performer, bowskill, bow, 0.0);
               if (act.getNumber() == 125 && time > 30) {
                  time = (int)Math.max(30.0F, (float)time * 0.8F);
               }

               performer.getCommunicator().sendNormalServerMessage("You start aiming at " + target.getNameWithGenus() + ".");
               if (WurmCalendar.getHour() > 20 || WurmCalendar.getHour() < 5) {
                  performer.getCommunicator().sendNormalServerMessage("The dusk makes it harder to get a clear view of the " + target.getName() + ".");
               }

               performer.sendActionControl(Actions.actionEntrys[act.getNumber()].getVerbString(), true, time);
               Server.getInstance()
                  .broadCastAction(performer.getName() + " draws an arrow and raises " + performer.getHisHerItsString() + " bow.", performer, 5);
               act.setTimeLeft(time);
               SoundPlayer.playSound("sound.arrow.aim", performer, 1.6F);
            } else {
               time = act.getTimeLeft();
            }

            if (counter * 10.0F > (float)time) {
               done = true;
               performer.getStatus().modifyStamina(-1000.0F);
               if (performer.isOnSurface() != target.isOnSurface()) {
                  performer.getCommunicator().sendNormalServerMessage("You fail to get a clear shot of the " + target.getName() + ".");
                  return true;
               }

               Zones.resetCoverHolder();
               int minRange1 = getMinimumRangeForBow(bow);
               if (Creature.getRange(performer, (double)target.getPosX(), (double)target.getPosY()) < (double)minRange1) {
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " is too close.");
                  return true;
               }

               int trees = 0;
               int treetilex = -1;
               int treetiley = -1;
               int tileArrowDownX = -1;
               int tileArrowDownY = -1;
               PathFinder pf = new PathFinder(true);

               try {
                  for(Path path = pf.rayCast(
                        performer.getCurrentTile().tilex,
                        performer.getCurrentTile().tiley,
                        target.getTileX(),
                        target.getTileY(),
                        performer.isOnSurface(),
                        ((int)Creature.getRange(performer, (double)target.getPosX(), (double)target.getPosY()) >> 2) + 5
                     );
                     !path.isEmpty();
                     path.removeFirst()
                  ) {
                     PathTile p = path.getFirst();
                     if (Tiles.getTile(Tiles.decodeType(p.getTile())).isTree()) {
                        if (treetilex == -1 && Server.rand.nextInt(10) > ++trees) {
                           treetilex = p.getTileX();
                           treetiley = p.getTileY();
                        }
                     }

                     if (tileArrowDownX == -1 && Server.rand.nextInt(15) == 0) {
                        tileArrowDownX = p.getTileX();
                        tileArrowDownY = p.getTileY();
                     }
                  }
               } catch (NoPathException var27) {
                  performer.getCommunicator().sendNormalServerMessage("You fail to get a clear shot.");
                  return true;
               }

               byte stringql = bow.getAuxData();
               if (Server.rand.nextInt(Math.max(2, stringql) * 10) == 0) {
                  int realTemplate = 459;
                  if (bow.getTemplateId() == 449) {
                     realTemplate = 461;
                  } else if (bow.getTemplateId() == 448) {
                     realTemplate = 460;
                  }

                  bow.setTemplateId(realTemplate);
                  bow.setAuxData((byte)0);
                  performer.getCommunicator().sendNormalServerMessage("The string breaks!");
                  return true;
               }

               double diff = getBaseDifficulty(act.getNumber());
               if (WurmCalendar.getHour() > 19) {
                  diff += (double)(WurmCalendar.getHour() - 19);
               } else if (WurmCalendar.getHour() < 6) {
                  diff += (double)(6 - WurmCalendar.getHour());
               }

               diff += (double)trees;
               float bon = bow.getSpellNimbleness();
               if (bon > 0.0F) {
                  diff -= (double)(bon / 10.0F);
               }

               double deviation = getRangeDifficulty(performer, bow.getTemplateId(), target.getPosX(), target.getPosY());
               diff += deviation;
               if (target.isBoat()) {
                  diff += 30.0;
               }

               Item arrow = getArrow(performer);
               if (arrow == null) {
                  performer.getCommunicator().sendNormalServerMessage("You have no arrows left to shoot!");
                  return true;
               }

               try {
                  diff -= (double)bow.getRarity();
                  diff -= (double)arrow.getRarity();
                  arrow.getParent().dropItem(arrow.getWurmId(), false);
               } catch (NoSuchItemException var25) {
                  Items.destroyItem(arrow.getWurmId());
                  performer.getCommunicator().sendNormalServerMessage("You have no arrows left to shoot!");
                  return true;
               }

               Arrows.addToHitItem(
                  arrow, performer, target, counter, bowskill, bow, tileArrowDownX, tileArrowDownY, deviation, diff, trees, treetilex, treetiley
               );
            }
         } else {
            done = true;
            performer.getCommunicator().sendNormalServerMessage("You can't shoot with that.");
         }

         return done;
      }
   }

   public static final boolean mayTakeDamage(Item target) {
      return !target.isStone()
         && !target.isMetal()
         && !target.isBodyPart()
         && !target.isTemporary()
         && target.getTemplateId() != 272
         && (!target.isLockable() || target.getLockId() == -10L)
         && !target.isIndestructible()
         && !target.isHugeAltar()
         && !target.isDomainItem()
         && !target.isKingdomMarker()
         && !target.isTraded()
         && !target.isBanked()
         && !target.isArtifact();
   }

   public static Item getArrow(Creature performer) {
      Item quiver = performer.getBody().getBodyItem().findFirstContainedItem(462);
      if (quiver == null || quiver.isEmpty(false)) {
         quiver = performer.getInventory().findItem(462, true);
      }

      Item arrow = null;
      if (quiver != null) {
         arrow = quiver.findFirstContainedItem(456);
         if (arrow == null) {
            arrow = quiver.findFirstContainedItem(455);
         }

         if (arrow == null) {
            arrow = quiver.findFirstContainedItem(454);
         }
      } else {
         arrow = performer.getBody().getBodyItem().findFirstContainedItem(456);
         if (arrow == null) {
            arrow = performer.getBody().getBodyItem().findFirstContainedItem(455);
         }

         if (arrow == null) {
            arrow = performer.getBody().getBodyItem().findFirstContainedItem(454);
         }

         if (arrow == null) {
            arrow = performer.getInventory().findItem(456, true);
         }

         if (arrow == null) {
            arrow = performer.getInventory().findItem(455, true);
         }

         if (arrow == null) {
            arrow = performer.getInventory().findItem(454, true);
         }
      }

      return arrow;
   }

   public static boolean isArchery(int action) {
      return action == 124 || action == 125 || action == 128 || action == 131 || action == 129 || action == 130 || action == 126 || action == 127;
   }

   public static final double getBaseDifficulty(int action) {
      if (action == 124) {
         return 20.0;
      } else if (action == 125) {
         return 30.0;
      } else if (action == 128) {
         return 35.0;
      } else if (action == 129) {
         return 40.0;
      } else if (action == 130) {
         return 40.0;
      } else if (action == 126) {
         return 70.0;
      } else if (action == 127) {
         return 70.0;
      } else if (action == 131) {
         return 70.0;
      } else {
         return action == 134 ? 1.0 : 99.0;
      }
   }

   public static final byte getWoundPos(Creature defender, int action) {
      if (action != 124 && action != 125) {
         if (action == 128) {
            return 2;
         }

         if (action == 131) {
            return 34;
         }

         if (action == 129) {
            return 3;
         }

         if (action == 130) {
            return 4;
         }

         if (action == 126) {
            return 1;
         }

         if (action == 127) {
            return 29;
         }
      } else {
         try {
            return defender.getBody().getRandomWoundPos();
         } catch (Exception var3) {
            logger.log(Level.WARNING, defender + ", " + var3.getMessage(), (Throwable)var3);
         }
      }

      return 2;
   }

   public static final double getRangeDifficulty(Creature performer, int itemTemplateId, float targetX, float targetY) {
      double perfectDist = 30.0;
      if (itemTemplateId == 447) {
         perfectDist = 20.0;
      } else if (itemTemplateId == 448) {
         perfectDist = 40.0;
      } else if (itemTemplateId == 449) {
         perfectDist = 80.0;
      }

      double range = Creature.getRange(performer, (double)targetX, (double)targetY);
      double deviation = Math.abs(perfectDist - range);
      deviation += range / 5.0;
      if (deviation < 1.0) {
         deviation = 1.0;
      }

      return deviation;
   }

   public static final boolean willParryWithShield(Creature archer, Creature shieldbearer) {
      return VirtualZone.isCreatureTurnedTowardsTarget(archer, shieldbearer, 90.0F, true);
   }
}
