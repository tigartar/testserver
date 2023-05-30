package com.wurmonline.server.support;

import com.wurmonline.server.Players;

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
      if (actionsToSend == 1) {
         this.ticketAction = action;
      } else {
         this.ticketAction = null;
      }
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
