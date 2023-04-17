/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.EquipmentStage;
import com.wurmonline.server.tutorial.stages.InventoryStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class StartingStage
extends TutorialStage {
    private static final short WINDOW_ID = 500;

    @Override
    public short getWindowId() {
        return (short)(500 + this.getCurrentSubStage());
    }

    public StartingStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new EquipmentStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return new InventoryStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new StartingInvSubStage(this.getPlayerId()));
        this.subStages.add(new DeathSubStage(this.getPlayerId()));
        this.subStages.add(new ChatSubStage(this.getPlayerId()));
        this.subStages.add(new EventSubStage(this.getPlayerId()));
    }

    public class EventSubStage
    extends TutorialStage.TutorialSubStage {
        public EventSubStage(long playerId) {
            super(StartingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Starting Out", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nThe event window will show you any relevant information to the action you're doing, as well as showing you a progress bar underneath with the current action you're completing and the time remaining.\r\n\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)3, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class ChatSubStage
    extends TutorialStage.TutorialSubStage {
        public ChatSubStage(long playerId) {
            super(StartingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Starting Out", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nThis is the chat window where you can talk to other players in the game. You can use the CA-Help chat to ask questions about game mechanics and how to do something.\r\n\r\nLocal chat will send messages to any players nearby you in the world and your kingdom chat (e.g. Freedom) will send messages to everyone on your current server in that kingdom.\r\n\r\nAny chat starting with GL will send messages to all connected servers instead of just your server.\r\n\r\nThis window also contains the Trade tab, where you can find other players selling or buying goods.\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)1, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class DeathSubStage
    extends TutorialStage.TutorialSubStage {
        public DeathSubStage(long playerId) {
            super(StartingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Starting Out", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nUpon death, your body will become a corpse at the location you died, and will contain any items you were carrying that aren't starter items. If you want to get those items back, you'll need to travel back to your corpse and retrieve your items.\r\n\r\nAs a part of your starting items you will have a tent. Dropping this tent on the ground will let you respawn there when you die. As well as your tent location, you'll be able to choose to spawn at a village if you are a member of one, any allied villages, or a starter town.\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)20, false);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class StartingInvSubStage
    extends TutorialStage.TutorialSubStage {
        public StartingInvSubStage(long playerId) {
            super(StartingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Starting Out", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nInside your inventory you will have some items to help you get started in Wurm. This includes some basic armour, weapons and tools.\r\n\r\nThese starter items cannot be improved, and will stay with you through death.\r\n\r\nIn order to get better versions of these items, you will need to create and improve them yourself or have another player help you.\r\n\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)20, false);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }
}

