/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
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

public class WcKingdomInfo
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcKingdomInfo.class.getName());
    private boolean sendSingleKingdom = false;
    private byte singleKingdomId;

    public WcKingdomInfo(long aId, boolean singleKingdom, byte kingdomId) {
        super(aId, (short)7);
        this.sendSingleKingdom = singleKingdom;
        this.singleKingdomId = kingdomId;
    }

    public WcKingdomInfo(long aId, byte[] aData) {
        super(aId, (short)7, aData);
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
            dos.writeBoolean(this.sendSingleKingdom);
            if (this.sendSingleKingdom) {
                dos.writeInt(1);
                Kingdom k = Kingdoms.getKingdom(this.singleKingdomId);
                dos.writeByte(k.getId());
                dos.writeUTF(k.getName());
                dos.writeUTF(k.getPassword());
                dos.writeUTF(k.getChatName());
                dos.writeUTF(k.getSuffix());
                dos.writeUTF(k.getFirstMotto());
                dos.writeUTF(k.getSecondMotto());
                dos.writeByte(k.getTemplate());
                dos.writeBoolean(k.acceptsTransfers());
            } else {
                Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
                dos.writeInt(kingdoms.length);
                for (Kingdom k : kingdoms) {
                    dos.writeByte(k.getId());
                    dos.writeUTF(k.getName());
                    dos.writeUTF(k.getPassword());
                    dos.writeUTF(k.getChatName());
                    dos.writeUTF(k.getSuffix());
                    dos.writeUTF(k.getFirstMotto());
                    dos.writeUTF(k.getSecondMotto());
                    dos.writeByte(k.getTemplate());
                    dos.writeBoolean(k.acceptsTransfers());
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

    @Override
    public void execute() {
        new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                DataInputStream dis;
                block6: {
                    dis = null;
                    try {
                        dis = new DataInputStream(new ByteArrayInputStream(WcKingdomInfo.this.getData()));
                        WcKingdomInfo.this.sendSingleKingdom = dis.readBoolean();
                        int numKingdoms = dis.readInt();
                        if (!WcKingdomInfo.this.sendSingleKingdom) {
                            Kingdoms.markAllKingdomsForDeletion();
                        }
                        for (int x = 0; x < numKingdoms; ++x) {
                            boolean acceptsTransfers;
                            byte id = dis.readByte();
                            String name = dis.readUTF();
                            String password = dis.readUTF();
                            String chatName = dis.readUTF();
                            String suffix = dis.readUTF();
                            String firstMotto = dis.readUTF();
                            String secondMotto = dis.readUTF();
                            byte templateKingdom = dis.readByte();
                            Kingdom kingdom = new Kingdom(id, templateKingdom, name, password, chatName, suffix, firstMotto, secondMotto, acceptsTransfers = dis.readBoolean());
                            if (!Kingdoms.addKingdom(kingdom)) continue;
                            logger.log(Level.INFO, "Received " + name + " in WcKingdomInfo.");
                        }
                        if (WcKingdomInfo.this.sendSingleKingdom) break block6;
                        Kingdoms.trimKingdoms();
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
        }.start();
    }

    static /* synthetic */ boolean access$002(WcKingdomInfo x0, boolean x1) {
        x0.sendSingleKingdom = x1;
        return x0.sendSingleKingdom;
    }

    static /* synthetic */ boolean access$000(WcKingdomInfo x0) {
        return x0.sendSingleKingdom;
    }

    static /* synthetic */ Logger access$100() {
        return logger;
    }
}

