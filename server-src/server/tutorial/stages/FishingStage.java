/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.RodFishingStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class FishingStage
extends TutorialStage {
    private static final short WINDOW_ID = 2000;

    public FishingStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new RodFishingStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return null;
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new FishingStartSubStage(this.getPlayerId()));
        this.subStages.add(new FishModifiersSubStage(this.getPlayerId()));
        this.subStages.add(new NettingSubStage(this.getPlayerId()));
        this.subStages.add(new SpearingSubStage(this.getPlayerId()));
    }

    @Override
    public short getWindowId() {
        return 2000;
    }

    public class SpearingSubStage
    extends TutorialStage.TutorialSubStage {
        public SpearingSubStage(long playerId) {
            super(FishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Spear Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nSpear fishing is also an action completed in shallow waters using either a spear or long spear and can be used to catch small fish as well as the odd larger fish.\r\n\r\nActivate a spear and choose the Fish action on a shallow water tile to start spear fishing. A target reticle will appear at your mouse position and can be used to line up your spear when a fish swims past. Wait for a fish to get close to you then left click on it to throw your spear at it and catch it.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.spearing", 300, 150).addText("\r\nRight clicking while the targeting reticle is active will cancel the action and you will put your spear away.\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class NettingSubStage
    extends TutorialStage.TutorialSubStage {
        public NettingSubStage(long playerId) {
            super(FishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Net Fishing", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nNet fishing can be done in shallow waters using a fishing net and is a good way to catch small fish, including those used as bait in rod/pole fishing like roaches and minnow.\r\n\r\nTo start net fishing, simply activate your fishing net and choose the Fish action on a shallow water tile. You will start catching fish in your net which will need to be emptied before starting your next net fishing action.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.netting", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class FishModifiersSubStage
    extends TutorialStage.TutorialSubStage {
        public FishModifiersSubStage(long playerId) {
            super(FishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Fishing Intro", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nThe first stage of fishing is deciding what you want to catch. Your chances of catching each fish type can change based on time of day, where you are fishing, depth of the water, float, bait, and fishing rod types. Aligning all of these to the fish you want will give you a much better chance of catching good quality fish.\r\n\r\nAs an example, your best chance at catching pike is on a lake in the evening or night with water up to 10m deep using a basic or fine rod with moss for a float and flies as bait.\r\n\r\nYou will still be able to catch pike if you do not meet all of the above conditions, but you may come across them less often and have a lower chance to reel them in when you do find them.\r\n\r\nFull info about all fish types and chance modifiers can be found here: https://www.wurmpedia.com/index.php/Fish\r\n\r\n", null, null, null, 300, 400).toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }

    public class FishingStartSubStage
    extends TutorialStage.TutorialSubStage {
        public FishingStartSubStage(long playerId) {
            super(FishingStage.this, playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Fishing Intro", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nIn this tutorial you will learn about the different kinds of fishing in Wurm, what you need and how to do it, and what you can expect to catch.\r\n\r\nThere are currently 3 main types of fishing you can find in Wurm: Net Fishing, Spear Fishing and Rod/Pole Fishing. Each have their own sets of items and actions involved and may catch you different kinds of fish.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.fishing.types", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, false).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }
    }
}

