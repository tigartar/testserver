package com.wurmonline.server.villages;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchVillageException extends WurmServerException {
   private static final long serialVersionUID = 1L;

   public NoSuchVillageException(String message) {
      super(message);
   }

   NoSuchVillageException(Throwable cause) {
      super(cause);
   }

   NoSuchVillageException(String message, Throwable cause) {
      super(message, cause);
   }
}
