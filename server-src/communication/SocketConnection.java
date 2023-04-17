/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.communication;

import com.wurmonline.communication.SimpleConnectionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketConnection {
    private static final Logger logger = Logger.getLogger(SocketConnection.class.getName());
    private static final String CLASS_NAME = SocketConnection.class.getName();
    public static final int BUFFER_SIZE = 262136;
    private final ByteBuffer writeBufferTmp = ByteBuffer.allocate(65534);
    private ByteBuffer readBuffer = ByteBuffer.allocate(262136);
    private ByteBuffer writeBuffer_w = null;
    private ByteBuffer writeBuffer_r = null;
    public static final long timeOutTime = 300000L;
    public static final long disconTime = 5000L;
    private boolean connected;
    private boolean playerServerConnection = false;
    private SocketChannel socketChannel;
    private long lastRead = System.currentTimeMillis();
    private SimpleConnectionListener connectionListener;
    private int toRead = -1;
    private volatile boolean writing;
    private int bytesRead;
    private int totalBytesWritten;
    private int maxBlocksPerIteration = 3;
    private boolean isLoggedIn = true;
    public int ticksToDisconnect = -1;
    private Socket socket;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    public Random encryptRandom = new Random(105773331L);
    private int remainingEncryptBytes = 0;
    private int encryptByte = 0;
    private int encryptAddByte = 0;
    public Random decryptRandom = new Random(105773331L);
    private int remainingDencryptBytes = 0;
    private int dencryptByte = 0;
    private int decryptAddByte = 0;
    private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private boolean callTickWritingFromTick = true;
    static long maxRead = 0L;
    static int maxTotalRead = 0;
    static int maxTotalReadAllowed = 20000;
    static int maxReadAllowed = 20000;

    SocketConnection(SocketChannel socketChannel, boolean enableNagles, boolean intraServer) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        this.socket = socketChannel.socket();
        boolean bl = this.playerServerConnection = !intraServer;
        if (this.playerServerConnection) {
            this.readBuffer = ByteBuffer.allocate(262136);
            this.writeBuffer_w = ByteBuffer.allocate(Short.MAX_VALUE);
            this.writeBuffer_r = ByteBuffer.allocate(Short.MAX_VALUE);
        } else {
            this.readBuffer = ByteBuffer.allocate(262136);
            this.writeBuffer_w = ByteBuffer.allocate(262136);
            this.writeBuffer_r = ByteBuffer.allocate(262136);
        }
        if (!enableNagles) {
            System.out.println("Disabling Nagles");
            socketChannel.socket().setTcpNoDelay(true);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SocketChannel validOps: " + socketChannel.validOps() + ", isConnected: " + socketChannel.isConnected() + ", isOpen: " + socketChannel.isOpen() + ", isRegistered: " + socketChannel.isRegistered() + ", socket: " + socketChannel.socket());
        }
        this.connected = true;
        this.readBuffer.clear();
        this.readBuffer.limit(2);
        this.writing = false;
        this.writeBuffer_w.clear();
        this.writeBuffer_r.flip();
        this.isLoggedIn = false;
    }

    protected SocketConnection(String ip, int port, boolean enableNagles) throws UnknownHostException, IOException {
        this.readBuffer = ByteBuffer.allocate(262136);
        this.writeBuffer_w = ByteBuffer.allocate(262136);
        this.writeBuffer_r = ByteBuffer.allocate(262136);
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASS_NAME, "SocketConnection", new Object[]{ip, port});
        }
        this.socketChannel = SocketChannel.open();
        this.socketChannel.connect(new InetSocketAddress(ip, port));
        if (!enableNagles) {
            System.out.println("Disabling Nagles");
            this.socketChannel.socket().setTcpNoDelay(true);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SocketChannel validOps: " + this.socketChannel.validOps() + ", isConnected: " + this.socketChannel.isConnected() + ", isOpen: " + this.socketChannel.isOpen() + ", isRegistered: " + this.socketChannel.isRegistered() + ", socket: " + this.socketChannel.socket());
        }
        this.socketChannel.configureBlocking(false);
        this.connected = true;
        this.readBuffer.clear();
        this.readBuffer.limit(2);
        this.writing = false;
        this.writeBuffer_w.clear();
        this.writeBuffer_r.flip();
    }

    public SocketConnection(String ip, int port, int timeout) throws UnknownHostException, IOException {
        this(ip, port, timeout, true);
    }

    SocketConnection(String ip, int port, int timeout, boolean enableNagles) throws UnknownHostException, IOException {
        this.readBuffer = ByteBuffer.allocate(262136);
        this.writeBuffer_w = ByteBuffer.allocate(262136);
        this.writeBuffer_r = ByteBuffer.allocate(262136);
        this.socketChannel = SocketChannel.open();
        this.socketChannel.socket().setSoTimeout(timeout);
        this.socketChannel.connect(new InetSocketAddress(ip, port));
        if (!enableNagles) {
            System.out.println("Disabling Nagles");
            this.socketChannel.socket().setTcpNoDelay(true);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("SocketChannel validOps: " + this.socketChannel.validOps() + ", isConnected: " + this.socketChannel.isConnected() + ", isOpen: " + this.socketChannel.isOpen() + ", isRegistered: " + this.socketChannel.isRegistered() + ", socket: " + this.socketChannel.socket());
        }
        this.socketChannel.configureBlocking(false);
        this.connected = true;
        this.readBuffer.clear();
        this.readBuffer.limit(2);
        this.writing = false;
        this.writeBuffer_w.clear();
        this.writeBuffer_r.flip();
    }

    public void setMaxBlocksPerIteration(int aMaxBlocksPerIteration) {
        this.maxBlocksPerIteration = aMaxBlocksPerIteration;
    }

    public String getIp() {
        return this.socket.getInetAddress().toString();
    }

    public ByteBuffer getBuffer() {
        if (this.writing) {
            throw new IllegalStateException("getBuffer() called twice in a row. You probably forgot to flush()");
        }
        this.writing = true;
        this.writeBufferTmp.clear();
        return this.writeBufferTmp;
    }

    public void clearBuffer() {
        if (this.writing) {
            this.writing = false;
            this.writeBufferTmp.clear();
        }
    }

    public int getUnflushed() {
        return this.writeBuffer_w.position() + this.writeBuffer_r.remaining();
    }

    public void flush() throws IOException {
        if (!this.writing) {
            throw new IllegalStateException("flush() called twice in a row.");
        }
        this.writing = false;
        this.writeBufferTmp.flip();
        int bytesWritten = this.writeBufferTmp.limit();
        this.totalBytesWritten += bytesWritten;
        if (bytesWritten > 65524) {
            logger.log(Level.WARNING, "WARNING Written " + bytesWritten, new Exception());
        }
        if (this.writeBuffer_w.remaining() < bytesWritten + 2) {
            if (!this.tickWriting(0L)) {
                throw new IOException("BufferOverflow: Tried to write " + (bytesWritten + 2) + " bytes, but only " + this.writeBuffer_w.remaining() + " bytes remained. Written=" + this.totalBytesWritten + ", BufferTmp: " + this.writeBufferTmp + ", Buffer_w: " + this.writeBuffer_w + ", Buffer_r: " + this.writeBuffer_r);
            }
            logger.log(Level.INFO, "Possibly saved client crash by forcing a write of the writeBuffer_w.");
        }
        if (logger.isLoggable(Level.FINEST) && (bytesWritten > 1 || logger.isLoggable(Level.FINEST))) {
            logger.finer("Number of bytes in the write buffer: " + bytesWritten);
        }
        int startPos = this.writeBuffer_w.position();
        this.writeBuffer_w.putShort((short)bytesWritten);
        this.writeBuffer_w.put(this.writeBufferTmp);
        int endPos = this.writeBuffer_w.position();
        this.encrypt(this.writeBuffer_w, startPos, endPos);
    }

    public void setConnectionListener(SimpleConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public boolean isConnected() {
        if (this.playerServerConnection && (this.isLoggedIn ? this.lastRead < System.currentTimeMillis() - 300000L : this.lastRead < System.currentTimeMillis() - 5000L)) {
            return false;
        }
        return this.connected;
    }

    public void setLogin(boolean li) {
        if (!this.isLoggedIn && li && this.playerServerConnection) {
            this.writeBuffer_w = ByteBuffer.allocate(786408);
            this.writeBuffer_r = ByteBuffer.allocate(786408);
            this.writeBuffer_w.clear();
            this.writeBuffer_r.flip();
        }
        this.isLoggedIn = li;
    }

    public void disconnect() {
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASS_NAME, "disconnect");
        }
        this.connected = false;
        try {
            if (this.in != null) {
                this.in.close();
            }
            this.in = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (this.out != null) {
                this.out.close();
            }
            this.out = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (this.socket != null) {
                this.socket.close();
            }
            this.socket = null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.readBuffer.clear();
        this.writeBuffer_w.clear();
        this.writeBuffer_r.clear();
        this.isLoggedIn = false;
    }

    public void sendShutdown() {
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASS_NAME, "sendShutdown");
        }
        if (this.socketChannel != null) {
            try {
                this.socketChannel.socket().shutdownOutput();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.socketChannel != null) {
            try {
                this.socketChannel.socket().shutdownInput();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void closeChannel() {
        if (logger.isLoggable(Level.FINER)) {
            logger.entering(CLASS_NAME, "closeChannel");
        }
        if (this.socketChannel != null && this.socketChannel.socket() != null) {
            try {
                this.socketChannel.socket().close();
            }
            catch (IOException iox) {
                iox.printStackTrace();
            }
        }
        if (this.socketChannel != null) {
            try {
                this.socketChannel.close();
            }
            catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }

    public void tick() throws IOException {
        if (this.callTickWritingFromTick) {
            this.tickWriting(0L);
        }
        if (this.ticksToDisconnect >= 0 && --this.ticksToDisconnect <= 0) {
            throw new IOException("Disconnecting by timeout.");
        }
        int preRead = this.bytesRead;
        int totalRead = 0;
        long maxNanosPerIteration = 3000000000L;
        long startTime = System.nanoTime();
        int readBlocks = 0;
        while (readBlocks < this.maxBlocksPerIteration && System.nanoTime() - startTime < 3000000000L && (totalRead = this.socketChannel.read(this.readBuffer)) > 0) {
            if (this.playerServerConnection) {
                if (totalRead > maxTotalRead) {
                    maxTotalRead = totalRead;
                }
                if (totalRead > maxTotalReadAllowed) {
                    throw new IOException(this.getIp() + " disconnected in SocketConnection. Maxtotalread not allowed: " + totalRead);
                }
            }
            this.lastRead = System.currentTimeMillis();
            if (this.toRead < 0) {
                if (this.readBuffer.position() != 2) continue;
                this.bytesRead += this.readBuffer.position();
                this.readBuffer.flip();
                this.decrypt(this.readBuffer);
                this.toRead = this.readBuffer.getShort() & 0xFFFF;
                this.readBuffer.clear();
                this.readBuffer.limit(this.toRead);
                if (!this.playerServerConnection) continue;
                if ((long)this.toRead > maxRead) {
                    maxRead = this.toRead & 0xFFFF;
                }
                if (this.toRead <= maxReadAllowed) continue;
                throw new IOException(this.getIp() + " disconnected in SocketConnection. Maxread not allowed: " + this.toRead);
            }
            if (this.readBuffer.position() != this.toRead) continue;
            this.bytesRead += this.readBuffer.position();
            ++readBlocks;
            this.readBuffer.flip();
            this.decrypt(this.readBuffer);
            this.connectionListener.reallyHandle(0, this.readBuffer);
            this.readBuffer.clear();
            this.readBuffer.limit(2);
            if (this.playerServerConnection && this.toRead > maxReadAllowed) {
                throw new IOException(this.getIp() + " disconnected in SocketConnection. Maxread not allowed: " + this.toRead);
            }
            this.toRead = -1;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean tickWriting(long aNanosToWaitForLock) throws IOException {
        try {
            if (aNanosToWaitForLock <= 0L && RW_LOCK.writeLock().tryLock() || aNanosToWaitForLock > 0L && RW_LOCK.writeLock().tryLock(aNanosToWaitForLock, TimeUnit.NANOSECONDS)) {
                if (this.socketChannel == null || !this.socketChannel.isConnected()) {
                    if (!logger.isLoggable(Level.FINE)) return false;
                    logger.fine("Cannot write message to socketChannel: " + this.socketChannel);
                    return false;
                }
                try {
                    int lBytesWritten;
                    if (this.writing) {
                        throw new IllegalStateException("update called between a getBuffer() and a flush(). Don't do that.");
                    }
                    if (this.getUnflushed() > 0x100000) {
                        throw new IOException("Buffer overflow (1 mb unsent)");
                    }
                    int preWrite = this.writeBuffer_r.remaining();
                    this.socketChannel.write(this.writeBuffer_r);
                    if (this.writeBuffer_r.remaining() == 0) {
                        ByteBuffer tmp = this.writeBuffer_w;
                        this.writeBuffer_w = this.writeBuffer_r;
                        this.writeBuffer_r = tmp;
                        this.writeBuffer_w.clear();
                        this.writeBuffer_r.flip();
                    }
                    if (logger.isLoggable(Level.FINER) && (lBytesWritten = preWrite - this.writeBuffer_r.remaining()) > 0) {
                        logger.finer("Number of bytes wriiten to the socketChannel: " + lBytesWritten + ", channel: " + this.socketChannel);
                    }
                    boolean bl = true;
                    return bl;
                }
                catch (IOException e) {
                    if (!logger.isLoggable(Level.FINE)) throw e;
                    logger.log(Level.FINE, "IOException while writing to channel: " + this.socketChannel + ", only " + this.writeBuffer_w.remaining() + " bytes remained. Written=" + this.totalBytesWritten + ", BufferTmp: " + this.writeBufferTmp + ", Buffer_w: " + this.writeBuffer_w + ", Buffer_r: " + this.writeBuffer_r, e);
                    throw e;
                }
                finally {
                    RW_LOCK.writeLock().unlock();
                }
            }
            if (!logger.isLoggable(Level.FINEST)) return false;
            return false;
        }
        catch (InterruptedException e) {
            logger.log(Level.WARNING, "Lock was interrupted", e);
            return false;
        }
    }

    public void changeProtocol(long newSeed) {
    }

    private void encrypt(ByteBuffer bb, int start, int end) {
        byte[] bytes = bb.array();
        int i = start;
        while (i < end) {
            if (--this.remainingEncryptBytes < 0) {
                this.remainingEncryptBytes = this.encryptRandom.nextInt(100) + 1;
                this.encryptByte = (byte)this.encryptRandom.nextInt(254);
                this.encryptAddByte = (byte)this.encryptRandom.nextInt(254);
            }
            bytes[i] = (byte)(bytes[i] - this.encryptAddByte);
            int n = i++;
            bytes[n] = (byte)(bytes[n] ^ this.encryptByte);
        }
    }

    private void decrypt(ByteBuffer bb) {
        byte[] bytes = bb.array();
        int start = bb.position();
        int end = bb.limit();
        for (int i = start; i < end; ++i) {
            if (--this.remainingDencryptBytes < 0) {
                this.remainingDencryptBytes = this.decryptRandom.nextInt(100) + 1;
                this.dencryptByte = (byte)this.decryptRandom.nextInt(254);
                this.decryptAddByte = (byte)this.decryptRandom.nextInt(254);
            }
            int n = i;
            bytes[n] = (byte)(bytes[n] ^ this.dencryptByte);
            bytes[i] = (byte)(bytes[i] + this.decryptAddByte);
        }
    }

    public void setEncryptSeed(long seed) {
        this.encryptRandom.setSeed(seed);
        this.remainingEncryptBytes = 0;
    }

    public void setDecryptSeed(long seed) {
        this.decryptRandom.setSeed(seed);
        this.remainingDencryptBytes = 0;
    }

    public int getSentBytes() {
        return this.totalBytesWritten;
    }

    public int getReadBytes() {
        return this.bytesRead;
    }

    public void clearSentBytes() {
        this.totalBytesWritten = 0;
    }

    public void clearReadBytes() {
        this.bytesRead = 0;
    }

    public boolean isCallTickWritingFromTick() {
        return this.callTickWritingFromTick;
    }

    public void setCallTickWritingFromTick(boolean newCallTickWritingFromTick) {
        this.callTickWritingFromTick = newCallTickWritingFromTick;
    }

    public boolean isWriting() {
        return this.writing;
    }

    public String toString() {
        return "SocketConnection [IrcChannel: " + this.socketChannel + ']';
    }
}

