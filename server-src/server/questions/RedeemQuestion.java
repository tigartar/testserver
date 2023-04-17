/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.questions.Question;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedeemQuestion
extends Question
implements TimeConstants {
    private static final Logger logger = Logger.getLogger(RedeemQuestion.class.getName());

    public RedeemQuestion(Creature aResponder, String aTitle, String aQuestion) {
        super(aResponder, aTitle, aQuestion, 87, aResponder.getWurmId());
    }

    @Override
    public void answer(Properties aAnswers) {
        String key = "plays";
        String val = aAnswers.getProperty("plays");
        try {
            PlayerInfo pinf;
            long wid = Long.parseLong(val);
            if (wid > 0L && (pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wid)) != null) {
                Set<Item> items;
                if (pinf.hasMovedInventory()) {
                    Items.returnItemsFromFreezerFor(wid);
                }
                if ((items = Items.loadAllItemsForCreatureWithId(wid, false)).size() > 0) {
                    try {
                        Item chest = ItemFactory.createItem(192, 50 + Server.rand.nextInt(30), pinf.getName());
                        for (Item i : items) {
                            if (i.isInventory() || i.isBodyPart() || i.isHomesteadDeed() || i.isNewDeed() || i.isVillageDeed() || i.getTemplateId() == 166) continue;
                            chest.insertItem(i, true);
                        }
                        this.getResponder().getInventory().insertItem(chest, true);
                        this.getResponder().getCommunicator().sendNormalServerMessage("You redeem the items for " + pinf.getName() + " and put them in a nice chest.");
                        this.getResponder().getLogger().info(this.getResponder().getName() + " redeems the items of " + pinf.getName() + ".");
                        logger.log(Level.INFO, this.getResponder().getName() + " redeems the items of " + pinf.getName() + ".");
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, ex.getMessage(), ex);
                    }
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("There were no items for " + pinf.getName() + ".");
                }
            }
        }
        catch (NumberFormatException nfn) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Unknown player " + val);
        }
    }

    @Override
    public void sendQuestion() {
        PlayerInfo[] plays = PlayerInfoFactory.getPlayerInfos();
        String lHtml = this.getBmlHeader();
        StringBuilder buf = new StringBuilder(lHtml);
        buf.append("text{text=\"This functionality will retrieve all non-deed and non-writ items from a banned player and put them in a chest in your inventory.\"}");
        buf.append("text{text=\"The suggestion is to use the hide functionality to get a pair of random coordinates to put it at or simply hide it at the suggested location.\"}");
        buf.append("text{text=\"Only players with at least a year of bannination left will be listed here.\"}");
        buf.append("text{text=\"Select a banned player from which to redeem items:\"}");
        buf.append("radio{group=\"plays\";id=\"-1\";text=\"None\";selected=\"true\"};");
        for (int x = 0; x < plays.length; ++x) {
            if (!plays[x].isBanned() || plays[x].getCurrentServer() != Servers.localServer.id || plays[x].banexpiry - System.currentTimeMillis() <= 29030400000L) continue;
            buf.append("radio{group=\"plays\";id=\"" + plays[x].wurmId + "\";text=\"" + plays[x].getName() + "\"};");
        }
        buf.append(this.createAnswerButton3());
        this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

