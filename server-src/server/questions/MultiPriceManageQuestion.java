/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.TradeHandler;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionParser;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MultiPriceManageQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(MultiPriceManageQuestion.class.getName());
    private final Map<Long, Integer> itemMap = new HashMap<Long, Integer>();

    public MultiPriceManageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 23, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        QuestionParser.parseMultiPriceQuestion(this);
    }

    public Map<Long, Integer> getItemMap() {
        return this.itemMap;
    }

    @Override
    public void sendQuestion() {
        try {
            int idx = 0;
            Creature trader = Server.getInstance().getCreature(this.target);
            if (trader.isNpcTrader()) {
                Shop shop = Economy.getEconomy().getShop(trader);
                if (shop == null) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("No shop registered for that creature.");
                } else if (shop.getOwnerId() == this.getResponder().getWurmId()) {
                    Object[] items = trader.getInventory().getAllItems(false);
                    Arrays.sort(items);
                    int removed = 0;
                    for (int x = 0; x < items.length; ++x) {
                        if (!((Item)items[x]).isFullprice()) continue;
                        ++removed;
                    }
                    String lHtml = this.getBmlHeader();
                    StringBuilder buf = new StringBuilder(lHtml);
                    DecimalFormat df = new DecimalFormat("#.##");
                    buf.append("text{text=\"" + trader.getName() + " may put up " + (TradeHandler.getMaxNumPersonalItems() - trader.getNumberOfShopItems()) + " more items for sale.\"}");
                    buf.append("text{type=\"bold\";text=\"Prices for " + trader.getName() + "\"}text{text=''}");
                    buf.append("table{rows=\"" + (items.length - removed + 1) + "\"; cols=\"7\";label{text=\"Item name\"};label{text=\"QL\"};label{text=\"DMG\"};label{text=\"Gold\"};label{text=\"Silver\"};label{text=\"Copper\"};label{text=\"Iron\"}");
                    for (int x = 0; x < items.length; ++x) {
                        if (((Item)items[x]).isFullprice()) continue;
                        long wid = ((Item)items[x]).getWurmId();
                        ++idx;
                        Change change = Economy.getEconomy().getChangeFor(((Item)items[x]).getPrice());
                        buf.append(MultiPriceManageQuestion.itemNameWithColorByRarity((Item)items[x]));
                        buf.append("label{text=\"" + df.format(((Item)items[x]).getQualityLevel()) + "\"};");
                        buf.append("label{text=\"" + df.format(((Item)items[x]).getDamage()) + "\"};");
                        buf.append("harray{input{maxchars=\"3\"; id=\"" + idx + "g\";text=\"" + change.getGoldCoins() + "\"};label{text=\" \"}};");
                        buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "s\";text=\"" + change.getSilverCoins() + "\"};label{text=\" \"}};");
                        buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "c\";text=\"" + change.getCopperCoins() + "\"};label{text=\" \"}};");
                        buf.append("harray{input{maxchars=\"2\"; id=\"" + idx + "i\";text=\"" + change.getIronCoins() + "\"};label{text=\" \"}}");
                        this.itemMap.put(new Long(wid), idx);
                    }
                    buf.append("}");
                    buf.append(this.createAnswerButton2());
                    this.getResponder().getCommunicator().sendBml(500, 300, true, true, buf.toString(), 200, 200, 200, this.title);
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("You don't own that shop.");
                }
            }
        }
        catch (NoSuchCreatureException nsc) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such creature.");
            logger.log(Level.WARNING, this.getResponder().getName(), nsc);
        }
        catch (NoSuchPlayerException nsp) {
            this.getResponder().getCommunicator().sendNormalServerMessage("No such creature.");
            logger.log(Level.WARNING, this.getResponder().getName(), nsp);
        }
    }
}

