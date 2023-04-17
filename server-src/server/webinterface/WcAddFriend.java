/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
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

public final class WcAddFriend
extends WebCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(WcAddFriend.class.getName());
    public static final byte ASKING = 0;
    public static final byte UNKNOWN = 1;
    public static final byte OFFLINE = 2;
    public static final byte TIMEDOUT = 3;
    public static final byte ISBUSY = 4;
    public static final byte SUCCESS = 5;
    public static final byte REPLYING = 6;
    public static final byte FINISHED = 7;
    public static final byte IGNORED = 8;
    public static final byte SENT = 9;
    private byte reply;
    private String playerName;
    private byte playerKingdom;
    private String friendsName;
    private boolean xkingdom;

    public WcAddFriend(String aPlayerName, byte aKingdom, String aFriendName, byte aReply, boolean crossKingdom) {
        super(WurmId.getNextWCCommandId(), (short)25);
        this.reply = aReply;
        this.playerName = aPlayerName;
        this.playerKingdom = aKingdom;
        this.friendsName = aFriendName;
        this.xkingdom = crossKingdom;
    }

    public WcAddFriend(long aId, byte[] aData) {
        super(aId, (short)25, aData);
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
            dos.writeByte(this.reply);
            dos.writeUTF(this.playerName);
            dos.writeByte(this.playerKingdom);
            dos.writeUTF(this.friendsName);
            dos.writeBoolean(this.xkingdom);
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
                    dis = new DataInputStream(new ByteArrayInputStream(WcAddFriend.this.getData()));
                    WcAddFriend.this.reply = dis.readByte();
                    WcAddFriend.this.playerName = dis.readUTF();
                    WcAddFriend.this.playerKingdom = dis.readByte();
                    WcAddFriend.this.friendsName = dis.readUTF();
                    WcAddFriend.this.xkingdom = dis.readBoolean();
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
                byte newReply = 7;
                if (Servers.isThisLoginServer()) {
                    newReply = WcAddFriend.this.sendToPlayerServer(WcAddFriend.this.friendsName);
                }
                if (newReply == 7) {
                    try {
                        Player p = Players.getInstance().getPlayer(WcAddFriend.this.friendsName);
                        newReply = p.remoteAddFriend(WcAddFriend.this.playerName, WcAddFriend.this.playerKingdom, WcAddFriend.this.reply, true, WcAddFriend.this.xkingdom);
                    }
                    catch (NoSuchPlayerException e) {
                        newReply = 2;
                    }
                }
                if (newReply != 7 && newReply != 9) {
                    WcAddFriend waf = new WcAddFriend(WcAddFriend.this.friendsName, WcAddFriend.this.playerKingdom, WcAddFriend.this.playerName, newReply, true);
                    waf.sendToServer(WurmId.getOrigin(WcAddFriend.this.getWurmId()));
                }
            }
        }.start();
    }

    public byte sendToPlayerServer(String aFriendsName) {
        PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(aFriendsName);
        if (pInfo != null) {
            try {
                pInfo.load();
                if (pInfo.currentServer != Servers.getLocalServerId()) {
                    this.sendToServer(pInfo.currentServer);
                    return 9;
                }
                return 7;
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        return 1;
    }

    static /* synthetic */ byte access$002(WcAddFriend x0, byte x1) {
        x0.reply = x1;
        return x0.reply;
    }

    static /* synthetic */ String access$102(WcAddFriend x0, String x1) {
        x0.playerName = x1;
        return x0.playerName;
    }

    static /* synthetic */ byte access$202(WcAddFriend x0, byte x1) {
        x0.playerKingdom = x1;
        return x0.playerKingdom;
    }

    static /* synthetic */ String access$302(WcAddFriend x0, String x1) {
        x0.friendsName = x1;
        return x0.friendsName;
    }

    static /* synthetic */ boolean access$402(WcAddFriend x0, boolean x1) {
        x0.xkingdom = x1;
        return x0.xkingdom;
    }

    static /* synthetic */ Logger access$500() {
        return logger;
    }

    static /* synthetic */ String access$300(WcAddFriend x0) {
        return x0.friendsName;
    }

    static /* synthetic */ String access$100(WcAddFriend x0) {
        return x0.playerName;
    }

    static /* synthetic */ byte access$200(WcAddFriend x0) {
        return x0.playerKingdom;
    }

    static /* synthetic */ byte access$000(WcAddFriend x0) {
        return x0.reply;
    }

    static /* synthetic */ boolean access$400(WcAddFriend x0) {
        return x0.xkingdom;
    }
}

