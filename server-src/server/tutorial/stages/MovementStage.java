package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class MovementStage extends TutorialStage {
   private static final short WINDOW_ID = 300;

   @Override
   public short getWindowId() {
      return (short)(300 + this.getCurrentSubStage());
   }

   public MovementStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new InventoryStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new ViewStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new MovementStage.WASDSubStage(this.getPlayerId()));
      this.subStages.add(new MovementStage.ClimbingOnSubStage(this.getPlayerId()));
      this.subStages.add(new MovementStage.ClimbingOffSubStage(this.getPlayerId()));
      this.subStages.add(new MovementStage.HealthStaminaSubStage(this.getPlayerId()));
   }

   public class ClimbingOffSubStage extends TutorialStage.TutorialSubStage {
      public ClimbingOffSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.DISABLED_CLIMBING);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Movement", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nWhile climbing mode is enabled, you'll walk a lot slower and drain more stamina than usual.\r\n\r\nDisable climbing mode by pressing $bind:TOGGLE_CLIMB$ or the climb button again.\r\n\r\n",
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

   public class ClimbingOnSubStage extends TutorialStage.TutorialSubStage {
      public ClimbingOnSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_CLIMBING);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Movement", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nSome land in Wurm can be too steep for you to simply walk across.\r\n\r\nEnable climbing mode by pressing $bind:TOGGLE_CLIMB$ or the climb button in order to climb steep slopes.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
               )
               .addImage("image.tutorial.climbing", 300, 150)
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
            p.getCommunicator().sendOpenWindow((short)9, true);
            p.getCommunicator().sendToggleQuickbarBtn((short)2001, true);
         } catch (NoSuchPlayerException var2) {
         }
      }
   }

   public class HealthStaminaSubStage extends TutorialStage.TutorialSubStage {
      public HealthStaminaSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Movement", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createScrollPanelNode(true, false)
               .addString(
                  BMLBuilder.createVertArrayNode(false)
                     .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
                     .addText(
                        "\r\nKeep an eye on your stamina while moving, climbing or doing actions.\r\n\r\nRunning out of stamina can lead to your character getting tired and walking slower, completing actions slower, or possibly falling from a steep slope if you are climbing.\r\n\r\n",
                        null,
                        null,
                        null,
                        300,
                        400
                     )
                     .addImage("image.tutorial.stamina", 300, 100)
                     .addText(
                        "\r\nTo regain your stamina simply stand still for a few seconds. You cannot regain stamina while climbing is toggled on.\r\n\r\n",
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

      @Override
      public void triggerOnView() {
         try {
            Player p = Players.getInstance().getPlayer(this.getPlayerId());
            p.getCommunicator().sendOpenWindow((short)5, true);
            p.getCommunicator().sendOpenWindow((short)13, false);
         } catch (NoSuchPlayerException var2) {
         }
      }
   }

   public class WASDSubStage extends TutorialStage.TutorialSubStage {
      public WASDSubStage(long playerId) {
         super(playerId);
         this.setNextTrigger(PlayerTutorial.PlayerTrigger.MOVED_PLAYER);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Movement", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nUse the [$bind:MOVE_FORWARD$], [$bind:MOVE_LEFT$], [$bind:MOVE_BACK$] and [$bind:MOVE_RIGHT$] keys in order to move around.\r\n\r\n",
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
