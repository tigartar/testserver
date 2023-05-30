package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class WoodcuttingStage extends TutorialStage {
   private static final short WINDOW_ID = 1000;

   @Override
   public short getWindowId() {
      return (short)(1000 + this.getCurrentSubStage());
   }

   public WoodcuttingStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new CreationStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new TerraformStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new WoodcuttingStage.CutDownSubStage(this.getPlayerId()));
      this.subStages.add(new WoodcuttingStage.FellTreeSubStage(this.getPlayerId()));
      this.subStages.add(new WoodcuttingStage.CreateLogSubStage(this.getPlayerId()));
   }

   public class CreateLogSubStage extends TutorialStage.TutorialSubStage {
      public CreateLogSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.CREATE_LOG);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Woodcutting", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nWith a hatchet as your active item, you can select the Chop Up action on a felled tree in order to turn it into logs.\r\n\r\nLogs can then be used to craft some items, or create other resources through further processing.\r\n\r\nCreate a log from the felled tree to continue.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
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

   public class CutDownSubStage extends TutorialStage.TutorialSubStage {
      public CutDownSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.CUT_TREE);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Woodcutting", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nAttempting to terraform a tile that has too many trees or bushes adjacent to it may make it impossible to terraform. Fortunately you can cut them down, and the tile will return to grass once the tree has fallen.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
               )
               .addImage("image.tutorial.cuttree", 300, 150)
               .addText("\r\nActivate a hatchet and select the Cut Down action on a tree tile to continue.\r\n\r\n", null, null, null, 300, 400)
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

   public class FellTreeSubStage extends TutorialStage.TutorialSubStage {
      public FellTreeSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.FELL_TREE);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Woodcutting", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nMost of the time a single swing at a tree will not be enough to fell it. Selecting the Examine action on a tree tile that has been damaged will show the current damage of the tree.\r\n\r\nWhen a tree or bush gets to 100 damage, it will fall and the tile will become grass.\r\n\r\nFinish cutting down a tree to continue.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
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
