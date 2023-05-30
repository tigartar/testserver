package com.wurmonline.server.players;

public abstract class JournalReward {
   private final String rewardDescription;

   public abstract void runReward(Player var1);

   public JournalReward(String rewardDescription) {
      this.rewardDescription = rewardDescription;
   }

   public String getRewardDesc() {
      return this.rewardDescription;
   }
}
