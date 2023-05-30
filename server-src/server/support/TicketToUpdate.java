package com.wurmonline.server.support;

public final class TicketToUpdate {
   public static final byte ARCHIVESTATE = 0;
   public static final byte TRELLOCARDID = 1;
   public static final byte TRELLOFEEDBACKCARDID = 2;
   public static final byte ISDIRTY = 3;
   private final Ticket ticket;
   private final byte toUpdate;
   private byte archiveState;
   private String trelloCardId;
   private boolean isDirty;

   public TicketToUpdate(Ticket aTicket, byte aToUpdate, byte newArchiveState) {
      this.ticket = aTicket;
      this.toUpdate = aToUpdate;
      this.archiveState = newArchiveState;
   }

   public TicketToUpdate(Ticket aTicket, byte aToUpdate, String aTrelloCardId) {
      this.ticket = aTicket;
      this.toUpdate = aToUpdate;
      this.trelloCardId = aTrelloCardId;
   }

   public TicketToUpdate(Ticket aTicket, byte aToUpdate, boolean aIsDirty) {
      this.ticket = aTicket;
      this.toUpdate = aToUpdate;
      this.isDirty = aIsDirty;
   }

   public void update() {
      switch(this.toUpdate) {
         case 0:
            this.ticket.setArchiveState(this.archiveState);
            break;
         case 1:
            this.ticket.setTrelloCardId(this.trelloCardId);
            break;
         case 2:
            this.ticket.setTrelloFeedbackCardId(this.trelloCardId);
            break;
         case 3:
            this.ticket.setDirty(this.isDirty);
            this.ticket.dbUpdateIsDirty();
      }
   }
}
