package com.wurmonline.server.support;

import com.wurmonline.shared.exceptions.WurmServerException;

public class TrelloCardNotFoundException extends WurmServerException {
   private static final long serialVersionUID = 7427993543996731841L;

   public TrelloCardNotFoundException(String message) {
      super(message);
   }

   public TrelloCardNotFoundException(Throwable cause) {
      super(cause);
   }

   public TrelloCardNotFoundException(String message, Throwable cause) {
      super(message, cause);
   }
}
