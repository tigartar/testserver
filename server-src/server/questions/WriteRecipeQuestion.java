package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.shared.constants.ItemMaterials;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

public final class WriteRecipeQuestion extends Question implements ItemMaterials {
   private Recipe[] recipes;
   private Item paper;

   public WriteRecipeQuestion(Creature aResponder, Item apaper) {
      super(aResponder, "Select Recipe", "Select Recipe", 138, -10L);
      this.paper = apaper;
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      String sel = answers.getProperty("recipe");
      int selId = Integer.parseInt(sel);
      Recipe recipe = this.recipes[selId];
      this.paper.setInscription(recipe, this.getResponder().getName(), 0);
      this.getResponder().getCommunicator().sendNormalServerMessage("You carefully finish writing the recipe \"" + recipe.getName() + "\" and sign it.");
   }

   @Override
   public void sendQuestion() {
      this.recipes = Recipes.getUnknownRecipes();
      Arrays.sort(this.recipes, new Comparator<Recipe>() {
         public int compare(Recipe param1, Recipe param2) {
            return param1.getName().compareTo(param2.getName());
         }
      });
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("harray{label{text=\"Recipe\"};");
      buf.append("dropdown{id=\"recipe\";default=\"0\";options=\"");

      for(int i = 0; i < this.recipes.length; ++i) {
         if (i > 0) {
            buf.append(",");
         }

         Recipe recipe = this.recipes[i];
         buf.append(recipe.getName().replace(",", "") + " - " + recipe.getRecipeId());
      }

      buf.append("\"}}");
      buf.append("label{text=\"\"}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 120, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
