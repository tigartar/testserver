package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public final class WurmInfo extends Question {
   public WurmInfo(Creature aResponder) {
      super(aResponder, "Cooking test info", "Change test info", 15, -10L);
   }

   @Override
   public void answer(Properties answers) {
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("label{type=\"bold\";text=\"New command\"}");
      buf.append("label{text=\"/toggleccfp\"}");
      buf.append("label{text=\"    will toggle visibility of the ccfp bar.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"Cooking Test commands\"}");
      buf.append("label{text=\"/changelog\"}");
      buf.append("label{text=\"    will show the full changelog.\"}");
      buf.append("label{text=\"/info\"}");
      buf.append("label{text=\"    will show this information.\"}");
      buf.append("label{text=\"/news\"}");
      buf.append("label{text=\"    will show each update if i remember to change it.\"}");
      buf.append("label{text=\"/resetccfp\"}");
      buf.append("label{text=\"    will reset your CCFP Values to zero, will not show immediatly, \"}");
      buf.append("label{text=\"    as needs a hunger change before the values are sent to client.\"}");
      buf.append("label{text=\"/resetfood\"}");
      buf.append("label{text=\"    to set your food to approx 31%.\"}");
      buf.append("label{text=\"    does not set to zero as may cause issues with fat layers.\"}");
      buf.append("label{text=\"/resetthirst\"}");
      buf.append("label{text=\"    to set your water bar to approx 0%.\"}");
      if (this.getResponder().getPower() >= 2) {
         buf.append("label{text=\"#listwildhives x\"}");
         buf.append("label{text=\"    Lists the hives that have two queens, does not matter what the parm is.\"}");
         buf.append("label{text=\"    Will give wild hives then domestic ones together with x,y.\"}");
         buf.append("label{text=\"#listwildhives\"}");
         buf.append("label{text=\"    List of wild hives and any honey in them!\"}");
         buf.append("label{text=\"    Will give wild hives only with their x and y, amount of honey, wax and number of queens.\"}");
         buf.append("label{text=\"#removewildhives\"}");
         buf.append("label{text=\"    Will remove all wild hives and anything in them.\"}");
         buf.append("label{text=\"    SO USE WITH CARE!!\"}");
         buf.append("label{text=\"    Outputs the list of hives that were destroyed.\"}");
         buf.append("label{text=\"#removeknownrecipes [name]\"}");
         buf.append("label{text=\"    Removes known recipes from the specified player, if name is specified.\"}");
         buf.append("label{text=\"    If no name given, then will remove all known recipes from everyone...\"}");
         buf.append("label{text=\"    SO USE WITH CARE!!\"}");
         buf.append("label{text=\"Note: New tab added for debugging wild hives.\"}");
      }

      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"CCFP Bonuses...\"}");
      buf.append("label{text=\"    Calories => reduced stamina drain.\"}");
      buf.append("label{text=\"    Carbs => reduced water usage.\"}");
      buf.append("label{text=\"    Fats => increased favour regeneration.\"}");
      buf.append("label{text=\"    Proteins => reduced food usage.\"}");
      buf.append("label{text=\"\"}");
      buf.append("label{type=\"bold\";text=\"Result Bonus value...\"}");
      buf.append("label{text=\"It is calculated from each ingredients bonus, plus\"}");
      buf.append("label{text=\"    Each ingredient template id, real template, pstate, cstate, material\"}");
      buf.append("label{text=\"    plus Cooker template id,\"}");
      buf.append("label{text=\"    plus Container template id.\"}");
      buf.append("label{text=\"Note: Fresh ingredient.v.non-fresh will give a different bonus.\"}");
      buf.append("label{text=\"      Fresh state gets lost when you put something in a fsb.\"}");
      buf.append("label{text=\"Also when the result item is eaten, it adds in your player id, and uses that for a pointer \"}");
      buf.append("label{text=\"into the skill list, thus giving you a timed affinity.\"}");
      buf.append("label{text=\"Note: if the bonus was -1 on the item being eaten, then no timed affinity is given.\"}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(480, 500, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   public static String getInfo() {
      return "";
   }
}
