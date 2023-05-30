package com.wurmonline.communication;

public interface ServerListener {
   void clientConnected(SocketConnection var1);

   void clientException(SocketConnection var1, Exception var2);
}
