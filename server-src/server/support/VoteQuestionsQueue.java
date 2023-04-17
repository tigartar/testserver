/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.Players;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;

public class VoteQuestionsQueue {
    public static final byte ADD = 0;
    public static final byte DELETE = 1;
    public static final byte CLOSE = 2;
    public static final byte SETCARDID = 3;
    public static final byte SETARCHIVESTATE = 4;
    private final byte action;
    private final int questionId;
    private VoteQuestion voteQuestion;
    private long newVoteEnd;
    private String newTrelloCardId;
    private byte newArchiveState;

    public VoteQuestionsQueue(byte aAction, VoteQuestion aVoteQuestion) {
        this.questionId = aVoteQuestion.getQuestionId();
        this.voteQuestion = aVoteQuestion;
        this.action = aAction;
    }

    public VoteQuestionsQueue(byte aAction, int aQuestionId) {
        this.questionId = aQuestionId;
        this.action = aAction;
    }

    public VoteQuestionsQueue(byte aAction, int aQuestionId, long aNewEnd) {
        this.questionId = aQuestionId;
        this.action = aAction;
        this.newVoteEnd = aNewEnd;
    }

    public VoteQuestionsQueue(byte aAction, int aQuestionId, String aNewTrelloCardId) {
        this.questionId = aQuestionId;
        this.action = aAction;
        this.newTrelloCardId = aNewTrelloCardId;
    }

    public VoteQuestionsQueue(byte aAction, int aQuestionId, byte aNewArchiveState) {
        this.questionId = aQuestionId;
        this.action = aAction;
        this.newArchiveState = aNewArchiveState;
    }

    public void action() {
        switch (this.action) {
            case 0: {
                VoteQuestions.addVoteQuestion(this.voteQuestion, true);
                for (Player p : Players.getInstance().getPlayers()) {
                    if (!this.voteQuestion.canVote(p)) continue;
                    p.addPlayerVote(new PlayerVote(p.getWurmId(), this.voteQuestion.getQuestionId(), false, false, false, false));
                    p.gotVotes(true);
                }
                break;
            }
            case 1: {
                VoteQuestions.deleteVoteQuestion(this.questionId);
                break;
            }
            case 2: {
                VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
                if (vq == null) break;
                vq.closeVoting(this.newVoteEnd);
                break;
            }
            case 3: {
                VoteQuestion vq1 = VoteQuestions.getVoteQuestion(this.questionId);
                if (vq1 == null) break;
                vq1.setTrelloCardId(this.newTrelloCardId);
                break;
            }
            case 4: {
                VoteQuestion vq2 = VoteQuestions.getVoteQuestion(this.questionId);
                if (vq2 == null) break;
                vq2.setArchiveState(this.newArchiveState);
                break;
            }
        }
    }
}

