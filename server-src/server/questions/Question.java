package com.wurmonline.server.questions;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Question implements MiscConstants, QuestionTypes {
   private static int ids = 0;
   protected final int id;
   private Creature responder;
   private final boolean answered = false;
   private final String html = "";
   final String title;
   protected final String question;
   final int type;
   final long target;
   private final long sent = System.currentTimeMillis();
   private Properties answer = null;
   protected static final Logger logger = Logger.getLogger(Question.class.getName());
   static final String CHECKED = "CHECKED";
   static final String CHECKED2 = ";selected='true'";
   public int windowSizeX = 600;
   public int windowSizeY = 400;
   static final String colourCommon = "";
   static final String colourRare = "color=\"66,153,225\";";
   static final String colourSupreme = "color=\"0,255,255\";";
   static final String colourFantastic = "color=\"255,0,255\";";

   Question(Creature aResponder, String aTitle, String aQuestion, int aType, long aTarget) {
      this.id = ids++;
      this.responder = aResponder;
      this.title = aTitle;
      this.question = aQuestion;
      this.type = aType;
      this.target = aTarget;
      Questions.addQuestion(this);
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Created " + this);
      }
   }

   public final long getSendTime() {
      return this.sent;
   }

   public final int getId() {
      return this.id;
   }

   public final long getTarget() {
      return this.target;
   }

   public final int getType() {
      return this.type;
   }

   public final Creature getResponder() {
      return this.responder;
   }

   void clearResponder() {
      this.responder = null;
   }

   public final boolean isAnswered() {
      return false;
   }

   public final String getTitle() {
      return this.title;
   }

   public final String getQuestion() {
      return this.question;
   }

   final String getHtmlHeader() {
      return "<B>" + this.question + "</B><FORM action=\"\" method=\"post\"><INPUT TYPE=\"hidden\" name=\"id\" value=\"" + this.id + "\"><BR>";
   }

   final String getBmlHeader() {
      return "border{center{text{type='bold';text=\""
         + this.question
         + "\"}};null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\""
         + this.id
         + "\"}";
   }

   final String getBmlHeaderNoQuestion() {
      return "border{null;null;scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + this.id + "'}";
   }

   final String getBmlHeaderWithScroll() {
      return "border{scroll{vertical='true';horizontal='true';varray{rescale='false';passthrough{id='id';text='" + this.id + "'}";
   }

   final String getBmlHeaderWithScrollAndQuestion() {
      return "border{center{text{type='bold';text=\""
         + this.question
         + "\"}};null;scroll{vertical=\"true\";horizontal=\"true\";varray{rescale=\"false\";passthrough{id=\"id\";text=\""
         + this.id
         + "\"}";
   }

   final String getBmlHeaderScrollOnly() {
      return "scroll{vertical='true';horizontal='true';varray{rescale='false';passthrough{id='id';text='" + this.id + "'}";
   }

   final String createAnswerButtonForNoBorder() {
      return "harray {button{text='Send';id='submit'}}};}";
   }

   final String createAnswerButton() {
      return "<INPUT type=\"submit\" VALUE=\"Send\">";
   }

   final String createAnswerButton2() {
      return this.createAnswerButton2("Send");
   }

   final String createAnswerButton2(String sendText) {
      return "harray {button{text='" + sendText + "';id='submit'}}}};null;null;}";
   }

   final String createOkAnswerButton() {
      return "harray {button{text='Ok';id='submit'}}}};null;null;}";
   }

   final String createBackAnswerButton() {
      return "harray {button{text='Send';id='submit'};label{text=' ';id='spacedlxg'};button{text='Previous';id='back'}}}};null;null;}";
   }

   final String createSurveyPerimeterButton() {
      return "harray {button{text='Survey Perimeter';id='submit'};label{text=' ';id='spacedlxg'};button{text='Go Back';id='back'}}}};null;null;}";
   }

   final String createSurveyButton() {
      return "harray {button{text='Cancel';id='cancel'};label{text=' ';id='spacedlxg'};button{text='Survey Area';id='submit'}}}};null;null;}";
   }

   final String createAnswerButton3() {
      return "harray {button{text='Send';id='submit'}}}};null;null;null;null;}";
   }

   final byte[] getHtmlAsBytes() {
      return "".getBytes();
   }

   void setAnswer(Properties aAnswer) {
      this.answer = aAnswer;
   }

   final Properties getAnswer() {
      return this.answer;
   }

   public abstract void answer(Properties var1);

   public abstract void sendQuestion();

   @Override
   public final String toString() {
      return "Question [id: "
         + this.id
         + ", type: "
         + this.type
         + ", target: "
         + this.target
         + ", sent: "
         + new Date(this.sent)
         + ", title: "
         + this.title
         + ']';
   }

   public void timedOut() {
   }

   public static String itemNameWithColorByRarity(Item litem) {
      StringBuilder sb = new StringBuilder();
      String star = "";
      if (litem.getItemsAsArray().length > 0) {
         star = "* ";
      }

      if (litem.isBulkItem()) {
         int nums = litem.getBulkNums();

         try {
            ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(litem.getRealTemplateId());
            sb.append(it.sizeString);
            if (nums > 1) {
               MaterialUtilities.appendNameWithMaterialSuffix(sb, it.getPlural(), litem.getMaterial());
            } else {
               MaterialUtilities.appendNameWithMaterialSuffix(sb, it.getName(), litem.getMaterial());
            }
         } catch (NoSuchTemplateException var5) {
            logger.log(Level.WARNING, litem.getWurmId() + " bulk nums=" + nums + " but template is " + litem.getBulkTemplateId());
         }
      } else {
         String name = litem.getName().length() == 0 ? litem.getTemplate().getName() : litem.getName();
         MaterialUtilities.appendNameWithMaterialSuffix(sb, name, litem.getMaterial());
      }

      if (litem.getDescription().length() > 0) {
         sb.append(" (" + litem.getDescription() + ")");
      }

      String name = sb.toString();
      return nameColoredByRarity(name, star, litem.getRarity(), false);
   }

   public static String nameColoredByRarity(String name, String star, byte rarity, boolean isBold) {
      String bold = isBold ? "type=\"bold\";" : "";
      switch(rarity) {
         case 1:
            return "label{" + bold + "color=\"66,153,225\";" + "text=\"" + star + "rare " + name.replace("\"", "''") + "\"};";
         case 2:
            return "label{" + bold + "color=\"0,255,255\";" + "text=\"" + star + "supreme " + name.replace("\"", "''") + "\"};";
         case 3:
            return "label{" + bold + "color=\"255,0,255\";" + "text=\"" + star + "fantastic " + name.replace("\"", "''") + "\"};";
         default:
            return "label{" + bold + "" + "text=\"" + star + name.replace("\"", "''") + "\"};";
      }
   }

   boolean getBooleanProp(String name) {
      String bool = this.answer.getProperty(name);
      return bool != null && bool.equalsIgnoreCase("true");
   }

   String getStringProp(String name) {
      String string = this.answer.getProperty(name);
      return string == null ? "" : string;
   }

   String colHeader(String aName, int numb, int sortBy) {
      StringBuilder buf = new StringBuilder();
      buf.append(
         "button{text=\""
            + aName
            + (sortBy == numb ? " /\\" : (sortBy == -numb ? " \\/" : ""))
            + "\";id=\"sort"
            + (sortBy == numb ? "-" + numb : "" + numb)
            + "\"};"
      );
      return buf.toString();
   }
}
