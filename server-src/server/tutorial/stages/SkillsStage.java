package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class SkillsStage extends TutorialStage {
   private static final short WINDOW_ID = 1300;

   @Override
   public short getWindowId() {
      return (short)(1300 + this.getCurrentSubStage());
   }

   public SkillsStage(long playerId) {
      super(playerId);
   }

   @Override
   public TutorialStage getNextStage() {
      return new CombatStage(this.getPlayerId());
   }

   @Override
   public TutorialStage getLastStage() {
      return new MiningStage(this.getPlayerId());
   }

   @Override
   public void buildSubStages() {
      this.subStages.add(new SkillsStage.SkillWindowSubStage(this.getPlayerId()));
      this.subStages.add(new SkillsStage.SkillGainSubStage(this.getPlayerId()));
   }

   public class SkillGainSubStage extends TutorialStage.TutorialSubStage {
      public SkillGainSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Skills", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nThere are various ways to increase the amount of skill gain you receive from an action such as Affinities, Item Enchantments and Sleep Bonus.\r\n\r\nSleep Bonus is gained while you are offline and have logged out from a bed or from completing special missions. When you have some Sleep Bonus, you can enable it from the quickbar to double any skill gains (with a few exceptions) you receive while Sleep Bonus is active.\r\n\r\n",
                  null,
                  null,
                  null,
                  300,
                  400
               )
               .addImage("image.tutorial.sleepbonus", 300, 100)
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

      @Override
      public void triggerOnView() {
         try {
            Player p = Players.getInstance().getPlayer(this.getPlayerId());
            p.getCommunicator().sendToggleQuickbarBtn((short)2006, true);
         } catch (NoSuchPlayerException var2) {
         }
      }
   }

   public class SkillWindowSubStage extends TutorialStage.TutorialSubStage {
      public SkillWindowSubStage(long playerId) {
         super(playerId);
      }

      @Override
      protected void buildBMLString() {
         BMLBuilder builder = BMLBuilder.createBMLBorderPanel(
            BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Skills", Color.LIGHT_GRAY)),
            null,
            BMLBuilder.createVertArrayNode(false)
               .addPassthrough("tutorialid", Long.toString(this.getPlayerId()))
               .addText(
                  "\r\nMost actions you can complete in Wurm also raise a related Skill, and are sometimes limited by the level you have in the related Skill.\r\n\r\nAll skills start out at level 1, and will slowly increase as you complete actions - up to 100. However the higher the skill, the slower the increases will come.\r\n\r\nAs a free player on Wurm Online your skills are limited to a maximum of 20. This limit is then removed while you are a premium player or during special event weekends.\r\n\r\n",
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

      @Override
      public void triggerOnView() {
         try {
            Player p = Players.getInstance().getPlayer(this.getPlayerId());
            p.getCommunicator().sendOpenWindow((short)16, true);
            p.getCommunicator().sendToggleQuickbarBtn((short)2008, true);
         } catch (NoSuchPlayerException var2) {
         }
      }
   }
}
