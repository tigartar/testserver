/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.tutorial.stages;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.tutorial.PlayerTutorial;
import com.wurmonline.server.tutorial.TutorialStage;
import com.wurmonline.server.tutorial.stages.StartingStage;
import com.wurmonline.server.tutorial.stages.WorldStage;
import com.wurmonline.server.utils.BMLBuilder;
import java.awt.Color;

public class EquipmentStage
extends TutorialStage {
    private static final short WINDOW_ID = 600;

    @Override
    public short getWindowId() {
        return (short)(600 + this.getCurrentSubStage());
    }

    public EquipmentStage(long playerId) {
        super(playerId);
    }

    @Override
    public TutorialStage getNextStage() {
        return new WorldStage(this.getPlayerId());
    }

    @Override
    public TutorialStage getLastStage() {
        return new StartingStage(this.getPlayerId());
    }

    @Override
    public void buildSubStages() {
        this.subStages.add(new ActivateSubStage(this.getPlayerId()));
        this.subStages.add(new CharacterSubStage(this.getPlayerId()));
        this.subStages.add(new EquipSubStage(this.getPlayerId()));
    }

    public class ToolbeltSubStage
    extends TutorialStage.TutorialSubStage {
        public ToolbeltSubStage(long playerId) {
            super(playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Activating & Equipping", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nIf you have a toolbelt equipped, it will show up on screen now. You can use your toolbelt as a quick access to any items you have in your inventory or equipped on your body. Simply drag the item to any toolbelt slot to add it to your toolbelt.\r\n\r\nOnce on your toolbelt, a single click on an item icon on your toolbelt will activate it allowing you to use it in actions. You can also bind keys to your toolbelt slots for quick access without having to click on the icons.\r\n\r\nThe amount of slots shown for your toolbelt depends on the quality of your toolbelt, with one additional slot per 10QL.Clicking the arrows on the toolbelt will switch your toolbelt presets which you can save and load by right clicking on the toolbelt at any time.\r\n", null, null, null, 300, 400).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)7, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class EquipSubStage
    extends TutorialStage.TutorialSubStage {
        public EquipSubStage(long playerId) {
            super(playerId);
            this.setNextTrigger(PlayerTutorial.PlayerTrigger.EQUIPPED_ITEM);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Activating & Equipping", Color.LIGHT_GRAY)), null, BMLBuilder.createScrollPanelNode(true, false).addString(BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nClick and drag any equippable items such as armour pieces to your character in this window to have it equip.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.equipping", 300, 150).addText("\r\nTo unequip an item, drag it from the slot it is equipped in to your character in this window again, and it will move the item back to your inventory.\r\n\r\nYou can also right click an item in your inventory and select the Equip action.\r\n\r\nEquip an item now to continue.", null, null, null, 300, 400).addText("").toString()), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Waiting...", 80, 20, false).maybeAddSkipButton()));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendOpenWindow((short)20, false);
                p.getCommunicator().sendOpenWindow((short)26, false);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class CharacterSubStage
    extends TutorialStage.TutorialSubStage {
        public CharacterSubStage(long playerId) {
            super(playerId);
            this.setNextTrigger(PlayerTutorial.PlayerTrigger.ENABLED_CHARACTER);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Activating & Equipping", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nEquipping items is different than activating them, and is generally only used for Armour and Weapons.\r\n\r\nYou can still activate items that are equipped, but items equipped in your hands are not considered activated by default.\r\n\r\nPress [$bind:TOGGLE_CHARACTER$] or click the character button to open the Character Window.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.character", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Waiting...", 80, 20, false).maybeAddSkipButton()));
            this.bmlString = builder.toString();
        }

        @Override
        public void triggerOnView() {
            try {
                Player p = Players.getInstance().getPlayer(this.getPlayerId());
                p.getCommunicator().sendToggleQuickbarBtn((short)2009, true);
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
    }

    public class ActivateSubStage
    extends TutorialStage.TutorialSubStage {
        public ActivateSubStage(long playerId) {
            super(playerId);
        }

        @Override
        protected void buildBMLString() {
            BMLBuilder builder = BMLBuilder.createBMLBorderPanel(BMLBuilder.createCenteredNode(BMLBuilder.createVertArrayNode(false).addText("").addHeader("Activating & Equipping", Color.LIGHT_GRAY)), null, BMLBuilder.createVertArrayNode(false).addPassthrough("tutorialid", Long.toString(this.getPlayerId())).addText("\r\nMost actions that you can complete in Wurm require an active item in order to do the action. This may mean you need to activate a shovel to dig some dirt, or a hatchet in order to chop down a tree.\r\n\r\nTo activate an item, simply double click it while it is inside your inventory. The item will turn green, and will show at the bottom of your inventory as your currently active item.\r\n\r\n", null, null, null, 300, 400).addImage("image.tutorial.activate", 300, 150).addText(""), null, BMLBuilder.createLeftAlignedNode(BMLBuilder.createHorizArrayNode(false).addButton("back", "Back", 80, 20, true).addText("", null, null, null, 35, 0).addButton("next", "Next", 80, 20, true)));
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

