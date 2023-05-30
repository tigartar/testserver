package com.wurmonline.server.intra;

import java.nio.ByteBuffer;

public interface IntraServerConnectionListener {
   void reschedule(IntraClient var1);

   void remove(IntraClient var1);

   void commandExecuted(IntraClient var1);

   void commandFailed(IntraClient var1);

   void dataReceived(IntraClient var1);

   void receivingData(ByteBuffer var1);
}
