package com.wurmonline.server.questions;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;
import java.util.Random;

public class AscensionQuestion extends Question implements MiscConstants {
   private final String deityName;

   public AscensionQuestion(Creature aResponder, long aTarget, String _deityName) {
      super(aResponder, "Ascension", "Are you prepared to become a DEMIGOD?", 91, aTarget);
      this.deityName = _deityName;
   }

   @Override
   public void answer(Properties aAnswers) {
      this.setAnswer(aAnswers);
      QuestionParser.parseAscensionQuestion(this);
   }

   public static final String getNewPrefix(String currentName) {
      int hash = currentName.hashCode();
      Random r = new Random((long)hash);
      int result = r.nextInt(20);
      String prefix = "Evi";
      switch(result) {
         case 0:
            prefix = "Ana";
            break;
         case 1:
            prefix = "Anti";
            break;
         case 2:
            prefix = "Dega";
            break;
         case 3:
            prefix = "Deri";
            break;
         case 4:
            prefix = "Raxa";
            break;
         case 5:
            prefix = "Meni";
            break;
         case 6:
            prefix = "Doco";
            break;
         case 7:
            prefix = "Dedi";
            break;
         case 8:
            prefix = "Ani";
            break;
         case 9:
            prefix = "Mono";
            break;
         case 10:
            prefix = "Hani";
            break;
         case 11:
            prefix = "Vidi";
            break;
         case 12:
            prefix = "Zase";
            break;
         case 13:
            prefix = "Omo";
            break;
         case 14:
            prefix = "Lono";
            break;
         case 15:
            prefix = "Togo";
            break;
         case 16:
            prefix = "Paly";
            break;
         case 17:
            prefix = "Parme";
            break;
         case 18:
            prefix = "Daga";
            break;
         case 19:
            prefix = "Jora";
            break;
         case 20:
            prefix = "Easy";
            break;
         case 21:
            prefix = "High";
            break;
         case 22:
            prefix = "Sta";
            break;
         case 23:
            prefix = "Cha";
            break;
         case 24:
            prefix = "Flo";
            break;
         case 25:
            prefix = "Tru";
            break;
         default:
            prefix = "Nami";
      }

      if ("aeiouyAEIOUY".contains(currentName.substring(0, 1))) {
         prefix = prefix.substring(0, prefix.length() - 1);
      }

      return prefix;
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getBmlHeader());
      buf.append("text{type=\"bold\";text=\"YOU ONLY HAVE 13 MINUTES TO HANDLE THIS SO LISTEN WELL:\"};");
      buf.append("text{text=\"" + this.deityName + " has given you a FANTASTIC AND UNIQUE CHANCE chance to become a demigod!\"};");
      buf.append("text{text=\"This would require that part of your soul ascends to Valrei, the home of the gods.\"};");
      buf.append("text{text=''}");
      buf.append(
         "text{text=\"The demigod"
            + this.getResponder().getName()
            + " would be in charge of and protecting a special area of Valrei and aiding your deity.\"};"
      );
      buf.append("text{text=''}");
      buf.append("text{text=\"Nothing will be changed on your current character except the name.\"};");
      buf.append("text{text=''}");
      buf.append(
         "text{text=\""
            + this.getResponder().getName()
            + " will play on as a demigod of "
            + this.deityName
            + " and if all goes well be elevated to true Deity.\"};"
      );
      buf.append("text{text=\"If a demigod later becomes a true Deity it will travel Valrei itself and also have its own very special religion.\"};");
      buf.append(
         "text{text=\"There are no promises made that "
            + this.deityName
            + " will be able to elevate your demigod to a true Deity and In the worst case the demigod is even killed or demoted.\"};"
      );
      buf.append("text{text=''}");
      buf.append("text{text=\"One thing remains clear though: whatever happens your demigod will play a very important part in the history of Wurm.\"};");
      buf.append("text{text=''}");
      buf.append("text{text=\"The chance is gone if you have not answered Yes within cirka 13 minutes of receiving this notice.\"};");
      buf.append("text{text=''}");
      buf.append("text{type='italic';text='Do you want " + this.getResponder().getName() + " to become a demigod of " + this.deityName + "?'}");
      buf.append("text{text=''}");
      buf.append("radio{ group='demig'; id='true';text='Yes'}");
      buf.append("radio{ group='demig'; id='false';text='No';selected='true'}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(500, 600, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
