/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Group;
import com.wurmonline.server.Groups;
import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Creature;
import java.util.HashMap;
import java.util.Map;

public class Team
extends Group {
    private Creature leader = null;
    private final Map<Long, Boolean> offlineMembers = new HashMap<Long, Boolean>();

    public Team(String aName, Creature _leader) {
        super(aName);
        this.leader = _leader;
    }

    @Override
    public boolean isTeam() {
        return true;
    }

    public boolean isTeamLeader(Creature c) {
        return c == this.leader;
    }

    public Creature[] getMembers() {
        return this.members.values().toArray(new Creature[this.members.size()]);
    }

    public final void setNewLeader(Creature newLeader) {
        this.leader = newLeader;
        Message m = new Message(newLeader, newLeader == this.leader ? (byte)14 : 13, "Team", newLeader.getName() + " has been appointed new leader.");
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendRemoveTeam(newLeader.getName());
            c.getCommunicator().sendAddTeam(newLeader.getName(), newLeader.getWurmId());
            c.getCommunicator().sendMessage(m);
        }
    }

    public final void creatureJoinedTeam(Creature joined) {
        this.addMember(joined.getName(), joined);
        Message m = new Message(joined, joined == this.leader ? (byte)14 : 13, "Team", "Welcome to team chat.");
        joined.getCommunicator().sendMessage(m);
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendAddTeam(joined.getName(), joined.getWurmId());
            joined.getCommunicator().sendAddTeam(c.getName(), c.getWurmId());
        }
        if (this.offlineMembers.containsKey(joined.getWurmId())) {
            Boolean mayInvite = this.offlineMembers.remove(joined.getWurmId());
            joined.setMayInviteTeam(mayInvite);
        }
    }

    public final void creatureReconnectedTeam(Creature joined) {
        Message m = new Message(joined, joined == this.leader ? (byte)14 : 13, "Team", "Welcome to team chat.");
        joined.getCommunicator().sendMessage(m);
        for (Creature c : this.members.values()) {
            joined.getCommunicator().sendAddTeam(c.getName(), c.getWurmId());
        }
    }

    public final void creaturePartedTeam(Creature parted, boolean sendRemove) {
        Creature[] s;
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendRemoveTeam(parted.getName());
            if (!sendRemove) continue;
            parted.getCommunicator().sendRemoveTeam(c.getName());
        }
        this.dropMember(parted.getName());
        if (this.members.size() == 1) {
            s = this.getMembers();
            s[0].getCommunicator().sendNormalServerMessage("The team has dissolved.");
            s[0].setTeam(null, true);
        } else if (this.members.size() > 1) {
            if (parted == this.leader) {
                s = this.getMembers();
                this.setNewLeader(s[0]);
                if (!sendRemove) {
                    this.offlineMembers.put(parted.getWurmId(), parted.mayInviteTeam());
                }
            }
        } else {
            Groups.removeGroup(this.name);
        }
    }

    public final void sendTeamMessage(Creature sender, Message message) {
        for (Creature c : this.members.values()) {
            if (c.isIgnored(message.getSender().getWurmId())) continue;
            c.getCommunicator().sendMessage(message);
        }
    }

    @Override
    public boolean containsOfflineMember(long wurmid) {
        return this.offlineMembers.keySet().contains(wurmid);
    }

    public final void sendTeamMessage(Creature sender, String message) {
        Message m = new Message(sender, sender == this.leader ? (byte)14 : 13, "Team", "<" + sender.getName() + "> " + message);
        for (Creature c : this.members.values()) {
            if (c.isIgnored(m.getSender().getWurmId())) continue;
            c.getCommunicator().sendMessage(m);
        }
    }
}

