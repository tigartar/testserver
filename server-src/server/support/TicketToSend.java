/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.Players;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;

public final class TicketToSend {
    private final Ticket ticket;
    private final TicketAction ticketAction;
    private final boolean sendActions;

    public TicketToSend(Ticket aTicket) {
        this.ticket = aTicket;
        this.sendActions = false;
        this.ticketAction = null;
    }

    public TicketToSend(Ticket aTicket, TicketAction action) {
        this.ticket = aTicket;
        this.sendActions = true;
        this.ticketAction = action;
    }

    public TicketToSend(Ticket aTicket, int actionsToSend, TicketAction action) {
        this.ticket = aTicket;
        this.sendActions = actionsToSend > 0;
        this.ticketAction = actionsToSend == 1 ? action : null;
    }

    public void send() {
        if (this.ticket.getArchiveState() == 1) {
            Players.getInstance().removeTicket(this.ticket);
            this.ticket.setArchiveState((byte)2);
        } else if (!this.sendActions) {
            Players.getInstance().sendTicket(this.ticket);
        } else {
            Players.getInstance().sendTicket(this.ticket, this.ticketAction);
        }
    }
}

