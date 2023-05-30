package com.wurmonline.server.support;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.webinterface.WcTicket;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Tickets implements CounterTypes, TimeConstants {
   private static Logger logger = Logger.getLogger(Tickets.class.getName());
   private static final Map<Integer, Ticket> tickets = new ConcurrentHashMap<>();
   private static final ConcurrentLinkedDeque<TicketToSend> ticketsToSend = new ConcurrentLinkedDeque<>();
   private static final ConcurrentLinkedDeque<TicketToUpdate> ticketsToUpdate = new ConcurrentLinkedDeque<>();
   private static final String LOADTICKETNOS = "SELECT * FROM TICKETNOS";
   private static final String ADDTICKETNOS = "INSERT INTO TICKETNOS (PK,NEXTTICKETID,LASTTICKETID,NEXTBATCH) VALUES(0,?,?,?)";
   private static final String UPDATETICKETNOS = "UPDATE TICKETNOS SET NEXTTICKETID=?, LASTTICKETID=?, NEXTBATCH=? WHERE PK=0";
   private static final String LOADALLTICKETS = "SELECT * FROM TICKETS";
   private static final String LOADALLTICKETACTIONS = "SELECT * FROM TICKETACTIONS";
   private static final String PURGEBADACTIONS = "DELETE FROM TICKETACTIONS USING TICKETACTIONS LEFT JOIN TICKETS ON TICKETS.TICKETID = TICKETACTIONS.TICKETID WHERE TICKETS.TICKETID IS NULL";
   private static int nextTicketId = 0;
   private static int lastTicketId = 0;
   private static int nextBatch = 0;

   private Tickets() {
   }

   public static void loadTickets() {
      long start = System.nanoTime();

      try {
         dbLoadTicketNos();
         dbLoadAllTickets();
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Problems loading Tickets", (Throwable)var3);
      }

      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.log(Level.INFO, "Loaded " + tickets.size() + " Tickets. It took " + lElapsedTime + " millis.");
   }

   public static Ticket addTicket(Ticket newTicket, boolean saveit) {
      if (tickets.containsKey(newTicket.getTicketId())) {
         Ticket oldTicket = tickets.get(newTicket.getTicketId());
         oldTicket.update(
            newTicket.isGlobal(),
            newTicket.getClosedDate(),
            newTicket.getStateCode(),
            newTicket.getLevelCode(),
            newTicket.getResponderName(),
            newTicket.getDescription(),
            newTicket.getRefFeedback(),
            newTicket.getAcknowledged()
         );
         return oldTicket;
      } else {
         tickets.put(newTicket.getTicketId(), newTicket);
         if (saveit) {
            newTicket.save();
         }

         return newTicket;
      }
   }

   public static long calcWurmId(int ticketId) {
      return (long)((ticketId << 8) + 25);
   }

   public static int decodeTicketId(long wurmId) {
      return (int)(wurmId >>> 8 & -1L);
   }

   public static int getNextTicketNo() {
      int nextTicketNo = nextTicketId++;
      if (nextTicketId > lastTicketId) {
         nextTicketId = nextBatch;
         lastTicketId = nextTicketId + 9999;
         if (Servers.isThisLoginServer()) {
            nextBatch += 10000;
         } else {
            nextBatch = 0;
            WcTicket wt = new WcTicket(1);
            wt.sendToLoginServer();
         }
      }

      dbUpdateTicketNos();
      return nextTicketNo;
   }

   public static int getNextBatchNo() {
      if (Servers.isThisLoginServer()) {
         int nextBatchNo = nextBatch;
         nextBatch += 10000;
         dbUpdateTicketNos();
         return nextBatchNo;
      } else {
         return nextBatch;
      }
   }

   public static void checkBatchNos() {
      if (nextTicketId == 0) {
         WcTicket wt = new WcTicket(2);
         wt.sendToLoginServer();
      } else if (nextBatch == 0) {
         WcTicket wt = new WcTicket(1);
         wt.sendToLoginServer();
      }
   }

   public static void setNextBatchNo(int newBatchNo, int secondBatchNo) {
      if (nextTicketId == 0) {
         nextTicketId = newBatchNo;
         lastTicketId = newBatchNo + 9999;
         if (nextBatch == 0) {
            nextBatch = secondBatchNo;
         }
      } else if (nextBatch == 0) {
         nextBatch = newBatchNo;
      }

      dbUpdateTicketNos();
   }

   public static Ticket[] getTickets(Player player) {
      Map<Integer, Ticket> playerTickets = new HashMap<>();

      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         if (entry.getValue().isTicketShownTo(player)) {
            playerTickets.put(entry.getKey(), entry.getValue());
         }
      }

      return playerTickets.values().toArray(new Ticket[playerTickets.size()]);
   }

   public static void acknowledgeTicketUpdatesFor(Player player) {
      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         entry.getValue().acknowledgeTicketUpdate(player);
      }
   }

   public static Ticket[] getTicketsChangedSince(long aDate) {
      Map<Integer, Ticket> playerTickets = new HashMap<>();

      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         if (entry.getValue().hasTicketChangedSince(aDate)) {
            playerTickets.put(entry.getKey(), entry.getValue());
         }
      }

      return playerTickets.values().toArray(new Ticket[playerTickets.size()]);
   }

   public static long getLatestActionDate() {
      long latestTicketDate = 0L;

      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         if (latestTicketDate < entry.getValue().getLatestActionDate()) {
            latestTicketDate = entry.getValue().getLatestActionDate();
         }
      }

      return latestTicketDate;
   }

   public static Ticket[] getDirtyTickets() {
      Map<Integer, Ticket> dirtyTickets = new HashMap<>();

      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         Ticket ticket = entry.getValue();
         if (ticket.isDirty() && (ticket.isGlobal() || ticket.isClosed())) {
            dirtyTickets.put(entry.getKey(), entry.getValue());
         }
      }

      return dirtyTickets.values().toArray(new Ticket[dirtyTickets.size()]);
   }

   public static Ticket[] getArchiveTickets() {
      Map<Integer, Ticket> archiveTickets = new HashMap<>();

      for(Entry<Integer, Ticket> entry : tickets.entrySet()) {
         Ticket ticket = entry.getValue();
         if (ticket.getArchiveState() == 2) {
            archiveTickets.put(entry.getKey(), entry.getValue());
         }
      }

      return archiveTickets.values().toArray(new Ticket[archiveTickets.size()]);
   }

   public static Ticket getTicket(int ticketNo) {
      return tickets.get(ticketNo);
   }

   public static void removeTicket(int ticketNo) {
      tickets.remove(ticketNo);
   }

   public static void playerStateChange(PlayerState pState) {
      for(Ticket ticket : tickets.values()) {
         if (ticket.getPlayerId() == pState.getPlayerId() && ticket.isOpen()) {
            Players.getInstance().sendTicket(ticket, null);
         }
      }
   }

   private static void dbLoadTicketNos() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      boolean create = false;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM TICKETNOS");
         rs = ps.executeQuery();
         if (rs.next()) {
            nextTicketId = rs.getInt("NEXTTICKETID");
            lastTicketId = rs.getInt("LASTTICKETID");
            nextBatch = rs.getInt("NEXTBATCH");
         } else if (Servers.isThisLoginServer()) {
            nextTicketId = 10000;
            lastTicketId = 9999;
            nextBatch = 200000;
            create = true;
         } else {
            int lastDigits = Servers.getLocalServerId() % 100;
            nextTicketId = lastDigits * 10000;
            lastTicketId = nextTicketId + 9999;
            nextBatch = 0;
            create = true;
         }

         if (create) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            ps = dbcon.prepareStatement("INSERT INTO TICKETNOS (PK,NEXTTICKETID,LASTTICKETID,NEXTBATCH) VALUES(0,?,?,?)");
            ps.setInt(1, nextTicketId);
            ps.setInt(2, lastTicketId);
            ps.setInt(3, nextBatch);
            ps.executeUpdate();
         }
      } catch (SQLException var8) {
         logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbUpdateTicketNos() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("UPDATE TICKETNOS SET NEXTTICKETID=?, LASTTICKETID=?, NEXTBATCH=? WHERE PK=0");
         ps.setInt(1, nextTicketId);
         ps.setInt(2, lastTicketId);
         ps.setInt(3, nextBatch);
         if (ps.executeUpdate() == 0) {
            DbUtilities.closeDatabaseObjects(ps, null);
            ps = dbcon.prepareStatement("INSERT INTO TICKETNOS (PK,NEXTTICKETID,LASTTICKETID,NEXTBATCH) VALUES(0,?,?,?)");
            ps.setInt(1, nextTicketId);
            ps.setInt(2, lastTicketId);
            ps.setInt(3, nextBatch);
            ps.executeUpdate();
         }
      } catch (SQLException var6) {
         logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private static void dbLoadAllTickets() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         if (!DbConnector.isUseSqlite()) {
            ps = dbcon.prepareStatement(
               "DELETE FROM TICKETACTIONS USING TICKETACTIONS LEFT JOIN TICKETS ON TICKETS.TICKETID = TICKETACTIONS.TICKETID WHERE TICKETS.TICKETID IS NULL"
            );
            int purgedActions = ps.executeUpdate();
            if (purgedActions > 0) {
               logger.log(Level.INFO, "Purged " + purgedActions + " Ticket Actions from database.");
            }

            DbUtilities.closeDatabaseObjects(ps, null);
         }

         ps = dbcon.prepareStatement("SELECT * FROM TICKETS");
         rs = ps.executeQuery();

         while(rs.next()) {
            int aTicketId = rs.getInt("TICKETID");
            long aTicketDate = rs.getLong("TICKETDATE");
            long aPlayerWurmId = rs.getLong("PLAYERWURMID");
            String aPlayerName = rs.getString("PLAYERNAME");
            byte aTicketCategory = rs.getByte("CATEGORYCODE");
            int aServerId = rs.getInt("SERVERID");
            boolean aGloabl = rs.getBoolean("ISGLOBAL");
            long aClosedDate = rs.getLong("CLOSEDDATE");
            byte aState = rs.getByte("STATECODE");
            byte aLevel = rs.getByte("LEVELCODE");
            String aResponderName = rs.getString("RESPONDERNAME");
            String aDescription = rs.getString("DESCRIPTION");
            boolean isDirty = rs.getBoolean("ISDIRTY");
            short refFeedback = rs.getShort("REFFEEDBACK");
            String trelloFeedbackCardId = rs.getString("TRELLOFEEDBACKCARDID");
            String trelloCardId = rs.getString("TRELLOCARDID");
            byte trelloListCode = rs.getByte("TRELLOLISTCODE");
            boolean hasDescriptionChanged = rs.getBoolean("HASDESCRIPTIONCHANGED");
            boolean hasSummaryChanged = rs.getBoolean("HASSUMMARYCHANGED");
            boolean hasListChanged = rs.getBoolean("HASTRELLOLISTCHANGED");
            byte archiveCode = rs.getByte("ARCHIVESTATECODE");
            boolean acknowledged = rs.getBoolean("ACKNOWLEDGED");
            addTicket(
               new Ticket(
                  aTicketId,
                  aTicketDate,
                  aPlayerWurmId,
                  aPlayerName,
                  aTicketCategory,
                  aServerId,
                  aGloabl,
                  aClosedDate,
                  aState,
                  aLevel,
                  aResponderName,
                  aDescription,
                  isDirty,
                  refFeedback,
                  trelloFeedbackCardId,
                  trelloCardId,
                  trelloListCode,
                  hasDescriptionChanged,
                  hasSummaryChanged,
                  hasListChanged,
                  archiveCode,
                  acknowledged
               ),
               false
            );
         }

         DbUtilities.closeDatabaseObjects(ps, rs);
         ps = dbcon.prepareStatement("SELECT * FROM TICKETACTIONS");
         rs = ps.executeQuery();

         while(rs.next()) {
            int aTicketId = rs.getInt("TICKETID");
            short aActionNo = rs.getShort("ACTIONNO");
            long aActionDate = rs.getLong("ACTIONDATE");
            byte aAction = rs.getByte("ACTIONTYPE");
            String aByWhom = rs.getString("BYWHOM");
            String aNote = rs.getString("NOTE");
            byte aVisibilityLevel = rs.getByte("VISIBILITYLEVEL");
            boolean isDirty = rs.getBoolean("ISDIRTY");
            byte aQualityOfServiceCode = rs.getByte("QUALITYOFSERVICECODE");
            byte aCourteousCode = rs.getByte("COURTEOUSCODE");
            byte aKnowledgeableCode = rs.getByte("KNOWLEDGEABLECODE");
            byte aGeneralFlags = rs.getByte("GENERALFLAGS");
            byte aQualitiesFlags = rs.getByte("QUALITIESFLAGS");
            byte aIrkedFlags = rs.getByte("IRKEDFLAGS");
            String aTrelloLink = rs.getString("TRELLOCOMMENTID");
            getTicket(aTicketId)
               .addTicketAction(
                  new TicketAction(
                     aTicketId,
                     aActionNo,
                     aAction,
                     aActionDate,
                     aByWhom,
                     aNote,
                     aVisibilityLevel,
                     isDirty,
                     aQualityOfServiceCode,
                     aCourteousCode,
                     aKnowledgeableCode,
                     aGeneralFlags,
                     aQualitiesFlags,
                     aIrkedFlags,
                     aTrelloLink
                  )
               );
         }

         DbUtilities.closeDatabaseObjects(ps, rs);
      } catch (SQLException var31) {
         logger.log(Level.WARNING, var31.getMessage(), (Throwable)var31);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static String convertTime(long time) {
      return new SimpleDateFormat("dd/MMM/yyyy HH:mm").format(new Date(time));
   }

   public static final void addTicketToSend(Ticket ticket, int numbActions, TicketAction action) {
      ticketsToSend.add(new TicketToSend(ticket, numbActions, action));
   }

   public static final void addTicketToSend(Ticket ticket) {
      ticketsToSend.add(new TicketToSend(ticket));
   }

   public static final void addTicketToSend(Ticket ticket, TicketAction action) {
      ticketsToSend.add(new TicketToSend(ticket, action));
   }

   public static final void setTicketArchiveState(Ticket ticket, byte newArchiveState) {
      ticketsToUpdate.add(new TicketToUpdate(ticket, (byte)0, newArchiveState));
   }

   public static final void setTicketTrelloCardId(Ticket ticket, String aTrelloCardId) {
      ticketsToUpdate.add(new TicketToUpdate(ticket, (byte)1, aTrelloCardId));
   }

   public static final void setTicketTrelloFeedbackCardId(Ticket ticket, String aTrelloCardId) {
      ticketsToUpdate.add(new TicketToUpdate(ticket, (byte)2, aTrelloCardId));
   }

   public static final void setTicketIsDirty(Ticket ticket, boolean isDirty) {
      ticketsToUpdate.add(new TicketToUpdate(ticket, (byte)3, isDirty));
   }

   public static final void handleTicketsToSend() {
      for(TicketToSend ticketToSend = ticketsToSend.pollFirst(); ticketToSend != null; ticketToSend = ticketsToSend.pollFirst()) {
         ticketToSend.send();
      }

      for(TicketToUpdate ticketToUpdate = ticketsToUpdate.pollFirst(); ticketToUpdate != null; ticketToUpdate = ticketsToUpdate.pollFirst()) {
         ticketToUpdate.update();
      }
   }

   public static final void handleArchiveTickets() {
      for(Ticket ticket : tickets.values()) {
         if (ticket.getArchiveState() == 0 && ticket.isClosed() && ticket.getClosedDate() < System.currentTimeMillis() - 604800000L) {
            ticket.setArchiveState((byte)1);
            addTicketToSend(ticket);
         }
      }
   }

   public static final boolean checkForFlooding(long playerId) {
      int count = 0;

      for(Ticket ticket : tickets.values()) {
         if (ticket.getPlayerId() == playerId && ticket.isOpen() && ticket.getTicketDate() > System.currentTimeMillis() - 10800000L) {
            ++count;
         }
      }

      return count >= 3;
   }

   public static final void sendRequiresAckMessage(Player player) {
      for(Ticket ticket : tickets.values()) {
         if (ticket.getPlayerId() == player.getWurmId() && ticket.isWaitingAcknowledgement()) {
            String msg = "Your ticket " + ticket.getTicketName() + " has been ";
            if (ticket.getStateCode() == 2) {
               player.getCommunicator().sendSafeServerMessage(msg + "resolved.");
            } else {
               player.getCommunicator().sendSafeServerMessage(msg + "updated.");
            }
         }
      }
   }
}
