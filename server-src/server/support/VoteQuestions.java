/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestionsQueue;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcVoting;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class VoteQuestions
implements CounterTypes,
TimeConstants {
    private static Logger logger = Logger.getLogger(VoteQuestions.class.getName());
    private static final Map<Integer, VoteQuestion> voteQuestions = new ConcurrentHashMap<Integer, VoteQuestion>();
    private static final ConcurrentLinkedDeque<VoteQuestionsQueue> questionsQueue = new ConcurrentLinkedDeque();
    private static int lastQuestionId = 0;
    private static final String LOADALLQUESTIONS = "SELECT * FROM VOTINGQUESTIONS";
    private static final String LOADALLVOTINGSERVERS = "SELECT * FROM VOTINGSERVERS";

    private VoteQuestions() {
    }

    public static void loadVoteQuestions() {
        long start = System.nanoTime();
        try {
            VoteQuestions.dbLoadAllVoteQuestions();
        }
        catch (Exception ex) {
            logger.log(Level.WARNING, "Problems loading Vote Questions", ex);
        }
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
        logger.log(Level.INFO, "Loaded " + voteQuestions.size() + " Vote Questions. It took " + lElapsedTime + " millis.");
    }

    public static VoteQuestion addVoteQuestion(VoteQuestion newVoteQuestion, boolean saveit) {
        if (voteQuestions.containsKey(newVoteQuestion.getQuestionId())) {
            VoteQuestion oldVoteQuestion = voteQuestions.get(newVoteQuestion.getQuestionId());
            oldVoteQuestion.update(newVoteQuestion.getQuestionTitle(), newVoteQuestion.getQuestionText(), newVoteQuestion.getOption1Text(), newVoteQuestion.getOption2Text(), newVoteQuestion.getOption3Text(), newVoteQuestion.getOption4Text(), newVoteQuestion.isAllowMultiple(), newVoteQuestion.isPremOnly(), newVoteQuestion.isJK(), newVoteQuestion.isMR(), newVoteQuestion.isHots(), newVoteQuestion.isFreedom(), newVoteQuestion.getVoteStart(), newVoteQuestion.getVoteEnd(), newVoteQuestion.getServers());
            return oldVoteQuestion;
        }
        voteQuestions.put(newVoteQuestion.getQuestionId(), newVoteQuestion);
        if (saveit) {
            newVoteQuestion.save();
        }
        return newVoteQuestion;
    }

    public static VoteQuestion[] getArchiveVoteQuestions() {
        HashMap<Integer, VoteQuestion> archiveVoteQuestions = new HashMap<Integer, VoteQuestion>();
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (voteQuestion.getArchiveState() != 2) continue;
            archiveVoteQuestions.put(entry.getKey(), entry.getValue());
        }
        return archiveVoteQuestions.values().toArray(new VoteQuestion[archiveVoteQuestions.size()]);
    }

    public static VoteQuestion[] getFinishedQuestions() {
        HashMap<Integer, VoteQuestion> finishedVoteQuestions = new HashMap<Integer, VoteQuestion>();
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (!voteQuestion.hasSummary() || voteQuestion.getSent() != 4) continue;
            finishedVoteQuestions.put(entry.getKey(), entry.getValue());
        }
        return finishedVoteQuestions.values().toArray(new VoteQuestion[finishedVoteQuestions.size()]);
    }

    public static void deleteVoteQuestion(int aId) {
        VoteQuestion vq = VoteQuestions.getVoteQuestion(aId);
        if (Servers.isThisLoginServer() && vq != null && vq.getSent() == 1) {
            WcVoting wv = new WcVoting(5, aId);
            for (VoteServer vs : vq.getServers()) {
                if (vs.getServerId() == Servers.getLocalServerId()) continue;
                wv.sendToServer(vs.getServerId());
            }
        }
        voteQuestions.remove(aId);
        if (vq != null) {
            vq.delete();
        }
    }

    public static void closeVoteing(int aId, long aVoteEnd) {
        VoteQuestion vq = VoteQuestions.getVoteQuestion(aId);
        if (Servers.isThisLoginServer() && vq != null && vq.getSent() == 1) {
            WcVoting wv = new WcVoting(6, aId, aVoteEnd);
            for (VoteServer vs : vq.getServers()) {
                if (vs.getServerId() == Servers.getLocalServerId()) continue;
                wv.sendToServer(vs.getServerId());
            }
        }
        vq.closeVoting(aVoteEnd);
    }

    public static VoteQuestion getVoteQuestion(int aId) {
        return voteQuestions.get(aId);
    }

    public static VoteQuestion[] getVoteQuestions(Player player) {
        HashMap<Integer, VoteQuestion> playerVoteQuestions = new HashMap<Integer, VoteQuestion>();
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (!voteQuestion.canVote(player)) continue;
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
        }
        return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
    }

    public static int[] getVoteQuestionIds(Player player) {
        VoteQuestion[] vqs = VoteQuestions.getVoteQuestions(player);
        int[] ids = new int[vqs.length];
        for (int i = 0; i < vqs.length; ++i) {
            ids[i] = vqs[i].getQuestionId();
        }
        return ids;
    }

    public static VoteQuestion[] getVoteQuestionsAboutToStart() {
        HashMap<Integer, VoteQuestion> playerVoteQuestions = new HashMap<Integer, VoteQuestion>();
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (!voteQuestion.aboutToStart()) continue;
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
        }
        return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
    }

    public static VoteQuestion[] getVoteQuestionsNeedingSummary() {
        HashMap<Integer, VoteQuestion> playerVoteQuestions = new HashMap<Integer, VoteQuestion>();
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (!voteQuestion.canMakeSummary()) continue;
            playerVoteQuestions.put(entry.getKey(), entry.getValue());
        }
        return playerVoteQuestions.values().toArray(new VoteQuestion[playerVoteQuestions.size()]);
    }

    public static VoteQuestion[] getVoteQuestions() {
        return voteQuestions.values().toArray(new VoteQuestion[voteQuestions.size()]);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbLoadAllVoteQuestions() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            int aQuestionId;
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(LOADALLQUESTIONS);
            rs = ps.executeQuery();
            while (rs.next()) {
                aQuestionId = rs.getInt("QUESTIONID");
                String aQuestionTitle = rs.getString("QUESTIONTITLE");
                String aQuestionText = rs.getString("QUESTIONTEXT");
                String aOption1Text = rs.getString("OPTION1_TEXT");
                String aOption2Text = rs.getString("OPTION2_TEXT");
                String aOption3Text = rs.getString("OPTION3_TEXT");
                String aOption4Text = rs.getString("OPTION4_TEXT");
                boolean aAllowMultiple = rs.getBoolean("ALLOW_MULTIPLE");
                boolean aPremOnly = rs.getBoolean("PREMIUM_ONLY");
                boolean aJK = rs.getBoolean("JK");
                boolean aMR = rs.getBoolean("MR");
                boolean aHots = rs.getBoolean("HOTS");
                boolean aFreedom = rs.getBoolean("FREEDOM");
                long aStart = rs.getLong("VOTE_START");
                long aEnd = rs.getLong("VOTE_END");
                byte aSent = rs.getByte("SENT");
                short aVotesTotal = rs.getShort("VOTES_TOTAL");
                boolean aHasSummary = rs.getBoolean("HAS_SUMMARY");
                short aOption1Count = rs.getShort("OPTION1_COUNT");
                short aOption2Count = rs.getShort("OPTION2_COUNT");
                short aOption3Count = rs.getShort("OPTION3_COUNT");
                short aOption4Count = rs.getShort("OPTION4_COUNT");
                String aTrelloCardId = rs.getString("TRELLOCARDID");
                byte aArchiveState = rs.getByte("ARCHIVESTATECODE");
                if (aQuestionId > lastQuestionId) {
                    lastQuestionId = aQuestionId;
                }
                VoteQuestions.addVoteQuestion(new VoteQuestion(aQuestionId, aQuestionTitle, aQuestionText, aOption1Text, aOption2Text, aOption3Text, aOption4Text, aAllowMultiple, aPremOnly, aJK, aMR, aHots, aFreedom, aStart, aEnd, aSent, aVotesTotal, aHasSummary, aOption1Count, aOption2Count, aOption3Count, aOption4Count, aTrelloCardId, aArchiveState), false);
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            ps = dbcon.prepareStatement(LOADALLVOTINGSERVERS);
            rs = ps.executeQuery();
            while (rs.next()) {
                aQuestionId = rs.getInt("QUESTIONID");
                int aServerId = rs.getInt("SERVERID");
                short aVotesTotal = rs.getShort("VOTES_TOTAL");
                short aOption1Count = rs.getShort("OPTION1_COUNT");
                short aOption2Count = rs.getShort("OPTION2_COUNT");
                short aOption3Count = rs.getShort("OPTION3_COUNT");
                short aOption4Count = rs.getShort("OPTION4_COUNT");
                VoteQuestions.getVoteQuestion(aQuestionId).addServer(aServerId, aVotesTotal, aOption1Count, aOption2Count, aOption3Count, aOption4Count);
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

    public static final int getNextQuestionId() {
        return ++lastQuestionId;
    }

    public static final void queueAddVoteQuestion(int aQuestionId, String aQuestionTitle, String aQuestionText, String aOption1Text, String aOption2Text, String aOption3Text, String aOption4Text, boolean aAllowMultiple, boolean aPremOnly, boolean aJK, boolean aMR, boolean aHoTs, boolean aFreedom, long voteStart, long voteEnd) {
        VoteQuestion newVoteQuestion = new VoteQuestion(aQuestionId, aQuestionTitle, aQuestionText, aOption1Text, aOption2Text, aOption3Text, aOption4Text, aAllowMultiple, aPremOnly, aJK, aMR, aHoTs, aFreedom, voteStart, voteEnd);
        questionsQueue.add(new VoteQuestionsQueue(0, newVoteQuestion));
    }

    public static final void queueRemoveVoteQuestion(int aQuestionId) {
        questionsQueue.add(new VoteQuestionsQueue(1, aQuestionId));
    }

    public static final void queueCloseVoteQuestion(int aQuestionId, long newEnd) {
        questionsQueue.add(new VoteQuestionsQueue(2, aQuestionId, newEnd));
    }

    public static final void queueSetTrelloCardId(int aQuestionId, String aTrelloCardId) {
        questionsQueue.add(new VoteQuestionsQueue(3, aQuestionId, aTrelloCardId));
    }

    public static final void queueSetArchiveState(int aQuestionId, byte newArchiveState) {
        questionsQueue.add(new VoteQuestionsQueue(4, aQuestionId, newArchiveState));
    }

    public static final void handleVoting() {
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            entry.getValue().endVoting();
        }
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            entry.getValue().setArchive();
        }
        if (Servers.isThisLoginServer()) {
            VoteQuestion[] vqStarting = VoteQuestions.getVoteQuestionsAboutToStart();
            if (vqStarting.length > 0) {
                for (VoteQuestion vq : vqStarting) {
                    WcVoting wv = new WcVoting(vq);
                    for (VoteServer vs : vq.getServers()) {
                        if (vs.getServerId() == Servers.getLocalServerId()) continue;
                        wv.sendToServer(vs.getServerId());
                    }
                    vq.setSent((byte)1);
                }
                return;
            }
            VoteQuestion[] voteQuestionArray = VoteQuestions.getVoteQuestionsNeedingSummary();
            if (voteQuestionArray.length > 0) {
                for (VoteQuestion vq : voteQuestionArray) {
                    short total = 0;
                    short count1 = 0;
                    short count2 = 0;
                    short count3 = 0;
                    short count4 = 0;
                    for (PlayerVote pv : PlayerVotes.getPlayerVotesByQuestion(vq.getQuestionId())) {
                        total = (short)(total + 1);
                        if (pv.getOption1()) {
                            count1 = (short)(count1 + 1);
                        }
                        if (pv.getOption2()) {
                            count2 = (short)(count2 + 1);
                        }
                        if (pv.getOption3()) {
                            count3 = (short)(count3 + 1);
                        }
                        if (!pv.getOption4()) continue;
                        count4 = (short)(count4 + 1);
                    }
                    vq.saveSummary(total, count1, count2, count3, count4);
                    WcVoting wv = new WcVoting(vq.getQuestionId(), vq.getVoteCount(), vq.getOption1Count(), vq.getOption2Count(), vq.getOption3Count(), vq.getOption4Count());
                    for (VoteServer vs : vq.getServers()) {
                        if (vs.getServerId() == Servers.getLocalServerId()) continue;
                        wv.sendToServer(vs.getServerId());
                    }
                    vq.setSent((byte)4);
                }
                return;
            }
        }
        VoteQuestionsQueue vqq = questionsQueue.pollFirst();
        while (vqq != null) {
            vqq.action();
            vqq = questionsQueue.pollFirst();
        }
        for (Map.Entry<Integer, VoteQuestion> entry : voteQuestions.entrySet()) {
            VoteQuestion voteQuestion = entry.getValue();
            if (!voteQuestion.justStarted()) continue;
            voteQuestion.setSent((byte)2);
            Players.sendVotingOpen(voteQuestion);
        }
    }

    public static final void handleArchiveTickets() {
        for (VoteQuestion vq : voteQuestions.values()) {
            if (vq.getArchiveState() != 3) continue;
            VoteQuestions.deleteVoteQuestion(vq.getQuestionId());
        }
    }
}

