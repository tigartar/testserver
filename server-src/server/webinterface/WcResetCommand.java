/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcResetCommand
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcResetCommand.class.getName());
    private long pid = -10L;

    public WcResetCommand(long _id, long playerid) {
        super(_id, (short)6);
        this.pid = playerid;
    }

    public WcResetCommand(long _id, byte[] _data) {
        super(_id, (short)6, _data);
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
            dos.writeLong(this.pid);
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
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
            this.pid = dis.readLong();
            Players.getInstance().resetPlayer(this.pid);
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
        StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
}

