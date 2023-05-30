package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class RecipesByPlayer implements MiscConstants {
   private static final Logger logger = Logger.getLogger(RecipesByPlayer.class.getName());
   private static final Map<Long, RecipesByPlayer> playerRecipes = new ConcurrentHashMap<>();
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
   private final Map<Short, Recipe> knownRecipes = new ConcurrentHashMap<>();
   private final Map<Short, Boolean> playerFavourites = new ConcurrentHashMap<>();
   private final Map<Short, String> playerNotes = new ConcurrentHashMap<>();

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
      return isFavourite != null && isFavourite;
   }

   String getNotes(short recipeId) {
      String notes = this.playerNotes.get(recipeId);
      return notes != null ? notes.substring(0, Math.min(notes.length(), 200)) : "";
   }

   public static final int loadAllPlayerKnownRecipes() {
      int count = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      long playerId;
      short recipeId;
      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM RECIPESPLAYER");
         rs = ps.executeQuery();

         while(rs.next()) {
            playerId = rs.getLong("PLAYERID");
            recipeId = rs.getShort("RECIPEID");
            boolean favourite = rs.getBoolean("FAVOURITE");
            String notes = rs.getString("NOTES");
            Recipe templateRecipe = Recipes.getRecipeById(recipeId);
            if (templateRecipe != null) {
               RecipesByPlayer rbp = getRecipesByPlayer(playerId, true);
               rbp.setFavourite(recipeId, favourite);
               rbp.setNotes(recipeId, notes);
               if (!templateRecipe.isKnown()) {
                  ++count;
                  Recipe playerRecipe = new Recipe(recipeId);
                  rbp.addRecipe(playerRecipe);
               }
            } else {
               logger.log(Level.WARNING, "Known recipe is not found in templates " + recipeId + " for player " + playerId);
            }
         }
      } catch (SQLException var63) {
         playerId = (long)var63;
         logger.log(Level.WARNING, "Failed to load all player known recipes: " + var63.getMessage(), (Throwable)var63);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      try {
         assert dbcon != null;

         ps = dbcon.prepareStatement("SELECT * FROM RECIPEPLAYERCOOKERS");
         rs = ps.executeQuery();

         while(rs.next()) {
            playerId = rs.getLong("PLAYERID");
            recipeId = rs.getShort("RECIPEID");
            short cookerId = rs.getShort("COOKERID");
            RecipesByPlayer rbp = getRecipesByPlayer(playerId, false);
            if (rbp != null) {
               Recipe recipe = rbp.getRecipe(recipeId);
               if (recipe != null) {
                  recipe.addToCookerList(cookerId);
               }
            }
         }
      } catch (SQLException var61) {
         playerId = (long)var61;
         logger.log(Level.WARNING, "Failed to load all player known recipes: " + var61.getMessage(), (Throwable)var61);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      try {
         ps = dbcon.prepareStatement("SELECT * FROM RECIPEPLAYERCONTAINERS");
         rs = ps.executeQuery();

         while(rs.next()) {
            playerId = rs.getLong("PLAYERID");
            recipeId = rs.getShort("RECIPEID");
            short containerId = rs.getShort("CONTAINERID");
            RecipesByPlayer rbp = getRecipesByPlayer(playerId, false);
            if (rbp != null) {
               Recipe recipe = rbp.getRecipe(recipeId);
               if (recipe != null) {
                  recipe.addToContainerList(containerId);
               }
            }
         }
      } catch (SQLException var59) {
         playerId = (long)var59;
         logger.log(Level.WARNING, "Failed to load all player known recipes: " + var59.getMessage(), (Throwable)var59);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
      }

      try {
         ps = dbcon.prepareStatement("SELECT * FROM RECIPEPLAYERINGREDIENTS");
         rs = ps.executeQuery();

         while(rs.next()) {
            playerId = rs.getLong("PLAYERID");
            recipeId = rs.getShort("RECIPEID");
            byte ingredientId = rs.getByte("INGREDIENTID");
            byte groupId = rs.getByte("GROUPID");
            short templateId = rs.getShort("TEMPLATEID");
            byte cstate = rs.getByte("CSTATE");
            byte pstate = rs.getByte("PSTATE");
            byte material = rs.getByte("MATERIAL");
            short realTemplateId = rs.getShort("REALTEMPLATEID");
            RecipesByPlayer rbp = getRecipesByPlayer(playerId, false);
            if (rbp != null) {
               try {
                  Recipe recipe = rbp.getRecipe(recipeId);
                  if (recipe != null) {
                     Recipe refRecipe = Recipes.getRecipeById(recipeId);

                     assert refRecipe != null;

                     Ingredient refingredient = refRecipe.getIngredientById(ingredientId);
                     Ingredient ingredient = makeIngredient(templateId, cstate, pstate, material, refingredient.hasRealTemplate(), realTemplateId, groupId);
                     if (ingredient != null) {
                        ingredient.setAmount(refingredient.getAmount());
                        ingredient.setRatio(refingredient.getRatio());
                        ingredient.setLoss(refingredient.getLoss());
                        ingredient.setIngredientId(ingredientId);
                        recipe.addIngredient(ingredient);
                     } else {
                        logger.log(Level.WARNING, "Failed to find template for " + templateId + " or " + realTemplateId + ".");
                     }
                  } else {
                     logger.log(Level.WARNING, "Failed to find player recipe " + recipeId + ".");
                  }
               } catch (Exception var56) {
                  logger.log(Level.WARNING, "Failed to load player recipe " + recipeId + ", so deleted entry on db.");
                  dbRemovePlayerRecipe(playerId, recipeId);
               }
            } else {
               logger.log(Level.WARNING, "Failed to find player recipe list, so deleted entry on db.");
               dbRemovePlayerRecipe(playerId, recipeId);
            }
         }
      } catch (SQLException var57) {
         playerId = (long)var57;
         logger.log(Level.WARNING, "Failed to load all player known recipes: " + var57.getMessage(), (Throwable)var57);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return count;
   }

   private static Ingredient makeIngredient(
      short templateId, byte cstate, byte pstate, byte material, boolean hasRealTemplate, short realTemplateId, int groupId
   ) {
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
      } catch (NoSuchTemplateException var10) {
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
      } else {
         RecipesByPlayer rbp = playerRecipes.get(playerId);
         return rbp != null && rbp.knownRecipes.containsKey(recipeId);
      }
   }

   public static boolean isFavourite(long playerId, short recipeId) {
      RecipesByPlayer rbp = playerRecipes.get(playerId);
      return rbp != null && rbp.isFavourite(recipeId);
   }

   public static String getNotes(long playerId, short recipeId) {
      RecipesByPlayer rbp = playerRecipes.get(playerId);
      return rbp != null ? rbp.getNotes(recipeId) : "";
   }

   public static Recipe getPlayerKnownRecipeOrNull(long playerId, short recipeId) {
      RecipesByPlayer rbp = playerRecipes.get(playerId);
      return rbp != null ? rbp.knownRecipes.get(recipeId) : null;
   }

   public static final Recipe[] getTargetActionRecipesFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.isTargetActionType()) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe[] getContainerActionRecipesFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.isContainerActionType()) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe[] getHeatRecipesFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.isHeatType()) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe[] getTimeRecipesFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.isTimeType()) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final ItemTemplate[] getKnownCookersFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<ItemTemplate> knownCookers = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.hasCooker()) {
            knownCookers.addAll(recipe.getCookerTemplates());
         }
      }

      return knownCookers.toArray(new ItemTemplate[knownCookers.size()]);
   }

   public static final Recipe[] getCookerRecipesFor(long playerId, int cookerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.hasCooker(cookerId)) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final ItemTemplate[] getKnownContainersFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<ItemTemplate> knownContainers = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.hasContainer()) {
            knownContainers.addAll(recipe.getContainerTemplates());
         }
      }

      return knownContainers.toArray(new ItemTemplate[knownContainers.size()]);
   }

   public static final Recipe[] getContainerRecipesFor(long playerId, int containerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.hasContainer(containerId)) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final ItemTemplate[] getKnownToolsFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<ItemTemplate> knownTools = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.getActiveItem() != null && recipe.getActiveItem().getTemplate().isCookingTool()) {
            knownTools.add(recipe.getActiveItem().getTemplate());
         }
      }

      return knownTools.toArray(new ItemTemplate[knownTools.size()]);
   }

   public static final Recipe[] getToolRecipesFor(long playerId, int toolId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.getActiveItem() != null && recipe.getActiveItem().getTemplateId() == toolId) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Ingredient[] getKnownIngredientsFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Map<String, Ingredient> knownIngredients = new HashMap<>();

      for(Recipe recipe : recipes) {
         for(Ingredient i : recipe.getAllIngredients(true).values()) {
            Ingredient ingredient = i.clone(null);
            knownIngredients.put(ingredient.getName(true), ingredient);
         }
      }

      return knownIngredients.values().toArray(new Ingredient[knownIngredients.size()]);
   }

   public static final Ingredient[] getRecipeIngredientsFor(long playerId, int recipeId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Map<String, Ingredient> knownIngredients = new HashMap<>();

      for(Recipe recipe : recipes) {
         if (recipe.getRecipeId() == recipeId) {
            knownIngredients.putAll(recipe.getAllIngredients(true));
            break;
         }
      }

      return knownIngredients.values().toArray(new Ingredient[knownIngredients.size()]);
   }

   public static final Recipe[] getIngredientRecipesFor(long playerId, Ingredient ingredient) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         Map<String, Ingredient> recipeIngredients = recipe.getAllIngredients(true);

         for(Ingredient i : recipeIngredients.values()) {
            if (i.getName(false).equalsIgnoreCase(ingredient.getName(false))) {
               knownRecipes.add(recipe);
               break;
            }
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe[] getSearchRecipesFor(long playerId, String searchFor) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         if (recipe.getName().toLowerCase().contains(searchFor.toLowerCase())) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe[] getKnownRecipesFor(long playerId) {
      Set<Recipe> recipes = getKnownRecipesSetFor(playerId);
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipes) {
         knownRecipes.add(recipe);
      }

      return knownRecipes.toArray(new Recipe[knownRecipes.size()]);
   }

   public static final Recipe getRecipe(long playerId, int recipeId) {
      for(Recipe recipe : getKnownRecipesSetFor(playerId)) {
         if (recipe.getRecipeId() == recipeId) {
            return recipe;
         }
      }

      return null;
   }

   public static void packRecipes(DataOutputStream dos, long playerId) throws IOException {
      Set<Recipe> recipes = new HashSet<>();
      RecipesByPlayer rbp = playerRecipes.get(playerId);
      if (rbp != null) {
         recipes.addAll(rbp.knownRecipes.values());
      }

      dos.writeChar(88);
      dos.writeShort(recipes.size());
      logger.log(Level.INFO, "packing " + recipes.size() + " known recipes!");

      for(Recipe recipe : recipes) {
         dos.writeChar(82);
         recipe.pack(dos);
      }

      if (rbp != null) {
         int count = 0;

         for(Boolean b : rbp.playerFavourites.values()) {
            if (b) {
               ++count;
            }
         }

         logger.log(Level.INFO, "packing " + count + " favourites!");
         dos.writeShort(count);

         for(Entry<Short, Boolean> entry : rbp.playerFavourites.entrySet()) {
            if (entry.getValue()) {
               dos.writeShort(entry.getKey());
            }
         }

         count = 0;

         for(String n : rbp.playerNotes.values()) {
            if (n.length() > 0) {
               ++count;
            }
         }

         logger.log(Level.INFO, "packing " + count + " notes!");
         dos.writeShort(count);

         for(Entry<Short, String> entry : rbp.playerNotes.entrySet()) {
            if (entry.getValue().length() > 0) {
               dos.writeShort(entry.getKey());
               byte[] notesAsBytes = entry.getValue().getBytes("UTF-8");
               dos.writeByte((byte)notesAsBytes.length);
               dos.write(notesAsBytes);
            }
         }
      } else {
         dos.writeShort(0);
         dos.writeShort(0);
      }
   }

   public static void unPackRecipes(DataInputStream dis, long playerId) throws IOException {
      deleteRecipesForPlayer(playerId);
      if (dis.readChar() != 'X') {
         throw new IOException(new Exception("unpacking error, no start recipe list 'X' char"));
      } else {
         int count = dis.readShort();
         logger.log(Level.INFO, "unpacking " + count + " known recipes!");
         if (count > 0) {
            RecipesByPlayer rbp = getRecipesByPlayer(playerId, true);

            for(int x = 0; x < count; ++x) {
               if (dis.readChar() != 'R') {
                  throw new IOException(new Exception("unpacking error, no start recipe 'R' char for recipe " + x + " out of " + count + "."));
               }

               try {
                  Recipe recipe = new Recipe(dis);
                  addRecipe(rbp, recipe);
               } catch (NoSuchTemplateException var11) {
                  logger.log(Level.INFO, "unpacking fail: " + var11.getMessage(), (Throwable)var11);
                  throw new IOException(var11.getMessage());
               }
            }
         }

         int var12 = dis.readShort();
         logger.log(Level.INFO, "unpacking " + var12 + " favourites!");
         if (var12 > 0) {
            for(int x = 0; x < var12; ++x) {
               short recipeId = dis.readShort();
               setIsFavourite(playerId, recipeId, true);
            }
         }

         var12 = dis.readShort();
         logger.log(Level.INFO, "unpacking " + var12 + " notes!");
         if (var12 > 0) {
            for(int x = 0; x < var12; ++x) {
               short recipeId = dis.readShort();
               byte lByte = dis.readByte();
               int length = lByte & 255;
               byte[] tempStringArr = new byte[length];
               int read = dis.read(tempStringArr);
               if (length != read) {
                  logger.warning("Read in " + read + ", expected " + length);
               }

               String notes = new String(tempStringArr, "UTF-8");
               setNotes(playerId, recipeId, notes);
            }
         }
      }
   }

   public static boolean saveRecipe(@Nullable Creature performer, Recipe templateRecipe, long playerId, @Nullable Item source, Item target) {
      if (templateRecipe.isKnown()) {
         return false;
      } else if (playerId == -10L) {
         logger.log(Level.WARNING, "Failed to save recipe '" + templateRecipe.getName() + "' (#" + templateRecipe.getRecipeId() + "): No player ID given");
         return false;
      } else {
         if (performer != null) {
            Recipes.setRecipeNamer(templateRecipe, performer);
         }

         boolean isChanged = false;
         RecipesByPlayer rbp = getRecipesByPlayer(playerId, true);
         Recipe playerRecipe = rbp.getRecipe(templateRecipe.getRecipeId());
         if (playerRecipe != null) {
            if (templateRecipe.hasCooker()) {
               Item cooker = target.getTopParentOrNull();
               if (cooker != null && !playerRecipe.hasCooker(cooker.getTemplateId())) {
                  playerRecipe.addToCookerList(cooker.getTemplateId());
                  dbSaveRecipeCooker(playerId, playerRecipe.getRecipeId(), cooker.getTemplateId());
                  isChanged = true;
               }
            }

            if (templateRecipe.hasContainer() && target != null && !playerRecipe.hasContainer(target.getTemplateId())) {
               playerRecipe.addToContainerList(target.getTemplateId());
               dbSaveRecipeContainer(playerId, playerRecipe.getRecipeId(), target.getTemplateId());
               isChanged = true;
            }

            if (source != null & templateRecipe.getActiveItem() != null
               && playerRecipe.getActiveItem() != null
               && templateRecipe.getActiveItem().isFoodGroup()
               && !playerRecipe.getActiveItem().isFoodGroup()
               && playerRecipe.getActiveItem().getTemplateId() != source.getTemplateId()) {
               Ingredient pi = playerRecipe.getIngredientById(templateRecipe.getActiveItem().getIngredientId());
               pi.setTemplate(templateRecipe.getActiveItem().getTemplate());
               dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), pi);
               playerRecipe.addIngredient(pi);
               isChanged = true;
            }

            if (templateRecipe.getTargetItem() != null
               && playerRecipe.getTargetItem() != null
               && templateRecipe.getTargetItem().isFoodGroup()
               && !playerRecipe.getTargetItem().isFoodGroup()
               && playerRecipe.getTargetItem().getTemplateId() != target.getTemplateId()) {
               Ingredient pi = playerRecipe.getIngredientById(templateRecipe.getTargetItem().getIngredientId());
               pi.setTemplate(templateRecipe.getTargetItem().getTemplate());
               dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), pi);
               playerRecipe.addIngredient(pi);
               isChanged = true;
            }

            if (target.isFoodMaker() || target.getTemplate().isCooker()) {
               for(Item item : target.getItemsAsArray()) {
                  Ingredient ti = templateRecipe.findMatchingIngredient(item);
                  if (ti == null) {
                     logger.log(Level.WARNING, "Failed to find matching ingredient:" + item.getName() + ".");
                  } else {
                     Ingredient pi = playerRecipe.getIngredientById(ti.getIngredientId());
                     if (pi == null) {
                        Ingredient ingredient = ti.clone(item);
                        ingredient.setTemplate(item.getTemplate());
                        dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), ingredient);
                        playerRecipe.addIngredient(ingredient);
                        isChanged = true;
                     } else if (ti.isFoodGroup() && !pi.isFoodGroup()) {
                        pi.setTemplate(ti.getTemplate());
                        dbSaveRecipeIngredient(true, playerId, playerRecipe.getRecipeId(), pi);
                        playerRecipe.addIngredient(pi);
                        isChanged = true;
                     }
                  }
               }
            }

            if (isChanged && performer != null) {
               performer.getCommunicator().sendCookbookRecipe(playerRecipe);
            }

            return false;
         } else {
            playerRecipe = new Recipe(templateRecipe.getRecipeId());
            dbSaveRecipe(playerId, playerRecipe.getRecipeId(), false, "");
            if (templateRecipe.hasCooker()) {
               Item cooker = target.getTopParentOrNull();
               if (cooker != null) {
                  playerRecipe.addToCookerList((short)cooker.getTemplateId());
                  dbSaveRecipeCooker(playerId, playerRecipe.getRecipeId(), cooker.getTemplateId());
               }
            }

            if (templateRecipe.hasContainer()) {
               playerRecipe.addToContainerList((short)target.getTemplateId());
               dbSaveRecipeContainer(playerId, playerRecipe.getRecipeId(), target.getTemplateId());
            }

            if (templateRecipe.getActiveItem() != null && source != null) {
               Ingredient pi;
               if (templateRecipe.getActiveItem().getTemplateId() == 14) {
                  pi = new Ingredient(templateRecipe.getActiveItem().getTemplate(), false, (byte)-2);
               } else {
                  pi = new Ingredient(source.getTemplate(), false, (byte)-2);
               }

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
               dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi);
               playerRecipe.addIngredient(pi);
            }

            if (templateRecipe.getTargetItem() != null) {
               Ingredient pi = new Ingredient(target.getTemplate(), false, (byte)-1);
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
               dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi);
               playerRecipe.addIngredient(pi);
            }

            if (target.isFoodMaker() || target.getTemplate().isCooker()) {
               templateRecipe.clearFound();

               for(Item item : target.getItemsAsArray()) {
                  Ingredient ti = templateRecipe.findMatchingIngredient(item);
                  if (ti == null) {
                     logger.log(Level.WARNING, "Failed to find matching ingredient:" + item.getName() + ".");
                  } else if (!ti.wasFound(true, false)) {
                     ti.setFound(true);
                     Ingredient pi = ti.clone(item);
                     pi.setTemplate(item.getTemplate());
                     dbSaveRecipeIngredient(false, playerId, playerRecipe.getRecipeId(), pi);
                     playerRecipe.addIngredient(pi);
                  }
               }
            }

            rbp.addRecipe(playerRecipe);
            if (performer != null) {
               performer.getCommunicator().sendCookbookRecipe(playerRecipe);
            }

            return true;
         }
      }
   }

   public static boolean addRecipe(Creature performer, Recipe recipe) {
      RecipesByPlayer rbp = getRecipesByPlayer(performer.getWurmId(), true);
      if (addRecipe(rbp, recipe)) {
         return true;
      } else {
         performer.getCommunicator().sendNormalServerMessage("That recipe is already in your cookbook!");
         return false;
      }
   }

   private static boolean addRecipe(RecipesByPlayer rbp, Recipe recipe) {
      Recipe playerRecipe = rbp.getRecipe(recipe.getRecipeId());
      if (playerRecipe != null) {
         return false;
      } else {
         dbSaveRecipe(rbp.getPlayerId(), recipe.getRecipeId(), rbp.isFavourite(recipe.getRecipeId()), rbp.getNotes(recipe.getRecipeId()));
         if (recipe.hasCooker()) {
            for(ItemTemplate cooker : recipe.getCookerTemplates()) {
               dbSaveRecipeCooker(rbp.getPlayerId(), recipe.getRecipeId(), cooker.getTemplateId());
            }
         }

         if (recipe.hasContainer()) {
            for(ItemTemplate container : recipe.getContainerTemplates()) {
               dbSaveRecipeContainer(rbp.getPlayerId(), recipe.getRecipeId(), container.getTemplateId());
            }
         }

         if (recipe.getActiveItem() != null) {
            dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), recipe.getActiveItem());
         }

         if (recipe.getTargetItem() != null) {
            dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), recipe.getTargetItem());
         }

         for(Ingredient i : recipe.getAllIngredients(true).values()) {
            dbSaveRecipeIngredient(false, rbp.getPlayerId(), recipe.getRecipeId(), i);
         }

         rbp.addRecipe(recipe);
         return true;
      }
   }

   public static void removeRecipeForPlayer(long playerId, short recipeId) {
      dbRemovePlayerRecipe(playerId, recipeId);
      RecipesByPlayer rbp = getRecipesByPlayer(playerId, false);
      if (rbp != null) {
         rbp.removeRecipe(recipeId);
      }
   }

   public static void deleteRecipesByNumber(short recipeId) {
      for(Entry<Long, RecipesByPlayer> entry : playerRecipes.entrySet()) {
         Recipe recipe = entry.getValue().getRecipe(recipeId);
         if (recipe != null) {
            RecipesByPlayer rbp = entry.getValue();
            long playerId = entry.getKey();
            dbRemovePlayerRecipe(playerId, recipeId);
            rbp.removeRecipe(recipeId);

            try {
               Player player = Players.getInstance().getPlayer(playerId);
               player.getCommunicator().sendCookbookRecipe(recipe);
            } catch (NoSuchPlayerException var8) {
            }
         }
      }
   }

   public static void deleteRecipesForPlayer(long playerId) {
      dbRemovePlayerRecipes(playerId);
      playerRecipes.remove(playerId);
   }

   public static void deleteAllKnownRecipes() {
      dbRemoveAllPlayerRecipes();
      playerRecipes.clear();
   }

   public static void setIsFavourite(long playerId, short recipeId, boolean isFavourite) {
      RecipesByPlayer rbp = getRecipesByPlayer(playerId, true);
      rbp.setFavourite(recipeId, isFavourite);
      dbUpdateRecipeFavourite(playerId, recipeId, isFavourite);
   }

   public static void setNotes(long playerId, short recipeId, String notes) {
      RecipesByPlayer rbp = getRecipesByPlayer(playerId, true);
      rbp.setNotes(recipeId, notes);
      dbUpdateRecipeNotes(playerId, recipeId, notes);
   }

   private static void dbSaveRecipe(long playerId, short recipeId, boolean favourite, String notes) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RECIPESPLAYER (PLAYERID,RECIPEID,FAVOURITE,NOTES) VALUES(?,?,?,?)");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.setBoolean(3, favourite);
         ps.setString(4, notes);
         ps.executeUpdate();
      } catch (SQLException var11) {
         logger.log(Level.WARNING, "Failed to save player recipe: " + var11.getMessage(), (Throwable)var11);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbSaveRecipeCooker(long playerId, short recipeId, int cookerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RECIPEPLAYERCOOKERS (PLAYERID,RECIPEID,COOKERID) VALUES(?,?,?)");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.setShort(3, (short)cookerId);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save player recipe cooker: " + var10.getMessage(), (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbSaveRecipeContainer(long playerId, short recipeId, int containerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RECIPEPLAYERCONTAINERS (PLAYERID,RECIPEID,CONTAINERID) VALUES(?,?,?)");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.setShort(3, (short)containerId);
         ps.executeUpdate();
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to save player recipe container: " + var10.getMessage(), (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbSaveRecipeIngredient(boolean update, long playerId, short recipeId, Ingredient ingredient) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         try {
            dbcon = DbConnector.getItemDbCon();
            if (update) {
               ps = dbcon.prepareStatement(
                  "UPDATE RECIPEPLAYERINGREDIENTS SET TEMPLATEID=?,CSTATE=?,PSTATE=?,MATERIAL=?,REALTEMPLATEID=? WHERE PLAYERID=? AND RECIPEID=? AND INGREDIENTID=?"
               );
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
               int did = ps.executeUpdate();
               if (did > 0) {
                  return;
               }

               DbUtilities.closeDatabaseObjects(ps, null);
            }

            ps = dbcon.prepareStatement(
               "INSERT INTO RECIPEPLAYERINGREDIENTS (PLAYERID,RECIPEID,INGREDIENTID,GROUPID,TEMPLATEID,CSTATE,PSTATE,MATERIAL,REALTEMPLATEID) VALUES(?,?,?,?,?,?,?,?,?)"
            );
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
         } catch (SQLException var11) {
            logger.log(Level.WARNING, "Failed to save player recipe ingredient: " + var11.getMessage(), (Throwable)var11);
         }
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbUpdateRecipeFavourite(long playerId, short recipeId, boolean isFavourite) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         logger.info("update favourite for " + recipeId);
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("UPDATE RECIPESPLAYER SET FAVOURITE=? WHERE PLAYERID=? AND RECIPEID=?");
         ps.setBoolean(1, isFavourite);
         ps.setLong(2, playerId);
         ps.setShort(3, recipeId);
         int did = ps.executeUpdate();
         if (did <= 0) {
            logger.info("Update favourite failed, so trying create " + did);
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement("INSERT INTO RECIPESPLAYER (PLAYERID,RECIPEID,FAVOURITE,NOTES) VALUES(?,?,?,?)");
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setBoolean(3, isFavourite);
            ps.setString(4, "");
            ps.executeUpdate();
            return;
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to update player (" + playerId + ") recipe (" + recipeId + ") favourite: " + var10.getMessage(), (Throwable)var10);
         return;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbUpdateRecipeNotes(long playerId, short recipeId, String notes) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         logger.info("update notes for " + recipeId);
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("UPDATE RECIPESPLAYER SET NOTES=? WHERE PLAYERID=? AND RECIPEID=?");
         ps.setString(1, notes);
         ps.setLong(2, playerId);
         ps.setShort(3, recipeId);
         int did = ps.executeUpdate();
         if (did <= 0) {
            logger.info("Update notes failed, so trying create " + did);
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement("INSERT INTO RECIPESPLAYER (PLAYERID,RECIPEID,FAVOURITE,NOTES) VALUES(?,?,?,?)");
            ps.setLong(1, playerId);
            ps.setShort(2, recipeId);
            ps.setBoolean(3, false);
            ps.setString(4, notes);
            ps.executeUpdate();
            return;
         }
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Failed to update player (" + playerId + ") recipe (" + recipeId + ") notes: " + var10.getMessage(), (Throwable)var10);
         return;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbRemovePlayerRecipes(long playerId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM RECIPESPLAYER WHERE PLAYERID=?");
         ps.setLong(1, playerId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCOOKERS WHERE PLAYERID=?");
         ps.setLong(1, playerId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCONTAINERS WHERE PLAYERID=?");
         ps.setLong(1, playerId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERINGREDIENTS WHERE PLAYERID=?");
         ps.setLong(1, playerId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to remove player recipes: " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbRemovePlayerRecipe(long playerId, short recipeId) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM RECIPESPLAYER WHERE PLAYERID=? AND RECIPEID=?");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCOOKERS WHERE PLAYERID=? AND RECIPEID=?");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCONTAINERS WHERE PLAYERID=? AND RECIPEID=?");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERINGREDIENTS WHERE PLAYERID=? AND RECIPEID=?");
         ps.setLong(1, playerId);
         ps.setShort(2, recipeId);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to remove player recipes: " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbRemoveAllPlayerRecipes() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM RECIPESPLAYER");
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCOOKERS");
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERCONTAINERS");
         ps.executeUpdate();
         DbUtilities.closeDatabaseObjects(ps, null);
         ps = dbcon.prepareStatement("DELETE FROM RECIPEPLAYERINGREDIENTS");
         ps.executeUpdate();
      } catch (SQLException var6) {
         logger.log(Level.WARNING, "Failed to remove all player recipes: " + var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
