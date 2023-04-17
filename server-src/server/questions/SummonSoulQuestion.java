/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.SummonSoulAcceptQuestion;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SummonSoulQuestion
extends Question {
    private boolean properlySent = false;
    private static final Logger logger = Logger.getLogger(SummonSoulQuestion.class.getName());

    public SummonSoulQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 79, aTarget);
    }

    @Override
    public void answer(Properties aAnswers) {
        if (!this.properlySent) {
            return;
        }
        String name = aAnswers.getProperty("name");
        Creature soul = null;
        if (name != null && name.length() > 1) {
            soul = SummonSoulQuestion.acquireSoul(StringUtilities.raiseFirstLetter(name));
        }
        if (soul == null || soul.getPower() > this.getResponder().getPower()) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such soul found.");
        } else {
            SummonSoulAcceptQuestion ssaq = new SummonSoulAcceptQuestion(soul, "Accept Summon?", "Would you like to accept a summon from " + this.getResponder().getName() + "?", this.getResponder().getWurmId(), this.getResponder());
            ssaq.sendQuestion();
        }
    }

    private static Creature acquireSoul(String name) {
        PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
        if (pinf != null && pinf.loaded) {
            try {
                return Server.getInstance().getCreature(pinf.wurmId);
            }
            catch (NoSuchPlayerException | NoSuchCreatureException ex) {
                logger.log(Level.WARNING, ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public void sendQuestion() {
        this.properlySent = true;
        String sb = this.getBmlHeader() + "text{text='Which soul do you wish to summon?'};label{text='Name:'};input{id='name';maxchars='40';text=\"\"};" + this.createAnswerButton2();
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, sb, 200, 200, 200, this.title);
    }
}

