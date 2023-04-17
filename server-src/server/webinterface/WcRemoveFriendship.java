/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
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

public final class WcRemoveFriendship
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcRemoveFriendship.class.getName());
    private String playerName;
    private long playerWurmId;
    private String friendName;
    private long friendWurmId;

    public WcRemoveFriendship(String aPlayerName, long aPlayerWurmId, String aFriendName, long aFriendWurmId) {
        super(WurmId.getNextWCCommandId(), (short)4);
        this.playerName = aPlayerName;
        this.playerWurmId = aPlayerWurmId;
        this.friendName = aFriendName;
        this.friendWurmId = aFriendWurmId;
    }

    public WcRemoveFriendship(long aId, byte[] aData) {
        super(aId, (short)4, aData);
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
            dos.writeUTF(this.playerName);
            dos.writeLong(this.playerWurmId);
            dos.writeUTF(this.friendName);
            dos.writeLong(this.friendWurmId);
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
                    dis = new DataInputStream(new ByteArrayInputStream(WcRemoveFriendship.this.getData()));
                    WcRemoveFriendship.this.playerName = dis.readUTF();
                    WcRemoveFriendship.this.playerWurmId = dis.readLong();
                    WcRemoveFriendship.this.friendName = dis.readUTF();
                    WcRemoveFriendship.this.friendWurmId = dis.readLong();
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
                    PlayerInfo fInfo;
                    if (WcRemoveFriendship.this.friendWurmId == -10L && (fInfo = PlayerInfoFactory.getPlayerInfoWithName(WcRemoveFriendship.this.friendName)) != null) {
                        WcRemoveFriendship.this.friendWurmId = fInfo.wurmId;
                    }
                    WcRemoveFriendship.this.sendFromLoginServer();
                }
                PlayerInfoFactory.breakFriendship(WcRemoveFriendship.this.playerName, WcRemoveFriendship.this.playerWurmId, WcRemoveFriendship.this.friendName, WcRemoveFriendship.this.friendWurmId);
            }
        }.start();
    }

    static /* synthetic */ String access$002(WcRemoveFriendship x0, String x1) {
        x0.playerName = x1;
        return x0.playerName;
    }

    static /* synthetic */ long access$102(WcRemoveFriendship x0, long x1) {
        x0.playerWurmId = x1;
        return x0.playerWurmId;
    }

    static /* synthetic */ String access$202(WcRemoveFriendship x0, String x1) {
        x0.friendName = x1;
        return x0.friendName;
    }

    static /* synthetic */ long access$302(WcRemoveFriendship x0, long x1) {
        x0.friendWurmId = x1;
        return x0.friendWurmId;
    }

    static /* synthetic */ Logger access$400() {
        return logger;
    }

    static /* synthetic */ long access$300(WcRemoveFriendship x0) {
        return x0.friendWurmId;
    }

    static /* synthetic */ String access$200(WcRemoveFriendship x0) {
        return x0.friendName;
    }

    static /* synthetic */ String access$000(WcRemoveFriendship x0) {
        return x0.playerName;
    }

    static /* synthetic */ long access$100(WcRemoveFriendship x0) {
        return x0.playerWurmId;
    }
}

