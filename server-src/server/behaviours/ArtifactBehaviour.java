package com.wurmonline.server.behaviours;

import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.bodys.TempWound;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.LocatePlayerQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Excel;
import com.wurmonline.server.spells.ForestGiant;
import com.wurmonline.server.spells.FranticCharge;
import com.wurmonline.server.spells.Hellstrength;
import com.wurmonline.server.spells.OakShell;
import com.wurmonline.server.spells.Phantasms;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class ArtifactBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(ArtifactBehaviour.class.getName());
   private static long orbActivation = 0L;

   ArtifactBehaviour() {
      super((short)35);
   }

   public static long getOrbActivation() {
      return orbActivation;
   }

   public static void resetOrbActivation() {
      orbActivation = 0L;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = new ArrayList<>();
      toReturn.addAll(super.getBehavioursFor(performer, target));
      if (performer.getWurmId() == target.getOwnerId()) {
         toReturn.add(Actions.actionEntrys[118]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = new ArrayList<>();
      toReturn.addAll(super.getBehavioursFor(performer, source, target));
      if (performer.getWurmId() == target.getOwnerId()) {
         toReturn.add(Actions.actionEntrys[118]);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      boolean done = true;
      if (performer.getWurmId() == target.getOwnerId() && action == 118) {
         if (mayUseItem(target, performer)) {
            return useItem(null, target, performer, counter);
         }

         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits no sense of power right now.");
      } else {
         done = super.action(act, performer, target, action, counter);
      }

      return done;
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      boolean done = false;
      if (performer.getWurmId() == target.getOwnerId() && action == 118) {
         done = true;
         if (mayUseItem(target, performer)) {
            return useItem(source, target, performer, counter);
         }

         performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " emits no sense of power right now.");
      } else {
         done = super.action(act, performer, source, target, action, counter);
      }

      return done;
   }

   public static final boolean mayUseItem(Item target, @Nullable Creature performer) {
      if (target.getAuxData() <= 0) {
         return false;
      } else if (target.getTemplateId() == 330) {
         return WurmCalendar.currentTime - target.getData() > 86400L;
      } else if (target.getTemplateId() == 334) {
         return WurmCalendar.currentTime - target.getData() > 345600L;
      } else if (target.getTemplateId() == 339) {
         if (WurmCalendar.currentTime - target.getData() < 28800L) {
            if (performer != null) {
               performer.getCommunicator().sendNormalServerMessage("The " + target.getName() + " is still fading from the last activation.");
            }

            return false;
         } else {
            return true;
         }
      } else {
         return WurmCalendar.currentTime - target.getData() > 28800L;
      }
   }

   public static final void spawnCreature(int deity, float posx, float posy, boolean onSurface, byte kingdom) {
      int creatureTemplate = 37;
      if (deity == 4) {
         kingdom = 3;
         creatureTemplate = 40;
      } else if (kingdom == 3) {
         if (Server.rand.nextInt(2) == 0) {
            kingdom = 1;
         } else {
            kingdom = 2;
         }
      }

      if (deity == 2) {
         creatureTemplate = 39;
      } else if (deity == 3) {
         creatureTemplate = 38;
      }

      try {
         CreatureTemplate ctemplate = CreatureTemplateFactory.getInstance().getTemplate(creatureTemplate);
         Creature cret = Creature.doNew(creatureTemplate, posx, posy, (float)Server.rand.nextInt(360), onSurface ? 0 : -1, "", ctemplate.getSex(), kingdom);
         cret.setDeity(Deities.getDeity(deity));
      } catch (Exception var8) {
         logger.log(
            Level.WARNING,
            "Problem spawning new Creature with Template ID: "
               + creatureTemplate
               + ", at position: "
               + posx
               + ", "
               + posy
               + ", kingdom: "
               + Kingdoms.getNameFor(kingdom)
               + ", deity: "
               + deity
               + " due to "
               + var8.getMessage(),
            (Throwable)var8
         );
      }
   }

   public static final boolean useItem(@Nullable Item source, Item target, Creature performer, float counter) {
      boolean done = true;
      Server.getInstance().broadCastAction(performer.getName() + " uses " + performer.getHisHerItsString() + " " + target.getName() + "!", performer, 5);
      if (target.getTemplateId() == 332) {
         performer.getCommunicator().sendNormalServerMessage(EndGameItems.locateRandomEndGameItem(performer));
         LocatePlayerQuestion lpq = new LocatePlayerQuestion(performer, "Locate a soul", "Which soul do you wish to locate?", target.getWurmId(), true, 100.0);
         lpq.sendQuestion();
         target.setAuxData((byte)(target.getAuxData() - 1));
      } else if (target.getTemplateId() == 330) {
         if (Server.rand.nextInt(8) == 0) {
            spawnCreature(
               2,
               performer.getPosX() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.getPosY() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.isOnSurface(),
               performer.getKingdomId()
            );
         }

         performer.getCommunicator().sendNormalServerMessage("You notice how a previously invisible etched dragon glows for a second.");
         if (performer.getDeity() != null && performer.getDeity().number == 2) {
            FranticCharge.doImmediateEffect(423, 200, (double)(40 + Server.rand.nextInt(40)), performer);
         }

         target.setAuxData((byte)(target.getAuxData() - 1));
      } else if (target.getTemplateId() == 331) {
         if (Server.rand.nextInt(8) == 0) {
            spawnCreature(
               1,
               performer.getPosX() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.getPosY() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.isOnSurface(),
               performer.getKingdomId()
            );
         }

         performer.getCommunicator().sendNormalServerMessage("You notice how a previously invisible etched boar glows for a second.");
         if (performer.getDeity() != null && performer.getDeity().number == 1) {
            OakShell.doImmediateEffect(404, 200, (double)(20 + Server.rand.nextInt(40)), performer);
         }

         target.setAuxData((byte)(target.getAuxData() - 1));
      } else if (target.getTemplateId() == 333) {
         Village v = Villages.getVillageWithPerimeterAt(performer.getTileX(), performer.getTileY(), true);
         if (v == null) {
            if (Server.rand.nextInt(2) == 0) {
               Item item = TileRockBehaviour.createRandomGem();
               if (item != null) {
                  item.setQualityLevel(70.0F + Server.rand.nextFloat() * 30.0F);
                  performer.getCommunicator().sendNormalServerMessage("You shake the ear, and out pops " + item.getNameWithGenus() + "!");
                  performer.getInventory().insertItem(item, true);
                  target.setAuxData((byte)(target.getAuxData() - 1));
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You shake the ear, but nothing happens.");
               }
            } else if (Server.rand.nextInt(8) == 1) {
               spawnCreature(
                  3,
                  performer.getPosX() - 4.0F + Server.rand.nextFloat() * 8.0F,
                  performer.getPosY() - 4.0F + Server.rand.nextFloat() * 8.0F,
                  performer.isOnSurface(),
                  performer.getKingdomId()
               );
               target.setAuxData((byte)(target.getAuxData() - 1));
            } else {
               performer.getCommunicator().sendNormalServerMessage("You shake the ear, but nothing happens.");
            }
         } else {
            performer.getCommunicator().sendNormalServerMessage("You shake the ear, but nothing happens here at " + v.getName() + ".");
         }
      } else if (target.getTemplateId() == 334) {
         performer.getStatus().modifyStamina2(100.0F);
         performer.getStatus().refresh(0.99F, true);
         ((Player)performer).getSaveFile().addToSleep(3600);
         performer.getCommunicator().sendNormalServerMessage("You feel refreshed.");
         if (performer.getDeity() != null && performer.getDeity().number == 3) {
            Excel.doImmediateEffect(442, 200, (double)(40 + Server.rand.nextInt(40)), performer);
         }

         target.setAuxData((byte)(target.getAuxData() - 1));
      } else if (target.getTemplateId() == 335) {
         performer.getBody().healFully();
         performer.getStatus().removeWounds();
         performer.getCommunicator().sendNormalServerMessage("A strong warm feeling permeates you and heals your wounds!");
         if (performer.getDeity() != null && performer.getDeity().number == 1) {
            ForestGiant.doImmediateEffect(410, 200, (double)(20 + Server.rand.nextInt(40)), performer);
         }

         target.setAuxData((byte)(target.getAuxData() - 1));
      } else if (target.getTemplateId() == 336) {
         try {
            if (performer.getDeity() != null) {
               performer.setFavor(performer.getFavor() + 10.0F);
               performer.getCommunicator().sendNormalServerMessage("You feel the strength of " + Deities.getDeity(2).name + " within you.");
            } else {
               performer.getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
               performer.die(false, "Sword of Magranon");
            }

            target.setAuxData((byte)(target.getAuxData() - 1));
         } catch (IOException var24) {
         }
      } else if (target.getTemplateId() == 337) {
         try {
            if (performer.getDeity() != null) {
               performer.setFavor(performer.getFavor() + 10.0F);
               performer.getCommunicator().sendNormalServerMessage("You feel the strength of " + Deities.getDeity(2).name + " within you.");
            } else {
               performer.getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
               performer.die(false, "Hammer of Magranon");
            }

            target.setAuxData((byte)(target.getAuxData() - 1));
         } catch (IOException var23) {
         }
      } else if (target.getTemplateId() == 338) {
         if (Server.rand.nextInt(8) == 0) {
            spawnCreature(
               4,
               performer.getPosX() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.getPosY() - 4.0F + Server.rand.nextFloat() * 8.0F,
               performer.isOnSurface(),
               performer.getKingdomId()
            );
            target.setAuxData((byte)(target.getAuxData() - 1));
         }

         if (performer.getDeity() != null && performer.getDeity().number != 4) {
            target.setAuxData((byte)(target.getAuxData() - 1));
            Phantasms.doImmediateEffect(100.0, performer);
         }

         performer.getCommunicator().sendNormalServerMessage("You notice how a previously invisible etched skull glows for a second.");
      } else if (target.getTemplateId() == 339) {
         boolean slow = true;
         if (Server.rand.nextInt(20) == 0) {
            slow = false;
         }

         if (!slow) {
            if (performer.getCurrentTile() != null) {
               target.setAuxData((byte)Math.max(0, target.getAuxData() - 10));
               int tilex = performer.getCurrentTile().tilex;
               int tiley = performer.getCurrentTile().tiley;

               for(int x = 2; x >= -2; --x) {
                  for(int y = 2; y >= -2; --y) {
                     try {
                        Zone zone = Zones.getZone(tilex + x, tiley + y, performer.isOnSurface());
                        VolaTile vtile = zone.getTileOrNull(tilex + x, tiley + y);
                        if (vtile != null) {
                           Creature[] crets = vtile.getCreatures();

                           for(int c = 0; c < crets.length; ++c) {
                              if (!crets[c].isUnique() && !crets[c].isInvulnerable()) {
                                 Skill soul = null;

                                 try {
                                    soul = crets[c].getSkills().getSkill(105);
                                 } catch (NoSuchSkillException var22) {
                                    soul = crets[c].getSkills().learn(105, 1.0F);
                                 }

                                 if (soul.skillCheck(crets[c].isChampion() ? 40.0 : 30.0, 0.0, false, 1.0F) < 0.0) {
                                    crets[c].getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
                                    if (crets[c] == performer) {
                                       crets[c].die(false, "Orb of Doom");
                                    } else {
                                       int damage = crets[c].getStatus().damage;
                                       int minhealth = 65435;
                                       if (crets[c].isUnique()) {
                                          minhealth = 55535;
                                       }

                                       float maxdam = (float)Math.max(0, minhealth - damage);
                                       if (maxdam > 500.0F) {
                                          Wound wound = null;
                                          if (crets[c] instanceof Player) {
                                             crets[c].addWoundOfType(performer, (byte)6, 0, false, 1.0F, false, (double)maxdam, 0.0F, 50.0F, false, false);
                                          } else {
                                             Wound var32 = new TempWound((byte)4, (byte)0, maxdam, crets[c].getWurmId(), 0.0F, 0.0F, false);
                                             crets[c].getBody().addWound(var32);
                                          }
                                       } else {
                                          crets[c]
                                             .getCommunicator()
                                             .sendSafeServerMessage(
                                                "You grit your teeth and escape to the darkest, innermost corner of your soul. There you barely escape death."
                                             );
                                          Server.getInstance().broadCastAction(crets[c].getNameWithGenus() + " seems unaffected.", crets[c], 5);
                                       }
                                    }
                                 } else {
                                    Server.getInstance().broadCastAction(crets[c].getNameWithGenus() + " seems unaffected.", crets[c], 5);
                                    crets[c]
                                       .getCommunicator()
                                       .sendSafeServerMessage(
                                          "A sudden pain enters your body! You grit your teeth and escape to the darkest, innermost corner of your soul. There you barely escape death."
                                       );
                                 }
                              } else {
                                 Server.getInstance().broadCastAction(crets[c].getNameWithGenus() + " seems unaffected.", crets[c], 5);
                              }
                           }
                        }
                     } catch (NoSuchZoneException var25) {
                     }
                  }
               }
            }
         } else {
            Skill soul = null;

            try {
               soul = performer.getSkills().getSkill(105);
            } catch (NoSuchSkillException var21) {
               soul = performer.getSkills().learn(105, 1.0F);
            }

            performer.getCommunicator().sendNormalServerMessage("You use the " + target.getName() + ".");
            if (soul.skillCheck(performer.isChampion() ? 30.0 : 25.0, 0.0, false, 1.0F) < 0.0) {
               performer.getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
               performer.die(false, target.getName());
               return true;
            }

            Server.getInstance()
               .broadCastAction("A pulse surges through the air. " + performer.getName() + " activates the " + target.getName() + "!", performer, 15);
            performer.sendActionControl(Actions.actionEntrys[118].getVerbString(), true, 200);
            markOrbRecipients(performer, true, 0.0F, 0.0F, 0.0F);
            target.setData(WurmCalendar.currentTime);
            orbActivation = System.currentTimeMillis();
         }
      } else if (target.getTemplateId() == 340) {
         try {
            target.setAuxData((byte)(target.getAuxData() - 1));
            if (performer.getDeity() != null) {
               performer.setFavor(performer.getFavor() + 5.0F);
               performer.getCommunicator().sendNormalServerMessage("You feel the power of " + Deities.getDeity(4).name + " descend on you.");
               if (performer.getDeity().number != 4) {
                  Phantasms.doImmediateEffect(100.0, performer);
               } else {
                  Hellstrength.doImmediateEffect(427, 200, (double)(20 + Server.rand.nextInt(40)), performer);
               }
            } else {
               performer.getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
               performer.die(false, "Scepter of Ascension");
            }
         } catch (IOException var20) {
         }
      } else if (target.getTemplateId() == 329) {
         try {
            target.setAuxData((byte)(target.getAuxData() - 1));
            if (performer.getDeity() != null) {
               if (performer.getFavor() < performer.getFaith() * 0.9F) {
                  performer.setFavor(performer.getFaith() * 0.9F);
               }

               performer.getCommunicator().sendNormalServerMessage("You feel the light of " + Deities.getDeity(1).name + " shine on you.");
               if (performer.getDeity().number != 1) {
                  Phantasms.doImmediateEffect(100.0, performer);
               } else {
                  ForestGiant.doImmediateEffect(410, 200, (double)(20 + Server.rand.nextInt(40)), performer);
               }
            } else {
               performer.getCommunicator().sendAlertServerMessage("A sudden pain enters your body.");
               performer.die(false, "Rod of Beguiling");
            }
         } catch (IOException var19) {
         }
      }

      target.setData(WurmCalendar.currentTime);
      return true;
   }

   public static final void markOrbRecipients(@Nullable Creature performer, boolean mark, float posx, float posy, float posz) {
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (mark && p.isWithinDistanceTo(performer, 24.0F)) {
            if (!p.isFriendlyKingdom(performer.getKingdomId()) && p.getPower() == 0) {
               p.setMarkedByOrb(true);
            }
         } else if (!mark && p.isMarkedByOrb()) {
            boolean deal = false;
            if (performer != null) {
               if (p.isWithinDistanceTo(performer, 12.0F)) {
                  deal = true;
               }
            } else if (p.isWithinDistanceTo(posx, posy, posz, 12.0F)) {
               deal = true;
            }

            if (deal) {
               p.addAttacker(performer);
               p.getCommunicator().sendAlertServerMessage("You are marked by the Orb of Doom!");
               p.addWoundOfType(performer, (byte)6, 21, false, 1.0F, false, 30000.0, 0.0F, 50.0F, false, false);
            }

            p.setMarkedByOrb(false);
         }
      }
   }
}
