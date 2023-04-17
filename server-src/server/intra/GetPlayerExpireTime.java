/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.intra.IntraClient;
import com.wurmonline.server.intra.IntraCommand;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class GetPlayerExpireTime
extends IntraCommand
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(GetPlayerExpireTime.class.getName());
    private boolean done = false;
    private IntraClient client;
    private final long wurmId;

    private GetPlayerExpireTime(long playerId) {
        this.wurmId = playerId;
    }

    @Override
    public boolean poll() {
        if (Servers.isThisLoginServer()) {
            return true;
        }
        if (this.client == null) {
            try {
                this.client = new IntraClient(Servers.loginServer.INTRASERVERADDRESS, Integer.parseInt(Servers.loginServer.INTRASERVERPORT), this);
                this.client.login(Servers.loginServer.INTRASERVERPASSWORD, true);
            }
            catch (IOException iox) {
                this.done = true;
            }
        }
        if (this.client != null && !this.done) {
            if (System.currentTimeMillis() > this.timeOutAt) {
                this.done = true;
            }
            if (this.client.loggedIn) {
                try {
                    this.client.executeRequestPlayerPaymentExpire(this.wurmId);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                    this.done = true;
                }
            }
            if (!this.done) {
                try {
                    this.client.update();
                }
                catch (Exception ex) {
                    this.done = true;
                }
            }
        }
        if (this.done && this.client != null) {
            this.client.disconnect("Done");
            this.client = null;
        }
        return this.done;
    }

    @Override
    public void commandExecuted(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void commandFailed(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void dataReceived(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void reschedule(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void remove(IntraClient aClient) {
        this.done = true;
    }

    @Override
    public void receivingData(ByteBuffer buffer) {
        long expireTime = buffer.getLong();
        PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(this.wurmId);
        long oldExpire = info.getPaymentExpire();
        try {
            if (expireTime > oldExpire) {
                info.setPaymentExpire(expireTime);
                try {
                    Player p = Players.getInstance().getPlayer(this.wurmId);
                    p.getCommunicator().sendNormalServerMessage("Your payment expiration date is updated to " + WurmCalendar.formatGmt(expireTime));
                }
                catch (NoSuchPlayerException p) {}
            }
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        this.done = true;
    }
}

