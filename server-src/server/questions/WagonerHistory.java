/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.questions.Question;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WagonerHistory
extends Question {
    private static final Logger logger = Logger.getLogger(WagonerHistory.class.getName());
    private final Wagoner wagoner;
    private long deliveryId = -10L;
    private int sortBy = 1;
    private int pageNo = 1;
    private boolean inQueue = true;
    private boolean waitAccept = true;
    private boolean inProgress = true;
    private boolean delivered = true;
    private boolean rejected = true;
    private boolean cancelled = true;

    public WagonerHistory(Creature aResponder, Wagoner wagoner) {
        super(aResponder, "History of " + wagoner.getName(), "History of " + wagoner.getName(), 148, wagoner.getWurmId());
        this.wagoner = wagoner;
    }

    public WagonerHistory(Creature aResponder, Wagoner wagoner, long deliveryId, int sortBy, int pageNo, boolean inQueue, boolean waitAccept, boolean inProgress, boolean delivered, boolean rejected, boolean cancelled) {
        super(aResponder, "History of " + wagoner.getName(), "History of " + wagoner.getName(), 148, wagoner.getWurmId());
        this.wagoner = wagoner;
        this.deliveryId = deliveryId;
        this.sortBy = sortBy;
        this.pageNo = pageNo;
        this.inQueue = inQueue;
        this.waitAccept = waitAccept;
        this.inProgress = inProgress;
        this.delivered = delivered;
        this.rejected = rejected;
        this.cancelled = cancelled;
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 148) {
            boolean close = this.getBooleanProp("close");
            if (close) {
                return;
            }
            boolean filter = this.getBooleanProp("filter");
            if (filter) {
                this.inQueue = this.getBooleanProp("inqueue");
                this.waitAccept = this.getBooleanProp("waitaccept");
                this.inProgress = this.getBooleanProp("inprogress");
                this.delivered = this.getBooleanProp("delivered");
                this.rejected = this.getBooleanProp("rejected");
                this.cancelled = this.getBooleanProp("cancelled");
            } else {
                for (String key : this.getAnswer().stringPropertyNames()) {
                    if (!key.startsWith("sort")) continue;
                    String sid = key.substring(4);
                    this.sortBy = Integer.parseInt(sid);
                    break;
                }
            }
            WagonerHistory wh = new WagonerHistory(this.getResponder(), this.wagoner, this.deliveryId, this.sortBy, this.pageNo, this.inQueue, this.waitAccept, this.inProgress, this.delivered, this.rejected, this.cancelled);
            wh.sendQuestion();
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeaderWithScrollAndQuestion());
        buf.append("label{text=\"\"}");
        Delivery[] deliveries = Delivery.getDeliveriesFor(this.target, this.inQueue, this.waitAccept, this.inProgress, this.rejected, this.delivered);
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
                        return param1.getSenderCostString().compareTo(param2.getSenderCostString()) * upDown;
                    }
                });
                break;
            }
            case 6: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        return param1.getReceiverCostString().compareTo(param2.getReceiverCostString()) * upDown;
                    }
                });
                break;
            }
            case 7: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        if (param1.getWhenDelivered() < param2.getWhenDelivered()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
            }
        }
        buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + this.colHeader("Sender", 1, this.sortBy) + this.colHeader("Receiver", 2, this.sortBy) + this.colHeader("Delivery State", 3, this.sortBy) + this.colHeader("# Crates", 4, this.sortBy) + this.colHeader("Sender Costs", 5, this.sortBy) + this.colHeader("Receiver Costs", 6, this.sortBy) + this.colHeader("When Delivered", 7, this.sortBy));
        for (Delivery delivery : deliveries) {
            buf.append("label{text=\"\"};label{text=\"" + delivery.getSenderName() + "\"};label{text=\"" + delivery.getReceiverName() + "\"};label{text=\"" + delivery.getStateName() + "\"};label{text=\"" + delivery.getCrates() + "\"};label{text=\"" + delivery.getSenderCostString() + "\"};label{text=\"" + delivery.getReceiverCostString() + "\"};label{text=\"" + delivery.getStringDelivered() + "\"};");
        }
        buf.append("}");
        buf.append("text{text=\"\"}");
        buf.append("harray{button{text=\"Filter\";id=\"filter\"};label{text=\" by \"}checkbox{id=\"waitaccept\";text=\"Waiting for accept  \"" + (this.waitAccept ? ";selected=\"true\"" : "") + "};checkbox{id=\"inqueue\";text=\"In queue  \"" + (this.inQueue ? ";selected=\"true\"" : "") + "};checkbox{id=\"inprogress\";text=\"In Progress  \"" + (this.inProgress ? ";selected=\"true\"" : "") + "};checkbox{id=\"delivered\";text=\"Delivered  \"" + (this.delivered ? ";selected=\"true\"" : "") + "};checkbox{id=\"rejected\";text=\"Rejected  \"" + (this.rejected ? ";selected=\"true\"" : "") + "};checkbox{id=\"cancelled\";text=\"Cancelled \"" + (this.cancelled ? ";selected=\"true\"" : "") + "};};");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(550, 500, true, true, buf.toString(), 200, 200, 200, this.title);
    }
}

