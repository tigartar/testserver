/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Ingredient;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class RecipesByPlayer
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(RecipesByPlayer.class.getName());
    private static final Map<Long, RecipesByPlayer> playerRecipes = new ConcurrentHashMap<Long, RecipesByPlayer>();
    private static final String GET_ALL_PLAYER_RECIPES = "SELECT * FROM RECIPESPLAYER";
    private static final String GET_ALL_PLAYER_COOKERS = "SELECT * FROM RECIPEPLAYERCOOKERS";
    private static final String GET_ALL_PLAYER_CONTAINERS = "SELECT * FROM RECIPEPLAYERCONTAINERS";
    private static final String GET_ALL_PLAYER_INGREDIENTS = "SELECT * FROM RECIPEPLAYERINGREDIENTS";
    private static final String CREATE_PLAYER_RECIPE = "INSERT INTO RECIPESPLAYER (PLAYERID,RECIPEID,FAVOURITE,NOTES) VALUES(?,?,?,?)";
    private static final String CREATE_PLAYER_RECIPE_COOKER = "INSERT INTO RECIPEPLAYERCOOKERS (PLAYERID,RECIPEID,COOKERID) VALUES(?,?,?)";
    private static final String CREATE_PLAYER_RECIPE_CONTAINER = "INSERT INTO RECIPEPLAYERCONTAINERS (PLAYERID,RECIPEID,CONTAINERID) VALUES(?,?,?)";
    private static final String CREATE_PLAYER_RECIPE_INGREDIENT = "INSERT INTO RECIPEPLAYERINGREDIENTS (PLAYERID,RECIPEID,INGREDIENTID,GROUPID,TEMPLATEID,CSTATE,PSTATE,MATERIAL,REALTEMPLATEID) VALUES(?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_PLAYER_RECIPE_FAVOURITE = "UPDATE RECIPESPLAYER SET FAVOURITE=? WHERE PLAYERID=? AND RECIPEID=?";
    private static final String UPDATE_PLAYER_RECIPE_NOTES = "UPDATE RECIPESPLAYER SET NOTES=? WHERE PLAYERID=? AND RECIPEID=?";
    private static final String UPDATE_PLAYER_RECIPE_INGREDIENT = "UPDATE RECIPEPLAYERINGREDIENTS SET TEMPLATEID=?,CSTATE=?,PSTATE=?,MATERIAL=?,REALTEMPLATEID=? WHERE PLAYERID=? AND RECIPEID=? AND INGREDIENTID=?";
    private static final String DELETE_PLAYER_RECIPES = "DELETE FROM RECIPESPLAYER WHERE PLAYERID=?";
    private static final String DELETE_PLAYER_RECIPES_COOKERS = "DELETE FROM RECIPEPLAYERCOOKERS WHERE PLAYERID=?";
    private static final String DELETE_PLAYER_RECIPES_CONTAINERS = "DELETE FROM RECIPEPLAYERCONTAINERS WHERE PLAYERID=?";
    private static final String DELETE_PLAYER_RECIPES_INGREDIENTS = "DELETE FROM RECIPEPLAYERINGREDIENTS WHERE PLAYERID=?";
    private static final String DELETE_PLAYER_RECIPE = "DELETE FROM RECIPESPLAYER WHERE PLAYERID=? AND RECIPEID=?";
    private static final String DELETE_PLAYER_RECIPE_COOKERS = "DELETE FROM RECIPEPLAYERCOOKERS WHERE PLAYERID=? AND RECIPEID=?";
    private static final String DELETE_PLAYER_RECIPE_CONTAINERS = "DELETE FROM RECIPEPLAYERCONTAINERS WHERE PLAYERID=? AND RECIPEID=?";
    private static final String DELETE_PLAYER_RECIPE_INGREDIENTS = "DELETE FROM RECIPEPLAYERINGREDIENTS WHERE PLAYERID=? AND RECIPEID=?";
    private static final String DELETE_ALL_PLAYER_RECIPES = "DELETE FROM RECIPESPLAYER";
    private static final String DELETE_ALL_PLAYER_RECIPE_COOKERS = "DELETE FROM RECIPEPLAYERCOOKERS";
    private static final String DELETE_ALL_PLAYER_RECIPE_CONTAINERS = "DELETE FROM RECIPEPLAYERCONTAINERS";
    private static final String DELETE_ALL_PLAYER_RECIPE_INGREDIENTS = "DELETE FROM RECIPEPLAYERINGREDIENTS";
    private final long wurmId;
    private final Map<Short, Recipe> knownRecipes = new ConcurrentHashMap<Short, Recipe>();
    private final Map<Short, Boolean> playerFavourites = new ConcurrentHashMap<Short, Boolean>();
    private final Map<Short, String> playerNotes = new ConcurrentHashMap<Short, String>();

    public RecipesByPlayer(long playerId) {
        this.wurmId = playerId;
    }

    public long getPlayerId() {
        return this.wurmId;
    }

    public void addRecipe(Recipe recipe) {
        this.knownRecipes.put(recipe.getRecipeId(), recipe);
    }

    @Nullable
    public Recipe getRecipe(short recipeId) {
        return this.knownRecipes.get(recipeId);
    }

    public boolean isKnownRecipe(short recipeId) {
        return this.knownRecipes.containsKey(recipeId);
    }

    public void removeRecipe(short recipeId) {
        this.knownRecipes.remove(recipeId);
    }

    boolean setFavourite(short recipeId, boolean isFavourite) {
        Boolean wasFavourite = this.playerFavourites.put(recipeId, isFavourite);
        if (!this.playerNotes.containsKey(recipeId)) {
            this.playerNotes.put(recipeId, "");
        }
        return wasFavourite == null || wasFavourite != isFavourite;
    }

    boolean setNotes(short recipeId, String notes) {
        String newNotes = notes.substring(0, Math.min(notes.length(), 200));
        String oldNotes = this.playerNotes.put(recipeId, newNotes);
        if (!this.playerFavourites.containsKey(recipeId)) {
            this.playerFavourites.put(recipeId, false);
        }
        return oldNotes == null || !oldNotes.equals(newNotes);
    }

    boolean isFavourite(short recipeId) {
        Boolean isFavourite = this.playerFavourites.get(recipeId);
        return isFavourite != null && isFavourite != false;
    }

    String getNotes(short recipeId) {
        String notes = this.playerNotes.get(recipeId);
        if (notes != null) {
            return notes.substring(0, Math.min(notes.length(), 200));
        }
        return "";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final int loadAllPlayerKnownRecipes() {
        Recipe recipe;
        RecipesByPlayer rbp;
        short recipeId;
        int count = 0;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(GET_ALL_PLAYER_RECIPES);
            rs = ps.executeQuery();
            while (rs.next()) {
                long playerId = rs.getLong("PLAYERID");
                recipeId = rs.getShort("RECIPEID");
                boolean favourite = rs.getBoolean("FAVOURITE");
                String notes = rs.getString("NOTES");
                Recipe templateRecipe = Recipes.getRecipeById(recipeId);
                if (templateRecipe != null) {
                    RecipesByPlayer rbp2 = RecipesByPlayer.getRecipesByPlayer(playerId, true);
                    rbp2.setFavourite(recipeId, favourite);
                    rbp2.setNotes(recipeId, notes);
                    if (templateRecipe.isKnown()) continue;
                    ++count;
                    Recipe playerRecipe = new Recipe(recipeId);
                    rbp2.addRecipe(playerRecipe);
                    continue;
                }
                logger.log(Level.WARNING, "Known recipe is not found in templates " + recipeId + " for player " + playerId);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load all player known recipes: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        try {
            assert (dbcon != null);
            ps = dbcon.prepareStatement(GET_ALL_PLAYER_COOKERS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long playerId = rs.getLong("PLAYERID");
                recipeId = rs.getShort("RECIPEID");
                short cookerId = rs.getShort("COOKERID");
                rbp = RecipesByPlayer.getRecipesByPlayer(playerId, false);
                if (rbp == null || (recipe = rbp.getRecipe(recipeId)) == null) continue;
                recipe.addToCookerList(cookerId);
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Failed to load all player known recipes: " + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        try {
            ps = dbcon.prepareStatement(GET_ALL_PLAYER_CONTAINERS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long playerId = rs.getLong("PLAYERID");
                recipeId = rs.getShort("RECIPEID");
                short containerId = rs.getShort("CONTAINERID");
                rbp = RecipesByPlayer.getRecipesByPlayer(playerId, false);
                if (rbp == null || (recipe = rbp.getRecipe(recipeId)) == null) continue;
                recipe.addToContainerList(containerId);
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Failed to load all player known recipes: " + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
        try {
            ps = dbcon.prepareStatement(GET_ALL_PLAYER_INGREDIENTS);
            rs = ps.executeQuery();
            while (rs.next()) {
                long playerId = rs.getLong("PLAYERID");
                recipeId = rs.getShort("RECIPEID");
                byte ingredientId = rs.getByte("INGREDIENTID");
                byte groupId = rs.getByte("GROUPID");
                short templateId = rs.getShort("TEMPLATEID");
                byte cstate = rs.getByte("CSTATE");
                byte pstate = rs.getByte("PSTATE");
                byte material = rs.getByte("MATERIAL");
                short realTemplateId = rs.getShort("REALTEMPLATEID");
                RecipesByPlayer rbp3 = RecipesByPlayer.getRecipesByPlayer(playerId, false);
                if (rbp3 != null) {
                    try {
                        Recipe recipe2 = rbp3.getRecipe(recipeId);
                        if (recipe2 != null) {
                            Recipe refRecipe = Recipes.getRecipeById(recipeId);
                            assert (refRecipe != null);
                            Ingredient refingredient = refRecipe.getIngredientById(ingredientId);
                            Ingredient ingredient = RecipesByPlayer.makeIngredient(templateId, cstate, pstate, material, refingredient.hasRealTemplate(), realTemplateId, groupId);
                            if (ingredient != null) {
                                ingredient.setAmount(refingredient.getAmount());
                                ingredient.setRatio(refingredient.getRatio());
                                ingredient.setLoss(refingredient.getLoss());
                                ingredient.setIngredientId(ingredientId);
                                recipe2.addIngredient(ingredient);
                                continue;
                            }
                            logger.log(Level.WARNING, "Failed to find template for " + templateId + " or " + realTemplateId + ".");
                            continue;
                        }
                        logger.log(Level.WARNING, "Failed to find player recipe " + recipeId + ".");
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to load player recipe " + recipeId + ", so deleted entry on db.");
                        RecipesByPlayer.dbRemovePlayerRecipe(playerId, recipeId);
                    }
                    continue;
                }
                logger.log(Level.WARNING, "Failed to find player recipe list, so deleted entry on db.");
                RecipesByPlayer.dbRemovePlayerRecipe(playerId, recipeId);
            }
        }
        catch (SQLException sqex) {
            logger.log(Level.WARNING, "Failed to load all player known recipes: " + sqex.getMessage(), sqex);
        }
        finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        return count;
    }

    private static Ingredient makeIngredient(short templateId, byte cstate, byte pstate, byte material, boolean hasRealTemplate, short realTemplateId, int groupId) {
        try {
            ItemTemplate itemTemplate = ItemTemplateFactory.getInstance().getTemplate(templateId);
            Ingredient ingredient = new Ingredient(itemTemplate, false, (byte)groupId);
            ingredient.setCState(cstate);
            ingredient.setPState(pstate);
            ingredient.setMaterial(material);
            if (templateId == 272) {
                ingredient.setCorpseData(realTemplateId);
            } else if (realTemplateId > -1) {
                ItemTemplate realItemTemplate = ItemTemplateFactory.getInstance().getTemplate(realTemplateId);
                ingredient.setRealTemplate(realItemTemplate);
            } else if (hasRealTemplate) {
                ingredient.setRealTemplate(null);
            }
            return ingredient;
        }
        catch (NoSuchTemplateException e) {
            return null;
        }
    }

    static final RecipesByPlayer getRecipesByPlayer(long playerId, boolean autoCreate) {
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        if (rbp == null && autoCreate) {
            rbp = new RecipesByPlayer(playerId);
            playerRecipes.put(playerId, rbp);
        }
        return rbp;
    }

    private static final Set<Recipe> getKnownRecipesSetFor(long playerId) {
        Set<Recipe> recipes = Recipes.getKnownRecipes();
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        if (rbp != null) {
            recipes.addAll(rbp.knownRecipes.values());
        }
        return recipes;
    }

    public static boolean isKnownRecipe(long playerId, short recipeId) {
        if (Recipes.isKnownRecipe(recipeId)) {
            return true;
        }
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        return rbp != null && rbp.knownRecipes.containsKey(recipeId);
    }

    public static boolean isFavourite(long playerId, short recipeId) {
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        return rbp != null && rbp.isFavourite(recipeId);
    }

    public static String getNotes(long playerId, short recipeId) {
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        if (rbp != null) {
            return rbp.getNotes(recipeId);
        }
        return "";
    }

    public static Recipe getPlayerKnownRecipeOrNull(long playerId, short recipeId) {
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        if (rbp != null) {
            return rbp.knownRecipes.get(recipeId);
        }
        return null;
    }

    public static final Recipe[] getTargetActionRecipesFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.isTargetActionType()) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe[] getContainerActionRecipesFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.isContainerActionType()) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe[] getHeatRecipesFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.isHeatType()) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe[] getTimeRecipesFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.isTimeType()) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final ItemTemplate[] getKnownCookersFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<ItemTemplate> knownCookers = new HashSet<ItemTemplate>();
        for (Recipe recipe : recipes) {
            if (!recipe.hasCooker()) continue;
            knownCookers.addAll(recipe.getCookerTemplates());
        }
        return knownCookers.toArray(new ItemTemplate[knownCookers.size()]);
    }

    public static final Recipe[] getCookerRecipesFor(long playerId, int cookerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.hasCooker(cookerId)) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final ItemTemplate[] getKnownContainersFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<ItemTemplate> knownContainers = new HashSet<ItemTemplate>();
        for (Recipe recipe : recipes) {
            if (!recipe.hasContainer()) continue;
            knownContainers.addAll(recipe.getContainerTemplates());
        }
        return knownContainers.toArray(new ItemTemplate[knownContainers.size()]);
    }

    public static final Recipe[] getContainerRecipesFor(long playerId, int containerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.hasContainer(containerId)) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final ItemTemplate[] getKnownToolsFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<ItemTemplate> knownTools = new HashSet<ItemTemplate>();
        for (Recipe recipe : recipes) {
            if (recipe.getActiveItem() == null || !recipe.getActiveItem().getTemplate().isCookingTool()) continue;
            knownTools.add(recipe.getActiveItem().getTemplate());
        }
        return knownTools.toArray(new ItemTemplate[knownTools.size()]);
    }

    public static final Recipe[] getToolRecipesFor(long playerId, int toolId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (recipe.getActiveItem() == null || recipe.getActiveItem().getTemplateId() != toolId) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Ingredient[] getKnownIngredientsFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashMap<String, Ingredient> knownIngredients = new HashMap<String, Ingredient>();
        for (Recipe recipe : recipes) {
            for (Ingredient i : recipe.getAllIngredients(true).values()) {
                Ingredient ingredient = i.clone(null);
                knownIngredients.put(ingredient.getName(true), ingredient);
            }
        }
        return knownIngredients.values().toArray(new Ingredient[knownIngredients.size()]);
    }

    public static final Ingredient[] getRecipeIngredientsFor(long playerId, int recipeId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashMap<String, Ingredient> knownIngredients = new HashMap<String, Ingredient>();
        for (Recipe recipe : recipes) {
            if (recipe.getRecipeId() != recipeId) continue;
            knownIngredients.putAll(recipe.getAllIngredients(true));
            break;
        }
        return knownIngredients.values().toArray(new Ingredient[knownIngredients.size()]);
    }

    public static final Recipe[] getIngredientRecipesFor(long playerId, Ingredient ingredient) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        block0: for (Recipe recipe : recipes) {
            Map<String, Ingredient> recipeIngredients = recipe.getAllIngredients(true);
            for (Ingredient i : recipeIngredients.values()) {
                if (!i.getName(false).equalsIgnoreCase(ingredient.getName(false))) continue;
                knownRecipes.add(recipe);
                continue block0;
            }
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe[] getSearchRecipesFor(long playerId, String searchFor) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            if (!recipe.getName().toLowerCase().contains(searchFor.toLowerCase())) continue;
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe[] getKnownRecipesFor(long playerId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        HashSet<Recipe> knownRecipes = new HashSet<Recipe>();
        for (Recipe recipe : recipes) {
            knownRecipes.add(recipe);
        }
        return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
    }

    public static final Recipe getRecipe(long playerId, int recipeId) {
        Set<Recipe> recipes = RecipesByPlayer.getKnownRecipesSetFor(playerId);
        for (Recipe recipe : recipes) {
            if (recipe.getRecipeId() != recipeId) continue;
            return recipe;
        }
        return null;
    }

    public static void packRecipes(DataOutputStream dos, long playerId) throws IOException {
        HashSet<Recipe> recipes = new HashSet<Recipe>();
        RecipesByPlayer rbp = playerRecipes.get(playerId);
        if (rbp != null) {
            recipes.addAll(rbp.knownRecipes.values());
        }
        dos.writeChar(88);
        dos.writeShort(recipes.size());
        logger.log(Level.INFO, "packing " + recipes.size() + " known recipes!");
        for (Recipe recipe : recipes) {
            dos.writeChar(82);
            recipe.pack(dos);
        }
        if (rbp != null) {
            int count = 0;
            for (Boolean bl : rbp.playerFavourites.values()) {
                if (!bl.booleanValue()) continue;
                ++count;
            }
            logger.log(Level.INFO, "packing " + count + " favourites!");
            dos.writeShort(count);
            for (Map.Entry entry : rbp.playerFavourites.entrySet()) {
                if (!((Boolean)entry.getValue()).booleanValue()) continue;
                dos.writeShort(((Short)entry.getKey()).shortValue());
            }
            count = 0;
            for (String string : rbp.playerNotes.values()) {
                if (string.length() <= 0) continue;
                ++count;
            }
            logger.log(Level.INFO, "packing " + count + " notes!");
            dos.writeShort(count);
            for (Map.Entry entry : rbp.playerNotes.entrySet()) {
                if (((String)entry.getValue()).length() <= 0) continue;
                dos.writeShort(((Short)entry.getKey()).shortValue());
                byte[] notesAsBytes = ((String)entry.getValue()).getBytes("UTF-8");
                dos.writeByte((byte)notesAsBytes.length);
                dos.write(notesAsBytes);
            }
        } else {
            dos.writeShort(0);
            dos.writeShort(0);
        }
    }

    public static void unPackRecipes(DataInputStream dis, long playerId) throws IOException {
        short recipeId;
        RecipesByPlayer.deleteRecipesForPlayer(playerId);
        if (dis.readChar() != 'X') {
            throw new IOException(new Exception("unpacking error, no start recipe list 'X' char"));
        }
        int count = dis.readShort();
        logger.log(Level.INFO, "unpacking " + count + " known recipes!");
        if (count > 0) {
            RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(playerId, true);
            for (int x = 0; x < count; ++x) {
                if (dis.readChar() != 'R') {
                    throw new IOException(new Exception("unpacking error, no start recipe 'R' char for recipe " + x + " out of " + count + "."));
                }
                try {
                    Recipe recipe = new Recipe(dis);
                    RecipesByPlayer.addRecipe(rbp, recipe);
                    continue;
                }
                catch (NoSuchTemplateException e) {
                    logger.log(Level.INFO, "unpacking fail: " + e.getMessage(), e);
                    throw new IOException(e.getMessage());
                }
            }
        }
        count = dis.readShort();
        logger.log(Level.INFO, "unpacking " + count + " favourites!");
        if (count > 0) {
            for (int x = 0; x < count; ++x) {
                recipeId = dis.readShort();
                RecipesByPlayer.setIsFavourite(playerId, recipeId, true);
            }
        }
        count = dis.readShort();
        logger.log(Level.INFO, "unpacking " + count + " notes!");
        if (count > 0) {
            for (int x = 0; x < count; ++x) {
                byte[] tempStringArr;
                int read;
                recipeId = dis.readShort();
                byte lByte = dis.readByte();
                int length = lByte & 0xFF;
                if (length != (read = dis.read(tempStringArr = new byte[length]))) {
                    logger.warning("Read in " + read + ", expected " + length);
                }
                String notes = new String(tempStringArr, "UTF-8");
                RecipesByPlayer.setNotes(playerId, recipeId, notes);
            }
        }
    }

    public static boolean saveRecipe(@Nullable Creature performer, Recipe templateRecipe, long playerId, @Nullable Item source, Item target) {
        Ingredient pi;
        Item cooker;
        if (templateRecipe.isKnown()) {
            return false;
        }
        if (playerId == -10L) {
            logger.log(Level.WARNING, "Failed to save recipe '" + templateRecipe.getName() + "' (#" + templateRecipe.getRecipeId() + "): No player ID given");
            return false;
        }
        if (performer != null) {
            Recipes.setRecipeNamer(templateRecipe, performer);
        }
        boolean isChanged = false;
        RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(playerId, true);
        Recipe playerRecipe = rbp.getRecipe(templateRecipe.getRecipeId());
        if (playerRecipe != null) {
            Object pi2;
            Item cooker2;
            if (templateRecipe.hasCooker() && (cooker2 = target.getTopParentOrNull()) != null && !playerRecipe.hasCooker(cooker2.getTemplateId())) {
                playerRecipe.addToCookerList(cooker2.getTemplateId());
                RecipesByPlayer.dbSaveRecipeCooker(playerId, playerRecipe.getRecipeId(), cooker2.getTemplateId());
                isChanged = true;
            }
            if (templateRecipe.hasContainer() && target != null && !playerRecipe.hasContainer(target.getTemplateId())) {
                playerRecipe.addToContainerList(target.getTemplateId());
                RecipesByPlayer.dbSaveRecipeContainer(playerId, playerRecipe.getRecipeId(), target.getTemplateId());
                isChanged = true;
            }
            if (source != null & templateRecipe.getActiveItem() != null && playerRecipe.getActiveItem() != null && templateRecipe.getActiveItem().isFoodGroup() && !playerRecipe.getActiveItem().isFoodGroup() && playerRecipe.getActiveItem().getTemplateId() != source.getTemplateId()) {
                pi2 = playerRecipe.getIngredientById(templateRecipe.getActiveItem().getIngredientId());
                ((Ingredient)pi2).setTemplate(templateRecipe.getActiveItem().getTemplate());
                RecipesByPlayer.dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), (Ingredient)pi2);
                playerRecipe.addIngredient((Ingredient)pi2);
                isChanged = true;
            }
            if (templateRecipe.getTargetItem() != null && playerRecipe.getTargetItem() != null && templateRecipe.getTargetItem().isFoodGroup() && !playerRecipe.getTargetItem().isFoodGroup() && playerRecipe.getTargetItem().getTemplateId() != target.getTemplateId()) {
                pi2 = playerRecipe.getIngredientById(templateRecipe.getTargetItem().getIngredientId());
                ((Ingredient)pi2).setTemplate(templateRecipe.getTargetItem().getTemplate());
                RecipesByPlayer.dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), (Ingredient)pi2);
                playerRecipe.addIngredient((Ingredient)pi2);
                isChanged = true;
            }
            if (target.isFoodMaker() || target.getTemplate().isCooker()) {
                for (Item item : target.getItemsAsArray()) {
                    Ingredient ti = templateRecipe.findMatchingIngredient(item);
                    if (ti == null) {
                        logger.log(Level.WARNING, "Failed to find matching ingredient:" + item.getName() + ".");
                        continue;
                    }
                    Ingredient pi3 = playerRecipe.getIngredientById(ti.getIngredientId());
                    if (pi3 == null) {
                        Ingredient ingredient = ti.clone(item);
                        ingredient.setTemplate(item.getTemplate());
                        RecipesByPlayer.dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), ingredient);
                        playerRecipe.addIngredient(ingredient);
                        isChanged = true;
                        continue;
                    }
                    if (!ti.isFoodGroup() || pi3.isFoodGroup()) continue;
                    pi3.setTemplate(ti.getTemplate());
                    RecipesByPlayer.dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), pi3);
                    playerRecipe.addIngredient(pi3);
                    isChanged = true;
                }
            }
            if (isChanged && performer != null) {
                performer.getCommunicator().sendCookbookRecipe(playerRecipe);
            }
            return false;
        }
        playerRecipe = new Recipe(templateRecipe.getRecipeId());
        RecipesByPlayer.dbSaveRecipe(playerId, playerRecipe.getRecipeId(), false, "");
        if (templateRecipe.hasCooker() && (cooker = target.getTopParentOrNull()) != null) {
            playerRecipe.addToCookerList((short)cooker.getTemplateId());
            RecipesByPlayer.dbSaveRecipeCooker(playerId, playerRecipe.getRecipeId(), cooker.getTemplateId());
        }
        if (templateRecipe.hasContainer()) {
            playerRecipe.addToContainerList((short)target.getTemplateId());
            RecipesByPlayer.dbSaveRecipeContainer(playerId, playerRecipe.getRecipeId(), target.getTemplateId());
        }
        if (templateRecipe.getActiveItem() != null && source != null) {
            pi = templateRecipe.getActiveItem().getTemplateId() == 14 ? new Ingredient(templateRecipe.getActiveItem().getTemplate(), false, -2) : new Ingredient(source.getTemplate(), false, -2);
            if (templateRecipe.getActiveItem().hasMaterial()) {
                pi.setMaterial(source.getMaterial());
            }
            if (templateRecipe.getActiveItem().hasCState()) {
                pi.setCState(source.getRightAuxData());
            }
            if (templateRecipe.getActiveItem().hasPState()) {
                pi.setPState((byte)(source.getLeftAuxData() * 16));
            }
            if (templateRecipe.getActiveItem().hasRealTemplate()) {
                pi.setRealTemplate(source.getRealTemplate());
            }
            pi.setIngredientId(templateRecipe.getActiveItem().getIngredientId());
            RecipesByPlayer.dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi);
            playerRecipe.addIngredient(pi);
        }
        if (templateRecipe.getTargetItem() != null) {
            pi = new Ingredient(target.getTemplate(), false, -1);
            if (templateRecipe.getTargetItem().hasMaterial()) {
                pi.setMaterial(target.getMaterial());
            }
            if (templateRecipe.getTargetItem().hasCState()) {
                pi.setCState(target.getRightAuxData());
            }
            if (templateRecipe.getTargetItem().hasPState()) {
                pi.setPState((byte)(target.getLeftAuxData() * 16));
            }
            if (templateRecipe.getTargetItem().hasRealTemplate()) {
                pi.setRealTemplate(target.getRealTemplate());
            }
            if (templateRecipe.getTargetItem().hasCorpseData()) {
                pi.setCorpseData(templateRecipe.getTargetItem().getCorpseData());
            }
            pi.setIngredientId(templateRecipe.getTargetItem().getIngredientId());
            RecipesByPlayer.dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi);
            playerRecipe.addIngredient(pi);
        }
        if (target.isFoodMaker() || target.getTemplate().isCooker()) {
            templateRecipe.clearFound();
            for (Item item : target.getItemsAsArray()) {
                Ingredient ti = templateRecipe.findMatchingIngredient(item);
                if (ti == null) {
                    logger.log(Level.WARNING, "Failed to find matching ingredient:" + item.getName() + ".");
                    continue;
                }
                if (ti.wasFound(true, false)) continue;
                ti.setFound(true);
                Ingredient pi4 = ti.clone(item);
                pi4.setTemplate(item.getTemplate());
                RecipesByPlayer.dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi4);
                playerRecipe.addIngredient(pi4);
            }
        }
        rbp.addRecipe(playerRecipe);
        if (performer != null) {
            performer.getCommunicator().sendCookbookRecipe(playerRecipe);
        }
        return true;
    }

    public static boolean addRecipe(Creature performer, Recipe recipe) {
        RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(performer.getWurmId(), true);
        if (RecipesByPlayer.addRecipe(rbp, recipe)) {
            return true;
        }
        performer.getCommunicator().sendNormalServerMessage("That recipe is already in your cookbook!");
        return false;
    }

    private static boolean addRecipe(RecipesByPlayer rbp, Recipe recipe) {
        Recipe playerRecipe = rbp.getRecipe(recipe.getRecipeId());
        if (playerRecipe == null) {
            RecipesByPlayer.dbSaveRecipe(rbp.getPlayerId(), recipe.getRecipeId(), rbp.isFavourite(recipe.getRecipeId()), rbp.getNotes(recipe.getRecipeId()));
            if (recipe.hasCooker()) {
                for (ItemTemplate cooker : recipe.getCookerTemplates()) {
                    RecipesByPlayer.dbSaveRecipeCooker(rbp.getPlayerId(), recipe.getRecipeId(), cooker.getTemplateId());
                }
            }
            if (recipe.hasContainer()) {
                for (ItemTemplate container : recipe.getContainerTemplates()) {
                    RecipesByPlayer.dbSaveRecipeContainer(rbp.getPlayerId(), recipe.getRecipeId(), container.getTemplateId());
                }
            }
            if (recipe.getActiveItem() != null) {
                RecipesByPlayer.dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), recipe.getActiveItem());
            }
            if (recipe.getTargetItem() != null) {
                RecipesByPlayer.dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), recipe.getTargetItem());
            }
            for (Ingredient i : recipe.getAllIngredients(true).values()) {
                RecipesByPlayer.dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), i);
            }
            rbp.addRecipe(recipe);
            return true;
        }
        return false;
    }

    public static void removeRecipeForPlayer(long playerId, short recipeId) {
        RecipesByPlayer.dbRemovePlayerRecipe(playerId, recipeId);
        RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(playerId, false);
        if (rbp != null) {
            rbp.removeRecipe(recipeId);
        }
    }

    public static void deleteRecipesByNumber(short recipeId) {
        for (Map.Entry<Long, RecipesByPlayer> entry : playerRecipes.entrySet()) {
            Recipe recipe = entry.getValue().getRecipe(recipeId);
            if (recipe == null) continue;
            RecipesByPlayer rbp = entry.getValue();
            long playerId = entry.getKey();
            RecipesByPlayer.dbRemovePlayerRecipe(playerId, recipeId);
            rbp.removeRecipe(recipeId);
            try {
                Player player = Players.getInstance().getPlayer(playerId);
                player.getCommunicator().sendCookbookRecipe(recipe);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {}
        }
    }

    public static void deleteRecipesForPlayer(long playerId) {
        RecipesByPlayer.dbRemovePlayerRecipes(playerId);
        playerRecipes.remove(playerId);
    }

    public static void deleteAllKnownRecipes() {
        RecipesByPlayer.dbRemoveAllPlayerRecipes();
        playerRecipes.clear();
    }

    public static void setIsFavourite(long playerId, short recipeId, boolean isFavourite) {
        RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(playerId, true);
        rbp.setFavourite(recipeId, isFavourite);
        RecipesByPlayer.dbUpdateRecipeFavourite(playerId, recipeId, isFavourite);
    }

    public static void setNotes(long playerId, short recipeId, String notes) {
        RecipesByPlayer rbp = RecipesByPlayer.getRecipesByPlayer(playerId, true);
        rbp.setNotes(recipeId, notes);
        RecipesByPlayer.dbUpdateRecipeNotes(playerId, recipeId, notes);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbSaveRecipe(long playerId, short recipeId, boolean favourite, String notes) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(CREATE_PLAYER_RECIPE);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setBoolean(3, favourite);
            ps.setString(4, notes);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save player recipe: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbSaveRecipeCooker(long playerId, short recipeId, int cookerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(CREATE_PLAYER_RECIPE_COOKER);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setShort(3, (short)cookerId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save player recipe cooker: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbSaveRecipeContainer(long playerId, short recipeId, int containerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(CREATE_PLAYER_RECIPE_CONTAINER);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setShort(3, (short)containerId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to save player recipe container: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    private static void dbSaveRecipeIngredient(boolean update, long playerId, short recipeId, Ingredient ingredient) {
        block9: {
            dbcon = null;
            ps = null;
            dbcon = DbConnector.getItemDbCon();
            if (!update) ** GOTO lbl26
            ps = dbcon.prepareStatement("UPDATE RECIPEPLAYERINGREDIENTS SET TEMPLATEID=?,CSTATE=?,PSTATE=?,MATERIAL=?,REALTEMPLATEID=? WHERE PLAYERID=? AND RECIPEID=? AND INGREDIENTID=?");
            ps.setShort(1, (short)ingredient.getTemplateId());
            ps.setByte(2, ingredient.getCState());
            ps.setByte(3, ingredient.getPState());
            ps.setByte(4, ingredient.getMaterial());
            if (ingredient.getTemplateId() == 272) {
                ps.setShort(5, (short)ingredient.getCorpseData());
            } else {
                ps.setShort(5, (short)ingredient.getRealTemplateId());
            }
            ps.setLong(6, playerId);
            ps.setShort(7, recipeId);
            ps.setByte(8, ingredient.getIngredientId());
            did = ps.executeUpdate();
            if (did <= 0) break block9;
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return;
        }
        try {
            DbUtilities.closeDatabaseObjects(ps, null);
lbl26:
            // 2 sources

            ps = dbcon.prepareStatement("INSERT INTO RECIPEPLAYERINGREDIENTS (PLAYERID,RECIPEID,INGREDIENTID,GROUPID,TEMPLATEID,CSTATE,PSTATE,MATERIAL,REALTEMPLATEID) VALUES(?,?,?,?,?,?,?,?,?)");
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setByte(3, ingredient.getIngredientId());
            ps.setByte(4, ingredient.getGroupId());
            ps.setShort(5, (short)ingredient.getTemplateId());
            ps.setByte(6, ingredient.getCState());
            ps.setByte(7, ingredient.getPState());
            ps.setByte(8, ingredient.getMaterial());
            if (ingredient.getTemplateId() == 272) {
                ps.setShort(9, (short)ingredient.getCorpseData());
            } else {
                ps.setShort(9, (short)ingredient.getRealTemplateId());
            }
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                RecipesByPlayer.logger.log(Level.WARNING, "Failed to save player recipe ingredient: " + sqex.getMessage(), sqex);
            }
            catch (Throwable var8_8) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw var8_8;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateRecipeFavourite(long playerId, short recipeId, boolean isFavourite) {
        int did;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            logger.info("update favourite for " + recipeId);
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(UPDATE_PLAYER_RECIPE_FAVOURITE);
            ps.setBoolean(1, isFavourite);
            ps.setLong(2, playerId);
            ps.setShort(3, recipeId);
            did = ps.executeUpdate();
            if (did <= 0) break block5;
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return;
        }
        try {
            logger.info("Update favourite failed, so trying create " + did);
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(CREATE_PLAYER_RECIPE);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setBoolean(3, isFavourite);
            ps.setString(4, "");
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update player (" + playerId + ") recipe (" + recipeId + ") favourite: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateRecipeNotes(long playerId, short recipeId, String notes) {
        int did;
        PreparedStatement ps;
        Connection dbcon;
        block5: {
            dbcon = null;
            ps = null;
            logger.info("update notes for " + recipeId);
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(UPDATE_PLAYER_RECIPE_NOTES);
            ps.setString(1, notes);
            ps.setLong(2, playerId);
            ps.setShort(3, recipeId);
            did = ps.executeUpdate();
            if (did <= 0) break block5;
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
            return;
        }
        try {
            logger.info("Update notes failed, so trying create " + did);
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(CREATE_PLAYER_RECIPE);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setBoolean(3, false);
            ps.setString(4, notes);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to update player (" + playerId + ") recipe (" + recipeId + ") notes: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbRemovePlayerRecipes(long playerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPES);
            ps.setLong(1, playerId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPES_COOKERS);
            ps.setLong(1, playerId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPES_CONTAINERS);
            ps.setLong(1, playerId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPES_INGREDIENTS);
            ps.setLong(1, playerId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to remove player recipes: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbRemovePlayerRecipe(long playerId, short recipeId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPE);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPE_COOKERS);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPE_CONTAINERS);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_PLAYER_RECIPE_INGREDIENTS);
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to remove player recipes: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    private static void dbRemoveAllPlayerRecipes() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getItemDbCon();
            ps = dbcon.prepareStatement(DELETE_ALL_PLAYER_RECIPES);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_ALL_PLAYER_RECIPE_COOKERS);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_ALL_PLAYER_RECIPE_CONTAINERS);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETE_ALL_PLAYER_RECIPE_INGREDIENTS);
            ps.executeUpdate();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to remove all player recipes: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }
}

