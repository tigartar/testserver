/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcSpawnPoints
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcSpawnPoints.class.getName());
    private Spawnpoint[] spawns;

    public WcSpawnPoints(long _id) {
        super(_id, (short)21);
    }

    public final void setSpawns(Spawnpoint[] spawnpoints) {
        this.spawns = spawnpoints;
    }

    public WcSpawnPoints(long _id, byte[] _data) {
        super(_id, (short)21, _data);
    }

    @Override
    public boolean autoForward() {
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] barr = null;
        try {
            dos = new DataOutputStream(bos);
            if (this.spawns == null) {
                dos.writeInt(0);
            } else {
                dos.writeInt(this.spawns.length);
                for (Spawnpoint spawn : this.spawns) {
                    dos.writeShort(spawn.tilex);
                    dos.writeShort(spawn.tiley);
                    dos.writeUTF(spawn.name);
                    dos.writeUTF(spawn.description);
                    dos.writeByte(spawn.kingdom);
                }
            }
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                barr = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(barr);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
            barr = bos.toByteArray();
            StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
            this.setData(barr);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
        barr = bos.toByteArray();
        StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
        this.setData(barr);
        return barr;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute() {
        DataInputStream dis;
        block5: {
            dis = null;
            try {
                ServerEntry entry;
                dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                int nums = dis.readInt();
                if (nums <= 0) break block5;
                HashSet<Spawnpoint> lspawns = new HashSet<Spawnpoint>();
                for (int x = 0; x < nums; ++x) {
                    short tilex = dis.readShort();
                    short tiley = dis.readShort();
                    String name = dis.readUTF();
                    String desc = dis.readUTF();
                    byte kingdom = dis.readByte();
                    lspawns.add(new Spawnpoint(name, (byte)x, desc, tilex, tiley, true, kingdom));
                }
                if (lspawns.size() <= 0 || (entry = Servers.getServerWithId(WurmId.getOrigin(this.getWurmId()))) == null) break block5;
                entry.setSpawns(lspawns.toArray(new Spawnpoint[lspawns.size()]));
            }
            catch (IOException ex) {
                try {
                    logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
                }
                catch (Throwable throwable) {
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                    throw throwable;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            }
        }
        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
}

