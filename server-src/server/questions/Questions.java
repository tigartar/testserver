package com.wurmonline.server.questions;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Questions implements TimeConstants {
   private static Map<Integer, Question> questions = new HashMap<>();
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
      } else {
         return question;
      }
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

      for(int x = 0; x < quests.length; ++x) {
         if (quests[x].getResponder() == player) {
            quests[x].clearResponder();
            questions.remove(quests[x].getId());
         }
      }
   }

   public static void trimQuestions() {
      long now = System.currentTimeMillis();
      Set<Question> toRemove = new HashSet<>();

      for(Question lQuestion : questions.values()) {
         long maxTime = 900000L;
         if (lQuestion instanceof CultQuestion) {
            maxTime = 1800000L;
         }

         if (lQuestion instanceof SpawnQuestion) {
            maxTime = 7200000L;
         }

         if (!(lQuestion instanceof SelectSpawnQuestion) && (now - lQuestion.getSendTime() > maxTime || !lQuestion.getResponder().hasLink())) {
            toRemove.add(lQuestion);
         }
      }

      for(Question lQuestion : toRemove) {
         lQuestion.timedOut();
         removeQuestion(lQuestion);
         if (lQuestion.getResponder().isPlayer() && ((Player)lQuestion.getResponder()).question == lQuestion) {
            ((Player)lQuestion.getResponder()).question = null;
         }
      }

      if (logger.isLoggable(Level.FINER) && questions.size() > 0) {
         logger.finer("Size of question list=" + questions.size());
      }
   }
}
