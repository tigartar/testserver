/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.questions.Question;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ManageFriends
extends Question {
    private static final Logger logger = Logger.getLogger(ManageFriends.class.getName());
    private final Friend[] friends;
    private final Player player = (Player)this.getResponder();
    private static final String line = "label{type=\"bold\";text=\"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\"}";

    public ManageFriends(Creature aResponder) {
        super(aResponder, aResponder.getName() + "'s List of Friends", "Manage Your List of Friends", 118, -10L);
        this.friends = this.player.getFriends();
        Arrays.sort(this.friends);
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 118) {
            String add;
            for (int i = 0; i < this.friends.length; ++i) {
                String remove = aAnswer.getProperty("rem" + i);
                if (remove == null || !remove.equalsIgnoreCase("true")) continue;
                this.player.removeFriend(this.friends[i].getFriendId());
                this.player.removeMeFromFriendsList(this.friends[i].getFriendId(), this.friends[i].getName());
                ManageFriends mf = new ManageFriends(this.getResponder());
                mf.sendQuestion();
                return;
            }
            String reply = aAnswer.getProperty("reply");
            if (reply != null && reply.equalsIgnoreCase("true")) {
                String cat = aAnswer.getProperty("cat");
                String category = Friend.Category.catFromInt(Integer.parseInt(cat)).name();
                String wffn = this.player.waitingForFriend();
                if (wffn.length() > 0) {
                    this.player.getCommunicator().addFriend(wffn, category);
                    ManageFriends mf = new ManageFriends(this.getResponder());
                    mf.sendQuestion();
                    return;
                }
                this.player.getCommunicator().sendNormalServerMessage("Too slow! Noone is waiting for a reply anymore.");
            }
            if ((add = aAnswer.getProperty("add")) != null && add.equalsIgnoreCase("true")) {
                String addname = aAnswer.getProperty("addname");
                String cat = aAnswer.getProperty("addcat");
                String category = Friend.Category.catFromInt(Integer.parseInt(cat)).name();
                if (addname.length() < 3) {
                    this.player.getCommunicator().sendNormalServerMessage("Name is too short");
                } else {
                    this.player.getCommunicator().addFriend(addname, category);
                }
                ManageFriends mf = new ManageFriends(this.getResponder());
                mf.sendQuestion();
                return;
            }
            String update = aAnswer.getProperty("update");
            if (update != null && update.equalsIgnoreCase("true")) {
                boolean didChange = false;
                for (int i = 0; i < this.friends.length; ++i) {
                    String cat = aAnswer.getProperty("cat" + i);
                    String note = aAnswer.getProperty("note" + i);
                    if (cat == null) continue;
                    byte catId = Byte.parseByte(cat);
                    if (this.friends[i].getCatId() == catId && this.friends[i].getNote().equals(note)) continue;
                    ((Player)this.getResponder()).updateFriendData(this.friends[i].getFriendId(), catId, note);
                    if (this.friends[i].getCatId() != catId) {
                        this.getResponder().getCommunicator().sendNormalServerMessage(this.friends[i].getName() + " is now in your category " + Friend.Category.catFromInt(catId).name() + ".");
                    }
                    if (!this.friends[i].getNote().equals(note)) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You added a note for " + this.friends[i].getName() + ".");
                    }
                    didChange = true;
                }
                if (!didChange) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("You decide not to do anything.");
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        boolean notFound = false;
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"\"};");
        int row = 0;
        String blank = "image{src=\"img.gui.bridge.blank\";size=\"200,1\";text=\"\"}";
        for (Friend friend : this.friends) {
            PlayerState pState = PlayerInfoFactory.getPlayerState(friend.getFriendId());
            String pName = "Not found";
            byte cat = friend.getCatId();
            if (pState == null) {
                notFound = true;
            } else {
                pName = pState.getPlayerName();
            }
            buf.append("harray{varray{image{src=\"img.gui.bridge.blank\";size=\"200,1\";text=\"\"}label{text=\"" + pName + "\"}};radio{group=\"cat" + row + "\";id=\"3\";selected=\"" + (cat == 3) + "\";text=\"Trusted \";hover=\"Trusted\"}radio{group=\"cat" + row + "\";id=\"2\";selected=\"" + (cat == 2) + "\";text=\"Friend \";hover=\"Friend\"}radio{group=\"cat" + row + "\";id=\"1\";selected=\"" + (cat == 1) + "\";text=\"Contact \";hover=\"Contact\"}radio{group=\"cat" + row + "\";id=\"0\";selected=\"" + (cat == 0) + "\";text=\"Other \";hover=\"Other\"}harray{label{text=\" \"};button{id=\"rem" + row + "\";text=\"Remove\";confirm=\"You are about to remove " + pName + "  from your friends list.\";question=\"Do you really want to do that?\";hover=\"remove " + pName + " from your friends list\"}}}");
            buf.append("input{maxchars=\"40\";id=\"note" + row + "\";text=\"" + friend.getNote() + "\"};");
            ++row;
        }
        buf.append("text{text=\"\"};");
        buf.append("harray{button{text=\"Update Friends\";id=\"update\"}};");
        buf.append("text{text=\"\"};");
        if (notFound) {
            buf.append("label{text=\"'Not Found' could be the result of a server being offline.\"};");
        }
        buf.append("label{text=\"Note 'Remove' is immediate, but does double check.\"};");
        buf.append(line);
        String wffn = this.player.waitingForFriend();
        if (wffn.length() != 0) {
            if (this.player.askingFriend()) {
                buf.append("text{text=\"You are still waiting for a response from " + wffn + ".\"};");
            } else {
                buf.append("label{text=\"" + wffn + " is waiting for you to add them to their list of friends. \"};");
                buf.append("harray{button{text=\"Send Reply\";id=\"reply\"};label{text=\" and add them to \"};dropdown{id=\"cat\";default=\"0\";options=\"Other,Contacts,Friends,Trusted\"}label{text=\" category.\"}};");
            }
        } else {
            buf.append("harray{button{text=\"Send Request\";id=\"add\"};label{text=\" to \"};input{maxchars=\"40\";id=\"addname\";onenter=\"add\"};label{text=\" so can add them to your \"};dropdown{id=\"addcat\";default=\"0\";options=\"Other,Contacts,Friends,Trusted\"}label{text=\" category.\"}};");
        }
        buf.append("text{text=\"\"}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

