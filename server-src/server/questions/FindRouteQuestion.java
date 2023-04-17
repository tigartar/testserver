/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayFinder;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;
import java.util.Arrays;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FindRouteQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(FindRouteQuestion.class.getName());
    public String villageName = "";
    private Village[] villages;
    private Player player;

    public FindRouteQuestion(Creature aResponder, Item waystone) {
        super(aResponder, "Find a route", "Find a route", 139, waystone.getWurmId());
        this.player = aResponder.isPlayer() ? (Player)this.getResponder() : null;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 139) {
            Village village;
            block15: {
                village = null;
                this.villageName = this.getAnswer().getProperty("vname");
                this.villageName = this.villageName.replaceAll("\"", "");
                this.villageName = this.villageName.trim();
                if (this.villageName.length() > 3) {
                    void var4_6;
                    this.villageName = StringUtilities.raiseFirstLetter(this.villageName);
                    StringTokenizer tokens = new StringTokenizer(this.villageName);
                    String string = tokens.nextToken();
                    while (tokens.hasMoreTokens()) {
                        String string2 = (String)var4_6 + " " + StringUtilities.raiseFirstLetter(tokens.nextToken());
                    }
                    this.villageName = var4_6;
                }
                if (!this.villageName.isEmpty()) {
                    try {
                        village = Villages.getVillage(this.villageName);
                        if (Routes.getNodesFor(village).length == 0) {
                            this.player.getCommunicator().sendNormalServerMessage("Unable to find connected waystones in " + this.villageName);
                            return;
                        }
                        break block15;
                    }
                    catch (NoSuchVillageException e) {
                        this.player.getCommunicator().sendNormalServerMessage("Unable to find a village with that name: " + this.villageName);
                        return;
                    }
                }
                String clear = this.getAnswer().getProperty("clear");
                if (clear != null && clear.equals("true")) {
                    this.player.setHighwayPath("", null);
                    block5: for (Item waystone : Items.getWaystones()) {
                        VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
                        if (vt == null) continue;
                        for (VirtualZone vz : vt.getWatchers()) {
                            try {
                                if (vz.getWatcher().getWurmId() != this.player.getWurmId()) continue;
                                this.player.getCommunicator().sendWaystoneData(waystone);
                                continue block5;
                            }
                            catch (Exception e) {
                                logger.log(Level.WARNING, e.getMessage(), e);
                            }
                        }
                    }
                    return;
                }
                String string = this.getAnswer().getProperty("vill");
                int vno = Integer.parseInt(string);
                if (this.villages.length == 0 || vno > this.villages.length) {
                    this.player.getCommunicator().sendNormalServerMessage("No village selected!");
                    return;
                }
                village = this.villages[vno];
                this.villageName = village.getName();
            }
            if (village.equals(this.player.getCurrentVillage())) {
                this.player.getCommunicator().sendNormalServerMessage("You are already in that village.");
                return;
            }
            HighwayFinder.queueHighwayFinding(this.player, Routes.getNode(this.target), village, (byte)0);
            this.player.achievement(524);
            return;
        }
    }

    @Override
    public void sendQuestion() {
        if (this.player == null) {
            return;
        }
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        int height = 220;
        if (this.player.getHighwayPathDestination().length() > 0) {
            buf.append("harray{label{text=\"Already heading to: " + this.player.getHighwayPathDestination() + "  \"}button{id=\"clear\";text=\"Clear route\"};}");
            buf.append("label{text=\"\"}");
            height += 50;
        }
        this.villages = Routes.getVillages(this.target);
        buf.append("harray{label{text=\"Find a route to village \"};dropdown{id=\"vill\";options=\"");
        if (this.villages.length == 0) {
            buf.append("None");
        } else {
            Arrays.sort(this.villages);
            for (int i = 0; i < this.villages.length; ++i) {
                if (i > 0) {
                    buf.append(",");
                }
                buf.append(this.villages[i].getName());
            }
        }
        buf.append("\"}}");
        buf.append("text{text=\"You may also specify a village name here to get a route to it.\"}");
        buf.append("harray{input{maxchars=\"40\";id=\"vname\";text=\"\"}}");
        buf.append("text{text=\"Note: The village must have a waystone in it, and be connected to the highway system.\"}");
        buf.append("label{text=\"\"}");
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(400, height, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

