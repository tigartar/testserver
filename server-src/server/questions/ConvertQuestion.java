/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import java.util.Properties;

public final class ConvertQuestion
extends Question {
    private final Item holyItem;
    private float skillcounter = 0.0f;

    public ConvertQuestion(Creature aResponder, String aTitle, String aQuestion, long aAsker, Item aHolyItem) {
        super(aResponder, aTitle, aQuestion, 28, aAsker);
        this.holyItem = aHolyItem;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseConvertQuestion(this);
    }

    @Override
    public void sendQuestion() {
        try {
            Creature asker = Server.getInstance().getCreature(this.target);
            Deity deity = asker.getDeity();
            StringBuilder buf = new StringBuilder();
            buf.append(this.getBmlHeader());
            if (!QuestionParser.doesKingdomTemplateAcceptDeity(this.getResponder().getKingdomTemplateId(), deity)) {
                buf.append("text{text='" + this.getResponder().getKingdomName() + " would never accept a follower of " + deity.name + ".'}");
                buf.append("text{text=''}");
            } else if (deity != this.getResponder().getDeity()) {
                buf.append("text{text='" + asker.getName() + " asks if you wish to become a follower of " + deity.name + ".'}");
                buf.append("text{text=''}");
                buf.append("text{text=''}");
                if (this.getResponder().getDeity() != null) {
                    buf.append("text{type='bold';text='If you answer yes, your faith and all your abilities granted by " + this.getResponder().getDeity().name + " will be lost!'}");
                }
                if (!Servers.localServer.PVPSERVER) {
                    buf.append("text{type='bold';text='Warning: Converting to a deity on Freedom then travelling to a Chaos kingdom that does notalign with your deity you will lose all faith and abilities granted, and you will stop following that deity. Libila does not align with WL kingdoms and Fo/Vynora/Magranon do not align with BL kingdoms.'}");
                }
                buf.append("text{type='italic';text='Do you want to become a follower of " + deity.name + "?'}");
                buf.append("text{text=''}");
                buf.append("radio{ group='conv'; id='true';text='Yes'}");
                buf.append("radio{ group='conv'; id='false';text='No';selected='true'}");
            } else {
                buf.append("text{text='You are already a follower of " + deity.name + ".'}");
            }
            buf.append(this.createAnswerButton2());
            this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
        }
        catch (NoSuchCreatureException noSuchCreatureException) {
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
            // empty catch block
        }
    }

    public float getSkillcounter() {
        return this.skillcounter;
    }

    public void setSkillcounter(float aSkillcounter) {
        this.skillcounter = aSkillcounter;
    }

    Item getHolyItem() {
        return this.holyItem;
    }
}

