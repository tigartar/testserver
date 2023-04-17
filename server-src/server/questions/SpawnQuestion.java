/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Features;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class SpawnQuestion
extends Question {
    private final List<Spawnpoint> spawnpoints = new LinkedList<Spawnpoint>();
    private final Map<Integer, Integer> servers = new HashMap<Integer, Integer>();

    public SpawnQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 34, aTarget);
    }

    @Override
    public void sendQuestion() {
        int x;
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        Set<Spawnpoint> spawnPoints = ((Player)this.getResponder()).spawnpoints;
        if (spawnPoints == null || spawnPoints.isEmpty()) {
            ((Player)this.getResponder()).calculateSpawnPoints();
            spawnPoints = ((Player)this.getResponder()).spawnpoints;
        }
        if (spawnPoints != null && !spawnPoints.isEmpty()) {
            x = 0;
            buf.append("dropdown{id='spawnpoint';options=\"");
            for (Spawnpoint sp : spawnPoints) {
                if (x > 0) {
                    buf.append(",");
                }
                this.spawnpoints.add(sp);
                buf.append(sp.description);
                ++x;
            }
            buf.append("\"};");
        } else {
            buf.append("label{text=\"No valid spawn points found. Wait and try again using the /respawn command or send to start at the startpoint.\"};");
        }
        if (Servers.localServer.EPIC && Servers.localServer.getKingdom() != this.getResponder().getKingdomId()) {
            buf.append("label{text=\"You may also select to spawn on another Epic server.\"};");
            x = 0;
            this.servers.put(x, 0);
            buf.append("dropdown{id='eserver';options=\"None");
            for (ServerEntry s : Servers.getAllServers()) {
                if (!s.EPIC || !s.isAvailable(0, this.getResponder().isPaying()) || s.getId() == Servers.localServer.id || s.getKingdom() != 0 && s.getKingdom() != this.getResponder().getKingdomId() || !this.getResponder().isPaying() && s.ISPAYMENT) continue;
                buf.append(",");
                buf.append(s.getName());
                this.servers.put(++x, s.getId());
            }
            buf.append("\"};");
        }
        if (Features.Feature.FREE_ITEMS.isEnabled()) {
            buf.append("text{text=''};text{text=''};");
            buf.append("label{text=\"Do you require a weapon QL 40 or a rope?\"};");
            buf.append("dropdown{id='weapon';options=\"No");
            buf.append(",Long Sword + Shield,Two Handed Sword, Large Axe + Shield, Huge Axe, Medium Maul + Shield, Large Maul, Halberd, Long Spear");
            buf.append("\"};");
            buf.append("text{text=''};text{text=''};");
            buf.append("label{text=\"You may also select to spawn with some armour.\"};");
            buf.append("dropdown{id='armour';options=\"None");
            buf.append(",Chain (QL 40), Leather (QL 60), Plate (QL 20)");
            buf.append("\"};");
        }
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, Servers.localServer.isChallengeServer() ? 500 : 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    final Map<Integer, Integer> getServerEntries() {
        return this.servers;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseSpawnQuestion(this);
    }

    Spawnpoint getSpawnpoint(int aIndex) {
        if (this.spawnpoints.isEmpty()) {
            return null;
        }
        return this.spawnpoints.get(aIndex);
    }
}

