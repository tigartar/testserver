/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.communication;

import com.wurmonline.communication.ServerListener;
import com.wurmonline.communication.SocketConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {
    private final ServerSocketChannel ssc;
    private final ServerListener serverListener;
    private final List<SocketConnection> connections = new LinkedList<SocketConnection>();
    public static final ReentrantReadWriteLock CONNECTIONS_RW_LOCK = new ReentrantReadWriteLock();
    private final int acceptedPort;
    public boolean intraServer = false;
    private static final Logger logger = Logger.getLogger(SocketServer.class.getName());
    private static Map<String, Long> connectedIps = new HashMap<String, Long>();
    public static long MIN_MILLIS_BETWEEN_CONNECTIONS = 1000L;

    public SocketServer(byte[] ips, int port, int acceptedPort, ServerListener serverListener) throws IOException {
        this.serverListener = serverListener;
        this.acceptedPort = acceptedPort;
        InetAddress hostip = InetAddress.getByAddress(ips);
        logger.info("Creating Wurm SocketServer on " + hostip + ':' + port);
        this.ssc = ServerSocketChannel.open();
        this.ssc.socket().bind(new InetSocketAddress(hostip, port));
        this.ssc.configureBlocking(false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void tick() throws IOException {
        SocketConnection socketConnection;
        SocketChannel socketChannel;
        while ((socketChannel = this.ssc.accept()) != null) {
            try {
                if (socketChannel.socket().getPort() != this.acceptedPort) {
                    if (!this.intraServer) {
                        logger.log(Level.INFO, "Accepted player connection: " + socketChannel.socket());
                    }
                } else if (!this.intraServer) {
                    logger.log(Level.INFO, socketChannel.socket().getRemoteSocketAddress() + " connected from the correct port");
                }
                boolean keepGoing = true;
                if (!this.intraServer && MIN_MILLIS_BETWEEN_CONNECTIONS > 0L) {
                    String remoteIp = socketChannel.socket().getRemoteSocketAddress().toString().substring(0, socketChannel.socket().getRemoteSocketAddress().toString().indexOf(":"));
                    Long lastConnTime = connectedIps.get(remoteIp);
                    if (lastConnTime != null) {
                        long lct = lastConnTime;
                        if (System.currentTimeMillis() - lct < MIN_MILLIS_BETWEEN_CONNECTIONS) {
                            logger.log(Level.INFO, "Disconnecting " + remoteIp + " due to too many connections.");
                            if (socketChannel != null && socketChannel.socket() != null) {
                                try {
                                    socketChannel.socket().close();
                                }
                                catch (IOException iox) {
                                    iox.printStackTrace();
                                }
                            }
                            if (socketChannel != null) {
                                try {
                                    socketChannel.close();
                                }
                                catch (IOException iox) {
                                    iox.printStackTrace();
                                }
                            }
                            keepGoing = false;
                        }
                    } else {
                        connectedIps.put(remoteIp, new Long(System.currentTimeMillis()));
                    }
                }
                if (!keepGoing) continue;
                socketChannel.configureBlocking(false);
                socketConnection = new SocketConnection(socketChannel, true, this.intraServer);
                CONNECTIONS_RW_LOCK.writeLock().lock();
                try {
                    this.connections.add(socketConnection);
                }
                finally {
                    CONNECTIONS_RW_LOCK.writeLock().unlock();
                }
                this.serverListener.clientConnected(socketConnection);
            }
            catch (IOException e) {
                try {
                    socketChannel.close();
                }
                catch (Exception socketConnection2) {
                    // empty catch block
                }
                throw e;
            }
        }
        CONNECTIONS_RW_LOCK.writeLock().lock();
        try {
            Iterator<SocketConnection> it = this.connections.iterator();
            while (it.hasNext()) {
                socketConnection = it.next();
                if (!socketConnection.isConnected()) {
                    socketConnection.disconnect();
                    this.serverListener.clientException(socketConnection, new Exception());
                    it.remove();
                    continue;
                }
                try {
                    socketConnection.tick();
                }
                catch (Exception e) {
                    socketConnection.disconnect();
                    this.serverListener.clientException(socketConnection, e);
                    it.remove();
                }
            }
        }
        finally {
            CONNECTIONS_RW_LOCK.writeLock().unlock();
        }
    }

    public int getNumberOfConnections() {
        CONNECTIONS_RW_LOCK.readLock().lock();
        try {
            if (this.connections != null) {
                int n = this.connections.size();
                return n;
            }
            int n = 0;
            return n;
        }
        finally {
            CONNECTIONS_RW_LOCK.readLock().unlock();
        }
    }
}

