package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchLockException extends WurmServerException {
   private static final long serialVersionUID = 2894616265258932169L;

   NoSuchLockException(String message) {
      super(message);
   }

   NoSuchLockException(Throwable cause) {
      super(cause);
   }

   NoSuchLockException(String message, Throwable cause) {
      super(message, cause);
   }
}
