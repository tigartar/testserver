package com.wurmonline.server.statistics;

public class ScoreNamePair implements Comparable<ScoreNamePair> {
   public final String name;
   public final ChallengeScore score;

   public ScoreNamePair(String owner, ChallengeScore score) {
      this.name = owner;
      this.score = score;
   }

   public int compareTo(ScoreNamePair namePair) {
      if (this.score.getPoints() > namePair.score.getPoints()) {
         return -1;
      } else {
         return this.name.toLowerCase().equals(namePair.name.toLowerCase()) && this.score.getPoints() == namePair.score.getPoints() ? 0 : 1;
      }
   }
}
