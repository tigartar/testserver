/*
 * Decompiled with CFR 0.152.
 */
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
import com.wurmonline.server.items.Ingredient;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.RecipesByPlayer;
import com.wurmonline.server.items.TempState;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TempStates
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(TempStates.class.getName());
    private static final Set<TempState> tempStates = new HashSet<TempState>();

    private TempStates() {
    }

    public static void addState(TempState state) {
        tempStates.add(state);
    }

    public static Set<TempState> getTempStates() {
        return tempStates;
    }

    static boolean checkForChange(Item parent, Item target, short oldTemp, short newTemp, float qualityRatio) {
        for (TempState tempState : tempStates) {
            if (tempState.getOrigItemTemplateId() != target.getTemplateId()) continue;
            return tempState.changeItem(parent, target, oldTemp, newTemp, qualityRatio);
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
                for (Item i : parent.getItemsAsArray()) {
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
            float alc = 0.0f;
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
            }
            catch (NoSuchCreatureException skills) {
            }
            catch (NoSuchPlayerException skills) {
                // empty catch block
            }
            int newWeight = 0;
            if (realTarget.isFoodMaker() || realTarget.getTemplate().isCooker()) {
                int liquid = 0;
                for (Item item : realTarget.getItemsAsArray()) {
                    if (item.isLiquid()) {
                        Ingredient ii = recipe.findMatchingIngredient(item);
                        if (ii == null) continue;
                        liquid = (int)((float)liquid + (float)item.getWeightGrams() * ((float)(100 - ii.getLoss()) / 100.0f));
                        continue;
                    }
                    newWeight += item.getWeightGrams();
                }
                newWeight += liquid;
            } else {
                newWeight = realTarget.getWeightGrams();
            }
            int diff = recipe.getDifficulty(realTarget);
            float howHard = recipe.getIngredientCount() + diff;
            if (template.isLiquid()) {
                howHard *= (float)newWeight / (float)template.getWeightGrams();
            }
            float power = 10.0f;
            if (primSkill != null) {
                power = (float)primSkill.skillCheck((float)diff + alc, null, bonus, false, howHard);
            }
            byte material = recipe.getResultMaterial(realTarget);
            double avgQL = MethodsItems.getAverageQL(null, realTarget);
            double ql = Math.min(99.0, Math.max(1.0, avgQL + (double)(power / 10.0f)));
            if (chefMade) {
                ql = Math.max(30.0, ql);
            }
            float maxMod = 1.0f;
            if (template.isLowNutrition()) {
                maxMod = 4.0f;
            } else if (template.isMediumNutrition()) {
                maxMod = 3.0f;
            } else if (template.isGoodNutrition()) {
                maxMod = 2.0f;
            } else if (template.isHighNutrition()) {
                maxMod = 1.0f;
            }
            ql = primSkill != null ? Math.max(1.0, Math.min(primSkill.getKnowledge(0.0) * (double)maxMod, ql)) : Math.max(1.0, Math.min((double)(20.0f * maxMod), ql));
            if (realTarget.getRarity() > 0) {
                ql += (100.0 - ql) / 20.0 * (double)realTarget.getRarity();
            }
            try {
                byte rarity = 0;
                if (Server.rand.nextInt(500) == 0) {
                    if (Server.rand.nextFloat() * 10000.0f <= 1.0f) {
                        rarity = 3;
                    } else if (Server.rand.nextInt(100) <= 0) {
                        rarity = 2;
                    } else if (Server.rand.nextBoolean()) {
                        rarity = 1;
                    }
                }
                ql = GeneralUtilities.calcRareQuality(ql, recipe.getLootableRarity(), realTarget.getRarity(), rarity);
                String owner = showOwner ? PlayerInfoFactory.getPlayerName(lastowner) : null;
                Item newItem = ItemFactory.createItem(template.getTemplateId(), (float)ql, material, rarity, owner);
                newItem.setIsSalted(ItemBehaviour.getSalted(null, realTarget));
                if (realTarget.isFoodMaker() || realTarget.getTemplate().isCooker()) {
                    newItem.setWeight(newWeight, true);
                } else {
                    if (template.getTemplateId() == realTarget.getTemplateId()) {
                        newItem.setQualityLevel(realTarget.getQualityLevel());
                        newItem.setDamage(realTarget.getDamage());
                    }
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
                    for (Item item : realTarget.getItemsAsArray()) {
                        Items.destroyItem(item.getWurmId());
                    }
                    Item c = realTarget.getParentOrNull();
                    if (c != null) {
                        Items.destroyItem(realTarget.getWurmId());
                        c.insertItem(newItem);
                        newItem.setLastOwnerId(lastowner);
                    }
                } else if (realTarget.isFoodMaker() || realTarget.getTemplate().isCooker()) {
                    int volAvail;
                    long lastOwner = -10L;
                    for (Item item : realTarget.getItemsAsArray()) {
                        Items.destroyItem(item.getWurmId());
                    }
                    if (newItem.isLiquid() && (volAvail = realTarget.getFreeVolume()) < newItem.getWeightGrams()) {
                        newItem.setWeight(volAvail, true);
                    }
                    realTarget.insertItem(newItem);
                    newItem.setLastOwnerId(lastowner);
                } else {
                    Item c = realTarget.getParentOrNull();
                    if (c != null) {
                        Items.destroyItem(realTarget.getWurmId());
                        c.insertItem(newItem);
                        newItem.setLastOwnerId(lastowner);
                    }
                }
            }
            catch (FailedException fe) {
                logger.log(Level.WARNING, fe.getMessage(), fe);
            }
            catch (NoSuchTemplateException nste) {
                logger.log(Level.WARNING, nste.getMessage(), nste);
            }
        }
        return false;
    }

    public static int getFoodTemplateFor(Item cookingItem) {
        switch (cookingItem.template.templateId) {
            case 75: {
                return 347;
            }
            case 351: {
                return 352;
            }
            case 77: {
                return 346;
            }
            case 350: {
                return 348;
            }
            case 287: {
                return 345;
            }
        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Returning stew template for unexpected cookingItem: " + cookingItem);
        }
        return 345;
    }

    static {
        TempStates.addState(new TempState(38, 46, 4000, true, false, true));
        TempStates.addState(new TempState(697, 698, 7000, true, false, true));
        TempStates.addState(new TempState(693, 694, 8000, true, false, true));
        TempStates.addState(new TempState(684, 46, 2000, true, false, true));
        TempStates.addState(new TempState(43, 47, 4000, true, false, true));
        TempStates.addState(new TempState(39, 44, 4000, true, false, true));
        TempStates.addState(new TempState(40, 45, 4000, true, false, true));
        TempStates.addState(new TempState(42, 48, 4000, true, false, true));
        TempStates.addState(new TempState(41, 49, 4000, true, false, true));
        TempStates.addState(new TempState(207, 220, 4000, true, false, true));
        TempStates.addState(new TempState(769, 776, 6000, true, true, false));
        TempStates.addState(new TempState(777, 778, 7000, true, true, false));
        TempStates.addState(new TempState(181, 76, 4000, true, true, false));
        TempStates.addState(new TempState(182, 77, 4000, true, true, false));
        TempStates.addState(new TempState(183, 78, 4000, true, true, false));
        TempStates.addState(new TempState(812, 813, 4000, true, true, false));
        TempStates.addState(new TempState(1019, 1020, 8500, true, true, false));
        TempStates.addState(new TempState(1021, 1022, 10000, true, true, false));
        TempStates.addState(new TempState(789, 788, 4000, true, true, false));
        TempStates.addState(new TempState(342, 343, 4000, true, true, false));
        TempStates.addState(new TempState(225, 221, 3500, true, true, true));
        TempStates.addState(new TempState(224, 223, 3500, true, true, true));
        TempStates.addState(new TempState(699, 698, 5500, true, true, true));
        TempStates.addState(new TempState(695, 694, 6000, true, true, true));
        TempStates.addState(new TempState(170, 46, 3500, true, true, true));
        TempStates.addState(new TempState(197, 45, 3500, true, true, true));
        TempStates.addState(new TempState(195, 47, 3500, true, true, true));
        TempStates.addState(new TempState(198, 48, 3500, true, true, true));
        TempStates.addState(new TempState(199, 49, 3500, true, true, true));
        TempStates.addState(new TempState(196, 44, 3500, true, true, true));
        TempStates.addState(new TempState(222, 220, 3500, true, true, true));
        TempStates.addState(new TempState(206, 205, 3500, true, true, true));
        TempStates.addState(new TempState(763, 764, 1500, true, true, true));
        TempStates.addState(new TempState(1160, 1161, 4000, true, true, false));
        TempStates.addState(new TempState(1164, 1165, 5000, true, true, false));
        TempStates.addState(new TempState(1168, 1169, 5000, true, true, false));
        TempStates.addState(new TempState(1171, 1172, 5000, true, true, false));
        TempStates.addState(new TempState(1251, 1252, 5500, true, true, false));
        TempStates.addState(new TempState(1303, 1304, 4250, true, true, false));
    }
}

