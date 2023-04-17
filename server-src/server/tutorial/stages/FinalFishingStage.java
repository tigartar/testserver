/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.RodFishingStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class FinalFishingStage
extends TutorialStage {
    private static final short WINDOW_ID = 2200;

    public FinalFishingStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return null;
    }

    @Override
    public TutorialStage getLastStage() {
        return new RodFishingStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new FishingEndSubStage(this.getPlayerId()));
    }

    @Override
    public short getWindowId() {
        return 2200;
    }

    public class FishingEndSubStage
    extends TutorialStage.TutorialSubStage {
        public FishingEndSubStage(long playerId) {
            super(FinalFishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Final Tips", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nAs well as the mentioned equipment, time and location preferences, each fish type has a base difficulty  that may need a higher Fishing skill to reliably catch.\r\n\r\nUsing the Lore action on a boat or water tile with a pole/rod/spear/net active may give you some extra information on what you are likely to catch at that time and location with the active item.\r\n\r\nCreating and carrying a supplied tacklebox will allow for automatic restocking of bait, floats and hooks when they become useless from too much damage due to fishing.\r\n\r\nNo matter where you are fishing you will have a small chance of catching clams which may contain some special items when opened with a knife.\r\n\r\n", null, null, null, 300, 400), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "End Tutorial", 80, 20, true).addText("", null, null, null, 35, 0).addButton("restart", "Restart Tutorial", " ", "Are you sure you want to restart the tutorial from the beginning?", null, false, 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }
}

