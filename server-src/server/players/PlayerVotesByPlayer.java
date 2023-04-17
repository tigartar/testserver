/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.server.players.PlayerVote;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerVotesByPlayer {
    private static Logger logger = Logger.getLogger(PlayerVotesByPlayer.class.getName());
    private final Map<Integer, PlayerVote> playerQuestionVotes = new ConcurrentHashMap<Integer, PlayerVote>();

    public PlayerVotesByPlayer() {
    }

    public PlayerVotesByPlayer(PlayerVote pv) {
        this.add(pv);
    }

    public void add(PlayerVote pv) {
        this.playerQuestionVotes.put(pv.getQuestionId(), pv);
    }

    public void remove(int questionId) {
        if (this.playerQuestionVotes.containsKey(questionId)) {
            this.playerQuestionVotes.remove(questionId);
        }
    }

    public PlayerVote get(int qId) {
        return this.playerQuestionVotes.get(qId);
    }

    public boolean containsKey(int qId) {
        return this.playerQuestionVotes.containsKey(qId);
    }

    public PlayerVote[] getVotes() {
        return this.playerQuestionVotes.values().toArray(new PlayerVote[this.playerQuestionVotes.size()]);
    }
}

