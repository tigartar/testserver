/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerCommunicatorQueued;
import com.wurmonline.server.players.PlayerMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerCommunicatorSender
implements Runnable {
    private static Logger logger = Logger.getLogger(PlayerCommunicatorSender.class.getName());

    public PlayerCommunicatorSender() {
        logger.info("Creating");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        logger.info("Starting on " + Thread.currentThread());
        try {
            Player lPlayer = null;
            while (true) {
                PlayerMessage lMessage;
                if ((lMessage = PlayerCommunicatorQueued.getMessageQueue().take()) != null) {
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
                    }
                    catch (NoSuchPlayerException e) {
                        logger.log(Level.WARNING, "Could not find Player for Message: " + lMessage + " - " + e.getMessage(), e);
                    }
                    catch (IOException e) {
                        logger.log(Level.WARNING, lPlayer.getName() + ": Message: " + lMessage + " - " + e.getMessage(), e);
                        lPlayer.setLink(false);
                    }
                    Thread.yield();
                    continue;
                }
                logger.warning("Removed null message from Queue");
            }
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, "Problem running - " + e.getMessage(), e);
            Server.getInstance().initialisePlayerCommunicatorSender();
            logger.info("Finished");
        }
        catch (InterruptedException e) {
            try {
                logger.log(Level.WARNING, e.getMessage(), e);
                Server.getInstance().initialisePlayerCommunicatorSender();
            }
            catch (Throwable throwable) {
                throw throwable;
            }
            finally {
                logger.info("Finished");
            }
        }
    }
}

