package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchItemException extends WurmServerException {
   private static final long serialVersionUID = -4699460609829035442L;

   public NoSuchItemException(String message) {
      super(message);
   }

   public NoSuchItemException(Throwable cause) {
      super(cause);
   }

   public NoSuchItemException(String message, Throwable cause) {
      super(message, cause);
   }
}
