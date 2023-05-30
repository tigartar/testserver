package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchPlayerException extends WurmServerException {
   private static final long serialVersionUID = 8878640068891218711L;

   public NoSuchPlayerException(String message) {
      super(message);
   }

   public NoSuchPlayerException(Throwable cause) {
      super(cause);
   }

   public NoSuchPlayerException(String message, Throwable cause) {
      super(message, cause);
   }
}
