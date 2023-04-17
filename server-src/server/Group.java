/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Message;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.MapAnnotation;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group {
    protected final Map<String, Creature> members = new HashMap<String, Creature>();
    protected String name;
    protected static final Logger logger = Logger.getLogger(Group.class.getName());

    public Group(String aName) {
        this.name = aName;
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Creating a Group - Name: " + this.name);
        }
    }

    public String getName() {
        return this.name;
    }

    protected void setName(String newName) {
        this.name = newName;
    }

    public boolean contains(Creature c) {
        return this.members.get(c.getName()) != null;
    }

    public boolean isTeam() {
        return false;
    }

    public final void addMember(String aName, Creature aMember) {
        if (!this.members.values().contains(aMember)) {
            this.members.put(aName, aMember);
        }
    }

    public final void dropMember(String aName) {
        this.members.remove(aName);
    }

    int getNumberOfMembers() {
        return this.members != null ? this.members.size() : 0;
    }

    public final void sendMessage(Message message) {
        for (Creature c : this.members.values()) {
            Creature sender = message.getSender();
            if (sender != null && c.isIgnored(sender.getWurmId())) continue;
            c.getCommunicator().sendMessage(message);
        }
    }

    public final void sendMapAnnotation(MapAnnotation[] annotations) {
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendMapAnnotations(annotations);
        }
    }

    public final void sendRemoveMapAnnotation(MapAnnotation annotation2) {
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendRemoveMapAnnotation(annotation2.getId(), annotation2.getType(), annotation2.getServer());
        }
    }

    public final void sendClearMapAnnotationsOfType(byte type) {
        for (Creature c : this.members.values()) {
            c.getCommunicator().sendClearMapAnnotationsOfType(type);
        }
    }

    public final void broadCastSafe(String message) {
        this.broadCastSafe(message, (byte)0);
    }

    public final void broadCastSafe(String message, byte messageType) {
        for (Creature player : this.members.values()) {
            player.getCommunicator().sendSafeServerMessage(message, messageType);
        }
    }

    public final void broadCastAlert(String message, byte messageType) {
        for (Creature player : this.members.values()) {
            player.getCommunicator().sendAlertServerMessage(message, messageType);
        }
    }

    public final void broadCastNormal(String message) {
        for (Creature player : this.members.values()) {
            player.getCommunicator().sendNormalServerMessage(message);
        }
    }

    public boolean containsOfflineMember(long wurmid) {
        return false;
    }
}

