/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.structures.DbDoor;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.structures.WallEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GMBuildAllWallsQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(GMBuildAllWallsQuestion.class.getName());
    private final Structure targetStructure;
    private final Item buildItem;
    private final List<WallEnum> wallList;

    public GMBuildAllWallsQuestion(Creature aResponder, Structure aTarget) {
        super(aResponder, "Build all unfinished walls", "What type of walls?", 143, aTarget.getWurmId());
        this.targetStructure = aTarget;
        this.buildItem = aResponder.getCarriedItem(176);
        this.wallList = WallEnum.getWallsByTool(this.getResponder(), this.buildItem, false, false);
        this.wallList.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
    }

    @Override
    public void answer(Properties aAnswer) {
        block9: {
            this.setAnswer(aAnswer);
            if (this.type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (this.type == 143 && this.getResponder().getPower() >= 4) {
                try {
                    String prop = aAnswer.getProperty("walltype");
                    if (prop == null) break block9;
                    short wallId = Short.parseShort(prop);
                    if (wallId < this.wallList.size() && wallId >= 0) {
                        this.getResponder().getLogger().log(Level.INFO, "Building all unfinished walls of structure with ID=" + this.targetStructure.getWurmId() + "to type " + this.wallList.get(wallId).getName());
                        for (Wall w : this.targetStructure.getWalls()) {
                            if (!w.isWallPlan()) continue;
                            w.setQualityLevel(80.0f);
                            w.setMaterial(this.wallList.get(wallId).getMaterial());
                            w.setType(this.wallList.get(wallId).getType());
                            w.setState(StructureStateEnum.FINISHED);
                            w.setDamage(0.0f);
                            if (w.isDoor()) {
                                DbDoor door = new DbDoor(w);
                                door.setStructureId(this.targetStructure.getOwnerId());
                                this.targetStructure.addDoor(door);
                                try {
                                    ((Door)door).save();
                                    door.addToTiles();
                                }
                                catch (IOException e) {
                                    logger.warning("Failed to save door! " + e);
                                    this.getResponder().getCommunicator().sendAlertServerMessage("ERROR: IOException. Aborting!");
                                    return;
                                }
                            }
                            w.getTile().updateWall(w);
                        }
                        this.targetStructure.updateStructureFinishFlag();
                        break block9;
                    }
                    logger.fine("WallID was larger than WallList.size(), parsed value = " + wallId);
                    this.getResponder().getCommunicator().sendAlertServerMessage("ERROR: Something went wrong with parsing the input. Aborting.");
                    return;
                }
                catch (NumberFormatException nsf) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Unable to set wall types with that answer");
                    return;
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        if (this.buildItem == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("You need to have at least one Ebony Wand in your inventory for this to work.");
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() >= 4) {
            buf.append("text{type=\"bold\";text=\"Choose the type of wall all wall plans will be turned into:\"}");
            boolean isSelected = false;
            buf.append("harray{label{text='Tile type'}dropdown{id='walltype';options=\"");
            for (int i = 0; i < this.wallList.size(); ++i) {
                buf.append(this.wallList.get(i).getName() + ",");
            }
            buf.append("\"}}");
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(250, 150, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

