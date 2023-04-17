/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.DropTakeStage;
import com.wurmonline.server.tutorial.stages.EquipmentStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class WorldStage
extends TutorialStage {
    private static final short WINDOW_ID = 700;

    @Override
    public short getWindowId() {
        return (short)(700 + this.getCurrentSubStage());
    }

    public WorldStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new DropTakeStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return new EquipmentStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new InteractSubStage(this.getPlayerId()));
        this.subStages.add(new SelectSubStage(this.getPlayerId()));
        this.subStages.add(new KeybindSubStage(this.getPlayerId()));
    }

    public class KeybindSubStage
    extends TutorialStage.TutorialSubStage {
        public KeybindSubStage(long playerId) {
            super(WorldStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("World Interaction", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nMost actions can also be bound to a key of your choice, which will attempt the action when the key is pressed while hovering the mouse over a certain target. You can set your keybinds from the settings window via the main menu.\r\n\r\nIn addition, double clicking most targets in the world will Examine that item and give you some extra information about them. Be careful when double clicking another creature though, as that will cause you to attempt to attack it.\r\n\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendToggleQuickbarBtn((short)2014, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class SelectSubStage
    extends TutorialStage.TutorialSubStage {
        public SelectSubStage(long playerId) {
            super(WorldStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("World Interaction", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nYou can also left click on anything in the world to select it and have it show in the Select bar. This will then show the primary actions you can take for the selected target with your current activated item as buttons on the bottom of the Select bar.\r\n\r\nIn addition you can right click the selected target in this bar to get the full right click action menu as well.\r\n\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)12, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class InteractSubStage
    extends TutorialStage.TutorialSubStage {
        public InteractSubStage(long playerId) {
            super(WorldStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("World Interaction", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nA large amount of game interaction can be completed through the menus that appear when right clicking something while hovering over it.\r\n\r\nThis will show various actions you can take with the hovered target, and may show extra options depending on what item you have activated at the time.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.rightclick", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }
}

