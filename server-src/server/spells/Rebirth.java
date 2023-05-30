package com.wurmonline.server.spells;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.Players;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Rebirth extends ReligiousSpell {
   private static final Logger logger = Logger.getLogger(Rebirth.class.getName());
   public static final int RANGE = 4;

   Rebirth() {
      super("Rebirth", 273, 20, 40, 40, 40, 0L);
      this.targetItem = true;
      this.description = "raises zombies from corpses";
      this.type = 2;
   }

   @Override
   boolean precondition(Skill castSkill, Creature performer, Item target) {
      return mayRaise(performer, target, true);
   }

   @Override
   boolean postcondition(Skill castSkill, Creature performer, Item target, double power) {
      try {
         CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
         byte ctype = CreatureStatus.getModtypeForString(target.getName());
         if (template.isUnique() && power < 50.0) {
            performer.getCommunicator().sendNormalServerMessage("The soul of this creature is strong, and it manages to resist your attempt.", (byte)3);
            return false;
         } else if (ctype == 99 && power < 20.0) {
            performer.getCommunicator().sendNormalServerMessage("The soul of this creature is strong, and it manages to resist your attempt.", (byte)3);
            return false;
         } else {
            return true;
         }
      } catch (NoSuchCreatureTemplateException var8) {
         performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
         return false;
      }
   }

   public static boolean mayRaise(Creature performer, Item target, boolean sendMess) {
      if (target.getTemplateId() == 272) {
         if (performer.getDeity() != null) {
            if (target.getDamage() < 10.0F) {
               if (!target.isButchered()) {
                  try {
                     CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
                     if (template.isRiftCreature()) {
                        if (sendMess) {
                           performer.getCommunicator().sendNormalServerMessage("This corpse is too far gone for that to work.", (byte)3);
                        }

                        return false;
                     }

                     if (template.isNotRebirthable()) {
                        if (sendMess) {
                           performer.getCommunicator().sendNormalServerMessage("The soul refuses to return to this corpse.", (byte)3);
                        }

                        return false;
                     }

                     if (template.isTowerBasher()) {
                        if (sendMess) {
                           performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
                        }

                        return false;
                     }

                     if (!template.isHuman()) {
                        return true;
                     }

                     if (MethodsItems.isLootableBy(performer, target)) {
                        String name = target.getName().substring(10, target.getName().length());

                        try {
                           long wid = Players.getInstance().getWurmIdFor(name);
                           if (wid == performer.getWurmId()) {
                              if (sendMess) {
                                 performer.getCommunicator()
                                    .sendNormalServerMessage(performer.getDeity().getName() + " does not allow you to raise your own corpse.", (byte)3);
                              }

                              return false;
                           }

                           return true;
                        } catch (Exception var7) {
                           if (sendMess) {
                              performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
                           }
                        }
                     } else if (sendMess) {
                        performer.getCommunicator().sendNormalServerMessage("You may not touch this body right now.", (byte)3);
                     }
                  } catch (NoSuchCreatureTemplateException var8) {
                     if (sendMess) {
                        performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
                     }
                  }
               } else if (sendMess) {
                  performer.getCommunicator().sendNormalServerMessage("The corpse is butchered and may not be raised.", (byte)3);
               }
            } else if (sendMess) {
               performer.getCommunicator().sendNormalServerMessage("The corpse is too damaged.", (byte)3);
            }
         } else if (sendMess) {
            performer.getCommunicator().sendNormalServerMessage("Nothing happens. No deity answers to the call.", (byte)3);
         }
      } else if (sendMess) {
         performer.getCommunicator().sendNormalServerMessage("The spell will only work on corpses.", (byte)3);
      }

      return false;
   }

   @Override
   void doEffect(Skill castSkill, double power, Creature performer, Item target) {
      if (power > 0.0) {
         raise(power, performer, target, false);
      } else {
         performer.getCommunicator().sendNormalServerMessage("You fail to connect with the soul of this creature and bind it in a physical form.", (byte)3);
      }
   }

   public static void raise(double power, Creature performer, Item target, boolean massRaise) {
      if (target.getTemplateId() != 272) {
         if (!massRaise) {
            performer.getCommunicator().sendNormalServerMessage("The spell will only work on corpses.", (byte)3);
         }
      } else if (performer.getDeity() != null) {
         if (!(target.getDamage() < 10.0F)) {
            if (!massRaise) {
               performer.getCommunicator().sendNormalServerMessage("The corpse is too damaged.", (byte)3);
            }
         } else {
            try {
               CreatureTemplate template = CreatureTemplateFactory.getInstance().getTemplate(target.getData1());
               Creature cret = null;
               if (template.isHuman()) {
                  if (MethodsItems.isLootableBy(performer, target)) {
                     String name = target.getName().substring(10, target.getName().length());

                     try {
                        long wid = Players.getInstance().getWurmIdFor(name);
                        byte sex = 0;
                        if (target.female) {
                           sex = 1;
                        }

                        if (wid == performer.getWurmId()) {
                           if (!massRaise) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage(performer.getDeity().getName() + " does not allow you to raise your own corpse.", (byte)3);
                           }

                           return;
                        }

                        cret = Creature.doNew(
                           template.getTemplateId(),
                           false,
                           target.getPosX(),
                           target.getPosY(),
                           target.getRotation(),
                           target.isOnSurface() ? 0 : -1,
                           LoginHandler.raiseFirstLetter("Zombie " + name),
                           sex,
                           performer.getKingdomId(),
                           (byte)0,
                           true
                        );
                        cret.getStatus().setTraitBit(63, true);
                        Skills skills = SkillsFactory.createSkills(wid);

                        try {
                           skills.load();
                           cret.getSkills().delete();
                           cret.getSkills().clone(skills.getSkills());
                           Skill[] cskills = cret.getSkills().getSkills();

                           for(Skill cSkill : cskills) {
                              if (cSkill.getNumber() == 10052) {
                                 cSkill.setKnowledge(Math.min(70.0, cSkill.getKnowledge() * 0.7F), false);
                              } else {
                                 cSkill.setKnowledge(Math.min(40.0, cSkill.getKnowledge() * 0.7F), false);
                              }
                           }

                           cret.getSkills().save();
                        } catch (Exception var19) {
                           logger.log(Level.WARNING, var19.getMessage(), (Throwable)var19);
                           if (!massRaise) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("You struggle to bring the corpse back to life, but you sense problems.", (byte)3);
                           }
                        }
                     } catch (Exception var20) {
                        if (!massRaise) {
                           performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
                        }
                     }
                  } else if (!massRaise) {
                     performer.getCommunicator().sendNormalServerMessage("You may not touch this body right now.", (byte)3);
                  }
               } else {
                  byte ctype = CreatureStatus.getModtypeForString(target.getName());
                  if (template.isUnique() && power < 50.0) {
                     if (!massRaise) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The soul of this creature is strong, and it manages to resist your attempt.", (byte)3);
                     }

                     return;
                  }

                  if (ctype == 99 && power < 20.0) {
                     if (!massRaise) {
                        performer.getCommunicator()
                           .sendNormalServerMessage("The soul of this creature is strong, and it manages to resist your attempt.", (byte)3);
                     }

                     return;
                  }

                  byte sex = 0;
                  if (target.female) {
                     sex = 1;
                  }

                  try {
                     cret = Creature.doNew(
                        template.getTemplateId(),
                        false,
                        target.getPosX(),
                        target.getPosY(),
                        target.getRotation(),
                        target.isOnSurface() ? 0 : -1,
                        LoginHandler.raiseFirstLetter("Zombie " + template.getName()),
                        sex,
                        performer.getKingdomId(),
                        ctype,
                        true
                     );
                     cret.getStatus().setTraitBit(63, true);
                     if (template.isUnique()) {
                        try {
                           Skill[] skills = cret.getSkills().getSkills();

                           for(Skill lSkill : skills) {
                              if (lSkill.getNumber() == 10052) {
                                 lSkill.setKnowledge(lSkill.getKnowledge() * 0.4F, false);
                              } else {
                                 lSkill.setKnowledge(lSkill.getKnowledge() * 0.2F, false);
                              }
                           }
                        } catch (Exception var17) {
                           logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
                           if (!massRaise) {
                              performer.getCommunicator()
                                 .sendNormalServerMessage("You struggle to bring the corpse back to life, but you sense problems.", (byte)3);
                           }
                        }
                     }
                  } catch (Exception var18) {
                     if (!massRaise) {
                        performer.getCommunicator().sendNormalServerMessage("You struggle to bring the corpse back to life.", (byte)3);
                     }
                  }
               }

               if (cret != null && !massRaise) {
                  if (performer.getPet() != null) {
                     performer.getCommunicator().sendNormalServerMessage(performer.getPet().getName() + " stops obeying you.", (byte)2);
                     if (performer.getPet().getLeader() == performer) {
                        performer.getPet().setLeader(null);
                     }

                     if (performer.getPet().isReborn()) {
                        performer.getPet().die(false, "Neglect");
                     } else {
                        performer.getPet().setDominator(-10L);
                     }
                  }

                  performer.setPet(cret.getWurmId());
                  cret.setDominator(performer.getWurmId());
                  cret.setLoyalty(Math.max(10.0F, (float)power));
                  cret.getStatus().setLastPolledLoyalty();
                  cret.setTarget(-10L, true);
                  if (performer.getTarget() == cret) {
                     performer.setTarget(-10L, true);
                  }

                  if (cret.opponent != null) {
                     cret.setOpponent(null);
                  }

                  if (performer.opponent == cret) {
                     performer.setOpponent(null);
                  }

                  performer.getCommunicator().sendNormalServerMessage(cret.getName() + " now obeys you.", (byte)2);
                  VolaTile targetVolaTile = Zones.getTileOrNull(cret.getTileX(), cret.getTileY(), cret.isOnSurface());
                  if (targetVolaTile != null) {
                     targetVolaTile.sendAttachCreatureEffect(cret, (byte)8, (byte)0, (byte)0, (byte)0, (byte)0);
                  }
               }

               target.setDamage(Math.max(target.getDamage(), 10.0F));
            } catch (NoSuchCreatureTemplateException var21) {
               if (!massRaise) {
                  performer.getCommunicator().sendNormalServerMessage("There is no soul attached to this corpse any longer.", (byte)3);
               }
            }
         }
      } else if (!massRaise) {
         performer.getCommunicator().sendNormalServerMessage("Nothing happens. No deity answers to the call.", (byte)3);
      }
   }
}
