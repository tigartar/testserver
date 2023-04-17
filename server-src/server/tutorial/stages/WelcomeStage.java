/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.ViewStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class WelcomeStage
extends TutorialStage {
    private static final short WINDOW_ID = 100;

    @Override
    public short getWindowId() {
        return (short)(100 + this.getCurrentSubStage());
    }

    public WelcomeStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new ViewStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return null;
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new WelcomeSubStage(this.getPlayerId()));
    }

    public class WelcomeSubStage
    extends TutorialStage.TutorialSubStage {
        public WelcomeSubStage(long playerId) {
            super(WelcomeStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Welcome to Wurm!", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nThis tutorial will show you how to get started in Wurm.\r\n\r\nClick [Next] below to get started.", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, false).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true).maybeAddSkipButton(Players.getInstance().getPlayerOrNull(this.getPlayerId()), true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendCloseWindow((short)9);
                p.getCommunicator().sendCloseWindow((short)5);
                p.getCommunicator().sendCloseWindow((short)1);
                p.getCommunicator().sendCloseWindow((short)3);
                p.getCommunicator().sendCloseWindow((short)16);
                p.getCommunicator().sendCloseWindow((short)20);
                p.getCommunicator().sendCloseWindow((short)26);
                p.getCommunicator().sendCloseWindow((short)11);
                p.getCommunicator().sendCloseWindow((short)4);
                p.getCommunicator().sendCloseWindow((short)41);
                p.getCommunicator().sendCloseWindow((short)6);
                p.getCommunicator().sendCloseWindow((short)7);
                p.getCommunicator().sendCloseWindow((short)2);
                p.getCommunicator().sendCloseWindow((short)12);
                p.getCommunicator().sendCloseWindow((short)13);
                p.getCommunicator().sendToggleAllQuickbarBtns(false);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }
}

