/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

public interface ServerMonitoring {
    public boolean isLagging();

    public byte[] getExternalIp();

    public byte[] getInternalIp();

    public int getIntraServerPort();
}

