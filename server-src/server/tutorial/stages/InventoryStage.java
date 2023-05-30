package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class InventoryStage extends TutorialStage {
   private static final short WINDOW_ID = 400;

   @Override
   public short getWindowId() {
      return (short)(400 + this.getCurrentSubStage());
   }

   public InventoryStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new StartingStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new MovementStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new InventoryStage.InventorySubStage(this.getPlayerId()));
      this.subStages.add(new InventoryStage.MoveItemsSubStage(this.getPlayerId()));
      this.subStages.add(new InventoryStage.QualitySubStage(this.getPlayerId()));
   }

   public class InventorySubStage extends TutorialStage.TutorialSubStage {
      public InventorySubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_INVENTORY);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Inventory & Items", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText("\r\nPress [$bind:'toggle inventory'$] or click on the inventory button to open your inventory.\r\n\r\n", null, null, null, 300, 400)
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

      @Override
      public void triggerOnView() {
         try {
            Player p = Players.getInstance().getPlayer(this.getPlayerId());
            p.getCommunicator().sendToggleQuickbarBtn((short)2007, true);
         } catch (NoSuchPlayerException var2) {
         }
      }
   }

   public class MoveItemsSubStage extends TutorialStage.TutorialSubStage {
      public MoveItemsSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Inventory & Items", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nIn this window you can select items, move them around, and view any containers and items inside containers that are in your inventory.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.moveitems", 300, 150)
                     .addText(
                        "\r\nWhen opening any container item in the world (such as a chest), you will have a similar window show up for that item where you can interact with any items that are stored inside.\r\n\r\n",
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

   public class QualitySubStage extends TutorialStage.TutorialSubStage {
      public QualitySubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Inventory & Items", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nEach item in Wurm has an associated Quality Level (QL) and Damage, both between 0 and 100.\r\n\r\n", null, null, null, 300, 400
                     )
                     .addImage("image.tutorial.qldmg", 300, 150)
                     .addText(
                        "\r\nAn item with a higher QL will be more effective than a lower QL item and will generally last longer.\r\n\r\nAs an item is used it will gain Damage which lowers the effective QL of the item, and will cause it to be destroyed if the Damage ever reaches 100 without being repaired.\r\n\r\n",
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
