package com.wurmonline.server.intra;

import com.wurmonline.communication.SimpleConnectionListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmCalendar;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IntraClient implements SimpleConnectionListener, MiscConstants {
   private SocketConnection connection;
   private IntraServerConnectionListener serverConnectionListener;
   private boolean disconnected = true;
   public boolean loggedIn = false;
   private String disconnectReason = "Lost link.";
   int retryInSeconds = 0;
   long timeDifference = 0L;
   private static final int DATABUFSIZE = 16384;
   private static final int TRANSFERSIZE = 16366;
   private static Logger logger = Logger.getLogger(IntraClient.class.getName());
   public boolean isConnecting = false;
   public boolean hasFailedConnection = false;
   protected static final String CHARSET_ENCODING_FOR_COMMS = "UTF-8";

   public IntraClient(String serverIp, int serverPort, IntraServerConnectionListener aServerConnectionListener) throws IOException {
      this.reconnect(serverIp, serverPort, aServerConnectionListener);
   }

   public IntraClient() {
   }

   private void reconnect(String serverIp, int serverPort, IntraServerConnectionListener aServerConnectionListener) throws IOException {
      this.serverConnectionListener = aServerConnectionListener;
      this.connection = new SocketConnection(serverIp, serverPort, 20000);
      this.connection.setMaxBlocksPerIteration(1000000);
      this.connection.setConnectionListener(this);
      this.disconnected = false;
   }

   public void reconnectAsynch(final String serverIp, final int serverPort, IntraServerConnectionListener aServerConnectionListener) {
      this.isConnecting = true;
      this.serverConnectionListener = aServerConnectionListener;
      final IntraClient c = this;
      (new Thread() {
         @Override
         public void run() {
            try {
               IntraClient.this.connection = new SocketConnection(serverIp, serverPort, 20000);
               IntraClient.this.connection.setMaxBlocksPerIteration(1000000);
               IntraClient.this.connection.setConnectionListener(c);
               IntraClient.this.disconnected = false;
               IntraClient.this.isConnecting = false;
            } catch (IOException var2) {
               IntraClient.this.hasFailedConnection = true;
            }
         }
      }).start();
   }

   public void login(String password, boolean dev) {
      if (this.retryInSeconds <= 0) {
         try {
            byte[] passwordBytes = password.getBytes("UTF-8");
            ByteBuffer buf = this.connection.getBuffer();
            buf.put((byte)1);
            buf.putInt(1);
            buf.put((byte)passwordBytes.length);
            buf.put(passwordBytes);
            buf.put((byte)(dev ? 1 : 0));
            this.connection.flush();
            this.retryInSeconds = 0;
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("Client sent login");
            }
         } catch (IOException var5) {
            logger.log(Level.WARNING, "Failed to login", (Throwable)var5);
            this.serverConnectionListener.commandFailed(this);
         }
      } else {
         --this.retryInSeconds;
      }
   }

   public synchronized void update() throws IOException {
      if (this.disconnected) {
         throw new IOException(this.disconnectReason);
      } else {
         try {
            this.connection.tick();
         } catch (Exception var2) {
            if (logger.isLoggable(Level.FINE)) {
               logger.log(Level.FINE, "Failed to update on connection: " + this.connection, (Throwable)var2);
            }

            this.serverConnectionListener.commandFailed(this);
         }
      }
   }

   public synchronized void disconnect(String reason) {
      if (logger.isLoggable(Level.FINE) && reason != null && reason.equals("Done")) {
         logger.log(Level.FINE, "Disconnecting connection: " + this.connection + ", reason: " + reason);
      }

      if (this.connection != null && this.connection.isConnected()) {
         try {
            this.sendDisconnect();
         } catch (Exception var4) {
         }

         try {
            this.connection.sendShutdown();
            this.connection.disconnect();
            this.connection.closeChannel();
         } catch (Exception var3) {
         }
      }

      this.disconnectReason = reason;
      this.disconnected = true;
      this.loggedIn = false;
      if (this.serverConnectionListener != null) {
         this.serverConnectionListener.remove(this);
      }
   }

   @Override
   public void reallyHandle(int num, ByteBuffer bb) {
      try {
         byte cmd = bb.get();
         if (logger.isLoggable(Level.FINER)) {
            logger.finer("Received cmd " + cmd);
         }

         if (cmd == 2) {
            this.loggedIn = bb.get() != 0;
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest("This client is loggedin=" + this.loggedIn);
            }

            byte[] bytes = new byte[bb.getShort() & '\uffff'];
            bb.get(bytes);
            String message = new String(bytes, "UTF-8");
            this.retryInSeconds = bb.getShort() & '\uffff';
            long targetNow = bb.getLong();
            this.timeDifference = targetNow - System.currentTimeMillis();
            if (!this.loggedIn) {
               logger.log(Level.WARNING, "Login Failed: " + message);
               this.serverConnectionListener.commandFailed(this);
            } else if (logger.isLoggable(Level.FINER)) {
               logger.finer("Client logged in - message: " + message + ", " + this);
            }
         } else if (cmd == 6) {
            boolean ok = bb.get() != 0;
            byte[] bytes = new byte[bb.getShort() & '\uffff'];
            bb.get(bytes);
            new String(bytes, "UTF-8");
            this.retryInSeconds = bb.getShort() & '\uffff';
            long targetNow = bb.getLong();
            this.timeDifference = targetNow - System.currentTimeMillis();
            if (this.retryInSeconds > 0) {
               this.serverConnectionListener.commandFailed(this);
            } else if (!ok) {
               this.serverConnectionListener.commandFailed(this);
            } else if (logger.isLoggable(Level.FINE)) {
               logger.fine("Client received transferrequest ok - " + this);
            }
         } else if (cmd == 10) {
            long oldTime = WurmCalendar.currentTime;
            long wurmTime = bb.getLong();
            WurmCalendar.currentTime = wurmTime;
            logger.log(
               Level.INFO, "The server just synched wurm clock. New wurm time=" + wurmTime + ". Difference was " + (oldTime - wurmTime) + " wurm seconds."
            );
            this.serverConnectionListener.commandExecuted(this);
         } else if (cmd == 4) {
            this.serverConnectionListener.commandExecuted(this);
         } else if (cmd == 5) {
            this.serverConnectionListener.commandFailed(this);
         } else if (cmd == 8) {
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest("Client received data received.");
            }

            this.serverConnectionListener.dataReceived(this);
         } else if (cmd == 9) {
            this.serverConnectionListener.receivingData(bb);
         } else if (cmd == 11) {
            this.serverConnectionListener.receivingData(bb);
         } else if (cmd == 14) {
            this.serverConnectionListener.reschedule(this);
         } else if (cmd == 13) {
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest("IntraClient received PONG - " + this);
            }

            this.serverConnectionListener.receivingData(bb);
         } else {
            logger.warning("Ignoring unknown cmd " + cmd);
            System.out.println("Ignoring unknown cmd " + cmd);
         }
      } catch (Exception var9) {
         logger.log(Level.WARNING, "Problem handling Block: " + bb, (Throwable)var9);
         var9.printStackTrace();
      }
   }

   int sendNextDataPart(byte[] data, int index) throws IOException {
      ByteBuffer buf = this.connection.getBuffer();
      int length = Math.min(data.length - index, 16366);
      int nextindex = index + length;
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Sending " + length + " out of " + data.length + " up to " + nextindex + " max size is " + 16384);
      }

      buf.put((byte)7);
      buf.putInt(length);
      buf.put(data, index, length);
      buf.put((byte)(nextindex == data.length ? 1 : 0));
      this.connection.flush();
      return nextindex;
   }

   void executePlayerTransferRequest(int posx, int posy, boolean surfaced) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Requesting player transfer for coordinates: " + posx + ", " + posy + ", surfaced: " + surfaced + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)3);
      buf.putInt(posx);
      buf.putInt(posy);
      buf.put((byte)(surfaced ? 1 : 0));
      this.connection.flush();
   }

   void executeSyncCommand() throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Synchronising the time - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)10);
      this.connection.flush();
   }

   void executeRequestPlayerVersion(long playerId) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Requesting player version for player id: " + playerId + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)9);
      buf.putLong(playerId);
      this.connection.flush();
   }

   void executeRequestPlayerPaymentExpire(long playerId) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Requesting player payment expire for player id: " + playerId + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)11);
      buf.putLong(playerId);
      this.connection.flush();
   }

   void executeMoneyUpdate(long playerId, long currentMoney, long moneyAdded, String detail) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Updating money update for player id: " + playerId + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)16);
      buf.putLong(playerId);
      buf.putLong(currentMoney);
      buf.putLong(moneyAdded);
      byte[] det = detail.getBytes("UTF-8");
      buf.putInt(det.length);
      buf.put(det);
      this.connection.flush();
   }

   void executeExpireUpdate(long playerId, long currentExpire, String detail, int days, int months, boolean dealItems) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Updating expire update for player id: " + playerId + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)17);
      buf.putLong(playerId);
      buf.putLong(currentExpire);
      buf.putInt(days);
      buf.putInt(months);
      buf.put((byte)(dealItems ? 1 : 0));
      byte[] det = detail.getBytes("UTF-8");
      buf.putInt(det.length);
      buf.put(det);
      this.connection.flush();
   }

   void executePasswordUpdate(long playerId, String currentHashedPassword, long timestamp) throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Updating password for player id: " + playerId + " at timestamp " + timestamp + " - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)18);
      buf.putLong(playerId);
      byte[] pw = currentHashedPassword.getBytes("UTF-8");
      buf.putInt(pw.length);
      buf.put(pw);
      buf.putLong(timestamp);
      this.connection.flush();
   }

   public void executePingCommand() throws IOException {
      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)13);
      this.connection.flush();
   }

   static final void sendShortStringLength(String toSend, ByteBuffer bb) throws Exception {
      byte[] toSendStringArr = toSend.getBytes("UTF-8");
      bb.putShort((short)toSendStringArr.length);
      bb.put(toSendStringArr);
   }

   private void sendDisconnect() throws IOException {
      if (logger.isLoggable(Level.FINEST)) {
         logger.finest("Client sending disconnect - " + this);
      }

      ByteBuffer buf = this.connection.getBuffer();
      buf.put((byte)15);
      this.connection.flush();
   }

   @Override
   public String toString() {
      return "IntraClient [SocketConnection: "
         + this.connection
         + ", disconnected: "
         + this.disconnected
         + ", loggedIn: "
         + this.loggedIn
         + ", disconnectReason: "
         + this.disconnectReason
         + ']';
   }
}
