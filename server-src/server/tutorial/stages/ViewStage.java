package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class ViewStage extends TutorialStage {
   private static final short WINDOW_ID = 200;

   @Override
   public short getWindowId() {
      return (short)(200 + this.getCurrentSubStage());
   }

   public ViewStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new MovementStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new WelcomeStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new ViewStage.ViewSubStage(this.getPlayerId()));
   }

   public class ViewSubStage extends TutorialStage.TutorialSubStage {
      public ViewSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.MOVED_PLAYER_VIEW);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Looking Around", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nClick and hold the Left Mouse Button then move the mouse to look around at your surroundings.\r\n\r\n", null, null, null, 300, 400
               )
               .addText(""),
            null,
            BMLBuilder.createLeftAlignedNode(
               BMLBuilder.createHorizArrayNode(false)
                  .addButton("back", "Back", 80, 20, true)
                  .addText("", null, null, null, 35, 0)
                  .addButton("next", "Waiting...", 80, 20, false)
                  .maybeAddSkipButton()
            )
         );
         this.bmlString = builder.toString();
      }
   }
}
