package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.gui.folders.DistEntity;
import com.wurmonline.server.gui.folders.Folders;
import com.wurmonline.server.gui.folders.GameEntity;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementList;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.support.JSONArray;
import com.wurmonline.server.support.JSONException;
import com.wurmonline.server.support.JSONObject;
import com.wurmonline.server.support.JSONTokener;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.IconConstants;
import com.wurmonline.shared.constants.ItemMaterials;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Recipes implements ItemMaterials, MiscConstants, AchievementList {
   private static final Logger logger = Logger.getLogger(Recipes.class.getName());
   private static final Map<Short, Recipe> recipes = new HashMap<>();
   private static final Map<Short, String> namedRecipes = new HashMap<>();
   private static final List<Recipe> recipesList = new ArrayList<>();
   private static final String GET_ALL_NAMED_RECIPES = "SELECT * FROM RECIPESNAMED";
   private static final String CREATE_NAMED_RECIPE = "INSERT INTO RECIPESNAMED (RECIPEID, NAMER) VALUES(?,?)";
   private static final String DELETE_NAMED_RECIPE = "DELETE FROM RECIPESNAMED WHERE RECIPEID=?";
   public static final byte NOT_FOUND = 0;
   public static final byte FOUND = 1;
   public static final byte SWAPPED = 2;

   public static void add(Recipe recipe) {
      recipes.put(recipe.getMenuId(), recipe);
      recipesList.add(recipe);
   }

   public static boolean exists(short recipeId) {
      return recipes.containsKey((short)(recipeId + 8000));
   }

   public static void loadAllRecipes() {
      loadRecipes(Folders.getDist().getPathFor(DistEntity.Recipes));
      if (!GameEntity.Recipes.existsIn(Folders.getCurrent())) {
         try {
            Files.createDirectory(Folders.getCurrent().getPathFor(GameEntity.Recipes));
         } catch (IOException var1) {
            logger.warning("Could not create recipe folder");
            return;
         }
      }

      loadRecipes(Folders.getCurrent().getPathFor(GameEntity.Recipes));
   }

   public static void loadRecipes(Path path) {
      logger.info("Loading all Recipes");
      long start = System.nanoTime();

      try {
         Files.walk(path)
            .sorted()
            .forEachOrdered(
               p -> {
                  if (!Files.isDirectory(p)) {
                     if (p.getFileName().toString().startsWith("recipe ")
                        && p.getFileName().toString().endsWith(".json")
                        && p.getFileName().toString().length() == 16) {
                        readRecipeFile(p.toString());
                     } else {
                        logger.log(
                           Level.INFO,
                           "recipe file name ("
                              + p.toString()
                              + ") is not in correct format, expected \" recipe xxxx.json\" where xxxx are the recipe id (same as in the file)."
                        );
                     }
                  }
               }
            );
      } catch (IOException var6) {
         logger.warning("Exception loading recipes");
         var6.printStackTrace();
      }

      int numberOfRecipes = recipes.size();
      logger.log(Level.INFO, "Total number of recipes=" + numberOfRecipes + ".");
      int numberOfKnownRecipes = RecipesByPlayer.loadAllPlayerKnownRecipes();
      logger.log(Level.INFO, "Number of player known recipes=" + numberOfKnownRecipes + ".");
      int numberOfNamedRecipes = dbNamedRecipes();
      logger.log(Level.INFO, "Number of named recipes=" + numberOfNamedRecipes + ".");
      logger.log(Level.INFO, "Recipes loaded. That took " + (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
   }

   public static Recipe[] getNamedRecipesFor(String playerName) {
      Set<Recipe> recipes = new HashSet<>();

      for(Entry<Short, String> entry : namedRecipes.entrySet()) {
         if (entry.getValue().equalsIgnoreCase(playerName)) {
            Recipe recipe = getRecipeById(entry.getKey());
            if (recipe != null) {
               recipes.add(recipe);
            }
         }
      }

      return recipes.toArray(new Recipe[recipes.size()]);
   }

   public static Recipe[] getNamedRecipes() {
      Set<Recipe> recipes = new HashSet<>();

      for(Entry<Short, String> entry : namedRecipes.entrySet()) {
         Recipe recipe = getRecipeById(entry.getKey());
         if (recipe != null) {
            recipes.add(recipe);
         }
      }

      return recipes.toArray(new Recipe[recipes.size()]);
   }

   private static int dbNamedRecipes() {
      int count = 0;
      int failed = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM RECIPESNAMED");

         for(rs = ps.executeQuery(); rs.next(); ++count) {
            short recipeId = rs.getShort("RECIPEID");
            String namer = rs.getString("NAMER");
            long playerId = PlayerInfoFactory.getWurmId(namer);
            namedRecipes.put(recipeId, namer);
         }
      } catch (SQLException var12) {
         logger.log(Level.WARNING, "Failed to get namer on known recipes: " + var12.getMessage(), (Throwable)var12);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      if (failed > 0) {
         logger.log(Level.INFO, "Number of removed named recipes=" + failed + ".");
      }

      return count;
   }

   public static void setRecipeNamer(Recipe recipe, Creature creature) {
      if (recipe.isNameable() && !namedRecipes.containsKey(recipe.getRecipeId()) && creature.isPlayer() && !creature.hasFlag(50)) {
         namedRecipes.put(recipe.getRecipeId(), creature.getName());
         creature.setFlag(50, true);
         dbSetRecipeNamer(recipe.getRecipeId(), creature.getName());

         for(Player player : Players.getInstance().getPlayers()) {
            if (player.isViewingCookbook() && RecipesByPlayer.isKnownRecipe(player.getWurmId(), recipe.getRecipeId())) {
               player.getCommunicator().sendCookbookRecipe(recipe);
            }
         }
      }
   }

   @Nullable
   public static String getRecipeNamer(short recipeId) {
      return namedRecipes.get(recipeId);
   }

   public static short getNamedRecipe(String namer) {
      for(Entry<Short, String> entry : namedRecipes.entrySet()) {
         if (entry.getValue().equalsIgnoreCase(namer)) {
            return entry.getKey();
         }
      }

      return 0;
   }

   private static void dbSetRecipeNamer(short recipeId, String namer) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("INSERT INTO RECIPESNAMED (RECIPEID, NAMER) VALUES(?,?)");
         ps.setShort(1, recipeId);
         ps.setString(2, namer);
         ps.executeUpdate();
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to save namer (" + namer + ") on recipe (" + recipeId + "): " + var9.getMessage(), (Throwable)var9);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static boolean removeRecipeNamer(short recipeId) {
      String namer = namedRecipes.remove(recipeId);
      if (namer != null) {
         PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithName(namer);
         if (pInfo != null) {
            pInfo.setFlag(50, false);
         }
      }

      dbRemoveRecipeNamer(recipeId);
      return namer != null;
   }

   private static void dbRemoveRecipeNamer(short recipeId) {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM RECIPESNAMED WHERE RECIPEID=?");
         ps.setShort(1, recipeId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, "Failed to delete entry for recipe (" + recipeId + "): " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void readRecipeFile(String fileName) {
      try {
         File file = new File(fileName);
         if (!file.exists()) {
            logger.log(Level.INFO, "file '" + fileName + "' not found!");
            return;
         }

         BufferedReader br = new BufferedReader(new FileReader(file));
         StringBuilder sb = new StringBuilder();

         String line;
         while((line = br.readLine()) != null) {
            sb.append(line.trim());
         }

         br.close();
         InputStream in = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

         try {
            String name;
            String recipeId;
            try {
               name = "";
               recipeId = "unknown";
               String skillName = "";

               try {
                  JSONTokener tk = new JSONTokener(in);
                  JSONObject recipeJO = new JSONObject(tk);
                  if (recipeJO.has("name")) {
                     name = recipeJO.getString("name");
                  }

                  if (recipeJO.has("recipeid")) {
                     recipeId = recipeJO.getString("recipeid");
                     if (recipeId.length() == 1) {
                        recipeId = "000" + recipeId;
                     } else if (recipeId.length() == 2) {
                        recipeId = "00" + recipeId;
                     } else if (recipeId.length() == 3) {
                        recipeId = "0" + recipeId;
                     }

                     if (recipeId.length() != 4) {
                        throw new JSONException("RecipeId " + recipeId + " for '" + name + "' is wrong length, should be 1 to 4 digits.");
                     }

                     if (!fileName.endsWith("recipe " + recipeId + ".json")) {
                        throw new JSONException("RecipeId " + recipeId + " does not match the filename (" + fileName + ").");
                     }

                     short rid = 0;

                     try {
                        rid = Short.parseShort(recipeId);
                     } catch (NumberFormatException var32) {
                        throw new JSONException("RecipeId for '" + name + "' (" + recipeId + ") is not a number.");
                     }

                     if (name.length() == 0) {
                        throw new JSONException("Name missing for recipe id of " + recipeId + ".");
                     }

                     checkRecipeSchema(rid, "recipe", recipeJO);
                     if (rid >= 1 && rid <= 1999) {
                        if (recipes.containsKey((short)(rid + 8000))) {
                           logger.info("Recipe '" + name + "' (" + recipeId + ") already exists, replacing");
                           recipes.remove((short)(rid + 8000));
                        }

                        Recipe recipe = new Recipe(name, rid);
                        if (recipeJO.has("skill")) {
                           skillName = recipeJO.getString("skill");
                        }

                        if (skillName.length() == 0) {
                           throw new JSONException("Skill name missing for '" + name + "' (" + recipeId + ").");
                        }

                        int skillId = SkillSystem.getSkillByName(skillName);
                        if (skillId <= 0) {
                           throw new JSONException("Skill '" + skillName + "' does not exist in recipe '" + name + "' (" + recipeId + ").");
                        }

                        recipe.setSkill(skillId, skillName);
                        if (recipeJO.has("known")) {
                           recipe.setKnown(recipeJO.getBoolean("known"));
                        }

                        if (recipeJO.has("nameable")) {
                           recipe.setNameable(recipeJO.getBoolean("nameable"));
                        }

                        if (recipeJO.has("lootable")) {
                           JSONObject jo = recipeJO.getJSONObject("lootable");
                           checkRecipeSchema(rid, "lootable", jo);
                           int cid = -10;
                           byte rarity = -1;
                           if (!jo.has("creature")) {
                              throw new JSONException("Recipe '" + name + "' (" + recipeId + ") Lootable is missing creature.");
                           }

                           String creatureName = jo.getString("creature");

                           try {
                              CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(creatureName);
                              cid = ct.getTemplateId();
                           } catch (Exception var31) {
                              throw new JSONException("Recipe '" + name + "' (" + recipeId + ") Creature '" + creatureName + "' does not exist as a creature.");
                           }

                           if (!jo.has("rarity")) {
                              throw new JSONException("Recipe '" + name + "' (" + recipeId + ") Lootable is missing rarity.");
                           }

                           creatureName = jo.getString("rarity");
                           rarity = convertRarityStringIntoByte(recipe.getRecipeId(), creatureName);
                           recipe.setLootable(cid, rarity);
                        }

                        if (recipeJO.has("trigger")) {
                           String triggerName = recipeJO.getString("trigger");
                           byte triggerId = -1;
                           switch(triggerName) {
                              case "heat":
                                 triggerId = 1;
                                 break;
                              case "time":
                                 triggerId = 0;
                                 break;
                              case "create":
                                 triggerId = 2;
                           }

                           if (triggerId < 0) {
                              throw new JSONException("Trigger '" + triggerName + "' does not exist.");
                           }

                           recipe.setTrigger(triggerId);
                        }

                        if (recipeJO.has("cookers")) {
                           JSONArray cja = recipeJO.getJSONArray("cookers");

                           for(int c = 0; c < cja.length(); ++c) {
                              JSONObject jo = cja.getJSONObject(c);
                              checkRecipeSchema(rid, "cookers", jo);
                              String cooker = jo.getString("id");
                              ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(cooker);
                              if (cookerIT == null) {
                                 throw new JSONException("Cooker '" + cooker + "' does not exist as a template.");
                              }

                              if (!cookerIT.isCooker()) {
                                 throw new JSONException("Cooker '" + cooker + "' cannot be used to make food in.");
                              }

                              int dif = 0;
                              if (jo.has("difficulty")) {
                                 dif = jo.getInt("difficulty");
                              }

                              if (dif < 0 || dif > 100) {
                                 throw new JSONException("Difficulty for cooker '" + cooker + "' is out of range (0..100), was " + dif + " in recipe.");
                              }

                              recipe.addToCookerList(cookerIT.getTemplateId(), cooker, dif);
                           }
                        }

                        if (recipeJO.has("containers")) {
                           JSONArray cja = recipeJO.getJSONArray("containers");

                           for(int c = 0; c < cja.length(); ++c) {
                              JSONObject jo = cja.getJSONObject(c);
                              checkRecipeSchema(rid, "containers", jo);
                              String container = jo.getString("id");
                              ItemTemplate containerIT = ItemTemplateFactory.getInstance().getTemplate(container);
                              if (containerIT == null) {
                                 throw new JSONException("Container '" + container + "' does not exist as a template.");
                              }

                              if (!containerIT.isFoodMaker() && !containerIT.isRecipeItem()) {
                                 throw new JSONException("Container '" + container + "' cannot be used to make food in.");
                              }

                              int dif = 0;
                              if (jo.has("difficulty")) {
                                 dif = jo.getInt("difficulty");
                              }

                              if (dif < 0 || dif > 100) {
                                 throw new JSONException("Difficulty for container '" + container + "' is out of range (0..100), was " + dif + " in recipe.");
                              }

                              recipe.addToContainerList(containerIT.getTemplateId(), container, dif);
                           }
                        }

                        if (recipeJO.has("active")) {
                           JSONObject activeJO = recipeJO.getJSONObject("active");
                           checkRecipeSchema(rid, "active", activeJO);
                           readIngredient(recipe, activeJO, "Active item", false, true, true, -2);
                        }

                        if (recipeJO.has("target")) {
                           JSONObject targetJO = recipeJO.getJSONObject("target");
                           checkRecipeSchema(rid, "target", targetJO);
                           readIngredient(recipe, targetJO, "Target item", false, true, true, -1);
                        }

                        if (recipeJO.has("ingredients")) {
                           JSONObject ingredientsJO = recipeJO.getJSONObject("ingredients");
                           checkRecipeSchema(rid, "ingredients group", ingredientsJO);
                           if (ingredientsJO.has("mandatory")) {
                              IngredientGroup group = new IngredientGroup((byte)1);
                              recipe.addToIngredientGroupList(group);
                              JSONArray groupJA = ingredientsJO.getJSONArray("mandatory");

                              for(int i = 0; i < groupJA.length(); ++i) {
                                 JSONObject ingredientJO = groupJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "mandatory", ingredientJO);
                                 readIngredient(recipe, ingredientJO, "Mandatory Ingredient", false, false, true, recipe.getCurrentGroupId());
                              }
                           }

                           if (ingredientsJO.has("zeroorone")) {
                              JSONArray zerooroneJA = ingredientsJO.getJSONArray("zeroorone");

                              for(int i = 0; i < zerooroneJA.length(); ++i) {
                                 IngredientGroup group = new IngredientGroup((byte)2);
                                 recipe.addToIngredientGroupList(group);
                                 JSONObject groupJO = zerooroneJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "zeroorone group", groupJO);
                                 JSONArray listJA = groupJO.getJSONArray("list");

                                 for(int j = 0; j < listJA.length(); ++j) {
                                    JSONObject listJO = listJA.getJSONObject(j);
                                    checkRecipeSchema(rid, "zeroorone", listJO);
                                    readIngredient(recipe, listJO, "ZeroOrOne Ingredient", false, false, true, recipe.getCurrentGroupId());
                                 }
                              }
                           }

                           if (ingredientsJO.has("oneof")) {
                              JSONArray oneOfJA = ingredientsJO.getJSONArray("oneof");

                              for(int i = 0; i < oneOfJA.length(); ++i) {
                                 IngredientGroup group = new IngredientGroup((byte)3);
                                 recipe.addToIngredientGroupList(group);
                                 JSONObject groupJO = oneOfJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "oneof group", groupJO);
                                 JSONArray listJA = groupJO.getJSONArray("list");

                                 for(int j = 0; j < listJA.length(); ++j) {
                                    JSONObject listJO = listJA.getJSONObject(j);
                                    checkRecipeSchema(rid, "oneof", listJO);
                                    readIngredient(recipe, listJO, "OneOf Ingredient ", false, false, true, recipe.getCurrentGroupId());
                                 }
                              }
                           }

                           if (ingredientsJO.has("oneormore")) {
                              JSONArray oneormoreJA = ingredientsJO.getJSONArray("oneormore");

                              for(int i = 0; i < oneormoreJA.length(); ++i) {
                                 IngredientGroup group = new IngredientGroup((byte)4);
                                 recipe.addToIngredientGroupList(group);
                                 JSONObject groupJO = oneormoreJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "oneormore group", groupJO);
                                 JSONArray listJA = groupJO.getJSONArray("list");

                                 for(int j = 0; j < listJA.length(); ++j) {
                                    JSONObject listJO = listJA.getJSONObject(j);
                                    checkRecipeSchema(rid, "oneormore", listJO);
                                    readIngredient(recipe, listJO, "OneOrMore Ingredient", false, false, true, recipe.getCurrentGroupId());
                                 }
                              }
                           }

                           if (ingredientsJO.has("optional")) {
                              IngredientGroup group = new IngredientGroup((byte)5);
                              recipe.addToIngredientGroupList(group);
                              JSONArray groupJA = ingredientsJO.getJSONArray("optional");

                              for(int i = 0; i < groupJA.length(); ++i) {
                                 JSONObject ingredientJO = groupJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "optional", ingredientJO);
                                 readIngredient(recipe, ingredientJO, "Optional Ingredient", false, false, true, recipe.getCurrentGroupId());
                              }
                           }

                           if (ingredientsJO.has("any")) {
                              IngredientGroup group = new IngredientGroup((byte)6);
                              recipe.addToIngredientGroupList(group);
                              JSONArray groupJA = ingredientsJO.getJSONArray("any");

                              for(int i = 0; i < groupJA.length(); ++i) {
                                 JSONObject ingredientJO = groupJA.getJSONObject(i);
                                 checkRecipeSchema(rid, "any", ingredientJO);
                                 readIngredient(recipe, ingredientJO, "Any Ingredient", false, false, true, recipe.getCurrentGroupId());
                              }
                           }
                        }

                        JSONObject resultJO = recipeJO.getJSONObject("result");
                        checkRecipeSchema(rid, "result", resultJO);
                        readIngredient(recipe, resultJO, "Result item", true, false, true, -3);
                        add(recipe);
                        return;
                     }

                     throw new JSONException("RecipeId for '" + name + "' (" + recipeId + ") is not in range (1..1999), was " + rid + " in recipe.");
                  }

                  throw new JSONException("RecipeId for '" + name + "' is missing.");
               } catch (JSONException var33) {
                  if (name.equals("")) {
                     logger.log(Level.WARNING, "Failed to load recipe from file " + fileName, (Throwable)var33);
                  } else {
                     logger.log(Level.WARNING, "Failed to load recipe " + name + " {" + recipeId + "}", (Throwable)var33);
                  }
               }
            } catch (JSONException var34) {
               name = var34;
               recipeId = var34.getMessage().indexOf("[character ");
               if (recipeId > -1) {
                  String rline = var34.getMessage().substring(recipeId + 11);
                  String ss = rline.substring(0, rline.indexOf(" line 1]"));

                  try {
                     int pos = Integer.parseInt(ss);
                     int p = sb.lastIndexOf("recipeid", pos);
                     if (p > -1) {
                        int pio = sb.indexOf("\"", p + 11);
                        String recipeId = sb.substring(p + 11, pio);
                        String lloc = sb.substring(pos - 17, pos - 2);
                        String bad = sb.substring(pos - 2, pos - 1);
                        String rloc = sb.substring(pos - 1, pos + 16);
                        logger.log(Level.INFO, "Bad recipeid:" + recipeId + " |" + lloc + ">" + bad + "<" + rloc);
                     }
                  } catch (StringIndexOutOfBoundsException | NumberFormatException var30) {
                  }
               }

               logger.log(Level.SEVERE, "Unable to load recipes:" + var34.getMessage(), (Throwable)var34);
            }
         } finally {
            in.close();
         }
      } catch (Exception var36) {
         logger.log(Level.SEVERE, "Unable to load recipes:" + var36.getMessage(), (Throwable)var36);
      }
   }

   private static void readIngredient(
      Recipe recipe, JSONObject currentJO, String ingredientType, boolean isResult, boolean canBeTool, boolean canBeRecipeItem, int groupId
   ) throws JSONException {
      String idName = currentJO.getString("id");
      ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(idName);
      if (template == null) {
         throw new JSONException(ingredientType + " '" + idName + "' does not exist as a template.");
      } else {
         if (!template.isFood() && !template.isLiquidCooking()) {
            if (canBeTool) {
               if (canBeRecipeItem) {
                  if (!template.isCookingTool() && !template.isRecipeItem()) {
                     throw new JSONException(ingredientType + " '" + idName + "' is not a food item or tool or usable item.");
                  }
               } else if (!template.isCookingTool()) {
                  throw new JSONException(ingredientType + " '" + idName + "' is not a food item or tool.");
               }
            } else {
               if (!canBeRecipeItem) {
                  throw new JSONException(ingredientType + " '" + idName + "' is not a food item.");
               }

               if (!template.isRecipeItem() && !template.canBeFermented()) {
                  throw new JSONException(ingredientType + " '" + idName + "' is not a food item or usable item.");
               }
            }
         }

         Ingredient ingredient = new Ingredient(template, isResult, (byte)groupId);
         if (currentJO.has("cstate")) {
            String cstate = currentJO.getString("cstate");
            ingredient.setCState(convertCookingStateIntoByte(recipe.getRecipeId(), cstate), cstate);
         }

         if (currentJO.has("pstate")) {
            String pstate = currentJO.getString("pstate");
            ingredient.setPState(convertPhysicalStateIntoByte(recipe.getRecipeId(), pstate), pstate);
         }

         if (currentJO.has("material")) {
            String material = currentJO.getString("material");
            byte mat = Materials.convertMaterialStringIntoByte(material);
            if (mat == 0) {
               throw new JSONException(ingredientType + " Material '" + material + "' does not exist as a material.");
            }

            ingredient.setMaterial(mat, material);
         }

         if (currentJO.has("realtemplate")) {
            String realTemplateName = currentJO.getString("realtemplate");
            if (realTemplateName.equalsIgnoreCase("none")) {
               ingredient.setRealTemplate(null);
            } else {
               ItemTemplate realIT = ItemTemplateFactory.getInstance().getTemplate(realTemplateName);
               if (realIT == null) {
                  throw new JSONException(ingredientType + " RealTemplate '" + realTemplateName + "' does not exist as a template.");
               }

               ingredient.setRealTemplate(realIT);
            }
         }

         if (currentJO.has("difficulty")) {
            int dif = currentJO.getInt("difficulty");
            if (dif < 0 || dif > 100) {
               throw new JSONException("Difficulty for ingredient '" + idName + "' is out of range (0..100), was " + dif + " in recipe.");
            }

            ingredient.setDifficulty(dif);
         }

         if (isResult) {
            if (currentJO.has("name")) {
               ingredient.setResultName(currentJO.getString("name"));
            }

            if (currentJO.has("refmaterial")) {
               String ingredientRef = currentJO.getString("refmaterial");
               if (recipe.getTargetItem() != null && recipe.getTargetItem().getTemplateName().equalsIgnoreCase(ingredientRef)) {
                  ingredient.setMaterialRef(ingredientRef);
               } else {
                  IngredientGroup group = recipe.getGroupByType(1);
                  if (group == null || !group.contains(ingredientRef)) {
                     throw new JSONException("Result ref material '" + ingredientRef + "' does not reference a mandatory ingredient.");
                  }

                  ingredient.setMaterialRef(ingredientRef);
               }
            }

            if (currentJO.has("refrealtemplate")) {
               String ingredientRef = currentJO.getString("refrealtemplate");
               if (recipe.getTargetItem() != null && recipe.getTargetItem().getTemplateName().equalsIgnoreCase(ingredientRef)) {
                  ingredient.setRealTemplateRef(ingredientRef);
               } else if (recipe.hasContainer(ingredientRef)) {
                  ingredient.setRealTemplateRef(ingredientRef);
               } else {
                  IngredientGroup group = recipe.getGroupByType(1);
                  if (group == null || !group.contains(ingredientRef)) {
                     throw new JSONException("Result ref realtemplate '" + ingredientRef + "' does not reference a mandatory ingredient.");
                  }

                  ingredient.setRealTemplateRef(ingredientRef);
               }
            }

            if (currentJO.has("achievement")) {
               String achievementStr = currentJO.getString("achievement");
               AchievementTemplate at = Achievement.getTemplate(achievementStr);
               if (at == null) {
                  throw new JSONException("Achievement '" + achievementStr + "' does not reference an achievement.");
               }

               if (!at.isForCooking()) {
                  throw new JSONException("Achievement '" + achievementStr + "' is not for recipes.");
               }

               recipe.setAchievementTriggered(at.getNumber(), at.getName());
            }

            if (currentJO.has("usetemplateweight")) {
               ingredient.setUseResultTemplateWeight(currentJO.getBoolean("usetemplateweight"));
            }

            if (currentJO.has("description")) {
               ingredient.setResultDescription(currentJO.getString("description"));
            }

            if (currentJO.has("icon")) {
               String iconName = currentJO.getString("icon");
               int icon = IconConstants.getRecipeIconFromName(iconName);
               if (icon < 0) {
                  throw new JSONException(
                     "No Icon found with name of '" + iconName + "' in recipe in '" + recipe.getName() + "' (" + recipe.getRecipeId() + ")."
                  );
               }

               ingredient.setIcon((short)icon);
            }
         } else {
            if (currentJO.has("creature")) {
               String corpseName = currentJO.getString("creature");

               try {
                  CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(corpseName);
                  ingredient.setCorpseData(ct.getTemplateId(), corpseName);
               } catch (Exception var12) {
                  throw new JSONException(ingredientType + " Creature '" + corpseName + "' does not exist as a creature.");
               }
            }

            if (ingredient.isLiquid()) {
               if (!currentJO.has("ratio")) {
                  throw new JSONException("Ratio is missing for liquid ingredient '" + idName + "'.");
               }

               int rat = currentJO.getInt("ratio");
               if (rat < 0 || rat > 10000) {
                  throw new JSONException("Ratio percentage for ingredient '" + idName + "' is out of range (0..10000), was " + rat + " in recipe.");
               }

               ingredient.setRatio(rat);
            } else if (currentJO.has("amount")) {
               int amo = currentJO.getInt("amount");
               if (amo < 1 || amo > 3) {
                  throw new JSONException("Amount for ingredient '" + idName + "' is out of range (1..3), was " + amo + " in recipe.");
               }

               ingredient.setAmount(amo);
            }

            if (currentJO.has("loss")) {
               int los = currentJO.getInt("loss");
               if (los < 0 || los > 100) {
                  throw new JSONException("Loss for ingredient '" + idName + "' is out of range (0..100), was " + los + " in recipe.");
               }

               ingredient.setLoss(los);
            } else if (ingredient.isLiquid()) {
               throw new JSONException("Loss is missing for liquid ingredient '" + idName + "'.");
            }

            ingredient.setIngredientId(recipe.getIngredientCount());
         }

         recipe.addIngredient(ingredient);
      }
   }

   private static void checkRecipeSchema(short recipeId, String level, JSONObject recipeJO) throws JSONException {
      Iterator keys = recipeJO.keys();

      while(keys.hasNext()) {
         String nextKey = (String)keys.next();
         switch(level) {
            case "recipe":
               switch(nextKey) {
                  case "name":
                  case "recipeid":
                  case "skill":
                  case "trigger":
                  case "cookers":
                  case "containers":
                  case "active":
                  case "target":
                  case "ingredients":
                  case "result":
                  case "known":
                  case "nameable":
                  case "lootable":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "lootable":
               switch(nextKey) {
                  case "creature":
                  case "rarity":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "cookers":
            case "containers":
               switch(nextKey) {
                  case "id":
                  case "difficulty":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "active":
               switch(nextKey) {
                  case "id":
                  case "cstate":
                  case "pstate":
                  case "material":
                  case "realtemplate":
                  case "difficulty":
                  case "loss":
                  case "ratio":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "target":
               switch(nextKey) {
                  case "id":
                  case "cstate":
                  case "pstate":
                  case "material":
                  case "realtemplate":
                  case "difficulty":
                  case "loss":
                  case "ratio":
                  case "creature":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "ingredients group":
               switch(nextKey) {
                  case "mandatory":
                  case "optional":
                  case "oneof":
                  case "zeroorone":
                  case "oneormore":
                  case "any":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "oneof group":
            case "zeroorone group":
            case "oneormore group":
               byte var10 = -1;
               switch(nextKey.hashCode()) {
                  case 3322014:
                     if (nextKey.equals("list")) {
                        var10 = 0;
                     }
                  default:
                     switch(var10) {
                        case 0:
                           continue;
                        default:
                           throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
                     }
               }
            case "mandatory":
            case "optional":
            case "oneof":
            case "zeroorone":
            case "oneormore":
            case "any":
               switch(nextKey) {
                  case "id":
                  case "cstate":
                  case "pstate":
                  case "material":
                  case "realtemplate":
                  case "difficulty":
                  case "loss":
                  case "ratio":
                  case "amount":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            case "result":
               switch(nextKey) {
                  case "id":
                  case "name":
                  case "cstate":
                  case "pstate":
                  case "material":
                  case "realtemplate":
                  case "refmaterial":
                  case "refrealtemplate":
                  case "difficulty":
                  case "description":
                  case "achievement":
                  case "usetemplateweight":
                  case "icon":
                     continue;
                  default:
                     throw new JSONException("Recipe " + recipeId + " invalid " + level + " attribute " + nextKey + ".");
               }
            default:
               throw new JSONException("Recipe " + recipeId + " invalid " + level + " when checing attributes .");
         }
      }
   }

   private static byte convertCookingStateIntoByte(short recipeId, String state) {
      switch(state) {
         case "raw":
            return 0;
         case "fried":
            return 1;
         case "grilled":
            return 2;
         case "boiled":
            return 3;
         case "roasted":
            return 4;
         case "steamed":
            return 5;
         case "baked":
            return 6;
         case "cooked":
            return 7;
         case "candied":
            return 8;
         case "chocolate coated":
            return 9;
         default:
            logger.warning("Recipe " + recipeId + " has unknown state name:" + state);
            return 0;
      }
   }

   private static byte convertPhysicalStateIntoByte(short recipeId, String state) {
      if (!state.contains("+")) {
         switch(state) {
            case "none":
               return 0;
            case "chopped":
               return 16;
            case "diced":
               return 16;
            case "ground":
               return 16;
            case "unfermented":
               return 16;
            case "zombiefied":
               return 16;
            case "whipped":
               return 16;
            case "mashed":
               return 32;
            case "minced":
               return 32;
            case "fermenting":
               return 32;
            case "clotted":
               return 32;
            case "wrapped":
               return 64;
            case "undistilled":
               return 64;
            case "salted":
               return -128;
            case "fresh":
               return -128;
            default:
               logger.warning("Recipe " + recipeId + " has unknown state name:" + state);
               return 0;
         }
      } else {
         String[] states = state.split("\\+");
         byte theByte = 0;

         for(String s : states) {
            byte code = convertPhysicalStateIntoByte(recipeId, s);
            theByte |= code;
         }

         return theByte;
      }
   }

   private static byte convertRarityStringIntoByte(short recipeId, String rarityName) {
      switch(rarityName) {
         case "common":
            return 0;
         case "rare":
            return 1;
         case "supreme":
            return 2;
         case "fantastic":
            return 3;
         default:
            logger.warning("Recipe " + recipeId + " has unknown rarity name:" + rarityName);
            return 0;
      }
   }

   @Nullable
   public static Recipe getRecipeFor(long playerId, byte wantedType, @Nullable Item activeItem, Item targetItem, boolean checkActive, boolean checkLiquids) {
      return getRecipeFor(playerId, activeItem, targetItem, wantedType, checkActive, checkLiquids);
   }

   @Nullable
   private static Recipe getRecipeFor(long playerId, @Nullable Item activeItem, Item targetItem, byte wantedType, boolean checkActive, boolean checkLiquids) {
      for(Recipe recipe : recipesList) {
         if (recipe.getTrigger() == wantedType && isRecipeOk(playerId, recipe, activeItem, targetItem, checkActive, checkLiquids) != 0) {
            return recipe;
         }
      }

      return null;
   }

   public static int isRecipeOk(long playerId, Recipe recipe, @Nullable Item activeItem, Item target, boolean checkActive, boolean checkLiquids) {
      if (recipe.isRecipeOk(playerId, activeItem, target, checkActive, checkLiquids)) {
         return 1;
      } else {
         return recipe.getTrigger() == 2
               && activeItem != null
               && !activeItem.getTemplate().isCookingTool()
               && !target.getTemplate().isFoodMaker()
               && recipe.isRecipeOk(playerId, target, activeItem, checkActive, checkLiquids)
            ? 2
            : 0;
      }
   }

   public static Recipe[] getPartialRecipeListFor(Creature performer, byte wantedType, Item target) {
      Set<Recipe> recipes = new HashSet<>();

      for(Recipe recipe : recipesList) {
         if (recipe.getTrigger() == wantedType && recipe.isPartialMatch(target)) {
            recipes.add(recipe);
         }
      }

      return recipes.toArray(new Recipe[recipes.size()]);
   }

   @Nullable
   public static Recipe getRecipeByActionId(short id) {
      return recipes.get(id);
   }

   @Nullable
   public static Recipe getRecipeById(short id) {
      return recipes.get((short)(id + 8000));
   }

   @Nullable
   public static Recipe getRecipeByResult(Ingredient ingredient) {
      for(Recipe recipe : recipesList) {
         if (recipe.matchesResult(ingredient, true)) {
            return recipe;
         }
      }

      for(Recipe recipe : recipesList) {
         if (recipe.matchesResult(ingredient, false)) {
            return recipe;
         }
      }

      return null;
   }

   @Nullable
   public static Recipe[] getRecipesByResult(Ingredient ingredient) {
      LinkedList<Recipe> recipes = new LinkedList<>();

      for(Recipe recipe : recipesList) {
         if (recipe.matchesResult(ingredient, true)) {
            recipes.add(recipe);
         }
      }

      if (recipes.size() == 0) {
         for(Recipe recipe : recipesList) {
            if (recipe.matchesResult(ingredient, false)) {
               recipes.add(recipe);
            }
         }
      }

      Recipe[] recipesArr = recipes.toArray(new Recipe[recipes.size()]);
      if (recipesArr.length > 1) {
         Arrays.sort(recipesArr, new Comparator<Recipe>() {
            public int compare(Recipe param1, Recipe param2) {
               return param1.getName().compareTo(param2.getName());
            }
         });
      }

      return recipesArr;
   }

   public static String getIngredientName(@Nullable Ingredient ingredient) {
      return getIngredientName(ingredient, true);
   }

   public static String getIngredientName(@Nullable Ingredient ingredient, boolean withAmount) {
      if (ingredient == null) {
         return "";
      } else {
         StringBuilder buf = new StringBuilder();
         if (ingredient.hasCState()) {
            buf.append(ingredient.getCStateName());
            if (ingredient.hasPState()) {
               buf.append(" " + ingredient.getPStateName());
            }

            buf.append(" ");
         } else if (ingredient.hasPState()) {
            buf.append(ingredient.getPStateName() + " ");
         }

         if (ingredient.hasCorpseData()) {
            buf.append(ingredient.getCorpseName() + " corpse");
            return buf.toString();
         } else {
            if (ingredient.hasMaterial() || ingredient.hasRealTemplate() || ingredient.hasMaterialRef() || ingredient.hasRealTemplateRef()) {
               Recipe[] recipes = getRecipesByResult(ingredient);
               if (recipes.length > 0) {
                  StringBuilder buf2 = new StringBuilder();
                  if (recipes.length == 1) {
                     buf2.append(recipes[0].getResultName(ingredient));
                  } else {
                     buf2.append(ingredient.getName(withAmount));
                  }

                  return buf2.toString();
               }
            }

            return ingredient.getName(withAmount);
         }
      }
   }

   public static boolean isRecipeAction(short action) {
      return action >= 8000 && action < 10000;
   }

   public static Set<Recipe> getKnownRecipes() {
      Set<Recipe> knownRecipes = new HashSet<>();

      for(Recipe recipe : recipesList) {
         if (recipe.isKnown()) {
            knownRecipes.add(recipe);
         }
      }

      return knownRecipes;
   }

   public static Recipe[] getUnknownRecipes() {
      LinkedList<Recipe> unknownRecipes = new LinkedList<>();

      for(Recipe recipe : recipesList) {
         if (!recipe.isKnown()) {
            unknownRecipes.add(recipe);
         }
      }

      return unknownRecipes.toArray(new Recipe[unknownRecipes.size()]);
   }

   public static Recipe[] getAllRecipes() {
      return recipesList.toArray(new Recipe[recipesList.size()]);
   }

   public static boolean isKnownRecipe(short recipeId) {
      Recipe recipe = getRecipeById(recipeId);
      return recipe != null ? recipe.isKnown() : false;
   }
}
