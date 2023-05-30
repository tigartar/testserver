package com.wurmonline.server.skills;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import javax.annotation.Nonnull;

public class SkillTemplate implements TimeConstants, Comparable<SkillTemplate> {
   private long decayTime = 86400000L;
   @Nonnull
   private int[] dependencies = MiscConstants.EMPTY_INT_ARRAY;
   String name = "Unknown skill";
   private float difficulty = 1.0F;
   private final int number;
   private final short type;
   boolean fightSkill = false;
   boolean thieverySkill = false;
   boolean ignoresEnemies = false;
   boolean isPriestSlowskillgain = false;
   long tickTime = 0L;
   public static final long TICKTIME_ZERO = 0L;
   public static final long TICKTIME_FIVE = 300000L;
   public static final long TICKTIME_ONE = 60000L;
   public static final long TICKTIME_TEN = 600000L;
   public static final long TICKTIME_TWENTY = 1200000L;
   public static final long TICKTIME_HOUR = 3600000L;
   private final float difficultyDivider = Servers.localServer.isChallengeServer() ? 50.0F : 1.0F;

   SkillTemplate(int aNumber, String aName, float aDifficulty, @Nonnull int[] aDependencies, long aDecayTime, short aType) {
      this.number = aNumber;
      this.name = aName;
      this.difficulty = aDifficulty / this.difficultyDivider;
      this.dependencies = aDependencies;
      this.decayTime = Math.max(aDecayTime, 1L);
      this.type = aType;
   }

   SkillTemplate(
      int aNumber, String aName, float aDifficulty, @Nonnull int[] aDependencies, long aDecayTime, short aType, boolean aFightingSkill, boolean aIgnoreEnemy
   ) {
      this(aNumber, aName, aDifficulty, aDependencies, aDecayTime, aType);
      this.fightSkill = aFightingSkill;
      this.ignoresEnemies = aIgnoreEnemy;
   }

   SkillTemplate(
      int aNumber, String aName, float aDifficulty, @Nonnull int[] aDependencies, long aDecayTime, short aType, boolean aThieverySkill, long _tickTime
   ) {
      this(aNumber, aName, aDifficulty, aDependencies, aDecayTime, aType);
      this.thieverySkill = aThieverySkill;
      if (this.thieverySkill) {
         this.ignoresEnemies = true;
      }

      this.tickTime = _tickTime;
   }

   public String getName() {
      return this.name;
   }

   @Nonnull
   int[] getDependencies() {
      return this.dependencies;
   }

   public float getDifficulty() {
      return this.difficulty;
   }

   public void setDifficulty(float newDifficulty) {
      this.difficulty = newDifficulty;
   }

   public long getDecayTime() {
      return this.decayTime;
   }

   public int getNumber() {
      return this.number;
   }

   public boolean isMission() {
      return this.number >= 10001 && this.number <= 10040 || this.number == 1005;
   }

   public short getType() {
      return this.type;
   }

   public int compareTo(SkillTemplate aTemplate) {
      return this.getName().compareTo(aTemplate.getName());
   }

   public long getTickTime() {
      return this.tickTime;
   }

   public boolean isSlowForPriests() {
      return this.isPriestSlowskillgain;
   }

   public void setIsSlowForPriests(boolean isSlow) {
      this.isPriestSlowskillgain = isSlow;
   }
}
