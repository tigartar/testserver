/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.support.Trello;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcGlobalModeration
extends WebCommand
implements MiscConstants,
TimeConstants {
    private static final Logger logger = Logger.getLogger(WcGlobalModeration.class.getName());
    private boolean warning;
    private boolean ban;
    private boolean mute;
    private boolean unmute;
    private boolean muteWarn;
    private int hours;
    private int days;
    private String sender = "";
    private String reason = "";
    private String playerName = "";
    private byte senderPower = 0;

    public WcGlobalModeration(long id, String _sender, byte _senderPower, boolean _mute, boolean _unmute, boolean _mutewarn, boolean _ban, boolean _warning, int _hours, int _days, String _playerName, String _reason) {
        super(id, (short)14);
        this.sender = _sender;
        this.warning = _warning;
        this.ban = _ban;
        this.mute = _mute;
        this.unmute = _unmute;
        this.muteWarn = _mutewarn;
        this.hours = _hours;
        this.days = _days;
        this.reason = _reason;
        this.playerName = _playerName;
        this.senderPower = _senderPower;
    }

    public WcGlobalModeration(long id, byte[] data) {
        super(id, (short)14, data);
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
            dos.writeUTF(this.sender);
            dos.writeBoolean(this.ban);
            dos.writeBoolean(this.mute);
            dos.writeBoolean(this.unmute);
            dos.writeBoolean(this.muteWarn);
            dos.writeBoolean(this.warning);
            dos.writeUTF(this.playerName);
            dos.writeUTF(this.reason);
            dos.writeInt(this.days);
            dos.writeInt(this.hours);
            dos.writeByte(this.senderPower);
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
                block21: {
                    dis = null;
                    try {
                        Message mess;
                        block20: {
                            dis = new DataInputStream(new ByteArrayInputStream(WcGlobalModeration.this.getData()));
                            WcGlobalModeration.this.sender = dis.readUTF();
                            WcGlobalModeration.this.ban = dis.readBoolean();
                            WcGlobalModeration.this.mute = dis.readBoolean();
                            WcGlobalModeration.this.unmute = dis.readBoolean();
                            WcGlobalModeration.this.muteWarn = dis.readBoolean();
                            WcGlobalModeration.this.warning = dis.readBoolean();
                            WcGlobalModeration.this.playerName = dis.readUTF();
                            WcGlobalModeration.this.reason = dis.readUTF();
                            WcGlobalModeration.this.days = dis.readInt();
                            WcGlobalModeration.this.hours = dis.readInt();
                            WcGlobalModeration.this.senderPower = dis.readByte();
                            try {
                                Message mess2;
                                Player p = Players.getInstance().getPlayer(WcGlobalModeration.this.playerName);
                                if (WcGlobalModeration.this.ban && p.getPower() < WcGlobalModeration.this.senderPower) {
                                    try {
                                        mess2 = new Message(null, 3, ":Event", "You have been banned for " + WcGlobalModeration.this.days + " days and thrown out from the game.");
                                        mess2.setReceiver(p.getWurmId());
                                        Server.getInstance().addMessage(mess2);
                                        p.ban(WcGlobalModeration.this.reason, System.currentTimeMillis() + (long)WcGlobalModeration.this.days * 86400000L + (long)WcGlobalModeration.this.hours * 3600000L);
                                    }
                                    catch (Exception ex) {
                                        logger.log(Level.WARNING, ex.getMessage());
                                    }
                                }
                                if (WcGlobalModeration.this.mute && p.getPower() <= WcGlobalModeration.this.senderPower) {
                                    p.mute(true, WcGlobalModeration.this.reason, System.currentTimeMillis() + (long)WcGlobalModeration.this.days * 86400000L + (long)WcGlobalModeration.this.hours * 3600000L);
                                    mess2 = new Message(null, 3, ":Event", "You have been muted by " + WcGlobalModeration.this.sender + " for " + WcGlobalModeration.this.hours + " hours and cannot shout anymore. Reason: " + WcGlobalModeration.this.reason);
                                    mess2.setReceiver(p.getWurmId());
                                    Server.getInstance().addMessage(mess2);
                                }
                                if (WcGlobalModeration.this.unmute) {
                                    p.mute(false, "", 0L);
                                    mess2 = new Message(null, 3, ":Event", "You have been given your voice back and can shout again.");
                                    mess2.setReceiver(p.getWurmId());
                                    Server.getInstance().addMessage(mess2);
                                }
                                if (WcGlobalModeration.this.muteWarn && p.getPower() <= WcGlobalModeration.this.senderPower) {
                                    mess2 = new Message(null, 3, ":Event", WcGlobalModeration.this.sender + " issues a warning that you may be muted. Be silent for a while and try to understand why or change the subject of your conversation please.");
                                    mess2.setReceiver(p.getWurmId());
                                    Server.getInstance().addMessage(mess2);
                                    if (WcGlobalModeration.this.reason.length() > 0) {
                                        Message mess22 = new Message(null, 3, ":Event", "The reason for this is '" + WcGlobalModeration.this.reason + "'");
                                        mess22.setReceiver(p.getWurmId());
                                        Server.getInstance().addMessage(mess22);
                                    }
                                }
                                if (WcGlobalModeration.this.warning && p.getPower() < WcGlobalModeration.this.senderPower) {
                                    p.getSaveFile().warn();
                                    mess2 = new Message(null, 3, ":Event", "You have just received an official warning. Too many of these will get you banned from the game.");
                                    mess2.setReceiver(p.getWurmId());
                                    Server.getInstance().addMessage(mess2);
                                }
                            }
                            catch (NoSuchPlayerException nsp) {
                                if (!WcGlobalModeration.this.unmute) break block20;
                                try {
                                    PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(WcGlobalModeration.this.playerName);
                                    pinf.load();
                                    if (pinf.wurmId > 0L) {
                                        pinf.setMuted(false, "", 0L);
                                    }
                                }
                                catch (IOException ex) {
                                    if (!Servers.isThisATestServer()) break block20;
                                    logger.log(Level.WARNING, "Unable to find player:" + WcGlobalModeration.this.playerName + "." + ex.getMessage(), ex);
                                }
                            }
                        }
                        if (WcGlobalModeration.this.mute) {
                            Players.addMgmtMessage(WcGlobalModeration.this.sender, "mutes " + WcGlobalModeration.this.playerName + " for " + WcGlobalModeration.this.hours + " hours. Reason: " + WcGlobalModeration.this.reason);
                            mess = new Message(null, 9, "MGMT", "<" + WcGlobalModeration.this.sender + "> mutes " + WcGlobalModeration.this.playerName + " for " + WcGlobalModeration.this.hours + " hours. Reason: " + WcGlobalModeration.this.reason);
                            Server.getInstance().addMessage(mess);
                        }
                        if (WcGlobalModeration.this.unmute) {
                            Players.addMgmtMessage(WcGlobalModeration.this.sender, "unmutes " + WcGlobalModeration.this.playerName);
                            mess = new Message(null, 9, "MGMT", "<" + WcGlobalModeration.this.sender + "> unmutes " + WcGlobalModeration.this.playerName);
                            Server.getInstance().addMessage(mess);
                        }
                        if (WcGlobalModeration.this.muteWarn) {
                            Players.addMgmtMessage(WcGlobalModeration.this.sender, "mutewarns " + WcGlobalModeration.this.playerName + " (" + WcGlobalModeration.this.reason + ")");
                            mess = new Message(null, 9, "MGMT", "<" + WcGlobalModeration.this.sender + "> mutewarns " + WcGlobalModeration.this.playerName + " (" + WcGlobalModeration.this.reason + ")");
                            Server.getInstance().addMessage(mess);
                        }
                        if (!Servers.isThisLoginServer() || !WcGlobalModeration.this.mute && !WcGlobalModeration.this.muteWarn && !WcGlobalModeration.this.unmute) break block21;
                        Trello.addMessage(WcGlobalModeration.this.sender, WcGlobalModeration.this.playerName, WcGlobalModeration.this.reason, WcGlobalModeration.this.hours);
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
}

