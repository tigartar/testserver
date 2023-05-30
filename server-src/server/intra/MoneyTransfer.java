package com.wurmonline.server.intra;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MoneyTransfer extends IntraCommand implements MiscConstants {
   private static final Logger logger = Logger.getLogger(MoneyTransfer.class.getName());
   private static final Logger moneylogger = Logger.getLogger("Money");
   private final String name;
   private final long wurmid;
   private final long newMoney;
   private final long moneyAdded;
   private final String detail;
   private boolean done = false;
   private IntraClient client = null;
   private boolean started = false;
   public boolean deleted = false;
   private boolean sentTransfer = false;
   public static final byte EXECUTOR_NONE = 0;
   public static final byte EXECUTOR_BLVDMEDIA = 1;
   public static final byte EXECUTOR_SUPERREWARDS = 2;
   public static final byte EXECUTOR_INGAME_BONUS = 3;
   public static final byte EXECUTOR_PAYPAL = 4;
   public static final byte EXECUTOR_INGAME_REFER = 5;
   public static final byte EXECUTOR_INGAME_SHOP = 6;
   public static final byte EXECUTOR_ALLOPASS = 7;
   public static final byte EXECUTOR_COINLAB = 8;
   public static final byte EXECUTOR_XSOLLA = 9;
   private final byte paymentExecutor;
   private final String campaignId;
   public static final ConcurrentLinkedDeque<MoneyTransfer> transfers = new ConcurrentLinkedDeque<>();
   private static final Map<Long, Set<MoneyTransfer>> batchTransfers = new HashMap<>();

   public MoneyTransfer(String aName, long playerId, long money, long _moneyAdded, String transactionDetail, byte executor, String campid, boolean load) {
      this.name = aName;
      this.wurmid = playerId;
      this.newMoney = money;
      this.detail = transactionDetail.substring(0, Math.min(39, transactionDetail.length()));
      this.paymentExecutor = executor;
      this.campaignId = campid;
      this.moneyAdded = _moneyAdded;
      if (!load) {
         this.save();
      }

      transfers.add(this);
   }

   public MoneyTransfer(long playerId, String aName, long money, long _moneyAdded, String transactionDetail, byte executor, String campid) {
      this.name = aName;
      this.wurmid = playerId;
      this.newMoney = money;
      this.detail = transactionDetail.substring(0, Math.min(39, transactionDetail.length()));
      this.paymentExecutor = executor;
      this.campaignId = campid;
      this.moneyAdded = _moneyAdded;
   }

   public MoneyTransfer(String aName, long playerId, long money, long _moneyAdded, String transactionDetail, byte executor, String campId) {
      this.name = aName;
      this.wurmid = playerId;
      this.newMoney = money;
      this.moneyAdded = _moneyAdded;
      this.detail = transactionDetail.substring(0, Math.min(39, transactionDetail.length()));
      this.campaignId = campId;
      this.paymentExecutor = executor;
      this.saveProcessed();
   }

   private void saveProcessed() {
      this.deleted = true;
   }

   private void save() {
   }

   public void process() {
      this.deleted = true;
   }

   public static final Set<MoneyTransfer> getMoneyTransfersFor(long id) {
      return batchTransfers.get(id);
   }

   @Override
   public boolean poll() {
      ++this.pollTimes;
      PlayerInfo info = PlayerInfoFactory.createPlayerInfo(this.name);

      try {
         info.load();
      } catch (Exception var6) {
         logger.log(Level.WARNING, "Failed to load player info for wurmid " + this.wurmid + ".", (Throwable)var6);
      }

      if (info.wurmId <= 0L) {
         logger.log(Level.WARNING, "Failed to load player info for wurmid " + this.wurmid + ". No info available.");
         this.done = true;
      } else if (info.currentServer == Servers.localServer.id) {
         logger2.log(Level.INFO, "MT Processing " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", money: " + this.newMoney);
         this.process();
         this.done = true;
      } else if (this.client == null && (System.currentTimeMillis() > this.timeOutAt || !this.started)) {
         ServerEntry entry = Servers.getServerWithId(info.currentServer);
         if (entry != null && entry.isAvailable(5, true)) {
            try {
               this.startTime = System.currentTimeMillis();
               this.timeOutAt = this.startTime + this.timeOutTime;
               this.started = true;
               this.client = new IntraClient(entry.INTRASERVERADDRESS, Integer.parseInt(entry.INTRASERVERPORT), this);
               this.client.login(entry.INTRASERVERPASSWORD, true);
               this.done = false;
            } catch (IOException var5) {
               this.done = true;
            }
         } else {
            this.timeOutAt = this.startTime + this.timeOutTime;
            this.done = true;
            if (entry == null) {
               logger.log(Level.WARNING, "No available server entry for server with id " + info.currentServer);
            }
         }
      }

      if (this.client != null && !this.done) {
         if (System.currentTimeMillis() > this.timeOutAt) {
            logger2.log(Level.INFO, "MT timeout " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", money: " + this.newMoney);
            this.done = true;
         }

         if (this.client.loggedIn && !this.done && !this.sentTransfer) {
            try {
               this.client.executeMoneyUpdate(this.wurmid, this.newMoney, this.moneyAdded, this.detail);
               this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
               this.sentTransfer = true;
            } catch (IOException var4) {
               logger2.log(Level.WARNING, "Problem calling IntraClient.executeMoneyUpdate() for " + this + " due to " + var4.getMessage(), (Throwable)var4);
               this.done = true;
            }
         }

         if (!this.done) {
            try {
               this.client.update();
            } catch (Exception var7) {
               this.done = true;
               if (logger.isLoggable(Level.FINE)) {
                  logger.log(Level.FINE, "Problem calling IntraClient.update() but hopefully not serious", (Throwable)var7);
               }
            }
         }
      }

      if (this.done && this.client != null) {
         this.sentTransfer = false;
         this.client.disconnect("Done");
         this.client = null;
         logger2.log(Level.INFO, "MT Disconnected " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", money: " + this.newMoney);
      }

      return this.done;
   }

   public final long getMoneyAdded() {
      return this.moneyAdded;
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
      logger2.log(Level.INFO, "MT accepted " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", money: " + this.newMoney);
      this.done = true;
   }

   @Override
   public void commandFailed(IntraClient aClient) {
      this.done = true;
      logger2.log(Level.INFO, "MT rejected " + num + ", name: " + this.name + ", wurmid: " + this.wurmid + ", money: " + this.newMoney);
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

   @Override
   public String toString() {
      return "MoneyTransfer [num: "
         + num
         + ", wurmid: "
         + this.wurmid
         + ", name: "
         + this.name
         + ", detail: "
         + this.detail
         + ", newMoney: "
         + this.newMoney
         + ", moneyAdded: "
         + this.moneyAdded
         + ']';
   }
}
