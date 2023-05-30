package com.wurmonline.server.effects;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchEffectException extends WurmServerException {
   private static final long serialVersionUID = 1247908282339057518L;

   public NoSuchEffectException(String message) {
      super(message);
   }

   public NoSuchEffectException(Throwable cause) {
      super(cause);
   }

   public NoSuchEffectException(String message, Throwable cause) {
      super(message, cause);
   }
}
