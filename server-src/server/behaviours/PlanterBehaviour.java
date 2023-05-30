package com.wurmonline.server.behaviours;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlanterBehaviour extends ItemBehaviour {
   private static final Logger logger = Logger.getLogger(PlanterBehaviour.class.getName());

   public PlanterBehaviour() {
      super((short)55);
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, target);
      if (canBePicked(target)) {
         toReturn.add(Actions.actionEntrys[137]);
      }

      return toReturn;
   }

   @Override
   public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target) {
      List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
      if (source.isPotable() && target.getTemplateId() == 1161 && source.isRaw() && (source.isPStateNone() || source.isFresh())) {
         toReturn.add(new ActionEntry((short)186, "Plant " + source.getName(), "planting"));
      }

      if (source.getTemplateId() == 176 && performer.getPower() >= 2 && target.getTemplateId() == 1162) {
         toReturn.add(Actions.actionEntrys[188]);
      }

      if (canBePicked(target)) {
         toReturn.add(Actions.actionEntrys[137]);
      }

      return toReturn;
   }

   @Override
   public boolean action(Action act, Creature performer, Item target, short action, float counter) {
      return action == 137 ? pickHerb(act, performer, target, counter) : super.action(act, performer, target, action, counter);
   }

   @Override
   public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter) {
      if (action == 1) {
         return this.action(act, performer, target, action, counter);
      } else if (action != 186) {
         if (action == 188 && source.getTemplateId() == 176 && performer.getPower() >= 2) {
            target.advancePlanterWeek();
            return true;
         } else {
            return super.action(act, performer, source, target, action, counter);
         }
      } else if (!source.isSpice() || !source.isFresh() && !source.isPStateNone()) {
         return !source.isHerb() || !source.isFresh() && !source.isPStateNone() ? true : plantHerb(act, performer, source, target, counter);
      } else {
         return plantHerb(act, performer, source, target, counter);
      }
   }

   private static final boolean pickHerb(Action act, Creature performer, Item pot, float counter) {
      int time = 0;
      ItemTemplate growing = pot.getRealTemplate();
      if (growing == null) {
         performer.getCommunicator().sendNormalServerMessage("Not sure what is growing in here.", (byte)3);
         return true;
      } else if (!Methods.isActionAllowed(performer, act.getNumber())) {
         return true;
      } else if (!canBePicked(pot)) {
         performer.getCommunicator().sendNormalServerMessage("It is not at correct age to be picked.", (byte)3);
         return true;
      } else if (!performer.getInventory().mayCreatureInsertItem()) {
         performer.getCommunicator().sendNormalServerMessage("Your inventory is full. You would have no space to put whatever you pick.");
         return true;
      } else {
         Skill gardening = performer.getSkills().getSkillOrLearn(10045);
         if (counter == 1.0F) {
            time = Actions.getStandardActionTime(performer, gardening, pot, 0.0) / 5;
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start picking " + growing.getNameWithGenus() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to pick some " + growing.getName() + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[137].getVerbString(), true, time);
            return false;
         } else {
            time = act.getTimeLeft();
            if (counter * 10.0F > (float)time) {
               if (act.getRarity() != 0) {
                  performer.playPersonalSound("sound.fx.drumroll");
               }

               int age = pot.getAuxData() & 127;
               int knowledge = (int)gardening.getKnowledge(0.0);
               float diff = getDifficulty(pot.getRealTemplateId(), knowledge);
               double power = gardening.skillCheck((double)diff, 0.0, false, counter);

               try {
                  float ql = Herb.getQL(power, knowledge);
                  Item newItem = ItemFactory.createItem(pot.getRealTemplateId(), Math.max(ql, 1.0F), (byte)0, act.getRarity(), null);
                  if (ql < 0.0F) {
                     newItem.setDamage(-ql / 2.0F);
                  } else {
                     newItem.setIsFresh(true);
                  }

                  Item inventory = performer.getInventory();
                  inventory.insertItem(newItem);
                  performer.achievement(602);
               } catch (FailedException var15) {
                  logger.log(Level.WARNING, performer.getName() + " " + var15.getMessage(), (Throwable)var15);
               } catch (NoSuchTemplateException var16) {
                  logger.log(Level.WARNING, performer.getName() + " " + var16.getMessage(), (Throwable)var16);
               }

               pot.setLastMaintained(WurmCalendar.currentTime);
               if (power < -50.0) {
                  performer.getCommunicator()
                     .sendNormalServerMessage(
                        "You broke off more than needed and damaged the plant, but still managed to get " + growing.getNameWithGenus() + "."
                     );
                  pot.setAuxData((byte)(age + 1));
               } else if (power > 0.0) {
                  performer.getCommunicator().sendNormalServerMessage("You successfully picked " + growing.getNameWithGenus() + ", it now looks healthier.");
                  pot.setAuxData((byte)(age - 1));
               } else {
                  performer.getCommunicator().sendNormalServerMessage("You successfully picked " + growing.getNameWithGenus() + ".");
                  pot.setAuxData((byte)age);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static boolean canBePicked(Item pot) {
      if (pot.getTemplateId() != 1162) {
         return false;
      } else {
         ItemTemplate temp = pot.getRealTemplate();
         int age = pot.getAuxData() & 127;
         boolean pickable = (pot.getAuxData() & 128) != 0;
         return temp != null && pickable && age > 5 && age < 95;
      }
   }

   private static boolean plantHerb(Action act, Creature performer, Item herbSpice, Item pot, float counter) {
      if (!Methods.isActionAllowed(performer, act.getNumber())) {
         return true;
      } else {
         int time = 0;
         if (counter == 1.0F) {
            String type = herbSpice.isSpice() ? "spice" : "herb";
            Skill gardening = performer.getSkills().getSkillOrLearn(10045);
            time = Actions.getStandardActionTime(performer, gardening, herbSpice, 0.0);
            act.setTimeLeft(time);
            performer.getCommunicator().sendNormalServerMessage("You start planting the " + herbSpice.getName() + ".");
            Server.getInstance().broadCastAction(performer.getName() + " starts to plant some " + type + ".", performer, 5);
            performer.sendActionControl(Actions.actionEntrys[186].getVerbString(), true, time);
            return false;
         } else {
            time = act.getTimeLeft();
            if (counter * 10.0F > (float)time) {
               float ql = herbSpice.getQualityLevel() + pot.getQualityLevel();
               ql /= 2.0F;
               float dmg = herbSpice.getDamage() + pot.getDamage();
               dmg /= 2.0F;
               Skill gardening = performer.getSkills().getSkillOrLearn(10045);

               try {
                  int toCreate = 1162;
                  ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(1162);
                  double power = gardening.skillCheck((double)(template.getDifficulty() + dmg), (double)ql, false, counter);
                  if (!(power > 0.0)) {
                     performer.getCommunicator()
                        .sendNormalServerMessage("Sadly, the fragile " + herbSpice.getName() + " do not survive despite your best efforts.", (byte)3);
                  } else {
                     try {
                        Item newPot = ItemFactory.createItem(1162, pot.getQualityLevel(), pot.getRarity(), performer.getName());
                        newPot.setRealTemplate(herbSpice.getTemplate().getGrows());
                        newPot.setLastOwnerId(pot.getLastOwnerId());
                        newPot.setDescription(pot.getDescription());
                        newPot.setDamage(pot.getDamage());
                        Item parent = pot.getParentOrNull();
                        if (parent != null && parent.getTemplateId() == 1110 && parent.getItemsAsArray().length > 30) {
                           performer.getCommunicator()
                              .sendNormalServerMessage("The pot will not fit back into the rack, so you place it on the ground.", (byte)2);
                           newPot.setPosXY(pot.getPosX(), pot.getPosY());
                           VolaTile tile = Zones.getTileOrNull(pot.getTileX(), pot.getTileY(), pot.isOnSurface());
                           if (tile != null) {
                              tile.addItem(newPot, false, false);
                           }
                        } else if (parent == null) {
                           newPot.setPosXYZRotation(pot.getPosX(), pot.getPosY(), pot.getPosZ(), pot.getRotation());
                           newPot.setIsPlanted(pot.isPlanted());
                           VolaTile tile = Zones.getTileOrNull(pot.getTileX(), pot.getTileY(), pot.isOnSurface());
                           if (tile != null) {
                              tile.addItem(newPot, false, false);
                           }
                        } else {
                           parent.insertItem(newPot, true);
                        }

                        Items.destroyItem(pot.getWurmId());
                        performer.getCommunicator().sendNormalServerMessage("You finished planting the " + herbSpice.getName() + " in the pot.");
                     } catch (NoSuchTemplateException var16) {
                        logger.log(Level.WARNING, var16.getMessage(), (Throwable)var16);
                     } catch (FailedException var17) {
                        logger.log(Level.WARNING, var17.getMessage(), (Throwable)var17);
                     }
                  }

                  Items.destroyItem(herbSpice.getWurmId());
               } catch (NoSuchTemplateException var18) {
                  logger.log(Level.WARNING, var18.getMessage(), (Throwable)var18);
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   private static float getDifficulty(int templateId, int knowledge) {
      float h = Herb.getDifficulty(templateId, knowledge);
      if (h > 0.0F) {
         return h;
      } else {
         float f = Forage.getDifficulty(templateId, knowledge);
         return f > 0.0F ? f : 0.0F;
      }
   }
}
