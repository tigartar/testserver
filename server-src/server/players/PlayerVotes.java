package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerVotes {
   private static Logger logger = Logger.getLogger(PlayerVotes.class.getName());
   private static final Map<Long, PlayerVotesByPlayer> playerVotes = new ConcurrentHashMap<>();
   private static final String LOADALLPLAYERVOTES = "SELECT * FROM VOTES";
   private static final String DELETEQUESTIONVOTES = "DELETE FROM VOTES WHERE QUESTIONID=?";

   private PlayerVotes() {
   }

   public static void loadAllPlayerVotes() {
      long start = System.nanoTime();

      try {
         dbLoadAllPlayerVotes();
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Problems loading Player Votes.", (Throwable)var3);
      }

      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.log(Level.INFO, "Loaded " + playerVotes.size() + " Player Votes. It took " + lElapsedTime + " millis.");
   }

   public static PlayerVote addPlayerVote(PlayerVote newPlayerVote, boolean saveit) {
      Long pId = newPlayerVote.getPlayerId();
      if (!playerVotes.containsKey(pId)) {
         playerVotes.put(pId, new PlayerVotesByPlayer());
      }

      PlayerVotesByPlayer pvbp = playerVotes.get(pId);
      PlayerVote oldPlayerVote = pvbp.get(newPlayerVote.getQuestionId());
      if (oldPlayerVote != null) {
         oldPlayerVote.update(newPlayerVote.getOption1(), newPlayerVote.getOption2(), newPlayerVote.getOption3(), newPlayerVote.getOption4());
         return oldPlayerVote;
      } else {
         pvbp.add(newPlayerVote);
         if (saveit) {
            newPlayerVote.save();
         }

         return newPlayerVote;
      }
   }

   public static PlayerVote[] getPlayerVotes(long aPlayerId) {
      PlayerVotesByPlayer pvbp = playerVotes.get(aPlayerId);
      return pvbp == null ? new PlayerVote[0] : pvbp.getVotes();
   }

   public static boolean hasPlayerVotedByQuestion(long aPlayerId, int aQuestionId) {
      Long pId = aPlayerId;
      if (playerVotes.containsKey(pId)) {
         PlayerVotesByPlayer pvbp = playerVotes.get(pId);
         if (pvbp.containsKey(aQuestionId)) {
            PlayerVote pv = pvbp.get(aQuestionId);
            return pv.hasVoted();
         }
      }

      return false;
   }

   public static PlayerVote getPlayerVotesByQuestions(long aPlayerId, int aQuestionId) {
      Long pId = aPlayerId;
      if (playerVotes.containsKey(pId)) {
         PlayerVotesByPlayer pvbp = playerVotes.get(pId);
         if (pvbp.containsKey(aQuestionId)) {
            return pvbp.get(aQuestionId);
         }
      }

      return null;
   }

   public static PlayerVote getPlayerVoteByQuestion(long aPlayerId, int aQuestionId) {
      Long pId = aPlayerId;
      if (playerVotes.containsKey(pId)) {
         PlayerVotesByPlayer pvbp = playerVotes.get(pId);
         if (pvbp.containsKey(aQuestionId)) {
            return pvbp.get(aQuestionId);
         }
      }

      return null;
   }

   public static PlayerVote[] getPlayerVotesByQuestion(int aQuestionId) {
      Map<Long, PlayerVote> pVotes = new HashMap<>();

      for(Entry<Long, PlayerVotesByPlayer> entry : playerVotes.entrySet()) {
         if (entry.getValue().containsKey(aQuestionId)) {
            PlayerVote pv = entry.getValue().get(aQuestionId);
            if (pv.hasVoted()) {
               pVotes.put(entry.getKey(), pv);
            }
         }
      }

      return pVotes.values().toArray(new PlayerVote[pVotes.size()]);
   }

   public static void deletePlayerVotes(int questionId) {
      for(Entry<Long, PlayerVotesByPlayer> entry : playerVotes.entrySet()) {
         entry.getValue().remove(questionId);
      }

      for(Player p : Players.getInstance().getPlayers()) {
         p.removePlayerVote(questionId);
      }

      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("DELETE FROM VOTES WHERE QUESTIONID=?");
         ps.setInt(1, questionId);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbLoadAllPlayerVotes() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getPlayerDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM VOTES");
         rs = ps.executeQuery();

         while(rs.next()) {
            long aPlayerId = rs.getLong("PLAYERID");
            int aQuestionId = rs.getInt("QUESTIONID");
            boolean aOption1 = rs.getBoolean("OPTION1");
            boolean aOption2 = rs.getBoolean("OPTION2");
            boolean aOption3 = rs.getBoolean("OPTION3");
            boolean aOption4 = rs.getBoolean("OPTION4");
            addPlayerVote(new PlayerVote(aPlayerId, aQuestionId, aOption1, aOption2, aOption3, aOption4), false);
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }
}
