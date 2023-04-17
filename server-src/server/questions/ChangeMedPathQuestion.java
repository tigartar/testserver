/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.questions.Question;
import java.util.Properties;
import java.util.logging.Level;

public class ChangeMedPathQuestion
extends Question {
    private final Cultist cultist;

    public ChangeMedPathQuestion(Creature aResponder, Cultist cultist, Item target) {
        super(aResponder, "Meditation Path", aResponder.getName() + " Meditation Path", 722, target.getWurmId());
        this.cultist = cultist;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 722) {
            if (this.cultist.getPath() == 4) {
                String prop = answers.getProperty("newcult");
                if (prop != null) {
                    byte cultId = Byte.parseByte(prop);
                    this.cultist.setPath(cultId);
                    this.getResponder().getCommunicator().sendNormalServerMessage("You are now " + this.cultist.getCultistTitle());
                    this.getResponder().refreshVisible();
                    this.getResponder().getCommunicator().sendOwnTitles();
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Your path was not changed.");
                }
            } else {
                this.getResponder().getCommunicator().sendNormalServerMessage("You are not currently on the correct path to be able to change.");
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.cultist.getPath() == 4) {
            buf.append("text{type=\"bold\";text=\"Choose which path you want to change to. This cannot be undone. This change is only available once, and only from the path of insanity.\"}");
            int cId = this.cultist.getPath();
            for (int i = 1; i < 6; ++i) {
                if (i == cId) continue;
                buf.append("radio{ group='newcult'; id='" + i + "'; text='" + Cults.getPathNameFor((byte)i) + "'}");
            }
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(350, 300, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

