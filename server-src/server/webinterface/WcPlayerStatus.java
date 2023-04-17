/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcPlayerStatus
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcPlayerStatus.class.getName());
    public static final byte DO_NOTHING = 0;
    public static final byte WHOS_ONLINE = 1;
    public static final byte STATUS_CHANGE = 2;
    private byte type = 0;
    private String playerName;
    private long playerWurmId;
    private long lastLogin;
    private long lastLogout;
    private int currentServerId;
    private PlayerOnlineStatus status;

    public WcPlayerStatus() {
        super(WurmId.getNextWCCommandId(), (short)19);
        this.type = 1;
    }

    public WcPlayerStatus(PlayerState pState) {
        super(WurmId.getNextWCCommandId(), (short)19);
        this.type = (byte)2;
        this.playerName = pState.getPlayerName();
        this.playerWurmId = pState.getPlayerId();
        this.lastLogin = pState.getLastLogin();
        this.lastLogout = pState.getLastLogout();
        this.currentServerId = pState.getServerId();
        this.status = pState.getState();
    }

    public WcPlayerStatus(String aPlayerName, long aPlayerWurmId, long aLastLogin, long aLastLogout, int aCurrentServerId, PlayerOnlineStatus aStatus) {
        super(WurmId.getNextWCCommandId(), (short)19);
        this.type = (byte)2;
        this.playerName = aPlayerName;
        this.playerWurmId = aPlayerWurmId;
        this.lastLogin = aLastLogin;
        this.lastLogout = aLastLogout;
        this.currentServerId = aCurrentServerId;
        this.status = aStatus;
    }

    public WcPlayerStatus(long aId, byte[] aData) {
        super(aId, (short)19, aData);
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
            dos.writeByte(this.type);
            switch (this.type) {
                case 1: {
                    break;
                }
                case 2: {
                    dos.writeUTF(this.playerName);
                    dos.writeLong(this.playerWurmId);
                    dos.writeLong(this.lastLogin);
                    dos.writeLong(this.lastLogout);
                    dos.writeInt(this.currentServerId);
                    dos.writeByte(this.status.getId());
                    break;
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

            @Override
            public void run() {
                DataInputStream dis = null;
                try {
                    dis = new DataInputStream(new ByteArrayInputStream(WcPlayerStatus.this.getData()));
                    WcPlayerStatus.this.type = dis.readByte();
                    switch (WcPlayerStatus.this.type) {
                        case 1: {
                            break;
                        }
                        case 2: {
                            WcPlayerStatus.this.playerName = dis.readUTF();
                            WcPlayerStatus.this.playerWurmId = dis.readLong();
                            WcPlayerStatus.this.lastLogin = dis.readLong();
                            WcPlayerStatus.this.lastLogout = dis.readLong();
                            WcPlayerStatus.this.currentServerId = dis.readInt();
                            WcPlayerStatus.this.status = PlayerOnlineStatus.playerOnlineStatusFromId(dis.readByte());
                            break;
                        }
                    }
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
                if (WcPlayerStatus.this.type == 1) {
                    if (!Servers.isThisLoginServer()) {
                        PlayerInfoFactory.whosOnline();
                    }
                } else if (WcPlayerStatus.this.type == 2) {
                    PlayerState pState = new PlayerState(WcPlayerStatus.this.currentServerId, WcPlayerStatus.this.playerWurmId, WcPlayerStatus.this.playerName, WcPlayerStatus.this.lastLogin, WcPlayerStatus.this.lastLogout, WcPlayerStatus.this.status);
                    PlayerInfoFactory.updatePlayerState(pState);
                    if (Servers.isThisLoginServer()) {
                        WcPlayerStatus.this.sendFromLoginServer();
                    }
                }
            }
        }.start();
    }

    static /* synthetic */ byte access$002(WcPlayerStatus x0, byte x1) {
        x0.type = x1;
        return x0.type;
    }

    static /* synthetic */ byte access$000(WcPlayerStatus x0) {
        return x0.type;
    }

    static /* synthetic */ String access$102(WcPlayerStatus x0, String x1) {
        x0.playerName = x1;
        return x0.playerName;
    }

    static /* synthetic */ long access$202(WcPlayerStatus x0, long x1) {
        x0.playerWurmId = x1;
        return x0.playerWurmId;
    }

    static /* synthetic */ long access$302(WcPlayerStatus x0, long x1) {
        x0.lastLogin = x1;
        return x0.lastLogin;
    }

    static /* synthetic */ long access$402(WcPlayerStatus x0, long x1) {
        x0.lastLogout = x1;
        return x0.lastLogout;
    }

    static /* synthetic */ int access$502(WcPlayerStatus x0, int x1) {
        x0.currentServerId = x1;
        return x0.currentServerId;
    }

    static /* synthetic */ PlayerOnlineStatus access$602(WcPlayerStatus x0, PlayerOnlineStatus x1) {
        x0.status = x1;
        return x0.status;
    }

    static /* synthetic */ Logger access$700() {
        return logger;
    }

    static /* synthetic */ int access$500(WcPlayerStatus x0) {
        return x0.currentServerId;
    }

    static /* synthetic */ long access$200(WcPlayerStatus x0) {
        return x0.playerWurmId;
    }

    static /* synthetic */ String access$100(WcPlayerStatus x0) {
        return x0.playerName;
    }

    static /* synthetic */ long access$300(WcPlayerStatus x0) {
        return x0.lastLogin;
    }

    static /* synthetic */ long access$400(WcPlayerStatus x0) {
        return x0.lastLogout;
    }

    static /* synthetic */ PlayerOnlineStatus access$600(WcPlayerStatus x0) {
        return x0.status;
    }
}

