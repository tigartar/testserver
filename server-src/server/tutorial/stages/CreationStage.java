/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.MiningStage;
import com.wurmonline.server.tutorial.stages.WoodcuttingStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class CreationStage
extends TutorialStage {
    private static final short WINDOW_ID = 1100;

    @Override
    public short getWindowId() {
        return (short)(1100 + this.getCurrentSubStage());
    }

    public CreationStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new MiningStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return new WoodcuttingStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new CreationWindowSubStage(this.getPlayerId()));
        this.subStages.add(new CreateKindlingSubStage(this.getPlayerId()));
        this.subStages.add(new CreateCampfireSubStage(this.getPlayerId()));
    }

    public class CreateCampfireSubStage
    extends TutorialStage.TutorialSubStage {
        public CreateCampfireSubStage(long playerId) {
            super(CreationStage.this, playerId);
            this.setNextTrigger(PlayerTutorial.PlayerTrigger.CREATE_CAMPFIRE);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Creating Items", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nNow you can create a Campfire using the kindling you just made and the steel & flint in your inventory.\r\n\r\nActivate the steel & flint, then right click the kindling and select the Create>Campfire action.\r\n\r\nAlternatively you can drag the steel & flint and kindling items from your inventory to the Creation Window and select campfire from the list on the right, then click Create.\r\n\r\nCreate a campfire to continue.\r\n\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Waiting...", 80, 20, false).maybeAddSkipButton()));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendToggleQuickbarBtn((short)2012, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class CreateKindlingSubStage
    extends TutorialStage.TutorialSubStage {
        public CreateKindlingSubStage(long playerId) {
            super(CreationStage.this, playerId);
            this.setNextTrigger(PlayerTutorial.PlayerTrigger.CREATE_KINDLING);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Creating Items", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nDrag your hatchet from your inventory to one side of the Creation Window, and a Log from your inventory to the other side of the Creation Window.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.creation2", 300, 150).addText("\r\nSelect Kindling from the list on the right hand side, then click the Create button.\r\n\r\nCreate some kindling to continue.\r\n\r\n", null, null, null, 300, 400).addText("").toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Waiting...", 80, 20, false).maybeAddSkipButton()));
            this.bmlString = builder.toString();
        }
    }

    public class CreationWindowSubStage
    extends TutorialStage.TutorialSubStage {
        public CreationWindowSubStage(long playerId) {
            super(CreationStage.this, playerId);
            this.setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_CREATION);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Creating Items", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nMost item creation in Wurm can be done in one of two ways - using the Crafting Window, or by activating an item and selecting the relevant Create action on another item.\r\n\r\nLet's make some kindling with the Crafting Window. Click the Crafting Window button to open the window.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.creation", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Waiting...", 80, 20, false).maybeAddSkipButton()));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendToggleQuickbarBtn((short)2011, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }
}

