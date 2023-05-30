package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class TerraformStage extends TutorialStage {
   private static final short WINDOW_ID = 900;

   @Override
   public short getWindowId() {
      return (short)(900 + this.getCurrentSubStage());
   }

   public TerraformStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new WoodcuttingStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new DropTakeStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new TerraformStage.DigExplainSubStage(this.getPlayerId()));
      this.subStages.add(new TerraformStage.PlayerDigSubStage(this.getPlayerId()));
      this.subStages.add(new TerraformStage.FlattenSubStage(this.getPlayerId()));
      this.subStages.add(new TerraformStage.LevelSubStage(this.getPlayerId()));
      this.subStages.add(new TerraformStage.TileTypeSubStage(this.getPlayerId()));
   }

   public class DigExplainSubStage extends TutorialStage.TutorialSubStage {
      public DigExplainSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Terraforming", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nTerraforming the land in Wurm is the first step in making the world your own.\r\n\r\nWith a shovel activated, hovering your mouse over the border between tiles in the world will show a slope between the tile corners.\r\n\r\nCompleting the digging action on a tile will lower the closest tile corner by 1, and dropping a pile of dirt or pile of sand will raise the closest tile corner by 1.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.digging", 300, 150)
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

   public class FlattenSubStage extends TutorialStage.TutorialSubStage {
      public FlattenSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Terraforming", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nWhen there are no height differences between corners of a tile, the slope will become flat. When all borders of a tile are flat, the entire tile is then flat.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
               )
               .addImage("image.tutorial.flatten", 300, 150)
               .addText(
                  "\r\nUsing the Flatten action on a tile will attempt to get the tile as flat as possible if there is enough dirt on the tile to move around. Flat tiles are necessary for planning buildings.\r\n\r\n",
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
                  .addButton("next", "Next", 80, 20, true)
            )
         );
         this.bmlString = builder.toString();
      }
   }

   public class LevelSubStage extends TutorialStage.TutorialSubStage {
      public LevelSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Terraforming", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nOnce a tile has become flat, you can stand on the flat tile and use the Level action on adjacent tiles to terraform them to the same height as your current tile.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
               )
               .addImage("image.tutorial.level", 300, 150)
               .addText(
                  "\r\nIf a tile needs extra dirt or sand to be dropped on it to become the same level as your current tile, dirt and sand will be used from your inventory automatically.\r\n\r\n",
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
                  .addButton("next", "Next", 80, 20, true)
            )
         );
         this.bmlString = builder.toString();
      }
   }

   public class PlayerDigSubStage extends TutorialStage.TutorialSubStage {
      public PlayerDigSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.DIG_TILE);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Terraforming", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nThe slope of land you'll be able to terraform is restricted by your Digging skill, where you can dig in slopes of up to your skill multiplied by 3.\r\n\r\nStart off by digging some dirt nearby to continue.\r\n\r\n",
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

   public class TileTypeSubStage extends TutorialStage.TutorialSubStage {
      public TileTypeSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Terraforming", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nSome land types will not be as easily terraformed as dirt and sand tiles. Tiles such as as Clay and Tar may take a lot more effort to shape, however their resources can be used for a lot more than just shaping the land.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.tiletypes", 300, 150)
                     .addText(
                        "\r\nDigging on different tile types will give you an item of the same type. Only dirt and sand can be used to raise land for terraforming, but every resource has its use outside of terraforming.\r\n\r\n",
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
}
