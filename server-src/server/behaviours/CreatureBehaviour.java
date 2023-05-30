package com.wurmonline.server.behaviours;

import com.wurmonline.math.TilePos;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
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
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.Archery;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.combat.Arrows;
import com.wurmonline.server.combat.Battle;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.combat.SpecialMove;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.ItemBonus;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.AppointmentsQuestion;
import com.wurmonline.server.questions.CreatureChangeAgeQuestion;
import com.wurmonline.server.questions.CultQuestion;
import com.wurmonline.server.questions.DuelQuestion;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.ManagePermissions;
import com.wurmonline.server.questions.MissionManager;
import com.wurmonline.server.questions.MissionQuestion;
import com.wurmonline.server.questions.PermissionsHistory;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.questions.RealDeathQuestion;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.TeamManagementQuestion;
import com.wurmonline.server.questions.TraderManagementQuestion;
import com.wurmonline.server.questions.WagonerHistory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.spells.Dominate;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BlockingResult;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.OldMission;
import com.wurmonline.server.utils.CoordUtils;
import com.wurmonline.server.utils.CreatureLineSegment;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.AttitudeConstants;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.util.MulticolorLineSegment;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public final class CreatureBehaviour
   extends Behaviour
   implements CreatureTypes,
   VillageStatus,
   MiscConstants,
   TimeConstants,
   AttitudeConstants,
   CreatureTemplateIds {
   private static final Logger logger = Logger.getLogger(CreatureBehaviour.class.getName());

   CreatureBehaviour() {
      super((short)4);
   }

   @Nonnull
   @Override
   public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Creature target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(Actions.getDefaultCreatureActions());
      addEmotes(toReturn);
      if (target instanceof Player) {
         if (performer instanceof Player) {
            if (target.getKingdomId() == performer.getKingdomId()) {
               toReturn.add(new ActionEntry((short)-1, "Friends", "", emptyIntArr));
               Friend[] friends = ((Player)performer).getFriends();
               boolean friend = false;
               long targid = target.getWurmId();

               for(Friend lFriend : friends) {
                  if (lFriend.getFriendId() == targid) {
                     friend = true;
                     break;
                  }
               }

               if (friend) {
                  toReturn.add(Actions.actionEntrys[61]);
               } else {
                  toReturn.add(Actions.actionEntrys[60]);
               }

               if (performer.mayInviteTeam()) {
                  if (target.getTeam() == null || performer.getTeam() != target.getTeam()) {
                     toReturn.add(Actions.actionEntrys[469]);
                  }

                  if (performer.getTeam() == target.getTeam() && (performer.isTeamLeader() || performer.mayInviteTeam() && !target.mayInviteTeam())) {
                     toReturn.add(Actions.actionEntrys[470]);
                  }

                  if (performer.isTeamLeader()) {
                     toReturn.add(Actions.actionEntrys[471]);
                  }
               }

               Village perfVillage = performer.getCitizenVillage();
               if (perfVillage != null) {
                  Village targVillage = target.getCitizenVillage();
                  if (!perfVillage.equals(targVillage)) {
                     boolean diplomat = false;
                     boolean mayinvite = false;
                     boolean peacemaker = false;
                     int size = 0;
                     if (perfVillage.mayDoDiplomacy(performer) && targVillage != null) {
                        boolean atPeace = perfVillage.isAtPeaceWith(targVillage);
                        if (atPeace && targVillage.mayDoDiplomacy(target) && !perfVillage.isAlly(targVillage)) {
                           diplomat = true;
                           --size;
                        }

                        if (targVillage.kingdom != performer.getKingdomId() || atPeace) {
                           if (targVillage.kingdom == performer.getKingdomId() && !atPeace) {
                              peacemaker = true;
                              --size;
                           }
                        } else if (perfVillage.mayDeclareWarOn(targVillage)) {
                           --size;
                        }
                     }

                     if (perfVillage.isActionAllowed((short)73, performer) && perfVillage.acceptsNewCitizens()) {
                        Village targvil = target.getCitizenVillage();
                        if (!perfVillage.equals(targvil)) {
                           mayinvite = true;
                           --size;
                        }
                     }

                     if (size > 0) {
                        toReturn.add(new ActionEntry((short)size, "Village", "Village options", emptyIntArr));
                     }

                     if (mayinvite) {
                        toReturn.add(Actions.actionEntrys[73]);
                     }

                     if (diplomat) {
                        toReturn.add(Actions.actionEntrys[81]);
                     }

                     if (peacemaker) {
                        toReturn.add(Actions.actionEntrys[210]);
                     }
                  }
               }
            } else if (performer.getPower() >= 2 || target.acceptsInvitations() && target.getKingdomId() != performer.getKingdomId()) {
               toReturn.add(Actions.actionEntrys[89]);
            }
         }
      } else {
         if (performer.getPower() >= 2) {
            toReturn.add(Actions.actionEntrys[89]);
         }

         if (target.isNpcTrader() && this.mayDismissMerchant(performer, target)) {
            toReturn.add(Actions.actionEntrys[62]);
         }

         if (target.isWagoner()
            && target.getWagoner() != null
            && target.getWagoner().getVillageId() != -1
            && (target.getWagoner().getOwnerId() == performer.getWurmId() || performer.getPower() > 1)) {
            List<ActionEntry> waglist = new LinkedList<>();
            waglist.add(new ActionEntry((short)-2, "Permissions", "viewing"));
            waglist.add(Actions.actionEntrys[863]);
            waglist.add(new ActionEntry((short)691, "History Of Wagoner", "viewing"));
            waglist.add(Actions.actionEntrys[919]);
            waglist.add(new ActionEntry((short)566, "Manage chat options", "managing"));
            toReturn.add(new ActionEntry((short)(-(waglist.size() - 2)), target.getWagoner().getName(), "wagoner"));
            toReturn.addAll(waglist);
         }

         if (target.isHorse() || target.isUnicorn()) {
            for(Item i : target.getBody().getAllItems()) {
               if (i.isSaddleBags()) {
                  toReturn.add(new ActionEntry((short)3, "Open " + i.getName(), "opening"));
               }
            }
         }
      }

      if (target.getTemplate().isRoyalAspiration() && !Servers.localServer.isChallengeServer()) {
         toReturn.add(Actions.actionEntrys[353]);
      }

      if (performer.getPower() >= 2 || performer.getCultist() != null && performer.getCultist().mayCreatureInfo()) {
         toReturn.add(Actions.actionEntrys[185]);
      }

      if (performer.isRoyalAnnouncer() && !performer.isFighting() && !target.isFighting()) {
         toReturn.add(Actions.actionEntrys[357]);
      }

      if (target.isGuide()) {
         toReturn.add(Actions.actionEntrys[351]);
         if (target.getKingdomId() != performer.getKingdomId()) {
            toReturn.add(Actions.actionEntrys[213]);
         }
      }

      if (target.isTrader()
         && !target.isFighting()
         && target.getFloorLevel() == performer.getFloorLevel()
         && (!target.isNpcTrader() || performer.getVehicle() == -10L || performer.isVehicleCommander())) {
         if (performer.isFriendlyKingdom(target.getKingdomId()) || performer.getPower() > 0) {
            toReturn.add(Actions.actionEntrys[63]);
         }

         if (Servers.localServer.PVPSERVER
            && Servers.localServer.isChallengeOrEpicServer()
            && !Servers.localServer.HOMESERVER
            && target.isNpcTrader()
            && !target.isFriendlyKingdom(performer.getKingdomId())) {
            Shop shop = Economy.getEconomy().getShop(target);
            if (shop != null && shop.isPersonal()) {
               toReturn.add(Actions.actionEntrys[63]);
               toReturn.add(Actions.actionEntrys[537]);
            }
         }
      }

      if (!target.isInvulnerable() && performer.getAttitude(target) == 2) {
         if (performer.getCultist() != null && performer.getCultist().mayDealFinalBreath() && performer.isWithinDistanceTo(target, 8.0F)) {
            toReturn.add(Actions.actionEntrys[490]);
         }
      } else if (!target.isInvulnerable()
         && !target.isPlayer()
         && !target.isHuman()
         && !target.isGhost()
         && !target.isReborn()
         && (!target.isCaredFor() || target.getCareTakerId() == performer.getWurmId())) {
         BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
         if (result == null) {
            toReturn.add(Actions.actionEntrys[493]);
         }
      }

      if (target.isBartender() && !target.isFighting()) {
         if (performer.isFriendlyKingdom(target.getKingdomId()) || performer.getPower() > 0) {
            toReturn.add(Actions.actionEntrys[91]);
         }
      } else if (target.isPlayer()
         && (performer.getPower() > 0 || performer.getCultist() != null && performer.getCultist().mayRefresh())
         && (performer.isPaying() && performer.isFriendlyKingdom(target.getKingdomId()) || performer.getPower() > 0)) {
         toReturn.add(new ActionEntry((short)91, "Refresh", "refreshing", emptyIntArr));
      }

      if (!target.isInvulnerable() || performer.getPower() >= 5) {
         toReturn.add(new ActionEntry((short)-1, "Attacks", "attacks"));
         toReturn.add(Actions.actionEntrys[326]);
         if (!performer.isFighting()) {
            if (target.isPlayer() && target.getKingdomId() == performer.getKingdomId()) {
               toReturn.add(new ActionEntry((short)-2, "Sparring", "Sparring"));
               toReturn.add(Actions.actionEntrys[344]);
               toReturn.add(Actions.actionEntrys[343]);
            }
         } else if (target.isFighting()
            && target.opponent != performer
            && (!target.isPlayer() || !performer.isPlayer() || target.isOnPvPServer() && performer.isOnPvPServer())) {
            toReturn.add(Actions.actionEntrys[103]);
         }
      }

      if (target.getTemplate().getTemplateId() == 46 || target.getTemplate().getTemplateId() == 47) {
         toReturn.add(Actions.actionEntrys[214]);
      }

      if (target.getLeader() == performer) {
         toReturn.add(Actions.actionEntrys[107]);
      } else if (performer.getFollowers().length == 1 && performer.getFollowers()[0].mayMate(target)) {
         toReturn.add(Actions.actionEntrys[379]);
      }

      if (!target.isWagoner()) {
         this.addVehicleOptions(performer, target, toReturn);
      }

      if (performer.getPet() != null) {
         boolean attack = false;
         boolean give = false;
         short nums = -1;
         if (target.getAttitude(performer) != 2 && performer.getPower() <= 0) {
            if (target instanceof Player && target.getPet() == null) {
               give = true;
               --nums;
            }
         } else if (!target.isInvulnerable()) {
            attack = true;
            --nums;
         }

         if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
            --nums;
         }

         toReturn.add(new ActionEntry(nums, "Pet", "Pet"));
         toReturn.add(Actions.actionEntrys[41]);
         if (attack) {
            toReturn.add(Actions.actionEntrys[42]);
         } else if (give) {
            toReturn.add(Actions.actionEntrys[43]);
         }

         if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
            if (performer.getPet().isStayonline()) {
               toReturn.add(Actions.actionEntrys[45]);
            } else {
               toReturn.add(Actions.actionEntrys[44]);
            }
         }
      }

      if (performer.getVehicle() != -10L
         && performer.isVehicleCommander()
         && target.getHitched() != null
         && target.getHitched().getWurmid() == performer.getVehicle()) {
         toReturn.add(Actions.actionEntrys[378]);
      }

      return toReturn;
   }

   public void addVehicleOptions(Creature performer, Creature target, List<ActionEntry> toReturn) {
      if (Constants.enabledMounts) {
         if (performer.getVehicle() != -10L) {
            Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
            if (vehicle.isChair()) {
               toReturn.add(Actions.actionEntrys[708]);
            } else {
               toReturn.add(Actions.actionEntrys[333]);
            }
         } else if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPositionZ(), 8.0F)) {
            BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
            if (result == null && !performer.isClimbing()) {
               boolean addedPassenger = false;
               boolean addedDriver = false;
               Vehicle vehicle = Vehicles.getVehicle(target);
               if (vehicle != null) {
                  for(Seat lSeat : vehicle.seats) {
                     if (!addedDriver && !lSeat.isOccupied() && lSeat.type == 0) {
                        if (!addedDriver
                           && !Servers.isThisAPvpServer()
                           && target.isBranded()
                           && target.getLeader() == performer
                           && target.mayCommand(performer)) {
                           toReturn.add(Actions.actionEntrys[11]);
                           addedDriver = true;
                        }

                        if (!addedDriver && (target.dominator == performer.getWurmId() || target.getLeader() == performer)) {
                           toReturn.add(Actions.actionEntrys[11]);
                           addedDriver = true;
                        }
                     } else if (!addedPassenger && !lSeat.isOccupied() && lSeat.type == 1) {
                        if (!Servers.isThisAPvpServer() && target.isBranded()) {
                           if (target.mayPassenger(performer)) {
                              toReturn.add(Actions.actionEntrys[332]);
                              addedPassenger = true;
                           }
                        } else if (target.getDominator() != null && performer.getKingdomId() == target.getDominator().getKingdomId()) {
                           toReturn.add(Actions.actionEntrys[332]);
                           addedPassenger = true;
                        }

                        if (addedPassenger && addedDriver) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         if (!target.isWagoner()) {
            List<ActionEntry> permissions = new LinkedList<>();
            if (target.mayManage(performer)) {
               permissions.add(Actions.actionEntrys[663]);
            }

            if (target.maySeeHistory(performer)) {
               permissions.add(new ActionEntry((short)691, "History of Animal", "viewing"));
            }

            if (!permissions.isEmpty()) {
               if (permissions.size() > 1) {
                  Collections.sort(permissions);
                  toReturn.add(new ActionEntry((short)(-permissions.size()), "Permissions", "viewing"));
               }

               toReturn.addAll(permissions);
            }
         }
      }
   }

   @Nonnull
   @Override
   public List<ActionEntry> getBehavioursFor(@Nonnull Creature performer, @Nonnull Item subject, @Nonnull Creature target) {
      List<ActionEntry> toReturn = new LinkedList<>();
      toReturn.addAll(this.getBehavioursFor(performer, target));
      int stid = subject.getTemplateId();
      if (stid == 330 || stid == 331 || stid == 334) {
         if (subject.getOwnerId() == performer.getWurmId()) {
            toReturn.add(Actions.actionEntrys[118]);
         }
      } else if (stid != 489 && (stid != 176 && stid != 315 || performer.getPower() < 2)) {
         if (subject.isWeaponBow() && !target.isInvulnerable()) {
            int numacts = -1;

            try {
               int skillnum = subject.getPrimarySkill();
               if ((long)skillnum != -10L) {
                  Skill skill = performer.getSkills().getSkill(skillnum);
                  double knowl = skill.getRealKnowledge();
                  if (knowl > 30.0) {
                     --numacts;
                  }

                  if (knowl > 35.0) {
                     --numacts;
                  }

                  if (knowl > 40.0) {
                     --numacts;
                     --numacts;
                  }

                  if (knowl > 50.0) {
                     --numacts;
                  }

                  if (knowl > 60.0) {
                     --numacts;
                  }

                  if (knowl > 70.0) {
                     --numacts;
                  }
               }
            } catch (NoSuchSkillException var14) {
            }

            toReturn.add(new ActionEntry((short)numacts, "Bow", "shooting", emptyIntArr));
            toReturn.add(Actions.actionEntrys[124]);
            if (numacts < -1) {
               toReturn.add(Actions.actionEntrys[125]);
            }

            if (numacts < -2) {
               toReturn.add(Actions.actionEntrys[128]);
            }

            if (numacts < -3) {
               toReturn.add(Actions.actionEntrys[129]);
               toReturn.add(Actions.actionEntrys[130]);
            }

            if (numacts < -5) {
               toReturn.add(Actions.actionEntrys[126]);
            }

            if (numacts < -6) {
               toReturn.add(Actions.actionEntrys[127]);
            }

            if (numacts < -7) {
               toReturn.add(Actions.actionEntrys[131]);
            }
         } else if (stid == 676) {
            if (subject.getOwnerId() == performer.getWurmId()) {
               toReturn.add(Actions.actionEntrys[472]);
            }
         } else if (performer.isFighting() && !target.isInvulnerable()) {
            toReturn.add(new ActionEntry((short)-1, "Throw", "throwing", new int[0]));
            toReturn.add(new ActionEntry((short)342, 8, "At target", "throwing", new int[]{22, 3, 23}, 200, false));
         } else if (subject.getTemplateId() == 1276
            || subject.getTemplateId() == 833
            || subject.getTemplateId() == 1258 && subject.getRealTemplateId() == 1195
            || subject.getTemplateId() == 1177 && subject.getRealTemplateId() == 1195) {
            toReturn.add(Actions.actionEntrys[745]);
         }
      } else if (subject.getOwnerId() == performer.getWurmId()) {
         toReturn.add(Actions.actionEntrys[329]);
      }

      if (subject.getTemplate().isRune()) {
         Skill soulDepth = performer.getSoulDepth();
         double diff = (double)(20.0F + subject.getDamage()) - ((double)(subject.getCurrentQualityLevel() + (float)subject.getRarity()) - 45.0);
         double chance = soulDepth.getChance(diff, null, (double)subject.getCurrentQualityLevel());
         if (RuneUtilities.isSingleUseRune(subject)
            && (
               RuneUtilities.getSpellForRune(subject) != null && RuneUtilities.getSpellForRune(subject).isTargetCreature()
                  || RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(subject), RuneUtilities.ModifierEffect.SINGLE_REFRESH) > 0.0F
                  || RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(subject), RuneUtilities.ModifierEffect.SINGLE_CHANGE_AGE) > 0.0F
            )) {
            toReturn.add(new ActionEntry((short)945, "Use Rune: " + chance + "%", "using rune", emptyIntArr));
         }
      }

      if (performer.getPower() >= 2) {
         toReturn.add(Actions.actionEntrys[179]);
         toReturn.add(Actions.actionEntrys[47]);
      }

      if (subject.getTemplateId() == 76 && subject.isFlyTrap()) {
         toReturn.add(Actions.actionEntrys[938]);
      }

      if (target instanceof Player) {
         if (subject.isRoyal() && (stid == 535 || stid == 529 || stid == 532)) {
            toReturn.add(Actions.actionEntrys[354]);
         }

         if (performer.getDeity() != null && subject.isHolyItem() && subject.isHolyItem(performer.getDeity())) {
            if (target.getDeity() != performer.getDeity()) {
               try {
                  target.getCurrentAction();
               } catch (NoSuchActionException var13) {
                  toReturn.add(new ActionEntry((short)-1, "Religion", "religion", emptyIntArr));
                  toReturn.add(Actions.actionEntrys[213]);
               }
            } else if (!target.isPriest()) {
               if (performer.getFaith() > 40.0F && target.getFaith() == 30.0F && (!Servers.localServer.PVPSERVER || Servers.localServer.testServer)) {
                  toReturn.add(Actions.actionEntrys[286]);
               }

               toReturn.add(Actions.actionEntrys[115]);
            }

            if (target.getDeity() != null && performer.getDeity().getTemplateDeity() == target.getDeity().getTemplateDeity()) {
               toReturn.add(Actions.actionEntrys[399]);
            }
         }

         if (subject.isMeditation()) {
            Cultist perfCultist = Cultist.getCultist(performer.getWurmId());
            if (perfCultist != null && perfCultist.getLevel() > 4) {
               Cultist respCultist = Cultist.getCultist(target.getWurmId());
               if (respCultist != null
                  && respCultist.getPath() == perfCultist.getPath()
                  && respCultist != null
                  && respCultist.getLevel() > 0
                  && perfCultist.getLevel() - respCultist.getLevel() == 3) {
                  toReturn.add(Actions.actionEntrys[387]);
               }
            }
         }

         if (subject.isPuppet()) {
            toReturn.add(Actions.actionEntrys[397]);
         }

         if (performer instanceof Player && stid == 903) {
            toReturn.add(Actions.actionEntrys[637]);
            toReturn.add(Actions.actionEntrys[640]);
         }
      } else {
         if (performer.getPower() >= 2) {
            toReturn.add(Actions.actionEntrys[89]);
            if (performer.getPower() >= 4 && target.isLeadable(performer) && stid == 176) {
               Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
               if (brand != null) {
                  toReturn.add(Actions.actionEntrys[643]);
               }
            }

            if (target.isFish() && stid == 176) {
               toReturn.add(Actions.actionEntrys[88]);
            }
         }

         if (Features.Feature.WAGONER.isEnabled()
            && target.isWagoner()
            && performer.getPower() >= 4
            && (stid == 176 || stid == 1129 && target.getWurmId() == subject.getData())) {
            Wagoner wagoner = Wagoner.getWagoner(target.getWurmId());
            if (wagoner != null) {
               List<ActionEntry> testlist = new LinkedList<>();
               if (wagoner.getState() == 0) {
                  testlist.add(new ActionEntry((short)140, "Send to bed", "testing"));
                  testlist.add(new ActionEntry((short)111, "Test delivery", "testing"));
               }

               if (wagoner.getState() == 2) {
                  testlist.add(new ActionEntry((short)30, "Wake up", "testing"));
               }

               if (!testlist.isEmpty()) {
                  toReturn.add(new ActionEntry((short)(-testlist.size()), "Test only", "testing"));
                  toReturn.addAll(testlist);
               }
            }
         }

         if (target.isMilkable()
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 1)
            && !target.isMilked()
            && subject.isContainerLiquid()
            && !subject.isSealedByPlayer()) {
            toReturn.add(Actions.actionEntrys[345]);
         }

         if (target.isWoolProducer()
            && subject.getTemplateId() == 394
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 1)
            && !target.isSheared()) {
            toReturn.add(Actions.actionEntrys[646]);
         }

         if (target.isNeedFood() && !subject.isBodyPartAttached()) {
            toReturn.add(Actions.actionEntrys[230]);
         }

         if (target.isDominatable(performer) && target.isAnimal()) {
            toReturn.add(Actions.actionEntrys[46]);
         }

         if (target.isDomestic()
            && stid == 647
            && subject.getOwnerId() == performer.getWurmId()
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 1)) {
            toReturn.add(Actions.actionEntrys[398]);
         }

         if (stid == 792
            && !target.isUnique()
            && !target.isUndead()
            && !target.isReborn()
            && target.getHitched() == null
            && !target.isRidden()
            && !target.isNpc()
            && (Servers.isThisAPvpServer() || !target.isDominated() && !target.isBranded())) {
            toReturn.add(Actions.actionEntrys[142]);
         }

         if (target.isLeadable(performer)
            && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), (int)(target.getStatus().getPositionZ() + target.getAltOffZ()) >> 2, 1)) {
            boolean canItemLead = !performer.isItemLeading(subject) || subject.isLeadMultipleCreatures();
            if (target.getLeader() == null && (subject.isLeadCreature() && canItemLead || target.hasBridle())) {
               boolean lastLed = target.isBranded() ? false : Creatures.getInstance().wasLastLed(performer.getWurmId(), target.getWurmId());
               if ((performer.getVehicle() <= -10L || performer.isVehicleCommander()) && (target.mayLead(performer) || lastLed)) {
                  toReturn.add(Actions.actionEntrys[106]);
               }
            }

            if (performer.getCitizenVillage() != null
               && target.getCurrentVillage() != null
               && target.getCurrentVillage() == performer.getCitizenVillage()
               && stid == 701) {
               Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
               if (brand == null) {
                  toReturn.add(Actions.actionEntrys[484]);
               } else {
                  try {
                     Village villageBrand = Villages.getVillage((int)brand.getBrandId());
                     if (performer.getCitizenVillage() == villageBrand) {
                        toReturn.add(Actions.actionEntrys[643]);
                     }
                  } catch (NoSuchVillageException var12) {
                     logger.log(Level.INFO, "Deleting brand for " + target.getName() + " since the settlement is gone.");
                     brand.deleteBrand();
                     toReturn.add(Actions.actionEntrys[484]);
                  }
               }
            }
         }

         MissionTrigger[] m = MissionTriggers.getMissionTriggersWith(stid, 47, target.getWurmId());
         if (m.length > 0) {
            toReturn.add(Actions.actionEntrys[47]);
         }

         MissionTrigger[] m2 = MissionTriggers.getMissionTriggersWith(stid, 473, target.getWurmId());
         if (m2.length > 0) {
            toReturn.add(Actions.actionEntrys[473]);
         }

         MissionTrigger[] m3 = MissionTriggers.getMissionTriggersWith(stid, 474, target.getWurmId());
         if (m3.length > 0) {
            toReturn.add(Actions.actionEntrys[474]);
         }
      }

      if (subject.isHolyItem()) {
         if (subject.isHolyItem(performer.getDeity()) && (performer.isPriest() || performer.getPower() > 0)) {
            float faith = performer.getFaith();
            Spell[] spells = performer.getDeity().getSpellsTargettingCreatures((int)faith);
            if (spells.length > 0) {
               toReturn.add(new ActionEntry((short)(-spells.length), "Spells", "spells"));

               for(Spell lSpell : spells) {
                  toReturn.add(Actions.actionEntrys[lSpell.number]);
               }
            }
         }
      } else if (subject.isMagicStaff() || subject.getTemplateId() == 176 && performer.getPower() >= 4 && Servers.isThisATestServer()) {
         List<ActionEntry> slist = new LinkedList<>();
         if (performer.knowsKarmaSpell(549)) {
            slist.add(Actions.actionEntrys[549]);
         }

         if (performer.knowsKarmaSpell(547)) {
            slist.add(Actions.actionEntrys[547]);
         }

         if (performer.knowsKarmaSpell(550)) {
            slist.add(Actions.actionEntrys[550]);
         }

         if (performer.knowsKarmaSpell(551)) {
            slist.add(Actions.actionEntrys[551]);
         }

         if (performer.knowsKarmaSpell(554)) {
            slist.add(Actions.actionEntrys[554]);
         }

         if (performer.knowsKarmaSpell(555)) {
            slist.add(Actions.actionEntrys[555]);
         }

         if (performer.knowsKarmaSpell(560)) {
            slist.add(Actions.actionEntrys[560]);
         }

         if (performer.knowsKarmaSpell(686)) {
            slist.add(Actions.actionEntrys[686]);
         }

         if (performer.getPower() >= 4) {
            toReturn.add(new ActionEntry((short)(-slist.size()), "Sorcery", "casting"));
         }

         toReturn.addAll(slist);
      }

      if (stid == 300 && performer.getPower() > 0 && subject.getData() < 0L) {
         toReturn.add(Actions.actionEntrys[85]);
      }

      if (target.isTrader()
         && !target.isPlayer()
         && target.getFloorLevel() == performer.getFloorLevel()
         && (!target.isNpcTrader() || performer.getVehicle() == -10L)
         && (performer.isFriendlyKingdom(target.getKingdomId()) || performer.getPower() > 0)
         && (target.getCurrentVillage() == null || !target.getCurrentVillage().isEnemy(performer))
         && !subject.isNoDiscard()
         && !subject.isInstaDiscard()
         && !subject.isTemporary()) {
         toReturn.add(Actions.actionEntrys[31]);
      }

      if (stid == 1027 && performer.getPower() >= 1 && target instanceof Player) {
         toReturn.add(new ActionEntry((short)-3, "LCM", "checking", emptyIntArr));
         toReturn.add(Actions.actionEntrys[698]);
         toReturn.add(Actions.actionEntrys[699]);
         toReturn.add(Actions.actionEntrys[700]);
      }

      if ((stid == 176 || stid == 315) && performer.getPower() >= 1) {
         try {
            if (target.getBody().getWounds() != null && target.getBody().getWounds().getWounds().length > 0) {
               toReturn.add(Actions.actionEntrys[346]);
            }

            if (performer.getPower() > 1) {
               if (target.getPower() <= performer.getPower()) {
                  int sub = -5;
                  if (stid == 176 && performer.getPower() >= 4 && !(target instanceof Player)) {
                     --sub;
                  }

                  if (stid == 176 && performer.getPower() >= 4 && target instanceof Player) {
                     --sub;
                  }

                  toReturn.add(new ActionEntry((short)sub, "Powers", "godlypower", emptyIntArr));
                  toReturn.add(Actions.actionEntrys[33]);
                  toReturn.add(new ActionEntry((short)392, "Test Effect", "testing effect", emptyIntArr));
                  toReturn.add(Actions.actionEntrys[327]);
                  toReturn.add(Actions.actionEntrys[352]);
                  toReturn.add(Actions.actionEntrys[244]);
                  toReturn.add(Actions.actionEntrys[928]);
                  if (stid == 176 && performer.getPower() >= 4 && target instanceof Player) {
                     toReturn.add(Actions.actionEntrys[721]);
                  }

                  if (stid == 176 && performer.getPower() >= 4 && !(target instanceof Player)) {
                     toReturn.add(Actions.actionEntrys[538]);
                  }
               }

               if (performer.getPower() >= 2 && target.isPlayer()) {
                  int nums = 0;
                  if (Servers.localServer.serverNorth != null) {
                     --nums;
                  }

                  if (Servers.localServer.serverEast != null) {
                     --nums;
                  }

                  if (Servers.localServer.serverSouth != null) {
                     --nums;
                  }

                  if (Servers.localServer.serverWest != null) {
                     --nums;
                  }

                  if (nums < 0) {
                     toReturn.add(new ActionEntry((short)nums, "Specials", "specials"));
                     if (Servers.localServer.serverNorth != null) {
                        toReturn.add(Actions.actionEntrys[240]);
                     }

                     if (Servers.localServer.serverEast != null) {
                        toReturn.add(Actions.actionEntrys[241]);
                     }

                     if (Servers.localServer.serverSouth != null) {
                        toReturn.add(Actions.actionEntrys[242]);
                     }

                     if (Servers.localServer.serverWest != null) {
                        toReturn.add(Actions.actionEntrys[243]);
                     }
                  }

                  if (performer.getPower() >= 4 || Servers.localServer.testServer && performer.getPower() >= 3) {
                     toReturn.add(new ActionEntry((short)-1, "Skills", "Skills stuff", emptyIntArr));
                     toReturn.add(Actions.actionEntrys[92]);
                     if (Servers.localServer.testServer) {
                        toReturn.add(new ActionEntry((short)486, "Toggle champ", "testing"));
                     }
                  }
               }

               if (!target.isPlayer() && performer.getPower() >= 4) {
                  toReturn.add(new ActionEntry((short)-2, "Npcs", "Npc stuff", emptyIntArr));
                  toReturn.add(Actions.actionEntrys[92]);
                  toReturn.add(Actions.actionEntrys[73]);
               }
            }
         } catch (Exception var15) {
            logger.log(Level.WARNING, target.getName() + ": " + var15.getMessage(), (Throwable)var15);
         }

         toReturn.add(Actions.actionEntrys[180]);
      }

      return toReturn;
   }

   @Override
   public boolean action(@Nonnull Action act, @Nonnull Creature performer, Creature target, short action, float counter) {
      boolean done = true;
      if (target == null) {
         logger.severe("Target is null for player " + performer);
         return true;
      } else {
         switch(action) {
            case 1:
               handle_EXAMINE(performer, target);
               break;
            case 2:
               target.sendEquipment(performer);
               break;
            case 3:
               if (target.isHorse() || target.isUnicorn()) {
                  for(Item i : target.getBody().getAllItems()) {
                     if (i.isSaddleBags()) {
                        performer.getCommunicator().sendOpenInventoryWindow(i.getWurmId(), target.getNamePossessive() + " " + i.getName());
                        performer.addItemWatched(i);
                        i.addWatcher(i.getWurmId(), performer);
                        i.sendContainedItems(i.getWurmId(), performer);
                     }
                  }
               }
            default:
               if (act.isStanceChange()) {
                  done = handleStanceChange(act, performer, target, action, counter);
               } else if (act.isDefend()) {
                  done = handleDefend(act, performer, action, counter);
               } else if (act.isSpell()) {
                  done = this.handleSpell(act, performer, target, action, counter);
               } else {
                  done = super.action(act, performer, target, action, counter);
               }
               break;
            case 30:
               if (Features.Feature.WAGONER.isEnabled() && target.isWagoner()) {
                  Wagoner wagoner = target.getWagoner();
                  if (wagoner != null && wagoner.getState() == 2) {
                     wagoner.forceStateChange((byte)3);
                  }
               }
               break;
            case 33:
               handle_WIZKILL(performer, target);
               break;
            case 34:
               done = MethodsCreatures.findCaveExit(target, performer);
               break;
            case 41:
               Creature pet = performer.getPet();
               if (pet != null) {
                  if (!pet.isWithinDistanceTo(performer, 200.0F)) {
                     performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " is too far away.");
                  } else {
                     pet.setTarget(-10L, true);
                     pet.clearOrders();
                     performer.getCommunicator()
                        .sendNormalServerMessage("You order the " + pet.getName() + " to forget all you told " + pet.getHimHerItString() + ".");
                  }
               }
               break;
            case 42:
               Creature pet = performer.getPet();
               if (pet == null) {
                  break;
               }

               if (!pet.isWithinDistanceTo(performer.getPosX(), performer.getPosY(), performer.getPositionZ(), 200.0F, 0.0F)) {
                  performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " is too far away.");
               } else if (!pet.mayReceiveOrder()) {
                  performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " ignores your order.");
               } else if (target.getWurmId() == pet.getWurmId()) {
                  performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " seems to ignore your order.");
               } else if (target.getAttitude(performer) != 2 && performer.getPower() == 0) {
                  performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " ignores your order.");
               } else {
                  Village v = target.getCurrentVillage();
                  if (v != null && v.isEnemy(performer)) {
                     performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " hesitates and does not enter " + v.getName() + ".");
                     return true;
                  }

                  if (target.getTileX() < 10
                     || target.getTileY() < 10
                     || target.getTileX() > Zones.worldTileSizeX - 10
                     || target.getTileY() > Zones.worldTileSizeY - 10) {
                     performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " hesitates and does not go there.");
                     return true;
                  }

                  if (target.isInvulnerable()) {
                     performer.getCommunicator().sendNormalServerMessage("The " + pet.getName() + " ignores your order.");
                  } else {
                     if (target.isUnique()) {
                        if (pet.isUnique()) {
                           performer.getCommunicator()
                              .sendAlertServerMessage("The " + pet.getName() + " becomes outraged instead of attacking " + target.getName() + ".");
                           pet.setDominator(-10L);
                           return true;
                        }

                        if (Server.rand.nextInt((int)performer.getSoulStrength().getKnowledge(0.0) / 2) == 0) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("Your " + pet.getName() + " seems hesitant about attacking " + target.getName() + ".");
                           pet.setLoyalty(pet.getLoyalty() - 20.0F);
                        }
                     }

                     pet.setTarget(target.getWurmId(), true);
                     pet.attackTarget();
                     performer.getCommunicator().sendNormalServerMessage("You issue an order to the " + pet.getName() + ".");
                  }
               }
               break;
            case 43:
               done = true;
               MethodsCreatures.petGiveAway(performer, target);
               break;
            case 44:
               done = true;
               if (performer.getPet() != null) {
                  if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                     performer.getPet().setStayOnline(true);
                     performer.getCommunicator()
                        .sendNormalServerMessage("The " + performer.getPet().getName() + " will now stay in the world when you log off.");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("The " + performer.getPet().getName() + " may not go offline.");
                  }
               }
               break;
            case 45:
               done = true;
               if (performer.getPet() != null) {
                  if (performer.getPet().isAnimal() && !performer.getPet().isReborn()) {
                     performer.getPet().setStayOnline(false);
                     performer.getCommunicator().sendNormalServerMessage("The " + performer.getPet().getName() + " will now leave the world when you do.");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("The " + performer.getPet().getName() + " may not go offline.");
                  }
               }
               break;
            case 60:
               if (performer instanceof Player) {
                  if (performer.getKingdomId() == target.getKingdomId()) {
                     if (((Player)performer).isFriend(target.getWurmId())) {
                        performer.getCommunicator().sendNormalServerMessage(target.getName() + " already is your friend.");
                     } else {
                        Methods.sendFriendQuestion(performer, target);
                     }
                  } else {
                     logger.log(Level.WARNING, performer.getName() + " tried to invite ENEMY " + target.getName() + " as friend!");
                  }
               }
               break;
            case 61:
               if (performer instanceof Player) {
                  ((Player)performer).removeFriend(target.getWurmId());
               }
               break;
            case 62:
               if (target.isNpcTrader() && this.mayDismissMerchant(performer, target)) {
                  TraderManagementQuestion tmq = new TraderManagementQuestion(performer, "Dismiss trader", "Do you want to dismiss this merchant?", target);
                  tmq.sendQuestion();
               }
               break;
            case 63:
               if (!target.isTrader()) {
                  return true;
               }

               if (target.isNpcTrader() && performer.getVehicle() != -10L && !performer.isVehicleCommander()) {
                  return true;
               }

               if (target.isFighting()) {
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " is too busy fighting!");
               } else if (performer.isTrading()) {
                  performer.getCommunicator().sendNormalServerMessage("You are already trading with someone.");
               } else if (target.isTrading() && !target.shouldStopTrading(true)) {
                  Trade trade = target.getTrade();
                  if (trade != null) {
                     Creature oppos = trade.creatureOne;
                     if (target.equals(oppos)) {
                        oppos = trade.creatureTwo;
                     }

                     String name = "someone";
                     if (oppos != null) {
                        name = oppos.getName();
                     }

                     performer.getCommunicator().sendNormalServerMessage(target.getName() + " is already trading with " + name + ".");
                  }
               } else {
                  if (!(target instanceof Player) && performer.isGuest()) {
                     performer.getCommunicator().sendNormalServerMessage("Guests may not trade with creatures to prevent abuse.");
                     return true;
                  }

                  if (target.getFloorLevel() != performer.getFloorLevel() && performer.getPower() <= 0) {
                     performer.getCommunicator().sendNormalServerMessage("You can't reach " + target.getName() + " there.");
                     return true;
                  }

                  if (!performer.isFriendlyKingdom(target.getKingdomId()) && performer.getPower() <= 0) {
                     boolean ok = false;
                     if (Servers.localServer.PVPSERVER && Servers.localServer.isChallengeOrEpicServer() && !Servers.localServer.HOMESERVER) {
                        Village v = target.getCurrentVillage();
                        if (v != null && v.getGuards().length > 0) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("There are guards in the vicinity. You can't start trading with " + target.getName() + " now.");
                           return true;
                        }

                        if (target.isNpcTrader()) {
                           Shop shop = Economy.getEconomy().getShop(target);
                           if (shop != null && shop.isPersonal()) {
                              ok = true;
                           }
                        }
                     }

                     if (!ok) {
                        performer.getCommunicator().sendNormalServerMessage(target.getName() + " snorts and refuses to trade with you.");
                        return true;
                     }
                  }

                  if (!(target instanceof Player) && performer.getPower() < 2) {
                     target.turnTowardsCreature(performer);

                     try {
                        target.getStatus().savePosition(target.getWurmId(), false, target.getStatus().getZoneId(), true);
                     } catch (IOException var19) {
                     }
                  }

                  MethodsCreatures.initiateTrade(performer, target);
               }
               break;
            case 73:
               if (target.isPlayer()) {
                  if (performer.getKingdomId() != target.getKingdomId()) {
                     logger.log(Level.WARNING, performer.getName() + " tried to invite ENEMY " + target.getName() + " as villager!");
                  } else if (target.isGuest()) {
                     performer.getCommunicator().sendAlertServerMessage("You just tried to invite a guest. This should not be possible and has been logged.");
                     logger.log(Level.WARNING, performer.getName() + " has managed to invite a guest. This should not be possible, so cheating is involved.");
                  } else if (((Player)performer).getSaveFile().getPaymentExpire() <= 0L && !Servers.localServer.isChallengeServer()) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "You may not invite other players to your settlement unless you are premium. The settlement will disband if your character is deleted and the settlement needs a mayor."
                        );
                  } else if (target.isPlayer() && target.mayChangeVillageInMillis() > 0L) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           target.getName() + " may not change village until " + Server.getTimeFor(target.mayChangeVillageInMillis()) + " has elapsed."
                        );
                  } else {
                     Methods.sendJoinVillageQuestion(performer, target);
                  }
               }
               break;
            case 81:
               if (performer.getKingdomId() != target.getKingdomId()) {
                  logger.log(Level.WARNING, performer.getName() + " tried to invite ENEMY " + target.getName() + " as ally!");
               } else {
                  Methods.sendAllianceQuestion(performer, target);
               }
               break;
            case 89:
               done = true;
               handle_SETKINGDOM(performer, target);
               break;
            case 90:
               Server.getInstance().pollShopDemands();
               performer.getCommunicator().sendAlertServerMessage("You just lowered the demand of all traders by 0.9!");
               break;
            case 91:
               handle_ASK_REFRESH(performer, target);
               break;
            case 103:
               if (target.isInvulnerable() && performer.getPower() < 5) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(target.getName() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
               } else if (performer.isGuest()) {
                  performer.getCommunicator().sendNormalServerMessage("Guests may not attack.");
               } else if (performer.mayAttack(target)) {
                  if (performer.isFighting() && target.opponent != performer) {
                     done = CombatEngine.taunt(performer, target, counter, act);
                  }
               } else if (!performer.isStunned() && !performer.isUnconscious()) {
                  performer.getCommunicator().sendNormalServerMessage("You are too inexperienced to start attacking anyone.");
               }
               break;
            case 105:
               if (performer.isGuest()) {
                  performer.getCommunicator().sendNormalServerMessage("Guests may not attack.");
               } else if (target.isInvulnerable() && performer.getPower() < 5) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(target.getName() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
               } else if (!performer.mayAttack(target)) {
                  if (!performer.isStunned() && !performer.isUnconscious()) {
                     performer.getCommunicator().sendNormalServerMessage("You are too inexperienced to start attacking anyone.");
                  }
               } else if (performer.isFighting()) {
                  if (target.isDead()) {
                     return true;
                  }

                  done = CombatEngine.shieldBash(performer, target, counter);
               }
               break;
            case 107:
               if (target.getLeader() == performer) {
                  target.setLeader(null);
                  if (target.getVisionArea() != null) {
                     target.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
                  }
               }
               break;
            case 111:
               if (Features.Feature.WAGONER.isEnabled() && target.isWagoner()) {
                  Wagoner wagoner = target.getWagoner();
                  if (wagoner != null && wagoner.getState() == 0) {
                     wagoner.forceStateChange((byte)4);
                  }
               }
               break;
            case 114:
               if (target == performer.getTarget() || !performer.hasLink()) {
                  if (performer.isGuest()) {
                     performer.getCommunicator().sendNormalServerMessage("Guests may not attack.");
                  } else if (target.isInvulnerable() && performer.getPower() < 5) {
                     performer.getCommunicator()
                        .sendNormalServerMessage(target.getName() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
                     performer.setTarget(-10L, true);
                  } else if (performer.mayAttack(target)) {
                     target.setStealth(false);
                     done = performer.getCombatHandler().attack(target, Server.getCombatCounter(), false, counter, act);
                     setOpponent(performer, target, done, act);
                  } else if (!performer.isStunned() && !performer.isUnconscious()) {
                     performer.getCommunicator().sendNormalServerMessage("You are too weak or not allowed to attack " + target.getName() + ".");
                  }
               }
               break;
            case 136:
               done = MethodsCreatures.stealth(performer, counter);
               break;
            case 140:
               if (Features.Feature.WAGONER.isEnabled() && target.isWagoner()) {
                  Wagoner wagoner = target.getWagoner();
                  if (wagoner != null && wagoner.getState() == 0) {
                     wagoner.forceStateChange((byte)1);
                  }
               }
               break;
            case 180:
               if (target instanceof Player) {
                  performer.getCommunicator().sendNormalServerMessage("You can't go around destroying players!");
                  logger.log(Level.WARNING, performer.getName() + " tried to destroy a creature: " + target);
               } else if (performer.getPower() >= 2) {
                  MethodsCreatures.destroyCreature(target);
               }
               break;
            case 185:
               done = true;
               handle_GETINFO(performer, target);
               break;
            case 197:
            case 198:
            case 199:
            case 200:
            case 201:
            case 202:
            case 203:
            case 204:
            case 205:
            case 206:
            case 207:
            case 208:
               done = handle_SPECMOVE(performer, target, action, counter);
               break;
            case 209:
               if (target.isGuest()) {
                  performer.getCommunicator()
                     .sendAlertServerMessage("You just tried to declare war to a guest. This should not be possible and has been logged.");
                  logger.log(
                     Level.WARNING, performer.getName() + " has managed to declare war to a guest. This should not be possible, so cheating is involved."
                  );
               } else if (performer.getCitizenVillage() == null) {
                  performer.getCommunicator().sendAlertServerMessage("You are no longer a citizen of a village.");
               } else if (target.getCitizenVillage() == null) {
                  performer.getCommunicator().sendAlertServerMessage(target.getName() + " is no longer a citizen of a village.");
               } else if (!performer.getCitizenVillage().mayDeclareWarOn(target.getCitizenVillage())) {
                  performer.getCommunicator().sendAlertServerMessage(target.getName() + " is already at war with your village.");
               } else {
                  Methods.sendWarDeclarationQuestion(performer, target.getCitizenVillage());
               }
               break;
            case 210:
               if (target.isGuest()) {
                  performer.getCommunicator()
                     .sendAlertServerMessage("You just tried to offer peace to a guest. This should not be possible and has been logged.");
                  logger.log(
                     Level.WARNING, performer.getName() + " has managed to offer peace to a guest. This should not be possible, so cheating is involved."
                  );
               } else if (performer.getCitizenVillage() == null) {
                  performer.getCommunicator().sendAlertServerMessage("You are no longer a citizen of a village.");
               } else if (target.getCitizenVillage() == null) {
                  performer.getCommunicator().sendAlertServerMessage(target.getName() + " is no longer a citizen of a village.");
               } else if (performer.getCitizenVillage().mayDeclareWarOn(target.getCitizenVillage())) {
                  performer.getCommunicator().sendAlertServerMessage(target.getName() + " is no longer at war with your village.");
               } else {
                  Methods.sendVillagePeaceQuestion(performer, target);
               }
               break;
            case 213:
               if (target.isGuide() && performer.getKingdomId() != target.getKingdomId()) {
                  if (performer.isChampion()) {
                     performer.getCommunicator().sendNormalServerMessage("You are a champion and may not change kingdom.");
                  } else {
                     MethodsCreatures.sendAskKingdomQuestion(target, performer);
                  }
               }
               break;
            case 214:
               handle_ASK_GIFT(performer, target);
               break;
            case 240:
            case 241:
            case 242:
            case 243:
               if (performer.getPower() >= 2) {
                  performer.getCommunicator().sendNormalServerMessage("You try to transfer " + target.getName() + ".");
                  target.getCommunicator().sendNormalServerMessage(performer.getName() + " tries to transfer you.");
                  done = Methods.transferPlayer(performer, target, act, counter);
               }
               break;
            case 326:
            case 716:
               if (handle_TARGET_and_TARGET_HOSTILE(performer, target, action)) {
                  return true;
               }
               break;
            case 327:
               if (performer.getPower() >= 2 && target.getPower() <= performer.getPower()) {
                  if (!target.isPlayer()) {
                     performer.getCommunicator().sendNormalServerMessage("Your wand has no effect.");
                  } else {
                     String message = target.getName();
                     String logActionMsg;
                     if (target.isFrozen()) {
                        logActionMsg = "thaws";
                        message = message + " emits a deep sigh of relief and stretches " + target.getHisHerItsString() + " legs.";
                     } else {
                        logActionMsg = "freezes";
                        message = message
                           + " gnaws "
                           + target.getHisHerItsString()
                           + " teeth as "
                           + target.getHisHerItsString()
                           + " legs refuse to move any longer.";
                     }

                     logger.log(Level.INFO, performer.getName() + " " + logActionMsg + " " + target.getName());
                     performer.getCommunicator().sendNormalServerMessage(message);
                     target.toggleFrozen(performer);
                     Server.getInstance().broadCastAction(message, performer, target, 5);
                  }
               }
               break;
            case 331:
               done = MethodsCreatures.ride(performer, target, action);
               break;
            case 332:
               done = MethodsCreatures.ride(performer, target, action);
               break;
            case 333:
            case 708:
               if (performer.getVehicle() != -10L) {
                  if (performer.getVisionArea() != null) {
                     performer.getVisionArea().broadCastUpdateSelectBar(performer.getWurmId(), true);
                  }

                  performer.disembark(true);
               }
               break;
            case 342:
               performer.getCommunicator().sendNormalServerMessage("You need to select an item in order to throw it.");
               return true;
            case 343:
               if (target.isPlayer() && target.getKingdomId() == performer.getKingdomId() && !performer.isFighting()) {
                  if (performer.isDuelling(target)) {
                     ((Player)performer).removeDuellist(target);
                     ((Player)target).removeDuellist(performer);
                  } else {
                     DuelQuestion dq = new DuelQuestion(
                        target, "Duel to the death", "Do you wish to duel with " + performer.getName() + " to the death?", 59, performer.getWurmId()
                     );
                     dq.sendQuestion();
                     performer.getCommunicator()
                        .sendNormalServerMessage("You ask " + target.getName() + " if " + target.getHeSheItString() + " wants to duel to the death with you.");
                  }
               }
               break;
            case 344:
               if (target.isPlayer()) {
                  if (performer.isSparring(target)) {
                     ((Player)performer).removeSparrer(target);
                     ((Player)target).removeSparrer(performer);
                  } else {
                     DuelQuestion dq = new DuelQuestion(target, "Sparring", "Do you wish to spar with " + performer.getName() + "?", 60, performer.getWurmId());
                     dq.sendQuestion();
                     performer.getCommunicator()
                        .sendNormalServerMessage("You ask " + target.getName() + " if " + target.getHeSheItString() + " wants to spar with you.");
                  }
               }
               break;
            case 351:
               handle_ASK_TUTORIAL(performer, target);
               break;
            case 352:
               if (performer.getPower() >= 2) {
                  if (target.getPower() > performer.getPower()) {
                     performer.getCommunicator().sendSafeServerMessage("You may not log " + target.getName());
                     target.getCommunicator().sendSafeServerMessage(performer.getName() + " tried to log you.");
                  } else if (target.loggerCreature1 == -10L) {
                     target.loggerCreature1 = performer.getWurmId();
                     performer.getCommunicator().sendSafeServerMessage("You now log " + target.getName());
                     performer.getLogger().log(Level.INFO, "Started logging " + target.getName());
                  } else if (target.loggerCreature1 == performer.getWurmId()) {
                     target.loggerCreature1 = -10L;
                     performer.getCommunicator().sendSafeServerMessage("You no longer log " + target.getName());
                     performer.getLogger().log(Level.INFO, "Stopped logging " + target.getName());
                  }
               }
               break;
            case 353:
               if (Servers.localServer.isChallengeServer()) {
                  done = true;
               } else if (target.getTemplate().isRoyalAspiration()) {
                  if (King.getKing(target.getKingdomId()) != null) {
                     done = true;
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "The "
                              + King.getKing(target.getKingdomId()).getRulerTitle()
                              + " is appointed already. The "
                              + target.getName()
                              + " is still and silent."
                        );
                     if (target.getKingdomId() == 1) {
                        Methods.resetJennElector();
                     }

                     if (target.getKingdomId() == 3) {
                        Methods.resetHotsElector();
                     }
                  } else {
                     done = Methods.aspireKing(performer, target.getKingdomId(), null, target, act, counter);
                     if (done) {
                        if (target.getKingdomId() == 1) {
                           Methods.resetJennElector();
                        }

                        if (target.getKingdomId() == 3) {
                           Methods.resetHotsElector();
                        }

                        if (target.getKingdomId() == 2) {
                           Methods.resetMolrStone();
                        }
                     }
                  }
               }
               break;
            case 357:
               if (performer.isRoyalAnnouncer()) {
                  performer.getCommunicator().sendNormalServerMessage("You announce " + target.getAnnounceString());
                  Server.getInstance().broadCastAction(performer.getName() + " announces, '" + target.getAnnounceString() + "'", performer, 5);
               }
               break;
            case 378:
               if (performer.getVehicle() != -10L) {
                  Vehicle hitched = target.getHitched();
                  if (hitched != null && hitched.getWurmid() == performer.getVehicle() && performer.isVehicleCommander()) {
                     if (!hitched.positionDragger(target, performer)) {
                        performer.getCommunicator().sendNormalServerMessage("You can't unhitch the " + target.getName() + " here. Please move the vehicle.");
                        return true;
                     }

                     try {
                        Zone z = Zones.getZone(target.getTilePos(), target.isOnSurface());
                        target.getStatus().savePosition(target.getWurmId(), true, z.getId(), true);
                     } catch (Exception var16) {
                        logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                     }

                     hitched.removeDragger(target);
                     Creatures.getInstance().setLastLed(target.getWurmId(), performer.getWurmId());
                     VolaTile t = target.getCurrentTile();
                     if (t == null) {
                        logger.log(Level.WARNING, target.getName() + " has no tile?");
                     } else {
                        t.sendAttachCreature(target.getWurmId(), -10L, 0.0F, 0.0F, 0.0F, 0);
                     }
                  }
               }
               break;
            case 379:
               if (performer.isPlayer()) {
                  done = MethodsCreatures.breed(performer, target, action, act, counter);
               } else {
                  done = MethodsCreatures.naturalBreed(performer, target, act, counter);
               }
               break;
            case 392:
               if (performer.getPower() >= 5) {
                  if (target.getEffects() == null) {
                     target.addEffect(
                        EffectFactory.getInstance()
                           .createGenericEffect(
                              target.getWurmId(),
                              "traitor",
                              target.getPosX(),
                              target.getPosY(),
                              target.getPositionZ(),
                              target.isOnSurface(),
                              -1.0F,
                              target.getStatus().getRotation()
                           )
                     );
                  } else {
                     target.removeEffect(EffectFactory.getInstance().getEffectForOwner(target.getWurmId()));
                     EffectFactory.getInstance().deleteEffByOwner(target.getWurmId());
                  }
               }
               break;
            case 398:
               performer.getCommunicator().sendNormalServerMessage("You need to use a tool to do that.");
               done = true;
               break;
            case 469:
               if (performer != target && performer.mayInviteTeam()) {
                  try {
                     if (performer.getTeam() == null) {
                        TeamManagementQuestion tme = new TeamManagementQuestion(
                           performer, "Found a team", "Creating a team", false, target.getWurmId(), false, false
                        );
                        tme.sendQuestion();
                     } else {
                        TeamManagementQuestion tme = new TeamManagementQuestion(
                           performer, "Expanding the team", "Inviting " + target.getName(), false, target.getWurmId(), false, false
                        );
                        tme.sendQuestion();
                     }
                  } catch (Exception var21) {
                  }
               }
               break;
            case 470:
               if (performer.getTeam() == target.getTeam() && (performer.isTeamLeader() || performer.mayInviteTeam() && !target.mayInviteTeam())) {
                  performer.getCommunicator().sendNormalServerMessage("You remove " + target.getName() + " from the team.");
                  target.setTeam(null, true);
               }
               break;
            case 471:
               if (performer.isTeamLeader()) {
                  try {
                     TeamManagementQuestion tme = new TeamManagementQuestion(
                        performer, "Managing the team", "Managing " + performer.getTeam().getName(), false, performer.getWurmId(), true, false
                     );
                     tme.sendQuestion();
                  } catch (Exception var20) {
                  }
               }
               break;
            case 490:
               done = true;
               handle_FINAL_BREATH(performer, target);
               break;
            case 493:
               done = true;
               handle_SET_PROTECTED(performer, target);
               break;
            case 537:
               done = true;
               if (Servers.localServer.PVPSERVER && Servers.localServer.EPIC && !Servers.localServer.HOMESERVER) {
                  if (target.getFloorLevel() != performer.getFloorLevel() || performer.getMountVehicle() != null) {
                     performer.getCommunicator().sendNormalServerMessage("You can't reach " + target.getName() + " there.");
                     done = true;
                  } else if (target.isFriendlyKingdom(performer.getKingdomId())) {
                     performer.getCommunicator().sendNormalServerMessage("You can't rob " + target.getName() + "!");
                  } else if (!target.isNpcTrader()) {
                     performer.getCommunicator().sendNormalServerMessage(target.getName() + " snorts at you and refuses to yield.");
                  } else {
                     Shop shop = Economy.getEconomy().getShop(target);
                     if (shop != null && shop.isPersonal()) {
                        int time = act.getTimeLeft();
                        done = false;
                        Skill taunting = null;

                        try {
                           taunting = performer.getSkills().getSkill(10057);
                        } catch (NoSuchSkillException var18) {
                           taunting = performer.getSkills().learn(10057, 1.0F);
                        }

                        if (counter == 1.0F) {
                           Village v = target.getCurrentVillage();
                           if (v != null && v.getGuards().length > 0) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("There are guards in the vicinity. You can't start robbing " + target.getName() + " now.");
                              return true;
                           }

                           performer.getCommunicator().sendNormalServerMessage("You start to rob " + target.getNameWithGenus() + ".");
                           time = Actions.getSlowActionTime(performer, taunting, null, 0.0) * 10;
                           Server.getInstance().broadCastAction(performer.getNameWithGenus() + " starts robbing " + target.getNameWithGenus(), performer, 10);
                           performer.sendActionControl("threatening", true, time);
                           act.setTimeLeft(time);
                           performer.getStatus().modifyStamina(-500.0F);
                        }

                        if (!(counter * 10.0F <= (float)time)) {
                           done = true;
                           if (taunting.skillCheck(target.getSoulStrengthVal(), 0.0, false, 20.0F) <= 0.0) {
                              performer.getCommunicator().sendNormalServerMessage(target.getName() + " snorts at you and refuses to yield.");
                              Server.getInstance()
                                 .broadCastAction(performer.getNameWithGenus() + " fails to scare " + target.getNameWithGenus() + ".", performer, 10);
                           } else {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(
                                    target.getNameWithGenus() + " looks really scared and fetches " + target.getHisHerItsString() + " hidden stash."
                                 );
                              Server.getInstance()
                                 .broadCastAction(
                                    performer.getNameWithGenus()
                                       + " scares "
                                       + target.getNameWithGenus()
                                       + " into fetching "
                                       + target.getHisHerItsString()
                                       + " hidden stash.",
                                    performer,
                                    10
                                 );
                              Item[] invitems = target.getInventory().getAllItems(false);

                              for(Item i : invitems) {
                                 if (!i.isCoin()) {
                                    try {
                                       i.putItemInfrontof(target);
                                    } catch (NoSuchPlayerException | NoSuchCreatureException | NoSuchItemException | NoSuchZoneException var17) {
                                       logger.log(Level.INFO, target.getName() + " : " + var17.getMessage());
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               break;
            case 663:
               if (target.mayManage(performer)) {
                  ManageObjectList.Type animalType = ManageObjectList.Type.ANIMAL0;
                  if (target.isWagoner()) {
                     animalType = ManageObjectList.Type.WAGONER;
                  } else {
                     Vehicle vehicle = Vehicles.getVehicle(target);
                     if (vehicle == null || vehicle.isUnmountable()) {
                        animalType = ManageObjectList.Type.ANIMAL0;
                     } else if (vehicle.getMaxPassengers() == 0) {
                        animalType = ManageObjectList.Type.ANIMAL1;
                     } else {
                        animalType = ManageObjectList.Type.ANIMAL2;
                     }
                  }

                  ManagePermissions mp = new ManagePermissions(performer, animalType, target, false, -10L, false, null, "");
                  mp.sendQuestion();
               }
               break;
            case 691:
               if (target.isWagoner()) {
                  Wagoner wagoner = Wagoner.getWagoner(target.getWurmId());
                  if (wagoner == null) {
                     performer.getCommunicator().sendNormalServerMessage("Cannot find the wagoner associated with this creature, most odd.");
                     break;
                  }

                  if (wagoner.getVillageId() == -1) {
                     performer.getCommunicator().sendNormalServerMessage("Wagoner is in progress of being dismissed..");
                     break;
                  }
               }

               if (target.maySeeHistory(performer)) {
                  PermissionsHistory ph = new PermissionsHistory(performer, target.getWurmId());
                  ph.sendQuestion();
               }
               break;
            case 919:
               if (target.isWagoner()) {
                  Wagoner wagoner = Wagoner.getWagoner(target.getWurmId());
                  if (wagoner == null) {
                     performer.getCommunicator().sendNormalServerMessage("Cannot find the wagoner associated with this creature, most odd.");
                  } else if (wagoner.getVillageId() == -1) {
                     performer.getCommunicator().sendNormalServerMessage("Wagoner is in progress of being dismissed..");
                  } else {
                     WagonerHistory wh = new WagonerHistory(performer, target.getWagoner());
                     wh.sendQuestion();
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("Creature is not a wagoner.");
               }
               break;
            case 928:
               if (performer.getPower() >= 2 && !target.isPlayer()) {
                  CreatureChangeAgeQuestion q = new CreatureChangeAgeQuestion(performer, "Change Age", "", target.getWurmId());
                  q.sendQuestion();
               }
         }

         return done;
      }
   }

   private static boolean handle_SPECMOVE(@Nonnull Creature performer, @Nonnull Creature target, short action, float counter) {
      Communicator comm = performer.getCommunicator();
      if (target == performer) {
         comm.sendCombatNormalMessage("You need to fight a real enemy to perform special moves.");
         logger.fine(performer.getName() + " tried to attack themself and was told to attack a real enemy for SpecialMove: " + action);
         return true;
      } else if (target.isInvulnerable() && performer.getPower() < 5) {
         comm.sendNormalServerMessage(target.getNameWithGenus() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
         return true;
      } else if (!performer.isFighting() || target != performer.opponent) {
         return true;
      } else if (target.isDead()) {
         return true;
      } else {
         if (target.opponent == null) {
            target.setOpponent(performer);
         }

         Item primweapon = performer.getPrimWeapon();
         if (primweapon == null) {
            comm.sendCombatNormalMessage("You need to wield a weapon in order to perform a special move.");
            return true;
         } else {
            double fightskill;
            try {
               fightskill = performer.getSkills().getSkill(primweapon.getPrimarySkill()).getKnowledge(0.0);
            } catch (NoSuchSkillException var19) {
               comm.sendCombatNormalMessage("You are not proficient enough with the " + primweapon.getName() + " to perform such a feat.");
               return true;
            }

            if (fightskill <= 19.0) {
               return true;
            } else {
               CombatHandler tgtCmbtHndl = target.getCombatHandler();
               CombatHandler srcCmbtHndl = performer.getCombatHandler();
               byte tgtStance = tgtCmbtHndl.getCurrentStance();
               byte srcStance = srcCmbtHndl.getCurrentStance();
               if (!CombatHandler.isStanceOpposing(tgtStance, srcStance) && !CombatHandler.isStanceParrying(tgtStance, srcStance)) {
                  SpecialMove[] specialmoves = SpecialMove.getMovesForWeaponSkillAndStance(performer, primweapon, (int)fightskill);
                  if (specialmoves.length <= 0) {
                     return true;
                  } else if (target.isDead()) {
                     return true;
                  } else {
                     boolean done = false;
                     if (counter != 1.0F) {
                        SpecialMove tempmove = performer.specialMove;
                        if (tempmove == null) {
                           return true;
                        } else if (tempmove.getWeaponType() != -1 && srcCmbtHndl.getType(primweapon, true) != tempmove.getWeaponType()) {
                           comm.sendCombatNormalMessage("You can't perform a " + tempmove.getName() + " with the " + performer.getPrimWeapon().getName() + ".");
                           return true;
                        } else if (counter < (float)tempmove.getSpeed()) {
                           return false;
                        } else if (performer.getStatus().getStamina() < tempmove.getStaminaCost()) {
                           comm.sendCombatNormalMessage("You have no stamina left to perform a " + tempmove.getName() + ".");
                           return true;
                        } else {
                           try {
                              double eff = performer.getSkills()
                                 .getSkill(primweapon.getPrimarySkill())
                                 .skillCheck(tempmove.getDifficulty(), 0.0, primweapon.isWeaponBow(), 5.0F, performer, target);
                              if (eff > 0.0) {
                                 comm.sendCombatNormalMessage("You try a " + tempmove.getName() + ".");
                                 tempmove.doEffect(performer, performer.getPrimWeapon(), performer.opponent, Math.max(20.0, eff));
                              } else {
                                 performer.getStatus().modifyStamina((float)(-tempmove.getStaminaCost() / 3));
                                 comm.sendCombatNormalMessage("You try a " + tempmove.getName() + " but miss.");
                                 Server.getInstance()
                                    .broadCastAction(performer.getNameWithGenus() + " tries a " + tempmove.getName() + " but misses.", performer, 2, true);
                              }

                              return true;
                           } catch (NoSuchSkillException var18) {
                              comm.sendCombatNormalMessage("You fail to perform the attack.");
                              logger.log(Level.WARNING, performer.getName() + " trying spec move with " + performer.getPrimWeapon().getName());
                              return true;
                           }
                        }
                     } else if (performer.combatRound < 3) {
                        comm.sendCombatNormalMessage("You have not moved into position yet.");
                        return true;
                     } else {
                        try {
                           SpecialMove tempmove = specialmoves[action - 197];
                           if (tempmove != null) {
                              performer.specialMove = tempmove;
                              performer.sendActionControl(tempmove.getName(), true, tempmove.getSpeed() * 10);
                              ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                              segments.add(new CreatureLineSegment(performer));
                              segments.add(new MulticolorLineSegment(" prepare to perform a " + tempmove.getName() + " on ", (byte)7));
                              segments.add(new CreatureLineSegment(target));
                              segments.add(new MulticolorLineSegment("!", (byte)7));
                              performer.getCommunicator().sendColoredMessageCombat(segments, (byte)2);
                              segments.get(1).setText(" prepares to perform a " + tempmove.getName() + " on ");
                              target.getCommunicator().sendColoredMessageCombat(segments, (byte)4);
                           } else {
                              performer.specialMove = null;
                              comm.sendCombatNormalMessage("No such move available right now.");
                              done = true;
                           }
                        } catch (Exception var17) {
                           comm.sendCombatNormalMessage("No such move available right now.");
                           done = true;
                        }

                        return done;
                     }
                  }
               } else {
                  comm.sendCombatNormalMessage(target.getNameWithGenus() + " is protecting that area.");
                  return true;
               }
            }
         }
      }
   }

   private static boolean handleStanceChange(@Nonnull Action act, @Nonnull Creature performer, Creature target, short action, float counter) {
      if (target != performer.getTarget()) {
         return true;
      } else {
         Communicator comm = performer.getCommunicator();
         if (target == performer) {
            if (counter == 1.0F) {
               comm.sendCombatNormalMessage("You show off some moves.");
               if (act.getNumber() > 0) {
                  byte newStance = CombatHandler.getStanceForAction(act.getActionEntry());
                  String animationName = getAnimationNameForStanceChange(newStance);
                  performer.playAnimation(animationName, false);
               }

               return false;
            }

            if (counter > 3.0F) {
               performer.playAnimation("idle", true);
               return true;
            }
         }

         if (target.isInvulnerable()) {
            comm.sendNormalServerMessage(target.getNameWithGenus() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
            return true;
         } else if (!performer.mayAttack(target)) {
            if (performer.isGuest()) {
               comm.sendNormalServerMessage("Guests may not attack.");
               return true;
            } else if (!performer.isStunned() && !performer.isUnconscious()) {
               comm.sendNormalServerMessage("You are too weak to attack anyone.");
               return true;
            } else {
               return true;
            }
         } else if (performer.opponent == null) {
            comm.sendCombatNormalMessage("You are not attacking anyone.");
            return true;
         } else if (target.isDead()) {
            return true;
         } else {
            CombatHandler perfCombatHandler = performer.getCombatHandler();
            if (perfCombatHandler.isOpen()) {
               comm.sendCombatNormalMessage("You are imbalanced and may not change stance right now.");
               return true;
            } else if (perfCombatHandler.isProne()) {
               comm.sendCombatNormalMessage("You are thrown to the ground, trying to get up.");
               return true;
            } else {
               if (act.getTimeLeft() < 20) {
                  act.setTimeLeft(50);
               }

               Communicator targetComm = target.getCommunicator();
               if (counter != 1.0F) {
                  if (counter * 10.0F <= (float)act.getTimeLeft()) {
                     return false;
                  } else if (action == 340) {
                     if (performer.opponent == null) {
                        return true;
                     } else if (CombatHandler.prerequisitesFail(performer, performer.opponent, false, performer.getPrimWeapon())) {
                        return true;
                     } else {
                        if (performer.getFightlevel() == 5) {
                           comm.sendCombatNormalMessage("You are already focused to the maximum.");
                        } else {
                           double num = performer.getFightingSkill()
                              .skillCheck((double)((float)((performer.getFightlevel() + 1) * 19) * ItemBonus.getFocusBonus(performer)), 0.0, true, 1.0F);
                           if (num > 0.0) {
                              if (Servers.localServer.testServer && performer.spamMode()) {
                                 comm.sendCombatNormalMessage(
                                    "Your result for focusing is "
                                       + num
                                       + " when difficulty is "
                                       + (performer.getFightlevel() + 1) * 10
                                       + " and skill "
                                       + performer.getFightingSkill().getKnowledge(0.0)
                                 );
                              }

                              performer.increaseFightlevel(1);
                              if (performer.getFightlevel() == 3) {
                                 performer.achievement(549);
                              } else if (performer.getFightlevel() == 5) {
                                 performer.achievement(562);
                              }

                              performer.getStatus().modifyStamina(-4000.0F);
                              ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                              segments.add(new CreatureLineSegment(performer));
                              segments.add(new MulticolorLineSegment(" now seems more focused.", (byte)0));
                              target.getCommunicator().sendColoredMessageCombat(segments);
                           } else {
                              comm.sendCombatNormalMessage("You fail to reach a higher degree of focus.");
                              ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                              segments.add(new CreatureLineSegment(performer));
                              segments.add(new MulticolorLineSegment(" looks disturbed.", (byte)0));
                              target.getCommunicator().sendColoredMessageCombat(segments);
                              performer.getStatus().modifyStamina(-1000.0F);
                           }

                           comm.sendFocusLevel(performer.getWurmId());
                        }

                        return true;
                     }
                  } else {
                     ActionEntry entry = act.getActionEntry();
                     if (perfCombatHandler.getCurrentStance() == CombatHandler.getStanceForAction(entry)) {
                        comm.sendCombatNormalMessage("Changing to existing stance.");
                        return true;
                     } else {
                        performer.getStatus().modifyStamina(-1000.0F);
                        String move = "";
                        if (entry.isAttackHigh()) {
                           move = "upper";
                        } else if (entry.isAttackLow()) {
                           move = "lower";
                        }

                        String dir;
                        if (entry.isAttackLeft()) {
                           dir = move.equals("") ? "left" : " left";
                        } else if (entry.isAttackRight()) {
                           dir = move.equals("") ? "right" : " right";
                        } else {
                           dir = move.equals("") ? "center" : " center";
                        }

                        float oppcr = 0.0F;
                        if (performer.opponent != null) {
                           oppcr = performer.opponent.getCombatHandler().getCombatRating(performer, performer.opponent.getPrimWeapon(), false);
                        }

                        float mycr = perfCombatHandler.getCombatRating(performer.opponent, performer.getPrimWeapon(), false);
                        float knowl = perfCombatHandler.getCombatKnowledgeSkill();
                        double chance = (double)CombatHandler.getMoveChance(
                           performer, performer.getPrimWeapon(), perfCombatHandler.getCurrentStance(), entry, mycr, oppcr, knowl
                        );
                        if (chance <= 0.0) {
                           comm.sendCombatNormalMessage("That move is too advanced for you.");
                           ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                           segments.add(new CreatureLineSegment(performer));
                           segments.add(new MulticolorLineSegment(" decides that the move is too advanced.", (byte)0));
                           target.getCommunicator().sendColoredMessageCombat(segments);
                           return true;
                        } else if ((double)(Server.rand.nextFloat() * 100.0F) >= chance) {
                           comm.sendCombatNormalMessage("You fail to move into position.");
                           ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                           segments.add(new CreatureLineSegment(performer));
                           segments.add(new MulticolorLineSegment(" fails to move into position.", (byte)0));
                           target.getCommunicator().sendColoredMessageCombat(segments);
                           return true;
                        } else {
                           perfCombatHandler.setCurrentStance(action, CombatHandler.getStanceForAction(entry));
                           ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                           segments.add(new CreatureLineSegment(performer));
                           segments.add(new MulticolorLineSegment(" move into position for the " + move + dir + " parts of ", (byte)0));
                           segments.add(new CreatureLineSegment(target));
                           segments.add(new MulticolorLineSegment(".", (byte)0));
                           performer.getCommunicator().sendColoredMessageCombat(segments);
                           segments.get(1).setText(" targets your " + move + dir + " parts.");
                           segments.remove(3);
                           segments.remove(2);
                           target.getCommunicator().sendColoredMessageCombat(segments);
                           perfCombatHandler.setSentAttacks(false);
                           perfCombatHandler.calcAttacks(false);
                           if (!performer.isAutofight()) {
                              comm.sendCombatOptions(
                                 CombatHandler.getOptions(perfCombatHandler.getMoveStack(), perfCombatHandler.getCurrentStance()), (short)0
                              );
                           }

                           return true;
                        }
                     }
                  }
               } else {
                  if (action == 340) {
                     int actionTime = performer.isPlayer() ? 50 : 20;
                     if (performer.opponent == null || performer.opponent == performer || Creature.rangeTo(performer, performer.opponent) > 6) {
                        comm.sendCombatNormalMessage("You are too far away from that right now.");
                        return true;
                     }

                     if (performer.combatRound < 3 && performer.getFightlevel() <= 0) {
                        comm.sendCombatNormalMessage("You need to get into the fight more first.");
                        return true;
                     }

                     if (performer.getFightlevel() == 5) {
                        comm.sendCombatNormalMessage("You are already focused to the maximum.");
                        return true;
                     }

                     comm.sendCombatNormalMessage("You try to focus.");
                     ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                     segments.add(new CreatureLineSegment(performer));
                     segments.add(new MulticolorLineSegment(" seems to focus.", (byte)0));
                     target.getCommunicator().sendColoredMessageCombat(segments);
                     performer.sendActionControl("focusing", true, actionTime);
                     act.setTimeLeft(actionTime);
                  } else {
                     float oppcr = 0.0F;
                     if (performer.opponent != null) {
                        oppcr = performer.opponent.getCombatHandler().getCombatRating(performer, performer.opponent.getPrimWeapon(), false);
                     }

                     float mycr = perfCombatHandler.getCombatRating(performer.opponent, performer.getPrimWeapon(), false);
                     ActionEntry entry = act.getActionEntry();
                     float knowl = perfCombatHandler.getCombatKnowledgeSkill();
                     double chance = (double)CombatHandler.getMoveChance(
                        performer, performer.getPrimWeapon(), perfCombatHandler.getCurrentStance(), entry, mycr, oppcr, knowl
                     );
                     if (chance < 1.0 || !(knowl > (float)CombatHandler.getAttackSkillCap(entry.getNumber()))) {
                        comm.sendCombatNormalMessage("That move is too advanced for you.");
                        return true;
                     }

                     String move = "";
                     if (entry.isAttackHigh()) {
                        move = "upper";
                     } else if (entry.isAttackLow()) {
                        move = "lower";
                     }

                     String dir;
                     if (entry.isAttackLeft()) {
                        dir = move.equals("") ? "left" : " left";
                     } else if (entry.isAttackRight()) {
                        dir = move.equals("") ? "right" : " right";
                     } else {
                        dir = move.equals("") ? "center" : " center";
                     }

                     ArrayList<MulticolorLineSegment> segments = new ArrayList<>();
                     segments.add(new CreatureLineSegment(performer));
                     segments.add(new MulticolorLineSegment(" try to move into position to target the " + move + dir + " parts of ", (byte)0));
                     segments.add(new CreatureLineSegment(target));
                     segments.add(new MulticolorLineSegment(".", (byte)0));
                     comm.sendColoredMessageCombat(segments);
                     segments.get(1).setText(" tries to move into position to target your " + move + dir + " parts.");
                     segments.remove(3);
                     segments.remove(2);
                     targetComm.sendColoredMessageCombat(segments);
                     short speed = (short)((int)Math.max(2.0F, perfCombatHandler.getSpeed(performer.getPrimWeapon()) / 2.0F));
                     act.setTimeLeft(speed * 10);
                     performer.sendActionControl(move + dir, true, act.getTimeLeft());
                     if (act.getNumber() > 0) {
                        byte newStance = CombatHandler.getStanceForAction(act.getActionEntry());
                        String animationName = getAnimationNameForStanceChange(newStance);
                        performer.playAnimation(animationName, false);
                     }
                  }

                  return false;
               }
            }
         }
      }
   }

   private static boolean handleDefend(@Nonnull Action act, @Nonnull Creature performer, short action, float counter) {
      Communicator comm = performer.getCommunicator();
      if (performer.getCombatHandler().isOpen()) {
         comm.sendCombatNormalMessage("You are imbalanced and may not defend right now.");
         return true;
      } else if (performer.getCombatHandler().isProne()) {
         comm.sendCombatNormalMessage("You are thrown to the ground, trying to get up.");
         return true;
      } else if (counter == 1.0F) {
         String dir = "center";
         if (action == 314) {
            dir = "higher";
         } else if (action == 316) {
            dir = "lower";
         }

         if (action == 315) {
            dir = "left";
         } else if (action == 317) {
            dir = "right";
         }

         comm.sendCombatNormalMessage("You prepare to shelter the " + dir + " parts of your body.");
         return false;
      } else if (counter < 3.0F) {
         return false;
      } else {
         ActionEntry entry = act.getActionEntry();
         if (performer.getCombatHandler().getCurrentStance() == CombatHandler.getStanceForAction(entry)) {
            return true;
         } else {
            String dir = "center";
            if (action == 314) {
               dir = "higher";
            } else if (action == 316) {
               dir = "lower";
            }

            if (action == 315) {
               dir = "left";
            } else if (action == 317) {
               dir = "right";
            }

            if (performer.getFightingSkill().skillCheck(10.0, 0.0, true, 1.0F) <= 0.0) {
               comm.sendCombatNormalMessage("You still feel open at the " + dir + " parts of your body.");
               return true;
            } else {
               comm.sendCombatNormalMessage("You shelter the " + dir + " parts of your body.");
               Server.getInstance()
                  .broadCastAction(
                     performer.getNameWithGenus() + " seems to shelter " + performer.getHisHerItsString() + " " + dir + " parts.", performer, 2, true
                  );
               performer.getCombatHandler().setCurrentStance(CombatHandler.getStanceForAction(entry));
               return true;
            }
         }
      }
   }

   private static void handle_FINAL_BREATH(@Nonnull Creature performer, @Nonnull Creature target) {
      if (Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().mayDealFinalBreath()) {
         if (target.isInvulnerable() || performer.getAttitude(target) != 2) {
            performer.getCommunicator()
               .sendCombatNormalMessage("You are not mentally triggered enough to attack " + target.getName() + " with your thought pulse now.");
         } else if (target.getCurrentTile() != null && performer.getCurrentTile() != null) {
            Structure one = target.getCurrentTile().getStructure();
            Structure two = performer.getCurrentTile().getStructure();
            if (one != two) {
               performer.getCommunicator().sendCombatNormalMessage("The structures block the force.");
            } else {
               performer.getCultist().touchCooldown5();
               byte woundType = 0;
               float infectionSeverity = 0.0F;
               float poisonSeverity = 0.0F;
               byte location = 1;
               if (performer.getCultist().getPath() == 2) {
                  woundType = 9;
                  location = 23;
               }

               if (performer.getCultist().getPath() == 3) {
                  woundType = 2;
               }

               if (performer.getCultist().getPath() == 1) {
                  woundType = 9;
                  location = 21;
                  poisonSeverity = 50.0F;
               }

               if (performer.getCultist().getPath() == 5) {
                  location = 34;
               }

               if (performer.getCultist().getPath() == 4) {
                  woundType = 6;
                  location = 25;
                  infectionSeverity = 50.0F;
               }

               int damage = target.isUnique() ? 1000 + Server.rand.nextInt(2000) : 15000 + Server.rand.nextInt(5000);
               Battle batle = target.getBattle();
               if (batle == null) {
                  batle = performer.getBattle();
               }

               performer.getCommunicator()
                  .sendCombatNormalMessage("You sharpen your thoughts into a shining arrow of energy with which you assault " + target.getName() + ".");
               logger.log(
                  Level.INFO,
                  performer.getName()
                     + " hurting "
                     + target.getName()
                     + " dam="
                     + damage
                     + " in "
                     + location
                     + ", infection="
                     + infectionSeverity
                     + ", poison="
                     + poisonSeverity
               );
               float armourMod = target.getArmourMod();
               if (armourMod == 1.0F || target.isVehicle()) {
                  try {
                     armourMod = ArmourTemplate.getArmourModForLocation(target, location, woundType);
                  } catch (NoArmourException var12) {
                  }
               }

               target.addAttacker(performer);
               CombatEngine.addWound(
                  performer,
                  target,
                  woundType,
                  location,
                  (double)damage,
                  armourMod,
                  "thought pulse",
                  batle,
                  infectionSeverity,
                  poisonSeverity,
                  false,
                  false,
                  false,
                  false
               );
            }
         } else {
            performer.getCommunicator().sendCombatNormalMessage("You are not in the same place.");
         }
      } else {
         performer.getCommunicator().sendCombatNormalMessage("You are not at high enough level of insight for this.");
      }
   }

   private static void handle_SET_PROTECTED(@Nonnull Creature performer, @Nonnull Creature target) {
      if (!target.isPlayer()) {
         Communicator comm = performer.getCommunicator();
         if (!target.isHuman() && !target.isGhost() && !target.isReborn() && !target.isUnique() && !target.onlyAttacksPlayers()) {
            if (target.getAttitude(performer) != 2) {
               Village v = Villages.getVillage(target.getTileX(), target.getTileY(), performer.isOnSurface());
               if (v != null && v != performer.getCitizenVillage()) {
                  comm.sendNormalServerMessage("You need to be citizen of " + v.getName() + " in order to care for the " + target.getName() + ".");
               } else {
                  BlockingResult result = Blocking.getBlockerBetween(performer, target, 4);
                  if (result != null) {
                     Blocker firstBlocker = result.getFirstBlocker();

                     assert firstBlocker != null;

                     comm.sendNormalServerMessage("The " + firstBlocker.getName() + " is in the way.");
                  } else if (target.getDominator() != null && target.getDominator() != performer) {
                     comm.sendNormalServerMessage("The " + target.getName() + " is tamed, but not by you, so you cannot care for it.");
                  } else {
                     Creatures creInst = Creatures.getInstance();
                     int tc = creInst.getNumberOfCreaturesProtectedBy(performer.getWurmId());
                     int max = performer.getNumberOfPossibleCreatureTakenCareOf();
                     if (!target.isCaredFor()) {
                        if (tc < max) {
                           creInst.setCreatureProtected(target, performer.getWurmId(), true);
                           comm.sendNormalServerMessage(
                              "You now care specially for "
                                 + target.getName()
                                 + ", to ensure longevity. You may care for "
                                 + (max - tc - 1)
                                 + " more creatures."
                           );
                        } else {
                           comm.sendNormalServerMessage("You may not care for any more creatures right now. You are already caring for " + tc + " creatures.");
                        }
                     } else if (target.getCareTakerId() == performer.getWurmId()) {
                        creInst.setCreatureProtected(target, -10L, false);
                        comm.sendNormalServerMessage(
                           "You let " + target.getName() + " go in order to care for other creatures. You may care for " + (max - tc + 1) + " more creatures."
                        );
                     }
                  }
               }
            }
         } else {
            comm.sendNormalServerMessage("It makes no sense caring for " + target.getNameWithGenus() + ".");
         }
      }
   }

   public static void handle_CAGE_SET_PROTECTED(@Nonnull Creature performer, @Nonnull Creature target) {
      if (!target.isPlayer()
         && !target.isHuman()
         && !target.isGhost()
         && !target.isReborn()
         && (!target.isCaredFor() || target.getCareTakerId() == performer.getWurmId())) {
         handle_SET_PROTECTED(performer, target);
      }
   }

   private static void handle_SETKINGDOM(@Nonnull Creature performer, @Nonnull Creature target) {
      Communicator comm = performer.getCommunicator();
      if (target instanceof Player && performer.getPower() < 2) {
         if (!target.acceptsInvitations()) {
            comm.sendNormalServerMessage(
               target.getName() + " does not accept invitations now. " + target.getHeSheItString() + " needs to type /invitations in a chat window."
            );
         } else if (target.isChampion()) {
            comm.sendNormalServerMessage(target.getName() + " is a champion and may not change kingdom.");
         } else {
            if (target.mayChangeKingdom(performer)) {
               MethodsCreatures.sendAskKingdomQuestion(performer, target);
            }
         }
      } else if (performer.getPower() >= 2) {
         if (target.isChampion()) {
            comm.sendNormalServerMessage(target.getName() + " is a champion and may not change kingdom.");
         } else {
            try {
               target.setKingdomId(performer.getKingdomId());
               comm.sendNormalServerMessage(target.getName() + " now is part of " + Kingdoms.getNameFor(target.getKingdomId()) + ".");
               if (performer.getLogger() != null) {
                  performer.getLogger()
                     .info(
                        performer.getName()
                           + " sets kingdom of "
                           + target.getName()
                           + ", Id: "
                           + target.getWurmId()
                           + " to "
                           + Kingdoms.getNameFor(target.getKingdomId())
                           + "."
                     );
               }

               target.getCommunicator().sendUpdateKingdomId();
            } catch (IOException var4) {
               logger.log(Level.WARNING, performer.getName() + ": " + var4.getMessage(), (Throwable)var4);
            }
         }
      }
   }

   private static void handle_WIZKILL(@Nonnull Creature performer, @Nonnull Creature target) {
      if (performer.getPower() > 1) {
         if (target.getPower() <= performer.getPower()) {
            logger.log(Level.INFO, performer.getName() + " WIZKILL " + target.getName() + ", id: " + target.getWurmId());
            Communicator perfComm = performer.getCommunicator();
            Communicator tgtComm = target.getCommunicator();
            perfComm.sendNormalServerMessage(
               target.getNameWithGenus() + " looks surprised as you quickly rip the heart out from " + target.getHisHerItsString() + " body."
            );
            tgtComm.sendAlertServerMessage("You look surprised as " + performer.getName() + " suddenly rips the heart out of your body.");
            tgtComm.sendAlertServerMessage("You see it throb one last time.");
            Server.getInstance()
               .broadCastAction(
                  target.getNameWithGenus()
                     + " looks very surprised as "
                     + performer.getName()
                     + " suddenly rips the heart out of "
                     + target.getHisHerItsString()
                     + " body.",
                  performer,
                  target,
                  5
               );
            if (performer.getLogger() != null) {
               performer.getLogger().log(Level.INFO, performer.getName() + " wizkills " + target.getName() + ", id: " + target.getWurmId());
            }

            try {
               Item heart = ItemFactory.createItem(636, 99.0F, performer.getName());
               heart.setData1(target.getTemplate().getTemplateId());
               heart.setName(target.getName().toLowerCase() + " heart");
               heart.setWeight(heart.getWeightGrams() * Math.max(20, target.getSize()), true);
               heart.setButchered();
               performer.getInventory().insertItem(heart, true);
            } catch (FailedException | NoSuchTemplateException var5) {
               logger.log(Level.WARNING, var5.getMessage());
            }

            target.die(true, "Wizkill");
         }
      }
   }

   private static void handle_ASK_GIFT(@Nonnull Creature performer, @Nonnull Creature target) {
      int targetTemplateId = target.getTemplate().getTemplateId();
      if (targetTemplateId == 46 || targetTemplateId == 47) {
         Communicator comm = performer.getCommunicator();
         if (!performer.isPaying() && performer.getPower() <= 1) {
            comm.sendNormalServerMessage("Sorry, you have played too little to receive this year's gift.");
         } else {
            String tgtName = target.getName();
            String perfName = performer.getName();
            if (performer.hasFlag(62)) {
               comm.sendNormalServerMessage(tgtName + " says, 'You have already received my gift this year, " + perfName + "'.");
            } else if (performer.hasFlag(63)) {
               comm.sendNormalServerMessage(
                  tgtName + " says, 'Sorry, but you must have paid for premium time to receive my gift this year, " + perfName + "'."
               );
            } else {
               byte kingdomId = performer.getKingdomId();
               boolean isEvil = kingdomId == 3;
               int fittingSanta = isEvil ? 47 : 46;
               String fittingBehaviour = isEvil ? "bad" : "good";
               String unbefitBehaviour = isEvil ? "good" : "bad";
               String fittingStr = tgtName + " says, 'You have been a " + fittingBehaviour + " person this year, " + perfName + ".";
               if (targetTemplateId != fittingSanta) {
                  comm.sendNormalServerMessage(fittingStr + " No gift for you!'.");
               } else {
                  if (!isEvil && performer.getReputation() < 0) {
                     comm.sendNormalServerMessage(
                        tgtName + " says, 'You have been a " + unbefitBehaviour + " person this year, " + perfName + ". But here is your gift anyways.'."
                     );
                  } else {
                     comm.sendNormalServerMessage(fittingStr + " Here is your gift.'.");
                  }

                  ItemBehaviour.awardChristmasPresent(performer);
               }
            }
         }
      }
   }

   private static void handle_ASK_REFRESH(@Nonnull Creature performer, Creature target) {
      Communicator comm = performer.getCommunicator();
      if (target.isBartender()) {
         if (performer.getTutorialLevel() == 7 && !performer.skippedTutorial()) {
            performer.missionFinished(true, true);
         }

         if (Servers.localServer.testServer) {
            performer.getStatus().refresh(0.5F, false);
            comm.sendNormalServerMessage(target.getName() + " feeds you some delicious steak and cool water.");
            performer.getBody().healFully();
            target.playAnimation("give", false);

            try {
               performer.setFavor(performer.getFaith());
            } catch (IOException var8) {
            }

            comm.sendNormalServerMessage(target.getName() + " heals you.");
         } else if (performer.getPlayingTime() >= 86400000L
            && performer.getPower() <= 0
            && (!Servers.localServer.entryServer || !performer.isPlayerAssistant())) {
            comm.sendNormalServerMessage(
               target.getName()
                  + " shakes "
                  + target.getHisHerItsString()
                  + " head at you and tells you that you are too experienced to ask for "
                  + target.getHisHerItsString()
                  + " help."
            );
            target.playAnimation("deny", false);
         } else {
            float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0F;
            performer.getStatus().refresh(nut, false);
            comm.sendNormalServerMessage(target.getName() + " feeds you some delicious steak and cool water.");
            target.playAnimation("give", false);
         }
      } else {
         boolean isAboveHero = performer.getPower() > 1;
         boolean canMeditate = Methods.isActionAllowed(performer, (short)384);
         Cultist cultist = performer.getCultist();
         boolean mayCultistRefresh = cultist != null && cultist.mayRefresh();
         if (target.isPlayer()
            && (isAboveHero || canMeditate && mayCultistRefresh)
            && (isAboveHero || performer.isPaying() && performer.isFriendlyKingdom(target.getKingdomId()))) {
            float nut = (float)(50 + Server.rand.nextInt(49)) / 100.0F;
            target.getStatus().refresh(nut, true);
            target.getCommunicator().sendNormalServerMessage(performer.getName() + " emits a positive wave of energy in your direction!");
            comm.sendNormalServerMessage("You send " + target.getName() + " a warm thought.");
            if (cultist != null) {
               cultist.touchCooldown1();
            }

            if (performer.getLogger() != null) {
               performer.getLogger().info(performer.getName() + " refreshed " + target.getName() + ", Id: " + target.getWurmId());
            }
         }
      }
   }

   private static boolean handle_TARGET_and_TARGET_HOSTILE(@Nonnull Creature performer, Creature target, short action) {
      Communicator comm = performer.getCommunicator();
      if (action == 716 && target.getAttitude(performer) != 2) {
         comm.sendNormalServerMessage(target.getNameWithGenus() + " is not hostile towards you.");
         return true;
      } else if (target.isInvulnerable()) {
         return false;
      } else {
         if (!Servers.isThisAPvpServer()) {
            Village bVill = target.getBrandVillage();
            if (bVill != null && !bVill.mayAttack(performer, target)) {
               comm.sendNormalServerMessage("You cannot attack this branded animal.");
               performer.setTarget(-10L, true);
               return true;
            }

            if (target.isRiddenBy(performer.getWurmId())) {
               comm.sendNormalServerMessage("You cannot attack your own mount.");
               performer.setTarget(-10L, true);
               return true;
            }
         }

         Village village = target.getCurrentVillage();
         if (village == null) {
            if (target.isPlayer()
               && (!target.isOnPvPServer() || !performer.isOnPvPServer())
               && !target.isDuelOrSpar(performer)
               && !performer.isDuelOrSpar(target)) {
               comm.sendNormalServerMessage("That would be very bad for your karma and is disallowed on this server.");
               performer.setTarget(-10L, true);
               return true;
            }
         } else {
            boolean pvpServer = target.isOnPvPServer() && performer.isOnPvPServer();
            if ((performer.isFriendlyKingdom(target.getKingdomId()) || !pvpServer)
               && !village.mayAttack(performer, target)
               && (performer.isLegal() || !pvpServer)) {
               comm.sendNormalServerMessage("That would be illegal here. You can check the settlement token for the local laws.");
               performer.setTarget(-10L, true);
               return true;
            }
         }

         if (!target.isPlayer() && target.getCitizenVillage() != null && target.getCitizenVillage() == performer.getCitizenVillage()) {
            comm.sendSafeServerMessage("You will not target " + target.getNameWithGenus() + ".");
            return true;
         } else {
            performer.setTarget(target.getWurmId(), true);
            comm.sendSafeServerMessage("You target " + target.getNameWithGenus() + ".");
            return false;
         }
      }
   }

   private static void handle_ASK_TUTORIAL(@Nonnull Creature performer, Creature target) {
      target.turnTowardsCreature(performer);
      if (!performer.skippedTutorial()) {
         OldMission m = OldMission.getMission(performer.getTutorialLevel(), performer.getKingdomId());
         if (m != null) {
            MissionQuestion ms = new MissionQuestion(m.number, performer, m.title, m.missionDescription, target.getWurmId());
            ms.sendQuestion();
         } else {
            SimplePopup popup = new SimplePopup(performer, "Already finished", "The " + target.getName() + " has no more instructions for you.");
            popup.sendQuestion();
         }
      } else {
         OldMission m = OldMission.getMission(9999, performer.getKingdomId());
         if (m != null) {
            MissionQuestion ms = new MissionQuestion(m.number, performer, m.title, m.missionDescription, target.getWurmId());
            ms.sendQuestion();
         }
      }
   }

   private static void handle_GETINFO(@Nonnull Creature performer, Creature target) {
      if (performer.getPower() >= 2
         || Methods.isActionAllowed(performer, (short)384) && performer.getCultist() != null && performer.getCultist().mayCreatureInfo()) {
         Communicator comm = performer.getCommunicator();
         if (performer.getCultist() != null && performer.getCultist().mayCreatureInfo()) {
            performer.getCultist().touchCooldown1();
            comm.sendNormalServerMessage("You decide to classify " + target.getNameWithGenus() + " using numbers.");
            comm.sendNormalServerMessage("Stamina level " + target.getStatus().getStamina() + ", damage level " + target.getStatus().damage + ".");
            String domname = "none";

            try {
               domname = Players.getInstance().getNameFor(target.dominator);
            } catch (Exception var18) {
            }

            comm.sendNormalServerMessage(
               "Hunger value "
                  + target.getStatus().getHunger()
                  + ", fat level "
                  + target.getStatus().fat
                  + ", nutrition level "
                  + target.getStatus().getNutritionlevel() * 100.0F
                  + ", thirst level "
                  + target.getStatus().getThirst()
                  + ", dominated by "
                  + domname
                  + ", loyalty level "
                  + target.getStatus().loyalty
                  + "."
            );
            comm.sendNormalServerMessage("Normal stamina regen " + target.getStatus().hasNormalRegen() + ".");
            String leader = "none";
            if (target.getLeader() != null) {
               leader = target.getLeader().getName();
            }

            String hitched = "none";
            if (target.getHitched() != null) {
               hitched = target.getHitched().getName();
            }

            comm.sendNormalServerMessage("Kingdom is " + Kingdoms.getNameFor(target.getKingdomId()) + ", leader is " + leader + ", hitched to " + hitched);
            if (target.isPlayer()) {
               comm.sendNormalServerMessage("Rank=" + ((Player)target).getRank() + ", Max Rank=" + ((Player)target).getMaxRank());
            }

            try {
               if (target.getCurrentAction() != null) {
                  comm.sendNormalServerMessage("Busy " + target.getCurrentAction().getActionString());
               }
            } catch (NoSuchActionException var17) {
               comm.sendNormalServerMessage("Not busy doing anything.");
            }

            String opostring = "none";
            if (target.opponent != null) {
               opostring = target.opponent.getNameWithGenus();
            }

            String targstring = "none";
            if (target.target > -10L) {
               try {
                  Creature c = Server.getInstance().getCreature(target.target);
                  targstring = c.getNameWithGenus();
               } catch (NoSuchCreatureException var15) {
                  targstring = " unknown creature. That is bad so setting to none.";
                  target.setTarget(-10L, true);
               } catch (NoSuchPlayerException var16) {
                  targstring = " unknown player. That is bad so setting to none.";
                  target.setTarget(-10L, true);
               }
            }

            comm.sendNormalServerMessage(target.getNameWithGenus() + " targets " + targstring + ", and fights " + opostring);
            target.getCommunicator().sendAlertServerMessage(performer.getNameWithGenus() + " evaluates you.");
            if (performer.getCultist().getLevel() > 8) {
               Skills skills = target.getSkills();

               for(Skill skill : skills.getSkills()) {
                  if (skill.affinity > 0) {
                     comm.sendNormalServerMessage("Affinity in skill " + skill.getName());
                  }
               }

               comm.sendNormalServerMessage(
                  target.getHeSheItString()
                     + " is carrying "
                     + target.getInventory().getFullWeight() / 1000
                     + " kgs in inventory and "
                     + target.getBody().getBodyItem().getFullWeight() / 1000
                     + " kgs equipped."
               );
               if (target.isPlayer()) {
                  comm.sendNormalServerMessage("Battle rank: " + ((Player)target).getRank());
               }
            }

            if (target.isNpcTrader()) {
               Shop shop = Economy.getEconomy().getShop(target);
               if (shop != null) {
                  comm.sendNormalServerMessage(
                     "Economic breakdown this period: Earned="
                        + shop.getMoneyEarnedMonth()
                        + ", spent="
                        + shop.getMoneySpentMonth()
                        + " Ratio="
                        + shop.getSellRatio()
                  );
                  comm.sendNormalServerMessage("Taxes paid=" + shop.getTaxPaid() + ", rate=" + shop.getTax());
               }
            }
         } else {
            int zid = -1;

            try {
               Zone z = Zones.getZone(target.getTileX(), target.getTileY(), true);
               zid = z.getId();
            } catch (NoSuchZoneException var22) {
            }

            comm.sendNormalServerMessage(
               target.getName()
                  + " posx="
                  + target.getPos3f()
                  + "(+"
                  + target.getAltOffZ()
                  + "), rot="
                  + target.getStatus().getRotation()
                  + " floor level "
                  + target.getFloorLevel()
            );
            comm.sendNormalServerMessage(
               target.getTilePos() + " surf=" + target.isOnSurface() + " fg=" + target.followsGround() + " og=" + target.getMovementScheme().onGround + "."
            );
            comm.sendNormalServerMessage("Stamina=" + target.getStatus().getStamina() + ", damage=" + target.getStatus().damage + ".");
            comm.sendNormalServerMessage("Normal stamina regen " + target.getStatus().hasNormalRegen() + ". Zonebonus: " + target.zoneBonus);
            comm.sendNormalServerMessage(
               "Hunger="
                  + target.getStatus().getHunger()
                  + ", fat="
                  + target.getStatus().fat
                  + ", nutrition="
                  + target.getStatus().getNutritionlevel()
                  + ", thirst="
                  + target.getStatus().getThirst()
                  + ", dominator="
                  + target.dominator
                  + " loyalty="
                  + target.getStatus().loyalty
                  + "."
            );
            String leader = "none";
            if (target.getLeader() != null) {
               leader = target.getLeader().getName();
            }

            String hitched = "none";
            if (target.getHitched() != null) {
               hitched = target.getHitched().getName();
            }

            comm.sendNormalServerMessage(
               "Model=" + target.getModelName() + ", Kingdom=" + Kingdoms.getNameFor(target.getKingdomId()) + " leader=" + leader + ", hitched=" + hitched
            );
            VisionArea visionArea = target.getVisionArea();
            VirtualZone surface = visionArea == null ? null : visionArea.getSurface();
            if (surface != null) {
               comm.sendNormalServerMessage(
                  "Watching (" + surface.getStartX() + "," + surface.getStartY() + ")-(" + surface.getEndX() + "," + surface.getEndY() + ")"
               );
            }

            if (target.getStatus().getPath() != null) {
               Path p = target.getStatus().getPath();

               for(PathTile pt : p.getPathTiles()) {
                  comm.sendNormalServerMessage("Pathing to " + pt.getTileX() + ", " + pt.getTileY());
               }
            }

            try {
               if (target.getCurrentAction() != null) {
                  comm.sendNormalServerMessage("Busy " + target.getCurrentAction().getActionString());
               }
            } catch (NoSuchActionException var21) {
               comm.sendNormalServerMessage("Not busy doing anything.");
            }

            String opostring = "none";
            if (target.opponent != null) {
               opostring = target.opponent.getName();
            }

            String targstring = "none";
            if (target.target > -10L) {
               try {
                  Creature c = Server.getInstance().getCreature(target.target);
                  targstring = c.getName();
               } catch (NoSuchCreatureException var19) {
                  targstring = " unknown creature. That is bad so setting to none.";
                  target.setTarget(-10L, true);
               } catch (NoSuchPlayerException var20) {
                  targstring = " unknown player. That is bad so setting to none.";
                  target.setTarget(-10L, true);
               }
            }

            comm.sendNormalServerMessage("Target=" + targstring + ", opponent=" + opostring);
            if (target.isNpcTrader()) {
               Shop shop = Economy.getEconomy().getShop(target);
               if (shop != null) {
                  comm.sendNormalServerMessage(
                     "Month spent="
                        + shop.getMoneySpentMonth()
                        + ", earned="
                        + shop.getMoneyEarnedMonth()
                        + " Life spent="
                        + shop.getMoneySpentLife()
                        + " earned="
                        + shop.getMoneyEarnedLife()
                        + " tax="
                        + shop.getTax()
                        + " earned="
                        + shop.getTaxPaid()
                  );
                  if (shop.isPersonal()) {
                     Item[] items = target.getInventory().getAllItems(false);

                     for(int x = 0; x < items.length; ++x) {
                        String lname = String.valueOf(items[x].lastOwner);
                        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(items[x].lastOwner);
                        if (pinf != null) {
                           lname = pinf.getName();
                        }

                        comm.sendNormalServerMessage(
                           items[x].getName()
                              + " ["
                              + items[x].getDescription()
                              + "] "
                              + items[x].getQualityLevel()
                              + ": "
                              + Economy.getEconomy().getChangeFor((long)items[x].getPrice()).getChangeShortString()
                              + " last:"
                              + lname
                        );
                     }
                  }
               }
            }

            comm.sendNormalServerMessage("Target=" + targstring + ", opponent=" + opostring);
            if (performer.getPower() >= 5) {
               comm.sendNormalServerMessage("Wurmid=" + target.getWurmId() + ", zoneid=" + zid);
            }
         }
      }
   }

   private static void handle_EXAMINE(@Nonnull Creature performer, Creature target) {
      if (performer.getPower() >= 3) {
         Skills skills = target.getSkills();

         for(Skill skill : skills.getSkills()) {
            performer.getCommunicator().sendNormalServerMessage(skill.getName() + ": " + skill.getKnowledge() + " *:" + skill.affinity);
         }
      }

      if (target.isPlayer()) {
         String kingdom = "a Jenn-Kellon.";
         if (target.getKingdomId() == 0) {
            kingdom = "not allied to anyone.";
         } else if (target.getKingdomId() == 2) {
            kingdom = "a Mol Rehan.";
         } else if (target.getKingdomId() == 3) {
            kingdom = "with the Horde of the Summoned.";
         } else if (target.getKingdomId() == 4) {
            kingdom = "from the Freedom Isles.";
         } else if (target.getKingdomId() != 0) {
            kingdom = "a " + Kingdoms.getNameFor(target.getKingdomId());
         }

         if (performer.isVisible()) {
            target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " takes a long good look at you.");
         }

         boolean send = true;
         if (target.isKing()) {
            King k = King.getKing(target.getKingdomId());
            if (k != null) {
               performer.getCommunicator().sendNormalServerMessage("You are looking at " + k.getFullTitle() + ".");
               send = false;
            } else {
               logger.log(Level.WARNING, target.getName() + " is king but there is none for " + target.getKingdomId() + "?");
            }
         }

         if (target.getPower() <= 0) {
            if (send) {
               performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is " + kingdom);
            }
         } else if (target.getPower() == 1) {
            performer.getCommunicator().sendNormalServerMessage("This person is a hero among the living.");
         } else if (target.getPower() == 2) {
            performer.getCommunicator()
               .sendNormalServerMessage(target.getName() + " strikes you with " + target.getHisHerItsString() + " splendor! A demigod!");
         } else if (target.getPower() == 3) {
            performer.getCommunicator()
               .sendNormalServerMessage("Your eyes hurt as you look at " + target.getName() + "! The presence of a high god awes you.");
         } else if (target.getPower() == 4) {
            performer.getCommunicator()
               .sendNormalServerMessage("Looking at " + target.getName() + " is almost unbearable, and you hear beautiful songs in your mind! An arch angel!");
         } else if (target.getPower() == 5) {
            performer.getCommunicator()
               .sendNormalServerMessage("You have met your maker. " + target.getName() + " is an implementor, with powers beyond reason.");
         }

         String blood = "no discernable bloodline.";
         switch(target.getBlood()) {
            case 1:
               blood = "blood from the Horde.";
               break;
            case 2:
               blood = "blood from the Freedom Isles.";
               break;
            case 3:
            case 5:
            case 6:
            case 7:
            default:
               blood = "no discernable bloodline.";
               break;
            case 4:
               blood = "blood from Jenn-Kellon.";
               break;
            case 8:
               blood = "blood from Mol Rehan.";
         }

         performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getHeSheItString()) + " seems to have " + blood);
         String appointments = target.getAppointmentTitles();
         if (appointments.length() > 0) {
            performer.getCommunicator().sendNormalServerMessage(appointments);
         }

         if (target.getAlignment() < -20.0F && performer.getAlignment() > 20.0F) {
            performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getHeSheItString() + " radiates an unsettling aura."));
         } else if (performer.getAlignment() < -20.0F && target.getAlignment() > 20.0F) {
            performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getHeSheItString() + " radiates an unsettling aura."));
         }

         if (performer.getPower() > 0) {
            performer.getCommunicator().sendNormalServerMessage("Reputation: " + target.getReputation() + " Alignment: " + target.getAlignment());
         }

         performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getStatus().getBodyType()));
      } else {
         String exa = target.examine();
         if (target.isNpcTrader()) {
            Shop shop = Economy.getEconomy().getShop(target);
            long owner = shop.getOwnerId();
            if (owner > 0L) {
               try {
                  String name = Players.getInstance().getNameFor(owner);
                  exa = target.getName() + " is here selling items on behalf of " + name + ".";
               } catch (NoSuchPlayerException var15) {
                  exa = target.getName() + " is here selling items.";
               } catch (IOException var16) {
                  logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                  exa = target.getName() + " is here selling items.";
               }
            } else {
               exa = target.getName() + " is here selling items.";
            }
         }

         performer.getCommunicator().sendNormalServerMessage(exa);
         Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
         if (brand != null) {
            try {
               Village v = Villages.getVillage((int)brand.getBrandId());
               performer.getCommunicator().sendNormalServerMessage("It has been branded by and belongs to the settlement of " + v.getName() + ".");
            } catch (NoSuchVillageException var14) {
               brand.deleteBrand();
            }
         }

         if (target.isCaredFor()) {
            long careTaker = target.getCareTakerId();
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(careTaker);
            if (info != null) {
               performer.getCommunicator().sendNormalServerMessage("It is being taken care of by " + info.getName() + ".");
            } else if (System.currentTimeMillis() - Players.getInstance().getLastLogoutForPlayer(careTaker) > 1209600000L) {
               Creatures.getInstance().setCreatureProtected(target, -10L, false);
            }
         }

         performer.getCommunicator().sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getStatus().getBodyType()));
         if (target.isDominated() && target.isAnimal()) {
            float loy = target.getLoyalty();
            if (loy < 10.0F) {
               exa = target.getNameWithGenus() + " looks upset.";
            } else if (loy < 20.0F) {
               exa = target.getNameWithGenus() + " acts nervously.";
            } else if (loy < 30.0F) {
               exa = target.getNameWithGenus() + " looks submissive.";
            } else if (loy < 40.0F) {
               exa = target.getNameWithGenus() + " looks calm.";
            } else if (loy < 50.0F) {
               exa = target.getNameWithGenus() + " looks tame.";
            } else if (loy < 60.0F) {
               exa = target.getNameWithGenus() + " acts loyal.";
            } else if (loy < 70.0F) {
               exa = target.getNameWithGenus() + " looks trusting.";
            } else if (loy < 100.0F) {
               exa = target.getNameWithGenus() + " looks extremely loyal.";
            }

            performer.getCommunicator().sendNormalServerMessage(exa);
         }

         if (target.isDomestic() && System.currentTimeMillis() - target.getLastGroomed() > 172800000L) {
            performer.getCommunicator().sendNormalServerMessage("This creature could use some grooming.");
         }

         if (target.hasTraits()) {
            try {
               Skill breeding = performer.getSkills().getSkill(10085);
               double knowl = breeding.getKnowledge(0.0);
               if (knowl > 20.0) {
                  StringBuilder buf = new StringBuilder();

                  for(int x = 0; x < 64; ++x) {
                     if (target.hasTrait(x) && knowl - 20.0 > (double)x) {
                        String l = Traits.getTraitString(x);
                        if (l.length() > 0) {
                           buf.append(l);
                           buf.append(' ');
                        }
                     }
                  }

                  if (buf.toString().length() > 0) {
                     performer.getCommunicator().sendNormalServerMessage(buf.toString());
                  }
               }
            } catch (NoSuchSkillException var17) {
            }
         }

         if (target.isHorse()) {
            performer.getCommunicator().sendNormalServerMessage("Its colour is " + target.getColourName() + ".");
         }

         if (target.isPregnant()) {
            Offspring o = target.getOffspring();
            Random rand = new Random(target.getWurmId());
            int left = o.getDaysLeft() + rand.nextInt(3);
            performer.getCommunicator()
               .sendNormalServerMessage(
                  LoginHandler.raiseFirstLetter(target.getHeSheItString()) + " will deliver in about " + left + (left != 1 ? " days." : " day.")
               );
         }
      }

      String motherfather = "";
      if (target.getMother() != -10L) {
         try {
            Creature mother = Server.getInstance().getCreature(target.getMother());
            motherfather = motherfather + StringUtilities.raiseFirstLetter(target.getHisHerItsString()) + " mother is " + mother.getNameWithGenus() + ". ";
         } catch (NoSuchCreatureException var12) {
         } catch (NoSuchPlayerException var13) {
         }
      }

      if (target.getFather() != -10L) {
         try {
            Creature father = Server.getInstance().getCreature(target.getFather());
            motherfather = motherfather + StringUtilities.raiseFirstLetter(target.getHisHerItsString()) + " father is " + father.getNameWithGenus() + ". ";
         } catch (NoSuchCreatureException var10) {
         } catch (NoSuchPlayerException var11) {
         }
      }

      if (motherfather.length() > 0) {
         performer.getCommunicator().sendNormalServerMessage(motherfather);
      }

      Village vill = target.getCitizenVillage();
      if (vill != null) {
         Citizen citiz = vill.getCitizen(target.getWurmId());
         if (citiz != null) {
            VillageRole role = citiz.getRole();
            String cit = StringUtilities.raiseFirstLetter(target.getHeSheItString())
               + " bears the mark of a "
               + role.getName()
               + " of "
               + vill.getName()
               + ".";
            performer.getCommunicator().sendNormalServerMessage(cit);
            PvPAlliance alliance = PvPAlliance.getPvPAlliance(vill.getAllianceNumber());
            if (alliance != null) {
               performer.getCommunicator().sendNormalServerMessage(vill.getName() + " is in the alliance named " + alliance.getName() + ".");
            }
         } else {
            logger.log(Level.WARNING, target.getName() + " with id " + target.getWurmId() + " no citizen role for village " + vill.getName());
         }
      }

      if (target.isChampion()) {
         performer.getCommunicator()
            .sendNormalServerMessage(StringUtilities.raiseFirstLetter(target.getHeSheItString()) + " is a Champion of " + target.getDeity().name + ".");
      }

      if (performer.opponent == target) {
         if (CombatHandler.isDefend(target.getCombatHandler().getCurrentStance())) {
            performer.getCommunicator().sendNormalServerMessage(target.getHeSheItString() + " is in defensive stance.");
         } else {
            String desc = CombatHandler.getStanceDescription(target.getCombatHandler().getCurrentStance());
            performer.getCommunicator()
               .sendNormalServerMessage(LoginHandler.raiseFirstLetter(target.getHeSheItString()) + " is targetting your " + desc + "parts.");
            performer.getCommunicator()
               .sendCombatNormalMessage(LoginHandler.raiseFirstLetter(target.getHeSheItString()) + " is targetting your " + desc + "parts.");
         }
      }

      if ((performer.getKingdomId() == target.getKingdomId() || target.getAttitude(performer) == 1 || target.getAttitude(performer) == 0)
         && target.getSpellEffects() != null) {
         SpellEffect[] effs = target.getSpellEffects().getEffects();

         for(SpellEffect eff : effs) {
            performer.getCommunicator()
               .sendNormalServerMessage(String.format("%s has been cast on it, so it has %s [%d]", eff.getName(), eff.getLongDesc(), (int)eff.getPower()));
         }
      }
   }

   private boolean handleSpell(@Nonnull Action act, @Nonnull Creature performer, Creature target, short action, float counter) {
      boolean done = true;
      Spell spell = Spells.getSpell(action);
      if (spell != null) {
         if (spell.offensive) {
            if (!Servers.isThisAPvpServer()) {
               Village bVill = target.getBrandVillage();
               if (bVill != null) {
                  if (spell.number != 275 && spell.number != 274) {
                     if (!bVill.mayAttack(performer, target)) {
                        performer.getCommunicator()
                           .sendNormalServerMessage(target.getNameWithGenus() + " seems to be protected by an aura from its branding.");
                        return true;
                     }
                  } else if (!bVill.isActionAllowed((short)46, performer)) {
                     performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " rolls its eyes and looks too nervous to focus.");
                     return true;
                  }
               }
            }

            if (target.isInvulnerable()) {
               performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " seems to absorb the spell with no effect.");
               return true;
            }

            if (!performer.mayAttack(target)) {
               if (!performer.isStunned() && !performer.isUnconscious()) {
                  performer.getCommunicator().sendNormalServerMessage("You are too weak to attack.");
                  return true;
               }

               return true;
            }
         }

         if (spell.religious) {
            if (performer.getDeity() != null) {
               if ((spell.number == 275 || spell.number == 274) && !Methods.isActionAllowed(performer, action)) {
                  return true;
               }

               if (Methods.isActionAllowed(performer, (short)245)) {
                  done = Methods.castSpell(performer, spell, target, counter);
               }
            } else {
               performer.getCommunicator().sendNormalServerMessage("You have no deity and cannot cast the spell.");
            }
         } else if (Methods.isActionAllowed(performer, (short)547)) {
            done = Methods.castSpell(performer, spell, target, counter);
         }
      }

      return done;
   }

   static String getAnimationNameForStanceChange(byte newStance) {
      StringBuilder sb = new StringBuilder();
      if (CombatHandler.isDefend(newStance)) {
         sb.append("defend");
      } else if (newStance == 0) {
         sb.append("ready");
      }

      if (CombatHandler.isHigh(newStance)) {
         sb.append("high");
      } else if (CombatHandler.isLow(newStance)) {
         sb.append("low");
      }

      if (CombatHandler.isLeft(newStance)) {
         sb.append("left");
      } else if (CombatHandler.isRight(newStance)) {
         sb.append("right");
      }

      return sb.toString();
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Creature target, short action, float counter) {
      boolean done = false;
      int stid = source.getTemplateId();
      switch(action) {
         case 31:
            done = true;
            if (target.isTrader()
               && !target.isPlayer()
               && (!Servers.localServer.LOGINSERVER || Servers.localServer.testServer)
               && target.getFloorLevel() == performer.getFloorLevel()
               && performer.getKingdomId() == target.getKingdomId()
               && (target.getCurrentVillage() == null || !target.getCurrentVillage().isEnemy(performer))
               && performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), 0, 2)) {
               done = Methods.discardSellItem(performer, act, source, counter);
            }
            break;
         case 34:
            done = MethodsCreatures.findCaveExit(target, performer);
            break;
         case 46:
            if (source.isEdibleBy(target)) {
               if (stid == 272) {
                  done = true;
                  performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " seems uninterested in the " + source.getName() + ".");
               } else if (target.isDominatable(performer) && target.isAnimal()) {
                  done = MethodsCreatures.tame(act, performer, target, source, counter);
               } else {
                  done = true;
               }
            } else {
               done = true;
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " seems uninterested in the " + source.getName() + ".");
            }
            break;
         case 47:
            done = MissionTriggers.activateTriggers(performer, source, action, target.getWurmId(), act.currentSecond());
            if (!source.deleted && performer.getPower() >= 2 && !source.isNoDrop()) {
               source.putInVoid();
               target.getInventory().insertItem(source, true);
               if (target.isHuman() || target.getModelName().contains("humanoid")) {
                  target.wearItems();
               }
            }
            break;
         case 81:
            done = true;
            Methods.sendAllianceQuestion(performer, target);
            break;
         case 85:
            done = true;
            if (performer.getPower() >= 2 && stid == 300) {
               Shop shop = Economy.getEconomy().getShop(target);
               if (shop == null || !shop.isPersonal()) {
                  performer.getCommunicator().sendSafeServerMessage("The merchant has no shop. Weird.");
               } else if (performer.getPower() > 0) {
                  if (source.getData() >= 0L) {
                     performer.getCommunicator().sendSafeServerMessage("The contract already manages a merchant.");
                  } else {
                     source.setData(target.getWurmId());
                     performer.getCommunicator().sendSafeServerMessage("The contract will now manage " + target.getName() + ".");
                  }
               }
            }
            break;
         case 88:
            if (target.isFish() && stid == 176) {
               Methods.sendSetDataQuestion(performer, target);
            }

            done = true;
            break;
         case 92:
            done = true;
            if ((performer.getPower() >= 4 || Servers.localServer.testServer && performer.getPower() >= 3) && (stid == 176 || stid == 315)) {
               Methods.sendLearnSkillQuestion(performer, source, target.getWurmId());
            }
            break;
         case 106:
            done = true;
            if (target.getLeader() == null && target.isLeadable(performer) && (source.isLeadCreature() || target.hasBridle())) {
               float tstZ = target.getStatus().getPositionZ() + target.getAltOffZ();
               if (!performer.isWithinTileDistanceTo(target.getTileX(), target.getTileY(), CoordUtils.WorldToTile(tstZ), 1)) {
                  performer.getCommunicator().sendNormalServerMessage("You are too far away to lead " + target.getNameWithGenus());
               } else if (!performer.mayLeadMoreCreatures()) {
                  performer.getCommunicator().sendNormalServerMessage("You would get nowhere if you tried to lead more creatures.");
               } else {
                  if (performer.getBridgeId() == -10L && target.getBridgeId() == -10L && performer.getFloorLevel(true) != target.getFloorLevel()) {
                     performer.getCommunicator().sendNormalServerMessage("You must be on the same floor level to lead.");
                     return true;
                  }

                  boolean lastLed = target.isBranded() ? false : Creatures.getInstance().wasLastLed(performer.getWurmId(), target.getWurmId());
                  if (performer.getVehicle() > -10L && !performer.isVehicleCommander()) {
                     if (Vehicles.getVehicleForId(performer.getVehicle()).isChair()) {
                        performer.getCommunicator().sendNormalServerMessage("You can't lead while sitting.");
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You can't lead as a passenger.");
                     }
                  } else if (!target.mayLead(performer) && !lastLed && !Servers.isThisAPvpServer()) {
                     performer.getCommunicator().sendNormalServerMessage("You don't have permission to lead.");
                  } else {
                     boolean canItemLead = !performer.isItemLeading(source) || source.isLeadMultipleCreatures();
                     if (!canItemLead && !target.hasBridle()) {
                        Creature follower = performer.getFollowedCreature(source);
                        if (follower != null) {
                           performer.getCommunicator().sendNormalServerMessage("You are already using that item to lead " + follower.getNameWithGenus());
                        } else {
                           performer.getCommunicator().sendNormalServerMessage("That item is already used to lead a creature.");
                        }
                     } else {
                        target.setLeader(performer);
                        if (source != null && source.isRope() && !target.hasBridle()) {
                           performer.addFollower(target, source);
                        } else {
                           performer.addFollower(target, null);
                        }

                        if (target.getVisionArea() != null) {
                           target.getVisionArea().broadCastUpdateSelectBar(target.getWurmId());
                        }

                        if (performer.getFollowers().length > 2) {
                           performer.achievement(136);
                           if (performer.getVehicle() != -10L) {
                              Vehicle vehicle = Vehicles.getVehicleForId(performer.getVehicle());
                              if (vehicle != null && vehicle.creature) {
                                 boolean award = true;

                                 for(Creature c : performer.getFollowers()) {
                                    int cid = c.getTemplate().getTemplateId();
                                    if (cid != 82 && cid != 3 && cid != 49 && cid != 50) {
                                       award = false;
                                    }
                                 }

                                 if (award) {
                                    performer.achievement(137);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
            break;
         case 115:
            if (performer.isPriest() && performer.getDeity() == target.getDeity()) {
               done = MethodsReligion.listen(performer, target, act, counter);
            }
            break;
         case 118:
            done = true;
            if (source.getOwnerId() == performer.getWurmId()) {
               if (source.isArtifact() && !ArtifactBehaviour.mayUseItem(source, performer)) {
                  performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " emits no sense of power right now.");
               } else if (stid == 330) {
                  done = false;
                  if (counter == 1.0F) {
                     performer.sendActionControl("dominating", true, 100);
                     act.setTimeLeft(100);
                  } else if (!(counter < 10.0F)) {
                     done = true;
                     if (!performer.isWithinDistanceTo(target, 8.0F)) {
                        performer.getCommunicator().sendNormalServerMessage("You need to be closer to the " + target.getName() + " in order to control it.");
                     } else if (!Dominate.mayDominate(performer, target)) {
                        performer.getCommunicator().sendNormalServerMessage("Nothing happens.");
                     } else {
                        float f = 1.0F;

                        try {
                           f = (float)target.getSkills().getSkill(105).getKnowledge(0.0);
                        } catch (NoSuchSkillException var19) {
                           target.getSkills().learn(105, 1.0F);
                        }

                        if (!((float)Server.rand.nextInt(100) > f) && performer.getPower() < 5) {
                           source.setData(WurmCalendar.currentTime - 691200L + 1800L);
                           performer.getCommunicator()
                              .sendNormalServerMessage("The " + target.getName() + " resists your attempt to dominate " + target.getHimHerItString() + ".");
                        } else {
                           Server.getInstance()
                              .broadCastAction(performer.getName() + " uses " + performer.getHisHerItsString() + " " + source.getName() + "!", performer, 5);
                           Dominate.dominate(50.0, performer, target);
                           if (performer.getPower() < 5) {
                              source.setData(WurmCalendar.currentTime);
                           }
                        }

                        source.setAuxData((byte)(source.getAuxData() - 1));
                     }
                  }
               } else if (stid == 331) {
                  if (!target.isDominatable(performer) || !target.isAnimal()) {
                     performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " does not seem subjective to the effect.");
                  } else if (!performer.isWithinDistanceTo(target, 8.0F)) {
                     performer.getCommunicator().sendNormalServerMessage("You need to be closer to the " + target.getName() + " in order to control it.");
                     done = true;
                  } else {
                     Server.getInstance()
                        .broadCastAction(performer.getName() + " uses " + performer.getHisHerItsString() + " " + source.getName() + "!", performer, 5);
                     MethodsCreatures.tameEffect(performer, target, 50.0, false, 50.0);
                     source.setData(WurmCalendar.currentTime);
                  }
               } else if (stid == 334) {
                  target.getStatus().modifyStamina2(100.0F);
                  target.getStatus().refresh(0.99F, true);
                  Server.getInstance()
                     .broadCastAction(performer.getNameWithGenus() + " uses " + performer.getHisHerItsString() + " " + source.getName() + "!", performer, 5);
                  performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is now refreshed.");
                  target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " refreshes you.");
                  source.setAuxData((byte)(source.getAuxData() - 1));
                  source.setData(WurmCalendar.currentTime);
               }
            }
            break;
         case 142:
            done = true;
            if (stid != 792) {
               performer.getCommunicator().sendNormalServerMessage("You need to use a special ceremonial knife to do this.");
            } else if (!target.isUnique()
               && !target.isUndead()
               && !target.isReborn()
               && target.getHitched() == null
               && !target.isNpc()
               && !target.isRidden()
               && (Servers.isThisAPvpServer() || !target.isDominated() && !target.isBranded())) {
               if (!Methods.isActionAllowed(performer, action, target.getTileX(), target.getTileY())) {
                  performer.getCommunicator().sendNormalServerMessage("You need permission to do this.");
               } else {
                  done = MethodsReligion.sacrifice(performer, target, source, act, counter);
               }
            }
            break;
         case 179:
            done = true;
            handle_SUMMON(performer, target, stid);
            break;
         case 185:
            if (source.getTemplateId() == 176 && performer.getPower() > 0 && source.getAuxData() == 1) {
               String positionText = StringUtil.format("TileX: %d TileY: %d, id: %d", target.getTileX(), target.getTileY(), target.getWurmId());
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     positionText
                        + " pathing="
                        + target.isPathing()
                        + " has path="
                        + (target.getStatus().getPath() != null)
                        + " should stand still="
                        + target.shouldStandStill
                        + " leader="
                        + target.leader
                  );
               done = true;
            } else {
               done = this.action(act, performer, target, action, counter);
            }
            break;
         case 213:
            done = true;
            if (target.isGuide()) {
               if (performer.getKingdomId() != target.getKingdomId()) {
                  if (performer.isChampion()) {
                     performer.getCommunicator().sendNormalServerMessage("You are a champion and may not change kingdom.");
                  } else {
                     MethodsCreatures.sendAskKingdomQuestion(target, performer);
                  }
               }
            } else if (!target.acceptsInvitations()) {
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " must issue the command '/invitations' to start accepting converting.");
            } else {
               if (target.isInvulnerable()) {
                  performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " must not be invulnerable!");
                  return true;
               }

               try {
                  target.getCurrentAction();
                  performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is too busy right now.");
               } catch (NoSuchActionException var20) {
                  MethodsReligion.sendAskConvertQuestion(performer, target, source);
               }
            }
            break;
         case 216:
            done = MethodsReligion.preach(performer, target, source, counter);
            break;
         case 230:
            done = true;
            if (!target.isNeedFood()) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " refuses to be fed.");
            } else if (!target.canEat()) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " does not seem interested in food right now.");
            } else if (!source.isEdibleBy(target)) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " seems uninterested in the " + source.getName() + ".");
            } else {
               target.eat(source);
               performer.achievement(166);
               if (!target.canEat()) {
                  performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " does not seem hungry any longer.");
                  if (performer.getPet() != null && performer.getPet() == target) {
                     target.modifyLoyalty(1.0F);
                     performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " seems pleased with you.");
                  }
               }
            }
            break;
         case 244:
            done = true;
            if (performer.getPower() <= 1) {
               performer.getCommunicator().sendNormalServerMessage("This option is currently unavailable to your level.");
            } else if (stid == 176 || stid == 315) {
               Methods.sendServerManagementQuestion(performer, target.getWurmId());
            }
            break;
         case 286:
            done = true;
            handle_BECOME_PRIEST(performer, target);
            break;
         case 329:
            if (stid != 489 && (stid != 176 && stid != 315 || performer.getPower() < 2)) {
               done = this.action(act, performer, target, action, counter);
            } else {
               done = MethodsItems.watchSpyglass(performer, source, act, counter);
            }
            break;
         case 342:
            done = true;
            if (source.isComponentItem() || source.getTemplateId() == 1392) {
               performer.getCommunicator().sendNormalServerMessage("You cannot throw that.");
            } else if (target.isInvulnerable() && performer.getPower() < 5) {
               performer.getCommunicator()
                  .sendNormalServerMessage(target.getNameWithGenus() + " is protected by the gods. You may not attack " + target.getHimHerItString() + ".");
            } else if (performer.mayAttack(target)) {
               done = Archery.throwItem(performer, target, source, act);
            } else if (performer.isGuest()) {
               performer.getCommunicator().sendNormalServerMessage("Guests may not attack.");
            } else if (!performer.isStunned() && !performer.isUnconscious()) {
               performer.getCommunicator().sendNormalServerMessage("You are too inexperienced to start attacking anyone.");
            }
            break;
         case 345:
            if (target.isMilkable()) {
               done = MethodsCreatures.milk(act, source, performer, target, counter);
            } else {
               done = true;
               performer.getCommunicator().sendNormalServerMessage("You can't milk that!");
               logger.log(Level.WARNING, performer.getName() + " tried to milk " + target.getName());
            }
            break;
         case 346:
            done = true;
            if ((stid == 176 || stid == 315) && performer.getPower() >= 2) {
               try {
                  Wound[] wounds = target.getBody().getWounds().getWounds();

                  for(Wound lWound : wounds) {
                     performer.getCommunicator().sendNormalServerMessage("Healing " + lWound.getDescription());
                     lWound.heal();
                  }

                  if (performer.getLogger() != null) {
                     performer.getLogger().log(Level.INFO, performer.getName() + " healing " + target.getName());
                  }
               } catch (Exception var23) {
                  logger.log(Level.WARNING, var23.getMessage(), (Throwable)var23);
               }
            }
            break;
         case 352:
            if (performer.getPower() < 2) {
               done = this.action(act, performer, target, action, counter);
            } else {
               done = true;
               if (target.loggerCreature1 == -10L) {
                  target.loggerCreature1 = performer.getWurmId();
                  performer.getCommunicator().sendSafeServerMessage("You now log " + target.getName());
                  performer.getLogger().log(Level.INFO, "Started logging " + target.getName());
               } else if (target.loggerCreature1 == performer.getWurmId()) {
                  target.loggerCreature1 = -10L;
                  performer.getCommunicator().sendSafeServerMessage("You no longer log " + target.getName());
                  performer.getLogger().log(Level.INFO, "Stopped logging " + target.getName());
               }
            }
            break;
         case 354:
            done = true;
            handle_APPOINT(performer, source, stid);
            break;
         case 387:
            done = true;
            handle_LIGHT_PATH(performer, source, target);
            break;
         case 397:
            done = true;
            if (source.isPuppet()) {
               boolean found = false;

               try {
                  Item heldRight = target.getEquippedItem((byte)38);
                  if (heldRight != null) {
                     found = true;
                     done = MethodsItems.puppetSpeak(performer, source, heldRight, act, counter);
                  }
               } catch (NoSpaceException var22) {
               }

               if (!found) {
                  try {
                     Item heldLeft = target.getEquippedItem((byte)37);
                     if (heldLeft != null) {
                        done = MethodsItems.puppetSpeak(performer, source, heldLeft, act, counter);
                        found = true;
                     }
                  } catch (NoSpaceException var21) {
                  }
               }

               if (!found) {
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " is not holding a puppet.");
               }
            }
            break;
         case 398:
            done = true;
            if (target.isDomestic() && stid == 647 && source.getOwnerId() == performer.getWurmId()) {
               done = MethodsCreatures.groom(performer, target, source, action, act, counter);
            }
            break;
         case 399:
            done = true;
            handle_MAGICLINK(performer, target);
            break;
         case 472:
            done = true;
            if (stid == 676 && source.getOwnerId() == performer.getWurmId()) {
               MissionManager m = new MissionManager(performer, "Manage missions", "Select action", target.getWurmId(), target.getName(), source.getWurmId());
               m.sendQuestion();
            }
            break;
         case 484:
            if (stid == 701) {
               done = MethodsCreatures.brand(performer, target, source, act, counter);
            } else if (performer.getPower() >= 4 && stid == 176) {
               Brand brand = Creatures.getInstance().getBrand(target.getWurmId());
               if (brand != null) {
                  logger.log(Level.INFO, "Deleting brand for " + target.getName() + " by Arch Command.");
                  brand.deleteBrand();
                  performer.getCommunicator().sendNormalServerMessage("You filled in the old brand!");
               }

               done = true;
            } else {
               done = this.action(act, performer, target, action, counter);
            }
            break;
         case 486:
            done = true;
            if (Servers.localServer.testServer && performer.getPower() > 1) {
               if (target.isChampion()) {
                  target.revertChamp();
               } else {
                  long wid = -10L;
                  EndGameItem altar = null;
                  EndGameItem goodAltar = EndGameItems.getGoodAltar();
                  EndGameItem evilAltar = EndGameItems.getEvilAltar();
                  if (goodAltar != null) {
                     altar = goodAltar;
                  }

                  if (performer.getDeity().number == 4 && evilAltar != null) {
                     altar = evilAltar;
                  }

                  if (altar != null) {
                     wid = altar.getWurmid();
                  }

                  RealDeathQuestion cq = new RealDeathQuestion(performer, "Real death", "Offer to become a Champion:", wid, performer.getDeity());
                  cq.sendQuestion();
                  target.becomeChamp();
               }
            }
            break;
         case 538:
            done = true;
            if (performer.getPower() >= 4 && !(target instanceof Player)) {
               Methods.sendGmSetTraitsQuestion(performer, target);
            }
            break;
         case 637:
            if (stid == 903) {
               done = MethodsSurveying.planBridge(act, performer, source, target, null, action, counter);
            } else {
               done = this.action(act, performer, target, action, counter);
            }
            break;
         case 640:
            if (stid == 903) {
               done = MethodsSurveying.survey(act, performer, source, target, null, action, counter);
            } else {
               done = this.action(act, performer, target, action, counter);
            }
            break;
         case 643:
            if (stid == 701) {
               done = MethodsCreatures.unbrand(performer, target, source, act, counter);
            } else {
               done = this.action(act, performer, target, action, counter);
            }
            break;
         case 646:
            if (target.isWoolProducer()) {
               done = MethodsCreatures.shear(act, source, performer, target, counter);
            } else {
               done = true;
            }
            break;
         case 698:
            done = true;
            Players.appointCA(performer, target.getName());
            break;
         case 699:
            done = true;
            Players.appointCM(performer, target.getName());
            break;
         case 700:
            done = true;
            Players.displayLCMInfo(performer, target.getName());
            break;
         case 721:
            done = true;
            if (performer.getPower() >= 4 && target instanceof Player) {
               Methods.sendGmSetMedpathQuestion(performer, target);
            }
            break;
         case 745:
            done = true;
            if (source.getTemplateId() == 1276
               || source.getTemplateId() == 833
               || source.getTemplateId() == 1258 && source.getRealTemplateId() == 1195
               || source.getTemplateId() == 1177 && source.getRealTemplateId() == 1195) {
               boolean caught = (double)(Server.rand.nextFloat() * 150.0F) < target.getBodyControl();
               new Arrows(
                  source,
                  performer,
                  target,
                  -1,
                  -1,
                  caught ? Arrows.ArrowHitting.NOT : Arrows.ArrowHitting.HIT,
                  caught ? Arrows.ArrowDestroy.DO_NOTHING : Arrows.ArrowDestroy.NORMAL,
                  null,
                  0.0,
                  1.0F,
                  0.0F,
                  (byte)0,
                  true,
                  0.0,
                  0.0
               );
               float dam = source.getDamage() + 10.0F;
               if (caught && dam < 100.0F && target.getInventory().mayCreatureInsertItem()) {
                  target.getCommunicator()
                     .sendNormalServerMessage(performer.getName() + " throws a " + source.getName() + " at you, but you deftly catch it.");
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " deftly catches the " + source.getName() + ".");
                  source.setDamage(dam);
                  target.getInventory().insertItem(source, false);
                  source.setBusy(false);
               } else {
                  String[] reactions = getPlayfulReactionString();
                  target.getCommunicator()
                     .sendNormalServerMessage("You " + reactions[1] + " as " + performer.getName() + " throws a " + source.getName() + " at you!");
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        target.getName() + " " + reactions[0] + " as you throw a " + source.getName() + " at " + target.getHimHerItString() + "!"
                     );
                  if (reactions[0].equals("falls over")) {
                     target.playAnimation("sprawl", false);
                  }

                  Items.destroyItem(source.getWurmId());
               }

               return done;
            }
            break;
         case 938:
            done = MethodsCreatures.catchFlies(performer, target, source, action, act, counter);
            break;
         case 945:
            if (source.getTemplate().isRune()
               && RuneUtilities.isSingleUseRune(source)
               && (
                  RuneUtilities.getSpellForRune(source) != null && RuneUtilities.getSpellForRune(source).isTargetCreature()
                     || RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_REFRESH) > 0.0F
                     || RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_CHANGE_AGE) > 0.0F
               )) {
               done = useRuneOnCreature(act, performer, source, target, action, counter);
            }
            break;
         default:
            if (Archery.isArchery(action)) {
               done = Archery.attack(performer, target, source, counter, act);
            } else if (act.isSpell()) {
               done = true;
               Spell spell = Spells.getSpell(action);
               if (spell != null) {
                  if (spell.offensive) {
                     if (!Servers.isThisAPvpServer()) {
                        Village bVill = target.getBrandVillage();
                        if (bVill != null) {
                           if (spell.number != 275 && spell.number != 274) {
                              if (!bVill.mayAttack(performer, target)) {
                                 performer.getCommunicator()
                                    .sendNormalServerMessage(target.getName() + " seems to be protected by an aura from its branding.");
                                 return true;
                              }
                           } else if (!bVill.isActionAllowed((short)46, performer)) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("The " + target.getName() + " rolls its eyes and looks too nervous to focus.");
                              return true;
                           }
                        }
                     }

                     if (target.isInvulnerable()) {
                        performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is protected by the gods.");
                        return true;
                     }

                     if (!performer.mayAttack(target)) {
                        if (!performer.isStunned() && !performer.isUnconscious()) {
                           performer.getCommunicator().sendNormalServerMessage("You are too weak to attack.");
                           return true;
                        }

                        return true;
                     }
                  }

                  if (spell.religious) {
                     if (performer.getDeity() != null) {
                        if (!performer.isSpellCaster() && !source.isHolyItem(performer.getDeity())) {
                           performer.getCommunicator().sendNormalServerMessage(performer.getDeity().name + " will not let you use that item.");
                        } else if (Methods.isActionAllowed(performer, (short)245)) {
                           if ((spell.number == 275 || spell.number == 274)
                              && !Methods.isActionAllowed(performer, action, target.getTileX(), target.getTileY())) {
                              return true;
                           }

                           done = Methods.castSpell(performer, spell, target, counter);
                        }
                     } else {
                        performer.getCommunicator().sendNormalServerMessage("You have no deity and cannot cast the spell.");
                     }
                  } else if (performer.isSpellCaster()
                     || source.isMagicStaff()
                     || source.getTemplateId() == 176 && performer.getPower() >= 2 && Servers.isThisATestServer()) {
                     if (Methods.isActionAllowed(performer, (short)547, target.getTileX(), target.getTileY())) {
                        done = Methods.castSpell(performer, spell, target, counter);
                     }
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You need to use a magic staff.");
                     done = true;
                  }
               } else {
                  logger.log(Level.INFO, performer.getName() + " tries to cast unknown spell:" + Actions.actionEntrys[action].getActionString());
                  performer.getCommunicator().sendNormalServerMessage("That spell is unknown.");
               }
            } else {
               done = this.action(act, performer, target, action, counter);
            }
      }

      return done;
   }

   private static void handle_BECOME_PRIEST(Creature performer, Creature target) {
      if (!performer.isPriest()) {
         performer.getCommunicator().sendNormalServerMessage("You must be a priest to make a priest!");
      } else if (!target.acceptsInvitations()) {
         performer.getCommunicator().sendNormalServerMessage(target.getName() + " must issue the command '/invitations' to start accepting converting.");
      } else if (target.getDeity() != performer.getDeity()) {
         performer.getCommunicator().sendNormalServerMessage("You must be of the same religion as " + target.getName() + ".");
      } else if (!Servers.localServer.PVPSERVER || Servers.localServer.testServer) {
         try {
            target.getCurrentAction();
            performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is too busy right now.");
         } catch (NoSuchActionException var3) {
            performer.getCommunicator()
               .sendNormalServerMessage("You ask " + target.getName() + " if " + target.getHeSheItString() + " wants to become a priest.");
            target.getCommunicator().sendNormalServerMessage(performer.getName() + " asks you if you want to become a priest.");
            Server.getInstance()
               .broadCastAction(
                  performer.getName() + " asks " + target.getName() + " if " + target.getHeSheItString() + " wants to become a priest.", performer, target, 5
               );
            MethodsCreatures.sendAskPriestQuestion(target, null, performer);
         }
      }
   }

   private static void handle_MAGICLINK(Creature performer, Creature target) {
      if (target.getDeity().getTemplateDeity() != performer.getDeity().getTemplateDeity()) {
         performer.getCommunicator().sendNormalServerMessage("You must be of the same religion as " + target.getName() + ".");
      } else if (performer.isFriendlyKingdom(target.getKingdomId())) {
         if (performer.isPriest() && target.isPriest()) {
            try {
               target.getCurrentAction();
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " is too busy right now.");
            } catch (NoSuchActionException var3) {
               if (target.isLinked()) {
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " is linked and can not use links himself.");
               } else if (target.getChannelingSkill().getKnowledge(0.0) / 10.0 > (double)target.getNumLinks()) {
                  Server.getInstance().broadCastAction(performer.getName() + " links with " + target.getName() + ".", performer, target, 5);
                  performer.setLinkedTo(target.getWurmId(), true);
                  performer.achievement(615);
               } else {
                  performer.getCommunicator().sendNormalServerMessage(target.getName() + " may not accept more links right now.");
               }
            }
         }
      }
   }

   private static void handle_LIGHT_PATH(Creature performer, Item source, Creature target) {
      if (source.isMeditation()) {
         Cultist perfCultist = Cultist.getCultist(performer.getWurmId());
         Communicator comm = performer.getCommunicator();
         if (Methods.isActionAllowed(performer, (short)384) && perfCultist != null && perfCultist.getLevel() > 4) {
            Cultist respCultist = Cultist.getCultist(target.getWurmId());
            String targetName = target.getName();
            if (respCultist.getPath() != perfCultist.getPath()) {
               comm.sendNormalServerMessage(targetName + " is not susceptible enlightenment from your path.");
            } else if (respCultist == null || respCultist.getLevel() <= 0) {
               comm.sendNormalServerMessage(targetName + " is not susceptible to enlightenment on any path.");
            } else if (perfCultist.getLevel() - respCultist.getLevel() != 3) {
               comm.sendNormalServerMessage(targetName + " is not susceptible to that enlightenment right now.");
            } else {
               long timeSinceLastEnlightAttempt = System.currentTimeMillis() - respCultist.getLastEnlightened();
               if (timeSinceLastEnlightAttempt <= 60000L) {
                  comm.sendNormalServerMessage(
                     targetName
                        + " has to ponder the path another "
                        + Server.getTimeFor(respCultist.getLastEnlightened() + 60000L - System.currentTimeMillis())
                        + "."
                  );
               } else {
                  long lastAppointedLevel = perfCultist.getLastAppointedLevel();
                  long timeSinceLast = System.currentTimeMillis() - lastAppointedLevel;
                  if (timeSinceLast <= 604800000L && performer.getPower() < 5) {
                     comm.sendNormalServerMessage(
                        "You may not light the path again until another "
                           + Server.getTimeFor(lastAppointedLevel + 604800000L - System.currentTimeMillis())
                           + " has passed."
                     );
                  } else {
                     if (timeSinceLast < 604800000L) {
                        comm.sendNormalServerMessage(
                           "You actually should not be able light the path again until another "
                              + Server.getTimeFor(lastAppointedLevel + 604800000L - System.currentTimeMillis())
                              + " has passed."
                        );
                     }

                     if (!target.acceptsInvitations()) {
                        comm.sendNormalServerMessage(targetName + " needs to type in the command /invitations.");
                     } else {
                        boolean mayIncreaseLevel = false;
                        long timeToNextLevel = respCultist.getTimeLeftToIncreasePath(
                           System.currentTimeMillis(), target.getSkills().getSkillOrLearn(10086).getKnowledge(0.0)
                        );
                        if (timeToNextLevel <= 0L) {
                           mayIncreaseLevel = true;
                        } else if (System.currentTimeMillis() - respCultist.getLastReceivedLevel() > 604800000L) {
                           mayIncreaseLevel = true;
                        } else if (performer.getPower() >= 5) {
                           comm.sendNormalServerMessage(
                              targetName
                                 + " next level="
                                 + Server.getTimeFor(timeToNextLevel)
                                 + ", last received="
                                 + Server.getTimeFor(System.currentTimeMillis() - respCultist.getLastReceivedLevel())
                                 + "."
                           );
                           mayIncreaseLevel = true;
                        } else {
                           comm.sendNormalServerMessage(
                              targetName
                                 + " must wait "
                                 + Server.getTimeFor(
                                    respCultist.getTimeLeftToIncreasePath(
                                       System.currentTimeMillis(), target.getSkills().getSkillOrLearn(10086).getKnowledge(0.0)
                                    )
                                 )
                                 + " until next enlightenment."
                           );
                        }

                        if (mayIncreaseLevel) {
                           comm.sendNormalServerMessage("You attempt to light the path for " + targetName + ".");
                           Skill meditation = null;

                           try {
                              meditation = target.getSkills().getSkill(10086);
                           } catch (NoSuchSkillException var21) {
                              comm.sendNormalServerMessage(targetName + " needs to at least learn how to meditate!");
                              return;
                           }

                           if (meditation != null) {
                              respCultist.setLastEnlightened(System.currentTimeMillis());
                              float diff = (float)respCultist.getLevel() * 20.0F;
                              if (meditation.skillCheck((double)diff, 0.0, true, 1.0F) <= 0.0) {
                                 comm.sendNormalServerMessage(
                                    targetName
                                       + " does not grasp the question. "
                                       + LoginHandler.raiseFirstLetter(target.getHeSheItString())
                                       + " needs to meditate more."
                                 );
                                 target.getCommunicator()
                                    .sendNormalServerMessage(
                                       performer.getName() + " asks you a very confusing question. You do not understand it and need to contemplate more."
                                    );
                              } else {
                                 comm.sendNormalServerMessage(
                                    "You ask "
                                       + targetName
                                       + " a question. "
                                       + LoginHandler.raiseFirstLetter(target.getHeSheItString())
                                       + " seems to think hard."
                                 );
                                 target.getCommunicator().sendNormalServerMessage(performer.getName() + " asks you a question.");
                                 CultQuestion cq = new CultQuestion(
                                    target,
                                    "Question asked by " + performer.getName(),
                                    "As follows:",
                                    target.getWurmId(),
                                    respCultist,
                                    respCultist.getPath(),
                                    false,
                                    false
                                 );
                                 cq.sendQuestion();
                                 perfCultist.setLastAppointedLevel(System.currentTimeMillis());

                                 try {
                                    perfCultist.saveCultist(false);
                                 } catch (IOException var20) {
                                    logger.log(Level.INFO, performer.getName() + " " + var20.getMessage(), (Throwable)var20);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } else {
            comm.sendNormalServerMessage("You are not able to give enlightenment yet.");
         }
      }
   }

   private static void handle_APPOINT(Creature performer, Item source, int stid) {
      if (source.isRoyal()) {
         if (stid == 535 || stid == 529 || stid == 532) {
            King k = King.getKing(performer.getKingdomId());

            assert k != null;

            if (k.kingid != performer.getWurmId()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You laugh at yourself - you who probably couldn't even appoint a cat to catch mice, now wielding a mighty sceptre! How preposterous!"
                  );
            } else {
               AppointmentsQuestion question = new AppointmentsQuestion(
                  performer, "Appointments", "Which appointments do you wish to do today?", source.getWurmId()
               );
               question.sendQuestion();
            }
         }
      }
   }

   private static void handle_SUMMON(Creature performer, Creature target, int stid) {
      if (performer.getPower() >= 2) {
         if (stid == 176 || stid == 315) {
            TilePos perfTile = performer.getTilePos();
            int tilex = perfTile.x;
            int tiley = perfTile.y;
            int layer = performer.getLayer();
            if (target instanceof Player) {
               String pName = target.getName();
               QuestionParser.summon(pName, performer, tilex, tiley, (byte)layer);
            } else {
               Server.getInstance().broadCastAction(target.getNameWithGenus() + " suddenly disappears.", target, 5);
               blinkTo(
                  target,
                  performer.getPosX(),
                  performer.getPosY(),
                  performer.getLayer(),
                  performer.getPositionZ(),
                  performer.getBridgeId(),
                  performer.getFloorLevel()
               );
               logger.log(
                  Level.INFO,
                  performer.getName() + " summoned creature " + target.getName() + ", with ID: " + target.getWurmId() + " at coords " + tilex + ',' + tiley
               );
               Server.getInstance().broadCastAction(target.getNameWithGenus() + " suddenly appears.", target, 5);
            }
         }
      }
   }

   public static final void blinkTo(Creature target, float posx, float posy, int layer, float posz, long bridgeId, int floorLevel) {
      target.getCurrentTile().deleteCreature(target);
      target.setPositionX(posx);
      target.setPositionY(posy);
      target.setLayer(layer, true);
      target.setPositionZ(posz);
      target.setBridgeId(bridgeId);
      target.getMovementScheme().setPosition(posx, posy, posz, target.getStatus().getRotation(), layer);
      target.getMovementScheme().setBridgeId(bridgeId);
      if (bridgeId == -10L) {
         target.pushToFloorLevel(floorLevel);
      }

      try {
         target.createVisionArea();
         Zones.getZone(target.getTileX(), target.getTileY(), target.isOnSurface()).addCreature(target.getWurmId());
      } catch (NoSuchZoneException var9) {
         logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
      } catch (NoSuchCreatureException var10) {
         logger.log(Level.WARNING, var10.getMessage(), (Throwable)var10);
      } catch (NoSuchPlayerException var11) {
         logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
      } catch (Exception var12) {
         logger.log(Level.WARNING, var12.getMessage(), (Throwable)var12);
      }
   }

   public static void setOpponent(Creature performer, Creature target, boolean done, Action act) {
      if (done) {
         performer.setOpponent(null);
      }

      if (!done && target.opponent == null) {
         try {
            if (target.getCurrentAction() != null && target.getCurrentAction().getPriority() < act.getPriority()) {
               target.getCurrentAction().stop(false);
            }
         } catch (NoSuchActionException var5) {
         }

         target.setOpponent(performer);
      }
   }

   static boolean moveCreature(Creature performer, Creature target, int counter, Action act) {
      String actString = "push";
      if (act.getNumber() == 181) {
         actString = "pull";
      }

      boolean toReturn = false;
      int time = 50;
      if (performer.getPower() > 0) {
         time = 10;
      }

      if (performer.isGuest()) {
         performer.getCommunicator().sendNormalServerMessage("Sorry, but we cannot allow our guests to push people around.");
         return true;
      } else if (performer.getStrengthSkill() < 21.0) {
         performer.getCommunicator().sendNormalServerMessage("You are too weak to " + actString + " " + target.getNameWithGenus() + " around.");
         return true;
      } else {
         if (counter == 1) {
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start to " + actString + " " + target.getNameWithGenus() + ".");
            Server.getInstance()
               .broadCastAction(performer.getNameWithGenus() + " starts to " + actString + " " + target.getNameWithGenus() + ".", performer, 5);
            performer.sendActionControl(actString + "ing " + target.getNameWithGenus(), true, time);
            if (performer.getPower() == 0) {
               performer.getStatus().modifyStamina(-1000.0F);
            }
         } else {
            time = act.getTimeLeft();
         }

         if (counter * 10 > time) {
            toReturn = true;
            performer.getStatus().modifyStamina(-250.0F);

            try {
               float dir = target.getStatus().getRotation();
               float iposx = target.getStatus().getPositionX();
               float iposy = target.getStatus().getPositionY();
               BlockingResult firstCheck = Blocking.getBlockerBetween(performer, target, 4);
               if (target.getPushCounter() != 0 && performer.getPower() < 2
                  || target.isTeleporting()
                  || target.getMovementScheme().isIntraTeleporting()
                  || target.getVehicle() != -10L
                  || target.isCantMove()
                  || target.isDead()
                  || firstCheck != null
                  || !target.isVisible()
                  || target.isInvulnerable()
                  || target.getPower() > performer.getPower()) {
                  performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " won't budge.");
                  return true;
               }

               float rot = performer.getStatus().getRotation();
               float xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * 1.0F;
               float yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * 1.0F;
               if (act.getNumber() == 181) {
                  double rotRads = Math.atan2((double)(performer.getStatus().getPositionY() - iposy), (double)(performer.getStatus().getPositionX() - iposx));
                  rot = (float)(rotRads * (180.0 / Math.PI)) + 90.0F;
                  xPosMod = (float)Math.sin((double)(rot * (float) (Math.PI / 180.0))) * 0.2F;
                  yPosMod = -((float)Math.cos((double)(rot * (float) (Math.PI / 180.0)))) * 0.2F;
               }

               float newPosX = iposx + xPosMod;
               float newPosY = iposy + yPosMod;
               float oldheight = Zones.calculateHeight(iposx, iposy, target.isOnSurface());
               float height = Zones.calculateHeight(newPosX, newPosY, target.isOnSurface());
               if (Math.abs(height - oldheight) > 1.0F) {
                  performer.getCommunicator().sendNormalServerMessage("You cannot " + actString + " " + target.getNameWithGenus() + " that high.");
                  return true;
               }

               BlockingResult result = Blocking.getBlockerBetween(
                  target,
                  iposx,
                  iposy,
                  newPosX,
                  newPosY,
                  target.getPositionZ(),
                  target.getPositionZ(),
                  target.isOnSurface(),
                  target.isOnSurface(),
                  false,
                  4,
                  -1L,
                  target.getBridgeId(),
                  target.getBridgeId(),
                  false
               );
               if (result != null && result.getFirstBlocker() != null) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You cannot " + actString + " " + target.getNameWithGenus() + " into the " + result.getFirstBlocker().getName() + "."
                     );
                  return true;
               }

               performer.getCommunicator().sendNormalServerMessage("You " + actString + " " + target.getNameWithGenus() + ".");
               Server.getInstance().broadCastAction(performer.getNameWithGenus() + " moves " + target.getNameWithGenus() + ".", performer, 5);
               target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " moves you around.");
               target.setPushCounter(200);
               if ((int)iposx >> 2 == (int)newPosX >> 2 && (int)iposy >> 2 == (int)newPosY >> 2) {
                  ((Player)target).intraTeleport(newPosX, newPosY, height, dir, target.getLayer(), actString + "ed by " + performer.getName());
               } else {
                  target.setTeleportPoints((short)((int)newPosX >> 2), (short)((int)newPosY >> 2), target.getLayer(), 0);
                  if (target.startTeleporting()) {
                     target.getCommunicator().sendTeleport(false);
                  }
               }
            } catch (NoSuchZoneException var19) {
               performer.getCommunicator()
                  .sendNormalServerMessage("You fail to " + actString + " " + target.getNameWithGenus() + ". " + target.getHeSheItString() + " must be stuck.");
               logger.log(Level.WARNING, performer.getName() + ": " + var19.getMessage(), (Throwable)var19);
            }

            toReturn = true;
         }

         return toReturn;
      }
   }

   private boolean mayDismissMerchant(Creature performer, Creature target) {
      Shop shop = Economy.getEconomy().getShop(target);
      if (shop.isPersonal()) {
         if (performer.getPower() >= 4 && Servers.isThisATestServer()) {
            return true;
         }

         if (performer.getCitizenVillage() != null
            && performer.getCitizenVillage() == target.getCurrentVillage()
            && performer.getCitizenVillage().getMayor().getId() == performer.getWurmId()) {
            if (shop.howLongEmpty() > 2419200000L) {
               return true;
            }

            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(shop.getOwnerId());
            if (pinf == null) {
               return true;
            }

            try {
               pinf.load();
               if (System.currentTimeMillis() - pinf.lastLogout > 2419200000L) {
                  return true;
               }
            } catch (IOException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         }
      }

      return false;
   }

   static boolean useRuneOnCreature(Action act, Creature performer, Item source, Creature target, short action, float counter) {
      if (RuneUtilities.isEnchantRune(source)) {
         performer.getCommunicator().sendNormalServerMessage("You cannot use the rune on that.", (byte)3);
         return true;
      } else {
         if (RuneUtilities.getSpellForRune(source) == null) {
            if (!Methods.isActionAllowed(performer, (short)384, target.getTileX(), target.getTileY())) {
               performer.getCommunicator().sendNormalServerMessage("You are not allowed to use that here.", (byte)3);
               return true;
            }
         } else if (RuneUtilities.isSingleUseRune(source) && RuneUtilities.getSpellForRune(source) != null) {
            if (!Methods.isActionAllowed(performer, (short)245, target.getTileX(), target.getTileY())) {
               performer.getCommunicator().sendNormalServerMessage("You are not allowed to use that here.", (byte)3);
               return true;
            }
         } else if (target.isPlayer() && RuneUtilities.getSpellForRune(source).number == 275) {
            performer.getCommunicator().sendNormalServerMessage(target.getName() + " is not very impressed by the rune.", (byte)3);
            return true;
         }

         if (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_CHANGE_AGE) > 0.0F) {
            if (target.isPlayer() || target.isNpc() || target.isNpcTrader()) {
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " is not very impressed by the rune.", (byte)3);
               return true;
            }

            if (target.isUnique()) {
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " crushes the rune before you have time to pull it away.");
               Items.destroyItem(source.getWurmId());
               return true;
            }

            if (target.getAttitude(performer) == 2) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     target.getName() + " ignores the rune as " + target.getHeSheItString() + " is more interested in attacking you.", (byte)3
                  );
               return true;
            }

            if (target.getStatus().age <= 12 || target.getTemplate().isBabyCreature()) {
               performer.getCommunicator().sendNormalServerMessage(target.getName() + " cannot become any younger.");
               return true;
            }

            if (target.isBranded() && !target.mayManage(performer)
               || target.isDominated() && target.getDominator() != performer
               || target.getLeader() != null && target.getLeader() != performer) {
               performer.getCommunicator().sendNormalServerMessage("You do not have permission to use the rune on " + target.getName() + ".");
               return true;
            }

            if (target.isRidden()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     target.getName()
                        + " is rather preoccupied right now. Maybe this will work better when "
                        + target.getHeSheItString()
                        + " is not being ridden."
                  );
               return true;
            }

            if (target.isHitched() && target.getHitched().isDragger(target)) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     target.getName() + " is rather preoccupied right now. Maybe this will work better when " + target.getHeSheItString() + " is not hitched."
                  );
               return true;
            }

            if (target.isPregnant()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "You decide against using the rune on " + target.getName() + " as you're unsure what it will do to the unborn baby."
                  );
               return true;
            }
         }

         int time = act.getTimeLeft();
         String targetName;
         if (performer == target) {
            if (target.getSex() == 0) {
               targetName = "himself";
            } else {
               targetName = "herself";
            }
         } else {
            targetName = "the " + target.getName();
         }

         if (counter == 1.0F) {
            String actionString = "use the rune on ";
            performer.getCommunicator().sendNormalServerMessage("You start to use the rune on " + target.getName() + ".");
            Server.getInstance()
               .broadCastAction(performer.getNameWithGenus() + " starts using " + source.getNameWithGenus() + " on " + targetName + ".", performer, 10);
            time = Actions.getSlowActionTime(performer, performer.getSoulDepth(), null, 0.0);
            act.setTimeLeft(time);
            performer.sendActionControl(act.getActionString(), true, act.getTimeLeft());
            performer.getStatus().modifyStamina(-600.0F);
         }

         if (act.currentSecond() % 5 == 0) {
            performer.getStatus().modifyStamina(-300.0F);
         }

         if (!(counter * 10.0F > (float)time)) {
            return false;
         } else {
            Skill soulDepth = performer.getSoulDepth();
            double diff = (double)(20.0F + source.getDamage()) - ((double)(source.getCurrentQualityLevel() + (float)source.getRarity()) - 45.0);
            double power = soulDepth.skillCheck(diff, (double)source.getCurrentQualityLevel(), false, counter);
            if (power > 0.0) {
               if (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_REFRESH) > 0.0F) {
                  target.getStatus().refresh(99.0F, true);
                  if (target != performer) {
                     performer.getCommunicator().sendNormalServerMessage(target.getNameWithGenus() + " is now refreshed.");
                     target.getCommunicator().sendNormalServerMessage(performer.getNameWithGenus() + " refreshes you.");
                  } else {
                     performer.getCommunicator().sendNormalServerMessage("You feel refreshed.");
                  }

                  performer.achievement(491);
               } else if (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_CHANGE_AGE) > 0.0F) {
                  int redux = Math.max(4, target.getStatus().age / 10);
                  int ageSum = Math.max(12, target.getStatus().age - redux);

                  try {
                     ((DbCreatureStatus)target.getStatus()).updateAge(ageSum);
                     target.refreshVisible();
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           target.getName()
                              + " looks slightly more youthful as the clock is rolled back "
                              + redux
                              + " months for "
                              + target.getHimHerItString()
                              + "."
                        );
                     performer.achievement(491);
                  } catch (IOException var16) {
                     performer.getCommunicator().sendNormalServerMessage("Something went wrong when using the rune. Try again a bit later.");
                     return true;
                  }
               } else {
                  if (RuneUtilities.getSpellForRune(source) == null || !RuneUtilities.getSpellForRune(source).isTargetCreature()) {
                     performer.getCommunicator().sendNormalServerMessage("You can't use the rune on that.", (byte)3);
                     return true;
                  }

                  RuneUtilities.getSpellForRune(source).castSpell(50.0, performer, target);
                  performer.achievement(491);
               }

               Server.getInstance()
                  .broadCastAction(performer.getNameWithGenus() + " successfully uses " + source.getNameWithGenus() + " on " + targetName + ".", performer, 10);
            } else {
               performer.getCommunicator().sendNormalServerMessage("You try to use the rune on " + target.getName() + " but fail.", (byte)3);
               Server.getInstance()
                  .broadCastAction(
                     performer.getNameWithGenus() + " attempts to use " + source.getNameWithGenus() + " on " + targetName + " but fails.", performer, 10
                  );
            }

            if (Servers.isThisATestServer()) {
               performer.getCommunicator()
                  .sendNormalServerMessage(
                     "Diff: "
                        + diff
                        + ", bonus: "
                        + source.getCurrentQualityLevel()
                        + ", sd: "
                        + soulDepth.getKnowledge()
                        + ", power: "
                        + power
                        + ", chance: "
                        + soulDepth.getChance(diff, null, (double)source.getCurrentQualityLevel())
                  );
            }

            Items.destroyItem(source.getWurmId());
            return true;
         }
      }
   }

   static String[] getPlayfulReactionString() {
      String[] preactions = new String[]{"squeals", "cries", "screams", "laughs", "squirms", "giggles", "coughs", "curses", "falls over", "glares"};
      String[] treactions = new String[]{"squeal", "cry", "scream", "laugh", "squirm", "giggle", "cough", "curse", "fall over", "glare"};
      int selection = Server.rand.nextInt(preactions.length);
      return new String[]{preactions[selection], treactions[selection]};
   }
}
