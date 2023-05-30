package com.wurmonline.server.creatures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoArmourException extends WurmServerException {
   private static final long serialVersionUID = 9021493151024263335L;

   public NoArmourException(String message) {
      super(message);
   }

   NoArmourException(Throwable cause) {
      super(cause);
   }

   NoArmourException(String message, Throwable cause) {
      super(message, cause);
   }
}
