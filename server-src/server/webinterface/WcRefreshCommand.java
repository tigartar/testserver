/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcRefreshCommand
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcRefreshCommand.class.getName());
    private String nameToReload;

    public WcRefreshCommand(long aId, String _nameToReload) {
        super(aId, (short)5);
        this.nameToReload = _nameToReload;
    }

    public WcRefreshCommand(long aId, byte[] _data) {
        super(aId, (short)5, _data);
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
            dos.writeUTF(this.nameToReload);
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
            this.nameToReload = dis.readUTF();
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(this.nameToReload);
            pinf.loaded = false;
            pinf.load();
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

