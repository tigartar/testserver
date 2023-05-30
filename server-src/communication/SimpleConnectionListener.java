package com.wurmonline.communication;

import java.nio.ByteBuffer;

public interface SimpleConnectionListener {
   void reallyHandle(int var1, ByteBuffer var2);
}
