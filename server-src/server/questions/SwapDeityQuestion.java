/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.GmTool;
import com.wurmonline.server.questions.Question;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SwapDeityQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(GmTool.class.getName());
    final Deity playerDeity;

    public SwapDeityQuestion(Creature aResponder) {
        super(aResponder, "Switch deity", "Which other deity do you want to change your deity to?", 110, -10L);
        this.playerDeity = aResponder.getDeity();
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        String did = answers.getProperty("did");
        int deityid = Integer.parseInt(did);
        Player player = (Player)this.getResponder();
        if (deityid != this.playerDeity.getNumber()) {
            Deity deity = Deities.getDeity(deityid);
            try {
                player.setDeity(deity);
                player.getCommunicator().sendNormalServerMessage("You are now a follower of " + deity.name + ".");
                if (deity.getNumber() == 4) {
                    if (player.getKingdomTemplateId() != 3) {
                        player.setKingdomId((byte)3);
                        player.setAlignment(Math.min(-50.0f, player.getAlignment()));
                        player.getCommunicator().sendNormalServerMessage("You are now with the Horde of the Summoned.");
                    }
                } else if (player.getAlignment() < 0.0f) {
                    if (player.getKingdomId() == 3) {
                        if (player.getCurrentTile().getKingdom() != 0) {
                            player.setKingdomId(player.getCurrentTile().getKingdom());
                        } else {
                            player.setKingdomId((byte)4);
                        }
                    }
                    player.setAlignment(50.0f);
                }
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        if (this.playerDeity == null) {
            buf.append("label{type=\"bold\";text=\"You are not following any deity.}");
            buf.append("label{text=\"so therefore cannot change it!}");
        } else {
            buf.append("label{text=\"\"}");
            buf.append("label{type=\"bold\";text=\"Select your replacement deity.\"}");
            buf.append("label{type=\"italic\";text=\"Note that this is a once only option, and is not reverseable.\"}");
            buf.append("label{text=\"\"}");
            Deity[] deitys = Deities.getDeities();
            buf.append("table{rows=\"1\";cols=\"2\";");
            for (Deity d : deitys) {
                if (!Servers.isThisAPvpServer() && d.getNumber() == 4) continue;
                buf.append("radio{group=\"did\";id=\"" + d.getNumber() + "\"};label{text=\"" + d.getName() + (d.getNumber() == this.playerDeity.getNumber() ? " (No Change)" : "") + "\"};");
            }
            buf.append("}");
            buf.append("label{text=\"\"}");
            buf.append(this.createAnswerButton2());
        }
        this.getResponder().getCommunicator().sendBml(350, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

