/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.questions.Question;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GmSetMedPath
extends Question {
    private static final Logger logger = Logger.getLogger(GmSetMedPath.class.getName());
    private final Creature creature;

    public GmSetMedPath(Creature aResponder, Creature aTarget) {
        super(aResponder, "Meditation Path", aTarget.getName() + " Meditation Path", 132, aTarget.getWurmId());
        this.creature = aTarget;
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 132 && this.getResponder().getPower() >= 4) {
            try {
                Cultist c;
                String prop = aAnswer.getProperty("newcult");
                if (prop != null) {
                    this.getResponder().getLogger().log(Level.INFO, "Setting meditation path and level of " + this.creature.getName());
                    byte cultId = Byte.parseByte(prop);
                    c = this.creature.getCultist();
                    if (c == null && cultId > 0) {
                        c = new Cultist(this.creature.getWurmId(), cultId);
                    } else if (c != null) {
                        if (cultId > 0) {
                            c.setPath(cultId);
                        } else {
                            try {
                                c.deleteCultist();
                                if (this.creature != this.getResponder()) {
                                    this.creature.getCommunicator().sendNormalServerMessage("You are no longer following any path.");
                                }
                                this.getResponder().getCommunicator().sendNormalServerMessage(this.creature.getName() + " is no longer following any path.");
                                this.creature.refreshVisible();
                                this.creature.getCommunicator().sendOwnTitles();
                                return;
                            }
                            catch (IOException iOException) {}
                        }
                    } else {
                        return;
                    }
                }
                if ((prop = aAnswer.getProperty("cultlevel")) != null) {
                    byte cultLvl = Byte.parseByte(prop);
                    c = this.creature.getCultist();
                    if (c != null) {
                        c.setLevel(cultLvl);
                    }
                }
                if (this.creature != this.getResponder()) {
                    this.creature.getCommunicator().sendNormalServerMessage("You are now " + this.creature.getCultist().getCultistTitle());
                }
                this.getResponder().getCommunicator().sendNormalServerMessage(this.creature.getName() + " is now " + this.creature.getCultist().getCultistTitle());
                this.creature.refreshVisible();
                this.creature.getCommunicator().sendOwnTitles();
            }
            catch (NumberFormatException nsf) {
                this.getResponder().getCommunicator().sendNormalServerMessage("Unable to set meditation path and level with those answers.");
                return;
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() >= 4) {
            buf.append("text{type=\"bold\";text=\"Choose path to set the target to:\"}");
            Cultist c = this.creature.getCultist();
            int cId = c == null ? 0 : c.getPath();
            boolean isSelected = false;
            for (int i = 0; i < 6; ++i) {
                isSelected = i == cId;
                buf.append("radio{ group='newcult'; id='" + i + "'; text='" + Cults.getPathNameFor((byte)i) + "'" + (isSelected ? ";selected='true'" : "") + "}");
            }
            byte cLvl = c == null ? (byte)0 : c.getLevel();
            buf.append("harray{input{id='cultlevel'; maxchars='2'; text='" + cLvl + "'}label{text='Path Level'}}");
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(250, 250, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

