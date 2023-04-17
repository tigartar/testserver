/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Ingredient;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.RecipesByPlayer;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.skills.SkillSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CookBookQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(CookBookQuestion.class.getName());
    private byte displayType;
    private final int sortBy;
    private Ingredient[] ingredients;
    private Ingredient ingred = null;
    private Recipe recip = null;
    private boolean showExtra = false;
    private boolean showLinks = true;
    private String from = "";
    private String searchFor = "";
    ArrayList<String> history = new ArrayList();
    private static final String red = "color=\"255,127,127\"";
    private static final String green = "color=\"127,255,127\"";
    public static final byte TYPE_INFO = 0;
    public static final byte TYPE_TARGET_ACTION_RECIPES = 1;
    public static final byte TYPE_CONTAINER_ACTION_RECIPES = 2;
    public static final byte TYPE_HEAT_RECIPES = 3;
    public static final byte TYPE_TIME_RECIPES = 4;
    public static final byte TYPE_COOKERS_LIST = 5;
    public static final byte TYPE_COOKER_RECIPES = 6;
    public static final byte TYPE_CONTAINERS_LIST = 7;
    public static final byte TYPE_CONTAINER_RECIPES = 8;
    public static final byte TYPE_TOOLS_LIST = 9;
    public static final byte TYPE_TOOL_RECIPES = 10;
    public static final byte TYPE_INGREDIENTS_LIST = 11;
    public static final byte TYPE_INGREDIENT_RECIPES = 12;
    public static final byte TYPE_RECIPE = 13;
    public static final byte TYPE_SEARCH_RECIPES = 14;
    public static final byte TYPE_BACK = 15;

    public CookBookQuestion(Creature aResponder, long aTargetId) {
        super(aResponder, aResponder.getName() + "'s CookBook", CookBookQuestion.makeTitle(aResponder, (byte)0, aTargetId), 135, aTargetId);
        this.sortBy = 1;
        if (aTargetId == -10L) {
            this.displayType = 0;
        } else {
            try {
                Item target = Items.getItem(aTargetId);
                this.displayType = target.getTemplate().isCooker() ? (byte)6 : (target.getTemplate().isCookingTool() ? (byte)10 : (byte)12);
            }
            catch (NoSuchItemException e) {
                this.displayType = 0;
            }
        }
    }

    public CookBookQuestion(Creature aResponder, byte aDisplayType, long aTargetId) {
        super(aResponder, aResponder.getName() + "'s Cookbook", CookBookQuestion.makeTitle(aResponder, aDisplayType, aTargetId), 135, aTargetId);
        this.sortBy = 1;
        this.displayType = aDisplayType;
    }

    public CookBookQuestion(Creature aResponder, byte aDisplayType, long aTargetId, int sortId) {
        super(aResponder, aResponder.getName() + "'s Cookbook", CookBookQuestion.makeTitle(aResponder, aDisplayType, aTargetId), 135, aTargetId);
        this.sortBy = sortId;
        this.displayType = aDisplayType;
    }

    public CookBookQuestion(Creature aResponder, byte aDisplayType, Ingredient ingredient) {
        super(aResponder, aResponder.getName() + "'s Cookbook", "List of recipes that use " + ingredient.getName(false) + ".", 135, -10L);
        this.sortBy = 1;
        this.displayType = aDisplayType;
        this.ingred = ingredient;
    }

    public CookBookQuestion(Creature aResponder, byte aDisplayType, Recipe recipe, boolean justRecipe, long aTarget, String signedBy) {
        super(aResponder, justRecipe ? recipe.getName() : aResponder.getName() + "'s Cookbook", "Recipe: " + recipe.getName(), 135, aTarget);
        this.sortBy = 1;
        this.displayType = aDisplayType;
        this.recip = recipe;
        boolean bl = this.showLinks = !justRecipe;
        if (signedBy.length() > 0) {
            this.from = signedBy;
        }
        if (justRecipe) {
            this.history.add(aDisplayType + "," + recipe.getRecipeId() + "," + justRecipe + "," + aTarget + "," + signedBy);
        }
    }

    public CookBookQuestion(Creature aResponder, String searchFor, int sortId) {
        super(aResponder, aResponder.getName() + "'s Cookbook", searchFor.length() > 0 ? "List of recipes that have a name with " + searchFor + " in it." : "List of all your known recipes.", 135, -10L);
        this.sortBy = sortId;
        this.displayType = (byte)14;
        this.searchFor = searchFor;
    }

    static String makeTitle(Creature aResponder, byte aType, long templateId) {
        switch (aType) {
            case 0: {
                return aResponder.getName() + "'s Cookbook";
            }
            case 1: {
                return "Target Action Recipe List";
            }
            case 2: {
                return "Container Action Recipe List";
            }
            case 3: {
                return "Heat Recipe List";
            }
            case 4: {
                return "Time Recipe List";
            }
            case 5: {
                return "List of known cookers";
            }
            case 6: {
                ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplateOrNull((int)templateId);
                String cookerName = cookerIT == null ? "xxxx" : cookerIT.getName();
                return "Recipes made in " + cookerName;
            }
            case 7: {
                return "List of known containers";
            }
            case 8: {
                ItemTemplate containerIT = ItemTemplateFactory.getInstance().getTemplateOrNull((int)templateId);
                String cookerName = containerIT == null ? "xxxx" : containerIT.getName();
                return "Recipes made in " + cookerName;
            }
            case 9: {
                return "List of known tools";
            }
            case 10: {
                ItemTemplate toolIT = ItemTemplateFactory.getInstance().getTemplateOrNull((int)templateId);
                String toolName = toolIT == null ? "xxxx" : toolIT.getName();
                return "Recipes made with a " + toolName;
            }
            case 11: {
                return "List of known ingredients";
            }
            case 12: {
                return "List of Recipes that use a xxxx";
            }
            case 14: {
                return "List of recipes that were found with xxxx in";
            }
        }
        return "";
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 135) {
            String[] parts;
            CookBookQuestion cbq;
            boolean close = this.getBooleanProp("close");
            if (close) {
                return;
            }
            boolean add = this.getBooleanProp("add");
            if (add && this.target != -10L) {
                try {
                    Item paper = Items.getItem(this.target);
                    Item parent = paper.getTopParentOrNull();
                    if (parent != null && parent.isInventory()) {
                        if (RecipesByPlayer.addRecipe(this.getResponder(), this.recip)) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("You finish adding the " + this.recip.getName() + " into your cookbook, just in time, as the recipe has decayed away.");
                            Server.getInstance().broadCastAction(this.getResponder().getName() + " stops writing.", this.getResponder(), 5);
                            Items.destroyItem(this.target);
                            this.getResponder().getCommunicator().sendCookbookRecipe(this.recip);
                        }
                    } else {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find the recipe on you!");
                    }
                }
                catch (NoSuchItemException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
                return;
            }
            for (String key : this.getAnswer().stringPropertyNames()) {
                if (!key.startsWith("sort")) continue;
                String sid = key.substring(4);
                int newSort = Integer.parseInt(sid);
                if (this.searchFor.equalsIgnoreCase("")) {
                    CookBookQuestion cbq2 = new CookBookQuestion(this.getResponder(), this.displayType, this.target, newSort);
                    cbq2.history = this.history;
                    cbq2.ingred = this.ingred;
                    cbq2.sendQuestion();
                    return;
                }
                CookBookQuestion cbq3 = new CookBookQuestion(this.getResponder(), this.searchFor, newSort);
                cbq3.history = this.history;
                cbq3.ingred = this.ingred;
                cbq3.sendQuestion();
                return;
            }
            if (this.getBooleanProp("find")) {
                String srch = aAnswer.getProperty("search");
                if (srch != null) {
                    CookBookQuestion cbq4 = new CookBookQuestion(this.getResponder(), srch, 0);
                    cbq4.history = this.history;
                    cbq4.history.add("14," + srch);
                    cbq4.sendQuestion();
                    return;
                }
            } else if (this.getBooleanProp("remove")) {
                RecipesByPlayer.removeRecipeForPlayer(this.getResponder().getWurmId(), this.recip.getRecipeId());
                this.getResponder().getCommunicator().sendCookbookRecipe(this.recip);
            } else if (this.getBooleanProp("back")) {
                if (this.history.size() < 2) {
                    cbq = new CookBookQuestion(this.getResponder(), 0L);
                    cbq.history.add("0");
                    cbq.sendQuestion();
                    return;
                }
                this.history.remove(this.history.size() - 1);
                String last = this.history.get(this.history.size() - 1);
                parts = last.split(",");
                byte type = Byte.parseByte(parts[0]);
                switch (type) {
                    case 0: {
                        CookBookQuestion cbq5 = new CookBookQuestion(this.getResponder(), 0L);
                        cbq5.history.add("0");
                        cbq5.sendQuestion();
                        return;
                    }
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                    case 9: 
                    case 10: 
                    case 11: {
                        int id = Integer.parseInt(parts[1]);
                        CookBookQuestion cbq6 = new CookBookQuestion(this.getResponder(), type, (long)id);
                        cbq6.history = this.history;
                        cbq6.sendQuestion();
                        return;
                    }
                    case 12: {
                        int templateId = Integer.parseInt(parts[1]);
                        byte cstate = Byte.parseByte(parts[2]);
                        byte pstate = Byte.parseByte(parts[3]);
                        byte material = Byte.parseByte(parts[4]);
                        boolean hasRealTemplate = Boolean.parseBoolean(parts[5]);
                        int realTemplateId = Integer.parseInt(parts[6]);
                        int corpseData = Integer.parseInt(parts[7]);
                        try {
                            Ingredient ingredient = new Ingredient(templateId, cstate, pstate, material, hasRealTemplate, realTemplateId, corpseData);
                            CookBookQuestion cbq7 = new CookBookQuestion(this.getResponder(), type, ingredient);
                            cbq7.history = this.history;
                            cbq7.sendQuestion();
                            return;
                        }
                        catch (NoSuchTemplateException e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                    case 13: {
                        int id = Integer.parseInt(parts[1]);
                        Recipe recipe = RecipesByPlayer.getRecipe(this.getResponder().getWurmId(), id);
                        CookBookQuestion cbq8 = new CookBookQuestion(this.getResponder(), 13, recipe, false, -10L, "");
                        cbq8.history = this.history;
                        cbq8.sendQuestion();
                        return;
                    }
                    case 14: {
                        String sFor = parts.length > 1 ? parts[1] : "";
                        CookBookQuestion cbq9 = new CookBookQuestion(this.getResponder(), sFor, 0);
                        cbq9.history = this.history;
                        cbq9.sendQuestion();
                        return;
                    }
                }
            } else if (this.getBooleanProp("show")) {
                String sel = aAnswer.getProperty("sel");
                parts = sel.split(",");
                byte type = Byte.parseByte(parts[0]);
                if (type == 12) {
                    int templateId = Integer.parseInt(parts[1]);
                    byte cstate = Byte.parseByte(parts[2]);
                    byte pstate = Byte.parseByte(parts[3]);
                    byte material = Byte.parseByte(parts[4]);
                    boolean hasRealTemplate = Boolean.parseBoolean(parts[5]);
                    int realTemplateId = Integer.parseInt(parts[6]);
                    int corpseData = Integer.parseInt(parts[7]);
                    try {
                        Ingredient ingredient = new Ingredient(templateId, cstate, pstate, material, hasRealTemplate, realTemplateId, corpseData);
                        CookBookQuestion cbq10 = new CookBookQuestion(this.getResponder(), type, ingredient);
                        cbq10.history = this.history;
                        this.history.add("12," + templateId + "," + cstate + "," + pstate + "," + material + "," + hasRealTemplate + "," + realTemplateId + "," + corpseData);
                        cbq10.sendQuestion();
                        return;
                    }
                    catch (NoSuchTemplateException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                } else {
                    if (type == 13) {
                        int id = Integer.parseInt(parts[1]);
                        Recipe recipe = RecipesByPlayer.getRecipe(this.getResponder().getWurmId(), id);
                        CookBookQuestion cbq11 = new CookBookQuestion(this.getResponder(), 13, recipe, false, -10L, "");
                        cbq11.history = this.history;
                        this.history.add("13," + recipe.getRecipeId() + "," + false + "," + id + ",");
                        cbq11.sendQuestion();
                        return;
                    }
                    int id = Integer.parseInt(parts[1]);
                    CookBookQuestion cbq12 = new CookBookQuestion(this.getResponder(), type, (long)id);
                    cbq12.history = this.history;
                    cbq12.history.add(type + "," + id);
                    cbq12.sendQuestion();
                    return;
                }
            }
            cbq = new CookBookQuestion(this.getResponder(), 0L);
            cbq.history.add("0");
            cbq.sendQuestion();
            return;
        }
    }

    @Override
    public void sendQuestion() {
        this.showExtra = this.getResponder().getPower() > 4 && Servers.isThisATestServer();
        switch (this.displayType) {
            case 0: {
                this.sendInfo();
                break;
            }
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 6: 
            case 8: 
            case 10: 
            case 12: 
            case 14: {
                this.sendRecipes();
                break;
            }
            case 5: 
            case 7: 
            case 9: 
            case 11: {
                this.sendList();
                break;
            }
            case 13: {
                this.sendRecipe();
            }
        }
    }

    public void sendInfo() {
        StringBuilder buf = new StringBuilder();
        String closeBtn = "harray{label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};label{text=\" \"}};";
        buf.append("border{border{size=\"20,20\";null;null;center{varray{header{text=\"" + this.question + "\"}}};" + "harray{label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};label{text=\" \"}};" + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        buf.append("label{type=\"bold\";text=\"Recipes are split into various categories, these are:\"};");
        buf.append("radio{group=\"sel\";id=\"0,-10\";selected=\"true\";hidden=\"true\"};");
        buf.append("table{rows=\"4\";cols=\"2\";");
        buf.append("radio{group=\"sel\";id=\"1,-10\"};label{text=\"Target actions\";hover=\"e.g. ones where you use one item on another.\"};");
        buf.append("radio{group=\"sel\";id=\"2,-10\"};label{text=\"Container actions\";hover=\"e.g. when you use an item on a container.\"};");
        buf.append("radio{group=\"sel\";id=\"3,-10\"};label{text=\"Heat \";hover=\"normal cooking ones.\"};");
        buf.append("radio{group=\"sel\";id=\"4,-10\"};label{text=\"Time \";hover=\"ones that take time e.g. brewing).\"};");
        buf.append("}");
        buf.append("label{text=\"Selecting one of the above types will give a list of the recipes that you know about of that type.\"};");
        buf.append("label{text=\"\"};");
        buf.append("label{type=\"bold\";text=\"Or you can get a list of:\"};");
        buf.append("table{rows=\"4\";cols=\"2\";");
        buf.append("radio{group=\"sel\";id=\"5,-10\"};label{text=\"Cookers\"};");
        buf.append("radio{group=\"sel\";id=\"7,-10\"};label{text=\"Containers\"};");
        buf.append("radio{group=\"sel\";id=\"9,-10\"};label{text=\"Tools\"};");
        buf.append("radio{group=\"sel\";id=\"11,-10\"};label{text=\"Ingredients\"};");
        buf.append("}");
        buf.append("text{text=\"Selecting one of the above searches will give a list of those items that are used in that category that you know about.\"};");
        buf.append("label{text=\"\"};");
        buf.append("harray{label{type=\"bold\";text=\"Select what you want to do above and click :\"};button{text=\"here\";id=\"show\";default=\"true\"}}");
        buf.append("label{text=\"\"};");
        buf.append("harray{label{text=\"Or you can \"}button{text=\"search\";id=\"find\";hover=\"Dont forget to add a search criteria.\"}label{text=\" for \"}input{maxchars=\"20\";id=\"search\";text=\"\";onenter=\"find\"}label{text=\" in your known recipe names.\"}}");
        buf.append("label{text=\"If you leave the input box blank and do a search, it will list all of your known recipes.\"};");
        buf.append("label{text=\"\"};");
        Recipe[] knownrecipes = RecipesByPlayer.getSearchRecipesFor(this.getResponder().getWurmId(), "");
        buf.append("label{text=\"You know a total of " + knownrecipes.length + " recipes\"};");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 480, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendRecipes() {
        StringBuilder buf = new StringBuilder();
        String closeBtn = "harray{label{text=\" \"};harray{button{text=\"Back\";id=\"back\";hover=\"Go back to last screen.\"};label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};};label{text=\" \"}};";
        buf.append("border{border{size=\"20,25\";null;null;label{type=\"bold\";text=\"" + this.question + (this.showExtra && this.target != -10L ? " - " + this.target : "") + "\"};" + "harray{label{text=\" \"};harray{button{text=\"Back\";id=\"back\";hover=\"Go back to last screen.\"};label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};};label{text=\" \"}};" + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        int width = 470;
        int height = 400;
        String mid = "null;";
        boolean defaultShow = true;
        switch (this.displayType) {
            case 1: {
                buf.append(this.sendTargetActionRecipes());
                break;
            }
            case 2: {
                buf.append(this.sendContainerActionRecipes());
                width = 730;
                break;
            }
            case 3: {
                buf.append(this.sendHeatRecipes());
                width = 730;
                break;
            }
            case 4: {
                buf.append(this.sendTimeRecipes());
                break;
            }
            case 6: {
                buf.append(this.sendCookerRecipes());
                break;
            }
            case 8: {
                buf.append(this.sendContainerRecipes());
                break;
            }
            case 10: {
                buf.append(this.sendToolRecipes());
                break;
            }
            case 12: {
                buf.append(this.sendIngredientRecipes());
                break;
            }
            case 14: {
                buf.append(this.sendSearchRecipes());
                mid = "center{harray{button{text=\"Search\";id=\"find\";default=\"true\"};label{text=\" \"};input{maxchars=\"20\";id=\"search\";text=\"" + this.searchFor + "\"}}};";
                defaultShow = false;
            }
        }
        buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";hidden=\"true\";text=\"None\"}");
        buf.append("text{text=\"\"}");
        buf.append("}};null;");
        buf.append("border{size=\"20,20\";null;harray{label{text=\" \"};button{text=\"Show selected\";id=\"show\"" + (defaultShow ? "default=\"true\"" : "") + "}};" + mid + "harray{button{text=\"Go to info\";id=\"info\"};label{text=\" \"}};null;}");
        buf.append("}");
        this.getResponder().getCommunicator().sendBml(width, height, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    private void sendList() {
        StringBuilder buf = new StringBuilder();
        String closeBtn = "harray{label{text=\" \"};harray{button{text=\"Back\";id=\"back\";hover=\"Go back to last screen.\"};label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};};label{text=\" \"}};";
        buf.append("border{border{size=\"20,25\";null;null;label{type=\"bold\";text=\"" + this.question + "          \"};" + "harray{label{text=\" \"};harray{button{text=\"Back\";id=\"back\";hover=\"Go back to last screen.\"};label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};};label{text=\" \"}};" + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        int width = 400;
        int height = 300;
        switch (this.displayType) {
            case 5: {
                buf.append(this.sendCookersList());
                break;
            }
            case 7: {
                buf.append(this.sendContainersList());
                break;
            }
            case 9: {
                buf.append(this.sendToolsList());
                break;
            }
            case 11: {
                buf.append(this.sendIngredientsList());
                width = this.showExtra ? 550 : 450;
                height = 450;
            }
        }
        buf.append("radio{group=\"sel\";id=\"-10,-10\";selected=\"true\";hidden=\"true\";text=\"None\"}");
        buf.append("text{text=\"\"}");
        buf.append("}};null;");
        buf.append("border{size=\"20,20\";null;harray{label{text=\" \"};button{text=\"Show selected\";id=\"show\"}};null;harray{button{text=\"Go to info\";id=\"info\"};label{text=\" \"}};null;}");
        buf.append("}");
        this.getResponder().getCommunicator().sendBml(width, height, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    public String sendTargetActionRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getTargetActionRecipesFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getActiveItem().getName(false).compareTo(param2.getActiveItem().getName(false)) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getTargetItem().getName(false).compareTo(param2.getTargetItem().getName(false)) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"6\";label{text=\" \"};" + this.colHeader("Recipe Name", 1, this.sortBy) + "label{text=\" \"};" + this.colHeader("Active Item", 2, this.sortBy) + "label{text=\" \"};" + this.colHeader("Target Item", 3, this.sortBy));
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, recipe.getName(), false) + (recipe.getActiveItem() == null ? "label{text=\"\"}" : (recipe.getActiveItem().getTemplate().isCookingTool() ? "radio{group=\"sel\";id=\"10," + recipe.getActiveItem().getTemplateId() + "\";text=\"\"}" : "radio{group=\"sel\";id=\"12," + recipe.getActiveItem().getTemplateId() + "," + recipe.getActiveItem().getCState() + "," + recipe.getActiveItem().getPState() + "," + recipe.getActiveItem().getMaterial() + "," + recipe.getActiveItem().hasRealTemplate() + "," + recipe.getActiveItem().getRealTemplateId() + "," + recipe.getActiveItem().getCorpseData() + "\";text=\"\"}")) + "label{text=\"" + Recipes.getIngredientName(recipe.getActiveItem()) + (this.showExtra && recipe.getActiveItem() != null ? " - " + recipe.getActiveItem().getTemplateId() : "") + "\"};" + (recipe.getTargetItem() == null ? "label{text=\"\"}" : (recipe.getTargetItem().getTemplate().isFoodMaker() ? "radio{group=\"sel\";id=\"8," + recipe.getTargetItem().getTemplateId() + "\";text=\"\"}" : "radio{group=\"sel\";id=\"12," + recipe.getTargetItem().getTemplateId() + "," + recipe.getTargetItem().getCState() + "," + recipe.getTargetItem().getPState() + "," + recipe.getTargetItem().getMaterial() + "," + recipe.getTargetItem().hasRealTemplate() + "," + recipe.getTargetItem().getRealTemplateId() + "," + recipe.getTargetItem().getCorpseData() + "\";text=\"\"}")) + "label{text=\"" + Recipes.getIngredientName(recipe.getTargetItem()) + (this.showExtra && recipe.getTargetItem() != null ? " - " + recipe.getTargetItem().getTemplateId() : "") + "\"};");
        }
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any target action recipes.\"}");
        }
        return buf.toString();
    }

    public String sendContainerActionRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getContainerActionRecipesFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return Recipes.getIngredientName(param1.getActiveItem()).compareTo(Recipes.getIngredientName(param2.getActiveItem())) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getContainersAsString().compareTo(param2.getContainersAsString()) * upDown;
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getIngredientsAsString().compareTo(param2.getIngredientsAsString()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"8\";label{text=\" \"};" + this.colHeader("Recipe Name", 1, this.sortBy) + "label{text=\" \"};" + this.colHeader("Active Item", 2, this.sortBy) + "label{text=\" \"};" + this.colHeader("Containers", 3, this.sortBy) + "label{text=\" \"};" + this.colHeader("Ingredients", 4, this.sortBy));
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, recipe.getName(), false) + (recipe.getActiveItem() == null ? "label{text=\"\"}" : (recipe.getActiveItem().getTemplate().isCookingTool() ? "radio{group=\"sel\";id=\"9," + recipe.getActiveItem().getTemplateId() + "\";text=\"\"}" : "radio{group=\"sel\";id=\"12," + recipe.getActiveItem().getTemplateId() + "," + recipe.getActiveItem().getCState() + "," + recipe.getActiveItem().getPState() + "," + recipe.getActiveItem().getMaterial() + "," + recipe.getActiveItem().hasRealTemplate() + "," + recipe.getActiveItem().getRealTemplateId() + "," + recipe.getActiveItem().getCorpseData() + "\";text=\"\"}")) + "label{text=\"" + recipe.getActiveItemName() + (this.showExtra && recipe.getActiveItem() != null ? " - " + recipe.getActiveItem().getTemplateId() : "") + "\"};" + (!recipe.hasOneContainer() ? "label{text=\"\"}" : "radio{group=\"sel\";id=\"7," + recipe.getContainerId() + "\";text=\"\"}") + "label{text=\"" + recipe.getContainersAsString() + "\"};label{text=\" \"};label{text=\"" + recipe.getIngredientsAsString() + "\"};");
        }
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any container action recipes.\"}");
        }
        return buf.toString();
    }

    public String sendHeatRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getHeatRecipesFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getCookersAsString().compareTo(param2.getCookersAsString()) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getContainersAsString().compareTo(param2.getContainersAsString()) * upDown;
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getIngredientsAsString().compareTo(param2.getIngredientsAsString()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"8\";label{text=\" \"};" + this.colHeader("Recipe Name", 1, this.sortBy) + "label{text=\" \"};" + this.colHeader("Cookers List", 2, this.sortBy) + "label{text=\" \"};" + this.colHeader("Containers List", 3, this.sortBy) + "label{text=\" \"};" + this.colHeader("Ingredients List", 4, this.sortBy));
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, recipe.getName(), false) + (!recipe.hasOneCooker() ? "label{text=\"\"}" : "radio{group=\"sel\";id=\"6," + recipe.getCookerId() + "\";text=\"\"}") + "label{text=\"" + recipe.getCookersAsString() + "\"};" + (!recipe.hasOneContainer() ? "label{text=\"\"}" : "radio{group=\"sel\";id=\"8," + recipe.getContainerId() + "\";text=\"\"}") + "label{text=\"" + recipe.getContainersAsString() + "\"};label{text=\" \"};label{text=\"" + recipe.getIngredientsAsString() + "\"};");
        }
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any heat recipes.\"}");
        }
        return buf.toString();
    }

    public String sendTimeRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getTimeRecipesFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getContainersAsString().compareTo(param2.getContainersAsString()) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getIngredientsAsString().compareTo(param2.getIngredientsAsString()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"6\";label{text=\" \"};" + this.colHeader("Recipe Name", 1, this.sortBy) + "label{text=\" \"};" + this.colHeader("Containers", 2, this.sortBy) + "label{text=\" \"};" + this.colHeader("Ingredients", 3, this.sortBy));
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\" \"}" + this.colourRecipeName(recipe, recipe.getName(), false) + (!recipe.hasOneContainer() ? "label{text=\" \"}" : "radio{group=\"sel\";id=\"7," + recipe.getContainerId() + "\";text=\"\"}") + "label{text=\"" + recipe.getContainersAsString() + "\"};label{text=\"\"};label{text=\"" + recipe.getIngredientsAsString() + "\"};");
        }
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any time recipes.\"}");
        }
        return buf.toString();
    }

    public String sendCookersList() {
        StringBuilder buf = new StringBuilder();
        ItemTemplate[] templates = RecipesByPlayer.getKnownCookersFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        if (param1.getTemplateId() < param2.getTemplateId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"3\";label{text=\" \"};" + this.colHeader("Cooker Name", 1, this.sortBy) + "label{text=\" \"};");
        for (ItemTemplate itemTemplate : templates) {
            buf.append("radio{group=\"sel\";id=\"6," + itemTemplate.getTemplateId() + "\";text=\" \"}label{text=\"" + itemTemplate.getName() + (this.showExtra ? " - " + itemTemplate.getTemplateId() : "") + "\"};label{text=\"\"};");
        }
        buf.append("}");
        if (templates.length == 0) {
            buf.append("label{text=\"You dont know any cookers.\"}");
        }
        return buf.toString();
    }

    public String sendCookerRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getCookerRecipesFor(this.getResponder().getWurmId(), (int)this.target);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Recipe Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"6\";");
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\" \"}" + this.colourRecipeName(recipe, recipe.getName(), false));
        }
        int rem = recipes.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any recipes using that cooker.\"}");
        }
        return buf.toString();
    }

    public String sendContainersList() {
        StringBuilder buf = new StringBuilder();
        ItemTemplate[] templates = RecipesByPlayer.getKnownContainersFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        if (param1.getTemplateId() < param2.getTemplateId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"3\";label{text=\" \"};" + this.colHeader("Container Name", 1, this.sortBy) + "label{text=\" \"};");
        for (ItemTemplate itemTemplate : templates) {
            buf.append("radio{group=\"sel\";id=\"8," + itemTemplate.getTemplateId() + "\";text=\"\"}label{text=\"" + itemTemplate.getName() + (this.showExtra ? " - " + itemTemplate.getTemplateId() : "") + "\"};label{text=\"\"};");
        }
        buf.append("}");
        if (templates.length == 0) {
            buf.append("label{text=\"You dont know any containers.\"}");
        }
        return buf.toString();
    }

    public String sendContainerRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getContainerRecipesFor(this.getResponder().getWurmId(), (int)this.target);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Recipe Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"6\";");
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\" \"}" + this.colourRecipeName(recipe, recipe.getName(), false));
        }
        int rem = recipes.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any recipes using that container.\"}");
        }
        return buf.toString();
    }

    public String sendToolsList() {
        StringBuilder buf = new StringBuilder();
        ItemTemplate[] templates = RecipesByPlayer.getKnownToolsFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        if (param1.getTemplateId() < param2.getTemplateId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(templates, new Comparator<ItemTemplate>(){

                    @Override
                    public int compare(ItemTemplate param1, ItemTemplate param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"3\";label{text=\" \"};" + this.colHeader("Tool Name", 1, this.sortBy) + "label{text=\" \"};");
        for (ItemTemplate itemTemplate : templates) {
            buf.append("radio{group=\"sel\";id=\"10," + itemTemplate.getTemplateId() + "\";text=\"\"}label{text=\"" + itemTemplate.getName() + (this.showExtra ? " - " + itemTemplate.getTemplateId() : "") + "\"};label{text=\"\"};");
        }
        buf.append("}");
        if (templates.length == 0) {
            buf.append("label{text=\"You dont know any cooking tools.\"}");
        }
        return buf.toString();
    }

    public String sendToolRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getToolRecipesFor(this.getResponder().getWurmId(), (int)this.target);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Recipe Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"6\";");
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, recipe.getName(), false));
        }
        int rem = recipes.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any recipes using that tool.\"}");
        }
        return buf.toString();
    }

    public String sendIngredientsList() {
        StringBuilder buf = new StringBuilder();
        this.ingredients = RecipesByPlayer.getKnownIngredientsFor(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: 
            case 1: {
                Arrays.sort(this.ingredients, new Comparator<Ingredient>(){

                    @Override
                    public int compare(Ingredient param1, Ingredient param2) {
                        return Recipes.getIngredientName(param1).compareTo(Recipes.getIngredientName(param2)) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Ingredient Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"9\";");
        for (Ingredient ingredient : this.ingredients) {
            buf.append("radio{group=\"sel\";id=\"12," + ingredient.getTemplateId() + "," + ingredient.getCState() + "," + ingredient.getPState() + "," + ingredient.getMaterial() + "," + ingredient.hasRealTemplate() + "," + ingredient.getRealTemplateId() + "," + ingredient.getCorpseData() + "\";text=\"\"}label{text=\"" + Recipes.getIngredientName(ingredient) + (this.showExtra ? " - " + ingredient.getTemplateId() : "") + "\"};label{text=\"\"};");
        }
        int rem = this.ingredients.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (this.ingredients.length == 0) {
            buf.append("label{text=\"You dont know any ingredients.\"}");
        }
        return buf.toString();
    }

    public String sendIngredientRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getIngredientRecipesFor(this.getResponder().getWurmId(), this.ingred);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Recipe Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"6\";");
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\" \"}" + this.colourRecipeName(recipe, recipe.getName(), false));
        }
        int rem = recipes.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any recipes using that ingredient.\"}");
        }
        return buf.toString();
    }

    public String sendSearchRecipes() {
        StringBuilder buf = new StringBuilder();
        Recipe[] recipes = RecipesByPlayer.getSearchRecipesFor(this.getResponder().getWurmId(), this.searchFor);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 0: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        if (param1.getRecipeId() < param2.getRecipeId()) {
                            return -1;
                        }
                        return 1;
                    }
                });
                break;
            }
            case 1: {
                Arrays.sort(recipes, new Comparator<Recipe>(){

                    @Override
                    public int compare(Recipe param1, Recipe param2) {
                        return param1.getName().compareTo(param2.getName()) * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"1\";" + this.colHeader("Recipe Name", 1, this.sortBy));
        buf.append("table{rows=\"1\";cols=\"6\";");
        for (Recipe recipe : recipes) {
            buf.append("radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\" \"}" + this.colourRecipeName(recipe, recipe.getName(), false));
        }
        int rem = recipes.length % 3;
        if (rem > 0) {
            for (int i = 0; i < 3 - rem; ++i) {
                buf.append("label{text=\"\"};label{text=\"\"}");
            }
        }
        buf.append("}");
        buf.append("}");
        if (recipes.length == 0) {
            buf.append("label{text=\"You dont know any recipes using that ingredient.\"}");
        }
        return buf.toString();
    }

    /*
     * WARNING - void declaration
     */
    public void sendRecipe() {
        void var8_17;
        Iterator<ItemTemplate> link;
        Recipe recipe;
        if (RecipesByPlayer.isKnownRecipe(this.getResponder().getWurmId(), this.recip.getRecipeId())) {
            this.ingredients = RecipesByPlayer.getRecipeIngredientsFor(this.getResponder().getWurmId(), this.recip.getRecipeId());
        } else {
            Map<String, Ingredient> knownIngredients = this.recip.getAllIngredients(true);
            this.ingredients = knownIngredients.values().toArray(new Ingredient[knownIngredients.size()]);
        }
        Arrays.sort(this.ingredients, new Comparator<Ingredient>(){

            @Override
            public int compare(Ingredient param1, Ingredient param2) {
                return Recipes.getIngredientName(param1).compareTo(Recipes.getIngredientName(param2));
            }
        });
        Arrays.sort(this.ingredients, new Comparator<Ingredient>(){

            @Override
            public int compare(Ingredient param1, Ingredient param2) {
                return Byte.valueOf(param1.getGroupId()).compareTo(param2.getGroupId());
            }
        });
        String name = this.colourRecipeName(this.recip, this.question, true);
        StringBuilder buf = new StringBuilder();
        String closeBtn = "harray{label{text=\" \"};harray{" + (this.showLinks ? "button{text=\"Back\";id=\"back\";hover=\"Go back to last screen.\"};" : "") + "label{text=\" \"};button{text=\"Close\";id=\"close\";hover=\"Close the cookbook.\"};};label{text=\" \"}};";
        buf.append("border{border{size=\"20,25\";null;null;" + name + closeBtn + "null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        buf.append("table{rows=\"1\";cols=\"5\";");
        int type = 1;
        if (this.recip.isContainerActionType()) {
            type = 2;
        } else if (this.recip.isHeatType()) {
            type = 3;
        } else if (this.recip.isTimeType()) {
            type = 4;
        }
        int lines = 7;
        buf.append("label{text=\"Type:\"}");
        buf.append(this.showLinks ? "radio{group=\"sel\";id=\"" + type + "," + -10L + "\"}" : "label{text=\"\"}");
        buf.append("label{text=\"" + this.recip.getTriggerName().toLowerCase() + "\"}");
        buf.append("label{text=\"\"}");
        buf.append("label{text=\"\"}");
        if (this.recip.getSkillId() != -1) {
            ++lines;
            buf.append("label{text=\"Skill:\"}");
            buf.append("label{text=\"\"}");
            buf.append("label{text=\"" + SkillSystem.getNameFor(this.recip.getSkillId()).toLowerCase() + "\"}");
            buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(this.recip.getSkillId()) : "") + "\"}");
            buf.append("label{text=\"\"}");
        }
        if (this.recip.getActiveItem() != null) {
            ++lines;
            if (this.recip.getActiveItem().getTemplate().isCookingTool()) {
                buf.append("label{text=\"Tool:\"}");
                buf.append(this.showLinks ? "radio{group=\"sel\";id=\"10," + this.recip.getActiveItem().getTemplateId() + "\"}" : "label{text=\"\"}");
                buf.append("label{text=\"" + this.recip.getActiveItem().getName(false) + "\"}");
                buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(this.recip.getActiveItem().getTemplateId()) : "") + "\"}");
                buf.append("label{text=\"\"}");
            } else {
                recipe = Recipes.getRecipeByResult(this.recip.getActiveItem());
                link = recipe == null ? "label{text=\" \"}" : (RecipesByPlayer.isKnownRecipe(this.getResponder().getWurmId(), recipe.getRecipeId()) ? "harray{radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, "Show recipe", false) + "}" : "label{color=\"255,127,127\";text=\"Unknown recipe" + (this.showExtra ? " - " + recipe.getRecipeId() : "") + "\"}");
                buf.append("label{text=\"Active Item:\"}");
                buf.append(this.showLinks ? "radio{group=\"sel\";id=\"12," + this.recip.getActiveItem().getTemplateId() + "," + this.recip.getActiveItem().getCState() + "," + this.recip.getActiveItem().getPState() + "," + this.recip.getActiveItem().getMaterial() + "," + this.recip.getActiveItem().hasRealTemplate() + "," + this.recip.getActiveItem().getRealTemplateId() + "," + this.recip.getActiveItem().getCorpseData() + "\";text=\"\"}" : "label{text=\"\"}");
                buf.append("label{text=\"" + Recipes.getIngredientName(this.recip.getActiveItem()) + "\"}");
                buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(this.recip.getActiveItem().getTemplateId()) : "") + "\"}");
                buf.append((String)((Object)link));
            }
        }
        if (this.recip.getTargetItem() != null) {
            ++lines;
            recipe = Recipes.getRecipeByResult(this.recip.getTargetItem());
            link = recipe == null ? "label{text=\" \"}" : (RecipesByPlayer.isKnownRecipe(this.getResponder().getWurmId(), recipe.getRecipeId()) ? "harray{radio{group=\"sel\";id=\"13," + recipe.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe, "Show recipe", false) + "}" : "label{color=\"255,127,127\";text=\"Unknown recipe" + (this.showExtra ? " - " + recipe.getRecipeId() : "") + "\"}");
            buf.append("label{text=\"Target Item:\"}");
            buf.append(this.showLinks ? "radio{group=\"sel\";id=\"12," + this.recip.getTargetItem().getTemplateId() + "," + this.recip.getTargetItem().getCState() + "," + this.recip.getTargetItem().getPState() + "," + this.recip.getTargetItem().getMaterial() + "," + this.recip.getTargetItem().hasRealTemplate() + "," + this.recip.getTargetItem().getRealTemplateId() + "," + this.recip.getTargetItem().getCorpseData() + "\";text=\"\"}" : "label{text=\"\"}");
            buf.append("label{text=\"" + Recipes.getIngredientName(this.recip.getTargetItem()) + "\"}");
            buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(this.recip.getTargetItem().getTemplateId()) : "") + "\"}");
            buf.append((String)((Object)link));
        }
        if (this.recip.hasCooker()) {
            String cooker = "Cooker:";
            for (ItemTemplate itemTemplate : this.recip.getCookerTemplates()) {
                ++lines;
                buf.append("label{text=\"" + cooker + "\"}");
                buf.append(this.showLinks ? "radio{group=\"sel\";id=\"6," + itemTemplate.getTemplateId() + "\"}" : "label{text=\"\"}");
                buf.append("label{text=\"" + itemTemplate.getName() + "\"}");
                buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(itemTemplate.getTemplateId()) : "") + "\"}");
                buf.append("label{text=\"\"}");
                cooker = "";
            }
        }
        if (this.recip.hasContainer()) {
            String container = "Container:";
            for (ItemTemplate itemTemplate : this.recip.getContainerTemplates()) {
                ++lines;
                buf.append("label{text=\"" + container + "\"}");
                buf.append(this.showLinks ? "radio{group=\"sel\";id=\"8," + itemTemplate.getTemplateId() + "\"}" : "label{text=\"\"}");
                buf.append("label{text=\"" + itemTemplate.getName() + "\"}");
                buf.append("label{text=\"" + (this.showExtra ? Integer.valueOf(itemTemplate.getTemplateId()) : "") + "\"}");
                buf.append("label{text=\"\"}");
                container = "";
            }
        }
        if (this.ingredients.length > 0) {
            byte gid = -5;
            String strIngredient = "";
            for (Ingredient ingredient : this.ingredients) {
                String link2;
                Recipe recipe2 = Recipes.getRecipeByResult(ingredient);
                String string = recipe2 == null ? "label{text=\" \"}" : (RecipesByPlayer.isKnownRecipe(this.getResponder().getWurmId(), recipe2.getRecipeId()) ? "harray{radio{group=\"sel\";id=\"13," + recipe2.getRecipeId() + "\";text=\"\"}" + this.colourRecipeName(recipe2, "Show recipe", false) + "}" : (link2 = "label{color=\"255,127,127\";text=\"Unknown recipe" + (this.showExtra ? " - " + recipe2.getRecipeId() : "") + "\"}"));
                if (ingredient.getGroupId() < 0) continue;
                if (gid == -5) {
                    ++lines;
                    buf.append("label{type=\"bolditalic\";text=\"Ingredients:\"};label{text=\"\"};label{text=\"\"};label{text=\"\"}label{text=\"\"}");
                }
                if (gid < ingredient.getGroupId()) {
                    gid = ingredient.getGroupId();
                    strIngredient = this.recip.getGroupById(gid).getGroupTypeName() + ":";
                }
                ++lines;
                buf.append("label{text=\"" + strIngredient + "\"}");
                buf.append(this.showLinks ? "radio{group=\"sel\";id=\"12," + ingredient.getTemplateId() + "," + ingredient.getCState() + "," + ingredient.getPState() + "," + ingredient.getMaterial() + "," + ingredient.hasRealTemplate() + "," + ingredient.getRealTemplateId() + "," + ingredient.getCorpseData() + "\";text=\"\"}" : "label{text=\"\"}");
                String amount = (ingredient.isLiquid() && ingredient.getRatio() != 0 ? " (ratio " + ingredient.getRatio() + "%)" : "") + (ingredient.getLoss() > 0 ? " (loss " + ingredient.getLoss() + "%)" : "");
                buf.append("label{text=\"" + Recipes.getIngredientName(ingredient) + amount + "\"}");
                buf.append("label{text=\"" + (this.showExtra ? ingredient.getGroupId() + "," + ingredient.getTemplateId() + "," + ingredient.getCState() + "," + ingredient.getPState() + "," + ingredient.getRatio() + "," + ingredient.getLoss() + "," + ingredient.getMaterial() + "," + ingredient.getRealTemplateId() + "," + ingredient.getCorpseData() : "") + "\"}");
                buf.append(link2);
                strIngredient = "";
            }
        }
        buf.append("}");
        buf.append("radio{group=\"sel\";id=\"-10\";selected=\"true\";hidden=\"true\";text=\"None\"}");
        buf.append("text{text=\"\"}");
        boolean knownRecipe = false;
        String mid = "null;";
        if (this.getResponder().getPower() == 5) {
            mid = "center{harray{button{text=\"Remove from Cookbook\";id=\"remove\";hover=\"Use with care\";confirm=\"Are you sure you want to do that?\";question=\"This will remove this recipe from your cookbook.\"};}};";
        }
        if (Recipes.isKnownRecipe(this.recip.getRecipeId())) {
            buf.append("label{color=\"127,255,127\"text=\"This recipe is known to everyone\"}");
            ++lines;
            mid = "null;";
        }
        if (!this.showLinks) {
            if (this.from.length() > 0) {
                buf.append("harray{label{text=\"Signed:\"};label{type=\"italics\";text=\"" + this.from + "\"}}");
                buf.append("text{text=\"\"}");
                ++lines;
                ++lines;
            }
            if (RecipesByPlayer.isKnownRecipe(this.getResponder().getWurmId(), this.recip.getRecipeId())) {
                buf.append("label{type=\"bold\";color=\"255,127,127\"text=\"This recipe is already in your cookbook.\"}");
                knownRecipe = true;
            } else {
                buf.append("label{type=\"bold\";color=\"255,127,127\"text=\"To make this recipe it first must be put in your cookbook.\"}");
            }
            ++lines;
            mid = "null;";
        }
        buf.append("}};null;");
        Object var8_15 = null;
        if (this.target != -10L) {
            try {
                Item item = Items.getItem(this.target);
            }
            catch (NoSuchItemException e) {
                logger.log(Level.WARNING, "Target (" + this.target + ") no longer exists!");
            }
        }
        if (this.showLinks) {
            buf.append("border{size=\"20,20\";null;harray{label{text=\" \"};button{text=\"Show selected\";id=\"show\"}};" + mid + "harray{button{text=\"Go to info\";id=\"info\"};label{text=\" \"}};null;}");
        } else if (this.target != -10L && !knownRecipe && var8_17 != null && var8_17.getTopParent() == this.getResponder().getInventory().getWurmId()) {
            buf.append("border{size=\"20,20\";null;null;center{harray{label{text=\" \"};button{text=\"Add to cookbook\";id=\"add\"}}};null;null;}");
        } else {
            buf.append("null;");
        }
        buf.append("}");
        int height = lines * 20;
        this.getResponder().getCommunicator().sendBml(this.showExtra ? 470 : 400, height, true, false, buf.toString(), 200, 200, 200, this.title);
    }

    private String colourRecipeName(Recipe recipe, String name, boolean isBold) {
        StringBuilder buf = new StringBuilder();
        if (recipe.isKnown()) {
            buf.append("label{" + (isBold ? "type=\"bold\";" : "") + green + "text=\"" + name.replace("\"", "''") + (this.showExtra ? " - " + recipe.getRecipeId() : "") + "\"};");
        } else {
            buf.append(CookBookQuestion.nameColoredByRarity(name + (this.showExtra ? " - " + recipe.getRecipeId() : ""), "", recipe.getLootableRarity(), isBold));
        }
        return buf.toString();
    }

    public Recipe getRecipe() {
        return this.recip;
    }

    public byte getDisplayType() {
        return this.displayType;
    }
}

