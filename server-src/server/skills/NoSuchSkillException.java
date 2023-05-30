package com.wurmonline.server.skills;

import com.wurmonline.shared.exceptions.WurmServerException;

public class NoSuchSkillException extends WurmServerException {
   private static final long serialVersionUID = 534621721301818809L;

   public NoSuchSkillException(String message) {
      super(message);
   }

   public NoSuchSkillException(Throwable cause) {
      super(cause);
   }

   public NoSuchSkillException(String message, Throwable cause) {
      super(message, cause);
   }
}
