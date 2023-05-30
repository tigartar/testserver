package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchEntryException extends WurmServerException {
   private static final long serialVersionUID = 5813764704231108263L;

   public NoSuchEntryException(String message) {
      super(message);
   }

   public NoSuchEntryException(Throwable cause) {
      super(cause);
   }

   public NoSuchEntryException(String message, Throwable cause) {
      super(message, cause);
   }
}
