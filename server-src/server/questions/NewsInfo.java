package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public final class NewsInfo extends Question {
   public NewsInfo(Creature aResponder) {
      super(aResponder, "Latest cooking news", "Latest cooking changes", 14, -10L);
   }

   @Override
   public void answer(Properties answers) {
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("label{text=\"This will contain the latest fixes, so long as i remember to update it.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"07 Nov 2016\"}");
      buf.append("label{text=\" * Updated recipes so more can optionally have salt, tomato ketchup and mayo.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"03 Nov 2016\"}");
      buf.append("label{text=\" * Made it so only processed food items can go onto food shelves.\"}");
      buf.append("label{text=\" * Made it so only unprocessed food items can go into a fsb.\"}");
      buf.append("label{text=\" * Added two more shelves to (new) larders.\"}");
      buf.append("label{text=\" * Fix for recipes that dont use a container but do use a cooker.\"}");
      buf.append("label{text=\" * Fix for matching bread to incorrect recipe.\"}");
      buf.append("label{text=\" * Added client cookbook, to use it type ''toggle cookbook'' into the console.\"}");
      buf.append("label{text=\" * Fix for hand mirror usage.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"29 Oct 2016\"}");
      buf.append("label{text=\" * Fix for loosing recipes when you cross servers.\"}");
      buf.append("label{text=\" * Fix for filling containers.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"28 Oct 2016\"}");
      buf.append("label{text=\" * Fix so one or more items works correctly.\"}");
      buf.append("label{text=\" * Removed ability to put butter in fsb.\"}");
      buf.append("label{text=\" * Put picking herbs and spices under deed harvest fruit permission.\"}");
      buf.append("label{text=\" * fix for chocolate coated nut having wrong recipe name.\"}");
      buf.append("label{text=\" * Made it possible to unseal when liquid isnt fermenting.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"27 Oct 2016\"}");
      buf.append("label{text=\" * Fix for dagwood.\"}");
      buf.append("label{text=\" * Updated the messages when you learn a new recipe.\"}");
      buf.append("label{text=\" * Expanded the volume setting for measureing jug to include 1g, 2g and 5g.\"}");
      buf.append("label{text=\" * Changed model for pancakes, new ones will use the omlette model.\"}");
      buf.append("label{text=\" * Updated the messages for writing recipes.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"26 Oct 2016\"}");
      buf.append("label{text=\" * Added missing fermenting recipes.\"}");
      buf.append("label{text=\" * Fix for LORE allowing partial matches when it should not.\"}");
      buf.append("label{text=\" * Fixed the cream merging with whipped cream.\"}");
      buf.append("label{text=\" * Enabled fermenting of unfermented spirits.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"25 Oct 2016\"}");
      buf.append("label{text=\" * Added message when you learn a new recipe.\"}");
      buf.append("label{text=\" * Fix for zombiefied spirits.\"}");
      buf.append("label{text=\" * Fix for LORE message when too many of an item.\"}");
      buf.append("label{text=\" * Made the number of slices you get from bread and cake depend on the weight of the item.\"}");
      buf.append("label{text=\" * Fix to remove fillet option when food knife is acive for meats and fish.\"}");
      buf.append("label{text=\" * Fix so LORE tells you the required material (if its specified in recipe).\"}");
      buf.append("label{text=\" * Fix so you cant attempt to add a recipe to cookbook when its not in inventory (part 2).\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"24 Oct 2016\"}");
      buf.append("label{text=\" * Added fermenting phase to spirits.\"}");
      buf.append("label{text=\" * Added difficulty to LORE output.\"}");
      buf.append("label{text=\" * Fix so you cant attempt to add a recipe to cookbook when its not in inventory.\"}");
      buf.append("label{text=\" * Added old style sandwiches back in (now called endurance sandwiches).\"}");
      buf.append("label{text=\"   They are made by using bread on cheese (or jam, egg, syrup or honey). \"}");
      buf.append("label{text=\"   They can be eaten even when hungry for the stamina regeneration, but \"}");
      buf.append("label{text=\"   will not give any CCFP or a timed affinity.\"}");
      buf.append("label{text=\" * Added new command to toggle visibility of ccfp bar (/toggleccfp).\"}");
      buf.append("label{text=\" * Made tar combinable.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"23 Oct 2016\"}");
      buf.append("label{text=\" * Fix so can plant fresh spices.\"}");
      buf.append("label{text=\" * Fixed recipes that used stoneware.\"}");
      buf.append("label{text=\" * Fix for 'you cant breed egglayers' with animals that you should be able to.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"22 Oct 2016\"}");
      buf.append("label{text=\" * Typo fix in fudge sauce recipe name.\"}");
      buf.append("label{text=\" * Made beersteins easier to make.\"}");
      buf.append("label{text=\" * Fix for double spacing in lore statement.\"}");
      buf.append("label{text=\" * Fix so sweets can go into larder.\"}");
      buf.append("label{text=\" * Fix so can plant items (again).\"}");
      buf.append("label{text=\" * Fix so beesmoker when made does not end up on floor.\"}");
      buf.append("label{text=\" * Recipe typo fixes.\"}");
      buf.append("label{text=\" * Fix for examine message on chopped veg (etc).\"}");
      buf.append("label{text=\" * Fix for message given when you write a recipe.\"}");
      buf.append("label{text=\" * Fix for out of bounds error when using back button in cookbook.\"}");
      buf.append("label{text=\" * Renamed stoneware to baking stone.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"21 Oct 2016\"}");
      buf.append("label{text=\" * Fix so cooked meat in fsb uses cooked meat icon.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"18 Oct 2016\"}");
      buf.append("label{text=\" * Enabled pottery planters to be planted.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"17 Oct 2016\"}");
      buf.append("label{text=\" * Fix so can plant 4 trellis on a tile.\"}");
      buf.append("label{text=\" * Fixed recipe transfering when player does.\"}");
      buf.append("label{text=\" * Enabled trellis to be planted against walls and fences (as well s tile borders).\"}");
      buf.append("label{text=\"\"}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(500, 500, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public static String getInfo() {
      return "";
   }
}
