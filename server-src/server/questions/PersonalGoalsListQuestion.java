/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.questions.Question;
import java.util.HashSet;
import java.util.Properties;

public class PersonalGoalsListQuestion
extends Question {
    public PersonalGoalsListQuestion(Creature aResponder, long aTarget) {
        super(aResponder, "Personal Goals", "Personal Goals", 152, aTarget);
    }

    @Override
    public void answer(Properties answers) {
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() >= 4) {
            Achievements achs = Achievements.getAchievementObject(this.getTarget());
            HashSet goals = (HashSet)Achievements.getPersonalGoals(this.getTarget(), false);
            HashSet oldGoals = (HashSet)Achievements.getOldPersonalGoals(this.getTarget());
            buf.append("text{text='Current Personal Goals for WurmId " + this.getTarget() + "'}");
            buf.append("text{text=''}");
            buf.append("table{rows='" + goals.size() + "';cols='2';");
            for (AchievementTemplate t : goals) {
                boolean done = false;
                if (achs.getAchievement(t.getNumber()) != null) {
                    done = true;
                }
                buf.append("label{color=\"" + (done ? "20,255,20" : "200,200,200") + "\";text=\"" + t.getName() + "\"};");
                buf.append("label{color=\"" + (done ? "20,255,20" : "200,200,200") + "\";text=\"" + t.getRequirement() + "\"}");
            }
            buf.append("}");
            buf.append("text{text=''}");
            buf.append("text{text='Pre June 5 2018 Personal Goals for WurmId " + this.getTarget() + "'}");
            buf.append("text{text=''}");
            buf.append("table{rows='" + oldGoals.size() + "';cols='2';");
            for (AchievementTemplate t : oldGoals) {
                buf.append("label{text=\"" + t.getName() + "\"};");
                buf.append("label{text=\"" + t.getRequirement() + "\"}");
            }
            buf.append("}");
            buf.append("}};null;null;}");
            this.getResponder().getCommunicator().sendBml(300, 600, true, true, buf.toString(), 200, 200, 200, this.title);
        }
    }
}

