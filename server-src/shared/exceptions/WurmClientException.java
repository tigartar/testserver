package com.wurmonline.shared.exceptions;

public class WurmClientException extends WurmException {
   private static final long serialVersionUID = 1268608703615765075L;

   public WurmClientException(String message) {
      super(message);
   }

   public WurmClientException(Throwable cause) {
      super(cause);
   }

   public WurmClientException(String message, Throwable cause) {
      super(message, cause);
   }
}
