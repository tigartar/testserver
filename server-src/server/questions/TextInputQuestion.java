package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmColor;
import java.util.Properties;

public final class TextInputQuestion extends Question {
   private final int maxSize;
   private String oldtext = "";
   private boolean sign = false;
   private static final String NOCHANGE = "No change";
   private Item liquid;
   private final Item[] items;

   public TextInputQuestion(Creature aResponder, String aTitle, String aQuestion, int aType, long aTarget, int aMaxsize, boolean isSign) {
      super(aResponder, aTitle, aQuestion, aType, aTarget);
      this.maxSize = aMaxsize;
      this.sign = isSign;
      this.items = new Item[0];
   }

   public TextInputQuestion(Creature aResponder, String aTitle, String aQuestion, Item[] aTargets) {
      super(aResponder, aTitle, aQuestion, 1, -10L);
      this.maxSize = 20;
      this.sign = false;
      this.items = aTargets;
   }

   public void setLiquid(Item aLiquid) {
      this.liquid = aLiquid;
   }

   @Override
   public void sendQuestion() {
      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      if (this.target == -10L) {
         buf.append("label{text=\"Renaming multiple items at the same time.\"}");
      }

      buf.append("input{id=\"answer\";maxchars=\"" + this.maxSize + "\";");
      if (this.type == 2 && this.liquid != null) {
         buf.append("maxlines=\"-1\";bgcolor=\"200,200,200\";");
         if (this.liquid.getTemplateId() == 753) {
            buf.append("color=\"0,0,0\";");
         } else {
            buf.append(
               "color=\""
                  + WurmColor.getColorRed(this.liquid.color)
                  + ","
                  + WurmColor.getColorGreen(this.liquid.color)
                  + ","
                  + WurmColor.getColorBlue(this.liquid.color)
                  + "\";"
            );
         }
      }

      buf.append("text=\"" + this.oldtext + "\"}");
      if (this.sign) {
         buf.append("harray{label{text='Sign image'}dropdown{id='data1';options=\"");
         buf.append("No change,");
         buf.append(
            "Bowl,Beer,Bear and beer,Crops,Construction,Sleep,Wine,Coins,Horse,Hunt,Sword and bowl,Lumber,Swordsmith,Anvil,Helmet,Baker,Shipwright,Anchor,Pirate,Mystery,Tailor,Alchemy,"
         );
         buf.append("No change");
         buf.append("\"}}");
      }

      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseTextInputQuestion(this, this.liquid);
   }

   String getOldtext() {
      return this.oldtext;
   }

   public void setOldtext(String aOldtext) {
      this.oldtext = aOldtext;
   }

   Item[] getItems() {
      return this.items;
   }
}
