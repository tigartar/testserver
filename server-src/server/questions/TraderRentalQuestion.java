package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public final class TraderRentalQuestion extends Question {
   public TraderRentalQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
      super(aResponder, aTitle, aQuestion, 22, aTarget);
   }

   @Override
   public void answer(Properties answers) {
      this.setAnswer(answers);
      QuestionParser.parseTraderRentalQuestion(this);
   }

   @Override
   public void sendQuestion() {
      boolean citiz = false;
      Village v = this.getResponder().getCurrentTile().getVillage();
      if (v != null) {
         citiz = true;
      }

      StringBuilder buf = new StringBuilder(this.getBmlHeader());
      buf.append("text{text=''}");
      buf.append("text{type=\"bold\";text='Traders:'}");
      buf.append("text{text='There are two types of traders:'}");
      buf.append("text{text='1. A normal trader buys and sells anything. He owns his own shop.'}");
      buf.append("text{text='2. A personal merchant tries to sell anything you give him to other players. Then you can come back and collect the money.'}");
      buf.append("text{text='Traders will only appear in finished structures where no other creatures but you stand.'}");
      buf.append(
         "text{text='If you are citizen of a village or homestead, the trader will donate part of its income from foreign traders to the settlement funds.'}"
      );
      buf.append("text{text=''}");
      buf.append("text{type='bold';text='Hire normal trader:'}");
      buf.append("text{text='By using this contract a normal trader will appear.'}");
      buf.append("text{text='You do not own a trader and can not count on receiving any money back from using the contract.'}");
      buf.append("text{text='The trader will appear where you stand, if the tile is inside a structure and contains no other creature.'}");
      buf.append("text{text='This contract will disappear once the trader arrives.'}");
      buf.append("text{text=\"The trader will stop receiving money if it doesn't sell for approximately 10% of what it purchases.\"}");
      buf.append("text{text=\"The trader will only set up shop if there are no other normal traders in the area.\"}");
      buf.append("text{text='You will not be able to set local prices, or the price modifier for a normal trader.'}");
      buf.append("text{text=''}");
      buf.append(
         "text{type=\"bold\";text=\"Note that if the trader is citizen of a settlement when it disbands, he or she will disappear regardless of whether he is on deed or not!\"}"
      );
      if (citiz && v != null) {
         buf.append("text{type='italic';color='200,40,40';text=\"The trader will become part of " + v.getName() + " and pay taxes there.\"}");
      } else {
         buf.append("text{type='italic';color='200,40,40';text='The trader will not become part of any village, so no tax revenue will be gained.'}");
      }

      buf.append("text{text=''}");
      buf.append("label{text='Gender: '}");
      if (this.getResponder().getSex() == 1) {
         buf.append("radio{ group='gender'; id='male';text='Male'}");
         buf.append("radio{ group='gender'; id='female';text='Female';selected='true'}");
      } else {
         buf.append("radio{ group='gender'; id='male';text='Male';selected='true'}");
         buf.append("radio{ group='gender'; id='female';text='Female'}");
      }

      if (citiz) {
         buf.append("text{text='You must now decide upon the fraction of the profit the trader makes that will go directly to the village upkeep fund.'}");
         buf.append("text{text='Note that for now you cannot change this number later. Max is 40 percent.'}");
         buf.append("harray{label{text='Tax, in percent: '};input{maxchars='2';id='tax';text='20'}}");
      }

      buf.append("text{text=''}");
      buf.append("harray{label{text='The trader shall be called '};input{maxchars='20';id='ntradername'};label{text='!'}}");
      buf.append("text{text=''}");
      buf.append(this.createAnswerButton2());
      this.getResponder().getCommunicator().sendBml(500, 660, true, true, buf.toString(), 200, 200, 200, this.title);
   }
}
