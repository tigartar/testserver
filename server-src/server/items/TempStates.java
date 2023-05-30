package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ItemBehaviour;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TempStates implements MiscConstants {
   private static final Logger logger = Logger.getLogger(TempStates.class.getName());
   private static final Set<TempState> tempStates = new HashSet<>();

   private TempStates() {
   }

   public static void addState(TempState state) {
      tempStates.add(state);
   }

   public static Set<TempState> getTempStates() {
      return tempStates;
   }

   static boolean checkForChange(Item parent, Item target, short oldTemp, short newTemp, float qualityRatio) {
      for(TempState tempState : tempStates) {
         if (tempState.getOrigItemTemplateId() == target.getTemplateId()) {
            return tempState.changeItem(parent, target, oldTemp, newTemp, qualityRatio);
         }
      }

      if (newTemp > 1200 && !target.isLiquid() && target.isWrapped()) {
         if (Server.rand.nextInt(75) == 0) {
            if (target.canBeRawWrapped()) {
               if (target.isMeat()) {
                  target.setIsCooked();
               } else {
                  target.setIsSteamed();
               }
            }

            target.setIsWrapped(false);
         }
      } else if (newTemp > 1500 && Server.rand.nextInt(75) == 0) {
         long lastowner = parent.getLastOwnerId();
         if (parent.isFoodMaker() || parent.getTemplate().isCooker()) {
            for(Item i : parent.getItemsAsArray()) {
               if (i.getTemperature() < 1500) {
                  return false;
               }

               lastowner = i.getLastOwnerId();
            }
         }

         Item realTarget = parent;
         Recipe recipe = Recipes.getRecipeFor(lastowner, (byte)1, null, parent, true, true);
         if (recipe == null && !target.isHollow()) {
            recipe = Recipes.getRecipeFor(lastowner, (byte)1, null, target, true, true);
            lastowner = target.getLastOwnerId();
            realTarget = target;
         }

         if (recipe == null) {
            return false;
         }

         ItemTemplate template = recipe.getResultTemplate(realTarget);
         Skill primSkill = null;
         Creature lastown = null;
         float alc = 0.0F;
         boolean chefMade = false;
         double bonus = 0.0;
         boolean showOwner = false;

         try {
            lastown = Server.getInstance().getCreature(lastowner);
            bonus = lastown.getVillageSkillModifier();
            alc = Players.getInstance().getPlayer(lastowner).getAlcohol();
            Skills skills = lastown.getSkills();
            primSkill = skills.getSkillOrLearn(recipe.getSkillId());
            if (lastown.isRoyalChef()) {
               chefMade = true;
            }

            showOwner = primSkill.getKnowledge(0.0) > 70.0;
         } catch (NoSuchCreatureException var37) {
         } catch (NoSuchPlayerException var38) {
         }

         int newWeight = 0;
         if (!realTarget.isFoodMaker() && !realTarget.getTemplate().isCooker()) {
            newWeight = realTarget.getWeightGrams();
         } else {
            int liquid = 0;

            for(Item item : realTarget.getItemsAsArray()) {
               if (item.isLiquid()) {
                  Ingredient ii = recipe.findMatchingIngredient(item);
                  if (ii != null) {
                     liquid = (int)((float)liquid + (float)item.getWeightGrams() * ((float)(100 - ii.getLoss()) / 100.0F));
                  }
               } else {
                  newWeight += item.getWeightGrams();
               }
            }

            newWeight += liquid;
         }

         int diff = recipe.getDifficulty(realTarget);
         float howHard = (float)(recipe.getIngredientCount() + diff);
         if (template.isLiquid()) {
            howHard *= (float)newWeight / (float)template.getWeightGrams();
         }

         float power = 10.0F;
         if (primSkill != null) {
            power = (float)primSkill.skillCheck((double)((float)diff + alc), null, bonus, false, howHard);
         }

         byte material = recipe.getResultMaterial(realTarget);
         double avgQL = (double)MethodsItems.getAverageQL(null, realTarget);
         double ql = Math.min(99.0, Math.max(1.0, avgQL + (double)(power / 10.0F)));
         if (chefMade) {
            ql = Math.max(30.0, ql);
         }

         float maxMod = 1.0F;
         if (template.isLowNutrition()) {
            maxMod = 4.0F;
         } else if (template.isMediumNutrition()) {
            maxMod = 3.0F;
         } else if (template.isGoodNutrition()) {
            maxMod = 2.0F;
         } else if (template.isHighNutrition()) {
            maxMod = 1.0F;
         }

         if (primSkill != null) {
            ql = Math.max(1.0, Math.min(primSkill.getKnowledge(0.0) * (double)maxMod, ql));
         } else {
            ql = Math.max(1.0, Math.min((double)(20.0F * maxMod), ql));
         }

         if (realTarget.getRarity() > 0) {
            ql += (100.0 - ql) / 20.0 * (double)realTarget.getRarity();
         }

         try {
            byte rarity = 0;
            if (Server.rand.nextInt(500) == 0) {
               if (Server.rand.nextFloat() * 10000.0F <= 1.0F) {
                  rarity = 3;
               } else if (Server.rand.nextInt(100) <= 0) {
                  rarity = 2;
               } else if (Server.rand.nextBoolean()) {
                  rarity = 1;
               }
            }

            ql = (double)GeneralUtilities.calcRareQuality(ql, recipe.getLootableRarity(), realTarget.getRarity(), rarity);
            String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
            Item newItem = ItemFactory.createItem(template.getTemplateId(), (float)ql, material, rarity, owner);
            newItem.setIsSalted(ItemBehaviour.getSalted(null, realTarget));
            if (!realTarget.isFoodMaker() && !realTarget.getTemplate().isCooker()) {
               if (template.getTemplateId() == realTarget.getTemplateId()) {
                  newItem.setQualityLevel(realTarget.getQualityLevel());
                  newItem.setDamage(realTarget.getDamage());
               }

               newItem.setWeight(newWeight, true);
            } else {
               newItem.setWeight(newWeight, true);
            }

            if (newWeight >= 0 && template.getWeightGrams() != newWeight) {
               MethodsItems.setSizes(realTarget, newWeight, newItem);
            }

            if (RecipesByPlayer.saveRecipe(lastown, recipe, lastowner, null, realTarget) && lastown != null) {
               lastown.getCommunicator().sendServerMessage("Recipe \"" + recipe.getName() + "\" added to your cookbook.", 216, 165, 32, (byte)2);
            }

            newItem.calculateAndSaveNutrition(null, realTarget, recipe);
            if (lastown != null) {
               recipe.addAchievements(lastown, newItem);
            } else {
               recipe.addAchievementsOffline(lastowner, newItem);
            }

            newItem.setName(recipe.getResultName(realTarget));
            ItemTemplate rit = recipe.getResultRealTemplate(realTarget);
            if (rit != null) {
               newItem.setRealTemplate(rit.getTemplateId());
            }

            if (recipe.hasResultState()) {
               newItem.setAuxData(recipe.getResultState());
            }

            newItem.setTemperature((short)1500);
            if (realTarget.getTemplateId() == 1236 || realTarget.getTemplateId() == 1223) {
               for(Item item : realTarget.getItemsAsArray()) {
                  Items.destroyItem(item.getWurmId());
               }

               Item c = realTarget.getParentOrNull();
               if (c != null) {
                  Items.destroyItem(realTarget.getWurmId());
                  c.insertItem(newItem);
                  newItem.setLastOwnerId(lastowner);
               }
            } else if (!realTarget.isFoodMaker() && !realTarget.getTemplate().isCooker()) {
               Item c = realTarget.getParentOrNull();
               if (c != null) {
                  Items.destroyItem(realTarget.getWurmId());
                  c.insertItem(newItem);
                  newItem.setLastOwnerId(lastowner);
               }
            } else {
               long lastOwner = -10L;

               for(Item item : realTarget.getItemsAsArray()) {
                  Items.destroyItem(item.getWurmId());
               }

               if (newItem.isLiquid()) {
                  int volAvail = realTarget.getFreeVolume();
                  if (volAvail < newItem.getWeightGrams()) {
                     newItem.setWeight(volAvail, true);
                  }
               }

               realTarget.insertItem(newItem);
               newItem.setLastOwnerId(lastowner);
            }
         } catch (FailedException var39) {
            logger.log(Level.WARNING, var39.getMessage(), (Throwable)var39);
         } catch (NoSuchTemplateException var40) {
            logger.log(Level.WARNING, var40.getMessage(), (Throwable)var40);
         }
      }

      return false;
   }

   public static int getFoodTemplateFor(Item cookingItem) {
      switch(cookingItem.template.templateId) {
         case 75:
            return 347;
         case 77:
            return 346;
         case 287:
            return 345;
         case 350:
            return 348;
         case 351:
            return 352;
         default:
            if (logger.isLoggable(Level.FINER)) {
               logger.finer("Returning stew template for unexpected cookingItem: " + cookingItem);
            }

            return 345;
      }
   }

   static {
      addState(new TempState(38, 46, (short)4000, true, false, true));
      addState(new TempState(697, 698, (short)7000, true, false, true));
      addState(new TempState(693, 694, (short)8000, true, false, true));
      addState(new TempState(684, 46, (short)2000, true, false, true));
      addState(new TempState(43, 47, (short)4000, true, false, true));
      addState(new TempState(39, 44, (short)4000, true, false, true));
      addState(new TempState(40, 45, (short)4000, true, false, true));
      addState(new TempState(42, 48, (short)4000, true, false, true));
      addState(new TempState(41, 49, (short)4000, true, false, true));
      addState(new TempState(207, 220, (short)4000, true, false, true));
      addState(new TempState(769, 776, (short)6000, true, true, false));
      addState(new TempState(777, 778, (short)7000, true, true, false));
      addState(new TempState(181, 76, (short)4000, true, true, false));
      addState(new TempState(182, 77, (short)4000, true, true, false));
      addState(new TempState(183, 78, (short)4000, true, true, false));
      addState(new TempState(812, 813, (short)4000, true, true, false));
      addState(new TempState(1019, 1020, (short)8500, true, true, false));
      addState(new TempState(1021, 1022, (short)10000, true, true, false));
      addState(new TempState(789, 788, (short)4000, true, true, false));
      addState(new TempState(342, 343, (short)4000, true, true, false));
      addState(new TempState(225, 221, (short)3500, true, true, true));
      addState(new TempState(224, 223, (short)3500, true, true, true));
      addState(new TempState(699, 698, (short)5500, true, true, true));
      addState(new TempState(695, 694, (short)6000, true, true, true));
      addState(new TempState(170, 46, (short)3500, true, true, true));
      addState(new TempState(197, 45, (short)3500, true, true, true));
      addState(new TempState(195, 47, (short)3500, true, true, true));
      addState(new TempState(198, 48, (short)3500, true, true, true));
      addState(new TempState(199, 49, (short)3500, true, true, true));
      addState(new TempState(196, 44, (short)3500, true, true, true));
      addState(new TempState(222, 220, (short)3500, true, true, true));
      addState(new TempState(206, 205, (short)3500, true, true, true));
      addState(new TempState(763, 764, (short)1500, true, true, true));
      addState(new TempState(1160, 1161, (short)4000, true, true, false));
      addState(new TempState(1164, 1165, (short)5000, true, true, false));
      addState(new TempState(1168, 1169, (short)5000, true, true, false));
      addState(new TempState(1171, 1172, (short)5000, true, true, false));
      addState(new TempState(1251, 1252, (short)5500, true, true, false));
      addState(new TempState(1303, 1304, (short)4250, true, true, false));
   }
}
