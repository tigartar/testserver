/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public final class VillageHistoryQuestion
extends Question {
    public VillageHistoryQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 40, aTarget);
    }

    @Override
    public void answer(Properties answers) {
    }

    @Override
    public void sendQuestion() {
        Village citizenVillage = this.getResponder().getCitizenVillage();
        StringBuilder sb = new StringBuilder();
        sb.append(this.getBmlHeader());
        if (citizenVillage != null) {
            sb.append("header{text=\"Latest events in " + citizenVillage.getName() + ":\"}");
            String[] list = citizenVillage.getHistoryAsStrings(50);
            for (int x = 0; x < list.length; ++x) {
                sb.append("text{text=\"" + list[x] + "\"}");
            }
        } else {
            sb.append("text{text='You are not citizen of a village.'}");
        }
        sb.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(500, 300, true, true, sb.toString(), 200, 200, 200, this.title);
    }
}

