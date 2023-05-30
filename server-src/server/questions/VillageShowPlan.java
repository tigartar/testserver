package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public class VillageShowPlan extends Question {
   private final Village deed;

   public VillageShowPlan(Creature aResponder, Village tokenVill) {
      super(aResponder, "Plan of " + tokenVill.getName(), "", 125, (long)tokenVill.getId());
      this.deed = tokenVill;
   }

   @Override
   public void answer(Properties aAnswers) {
   }

   @Override
   public void sendQuestion() {
      int perimTiles = this.deed.getTotalPerimeterSize();
      this.getResponder()
         .getCommunicator()
         .sendShowDeedPlan(
            this.getId(),
            this.deed.getName(),
            this.deed.getTokenX(),
            this.deed.getTokenY(),
            this.deed.getStartX(),
            this.deed.getStartY(),
            this.deed.getEndX(),
            this.deed.getEndY(),
            perimTiles
         );
   }
}
