/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.server.villages.RecruitmentAd;
import com.wurmonline.server.villages.RecruitmentAds;
import java.util.Properties;

public class GmVillageAdInterface
extends Question {
    public GmVillageAdInterface(Creature aResponder, long aTarget) {
        super(aResponder, "Manage Village Recruitment Ads", "", 101, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseGmVillageAdQuestion(this);
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        RecruitmentAd[] ads = RecruitmentAds.getAllRecruitmentAds();
        buf.append("table{rows=\"" + ads.length + 1 + "\";cols=\"3\";label{text=\"Remove\"};label{text=\"Village\"};label{text=\"Contact\"};");
        for (int i = 0; i < ads.length; ++i) {
            buf.append("checkbox{id=\"" + ads[i].getVillageId() + "remove\";selected=\"false\";text=\" \"}");
            buf.append("label{text=\"" + ads[i].getVillageName() + "\"};");
            buf.append("label{text=\"" + ads[i].getContactName() + "\"};");
        }
        buf.append("}");
        buf.append(this.createAnswerButton2("Remove"));
        this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

