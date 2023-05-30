package com.wurmonline.server.support;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcVoting;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VoteQuestions implements CounterTypes, TimeConstants {
   private static Logger logger = Logger.getLogger(VoteQuestions.class.getName());
   private static final Map<Integer, VoteQuestion> voteQuestions = new ConcurrentHashMap<>();
   private static final ConcurrentLinkedDeque<VoteQuestionsQueue> questionsQueue = new ConcurrentLinkedDeque<>();
   private static int lastQuestionId = 0;
   private static final String LOADALLQUESTIONS = "SELECT * FROM VOTINGQUESTIONS";
   private static final String LOADALLVOTINGSERVERS = "SELECT * FROM VOTINGSERVERS";

   private VoteQuestions() {
   }

   public static void loadVoteQuestions() {
      long start = System.nanoTime();

      try {
         dbLoadAllVoteQuestions();
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Problems loading Vote Questions", (Throwable)var3);
      }

      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.log(Level.INFO, "Loaded " + voteQuestions.size() + " Vote Questions. It took " + lElapsedTime + " millis.");
   }

   public static VoteQuestion addVoteQuestion(VoteQuestion newVoteQuestion, boolean saveit) {
      if (voteQuestions.containsKey(newVoteQuestion.getQuestionId())) {
         VoteQuestion oldVoteQuestion = voteQuestions.get(newVoteQuestion.getQuestionId());
         oldVoteQuestion.update(
            newVoteQuestion.getQuestionTitle(),
            newVoteQuestion.getQuestionText(),
            newVoteQuestion.getOption1Text(),
            newVoteQuestion.getOption2Text(),
            newVoteQuestion.getOption3Text(),
            newVoteQuestion.getOption4Text(),
            newVoteQuestion.isAllowMultiple(),
            newVoteQuestion.isPremOnly(),
            newVoteQuestion.isJK(),
            newVoteQuestion.isMR(),
            newVoteQuestion.isHots(),
            newVoteQuestion.isFreedom(),
            newVoteQuestion.getVoteStart(),
            newVoteQuestion.getVoteEnd(),
            newVoteQuestion.getServers()
         );
         return oldVoteQuestion;
      } else {
         voteQuestions.put(newVoteQuestion.getQuestionId(), newVoteQuestion);
         if (saveit) {
            newVoteQuestion.save();
         }

         return newVoteQuestion;
      }
   }

   public static VoteQuestion[] getArchiveVoteQuestions() {
      Map<Integer, VoteQuestion> archiveVoteQuestions = new HashMap<>();

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.getArchiveState() == 2) {
            archiveVoteQuestions.put(entry.getKey(), entry.getValue());
         }
      }

      return archiveVoteQuestions.values().toArray(new VoteQuestion[archiveVoteQuestions.size()]);
   }

   public static VoteQuestion[] getFinishedQuestions() {
      Map<Integer, VoteQuestion> finishedVoteQuestions = new HashMap<>();

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.hasSummary() && voteQuestion.getSent() == 4) {
            finishedVoteQuestions.put(entry.getKey(), entry.getValue());
         }
      }

      return finishedVoteQuestions.values().toArray(new VoteQuestion[finishedVoteQuestions.size()]);
   }

   public static void deleteVoteQuestion(int aId) {
      VoteQuestion vq = getVoteQuestion(aId);
      if (Servers.isThisLoginServer() && vq != null && vq.getSent() == 1) {
         WcVoting wv = new WcVoting((byte)5, aId);

         for(VoteServer vs : vq.getServers()) {
            if (vs.getServerId() != Servers.getLocalServerId()) {
               wv.sendToServer(vs.getServerId());
            }
         }
      }

      voteQuestions.remove(aId);
      if (vq != null) {
         vq.delete();
      }
   }

   public static void closeVoteing(int aId, long aVoteEnd) {
      VoteQuestion vq = getVoteQuestion(aId);
      if (Servers.isThisLoginServer() && vq != null && vq.getSent() == 1) {
         WcVoting wv = new WcVoting((byte)6, aId, aVoteEnd);

         for(VoteServer vs : vq.getServers()) {
            if (vs.getServerId() != Servers.getLocalServerId()) {
               wv.sendToServer(vs.getServerId());
            }
         }
      }

      vq.closeVoting(aVoteEnd);
   }

   public static VoteQuestion getVoteQuestion(int aId) {
      return voteQuestions.get(aId);
   }

   public static VoteQuestion[] getVoteQuestions(Player player) {
      Map<Integer, VoteQuestion> playerVoteQuestions = new HashMap<>();

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.canVote(player)) {
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
         }
      }

      return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
   }

   public static int[] getVoteQuestionIds(Player player) {
      VoteQuestion[] vqs = getVoteQuestions(player);
      int[] ids = new int[vqs.length];

      for(int i = 0; i < vqs.length; ++i) {
         ids[i] = vqs[i].getQuestionId();
      }

      return ids;
   }

   public static VoteQuestion[] getVoteQuestionsAboutToStart() {
      Map<Integer, VoteQuestion> playerVoteQuestions = new HashMap<>();

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.aboutToStart()) {
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
         }
      }

      return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
   }

   public static VoteQuestion[] getVoteQuestionsNeedingSummary() {
      Map<Integer, VoteQuestion> playerVoteQuestions = new HashMap<>();

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.canMakeSummary()) {
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
         }
      }

      return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
   }

   public static VoteQuestion[] getVoteQuestions() {
      return voteQuestions.values().toArray(new VoteQuestion[voteQuestions.size()]);
   }

   private static void dbLoadAllVoteQuestions() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM VOTINGQUESTIONS");

         int aQuestionId;
         String aQuestionTitle;
         String aQuestionText;
         String aOption1Text;
         String aOption2Text;
         String aOption3Text;
         String aOption4Text;
         boolean aAllowMultiple;
         boolean aPremOnly;
         boolean aJK;
         boolean aMR;
         boolean aHots;
         boolean aFreedom;
         long aStart;
         long aEnd;
         byte aSent;
         short aVotesTotal;
         boolean aHasSummary;
         short aOption1Count;
         short aOption2Count;
         short aOption3Count;
         short aOption4Count;
         String aTrelloCardId;
         byte aArchiveState;
         for(rs = ps.executeQuery();
            rs.next();
            addVoteQuestion(
               new VoteQuestion(
                  aQuestionId,
                  aQuestionTitle,
                  aQuestionText,
                  aOption1Text,
                  aOption2Text,
                  aOption3Text,
                  aOption4Text,
                  aAllowMultiple,
                  aPremOnly,
                  aJK,
                  aMR,
                  aHots,
                  aFreedom,
                  aStart,
                  aEnd,
                  aSent,
                  aVotesTotal,
                  aHasSummary,
                  aOption1Count,
                  aOption2Count,
                  aOption3Count,
                  aOption4Count,
                  aTrelloCardId,
                  aArchiveState
               ),
               false
            )
         ) {
            aQuestionId = rs.getInt("QUESTIONID");
            aQuestionTitle = rs.getString("QUESTIONTITLE");
            aQuestionText = rs.getString("QUESTIONTEXT");
            aOption1Text = rs.getString("OPTION1_TEXT");
            aOption2Text = rs.getString("OPTION2_TEXT");
            aOption3Text = rs.getString("OPTION3_TEXT");
            aOption4Text = rs.getString("OPTION4_TEXT");
            aAllowMultiple = rs.getBoolean("ALLOW_MULTIPLE");
            aPremOnly = rs.getBoolean("PREMIUM_ONLY");
            aJK = rs.getBoolean("JK");
            aMR = rs.getBoolean("MR");
            aHots = rs.getBoolean("HOTS");
            aFreedom = rs.getBoolean("FREEDOM");
            aStart = rs.getLong("VOTE_START");
            aEnd = rs.getLong("VOTE_END");
            aSent = rs.getByte("SENT");
            aVotesTotal = rs.getShort("VOTES_TOTAL");
            aHasSummary = rs.getBoolean("HAS_SUMMARY");
            aOption1Count = rs.getShort("OPTION1_COUNT");
            aOption2Count = rs.getShort("OPTION2_COUNT");
            aOption3Count = rs.getShort("OPTION3_COUNT");
            aOption4Count = rs.getShort("OPTION4_COUNT");
            aTrelloCardId = rs.getString("TRELLOCARDID");
            aArchiveState = rs.getByte("ARCHIVESTATECODE");
            if (aQuestionId > lastQuestionId) {
               lastQuestionId = aQuestionId;
            }
         }

         DbUtilities.closeDatabaseObjects(ps, rs);
         ps = dbcon.prepareStatement("SELECT * FROM VOTINGSERVERS");
         rs = ps.executeQuery();

         while(rs.next()) {
            aQuestionId = rs.getInt("QUESTIONID");
            int aServerId = rs.getInt("SERVERID");
            short aVotesTotal = rs.getShort("VOTES_TOTAL");
            short aOption1Count = rs.getShort("OPTION1_COUNT");
            short aOption2Count = rs.getShort("OPTION2_COUNT");
            short aOption3Count = rs.getShort("OPTION3_COUNT");
            short aOption4Count = rs.getShort("OPTION4_COUNT");
            getVoteQuestion(aQuestionId).addServer(aServerId, aVotesTotal, aOption1Count, aOption2Count, aOption3Count, aOption4Count);
         }
      } catch (SQLException var32) {
         logger.log(Level.WARNING, var32.getMessage(), (Throwable)var32);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final int getNextQuestionId() {
      return ++lastQuestionId;
   }

   public static final void queueAddVoteQuestion(
      int aQuestionId,
      String aQuestionTitle,
      String aQuestionText,
      String aOption1Text,
      String aOption2Text,
      String aOption3Text,
      String aOption4Text,
      boolean aAllowMultiple,
      boolean aPremOnly,
      boolean aJK,
      boolean aMR,
      boolean aHoTs,
      boolean aFreedom,
      long voteStart,
      long voteEnd
   ) {
      VoteQuestion newVoteQuestion = new VoteQuestion(
         aQuestionId,
         aQuestionTitle,
         aQuestionText,
         aOption1Text,
         aOption2Text,
         aOption3Text,
         aOption4Text,
         aAllowMultiple,
         aPremOnly,
         aJK,
         aMR,
         aHoTs,
         aFreedom,
         voteStart,
         voteEnd
      );
      questionsQueue.add(new VoteQuestionsQueue((byte)0, newVoteQuestion));
   }

   public static final void queueRemoveVoteQuestion(int aQuestionId) {
      questionsQueue.add(new VoteQuestionsQueue((byte)1, aQuestionId));
   }

   public static final void queueCloseVoteQuestion(int aQuestionId, long newEnd) {
      questionsQueue.add(new VoteQuestionsQueue((byte)2, aQuestionId, newEnd));
   }

   public static final void queueSetTrelloCardId(int aQuestionId, String aTrelloCardId) {
      questionsQueue.add(new VoteQuestionsQueue((byte)3, aQuestionId, aTrelloCardId));
   }

   public static final void queueSetArchiveState(int aQuestionId, byte newArchiveState) {
      questionsQueue.add(new VoteQuestionsQueue((byte)4, aQuestionId, newArchiveState));
   }

   public static final void handleVoting() {
      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         entry.getValue().endVoting();
      }

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         entry.getValue().setArchive();
      }

      if (Servers.isThisLoginServer()) {
         VoteQuestion[] vqStarting = getVoteQuestionsAboutToStart();
         if (vqStarting.length > 0) {
            for(VoteQuestion vq : vqStarting) {
               WcVoting wv = new WcVoting(vq);

               for(VoteServer vs : vq.getServers()) {
                  if (vs.getServerId() != Servers.getLocalServerId()) {
                     wv.sendToServer(vs.getServerId());
                  }
               }

               vq.setSent((byte)1);
            }

            return;
         }

         VoteQuestion[] vqEnding = getVoteQuestionsNeedingSummary();
         if (vqEnding.length > 0) {
            for(VoteQuestion vq : vqEnding) {
               short total = 0;
               short count1 = 0;
               short count2 = 0;
               short count3 = 0;
               short count4 = 0;

               for(PlayerVote pv : PlayerVotes.getPlayerVotesByQuestion(vq.getQuestionId())) {
                  ++total;
                  if (pv.getOption1()) {
                     ++count1;
                  }

                  if (pv.getOption2()) {
                     ++count2;
                  }

                  if (pv.getOption3()) {
                     ++count3;
                  }

                  if (pv.getOption4()) {
                     ++count4;
                  }
               }

               vq.saveSummary(total, count1, count2, count3, count4);
               WcVoting wv = new WcVoting(
                  vq.getQuestionId(), vq.getVoteCount(), vq.getOption1Count(), vq.getOption2Count(), vq.getOption3Count(), vq.getOption4Count()
               );

               for(VoteServer vs : vq.getServers()) {
                  if (vs.getServerId() != Servers.getLocalServerId()) {
                     wv.sendToServer(vs.getServerId());
                  }
               }

               vq.setSent((byte)4);
            }

            return;
         }
      }

      for(VoteQuestionsQueue vqq = questionsQueue.pollFirst(); vqq != null; vqq = questionsQueue.pollFirst()) {
         vqq.action();
      }

      for(Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
         VoteQuestion voteQuestion = entry.getValue();
         if (voteQuestion.justStarted()) {
            voteQuestion.setSent((byte)2);
            Players.sendVotingOpen(voteQuestion);
         }
      }
   }

   public static final void handleArchiveTickets() {
      for(VoteQuestion vq : voteQuestions.values()) {
         if (vq.getArchiveState() == 3) {
            deleteVoteQuestion(vq.getQuestionId());
         }
      }
   }
}
