package com.wurmonline.server;

public interface ServerMonitoring {
   boolean isLagging();

   byte[] getExternalIp();

   byte[] getInternalIp();

   int getIntraServerPort();
}
