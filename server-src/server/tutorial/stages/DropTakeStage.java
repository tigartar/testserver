package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class DropTakeStage extends TutorialStage {
   private static final short WINDOW_ID = 800;

   @Override
   public short getWindowId() {
      return (short)(800 + this.getCurrentSubStage());
   }

   public DropTakeStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new TerraformStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new WorldStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new DropTakeStage.DropSubStage(this.getPlayerId()));
      this.subStages.add(new DropTakeStage.PlaceSubStage(this.getPlayerId()));
      this.subStages.add(new DropTakeStage.TakeSubStage(this.getPlayerId()));
   }

   public class DropSubStage extends TutorialStage.TutorialSubStage {
      public DropSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Dropping & Taking", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nRight clicking on an item in your Inventory or Character and selecting the Drop action will cause the item to drop on the ground in front of you.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.drop", 300, 150)
                     .addText(
                        "\r\nA special exception to this is when dropping a Pile of Dirt or Pile of Sand, which will change the terrain at the nearest tile corner to your position. Top drop these as an item on the ground, select the Drop As Pile action.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addText("")
                     .toString()
               ),
            null,
            BMLBuilder.createLeftAlignedNode(
               BMLBuilder.createHorizArrayNode(false)
                  .addButton("back", "Back", 80, 20, true)
                  .addText("", null, null, null, 35, 0)
                  .addButton("next", "Next", 80, 20, true)
            )
         );
         this.bmlString = builder.toString();
      }
   }

   public class PlaceSubStage extends TutorialStage.TutorialSubStage {
      public PlaceSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.PLACED_ITEM);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Dropping & Taking", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nYou can also select the Place option from the same menu instead of Drop. This will allow you to place the item in an exact position nearby using your mouse, with the rotation that you want.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.place", 300, 150)
                     .addText(
                        "\r\nUse the Mouse Scroll to rotate the item while placing, Left click to confirm placement, or right click to cancel placement.\r\n\r\nPlace an item from your Inventory to continue.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addText("")
                     .toString()
               ),
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

   public class TakeSubStage extends TutorialStage.TutorialSubStage {
      public TakeSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.TAKEN_ITEM);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Dropping & Taking", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nSimilar to dropping, once an item is on the ground or in the world, you can right click it and select the Take action in order to pick it up.\r\n\r\nTake an item from the ground to continue.\r\n",
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
