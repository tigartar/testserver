package com.wurmonline.server.creatures;

public enum AttackIdentifier {
   STRIKE(0, "strike"),
   BITE(1, "bite"),
   MAUL(2, "maul"),
   CLAW(3, "claw"),
   HEADBUTT(4, "headbutt"),
   KICK(5, "kick");

   private final int id;
   private final String animationString;

   private AttackIdentifier(int _id, String animation) {
      this.id = _id;
      this.animationString = animation;
   }

   public final int getId() {
      return this.id;
   }

   public final String getAnimationString() {
      return this.animationString;
   }
}
