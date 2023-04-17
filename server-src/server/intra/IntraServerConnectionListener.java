/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.intra;

import com.wurmonline.server.intra.IntraClient;
import java.nio.ByteBuffer;

public interface IntraServerConnectionListener {
    public void reschedule(IntraClient var1);

    public void remove(IntraClient var1);

    public void commandExecuted(IntraClient var1);

    public void commandFailed(IntraClient var1);

    public void dataReceived(IntraClient var1);

    public void receivingData(ByteBuffer var1);
}

