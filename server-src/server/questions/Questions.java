/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.CultQuestion;
import com.wurmonline.server.questions.NoSuchQuestionException;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.SelectSpawnQuestion;
import com.wurmonline.server.questions.SpawnQuestion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Questions
implements TimeConstants {
    private static Map<Integer, Question> questions = new HashMap<Integer, Question>();
    private static Logger logger = Logger.getLogger(Questions.class.getName());

    private Questions() {
    }

    static void addQuestion(Question question) {
        questions.put(question.getId(), question);
        Question lastQuestion = ((Player)question.getResponder()).getCurrentQuestion();
        if (lastQuestion != null) {
            lastQuestion.timedOut();
        }
        ((Player)question.getResponder()).setQuestion(question);
    }

    public static Question getQuestion(int id) throws NoSuchQuestionException {
        Integer iid = id;
        Question question = questions.get(iid);
        if (question == null) {
            throw new NoSuchQuestionException(String.valueOf(id));
        }
        return question;
    }

    public static final int getNumUnanswered() {
        return questions.size();
    }

    public static void removeQuestion(Question question) {
        if (question != null) {
            Integer iid = question.getId();
            questions.remove(iid);
        }
    }

    public static void removeQuestions(Player player) {
        Question[] quests = questions.values().toArray(new Question[questions.values().size()]);
        for (int x = 0; x < quests.length; ++x) {
            if (quests[x].getResponder() != player) continue;
            quests[x].clearResponder();
            questions.remove(quests[x].getId());
        }
    }

    public static void trimQuestions() {
        long now = System.currentTimeMillis();
        HashSet<Question> toRemove = new HashSet<Question>();
        for (Question lQuestion : questions.values()) {
            long maxTime = 900000L;
            if (lQuestion instanceof CultQuestion) {
                maxTime = 1800000L;
            }
            if (lQuestion instanceof SpawnQuestion) {
                maxTime = 0x6DDD00L;
            }
            if (lQuestion instanceof SelectSpawnQuestion || now - lQuestion.getSendTime() <= maxTime && lQuestion.getResponder().hasLink()) continue;
            toRemove.add(lQuestion);
        }
        for (Question lQuestion : toRemove) {
            lQuestion.timedOut();
            Questions.removeQuestion(lQuestion);
            if (!lQuestion.getResponder().isPlayer() || ((Player)lQuestion.getResponder()).question != lQuestion) continue;
            ((Player)lQuestion.getResponder()).question = null;
        }
        if (logger.isLoggable(Level.FINER) && questions.size() > 0) {
            logger.finer("Size of question list=" + questions.size());
        }
    }
}

