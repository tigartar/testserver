package com.wurmonline.server.intra;

import com.wurmonline.communication.ServerListener;
import com.wurmonline.communication.SocketConnection;
import com.wurmonline.communication.SocketServer;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerMonitoring;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IntraServer implements ServerListener, CounterTypes, MiscConstants, TimeConstants {
   private static final Logger logger = Logger.getLogger(IntraServer.class.getName());
   public SocketServer socketServer;
   private final ServerMonitoring wurmserver;

   public IntraServer(ServerMonitoring server) throws IOException {
      this.wurmserver = server;
      this.socketServer = new SocketServer(server.getInternalIp(), server.getIntraServerPort(), server.getIntraServerPort() + 1, this);
      this.socketServer.intraServer = true;
      logger.log(Level.INFO, "Intraserver listening on " + InetAddress.getByAddress(server.getInternalIp()) + ':' + server.getIntraServerPort());
   }

   @Override
   public void clientConnected(SocketConnection serverConnection) {
      try {
         IntraServerConnection conn = new IntraServerConnection(serverConnection, this.wurmserver);
         serverConnection.setConnectionListener(conn);
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("IntraServer client connected from IP " + serverConnection.getIp());
         }
      } catch (Exception var3) {
         logger.log(Level.SEVERE, "Failed to create intraserver connection: " + serverConnection + '.', (Throwable)var3);
      }
   }

   @Override
   public void clientException(SocketConnection conn, Exception ex) {
      if (logger.isLoggable(Level.FINE)) {
         logger.log(Level.FINE, "Remote server lost link on connection: " + conn + " - cause:" + ex.getMessage(), (Throwable)ex);
      }

      if (conn != null) {
         try {
            conn.flush();
         } catch (Exception var5) {
         }

         conn.sendShutdown();

         try {
            conn.disconnect();
         } catch (Exception var4) {
         }

         conn.closeChannel();
      }
   }
}
