package com.wurmonline.server.skills;

public final class SkillsFactory {
   private SkillsFactory() {
   }

   public static Skills createSkills(long id) {
      return new DbSkills(id);
   }

   public static Skills createSkills(String templateName) {
      return new DbSkills(templateName);
   }
}
