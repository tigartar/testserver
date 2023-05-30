package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemMealData {
   private static final Logger logger = Logger.getLogger(ItemMealData.class.getName());
   private static final Map<Long, ItemMealData> mealData = new ConcurrentHashMap<>();
   private static final String GET_ALL_MEAL_DATA = "SELECT * FROM MEALDATA";
   private static final String CREATE_MEAL_DATA = "INSERT INTO MEALDATA(MEALID,RECIPEID,CALORIES,CARBS,FATS,PROTEINS,BONUS,STAGESCOUNT,INGREDIENTSCOUNT) VALUES(?,?,?,?,?,?,?,?,?)";
   private static final String DELETE_MEAL_DATA = "DELETE FROM MEALDATA WHERE MEALID=?";
   private static final String UPDATE_MEAL_DATA = "UPDATE MEALDATA SET RECIPEID=?,CALORIES=?,CARBS=?,FATS=?,PROTEINS=?,BONUS=? WHERE MEALID=?";
   private final long wurmId;
   private short recipeId;
   private short calories;
   private short carbs;
   private short fats;
   private short proteins;
   private byte bonus;
   private byte stagesCount;
   private byte ingredientsCount;

   public ItemMealData(long mealId, short recipeId, short calorie, short carb, short fat, short protein, byte bonus, byte stages, byte ingredients) {
      this.wurmId = mealId;
      this.recipeId = recipeId;
      this.calories = calorie;
      this.carbs = carb;
      this.fats = fat;
      this.proteins = protein;
      this.bonus = bonus;
      this.stagesCount = stages;
      this.ingredientsCount = ingredients;
   }

   public long getMealId() {
      return this.wurmId;
   }

   public short getRecipeId() {
      return this.recipeId;
   }

   public short getCalories() {
      return this.calories;
   }

   public short getCarbs() {
      return this.carbs;
   }

   public short getFats() {
      return this.fats;
   }

   public short getProteins() {
      return this.proteins;
   }

   public byte getBonus() {
      return this.bonus;
   }

   public byte getStages() {
      return this.stagesCount;
   }

   public byte getIngredients() {
      return this.ingredientsCount;
   }

   public byte getBonus(long playerId) {
      return (byte)((int)((long)this.bonus + (playerId >> 24)) & 0xFF);
   }

   boolean update(short recipeId, short calorie, short carb, short fat, short protein, byte bonus) {
      this.recipeId = recipeId;
      this.calories = calorie;
      this.carbs = carb;
      this.fats = fat;
      this.proteins = protein;
      this.bonus = bonus;
      return this.dbUpdateMealData();
   }

   private boolean dbUpdateMealData() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean sqex;
      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("UPDATE MEALDATA SET RECIPEID=?,CALORIES=?,CARBS=?,FATS=?,PROTEINS=?,BONUS=? WHERE MEALID=?");
         ps.setShort(1, this.recipeId);
         ps.setShort(2, this.calories);
         ps.setShort(3, this.carbs);
         ps.setShort(4, this.fats);
         ps.setShort(5, this.proteins);
         ps.setByte(6, this.bonus);
         ps.setLong(7, this.wurmId);
         if (ps.executeUpdate() != 1) {
            return this.dbSaveMealData();
         }

         sqex = true;
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to update item (meal) data: " + var9.getMessage(), (Throwable)var9);
         return false;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return sqex;
   }

   private boolean dbSaveMealData() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement(
            "INSERT INTO MEALDATA(MEALID,RECIPEID,CALORIES,CARBS,FATS,PROTEINS,BONUS,STAGESCOUNT,INGREDIENTSCOUNT) VALUES(?,?,?,?,?,?,?,?,?)"
         );
         ps.setLong(1, this.wurmId);
         ps.setShort(2, this.recipeId);
         ps.setShort(3, this.calories);
         ps.setShort(4, this.carbs);
         ps.setShort(5, this.fats);
         ps.setShort(6, this.proteins);
         ps.setByte(7, this.bonus);
         ps.setByte(8, this.stagesCount);
         ps.setByte(9, this.ingredientsCount);
         ps.executeUpdate();
         return true;
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to save item (meal) data: " + var9.getMessage(), (Throwable)var9);
         var5 = false;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return var5;
   }

   private boolean dbDeleteMealData() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var5;
      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MEALDATA WHERE MEALID=?");
         ps.setLong(1, this.wurmId);
         ps.executeUpdate();
         return true;
      } catch (SQLException var9) {
         logger.log(Level.WARNING, "Failed to delete item (meal) data: " + var9.getMessage(), (Throwable)var9);
         var5 = false;
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return var5;
   }

   private static final ItemMealData add(ItemMealData itemData) {
      return mealData.put(itemData.getMealId(), itemData);
   }

   public static ItemMealData getItemMealData(long mealId) {
      return mealData.get(mealId);
   }

   public static final void save(
      long mealId, short recipeId, short calories, short carbs, short fats, short proteins, byte bonus, byte stages, byte ingredients
   ) {
      ItemMealData imd = new ItemMealData(mealId, recipeId, calories, carbs, fats, proteins, bonus, stages, ingredients);
      if (add(imd) != null) {
         imd.dbUpdateMealData();
      } else {
         imd.dbSaveMealData();
      }
   }

   public static final void update(
      long mealId, short recipeId, short calories, short carbs, short fats, short proteins, byte bonus, byte stages, byte ingredients
   ) {
      ItemMealData imd = mealData.get(mealId);
      if (imd != null) {
         imd.update(recipeId, calories, carbs, fats, proteins, bonus);
      } else {
         save(mealId, recipeId, calories, carbs, fats, proteins, bonus, stages, ingredients);
      }
   }

   public static final boolean delete(long mealId) {
      ItemMealData imd = mealData.get(mealId);
      return imd != null ? imd.dbDeleteMealData() : false;
   }

   public static final int loadAllMealData() {
      int count = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM MEALDATA");
         rs = ps.executeQuery();

         while(rs.next()) {
            ++count;
            long mealId = rs.getLong("MEALID");
            short recipeId = rs.getShort("RECIPEID");
            short calories = rs.getShort("CALORIES");
            short carbs = rs.getShort("CARBS");
            short fats = rs.getShort("FATS");
            short proteins = rs.getShort("PROTEINS");
            byte bonus = rs.getByte("BONUS");
            byte stages = rs.getByte("STAGESCOUNT");
            byte ingredients = rs.getByte("INGREDIENTSCOUNT");
            add(new ItemMealData(mealId, recipeId, calories, carbs, fats, proteins, bonus, stages, ingredients));
         }
      } catch (SQLException var17) {
         logger.log(Level.WARNING, "Failed to load all meal data: " + var17.getMessage(), (Throwable)var17);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      return count;
   }
}
