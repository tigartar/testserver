/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import java.util.Date;
import java.util.Properties;

public class ChallengeInfoQuestion
extends Question {
    public ChallengeInfoQuestion(Creature aResponder) {
        super(aResponder, "Challenge Server Notification", "Server reset in " + Server.getTimeFor(Servers.localServer.getChallengeEnds() - System.currentTimeMillis()) + "!", 117, aResponder.getWurmId());
    }

    @Override
    public void answer(Properties answers) {
        String val = answers.getProperty("okaycb");
        if (val != null && val.equals("true")) {
            this.getResponder().setFlag(27, true);
        }
        ((Player)this.getResponder()).setQuestion(null);
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The challenge server resets after a certain time or when a set goal is achieved.\"}");
        buf.append("text{text=\"This server will shut down around " + new Date(Servers.localServer.getChallengeEnds()) + ".\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"This means that all your items and skills will be lost. What you do keep is your money and titles. Also, please note that on this server settlements are harder to defend than normal and it usually requires some manpower. This means you may want to avoid deeding on your own.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"This is a competitive server and all investments are considered to be made in order to further your chances to win. Hence, when the server resets, settlements are disbanded and the mayor will be refunded what is left in upkeep but the cost of the tiles is a sunk cost.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Also please note that the nature of this server means that GM presence is very limited, so please keep our no reimbursement policy in mind if you lose your stuff for whatever reason.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=''}");
        buf.append("checkbox{id='okaycb';selected='false';text='I have understood this message and do not need to see it ever again'}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

