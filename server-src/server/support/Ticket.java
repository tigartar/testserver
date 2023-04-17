/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcTicket;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.TicketGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Ticket
implements MiscConstants,
ProtoConstants,
TimeConstants {
    private final Map<Long, TicketAction> ticketActions = new HashMap<Long, TicketAction>();
    private static final String ADDTICKET = "INSERT INTO TICKETS (TICKETID,TICKETDATE,PLAYERWURMID,PLAYERNAME,CATEGORYCODE,SERVERID,ISGLOBAL,CLOSEDDATE,STATECODE,LEVELCODE,RESPONDERNAME,DESCRIPTION,ISDIRTY,REFFEEDBACK,TRELLOCARDID,TRELLOLISTCODE,HASDESCRIPTIONCHANGED,HASSUMMARYCHANGED,HASTRELLOLISTCHANGED,ACKNOWLEDGED) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATETICKET = "UPDATE TICKETS SET ISGLOBAL=?,CLOSEDDATE=?,STATECODE=?,LEVELCODE=?,RESPONDERNAME=?,ISDIRTY=?,REFFEEDBACK=?,TRELLOLISTCODE=?,HASDESCRIPTIONCHANGED=?,HASSUMMARYCHANGED=?,HASTRELLOLISTCHANGED=?,ACKNOWLEDGED=? WHERE TICKETID=?";
    private static final String UPDATETICKETDESCRIPTION = "UPDATE TICKETS SET DESCRIPTION=?,ISDIRTY=?,HASDESCRIPTIONCHANGED=? WHERE TICKETID=?";
    private static final String UPDATEISDIRTY = "UPDATE TICKETS SET ISDIRTY=?,TRELLOLISTCODE=?,HASDESCRIPTIONCHANGED=?,HASSUMMARYCHANGED=?,HASTRELLOLISTCHANGED=?,ACKNOWLEDGED=? WHERE TICKETID=?";
    private static final String UPDATETRELLOCARDID = "UPDATE TICKETS SET TRELLOCARDID=?,ISDIRTY=?,TRELLOLISTCODE=?,HASDESCRIPTIONCHANGED=?,HASSUMMARYCHANGED=?,HASTRELLOLISTCHANGED=? WHERE TICKETID=?";
    private static final String UPDATETRELLOFEEDBACKCARDID = "UPDATE TICKETS SET TRELLOFEEDBACKCARDID=?,ISDIRTY=? WHERE TICKETID=?";
    private static final String UPDATEARCHIVESTATE = "UPDATE TICKETS SET ARCHIVESTATECODE=? WHERE TICKETID=?";
    private static final String UPDATEACKNOWLEDGED = "UPDATE TICKETS SET ACKNOWLEDGED=? WHERE TICKETID=?";
    private static final String DELETETICKETACTIONS = "DELETE FROM TICKETACTIONS WHERE TICKETID=?";
    private static final String DELETETICKET = "DELETE FROM TICKETS WHERE TICKETID=?";
    private static final Logger logger = Logger.getLogger(Ticket.class.getName());
    public static final byte STATE_NEW = 0;
    public static final byte STATE_ONHOLD = 1;
    public static final byte STATE_RESOLVED = 2;
    public static final byte STATE_RESPONDED = 3;
    public static final byte STATE_CANCELLED = 4;
    public static final byte STATE_WATCHING = 5;
    public static final byte STATE_TAKEN = 6;
    public static final byte STATE_FORWARDED = 7;
    public static final byte STATE_REOPENED = 8;
    public static final byte LEVEL_NONE = 0;
    public static final byte LEVEL_CM = 1;
    public static final byte LEVEL_GM = 2;
    public static final byte LEVEL_ARCH = 3;
    public static final byte LEVEL_DEV = 4;
    public static final byte ARCHIVE_NOT_YET = 0;
    public static final byte ARCHIVE_TELL_PLAYERS = 1;
    public static final byte ARCHIVE_UPDATE_TRELLO = 2;
    public static final byte ARCHIVE_REMOVE_FROM_DB = 3;
    private final int ticketId;
    private final long ticketDate;
    private final long playerWurmId;
    private final String playerName;
    private final byte categoryCode;
    private final int serverId;
    private boolean global = true;
    private long closedDate = 0L;
    private byte stateCode = 0;
    private byte levelCode = 0;
    private String responderName = "";
    private String description = "";
    private boolean dirty = true;
    private short refFeedback = 0;
    private String trelloCardId = "";
    private String trelloFeedbackCardId = "";
    private byte trelloListCode = 0;
    private boolean descriptionChanged = true;
    private boolean summaryChanged = true;
    private boolean trelloListChanged = true;
    private byte lastListCode = 0;
    private String lastDescription = "";
    private String lastSummary = "";
    private byte archiveState = 0;
    private boolean acknowledged = true;

    public Ticket(int aTicketId, long aTicketDate, long aPlayerWurmId, String aPlayerName, byte aCategoryCode, int aServerId, boolean aGlobal, long aClosedDate, byte aStateCode, byte aLevelCode, String aResponderName, String aDescription, boolean isDirty, short aRefFeedback, String aTrelloFeedbackCardId, String aTrelloCardId, byte currentTrelloListCode, boolean hasDescriptionChanged, boolean hasSummaryChanged, boolean hasListChanged, byte theArchiveState, boolean isAcknowledged) {
        this.ticketId = aTicketId;
        this.ticketDate = aTicketDate;
        this.playerWurmId = aPlayerWurmId;
        this.playerName = aPlayerName;
        this.categoryCode = aCategoryCode;
        this.serverId = aServerId;
        this.global = true;
        this.closedDate = aClosedDate;
        this.stateCode = aStateCode;
        this.levelCode = aLevelCode;
        this.responderName = aResponderName;
        this.description = aDescription.length() < 10240 ? aDescription : aDescription.substring(0, 10240);
        this.dirty = isDirty;
        this.refFeedback = aRefFeedback;
        this.trelloFeedbackCardId = aTrelloFeedbackCardId;
        this.trelloCardId = aTrelloCardId;
        this.trelloListCode = currentTrelloListCode;
        this.descriptionChanged = hasDescriptionChanged;
        this.summaryChanged = hasSummaryChanged;
        this.trelloListChanged = hasListChanged;
        this.archiveState = theArchiveState;
        this.acknowledged = isAcknowledged;
        if (!hasDescriptionChanged) {
            this.lastDescription = this.description;
        }
        if (!hasSummaryChanged) {
            this.lastSummary = this.getTrelloName();
        }
        if (!hasListChanged) {
            this.lastListCode = this.trelloListCode;
        }
    }

    public Ticket(int aTicketId, long aTicketDate, long aPlayerWurmId, String aPlayerName, byte aCategoryCode, int aServerId, boolean aGlobal, long aClosedDate, byte aStateCode, byte aLevelCode, String aResponderName, String aDescription, boolean isDirty, short aRefFeedback, boolean aAcknowledge) {
        this(aTicketId, aTicketDate, aPlayerWurmId, aPlayerName, aCategoryCode, aServerId, true, aClosedDate, aStateCode, aLevelCode, aResponderName, aDescription, isDirty, aRefFeedback, "", "", 0, false, false, false, 0, aAcknowledge);
    }

    public Ticket(long aPlayerWurmId, String aPlayerName, byte aCategoryCode, String aDescription) {
        this(Tickets.getNextTicketNo(), System.currentTimeMillis(), aPlayerWurmId, aPlayerName, aCategoryCode, Servers.getLocalServerId(), true, 0L, 0, 1, "", aDescription.replace('\"', '\''), true, 0, "", "", 0, false, false, false, 0, true);
        this.dbAddTicket();
        Players.getInstance().sendTicket(this);
        this.sendTicketGlobal();
    }

    public int getTicketId() {
        return this.ticketId;
    }

    public String getTicketName() {
        return "#" + this.ticketId;
    }

    public long getWurmId() {
        return Tickets.calcWurmId(this.ticketId);
    }

    public long getTicketDate() {
        return this.ticketDate;
    }

    public String getDateAsString() {
        return Tickets.convertTime(this.ticketDate);
    }

    public boolean isOpen() {
        return this.closedDate == 0L;
    }

    public boolean isClosed() {
        return this.closedDate != 0L;
    }

    public long getClosedDate() {
        return this.closedDate;
    }

    public byte getStateCode() {
        return this.stateCode;
    }

    public void setStateCode(byte aStateCode) {
        this.stateCode = aStateCode;
    }

    public byte getCategoryCode() {
        return this.categoryCode;
    }

    public int getServerId() {
        return this.serverId;
    }

    public boolean isGlobal() {
        return this.global;
    }

    public boolean isWaitingAcknowledgement() {
        return !this.acknowledged;
    }

    public boolean getAcknowledged() {
        return this.acknowledged;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean isDirty) {
        if (isDirty) {
            this.dirty = isDirty;
            this.descriptionChanged = !this.lastDescription.equals(this.description);
            this.summaryChanged = !this.lastSummary.equals(this.getTrelloName());
            this.trelloListChanged = this.lastListCode != this.getTrelloListCode();
        } else {
            this.dirty = isDirty;
            this.lastDescription = this.description;
            this.lastSummary = this.getTrelloName();
            this.lastListCode = this.getTrelloListCode();
        }
    }

    public void setTrelloCardId(String aTrelloCardId) {
        this.trelloCardId = aTrelloCardId;
        this.setDirty(false);
        this.dbUpdateTrelloCardId();
    }

    public void setTrelloFeedbackCardId(String aTrelloCardId) {
        this.trelloFeedbackCardId = aTrelloCardId;
        this.setDirty(false);
        this.dbUpdateTrelloFeedbackCardId();
    }

    public byte getLevelCode() {
        return this.levelCode;
    }

    public void setLevelCode(byte aLevelCode) {
        this.levelCode = aLevelCode;
    }

    public String getResponderName() {
        return this.responderName;
    }

    public boolean hasFeedback() {
        return this.refFeedback != 0;
    }

    public short getRefFeedback() {
        return this.refFeedback;
    }

    public String getTrelloCardId() {
        return this.trelloCardId;
    }

    public String getTrelloFeedbackCardId() {
        return this.trelloFeedbackCardId;
    }

    public byte getArchiveState() {
        return this.archiveState;
    }

    public void setArchiveState(byte newArchiveState) {
        if (!Servers.isThisLoginServer() && newArchiveState == 2) {
            this.dbDelete();
            Tickets.removeTicket(this.ticketId);
        } else if (newArchiveState == 3) {
            this.dbDelete();
            Tickets.removeTicket(this.ticketId);
        } else {
            this.archiveState = newArchiveState;
            this.dbUpdateArchiveState();
        }
    }

    public boolean hasTicketChangedSince(long aDate) {
        for (TicketAction ta : this.ticketActions.values()) {
            if (!ta.isActionAfter(aDate)) continue;
            return true;
        }
        return false;
    }

    public void update(boolean aGlobal, long aClosedDate, byte aStateCode, byte aLevelCode, String aResponderName, String aDescription, short aRefFeedback, boolean isAcknowledged) {
        this.global = true;
        this.closedDate = aClosedDate;
        this.stateCode = aStateCode;
        this.levelCode = aLevelCode;
        this.responderName = aResponderName;
        this.description = aDescription;
        this.refFeedback = aRefFeedback;
        this.acknowledged = isAcknowledged;
        this.setDirty(true);
        this.dbUpdateTicket();
    }

    public void save() {
        this.dbAddTicket();
    }

    public void addTicketAction(TicketAction newTicketAction) {
        if (newTicketAction.getAction() == 14) {
            this.refFeedback = newTicketAction.getActionNo();
        }
        this.ticketActions.put(Long.valueOf(newTicketAction.getActionNo()), newTicketAction);
    }

    public TicketAction addTicketAction(short aActionNo, byte aAction, long aDate, String aByWhom, String aNote, byte aVisibilityLevel, byte aQualityOfServiceCode, byte aCourteousCode, byte aKnowledgeableCode, byte aGeneralFlags, byte aQualitiesFlags, byte aIrkedFlags) {
        if (!this.ticketActions.containsKey(aActionNo)) {
            TicketAction ta = new TicketAction(this.ticketId, aActionNo, aAction, aDate, aByWhom, aNote, aVisibilityLevel, true, aQualityOfServiceCode, aCourteousCode, aKnowledgeableCode, aGeneralFlags, aQualitiesFlags, aIrkedFlags, "");
            ta.dbSave();
            this.addTicketAction(ta);
            Tickets.addTicketToSend(this, ta);
            return ta;
        }
        return this.ticketActions.get(aActionNo);
    }

    public void autoForwardToGM() {
        if (this.stateCode == 0 && this.serverId == Servers.getLocalServerId() && this.ticketDate < System.currentTimeMillis() - 900000L) {
            this.addNewTicketAction((byte)6, "Auto", "Auto forward to GMs", (byte)1);
        }
    }

    public void addNewTicketAction(byte action, String byWhom, String note, byte visibilityLevel) {
        this.addNewTicketAction(action, byWhom, note, visibilityLevel, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);
    }

    public void addNewTicketAction(byte action, String byWhom, String note, byte visibilityLevel, byte aQualityOfServiceCode, byte aCourteousCode, byte aKnowledgeableCode, byte aGeneralFlags, byte aQualitiesFlags, byte aIrkedFlags) {
        TicketAction ta = new TicketAction(this.ticketId, (short)(this.ticketActions.size() + 1), action, byWhom, note, visibilityLevel, aQualityOfServiceCode, aCourteousCode, aKnowledgeableCode, aGeneralFlags, aQualitiesFlags, aIrkedFlags);
        ta.dbSave();
        this.addTicketAction(ta);
        boolean oldGlobal = this.global;
        this.global = true;
        this.setAckFor(action, visibilityLevel);
        switch (action) {
            case 1: {
                this.stateCode = (byte)4;
                this.closedDate = System.currentTimeMillis();
                this.responderName = "";
                break;
            }
            case 13: {
                this.stateCode = (byte)7;
                this.levelCode = 1;
                this.responderName = "";
                break;
            }
            case 6: {
                this.stateCode = (byte)7;
                this.levelCode = (byte)2;
                this.responderName = "";
                break;
            }
            case 7: {
                this.stateCode = (byte)7;
                this.levelCode = (byte)3;
                this.responderName = "";
                break;
            }
            case 8: {
                this.stateCode = (byte)7;
                this.levelCode = (byte)4;
                this.responderName = "";
                break;
            }
            case 10: {
                this.stateCode = 1;
                this.responderName = "";
                break;
            }
            case 3: {
                if (this.levelCode != 3 && this.levelCode != 4) {
                    this.levelCode = (byte)2;
                }
                this.stateCode = (byte)3;
                this.responderName = byWhom;
                break;
            }
            case 2: {
                this.stateCode = (byte)3;
                this.responderName = byWhom;
                break;
            }
            case 11: {
                if (this.levelCode != 3 && this.levelCode != 4) {
                    this.levelCode = (byte)2;
                }
                this.stateCode = (byte)6;
                this.responderName = byWhom;
                break;
            }
            case 9: {
                this.stateCode = (byte)2;
                this.closedDate = System.currentTimeMillis();
                this.responderName = byWhom;
                break;
            }
            case 15: {
                this.stateCode = this.categoryCode == 11 ? (byte)5 : (byte)8;
                this.responderName = byWhom;
                this.closedDate = 0L;
                break;
            }
            case 14: {
                this.refFeedback = ta.getActionNo();
                break;
            }
            case 0: {
                break;
            }
        }
        this.setDirty(true);
        this.dbUpdateTicket();
        Tickets.addTicketToSend(this, ta);
        if (!oldGlobal) {
            this.sendTicketGlobal();
        } else {
            this.sendTicketGlobal(ta);
        }
    }

    public void addSurvey(byte aQualityOfServiceCode, byte aCourteousCode, byte aKnowledgeableCode, byte aGeneralFlags, byte aQualitiesFlags, byte aIrkedFlags) {
        this.setDirty(true);
        this.dbUpdateTicket();
    }

    public String getShortDescription() {
        if (this.description.length() <= 400 || this.getStateCode() != 5) {
            return this.getDescription();
        }
        if (this.getTrelloCardId().length() == 0) {
            return "Possible Client Hack: Soon in a trello near you!";
        }
        return "Possible Client Hack: see trello https://trello.com/c/" + this.getTrelloCardId();
    }

    public String getDescription() {
        return this.description.replace('\"', '\'');
    }

    public void appendDescription(String aDescription) {
        if (this.description.length() == 0) {
            this.description = aDescription.replace('\"', '\'');
        } else {
            this.description = this.description + "\n" + aDescription;
            if (this.description.length() > 400) {
                this.description = this.description.substring(0, 399).replace('\"', '\'');
            }
        }
        this.setDirty(true);
        this.dbUpdateTicketDescription();
    }

    public byte getTicketGroup(Player player) {
        if (player.mayHearDevTalk()) {
            if (!this.isOpen()) {
                return TicketGroup.CLOSED.getId();
            }
            if (this.categoryCode == 11) {
                return TicketGroup.WATCH.getId();
            }
            if (this.categoryCode == 4) {
                return TicketGroup.FORUM.getId();
            }
            return TicketGroup.OPEN.getId();
        }
        if (player.mayHearMgmtTalk()) {
            if (!this.isOpen()) {
                return TicketGroup.CLOSED.getId();
            }
            return TicketGroup.OPEN.getId();
        }
        return TicketGroup.NONE.getId();
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public long getPlayerId() {
        return this.playerWurmId;
    }

    public void sendTicketGlobal() {
        WcTicket wct = new WcTicket(this);
        if (Servers.isThisLoginServer()) {
            wct.sendFromLoginServer();
        } else {
            wct.sendToLoginServer();
        }
    }

    private void sendTicketGlobal(TicketAction ticketAction) {
        WcTicket wct = new WcTicket(this, ticketAction);
        if (Servers.isThisLoginServer()) {
            wct.sendFromLoginServer();
        } else {
            wct.sendToLoginServer();
        }
    }

    public byte getColourCode(Player player) {
        if (player.mayHearDevTalk() || player.mayHearMgmtTalk()) {
            if (this.isOpen()) {
                if (PlayerInfoFactory.isPlayerOnline(this.playerWurmId)) {
                    switch (this.levelCode) {
                        case 2: {
                            return 11;
                        }
                        case 3: {
                            return 8;
                        }
                        case 4: {
                            return 2;
                        }
                    }
                    return 3;
                }
                return 14;
            }
            return 0;
        }
        if (this.isOpen()) {
            return 3;
        }
        return 0;
    }

    private String getLastAction() {
        String lastAction = Ticket.decodeState(this.stateCode);
        if (this.stateCode != 2 && this.stateCode != 4 && this.stateCode != 0) {
            if (lastAction.length() > 0) {
                lastAction = lastAction + " ";
            }
            lastAction = lastAction + Ticket.decodeLevel(this.levelCode);
        }
        return lastAction;
    }

    public boolean hasSummaryChanged() {
        return this.summaryChanged;
    }

    public boolean hasDescriptionChanged() {
        return this.descriptionChanged;
    }

    public boolean hasListChanged() {
        return this.trelloListChanged;
    }

    public String getTrelloName() {
        String lastAction = this.getLastAction();
        if (this.hasFeedback()) {
            lastAction = lastAction + "*";
        }
        return "#" + this.ticketId + " " + Tickets.convertTime(this.ticketDate) + " " + this.playerName + " : " + Ticket.abbreviateCategory(this.categoryCode) + " (" + this.getServerAbbreviation() + ") " + lastAction + " " + this.responderName;
    }

    public String getTrelloFeedbackTitle() {
        if (this.hasFeedback()) {
            TicketAction ta = this.getFeedback();
            return "#" + this.ticketId + " " + Tickets.convertTime(this.closedDate) + " S:" + ta.getQualityOfServiceCode() + " C:" + ta.getCourteousCode() + " K:" + ta.getKnowledgeableCode() + " G:" + ta.getGeneralFlagString() + " Q:" + ta.getQualitiesFlagsString() + " I:" + ta.getIrkedFlagsString();
        }
        return "";
    }

    public String getFeedbackText() {
        if (this.hasFeedback()) {
            return this.getFeedback().getNote();
        }
        return "";
    }

    private String getServerAbbreviation() {
        ServerEntry serverEntry = Servers.getServerWithId(this.serverId);
        if (serverEntry == null) {
            String s = "unknown" + String.valueOf(this.serverId);
            return s.substring(s.length() - 3);
        }
        return serverEntry.getAbbreviation();
    }

    public String getTicketSummary(Player player) {
        if (player.mayHearDevTalk()) {
            return this.getTrelloName();
        }
        if (player.mayHearMgmtTalk()) {
            return "#" + this.ticketId + " " + Tickets.convertTime(this.ticketDate) + " " + this.playerName + " : " + Ticket.abbreviateCategory(this.categoryCode) + (this.serverId == Servers.getLocalServerId() ? " " : " (" + this.getServerAbbreviation() + ")") + " " + this.getLastAction() + (this.levelCode <= 1 || player.mayMute() ? " " + this.responderName : "");
        }
        return "#" + this.ticketId + " " + Tickets.convertTime(this.ticketDate) + " : " + this.getLastAction();
    }

    public byte getTrelloListCode() {
        if (this.getStateCode() == 4 || this.getStateCode() == 2) {
            return 3;
        }
        if (this.categoryCode == 11) {
            return 4;
        }
        if (this.getLevelCode() == 3 || this.getLevelCode() == 4) {
            return 2;
        }
        return 1;
    }

    public TicketAction getFeedback() {
        if (this.hasFeedback()) {
            return this.ticketActions.get(this.refFeedback);
        }
        return null;
    }

    public TicketAction[] getTicketActions(Player player) {
        HashMap<Long, TicketAction> playerTicketAction = new HashMap<Long, TicketAction>();
        for (Map.Entry<Long, TicketAction> entry : this.ticketActions.entrySet()) {
            if (!entry.getValue().isActionShownTo(player)) continue;
            playerTicketAction.put(entry.getKey(), entry.getValue());
        }
        return playerTicketAction.values().toArray(new TicketAction[playerTicketAction.size()]);
    }

    public TicketAction[] getDirtyTicketActions() {
        HashMap<Long, TicketAction> dirtyTicketAction = new HashMap<Long, TicketAction>();
        for (Map.Entry<Long, TicketAction> entry : this.ticketActions.entrySet()) {
            if (!entry.getValue().isDirty()) continue;
            dirtyTicketAction.put(entry.getKey(), entry.getValue());
        }
        return dirtyTicketAction.values().toArray(new TicketAction[dirtyTicketAction.size()]);
    }

    public TicketAction[] getTicketActions() {
        return this.ticketActions.values().toArray(new TicketAction[this.ticketActions.size()]);
    }

    public long getLatestActionDate() {
        long newestAction = 0L;
        for (Map.Entry<Long, TicketAction> entry : this.ticketActions.entrySet()) {
            if (entry.getValue().getDate() <= newestAction) continue;
            newestAction = entry.getValue().getDate();
        }
        return newestAction;
    }

    public boolean isTicketShownTo(Player player) {
        if (this.archiveState != 0) {
            return false;
        }
        if (player.mayHearDevTalk() && (Servers.getLocalServerId() == this.serverId || this.levelCode >= 2)) {
            return true;
        }
        if (player.mayHearMgmtTalk() && this.categoryCode != 11) {
            return true;
        }
        return this.categoryCode != 11 && player.getWurmId() == this.playerWurmId;
    }

    public void acknowledgeTicketUpdate(Player player) {
        if (this.categoryCode != 11 && player.getWurmId() == this.playerWurmId && !this.acknowledged) {
            this.acknowledged = true;
            this.dbUpdateAchnowledged();
            this.sendTicketGlobal();
        }
    }

    public static String decodeState(byte aState) {
        switch (aState) {
            case 0: {
                return "New";
            }
            case 1: {
                return "OnHold";
            }
            case 8: {
                return "ReOpened";
            }
            case 2: {
                return "Resolved";
            }
            case 4: {
                return "Cancelled";
            }
            case 7: {
                return "Fwd";
            }
        }
        return "";
    }

    public static String abbreviateCategory(byte aCategory) {
        switch (aCategory) {
            case 3: {
                return "Bug";
            }
            case 8: {
                return "Paymt";
            }
            case 7: {
                return "Pwd";
            }
            case 1: {
                return "Acct";
            }
            case 5: {
                return "Grief";
            }
            case 9: {
                return "Stuck";
            }
            case 2: {
                return "Boat";
            }
            case 6: {
                return "Horse";
            }
            case 11: {
                return "Watch";
            }
            case 4: {
                return "Forum";
            }
            case 10: {
                return "Other";
            }
        }
        return "";
    }

    public static String decodeLevel(byte aLevel) {
        switch (aLevel) {
            case 1: {
                return "CM";
            }
            case 2: {
                return "GM";
            }
            case 3: {
                return "Arch";
            }
            case 4: {
                return "Admin";
            }
        }
        return "";
    }

    private void setAckFor(byte action, byte noteVisibility) {
        if (this.categoryCode == 11) {
            return;
        }
        if (action != 1 && action != 14 && noteVisibility == 0) {
            this.acknowledged = false;
        }
        if (action == 9 || action == 15) {
            this.acknowledged = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbAddTicket() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(ADDTICKET);
            ps.setInt(1, this.ticketId);
            ps.setLong(2, this.ticketDate);
            ps.setLong(3, this.playerWurmId);
            ps.setString(4, this.playerName);
            ps.setByte(5, this.categoryCode);
            ps.setInt(6, this.serverId);
            ps.setBoolean(7, this.global);
            ps.setLong(8, this.closedDate);
            ps.setByte(9, this.stateCode);
            ps.setByte(10, this.levelCode);
            ps.setString(11, this.responderName);
            ps.setString(12, this.description.substring(0, Math.min(this.description.length(), 398)));
            ps.setBoolean(13, this.dirty);
            ps.setShort(14, this.refFeedback);
            ps.setString(15, this.trelloCardId);
            ps.setByte(16, this.trelloListCode);
            ps.setBoolean(17, this.descriptionChanged);
            ps.setBoolean(18, this.summaryChanged);
            ps.setBoolean(19, this.trelloListChanged);
            ps.setBoolean(20, this.acknowledged);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateTicket() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATETICKET);
            ps.setBoolean(1, this.global);
            ps.setLong(2, this.closedDate);
            ps.setByte(3, this.stateCode);
            ps.setByte(4, this.levelCode);
            ps.setString(5, this.responderName);
            ps.setBoolean(6, this.dirty);
            ps.setShort(7, this.refFeedback);
            ps.setByte(8, this.trelloListCode);
            ps.setBoolean(9, this.descriptionChanged);
            ps.setBoolean(10, this.summaryChanged);
            ps.setBoolean(11, this.trelloListChanged);
            ps.setBoolean(12, this.acknowledged);
            ps.setInt(13, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateTicketDescription() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATETICKETDESCRIPTION);
            ps.setString(1, this.description);
            ps.setBoolean(2, this.dirty);
            ps.setBoolean(3, this.descriptionChanged);
            ps.setInt(4, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dbUpdateIsDirty() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATEISDIRTY);
            ps.setBoolean(1, this.dirty);
            ps.setByte(2, this.trelloListCode);
            ps.setBoolean(3, this.descriptionChanged);
            ps.setBoolean(4, this.summaryChanged);
            ps.setBoolean(5, this.trelloListChanged);
            ps.setBoolean(6, this.acknowledged);
            ps.setInt(7, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateTrelloCardId() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATETRELLOCARDID);
            ps.setString(1, this.trelloCardId);
            ps.setBoolean(2, this.dirty);
            ps.setByte(3, this.trelloListCode);
            ps.setBoolean(4, this.descriptionChanged);
            ps.setBoolean(5, this.summaryChanged);
            ps.setBoolean(6, this.trelloListChanged);
            ps.setInt(7, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateTrelloFeedbackCardId() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATETRELLOFEEDBACKCARDID);
            ps.setString(1, this.trelloFeedbackCardId);
            ps.setBoolean(2, this.dirty);
            ps.setInt(3, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateArchiveState() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATEARCHIVESTATE);
            ps.setByte(1, this.archiveState);
            ps.setInt(2, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbUpdateAchnowledged() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(UPDATEACKNOWLEDGED);
            ps.setBoolean(1, this.acknowledged);
            ps.setInt(2, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void dbDelete() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(DELETETICKETACTIONS);
            ps.setInt(1, this.ticketId);
            ps.executeUpdate();
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement(DELETETICKET);
            ps.setInt(1, this.ticketId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public String toString() {
        return "Ticket [ticketId=" + this.ticketId + ", player=" + this.playerName + ", category=" + this.categoryCode + ", state=" + this.stateCode + ", level=" + this.levelCode + ", description=" + this.description + "]";
    }
}

