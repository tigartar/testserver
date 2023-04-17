/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public final class TitleQuestion
extends Question {
    private final List<Titles.Title> titlelist = new LinkedList<Titles.Title>();

    public TitleQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 39, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseTitleQuestion(this);
    }

    @Override
    public void sendQuestion() {
        King king;
        String meditation;
        int defaultTitle;
        StringBuilder sb = new StringBuilder(this.getBmlHeader());
        boolean isMale = ((Player)this.getResponder()).isNotFemale();
        Titles.Title[] titles = ((Player)this.getResponder()).getTitles();
        Arrays.sort(titles, new Comparator<Titles.Title>(){

            @Override
            public int compare(Titles.Title t1, Titles.Title t2) {
                return t1.getName(isMale).compareTo(t2.getName(isMale));
            }
        });
        String suff = "";
        String pre = "";
        if (!this.getResponder().hasFlag(24)) {
            pre = this.getResponder().getAbilityTitle();
        }
        if (this.getResponder().getCultist() != null && !this.getResponder().hasFlag(25)) {
            suff = suff + " " + this.getResponder().getCultist().getCultistTitleShort();
        }
        Titles.Title lTempTitle = this.getResponder().getTitle();
        if (this.getResponder().isKing()) {
            suff = suff + " [" + King.getRulerTitle(this.getResponder().getSex() == 0, this.getResponder().getKingdomId()) + "]";
        }
        if (lTempTitle != null) {
            if (lTempTitle.isRoyalTitle()) {
                if (this.getResponder().getAppointments() != 0L || this.getResponder().isAppointed()) {
                    suff = suff + " [" + this.getResponder().getKingdomTitle() + "]";
                }
            } else {
                suff = suff + " [" + lTempTitle.getName(this.getResponder().isNotFemale()) + "]";
            }
        }
        if (this.getResponder().isChampion() && this.getResponder().getDeity() != null) {
            suff = suff + " [Champion of " + this.getResponder().getDeity().name + "]";
        }
        String playerName = pre + StringUtilities.raiseFirstLetterOnly(this.getResponder().getName()) + suff;
        sb.append("text{text=\"You are currently known as: " + playerName + "\"}");
        sb.append("text{text=\"\"}");
        Titles.Title currentTitle = ((Player)this.getResponder()).getTitle();
        int totalTitles = 0;
        if (titles.length == 0) {
            if (this.getResponder().getAppointments() != 0L || this.getResponder().isAppointed()) {
                defaultTitle = 0;
                sb.append("harray{text{text=\"Title: \"};dropdown{id=\"TITLE\";options=\"None");
                sb.append(",");
                sb.append(Titles.Title.Kingdomtitle.getName(this.getResponder().isNotFemale()));
                this.titlelist.add(Titles.Title.Kingdomtitle);
                if (currentTitle != null && currentTitle.isRoyalTitle()) {
                    defaultTitle = 1;
                }
                sb.append("\";default=\"" + defaultTitle + "\"}}");
                ++totalTitles;
            } else {
                sb.append("text{text=\"You have no titles to select from.\"}");
            }
        } else {
            defaultTitle = 0;
            sb.append("harray{text{text=\"Title: \"};dropdown{id=\"TITLE\";options=\"None");
            for (int x = 0; x < titles.length; ++x) {
                sb.append(",");
                sb.append(titles[x].getName(this.getResponder().isNotFemale()));
                if (currentTitle != null && titles[x].id == currentTitle.id) {
                    defaultTitle = x + 1;
                }
                this.titlelist.add(titles[x]);
                ++totalTitles;
            }
            if (this.getResponder().getAppointments() != 0L || this.getResponder().isAppointed()) {
                sb.append(",");
                sb.append(Titles.Title.Kingdomtitle.getName(this.getResponder().isNotFemale()));
                this.titlelist.add(Titles.Title.Kingdomtitle);
                if (currentTitle != null && currentTitle.isRoyalTitle()) {
                    defaultTitle = titles.length + 1;
                }
                ++totalTitles;
            }
            sb.append("\";default=\"" + defaultTitle + "\"}}");
            sb.append("text{text=\"\"}");
            sb.append("text{text=\"You have a total of " + totalTitles + " titles.\"}");
            sb.append("text{text=\"\"}");
            sb.append("text{type=\"italic\";text=\"Note: Armour smiths that use their title gets faster armour improvement rate.\"}");
        }
        String occultist = this.getResponder().getAbilityTitle();
        String string = meditation = this.getResponder().getCultist() != null ? Cults.getNameForLevel(this.getResponder().getCultist().getPath(), this.getResponder().getCultist().getLevel()) : "";
        if (occultist.length() > 0 || meditation.length() > 0) {
            sb.append("text{type=\"bold\";text=\"Select which titles to hide (if any)\"}");
            if (occultist.length() > 0) {
                sb.append("checkbox{id=\"hideoccultist\";text=\"" + occultist + "(Occultist)\";selected=\"" + this.getResponder().hasFlag(24) + "\"}");
            }
            if (meditation.length() > 0) {
                sb.append("checkbox{id=\"hidemeditation\";text=\"" + meditation + " (Meditation)\";selected=\"" + this.getResponder().hasFlag(25) + "\"}");
            }
            sb.append("text{text=\"\"}");
        }
        if (Servers.isThisAPvpServer() && (king = King.getKing(this.getResponder().getKingdomId())) != null && (this.getResponder().getAppointments() != 0L || this.getResponder().isAppointed())) {
            sb.append("text{type=\"bold\";text=\"Select which kingdom office to remove (if any)\"}");
            Appointments a = Appointments.getAppointments(king.era);
            for (int x = 0; x < a.officials.length; ++x) {
                int oId = x + 1500;
                Appointment o = a.getAppointment(oId);
                if (a.officials[x] != this.getResponder().getWurmId()) continue;
                sb.append("checkbox{id=\"office" + oId + "\";text=\"" + o.getNameForGender(this.getResponder().getSex()) + " (Office)\";}");
            }
        }
        sb.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(500, 300, true, true, sb.toString(), 200, 200, 200, this.title);
    }

    Titles.Title getTitle(int aPosition) {
        return this.titlelist.get(aPosition);
    }
}

