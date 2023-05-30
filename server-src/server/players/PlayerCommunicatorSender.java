package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerCommunicatorSender implements Runnable {
   private static Logger logger = Logger.getLogger(PlayerCommunicatorSender.class.getName());

   public PlayerCommunicatorSender() {
      logger.info("Creating");
   }

   @Override
   public void run() {
      logger.info("Starting on " + Thread.currentThread());

      try {
         Player lPlayer = null;

         while(true) {
            PlayerMessage lMessage = PlayerCommunicatorQueued.getMessageQueue().take();
            if (lMessage != null) {
               if (logger.isLoggable(Level.FINEST)) {
                  logger.finest("Removed " + lMessage);
               }

               try {
                  lPlayer = Players.getInstance().getPlayer(lMessage.getPlayerId());
                  SocketConnection lConnection = lPlayer.getCommunicator().getConnection();
                  if (lPlayer.hasLink() && lConnection.isConnected()) {
                     ByteBuffer lBuffer = lConnection.getBuffer();
                     lBuffer.put(lMessage.getMessageBytes());
                     lConnection.flush();
                     if (!lConnection.tickWriting(1000000L)) {
                        logger.warning("Could not get a lock within 1ms to send message: " + lMessage);
                     } else if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("Sent message through connection: " + lMessage);
                     }
                  } else if (logger.isLoggable(Level.FINEST)) {
                     logger.finest("Player is not connected so cannot send message: " + lMessage);
                  }
               } catch (NoSuchPlayerException var12) {
                  logger.log(Level.WARNING, "Could not find Player for Message: " + lMessage + " - " + var12.getMessage(), (Throwable)var12);
               } catch (IOException var13) {
                  logger.log(Level.WARNING, lPlayer.getName() + ": Message: " + lMessage + " - " + var13.getMessage(), (Throwable)var13);
                  lPlayer.setLink(false);
               }

               Thread.yield();
            } else {
               logger.warning("Removed null message from Queue");
            }
         }
      } catch (RuntimeException var14) {
         logger.log(Level.WARNING, "Problem running - " + var14.getMessage(), (Throwable)var14);
         Server.getInstance().initialisePlayerCommunicatorSender();
      } catch (InterruptedException var15) {
         logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
         Server.getInstance().initialisePlayerCommunicatorSender();
      } finally {
         logger.info("Finished");
      }
   }
}
