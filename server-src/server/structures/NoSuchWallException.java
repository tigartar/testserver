package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchWallException extends WurmServerException {
   private static final long serialVersionUID = 2443093162318322030L;

   public NoSuchWallException(String message) {
      super(message);
   }

   NoSuchWallException(Throwable cause) {
      super(cause);
   }

   NoSuchWallException(String message, Throwable cause) {
      super(message, cause);
   }
}
