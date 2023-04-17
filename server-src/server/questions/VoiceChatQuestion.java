/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.questions.Question;
import java.util.Properties;

public class VoiceChatQuestion
extends Question
implements TimeConstants,
MonetaryConstants {
    public VoiceChatQuestion(Creature aResponder) {
        super(aResponder, "Voice chat options", "Select voice chat subscription", 95, aResponder.getWurmId());
    }

    @Override
    public void answer(Properties aAnswers) {
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"The Ericsson ingame voice chat service is being shut down as of 2013-01-31 due to low interest. We can no longer offer these packages.\"};text{text=\"\"}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(500, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

