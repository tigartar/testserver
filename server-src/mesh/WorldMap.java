/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

import com.wurmonline.mesh.Chunk;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public final class WorldMap {
    private static final byte[] HEADER_PREFIX = "WURMMAP".getBytes();
    private static final byte FILE_FORMAT_VERSION = 0;
    private static final int HEADER_SIZE = 12;
    private final int width;
    private Chunk[][] chunks;
    private final FileChannel channel;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(16384);

    private WorldMap(FileChannel channel, int width, int height) {
        this.width = width;
        this.chunks = new Chunk[width][height];
        this.channel = channel;
    }

    protected void freeChunk(int x, int y) throws IOException {
        if (this.chunks[x][y] != null) {
            if (this.chunks[x][y].isDirty()) {
                long pos = (long)(x + y * this.width) * 16384L + 12L;
                this.channel.position(pos);
                this.byteBuffer.clear();
                IntBuffer ib = this.byteBuffer.asIntBuffer();
                this.chunks[x][y].encode(ib);
                this.byteBuffer.position(ib.position() * 4);
                this.byteBuffer.flip();
                this.channel.write(this.byteBuffer);
            }
            this.chunks[x][y] = null;
        }
    }

    protected void loadChunk(int x, int y) throws IOException {
        if (this.chunks[x][y] == null) {
            long pos = (long)(x + y * this.width) * 16384L + 12L;
            this.channel.position(pos);
            this.byteBuffer.clear();
            this.channel.read(this.byteBuffer);
            this.byteBuffer.flip();
            IntBuffer ib = this.byteBuffer.asIntBuffer();
            this.chunks[x][y] = Chunk.decode(ib);
            this.byteBuffer.position(ib.position() * 4);
        }
    }

    public static WorldMap getWorldMap(File file) throws IOException {
        if (file.exists()) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
            FileChannel channel = randomAccessFile.getChannel();
            if (file.length() < 12L) {
                throw new IOException("Map file too small to even contain a header.");
            }
            channel.position(0L);
            ByteBuffer header = ByteBuffer.allocate(12);
            channel.read(header);
            header.flip();
            byte[] prefix = new byte[7];
            header.get(prefix);
            byte version = header.get();
            ShortBuffer headerShort = header.asShortBuffer();
            short width = headerShort.get();
            short height = headerShort.get();
            if (!Arrays.equals(prefix, HEADER_PREFIX)) {
                throw new IOException("Bad map file header: " + new String(prefix) + ".");
            }
            if (version != 0) {
                throw new IOException("Bad map file format version number.");
            }
            if (file.length() != (long)(width * height * 16384 + 12)) {
                throw new IOException("Found the map file, but it was the wrong size. (found " + file.length() + ", expected " + (width * height * 16384 + 12) + ")");
            }
            return new WorldMap(channel, width, height);
        }
        throw new IOException("Failed to locate mapfile");
    }
}

