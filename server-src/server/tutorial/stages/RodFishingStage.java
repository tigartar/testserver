/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.FinalFishingStage;
import com.wurmonline.server.tutorial.stages.FishingStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class RodFishingStage
extends TutorialStage {
    private static final short WINDOW_ID = 2100;

    public RodFishingStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new FinalFishingStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return new FishingStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new RodStartSubStage(this.getPlayerId()));
        this.subStages.add(new RodSetupSubStage(this.getPlayerId()));
        this.subStages.add(new RodActionSubStage(this.getPlayerId()));
        this.subStages.add(new RodHookingSubStage(this.getPlayerId()));
    }

    @Override
    public short getWindowId() {
        return 2100;
    }

    public class RodHookingSubStage
    extends TutorialStage.TutorialSubStage {
        public RodHookingSubStage(long playerId) {
            super(RodFishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Pole/Rod Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nWhen a fish nibbles on your line you'll see the float dip in the water and your rod will twitch. Left click anywhere on the screen to hook the fish and start reeling it in.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.reeling", 300, 150).addText("\r\nThis will begin a battle between you and the fish where you'll either reel the fish in slightly or it will manage to pull slightly away from you. The winner of this battle will depend on your fishing skill and the equipment you are using to catch the fish.\r\n\r\nIf successful, you will catch the fish and it will be put into your inventory, otherwise it may jump the hook and get away.\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class RodActionSubStage
    extends TutorialStage.TutorialSubStage {
        public RodActionSubStage(long playerId) {
            super(RodFishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Pole/Rod Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nWith your rod or pole active, you can select the Fish action on a water tile or a boat you are embarked on which will start the fishing action. A target reticle will appear at your mouse position for you to select where you cast your line. Left click to cast your line or right click to cancel the action.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.casting", 300, 150).addText("\r\nOnce casted you'll need to wait for a fish to come along and nibble on your line.\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class RodSetupSubStage
    extends TutorialStage.TutorialSubStage {
        public RodSetupSubStage(long playerId) {
            super(RodFishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Pole/Rod Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nOnce you have the items necessary for the fish you want to catch, insert them into the indicated empty slots to build up your pole or rod. A completed pole or rod should have all non-optional empty slots filled.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.rodinventory", 300, 150).addText("\r\nKeeping an eye on the damage of these items during fishing is important as long battles with fish can cause items be take enough damage to break which may destroy all internal items of the part that broke.\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class RodStartSubStage
    extends TutorialStage.TutorialSubStage {
        public RodStartSubStage(long playerId) {
            super(RodFishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Pole/Rod Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nPole and Rod fishing is slightly more involved than net and spear fishing and will require a few more items to get going for the fish you want to catch. For pole fishing you will need a fishing pole, line, fishing hook, float, and optional bait. For rod fishing you will need all of that as well as a fishing reel.\r\n\r\nWhether you want to be pole fishing or rod fishing will depend on the fish you want to catch as there is no one-fits-all solution that all fish will like. Using the wrong equipment when seeking out some fish will make catching them become much more difficult than using the correct equipment.\r\n\r\nCheck https://www.wurmpedia.com/index.php/Fish for the equipment that you are going to need for the fish you want to catch.\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }
}

