/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcTicket
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcTicket.class.getName());
    public static final byte DO_NOTHING = 0;
    public static final byte GET_BATCHNOS = 1;
    public static final byte THE_BATCHNOS = 2;
    public static final byte SEND_TICKET = 3;
    public static final byte CHECK_FOR_UPDATES = 4;
    private byte type = 0;
    private int noBatchNos = 1;
    private int firstBatchNos = 0;
    private int secondBatchNos = 0;
    private Ticket ticket = null;
    private boolean sendActions = false;
    private TicketAction ticketAction = null;
    private long checkDate = 0L;

    public WcTicket(int aNoBatchNos) {
        super(WurmId.getNextWCCommandId(), (short)18);
        this.type = 1;
        this.noBatchNos = aNoBatchNos;
    }

    public WcTicket(int aFirstBatchNos, int aSecondBatchNos) {
        super(WurmId.getNextWCCommandId(), (short)18);
        this.type = (byte)2;
        this.firstBatchNos = aFirstBatchNos;
        this.secondBatchNos = aSecondBatchNos;
    }

    public WcTicket(Ticket aTicket) {
        super(WurmId.getNextWCCommandId(), (short)18);
        this.type = (byte)3;
        this.ticket = aTicket;
        this.ticketAction = null;
        this.sendActions = true;
    }

    public WcTicket(long aId, Ticket aTicket, int aNumbActions, TicketAction aTicketAction) {
        super(aId, (short)18);
        this.type = (byte)3;
        this.ticket = aTicket;
        this.ticketAction = aNumbActions > 1 ? null : aTicketAction;
        this.sendActions = aNumbActions > 0;
    }

    public WcTicket(Ticket aTicket, TicketAction aTicketAction) {
        super(WurmId.getNextWCCommandId(), (short)18);
        this.type = (byte)3;
        this.ticket = aTicket;
        this.ticketAction = aTicketAction;
        this.sendActions = true;
    }

    public WcTicket(long aDate) {
        super(WurmId.getNextWCCommandId(), (short)18);
        this.type = (byte)4;
        this.checkDate = aDate;
    }

    public WcTicket(long aId, byte[] aData) {
        super(aId, (short)18, aData);
    }

    @Override
    public boolean autoForward() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] barr = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeByte(this.type);
            switch (this.type) {
                case 1: {
                    dos.writeInt(this.noBatchNos);
                    break;
                }
                case 2: {
                    dos.writeInt(this.firstBatchNos);
                    dos.writeInt(this.secondBatchNos);
                    break;
                }
                case 3: {
                    dos.writeInt(this.ticket.getTicketId());
                    dos.writeLong(this.ticket.getTicketDate());
                    dos.writeLong(this.ticket.getPlayerId());
                    dos.writeUTF(this.ticket.getPlayerName());
                    dos.writeByte(this.ticket.getCategoryCode());
                    dos.writeInt(this.ticket.getServerId());
                    dos.writeBoolean(this.ticket.isGlobal());
                    dos.writeLong(this.ticket.getClosedDate());
                    dos.writeByte(this.ticket.getStateCode());
                    dos.writeByte(this.ticket.getLevelCode());
                    dos.writeUTF(this.ticket.getResponderName());
                    dos.writeUTF(this.ticket.getDescription());
                    dos.writeShort(this.ticket.getRefFeedback());
                    dos.writeBoolean(this.ticket.getAcknowledged());
                    if (this.sendActions) {
                        if (this.ticketAction == null) {
                            TicketAction[] ticketActions = this.ticket.getTicketActions();
                            dos.writeByte(ticketActions.length);
                            for (TicketAction ta : ticketActions) {
                                this.addTicketAction(dos, ta);
                            }
                            break;
                        }
                        dos.writeByte(1);
                        this.addTicketAction(dos, this.ticketAction);
                        break;
                    }
                    dos.writeByte(0);
                    break;
                }
                case 4: {
                    dos.writeLong(this.checkDate);
                    break;
                }
            }
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                barr = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(barr);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
            barr = bos.toByteArray();
            StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
            this.setData(barr);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
        barr = bos.toByteArray();
        StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
        this.setData(barr);
        return barr;
    }

    private void addTicketAction(DataOutputStream dos, TicketAction ta) throws IOException {
        dos.writeShort(ta.getActionNo());
        dos.writeByte(ta.getAction());
        dos.writeLong(ta.getDate());
        dos.writeUTF(ta.getByWhom());
        dos.writeUTF(ta.getNote());
        dos.writeByte(ta.getVisibilityLevel());
        if (ta.getAction() == 14) {
            dos.writeByte(ta.getQualityOfServiceCode());
            dos.writeByte(ta.getCourteousCode());
            dos.writeByte(ta.getKnowledgeableCode());
            dos.writeByte(ta.getGeneralFlags());
            dos.writeByte(ta.getQualitiesFlags());
            dos.writeByte(ta.getIrkedFlags());
        }
    }

    @Override
    public void execute() {
        new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new ByteArrayInputStream(WcTicket.this.getData()));
                    WcTicket.this.type = dis.readByte();
                    switch (WcTicket.this.type) {
                        case 1: {
                            WcTicket.this.noBatchNos = dis.readInt();
                            break;
                        }
                        case 2: {
                            WcTicket.this.firstBatchNos = dis.readInt();
                            WcTicket.this.secondBatchNos = dis.readInt();
                            break;
                        }
                        case 3: {
                            int ticketId = dis.readInt();
                            long ticketDate = dis.readLong();
                            long playerWurmId = dis.readLong();
                            String playerName = dis.readUTF();
                            byte categoryCode = dis.readByte();
                            int serverId = dis.readInt();
                            boolean global = dis.readBoolean();
                            long closedDate = dis.readLong();
                            byte stateCode = dis.readByte();
                            byte levelCode = dis.readByte();
                            String responderName = dis.readUTF();
                            String description = dis.readUTF();
                            short refFeedback = dis.readShort();
                            boolean acknowledge = dis.readBoolean();
                            WcTicket.this.ticket = new Ticket(ticketId, ticketDate, playerWurmId, playerName, categoryCode, serverId, global, closedDate, stateCode, levelCode, responderName, description, true, refFeedback, acknowledge);
                            WcTicket.this.ticket = Tickets.addTicket(WcTicket.this.ticket, true);
                            int numbActions = dis.readByte();
                            TicketAction ta = null;
                            for (int x = 0; x < numbActions; ++x) {
                                byte qualityOfServiceCode = 0;
                                byte courteousCode = 0;
                                byte knowledgeableCode = 0;
                                byte generalFlags = 0;
                                byte qualitiesFlags = 0;
                                byte irkedFlags = 0;
                                short actionNo = dis.readShort();
                                byte action = dis.readByte();
                                long dated = dis.readLong();
                                String byWhom = dis.readUTF();
                                String note = dis.readUTF();
                                byte visLevel = dis.readByte();
                                if (action == 14) {
                                    qualityOfServiceCode = dis.readByte();
                                    courteousCode = dis.readByte();
                                    knowledgeableCode = dis.readByte();
                                    generalFlags = dis.readByte();
                                    qualitiesFlags = dis.readByte();
                                    irkedFlags = dis.readByte();
                                }
                                ta = WcTicket.this.ticket.addTicketAction(actionNo, action, dated, byWhom, note, visLevel, qualityOfServiceCode, courteousCode, knowledgeableCode, generalFlags, qualitiesFlags, irkedFlags);
                            }
                            if (Servers.isThisLoginServer() && WcTicket.this.ticket.isGlobal()) {
                                WcTicket wct = new WcTicket(WcTicket.this.getWurmId(), WcTicket.this.ticket, numbActions, ta);
                                wct.sendFromLoginServer();
                            }
                            Tickets.addTicketToSend(WcTicket.this.ticket, numbActions, ta);
                            break;
                        }
                        case 4: {
                            WcTicket.this.checkDate = dis.readLong();
                            break;
                        }
                    }
                }
                catch (IOException ex) {
                    try {
                        logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
                    }
                    catch (Throwable throwable) {
                        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                        throw throwable;
                    }
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                    return;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                if (WcTicket.this.type == 1 && Servers.isThisLoginServer()) {
                    WcTicket.this.firstBatchNos = Tickets.getNextBatchNo();
                    if (WcTicket.this.noBatchNos > 1) {
                        WcTicket.this.secondBatchNos = Tickets.getNextBatchNo();
                    }
                    WcTicket wt = new WcTicket(WcTicket.this.firstBatchNos, WcTicket.this.secondBatchNos);
                    wt.sendToServer(WurmId.getOrigin(WcTicket.this.getWurmId()));
                } else if (WcTicket.this.type == 2 && !Servers.isThisLoginServer()) {
                    Tickets.setNextBatchNo(WcTicket.this.firstBatchNos, WcTicket.this.secondBatchNos);
                } else if (WcTicket.this.type != 3 && WcTicket.this.type == 4) {
                    for (Ticket t : Tickets.getTicketsChangedSince(WcTicket.this.checkDate)) {
                        WcTicket wt = new WcTicket(t);
                        wt.sendToServer(WurmId.getOrigin(WcTicket.this.getWurmId()));
                    }
                }
            }
        }.start();
    }
}

