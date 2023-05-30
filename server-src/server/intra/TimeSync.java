package com.wurmonline.server.intra;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TimeSync extends IntraCommand {
   private static final Logger logger = Logger.getLogger(TimeSync.class.getName());
   private boolean done = false;
   private IntraClient client;
   private boolean sentCommand = false;
   private boolean started = false;

   @Override
   public boolean poll() {
      if (this.isThisLoginServer()) {
         return true;
      } else {
         if (this.client == null && (System.currentTimeMillis() > this.timeOutAt || !this.started)) {
            try {
               this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
               this.client = new IntraClient(this.getLoginServerIntraServerAddress(), Integer.parseInt(this.getLoginServerIntraServerPort()), this);
               this.client.login(this.getLoginServerIntraServerPassword(), true);
               this.started = true;
            } catch (IOException var4) {
               this.done = true;
            }
         }

         if (this.client != null && !this.done) {
            if (System.currentTimeMillis() > this.timeOutAt) {
               this.done = true;
            } else if (this.client != null && this.client.loggedIn) {
               if (logger.isLoggable(Level.FINER)) {
                  logger.finer("3.5 sentcommand=" + this.sentCommand);
               }

               if (!this.sentCommand) {
                  try {
                     this.client.executeSyncCommand();
                     this.timeOutAt = System.currentTimeMillis() + this.timeOutTime;
                     this.sentCommand = true;
                  } catch (IOException var3) {
                     logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
                     this.done = true;
                  }
               }
            }

            if (!this.done) {
               try {
                  this.client.update();
               } catch (Exception var2) {
                  logger.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
                  this.done = true;
               }
            }
         }

         if (this.client != null && this.done) {
            this.client.disconnect("Done");
            this.client = null;
         }

         return this.done;
      }
   }

   @Override
   public void commandExecuted(IntraClient aClient) {
      this.done = true;
   }

   @Override
   public void commandFailed(IntraClient aClient) {
      logger.warning("Command failed for Client: " + aClient);
      this.done = true;
   }

   @Override
   public void dataReceived(IntraClient aClient) {
      this.done = true;
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
   public void receivingData(ByteBuffer buffer) {
      this.done = true;
   }

   public static class TimeSyncSender implements Runnable {
      public TimeSyncSender() {
         if (Servers.isThisLoginServer()) {
            throw new IllegalArgumentException("Do not send TimeSync commands from the LoginServer");
         }
      }

      @Override
      public void run() {
         if (TimeSync.logger.isLoggable(Level.FINER)) {
            TimeSync.logger.finer("Running newSingleThreadScheduledExecutor for sending TimeSync commands");
         }

         try {
            long now = System.currentTimeMillis();
            TimeSync synch = new TimeSync();
            Server.getInstance().addIntraCommand(synch);
            long lElapsedTime = System.currentTimeMillis() - now;
            if (lElapsedTime > Constants.lagThreshold) {
               TimeSync.logger.info("Finished sending TimeSync command, which took " + lElapsedTime + " millis.");
            }
         } catch (RuntimeException var6) {
            TimeSync.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while sending TimeSync command", (Throwable)var6);
            throw var6;
         }
      }
   }
}
