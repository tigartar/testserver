/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.webinterface.WcRemoveFriendship;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcExpelMember
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
    private long playerId;
    private byte fromKingdomId;
    private byte toKingdomId;
    private int originServer;

    public WcExpelMember(long aPlayerId, byte aFromKingdomId, byte aToKingdomId, int aOriginServer) {
        super(WurmId.getNextWCCommandId(), (short)30);
        this.playerId = aPlayerId;
        this.fromKingdomId = aFromKingdomId;
        this.toKingdomId = aToKingdomId;
        this.originServer = aOriginServer;
    }

    public WcExpelMember(long aId, byte[] aData) {
        super(aId, (short)30, aData);
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
            dos.writeLong(this.playerId);
            dos.writeByte(this.fromKingdomId);
            dos.writeByte(this.toKingdomId);
            dos.writeInt(this.originServer);
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
        new Thread(){

            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new ByteArrayInputStream(WcExpelMember.this.getData()));
                    WcExpelMember.this.playerId = dis.readLong();
                    WcExpelMember.this.fromKingdomId = dis.readByte();
                    WcExpelMember.this.toKingdomId = dis.readByte();
                    WcExpelMember.this.originServer = dis.readInt();
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
                    return;
                }
                StreamUtilities.closeInputStreamIgnoreExceptions(dis);
                if (Servers.isThisLoginServer()) {
                    WcExpelMember.this.sendFromLoginServer();
                }
                PlayerInfoFactory.expelMember(WcExpelMember.this.playerId, WcExpelMember.this.fromKingdomId, WcExpelMember.this.toKingdomId, WcExpelMember.this.originServer);
            }
        }.start();
    }

    static /* synthetic */ long access$002(WcExpelMember x0, long x1) {
        x0.playerId = x1;
        return x0.playerId;
    }

    static /* synthetic */ byte access$102(WcExpelMember x0, byte x1) {
        x0.fromKingdomId = x1;
        return x0.fromKingdomId;
    }

    static /* synthetic */ byte access$202(WcExpelMember x0, byte x1) {
        x0.toKingdomId = x1;
        return x0.toKingdomId;
    }

    static /* synthetic */ int access$302(WcExpelMember x0, int x1) {
        x0.originServer = x1;
        return x0.originServer;
    }

    static /* synthetic */ Logger access$400() {
        return logger;
    }

    static /* synthetic */ long access$000(WcExpelMember x0) {
        return x0.playerId;
    }

    static /* synthetic */ byte access$100(WcExpelMember x0) {
        return x0.fromKingdomId;
    }

    static /* synthetic */ byte access$200(WcExpelMember x0) {
        return x0.toKingdomId;
    }

    static /* synthetic */ int access$300(WcExpelMember x0) {
        return x0.originServer;
    }
}

