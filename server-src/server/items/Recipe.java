package com.wurmonline.server.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.util.StringUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Recipe implements MiscConstants {
   private static final Logger logger = Logger.getLogger(Recipe.class.getName());
   public static final byte TIME = 0;
   public static final byte HEAT = 1;
   public static final byte CREATE = 2;
   public static final short DEBUG_RECIPE = 0;
   private final String name;
   private final short recipeId;
   private boolean known = false;
   private boolean nameable = false;
   private String skillName = "";
   private int skillId = -1;
   private final Map<Short, String> cookers = new HashMap<>();
   private final Map<Short, Byte> cookersDif = new HashMap<>();
   private final Map<Short, String> containers = new HashMap<>();
   private final Map<Short, Byte> containersDif = new HashMap<>();
   private byte trigger = 2;
   private Ingredient activeItem = null;
   private Ingredient targetItem = null;
   private Ingredient resultItem = null;
   private final List<IngredientGroup> ingredientGroups = new ArrayList<>();
   private int achievementId = -1;
   private String achievementName = "";
   private final Map<Byte, Ingredient> allIngredients = new HashMap<>();
   private boolean lootable = false;
   private int lootableCreature = -10;
   private byte lootableRarity = 0;

   public Recipe(String name, short recipeId) {
      this.name = name;
      this.recipeId = recipeId;
   }

   public Recipe(short recipeId) {
      this.recipeId = recipeId;
      Recipe templateRecipe = Recipes.getRecipeById(this.recipeId);
      if (templateRecipe != null) {
         this.name = templateRecipe.name;
         this.setDefaults(templateRecipe);
      } else {
         this.name = "Null Recipe " + this.recipeId;
         logger.warning("Null recipe with ID: " + this.recipeId);
      }
   }

   public Recipe(DataInputStream dis) throws IOException, NoSuchTemplateException {
      this.recipeId = dis.readShort();
      Recipe templateRecipe = Recipes.getRecipeById(this.recipeId);
      if (templateRecipe != null) {
         this.name = templateRecipe.name;
         this.setDefaults(templateRecipe);
      } else {
         this.name = "Null Recipe " + this.recipeId;
      }

      byte cookerCount = dis.readByte();
      if (cookerCount > 0) {
         for(int ic = 0; ic < cookerCount; ++ic) {
            short cookerid = dis.readShort();
            this.addToCookerList(cookerid);
         }
      }

      byte containerCount = dis.readByte();
      if (containerCount > 0) {
         for(int ic = 0; ic < containerCount; ++ic) {
            short containerid = dis.readShort();
            this.addToContainerList(containerid);
         }
      }

      boolean hasActiveItem = dis.readBoolean();
      if (hasActiveItem) {
         this.setActiveItem(new Ingredient(dis));
      }

      boolean hasTargetItem = dis.readBoolean();
      if (hasTargetItem) {
         this.setTargetItem(new Ingredient(dis));
      }

      byte groupCount = dis.readByte();
      if (groupCount > 0) {
         for(int ic = 0; ic < groupCount; ++ic) {
            IngredientGroup ig = new IngredientGroup(dis);
            if (ig.size() > 0) {
               this.addToIngredientGroupList(ig);
            } else {
               logger.warning("recipe contains empty IngredientGroup: [" + this.recipeId + "] " + this.name);
            }

            for(Ingredient i : ig.getIngredients()) {
               this.allIngredients.put(i.getIngredientId(), i);
            }
         }
      }
   }

   public void pack(DataOutputStream dos) throws IOException {
      dos.writeShort(this.recipeId);
      dos.writeByte(this.cookers.size());

      for(Short cooker : this.cookers.keySet()) {
         dos.writeShort(cooker);
      }

      dos.writeByte(this.containers.size());

      for(Short container : this.containers.keySet()) {
         dos.writeShort(container);
      }

      dos.writeBoolean(this.hasActiveItem());
      if (this.hasActiveItem()) {
         this.activeItem.pack(dos);
      }

      dos.writeBoolean(this.hasTargetItem());
      if (this.hasTargetItem()) {
         this.targetItem.pack(dos);
      }

      ArrayList<IngredientGroup> toSend = new ArrayList<>();

      for(IngredientGroup ig : this.ingredientGroups) {
         if (ig.size() > 0) {
            toSend.add(ig);
         }
      }

      dos.writeByte(toSend.size());

      for(IngredientGroup ig : toSend) {
         ig.pack(dos);
      }
   }

   public String getRecipeName() {
      return this.name;
   }

   public String getName() {
      if (this.nameable) {
         String namer = Recipes.getRecipeNamer(this.recipeId);
         return namer != null && namer.length() > 0 ? namer + "'s " + this.name : this.name + "+";
      } else {
         return this.name;
      }
   }

   public short getRecipeId() {
      return this.recipeId;
   }

   public byte getRecipeColourCode(long playerId) {
      int colour = 0;
      if (this.lootable) {
         colour = this.lootableRarity;
      }

      if (this.isKnown()) {
         colour |= 4;
      }

      if (RecipesByPlayer.isFavourite(playerId, this.recipeId)) {
         colour |= 8;
      }

      if (!RecipesByPlayer.isKnownRecipe(playerId, this.recipeId)) {
         colour |= 16;
      }

      return (byte)colour;
   }

   public short getMenuId() {
      return (short)(this.recipeId + 8000);
   }

   byte getCurrentGroupId() {
      return (byte)(this.ingredientGroups.size() - 1);
   }

   public void setLootable(int creatureId, byte rarity) {
      if (creatureId != -10) {
         this.lootable = true;
         this.lootableCreature = creatureId;
         this.lootableRarity = rarity;
      } else {
         this.lootable = false;
      }
   }

   public boolean isLootable() {
      return this.lootable;
   }

   public int getLootableCreature() {
      return this.lootableCreature;
   }

   public byte getLootableRarity() {
      return this.lootableRarity;
   }

   public byte getIngredientCount() {
      return (byte)this.allIngredients.size();
   }

   public void addIngredient(Ingredient ingredient) {
      byte gId = ingredient.getGroupId();
      if (gId == -3) {
         this.setResultItem(ingredient);
      } else {
         Ingredient old = this.allIngredients.put(ingredient.getIngredientId(), ingredient);
         if (old != null) {
            logger.info(
               "Recipe ("
                  + this.recipeId
                  + ") Overridden Ingredient ("
                  + old.getIngredientId()
                  + ") group ("
                  + gId
                  + ") old:"
                  + old.getName(true)
                  + " new:"
                  + ingredient.getName(true)
                  + "."
            );
         }

         if (gId == -2) {
            this.setActiveItem(ingredient);
         } else if (gId == -1) {
            this.setTargetItem(ingredient);
         } else {
            IngredientGroup ig = this.getGroupById(gId);
            if (ig != null) {
               ig.add(ingredient);
            } else {
               logger.log(Level.WARNING, "IngredientGroup is null for groupID: " + gId, (Throwable)(new Exception()));
            }
         }
      }
   }

   public Ingredient getIngredientById(byte ingredientId) {
      return this.allIngredients.get(ingredientId);
   }

   public String getSubMenuName(Item container) {
      StringBuilder buf = new StringBuilder();
      if (this.resultItem.hasCState()) {
         buf.append(this.resultItem.getCStateName());
         if (this.resultItem.hasPState()) {
            buf.append(" " + this.resultItem.getPStateName());
         }

         buf.append(" ");
      } else if (this.resultItem.hasPState() && this.resultItem.getPState() != 0) {
         buf.append(this.resultItem.getPStateName() + " ");
      }

      buf.append(this.getResultName(container));
      return buf.toString();
   }

   void setKnown(boolean known) {
      this.known = known;
   }

   public boolean isKnown() {
      return this.known;
   }

   void setNameable(boolean nameable) {
      this.nameable = nameable;
   }

   public boolean isNameable() {
      return this.nameable;
   }

   public void setSkill(int skillId, String skillName) {
      this.skillName = skillName;
      this.skillId = skillId;
   }

   public int getSkillId() {
      return this.skillId;
   }

   public String getSkillName() {
      return this.skillName;
   }

   public void setTrigger(byte trigger) {
      this.trigger = trigger;
   }

   public byte getTrigger() {
      return this.trigger;
   }

   public int getDifficulty(Item target) {
      int diff = this.resultItem.getDifficulty();
      if (diff == -100) {
         diff = (int)this.getResultTemplate(target).getDifficulty();
      }

      if (target.isFoodMaker()) {
         for(IngredientGroup ig : this.ingredientGroups) {
            diff += ig.getGroupDifficulty();
         }
      } else if (this.hasTargetItem()) {
         diff += this.targetItem.getDifficulty();
      }

      Item cooker = target.getTopParentOrNull();
      if (cooker != null) {
         Byte cookerDif = this.cookersDif.get((short)cooker.getTemplateId());
         if (cookerDif != null) {
            diff += cookerDif;
         }
      }

      Byte containerDif = this.containersDif.get((short)target.getTemplateId());
      if (containerDif != null) {
         diff += containerDif;
      }

      return diff;
   }

   public void addToCookerList(int cookerTemplateId, String cookerName, int cookerDif) {
      this.cookers.put((short)cookerTemplateId, cookerName);
      this.cookersDif.put((short)cookerTemplateId, (byte)cookerDif);
   }

   public void addToCookerList(int cookerTemplateId) {
      String name = "";

      try {
         ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(cookerTemplateId);
         name = cookerIT.getName();
      } catch (NoSuchTemplateException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      }

      this.addToCookerList(cookerTemplateId, name, 0);
   }

   private boolean isCooker(int cookerTemplateId) {
      return this.cookers.containsKey((short)cookerTemplateId);
   }

   public Set<ItemTemplate> getCookerTemplates() {
      Set<ItemTemplate> cookerTemplates = new HashSet<>();

      for(Short sc : this.cookers.keySet()) {
         try {
            ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(sc);
            cookerTemplates.add(cookerIT);
         } catch (NoSuchTemplateException var5) {
         }
      }

      return cookerTemplates;
   }

   public void addToContainerList(int containerTemplateId, String containerName, int containerDif) {
      this.containers.put((short)containerTemplateId, containerName);
      this.containersDif.put((short)containerTemplateId, (byte)containerDif);
   }

   public void addToContainerList(int containerTemplateId) {
      String name = "";

      try {
         ItemTemplate containerIT = ItemTemplateFactory.getInstance().getTemplate(containerTemplateId);
         name = containerIT.getName();
      } catch (NoSuchTemplateException var4) {
         logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      }

      this.addToContainerList(containerTemplateId, name, 0);
   }

   public boolean isContainer(int containerTemplateId) {
      return this.containers.containsKey((short)containerTemplateId);
   }

   public Set<ItemTemplate> getContainerTemplates() {
      Set<ItemTemplate> containerTemplates = new HashSet<>();

      for(Short sc : this.containers.keySet()) {
         try {
            ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(sc);
            containerTemplates.add(cookerIT);
         } catch (NoSuchTemplateException var5) {
         }
      }

      return containerTemplates;
   }

   public Map<String, Ingredient> getAllIngredients(boolean incActiveAndTargetItems) {
      Map<String, Ingredient> knownIngredients = new HashMap<>();

      for(Ingredient ingredient : this.allIngredients.values()) {
         if ((ingredient.getGroupId() >= 0 || incActiveAndTargetItems) && !ingredient.getTemplate().isCookingTool()) {
            knownIngredients.put(ingredient.getName(true), ingredient);
         }
      }

      return knownIngredients;
   }

   public void setActiveItem(Ingredient ingredient) {
      this.activeItem = ingredient;
   }

   @Nullable
   public Ingredient getActiveItem() {
      return this.activeItem;
   }

   public boolean hasActiveItem() {
      return this.activeItem != null;
   }

   private boolean isActiveItem(Item source) {
      if (this.activeItem.getTemplateId() == 14) {
         return true;
      } else if (!this.activeItem.checkFoodGroup(source)) {
         return false;
      } else if (!this.activeItem.checkCorpseData(source)) {
         return false;
      } else if (!this.activeItem.checkState(source)) {
         return false;
      } else if (!this.activeItem.checkMaterial(source)) {
         return false;
      } else {
         return this.activeItem.checkRealTemplate(source);
      }
   }

   public String getActiveItemName() {
      return this.hasActiveItem() ? this.activeItem.getName(false) : "";
   }

   public void setTargetItem(Ingredient targetIngredient) {
      this.targetItem = targetIngredient;
      if (targetIngredient.getTemplateId() == 1173) {
         this.trigger = 2;
      }
   }

   @Nullable
   public Ingredient getTargetItem() {
      return this.targetItem;
   }

   public boolean hasTargetItem() {
      return this.targetItem != null;
   }

   private boolean isTargetItem(Item target, boolean checkLiquids) {
      if (target.isFoodMaker()) {
         for(Short ii : this.containers.keySet()) {
            if (ii.intValue() == target.getTemplateId()) {
               return true;
            }
         }

         return false;
      } else if (this.targetItem == null) {
         return false;
      } else if (!this.targetItem.checkFoodGroup(target)) {
         return false;
      } else if (!this.targetItem.checkCorpseData(target)) {
         return false;
      } else if (!this.targetItem.checkState(target)) {
         return false;
      } else if (!this.targetItem.checkMaterial(target)) {
         return false;
      } else if (!this.targetItem.checkRealTemplate(target)) {
         return false;
      } else {
         return !this.useResultTemplateWeight() || !checkLiquids || this.getTargetLossWeight(target) <= target.getWeightGrams();
      }
   }

   public int getTargetLossWeight(Item target) {
      int loss = this.targetItem.getLoss();
      return loss != 100 ? (int)((float)this.resultItem.getTemplate().getWeightGrams() * (1.0F / ((float)(100 - loss) / 100.0F))) : target.getWeightGrams();
   }

   public String getTargetItemName() {
      return this.hasTargetItem() ? this.targetItem.getName(false) : "";
   }

   public void setResultItem(Ingredient resultIngredient) {
      this.resultItem = resultIngredient;
   }

   public Ingredient getResultItem() {
      return this.resultItem;
   }

   public ItemTemplate getResultTemplate(Item container) {
      if (this.resultItem.isFoodGroup()) {
         Item item = this.findIngredient(container, this.resultItem);
         if (item != null) {
            return item.getTemplate();
         }
      }

      return this.resultItem.getTemplate();
   }

   public boolean useResultTemplateWeight() {
      return this.resultItem.useResultTemplateWeight();
   }

   public String getResultName(Item container) {
      String resultName = this.resultItem.getResultName();
      if (resultName.length() > 0) {
         return this.doSubstituation(container, resultName);
      } else {
         StringBuilder buf = new StringBuilder();
         if (this.resultItem.isFoodGroup()) {
            Item item = this.findIngredient(container, this.resultItem);
            if (item != null) {
               buf.append(item.getActualName());
            }
         } else {
            buf.append(this.resultItem.getTemplateName());
         }

         return buf.toString();
      }
   }

   String doSubstituation(Item container, String name) {
      String newName = name;
      if (name.indexOf(35) >= 0) {
         if (this.resultItem.hasRealTemplateId() && this.resultItem.getRealItemTemplate() != null) {
            newName = name.replace("#", this.resultItem.getRealItemTemplate().getName());
         } else if (this.resultItem.hasRealTemplateRef()) {
            ItemTemplate realTemplate = this.getResultRealTemplate(container);
            if (realTemplate != null) {
               newName = name.replace("#", realTemplate.getName());
            } else {
               newName = name.replace("# ", "").replace(" #", "");
            }
         }
      }

      if (newName.indexOf(36) >= 0) {
         if (this.resultItem.hasMaterial()) {
            newName = newName.replace("$", this.resultItem.getMaterialName());
         } else if (this.resultItem.hasMaterialRef()) {
            byte material = this.getResultMaterial(container);
            newName = newName.replace("$", Materials.convertMaterialByteIntoString(material));
         }
      }

      return newName.trim();
   }

   String getResultName(Ingredient ingredient) {
      StringBuilder buf = new StringBuilder();
      String resultName = this.resultItem.getResultName();
      if (resultName.length() > 0) {
         if (this.resultItem.hasCState()) {
            buf.append(this.resultItem.getCStateName());
            if (this.resultItem.hasPState() && this.resultItem.getPStateName().length() > 0) {
               buf.append(" " + this.resultItem.getPStateName());
            }

            buf.append(" ");
         } else if (this.resultItem.hasPState() && this.resultItem.getPStateName().length() > 0) {
            buf.append(this.resultItem.getPStateName() + " ");
         }

         if (resultName.indexOf(35) >= 0) {
            if (ingredient.getRealItemTemplate() != null) {
               resultName = resultName.replace("#", ingredient.getRealItemTemplate().getName().replace("any ", ""));
            } else if (this.resultItem.hasRealTemplateRef()) {
               resultName = resultName.replace("# ", "").replace(" #", "");
            }
         }

         if (resultName.indexOf(36) >= 0) {
            if (ingredient.hasMaterial()) {
               resultName = resultName.replace("$", ingredient.getMaterialName());
            } else if (this.resultItem.hasMaterialRef()) {
               resultName = resultName.replace("$ ", "").replace(" $", "");
            }
         }

         buf.append(resultName.trim());
         return buf.toString();
      } else {
         buf.append(this.resultItem.getName(false));
         if (!this.resultItem.hasMaterial() && ingredient.hasMaterial()) {
            buf.append(" (" + ingredient.getMaterialName() + ")");
         }

         return buf.toString();
      }
   }

   public String getResultNameWithGenus(Item container) {
      return StringUtilities.addGenus(this.getSubMenuName(container), container.isNamePlural());
   }

   public boolean hasResultState() {
      return this.resultItem.hasXState();
   }

   public byte getResultState() {
      return this.resultItem.getXState();
   }

   public byte getResultMaterial(Item target) {
      if (this.resultItem.hasMaterialRef()) {
         if (this.targetItem != null && this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getMaterialRef())) {
            return target.getMaterial();
         }

         IngredientGroup group = this.getGroupByType(1);
         if (group != null) {
            Ingredient ingredient = group.getIngredientByName(this.resultItem.getMaterialRef());
            if (ingredient != null && ingredient.getMaterial() != 0) {
               Item item = this.findIngredient(target, ingredient);
               if (item != null) {
                  return item.getMaterial();
               }
            }
         }
      }

      return this.resultItem.hasMaterial() ? this.resultItem.getMaterial() : this.resultItem.getTemplate().getMaterial();
   }

   public boolean hasDescription() {
      return this.resultItem.hasResultDescription();
   }

   public String getResultDescription(Item container) {
      return this.doSubstituation(container, this.resultItem.getResultDescription());
   }

   public void addAchievements(Creature performer, Item newItem) {
      if (this.achievementId != -1) {
         AchievementTemplate at = Achievement.getTemplate(this.achievementId);
         if (at != null) {
            if (at.isInLiters()) {
               performer.achievement(this.achievementId, newItem.getWeightGrams() / 1000);
            } else {
               performer.achievement(this.achievementId);
            }
         }
      }
   }

   public void addAchievementsOffline(long wurmId, Item newItem) {
      if (this.achievementId != -1) {
         AchievementTemplate at = Achievement.getTemplate(this.achievementId);
         if (at != null) {
            if (at.isInLiters()) {
               Achievements.triggerAchievement(wurmId, this.achievementId, newItem.getWeightGrams() / 1000);
            } else {
               Achievements.triggerAchievement(wurmId, this.achievementId);
            }
         }
      }
   }

   @Nullable
   public ItemTemplate getResultRealTemplate(Item target) {
      if (this.resultItem.getRealTemplateRef().length() > 0) {
         if (this.hasOneContainer()) {
            for(Entry<Short, String> container : this.containers.entrySet()) {
               if (container.getValue().equalsIgnoreCase(this.resultItem.getRealTemplateRef())) {
                  return target.getRealTemplate();
               }
            }
         }

         if (this.targetItem != null && this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getRealTemplateRef())) {
            ItemTemplate rit = target.getRealTemplate();
            if (rit != null) {
               return rit;
            }

            return target.getTemplate();
         }

         IngredientGroup group = this.getGroupByType(1);
         if (group != null) {
            Ingredient ingredient = group.getIngredientByName(this.resultItem.getRealTemplateRef());
            if (ingredient != null) {
               Item item = this.findIngredient(target, ingredient);
               if (item != null) {
                  ItemTemplate rit = item.getRealTemplate();
                  if (rit != null) {
                     return rit;
                  }

                  return item.getTemplate();
               }
            }
         }
      } else if (this.resultItem.hasRealTemplate()) {
         return this.resultItem.getRealItemTemplate();
      }

      return null;
   }

   @Nullable
   private Item findIngredient(Item container, Ingredient ingredient) {
      int foodGroup = ingredient.isFoodGroup() ? ingredient.getTemplateId() : 0;
      if (!container.isFoodMaker() && !container.getTemplate().isCooker() && container.getTemplateId() != 1284) {
         if (container.getTemplate().getFoodGroup() == foodGroup) {
            if (ingredient.hasRealTemplate() && container.getRealTemplateId() != ingredient.getRealTemplateId()) {
               return null;
            }

            if (ingredient.hasMaterial() && container.getMaterial() != ingredient.getMaterial()) {
               return null;
            }

            return container;
         }
      } else {
         for(Item item : container.getItemsAsArray()) {
            if (foodGroup > 0) {
               if (item.getTemplate().getFoodGroup() == foodGroup
                  && (!ingredient.hasRealTemplate() || item.getRealTemplateId() == ingredient.getRealTemplateId())
                  && (!ingredient.hasMaterial() || item.getMaterial() == ingredient.getMaterial())) {
                  return item;
               }
            } else if (item.getTemplateId() == ingredient.getTemplateId()
               && (
                  !ingredient.hasRealTemplate()
                     || item.getRealTemplateId() == ingredient.getRealTemplateId()
                     || item.getRealTemplate() != null && item.getRealTemplate().getFoodGroup() == ingredient.getRealTemplateId()
               )
               && (!ingredient.hasMaterial() || item.getMaterial() == ingredient.getMaterial())) {
               return item;
            }
         }
      }

      return null;
   }

   @Nullable
   public Ingredient findMatchingIngredient(Item item) {
      for(Ingredient ingredient : this.allIngredients.values()) {
         if (ingredient.matches(item)) {
            return ingredient;
         }
      }

      return null;
   }

   boolean isPartialMatch(Item container) {
      if (this.getRecipeId() == 0) {
         System.out.println("isPartialMatch:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      if (this.hasTargetItem()) {
         if (!this.isTargetItem(container, false)) {
            return false;
         }
      } else if (this.hasContainer() && !this.isContainer(container.getTemplateId())) {
         return false;
      }

      Item[] items = container.getItemsAsArray();
      boolean[] founds = new boolean[items.length];

      for(int x = 0; x < founds.length; ++x) {
         founds[x] = false;
      }

      if (this.getRecipeId() == 0) {
         System.out.println("isPartialMatch2:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      for(IngredientGroup ig : this.ingredientGroups) {
         ig.clearFound();

         for(int x = 0; x < items.length; ++x) {
            if (!founds[x] && ig.matches(items[x])) {
               founds[x] = true;
            }
         }
      }

      if (this.getRecipeId() == 0) {
         System.out.println("isPartialMatch3:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      for(int x = 0; x < items.length; ++x) {
         if (!founds[x]) {
            return false;
         }

         Ingredient ingredient = this.findMatchingIngredient(items[x]);
         if (ingredient != null && !ingredient.wasFound(true, false)) {
            return false;
         }
      }

      for(IngredientGroup ig : this.ingredientGroups) {
         if (ig.getGroupType() == 3 && ig.getFound(false) > 1) {
            return false;
         }

         if (ig.getGroupType() == 2 && ig.getFound(false) > 1) {
            return false;
         }

         if (ig.getGroupType() == 5 && !ig.wasFound()) {
            return false;
         }
      }

      return true;
   }

   public Ingredient[] getWhatsMissing() {
      Set<Ingredient> ingredients = new HashSet<>();

      for(IngredientGroup ig : this.ingredientGroups) {
         if ((ig.getGroupType() == 1 || ig.getGroupType() == 3 || ig.getGroupType() == 4) && !ig.wasFound()) {
            for(Ingredient ingredient : ig.getIngredients()) {
               if (!ingredient.wasFound(ig.getGroupType() == 4, false)) {
                  ingredients.add(ingredient);
               }
            }
         }
      }

      return ingredients.toArray(new Ingredient[ingredients.size()]);
   }

   public void addToIngredientGroupList(IngredientGroup ingredientGroup) {
      this.ingredientGroups.add(ingredientGroup);
   }

   public void setDefaults(Recipe templateRecipe) {
      for(IngredientGroup ig : templateRecipe.getGroups()) {
         if (ig.size() > 0) {
            this.addToIngredientGroupList(ig.clone());
         } else {
            logger.warning("recipe contains empty IngredientGroup: [" + templateRecipe.recipeId + "] " + templateRecipe.name);
         }
      }

      this.resultItem = templateRecipe.resultItem.clone(null);
      this.lootable = templateRecipe.lootable;
      this.nameable = templateRecipe.nameable;
      this.lootableCreature = templateRecipe.lootableCreature;
      this.lootableRarity = templateRecipe.lootableRarity;
      this.trigger = templateRecipe.trigger;
      this.skillId = templateRecipe.skillId;
      this.skillName = templateRecipe.skillName;
      this.achievementId = templateRecipe.achievementId;
      this.achievementName = templateRecipe.achievementName;
   }

   public void copyGroupsFrom(Recipe recipe) {
      for(IngredientGroup ig : recipe.getGroups()) {
         this.addToIngredientGroupList(ig.clone());
      }
   }

   @Nullable
   public IngredientGroup getGroupById(byte groupId) {
      try {
         return this.ingredientGroups.get(groupId);
      } catch (IndexOutOfBoundsException var3) {
         return null;
      }
   }

   @Nullable
   public IngredientGroup getGroupByType(int groupType) {
      for(IngredientGroup ig : this.ingredientGroups) {
         if (ig.getGroupType() == groupType) {
            return ig;
         }
      }

      return null;
   }

   public IngredientGroup[] getGroups() {
      return this.ingredientGroups.toArray(new IngredientGroup[this.ingredientGroups.size()]);
   }

   public boolean hasCooker() {
      return !this.cookers.isEmpty();
   }

   public boolean hasCooker(int cookerId) {
      return this.cookers.containsKey((short)cookerId);
   }

   public boolean hasOneCooker() {
      return this.cookers.size() == 1;
   }

   public short getCookerId() {
      Iterator var1 = this.cookers.keySet().iterator();
      if (var1.hasNext()) {
         Short ss = (Short)var1.next();
         return ss;
      } else {
         return -10;
      }
   }

   public boolean hasContainer() {
      return !this.containers.isEmpty();
   }

   public boolean hasOneContainer() {
      return this.containers.size() == 1;
   }

   public boolean hasContainer(int containerId) {
      return this.containers.containsKey((short)containerId);
   }

   public boolean hasContainer(String containerName) {
      for(Entry<Short, String> container : this.containers.entrySet()) {
         if (container.getValue().equalsIgnoreCase(containerName)) {
            return true;
         }
      }

      return false;
   }

   public short getContainerId() {
      Iterator var1 = this.containers.keySet().iterator();
      if (var1.hasNext()) {
         Short ss = (Short)var1.next();
         return ss;
      } else {
         return -10;
      }
   }

   boolean checkIngredients(Item container) {
      Item[] items = container.getItemsAsArray();
      boolean[] founds = new boolean[items.length];

      for(int x = 0; x < founds.length; ++x) {
         founds[x] = false;
      }

      if (this.getRecipeId() == 0) {
         System.out.println("checkIngredients:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      for(IngredientGroup ig : this.ingredientGroups) {
         ig.clearFound();

         for(int x = 0; x < items.length; ++x) {
            if (ig.matches(items[x])) {
               founds[x] = true;
            }
         }
      }

      if (this.getRecipeId() == 0) {
         System.out.println("checkIngredients2:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      for(int x = 0; x < founds.length; ++x) {
         if (!founds[x]) {
            return false;
         }
      }

      if (this.getRecipeId() == 0) {
         System.out.println("checkIngredients3:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      for(IngredientGroup ig : this.ingredientGroups) {
         if (!ig.wasFound()) {
            return false;
         }
      }

      if (this.getRecipeId() == 0) {
         System.out.println("checkIngredients4:" + this.getRecipeId() + " " + this.getTriggerName());
      }

      return true;
   }

   public float getChanceFor(@Nullable Item activeItem, Item target, Creature performer) {
      Skills skills = performer.getSkills();
      Skill primSkill = null;
      Skill secondarySkill = null;
      double bonus = 0.0;

      try {
         primSkill = skills.getSkill(this.getSkillId());
      } catch (Exception var12) {
      }

      try {
         if (this.hasActiveItem() && activeItem != null && this.isActiveItem(activeItem)) {
            secondarySkill = skills.getSkill(activeItem.getPrimarySkill());
         }
      } catch (Exception var11) {
      }

      if (secondarySkill != null) {
         bonus = Math.max(1.0, secondarySkill.getKnowledge(activeItem, 0.0) / 10.0);
      }

      float chance = 0.0F;
      int diff = this.getDifficulty(target);
      if (primSkill != null) {
         chance = (float)primSkill.getChance((double)diff, activeItem, bonus);
      } else {
         chance = (float)(1 / (1 + diff) * 100);
      }

      return chance;
   }

   void setAchievementTriggered(int achievementId, String achievementName) {
      this.achievementId = achievementId;
      this.achievementName = achievementName;
   }

   public String getTriggerName() {
      switch(this.trigger) {
         case 0:
            return "Time";
         case 1:
            return "Heat";
         case 2:
            if (this.isTargetActionType()) {
               return "Target Action";
            } else {
               if (this.isContainerActionType()) {
                  return "Container Action";
               }

               return "Create";
            }
         default:
            return "Unknown";
      }
   }

   boolean isRecipeOk(long playerId, @Nullable Item activeItem, Item target, boolean checkActive, boolean checkLiquids) {
      if (this.getRecipeId() == 0) {
         System.out.println("isRecipeOk:" + this.getRecipeId() + " " + checkActive + " " + this.getTriggerName() + "(" + target.getName() + ")");
      }

      if (playerId != -10L && this.isLootable() && !RecipesByPlayer.isKnownRecipe(playerId, this.recipeId)) {
         return false;
      } else {
         if (checkActive && activeItem != null && this.getActiveItem() != null) {
            if (!this.isActiveItem(activeItem)) {
               return false;
            }

            if (checkLiquids && activeItem.isLiquid()) {
               int weightNeeded = this.getUsedActiveItemWeightGrams(activeItem, target);
               if (activeItem.getWeightGrams() < weightNeeded) {
                  return false;
               }
            }
         }

         if (this.targetItem != null && !this.isTargetItem(target, checkLiquids)) {
            return false;
         } else {
            if (this.trigger == 1 && checkActive) {
               Item cooker = target.getTopParentOrNull();
               if (cooker == null) {
                  return false;
               }

               if (!this.isCooker((short)cooker.getTemplateId())) {
                  return false;
               }
            }

            if (this.targetItem == null) {
               if (this.hasContainer()) {
                  if (!this.isContainer((short)target.getTemplateId())) {
                     return false;
                  }
               } else if (this.hasCooker() && !this.isCooker((short)target.getTemplateId())) {
                  return false;
               }
            } else if (this.trigger == 1 && checkActive) {
               Item cooker = target.getTopParentOrNull();
               Item parent = target.getParentOrNull();
               if (cooker == null || parent == null) {
                  return false;
               }

               if (cooker.getTemplateId() != parent.getTemplateId()) {
                  return false;
               }

               if (this.hasContainer() && !this.isContainer((short)parent.getTemplateId())) {
                  return false;
               }
            }

            if (target.isFoodMaker() || target.getTemplate().isCooker() || target.isRecipeItem() && target.isHollow()) {
               if (this.getRecipeId() == 0) {
                  System.out.println("isRecipeOk2:" + this.getRecipeId() + " " + checkActive);
               }

               if (!this.checkIngredients(target)) {
                  return false;
               } else {
                  if (this.getRecipeId() == 0) {
                     System.out.println("isRecipeOk3:" + this.getRecipeId() + " " + checkActive);
                  }

                  return !checkLiquids || this.getNewWeightGrams(target).isSuccess();
               }
            } else {
               int needed = this.getActiveItem() != null ? 2 : 1;
               if (this.allIngredients.size() != needed) {
                  return false;
               } else {
                  for(Ingredient ingredient : this.allIngredients.values()) {
                     if (ingredient.matches(target)) {
                        return true;
                     }
                  }

                  return false;
               }
            }
         }
      }
   }

   public int getUsedActiveItemWeightGrams(Item source, Item target) {
      int rat = this.getActiveItem() != null ? this.getActiveItem().getRatio() : 0;
      return source.isLiquid() && rat != 0 ? target.getWeightGrams() * rat / 100 : source.getWeightGrams();
   }

   public Recipe.LiquidResult getNewWeightGrams(Item container) {
      Recipe.LiquidResult liquidResult = new Recipe.LiquidResult();
      Map<Short, Recipe.Liquid> liquids = new HashMap<>();

      for(Ingredient in : this.getAllIngredients(true).values()) {
         if (in.getTemplate().isLiquid()) {
            short id = (short)in.getTemplateId();
            int ratio = in.getRatio();
            String name = Recipes.getIngredientName(in, false);
            int loss = in.getLoss();
            liquids.put(id, new Recipe.Liquid(id, name, ratio, loss));
         }
      }

      int solidWeight = 0;

      for(Item item : container.getItemsAsArray()) {
         if (item.isLiquid()) {
            short id = (short)item.getTemplateId();
            int liquidWeight = item.getWeightGrams();
            Recipe.Liquid liquid = liquids.get(id);
            if (liquid == null) {
               short fgid = (short)item.getTemplate().getFoodGroup();
               liquid = liquids.get(fgid);
            }

            if (liquid != null) {
               if (liquid.getRatio() != 0) {
                  liquid.setWeight(liquidWeight);
               }
            } else {
               logger.info("Liquid Item " + item.getName() + " missing ingredient?");
            }
         } else {
            solidWeight += item.getWeightGrams();
         }
      }

      int newWeight = solidWeight;

      for(Recipe.Liquid liquid : liquids.values()) {
         if (liquid.getWeight() > 0) {
            int neededWeight = solidWeight * liquid.getRatio() / 100;
            int minLiquid = (int)((double)neededWeight * 0.8);
            int maxLiquid = (int)((double)neededWeight * 1.2);
            if (liquid.getWeight() < minLiquid) {
               liquidResult.add(
                  liquid.getId(), "not enough " + liquid.getName() + ", looks like it should use between " + minLiquid + " and " + maxLiquid + " grams."
               );
            } else if (liquid.getWeight() > maxLiquid) {
               liquidResult.add(
                  liquid.getId(), "too much " + liquid.getName() + ", looks like it should use between " + minLiquid + " and " + maxLiquid + " grams."
               );
            }

            newWeight += liquid.getWeight() * (100 - liquid.getLoss());
         }
      }

      liquidResult.setNewWeight(newWeight);
      return liquidResult;
   }

   public boolean isTargetActionType() {
      return this.trigger == 2 && this.containers.isEmpty();
   }

   public boolean isContainerActionType() {
      return this.trigger == 2 && !this.containers.isEmpty();
   }

   public boolean isHeatType() {
      return this.trigger == 1;
   }

   public boolean isTimeType() {
      return this.trigger == 0;
   }

   public String[] getCookers() {
      List<String> cookerList = new ArrayList<>();

      for(String cooker : this.cookers.values()) {
         cookerList.add(cooker);
      }

      return cookerList.toArray(new String[cookerList.size()]);
   }

   public String getCookersAsString() {
      StringBuilder buf = new StringBuilder();
      boolean first = true;

      for(String s : this.cookers.values()) {
         if (first) {
            first = false;
         } else {
            buf.append(",");
         }

         buf.append(s);
      }

      return buf.toString();
   }

   public String[] getContainers() {
      List<String> containerList = new ArrayList<>();

      for(String container : this.containers.values()) {
         containerList.add(container);
      }

      return containerList.toArray(new String[containerList.size()]);
   }

   public String getContainersAsString() {
      StringBuilder buf = new StringBuilder();
      boolean first = true;

      for(String s : this.containers.values()) {
         if (first) {
            first = false;
         } else {
            buf.append(",");
         }

         buf.append(s);
      }

      return buf.toString();
   }

   boolean matchesResult(Ingredient ingredient, boolean exactOnly) {
      if (this.resultItem.getTemplateId() != ingredient.getTemplateId()) {
         if (this.resultItem.getTemplate().isFoodGroup()) {
            if (this.targetItem != null) {
               if (!exactOnly || this.targetItem.getTemplate().getFoodGroup() != ingredient.getTemplate().getFoodGroup()) {
                  return false;
               } else if (ingredient.hasCState() && this.resultItem.hasCState() && this.resultItem.getCState() != ingredient.getCState()) {
                  return false;
               } else {
                  return !ingredient.hasPState() || !this.resultItem.hasPState() || this.resultItem.getPState() == ingredient.getPState();
               }
            } else {
               return false;
            }
         } else if (!exactOnly && ingredient.getTemplate().isFoodGroup()) {
            if (this.resultItem.getTemplate().getFoodGroup() != ingredient.getTemplateId()) {
               return false;
            } else if (this.resultItem.hasCState() && this.resultItem.getCState() != ingredient.getCState()) {
               return false;
            } else {
               return !this.resultItem.hasPState() || this.resultItem.getPState() == ingredient.getPState();
            }
         } else {
            return false;
         }
      } else {
         boolean ok = !this.resultItem.hasCState() && !ingredient.hasCState();
         if (!ok) {
            ok = this.resultItem.hasCState() && ingredient.hasCState() && this.resultItem.getCState() == ingredient.getCState();
         }

         if (!ok) {
            ok = exactOnly && !ingredient.hasCState() && this.resultItem.hasCState();
         }

         if (!ok) {
            return false;
         } else {
            ok = !this.resultItem.hasPState() && !ingredient.hasPState();
            if (!ok) {
               ok = this.resultItem.hasPState() && ingredient.hasPState() && this.resultItem.getPState() == ingredient.getPState();
            }

            if (!ok) {
               ok = exactOnly && !ingredient.hasPState() && this.resultItem.hasPState();
            }

            if (!ok) {
               return false;
            } else {
               if (ingredient.hasRealTemplate()) {
                  if (this.resultItem.hasRealTemplate()) {
                     if (this.resultItem.getRealTemplateId() != ingredient.getRealTemplateId()) {
                        if (exactOnly) {
                           return false;
                        }

                        if (this.resultItem.getRealItemTemplate() == null || ingredient.getRealItemTemplate() == null) {
                           return false;
                        }

                        if (this.resultItem.getRealItemTemplate().isFoodGroup()
                           && this.resultItem.getRealItemTemplate().getFoodGroup() != ingredient.getRealItemTemplate().getFoodGroup()) {
                           return false;
                        }

                        if (ingredient.getRealItemTemplate().isFoodGroup()
                           && this.resultItem.getRealItemTemplate().getFoodGroup() != ingredient.getRealItemTemplate().getFoodGroup()) {
                           return false;
                        }
                     }
                  } else {
                     if (!this.resultItem.hasRealTemplateRef()) {
                        return false;
                     }

                     boolean match = false;
                     if (this.hasTargetItem() && this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getRealTemplateRef())) {
                        Ingredient refingredient = this.targetItem;
                        if (ingredient.getRealItemTemplate() == null) {
                           if (refingredient.getTemplate() != null) {
                              return false;
                           }

                           match = true;
                        } else if (refingredient.getTemplateId() == ingredient.getRealItemTemplate().getTemplateId()) {
                           match = true;
                        } else {
                           if (exactOnly) {
                              return false;
                           }

                           if (refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup()
                              || refingredient.getTemplateId() == 369 && ingredient.getRealItemTemplate().getFoodGroup() == 1201) {
                              match = true;
                           }
                        }
                     }

                     if (!match) {
                        IngredientGroup group = this.getGroupByType(1);
                        if (group == null) {
                           return false;
                        }

                        Ingredient refingredient = group.getIngredientByName(this.resultItem.getRealTemplateRef());
                        if (refingredient == null) {
                           return false;
                        }

                        if (ingredient.getRealItemTemplate() == null) {
                           if (refingredient.getTemplate() != null) {
                              return false;
                           }

                           match = true;
                        } else if (!refingredient.hasRealTemplateId()) {
                           if (exactOnly) {
                              return false;
                           }

                           if (refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup()) {
                              match = true;
                           } else {
                              Recipe[] ning = Recipes.getRecipesByResult(new Ingredient(refingredient.getTemplate(), false, refingredient.getGroupId()));
                              if (ning == null || ning.length == 0) {
                                 return false;
                              }
                           }
                        } else if (refingredient.getTemplateId() == ingredient.getRealItemTemplate().getTemplateId()) {
                           match = true;
                        } else {
                           if (exactOnly) {
                              return false;
                           }

                           if (refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup()
                              || refingredient.getTemplateId() == 369 && ingredient.getRealItemTemplate().getFoodGroup() == 1201) {
                              match = true;
                           }
                        }
                     }
                  }
               }

               if (ingredient.hasMaterial() && this.resultItem.hasMaterial() && ingredient.getMaterial() != this.resultItem.getMaterial()) {
                  return false;
               } else {
                  if (ingredient.hasMaterial() && this.resultItem.hasMaterialRef()) {
                     if (this.targetItem != null) {
                        if (!this.isInMaterialGroup(this.targetItem.getTemplateId(), ingredient.getMaterial())) {
                           return false;
                        }
                     } else {
                        IngredientGroup group = this.getGroupByType(1);
                        if (group == null) {
                           return false;
                        }

                        Ingredient refingredient = group.getIngredientByName(this.resultItem.getMaterialRef());
                        if (refingredient == null) {
                           return false;
                        }

                        if (!this.isInMaterialGroup(refingredient.getTemplateId(), ingredient.getMaterial())) {
                           return false;
                        }
                     }
                  }

                  return true;
               }
            }
         }
      }
   }

   private boolean isInMaterialGroup(int templateGroup, byte material) {
      switch(templateGroup) {
         case 200:
         case 201:
         case 1157:
            switch(material) {
               case 3:
               case 4:
               case 5:
               case 6:
                  return true;
               default:
                  return false;
            }
         case 1261:
            switch(material) {
               case 2:
               case 72:
               case 73:
               case 74:
               case 75:
               case 76:
               case 77:
               case 78:
               case 79:
               case 80:
               case 81:
               case 82:
               case 83:
               case 84:
               case 85:
               case 86:
               case 87:
                  return true;
               default:
                  return false;
            }
         default:
            return false;
      }
   }

   public String getIngredientsAsString() {
      StringBuilder buf = new StringBuilder();
      byte groupId = -1;
      IngredientGroup group = null;

      for(Ingredient ingredient : this.allIngredients.values()) {
         group = this.getGroupById(ingredient.getGroupId());
         if (group != null && group.getGroupType() > 0) {
            byte newGroupId = ingredient.getGroupId();
            if (groupId != newGroupId) {
               IngredientGroup oldGroup;
               if (groupId > -1 && (oldGroup = this.getGroupById(groupId)) != null) {
                  switch(oldGroup.getGroupType()) {
                     case 2:
                        buf.append("]");
                        break;
                     case 3:
                        buf.append(")");
                        break;
                     case 4:
                        buf.append(")+");
                  }

                  buf.append(",");
               }

               switch(group.getGroupType()) {
                  case 2:
                     buf.append("[");
                     break;
                  case 3:
                     buf.append("(");
                     break;
                  case 4:
                     buf.append("(");
                     break;
                  case 5:
                     buf.append("[");
               }
            } else {
               switch(group.getGroupType()) {
                  case 1:
                     buf.append(",");
                     break;
                  case 2:
                     buf.append("|");
                     break;
                  case 3:
                     buf.append("|");
                     break;
                  case 4:
                     buf.append("|");
                     break;
                  case 5:
                     buf.append(",[");
               }
            }

            buf.append(Recipes.getIngredientName(ingredient));
            groupId = newGroupId;
            switch(group.getGroupType()) {
               case 5:
                  buf.append("]");
            }
         }
      }

      if (group != null) {
         switch(group.getGroupType()) {
            case 2:
               buf.append("]");
               break;
            case 3:
               buf.append(")");
               break;
            case 4:
               buf.append(")+");
         }
      }

      return buf.toString();
   }

   void clearFound() {
      for(IngredientGroup ig : this.ingredientGroups) {
         ig.clearFound();
      }
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();
      buf.append("Recipe:");
      buf.append("recipeId:" + this.recipeId);
      if (this.name.length() > 0) {
         buf.append(",name:" + this.name);
      }

      if (this.skillId > 0) {
         buf.append(",skill:" + this.skillName + "(" + this.skillId + ")");
      }

      buf.append(",trigger:" + this.getTriggerName());
      if (!this.cookers.isEmpty()) {
         buf.append(",cookers[");
         boolean first = true;

         for(Entry<Short, String> me : this.cookers.entrySet()) {
            if (first) {
               first = false;
            } else {
               buf.append(",");
            }

            buf.append((String)me.getValue() + "(" + me.getKey() + "),dif=" + this.cookersDif.get(me.getKey()));
         }

         buf.append("]");
      }

      if (!this.containers.isEmpty()) {
         buf.append(",containers[");
         boolean first = true;

         for(Entry<Short, String> me : this.containers.entrySet()) {
            if (first) {
               first = false;
            } else {
               buf.append(",");
            }

            buf.append((String)me.getValue() + "(" + me.getKey() + "),dif=" + this.containersDif.get(me.getKey()));
         }

         buf.append("]");
      }

      if (this.activeItem != null) {
         buf.append(",activeItem:" + this.activeItem.toString());
      }

      if (this.targetItem != null) {
         buf.append(",target:" + this.targetItem.toString());
      }

      if (!this.ingredientGroups.isEmpty()) {
         buf.append(",ingredients{");
         boolean first = true;

         for(IngredientGroup ig : this.ingredientGroups) {
            if (first) {
               first = false;
            } else {
               buf.append(",");
            }

            buf.append(ig.toString());
         }

         buf.append("}");
      }

      if (this.resultItem != null) {
         buf.append(",result:" + this.resultItem.toString());
      }

      if (this.achievementId != -1) {
         buf.append(",achievementTriggered{");
         buf.append(this.achievementName + "(" + this.achievementId + ")");
         buf.append("}");
      }

      buf.append("}");
      return buf.toString();
   }

   class Liquid {
      final short id;
      final int ratio;
      int weight = 0;
      final int loss;
      final String name;

      Liquid(short id, String name, int ratio, int loss) {
         this.id = id;
         this.name = name;
         this.ratio = ratio;
         this.loss = loss;
      }

      short getId() {
         return this.id;
      }

      String getName() {
         return this.name;
      }

      int getRatio() {
         return this.ratio;
      }

      int getAbsRatio() {
         return Math.abs(this.ratio);
      }

      int getWeight() {
         return this.weight;
      }

      int getLoss() {
         return this.loss;
      }

      void setWeight(int newWeight) {
         this.weight = newWeight;
      }
   }

   public class LiquidResult {
      private final Map<Short, String> errors = new HashMap<>();
      private int newWeight = 0;

      LiquidResult() {
      }

      public boolean isSuccess() {
         return this.errors.isEmpty();
      }

      public Map<Short, String> getErrors() {
         return this.errors;
      }

      void add(short templateId, String error) {
         this.errors.put(templateId, error);
      }

      void setNewWeight(int newWeight) {
         this.newWeight = newWeight;
      }

      public int getNewWeight() {
         return this.newWeight;
      }
   }
}
