/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.mesh;

import com.wurmonline.math.TilePos;
import com.wurmonline.mesh.Tiles;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MeshIO {
    private static final Logger logger = Logger.getLogger(MeshIO.class.getName());
    private static final long MAGIC_NUMBER = 5136955264682433437L;
    private static final int ROW_COUNT = 128;
    private int size_level;
    private int size;
    private short maxHeight = 0;
    private short[] maxHeightCoord = new short[]{-1, -1};
    public final int[] data;
    private FileChannel fileChannel;
    private ByteBuffer tmpBuf;
    private final IntBuffer tmpBufInt;
    private boolean[] rowDirty = new boolean[128];
    private int rowId = 0;
    private final int linesPerRow;
    private byte[] distantTerrainTypes;
    private static boolean allocateDirectBuffers;

    private MeshIO(int size_level, int[] data) {
        if (size_level > 32) {
            throw new IllegalArgumentException("I'm fairly sure you didn't mean to REALLY create a map 2^" + size_level + " units wide.");
        }
        if (size_level < 4) {
            throw new IllegalArgumentException("Maps can't be smaller than 2^4.");
        }
        this.size_level = size_level;
        this.size = 1 << size_level;
        this.data = data;
        int holes = 0;
        for (int x = 0; x < data.length; ++x) {
            if (Tiles.decodeType(data[x]) != Tiles.Tile.TILE_HOLE.getId()) continue;
            ++holes;
        }
        logger.info("Holes=" + holes);
        if (allocateDirectBuffers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a direct byte buffer for writing the Mesh.");
            }
            this.tmpBuf = ByteBuffer.allocateDirect(this.size * 4);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a heap byte buffer for writing the Mesh.");
            }
            this.tmpBuf = ByteBuffer.allocate(this.size * 4);
        }
        this.tmpBufInt = this.tmpBuf.asIntBuffer();
        this.linesPerRow = this.size / 128;
        logger.info("size_level: " + size_level);
        logger.info("size: " + this.size);
        logger.info("data length: " + data.length);
        logger.info("linesPerRow: " + this.linesPerRow);
    }

    private MeshIO(ByteBuffer header) throws IOException {
        this.readHeader(header);
        this.data = new int[this.size * this.size + 1];
        if (allocateDirectBuffers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a direct byte buffer for writing the Mesh.");
            }
            this.tmpBuf = ByteBuffer.allocateDirect(this.size * 4);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a heap byte buffer for writing the Mesh.");
            }
            this.tmpBuf = ByteBuffer.allocate(this.size * 4);
        }
        this.tmpBufInt = this.tmpBuf.asIntBuffer();
        this.linesPerRow = this.size / 128;
    }

    private void readHeader(ByteBuffer header) throws IOException {
        long magicNumber = header.getLong();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Magic Number: " + magicNumber);
        }
        if (magicNumber != 5136955264682433437L) {
            throw new IOException("Bad magic number! This is not a valid map file.");
        }
        byte headerVersionNumber = header.get();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Version Number: " + headerVersionNumber);
        }
        if (headerVersionNumber == 0) {
            this.size_level = header.get();
            this.size = 1 << this.size_level;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("size level: " + this.size_level);
            logger.fine("size: " + this.size);
        }
    }

    private void writeHeader(ByteBuffer header) throws IOException {
        header.putLong(5136955264682433437L);
        header.put((byte)0);
        header.put((byte)this.size_level);
    }

    public static MeshIO createMap(String filename, int level, int[] data) throws IOException {
        ByteBuffer stripBuffer;
        MeshIO meshIO = new MeshIO(level, data);
        FileChannel channel = new RandomAccessFile(filename, "rw").getChannel();
        logger.info(filename + " size is " + channel.size());
        logger.info("Data array length is " + data.length);
        MappedByteBuffer header = channel.map(FileChannel.MapMode.READ_WRITE, 0L, 1024L);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Header capacity: " + header.capacity() + ", header.limit: " + header.limit() + ", header.position: " + header.position());
        }
        meshIO.writeHeader(header);
        logger.info("meshIO size is " + meshIO.size);
        if (allocateDirectBuffers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a direct byte buffer for creating the map: " + filename);
            }
            stripBuffer = ByteBuffer.allocateDirect(meshIO.size * 4);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a heap byte buffer for creating the map: " + filename);
            }
            stripBuffer = ByteBuffer.allocate(meshIO.size * 4);
        }
        for (int i = 0; i < meshIO.size; ++i) {
            stripBuffer.clear();
            stripBuffer.asIntBuffer().put(meshIO.data, meshIO.size * i, meshIO.size);
            stripBuffer.flip();
            stripBuffer.limit(meshIO.size * 4);
            stripBuffer.position(0);
            channel.write(stripBuffer, 1024 + meshIO.size * 4 * i);
        }
        meshIO.fileChannel = channel;
        return meshIO;
    }

    public static MeshIO open(String filename) throws IOException {
        ByteBuffer stripBuffer;
        long lStart = System.nanoTime();
        FileChannel channel = new RandomAccessFile(filename, "rw").getChannel();
        logger.info(filename + " size is " + channel.size());
        MappedByteBuffer header = channel.map(FileChannel.MapMode.READ_ONLY, 0L, 1024L);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Header capacity: " + header.capacity() + ", header.limit: " + header.limit() + ", header.position: " + header.position());
        }
        MeshIO meshIO = new MeshIO(header);
        if (allocateDirectBuffers) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a direct byte buffer for reading the mesh: " + filename);
            }
            stripBuffer = ByteBuffer.allocateDirect(meshIO.size * 4);
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Will allocate a heap byte buffer for reading the mesh: " + filename);
            }
            stripBuffer = ByteBuffer.allocate(meshIO.size * 4);
        }
        for (int i = 0; i < meshIO.size; ++i) {
            stripBuffer.clear();
            stripBuffer.limit(meshIO.size * 4);
            stripBuffer.position(0);
            channel.read(stripBuffer, 1024 + meshIO.size * 4 * i);
            stripBuffer.flip();
            stripBuffer.asIntBuffer().get(meshIO.data, meshIO.size * i, meshIO.size);
            stripBuffer.rewind();
            if (!filename.contains("top_layer")) continue;
            IntBuffer tmp = stripBuffer.asIntBuffer();
            for (int x = 0; x < meshIO.size; ++x) {
                int tile = tmp.get(x);
                short height = Tiles.decodeHeight(tile);
                if (height <= meshIO.getMaxHeight()) continue;
                meshIO.setMaxHeight(height);
                meshIO.setMaxHeightCoord((short)x, (short)i);
            }
        }
        meshIO.fileChannel = channel;
        if (logger.isLoggable(Level.FINE)) {
            long lElapsedTime = System.nanoTime() - lStart;
            logger.fine("Loaded Mesh '" + filename + "', that took " + (float)lElapsedTime / 1000000.0f + ", millis.");
        }
        return meshIO;
    }

    public void close() throws IOException {
        this.saveAllDirtyRows();
        this.fileChannel.close();
    }

    public int getSize() {
        return this.size;
    }

    public int getSizeLevel() {
        return this.size_level;
    }

    public final int getTile(TilePos tilePos) {
        return this.getTile(tilePos.x, tilePos.y);
    }

    public final int getTile(int x, int y) {
        int tile = 0;
        try {
            tile = this.data[x | y << this.size_level];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            int idx;
            e.printStackTrace();
            int xx = x;
            int yy = y;
            if (xx < 0) {
                xx = 0;
            }
            if (yy < 0) {
                yy = 0;
            }
            if ((idx = xx | yy << this.size_level) < 0 || idx > this.data.length) {
                idx = this.data.length - 1;
            }
            logger.log(Level.WARNING, "data: " + this.data.length + ", x: " + x + ", y: " + y + ", size_level: " + this.size_level + ", x | (y << size_level): " + (x | y << this.size_level), e);
            logger.log(Level.WARNING, "Attempting to find closest tile using: x: " + xx + ", y: " + yy + " for an index of: " + idx);
            return this.data[idx];
        }
        return tile;
    }

    public final void setTile(int x, int y, int value) {
        this.data[x | y << this.size_level] = value;
        this.rowDirty[y / this.linesPerRow] = true;
    }

    @Deprecated
    public final void saveTile(int x, int y) throws IOException {
        this.tmpBuf.clear();
        this.tmpBuf.putInt(this.data[x | y << this.size_level]);
        this.tmpBuf.flip();
        this.fileChannel.write(this.tmpBuf, 1024 + ((x | y << this.size_level) << 2));
    }

    public final void saveFullRows(int y, int rows) throws IOException {
        this.fileChannel.position(1024 + (y << this.size_level << 2));
        for (int yy = 0; yy < rows; ++yy) {
            this.tmpBuf.clear();
            this.tmpBufInt.clear();
            this.tmpBufInt.put(this.data, y + yy << this.size_level, this.size);
            this.tmpBufInt.flip();
            this.tmpBuf.limit(this.size << 2);
            this.tmpBuf.position(0);
            this.fileChannel.write(this.tmpBuf);
        }
    }

    public final void saveAll() throws IOException {
        long lStart = System.nanoTime();
        this.saveFullRows(0, this.size);
        if (logger.isLoggable(Level.FINE)) {
            long lElapsedTime = System.nanoTime() - lStart;
            logger.fine("Saved all " + this.size + " rows that took " + (float)lElapsedTime / 1000000.0f + ", millis.");
        }
    }

    public final int saveAllDirtyRows() throws IOException {
        long lStart = System.nanoTime();
        int savedRowCount = 0;
        for (int i = 0; i < 128; ++i) {
            if (!this.saveNextDirtyRow()) continue;
            ++savedRowCount;
        }
        if (logger.isLoggable(Level.FINER)) {
            long lElapsedTime = System.nanoTime() - lStart;
            if (savedRowCount > 0) {
                logger.finer("Saved all " + savedRowCount + " dirty rows that took " + (float)lElapsedTime / 1000000.0f + ", millis.");
            }
        }
        return savedRowCount;
    }

    public final boolean saveNextDirtyRow() throws IOException {
        boolean lRowWasDirty = false;
        if (this.rowDirty[this.rowId]) {
            lRowWasDirty = true;
            long lStart = System.nanoTime();
            this.saveFullRows(this.rowId * this.linesPerRow, this.linesPerRow);
            this.rowDirty[this.rowId] = false;
            if (logger.isLoggable(Level.FINEST)) {
                long lElapsedTime = System.nanoTime() - lStart;
                logger.finest("Saved dirty row " + this.rowId + ", that took " + (float)lElapsedTime / 1000000.0f + ", millis.");
            }
        }
        ++this.rowId;
        if (this.rowId >= 128) {
            this.rowId = 0;
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Resetting dirty row id as it has reached 128");
            }
        }
        return lRowWasDirty;
    }

    int[] cloneData() {
        int[] data2 = new int[this.data.length];
        System.arraycopy(this.data, 0, data2, 0, this.data.length);
        return data2;
    }

    public void calcDistantTerrain() {
        this.distantTerrainTypes = new byte[this.size * this.size / 256];
        for (int xT = 0; xT < this.size / 16; ++xT) {
            for (int yT = 0; yT < this.size / 16; ++yT) {
                int[] counts = new int[256];
                for (int x = xT * 16; x < xT * 16 + 16; ++x) {
                    for (int y = yT * 16; y < yT * 16 + 16; ++y) {
                        int type;
                        int n = type = Tiles.decodeType(this.getTile(x, y)) & 0xFF;
                        counts[n] = counts[n] + 1;
                    }
                }
                int mostCommon = 0;
                for (int i = 0; i < 256; ++i) {
                    if (counts[i] <= counts[mostCommon]) continue;
                    mostCommon = i;
                }
                this.distantTerrainTypes[xT + yT * (this.size / 16)] = (byte)mostCommon;
            }
        }
    }

    public byte[] getDistantTerrainTypes() {
        return this.distantTerrainTypes;
    }

    public int[] getData() {
        return this.data;
    }

    public void setAllRowsDirty() {
        for (int yy = 0; yy < 128; ++yy) {
            this.rowDirty[yy] = true;
        }
    }

    public static boolean isAllocateDirectBuffers() {
        return allocateDirectBuffers;
    }

    public static void setAllocateDirectBuffers(boolean newAllocateDirectBuffers) {
        allocateDirectBuffers = newAllocateDirectBuffers;
    }

    public String toString() {
        return "MeshIO [Size: " + this.size + ", linesPerRow: " + this.linesPerRow + ", rowId: " + this.rowId + ", size_level: " + this.size_level + "]@" + this.hashCode();
    }

    public short getMaxHeight() {
        return this.maxHeight;
    }

    public void setMaxHeight(short maxHeight) {
        this.maxHeight = maxHeight;
    }

    public short[] getMaxHeightCoord() {
        return this.maxHeightCoord;
    }

    public void setMaxHeightCoord(short x, short y) {
        this.maxHeightCoord[0] = x;
        this.maxHeightCoord[1] = y;
    }
}

