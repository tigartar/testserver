package com.wurmonline.server.creatures;

public class UsedAttackData {
   private float time;
   private int rounds;

   public UsedAttackData(float swingTime, int round) {
      this.time = swingTime;
      this.rounds = round;
   }

   public final int getRounds() {
      return this.rounds;
   }

   public final float getTime() {
      return this.time;
   }

   public void setRounds(int numberOfRounds) {
      this.rounds = numberOfRounds;
   }

   public void setTime(float newTime) {
      this.time = newTime;
   }

   public void update(float newTime) {
      this.time = Math.max(0.0F, newTime);
      this.rounds = Math.max(this.rounds - 1, 0);
   }
}
