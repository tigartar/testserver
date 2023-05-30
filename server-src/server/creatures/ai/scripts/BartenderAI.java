package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.villages.Village;

public class BartenderAI extends CreatureAI {
   private static final long MIN_TIME_TALK = 120000L;
   private static final long MIN_TIME_NEWPATH = 30000L;
   private static final int TIMER_SPECTALK = 0;
   private static final int TIMER_NEWPATH = 1;

   @Override
   public void creatureCreated(Creature c) {
      if (c.getCurrentTile().getVillage() != null) {
         ((BartenderAI.BartenderAIData)c.getCreatureAIData()).setHomeVillage(c.getCurrentTile().getVillage());
      }
   }

   @Override
   protected boolean pollSpecialFinal(Creature c, long delta) {
      this.increaseTimer(c, delta, new int[]{0});
      if (!this.isTimerReady(c, 0, 120000L)) {
         return false;
      } else {
         c.say("Come and get some tasty treats!");
         this.resetTimer(c, new int[]{0});
         return false;
      }
   }

   @Override
   protected boolean pollMovement(Creature c, long delta) {
      BartenderAI.BartenderAIData aiData = (BartenderAI.BartenderAIData)c.getCreatureAIData();
      if (aiData.getFoodTarget() != null && aiData.getFoodTarget().getTileX() == c.getTileX() && aiData.getFoodTarget().getTileY() == c.getTileY()) {
         c.say("Hey " + aiData.getFoodTarget().getName() + " you look hungry, come and get some food!");
         aiData.setFoodTarget(null);
      }

      if (c.getStatus().getPath() == null) {
         if (aiData.getHomeVillage() != null && c.getCurrentTile().getVillage() != aiData.getHomeVillage()) {
            c.startPathingToTile(this.getMovementTarget(c, aiData.getHomeVillage().getTokenX(), aiData.getHomeVillage().getTokenY()));
            return false;
         }

         this.increaseTimer(c, delta, new int[]{1});
         if (this.isTimerReady(c, 1, 30000L)) {
            if (Server.rand.nextInt(100) < 10) {
               Creature[] nearbyCreatures = c.getCurrentTile().getZone().getAllCreatures();

               for(Creature otherC : nearbyCreatures) {
                  if (otherC != c
                     && otherC.isPlayer()
                     && otherC.getStatus().isHungry()
                     && otherC.getCurrentTile().getVillage() == aiData.getHomeVillage()
                     && (otherC.getTileX() != c.getTileX() || otherC.getTileY() != c.getTileY())) {
                     c.startPathingToTile(this.getMovementTarget(c, otherC.getTileX(), otherC.getTileY()));
                     aiData.setFoodTarget(otherC);
                  }
               }
            }

            this.resetTimer(c, new int[]{1});
         }
      } else {
         this.pathedMovementTick(c);
         if (c.getStatus().getPath().isEmpty()) {
            c.getStatus().setPath(null);
            c.getStatus().setMoving(false);
         }
      }

      return false;
   }

   @Override
   protected boolean pollAttack(Creature c, long delta) {
      return false;
   }

   @Override
   protected boolean pollBreeding(Creature c, long delta) {
      return false;
   }

   @Override
   public CreatureAIData createCreatureAIData() {
      return new BartenderAI.BartenderAIData();
   }

   class BartenderAIData extends CreatureAIData {
      private Creature currentFoodTarget = null;
      private Village homeVillage = null;

      void setHomeVillage(Village homeVillage) {
         this.homeVillage = homeVillage;
      }

      Village getHomeVillage() {
         return this.homeVillage;
      }

      void setFoodTarget(Creature newFoodTarget) {
         this.currentFoodTarget = newFoodTarget;
      }

      Creature getFoodTarget() {
         return this.currentFoodTarget;
      }
   }
}
