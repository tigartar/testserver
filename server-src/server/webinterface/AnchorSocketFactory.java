/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

final class AnchorSocketFactory
extends RMISocketFactory
implements Serializable {
    private static final long serialVersionUID = 720394327635467676L;
    private final InetAddress ipInterface;

    AnchorSocketFactory(InetAddress aIpInterface) {
        this.ipInterface = aIpInterface;
    }

    @Override
    public ServerSocket createServerSocket(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 50, this.ipInterface);
        }
        catch (Exception e) {
            System.out.println(e);
        }
        return serverSocket;
    }

    @Override
    public Socket createSocket(String dummy, int port) throws IOException {
        return new Socket(this.ipInterface, port);
    }

    public boolean equals(Object that) {
        return that != null && that.getClass() == this.getClass();
    }
}

