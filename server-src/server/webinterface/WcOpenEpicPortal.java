/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.PortalQuestion;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcOpenEpicPortal
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcOpenEpicPortal.class.getName());
    private boolean open = true;

    public WcOpenEpicPortal(long _id, boolean toggleOpen) {
        super(_id, (short)12);
        this.open = toggleOpen;
    }

    public WcOpenEpicPortal(long _id, byte[] _data) {
        super(_id, (short)12, _data);
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
            dos.writeBoolean(this.open);
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void execute() {
        DataInputStream dis;
        block5: {
            dis = null;
            try {
                Player[] players;
                dis = new DataInputStream(new ByteArrayInputStream(this.getData()));
                PortalQuestion.epicPortalsEnabled = this.open = dis.readBoolean();
                for (Player p : players = Players.getInstance().getPlayers()) {
                    SoundPlayer.playSound("sound.music.song.mountaintop", p, 2.0f);
                }
                if (!Servers.localServer.LOGINSERVER) break block5;
                WcOpenEpicPortal wccom = new WcOpenEpicPortal(WurmId.getNextWCCommandId(), PortalQuestion.epicPortalsEnabled);
                wccom.sendFromLoginServer();
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
}

