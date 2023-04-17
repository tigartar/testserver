/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcKillCommand
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcKillCommand.class.getName());
    private long wurmID;

    public WcKillCommand(long _id, long _wurmID) {
        super(_id, (short)36);
        this.wurmID = _wurmID;
    }

    WcKillCommand(long _id, byte[] _data) {
        super(_id, (short)36, _data);
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
        byte[] barr;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeLong(this.wurmID);
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                byte[] barr2 = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(barr2);
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
        new Thread(() -> {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                this.wurmID = dis.readLong();
            }
            catch (IOException ex) {
                try {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                catch (Throwable throwable) {
                    StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                    throw throwable;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                return;
            }
            StreamUtilities.closeInputStreamIgnoreExceptions(dis);
            try {
                Creature animal = Creatures.getInstance().getCreature(this.wurmID);
                animal.die(true, "Died on another server.", true);
            }
            catch (NoSuchCreatureException animal) {
                // empty catch block
            }
            if (Servers.isThisLoginServer()) {
                WcKillCommand wkc = new WcKillCommand(this.getWurmId(), this.wurmID);
                wkc.sendFromLoginServer();
            }
        }).start();
    }
}

