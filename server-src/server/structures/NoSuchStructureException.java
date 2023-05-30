package com.wurmonline.server.structures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchStructureException extends WurmServerException {
   private static final long serialVersionUID = 7841234936326217783L;

   public NoSuchStructureException(String message) {
      super(message);
   }

   NoSuchStructureException(Throwable cause) {
      super(cause);
   }

   NoSuchStructureException(String message, Throwable cause) {
      super(message, cause);
   }
}
