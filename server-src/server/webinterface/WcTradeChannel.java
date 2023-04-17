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

public class WcTradeChannel
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcTradeChannel.class.getName());
    private String sender = "unknown";
    private long senderId = -10L;
    private String message = "";
    private byte kingdom = 0;
    private int colorR = 0;
    private int colorG = 0;
    private int colorB = 0;

    public WcTradeChannel(long aId, long _senderId, String _sender, String _message, byte _kingdom, int r, int g, int b) {
        super(aId, (short)28);
        this.sender = _sender;
        this.senderId = _senderId;
        this.message = _message;
        this.kingdom = _kingdom;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    public WcTradeChannel(long _id, byte[] _data) {
        super(_id, (short)28, _data);
    }

    @Override
    public boolean autoForward() {
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] barr = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeUTF(this.sender);
            dos.writeLong(this.senderId);
            dos.writeUTF(this.message);
            dos.writeByte(this.kingdom);
            dos.writeInt(this.colorR);
            dos.writeInt(this.colorG);
            dos.writeInt(this.colorB);
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
                    dis = new DataInputStream(new ByteArrayInputStream(WcTradeChannel.this.getData()));
                    WcTradeChannel.this.sender = dis.readUTF();
                    WcTradeChannel.this.senderId = dis.readLong();
                    WcTradeChannel.this.message = dis.readUTF();
                    WcTradeChannel.this.kingdom = dis.readByte();
                    WcTradeChannel.this.colorR = dis.readInt();
                    WcTradeChannel.this.colorG = dis.readInt();
                    WcTradeChannel.this.colorB = dis.readInt();
                    Players.getInstance().sendGlobalTradeMessage(null, WcTradeChannel.this.senderId, WcTradeChannel.this.sender, WcTradeChannel.this.message, WcTradeChannel.this.kingdom, WcTradeChannel.this.colorR, WcTradeChannel.this.colorG, WcTradeChannel.this.colorB);
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
        }.start();
    }

    static /* synthetic */ String access$002(WcTradeChannel x0, String x1) {
        x0.sender = x1;
        return x0.sender;
    }

    static /* synthetic */ long access$102(WcTradeChannel x0, long x1) {
        x0.senderId = x1;
        return x0.senderId;
    }

    static /* synthetic */ String access$202(WcTradeChannel x0, String x1) {
        x0.message = x1;
        return x0.message;
    }

    static /* synthetic */ byte access$302(WcTradeChannel x0, byte x1) {
        x0.kingdom = x1;
        return x0.kingdom;
    }

    static /* synthetic */ int access$402(WcTradeChannel x0, int x1) {
        x0.colorR = x1;
        return x0.colorR;
    }

    static /* synthetic */ int access$502(WcTradeChannel x0, int x1) {
        x0.colorG = x1;
        return x0.colorG;
    }

    static /* synthetic */ int access$602(WcTradeChannel x0, int x1) {
        x0.colorB = x1;
        return x0.colorB;
    }

    static /* synthetic */ long access$100(WcTradeChannel x0) {
        return x0.senderId;
    }

    static /* synthetic */ String access$000(WcTradeChannel x0) {
        return x0.sender;
    }

    static /* synthetic */ String access$200(WcTradeChannel x0) {
        return x0.message;
    }

    static /* synthetic */ byte access$300(WcTradeChannel x0) {
        return x0.kingdom;
    }

    static /* synthetic */ int access$400(WcTradeChannel x0) {
        return x0.colorR;
    }

    static /* synthetic */ int access$500(WcTradeChannel x0) {
        return x0.colorG;
    }

    static /* synthetic */ int access$600(WcTradeChannel x0) {
        return x0.colorB;
    }

    static /* synthetic */ Logger access$700() {
        return logger;
    }
}

