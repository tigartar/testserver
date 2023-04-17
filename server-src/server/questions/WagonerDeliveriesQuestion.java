/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.highways.PathToCalculate;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.util.MaterialUtilities;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class WagonerDeliveriesQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(WagonerDeliveriesQuestion.class.getName());
    private static final DecimalFormat df = new DecimalFormat("#0.00");
    private long deliveryId;
    private int sortBy = 1;
    private int pageNo = 1;
    private boolean hasBack = false;
    private final boolean hasList;

    public WagonerDeliveriesQuestion(Creature aResponder, long deliveryId, boolean hasList) {
        super(aResponder, WagonerDeliveriesQuestion.getTitle(deliveryId), WagonerDeliveriesQuestion.getTitle(deliveryId), 147, deliveryId);
        this.deliveryId = deliveryId;
        this.hasList = hasList;
    }

    public WagonerDeliveriesQuestion(Creature aResponder, long deliveryId, boolean hasList, int sortBy, int pageNo, boolean hasBack) {
        super(aResponder, WagonerDeliveriesQuestion.getTitle(deliveryId), WagonerDeliveriesQuestion.getTitle(deliveryId), 147, deliveryId);
        this.deliveryId = deliveryId;
        this.hasList = hasList;
        this.sortBy = sortBy;
        this.pageNo = pageNo;
        this.hasBack = hasBack;
    }

    private static String getTitle(long deliveryId) {
        if (deliveryId == -10L) {
            return "Wagoner Deliveries Management";
        }
        return "Wagoner Delivery Management";
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        switch (this.pageNo) {
            case 1: {
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
                    this.hasBack = true;
                } else {
                    for (String key : this.getAnswer().stringPropertyNames()) {
                        if (key.startsWith("sort")) {
                            String sid = key.substring(4);
                            this.sortBy = Integer.parseInt(sid);
                            break;
                        }
                        if (!key.startsWith("assign")) continue;
                        String sid = key.substring(6);
                        this.deliveryId = Integer.parseInt(sid);
                        WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(this.getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
                        wdq.sendQuestion3();
                        return;
                    }
                }
                WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(this.getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
                switch (this.pageNo) {
                    case 1: {
                        wdq.sendQuestion();
                        break;
                    }
                    case 2: {
                        wdq.sendQuestion2();
                    }
                }
                return;
            }
            case 2: {
                boolean cancel = this.getBooleanProp("cancel");
                if (cancel) {
                    Delivery delivery = Delivery.getDelivery(this.deliveryId);
                    if (delivery != null && delivery.getState() == 0) {
                        delivery.setCancelled();
                        this.getResponder().getCommunicator().sendServerMessage("You have cancelled your delivery to " + delivery.getReceiverName() + ".", 255, 127, 127);
                    } else if (delivery == null) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Delivery cannot be found!");
                    } else {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Delivery may not be cancelled as it has been accepted.");
                    }
                    return;
                }
                boolean assign = this.getBooleanProp("assign");
                if (assign) {
                    WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(this.getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
                    wdq.sendQuestion3();
                    return;
                }
                boolean back = this.getBooleanProp("back");
                if (back) {
                    WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(this.getResponder(), -10L, true, 1, 1, false);
                    wdq.sendQuestion();
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
                catch (NoSuchPlayerException player) {}
                break;
            }
            case 3: {
                boolean close = this.getBooleanProp("close");
                if (close) {
                    return;
                }
                boolean cancel = this.getBooleanProp("cancel");
                if (cancel) {
                    Delivery delivery = Delivery.getDelivery(this.deliveryId);
                    delivery.setCancelledNoWagoner();
                } else {
                    String sel = aAnswer.getProperty("sel");
                    long selId = Long.parseLong(sel);
                    if (selId == -10L) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("No wagoner selected.");
                    } else {
                        Delivery delivery = Delivery.getDelivery(this.deliveryId);
                        delivery.setWagonerId(selId);
                    }
                }
                WagonerDeliveriesQuestion wdq = new WagonerDeliveriesQuestion(this.getResponder(), this.deliveryId, this.hasList, this.sortBy, this.pageNo, this.hasBack);
                if (this.hasList) {
                    wdq.sendQuestion();
                    break;
                }
                wdq.sendQuestion2();
            }
        }
    }

    @Override
    public void sendQuestion() {
        this.pageNo = 1;
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("text{text=\"\"}");
        Delivery[] deliveries = Delivery.getPendingDeliveries(this.getResponder().getWurmId());
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 7: {
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
            case 1: {
                Arrays.sort(deliveries, new Comparator<Delivery>(){

                    @Override
                    public int compare(Delivery param1, Delivery param2) {
                        if (param1.getWhen() < param2.getWhen()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
            }
        }
        buf.append("label{text=\"Select which delivery to view \"};");
        buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + this.colHeader("Sender", 7, this.sortBy) + this.colHeader("Receiver", 2, this.sortBy) + this.colHeader("Delivery State", 3, this.sortBy) + this.colHeader("# Crates", 4, this.sortBy) + this.colHeader("Wagoner", 5, this.sortBy) + this.colHeader("Wagoner State", 6, this.sortBy) + this.colHeader("Last state change", 1, this.sortBy));
        String noneSelected = "selected=\"true\";";
        for (Delivery delivery : deliveries) {
            String wagonerState;
            String wagonerName;
            String rad = delivery.canSeeCrates() ? "radio{group=\"sel\";id=\"" + delivery.getDeliveryId() + "\"text=\"\"}" : "label{text=\"  \"};";
            if (delivery.getWagonerId() == -10L && delivery.isQueued()) {
                wagonerName = "button{text=\"Assign wagoner\";id=\"assign" + delivery.getDeliveryId() + "\"}";
                wagonerState = "label{text=\"\"};";
            } else {
                wagonerName = "label{text=\"" + delivery.getWagonerName() + "\"};";
                wagonerState = "label{text=\"" + delivery.getWagonerState() + "\"};";
            }
            buf.append(rad + "label{text=\"" + delivery.getSenderName() + "\"};label{text=\"" + delivery.getReceiverName() + "\"};label{text=\"" + delivery.getStateName() + "\"};label{text=\"" + delivery.getCrates() + "\"};" + wagonerName + wagonerState + "label{text=\"" + delivery.getStringWhen() + "\"}");
        }
        buf.append("}");
        buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
        buf.append("text{text=\"\"}");
        buf.append("harray{label{text=\"Continue to \"};button{text=\"Next\";id=\"next\"}label{text=\" screen to view selected delivery.\"};}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(600, 400, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    public void sendQuestion2() {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        if (delivery == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
            if (this.hasBack) {
                this.pageNo = 1;
                this.sendQuestion();
            }
            return;
        }
        this.pageNo = 2;
        boolean hasCancel = delivery.getSenderId() == this.getResponder().getWurmId() && delivery.getState() == 0;
        boolean hasReject = delivery.getReceiverId() == this.getResponder().getWurmId() && delivery.getState() == 0;
        Creature creature = delivery.getReceiverId() == this.getResponder().getWurmId() ? this.getResponder() : null;
        String buffer = WagonerDeliveriesQuestion.showDelivery(delivery, this.getId(), creature, this.hasBack, false, false, hasReject, hasCancel);
        this.getResponder().getCommunicator().sendBml(400, 400, true, true, buffer, 200, 200, 200, this.title);
    }

    public void sendQuestion3() {
        Delivery delivery = Delivery.getDelivery(this.deliveryId);
        if (delivery == null) {
            this.getResponder().getCommunicator().sendNormalServerMessage("Delivery not found!");
            if (this.hasBack) {
                this.pageNo = 1;
                this.sendQuestion();
            }
            return;
        }
        this.pageNo = 3;
        Set<Creature> creatureSet = Creatures.getMayUseWagonersFor(this.getResponder());
        HashSet<Distanced> wagonerSet = new HashSet<Distanced>();
        float dist = PathToCalculate.getRouteDistance(delivery.getCollectionWaystoneId(), delivery.getDeliveryWaystoneId());
        if (dist != 99999.0f) {
            for (Creature creature : creatureSet) {
                Wagoner wagoner = Wagoner.getWagoner(creature.getWurmId());
                if (wagoner == null || (dist = PathToCalculate.getRouteDistance(wagoner.getHomeWaystoneId(), delivery.getCollectionWaystoneId())) == 99999.0f) continue;
                boolean isPublic = creature.publicMayUse(this.getResponder());
                String villName = "";
                Village vill = creature.getCitizenVillage();
                if (vill != null) {
                    villName = vill.getName();
                }
                wagonerSet.add(new Distanced(wagoner, isPublic, villName, dist));
            }
        }
        Distanced[] wagonerArr = wagonerSet.toArray(new Distanced[wagonerSet.size()]);
        int absSortBy = Math.abs(this.sortBy);
        int upDown = Integer.signum(this.sortBy);
        switch (absSortBy) {
            case 1: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        return param1.getWagoner().getName().compareTo(param2.getWagoner().getName()) * upDown;
                    }
                });
                break;
            }
            case 2: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        return param1.getType().compareTo(param2.getType()) * upDown;
                    }
                });
                break;
            }
            case 3: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        return param1.getVillageName().compareTo(param2.getVillageName()) * upDown;
                    }
                });
                break;
            }
            case 4: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        if (param1.getDistance() < param2.getDistance()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
                break;
            }
            case 5: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        return param1.getWagoner().getStateName().compareTo(param2.getWagoner().getStateName()) * upDown;
                    }
                });
                break;
            }
            case 6: {
                Arrays.sort(wagonerArr, new Comparator<Distanced>(){

                    @Override
                    public int compare(Distanced param1, Distanced param2) {
                        if (param1.getQueueLength() < param2.getQueueLength()) {
                            return 1 * upDown;
                        }
                        return -1 * upDown;
                    }
                });
            }
        }
        int height = 300;
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        buf.append("closebutton{id=\"close\"};");
        buf.append("label{text=\"Select which wagoner to use \"};");
        buf.append("table{rows=\"1\";cols=\"8\";label{text=\"\"};" + this.colHeader("Name", 1, this.sortBy) + this.colHeader("Type", 2, this.sortBy) + this.colHeader("Village", 3, this.sortBy) + this.colHeader("Distance", 4, this.sortBy) + this.colHeader("State", 5, this.sortBy) + this.colHeader("Queue", 6, this.sortBy) + "label{type=\"bold\";text=\"\"};");
        String noneSelected = "selected=\"true\";";
        for (Distanced distanced : wagonerArr) {
            buf.append(distanced.getVillageName().length() == 0 ? "label{text=\"\"}" : "radio{group=\"sel\";id=\"" + distanced.getWagoner().getWurmId() + "\";text=\"\"}label{text=\"" + distanced.getWagoner().getName() + "\"};label{text=\"" + distanced.getType() + "\"};label{text=\"" + distanced.getVillageName() + "\"};label{text=\"" + (int)distanced.getDistance() + "\"};label{text=\"" + distanced.getWagoner().getStateName() + "\"};label{text=\"" + distanced.getQueueLength() + "\"};label{text=\"\"}");
        }
        buf.append("}");
        buf.append("radio{group=\"sel\";id=\"-10\";" + noneSelected + "text=\" None\"}");
        buf.append("text{text=\"\"}");
        String assignButton = wagonerArr.length > 0 ? "button{text=\"Assign\";id=\"assign\"};label{text=\" \"};" : "";
        buf.append("harray{" + assignButton + "button{text=\"Cancel Delivery\";id=\"cancel\";hover=\"This will remove this delivery from the wagoner queue\";confirm=\"You are about to cancel a delivery to " + delivery.getReceiverName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};button{text=\"Back to Delivery\";id=\"back\"}label{text=\" \"};button{text=\"Close\";id=\"close\"}}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(420, height += wagonerArr.length * 20, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    static String showDelivery(Delivery delivery, int questionId, @Nullable Creature creature, boolean hasBack, boolean isNotConnected, boolean hasAccept, boolean hasReject, boolean hasCancel) {
        StringBuilder buf = new StringBuilder();
        buf.append("border{scroll{vertical='true';horizontal='false';varray{rescale='true';passthrough{id='id';text='" + questionId + "'}");
        buf.append("text{text=\"Delivery of " + delivery.getCrates() + " crate" + (delivery.getCrates() == 1 ? "" : "s") + " from " + delivery.getSenderName() + " to " + delivery.getReceiverName() + ".\"}");
        String wagonerName = delivery.getWagonerId() == -10L && delivery.isQueued() ? "harray{label{text=\"Using \"};button{text=\"Assign wagoner\";id=\"assign\"};label{text=\" \"};}" : "text{text=\"Using " + delivery.getWagonerName() + ".\"}";
        buf.append(wagonerName);
        if (isNotConnected) {
            buf.append("label{text=\"This waystone is not connected to the collection waystone, so cannot accept it here.\"}");
        } else if (creature != null) {
            long money = creature.getMoney();
            if (money <= 0L) {
                buf.append("text{text='You have no money in the bank.'}");
            } else {
                buf.append("text{text='You have " + new Change(money).getChangeString() + " in the bank.'}");
            }
        }
        buf.append("}};null;");
        buf.append("tree{id=\"t1\";cols=\"3\";showheader=\"true\";height=\"300\"col{text=\"QL\";width=\"50\"};col{text=\"DMG\";width=\"50\"};col{text=\"Weight\";width=\"50\"};");
        Item crateContainer = delivery.getCrateContainer();
        if (crateContainer != null) {
            Item[] crates;
            for (Item crate : crates = crateContainer.getItemsAsArray()) {
                buf.append(WagonerDeliveriesQuestion.addCrate(crate));
            }
        }
        buf.append("}");
        buf.append("null;varray{");
        if (delivery.getSenderCost() > 0L) {
            buf.append("label{text=\"The delivery fees of " + new Change(delivery.getSenderCost()).getChangeString() + " have been paid for by " + delivery.getSenderName() + ".\"}");
        } else {
            int deliveryFees = delivery.getCrates() * 100;
            buf.append("label{text=\"The delivery fees of " + new Change(deliveryFees).getChangeString() + " has been added into the goods cost.\"}");
        }
        if (delivery.getReceiverCost() == 0L) {
            buf.append("label{text=\"The goods have already been paid for by " + delivery.getSenderName() + ".\"}");
        } else if (delivery.getState() == 0) {
            buf.append("label{text=\"The goods cost of " + new Change(delivery.getReceiverCost()).getChangeString() + " have yet to be agreed by " + delivery.getReceiverName() + ".\"}");
        } else {
            buf.append("label{text=\"The goods cost of " + new Change(delivery.getReceiverCost()).getChangeString() + " has been paid for by " + delivery.getReceiverName() + ".\"}");
        }
        buf.append("label{text=\"All monies are held by the wagoner until the delivery is complete.\"}");
        buf.append("harray{" + (hasAccept ? "button{text=\"Accept\";id=\"accept\"};label{text=\" \"};" : "") + (hasReject ? "button{text=\"Reject\";id=\"reject\"};label{text=\" \"};" : "") + (!hasAccept && !hasReject ? "button{text=\"Close\";id=\"submit\"};label{text=\" \"};" : "") + (hasCancel ? "button{text=\"Cancel Delivery\";id=\"cancel\";hover=\"This will remove this delivery from the wagoner queue\";confirm=\"You are about to cancel a delivery to " + delivery.getReceiverName() + ".\";question=\"Do you really want to do that?\"}label{text=\" \"};" : "") + (hasBack ? "button{text=\"Back to list\";id=\"back\"};" : "") + "}");
        buf.append("text=\"\"}}");
        return buf.toString();
    }

    private static String addCrate(Item crate) {
        String itemName;
        StringBuilder buf = new StringBuilder();
        String sQL = "" + df.format(crate.getQualityLevel());
        String sDMG = "" + df.format(crate.getDamage());
        String sWeight = "" + df.format((float)crate.getFullWeight(true) / 1000.0f);
        String hover = itemName = WagonerDeliveriesQuestion.longItemName(crate);
        Item[] contained = crate.getItemsAsArray();
        int children = contained.length;
        buf.append("row{id=\"" + crate.getWurmId() + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"" + crate.getRarity() + "\";children=\"" + children + "\";col{text=\"" + sQL + "\"};col{text=\"" + sDMG + "\"};col{text=\"" + sWeight + "\"}}");
        for (Item bulkItem : contained) {
            buf.append(WagonerDeliveriesQuestion.addBulkItem(bulkItem));
        }
        return buf.toString();
    }

    private static String addBulkItem(Item bulkItem) {
        String itemName;
        StringBuilder buf = new StringBuilder();
        String sQL = "" + df.format(bulkItem.getQualityLevel());
        String sWeight = "" + df.format((float)bulkItem.getFullWeight(true) / 1000.0f);
        String hover = itemName = WagonerDeliveriesQuestion.longItemName(bulkItem);
        buf.append("row{id=\"" + bulkItem.getWurmId() + "\";hover=\"" + hover + "\";name=\"" + itemName + "\";rarity=\"0\";children=\"0\";col{text=\"" + sQL + "\"};col{text=\"0.00\"};col{text=\"" + sWeight + "\"}}");
        return buf.toString();
    }

    public static String longItemName(Item litem) {
        StringBuilder sb = new StringBuilder();
        if (litem.getRarity() == 1) {
            sb.append("rare ");
        } else if (litem.getRarity() == 2) {
            sb.append("supreme ");
        } else if (litem.getRarity() == 3) {
            sb.append("fantastic ");
        }
        String name = litem.getName().length() == 0 ? litem.getTemplate().getName() : litem.getName();
        MaterialUtilities.appendNameWithMaterialSuffix(sb, name.replace("\"", "''"), litem.getMaterial());
        if (litem.getDescription().length() > 0) {
            sb.append(" (" + litem.getDescription() + ")");
        }
        return sb.toString();
    }

    class Distanced {
        private final Wagoner wagoner;
        private final boolean isPublic;
        private final String villageName;
        private final float distance;
        private final int queueLength;

        Distanced(Wagoner wagoner, boolean isPublic, String villageName, float distance) {
            this.wagoner = wagoner;
            this.isPublic = isPublic;
            this.villageName = villageName;
            this.distance = distance;
            this.queueLength = Delivery.getQueueLength(wagoner.getWurmId());
        }

        Wagoner getWagoner() {
            return this.wagoner;
        }

        float getDistance() {
            return this.distance;
        }

        boolean isPublic() {
            return this.isPublic;
        }

        String getType() {
            if (this.isPublic) {
                return "Public";
            }
            return "Private";
        }

        String getVillageName() {
            return this.villageName;
        }

        int getQueueLength() {
            return this.queueLength;
        }
    }
}

