package com.wurmonline.server.intra;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TimeTransfer extends IntraCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(TimeTransfer.class.getName());
   private static final Logger moneylogger = Logger.getLogger("Money");
   private final String name;
   private final long wurmid;
   private final int monthsadded;
   private final int daysadded;
   private final boolean dealItems;
   private final String detail;
   public static final List<TimeTransfer> transfers = new LinkedList<>();
   private boolean done = false;
   private IntraClient client = null;
   private boolean started = false;
   public boolean deleted = false;
   private boolean sentTransfer = false;
   private static final Map<Long, Set<TimeTransfer>> batchTransfers = new HashMap<>();

   public TimeTransfer(String aName, long playerId, int months, boolean _dealItems, int days, String transactionDetail, boolean load) {
      this.name = aName;
      this.wurmid = playerId;
      this.monthsadded = months;
      this.daysadded = days;
      this.dealItems = _dealItems;
      this.detail = transactionDetail.substring(0, Math.min(19, transactionDetail.length()));
      if (!load) {
         this.save();
      }

      transfers.add(this);
   }

   public TimeTransfer(String aName, long playerId, int months, boolean _dealItems, int days, String transactionDetail) {
      this.name = aName;
      this.wurmid = playerId;
      this.monthsadded = months;
      this.daysadded = days;
      this.dealItems = _dealItems;
      this.detail = transactionDetail.substring(0, Math.min(19, transactionDetail.length()));
      this.saveProcessed();
   }

   public TimeTransfer(long playerId, String aName, int months, boolean _dealItems, int days, String transactionDetail) {
      this.name = aName;
      this.wurmid = playerId;
      this.monthsadded = months;
      this.daysadded = days;
      this.dealItems = _dealItems;
      this.detail = transactionDetail.substring(0, Math.min(19, transactionDetail.length()));
   }

   private void saveProcessed() {
   }

   private void save() {
   }

   private void process() {
      this.deleted = true;
   }

   public static final Set<TimeTransfer> getTimeTransfersFor(long id) {
      return batchTransfers.get(id);
   }

   @Override
   public boolean poll() {
      ++this.pollTimes;
      PlayerInfo info = PlayerInfoFactory.createPlayerInfo(this.name);

      try {
         info.load();
      } catch (Exception var7) {
         logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ".", (Throwable)var7);
      }

      if (info.wurmId <= 0L) {
         logger.log(Level.WARNING, "Failed to load info for wurmid " + this.wurmid + ". No info available.");
         this.done = true;
      } else if (info.currentServer == Servers.localServer.id) {
         this.process();
         this.done = true;
      } else if (this.client == null && (System.currentTimeMillis() > this.timeOutAt || !this.started)) {
         ServerEntry entry = Servers.getServerWithId(info.currentServer);
         if (entry != null && entry.isAvailable(5, true)) {
            try {
               this.started = true;
               this.startTime = System.currentTimeMillis();
               this.timeOutAt = this.startTime + this.timeOutTime;
               this.client = new IntraClient(entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), this);
               this.client.login(entry.INTRASERVERPASSWORD, true);
               this.done = false;
            } catch (IOException var6) {
               this.done = true;
            }
         } else {
            this.timeOutAt = this.startTime + this.timeOutTime;
            this.done = true;
            if (entry == null) {
               logger.log(Level.WARNING, "No server entry for server with id " + info.currentServer);
            }
         }
      }

      if (this.client != null && !this.done) {
         if (System.currentTimeMillis() > this.timeOutAt) {
            this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
            this.done = true;
         }

         if (this.client.loggedIn && !this.done && !this.sentTransfer) {
            try {
               this.timeOutAt = this.startTime + this.timeOutTime;
               this.client.executeExpireUpdate(this.wurmid, info.getPaymentExpire(), this.detail, this.daysadded, this.monthsadded, this.dealItems);
               this.sentTransfer = true;
            } catch (IOException var5) {
               logger.log(Level.WARNING, this + ", " + var5.getMessage(), (Throwable)var5);
               this.done = true;
            }
         }

         if (!this.done) {
            try {
               this.client.update();
            } catch (Exception var4) {
               this.done = true;
            }
         }
      }

      if (this.done && this.client != null) {
         this.sentTransfer = false;
         this.client.disconnect("Done");
         this.client = null;
      }

      return this.done;
   }

   @Override
   public void reschedule(IntraClient aClient) {
      this.done = true;
   }

   @Override
   public void remove(IntraClient aClient) {
      this.done = true;
   }

   @Override
   public void commandExecuted(IntraClient aClient) {
      this.process();
      logger2.log(Level.INFO, "TT accepted " + num);
      this.done = true;
   }

   @Override
   public void commandFailed(IntraClient aClient) {
      this.done = true;
      logger2.log(Level.INFO, "TT rejected " + num + " for " + this.wurmid + " " + this.name);
      this.deleted = true;
   }

   @Override
   public void dataReceived(IntraClient aClient) {
      this.done = true;
   }

   @Override
   public void receivingData(ByteBuffer buffer) {
      this.done = true;
   }

   public final int getDaysAdded() {
      return this.daysadded;
   }

   public final int getMonthsAdded() {
      return this.monthsadded;
   }

   @Override
   public String toString() {
      return "TimeTransfer [num: "
         + num
         + ", name: "
         + this.name
         + ", ID: "
         + this.wurmid
         + ", Months Added: "
         + this.monthsadded
         + ", Days Added: "
         + this.daysadded
         + ", detail: "
         + this.detail
         + ", done: "
         + this.done
         + ", started: "
         + this.started
         + ", deleted: "
         + this.deleted
         + ", sentTransfer: "
         + this.sentTransfer
         + ", IntraClient: "
         + this.client
         + ']';
   }
}
