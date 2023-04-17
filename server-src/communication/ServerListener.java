/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.communication;

import com.wurmonline.communication.SocketConnection;

public interface ServerListener {
    public void clientConnected(SocketConnection var1);

    public void clientException(SocketConnection var1, Exception var2);
}

