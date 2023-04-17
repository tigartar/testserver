/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotesByPlayer;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerVotes {
    private static Logger logger = Logger.getLogger(PlayerVotes.class.getName());
    private static final Map<Long, PlayerVotesByPlayer> playerVotes = new ConcurrentHashMap<Long, PlayerVotesByPlayer>();
    private static final String LOADALLPLAYERVOTES = "SELECT * FROM VOTES";
    private static final String DELETEQUESTIONVOTES = "DELETE FROM VOTES WHERE QUESTIONID=?";

    private PlayerVotes() {
    }

    public static void loadAllPlayerVotes() {
        long start = System.nanoTime();
        try {
            PlayerVotes.dbLoadAllPlayerVotes();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading Player Votes.", ex);
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Loaded " + playerVotes.size() + " Player Votes. It took " + lElapsedTime + " millis.");
    }

    public static PlayerVote addPlayerVote(PlayerVote newPlayerVote, boolean saveit) {
        PlayerVotesByPlayer pvbp;
        PlayerVote oldPlayerVote;
        Long pId = newPlayerVote.getPlayerId();
        if (!playerVotes.containsKey(pId)) {
            playerVotes.put(pId, new PlayerVotesByPlayer());
        }
        if ((oldPlayerVote = (pvbp = playerVotes.get(pId)).get(newPlayerVote.getQuestionId())) != null) {
            oldPlayerVote.update(newPlayerVote.getOption1(), newPlayerVote.getOption2(), newPlayerVote.getOption3(), newPlayerVote.getOption4());
            return oldPlayerVote;
        }
        pvbp.add(newPlayerVote);
        if (saveit) {
            newPlayerVote.save();
        }
        return newPlayerVote;
    }

    public static PlayerVote[] getPlayerVotes(long aPlayerId) {
        PlayerVotesByPlayer pvbp = playerVotes.get(aPlayerId);
        if (pvbp == null) {
            return new PlayerVote[0];
        }
        return pvbp.getVotes();
    }

    public static boolean hasPlayerVotedByQuestion(long aPlayerId, int aQuestionId) {
        PlayerVotesByPlayer pvbp;
        Long pId = aPlayerId;
        if (playerVotes.containsKey(pId) && (pvbp = playerVotes.get(pId)).containsKey(aQuestionId)) {
            PlayerVote pv = pvbp.get(aQuestionId);
            return pv.hasVoted();
        }
        return false;
    }

    public static PlayerVote getPlayerVotesByQuestions(long aPlayerId, int aQuestionId) {
        PlayerVotesByPlayer pvbp;
        Long pId = aPlayerId;
        if (playerVotes.containsKey(pId) && (pvbp = playerVotes.get(pId)).containsKey(aQuestionId)) {
            PlayerVote pv = pvbp.get(aQuestionId);
            return pv;
        }
        return null;
    }

    public static PlayerVote getPlayerVoteByQuestion(long aPlayerId, int aQuestionId) {
        PlayerVotesByPlayer pvbp;
        Long pId = aPlayerId;
        if (playerVotes.containsKey(pId) && (pvbp = playerVotes.get(pId)).containsKey(aQuestionId)) {
            PlayerVote pv = pvbp.get(aQuestionId);
            return pv;
        }
        return null;
    }

    public static PlayerVote[] getPlayerVotesByQuestion(int aQuestionId) {
        HashMap<Long, PlayerVote> pVotes = new HashMap<Long, PlayerVote>();
        for (Map.Entry<Long, PlayerVotesByPlayer> entry : playerVotes.entrySet()) {
            PlayerVote pv;
            if (!entry.getValue().containsKey(aQuestionId) || !(pv = entry.getValue().get(aQuestionId)).hasVoted()) continue;
            pVotes.put(entry.getKey(), pv);
        }
        return pVotes.values().toArray(new PlayerVote[pVotes.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void deletePlayerVotes(int questionId) {
        for (Map.Entry<Long, PlayerVotesByPlayer> entry : playerVotes.entrySet()) {
            entry.getValue().remove(questionId);
        }
        for (Player p : Players.getInstance().getPlayers()) {
            p.removePlayerVote(questionId);
        }
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(DELETEQUESTIONVOTES);
            ps.setInt(1, questionId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbLoadAllPlayerVotes() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(LOADALLPLAYERVOTES);
            rs = ps.executeQuery();
            while (rs.next()) {
                long aPlayerId = rs.getLong("PLAYERID");
                int aQuestionId = rs.getInt("QUESTIONID");
                boolean aOption1 = rs.getBoolean("OPTION1");
                boolean aOption2 = rs.getBoolean("OPTION2");
                boolean aOption3 = rs.getBoolean("OPTION3");
                boolean aOption4 = rs.getBoolean("OPTION4");
                PlayerVotes.addPlayerVote(new PlayerVote(aPlayerId, aQuestionId, aOption1, aOption2, aOption3, aOption4), false);
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }
}

