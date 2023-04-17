/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

import com.wurmonline.server.Servers;
import com.wurmonline.server.intra.IntraClient;
import com.wurmonline.server.intra.IntraServerConnectionListener;
import java.util.logging.Logger;

public abstract class IntraCommand
implements IntraServerConnectionListener {
    long startTime;
    long timeOutAt;
    long timeOutTime = 10000L;
    private static int nums = 0;
    static int num = 0;
    public int pollTimes = 0;
    static final Logger logger2 = Logger.getLogger("IntraServer");

    IntraCommand() {
        num = nums++;
        this.startTime = System.currentTimeMillis();
        this.timeOutAt = this.startTime + this.timeOutTime;
    }

    public abstract boolean poll();

    @Override
    public abstract void commandExecuted(IntraClient var1);

    @Override
    public abstract void commandFailed(IntraClient var1);

    @Override
    public abstract void dataReceived(IntraClient var1);

    boolean isThisLoginServer() {
        return Servers.isThisLoginServer();
    }

    String getLoginServerIntraServerAddress() {
        return Servers.loginServer.INTRASERVERADDRESS;
    }

    String getLoginServerIntraServerPort() {
        return Servers.loginServer.INTRASERVERPORT;
    }

    String getLoginServerIntraServerPassword() {
        return Servers.loginServer.INTRASERVERPASSWORD;
    }
}

