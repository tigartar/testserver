package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FlowerpotBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(FlowerpotBehaviour.class.getName());

   public FlowerpotBehaviour() {
      super((short)47);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      return super.getBehavioursFor(performer, target);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      if (!source.isFlower() || target.getTemplateId() != 813 && target.getTemplateId() != 1001) {
         if (source.isContainerLiquid()) {
            Item[] items = source.getItemsAsArray();

            for(int i = 0; i < items.length; ++i) {
               if (items[i].getTemplateId() == 128) {
                  toReturn.add(Actions.actionEntrys[565]);
                  break;
               }
            }
         }
      } else {
         toReturn.add(Actions.actionEntrys[564]);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      return super.action(act, performer, target, action, counter);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      if (action == 1) {
         return this.action(act, performer, target, action, counter);
      } else if (action == 564) {
         return plantFlowerInPot(act, performer, source, target, counter);
      } else {
         return action == 565 ? waterFlower(act, performer, source, target, counter) : super.action(act, performer, source, target, action, counter);
      }
   }

   private static final boolean waterFlower(Action act, Creature performer, Item waterSource, Item pot, float counter) {
      int time = 0;
      Skill gardening = null;
      Item water = null;
      Item[] items = waterSource.getItemsAsArray();

      for(int i = 0; i < items.length; ++i) {
         if (items[i].getTemplateId() == 128) {
            water = items[i];
            break;
         }
      }

      if (water == null) {
         performer.getCommunicator().sendNormalServerMessage("You need water to water the flowers.", (byte)3);
         return true;
      } else if (water.getWeightGrams() < 100) {
         performer.getCommunicator().sendNormalServerMessage("You need more water in order to water the flowers.", (byte)3);
         return true;
      } else if (pot.getDamage() == 0.0F) {
         performer.getCommunicator().sendNormalServerMessage("The flowers are in no need of watering.", (byte)3);
         return true;
      } else {
         try {
            gardening = performer.getSkills().getSkill(10045);
         } catch (NoSuchSkillException var12) {
            gardening = performer.getSkills().learn(10045, 1.0F);
         }

         if (counter == 1.0F) {
            time = Actions.getStandardActionTime(performer, gardening, pot, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start watering the flowers.");
            Server.getInstance().broadCastAction(performer.getName() + " starts to water some flowers.", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[565].getVerbString(), true, time);
            return false;
         } else {
            time = act.getTimeLeft();
            if (counter * 10.0F > (float)time) {
               double power = gardening.skillCheck(15.0, 0.0, false, counter);
               if (power > 0.0) {
                  float dmgChange = 20.0F * (float)(power / 100.0);
                  pot.setDamage(Math.max(0.0F, pot.getDamage() - dmgChange));
                  water.setWeight(water.getWeightGrams() - 100, true);
                  performer.getCommunicator().sendNormalServerMessage("You successfully watered the flowers, they look healthier.", (byte)2);
                  return true;
               } else {
                  int waterReduction = 100;
                  if (power >= -20.0) {
                     performer.getCommunicator().sendNormalServerMessage("You accidentally miss the pot and pour the water on the ground instead.", (byte)3);
                  } else if (power > -50.0 && power < -20.0) {
                     performer.getCommunicator().sendNormalServerMessage("You spill water all over your clothes.", (byte)3);
                  } else {
                     performer.getCommunicator()
                        .sendNormalServerMessage(
                           "For some inexplicable reason you poured all of the water on the ground, how you thought it would help you will never know."
                        );
                     waterReduction = Math.min(water.getWeightGrams(), 200);
                  }

                  water.setWeight(water.getWeightGrams() - waterReduction, true);
                  return true;
               }
            } else {
               return false;
            }
         }
      }
   }

   private static final int getFlowerpotIdFromFlower(Item flower, Item pot) {
      if (pot.getTemplateId() == 813) {
         int templateId = flower.getTemplateId();
         if (templateId == 498) {
            return 814;
         } else if (templateId == 499) {
            return 818;
         } else if (templateId == 500) {
            return 816;
         } else if (templateId == 501) {
            return 817;
         } else if (templateId == 502) {
            return 815;
         } else {
            return templateId == 503 ? 819 : 820;
         }
      } else {
         int templateId = flower.getTemplateId();
         if (templateId == 498) {
            return 1002;
         } else if (templateId == 499) {
            return 1006;
         } else if (templateId == 500) {
            return 1004;
         } else if (templateId == 501) {
            return 1005;
         } else if (templateId == 502) {
            return 1003;
         } else {
            return templateId == 503 ? 1007 : 1008;
         }
      }
   }

   private static boolean plantFlowerInPot(Action act, Creature performer, Item flower, Item pot, float counter) {
      int time = 0;
      if (counter == 1.0F) {
         Skill gardening = null;

         try {
            gardening = performer.getSkills().getSkill(10045);
         } catch (NoSuchSkillException var14) {
            gardening = performer.getSkills().learn(10045, 1.0F);
         }

         time = Actions.getStandardActionTime(performer, gardening, flower, 0.0);
         act.setTimeLeft(time);
         performer.getCommunicator().sendNormalServerMessage("You start planting the flowers.");
         Server.getInstance().broadCastAction(performer.getName() + " starts to plant some flowers.", performer, 5);
         performer.sendActionControl(Actions.actionEntrys[564].getVerbString(), true, time);
         return false;
      } else {
         time = act.getTimeLeft();
         if (counter * 10.0F > (float)time) {
            float ql = flower.getQualityLevel() + pot.getQualityLevel();
            ql /= 2.0F;
            float dmg = flower.getDamage() + pot.getDamage();
            dmg /= 2.0F;
            Skill gardening = null;

            try {
               gardening = performer.getSkills().getSkill(10045);
            } catch (NoSuchSkillException var18) {
               gardening = performer.getSkills().learn(10045, 1.0F);
            }

            try {
               int toCreate = getFlowerpotIdFromFlower(flower, pot);
               ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(toCreate);
               double power = gardening.skillCheck((double)(template.getDifficulty() + dmg), (double)ql, false, counter);
               if (power > 0.0) {
                  try {
                     Item newPot = ItemFactory.createItem(toCreate, pot.getQualityLevel(), pot.getRarity(), performer.getName());
                     newPot.setDamage(pot.getDamage());
                     newPot.setLastOwnerId(pot.getLastOwnerId());
                     newPot.setDescription(pot.getDescription());
                     Items.destroyItem(pot.getWurmId());
                     performer.getInventory().insertItem(newPot, true);
                     performer.getCommunicator().sendNormalServerMessage("You finished planting the flowers in the pot.");
                  } catch (NoSuchTemplateException var15) {
                     logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
                  } catch (FailedException var16) {
                     logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                  }
               } else {
                  performer.getCommunicator().sendNormalServerMessage("Sadly, the fragile flowers do not survive despite your best efforts.", (byte)3);
               }

               Items.destroyItem(flower.getWurmId());
            } catch (NoSuchTemplateException var17) {
               logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
