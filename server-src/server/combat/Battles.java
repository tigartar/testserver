package com.wurmonline.server.combat;

import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

public final class Battles implements TimeConstants {
   private static final List<Battle> battles = new LinkedList<>();
   private static final Logger logger = Logger.getLogger(Battles.class.getName());

   private Battles() {
   }

   public static Battle getBattleFor(Creature creature) {
      for(Battle battle : battles) {
         if (battle.containsCreature(creature)) {
            return battle;
         }
      }

      return null;
   }

   public static Battle getBattleFor(Creature attacker, Creature defender) {
      Battle bone = getBattleFor(attacker);
      Battle btwo = getBattleFor(defender);
      Battle toReturn = null;
      if (bone == null && btwo == null) {
         toReturn = new Battle(attacker, defender);
         battles.add(toReturn);
      } else if (bone == null && btwo != null) {
         btwo.addCreature(attacker);
         toReturn = btwo;
      } else if (btwo == null && bone != null) {
         bone.addCreature(defender);
         toReturn = bone;
      } else {
         toReturn = mergeBattles(bone, btwo);
      }

      return toReturn;
   }

   public static Battle mergeBattles(Battle battleOne, Battle battleTwo) {
      if (battleTwo != null && battleOne != null) {
         Creature[] bonec = battleTwo.getCreatures();

         for(Creature lElement : bonec) {
            battleOne.addCreature(lElement);
         }

         battles.remove(battleTwo);
      } else {
         logger.warning("Cannot merge null battles: battleOne: " + battleOne + ", battleTwo: " + battleTwo);
      }

      return battleOne;
   }

   public static void poll(boolean shutdown) {
      long now = System.currentTimeMillis();
      ListIterator<Battle> it = battles.listIterator();

      while(it.hasNext()) {
         Battle battle = it.next();
         if (battle.getCreatures().length <= 1 || now - battle.getEndTime() > 300000L || shutdown) {
            battle.save();
            battle.clearCreatures();
            it.remove();
         }
      }
   }
}
