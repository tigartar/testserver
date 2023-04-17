/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.structures.NoSuchWallException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SleepQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(SleepQuestion.class.getName());

    public SleepQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 47, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        String key = "sleep";
        String val = answers.getProperty("sleep");
        if (val != null && val.equals("true")) {
            try {
                this.getResponder().getCurrentAction();
                this.getResponder().getCommunicator().sendNormalServerMessage("You are too busy to sleep right now.");
            }
            catch (NoSuchActionException nsa) {
                try {
                    BehaviourDispatcher.action(this.getResponder(), this.getResponder().getCommunicator(), -1L, this.target, (short)140);
                }
                catch (FailedException failedException) {
                }
                catch (NoSuchBehaviourException nsb) {
                    logger.log(Level.WARNING, nsb.getMessage(), nsb);
                }
                catch (NoSuchCreatureException nsb) {
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, nsi.getMessage(), nsi);
                }
                catch (NoSuchPlayerException nsi) {
                }
                catch (NoSuchWallException nsw) {
                    logger.log(Level.WARNING, nsw.getMessage(), nsw);
                }
            }
        } else {
            this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to go to sleep right now.");
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text='Do you want to go to sleep? You will log off Wurm.'}text{text=''}");
        buf.append("radio{ group='sleep'; id='true';text='Yes';selected='true'}");
        buf.append("radio{ group='sleep'; id='false';text='No'}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

