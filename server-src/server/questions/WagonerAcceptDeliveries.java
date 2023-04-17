/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.WagonerDeliveriesQuestion;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WagonerAcceptDeliveries
extends Question
implements MonetaryConstants {
    private static final Logger logger = Logger.getLogger(WagonerAcceptDeliveries.class.getName());
    private static final String red = "color=\"255,127,127\";";
    private final Item waystone;
    private long deliveryId = -10L;
    private int sortBy = 1;
    private int pageNo = 1;

    public WagonerAcceptDeliveries(Creature aResponder, Item waystone) {
        super(aResponder, "Wagoner Accept Delivery Management", "Wagoner Accept Delivery Management", 146, waystone.getWurmId());
        this.waystone = waystone;
    }

    public WagonerAcceptDeliveries(Creature aResponder, Item waystone, long deliveryId, int sortBy, int pageNo) {
        super(aResponder, "Wagoner Accept Delivery Management", "Wagoner Accept Delivery Management", 146, waystone.getWurmId());
        this.waystone = waystone;
        this.deliveryId = deliveryId;
        this.sortBy = sortBy;
        this.pageNo = pageNo;
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        switch (this.pageNo) {
            case 1: {
                boolean close = this.getBooleanProp("close");
                if (close) {
                    return;
                }
                boolean next = this.getBooleanProp("next");
                if (next) {
                    String sel = aAnswer.getProperty("sel");
                    this.deliveryId = Long.parseLong(sel);
                    if (this.deliveryId == -10L) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("You decide to do nothing.");
                        return;
                    }
                    this.pageNo = 2;
                    this.sortBy = 1;
                } else {
                    for (String key : this.getAnswer().stringPropertyNames()) {
                        if (!key.startsWith("sort")) continue;
                        String sid = key.substring(4);
                        this.sortBy = Integer.parseInt(sid);
                        break;
                    }
                }
                WagonerAcceptDeliveries wad = new WagonerAcceptDeliveries(this.getResponder(), this.waystone, this.deliveryId, this.sortBy, this.pageNo);
                switch (this.pageNo) {
                    case 1: {
                        wad.sendQuestion();
                        break;
                    }
                    case 2: {
                        wad.sendQuestion2();
                    }
                }
                return;
            }
            case 2: {
                boolean back = this.getBooleanProp("back");
                if (back) {
                    WagonerAcceptDeliveries wad = new WagonerAcceptDeliveries(this.getResponder(), this.waystone, this.deliveryId, this.sortBy, 1);
                    wad.sendQuestion();
                    return;
                }
                boolean accept = this.getBooleanProp("accept");
                if (accept) {
                    Delivery delivery = Delivery.getDelivery(this.deliveryId);
                    long rmoney = this.getResponder().getMoney();
                    if (rmoney < delivery.getReceiverCost()) {
                        this.getResponder().getCommunicator().sendServerMessage("You cannot afford that delivery.", 255, 127, 127);
                        return;
                    }
                    boolean passed = true;
                    if (delivery.getReceiverCost() > 0L) {
                        try {
                            passed = this.getResponder().chargeMoney(delivery.getReceiverCost());
                            if (passed) {
                                Change change = Economy.getEconomy().getChangeFor(this.getResponder().getMoney());
                                this.getResponder().getCommunicator().sendNormalServerMessage("You now have " + change.getChangeString() + " in the bank.");
                                this.getResponder().getCommunicator().sendNormalServerMessage("If this amount is incorrect, please wait a while since the information may not immediately be updated.");
                            }
                        }
                        catch (IOException e) {
                            passed = false;
                            this.getResponder().getCommunicator().sendServerMessage("Something went wrong!", 255, 127, 127);
                            logger.log(Level.WARNING, e.getMessage(), e);
                        }
                    }
                    if (passed) {
                        delivery.setAccepted(this.waystone.getWurmId());
                        this.getResponder().getCommunicator().sendServerMessage("Delivery accepted from " + delivery.getSenderName() + ".", 127, 255, 127);
                        try {
                            Player player = Players.getInstance().getPlayer(delivery.getSenderId());
                            player.getCommunicator().sendServerMessage("Delivery accepted by " + delivery.getReceiverName() + ".", 127, 255, 127);
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {
                            // empty catch block
                        }
                    }
                    return;
                }
                boolean reject = this.getBooleanProp("reject");
                if (!reject) break;
                Delivery delivery = Delivery.getDelivery(this.deliveryId);
                delivery.setRejected();
                this.getResponder().getCommunicator().sendNormalServerMessage("Delivery rejected from " + delivery.getSenderName());
                try {
                    Player player = Players.getInstance().getPlayer(delivery.getSenderId());
                    player.getCommunicator().sendNormalServerMessage("Delivery rejected by " + delivery.getReceiverName());
                }
                catch (NoSuchPlayerException noSuchPlayerException) {}
                break;
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The following bulk deliveries are waiting, but you need to accept them first before their delivery can start.\"}");
        buf.append("text{text=\"If an delivery has a Cash On Delivery (C.O.D) cost associated with it, you have to pay upfront before the delivery will start, the monies will be held by the wagoner and will only be paid to the supplier when the delivery is complete.\"}");
        long money = this.getResponder().getMoney();
        if (money <= 0L) {
            buf.append("text{text='You have no money in the bank.'}");
        } else {
            buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
        }
        Delivery[] deliveries = Delivery.getWaitingDeliveries(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 1: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getSenderName().compareTo(param2.getSenderName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getReceiverName().compareTo(param2.getReceiverName()) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getStateName().compareTo(param2.getStateName()) * upDown;
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        if (param1.getCrates() < param2.getCrates()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
                break;
            }
            case 5: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getWagonerName().compareTo(param2.getWagonerName()) * upDown;
                    }
                });
                break;
            }
            case 6: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getWagonerState().compareTo(param2.getWagonerState()) * upDown;
                    }
                });
                break;
            }
            case 7: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        if (param1.getReceiverCost() < param2.getReceiverCost()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
            }
        }
        buf.append("label{text=\"Select which delivery to view \"};");
        buf.append("table{rows=\"1\";cols=\"9\";label{text=\"\"};" + this.colHeader("Sender", 1, this.sortBy) + this.colHeader("Receiver", 2, this.sortBy) + this.colHeader("Delivery State", 3, this.sortBy) + this.colHeader("# Crates", 4, this.sortBy) + this.colHeader("Wagoner", 5, this.sortBy) + this.colHeader("Wagoner State", 6, this.sortBy) + this.colHeader("Cost", 7, this.sortBy) + "label{type=\"bold\";text=\"\"};");
        String noneSelected = "selected=\"true\";";
        for (Delivery delivery : deliveries) {
            boolean sameWaystone = delivery.getCollectionWaystoneId() == this.waystone.getWurmId();
            boolean connected = !sameWaystone && PathToCalculate.isWaystoneConnected(delivery.getCollectionWaystoneId(), this.waystone.getWurmId());
            String selected = "";
            if (this.deliveryId == delivery.getDeliveryId()) {
                selected = "selected=\"true\";";
                noneSelected = "";
            }
            buf.append((connected ? "radio{group=\"sel\";id=\"" + delivery.getDeliveryId() + "\";" + selected + "text=\"\"};" : "label{text=\"  \"};") + "label{text=\"" + delivery.getSenderName() + "\"};label{text=\"" + delivery.getReceiverName() + "\"};label{text=\"" + delivery.getStateName() + "\"};label{text=\"" + delivery.getCrates() + "\"};label{text=\"" + delivery.getWagonerName() + "\"};label{text=\"" + delivery.getWagonerState() + "\"};label{text=\"" + new Change(delivery.getReceiverCost()).getChangeShortString() + "\"};");
            if (sameWaystone) {
                buf.append("label{color=\"255,127,127\";text=\"same waystone\";hover=\"Waystone is the collection one, no need for a wagoner.\"}");
                continue;
            }
            if (!connected) {
                buf.append("label{color=\"255,127,127\";text=\"no route!\";hover=\"No route found from collection waystone to this waystone.\"}");
                continue;
            }
            buf.append("label{text=\"\"}");
        }
        buf.append("}");
        buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
        buf.append("text{text=\"\"}");
        if (this.waystone.getData() != -1L) {
            Wagoner wagoner = Wagoner.getWagoner(this.waystone.getData());
            if (wagoner == null) {
                logger.log(Level.WARNING, "wagoner (" + this.waystone.getData() + ") not found that was associated with waystone " + this.waystone.getWurmId() + " @" + this.waystone.getTileX() + "," + this.waystone.getTileY() + "," + this.waystone.isOnSurface());
                this.waystone.setData(-1L);
            } else {
                buf.append("label{color=\"255,127,127\";text=\"This waystone is the home of " + wagoner.getName() + " and they wont allow deliveries here.\"};");
                buf.append("harray{button{text=\"Close\";id=\"close\"}}");
            }
        }
        if (this.waystone.getData() == -1L) {
            Village village;
            VolaTile vt = Zones.getTileOrNull(this.waystone.getTileX(), this.waystone.getTileY(), this.waystone.isOnSurface());
            Village village2 = village = vt != null ? vt.getVillage() : null;
            if (village != null && !village.isActionAllowed((short)605, this.getResponder())) {
                buf.append("label{color=\"255,127,127\";text=\"You need Load permissions to be able to accept a delivery here!\"};");
                buf.append("harray{button{text=\"Close\";id=\"close\"}}");
            } else {
                buf.append("harray{label{text=\"Continue to \"};button{text=\"Next\";id=\"next\"}label{text=\" screen to view selected delivery.\"};}");
            }
        }
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion2() {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        if (delivery == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
            this.pageNo = 1;
            this.sendQuestion();
            return;
        }
        this.pageNo = 2;
        long money = this.getResponder().getMoney();
        boolean connected = PathToCalculate.isWaystoneConnected(delivery.getCollectionWaystoneId(), this.waystone.getWurmId());
        boolean hasAccept = money >= delivery.getReceiverCost() && connected;
        String buffer = WagonerDeliveriesQuestion.showDelivery(delivery, this.getId(), this.getResponder(), true, !connected, hasAccept, true, false);
        this.getResponder().getCommunicator().sendBml(400, 400, true, true, buffer, 200, 200, 200, this.title);
    }
}

