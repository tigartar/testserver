/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.TabData;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcTabLists
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcTabLists.class.getName());
    public static final byte TAB_GM = 0;
    public static final byte TAB_MGMT = 1;
    public static final byte REMOVE = 2;
    private byte tab;
    private TabData tabData;

    WcTabLists(long aId, byte[] _data) {
        super(aId, (short)31, _data);
    }

    public WcTabLists(byte tab, TabData tabData) {
        super(WurmId.getNextWCCommandId(), (short)31);
        this.tab = tab;
        this.tabData = tabData;
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
            dos.writeByte(this.tab);
            this.tabData.pack(dos);
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, "Pack exception " + ex.getMessage(), ex);
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
            this.tab = dis.readByte();
            this.tabData = new TabData(dis);
            Players.getInstance().updateTabs(this.tab, this.tabData);
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

