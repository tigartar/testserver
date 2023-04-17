/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import java.util.HashSet;
import java.util.Properties;

public final class ReimbursementQuestion
extends Question {
    private String[] nameArr = new String[0];

    public ReimbursementQuestion(Creature aResponder, long aTarget) {
        super(aResponder, "Reimbursements", "These are your available reimbursements:", 50, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        String key = "";
        String value = "";
        for (int x = 0; x < this.nameArr.length; ++x) {
            int days = 0;
            int trinkets = 0;
            int silver = 0;
            boolean boktitle = false;
            boolean mbok = false;
            key = "silver" + this.nameArr[x];
            value = answers.getProperty(key);
            if (value != null) {
                try {
                    silver = Integer.parseInt(value);
                }
                catch (Exception ex) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of silver for " + this.nameArr[x]);
                    return;
                }
            }
            if ((value = answers.getProperty(key = "days" + this.nameArr[x])) != null) {
                try {
                    days = Integer.parseInt(value);
                }
                catch (Exception ex) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of days for " + this.nameArr[x]);
                    return;
                }
            }
            if ((value = answers.getProperty(key = "trinket" + this.nameArr[x])) != null) {
                try {
                    trinkets = Integer.parseInt(value);
                }
                catch (Exception ex) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("Wrong amount of trinkets for " + this.nameArr[x]);
                    return;
                }
            }
            if ((value = answers.getProperty(key = "mbok" + this.nameArr[x])) != null) {
                try {
                    boktitle = Boolean.parseBoolean(value);
                    if (boktitle) {
                        mbok = true;
                    }
                }
                catch (Exception ex) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the MBoK/Title answer for " + this.nameArr[x]);
                    return;
                }
            }
            if (!boktitle && (value = answers.getProperty(key = "bok" + this.nameArr[x])) != null) {
                try {
                    boktitle = Boolean.parseBoolean(value);
                }
                catch (Exception ex) {
                    this.getResponder().getCommunicator().sendAlertServerMessage("Unable to parse the BoK/Title answer for " + this.nameArr[x]);
                    return;
                }
            }
            if (days <= 0 && trinkets <= 0 && silver <= 0 && !boktitle) continue;
            if (days < 0 || trinkets < 0 || silver < 0) {
                this.getResponder().getCommunicator().sendAlertServerMessage("Less than 0 value entered for " + this.nameArr[x]);
                continue;
            }
            LoginServerWebConnection lsw = new LoginServerWebConnection();
            this.getResponder().getCommunicator().sendNormalServerMessage(lsw.withDraw((Player)this.getResponder(), this.nameArr[x], ((Player)this.getResponder()).getSaveFile().emailAddress, trinkets, silver, boktitle, mbok, days));
        }
    }

    @Override
    public void sendQuestion() {
        LoginServerWebConnection lsw = new LoginServerWebConnection();
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        String s = lsw.getReimburseInfo((Player)this.getResponder());
        if (s.equals("text{text='You have no reimbursements pending.'}")) {
            ((Player)this.getResponder()).getSaveFile().setHasNoReimbursementLeft(true);
        } else {
            String ttext = s;
            String newName = "";
            HashSet<String> names = new HashSet<String>();
            boolean keepGoing = true;
            while (keepGoing) {
                newName = this.getNextName(ttext);
                if (newName.equals("")) {
                    keepGoing = false;
                    continue;
                }
                names.add(newName);
                ttext = ttext.substring(ttext.indexOf(" - '}") + 5, ttext.length());
            }
            this.nameArr = names.toArray(new String[names.size()]);
        }
        buf.append(s);
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(400, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private String getNextName(String ttext) {
        int place = ttext.indexOf("Name=");
        if (place > 0) {
            return ttext.substring(place + 5, ttext.indexOf(" - '}"));
        }
        return "";
    }
}

