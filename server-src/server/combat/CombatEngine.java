package com.wurmonline.server.combat;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.MessageServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.PracticeDollBehaviour;
import com.wurmonline.server.bodys.DbWound;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.Enchants;
import com.wurmonline.shared.constants.SoundNames;
import com.wurmonline.shared.util.MulticolorLineSegment;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CombatEngine implements CombatConstants, CounterTypes, MiscConstants, SoundNames, TimeConstants, Enchants, CreatureTypes {
   private static final Logger logger = Logger.getLogger(CombatEngine.class.getName());
   public static final float ARMOURMOD = 1.0F;

   private CombatEngine() {
   }

   @Deprecated
   public static boolean attack2(Creature performer, Creature defender, int counter, Action act) {
      return attack(performer, defender, counter, -1, act);
   }

   @Deprecated
   public static boolean attack(Creature performer, Creature defender, int counter, int pos, Action act) {
      if (!performer.getStatus().visible) {
         performer.getCommunicator().sendAlertServerMessage("You are now visible again.");
         performer.setVisible(true);
      }

      boolean done = false;
      boolean dead = false;
      boolean aiming = false;
      if (performer.equals(defender)) {
         performer.getCommunicator().sendAlertServerMessage("You cannot attack yourself.");
         performer.setOpponent(null);
         return true;
      } else {
         Item primWeapon = performer.getPrimWeapon();
         performer.setSecondsToLogout(300);
         if (!defender.isPlayer()) {
            performer.setSecondsToLogout(180);
         }

         if (defender.getAttackers() > 9) {
            performer.getCommunicator().sendNormalServerMessage(defender.getNameWithGenus() + " is too crowded with attackers. You find no space.");
            performer.setOpponent(null);
            return true;
         } else if (primWeapon == null) {
            performer.getCommunicator().sendNormalServerMessage("You have no weapon to attack " + defender.getNameWithGenus() + " with.");
            performer.setOpponent(null);
            return true;
         } else {
            BlockingResult result = Blocking.getBlockerBetween(performer, defender, 4);
            if (result != null) {
               performer.getCommunicator().sendNormalServerMessage("The wall blocks your attempt.");
               performer.setOpponent(null);
               return true;
            } else if (Creature.rangeTo(performer, defender) > Actions.actionEntrys[114].getRange()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You are now too far away to " + Actions.actionEntrys[114].getActionString().toLowerCase() + " " + defender.getNameWithGenus() + "."
                  );
               performer.setOpponent(null);
               return true;
            } else {
               if (performer.getLeader() != null && performer.getLeader() == defender) {
                  performer.setLeader(null);
               }

               if (performer.getPet() != null && performer.getPet().target == -10L) {
                  performer.getPet().setTarget(defender.getWurmId(), false);
               }

               performer.staminaPollCounter = 2;
               int speed = 10;
               int timeMod = 2;
               if (performer.getFightStyle() == 2) {
                  timeMod = 4;
               } else if (performer.getFightStyle() == 1) {
                  timeMod = 0;
               }

               if (primWeapon.isBodyPart() && primWeapon.getAuxData() != 100) {
                  speed = (int)performer.getBodyWeaponSpeed(primWeapon);
               } else if (primWeapon.isWeaponPierce() || primWeapon.isWeaponKnife()) {
                  speed = primWeapon.getWeightGrams() / 1000 + 1 + timeMod;
               } else if (primWeapon.isWeaponSlash() || primWeapon.isWeaponSword() || primWeapon.isWeaponAxe()) {
                  speed = primWeapon.getWeightGrams() / 1000 + 3 + timeMod;
               } else if (primWeapon.isWeaponCrush()) {
                  speed = primWeapon.getWeightGrams() / 1000 + 6 + timeMod;
               } else {
                  speed = primWeapon.getWeightGrams() / 1000 + 6 + timeMod;
               }

               if (pos != -1) {
                  aiming = true;
                  ++speed;
               }

               defender.addAttacker(performer);
               if (!done) {
                  int posBonus = 0;
                  float defAngle = Creature.normalizeAngle(defender.getStatus().getRotation());
                  double newrot = Math.atan2(
                     (double)(performer.getStatus().getPositionY() - defender.getStatus().getPositionY()),
                     (double)(performer.getStatus().getPositionX() - defender.getStatus().getPositionX())
                  );
                  float attAngle = (float)(newrot * (180.0 / Math.PI)) + 90.0F;
                  attAngle = Creature.normalizeAngle(attAngle - defAngle);
                  if (attAngle > 90.0F && attAngle < 270.0F) {
                     if (attAngle > 135.0F && attAngle < 225.0F) {
                        posBonus = 10;
                     } else {
                        posBonus = 5;
                     }
                  }

                  float diff = (performer.getPositionZ() + performer.getAltOffZ() - defender.getPositionZ() + defender.getAltOffZ()) / 10.0F;
                  posBonus += (int)Math.min(5.0F, diff);
                  if (counter == 1) {
                     if (!(defender instanceof Player)) {
                        defender.turnTowardsCreature(performer);
                        if (defender.isHunter()) {
                           defender.setTarget(performer.getWurmId(), false);
                        }
                     }

                     if (performer instanceof Player && defender instanceof Player) {
                        Battle battle = Battles.getBattleFor(performer, defender);
                        battle.addEvent(new BattleEvent((short)-1, performer.getName(), defender.getName()));
                     }

                     if (aiming) {
                        String bodypartname = defender.getBody().getWoundLocationString(pos);
                        performer.getCommunicator()
                           .sendSafeServerMessage(
                              "You try to " + getAttackString(performer, primWeapon) + " " + defender.getNameWithGenus() + " in the " + bodypartname + "."
                           );
                        defender.getCommunicator()
                           .sendAlertServerMessage(performer.getNameWithGenus() + " tries to " + getAttackString(performer, primWeapon) + " you!");
                     } else {
                        performer.getCommunicator()
                           .sendSafeServerMessage("You try to " + getAttackString(performer, primWeapon) + " " + defender.getNameWithGenus() + ".");
                        if (performer.isDominated() && performer.getDominator() != null) {
                           performer.getDominator()
                              .getCommunicator()
                              .sendSafeServerMessage(
                                 performer.getNameWithGenus()
                                    + " tries to "
                                    + getAttackString(performer, primWeapon)
                                    + " "
                                    + defender.getNameWithGenus()
                                    + "."
                              );
                        }

                        defender.getCommunicator()
                           .sendAlertServerMessage(performer.getNameWithGenus() + " tries to " + getAttackString(performer, primWeapon) + " you!");
                        if (defender.isDominated() && defender.getDominator() != null) {
                           defender.getDominator()
                              .getCommunicator()
                              .sendAlertServerMessage(
                                 performer.getNameWithGenus()
                                    + " tries to "
                                    + getAttackString(performer, primWeapon)
                                    + " "
                                    + defender.getNameWithGenus()
                                    + "!"
                              );
                        }
                     }
                  } else {
                     Battle battle = performer.getBattle();
                     if (battle != null) {
                        battle.touch();
                     }
                  }

                  if (act.currentSecond() % speed == 0 || counter == 1 && !(performer instanceof Player)) {
                     if (!(defender instanceof Player)) {
                        defender.turnTowardsCreature(performer);
                     }

                     Item defPrimWeapon = null;
                     Skill attackerFightSkill = null;
                     Skill defenderFightSkill = null;
                     Skills performerSkills = performer.getSkills();
                     Skills defenderSkills = defender.getSkills();
                     double attBonus = (double)performer.zoneBonus - (double)performer.getMovePenalty() * 0.5;
                     double defBonus = (double)(defender.zoneBonus - (float)defender.getMovePenalty());
                     if (defender.isMoving() && defender instanceof Player) {
                        defBonus -= 5.0;
                     }

                     if (performer.isMoving() && performer instanceof Player) {
                        attBonus -= 5.0;
                     }

                     attBonus += (double)posBonus;
                     defPrimWeapon = defender.getPrimWeapon();
                     int attSknum = 1023;

                     try {
                        attackerFightSkill = performerSkills.getSkill(1023);
                     } catch (NoSuchSkillException var37) {
                        attackerFightSkill = performerSkills.learn(1023, 1.0F);
                     }

                     int defSknum = 1023;

                     try {
                        defenderFightSkill = defenderSkills.getSkill(1023);
                     } catch (NoSuchSkillException var36) {
                        defenderFightSkill = defenderSkills.learn(1023, 1.0F);
                     }

                     dead = performAttack(
                        pos,
                        aiming,
                        performer,
                        performerSkills,
                        defender,
                        defenderSkills,
                        primWeapon,
                        defPrimWeapon,
                        attBonus,
                        defBonus,
                        attackerFightSkill,
                        defenderFightSkill,
                        speed
                     );
                     if (aiming) {
                        done = true;
                     }

                     if (dead) {
                        done = true;
                     }
                  }

                  if (!done && !aiming) {
                     Item[] secondaryWeapons = performer.getSecondaryWeapons();

                     for(int x = 0; x < secondaryWeapons.length; ++x) {
                        if (!done) {
                           speed = 10;
                           if (secondaryWeapons[x].isBodyPart() && secondaryWeapons[x].getAuxData() != 100) {
                              speed = Server.rand.nextInt((int)(performer.getBodyWeaponSpeed(secondaryWeapons[x]) + 5.0F)) + 1 + timeMod;
                           } else if (secondaryWeapons[x].isWeaponPierce() || secondaryWeapons[x].isWeaponKnife()) {
                              speed = 5 + Server.rand.nextInt(secondaryWeapons[x].getWeightGrams() / 1000 + 3) + 1 + timeMod;
                           } else if (secondaryWeapons[x].isWeaponSlash() || secondaryWeapons[x].isWeaponSword() || secondaryWeapons[x].isWeaponAxe()) {
                              speed = 5 + Server.rand.nextInt(secondaryWeapons[x].getWeightGrams() / 1000 + 5) + 1 + timeMod;
                           } else if (secondaryWeapons[x].isWeaponCrush()) {
                              speed = 5 + Server.rand.nextInt(secondaryWeapons[x].getWeightGrams() / 1000 + 8) + 1 + timeMod;
                           } else {
                              speed = 5 + Server.rand.nextInt(secondaryWeapons[x].getWeightGrams() / 1000 + 10) + 1 + timeMod;
                           }

                           if (act.currentSecond() % speed == 0) {
                              Item defPrimWeapon = null;
                              Skill attackerFightSkill = null;
                              Skill defenderFightSkill = null;
                              Skills performerSkills = performer.getSkills();
                              Skills defenderSkills = defender.getSkills();
                              double attBonus = (double)performer.zoneBonus - (double)performer.getMovePenalty() * 0.5;
                              double defBonus = (double)(defender.zoneBonus - (float)defender.getMovePenalty());
                              if (defender.isMoving() && defender instanceof Player) {
                                 defBonus -= 5.0;
                              }

                              if (performer.isMoving() && performer instanceof Player) {
                                 attBonus -= 5.0;
                              }

                              attBonus += (double)posBonus;
                              defPrimWeapon = defender.getPrimWeapon();
                              int attSknum = 1023;

                              try {
                                 attackerFightSkill = performerSkills.getSkill(1023);
                              } catch (NoSuchSkillException var35) {
                                 attackerFightSkill = performerSkills.learn(1023, 1.0F);
                              }

                              int defSknum = 1023;

                              try {
                                 defenderFightSkill = defenderSkills.getSkill(1023);
                              } catch (NoSuchSkillException var34) {
                                 defenderFightSkill = defenderSkills.learn(1023, 1.0F);
                              }

                              dead = performAttack(
                                 pos,
                                 false,
                                 performer,
                                 performerSkills,
                                 defender,
                                 defenderSkills,
                                 secondaryWeapons[x],
                                 defPrimWeapon,
                                 attBonus,
                                 defBonus,
                                 attackerFightSkill,
                                 defenderFightSkill,
                                 speed
                              );
                              if (dead) {
                                 done = true;
                              }
                           }
                        }
                     }
                  }
               }

               performer.getStatus().modifyStamina(-50.0F);
               if (done) {
                  if (aiming) {
                     if (dead) {
                        defender.setOpponent(null);
                        defender.setTarget(-10L, true);
                        performer.setTarget(-10L, true);
                        performer.setOpponent(null);
                        if (performer.getCitizenVillage() != null) {
                           performer.getCitizenVillage().removeTarget(defender);
                        }

                        if (defender instanceof Player && performer instanceof Player) {
                           try {
                              Players.getInstance().addKill(performer.getWurmId(), defender.getWurmId(), defender.getName());
                           } catch (Exception var33) {
                              logger.log(
                                 Level.INFO,
                                 "Failed to add kill for " + performer.getName() + ":" + defender.getName() + " - " + var33.getMessage(),
                                 (Throwable)var33
                              );
                           }

                           if (!performer.isOnPvPServer() || !defender.isOnPvPServer()) {
                              boolean okToKill = false;
                              if (performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(defender.getCitizenVillage())) {
                                 okToKill = true;
                              }

                              if (defender.getKingdomId() == performer.getKingdomId()
                                 && defender.getKingdomTemplateId() != 3
                                 && defender.getReputation() >= 0
                                 && !okToKill) {
                                 performer.setReputation(performer.getReputation() - 20);
                              }
                           }
                        }
                     }
                  } else {
                     performer.setOpponent(null);
                     if (dead) {
                        defender.setOpponent(null);
                        defender.setTarget(-10L, true);
                        performer.setTarget(-10L, true);
                        if (performer.getCitizenVillage() != null) {
                           performer.getCitizenVillage().removeTarget(defender);
                        }

                        if (defender instanceof Player && performer instanceof Player) {
                           try {
                              Players.getInstance().addKill(performer.getWurmId(), defender.getWurmId(), defender.getName());
                           } catch (Exception var32) {
                              logger.log(
                                 Level.INFO,
                                 "Failed to add kill for " + performer.getName() + ":" + defender.getName() + " - " + var32.getMessage(),
                                 (Throwable)var32
                              );
                           }

                           if (!performer.isOnPvPServer() || !defender.isOnPvPServer()) {
                              boolean okToKill = false;
                              if (performer.getCitizenVillage() != null && performer.getCitizenVillage().isEnemy(defender.getCitizenVillage())) {
                                 okToKill = true;
                              }

                              if (defender.getKingdomId() == performer.getKingdomId()
                                 && defender.getKingdomId() != 3
                                 && defender.getReputation() >= 0
                                 && !okToKill) {
                                 performer.setReputation(performer.getReputation() - 20);
                              }
                           }
                        }
                     }
                  }

                  if (dead && !(defender instanceof Player) && performer instanceof Player) {
                     try {
                        int tid = defender.getTemplate().getTemplateId();
                        if (CreatureTemplate.isDragon(tid)) {
                           ((Player)performer).addTitle(Titles.Title.DragonSlayer);
                        } else if (tid == 11 || tid == 27) {
                           ((Player)performer).addTitle(Titles.Title.TrollSlayer);
                        } else if (tid == 20 || tid == 22) {
                           ((Player)performer).addTitle(Titles.Title.GiantSlayer);
                        }

                        if (defender.isUnique()) {
                           HistoryManager.addHistory(performer.getNameWithGenus(), "slayed " + defender.getNameWithGenus());
                        }
                     } catch (Exception var38) {
                        logger.log(
                           Level.WARNING,
                           "Defender: " + defender.getName() + " and attacker: " + performer.getName() + ":" + var38.getMessage(),
                           (Throwable)var38
                        );
                     }
                  }
               }

               return done;
            }
         }
      }
   }

   public static float getEnchantBonus(Item item, Creature defender) {
      if (item.enchantment != 0) {
         if (item.enchantment == 11) {
            if (defender.isAnimal()) {
               return 10.0F;
            }
         } else if (item.enchantment == 9) {
            if (defender.isHuman()) {
               return 10.0F;
            }
         } else if (item.enchantment == 10) {
            if (defender.isRegenerating()) {
               return 10.0F;
            }
         } else if (item.enchantment == 12) {
            if (defender.isDragon()) {
               return 10.0F;
            }
         } else if (defender.getDeity() != null) {
            Deity d = defender.getDeity();
            if (d.number == 1) {
               if (item.enchantment == 1) {
                  return 10.0F;
               }
            } else if (d.number == 4) {
               if (item.enchantment == 4) {
                  return 10.0F;
               }
            } else if (d.number == 2) {
               if (item.enchantment == 2) {
                  return 10.0F;
               }
            } else if (d.number == 3 && item.enchantment == 3) {
               return 10.0F;
            }
         }
      }

      return 0.0F;
   }

   public static final boolean checkEnchantDestruction(Item item, Item defw, Creature defender) {
      if (item.enchantment != 0 && defw.enchantment != 0) {
         boolean destroyed = false;
         if ((defw.enchantment != 1 || item.enchantment != 5) && (item.enchantment != 1 || defw.enchantment != 5)) {
            if ((defw.enchantment != 4 || item.enchantment != 8) && (item.enchantment != 4 || defw.enchantment != 8)) {
               if ((defw.enchantment != 2 || item.enchantment != 6) && (item.enchantment != 2 || defw.enchantment != 6)) {
                  if (defw.enchantment == 3 && item.enchantment == 7 || item.enchantment == 3 && defw.enchantment == 7) {
                     if (!item.isArtifact()) {
                        item.enchant((byte)0);
                     }

                     if (!defw.isArtifact()) {
                        defw.enchant((byte)0);
                     }

                     destroyed = true;
                  }
               } else {
                  if (!item.isArtifact()) {
                     item.enchant((byte)0);
                  }

                  if (!defw.isArtifact()) {
                     defw.enchant((byte)0);
                  }

                  destroyed = true;
               }
            } else {
               if (!item.isArtifact()) {
                  item.enchant((byte)0);
               }

               if (!defw.isArtifact()) {
                  defw.enchant((byte)0);
               }

               destroyed = true;
            }
         } else {
            if (!item.isArtifact()) {
               item.enchant((byte)0);
            }

            if (!defw.isArtifact()) {
               defw.enchant((byte)0);
            }

            destroyed = true;
         }

         if (destroyed) {
            int tilex = defender.getTileX();
            int tiley = defender.getTileY();
            VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, defender.isOnSurface());
            vtile.broadCast("A bright light emanates from where the " + item.getName() + " and the " + defw.getName() + " meet!");
         }
      }

      return false;
   }

   public static final float getMod(Creature performer, Creature defender, Skill skill) {
      float mod = 1.0F;
      if (defender instanceof Player && performer instanceof Player) {
         mod = 3.0F;

         try {
            if (Players.getInstance().isOverKilling(performer.getWurmId(), defender.getWurmId())) {
               mod = 0.1F;
            }
         } catch (Exception var5) {
            logger.log(Level.WARNING, performer.getName() + " failed to retrieve pk", (Throwable)var5);
         }
      } else if (performer instanceof Player && skill.getRealKnowledge() >= 50.0) {
         mod = 0.5F;
      }

      return mod;
   }

   public static boolean isEye(int pos) {
      return pos == 18 || pos == 19 || pos == 20;
   }

   public static int getRealPosition(int pos) {
      int rand = 10;
      if (pos == 1) {
         rand = Server.rand.nextInt(100);
         if (rand < 50) {
            pos = 17;
         }
      } else if (pos == 29) {
         rand = Server.rand.nextInt(100);
         if (rand < 97) {
            pos = 29;
         } else if (rand < 98) {
            pos = 18;
         } else if (rand < 99) {
            pos = 19;
         }
      } else if (pos == 2) {
         rand = Server.rand.nextInt(20);
         if (rand < 5) {
            pos = 21;
         } else if (rand < 7) {
            pos = 27;
         } else if (rand < 9) {
            pos = 26;
         } else if (rand < 12) {
            pos = 32;
         } else if (rand < 14) {
            pos = 23;
         } else if (rand < 18) {
            pos = 24;
         } else if (rand < 20) {
            pos = 25;
         }
      } else if (pos == 3) {
         rand = Server.rand.nextInt(10);
         if (rand < 5) {
            pos = 5;
         } else if (rand < 9) {
            pos = 9;
         } else {
            pos = 13;
         }
      } else if (pos == 4) {
         rand = Server.rand.nextInt(10);
         if (rand < 5) {
            pos = 6;
         } else if (rand < 9) {
            pos = 10;
         } else {
            pos = 14;
         }
      } else if (pos == 34) {
         rand = Server.rand.nextInt(20);
         if (rand < 5) {
            pos = 7;
         } else if (rand < 9) {
            pos = 11;
         } else if (rand < 10) {
            pos = 15;
         }

         if (rand < 15) {
            pos = 8;
         } else if (rand < 19) {
            pos = 12;
         } else {
            pos = 16;
         }
      }

      return pos;
   }

   @Deprecated
   protected static boolean performAttack(
      int pos,
      boolean aiming,
      Creature performer,
      Skills performerSkills,
      Creature defender,
      Skills defenderSkills,
      Item attWeapon,
      Item defPrimWeapon,
      double attBonus,
      double defBonus,
      Skill attackerFightSkill,
      Skill defenderFightSkill,
      int counter
   ) {
      boolean shieldBlocked = false;
      boolean done = false;
      Skill primWeaponSkill = null;
      Skill defPrimWeaponSkill = null;
      Item defShield = null;
      Skill defShieldSkill = null;
      int skillnum = -10;
      boolean dryrun = false;
      if (performer.isPlayer()) {
         if (!defender.isPlayer() && !defender.isReborn()) {
            if (defender.isKingdomGuard() || defender.isSpiritGuard() && defender.getKingdomId() == performer.getKingdomId()) {
               dryrun = true;
            }
         } else {
            dryrun = true;
         }
      } else if (performer.isKingdomGuard() || performer.isSpiritGuard() && defender.getKingdomId() == performer.getKingdomId()) {
         dryrun = true;
      }

      if (defender.isPlayer() && !defender.hasLink() || performer.isPlayer() && !performer.hasLink()) {
         dryrun = true;
      }

      if (defender.getStatus().getStunned() > 0.0F) {
         defBonus -= 20.0;
         attBonus += 20.0;
      }

      if (attWeapon != null) {
         if (attWeapon.isBodyPart()) {
            try {
               skillnum = 10052;
               primWeaponSkill = performerSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var51) {
               if (skillnum != -10) {
                  primWeaponSkill = performerSkills.learn(skillnum, 1.0F);
               }
            }

            if (performer.isPlayer() && defender.isPlayer() && primWeaponSkill.getKnowledge(0.0) >= 20.0) {
               dryrun = true;
            }
         } else {
            try {
               skillnum = attWeapon.getPrimarySkill();
               primWeaponSkill = performerSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var52) {
               if (skillnum != -10) {
                  primWeaponSkill = performerSkills.learn(skillnum, 1.0F);
               }
            }
         }
      }

      skillnum = -10;
      if (defPrimWeapon != null) {
         if (defPrimWeapon.isBodyPart()) {
            try {
               skillnum = 10052;
               defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var49) {
               if (skillnum != -10) {
                  defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0F);
               }
            }

            if (performer.isPlayer() && defender.isPlayer() && defPrimWeaponSkill.getKnowledge(0.0) >= 20.0) {
               dryrun = true;
            }
         } else {
            try {
               skillnum = defPrimWeapon.getPrimarySkill();
               defPrimWeaponSkill = defenderSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var50) {
               if (skillnum != -10) {
                  defPrimWeaponSkill = defenderSkills.learn(skillnum, 1.0F);
               }
            }
         }
      }

      Skill attStrengthSkill = null;

      try {
         attStrengthSkill = performerSkills.getSkill(102);
      } catch (NoSuchSkillException var47) {
         attStrengthSkill = performerSkills.learn(102, 1.0F);
         logger.log(Level.WARNING, performer.getName() + " had no strength. Weird.");
      }

      double bonus = 0.0;
      if (primWeaponSkill != null) {
         float mod = getMod(performer, defender, primWeaponSkill);
         bonus = Math.max(
            -20.0,
            primWeaponSkill.skillCheck(
               Math.abs(primWeaponSkill.getKnowledge(0.0) - (double)attWeapon.getCurrentQualityLevel()),
               attBonus,
               mod == 0.0F || dryrun,
               (float)((long)Math.max(1.0F, (float)counter * mod))
            )
         );
      }

      skillnum = -10;
      defShield = defender.getShield();
      if (defShield != null) {
         try {
            skillnum = defShield.getPrimarySkill();
            defShieldSkill = defenderSkills.getSkill(skillnum);
         } catch (NoSuchSkillException var48) {
            if (skillnum != -10) {
               defShieldSkill = defenderSkills.learn(skillnum, 1.0F);
            }
         }
      }

      if (aiming) {
         if (pos == 1) {
            bonus = -60.0;
         } else if (pos == 29) {
            bonus = -80.0;
         } else if (pos == 2) {
            bonus = -40.0;
         } else if (pos == 3) {
            bonus = -30.0;
         } else if (pos == 4) {
            bonus = -30.0;
         } else if (pos == 34) {
            bonus = -30.0;
         }

         pos = getRealPosition(pos);
      } else {
         try {
            pos = defender.getBody().getRandomWoundPos();
         } catch (Exception var46) {
            logger.log(Level.WARNING, "Problem getting a Random Wound Position for " + defender.getName() + ": due to " + var46.getMessage(), (Throwable)var46);
         }
      }

      if (performer.getEnemyPresense() > 1200 && defender.isPlayer()) {
         bonus += 20.0;
      }

      double attCheck = 0.0;
      boolean defFumbleShield = false;
      boolean defFumbleParry = false;
      boolean crit = false;
      if (defender.isPlayer()) {
         int critChance = attWeapon.getDamagePercent();
         if (Server.rand.nextInt(100 - Math.min(3, critChance)) == 0) {
            crit = true;
         }
      }

      if (attWeapon.isBodyPartAttached()) {
         float mod = getMod(performer, defender, attackerFightSkill);
         attCheck = attackerFightSkill.skillCheck(
            defenderFightSkill.getKnowledge(defBonus) / (double)Math.min(5, defender.getAttackers()),
            bonus,
            mod == 0.0F || dryrun,
            (float)((long)Math.max(1.0F, (float)counter * mod))
         );
      } else {
         float mod = getMod(performer, defender, attackerFightSkill);
         attCheck = attackerFightSkill.skillCheck(
            defenderFightSkill.getKnowledge(defBonus) / (double)Math.min(5, defender.getAttackers()),
            attWeapon,
            bonus,
            mod == 0.0F || dryrun,
            (float)((long)Math.max(1.0F, (float)counter * mod))
         );
      }

      byte type = performer.getTemplate().combatDamageType;
      if (attWeapon.isWeaponSword()) {
         if (Server.rand.nextInt(2) == 0) {
            type = 1;
         } else {
            type = 2;
         }
      } else if (attWeapon.isWeaponSlash()) {
         type = 1;
      } else if (attWeapon.isWeaponPierce()) {
         type = 2;
      } else if (attWeapon.isBodyPart()) {
         if (attWeapon.getTemplateId() == 17) {
            type = 3;
         } else if (attWeapon.getTemplateId() == 12) {
            type = 0;
         }
      }

      String attString = getAttackString(performer, attWeapon, type);
      double defCheck = 0.0;
      double damage = getWeaponDamage(attWeapon, attStrengthSkill);
      if (performer.getDeity() != null && performer.getDeity().isWarrior() && performer.getFaith() >= 40.0F && performer.getFavor() >= 20.0F) {
         damage = Math.min(4000.0, damage * 1.25);
      }

      if (performer.getEnemyPresense() > 1200 && defender.isPlayer()) {
         damage *= (double)Math.min(4000.0F, 1.15F);
      }

      if (defShield != null || crit) {
         if (!crit) {
            if (pos == 9) {
               shieldBlocked = true;
            } else if (defender.getStatus().getStamina() >= 300 && !defender.isMoving()) {
               defCheck = 0.0;
               if (defShieldSkill != null) {
                  float mod = getMod(performer, defender, defShieldSkill);
                  defCheck = defShieldSkill.skillCheck(attCheck, defShield, defBonus, mod == 0.0F || dryrun, (float)((long)mod));
               }

               defCheck += (double)(defShield.getSizeY() + defShield.getSizeZ()) / 10.0;
               defender.getStatus().modifyStamina(-300.0F);
            }
         }

         if (defCheck > 0.0 || shieldBlocked) {
            shieldBlocked = true;
            if (defender.isPlayer()) {
               defShield.setDamage(defShield.getDamage() + 0.001F * (float)damage * defShield.getDamageModifier());
            }
         } else if (defCheck < -90.0) {
            defFumbleShield = true;
         }
      }

      if (shieldBlocked && !crit) {
         if (performer.spamMode()) {
            if (aiming) {
               performer.getCommunicator()
                  .sendNormalServerMessage(defender.getNameWithGenus() + " raises " + defender.getHisHerItsString() + " shield and parries.");
            } else {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You try to "
                        + attString
                        + " "
                        + defender.getNameWithGenus()
                        + " but "
                        + defender.getHeSheItString()
                        + " raises "
                        + defender.getHisHerItsString()
                        + " shield and parries."
                  );
            }
         } else if (aiming) {
            performer.getCommunicator()
               .sendNormalServerMessage(defender.getNameWithGenus() + " raises " + defender.getHisHerItsString() + " shield and parries.");
         }

         if (defender.spamMode()) {
            defender.getCommunicator()
               .sendNormalServerMessage(performer.getNameWithGenus() + " tries to " + attString + " you but you raise your shield and parry.");
         }

         if (defShield.isWood()) {
            Methods.sendSound(defender, "sound.combat.shield.wood");
         } else {
            Methods.sendSound(defender, "sound.combat.shield.metal");
         }

         checkEnchantDestruction(attWeapon, defShield, defender);
      } else {
         boolean parryPrimWeapon = true;
         defCheck = 0.0;
         if (!crit && !defender.isMoving()) {
            int parryTime = 100;
            if (defender.getFightStyle() == 2) {
               parryTime = 40;
            } else if (defender.getFightStyle() == 1) {
               parryTime = 160;
            }

            if (WurmCalendar.currentTime > defender.lastParry + (long)Server.rand.nextInt(parryTime) && defPrimWeapon != null && !defPrimWeapon.isWeaponAxe()) {
               if (defPrimWeapon.isBodyPart() && defPrimWeapon.getAuxData() != 100) {
                  if (defender.getStatus().getStamina() >= 300) {
                     if (defPrimWeaponSkill != null) {
                        float mod = getMod(performer, defender, defPrimWeaponSkill);
                        defCheck = defPrimWeaponSkill.skillCheck(
                           (double)Math.min(100, 80 * defender.getAttackers()), defBonus, mod == 0.0F || dryrun, (float)((long)mod)
                        );
                        defender.lastParry = WurmCalendar.currentTime;
                        defender.getStatus().modifyStamina(-300.0F);
                     }

                     if (defCheck < 0.0 && Server.rand.nextInt(50) == 0) {
                        defCheck = secondaryParry(performer, attCheck, defender, defenderSkills, defCheck, defBonus, dryrun);
                        if (defCheck < -90.0) {
                           defFumbleParry = true;
                        } else if (defCheck > 0.0) {
                           parryPrimWeapon = false;
                        }
                     }
                  }
               } else if (defender.getStatus().getStamina() >= 300) {
                  if (defPrimWeaponSkill != null) {
                     float mod = getMod(performer, defender, defPrimWeaponSkill);
                     defCheck = defPrimWeaponSkill.skillCheck(
                        (attCheck * (double)defender.getAttackers() + (double)defPrimWeapon.getWeightGrams() / 200.0)
                           / (double)getWeaponParryBonus(defPrimWeapon),
                        defPrimWeapon,
                        defBonus,
                        mod == 0.0F || dryrun,
                        (float)((long)mod)
                     );
                     defender.lastParry = WurmCalendar.currentTime;
                     defender.getStatus().modifyStamina(-300.0F);
                  }

                  if (defCheck < -90.0) {
                     defFumbleParry = true;
                  } else if (defCheck < 0.0 && Server.rand.nextInt(50) == 0) {
                     defCheck = secondaryParry(performer, attCheck, defender, defenderSkills, defCheck, defBonus, dryrun);
                     if (defCheck < -90.0) {
                        defFumbleParry = true;
                     } else if (defCheck > 0.0) {
                        parryPrimWeapon = false;
                     }
                  }
               }
            }
         }

         if (!(defCheck <= 0.0) && !defFumbleShield && !crit) {
            defender.lastParry = WurmCalendar.currentTime;
            Item weapon = defPrimWeapon;
            if (!parryPrimWeapon) {
               weapon = defender.getLefthandWeapon();
            }

            if (aiming || performer.spamMode()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(defender.getNameWithGenus() + " " + getParryString(defCheck) + " parries with " + weapon.getNameWithGenus() + ".");
            }

            if (defender.spamMode()) {
               defender.getCommunicator().sendNormalServerMessage("You " + getParryString(defCheck) + " parry with your " + weapon.getName() + ".");
            }

            if (!weapon.isBodyPart() || weapon.getAuxData() == 100) {
               if (defender.isPlayer()) {
                  if (weapon.isWeaponSword()) {
                     weapon.setDamage(weapon.getDamage() + 0.001F * (float)damage * weapon.getDamageModifier());
                  } else {
                     weapon.setDamage(weapon.getDamage() + 0.005F * (float)damage * weapon.getDamageModifier());
                  }
               }

               if (performer.isPlayer() && (!attWeapon.isBodyPart() || attWeapon.getAuxData() == 100)) {
                  attWeapon.setDamage(attWeapon.getDamage() + 0.001F * (float)damage * attWeapon.getDamageModifier());
               }
            }

            String sstring = "sound.combat.parry1";
            int x = Server.rand.nextInt(3);
            if (x == 0) {
               sstring = "sound.combat.parry2";
            } else if (x == 1) {
               sstring = "sound.combat.parry3";
            }

            SoundPlayer.playSound(sstring, defender, 1.6F);
            checkEnchantDestruction(attWeapon, weapon, defender);
         } else {
            if (!defFumbleShield && !defFumbleParry && !crit && defender.getStatus().getStamina() >= 300) {
               defender.getStatus().modifyStamina(-300.0F);
               Skill defenderBodyControl = null;

               try {
                  defenderBodyControl = defenderSkills.getSkill(104);
               } catch (NoSuchSkillException var45) {
                  defenderBodyControl = defenderSkills.learn(104, 1.0F);
                  logger.log(Level.WARNING, defender.getName() + " no body control?");
               }

               if (defenderBodyControl == null) {
                  logger.log(Level.WARNING, defender.getName() + " has no body control!");
               } else {
                  float mod = getMod(performer, defender, defenderBodyControl);
                  defCheck = defenderBodyControl.skillCheck(attCheck, 0.0, mod == 0.0F || dryrun, (float)((long)mod));
               }
            }

            if (!(defCheck <= 0.0) && !crit) {
               String sstring = "sound.combat.miss.light";
               if (attCheck < -80.0) {
                  sstring = "sound.combat.miss.heavy";
               } else if (attCheck < -40.0) {
                  sstring = "sound.combat.miss.med";
               }

               SoundPlayer.playSound(sstring, defender, 1.6F);
               if (aiming || performer.spamMode()) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        defender.getNameWithGenus()
                           + " "
                           + getParryString(defCheck)
                           + " evades the blow to the "
                           + defender.getBody().getWoundLocationString(pos)
                           + "."
                     );
               }

               if (defender.spamMode()) {
                  defender.getCommunicator()
                     .sendNormalServerMessage(
                        "You " + getParryString(defCheck) + " evade the blow to the " + defender.getBody().getWoundLocationString(pos) + "."
                     );
               }
            } else {
               Item armour = null;
               float armourMod = defender.getArmourMod();
               float evasionChance = ArmourTemplate.calculateGlanceRate(defender.getArmourType(), armour, type, armourMod);
               if (!performer.isPlayer() && !defender.isPlayer() && !defender.isUnique()) {
                  armourMod = 1.0F;
               }

               if (armourMod == 1.0F) {
                  try {
                     byte bodyPosition = ArmourTemplate.getArmourPosition((byte)pos);
                     armour = defender.getArmour(bodyPosition);
                     armourMod = ArmourTemplate.calculateDR(armour, type);
                     if (defender.isPlayer()) {
                        armour.setDamage(
                           armour.getDamage()
                              + Math.min(
                                 1.0F,
                                 (float)(damage * (double)armourMod / 80.0) * armour.getDamageModifier() * ArmourTemplate.getArmourDamageModFor(armour, type)
                              )
                        );
                     }

                     checkEnchantDestruction(attWeapon, armour, defender);
                     evasionChance = ArmourTemplate.calculateGlanceRate(null, armour, type, armourMod);
                  } catch (NoArmourException var43) {
                     evasionChance = 1.0F - defender.getArmourMod();
                  } catch (NoSpaceException var44) {
                     logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos);
                  }
               }

               if ((!attWeapon.isBodyPart() || attWeapon.getAuxData() == 100) && performer.isPlayer()) {
                  attWeapon.setDamage(attWeapon.getDamage() + (float)(damage * (2.1 - (double)armourMod) / 1000.0) * attWeapon.getDamageModifier());
               }

               if (defender.isUnique()) {
                  evasionChance = 0.5F;
                  damage *= (double)armourMod;
               }

               if (Server.rand.nextFloat() < evasionChance) {
                  if (aiming || performer.spamMode()) {
                     performer.getCommunicator().sendNormalServerMessage("Your attack glances off " + defender.getNameWithGenus() + "'s armour.");
                  }

                  if (defender.spamMode()) {
                     defender.getCommunicator()
                        .sendNormalServerMessage("The attack to the " + defender.getBody().getWoundLocationString(pos) + " glances off your armour.");
                  }
               } else if (!(damage > (double)(5.0F + Server.rand.nextFloat() * 5.0F)) && !crit) {
                  if (aiming || performer.spamMode()) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           defender.getNameWithGenus() + " takes no real damage from the hit to the " + defender.getBody().getWoundLocationString(pos) + "."
                        );
                  }

                  if (defender.spamMode()) {
                     defender.getCommunicator()
                        .sendNormalServerMessage("You take no real damage from the blow to the " + defender.getBody().getWoundLocationString(pos) + ".");
                  }
               } else {
                  if (crit) {
                     armourMod = 1.0F;
                  }

                  Battle battle = performer.getBattle();
                  boolean dead = false;
                  if (defender.getStaminaSkill().getKnowledge(0.0) < 2.0) {
                     defender.die(false, "Combat Stam Check Fail");
                     dead = true;
                  } else {
                     dead = addWound(performer, defender, type, pos, damage, armourMod, attString, battle, 0.0F, 0.0F, false, false, false, false);
                  }

                  if (!dead
                     && attWeapon.getSpellDamageBonus() > 0.0F
                     && (damage * (double)attWeapon.getSpellDamageBonus() / 300.0 > (double)(Server.rand.nextFloat() * 5.0F) || crit)) {
                     dead = defender.addWoundOfType(
                        performer,
                        (byte)4,
                        (byte)pos,
                        false,
                        armourMod,
                        false,
                        damage * (double)attWeapon.getSpellDamageBonus() / 300.0,
                        0.0F,
                        0.0F,
                        false,
                        false
                     );
                  }

                  if (!dead
                     && attWeapon.getWeaponSpellDamageBonus() > 0.0F
                     && damage * (double)attWeapon.getWeaponSpellDamageBonus() / 300.0 > (double)(Server.rand.nextFloat() * 5.0F)) {
                     dead = defender.addWoundOfType(
                        performer,
                        (byte)6,
                        1,
                        true,
                        armourMod,
                        false,
                        damage * (double)attWeapon.getWeaponSpellDamageBonus() / 300.0,
                        (float)Server.rand.nextInt((int)attWeapon.getWeaponSpellDamageBonus()),
                        0.0F,
                        false,
                        false
                     );
                  }

                  if (armour != null && armour.getSpellPainShare() > 0.0F) {
                     if (performer.isUnique()) {
                        defender.getCommunicator()
                           .sendNormalServerMessage(performer.getNameWithGenus() + " ignores the effects of the " + armour.getName() + ".");
                     } else if (damage * (double)armour.getSpellPainShare() / 300.0 > 5.0) {
                        addBounceWound(defender, performer, type, pos, damage * (double)armour.getSpellPainShare() / 300.0, armourMod, 0.0F, 0.0F, false, true);
                     }
                  }

                  if (dead) {
                     performer.getCommunicator().sendSafeServerMessage(defender.getNameWithGenus() + " is dead!");
                     if (battle != null) {
                        battle.addCasualty(performer, defender);
                     }

                     done = true;
                  } else if (!defender.hasNoServerSound()) {
                     SoundPlayer.playSound(defender.getHitSound(), defender, 1.6F);
                  }
               }
            }
         }
      }

      return done;
   }

   public static boolean addWound(
      @Nullable Creature performer,
      Creature defender,
      byte type,
      int pos,
      double damage,
      float armourMod,
      String attString,
      @Nullable Battle battle,
      float infection,
      float poison,
      boolean archery,
      boolean alreadyCalculatedResist,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, type, pos, armourMod, damage);
      }

      if (defender != null && defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, type, pos, armourMod, damage);
      }

      if (!alreadyCalculatedResist) {
         if ((type == 8 || type == 4 || type == 10) && defender.getCultist() != null && defender.getCultist().hasNoElementalDamage()) {
            return false;
         }

         if (defender.hasSpellEffect((byte)69)) {
            damage *= 0.8F;
         }

         damage *= (double)Wound.getResistModifier(performer, defender, type);
      }

      boolean dead = false;
      if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
         if (defender.hasSpellEffect((byte)68)) {
            defender.reduceStoneSkin();
            return false;
         }

         Wound wound = null;
         boolean foundWound = false;
         String broadCastString = "";
         String otherString = CombatHandler.getOthersString() == "" ? attString + "s" : CombatHandler.getOthersString();
         CombatHandler.setOthersString("");
         if (Server.rand.nextInt(10) <= 6 && defender.getBody().getWounds() != null) {
            wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, type);
         }

         if (wound != null) {
            defender.setWounded();
            if (infection > 0.0F) {
               wound.setInfectionSeverity(Math.min(99.0F, wound.getInfectionSeverity() + (float)Server.rand.nextInt((int)infection)));
            }

            if (poison > 0.0F) {
               wound.setPoisonSeverity(Math.min(99.0F, wound.getPoisonSeverity() + poison));
            }

            wound.setBandaged(false);
            if (wound.getHealEff() > 0 && Server.rand.nextInt(2) == 0) {
               wound.setHealeff((byte)0);
            }

            dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null ? performer.isPlayer() : false, spell);
            foundWound = true;
         } else {
            if (!defender.isPlayer()) {
               wound = new TempWound(type, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
            } else {
               wound = new DbWound(
                  type, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null ? performer.isPlayer() : false, spell
               );
            }

            defender.setWounded();
         }

         if (performer != null && !attString.isEmpty()) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new CreatureLineSegment(performer));
            segments.add(new MulticolorLineSegment(" " + otherString + " ", (byte)0));
            if (performer == defender) {
               segments.add(new MulticolorLineSegment(performer.getHimHerItString() + "self", (byte)0));
            } else {
               segments.add(new CreatureLineSegment(defender));
            }

            segments.add(
               new MulticolorLineSegment(
                  " "
                     + getStrengthString(damage / 1000.0)
                     + " in the "
                     + defender.getBody().getWoundLocationString(wound.getLocation())
                     + " and "
                     + getRealDamageString(damage * (double)armourMod),
                  (byte)0
               )
            );
            segments.add(new MulticolorLineSegment("s it.", (byte)0));
            MessageServer.broadcastColoredAction(segments, performer, defender, 5, true);

            for(MulticolorLineSegment s : segments) {
               broadCastString = broadCastString + s.getText();
            }

            if (performer != defender) {
               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            segments.get(1).setText(" " + attString + " ");
            segments.get(4).setText(" it.");

            for(MulticolorLineSegment s : segments) {
               s.setColor((byte)3);
            }

            performer.getCommunicator().sendColoredMessageCombat(segments);
         }

         if (defender.isDominated()) {
            if (!archery) {
               if (!defender.isReborn() || defender.getMother() != -10L) {
                  defender.modifyLoyalty(-((float)((int)(damage * (double)armourMod)) * defender.getBaseCombatRating() / 200000.0F));
               }
            } else if (defender.getDominator() == performer) {
               defender.modifyLoyalty(-((float)((int)(damage * (double)armourMod)) * defender.getBaseCombatRating() / 200000.0F));
            }
         }

         if (infection > 0.0F && !attString.isEmpty() && performer != null) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
            segments.add(new MulticolorLineSegment(" infects ", (byte)3));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" with a disease.", (byte)3));
            performer.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(performer));

            for(MulticolorLineSegment s : segments) {
               s.setColor((byte)7);
            }

            defender.getCommunicator().sendColoredMessageCombat(segments);
         }

         if (poison > 0.0F && !attString.isEmpty() && performer != null) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
            segments.add(new MulticolorLineSegment(" poisons ", (byte)3));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(".", (byte)3));
            performer.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(performer));

            for(MulticolorLineSegment s : segments) {
               s.setColor((byte)7);
            }

            defender.getCommunicator().sendColoredMessageCombat(segments);
         }

         if (battle != null && performer != null) {
            battle.addEvent(new BattleEvent((short)114, performer.getName(), defender.getName(), broadCastString));
         }

         if (!foundWound) {
            dead = defender.getBody().addWound(wound);
         }
      }

      return dead;
   }

   public static boolean addRotWound(
      @Nullable Creature performer,
      Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)6, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)6, pos, armourMod, damage);
      }

      boolean dead = false;
      if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
         if (defender.hasSpellEffect((byte)68)) {
            defender.reduceStoneSkin();
            return false;
         }

         Wound wound = null;
         boolean foundWound = false;
         if (performer != null) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
            segments.add(new MulticolorLineSegment(" infects ", (byte)3));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" with a disease.", (byte)3));
            performer.getCommunicator().sendColoredMessageCombat(segments);
            segments.set(0, new CreatureLineSegment(performer));

            for(MulticolorLineSegment s : segments) {
               s.setColor((byte)7);
            }

            defender.getCommunicator().sendColoredMessageCombat(segments);
         }

         if (defender.getBody().getWounds() != null) {
            wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)6);
            if (wound != null) {
               if (wound.getType() != 6) {
                  wound = null;
               } else {
                  defender.setWounded();
                  wound.setBandaged(false);
                  dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
                  wound.setInfectionSeverity(Math.min(99.0F, wound.getInfectionSeverity() + infection));
                  foundWound = true;
               }
            }
         }

         if (wound == null) {
            if (WurmId.getType(defender.getWurmId()) == 1) {
               wound = new TempWound((byte)6, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
            } else {
               wound = new DbWound(
                  (byte)6, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
               );
            }
         }

         if (!foundWound) {
            dead = defender.getBody().addWound(wound);
         }
      }

      return dead;
   }

   public static boolean addFireWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)4, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)4, pos, armourMod, damage);
      }

      if (defender.getCultist() != null && defender.getCultist().hasNoElementalDamage()) {
         return false;
      } else {
         boolean dead = false;
         if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
            if (defender.hasSpellEffect((byte)68)) {
               defender.reduceStoneSkin();
               return false;
            }

            Wound wound = null;
            boolean foundWound = false;
            if (performer != null) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
               segments.add(new MulticolorLineSegment(" burns ", (byte)3));
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(".", (byte)3));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               segments.set(0, new CreatureLineSegment(performer));

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            if (defender.getBody().getWounds() != null) {
               wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)4);
               if (wound != null) {
                  if (wound.getType() != 4) {
                     wound = null;
                  } else {
                     defender.setWounded();
                     wound.setBandaged(false);
                     dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
                     foundWound = true;
                  }
               }
            }

            if (wound == null) {
               if (WurmId.getType(defender.getWurmId()) == 1) {
                  wound = new TempWound((byte)4, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
               } else {
                  wound = new DbWound(
                     (byte)4, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
                  );
               }
            }

            if (!foundWound) {
               dead = defender.getBody().addWound(wound);
            }
         }

         return dead;
      }
   }

   public static boolean addAcidWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)10, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)10, pos, armourMod, damage);
      }

      if (defender.getCultist() != null && defender.getCultist().hasNoElementalDamage()) {
         return false;
      } else {
         boolean dead = false;
         if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
            if (defender.hasSpellEffect((byte)68)) {
               defender.reduceStoneSkin();
               return false;
            }

            Wound wound = null;
            boolean foundWound = false;
            if (performer != null) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new MulticolorLineSegment("Acid from ", (byte)3));
               segments.add(new CreatureLineSegment(performer));
               segments.add(new MulticolorLineSegment(" dissolves ", (byte)3));
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(".", (byte)3));
               performer.getCommunicator().sendColoredMessageCombat(segments);

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            if (defender.getBody().getWounds() != null) {
               wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)10);
               if (wound != null) {
                  if (wound.getType() != 10) {
                     wound = null;
                  } else {
                     defender.setWounded();
                     wound.setBandaged(false);
                     dead = wound.modifySeverity((int)(damage / 2.0 * (double)armourMod), performer != null && performer.isPlayer(), spell);
                     foundWound = true;
                  }
               }
            }

            if (wound == null) {
               if (WurmId.getType(defender.getWurmId()) == 1) {
                  wound = new TempWound((byte)10, (byte)pos, (float)damage / 2.0F * armourMod, defender.getWurmId(), poison, infection, spell);
               } else {
                  wound = new DbWound(
                     (byte)10,
                     (byte)pos,
                     (float)damage / 2.0F * armourMod,
                     defender.getWurmId(),
                     poison,
                     infection,
                     performer != null && performer.isPlayer(),
                     spell
                  );
               }
            }

            if (!foundWound) {
               dead = defender.getBody().addWound(wound);
            }
         }

         return dead;
      }
   }

   public static boolean addInternalWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)9, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)9, pos, armourMod, damage);
      }

      if (!defender.isGhost() && !defender.isUnique()) {
         boolean dead = false;
         if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
            Wound wound = null;
            boolean foundWound = false;
            if (performer != null) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
               segments.add(new MulticolorLineSegment(" causes pain deep inside ", (byte)3));
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(".", (byte)3));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               segments.set(0, new CreatureLineSegment(performer));

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            if (defender.getBody().getWounds() != null) {
               wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)9);
               if (wound != null) {
                  if (wound.getType() != 9) {
                     wound = null;
                  } else {
                     defender.setWounded();
                     wound.setBandaged(false);
                     dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
                     foundWound = true;
                  }
               }
            }

            if (wound == null) {
               if (WurmId.getType(defender.getWurmId()) == 1) {
                  wound = new TempWound((byte)9, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
               } else {
                  wound = new DbWound(
                     (byte)9, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
                  );
               }
            }

            if (!foundWound) {
               dead = defender.getBody().addWound(wound);
            }
         }

         return dead;
      } else {
         return false;
      }
   }

   public static boolean addColdWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)8, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)8, pos, armourMod, damage);
      }

      if (defender.getCultist() != null && defender.getCultist().hasNoElementalDamage()) {
         return false;
      } else {
         boolean dead = false;
         if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
            if (defender.hasSpellEffect((byte)68)) {
               defender.reduceStoneSkin();
               return false;
            }

            Wound wound = null;
            boolean foundWound = false;
            if (performer != null) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
               segments.add(new MulticolorLineSegment(" freezes ", (byte)3));
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(".", (byte)3));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               segments.set(0, new CreatureLineSegment(performer));

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            if (defender.getBody().getWounds() != null) {
               wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)8);
               if (wound != null) {
                  if (wound.getType() != 8) {
                     wound = null;
                  } else {
                     defender.setWounded();
                     wound.setBandaged(false);
                     dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
                     foundWound = true;
                  }
               }
            }

            if (wound == null) {
               if (WurmId.getType(defender.getWurmId()) == 1) {
                  wound = new TempWound((byte)8, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
               } else {
                  wound = new DbWound(
                     (byte)8, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
                  );
               }
            }

            if (!foundWound) {
               dead = defender.getBody().addWound(wound);
            }
         }

         return dead;
      }
   }

   public static boolean addDrownWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, (byte)7, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, (byte)7, pos, armourMod, damage);
      }

      if (defender.getCultist() != null && defender.getCultist().hasNoElementalDamage()) {
         return false;
      } else if (defender.isSubmerged()) {
         return false;
      } else {
         boolean dead = false;
         if (damage * (double)armourMod > 500.0 || noMinimumDamage) {
            Wound wound = null;
            boolean foundWound = false;
            if (performer != null) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new MulticolorLineSegment("Your weapon", (byte)3));
               segments.add(new MulticolorLineSegment(" drowns ", (byte)3));
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(".", (byte)3));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               segments.set(0, new CreatureLineSegment(performer));

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               defender.getCommunicator().sendColoredMessageCombat(segments);
            }

            if (defender.getBody().getWounds() != null) {
               wound = defender.getBody().getWounds().getWoundTypeAtLocation((byte)pos, (byte)7);
               if (wound != null) {
                  if (wound.getType() != 7) {
                     wound = null;
                  } else {
                     defender.setWounded();
                     wound.setBandaged(false);
                     dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
                     foundWound = true;
                  }
               }
            }

            if (wound == null) {
               if (WurmId.getType(defender.getWurmId()) == 1) {
                  wound = new TempWound((byte)7, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
               } else {
                  wound = new DbWound(
                     (byte)7, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
                  );
               }
            }

            if (!foundWound) {
               dead = defender.getBody().addWound(wound);
            }
         }

         if (dead && defender.isPlayer()) {
            defender.achievement(98);
         }

         return dead;
      }
   }

   public static boolean addBounceWound(
      @Nullable Creature performer,
      @Nonnull Creature defender,
      byte type,
      int pos,
      double damage,
      float armourMod,
      float infection,
      float poison,
      boolean noMinimumDamage,
      boolean spell
   ) {
      if (performer != null && performer.getTemplate().getCreatureAI() != null) {
         damage = performer.getTemplate().getCreatureAI().causedWound(performer, defender, type, pos, armourMod, damage);
      }

      if (defender.getTemplate().getCreatureAI() != null) {
         damage = defender.getTemplate().getCreatureAI().receivedWound(defender, performer, type, pos, armourMod, damage);
      }

      boolean dead = false;
      Wound wound = null;
      boolean foundWound = false;
      if (performer != null) {
         ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
         segments.add(new MulticolorLineSegment("A sudden pain hits ", (byte)3));
         segments.add(new CreatureLineSegment(defender));
         segments.add(new MulticolorLineSegment(" in the same location that " + defender.getHeSheItString() + " hit ", (byte)3));
         segments.add(new CreatureLineSegment(performer));
         segments.add(new MulticolorLineSegment(".", (byte)3));
         performer.getCommunicator().sendColoredMessageCombat(segments);

         for(MulticolorLineSegment s : segments) {
            s.setColor((byte)7);
         }

         defender.getCommunicator().sendColoredMessageCombat(segments);
      }

      if (defender.getBody().getWounds() != null) {
         wound = defender.getBody().getWounds().getWoundAtLocation((byte)pos);
         if (wound != null) {
            defender.setWounded();
            wound.setBandaged(false);
            dead = wound.modifySeverity((int)(damage * (double)armourMod), performer != null && performer.isPlayer(), spell);
            foundWound = true;
         }
      }

      if (wound == null) {
         if (WurmId.getType(defender.getWurmId()) == 1) {
            wound = new TempWound(type, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, spell);
         } else {
            wound = new DbWound(
               type, (byte)pos, (float)damage * armourMod, defender.getWurmId(), poison, infection, performer != null && performer.isPlayer(), spell
            );
         }

         defender.setWounded();
      }

      if (!foundWound) {
         dead = defender.getBody().addWound(wound);
      }

      return dead;
   }

   @Deprecated
   public static double secondaryParry(
      Creature performer, double attCheck, Creature defender, Skills defenderSkills, double defCheck, double defBonus, boolean dryrun
   ) {
      Item leftWeapon = defender.getLefthandWeapon();
      if (leftWeapon != null) {
         int secSkillNum = -10;
         Skill secWeaponSkill = null;
         if (leftWeapon.isBodyPart() && leftWeapon.getAuxData() != 100) {
            try {
               secSkillNum = 10052;
               secWeaponSkill = defenderSkills.getSkill(secSkillNum);
            } catch (NoSuchSkillException var15) {
               if (secSkillNum != -10) {
                  secWeaponSkill = defenderSkills.learn(secSkillNum, 1.0F);
               }
            }

            if (performer.isPlayer() && defender.isPlayer()) {
               secWeaponSkill = null;
            }
         } else {
            try {
               secSkillNum = leftWeapon.getPrimarySkill();
               secWeaponSkill = defenderSkills.getSkill(secSkillNum);
            } catch (NoSuchSkillException var14) {
               if (secSkillNum != -10) {
                  secWeaponSkill = defenderSkills.learn(secSkillNum, 1.0F);
               }
            }
         }

         if (secWeaponSkill != null) {
            float mod = getMod(performer, defender, secWeaponSkill);
            defCheck = secWeaponSkill.skillCheck(
               (attCheck * (double)defender.getAttackers() + (double)leftWeapon.getWeightGrams() / 200.0) / (double)getWeaponParryBonus(leftWeapon),
               leftWeapon,
               defBonus,
               mod == 0.0F || dryrun,
               (float)((long)mod)
            );
         }
      }

      return defCheck;
   }

   public static float getWeaponParryBonus(Item weapon) {
      return weapon.isWeaponSword() ? 4.0F : 1.0F;
   }

   public static String getMissString(boolean perf, double miss) {
      if (!perf) {
         if (miss < 10.0) {
            return "barely misses.";
         } else if (miss < 30.0) {
            return "misses by a few inches.";
         } else if (miss < 60.0) {
            return "misses by a decimeter.";
         } else if (miss < 90.0) {
            return "isn't even close.";
         } else {
            return miss < 100.0 ? "swings a huge hole in the air instead." : "misses.";
         }
      } else if (miss < 10.0) {
         return "barely miss.";
      } else if (miss < 30.0) {
         return "miss by a few inches.";
      } else if (miss < 60.0) {
         return "miss by a decimeter.";
      } else if (miss < 90.0) {
         return "aren't even close.";
      } else {
         return miss < 100.0 ? "swing a huge hole in the air instead." : "miss.";
      }
   }

   public static double getWeaponDamage(Item weapon, Skill attStrength) {
      if (weapon.isBodyPart() && weapon.getAuxData() != 100) {
         try {
            float base = Server.getInstance().getCreature(weapon.getOwnerId()).getCombatDamage(weapon);
            return (double)(base + Server.rand.nextFloat() * base * 2.0F);
         } catch (NoSuchCreatureException var4) {
            logger.log(Level.WARNING, "Could not find Creature owner of weapon: " + weapon + " due to " + var4.getMessage(), (Throwable)var4);
         } catch (NoSuchPlayerException var5) {
            logger.log(Level.WARNING, "Could not find Player owner of weapon: " + weapon + " due to " + var5.getMessage(), (Throwable)var5);
         }
      }

      float base = 6.0F;
      if (weapon.isWeaponSword()) {
         base = 24.0F;
      } else if (weapon.isWeaponAxe()) {
         base = 30.0F;
      } else if (weapon.isWeaponPierce()) {
         base = 12.0F;
      } else if (weapon.isWeaponSlash()) {
         base = 18.0F;
      } else if (weapon.isWeaponCrush()) {
         base = 36.0F;
      } else if (weapon.isBodyPart() && weapon.getAuxData() == 100) {
         base = 6.0F;
      }

      if (weapon.isWood()) {
         base *= 0.1F;
      } else if (weapon.isTool()) {
         base *= 0.3F;
      }

      base = (float)((double)base * (1.0 + attStrength.getKnowledge(0.0) / 100.0));
      float randomizer = (50.0F + Server.rand.nextFloat() * 50.0F) / 100.0F;
      return (double)base + (double)(randomizer * base * 4.0F * weapon.getQualityLevel() * (float)weapon.getDamagePercent()) / 10000.0;
   }

   public static String getParryString(double result) {
      String toReturn = "easily";
      if (result < 10.0) {
         toReturn = "barely";
      } else if (result < 30.0) {
         toReturn = "skillfully";
      } else if (result < 60.0) {
         toReturn = "safely";
      }

      return toReturn;
   }

   public static String getStrengthString(double damage) {
      if (damage <= 0.0) {
         return "unnoticeably";
      } else if (damage <= 1.0) {
         return "very lightly";
      } else if (damage <= 2.0) {
         return "lightly";
      } else if (damage <= 3.0) {
         return "pretty hard";
      } else if (damage <= 6.0) {
         return "hard";
      } else if (damage <= 10.0) {
         return "very hard";
      } else {
         return damage <= 20.0 ? "extremely hard" : "deadly hard";
      }
   }

   public static String getConjunctionString(double armour, double origDam, double realDam) {
      return armour > 0.4 && origDam - realDam > 5000.0 ? "but only" : "and";
   }

   public static String getRealDamageString(double damage) {
      if (damage < 500.0) {
         return "tickle";
      } else if (damage < 1000.0) {
         return "slap";
      } else if (damage < 2500.0) {
         return "irritate";
      } else if (damage < 5000.0) {
         return "hurt";
      } else {
         return damage < 10000.0 ? "harm" : "damage";
      }
   }

   public static String getAttackString(Creature attacker, Item weapon, byte woundType) {
      if (weapon.isWeaponSword()) {
         return woundType == 2 ? "pierce" : "cut";
      } else if (weapon.isWeaponPierce()) {
         return "pierce";
      } else if (weapon.isWeaponSlash()) {
         return "cut";
      } else if (weapon.isWeaponCrush()) {
         return "maul";
      } else {
         return weapon.isBodyPart() && weapon.getAuxData() != 100 ? attacker.getAttackStringForBodyPart(weapon) : "hit";
      }
   }

   public static String getAttackString(Creature attacker, Item weapon) {
      if (weapon.isWeaponPierce()) {
         return "pierce";
      } else if (weapon.isWeaponSlash()) {
         return "cut";
      } else if (weapon.isWeaponCrush()) {
         return "maul";
      } else {
         return weapon.isBodyPart() && weapon.getAuxData() != 100 ? attacker.getAttackStringForBodyPart(weapon) : "hit";
      }
   }

   @Deprecated
   public static boolean taunt(Creature performer, Creature defender, float counter, Action act) {
      boolean done = false;
      if (defender.isDead()) {
         logger.log(Level.INFO, defender.getName() + " is dead when taunted by " + performer.getName());
         return true;
      } else {
         Skill taunt = null;
         int time = 70;
         Skills skills = performer.getSkills();
         Skill defPsyche = null;
         Skills defSkills = defender.getSkills();

         try {
            defPsyche = defSkills.getSkill(105);
         } catch (NoSuchSkillException var20) {
            defPsyche = defSkills.learn(105, 1.0F);
         }

         Skill attPsyche = null;

         try {
            attPsyche = skills.getSkill(105);
         } catch (NoSuchSkillException var19) {
            attPsyche = skills.learn(105, 1.0F);
         }

         double power = 0.0;

         try {
            taunt = skills.getSkill(10057);
         } catch (NoSuchSkillException var18) {
            taunt = skills.learn(10057, 1.0F);
         }

         if (counter == 1.0F) {
            act.setTimeLeft(time);
            performer.sendActionControl(Actions.actionEntrys[103].getVerbString(), true, time);
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new CreatureLineSegment(performer));
            segments.add(new MulticolorLineSegment(" starts to annoy ", (byte)7));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" in all imaginable ways.", (byte)7));
            defender.getCommunicator().sendColoredMessageCombat(segments);

            for(MulticolorLineSegment s : segments) {
               s.setColor((byte)0);
            }

            MessageServer.broadcastColoredAction(segments, performer, defender, 5, true);
            segments.get(1).setText(" start to annoy ");
            performer.getCommunicator().sendColoredMessageCombat(segments);
         } else {
            time = act.getTimeLeft();
         }

         if (counter * 10.0F > (float)time) {
            boolean dryrun = defender.isNoSkillFor(performer);
            defender.addAttacker(performer);
            float mod = getMod(performer, defender, taunt);
            power = taunt.skillCheck(
               Math.max(1.0, Math.max(taunt.getRealKnowledge() - 10.0, defPsyche.getKnowledge(0.0) - attPsyche.getKnowledge(0.0))),
               0.0,
               mod == 0.0F || dryrun,
               (float)((long)Math.max(1.0F, 4.0F * mod)),
               performer,
               defender
            );
            if (!(power > 0.0)) {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(" ignores your antics.", (byte)0));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               segments.clear();
               segments.add(new CreatureLineSegment(performer));
               segments.add(new MulticolorLineSegment(" tires and ceases taunting you.", (byte)0));
               defender.getCommunicator().sendColoredMessageCombat(segments);
               segments.clear();
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(" ignores ", (byte)0));
               segments.add(new CreatureLineSegment(performer));
               segments.add(new MulticolorLineSegment("'s antics.", (byte)0));
               MessageServer.broadcastColoredAction(segments, performer, defender, 5, true);
            } else {
               ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
               segments.add(new CreatureLineSegment(defender));
               segments.add(new MulticolorLineSegment(" sees red and turns to attack ", (byte)0));
               segments.add(new CreatureLineSegment(performer));
               segments.add(new MulticolorLineSegment(" instead.", (byte)0));
               performer.getCommunicator().sendColoredMessageCombat(segments);
               MessageServer.broadcastColoredAction(segments, performer, defender, 5, true);

               for(MulticolorLineSegment s : segments) {
                  s.setColor((byte)7);
               }

               segments.get(1).setText(" see red and turn to attack ");
               defender.getCommunicator().sendColoredMessageCombat(segments);
               defender.removeTarget(defender.target);
               defender.setTarget(performer.getWurmId(), true);
               defender.setOpponent(performer);
               performer.achievement(563);
            }

            done = true;
         }

         return done;
      }
   }

   @Deprecated
   public static boolean shieldBash(Creature performer, Creature defender, float counter) {
      boolean done = false;
      Skill bash = null;
      if (defender.isDead()) {
         return true;
      } else {
         Item shield = performer.getShield();
         if (defender.equals(performer)) {
            return true;
         } else if (defender.isStunned()) {
            ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
            segments.add(new MulticolorLineSegment("You can not bash ", (byte)0));
            segments.add(new CreatureLineSegment(defender));
            segments.add(new MulticolorLineSegment(" because " + defender.getHeSheItString() + " is already stunned.", (byte)0));
            performer.getCommunicator().sendColoredMessageCombat(segments);
            return true;
         } else {
            if (!performer.getCombatHandler().mayShieldBash()) {
               performer.getCommunicator().sendCombatNormalMessage("You are still gaining strength from your last shield bash.");
               done = true;
            } else if (shield == null) {
               performer.getCommunicator().sendCombatNormalMessage("You need to wear the shield to bash someone with it.");
               done = true;
            } else {
               try {
                  boolean dryrun = defender.isNoSkillFor(performer)
                     || defender.isNoSkillgain()
                     || defender.isPlayer() && (!defender.isPaying() || defender.isNewbie());
                  int time = 50;
                  Action act = performer.getCurrentAction();
                  if (!defender.isFighting()) {
                     defender.setOpponent(performer);
                  }

                  if (counter == 1.0F) {
                     act.setTimeLeft(time);
                     performer.sendActionControl(Actions.actionEntrys[105].getVerbString(), true, time);
                     ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                     segments.add(new MulticolorLineSegment("You aim to push ", (byte)0));
                     segments.add(new CreatureLineSegment(defender));
                     segments.add(new MulticolorLineSegment(" over with your shield.", (byte)0));
                     performer.getCommunicator().sendColoredMessageCombat(segments);
                  } else {
                     time = act.getTimeLeft();
                  }

                  if (counter * 10.0F > (float)time) {
                     Skills skills = performer.getSkills();
                     Skill defBodyControl = null;
                     Skills defSkills = defender.getSkills();
                     Skill attStrength = null;

                     try {
                        defBodyControl = defSkills.getSkill(104);
                     } catch (NoSuchSkillException var36) {
                        defBodyControl = defSkills.learn(104, 1.0F);
                     }

                     try {
                        attStrength = skills.getSkill(102);
                     } catch (NoSuchSkillException var35) {
                        attStrength = skills.learn(102, 1.0F);
                     }

                     double power = 0.0;
                     double bonus = 0.0;
                     int skillnum = -10;
                     Skill shieldSkill = null;
                     if (shield != null) {
                        try {
                           skillnum = shield.getPrimarySkill();
                           shieldSkill = skills.getSkill(skillnum);
                        } catch (NoSuchSkillException var38) {
                           if (skillnum != -10) {
                              shieldSkill = skills.learn(skillnum, 1.0F);
                           }
                        }
                     }

                     float mod = 1.0F;
                     if (shieldSkill != null) {
                        mod = getMod(performer, defender, shieldSkill);
                        bonus = shieldSkill.skillCheck(defBodyControl.getKnowledge(0.0), 0.0, dryrun, (float)((long)Math.min(1.0F, mod)), defender, performer);
                     }

                     if (attStrength != null) {
                        bonus += attStrength.getKnowledge(0.0);
                     }

                     try {
                        bash = skills.getSkill(10058);
                     } catch (NoSuchSkillException var34) {
                        bash = skills.learn(10058, 1.0F);
                     }

                     mod = getMod(performer, defender, bash);
                     defender.addAttacker(performer);
                     Methods.sendSound(defender, "sound.combat.shield.bash");
                     float materialModifier = 1.0F;
                     if (shield.isMetal()) {
                        materialModifier = 2.0F;
                     }

                     int weightModifier = 40000;
                     double diff = (double)(defender.getWeight() / defender.getTemplate().getWeight());
                     if (defender.isPlayer()) {
                        diff = (double)(
                           (
                                 defender.getWeight()
                                    + (float)Math.max(0, defender.getBody().getBodyItem().getFullWeight() + defender.getInventory().getFullWeight() - 40000)
                              )
                              / defender.getTemplate().getWeight()
                        );
                     }

                     boolean dodge = true;
                     boolean topple = false;
                     if (diff > 1.0) {
                        dodge = false;
                        if (defender.getMovePenalty() > 0) {
                           topple = true;
                        }
                     }

                     diff = defBodyControl.getKnowledge(0.0) * diff / (double)Math.max(1, defender.getMovePenalty() / 3);
                     power = bash.skillCheck(
                        diff * (double)ItemBonus.getBashDodgeBonusFor(defender),
                        bonus / 10.0 + (double)((float)shield.getWeightGrams() * materialModifier) / 1000.0,
                        mod == 0.0F || dryrun,
                        (float)((long)mod * 2L),
                        performer,
                        defender
                     );
                     defender.getCombatHandler().increaseUseShieldCounter();
                     performer.getCombatHandler().shieldBash();
                     if (power > 0.0) {
                        try {
                           int pos = defender.getBody().getRandomWoundPos();
                           float armourMod = defender.getArmourMod();
                           double damage = Math.max(500.0, Server.rand.nextDouble() * bash.getKnowledge(0.0) * 50.0);
                           if (!performer.isPlayer() && !defender.isPlayer()) {
                              armourMod = 1.0F;
                           }

                           if (armourMod == 1.0F || defender.isVehicle()) {
                              try {
                                 byte bodyPosition = ArmourTemplate.getArmourPosition((byte)pos);
                                 Item armour = defender.getArmour(bodyPosition);
                                 armourMod = ArmourTemplate.calculateDR(armour, (byte)0);
                                 if (defender.isPlayer()) {
                                    armour.setDamage(
                                       armour.getDamage()
                                          + Math.min(
                                             1.0F,
                                             (float)(damage * (double)armourMod / 80000.0)
                                                * armour.getDamageModifier()
                                                * ArmourTemplate.getArmourDamageModFor(armour, (byte)0)
                                          )
                                    );
                                 }

                                 checkEnchantDestruction(shield, armour, defender);
                              } catch (NoArmourException var32) {
                              } catch (NoSpaceException var33) {
                                 logger.log(Level.WARNING, defender.getName() + " no armour space on loc " + pos);
                              }
                           }

                           if (defender.getBonusForSpellEffect((byte)22) > 0.0F) {
                              if (armourMod >= 1.0F) {
                                 armourMod = 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F;
                              } else {
                                 armourMod = Math.min(armourMod, 0.2F + (1.0F - defender.getBonusForSpellEffect((byte)22) / 100.0F) * 0.6F);
                              }
                           }

                           if (damage * (double)armourMod > 500.0) {
                              if (shieldSkill != null) {
                                 mod = getMod(performer, defender, shieldSkill);
                                 bonus = shieldSkill.skillCheck(
                                    defBodyControl.getKnowledge(0.0), 0.0, dryrun, (float)((long)(mod * 3.0F)), defender, performer
                                 );
                              }

                              mod = getMod(performer, defender, bash);
                              power = bash.skillCheck(
                                 defBodyControl.getKnowledge(0.0),
                                 bonus / 10.0 + (double)((float)shield.getWeightGrams() * materialModifier) / 1000.0,
                                 mod == 0.0F || dryrun,
                                 (float)((long)(mod * 4.0F)),
                                 performer,
                                 defender
                              );
                           }

                           addWound(
                              performer, defender, (byte)0, pos, damage, armourMod, "hurt", performer.getBattle(), 0.0F, 0.0F, false, false, false, false
                           );
                        } catch (Exception var37) {
                           logger.log(Level.WARNING, defender.getName() + ":" + var37.getMessage(), (Throwable)var37);
                        }

                        defender.maybeInterruptAction(200000);
                        String pushtopple = topple ? " topples " : " pushes ";
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                        segments.add(new CreatureLineSegment(performer));
                        segments.add(new MulticolorLineSegment(pushtopple, (byte)7));
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" over with " + performer.getHisHerItsString() + " shield.", (byte)7));
                        defender.getCommunicator().sendColoredMessageCombat(segments);

                        for(MulticolorLineSegment s : segments) {
                           s.setColor((byte)0);
                        }

                        MessageServer.broadcastColoredAction(segments, performer, defender, 5, true);
                        ArrayList<MulticolorLineSegment> segmentsPerformer = new ArrayList<>();
                        segmentsPerformer.add(new CreatureLineSegment(defender));
                        segmentsPerformer.add(new MulticolorLineSegment(" is sprawling on the ground.", (byte)0));
                        performer.getCommunicator().sendColoredMessageCombat(segmentsPerformer);
                        defender.playAnimation("sprawl", false);
                        defender.getStatus().setStunned((float)((byte)((int)Math.max(2.0, power / 100.0 * 10.0))));
                        performer.getStatus().modifyStamina(-2000.0F);
                     } else if (dodge) {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" swiftly dodges your bash.", (byte)0));
                        performer.getCommunicator().sendColoredMessageCombat(segments);
                        segments.clear();
                        segments.add(new MulticolorLineSegment("You swiftly dodge the shield bash from ", (byte)0));
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(".", (byte)0));
                        defender.getCommunicator().sendColoredMessageCombat(segments);
                        performer.getStatus().modifyStamina(-500.0F);
                     } else {
                        ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(" keeps " + defender.getHisHerItsString() + " balance.", (byte)0));
                        performer.getCommunicator().sendColoredMessageCombat(segments);
                        segments.clear();
                        segments.add(new MulticolorLineSegment("You keep your balance after the shield bash from ", (byte)0));
                        segments.add(new CreatureLineSegment(defender));
                        segments.add(new MulticolorLineSegment(".", (byte)0));
                        defender.getCommunicator().sendColoredMessageCombat(segments);
                        performer.getStatus().modifyStamina(-500.0F);
                     }

                     done = true;
                  }
               } catch (NoSuchActionException var39) {
                  done = true;
                  logger.log(Level.WARNING, "Performer: " + performer.getName() + ", Defender: " + defender.getName() + " this action doesn't exist?");
               }
            }

            return done;
         }
      }
   }

   @Deprecated
   public static boolean attack(Creature performer, Item target, float counter, Action act) {
      return attack(performer, target, counter, -1, act);
   }

   @Deprecated
   public static boolean attack(Creature performer, Item target, float counter, int pos, Action act) {
      boolean done = false;
      boolean dead = false;
      boolean aiming = false;
      Item primWeapon = performer.getPrimWeapon();
      if (primWeapon == null || primWeapon.isBodyPart()) {
         performer.getCommunicator().sendNormalServerMessage("You have no weapon to attack " + target.getNameWithGenus() + " with.");
         return true;
      } else if (primWeapon.isShield()) {
         performer.getCommunicator().sendNormalServerMessage("You cannot practice attacks with shields on " + target.getNameWithGenus() + ".");
         return true;
      } else if (!primWeapon.isWeaponBow() && !primWeapon.isBowUnstringed()) {
         BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
         if (result != null) {
            performer.getCommunicator()
               .sendCombatNormalMessage("You fail to reach the " + target.getNameWithGenus() + " because of the " + result.getFirstBlocker().getName() + ".");
            return true;
         } else if (Creature.rangeTo(performer, target) > Actions.actionEntrys[114].getRange()) {
            performer.getCommunicator()
               .sendNormalServerMessage(
                  "You are now too far away to " + Actions.actionEntrys[114].getActionString().toLowerCase() + " " + target.getNameWithGenus() + "."
               );
            return true;
         } else {
            int speed = 10;
            speed = primWeapon.getWeightGrams() / 1000 + 3;
            if (pos != -1) {
               aiming = true;
               ++speed;
            }

            if (!done) {
               if (act.justTickedSecond()) {
                  performer.decreaseFatigue();
               }

               if (counter == 1.0F) {
                  if (aiming) {
                     String bodypartname = PracticeDollBehaviour.getWoundLocationString(pos);
                     performer.getCommunicator()
                        .sendSafeServerMessage(
                           "You try to " + getAttackString(performer, primWeapon) + " " + target.getNameWithGenus() + " in the " + bodypartname + "."
                        );
                  } else {
                     performer.getCommunicator()
                        .sendSafeServerMessage("You try to " + getAttackString(performer, primWeapon) + " " + target.getNameWithGenus() + ".");
                  }
               }

               if (act.currentSecond() % speed == 0) {
                  Skill attackerFightSkill = null;
                  Skills performerSkills = performer.getSkills();
                  double attBonus = 0.0;
                  int attSknum = 1023;

                  try {
                     attackerFightSkill = performerSkills.getSkill(1023);
                  } catch (NoSuchSkillException var20) {
                     attackerFightSkill = performerSkills.learn(1023, 1.0F);
                  }

                  dead = performAttack(pos, false, performer, performerSkills, primWeapon, target, 0.0, attackerFightSkill, speed);
                  if (aiming) {
                     done = true;
                  }

                  if (dead) {
                     done = true;
                  }
               }

               if (!done && !aiming) {
                  Item[] secondaryWeapons = performer.getSecondaryWeapons();

                  for(int x = 0; x < secondaryWeapons.length; ++x) {
                     if (!secondaryWeapons[x].isBodyPart()) {
                        speed = Server.rand.nextInt(secondaryWeapons[x].getWeightGrams() / 1000 + 7) + 2;
                        if (act.currentSecond() % speed == 0) {
                           Skill attackerFightSkill = null;
                           Skills performerSkills = performer.getSkills();
                           double attBonus = 0.0;
                           int attSknum = 1023;

                           try {
                              attackerFightSkill = performerSkills.getSkill(1023);
                           } catch (NoSuchSkillException var19) {
                              attackerFightSkill = performerSkills.learn(1023, 1.0F);
                           }

                           done = performAttack(pos, false, performer, performerSkills, secondaryWeapons[x], target, 0.0, attackerFightSkill, speed);
                        }
                     }
                  }
               }
            }

            return done;
         }
      } else {
         performer.getCommunicator()
            .sendNormalServerMessage("You cannot practice attacks with bows on " + target.getNameWithGenus() + ". You need to use an archery target instead.");
         return true;
      }
   }

   @Deprecated
   protected static boolean performAttack(
      int pos, boolean aiming, Creature performer, Skills performerSkills, Item attWeapon, Item target, double attBonus, Skill attackerFightSkill, int counter
   ) {
      if (!performer.hasLink()) {
         return true;
      } else {
         boolean done = false;
         Skill primWeaponSkill = null;
         int skillnum = -10;
         performer.getStatus().modifyStamina(-1000.0F);
         if (attWeapon != null) {
            try {
               skillnum = attWeapon.getPrimarySkill();
               primWeaponSkill = performerSkills.getSkill(skillnum);
            } catch (NoSuchSkillException var28) {
               if (skillnum != -10) {
                  primWeaponSkill = performerSkills.learn(skillnum, 1.0F);
               }
            }
         }

         try {
            Skill attStrengthSkill = performerSkills.getSkill(102);
         } catch (NoSuchSkillException var27) {
            Skill attStrengthSkillx = performerSkills.learn(102, 1.0F);
            logger.log(Level.WARNING, performer.getName() + " had no strength. Weird.");
         }

         double bonus = 0.0;
         if (primWeaponSkill != null) {
            boolean dryrun = primWeaponSkill.getKnowledge(0.0) >= 20.0;
            bonus = Math.max(
               0.0, primWeaponSkill.skillCheck((double)(attWeapon.getCurrentQualityLevel() + 10.0F), attBonus, dryrun, (float)Math.max(1, counter / 2))
            );
         }

         if (aiming) {
            int rand = 10;
            if (pos == 1) {
               rand = Server.rand.nextInt(100);
               bonus = -60.0;
               if (rand < 50) {
                  pos = 17;
               }
            } else if (pos == 29) {
               rand = Server.rand.nextInt(100);
               bonus = -80.0;
               if (rand < 98) {
                  pos = 29;
               } else if (rand < 99) {
                  pos = 18;
               } else if (rand < 100) {
                  pos = 19;
               }
            } else if (pos == 2) {
               rand = Server.rand.nextInt(20);
               bonus = -40.0;
               if (rand < 5) {
                  pos = 21;
               } else if (rand < 7) {
                  pos = 27;
               } else if (rand < 9) {
                  pos = 26;
               } else if (rand < 12) {
                  pos = 32;
               } else if (rand < 14) {
                  pos = 23;
               } else if (rand < 18) {
                  pos = 24;
               } else if (rand < 20) {
                  pos = 25;
               }
            } else if (pos == 3) {
               rand = Server.rand.nextInt(10);
               bonus = -30.0;
               if (rand < 5) {
                  pos = 5;
               } else if (rand < 9) {
                  pos = 9;
               } else {
                  pos = 13;
               }
            } else if (pos == 4) {
               rand = Server.rand.nextInt(10);
               bonus = -30.0;
               if (rand < 5) {
                  pos = 6;
               } else if (rand < 9) {
                  pos = 10;
               } else {
                  pos = 14;
               }
            } else if (pos == 34) {
               rand = Server.rand.nextInt(20);
               bonus = -30.0;
               if (rand < 5) {
                  pos = 7;
               } else if (rand < 9) {
                  pos = 11;
               } else if (rand < 10) {
                  pos = 15;
               }

               if (rand < 15) {
                  pos = 8;
               } else if (rand < 19) {
                  pos = 12;
               } else {
                  pos = 16;
               }
            }
         } else {
            try {
               pos = PracticeDollBehaviour.getRandomWoundPos();
            } catch (Exception var26) {
               logger.log(Level.WARNING, "Could not get random wound position on " + target.getName() + " due to " + var26.getMessage(), (Throwable)var26);
            }
         }

         double attCheck = 0.0;
         if (primWeaponSkill != null) {
            boolean dryrun = attackerFightSkill.getKnowledge(0.0) >= 20.0;
            attCheck = attackerFightSkill.skillCheck(10.0, attWeapon, bonus, dryrun, (float)Math.max(1, counter / 2));
         } else {
            attCheck = attackerFightSkill.skillCheck(10.0, attWeapon, bonus, true, (float)Math.max(1, counter));
         }

         String attString = getAttackString(performer, attWeapon);
         if (attCheck > 0.0) {
            double damage = (double)Math.max(0.1F, Server.rand.nextFloat() * Weapon.getBaseDamageForWeapon(attWeapon) / 10.0F);
            if (primWeaponSkill != null) {
               attWeapon.setDamage(attWeapon.getDamage() + 0.05F * Weapon.getBaseDamageForWeapon(attWeapon));
            }

            String broadCastString = performer.getNameWithGenus()
               + " "
               + attString
               + "s "
               + target.getNameWithGenus()
               + " "
               + getStrengthString(damage)
               + " in the "
               + PracticeDollBehaviour.getWoundLocationString(pos)
               + ".";
            performer.getCommunicator()
               .sendSafeServerMessage(
                  "You "
                     + attString
                     + " "
                     + target.getNameWithGenus()
                     + " "
                     + getStrengthString(damage)
                     + " in the "
                     + PracticeDollBehaviour.getWoundLocationString(pos)
                     + "."
               );
            Server.getInstance().broadCastAction(broadCastString, performer, 3);
            done = target.setDamage(target.getDamage() + (float)damage * target.getDamageModifier());
            int tilex = (int)target.getPosX() >> 2;
            int tiley = (int)target.getPosY() >> 2;
            String sstring = "sound.combat.parry1";
            int x = Server.rand.nextInt(3);
            if (x == 0) {
               sstring = "sound.combat.parry2";
            } else if (x == 1) {
               sstring = "sound.combat.parry3";
            }

            SoundPlayer.playSound(sstring, target, 1.6F);
            performer.playAnimation("practice_cut", false, target.getWurmId());
            if (done) {
               broadCastString = target.getNameWithGenus() + " is no more.";
               Server.getInstance().broadCastMessage(broadCastString, tilex, tiley, performer.isOnSurface(), 3);
            }
         } else {
            String sstring = "sound.combat.miss.light";
            if (attCheck < -80.0) {
               sstring = "sound.combat.miss.heavy";
            } else if (attCheck < -40.0) {
               sstring = "sound.combat.miss.med";
            }

            SoundPlayer.playSound(sstring, target, 1.6F);
            if (performer.spamMode()) {
               if (aiming) {
                  performer.getCommunicator().sendNormalServerMessage("You miss.");
               } else {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You try to "
                           + attString
                           + " "
                           + target.getNameWithGenus()
                           + " in the "
                           + PracticeDollBehaviour.getWoundLocationString(pos)
                           + " but "
                           + getMissString(true, attCheck)
                     );
               }
            } else if (aiming) {
               performer.getCommunicator().sendNormalServerMessage("You miss.");
            }
         }

         return done;
      }
   }
}
