package com.wurmonline.server.creatures;

import com.wurmonline.shared.exceptions.WurmServerException;

public final class NoSuchCreatureTemplateException extends WurmServerException {
   private static final long serialVersionUID = 9155452762336621872L;

   NoSuchCreatureTemplateException(String message) {
      super(message);
   }

   NoSuchCreatureTemplateException(Throwable cause) {
      super(cause);
   }

   NoSuchCreatureTemplateException(String message, Throwable cause) {
      super(message, cause);
   }
}
