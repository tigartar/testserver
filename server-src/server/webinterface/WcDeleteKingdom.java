/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Servers;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcDeleteKingdom
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcDeleteKingdom.class.getName());
    private byte kingdomId;

    public WcDeleteKingdom(long aId, byte kingdomToDelete) {
        super(aId, (short)8);
        this.kingdomId = kingdomToDelete;
    }

    public WcDeleteKingdom(long aId, byte[] aData) {
        super(aId, (short)8, aData);
    }

    @Override
    public boolean autoForward() {
        return true;
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
            dos.writeByte(this.kingdomId);
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

    @Override
    public void execute() {
        DataInputStream dis;
        block4: {
            dis = null;
            try {
                dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                this.kingdomId = dis.readByte();
                Servers.removeKingdomInfo(this.kingdomId);
                Kingdom k = Kingdoms.getKingdomOrNull(this.kingdomId);
                if (k == null || !k.isCustomKingdom()) break block4;
                k.delete();
                Kingdoms.removeKingdom(this.kingdomId);
                HistoryManager.addHistory(k.getName(), "has faded and is no more.");
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

