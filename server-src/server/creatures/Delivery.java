/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Delivery
implements MiscConstants,
Comparable<Delivery> {
    private static final Logger logger = Logger.getLogger(Wagoner.class.getName());
    public static final byte STATE_WAITING_FOR_ACCEPT = 0;
    public static final byte STATE_QUEUED = 1;
    public static final byte STATE_WAITING_FOR_PICKUP = 2;
    public static final byte STATE_BEING_DELIVERED = 3;
    public static final byte STATE_DELIVERED = 4;
    public static final byte STATE_REJECTING = 5;
    public static final byte STATE_TIMEING_OUT = 6;
    public static final byte STATE_COMPLETED = 7;
    public static final byte STATE_REJECTED = 8;
    public static final byte STATE_CANCELLING = 9;
    public static final byte STATE_CANCELLED = 10;
    public static final byte STATE_TIMED_OUT = 11;
    public static final byte STATE_CANCELLING_NO_WAGONER = 12;
    private final SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    private static final String CREATE_DELIVERY = "INSERT INTO DELIVERYQUEUE (STATE,COLLECTION_WAYSTONE_ID,CONTAINER_ID,CRATES,SENDER_ID,SENDER_COST,RECEIVER_ID,RECEIVER_COST,DELIVERY_WAYSTONE_ID,WAGONER_ID,TS_EXPIRY,TS_WAITING_FOR_ACCEPT,TS_ACCEPTED_OR_REJECTED,TS_DELIVERY_STARTED,TS_PICKED_UP,TS_DELIVERED) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_DELIVERY_ACCEPT = "UPDATE DELIVERYQUEUE SET STATE=?,DELIVERY_WAYSTONE_ID=?,TS_ACCEPTED_OR_REJECTED=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_REJECT = "UPDATE DELIVERYQUEUE SET STATE=?,TS_ACCEPTED_OR_REJECTED=?,WAGONER_ID=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_STARTED = "UPDATE DELIVERYQUEUE SET STATE=?,TS_DELIVERY_STARTED=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_PICKED_UP = "UPDATE DELIVERYQUEUE SET STATE=?,TS_PICKED_UP=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_DELIVERED = "UPDATE DELIVERYQUEUE SET STATE=?,TS_DELIVERED=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_STATE = "UPDATE DELIVERYQUEUE SET STATE=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_WAGONER = "UPDATE DELIVERYQUEUE SET WAGONER_ID=? WHERE DELIVERY_ID=?";
    private static final String UPDATE_DELIVERY_CONTAINER = "UPDATE DELIVERYQUEUE SET CONTAINER_ID=? WHERE DELIVERY_ID=?";
    private static final String DELETE_DELIVERY = "DELETE FROM DELIVERYQUEUE WHERE DELIVERY_ID=?";
    private static final String GET_ALL_DELIVERIES = "SELECT * FROM DELIVERYQUEUE ORDER BY DELIVERY_ID";
    private static final Map<Long, Delivery> deliveryQueue = new ConcurrentHashMap<Long, Delivery>();
    private static final Map<Long, Delivery> containerDelivery = new ConcurrentHashMap<Long, Delivery>();
    private final long deliveryId;
    private byte state;
    private final long collectionWaystoneId;
    private long containerId;
    private final int crates;
    private final long senderId;
    private final long senderCost;
    private final long receiverId;
    private final long receiverCost;
    private long deliveryWaystoneId;
    private long wagonerId;
    private final long tsExpiry;
    private long tsWaitingForAccept = 0L;
    private long tsAcceptedOrRejected = 0L;
    private long tsDeliveryStarted = 0L;
    private long tsPickedUp = 0L;
    private long tsDelivered = 0L;
    private long lastChecked = 0L;

    public Delivery(long deliveryId, byte state, long collectionWaystoneId, long containerId, int crates, long senderId, long senderCost, long receiverId, long receiverCost, long deliveryWaystoneId, long wagonerId, long tsExpiry, long tsWaitingForAccept, long tsAcceptedOrRejected, long tsDeliveryStarted, long tsPickedUp, long tsDelivered) {
        this.deliveryId = deliveryId;
        this.state = state;
        this.collectionWaystoneId = collectionWaystoneId;
        this.containerId = containerId;
        this.crates = crates;
        this.senderId = senderId;
        this.senderCost = senderCost;
        this.receiverId = receiverId;
        this.receiverCost = receiverCost;
        this.deliveryWaystoneId = deliveryWaystoneId;
        this.wagonerId = wagonerId;
        this.tsExpiry = tsExpiry;
        this.tsWaitingForAccept = tsWaitingForAccept;
        this.tsAcceptedOrRejected = tsAcceptedOrRejected;
        this.tsDeliveryStarted = tsDeliveryStarted;
        this.tsPickedUp = tsPickedUp;
        this.tsDelivered = tsDelivered;
        Delivery.addDelivery(this);
        if (containerId != -10L) {
            containerDelivery.put(containerId, this);
        }
    }

    @Override
    public int compareTo(Delivery otherDelivery) {
        if (this.getWhenAcceptedOrRejected() == otherDelivery.getWhenAcceptedOrRejected()) {
            return 0;
        }
        if (this.getWhenAcceptedOrRejected() < otherDelivery.getWhenAcceptedOrRejected()) {
            return -1;
        }
        return 1;
    }

    public long getDeliveryId() {
        return this.deliveryId;
    }

    public byte getState() {
        return this.state;
    }

    public long getCollectionWaystoneId() {
        return this.collectionWaystoneId;
    }

    public long getContainerId() {
        return this.containerId;
    }

    public void clrContainerId() {
        this.containerId = -10L;
        Delivery.dbUpdateDeliveryContainer(this.deliveryId, this.containerId);
    }

    public int getCrates() {
        return this.crates;
    }

    public long getSenderId() {
        return this.senderId;
    }

    public String getSenderName() {
        return PlayerInfoFactory.getPlayerName(this.senderId);
    }

    public long getSenderCost() {
        return this.senderCost;
    }

    public String getSenderCostString() {
        if (this.senderCost <= 0L) {
            return "none";
        }
        return new Change(this.senderCost).getChangeShortString();
    }

    public long getReceiverId() {
        return this.receiverId;
    }

    public String getReceiverName() {
        return PlayerInfoFactory.getPlayerName(this.receiverId);
    }

    public long getReceiverCost() {
        return this.receiverCost;
    }

    public String getReceiverCostString() {
        if (this.receiverCost <= 0L) {
            return "none";
        }
        return new Change(this.receiverCost).getChangeShortString();
    }

    public long getDeliveryWaystoneId() {
        return this.deliveryWaystoneId;
    }

    public long getWagonerId() {
        return this.wagonerId;
    }

    public String getWagonerName() {
        Wagoner wag = Wagoner.getWagoner(this.wagonerId);
        if (wag == null) {
            return "on vacation!";
        }
        return wag.getName();
    }

    public String getWagonerState() {
        Wagoner wag = Wagoner.getWagoner(this.wagonerId);
        if (wag == null) {
            return "on strike!";
        }
        if (wag.getDeliveryId() != this.deliveryId && wag.getDeliveryId() != -10L) {
            return "busy";
        }
        return wag.getStateName();
    }

    @Nullable
    public Item getCrateContainer() {
        long crateContainerId = -10L;
        Wagoner wag = Wagoner.getWagoner(this.wagonerId);
        if (wag == null) {
            crateContainerId = this.containerId;
        } else if (wag.getDeliveryId() != this.deliveryId) {
            crateContainerId = this.containerId;
        } else {
            switch (wag.getState()) {
                case 0: 
                case 1: 
                case 2: 
                case 3: 
                case 4: 
                case 5: {
                    crateContainerId = this.containerId;
                    break;
                }
                case 7: {
                    crateContainerId = wag.getWagonId();
                    break;
                }
                default: {
                    crateContainerId = -10L;
                }
            }
        }
        if (crateContainerId == -10L) {
            return null;
        }
        try {
            return Items.getItem(crateContainerId);
        }
        catch (NoSuchItemException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    public boolean canSeeCrates() {
        switch (this.state) {
            case 0: 
            case 1: 
            case 2: 
            case 3: {
                return true;
            }
        }
        return false;
    }

    public boolean isPreDelivery() {
        switch (this.state) {
            case 0: 
            case 1: 
            case 2: 
            case 3: {
                return true;
            }
        }
        return false;
    }

    public boolean isQueued() {
        switch (this.state) {
            case 1: {
                return true;
            }
        }
        return false;
    }

    public boolean isWaitingForAccept() {
        switch (this.state) {
            case 0: {
                return true;
            }
        }
        return false;
    }

    public boolean canViewDelivery() {
        switch (this.state) {
            case 0: 
            case 1: 
            case 2: {
                return true;
            }
        }
        return false;
    }

    public boolean inProgress() {
        switch (this.state) {
            case 2: 
            case 3: {
                return true;
            }
        }
        return false;
    }

    public boolean isComplete() {
        switch (this.state) {
            case 4: 
            case 7: {
                return true;
            }
        }
        return false;
    }

    public boolean wasRejected() {
        switch (this.state) {
            case 5: 
            case 6: 
            case 8: 
            case 11: {
                return true;
            }
        }
        return false;
    }

    public long getWhen() {
        switch (this.state) {
            case 0: {
                return this.tsWaitingForAccept;
            }
            case 1: {
                return this.tsAcceptedOrRejected;
            }
            case 2: {
                return this.tsDeliveryStarted;
            }
            case 3: {
                return this.tsPickedUp;
            }
            case 4: {
                return this.tsDelivered;
            }
            case 5: 
            case 6: 
            case 7: 
            case 8: 
            case 9: 
            case 10: 
            case 11: 
            case 12: {
                return this.tsAcceptedOrRejected;
            }
        }
        return 0L;
    }

    public String getStringWhen() {
        long ts = this.getWhen();
        if (ts == 0L) {
            return "";
        }
        return this.df.format(new Date(ts));
    }

    public long getWhenWaitingForAccept() {
        return this.tsWaitingForAccept;
    }

    public String getStringWaitingForAccept() {
        if (this.tsWaitingForAccept == 0L) {
            return "";
        }
        return this.df.format(new Date(this.tsWaitingForAccept));
    }

    public long getWhenAcceptedOrRejected() {
        return this.tsAcceptedOrRejected;
    }

    public String getStringAcceptedOrRejected() {
        if (this.tsAcceptedOrRejected == 0L) {
            return "";
        }
        return this.df.format(new Date(this.tsAcceptedOrRejected));
    }

    public long getWhenDeliveryStarted() {
        return this.tsDeliveryStarted;
    }

    public String getStringDeliveryStarted() {
        if (this.tsDeliveryStarted == 0L) {
            return "";
        }
        return this.df.format(new Date(this.tsDeliveryStarted));
    }

    public long getWhenPickedUp() {
        return this.tsPickedUp;
    }

    public String getStringPickedUp() {
        if (this.tsPickedUp == 0L) {
            return "";
        }
        return this.df.format(new Date(this.tsPickedUp));
    }

    public long getWhenDelivered() {
        return this.tsDelivered;
    }

    public String getStringDelivered() {
        if (this.tsDelivered == 0L) {
            return "";
        }
        return this.df.format(new Date(this.tsDelivered));
    }

    public void remove() {
        Delivery.removeDelivery(this);
    }

    public String getStateName() {
        switch (this.state) {
            case 0: {
                return "waiting for accept";
            }
            case 1: {
                return "queued";
            }
            case 2: {
                return "waiting for pickup";
            }
            case 3: {
                return "being delivered";
            }
            case 4: {
                return "delivered";
            }
            case 5: {
                return "rejecting";
            }
            case 6: {
                return "timing out";
            }
            case 11: {
                return "timed out";
            }
            case 7: {
                return "completed";
            }
            case 8: {
                return "rejected";
            }
            case 9: 
            case 12: {
                return "cancelling";
            }
            case 10: {
                return "cancelled";
            }
        }
        return "";
    }

    public void setAccepted(long waystoneId) {
        this.tsAcceptedOrRejected = System.currentTimeMillis();
        this.state = 1;
        this.deliveryWaystoneId = waystoneId;
        Delivery.dbUpdateDeliveryAccept(this.deliveryId, this.state, this.deliveryWaystoneId, this.tsAcceptedOrRejected);
    }

    public void setRejected() {
        this.tsAcceptedOrRejected = System.currentTimeMillis();
        this.state = (byte)5;
        this.wagonerId = -10L;
        Delivery.dbUpdateDeliveryReject(this.deliveryId, this.state, this.tsAcceptedOrRejected);
        this.updateContainerVisuals();
        this.checkPayment(true);
    }

    public void setCancelled() {
        this.tsAcceptedOrRejected = System.currentTimeMillis();
        this.state = (byte)9;
        this.wagonerId = -10L;
        Delivery.dbUpdateDeliveryReject(this.deliveryId, this.state, this.tsAcceptedOrRejected);
        this.updateContainerVisuals();
        this.checkPayment(true);
    }

    public void setCancelledNoWagoner() {
        this.tsAcceptedOrRejected = System.currentTimeMillis();
        this.state = (byte)12;
        this.wagonerId = -10L;
        Delivery.dbUpdateDeliveryReject(this.deliveryId, this.state, this.tsAcceptedOrRejected);
        this.updateContainerVisuals();
        this.checkPayment(true);
    }

    public void setTimingOut() {
        Player player2;
        this.tsAcceptedOrRejected = System.currentTimeMillis();
        this.state = (byte)6;
        this.wagonerId = -10L;
        Delivery.dbUpdateDeliveryReject(this.deliveryId, this.state, this.tsAcceptedOrRejected);
        this.updateContainerVisuals();
        try {
            player2 = Players.getInstance().getPlayer(this.getSenderId());
            player2.getCommunicator().sendServerMessage("Delivery to " + this.getReceiverName() + " timed out, was not accepted in time.", 255, 127, 127);
        }
        catch (NoSuchPlayerException player2) {
            // empty catch block
        }
        try {
            player2 = Players.getInstance().getPlayer(this.getReceiverId());
            player2.getCommunicator().sendServerMessage("Delivery from " + this.getSenderName() + " timed out, was not accepted in time.", 255, 127, 127);
        }
        catch (NoSuchPlayerException noSuchPlayerException) {
            // empty catch block
        }
        this.checkPayment(true);
    }

    public void setStarted() {
        this.tsDeliveryStarted = System.currentTimeMillis();
        this.state = (byte)2;
        Delivery.dbUpdateDeliveryStarted(this.deliveryId, this.state, this.tsDeliveryStarted);
    }

    public void setPickedUp() {
        this.tsPickedUp = System.currentTimeMillis();
        this.state = (byte)3;
        Delivery.dbUpdateDeliveryPickedUp(this.deliveryId, this.state, this.tsPickedUp);
        this.clrContainerId();
        this.updateContainerVisuals();
    }

    public void setDelivered() {
        this.tsDelivered = System.currentTimeMillis();
        this.state = (byte)4;
        Delivery.dbUpdateDeliveryDelivered(this.deliveryId, this.state, this.tsDelivered);
        int wagonersCut = this.getCrates() * 100;
        Wagoner wagoner = Wagoner.getWagoner(this.wagonerId);
        if (wagoner != null && wagoner.getVillageId() != -1) {
            try {
                Village village = Villages.getVillage(wagoner.getVillageId());
                int deedCut = (int)((float)wagonersCut * 0.2f);
                village.plan.addMoney(deedCut);
                village.plan.addPayment(wagoner.getName(), wagoner.getWurmId(), deedCut);
                Change newch = Economy.getEconomy().getChangeFor(deedCut);
                village.addHistory(wagoner.getName(), "added " + newch.getChangeString() + " to upkeep");
                logger.log(Level.INFO, wagoner.getName() + " added " + deedCut + " irons to " + village.getName() + " upkeep.");
            }
            catch (NoSuchVillageException noSuchVillageException) {
                // empty catch block
            }
        }
        this.checkPayment(true);
    }

    public void updateContainerVisuals() {
        try {
            Item container = Items.getItem(this.containerId);
            container.updateName();
        }
        catch (NoSuchItemException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public String getContainerDescription() {
        String desc = "goods from " + this.getSenderName() + " to " + this.getReceiverName();
        switch (this.state) {
            case 1: {
                if (this.wagonerId == -10L) {
                    return desc + " [NO WAGONER}";
                }
                return desc;
            }
            case 5: 
            case 8: {
                return desc + " [REJECTED]";
            }
            case 9: 
            case 10: {
                return desc + " [CANCELLED]";
            }
            case 6: 
            case 11: {
                return desc + " [TIMED OUT]";
            }
        }
        return desc;
    }

    private boolean pay(long payTo, String payName, long cost, String detail, String message, byte newState) {
        LoginServerWebConnection lsw;
        boolean paid;
        boolean bl = paid = cost <= 0L;
        if (!paid && (lsw = new LoginServerWebConnection()).addMoney(payTo, payName, cost, detail)) {
            paid = true;
            try {
                Player player = Players.getInstance().getPlayer(payTo);
                player.getCommunicator().sendNormalServerMessage(message);
                Change change = Economy.getEconomy().getChangeFor(player.getMoney());
                player.getCommunicator().sendNormalServerMessage("You now have " + change.getChangeString() + " in the bank.");
                player.getCommunicator().sendNormalServerMessage("If this amount is incorrect, please wait a while since the information may not immediately be updated.");
            }
            catch (NoSuchPlayerException noSuchPlayerException) {
                // empty catch block
            }
        }
        if (paid) {
            this.state = newState;
            Delivery.dbUpdateState(this.deliveryId, this.state);
        }
        return paid;
    }

    public void setWagonerId(long wagonerId) {
        this.wagonerId = wagonerId;
        Delivery.dbUpdateDeliveryDismiss(this.deliveryId, this.wagonerId);
        this.updateContainerVisuals();
    }

    void checkPayment(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now < this.lastChecked + 60000L) {
            return;
        }
        this.lastChecked = now;
        switch (this.state) {
            case 0: {
                if (now <= this.tsExpiry) break;
                this.setTimingOut();
                break;
            }
            case 12: {
                boolean paid = this.pay(this.receiverId, this.getReceiverName(), this.receiverCost, "Refund goods cost for delivery " + this.getDeliveryId() + " as cancelled.", this.getSenderName() + " has cancelled the delivery as no wagoner.", (byte)9);
                if (!paid) break;
            }
            case 9: {
                this.pay(this.senderId, this.getSenderName(), this.senderCost, "Refund delivery " + this.getDeliveryId() + " as cancelled.", "You have cancelled the delivery to " + this.getReceiverName() + ".", (byte)10);
                break;
            }
            case 5: {
                this.pay(this.senderId, this.getSenderName(), this.senderCost, "Refund delivery " + this.getDeliveryId() + " as rejected.", "Your delivery to " + this.getReceiverName() + " has been rejected.", (byte)8);
                break;
            }
            case 6: {
                this.pay(this.senderId, this.getSenderName(), this.senderCost, "Refund delivery " + this.getDeliveryId() + " as timed out.", "Your delivery to " + this.getReceiverName() + " has timed out.", (byte)11);
                break;
            }
            case 4: {
                int deliveryCost = this.senderCost == 0L ? this.getCrates() * 100 : 0;
                this.pay(this.senderId, this.getSenderName(), this.receiverCost - (long)deliveryCost, "Delivery " + this.getDeliveryId() + " paid.", "Your delivery to " + this.getReceiverName() + " has been paid.", (byte)7);
                break;
            }
        }
    }

    private static final void removeDelivery(Delivery delivery) {
        deliveryQueue.remove(delivery.getDeliveryId());
        Delivery.dbRemoveDelivery(delivery.getDeliveryId());
    }

    @Nullable
    public static final Delivery getDelivery(long deliveryId) {
        return deliveryQueue.get(deliveryId);
    }

    private static final void addDelivery(Delivery delivery) {
        deliveryQueue.put(delivery.getDeliveryId(), delivery);
    }

    public static final void addDelivery(long collectionWaystoneId, long containerId, int crates, long senderId, long senderCost, long receiverId, long receiverCost, long wagonerId) {
        boolean state = false;
        long deliveryWaystoneId = -10L;
        long tsExpiry = System.currentTimeMillis() + 604800000L;
        long tsWaitingForAccept = System.currentTimeMillis();
        long tsAcceptedOrRejected = 0L;
        long tsDeliveryStarted = 0L;
        long tsPickedUp = 0L;
        long tsDelivered = 0L;
        long deliveryId = Delivery.dbCreateDelivery((byte)0, collectionWaystoneId, containerId, crates, senderId, senderCost, receiverId, receiverCost, -10L, wagonerId, tsExpiry, tsWaitingForAccept, 0L, 0L, 0L, 0L);
        new Delivery(deliveryId, 0, collectionWaystoneId, containerId, crates, senderId, senderCost, receiverId, receiverCost, -10L, wagonerId, tsExpiry, tsWaitingForAccept, 0L, 0L, 0L, 0L);
    }

    public static final Delivery[] getWaitingDeliveries(long playerId) {
        HashSet<Delivery> deliverySet = new HashSet<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isWaitingForAccept() || delivery.getReceiverId() != playerId) continue;
            deliverySet.add(delivery);
        }
        return deliverySet.toArray(new Delivery[deliverySet.size()]);
    }

    public static final Delivery[] getLostDeliveries(long playerId) {
        HashSet<Delivery> deliverySet = new HashSet<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isQueued() || delivery.getSenderId() != playerId || delivery.getWagonerId() != -10L) continue;
            deliverySet.add(delivery);
        }
        return deliverySet.toArray(new Delivery[deliverySet.size()]);
    }

    public static final Delivery[] getPendingDeliveries(long playerId) {
        HashSet<Delivery> deliverySet = new HashSet<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (delivery.getReceiverId() != playerId && delivery.getSenderId() != playerId) continue;
            deliverySet.add(delivery);
        }
        return deliverySet.toArray(new Delivery[deliverySet.size()]);
    }

    public static final int countWaitingForAccept(long wagonerId) {
        int count = 0;
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isWaitingForAccept() || delivery.getWagonerId() != wagonerId) continue;
            ++count;
        }
        return count;
    }

    public static final void rejectWaitingForAccept(long wagonerId) {
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isWaitingForAccept() || delivery.getWagonerId() != wagonerId) continue;
            delivery.setRejected();
        }
    }

    public static final void clrWagonerQueue(long wagonerId) {
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isQueued() || delivery.getWagonerId() != wagonerId) continue;
            delivery.setWagonerId(-10L);
        }
    }

    public static final LinkedList<Delivery> getNextDeliveriesFor(long wagonerId) {
        LinkedList<Delivery> deliveries = new LinkedList<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isQueued() || delivery.getWagonerId() != wagonerId) continue;
            deliveries.add(delivery);
        }
        Collections.sort(deliveries);
        return deliveries;
    }

    public static final boolean hasNextDeliveryFor(long wagonerId) {
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isQueued() || delivery.getWagonerId() != wagonerId) continue;
            return true;
        }
        return false;
    }

    public static final Delivery[] getKnownDeliveries(long playerId) {
        HashSet<Delivery> deliverySet = new HashSet<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (delivery.getSenderId() != playerId && delivery.getReceiverId() != playerId) continue;
            deliverySet.add(delivery);
        }
        return deliverySet.toArray(new Delivery[deliverySet.size()]);
    }

    public static final int getQueueLength(long wagonerId) {
        int queueLength = 0;
        for (Delivery delivery : deliveryQueue.values()) {
            if (!delivery.isQueued() || delivery.getWagonerId() != wagonerId) continue;
            ++queueLength;
        }
        return queueLength;
    }

    public static final Delivery[] getDeliveriesFor(long wagonerId, boolean inQueue, boolean waitAccept, boolean inProgress, boolean rejected, boolean delivered) {
        HashSet<Delivery> deliverySet = new HashSet<Delivery>();
        for (Delivery delivery : deliveryQueue.values()) {
            if (delivery.getWagonerId() != wagonerId) continue;
            if (inQueue && delivery.isQueued()) {
                deliverySet.add(delivery);
            }
            if (waitAccept && delivery.isWaitingForAccept()) {
                deliverySet.add(delivery);
            }
            if (inProgress && delivery.inProgress()) {
                deliverySet.add(delivery);
            }
            if (rejected && delivery.wasRejected()) {
                deliverySet.add(delivery);
            }
            if (!delivered || !delivery.isComplete()) continue;
            deliverySet.add(delivery);
        }
        return deliverySet.toArray(new Delivery[deliverySet.size()]);
    }

    @Nullable
    public static final Delivery getDeliveryFrom(long containerId) {
        return containerDelivery.get(containerId);
    }

    @Nullable
    public static final Delivery canViewDelivery(Item container, Creature creature) {
        for (Delivery delivery : deliveryQueue.values()) {
            if (delivery.getContainerId() != container.getWurmId() || !delivery.canViewDelivery() || delivery.getSenderId() != creature.getWurmId() && creature.getPower() < 2) continue;
            return delivery;
        }
        return null;
    }

    @Nullable
    public static final boolean canUnSealContainer(Item container, Creature creature) {
        Delivery delivery = Delivery.getDeliveryFrom(container.getWurmId());
        return !(delivery == null || delivery.canViewDelivery() && delivery.getWagonerId() != -10L || delivery.getSenderId() != creature.getWurmId() && creature.getPower() < 2);
    }

    public static final boolean isDeliveryPoint(long waystoneId) {
        for (Delivery delivery : deliveryQueue.values()) {
            if (delivery.getDeliveryWaystoneId() != waystoneId || !delivery.isPreDelivery()) continue;
            return true;
        }
        return false;
    }

    public static void freeContainer(long containerId) {
        Delivery delivery = Delivery.getDeliveryFrom(containerId);
        if (delivery != null) {
            delivery.clrContainerId();
            containerDelivery.remove(containerId);
        }
    }

    public static String getContainerDescription(long containerId) {
        Delivery delivery = Delivery.getDeliveryFrom(containerId);
        if (delivery != null) {
            return delivery.getContainerDescription();
        }
        return "";
    }

    public static void poll() {
        for (Delivery delivery : deliveryQueue.values()) {
            delivery.checkPayment(false);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void dbLoadAllDeliveries() {
        logger.log(Level.INFO, "Loading all deliveries from delivery queue.");
        long start = System.nanoTime();
        long count = 0L;
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(GET_ALL_DELIVERIES);
            rs = ps.executeQuery();
            while (rs.next()) {
                long deliveryId = rs.getLong("DELIVERY_ID");
                byte state = rs.getByte("STATE");
                long collectionWaystoneId = rs.getLong("COLLECTION_WAYSTONE_ID");
                long containerId = rs.getLong("CONTAINER_ID");
                byte crates = rs.getByte("CRATES");
                long senderId = rs.getLong("SENDER_ID");
                long senderCost = rs.getLong("SENDER_COST");
                long receiverId = rs.getLong("RECEIVER_ID");
                long receiverCost = rs.getLong("RECEIVER_COST");
                long deliveryWaystoneId = rs.getLong("DELIVERY_WAYSTONE_ID");
                long wagonerId = rs.getLong("WAGONER_ID");
                long tsExpiry = rs.getLong("TS_EXPIRY");
                long tsWaitingForAccept = rs.getLong("TS_WAITING_FOR_ACCEPT");
                long tsAcceptedOrRejected = rs.getLong("TS_ACCEPTED_OR_REJECTED");
                long tsDeliveryStarted = rs.getLong("TS_DELIVERY_STARTED");
                long tsPickedUp = rs.getLong("TS_PICKED_UP");
                long tsDelivered = rs.getLong("TS_DELIVERED");
                new Delivery(deliveryId, state, collectionWaystoneId, containerId, crates, senderId, senderCost, receiverId, receiverCost, deliveryWaystoneId, wagonerId, tsExpiry, tsWaitingForAccept, tsAcceptedOrRejected, tsDeliveryStarted, tsPickedUp, tsDelivered);
            }
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to load all deliveries: " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.log(Level.INFO, "Loaded " + count + " deliveries from delivery queue. That took " + (float)(end - start) / 1000000.0f + " ms.");
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.log(Level.INFO, "Loaded " + count + " deliveries from delivery queue. That took " + (float)(end - start) / 1000000.0f + " ms.");
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long end = System.nanoTime();
        logger.log(Level.INFO, "Loaded " + count + " deliveries from delivery queue. That took " + (float)(end - start) / 1000000.0f + " ms.");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static long dbCreateDelivery(byte state, long collectionWaystoneId, long containerId, int crates, long senderId, long senderCost, long receiverId, long receiverCost, long deliveryWaystoneId, long wagonerId, long tsExpiry, long tsWaitingForAccept, long tsAcceptedOrRejected, long tsDeliveryStarted, long tsPickedUp, long tsDelivered) {
        ResultSet rs;
        PreparedStatement ps;
        Connection dbcon;
        long deliveryId;
        block4: {
            deliveryId = -10L;
            dbcon = null;
            ps = null;
            rs = null;
            try {
                dbcon = DbConnector.getCreatureDbCon();
                ps = dbcon.prepareStatement(CREATE_DELIVERY, 1);
                ps.setByte(1, state);
                ps.setLong(2, collectionWaystoneId);
                ps.setLong(3, containerId);
                ps.setByte(4, (byte)crates);
                ps.setLong(5, senderId);
                ps.setLong(6, senderCost);
                ps.setLong(7, receiverId);
                ps.setLong(8, receiverCost);
                ps.setLong(9, deliveryWaystoneId);
                ps.setLong(10, wagonerId);
                ps.setLong(11, tsExpiry);
                ps.setLong(12, tsWaitingForAccept);
                ps.setLong(13, tsAcceptedOrRejected);
                ps.setLong(14, tsDeliveryStarted);
                ps.setLong(15, tsPickedUp);
                ps.setLong(16, tsDelivered);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                if (!rs.next()) break block4;
                deliveryId = rs.getLong(1);
            }
            catch (SQLException ex) {
                try {
                    logger.log(Level.WARNING, "Failed to create delivery in deliveryQueue table.", ex);
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return deliveryId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbRemoveDelivery(long deliveryId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(DELETE_DELIVERY);
            ps.setLong(1, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to remove delivery " + deliveryId + " from deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryAccept(long deliveryId, byte state, long waystoneId, long tsAcceptOrReject) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_ACCEPT);
            ps.setByte(1, state);
            ps.setLong(2, waystoneId);
            ps.setLong(3, tsAcceptOrReject);
            ps.setLong(4, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryReject(long deliveryId, byte state, long tsAcceptOrReject) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_REJECT);
            ps.setByte(1, state);
            ps.setLong(2, tsAcceptOrReject);
            ps.setLong(3, -10L);
            ps.setLong(4, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryDismiss(long deliveryId, long wagonerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_WAGONER);
            ps.setLong(1, wagonerId);
            ps.setLong(2, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to wagoner " + wagonerId + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryContainer(long deliveryId, long containerId) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_CONTAINER);
            ps.setLong(1, containerId);
            ps.setLong(2, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to container " + containerId + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryStarted(long deliveryId, byte state, long tsStarted) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_STARTED);
            ps.setByte(1, state);
            ps.setLong(2, tsStarted);
            ps.setLong(3, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryPickedUp(long deliveryId, byte state, long tsPickedUp) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_PICKED_UP);
            ps.setByte(1, state);
            ps.setLong(2, tsPickedUp);
            ps.setLong(3, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateDeliveryDelivered(long deliveryId, byte state, long tsDelivered) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_DELIVERED);
            ps.setByte(1, state);
            ps.setLong(2, tsDelivered);
            ps.setLong(3, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void dbUpdateState(long deliveryId, byte state) {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getCreatureDbCon();
            ps = dbcon.prepareStatement(UPDATE_DELIVERY_STATE);
            ps.setByte(1, state);
            ps.setLong(2, deliveryId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            try {
                logger.log(Level.WARNING, "Failed to update delivery " + deliveryId + " to state " + state + " in deliveryQueue table.", ex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
    }
}

